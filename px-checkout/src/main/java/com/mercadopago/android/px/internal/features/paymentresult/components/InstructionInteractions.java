package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.paymentresult.props.InstructionInteractionsProps;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.model.Interaction;
import java.util.ArrayList;
import java.util.List;

public class InstructionInteractions extends Component<InstructionInteractionsProps, Void> {

    public InstructionInteractions(@NonNull final InstructionInteractionsProps instructionInteractionsProps,
        @NonNull final ActionDispatcher dispatcher) {
        super(instructionInteractionsProps, dispatcher);
    }

    public List<InstructionInteractionComponent> getInteractionComponents() {
        final List<InstructionInteractionComponent> componentList = new ArrayList<>();

        for (final Interaction interaction : props.interactions) {
            final InstructionInteractionComponent.Props interactionProps =
                new InstructionInteractionComponent.Props.Builder()
                    .setInteraction(interaction)
                    .build();

            final InstructionInteractionComponent component =
                new InstructionInteractionComponent(interactionProps, getDispatcher());

            componentList.add(component);
        }

        return componentList;
    }
}
