package com.mercadopago.android.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.CheckoutActivity;
import com.mercadopago.android.px.callbacks.CallbackHolder;
import com.mercadopago.android.px.hooks.CheckoutHooks;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.commission.ChargeRule;
import com.mercadopago.android.px.plugins.DataInitializationTask;
import com.mercadopago.android.px.plugins.PaymentMethodPlugin;
import com.mercadopago.android.px.plugins.PaymentProcessor;
import com.mercadopago.android.px.preferences.AdvancedConfiguration;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.FlowPreference;
import com.mercadopago.android.px.preferences.PaymentResultScreenPreference;
import com.mercadopago.android.px.review_and_confirm.models.ReviewAndConfirmPreferences;
import com.mercadopago.android.px.tracker.FlowHandler;
import com.mercadopago.android.px.uicontrollers.FontCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mercadopago.android.px.plugins.PaymentProcessor.PAYMENT_PROCESSOR_KEY;
import static com.mercadopago.android.px.util.TextUtils.isEmpty;

@SuppressWarnings("unused")
public class MercadoPagoCheckout implements Serializable {

    public static final int PAYMENT_RESULT_CODE = 7;
    public static final String EXTRA_PAYMENT_RESULT = "EXTRA_PAYMENT_RESULT";
    public static final String EXTRA_ERROR = "EXTRA_ERROR";

    @NonNull
    private final String publicKey;

    @Nullable
    private final CheckoutPreference checkoutPreference;

    @NonNull
    private final AdvancedConfiguration advancedConfiguration;

    @NonNull
    private final PaymentResultScreenPreference paymentResultScreenPreference;

    @Nullable
    private final String preferenceId;

    @Nullable
    private final Discount discount;

    @Nullable
    private final Campaign campaign;

    private final boolean binaryMode;

    @Nullable
    private final String privateKey;

    @NonNull
    private final ArrayList<ChargeRule> charges;

    /* default */ boolean prefetch = false;

    /* default */ MercadoPagoCheckout(final Builder builder) {
        publicKey = builder.publicKey;
        checkoutPreference = builder.checkoutPreference;
        advancedConfiguration = builder.advancedConfiguration;
        paymentResultScreenPreference = builder.paymentResultScreenPreference;
        binaryMode = builder.binaryMode;
        discount = builder.discount;
        campaign = builder.campaign;
        charges = builder.charges;
        preferenceId = builder.preferenceId;
        privateKey = builder.privateKey;
        configureCheckoutStore(builder);
        FlowHandler.getInstance().generateFlowId();
        CallbackHolder.getInstance().clean();
    }

    /**
     * Starts checkout experience.
     * When the flows ends it returns a {@link PaymentResult} object that
     * will be returned on {@link Activity#onActivityResult(int, int, Intent)} if success or
     * {@link com.mercadopago.android.px.model.exceptions.MercadoPagoError}
     * <p>
     * will return on {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @param context context needed to start checkout.
     */
    public void startPayment(@NonNull final Context context, final int resCode) {
        startIntent(context, CheckoutActivity.getIntent(context, this), resCode);
    }

    private void configureCheckoutStore(final Builder builder) {
        final CheckoutStore store = CheckoutStore.getInstance();
        store.reset();
        store.setReviewAndConfirmPreferences(builder.reviewAndConfirmPreferences);
        store.setPaymentResultScreenPreference(paymentResultScreenPreference);
        store.setPaymentMethodPluginList(builder.paymentMethodPluginList);
        store.setPaymentPlugins(builder.paymentPlugins);
        store.setCheckoutHooks(builder.checkoutHooks);
        store.setDataInitializationTask(builder.dataInitializationTask);
    }

