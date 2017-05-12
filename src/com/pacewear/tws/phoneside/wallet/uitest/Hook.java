
package com.pacewear.tws.phoneside.wallet.uitest;

import com.pacewear.tws.phoneside.wallet.walletservice.WalletService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hook {
    private static Hook sInstance = null;
    private static boolean sCardFackEnable = false;
    private static boolean sOrderFackEnable = false;
    public static int iHookNode = 0;
    // private HashMap<Integer, List<HookData>> mHookMap = new HashMap<Integer, List<HookData>>();
    private HashMap<Integer, IHookFunc> mHookMap = new HashMap<Integer, Hook.IHookFunc>();
    private List<IHookFunc> mDetails = new ArrayList<Hook.IHookFunc>();

    public interface IHookFunc {
        boolean onHook(int method, String src, HookMeteData target);
    }

    public static Hook getInstance() {
        if (sInstance == null) {
            sInstance = new Hook();
        }
        return sInstance;
    }

    private Hook() {
        mHookMap.put(1, HookDataLibrary.cardListException);
        mHookMap.put(2, HookDataLibrary.cardListEmpty);
        mHookMap.put(3, HookDataLibrary.cardList1);
        mHookMap.put(4, HookDataLibrary.cardList2);
        mHookMap.put(5, HookDataLibrary.cardList3);
        mDetails.add(HookDataLibrary.cardQuerySzt);
        mDetails.add(HookDataLibrary.cardQueryBjt);
        mDetails.add(HookDataLibrary.cardQueryLnt);
    }

    public final boolean invoke(WalletService service, long reqId, String[] contents) {
        if (!sCardFackEnable) {
            return false;
        }
        IHookFunc target = mHookMap.get(iHookNode);
        if (target == null) {
            return false;
        }
        String src = (contents != null && contents.length > 0) ? contents[0] : "";
        HookMeteData targetDat = new HookMeteData();
        if (target.onHook(service.getIntMethod(), src, targetDat)) {
            service.getCallback().onResult(reqId, targetDat.iRet, new String[] {
                    targetDat.sContent
            }, new Integer[] {
                    targetDat.iCodeInteral
            }, null);
            return true;
        }
        for (IHookFunc cardDetail : mDetails) {
            if (cardDetail.onHook(service.getIntMethod(), src, targetDat)) {
                service.getCallback().onResult(reqId, targetDat.iRet, new String[] {
                        targetDat.sContent
                }, new Integer[] {
                        targetDat.iCodeInteral
                }, null);
                return true;
            }
        }
        return false;
    }

    public final void updateNode(int index) {
        sCardFackEnable = (index > 0);
        iHookNode = index;
    }

    public static class HookMeteData {
        public int iRet;
        public String sContent;
        public int iCodeInteral;
    }

}
