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

public class MainForLess {

    Logger logger = Logger.getLogger(MainForLess.class);
    static int dateType = 2;

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        HelpBatch helpBatch = new HelpBatch();
        ses.scheduleAtFixedRate(helpBatch,0,10,TimeUnit.MINUTES);
        HttpUtilPool httpUtilPool = new HttpUtilPool(3);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        DishonestyService service = new DishonestyService();
        String querySql = " select t.* " +
                " from cred_dishonesty_pagelog t, cred_dishonesty_log r" +
                " where t.samenum + t.sucessnum < 10" +
                " and r.cardnum = t.cardnum" +
                " and r.areacode = t.areacode and to_number(r.endpage) > to_number(t.pagenum)";
        if (dateType == 2){
            querySql += " and t.cardnum = '________-_'";
        }else{
            querySql += " and t.cardnum != '________-_'";
        }
        List list = service.getExeList(querySql);
        service.resetProxy();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            String areacode = (String) map.get("AREACODE");
            String pageNum = (String) map.get("PAGENUM");
            PageHandler pageHandler = new PageHandler(dateType,pageNum, cardNum,areacode, httpUtilPool, null, 0, 0, service);
            executorService.execute(pageHandler);
        }
        executorService.shutdown();

    }

}
