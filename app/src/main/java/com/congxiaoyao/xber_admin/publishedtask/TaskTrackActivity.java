package com.congxiaoyao.xber_admin.publishedtask;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivityTaskTrackBinding;
import com.congxiaoyao.xber_admin.databinding.ItemTaskStateBinding;
import com.congxiaoyao.xber_admin.driverslist.driverdetail.HistoryTaskFragment;
import com.congxiaoyao.xber_admin.driverslist.module.ParcelTaskRsp;
import com.congxiaoyao.xber_admin.driverslist.taskdetail.TaskDetailActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskRspAndDriver;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class TaskTrackActivity extends SwipeBackActivity {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    private ActivityTaskTrackBinding binding;
    private TaskRspAndDriver task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_track);
        setSupportActionBar(binding.toolbar);
        task = getIntent().getParcelableExtra(PublishedTaskListFragment.KEY_TASK);
        if (task == null) {
            getSupportActionBar().setTitle("出现了一些错误");
            View view = getLayoutInflater().inflate(R.layout.view_empty,
                    binding.llContainer, true);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                    view.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            view.requestLayout();
            return;
        }
        getSupportActionBar().setTitle("订单跟踪");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.setTask(task);

        createItem("任务已派发", format.format(task.getCreateTime()), false);
        if (task.getStatus() == Task.STATUS_DELIVERED) {
            createItem("等待执行任务", format.format(task.getStartTime()), true);
        }
        if (task.getStatus() == Task.STATUS_EXECUTING) {
            createItem("开始执行任务", format.format(task.getRealStartTime()), false);
            ItemTaskStateBinding taskStateBinding = createItem("当前位置", "查询中...", true);
            new LocationQueryPresenter(new LocationQueryView(taskStateBinding)).subscribe();
        }
        if (task.getStatus() == Task.STATUS_COMPLETED) {
            createItem("开始执行任务", format.format(task.getRealStartTime()), false);
            createItem("结束行程", format.format(task.getRealEndTime()), false);
            if (task.getRealEndTime() - task.getEndTime() <= 0) {
                createItem("按时完成！", "✔", true);
            } else {
                createItem("未在规定时间内完成", "\uD83D\uDE1C", true);
            }
        }
    }

    private ItemTaskStateBinding createItem(String title, String subTitle, boolean isFooter) {
        ItemTaskStateBinding taskStateBinding;
        taskStateBinding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.item_task_state, binding.llContainer, true);
        taskStateBinding.setTitle(title);
        taskStateBinding.setSubTitle(subTitle);
        taskStateBinding.setIsFooter(isFooter);
        return taskStateBinding;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (task.getStatus() != Task.STATUS_COMPLETED) {
            return super.onCreateOptionsMenu(menu);
        }
        menu.add("查看详情").setOnMenuItemClickListener(new MenuItem
                .OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ParcelTaskRsp taskRsp = taskAndDriverToParcelTaskRsp(task);
                Intent intent = new Intent(TaskTrackActivity.this,
                        TaskDetailActivity.class);
                intent.putExtra(HistoryTaskFragment.EXTRA_KEY, taskRsp);
                startActivity(intent, ActivityOptionsCompat
                        .makeSceneTransitionAnimation(TaskTrackActivity.this).toBundle());
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private ParcelTaskRsp taskAndDriverToParcelTaskRsp(TaskRspAndDriver taskRspAndDriver) {
        ParcelTaskRsp taskRsp = new ParcelTaskRsp();
        taskRsp.setTaskId(taskRspAndDriver.getTaskId());
        taskRsp.setStartSpot(taskRspAndDriver.getStartSpot());
        taskRsp.setEndSpot(taskRspAndDriver.getEndSpot());
        taskRsp.setContent(taskRspAndDriver.getContent());
        taskRsp.setCreateUser(taskRspAndDriver.getCreateUser());
        taskRsp.setRealStartTime(taskRspAndDriver.getRealStartTime());
        taskRsp.setRealEndTime(taskRspAndDriver.getRealEndTime());
        taskRsp.setStatus(taskRspAndDriver.getStatus());
        taskRsp.setNote(taskRspAndDriver.getNote());
        return taskRsp;
    }

    class LocationQueryView implements LoadableView<LocationQueryPresenter> {

        private ItemTaskStateBinding itemTaskStateBinding;

        public LocationQueryView(ItemTaskStateBinding itemTaskStateBinding) {
            this.itemTaskStateBinding = itemTaskStateBinding;
        }

        @Override
        public void showLoading() {
            itemTaskStateBinding.setSubTitle("查询中...");
        }

        @Override
        public void hideLoading() {
            itemTaskStateBinding.setSubTitle("");
        }

        @Override
        public void setPresenter(LocationQueryPresenter presenter) {
        }

        @Override
        public Context getContext() {
            return TaskTrackActivity.this;
        }

        public void showLocation(String location) {
            itemTaskStateBinding.setSubTitle(location);
        }

        public void showError() {
            itemTaskStateBinding.setSubTitle("查询失败" + "\uD83D\uDE2D");
        }
    }

    class LocationQueryPresenter extends BasePresenterImpl<LocationQueryView> {

        public LocationQueryPresenter(LocationQueryView view) {
            super(view);
        }

        @Override
        public void subscribe() {
            view.showLoading();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.hideLoading();
                    view.showLocation("山东省 济南市");
                }
            }, 1000);
        }

        @Override
        public void onDispatchException(Throwable throwable) {
            view.showError();
        }
    }
}
