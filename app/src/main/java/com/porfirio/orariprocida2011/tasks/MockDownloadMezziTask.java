package com.porfirio.orariprocida2011.tasks;

import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

/**
 * Created by Porfirio on 16/02/2018.
 */

public class MockDownloadMezziTask extends DownloadMezziTask {

    public int delay;

    public MockDownloadMezziTask(OrariProcida2011Activity orariProcida2011Activity, int d) {
        super(orariProcida2011Activity, d);
        super.setDelay(d);

    }

}