package xyz.fycz.myreader.source;

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

/**
 * Created by zhao on 2016/4/16.
 */

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
     * http请求 (get)
     * @param url
     * @param callback
     */
    public static void httpGet(String url, final JsonCallback callback) {
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest(url, new HttpCallback() {
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
                        Log.d("Http", "read finish：" + response.toString());
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
//                        jsonModel.setResult(jsonModel.getResult().replace("\n",""));
//                        test(jsonModel.getResult());
//                        String str = new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n",""),Base64.DEFAULT),APPCONST.privateKey));
                        if (URLCONST.isRSA && !StringHelper.isEmpty(jsonModel.getResult())) {
                            jsonModel.setResult(StringHelper.decode(new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n", ""), Base64.DEFAULT), APPCONST.privateKey))));
                        }
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {

            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });
    }
    public static void httpGet(String url, final ResultCallback callback) {
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest(url, new HttpCallback() {
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
                        Log.d("Http", "read finish：" + response.toString());
                        callback.onFinish(response.toString(), 1);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {

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
     * http请求 (post)
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final JsonCallback callback) {
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
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
                        if (URLCONST.isRSA && !StringHelper.isEmpty(jsonModel.getResult())) {
                            jsonModel.setResult(StringHelper.decode(new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n", ""), Base64.DEFAULT), APPCONST.privateKey))));
                        }
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
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
        });
    }

    /**
     * http请求 (post)
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final ResultCallback callback) {
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
                    e.printStackTrace();
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

    /**
     * http非加密请求（get）
     * @param url
     * @param callback
     */
    public static void httpGetNoRSA(String url, final JsonCallback callback) {
        Log.d("HttpGet URl", url);
        HttpUtil.sendGetRequest(url, new HttpCallback() {
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
                        Log.i("Http", "read finish：" + response.toString());
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onFinish(String response) {

            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }


    public static void httpGetBitmap(String url, final HttpCallback callback) {
        Log.d("Http", "success1");
        HttpUtil.sendBitmapGetRequest(url, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                if (callback != null) {
                    Bitmap bm = BitmapFactory.decodeStream(in);
//                    setBitmap(bm);
                    callback.onFinish(bm);
                }
            }

            @Override
            public void onFinish(String response) {

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
     * 多文件上传
     * @param url
     * @param files
     * @param params
     * @param callback
     */
    public static void uploadFile(String url, String[] files, Map<String, Object> params, final JsonCallback callback) {
        HttpUtil.uploadFileRequest(url, files, params, callback);
    }

    /**
     * 多文件上传2
     * @param url
     * @param files
     * @param params
     * @param callback
     */
    public static void uploadFile(String url, ArrayList<File> files, Map<String, Object> params, final JsonCallback callback) {
        String[] filePaths = new String[files.size()];
        int i = 0;
        for (File file : files) {
            filePaths[i++] = file.getAbsolutePath();
        }
        HttpUtil.uploadFileRequest(url, filePaths, params, callback);
    }


    /**
     * 多文件上传（okhttp）
     * @param url
     * @param files
     * @param params
     * @param callback
     */
    public static void uploadFile_okhttp(String url, ArrayList<File> files, Map<String, Object> params, final JsonCallback callback) {
        HttpUtil.uploadFile(url, files, params, new HttpCallback() {
            @Override
            public void onFinish(String response) {
                try {
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        // setResponse(response.toString());
                        JsonModel jsonModel = new Gson().fromJson(response.toString(), JsonModel.class);
                        if (URLCONST.isRSA && !StringHelper.isEmpty(jsonModel.getResult())) {
                            jsonModel.setResult(StringHelper.decode(new String(RSAUtilV2.decryptByPrivateKey(Base64.decode(jsonModel.getResult().replace("\n", ""), Base64.DEFAULT), APPCONST.privateKey))));
                        }
                        callback.onFinish(jsonModel);
                        Log.d("Http", "RSA finish：" + new Gson().toJson(jsonModel));
                    }

                } catch (Exception e) {
                    callback.onError(e);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFinish(InputStream in) {

            }

            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    /**
     * 单文件上传
     * @param url
     * @param file
     * @param params
     * @param callback
     */
    public static void uploadFile(String url, File file, Map<String, Object> params, final JsonCallback callback) {
        String[] filePaths = {file.getAbsolutePath()};
        HttpUtil.uploadFileRequest(url, filePaths, params, callback);
    }

    /**
     * 文件下载
     * @param url
     * @param callback
     */
    public static void httpGetFile(String url, final HttpCallback callback) {
        Log.d("Http", "success1");
        HttpUtil.sendGetRequest(url, new HttpCallback() {
            @Override
            public void onFinish(Bitmap bm) {

            }

            @Override
            public void onFinish(InputStream in) {
                callback.onFinish(in);

            }

            @Override
            public void onFinish(String response) {

            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }



}
