package com.cardcamera.ch.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;

public class LibUtils {
    private static final String DEBUG_TAG = "fdj";

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;

    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    /**
     * 删除文件
     *
     * @param context
     * @param path
     */
    public static void deleteFile(Context context, String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                //通知删除文件，否则相册里会有灰色的空白图
                Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                media.setData(contentUri);
                context.sendBroadcast(media);
            }
        }
    }
    /**
     * 判断是否是主线程
     *
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
    public static void log(String title, String msg) {
        if (isMainThread()) {
            Log.e(title, "---- main ---- " + msg);
        } else {
            Log.e(title, msg);
        }
    }

    public static void myLog(String title, String msg) {
        if (msg == null) {
            log(title, "msg is null");
            return;
        }
        if (msg.length() > 4000) {
            //超过长度，分段
            for (int i = 0; i < msg.length(); i += 4000) {
                if (i + 4000 < msg.length()) {
                    log(title, msg.substring(i, i + 4000));
                } else {
                    log(title, msg.substring(i, msg.length()));
                }
            }
        } else {
            log(title, msg);
        }
    }
    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    public static String showDecimalValue(int value) {
        String str = value + ".00";
        LibUtils.myLog("int " + value + " --> " + str);
        return str;
    }

    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    public static String showDecimalValue(double value) {
        String str = String.format("%.2f", value);
        LibUtils.myLog("double " + value + " --> " + str);
        return str;
    }

    /**
     * 保留一位小数 返回double
     *
     * @param value
     * @return
     */
    public static double showDecimalValue2(double value) {
        double result = (double) Math.round(value * 100) / 100;
        LibUtils.myLog("double " + value + " --> " + result);
        return result;
    }
    /**
     * 记录日志
     *
     * @param msg
     */
    public static void myLog(String msg) {
        myLog(DEBUG_TAG, msg);
    }

    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    public static String showDecimalValue(String value) {
        String str;
        if (value.contains(".")) {
            int index = value.indexOf(".");
            int length = value.length();
            if (index == length - 1) {
                //1.
                str = value + "00";
            } else if (index == length - 2) {
                //1.x
                str = value + "0";
            } else if (index == length - 3) {
                //1.12
                str = value;
            } else {
                //1.123
                str = value.substring(0, index + 3);
            }
        } else {
            str = value + ".00";
        }
        LibUtils.myLog("String " + value + " --> " + str);
        return str;
    }

    /**
     * 获得包名
     *
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得freso使用的本地图片的uri
     *
     * @param context
     * @param localPath
     * @return
     */
    public static Uri getFrescoUriForLocalPicture(Context context, String localPath) {
        String packageName = getPackageName(context);
        if (packageName != null) {
            Uri parse = Uri.parse("file://" + packageName + localPath);
            return parse;
        } else {
            return null;
        }
    }

}
