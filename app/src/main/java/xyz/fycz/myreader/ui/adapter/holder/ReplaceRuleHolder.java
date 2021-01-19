package xyz.fycz.myreader.ui.adapter.holder;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.adapter.ViewHolderImpl;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.model.ReplaceRuleManager;
import xyz.fycz.myreader.ui.adapter.ReplaceRuleAdapter;
import xyz.fycz.myreader.ui.dialog.ReplaceDialog;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;

/**
 * @author fengyue
 * @date 2021/1/19 9:54
 */
public class ReplaceRuleHolder extends ViewHolderImpl<ReplaceRuleBean> {
    private RelativeLayout rlContent;
    private TextView tvRuleSummary;
    private Button btBan;
    private Button btShare;
    private Button btDelete;
    private AppCompatActivity activity;
    private ReplaceRuleAdapter.OnDeleteListener onDeleteListener;

    public ReplaceRuleHolder(AppCompatActivity activity, ReplaceRuleAdapter.OnDeleteListener onDeleteListener) {
        this.activity = activity;
        this.onDeleteListener = onDeleteListener;
    }


    @Override
    protected int getItemLayoutId() {
        return R.layout.item_replace_rule;
    }

    @Override
    public void initView() {
        rlContent = findById(R.id.rl_content);
        tvRuleSummary = findById(R.id.tv_rule_summary);
        btBan = findById(R.id.bt_ban);
        btShare = findById(R.id.bt_share);
        btDelete = findById(R.id.btnDelete);
    }

    @Override
    public void onBind(ReplaceRuleBean data, int pos) {
        banOrUse(data);

        rlContent.setOnClickListener(v -> {
            ReplaceDialog replaceDialog = new ReplaceDialog(activity, data,
                    () -> {
                        banOrUse(data);
                        ToastUtils.showSuccess("内容替换规则修改成功！");
                        refreshUI();
                    });
            replaceDialog.show(activity.getSupportFragmentManager(), "");
        });

        btBan.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            data.setEnable(!data.getEnable());
            ReplaceRuleManager.saveData(data)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                banOrUse(data);
                                refreshUI();
                            }
                        }
                    });
        });
        btShare.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            List<ReplaceRuleBean> shareRuleBean = new ArrayList<>();
            shareRuleBean.add(data);
            ShareUtils.share(activity, GsonExtensionsKt.getGSON().toJson(shareRuleBean));
        });
        btDelete.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                ReplaceRuleManager.delData(data);
                e.onNext(true);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            onDeleteListener.success(pos, data);
                            refreshUI();
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showError("删除失败");
                        }
                    });

        });
    }

    private void banOrUse(ReplaceRuleBean data){
        if (data.getEnable()) {
            tvRuleSummary.setTextColor(getContext().getResources().getColor(R.color.textPrimary));
            if (StringHelper.isEmpty(data.getReplaceSummary())) {
                tvRuleSummary.setText(String.format("%s->%s", data.getRegex(), data.getReplacement()));
            }else {
                tvRuleSummary.setText(data.getReplaceSummary());
            }
            btBan.setText(getContext().getString(R.string.ban));
        } else {
            tvRuleSummary.setTextColor(getContext().getResources().getColor(R.color.textSecondary));
            if (StringHelper.isEmpty(data.getReplaceSummary())) {
                tvRuleSummary.setText(String.format("(禁用中)%s->%s", data.getRegex(), data.getReplacement()));
            }else {
                tvRuleSummary.setText(String.format("(禁用中)%s", data.getReplaceSummary()));
            }
            btBan.setText(R.string.enable_use);
        }
    }

    private void refreshUI(){
        Intent result = new Intent();
        result.putExtra(APPCONST.RESULT_NEED_REFRESH, true);
        activity.setResult(AppCompatActivity.RESULT_OK, result);
    }
}
