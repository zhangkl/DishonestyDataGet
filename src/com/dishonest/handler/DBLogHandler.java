package com.dishonest.handler;

import com.dishonest.util.HttpUtil;
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

    public DBLogHandler(String pageNum, String cardNum, HttpUtil httpUtil, String hostName) throws HttpException {
        this.pageNum = pageNum;
        this.cardNum = cardNum;
        this.hostName = hostName;
        this.httpUtil = httpUtil;
    }

    @Override
    public void run() {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            String s = dishonestyService.getPageHtml(httpUtil,cardNum,"0");
            int maxPageNum = dishonestyService.saveLastMaxPageNum(s, cardNum);
            String account = dishonestyService.saveLastCount(s, cardNum);
            logger.info("cardnum:"+cardNum+",获取最大页面:"+maxPageNum+",最大条数:"+account);
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
