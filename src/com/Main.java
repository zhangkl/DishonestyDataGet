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

public class Main implements Runnable {

    Logger logger = Logger.getLogger(Main.class);

    int threadPoolSize;
    String hostName;
    HttpUtilPool httpUtilPool;
    int sqlPageNum;
    int dataType;              //1为个人 2为企业
    public static boolean timeFlag = true;



    public Main(int threadPoolSize, String hostName, HttpUtilPool httpUtilPool, int sqlPageNum,int dataType) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
        this.httpUtilPool = httpUtilPool;
        this.sqlPageNum = sqlPageNum;
        this.dataType = dataType;
    }


    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException, ExecutionException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        HelpBatch helpBatch = new HelpBatch();
        ses.scheduleAtFixedRate(helpBatch, 0, 10, TimeUnit.MINUTES);
        int pageNum = 0;
        for (int i = 0; i < 6; i++) {
            pageNum++;
            HttpUtilPool httpUtilPool = new HttpUtilPool(5);
            Main main = new Main(5, System.getenv("COMPUTERNAME"), httpUtilPool, pageNum,1);
            Thread thread = new Thread(main);
            thread.start();
        }
    }

    public void run() {
        if (timeFlag) {
            try {
                BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(150);
                ExecutorService threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, queue);

                DishonestyService service = new DishonestyService();
                String querySql = " select nvl(c,0), l.cardnum, l.endpage " +
                        " from (select count(*) c, t.cardnum from cred_dishonesty_pagelog t" +
                        " group by cardnum) r right join cred_dishonesty_log l " +
                        "on r.cardnum = l.cardnum where (c < to_number(l.endpage) or c is null) and l.cardnum not like '________-_' ";
                if (hostName == null) {
                    querySql += " and l.hostname is null ";
                } else if (!"".equals(hostName)) {
                    querySql += " and l.hostname = '" + hostName + "'";
                }
                querySql += " order by nvl(c,0) desc";
                List list;
                if (sqlPageNum > 0) {
                    list = service.getExeListForPage(querySql, sqlPageNum, 1);
                } else {
                    list = service.getExeList(querySql);
                }
                service.resetProxy();
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Map map = (Map) it.next();
                    String cardNum = (String) map.get("CARDNUM");
                    String areacode = (String) map.get("AREACODE");
                    int endpage = Integer.valueOf((String) map.get("ENDPAGE"));
                    int startpage = 1;
                    for (int i = startpage; i <= endpage; i++) {
                        String pageNum = i + "";
                        String sql = "select * from cred_dishonesty_pagelog where cardnum ='" + cardNum + "' and pagenum = '" + pageNum + "'";
                        List pagelist = service.getExeList(sql);
                        if (pagelist != null && pagelist.size() > 0) {
                            continue;
                        }
                        String name ;
                        if(dataType == 1){
                            name = "__";
                        } else{
                            name = "____";
                        }
                        PageHandler pageHandler = new PageHandler(name, cardNum,areacode,pageNum,hostName, httpUtilPool, service,dataType);
                        threadPool.execute(pageHandler);
                        if (queue.size() > 100) {
                            Thread.currentThread().sleep(1000 * 60 * 1);
                            logger.info("开始执行：queueSize" + queue.size()+",cardNum:"+cardNum+",pageNum"+pageNum);
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
            logger.info("22点-24点 为网站数据更新时间，线程休眠2小时begin...");
            try {
                Thread.sleep(1000 * 60 * 60 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("22点-24点 为网站数据更新时间，线程休眠2小时end...");
        }

    }
}
