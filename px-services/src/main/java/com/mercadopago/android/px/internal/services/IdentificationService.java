package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.IdentificationType;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IdentificationService {

    @GET("/v1/identification_types")
    MPCall<List<IdentificationType>> getIdentificationTypes(@Query("public_key") String publicKey,
        @Query("access_token") String privateKey);
}
