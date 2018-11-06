package com.mercadopago.android.px.model;

public class InstructionAction {
    private String label;
    private String url;
    private String tag;
    private String content;

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public String getTag() {
        return tag;
    }

    public String getContent() {
        return content;
    }

    public static class Tags {
        public static final String LINK = "link";
        public static final String COPY = "copy";
    }
}
