/*
 * This file is part of FYReader.
 * FYReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FYReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FYReader.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 - 2022 fengyuecanzhu
 */

package xyz.fycz.myreader.ui.adapter.holder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

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
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.activity.SourceEditActivity;
import xyz.fycz.myreader.ui.adapter.BookSourceAdapter;
import xyz.fycz.myreader.ui.adapter.helper.IItemTouchHelperViewHolder;
import xyz.fycz.myreader.ui.adapter.helper.OnStartDragListener;
import xyz.fycz.myreader.util.ShareUtils;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.widget.swipemenu.SwipeMenuLayout;

/**
 * @author fengyue
 * @date 2021/2/10 16:52
 */
public class BookSourceHolder extends ViewHolderImpl<BookSource> implements IItemTouchHelperViewHolder {
    private FragmentActivity activity;
    private BookSourceAdapter adapter;
    private HashMap<BookSource, Boolean> mCheckMap;
    private BookSourceAdapter.OnSwipeListener onSwipeListener;
    private SwipeMenuLayout layout;
    private RelativeLayout rlContent;
    private CheckBox cbSourceSelect;
    private TextView tvSourceName;
    private ImageView ivSwipeLeft;
    private ImageView ivMove;
    private Button btTop;
    private Button btBan;
    private Button btShare;
    private Button btDelete;

    private OnStartDragListener onStartDragListener;

    public BookSourceHolder(FragmentActivity activity, BookSourceAdapter adapter,
                            BookSourceAdapter.OnSwipeListener onSwipeListener,
                            OnStartDragListener onStartDragListener) {
        this.activity = activity;
        this.adapter = adapter;
        this.onSwipeListener = onSwipeListener;
        mCheckMap = adapter.getCheckMap();
        this.onStartDragListener = onStartDragListener;
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_book_source;
    }

    @Override
    public void initView() {
        layout = (SwipeMenuLayout) getItemView();
        rlContent = findById(R.id.rl_content);
        cbSourceSelect = findById(R.id.cb_source_select);
        tvSourceName = findById(R.id.tv_source_name);
        ivSwipeLeft = findById(R.id.iv_swipe_left);
        ivMove = findById(R.id.iv_move);
        btTop = findById(R.id.bt_top);
        btBan = findById(R.id.bt_ban);
        btShare = findById(R.id.bt_share);
        btDelete = findById(R.id.btnDelete);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBind(RecyclerView.ViewHolder holder, BookSource data, int pos) {
        banOrUse(data);
        initClick(data, pos);
        layout.setSwipeEnable(!adapter.ismEditState());
        if (adapter.ismEditState()) {
            cbSourceSelect.setVisibility(View.VISIBLE);
            ivSwipeLeft.setVisibility(View.GONE);
            ivMove.setVisibility(View.VISIBLE);
        } else {
            cbSourceSelect.setVisibility(View.GONE);
            ivSwipeLeft.setVisibility(View.VISIBLE);
            ivMove.setVisibility(View.GONE);
        }
        cbSourceSelect.setChecked(mCheckMap.get(data));
        ivMove.setOnTouchListener((view, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        //通知ItemTouchHelper开始拖拽
                        if (onStartDragListener != null) {
                            onStartDragListener.onStartDrag(holder);
                        }
                    }
                    return false;
                }
        );
    }

    private void initClick(BookSource data, int pos) {
        rlContent.setOnClickListener(v -> {
            if (adapter.ismEditState()) {
                adapter.setCheckedItem(pos);
            } else {
                Intent intent = new Intent(activity, SourceEditActivity.class);
                intent.putExtra(APPCONST.BOOK_SOURCE, data);
                activity.getSupportFragmentManager().getFragments().get(1)
                        .startActivityForResult(intent, APPCONST.REQUEST_EDIT_BOOK_SOURCE);
            }
        });
        btTop.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            BookSourceManager.toTop(data)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                onSwipeListener.onTop(pos, data);
                            }
                        }
                    });
        });

        btBan.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            data.setEnable(!data.getEnable());
            BookSourceManager.saveData(data)
                    .subscribe(new MySingleObserver<Boolean>() {
                        @Override
                        public void onSuccess(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                banOrUse(data);
                            }
                        }
                    });
        });

        btShare.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            ShareUtils.share(activity, GsonExtensionsKt.getGSON().toJson(data));
        });

        btDelete.setOnClickListener(v -> {
            ((SwipeMenuLayout) getItemView()).smoothClose();
            Observable.create((ObservableOnSubscribe<Boolean>) e -> {
                BookSourceManager.removeBookSource(data);
                e.onNext(true);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            onSwipeListener.onDel(pos, data);
                        }

                        @Override
                        public void onError(Throwable e) {
                            ToastUtils.showError("删除失败");
                        }
                    });

        });
    }

    private void banOrUse(BookSource data) {
        if (data.getEnable()) {
            tvSourceName.setTextColor(getContext().getResources().getColor(R.color.textPrimary));
            if (!StringHelper.isEmpty(data.getSourceGroup())) {
                tvSourceName.setText(String.format("%s [%s]", data.getSourceName(), data.getSourceGroup()));
            } else {
                tvSourceName.setText(data.getSourceName());
            }
            btBan.setText(getContext().getString(R.string.ban));
        } else {
            tvSourceName.setTextColor(getContext().getResources().getColor(R.color.textSecondary));
            if (!StringHelper.isEmpty(data.getSourceGroup())) {
                tvSourceName.setText(String.format("(禁用中)%s [%s]", data.getSourceName(), data.getSourceGroup()));
            } else {
                tvSourceName.setText(String.format("(禁用中)%s", data.getSourceName()));
            }
            btBan.setText(R.string.enable_use);
        }
    }

    @Override
    public void onItemSelected(RecyclerView.ViewHolder viewHolder) {
        getItemView().setTranslationZ(10);
    }

    @Override
    public void onItemClear(RecyclerView.ViewHolder viewHolder) {
        getItemView().setTranslationZ(0);
    }
}
