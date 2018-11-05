package com.mercadopago.android.px.testcheckout.pages;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import java.util.regex.Matcher;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PaymentMethodPage extends PageObject<CheckoutValidator> {

    protected PaymentMethodPage() {
    }

    public PaymentMethodPage(final CheckoutValidator validator) {
        super(validator);
    }

    //Payer access token 1 = "APP_USR-1505-080815-c6ea450de1bf828e39add499237d727f-312667294"
    public InstallmentsPage selectVisaCreditCardWithoutEsc(final String lastFourDigits) {
        onView(withText(Matchers.containsString(lastFourDigits))).perform(click());
        return new InstallmentsPage(validator);
    }

    public SecurityCodePage selectSavedDebitCard() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new SecurityCodePage(validator);
    }

    public CardPage selectCard() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new CardPage(validator);
    }

    public  CreditCardPage selectCardWhenSavedPresent() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        return new CreditCardPage(validator);
    }

    public CashPage selectCash() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        return new CashPage(validator);
    }

    public ReviewAndConfirmPage selectTicketWithDefaultPayer() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new ReviewAndConfirmPage();
    }

    public PayerInformationPage selectTicketWithoutPayer() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new PayerInformationPage();
    }

    public DiscountDetailPage pressOnDiscountDetail() {
        onView(withId(R.id.amount_view)).perform(click());
        return new DiscountDetailPage(validator);
    }

    public DiscountCodeInputPage pressOnDiscountCodeInput() {
        onView(withId(R.id.amount_view)).perform(click());
        return new DiscountCodeInputPage(validator);
    }

    public OneTapPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new OneTapPage(validator);
    }

    @Override
    public PaymentMethodPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
