package com.cardcamera.ch.camera;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class ScannerClient implements MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mConn;
    private Context mContext;
    private File mFile;
    private String mType;
    private ScanListener mListener;

    public interface ScanListener {
        public void onScanFinish();
    }

    public ScannerClient(Context context, File file, String type, ScanListener l) {
        mContext = context;
        mFile = file;
        mListener = l;
        mType = type;
        mConn = new MediaScannerConnection(context, this);
        mConn.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mConn.scanFile(mFile.getAbsolutePath(), mType);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mConn.disconnect();
        if (mListener != null) {
            mListener.onScanFinish();
        }
    }

}
