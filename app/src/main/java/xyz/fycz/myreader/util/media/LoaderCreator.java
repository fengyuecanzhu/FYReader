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

package xyz.fycz.myreader.util.media;

import android.content.Context;
import android.os.Bundle;
import androidx.loader.content.CursorLoader;

/**
 * Created by newbiechen on 2018/1/14.
 */

public class LoaderCreator {
    public static final int ALL_BOOK_FILE = 1;

    public static CursorLoader create(Context context, int id, Bundle bundle) {
        LocalFileLoader loader = null;
        switch (id){
            case ALL_BOOK_FILE:
                loader = new LocalFileLoader(context);
                break;
            default:
                loader = null;
                break;
        }
        if (loader != null) {
            return loader;
        }

        throw new IllegalArgumentException("The id of Loader is invalid!");
    }
}
