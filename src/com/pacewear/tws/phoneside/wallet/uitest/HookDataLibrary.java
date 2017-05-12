
package com.pacewear.tws.phoneside.wallet.uitest;

import com.pacewear.tws.phoneside.wallet.uitest.Hook.HookMeteData;
import com.pacewear.tws.phoneside.wallet.uitest.Hook.IHookFunc;
import com.tencent.tws.proto.wallet.WatchNFCManager.MSG_RPC_METHOD;

public class HookDataLibrary {
    // public static HookData cardList400301 = new HookData(MSG_RPC_METHOD._CARD_LIST_QUERY, 400301,
    // "");
    // public static HookData cardLits400200 = new HookData(MSG_RPC_METHOD._CARD_LIST_QUERY, 400200,
    // "");
    // //
    // "{'card_list':[{'activation_status':'0','install_status':'2','instance_id':'535A542E57414C4C45542E454E56'}"
    // public static HookData cardListszt = new HookData(MSG_RPC_METHOD._CARD_LIST_QUERY, 0,
    // "{\"card_list\":[{\"activation_status\":\"0\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\"}]}");
    // //
    // {'activation_status':'0','balance':'30000','card_number':'691000204','install_status':'2','instance_id':'535A542E57414C4C45542E454E56','validity':'2027-06-30'}
    // public static HookData cardQuery_szt = new HookData(MSG_RPC_METHOD._CARD_QUERY, 0,
    // "{\"activation_status\":\"1\",\"balance\":\"10016\",\"card_number\":\"5100004020003470\",\"install_status\":\"2\",\"instance_id\":\"5943542E55534552\",\"validity\":\"2026-12-31\"}");
    // // public static HookData cardQuery_lnt=new HookData(MSG_RPC_METHOD._CARD_QUERY,0,'');
    // // public static HookData cardQuery_bjt=new HookData(MSG_RPC_METHOD._CARD_QUERY,0,'');

    // public class VaribaleCall
    public static IHookFunc cardQuerySzt = new IHookFunc() {

        @Override
        public boolean onHook(int method, String data, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_QUERY) {
                return false;
            }
            target.iRet = 0;
            target.sContent = "{\"activation_status\":\"1\",\"balance\":\"10016\",\"card_number\":\"5100004020003470\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\",\"validity\":\"2026-12-31\"}";
            return true;
        }
    };
    public static IHookFunc cardQueryBjt = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_QUERY) {
                return false;
            }
            target.iRet = 0;
            target.sContent = "{\"activation_status\":\"0\",\"balance\":\"20000\",\"card_number\":\"22233000\",\"install_status\":\"2\",\"instance_id\":\"9156000014010001\",\"validity\":\"2020-12-31\"}";
            return true;
        }
    };
    public static IHookFunc cardQueryLnt = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_QUERY) {
                return false;
            }
            target.iRet = 0;
            target.sContent = "{\"activation_status\":\"0\",\"balance\":\"10000\",\"card_number\":\"114480200\",\"install_status\":\"2\",\"instance_id\":\"5943542E55534552\",\"validity\":\"2020-12-31\"}";
            return true;
        }
    };
    public static IHookFunc cardListException = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_LIST_QUERY) {
                return false;
            }
            target.iRet = 400201;
            target.sContent = "";
            return true;
        }
    };
    public static IHookFunc cardListEmpty = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_LIST_QUERY) {
                return false;
            }
            target.iRet = 400301;
            target.sContent = "";
            return true;
        }
    };
    public static IHookFunc cardList1 = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_LIST_QUERY) {
                return false;
            }
            target.iRet = 0;
            // "{\"card_list\":[{\"activation_status\":\"0\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\"}]}";
            target.sContent = "{\"card_list\":[{\"activation_status\":\"1\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\"}"
                    + ",{\"activation_status\":\"0\",\"install_status\":\"1\",\"instance_id\":\"9156000014010001\"},"
                    + "{\"activation_status\":\"0\",\"install_status\":\"1\",\"instance_id\":\"5943542E55534552\"}]}";
            return true;
        }
    };
    public static IHookFunc cardList2 = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_LIST_QUERY) {
                return false;
            }
            target.iRet = 0;
            target.sContent = "{\"card_list\":[{\"activation_status\":\"1\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\"}"
                    + ",{\"activation_status\":\"0\",\"install_status\":\"2\",\"instance_id\":\"9156000014010001\"},"
                    + "{\"activation_status\":\"0\",\"install_status\":\"1\",\"instance_id\":\"5943542E55534552\"}]}";
            return true;
        }
    };
    public static IHookFunc cardList3 = new IHookFunc() {

        @Override
        public boolean onHook(int method, String src, HookMeteData target) {
            if (method != MSG_RPC_METHOD._CARD_LIST_QUERY) {
                return false;
            }
            target.iRet = 0;
            target.sContent = "{\"card_list\":[{\"activation_status\":\"1\",\"install_status\":\"2\",\"instance_id\":\"535A542E57414C4C45542E454E56\"}"
                    + ",{\"activation_status\":\"0\",\"install_status\":\"2\",\"instance_id\":\"9156000014010001\"},"
                    + "{\"activation_status\":\"0\",\"install_status\":\"2\",\"instance_id\":\"5943542E55534552\"}]}";
            return true;
        }
    };
}
