package com.mercadopago.android.px.internal.di;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.datasource.CardAssociationService;
import com.mercadopago.android.px.internal.datasource.CardPaymentMethodService;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESCImpl;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.internal.services.CardService;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.internal.util.RetrofitUtil;

public final class CardAssociationSession extends ApplicationModule {

    /**
     * This singleton instance is safe because session will work with
     * application context. Application context it's never leaking.
     */
    @SuppressLint("StaticFieldLeak") private static CardAssociationSession instance;

    private CardAssociationSession(@NonNull final Context context) {
        super(context.getApplicationContext());
    }

    public static CardAssociationSession getCardAssociationSession(final Context context) {
        if (instance == null) {
            instance = new CardAssociationSession(context);
        }
        return instance;
    }

    @NonNull
    public CardAssociationService getCardAssociationService() {
        return new CardAssociationService(getRetrofitClient().create(CardService.class));
    }

    @NonNull
    public CardPaymentMethodRepository getCardPaymentMethodRepository() {
        return new CardPaymentMethodService(getRetrofitClient().create(
            com.mercadopago.android.px.internal.services.PaymentService.class));
    }

    @NonNull
    public MercadoPagoESC getMercadoPagoESC() {
        return new MercadoPagoESCImpl(getContext(), true);
    }

    public GatewayService getGatewayService(){
        return RetrofitUtil.getRetrofitClient(getContext()).create(GatewayService.class);
    }
}
