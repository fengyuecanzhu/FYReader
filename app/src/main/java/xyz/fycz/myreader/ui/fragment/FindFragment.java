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
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.FragmentFindBinding;
import xyz.fycz.myreader.entity.Quotation;
import xyz.fycz.myreader.greendao.entity.rule.BookSource;
import xyz.fycz.myreader.model.sourceAnalyzer.BookSourceManager;
import xyz.fycz.myreader.ui.activity.FindBookActivity;
import xyz.fycz.myreader.ui.adapter.holder.FindSourceHolder;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.MiaoBiGeFindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QB5FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianFindCrawler;
import xyz.fycz.myreader.widget.DividerItemDecoration;

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
            List<BookSource> sources = BookSourceManager.getEnabledBookSourceByOrderNum();
            for (BookSource source : sources){
                if (source.getFindRule() != null && !TextUtils.isEmpty(source.getFindRule().getUrl())){
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
        binding.rvFindSources.addItemDecoration(new DividerItemDecoration(getContext()));

        findSourcesAdapter.setOnItemClickListener((view, pos) -> {
            Intent intent = new Intent(getContext(), FindBookActivity.class);
            BitIntentDataManager.getInstance().putData(intent, findSourcesAdapter.getItem(pos));
            getContext().startActivity(intent);
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
        binding.findRlQidian.setOnClickListener(v -> comeToBookstore(new QiDianFindCrawler(false)));
        binding.findRlQidianNs.setOnClickListener(v -> comeToBookstore(new QiDianFindCrawler(true)));
        binding.findRlMiaoquStore.setOnClickListener(v -> comeToBookstore(new MiaoBiGeFindCrawler()));
        binding.findRlQb5Store.setOnClickListener(v -> comeToBookstore(new QB5FindCrawler()));
    }


    private void comeToBookstore(FindCrawler findCrawler) {
        Intent intent = new Intent(getContext(), FindBookActivity.class);
        BitIntentDataManager.getInstance().putData(intent, findCrawler);
        startActivity(intent);
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

    public boolean isRecreate() {
        return binding == null;
    }
}
