/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

/**
 *
 * @author earlie87
 */
import ibTradeApi.ibApi;
import positions.myUtils;
import java.util.*;

public class slopeAnalysis {

    int numberOfTickers = 0;
    public SlopesFound actSlopesFound = new SlopesFound();
    
    public class SlopeStructure {

        String ticker = null;
        final int SLOPE_MAX = 100;
        public int size = 0;
        boolean currentChangeInDirection = false;
        public String currentTrendIs = null;
        public double openPosValue = 0.0;

        public double plPercent = 0.0;
        public double overAllPl = 0.0;
        public double maxRunningProfitPercent = 0.0;
        public double maxRunningLossPercent = 0.0;
        public double overAllPlIncludeOpen = 0.0;
        /*this keeps the trading day that the change occurred in
              0 == today, 1 == yesterday etc..
            */
        public int dayInTradingYear = 0;
        //this is the first purchase in sequence made. this is the cost we compare our over all profit
        //to determine profit/loss
        public double costBasis = 0.0;
        public double accumulatedPriceMoves = 0.0;
        public double avePriceMove = 0;
        public String longOrShort = slopeDefs.oBiasLongStr;
        //profit loss sim...
        public ProfitLoss pl = new ProfitLoss();
        ibApi.historicalData.slopeData[] data = new ibApi.historicalData.slopeData[SLOPE_MAX];

        public void createPl() {
            pl = new ProfitLoss();
        }

        public void addOne(ibApi.historicalData.slopeData newOne) {
            data[size++] = newOne;

        }

        public void addAllFound(ibApi.historicalData.HistorySlope allFound, String forTicker) {
            int idx = 0;
            ticker = forTicker;
            size = 0;
            if (allFound.logIdx == 0){
                System.out.println("\naddAllFound: allFound.logIdx == 0!!!!!!");
                return;
            }
            ibApi.historicalData.slopeData lastSlope;
            longOrShort = pl.longOrShort =  allFound.longOrShort;
            for (idx = 0; idx < allFound.logIdx; idx++) {
                pl.doSimTrade(allFound.getFromLog(idx));
                data[size++] = allFound.getFromLog(idx);
            }
            //last one has today's or current change in direction...get that one..
            currentChangeInDirection = data[allFound.logIdx - 1].changeInDirection;
            //wfshere
            currentTrendIs = allFound.currentTrend;
            //get last slope in history to get current closing value
            lastSlope = allFound.getHistory();
            pl.currentClosePrice = lastSlope.currentClosePrice;
            //see if there is currently open pos and get value of it..
            if (pl.isPosOpen() == true) {
                openPosValue = pl.getCurrentPosValue();
            }
            //percent profit/loss is figured by what dollar amount we are up or down (runningPl) 
            //divided by the original amount invested at the very begining (first purchase)
            //get closing price which is the first purchase price. CostBasis has very first purchase.
            costBasis = pl.costBasis;
            if(costBasis != 0){
                overAllPl = myUtils.roundMe(((pl.getRunningPl() / costBasis) * 100), 2);
                overAllPlIncludeOpen = myUtils.roundMe((((pl.getRunningPl() + pl.getCurrentPosValue()) / costBasis) * 100), 2);
                maxRunningProfitPercent = myUtils.roundMe(((pl.getMaxRunningP() / costBasis) * 100), 2);
                maxRunningLossPercent = myUtils.roundMe(((pl.getMaxRunningL() / costBasis) * 100), 2);
            }            
            plPercent = pl.getPlPercent();
            //used to let us know we missed an posChange by one day..
            dayInTradingYear = pl.dayInTradingYear;
            accumulatedPriceMoves = pl.accumulatedPriceMoves;
            avePriceMove = accumulatedPriceMoves / pl.buySellCnt;
        }

    }

    public class ProfitLoss {

        final int SHARES = 100;
        final String LONG_SHORT = "LONG";
        public int transactions = 0;
        double currentPl = 0;
        double currentPlPercent = 0.0;
        public double runningPl = 0;
        public boolean lastBought = false;
        boolean lastSold = false;
        public double maxProfit = 0.0;
        public String maxProfitDate = null;
        public String maxLossDate = null;
        public double maxLoss = 0.0;
        public double maxRunningProfit = 0.0;
        public String maxRunningProfitDate = null;
        public String maxRunningLossDate = null;
        public double maxRunningLoss = 0.0;
        double closePrice = 0.0;
        double currentClosePrice = 0.0;
        double currentCost = 0.0;
        int buySellCnt = 0;
        double accumulatedPriceMoves = 0.0;
        double currentPrice;
        double tmp;
        int ticksIn = 0;
        int ticksOut = 0;
        String longOrShort = slopeDefs.oBiasLongStr;
        
