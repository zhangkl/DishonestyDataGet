package com;

import com.dishonest.handler.DBLogHandler;
import com.dishonest.handler.DishonestyService;
import com.dishonest.util.HttpUtil;
import org.apache.http.HttpException;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainForGetLog {

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException {
        MainForGetLog main_forGetLog = new MainForGetLog();
        main_forGetLog.worker();
    }

    public void worker() throws SQLException, InterruptedException, HttpException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        /*HttpUtilPool httpUtilPool = new HttpUtilPool(1, 7);*/
        /*HelpBatch help = new HelpBatch(httpUtilPool,threadPool);
        threadPool.execute(help);*/
        String querySql = "select * from cred_dishonesty_log where dcurrentdate < to_date('2016-08-08','yyyy-mm-dd')";
        DishonestyService service = new DishonestyService();
        List list = service.getExeList(querySql);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            DBLogHandler pageHandler = new DBLogHandler("0",cardNum,new HttpUtil(), "");
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();
    }
}
