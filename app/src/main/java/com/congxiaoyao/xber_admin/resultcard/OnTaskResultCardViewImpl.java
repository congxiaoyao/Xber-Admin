package com.congxiaoyao.xber_admin.resultcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ItemLoadingCardBinding;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;

/**
 * Created by congxiaoyao on 2017/3/22.
 */

public class OnTaskResultCardViewImpl extends LoadableViewImpl<OnTaskResultCardContract.Presenter>
implements OnTaskResultCardContract.View{

    private ItemLoadingCardBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.item_loading_card, container, false);
        progressBar = binding.contentProgressBar;

        View root = binding.getRoot();
        root.setTranslationY(DisplayUtils.dp2px(root.getContext(), 32));
        root.setAlpha(0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(root, "translationY", root.getTranslationY(), 0),
                ObjectAnimator.ofFloat(root, "alpha", 0, 1));
        set.setDuration(200);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        return root;
    }

    @Override
    public void hideMySelf(final Runnable runnable) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(binding.getRoot(), "scaleX", 1, 0.8f),
                ObjectAnimator.ofFloat(binding.getRoot(), "scaleY", 1, 0.8f),
                ObjectAnimator.ofFloat(binding.getRoot(), "alpha", 1, 0f));
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animatorSet.start();
    }
}
