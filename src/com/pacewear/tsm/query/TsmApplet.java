
package com.pacewear.tsm.query;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public abstract class TsmApplet implements ITsmApplet {
    public static class AppletTagQuery implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 802952981743012968L;
        private String aid = null;
        private HashMap<String, List<String>> mTagApduList;

        public AppletTagQuery(String aid) {
            this.aid = aid;
            mTagApduList = new HashMap<String, List<String>>();
        }

        public void put(String key, List<String> list) {
            mTagApduList.put(key, list);
        }

        public List<String> getApdusByTag(String key) {
            return mTagApduList.get(key);
        }
    }

    public static ITsmApplet get(String aid) {
        // if()
        ITsmApplet applet = null;
        if (FOODCARD.equalsIgnoreCase(aid)) {
            applet = new FoodCard();
        } else if (SHANGHAITONG.equalsIgnoreCase(aid)) {
            applet = new ShangHaiTong();
        } else if (BEIJINGTONG.equalsIgnoreCase(aid)) {
            applet = new BeijingTong();
        }
        return applet;
    }
}
