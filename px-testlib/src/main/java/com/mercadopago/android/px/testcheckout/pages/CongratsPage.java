package com.mercadopago.android.px.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import com.mercadopago.android.px.testcheckout.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CongratsPage extends PageObject<CheckoutValidator> {

    public CongratsPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public CongratsPage(final CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public CongratsPage validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public void clickButtonWithText(@NonNull final String text) {
        onView(withText(Matchers.containsString(text))).perform(click());
    }

    public void clickPrimaryButton() {
        onView(withId(R.id.px_button_primary)).perform(click());
    }
}
