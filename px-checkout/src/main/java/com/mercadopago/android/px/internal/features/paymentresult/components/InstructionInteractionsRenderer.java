package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.internal.view.RendererFactory;
import java.util.List;

public class InstructionInteractionsRenderer extends Renderer<InstructionInteractions> {
    @Override
    public View render(@NonNull final InstructionInteractions component, @NonNull final Context context,
        @Nullable final ViewGroup parent) {
        final View instructionsView = inflate(R.layout.px_payment_result_instructions_interactions, parent);
        final ViewGroup instructionsViewGroup =
            instructionsView.findViewById(R.id.mpsdkInstructionsInteractionsContainer);

        final List<InstructionInteractionComponent> interactionComponentList = component.getInteractionComponents();
        for (final InstructionInteractionComponent instructionInteractionComponent : interactionComponentList) {
            final View interaction = RendererFactory.create(context, instructionInteractionComponent).render(null);
            instructionsViewGroup.addView(interaction);
        }

        return instructionsView;
    }
}
