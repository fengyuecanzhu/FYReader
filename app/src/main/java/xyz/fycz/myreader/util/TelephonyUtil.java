package xyz.fycz.myreader.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;



public class TelephonyUtil {


    public static String getNum1(Context context){
        String tel;
        String IMSI;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceid = tm.getDeviceId();
            tel = tm.getLine1Number();//手机号码
            Class clazz = tm.getClass();
            Method getPhoneNumber = clazz.getDeclaredMethod("getLine1NumberForSubscriber",int.class);
            String tel0 = (String)getPhoneNumber.invoke(tm, 0);
            String tel1 = (String)getPhoneNumber.invoke(tm, 1);

            IMSI = tm.getSubscriberId();
            ToastUtils.showInfo(IMSI);
        }catch (Exception e){
            e.printStackTrace();
            tel = "";
        }


        return tel;
    }

    public static String getNum2(Context context){
        String tel;
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String deviceid = tm.getDeviceId();
            tel = tm.getLine1Number();//手机号码

        }catch (Exception e){
            e.printStackTrace();
            tel = "";
        }

        return tel;
    }

}
