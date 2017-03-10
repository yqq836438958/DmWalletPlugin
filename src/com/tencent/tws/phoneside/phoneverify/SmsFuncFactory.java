
package com.tencent.tws.phoneside.phoneverify;

import com.tencent.tws.phoneside.phoneverify.common.GetPhoneNumber;
import com.tencent.tws.phoneside.phoneverify.common.GetSecurityCode;
import com.tencent.tws.phoneside.phoneverify.common.SendSecurityCode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class SmsFuncFactory {
    private static SmsFuncFactory sInstance = null;
    public static final String SERVICE_GET_PHONE = "getPhoneNum";
    public static final String SERVICE_GET_VERIFYCODE = "getVerifyCode";
    public static final String SERVICE_SEND_VERIFYCODE = "sendVerifyCode";
    private HashMap<String, Class<?>> mRegistClassMap = new HashMap<String, Class<?>>();

    public static SmsFuncFactory get() {
        if (sInstance == null) {
            synchronized (SmsFuncFactory.class) {
                if (sInstance == null) {
                    sInstance = new SmsFuncFactory();
                }
            }
        }
        return sInstance;
    }

    public void regist(String className, Class<?> _class) {
        mRegistClassMap.put(className, _class);
    }

    public void clear() {
        mRegistClassMap.clear();
    }

    public SmsTosService getPhoneNum() {
        Class<?> _cClass = mRegistClassMap.get(SERVICE_GET_PHONE);
        if (_cClass != null) {
            try {
                return (SmsTosService) _cClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return new GetPhoneNumber();
    }

    public SmsTosService sendVerifyCode(String phone, String code) {
        Class<?> clz = mRegistClassMap.get(SERVICE_SEND_VERIFYCODE);
        if (clz != null) {
            try {
                Constructor<?> c = clz.getConstructor(String.class, String.class);
                return (SmsTosService) c.newInstance(phone, code);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new SendSecurityCode(phone, code);
    }

    public SmsTosService getSecurityCode(String phone, String code) {
        Class<?> clz = mRegistClassMap.get(SERVICE_GET_VERIFYCODE);
        if (clz != null) {
            try {
                Constructor<?> c = clz.getConstructor(String.class, String.class);
                return (SmsTosService) c.newInstance(phone, code);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return new GetSecurityCode(phone, code);
    }
}
