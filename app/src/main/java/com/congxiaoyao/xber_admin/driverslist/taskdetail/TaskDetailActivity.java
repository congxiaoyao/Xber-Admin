package com.congxiaoyao.xber_admin.driverslist.taskdetail;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.congxiaoyao.httplib.request.UserRequest;
import com.congxiaoyao.httplib.request.body.User;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.XberApplication;
import com.congxiaoyao.xber_admin.databinding.ActivityTaskDetailBinding;
import com.congxiaoyao.xber_admin.driverslist.driverdetail.HistoryTaskFragment;
import com.congxiaoyao.xber_admin.driverslist.module.ParcelTaskRsp;
import com.congxiaoyao.xber_admin.utils.RxUtils;
import com.congxiaoyao.xber_admin.utils.Token;

import java.text.SimpleDateFormat;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String KEY_TASK_ID = "KEY_TASK_ID";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH点mm分");
    public ActivityTaskDetailBinding binding;
    private ParcelTaskRsp taskRsp;

    private TracePresenter presenter;
    private TracePreviewHelper traceViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(TransitionInflater.from(this)
                .inflateTransition(R.transition.slide_in));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_detail);
        taskRsp = getIntent().getParcelableExtra(HistoryTaskFragment.EXTRA_KEY);
        if (taskRsp == null) {
            setSupportActionBar(binding.toolbar);
            getSupportActionBar().setTitle("发生了一些错误");
            binding.llContainer.removeAllViews();
            View view = getLayoutInflater().inflate(R.layout.view_empty,
                    binding.llContainer, true);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                    view.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            view.requestLayout();
            return;

        }
        binding.setFormat(format);
        binding.setPresenter(new Presenter());
        binding.setTaskRsp(taskRsp);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("任务编号" + taskRsp.getTaskId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        traceViewHelper = new TracePreviewHelper(binding.flContainer,
                binding.imgTracePreview, binding.mapContainer);
        presenter = new TracePresenter(traceViewHelper, taskRsp.getTaskId());
        traceViewHelper.showBitmap();

        binding.imgTracePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskDetailActivity.this, TraceDetailActivity.class);
                intent.putExtra(KEY_TASK_ID, presenter.getTaskId());
                Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        TaskDetailActivity.this, binding.imgTracePreview,
                        getString(R.string.shared_view_name)).toBundle();
                startActivity(intent, options);
            }
        });
        requestHeader();
    }

    private void requestHeader() {
        XberRetrofit.create(UserRequest.class).getUserDetail(taskRsp.getCreateUser(), Token.value)
                .compose(RxUtils.<User>delayWhenTimeEnough(250))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
                        binding.setUser(user);
                        binding.senderContainer.setVisibility(View.VISIBLE);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG.ME, "call: ", throwable);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((XberApplication) getApplicationContext()).clearCachedBitmap();
        if (traceViewHelper == null) return;
        if (traceViewHelper.getMapView() != null) {
            traceViewHelper.getMapView().onDestroy();
        }
        if (presenter != null) {
            presenter.unSubscribe();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (traceViewHelper == null) return;
        if (traceViewHelper.getMapView() != null) {
            traceViewHelper.getMapView().onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (traceViewHelper == null) return;
        if (traceViewHelper.getMapView() != null) {
            traceViewHelper.getMapView().onResume();
        }
    }

    public class Presenter {

        public void onClearCacheClick(View view) {
            if (traceViewHelper != null && traceViewHelper.isLoaded()
                    && presenter != null) {
                deleteFile(presenter.getLatLngFileName());
                deleteFile(presenter.getImageFileName());
                Snackbar.make(view, "已删除", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
