package com.congxiaoyao.xber_admin;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.test.mock.MockApplication;
import android.transition.Slide;

import com.congxiaoyao.xber_admin.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private ObjectAnimator animator;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            onAnimationEnd();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
        binding.getRoot().postDelayed(runnable, 500);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (animator != null) return;
//        int width = binding.rlContainer.getWidth();
//        binding.rlContainer.setTranslationX(width);
//        PushService.stopPushService(this);
//        animator = ObjectAnimator.ofFloat(binding.rlContainer, "translationX", width, 0)
//                .setDuration(650);
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                binding.rlContainer.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        WelcomeActivity.this.onAnimationEnd();
//                    }
//                }, 200);
//            }
//        });
//        animator.setStartDelay(100);
//        animator.start();
    }

    public void onAnimationEnd() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.rlContainer.removeCallbacks(runnable);
        if (animator != null) {
            animator.removeAllListeners();
            animator.cancel();
            animator = null;
        }
    }
}
