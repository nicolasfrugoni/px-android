package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.internal.view.RendererFactory;

/**
 * Created by vaserber on 11/14/17.
 */

public class InstructionsContentRenderer extends Renderer<InstructionsContent> {

    @Override
    public View render(final InstructionsContent component, final Context context, final ViewGroup parent) {
        final View instructionsView = inflate(R.layout.px_payment_result_instructions_content, parent);
        final ViewGroup parentViewGroup = instructionsView.findViewById(R.id.mpsdkInstructionsContentContainer);
        final View bottomMarginView = instructionsView.findViewById(R.id.mpsdkContentBottomMargin);

        if (component.needsBottomMargin()) {
            bottomMarginView.setVisibility(View.VISIBLE);
        } else {
            bottomMarginView.setVisibility(View.GONE);
        }

        if (component.hasInfo()) {
            RendererFactory.create(context, component.getInfoComponent()).render(parentViewGroup);
        }
        if(component.hasInstructionInteractions()){
            RendererFactory.create(context, component.getInteractionsComponent()).render(parentViewGroup);
        }
        if (component.hasReferences()) {
            RendererFactory.create(context, component.getReferencesComponent()).render(parentViewGroup);
        }
        if (component.hasTertiaryInfo()) {
            RendererFactory.create(context, component.getTertiaryInfoComponent()).render(parentViewGroup);
        }
        if (component.hasAccreditationTime()) {
            RendererFactory.create(context, component.getAccreditationTimeComponent()).render(parentViewGroup);
        }
        if (component.hasActions()) {
            RendererFactory.create(context, component.getActionsComponent()).render(parentViewGroup);
        }

        return instructionsView;
    }
}
