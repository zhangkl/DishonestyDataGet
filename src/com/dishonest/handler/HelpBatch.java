package com.dishonest.handler;

import com.Main;
import com.dishonest.dao.ConnUtil;
import com.dishonest.util.HttpUtilPool;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 八月,2016
 */
public class HelpBatch implements Runnable {
    private static Logger logger = Logger.getLogger(HelpBatch.class);

    HttpUtilPool httpUtilPool;
    BlockingQueue queue;

    public HelpBatch(HttpUtilPool httpUtilPool, BlockingQueue<Runnable> queue) {
        this.httpUtilPool = httpUtilPool;
        this.queue = queue;
    }

    public HelpBatch() {

    }

    public void getHttpPoolStatus() throws InterruptedException, SQLException {
        Calendar calender = Calendar.getInstance();
        if (calender.get(Calendar.HOUR_OF_DAY) != 11) {
            Main.timeFlag = true;
        } else {
            Main.timeFlag = false;
        }
    }

    public static void main(String[] args) throws InterruptedException, SQLException {

    }

    @Override
    public void run() {
        try {
            Calendar calender = Calendar.getInstance();
            if ((calender.get(Calendar.HOUR_OF_DAY) == 22 && (calender.get(Calendar.MINUTE) == 30)) || (calender.get(Calendar.HOUR_OF_DAY) == 23)) {
                Main.timeFlag = false;
            } else {
                Main.timeFlag = true;
            }
            logger.info("运行helpBatch......" + Main.timeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


}
