package com.mercadopago.android.px.internal.tracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import com.mercadopago.android.px.BuildConfig;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.model.ActionEvent;
import com.mercadopago.android.px.model.CardPaymentMetadata;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.StrategyMode;
import com.mercadopago.android.px.tracking.internal.utils.TrackingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.mercadopago.android.px.tracking.internal.StrategyMode.NOOP_STRATEGY;

public final class Tracker {

    private Tracker() {
    }

    private static void addProperties(final ScreenViewEvent.Builder builder,
        @Nullable final Iterable<Pair<String, String>> propertyList) {
        if (propertyList != null) {
            for (final Pair<String, String> property : propertyList) {
                builder.addProperty(property.first, property.second);
            }
        }
    }

    private static MPTrackingContext getTrackerContext(@NonNull final Context context) {
        return getTrackerContext(context, NOOP_STRATEGY);
    }

    private static MPTrackingContext getTrackerContext(@NonNull final Context context,
        @NonNull final String trackingStrategy) {
        final String publicKey =
            Session.getSession(context).getConfigurationModule().getPaymentSettings().getPublicKey();
        final MPTrackingContext.Builder builder =
            new MPTrackingContext.Builder(context.getApplicationContext(), publicKey)
            .setVersion(BuildConfig.VERSION_NAME);

        builder.setTrackingStrategy(trackingStrategy);

        return builder.build();
    }

    public static void trackScreen(final String screenId,
        final String screenName,
        final Context context) {

        trackScreen(screenId, screenName, context, new ArrayList<Pair<String, String>>());
    }

    public static void trackScreen(final String screenId,
        final String screenName,
        final Context context,
        @Nullable final Iterable<Pair<String, String>> properties) {

        trackScreen(screenId, screenName, context, properties, NOOP_STRATEGY);
    }

    public static void trackScreen(final String screenId,
        final String screenName,
        final Context context,
        @Nullable final Iterable<Pair<String, String>> properties,
        final String trackingStrategy) {

        final MPTrackingContext mpTrackingContext = getTrackerContext(context, trackingStrategy);

        final ScreenViewEvent.Builder builder = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(screenId)
            .setScreenName(screenName);

        addProperties(builder, properties);
        final ScreenViewEvent event = builder.build();
        mpTrackingContext.trackEvent(event);
    }

