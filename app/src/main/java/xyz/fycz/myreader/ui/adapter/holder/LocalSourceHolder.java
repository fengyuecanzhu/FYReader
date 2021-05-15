package xyz.fycz.myreader.ui.adapter.holder;

import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;

import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.greendao.DbManager;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.util.ToastUtils;

/**
 * @author fengyue
 * @date 2021/2/10 18:28
 */
public class LocalSourceHolder extends ViewHolderImpl<BookSource> {
    private HashMap<BookSource, Boolean> mCheckMap;
    private CheckBox cbSource;
    private TextView tvEnable;
    private TextView tvDisable;
    private TextView tvCheck;

    public LocalSourceHolder(HashMap<BookSource, Boolean> mCheckMap) {
        this.mCheckMap = mCheckMap;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_local_source;
    }

    @Override
    public void initView() {
        cbSource = findById(R.id.cb_source);
        tvEnable = findById(R.id.tv_enable);
        tvDisable = findById(R.id.tv_disable);
        tvCheck = findById(R.id.tv_check);
    }

    @Override
    public void onBind(BookSource data, int pos) {
        banOrUse(data);
        cbSource.setChecked(mCheckMap.get(data));
        tvEnable.setOnClickListener(v -> {
            data.setEnable(true);
            banOrUse(data);
            DbManager.getDaoSession().getBookSourceDao().insertOrReplace(data);
        });
        tvDisable.setOnClickListener(v -> {
            data.setEnable(false);
            banOrUse(data);
            DbManager.getDaoSession().getBookSourceDao().insertOrReplace(data);
        });
        tvCheck.setOnClickListener(v -> ToastUtils.showInfo("校验功能即将上线"));
    }

    private void banOrUse(BookSource data) {
        if (data.getEnable()) {
            cbSource.setTextColor(getContext().getResources().getColor(R.color.textPrimary));
            cbSource.setText(data.getSourceName());
        } else {
            cbSource.setTextColor(getContext().getResources().getColor(R.color.textSecondary));
            cbSource.setText(String.format("(禁用中)%s", data.getSourceName()));
        }
    }
}
