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
    String cardNum;
    String pageNum;
    String hostName;
    static int sameNum;
    static int sucessNum;
    HttpUtil httpUtil;
    int startPage;
    int endPage;

    public PageHandler(String cardNum, String pageNum, String hostName, HttpUtil httpUtil) {
        this.cardNum = cardNum;
        this.pageNum = pageNum;
        this.hostName = hostName;
        this.httpUtil = httpUtil;
    }

    public PageHandler(int startPage, int endPage, HttpUtil httpUtil, String cardNum, String hostName, int sameNum, int sucessNum) {
        this.startPage = startPage;
        this.endPage = endPage;
        this.cardNum = cardNum;
        this.hostName = hostName;
        this.sameNum = sameNum;
        this.sucessNum = sucessNum;
        this.httpUtil = httpUtil;
    }

    @Override
    public void run() {
        for (int i = startPage; i <= endPage; i++) {
            work(i + "");
            //getAllCount(i + "");
        }
        String logSql = "update cred_dishonesty_log set result = '1',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
        try {
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getAllCount(String pageNum) {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            String s = dishonestyService.getPageHtml(httpUtil, cardNum, pageNum);
            String account = dishonestyService.getPageAccount(s);
            System.out.println(cardNum + ":" + account);
            dishonestyService.saveAllCount(cardNum, account);
        } catch (IOException e) {
            try {
                dishonestyService.changeProxy(httpUtil);
            } catch (SQLException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (InterruptedException e) {
            try {
                dishonestyService.changeProxy(httpUtil);
            } catch (SQLException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void work(String pageNum) {
        DishonestyService dishonestyService = new DishonestyService();
        try {
            List arrayList = dishonestyService.getPageList(httpUtil, cardNum, pageNum);
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
                } else {
                    dishonestyService.saveDishoney(arrayList.get(i).toString(), httpUtil, cardNum);
                    sucessNum++;
                }
            }
            String logStr = DateUtil.getNowDateTime() + ":" + Thread.currentThread().getName() + ":查询条件：" + cardNum + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成" + ",代理地址：" + httpUtil.getProxyURL();
            System.out.println(logStr);
            String logSql = "update cred_dishonesty_log set samenum = '" + sameNum + "',sucessnum='" + sucessNum + "', remark='" + logStr + "',startpage='" + pageNum + "',hostname='" + hostName + "',dcurrentdate = sysdate where cardnum = '" + cardNum + "'";
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
        } catch (IOException e) {
            try {
                dishonestyService.changeProxy(httpUtil);
            } catch (SQLException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            work(pageNum);
        } catch (InterruptedException e) {
            try {
                dishonestyService.changeProxy(httpUtil);
            } catch (SQLException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            work(pageNum);
        } catch (ParserException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
