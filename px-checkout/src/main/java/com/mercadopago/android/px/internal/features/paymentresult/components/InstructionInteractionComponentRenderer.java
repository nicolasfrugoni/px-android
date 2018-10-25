package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.CopyAction;
import com.mercadopago.android.px.internal.view.LinkAction;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.InstructionAction;
import com.mercadopago.android.px.model.Interaction;

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
            final Action tagAction = getAction(interaction);
            if (tagAction != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        component.getDispatcher().dispatch(tagAction);
                    }
                });
            }
        }
        return view;
    }

    @Nullable
    private Action getAction(@NonNull final Interaction interaction) {
        switch (interaction.getAction().getTag()) {
        case LINK:
            return new CopyAction(interaction.getContent());
        case COPY:
            return new LinkAction(interaction.getAction().getUrl());
        default:
            return null;
        }
    }
}
