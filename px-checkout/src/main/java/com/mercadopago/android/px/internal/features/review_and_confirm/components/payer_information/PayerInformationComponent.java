package com.mercadopago.android.px.internal.features.review_and_confirm.components.payer_information;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.paymentresult.components.LineSeparator;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.ButtonLink;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Payer;
import javax.annotation.Nonnull;

public class PayerInformationComponent extends CompactComponent<Payer, PayerInformationComponent.Actions> {
    @Nonnull private final Context context;

    public interface Actions {
        void onModifyPayerInformationClicked();
    }

    public PayerInformationComponent(@NonNull final Payer props, @Nonnull Context context, @Nonnull Actions actions) {
        super(props, actions);
        this.context = context;
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup payerInfoView = (ViewGroup) inflate(parent, R.layout.px_payer_information);
        final MPTextView docTypeAndNumber = payerInfoView.findViewById(R.id.payer_doc_type_and_number);
        final MPTextView fullName = payerInfoView.findViewById(R.id.payer_full_name);
        final ImageView icon = payerInfoView.findViewById(R.id.icon);

        setText(docTypeAndNumber, getIdentificationTypeAndNumber());
        setText(fullName, getFirstAndLastName());
        drawIconFromRes(icon, R.drawable.px_payer_information);
        drawModifyButton(payerInfoView);

        return payerInfoView;
    }

    private void drawModifyButton(@NonNull final ViewGroup payerInfoView) {
        MeliButton buttonLink = payerInfoView.findViewById(R.id.payer_information_modify_button);
        buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (getActions() != null) {
                    getActions().onModifyPayerInformationClicked();
                }
            }
        });
    }

    @NonNull
    private String getFirstAndLastName() {
        @StringRes
        final int res = R.string.px_payer_information_first_and_last_name;
        return context.getString(res, props.getFirstName(), props.getLastName());
    }

    @NonNull
    private String getIdentificationTypeAndNumber() {
        final int res = R.string.px_payer_information_identification_type_and_number;
        return context.getString(res, props.getIdentification().getType(), props.getIdentification().getNumber());
    }

    private void drawIconFromRes(@Nonnull final ImageView imageView, @DrawableRes final int resource) {
        imageView.setImageResource(resource);
    }

    private void setText(@NonNull final MPTextView textView, @NonNull String text) {
        if (TextUtil.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
    }
}