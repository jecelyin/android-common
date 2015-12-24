package com.jecelyin.android.common.http;

import com.jecelyin.android.common.bean.BaseBean;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class BeanStatusException extends Exception {
    private final BaseBean baseBean;

    public BeanStatusException(String detailMessage, BaseBean bean) {
        super(detailMessage);
        this.baseBean = bean;
    }

    public BaseBean getBaseBean() {
        return baseBean;
    }
}
