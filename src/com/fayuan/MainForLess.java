package com.fayuan;

import com.fayuan.handler.DishonestyService;
import com.fayuan.handler.HelpBatch;
import com.fayuan.handler.PageHandler;
import com.fayuan.util.GetDateException;
import com.fayuan.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainForLess {

    static Logger logger = Logger.getLogger(MainForLess.class);
    static int dateType = 2;

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        HelpBatch helpBatch = new HelpBatch();
        ses.scheduleAtFixedRate(helpBatch, 0, 10, TimeUnit.MINUTES);
        HttpUtilPool httpUtilPool = new HttpUtilPool(8);
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        DishonestyService service = new DishonestyService();
        String querySql = " select t.* " +
                " from cred_dishonesty_pagelog t, cred_dishonesty_log r" +
                " where t.samenum + t.sucessnum < 10" +
                " and r.cardnum = t.cardnum" +
                " and r.areacode = t.areacode and to_number(r.endpage) > to_number(t.pagenum)";
        if (dateType == 2) {
            querySql += " and t.cardnum = ''";
        } else {
            querySql += " and t.cardnum is not null";
        }
        List list = service.getExeList(querySql);
        service.resetProxy();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            String areacode = (String) map.get("AREACODE");
            String pageNum = (String) map.get("PAGENUM");
            String name;
            if (dateType == 2) {
                name = "____";
            } else {
                name = "__";
            }
            PageHandler pageHandler = new PageHandler(name, cardNum, areacode, pageNum, null, httpUtilPool, dateType);
            executorService.execute(pageHandler);
        }
        executorService.shutdown();

    }

}
