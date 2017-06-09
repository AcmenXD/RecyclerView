package com.acmenxd.recyclerview.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2016/11/22 14:36
 * @detail 工具类
 */
public final class RecyclerViewUtils {
    /**
     * 串拼接
     *
     * @param strs 可变参数类型
     * @return 拼接后的字符串
     */
    public static String appendStrs(@NonNull Object... strs) {
        StringBuilder sb = new StringBuilder();
        if (strs != null && strs.length > 0) {
            for (Object str : strs) {
                sb.append(String.valueOf(str));
            }
        }
        return sb.toString();
    }

    /**
     * 根据手机的分辨率从 dp 的单位转成 px(像素)
     */
    public static float dp2px(@NonNull Context pContext, @FloatRange(from = 0) float dp) {
        return dp2px(pContext.getResources(), dp);
    }

    public static float dp2px(@NonNull Resources resources, @FloatRange(from = 0) float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    /**
     * 根据手机的分辨率从 px(像素)的单位转成 dp
     */
    public static float px2dp(@NonNull Context pContext, @FloatRange(from = 0) float px) {
        return px2dp(pContext.getResources(), px);
    }

    public static float px2dp(@NonNull Resources resources, @FloatRange(from = 0) float px) {
        final float scale = resources.getDisplayMetrics().density;
        return px / scale + 0.5f;
    }

    /**
     * 根据手机的分辨率从 sp 的单位转成 px(像素)
     */
    public static float sp2px(@NonNull Context pContext, @FloatRange(from = 0) float sp) {
        return sp2px(pContext.getResources(), sp);
    }

    public static float sp2px(@NonNull Resources resources, @FloatRange(from = 0) float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale + 0.5f;
    }

    /**
     * 根据手机的分辨率从 px(像素)的单位转成 sp
     */
    public static float px2sp(@NonNull Context pContext, @FloatRange(from = 0) float px) {
        return px2sp(pContext.getResources(), px);
    }

    public static float px2sp(@NonNull Resources resources, @FloatRange(from = 0) float px) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return px / scale + 0.5f;
    }
}
