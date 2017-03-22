package com.congxiaoyao.xber_admin.mvpbase.presenter;

import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.httplib.response.exception.NetWorkException;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by congxiaoyao on 2017/3/16.
 */

public abstract class ListLoadablePresenterImpl<T extends ListLoadableView> extends BasePresenterImpl<T>
        implements ListLoadablePresenter, Action1<Throwable> {

    protected boolean isLoading = false;

    public ListLoadablePresenterImpl(T view) {
        super(view);
    }

    @Override
    public void refreshData() {
        //如果正在加载中 也可能是别的方法正在加载 所以我这里先不下拉刷新
        if (isLoading) {
            view.hideSwipeRefreshLoading();
            return;
        }

        //先试探下子类有没有返回可用的observable 如果返回null可以当做出现了异常的一个信号
        Observable<? extends List> observable = pullListData();
        //如果子类觉得当前无法请求比如参数错误 或没有token等 可以返回null然后这里就直接return
        if (observable == null) return;

        //标记正在加载
        isLoading = true;
        //拉取数据并处理
        Subscription subscription = observable
                .doOnNext(new Action1<List>() {
                    @Override
                    public void call(List list) {
                        if (list == null || list.size() == 0) throw new EmptyDataException();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List>() {
                    @Override
                    public void call(List list) {
                        //等加载成功了再清掉老的乱七八糟的view
                        view.showNothing();
                        //添加刚刚获得的数据
                        view.addData(list);
                        //隐藏下拉加载进度条
                        view.hideSwipeRefreshLoading();
                        //重置加载状态
                        isLoading = false;
                    }
                }, this);
        subscriptions.add(subscription);
    }

    @Override
    public void subscribe() {
        //如果正在加载数据 则忽略请求
        if (isLoading) return;
        //先试探下子类有没有返回可用的observable 如果返回null可以当做出现了异常的一个信号
        Observable<? extends List> observable = pullListData();
        //如果子类觉得当前无法请求比如参数错误 或没有token等 可以返回null然后这里就直接return
        if (observable == null) return;

        //如果有乱七八糟的东西先清掉东西
        view.showNothing();
        //显示一个加载中进度条
        view.showLoading();

        //标记正在加载
        isLoading = true;
        //拉取数据并处理
        Subscription subscribe = observable
                .doOnNext(new Action1<List>() {
                    @Override
                    public void call(List list) {
                        if (list == null || list.size() == 0) throw new EmptyDataException();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List>() {
                    @Override
                    public void call(List list) {
                        //拉取成功 隐藏进度条
                        view.hideLoading();
                        //更新数据
                        view.addData(list);
                        //恢复状态
                        isLoading = false;
                    }
                }, this);
        subscriptions.add(subscribe);
    }

    /**
     * 错误处理
     * @param throwable
     */
    @Override
    public void call(Throwable throwable) {
        exceptionDispatcher.dispatchException(throwable);
    }

    public abstract Observable<? extends List> pullListData();

    @Override
    public void unSubscribe() {
        subscriptions.unsubscribe();
    }

    /**
     * 当出现了错误 首先要做的就是取消加载进度条 重置加载状态
     *
     * @param throwable
     */
    @Override
    public void onDispatchException(Throwable throwable) {
        view.hideLoading();
        view.hideSwipeRefreshLoading();
        isLoading = false;
    }

    @Override
    public void onNetworkError(NetWorkException exception) {
        super.onNetworkError(exception);
        view.showNetworkError();
    }

    @Override
    public void onEmptyDataError(EmptyDataException exception) {
        view.showDataEmpty();
    }
}
