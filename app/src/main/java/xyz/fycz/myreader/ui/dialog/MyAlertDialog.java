package xyz.fycz.myreader.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputLayout;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.util.StringHelper;

/**
 * @author fengyue
 * @date 2020/9/20 9:48
 */
public class MyAlertDialog {
    public static AlertDialog.Builder build(Context context) {
        return new AlertDialog.Builder(context, R.style.alertDialogTheme);
    }

    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                      Integer inputType, boolean cancelable, int maxLen, onInputChangeListener oic,
                                      DialogInterface.OnClickListener posListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.edit_dialog, null);
        TextInputLayout textInputLayout = view.findViewById(R.id.text_input_lay);

        textInputLayout.setCounterMaxLength(maxLen);
        EditText editText = textInputLayout.getEditText();
        editText.setHint(hint);
        if (inputType != null) editText.setInputType(inputType);
        if (!StringHelper.isEmpty(initText)) editText.setText(initText);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        MyApplication.getHandler().postDelayed(() -> imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED), 220);
        AlertDialog inputDia = build(context)
                .setTitle(title)
                .setView(view)
                .setCancelable(cancelable)
                .setPositiveButton("确认", (dialog, which) -> {
                    posListener.onClick(dialog, which);
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                })
                .show();
        Button posBtn = inputDia.getButton(AlertDialog.BUTTON_POSITIVE);
        posBtn.setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editText.getText().toString();
                if (editText.getText().length() > 0 && editText.getText().length() <= maxLen && !text.equals(initText)) {
                    posBtn.setEnabled(true);
                } else {
                    posBtn.setEnabled(false);
                }
                oic.onChange(text);
            }
        });
        return inputDia;
    }
    public static AlertDialog createInputDia(Context context, String title, String hint, String initText,
                                             boolean cancelable, int maxLen, onInputChangeListener oic,
                                             DialogInterface.OnClickListener posListener) {
        return createInputDia(context, title, hint, initText, InputType.TYPE_CLASS_TEXT, cancelable, maxLen, oic, posListener);
    }

    public interface onInputChangeListener{
        void onChange(String text);
    }
}
