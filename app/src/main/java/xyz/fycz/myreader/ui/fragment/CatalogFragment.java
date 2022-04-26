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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import xyz.fycz.myreader.databinding.FragmentCatalogBinding;
import xyz.fycz.myreader.ui.presenter.CatalogPresenter;

/**
 * A simple {@link Fragment} subclass.
 */
public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;

    private CatalogPresenter mCatalogPresent;

    public CatalogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        mCatalogPresent = new CatalogPresenter(this);
        mCatalogPresent.start();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public RelativeLayout getRlCatalog() {
        return binding.rlCatalog;
    }

    public ListView getLvChapterList() {
        return binding.lvChapterList;
    }

    public FloatingActionButton getFcChangeSort() {
        return binding.changeSort;
    }

    public CatalogPresenter getmCatalogPresent() {
        return mCatalogPresent;
    }

    public ProgressBar getPbLoading() {
        return binding.pbLoading;
    }
}
