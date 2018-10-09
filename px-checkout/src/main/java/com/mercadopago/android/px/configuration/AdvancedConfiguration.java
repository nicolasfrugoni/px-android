package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import java.io.Serializable;

/**
 * Advanced configuration provides you support for custom checkout functionality/configure special behaviour
 * when checkout is running.
 */
@SuppressWarnings("unused")
public class AdvancedConfiguration implements Serializable {

    /**
     * Instores usage / money in usage.
     * use case : not all bank deals apply right now to all preferences.
     */
    private final boolean bankDealsEnabled;
    private final boolean escEnabled;
    @NonNull private final PaymentResultScreenConfiguration paymentResultScreenConfiguration;
    @NonNull private final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration;
    @NonNull private final DynamicFragmentConfiguration dynamicFragmentConfiguration;
    @NonNull private final DynamicDialogConfiguration dynamicDialogConfiguration;

    /* default */ AdvancedConfiguration(final Builder builder) {
        bankDealsEnabled = builder.bankDealsEnabled;
        escEnabled = builder.escEnabled;
        paymentResultScreenConfiguration = builder.paymentResultScreenConfiguration;
        reviewAndConfirmConfiguration = builder.reviewAndConfirmConfiguration;
        dynamicFragmentConfiguration = builder.dynamicFragmentConfiguration;
        dynamicDialogConfiguration = builder.dynamicDialogConfiguration;
    }

    public boolean isBankDealsEnabled() {
        return bankDealsEnabled;
    }

    public boolean isEscEnabled() {
        return escEnabled;
    }

    @NonNull
    public DynamicFragmentConfiguration getDynamicFragmentConfiguration() {
        return dynamicFragmentConfiguration;
    }

    @NonNull
    public DynamicDialogConfiguration getDynamicDialogConfiguration() {
        return dynamicDialogConfiguration;
    }

    @NonNull
    public PaymentResultScreenConfiguration getPaymentResultScreenConfiguration() {
        return paymentResultScreenConfiguration;
    }

    @NonNull
    public ReviewAndConfirmConfiguration getReviewAndConfirmConfiguration() {
        return reviewAndConfirmConfiguration;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        /* default */ boolean bankDealsEnabled = true;
        /* default */ boolean escEnabled = false;
        /* default */ @NonNull PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            new PaymentResultScreenConfiguration.Builder().build();
        /* default */ @NonNull ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
            new ReviewAndConfirmConfiguration.Builder().build();
        /* default */ @NonNull DynamicFragmentConfiguration dynamicFragmentConfiguration =
            new DynamicFragmentConfiguration.Builder().build();
        /* default */ @NonNull DynamicDialogConfiguration dynamicDialogConfiguration =
            new DynamicDialogConfiguration.Builder().build();



        /**
         * Add the possibility to configure Bank's deals behaviour.
         * If set as true, then the checkout will try to retrieve bank deals.
         * If set as false, then the checkout will not try to retrieve bank deals.
         *
         * @param bankDealsEnabled bool that reflects it's behaviour
         * @return builder to keep operating
         */
        public Builder setBankDealsEnabled(final boolean bankDealsEnabled) {
            this.bankDealsEnabled = bankDealsEnabled;
            return this;
        }

        /**
         * Add the possibility to configure ESC behaviour.
         * If set as true, then saved cards will try to use ESC feature.
         * If set as false, then security code will be always asked.
         *
         * @param escEnabled bool that reflects it's behaviour
         * @return builder to keep operating
         */
        public Builder setEscEnabled(final boolean escEnabled) {
            this.escEnabled = escEnabled;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on the
         * 'Congrats' screen / 'PaymentResult' screen.
         * see {@link PaymentResultScreenConfiguration.Builder}
         *
         * @param paymentResultScreenConfiguration your custom preferences.
         * @return builder to keep operating
         */
        public Builder setPaymentResultScreenConfiguration(
            @NonNull final PaymentResultScreenConfiguration paymentResultScreenConfiguration) {
            this.paymentResultScreenConfiguration = paymentResultScreenConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on
         * the 'Review and Confirm screen' see {@link ReviewAndConfirmConfiguration.Builder}
         *
         * @param reviewAndConfirmConfiguration your custom preferences.
         * @return builder to keep operating
         */
        public Builder setReviewAndConfirmConfiguration(
            @NonNull final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration) {
            this.reviewAndConfirmConfiguration = reviewAndConfirmConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize dynamic visualization on
         * several screen locations see {@link DynamicFragmentConfiguration.Builder}
         *
         * @param dynamicFragmentConfiguration your custom configurations.
         * @return builder to keep operating
         */
        public Builder setDynamicFragmentConfiguration(
            @NonNull final DynamicFragmentConfiguration dynamicFragmentConfiguration) {
            this.dynamicFragmentConfiguration = dynamicFragmentConfiguration;
            return this;
        }

        /**
         * Enable to preset configurations to customize dynamic visualization on
         * several screen locations see {@link DynamicFragmentConfiguration.Builder}
         *
         * @param dynamicDialogConfiguration your custom configurations.
         * @return builder to keep operating
         */
        public Builder setDynamicDialogConfiguration(
            @NonNull final DynamicDialogConfiguration dynamicDialogConfiguration) {
            this.dynamicDialogConfiguration = dynamicDialogConfiguration;
            return this;
        }

        public AdvancedConfiguration build() {
            return new AdvancedConfiguration(this);
        }
    }
}
