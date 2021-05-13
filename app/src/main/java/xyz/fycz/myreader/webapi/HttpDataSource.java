package xyz.fycz.myreader.webapi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import xyz.fycz.myreader.webapi.callback.JsonCallback;
import xyz.fycz.myreader.common.APPCONST;
import xyz.fycz.myreader.common.URLCONST;
import xyz.fycz.myreader.entity.JsonModel;
import xyz.fycz.myreader.util.HttpUtil;
import xyz.fycz.myreader.util.RSAUtilV2;
import xyz.fycz.myreader.util.StringHelper;
import com.google.gson.Gson;
import xyz.fycz.myreader.webapi.callback.HttpCallback;
import xyz.fycz.myreader.webapi.callback.ResultCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;


public class HttpDataSource {

    /**
     * http请求 (get) ps:获取html
     * @param url
     * @param callback
     */
    public static void httpGet_html(String url, final String charsetName,  boolean isRefresh, final ResultCallback callback){
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest_okHttp(url, isRefresh, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response.toString());
                       callback.onFinish(response.toString(),0);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {
                Log.d("Local", "read finish：" + response);
                callback.onFinish(response, 0);
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });
    }



    /**
     * http请求 (post) 获取蓝奏云直链
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final ResultCallback callback, final String referer) {
        Log.d("HttpPost:", url + "&" + output);
        HttpUtil.sendPostRequest(url, output, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }
            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        callback.onFinish(response.toString(), 1);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {
                Log.e("http", response);
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        }, referer);
    }

}
