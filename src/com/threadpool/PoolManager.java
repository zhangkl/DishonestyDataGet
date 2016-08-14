package com.threadpool;


import java.util.ArrayList;

/**
 * Created by zxl on 2016/7/5.
 */
public class PoolManager {
    public static PoolManager mPool = new PoolManager();
    public static int max_pool = 1;
    public final int max_Tasks = 20;
    public static ArrayList<Worker> init_pools;
    //  private int GetIdleThreadPollTime=50;//获取空闲线程轮询间隔时间,可配置
    private TaskMonitorThread mainThread;//任务监测线程

    static {
        init_pools = new ArrayList<Worker>(max_pool);
    }

    public static PoolManager getInstance() {
        if (mPool == null) {
            mPool = new PoolManager();
        }

        return mPool;
    }

    //获取空闲线程
    public Worker getIdleThread() {
        Worker working = null;
        while (true) {
            synchronized (init_pools) {
                for (int i = 0; i < max_pool; i++) {
                    //Worker working = init_pools.get(i);
                    working = init_pools.get(i);
                    if (!working.isrunning) {
                        //    System.out.println("工作将由闲置线程" + working.getThreadTag() + "执行");
                        return working;
                    }
                }
            }
            try {
                Thread.sleep(5000);//放弃CPU,若干时间后重新获取空闲线程
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }

    public void init() {
        System.out.println("线程池初始化开始。。。");
        Worker worker = null;
        for (int i = 0; i < this.max_pool; i++) {
            worker = new Worker("initThread" + i);
            init_pools.add(worker);
            worker.start();
        }
        mainThread = new TaskMonitorThread();
        mainThread.start();
        System.out.println("结束初始化线程池...");
    }

    public void destory() {
        init_pools.clear();
    }

}


