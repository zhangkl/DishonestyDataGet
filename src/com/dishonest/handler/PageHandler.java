package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
import com.dishonest.util.DateUtil;
import com.dishonest.util.HttpUtil;
import org.htmlparser.util.ParserException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class PageHandler implements Runnable {
    ConnUtil connUtil;
    String code;
    HttpUtil httpUtil;
    String cardNum;
    String pageNum;
    String hostName;
    int sameNum;
    int sucessNum;

    public PageHandler(ConnUtil connUtil, HttpUtil httpUtil,String code, String cardNum, String pageNum, String hostName, int sameNum, int sucessNum) {
        this.connUtil = connUtil;
        this.code = code;
        this.httpUtil = httpUtil;
        this.cardNum = cardNum;
        this.pageNum = pageNum;
        this.hostName = hostName;
        this.sameNum = sameNum;
        this.sucessNum = sucessNum;
    }

    @Override
    public void run() {
        DishonestyService dishonestyService = new DishonestyService();
        if ("".equals(code)) {
            try {
                code = dishonestyService.getImageCode(httpUtil);
                List arrayList = dishonestyService.getPageList(httpUtil,code,cardNum,pageNum);
                for (int i = 0; i < arrayList.size(); i++) {
                    dishonestyService.saveDishoney(arrayList.get(i).toString(),httpUtil,code,cardNum,sameNum,sucessNum);
                }
                String logStr = DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":查询条件：" + cardNum + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成。" + ",代理地址：" + httpUtil.getProxyURL();
                System.out.println(logStr);
                String logSql = "update cred_dishonesty_log set samenum = '" + sameNum + "',sucessnum='" + sucessNum + "', remark='" + logStr + "',startpage='" + pageNum + "',hostname='" + hostName + "',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
                connUtil.executeSaveOrUpdate(logSql);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ParserException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }
}
