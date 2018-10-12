package com.mercadopago.android.px.internal.driver;

import com.mercadopago.android.px.internal.navigation.DefaultPayerInformationDriver;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.Payer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPayerInformationDriverTest {
    private static final String TEST_NAME = "Test Name";
    private static final String TEST_LASTNAME = "Test Lastname";
    private static final String TEST_ID_TYPE = "CPF";
    private static final String TEST_ID_NUMBER = "12312312312";

    private DefaultPayerInformationDriver handler;

    @Mock private DefaultPayerInformationDriver.PayerInformationDriverCallback payerInfoDriverCallback;
    @Mock private Payer payerMock;
    @Mock private Identification identification;

    @Before
    public void setUp() {
        handler = new DefaultPayerInformationDriver(payerMock);
        when(identification.getNumber()).thenReturn(TEST_ID_NUMBER);
        when(identification.getType()).thenReturn(TEST_ID_TYPE);
        when(payerMock.getIdentification()).thenReturn(identification);
        when(payerMock.getFirstName()).thenReturn(TEST_NAME);
        when(payerMock.getLastName()).thenReturn(TEST_LASTNAME);
    }

    @Test
    public void whenPayerIsNullThenCollectPayerInfo() {
        new DefaultPayerInformationDriver(null).drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasInvalidNameThenCollectPayerInfo() {
        when(payerMock.getFirstName()).thenReturn(StringUtils.EMPTY);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasNullNameThenCollectPayerInfo() {
        when(payerMock.getFirstName()).thenReturn(null);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasInvalidLastNameThenCollectPayerInfo() {
        when(payerMock.getLastName()).thenReturn(StringUtils.EMPTY);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasNullLastNameThenCollectPayerInfo() {
        when(payerMock.getLastName()).thenReturn(null);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasInvalidIdentificationNumberThenCollectPayerInfo() {
        when(identification.getNumber()).thenReturn(StringUtils.EMPTY);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasInvalidIdentificationTypeThenCollectPayerInfo() {
        when(identification.getType()).thenReturn(StringUtils.EMPTY);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasNullIdentificationThenCollectPayerInfo() {
        when(payerMock.getIdentification()).thenReturn(null);
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToNewPayerData();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }

    @Test
    public void whenPayerIsNotNullAndHasInfoThenDriveToReviewConfirm() {
        handler.drive(payerInfoDriverCallback);
        verify(payerInfoDriverCallback).driveToReviewConfirm();
        verifyNoMoreInteractions(payerInfoDriverCallback);
    }
}