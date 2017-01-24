
package com.test;

import com.pacewear.httpserver.IResponseObserver;
import com.pacewear.tsm.server.tosservice.TSMTosService;
import com.pacewear.tws.phoneside.wallet.walletservice.WalletService;
import com.qq.taf.jce.JceStruct;

import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

public abstract class BaseTsmAction {
    private TSMTosService mService = null;
    private OnSubscribe<Boolean> mHttpEvent = new OnSubscribe<Boolean>() {

        @Override
        public void call(Subscriber<? super Boolean> t) {
            mService.invoke(new IResponseObserver() {

                @Override
                public void onResponseSucceed(long uniqueSeq, int operType, JceStruct response) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                        String description) {
                    // TODO Auto-generated method stub

                }
            });

        }
    };

    protected abstract List<String> onParseApdu(Boolean t);

    private Func1<Boolean, List<String>> mMapEvent = new Func1<Boolean, List<String>>() {

        @Override
        public List<String> call(Boolean t) {
            List list = onParseApdu(t);
            return genApduObservable(list);
        }
    };

//    private Func1<List<String>, >
    // private Action1<>

    private Observable<List<String>> genApduObservable(List<String> apdus) {
        return Observable.create(new OnSubscribe<List<String>>() {

            @Override
            public void call(Subscriber<? super List<String>> t) {
                // TODO Auto-generated method stub

            }
        });
    }

    private Observable<Boolean> getEventSource(final TSMTosService service) {
        return Observable.create(new OnSubscribe<Boolean>() {

            @Override
            public void call(Subscriber<? super Boolean> handler) {
                // TODO Auto-generated method stub
                service.invoke(new IResponseObserver() {

                    @Override
                    public void onResponseSucceed(long uniqueSeq, int operType,
                            JceStruct response) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onResponseFailed(long uniqueSeq, int operType, int errorCode,
                            String description) {
                        // TODO Auto-generated method stub

                    }
                });
            }
        });
    }

    public BaseTsmAction(final TSMTosService service) {
        getEventSource(service).map(mMapEvent);
    }

}
