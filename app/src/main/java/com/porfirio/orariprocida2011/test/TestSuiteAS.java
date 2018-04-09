package com.porfirio.orariprocida2011.test;

import java.util.ArrayList;

public class TestSuiteAS {
    public static ArrayList<TestValue> valueList = new ArrayList<TestValue>();
    public static int testNumber = 0;

    public static int getDelay(String s) {
        if (valueList.size() > 0) {
            TestValue tv = valueList.get(testNumber);
            if (tv.taskDelay.containsKey(s))
                return tv.taskDelay.get(s);
        }
        return 0;
    }

    public static void setDelay(String s, int d) {
        TestValue tv = valueList.get(testNumber);
        tv.taskDelay.put(s, d);
        return;
    }

    public static void addTest(int n) {
        valueList.add(n, new TestValue());
    }
}
