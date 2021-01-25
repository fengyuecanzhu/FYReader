package xyz.fycz.myreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import xyz.fycz.myreader.databinding.DialogCopyContentBinding;

/**
 * Created by Zhouas666 on 2019-04-14
 * Github: https://github.com/zas023
 * <p>
 * 自由复制dialog
 */

public class CopyContentDialog extends Dialog {

    private static final String TAG = "CopyContentDialog";

    private DialogCopyContentBinding binding;

    private String content;

    /***************************************************************************/

    public CopyContentDialog(@NonNull Context context, String content) {
        super(context);
        this.content = content;
    }

    /*****************************Initialization********************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogCopyContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpWindow();

        binding.dialogTvContent.setText(content);
    }

    /**
     * 设置Dialog显示的位置
     */
    private void setUpWindow() {
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        window.setAttributes(lp);
    }

}
