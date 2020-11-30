package xyz.fycz.myreader.ui.adapter;

import android.app.Activity;
import android.text.TextUtils;

import xyz.fycz.myreader.application.MyApplication;
import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConcurrentMultiValueMap;
import xyz.fycz.myreader.ui.adapter.holder.SearchBookHolder;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/10/2 10:08
 */
public class SearchBookAdapter extends BaseListAdapter<SearchBookBean> {
    private ConcurrentMultiValueMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;

    public SearchBookAdapter(ConcurrentMultiValueMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine) {
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
    }

    public SearchBookAdapter(ConcurrentMultiValueMap<SearchBookBean, Book> mBooks) {
        this.mBooks = mBooks;
    }

    @Override
    protected IViewHolder<SearchBookBean> createViewHolder(int viewType) {
        return new SearchBookHolder(mBooks, searchEngine);
    }

    public synchronized void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = mList;
        List<SearchBookBean> filterDataS = new ArrayList<>();

        switch (SysManager.getSetting().getSearchFilter()) {
            case 0:
                filterDataS.addAll(newDataS);
                break;
            case 1:
            default:
                for (SearchBookBean ssb : newDataS) {
                    if (StringUtils.isContainEachOther(ssb.getName(), keyWord) ||
                            StringUtils.isContainEachOther(ssb.getAuthor(), keyWord)) {
                        filterDataS.add(ssb);
                    }
                }
                break;
            case 2:
                for (SearchBookBean ssb : newDataS) {
                    if (StringUtils.isEqual(ssb.getName(), keyWord) ||
                            StringUtils.isEqual(ssb.getAuthor(), keyWord)) {
                        filterDataS.add(ssb);
                    }
                }
                break;
        }

        if (filterDataS != null && filterDataS.size() > 0) {
            List<SearchBookBean> searchBookBeansAdd = new ArrayList<>();
            if (copyDataS.size() == 0) {
                copyDataS.addAll(filterDataS);
            } else {
                //存在
                for (SearchBookBean temp : filterDataS) {
                    boolean hasSame = false;
                    for (int i = 0, size = copyDataS.size(); i < size; i++) {
                        SearchBookBean searchBook = copyDataS.get(i);
                        if (TextUtils.equals(temp.getName(), searchBook.getName())
                                && TextUtils.equals(temp.getAuthor(), searchBook.getAuthor())) {
                            hasSame = true;
                            break;
                        }
                    }

                    if (!hasSame) {
                        searchBookBeansAdd.add(temp);
                    }
                }
                //添加
                for (SearchBookBean temp : searchBookBeansAdd) {
                    if (TextUtils.equals(keyWord, temp.getName())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else if (TextUtils.equals(keyWord, temp.getAuthor())) {
                        for (int i = 0; i < copyDataS.size(); i++) {
                            SearchBookBean searchBook = copyDataS.get(i);
                            if (!TextUtils.equals(keyWord, searchBook.getName()) && !TextUtils.equals(keyWord, searchBook.getAuthor())) {
                                copyDataS.add(i, temp);
                                break;
                            }
                        }
                    } else {
                        copyDataS.add(temp);
                    }
                }
            }
            MyApplication.runOnUiThread(this::notifyDataSetChanged);
        }
    }
}