    private void startIntent(@NonNull final Context context, @NonNull final Intent checkoutIntent, final int resCode) {
        if (!prefetch) {
            Session.getSession(context).init(this);
        }

        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(checkoutIntent, resCode);
        } else {
            context.startActivity(checkoutIntent);
        }
    }

    @NonNull
    public PaymentResultScreenPreference getPaymentResultScreenPreference() {
        return paymentResultScreenPreference;
    }

    @NonNull
    public AdvancedConfiguration getAdvancedConfiguration() {
        return advancedConfiguration;
    }

    public boolean isBinaryMode() {
        return binaryMode;
    }

    @Nullable
    public Discount getDiscount() {
        return discount;
    }

    @Nullable
    public Campaign getCampaign() {
        return campaign;
    }

    @NonNull
    public List<ChargeRule> getCharges() {
        return charges;
    }

    @NonNull
    public String getMerchantPublicKey() {
        return publicKey;
    }

    @Nullable
    public String getPreferenceId() {
        return preferenceId;
    }

    @Nullable
    public CheckoutPreference getCheckoutPreference() {
        return checkoutPreference;
    }

    @NonNull
    public String getPrivateKey() {
        return isEmpty(privateKey) ? "" : privateKey;
    }

    @SuppressWarnings("unused")
    public static final class Builder {

        final String publicKey;

        final String preferenceId;

        final CheckoutPreference checkoutPreference;

        @NonNull final ArrayList<ChargeRule> charges = new ArrayList<>();

        final Map<String, PaymentProcessor> paymentPlugins = new HashMap<>();

        final List<PaymentMethodPlugin> paymentMethodPluginList = new ArrayList<>();

        Boolean binaryMode = false;

        @NonNull
        AdvancedConfiguration advancedConfiguration = new AdvancedConfiguration.Builder().build();

        @Nullable
        String privateKey;

        @NonNull
        PaymentResultScreenPreference paymentResultScreenPreference =
            new PaymentResultScreenPreference.Builder().build();

        @NonNull
        ReviewAndConfirmPreferences reviewAndConfirmPreferences = new ReviewAndConfirmPreferences.Builder().build();

        Discount discount;
        Campaign campaign;
        CheckoutHooks checkoutHooks;
        DataInitializationTask dataInitializationTask;
        String regularFontPath;
        String lightFontPath;
        String monoFontPath;

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         *
         * @param publicKey merchant public key.
         * @param checkoutPreference the preference that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final CheckoutPreference checkoutPreference) {
            preferenceId = null;
            this.publicKey = publicKey;
            this.checkoutPreference = checkoutPreference;
        }

        /**
         * Checkout builder allow you to create a {@link MercadoPagoCheckout}
         *
         * @param publicKey merchant public key.
         * @param preferenceId the preference id that represents the payment information.
         */
        public Builder(@NonNull final String publicKey, @NonNull final String preferenceId) {
            this.publicKey = publicKey;
            this.preferenceId = preferenceId;
            checkoutPreference = null;
        }

        /**
         * Set Mercado Pago discount that will be applied to total amount.
         * When you set a discount with its campaign, we do not check in discount service.
         * You have to set a payment processor for discount be applied.
         *
         * @param discount Mercado Pago discount.
         * @param campaign Discount campaign with discount data.
         */
        public Builder setDiscount(@NonNull final Discount discount, @NonNull final Campaign campaign) {
            this.discount = discount;
            this.campaign = campaign;
            return this;
        }

        /**
         * Private key provides save card capabilities and account money balance.
         *
         * @param privateKey the user private key
         * @return builder
         */
        public Builder setPrivateKey(@NonNull final String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        /**
         * Add extra charges that will apply to total amount.
         *
         * @param charge Extra charge that you could collect.
         */
        public Builder addChargeRule(@NonNull final ChargeRule charge) {
            charges.add(charge);
            return this;
        }

        /**
         * Add extra charges that will apply to total amount.
         *
         * @param charges the list of chargest that could apply.
         */
        public Builder addChargeRules(@NonNull final Collection<ChargeRule> charges) {
            this.charges.addAll(charges);
            return this;
        }

        public Builder setAdvancedConfiguration(@NonNull final AdvancedConfiguration advancedConfiguration) {
            this.advancedConfiguration = advancedConfiguration;
            return this;
        }

        public Builder setPaymentResultScreenPreference(
            @NonNull final PaymentResultScreenPreference paymentResultScreenPreference) {
            this.paymentResultScreenPreference = paymentResultScreenPreference;
            return this;
        }

        /**
         * If enableBinaryMode is called, processed payment can only be APPROVED or REJECTED.
         * <p>
         * Non compatible with PaymentProcessor.
         * <p>
         * Non compatible with off payments methods
         *
         * @return builder
         */
        public Builder enableBinaryMode() {
            binaryMode = true;
            return this;
        }

        /**
         * Enable to preset configurations to customize visualization on
         * the Review and Confirm Screen see {@link ReviewAndConfirmPreferences.Builder}
         *
         * @param reviewAndConfirmPreferences the custom preference configuration
         * @return builder to keep operating
         */
        public Builder setReviewAndConfirmPreferences(final ReviewAndConfirmPreferences reviewAndConfirmPreferences) {
            this.reviewAndConfirmPreferences = reviewAndConfirmPreferences;
            return this;
        }

        public Builder setCheckoutHooks(@NonNull final CheckoutHooks checkoutHooks) {
            this.checkoutHooks = checkoutHooks;
            return this;
        }

        public Builder addPaymentMethodPlugin(@NonNull final PaymentMethodPlugin paymentMethodPlugin,
            @NonNull final PaymentProcessor paymentProcessor) {
            paymentMethodPluginList.add(paymentMethodPlugin);
            paymentPlugins.put(paymentMethodPlugin.getId(), paymentProcessor);
            return this;
        }

        public Builder setPaymentProcessor(@NonNull final PaymentProcessor paymentProcessor) {
            paymentPlugins.put(PAYMENT_PROCESSOR_KEY, paymentProcessor);
            return this;
        }

        public Builder setDataInitializationTask(@NonNull final DataInitializationTask dataInitializationTask) {
            this.dataInitializationTask = dataInitializationTask;
            return this;
        }

        public MercadoPagoCheckout build() {
            return new MercadoPagoCheckout(this);
        }

        /**
         * //TODO add new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomLightFont(final String lightFontPath, final Context context) {
            this.lightFontPath = lightFontPath;
            if (lightFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_LIGHT_FONT, this.lightFontPath);
            }
            return this;
        }

        /**
         * //TODO add new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomRegularFont(final String regularFontPath, final Context context) {
            this.regularFontPath = regularFontPath;
            if (regularFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_REGULAR_FONT, this.regularFontPath);
            }
            return this;
        }

        /**
         * //TODO add new mechanism
         *
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        public Builder setCustomMonoFont(final String monoFontPath, final Context context) {
            this.monoFontPath = monoFontPath;
            if (monoFontPath != null) {
                setCustomFont(context, FontCache.CUSTOM_MONO_FONT, this.monoFontPath);
            }
            return this;
        }

        /**
         * @deprecated we will not support this mechanism anymore.
         */
        @Deprecated
        private void setCustomFont(final Context context, final String fontType, final String fontPath) {
            final Typeface typeFace;
            if (!FontCache.hasTypeface(fontType)) {
                typeFace = Typeface.createFromAsset(context.getAssets(), fontPath);
                FontCache.setTypeface(fontType, typeFace);
            }
        }

        /**
         * @deprecated new mechanism {@link #setAdvancedConfiguration}
         */
        @Deprecated
        public Builder setFlowPreference(@NonNull final FlowPreference flowPreference) {
            return this;
        }
    }
}