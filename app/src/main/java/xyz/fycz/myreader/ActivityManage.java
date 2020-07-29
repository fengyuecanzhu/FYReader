package xyz.fycz.myreader;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by zhao on 2016/4/16.
 */
public class ActivityManage {

    private static ArrayList<AppCompatActivity> activities = new ArrayList<AppCompatActivity>();

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



}
