package com.acmenxd.recyclerview.group;

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
     * 获取GroupItem类型的数量
     */
    int getGroupItemTypeNum();

    /**
     * 获取GroupItem层级的数量
     */
    int getGroupItemLevelNum();

    /**
     * 设置Head是否自动与GroupItemView宽高同步
     */
    boolean isAutoSetGroupHeadViewWidthHeightByGroupItemView();

    /**
     * 是否创建GroupItemView
     *
     * @param dataPosition 定位数据的position
     * @return
     */
    boolean isCreateGroupItemView(int dataPosition);

    /**
     * 获取GroupItemView视图
     *
     * @param root         容器
     * @param dataPosition 定位数据的position
     * @return
     */
    View getGroupItemView(ViewGroup root, int dataPosition);

    /**
     * 更新GroupItemView视图
     *
     * @param groupItemView 要更新的groupItemView
     * @param dataPosition  定位数据的position
     */
    void changeGroupItemView(View groupItemView, int dataPosition);

    /**
     * 获取GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     *
     * @param root         容器
     * @param dataPosition 定位数据的position
     * @return
     */
    View getGroupHeadView(ViewGroup root, int dataPosition);

    /**
     * 更新GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     *
     * @param groupHeadView 要更新的groupHeadView
     * @param dataPosition  定位数据的position
     */
    void changeGroupHeadView(View groupHeadView, int dataPosition);
}
