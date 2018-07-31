package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfiguration;
import com.mercadopago.android.px.mvp.ResourcesProvider;
import com.mercadopago.android.px.services.adapters.MPCall;

import java.math.BigDecimal;

public interface DiscountRepository extends ResourcesProvider {

    public void configureMerchantDiscountManually(@Nullable final DiscountConfiguration configuration);

    void configureDiscountManually(@Nullable final Discount discount, @Nullable final Campaign campaign);

    @NonNull
    MPCall<Boolean> configureDiscountAutomatically(final BigDecimal amountToPay);

    @NonNull
    MPCall<Discount> getCodeDiscount(@NonNull final BigDecimal amount, @NonNull final String inputCode);

    @Nullable
    Discount getDiscount();

    @Nullable
    String getDiscountCode();

    @Nullable
    Campaign getCampaign();

    @Nullable
    Campaign getCampaign(String discountId);

    @Nullable
    DiscountConfiguration getDiscountConfiguration();

    boolean hasCodeCampaign();

    boolean hasValidDiscount();

    void saveDiscountCode(@NonNull final String code);

    void reset();

    boolean isUsedUpDiscount();
}
