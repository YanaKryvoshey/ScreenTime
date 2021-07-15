package com.example.screentime;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class MyClockTickerV4 {

     /*
     V4 - In the beginning there is no open timers.
      + Initialize this module in your Application class:
            MyClockTickerV4.initHelper();
          + you can create new task by calling:
                MyClockTickerV4.getInstance().addCallback(cycleTicker, 4, 1500);
                4 times every 1.5 seconds
                MyClockTickerV4.CycleTicker cycleTicker = new MyClockTickerV4.CycleTicker() {
                    @Override
                    public void secondly(int repeatsRemaining) {
                        Log.d("pttt", "AAAA");
                    }
                };
          + The task will run four times before closing itself
            or you can close it by yourself:
                MyClockTickerV4.getInstance().removeCallback(cycleTicker);
          + Number of repeats must be more than 0 or the timer does't start.
          + You can run the timer continuously if you sent MyClockTickerV4.CONTINUOUSLY_REPEATS instead of a number
          + This module support more than one of the same callback
          + The tasks will run on the background - if you want to be avoid from double timers
            on the background call removeAllCallbacks in activity's onDestroy()
     */

    public interface KillTicker {
        void killMe(Task task);
    }

    public interface CycleTicker {
        void secondly(int repeatsRemaining);
        void done();
    }

    public interface OneTimeTicker {
        void done();
    }

    public class Task {
        private ScheduledExecutorService scheduleTaskExecutor;
        CycleTicker cycleTickerCallback;
        OneTimeTicker oneTimeTicker;
        KillTicker killTickerCallBack;
        int repeats;
        boolean IM_DONE = false;

        public Task(KillTicker killTickerCallBack, CycleTicker cycleTickerCallback, int repeats, int periodInMilliseconds) {
            this.killTickerCallBack = killTickerCallBack;
            this.cycleTickerCallback = cycleTickerCallback;
            this.repeats = repeats;

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    tickerFunction();
                }
            }, 0, periodInMilliseconds, TimeUnit.MILLISECONDS);
        }

        public Task(KillTicker killTickerCallBack, OneTimeTicker oneTimeTicker, int delayInMilliseconds) {
            this.killTickerCallBack = killTickerCallBack;
            this.oneTimeTicker = oneTimeTicker;
            this.repeats = 1;

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    singleTickerFunction();
                }
            }, delayInMilliseconds, 1, TimeUnit.MILLISECONDS);
        }

        private void singleTickerFunction() {
            oneTimeTicker.done();
            killMe();
        }

        private void tickerFunction() {
            // send done function second after last call
            if (IM_DONE) {
                cycleTickerCallback.done();
                killMe();
            }
            else {
                cycleTickerCallback.secondly(repeats);

                if (!(repeats == CONTINUOUSLY_REPEATS)) {
                    repeats--;
                    if (repeats <= 0) {
                        IM_DONE = true;
                    }
                }
            }
        }

        public void killMe() {
            try {
                scheduleTaskExecutor.shutdown();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            scheduleTaskExecutor = null;
            cycleTickerCallback = null;
            killTickerCallBack.killMe(this);
        }
    }

    public static final int CONTINUOUSLY_REPEATS = -999;

    private ArrayList<Task> tasks;
    private Object locker = new Object();
    private static MyClockTickerV4 instance;

    KillTicker killTickerCallBack = new KillTicker() {
        @Override
        public void killMe(Task task) {
            synchronized (locker) {
                tasks.remove(task);
            }
        }
    };

    public static MyClockTickerV4 getInstance() {
        return instance;
    }

    public static MyClockTickerV4 initHelper() {
        if (instance == null)
            instance = new MyClockTickerV4();
        return instance;
    }

    private MyClockTickerV4() {
        tasks = new ArrayList<>();
    }

    public int getNumOfActiveTickers() {
        synchronized (locker) {
            return tasks.size();
        }
    }

    public void addCallback(CycleTicker cycleTicker, int repeats, int periodInMilliseconds) {
        if (repeats == CONTINUOUSLY_REPEATS || repeats > 0) {
            synchronized (locker) {
                tasks.add(new Task(killTickerCallBack, cycleTicker, repeats, periodInMilliseconds));
            }
        }
    }

    public void addSingleCallback(OneTimeTicker oneTimeTicker, int delayInMilliseconds) {
        synchronized (locker) {
            tasks.add(new Task(killTickerCallBack, oneTimeTicker, delayInMilliseconds));
        }
    }

    public void removeCallback(CycleTicker cycleTicker) {
        synchronized (locker) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).cycleTickerCallback == cycleTicker) {
                    tasks.get(i).killMe();
                    break;
                }
            }
        }
    }


    public void removeCallback(OneTimeTicker oneTimeTicker) {
        synchronized (locker) {
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).oneTimeTicker == oneTimeTicker) {
                    tasks.get(i).killMe();
                    break;
                }
            }
        }
    }

    public void removeAllCallbacks() {
        synchronized (locker) {
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).killMe();
            }
        }
    }
}
