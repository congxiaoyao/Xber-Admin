package com.congxiaoyao.xber_admin;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.databinding.ViewDataBinding;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemSearchBarBinding;
import com.congxiaoyao.xber_admin.helpers.SearchAddrBar;
import com.congxiaoyao.xber_admin.helpers.SearchCarBar;
import com.congxiaoyao.xber_admin.helpers.TopSearchBar;
import com.congxiaoyao.xber_admin.widget.CustomViewPager;

import java.util.List;

/**
 * Created by congxiaoyao on 2017/3/22.
 */

public class TopBarPagerAdapter extends PagerAdapter {

    private OnTraceCarListener listener;

    private final SearchCarBar searchCarBar;
    private final SearchAddrBar searchAddrBar;
    private View nameBarView;
    private View addrBarView;

    private ViewPager viewPager;

    public TopBarPagerAdapter(LinearLayout animationLayer, ViewPager viewPager) {
        this.viewPager = viewPager;
        Context context = animationLayer.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemSearchBarBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_search_bar, null, false);
        nameBarView = binding.getRoot();
        searchCarBar = new SearchCarBar(binding, animationLayer) {
            @Override
            protected void onTraceCars(List<Long> carIds) {
                if (listener == null) return;
                listener.onTraceCar(carIds);
            }
        };
        binding = DataBindingUtil.inflate(inflater,
                R.layout.item_search_bar, null, false);
        addrBarView = binding.getRoot();
        searchAddrBar = new SearchAddrBar(binding, animationLayer) {
            @Override
            protected void onTraceCars(List<Long> carIds) {
                if (listener == null) return;
                listener.onTraceCar(carIds);
            }
        };
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeView(position == 0 ? nameBarView : addrBarView);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View child = position == 0 ? nameBarView : addrBarView;
        container.addView(child);
        return child;
    }

    public SearchCarBar getSearchCarBar() {
        return searchCarBar;
    }

    public SearchAddrBar getSearchAddrBar() {
        return searchAddrBar;
    }

    public void setEnabled(boolean enabled) {
        if (searchAddrBar != null) {
            searchAddrBar.setEnabled(enabled);
        }
        if (searchCarBar != null) {
            searchCarBar.setEnabled(enabled);
        }
    }

    public class PageScrollHelper extends ViewPager.SimpleOnPageChangeListener {

        private CustomViewPager topBarPager;

        public PageScrollHelper(CustomViewPager pager) {
            this.topBarPager = pager;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == 1) return;
            if (searchCarBar.getIconState() == TopSearchBar.ICON_STATE_CANCEL) {
                handleCarBarScroll(positionOffsetPixels);
            } else if (searchAddrBar.getIconState() == TopSearchBar.ICON_STATE_CANCEL) {
                handleAddrBarScroll(positionOffsetPixels);
            }
        }

        public void handleCarBarScroll(int positionOffsetPixels) {
            int width = topBarPager.getWidth();
            int maxOffset = (int) (width * 0.10f);
            if (positionOffsetPixels >= maxOffset) {
                topBarPager.scrollTo(maxOffset, 0);
                topBarPager.setScrollEnabled(false);
            }
            float scale = (positionOffsetPixels / (float) width) + 1;
            searchCarBar.setHintScale(scale);
            searchCarBar.changeIconColorRed(scale);
        }

        public void handleAddrBarScroll(int positionOffsetPixels) {
            int width = topBarPager.getWidth();
            int minOffset = (int) (width * 0.90f);
            if (positionOffsetPixels <= minOffset) {
                topBarPager.scrollTo(minOffset, 0);
                topBarPager.setScrollEnabled(false);
            }
            float scale = 2 - (positionOffsetPixels / (float) width);
            searchAddrBar.setHintScale(scale);
            searchAddrBar.changeIconColorRed(scale);
        }
    }

    public void setOnTraceCarListener(OnTraceCarListener listener) {
        this.listener = listener;
    }

    public boolean onBackPressed() {
        int index = viewPager.getCurrentItem();
        if (index == 0) {
            return searchCarBar.onBackPressed();
        }else{
            return searchAddrBar.onBackPressed();
        }
    }

    public interface OnTraceCarListener {

        void onTraceCar(List<Long> carIds);
    }
}
