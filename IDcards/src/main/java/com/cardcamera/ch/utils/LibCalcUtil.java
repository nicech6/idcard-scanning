package com.cardcamera.ch.utils;

import android.content.Context;

public class LibCalcUtil {
    private LibCalcUtil() {
    }

    public static float dp2px(Context Context, int dp) {
        final float scale = Context.getResources().getDisplayMetrics().densityDpi;
        return dp * (scale / 160) + 0.5f;
    }

    public static float dx2dp(Context Context, int px) {
        final float scale = Context.getResources().getDisplayMetrics().densityDpi;
        return (px * 160) / scale + 0.5f;
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
