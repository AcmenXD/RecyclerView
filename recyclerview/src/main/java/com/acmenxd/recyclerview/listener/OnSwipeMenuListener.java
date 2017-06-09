package com.acmenxd.recyclerview.listener;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/9 14:40
 * @detail 滑动菜单事件监听
 */
public abstract class OnSwipeMenuListener {
    /**
     * 返回item左菜单的布局文件Id
     *
     * @return int[]{menu布局Id,menu布局中第 1 个菜单项view的Id,menu布局中第 2 个菜单项view的Id,...}
     * * 如不想添加菜单, 返回null即可
     */
    public abstract int[] getLeftMenuLayoutIds(@IntRange(from = 0) int dataPosition);

    /**
     * 返回item右菜单的布局文件Id
     *
     * @return int[]{menu布局Id,menu布局中第 1 个菜单项view的Id,menu布局中第 2 个菜单项view的Id,...}
     * * 如不想添加菜单, 返回null即可
     */
    public abstract int[] getRightMenuLayoutIds(@IntRange(from = 0) int dataPosition);

    /**
     * 菜单项的单击回调事件
     *
     * @param dataPosition 定位数据的position
     * @param menuItemId   被点击的菜单项Id
     * @param direction    SwipeMenuView.LEFT_DIRECTION:左侧菜单  SwipeMenuView.RIGHT_DIRECTION:右侧菜单
     * @return true:点击后关闭菜单项 false:点击后不关闭菜单项
     */
    public abstract boolean onMenuItemClick(@IntRange(from = 0) int dataPosition, @IdRes int menuItemId, int direction);
}
