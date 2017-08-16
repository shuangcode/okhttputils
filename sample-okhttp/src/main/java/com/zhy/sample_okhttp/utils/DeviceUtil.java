package com.zhy.sample_okhttp.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.zhy.sample_okhttp.MyApplication;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by magical.zhang on 2017/7/31.
 * Description :
 */

public class DeviceUtil {

    /**
     * 获取系统
     */
    public static String getOs() {
        return "Android" + Build.VERSION.RELEASE;
    }

    /**
     * 获取系统版本
     */
    public static String getOsVersion() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    /**
     * 获取手机品牌
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取设备型号
     * <p>如MI2SC</p>
     *
     * @return 设备型号
     */
    public static String getBrandModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }

    @Nullable
    public static String getDeviceNumber() {

        Context appContext = MyApplication.getContext();
        if (null != appContext) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);

            return null == telephonyManager ? null : telephonyManager.getDeviceId();

        }

        return "unknow";

    }

    public static String getDensity() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return metrics.widthPixels + " * " + metrics.heightPixels;
    }

    /**
     * 获取当前时区
     */
    public static String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName(false,TimeZone.SHORT,Locale.ENGLISH);
    }

    //没有网络连接
    public static final String NETWORK_NONE = "no";
    //wifi连接
    public static final String NETWORK_WIFI = "wifi";
    //手机网络数据连接类型
    public static final String NETWORK_2G = "2G";
    public static final String NETWORK_3G = "3G";
    public static final String NETWORK_4G = "4G";
    public static final String NETWORK_MOBILE = "mobile";

    /**
     * 获取当前网络连接类型
     */
    @Nullable
    public static String getNetworkState() {

        Context appContext = MyApplication.getContext();
        if (null == appContext) {
            return null;
        }

        //获取系统的网络服务
        ConnectivityManager connManager =
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        //如果当前没有网络
        if (null == connManager) return NETWORK_NONE;

        //获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }

        // 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORK_WIFI;
                }
            }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state) {
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_2G;
                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_3G;
                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_4G;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA")
                                    || strSubTypeName.equalsIgnoreCase("WCDMA")
                                    || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORK_3G;
                            } else {
                                return NETWORK_MOBILE;
                            }
                    }
                }
            }
        }
        return NETWORK_NONE;
    }

    public static String getCountry() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= 24) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = Resources.getSystem().getConfiguration().locale;
        }
        return locale.getCountry();
    }

    /**
     * 获取当前系统语言格式
     */
    public static String getCurrentLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= 24) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = Resources.getSystem().getConfiguration().locale;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (language.startsWith("en")) {
            return "en";
        }
        String lc = language + "_" + country;
        return lc;
    }

    @Nullable
    public static String getSdkVersion() {

        Context appContext = MyApplication.getContext();
        if (null == appContext) {
            return null;
        }

        return "1.2.3";
    }

    public static String getPlatform() {
        return "2";
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值, 或者异常)，则返回值为空
     */
    public static String getChannel(String key) {

        Context appContext = MyApplication.getContext();

        if (appContext == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = appContext.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo =
                        packageManager.getApplicationInfo(appContext.getPackageName(),
                                PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }
}
