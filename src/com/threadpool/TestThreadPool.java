package com.threadpool;//
//
///**
// * Created by zxl on 2016/7/5.
// */
//
//import TianJ.WorkTaskDownBaseTJImpl;
//import TianJ.WorkTaskDownTJImpl;
//import pool.ConnectionPool;
//import pool.ConnectionPoolManager;
//import threadpool.PoolManager;
//
//import java.sql.SQLException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
///**
// * 线程池测试类,测试功能如下：
// * 1、测试线程池创建功能
// * 2、测试处理并发请求功能
// * 3、测试关闭功能
// */
//
//public class TestThreadPool {
//    public static void main(String[] args) throws SQLException {
//        ConnectionPool conPool1 = (ConnectionPool) ConnectionPoolManager.getInstance().getPool("testPool");
//        ConnectionPool conPool2 = (ConnectionPool) ConnectionPoolManager.getInstance().getPool("testPool");
//        //创建线程池,开启处理请求服务
//        PoolManager pool = PoolManager.getInstance();
//        DownTJPage page = new DownTJPage();
//        DownTJBasePage page1 = new DownTJBasePage();
//        DownTJBasePage page2 = new DownTJBasePage();
//        pool.init();
//        //接收客户端请求
//        int pageNos = 172;
//        List<String> list = new ArrayList<String>();
//        for (int j = 1; j < 9918; j++) {
//            list = page.getContentFormUrl("http://tjcredit.gov.cn/platform/saic/exclist.ftl", pageNos);
//            String jsessionId = list.get(0);
//            String content = list.get(1);
//            WorkTaskDownTJImpl task1 = new WorkTaskDownTJImpl(jsessionId, content);
//            Map mp = task1.runMapTask();
//           // TaskManager.addTask(task1);
//
//
//            for (int i = 0; i < mp.size(); i++) {
//                List<String> list2 = (List<String>) mp.get(i);
//                String urlContent = page1.getContentFormUrl(list2.get(0), list2.get(1));
//                WorkTaskDownBaseTJImpl task = new WorkTaskDownBaseTJImpl(urlContent, conPool1);
//
//
//                Date d1 = new Date();//获取时间
//                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd kkmmss ");//转换格式
//                System.out.println("第一步  时间:" + sdf1.format(d1));
//
//
//                //遇到特殊情况
//                if (urlContent.indexOf("<td class=\"\" width=\"30%\">") == -1) break;
//
//                String iid = task.runBaseTask();
//               // TaskManager.addTask(task);
//
//                Date d2 = new Date();//获取时间
//                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd kkmmss ");//转换格式
//                System.out.println("第二步  时间:" + sdf2.format(d2));
//
//                String exContent = page2.getContentFormUrl(list2.get(3), list2.get(1));
//                String sorgnameStr = list2.get(2);
//                WorkTaskDownExTJImpl task2 = new WorkTaskDownExTJImpl(exContent, sorgnameStr, iid, conPool2);
//                task2.runExTask();
//                //TaskManager.addTask(task2);
//
//                Date d3 = new Date();//获取时间
//                SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd kkmmss ");//转换格式
//                System.out.println("第三步  时间:" + sdf3.format(d3));
//
//            }
//
//            System.out.println(pageNos);
//            pageNos++;
//        }
//
//    }
//
//}
