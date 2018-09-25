package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.features.hooks.Hook;
import com.mercadopago.android.px.internal.features.providers.CheckoutProvider;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.ChangePaymentMethodPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.RecoverPaymentPostPaymentAction;
import com.mercadopago.android.px.mocks.PaymentMethodSearchs;
import com.mercadopago.android.px.mocks.Payments;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Customer;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.utils.PluginInitializationSuccess;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import com.mercadopago.android.px.viewmodel.mappers.BusinessModelMapper;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubExpiredPreference;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItem;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItemAndPayer;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPresenterTest {

    private static final String DEFAULT_CARD_ID = "260077840";
    private static final String DEBIT_CARD_DEBCABAL = "debcabal";

    @Mock private CheckoutView checkoutView;
    @Mock private CheckoutProvider checkoutProvider;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private AmountRepository amountRepository;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private PluginRepository pluginRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private InternalConfiguration internalConfiguration;
    @Mock private BusinessModelMapper businessModelMapper;

    private MockedView stubView;
    private MockedProvider stubProvider;

    @Before
    public void setUp() {
        stubView = new MockedView();
        stubProvider = new MockedProvider();
        when(discountRepository.configureDiscountAutomatically(amountRepository.getAmountToPay()))
            .thenReturn(new StubSuccessMpCall<>(true));
    }

    @NonNull
    private CheckoutPresenter getPresenter() {
        return getBasePresenter(checkoutView, checkoutProvider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenterWithDefaultAdvancedConfigurationMla() {
        final CheckoutPreference preference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);

        stubProvider.setCheckoutPreferenceResponse(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        return getBasePresenter(stubView, stubProvider);
    }

    @NonNull
    private CheckoutPresenter getPaymentPresenterWithOnlyAccountMoneyMLA() {
        final CheckoutPreference preference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getPaymentMethodSearchWithOnlyAccountMoneyMLA()));
        return getPresenter();
    }

    @NonNull
    private CheckoutPresenter getBasePresenter(
        final CheckoutView view,
        final CheckoutProvider provider) {

        when(pluginRepository.getInitTask(false)).thenReturn(new PluginInitializationSuccess());

        final CheckoutStateModel model = new CheckoutStateModel();
        final CheckoutPresenter presenter = new CheckoutPresenter(model, paymentSettingRepository, amountRepository,
            userSelectionRepository, discountRepository,
            groupsRepository,
            pluginRepository,
            paymentRepository,
            internalConfiguration,
            businessModelMapper
        );
        presenter.attachResourcesProvider(provider);
        presenter.attachView(view);
        return presenter;
    }

    private void whenFlowHasRecoverableTokenProcess(final Payment payment) {
        final Token token = mock(Token.class);
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);
        final PayerCost payerCost = mock(PayerCost.class);
        final Issuer issuer = mock(Issuer.class);

        when(paymentSettingRepository.getToken()).thenReturn(token);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(userSelectionRepository.getPayerCost()).thenReturn(payerCost);
        when(userSelectionRepository.getIssuer()).thenReturn(issuer);
        when(payment.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_REJECTED);
        when(payment.getPaymentStatusDetail())
            .thenReturn(Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE);
    }

    @Test
    public void whenChoHasPrefIdSetRetrievePreferenceFromMercadoPago() {
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(null);
        when(paymentSettingRepository.getCheckoutPreferenceId()).thenReturn("some_pref_id");
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();
        verify(checkoutProvider).getCheckoutPreference(any(String.class), any(TaggedCallback.class));
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenChoHasCompletePrefSetDoNotCallProviderToGetPreference() {
        final CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        final CheckoutPresenter presenter = getPresenter();
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        presenter.initialize();
        verify(checkoutProvider).fetchFonts();
        verify(checkoutProvider, times(0)).getCheckoutPreference(any(String.class), any(TaggedCallback.class));
    }

    @Test
    public void whenPreferenceIsExpiredThenShowErrorInView() {
        final CheckoutPreference preference = stubExpiredPreference();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();
        verify(checkoutProvider).getCheckoutExceptionMessage(any(CheckoutPreferenceException.class));
        verify(checkoutView).showError(any(MercadoPagoError.class));
    }

    @Test
    public void whenChoHasPreferenceAndPaymentMethodRetrivedShowPaymentMethodSelection() {
        final CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        final CheckoutPresenter presenter = getPresenter();
        presenter.initialize();

        verify(groupsRepository).getGroups();
        verify(checkoutView).showProgress();
        verify(checkoutView).initializeMPTracker();
        verify(checkoutView).trackScreen();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenAPaymentMethodIsSelectedThenShowReviewAndConfirm() {
        final CheckoutPresenter presenter = getPresenter();
        presenter.onPaymentMethodSelectionResponse();
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenDefaultCardIdValidSelectedThenShowSecurityCode() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        PaymentMethodSearch search = mockPaymentMethodSearchForDriver(true);
        presenter.startFlow(search);
        assertTrue(stubView.showingSavedCardFlow);
    }

    @Test
    public void whenDefaultCardIdInvalidSelectedThenShowPaymentVault() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        PaymentMethodSearch search = mockPaymentMethodSearchForDriver(false);
        presenter.startFlow(search);
        assertTrue(stubView.showingPaymentMethodSelection);
    }

    @Test
    public void whenDefaultCardIdIsNullAndDefaultPaymentTypeIsValidThenShowNewCardFlow() {
        final CheckoutPresenter presenter = getBasePresenter(stubView, stubProvider);
        final PaymentMethodSearch search = mockPaymentMethodSearchForNewCardDriver();
        presenter.startFlow(search);
        assertTrue(stubView.showingNewCardFlow);
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForNewCardDriver() {
        final PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(mock(PaymentPreference.class));
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId())
            .thenReturn(null);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentTypeId())
            .thenReturn("debit_card");
        return search;
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForDriver(boolean isValidCard) {
        PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        PaymentMethod paymentMethod = mock(PaymentMethod.class);
        when(paymentMethod.getPaymentTypeId()).thenReturn("debit_card");
        final ArrayList settingsList = mock(ArrayList.class);
        final Setting setting = mock(Setting.class);
        when(setting.getSecurityCode()).thenReturn(null);
        when(settingsList.get(any(int.class))).thenReturn(setting);
        when(paymentMethod.getSettings()).thenReturn(settingsList);
        when(search.getPaymentMethodById(any(String.class))).thenReturn(paymentMethod);
        if (isValidCard) {
            when(search.getCardById(any(String.class))).thenReturn(new Card());
        } else {
            when(search.getCardById(any(String.class))).thenReturn(null);
        }
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        final PaymentPreference paymentPreference = mock(PaymentPreference.class);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId()).thenReturn(
            DEFAULT_CARD_ID);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentMethodId())
            .thenReturn(DEBIT_CARD_DEBCABAL);
        return search;
    }

    @Test
    public void whenPaymentMethodCanceledThenCancelCheckout() {
        final CheckoutPresenter presenter = getPresenter();
        presenter.onPaymentMethodSelectionCancel();
        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenPaymentResultWithCreatedPaymentThenFinishCheckoutWithPaymentResult() {
        final CheckoutPresenter presenter = getPresenter();
        final Payment payment = mock(Payment.class);

        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.getPayment()).thenReturn(payment);

        presenter.onPaymentResultResponse();

        verify(checkoutView).finishWithPaymentResult(payment);
    }

    @Test
    public void whenPaymentResultWithoutCreatedPaymentThenFinishCheckoutWithoutPaymentResult() {
        final CheckoutPresenter presenter = getPresenter();

        presenter.onPaymentResultResponse();

        verify(checkoutView).finishWithPaymentResult();
    }

    @Test
    public void whenPaymentIsCanceledBecauseUserWantsToSelectOtherPaymentMethodThenShowPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPresenter();

        presenter.changePaymentMethod();

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutProvider);
    }

    @Test
    public void whenUserSelectChangePaymentMethodFromPaymentResultAndExitOnIsTrueThenNotShowPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPresenter();
        when(paymentRepository.getPayment()).thenReturn(mock(Payment.class));
        when(internalConfiguration.shouldExitOnPaymentMethodChange()).thenReturn(true);

        presenter.changePaymentMethod();

        verify(checkoutView).finishWithPaymentResult(Constants.RESULT_CHANGE_PAYMENT_METHOD, (Payment) paymentRepository.getPayment());
    }

    @Test
    public void whenUserSelectChangePaymentMethodFromReviewAndConfirmAndExitOnIsTrueThenNotShowPaymentMehotdSelection() {
        final CheckoutPresenter presenter = getPresenter();

        when(internalConfiguration.shouldExitOnPaymentMethodChange()).thenReturn(true);

        presenter.onChangePaymentMethod();

        verify(checkoutView).finishWithPaymentResult(RESULT_CHANGE_PAYMENT_METHOD);
    }

    @Test
    public void whenPaymentNeedsRecoveryFromReviewAndConfirmThenStartPaymentRecoveryFlow() {
        final CheckoutPresenter presenter = getPresenter();

        final RecoverPaymentPostPaymentAction action =
            new RecoverPaymentPostPaymentAction(PostPaymentAction.OriginAction.REVIEW_AND_CONFIRM);
        action.execute(presenter);

        verify(checkoutView).showReviewAndConfirmAndRecoverPayment(false, action);
    }

    @Test
    public void whenPaymentNeedsRecoveryFromOneTapThenStartPaymentRecoveryFlow() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);
        when(paymentRepository.createPaymentRecovery()).thenReturn(paymentRecovery);

        final RecoverPaymentPostPaymentAction action =
            new RecoverPaymentPostPaymentAction(PostPaymentAction.OriginAction.ONE_TAP);
        action.execute(presenter);

        verify(checkoutView).startPaymentRecoveryFlow(paymentRecovery);
    }

    //TODO verify, should not happen
    @Ignore
    @Test
    public void whenPaymentIsCanceledBecausePaymentRecoveryIsRequiredButPaymentRecoveryCreationIsNotValidThenShowMercadoPagoError() {
        final CheckoutPresenter presenter = getPresenter();
        when(paymentRepository.createPaymentRecovery()).thenReturn(null);

        final RecoverPaymentPostPaymentAction action =
            new RecoverPaymentPostPaymentAction(PostPaymentAction.OriginAction.REVIEW_AND_CONFIRM);
        action.execute(presenter);

        verify(checkoutView).showError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCardFlowResponseHasRecoverableTokenProcessThenCreatePaymentInOneTap() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);
        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.createPaymentRecovery()).thenReturn(paymentRecovery);
        when(paymentRecovery.isTokenRecoverable()).thenReturn(true);

        presenter.onCardFlowResponse();

        verify(checkoutView).startPayment();
    }

    @Test
    public void whenCardFlowResponseHasNotRecoverableTokenProcessAndThereIsNoAvailableHooksThenShowReviewAndConfirm() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentData paymentData = mock(PaymentData.class);

        when(paymentRepository.hasPayment()).thenReturn(false);

        when(paymentRepository.getPaymentData()).thenReturn(paymentData);

        presenter.onCardFlowResponse();

        verify(paymentRepository).hasPayment();
        verify(paymentRepository).getPaymentData();
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(paymentRepository);
    }

    //Backs
    @Test
    public void ifCheckoutInitiatedAndUserPressesBackCancelCheckout() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);
        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.checkoutCanceled);
    }

    @Test
    public void whenPaymentMethodEditionIsRequestedAndUserPressesBackTwiceCancelCheckout() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionResponse();
        assertTrue(stubView.showingReviewAndConfirm);

        presenter.onChangePaymentMethodFromReviewAndConfirm();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.showingReviewAndConfirm);

        presenter.onReviewAndConfirmCancel();
        assertTrue(stubView.showingPaymentMethodSelection);

        presenter.onPaymentMethodSelectionCancel();
        assertTrue(stubView.checkoutCanceled);
    }

    @Test
    public void whenReviewAndConfirmCanceledAndOnlyOnePaymentMethodCancelCheckout() {
        final CheckoutPresenter presenter = getPaymentPresenterWithOnlyAccountMoneyMLA();

        presenter.initialize();
        presenter.onReviewAndConfirmCancel();

        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenPaymentSelectionErrorAndPaymentMethodChangeRequestedFromReviewAndConfirmThenBackToReviewAndConfirm() {
        final CheckoutPresenter presenter = getPresenter();
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        presenter.onChangePaymentMethodFromReviewAndConfirm();
        presenter.onPaymentMethodSelectionError(mercadoPagoError);

        verify(checkoutView).transitionOut();
        verify(checkoutView).showPaymentMethodSelection();
        verify(checkoutView).backToReviewAndConfirm();

        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentSelectionErrorAndPaymentMethodChangeNotRequestedFromReviewAndConfirmThenCancelCheckout() {
        final CheckoutPresenter presenter = getPresenter();
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        presenter.onPaymentMethodSelectionError(mercadoPagoError);

        verify(checkoutView).cancelCheckout(mercadoPagoError);

        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenErrorShownAndInvalidIdentificationThenGoBackToPaymentMethodSelection() {
        final CheckoutPresenter presenter = getPresenter();
        final ApiException apiException = Payments.getInvalidIdentificationPayment();
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenErrorShownAndValidIdentificationThenCancelCheckout() {
        final CheckoutPresenter presenter = getPresenter();
        final ApiException apiException = mock(ApiException.class);
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void ifNewFlowThenDoTrackInit() {
        final CheckoutPresenter presenter = getPaymentPresenterWithDefaultAdvancedConfigurationMla();
        presenter.initialize();
        assertTrue(stubView.initTracked);
    }

    private static class MockedView implements CheckoutView {

        MercadoPagoError errorShown;
        boolean showingError = false;
        boolean showingPaymentMethodSelection = false;
        boolean showingReviewAndConfirm = false;
        boolean initTracked = false;
        PaymentData paymentDataFinalResponse;
        boolean showingPaymentResult = false;
        boolean checkoutCanceled = false;
        boolean showingNewCardFlow = false;
        boolean showingSavedCardFlow = false;
        Payment paymentFinalResponse;
        boolean finishedCheckoutWithoutPayment = false;
        boolean showingPaymentRecoveryFlow = false;
        PaymentRecovery paymentRecoveryRequested;

        @Override
        public void showBusinessResult(final BusinessPaymentModel model) {
            //Do nothing
        }

        @Override
        public void showOneTap(@NonNull final OneTapModel oneTapModel) {
            //Do nothing
        }

        @Override
        public void hideProgress() {
            //Do nothing
        }

        @Override
        public void exitCheckout(final int resCode) {
            //Do nothing
        }

        @Override
        public void transitionOut() {
            //Do nothing
        }

        @Override
        public void showSavedCardFlow(final Card card) {
            this.showingSavedCardFlow = true;
        }

        @Override
        public void showNewCardFlow() {
            this.showingNewCardFlow = true;
        }

        @Override
        public void showReviewAndConfirmAndRecoverPayment(final boolean isUniquePaymentMethod,
            @NonNull final PostPaymentAction postPaymentAction) {

        }

        @Override
        public void startPayment() {

        }

        @Override
        public void showError(MercadoPagoError error) {
            this.showingError = true;
            this.errorShown = error;
        }

        @Override
        public void showProgress() {
            //Do nothing
        }

        @Override
        public void showReviewAndConfirm(final boolean isUniquePaymentMethod) {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentMethodSelection() {
            showingPaymentMethodSelection = true;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void showPaymentResult(final PaymentResult paymentResult) {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = true;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void backToReviewAndConfirm() {
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = true;
            showingPaymentResult = false;
            showingPaymentRecoveryFlow = false;
        }

        @Override
        public void finishWithPaymentResult() {
            finishedCheckoutWithoutPayment = true;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode) {

        }

        @Override
        public void finishWithPaymentResult(Payment payment) {
            paymentFinalResponse = payment;
        }

        @Override
        public void finishWithPaymentResult(Integer customResultCode, Payment payment) {

        }

        @Override
        public void cancelCheckout() {
            checkoutCanceled = true;
        }

        @Override
        public void cancelCheckout(MercadoPagoError mercadoPagoError) {

        }

        @Override
        public void cancelCheckout(final Integer customResultCode, final Boolean paymentMethodEdited) {

        }

        @Override
        public void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery) {
            paymentRecoveryRequested = paymentRecovery;
            showingPaymentRecoveryFlow = true;
            showingPaymentMethodSelection = false;
            showingReviewAndConfirm = false;
            showingPaymentResult = false;
        }

        @Override
        public void initializeMPTracker() {

        }

        @Override
        public void trackScreen() {
            initTracked = true;
        }

        @Override
        public void showHook(Hook hook, int requestCode) {

        }

        @Override
        public void showPaymentProcessor() {

        }

        @Override
        public boolean isActive() {
            return true;
        }
    }

    public class MockedProvider implements CheckoutProvider {

        private CheckoutPreference preference;
        private boolean paymentMethodSearchRequested = false;
        private PaymentMethodSearch paymentMethodSearchResponse;
        private Payment paymentResponse;
        private boolean paymentRequested;
        private Customer customerResponse;

        private String transactionId;
        private String paymentCustomerId;
        private PaymentMethod paymentMethodPaid;
        private Payer payerPosted;

        private boolean shouldFail = false;
        private MercadoPagoError failedResponse;
        public boolean manageEscRequested = false;

        @Override
        public void fetchFonts() {
            //TODO
        }

        @Override
        public void getCheckoutPreference(String checkoutPreferenceId,
            TaggedCallback<CheckoutPreference> taggedCallback) {
            //TODO
        }

        @Override
        public String getCheckoutExceptionMessage(CheckoutPreferenceException exception) {
            return null;
        }

        @Override
        public String getCheckoutExceptionMessage(final Exception exception) {
            return null;
        }

        public void setCheckoutPreferenceResponse(CheckoutPreference preference) {
            this.preference = preference;
        }
    }
}