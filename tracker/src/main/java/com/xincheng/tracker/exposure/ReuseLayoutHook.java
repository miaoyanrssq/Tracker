/*
 * Copyright （C）2010-2017 Alibaba Group Holding Limited
 */

package com.xincheng.tracker.exposure;

import android.view.View;


import androidx.viewpager.widget.ViewPager;

import com.xincheng.tracker.layout.TrackLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author wuzhiji on 17/4/12.
 */
public class ReuseLayoutHook {

    private static final int HOOK_VIEW_TAG = -9100;

    private TrackLayout mRootLayout;
    private HashMap<String, Object> mCommonInfo;
    private List<ViewHookListener> mList = new ArrayList<ViewHookListener>();

    private interface ViewHookListener {
        boolean isValid(View view);

        void hookView(View view);
    }


    private class ViewPagerHook implements ViewHookListener {

        @Override
        public boolean isValid(View view) {
            return view instanceof ViewPager;
        }

        @Override
        public void hookView(View view) {
            ViewPager viewPager = (ViewPager) view;
            Object tag = viewPager.getTag(HOOK_VIEW_TAG);
            if (tag != null && !(tag instanceof Boolean)) {
                return;
            }
            Boolean added = (Boolean) tag;
            if (added != null && added) {
                return;
            }
            viewPager.addOnPageChangeListener(new ViewPagerOnPageChangeListener());
            viewPager.setTag(HOOK_VIEW_TAG, true);
        }
    }

    public ReuseLayoutHook(TrackLayout rootLayout, HashMap<String, Object> commonInfo) {
        this.mRootLayout = rootLayout;
        this.mCommonInfo = commonInfo;
        mList.add(new ViewPagerHook());
    }

    public void checkHookLayout(View view) {
        for (ViewHookListener listener : mList) {
            if (listener != null && listener.isValid(view)) {
                listener.hookView(view);
            }
        }
    }


    private class ViewPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

        private int state = ViewPager.SCROLL_STATE_IDLE;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (state != ViewPager.SCROLL_STATE_SETTLING) {
//                ExposureManager.getInstance().triggerViewCalculate(TrackerInternalConstants.TRIGGER_VIEW_CHANGED, mRootLayout, mCommonInfo, mRootLayout.getLastVisibleViewMap());
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (this.state == ViewPager.SCROLL_STATE_SETTLING && state == ViewPager.SCROLL_STATE_IDLE) {
//                ExposureManager.getInstance().triggerViewCalculate(TrackerInternalConstants.TRIGGER_VIEW_CHANGED, mRootLayout, mCommonInfo, mRootLayout.getLastVisibleViewMap());
            }
            this.state = state;
        }
    }

}
