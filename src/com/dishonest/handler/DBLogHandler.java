package com.dishonest.handler;

import com.dishonest.util.HttpUtil;
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
    HttpUtil httpUtil;
    String areacode;


    public DBLogHandler(String cardNum, String areacode, HttpUtil httpUtil) {
        this.cardNum = cardNum;
        this.areacode = areacode;
        this.httpUtil = httpUtil;
    }

    @Override
    public void run() {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            String s = dishonestyService.getPageHtml(httpUtil, cardNum, "1", areacode);
            if (cardNum.contains("-")) {
                int maxPageNum = dishonestyService.saveLastMaxPageNum(s, cardNum, areacode);
                String account = dishonestyService.saveLastCount(s, cardNum, areacode);
                logger.info("cardnum:" + cardNum + "，地址代码：" + areacode + ",获取最大页面:" + maxPageNum + ",最大条数:" + account);
            } else {
                int maxPageNum = dishonestyService.saveLastMaxPageNum(s, cardNum.replaceAll("_", ""), areacode);
                String account = dishonestyService.saveLastCount(s, cardNum.replaceAll("_", ""), areacode);
                logger.info("cardnum:" + cardNum + "，地址代码：" + areacode + ",获取最大页面:" + maxPageNum + ",最大条数:" + account);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
