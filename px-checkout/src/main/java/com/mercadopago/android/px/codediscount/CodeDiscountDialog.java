package com.mercadopago.android.px.codediscount;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadolibre.android.ui.widgets.MeliDialog;
import com.mercadolibre.android.ui.widgets.MeliSpinner;
import com.mercadolibre.android.ui.widgets.TextField;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.callbacks.OnViewUpdated;
import com.mercadopago.android.px.components.Action;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.services.util.TextUtil;
import com.mercadopago.android.px.util.FlipModalAnimationUtil;

import static android.graphics.Typeface.NORMAL;
import static com.mercadolibre.android.ui.widgets.MeliButton.State.DISABLED;

public class CodeDiscountDialog extends MeliDialog implements View.OnClickListener, CodeDiscountView {

    private static final String TAG = CodeDiscountDialog.class.getName();

    private TextField input;
    private MeliSpinner progress;
    private MeliButton confirmButton;
    private ViewGroup container;
    private View inputLayout;

    private CodeDiscountPresenter presenter;

    protected OnDiscountRetrieved onDiscountRetrieved;
    protected OnViewUpdated onViewUpdated;

    public interface OnDiscountRetrieved {
        void onDiscountRetrieved(OnViewUpdated onViewUpdated);
    }

    public static void showDialog(@NonNull final FragmentManager supportFragmentManager) {
        final DialogFragment codeDiscountDialog = new CodeDiscountDialog();
        codeDiscountDialog.show(supportFragmentManager, TAG);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        container = view.findViewById(R.id.ui_melidialog_content_container);
        inputLayout = container.findViewById(R.id.input_view);
        confirmButton = inputLayout.findViewById(R.id.confirm_button);
        input = inputLayout.findViewById(R.id.text_field);
        progress = inputLayout.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        confirmButton.setOnClickListener(this);
        final Session session = Session.getSession(view.getContext());
        presenter = new CodeDiscountPresenter(session.getDiscountRepository(), session.getAmountRepository());
        presenter.attachView(this);
    }

    private boolean isValidInput() {
        return !TextUtil.isEmpty(input.getText());
    }

    private void showLoading() {
        confirmButton.setState(DISABLED);
        input.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public int getContentView() {
        return R.layout.px_dialog_code_discount;
    }

    @Override
    public void onClick(final View v) {
        if (isValidInput()) {
            showLoading();
            presenter.getDiscountForCode(input.getText());
        } else {
            processCodeError();
        }
    }

    public void processError(@NonNull final String errorMessage) {
        confirmButton.setState(NORMAL);
        input.setEnabled(true);
        progress.setVisibility(View.GONE);
        input.setError(errorMessage);
    }

    @Override
    public void onAttach(final Context context) {
        onDiscountRetrieved = (OnDiscountRetrieved) context;
        onViewUpdated = new OnViewUpdated() {
            @Override
            public void onSuccess(@NonNull final Discount discount) {
                final View back = new CongratsCodeDiscount(new CongratsCodeDiscount.Props(discount), new Actions()).render(container);
                FlipModalAnimationUtil.flipView(container, inputLayout, back);
            }

            @Override
            public void onFailure() {
                processError(getString(R.string.px_error_something_went_wrong_try_again));
                //TODO add retry
            }
        };
        super.onAttach(context);
    }

    @Override
    public void processCodeError() {
        processError(getString(R.string.px_discount_error_check_this_data));
    }

    @Override
    public void onDetach() {
        onDiscountRetrieved = null;
        super.onDetach();
    }

    @Override
    public void processSuccess(@NonNull final Discount discount) {
        if (onDiscountRetrieved != null) {
            onDiscountRetrieved.onDiscountRetrieved(onViewUpdated);
        }
    }

    @Override
    public void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    public class Actions {
        public void onButtonClicked() {
            dismiss();
        }
    }
}
