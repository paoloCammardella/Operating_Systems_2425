package com.porfirio.orariprocida2011.test;

import com.porfirio.orariprocida2011.tasks.DownloadMezziTask;

/**
 * Created by Porfirio on 27/03/2018.
 */

public class TestValues {
    public static int delay = 0;

    public static int getDelay(Class c) {
        if (c.toString().equals(DownloadMezziTask.class.toString()))
            return delay;
        else
            return 0;
    }

    public static void setDelay(int delay) {
        TestValues.delay = delay;
    }
}
