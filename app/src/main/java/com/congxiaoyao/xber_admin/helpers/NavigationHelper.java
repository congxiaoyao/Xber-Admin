package com.congxiaoyao.xber_admin.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.FlexibleDividerDecoration;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.functions.Action1;

/**
 * 由于官方提供的navigationView限制很多 没办法实现完全的侧滑栏自定义 但我对使用官方控件又有一种执念..
 * 所以这里用了一点奇技淫巧实现了navigationView自定义布局
 *
 * 上面所说的限制是指对菜单项的限制很多 没办法自己控制布局 但对头部View没有限制
 * 所以这里直接使用头部布局当做整个navigationView的布局 而菜单项为空 但这样就出现了两个问题
 *
 * 首先是头部跟菜单项之间之间会有一个间隔 这个间隔哪怕没有菜单项也会存在
 * 这是因为NavigationView内部是一个recyclerView 我们的header会添加到NavigationView内部的一个LinearLayout中
 * 然后NavigationView会将这个LinearLayout添加到recyclerView里，问题在于这个LinearLayout的padding被写死了
 * 所以总会存在一个padding导致显示效果很别扭 所以这里用反射拿到了LinearLayout并修改了padding为0
 *
 * 其次是如果我们只使用头部view NavigationView将无法为我们加载菜单配置 所以菜单的加载也需要手动处理
 * SegmentFault的侧滑菜单中 也出现了分组的概念，每组用一个细线分割 所以在定义菜单的时候
 * 请记得分组定义 为每组配置id 为每个item配置id和tittle 这样 {@link NavigationHelper} 就可以解析出来了
 *
 * 关于列表项的点击监听 参考{@link NavigationHelper#onItemSelected}
 *
 * Created by congxiaoyao on 2016/7/19.
 */
public class NavigationHelper {

    private NavigationView navigationView;
    private final View headerView;
    private final View mainView;

    private Context context;
    private RecyclerView recyclerView;
    private QuickAdapter adapter;
    private List<ItemBean> data;

    private Action1<Integer> onItemSelected;

    /**
     * 在构造函数中给侧滑栏绑定header和items
     *
     * @param navigationView 侧滑栏
     * @param menuResId      菜单的布局文件
     * @param headerResId    header的布局文件
     * @param mainResId      主布局文件
     */
    public NavigationHelper(NavigationView navigationView,
                            @MenuRes int menuResId,
                            @LayoutRes int mainResId,
                            @LayoutRes int headerResId) {
        this.navigationView = navigationView;
        this.context = navigationView.getContext();
        //去除scrollbar
        navigationView.getChildAt(0).setFitsSystemWindows(true);
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);
        //设置背景色
        navigationView.getChildAt(0).setBackgroundColor(ContextCompat
                .getColor(context, R.color.colorWhite));
        //去掉padding让显示效果看起来正常一些
        try {
            Field field = navigationView.getClass().getDeclaredField("mPresenter");
            field.setAccessible(true);
            NavigationMenuPresenter mPresenter = (NavigationMenuPresenter) field.get(navigationView);

            //mMenuView设置了padding准备去掉
            field = mPresenter.getClass().getDeclaredField("mMenuView");
            field.setAccessible(true);
            NavigationMenuView mMenuView = (NavigationMenuView) field.get(mPresenter);

            //mHeaderLayout也设置了padding准备去掉
            field = mPresenter.getClass().getDeclaredField("mHeaderLayout");
            field.setAccessible(true);
            LinearLayout mHeaderLayout = (LinearLayout) field.get(mPresenter);

            mHeaderLayout.setPadding(0, 0, 0, 0);
            mMenuView.setPadding(0, 0, 0, 0);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //加载菜单xml文件
        MenuInflater inflater = new SupportMenuInflater(context);
        MenuBuilder menu = new MenuBuilder(context);
        inflater.inflate(menuResId, menu);

        //根据分的组计算下分割线应该出现在哪个item下面
        int lastGroupId = -1;
        int nowGroupId = 0;
        List<Integer> showDivider = new ArrayList<>(2);

        //下面的for循环将menu加入到data中 透视计算分割线位置
        data = new ArrayList<>(menu.size());
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);

            nowGroupId = item.getGroupId();
            if (i == 0) lastGroupId = nowGroupId;

            if (nowGroupId != lastGroupId) showDivider.add(i);

            lastGroupId = nowGroupId;
            data.add(new ItemBean(item.getTitle().toString(), item.getItemId(), item.getIcon()));
        }

        //将arrayList转换为int数组 以便传入HorizontalDividerItemDecoration绘制分割线
        int[] showDividerInts = new int[showDivider.size()];
        for (int i = 0; i < showDividerInts.length; i++) {
            showDividerInts[i] = showDivider.get(i);
        }

        mainView = LayoutInflater.from(context).inflate(mainResId, null);
        recyclerView = (RecyclerView) mainView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));

        //加载header
        headerView = LayoutInflater.from(context).inflate(headerResId, recyclerView, false);
        int statusBarHeight = DisplayUtils.getStatusBarHeight((Activity) context);
//        headerView.setPadding(headerView.getPaddingLeft(),
//                headerView.getPaddingTop() + statusBarHeight,
//                headerView.getPaddingRight(),
//                headerView.getPaddingBottom());

        //根据已有的data、header、分割线位置来初始化recyclerView
        adapter = new QuickAdapter(R.layout.item_navigation, data);
        adapter.addHeaderView(headerView);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(context)
                .size(1)
                .colorResId(R.color.colorLightGray)
                .visibilityProvider(new VisibilityProvider(showDividerInts))
                .build());
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                ItemBean item = (ItemBean) adapter.getItem(position);
                if (onItemSelected != null) {
                    onItemSelected.call(item.id);
                }
            }
        });
        //不要被这句话蒙骗了 虽然叫addHeaderView但是是将整个view作为header的
        navigationView.addHeaderView(mainView);

    }

    /**
     * @return headerView
     */
    public View getHeaderView() {
        return headerView;
    }

    /**
     * @return navigationView
     */
    public NavigationView getNavigationView() {
        return navigationView;
    }

    /**
     * 设置item点击监听
     * @param onItemSelected
     */
    public void onItemSelected(Action1<Integer> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    private class QuickAdapter extends BaseQuickAdapter<ItemBean,BaseViewHolder> {

        public QuickAdapter(int layoutResId, List<ItemBean> data) {
            super(layoutResId, data);
        }
        @Override
        protected void convert(BaseViewHolder helper, ItemBean itemBean) {
            helper.setText(R.id.tv_title, itemBean.title)
                    .setImageDrawable(R.id.img_icon, itemBean.icon);
        }

    }
    /**
     * 侧滑栏功能列表中的一项 有标题和数字
     * 左侧是标题 右侧是数字
     */
    private class ItemBean {
        String title;
        @IdRes int id;
        Drawable icon;

        ItemBean(String title, @IdRes int id,Drawable icon) {
            this.title = title;
            this.id = id;
            this.icon = icon;
        }
    }

    /**
     * 用于确定哪些地方要显示分割线
     */
    private class VisibilityProvider implements FlexibleDividerDecoration.VisibilityProvider {

        private int[] visible;

        public VisibilityProvider(int[] visible) {
            this.visible = visible;
            Arrays.sort(visible);
        }

        @Override
        public boolean shouldHideDivider(int position, RecyclerView parent) {
            int len = visible.length;
            if (position < len) {
                if (visible[position] == position) {
                    return true;
                }
                len = position;
            }
            return Arrays.binarySearch(visible, 0, len, position) < 0;
        }
    }

}