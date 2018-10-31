package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.datasource.CardAssociationGatewayService;
import com.mercadopago.android.px.internal.datasource.CardAssociationService;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.CREATE_TOKEN;
import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_CARD_PAYMENT_METHODS;
import static com.mercadopago.android.px.internal.util.ApiUtil.RequestOrigin.GET_ISSUERS;

public class GuessingCardStoragePresenter extends GuessingCardPresenter {

    /* default */ final MercadoPagoESC mercadoPagoESC;
    /* default */ final String accessToken;
    private final CardPaymentMethodRepository cardPaymentMethodRepository;
    private final CardAssociationService cardAssociationService;
    private final CardAssociationGatewayService gatewayService;
    /* default */ @Nullable List<Issuer> cardIssuers;
    @Nullable
    private PaymentMethod currentPaymentMethod;

    public GuessingCardStoragePresenter(final String accessToken,
        final CardPaymentMethodRepository cardPaymentMethodRepository,
        final CardAssociationService cardAssociationService,
        final MercadoPagoESC mercadoPagoESC,
        final CardAssociationGatewayService gatewayService) {
        super();
        this.accessToken = accessToken;
        this.cardPaymentMethodRepository = cardPaymentMethodRepository;
        this.cardAssociationService = cardAssociationService;
        this.mercadoPagoESC = mercadoPagoESC;
        this.gatewayService = gatewayService;
    }

    @Override
    public void initialize() {
        getView().onValidStart();
        getView().hideBankDeals();
        initializeCardToken();
        getPaymentMethods();
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        if (currentPaymentMethod != null) {
            return currentPaymentMethod.getPaymentTypeId();
        }
        return null;
    }

    @Nullable
    @Override
    public PaymentMethod getPaymentMethod() {
        return currentPaymentMethod;
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        currentPaymentMethod = paymentMethod;
        if (paymentMethod == null) {
            clearCardSettings();
            // Reset card issuers
            cardIssuers = null;
        } else {
            // We just chosed a payment method, fetch issuers fot that PM
            fetchCardIssuers();
        }
    }

    @Override
    public void getIdentificationTypesAsync() {
        getResourcesProvider().getIdentificationTypesAsync(accessToken,
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    if (isViewAttached()) {
                        if (!identificationTypes.isEmpty()) {
                            resolveIdentificationTypes(identificationTypes);
                        } else {
                            getView().finishCardStorageFlowWithError(accessToken);
                        }
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().finishCardStorageFlowWithError(accessToken);
                    }
                }
            });
    }

    @Override
    public void getPaymentMethods() {
        getView().showProgress();
        cardPaymentMethodRepository.getCardPaymentMethods(accessToken).enqueue(
            new TaggedCallback<List<PaymentMethod>>(GET_CARD_PAYMENT_METHODS) {
                @Override
                public void onSuccess(final List<PaymentMethod> paymentMethods) {
                    if (isViewAttached()) {
                        getView().hideProgress();
                        if (paymentMethods != null && !paymentMethods.isEmpty()) {
                            mPaymentMethodGuessingController = new
                                PaymentMethodGuessingController(paymentMethods, null, null);
                            startGuessingForm();
                        } else {
                            getView().finishCardStorageFlowWithError(accessToken);
                        }
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().finishCardStorageFlowWithError(accessToken);
                    }
                }
            }
        );
    }

    @Override
    public void createToken() {
        gatewayService.createToken(accessToken, mCardToken)
            .enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
                @Override
                public void onSuccess(final Token token) {
                    if (token != null) {
                        resolveTokenRequest(token);
                    } else {
                        if (isViewAttached()) {
                            getView().finishCardStorageFlowWithError(accessToken);
                        }
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        if (isIdentificationNumberWrong(error)) {
                            showIdentificationNumberError();
                        } else {
                            getView().finishCardStorageFlowWithError(accessToken);
                        }
                    }
                }
            });
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        setToken(token);
        // We need to check for card issuers
        if (cardIssuers == null) {
            // We might still be able to create association without IssuerId,
            // otherwise the error will pop on the association.
            associateCardToUser(null);
        } else {
            if (cardIssuers.size() == 1) {
                // We only have 1 issuer, let's use it
                associateCardToUser(cardIssuers.get(0).getId());
            } else {
                // We need to prompt the user to select the issuer
                final CardInfo cardInfo = new CardInfo(token);
                if (isViewAttached()) {
                    getView().askForIssuer(cardInfo, cardIssuers, getPaymentMethod());
                }
            }
        }
    }

    void associateCardToUser(@Nullable final Long issuerId) {
        cardAssociationService
            .associateCardToUser(accessToken, getToken().getId(), getPaymentMethod().getId(),
                issuerId)
            .enqueue(
                new TaggedCallback<Card>(ApiUtil.RequestOrigin.ASSOCIATE_CARD) {
                    @Override
                    public void onSuccess(final Card card) {
                        if (card != null) {
                            saveCardEsc(card);
                        } else {
                            if (isViewAttached()) {
                                getView().finishCardStorageFlowWithError(accessToken);
                            }
                        }
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {
                        if (isViewAttached()) {
                            getView().finishCardStorageFlowWithError(accessToken);
                        }
                    }
                });
    }

    /* default */ void saveCardEsc(final Card card) {
        final SavedESCCardToken savedESCCardToken =
            SavedESCCardToken.createWithSecurityCode(card.getId(), getCardToken().getSecurityCode());
        gatewayService.createEscToken(accessToken, savedESCCardToken)
            .enqueue(new TaggedCallback<Token>(CREATE_TOKEN) {
                @Override
                public void onSuccess(final Token token) {
                    if (token != null) {
                        mercadoPagoESC.saveESC(token.getCardId(), token.getEsc());
                    }

                    if (isViewAttached()) {
                        getView().finishCardStorageFlowWithSuccess();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().finishCardStorageFlowWithSuccess();
                    }
                }
            });
    }

    /* default */ void fetchCardIssuers() {
        cardAssociationService.getCardIssuers(accessToken, getPaymentMethod().getId(), getSavedBin()).enqueue(
            new TaggedCallback<List<Issuer>>(GET_ISSUERS) {
                @Override
                public void onSuccess(final List<Issuer> issuers) {
                    cardIssuers = issuers;
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    // If issuers fail, we might still be able to associate card, otherwise the error screen will be
                    // displayed later when the association fails.
                }
            });
    }

    @Nullable
    @Override
    public List<BankDeal> getBankDealsList() {
        return null;
    }

    @Override
    public void onIssuerSelected(final Long issuerId) {
        associateCardToUser(issuerId);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        if (getPaymentMethod() != null) {
            super.onSaveInstanceState(outState, cardSideState, lowResActive);
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
            super.onRestoreInstanceState(savedInstanceState);
        }
    }
}
