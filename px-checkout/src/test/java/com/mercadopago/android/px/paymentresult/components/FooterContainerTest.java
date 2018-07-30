package com.mercadopago.android.px.paymentresult.components;

import com.mercadopago.android.px.components.ActionDispatcher;
import com.mercadopago.android.px.components.Footer;
import com.mercadopago.android.px.components.NextAction;
import com.mercadopago.android.px.components.RecoverPaymentAction;
import com.mercadopago.android.px.core.CheckoutStore;
import com.mercadopago.android.px.mocks.PaymentResults;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.paymentresult.PaymentResultProvider;
import com.mercadopago.android.px.preferences.PaymentResultScreenPreference;
import com.mercadopago.android.px.review_and_confirm.components.actions.ChangePaymentMethodAction;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FooterContainerTest {

    private static final String LABEL_REVIEW_TC_INFO = "Revisar los datos de tarjeta ";
    private static final String LABEL_CHANGE_PAYMENT_METHOD = "Pagar con otro medio";
    private static final String LABEL_KEEP_SHOPPING = "Seguir comprando";
    private static final String LABEL_CANCEL_PAYMENT = "Cancelar pago";

    private static final String EXIT_TITLE = "Exit sample title";

    private ActionDispatcher dispatcher;
    private PaymentResultProvider provider;

    @Before
    public void setup() {
        dispatcher = mock(ActionDispatcher.class);
        provider = mock(PaymentResultProvider.class);

        when(provider.getContinueShopping()).thenReturn(LABEL_KEEP_SHOPPING);
        when(provider.getRejectedBadFilledCardTitle()).thenReturn(LABEL_REVIEW_TC_INFO);
        when(provider.getChangePaymentMethodLabel()).thenReturn(LABEL_CHANGE_PAYMENT_METHOD);
        when(provider.getCancelPayment()).thenReturn(LABEL_CANCEL_PAYMENT);

        new PaymentResultScreenPreference.Builder().build();
    }

    @Test
    public void whenAskForFooterIsNotNull() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);
        Footer footer = footerContainer.getFooter();
        assertNotNull(footer);
    }

    @Test
    public void testApproved() {

        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNull(props.buttonAction);
        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, LABEL_KEEP_SHOPPING);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, is(instanceOf(NextAction.class)));
    }

    @Test
    public void testApprovedExitButtonTitle() {

        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();

        final PaymentResultScreenPreference preference = new PaymentResultScreenPreference.Builder()
            .setExitButtonTitle(EXIT_TITLE)
            .build();

        CheckoutStore.getInstance().setPaymentResultScreenPreference(preference);

        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNull(props.buttonAction);
        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, EXIT_TITLE);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, is(instanceOf(NextAction.class)));
    }

    @Test
    public void testRejectedBadFilledDatePaymentResult() {
        //TODO remove
        CheckoutStore.getInstance()
            .setPaymentResultScreenPreference(new PaymentResultScreenPreference.Builder().build());
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNotNull(props.buttonAction);
        assertEquals(props.buttonAction.label, LABEL_REVIEW_TC_INFO);
        Assert.assertNotNull(props.buttonAction.action);
        assertThat(props.buttonAction.action, instanceOf(RecoverPaymentAction.class));

        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, LABEL_CHANGE_PAYMENT_METHOD);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, instanceOf(ChangePaymentMethodAction.class));
    }

    @Test
    public void testRejectedCallForAuth() {

        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        CheckoutStore.getInstance().setPaymentResultScreenPreference(new PaymentResultScreenPreference.Builder().build());

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNotNull(props.buttonAction);
        assertEquals(props.buttonAction.label, LABEL_CHANGE_PAYMENT_METHOD);
        Assert.assertNotNull(props.buttonAction.action);
        assertThat(props.buttonAction.action, instanceOf(ChangePaymentMethodAction.class));

        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, LABEL_CANCEL_PAYMENT);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, is(instanceOf(NextAction.class)));
    }

    @Test
    public void testRejected() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        CheckoutStore.getInstance().setPaymentResultScreenPreference(new PaymentResultScreenPreference.Builder().build());

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNotNull(props.buttonAction);
        assertEquals(props.buttonAction.label, LABEL_CHANGE_PAYMENT_METHOD);
        Assert.assertNotNull(props.buttonAction.action);
        assertThat(props.buttonAction.action, instanceOf(ChangePaymentMethodAction.class));

        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, LABEL_CANCEL_PAYMENT);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, is(instanceOf(NextAction.class)));
    }

    @Test
    public void testRejectedDisableSecondaryExitButton() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final PaymentResultScreenPreference preference = new PaymentResultScreenPreference.Builder()
            .disableRejectedSecondaryExitButton()
            .build();

        CheckoutStore.getInstance().setPaymentResultScreenPreference(preference);

        final FooterContainer footerContainer = new FooterContainer(
            new FooterContainer.Props(paymentResult), dispatcher, provider);

        final Footer.Props props = footerContainer.getFooterProps();

        Assert.assertNotNull(props);
        Assert.assertNull(props.buttonAction);

        Assert.assertNotNull(props.linkAction);
        assertEquals(props.linkAction.label, LABEL_CANCEL_PAYMENT);
        Assert.assertNotNull(props.linkAction.action);
        assertThat(props.linkAction.action, is(instanceOf(NextAction.class)));
    }
}