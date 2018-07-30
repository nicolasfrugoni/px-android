package com.mercadopago.android.px.viewmodel;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.preferences.PaymentResultScreenPreference;
import java.io.Serializable;

public final class CheckoutStateModel implements Serializable {

    public final PaymentResultScreenPreference paymentResultScreenPreference;

    public final boolean isBinary;
    public Issuer selectedIssuer;
    public Token createdToken;
    public Card selectedCard;
    public Payment createdPayment;
    public Payer collectedPayer;
    public boolean paymentMethodEdited;
    public boolean editPaymentMethodFromReviewAndConfirm;
    public PaymentRecovery paymentRecovery;
    public String currentPaymentIdempotencyKey;
    public boolean isUniquePaymentMethod;
    public boolean isOneTap;

    public CheckoutStateModel(@NonNull final MercadoPagoCheckout config) {
        paymentResultScreenPreference = config.getPaymentResultScreenPreference();
        isBinary = config.isBinaryMode();
    }
}
