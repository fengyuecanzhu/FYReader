/*
 * This file is part of FYReader.
 *  FYReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FYReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.adapter.holder;

import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

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
    public void onBind(RecyclerView.ViewHolder holder, BookSource data, int pos) {
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
