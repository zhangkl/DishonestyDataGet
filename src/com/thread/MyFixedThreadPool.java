/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class MyFixedThreadPool extends ThreadPoolExecutor {

    public MyFixedThreadPool(int poolsize) {
        super(poolsize, poolsize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        System.out.println("before:" + System.currentTimeMillis() + ":" + t.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        System.out.println("end:" + System.currentTimeMillis() + ":" + r);
    }

    @Override
    protected void terminated() {
        System.out.println("terminated getCorePoolSize:" + this.getCorePoolSize() + "；getPoolSize:" + this.getPoolSize() + "；getTaskCount:" + this.getTaskCount() + "；getCompletedTaskCount:"
                + this.getCompletedTaskCount() + "；getLargestPoolSize:" + this.getLargestPoolSize() + "；getActiveCount:" + this.getActiveCount());
    }
}
