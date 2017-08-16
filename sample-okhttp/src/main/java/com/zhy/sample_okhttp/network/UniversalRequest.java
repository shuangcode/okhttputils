package com.zhy.sample_okhttp.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.OkHttpRequestBuilder;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.sample_okhttp.utils.DeviceUtil;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by daniel.xiao on 2017/8/16.
 */

public class UniversalRequest {

    private static HashSet<String> mTagSet;
    private static StringBuilder deviceParams = new StringBuilder();

    public static void requestUrlGet(Object tag, String url, Map<String, String> paramsMap,
            Callback callback){

        paramsMap = ensureParamMap(paramsMap);
        String newTag = checkTag(tag);
        GetBuilder builder = OkHttpUtils.get().url(url).params(paramsMap).tag(newTag);
        addAuthToHeader(builder, "POST", url, (HashMap<String, ?>) paramsMap);
        builder.addHeader("wpkua", getDeviceParams());
        builder.build().execute(callback);
    }

    /**
     * post类型请求
     */
    public static void requestUrl(Object tag, String url, Map<String, String> paramsMap,
            Callback callback) {

        paramsMap = ensureParamMap(paramsMap);
        String newTag = checkTag(tag);
        paramsMap.put("pf", "wpk");
        PostFormBuilder builder = OkHttpUtils.post().url(url).params(paramsMap).tag(newTag);
        addAuthToHeader(builder, "POST", url, (HashMap<String, ?>) paramsMap);
        builder.addHeader("wpkua", getDeviceParams());
        builder.build().execute(callback);
    }

    /**
     * 国内版的sdk需要验证登录信息
     * @param builder
     * @param method
     * @param url
     * @param data
     */
    private static void addAuthToHeader(OkHttpRequestBuilder builder, String method, String url,
            HashMap<String, ?> data) {
        //Map<String, Object> authMap = PubSky.getOAuthHeader(method, url, data);
        Map<String, Object> authMap = null;
        if (null == authMap) {
            return;
        }

        StringBuilder authString = new StringBuilder();
        authString.append("OAuth ");

        Set<String> keys = authMap.keySet();
        Iterator<String> i = keys.iterator();
        for (; ; ) {
            String key = i.next();
            authString.append(String.format("%s=\"%s\"", key, authMap.get(key).toString()));
            if (!i.hasNext()) {
                break;
            }
            authString.append(", ");
        }
        builder.addHeader("Authorization", authString.toString());
    }


    /**
     * 上传本地图片
     */
    public static void upLoadImage(Object tag, String imgPath,
            final Callback onJsonRequestListener) {
        HashMap<String, String> hm = new HashMap<>();
        hm.put("upload_type", "file");
        hm.put("file_type", "image");
        if (!TextUtils.isEmpty(imgPath)) {
            File photoFile = new File(imgPath);
            if (photoFile.exists() && photoFile.length() > 0) {
                UniversalRequest.uploadFile(tag, "服务器url", hm, photoFile,
                        "upload_file", onJsonRequestListener);
            } else {
            }
        } else {
        }
    }

    public static void uploadFile(Object tag, String url, Map<String, String> paramsMap, File file,
            String key, Callback callback) {

        paramsMap = ensureParamMap(paramsMap);
        String newTag = checkTag(tag);

        url += "?pf=wpk-file";
        paramsMap.put("pf", "wpk-file");

        PostFormBuilder builder = OkHttpUtils.post().url(url).params(paramsMap).tag(newTag);
        //添加头信息
        addAuthToHeader(builder, "POST", url, (HashMap<String, ?>) paramsMap);
        builder.addHeader("wpkua", getDeviceParams());
        builder.addFile(key, file.getName(), file).build().execute(callback);
    }

    /**
     * 获取设备信息
     */
    private static String getDeviceParams() {

        if(deviceParams.toString().length() > 20){
            return deviceParams.toString().toLowerCase();
        }
        String OS = DeviceUtil.getOs();
        String osVersion = DeviceUtil.getOsVersion();
        String brand = DeviceUtil.getBrand();
        String brandModel = DeviceUtil.getBrandModel();
        String deviceNum = DeviceUtil.getDeviceNumber();
        String screen = DeviceUtil.getDensity();
        String timeZone = DeviceUtil.getCurrentTimeZone();
        String network = DeviceUtil.getNetworkState();
        String country = DeviceUtil.getCountry();
        String language = DeviceUtil.getCurrentLanguage();
        String sdkVersion = DeviceUtil.getSdkVersion();
        String platform = DeviceUtil.getPlatform();
        String channel = DeviceUtil.getChannel("wpk_channel");

        //构造 params
        return deviceParams
                //append("os=")
                .append(OS)
                .append("|")
                .append(osVersion)
                .append("|")
                .append(brand)
                .append("|")
                .append(brandModel)
                .append("|")
                .append(deviceNum)
                .append("|")
                .append(screen)
                .append("|")
                .append(timeZone)
                .append("|")
                .append(network)
                .append("|")
                .append(country)
                .append("|")
                .append(language)
                .append("|")
                .append(sdkVersion)
                .append("|")
                .append(platform)
                .append("|")
                .append(channel)
                .toString()
                .toLowerCase();
    }

    /**
     * Step 3
     * 将对象取类名（String除外），防止请求引用了页面的对象造成内存泄漏
     * new function : add tag to a tag set and cancel all request when ui page is destroyed
     */
    private static String checkTag(Object tag) {
        String newTag;
        if (tag instanceof String) {
            newTag = (String) tag;
        } else {
            newTag = tag.getClass().getSimpleName();
        }
        //add by magical
        if (mTagSet == null) {
            mTagSet = new HashSet<>();
        }
        mTagSet.add(newTag);
        return newTag;
    }


    @NonNull
    private static Map<String, String> ensureParamMap(Map<String, String> param) {
        if (param == null || param.size() == 0) {
            param = new HashMap<>();
        }
        return param;
    }

    /**
     * 取消所有请求
     * 在 sdk 主 Activity destroy 时调用
     * 防止异步回调继续操作ui
     */
    public static void cancelAllReqAndReleaseTagSet() {
        Iterator<String> iterator = mTagSet.iterator();
        while (iterator.hasNext()) {
            String tag = iterator.next();
            cancelAll(tag);
        }
        mTagSet = null;
    }

    /**
     * 取消与 tag 匹配且正在运行或排队的请求
     */
    public static void cancelAll(Object tag) {
        OkHttpUtils.getInstance().cancelTag(tag);
    }
}
