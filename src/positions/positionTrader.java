/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import ibTradeApi.*;
import ibTradeApi.ibApi.quoteInfo;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.*;
import java.util.Date;
import positions.positionData.positionStates;
import positions.alarmsEvents.*;
import java.awt.Dimension;


/**
 *
 * @author walterstevenson
 */
public class positionTrader extends Thread {
    // trade frequency in minutes, i.e 10 == run polling of stock quotes every 10 minutes.
    private int runFrequency,  pollFrequency = 20; /* in seconds */

    private boolean posLock = false;
    private int runTimes,  runMinutes;
    private int runTraderRunTimes;
    private positions positionsToRun;
    private int positionsCount = 0;
    private int stockMove = 5; /* 5% */

    private int stockMoveOneTime = 3;
    private boolean filler = false;
    private boolean filler1 = false;
    private boolean allowToRun = true;
    private boolean pauseRun = false;
    private boolean traderTerminated = false;
    private quoteInfo sQuote = new quoteInfo();
    private quoteInfo oQuote = new quoteInfo();
    public dispStatusScreen actRunStatusScreen = null;
    private boolean updateFile = false;
    private positionStatistics actStats = null;
    private boolean oneTradePerDay = false;
    /*
    private timeEvents tradeOnceLowTrigger = new timeEvents(timeEvents.EVENT_SET_REMINDER);
    private timeEvents beginTradingHighTrigger = new timeEvents(timeEvents.EVENT_SET_REMINDER);
    private timeEvents stopTradingMarketClosed = new timeEvents(timeEvents.EVENT_SET_REMINDER);
     */
    private timeEvents tradeOnceLowTrigger = null; 
    private timeEvents beginTradingHighTrigger = null; 
    private timeEvents stopTradingMarketClosed = null; 
    private timeEvents aveInTradingTrigger = null;
    private timeEvents eventTicker = null;
    private boolean beginTrading = false;
    private boolean autoTradingOn = true;
    private int savedStockMoved = 5;
    private positionChart actChart = null;
    private boolean stopLossLockGain = false;
    private float stopLoss = 0;
    private float lockGain = 0;
    ibApi actIbApi = ibApi.getActApi();
    private boolean aveInCheckEvent = false;
    public positionTrader(positions toRun, positionConfig posConfigIn) {
        tradeOnceLowTrigger = new timeEvents(timeEvents.EVENT_SET_REMINDER);
        beginTradingHighTrigger = new timeEvents(timeEvents.EVENT_SET_REMINDER);
        stopTradingMarketClosed = new timeEvents(timeEvents.EVENT_SET_REMINDER);
        aveInTradingTrigger = new timeEvents(timeEvents.EVENT_SET_REMINDER);
        
        if (posConfigIn != null) {
            stockMove = posConfigIn.getConfigStockMove();
            savedStockMoved = stockMove;
            filler = posConfigIn.getConfigFiller();
            filler1 = posConfigIn.getConfigFiller1();
            pollFrequency = posConfigIn.getConfigPollFrequency();
            autoTradingOn = posConfigIn.getAutoTrade();
            if (posConfigIn.isOneDayTradeSet() == true) {
                tradeOnceLowTrigger.setTime(posConfigIn.getTradeTime());
                oneTradePerDay = true;
                stockMoveOneTime = posConfigIn.getConfigStockMoveOneTime();
            }
            if (posConfigIn.getStopLossLockGain() == true ) {
                stopLossLockGain = true;
                stopLoss = posConfigIn.getStopLoss();
                lockGain = posConfigIn.getLockGain();
            }

        }
        eventTicker = new timeEvents(timeEvents.EVENT_NONE);
        positionsToRun = toRun;
        positionsCount = toRun.posDataVecSize();
        runFrequency = 1000; /* 1 second */

        runTimes = 0;
        runMinutes = 0;
        runTraderRunTimes = 0;

        this.start();
        traderTerminated = false;

        /* this event will notify us when it is ok to begin trading just
         * after the bell rings. we do this to have better bid/ask prices.
         * 1/25/2012 changed close to 1:00 instead of 1:01. Ib returns -1 on quotes
         * 
         */
        /* 
         * wfs 11/30/2012
         * changed following times: 
         * - beginTradingHighTrigger to 6:40:00:AM from 6:45:00:AM. Did
         * this because market seems pretty settled after 10 minutes.
         * - stopTradingMarketClosed to 12:59:00:PM from 1:00:00:PM. Did this
         * as an attempt to fix the bad "stats" info and charts info that happens at close.
         * my theory is that we are already in tradeHandler when market closes and erronious quotes
         * are read at that time messing things up....I'll know in a wk if it worked.
         */
        beginTradingHighTrigger.setTime("6:40:00:AM");      
        stopTradingMarketClosed.setTime("12:59:00:PM");
        aveInTradingTrigger.setTime("12:30:00:PM");
        timeEvents.setDelayOpen(900 /*15 minute delay on open */);
        
        if (autoTradingOn == true) {
            beginTrading = beginTradingHighTrigger.isOkToTrade();        
        }else {
            beginTrading = true;
        }

    }

    public void setUpdateFile(boolean update) {
        updateFile = update;
    }

    private boolean getUpdateFile() {
        return (updateFile);
    }

    private void setPosLock() {
        posLock = true;
    }

    private void clrPosLock() {
        posLock = false;
    }

    private boolean getPosLock() {
        return (posLock);
    }

    public boolean getTerminated() {
        return (traderTerminated);
    }

    public void setStatistic(positionStatistics ps) {
        actStats = ps;
    }
    public void setChart(positionChart pc) {
        actChart = pc;
    }
    
