package xyz.fycz.myreader.util.utils;


import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

import java.util.List;

/**
 * Created by newbiechen on 17-4-29.
 */

public class RxUtils {

    public static <T> SingleSource<T> toSimpleSingle(Single<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NotNull
    public static <T> ObservableSource<T> toSimpleSingle(Observable<T> upstream){
        return upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static <T,R> TwoTuple<T,R> twoTuple(T first,R second){
        return new TwoTuple<T, R>(first, second);
    }


    public static class TwoTuple<A, B> {
        public final A first;
        public final B second;

        public TwoTuple(A a, B b) {
            this.first = a;
            this.second = b;
        }
    }
}
