package com.acmenxd.recyclerview.demo;

import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acmenxd.recyclerview.AdapterUtils;
import com.acmenxd.recyclerview.LoadMoreView;
import com.acmenxd.recyclerview.MultiItemTypeAdapter;
import com.acmenxd.recyclerview.MultiItemTypeSwipeMenuAdapter;
import com.acmenxd.recyclerview.SimpleAdapter;
import com.acmenxd.recyclerview.decoration.LinearLayoutDecoration;
import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.listener.AddItemListener;
import com.acmenxd.recyclerview.listener.ItemCallback;
import com.acmenxd.recyclerview.listener.ItemDragCallback;
import com.acmenxd.recyclerview.listener.ItemSwipeCallback;
import com.acmenxd.recyclerview.listener.OnEmptyListener;
import com.acmenxd.recyclerview.listener.OnLoadMoreListener;
import com.acmenxd.recyclerview.listener.OnSwipeMenuListener;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuView;
import com.acmenxd.recyclerview.wrapper.EmptyWrapper;
import com.acmenxd.recyclerview.wrapper.HeaderAndFooterWrapper;
import com.acmenxd.recyclerview.wrapper.LoadMoreWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/6 17:35
 * @detail 包含了所有集成功能,所以篇幅有点长
 */
public class MainActivity extends AppCompatActivity {
    private RecyclerView rv;
    private List<Data> datas = new ArrayList<>();
    private SwipeRefreshLayout srl;
    private EmptyWrapper mEmptyWarpper;
    private LoadMoreWrapper mLoadMoreWarpper;
    private HeaderAndFooterWrapper mHeaderAndFooterWrapper;
    private MultiItemTypeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("RecyclerView功能集封装");
        setContentView(R.layout.activity_main);

        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        rv = (RecyclerView) findViewById(R.id.rv);

        // 数据
        addData();

