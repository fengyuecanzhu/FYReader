package xyz.fycz.myreader;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.File;
import java.util.List;

import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.util.utils.GsonUtils;
import xyz.fycz.myreader.webapi.CommonApi;
import xyz.fycz.myreader.webapi.ResultCallback;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("xyz.fycz.myreader", appContext.getPackageName());
    }

    @Test
    public void test() {
        String json = "[{\"icon\":\"apk\",\"t\":0,\"id\":\"i6b1horcnde\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.4-\\u53bb\\u66f4\\u65b0\\u3001\\u5e7f\\u544a\\u7248.apk\",\"size\":\"10.2 M\",\"time\":\"5 \\u5929\\u524d\",\"duan\":\"iorcnd\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i0Jokohm6gf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.4.apk\",\"size\":\"14.9 M\",\"time\":\"12 \\u5929\\u524d\",\"duan\":\"iohm6g\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ivyijmwfdqd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.3.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-13\",\"duan\":\"imwfdq\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iMpIkmcv9dc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.2.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-02\",\"duan\":\"imcv9d\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ijJsnmcfwja\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.1.apk\",\"size\":\"10.0 M\",\"time\":\"2021-03-01\",\"duan\":\"imcfwj\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ixI19lqnscf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.9.0-beta.apk\",\"size\":\"10.0 M\",\"time\":\"2021-02-16\",\"duan\":\"ilqnsc\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iEy9kldwrpi\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.2.apk\",\"size\":\"9.6 M\",\"time\":\"2021-02-06\",\"duan\":\"ildwrp\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i5vu3l6pcuj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.1.apk\",\"size\":\"9.6 M\",\"time\":\"2021-02-01\",\"duan\":\"il6pcu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i4pMekkx9zc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.8.0.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkx9z\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iu2FYkkvjyj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.9.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkvjy\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"icrU3kkqrud\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.9.apk\",\"size\":\"9.7 M\",\"time\":\"2021-01-19\",\"duan\":\"ikkqru\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ieSvwk7vucf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.8.apk\",\"size\":\"9.6 M\",\"time\":\"2021-01-09\",\"duan\":\"ik7vuc\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGTvFk7ugmf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.7.8.apk\",\"size\":\"9.6 M\",\"time\":\"2021-01-09\",\"duan\":\"ik7ugm\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iwIrojatw9e\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.121218.apk\",\"size\":\"9.5 M\",\"time\":\"2020-12-12\",\"duan\":\"ijatw9\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i9W2Gj3mera\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3mer\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iNFGZj3m4xg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3m4x\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iqvwqj3kr5e\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.120612.apk\",\"size\":\"9.8 M\",\"time\":\"2020-12-06\",\"duan\":\"ij3kr5\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ib8ZJivckmd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112822.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-28\",\"duan\":\"iivckm\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iPzSWipk1wb\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112410.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-24\",\"duan\":\"iipk1w\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"itfplio73ob\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112309.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-23\",\"duan\":\"iio73o\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ikhkGinehwj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112216.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iinehw\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iB8Xsin8tgj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112213.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin8tg\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iJ9qwin5qeb\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112212.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin5qe\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iHgbpin5b3a\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.112212.apk\",\"size\":\"9.1 M\",\"time\":\"2020-11-22\",\"duan\":\"iin5b3\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iowiDiecicj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111420.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-14\",\"duan\":\"iiecic\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGu0Mibqpni\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111217.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iibqpn\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i3wzXib1eaj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111209.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iib1ea\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iVjH6ib15hc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111208.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-12\",\"duan\":\"iib15h\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iEAaUiamvub\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.111121.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-11\",\"duan\":\"iiamvu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"inaPWi6fy6j\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.110811.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-08\",\"duan\":\"ii6fy6\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iGMHPi0c7pc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.110311.apk\",\"size\":\"8.0 M\",\"time\":\"2020-11-03\",\"duan\":\"ii0c7p\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iDjPghnbxod\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.102217.apk\",\"size\":\"8.2 M\",\"time\":\"2020-10-22\",\"duan\":\"ihnbxo\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iTmLPh4t2wj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100316.apk\",\"size\":\"7.9 M\",\"time\":\"2020-10-03\",\"duan\":\"ih4t2w\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"icVyfh4lnsd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100313.apk\",\"size\":\"7.9 M\",\"time\":\"2020-10-03\",\"duan\":\"ih4lns\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iBofFh42pxg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100220.apk\",\"size\":\"7.2 M\",\"time\":\"2020-10-02\",\"duan\":\"ih42px\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iG2tPh3akej\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.100123.apk\",\"size\":\"7.2 M\",\"time\":\"2020-10-01\",\"duan\":\"ih3ake\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iMIWSgz5eaf\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.092718.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-27\",\"duan\":\"igz5ea\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"itWVmgxz3ob\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.092617.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-26\",\"duan\":\"igxz3o\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iYYzdgrtkda\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.091922.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-19\",\"duan\":\"igrtkd\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"ivi6Sgrrj2b\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.091921.apk\",\"size\":\"7.2 M\",\"time\":\"2020-09-19\",\"duan\":\"igrrj2\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iviXVgfksuj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.090722.apk\",\"size\":\"7.0 M\",\"time\":\"2020-09-07\",\"duan\":\"igfksu\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i1HYEg0q8je\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.082422.apk\",\"size\":\"6.9 M\",\"time\":\"2020-08-24\",\"duan\":\"ig0q8j\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"i2m6vfnvvfa\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.081523.apk\",\"size\":\"6.8 M\",\"time\":\"2020-08-15\",\"duan\":\"ifnvvf\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iUQ0Zfka1bc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.081221.apk\",\"size\":\"6.8 M\",\"time\":\"2020-08-12\",\"duan\":\"ifka1b\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"igzNbfdbc2d\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.080709.apk\",\"size\":\"6.7 M\",\"time\":\"2020-08-07\",\"duan\":\"ifdbc2\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iHXgWf67zqj\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.073120.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-31\",\"duan\":\"if67zq\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iXaHUf5uyyd\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.073114.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-31\",\"duan\":\"if5uyy\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"il7HPezefjc\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072519.apk\",\"size\":\"6.7 M\",\"time\":\"2020-07-25\",\"duan\":\"iezefj\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iUmNAevkyhg\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072221.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-22\",\"duan\":\"ievkyh\",\"p_ico\":0},{\"icon\":\"apk\",\"t\":0,\"id\":\"iviG9evcn5a\",\"name_all\":\"\\u98ce\\u6708\\u8bfb\\u4e66v1.20.072217.apk\",\"size\":\"6.2 M\",\"time\":\"2020-07-22\",\"duan\":\"ievcn5\",\"p_ico\":0}]";
        List<File1> files =  GsonUtils.parseJArray(json, File1.class);
        for (File1 file : files){
            CommonApi.getUrl(URLCONST.LAN_ZOUS_URL + "/" + file.getId(), new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    String downloadUrl = (String) o;
                    startDownloading(downloadUrl, file.getName_all());
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }
    private void startDownloading(String decodeUrl, String name) {
        String path = APPCONST.UPDATE_APK_FILE_DIR + name;
        BaseDownloadTask baseDownloadTask = FileDownloader.getImpl()
                .create(decodeUrl)
                .setPath(path, new File(path).isDirectory())
                .setCallbackProgressMinInterval(100)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.e("downloadApk" + name, "pending-------");
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        float percent = 1f * soFarBytes / totalBytes * 100;
                        Log.e("downloadApk" + name, "progress-------" + percent);
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        Log.e("downloadApk" + name, "paused-------");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.e("downloadApk" + name, "completed-------");
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Log.e("downloadApk" + name, "error-------" + e.toString());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                        Log.e("downloadApk" + name, "warn-------");
                    }
                });
        baseDownloadTask.setAutoRetryTimes(3);
        baseDownloadTask.start();
    }
}