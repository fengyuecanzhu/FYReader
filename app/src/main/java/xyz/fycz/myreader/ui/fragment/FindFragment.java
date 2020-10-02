package xyz.fycz.myreader.ui.fragment;

import android.content.Intent;
import android.widget.RelativeLayout;
import butterknife.BindView;
import xyz.fycz.myreader.R;
import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.ui.activity.BookstoreActivity;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;
import xyz.fycz.myreader.webapi.crawler.read.QB5ReadCrawler;
import xyz.fycz.myreader.webapi.crawler.find.QiDianMobileRank;

/**
 * @author fengyue
 * @date 2020/9/13 21:07
 */
public class FindFragment extends BaseFragment {
    @BindView(R.id.find_rl_qidian_top)
    RelativeLayout mRlQiDianTop;
    @BindView(R.id.find_rl_qidian_ns_top)
    RelativeLayout mRlQiDianNSTop;
    @BindView(R.id.find_rl_qidian_sort)
    RelativeLayout mRlQiDianSort;
    @BindView(R.id.find_rl_qidian_ns_sort)
    RelativeLayout mRlQiDianNSSort;
    @BindView(R.id.find_rl_qb5_store)
    RelativeLayout mRlQB5Store;
    @BindView(R.id.find_rl_biquge_store)
    RelativeLayout mRlBiQuGeStore;

    @Override
    protected int getContentId() {
        return R.layout.fragment_find;
    }

    @Override
    protected void initClick() {
        super.initClick();
        mRlQiDianTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false)));
        mRlQiDianNSTop.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true)));
        mRlQiDianSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(false, true)));
        mRlQiDianNSSort.setOnClickListener(v -> comeToBookstore(new QiDianMobileRank(true, true)));
        mRlQB5Store.setOnClickListener(v -> comeToBookstore(new QB5ReadCrawler()));
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
