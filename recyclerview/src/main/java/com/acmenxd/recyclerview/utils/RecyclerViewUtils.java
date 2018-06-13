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
     * 根据手机的分辨率从 dp 的单位转成 px(像素)
     */
    public static float dp2px(@NonNull Context pContext, @FloatRange(from = 0) float dp) {
        return dp2px(pContext.getResources(), dp);
    }

    public static float dp2px(@NonNull Resources resources, @FloatRange(from = 0) float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}
