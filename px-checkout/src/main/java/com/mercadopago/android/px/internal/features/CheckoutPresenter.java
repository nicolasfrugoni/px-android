package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.datasource.CheckoutStore;
import com.mercadopago.android.px.internal.datasource.PluginInitializationAsync;
import com.mercadopago.android.px.internal.features.hooks.Hook;
import com.mercadopago.android.px.internal.features.hooks.HookHelper;
import com.mercadopago.android.px.internal.features.providers.CheckoutProvider;
import com.mercadopago.android.px.internal.navigation.DefaultPayerInformationDriver;
import com.mercadopago.android.px.internal.navigation.DefaultPaymentMethodDriver;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginInitTask;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Cause;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import java.util.List;
import java.util.Map;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYER_INFO;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;

public class CheckoutPresenter extends MvpPresenter<CheckoutView, CheckoutProvider> implements PaymentServiceHandler,
    PostPaymentAction.ActionController {

    @NonNull private final CheckoutStateModel state;
    @NonNull private final PluginRepository pluginRepository;
    @NonNull private final PaymentRepository paymentRepository;

    @NonNull
    private final GroupsRepository groupsRepository;
    @NonNull
    private final DiscountRepository discountRepository;
    @NonNull
    private final PaymentSettingRepository paymentSettingRepository;
    @NonNull
    private final AmountRepository amountRepository;
    @NonNull
    private final UserSelectionRepository userSelectionRepository;

    @NonNull
    private final InternalConfiguration internalConfiguration;

    @NonNull
    private BusinessModelMapper businessModelMapper;

    private transient FailureRecovery failureRecovery;

    private PluginInitTask pluginInitializationTask; //instance saved as attribute to cancel and avoid crash

    public CheckoutPresenter(@NonNull final CheckoutStateModel persistentData,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final GroupsRepository groupsRepository,
        @NonNull final PluginRepository pluginRepository,
        @NonNull final PaymentRepository paymentRepository,
        @NonNull final InternalConfiguration internalConfiguration,
        @NonNull final BusinessModelMapper businessModelMapper) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.amountRepository = amountRepository;
        this.userSelectionRepository = userSelectionRepository;
        this.discountRepository = discountRepository;
        this.groupsRepository = groupsRepository;
        this.pluginRepository = pluginRepository;
        this.paymentRepository = paymentRepository;
        this.internalConfiguration = internalConfiguration;
        this.businessModelMapper = businessModelMapper;
        state = persistentData;
    }

    @NonNull
    public CheckoutStateModel getState() {
        return state;
    }

    public void initialize() {
        getView().showProgress();
        configurePreference();
    }

    @Override
    public void attachView(final CheckoutView view) {
        super.attachView(view);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    private void configurePreference() {
        if (paymentSettingRepository.getCheckoutPreference() != null) {
            startCheckoutForPreference();
        } else {
            retrieveCheckoutPreference(paymentSettingRepository.getCheckoutPreferenceId());
        }
    }

    private void startCheckoutForPreference() {
        try {
            getCheckoutPreference().validate();
            getView().initializeMPTracker();
            getView().trackScreen();
            startCheckout();
        } catch (CheckoutPreferenceException e) {
            final String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, false));
        }
    }

    private void startCheckout() {
        getResourcesProvider().fetchFonts();
        initializePluginsData();
    }

    private void initializePluginsData() {
        pluginInitializationTask = pluginRepository.getInitTask(false);
        pluginInitializationTask.init(getDataInitializationCallback());
    }

    @NonNull
    private PluginInitializationAsync.DataInitializationCallbacks getDataInitializationCallback() {
        return new PluginInitializationAsync.DataInitializationCallbacks() {
            @Override
            public void onDataInitialized() {
                pluginRepository.initialized();
                finishInitializingPluginsData();
            }

            @Override
            public void onFailure(@NonNull final Exception e) {
                pluginRepository.initialized();
                finishInitializingPluginsData();
            }
        };
    }

    private void finishInitializingPluginsData() {
        discountRepository.configureDiscountAutomatically(amountRepository.getAmountToPay())
            .enqueue(new Callback<Boolean>() {
                @Override
                public void success(final Boolean automatic) {
                    retrievePaymentMethodSearch();
                }

                @Override
                public void failure(final ApiException apiException) {
                    retrievePaymentMethodSearch();
                }
            });
    }

    public void retrievePaymentMethodSearch() {
        if (isViewAttached()) {
            groupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
                @Override
                public void success(final PaymentMethodSearch paymentMethodSearch) {
                    if (isViewAttached()) {
                        startFlow(paymentMethodSearch);
                    }
                }

                @Override
                public void failure(final ApiException apiException) {
                    if (isViewAttached()) {
                        getView()
                            .showError(new MercadoPagoError(apiException, ApiUtil.RequestOrigin.GET_PAYMENT_METHODS));
                    }
                }
            });
        }
    }

    /* default */ void startFlow(final PaymentMethodSearch paymentMethodSearch) {

        new DefaultPaymentMethodDriver(paymentMethodSearch,
            paymentSettingRepository.getCheckoutPreference().getPaymentPreference())
            .drive(new DefaultPaymentMethodDriver.PaymentMethodDriverCallback() {
                @Override
                public void driveToCardVault(@NonNull final Card card) {
                    userSelectionRepository.select(card);
                    getView().showSavedCardFlow(card);
                }

                @Override
                public void driveToNewCardFlow(final String defaultPaymentTypeId) {
                    userSelectionRepository.select(defaultPaymentTypeId);
                    getView().showNewCardFlow();
                }

                @Override
                public void doNothing() {
                    noDefaultPaymentMethods(paymentMethodSearch);
                }
            });
    }

    /* default */ void noDefaultPaymentMethods(final PaymentMethodSearch paymentMethodSearch) {
        saveIsOneTap(paymentMethodSearch);
        savePaymentMethodQuantity(paymentMethodSearch);

        if (state.isOneTap) {
            getView().hideProgress();
            getView().showOneTap();
        } else {
            getView().showPaymentMethodSelection();
        }
    }

    private void showReviewAndConfirm() {
        state.editPaymentMethodFromReviewAndConfirm = false;
        getView().showReviewAndConfirm(isUniquePaymentMethod());
    }

    public boolean isESCEnabled() {
        return paymentSettingRepository.getAdvancedConfiguration().isEscEnabled();
    }

    private void retrieveCheckoutPreference(final String checkoutPreferenceId) {
        getResourcesProvider().getCheckoutPreference(checkoutPreferenceId,
            new TaggedCallback<CheckoutPreference>(ApiUtil.RequestOrigin.GET_PREFERENCE) {

                @Override
                public void onSuccess(final CheckoutPreference checkoutPreference) {
                    paymentSettingRepository.configure(checkoutPreference);
                    if (isViewAttached()) {
                        startCheckoutForPreference();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                retrieveCheckoutPreference(checkoutPreferenceId);
                            }
                        });
                    }
                }
            });
    }

    public void onErrorCancel(@Nullable final MercadoPagoError mercadoPagoError) {
        if (isIdentificationInvalidInPayment(mercadoPagoError)) {
            getView().showPaymentMethodSelection();
        } else {
            cancelCheckout();
        }
    }

    private boolean isIdentificationInvalidInPayment(final MercadoPagoError mercadoPagoError) {
        boolean identificationInvalid = false;
        if (mercadoPagoError != null && mercadoPagoError.isApiException()) {
            List<Cause> causeList = mercadoPagoError.getApiException().getCause();
            if (causeList != null && !causeList.isEmpty()) {
                Cause cause = causeList.get(0);
                if (cause.getCode().equals(ApiException.ErrorCodes.INVALID_IDENTIFICATION_NUMBER)) {
                    identificationInvalid = true;
                }
            }
        }
        return identificationInvalid;
    }

    public void onPaymentMethodSelectionResponse() {
        onPaymentMethodSelected();
    }

    private void onPaymentMethodSelected() {
        if (!showHook2(paymentRepository.getPaymentData())) {
            hook2Continue();
        }
    }

    private void resolvePaymentFailure(final MercadoPagoError mercadoPagoError) {
        if (mercadoPagoError != null && mercadoPagoError.isPaymentProcessing()) {
            final PaymentResult paymentResult =
                new PaymentResult.Builder()
                    .setPaymentData(paymentRepository.getPaymentData())
                    .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
                    .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
                    .build();
            getView().showPaymentResult(paymentResult);
        } else if (mercadoPagoError != null && mercadoPagoError.isInternalServerError()) {
            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    getView().startPayment();
                }
            });
            getView().showError(mercadoPagoError);
        } else {
            // Strange that mercadoPagoError can be nullable here, but it was like this
            getView().showError(mercadoPagoError);
        }
    }

    public void onPaymentMethodSelectionError(final MercadoPagoError mercadoPagoError) {
        if (!state.editPaymentMethodFromReviewAndConfirm) {
            getView().cancelCheckout(mercadoPagoError);
        } else {
            state.editPaymentMethodFromReviewAndConfirm = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onPaymentMethodSelectionCancel() {
        if (!state.editPaymentMethodFromReviewAndConfirm) {
            cancelCheckout();
        } else {
            state.editPaymentMethodFromReviewAndConfirm = false;
            getView().backToReviewAndConfirm();
        }
    }

    public void onReviewAndConfirmCancel() {
        if (isUniquePaymentMethod()) {
            getView().cancelCheckout();
        } else {
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
            //Back button in R&C
            getView().transitionOut();
        }
    }

    public void onReviewAndConfirmError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onPaymentResultResponse() {
        finishCheckout();
    }

    public void onCardFlowResponse() {
        handleStartPayment();
    }

    private void handleStartPayment() {
        if (isRecoverableTokenProcess()) {
            getView().startPayment();
        } else {
            onPaymentMethodSelected();
        }
    }

    public void onTerminalError(@NonNull final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onCardFlowCancel() {
        groupsRepository.getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                new DefaultPaymentMethodDriver(paymentMethodSearch,
                    paymentSettingRepository.getCheckoutPreference().getPaymentPreference()).drive(
                    new DefaultPaymentMethodDriver.PaymentMethodDriverCallback() {
                        @Override
                        public void driveToCardVault(@NonNull final Card card) {
                            cancelCheckout();
                        }

                        @Override
                        public void driveToNewCardFlow(final String defaultPaymentTypeId) {
                            cancelCheckout();
                        }

                        @Override
                        public void doNothing() {
                            state.paymentMethodEdited = true;
                            getView().showPaymentMethodSelection();
                        }
                    });
            }

            @Override
            public void failure(final ApiException apiException) {
                state.paymentMethodEdited = true;
                getView().showPaymentMethodSelection();
            }
        });
    }

    public void onCustomReviewAndConfirmResponse(final Integer customResultCode) {
        getView().cancelCheckout(customResultCode, state.paymentMethodEdited);
    }

    private void savePaymentMethodQuantity(final PaymentMethodSearch paymentMethodSearch) {
        final int pluginCount = pluginRepository.getPaymentMethodPluginCount();
        int groupCount = 0;
        int customCount = 0;

        if (paymentMethodSearch != null && paymentMethodSearch.hasSearchItems()) {
            groupCount = paymentMethodSearch.getGroups().size();
            if (pluginCount == 0 && groupCount == 1 && paymentMethodSearch.getGroups().get(0).isGroup()) {
                state.isUniquePaymentMethod = false;
            }
        }

        if (paymentMethodSearch != null && paymentMethodSearch.hasCustomSearchItems()) {
            customCount = paymentMethodSearch.getCustomSearchItems().size();
        }

        state.isUniquePaymentMethod = groupCount + customCount + pluginCount == 1;
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        } else {
            IllegalStateException e = new IllegalStateException("Failure recovery not defined");
            getView().showError(new MercadoPagoError(getResourcesProvider().getCheckoutExceptionMessage(e), false));
        }
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    private boolean isRecoverableTokenProcess() {
        return paymentRepository.hasPayment() && paymentRepository.createPaymentRecovery().isTokenRecoverable();
    }

    public CheckoutPreference getCheckoutPreference() {
        return paymentSettingRepository.getCheckoutPreference();
    }

    public Campaign getCampaign() {
        return discountRepository.getCampaign();
    }

    //### Hooks #####################

    private boolean showHook2(final PaymentData paymentData) {
        return showHook2(paymentData, Constants.Activities.HOOK_2);
    }

    private boolean showHook2(final PaymentData paymentData, final int requestCode) {
        final Map<String, Object> data = CheckoutStore.getInstance().getData();
        final Hook hook = HookHelper.activateAfterPaymentMethodConfig(
            CheckoutStore.getInstance().getCheckoutHooks(), paymentData, data);
        if (hook != null && getView() != null) {
            getView().showHook(hook, requestCode);
            return true;
        }
        return false;
    }

    private boolean showHook3(final PaymentData paymentData) {
        return showHook3(paymentData, Constants.Activities.HOOK_3);
    }

    private boolean showHook3(final PaymentData paymentData, final int requestCode) {
        final Map<String, Object> data = CheckoutStore.getInstance().getData();
        final Hook hook = HookHelper.activateBeforePayment(
            CheckoutStore.getInstance().getCheckoutHooks(), paymentData, data);
        if (hook != null && getView() != null) {
            getView().showHook(hook, requestCode);
            return true;
        }
        return false;
    }

    public void hook2Continue() {
        state.editPaymentMethodFromReviewAndConfirm = false;
        showReviewAndConfirm();
    }

    public void cancelInitialization() {
        if (pluginInitializationTask != null) {
            pluginInitializationTask.cancel();
        }
    }

    private void finishCheckout() {
        //TODO improve this
        if (paymentRepository.hasPayment() && paymentRepository.getPayment() instanceof Payment) {
            getView().finishWithPaymentResult((Payment) paymentRepository.getPayment());
        } else {
            getView().finishWithPaymentResult();
        }
    }

    public void onCustomPaymentResultResponse(final Integer customResultCode) {
        //TODO improve this
        if (paymentRepository.hasPayment() && paymentRepository.getPayment() instanceof Payment) {
            getView().finishWithPaymentResult(customResultCode, (Payment) paymentRepository.getPayment());
        } else {
            getView().finishWithPaymentResult(customResultCode);
        }
    }

    private void recoverPayment() {
        try {
            getView().startPaymentRecoveryFlow(paymentRepository.createPaymentRecovery());
        } catch (final Exception e) {
            final String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, e.getMessage(), false));
        }
    }

    /**
     * Send intention to close checkout
     * if the checkout has oneTap data then it should not close.
     */
    public void cancelCheckout() {
        //TODO improve this
        if (state.isOneTap) {
            getView().hideProgress();
        } else {
            getView().cancelCheckout();
        }
    }

    private void saveIsOneTap(final PaymentMethodSearch paymentMethodSearch) {
        state.isOneTap = paymentMethodSearch.hasOneTapMetadata();
    }

    /**
     * Close checkout with resCode
     */
    public void exitWithCode(final int resCode) {
        if (resCode == RESULT_CHANGE_PAYER_INFO) {
            getView().collectPayerInformation();
        } else {
            getView().exitCheckout(resCode);
        }
    }

    public void onChangePaymentMethodFromReviewAndConfirm() {
        //TODO remove when navigation is corrected and works with stack.
        onChangePaymentMethod(true);
    }

    public void onChangePaymentMethod() {
        onChangePaymentMethod(false);
    }

    private void onChangePaymentMethod(final boolean fromReviewAndConfirm) {
        if (fromReviewAndConfirm) {
            //Button "change payment method" in R&C
            getView().transitionOut();
        }

        if (internalConfiguration.shouldExitOnPaymentMethodChange()) {
            getView().finishWithPaymentResult(RESULT_CHANGE_PAYMENT_METHOD);
        } else {
            //TODO remove when navigation is corrected and works with stack.
            state.editPaymentMethodFromReviewAndConfirm = fromReviewAndConfirm;
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
        }
    }

    public boolean isUniquePaymentMethod() {
        return state.isUniquePaymentMethod;
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        getView().showSavedCardFlow(card);
    }

    @Override
    public void onVisualPayment() {
        getView().showPaymentProcessor();
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        getView().hideProgress();
        getView().showPaymentResult(paymentRepository.createPaymentResult(payment));
    }

    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        getView().hideProgress();
        getView().showPaymentResult(paymentRepository.createPaymentResult(genericPayment));
    }

    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        getView().hideProgress();
        getView().showBusinessResult(businessModelMapper.map(businessPayment));
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        getView().hideProgress();
        recoverCreatePayment(error);
    }

    private void recoverCreatePayment(final MercadoPagoError error) {
        setFailureRecovery(new FailureRecovery() {
            @Override
            public void recover() {
                getView().startPayment();
            }
        });
        resolvePaymentFailure(error);
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().startPaymentRecoveryFlow(recovery);
    }

    @Override
    public void recoverFromReviewAndConfirm(@NonNull final PostPaymentAction postPaymentAction) {
        getView().showReviewAndConfirmAndRecoverPayment(isUniquePaymentMethod(), postPaymentAction);
    }

    @Override
    public void recoverFromOneTap() {
        recoverPayment();
    }

    @Override
    public void changePaymentMethod() {
        if (internalConfiguration.shouldExitOnPaymentMethodChange()) {
            final IPayment payment = paymentRepository.getPayment();
            if (payment instanceof Payment) {
                getView().finishWithPaymentResult(Constants.RESULT_CHANGE_PAYMENT_METHOD,
                    (Payment) payment);
            } else {
                getView().finishWithPaymentResult(Constants.RESULT_CHANGE_PAYMENT_METHOD);
            }
        } else {
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
        }
    }

    public void onChangePayerInfoFromReviewAndConfirm() {
        getView().collectPayerInformation();
    }

    public void onPayerInformationResponse() {
        handleStartPayment();
    }
}