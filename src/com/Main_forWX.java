package com;

import com.dishonest.util.HttpUtil;

import java.io.*;

/**
 * Created by zhang on 2016/10/9.
 */
public class Main_forWX {

    public static void main(String[] args) throws InterruptedException, IOException {
        String url = "http://b.jujiaonet2.com/xfqxk_2016/index.php?cid=50&tid=5513&from=singlemessage&isappinstalled=0";
        int sucCount = 45;
        String proxyFilePath = "D:\\Code\\DishonestyDataGet\\proxurl.txt";
        File file = new File(proxyFilePath);
        FileInputStream in = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String proxUrl = line.split("@")[0].trim();
            HttpUtil httpUtil = new HttpUtil(true, proxUrl);
            System.out.println(proxUrl);
            String netResult = httpUtil.doGetString(url, null);
            /*System.out.println(netResult);
            String ajaxResult = httpUtil.doPostString(url, "t", "498", "ajax", "a", "id", "20211", "uid", "478");
            System.out.println(ajaxResult);
            if ("succ".equals(ajaxResult)) {
                sucCount++;
                System.out.println("成功" + sucCount);
            } else {
                continue;
            }*/
        }
        System.out.println("执行结束，目前成功个数：" + sucCount);
    }
}
