package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardPaymentMetadata;
import com.mercadopago.android.px.model.OneTapMetadata;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;

public class CardMapper extends Mapper<PaymentMethodSearch, Card> {

    @Override
    public Card map(@NonNull final PaymentMethodSearch paymentMethodSearch) {
        final OneTapMetadata oneTapMetadata = paymentMethodSearch.getOneTapMetadata();
        final CardPaymentMetadata cardPaymentMetadata = oneTapMetadata.getCard();
        final PaymentMethod paymentMethod =
            paymentMethodSearch.getPaymentMethodById(oneTapMetadata.getPaymentMethodId());
        final Card card = paymentMethodSearch.getCardById(cardPaymentMetadata.getId());
        card.setSecurityCode(paymentMethod != null ? paymentMethod.getSecurityCode() : null);
        card.setPaymentMethod(paymentMethod);
        return card;
    }
}
