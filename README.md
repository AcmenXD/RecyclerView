# RecyclerView

RecyclerView功能集封装

如要了解功能实现,请运行app程序查看控制台日志和源代码!
* 源代码 : <a href="https://github.com/AcmenXD/RecyclerView">AcmenXD/RecyclerView</a>
* apk下载路径 : <a href="https://github.com/AcmenXD/Resource/blob/master/apks/RecyclerView.apk">RecyclerView.apk</a>

![gif](https://github.com/AcmenXD/RecyclerView/blob/master/pic/1.gif)
![gif](https://github.com/AcmenXD/RecyclerView/blob/master/pic/2.gif)
![gif](https://github.com/AcmenXD/RecyclerView/blob/master/pic/3.gif)
![gif](https://github.com/AcmenXD/RecyclerView/blob/master/pic/4.gif)
![gif](https://github.com/AcmenXD/RecyclerView/blob/master/pic/5.gif)

### 依赖
---
- AndroidStudio
```
	allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
	}
```
```
	 // Android系统提供的recyclerview-v7包
	 compile 'com.android.support:recyclerview-v7:25.0.0'
	 compile 'com.github.AcmenXD:RecyclerView:2.0'
```
### 功能
---
####v1.8 修复问题(修复问题较多,建议升到1.8版本使用):
- Adatper无数据时,导致GroupHeadLayout计算时出现OOM问题
- LoadMoreView初始化状态,ui显示异常问题
- Adatper.mDatas对象变化后,导致更新ui异常的问题
- Adatper新增setDatas函数, 方便更新数据

####v1.5 新增功能有:
- 调整兼容版本,支持4.0(含)以上系统

####v1.4 支持功能如下
- 支持多层级分组功能(支持垂直|水平布局)(支持LinearLayoutManager/GridLayoutManager/StaggeredGridLayoutManager)
- 优化item各种事件
- 特别说明:多层级分组的GroupHeadLayout&GroupItemLayout暂不支持Margin及Padding设置,显示效果会有影响

####v1.3 支持功能如下
- 支持分组功能
- 支持分组头布局悬浮RecyclerView顶部功能

####v1.0 支持功能如下
- 支持下拉刷新
- 支持LoadMore(上拉加载更多)
- 支持添加Header、Footer、Empty(头、尾、空)视图
- 支持一个Adapter自定义多种Item类型
- 简化RecyclerView.Adapter及ViewHolder的实现
- LoadMore 和 Empty支持点击回调
- Adapter链式调用，易读、易懂、易用
- 支持item事件：单击 & 长按 & 滑动删除 & 拖拽换位 & 侧滑菜单功能（事件无任何冲突）
- 此封装库未对RecyclerView进行任何更改,布局或代码中使用原生RecyclerView即可
### 使用 -> 以下代码 注释很详细、很重要很重要很重要!!!
---
- xml布局
```java
// 定义下拉刷新控件SwipeRefreshLayout 及 RecyclerView列表控件
<android.support.v4.widget.SwipeRefreshLayout
    android:id="@+id/srl"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

</android.support.v4.widget.SwipeRefreshLayout>
```
- 初始RecyclerView
```java
/*
 * recyclerView 需设置布局管理器
 * * 通常只需要一个管理器即可,由于演示代码,固配置了所有管理器
 */
RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
// 线性布局
LinearLayoutManager manager1 = new LinearLayoutManager(this);
// 网格布局  参数:1.上下文对象  2.设置 列/行 数
GridLayoutManager manager2 = new GridLayoutManager(this, 3);
// 瀑布流布局 参数:1.设置 列/行 数  2.横/纵 向排列
StaggeredGridLayoutManager manager3 = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
// 设置管理器的 横/纵 向排列
manager1.setOrientation(OrientationHelper.VERTICAL);
manager2.setOrientation(OrientationHelper.VERTICAL);
manager3.setOrientation(OrientationHelper.VERTICAL);
// 将管理器绑定到recyclerView
rv.setLayoutManager(manager1);
// 设置item之间的分隔线(默认提供三种分隔线,对应三种局部.如有其它需求,可自行实现)
rv.addItemDecoration(new LinearLayoutDecoration(this));
// rv.addItemDecoration(new GridLayoutDecoration(this));
// rv.addItemDecoration(new StaggeredGridLayoutDecoration(this));
// 设置增加或删除item项的动画
rv.setItemAnimator(new DefaultItemAnimator());
```
### 下拉刷新
```java
/**
 * 下载刷新用系统提供的SwipeRefreshLayout,并未使用PullToRefresh
 */
SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.srl);
// 设置刷新控件转圈圈的动画颜色,每转一圈一个颜色
srl.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
// 设置转圈圈的背景色
srl.setProgressBackgroundColorSchemeColor(Color.YELLOW);
// 设置转圈圈的大小,默认是DEFAULT
srl.setSize(SwipeRefreshLayout.DEFAULT);//SwipeRefreshLayout.LARGE
// 参数:1.下拉圈圈是否缩放  2.圈圈下拉的高度
srl.setProgressViewEndTarget(true, 200);
// 设置刷新的事件监听器
srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
    @Override
    public void onRefresh() {
        datas.clear(); // 清理输出
        addNewData(); // 添加新数据
        refreshAdapter(); // 刷新Adapter
        srl.setRefreshing(false); // 关闭圈圈
    }
});
```
### Adapter - 单类型Item
```java
/**
 * 创建SimpleAdapter
 * 泛型:数据的类型
 * 参数:1.上下文对象  2.recyclerview实例  3.item布局  4.数据集List
 */
SimpleAdapter mAdapter = new SimpleAdapter<Data>(this, rv, R.layout.activity_recycler_item, datas) {
    @Override
    public void convert(ViewHolder viewHolder,Data item, int dataPosition) {
	    // 刷新界面 viewHolder-控件集  item-数据  dataPosition-位置
	    // getView(rId)是viewHolder实现的方法,此方式获取控件无需再强转类型
	    TextView tv = viewHolder.getView(R.id.activity_recycler_item_tv);
            tv.setText(item.name);
    }
};
```
### Adapter - 多类型Item
```java
/**
 * 创建MultiItemTypeAdapter
 * 泛型:数据的类型
 * 参数:1.上下文对象  2.recyclerview实例  3.数据集List
 */
MultiItemTypeAdapter mAdapter = new MultiItemTypeAdapter<Data>(this, rv, datas);
// 添加一种item类型(有几种类型,添加几个)
mAdapter.addItemViewDelegate(new ItemDelegate<Data>() {
    @Override
    public int getItemViewLayoutId() {
	// 此种类的item需要的布局文件
	return R.layout.activity_recycler_item;
    }
    @Override
    public boolean isItemViewType(Data data, int dataPosition) {
	// 根据数据或位置判断是否用此种类型的item,返回bool类型
	return data.type == 1;
    }
    @Override
    public void convert(ViewHolder viewHolder, Data data, int dataPosition) {
	// 刷新界面 viewHolder-控件集  item-数据  dataPosition-位置
	// getView(rId)是viewHolder实现的方法,此方式获取控件无需再强转类型
        TextView tv = viewHolder.getView(R.id.activity_recycler_item_tv);
        TextView tv_age = viewHolder.getView(R.id.activity_recycler_item_tv_age);
        TextView tv_type = viewHolder.getView(R.id.activity_recycler_item_tv_type);
        tv.setText(data.name);
        tv_age.setText("index:" + data.index);
        tv_type.setText("类型:" + data.type);
    }
});
// 添加第二种item类型(有几种类型,添加几个)
mAdapter.addItemViewDelegate(new ItemDelegate<Data>() {
    @Override
    public int getItemViewLayoutId() {
        return R.layout.activity_recycler_item2;
    }
    @Override
    public boolean isItemViewType(Data data, int dataPosition) {
        return data.type == 2;
    }
    @Override
    public void convert(ViewHolder viewHolder, Data data, int dataPosition) {
        TextView tv = viewHolder.getView(R.id.activity_recycler_item_tv);
        TextView tv_age = viewHolder.getView(R.id.activity_recycler_item_tv_age);
        TextView tv_type = viewHolder.getView(R.id.activity_recycler_item_tv_type);
        tv.setText(data.name);
        tv_age.setText("index:" + data.index);
        tv_type.setText("类型:" + data.type);
    }
});
```
### Adapter - 单类型+侧滑菜单的Item
```java
/**
 * 创建SimpleSwipeMenuAdapter
 * 泛型:数据的类型
 * 参数:1.上下文对象  2.recyclerview实例  3.item布局  4.数据集List  5.侧滑菜单需要的监听器
 */
SimpleSwipeMenuAdapter mAdapter = new SimpleSwipeMenuAdapter<Data>(this, rv, R.layout.activity_recycler_item, datas, new OnSwipeMenuListener() {
    @Override
    public int[] getLeftMenuLayoutIds(int dataPosition) {
	// item左侧菜单(如不需要,return null即可)
	// 根据位置判断要显示的菜单布局及菜单项
        if (dataPosition == 3) {
	    // 返回一个数组new int[]{菜单布局,菜单项1,菜单项2,菜单项3}
            return new int[]{R.layout.activity_recycler_swipe_menu_right
                    , R.id.menu_1, R.id.menu_2, R.id.menu_3};
        }
        // 返回一个数组new int[]{菜单布局,菜单项1,菜单项2}
        return new int[]{R.layout.activity_recycler_swipe_menu_left
                , R.id.menu_1, R.id.menu_2};
    }
    @Override
    public int[] getRightMenuLayoutIds(int dataPosition) {
        // 右侧菜单同左侧菜单(如不需要,return null即可)
        return new int[]{R.layout.activity_recycler_swipe_menu_right
                , R.id.menu_1, R.id.menu_2, R.id.menu_3};
    }
    @Override
    public boolean onMenuItemClick(int dataPosition, int menuItemLayoutId, int direction) {
	// 菜单项点击回调
	// 函数参数:1.dataPosition列表的位置 2.menuItemLayoutId菜单项Id 3.左/右测菜单
        String dirStr = "左边菜单";
        if (direction == SwipeMenuView.RIGHT_DIRECTION) {
            dirStr = "右边菜单";
        }
        switch (menuItemLayoutId) {
            case R.id.menu_1:
                ToastUtils.show("position:" + dataPosition + dirStr + "的第一个menu");
                break;
            case R.id.menu_2:
                ToastUtils.show("position:" + dataPosition + dirStr + "的第二个menu");
                break;
            case R.id.menu_3:
                ToastUtils.show("position:" + dataPosition + dirStr + "的第三个menu");
                break;
        }
        return true;
    }
}) {
    @Override
    public void convert(ViewHolder viewHolder, Data item, int dataPosition) {
	// 刷新界面 viewHolder-控件集  item-数据  dataPosition-位置
    }
};
```
### Adapter - 多类型+侧滑菜单的Item
```java
/**
 * 创建MultiItemTypeSwipeMenuAdapter
 * 泛型:数据的类型
 * 参数:1.上下文对象  2.recyclerview实例  3.数据集List  4.侧滑菜单需要的监听器
 * 菜单创建和使用方式同上 : Adapter - 单类型+侧滑菜单的Item
 */
mAdapter = new MultiItemTypeSwipeMenuAdapter(this, rv, datas, new OnSwipeMenuListener() {
    @Override
    public int[] getLeftMenuLayoutIds(int dataPosition) {
        return new int[0];
    }
    @Override
    public int[] getRightMenuLayoutIds(int dataPosition) {
        return new int[0];
    }
    @Override
    public boolean onMenuItemClick(int dataPosition, int menuItemLayoutId, int direction) {
        return false;
    }
});
// 添加一种item类型(有几种类型,添加几个)
mAdapter.addItemViewDelegate(new ItemDelegate<Data>() {});
// 添加第二种item类型(有几种类型,添加几个)
mAdapter.addItemViewDelegate(new ItemDelegate<Data>() {});
```
### Header、Footer视图
```java
/*
 * 创建HeaderAndFooterWrapper
 * 参数:1.recyclerview实例  2.Adapter实例(链式Adapter,最底层为RecyclerView.Adapter,其次为自定义Wrapper)
 */
HeaderAndFooterWrapper mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(rv, mAdapter);
// HeaderView1
TextView t1 = new TextView(this);
t1.setText("Header 1\n\nHeader 1");
// HeaderView2
TextView t2 = new TextView(this);
t2.setText("Header 2\n\nHeader 2");
// HeaderView添加到列表中
mHeaderAndFooterWrapper.addHeaderView(t1);
mHeaderAndFooterWrapper.addHeaderView(t2);
// FooterView1
TextView t3 = new TextView(this);
t3.setText("Footer 1\n\nFooter 1");
// FooterView2
TextView t4 = new TextView(this);
t4.setText("Footer 2\n\nFooter 2");
// FooterView添加到列表中
mHeaderAndFooterWrapper.addFooterView(t3);
mHeaderAndFooterWrapper.addFooterView(t4);
```
### Empty视图
```java
/*
 * 创建EmptyWrapper
 * 参数:1.recyclerview实例  2.Adapter实例(链式Adapter,最底层为RecyclerView.Adapter,其次为自定义Wrapper) 3.EmptyView 4.Empty视图点击事件监听器
 */
// EmptyView
TextView t6 = new TextView(this);
t6.setText("无数据,点击加载\n\n无数据,点击加载");
EmptyWrapper mEmptyWarpper = new EmptyWrapper(rv, mHeaderAndFooterWrapper, t6, new OnEmptyListener() {
    @Override
    public void onEmptyClick(View itemView) {
	// 点击回调
        loadMore(itemView);
    }
});
```
### 上拉加载更多
```java
/*
 * 创建LoadMoreWrapper
 * 参数:1.recyclerview实例  2.Adapter实例(链式Adapter,最底层为RecyclerView.Adapter,其次为自定义Wrapper) 3.LoadMore视图点击事件监听器
 * 支持自定义LoadMore视图
 */
LoadMoreWrapper mLoadMoreWarpper = new LoadMoreWrapper(rv, mHeaderAndFooterWrapper, new OnLoadMoreListener() {
    @Override
    public void onLoadMore(View itemView) {
	// 上拉加载更多回调
        loadMore(itemView);
    }

    @Override
    public void onLoadMoreClick(View itemView) {
	// LoadMore单击回调
        loadMore(itemView);
    }
});
// 提前2条加载下一次数据
mLoadMoreWarpper.setRefreshBefore(2);
```
### 事件监听器(单击&长按 & 滑动删除 & 拖拽变换)
```java
/*
 * 需创建AddItemListener统一管理各个监听器,避免出现各事件冲突
 * 如不需要实现的功能,监听器可以传null
 * 参数:1.recyclerview实例  2.单击&长按 监听  3.滑动删除 监听  4.拖拽变换 监听
 */
new AddItemListener(rv, new ItemCallback() {
    @Override
    public void onClick(RecyclerView.ViewHolder viewHolder, int dataPosition) {
	// 单击回调
        ToastUtils.show("item:" + dataPosition);
    }
    @Override
    public void onLongClick(RecyclerView.ViewHolder viewHolder, int dataPosition) {
	//长按回调
        ToastUtils.show("longClick:" + dataPosition);
    }
}, new ItemSwipeCallback() {
    @Override
    public boolean onDeleteData(RecyclerView.ViewHolder viewHolder, int dataPosition, int viewPosition) {
	// 滑动删除回调,需手动处理数据
        datas.remove(dataPosition);
        // 返回值:true表示自动删掉item视图  false表示要手动处理视图
        return true;
    }
    @Override
    public boolean onDeleteCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
	// 返回值:true表示此item项支持侧滑删除功能 false表示不支持侧滑删除
        if (dataPosition < 5) {
            return false;
        }
        return super.onDeleteCheck(viewHolder, dataPosition);
    }
}, new ItemDragCallback() {
    @Override
    public boolean onTransformData(RecyclerView.ViewHolder fromViewHolder, RecyclerView.ViewHolder toViewHolder, int fromDataPosition, int toDataPosition, int fromViewPosition, int toViewPosition) {
	// 变换回调,需手动处理数据(变换数据位置)
        datas.add(toDataPosition, datas.remove(fromDataPosition));
        // 返回值:true表示自动删掉并添加item视图  false表示要手动处理视图
        return true;
    }
    @Override
    public boolean onTransformCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
        // 返回值:true表示此item项支持长按拖动功能 false表示不支持长按拖动功能
        // 此回调为起始位回调,非被换位置的item项
        if (dataPosition < 2) {
            return false;
        }
        return super.onTransformCheck(viewHolder, dataPosition);
    }
    @Override
    public boolean onTransformToCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
        // 返回值:true表示此item项允许被换位置 false表示不允许
        if (dataPosition < 2) {
            return false;
        }
        return super.onTransformToCheck(viewHolder, dataPosition);
    }
    @Override
    public void onSelectedStart(RecyclerView.ViewHolder viewHolder) {
        // 拖动开始生效时回调
        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
    }
    @Override
    public void onSelectedEnd(RecyclerView.ViewHolder viewHolder) {
        // 拖动结束时回调
        viewHolder.itemView.setBackgroundResource(0);
    }
});
```
### 分组 + 悬浮
```java
/*
 * 创建分组回调监听
 */
GroupListener mGroupListener = new GroupListener() {
    /**
     * 获取GroupItem层级的数量
    */
    @Override
    public int getGroupItemLevelNum() {
        return 4;
    }
    /**
     * 判断GroupItem的视图类型是否大于一种(当Level等级大于1时,此值不在有效)
     */
    @Override
    public boolean isGroupItemTypeMoreOne() {
        return false;
    }
    /**
     * 设置Head是否自动与GroupItemView宽高同步
     */
    @Override
    public boolean isAutoSetGroupHeadViewWidthHeightByGroupItemView() {
        return false;
    }
    /**
     * 判断是否创建GroupItemView
     * @param dataPosition 定位数据的position
     */
    @Override
    public boolean isCreateGroupItemView(int dataPosition) {
        if (datas.get(dataPosition).type == 3) {
            return true;
        }
        return false;
    }
    /**
     * 获取GroupItemView视图
     * @param root         容器
     * @param groupLevel   分组层级(计数从0开始)
     * @param dataPosition 定位数据的position
     */
    @Override
    public View getGroupItemView(ViewGroup root, int groupLevel, int dataPosition) {
        View view = null;
        if (datas.get(dataPosition).type == 3) {
            if (dataPosition % 15 == 0) {
                switch (groupLevel) {
                    case 0:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item, root, false);
                        break;
                    case 1:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item2, root, false);
                        break;
                    case 2:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item3, root, false);
                        break;
                    case 3:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item4, root, false);
                        break;
                }
            } else {
                switch (groupLevel) {
                    case 1:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item2, root, false);
                        break;
                    case 2:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item3, root, false);
                        break;
                    case 3:
                        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_recycler_group_item4, root, false);
                        break;
                }
            }
        }
        return view;
    }
    /**
     * 更新GroupItemView视图
     * @param groupItemView 要更新的groupItemView
     * @param groupLevel    分组层级(计数从0开始)
     * @param dataPosition  定位数据的position
     */
    @Override
    public void changeGroupItemView(View groupItemView, int groupLevel, int dataPosition) {
        TextView tv = (TextView) groupItemView.findViewById(R.id.activity_recycler_group_item_tv_number);
        tv.setText("dataPosition:" + dataPosition + "  groupLevel:" + groupLevel);
    }
    /**
     * 获取GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     * @param root         容器
     * @param groupLevel   分组层级(计数从0开始)
     * @param dataPosition 定位数据的position
     */
    @Override
    public View getGroupHeadView(ViewGroup root, int groupLevel, int dataPosition) {
        return getGroupItemView(root, groupLevel, dataPosition);
    }
    /**
     * 更新GroupHeadView视图
     * * 大多数情况下与GroupItemView相同,可互相调用(保留此回调是为了当出现于GroupHeadView不同时,方便拓展)
     * @param groupHeadView 要更新的groupHeadView
     * @param groupLevel    分组层级(计数从0开始)
     * @param dataPosition  定位数据的position
     */
    @Override
    public void changeGroupHeadView(View groupHeadView, int groupLevel, int dataPosition) {
        changeGroupItemView(groupHeadView, groupLevel, dataPosition);
    }
};
/**
 * 创建分组Decoration并设置布局,添加到RecyclerView中,并绑定GroupListener
 */
rv.addItemDecoration(new GroupDecoration((GroupHeadLayout) findViewById(R.id.groupLayout), mGroupListener));
/**
 * 如果RecyclerView为GridLayoutManager或StaggeredGridLayoutManager,则必须设置
 * 兼容Group分组功能,网格或瀑布流,必须设置,否则无法支持Group功能
 */
mAdapter.setGroupListener(mGroupListener);
```
### 打个小广告^_^
**gitHub** : https://github.com/AcmenXD   如对您有帮助,欢迎点Star支持,谢谢~

**技术博客** : http://blog.csdn.net/wxd_beijing
# END
