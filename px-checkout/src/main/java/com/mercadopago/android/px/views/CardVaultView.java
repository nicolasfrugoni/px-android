package com.mercadopago.android.px.views;

import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.mvp.MvpView;
import com.mercadopago.android.px.services.exceptions.ApiException;

/**
 * Created by vaserber on 10/12/16.
 */

public interface CardVaultView extends MvpView {

    void finishWithResult();

    void showApiExceptionError(ApiException exception, String requestOrigin);

    void showError(MercadoPagoError mercadoPagoError, String requestOrigin);

    void askForInstallments();

    void startIssuersActivity();

    void startSecurityCodeActivity(String reason);

    void showProgressLayout();

    void askForCardInformation();

    void askForSecurityCodeFromTokenRecovery();

    void askForInstallmentsFromIssuers();

    void askForInstallmentsFromNewCard();

    void cancelCardVault();

    void animateTransitionSlideInSlideOut();

    void transitionWithNoAnimation();
}
