package com;

import com.dishonest.dao.ConnUtil;
import com.dishonest.handler.DishonestyService;
import com.dishonest.handler.PageHandler;
import com.dishonest.util.HttpUtil;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    int threadPoolSize;
    String hostName;
    boolean hostNameLimit;

    public Main(int threadPoolSize, String hostName, boolean hostNameLimit) {
        this.threadPoolSize = threadPoolSize;
        this.hostName = hostName;
        this.hostNameLimit = hostNameLimit;
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        Main main = new Main(50, System.getenv("COMPUTERNAME"),false);
        main.worker();
    }

    public void worker() throws SQLException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        String querySql = "select * from cred_dishonesty_log where result is null order by to_number(startpage) desc";
        if (hostNameLimit) {
            querySql = "select * from cred_dishonesty_log where result is null and hostname = '" + hostName + "' order by to_number(startpage) desc";
        }
        List list = ConnUtil.getInstance().executeQueryForList(querySql);
        ConnUtil.getInstance().executeSaveOrUpdate("update cred_dishonesty_proxy set isusered = 0 where isusered = 1");
        Iterator it = list.iterator();
        int noProxy = 0;
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            int endpage = Integer.valueOf((String) map.get("ENDPAGE"));
            int startpage = Integer.valueOf((String) map.get("STARTPAGE"));
            int sucessNum = (Integer.valueOf((String) map.get("SUCESSNUM")));
            int sameNum = (Integer.valueOf((String) map.get("SAMENUM")));
            String threadHostName = (String) map.get("HOSTNAME");
            if (threadHostName != null && !"".equals(threadHostName) && !hostName.equals(threadHostName)) {
                continue;
            } else {
                if (noProxy < 5) {
                    PageHandler pageHandler = new PageHandler(startpage, endpage, new HttpUtil(), "", cardNum, hostName, sucessNum, sameNum);
                    noProxy++;
                    threadPool.execute(pageHandler);
                } else {
                    PageHandler pageHandler = new PageHandler(startpage, endpage, new HttpUtil(true, DishonestyService.getProxy(0)), "", cardNum, hostName, sucessNum, sameNum);
                    threadPool.execute(pageHandler);
                }
                Thread.sleep(1000);
            }
        }
        threadPool.shutdown();
    }
}
