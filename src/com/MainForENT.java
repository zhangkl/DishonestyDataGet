package com;

import com.dishonest.handler.DishonestyService;
import com.dishonest.handler.HelpBatch;
import com.dishonest.handler.PageHandler;
import com.dishonest.util.GetDateException;
import com.dishonest.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MainForENT implements Runnable {

    Logger logger = Logger.getLogger(MainForENT.class);

    int threadPoolSize;
    String hostName;
    HttpUtilPool httpUtilPool;
    int sqlPageNum;
    public static boolean timeFlag = true;

    public MainForENT(int threadPoolSize, String hostName, HttpUtilPool httpUtilPool, int sqlPageNum) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
        this.httpUtilPool = httpUtilPool;
        this.sqlPageNum = sqlPageNum;
    }

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException, ExecutionException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        HelpBatch helpBatch = new HelpBatch();
        ses.scheduleAtFixedRate(helpBatch, 0, 60, TimeUnit.SECONDS);
        HttpUtilPool httpUtilPool = new HttpUtilPool(8);
        MainForENT main = new MainForENT(8, "zhangkl", httpUtilPool, 0);
        Thread thread = new Thread(main);
        thread.start();
    }

    public void run() {
        if (timeFlag) {
            try {
                BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100);
                ExecutorService threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, queue);
                DishonestyService service = new DishonestyService(httpUtilPool);
                String querySql = " select nvl(c,0), l.areacode, l.endpage " +
                        " from (select count(*) c, t.areacode from cred_dishonesty_pagelog t" +
                        " group by areacode) r right join cred_dishonesty_log l " +
                        "on r.areacode = l.areacode where (c < to_number(l.endpage) or c is null) and l.cardnum = '________-_' ";
                if (hostName == null) {
                    querySql += " and l.hostname is null ";
                } else if (!"".equals(hostName)) {
                    querySql += " and l.hostname = '" + hostName + "'";
                }
                querySql += " order by nvl(c,0) desc";
                List list;
                if (sqlPageNum > 0) {
                    list = service.getExeListForPage(querySql, sqlPageNum, 3);
                } else {
                    list = service.getExeList(querySql);
                }
                service.resetProxy();
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    String areacode = (String) map.get("AREACODE");
                    int endpage = Integer.valueOf((String) map.get("ENDPAGE"));
                    for (int i = endpage; i > 0; i--) {
                        String pageNum = i + "";
                        String sql = "select * from cred_dishonesty_pagelog where cardnum ='________-_' and areacode = '" + areacode + "' and pagenum = '" + pageNum + "'";
                        List pagelist = service.getExeList(sql);
                        if (pagelist != null && pagelist.size() > 0) {
                            continue;
                        }
                        PageHandler pageHandler = new PageHandler("____", "", areacode, pageNum, hostName, httpUtilPool, service, 2);
                        threadPool.execute(pageHandler);
                        if (queue.size() > 20) {
                            Thread.currentThread().sleep(1000 * 30 * 1);
                            logger.info("开始执行：queueSize" + queue.size() + ",areacode:" + areacode + ",pageNum" + pageNum);
                        }
                    }
                }
                threadPool.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("22:30-24:30点 为网站数据更新时间，线程休眠2小时begin...");
            try {
                Thread.sleep(1000 * 60 * 60 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("22:30-24:30点 为网站数据更新时间，线程休眠2小时end...");
        }

    }

}
