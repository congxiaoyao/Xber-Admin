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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemCarBinding;
import com.congxiaoyao.xber_admin.databinding.ItemSearchBarBinding;
import com.congxiaoyao.xber_admin.databinding.ItemSearchCarBarBinding;
import com.congxiaoyao.xber_admin.resultcard.CarResultCardContract;
import com.congxiaoyao.xber_admin.resultcard.CarResultCardPresenterImpl;
import com.congxiaoyao.xber_admin.resultcard.CarResultCardViewImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import rx.functions.Action0;

/**
 * Created by congxiaoyao on 2017/3/18.
 */

public class SearchCarBar extends TopSearchBar {

    private ItemSearchCarBarBinding itemSearchCarBinding;
    private CarResultCardPresenterImpl presenter;
    private CarResultCardViewImpl fragment;

    private CarDetail trackingCar = null;

    public SearchCarBar(ItemSearchBarBinding binding, LinearLayout animationLayer) {
        super(binding, animationLayer);
        binding.tvHint.setText(R.string.please_input_car);
    }

    @Override
    protected void onCancelClick() {
        binding.tvHint.setText(R.string.please_input_car);
        setRightIconSearch();
        onTraceCars(null);
    }

    @Override
    protected void onSearchClick() {
        if (!enabled) return;
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
                itemSearchBarBinding.tvHint.setText(R.string.please_input_car);
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
    protected void onDetailClick() {
        final Context context = binding.getRoot().getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final ItemCarBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.item_car, null, false);
        binding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                binding.getRoot().getLayoutParams().height = context.getResources()
                        .getDimensionPixelSize(R.dimen.dp_72);
            }
        });

        binding.setCar(trackingCar);
        builder.setView(binding.getRoot()).setPositiveButton("确定", null)
                .setTitle("正在追踪").show();
    }

    @Override
    protected void onClickOutSide() {
        if (isAnimating) return;
        if (fragment == null) {
            runCarBarOutAnimation();
            return;
        }
        fragment.hideMyself(new Action0() {
            @Override
            public void call() {
                runCarBarOutAnimation();
            }
        });
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

    public void showResultCard() {
        AppCompatActivity activity = (AppCompatActivity) binding.getRoot().getContext();
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        fragment = new CarResultCardViewImpl();
        presenter = new CarResultCardPresenterImpl(fragment);
        presenter.setOnCarSelectedListener(new CarResultCardContract.OnCarSelectedListener() {
            @Override
            public void onCarSelected(CarDetail carDetail) {
                SearchCarBar.this.trackingCar = carDetail;
                runCarBarOutAnimation();
                binding.tvHint.setText("正在跟踪车辆...");
                setRightIconCancel();
                onTraceCars(Arrays.asList(carDetail.getCarId()));
            }
        });
        transaction.replace(R.id.animation_layer, fragment);
        transaction.commit();
    }

    public void runOrgBarOutAnimation(final ItemSearchBarBinding orgViewBinding) {

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200).setInterpolator(defaultInterpolator);
        ObjectAnimator animator = ObjectAnimator.ofFloat(orgViewBinding.menuButton, "alpha", 1, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final CardView cardView = orgViewBinding.cardView;
                int height = cardView.getHeight();
                ValueAnimator animator = ValueAnimator.ofInt(height, height << 1);
                final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int increment = (int) animation.getAnimatedValue();
                        layoutParams.height = increment;
                        cardView.requestLayout();
                    }
                });
                animator.setDuration(320).setInterpolator(defaultInterpolator);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationLayer.removeAllViews();
                        final ItemSearchCarBarBinding binding = DataBindingUtil.inflate(LayoutInflater
                                        .from(orgViewBinding.getRoot().getContext()),
                                R.layout.item_search_car_bar, null, false);
                        animationLayer.addView(binding.getRoot());
                        binding.getRoot().post(new Runnable() {
                            @Override
                            public void run() {
                                runCarBarInAnimation(binding);
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

    public void runOrgBarInAnimation() {
        CardView cardView = itemSearchCarBinding.cardView;
        cardView.setCardElevation(0);
        binding.getRoot().setAlpha(1);
        ObjectAnimator animator = ObjectAnimator.ofFloat(cardView, "alpha", 1, 0);
        animator.setDuration(200);
        animator.setInterpolator(defaultInterpolator);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationLayer.setVisibility(View.GONE);
                presenter = null;
                if (fragment != null) {
                    FragmentManager manager = ((AppCompatActivity) binding.getRoot().getContext())
                            .getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(fragment);
                    transaction.commit();
                }
                isAnimating = false;
            }
        });
        animator.start();
    }

    public void runCarBarOutAnimation() {
        isAnimating = true;
        Button btnSearch = itemSearchCarBinding.btnSearch;
        itemSearchCarBinding.editText.setEnabled(false);
        hideSoftInput(itemSearchCarBinding.getRoot().getContext());
        int translationY = btnSearch.getHeight() / 2;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(btnSearch,
                "translationY", 0, translationY).setDuration(200),
                ObjectAnimator.ofFloat(btnSearch, "alpha", 1, 0).setDuration(200),
                ObjectAnimator.ofFloat(itemSearchCarBinding.viewDivider,
                        "alpha", 1, 0).setDuration(200),
                ObjectAnimator.ofFloat(itemSearchCarBinding.editText,
                        "alpha", 1, 0).setDuration(200));
        animatorSet.setInterpolator(defaultInterpolator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final CardView cardView = itemSearchCarBinding.cardView;
                itemSearchCarBinding.btnSearch.setVisibility(View.GONE);
                itemSearchCarBinding.viewDivider.setVisibility(View.GONE);
                int height = cardView.getHeight();
                ValueAnimator animator = ValueAnimator.ofInt(height, height >>> 1);
                final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int newHeight = (int) animation.getAnimatedValue();
                        layoutParams.height = newHeight;
                        cardView.requestLayout();
                    }
                });
                animator.setDuration(200);
                animator.setInterpolator(defaultInterpolator);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        runOrgBarInAnimation();
                    }
                });
                animator.start();
            }
        });
        animatorSet.start();
    }

    public void runCarBarInAnimation(final ItemSearchCarBarBinding binding) {
        itemSearchCarBinding = binding;
        binding.editText.setEnabled(true);
        binding.btnSearch.setVisibility(View.VISIBLE);
        binding.viewDivider.setVisibility(View.VISIBLE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(binding.btnSearch,
                "alpha", 0.2f, 1f).setDuration(300),
                ObjectAnimator.ofFloat(binding.btnSearch,
                        "scaleY", 0.8f, 0.9f, 1.1f, 1.05f, 1).setDuration(300),
                ObjectAnimator.ofFloat(binding.btnSearch,
                        "scaleX", 0.8f, 0.9f, 1.1f, 1.05f, 1).setDuration(300),
                ObjectAnimator.ofFloat(binding.viewDivider,
                        "scaleX", 0.8f, 0.9f, 1.1f, 1.05f, 1).setDuration(300));

        animatorSet.setInterpolator(defaultInterpolator);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.editText.setFocusable(true);
                binding.editText.setFocusableInTouchMode(true);

                binding.editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) binding.getRoot().getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.editText, 0);
                binding.btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSoftInput(binding.getRoot().getContext());
                        searchCar(binding.editText.getText().toString().trim());
                    }
                });
                isAnimating = false;
            }
        });
    }

    private void searchCar(final String content) {
        if (presenter == null) {
            showResultCard();
        }
        if (!content.equals("")) {
            binding.getRoot().post(new Runnable() {
                @Override
                public void run() {
                    presenter.search(content);
                }
            });
        }else {
            shakeCard(itemSearchCarBinding.cardView);
        }
    }
}
