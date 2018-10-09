package com.mercadopago.android.px.internal.features.review_and_confirm.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class TermsAndConditionsModel implements Parcelable {
    private final String termsAndConditionsUrl;
    private final String message;
    private final String messageLinked;
    private final LineSeparatorType lineSeparatorType;

    public TermsAndConditionsModel(@NonNull final String termsAndConditionsUrl,
        @NonNull final String message,
        @NonNull final String messageLinked,
        @NonNull final LineSeparatorType lineSeparatorType) {

        this.termsAndConditionsUrl = termsAndConditionsUrl;
        this.message = message;
        this.messageLinked = messageLinked;
        this.lineSeparatorType = lineSeparatorType;
    }

    protected TermsAndConditionsModel(final Parcel in) {
        termsAndConditionsUrl = in.readString();
        message = in.readString();
        messageLinked = in.readString();
        lineSeparatorType = LineSeparatorType.valueOf(in.readString());
    }

    public static final Creator<TermsAndConditionsModel> CREATOR = new Creator<TermsAndConditionsModel>() {
        @Override
        public TermsAndConditionsModel createFromParcel(Parcel in) {
            return new TermsAndConditionsModel(in);
        }

        @Override
        public TermsAndConditionsModel[] newArray(int size) {
            return new TermsAndConditionsModel[size];
        }
    };

    public String getUrl() {
        return termsAndConditionsUrl;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageLinked() {
        return messageLinked;
    }

    public LineSeparatorType getLineSeparatorType() {
        return lineSeparatorType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(termsAndConditionsUrl);
        dest.writeString(message);
        dest.writeString(messageLinked);
        dest.writeString(lineSeparatorType.name());
    }
}
