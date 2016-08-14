package com.threadpool;

/**
 * Created by zxl on 2016/7/5.
 */
public interface WorkTask {

    public void runTask();

    //public int compareTo(mJob job);
    public void cancelTask();

    public int getProgress();


}
