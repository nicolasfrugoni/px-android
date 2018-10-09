package com.mercadopago.android.px.utils;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.example.R;
import java.math.BigDecimal;

public final class PaymentUtils {

    private PaymentUtils() {
        //Do nothing
    }

    @NonNull
    public static BusinessPayment getBusinessPaymentApproved() {
        return new BusinessPayment.Builder(BusinessPayment.Decorator.REJECTED, Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA,
            R.drawable.px_icon_card, "Title")
            .setSecondaryButton(new ExitAction("Button Name", 23))
            .setPrimaryButton(new ExitAction("Cambiar Informacion",301))
            .build();
    }

    @NonNull
    public static GenericPayment getGenericPaymentRejected() {
        return new GenericPayment(123L, Payment.StatusCodes.STATUS_REJECTED,
            Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA);
    }

    @NonNull
    public static GenericPayment getGenericPaymentApprovedAccountMoney() {
        return new GenericPayment(123L, Payment.StatusCodes.STATUS_APPROVED,
            Payment.StatusDetail.STATUS_DETAIL_ACCREDITED);
    }

    private static PaymentData getPaymentDataWithAccountMoneyPlugin(final BigDecimal amount) {
        final PaymentData paymentData = new PaymentData();
        final PaymentMethod paymentMethod = new PaymentMethod("account_money", "Dinero en cuenta", "account_money");
        paymentData.setPaymentMethod(paymentMethod);
        paymentData.setTransactionAmount(amount);
        return paymentData;
    }
}
