package xyz.fycz.myreader.webapi;


public interface ResultCallback {

    void onFinish(Object o, int code);

    void onError(Exception e);

}
