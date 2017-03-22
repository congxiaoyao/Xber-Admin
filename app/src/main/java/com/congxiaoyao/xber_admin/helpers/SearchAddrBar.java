package com.congxiaoyao.xber_admin.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.adapter.base.binding.BindingAdapterHelper;
import com.congxiaoyao.adapter.base.binding.annotations.ItemLayout;
import com.congxiaoyao.adapter.base.binding.demo.SampleBean;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemCarBinding;
import com.congxiaoyao.xber_admin.databinding.ItemSearchAddrBarBinding;
import com.congxiaoyao.xber_admin.databinding.ItemSearchBarBinding;
import com.congxiaoyao.xber_admin.resultcard.AddrResultCardContract;
import com.congxiaoyao.xber_admin.resultcard.AddrResultCardPresenterImpl;
import com.congxiaoyao.xber_admin.resultcard.AddrResultCardViewImpl;
import com.congxiaoyao.xber_admin.resultcard.OnTaskResultCardPresenterImpl;
import com.congxiaoyao.xber_admin.resultcard.OnTaskResultCardViewImpl;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by congxiaoyao on 2017/3/21.
 */

public class SearchAddrBar extends TopSearchBar {

    private Spot startSpot;
    private Spot endSpot;

    private ItemSearchAddrBarBinding itemSearchAddrBarBinding;

    private AddrResultCardViewImpl locationFragment;
    private AddrResultCardContract.Presenter locationPresenter;

    private OnTaskResultCardViewImpl onTaskFragment;
    private OnTaskResultCardPresenterImpl onTaskPresenter;

    private List<CarDetail> result;

    public SearchAddrBar(ItemSearchBarBinding binding, LinearLayout animationLayer) {
        super(binding, animationLayer);
        binding.tvHint.setText(R.string.please_input_addr);
    }

