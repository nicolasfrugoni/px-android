package com.mercadopago.android.px.internal.features.paymentresult.props;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Interaction;
import java.util.List;

public class InstructionInteractionsProps {

    public final List<Interaction> interactions;

    public InstructionInteractionsProps(@NonNull final Builder builder) {
        interactions = builder.interactionsProps;
    }

    public Builder toBuilder() {
        return new InstructionInteractionsProps.Builder()
            .setInstructionInteractions(interactions);
    }

    public static class Builder {
        public List<Interaction> interactionsProps;

        public Builder setInstructionInteractions(final List<Interaction> interactionsProps) {
            this.interactionsProps = interactionsProps;
            return this;
        }

        public InstructionInteractionsProps build() {
            return new InstructionInteractionsProps(this);
        }
    }
}
