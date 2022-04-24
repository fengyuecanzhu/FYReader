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

package xyz.fycz.myreader;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

import xyz.fycz.myreader.base.BaseActivity;

public class ActivityManage {

    private static ArrayList<AppCompatActivity> activities = new ArrayList<>();

    public static int mResumeActivityCount = 0;

    public static void addActivity(AppCompatActivity activity){
        activities.add(activity);
    }

    public static void removeActivity(AppCompatActivity activity ){
        activities.remove(activity);
    }

    public static void finishAllActivities() {
        for (AppCompatActivity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static AppCompatActivity getActivityByCurrentlyRun(){
        if(activities.size() <= 0){
            return null;
        }
        return activities.get(activities.size() - 1);
    }

    /**
     * 判断指定Activity是否存在
     */
    public static Boolean isExist(Class<?> activityClass) {
        boolean result = false;
        for (AppCompatActivity item : activities) {
            if (null != item && item.getClass() == activityClass) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static AppCompatActivity getByClass(Class<?> activity){
        for (AppCompatActivity item : activities) {
            if (null != item && item.getClass() == activity) {
                return item;
            }
        }
        return null;
    }
}
