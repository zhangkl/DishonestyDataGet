package com;

import com.dishonest.dao.ConnUtil;
import com.dishonest.handler.PageHandler;
import com.dishonest.util.HttpUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainForGetLog {

    public static void main(String[] args) throws SQLException, InterruptedException {
        MainForGetLog main_forGetLog = new MainForGetLog();
        main_forGetLog.worker();
    }

    public void worker() throws SQLException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        String querySql = "select * from cred_dishonesty_log where allcount is null ";
        List list = ConnUtil.getInstance().executeQueryForList(querySql);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            PageHandler pageHandler = new PageHandler(0, 0, new HttpUtil(), cardNum, "", 0, 0);
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();
    }
}
