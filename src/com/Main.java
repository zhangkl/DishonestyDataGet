package com;

import com.dishonest.handler.DishonestyService;
import com.dishonest.handler.HelpBatch;
import com.dishonest.handler.PageHandler;
import com.dishonest.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    Logger logger = Logger.getLogger(Main.class);

    int threadPoolSize;
    String hostName;
    boolean hostNameLimit;

    public Main(int threadPoolSize, String hostName, boolean hostNameLimit) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
        this.hostNameLimit = hostNameLimit;
    }

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException {
        Main main = new Main(100, System.getenv("COMPUTERNAME"), false);
        main.worker();
    }

    public void worker() throws HttpException, InterruptedException, SQLException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        HttpUtilPool httpUtilPool = new HttpUtilPool(10, threadPoolSize);
        HelpBatch help = new HelpBatch(httpUtilPool,threadPool);
        threadPool.execute(help);
        DishonestyService service = new DishonestyService();
        String querySql = "select * from cred_dishonesty_log ";
        if (hostNameLimit) {
            querySql = "select * from cred_dishonesty_log where hostname = '" + hostName + "'";
        }
        List list = service.getExeList(querySql);
        service.resetProxy();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            int endpage = Integer.valueOf((String) map.get("ENDPAGE"));
            //int startpage = Integer.valueOf((String) map.get("STARTPAGE"));
            int startpage = 1;
            int sucessNum = (Integer.valueOf((String) map.get("SUCESSNUM")));
            int sameNum = (Integer.valueOf((String) map.get("SAMENUM")));
            for (int i = startpage; i < endpage; i++) {
                String pageNum = i + "";
                String sql = "select * from cred_dishonesty_pagelog where cardnum ='" + cardNum + "' and pagenum = '" + pageNum + "'";
                List pagelist = service.getExeList(sql);
                if (pagelist != null && pagelist.size() > 0) {
                    continue;
                }
                PageHandler pageHandler = new PageHandler(pageNum, cardNum, httpUtilPool, hostName, 0, 0);
                threadPool.execute(pageHandler);
            }
        }



        threadPool.shutdown();
    }
}
