package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.CopyAction;
import com.mercadopago.android.px.internal.view.LinkAction;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.model.InstructionAction;
import com.mercadopago.android.px.model.Interaction;

import static android.view.View.GONE;
import static com.mercadopago.android.px.model.InstructionAction.Tags.COPY;
import static com.mercadopago.android.px.model.InstructionAction.Tags.LINK;

public class InstructionInteractionComponentRenderer extends Renderer<InstructionInteractionComponent> {
    @Override
    protected View render(@NonNull final InstructionInteractionComponent component, @NonNull final Context context,
        @Nullable final ViewGroup parent) {
        final View view = inflate(R.layout.px_payment_result_instruction_interaction, parent);
        final MPTextView title = view.findViewById(R.id.mpsdkInteractionTitle);
        final MPTextView content = view.findViewById(R.id.mpsdkInteractionContent);
        final MeliButton button = view.findViewById(R.id.mpsdkInteractionButton);

        final Interaction interaction = component.props.interaction;
        setText(title, interaction.getTitle());
        setText(content, interaction.getContent());

        final InstructionAction action = interaction.getAction();
        if (action != null) {
            setText(button, action.getLabel());

            final String tag = action.getTag();
            if (TextUtil.isNotEmpty(tag)) {
                if (tag.equals(COPY)) {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            component.getDispatcher().dispatch(new CopyAction(interaction.getContent()));
                        }
                    });
                } else if (tag.equals(LINK)) {
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            component.getDispatcher().dispatch(new LinkAction(interaction.getAction().getUrl()));
                        }
                    });
                }
            }
        } else {
            button.setVisibility(GONE);
        }
        return view;
    }
}
