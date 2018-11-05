package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;

public class DebitCardTestFlow extends TestFlow {

    public DebitCardTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public DebitCardTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runDebitCardPaymentFlow(@NonNull final Card card, final CheckoutValidator validator) {
        PaymentMethodPage paymentMethodPage = new PaymentMethodPage(validator);

        startCheckout();

        return paymentMethodPage.selectCard()
            .selectDebitCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToReviewAndConfirm(card.cardHolderIdentityNumber())
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runDebitCardPaymentFlow(@NonNull final Card card) {
        return runDebitCardPaymentFlow(card, null);
    }

    @NonNull
    public CongratsPage runDebitCardFlowFromPaymentMethod(@NonNull final Card card,
        @NonNull final PaymentMethodPage paymentMethodPage, final CheckoutValidator validator) {
        return paymentMethodPage
            .selectCard()
            .selectDebitCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToReviewAndConfirm(card.cardHolderIdentityNumber())
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runDebitCardFlowFromPaymentMethod(@NonNull final Card card,
        @NonNull final PaymentMethodPage paymentMethodPage) {
        return runDebitCardFlowFromPaymentMethod(card, paymentMethodPage, null);
    }
}
