package com.mercadopago.android.px.cardvault;

import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultPresenter;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultView;
import com.mercadopago.android.px.internal.features.providers.CardVaultProvider;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.mocks.Cards;
import com.mercadopago.android.px.mocks.Installments;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.mocks.PayerCosts;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.mocks.Tokens;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Installment;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardVaultPresenterTest {

    private MockedView mockedView = new MockedView();
    private MockedProvider provider = new MockedProvider();
    private CardVaultPresenter presenter;

    @Mock private AmountRepository amountRepository;

    @Mock private PaymentSettingRepository paymentSettingRepository;

    @Mock private UserSelectionRepository userSelectionRepository;

    @Mock private CheckoutPreference checkoutPreference;

    @Mock private MercadoPagoESC mercadoPagoESC;
    @Before
    public void setUp() {
        //Simulation no charge - no discount
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(new PaymentPreference());
        when(amountRepository.getAmountToPay()).thenReturn(new BigDecimal(1000));
        presenter = new CardVaultPresenter(amountRepository, userSelectionRepository, paymentSettingRepository,
            mercadoPagoESC);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
    }

    @Test
    public void ifInstallmentsEnabledAndSavedCardSetThenGetInstallmentsForCard() {

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());
        presenter.initialize();
        List<PayerCost> expectedPayerCosts = presenter.getPayerCostList();
        List<PayerCost> mockedPayerCosts = installmentsList.get(0).getPayerCosts();

        assertEquals(expectedPayerCosts.size(), mockedPayerCosts.size());
        assertTrue(expectedPayerCosts.size() > 1);
        assertNull(presenter.getPayerCost());
    }

    @Test
    public void ifInstallmentsForCardHasOnePayerCostThenSelectIt() {

        List<Installment> installmentsList = Installments.getInstallmentsListWithUniquePayerCost();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        List<PayerCost> expectedPayerCosts = presenter.getPayerCostList();
        assertTrue(expectedPayerCosts.size() == 1);
        assertNotNull(presenter.getPayerCost());
    }

    @Test
    public void ifInstallmentsForCardisEmptyhenShowErrorMessage() {

        List<Installment> installmentsList = new ArrayList<>();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertEquals(MockedProvider.MISSING_INSTALLMENTS, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInstallmentsForCardHasMultiplePayerCostsThenShowErrorMessage() {

        List<Installment> installmentsList = Installments.getInstallmentsListWithMultiplePayerCost();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertEquals(MockedProvider.MULTIPLE_INSTALLMENTS, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInstallmentsForCardFailsThenShowErrorMessage() {

        ApiException apiException = Installments.getDoNotFindInstallmentsException();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setResponse(mpException);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertTrue(provider.failedResponse.getApiException().getError().equals(provider.INSTALLMENTS_NOT_FOUND_ERROR));
    }

    @Test
    public void ifInstallmentsEnabledAndSavedCardSetThenStartInstallmentsFlow() {

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertTrue(mockedView.installmentsFlowStarted);
    }

    @Test
    public void ifPaymentPreferenceHasDefaultInstallmentsForSavedCardThenSelectIt() {

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        int mockedDefaultInstallment = 3;

        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(mockedDefaultInstallment);

        presenter.setCard(Cards.getCard());
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        presenter.initialize();

        assertNotNull(presenter.getPayerCost());
        assertTrue(presenter.getPayerCost().getInstallments() == mockedDefaultInstallment);
    }

    @Test
    public void ifPaymentPreferenceHasDefaultInstallmentsForSavedCardThenStartSecurityCodeFlow() {
        int mockedDefaultInstallment = 3;
        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(mockedDefaultInstallment);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);
        presenter.setCard(Cards.getCard());
        presenter.initialize();
        assertTrue(mockedView.securityCodeFlowStarted);
    }

    @Test
    public void ifInstallmentsForCardHasNoPayerCostsThenShowErrorMessage() {

        List<Installment> installmentsList = Installments.getInstallmentsListWithoutPayerCosts();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertEquals(MockedProvider.MISSING_PAYER_COSTS, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifPaymentRecoveryIsSetThenStartTokenRecoverableFlow() {

        Token mockedToken = Tokens.getToken();
        PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        PayerCost mockedPayerCost = PayerCosts.getPayerCost();
        Issuer mockedIssuer = Issuers.getIssuerMLA();
        String mockedPaymentStatus = Payment.StatusCodes.STATUS_REJECTED;
        String mockedPaymentStatusDeatil = Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE;

        PaymentRecovery mockedPaymentRecovery =
            new PaymentRecovery(mockedToken, mockedPaymentMethod, mockedPayerCost, mockedIssuer, mockedPaymentStatus,
                mockedPaymentStatusDeatil);

        presenter.setPaymentRecovery(mockedPaymentRecovery);

        presenter.initialize();

        assertNotNull(presenter.getCardInfo());
        assertNotNull(presenter.getPaymentMethod());
        assertNotNull(presenter.getToken());
        assertTrue(mockedView.recoverableTokenFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
    }

    @Test
    public void ifNothingIsSetThenStartNewCardFlow() {

        presenter.initialize();

        assertTrue(mockedView.guessingFlowStarted);
    }

    @Test
    public void whenNewCardDataAskedButNoIssuerResolvedThenAskForIssuer() {

        presenter.initialize();

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();
        final List<PayerCost> mockedPayerCostList = PayerCosts.getPayerCostList();

        userSelectionRepository.select(mockedPaymentMethod);
        when(paymentSettingRepository.getToken()).thenReturn(mockedToken);

        //Response from GuessingCardActivity, without an issuer selected
        presenter.resolveNewCardRequest(mockedPayerCost, mockedPayerCostList);

        assertTrue(mockedView.issuerFlowStarted);
    }

    @Test
    public void whenNewCardDataAskedButNoPayerCostResolvedThenAskForInstallments() {

        presenter.initialize();

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final Issuer mockedIssuer = Issuers.getIssuerMLA();
        final List<PayerCost> mockedPayerCostList = PayerCosts.getPayerCostList();
        userSelectionRepository.select(mockedPaymentMethod);
        userSelectionRepository.select(mockedIssuer);
        paymentSettingRepository.configure(mockedToken);
        when(userSelectionRepository.getIssuer()).thenReturn(mockedIssuer);
        when(paymentSettingRepository.getToken()).thenReturn(mockedToken);

        //Response from GuessingCardActivity, with an issuer selected
        presenter.resolveNewCardRequest(null,
            mockedPayerCostList);

        assertFalse(mockedView.issuerFlowStarted);
        assertTrue(mockedView.installmentsFlowStarted);
    }

    @Test
    public void whenNewCardDataAskedAndIssuerAndPayerCostResolvedThenFinishWithResult() {

        presenter.initialize();

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();
        final Issuer mockedIssuer = Issuers.getIssuerMLA();
        final List<PayerCost> mockedPayerCostList = PayerCosts.getPayerCostList();

        when(userSelectionRepository.getIssuer()).thenReturn(mockedIssuer);
        userSelectionRepository.select(mockedPaymentMethod);
        userSelectionRepository.select(mockedIssuer);
        when(paymentSettingRepository.getToken()).thenReturn(mockedToken);
        //Response from GuessingCardActivity, with an issuer selected
        presenter
            .resolveNewCardRequest(mockedPayerCost,
                mockedPayerCostList);

        assertFalse(mockedView.issuerFlowStarted);
        assertFalse(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.finishedWithResult);
    }

    @Test
    public void onIssuerResolvedAndPayerCostNotResolvedThenAskForPayerCost() {

        presenter.initialize();

        Token mockedToken = Tokens.getToken();
        PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        Issuer mockedIssuer = Issuers.getIssuerMLA();
        List<PayerCost> mockedPayerCostList = PayerCosts.getPayerCostList();

        presenter.setToken(mockedToken);
        presenter.setPaymentMethod(mockedPaymentMethod);
        presenter.setPayerCostsList(mockedPayerCostList);

        userSelectionRepository.select(mockedIssuer);

        //Response from IssuersActivity, with an issuer selected
        presenter.resolveIssuersRequest();

        assertTrue(mockedView.installmentsFlowStarted);
    }

    @Test
    public void whenPayerCostResolvedThenFinishWithResult() {

        presenter.initialize();

        Token mockedToken = Tokens.getToken();
        PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        presenter.setToken(mockedToken);
        presenter.setPaymentMethod(mockedPaymentMethod);
        //Response from InstallmentsActivity, with payer cost selected
        presenter.resolveInstallmentsRequest(mockedPayerCost);

        assertTrue(mockedView.finishedWithResult);
    }

    @Test
    public void whenPayerCostResolvedAndSavedCardSetThenAskForSecurityCode() {

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Response from InstallmentsActivity, with payer cost selected and saved card
        presenter.resolveInstallmentsRequest(mockedPayerCost);

        assertTrue(mockedView.securityCodeFlowStarted);
    }

    @Test
    public void whenSecurityCodeResolvedAndSavedCardSetThenFinishWithResult() {

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        final Token mockedToken = Tokens.getToken();

        paymentSettingRepository.configure(mockedToken);

        //Response from SecurityCodeActivity
        presenter.resolveSecurityCodeRequest();

        assertTrue(mockedView.finishedWithResult);
    }

    @Test
    public void onResponseCanceledThenCancelCardVault() {

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        presenter.onResultCancel();

        assertTrue(mockedView.cardVaultCanceled);
    }

    @Test
    public void whenSecurityCodeResolvedWithPaymentRecoverySetThenFinishWithResult() {

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();
        final Issuer mockedIssuer = Issuers.getIssuerMLA();
        final String mockedPaymentStatus = Payment.StatusCodes.STATUS_REJECTED;
        final String mockedPaymentStatusDetail = Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE;


        final PaymentRecovery mockedPaymentRecovery =
            new PaymentRecovery(mockedToken, mockedPaymentMethod, mockedPayerCost, mockedIssuer, mockedPaymentStatus,
                mockedPaymentStatusDetail);

        presenter.setPaymentRecovery(mockedPaymentRecovery);

        presenter.initialize();

        paymentSettingRepository.configure(mockedToken);
        when(paymentSettingRepository.getToken()).thenReturn(mockedToken);

        //Response from SecurityCodeActivity, with recoverable token
        presenter.resolveSecurityCodeRequest();

        assertNotNull(presenter.getPayerCost());
        assertNotNull(presenter.getToken());
        assertTrue(mockedView.finishedWithResult);
    }

    @Test
    public void ifInstallmentsForCardFailsThenRecoverRequest() {

        ApiException apiException = Installments.getDoNotFindInstallmentsException();
        MercadoPagoError mpException = new MercadoPagoError(apiException, "");
        provider.setResponse(mpException);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        assertTrue(mockedView.errorState);
        presenter.recoverFromFailure();

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        assertNotNull(installmentsList);
    }

    @Test
    public void onInstallmentsAskedThenAskForSecurityCodeWhenCardIdIsNotSaved() {

        provider.setESCEnabled(true);

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        presenter.setCard(Cards.getCard());

        presenter.initialize();

        PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Installments response
        presenter.resolveInstallmentsRequest(mockedPayerCost);
        assertTrue(mockedView.securityCodeFlowStarted);

        presenter.startSecurityCodeFlowIfNeeded();
        assertTrue(mockedView.securityCodeFlowStarted);
    }

    @Test
    public void onInstallmentsAskedThenDontAskForSecurityCodeWhenCardIdIsSaved() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);
        final Token mockedToken = Tokens.getTokenWithESC();
        provider.setResponse(mockedToken);

        presenter.initialize();

        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Installments response
        presenter.resolveInstallmentsRequest(mockedPayerCost);

        presenter.startSecurityCodeFlowIfNeeded();
        assertFalse(mockedView.securityCodeFlowStarted);
        assertEquals(provider.successfulTokenResponse.getId(), mockedToken.getId());
    }

    @Test
    public void onCreateTokenWithESCHasErrorThenAskForSecurityCode() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);

        presenter.initialize();

        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Set error with create token ESC
        final ApiException apiException = Tokens.getInvalidTokenWithESC();
        provider.setResponse(new MercadoPagoError(apiException, ""));
        //Installments onActivityResult
        presenter.resolveInstallmentsRequest(mockedPayerCost);

        assertTrue(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
        assertTrue(provider.deleteRequested);
        assertEquals(provider.cardIdDeleted, "12345");
    }

    @Test
    public void onCreateTokenWithESCHasErrorFingerprintThenAskForSecurityCode() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);

        presenter.initialize();

        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Set error with create token ESC
        final ApiException apiException = Tokens.getInvalidTokenWithESCFingerprint();
        provider.setResponse(new MercadoPagoError(apiException, ""));

        //Installments onActivityResult
        presenter.resolveInstallmentsRequest(mockedPayerCost);

        assertTrue(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
        assertTrue(provider.deleteRequested);
        assertEquals("12345", provider.cardIdDeleted);
    }

    @Test
    public void onESCDisabledThenAskForSecurityCodeWhenCardIdIsSaved() {

        //ESC disabled
        provider.setESCEnabled(false);

        List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);

        presenter.initialize();

        PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Installments response
        presenter.resolveInstallmentsRequest(mockedPayerCost);
        assertTrue(mockedView.securityCodeFlowStarted);

        Token mockedToken = Tokens.getToken();
        provider.setResponse(mockedToken);

        presenter.startSecurityCodeFlowIfNeeded();
        assertTrue(mockedView.securityCodeFlowStarted);

        assertEquals(provider.successfulTokenResponse.getId(), mockedToken.getId());
    }

    @Test
    public void onSavedCardWithESCSavedThenCreateTokenWithESC() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);

        //Set ESC to simulate it is saved
        presenter.setESC("12345678");

        presenter.initialize();

        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();
        final Token mockedToken = Tokens.getTokenWithESC();

        //Installments response
        provider.setResponse(mockedToken);
        presenter.resolveInstallmentsRequest(mockedPayerCost);
        //Set error with create token ESC
        assertEquals(provider.successfulTokenResponse.getId(), mockedToken.getId());
    }

    @Test
    public void whenAskInstallmentsAndSecurityCodeThenCloseFlowWithSlideAnimation() {
        //Ask for security code
        provider.setESCEnabled(false);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId("12345");
        presenter.setCard(mockedCard);

        presenter.initialize();

        final PayerCost mockedPayerCost = PayerCosts.getPayerCost();

        //Installments onActivityResult
        presenter.resolveInstallmentsRequest(mockedPayerCost);
        assertTrue(mockedView.securityCodeFlowStarted);
        assertTrue(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.animateSlide);
    }

    private static final class MockedProvider implements CardVaultProvider {

        private static final String MULTIPLE_INSTALLMENTS = "multiple installments";
        private static final String MISSING_INSTALLMENTS = "missing installments";
        private static final String MISSING_PAYER_COSTS = "missing payer costs";
        private static final String MISSING_AMOUNT = "missing amount";
        private static final String MISSING_PUBLIC_KEY = "missing public key";
        private static final String MISSING_SITE = "missing site";
        private static final String INSTALLMENTS_NOT_FOUND_ERROR = "installments not found error";

        private boolean shouldFail;
        private MercadoPagoError failedResponse;
        private List<Installment> successfulResponse;
        private Token successfulTokenResponse;
        private boolean escEnabled;
        private boolean deleteRequested;
        private String cardIdDeleted;

        public void setESCEnabled(boolean enabled) {
            this.escEnabled = enabled;
        }

        public void setResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }

        public void setResponse(List<Installment> installmentList) {
            shouldFail = false;
            successfulResponse = installmentList;
        }

        public void setResponse(Token token) {
            shouldFail = false;
            successfulTokenResponse = token;
        }

        @Override
        public String getMultipleInstallmentsForIssuerErrorMessage() {
            return MULTIPLE_INSTALLMENTS;
        }

        @Override
        public String getMissingInstallmentsForIssuerErrorMessage() {
            return MISSING_INSTALLMENTS;
        }

        @Override
        public String getMissingPayerCostsErrorMessage() {
            return MISSING_PAYER_COSTS;
        }

        @Override
        public String getMissingAmountErrorMessage() {
            return MISSING_AMOUNT;
        }

        @Override
        public String getMissingPublicKeyErrorMessage() {
            return MISSING_PUBLIC_KEY;
        }

        @Override
        public String getMissingSiteErrorMessage() {
            return MISSING_SITE;
        }

        @Override
        public void getInstallmentsAsync(String bin, Long issuerId, String paymentMethodId, BigDecimal amount,
            Integer differential,
            TaggedCallback<List<Installment>> taggedCallback) {
            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulResponse);
            }
        }

        @Override
        public void createESCTokenAsync(SavedESCCardToken escCardToken, TaggedCallback<Token> taggedCallback) {
            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulTokenResponse);
            }
        }

        @Override
        public String findESCSaved(String cardId) {
            if (escEnabled) {
                if (cardId.equals("12345")) {
                    return "12345";
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        public void deleteESC(String cardId) {
            deleteRequested = true;
            cardIdDeleted = cardId;
        }
    }

    private class MockedView implements CardVaultView {

        private MercadoPagoError errorShown;
        private boolean issuerFlowStarted;
        private boolean installmentsFlowStarted;
        private boolean securityCodeFlowStarted;
        private boolean guessingFlowStarted;
        private boolean recoverableTokenFlowStarted;
        private boolean finishedWithResult;
        private boolean cardVaultCanceled;
        private boolean errorState;
        private boolean animateSlide;
        private boolean animateNoAnimation;

        @Override
        public void askForInstallments() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForInstallmentsFromIssuers() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForInstallmentsFromNewCard() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForCardInformation() {
            guessingFlowStarted = true;
        }

        @Override
        public void askForSecurityCodeFromTokenRecovery() {
            recoverableTokenFlowStarted = true;
            securityCodeFlowStarted = true;
        }

        @Override
        public void startIssuersActivity() {
            issuerFlowStarted = true;
        }

        @Override
        public void showApiExceptionError(ApiException exception, String requestOrigin) {
            //Do something
        }

        @Override
        public void showProgressLayout() {
            //Do something
        }

        @Override
        public void showError(MercadoPagoError mercadoPagoError, String requestOrigin) {
            errorShown = mercadoPagoError;
            errorState = true;
        }

        @Override
        public void finishWithResult() {
            finishedWithResult = true;
        }

        @Override
        public void cancelCardVault() {
            cardVaultCanceled = true;
        }

        @Override
        public void animateTransitionSlideInSlideOut() {
            animateSlide = true;
        }

        @Override
        public void transitionWithNoAnimation() {
            animateNoAnimation = true;
        }

        @Override
        public void startSecurityCodeActivity(String reason) {
            securityCodeFlowStarted = true;
        }
    }
}
