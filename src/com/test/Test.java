
package com.pacewear.lntconnect;

import android.util.Log;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

public class Test {
    public static final String TAG = "Test";

    public interface OnInvokeCallback {
        public void onSuccess(String desc);

        public void onFail(int error);
    }
    final Person mPerson = new Person(18, "hello, yuanqq");
    public interface OnSecondcallback {
        public void onResult(int ret, String desc);
    }

    private void invoke(Person p, OnInvokeCallback callback) {
        if (callback != null) {
            if (p.id < 10) {
                callback.onSuccess("ok");
            } else {
                callback.onFail(-11);
            }
        }
    }

    private void invoke2(String val, OnSecondcallback callback) {
        if (callback != null) {
            callback.onResult(38, val);
        }
    }

    Observable<Person> firstObservable = Observable.create(new Observable.OnSubscribe<Person>() {

        @Override
        public void call(final Subscriber<? super Person> handler) {
            
            invoke(mPerson, new OnInvokeCallback() {

                @Override
                public void onSuccess(String desc) {
                    handler.onNext(mPerson);
                }

                @Override
                public void onFail(int error) {
                    handler.onError(new Exception("error!!!!!" + error));
                }
            });
        }
    });

    private Func1<Person, Observable<String>> mFlatmap = new Func1<Person, Observable<String>>() {

        @Override
        public Observable<String> call(Person t) {
            t.name = "201aaa";
            return secondObservable(t.name);
        }
    };

    private Observable<String> secondObservable(final String val) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {

                // subscriber.onNext(val);
                // // subscriber.onError(new Throwable());
                // subscriber.onCompleted();
                invoke2(val, new OnSecondcallback() {

                    @Override
                    public void onResult(int ret, String desc) {
                        if (ret > 0) {
                            subscriber.onNext(val);
                        } else {
                            subscriber.onError(new Exception("error!!!!!" + ret));
                        }

                    }
                });

                Log.d(TAG, "被观察者-observable->call()->onCompleted()之后是否还有输出");
            }
        });
    }

    Observer<String> observer = new Observer<String>() {
        @Override
        public void onCompleted() {
            Log.d(TAG, "观察者-observer:onCompleted()");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "观察者-observer:onError " + e.getMessage());
        }

        @Override
        public void onNext(String s) {
            Log.d(TAG, "观察者-observer:onNext(): " + s);
            // getException();//故意让程序出现异常,用于测试onError()方法的执行....
        }
    };

    private Observable<String> justObservable() {
        Observable<String> observable = Observable.just("nihao", "haha", "bb");
        return observable;
    }

    public void test() {
        firstObservable.flatMap(mFlatmap).subscribe(observer);
    }
}
