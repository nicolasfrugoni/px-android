package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.internal.view.RendererFactory;
import java.util.List;

public class InstructionInteractionsRenderer extends Renderer<InstructionInteractions> {
    @Override
    public View render(@NonNull final InstructionInteractions component, @NonNull final Context context,
        @Nullable final ViewGroup parent) {
        final LinearLayout instructionsView = new LinearLayout(context);
        instructionsView.setOrientation(LinearLayout.VERTICAL);
        instructionsView.setBackgroundColor(context.getResources().getColor(R.color.px_white_background));
        instructionsView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
        final int sideDimensionPadding = (int) context.getResources().getDimension(R.dimen.px_xl_margin);
        final int bottomDimensionPadding = (int) context.getResources().getDimension(R.dimen.px_xxs_margin);
        instructionsView.setPadding(sideDimensionPadding, 0, sideDimensionPadding, bottomDimensionPadding);

        final List<InstructionInteractionComponent> interactionComponentList = component.getInteractionComponents();
        for (final InstructionInteractionComponent instructionInteractionComponent : interactionComponentList) {
            final View view = RendererFactory.create(context, instructionInteractionComponent).render(null);
            instructionsView.addView(view);
        }

        parent.addView(instructionsView);
        return parent;
    }
}
