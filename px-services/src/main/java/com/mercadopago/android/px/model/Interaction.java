package com.mercadopago.android.px.model;

public class Interaction {

    private InstructionAction action;
    private String title;
    private String content;

    public Interaction(final InstructionAction action, final String title, final String content) {
        this.action = action;
        this.title = title;
        this.content = content;
    }

    public InstructionAction getAction() {
        return action;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
