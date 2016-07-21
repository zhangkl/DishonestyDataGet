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
public class PageHandler {
    String code;
    String cardNum;
    String hostName;
    int sameNum;
    int sucessNum;
    HttpUtil httpUtil;

    public PageHandler(HttpUtil httpUtil,String code, String cardNum, String hostName, int sameNum, int sucessNum) {
        this.code = code;
        this.cardNum = cardNum;
        this.hostName = hostName;
        this.sameNum = sameNum;
        this.sucessNum = sucessNum;
        this.httpUtil = httpUtil;
    }

    public void work(String pageNum) {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            if ("".equals(code)) {
                code = dishonestyService.getImageCode(httpUtil);
            }
            List arrayList = dishonestyService.getPageList(httpUtil, code, cardNum, pageNum);
            for (int i = 0; i < arrayList.size(); i++) {
                String saveid = arrayList.get(i).toString();
                String queryIdSql = "select * from CRED_DISHONESTY_PERSON where iid = '" + saveid + "'";
                List resultlist = null;
                try {
                    resultlist = ConnUtil.getInstance().executeQueryForList(queryIdSql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (resultlist != null && resultlist.size() > 0) {
                    sameNum++;
                    continue;
                }else{
                    dishonestyService.saveDishoney(arrayList.get(i).toString(), httpUtil, code, cardNum, sameNum, sucessNum);
                    sucessNum++;
                }
            }
            String logStr = DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":查询条件：" + cardNum + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成" + ",代理地址：" + httpUtil.getProxyURL();
            System.out.println(logStr);
            String logSql = "update cred_dishonesty_log set samenum = '" + sameNum + "',sucessnum='" + sucessNum + "', remark='" + logStr + "',startpage='" + pageNum + "',hostname='" + hostName + "',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
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