    public static void trackReviewAndConfirmScreen(final Context context,
        final PaymentModel paymentModel) {

        final Collection<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new Pair<>(TrackingUtil.PROPERTY_SHIPPING_INFO, TrackingUtil.HAS_SHIPPING_DEFAULT_VALUE));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, paymentModel.getPaymentType()));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, paymentModel.paymentMethodId));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_ISSUER_ID, String.valueOf(paymentModel.issuerId)));

        trackScreen(TrackingUtil.SCREEN_ID_REVIEW_AND_CONFIRM,
            TrackingUtil.SCREEN_NAME_REVIEW_AND_CONFIRM,
            context, properties);
    }

    public static void trackOneTapScreen(@NonNull final Context context) {
        final Session session = Session.getSession(context);
        final BigDecimal amountToPay = session.getAmountRepository().getAmountToPay();
        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);

        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                final OneTapMetadata oneTapMetadata = paymentMethodSearch.getOneTapMetadata();

                final ScreenViewEvent.Builder builder = new ScreenViewEvent.Builder()
                    .setFlowId(FlowHandler.getInstance().getFlowId())
                    .setScreenId(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .setScreenName(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .addProperty(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, oneTapMetadata.getPaymentTypeId())
                    .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, oneTapMetadata.getPaymentMethodId())
                    .addProperty(TrackingUtil.PROPERTY_PURCHASE_AMOUNT, amountToPay.toString());

                if (oneTapMetadata.getCard() != null) {
                    builder.addProperty(TrackingUtil.PROPERTY_INSTALLMENTS,
                        oneTapMetadata.getCard().getAutoSelectedInstallment().getInstallments().toString());
                    builder.addProperty(TrackingUtil.PROPERTY_CARD_ID, oneTapMetadata.getCard().getId());
                }

                mpTrackingContext.trackEvent(builder.build());
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("Something wrong OneTapTracking");
            }
        });


    }

    public static void trackOneTapConfirm(@NonNull final Context context) {
        final MPTrackingContext mpTrackingContext = getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);
        final Session session = Session.getSession(context);
        final BigDecimal amountToPay = session.getAmountRepository().getAmountToPay();

        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {

                final OneTapMetadata oneTapMetadata = paymentMethodSearch.getOneTapMetadata();

                final ActionEvent.Builder builder = new ActionEvent.Builder()
                    .setFlowId(FlowHandler.getInstance().getFlowId())
                    .setAction(TrackingUtil.ACTION_CHECKOUT_CONFIRMED)
                    .setScreenId(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .setScreenName(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .addProperty(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, oneTapMetadata.getPaymentTypeId())
                    .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, oneTapMetadata.getPaymentMethodId())
                    .addProperty(TrackingUtil.PROPERTY_PURCHASE_AMOUNT, amountToPay.toString());

                final CardPaymentMetadata card = oneTapMetadata.getCard();

                if (card != null) {
                    builder.addProperty(TrackingUtil.PROPERTY_INSTALLMENTS,
                        card.getAutoSelectedInstallment().getInstallments().toString());
                    builder.addProperty(TrackingUtil.PROPERTY_CARD_ID, card.getId());
                }

                mpTrackingContext.trackEvent(builder.build());
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("Something wrong OneTapTracking");
            }
        });
    }

    public static void trackOneTapCancel(@NonNull final Context context) {
        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);

        final ActionEvent.Builder builder = new ActionEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setAction(TrackingUtil.ACTION_CANCEL_ONE_TAP)
            .setScreenId(TrackingUtil.SCREEN_ID_ONE_TAP)
            .setScreenName(TrackingUtil.SCREEN_ID_ONE_TAP);

        mpTrackingContext.trackEvent(builder.build());
    }

    public static void trackOneTapSummaryDetail(@NonNull final Context context) {
        final Session session = Session.getSession(context);
        final MPTrackingContext mpTrackingContext = getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);

        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {

                final OneTapMetadata oneTapMetadata = paymentMethodSearch.getOneTapMetadata();
                final CardPaymentMetadata card = oneTapMetadata.getCard();
                final DiscountRepository discountRepository = session.getDiscountRepository();
                final boolean validDiscount = discountRepository.hasValidDiscount();

                final ActionEvent.Builder builder = new ActionEvent.Builder()
                    .setFlowId(FlowHandler.getInstance().getFlowId())
                    .setAction(TrackingUtil.ACTION_OPEN_SUMMARY_ONE_TAP)
                    .setScreenId(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .setScreenName(TrackingUtil.SCREEN_ID_ONE_TAP)
                    .addProperty(TrackingUtil.PROPERTY_HAS_DISCOUNT, String.valueOf(validDiscount));

                if (card != null) {
                    builder.addProperty(TrackingUtil.PROPERTY_INSTALLMENTS,
                        card.getAutoSelectedInstallment().getInstallments().toString());
                }
                mpTrackingContext.trackEvent(builder.build());
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("Something wrong OneTapTracking - trackOneTapSummaryDetail");
            }
        });
    }

    public static void trackDiscountTermsAndConditions(@NonNull final Context context) {

        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);

        final ActionEvent.Builder builder = new ActionEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_DISCOUNT_TERMS)
            .setScreenName(TrackingUtil.SCREEN_ID_DISCOUNT_TERMS);

        mpTrackingContext.trackEvent(builder.build());
    }

    public static void trackCheckoutConfirm(final Context context,
        final PaymentModel paymentModel,
        final SummaryModel summaryModel) {

        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context, StrategyMode.REALTIME_STRATEGY);

        final ActionEvent.Builder builder = new ActionEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setAction(TrackingUtil.ACTION_CHECKOUT_CONFIRMED)
            .setScreenId(TrackingUtil.SCREEN_ID_REVIEW_AND_CONFIRM)
            .setScreenName(TrackingUtil.SCREEN_NAME_REVIEW_AND_CONFIRM)
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, paymentModel.getPaymentType())
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, paymentModel.paymentMethodId)
            .addProperty(TrackingUtil.PROPERTY_PURCHASE_AMOUNT, summaryModel.getAmountToPay().toString());

        if (PaymentTypes.isCreditCardPaymentType(summaryModel.getPaymentTypeId())) {
            builder.addProperty(TrackingUtil.PROPERTY_INSTALLMENTS, String.valueOf(summaryModel.getInstallments()));
        }

        //If is saved card
        final String cardId = paymentModel.getCardId();
        if (cardId != null) {
            builder.addProperty(TrackingUtil.PROPERTY_CARD_ID, cardId);
        }

        final ActionEvent actionEvent = builder.build();
        mpTrackingContext.trackEvent(actionEvent);
    }

    public static void trackPaymentVaultScreen(final Context context,
        final PaymentMethodSearch paymentMethodSearch,
        final Set<String> escCardIds) {

        final Collection<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new Pair<>(TrackingUtil.PROPERTY_OPTIONS,
            getFormattedPaymentMethodsForTracking(context, paymentMethodSearch, escCardIds)));

        trackScreen(TrackingUtil.SCREEN_ID_PAYMENT_VAULT,
            TrackingUtil.SCREEN_NAME_PAYMENT_VAULT,
            context, properties);
    }

    public static void trackPaymentVaultChildrenScreen(@NonNull final Context context,
        @NonNull final PaymentMethodSearchItem selectedItem) {

        final String selectedItemId = selectedItem.getId();

        if (TrackingUtil.GROUP_TICKET.equals(selectedItemId)) {
            trackScreen(TrackingUtil.SCREEN_ID_PAYMENT_VAULT_TICKET, TrackingUtil.SCREEN_NAME_PAYMENT_VAULT_TICKET,
                context, null);
        } else if (TrackingUtil.GROUP_BANK_TRANSFER.equals(selectedItemId)) {
            trackScreen(TrackingUtil.SCREEN_ID_PAYMENT_VAULT_BANK_TRANSFER,
                TrackingUtil.SCREEN_NAME_PAYMENT_VAULT_BANK_TRANSFER, context, null);
        } else if (TrackingUtil.GROUP_CARDS.equals(selectedItemId)) {
            trackScreen(TrackingUtil.SCREEN_ID_PAYMENT_VAULT_CARDS, TrackingUtil.SCREEN_NAME_PAYMENT_VAULT_CARDS,
                context, null);
        } else {
            trackScreen(TrackingUtil.SCREEN_ID_PAYMENT_VAULT, TrackingUtil.SCREEN_NAME_PAYMENT_VAULT, context, null);
        }
    }

    private static String getFormattedPaymentMethodsForTracking(final Context context,
        @NonNull final PaymentMethodSearch paymentMethodSearch, final Set<String> escCardIds) {
        final Collection<PaymentMethodPlugin> paymentMethodPluginList =
            Session.getSession(context).getPluginRepository().getEnabledPlugins();
        return TrackingFormatter
            .getFormattedPaymentMethodsForTracking(paymentMethodSearch, paymentMethodPluginList, escCardIds);
    }

    public static void trackError(@NonNull final Context applicationContext, @NonNull final MercadoPagoError error) {
        ScreenViewEvent.Builder builder = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_ERROR)
            .setScreenName(TrackingUtil.SCREEN_NAME_ERROR);

        builder = error.getErrorEvent(builder);

        final ScreenViewEvent event = builder.build();
        getTrackerContext(applicationContext).trackEvent(event);
    }
}
