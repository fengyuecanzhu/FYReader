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

package xyz.fycz.dynamic;

import android.content.pm.ApplicationInfo;

/**
 * @author fengyue
 * @date 2022/3/29 11:31
 */
public class AppParam {
    /** The name of the package being loaded. */
    public String packageName;

    /** The ClassLoader used for this package. */
    public ClassLoader classLoader;

    /** More information about the application being loaded. */
    public ApplicationInfo appInfo;
}
