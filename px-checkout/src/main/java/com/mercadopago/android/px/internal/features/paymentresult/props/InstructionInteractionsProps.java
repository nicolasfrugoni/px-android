package com.mercadopago.android.px.internal.features.paymentresult.props;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.InstructionInteraction;
import java.util.List;

public class InstructionInteractionsProps {

    public final List<InstructionInteraction> instructionInteractions;

    public InstructionInteractionsProps(@NonNull final List<InstructionInteraction> instructionInteractions) {
        this.instructionInteractions = instructionInteractions;
    }

    public InstructionInteractionsProps(@NonNull Builder builder) {
        instructionInteractions = builder.instructionInteractionsProps;
    }

    public Builder toBuilder() {
        return new InstructionInteractionsProps.Builder()
            .setInstructionInteractions(instructionInteractions);
    }

    public static class Builder {
        public List<InstructionInteraction> instructionInteractionsProps;

        public Builder setInstructionInteractions(List<InstructionInteraction> instructionInteractionsProps) {
            this.instructionInteractionsProps = instructionInteractionsProps;
            return this;
        }

        public InstructionInteractionsProps build() {
            return new InstructionInteractionsProps(this);
        }
    }
}
