package com.mercadopago.android.px.internal.features.onetap.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.onetap.OneTap;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.ButtonPrimary;
import com.mercadopago.android.px.internal.view.TermsAndConditionsComponent;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;

public class OneTapView extends LinearLayout {

    public OneTap.Actions actionCallback;
    private View amountContainer;
    private View paymentMethodContainer;
    private View termsContainer;
    private View confirmButton;

    public OneTapView(final Context context) {
        this(context, null);
    }

    public OneTapView(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OneTapView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OneTapView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOneTapModel(@NonNull final OneTap.Actions callBack) {
        actionCallback = callBack;
        final Session session = Session.getSession(getContext());
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();
        final DiscountRepository discountRepository = session.getDiscountRepository();
        final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
            configuration.getAdvancedConfiguration().getReviewAndConfirmConfiguration();

        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {

                addItems(reviewAndConfirmConfiguration, configuration);
                amountContainer = createAmountView(configuration, discountRepository, paymentMethodSearch);
                addView(amountContainer);
                addPaymentMethod(configuration, discountRepository, paymentMethodSearch);
                termsContainer = createTermsView(discountRepository);
                if (termsContainer != null) {
                    addView(termsContainer);
                }
                confirmButton = addConfirmButton(discountRepository);
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("groups missing rendering one tap");
            }
        });
    }

    private void addItems(@NonNull final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration,
        @NonNull final PaymentSettingRepository configuration) {

        final Integer collectorIcon = reviewAndConfirmConfiguration.getCollectorIcon();
        final String defaultMultipleTitle = getContext().getString(R.string.px_review_summary_products);
        final int icon = collectorIcon == null ? R.drawable.px_review_item_default : collectorIcon;
        final String itemsTitle = com.mercadopago.android.px.model.Item
            .getItemsTitle(configuration.getCheckoutPreference().getItems(), defaultMultipleTitle);

        final View view = new CollapsedItem(new CollapsedItem.Props(icon, itemsTitle)).render(this);
        addView(view);
    }

    public void update() {
        final Session session = Session.getSession(getContext());
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();
        final DiscountRepository discountRepository = session.getDiscountRepository();
        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                for (int i = 0; i < getChildCount(); i++) {
                    updateAmount(i, getChildAt(i), configuration, discountRepository, paymentMethodSearch);
                    updateTerms(i, getChildAt(i), discountRepository);
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("groups missing rendering one tap");
            }
        });
    }

    private void updateAmount(final int indexView, @NonNull final View view,
        @NonNull final PaymentSettingRepository configuration,
        @NonNull final DiscountRepository discountRepository,
        final PaymentMethodSearch paymentMethodSearch) {
        if (view.equals(amountContainer)) {
            removeViewAt(indexView);
            amountContainer = createAmountView(configuration, discountRepository, paymentMethodSearch);
            addView(amountContainer, indexView);
        }
    }

    private void updateTerms(final int indexView, @NonNull final View view,
        @NonNull final DiscountRepository discountRepository) {

        if (view.equals(paymentMethodContainer)) {
            //Remove old terms
            if (indexView + 1 < getChildCount() && getChildAt(indexView + 1).equals(termsContainer)) {
                removeViewAt(indexView + 1);
            }
            //Update terms
            recreateTerms(indexView + 1, discountRepository);
        }
    }

    private void recreateTerms(final int indexView, @NonNull final DiscountRepository discountRepository) {
        termsContainer = createTermsView(discountRepository);
        if (termsContainer != null) {
            addView(termsContainer, indexView);
            final View buttonView = getChildAt(indexView + 1);
            ViewUtils.setMarginTopInView(buttonView,
                getContext().getResources().getDimensionPixelSize(R.dimen.px_zero_height));
        }
    }

    private View createAmountView(@NonNull final PaymentSettingRepository configuration,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final PaymentMethodSearch paymentMethodSearch) {
        final Amount.Props props = Amount.Props
            .from(paymentMethodSearch.getOneTapMetadata().getCard(),
                configuration,
                discountRepository
            );
        return new Amount(props, actionCallback).render(this);
    }

    private void addPaymentMethod(@NonNull final PaymentSettingRepository configuration,
        @NonNull final DiscountRepository discountRepository,
        final PaymentMethodSearch paymentMethodSearch) {
        paymentMethodContainer =
            new PaymentMethod(
                PaymentMethod.Props.createFrom(configuration.getCheckoutPreference().getSite().getCurrencyId(),
                    discountRepository.getDiscount(),
                    paymentMethodSearch), actionCallback)
                .render(this);
        addView(paymentMethodContainer);
    }

    private View createTermsView(@NonNull final DiscountRepository discountRepository) {
        final Campaign campaign = discountRepository.getCampaign();
        if (campaign != null) {
            final TermsAndConditionsModel model = new TermsAndConditionsModel(campaign.getCampaignTermsUrl(),
                getContext().getString(R.string.px_discount_terms_and_conditions_message),
                getContext().getString(R.string.px_discount_terms_and_conditions_linked_message),
                LineSeparatorType.NONE);
            return new TermsAndConditionsComponent(model)
                .render(this);
        }
        return null;
    }

    private View addConfirmButton(@NonNull final DiscountRepository discountRepository) {
        final Discount discount = discountRepository.getDiscount();

        final String confirm = getContext().getString(R.string.px_confirm);
        final Button.Actions buttonActions = new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                if (actionCallback != null) {
                    actionCallback.confirmPayment();
                }
            }

        };

        final Button button = new ButtonPrimary(new Button.Props(confirm), buttonActions);
        final View view = button.render(this);
        final int resMargin = discount == null ? R.dimen.px_m_margin : R.dimen.px_zero_height;
        ViewUtils.setMarginTopInView(view, getContext().getResources().getDimensionPixelSize(resMargin));
        addView(view);
        return view;
    }

    public void hideConfirmButton() {
        confirmButton.setVisibility(INVISIBLE);
    }

    public void showButton() {
        if (confirmButton != null) {
            confirmButton.setVisibility(VISIBLE);
        }
    }
}
