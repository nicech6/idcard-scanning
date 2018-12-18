package com.cardcamera.ch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CH
 * @date 2018/7/30
 */
public class OcrUtils extends Thread {
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "normal";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;


    private static final int ACTION_OCR = 0X2010;

    private OcrThreadHandler mOcrThreadHandler;

    private static String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则

    private Context mContext;


    private static final class OcrThreadHandler extends Handler {

        private OcrUtils mOcrThread;

        OcrThreadHandler(OcrUtils thread) {
            this.mOcrThread = thread;
        }

        public void release() {
            mOcrThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mOcrThread == null) {
                return;
            }
            switch (msg.what) {
                case ACTION_OCR:
                    mOcrThread.result(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    }

    public void result(String s) {
        if (mOnOrcResult != null) {
            if (s != null && s.length() == 18) {
                mOnOrcResult.success(s);
            } else {
                mOnOrcResult.failed("不等于18字");
            }
        }
    }

    Bitmap mBitmap;

    /**
     * ocr回调
     */
    public interface OnOrcResult {
        void success(String s);

        void failed(String s);
    }

    public OcrUtils(Context context,Bitmap bitmap, OnOrcResult onOrcResult) {
        mContext=context;
        mOnOrcResult = onOrcResult;
        mBitmap = bitmap;
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();

        mOcrThreadHandler = new OcrThreadHandler(this);

        init(mBitmap);
        // enter thread looper
        Looper.loop();
    }

    /**
     * 回调
     */
    public OnOrcResult mOnOrcResult;


    private void init(Bitmap bitmap) {
        if (bitmap == null) {
            Log.i("TAG", "init:  bitmap为空");
            return;
        }
        LibUtils.myLog("ocr", "init: start " + System.currentTimeMillis());
        File tessdataFileDir;
        File tessdataFile;
        final File newFile;
        // 判断是否有SD卡或者外部存储器
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 有SD卡则使用SD - PS:没SD卡但是有外部存储器，会使用外部存储器
            tessdataFileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/tessdata/");
        } else {
            // 没有SD卡或者外部存储器，使用内部存储器
            tessdataFileDir = new File(mContext.getFilesDir().getPath() + "/tessdata/");
        }
        // 若目录不存在则创建目录
        if (!tessdataFileDir.exists()) {
            tessdataFileDir.mkdir();
        }
        tessdataFile = new File(tessdataFileDir.getPath() + "/" + DEFAULT_LANGUAGE_NAME);
        if (tessdataFile.length() <= 0) {
            try {
                writeBytesToFile(mContext.getAssets().open(DEFAULT_LANGUAGE_NAME), tessdataFile);
            } catch (IOException e) {
                e.printStackTrace();
                LibUtils.myLog("ocr-writeBytesToFile" + e.toString());
            }
        }
        final TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
        tessBaseAPI.setImage(bitmap);
//        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "公民身份号码Xx0123456789"); // 识别白名单
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?"); // 识别黑名单
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);//设置识别模式
        final String text = tessBaseAPI.getUTF8Text().trim();
        LibUtils.myLog("ocr", "run: text " + System.currentTimeMillis() + text);
        // 去除中文
        Pattern pat = Pattern.compile(REGEX_CHINESE);
        Matcher mat = pat.matcher(text);
        String mResultText = mat.replaceAll("").trim().replaceAll(" ", "");
        LibUtils.myLog("ocr", "处理后的-" + mResultText);

        Message envelop = new Message();
        envelop.what = ACTION_OCR;
        envelop.obj = mResultText;
        mOcrThreadHandler.sendMessage(envelop);
        bitmap.recycle();
        bitmap = null;
        tessBaseAPI.clear();
        tessBaseAPI.end();
    }

    /**
     * 截取身份证号码位置
     */
    public static Bitmap ImageCropWithRectId(Bitmap bitmap) {
        bitmap = ImageCropWithRectIdCard(bitmap);
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int nw, nh, retX, retY;
        if (w > h) {
            nw = (int) (bitmap.getWidth());
            nh = (int) (bitmap.getHeight() * 0.12 + 0.5f);
            retX = (int) (0);
            retY = (int) (h * 0.800);
        } else {
            nw = w / 2;
            nh = w;
            retX = w / 4;
            retY = (h - w) / 2;
        }
        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

    /**
     * 截取身份证照片位置
     */
    private static Bitmap ImageCropWithRectIdCard(Bitmap bitmap) {
        bitmap = bitmap2Gray(bitmap);
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int nw, nh, retX, retY;
        if (w > h) {
            nw = (int) (bitmap.getHeight() * 1);
            nh = (int) (bitmap.getHeight() * 0.63 + 0.5f);
            retX = (int) (0.138 * w);
            retY = (int) (0.185 * h);
        } else {
            nw = w / 2;
            nh = w;
            retX = w / 4;
            retY = (h - w) / 2;
        }

        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

    /**
     * 截取姓名位置
     */
    public static Bitmap ImageCropWithRectName(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int nw, nh, retX, retY;
        if (w > h) {
            nw = (int) (w * 0.16);
            nh = (int) (h * 0.10);
            retX = (int) (w * 0.18);
            retY = (int) (h * 0.13);
        } else {
            nw = w / 2;
            nh = w;
            retX = w / 4;
            retY = (h - w) / 2;
        }

        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (bitmap != null && !bitmap.equals(bmp) && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

    /**
     * assets转换成file
     */
    private static void writeBytesToFile(InputStream is, File file) throws IOException {
        FileOutputStream fos = null;
        try {
            byte[] data = new byte[2048];
            int nbread = 0;
            fos = new FileOutputStream(file);
            while ((nbread = is.read(data)) > -1) {
                fos.write(data, 0, nbread);
            }
        } catch (Exception ex) {
            LibUtils.myLog("ocr-writeBytesToFile" + ex.toString());
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    //图像灰度处理
    private static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;

    }
}
