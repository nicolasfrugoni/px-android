package com.mercadopago.android.px.internal.features.onetap.components;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.onetap.OneTap;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.model.CardPaymentMetadata;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/* default */ class PaymentMethod extends CompactComponent<PaymentMethod.Props, OneTap.Actions> {

    static class Props {
        /* default */ @NonNull final String paymentMethodType;
        /* default */ @NonNull final String paymentMethodId;
        /* default */ @Nullable final CardPaymentMetadata cardPaymentMetadata;
        /* default */ @Nonnull final PaymentMethodSearch paymentMethodSearch;
        /* default */ @NonNull final String currencyId;
        /* default */ @Nullable final Discount discount;

        /* default */ Props(@NonNull final String paymentMethodType,
            @NonNull final String paymentMethodId,
            @Nullable final CardPaymentMetadata cardPaymentMetadata,
            @Nonnull final PaymentMethodSearch paymentMethodSearch,
            @NonNull final String currencyId,
            @Nullable final Discount discount) {

            this.paymentMethodType = paymentMethodType;
            this.paymentMethodId = paymentMethodId;
            this.cardPaymentMetadata = cardPaymentMetadata;
            this.paymentMethodSearch = paymentMethodSearch;
            this.currencyId = currencyId;
            this.discount = discount;
        }

        /* default */
        @NonNull
        String getPaymentMethodType() {
            return paymentMethodType;
        }

        /* default */
        @NonNull
        public String getPaymentMethodId() {
            return paymentMethodId;
        }

        /* default */
        @Nullable
        public CardPaymentMetadata getCard() {
            return cardPaymentMetadata;
        }

        /* default */
        @Nonnull
        public PaymentMethodSearch getPaymentMethodSearch() {
            return paymentMethodSearch;
        }

        /* default */
        static Props createFrom(@NonNull final String currencyId,
            @Nullable final Discount discount,
            final PaymentMethodSearch paymentMethodSearch) {
            final OneTapMetadata oneTapMetadata = paymentMethodSearch.getOneTapMetadata();
            return new Props(oneTapMetadata.getPaymentTypeId(), oneTapMetadata.getPaymentMethodId(),
                oneTapMetadata.getCard(), paymentMethodSearch,
                currencyId, discount);
        }
    }

    /* default */ PaymentMethod(final Props props, final OneTap.Actions callBack) {
        super(props, callBack);
    }

    @VisibleForTesting()
    CompactComponent resolveComponent() {
        if (PaymentTypes.isCardPaymentType(props.getPaymentMethodType())) {
            return new MethodCard(MethodCard.Props.createFrom(props));
        } else if (PaymentTypes.isPlugin(props.getPaymentMethodType())) {
            return new MethodPlugin(MethodPlugin.Props.createFrom(props));
        } else {
            //TODO should not happen or should have another way to resolve it.
            throw new IllegalStateException("shouldn't happen - one tap payment type not supported");
        }
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup main = (ViewGroup) inflate(parent, R.layout.px_payment_method_compact_container);
        final View view = resolveComponent().render(main);
        main.addView(view);
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                getActions().changePaymentMethod();
            }
        });
        return main;
    }
}