    @Override
    protected void onSearchClick() {
        animationLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOutSide();
            }
        });

        isAnimating = true;
        animationLayer.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.getRoot().setAlpha(0);
                final ItemSearchBarBinding itemSearchBarBinding = openAnimationLayer();
                itemSearchBarBinding.tvHint.setText(R.string.please_input_addr);
                animationLayer.post(new Runnable() {
                    @Override
                    public void run() {
                        runOrgBarOutAnimation(itemSearchBarBinding);
                    }
                });
            }
        }, 160);
    }

    @Override
    protected void onCancelClick() {
        binding.tvHint.setText(R.string.please_input_addr);
        setRightIconSearch();
        clearCache();
        onTraceCars(null);
    }

    @Override
    public boolean onBackPressed() {
        if (isAnimating) return true;
        if (animationLayer.getVisibility() != View.GONE) {
            onClickOutSide();
            return true;
        }
        return false;
    }

    @Override
    protected void onDetailClick() {
        if (result == null || result.size() == 0) {
            Toast.makeText(binding.getRoot().getContext(), "暂无车辆", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context context = binding.getRoot().getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        BaseQuickAdapter adapter = BindingAdapterHelper.create(recyclerView)
                .with(new LinearLayoutManager(context))
                .setBindingAdapter(new Object(){
                    @ItemLayout(R.layout.item_car)
                    public void bindItemData(ItemCarBinding binding, CarDetail car) {
                        binding.setCar(car);
                        binding.executePendingBindings();
                    }
                });
        adapter.addData(result);

        final AlertDialog dialog = builder.setView(recyclerView).setPositiveButton("确定", null)
                .setTitle("正在追踪").show();
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onClickOutSide() {
        if (isAnimating) return;
        clearCache();
        if (onTaskFragment != null) {
            onTaskPresenter.destroy(new Runnable() {
                @Override
                public void run() {
                    removeLoadingCard();
                    onClickOutSide();
                }
            });
            return;
        }
        if (locationFragment != null) {
            locationFragment.hideMyself(new Action0() {
                @Override
                public void call() {
                    removeLocationSelectCard();
                }
            });
            return;
        }
        runAddrBarOutAnimation();
    }

    public void runOrgBarOutAnimation(final ItemSearchBarBinding orgViewBinding) {
        isAnimating = true;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200).setInterpolator(defaultInterpolator);
        ObjectAnimator animator = ObjectAnimator.ofFloat(orgViewBinding.menuButton, "alpha", 1, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final CardView cardView = orgViewBinding.cardView;
                int height = cardView.getHeight();
                ValueAnimator animator = ValueAnimator.ofInt(height, (height << 1) + height);
                final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int increment = (int) animation.getAnimatedValue();
                        layoutParams.height = increment;
                        cardView.requestLayout();
                    }
                });
                animator.setDuration(200).setInterpolator(defaultInterpolator);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationLayer.removeAllViews();
                        final ItemSearchAddrBarBinding binding = DataBindingUtil.inflate(LayoutInflater
                                        .from(orgViewBinding.getRoot().getContext()),
                                R.layout.item_search_addr_bar, null, false);
                        animationLayer.addView(binding.getRoot());
                        binding.getRoot().post(new Runnable() {
                            @Override
                            public void run() {
                                runAddrBarInAnimation(binding);
                            }
                        });
                    }
                });
            }
        });
        animatorSet.playTogether(animator,
                ObjectAnimator.ofFloat(orgViewBinding.searchButton, "alpha", 1, 0),
                ObjectAnimator.ofFloat(orgViewBinding.tvHint, "alpha", 1, 0));
        animatorSet.start();
    }

    public void runAddrBarInAnimation(final ItemSearchAddrBarBinding binding) {
        this.itemSearchAddrBarBinding = binding;
        binding.llEndContainer.setAlpha(0);
        binding.llStartContainer.setAlpha(0);
        binding.btnSearch.setAlpha(0);
        binding.viewDivider1.setAlpha(0);
        binding.viewDivider2.setAlpha(0);
        int translationY = binding.llEndContainer.getHeight();

        binding.llEndContainer.setVisibility(View.VISIBLE);
        binding.llEndContainer.setTranslationY(translationY);
        binding.llStartContainer.setVisibility(View.VISIBLE);
        binding.llStartContainer.setTranslationY(translationY);
        binding.btnSearch.setVisibility(View.VISIBLE);
        binding.viewDivider1.setVisibility(View.VISIBLE);
        binding.viewDivider2.setVisibility(View.VISIBLE);
        binding.btnSearch.setTranslationY(translationY);

        List<Animator> list = new ArrayList<>();
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.llStartContainer,
                "translationY", translationY, 0);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(binding.llStartContainer,
                "alpha", 0, 1);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(binding.viewDivider1,
                "translationY", translationY, 0);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(binding.viewDivider1,
                "alpha", 0, 1);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(binding.viewDivider2,
                "translationY", translationY, 0);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(binding.viewDivider2,
                "alpha", 0, 1);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(binding.llEndContainer,
                "translationY", translationY, 0);
        animator.setStartDelay(60);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(binding.llEndContainer,
                "alpha", 0, 1);
        animator.setStartDelay(60);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(binding.btnSearch,
                "translationY", translationY, 0);
        animator.setStartDelay(120);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(binding.btnSearch,
                "alpha", 0, 1);
        animator.setStartDelay(120);
        list.add(animator);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(defaultInterpolator);
        animatorSet.setDuration(200);
        animatorSet.playTogether(list);
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.setPresenter(new ButtonPresenter());
                isAnimating = false;
            }
        });
    }

    private void runAddrBarOutAnimation() {
        isAnimating = true;
        int translationY = -itemSearchAddrBarBinding.llEndContainer.getHeight();
        List<Animator> list = new ArrayList<>();
        ObjectAnimator animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.llStartContainer,
                "translationY", 0, translationY);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.llStartContainer,
                "alpha", 1, 0);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.viewDivider1,
                "translationY", 0, translationY);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.viewDivider1,
                "alpha", 1, 0);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.viewDivider2,
                "translationY", 0, translationY);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.viewDivider2,
                "alpha", 1, 0);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.llEndContainer,
                "translationY", 0, translationY);
        animator.setStartDelay(60);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.llEndContainer,
                "alpha", 1, 0);
        animator.setStartDelay(60);
        list.add(animator);

        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.btnSearch,
                "translationY", 0, translationY);
        animator.setStartDelay(120);
        list.add(animator);
        animator = ObjectAnimator.ofFloat(itemSearchAddrBarBinding.btnSearch,
                "alpha", 1, 0);
        animator.setStartDelay(120);
        list.add(animator);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(defaultInterpolator);
        animatorSet.setDuration(200);
        animatorSet.playTogether(list);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final CardView cardView = itemSearchAddrBarBinding.cardView;
                int height = cardView.getHeight();
                final ValueAnimator animator = ValueAnimator.ofInt(height, height / 3);
                final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int increment = (int) animation.getAnimatedValue();
                        layoutParams.height = increment;
                        cardView.requestLayout();
                    }
                });
                animator.setDuration(200).setInterpolator(defaultInterpolator);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        runOrgBarInAnimation();
                    }
                });
            }
        });
    }

    private void runOrgBarInAnimation() {
        CardView cardView = itemSearchAddrBarBinding.cardView;
        cardView.setCardElevation(0);
        binding.getRoot().setAlpha(1);
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "alpha", 1, 0);
        animator.setDuration(200);
        animator.setInterpolator(defaultInterpolator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationLayer.setVisibility(View.GONE);
                isAnimating = false;
            }
        });
        animator.start();
    }

    public void showLocationSelectCard(int offset) {
        AppCompatActivity activity = (AppCompatActivity) binding.getRoot().getContext();
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        this.locationFragment = new AddrResultCardViewImpl(offset);
        this.locationPresenter = new AddrResultCardPresenterImpl(locationFragment,this);
        transaction.replace(R.id.animation_layer, locationFragment);
        transaction.commit();
    }

    public void removeLocationSelectCard() {
        locationPresenter = null;
        if (locationFragment != null) {
            FragmentManager manager = ((AppCompatActivity) binding.getRoot().getContext())
                    .getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(locationFragment);
            transaction.commit();
            locationFragment = null;
        }
    }

    public void showLoadingCard() {
        AppCompatActivity activity = (AppCompatActivity) binding.getRoot().getContext();
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        this.onTaskFragment = new OnTaskResultCardViewImpl();
        this.onTaskPresenter = new OnTaskResultCardPresenterImpl(onTaskFragment);
        transaction.replace(R.id.animation_layer, onTaskFragment);
        transaction.commit();
    }

    public void removeLoadingCard() {
        onTaskPresenter = null;
        if (onTaskFragment != null) {
            FragmentManager manager = ((AppCompatActivity) binding.getRoot().getContext())
                    .getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(onTaskFragment);
            transaction.commit();
            onTaskFragment = null;
        }
    }


    public class ButtonPresenter {

        public void onClickStart(View view) {
            if (locationFragment != null) {
                removeLocationSelectCard();
            }
            showLocationSelectCard(itemSearchAddrBarBinding.getRoot().getHeight() / 3 * 2 - 2);
            view.post(new Runnable() {
                @Override
                public void run() {
                    locationPresenter.selectStart();
                }
            });
        }

        public void onClickEnd(View view) {
            if (locationFragment != null) {
                removeLocationSelectCard();
            }
            showLocationSelectCard(itemSearchAddrBarBinding.getRoot().getHeight() / 3);
            view.post(new Runnable() {
                @Override
                public void run() {
                    locationPresenter.selectEnd();
                }
            });
        }

        public void onClickSearch(View view) {
            if (startSpot == null && endSpot == null) {
                shakeCard(itemSearchAddrBarBinding.cardView);
                return;
            }
            showLoadingCard();
            onTaskPresenter.getCarOnTask(startSpot, endSpot, new Action1<List<CarDetail>>() {
                @Override
                public void call(List<CarDetail> carDetails) {
                    removeLoadingCard();
                    onClickOutSide();
                    if (carDetails.size() == 0) {
                        Toast.makeText(binding.getRoot().getContext(), "暂无车辆",
                                Toast.LENGTH_SHORT).show();
                        onClickOutSide();
                        return;
                    }
                    binding.tvHint.setText("正在追踪车辆");
                    setRightIconCancel();
                    result = carDetails;
                    onTraceCars(new ArrayList<Long>() {{
                            for (CarDetail detail : result) add(detail.getCarId());
                        }
                    });
                }
            });
        }
    }

    public void setStartLocation(Spot spot) {
        startSpot = spot;
        if (itemSearchAddrBarBinding != null) {
            itemSearchAddrBarBinding.setStartSpot(spot);
        }
    }

    public void setEndLocation(Spot spot) {
        endSpot = spot;
        if (itemSearchAddrBarBinding != null) {
            itemSearchAddrBarBinding.setEndSpot(spot);
        }
    }

    public void clearCache() {
        this.endSpot = null;
        this.endSpot = null;
        this.result = null;
    }
}
