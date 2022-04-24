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

package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.base.observer.MyObserver;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.FragmentFindBinding;
import xyz.fycz.myreader.entity.Quotation;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.activity.FindBookActivity;
import xyz.fycz.myreader.ui.activity.SourceEditActivity;
import xyz.fycz.myreader.ui.adapter.holder.FindSourceHolder;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;

/**
 * @author fengyue
 * @date 2020/9/13 21:07
 */
public class FindFragment extends BaseFragment {

    private FragmentFindBinding binding;
    private BaseListAdapter<BookSource> findSourcesAdapter;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentFindBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Observable.create((ObservableOnSubscribe<List<BookSource>>) emitter -> {
            List<BookSource> findSources = new ArrayList<>();
            initLocalFind(findSources);
            List<BookSource> sources = BookSourceManager.getEnabledBookSourceByOrderNum();
            for (BookSource source : sources) {
                if (source.getFindRule() != null && !TextUtils.isEmpty(source.getFindRule().getUrl())) {
                    findSources.add(source);
                }
            }
            emitter.onNext(findSources);
            emitter.onComplete();
        }).compose(RxUtils::toSimpleSingle).subscribe(new MyObserver<List<BookSource>>() {
            @Override
            public void onSubscribe(Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull List<BookSource> findSources) {
                initFindSources(findSources);
            }
        });
    }

    private void initLocalFind(List<BookSource> findSources) {
        BookSource source1 = new BookSource();
        source1.setSourceName("起点中文网");
        source1.setSourceUrl("xyz.fycz.myreader.webapi.crawler.find.QiDianFindCrawler");
        BookSource source2 = new BookSource();
        source2.setSourceName("起点女生网");
        source2.setSourceUrl("xyz.fycz.myreader.webapi.crawler.find.QianDianNSFindCrawler");
        BookSource source3 = new BookSource();
        source3.setSourceName("妙笔阁");
        source3.setSourceUrl("xyz.fycz.myreader.webapi.crawler.find.MiaoBiGeFindCrawler");
        BookSource source4 = new BookSource();
        source4.setSourceName("全本小说");
        source4.setSourceUrl("xyz.fycz.myreader.webapi.crawler.find.QB5FindCrawler");
        findSources.add(source1);
        findSources.add(source2);
        findSources.add(source3);
        findSources.add(source4);
    }

    private void initFindSources(List<BookSource> findSources) {
        findSourcesAdapter = new BaseListAdapter<BookSource>() {
            @Override
            protected IViewHolder<BookSource> createViewHolder(int viewType) {
                return new FindSourceHolder();
            }
        };
        binding.rvFindSources.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFindSources.setAdapter(findSourcesAdapter);
        //设置分割线
//        binding.rvFindSources.addItemDecoration(new DividerItemDecoration(getContext()));

        findSourcesAdapter.setOnItemClickListener((view, pos) -> {
            Intent intent = new Intent(getContext(), FindBookActivity.class);
            BookSource source = findSourcesAdapter.getItem(pos);
            if (source.getFindRule() == null) {
                FindCrawler findCrawler = null;
                try {
                    findCrawler = (FindCrawler) Class.forName(source.getSourceUrl()).newInstance();
                } catch (IllegalAccessException | java.lang.InstantiationException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                BitIntentDataManager.getInstance().putData(intent, findCrawler);
            } else {
                BitIntentDataManager.getInstance().putData(intent, source);
            }
            getContext().startActivity(intent);
        });

        findSourcesAdapter.setOnItemLongClickListener((view, pos) -> {
            BookSource source = findSourcesAdapter.getItem(pos);
            if (source.getFindRule() == null) {
                ToastUtils.showWarring("内置发现无法编辑");
            } else {
                Intent intent = new Intent(getContext(), SourceEditActivity.class);
                intent.putExtra(APPCONST.BOOK_SOURCE, findSourcesAdapter.getItem(pos));
                getContext().startActivity(intent);
            }
            return true;
        });
        findSourcesAdapter.refreshItems(findSources);
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        getQuotation();
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.findRlQuotation.setOnClickListener(v -> getQuotation());
    }

    private void getQuotation() {
        Single.create((SingleOnSubscribe<Quotation>) emitter -> {
            String json = OkHttpUtils.getHtml(URLCONST.QUOTATION);
            emitter.onSuccess(GsonExtensionsKt.getGSON().fromJson(json, Quotation.class));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<Quotation>() {
            @Override
            public void onSubscribe(Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onSuccess(@NotNull Quotation q) {
                binding.tvQuotation.setText(q.getHitokoto());
                binding.tvFrom.setText(String.format("--- %s", q.getFrom()));
            }
        });
    }

    public void refreshFind() {
        initData(null);
    }

    public boolean isRecreate() {
        return binding == null;
    }
}
