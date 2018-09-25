package com.mercadopago.android.px.internal.navigation;

import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.Payer;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;


public class DefaultPayerInformationDriver {

    private Payer payer;

    public DefaultPayerInformationDriver(final Payer payer) {
        this.payer = payer;
    }

    public void drive(@Nonnull final PayerInformationDriverCallback callback) {
        if (payer != null && isPayerInfoValid(payer)) {
            callback.driveToReviewConfirm();
        } else {
            callback.driveToNewPayerData();
        }
    }

    private boolean isPayerInfoValid(final Payer payer) {
        final Identification identification = payer.getIdentification();

        return !(identification == null
            || (StringUtils.isEmpty(identification.getNumber())
            || StringUtils.isEmpty(identification.getType()))
            || StringUtils.isEmpty(payer.getFirstName())
            || StringUtils.isEmpty(payer.getLastName()));
    }

    public interface PayerInformationDriverCallback {
        void driveToNewPayerData();

        void driveToReviewConfirm();
    }
}
