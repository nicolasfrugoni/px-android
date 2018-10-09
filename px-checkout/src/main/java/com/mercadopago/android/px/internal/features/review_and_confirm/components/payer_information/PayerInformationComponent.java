package com.mercadopago.android.px.internal.features.review_and_confirm.components.payer_information;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.ButtonLink;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Payer;
import javax.annotation.Nonnull;

public class PayerInformationComponent extends CompactComponent<Payer, PayerInformationComponent.Actions> {
    public interface Actions {
        void onModifyPayerInformationClicked();
    }

    public PayerInformationComponent(final Payer props, Actions actions) {
        super(props, actions);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup payerInfoView = (ViewGroup) inflate(parent, R.layout.px_payer_information);

        final TextView docTypeAndNumber = payerInfoView.findViewById(R.id.payer_doc_type_and_number);
        docTypeAndNumber.setText(props.getIdentification().getType() + " " + props.getIdentification().getNumber());

        final TextView fullName = payerInfoView.findViewById(R.id.payer_full_name);
        fullName.setText(props.getFirstName() + " " + props.getLastName());

        ImageView icon = payerInfoView.findViewById(R.id.icon);
        icon.setImageResource(ResourceUtil.getIconResource(icon.getContext(), props.getId()));

        ButtonLink buttonLink = new ButtonLink(new Button.Props("Modificar", null), new Button.Actions() {
            @Override
            public void onClick(final Action action) {
                if (getActions() != null) {
                    getActions().onModifyPayerInformationClicked();
                }
            }

            @Override
            public void onClick(final int yButtonPosition, final int buttonHeight) {
                //Do Nothing
            }
        });

        compose(payerInfoView, buttonLink.render(payerInfoView));

        return payerInfoView;
    }
}
