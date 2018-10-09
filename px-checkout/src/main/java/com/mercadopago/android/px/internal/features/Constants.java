package com.mercadopago.android.px.internal.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultActivity;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.preferences.PaymentPreference;
import java.util.List;

public final class Constants {

    public static final int RESULT_PAYMENT = 200;
    public static final int RESULT_ACTION = 201;
    public static final int RESULT_CUSTOM_EXIT = 202;
    public static final int RESULT_CANCELED_RYC = 203;

    public static final int RESULT_CHANGE_PAYMENT_METHOD = 300;
    public static final int RESULT_CANCEL_PAYMENT = 499;
    public static final int RESULT_FAIL_ESC = 500;
    public static final int RESULT_ERROR = 502;

    private Constants() {
    }

    public static final class Activities {

        public static final int PAYMENT_METHODS_REQUEST_CODE = 1;
        public static final int INSTALLMENTS_REQUEST_CODE = 2;
        public static final int ISSUERS_REQUEST_CODE = 3;
        public static final int CALL_FOR_AUTHORIZE_REQUEST_CODE = 7;
        public static final int PENDING_REQUEST_CODE = 8;
        public static final int REJECTION_REQUEST_CODE = 9;
        public static final int PAYMENT_VAULT_REQUEST_CODE = 10;
        public static final int BANK_DEALS_REQUEST_CODE = 11;
        public static final int GUESSING_CARD_FOR_PAYMENT_REQUEST_CODE = 13;
        public static final int INSTRUCTIONS_REQUEST_CODE = 14;

        public static final int CONGRATS_REQUEST_CODE = 16;
        public static final int PAYMENT_TYPES_REQUEST_CODE = 17;
        public static final int SECURITY_CODE_REQUEST_CODE = 18;
        public static final int REVIEW_PAYMENT_METHODS_REQUEST_CODE = 21;

        public static final int HOOK_1 = 50;
        public static final int HOOK_1_PLUGIN = 52;
        public static final int HOOK_2 = 60;
        public static final int HOOK_3 = 70;

        public static final int PLUGIN_PAYMENT_METHOD_REQUEST_CODE = 100;

        private Activities() {
        }

        public static class PaymentVaultActivityBuilder {

            private Activity activity;

            public PaymentVaultActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }

                startPaymentVaultActivity();
            }

