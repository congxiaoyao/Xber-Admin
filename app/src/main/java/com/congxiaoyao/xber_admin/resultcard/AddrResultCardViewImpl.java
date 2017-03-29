package com.congxiaoyao.xber_admin.resultcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemResultCardBinding;
import com.congxiaoyao.xber_admin.helpers.SearchAddrBar;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import rx.functions.Action0;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

@SuppressLint("ValidFragment")
public class AddrResultCardViewImpl extends ListLoadableViewImpl<AddrResultCardContract.Presenter,
        Spot> implements AddrResultCardContract.View{

    private ItemResultCardBinding binding;
    private int iconId = -1;
    private int offset = 0;

    public AddrResultCardViewImpl(int offset) {
        this.offset = offset;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,
                R.layout.item_result_card, container, false);
        binding.getRoot().setTranslationY(-offset);
        progressBar = (ContentLoadingProgressBar) binding.getRoot()
                .findViewById(R.id.content_progress_bar);
        RecyclerView recyclerView = binding.recyclerView;
        int margin = DisplayUtils.dp2px(getContext(), 16);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getContext())
                .size(1)
                .margin(margin, margin)
                .colorResId(R.color.colorLightGray)
                .build());
        super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, final int position) {
                Log.d(com.congxiaoyao.xber_admin.TAG.ME, "onSimpleItemClick: ");
                presenter.onSelectSpot(getData().get(position));
                hideMyself(null);
            }
        });
        getAdapter().isFirstOnly(true);
        getAdapter().openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        return binding.getRoot();
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return false;
    }

    @Override
    public void requestResize(int dataSize) {
        if (dataSize == 0) dataSize = 5;
        int itemHeight = getContext().getResources().getDimensionPixelSize(R.dimen.dp_48);
        if (dataSize > 5) dataSize = 5;
        int recHeight = dataSize * itemHeight;
        final ViewGroup.LayoutParams layoutParams = binding.recyclerView.getLayoutParams();
        int orgHeight = layoutParams.height;
        if (orgHeight <= 0) {
            orgHeight = progressBar.getHeight();
        }
        //动画过渡
        ValueAnimator animator = ValueAnimator.ofInt(orgHeight, recHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                layoutParams.height = height;
                binding.recyclerView.requestLayout();
            }
        });
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(200);
        animator.start();
    }

    @Override
    public void post(Runnable runnable) {
        binding.getRoot().post(runnable);
    }

    @Override
    public void hideMyself(final Action0 callback) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(binding.getRoot(), "alpha", 1, 0));
        animatorSet.setDuration(200);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (callback != null) callback.call();
            }
        });
        animatorSet.setStartDelay(100);
        animatorSet.start();
    }

    @Override
    public void setLocationIcon(int id) {
        this.iconId = id;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Spot data) {
        viewHolder.setText(R.id.tv_location, data.getSpotName());
        if (iconId != -1) {
            viewHolder.setBackgroundRes(R.id.view_location_icon, iconId);
        }
    }

    @Override
    public void scrollToTop() {
        binding.recyclerView.scrollToPosition(0);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_location;
    }

    @Override
    public void hideSwipeRefreshLoading() {

    }
}