        /*this keeps the trading day that the change occurred in
              0 == today, 1 == yesterday etc..
            */
        public int dayInTradingYear = 0;
        double openPosValue = 0.0;
        double costBasis = 0.0;
        public String openPosDateIn = null;

        void doSimTrade(ibApi.historicalData.slopeData slopeIn) {
            if(isLong(longOrShort) == true){
                doLongSimTrade(slopeIn);
            }else if (isShort(longOrShort) == true){
                doShortSimTrade(slopeIn);
            }
        }
        void doLongSimTrade(ibApi.historicalData.slopeData slopeIn) {
            this.closePrice = slopeIn.closePrice;
            this.currentClosePrice = slopeIn.currentClosePrice;
            ticksIn += slopeIn.trendUpTicks;
            ticksOut += slopeIn.trendDnTicks;
            dayInTradingYear = slopeIn.dayInTradingYear;

            if (slopeIn.isTrendUp() == true) {
                //buy SHARES..
                currentCost = (SHARES * closePrice);
                if (transactions == 0) {
                    costBasis = currentCost;
                }               
                openPosDateIn = slopeIn.dateP1;
                lastBought = true;
                lastSold = false;
                transactions++;
                //reset to start fresh..
                slopeIn.maxClosingPrice = 0.0;
            } else if (lastBought == true) {
                //sell shares...for either profit or loss..
                currentPl = ((SHARES * closePrice) - currentCost);
                if ((currentPl > 0) && (currentPl > maxProfit)) {
                    //gain..remember max profit..
                    maxProfit = currentPl;
                    maxProfitDate = slopeIn.dateP1;

                } else if ((currentPl < 0) && (currentPl < maxLoss)) {
                    maxLoss = currentPl;
                    maxLossDate = slopeIn.dateP1;
                }
                runningPl += currentPl;
                //keep max/min of running pl too..
                if ((runningPl > 0) && (runningPl > maxRunningProfit)) {
                    //gain..remember max profit..
                    maxRunningProfit = runningPl;
                    maxRunningProfitDate = slopeIn.dateP1;

                } else if ((runningPl < 0) && (runningPl < maxRunningLoss)) {
                    maxRunningLoss = runningPl;
                    maxRunningLossDate = slopeIn.dateP1;
                }
                lastBought = false;
                lastSold = true;
                transactions++;
                currentPlPercent += (currentPl / currentCost);
                //used for gainLock..keep track of highest close during uptrend...
                //get price we started with with bot
                currentPrice = (currentCost / SHARES);
                //get change from buy price to maxClosing during uptrend..
                tmp = (slopeIn.maxClosingPrice - currentPrice);
                //accumulate the change in price.
                accumulatedPriceMoves += tmp;
                buySellCnt++;
            }
        }
        
       void doShortSimTrade(ibApi.historicalData.slopeData slopeIn) {
            this.closePrice = slopeIn.closePrice;
            this.currentClosePrice = slopeIn.currentClosePrice;
            ticksIn += slopeIn.trendUpTicks;
            ticksOut += slopeIn.trendDnTicks;
            dayInTradingYear = slopeIn.dayInTradingYear;

            if (slopeIn.isTrendUp() == true) {
                //buy SHARES..
                currentCost = (SHARES * closePrice);
                if (transactions == 0) {
                    costBasis = currentCost;
                }               
                openPosDateIn = slopeIn.dateP1;
                lastBought = true;
                lastSold = false;
                transactions++;
                //reset to start fresh..
                slopeIn.maxClosingPrice = 0.0;
            } else if (lastBought == true) {
                //sell shares...for either profit or loss..
                currentPl = ((SHARES * closePrice) - currentCost);
                if ((currentPl > 0) && (currentPl > maxProfit)) {
                    //gain..remember max profit..
                    maxProfit = currentPl;
                    maxProfitDate = slopeIn.dateP1;

                } else if ((currentPl < 0) && (currentPl < maxLoss)) {
                    maxLoss = currentPl;
                    maxLossDate = slopeIn.dateP1;
                }
                runningPl += currentPl;
                //keep max/min of running pl too..
                if ((runningPl > 0) && (runningPl > maxRunningProfit)) {
                    //gain..remember max profit..
                    maxRunningProfit = runningPl;
                    maxRunningProfitDate = slopeIn.dateP1;

                } else if ((runningPl < 0) && (runningPl < maxRunningLoss)) {
                    maxRunningLoss = runningPl;
                    maxRunningLossDate = slopeIn.dateP1;
                }
                lastBought = false;
                lastSold = true;
                transactions++;
                currentPlPercent += (currentPl / currentCost);
                //used for gainLock..keep track of highest close during uptrend...
                //get price we started with with bot
                currentPrice = (currentCost / SHARES);
                //get change from buy price to maxClosing during uptrend..
                tmp = (slopeIn.maxClosingPrice - currentPrice);
                //accumulate the change in price.
                accumulatedPriceMoves += tmp;
                buySellCnt++;
            }
        }
        

