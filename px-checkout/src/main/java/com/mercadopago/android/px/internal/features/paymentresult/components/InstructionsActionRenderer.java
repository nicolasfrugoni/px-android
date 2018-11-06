package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.internal.view.CopyAction;
import com.mercadopago.android.px.internal.view.LinkAction;
import com.mercadopago.android.px.internal.view.Renderer;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.InstructionAction;

import static com.mercadopago.android.px.model.InstructionAction.Tags.COPY;
import static com.mercadopago.android.px.model.InstructionAction.Tags.LINK;

public class InstructionsActionRenderer extends Renderer<InstructionsAction> {

    @Override
    public View render(@NonNull final InstructionsAction component, @NonNull final Context context,
        final ViewGroup parent) {

        final MeliButton button = new MeliButton(context);
        button.setType(MeliButton.Type.OPTION_PRIMARY);

        final Action action = getAction(component.props.instructionAction);
        if (action != null) {
            button.setText(component.props.instructionAction.getLabel());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    component.getDispatcher().dispatch(action);
                }
            });
            parent.addView(button);
        }

        return parent;
    }

    @Nullable
    private Action getAction(@NonNull final InstructionAction action) {
        switch (action.getTag()) {
        case COPY:
            return new CopyAction(action.getContent());
        case LINK:
            return new LinkAction(action.getUrl());
        default:
            return null;
        }
    }
}
