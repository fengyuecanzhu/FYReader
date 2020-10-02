package xyz.fycz.myreader.webapi;

import xyz.fycz.myreader.application.SysManager;
import xyz.fycz.myreader.common.ErrorCode;
import xyz.fycz.myreader.entity.JsonModel;
import xyz.fycz.myreader.util.HttpUtil;
import xyz.fycz.myreader.util.JsonArrayToObjectArray;
import com.google.gson.Gson;
import xyz.fycz.myreader.webapi.callback.JsonCallback;
import xyz.fycz.myreader.webapi.callback.ResultCallback;
import xyz.fycz.myreader.source.HttpDataSource;
import xyz.fycz.myreader.util.ToastUtils;

import java.util.Map;

/**
 * Created by zhao on 2017/6/20.
 */

public class BaseApi {

    /**
     * post通用返回实体api
     * @param url
     * @param params
     * @param c 返回的实体类型
     * @param callback
     */
    public static void postCommonApi(String url, Map<String, Object> params, final Class c, final ResultCallback callback) {
        HttpDataSource.httpPost(url, HttpUtil.makePostOutput(params), new JsonCallback() {
            @Override
            public void onFinish(JsonModel jsonModel) {
                if (jsonModel.isSuccess()) {
                    callback.onFinish(new Gson().fromJson(jsonModel.getResult(),c), jsonModel.getError());
                } else {
                    noSuccess(jsonModel,callback);
                }
            }
            @Override
            public void onError(Exception e) {
                error(e,callback);
            }
        });
    }

    /**
     * post通用返回字符串api
     * @param url
     * @param params
     * @param callback
     */
    public static void postCommonReturnStringApi(String url, Map<String, Object> params, final ResultCallback callback) {
        HttpDataSource.httpPost(url, HttpUtil.makePostOutput(params), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o, code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * get通用返回字符串api
     * 通过api获取蓝奏云直链
     * @param url
     * @param params
     * @param callback
     */
    public static void postLanzousApi(String url, Map<String, Object> params, final ResultCallback callback, final String referer) {
        HttpDataSource.httpPost(url, HttpUtil.makePostOutput(params), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o, code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        }, referer);
    }
    /**
     * get通用返回实体api
     * @param url
     * @param params
     * @param c 返回的实体类型
     * @param callback
     */
    public static void getCommonApi(String url, Map<String, Object> params, final Class c, final ResultCallback callback) {
        HttpDataSource.httpGet(HttpUtil.makeURL(url,params), new JsonCallback() {
            @Override
            public void onFinish(JsonModel jsonModel) {
                if (jsonModel.isSuccess()) {
                    callback.onFinish(new Gson().fromJson(jsonModel.getResult(),c), jsonModel.getError());
                } else {
                    noSuccess(jsonModel,callback);
                }
            }

            @Override
            public void onError(Exception e) {
               error(e,callback);
            }
        });
    }

    /**
     * get通用返回字符串api
     * @param url
     * @param params
     * @param callback
     */
    public static void getCommonReturnStringApi(String url, Map<String, Object> params, final ResultCallback callback) {
        HttpDataSource.httpGet(HttpUtil.makeURL(url,params), new JsonCallback() {
            @Override
            public void onFinish(JsonModel jsonModel) {
                if (jsonModel.isSuccess()) {
                    callback.onFinish(jsonModel.getResult(), jsonModel.getError());
                } else {
                    noSuccess(jsonModel, callback);
                }
            }
            @Override
            public void onError(Exception e) {
              error(e,callback);
            }
        });
    }
    /**
     * get通用返回字符串api
     * @param url
     * @param params
     * @param callback
     */
    public static void getCommonReturnStringApi2(String url, Map<String, Object> params, final ResultCallback callback) {
        HttpDataSource.httpGet(HttpUtil.makeURL(url, params), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o,code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * get通用返回Html字符串api
     * @param url
     * @param params
     * @param callback
     */
    public static void getCommonReturnHtmlStringApi(String url, Map<String, Object> params, String charsetName,  boolean isRefresh, final ResultCallback callback) {
        HttpDataSource.httpGet_html(HttpUtil.makeURL(url, params), charsetName, isRefresh ,new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o,code);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
//                error(e,callback);
            }
        });
    }


    /**
     * get通用获取实体列表api
     * @param url
     * @param params
     * @param c 返回是列表实体类型
     * @param callback
     */
    protected static void getCommonListApi(String url, Map<String, Object> params, final Class c, final ResultCallback callback) {
        HttpDataSource.httpGet(HttpUtil.makeURL(url,params), new JsonCallback() {
            @Override
            public void onFinish(JsonModel jsonModel) {
                if (jsonModel.isSuccess()) {
                    try {
                        callback.onFinish(JsonArrayToObjectArray.getArray(jsonModel.getResult(),c), jsonModel.getError());
                    }catch (Exception e){
                        callback.onError(e);
                        e.printStackTrace();
                    }
                } else {
                    noSuccess(jsonModel,callback);
                }
            }

            @Override
            public void onError(Exception e) {
               error(e,callback);
            }
        });
    }

    /**
     * api异常处理
     * @param e
     * @param callback
     */
    private static void error(Exception e, final ResultCallback callback){
      /*  if (e.toString().contains("SocketTimeoutException") || e.toString().contains("UnknownHostException")) {
            TextHelper.showText("网络连接超时，请检查网络");
        }*/
        e.printStackTrace();
        callback.onError(e);
    }

    /**
     * api请求失败处理
     * @param jsonModel
     * @param callback
     */
    private static void noSuccess(JsonModel jsonModel, ResultCallback callback){
        if (!jsonModel.isSuccess()) {
            if (jsonModel.getError() == ErrorCode.no_security) {
                ToastUtils.showWarring("登录过期，请重新登录");
                SysManager.logout();
            } else {
                if (jsonModel.getError() == 0) {
                    callback.onFinish(jsonModel.getResult(), -1);
                } else {
                    callback.onFinish(jsonModel.getResult(), jsonModel.getError());
                }
            }
        }
    }
}