        public double getPlPercent() {
            return myUtils.roundMe(currentPlPercent, 4);
        }

        public double getRunningPl() {
            return runningPl;
        }

        public double getMaxRunningP() {
            return maxRunningProfit;
        }

        public double getMaxRunningL() {
            return maxRunningLoss;
        }

        public int getDaysIn() {
            return ticksIn;
        }
        
        public boolean isPosOpen() {
            boolean retVal = false;
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true) {
                retVal = (lastBought == true);
            } else if (longOrShort.equals(slopeDefs.oBiasShortStr) == true) {
                retVal = (lastSold == true);
            }
            return (retVal);
        }
        public double getCurrentPosValue() {
            double cv = 0.0;
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true) {
                if (lastBought == true) {
                    cv = ((SHARES * currentClosePrice) - currentCost);
                } else {
                    cv = 0.0;
                }
            } else if (longOrShort.equals(slopeDefs.oBiasShortStr) == true) {
                if (lastSold == true) {
                    cv = (currentCost - (SHARES * currentClosePrice));
                } else {
                    cv = 0.0;
                }
            }
            return myUtils.roundMe(cv, 2);
        }
        public String getCurrentPosDateIn() {
            String cd = "";
            if (isLong(longOrShort) == true) {
                if (lastBought == true) {
                    cd = openPosDateIn;
                } else {

                }
            } else if (isShort(longOrShort) == true) {
                if (lastSold == true) {
                    cd = openPosDateIn;
                } else {

                }
            }
            return cd;
        }
        
        public boolean isLong(String ls){
            return (ls.equals(slopeDefs.oBiasLongStr));
        }
        public boolean isShort(String ls){
            return (ls.equals(slopeDefs.oBiasShortStr));
        }
        public void setLongOrShort(String ls){
            longOrShort = ls;
        }

    }

    public class SlopesFound {

        SlopeStructure Slopes[] = new SlopeStructure[numberOfTickers];
        public int tickerNumber = 0;

        public void addOne(SlopeStructure newOne) {
            Slopes[tickerNumber++] = newOne;

        }

        public void calcPL(SlopeStructure newOne) {
            int idx = 0;
            for (idx = 0; idx < newOne.size; idx++) {
                if ((newOne.data[idx].changeInDirection == true) && (newOne.data[idx].trend == "trendUp")) {
                    //do it here...
                }
            }
        }

        public void setSize(int sz) {
            Slopes = new SlopeStructure[sz];

        }

    }
    public class MaWindowSz {

        public MaWindowSizeDialogForm maSz;
        public boolean allSet = false;
        int ma50Day = slopeDefs.MA_50DAY_MAXSZ;
        int ma100Day = slopeDefs.MA_100DAY_MAXSZ;
        int ma200Day = slopeDefs.MA_200DAY_MAXSZ;

        public MaWindowSz(boolean askUser) {
            if (askUser == true) {
                maSz = new MaWindowSizeDialogForm(new javax.swing.JFrame(), true);

                maSz.set50DayWindow(ma50Day);
                maSz.set100DayWindow(ma100Day);
                maSz.set200DayWindow(ma200Day);

                maSz.setVisible(true);
                if (maSz.allGood() == true) {
                    ma50Day = maSz.get50DayWindow();
                    ma100Day = maSz.get100DayWindow();
                    ma200Day = maSz.get200DayWindow();
                    allSet = true;
                }
            }            
        }

        public void getUserInput() {
            maSz.set50DayWindow(ma50Day);
            maSz.set100DayWindow(ma100Day);
            maSz.set200DayWindow(ma200Day);
            maSz.setVisible(true);
            if (maSz.allGood() == true) {
                ma50Day = maSz.get50DayWindow();
                ma100Day = maSz.get100DayWindow();
                ma200Day = maSz.get200DayWindow();
                allSet = true;
            }
        }

        public int get50DaySz() {
            return ma50Day;
        }

        public int get100DaySz() {
            return ma100Day;
        }

        public int get200DaySz() {
            return ma200Day;
        }
        //these are used when we don't want ask user for input..
        public void set50DaySz(int s) {
            ma50Day = s;
        }

        public void set100DaySz(int s) {
            ma100Day = s;
        }

        public void set200DaySz(int s) {
            ma200Day = s;
        }
    }
    public class BullBearCross {

        public boolean enabled = false;
        public boolean searchNow = false;
        public String crossConditionStr = slopeDefs.oBULL_50_200DMA;
        public int crossConditionInt = 0;
        //condition above exists during conditionPeriodInDays (from today)
        public int conditionPeriodInDays;
        public int daysBackWhenOccurred = 0;
        public String tickerFound;
        public boolean wePassed = false;
        public String dateItHappend = "empty.";     

    }
    public class GapUpDnTail {
        /*
        This is used for GapUp GapDn Tail definition.
        Gap is previous day's close to today's open.
        gap up:
          If up today, then we gapped UP.
          If down, we gapped Down.
        Tail gapUP:
          the tail is today's open price to Hi of day 
        Tail gapDn:
          the tail is today's open price to Low of day          
        */
        //Gap Up 
        Vector dates;
        public GapUpDnTail(){
             dates = new Vector(10,10);
        }
        int gapUpPct = 0;
        int gapUpTailPct = 0;
        //Gap Down
        int gapDnPct = 0;
        int gapDnTailPct = 0;
        int dateIndx = 0;
        int totalFound = 0;
        public void addDate(String newDate){
            dates.add(newDate);
            totalFound++;
        }
        public String getNextDate(boolean startNew){
            String retStr = null;
            if (startNew == true){
                dateIndx = 0;
            }
            if (dateIndx < dates.size()){
                retStr = (String)dates.get(dateIndx);
                dateIndx++;
            }
            return retStr;
        }
        public void setNumFound(int totNum){
            totalFound = totNum;
        }
        public int getTotalFound(){
            return totalFound;
        }
        public int getGapUpPct(){
            return gapUpPct;
        } 
        public int getGapUpTailPct(){
            return gapUpTailPct;
        } 
        public int getGapDnPct(){
            return gapDnPct;
        } 
        public int getGapDnTailPct(){
            return gapDnTailPct;
        } 
        public int setTotalFound(){
            return totalFound;
        }
        public void setGapUpPct(int in){
            gapUpPct = in;
        } 
        public void setGapUpTailPct(int in){
            gapUpTailPct = in;
        } 
        public void setGapDnPct(int in){
            gapDnPct = in;
        } 
        public void setGapDnTailPct(int in){
            gapDnTailPct = in;
        } 
        public boolean isEnabled(){
            //did user turn this on?
            return ((gapUpPct != 0) || (gapDnPct != 0) || (gapUpTailPct != 0) || (gapDnTailPct != 0));
        }
    }
    public class BackTestDataOneDay {

        public String trendToday = slopeDefs.TREND_UP;
        public String newTrend = slopeDefs.TREND_UP;
        public boolean changeInDirection = false;
        public int btConsecutiveDaysAbove10dma = 0;
        public int btConsecutiveDaysBelow10dma = 0;
        public double closePrice = 0.0;
        public boolean empty = true;
        public String date = "empty";
        public int dayCnt = 0;       
        public double runningPl = 0.0;
        public double lastPl = 0.0;
        public boolean boughtToday = false;
        public boolean soldToday = false;
        public boolean positionOpen = false;
        public double currentCostBasis = 0.0;
        
    }

    public class BackTestDataOneYear {
        public String ticker = "empty";
        public BackTestDataOneDay day[];
        int sz;
        public int numberOfTrades = 0;
        public double runningPl = 0.0;
        public double openPosValue = 0.0;
        public double openPosPl = 0.0;
        public double openPosCostBasis = 0.0;
        public double openPosClosePrice = 0.0;
        public boolean positionOpen = false;

        public BackTestDataOneYear(int szin) {
            sz = szin;
            day = new BackTestDataOneDay[szin];
            for (int i = 0; i < szin; i++) {
                day[i] = new BackTestDataOneDay();
            }
        }
        public int getSz(){
            return sz;
        }
    }
}
