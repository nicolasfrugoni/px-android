package com.mercadopago.android.px;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mercadopago.android.px.callbacks.card.CardSecurityCodeEditTextCallback;
import com.mercadopago.android.px.controllers.CheckoutTimer;
import com.mercadopago.android.px.customviews.MPEditText;
import com.mercadopago.android.px.customviews.MPTextView;
import com.mercadopago.android.px.exceptions.ExceptionHandler;
import com.mercadopago.android.px.exceptions.MercadoPagoError;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.listeners.card.CardSecurityCodeTextWatcher;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.presenters.SecurityCodePresenter;
import com.mercadopago.android.px.providers.SecurityCodeProviderImpl;
import com.mercadopago.android.px.services.exceptions.ApiException;
import com.mercadopago.android.px.services.exceptions.CardTokenException;
import com.mercadopago.android.px.tracker.FlowHandler;
import com.mercadopago.android.px.tracker.MPTrackingContext;
import com.mercadopago.android.px.tracking.model.ScreenViewEvent;
import com.mercadopago.android.px.tracking.utils.TrackingUtil;
import com.mercadopago.android.px.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.uicontrollers.card.CardView;
import com.mercadopago.android.px.util.ApiUtil;
import com.mercadopago.android.px.util.ErrorUtil;
import com.mercadopago.android.px.util.JsonUtil;
import com.mercadopago.android.px.util.MPCardUIUtils;
import com.mercadopago.android.px.views.SecurityCodeActivityView;

/**
 * Created by vaserber on 10/26/16.
 */

public class SecurityCodeActivity extends MercadoPagoBaseActivity implements SecurityCodeActivityView {

    private static final String PRESENTER_BUNDLE = "mSecurityCodePresenter";
    private static final String REASON_BUNDLE = "mReason";

    protected SecurityCodePresenter mSecurityCodePresenter;

    public static final String ERROR_STATE = "textview_error";
    public static final String NORMAL_STATE = "textview_normal";
    protected String mReason;

    //View controls
    protected ViewGroup mProgressLayout;
    protected MPEditText mSecurityCodeEditText;
    protected FrameLayout mNextButton;
    protected FrameLayout mBackButton;
    protected MPTextView mNextButtonText;
    protected MPTextView mBackButtonText;
    protected LinearLayout mButtonContainer;
    protected FrameLayout mErrorContainer;
    protected MPTextView mErrorTextView;
    protected String mErrorState;
    protected FrameLayout mBackground;
    protected ImageView mSecurityCodeCardIcon;
    protected Toolbar mToolbar;

