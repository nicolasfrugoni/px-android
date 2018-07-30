package com.mercadopago.android.px.views;

import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.mvp.MvpView;
import java.util.List;

/**
 * Created by mromar on 29/9/16.
 */
public interface PayerInformationView extends MvpView {

    void initializeIdentificationTypes(List<IdentificationType> identificationTypes);

    void setIdentificationNumberRestrictions(String type);

    void clearErrorIdentificationNumber();

    void clearErrorName();

    void clearErrorLastName();

    void setErrorIdentificationNumber();

    void setErrorName();

    void setErrorLastName();

    void setErrorView(String message);

    void clearErrorView();

    void showInputContainer();

    void showError(MercadoPagoError error, String requestOrigin);

    void showProgressBar();

    void hideProgressBar();
}
