package xyz.fycz.myreader.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.fycz.myreader.R;

/**
 * Created by Zhouas666 on 2019-04-14
 * Github: https://github.com/zas023
 * <p>
 * 自由复制dialog
 */

public class CopyContentDialog extends Dialog {

    private static final String TAG = "CopyContentDialog";

    @BindView(R.id.dialog_tv_content)
    TextView dialogTvContent;

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
        setContentView(R.layout.dialog_copy_content);
        ButterKnife.bind(this);

        setUpWindow();

        dialogTvContent.setText(content);
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
