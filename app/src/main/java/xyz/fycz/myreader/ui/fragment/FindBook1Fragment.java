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

package xyz.fycz.myreader.ui.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import xyz.fycz.myreader.base.BaseFragment;
import xyz.fycz.myreader.base.BitIntentDataManager;
import xyz.fycz.myreader.base.LazyFragment;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.databinding.FragmentFindBook1Binding;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.ui.adapter.TabFragmentPageAdapter;
import xyz.fycz.myreader.util.help.StringHelper;
import xyz.fycz.myreader.webapi.crawler.base.FindCrawler;

/**
 * @author fengyue
 * @date 2021/7/21 23:06
 */
public class FindBook1Fragment extends LazyFragment {
    private FragmentFindBook1Binding binding;
    private List<FindKind> kinds;
    private FindCrawler findCrawler;
    private PopupMenu kindMenu;
    private static final String KINDS = "FindBook1FragmentKinds";
    private static final String FIND_CRAWLER = "FindBook1FragmentFindCrawler";

    public FindBook1Fragment() {
    }

    public FindBook1Fragment(List<FindKind> kinds, FindCrawler findCrawler) {
        this.kinds = kinds;
        this.findCrawler = findCrawler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String dataKey = savedInstanceState.getString(APPCONST.DATA_KEY);
            kinds = (List<FindKind>) BitIntentDataManager.getInstance().getData(KINDS + dataKey);
            findCrawler = (FindCrawler) BitIntentDataManager.getInstance().getData(FIND_CRAWLER + dataKey);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        String dataKey = StringHelper.getStringRandom(25);
        BitIntentDataManager.getInstance().putData(KINDS + dataKey, kinds);
        BitIntentDataManager.getInstance().putData(FIND_CRAWLER + dataKey, findCrawler);
        outState.putString(APPCONST.DATA_KEY, dataKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void lazyInit() {
        kindMenu = new PopupMenu(getContext(), binding.ivMenu, Gravity.END);
        TabFragmentPageAdapter adapter = new TabFragmentPageAdapter(getChildFragmentManager());
        for (int i = 0, kindsSize = kinds.size(); i < kindsSize; i++) {
            FindKind kind = kinds.get(i);
            adapter.addFragment(new FindBook2Fragment(kind, findCrawler), kind.getName());
            kindMenu.getMenu().add(0, 0, i, kind.getName());
        }
        binding.tabVp.setAdapter(adapter);
        binding.tabVp.setOffscreenPageLimit(3);
        binding.tabTlIndicator.setUpWithViewPager(binding.tabVp);
        kindMenu.setOnMenuItemClickListener(item -> {
            binding.tabTlIndicator.setCurrentItem(item.getOrder(), true);
            return true;
        });
        binding.ivMenu.setOnClickListener(v -> kindMenu.show());
    }

    @Nullable
    @Override
    public View bindView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        binding = FragmentFindBook1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
