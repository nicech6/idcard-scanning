package com.cardcamera.ch.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.cardcamera.ch.R;
import com.cardcamera.ch.constants.Constants;
import com.cardcamera.ch.utils.CameraUtil;
import com.cardcamera.ch.utils.LibBitmapUtil;
import com.cardcamera.ch.utils.LibCalcUtil;
import com.cardcamera.ch.utils.LibUtils;
import com.cardcamera.ch.utils.OcrUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 自定义camera
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    SurfaceView mSurfaceView;
    ImageView mIgCloseCameraActivity;
    ImageView mIgTakePictureCameraActivity;
    RelativeLayout mRlBottomBarCameraActivity;
    ImageView mSdvPreview;
    TextView mIdCardType;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private int mCameraId = 0;
    private int picWidth;
    private int previewPicWidth;
    private boolean isview = false;
    private String mResultPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        initData();
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceView);
        mIgCloseCameraActivity = findViewById(R.id.ig_close_camera_activity);
        mIgTakePictureCameraActivity = findViewById(R.id.ig_take_picture_camera_activity);

        mIgCloseCameraActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResultPath == null) {
//                    //退出
                    finish();
                } else {
                    //正在显示图片，点击重新拍照
                    LibUtils.deleteFile(getApplicationContext(), mResultPath);
                    mResultPath = null;
                    starReviewCamera();
                    mIgTakePictureCameraActivity.setImageResource(R.mipmap.mine_id_ic_camera);
                    mSdvPreview.setImageBitmap(null);
                }
            }
        });
        mIgTakePictureCameraActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mResultPath == null) {
//                    //拍照
                    captrue();
                } else {
                    //选中该照片
//                    EventBus.getDefault().post(new CheckIdCardEvent(mResultPath, mIsIdCardFront, mIsOcr));
                    finish();
                }
            }
        });
        mRlBottomBarCameraActivity = findViewById(R.id.rl_bottom_bar_camera_activity);
        mSdvPreview = findViewById(R.id.sdv_picture_review_camera_activity);
        mIdCardType = findViewById(R.id.tv_top_camera_activity);
    }

    private void initData() {
        screenWidth = LibUtils.getScreenWidth(this);
        screenHeight = LibUtils.getScreenHeight(this);
    }

//    @OnClick({R.id.ig_close_camera_activity, R.id.ig_take_picture_camera_activity})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.ig_close_camera_activity:
//                if (mResultPath == null) {
//                    //退出
//                    finish();
//                } else {
//                    //正在显示图片，点击重新拍照
//                    LibUtils.deleteFile(this, mResultPath);
//                    mResultPath = null;
//                    starReviewCamera();
//                    mIgTakePictureCameraActivity.setImageResource(R.mipmap.mine_id_ic_camera);
//                    mSdvPreview.setImageURI("");
//                }
//                break;
//            case R.id.ig_take_picture_camera_activity:
//                if (mResultPath == null) {
//                    //拍照
//                    captrue();
//                } else {
//                    //选中该照片
////                    EventBus.getDefault().post(new CheckIdCardEvent(mResultPath, mIsIdCardFront, mIsOcr));
//                    finish();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    private void captrue() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                isview = false;
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                //这里我相信大部分都有其他用处把 比如加个水印 后续再讲解
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);
                saveBitmap = Bitmap.createScaledBitmap(saveBitmap, screenHeight, picWidth, true);

                //旋转90度
                Bitmap rotatedBitmap = LibBitmapUtil.rotateBitmapByDegree(saveBitmap, -90);

                //正方形 animHeight(动画高度)
//                saveBitmap = Bitmap.createBitmap(saveBitmap, 0, 0, screenWidth, screenWidth * 4 / 3);

                //拍照图片保存至本地
                final String img_path = Environment.getExternalStorageDirectory() + "/Picture_" + System.currentTimeMillis() + ".jpg";
                String savePath = LibBitmapUtil.saveBitmap(getApplicationContext(), rotatedBitmap, img_path, 100, null);

                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }

                if (!rotatedBitmap.isRecycled()) {
                    rotatedBitmap.recycle();
                }

                Intent intent = new Intent();
                intent.putExtra(Constants.CameraActivity.KEY.IMG_PATH, img_path);
                intent.putExtra(Constants.CameraActivity.KEY.PIC_WIDTH, screenHeight);
                intent.putExtra(Constants.CameraActivity.KEY.PIC_HEIGHT, picWidth);
                setResult(Constants.CameraActivity.RESULT_CODE.RESULT_OK, intent);

                //显示在sdv上
                if (savePath != null && !savePath.isEmpty()) {
                    mResultPath = savePath;
                    mSdvPreview.setImageURI(LibUtils.getFrescoUriForLocalPicture(CameraActivity.this, mResultPath));
                    //暂停实时预览
                    releaseCamera();
                    mIgTakePictureCameraActivity.setImageResource(R.mipmap.mine_id_ic_confirm);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        starReviewCamera();
    }

    /**
     * 开始预览
     */
    private void starReviewCamera() {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                startPreview(mCamera, mHolder);
            }
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(this, mCameraId, camera);

            camera.startPreview();
            isview = true;
            mCamera.setPreviewCallback(this);
