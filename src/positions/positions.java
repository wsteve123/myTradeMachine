/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import positions.positionData.positionStates;


/**
 *
 * @author walterstevenson
 */
public class positions implements Serializable {

    private Vector posVec;
    private Vector posVecRead;
    private int posCnt;
    private int fetch = 0;
    private String posDataObjFile = "posdataobj.dat";
    private positions actPositions;
    private Semaphore positionSem;
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    /* these can be used as global gain/loss control to all positions */
    /* temporary test boolean of gain/loss feature. false turns it off, true on.*/
    private boolean testMode = false;
    private boolean gainLossControl = false;
    private float gainLock = 8;
    private float stopLoss = 4;
    private String positionFileName = null;
    public volatilityMonitor defVm;
    public volatilityMonitorManager vmManager = null;

    public volatilityMonitorManager getVmManager() {
        return vmManager;
    }
    public void setDefVm(volatilityMonitor def){
        defVm = def;
    }
    public volatilityMonitor getDefVm(){
        return defVm;
    }
    public void resetVm() {

        actPositions.setDefVm(new volatilityMonitor(actPositions));
    }
    /* globally controls the position loopback. Used to test
     * a position(s) by not allowing the actual trade to go out, instead
     * just "loops" back as if the trade happen. When this bit gets set, all 
     * position level loopback bits get set. When this one gets cleared, then
     * the position level bits control each position.
     */
    private boolean tradeLoopedback = true;
    private String accountNumber = "New";
    public boolean getGainLossControl() {
        return(gainLossControl);
    }
    public float getGainLockValue() {
        return(gainLock);
    }
    public float getStopLossValue() {
        return(stopLoss);
    }
    
    public boolean areWeInTestMode() {
        return (testMode);
    }
    boolean posDataEmpty() {
        return (posVec.size() == 0);
    }
    public int getNumberOfPositions(){
        return posCnt;
    }
    public positions() {
        posVec = new Vector(10, 2);
        posVecRead = new Vector(10, 2);
        posCnt = 0;
        fetch = 0;
        actPositions = this;
        positionSem = new Semaphore(1);
        tradeLoopedback = true;
        gainLossControl = false;
        /* set to program account# */
        //accountNumber = "756461494";
        //added 8/5/12 implementing real account numbers...
        accountNumber = "New";
        vmManager = new volatilityMonitorManager(actPositions);
    }

