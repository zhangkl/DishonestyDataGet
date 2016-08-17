package com.dishonest.handler;

import com.dishonest.dao.ConnUtil;
import com.dishonest.util.GetDateException;
import com.dishonest.util.HttpUtil;
import com.dishonest.util.HttpUtilPool;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
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

    Logger logger = Logger.getLogger(PageHandler.class);

    int dateType;//数据类型  1为个人 2为企业
    String cardNum;
    String pageNum;
    String hostName;
    String areacode;
    int sameNum;
    int sucessNum;
    HttpUtil httpUtil;
    HttpUtilPool httpUtilPool;
    DishonestyService dishonestyService;

    public PageHandler(int dateType, String pageNum, String cardNum, String areacode, HttpUtilPool httpUtilPool, String hostName, int sameNum, int sucessNum, DishonestyService dishonestyService) throws HttpException {
        this.dateType = dateType;
        this.pageNum = pageNum;
        this.cardNum = cardNum;
        this.hostName = hostName;
        this.sameNum = sameNum;
        this.areacode = areacode;
        this.sucessNum = sucessNum;
        this.httpUtilPool = httpUtilPool;
        this.dishonestyService = dishonestyService;
    }

    @Override
    public void run() {
        work();
    }

    public void work() {
        try {
            this.httpUtil = httpUtilPool.getHttpUtil();
            List arrayList = dishonestyService.getPageList(httpUtil, cardNum, pageNum, areacode);
            for (int j = 0; j < arrayList.size(); j++) {
                String saveid = arrayList.get(j).toString();
                String queryIdSql = "select * from CRED_DISHONESTY_PERSON where iid = '" + saveid + "'";
                if (dateType == 2) {
                    queryIdSql = "select * from CRED_DISHONESTY_ENT where iid = '" + saveid + "'";
                }
                List resultlist = ConnUtil.getInstance().executeQueryForList(queryIdSql);
                if (resultlist != null && resultlist.size() > 0) {
                    sameNum++;
                    continue;
                } else {
                    int num = dishonestyService.saveDishoney(arrayList.get(j).toString(), httpUtil, cardNum, areacode);
                    if (num == 0) {
                        sameNum++;
                    } else {
                        sucessNum++;
                    }
                }
            }
            httpUtilPool.returnHttpUtil(httpUtil);
            String logStr = "查询条件：" + cardNum + ",areacode:" + areacode + ",当前页数：" + pageNum + "，总重复个数" + sameNum + ",总成功个数：" + sucessNum + ",查询入库完成,idList:" + arrayList + ",代理地址：" + httpUtil.getProxyURL();
            logger.info(logStr);
            String sql = "select * from cred_dishonesty_pagelog where cardnum ='" + cardNum + "' and pagenum = '" + pageNum + "' and areacode = '" + areacode + "'";
            List pagelist = dishonestyService.getExeList(sql);
            String logSql;
            if (pagelist != null && pagelist.size() > 0) {
                logSql = "update cred_dishonesty_pagelog set samenum = '" + sameNum + "',sucessnum = '" + sucessNum + "' where pagenum = '" + pageNum + "' and cardnum = '" + cardNum + "'";
            } else {
                logSql = "insert into cred_dishonesty_pagelog (CARDNUM, PAGENUM, HOSTNAME, RESULT, DCURRENTDATE, REMARK, SAMENUM, SUCESSNUM,AREACODE)" +
                        " values ( '" + cardNum + "', '" + pageNum + "', '" + hostName + "', '" + arrayList.toString() + "', sysdate, '" + logStr + "', '" + sameNum + "', '" + sucessNum + "','" + areacode + "')";
            }
            ConnUtil.getInstance().executeSaveOrUpdate(logSql);
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
        } finally {
            httpUtilPool.returnHttpUtil(httpUtil);
        }
    }
}
