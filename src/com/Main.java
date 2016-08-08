package com;

import com.dishonest.handler.DishonestyService;
import com.dishonest.handler.PageHandler;
import com.dishonest.util.GetDateException;
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

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException {
        Main main = new Main(56, "zhangkl", true);
//        Main main = new Main(56, System.getenv("COMPUTERNAME"), true);
        main.worker();
    }

    public void worker() throws HttpException, InterruptedException, SQLException, GetDateException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        HttpUtilPool httpUtilPool = new HttpUtilPool(7, threadPoolSize);

        DishonestyService service = new DishonestyService();
        String querySql = "select * from cred_dishonesty_log t where  to_number(t.startpage) < to_number(t.endpage) ";
        if (hostNameLimit) {
            querySql += "and hostname = '" + hostName + "'";
        }
        querySql += "order by to_number(t.startpage) desc";
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
            for (int i = startpage; i <= endpage; i++) {
                String pageNum = i + "";
                String sql = "select * from cred_dishonesty_pagelog where cardnum ='" + cardNum + "' and pagenum = '" + pageNum + "'";
                List pagelist = service.getExeList(sql);
                if (pagelist != null && pagelist.size() > 0) {
                    continue;
                }
                for (int j = 660; j < 694; j++) {
                    PageHandler pageHandler = new PageHandler(pageNum, cardNum, httpUtilPool, hostName, 0, 0);
                    threadPool.execute(pageHandler);
                }
            }
        }
        threadPool.shutdown();
    }
}
