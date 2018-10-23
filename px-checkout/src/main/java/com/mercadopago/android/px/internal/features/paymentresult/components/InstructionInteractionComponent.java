package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.model.InstructionInteraction;

public class InstructionInteractionComponent extends Component<InstructionInteractionComponent.Props, Void> {

    public InstructionInteractionComponent(@NonNull final Props props, @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    public static class Props {

        public final InstructionInteraction interaction;

        public Props(final Props.Builder builder) {
            interaction = builder.interaction;
        }

        public Props.Builder toBuilder() {
            return new Props.Builder()
                .setInteraction(interaction);
        }

        public static class Builder {
            public InstructionInteraction interaction;

            public Props.Builder setInteraction(
                @NonNull final InstructionInteraction interaction) {
                this.interaction = interaction;
                return this;
            }

            public Props build() {
                return new Props(this);
            }
        }
    }
}
