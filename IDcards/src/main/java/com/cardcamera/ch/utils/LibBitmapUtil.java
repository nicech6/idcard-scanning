package com.cardcamera.ch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.cardcamera.ch.camera.ScannerClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LibBitmapUtil {
    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap 需要旋转的图片
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @param path   保存的地址
     * @return
     * @throws IOException
     */
    public static String saveBitmap(Bitmap bitmap, String path) {
        return saveBitmap(null, bitmap, path, 90, null);
    }

    /**
     * 保存bitmap到本地，并通知相册刷新图片
     *
     * @param bitmap
     * @param path   保存的地址
     * @return
     * @throws IOException
     */
    public static String saveBitmap(Context context, Bitmap bitmap, String path, int quality, ScannerClient.ScanListener listener) {
        String result = null;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
            result = path;
            insertImageToGallery(context, path, null, listener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            result = null;
        } catch (IOException e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * 保存图片到相册
     *
     * @param context 要使用 getApplicationContext()
     * @param path
     * @param type
     */
    public static void insertImageToGallery(Context context, String path, String type, ScannerClient.ScanListener l) {
        new ScannerClient(context, new File(path), type, l);
    }

}