//            //对焦回调
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                    @Override
                    public void onAutoFocusMoving(boolean start, Camera camera) {

                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        if (camera == null) return;

        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //如果 preview 和 picture 共有的尺寸
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();

        List<Camera.Size> publicSizes = new ArrayList<>();

//        for (int i = 0; i < supportedPictureSizes.size(); i++) {
//            Camera.Size size = supportedPictureSizes.get(i);
//            LibUtils.myLog("supported Picture size w " + size.width + " h " + size.height);
//        }
//
//        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
//            Camera.Size size = supportedPreviewSizes.get(i);
//            LibUtils.myLog("supported Preview size w " + size.width + " h " + size.height);
//        }
//
//        for (int i = 0; i < publicSizes.size(); i++) {
//            Camera.Size size = publicSizes.get(i);
//            LibUtils.myLog("public size w " + size.width + " h " + size.height);
//        }

        for (Camera.Size size : supportedPreviewSizes) {
            if (supportedPictureSizes.contains(size)) {
                //取出公共的size
                publicSizes.add(size);
            }
        }

        Camera.Size previewSize = null;
        Camera.Size pictrueSize = null;

        if (publicSizes.size() > 0) {
            //有公共的尺寸
            Camera.Size publicSize = CameraUtil.getInstance().getPropSizeForHeight(publicSizes, 800, true);

            parameters.setPreviewSize(publicSize.width, publicSize.height);  //oppo 1280 960
            parameters.setPictureSize(publicSize.width, publicSize.height); //oppo 1536 864

            previewSize = publicSize;
            pictrueSize = publicSize;
            LibUtils.myLog("publicSize w " + publicSize.width + " h " + publicSize.height);

        } else {
            //没有公共的尺寸
            //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
            previewSize = CameraUtil.getInstance().getPropSizeForHeight(supportedPreviewSizes, 800);
            parameters.setPreviewSize(previewSize.width, previewSize.height);  //oppo 1280 960

            pictrueSize = CameraUtil.getInstance().getPropSizeForHeight(supportedPictureSizes, previewSize, 800);
            parameters.setPictureSize(pictrueSize.width, pictrueSize.height); //oppo 1536 864

            LibUtils.myLog("previewSize w " + previewSize.width + " h " + previewSize.height);
            LibUtils.myLog("pictrueSize w " + pictrueSize.width + " h " + pictrueSize.height);
        }

        LibUtils.myLog("screen h " + screenHeight + " w " + screenWidth);

        camera.setParameters(parameters);

        /**
         * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
         * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
         * previewSize.width才是surfaceView的高度
         * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
         */
        //照片宽度
        picWidth = (screenHeight * pictrueSize.width) / pictrueSize.height;
        //预览宽度
        previewPicWidth = (screenHeight * previewSize.width) / previewSize.height;

        //设置底部控件的高度是剩余部分
        initBottomParam(previewPicWidth);

        //设置预览图片尺寸
        ViewGroup.LayoutParams layoutParams = mSdvPreview.getLayoutParams();
        layoutParams.width = picWidth;
        layoutParams.height = screenHeight;
        mSdvPreview.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(previewPicWidth, screenHeight); //横屏下(长，宽)
        //这里当然可以设置拍照位置 比如居中 我这里就置顶了
        mSurfaceView.setLayoutParams(params);
    }

    private void initBottomParam(int previewPicWidth) {
        int width = (int) LibCalcUtil.dp2px(this, 70);
        int barWidth = screenWidth - previewPicWidth;
        if (barWidth > width) {
            RelativeLayout.LayoutParams bottomParam = new RelativeLayout.LayoutParams(barWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            bottomParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            mRlBottomBarCameraActivity.setLayoutParams(bottomParam);
        }
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
        }
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
        }
        mCamera.stopPreview();
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        ByteArrayOutputStream baos;
        byte[] rawImage;
        Bitmap bitmap;
        Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                previewSize.width,
                previewSize.height,
                null);
        baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        rawImage = baos.toByteArray();
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);
        saveBitmap = Bitmap.createScaledBitmap(saveBitmap, screenHeight, picWidth, true);

        //旋转90度
        Bitmap rotatedBitmap = LibBitmapUtil.rotateBitmapByDegree(saveBitmap, -90);

        //正方形 animHeight(动画高度)
//                saveBitmap = Bitmap.createBitmap(saveBitmap, 0, 0, screenWidth, screenWidth * 4 / 3);

        //拍照图片保存至本地
        final String img_path = Environment.getExternalStorageDirectory() + "/Picture_" + System.currentTimeMillis() + ".jpg";
        String savePath = LibBitmapUtil.saveBitmap(getApplicationContext(), rotatedBitmap, img_path, 100, null);
        Log.i("TAG", "" + savePath);

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }

        new OcrUtils(getApplicationContext(), OcrUtils.ImageCropWithRectId(BitmapFactory.decodeFile(savePath)), new OcrUtils.OnOrcResult() {
            @Override
            public void success(final String s) {
                Log.i("TAG", "" + s);
                //退出系统
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TAG", "" + s);
                    }
                });
            }

            @Override
            public void failed(String s) {
            }
        }).start();
    }
}
