package com.fayuan.handler;

import com.fayuan.util.GetDateException;
import com.fayuan.util.HttpUtil;
import com.fayuan.util.NetWorkException;
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

    String name;
    String cardNum;
    String areacode;
    String pagenum;
    HttpUtil httpUtil;
    int dataType;


    public DBLogHandler(String name, String cardNum, String areacode, String pagenum, HttpUtil httpUtil, int dataType) {
        this.name = name;
        this.cardNum = cardNum;
        this.areacode = areacode;
        this.pagenum = pagenum;
        this.httpUtil = httpUtil;
        this.dataType = dataType;
    }

    @Override
    public void run() {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            String s = dishonestyService.getPageHtml(name, cardNum, areacode, pagenum,httpUtil);
            int maxPageNum = dishonestyService.saveLastMaxPageNum(s, cardNum, areacode);
            String account = dishonestyService.saveLastCount(s, cardNum, areacode);
            logger.info("cardnum:" + cardNum + "，地址代码：" + areacode + ",获取最大页面:" + maxPageNum + ",最大条数:" + account);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (NetWorkException e) {
            e.printStackTrace();
        } catch (GetDateException e) {
            e.printStackTrace();
        }
    }
}
