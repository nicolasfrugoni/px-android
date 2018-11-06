package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.model.InstructionAction;
import com.mercadopago.android.px.model.Interaction;

public class InstructionInteractionComponentRenderer extends Renderer<InstructionInteractionComponent> {
    @Override
    protected View render(@NonNull final InstructionInteractionComponent component, @NonNull final Context context,
        @Nullable final ViewGroup parent) {
        final View view = inflate(R.layout.px_payment_result_instruction_interaction,null);
        final ViewGroup interactionContainer = view.findViewById(R.id.mpsdkInstructionInteractionContainer);
        final MPTextView title = view.findViewById(R.id.mpsdkInteractionTitle);
        final MPTextView content = view.findViewById(R.id.mpsdkInteractionContent);

        final Interaction interaction = component.props.interaction;
        setText(title, interaction.getTitle());
        setText(content, interaction.getContent());

        final InstructionAction action = interaction.getAction();
        if (action != null) {
            final InstructionsAction.Prop props =
                new InstructionsAction.Prop.Builder().setInstructionAction(action).build();

            RendererFactory.create(context, new InstructionsAction(props, component.getDispatcher()))
                .render(interactionContainer);
        }
        return view;
    }
}
