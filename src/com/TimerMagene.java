package com;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: zhangkl
 * Date: 七月,2016
 */
public class TimerMagene {

    //时间间隔
    private static final long PERIOD_DAY = 1 * 1 * 30 * 1000;

    public static void main(String[] args) {
        TimerMagene timerMagene = new TimerMagene();
    }

    public TimerMagene() {
        Calendar calendar = Calendar.getInstance();

        /*** 定制每日2:00执行方法 ***/
        calendar.set(Calendar.HOUR_OF_DAY, 2);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date date=calendar.getTime(); //第一次执行定时任务的时间

        //如果第一次执行定时任务的时间 小于 当前的时间
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。
        if (date.before(new Date())) {
            date = this.addDay(date, 1);
        }

        Timer timer = new Timer();

        NFDFlightDataTimerTask task = new NFDFlightDataTimerTask();
        //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
        timer.schedule(task,new Date(),PERIOD_DAY);
    }

    // 增加或减少天数
    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }
}
class NFDFlightDataTimerTask extends TimerTask {

    @Override
    public void run() {
        try {
            String shStr = "D:\\code\\DishonestyDataGet\\restart.bat";
            Process process;
            process = Runtime.getRuntime().exec(shStr);

            InputStreamReader ir = new InputStreamReader(process
                    .getInputStream(),"GBK");
            LineNumberReader input = new LineNumberReader(ir);
            String line;
//            process.waitFor();
            while (true){
                line = input.readLine();
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

