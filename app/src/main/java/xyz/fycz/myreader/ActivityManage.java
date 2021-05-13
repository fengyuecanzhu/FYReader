package xyz.fycz.myreader;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ActivityManage {

    private static ArrayList<AppCompatActivity> activities = new ArrayList<>();

    public static void addActivity(AppCompatActivity activity){
        activities.add(activity);
    }

    public static void removeActivity(AppCompatActivity activity ){
        activities.remove(activity);
    }

    public static void finishAllActivites() {
        for (AppCompatActivity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static AppCompatActivity getActivityByCurrenlyRun(){
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

}
