package com.acmenxd.recyclerview.group;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/4/14 11:57
 * @detail RecyclerView -> 分组监听器
 */
public interface GroupListener {
    /**
     * GroupItem的显示位置
     */
    // orientation == OrientationHelper.VERTICAL 时使用
    int ITEM_TOP = 0; //内侧顶部
    int ITEM_OUT_TOP = 1; //外侧顶部
    // orientation == OrientationHelper.HORIZONTAL 时使用
    int ITEM_LEFT = 2; //内侧左部
    int ITEM_OUT_LEFT = 3; //外侧左部

    /**
     * 获取GroupItem层级的数量
     */
    int getGroupItemLevelNum();

    /**
     * 判断GroupItem的视图类型是否大于一种(当Level等级大于1时,此值不在有效)
     */
    boolean isGroupItemTypeMoreOne();

    /**
     * 设置Head是否自动与GroupItemView宽高同步
     */
    boolean isAutoSetGroupHeadViewWidthHeightByGroupItemView();

    /**
     * 判断是否创建GroupItemView
     *
     * @param dataPosition 定位数据的position
     */
    boolean isCreateGroupItemView(@IntRange(from = 0) int dataPosition);

    /**
     * 获取GroupItemView视图
     *
     * @param root         容器
     * @param groupLevel   分组层级(计数从0开始)
     * @param dataPosition 定位数据的position
     */
    View getGroupItemView(@NonNull ViewGroup root, @IntRange(from = 0) int groupLevel, @IntRange(from = 0) int dataPosition);

    /**
     * 更新GroupItemView视图
     *
     * @param groupItemView 要更新的groupItemView
     * @param groupLevel    分组层级(计数从0开始)
     * @param dataPosition  定位数据的position
     */
    void changeGroupItemView(@NonNull View groupItemView, @IntRange(from = 0) int groupLevel, @IntRange(from = 0) int dataPosition);

    /**
     * 获取GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     *
     * @param root         容器
     * @param groupLevel   分组层级(计数从0开始)
     * @param dataPosition 定位数据的position
     */
    View getGroupHeadView(@NonNull ViewGroup root, @IntRange(from = 0) int groupLevel, @IntRange(from = 0) int dataPosition);

    /**
     * 更新GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     *
     * @param groupHeadView 要更新的groupHeadView
     * @param groupLevel    分组层级(计数从0开始)
     * @param dataPosition  定位数据的position
     */
    void changeGroupHeadView(@NonNull View groupHeadView, @IntRange(from = 0) int groupLevel, @IntRange(from = 0) int dataPosition);
}
