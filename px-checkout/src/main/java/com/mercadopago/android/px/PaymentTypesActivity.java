package com.mercadopago.android.px;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.adapters.PaymentTypesAdapter;
import com.mercadopago.android.px.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.controllers.CheckoutTimer;
import com.mercadopago.android.px.customviews.MPTextView;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.listeners.RecyclerItemClickListener;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.presenters.PaymentTypesPresenter;
import com.mercadopago.android.px.services.exceptions.ApiException;
import com.mercadopago.android.px.tracker.FlowHandler;
import com.mercadopago.android.px.tracker.MPTrackingContext;
import com.mercadopago.android.px.tracking.model.ScreenViewEvent;
import com.mercadopago.android.px.tracking.utils.TrackingUtil;
import com.mercadopago.android.px.uicontrollers.FontCache;
import com.mercadopago.android.px.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.util.ApiUtil;
import com.mercadopago.android.px.util.ErrorUtil;
import com.mercadopago.android.px.util.JsonUtil;
import com.mercadopago.android.px.util.ScaleUtil;
import com.mercadopago.android.px.views.PaymentTypesActivityView;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by vaserber on 10/25/16.
 */

public class PaymentTypesActivity extends MercadoPagoBaseActivity implements PaymentTypesActivityView {

    protected PaymentTypesPresenter mPresenter;
    //ViewMode
    protected boolean mLowResActive;
    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mTimerTextView;
    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected Toolbar mNormalToolbar;
    protected FrontCardView mFrontCardView;
    private Activity mActivity;
    //View controls
    private PaymentTypesAdapter mPaymentTypesAdapter;
    private RecyclerView mPaymentTypesRecyclerView;
    private ViewGroup mProgressLayout;
    private MPTextView mLowResTitleToolbar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPresenter == null) {
            mPresenter = new PaymentTypesPresenter();
        }
        mPresenter.setView(this);
        mActivity = this;
        getActivityParameters();

        analizeLowRes();
        setContentView();
        mPresenter.validateActivityParameters();
    }

    private void getActivityParameters() {

        List<PaymentMethod> paymentMethods;
        try {
            Type listType = new TypeToken<List<PaymentMethod>>() {
            }.getType();
            paymentMethods =
                JsonUtil.getInstance().getGson().fromJson(getIntent().getStringExtra("paymentMethods"), listType);
        } catch (Exception ex) {
            paymentMethods = null;
        }

        List<PaymentType> paymentTypes;
        try {
            Type listType = new TypeToken<List<PaymentType>>() {
            }.getType();
            paymentTypes =
                JsonUtil.getInstance().getGson().fromJson(getIntent().getStringExtra("paymentTypes"), listType);
        } catch (Exception ex) {
            paymentTypes = null;
        }
        CardInfo cardInfo = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("cardInfo"), CardInfo.class);
        mPresenter.setPaymentMethodList(paymentMethods);
        mPresenter.setPaymentTypesList(paymentTypes);
        mPresenter.setCardInfo(cardInfo);
    }

    public void analizeLowRes() {
        if (mPresenter.isCardInfoAvailable()) {
            mLowResActive = ScaleUtil.isLowRes(this);
        } else {
            mLowResActive = true;
        }
    }

    public void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    @Override
    public void onValidStart() {
        mPresenter.initializePaymentMethod();
        initializeViews();
        loadViews();
        showTimer();
        initializeAdapter();
        mPresenter.loadPaymentTypes();
        trackScreen(Session.getSession(this).getConfigurationModule().getPaymentSettings().getPublicKey());
    }

    protected void trackScreen(final String publicKey) {
        MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, publicKey)
            .setVersion(BuildConfig.VERSION_NAME)
            .build();

        final ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.SCREEN_ID_PAYMENT_TYPES)
            .setScreenName(TrackingUtil.SCREEN_NAME_PAYMENT_TYPES)
            .build();
        mpTrackingContext.trackEvent(event);
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void onInvalidStart(String message) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    private void setContentViewLowRes() {
        setContentView(R.layout.px_activity_payment_types_lowres);
    }

    private void setContentViewNormal() {
        setContentView(R.layout.px_activity_payment_types_normal);
    }

    private void initializeViews() {
        mPaymentTypesRecyclerView = findViewById(R.id.mpsdkActivityPaymentTypesRecyclerView);
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        if (mLowResActive) {
            mLowResToolbar = findViewById(R.id.mpsdkRegularToolbar);
            mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mCollapsingToolbar = findViewById(R.id.mpsdkCollapsingToolbar);
            mAppBar = findViewById(R.id.mpsdkPaymentTypesAppBar);
            mCardContainer = findViewById(R.id.mpsdkActivityCardContainer);
            mNormalToolbar = findViewById(R.id.mpsdkRegularToolbar);
            mNormalToolbar.setVisibility(View.VISIBLE);
        }
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout.setVisibility(View.GONE);
    }

    private void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    private void initializeAdapter() {
        mPaymentTypesAdapter = new PaymentTypesAdapter(getDpadSelectionCallback());
        initializeAdapterListener(mPaymentTypesAdapter, mPaymentTypesRecyclerView);
    }

    protected OnSelectedCallback<Integer> getDpadSelectionCallback() {
        return new OnSelectedCallback<Integer>() {
            @Override
            public void onSelected(Integer position) {
                mPresenter.onItemSelected(position);
            }
        };
    }

    private void initializeAdapterListener(RecyclerView.Adapter adapter, RecyclerView view) {
        view.setAdapter(adapter);
        view.setLayoutManager(new LinearLayoutManager(this));
        view.addOnItemTouchListener(new RecyclerItemClickListener(this,
            new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    mPresenter.onItemSelected(position);
                }
            }));
    }

    @Override
    public void initializePaymentTypes(List<PaymentType> paymentTypes) {
        mPaymentTypesAdapter.addResults(paymentTypes);
    }

    @Override
    public void showApiExceptionError(ApiException exception, String requestOrigin) {
        ApiUtil.showApiExceptionError(mActivity, exception, requestOrigin);
    }

    @Override
    public void startErrorView(String message, String errorDetail) {
        ErrorUtil.startErrorActivity(mActivity, message, errorDetail, false);
    }

    private void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.px_payment_types_title));
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mLowResTitleToolbar.setTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.px_payment_types_title));
        setCustomFontNormal();
        mFrontCardView = new FrontCardView(mActivity, CardRepresentationModes.SHOW_FULL_FRONT_ONLY);
        mFrontCardView.setSize(CardRepresentationModes.MEDIUM_SIZE);
        mFrontCardView.setPaymentMethod(mPresenter.getPaymentMethod());
        if (mPresenter.isCardInfoAvailable()) {
            mFrontCardView.setCardNumberLength(mPresenter.getCardInfo().getCardNumberLength());
            mFrontCardView.setLastFourDigits(mPresenter.getCardInfo().getLastFourDigits());
        }
        mFrontCardView.inflateInParent(mCardContainer, true);
        mFrontCardView.initializeControls();
        mFrontCardView.draw();
        mFrontCardView.enableEditingCardNumber();
    }

    private void setCustomFontNormal() {
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mCollapsingToolbar.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            mCollapsingToolbar.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void loadToolbarArrow(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    public void showLoadingView() {
        mPaymentTypesRecyclerView.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopLoadingView() {
        mPaymentTypesRecyclerView.setVisibility(View.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void finishWithResult(PaymentType paymentType) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentType", JsonUtil.getInstance().toJson(paymentType));
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.px_hold, R.anim.px_hold);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("backButtonPressed", true);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPresenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        }
    }
}
