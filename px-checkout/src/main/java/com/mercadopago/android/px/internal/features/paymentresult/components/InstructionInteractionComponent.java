package com.mercadopago.android.px.internal.features.paymentresult.components;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.model.Interaction;

public class InstructionInteractionComponent extends Component<InstructionInteractionComponent.Props, Void> {

    public InstructionInteractionComponent(@NonNull final Props props, @NonNull final ActionDispatcher dispatcher) {
        super(props, dispatcher);
    }

    public static class Props {

        public final Interaction interaction;

        public Props(final Props.Builder builder) {
            interaction = builder.interaction;
        }

        public Props.Builder toBuilder() {
            return new Props.Builder()
                .setInteraction(interaction);
        }

        public static class Builder {
            public Interaction interaction;

            public Props.Builder setInteraction(
                @NonNull final Interaction interaction) {
                this.interaction = interaction;
                return this;
            }

            public Props build() {
                return new Props(this);
            }
        }
    }
}
