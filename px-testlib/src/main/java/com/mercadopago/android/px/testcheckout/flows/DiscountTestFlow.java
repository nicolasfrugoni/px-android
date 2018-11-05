package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.OneTapPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;

public class DiscountTestFlow extends TestFlow {

    private static String DISCOUNT_CODE = "prueba";
    private static String PAYMENT_METHOD_NAME = "Pago Fácil";

    public DiscountTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public DiscountTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout, @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithMerchantDiscountApplied(@NonNull final Card card,
        final int installmentsOption) {
        return runCreditCardPaymentFlowWithMerchantDiscountApplied(card, installmentsOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithMerchantDiscountApplied(@NonNull final Card card,
        final int installmentsOption,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).pressOnDiscountDetail()
            .pressCloseToPaymentMethod()
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .pressOnDiscountDetail()
            .pressCloseToInstallments()
            .selectInstallments(installmentsOption)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithDiscountApplied(@NonNull final Card card,
        final int installmentsOption) {
        return runCreditCardPaymentFlowWithDiscountApplied(card, installmentsOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithDiscountApplied(@NonNull final Card card,
        final int installmentsOption,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).pressOnDiscountDetail()
            .pressCloseToPaymentMethod()
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .pressOnDiscountDetail()
            .pressCloseToInstallments()
            .selectInstallments(installmentsOption)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCashPaymentFlowWithDiscountApplied() {
        return runCashPaymentFlowWithDiscountApplied(null);
    }

    @NonNull
    public CongratsPage runCashPaymentFlowWithDiscountApplied(final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).pressOnDiscountDetail()
            .pressCloseToPaymentMethod()
            .selectCash()
            .selectMethod(PAYMENT_METHOD_NAME)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithoutPaymentProcessorWithMerchantDiscountApplied(
        @NonNull final Card card, final int installmentsOption) {
        return runCreditCardPaymentFlowWithoutPaymentProcessorWithMerchantDiscountApplied(card, installmentsOption,
            null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithoutPaymentProcessorWithMerchantDiscountApplied(
        @NonNull final Card card, final int installmentsOption,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator)
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .selectInstallments(installmentsOption)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithCodeDiscount(@NonNull final Card card,
        final int installmentsOption) {
        return runCreditCardPaymentFlowWithCodeDiscount(card, installmentsOption, null);
    }

    @NonNull
    public CongratsPage runCreditCardPaymentFlowWithCodeDiscount(@NonNull final Card card, final int installmentsOption,
        final CheckoutValidator validator) {
        startCheckout();

        return new PaymentMethodPage(validator).pressOnDiscountCodeInput()
            .focusInputCode()
            .enterDiscountCode(DISCOUNT_CODE)
            .pressContinueToPaymentMethod()
            .selectCard()
            .selectCreditCard()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToInstallments(card.cardHolderIdentityNumber())
            .pressOnDiscountDetail()
            .pressCloseToInstallments()
            .selectInstallments(installmentsOption)
            .pressConfirmButton();
    }

    public CongratsPage runCreditCardWithOneTapWithoutESCPaymentFlowWithMerchantDiscountApplied(final Card card) {
        return runCreditCardWithOneTapWithoutESCPaymentFlowWithMerchantDiscountApplied(card, null);
    }

    public CongratsPage runCreditCardWithOneTapWithoutESCPaymentFlowWithMerchantDiscountApplied(final Card card,
        final CheckoutValidator validator) {
        startCheckout();

        return new OneTapPage(validator)
            .pressOnDiscountDetail()
            .pressCloseToOneTap()
            .pressConfirmButton()
            .enterSecurityCodeToCongratsPage(card.escNumber());
    }
}
