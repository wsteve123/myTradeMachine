/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 *
 * @author walterstevenson
 */
public class positionData implements Serializable {

    /* added 3/2/12 to use to treat new version of this structure with respect 
     to reading from writing to file. Set this true when new parms are added to structure,
     * this will NOT read the new parms from file when reading. The writing to file with new params will
     * happen and thus the file is now updated with new parms and this is automatically set false; the only time this is set true,
     * is manually via the compile time.
     */
    public boolean parmChanged = false;
    private int posId;
    public String longTicker;
    public String shortTicker;
    public float longEntryPrice;
    public int longShares;
    public float shortEntryPrice;
    public float shortDelta;
    public int shortShares;
    public float posBalance;
    public boolean completed = false;
    private Vector adjVec;
    private int adjVecIdx = 0;
    private int adjustmentCounter;
    private boolean adjustmentCountEnable;
    private positionAdjustment currentAdjData;
    private Date posDate;
    private int posVecIdx;
    public boolean closed = false;
    public int staLongShares = 0;
    public int staShortShares = 0;
    public String posDateStr;
    public boolean tradingActive;
    public float gainLock = 0;
    public float stopLoss = 0;
    public boolean gainLossControl = false;
    /* this boolean blocks trades from going out, 
     * it's used to test a position before actually doing 
     * a trade. Note: need to make these boolean controls into 
     * a boolean bit field to save some space.
     */
    public boolean tradeLoopedback = true;
    public positionStates currentState;
    public positionStates prevState;
    public float posClosedRisk;
    private boolean tradeError = false;
    private float runningBalance = 0;
    private int gainLossDebounce = 0;
    private int bidZeroDebounce = 0;
    public volatilityMonitor posVm = null;
    /* 2/29/12
     added the following to allow averaging in to 
     lowering implied volatility. An enable, how much IV should drop
     * before adding a DN adjustment (does stock and option), and 
     * maximum number of adjustments allowd.
     */
    public boolean ivAveIn = false;
    public double ivAveInPrecentDrop = 0;
    public int ivAveInMaxAdjustments = 0;
    public double ivAveInReading = 0;
    public int ivAveInAdjustmentCnt = 0;
    
    public double sQuote = 0;
    public double oQuote = 0;

    public void setVm(volatilityMonitor vm){
        posVm = vm;
    }
    public volatilityMonitor getVm(){
        return posVm;
    }
    
    
    public positionData(boolean newDate) {

        posId = 0;
        longTicker = null;
        shortTicker = null;
        longEntryPrice = 0;
        shortEntryPrice = 0;
        shortDelta = 0;
        posBalance = 0;
        completed = false;
        shortShares = 0;
        longShares = 0;
        adjVec = new Vector(10, 2);
        adjVecIdx = 0;
        adjustmentCounter = 0;
        adjustmentCountEnable = false;
        if (newDate) {
            posDate = new Date();
        }
        posDateStr = getPosDateStr();
        posVecIdx = 0;
        tradingActive = false;
        gainLossControl = false;
        gainLock = 8;
        stopLoss = 4;
        currentState = positionStates.INACTIVE;
        prevState = currentState;
        tradeLoopedback = true;
        posClosedRisk = 0;
        tradeError = false;
        runningBalance = 0;
        gainLossDebounce = 0;
    }

    void posAdjCopy(positionAdjustment pa) {
        pa.balance = this.currentAdjData.balance;
        pa.delta = this.currentAdjData.delta;
        pa.longPrice = this.currentAdjData.longPrice;
        pa.sharesLong = this.currentAdjData.sharesLong;
        pa.sharesShort = this.currentAdjData.sharesShort;
        pa.shortPrice = this.currentAdjData.shortPrice = pa.shortPrice;


    }

    void enAdjCounter() {
        adjustmentCountEnable = true;
    }

    void disAdjCounter() {
        adjustmentCountEnable = false;
    }

    int getAdjCounter() {
        return (adjustmentCounter);
    }

    void adjCounterReset() {
        adjustmentCounter = 0;
    }

    int getPosVecIdx() {
        return (posVecIdx);
    }

    void setPosVecIdx(int idx) {
        posVecIdx = idx;
    }

    public void setPosId(int id) {
        posId = id;
    }

    public int getPosId() {
        return (posId);
    }

    void incPosId() {
        posId++;
    }

    void posAdjAdd(positionAdjustment pa) {
        adjVec.add(adjVecIdx, (positionAdjustment) pa);
        adjVecIdx++;
        if (adjustmentCountEnable == true) {
            adjustmentCounter++;
        }
    }
    
    void posAdjReplace(positionAdjustment pa, int location) {
        if(location < adjVec.size()) {
            adjVec.setElementAt(pa, location);    
        }
    }

    boolean posAdjExists(int ix) {
        return ((this.adjVecIdx) >= ix);
    }

    int posAdjIdxGet() {
        return (adjVecIdx);
    }

    boolean posAdjEmpty() {
        return (adjVecIdx == 0);
    }

    public int posAdjustments() {
        return (adjVecIdx);
    }
    positionAdjustment getLastAdjustment() {
        if (this.posAdjustments() > 0) {
            return this.posAdjGet(this.posAdjustments() - 1);
        }else {
            return null;
        }
    }
    public positionAdjustment posAdjGet(int ix) {
        if (ix == 0) {
            return ((positionAdjustment) adjVec.get(0));
        } else if ((ix > 0) && (ix < adjVec.size())) {
            return ((positionAdjustment) adjVec.get(ix));
        } else {
            return (null);
        }
    }

