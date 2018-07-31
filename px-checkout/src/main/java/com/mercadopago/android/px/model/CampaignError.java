package com.mercadopago.android.px.model;

public class CampaignError {
    //TODO chequear firma con iOS y definir atributos según lo que recibamos de MKTools.
    private String campaignErrormessage;

    public CampaignError(String campaignErrormessage) {
        this.campaignErrormessage = campaignErrormessage;
    }

    public String getCampaignErrormessage() {
        return campaignErrormessage;
    }
}