    public void setAccountNumber(String accIn) {
        accountNumber = accIn;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setTradeLoopback(boolean lpin) {
        tradeLoopedback = lpin;
    }
    public boolean getTradeLoopback() {
        return tradeLoopedback;
    }
    public void semTake() {
        positionSem.acquire();
    }
    public void semGive() {
        positionSem.release();
    }
    
    public void posDataVecInit() {
        posVec.clear();
        posCnt = 0;
        fetch = 0;
    }

    public int posDataVecSize() {
        return (posVec.size());
    }

    public void posDataStore(positionData dp) {
        posVec.add(dp);
        dp.setPosVecIdx(posCnt);
        dp.setPosId(posCnt);
        posCnt++;
    }

    void posDataReplace(positionData pd, int location) {
        if(location < posVec.size()) {
            posVec.setElementAt(pd, location);
        }
    }

    public positionData posDataFetchNext(boolean newFetch) {
        if (newFetch == true) {
            fetch = 0;
        }

        if (fetch < posVec.size()) {
            return ((positionData) posVec.elementAt(fetch++));
        } else {
            fetch = 0;
            return (null);
        }

    }

    public void posDataDelete(positionData dp) {
        this.semTake();
        posVec.removeElementAt(dp.getPosVecIdx());
        this.semGive();
    }

   public  void posDataCloseout(positionData dp) {
        /* Here we close out the position by basically
         * just selling all, both longs and shorts.
         * Mark it closed and don't let future adjustments occur.wfs
         */
    }

    public void posDataDeleteAll() {
        this.semTake();
        posDataVecInit();
        this.semGive();
    }

    public void posDataDelete(boolean all) {
    }

   public  boolean posDataSearch(int posId) {
        positionData actPos;
        this.semTake();
        for (int lp = 0; lp < posVec.size(); lp++) {
            actPos = (positionData) posVec.elementAt(lp);
            if (posId == actPos.getPosId()) {
                this.semGive();
                return true;
            }
        }
        this.semGive();
        return (false);
    }

   public  positionData posDataRetrieve(int posId) {
        positionData actPos;
        this.semTake();
        for (int lp = 0; lp < posVec.size(); lp++) {
            actPos = (positionData) posVec.elementAt(lp);
            if (posId == actPos.getPosId()) {
                this.semGive();
                return actPos;
            }
        }
        this.semGive();
        return (null);
    }

    public positionData posDataSearch(String posTicker) {
        positionData actPos;
        
        if (posTicker == null) {
            return null;
        }
        this.semTake();
        for (int lp = 0; lp < posVec.size(); lp++) {
            actPos = (positionData) posVec.elementAt(lp);
            if (posTicker.compareTo(actPos.longTicker) == 0) {
                this.semGive();
                return actPos;
            }
        }
        this.semGive();
        return (null);
    }

    public int posCompletedCount() {
        int retCnt = 0;
        positionData actPos = new positionData(false);
        for (int lp = 0; lp < posVec.size(); lp++) {
            actPos = (positionData) posVec.elementAt(lp);
            if (actPos.completed == true) {
                retCnt++;
            }
        }
        return (retCnt);
    }

    public String posDataGetUserFileToOpen() {

        JFrame frame = new JFrame();
        JFileChooser fc = new JFileChooser(new File("."));
        File selFile;

        // Show open dialog; this method does not return until the dialog is closed
        fc.showOpenDialog(frame);
        //fc.setCurrentDirectory(".");
        selFile = fc.getSelectedFile();
       
        System.out.println("selfile is:" + selFile);

        return (selFile.getName());

    }

   public  String posDataGetUserFileToSave() {

        JFrame frame = new JFrame();
        JFileChooser fc = new JFileChooser(new File("."));
        File selFile;

        // Show save dialog; this method does not return until the dialog is closed
        fc.showSaveDialog(frame);
        selFile = fc.getSelectedFile();
        System.out.println("selfile is:" + selFile);

        return (selFile.getName());

    }

   public  void posDataReadFromDisk(String fileName, boolean prompt) {

        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        boolean split = false;
        String rdFileName = homeDirectory;
        String tmpStr;
        positionData tmpPos;
        positionAdjustment tmpAdj;
        int numOfAdjustments = 0;
        int numOfGlobalLines = 0;
        /* need to protect */
        String tradingAccount = null;
        this.semTake();
        if (fileName == null) {
            rdFileName += "positions.txt";
        } else {
            rdFileName += fileName;
        }

        posDataVecInit();
        try {
            fis = new FileInputStream(rdFileName);
            dis = new DataInputStream(fis);
            bir = new BufferedReader(new InputStreamReader(fis));
            
            /* adding global params to this file, ones that are not position level 
             * but applies to enture portfilio level 
             * first one we need is the account number this entire file will operate with.
             * account are global to an etire portfilio not changable per position.
             */ 
            if (false) {
                // mark begining in case we need to start over...
                bir.mark(20);
                if (((tmpStr = bir.readLine()) != null)) {
                    if (tmpStr.equals("global")) {
                        if (((tmpStr = bir.readLine()) != null)) {
                            numOfGlobalLines = Integer.parseInt(tmpStr);
                        } else {
                            // reset to begining again, we have no globals...
                            System.out.println("posDataReadFromDisk: Error reading global variable ..");
                            bir = new BufferedReader(new InputStreamReader(fis));

                        }

                    } else {
                        System.out.println("posDataReadFromDisk: global not detected....reset buffer...");
                        // reset to begining again, we have no globals...
                        bir.reset();

                    }
                }
                if (numOfGlobalLines > 0) {
                    System.out.println("posDataReadFromDisk: Global params -> " + numOfGlobalLines);
                    if (((tmpStr = bir.readLine()) != null)) {
                        tradingAccount = tmpStr;
                        System.out.println("posDataReadFromDisk: TradingAccount ->  " + tradingAccount);
                        if (tradingAccount != null) {
                            this.accountNumber = tmpStr;
                        }
                    } else {
                        System.out.println("posDataReadFromDisk: error reading file....");

                    }
                }
            }
            /* read global stuff first, start with portfolio name.
             * these two parameters are written to the file when the portfolio is 
             * created.
             */
            if (((tmpStr = bir.readLine()) != null)) {
                this.positionFileName = tmpStr;
            }
            /* now read the account number associated with this portfolio. */
            if (((tmpStr = bir.readLine()) != null)) {
                this.accountNumber = tmpStr;
            }
            
            while (((tmpStr = bir.readLine()) != null) && (!split)) {
                tmpPos = new positionData(false);
                //read the ticker first.               
                tmpPos.longTicker = tmpStr;
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the shortTicker
                    tmpPos.shortTicker = tmpStr;
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the long entry price
                    tmpPos.longEntryPrice = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the long shares
                    tmpPos.longShares = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the short entry price
                    tmpPos.shortEntryPrice = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the short delta
                    tmpPos.shortDelta = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the short shares
                    tmpPos.shortShares = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the short delta
                    tmpPos.posBalance = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the running balance
                    tmpPos.setRunningBalance(Float.parseFloat(tmpStr));
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the pos date
                    tmpPos.setPosDate(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    /*now read the adjVecIdx, this tells 
                    us how many adjustments to read in for this position
                     */
                    numOfAdjustments = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position's number of shared to start with, both long and short.
                    tmpPos.staLongShares = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position start long shares and short shares
                    tmpPos.staShortShares = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
            
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position gain/loss control boolean
                    tmpPos.gainLossControl = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position gainLock value
                    tmpPos.gainLock = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position stopLoss value
                    tmpPos.stopLoss = Float.parseFloat(tmpStr);
                } else {
                    split = true; 
                }
  
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position loopback state
                    tmpPos.tradeLoopedback = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true; 
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the current position states
                    int tmpOrdinal = Integer.parseInt(tmpStr);
                    tmpPos.currentState = positionStates.getStateFromOrd(tmpOrdinal);
                    
                } else {
                    split = true; 
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the previous position states
                    int tmpOrdinal = Integer.parseInt(tmpStr);
                    tmpPos.prevState = positionStates.getStateFromOrd(tmpOrdinal);
                    
                } else {
                    split = true; 
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position closed boolean
                    tmpPos.closed = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true; 
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the position Risk value for closed positions.
                    tmpPos.posClosedRisk = Float.parseFloat(tmpStr);

                } else {
                    split = true;
                }
/* use this parmChanged to flag when structure parms hare different than file (new vers)
 * The writing to file clears this to false; if true don't read....write portion set it back to false....
 */                
if (tmpPos.parmChanged != true) {                
                /* added 3/2/12 implied volatility averaging in parameters next */
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the ivAveIn value that turns the feature on/off
                    tmpPos.ivAveIn = Boolean.parseBoolean(tmpStr);

                } else {
                    split = true;
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the ivAveInPrecentDrop value that says when we should aveIn
                    tmpPos.ivAveInPrecentDrop = Double.parseDouble(tmpStr);

                } else {
                    split = true;
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the ivAveInMaxAdjustments value that says how many adjustmens we can do
                    tmpPos.ivAveInMaxAdjustments = Integer.parseInt(tmpStr);

                } else {
                    split = true;
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the ivAveInReading value that holds the last IV reading
                    tmpPos.ivAveInReading = Double.parseDouble(tmpStr);

                } else {
                    split = true;
                }
                
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the ivAveInAdjustmentCnt value that holds the number of IV adjustments so far
                    tmpPos.ivAveInAdjustmentCnt = Integer.parseInt(tmpStr);

                } else {
                    split = true;
                }
}                        
                /* read in the volatility monitor values */
                tmpPos.posVm = new volatilityMonitor(actPositions);
                actPositions.setDefVm(tmpPos.getVm());
                //read in adjustments..
                
                while ((numOfAdjustments != 0) && !split) {
                    tmpAdj = new positionAdjustment(false);
                    if ((tmpStr = bir.readLine()) != null) {
                        //now read the balance
                        tmpAdj.balance = Float.parseFloat(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the long delta
                        tmpAdj.delta = Float.parseFloat(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the long price
                        tmpAdj.longPrice = Float.parseFloat(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the shares long
                        tmpAdj.sharesLong = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.sharesShort = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short price
                        tmpAdj.shortPrice = Float.parseFloat(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the adj date
                        tmpAdj.setAdjDate(tmpStr);
                        
                    } else {
                        split = true;
                    //now readin the outcome data for this adjustment
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.BSCount = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.SSCount = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.BLCount = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.SLCount = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.adjustment = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.outcome.outcome = Integer.parseInt(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the short shares
                        tmpAdj.profitLoss = Float.parseFloat(tmpStr);
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the tradeMe boolean
                        tmpAdj.setTradeNeeded(Boolean.parseBoolean(tmpStr));
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the pendingTrade boolean
                        tmpAdj.setTradePending(Boolean.parseBoolean(tmpStr));
                    } else {
                        split = true;
                    }

                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the pendingTrade boolean
                        tmpAdj.setOptionMarketOrder(Boolean.parseBoolean(tmpStr));
                    } else {
                        split = true;
                    }

                    numOfAdjustments--;
                    tmpPos.posAdjAdd(tmpAdj);
                }
                posDataStore(tmpPos);
            }
            if(prompt == true) {
                commonGui.prMsg(posVec.size() + " Position(s) Read from File: " + rdFileName);
            }
            bir.close();

        } catch (Exception e) {
            System.out.println("error reading text for: " + rdFileName + "(" + e + ").");
            System.out.println("homeDir:" + homeDirectory+rdFileName);
        }
        /* give back key */
        this.semGive();
    } /* posDataReadFromDisk */

    void posDataVecCopy(Vector dst, Vector src) throws Exception {

//            dst = (Vector) ObjectCloner.deepCopy((Vector)src);
        positionData tPos = new positionData(false);

        dst.clear();
        for (int lp = 0; lp < src.size(); lp++) {
            tPos = (positionData) src.elementAt(lp);
            dst.add((positionData) tPos);
        }
    }

    public void posDataSaveToDisk(String fileName, boolean prompt) {
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        boolean split = false;
        String wrFileName = homeDirectory;
        String tmpStr;
        positionData tmpPos;
        positionAdjustment tmpAdj;
        int numOfAdjustments = 0;
        int numOfPositions = 0;
        int adjIx = 0;
        /* need to protect data fetches */
        this.semTake();
        
        if (fileName == null) {
            wrFileName += "positions.txt";
        } else {
            wrFileName += fileName;
        }

        try {
            fos = new FileOutputStream(wrFileName);
            dos = new DataOutputStream(fos);
            bow = new BufferedWriter(new OutputStreamWriter(fos));
            //init to fetch from begining..and get the first.
            tmpPos = this.posDataFetchNext(true);
            numOfPositions = this.posCnt;

            tmpAdj = new positionAdjustment(false);
            
            /*
             * added 8/5/12 added associated trading account number to store in
             * this file.
             */
            if (false) {
                if (this.accountNumber != null) {
                    bow.write("global");
                    bow.newLine();
                    bow.write("1");
                    bow.newLine();
                    bow.write(this.accountNumber);
                    bow.newLine();

                }
            }
            /* write the global stuff first portfolio name and account number */
            if (this.positionFileName != null) {
                bow.write(this.positionFileName);
                bow.newLine();
            }
            if (this.accountNumber != null) {
                bow.write(this.accountNumber);
                bow.newLine();
            }
            
            while ((numOfPositions > 0) && (tmpPos != null) && (!split)) {               

                bow.write(tmpPos.longTicker);
                bow.newLine();
                bow.write(tmpPos.shortTicker);
                bow.newLine();
                bow.write(Float.toString(tmpPos.longEntryPrice));
                bow.newLine();
                bow.write(Integer.toString(tmpPos.longShares));
                bow.newLine();
                bow.write(Float.toString(tmpPos.shortEntryPrice));
                bow.newLine();
                bow.write(Float.toString(tmpPos.shortDelta));
                bow.newLine();
                bow.write(Integer.toString(tmpPos.shortShares));
                bow.newLine();
                bow.write(Float.toString(tmpPos.posBalance));
                bow.newLine();
                bow.write(Float.toString(tmpPos.getRunningBalance()));
                bow.newLine();
                bow.write(tmpPos.getPosDateStr());
                bow.newLine();
                bow.write(Integer.toString(tmpPos.posAdjIdxGet()));
                bow.newLine();
                bow.write(Integer.toString(tmpPos.staLongShares));
                bow.newLine();
                bow.write(Integer.toString(tmpPos.staShortShares));
                bow.newLine();
          
                bow.write(Boolean.toString(tmpPos.gainLossControl));
                bow.newLine();
                bow.write(Float.toString(tmpPos.gainLock));
                bow.newLine();
                bow.write(Float.toString(tmpPos.stopLoss));
                bow.newLine();

                bow.write(Boolean.toString(tmpPos.tradeLoopedback));
                bow.newLine();
                // write the current state 
                bow.write(Integer.toString(tmpPos.currentState.toInt()));
                bow.newLine();
                // write the previous state 
                bow.write(Integer.toString(tmpPos.prevState.toInt()));
                bow.newLine();
                // write the closed boolean 
                bow.write(Boolean.toString(tmpPos.closed));
                bow.newLine();
                // write the closed position risk value 
                bow.write(Float.toString(tmpPos.posClosedRisk));
                bow.newLine();
                
                /* added 3/2/12 implied volatility averaging in parameters next */
                // write the ivAveIn value that turns the feature on/off 
                bow.write(Boolean.toString(tmpPos.ivAveIn));
                bow.newLine();
                
                // write the ivAveInPrecentDrop value that says when we should aveIn 
                bow.write(Double.toString(tmpPos.ivAveInPrecentDrop));
                bow.newLine();
                // write the ivAveInMaxAdjustments value that says how many adjustmens we can do
                bow.write(Integer.toString(tmpPos.ivAveInMaxAdjustments));
                bow.newLine();
                // write the ivAveInReading value that holds the last IV reading
                bow.write(Double.toString(tmpPos.ivAveInReading));
                bow.newLine();
                // write the ivAveInAdjustmentCnt value that holds the number of IV adjustments so far
                bow.write(Integer.toString(tmpPos.ivAveInAdjustmentCnt));
                bow.newLine();
/* this bool is read when reading params and will be set true when structure vers is different than file */                
tmpPos.parmChanged = false;                
                
                //check if we have adjustments to write for this position..

                numOfAdjustments = tmpPos.posAdjustments();
                adjIx = 0;
                while (numOfAdjustments > 0) {
                    tmpAdj = tmpPos.posAdjGet(adjIx++);
                    //wfstmpAdj.outcome.
                    numOfAdjustments--;
                    //process this adjustment, write it to file..
                    bow.write(Float.toString(tmpAdj.balance));
                    bow.newLine();
                    bow.write(Float.toString(tmpAdj.delta));
                    bow.newLine();
                    bow.write(Float.toString(tmpAdj.longPrice));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.sharesLong));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.sharesShort));
                    bow.newLine();
                    bow.write(Float.toString(tmpAdj.shortPrice));
                    bow.newLine();
                    bow.write(tmpAdj.getAdjDateStr());
                    bow.newLine();
                    //do the outcome data for this adjustment.
                    bow.write(Integer.toString(tmpAdj.outcome.BSCount));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.outcome.SSCount));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.outcome.BLCount));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.outcome.SLCount));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.outcome.adjustment));
                    bow.newLine();
                    bow.write(Integer.toString(tmpAdj.outcome.outcome));
                    bow.newLine();
                    bow.write(Float.toString(tmpAdj.profitLoss));
                    bow.newLine();
                    bow.write(Boolean.toString(tmpAdj.getTradeNeeded()));
                    bow.newLine();
                    bow.write(Boolean.toString(tmpAdj.getTradePending()));
                    bow.newLine();
                    bow.write(Boolean.toString(tmpAdj.isItOptionMarketOrder()));
                    bow.newLine();
                }
                numOfPositions--;
                if (numOfPositions > 0) {
                    tmpPos = this.posDataFetchNext(false);
                }
            }
            bow.close();
            if(prompt == true) {
                commonGui.prMsg(posVec.size() + " Position(s) Saved to File: " + wrFileName);
                
            }
            System.out.println("Position(s) Saved to file:"+wrFileName);

        } catch (Exception e) {
            System.out.println("error writing text to: " + wrFileName + "(" + e + ").");
        }
        /* give back key */
        this.semGive();

    }
    public void dispAllPositions(){
        new dispPosDetail("All Position Detail");
        
    }
    private class dispPosDetail extends JFrame {


        private dispPosDetail(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    // dispose();
                    // System.exit( 0 );
                }
            });
            initTable(actPositions);
            pack();
            setVisible(true);
        }

        private void initTable(positions allPositions) {

            String[] columns = {
                "PosId", "LSym", "SSym", "LPrice", "SPrice", "LShares", "SContracts", "SDelta", "Balance", "P/L", "Created"
            };
            String[][] dataArr = new String[400][11];
            positionAdjustment actAdj;
            int posNum;
            //posData actPos = new posData();
            positionData actPos = null;
            int numOfPositions = allPositions.posDataVecSize();
            int numOfAdjustments;
            int rowNum = 0;
            
            /* protection needed here, take the key. */
            allPositions.semTake();
            for (posNum = 0  , actPos = allPositions.posDataFetchNext(true); posNum < numOfPositions;) {
                numOfAdjustments = actPos.posAdjustments();

                dataArr[rowNum][0] = Integer.toString(actPos.getPosId() + 1);
                dataArr[rowNum][1] = actPos.longTicker;
                dataArr[rowNum][2] = actPos.shortTicker;
                dataArr[rowNum][3] = Float.toString(actPos.longEntryPrice);
                dataArr[rowNum][4] = Float.toString(actPos.shortEntryPrice);
                dataArr[rowNum][5] = Float.toString(actPos.longShares);
                dataArr[rowNum][6] = Float.toString(actPos.shortShares);
                dataArr[rowNum][7] = Float.toString(actPos.shortDelta);
                dataArr[rowNum][8] = Float.toString(actPos.posBalance);
                dataArr[rowNum][9] = Float.toString((float) 0.0);
                dataArr[rowNum][10] = actPos.getPosDateStr();
                for (int line = 1; line < (numOfAdjustments + 1); line++) {
                    // get active Trade Obj to work on.
                    actAdj = (positionAdjustment) actPos.posAdjGet(line - 1);
                    dataArr[rowNum + line][0] = Integer.toString(actPos.getPosId() + 1) + "." + Integer.toString(line);
                    dataArr[rowNum + line][1] = "-->";
                    dataArr[rowNum + line][2] = "-->";
                    dataArr[rowNum + line][3] = Float.toString(actAdj.longPrice);
                    dataArr[rowNum + line][4] = Float.toString(actAdj.shortPrice);

                    dataArr[rowNum + line][5] = Float.toString(actAdj.sharesLong) + actAdj.outcome.getOutcomeLongStr();
                    dataArr[rowNum + line][6] = Float.toString(actAdj.sharesShort) + actAdj.outcome.getOutcomeShortStr();

                    dataArr[rowNum + line][7] = Float.toString(actAdj.delta);
                    dataArr[rowNum + line][8] = Float.toString(actAdj.balance);
                    dataArr[rowNum + line][9] = Float.toString(Math.round(actAdj.profitLoss));
                    dataArr[rowNum + line][10] = actAdj.getAdjDateStr();
                }
                rowNum = ((rowNum) + (numOfAdjustments) + 1);
                if (++posNum < numOfPositions) {
                    actPos = allPositions.posDataFetchNext(false);
                    numOfAdjustments = actPos.posAdjustments();

                }
            }
            /* give back key, we're done. */
            allPositions.semGive();
            JTable jt = new JTable(dataArr, columns);
            JScrollPane pane = new JScrollPane(jt);
            getContentPane().add(pane);
        }
    }

    public void deletePos() {
        String actPosStr;
        positionData actPosition;
        if (actPositions.posDataEmpty() == false) {
                actPosStr = commonGui.getUserInput("Enter Long Ticker Of Position to Delete","csco");
                if ((actPosition = actPositions.posDataSearch(actPosStr)) != null) {
                    actPositions.posDataDelete(actPosition);
                    commonGui.prMsg("Delete Completed.");
                }
                else {
                    commonGui.prMsg("position not found.");    
                }
            }
            else {
                commonGui.prMsg("There are no positions to Delete.");
            }    
    }
    
    
    
    public void deletePos(boolean all) {
        int deleteIt;
        if (actPositions.posDataEmpty() == false) {
            if ((deleteIt = commonGui.postConfirmationMsg("Are You Sure You Want To Delete All Positions?")) == 0) {
                actPositions.posDataDeleteAll();
                commonGui.prMsg("All Positions Deleted.");
            } else {
                commonGui.prMsg("Positions Not Deleted.");
            }

        } else {
            commonGui.prMsg("There are no positions to Delete.");
        }
    }
    
    public void deleteAdjustment() {
        String actPosStr;
        String actAdjNumStr;
        int actAdjNum;
        positionData actPosition;
        if (actPositions.posDataEmpty() == false) {
                actPosStr = commonGui.getUserInput("Enter Long Ticker Of Position Adjustment to Delete","csco");
                if ((actPosition = actPositions.posDataSearch(actPosStr)) != null) {
                    actAdjNumStr = commonGui.getUserInput("Enter Adjustment ID to Delete","1");
                    actAdjNum = Integer.parseInt(actAdjNumStr);
                    if (actPosition.posAdjExists(actAdjNum) == true){
                        actPosition.posAdjClearOne(actPosition, actAdjNum);
                        commonGui.prMsg("Delete Completed.");
                    }
                    else {
                        commonGui.prMsg("adjustment not found.");     
                    }
                }
                else {
                    commonGui.prMsg("position not found.");    
                }
            }
            else {
                commonGui.prMsg("There are no positions to Delete Adjustments of.");
            }    
    }
    
    
    public void saveAllPositionsToDisk() {
        int saveIt;

        if (actPositions.posDataEmpty() == false) {
            if ((saveIt = commonGui.postConfirmationMsg("Save All Positions To Disk?")) == 0) {
                String fts = actPositions.posDataGetUserFileToSave();
                
                actPositions.posDataSaveToDisk(fts, true /* prompt ? */);

            } else {
                commonGui.prMsg("Positions Not Saved.");
            }

        } else {
            commonGui.prMsg("There are no positions to Save.");
        }
    }

    public void readAllPositionsFromDisk() {
        int readIt;

        if ((readIt = commonGui.postConfirmationMsg("Read All Positions From Disk?")) == 0) {
            String fts = actPositions.posDataGetUserFileToOpen();
            
            actPositions.positionFileName = fts.substring(0, fts.indexOf("."));
            System.out.println("readAllPositionsFromDisk: positionFileName: "+actPositions.positionFileName);
            actPositions.posDataReadFromDisk(fts, true /* prompt ? */);

        } else {
            commonGui.prMsg("Positions Not Read.");
        }


    }
    public void setPositionFileName(String setto) {
        positionFileName = setto;
    }
    public String getPositionFileName() {
        return(actPositions.positionFileName);
    }
    public void askForPositionFileName() {
            BufferedWriter bow = null;
            String tmpStr;
            actPositions.positionFileName = tmpStr = commonGui.getUserInput("Enter Position Filename", actPositions.positionFileName);
            System.out.println("creating file "+tmpStr+".dat as position filename.");
            try {
                
                bow = new BufferedWriter(new FileWriter(tmpStr+".dat", true /*append*/));

            } catch (Exception e) {
                System.out.println("error writing text to: " + tmpStr + "(" + e + ").");
            }
            
            try {
                bow.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
             

    }
    
    public void printableSummary() {
        positionData actPos;
        int numOfPositions;
        int numOfAdjustments;
        int actAdjNum;
        positionAdjustment actAdj;
        float moneyUsed;
        float moneyAtWork = (float)0;
        String stateStr;
        String closedDate;
        int profit;
        float ivRaDFirst = 0;
        float ivRaDLast = 0;
        runningAverage actAve;
        volatilityMonitor actVm;
        String prString;
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        String wrFileName = homeDirectory;
        
        
        this.getVmManager().readFromDisk(this.getPositionFileName());
        
        this.semTake();
        System.out.println(
                "\n\n                    Printable Summary for " + homeDirectory + this.getPositionFileName() + "\n"
                );
        prString = String.format(
                "\n\n                    Printable Summary for " + homeDirectory + this.getPositionFileName() + "\n"            
                );
        System.out.format(
                            "%12s %12s %12s %12s %15s %15s %10s %10s %8s %8s %8s", 
                            "Position", "State", "Gain/Loss", "Gain/Loss(%)", "Adjustments", 
                            "MoneyAtWork", "DateIn", "DateOut", "IVin(%)", "IVout(%)", "IVChange\n"
                );
        prString += String.format(
                            "%12s %12s %12s %12s %15s %15s %10s %10s %8s %8s %8s", 
                            "Position", "State", "Gain/Loss", "Gain/Loss(%)", "Adjustments", 
                            "MoneyAtWork", "DateIn", "DateOut", "IVin(%)", "IVout(%)", "IVChange\n"
                );
        
        actPos = this.posDataFetchNext(true);
        numOfPositions = this.posCnt;

        //    tmpAdj = new positionAdjustment(false);
         
        while ((numOfPositions > 0) && (actPos != null)) {
            actVm = actPos.getVm();
            if (actVm.monitorDays.size() > 0) {
                actAve = actVm.volatilityGetRunningAve(0);
                ivRaDFirst = (float)myUtils.roundMe(actAve.runningAveGet(),4);
                actAve = actVm.volatilityGetRunningAve(actVm.monitorDays.size() - 1);
                ivRaDLast = (float)myUtils.roundMe(actAve.runningAveGet(), 4);
            }
            stateStr = (actPos.closed==true)?"Closed":"Open";
            
            numOfAdjustments = actPos.posAdjustments();
            if (actPos.closed == true) {
                actAdj = (positionAdjustment) actPos.posAdjGet(numOfAdjustments - 3);
            } else {
                actAdj = (positionAdjustment) actPos.posAdjGet(numOfAdjustments - 1);
            }
            
            moneyUsed = (actPos.posBalance - actAdj.balance);
            if (actPos.closed != true) {
                moneyAtWork += moneyUsed;
            }
            actAdj = (positionAdjustment) actPos.posAdjGet(numOfAdjustments - 1);
            closedDate = (actPos.closed==true)?actAdj.getAdjDateStr():"-";
            profit = Math.round(actAdj.profitLoss);
            System.out.format(
                    "\n%12s %12s %12d %12f %15d %15d %10s %10s %8f %8f %8f", 
                    actPos.longTicker, stateStr, profit, myUtils.roundMe((double)(profit / moneyUsed) * 100, 3), numOfAdjustments, 
                    Math.round(moneyUsed), actPos.getPosDateStr(), closedDate, ivRaDFirst * 100, 
                    ivRaDLast *100, myUtils.roundMe((((ivRaDLast - ivRaDFirst) / ivRaDFirst) * 100), 3)
                    );
            
            prString += String.format("\n%12s %12s %12d %12f %15d %15d %10s %10s %8f %8f %8f", 
                    actPos.longTicker, stateStr, profit, myUtils.roundMe((double)(profit / moneyUsed) * 100, 3), numOfAdjustments, 
                    Math.round(moneyUsed), actPos.getPosDateStr(), closedDate, ivRaDFirst * 100, 
                    ivRaDLast *100, myUtils.roundMe((((ivRaDLast - ivRaDFirst) / ivRaDFirst) * 100), 3));
            
            
            for (actAdjNum = 1; actAdjNum < (numOfAdjustments + 1); actAdjNum++) {
                actAdj = (positionAdjustment) actPos.posAdjGet(actAdjNum - 1);
                
            }
            
            
            numOfPositions--;
                if (numOfPositions > 0) {
                    actPos = this.posDataFetchNext(false);
                }
        }
        System.out.format("\n%s", "Money At Work:");
        prString += String.format("\n%s", "Money At Work:");
        System.out.format("\n%12f", myUtils.roundMe( moneyAtWork, 3));
        prString += String.format("\n%12f", myUtils.roundMe(moneyAtWork, 3));
        
        wrFileName += this.getPositionFileName() + ".print";
        System.out.println("\n\nprinting out to file -> " + wrFileName);
        try {
            fos = new FileOutputStream(wrFileName);
            dos = new DataOutputStream(fos);
            bow = new BufferedWriter(new OutputStreamWriter(fos));
            
            bow.write(prString);
            bow.newLine();           
            bow.close();
        } catch (Exception e) {
            System.out.println("error writing text to: " + wrFileName + "(" + e + ").");
        } 
        
       // commonGui.postInformationMsg(prString);
        System.out.format("%s","\n");

        /* give back key */
        this.semGive();

    }

}
    