        //设置刷新时动画的颜色
        srl.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        srl.setProgressBackgroundColorSchemeColor(Color.YELLOW);
        srl.setSize(SwipeRefreshLayout.DEFAULT);//SwipeRefreshLayout.LARGE
        srl.setProgressViewEndTarget(true, 200);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                datas.clear();
                addNewData();
                refreshAdapter();
                srl.setRefreshing(false);
            }
        });

        //设置布局管理器
        LinearLayoutManager manager1 = new LinearLayoutManager(this);
        GridLayoutManager manager2 = new GridLayoutManager(this, 3);
        StaggeredGridLayoutManager manager3 = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        manager1.setOrientation(LinearLayoutManager.VERTICAL);
        manager2.setOrientation(GridLayoutManager.VERTICAL);
        manager3.setOrientation(StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(manager1);
        //设置分隔线
        rv.addItemDecoration(new LinearLayoutDecoration(this));
//        rv.addItemDecoration(new GridLayoutDecoration(this));
//        rv.addItemDecoration(new StaggeredGridLayoutDecoration(this));
        //设置增加或删除条目的动画
        rv.setItemAnimator(new DefaultItemAnimator());
        /**
         * 设置item监听 -> 单击&长按&滑动删除&拖拽变换
         */
        new AddItemListener(rv,
                new ItemCallback() {
                    @Override
                    public void onClick(RecyclerView.ViewHolder viewHolder, int dataPosition) {
                        showToast("item:" + dataPosition);
                    }

                    @Override
                    public void onLongClick(RecyclerView.ViewHolder viewHolder, int dataPosition) {
                        showToast("longClick:" + dataPosition);
                    }
                },
                new ItemSwipeCallback() {
                    @Override
                    public boolean onDeleteData(RecyclerView.ViewHolder viewHolder, int dataPosition, int viewPosition) {
                        datas.remove(dataPosition);
                        return true;
                    }

                    @Override
                    public boolean onDeleteCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
                        if (dataPosition < 5) {
                            return false;
                        }
                        return super.onDeleteCheck(viewHolder, dataPosition);
                    }
                },
                new ItemDragCallback() {
                    @Override
                    public boolean onTransformData(RecyclerView.ViewHolder fromViewHolder, RecyclerView.ViewHolder toViewHolder, int fromDataPosition, int toDataPosition, int fromViewPosition, int toViewPosition) {
                        datas.add(toDataPosition, datas.remove(fromDataPosition));
                        return true;
                    }

                    @Override
                    public boolean onTransformCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
                        if (dataPosition < 2) {
                            return false;
                        }
                        return super.onTransformCheck(viewHolder, dataPosition);
                    }

                    @Override
                    public boolean onTransformToCheck(RecyclerView.ViewHolder viewHolder, int dataPosition) {
                        if (dataPosition < 2) {
                            return false;
                        }
                        return super.onTransformToCheck(viewHolder, dataPosition);
                    }

                    @Override
                    public void onSelectedStart(RecyclerView.ViewHolder viewHolder) {
                        super.onSelectedStart(viewHolder);
                        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                    }

                    @Override
                    public void onSelectedEnd(RecyclerView.ViewHolder viewHolder) {
                        super.onSelectedEnd(viewHolder);
                        viewHolder.itemView.setBackgroundResource(0);
                    }
                }
        );
        // 设置Adapter
        mAdapter = new SimpleAdapter(this, rv, R.layout.activity_recycler_item, datas) {
            @Override
            public void convert(ViewHolder viewHolder, Object item, int dataPosition) {
            }
        };
        // 多item类型Adapter
        mAdapter = new MultiItemTypeAdapter(this, rv, datas);
        // 多item类型&侧滑菜单 Adapter
        mAdapter = new MultiItemTypeSwipeMenuAdapter(this, rv, datas, new OnSwipeMenuListener() {
            @Override
            public int[] getLeftMenuLayoutIds(int dataPosition) {
                if (dataPosition == 3) {
                    return new int[]{R.layout.activity_recycler_swipe_menu_right
                            , R.id.menu_1, R.id.menu_2, R.id.menu_3};
                }
                return new int[]{R.layout.activity_recycler_swipe_menu_left
                        , R.id.menu_1, R.id.menu_2};
            }

            @Override
            public int[] getRightMenuLayoutIds(int dataPosition) {
                return new int[]{R.layout.activity_recycler_swipe_menu_right
                        , R.id.menu_1, R.id.menu_2, R.id.menu_3};
            }

            @Override
            public boolean onMenuItemClick(int dataPosition, int menuItemLayoutId, int direction) {
                String dirStr = "左边菜单";
                if (direction == SwipeMenuView.RIGHT_DIRECTION) {
                    dirStr = "右边菜单";
                }
                switch (menuItemLayoutId) {
                    case R.id.menu_1:
                        showToast("position:" + dataPosition + dirStr + "的第一个menu");
                        break;
                    case R.id.menu_2:
                        showToast("position:" + dataPosition + dirStr + "的第二个menu");
                        break;
                    case R.id.menu_3:
                        showToast("position:" + dataPosition + dirStr + "的第三个menu");
                        break;
                }
                return true;
            }
        });
        mAdapter.addItemViewDelegate(new ItemDelegate<Data>() {
            @Override
            public int getItemViewLayoutId() {
                return R.layout.activity_recycler_item;
            }

            @Override
            public boolean isItemViewType(Data data, int dataPosition) {
                return data.type == 1;
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
        // Header&Footer Adapter
        mHeaderAndFooterWrapper = new HeaderAndFooterWrapper(rv, mAdapter);
        TextView t1 = new TextView(this);
        t1.setText("Header 1\n\nHeader 1");
        TextView t2 = new TextView(this);
        t2.setText("Header 2\n\nHeader 2");
        mHeaderAndFooterWrapper.addHeaderView(t1);
        mHeaderAndFooterWrapper.addHeaderView(t2);
        TextView t3 = new TextView(this);
        t3.setText("Footer 1\n\nFooter 1");
        TextView t4 = new TextView(this);
        t4.setText("Footer 2\n\nFooter 2");
        mHeaderAndFooterWrapper.addFootView(t3);
        mHeaderAndFooterWrapper.addFootView(t4);
        // LoadMore Adapter
        mLoadMoreWarpper = new LoadMoreWrapper(rv, mHeaderAndFooterWrapper, new OnLoadMoreListener() {
            @Override
            public void onLoadMore(View itemView) {
                loadMore(itemView);
            }

            @Override
            public void onLoadMoreClick(View itemView) {
                loadMore(itemView);
            }
        });
        // 提前2条加载下一次数据
        mLoadMoreWarpper.setRefreshBefore(2);
        // 空数据 Adapter
        TextView t6 = new TextView(this);
        t6.setText("无数据,点击加载\n\n无数据,点击加载");
        mEmptyWarpper = new EmptyWrapper(rv, mLoadMoreWarpper, t6, new OnEmptyListener() {
            @Override
            public void onEmptyClick(View itemView) {
                loadMore(itemView);
            }
        });
        // 设置Adapter
        rv.setAdapter(mEmptyWarpper);
    }

    public void addData() {
        int count = datas.size();
        for (int i = count, len = count + 15; i < len; i++) {
            datas.add(new Data("name", i + 1, randomByMinMax(1, 2)));
        }
    }

    public void addNewData() {
        int count = datas.size();
        for (int i = count, len = count + 15; i < len; i++) {
            datas.add(new Data("new name", i + 1, randomByMinMax(1, 2)));
        }
    }

    public void loadMore(View itemView) {
        if (mAdapter.getItemCount() >= 50) {
            ((LoadMoreView) itemView).showFinish();
            itemView.setEnabled(false);
        } else {
            ((LoadMoreView) itemView).showLoading();
            addData();
            refreshAdapter();
            showToast("加载更多");
        }
    }

    public void refreshAdapter() {
        /**
         * 在Adapter.onBindViewHolder()中调用notifyDataSetChanged()会使程序崩溃
         * mEmptyWarpper.notifyDataSetChanged();
         */
        AdapterUtils.notifyDataSetChanged(rv, mEmptyWarpper);
    }

    /**
     * 吐司
     */
    public void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取一个随机数
     * * 包含 min 和 max
     */
    public static int randomByMinMax(int min, int max) {
        return new Random().nextInt(max + 1 - min) + min;
    }

    class Data {
        String name;
        int index;
        int type;

        public Data(String name, int index, int type) {
            this.name = name;
            this.index = index;
            this.type = type;
        }
    }

}
