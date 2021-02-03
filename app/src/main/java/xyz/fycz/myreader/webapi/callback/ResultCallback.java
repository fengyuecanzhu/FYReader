package xyz.fycz.myreader.webapi.callback;


public interface ResultCallback {

    void onFinish(Object o, int code);

    void onError(Exception e);

}
