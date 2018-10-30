package com.mercadopago.android.px.internal.features.review_and_confirm.components.payer_information;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.MPCardMaskUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Payer;
import javax.annotation.Nonnull;

public class PayerInformationComponent extends CompactComponent<Payer, PayerInformationComponent.Actions> {
    @Nonnull private final Context context;

    public interface Actions {
        void onModifyPayerInformationClicked();
    }

    public PayerInformationComponent(@NonNull final Payer props, @Nonnull final Context context, @Nonnull final Actions actions) {
        super(props, actions);
        this.context = context;
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup payerInfoView = (ViewGroup) inflate(parent, R.layout.px_payer_information);
        final MPTextView docTypeAndNumber = payerInfoView.findViewById(R.id.payer_doc_type_and_number);
        final MPTextView fullName = payerInfoView.findViewById(R.id.payer_full_name);
        final ImageView icon = payerInfoView.findViewById(R.id.icon);

        ViewUtils.loadOrGone(getIdentificationTypeAndNumber(), docTypeAndNumber);
        ViewUtils.loadOrGone(getFirstAndLastName(), fullName);
        drawIconFromRes(icon, R.drawable.px_payer_information);
        drawModifyButton(payerInfoView);

        return payerInfoView;
    }

    private void drawModifyButton(@NonNull final ViewGroup payerInfoView) {
        final MeliButton buttonLink = payerInfoView.findViewById(R.id.payer_information_modify_button);
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
        final IdentificationType identificationType = new IdentificationType();
        identificationType.setId(props.getIdentification().getType());

        final String identificationNumber = props.getIdentification().getNumber();
        try {
            identificationType.setMaxLength(identificationNumber.length());
        } catch (NumberFormatException e) {
            identificationType.setMaxLength(0);
        }
        final String maskedNumber =
            MPCardMaskUtil.buildIdentificationNumberWithMask(identificationNumber, identificationType);
        return context.getString(res, identificationType.getId(), maskedNumber);
    }

    private void drawIconFromRes(@Nonnull final ImageView imageView, @DrawableRes final int resource) {
        imageView.setImageResource(resource);
    }
}