    public void run() {
//wfshere
//        allowToRun = true;
//        myUtils.marketVolume mvol = new myUtils.marketVolume();

        while (allowToRun == true) {
            
            /* check if we added or deleted some positions */
            if(positionsCount != positionsToRun.posDataVecSize()) {
                /* position added/removed so update our count of them */
                System.out.println("position added/deleted, update stuff.");
                positionsCount = positionsToRun.posDataVecSize();
                /* so update stuff that cares */
                if(actChart != null){
                    /* charts care.. */
                    actChart = new positionChart(positionsToRun);
                }
                if(actRunStatusScreen != null) {
                    /* status screen cares...*/
                    actRunStatusScreen.setTimerPos(positionsCount + 2, 0, false /*init*/);
                }
                
                positionsToRun.posDataSaveToDisk(positionsToRun.getPositionFileName(), false /* no prompt */);
                            System.out.println("updated "+positionsToRun.getPositionFileName()+" file with new position change.");
            }
            ++runTimes;
//            System.out.println("running run : "+runTimes+" times."+" minutes : "+runMinutes+" runTraderRunTimes: "+ runTraderRunTimes);



            if (actRunStatusScreen != null) {
                actRunStatusScreen.bumpStatusTimer();
                actRunStatusScreen.updateTimer();
            }
            if ((runTimes % 60) == 0) {
                runMinutes++;
                /* every 5 minutes send a keep alive to keep us logged in 
                 * and valid with Td ameritrade.
                 * 4/29/11
                 * but only during trading time. Doing this all night causes exception sometimes
                 * if td ameritrade goes down for whatever reason.
                 */
                if((beginTrading == true) && ((runMinutes % 5) == 0) && (ibApi.keepAlive() == false )) {
                        /* it failed ? just try loging in again. */
                        System.out.println("posistionTrader: TD keep alive failed! Forcing Fresh LoginIN...");
                        /* wfs 9/30/10 keep getting invalid session exception, so added this routine
                         * to force a login when our keep alive fails.
                         */
                        ibApi.loginIn(true /*forcLogin*/);
                }

            }
            
            if ((autoTradingOn == true) && (beginTradingHighTrigger.didEventOccur() == true)) {
                System.out.println("it happend!!!! beginTradingHighTrigger!!!");
                beginTradingHighTrigger.ackEvent();
                beginTrading = true;
                System.out.println("logout, then login..");
                ibApi.logOut();
                // login, it timed out, likely.
                ibApi.loginIn();
                System.out.println("enabling all VM.");
                positionsToRun.getVmManager().vmEnableAll();
            }else if ((beginTrading == false) && (beginTradingHighTrigger.isOkToTrade() == true)) { 
                /* need to put this check in to allow trading if we start after the trigger above. */
                beginTrading = true;
                System.out.println("logout, then login..");
                ibApi.logOut();
                // login, it timed out, likely.
                ibApi.loginIn();
                System.out.println("enabling all VM.");
                positionsToRun.getVmManager().vmEnableAll();
            }
            if ((autoTradingOn == true) && (stopTradingMarketClosed.didEventOccur() == true)) {
                System.out.println("it happend!!!! stopTradingMarketClosed!!!");
                stopTradingMarketClosed.ackEvent();
                beginTrading = false;
                /* tell alarms that market is now closed....*/
                alarmsEvents.setMarketIsClosed(true);
                /* A */
                System.out.println("disabling all VM.");
                positionsToRun.getVmManager().vmDisableAll();
                positionsToRun.getVmManager().vmStopAll();
                /* 3/27/12 moved the folling down here from A above. Noticed that if program status screen is not
                 closed after market closed, and prm terminates w/o closing stat screen, the bins are messed up.
                 I think it's because of this ordering here. should stop vm then save to disk. we were saving then stopping.
                 The status screen, when closed, would then save to disk and that's why it would work.....k*/
                System.out.println("saving VM to disk.");
                positionsToRun.vmManager.saveToDisk(positionsToRun.getPositionFileName());
            }
            if ((beginTrading == true) && (positionsToRun.getVmManager().vmControlGet() == false)) {
                //checks if beginTrading is true and vm is not on. if so turn it on...
                System.out.println("enabling all VM.");
                positionsToRun.getVmManager().vmEnableAll();


            }
            /*
             * average in feature
             */
            if ((autoTradingOn == true) && (aveInTradingTrigger.didEventOccur() == true)) {
                System.out.println("it happend!!!! aveInTradingTrigger!!!");
                aveInTradingTrigger.ackEvent();
                /*
                 * once a day, when this trigger happens, process implied vol
                 * averaging in feature..
                 */
                System.out.println("set aveInCheckEvent to TRUE!!!");
                aveInCheckEvent = true;

            }

            if (runTimes % (pollFrequency) == 0) {
                if (!pauseRun) {
                    checkStockMoveRequirment();
                    System.out.println("");
                    System.out.println("running runTrader("+positionsToRun.getPositionFileName()+") : " + runTraderRunTimes + " times w/stockMove = " + getStockMove() + " Filler = " + filler);
                    
                    /* when status starts up you want to protect agains table acccess with same positions with trader. */
                    /* also add to not trade on weekends, next need to exclude holidays when market is closed...*/
                    /* added 4/29/11 a check of operatioin error. when this happens things get screwed up so should abort */
                    if((beginTrading == true) && (myUtils.isItTradingDay() == true) && ibApi.getOperationError() == false) {
                        positionsToRun.semTake();
                        runTrader();
                        positionsToRun.semGive();
                        
                        if (this.getUpdateFile() == true) {
                            //time to update file
                            positionsToRun.posDataSaveToDisk(positionsToRun.getPositionFileName(), false /* prompt? */);
                            System.out.println("updated "+positionsToRun.getPositionFileName()+" file with new adjustment change.");
                            this.setUpdateFile(false);
                        }
                    }
                }

            }
            //wfs added 4/29/11 to catch io type errors with connection to td ameritrade
            //this causes us grief....
            if (ibApi.getOperationError() == true) {
                System.out.println("positionTrader: getOperationError returned true !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }

            try {
                positionTrader.sleep(runFrequency);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        } /* while */
        System.out.println("positionTrader: exited while loop, allowToRun == " + allowToRun);
        this.traderTerminated = true;
    }

    class myFloat {

        myFloat(float fin) {
            fval = fin;
        }
        float fval;

        void setVal(float fin) {
            fval = fin;
        }

        float getVal() {
            return (fval);
        }
    }
    public  myFloat creMyFloat(){
        return new myFloat(0);
    }
    boolean wfs = true;
    public void runTrader() {

        positionData actPosToRun;

        int eccessVal;
        positionAdjustment actAdj;
        float actStockPrice;
        positionEditor editPos;
        boolean quoteError = false;
        myFloat percentMove = new myFloat(0);
        double aveInTemp = 0.0;
        

        actPosToRun = positionsToRun.posDataFetchNext(true /* fresh/first fetch */);

        while (actPosToRun != null) {

           
            quoteError = false;
            actPosToRun.setTrading(true);
            if (actPosToRun != null) {
                System.out.println("checking " + actPosToRun.longTicker + " stock.");
            }
             /* added 3/19/2012....should not bother with closed positions. 
             *  get quotes from expired options ...
             */
            
            if (actPosToRun.currentState == positionStates.CLOSED) {
                sQuote.bid = actStats.getStockBid(actPosToRun.longTicker);
                sQuote.ask = actStats.getStockAsk(actPosToRun.longTicker);
                sQuote.last = actStats.getStockLast(actPosToRun.longTicker);
                oQuote.bid = actStats.getOptionBid(actPosToRun.longTicker);
                oQuote.ask = actStats.getOptionAsk(actPosToRun.longTicker);
                oQuote.last = actStats.getOptionLast(actPosToRun.longTicker);
                oQuote.value = actStats.getOptionValue(actPosToRun.longTicker);


            } else {
                sQuote = actIbApi.getQuote(actPosToRun.longTicker, false);
//            oQuote = actTraderApi.getQuote("+" + actPosToRun.shortTicker, true /* option quote */);
                oQuote = actIbApi.getQuote(actPosToRun.shortTicker, true /*
                         * option quote
                         */);
            }
            /*
             * wfs 4/3/11
             * added this to catch when we get an error from td ameritrade. If this happens we don't
             * want to even look at the quote results so just skip.
             */
            if ((sQuote == null) || (oQuote == null ) || ((oQuote != null) && (oQuote.value == -1)) || 
                (sQuote.errorGettingQuote == true) || (oQuote.errorGettingQuote == true)) {
                
                System.err.println("runTrader: got error getting Quote!!!!!!!!!!!!!!!!!!!!!!!");
                System.err.println("runTrader: skipping this cycle.....");
                continue;
            }
            
            
            /* 3/19/2012 added check for closed positions, don't want to do those iv */
            if ((positionsToRun.getVmManager() != null) && (actPosToRun.getVm() != null) && (actPosToRun.currentState != positionStates.CLOSED)) {
                if ((positionsToRun.getVmManager().vmControlGet() == true) && (actPosToRun.getVm().isStartDayOn() == false)) {
                    /* set oneshot false so we use start/stop operation..
                     */
                    actPosToRun.getVm().setOneShot(false);
                    actPosToRun.getVm().startDayMonitor();
                }
                if ((positionsToRun.getVmManager().vmControlGet() == false) && (actPosToRun.getVm().isStartDayOn() == true)) {

                    actPosToRun.getVm().stoptDayMonitor();
                }
                /* don't count IV of zero...means the read was wrong */
                if(oQuote.impliedVolatility > 0) {
                    actPosToRun.getVm().volatilityMonitorTick((float) oQuote.impliedVolatility);
                }else{
                    System.out.println("runTrader: impliedVol was zero??? Bypassed VM for " + actPosToRun.shortTicker);
                }

            }
/* wfs test!!!!!!!!!!!
 */ 
            if (false) {
            if(wfs == true) {
                    /* buy shorts first */
                    editPos = new positionEditor(actPosToRun);
                    editPos.editAdjData.setAdjDate(new Date());
                    editPos.editAdjData.setActualMove(percentMove.getVal());
                    System.out.println("date:" + editPos.editAdjData.getAdjDateStr());                    
                    editPos.commitPosBuyShorts((float) sQuote.last, (float) oQuote.value, 1, (float) Math.abs(oQuote.delta));
                    /* now buy longs */
                    editPos = new positionEditor(actPosToRun);
                    editPos.editAdjData.setAdjDate(new Date());
                    editPos.editAdjData.setActualMove(percentMove.getVal());
                    System.out.println("date:" + editPos.editAdjData.getAdjDateStr());
                    
                    editPos.commitPosBuyLongs((float) sQuote.last, (float) oQuote.value, 1, (float) Math.abs(oQuote.delta));
                    this.setUpdateFile(true);
                    wfs = false;
                    
            }
            }
            
/* wfs end test!!!!!!!!!!
 * 
 */
            
            

            /* added check of implied vol and delta here because sometimes these values are wrong and zero.
               If this happens, avoid trading it and just let it be known that something is wrong.  
             */
            quoteError = (  (checkQuoteErrors(actStats, actPosToRun.longTicker, sQuote)) || 
                            (checkQuoteErrors(actStats, actPosToRun.longTicker, oQuote)) ||
                            !(oQuote.impliedVolatility > 0) ||
                            (oQuote.delta == 0)
                        );
            if(quoteError == true) {
                System.err.println("RunTrader: quoteError occurred!!!!! ");
                if (oQuote != null) {
                    System.err.println("implied Vol: " + oQuote.impliedVolatility + " delta: " + oQuote.delta);
                }else {
                    System.err.println("oQoute == null???");
                }
                
            } else {
                /* added 9/14/12 to help calculate original 
                 * values and current ligudating values etc. from higher level 
                 * routines.
                 */
                actPosToRun.sQuote = sQuote.last;
                actPosToRun.oQuote = oQuote.value;
            }
            /* check if it's time to process implied volatility averaging in feature ....
             * do only on active non quoteError positions.
             */
            if ((quoteError == false) && (aveInCheckEvent == true) && (actPosToRun.currentState == positionStates.ACTIVE)){
                System.out.println("runTrader: aveInCheckEvent happenned!!! for " + actPosToRun.longTicker);
                if ((actPosToRun.ivAveIn == true) && (actPosToRun.ivAveInAdjustmentCnt <= actPosToRun.ivAveInMaxAdjustments)) {
                    if (actPosToRun.ivAveInReading > 0.0) {
                        System.out.println("runTrader: process averaging In option....." + actPosToRun.getVm().todaysBin.runningAveGet());
                        /*
                         * ivAveInReading holds the ivAve of the last time we
                         * did an adjustment. Complare this value with the new
                         * todaysBin RA value and if the difference is greater
                         * than or equal to (percentage wise) the allowed
                         * percentage (ivAveInPercentDrop), then trigger an
                         * aveIn adjustment and update the ivAveInReading with
                         * this new lower value (todaysBin RA).
                         */
                        aveInTemp = (actPosToRun.ivAveInReading - actPosToRun.getVm().todaysBin.runningAveGet()) / actPosToRun.ivAveInReading;
                        if (aveInTemp >= actPosToRun.ivAveInPrecentDrop) {
                            System.out.println("runTrader: process averaging In option. Trigger the adjustment!!! actual drop since last adjustment is " + aveInTemp);
                            /*
                             * buy shorts first
                             */
                            editPos = new positionEditor(actPosToRun);
                            editPos.editAdjData.setAdjDate(new Date());
                            editPos.editAdjData.setActualMove(percentMove.getVal());
                            System.out.println("date:" + editPos.editAdjData.getAdjDateStr());
                            editPos.commitPosBuyShorts((float) sQuote.last, (float) oQuote.value, 1, (float) Math.abs(oQuote.delta));
                            /*
                             * now buy longs
                             */
                            editPos = new positionEditor(actPosToRun);
                            editPos.editAdjData.setAdjDate(new Date());
                            editPos.editAdjData.setActualMove(percentMove.getVal());
                            System.out.println("date:" + editPos.editAdjData.getAdjDateStr());

                            editPos.commitPosBuyLongs((float) sQuote.last, (float) oQuote.value, 1, (float) Math.abs(oQuote.delta));
                            /* 3/27/12 - must update the stored value for next time */
                            actPosToRun.ivAveInReading = actPosToRun.getVm().todaysBin.runningAveGet();
                            this.setUpdateFile(true);
                            /* 10/12/12 dumb ass forgot to increment this value to stop averaging in when we hit max!! */
                            actPosToRun.ivAveInAdjustmentCnt++;

                        }else{
                            System.out.println("runTrader: process averaging In option. drop not enough only " + aveInTemp);
                            System.out.println("runTrader: process averaging In option. ivAveReading " + actPosToRun.ivAveInReading);
                            System.out.println("runTrader: process averaging In option. todaysBin RA " + actPosToRun.getVm().todaysBin.runningAveGet());
                            
                            
                        }
                        
                    }else {
                        System.out.println("runTrader: process averaging In option first time so just read in running average ..");
                        actPosToRun.ivAveInReading = actPosToRun.getVm().todaysBin.runningAveGet();
                        this.setUpdateFile(true);
                    }                                                         
                }
                        
            }
            

            /*
             * 3/19/12 wrapped the following with check of closed positinos,
             * dont need to do those charts or stats but do do them for update status screen...
             */
            if (actPosToRun.currentState != positionStates.CLOSED) {
                if ((quoteError == false) && (actChart != null)) {
                    actChart.logToFile(actPosToRun, (float) sQuote.last, (float) oQuote.value);
                }


                if ((quoteError == false ) && (actStats != null)) {
                    actStats.sQuoteMinMax(actPosToRun.longTicker, sQuote);
                    actStats.oQuoteMinMax(actPosToRun.longTicker, oQuote);

                }
            }
            
            if (actRunStatusScreen != null && (quoteError == false)) {
                actRunStatusScreen.updateStatus(actPosToRun, sQuote, oQuote);
            }

            /* first see if we need to close out this position because of 
             * either gain lock or stop loss critera. The position must
             * first be in active state. If a trigger occures, then the position is set
             * to closed state and therefore skips the adjustment check that follows.
             */
            if ((quoteError == false) && (actPosToRun.currentState == positionStates.ACTIVE)) {
                checkLockGainStopLossTriggers(actPosToRun, sQuote, oQuote);
            }
            /* only make adjustments if no error, we are in ACTIVE state and stock moved enough. */
            if (actPosToRun.currentState == positionStates.ACTIVE) {
                    
               /* now check for price movement in stock price to posibly trigger 
                * an adjustment for this position. You compare the previous 
                * adjustments stock price vs the current stock price. If the difference is 
                * enough, then an adjustment is made. 
                */
                actStockPrice = actPosToRun.posAdjGet(actPosToRun.posAdjustments() - 1).longPrice;
                if ((quoteError == false) && (stockMoved(actStockPrice,(float) sQuote.last, percentMove /*gives us the actual move it made */) == true)) {
                    /* moved significantly so make adjustement */

                    editPos = new positionEditor(actPosToRun);
                    editPos.editAdjData.setAdjDate(new Date());
                    editPos.editAdjData.setActualMove(percentMove.getVal());
                    System.out.println("date:" + editPos.editAdjData.getAdjDateStr());
                    if (filler == true) {
                        editPos.commitPosEdit((float) sQuote.last,(float) oQuote.value,(float) Math.abs(oQuote.delta), filler);
                    } else {
                        editPos.commitPosEdit((float) sQuote.last,(float) oQuote.value,(float) Math.abs(oQuote.delta));
                    }

                    /* check if quartile switch occurred and we have enough eccess to justify another adjustment....*/
                    if (editPos.isInterimAdj() && ((eccessVal = editPos.checkQuartileSwitchAmount()) > 0)) {
                        System.out.println("quartile switch and we still have more to do: eccessVal = "+eccessVal);
                        editPos = new positionEditor(editPos.getEditPosition());
                        editPos.editAdjData.setAdjDate(new Date());
                        if (filler == true) {
                            editPos.commitPosEdit((float) sQuote.last,(float) oQuote.value,(float) Math.abs(oQuote.delta), filler);
                        } else {
                            editPos.commitPosEdit((float) sQuote.last, (float) oQuote.value,(float) Math.abs(oQuote.delta));
                        }
                    }

                    System.out.println("yo! stock moved by more than " + this.getStockMove() + "%! (actually " + percentMove.getVal() + " %).");
                    actStockPrice = (float) sQuote.last;
                    this.setUpdateFile(true);

                }
            } /* if ACTIVE */

            actPosToRun = positionsToRun.posDataFetchNext(false /* not fresh/fetch */);

        } /* while */
        
        /* if aveIn was triggred, means all ports were already processed, so clear it now....*/
        if (aveInCheckEvent == true) {
            System.out.println("aveInCheckEvent set to false.");
            aveInCheckEvent = false;
        }
        runTraderRunTimes++;


    } /* runTrader */


    public void runStatusScreen() {
        actRunStatusScreen = new dispStatusScreen(
                                        "Trade Status Screen (account: "+positionsToRun.getAccountNumber()+") " + 
                                        "(Client:" + Integer.toString(actIbApi.getCliendId()) +
                                        " " +positionsToRun.getPositionFileName() + ")"
                            );
        actRunStatusScreen.setPreferredSize(new Dimension(1500, (positionsToRun.posDataVecSize() * 50)+100));
        //actRunStatusScreen.setSize(500, 500);
        actRunStatusScreen.pack();
        actRunStatusScreen.setVisible(true);
    }

    public class dispStatusScreen extends JFrame {

        statusScreenTable actStatusScreen;
        int timerPosLine;
        int prevTimerPosLine;
        int timerPosCol;
        int minutes;
        int seconds;
        int hours;
        positions positionsForStatus;
        String[] columns = {
            "Ticker", "StockQ", "Change", "%", "OptionQ ", "Change", "%", "#Adj", "State", "Profit/Loss", "(%)", "Long/Short Bias"
        };
        String[][] dataArr = new String[100][columns.length];
        JTable jt = null;
        JScrollPane pane = null;
        final int TICKER = 0;
        final int SQUOTE = 1;
        final int SCHANGE = 2;
        final int SPERCENT = 3;
        final int OQUOTE = 4;
        final int OCHANGE = 5;
        final int OPERCENT = 6;
        final int ADJ = 7;
        final int STATE = 8;
        final int PROFIT = 9;
        final int PPERCENT = 10;
        final int BIAS = 11;
        final int _LAST_ = 12;

        private dispStatusScreen(String title) {
            super(title);
            //setSize(150, 150);
            addWindowListener(new WindowAdapter() {
            

                public void windowClosing(WindowEvent we) {
                    System.out.println("display status screening closing, saving statistics to file.");
                    actStats.statSaveToDisk(positionsToRun.getPositionFileName(), false /* no prompt */);
                    //positionsToRun.vmManager.vmDisableAll();
                    positionsToRun.vmManager.saveToDisk(positionsToRun.getPositionFileName());
                }
            });
            timerPosLine = prevTimerPosLine = 0;
            actStatusScreen = new statusScreenTable();
            /* have status run with it's own copy of positions to 
            run so that they do not interfere with each other.
             */
            positionsForStatus = positionsToRun;

            /* sem to lock use of same positions  */

            positionsForStatus.semTake();
            initTable(positionsForStatus);
            positionsForStatus.semGive();

            //pack();
            //setVisible(true);

        }

        private class statusScreenTable {

            final int MAX_STAT_POS = 20;
            private String[] screenTable = new String[MAX_STAT_POS];
            private int tableCount;
            private positionData[] statusPositions = new positionData[MAX_STAT_POS];
            private int statusPosCount;

            statusScreenTable() {
                tableCount = 0;
                statusPosCount = 0;
                for (int idx = 0; idx < screenTable.length; idx++) {
                    screenTable[idx] = "";
                    statusPositions[idx] = null;
                }

            }

            private int findEntry(String inTick) {
                int cnt = 0;
                while (cnt < screenTable.length) {
                    if (inTick.equals(screenTable[cnt++])) {
                        return (cnt);
                    }
                }
                return (0);
            }

            private int addEntry(String inTick) {
                if (tableCount < screenTable.length) {
                    screenTable[tableCount++] = inTick;
                    return (tableCount);
                } else {
                    return (0);
                }
            }
        }

        public void updateStatus(positionData posin, quoteInfo squote, quoteInfo oquote) {
            if (actRunStatusScreen == null) {
                return;
            } else {
                updateTable(posin, squote, oquote);
            }
        }

        private void setTimerPos(int line, int col, boolean initme) {
            if(initme == true) {
                prevTimerPosLine = timerPosLine = line;
                timerPosCol = col;
                return;
            }
            /* save old line so that we can erase it on the display 
             * when we get a new one.
             */
            prevTimerPosLine = timerPosLine;
            timerPosLine = line;
            timerPosCol = col;
        }
        

        private void updateTimer() {
            int column = 0;
            
            dataArr[timerPosLine][timerPosCol + column] = getStatusTimer();
            jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
            column++;
            dataArr[timerPosLine][timerPosCol + column] = "Updates: " + Integer.toString(runTraderRunTimes);
            jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
            column++;
            dataArr[timerPosLine][timerPosCol + column] = "AdjTrig: " + Integer.toString(getStockMove()) + "%";
            jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
            column++;
            if (filler1 == true) {
                dataArr[timerPosLine][timerPosCol + column] = "Filler1: ON";
                jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
                column++;
            }
            
            dataArr[timerPosLine][timerPosCol + column] = "Market Open: " + tradeOnceLowTrigger.isMarketOpen();
            jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
            column++;
            
            dataArr[timerPosLine][timerPosCol + column] = "AutoLossGain: " + stopLossLockGain;
            jt.getModel().setValueAt(dataArr[timerPosLine][timerPosCol + column], timerPosLine, timerPosCol + column);
            column++;
            /* check if pos has changed (new pos added)
             * and if so need to erase old line.
             */
            if(timerPosLine != prevTimerPosLine) {
                /* new line to show updates so erase old one.. */
                do{
                    dataArr[prevTimerPosLine][timerPosCol+column] = "                    ";
                    jt.getModel().setValueAt(dataArr[prevTimerPosLine][timerPosCol + column], prevTimerPosLine, timerPosCol + column);
                    
                }while(column-- != 0);
                prevTimerPosLine = timerPosLine;
            }

        }

        private void updateTable(positionData pos, quoteInfo sQuote, quoteInfo oQuote) {
            float origValue = 0;
            float curValue = 0;
            float adjValue = 0;
            float curProfit = 0;
            float adjProfit = 0;
            DecimalFormat dfl = new DecimalFormat("000.00");
            DecimalFormat dfs = new DecimalFormat("00.00");
            DecimalFormat df = new DecimalFormat("00");
            int updateEntry;
            float change;

            String curPosNeg = "-";
            String adjPosNeg = "-";
            positionAdjustment adj;
            int numOfAdjustments;

            if ((updateEntry = actStatusScreen.findEntry(pos.longTicker)) == 0) {
                /* entry not found so it must be new - handle it...*/
                updateEntry = actStatusScreen.addEntry(pos.longTicker);

            }

            /* entry exists already, updateEntry is 1 based so take one away to zero based */
            updateEntry--;
            dataArr[updateEntry][TICKER] = pos.longTicker;

            change = (float)(sQuote.last - sQuote.prevClose);
            curPosNeg = (change >= 0) ? "+" : "-";
            dataArr[updateEntry][SQUOTE] = Float.toString((float)sQuote.last);
            dataArr[updateEntry][SCHANGE] = curPosNeg + Float.toString(Math.abs(Round(change, 2)));
            dataArr[updateEntry][SPERCENT] = getMovement((float)sQuote.last, (float)sQuote.prevClose);
            change = (float)(oQuote.last - oQuote.prevClose);
            curPosNeg = (change >= 0) ? "+" : "-";
            dataArr[updateEntry][OQUOTE] = Float.toString((float) oQuote.last);
            dataArr[updateEntry][OCHANGE] = curPosNeg + Float.toString(Math.abs(Round(change, 2)));
            dataArr[updateEntry][OPERCENT] = getMovement((float) oQuote.last,(float) oQuote.prevClose);
            dataArr[updateEntry][STATE] = pos.currentState.toString();
            /* the following only makes sense if we are in active or closed states
             * we don't want to bother if we are in fill or in the middle of adjusting.
             */
            if ((pos.currentState == positionStates.ACTIVE) || (pos.currentState == positionStates.CLOSED)) {

                numOfAdjustments = pos.posAdjustments();
                dataArr[updateEntry][ADJ] = myUtils.format((numOfAdjustments - 2), 2) + " (" + myUtils.format(pos.getAdjCounter(), 2) + ")";
                origValue = (pos.staLongShares * pos.longEntryPrice) + (pos.staShortShares * pos.shortEntryPrice * 100) + pos.posAdjGet(1).balance;
                adj = (positionAdjustment) pos.posAdjGet(numOfAdjustments - 1);
                // value at adjustment time.
                adjValue = (adj.sharesLong * adj.longPrice) + (adj.sharesShort * adj.shortPrice * 100) + adj.balance;
                // value now
                curValue = (float)(adj.sharesLong * sQuote.last) + (float)(adj.sharesShort * oQuote.value * 100) + adj.balance;

                /* if position is closed we need to use the position closed risk value
                 * which is the very last adjustment before the closing of the position; this
                 * is the risk amount or money in the market that is used to calculate the percent profit/loss.
                 */
                if ((pos.currentState == positionStates.CLOSED) && (pos.closed == true)) {
                    curProfit = ((curValue - origValue) / (pos.posClosedRisk)) * 100;
                    adjProfit = ((adjValue - origValue) / (pos.posClosedRisk)) * 100;
                } else {
                    curProfit = ((curValue - origValue) / (pos.posBalance - adj.balance)) * 100;
                    adjProfit = ((adjValue - origValue) / (pos.posBalance - adj.balance)) * 100;
                }

                curPosNeg = (curValue >= origValue) ? "+" : "-";
                adjPosNeg = (adjValue >= origValue) ? "+" : "-";

                dataArr[updateEntry][PROFIT] = adjPosNeg + dfl.format(Math.abs(adjValue - origValue)) + " (" + curPosNeg + dfl.format(Math.abs(curValue - origValue)) + ")";
                actStats.posProfitMinMax(pos.longTicker, Round((curValue - origValue), 2));
                dataArr[updateEntry][PPERCENT] = adjPosNeg + dfs.format(Math.abs(adjProfit)) + " (" + curPosNeg + dfs.format(Math.abs(curProfit)) + ")";
                dataArr[updateEntry][BIAS] = Float.toString(Round(((adj.sharesShort * (float)oQuote.delta * 100) + adj.sharesLong), 2));
            } else {
                dataArr[updateEntry][ADJ] = myUtils.format((0), 2) + " (" + myUtils.format(pos.getAdjCounter(), 2) + ")";
                dataArr[updateEntry][PROFIT] = "+" + dfl.format(Math.abs(1 - 1)) + " (" + "+" + dfl.format(Math.abs(1 - 1)) + ")";
                dataArr[updateEntry][PPERCENT] = adjPosNeg + dfs.format(Math.abs(0)) + " (" + curPosNeg + dfs.format(Math.abs(0)) + ")";
                dataArr[updateEntry][BIAS] = Float.toString(Round((0), 2));
            }

            /* the following will update the fields affected by this update */
            jt.getModel().setValueAt(dataArr[updateEntry][0], updateEntry, 0);
            jt.getModel().setValueAt(dataArr[updateEntry][1], updateEntry, 1);
            jt.getModel().setValueAt(dataArr[updateEntry][2], updateEntry, 2);
            jt.getModel().setValueAt(dataArr[updateEntry][3], updateEntry, 3);
            jt.getModel().setValueAt(dataArr[updateEntry][4], updateEntry, 4);
            jt.getModel().setValueAt(dataArr[updateEntry][5], updateEntry, 5);
            jt.getModel().setValueAt(dataArr[updateEntry][6], updateEntry, 6);
            jt.getModel().setValueAt(dataArr[updateEntry][7], updateEntry, 7);
            jt.getModel().setValueAt(dataArr[updateEntry][8], updateEntry, 8);
            jt.getModel().setValueAt(dataArr[updateEntry][9], updateEntry, 9);
            jt.getModel().setValueAt(dataArr[updateEntry][10], updateEntry, 10);
            jt.getModel().setValueAt(dataArr[updateEntry][11], updateEntry, 11);


        }

        private void initTable(positions allPositions) {


            positionAdjustment actAdj;
            int posNum;
            float curValue = 0;
            float adjValue = 0;
            float origValue = 0;
            float profit = 0;
            float adjProfit = 0;
            float curProfit = 0;
            float change;
            String posNeg;
            String adjPosNeg;
            //posData actPos = new posData();
            positionData actPos = null;
            int numOfPositions = allPositions.posDataVecSize();
            int numOfAdjustments;
            quoteInfo squote = new quoteInfo();
            quoteInfo oquote = new quoteInfo();
            DecimalFormat df = new DecimalFormat("00");
            DecimalFormat dfl = new DecimalFormat("000.00");
            DecimalFormat dfs = new DecimalFormat("00.00");


            for (posNum = 0          , actPos = allPositions.posDataFetchNext(true); posNum < numOfPositions;) {
                /* enable the adjustment counter for status screen */
                actPos.enAdjCounter();
                numOfAdjustments = actPos.posAdjustments();
                /* 3/19/2012 added this check for CLOSED positions because expired closed options were a problem */
                if (actPos.currentState == positionStates.CLOSED) {
                    squote.bid = actStats.getStockBid(actPos.longTicker);
                    squote.ask = actStats.getStockAsk(actPos.longTicker);
                    squote.last = actStats.getStockLast(actPos.longTicker);
                    oquote.bid = actStats.getOptionBid(actPos.longTicker);
                    oquote.ask = actStats.getOptionAsk(actPos.longTicker);
                    oquote.last = actStats.getOptionLast(actPos.longTicker);
                    oquote.value = actStats.getOptionValue(actPos.longTicker);

                } else if ((beginTrading == true) && (myUtils.isItTradingDay() == true)) {
                    squote = actIbApi.getQuote(actPos.longTicker, false);
//                  oquote = actTraderApi.getQuote("+" + actPos.shortTicker, true /* option quote */);
                    oquote = actIbApi.getQuote(actPos.shortTicker, true /*
                             * option quote
                             */);
                } else {
                    squote = actIbApi.getQuote(actPos.longTicker, false);
                    oquote = actIbApi.getQuote(actPos.shortTicker, true /*
                             * option quote
                             */);
                    if (true) {
                        squote.bid = actStats.getStockBid(actPos.longTicker);
                        squote.ask = actStats.getStockAsk(actPos.longTicker);
                        squote.last = actStats.getStockLast(actPos.longTicker);
                        oquote.bid = actStats.getOptionBid(actPos.longTicker);
                        oquote.ask = actStats.getOptionAsk(actPos.longTicker);
                        oquote.last = actStats.getOptionLast(actPos.longTicker);
                        oquote.value = actStats.getOptionValue(actPos.longTicker);
                    }

                }

                actStatusScreen.addEntry(actPos.longTicker);
                dataArr[posNum][TICKER] = actPos.longTicker;
                change = (float)(squote.last - squote.prevClose);
                posNeg = (change >= 0) ? "+" : "-";
                dataArr[posNum][SQUOTE] = Float.toString((float) squote.last);
                dataArr[posNum][SCHANGE] = posNeg + Float.toString(Math.abs(Round(change, 2)));
                dataArr[posNum][SPERCENT] = getMovement((float)squote.last, (float) squote.prevClose);

                change = (float)(oquote.last - oquote.prevClose);
                posNeg = (change >= 0) ? "+" : "-";
                dataArr[posNum][OQUOTE] = Float.toString((float)oquote.last);
                dataArr[posNum][OCHANGE] = posNeg + Float.toString(Math.abs(Round(change, 2)));
                dataArr[posNum][OPERCENT] = getMovement((float) oquote.last, (float) oquote.prevClose);
                dataArr[posNum][STATE] = actPos.currentState.toString();
                /* the following only makes sense if we are in active or closed states
                 * we don't want to bother if we are in fill or in the middle of adjusting.
                 */
                if ((actPos.currentState == positionStates.ACTIVE) || (actPos.currentState == positionStates.CLOSED)) {

                    dataArr[posNum][ADJ] = myUtils.format((numOfAdjustments - 2), 2) + " (" + myUtils.format(actPos.getAdjCounter(), 2) + ")";
                    origValue = (actPos.staLongShares * actPos.longEntryPrice) + (actPos.staShortShares * actPos.shortEntryPrice * 100) + actPos.posAdjGet(1).balance;
                    actAdj = (positionAdjustment) actPos.posAdjGet(numOfAdjustments - 1);
                    curValue = (float) ((actAdj.sharesLong * squote.last) + (actAdj.sharesShort * oquote.value * 100) + actAdj.balance);
                    adjValue = (actAdj.sharesLong * actAdj.longPrice) + (actAdj.sharesShort * actAdj.shortPrice * 100) + actAdj.balance;

                    /* if position is closed we need to use the position closed risk value
                     * which is the very last adjustment before the closing of the position; this
                     * is the risk amount or money in the market that is used to calculate the percent profit/loss.
                     */
                    if ((actPos.currentState == positionStates.CLOSED) && (actPos.closed == true)) {
                        curProfit = ((curValue - origValue) / (actPos.posClosedRisk)) * 100;
                        adjProfit = ((adjValue - origValue) / (actPos.posClosedRisk)) * 100;
                    } else {
                        curProfit = ((curValue - origValue) / (actPos.posBalance - actAdj.balance)) * 100;
                        adjProfit = ((adjValue - origValue) / (actPos.posBalance - actAdj.balance)) * 100;
                    }

                    posNeg = (curValue >= origValue) ? "+" : "-";
                    adjPosNeg = (adjValue >= origValue) ? "+" : "-";
                    profit = ((origValue - curValue) / (actPos.posBalance - actAdj.balance)) * 100;
                    dataArr[posNum][PROFIT] = adjPosNeg + dfl.format(Math.abs(adjValue - origValue)) + " (" + posNeg + dfl.format(Math.abs(curValue - origValue)) + ")";
                    dataArr[posNum][PPERCENT] = adjPosNeg + dfs.format(Math.abs(adjProfit)) + " (" + posNeg + dfs.format(Math.abs(curProfit)) + ")";
                    dataArr[posNum][BIAS] = Float.toString(Round(((actAdj.sharesShort * (float)oquote.delta * 100) + actAdj.sharesLong), 2));

                }else {
                    /* we are in adj or fill states so these values do not apply or make sense, so fudge. */
                    dataArr[posNum][ADJ] = myUtils.format((2 - 2), 2) + " (" + myUtils.format(actPos.getAdjCounter(), 2) + ")";
                    dataArr[posNum][PROFIT] = "+" + dfl.format(Math.abs(1 - 1)) + " (" + posNeg + dfl.format(Math.abs(1 - 1)) + ")";
                    dataArr[posNum][PPERCENT] = "+" + dfs.format(Math.abs(0)) + " (" + posNeg + dfs.format(Math.abs(0)) + ")";
                    dataArr[posNum][BIAS] = Float.toString(Round((0), 2));
                    
                }
                if (++posNum < numOfPositions) {
                    actPos = allPositions.posDataFetchNext(false);
                    numOfAdjustments = actPos.posAdjustments();

                }
            }
            setTimerPos(positionsCount + 2, 0, true /*init */);
            dataArr[posNum + 2][0] = getStatusTimer();
            if (jt == null) {
                jt = new JTable(dataArr, columns);
            }
            if (pane == null) {
                pane = new JScrollPane(jt);
            }

            getContentPane().add(pane);
        }

        private void bumpStatusTimer() {
            float tmpFloat;
            int tmpInt;
            if (actRunStatusScreen != null) {
                tmpInt = runTimes / 60;
                tmpFloat = ((float) runTimes / (float) 60.0) - tmpInt;
                tmpFloat *= 60;
                seconds = (int) Round(tmpFloat, 2);

                hours = runTimes / 60 / 60;
                tmpInt = runTimes / 60 / 60;
                tmpFloat = ((float) runTimes / (float) 60.0 / (float) 60.0) - tmpInt;
                tmpFloat *= 60;
                minutes = (int) Round(tmpFloat, 2);


            //actRunStatusScreen.updateTimer();
            } else {
                hours = 0;
                minutes = 0;

            }
        }

        private String getStatusTimer() {

            return (Integer.toString(hours) + ":" + Integer.toString(minutes) + ":" + Integer.toString(seconds));

        }
    }

    public void setPositions(positions actPos) {
        positionsToRun = actPos;
    }

    public float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float) tmp / p;
    }

    private String getMovement(float ina, float inb) {
        float change;
        float percent;

        String percentDir = "-";
        change = (ina - inb);
        if (change > 0) {
            percentDir = "+";
        } else {
            percentDir = "-";
        }

        percent = (Math.abs(change) / ina);

        percent *= 100;
        percent = Round(percent, 2);
        return (percentDir + Float.toString(percent));
    }

    /* run positions in posStorage as follows:
     * do the following while number of days to test is not zero
     *  get stock quote on position
     *  if the change in stock price is 5% or higher then do the following:
     *      get the option quote on this position
     *      make adjustment
     * bump to next day
     * 
     */
    public boolean stockMoved(float stockOld, float stockNew) {

        float sDiff;
        float percent;
        sDiff = Math.abs(stockOld - stockNew);
        percent = (sDiff / stockOld) * 100;
        if (percent > stockMove) {
            return (true);

        }
        return (false);
    }
    private void setStockMove(int newMove) {
        stockMove = newMove;
    }
    public int getStockMove() {
        return(stockMove);
    }
    public boolean stockMoved(float stockOld, float stockNew, myFloat retPercent) {

        float sDiff;
        float percent;


        retPercent.setVal(0);
        /* check if single trade perday is enabled and do it only if it is 
        time. Time was set initially via config file 
         */
       
        System.out.println("stockMoved: stockOld " + stockOld + "stockNew " + stockNew);
        
        sDiff = Math.abs(stockOld - stockNew);
        percent = (sDiff / stockOld) * 100;
        if (percent > stockMove) {
            retPercent.setVal(percent);
            return (true);

        }
        return (false);
    }

    public void setAllowToRun(boolean run) {
        allowToRun = run;
        System.out.println("setAllowToRun: " + allowToRun + " for posTrader servicing : "+positionsToRun.getPositionFileName());
    }

    public void setPauseToRun(boolean pauseIt) {
        pauseRun = pauseIt;
    }

    public boolean getPauseToRun() {
        return (pauseRun);
    }

    public boolean checkQuoteErrors(positionStatistics statin, String tickerin, quoteInfo quote) {
        boolean retv = false;

        if ((statin != null) && (quote == null)) {
            statin.quoteErrors(tickerin);
            retv = true;
        }
        return (retv);
    }
    
    private void checkStockMoveRequirment() {
    
        if ((oneTradePerDay == true) && (tradeOnceLowTrigger.didEventOccur() == true)) {
            savedStockMoved = getStockMove();
            setStockMove(stockMoveOneTime);
            System.out.println("got event tradeOnceLowTrigger!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!, set stockMovement to: " + getStockMove());
            tradeOnceLowTrigger.ackEvent();
            
        } else {
            setStockMove(savedStockMoved);
        }
    }
    
    private void checkLockGainStopLossTriggers(positionData posin, quoteInfo sqin, quoteInfo oqin) {
        float origBalance;
        float curValue;
        float dollarPL;
        float percentPL;
        float moneyAtWork;
        positionAdjustment posAdj;
        float localGainLock;
        float localStopLoss;
        boolean closeTrigger = false;
        positionEditor posToEdit;
        if(true) {
            /* set local copies of stoploss & gainlock now. 
             * the global setting overrides the per position setting. So check
             * the global and if true, use the global settings. Otherwise use the
             * per position settings.
             */
            if (positionConfig.getActConfig().getStopLossLockGain() == true) {
                localGainLock = positionConfig.getActConfig().getLockGain();
                localStopLoss = positionConfig.getActConfig().getStopLoss();
            }else {
                localGainLock = posin.getGainLock();
                localStopLoss = posin.getStopLoss();
            }
            
            posAdj = (positionAdjustment) posin.posAdjGet(posin.posAdjustments() - 1);
                
            /* the current value is everything at risk (in the market) 
             * plus the current remaining balance. We add the balance to allow us
             * a comparison to what we started with and this tells us how much we are ahead/behind.
             */
            curValue = (float) ((posAdj.sharesLong * sqin.last) + (posAdj.sharesShort * oqin.value * 100) + posAdj.balance);
            origBalance = posin.posBalance;
            /* money at work is original position balance minus current balance.
             * this is the money in the market right now working or at risk.
             */
            moneyAtWork = (origBalance - posAdj.balance);
            dollarPL = (curValue - origBalance);
            /* now in percent: our profit/loss divided by money currently at 
             * risk/work times 100 for percent. The result will be + for profit or - 
             * for loss.
             */
            percentPL = ((dollarPL / moneyAtWork) * 100);
            
            /* now check of PL is greater than gainLock. If so, trigger liquidation. OR if this
             * is not true, then check if PL is 
             */
            /* make our stop loss a negative number. */
           
            if (percentPL >= localGainLock) {
                System.out.println("checkLockGainStopLossTriggers: trigger gainLock!! (profit = "+percentPL+"%, $"+dollarPL+" gain)");
                closeTrigger = true;
            /* check if bid is zero. If so, just close the position, no matter what gain/loss. You just
             * don't want to deal with no bid. This happened with real money and
             * program ended up buying a whole bunch of worthless options trying to stay neutral.
             */
            }else if (oqin.bid == (float) 0.0) {

                /* we keep a debounce counter for each position.
                 * This means the condition needs to persist for a debounce
                 * period before reacting to the bid == 0 condition. I did this
                 * because every so often there was an erronious reading that eventually clears itself.
                 */
                if (posin.getBidZeroDebounce() == 0) {
                    posin.setBidZeroDebounce(3);
                }else if (posin.getBidZeroDebounce() == 1) {
                    /* we are about to trigger it because it has persisted through
                     * debounce period.
                     */
                    System.out.println("checkLockGainStopLossTriggers: option Bid == 0!!, close position!! Set Option to market sell!!");
                    posin.decrementBidZeroDebounce();
                    posAdj.setOptionMarketOrder(true);
                    closeTrigger = true;
                }else {
                    System.out.println("checkLockGainStopLossTriggers: debounce bid Zero Condition!! (debounce =  "+posin.getBidZeroDebounce()+").");
                    posin.decrementBidZeroDebounce();
                }

            }else if (percentPL <= (localStopLoss * -1.0)) {

                /* we keep a debounce counter for each position.
                 * This means the condition needs to persist for a debounce
                 * period before reacting to the StopLoss condition. I did this
                 * because every so often there was an erronious reading that clears.
                 */
                if (posin.getGainLossDebounce() == 0) {
                    posin.setGainLossDebounce(3);
                }else if (posin.getGainLossDebounce() == 1) {
                    /* we are about to trigger it because it has persisted through
                     * debounce period.
                     */
                    System.out.println("checkLockGainStopLossTriggers: trigger stopLoss!! (profit = "+percentPL+"%, $"+dollarPL+" loss)");
                    System.out.println("sqin.last = " + sqin.last + ", oqin.value = " + oqin.value);
                    posin.decrementGainLossDebounce();
                    closeTrigger = true;
                }else {
                    System.out.println("checkLockGainStopLossTriggers: debounce stopLoss!! (debounce =  "+posin.getGainLossDebounce()+").");
                    System.out.println("sqin.last = " + sqin.last + ", oqin.value = " + oqin.value);
                    posin.decrementGainLossDebounce();
                }


//                closeTrigger = true;
                
            }else{
                /* we check if we were in the middle of debounce sequence
                 * if so, clear it because it did not persist.
                 */
                if (posin.getGainLossDebounce() > 0) {
                    posin.setGainLossDebounce(0);
                    System.out.println("checkLockGainStopLossTriggers: debounce reset.");

                }
            }
            
            if (closeTrigger == true) {
                /* close this position, the trigger occured. */
                posToEdit = new positionEditor(posin);
                /* first do option/short close out..*/
                posToEdit.closeOutPosition();
                /* then long stock position close out..*/
                posToEdit = new positionEditor(posin);
                posToEdit.closeOutPosition();
                
            }
            
        }
        
    }
}
