package xyz.fycz.myreader.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.databinding.FragmentFindBinding;
import xyz.fycz.myreader.entity.Quotation;
import xyz.fycz.myreader.ui.activity.BookstoreActivity;
import xyz.fycz.myreader.util.ToastUtils;
import xyz.fycz.myreader.util.utils.GsonExtensionsKt;
import xyz.fycz.myreader.util.utils.ImageLoader;
import xyz.fycz.myreader.util.utils.OkHttpUtils;
import xyz.fycz.myreader.util.utils.RxUtils;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.Ben100FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.MiaoBiFindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QB5FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;

/**
 * @author fengyue
 * @date 2020/9/13 21:07
 */
public class FindFragment extends BaseFragment {

    private FragmentFindBinding binding;

    @Override
    protected View bindView(LayoutInflater inflater, ViewGroup container) {
        binding = FragmentFindBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    protected void initWidget(Bundle savedInstanceState) {
        getQuotation();
    }

    @Override
    protected void initClick() {
        super.initClick();
        binding.findRlQuotation.setOnClickListener(v -> getQuotation());
        binding.findRlQidianTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false)));
        binding.findRlQidianNsTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true)));
        //binding.findRlXs7Top.setOnClickListener(v -> comeToBookstore(new XS7Rank()));
        binding.findRlQidianSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false, true)));
        binding.findRlQidianNsSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true, true)));
        binding.findRlQb5Store.setOnClickListener(v -> comeToBookstore(new QB5FindCrawler()));
        binding.findRlBen100Store.setOnClickListener(v -> comeToBookstore(new Ben100FindCrawler()));
        binding.findRlMiaoquStore.setOnClickListener(v -> comeToBookstore(new MiaoBiFindCrawler()));
        //binding.findRlXs7Store.setOnClickListener(v -> comeToBookstore(new XS7ReadCrawler()));
    }


    private void comeToBookstore(FindCrawler findCrawler) {
        Intent intent = new Intent(getContext(), BookstoreActivity.class);
        intent.putExtra(APPCONST.FIND_CRAWLER, findCrawler);
        startActivity(intent);
    }

    private void getQuotation() {
        Single.create((SingleOnSubscribe<Quotation>) emitter -> {
            String json = OkHttpUtils.getHtml(URLCONST.QUOTATION);
            emitter.onSuccess(GsonExtensionsKt.getGSON().fromJson(json, Quotation.class));
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<Quotation>() {
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
