package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class OpenPayer extends Payer {

    OpenPayer(@NonNull final Builder builder) {
        setIdentification(builder.identification);
        setEmail(builder.email);
        setFirstName(builder.firstName);
        setLastName(builder.lastName);
    }

    public static class Builder {

        //region mandatory params
        /* default */ @NonNull String email;
        //endregion mandatory params

        /* default */ @Nullable Identification identification;
        /* default */ @Nullable String firstName;
        /* default */ @Nullable String lastName;

        /**
         * Builder for OpenPayer
         *
         * @param email payer email
         */
        public Builder(@NonNull final String email) {
            this.email = email;
        }

        public Builder setIdentification(@Nullable final Identification identification) {
            this.identification = identification;
            return this;
        }

        public Builder setFirstName(@Nullable final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(@Nullable final String lastName) {
            this.lastName = lastName;
            return this;
        }

        /**
         * It creates the OpenPayer
         *
         * @return OpenPayer
         */
        public OpenPayer build() {
            return new OpenPayer(this);
        }
    }
}