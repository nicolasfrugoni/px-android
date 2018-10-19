package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.paymentresult.props.InstructionInteractionsProps;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.model.InstructionInteraction;
import java.util.ArrayList;
import java.util.List;

public class InstructionInteractions extends Component<InstructionInteractionsProps, Void> {

    public InstructionInteractions(@NonNull final InstructionInteractionsProps instructionInteractionsProps,
        @NonNull final ActionDispatcher dispatcher) {
        super(instructionInteractionsProps, dispatcher);
    }

    public List<InstructionInteractionComponent> getInteractionComponents() {
        List<InstructionInteractionComponent> componentList = new ArrayList<>();

        for (InstructionInteraction interaction : props.instructionInteractions) {
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
