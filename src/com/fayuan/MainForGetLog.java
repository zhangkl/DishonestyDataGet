package com.fayuan;

import com.fayuan.handler.DBLogHandler;
import com.fayuan.handler.DishonestyService;
import com.fayuan.util.HttpUtil;
import org.apache.http.HttpException;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainForGetLog {
    static int DataType = 2; //1为个人 2为企业

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException {
        MainForGetLog main_forGetLog = new MainForGetLog();
        if (DataType == 1) {
            main_forGetLog.workerForPerson();
        } else {
            main_forGetLog.workForENT();
        }
    }

    /**
     * 企业根据areaCode为变量，将条数信息存储到log表中
     * @throws SQLException
     * @throws InterruptedException
     * @throws HttpException
     */
    public void workForENT() throws SQLException, InterruptedException, HttpException {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        String[] areaArray = {"660", "661", "662", "663", "664", "665", "666", "667", "668", "669", "670", "671", "672", "673", "674", "675", "676", "677", "678", "679", "680", "681", "682", "683", "684", "685", "686", "687", "688", "689", "690", "691", "692", "693"};
        for (int i = 0; i < areaArray.length; i++) {
            DBLogHandler dlh = new DBLogHandler("____", "_________", areaArray[i], "0", new HttpUtil(), DataType);
            threadPool.execute(dlh);
        }
    }

    /**
     * 个人根据身份证号码日期四位为变量，将该条信息存储在log表中
     * @throws SQLException
     * @throws InterruptedException
     * @throws HttpException
     */
    public void workerForPerson() throws SQLException, InterruptedException, HttpException {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        String querySql = "select * from cred_dishonesty_log where dcurrentdate < to_date('2016-08-13','yyyy-mm-dd')";
        DishonestyService service = new DishonestyService();
        List list = service.getExeList(querySql);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Map map = (Map) it.next();
            String cardNum = (String) map.get("CARDNUM");
            DBLogHandler pageHandler = new DBLogHandler("__", cardNum, "0", "1", new HttpUtil(), DataType);
            threadPool.execute(pageHandler);
        }
        threadPool.shutdown();
    }
}
