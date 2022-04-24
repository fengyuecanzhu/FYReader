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

package xyz.fycz.myreader.ui.adapter;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.base.adapter.BaseListAdapter;
import xyz.fycz.myreader.base.adapter.IViewHolder;
import xyz.fycz.myreader.entity.SearchBookBean;
import xyz.fycz.myreader.greendao.entity.Book;
import xyz.fycz.myreader.model.SearchEngine;
import xyz.fycz.myreader.model.mulvalmap.ConMVMap;
import xyz.fycz.myreader.ui.adapter.holder.SearchBookHolder;
import xyz.fycz.myreader.util.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author fengyue
 * @date 2020/10/2 10:08
 */
public class SearchBookAdapter extends BaseListAdapter<SearchBookBean> {
    private Activity activity;
    private ConMVMap<SearchBookBean, Book> mBooks;
    private SearchEngine searchEngine;
    private String keyWord;
    private Handler handler = new Handler(Looper.getMainLooper());
    private long postTime = 0L;
    private Runnable sendRunnable = this::upAdapter;

    public SearchBookAdapter(Activity activity, ConMVMap<SearchBookBean, Book> mBooks, SearchEngine searchEngine, String keyWord) {
        this.activity = activity;
        this.mBooks = mBooks;
        this.searchEngine = searchEngine;
        this.keyWord = keyWord;
    }

    @Override
    protected IViewHolder<SearchBookBean> createViewHolder(int viewType) {
        return new SearchBookHolder(activity, mBooks, searchEngine, keyWord);
    }

    public void addAll(List<SearchBookBean> newDataS, String keyWord) {
        List<SearchBookBean> copyDataS = new ArrayList<>(getItems());
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

        if (filterDataS.size() > 0) {
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
            mList = copyDataS;
            upAdapter();
            /*synchronized (this) {
                App.runOnUiThread(() -> {
                    mList = copyDataS;
                    notifyDataSetChanged();
                });
            }*/
        }
    }

    private synchronized void upAdapter(){
        if (System.currentTimeMillis() >= postTime + 500) {
            handler.removeCallbacks(sendRunnable);
            postTime = System.currentTimeMillis();
            notifyDataSetChanged();
        } else {
            handler.removeCallbacks(sendRunnable);
            handler.postDelayed(sendRunnable, 500 - System.currentTimeMillis() + postTime);
        }
    }

    private void sort(List<SearchBookBean> bookBeans) {
        //排序，基于最符合关键字的搜书结果应该是最短的
        //TODO ;这里只做了简单的比较排序，还需要继续完善
        Collections.sort(bookBeans, (o1, o2) -> {
            if (o1.getName().equals(keyWord))
                return -1;
            if (o2.getName().equals(keyWord))
                return 1;
            if (o1.getAuthor() != null && o1.getAuthor().equals(keyWord))
                return -1;
            if (o2.getAuthor() != null && o2.getAuthor().equals(keyWord))
                return 1;
            return Integer.compare(o1.getName().length(), o2.getName().length());
        });
    }

    private int getAddIndex(List<SearchBookBean> beans, SearchBookBean bean) {
        int maxWeight = 0;
        int index = -1;
        if (TextUtils.equals(keyWord, bean.getName())) {
            maxWeight = 5;
        }else if (TextUtils.equals(keyWord, bean.getAuthor())) {
            maxWeight = 3;
        }
        for (int i = 0; i < beans.size(); i++) {
            SearchBookBean searchBook = beans.get(i);
            int weight = 0;
            if (TextUtils.equals(bean.getName(), searchBook.getName())) {
                weight = 4;
            } else if (TextUtils.equals(bean.getAuthor(), searchBook.getAuthor())) {
                weight = 2;
            } else if (bean.getName().length() <= searchBook.getName().length()) {
                weight = 1;
            }
            if (weight > maxWeight) {
                index = i;
                maxWeight = weight;
            }
        }
        if (maxWeight == 5 || maxWeight == 3) index = 0;
        return index;
    }
}
