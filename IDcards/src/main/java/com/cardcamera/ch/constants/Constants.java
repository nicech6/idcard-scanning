package com.cardcamera.ch.constants;

public class Constants {
    public static class CheckIdCardActivity {
        //拍摄身份证类型
        public static final String ID_CARD_TYPE = "ID_CARD_TYPE";
        //拍摄身份证正面
        public static final String ID_CAARD_FRONT = "ID_CAARD_FRONT";
        //拍摄身份证背面
        public static final String ID_CAARD_BACK = "ID_CAARD_BACK";
        //输入身份证号码
        public static final String INPUT_ID_CAARD = "INPUT_ID_CAARD";

        //上传身份证成功
        public static final String CHECK_ID_CARD_SUCCESS = "CHECK_ID_CARD_SUCCESS";

        //上传身份证状态
        public static final String ID_CARD_STATE = "ID_CARD_STATE";

        public static final String CAMERA_OCR = "CAMERA_OCR";
    }

    public static class CameraActivity {
        public interface KEY {
            String IMG_PATH = "IMG_PATH";
            String PIC_WIDTH = "PIC_WIDTH";
            String PIC_HEIGHT = "PIC_HEIGHT";
        }

        public interface REQUEST_CODE {
            int CAMERA = 0;
        }

        public interface RESULT_CODE {
            int RESULT_OK = -1;
            int RESULT_CANCELED = 0;
            int RESULT_ERROR = 1;
        }
    }
}
