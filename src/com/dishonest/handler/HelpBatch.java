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
        String sql = "select ac 已存总条数," +
                "       l 不合格页数," +
                "       sc 重复页数," +
                "       pc 剩余可用代理数," +
                "       c 已查询页数," +
                "       s 总页数," +
                "       c / s * 100 || '%' 已完成比例," +
                "       sysdate" +
                "  from (select count(*) c from cred_dishonesty_pagelog)," +
                "       (select sum(endpage) s from cred_dishonesty_log)," +
                "       (select count(*) l" +
                "          from cred_dishonesty_pagelog t,cred_dishonesty_log r" +
                "         where t.samenum + t.sucessnum < 10 and r.cardnum = t.cardnum and r.endpage != t.pagenum)," +
                "       (select count(*) ac from CRED_DISHONESTY_PERSON t)," +
                "       (select count(*) pc from cred_dishonesty_proxy where isusered != 2)," +
                "       (select sum(count(*)) sc from cred_dishonesty_pagelog t group by t.cardnum,t.pagenum having count(*)>1)";
        Map map = ConnUtil.getInstance().executeQueryForMap(sql);
        logger.info("当前运行状态:queue.size()=" + map);

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
            if (calender.get(Calendar.HOUR_OF_DAY) == 22 || calender.get(Calendar.HOUR_OF_DAY) == 23) {
                Main.timeFlag = false;
            } else {
                Main.timeFlag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ;
    }


}
