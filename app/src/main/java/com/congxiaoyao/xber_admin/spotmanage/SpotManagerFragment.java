package com.congxiaoyao.xber_admin.spotmanage;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.congxiaoyao.httplib.response.Spot;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableViewImpl;
import com.congxiaoyao.xber_admin.utils.DisplayUtils;
import com.congxiaoyao.xber_admin.utils.MathUtils;
import com.daimajia.swipe.SwipeLayout;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by guo on 2017/3/24.
 */

public class SpotManagerFragment extends ListLoadableViewImpl<SpotManagerContract.Presenter,Spot>
        implements SpotManagerContract.View{

    private Queue<SwipeLayout> openedItems = new LinkedList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spot_manager, container, false);
        progressBar = (ContentLoadingProgressBar) view.findViewById(R.id.content_progress_bar);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.spot_recycler);
        super.onCreateView(inflater, container, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration
                .Builder(getContext())
                .size(1)
                .margin(DisplayUtils.dp2px(getContext(), 16))
                .colorResId(R.color.colorLightGray)
                .build());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                switch (view.getId()) {
                    case R.id.tv_spot_delete:
                        showDeleteDialog(getData().get(position));
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                closeOpenedItems();
                            }
                        }, 200);
                        break;
                    case R.id.tv_spot_revise:
                        SelectSpotActivity.startForUpdate(SpotManagerFragment.this,
                                getData().get(position));
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                closeOpenedItems();
                            }
                        }, 200);
                        break;
                    case R.id.btn_more:
                        ((SwipeLayout) view.getParent().getParent()).open();
                        break;
                }
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (openedItems.isEmpty()) return false;
                closeOpenedItems();
                return true;
            }
        });
        view.findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectSpotActivity.startForAdd(SpotManagerFragment.this);
            }
        });
        if (presenter != null) {
            presenter.subscribe();
        }
        return view;
    }

    @Override
    public boolean isSupportSwipeRefresh() {
        return false;
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.item_spot_manager;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Spot data) {
        super.convert(viewHolder, data);
        viewHolder.setText(R.id.tv_spot_name, data.getSpotName());
        viewHolder.addOnClickListener(R.id.tv_spot_delete);
        viewHolder.addOnClickListener(R.id.tv_spot_revise);
        viewHolder.addOnClickListener(R.id.btn_more);

        SwipeLayout swipeLayout = (SwipeLayout) viewHolder.itemView;
        swipeLayout.addSwipeListener(new SwipeListenerAdapter(){
            @Override
            public void onStartOpen(SwipeLayout layout) {
                closeOpenedItems();
                openedItems.offer(layout);
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                int total = -DisplayUtils.dp2px(getContext(), 160);
                float alpha = MathUtils.map(0, total, 1, 0, leftOffset);
                layout.findViewById(R.id.btn_more).setAlpha(alpha);
                if (leftOffset == 0) {
                    closeOpenedItems();
                }
            }

            @Override
            public void onClose(SwipeLayout layout) {
            }
        });
    }

    private void closeOpenedItems() {
        while (!openedItems.isEmpty()) {
            SwipeLayout openedItem = openedItems.poll();
            openedItem.close();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            presenter.refreshData();
        }
    }

    @Override
    public void scrollToTop() {

    }

    @Override
    public void hideSwipeRefreshLoading() {
        hideLoading();
    }

    public void showDeleteDialog(final Spot spot) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("是否删除该位置");
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.remove(spot);
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    public void showReviseDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("是否修改该位置");
        dialog.setView(R.layout.dialog_layout);
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getContext(), "爱改不改", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "不给你改", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

}
