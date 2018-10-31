package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.features.providers.IssuersProvider;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.ArrayList;
import java.util.List;

public class IssuersPresenter extends MvpPresenter<IssuersActivityView, IssuersProvider> {

    @Nullable private PaymentMethod paymentMethod;
    //Local vars
    @Nullable private List<Issuer> mIssuers;
    private CardInfo mCardInfo;
    private FailureRecovery mFailureRecovery;
    private boolean comesFromStorageFlow = false;

    //Card Info
    private String mBin = "";

    public IssuersPresenter(@Nullable final PaymentMethod paymentMethod, final boolean comesFromStorageFlow) {
        this.paymentMethod = paymentMethod;
        this.comesFromStorageFlow = comesFromStorageFlow;
    }

    public void initialize() {
        if (wereIssuersSet()) {
            resolveIssuers(mIssuers);
        } else {
            getIssuersAsync();
        }
    }

    private boolean wereIssuersSet() {
        return mIssuers != null;
    }

    /* default */ void resolveIssuers(@Nullable List<Issuer> issuers) {
        if (issuers == null) {
            issuers = new ArrayList<>();
        }

        setIssuers(issuers);

        if (mIssuers.isEmpty()) {
            getView().showError(getResourcesProvider().getEmptyIssuersError(), "");
        } else if (mIssuers.size() == 1) {
            final Issuer issuer = issuers.get(0);
            getView().finishWithResult(issuer);
        } else {
            getView().showHeader();
            getView().showIssuers(issuers, getDpadSelectionCallback());
        }
    }

    /* default */ void getIssuersAsync() {
        getView().showLoadingView();

        getResourcesProvider().getIssuers(getPaymentMethod().getId(), mBin,
            new TaggedCallback<List<Issuer>>(ApiUtil.RequestOrigin.GET_ISSUERS) {
                @Override
                public void onSuccess(final List<Issuer> issuers) {
                    getView().stopLoadingView();
                    resolveIssuers(issuers);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    getView().stopLoadingView();

                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            getIssuersAsync();
                        }
                    });

                    getView().showError(error, ApiUtil.RequestOrigin.GET_ISSUERS);
                }
            });
    }

    private OnSelectedCallback<Integer> getDpadSelectionCallback() {
        return new OnSelectedCallback<Integer>() {
            @Override
            public void onSelected(final Integer position) {
                onItemSelected(position);
            }
        };
    }

    public void onItemSelected(final int position) {
        final Issuer issuer = mIssuers.get(position);
        if (comesFromStorageFlow) {
            getView().finishWithResultForCardStorage(issuer.getId());
        } else {
            getView().finishWithResult(issuer);
        }
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    @Nullable
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@NonNull final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setIssuers(@Nullable final List<Issuer> issuers) {
        mIssuers = issuers;
    }

    public CardInfo getCardInfo() {
        return mCardInfo;
    }

    public void setCardInfo(final CardInfo cardInfo) {
        mCardInfo = cardInfo;

        if (mCardInfo != null) {
            mBin = mCardInfo.getFirstSixDigits();
        }
    }

    public FailureRecovery getFailureRecovery() {
        return mFailureRecovery;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public String getBin() {
        return mBin;
    }

    public boolean isRequiredCardDrawn() {
        return mCardInfo != null && paymentMethod != null;
    }
}
