package com.cardcamera.ch.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cardcamera.ch.camera.CameraActivity;
import com.cardcamera.ch.myapplication.R;


public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_PERMISSIONS = 1;
    //6.0以上权限获取监听器
    private PermissionListener mPermissionListener;
    //申请权限请求码
    private int mRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkMyPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}
                        , REQUEST_PERMISSIONS
                        , new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                ocr();
                            }

                            @Override
                            public void onPermissionDenied() {
                                Toast.makeText(MainActivity.this, "--", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void ocr() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == mRequestCode) {
            if (checkEachPermissionsGranted(grantResults)) {
                if (mPermissionListener != null) {
                    mPermissionListener.onPermissionGranted();
                }
            } else {
                //权限申请被拒绝
                if (mPermissionListener != null) {
                    mPermissionListener.onPermissionDenied();
                }
            }
        }
    }

    public interface PermissionListener {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    /**
     * 检查回调结果
     *
     * @param grantResults
     * @return
     */
    private boolean checkEachPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检察每个权限是否申请
     *
     * @param permissions
     * @return true 需要申请权限,false 已申请权限
     */
    private boolean checkEachSelfPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    /**
     * 申请权限
     *
     * @param permissions 要申请的权限
     * @param requestCode 请求码
     * @param listener    权限回调
     */
    protected void checkMyPermission(String[] permissions, int requestCode, PermissionListener listener) {
        if (permissions.length == 0 || permissions == null) return;
        mPermissionListener = listener;
        mRequestCode = requestCode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkEachSelfPermission(permissions)) {
                //申请权限
                requestPermissions(permissions, requestCode);
            } else { //权限正常
                if (mPermissionListener != null) {
                    mPermissionListener.onPermissionGranted();
                }
            }
        } else { //6.0以下
            if (mPermissionListener != null) {
                mPermissionListener.onPermissionGranted();
            }
        }
    }

}
