package com.jecelyin.android.common.api;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 * @since 2014.01.13
 */
public class CrashReportApi extends AbstractApi
{
    @Override
    protected String getPath()
    {
        return "/report/report_app_error";
    }

    @Override
    public Method requestMethod() {
        return Method.POST;
    }

    private String appVersion, phoneModel, androidVersion
            ,screen, freeMem, trace, email, comment;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(String freeMem) {
        this.freeMem = freeMem;
    }

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
