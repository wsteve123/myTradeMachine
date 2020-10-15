
package tradeMenus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import tradeMenus.displayStocksWthFilter.SlopesFound;
import positions.myUtils;
import positions.runningAverage;
import java.text.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author earlie87
 */
public class displaySlopeDialogForm extends javax.swing.JDialog {

    /**
     * Creates new form displaySlopeDialogForm
     */
    boolean dispIt = true;
    dispSlopeJob actDispJob;
    boolean userDisplayNext = true;
    public int slopeFileCnt = 0;
    public void setWrFileCnt(int wc){
        slopeFileCnt = wc;
    }
    
    public displaySlopeDialogForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        actDispJob = new dispSlopeJob();
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });
        
        
    }
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    public TotalStats stats = new TotalStats();
    public class TotalStats {
        final int CURRENT = 0;
        final int RUNNING = 1;
        public String exchange = null;
        public int totalNumberOfTickers = 0;
        public int pointToPointSpread = 5;
        public String date = null;
        //current to today in $ amounts
        public double currentTotalProfit = 0;
        public double currentTotalLoss = 0;
        public double currentTotalPercent = 0;
        public double currentTotalPl = 0.0;
        //maximum running totals i.e maximum running gains through out year in $$
        public double runningTotalProfit = 0;
        public double runningTotalLoss = 0;
        public double runningTotalPercent = 0;
        public double runningTotalPl = 0.0;
        public double totalCostBasis = 0.0;
        //current to today counts amounts
        public int currentTotalProfitCnt = 0;
        public int currentTotalLossCnt = 0;
        public int currentTotalPercentCnt = 0;
        //maximum running totals i.e maximum running gains through out year in $$
        public int runningTotalProfitCnt = 0;
        public int runningTotalLossCnt = 0;
        public int runningTotalPercentCnt = 0;
        public int runningTotalZeroLossCnt = 0;
        public int runningTotalZeroGainCnt = 0;
        public int runningLossZero = 0;
        public double maximumRunningLoss = 0.0;
        public double maximumRunningProfit = 0.0;
        public double maximumCurrentLoss = 0.0;
        public double maximumCurrentProfit = 0.0;
        public double runningLossAve = 0.0;
        public int trendUpCnt = 0;
        public int trendDnCnt = 0;
        public int trendNowUpCnt = 0;
        public int trendNowDnCnt = 0;
        double runningLoss = 0.0;
        public double runningProfitAve = 0.0;
        double runningProfit = 0.0;
        //current to today p/l maps in percentages..
        //profit map, profit >= 0
        public int c_profit_00_to_05 = 0; // count 0 - 5  i.e 0.4%, 4.99%,  5.00% goes here.
        public int c_profit_05_to_10 = 0; // count 5 - 10 i.e 5.1%, 9.99%, 10.00% goes here.
        public int c_profit_10_to_15 = 0;
        public int c_profit_15_to_20 = 0;
        public int c_profit_20_to_25 = 0;
        public int c_profit_25_to_30 = 0;
        public int c_profit_30_to_35 = 0;
        public int c_profit_35_to_40 = 0;
        public int c_profit_40_to_45 = 0;
        public int c_profit_45_to_50 = 0;
        public int c_profit_50_to_55 = 0;
        public int c_profit_55_to_60 = 0;
        public int c_profit_60_above = 0;
         //Loss map, profit < 0
        public int c_loss_00_to_05 = 0;  // count -0 to -5  i.e -0.4%, -4.99%,  -5.00% goes here.
        public int c_loss_05_to_10 = 0;  // count -5 to -10 i.e -5.1%, -9.99%, -10.00% goes here.
        public int c_loss_10_to_15 = 0;
        public int c_loss_15_to_20 = 0;
        public int c_loss_20_to_25 = 0;
        public int c_loss_25_to_30 = 0;
        public int c_loss_30_to_35 = 0;
        public int c_loss_35_to_40 = 0;
        public int c_loss_40_to_45 = 0;
        public int c_loss_45_to_50 = 0;
        public int c_loss_50_to_55 = 0;
        public int c_loss_55_to_60 = 0;
        public int c_loss_60_above = 0;
        //Max Running p/l throughout year maps in percentages..
        //Max Running profit map, profit >= 0
        public int r_profit_00_to_05 = 0;  // count 0 - 5  i.e 0.4%, 4.99%,  5.00% goes here.
        public int r_profit_05_to_10 = 0;  // count 5 - 10 i.e 5.1%, 9.99%, 10.00% goes here.
        public int r_profit_10_to_15 = 0;
        public int r_profit_15_to_20 = 0;
        public int r_profit_20_to_25 = 0;
        public int r_profit_25_to_30 = 0;
        public int r_profit_30_to_35 = 0;
        public int r_profit_35_to_40 = 0;
        public int r_profit_40_to_45 = 0;
        public int r_profit_45_to_50 = 0;
        public int r_profit_50_to_55 = 0;
        public int r_profit_55_to_60 = 0;
        public int r_profit_60_above = 0;
        //Loss map, profit < 0
        public int r_loss_00_to_05 = 0;  // count -0 to -5  i.e -0.4%, -4.99%,  -5.00% goes here.
        public int r_loss_05_to_10 = 0;  // count -5 to -10 i.e -5.1%, -9.99%, -10.00% goes here.
        public int r_loss_10_to_15 = 0;
        public int r_loss_15_to_20 = 0;
        public int r_loss_20_to_25 = 0;
        public int r_loss_25_to_30 = 0;
        public int r_loss_30_to_35 = 0;
        public int r_loss_35_to_40 = 0;
        public int r_loss_40_to_45 = 0;
        public int r_loss_45_to_50 = 0;
        public int r_loss_50_to_55 = 0;
        public int r_loss_55_to_60 = 0;
        public int r_loss_60_above = 0;
        //public ioWrTextFiles wrFile = new ioWrTextFiles("slope." + exchangeStr + "." + slopeFileCnt + ".txt", false);
        public String getTodaysDate(){
            String todaysDate;
            todaysDate = myUtils.GetTodaysDate("YYY/MM/DD");
            todaysDate = myUtils.reverseDate(todaysDate);
            return(todaysDate);
        }
        public void update(SlopesFound slopeIn, int actSlope){
            double curProfit = 0.0;
            double profit = 0.0;
            displayStocksWthFilter.SlopeStructure thisSlope = actSlopesList.Slopes[actSlope];
            double maxRunningProfit = 0.0;
            double maxRunningLoss = 0.0;
            if (thisSlope == null){
                //nothing to see here go back..
                return;
            }
            //keep adding to number of tickers for total..
            totalNumberOfTickers++;
            //if position is open, need to get open value and percent pl...
            if (thisSlope.pl.isPosOpen() == true) {
                curProfit = myUtils.roundMe(thisSlope.pl.runningPl + thisSlope.pl.getCurrentPosValue(), 2);
                profit = myUtils.roundMe(thisSlope.pl.runningPl, 2);
            } else {
                curProfit = myUtils.roundMe(thisSlope.pl.runningPl, 2);
                profit = curProfit;
            }
            currentTotalPl += curProfit;
            totalCostBasis += thisSlope.pl.costBasis;
            if (curProfit >= 0.0){
                currentTotalProfit += curProfit;
                currentTotalProfitCnt++;
            }else{
                currentTotalLoss += curProfit;
                currentTotalLossCnt++;
            }
            maxRunningProfit = myUtils.roundMe(thisSlope.pl.maxRunningProfit, 2);
            runningTotalPl += maxRunningProfit;
            if (maxRunningProfit >= 0.0) {
                runningTotalProfit += maxRunningProfit;
                runningTotalProfitCnt++;
            }
            maxRunningLoss = myUtils.roundMe(thisSlope.pl.maxRunningLoss, 2);
            
            if (maxRunningLoss < 0.0){
                runningTotalLoss += maxRunningLoss;
                runningTotalLossCnt++;
            }else if (maxRunningLoss == 0.0){
                    runningLossZero++;
            }
            // keep max running values of loss and gain
            if (thisSlope.maxRunningLossPercent < maximumRunningLoss){
                maximumRunningLoss = thisSlope.maxRunningLossPercent;
            }
            if (thisSlope.maxRunningProfitPercent > maximumRunningProfit){
                maximumRunningProfit = thisSlope.maxRunningProfitPercent;
            }
            // keep max current values of loss and gain
            if ((thisSlope.overAllPl < 0) && (thisSlope.overAllPl < maximumCurrentLoss)){
                maximumCurrentLoss = thisSlope.overAllPl;
            }else if ((thisSlope.overAllPl > 0) && (thisSlope.overAllPl > maximumCurrentProfit)) {
                maximumCurrentProfit = thisSlope.overAllPl;    
            }
            
            
            //keep running max loss to calc ave loss.
            runningLoss += thisSlope.maxRunningLossPercent;
            // zero makes divide blowup..
            if (runningLoss > 0){
                runningLossAve = myUtils.roundMe(((runningLoss / (double)runningTotalLossCnt)), 2);
            }else{
                runningLossAve = 0;
            }
            //keep running max profit to calc ave profit.
            runningProfit += thisSlope.maxRunningProfitPercent;
            if (runningProfit > 0){
                runningProfitAve = myUtils.roundMe(((runningProfit / (double)runningTotalProfitCnt)), 2);
            }else{
                runningProfitAve = 0;
            }
            if (thisSlope.pl.isPosOpen() == true){
                mapPercentByCount(thisSlope.overAllPlIncludeOpen, CURRENT);
            }else{
                mapPercentByCount(thisSlope.overAllPl, CURRENT);    
            }  
            currentTotalPercent = myUtils.roundMe(((currentTotalPl / totalCostBasis) * 100),2);
            runningTotalPercent = myUtils.roundMe(((runningTotalPl / totalCostBasis) * 100),2);
            mapPercentByCount(thisSlope.maxRunningProfitPercent, RUNNING);
            mapPercentByCount(thisSlope.maxRunningLossPercent, RUNNING); 
            if (thisSlope.currentTrendIs.equals(slopeDefs.TREND_UP)){
                trendUpCnt++;
                if (thisSlope.currentChangeInDirection  == true) {
                    //trend is up and we just changed to this ..
                    trendNowUpCnt++;
                }else{
                    
                }
            }else if(thisSlope.currentTrendIs.equals(slopeDefs.TREND_DN)){
                trendDnCnt++;
                if (thisSlope.currentChangeInDirection  == true) {
                    //trend is down and we just changed to this ..
                    trendNowDnCnt++;
                }else{
                    
                }
            }
        }
        public void mapPercentByCount(double plPercent, int whichOne){
            
            if(whichOne == CURRENT){
                //count the profits
                if ((plPercent >= 0.0) && (plPercent <= 5.0)) {
                    c_profit_00_to_05++;
                }else if ((plPercent > 5.0) && (plPercent <= 10.0)) {
                    c_profit_05_to_10++;
                }else if ((plPercent > 10.0) && (plPercent <= 15.0)) {
                    c_profit_10_to_15++;
                }else if ((plPercent > 15.0) && (plPercent <= 20.0)) {
                    c_profit_15_to_20++;
                }else if ((plPercent > 20.0) && (plPercent <= 25.0)) {
                    c_profit_20_to_25++;
                }else if ((plPercent > 25.0) && (plPercent <= 30.0)) {
                    c_profit_25_to_30++;
                }else if ((plPercent > 30.0) && (plPercent <= 35.0)) {
                    c_profit_30_to_35++;
                }else if ((plPercent > 35.0) && (plPercent <= 40.0)) {
                    c_profit_35_to_40++;
                }else if ((plPercent > 40.0) && (plPercent <= 45.0)) {
                    c_profit_40_to_45++;
                }else if ((plPercent > 45.0) && (plPercent <= 50.0)) {
                    c_profit_45_to_50++;
                }else if ((plPercent > 50.0) && (plPercent <= 55.0)) {
                    c_profit_50_to_55++;
                }else if ((plPercent > 55.0) && (plPercent <= 60.0)) {
                    c_profit_55_to_60++;
                }else if (plPercent > 60.0) {
                    c_profit_60_above++;
                }
                // count the losses..
                else if ((plPercent < 0.0) && (plPercent >= -5.0)) {
                    c_loss_00_to_05++;
                }else if ((plPercent < -5.0) && (plPercent >= -10.0)) {
                    c_loss_05_to_10++;
                }else if ((plPercent < -10.0) && (plPercent >= -15.0)) {
                    c_loss_10_to_15++;
                }else if ((plPercent < -15.0) && (plPercent >= -20.0)) {
                    c_loss_15_to_20++;
                }else if ((plPercent < -20.0) && (plPercent >= -25.0)) {
                    c_loss_20_to_25++;
                }else if ((plPercent < -25.0) && (plPercent >= -30.0)) {
                    c_loss_25_to_30++;
                }else if ((plPercent < -30.0) && (plPercent >= -35.0)) {
                    c_loss_30_to_35++;
                }else if ((plPercent < -35.0) && (plPercent >= -40.0)) {
                    c_loss_35_to_40++;
                }else if ((plPercent < -40.0) && (plPercent >= -45.0)) {
                    c_loss_40_to_45++;
                }else if ((plPercent < -45.0) && (plPercent >= -50.0)) {
                    c_loss_45_to_50++;
                }else if ((plPercent < -50.0) && (plPercent >= -55.0)) {
                    c_loss_50_to_55++;
                }else if ((plPercent < -55.0) && (plPercent >= -60.0)) {
                    c_loss_55_to_60++;
                }else if (plPercent < -60.0) {
                    c_loss_60_above++;
                }
                
            }else if(whichOne == RUNNING){
                
                //count the profits
                if ((plPercent >= 0.0) && (plPercent <= 5.0)) {
                    r_profit_00_to_05++;
                }else if ((plPercent > 5.0) && (plPercent <= 10.0)) {
                    r_profit_05_to_10++;
                }else if ((plPercent > 10.0) && (plPercent <= 15.0)) {
                    r_profit_10_to_15++;
                }else if ((plPercent > 15.0) && (plPercent <= 20.0)) {
                    r_profit_15_to_20++;
                }else if ((plPercent > 20.0) && (plPercent <= 25.0)) {
                    r_profit_20_to_25++;
                }else if ((plPercent > 25.0) && (plPercent <= 30.0)) {
                    r_profit_25_to_30++;
                }else if ((plPercent > 30.0) && (plPercent <= 35.0)) {
                    r_profit_30_to_35++;
                }else if ((plPercent > 35.0) && (plPercent <= 40.0)) {
                    r_profit_35_to_40++;
                }else if ((plPercent > 40.0) && (plPercent <= 45.0)) {
                    r_profit_40_to_45++;
                }else if ((plPercent > 45.0) && (plPercent <= 50.0)) {
                    r_profit_45_to_50++;
                }else if ((plPercent > 50.0) && (plPercent <= 55.0)) {
                    r_profit_50_to_55++;
                }else if ((plPercent > 55.0) && (plPercent <= 60.0)) {
                    r_profit_55_to_60++;
                }else if (plPercent > 60.0) {
                    r_profit_60_above++;
                }
                // count the losses..
                else if ((plPercent < 0.0) && (plPercent >= -5.0)) {
                    r_loss_00_to_05++;
                }else if ((plPercent < -5.0) && (plPercent >= -10.0)) {
                    r_loss_05_to_10++;
                }else if ((plPercent < -10.0) && (plPercent >= -15.0)) {
                    r_loss_10_to_15++;
                }else if ((plPercent < -15.0) && (plPercent >= -20.0)) {
                    r_loss_15_to_20++;
                }else if ((plPercent < -20.0) && (plPercent >= -25.0)) {
                    r_loss_20_to_25++;
                }else if ((plPercent < -25.0) && (plPercent >= -30.0)) {
                    r_loss_25_to_30++;
                }else if ((plPercent < -30.0) && (plPercent >= -35.0)) {
                    r_loss_30_to_35++;
                }else if ((plPercent < -35.0) && (plPercent >= -40.0)) {
                    r_loss_35_to_40++;
                }else if ((plPercent < -40.0) && (plPercent >= -45.0)) {
                    r_loss_40_to_45++;
                }else if ((plPercent < -45.0) && (plPercent >= -50.0)) {
                    r_loss_45_to_50++;
                }else if ((plPercent < -50.0) && (plPercent >= -55.0)) {
                    r_loss_50_to_55++;
                }else if ((plPercent < -55.0) && (plPercent >= -60.0)) {
                    r_loss_55_to_60++;
                }else if (plPercent < -60.0) {
                    r_loss_60_above++;
                }                                  
            }
        }
        public void display(){
            DecimalFormat df = new DecimalFormat("##.##");
            double percentUp;
            double percentDn;
            double tmpDbl = 0;
            if(totalNumberOfTickers != 0){
                tmpDbl = (((double)currentTotalProfitCnt / (double)totalNumberOfTickers) * 100.00);
            }else{
                tmpDbl = 0.0;
            }
            
            displaySlopeTextArea.append("\n\n");
            displaySlopeTextArea.append(
                    "Exchange: " + exchangeStr + "  TotalNumOfTickers: " + totalNumberOfTickers + 
                    "  P2PDays: " +  pointToPointSpread + "  Date:" + stats.getTodaysDate());
            displaySlopeTextArea.append("\n\n" + "SearchCriteria: " + searchCriteriaStr);
            displaySlopeTextArea.append("\n\n" + "TotalCost: " + totalCostBasis);
                    
            displaySlopeTextArea.append("\n\n" + "CurrentTotals($):" + "\n" +
                    "  CurrentProfits: " + currentTotalProfit + "  CurrentLosses: " + currentTotalLoss + 
                    "  P&L: " +  currentTotalPl + "  CurrentPercent: " + currentTotalPercent + "%" +
                    "\n  CurrentMaxProfit: " + maximumCurrentProfit + "%" + "  CurrentMaxLoss: " + maximumCurrentLoss + "%"
            );
            
            displaySlopeTextArea.append("\n\n" + "RunningMaxTotals($):" + "\n" +
                    "  RmProfits: " + runningTotalProfit + "  RmLosses: " + runningTotalLoss + 
                    "  RmP&L: " +  runningTotalPl + "  RmPercent: " + runningTotalPercent + "%" + 
                    "\n  RmProfit: " + maximumRunningProfit + "%" + "  RmAveProfit: " + runningProfitAve + "%" + 
                    "  RmLoss: " + maximumRunningLoss + "%" + "  RmAveLoss: " + runningLossAve + "%"
                    );
            
            displaySlopeTextArea.append("\n\n" + "CurrentTotalCounts:" + "\n" +
                    "  CurrentProfits: " + currentTotalProfitCnt + "  CurrentLosses: " + currentTotalLossCnt + 
                    "  CurrentPercent: " +  myUtils.roundMe(tmpDbl, 2) + "%");
            
            displaySlopeTextArea.append("\n\n" + "RunningMaxTotalCounts:" + "\n" +
                    "  RmProfits: " + runningTotalProfitCnt + "  RmLosses: " + runningTotalLossCnt + 
                    "  RmZeroLoss: " + runningLossZero 
                    );
            
            displaySlopeTextArea.append("\n\n" + "CurrentProfitMap (Profit >= 0) \n" +
                    "  00%-05%: " + c_profit_00_to_05 + "    05%-10%: " + c_profit_05_to_10 + "    10%-15%: " + c_profit_10_to_15 + "    15%-20%: " + c_profit_15_to_20 + "\n" + 
                    "  20%-25%: " + c_profit_20_to_25 + "    25%-30%: " + c_profit_25_to_30 + "    30%-35%: " + c_profit_30_to_35 + "    35%-40%: " + c_profit_35_to_40 + "\n" +
                    "  40%-45$: " + c_profit_40_to_45 + "    45%-50%: " + c_profit_45_to_50 + "    50%-55%: " + c_profit_50_to_55 + "    55%-60%: " + c_profit_55_to_60 + 
                    "    >60%: " + c_profit_60_above     
            );
            displaySlopeTextArea.append("\n\n" + "CurrentLossMap (Profit < 0) \n" +
                    "  00%-05%: " + c_loss_00_to_05 + "    05%-10%: " + c_loss_05_to_10 + "    10%-15%: " + c_loss_10_to_15 + "    15%-20%: " + c_loss_15_to_20 + "\n" + 
                    "  20%-25%: " + c_loss_20_to_25 + "    25%-30%: " + c_loss_25_to_30 + "    30%-35%: " + c_loss_30_to_35 + "    35%-40%: " + c_loss_35_to_40 + "\n" +
                    "  40%-45$: " + c_loss_40_to_45 + "    45%-50%: " + c_loss_45_to_50 + "    50%-55%: " + c_loss_50_to_55 + "    55%-60%: " + c_loss_55_to_60 + 
                    "    >60%: " + c_loss_60_above     
            );
            
            displaySlopeTextArea.append("\n\n" + "RunningMaxProfitMap (Profit >= 0) \n" +
                    "  00%-05%: " + r_profit_00_to_05 + "    05%-10%: " + r_profit_05_to_10 + "    10%-15%: " + r_profit_10_to_15 + "    15%-20%: " + r_profit_15_to_20 + "\n" + 
                    "  20%-25%: " + r_profit_20_to_25 + "    25%-30%: " + r_profit_25_to_30 + "    30%-35%: " + r_profit_30_to_35 + "    35%-40%: " + r_profit_35_to_40 + "\n" +
                    "  40%-45$: " + r_profit_40_to_45 + "    45%-50%: " + r_profit_45_to_50 + "    50%-55%: " + r_profit_50_to_55 + "    55%-60%: " + r_profit_55_to_60 + 
                    "    >60%: " + r_profit_60_above     
            );
            displaySlopeTextArea.append("\n\n" + "RunningMaxLossMap (Profit < 0) \n" +
                    "  00%-05%: " + r_loss_00_to_05 + "    05%-10%: " + r_loss_05_to_10 + "    10%-15%: " + r_loss_10_to_15 + "    15%-20%: " + r_loss_15_to_20 + "\n" + 
                    "  20%-25%: " + r_loss_20_to_25 + "    25%-30%: " + r_loss_25_to_30 + "    30%-35%: " + r_loss_30_to_35 + "    35%-40%: " + r_loss_35_to_40 + "\n" +
                    "  40%-45$: " + r_loss_40_to_45 + "    45%-50%: " + r_loss_45_to_50 + "    50%-55%: " + r_loss_50_to_55 + "    55%-60%: " + r_loss_55_to_60 + 
                    "    >60%: " + r_loss_60_above     
            );
            percentUp = (((double)trendUpCnt / (double)totalNumberOfTickers) * 100.0);
            percentDn = (((double)trendDnCnt / (double)totalNumberOfTickers) * 100.0);
            displaySlopeTextArea.append("\n\n" + "Trend Count:" + "\n" +
                    "  TrendUp: " + trendUpCnt + "  TrendDn: " + trendDnCnt + "  PercentUp:" + df.format(percentUp) + " %" +
                    "  PercentDn:" + df.format(percentDn) + " %\n" +       
                    "  TrendChangeToUp: " + trendNowUpCnt + "  TrendChangeToDn: " + trendNowDnCnt + 
                    "  PercentToUp:" + df.format(((double)trendNowUpCnt/(double)(((trendUpCnt == 0) ? 1 : (trendUpCnt)) * 100.0))) + "%" +
                    "  PercentToDn:" + df.format(((double)trendNowDnCnt/(double)(((trendDnCnt == 0) ? 1 : (trendDnCnt)) * 100.0))) + "%" 
                    );
        }
        
        
        public void writeToFile() {
            

            double tmpDbl = 0;
            if (totalNumberOfTickers != 0){
                tmpDbl = (((double) currentTotalProfitCnt / (double) totalNumberOfTickers) * 100.00);
            }else{
                tmpDbl = 0.0;
            }
            actDispJob.wrFile.write("\n\n");
            actDispJob.wrFile.write(
                    "Exchange: " + exchangeStr + "  TotalNumOfTickers: " + totalNumberOfTickers
                    + "  P2PDays: " + pointToPointSpread + "  Date:" + stats.getTodaysDate());
            actDispJob.wrFile.write("\n\n" + "SearchCriteria: " + searchCriteriaStr);
            actDispJob.wrFile.write("\n\n" + "TotalCost: " + totalCostBasis);

            actDispJob.wrFile.write("\n\n" + "CurrentTotals($):" + "\n"
                    + "  CurrentProfits: " + currentTotalProfit + "  CurrentLosses: " + currentTotalLoss
                    + "  P&L: " + currentTotalPl + "  CurrentPercent: " + currentTotalPercent + "%"
                    + "\n  CurrentMaxProfit: " + maximumCurrentProfit + "%" + "  CurrentMaxLoss: " + maximumCurrentLoss + "%"
            );

            actDispJob.wrFile.write("\n\n" + "RunningMaxTotals($):" + "\n"
                    + "  RmProfits: " + runningTotalProfit + "  RmLosses: " + runningTotalLoss
                    + "  RmP&L: " + runningTotalPl + "  RmPercent: " + runningTotalPercent + "%"
                    + "\n  RmProfit: " + maximumRunningProfit + "%" + "  RmAveProfit: " + runningProfitAve + "%"
                    + "  RmLoss: " + maximumRunningLoss + "%" + "  RmAveLoss: " + runningLossAve + "%"
            );

            actDispJob.wrFile.write("\n\n" + "CurrentTotalCounts:" + "\n"
                    + "  CurrentProfits: " + currentTotalProfitCnt + "  CurrentLosses: " + currentTotalLossCnt
                    + "  CurrentPercent: " + myUtils.roundMe(tmpDbl, 2) + "%");

            actDispJob.wrFile.write("\n\n" + "RunningMaxTotalCounts:" + "\n"
                    + "  RmProfits: " + runningTotalProfitCnt + "  RmLosses: " + runningTotalLossCnt
                    + "  RmZeroLoss: " + runningLossZero
            );

            actDispJob.wrFile.write("\n\n" + "CurrentProfitMap (Profit >= 0) \n"
                    + "  00%-05%: " + c_profit_00_to_05 + "    05%-10%: " + c_profit_05_to_10 + "    10%-15%: " + c_profit_10_to_15 + "    15%-20%: " + c_profit_15_to_20 + "\n"
                    + "  20%-25%: " + c_profit_20_to_25 + "    25%-30%: " + c_profit_25_to_30 + "    30%-35%: " + c_profit_30_to_35 + "    35%-40%: " + c_profit_35_to_40 + "\n"
                    + "  40%-45$: " + c_profit_40_to_45 + "    45%-50%: " + c_profit_45_to_50 + "    50%-55%: " + c_profit_50_to_55 + "    55%-60%: " + c_profit_55_to_60
                    + "    >60%: " + c_profit_60_above
            );
            actDispJob.wrFile.write("\n\n" + "CurrentLossMap (Profit < 0) \n"
                    + "  00%-05%: " + c_loss_00_to_05 + "    05%-10%: " + c_loss_05_to_10 + "    10%-15%: " + c_loss_10_to_15 + "    15%-20%: " + c_loss_15_to_20 + "\n"
                    + "  20%-25%: " + c_loss_20_to_25 + "    25%-30%: " + c_loss_25_to_30 + "    30%-35%: " + c_loss_30_to_35 + "    35%-40%: " + c_loss_35_to_40 + "\n"
                    + "  40%-45$: " + c_loss_40_to_45 + "    45%-50%: " + c_loss_45_to_50 + "    50%-55%: " + c_loss_50_to_55 + "    55%-60%: " + c_loss_55_to_60
                    + "    >60%: " + c_loss_60_above
            );

            actDispJob.wrFile.write("\n\n" + "RunningMaxProfitMap (Profit >= 0) \n"
                    + "  00%-05%: " + r_profit_00_to_05 + "    05%-10%: " + r_profit_05_to_10 + "    10%-15%: " + r_profit_10_to_15 + "    15%-20%: " + r_profit_15_to_20 + "\n"
                    + "  20%-25%: " + r_profit_20_to_25 + "    25%-30%: " + r_profit_25_to_30 + "    30%-35%: " + r_profit_30_to_35 + "    35%-40%: " + r_profit_35_to_40 + "\n"
                    + "  40%-45$: " + r_profit_40_to_45 + "    45%-50%: " + r_profit_45_to_50 + "    50%-55%: " + r_profit_50_to_55 + "    55%-60%: " + r_profit_55_to_60
                    + "    >60%: " + r_profit_60_above
            );
            actDispJob.wrFile.write("\n\n" + "RunningMaxLossMap (Profit < 0) \n"
                    + "  00%-05%: " + r_loss_00_to_05 + "    05%-10%: " + r_loss_05_to_10 + "    10%-15%: " + r_loss_10_to_15 + "    15%-20%: " + r_loss_15_to_20 + "\n"
                    + "  20%-25%: " + r_loss_20_to_25 + "    25%-30%: " + r_loss_25_to_30 + "    30%-35%: " + r_loss_30_to_35 + "    35%-40%: " + r_loss_35_to_40 + "\n"
                    + "  40%-45$: " + r_loss_40_to_45 + "    45%-50%: " + r_loss_45_to_50 + "    50%-55%: " + r_loss_50_to_55 + "    55%-60%: " + r_loss_55_to_60
                    + "    >60%: " + r_loss_60_above
            );
            actDispJob.wrFile.write("\nDone.");

            //actDispJob.wrFile.closeWr();
            System.out.println("wrote to file.");
        }
    }
    public void startJob(){
     actDispJob = new dispSlopeJob();    
    }
    public void dispSlopeData(SlopesFound slopeIn, int actSlope, boolean wrToFileToo){
        DecimalFormat df = new DecimalFormat("##.##");
        double curProfit = 0.0;
        double profit = 0.0;
        String op = null; 
        int sz = 0;
        int lossCnt = 0;
        double accumLoss = 0.0;
        int gainCnt = 0;
        double accumGain = 0.0;
        if (actSlopesList.Slopes[actSlope] != null) {
            sz = actSlopesList.Slopes[actSlope].size;
        }else{
            //nothing to show here go back..
            return;
        }
  
        displayStocksWthFilter.SlopeStructure thisSlope = actSlopesList.Slopes[actSlope];
        displaySlopeTextArea.append("\n\nTicker: " + thisSlope.ticker + "(" + actSlope + ")");
        displaySlopeTextArea.append("\n  Number of Pivot points: " + sz);
        if(wrToFileToo == true){
            actDispJob.wrFile.write("\n\nTicker: " + thisSlope.ticker + "(" + actSlope + ")");
            actDispJob.wrFile.write("\n  Number of Pivot points: " + sz);
        }
        op = thisSlope.pl.lastBought ? "Bought" : "Sold";
        //if position is open, need to get open value and percent pl...
        if (thisSlope.pl.isPosOpen() == true){     
            curProfit = myUtils.roundMe(thisSlope.pl.runningPl + thisSlope.pl.getCurrentPosValue(), 2);
            profit = myUtils.roundMe(thisSlope.pl.runningPl, 2);
        }else{
            curProfit = myUtils.roundMe(thisSlope.pl.runningPl, 2);
            profit = curProfit;
        }
        displaySlopeTextArea.append(
                "\n  Profit: " + profit + " (" + (thisSlope.overAllPl) + "%)"
                + " Max Profit: " + myUtils.roundMe(thisSlope.pl.maxProfit, 2)
                + " Max Profit Date: " + thisSlope.pl.maxProfitDate
                + "\n  Max Loss: " + myUtils.roundMe(thisSlope.pl.maxLoss, 2)
                + " Max Loss Date: " + thisSlope.pl.maxLossDate
                + " \n  Max Running Profit: " + myUtils.roundMe(thisSlope.pl.maxRunningProfit, 2) + " (" + thisSlope.maxRunningProfitPercent + "%)"
                + "  Max Running Profit Date: " + thisSlope.pl.maxRunningProfitDate
                + "\n  Max Running Loss: " + myUtils.roundMe(thisSlope.pl.maxRunningLoss, 2) + " (" + thisSlope.maxRunningLossPercent + "%)"
                + " Max Running Loss Date: " + thisSlope.pl.maxRunningLossDate        
                + "\n  Transactions: " + thisSlope.pl.transactions + " Last Operation: " + op
                + " DaysIn: " + (thisSlope.pl.ticksIn) + " DaysOut: " + (thisSlope.pl.ticksOut)
                + "\n  Current Trend: "+ thisSlope.currentTrendIs 
                + "\n  AvePriceMv: " + df.format(thisSlope.avePriceMove) + " (" + df.format((thisSlope.avePriceMove / thisSlope.pl.currentPrice) * 100.0) + "%)"
                + "\n  StdDev50Day: " + df.format(thisSlope.stdDev50Day * 100) + "%"
                + "\n  Vol50Day: " + df.format(thisSlope.volDev50Day * 100) + "%"
                + "\n  StdDev100Day: " + df.format(thisSlope.stdDev100Day * 100) + "%"
                + "\n  Vol100Day: " + df.format(thisSlope.volDev100Day * 100) + "%"
                
        );
        displaySlopeTextArea.append("\n");
        for (int i = 0; i < thisSlope.pl.allPl.size(); i++){
            String date = thisSlope.pl.allPl.get(i).dateOut;
            date = date.substring(date.length() - 6);
            displaySlopeTextArea.append(
                    "  P" + i + ": " + df.format(thisSlope.pl.allPl.get(i).pl) + 
                    " (" + df.format(thisSlope.pl.allPl.get(i).plPercent * 100.0) + "%, " + date + ")"                 
            );
            if(((i % 2) == 0) && (i != 0)){
                displaySlopeTextArea.append("\n");    
            }
            //get ave loss
            if (thisSlope.pl.allPl.get(i).plPercent < 0){
                lossCnt++;
                accumLoss += thisSlope.pl.allPl.get(i).plPercent;
            }else if (thisSlope.pl.allPl.get(i).plPercent > 0){
                gainCnt++;
                accumGain += thisSlope.pl.allPl.get(i).plPercent;
            }
            
        }
        accumLoss = (accumLoss / lossCnt);
        accumGain = (accumGain / gainCnt);
        displaySlopeTextArea.append("\n  AveLoss: " + df.format(accumLoss * 100.0) + " (%)" +
                                    "\n  AveGain: " + df.format(accumGain * 100.0) + " (%)"
        );
        if(wrToFileToo == true){
            actDispJob.wrFile.write(
                    "\n  Profit: " + profit + " (" + (thisSlope.overAllPl) + "%)"
                    + " Max Profit: " + myUtils.roundMe(thisSlope.pl.maxProfit, 2)
                    + " Max Profit Date: " + thisSlope.pl.maxProfitDate
                    + "\n  Max Loss: " + myUtils.roundMe(thisSlope.pl.maxLoss, 2)
                    + " Max Loss Date: " + thisSlope.pl.maxLossDate
                    + " \n  Max Running Profit: " + myUtils.roundMe(thisSlope.pl.maxRunningProfit, 2) + " (" + thisSlope.maxRunningProfitPercent + "%)"
                    + "  Max Running Profit Date: " + thisSlope.pl.maxRunningProfitDate
                    + "\n  Max Running Loss: " + myUtils.roundMe(thisSlope.pl.maxRunningLoss, 2) + " (" + thisSlope.maxRunningLossPercent + "%)"
                    + " Max Running Loss Date: " + thisSlope.pl.maxRunningLossDate
                    + "\n  Transactions: " + thisSlope.pl.transactions + " Last Operation: " + op
                    + " DaysIn: " + (thisSlope.pl.ticksIn) + " DaysOut: " + (thisSlope.pl.ticksOut)
                    + "\n  Current Trend: " + thisSlope.currentTrendIs
                    + "\n  AvePriceMv: " + thisSlope.avePriceMove
            );
        }
        if(thisSlope.pl.isPosOpen() == true){          
            displaySlopeTextArea.append(
                    "\n "
                    + " Current Pos Value: " + thisSlope.pl.getCurrentPosValue()
                    + "\n  OverAllPl: " + curProfit + " OverAllPl %: " + myUtils.roundMe((thisSlope.overAllPlIncludeOpen), 2)
                    + ". Current Pos DateIn: " + thisSlope.pl.getCurrentPosDateIn()
            );
            if (wrToFileToo == true) {
                actDispJob.wrFile.write(
                        "\n "
                        + " Current Pos Value: " + thisSlope.pl.getCurrentPosValue()
                        + "\n  OverAllPl: " + curProfit + " OverAllPl %: " + myUtils.roundMe((thisSlope.overAllPlIncludeOpen), 2)
                        + ". Current Pos DateIn: " + thisSlope.pl.getCurrentPosDateIn()
                );
            }
        }

    }
    class dispSlopeJob extends Thread {

        dispSlopeJob() {
            this.start();
        }
        public ioWrTextFiles wrFile; 
        public void run() {
            int actSlope = 0;
            int actTicker = 0;
            boolean displayIt = true;
            boolean done = false;
            wrFile = new ioWrTextFiles("slope." + exchangeStr + "." + slopeFileCnt + ".txt", false);
            System.out.println("\nopened wrFile with exchange: " + exchangeStr);
            //System.out.println("\n Number of Tickers: " + actSlopesList.tickerNumber + ".");
            //displaySlopeTextArea.append("\n Number of Tickers: " + actSlopesList.tickerNumber + ".");
            while (displayIt == true) {
                try {
                    dispSlopeJob.sleep(500);
                    if ((userDisplayNext == true) && (done == false)){
                        //check if view just one
                        if (actSingleViewStr == null) {
                            dispSlopeData(actSlopesList, actSlope, true /*wrToFileTOO*/);
                            stats.update(actSlopesList, actSlope);
                            actSlope++;
                            if (actSlope >= actSlopesList.tickerNumber) {
                                displaySlopeTextArea.append("\nNo More.");
                                done = true;
                                stats.display();
                                stats.writeToFile();
                                displaySlopeTextArea.append("\nDone.");
                                
                            }
                        }else{
                            dispSlopeData(actSlopesList, actSingleViewInt, true /*wrToFileToo*/);
                            userDisplayNext = false;
                        }
                        //userDisplayNext = false;
                    }
                    
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } /* while */
        } /* run */
    } /* dispSlopeJob */
    
    SlopesFound actSlopesList ;
    String actSingleViewStr = null;
    int actSingleViewInt = 0;
    String exchangeStr = null;
    String searchCriteriaStr = null;
    
    public void setActiveSlopes(SlopesFound slopes) {
        actSlopesList = slopes;

    }
    public void setSearchCriteriaStr(String sc){
        searchCriteriaStr = sc;
    }
    public void setExchangeStr(String exch){
        exchangeStr = exch;
    }
    public void setSingleOneToView(String viewThisStr, int itemNum) {
        actSingleViewStr = viewThisStr;
        actSingleViewInt = itemNum;
    }
    public void displayThem() {
        System.out.println("\n Number of Tickers: " + actSlopesList.tickerNumber + ".");
        displaySlopeTextArea.append("\n Number of Tickers: " + actSlopesList.tickerNumber + ".");
        
    }
    
    public class ioWrTextFiles {

        String fname = homeDirectory;

        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;

        void openWr(String fileName, boolean append) {
            fname = fileName;

            try {
                //fos = new FileOutputStream(fileName);
                //dos = new DataOutputStream(fos);
                bow = new BufferedWriter(new FileWriter(fname, append));

                try {
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                } catch (Exception e) {
                    System.out.println("file does not exist " + fname);
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                }

            } catch (Exception e) {
                System.out.println("error writing text to: " + fileName + "(" + e + ").");
            }
        }

        void closeWr() {
            try {
                bow.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioWrTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openWr(fileName, append);

        }

        boolean write(String str) {

            try {
                if (str != null) {
                    bow.write(str);
                    bow.newLine();
                    bow.flush();
                }
            } catch (Exception e) {
                System.out.println("error write to file: " + fname + "(" + e + ").");
            }
            return true;
        }

        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        displaySlopeTextArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        displayNextButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        displaySlopeTextArea.setColumns(20);
        displaySlopeTextArea.setRows(5);
        jScrollPane1.setViewportView(displaySlopeTextArea);

        jLabel1.setText("Display Slope Data");

        displayNextButton.setText("DisplayNext");
        displayNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayNextButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.setName("closeButton"); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(159, 159, 159)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(190, 190, 190)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(193, 193, 193)
                        .addComponent(displayNextButton)
                        .addGap(40, 40, 40)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayNextButton)
                    .addComponent(closeButton))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void displayNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayNextButtonActionPerformed
        // TODO add your handling code here:
        userDisplayNext = true;
    }//GEN-LAST:event_displayNextButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        actDispJob.wrFile.closeWr();
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(displaySlopeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(displaySlopeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(displaySlopeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(displaySlopeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                displaySlopeDialogForm dialog = new displaySlopeDialogForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton displayNextButton;
    private javax.swing.JTextArea displaySlopeTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
