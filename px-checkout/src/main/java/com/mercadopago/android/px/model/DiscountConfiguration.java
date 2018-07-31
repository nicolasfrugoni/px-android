package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;

public class DiscountConfiguration {

    private CampaignError campaignError;
    private Discount discount;
    private Campaign campaign;

    public DiscountConfiguration(@NonNull final CampaignError campaignError) {
        this.campaignError = campaignError;
    }

    public DiscountConfiguration(@NonNull final Discount discount, @NonNull final Campaign campaign) {
        this.discount = discount;
        this.campaign = campaign;
    }

    public CampaignError getCampaignError() {
        return campaignError;
    }

    public Discount getDiscount() {
        return discount;
    }

    public Campaign getCampaign() {
        return campaign;
    }
}
