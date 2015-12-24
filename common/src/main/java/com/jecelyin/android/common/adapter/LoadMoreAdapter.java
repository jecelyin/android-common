package com.jecelyin.android.common.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jecelyin.android.common.R;
import com.jecelyin.android.common.widget.OnTryLoadListener;

import java.util.List;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public abstract class LoadMoreAdapter<T> extends RecyclerView.Adapter {
    final public static int TYPE_HEADER = Integer.MIN_VALUE;
    final public static int TYPE_ITEM = 0;
    final public static int TYPE_FOOTER = Integer.MAX_VALUE - 1;
    final public static int TYPE_LOADER = Integer.MAX_VALUE;

    private OnTryLoadListener onTryLoadListener;
    private boolean loading = false;
    private LoadMoreViewHolder loadMoreViewHolder;

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        public final ProgressBar progressBar;
        public final TextView errorTextView;
        public final TextView tryAgainButton;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.ssrl___progress);
            errorTextView = (TextView) itemView.findViewById(R.id.ssrl___error_tv);
            tryAgainButton = (TextView) itemView.findViewById(R.id.ssrl___try_btn);
        }
    }

    public void showLoading(boolean loading) {
        if(loading == this.loading)
            return;
        this.loading = loading;
        //notifyDataSetChanged();
        if(loading) {
            notifyItemInserted(getItemCount());
        }else{
            notifyItemRemoved(getItemCount()+1);
        }
    }

    public void setOnTryLoadListener(OnTryLoadListener onTryLoadListener) {
        this.onTryLoadListener = onTryLoadListener;
    }

    public void setError(String error) {
        if(loadMoreViewHolder == null)
            return;
        loadMoreViewHolder.progressBar.setVisibility(View.GONE);
        loadMoreViewHolder.errorTextView.setText(error);
        loadMoreViewHolder.errorTextView.setVisibility(View.VISIBLE);
        loadMoreViewHolder.tryAgainButton.setVisibility(View.VISIBLE);
    }

    @Override
    final public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_LOADER) {
            return new LoadMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ssrl_load_more, parent, false));
        }
        return onCreateViewHolder2(parent, viewType);
    }

    @Override
    final public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LoadMoreViewHolder) {
            loadMoreViewHolder = (LoadMoreViewHolder) holder;
            if(onTryLoadListener != null) {
                loadMoreViewHolder.tryAgainButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadMoreViewHolder.progressBar.setVisibility(View.VISIBLE);
                        loadMoreViewHolder.errorTextView.setVisibility(View.GONE);
                        loadMoreViewHolder.tryAgainButton.setVisibility(View.GONE);
                        onTryLoadListener.onTryLoadMore();
                    }
                });
            }
            return;
        }
        onBindViewHolder2(holder, position);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder instanceof LoadMoreViewHolder) {
            loadMoreViewHolder = null;
        }
    }

    /**
     * 使用时注意判断loading的情况，比如自定义data时，要判断 positions < data.size()
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if(loading && position >= getItemCount()-1)
            return TYPE_LOADER;
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return getCount() + (loading ? 1 : 0);
    }

    abstract public RecyclerView.ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType);
    abstract public void onBindViewHolder2(RecyclerView.ViewHolder holder, int position);

    /**
     * 仅列表数据部分个数，不包括头，尾这些非列表数据类型个数
     * @return
     */
    abstract public int getCount() ;

    abstract public void setData(List<T> data);

    abstract public void addData(List<T> data);
}
