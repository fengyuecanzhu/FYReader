package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentFindBinding;
import xyz.fycz.myreader.ui.activity.BookstoreActivity;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;
import xyz.fycz.myreader.webapi.crawler.find.XS7Rank;
import xyz.fycz.myreader.webapi.crawler.read.Ben100ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.MiaoBiReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.QB5ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.XS7ReadCrawler;

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
    protected void initClick() {
        super.initClick();
        binding.findRlQidianTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false)));
        binding.findRlQidianNsTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true)));
        //binding.findRlXs7Top.setOnClickListener(v -> comeToBookstore(new XS7Rank()));
        binding.findRlQidianSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false, true)));
        binding.findRlQidianNsSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true, true)));
        binding.findRlQb5Store.setOnClickListener(v -> comeToBookstore(new QB5ReadCrawler()));
        binding.findRlBen100Store.setOnClickListener(v -> comeToBookstore(new Ben100ReadCrawler()));
        binding.findRlMiaoquStore.setOnClickListener(v -> comeToBookstore(new MiaoBiReadCrawler()));
        //binding.findRlXs7Store.setOnClickListener(v -> comeToBookstore(new XS7ReadCrawler()));
    }


    private void comeToBookstore(FindCrawler findCrawler){
        Intent intent = new Intent(getContext(), BookstoreActivity.class);
        intent.putExtra(APPCONST.FIND_CRAWLER, findCrawler);
        startActivity(intent);
    }

    public boolean isRecreate() {
        return binding == null;
    }
}
