/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import java.util.StringTokenizer;

/**
 *
 * @author walterstevenson
 */

public class timeEvents {

    static final int EVENT_NONE = 10;
    static final int EVENT_SET_REMINDER = EVENT_NONE + 1;
    static final int EVENT_START_TRADING = EVENT_SET_REMINDER + 1;
    static final int EVENT_WAIT_ONE_MINUTE = EVENT_START_TRADING +1;
    final int MARKET_OPEN_INSECS = ((6*60*60)+(30*60)); /* 6:30AM */
    private timeEvents notifyee = null;
    private int eventType = EVENT_NONE;
    private String notifyTime = "";
    private int eventHour;
    int eventMin;
    int eventSec;
    boolean eventAm;
    int eventInSecs;
    private Calendar eventCalendar;
    private boolean eventOccurred = false;
    private boolean eventEnabled = false;
    private boolean marketIsClosed = false;
    private int oneMinute;
    int saveEventType = EVENT_NONE;
    static Vector actEvents = new Vector(5,1);
    static int eIdx = 0;
    static boolean okToTrade = false;
    static int delayOpen = 0;
    static tickTock tick = new tickTock();
    private static Semaphore timeEventsSem = new Semaphore(1);
    
    timeEvents(int onEvent) {

        eventCalendar = new GregorianCalendar();
        eventOccurred = false;
        /* this is the event to watch and notify on */
        eventType = onEvent;
        timeEventsSem.acquire();
        actEvents.add(eIdx, (timeEvents)this);
        timeEventsSem.release();
        eIdx++;
    }

    void setEventType(int tin) {
        eventType = tin;

    }
    

    void setTime(String timein) {
        StringTokenizer st = new StringTokenizer(timein, ":");

        notifyTime = timein;
        eventHour = Integer.parseInt(st.nextToken());
        if(eventHour == 12) {
            /* am/pm takes care of 12 so 12 is not used. */
            eventHour = 0;
        }
        eventMin = Integer.parseInt(st.nextToken());
        eventSec = Integer.parseInt(st.nextToken());
        // AM == 0, PM != 0...
        eventAm = st.nextToken().toUpperCase().equals("AM");
        eventInSecs = giveMeSeconds(eventHour, eventMin, eventSec, (eventAm == true)?0:1);
        reArm();
        
    }
    /* this is the method that is called regularly to keep track of time, should be well within a minute (~10/20 seconds) */
    static class tickTock extends Thread {

        tickTock() {
            this.start();
        }

        public void run() {
        int runTimes = 0;
            while (true) {

                try {

                    timeEvents tickit;
                    int ix = 0;
                    if (actEvents.size() > 0) {
                        timeEventsSem.acquire();
                        for (ix = 0, tickit = (timeEvents) actEvents.elementAt(ix); ix < actEvents.size(); tickit = (timeEvents) actEvents.elementAt(ix), ix++) {
                            tickit.tickMe();
                        }   
                        timeEventsSem.release();
                    }else{
                        
                        System.out.println("timeEvents: no actEvent elements!");
                    }
                
                /* run every 10 seconds */
                //System.out.println("tickTock: tick ("+runTimes+").");
                runTimes++;
                tradeHandler.sleep(10000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } /* while */
        } /* run */
    } /* tickTock */

    private void tickMe() {
        boolean marketOpen;
        int timeNowInSecs = 0;
        int tmp;
        int h; int m; int s;int ampm;
        //eventCalendar = new GregorianCalendar();
        eventCalendar = GregorianCalendar.getInstance();
        
        h = eventCalendar.get(Calendar.HOUR);
        m = eventCalendar.get(Calendar.MINUTE);
        s = eventCalendar.get(Calendar.SECOND);
        ampm = eventCalendar.get(Calendar.AM_PM);
        timeNowInSecs = giveMeSeconds(eventCalendar.get(Calendar.HOUR), 
                               eventCalendar.get(Calendar.MINUTE), 
                               eventCalendar.get(Calendar.SECOND), 
                               eventCalendar.get(Calendar.AM_PM));
        switch (eventType) {
            case EVENT_SET_REMINDER:
                 {
                    if ((eventEnabled == true) && (eventOccurred == false)) {
                        
                        /* if current time exceeds event time then throw signal */
                        if ((timeNowInSecs >= eventInSecs) && (timeNowInSecs < eventInSecs + 60)) {
                            eventOccurred = true;
                            eventEnabled = false;   
                            /* wait one minute */
                            oneMinute = eventCalendar.get(Calendar.MINUTE);
                            saveEventType = eventType;
                            eventType = EVENT_WAIT_ONE_MINUTE;
                        }
                    }


                }
                break;

            case EVENT_START_TRADING:
                 {
                    if ((eventEnabled == true) && (eventOccurred == false)) {
                        /*
                        if (((MARKET_OPEN_HOUR + eventHour) == eventCalendar.get(eventCalendar.HOUR)) &&
                                ((MARKET_OPEN_MIN + eventMin) == eventCalendar.get(eventCalendar.MINUTE)) &&
                                (eventAm == (eventCalendar.get(eventCalendar.AM_PM) == 0))) {
                            eventOccurred = true;
                        }
                         */
                        /* if current time exceeds market open time plus warm up time then signal */
                        int okToTradeNow = MARKET_OPEN_INSECS + eventInSecs;
                        if ((timeNowInSecs >= okToTradeNow) && (timeNowInSecs < okToTradeNow + 60)) {
                            eventOccurred = true;
                            eventEnabled = false;   
                            /* wait one minute */
                            oneMinute = eventCalendar.get(Calendar.MINUTE);
                            saveEventType = eventType;
                            eventType = EVENT_WAIT_ONE_MINUTE;
                            
                        }
                    }

                }
            case EVENT_WAIT_ONE_MINUTE:
                
                
                if (((tmp = eventCalendar.get(Calendar.MINUTE)) != oneMinute) && (tmp != oneMinute+1)){
                   /* one minute passed 
                      restore previouse state/event and rearm
                    */
                   eventType = saveEventType;
                   reArm();
                }
                break;

            default:
                 {
                }
                break;

        } /* switch */

        //eventCalendar.set(2009,3,22,12,59,59);

        marketOpen = (  /*is it > than 6:30am AND < 12:59 pm */
                        (timeNowInSecs >= 23400) && (timeNowInSecs <= 46799 )
                     );
        okToTrade = (
                        /* 6:45AM - 12:59PM */
                        (timeNowInSecs >= (23400+delayOpen)) && (timeNowInSecs <= 46740)
                    );
        marketIsClosed = !marketOpen;

        eventCalendar = null;
    }
    int giveMeSeconds(int h, int m, int s, int ampm) {
        return((ampm==0)?((h*60*60)+(m*60)+s):((h*60*60)+(m*60)+s+(12*60*60)));
    }
    boolean didEventOccur() {
        return eventOccurred;
    }

    void ackEvent() {
        eventOccurred = false;
        
    }
    void reArm() {
        eventOccurred = false;
        eventEnabled = true;    
    }

    boolean isMarketClosed() {
        return marketIsClosed;
    }
    boolean isMarketOpen() {
        return !marketIsClosed;
    }

    void setMarketOpen() {
        marketIsClosed = false;
    }

    void setMarketClosed() {
        marketIsClosed = true;
    }
    public boolean isOkToTrade() {
        return(okToTrade);
    }
    public static void setDelayOpen(int secs) {
        delayOpen = secs;
    }
}
