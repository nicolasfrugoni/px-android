package com.mercadopago.android.px.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.test.espresso.action.ViewActions;
import android.view.View;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class PayerInformationPage extends PageObject<CheckoutValidator> {

    public PayerInformationPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public PayerInformationPage(final CheckoutValidator validator) {
        super(validator);
    }

    // Case debit card - a way to resolve this is with a card type
    public PayerInformationPage enterIdentificationTypeAndNumberAndPressNext(@NonNull final String idType,
        @NonNull final String idNumber) {
        typeTextInView(idNumber, com.mercadopago.android.px.R.id.mpsdkCardIdentificationNumber);
        pressNextButton();
        return new PayerInformationPage(validator);
    }

    public PayerInformationPage enterFirstNameAndPressNext(@NonNull final String firstName) {
        typeTextInView(firstName, com.mercadopago.android.px.R.id.mpsdkName);
        pressNextButton();
        return new PayerInformationPage(validator);
    }

    public ReviewAndConfirmPage enterLastNameAndPressNext(@NonNull final String lastName) {
        typeTextInView(lastName, com.mercadopago.android.px.R.id.mpsdkLastName);
        pressNextButton();
        return new ReviewAndConfirmPage();
    }

    private void pressNextButton() {
        Matcher<View> cardNextButtonTextMatcher = withId(com.mercadopago.android.px.R.id.mpsdkNextButton);
        onView(cardNextButtonTextMatcher).perform(click());
    }

    private void typeTextInView(final String text, final int mpsdkCardIdentificationNumber) {
        final Matcher<View> viewMatcher = withId(mpsdkCardIdentificationNumber);
        onView(viewMatcher).perform(typeText(text));
    }

    @Override
    public PayerInformationPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public PaymentMethodPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new PaymentMethodPage(validator);
    }
}