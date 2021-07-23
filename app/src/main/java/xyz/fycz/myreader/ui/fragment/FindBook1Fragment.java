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
import xyz.fycz.myreader.base.LazyFragment;
import xyz.fycz.myreader.databinding.FragmentFindBook1Binding;
import xyz.fycz.myreader.entity.FindKind;
import xyz.fycz.myreader.ui.adapter.TabFragmentPageAdapter;
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

    public FindBook1Fragment(List<FindKind> kinds, FindCrawler findCrawler) {
        this.kinds = kinds;
        this.findCrawler = findCrawler;
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
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        binding = FragmentFindBook1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
