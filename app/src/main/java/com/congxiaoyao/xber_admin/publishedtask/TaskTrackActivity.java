package com.congxiaoyao.xber_admin.publishedtask;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.ActivityTaskTrackBinding;
import com.congxiaoyao.xber_admin.databinding.ItemTaskStateBinding;
import com.congxiaoyao.xber_admin.driverslist.DriverListActivity;
import com.congxiaoyao.xber_admin.driverslist.DriverListFragment;
import com.congxiaoyao.xber_admin.driverslist.driverdetail.DriverDetailActivity;
import com.congxiaoyao.xber_admin.driverslist.driverdetail.HistoryTaskFragment;
import com.congxiaoyao.xber_admin.driverslist.module.CarDetailParcel;
import com.congxiaoyao.xber_admin.driverslist.module.ParcelTaskRsp;
import com.congxiaoyao.xber_admin.driverslist.taskdetail.TaskDetailActivity;
import com.congxiaoyao.xber_admin.mvpbase.presenter.BasePresenterImpl;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableView;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskRspAndDriver;
import com.congxiaoyao.xber_admin.publishedtask.bean.TaskTrackContact;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageHelper;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class TaskTrackActivity extends SwipeBackActivity implements TaskTrackContact.View{

    public static final String TITLE = "订单跟踪";
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    private ActivityTaskTrackBinding binding;

    private TaskTrackContact.Presenter presenter;
    private ContentLoadingProgressBar progressBar;
    private Menu menu;
    private List<Runnable> addMenuActions = new ArrayList<>(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TaskTrackPresenter(this).subscribe();
    }

    @Override
    public void showTask(final TaskRspAndDriver task) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_track);
        setSupportActionBar(binding.toolbar);
        if (task == null) {
            showError();
            return;
        }
        getSupportActionBar().setTitle(TITLE);
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
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    menu.add("查看详情").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
                    }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            };
            if (menu == null) addMenuActions.add(action);
            else action.run();
        }
        Runnable action = new Runnable() {
            @Override
            public void run() {
                menu.add("查看该司机").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        CarDetailParcel parcel = DriverListFragment.carDetailToParcel(task.getCarDetail());
                        Intent intent = new Intent(TaskTrackActivity.this, DriverDetailActivity.class);
                        intent.putExtra(DriverListActivity.EXTRA_CARDETIAL, parcel);
                        startActivity(intent);
                        return true;
                    }
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);;
            }
        };
        if (menu == null) addMenuActions.add(action);
        else action.run();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        for (Runnable addMenuAction : addMenuActions) {
            addMenuAction.run();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void showError() {
        View parent = getLayoutInflater().inflate(R.layout.activity_standard, null);
        Toolbar toolbar = (Toolbar) parent.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("出现了一些错误");
        setContentView(parent);
        View view = getLayoutInflater().inflate(R.layout.view_empty,
                (ViewGroup) parent.findViewById(R.id.fragment_content), true);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                view.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        view.requestLayout();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void showLoading() {
        View parent = getLayoutInflater().inflate(R.layout.activity_standard, null);
        Toolbar toolbar = (Toolbar) parent.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TITLE);
        progressBar = (ContentLoadingProgressBar) getLayoutInflater()
                .inflate(R.layout.view_progress_bar,
                        (ViewGroup) parent.findViewById(R.id.fragment_content), true)
                .findViewById(R.id.content_progress_bar);

        setContentView(parent);
        progressBar.show();
    }

    @Override
    public void hideLoading() {
        if (progressBar != null) {
            progressBar.hide();
        }
    }

    @Override
    public void setPresenter(TaskTrackContact.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return this;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.unSubscribe();
        }
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
