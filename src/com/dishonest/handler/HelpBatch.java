package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
import com.dishonest.util.HttpUtilPool;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 八月,2016
 */
public class HelpBatch implements Runnable {
    private static Logger logger = Logger.getLogger(HelpBatch.class);

    HttpUtilPool httpUtilPool;
    ExecutorService executorService;

    public HelpBatch(HttpUtilPool httpUtilPool, ExecutorService executorService) {
        this.httpUtilPool = httpUtilPool;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            getHttpPoolStatus();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getHttpPoolStatus() throws InterruptedException, SQLException {
        while(true){
            httpUtilPool.getStatus();
            String sql = "select ac 已存总条数,l 不合格页数,pc 剩余可用代理数,c 已查询页数,s 总页数,c / s * 100 || '%' 已完成比例," +
                    "sysdate from (select count(*) c from cred_dishonesty_pagelog)," +
                    "(select sum(endpage) s from cred_dishonesty_log), (select count(*) l " +
                    " from cred_dishonesty_pagelog t  where t.samenum + t.sucessnum < 10)," +
                    " (select count(*) ac from CRED_DISHONESTY_PERSON t)," +
                    " (select count(*) pc from cred_dishonesty_proxy where isusered != 2)";
            Map map = ConnUtil.getInstance().executeQueryForMap(sql);
            logger.info("当前运行状态"+map);
            Thread.currentThread().sleep(1000*60*10);
        }
    }
}
