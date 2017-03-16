package com.congxiaoyao.xber_admin.mvpbase.presenter;

import com.congxiaoyao.httplib.response.Page;
import com.congxiaoyao.httplib.response.Pageable;
import com.congxiaoyao.httplib.response.ResponsePreProcess;
import com.congxiaoyao.httplib.response.exception.EmptyDataException;
import com.congxiaoyao.httplib.response.exception.NetWorkException;
import com.congxiaoyao.xber_admin.mvpbase.view.ListLoadableView;
import com.congxiaoyao.xber_admin.mvpbase.view.PagedListLoadableViewImpl;
import com.congxiaoyao.xber_admin.mvpbase.view.SimplePagedListLoadableView;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * isLoading变量的线程同步问题
 * 这是一个关于{@link PagedListLoadablePresenter}接口的实现类
 * 与之对应的view为{@link PagedListLoadableViewImpl}
 * 或者是{@link SimplePagedListLoadableView}
 * 此Presenter为了一站式解决ListLoadable系列presenter的所有问题：
 * <ul>
 * <li>初始状态下应该如何拉取数据并操作view</li>
 * <li>滑动到底部时应该如何拉取数据并操作view</li>
 * <li>触发下拉刷新时应该如何拉取数据并操作view</li>
 * </ul>
 * <p>
 * 其实上述三个问题的解决方法大同小异 而每个方法又会有细微差别 所以 这里提出一个抽象方法
 * {@link PagedListLoadablePresenterImpl#pullPagedListData(int)}
 * 只要子类实现这个方法 这里作为父类便可以实现上述三个功能
 * <p>
 * 由于类内持有的view为{@link ListLoadableView}的子类 所以这里可以实现一部分错误处理方法
 * 关于{@link PagedListLoadablePresenterImpl#isLoading}的使用不是线程安全的 可能会带来一些影响
 * 其他详细注释见方法
 * <p>
 * Created by congxiaoyao on 2016/8/26.
 */
public abstract class PagedListLoadablePresenterImpl<T extends ListLoadableView>
        extends BasePresenterImpl<T>
        implements PagedListLoadablePresenter, Action1<Throwable>, Action2<Page, Date> {


    protected boolean isLoading = false;
    protected Date timeStamp;
    private Page page;

    public PagedListLoadablePresenterImpl(T view) {
        super(view);
    }

    @Override
    public void savePage(Date timeStamp, Page page) {
        this.timeStamp = timeStamp;
        this.page = page;
    }

    @Override
    public boolean hasMoreData() {
        if (page == null) return false;
        return page.getNext() != 0;
    }

    /**
     * 拉取当前业务逻辑类所需要的数据 一般来说 只需要调用各种Request接口中的方法就好了
     * 他们的返回值就是这个方法所需要的返回值
     *
     * @param page 需要请求哪一页
     * @param <D>
     * @return
     */
    public abstract <D> Observable<D> pullPagedListData(int page);

    /**
     * 适用于初始状态下加载数据 如果不是什么都没有的状态 我这里也会清除为什么都没有的状态
     * 加载过程中会显示一个加载的进度条 然后加载结束或出错的时候隐藏进度条
     */
    @Override
    public void subscribe() {
        //如果正在加载数据 则忽略请求
        if (isLoading) return;
        //这个方法视为从最初状态开始加载数据 所以page也要初始化为null
        page = null;

        //先试探下子类有没有返回可用的observable 如果返回null可以当做出现了异常的一个信号
        Observable<Pageable<Object>> observable = pullPagedListData(1);
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
                .compose(ResponsePreProcess.pagedListDataToBeanList(this))
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
     * 适用于刷新数据 这里说的刷新指的是{@link android.support.v4.widget.SwipeRefreshLayout}中的
     * 下拉刷新 同样也是获取第一页的内容 在刷新结束后隐藏下拉刷新的进度条
     */
    @Override
    public void refreshData() {
        //如果正在加载中 也可能是别的方法正在加载 所以我这里先不下拉刷新
        if (isLoading) {
            view.hideSwipeRefreshLoading();
            return;
        }

        //先试探下子类有没有返回可用的observable 如果返回null可以当做出现了异常的一个信号
        Observable<Pageable<Object>> observable = pullPagedListData(1);
        //如果子类觉得当前无法请求比如参数错误 或没有token等 可以返回null然后这里就直接return
        if (observable == null) return;

        //标记正在加载
        isLoading = true;
        //拉取数据并处理
        Subscription subscription = observable
                .compose(ResponsePreProcess.pagedListDataToBeanList(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> list) {
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

    /**
     * 这个方法对应着滑动到底部加载更多 这个时候就会根据当前保存的page来获取相应数据
     */
    @Override
    public void loadMoreData() {
        //惯例判断正在加载和是否能继续加载
        if (isLoading) return;
        if (!hasMoreData()) return;

        //先试探下子类有没有返回可用的observable 如果返回null可以当做出现了异常的一个信号
        Observable<Pageable<Object>> observable = pullPagedListData
                (page.getNext());
        //如果子类觉得当前无法请求比如参数错误 或没有token等 可以返回null然后这里就直接return
        if(observable == null) return;

        isLoading = true;
        Subscription subscribe = observable
                .compose(ResponsePreProcess.pagedListDataToBeanList(this))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> list) {
                        //添加刚刚获得的数据
                        view.addData(list);
                        //重置加载状态
                        isLoading = false;
                    }
                },this);
        subscriptions.add(subscribe);
    }

    /**
     * 处理错误
     *
     * @param throwable
     */
    @Override
    public void call(Throwable throwable) {
        exceptionDispatcher.dispatchException(throwable);
    }

    /**
     * 分页信息保存
     *
     * @param page
     * @param date
     */
    @Override
    public void call(Page page, Date date) {
        savePage(date, page);
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
        super.onEmptyDataError(exception);
        view.showDataEmpty();
    }
}