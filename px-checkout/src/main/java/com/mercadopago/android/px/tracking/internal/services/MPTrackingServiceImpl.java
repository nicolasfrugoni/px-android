package com.mercadopago.android.px.tracking.internal.services;

import android.content.Context;
import android.util.Log;

import com.mercadopago.android.px.core.Settings;
import com.mercadopago.android.px.services.util.HttpClientUtil;
import com.mercadopago.android.px.tracking.internal.model.EventTrackIntent;
import com.mercadopago.android.px.tracking.internal.model.PaymentIntent;
import com.mercadopago.android.px.tracking.internal.model.TrackingIntent;
import com.mercadopago.android.px.tracking.internal.utils.JsonConverter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MPTrackingServiceImpl implements MPTrackingService {

    private static final String BASE_URL = "https://api.mercadopago.com/";

    private Retrofit getRetrofit(Context context) {
        return new Retrofit.Builder()
                .client(HttpClientUtil.getClient(context, 20, 20, 20))
                .addConverterFactory(GsonConverterFactory.create(JsonConverter.getInstance().getGson()))
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public void trackToken(TrackingIntent trackingIntent, Context context) {

        Retrofit retrofit = getRetrofit(context);
        TrackingService service = retrofit.create(TrackingService.class);

        Call<Void> call = service.trackToken(Settings.servicesVersion, trackingIntent);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 400) {
                    Log.e("Failure", "Error 400, parameter invalid");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Failure", "Service failure");
            }
        });
    }

    @Override
    public void trackPaymentId(PaymentIntent paymentIntent, Context context) {

        Retrofit retrofit = getRetrofit(context);
        TrackingService service = retrofit.create(TrackingService.class);

        Call<Void> call = service.trackPaymentId(Settings.servicesVersion, paymentIntent);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 400) {
                    Log.e("Failure", "Error 400, parameter invalid");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Failure", "Service failure");
            }
        });
    }

    @Override
    public void trackEvents(String publicKey, EventTrackIntent eventTrackIntent, Context context) {
        Retrofit retrofit = getRetrofit(context);
        TrackingService service = retrofit.create(TrackingService.class);

        Call<Void> call =
                service.trackEvents(Settings.eventsTrackingVersion, Settings.servicesVersion, publicKey, eventTrackIntent);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 400) {
                    Log.e("Failure", "Error 400, parameter invalid");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Failure", "Service failure");
            }
        });
    }

    @Override
    public void trackEvents(String publicKey, EventTrackIntent eventTrackIntent, Context context,
                            Callback<Void> callback) {
        Retrofit retrofit = getRetrofit(context);
        TrackingService service = retrofit.create(TrackingService.class);
        Call<Void> call =
                service.trackEvents(Settings.eventsTrackingVersion, Settings.servicesVersion, publicKey, eventTrackIntent);
        call.enqueue(callback);
    }
}