package com.jecelyin.android.common.app;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;


/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class JecFragment extends Fragment {
    /**
     * 在当前Fragment开启另外一个Fragment
     * @param fragment
     */
    protected void startFragment(Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(getId(), fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

}
