package com;

import com.dishonest.handler.CardHandler;
import com.dishonest.dao.ConnUtil;
import com.dishonest.handler.DishonestyService;
import com.dishonest.util.HttpUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    int threadPoolSize ;
    String hostName ;

    public Main(int threadPoolSize, String hostName) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        Main main = new Main(50, System.getenv("COMPUTERNAME"));
        main.worker();
    }

    public void worker() throws SQLException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        String querySql = "select * from cred_dishonesty_log where result is null order by to_number(startpage) desc";
        List list = ConnUtil.getInstance().executeQueryForList(querySql);
        ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where isusered = 1");
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            String endpage = (String) map.get("ENDPAGE");
            String startpage = (String) map.get("STARTPAGE");
            int sucessNum = (Integer.valueOf((String) map.get("SUCESSNUM")));
            int sameNum = (Integer.valueOf((String)map.get("SAMENUM"))) ;
            String threadHostName = (String) map.get("HOSTNAME");
            if (threadHostName != null && !"".equals(threadHostName) && !hostName.equals(threadHostName)) {
                continue;
            } else {
                CardHandler cardHandler = new CardHandler("", cardNum, Integer.valueOf(startpage), Integer.valueOf(endpage), sucessNum, sameNum, hostName);
                threadPool.execute(cardHandler);
                Thread.sleep(1000);
            }
        }
    }
}
