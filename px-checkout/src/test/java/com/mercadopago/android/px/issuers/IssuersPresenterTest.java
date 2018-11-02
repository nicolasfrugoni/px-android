package com.mercadopago.android.px.issuers;

import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.features.IssuersActivityView;
import com.mercadopago.android.px.internal.features.IssuersPresenter;
import com.mercadopago.android.px.internal.features.providers.IssuersProvider;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class IssuersPresenterTest {

    private IssuersPresenter presenter;

    //STUB
    private MockedView stubView = new MockedView();
    private MockedProvider provider = new MockedProvider();

    @Mock
    private UserSelectionRepository userSelectionRepository;

    @Mock
    private IssuersActivityView mockedView;

    @Before
    public void setUp() {
        //Simulation no charge - no discount
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        presenter = new IssuersPresenter(paymentMethod, false);
        presenter.attachView(stubView);
        presenter.attachResourcesProvider(provider);
    }

    @Test
    public void whenIssuersAreNullThenGetIssuersAndShow() {
        final List<Issuer> issuers = Issuers.getIssuersListMLA();
        provider.setResponse(issuers);

        presenter.initialize();

        stubView.simulateIssuerSelection(0);

        assertTrue(stubView.issuersShown);
        assertTrue(stubView.headerShown);
        assertEquals(issuers.get(0), stubView.selectedIssuer);
        assertTrue(stubView.finishWithResult);
    }

    @Test
    public void whenInitIssuersWithNoIssuersAndRetrieveIssuersAndOnlyOneIssuerGivenThenFinish() {
        //Param for issuer provider
        final PaymentMethod mockedPaymentMethod = mock(PaymentMethod.class);
        presenter = new IssuersPresenter(mockedPaymentMethod, false);

        //The returned issuer list from provider
        final List<Issuer> issuers = Issuers.getOneIssuerListMLA();
        provider.setResponse(issuers);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.initialize();

        verify(mockedView).showLoadingView();
        verify(mockedView).stopLoadingView();
        verify(mockedView).finishWithResult(issuers.get(0));

        verifyNoMoreInteractions(mockedView);
        verifyNoMoreInteractions(userSelectionRepository);
    }

    @Test
    public void whenIssuersAreNotNullThenShowIssuers() {
        final List<Issuer> issuers = Issuers.getIssuersListMLA();

        presenter.setIssuers(issuers);

        presenter.initialize();

        stubView.simulateIssuerSelection(0);

        assertTrue(stubView.issuersShown);
        assertTrue(stubView.headerShown);
        assertEquals(issuers.get(0), stubView.selectedIssuer);
        assertTrue(stubView.finishWithResult);
    }

    @Test
    public void whenGetIssuersIsNullThenGetNewIssuersList() {
        final List<Issuer> issuers = new ArrayList<Issuer>();
        provider.setResponse(issuers);

        presenter.initialize();

        assertTrue(stubView.errorShown);
    }

    @Test
    public void whenGetIssuersFailThenShowMercadoPagoError() {
        final MercadoPagoError mercadoPagoError = new MercadoPagoError("Error", true);
        provider.setResponse(mercadoPagoError);

        presenter.initialize();

        assertFalse(stubView.loadingViewShown);
        assertTrue(stubView.errorShown);
    }

    @Test
    public void whenGetIssuersReturnNullThenShowMercadoPagoError() {
        final List<Issuer> issuers = null;
        provider.setResponse(issuers);

        presenter.initialize();

        assertFalse(stubView.loadingViewShown);
        assertTrue(stubView.errorShown);
        assertTrue(provider.emptyIssuersErrorGotten);
    }

    @Test
    public void whenRecoverFromFailureThenGetIssuersAgain() {
        final MercadoPagoError mercadoPagoError = new MercadoPagoError("Error", true);
        provider.setResponse(mercadoPagoError);

        presenter.initialize();

        presenter.recoverFromFailure();

        assertFalse(stubView.loadingViewShown);
        assertTrue(stubView.errorShown);
        assertNotEquals(presenter.getFailureRecovery(), null);
    }

    @Test
    public void whenRecoverFromFailureIsNullThenNotRecoverFromError() {
        presenter.recoverFromFailure();

        assertEquals(presenter.getFailureRecovery(), null);
    }

    @Test
    public void whenSetCardInfoThenSetBin() {
        final CardInfo cardInfo = getCardInfo();

        presenter.setCardInfo(cardInfo);

        assertEquals(presenter.getBin(), getCardInfo().getFirstSixDigits());
    }

    @Test
    public void whenCardInfoIsNullThenPresenterBinIsEmpty() {
        presenter.setCardInfo(null);

        assertEquals(presenter.getBin(), "");
    }

    @Test
    public void whenIsCardInfoAndPaymentMethodAvailableThenIsNotRequiredCardDrawn() {
        final CardInfo cardInfo = getCardInfo();
        presenter.setCardInfo(cardInfo);
        assertTrue(presenter.isRequiredCardDrawn());
    }

    @Test
    public void whenIsNotPaymentMethodAvailableThenIsNotRequiredCardDrawn() {
        final CardInfo cardInfo = getCardInfo();
        presenter = new IssuersPresenter(null, false);
        presenter.setCardInfo(cardInfo);

        assertFalse(presenter.isRequiredCardDrawn());
    }

    @Test
    public void whenIsNotCardInfoAvailableThenIsNotRequiredCardDrawn() {
        assertFalse(presenter.isRequiredCardDrawn());
    }

    private CardInfo getCardInfo() {
        final Card card = new Card();
        card.setLastFourDigits("4321");
        card.setFirstSixDigits("123456");

        return new CardInfo(card);
    }

    @Test
    public void whenIssuersSelectionComesFromStorageThenReturnSelectedIssuerId() {
        final PaymentMethod paymentMethod = PaymentMethods.getPaymentMethodOnVisa();
        presenter = new IssuersPresenter(paymentMethod, true);
        presenter.attachView(stubView);
        presenter.attachResourcesProvider(provider);
        final List<Issuer> issuers = Issuers.getIssuersListMLA();

        presenter.setIssuers(issuers);

        presenter.initialize();

        stubView.simulateIssuerSelection(0);

        assertTrue(stubView.issuersShown);
        assertTrue(stubView.headerShown);
        assertEquals(issuers.get(0), stubView.selectedIssuer);
        assertTrue(stubView.finishWithResult);
    }

    private class MockedProvider implements IssuersProvider {

        private boolean shouldFail;
        private List<Issuer> successfulResponse;
        private MercadoPagoError failedResponse;

        private boolean emptyIssuersErrorGotten = false;

        void setResponse(List<Issuer> issuers) {
            shouldFail = false;
            successfulResponse = issuers;
        }

        private void setResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }

        @Override
        public void getIssuers(String paymentMethodId, String bin, TaggedCallback<List<Issuer>> taggedCallback) {

            if (shouldFail) {
                taggedCallback.onFailure(failedResponse);
            } else {
                taggedCallback.onSuccess(successfulResponse);
            }
        }

        @Override
        public MercadoPagoError getEmptyIssuersError() {
            emptyIssuersErrorGotten = true;
            return null;
        }

        @Override
        public String getCardIssuersTitle() {
            return null;
        }
    }

    private class MockedView implements IssuersActivityView {

        private boolean issuersShown = false;
        private boolean headerShown = false;
        private boolean loadingViewShown = false;
        private boolean errorShown = false;
        private boolean finishWithResult = false;
        private List<Issuer> issuersList;
        private Issuer selectedIssuer;
        private OnSelectedCallback<Integer> issuerSelectionCallback;

        @Override
        public void showIssuers(List<Issuer> issuers, OnSelectedCallback<Integer> onSelectedCallback) {
            issuerSelectionCallback = onSelectedCallback;
            issuersShown = true;
            issuersList = issuers;
        }

        @Override
        public void showHeader() {
            headerShown = true;
        }

        @Override
        public void showLoadingView() {
            loadingViewShown = true;
        }

        @Override
        public void stopLoadingView() {
            loadingViewShown = false;
        }

        @Override
        public void showError(MercadoPagoError error, String requestOrigin) {
            errorShown = true;
        }

        @Override
        public void finishWithResult(final Issuer issuer) {
            finishWithResult = true;
        }

        @Override
        public void finishWithResultForCardStorage(final Long issuerId) {
            finishWithResult = true;
        }

        private void simulateIssuerSelection(int index) {
            issuerSelectionCallback.onSelected(index);
            if (issuersList != null && !issuersList.isEmpty()) {
                selectedIssuer = issuersList.get(index);
            }
        }
    }
}
