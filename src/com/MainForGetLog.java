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
        main_forGetLog.workforENT();
    }

    public void workforENT() throws SQLException, InterruptedException, HttpException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        String[] areaArray = {"660", "661", "662", "663", "664", "665", "666", "667", "668", "669", "670", "671", "672", "673", "674", "675", "676", "677", "678", "679", "680", "681", "682", "683", "684", "685", "686", "687", "688", "689", "690", "691", "692", "693"};
        for (int i = 0; i < areaArray.length; i++) {
            DBLogHandler dlh = new DBLogHandler("________-_",areaArray[i],new HttpUtil());
            threadPool.execute(dlh);
        }
        String querySql = "select * from cred_dishonesty_log t where t.cardnum = '________-_' and endpage = 1 and areacode not in ('693','692','691')";
        DishonestyService service = new DishonestyService();
        List list = service.getExeList(querySql);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            DBLogHandler pageHandler = new DBLogHandler("________-_", (String) map.get("AREACODE"),new HttpUtil());
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();

    }

    public void worker() throws SQLException, InterruptedException, HttpException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
        /*HttpUtilPool httpUtilPool = new HttpUtilPool(1, 7);*/
        /*HelpBatch help = new HelpBatch(httpUtilPool,threadPool);
        threadPool.execute(help);*/
        String querySql = "select * from cred_dishonesty_log where dcurrentdate < to_date('2016-08-13','yyyy-mm-dd')";
        DishonestyService service = new DishonestyService();
        List list = service.getExeList(querySql);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            DBLogHandler pageHandler = new DBLogHandler("__________"+cardNum+"____","1",new HttpUtil());
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();
    }
}
