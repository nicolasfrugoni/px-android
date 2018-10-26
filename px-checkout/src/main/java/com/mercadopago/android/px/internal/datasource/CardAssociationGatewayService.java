package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Device;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;

public class CardAssociationGatewayService {
    @NonNull private final GatewayService gatewayService;
    @NonNull private final Device device;

    public CardAssociationGatewayService(
        @NonNull final GatewayService gatewayService, @NonNull final Device device) {
        this.gatewayService = gatewayService;
        this.device = device;
    }

    public MPCall<Token> createToken(@NonNull final String accessToken, @NonNull final CardToken cardToken) {
        return gatewayService.createToken(null, accessToken, cardToken);
    }

    public MPCall<Token> createEscToken(@NonNull final String accessToken, @NonNull final SavedESCCardToken cardToken) {
        cardToken.setDevice(device);
        return gatewayService.createToken(null, accessToken, cardToken);
    }
}
