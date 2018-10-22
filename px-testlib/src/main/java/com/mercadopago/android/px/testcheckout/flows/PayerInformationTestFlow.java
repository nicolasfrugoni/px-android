package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;

public class PayerInformationTestFlow extends TestFlow {

    public static final String PAYER_IDENTIFICATION_TYPE = "CPF";
    public static final String PAYER_IDENTIFICATION_NUMBER = "12312312312";
    public static final String PAYER_NAME = "Test First Name";
    public static final String PAYER_LASTNAME = "Test Last Name";

    public PayerInformationTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public PayerInformationTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout,
        @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runWithDefaultOpenPayerFlow(){
        startCheckout();
        return new PaymentMethodPage(null)
            .selectTicketWithDefaultPayer()
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runWithDefaultOpenPayerAndModifyFlow(){
        startCheckout();
        return new PaymentMethodPage(null)
            .selectTicketWithDefaultPayer()
            .pressModifyPayerInformation()
            .enterIdentificationTypeAndNumberAndPressNext(PAYER_IDENTIFICATION_TYPE, PAYER_IDENTIFICATION_NUMBER)
            .enterFirstNameAndPressNext(PAYER_NAME)
            .enterLastNameAndPressNext(PAYER_LASTNAME)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runWithoutOpenPayerFlow(){
        startCheckout();
        return new PaymentMethodPage(null)
            .selectTicketWithoutPayer()
            .enterIdentificationTypeAndNumberAndPressNext(PAYER_IDENTIFICATION_TYPE, PAYER_IDENTIFICATION_NUMBER)
            .enterFirstNameAndPressNext(PAYER_NAME)
            .enterLastNameAndPressNext(PAYER_LASTNAME)
            .pressConfirmButton();
    }

}
