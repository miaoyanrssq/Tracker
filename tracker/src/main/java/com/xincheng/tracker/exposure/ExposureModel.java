/*
 * Copyright （C）2010-2017 Alibaba Group Holding Limited
 */

package com.xincheng.tracker.exposure;


import java.util.HashMap;

public class ExposureModel implements Cloneable {
    public String tag;

    public long beginTime = 0;
    public long endTime = 0;

    public HashMap<String, Object> params;

    /**
     * deep copy
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() {
        ExposureModel exposureModel = null;

        try {
            exposureModel = (ExposureModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (exposureModel != null && this.params != null) {
            exposureModel.params = (HashMap<String, Object>) this.params.clone();
        }

        return exposureModel;
    }
}
