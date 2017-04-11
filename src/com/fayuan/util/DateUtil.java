/*******************************************************************************
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 ******************************************************************************/

package com.fayuan.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 2016/3/17
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class DateUtil {

    private static String pattern = "yyyy-MM-dd";

    public static void main(String[] args) {
        // Date testDate = DateUtil.getNextDay(Date.valueOf("2007-1-7"));
        // log.debug(DateFormatUtils.format(testDate.getTime(), "yyyy-MM-dd
        // hh:mm:ss"));

        System.out.println("|" + DateUtil.getNowDateTime()
                + "|");

    }

    /**
     * 获取下一个周末
     *
     * @param date
     * @return
     */
    public static Date getLastStartOfWeek(Date date) {

        Calendar calTemp = Calendar.getInstance(Locale.CHINA);
        calTemp.setTime(date);

        int idayofweek = calTemp.get(Calendar.DAY_OF_WEEK);
        if (idayofweek == 7) {
            return DateUtil.getNextDay(date);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK);
        return new Date(cal.getTime().getTime());
    }

    /**
     * 获取下一天
     *
     * @param date
     * @return
     */
    public static Date getNextDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return new Date(cal.getTime().getTime());
    }

    /**
     * 获取后某n天
     *
     * @param date
     * @return
     */
    public static Date getNextNDay(Date date, int n) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, n);
        return new Date(cal.getTime().getTime());
    }

    // 月初
    public static Date getMonthFirstDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        return new Date(cal.getTime().getTime());
    }

    // 月末
    public static Date getMonthLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -1);
        return new Date(cal.getTime().getTime());

    }

    // 上月同日
    public static Date getLastMonthThisDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(cal.MONTH) - 1);
        return new Date(cal.getTime().getTime());
    }

    // 下月同日
    public static Date getNextMonthThisDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(cal.MONTH) + 1);
        return new Date(cal.getTime().getTime());
    }

    // n个月后
    public static Date getNextNMonth(Date date, int n) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.MONTH, +n);


        return new Date(cal.getTime().getTime());

    }

    // 同年上月同日,1月则返回1月
    public static Date getSameYearLastMonthThisDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(cal.MONTH) != 1) {
            cal.set(Calendar.MONTH, cal.get(cal.MONTH) - 1);
        }
        cal.set(Calendar.DATE, 1);
        return new Date(cal.getTime().getTime());
    }

    // 上月月末
    public static Date getLastMonthLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -1);
        return new Date(cal.getTime().getTime());
    }

    // 年初
    public static Date getYearFisrtDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        return new Date(cal.getTime().getTime());
    }

    // 去年同期

    // 年初
    public static Date getLastYearLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -1);
        return new Date(cal.getTime().getTime());
    }

    public static Date getLastYearThisDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, -1);
        return new Date(cal.getTime().getTime());
    }

    // 昨天
    public static Date yesterday(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.DATE, -1);
        return new Date(temp.getTimeInMillis());
    }

    // 明天
    public static Date tomorrow(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.DATE, 1);
        return new Date(temp.getTimeInMillis());
    }

    // 上个月
    public static Date lastMonth(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.MONTH, -1);
        return new Date(temp.getTimeInMillis());
    }

    // 下个月
    public static Date nextMonth(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.MONTH, 1);
        return new Date(temp.getTimeInMillis());
    }

    // 月末
    public static Date monthEndDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String _endTemp = temp.get(Calendar.YEAR) + "-"
                + (temp.get(Calendar.MONTH) + 1) + "-" + "01";
        temp.setTimeInMillis(Date.valueOf(_endTemp).getTime());
        temp.add(Calendar.MONTH, 1);
        temp.add(Calendar.DATE, -1);
        return new Date(temp.getTimeInMillis());
    }

    //
    public static Date lastMonthEndDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String _endTemp = temp.get(Calendar.YEAR) + "-"
                + (temp.get(Calendar.MONTH) + 1) + "-" + "01";
        temp.setTimeInMillis(Date.valueOf(_endTemp).getTime());
        temp.add(Calendar.DATE, -1);
        return new Date(temp.getTimeInMillis());
    }

    public static Date monthBeginDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String _endTemp = temp.get(Calendar.YEAR) + "-"
                + (temp.get(Calendar.MONTH) + 1) + "-" + "01";
        return Date.valueOf(_endTemp);
    }

    public static Date lastMonthBeginDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String _endTemp = temp.get(Calendar.YEAR) + "-"
                + (temp.get(Calendar.MONTH) + 1) + "-" + "01";
        temp.setTimeInMillis(Date.valueOf(_endTemp).getTime());
        temp.add(Calendar.DATE, -1);
        _endTemp = temp.get(Calendar.YEAR) + "-"
                + (temp.get(Calendar.MONTH) + 1) + "-" + "01";
        return Date.valueOf(_endTemp);
    }

    /**
     * 得到某一年内的第一天
     *
     * @param date 当前时间
     * @return 某一年内的第一天
     */
    public static Date firstOfLastYear(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String s = (temp.get(Calendar.YEAR) - 1) + "-01-01";
        return Date.valueOf(s);
    }

    /**
     * 返回当前系统时间的年份（四位）
     *
     * @return int
     */
    public static int getSystemYear() {
        Calendar actualDate = Calendar.getInstance();
        return actualDate.get(Calendar.YEAR);
    }

    /**
     * 返回指定日期的年份（四位）
     *
     * @return int
     */
    public static int getYear(Date date) {
        Calendar actualDate = Calendar.getInstance();
        actualDate.setTime(date);
        return actualDate.get(Calendar.YEAR);
    }

    /**
     * 返回指定日期的月份（两位）
     *
     * @return int
     */
    public static int getMonth(Date date) {
        Calendar actualMonth = Calendar.getInstance();
        actualMonth.setTime(date);
        return actualMonth.get(Calendar.MONTH) + 1;
    }

    /**
     * 返回指定日期的天（两位）
     *
     * @return int
     */
    public static int getDay(Date date) {
        Calendar actualMonth = Calendar.getInstance();
        actualMonth.setTime(date);
        return actualMonth.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到某一年内的第一天
     *
     * @param date 当前时间
     * @return 某一年内的第一天
     */
    public static Date firstOfYear(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String s = temp.get(Calendar.YEAR) + "-01-01";
        return Date.valueOf(s);
    }

    /**
     * 得到某一年内的最后一天
     *
     * @param date 当前时间
     * @return 某一年内的最后一天
     */
    public static Date lastOfYear(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        String s = temp.get(Calendar.YEAR) + "-12-31";
        return Date.valueOf(s);
    }

    /**
     * 得到下一年的同一天
     *
     * @param date 当前时间
     * @return 下一年的同一天
     */
    public static Date nextYearSameDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.YEAR, 1);
        return new Date(temp.getTimeInMillis());
    }

    /**
     * 得到上一年的同一天
     *
     * @param date 当前时间
     * @return 下一年的同一天
     */
    public static Date lastYearSameDay(Date date) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.YEAR, -1);
        return new Date(temp.getTimeInMillis());
    }

    /**
     * 根据传入的字符串生成日期
     *
     * @param strDate 格式为"yyyy-mm-dd"或"yyyy-mm"或"yyyy"的字符串
     * @return 日期
     */
    public static Date StringToDate(String strDate) {
        try {
            StringBuffer strBuffer = new StringBuffer(strDate);
            while (strBuffer.lastIndexOf("-") < 7) {
                strBuffer.append("-01");
            }

            return Date.valueOf(strBuffer.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * yyyymmdd
     *
     * @param strDate
     * @return
     */
    public static Date StringToDate2(String strDate) {
        try {
            String year = strDate.substring(0, 4);
            String month = strDate.substring(5, 7);
            String date = strDate.substring(8, 10);

            return StringToDate(year + "-" + month + "-" + date);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static Date StringToDate3(String strDate) {
        try {
            String year = strDate.substring(0, 4);
            String month = strDate.substring(6, 8);
            String date = strDate.substring(10, 12);

            return StringToDate(year + "-" + month + "-" + date);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param date1 第一个日期
     * @param date1 第二个日期
     * @return int 0---是同一天，1--date1>date2，-1--date1<date2
     */
    public static int compareTwoDates(Date date1, Date date2) {
        int intRet = -1;
        if (date1 == null && date2 == null) {
            intRet = 0;
        } else if (date1 == null || date2 == null) {
            intRet = -1;
        } else {
            String strDate1 = date1.toString();
            String strDate2 = date2.toString();
            intRet = strDate1.compareTo(strDate2);
            if (intRet > 0)
                intRet = 1;
            else if (intRet < 0)
                intRet = -1;
        }
        return intRet;
    }

    /**
     * 得到和入参日期，相差N天的日期。n<0,日期后退，n>0日期增加。
     */
    public static Date difDate(Date date, Integer dateNum) {
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(date.getTime());
        temp.add(Calendar.DATE, dateNum.intValue());
        return new Date(temp.getTimeInMillis());
    }

    /**
     * 判断dateOne是否after，dateTwo，如果日期为null就认为日期无限大。
     *
     * @param dateOne
     * @param dateTwo
     * @return
     */
    public static boolean after(Date dateOne, Date dateTwo) {
        if (dateTwo == null)
            return false;
        if (dateOne == null)
            return true;
        return dateOne.after(dateTwo);
    }

    public static boolean afterequal(Date dateOne, Date dateTwo) {
        if (dateTwo == null)
            return false;
        if (dateOne == null)
            return true;
        if (dateOne.equals(dateTwo)) return true;
        return dateOne.after(dateTwo);
    }

    /**
     * 判断dateOne是否before，dateTwo，如果日期为null就认为日期无限大。
     *
     * @param dateOne
     * @param dateTwo
     * @return
     */
    public static boolean before(Date dateOne, Date dateTwo) {
        if (dateTwo == null)
            return true;
        if (dateOne == null)
            return false;

        return dateOne.before(dateTwo);
    }

    public static boolean beforeequal(Date dateOne, Date dateTwo) {
        if (dateTwo == null)
            return true;
        if (dateOne == null)
            return false;
        if (dateOne.equals(dateTwo)) return true;
        return dateOne.before(dateTwo);
    }

    public static Date getNowDate() {

        Calendar temp = Calendar.getInstance();
        // String s = temp.get(Calendar.YEAR) + "-01-01";

        return new Date(temp.getTimeInMillis());
    }

    public static Date getTodayDate() {
        return Date.valueOf(getNowDate().toString());
    }

    public static String getNowDateTime() {
        Calendar temp = Calendar.getInstance();
        return temp.getTime().toString();
    }

    public static Timestamp getNowTimeStamp() {

        Calendar temp = Calendar.getInstance();
        return new Timestamp(temp.getTimeInMillis());
    }

    public static String getTime(long ts, int i, int j) {
        return DateFormat.getDateTimeInstance(i, j).format(new Timestamp(ts));
    }

    /**
     * 得到两个日期之间差的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int difference(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();

        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();

        long dif = time1 - time2;
        long day = dif / (24 * 60 * 60 * 1000);

        return (int) day;
    }

    public static String toString(Date date) {

        if (date == null)
            return "";

        Calendar gc = new GregorianCalendar();
        gc.setTime(date);

        String yy = "" + gc.get(Calendar.YEAR);
        String mm = ""
                + (((gc.get(Calendar.MONTH) + 1) > 9) ? ("" + (gc
                .get(Calendar.MONTH) + 1)) : ("0" + (gc
                .get(Calendar.MONTH) + 1)));
        String dd = ""
                + (((gc.get(Calendar.DAY_OF_MONTH)) > 9) ? ("" + (gc
                .get(Calendar.DAY_OF_MONTH))) : ("0" + (gc
                .get(Calendar.DAY_OF_MONTH))));
        return yy + "-" + mm + "-" + dd;

    }

    public static String toString2(Date date) {

        if (date == null)
            return "";

        Calendar gc = new GregorianCalendar();
        gc.setTime(date);

        String yy = "" + gc.get(Calendar.YEAR);
        String mm = ""
                + (((gc.get(Calendar.MONTH) + 1) > 9) ? ("" + (gc
                .get(Calendar.MONTH) + 1)) : ("0" + (gc
                .get(Calendar.MONTH) + 1)));
        String dd = ""
                + (((gc.get(Calendar.DAY_OF_MONTH)) > 9) ? ("" + (gc
                .get(Calendar.DAY_OF_MONTH))) : ("0" + (gc
                .get(Calendar.DAY_OF_MONTH))));
        return yy + mm + dd;

    }

    public static String toFormatedString(Timestamp timestamp, String format) {

        DateFormat df = new SimpleDateFormat(format);
        return df.format(timestamp);
    }

    public static String toFormatedString(Date date, String format) {

        DateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static String toString(Timestamp date) {

        if (date == null)
            return "";

        Calendar gc = new GregorianCalendar();
        gc.setTime(date);

        String yy = "" + gc.get(Calendar.YEAR);
        String mm = ""
                + (((gc.get(Calendar.MONTH) + 1) > 9) ? ("" + (gc
                .get(Calendar.MONTH) + 1)) : ("0" + (gc
                .get(Calendar.MONTH) + 1)));
        String dd = ""
                + (((gc.get(Calendar.DAY_OF_MONTH)) > 9) ? ("" + (gc
                .get(Calendar.DAY_OF_MONTH))) : ("0" + (gc
                .get(Calendar.DAY_OF_MONTH))));
        return yy + "-" + mm + "-" + dd + " " + gc.get(Calendar.HOUR) + ":"
                + gc.get(Calendar.MINUTE) + ":" + gc.get(Calendar.SECOND)
                + ".00000";

    }

    public static String changeDatetoString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        return df.format(date);
    }

    public static String changeDatetoStr(java.util.Date date) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(date);
    }

    public static String changeDatetoStr1(java.util.Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    public static String changeDatetoFormateStr(java.util.Date date,
                                                String formate) {
        DateFormat df = new SimpleDateFormat(formate);
        return df.format(date);
    }

    public static String changeDatetoComplexString(Date date) {
        DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        return df.format(date);
    }

    /**
     * 比较一个日期begin +day日后是否大于日期end
     *
     * @param begin 开始日期
     * @param end   中止日期
     * @param day   间隔日期
     * @return 如果begin +day日<=end返回true，否则返回true
     */
    public static boolean compareDate(Date begin, Date end,
                                      int day) {
        long lbegin = begin.getTime();
        long lend = end.getTime();
        long interval = lend - lbegin;
        long lday = day * 24 * 60 * 60 * 1000;
        if (interval == 0) {
            return true;
        }
        if (interval <= lday) {
            return true;
        }
        return false;
    }

    // 200601--〉2006年第一季度
    public static Date getLastSeasonLatDay(String season) {
        String year = season.substring(0, 4);
        String ss = season.substring(4, 6);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        if (ss.equals("01")) {
            try {
                date = new Date(df.parse(
                        (Integer.parseInt(year) - 1) + "-12-31").getTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (ss.equals("02")) {
            try {
                date = new Date(df.parse(year + "-03-31").getTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (ss.equals("03")) {
            try {
                date = new Date(df.parse(year + "-06-30").getTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (ss.equals("04")) {
            try {
                date = new Date(df.parse(year + "-09-30").getTime());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static boolean getDateStr(String clientDate, String serverDate) {

        try {

            if (!serverDate.equals(clientDate)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }

        // String[] clentDateStr = clientDate.split("-");
        // String yy = clentDateStr[0];
        // int mmInt = Integer.valueOf(clentDateStr[1]).intValue();
        // String mm = "" + (((mmInt) > 9) ? ("" + (mmInt)) : ("0" + (mmInt)));
        // int ddInt = Integer.valueOf(clentDateStr[2]).intValue();
        // String dd = "" + (((ddInt) > 9) ? ("" + (ddInt)) : ("0" + (ddInt)));
        // return yy + "-" + mm + "-" + dd;

    }

    public static String getServerDate(String format) {
        SimpleDateFormat dateformat = new SimpleDateFormat(
                format);
        return dateformat.format(Calendar.getInstance().getTime());
    }

    public static String getDefaultFormatServerDate() {

        return getServerDate(pattern);
    }

    // 把 sql.Date 转换成 util.Date
    public static java.util.Date getSql2UtilDate(Date sqlDate) {

        java.util.Date d = new java.util.Date(sqlDate.getTime());
        return d;

    }

    // 把util.Date 转换成 sql.Date
    public static Date getUtil2SqlDate(java.util.Date utilDate) {

        Date d = new Date(utilDate.getTime());
        return d;

    }

    // 取得当前日期 sql
    public static Date getNowSqlDate() {
        java.util.Date utilDate = new java.util.Date();
        Date d = new Date(utilDate.getTime());
        return d;
    }

    //得到月数差
    public static int getMonthDifference(Date start, Date end) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        return (endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)) * 12 +endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH) + 1;
    }

    //Date 转为 timestamp
    public static Timestamp dateToTimestamp(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return new Timestamp(calendar.getTimeInMillis());
    }


    public static Date stringToDate(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            java.util.Date date = sdf.parse(dateStr);
            return getUtil2SqlDate(date);
        } catch (Exception e) {
            return null;
        }
    }
}
