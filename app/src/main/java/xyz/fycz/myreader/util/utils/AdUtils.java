package xyz.fycz.myreader.util.utils;

import android.util.Log;

import com.weaction.ddsdk.base.DdSdkHelper;
import com.weaction.ddsdk.bean.DDSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import xyz.fycz.myreader.application.App;
import xyz.fycz.myreader.base.observer.MySingleObserver;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.AdBean;
import xyz.fycz.myreader.model.user.UserService;
import xyz.fycz.myreader.util.SharedPreUtils;
import xyz.fycz.myreader.util.help.DateHelper;

/**
 * @author fengyue
 * @date 2021/4/22 19:00
 */
public class AdUtils {
    public static final String TAG = AdUtils.class.getSimpleName();
    private static boolean hasInitAd = false;
    private static AdBean adConfig;

    static {
        String config = SharedPreUtils.getInstance(true).getString("adConfig");
        adConfig = GsonExtensionsKt.getGSON().fromJson(config, AdBean.class);
        if (adConfig == null || adConfig.getBackAdTime() == 0) {
            adConfig = new AdBean(false, 20, 60);
        }
    }

    public static Single<Boolean> checkHasAd() {
        return Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            boolean hasAd = false;
            if (!adConfig.isCloud()) {
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                String body = "type=adConfig" + UserService.INSTANCE.makeAuth();
                RequestBody requestBody = RequestBody.create(mediaType, body);
                String jsonStr = OkHttpUtils.getHtml(URLCONST.AD_URL, requestBody, "UTF-8");
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    int code = jsonObject.getInt("code");
                    if (code > 200) {
                        Log.e(TAG, "checkHasAd-->errorCode：" + code);
                        if (code == 213) {
                            hasAd = true;
                        }
                    } else {
                        String res = jsonObject.getString("result");
                        SharedPreUtils.getInstance(true).putString("adConfig", res);
                        adConfig = GsonExtensionsKt.getGSON().fromJson(res, AdBean.class);
                        adConfig.setCloud(true);
                        hasAd = adConfig.isHasAd();
                    }
                    Log.i(TAG, "hasAd：" + hasAd);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                hasAd = adConfig.isHasAd();
            }
            emitter.onSuccess(hasAd);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static void adRecord(String type, String name) {
        Single.create((SingleOnSubscribe<Boolean>) emitter -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String body = "adType=" + type + "&type=" + name + UserService.INSTANCE.makeAuth();
            RequestBody requestBody = RequestBody.create(mediaType, body);
            OkHttpUtils.getHtml(URLCONST.AD_URL, requestBody, "UTF-8");
            emitter.onSuccess(true);
        }).compose(RxUtils::toSimpleSingle).subscribe(new MySingleObserver<Boolean>() {
            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                Log.i(TAG, name + "上报成功");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, name + "上报失败\n" + e.getLocalizedMessage());
            }
        });
    }

    public static Single<int[]> adTimes() {
        return Single.create((SingleOnSubscribe<int[]>) emitter -> {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String body = "type=adTimes" + UserService.INSTANCE.makeAuth();
            RequestBody requestBody = RequestBody.create(mediaType, body);
            String jsonStr = OkHttpUtils.getHtml(URLCONST.AD_URL, requestBody, "UTF-8");
            JSONObject jsonObject = new JSONObject(jsonStr);
            int[] adTimes = new int[]{-1, 3, 5};
            try {
                int code = jsonObject.getInt("code");
                JSONArray adTimesArr = jsonObject.getJSONArray("result");
                Log.i(TAG, "adTimesArr：" + adTimesArr.toString());
                if (code > 200) {
                    Log.e(TAG, "adTimes-->errorCode：" + code);
                    if (code == 213) {
                        adTimes = new int[]{-1};
                    }
                } else {
                    adTimes = new int[adTimesArr.length()];
                    for (int i = 0; i < adTimesArr.length(); i++) {
                        adTimes[i] = adTimesArr.getInt(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            emitter.onSuccess(adTimes);
        }).compose(RxUtils::toSimpleSingle);
    }

    public static boolean checkTodayShowAd() {
        SharedPreUtils spu = SharedPreUtils.getInstance();
        String splashAdCount = spu.getString("splashAdCount");
        boolean bookDetailAd = spu.getBoolean("bookDetailAd", true);
        int adTimes = spu.getInt("curAdTimes", 3);
        String[] splashAdCounts = splashAdCount.split(":");
        String today = DateHelper.getYearMonthDay1();
        int todayAdCount;
        if (today.equals(splashAdCounts[0])) {
            todayAdCount = Integer.parseInt(splashAdCounts[1]);
        } else {
            todayAdCount = 0;
        }
        return adTimes < 0 || todayAdCount < adTimes || bookDetailAd;
    }

    public static void backTime() {
        SharedPreUtils.getInstance(true).putLong("backTime", System.currentTimeMillis());
    }

    public static boolean backSplashAd() {
        if (!adConfig.isHasAd()) return false;
        SharedPreUtils sp = SharedPreUtils.getInstance(true);
        Long splashAdTime = sp.getLong("splashAdTime");
        Long backTime = sp.getLong("backTime");
        Long currentTime = System.currentTimeMillis();
        return currentTime - splashAdTime >= adConfig.getIntervalAdTime() * 60L * 1000 ||
                currentTime - backTime >= adConfig.getBackAdTime() * 60L * 1000;
    }

    public static AdBean getAdConfig() {
        return adConfig;
    }

    public static void initAd() {
        /*if (!hasInitAd) {
            hasInitAd = true;
            DdSdkHelper.init(new DDSDK.Builder()
                    .setUserId("1234")
                    .setAppId("216")
                    .setAppKey("51716a16fbdf50905704b6575b1b3b60")
                    .setCsjAppId("5273043")
                    .setApp(App.getApplication())
                    .setShowLog(App.isDebug())
                    .create()
            );
        }*/
        DdSdkHelper.init(new DDSDK.Builder()
                .setUserId("1234")
                .setAppId("216")
                .setAppKey("51716a16fbdf50905704b6575b1b3b60")
                .setCsjAppId("5273043")
                .setApp(App.getApplication())
                .setShowLog(App.isDebug())
                .setCustomRequestPermission(true)
                .create()
        );
    }
}
