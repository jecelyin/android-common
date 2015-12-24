package com.jecelyin.android.common.bean;

import java.util.List;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class ListBean<T> extends BaseBean {
    private int totalPage;
    private int currPage;

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public List<T> getDataList() {
        return null;
    }
}