    void posAdjClearAll(positionData pos) {
        for (int x = 2; x < adjVec.size(); x++) {
            pos.adjVec.removeElementAt(x);
            adjVecIdx = 2;
        }

    }

    void posAdjClearOne(positionData pos, int ix) {
        if (ix <= adjVec.size()) {
            pos.adjVec.removeElementAt(ix - 1);
            adjVecIdx--;
        }
    }

    public Date getPosDate() {
        return (posDate);
    }

    public String getPosDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        if (posDate != null) {
            posDateStr = sdf.format(posDate);
//                posDateStr = DateFormat.getDateInstance().format(posDate);
        }
        return (posDateStr);
    }

    void setPosDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            posDate = sdf.parse(dateStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

    }

    String testPosDate(Date dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
        return (sdf.format(dateStr));


    }

    void setTrading(boolean activate) {
        tradingActive = activate;
    }
    public void setGainLock(float gl) {
        gainLock = gl;
    }
    public float getGainLock(){
        return(gainLock);
    }
    public void setStopLoss(float sl) {
        stopLoss = sl;
    }
    public float getStopLoss() {
        return(stopLoss);
    }
    public void setGainLockControl(boolean glc) {
        gainLossControl = glc;
    }
    public boolean getGainLockControl() {
        return(gainLossControl);
    }
    public void setState(positionStates toState) {
        currentState = toState;
    }
    public int getCurrentState() {
        return (this.currentState.enumVal);
    }
    public int getPreviousState() {
        return (this.prevState.enumVal);
    }
    public void setTradeLoopback(boolean lpin) {
        tradeLoopedback = lpin;
    }
    public boolean getTradeLoopback() {
        return(tradeLoopedback);
    }
    public boolean getTradeError() {
        return tradeError;
    }
    public void  setTradeError(boolean te) {
        tradeError = te;
    }
    public float getRunningBalance() {
        return runningBalance;
    }
    public void setRunningBalance(float rb) {
        runningBalance = rb;
    }
    public float addRunningBalance(float val) {
        runningBalance += val;
        return(runningBalance);
    }
    public float subtractRunningBalance(float val) {
        runningBalance -= val;
        return runningBalance;
    }
    public int getGainLossDebounce() {
        return gainLossDebounce;
    }

    public void setGainLossDebounce(int debounce) {
        gainLossDebounce = debounce;
    }
    public void decrementGainLossDebounce() {
        if (gainLossDebounce > 0) {
            gainLossDebounce--;
        }
    }
    public int getBidZeroDebounce() {
        return bidZeroDebounce;
    }
    public void setBidZeroDebounce(int debounce) {
        bidZeroDebounce = debounce;
    }
    public void decrementBidZeroDebounce() {
        if (bidZeroDebounce > 0) {
            bidZeroDebounce--;
        }
    }

    void initCurrentAdjData(positionAdjustment initMe) {
        positionAdjustment prevAdj = new positionAdjustment(true);
        if (adjVecIdx == 0) {

            initMe.balance = this.posBalance;
            initMe.delta = this.shortDelta;
            initMe.longPrice = this.longEntryPrice;
            initMe.sharesLong = this.longShares;
            initMe.sharesShort = this.shortShares;
            initMe.shortPrice = this.shortEntryPrice;
        } else if (adjVecIdx > 0) {
            prevAdj = this.posAdjGet(adjVecIdx - 1);
            initMe.balance = prevAdj.balance;
            initMe.delta = prevAdj.delta;
            initMe.longPrice = prevAdj.longPrice;
            initMe.sharesLong = prevAdj.sharesLong;
            initMe.sharesShort = prevAdj.sharesShort;
            initMe.shortPrice = prevAdj.shortPrice;
        } else {
            initMe.balance = 0;
            initMe.delta = 0;
            initMe.longPrice = 0;
            initMe.sharesLong = 0;
            initMe.sharesShort = 0;
            initMe.shortPrice = 0;

        }
    }
    
public void saveCurrentState() {
    prevState = currentState;
}
public void restoreState() {
    currentState = prevState;
}
public final static class positionStates
{
    private static int enumCount = 1;
    private int enumVal;
    private String name;

    private positionStates( String str )
    {
      name = str;
      enumVal = enumCount;
      enumCount++;
    }
    
    public String toString() { return name; }
    public int toInt() { return enumVal; }
    
    public static positionStates getStateFromOrd(int ordin) {
        if (ordin < STATES.length) {
            return(STATES[ordin]);
        }
        return null;
    }
    
    public static final positionStates INACTIVE = new positionStates("INACTIVE");
    public static final positionStates ACTIVE = new positionStates("ACTIVE");
    public static final positionStates ADJUST = new positionStates("ADJUST");
    public static final positionStates FILL = new positionStates("FILL");
    public static final positionStates CLOSED = new positionStates("CLOSED");
    
    public static final positionStates[] STATES = 
        {
            null, /* first is empty, so null it.  */
            INACTIVE, ACTIVE, ADJUST, FILL, CLOSED
            
        };
    
    
} /* PositionStates */ 

public void setClosed(boolean cl) {
    closed = cl;
}
public boolean isClosed() {
    return(closed);
}    
    
}