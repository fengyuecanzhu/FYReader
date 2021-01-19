package xyz.fycz.myreader.ui.adapter;

import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.greendao.entity.ReplaceRuleBean;
import xyz.fycz.myreader.ui.adapter.holder.ReplaceRuleHolder;

/**
 * @author fengyue
 * @date 2021/1/19 9:51
 */
public class ReplaceRuleAdapter extends BaseListAdapter<ReplaceRuleBean> {
    private AppCompatActivity activity;
    private OnDeleteListener onDeleteListener;

    public ReplaceRuleAdapter(AppCompatActivity activity, OnDeleteListener onDeleteListener) {
        this.activity = activity;
        this.onDeleteListener = onDeleteListener;
    }

    @Override
    protected IViewHolder<ReplaceRuleBean> createViewHolder(int viewType) {
        return new ReplaceRuleHolder(activity, onDeleteListener);
    }

    public void removeItem2(ReplaceRuleBean ruleBean){
        mList.remove(ruleBean);
    }

    public interface OnDeleteListener{
        void success(int which, ReplaceRuleBean ruleBean);
    }
}
