package com.mercadopago.android.px.internal.navigation;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.Payer;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

public class DefaultPayerInformationDriver {
    @Nullable private final Payer payer;

    public DefaultPayerInformationDriver(@Nullable final Payer payer) {
        this.payer = payer;
    }

    public void drive(@Nonnull final PayerInformationDriverCallback callback) {
        if (isPayerInformationValid(payer)) {
            callback.driveToReviewConfirm();
        } else {
            callback.driveToNewPayerData();
        }
    }

    private boolean isPayerInformationValid(@Nullable final Payer payer) {
        return payer != null
            && !(!isIdentificationValid(payer.getIdentification())
            || StringUtils.isEmpty(payer.getFirstName())
            || StringUtils.isEmpty(payer.getLastName()));
    }

    private boolean isIdentificationValid(final Identification identification) {
        return identification != null
            && !StringUtils.isEmpty(identification.getNumber())
            && !StringUtils.isEmpty(identification.getType());
    }

    public interface PayerInformationDriverCallback {
        void driveToNewPayerData();

        void driveToReviewConfirm();
    }
}