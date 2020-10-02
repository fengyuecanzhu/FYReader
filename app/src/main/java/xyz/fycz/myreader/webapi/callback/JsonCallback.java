package xyz.fycz.myreader.webapi.callback;


import xyz.fycz.myreader.entity.JsonModel;

/**
 * Created by zhao on 2016/10/25.
 */

public interface JsonCallback {

    void onFinish(JsonModel jsonModel);

    void onError(Exception e);

}
