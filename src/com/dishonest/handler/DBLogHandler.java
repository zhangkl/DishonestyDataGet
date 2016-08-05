package com.dishonest.handler;

import com.dishonest.util.GetDateException;
import com.dishonest.util.HttpUtil;
import com.dishonest.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class DBLogHandler implements Runnable {

    Logger logger = Logger.getLogger(DBLogHandler.class);


    String cardNum;
    String pageNum;
    String hostName;
    HttpUtil httpUtil;
    HttpUtilPool httpUtilPool;

    public DBLogHandler(String pageNum, String cardNum, HttpUtilPool httpUtilPool, String hostName) throws HttpException {
        this.pageNum = pageNum;
        this.cardNum = cardNum;
        this.hostName = hostName;
        this.httpUtilPool = httpUtilPool;
    }

    @Override
    public void run() {
        DishonestyService dishonestyService = new DishonestyService();
        int maxPageNum = 0;
        String account = null;
        try {
            this.httpUtil = httpUtilPool.getHttpUtil();
            maxPageNum = dishonestyService.saveLastMaxPageNum(httpUtil, cardNum);
            account = dishonestyService.saveLastCount(httpUtil, cardNum,pageNum);
        } catch (GetDateException e) {
            logger.error("线程错误：", e);
            throw new RuntimeException(new InterruptedException());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }finally {
            httpUtilPool.returnHttpUtil(httpUtil);
        }
        logger.info("cardnum:"+cardNum+",获取最大页面:"+maxPageNum+",最大条数:"+account);
    }
}