    //Normal View
    protected FrameLayout mCardContainer;
    protected CardView mCardView;
    protected MPTextView mTimerTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            createPresenter();
            getActivityParameters();
            configurePresenter();
            setContentView();
            mSecurityCodePresenter.initialize();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PRESENTER_BUNDLE, JsonUtil.getInstance().toJson(mSecurityCodePresenter));
        outState.putString(REASON_BUNDLE, mReason);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mSecurityCodePresenter = JsonUtil.getInstance()
                .fromJson(savedInstanceState.getString(PRESENTER_BUNDLE), SecurityCodePresenter.class);
            mReason = savedInstanceState.getString(REASON_BUNDLE);

            configurePresenter();
            setContentView();
            mSecurityCodePresenter.initialize();
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ApiUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    private void createPresenter() {
        if (mSecurityCodePresenter == null) {
            mSecurityCodePresenter = new SecurityCodePresenter();
        }
    }

    private void configurePresenter() {
        if (mSecurityCodePresenter != null) {
            mSecurityCodePresenter.attachView(this);
            SecurityCodeProviderImpl provider =
                new SecurityCodeProviderImpl(this);
            mSecurityCodePresenter.attachResourcesProvider(provider);
        }
    }

    private void getActivityParameters() {
        mReason = getIntent().getStringExtra("reason");

        CardInfo cardInfo = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("cardInfo"), CardInfo.class);
        Card card = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("card"), Card.class);
        Token token = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("token"), Token.class);
        PaymentMethod paymentMethod =
            JsonUtil.getInstance().fromJson(getIntent().getStringExtra("paymentMethod"), PaymentMethod.class);
        PaymentRecovery paymentRecovery =
            JsonUtil.getInstance().fromJson(getIntent().getStringExtra("paymentRecovery"), PaymentRecovery.class);

        mSecurityCodePresenter.setToken(token);
        mSecurityCodePresenter.setCard(card);
        mSecurityCodePresenter.setPaymentMethod(paymentMethod);
        mSecurityCodePresenter.setCardInfo(cardInfo);
        mSecurityCodePresenter.setPaymentRecovery(paymentRecovery);
    }

    public void setContentView() {
        setContentView(R.layout.px_activity_security_code);
    }

    private void initializeControls() {
        initializeToolbar();

        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        mSecurityCodeEditText = findViewById(R.id.mpsdkCardSecurityCode);
        mNextButton = findViewById(R.id.mpsdkNextButton);
        mBackButton = findViewById(R.id.mpsdkBackButton);
        mNextButtonText = findViewById(R.id.mpsdkNextButtonText);
        mBackButtonText = findViewById(R.id.mpsdkBackButtonText);
        mButtonContainer = findViewById(R.id.mpsdkButtonContainer);
        mErrorContainer = findViewById(R.id.mpsdkErrorContainer);
        mErrorTextView = findViewById(R.id.mpsdkErrorTextView);
        mBackground = findViewById(R.id.mpsdkSecurityCodeActivityBackground);
        mCardContainer = findViewById(R.id.mpsdkCardViewContainer);
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout.setVisibility(View.GONE);
        mSecurityCodeCardIcon = findViewById(R.id.mpsdkSecurityCodeCardIcon);

        setListeners();
    }

    private void initializeToolbar() {
        mToolbar = findViewById(R.id.mpsdkToolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        final Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
        overrideTransitionOut();
    }

    private void setListeners() {
        setSecurityCodeListeners();
        setButtonsListeners();
    }

    @Override
    public void showLoadingView() {
        hideKeyboard();
        mProgressLayout.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void stopLoadingView() {
        mProgressLayout.setVisibility(View.GONE);
    }

    private void loadViews() {
        loadNormalViews();
    }

    private void loadNormalViews() {
        mCardView = new CardView(this);
        mCardView.setSize(CardRepresentationModes.BIG_SIZE);
        mCardView.inflateInParent(mCardContainer, true);
        mCardView.initializeControls();
        mCardView.setPaymentMethod(mSecurityCodePresenter.getPaymentMethod());
        mCardView.setSecurityCodeLength(mSecurityCodePresenter.getSecurityCodeLength());
        mCardView.setSecurityCodeLocation(mSecurityCodePresenter.getSecurityCodeLocation());
        mCardView.setCardNumberLength(mSecurityCodePresenter.getCardNumberLength());
        mCardView.setLastFourDigits(mSecurityCodePresenter.getCardInfo().getLastFourDigits());
        mCardView.draw(CardView.CARD_SIDE_FRONT);
        mCardView.drawFullCard();
        mCardView.drawEditingSecurityCode("");
        mSecurityCodePresenter.setSecurityCodeCardType();
    }

    private void setSecurityCodeCardColorFilter() {
        int color = MPCardUIUtils.getCardColor(mSecurityCodePresenter.getPaymentMethod(), this);
        mSecurityCodeCardIcon.setColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.DST_OVER);
    }

    @Override
    public void initialize() {
        initializeControls();
        mSecurityCodePresenter.initializeSettings();
        loadViews();
    }

    @Override
    public void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void trackScreen() {
        final String publicKey = Session.getSession(this).getConfigurationModule().getPaymentSettings().getPublicKey();
        final MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, publicKey)
            .setVersion(BuildConfig.VERSION_NAME)
            .build();

        ScreenViewEvent event = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(
                TrackingUtil.SCREEN_ID_CARD_FORM + mSecurityCodePresenter.getPaymentMethod().getPaymentTypeId() +
                    TrackingUtil.CARD_SECURITY_CODE_VIEW)
            .setScreenName(TrackingUtil.SCREEN_NAME_SECURITY_CODE)
            .addProperty(TrackingUtil.PROPERTY_SECURITY_CODE_REASON, mReason)
            .build();

        mpTrackingContext.trackEvent(event);
    }

    @Override
    public void showBackSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void showFrontSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_amex_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void setSecurityCodeInputMaxLength(int length) {
        setInputMaxLength(mSecurityCodeEditText, length);
    }

    private void setInputMaxLength(MPEditText text, int maxLength) {
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    private void setSecurityCodeListeners() {
        mSecurityCodeEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mSecurityCodeEditText, event);
                return true;
            }
        });
        mSecurityCodeEditText
            .addTextChangedListener(new CardSecurityCodeTextWatcher(new CardSecurityCodeEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mSecurityCodeEditText);
                }

                @Override
                public void saveSecurityCode(CharSequence s) {
                    mSecurityCodePresenter.saveSecurityCode(s.toString());
                    mCardView.setSecurityCodeLocation(mSecurityCodePresenter.getSecurityCodeLocation());
                    mCardView.drawEditingSecurityCode(s.toString());
                }

                @Override
                public void changeErrorView() {
                    clearErrorView();
                }

                @Override
                public void toggleLineColorOnError(boolean toggle) {
                    mSecurityCodeEditText.toggleLineColorOnError(toggle);
                }
            }));
        mSecurityCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
    }

    private boolean onNextKey(int actionId, KeyEvent event) {
        if (isNextKey(actionId, event)) {
            mSecurityCodePresenter.validateSecurityCodeInput();
            return true;
        }
        return false;
    }

    private boolean isNextKey(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
            (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    private void setButtonsListeners() {
        setNextButtonListeners();
        setBackButtonListeners();
    }

    private void setNextButtonListeners() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSecurityCodePresenter.validateSecurityCodeInput();
            }
        });
    }

    public void setBackButtonListeners() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setErrorState(String mErrorState) {
        this.mErrorState = mErrorState;
    }

    @Override
    public void setErrorView(CardTokenException exception) {
        mSecurityCodeEditText.toggleLineColorOnError(true);
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        String errorText = ExceptionHandler.getErrorMessage(this, exception);
        mErrorTextView.setText(errorText);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void showError(MercadoPagoError error, String requestOrigin) {
        if (error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void clearErrorView() {
        mSecurityCodeEditText.toggleLineColorOnError(false);
        mButtonContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        mErrorTextView.setText("");
        setErrorState(NORMAL_STATE);
    }

    private void onTouchEditText(MPEditText editText, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            openKeyboard(editText);
        }
    }

    private void openKeyboard(MPEditText ediText) {
        ediText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ediText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mSecurityCodePresenter.recoverFromFailure();
            } else {
                setResult(RESULT_CANCELED, data);
                finish();
            }
        }
    }

    @Override
    public void finishWithResult() {
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("token", JsonUtil.getInstance().toJson(mSecurityCodePresenter.getToken()));
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
