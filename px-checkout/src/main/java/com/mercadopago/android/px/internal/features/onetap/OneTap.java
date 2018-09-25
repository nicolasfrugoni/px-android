package com.mercadopago.android.px.internal.features.onetap;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.viewmodel.OneTapModel;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public interface OneTap {

    interface View extends MvpView {

        void cancel();

        void changePaymentMethod();

        void showCardFlow(@NonNull final OneTapModel oneTapModel, @NonNull final Card card);

        void showDetailModal(@NonNull final OneTapModel model);

        void trackConfirm(final OneTapModel model);

        void trackCancel();

        void trackModal(final OneTapModel model);

        void showPaymentProcessor();

        void showLoadingFor(@NonNull final ExplodeDecorator params,
            @NonNull final ExplodingFragment.ExplodingAnimationListener explodingAnimationListener);

        void cancelLoading();

        void startLoadingButton(int yButtonPosition, final int buttonHeight, final int paymentTimeout);

        //TODO shared with Checkout activity

        void showErrorScreen(@NonNull final MercadoPagoError error);

        void showPaymentResult(@NonNull final IPayment paymentResult);

        void onRecoverPaymentEscInvalid(final PaymentRecovery recovery);

        void startPayment();

        void hideToolbar();

        void hideConfirmButton();

        void updateViews(OneTapModel model);

        void showErrorSnackBar(@NonNull final MercadoPagoError error);
    }

    interface Actions extends PaymentServiceHandler {

        void confirmPayment(int yButtonPosition, final int buttonHeight);

        void onTokenResolved();

        void changePaymentMethod();

        void onAmountShowMore();

        void onViewResumed(final OneTapModel model);

        void onViewPaused();
    }
}