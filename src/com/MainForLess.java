package com;

import com.dishonest.handler.HelpBatch;
import com.dishonest.util.GetDateException;
import com.dishonest.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainForLess {

    Logger logger = Logger.getLogger(MainForLess.class);

    public static void main(String[] args) throws SQLException, InterruptedException, HttpException, GetDateException {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        HelpBatch helpBatch = new HelpBatch();
        ses.scheduleAtFixedRate(helpBatch,0,10,TimeUnit.MINUTES);

        /*int pageNum = 0;
        for (int i = 0; i < 5; i++) {
            pageNum++;
            HttpUtilPool httpUtilPool = new HttpUtilPool(3);
            Main main = new Main(3, "zhangkl", httpUtilPool,pageNum);
            Thread thread = new Thread(main);
            thread.start();
        }*/
        HttpUtilPool httpUtilPool = new HttpUtilPool(10);
        Main main = new Main(10, System.getenv("COMPUTERNAME"), httpUtilPool,0);
        Thread thread = new Thread(main);
        thread.start();
    }

}
