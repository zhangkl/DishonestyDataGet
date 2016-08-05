package com;

import com.dishonest.handler.DishonestyService;
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

public class MainForLess {

    Logger logger = Logger.getLogger(MainForLess.class);

    int threadPoolSize;
    String hostName;
    boolean hostNameLimit;

    public MainForLess(int threadPoolSize, String hostName, boolean hostNameLimit) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
        this.hostNameLimit = hostNameLimit;
    }

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException {
        MainForLess main = new MainForLess(5, System.getenv("COMPUTERNAME"), false);
        main.worker();
    }

    public void worker() throws HttpException, InterruptedException, SQLException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        HttpUtilPool httpUtilPool = new HttpUtilPool(5, threadPoolSize);
        DishonestyService service = new DishonestyService();
        String querySql = "select * from cred_dishonesty_pagelog t where t.samenum + t.sucessnum < 10";
        if (hostNameLimit) {
            querySql = "select * from cred_dishonesty_pagelog t where t.samenum + t.sucessnum < 10 and hostname = '" + hostName + "'";
        }
        List list = service.getExeList(querySql);
        service.resetProxy();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            String pageNum = (String) map.get("PAGENUM");
            PageHandler pageHandler = new PageHandler(pageNum, cardNum, httpUtilPool, hostName, 0, 0);
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();
    }
}