            private void startPaymentVaultActivity() {
                final Intent intent = new Intent(activity, PaymentVaultActivity.class);
                activity.startActivityForResult(intent, PAYMENT_VAULT_REQUEST_CODE);
            }
        }

        public static class CardVaultActivityBuilder {

            private Card card;
            private PaymentRecovery paymentRecovery;
            private boolean automaticSelection;

            public CardVaultActivityBuilder setCard(final Card card) {
                this.card = card;
                return this;
            }

            public CardVaultActivityBuilder setPaymentRecovery(final PaymentRecovery paymentRecovery) {
                this.paymentRecovery = paymentRecovery;
                return this;
            }

            public CardVaultActivityBuilder setAutomaticSelection(final Boolean automaticSelection) {
                this.automaticSelection = automaticSelection;
                return this;
            }

            private Intent getIntent(final Context context) {
                final Intent intent = new Intent(context, CardVaultActivity.class);
                intent.putExtra("paymentRecovery", JsonUtil.getInstance().toJson(paymentRecovery));
                intent.putExtra("card", JsonUtil.getInstance().toJson(card));
                intent.putExtra("automaticSelection", automaticSelection);
                return intent;
            }

            public void startActivity(@NonNull final Activity context, final int reqCode) {
                context.startActivityForResult(getIntent(context), reqCode);
            }

            public void startActivity(final Fragment oneTapFragment, final int reqCode) {
                oneTapFragment.startActivityForResult(getIntent(oneTapFragment.getActivity()), reqCode);
            }
        }

        public static class PaymentMethodsActivityBuilder {

            private Activity activity;
            private PaymentPreference paymentPreference;

            public PaymentMethodsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentMethodsActivityBuilder setPaymentPreference(final PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startPaymentMethodsActivity();
            }

            private void startPaymentMethodsActivity() {
                final Intent paymentMethodsIntent = new Intent(activity, PaymentMethodsActivity.class);
                paymentMethodsIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

                activity.startActivityForResult(paymentMethodsIntent, PAYMENT_METHODS_REQUEST_CODE);
            }
        }

        public static class IssuersActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private List<Issuer> issuers;

            public IssuersActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public IssuersActivityBuilder setCardInfo(final CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public IssuersActivityBuilder setIssuers(final List<Issuer> issuers) {
                this.issuers = issuers;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startIssuersActivity();
            }

            private void startIssuersActivity() {
                final Intent intent = new Intent(activity, IssuersActivity.class);
                intent.putExtra("issuers", JsonUtil.getInstance().toJson(issuers));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                activity.startActivityForResult(intent, ISSUERS_REQUEST_CODE);
            }
        }

        public static class InstallmentsActivityBuilder {
            private Activity activity;
            private CardInfo cardInfo;
            private List<PayerCost> payerCosts;
            private PaymentPreference paymentPreference;

            public InstallmentsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public InstallmentsActivityBuilder setCardInfo(final CardInfo cardInformation) {
                cardInfo = cardInformation;
                return this;
            }

            public InstallmentsActivityBuilder setPaymentPreference(final PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public InstallmentsActivityBuilder setPayerCosts(final List<PayerCost> payerCosts) {
                this.payerCosts = payerCosts;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startInstallmentsActivity();
            }

            private void startInstallmentsActivity() {
                final Intent intent = new Intent(activity, InstallmentsActivity.class);
                intent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCosts));
                intent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInfo));
                activity.startActivityForResult(intent, INSTALLMENTS_REQUEST_CODE);
            }
        }

        public static class SecurityCodeActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private PaymentMethod paymentMethod;
            private Card card;
            private Token token;
            private PaymentRecovery paymentRecovery;
            private String reason;

            public SecurityCodeActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public SecurityCodeActivityBuilder setCardInfo(final CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public SecurityCodeActivityBuilder setTrackingReason(final String reason) {
                this.reason = reason;
                return this;
            }

            public SecurityCodeActivityBuilder setPaymentRecovery(final PaymentRecovery paymentRecovery) {
                this.paymentRecovery = paymentRecovery;
                return this;
            }

            public SecurityCodeActivityBuilder setPaymentMethod(final PaymentMethod paymentMethod) {
                this.paymentMethod = paymentMethod;
                return this;
            }

            public SecurityCodeActivityBuilder setCard(final Card card) {
                this.card = card;
                return this;
            }

            public SecurityCodeActivityBuilder setToken(final Token token) {
                this.token = token;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (cardInformation == null) {
                    throw new IllegalStateException("card info is null");
                }
                if (paymentMethod == null) {
                    throw new IllegalStateException("payment method is null");
                }
                if (card != null && token != null && paymentRecovery == null) {
                    throw new IllegalStateException(
                        "can't start with card and token at the same time if it's not recoverable");
                }
                if (card == null && token == null) {
                    throw new IllegalStateException("card and token can't both be null");
                }

                startSecurityCodeActivity();
            }

            private void startSecurityCodeActivity() {
                final Intent intent = new Intent(activity, SecurityCodeActivity.class);
                intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
                intent.putExtra("token", JsonUtil.getInstance().toJson(token));
                intent.putExtra("card", JsonUtil.getInstance().toJson(card));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                intent.putExtra("paymentRecovery", JsonUtil.getInstance().toJson(paymentRecovery));
                intent.putExtra("reason", reason);
                activity.startActivityForResult(intent, SECURITY_CODE_REQUEST_CODE);
            }
        }

        public static class PaymentTypesActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private List<PaymentMethod> paymentMethods;
            private List<PaymentType> paymentTypes;

            public PaymentTypesActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentTypesActivityBuilder setCardInfo(final CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentMethods(@Nullable final List<PaymentMethod> paymentMethods) {
                this.paymentMethods = paymentMethods;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentTypes(final List<PaymentType> paymentTypes) {
                this.paymentTypes = paymentTypes;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (paymentMethods == null) {
                    throw new IllegalStateException("payment method list is null");
                }
                if (paymentTypes == null) {
                    throw new IllegalStateException("payment types list is null");
                }

                startSecurityCodeActivity();
            }

            private void startSecurityCodeActivity() {
                final Intent intent = new Intent(activity, PaymentTypesActivity.class);
                intent.putExtra("paymentMethods", JsonUtil.getInstance().toJson(paymentMethods));
                intent.putExtra("paymentTypes", JsonUtil.getInstance().toJson(paymentTypes));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                activity.startActivityForResult(intent, PAYMENT_TYPES_REQUEST_CODE);
            }
        }

        public static class BankDealsActivityBuilder {

            private Activity activity;
            private List<BankDeal> bankDeals;

            public BankDealsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public BankDealsActivityBuilder setBankDeals(final List<BankDeal> bankDeals) {
                this.bankDeals = bankDeals;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startBankDealsActivity();
            }

            private void startBankDealsActivity() {
                final Intent bankDealsIntent = new Intent(activity, BankDealsActivity.class);
                if (bankDeals != null) {
                    bankDealsIntent.putExtra("bankDeals", JsonUtil.getInstance().toJson(bankDeals));
                }
                activity.startActivityForResult(bankDealsIntent, BANK_DEALS_REQUEST_CODE);
            }
        }

        public static class ReviewPaymentMethodsActivityBuilder {

            private Activity activity;
            private List<PaymentMethod> paymentMethods;

            public ReviewPaymentMethodsActivityBuilder setActivity(@NonNull final Activity activity) {
                this.activity = activity;
                return this;
            }

            public ReviewPaymentMethodsActivityBuilder setPaymentMethods(final List<PaymentMethod> paymentMethods) {
                this.paymentMethods = paymentMethods;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (paymentMethods == null) {
                    throw new IllegalStateException("payment methods is null");
                }
                if (paymentMethods.isEmpty()) {
                    throw new IllegalStateException("payment methods is empty");
                }
                startReviewPaymentMethodsActivity();
            }

            private void startReviewPaymentMethodsActivity() {
                final Intent intent = new Intent(activity, ReviewPaymentMethodsActivity.class);
                intent.putExtra("paymentMethods", JsonUtil.getInstance().toJson(paymentMethods));
                activity.startActivityForResult(intent, REVIEW_PAYMENT_METHODS_REQUEST_CODE);
            }
        }
    }
}
