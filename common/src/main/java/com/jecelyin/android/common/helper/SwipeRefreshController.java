package com.jecelyin.android.common.helper;

import android.content.Context;

import com.jecelyin.android.common.adapter.LoadMoreAdapter;
import com.jecelyin.android.common.api.AbstractApi;
import com.jecelyin.android.common.bean.ListBean;
import com.jecelyin.android.common.http.BeanResponse;
import com.jecelyin.android.common.http.HttpClient;
import com.jecelyin.android.common.http.HttpRequest;
import com.jecelyin.android.common.http.HttpResponse;
import com.jecelyin.android.common.utils.L;
import com.jecelyin.android.common.widget.OnLoadingListener;
import com.jecelyin.android.common.widget.OnTryLoadListener;
import com.jecelyin.android.common.widget.SmartSwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class SwipeRefreshController<T2 extends ListBean> implements OnLoadingListener, OnTryLoadListener {
    private final WeakReference<SmartSwipeRefreshLayout> pullToRefresh;
    private final LoadMoreAdapter adapter;
    private final AbstractApi api;
    private final HttpClient httpClient;
    private int page;
    private int totalPage;
    private String tag;
    private final Type type;
    private CacheEnum cache = CacheEnum.CACHE_FIRST_PAGE;
    private boolean mIsCacheResult = false;
    public static enum CacheEnum {
        CACHE_FIRST_PAGE,
        CACHE_ALL,
        NO_CACHE
    }

    public SwipeRefreshController(Context context, SmartSwipeRefreshLayout smartSwipeRefreshLayout, AbstractApi api, LoadMoreAdapter adapter) {
        if(smartSwipeRefreshLayout.getAdapter() == null) {
            L.e("Please set the " + adapter.getClass().getSimpleName() + " to SmartSwipeRefreshLayout.");
        }
        this.pullToRefresh = new WeakReference<>(smartSwipeRefreshLayout);
        this.adapter = adapter;
        this.api = api;
        Type superClass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        httpClient = HttpClient.newInstance(context);
        smartSwipeRefreshLayout.setOnLoadingListener(this);
        smartSwipeRefreshLayout.setOnTryLoadListener(this);
    }

    @Override
    public void onRefresh() {
        loadFirstPage();
    }

    @Override
    public void onLoadMore() {
        loadNextPage();
    }

    @Override
    public void onTryRefresh() {
        onRefresh();
    }

    @Override
    public void onTryLoadMore() {
        onLoadMore();
    }

    public void loadFirstPage() {
        loadPage(1);
    }

    public void loadNextPage() {
        if(page >= totalPage) {
            if(pullToRefresh.get() == null)
                return;
            pullToRefresh.get().setLoadingMore(false);
            return;
        }
        loadPage(page + 1);
    }

    private void loadPage(int p) {
        api.setPage(p);
        if(cache == CacheEnum.CACHE_FIRST_PAGE) {
            if(p == 1) {
                httpClient.setUseCache(true);
            } else {
                httpClient.setUseCache(false);
            }
        } else if(cache == CacheEnum.CACHE_ALL) {
            httpClient.setUseCache(true);
        } else {
            httpClient.setUseCache(false);
        }

        SmartSwipeRefreshLayout view = pullToRefresh.get();
        if(view == null)
            return;
        view.showLoading();

        tag = httpClient.request(api, new BeanResponse<T2>() {
            @Override
            public void onResponse(HttpClient httpClient, HttpResponse response, T2 bean) {
                mIsCacheResult = response.isCacheResponse();
                page = bean.getCurrPage();
                totalPage = bean.getTotalPage();

                SmartSwipeRefreshLayout view = pullToRefresh.get();
                if (view == null) //被回收或离开了当前页面
                    return;
                //是否自己处理结果
                if (!onSuccessResponse(httpClient, response, bean)) {
                    List data = bean.getDataList();
                    if (data != null) {
                        if (page > 1) {
                            adapter.addData(data);
                        } else {
                            adapter.setData(data);
                        }
                    }
                }
                /**
                 * {@link android.widget.ListView#layoutChildren}
                 * 数据修改后，要马上通知Adapter数据已经改变，期间若做其它操作会导致异常
                 */
                adapter.notifyDataSetChanged();
//                Log.d(SwipeRefreshController.class.getName(), "page="+page+" totalPage="+totalPage);
                if (page >= totalPage) {
                    view.setMode(SmartSwipeRefreshLayout.Mode.REFRESH);
                } else {
                    view.setMode(SmartSwipeRefreshLayout.Mode.BOTH);
                }
                if (!mIsCacheResult) {
                    view.hideLoading();
                } else {
                    //数据变动后，可能就没有加载状态显示了，再显示一下
                    view.showLoading();
                }
            }

            @Override
            public void onFailure(HttpClient httpClient, HttpRequest request, Exception e) {
                SmartSwipeRefreshLayout view = pullToRefresh.get();
                if(view == null)
                    return;

                //SmartSwipeRefreshLayout不显示错误时，调用默认规则显示错误
                if(!view.setError(e.getMessage()))
                    super.onFailure(httpClient, request, e);

                //注意setError需要正确地判断是否正在loading
                view.hideLoading();
            }
        });

    }

    public void close() {
        httpClient.cancel(tag);
    }

    /**
     * 返回true表示手动处理返回数据，false表示由本控制器处理返回数据到Adapter
     * @param response
     * @return
     */
    public boolean onSuccessResponse(HttpClient httpClient, HttpResponse response, T2 bean) {
        return false;
    }

    public void setCacheType(CacheEnum cache) {
        this.cache = cache;
    }

    public boolean isCacheResult() {
        return mIsCacheResult;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPage() {
        return totalPage;
    }
}
