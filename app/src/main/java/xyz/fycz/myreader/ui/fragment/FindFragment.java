package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.widget.RelativeLayout;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.BookstoreActivity;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.find.XS7Rank;
import xyz.fycz.myreader.webapi.crawler.read.Ben100ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.MiaoBiReadCrawler;
import xyz.fycz.myreader.webapi.crawler.read.QB5ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;
import xyz.fycz.myreader.webapi.crawler.read.XS7ReadCrawler;

/**
 * @author fengyue
 * @date 2020/9/13 21:07
 */
public class FindFragment extends BaseFragment {
    @BindView(R.id.find_rl_qidian_top)
    RelativeLayout mRlQiDianTop;
    @BindView(R.id.find_rl_qidian_ns_top)
    RelativeLayout mRlQiDianNSTop;
    @BindView(R.id.find_rl_xs7_top)
    RelativeLayout mRlXS7Top;
    @BindView(R.id.find_rl_qidian_sort)
    RelativeLayout mRlQiDianSort;
    @BindView(R.id.find_rl_qidian_ns_sort)
    RelativeLayout mRlQiDianNSSort;
    @BindView(R.id.find_rl_qb5_store)
    RelativeLayout mRlQB5Store;
    @BindView(R.id.find_rl_ben100_store)
    RelativeLayout mRlBen100Store;
    @BindView(R.id.find_rl_miaoqu_store)
    RelativeLayout mRlMiaoQuStore;
    @BindView(R.id.find_rl_xs7_store)
    RelativeLayout mRlXS7Store;

    @Override
    protected int getContentId() {
        return R.layout.fragment_find;
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlQiDianTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false)));
        mRlQiDianNSTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true)));
        mRlXS7Top.setOnClickListener(v -> comeToBookstore(new XS7Rank()));
        mRlQiDianSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false, true)));
        mRlQiDianNSSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true, true)));
        mRlQB5Store.setOnClickListener(v -> comeToBookstore(new QB5ReadCrawler()));
        mRlBen100Store.setOnClickListener(v -> comeToBookstore(new Ben100ReadCrawler()));
        mRlMiaoQuStore.setOnClickListener(v -> comeToBookstore(new MiaoBiReadCrawler()));
        mRlXS7Store.setOnClickListener(v -> comeToBookstore(new XS7ReadCrawler()));
    }


    private void comeToBookstore(FindCrawler findCrawler){
        Intent intent = new Intent(getContext(), BookstoreActivity.class);
        intent.putExtra(APPCONST.FIND_CRAWLER, findCrawler);
        startActivity(intent);
    }

    public boolean isRecreate() {
        return unbinder == null;
    }
}
