/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.commonGui;
import tradeMenus.slopeAnalysis.*;
import tradeMenus.slopeAnalysis.SlopesFound;
import positions.myUtils;
import java.text.*;
import ibTradeApi.ibApi.historicalData.*;
import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import java.util.Date;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import positions.TradeActivity;
import positions.TradeRequestList;
import positions.TradeRequestData;
import tradeMenus.genComboBoxDialogForm;


/**
 *
 * @author earlie87
 */
public class slopeTraderFrameForm extends javax.swing.JFrame {

    public TradeList tradeList;
    List<String> portfolioTradeList;
    
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    public String tradeListFileName;
    //added 2/22/15 to store todays date. This gives us a way to tell if this is a new day...
    //which we use to mark all didWeTradeToday variables in TradeTicketFile.txt file to NO.
    ioWrTextFiles dateFileWr;
    ioRdTextFiles dateFileRd;
    //added 2/24/16 to stream performance data to file.
    ioWrTextFiles perfFileWr;
    ioRdTextFiles perfFileRd;
    
    String todaysDate = null; 
    boolean thisIsANewDay = false;
    Positions actPositions = new Positions();
    public String accountNumber = "DU218371";
    TradeTickets tradeTickets;
    //int segTradingScale = 6;
    int segTradingScale = 6;
    
    final int DIRECTION_CHANGE  = slopeDefs.DIRECTION_CHANGE;
    final int TREND_UP          = slopeDefs.UPTREND;
    final int TREND_DN          = slopeDefs.DNTREND;
    ibApi.historicalData hd = null;// = new ibApi.historicalData();
    ibApi.historicalData.HistorySlope actHistory;
    
    /* 
     need outer ref to get to child class...confusing!!!!
     compiler error: an enclosing instance that contains...and make sure to 
     import see above@!!!!!
     */
    //outer class in slopeAnalysis..
    slopeAnalysis saOuter = new slopeAnalysis();
    //inner classes within slopeAnalysis...
    SlopesFound allSlopes = saOuter.new SlopesFound();
    SlopeStructure slopeStruct = saOuter.new SlopeStructure();
    final int MA_10DAY = slopeDefs.MA_10DAY;
    final int MA_100DAY = slopeDefs.MA_100DAY;
    final int MA_200DAY = slopeDefs.MA_200DAY;
    final int MA_50DAY = slopeDefs.MA_50DAY;
    final int AVE_90DAY = slopeDefs.AVE_90DAY;
    final int MA_20DAY = slopeDefs.MA_20DAY;
    final int MA_30DAY = slopeDefs.MA_30DAY;
    final int MA_150DAY = slopeDefs.MA_150DAY;
    ibApi actIbApi = ibApi.getActApi();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    ibApi.OrderStatus orderStatus = new ibApi.OrderStatus();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();    
    ibApi.accountInfo actAccInfo;
    sellCriteriaDialogForm sellCriteria = null;
    SellCriteria sellWhen;
    MaWindowSz maWindowSizes = null;
    BuyCriteriaDialogForm buyCriteria = null;
    BuyWhen buyWhen;
    ConfigDialogForm Config = null;   
    int currentBias = slopeDefs.oBiasLong;
    boolean userSelectedBackTesting = false;
    boolean userSelectedBackTestOneTicker = false;
    String backTestingStr = "*** BACK TESTING ONY ***";
    String realTradingStr = "*** BACK TEST AND TRADE ***";
    //traderConfigParams traderConfig = new traderConfigParams(currentBias);
    int openWhenCode = traderConfigParams.DISABLED_INT;
    int closeWhenCode = traderConfigParams.DISABLED_INT;
    boolean userSelectedClickChartOn = true;
    boolean userSelectedClickStatisticsOn = false;
    boolean userSelectedClickPivotStatisticsOn = false;
    boolean userSelectedStreamToFile = false;    
    boolean userSelectedRecommendedOverShootPercent = true;
    String userSelectedBackTestYear = "";
    int userSelectedMa = slopeDefs.MA_100DAY;
    /* userSelectedTradingModeMarket == true, means market orders; 
    userSelectedTradingModeMarket == false, means limitOrdersAlgo */
    boolean userSelectedTradingModeMarket = true;
    /*
    linearMode is used to string long segments then short segments 12 total..
    if scale is set to 6. start with it turned off. when off, it buys sharesPerTrade+sharesOvershoot
    for each segment, then sells sharesPerTrade+sharesOvershoot. So you are always either long or 
    short for each segment. This will not work when really trading because after 2 or more consecutive
    buy's things would be off because you would be less long when selling and not short. It would work if 
    each segemt was connected to different accounts..When linear is off, results are better.
    goal is to try and make linear mode work as good or better...
    */
    boolean linearMode = true;
    TradeActivity actTradeActivity = null;
    TradeRequestList actTradeRequestList = null;
    TradeActivityDialogForm actTradeActivityFrameForm = null;
    AccountInfoFrameForm accInfoFrameForm = null;
    /**
     * Creates new form slopeTraderFrameForm
     * @param posIn
     */
    public slopeTraderFrameForm(Positions posIn) {

        initComponents();
        //this is the new portfolio trade list...
        actPositions = posIn;
        homeDirectory = homeDirectory + actPositions.pathNamePrefix;
        tradeListFileName = "slopeTraderList.txt";
        accountNumber = actPositions.getAccountNumber();
        dateFileRd = new ioRdTextFiles("TraderDateFile.txt", false);
        // read all tickers to trade from file 
        tradeList = readTradeList();
        int idx = 0;
        String tmpStr = null;  
        actAccInfo = actIbApi.new accountInfo(accountNumber);
        todaysDate =  myUtils.GetTodaysDate("YYY/MM/DD");
        todaysDate = myUtils.reverseDate(todaysDate);
        userSelectedBackTestYear = todaysDate;
        userSelectedRecommendedOverShootPercent = recommendOverShootCheckBoxMenuItem.isSelected();
        userSelectedTradingModeMarket = marketOrdersCheckBoxMenuItem.isSelected();
        tmpStr = readDateFromFile();
        //see if new day..
        thisIsANewDay = !tmpStr.equals(todaysDate);        
        currentBias = actPositions.positionBias;
        for (idx = 0; idx < tradeList.getSz(); idx++) {
            tmpStr = tradeList.returnStr(idx);
            tradeListComboBox.addItem(tmpStr);
        }
        tradeTickets = new TradeTickets(tradeList.getSz());
        tradeTickets.setHomeDirectory(homeDirectory);
        tradeTickets.numOfTickets = tradeList.getSz();
        for (idx = 0; idx < tradeList.getSz(); idx++) {
            tmpStr = tradeList.returnStr(idx);
            tradeTickets.tickets[idx] = new TradeTicket(false);
            tradeTickets.tickets[idx].ticker = tmpStr;
            tradeTickets.tickets[idx].numOfSharesToTrade = actPositions.numOfSharesToTrade;

        }
        tradeTickets = getTicketsInfoFromFile(tradeTickets);
        numberOfShares = actPositions.numOfSharesToTrade;
        longOrShort = slopeDefs.getPositionBiasStr(currentBias);
        allSlopes.setSize(tradeTickets.numOfTickets);
        portfolioNameLabel.setText(actPositions.getPortfolioAlias() + " (" + actPositions.getAccountNumber() + ")");
        tickersLabel.setText((Integer.toString(tradeList.getSz())));
        positionBiasLable.setText(slopeDefs.getPositionBiasStr(currentBias));
        portfolioNameLabel.setToolTipText(actPositions.userCriteria);
        if (userSelectedBackTesting == true){
            tradingModeLable.setText(backTestingStr + " *" + userSelectedBackTestYear + "*" );   
        }else{
            tradingModeLable.setText(realTradingStr); 
        }
        if (actTraderConfigParams == null){
            actTraderConfigParams = new traderConfigParams((currentBias));
            actTraderConfigParams.setPrefixDirectory(actPositions.pathNamePrefix);
            actTraderConfigParams.rdFromFile();
        }
        //click on position and display either charts, stats or both...Initialize here..
        userSelectedClickChartOn = clickChartOnRadioButtonMenuItem.isSelected();
        userSelectedClickStatisticsOn = clickStatisticsOnRadioButtonMenuItem.isSelected();
        userSelectedClickPivotStatisticsOn = clickPivotStatisticsOnRadioButtonMenuItem.isSelected();  
        actTradeRequestList = new TradeRequestList();
        actTradeActivity = new TradeActivity(actTradeRequestList);
        actTradeActivity.setTradeMode((
                                        (userSelectedTradingModeMarket == true) ? 
                                        TradeRequestData.TradeModes.oMarket : 
                                        TradeRequestData.TradeModes.oLimitAlgo)
        );
        actTradeActivity.setAccountNumber(accountNumber);
        
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.out.println("Stopping aTimer...and closing window.");

                        setVisible(false);
                        dispose();
                    }
                });

    }
    public void setBias(int bias){
        currentBias = bias;
    }
    public void deleteDateFile(){
        File f  = new File(homeDirectory + "TraderDateFile.txt"); 
        if (f.isFile()){
            f.delete();
        }
    }
    private void writeDateToFile(String dateIn){
        //
        dateFileWr =  new ioWrTextFiles("TraderDateFile.txt", false);
        dateFileWr.write(dateIn);
    }
    private String readDateFromFile(){
        String s = null;
        //check if it exists..
        File f  = new File(homeDirectory + "TraderDateFile.txt");
        
        if (!f.isFile()) {
            System.out.println("date file does not exist..return empty string..");
            s = "empty";
        }else{
            if (f.length() > 0 ) {
                //file just holds the date.
                s = dateFileRd.read(false);
            }else{
                s = "empty";
            }            
        }
        return(s);
        
    }
    public TradeTickets getTicketsInfoFromFile(TradeTickets theseTickets) {
        int idx = 0;
        /*
        If the paramters for this file have changed, you need to manually delete the file (from term).
        Here we detect the file does not exist so we first down load (wr) to the file, we then read it back.
        */
        File f  = new File(homeDirectory + "TradeTicket.txt");
        if (!f.isFile()){
            //ok. does not exist so we need to create one with default values...
            backUpTicketInfoToFile(theseTickets);
        }
        theseTickets.tradeTicketFile.openRdFile("TradeTicket.txt", true /*append*/);
        for (idx = 0; idx < theseTickets.numOfTickets; idx++) {
            theseTickets.rdFromFile(idx);
        }
        theseTickets.tradeTicketFile.closeRdFile();
        return (theseTickets);
    }

    public void backUpTicketInfoToFile(TradeTickets theseTickets) {
        /*
         This routine will backup the Trade information for all tickets to a file.
         This should be done periodically but definitely before shuting down.
         */
        int idx = 0;
        theseTickets.tradeTicketFile.openWrFile("TradeTicket.txt", false /*don't append*/);
        for (idx = 0; idx < theseTickets.numOfTickets; idx++) {
            theseTickets.wrToFile(idx);
        }
        theseTickets.tradeTicketFile.closeWrFile();

    }

    public class TradeList {

        List<String> list = new ArrayList<String>();

        public void addOne(String ticker) {
            list.add(ticker);
        }

        public void tradeList() {

        }

        public String returnStr(int idx) {
            return (list.get(idx));
        }

        public int findOne(String ticker) {
            int idx = 0;
            int retIdx = 0;
            boolean found = false;
            for (idx = 0; (found == false) && (idx < list.size()); idx++) {
                if (ticker == list.get(idx)) {
                    found = true;
                    retIdx = idx;
                }
            }
            if (found == true) {
                return (retIdx);
            } else {
                return -1;
            }
        }

        public void removeAll() {
            list.removeAll(list);
        }

        public void removeOne(String remTicker) {
            list.remove(remTicker);
        }

        public int getSz() {
            return (list.size());
        }

        public void displayAll() {
            for (int idx = 0; idx < list.size(); idx++) {
                System.out.println("\n" + list.get(idx));
            }
        }
    }

    public TradeList readTradeList() {
        TradeList list = new TradeList();
        ioRdTextFiles tradeListFile = new ioRdTextFiles(tradeListFileName, false);
        String tmpStr = null;
        int idx = 0;
        while ((tmpStr = tradeListFile.read(false)) != null) {
            list.addOne(tmpStr);
            idx++;
        }
        System.out.println("\nread tradeList.." + idx + " tickers.");
        tradeListFile.closeRd();
        return (list);
    }

    public class TradeTickets {

        public FileItAway tradeTicketFile = new FileItAway();
        public TradeTicket tickets[];
        int numOfTickets = 0;

        public TradeTickets(int size) {
            numOfTickets = size;
            //tradeTicketFile.openWrFile("TradeTicket.txt", false /*don't append first time*/);
            tickets = new TradeTicket[size];
        }
        public void setHomeDirectory(String homeDir){
            tradeTicketFile.setHomeDirectory(homeDir);
        }
        public void rdFromFile(int ticket) {

            tradeTicketFile.getBeg(ticket);
            tradeTicketFile.getString(tickets[ticket].ticker, "ticker");
            tradeTicketFile.getDouble(tickets[ticket].originalCost, "originalCost");
            tradeTicketFile.getDouble(tickets[ticket].originalCostOverShoot, "originalCostOverShoot");
            tradeTicketFile.getDouble(tickets[ticket].currentCost, "currentCost");
            tradeTicketFile.getDouble(tickets[ticket].runningPl, "runningPl");
            tradeTicketFile.getDouble(tickets[ticket].runningPlPercent, "runningPlPercent");
            tradeTicketFile.getDouble(tickets[ticket].maxProfit, "maxProfit");
            tradeTicketFile.getDouble(tickets[ticket].maxProfitPercent, "maxProfitPercent");
            tradeTicketFile.getDouble(tickets[ticket].maxLoss, "maxLoss");
            tradeTicketFile.getDouble(tickets[ticket].maxLossPercent, "maxLossPercent");
            tradeTicketFile.getInteger(tickets[ticket].sharesAtHand, "sharesAtHand");
            tradeTicketFile.getInteger(tickets[ticket].numberOfTrades, "numberOfTrades");
            tradeTicketFile.getInteger(tickets[ticket].numOfSharesToTrade, "numOfSharesToTrade");
            //new added daysIn daysOut..2/25/15
            tradeTicketFile.getInteger(tickets[ticket].daysIn, "daysIn");
            tradeTicketFile.getInteger(tickets[ticket].daysOut, "daysOut");
            
            
            tradeTicketFile.getString(tickets[ticket].lastOperation, "lastOperation");
            //new added 2/20/15 to remember if we traded this position today.
            tradeTicketFile.getString(tickets[ticket].didWeTradeToday, "didWeTradeToday");
            tradeTicketFile.getString(tickets[ticket].curTrend, "curTrend");
            
            tradeTicketFile.getString(tickets[ticket].maxPDate, "maxProfitDate");
            tradeTicketFile.getString(tickets[ticket].maxLDate, "maxLossDate");
            tradeTicketFile.getString(tickets[ticket].maxRunningProfitDate, "maxRuningProfitDate");
            tradeTicketFile.getString(tickets[ticket].maxRunningLossDate, "maxRuningLossDate");
            //added openPosDateIn 2/25/15..
            tradeTicketFile.getString(tickets[ticket].OpenPosDateIn, "OpenPosDateIn");
            
            tradeTicketFile.getDouble(tickets[ticket].maxRunningProfit, "maxRunningProfit");
            tradeTicketFile.getDouble(tickets[ticket].maxRunningLoss, "maxRunningLoss");
            tradeTicketFile.getDouble(tickets[ticket].lastPrice, "lastPrice");
            //added 11/23/15
            tradeTicketFile.getInteger(tickets[ticket].hysteresisDays, "hysteresisDays");
            if (true) {
                //added 2/14/16 for long segments
                tradeTicketFile.getInteger(tickets[ticket].actSegTrade, "actSegTrade");
                //added 2/16/16
                int tmp = 0;
                boolean open = false;
                tradeTicketFile.getInteger(tmp, "segTrades0 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[0].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTrades1 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[1].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTrades2 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[2].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTrades3 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[3].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTrades4 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[4].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTrades5 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTrades[5].segOpen = open;

                //added 2/10/16
                tradeTicketFile.getDouble(tickets[ticket].segTrades[0].segPrice, "segTrades0 - price");
                tradeTicketFile.getDouble(tickets[ticket].segTrades[1].segPrice, "segTrades1");
                tradeTicketFile.getDouble(tickets[ticket].segTrades[2].segPrice, "segTrades2");
                tradeTicketFile.getDouble(tickets[ticket].segTrades[3].segPrice, "segTrades3");
                tradeTicketFile.getDouble(tickets[ticket].segTrades[4].segPrice, "segTrades4");
                tradeTicketFile.getDouble(tickets[ticket].segTrades[5].segPrice, "segTrades5");

                //added 2/16/16
                tradeTicketFile.getInteger(tickets[ticket].segTrades[0].segShares, "segTrades0 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTrades[1].segShares, "segTrades1 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTrades[2].segShares, "segTrades2 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTrades[3].segShares, "segTrades3 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTrades[4].segShares, "segTrades4 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTrades[5].segShares, "segTrades5 - segShares");

            }
             if (true) {
                //added 2/22/16 for overShoot short segments
                tradeTicketFile.getInteger(tickets[ticket].actSegTradeOverShoot, "actSegTradeOverShoot");
                //added 2/22/16
                int tmp = 0;
                boolean open = false;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot0 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[0].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot1 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[1].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot2 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[2].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot3 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[3].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot4 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[4].segOpen = open;
                tradeTicketFile.getInteger(tmp, "segTradesOverShoot5 - segOpen");
                open = ((tmp == 1) ? true : false);
                tickets[ticket].segTradesOverShoot[5].segOpen = open;
                
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[0].segPrice, "segTradesOverShoot0 - price");
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[1].segPrice, "segTradesOverShoot1");
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[2].segPrice, "segTradesOverShoot2");
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[3].segPrice, "segTradesOverShoot3");
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[4].segPrice, "segTradesOverShoot4");
                tradeTicketFile.getDouble(tickets[ticket].segTradesOverShoot[5].segPrice, "segTradesOverShoot5");

                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[0].segShares, "segTradesOverShoot0 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[1].segShares, "segTradesOverShoot1 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[2].segShares, "segTradesOverShoot2 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[3].segShares, "segTradesOverShoot3 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[4].segShares, "segTradesOverShoot4 - segShares");
                tradeTicketFile.getInteger(tickets[ticket].segTradesOverShoot[5].segShares, "segTradesOverShoot5 - segShares");

            }
            tradeTicketFile.getEnd(ticket);
            tradeTicketFile.rd();
            tradeTicketFile.putToThisTicket(tickets[ticket]);
        }

        public void wrToFile(int ticket) {

            tradeTicketFile.setBeg(ticket);
            tradeTicketFile.setString(tickets[ticket].ticker, "ticker");
            tradeTicketFile.setDouble(tickets[ticket].originalCost, "originalCost");
            tradeTicketFile.setDouble(tickets[ticket].originalCostOverShoot, "originalCostOverShoot");
            tradeTicketFile.setDouble(tickets[ticket].currentCost, "currentCost");
            tradeTicketFile.setDouble(tickets[ticket].runningPl, "runningPl");
            tradeTicketFile.setDouble(tickets[ticket].runningPlPercent, "runningPlPercent");
            tradeTicketFile.setDouble(tickets[ticket].maxProfit, "maxProfit");
            tradeTicketFile.setDouble(tickets[ticket].maxProfitPercent, "maxProfitPercent");
            tradeTicketFile.setDouble(tickets[ticket].maxLoss, "maxLoss");
            tradeTicketFile.setDouble(tickets[ticket].maxLossPercent, "maxLossPercent");
            tradeTicketFile.setInteger(tickets[ticket].sharesAtHand, "sharesAtHand");
            tradeTicketFile.setInteger(tickets[ticket].numberOfTrades, "numberOfTrades");
            tradeTicketFile.setInteger(tickets[ticket].numOfSharesToTrade, "numOfSharesToTrade");
            //added daysIn/DaysOut on 2/25/15..
            tradeTicketFile.setInteger(tickets[ticket].daysIn, "daysIn");
            tradeTicketFile.setInteger(tickets[ticket].daysOut, "daysOut");
            
            tradeTicketFile.setString(tickets[ticket].lastOperation, "lastOperation");
            //new added 2/20/15 to remember that we traded this pos today..
            tradeTicketFile.setString(tickets[ticket].didWeTradeToday, "didWeTradeToday");
            tradeTicketFile.setString(tickets[ticket].curTrend, "curTrend");
            
            tradeTicketFile.setString(tickets[ticket].maxPDate, "maxProfitDate");
            tradeTicketFile.setString(tickets[ticket].maxLDate, "maxLossDate");
            tradeTicketFile.setString(tickets[ticket].maxRunningProfitDate, "maxRuningProfitDate");
            tradeTicketFile.setString(tickets[ticket].maxRunningLossDate, "maxRuningLossDate");
            //added openPosDateIn 2/25/15..
            tradeTicketFile.setString(tickets[ticket].OpenPosDateIn, "OpenPosDateIn");
            
            tradeTicketFile.setDouble(tickets[ticket].maxRunningProfit, "maxRunningProfit");
            tradeTicketFile.setDouble(tickets[ticket].maxRunningLoss, "maxRunningLoss");
            //added 3/20/15
            tradeTicketFile.setDouble(tickets[ticket].lastPrice, "lastPrice");
            //added 11/23/15
            tradeTicketFile.setInteger(tickets[ticket].hysteresisDays, "hysteresisDays");
            if (true) {
                //added 2/16/16 for long segments
                tradeTicketFile.setInteger(tickets[ticket].actSegTrade, "actSegTrade");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[0].segOpen == true) ? 1 : 0, "segTrades0 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[1].segOpen == true) ? 1 : 0, "segTrades1 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[2].segOpen == true) ? 1 : 0, "segTrades2 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[3].segOpen == true) ? 1 : 0, "segTrades3 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[4].segOpen == true) ? 1 : 0, "segTrades4 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTrades[5].segOpen == true) ? 1 : 0, "segTrades5 - segOpen");
                //added 2/10/16
                tradeTicketFile.setDouble(tickets[ticket].segTrades[0].segPrice, "segTrades0 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTrades[1].segPrice, "segTrades1 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTrades[2].segPrice, "segTrades2 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTrades[3].segPrice, "segTrades3 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTrades[4].segPrice, "segTrades4 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTrades[5].segPrice, "segTrades5 - price");
                //added 2/16/16
                tradeTicketFile.setInteger(tickets[ticket].segTrades[0].segShares, "segTrades0 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTrades[1].segShares, "segTrades1 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTrades[2].segShares, "segTrades2 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTrades[3].segShares, "segTrades3 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTrades[4].segShares, "segTrades4 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTrades[5].segShares, "segTrades5 - shares");

            }
            if (true) {
                //added 2/22/16 for overShoot short segments
                tradeTicketFile.setInteger(tickets[ticket].actSegTradeOverShoot, "actSegTradeOverShoot");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[0].segOpen == true) ? 1 : 0, "segTradesOverShoot0 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[1].segOpen == true) ? 1 : 0, "segTradesOverShoot1 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[2].segOpen == true) ? 1 : 0, "segTradesOverShoot2 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[3].segOpen == true) ? 1 : 0, "segTradesOverShoot3 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[4].segOpen == true) ? 1 : 0, "segTradesOverShoot4 - segOpen");
                tradeTicketFile.setInteger((tickets[ticket].segTradesOverShoot[5].segOpen == true) ? 1 : 0, "segTradesOverShoot5 - segOpen");
              
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[0].segPrice, "segTradesOverShoot0 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[1].segPrice, "segTradesOverShoot1 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[2].segPrice, "segTradesOverShoot2 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[3].segPrice, "segTradesOverShoot3 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[4].segPrice, "segTradesOverShoot4 - price");
                tradeTicketFile.setDouble(tickets[ticket].segTradesOverShoot[5].segPrice, "segTradesOverShoot5 - price");
               
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[0].segShares, "segTradesOverShoot0 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[1].segShares, "segTradesOverShoot1 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[2].segShares, "segTradesOverShoot2 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[3].segShares, "segTradesOverShoot3 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[4].segShares, "segTradesOverShoot4 - shares");
                tradeTicketFile.setInteger(tickets[ticket].segTradesOverShoot[5].segShares, "segTradesOverShoot5 - shares");

            }
            tradeTicketFile.setEnd(ticket);
            tradeTicketFile.wr();

        }
        private int findTicket(String ticker){
            int idx;
            int retIdx = -1;
            boolean found = false;
            for(idx = 0; ((found == false) && (idx < this.numOfTickets)); idx++){
                if (this.tickets[idx].ticker.equals(ticker) == true) {
                    found  = true;
                    retIdx = idx;
                }
            }
            return(retIdx);
        }
    }

    public class TradeTicket {

        //pos state the ticket can be..
        public static final String NEW = slopeDefs.NEW;
        public static final String BOT = slopeDefs.BOT;
        public static final String SOLD = slopeDefs.SOLD;
        // begin of ones stored to file..
        String ticker = null;
        double chLastToOpenPrice = 0.0;
        double lastPrice = 0.0;
        double originalCost = 0.0;
        double originalCostOverShoot = 0.0;
        double currentCost = 0.0;
        double runningPl = 0.0;
        double runningPlPercent = 0.0;
        double maxProfit = 0.0;
        double maxProfitPercent = 0.0;
        double maxLoss = 0.0;
        double maxLossPercent = 0.0;
        int sharesAtHand = 0;
        int numberOfTrades = 0;
        int numOfSharesToTrade = 0;
        String lastOperation = NEW;
        String curTrend = "empty";
        //end of ones stored
        
        //wfs add these to file save/restore....2/12/15
        String maxPDate = "empty";
        String maxLDate = "empty";
        String maxRunningProfitDate = "empty";
        String maxRunningLossDate = "empty";
        String OpenPosDateIn = "empty";
        double maxRunningProfit = 0.0;
        double maxRunningLoss = 0.0;
        int daysIn = 0;
        int daysOut = 0;
        //aded 11/23/15 
        int hysteresisDays = 0;
        //wfs new but don't need to store to file can caclulate on the fly
        double currentPlPercent = 0.0;
        double openPosValue = 0.0;
        double openPosValuePercent = 0.0;
        
        public FileItAway tradeTicketFile = new FileItAway();
        int elementCnt = 0;
        double overAllPlPercent = 0.0;
        double overAllPl = 0.0;
        double overAllPlIncludeOpenPercent = 0.0;
        double maxRunningProfitPercent = 0.0;
        double maxRunningLossPercent = 0.0;
        boolean buyNow = false;
        boolean sellNow = false;
        boolean buyOrderPlaced = false;
        boolean sellOrderPlaced = false;
        boolean problemProcessing = false;
        /* 
        added 4/10/15 to lock in gains. if lockGain is true 
        then look at lockGainPercent to see if it's time to sell.
        */
        boolean lockGain = false;
        double lockGainPercent = 0.0;
        /* 
        added 6/19/15 to stop loss. If stopLoss is true
        then look at stopLossPercent to see if it's time to sell.
        */
        boolean stopLoss = false;
        double stopLossPercent = 0.0;
        
        boolean liquidateAll = false;
        //this tells us if all tickers are affected by lockGain/stopLoss setting.
        boolean setAllTickers = false;
        double avePriceMove = 0.0;
        
       /* this was added to "remember" if we traded today.
          this is needed because the close price for today is really the
          last price (which will change throughtout the day until the market
          closes, then does become the close. We do not want to trigger trades multiple times 
          in one day. So if it trades, it will not trade again that day.
        */
        String didWeTradeToday = slopeDefs.NO;
        //added 2/10/16 for segment trading..
        public class SegTrades{
            boolean segOpen = false;
            double segPrice = 0.0;
            int segShares = 0;
        }
        //double segTrades[] = new double[segTradingScale];
        
        int actSegTrade = 0;
        SegTrades segTrades[] = new SegTrades[segTradingScale];
        int actSegTradeOverShoot = 0;
        SegTrades segTradesOverShoot[] = new SegTrades[segTradingScale];
        //this will contain the one to trade: either setTrades or segTradesOverShoot..
        int actSegTradeDoThisOne = 0;
        SegTrades segTradeDoThisOne[] = new SegTrades[segTradingScale];
        
        public boolean segTradeAllIn(SegTrades thisSegTrade[]){
            int idx = 0;
            boolean allIn = true;
            for (idx = 0; idx < segTradingScale; idx++){
                if (thisSegTrade[idx].segOpen == false){
                    allIn = false;
                    return allIn;
                }
            }
            return (allIn);            
        }
        public boolean segTradeAllout(SegTrades thisSegTrade[]){
            int idx = 0;
            boolean allOut = true;
            for (idx = 0; idx < segTradingScale; idx++){
                if (thisSegTrade[idx].segOpen == true){
                    allOut = false;
                    return allOut;
                }
            }
            return (allOut);
        }
        public double segGetPrice(SegTrades thisSegTrade[], int seg){
            return(thisSegTrade[seg].segPrice);
        }
        public int segGetNumShares(SegTrades thisSegTrade[], int seg){
            return(thisSegTrade[seg].segShares);
        }
        public double segTrade(String operation, double price, int segShares){
            /*  scale trading. keep open price of upto 6 positions
                values > 0 are open positions,  values == -1.0 means closed.
                open operation stores the price, close operations returns the stored price.
            */
            
            double retValue = 0.0;
            if((operation.equals(slopeDefs.oBuyToOpenLong)) || (operation.equals(slopeDefs.oSellToOpenShort))){
                //open operation, we don't return anything.
                retValue = 0.0;
                
                if(this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen == true){
                    //want to open a position and this one is currently open, so go to next one if not all in..
                    if(this.actSegTradeDoThisOne < (segTradingScale - 1)){
                        this.actSegTradeDoThisOne++;
                        //store the price and return 0.0..
                        this.segTradeDoThisOne[this.actSegTradeDoThisOne].segPrice = price; 
                        this.segTradeDoThisOne[this.actSegTradeDoThisOne].segShares = segShares; 
                        this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen = true;
                        
                    }else{                        
                    }
                }else{
                    //this one is available, already closed so store it..
                    this.segTradeDoThisOne[this.actSegTradeDoThisOne].segPrice = price; 
                    this.segTradeDoThisOne[this.actSegTradeDoThisOne].segShares = segShares; 
                    this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen = true;                   
                }                   
            }else if ((operation.equals(slopeDefs.oSellToCloseLong)) || (operation.equals(slopeDefs.oBuyToCloseShort))){  
                //close operation return the segment price at which is was opened..
                if(this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen == false){
                   //want to close a position and this one is already closed, so go to previos one if not all out.
                    if(this.actSegTradeDoThisOne > 0){
                        this.actSegTradeDoThisOne--;
                        retValue = this.segTradeDoThisOne[actSegTradeDoThisOne].segPrice;
                        this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen = false;
                    } else {
                         //nothing to close..should not happen..
                        //allOut = true;
                        retValue = slopeDefs.oCLOSED_POSITION;
                    }
                }else{
                    retValue = this.segTradeDoThisOne[actSegTradeDoThisOne].segPrice;
                    this.segTradeDoThisOne[this.actSegTradeDoThisOne].segOpen = false;                    
                }               
            }            
            return retValue;
        }
        public double segTradeGetCost(){
            double cost = 0.0;
            int idx = 0;
            for (idx = 0; idx < segTradingScale; idx++){
                if(segTrades[idx].segOpen == true){
                    cost += (segTrades[idx].segPrice * segTrades[idx].segShares);
                }
                
            }
            return cost;
        }
        public double segTradeGetCost(SegTrades trades[]){
            /*
            this one works with either segTrades or SegTradesOvershoot arrays...
            */
            double cost = 0.0;
            int idx = 0;
            for (idx = 0; idx < segTradingScale; idx++){
                if(trades[idx].segOpen == true){
                    cost += (trades[idx].segPrice * trades[idx].segShares);
                }
                
            }
            return cost;
        }
        public double segTradeGetCost(SegTrades trades){
            /*
            just get cost of open segment.
             */
            double cost = 0.0;

            if (trades.segOpen == true) {
                cost = (trades.segPrice * trades.segShares);
            }        
            return cost ;
        }
        public TradeTicket(boolean first) {
            int idx = 0;
            for (idx = 0; idx < segTradingScale; idx++){
                //do longs...
                segTrades[idx] = new SegTrades();
                segTrades[idx].segOpen = false;
                segTrades[idx].segPrice = 0.0;
                segTrades[idx].segShares = 0;
                //do overshoot shorts..
                segTradesOverShoot[idx] = new SegTrades();
                segTradesOverShoot[idx].segOpen = false;
                segTradesOverShoot[idx].segPrice = 0.0;
                segTradesOverShoot[idx].segShares = 0;
            }
        }

        public void rdFromFile() {

        }
    }

    public class ioRdTextFiles {

        String fname = homeDirectory;

        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;
        BufferedWriter bow;

        private void openRd(String fileName, boolean append) {
            fname = fileName;

            try {
                fis = new FileInputStream(fname);
                bir = new BufferedReader(new InputStreamReader(fis));
            } catch (Exception e) {
                System.out.println("error reading text from: " + fname + "(" + e + ").");
                System.out.println("file does not exist " + fname);
                try {
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                } catch (IOException ex) {
                    Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
                }

                bir = new BufferedReader(new InputStreamReader(fis));
            }
        }

        void closeRd() {
            try {
                bir.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioRdTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            
            this.openRd(fname + fileName, append);

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
            this.openWr(fname + fileName, append);

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

        jDialog1 = new javax.swing.JDialog();
        startStopButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        tradeListComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        traderStatusTable = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        progressLable = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        totalsTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        pauseButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        portfolioNameLabel = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        tickersLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        availFundsLabel = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        commentsTextArea = new javax.swing.JTextArea();
        buyCriteriaButton = new javax.swing.JButton();
        tickerTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        sellCriteriaButton = new javax.swing.JButton();
        tradingModeLable = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        positionBiasLable = new javax.swing.JLabel();
        backTestOneTickerButton = new javax.swing.JButton();
        showTradeDescriptioinsRadioButton = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        liqValueLabel = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        tradeTypeLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        moneySavedLabel = new javax.swing.JLabel();
        traderMenuBar = new javax.swing.JMenuBar();
        clickChartOnRadioButtonItemMenu = new javax.swing.JMenu();
        openCloseCriteriaMenuItem = new javax.swing.JMenuItem();
        backtestingMenuItemCheckBox = new javax.swing.JCheckBoxMenuItem();
        linearModeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        streamToFileCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        recommendOverShootCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        marketOrdersCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        clickChartOnRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        clickStatisticsOnRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        clickPivotStatisticsOnRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        backTestYearMenuItem = new javax.swing.JMenuItem();
        setMovingAverageMenuItem = new javax.swing.JMenuItem();
        TradeRulesMenuItem = new javax.swing.JMenuItem();
        displayMenu = new javax.swing.JMenu();
        displayCriteriaMenuItem = new javax.swing.JMenuItem();
        positionPerformanceMenuItem = new javax.swing.JMenuItem();
        portfolioPerformanceMenuItem = new javax.swing.JMenuItem();
        portfolioOsUsageMenuItem = new javax.swing.JMenuItem();
        TradingActivityMenuItem = new javax.swing.JMenuItem();
        accountInfoMenuItem = new javax.swing.JMenuItem();
        chartMenu = new javax.swing.JMenu();
        chartPositionMenuItem = new javax.swing.JMenuItem();
        chartPortfolioMenuItem = new javax.swing.JMenuItem();
        chartVolocityMenuItem = new javax.swing.JMenuItem();
        chartPositionScaleUsageMenuItem = new javax.swing.JMenuItem();
        chartPortfolioScaleUsageMenuItem = new javax.swing.JMenuItem();
        openPositionValuesMenuItem = new javax.swing.JMenuItem();
        openPortfolioPositionValues = new javax.swing.JMenuItem();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setTitle("Pivotal Slope Trader");

        startStopButton.setText("Start/Stop");
        startStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        tradeListComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tradeListComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("TradeList");

        traderStatusTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Num", "Stock", "Shares", "Last", "Chng", "RealZdPL", "%", "RunningPl", "%", "OpnPosVal", "%", "State", "Trades", "CurTrend"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        traderStatusTable.setCellSelectionEnabled(true);
        traderStatusTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                traderStatusTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(traderStatusTable);

        jLabel4.setText("Trade Status");

        jLabel6.setText("Progress:");

        totalsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Displaying:", "OrigCost", "RunningPL", "%", "TotalsOfEach:"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(totalsTable);

        jLabel2.setText("Totals:");

        pauseButton.setText("Pause/Continue");
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("NotableComments:");

        jLabel9.setText("Portfolio:");

        portfolioNameLabel.setText("         ");

        jLabel10.setText("Tickers:");

        tickersLabel.setText("              ");

        jLabel11.setText("AvailFunds:");

        availFundsLabel.setText("            ");

        jLabel13.setText("Select From Trade List Or Enter Ticker");

        commentsTextArea.setColumns(20);
        commentsTextArea.setRows(5);
        jScrollPane2.setViewportView(commentsTextArea);

        buyCriteriaButton.setText("BuyCriteria");
        buyCriteriaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buyCriteriaButtonActionPerformed(evt);
            }
        });

        tickerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tickerTextFieldActionPerformed(evt);
            }
        });

        jLabel14.setText("Enter Ticker");

        sellCriteriaButton.setText("SellCriteria");
        sellCriteriaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sellCriteriaButtonActionPerformed(evt);
            }
        });

        tradingModeLable.setText("     ");

        jLabel1.setText("PositionBias:");

        positionBiasLable.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                positionBiasLableAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        backTestOneTickerButton.setText("BackTest");
        backTestOneTickerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestOneTickerButtonActionPerformed(evt);
            }
        });

        showTradeDescriptioinsRadioButton.setText("ShowTradeDescriptions");
        showTradeDescriptioinsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTradeDescriptioinsRadioButtonActionPerformed(evt);
            }
        });

        jLabel12.setText("LiqValue:");

        liqValueLabel.setText("      ");

        jLabel15.setText("TradeType:");

        jLabel16.setText("MoneySaved:");

        moneySavedLabel.setText("             ");

        clickChartOnRadioButtonItemMenu.setText("Set");
        clickChartOnRadioButtonItemMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clickChartOnRadioButtonItemMenuActionPerformed(evt);
            }
        });

        openCloseCriteriaMenuItem.setText("OpenCloseCriteria");
        openCloseCriteriaMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCloseCriteriaMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(openCloseCriteriaMenuItem);

        backtestingMenuItemCheckBox.setText("BackTestingOn");
        backtestingMenuItemCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backtestingMenuItemCheckBoxActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(backtestingMenuItemCheckBox);

        linearModeCheckBoxMenuItem.setSelected(true);
        linearModeCheckBoxMenuItem.setText("LinearMode");
        linearModeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linearModeCheckBoxMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(linearModeCheckBoxMenuItem);

        streamToFileCheckBoxMenuItem.setText("StreamToFile");
        streamToFileCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                streamToFileCheckBoxMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(streamToFileCheckBoxMenuItem);

        recommendOverShootCheckBoxMenuItem.setSelected(true);
        recommendOverShootCheckBoxMenuItem.setText("RecommendOverShoot");
        recommendOverShootCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recommendOverShootCheckBoxMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(recommendOverShootCheckBoxMenuItem);

        marketOrdersCheckBoxMenuItem.setText("MarketOrdersOn");
        marketOrdersCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                marketOrdersCheckBoxMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(marketOrdersCheckBoxMenuItem);
        clickChartOnRadioButtonItemMenu.add(jSeparator1);

        clickChartOnRadioButtonMenuItem.setSelected(true);
        clickChartOnRadioButtonMenuItem.setText("ClickChartOn");
        clickChartOnRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clickChartOnRadioButtonMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(clickChartOnRadioButtonMenuItem);

        clickStatisticsOnRadioButtonMenuItem.setText("ClickStatisticsOn");
        clickStatisticsOnRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clickStatisticsOnRadioButtonMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(clickStatisticsOnRadioButtonMenuItem);

        clickPivotStatisticsOnRadioButtonMenuItem.setText("ClickPivotStatisticsOn");
        clickPivotStatisticsOnRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clickPivotStatisticsOnRadioButtonMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(clickPivotStatisticsOnRadioButtonMenuItem);
        clickChartOnRadioButtonItemMenu.add(jSeparator2);

        backTestYearMenuItem.setText("BackTestYear");
        backTestYearMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestYearMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(backTestYearMenuItem);

        setMovingAverageMenuItem.setText("MovingAverage");
        setMovingAverageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMovingAverageMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(setMovingAverageMenuItem);

        TradeRulesMenuItem.setText("TradeRules");
        TradeRulesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TradeRulesMenuItemActionPerformed(evt);
            }
        });
        clickChartOnRadioButtonItemMenu.add(TradeRulesMenuItem);

        traderMenuBar.add(clickChartOnRadioButtonItemMenu);

        displayMenu.setText("Display");
        displayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayMenuActionPerformed(evt);
            }
        });

        displayCriteriaMenuItem.setText("Criteria");
        displayCriteriaMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCriteriaMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(displayCriteriaMenuItem);

        positionPerformanceMenuItem.setText("PositionPerformance");
        positionPerformanceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionPerformanceMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(positionPerformanceMenuItem);

        portfolioPerformanceMenuItem.setText("PortfolioPerformance");
        portfolioPerformanceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portfolioPerformanceMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(portfolioPerformanceMenuItem);

        portfolioOsUsageMenuItem.setText("PortfolioOSUsage");
        portfolioOsUsageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portfolioOsUsageMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(portfolioOsUsageMenuItem);

        TradingActivityMenuItem.setText("TradingActivity");
        TradingActivityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TradingActivityMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(TradingActivityMenuItem);

        accountInfoMenuItem.setText("AccountInfo");
        accountInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountInfoMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(accountInfoMenuItem);

        traderMenuBar.add(displayMenu);

        chartMenu.setText("Chart");
        chartMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartMenuActionPerformed(evt);
            }
        });

        chartPositionMenuItem.setText("Position");
        chartPositionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartPositionMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(chartPositionMenuItem);

        chartPortfolioMenuItem.setText("Portfolio");
        chartPortfolioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartPortfolioMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(chartPortfolioMenuItem);

        chartVolocityMenuItem.setText("Volocity");
        chartVolocityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartVolocityMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(chartVolocityMenuItem);

        chartPositionScaleUsageMenuItem.setText("PositionScaleUsage");
        chartPositionScaleUsageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartPositionScaleUsageMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(chartPositionScaleUsageMenuItem);

        chartPortfolioScaleUsageMenuItem.setText("PortfolioScaleUsage");
        chartPortfolioScaleUsageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartPortfolioScaleUsageMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(chartPortfolioScaleUsageMenuItem);

        openPositionValuesMenuItem.setText("OpenPositionValues");
        openPositionValuesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPositionValuesMenuItemActionPerformed(evt);
            }
        });
        chartMenu.add(openPositionValuesMenuItem);

        openPortfolioPositionValues.setText("OpenPortfolioPositionValues");
        openPortfolioPositionValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPortfolioPositionValuesActionPerformed(evt);
            }
        });
        chartMenu.add(openPortfolioPositionValues);

        traderMenuBar.add(chartMenu);

        setJMenuBar(traderMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1007, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(51, 51, 51)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(portfolioNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                    .addComponent(jLabel12)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(liqValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(availFundsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(tradingModeLable, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(33, 33, 33))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                        .addComponent(jLabel13)
                                                        .addGap(52, 52, 52)))
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel14)
                                                        .addGap(166, 166, 166)
                                                        .addComponent(jLabel3))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(tickerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(144, 144, 144)
                                                        .addComponent(tradeListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(500, 500, 500)
                                                .addComponent(sellCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tickersLabel))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(positionBiasLable, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(55, 55, 55)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel16)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel15)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(12, 12, 12)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addComponent(jLabel7)
                                                            .addGroup(layout.createSequentialGroup()
                                                                .addComponent(tradeTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 0, Short.MAX_VALUE)))
                                                        .addGap(63, 63, 63)
                                                        .addComponent(showTradeDescriptioinsRadioButton))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addGap(18, 18, 18)
                                                        .addComponent(moneySavedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(439, 439, 439)
                                                        .addComponent(backTestOneTickerButton)
                                                        .addGap(34, 34, 34)
                                                        .addComponent(buyCriteriaButton, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressLable, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(99, 99, 99)
                                .addComponent(startStopButton)
                                .addGap(191, 191, 191)
                                .addComponent(pauseButton)
                                .addGap(191, 191, 191)
                                .addComponent(closeButton)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(573, 573, 573)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(795, 795, 795)
                        .addComponent(jLabel5)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(tickersLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(positionBiasLable, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(availFundsLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(liqValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel16)
                                    .addComponent(moneySavedLabel)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 26, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tradeTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15))
                                .addGap(26, 26, 26)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(portfolioNameLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tradingModeLable, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel13))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel14))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(tradeListComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tickerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(buyCriteriaButton)
                                    .addComponent(sellCriteriaButton)
                                    .addComponent(backTestOneTickerButton))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(showTradeDescriptioinsRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(8, 8, 8)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(progressLable, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(jLabel5)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startStopButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pauseButton)
                        .addComponent(closeButton)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public String userSelectedTicker = null;
    public boolean userSelectedBuy = false;
    public boolean userSelectedCancel = false;
    public boolean userSelectedAutoTest = false;
    public boolean userSelectedSell = false;
    public String userSelectedSingleTradeTicker = null;
    public boolean start = false;
    public boolean startTrader = false;
    public SlopeTrader actTrader = null;
    public int numberOfShares = 50;
    //added 5/27/15 to tell us if this portfolio is long or short
    public String longOrShort = slopeDefs.oBiasLongStr;
    public boolean userSelectedPause = false;
    public double userSelectedPrunePercent = 0;
    public boolean userSelectedAllPositions = false;
    public boolean userSelectedLiquidateAll = false;
    public boolean userSelectedShowTradeDescription = false;
    private void startStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopButtonActionPerformed
        // TODO add your handling code here:
        //toggle
        start = !start;
        //see if first time or we re-starting..
        if ((start == true) && (actTrader == null) && (startTrader == false)) {
            //means first time...
            startTrader = true;
            actTrader = new SlopeTrader();
        } else if ((start == true) && (actTrader != null) && startTrader == false){
            startTrader = true;
            clearTraderStatusTable();
            actTrader = new SlopeTrader();
        }else if (start == false){
            startTrader = false;
        }
    }//GEN-LAST:event_startStopButtonActionPerformed
    private void clearTraderStatusTable(){
        int rowCount = traderStatusTable.getRowCount();
        int colCount = traderStatusTable.getColumnCount();
        for (int il = 0; il < rowCount; il++){
            for (int ic = 0; ic < colCount; ic++) {
                traderStatusTable.getModel().setValueAt("", il, ic);
            }
        }
    }
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        System.out.println("closing slopeTrader..");
        actChain.cancelChainStreams();
        actPositions.state = slopeDefs.oREADY;
        tradeTickets.tradeTicketFile.closeWrFile();
        //wfs here
        actTrader = null;
        if(actTradeActivity != null){
            actTradeActivity.stopIt();
            myUtils.delay(200);
            if(actTradeActivity.haveWeStoppedRunning() == true){
                System.out.println("TradeActivity has stopped.");
            }else{
                System.out.println("TradeActivity has NOT stopped.");
            }
            actTradeActivity = null;
        }        
        
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void tradeListComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tradeListComboBoxActionPerformed
        // TODO add your handling code here:
        userSelectedSingleTradeTicker = tradeListComboBox.getSelectedItem().toString();
        userSelectedTicker = userSelectedSingleTradeTicker;
        tickerTextField.setText(userSelectedTicker);
    }//GEN-LAST:event_tradeListComboBoxActionPerformed

    private void displayPositionChart(String forThisTicker){
        //display position chart..
               
        int port = 0;
        Chart chart;
        int days = 0;
        double buyHold = 0.0; 
        double runPl = 0.0;
        double openValue = 0.0;
        double openValueOverShoot = 0.0;
        int day = 0;
        int sharesTraded = 0;
        String plLongsStr = "PlLongs:";
        String plShortsStr = "PlShorts:";
        double plLongs = 0.0;
        double plShorts = 0.0;
        double maxPl = 0.0;
        
        XYTextAnnotation textAnnotationsPlLongs; 
        XYTextAnnotation textAnnotationsPlShorts;
        
        if ((port = hd.searchForTicker(forThisTicker)) != -1){           
            System.out.println("\nport number is: " + port + " for ticker: " + forThisTicker);
            sharesTraded = hd.getSharesTradedForBackTesting(port);
            //chart = new Chart(forThisTicker + " (" + Integer.toString(sharesTraded) + " , OS% " + hd.getOverShootSharesPercent(port)+ ")");
            chart = new Chart(forThisTicker + 
                             " (" + Integer.toString(sharesTraded) +
                             " , OS% " + hd.getOverShootSharesPercent(port) +
                             ", Yr " + userSelectedBackTestYear + ")");
            days = hd.getNumOfSlopes(port);
            maxPl = hd.getMaxGainForYear(port);
            
            for(day = 0; day < days; day++){
                buyHold = hd.getBuyHoldValue(port, day);
                chart.buyHoldPlSeries.addOrUpdate(day, buyHold);
                runPl = hd.getRunningPl(port, day);
                chart.runningPlSeries.addOrUpdate(day, runPl);
                openValue = hd.getOpenPosValue(port, day);
                openValueOverShoot = hd.getOpenPosValueOverShoot(port, day);
                chart.runningPlPlusOpenSeries.addOrUpdate(day, (openValue + runPl + openValueOverShoot));               
            }
            //text annotations for profit/loss for both longs and shorts..
            textAnnotationsPlLongs = new XYTextAnnotation(plLongsStr, days-1, maxPl);
            textAnnotationsPlShorts = new XYTextAnnotation(plShortsStr, days-1, (maxPl/2));
            plLongs = myUtils.roundMe(hd.getPlLongs(port), 2);
            plShorts = myUtils.roundMe(hd.getPlShorts(port), 2);
            textAnnotationsPlLongs.setText(plLongsStr+plLongs);
            chart.plot.addAnnotation(textAnnotationsPlLongs);
            textAnnotationsPlShorts.setText(plShortsStr+plShorts);
            chart.plot.addAnnotation(textAnnotationsPlShorts);
        }else{
            commonGui.postInformationMsg("Ticker not found. Maybe not run yet?");
        }
        
    }
    public void dispPortfolioStatistics() {
        String perfData = "";
        perfData = hd.analizeDataPositions("");
        commonGui.postInformationMsg("Historical Portfolio Performance: \n " + perfData);

        perfFileWr.write(perfData);

    }
    public String dispStatisticsToStr(String forThisTicker) {
        String op = null;
        String outPutStr = "";
        appendToStr strToWr = new appendToStr();
        strToWr.setStrToWr(outPutStr);
        boolean isPosOpen = false;
        double runningPl = 0.0;
        double runningPlPlusOpen = 0.0;
        double openOverShootPl = 0.0;
        double openPl = 0.0;
        double profit = 0.0;
        double initialPositionValue = 0.0;
        double runningPlPercent = 0.0;
        double overAllPl = 0.0;
        double runningPlPlusOpenPosPercent = 0.0;
        DecimalFormat df = new DecimalFormat("##.##");
        
        int position = 0;
        double maxRunningProfit = 0.0;
        double maxRunningLoss = 0.0;
        double buyHoldPl = 0.0;
        double buyHoldPlPercent = 0.0;
        String maxRunningProfitDate = "";
        String maxRunningLossDate = "";
        int numOfTrades = 0;
        boolean goodness = false;
        int overShootPercent = 0;
        strToWr.appendMe("\n\nTicker: " + forThisTicker);

        if ((position = hd.searchForTicker(forThisTicker)) != -1) {
            overShootPercent = hd.getOverShootSharesPercent(position);
            runningPl += hd.getRunningPl(position);
            openPl += hd.getOpenPosValue(position);
            openOverShootPl += hd.getOpenPosValueOverShoot(position);
            //use day 0, should be same for all days..
            initialPositionValue = (hd.getInitialStockPrice1YearAgo(position) * hd.getSharesTradedForBackTesting(position));
            overAllPl = (runningPl + openPl + openOverShootPl);
            if (runningPl != 0.0) {
                runningPlPercent = myUtils.truncate((runningPl/initialPositionValue), 2);
                runningPlPlusOpenPosPercent = myUtils.truncate(overAllPl / (initialPositionValue), 2);
            }
            
            maxRunningProfit = hd.getMaxGainForYear(position);
            maxRunningProfitDate = hd.getMaxGainForYearDate(position);
            maxRunningLoss = hd.getMinGainForYear(position);
            maxRunningLossDate = hd.getMinGainForYearDate(position);
            numOfTrades = hd.getNumOfTrades(position);
            buyHoldPl = hd.getBuyHoldValue(position);
            buyHoldPlPercent = myUtils.roundMe(hd.getBuyHoldValuePercent(position), 2);
            goodness = true;
        } else if (userSelectedStreamToFile == false) {
            //don't display this msg when streaming to file..interupts flow..
            commonGui.postInformationMsg("Ticker not found. Maybe not run yet?");
        } else {

        }

        if (goodness == true) {
            strToWr.appendMe("\n OS%: " + overShootPercent + "Year: " + userSelectedBackTestYear + "\n" +
                    "\n Profit: " + myUtils.truncate(runningPl, 2) + " (" + (runningPlPercent * 100) + "%)"
                    + "\n Profit+Open: " + myUtils.truncate(overAllPl, 2) + " (" +(myUtils.truncate((runningPlPlusOpenPosPercent * 100), 2)) + "%)"       
                + "\n Max Profit: " + myUtils.truncate(maxRunningProfit, 2)
                + "\n Max Profit Date: " + maxRunningProfitDate
                + "\n Max Loss: " + myUtils.truncate(maxRunningLoss, 2)
                + "\n Max Loss Date: " + maxRunningLossDate
                + "\n Trades: " + numOfTrades
                + "\n ProfitPerTrade: " + myUtils.truncate((runningPl/numOfTrades), 2)
                + "\n InitialInvestment: " + myUtils.truncate(initialPositionValue, 2)
                + "\n CurrentLiquidatingValue: " + myUtils.truncate(initialPositionValue + overAllPl, 2)
                + "\n BuyHoldPl: " + myUtils.truncate(buyHoldPl, 2) + " (" + myUtils.truncate((buyHoldPlPercent * 100), 2) + "%)"
                
            );                            
        //wfshere
            if(goodness == true){
                outPutStr = strToWr.getStr();
            }else{
                outPutStr = null;
            }
            
            return outPutStr;
        }
            
        outPutStr = strToWr.getStr();
        return outPutStr;
    }
    private void traderStatusTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_traderStatusTableMouseClicked
        // TODO add your handling code here:
        int row = 0;
        String tickerSelected = null;
        String pivotHistSlopeStr = "";
        String pivotCurrentSlopeStr = "";
        String posStatisticsStr = "";
        try {
            row = traderStatusTable.getSelectedRow();
            System.out.println("traderStatusTable: " + "row is : " + row);
        }catch(Exception e) {
            System.out.println("traderStatusTableMouseClicked: Exception!!" + evt);   
        }
        
        tickerSelected = String.valueOf(traderStatusTable.getModel().getValueAt(row, actTrader.oTicker ));
        System.out.println("traderStatusTable: " + "ticker is : " + tickerSelected);
        if ((!tickerSelected.equals(null)) && (!tickerSelected.equals(slopeDefs.FAILED))){
            //wfshere
            if (userSelectedClickPivotStatisticsOn == true) {
                pivotHistSlopeStr = dispPivotSlopeDataToStr(allSlopes, row);
                pivotCurrentSlopeStr = dispPivotCurrentSlopeDataToStr(tradeTickets, row);
                if (!pivotHistSlopeStr.equals("") && !pivotCurrentSlopeStr.equals("")) {
                    commonGui.postInformationMsg("Historical: \n " + pivotHistSlopeStr + "\n\nCurrent:\n" + pivotCurrentSlopeStr);
                } else {
                    commonGui.postInformationMsg("Wait for this Ticker to be updated..and try again...");
                }
            }
            if (userSelectedClickChartOn == true){
                displayPositionChart(tickerSelected);
            }
            if (userSelectedClickStatisticsOn == true){
                posStatisticsStr = dispStatisticsToStr(tickerSelected);
                commonGui.postInformationMsg("Historical: \n " + posStatisticsStr);
            }
            if (userSelectedStreamToFile == true){
                posStatisticsStr = dispStatisticsToStr(tickerSelected);
                perfFileWr.write(posStatisticsStr);
            }
        }
        
    }//GEN-LAST:event_traderStatusTableMouseClicked

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        // TODO add your handling code here:
        userSelectedPause = !userSelectedPause;
    }//GEN-LAST:event_pauseButtonActionPerformed


    private void buyCriteriaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buyCriteriaButtonActionPerformed
        // TODO add your handling code here:
        
        int actTicket;
        
        if (buyCriteria == null){
            buyCriteria = new BuyCriteriaDialogForm(new javax.swing.JFrame(), true);
        }
        buyCriteria.setActiveTicker(userSelectedTicker);       
        buyCriteria.setVisible(true);
        if (buyCriteria.getGoodInput() == true){
            buyWhen = new BuyWhen();            
            buyWhen.exitWhen = buyCriteria.getUserCriteriaStr();  
            buyWhen.ticker = buyCriteria.getTicker();
            setBuyCriteria(buyWhen);
        }else{
            buyCriteria = null; 
        }
        System.out.println("\ndone.");
        
    }//GEN-LAST:event_buyCriteriaButtonActionPerformed

    private void tickerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tickerTextFieldActionPerformed

        // TODO add your handling code here:
        userSelectedTicker = tickerTextField.getText().toUpperCase();
        //search for it in TradeList...
        int cnt = tradeListComboBox.getItemCount();
        int itemNumber = 0;
        boolean found = false;
        while ((cnt > 0) && (!found)) {
            if (userSelectedTicker.equals(tradeListComboBox.getItemAt(itemNumber))){
                found = true;
            }else{
                cnt--;
                itemNumber++;
            }
        }
        if (found == true){
            tradeListComboBox.setSelectedIndex(itemNumber);
        }else{
            tickerTextField.setText(userSelectedTicker);
        }
        
    }//GEN-LAST:event_tickerTextFieldActionPerformed
    public class SellCriteria{
       public double stopLossPercent = 0.0;
       public double lockGainPercent = 0.0;
       public boolean liquidateAll = false;
       public boolean setAllTickers = false;
       public String exitWhen = null;
       public boolean isStopLossSet = false;
       public boolean isLockGainSet = false;
       public String ticker;
        public SellCriteria(){
            
        }
    }
    public class BuyWhen{
       public String exitWhen = null;
       
       public String ticker;
        public BuyWhen(){
            
        }
    }
    private void setExitCriteria(SellCriteria criteria) {
        String ticker = "";
        int idx = 0;
        int cnt = 0;
        if (criteria.liquidateAll == true) {
            if (commonGui.postConfirmationMsg("LiquidateAll?") == 0) {
                for (idx = 0; idx < tradeTickets.numOfTickets; idx++) {
                    
                    if (longOrShort.equals(slopeDefs.oBiasLongStr) && 
                       (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)) &&
                       (tradeTickets.tickets[idx].sharesAtHand != 0)) {
                        tradeTickets.tickets[idx].liquidateAll = true;
                        cnt++;
                    }else if (longOrShort.equals(slopeDefs.oBiasShortStr) && 
                             (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD)) &&
                             (tradeTickets.tickets[idx].sharesAtHand != 0)){
                        tradeTickets.tickets[idx].liquidateAll = true;
                        cnt++;    
                    }
                }
                userSelectedLiquidateAll = true;
                commonGui.postInformationMsg("All Open Positons Set to Liquidate. (" + cnt + ").");
                return;
            } else {
                commonGui.postInformationMsg("Not Liquidating.");
            }

        }else{
            userSelectedLiquidateAll = false;
        }
        //do lockGain first...
        if ((criteria.setAllTickers == true) && (criteria.isLockGainSet == true)) {
            if (commonGui.postConfirmationMsg("Set GainLock for All Open Positions to: " + myUtils.truncate((criteria.lockGainPercent), 2) + "% ?") == 0) {
                for (idx = 0; idx < tradeTickets.numOfTickets; idx++) {
                    if ((longOrShort.equals(slopeDefs.oBiasLongStr)) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))) {
                        tradeTickets.tickets[idx].lockGain = true;
                        tradeTickets.tickets[idx].lockGainPercent = myUtils.truncate((criteria.lockGainPercent), 2);
                        cnt++;
                    }else if ((longOrShort.equals(slopeDefs.oBiasShortStr)) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))) {
                        tradeTickets.tickets[idx].lockGain = true;
                        tradeTickets.tickets[idx].lockGainPercent = myUtils.truncate((criteria.lockGainPercent), 2);
                        cnt++;
                    }
                }
                userSelectedAllPositions = true;
                commonGui.postInformationMsg("All Open Positons Set (" + cnt + ").");
                System.out.println("\nset all positions to : " + (criteria.lockGainPercent) + "%");
            } else {
                commonGui.postInformationMsg("Not set.");
            }
        } else if(criteria.isLockGainSet == true) {
            ticker = criteria.ticker;
            if (commonGui.postConfirmationMsg("Set LockGain " + ticker + " Position to: " + (criteria.lockGainPercent) + "% ?") == 0) {
                if ((idx = tradeTickets.findTicket(ticker)) != -1) {
                    System.out.println("found idx for " + ticker + " it is: " + idx);
                    System.out.println("setting lockGain true and lockGainPercent.");
                    tradeTickets.tickets[idx].lockGain = true;
                    tradeTickets.tickets[idx].lockGainPercent = myUtils.truncate((criteria.lockGainPercent), 2);;
                    commonGui.postInformationMsg("Done.");
                    System.out.println("\nset " + ticker + " position to : " + (criteria.lockGainPercent) + "%");
                } else {
                    System.out.println("index not found for ticker: " + ticker + "?");
                    commonGui.postInformationMsg("index not found for ticker: " + ticker + "??");
                }

            } else {
                commonGui.postInformationMsg("Not set.");
            }
        }
        //now stopLoss...
        if ((criteria.setAllTickers == true) && (criteria.isStopLossSet == true)) {
            if (commonGui.postConfirmationMsg("Set StopLoss for All Open Positions to: " + myUtils.truncate((criteria.stopLossPercent), 2) + "% ?") == 0) {
                for (idx = 0; idx < tradeTickets.numOfTickets; idx++) {
                    if (longOrShort.equals(slopeDefs.oBiasLongStr) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))) {
                        tradeTickets.tickets[idx].stopLoss = true;
                        tradeTickets.tickets[idx].stopLossPercent = myUtils.truncate((criteria.stopLossPercent), 2);
                        cnt++;
                    }else if (longOrShort.equals(slopeDefs.oBiasShortStr) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))) {
                        tradeTickets.tickets[idx].stopLoss = true;
                        tradeTickets.tickets[idx].stopLossPercent = myUtils.truncate((criteria.stopLossPercent), 2);
                        cnt++;
                    }
                    
                }
                commonGui.postInformationMsg("All Open Positons Set (" + cnt + ").");
                System.out.println("\nset all positions to : " + (criteria.stopLossPercent) + "%");
                userSelectedAllPositions = true;
            } else {
                commonGui.postInformationMsg("Not set.");
            }
        } else if(criteria.isStopLossSet == true) {
            ticker = criteria.ticker;
            if (commonGui.postConfirmationMsg("Set StopLoss " + ticker + " Position to: " + (criteria.stopLossPercent) + "% ?") == 0) {
                if ((idx = tradeTickets.findTicket(ticker)) != -1) {
                    System.out.println("found idx for " + ticker + " it is: " + idx);
                    System.out.println("setting stopLoss true and stopLossPercent.");
                    tradeTickets.tickets[idx].stopLoss = true;
                    tradeTickets.tickets[idx].stopLossPercent = myUtils.truncate((criteria.stopLossPercent), 2);;
                    commonGui.postInformationMsg("Done.");
                    System.out.println("\nset " + ticker + " position to : " + (criteria.stopLossPercent) + "%");
                } else {
                    System.out.println("index not found for ticker: " + ticker + "?");
                    commonGui.postInformationMsg("index not found for ticker: " + ticker + "??");
                }

            } else {
                commonGui.postInformationMsg("Not set.");
            }
        }

    }
    private void setBuyCriteria(BuyWhen criteria) {
        
    }
    private void sellCriteriaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sellCriteriaButtonActionPerformed
        // TODO add your handling code here:
        int actTicket;
        
        if (sellCriteria == null){
            sellCriteria = new sellCriteriaDialogForm(new javax.swing.JFrame(), true);
        }
        sellCriteria.setActiveTicker(userSelectedTicker);
        if ((actTicket = tradeTickets.findTicket(userSelectedTicker)) != -1){
            sellCriteria.setCurrentLockGainPercent(tradeTickets.tickets[actTicket].lockGainPercent);
            sellCriteria.setCurrentStopLossPercent(tradeTickets.tickets[actTicket].stopLossPercent);
            sellCriteria.setLiquidateAll(userSelectedLiquidateAll);
            sellCriteria.setAffectAllTickers(userSelectedAllPositions);  
            sellCriteria.setCurrentLockGain(tradeTickets.tickets[actTicket].lockGain);
            sellCriteria.setCurrentStopLoss(tradeTickets.tickets[actTicket].stopLoss);
        }
        
        sellCriteria.setVisible(true);
        if (sellCriteria.getGoodInput() == true){
            sellWhen = new SellCriteria();
            sellWhen.stopLossPercent = sellCriteria.getStopLossPercent();
            sellWhen.lockGainPercent = sellCriteria.getLockGainPercent();
            sellWhen.exitWhen = sellCriteria.getUserCriteriaStr(); 
            sellWhen.setAllTickers = userSelectedAllPositions = sellCriteria.getSetAllTickers();
            sellWhen.liquidateAll = userSelectedLiquidateAll = sellCriteria.getLiquidateAll();
            sellWhen.isStopLossSet = sellCriteria.isStopLossValid();
            sellWhen.isLockGainSet = sellCriteria.isLockGainValid();  
            sellWhen.ticker = sellCriteria.getTicker();
            setExitCriteria(sellWhen);
        }else{
            sellCriteria = null; 
        }
        System.out.println("\ndone.");
    }//GEN-LAST:event_sellCriteriaButtonActionPerformed

    private void displayMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayMenuActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_displayMenuActionPerformed

    private void displayCriteriaMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayCriteriaMenuItemActionPerformed
        // TODO add your handling code here:
        if (Config != null){
            Config.displayCriteria();    
        }else{
            Config = new ConfigDialogForm(new javax.swing.JFrame(), true);
            Config.create(currentBias);
            actTraderConfigParams = Config.actConfig;
            actTraderConfigParams.setPrefixDirectory(actPositions.pathNamePrefix);
            actTraderConfigParams.rdFromFile();           
            Config.setTraderConfigParams(actTraderConfigParams);
            Config.setConfigNow();
            //Config.setVisible(false);
            Config.displayCriteria();
        }
        
    }//GEN-LAST:event_displayCriteriaMenuItemActionPerformed
    traderConfigParams  actTraderConfigParams = null;
    private void positionBiasLableAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_positionBiasLableAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_positionBiasLableAncestorAdded

    private void backTestOneTickerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestOneTickerButtonActionPerformed
        // TODO add your handling code here:
        
        if ((userSelectedTicker != null) && (userSelectedBackTesting == true) && (commonGui.postConfirmationMsg("BackTestOneTicker: " + userSelectedTicker) == 0)){
            userSelectedBackTestOneTicker = true;           
        }else{
            if(userSelectedBackTesting == true){
                commonGui.postInformationMsg("Enter Ticker!"); 
            }else{
                commonGui.postInformationMsg("Must Be in BackTest mode!"); 
            }               
            userSelectedBackTestOneTicker = false;  
        }
            
        
    }//GEN-LAST:event_backTestOneTickerButtonActionPerformed

    private class Chart {

        XYSeries runningPlSeries;
        XYSeries runningPlPlusOpenSeries;
        XYSeries buyHoldPlSeries;
        XYSeriesCollection dataset;
        String plLongsStr = "PlLongs:";
        String plShortsStr = "PlShorts:";
        double plLongs = 0.0;
        double plShorts = 0.0;
        XYPlot plot;
        
        //XYPlot plot = this.
        JFreeChart chart;
        ChartFrame frame1;       
        TimePeriodValues timePeriodValues;
        private Chart(String ticker) {
            runningPlSeries = new XYSeries("Running PL Value");
            runningPlPlusOpenSeries = new XYSeries("Running PL + OpenPositions Value");
            buyHoldPlSeries = new XYSeries("BuyHold Value");                       
            dataset = new XYSeriesCollection();
            dataset.addSeries(runningPlSeries);
            dataset.addSeries(runningPlPlusOpenSeries);
            dataset.addSeries(buyHoldPlSeries); 
            
            chart = ChartFactory.createXYLineChart("BackTest Position Performance", "TradingDay", "PositionValue",
                    dataset, PlotOrientation.VERTICAL, true, true, false);
            
            frame1 = new ChartFrame(ticker + " Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(1000, 600);
            plot = this.chart.getXYPlot(); 
        }
        XYDataset createDataSet(){
            

        return dataset;
        }
        
    }
     private class Chart1 {
        XYSeries closePriceSeries;
        XYSeries volocitySeries;
        XYSeries ma10DaySeries;
        XYSeries buyHoldPlSeries;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;       
        TimePeriodValues timePeriodValues;
        private Chart1(String ticker) {
            volocitySeries = new XYSeries("Volocity");
            ma10DaySeries = new XYSeries("10DayMa"); 
            closePriceSeries = new XYSeries("ClosePrice");
            dataset = new XYSeriesCollection();
            dataset.addSeries(volocitySeries);
            dataset.addSeries(ma10DaySeries); 
            dataset.addSeries(closePriceSeries); 
            chart = ChartFactory.createXYLineChart("BackTest Position Volocity", "TradingDay", "Volocity",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            frame1 = new ChartFrame(ticker + " Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(1000, 600);

        }
        XYDataset createDataSet(){
            

        return dataset;
        }
        
    }
    private class ChartPositionsScaleUsage {

        XYSeries posScaleUsageSeries;
        XYSeries posScaleUsageOverShootSeries;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;       
        TimePeriodValues timePeriodValues;
        private ChartPositionsScaleUsage(String ticker) {           
            posScaleUsageOverShootSeries = new XYSeries("ScaleUsageOverShoot");
            posScaleUsageSeries = new XYSeries("ScaleUsage");
            dataset = new XYSeriesCollection();              
            dataset.addSeries(posScaleUsageOverShootSeries);
            dataset.addSeries(posScaleUsageSeries);
            chart = ChartFactory.createXYLineChart("BackTest Position Scale Usage", "TradingDay", "ScaleUsage",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            frame1 = new ChartFrame(ticker + " Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(1000, 600);

        }
        XYDataset createDataSet(){
            

        return dataset;
        }
        
    } 
    
    private class ChartPositionsOpenPosValueUsage {

        XYSeries openPosValueSeries;
        XYSeries openPosValuesOverShootSeries;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;       
        TimePeriodValues timePeriodValues;
        private ChartPositionsOpenPosValueUsage(String ticker) {            
            openPosValuesOverShootSeries = new XYSeries("OpenPositionOverShootValues(Shorts)");
            openPosValueSeries = new XYSeries("OpenPositionValues(Longs)");
            dataset = new XYSeriesCollection();              
            dataset.addSeries(openPosValuesOverShootSeries);
            dataset.addSeries(openPosValueSeries);
            chart = ChartFactory.createXYLineChart("BackTest Open Position Values", "TradingDay", "OpenPosValues",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            frame1 = new ChartFrame(ticker + " Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(1000, 600);

        }
        XYDataset createDataSet(){
            

        return dataset;
        }
        
    } 
    
    private class ChartPortfolioScaleUsage {

        XYSeries portfolioScaleUsageSeries; 
        XYSeries portfolioScaleOverShootUsageSeries;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;       
        TimePeriodValues timePeriodValues;
        private ChartPortfolioScaleUsage(String ticker) {
            
            portfolioScaleOverShootUsageSeries = new XYSeries("ScaleUsageOverShoot(Short)");
            portfolioScaleUsageSeries = new XYSeries("ScaleUsage(Long)");
            
            dataset = new XYSeriesCollection();
            dataset.addSeries(portfolioScaleOverShootUsageSeries);
            dataset.addSeries(portfolioScaleUsageSeries);         
            chart = ChartFactory.createXYLineChart("BackTest Scale Usage", "TradingDay", "Scale Usage",
                    dataset, PlotOrientation.VERTICAL, true, true, false);

            frame1 = new ChartFrame(ticker + " Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(1000, 600);

        }
        XYDataset createDataSet(){
            

        return dataset;
        }
        
    }
    private void chartMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chartMenuActionPerformed

    private void chartPositionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartPositionMenuItemActionPerformed
        // TODO add your handling code here:
        String ticker;       
        int port = 0;
        Chart chart;
        int days = 0;
        ticker = commonGui.getUserInput("Ticker to Chart:", "");
        double buyHold = 0.0; 
        double runPl = 0.0;
        double openValue = 0.0;
        double openValueOverShoot = 0.0;
        int day = 0;
        int sharesTraded = 0;
        String plLongsStr = "PlLongs:";
        String plShortsStr = "PlShorts:";
        double plLongs = 0.0;
        double plShorts = 0.0;
        double maxPl = 0.0;
        
        XYTextAnnotation textAnnotationsPlLongs; 
        XYTextAnnotation textAnnotationsPlShorts;
        
        if ((port = hd.searchForTicker(ticker)) != -1){           
            System.out.println("\nport number is: " + port + " for ticker: " + ticker);
            sharesTraded = hd.getSharesTradedForBackTesting(port);
            chart = new Chart(ticker + 
                             " (" + Integer.toString(sharesTraded) +
                             " , OS% " + hd.getOverShootSharesPercent(port) +
                             ", Yr " + userSelectedBackTestYear + ")");
            days = hd.getNumOfSlopes(port);
            maxPl = hd.getMaxGainForYear(port);
            
            for(day = 0; day < days; day++){
                buyHold = hd.getBuyHoldValue(port, day);
                //remove..
                System.out.println("\nbuyHold: " + buyHold + " day: " + day);
                chart.buyHoldPlSeries.addOrUpdate(day, buyHold);
                runPl = hd.getRunningPl(port, day);
                chart.runningPlSeries.addOrUpdate(day, runPl);
                openValue = hd.getOpenPosValue(port, day);
                openValueOverShoot = hd.getOpenPosValueOverShoot(port, day);
                chart.runningPlPlusOpenSeries.addOrUpdate(day, (openValue + runPl + openValueOverShoot));               
            }
            //text annotations for profit/loss for both longs and shorts..
            textAnnotationsPlLongs = new XYTextAnnotation(plLongsStr, days-1, maxPl);
            textAnnotationsPlShorts = new XYTextAnnotation(plShortsStr, days-1, (maxPl/2));
            plLongs = myUtils.roundMe(hd.getPlLongs(port), 2);
            plShorts = myUtils.roundMe(hd.getPlShorts(port), 2);
            textAnnotationsPlLongs.setText(plLongsStr+plLongs);
            chart.plot.addAnnotation(textAnnotationsPlLongs);
            textAnnotationsPlShorts.setText(plShortsStr+plShorts);
            chart.plot.addAnnotation(textAnnotationsPlShorts);
        }else{
            commonGui.postInformationMsg("Ticker not found.");
        }
                
    }//GEN-LAST:event_chartPositionMenuItemActionPerformed

    private void chartPortfolioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartPortfolioMenuItemActionPerformed
        // TODO add your handling code here:
        Chart chart;
        int days = 0;
        int day = 0;
        int numOfPositions = 0;
        double buyHold = 0.0; 
        double runPl = 0.0;
        double openValue = 0.0;
        double openValueOverShoot = 0.0;
        int position = 0;
        String plLongsStr = "PlLongs:";
        String plShortsStr = "PlShorts:";
        double plLongs = 0.0;
        double plShorts = 0.0;
        XYTextAnnotation textAnnotationsPlLongs; 
        XYTextAnnotation textAnnotationsPlShorts;
        double maxPl = 0.0;
        double minPl = 0.0;
        double maxPlLast = 0.0;
        double minPlLast = 0.0;
        int lastDay = 0;
        int maxDays = hd.getMaxDaysOfAllPositions();
        
        if ((numOfPositions = hd.getNumOfPositionsInPortfolio()) != 0){             
            chart = new Chart(actPositions.getPortfolioAlias());            
            for(day = 0; day < maxDays; day++){
                for(position = 0; position < numOfPositions; position++){
                    if(day < (lastDay = hd.getNumOfSlopes(position))){
                        buyHold += hd.getBuyHoldValue(position, day);                    
                        runPl += hd.getRunningPl(position, day);
                        openValue += hd.getOpenPosValue(position, day); 
                        openValueOverShoot += hd.getOpenPosValueOverShoot(position, day);
                    }else{
                        /*a position could have fewer days than others depending on when they were listed.
                          in this case just add the last day's same value to sum so it does not take down all.
                        */                        
                        buyHold += hd.getBuyHoldValue(position, (lastDay - 1));                    
                        runPl += hd.getRunningPl(position, (lastDay - 1));
                        openValue += hd.getOpenPosValue(position, (lastDay - 1));
                        openValueOverShoot += hd.getOpenPosValueOverShoot(position, (lastDay - 1));
                    }                  
                }                
                chart.buyHoldPlSeries.addOrUpdate(day, buyHold);
                chart.runningPlSeries.addOrUpdate(day, runPl);
                chart.runningPlPlusOpenSeries.addOrUpdate(day, (openValue + runPl + openValueOverShoot)); 
                buyHold = 0.0;
                runPl = 0.0;
                openValue = 0.0;
                openValueOverShoot = 0.0;
            }
            //cycle thru all positions adding up pl by long/short
            for(position = 0; position < numOfPositions; position++){
                maxPl = hd.getMaxGainForYear(position);
                if (maxPl > maxPlLast){
                    maxPlLast = maxPl;
                }               
                minPl = hd.getMinGainForYear(position);
                if (minPl < minPlLast){
                    minPlLast = minPl;
                }
                plLongs += hd.getPlLongs(position);
                plShorts += hd.getPlShorts(position);
            }
            //text annotations for profit/loss for both longs and shorts..
            //use maxPl / 2 so it scales to specific charts correctly.
            textAnnotationsPlLongs = new XYTextAnnotation(plLongsStr, days-1, (maxPlLast));
            textAnnotationsPlShorts = new XYTextAnnotation(plShortsStr, days-1, (minPlLast));
            plLongs = myUtils.roundMe(plLongs, 2);
            plShorts = myUtils.roundMe(plShorts, 2);
            textAnnotationsPlLongs.setText(plLongsStr+plLongs);
            chart.plot.addAnnotation(textAnnotationsPlLongs);
            textAnnotationsPlShorts.setText(plShortsStr+plShorts);
            chart.plot.addAnnotation(textAnnotationsPlShorts);
            if (userSelectedStreamToFile == true){
                dispPortfolioStatistics();
            }
        }else{
            commonGui.postInformationMsg("Portfolio not found.");
        }
                
    }//GEN-LAST:event_chartPortfolioMenuItemActionPerformed

    private void chartVolocityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartVolocityMenuItemActionPerformed
        // TODO add your handling code here:
        Chart1 chart;
        int days = 0;
        int day = 0;
        String ticker;
        double buyHold = 0.0; 
        double runPl = 0.0;
        double openValue = 0.0;
        double volocity = 0;
        double ma10ma = 0;
        double closingPrice = 0.0;
        int pos = 0;
        ticker = commonGui.getUserInput("Ticker to Chart:", "");       
        days = hd.getNumOfSlopes(0);
        chart = new Chart1(ticker + ": volocity");
        if ((pos = hd.searchForTicker(ticker)) != -1){
            for (day = 0; day < days; day++) {
                volocity = hd.getSlopeValue(pos, day);
                chart.volocitySeries.addOrUpdate(day, (volocity + 50));
                ma10ma = hd.get10DayMa(pos, day);
                chart.ma10DaySeries.addOrUpdate(day, ma10ma);
                closingPrice = hd.getClosePrice(pos, day);
                chart.closePriceSeries.addOrUpdate(day, closingPrice);
        }
        }else{
            commonGui.postInformationMsg("Ticker not found.");
        }
        
    }//GEN-LAST:event_chartVolocityMenuItemActionPerformed

    private void chartPositionScaleUsageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartPositionScaleUsageMenuItemActionPerformed
        // TODO add your handling code here:
        String ticker;       
        int port = 0;
        ChartPositionsScaleUsage chart;
        int days = 0;
        ticker = commonGui.getUserInput("Ticker to Chart:", "");
        int day = 0;
        int posCount = 0;
        int posCountOvershoot = 0;
        int overShootPercent = 0;
        if ((port = hd.searchForTicker(ticker)) != -1){           
            System.out.println("\nport number is: " + port + " for ticker: " + ticker);
            overShootPercent = hd.getOverShootSharesPercent(port);
            chart = new ChartPositionsScaleUsage(ticker + " OS:" + overShootPercent + " Yr: " + userSelectedBackTestYear);
            days = hd.getNumOfSlopes(port);
            
            for(day = 0; day < days; day++){
                posCount = hd.getOpenPositionCnt(port, day);
                posCountOvershoot = hd.getOpenPositionCntOvershoot(port, day);
                //show as negative values..
                posCountOvershoot *= -1;
                chart.posScaleUsageSeries.addOrUpdate(day, posCount); 
                chart.posScaleUsageOverShootSeries.addOrUpdate(day, posCountOvershoot);
            }
            
        }else{
            commonGui.postInformationMsg("Ticker not found.");
        }
                
    }//GEN-LAST:event_chartPositionScaleUsageMenuItemActionPerformed

    private void chartPortfolioScaleUsageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartPortfolioScaleUsageMenuItemActionPerformed
        // TODO add your handling code here:
        ChartPortfolioScaleUsage chart;
        int days = 0;
        int day = 0;
        int numOfPositions = 0;        
        int position = 0;
        int openPosCnt = 0;
        int openPosCntOverShoot = 0;
        int maxDays = hd.getMaxDaysOfAllPositions();
        if ((numOfPositions = hd.getNumOfPositionsInPortfolio()) != 0){ 
            days = hd.getNumOfSlopes(0);
            chart = new ChartPortfolioScaleUsage(actPositions.getPortfolioAlias());
            for(day = 0; day < maxDays; day++){
                for(position = 0; position < numOfPositions; position++){
                    //days = hd.getNumOfSlopes(position);
                    if (day < hd.getNumOfSlopes(position)){
                        openPosCnt += hd.getOpenPositionCnt(position, day);
                        openPosCntOverShoot += hd.getOpenPositionCntOvershoot(position, day);
                    }                    
                }
                //show as negative values..                
                openPosCntOverShoot *= -1;
                chart.portfolioScaleUsageSeries.addOrUpdate(day, openPosCnt); 
                chart.portfolioScaleOverShootUsageSeries.addOrUpdate(day, openPosCntOverShoot); 
                openPosCnt = 0;
            }                      
            
        }else{
            commonGui.postInformationMsg("Portfolio not found.");
        }
        
    }//GEN-LAST:event_chartPortfolioScaleUsageMenuItemActionPerformed

    private void openPositionValuesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPositionValuesMenuItemActionPerformed
        // TODO add your handling code here:
        //..//ChartPositionsOpenPosValueUsage
        String ticker;       
        int port = 0;
        ChartPositionsOpenPosValueUsage chart;
        int days = 0;
        ticker = commonGui.getUserInput("Ticker to Chart:", "");
        int day = 0;
        double openPosValue = 0.0;
        double openPosValueOverShoot = 0.0;
        
        if ((port = hd.searchForTicker(ticker)) != -1){           
            System.out.println("\nport number is: " + port + " for ticker: " + ticker);
            
            chart = new ChartPositionsOpenPosValueUsage(ticker);
            days = hd.getNumOfSlopes(port);
            
            for(day = 0; day < days; day++){
                openPosValue = hd.getOpenPosValue(port, day);
                openPosValueOverShoot = hd.getOpenPosValueOverShoot(port, day);
               
                chart.openPosValueSeries.addOrUpdate(day, openPosValue); 
                chart.openPosValuesOverShootSeries.addOrUpdate(day, openPosValueOverShoot);
            }
            
        }else{
            commonGui.postInformationMsg("Ticker not found.");
        }
                        
    }//GEN-LAST:event_openPositionValuesMenuItemActionPerformed

    private void openPortfolioPositionValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPortfolioPositionValuesActionPerformed
        // TODO add your handling code here:
        ChartPositionsOpenPosValueUsage chart;
        int days = 0;
        int day = 0;
        int numOfPositions = 0;        
        int position = 0;
        double openPosValues = 0.0;
        double openPosValuesOverShoot = 0.0;
        
        if ((numOfPositions = hd.getNumOfPositionsInPortfolio()) != 0){ 
            days = hd.getNumOfSlopes(0);
            chart = new ChartPositionsOpenPosValueUsage(actPositions.getPortfolioAlias());
            for(day = 0; day < days; day++){
                for(position = 0; position < numOfPositions; position++){
                    //days = hd.getNumOfSlopes(position);
                    openPosValues += hd.getOpenPosValue(position, day);
                    openPosValuesOverShoot += hd.getOpenPosValueOverShoot(position, day);
                }
               
                chart.openPosValueSeries.addOrUpdate(day, openPosValues); 
                chart.openPosValuesOverShootSeries.addOrUpdate(day, openPosValuesOverShoot); 
                openPosValues = 0.0;
                openPosValuesOverShoot = 0.0;
            }                      
            
        }else{
            commonGui.postInformationMsg("Portfolio not found.");
        }
                
    }//GEN-LAST:event_openPortfolioPositionValuesActionPerformed

    private void clickChartOnRadioButtonItemMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickChartOnRadioButtonItemMenuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_clickChartOnRadioButtonItemMenuActionPerformed

    private void clickPivotStatisticsOnRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickPivotStatisticsOnRadioButtonMenuItemActionPerformed
        // TODO add your handling code here:
        userSelectedClickPivotStatisticsOn = clickPivotStatisticsOnRadioButtonMenuItem.isSelected();
    }//GEN-LAST:event_clickPivotStatisticsOnRadioButtonMenuItemActionPerformed

    private void clickStatisticsOnRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickStatisticsOnRadioButtonMenuItemActionPerformed
        // TODO add your handling code here:
        userSelectedClickStatisticsOn = clickStatisticsOnRadioButtonMenuItem.isSelected();

    }//GEN-LAST:event_clickStatisticsOnRadioButtonMenuItemActionPerformed

    private void clickChartOnRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clickChartOnRadioButtonMenuItemActionPerformed
        // TODO add your handling code here:
        userSelectedClickChartOn = clickChartOnRadioButtonMenuItem.isSelected();

    }//GEN-LAST:event_clickChartOnRadioButtonMenuItemActionPerformed

    private void linearModeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linearModeCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
        if (linearModeCheckBoxMenuItem.isSelected() == true) {
            System.out.println("\nLinear Mode  ON");
            tradingModeLable.setText(backTestingStr);
            linearMode = true;
        } else {
            System.out.println("\nLinear Mode  OFF");
            tradingModeLable.setText(realTradingStr);
            linearMode = false;
        }

    }//GEN-LAST:event_linearModeCheckBoxMenuItemActionPerformed

    /*
    private class Config{
        boolean simulatedTrading = false;
        int lsBias = slopeDefs.oBiasLong;
        int openOn = slopeDefs.oPivot;
        int closeOn = slopeDefs.oPivot;
    }
    */
    private void backtestingMenuItemCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backtestingMenuItemCheckBoxActionPerformed
        // TODO add your handling code here:
        if (backtestingMenuItemCheckBox.isSelected() == true){
            System.out.println("\nbacktesting  ON");
            tradingModeLable.setText(backTestingStr + " *" + userSelectedBackTestYear + "*");
            userSelectedBackTesting = true;
        }else{
            System.out.println("\nbacktesting  OFF");
            tradingModeLable.setText(realTradingStr);
            userSelectedBackTesting = false;
        }

    }//GEN-LAST:event_backtestingMenuItemCheckBoxActionPerformed

    private void openCloseCriteriaMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCloseCriteriaMenuItemActionPerformed
        // TODO add your handling code here:
        if (actTraderConfigParams == null){
            actTraderConfigParams = new traderConfigParams((currentBias));
            actTraderConfigParams.setPrefixDirectory(actPositions.pathNamePrefix);
            actTraderConfigParams.rdFromFile();
        }

        Config = new ConfigDialogForm(new javax.swing.JFrame(), true);
        Config.create(currentBias);
        Config.setTraderConfigParams(actTraderConfigParams);
        Config.setConfigNow();
        Config.setVisible(true);
        if (Config.getGoodInput() == true){
            actTraderConfigParams.setPrefixDirectory(actPositions.pathNamePrefix);
            actTraderConfigParams.wrToFile();
        }

    }//GEN-LAST:event_openCloseCriteriaMenuItemActionPerformed

    private void positionPerformanceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionPerformanceMenuItemActionPerformed
        // TODO add your handling code here:
        String ticker = "";
        int port = 0;
        String posStatisticsStr = "";
        ticker = commonGui.getUserInput("Ticker to Chart:", "");
        
        posStatisticsStr = dispStatisticsToStr(ticker);
        if(posStatisticsStr != null){
            commonGui.postInformationMsg("Historical Position Performance : \n " + posStatisticsStr);
            if(userSelectedStreamToFile == true){
                perfFileWr.write(posStatisticsStr);
            }
        }
        
    }//GEN-LAST:event_positionPerformanceMenuItemActionPerformed

    private void portfolioPerformanceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portfolioPerformanceMenuItemActionPerformed
        // TODO add your handling code here:
        String perfData = "";
        perfData = hd.analizeDataPositions("");
        commonGui.postInformationMsg("Historical Portfolio Performance: \n " + perfData);
        if(userSelectedStreamToFile == true){
                perfFileWr.write(perfData);
        }
    }//GEN-LAST:event_portfolioPerformanceMenuItemActionPerformed

    private void streamToFileCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streamToFileCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
        /*
            turns on streaming performance data to file. When on, any performance data generated will be written to a file.
            The filename will be the name of the portfolio with suffix .perf. All data will go to this one file. It can then be
            viewed outside (text) the program or via display menu.
        */
        
        userSelectedStreamToFile = streamToFileCheckBoxMenuItem.isSelected();
        if(userSelectedStreamToFile == true){
            perfFileWr =  new ioWrTextFiles("perfData.txt", false);
            perfFileWr.write("Performance Data On " + todaysDate + ":\n");
        }
        
    }//GEN-LAST:event_streamToFileCheckBoxMenuItemActionPerformed

    private void showTradeDescriptioinsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTradeDescriptioinsRadioButtonActionPerformed
        // TODO add your handling code here:
        userSelectedShowTradeDescription = showTradeDescriptioinsRadioButton.isSelected();
    }//GEN-LAST:event_showTradeDescriptioinsRadioButtonActionPerformed

    private void recommendOverShootCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recommendOverShootCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
        userSelectedRecommendedOverShootPercent = recommendOverShootCheckBoxMenuItem.isSelected();
    }//GEN-LAST:event_recommendOverShootCheckBoxMenuItemActionPerformed

    private void backTestYearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestYearMenuItemActionPerformed
        // TODO add your handling code here:
        String subStr = new String();
        genComboBoxDialogForm backTestYearsComboBox; 
        String[] btYearsTable = {"2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "CurrentYear"};        
        backTestYearsComboBox = new genComboBoxDialogForm(new javax.swing.JFrame(), true, "Select BackTestYear", btYearsTable);
        backTestYearsComboBox.setVisible(true);
        userSelectedBackTestYear = backTestYearsComboBox.getSelection();
        if (userSelectedBackTestYear.equals("CurrentYear")){
            userSelectedBackTestYear = todaysDate;
        }else{
            //replace todays year with userselected year..
            subStr = todaysDate.substring(4);
            userSelectedBackTestYear = (userSelectedBackTestYear + subStr);
        }
        System.out.println("\nuserSelectedBackTestYear: "+ userSelectedBackTestYear);
        if(userSelectedBackTesting == true){
            tradingModeLable.setText(backTestingStr + " *" + userSelectedBackTestYear + "*");    
        }
        
    }//GEN-LAST:event_backTestYearMenuItemActionPerformed

    private void portfolioOsUsageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portfolioOsUsageMenuItemActionPerformed
        // TODO add your handling code here:       
        //portfolio OverShoot Usage by position. Posible values are: 0, 25, 50 .. 150, increments of 25.
        String outPutStr = "";
        int pos = 0;
        appendToStr strToWr = new appendToStr();
        strToWr.setStrToWr(outPutStr);
        int numOfPositions = 0;
        int overShootPercent = 0;
        int zeroCnt = 0;
        int twentyFiveCnt = 0;
        int fiftyCnt = 0;
        int seventyFiveCnt = 0;
        int hundredCnt = 0;
        int hundredTwentyFiveCnt = 0;
        int hundredFiftyCnt = 0;
        String ticker = "";
        String portfolioName = actPositions.getPortfolioAlias();
        if ((numOfPositions = hd.getNumOfPositionsInPortfolio()) != 0) {
            strToWr.appendMe("\n\n ");
            for (pos = 0; pos < numOfPositions; pos++) {
                ticker = hd.getTicker(pos);
                overShootPercent = hd.getOverShootSharesPercent(pos);
                strToWr.appendMe( "\n  " + pos + ") " + ticker + "\t uses: " + overShootPercent);
                switch (overShootPercent){
                    case 0:zeroCnt++;
                        break;
                    case 25: twentyFiveCnt++;
                        break;
                    case 50: fiftyCnt++;
                        break;
                    case 100: hundredCnt++;
                        break;
                    case 125: hundredTwentyFiveCnt++;
                        break;
                    case 150: hundredFiftyCnt++;
                        break;   
                    default:
                        System.out.println("portfolioOsUsageMenuItemActionPerformed: overShootPercent out of bounds.");
                        break;
                }/*switch*/
            } /*for*/
            strToWr.appendMe("\n"   +
                    "\n  0%\t: "     + zeroCnt +
                    "\n  25%\t: "     + twentyFiveCnt +
                    "\n  50%\t: "     + fiftyCnt +
                    "\n  75%\t: "     + seventyFiveCnt +
                    "\n  100%\t: "    + hundredCnt +
                    "\n  125%\t: "    + hundredTwentyFiveCnt +
                    "\n  150%\t: "     + hundredFiftyCnt +
                    "\n"
                    );
        } else {
            commonGui.postInformationMsg("Portfolio not found.");
        }
        
        commonGui.postToTextAreaMsg("Portfolio " + portfolioName + " OverShoot Usage", strToWr.getStr());
        
    }//GEN-LAST:event_portfolioOsUsageMenuItemActionPerformed

    private void TradingActivityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TradingActivityMenuItemActionPerformed
        // TODO add your handling code here:wfsnow
        actTradeActivityFrameForm = new TradeActivityDialogForm(actTradeRequestList);
        actTradeActivity.workOnThisTradeActivityTbl(actTradeActivityFrameForm);
        actTradeActivityFrameForm.setVisible(true);
    }//GEN-LAST:event_TradingActivityMenuItemActionPerformed

    private void marketOrdersCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_marketOrdersCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
        userSelectedTradingModeMarket = marketOrdersCheckBoxMenuItem.isSelected();
        actTradeActivity.setTradeMode((
                                        (userSelectedTradingModeMarket == true) ? 
                                        TradeRequestData.TradeModes.oMarket : 
                                        TradeRequestData.TradeModes.oLimitAlgo)
        );        
    }//GEN-LAST:event_marketOrdersCheckBoxMenuItemActionPerformed

    private void accountInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountInfoMenuItemActionPerformed
        int x = 0;
        int sz = 0;
        
        //accInfoPanelForm = new AccountInfoPanelForm();
        actAccInfo = actIbApi.new accountInfo(accountNumber);
        accInfoFrameForm = new AccountInfoFrameForm();                                      
        accInfoFrameForm.setActAccountInfo(actAccInfo); 
        accInfoFrameForm.setActTradeTickets(tradeTickets);
        accInfoFrameForm.setVisible(true);                
        actIbApi.setActAccountInfo(actAccInfo); 
        accInfoFrameForm.setRun(true);

    }//GEN-LAST:event_accountInfoMenuItemActionPerformed

    private void setMovingAverageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setMovingAverageMenuItemActionPerformed
        // TODO add your handling code here:
        String ma;
        ma = commonGui.getUserInput("Enter MA", "100");        
        userSelectedMa = Integer.valueOf(ma);
        System.out.println("user enterred: " + userSelectedMa);
    }//GEN-LAST:event_setMovingAverageMenuItemActionPerformed
    TradeRulesDialogForm actTraderRulesDialog = null;
    private void TradeRulesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TradeRulesMenuItemActionPerformed
  
        // TODO add your handling code here:
        actTraderRulesDialog = new TradeRulesDialogForm(this, true);
        actTraderRulesDialog.setVisible(true);
    }//GEN-LAST:event_TradeRulesMenuItemActionPerformed
    private class appendToStr{
        String wrToStr = "";
        public void setStrToWr(String s){
            wrToStr = s;
        }
        public void appendMe(String s){
            wrToStr += s;
        }
        public String getStr(){
            return wrToStr;
        }
    }
    public String dispPivotSlopeDataToStr(SlopesFound slopeIn, int actSlope){
        
        double curProfit = 0.0;
        double profit = 0.0;
        String op = null;
        String outPutStr = "";
        appendToStr strToWr = new appendToStr();
        strToWr.setStrToWr(outPutStr);
        int sz = 0;
        
        if (allSlopes.Slopes[actSlope] != null) {
            sz = allSlopes.Slopes[actSlope].size;
        }else{
            //nothing to show here go back..
            return "";
        }
  
        slopeAnalysis.SlopeStructure thisSlope = allSlopes.Slopes[actSlope];
        strToWr.appendMe("\n\nTicker: " + thisSlope.ticker + "(" + actSlope + ")");
        strToWr.appendMe("\n  Number of Pivot points: " + sz);
        
        op = thisSlope.pl.lastBought ? "Bought" : "Sold";
        //if position is open, need to get open value and percent pl...
        if (thisSlope.pl.isPosOpen() == true){     
            curProfit = myUtils.truncate(thisSlope.pl.runningPl + thisSlope.pl.getCurrentPosValue(), 2);
            profit = myUtils.truncate(thisSlope.pl.runningPl, 2);
        }else{
            curProfit = myUtils.truncate(thisSlope.pl.runningPl, 2);
            profit = curProfit;
        }
        strToWr.appendMe(
                "\n  Profit: " + profit + " (" + (thisSlope.overAllPl) + "%)"
                + " Max Profit: " + myUtils.truncate(thisSlope.pl.maxProfit, 2)
                + " Max Profit Date: " + thisSlope.pl.maxProfitDate
                + "\n  Max Loss: " + myUtils.truncate(thisSlope.pl.maxLoss, 2)
                + " Max Loss Date: " + thisSlope.pl.maxLossDate
                + " \n  Max Running Profit: " + myUtils.truncate(thisSlope.pl.maxRunningProfit, 2) + " (" + thisSlope.maxRunningProfitPercent + "%)"
                + "  Max Running Profit Date: " + thisSlope.pl.maxRunningProfitDate
                + "\n  Max Running Loss: " + myUtils.truncate(thisSlope.pl.maxRunningLoss, 2) + " (" + thisSlope.maxRunningLossPercent + "%)"
                + " Max Running Loss Date: " + thisSlope.pl.maxRunningLossDate        
                + "\n  Transactions: " + thisSlope.pl.transactions + " Last Operation: " + op
                + " DaysIn: " + (thisSlope.pl.ticksIn) + " DaysOut: " + (thisSlope.pl.ticksOut)
                + "\n  Current Trend: "+ thisSlope.currentTrendIs
        );
        
        if(thisSlope.pl.isPosOpen() == true){          
            strToWr.appendMe(
                    "\n "
                    + " Current Pos Value: " + thisSlope.pl.getCurrentPosValue()
                    + "\n  OverAllPl: " + curProfit + " OverAllPl %: " + myUtils.truncate((thisSlope.overAllPlIncludeOpen), 2)
                    + ". Current Pos DateIn: " + thisSlope.pl.getCurrentPosDateIn()
            );
            
        }
        outPutStr = strToWr.getStr();
        return(outPutStr);
    }
    public String dispPivotCurrentSlopeDataToStr(TradeTickets ticketsIn, int actSlope){
        String op = null;
        String outPutStr = "";
        appendToStr strToWr = new appendToStr();
        strToWr.setStrToWr(outPutStr);
        int numOfTrades;
        boolean isPosOpen = false;
        double currentProfit = 0.0;
        double profit = 0.0;
        DecimalFormat df = new DecimalFormat("##.##");
        
        TradeTicket thisTicket = ticketsIn.tickets[actSlope];
        
        if(thisTicket != null){
            numOfTrades = thisTicket.numberOfTrades;
        }else{
            return"";
        }
        strToWr.appendMe("\n\nTicker: " + thisTicket.ticker + "(" + actSlope + ")");
        strToWr.appendMe("\n  Number of Pivot points: " + numOfTrades);
        op = thisTicket.lastOperation;
        isPosOpen = thisTicket.lastOperation.equals(slopeDefs.BOT);
        if (isPosOpen == true){
            currentProfit = myUtils.truncate((thisTicket.runningPl + thisTicket.openPosValue), 2);          
            profit = myUtils.truncate(thisTicket.runningPl, 2);
        }else{
            currentProfit = myUtils.truncate(thisTicket.runningPl, 2);
            profit = currentProfit;
        }
        currentProfit = myUtils.truncate((thisTicket.runningPl + thisTicket.openPosValue), 2);
        strToWr.appendMe(
                "\n  Profit: " + profit + " (" + (thisTicket.overAllPlPercent) + "%)"
                + " Max Profit: " + myUtils.truncate(thisTicket.maxProfit, 2)
                + " Max Profit Date: " + thisTicket.maxPDate
                + "\n  Max Loss: " + myUtils.truncate(thisTicket.maxLoss, 2)
                + " Max Loss Date: " + thisTicket.maxLDate
                + " \n  Max Running Profit: " + myUtils.truncate(thisTicket.maxRunningProfit, 2) + " (" + thisTicket.maxRunningProfitPercent + "%)"
                + "  Max Running Profit Date: " + thisTicket.maxRunningProfitDate
                + "\n  Max Running Loss: " + myUtils.truncate(thisTicket.maxRunningLoss, 2) + " (" + thisTicket.maxRunningLossPercent + "%)"
                + " Max Running Loss Date: " + thisTicket.maxRunningLossDate        
                + "\n  Transactions: " + thisTicket.numberOfTrades + " Last Operation: " + op
                + " DaysIn: " + (thisTicket.daysIn) + " DaysOut: " + (thisTicket.daysOut)
                + "\n  Current Trend: "+ thisTicket.curTrend
                + "\n  AvePriceMv: " + df.format(thisTicket.avePriceMove) + " (" + df.format((thisTicket.avePriceMove / thisTicket.lastPrice) * 100.0) + "%)"
                //wfshere
        );
        
        if (isPosOpen == true){
            strToWr.appendMe(
                    "\n "
                    + " Current Pos Value: " + thisTicket.openPosValue
                    + "\n  OverAllPl: " + currentProfit + " OverAllPl %: " + myUtils.truncate((thisTicket.overAllPlIncludeOpenPercent), 2)
                    + ". Current Pos DateIn: " + thisTicket.OpenPosDateIn
            );
            
        }
        outPutStr = strToWr.getStr();
        return outPutStr;
    }
    int getSegmentUsed(TradeTicket ticket){
        int segUsed = 0;
        int actSeg = 0;
        int segUsedOverShoot = 0;
        
        /*
        gets the current segment number being used by either longsegments or 
        shortssegments. if shortsegment it returns a negative number. values are 1-6 not 0-5..
        0 == no seg used.
        */
        for (actSeg = 0; (actSeg < segTradingScale); actSeg++){
            if (ticket.segTrades[actSeg].segOpen == true){
                segUsed++;                
            }
            if (ticket.segTradesOverShoot[actSeg].segOpen == true){
                segUsedOverShoot++;            
            }           
        }
        if (segUsedOverShoot > 0) {
            segUsedOverShoot *= -1;
            segUsed = segUsedOverShoot;
        }
        return (segUsed);
    }
    class SlopeTrader extends Thread {
      
        SlopeTrader() {
            traderStatusTable.setRowSelectionAllowed(true);
            traderStatusTable.setColumnSelectionAllowed(false);
            this.start();
        }
        public final int oNumber             = 0;
        public final int oTicker             = 1;
        public final int oShares             = 2;
        public final int oLast               = 3;
        public final int oChange             = 4;
        public final int oRealZdPL           = 5;
        public final int oRealZdPLPercent    = 6;
        public final int oUrealZdPL          = 7;
        public final int oUrealZdPLPercent   = 8;
        
        public final int oOpenPosVal         = 9;
        public final int oOpenPosValPercent  = 10;
        public final int oState              = 11;
        public final int oTrades             = 12;
        public final int oCurTrend           = 13;
        public void updateTraderStatusTbl(int line, TradeTicket ticket, boolean init) {
            double changeInValue = 0.0;
            double last = 0.0;
            boolean isPosOpen = false;
            double currentProfit = 0.0;
            double currentProfitPercent = 0.0;
            double profit = 0.0;
            boolean newTrade = false;
            int actSegNum = 0;
            String segAndTrades = "";
            DecimalFormat df = new DecimalFormat("##.##");
            
            traderStatusTable.setRowSelectionInterval(line, line);
            if (line > 0){
                traderStatusTable.removeRowSelectionInterval(line-1, line-1);    
            }
            if (ticket.problemProcessing == false){
                if (init == false) {
                    qInfo = actChain.getQuote(ticket.ticker, false);
                    if (qInfo.last == 0.0) {
                        actPositions.getQuoteProblemCnt++;
                        qInfo.last = ticket.lastPrice;
                        qInfo.open = ticket.lastPrice;
                    }else{
                        
                    }
                }else{
                    //if initializing table just use ticket.lastPrice..
                    qInfo.last = ticket.lastPrice;
                    qInfo.open = ticket.lastPrice;
                }
                
                changeInValue = Double.valueOf(df.format(qInfo.last - qInfo.open));
                last = Double.valueOf(df.format(qInfo.last));
                ticket.lastPrice = last;
            }else{
                changeInValue = 0.0;
                last = 0.0;
                return;
            }
            //changed to handle shorts..3.14.2016
            //isPosOpen = ticket.lastOperation.equals(slopeDefs.BOT);
            isPosOpen = (ticket.sharesAtHand != 0);
            
            // these values are not stored to file so need to calculate at runtime.
            ticket.runningPlPercent = getRunningPlPercent(ticket);
            //only calc if real value..
            if (last > 0){
                ticket.openPosValue = getOpenPosValue(ticket, last);
                ticket.openPosValuePercent = getOpenPosValuePercent(ticket, last);
            }else{
                ticket.openPosValue = 0;
                ticket.openPosValuePercent = 0;
            }
            
            ticket.overAllPl = getOverAllPl(ticket);
            ticket.overAllPlPercent = getOverAllPlPercent(ticket);
            ticket.maxRunningProfitPercent = getMaxRunningProfitPercent(ticket);
            ticket.maxRunningLossPercent = getMaxRunningLossPercent(ticket);
            
            ticket.overAllPlIncludeOpenPercent = getOverAllPlPlusOpenPercent(ticket);
            
            if (isPosOpen == true) {
                currentProfit = myUtils.truncate((ticket.runningPl + ticket.openPosValue), 2);
                profit = myUtils.truncate(ticket.runningPl, 2);
            } else {
                currentProfit = myUtils.truncate(ticket.runningPl, 2);
                profit = currentProfit;
            }
            //use abs for short positions..
            if (Math.abs(ticket.originalCost) > 0){
                currentProfitPercent = myUtils.truncate(((currentProfit / Math.abs(ticket.originalCost)) * 100.0) ,2);
            }
            //see if we just did a trade..today..
            newTrade = ((ticket.buyOrderPlaced == true) || (ticket.sellOrderPlaced == true));
            traderStatusTable.getModel().setValueAt(Integer.toString(line + 1), line, oNumber);
            traderStatusTable.getModel().setValueAt(ticket.ticker, line, oTicker);
            traderStatusTable.getModel().setValueAt(Integer.toString(ticket.sharesAtHand), line, oShares);
            traderStatusTable.getModel().setValueAt(Double.toString(last), line, oLast);
            traderStatusTable.getModel().setValueAt(Double.toString(changeInValue), line, oChange);
            traderStatusTable.getModel().setValueAt(Double.toString(myUtils.truncate(ticket.runningPl, 2)), line, oRealZdPL);
            traderStatusTable.getModel().setValueAt(Double.toString(ticket.runningPlPercent), line, oRealZdPLPercent);
            
            traderStatusTable.getModel().setValueAt(Double.toString(currentProfit), line, oUrealZdPL);
            traderStatusTable.getModel().setValueAt(Double.toString(currentProfitPercent), line, oUrealZdPLPercent);
            
            traderStatusTable.getModel().setValueAt(Double.toString(ticket.openPosValue), line, oOpenPosVal);
            //traderStatusTable.getModel().setValueAt(Double.toString(ticket.overAllPlIncludeOpenPercent), line, oOpenPosValPercent);
            traderStatusTable.getModel().setValueAt(Double.toString(ticket.openPosValuePercent), line, oOpenPosValPercent);
            
            traderStatusTable.getModel().setValueAt(ticket.lastOperation, line, oState);
            if(linearMode == true){
                //if linear mode we are segmenting orders this is more interesting to display..
                actSegNum = getSegmentUsed(ticket);
                //change column header to show both segnum and trades..
                segAndTrades = (ticket.numberOfTrades + "(" + actSegNum + ")");
                traderStatusTable.getColumnModel().getColumn(oTrades).setHeaderValue("Trades(SN)");
                traderStatusTable.getTableHeader().repaint();
                traderStatusTable.getModel().setValueAt(segAndTrades, line, oTrades);
            }else{
                traderStatusTable.getModel().setValueAt(Integer.toString(ticket.numberOfTrades), line, oTrades);
            }
            
            if (newTrade == true){
                traderStatusTable.getModel().setValueAt(ticket.curTrend + "*", line, oCurTrend);
            }else{
                traderStatusTable.getModel().setValueAt(ticket.curTrend, line, oCurTrend);
            }
            
        }
        public void initTraderStatusTable(TradeTickets allTickets){
            int idx;
            /*
            initialize the table with data orignally read from file that is in traderTickets[]...
            */
            for(idx = 0;idx < allTickets.numOfTickets; idx++){
                updateTraderStatusTbl(idx, allTickets.tickets[idx], false/*initialize*/);               
            }           
            
        }

        public class OrderResult{
            public boolean filled = false;
            public double price = 0.0;
            public int quantity = 0;
            public boolean bot = false;
            public String err = null;  
            public int remaining = 0;
            public int tradeId = 0;
            public boolean error = false;
            public boolean partial = false;
            public void fillme(ibApi.OrderStatus with, boolean buyOperation){
                //if bot true then it was a buy else a sell.
                bot = buyOperation;
                filled = (with.errorMsg == "");
                price = with.aveFillPrice;
                quantity = with.filled;               
                err = with.errorMsg;
                remaining = with.remaining;  
                error = with.error;
                partial = (with.remaining != 0);
            }
            public void dispErrors(){
                
            }
            public boolean isErrorFree(){
                //filled
                return(quantity != 0);               
            }
        }
        int calculateSharesToTrade(TradeTicket thisTicket, double currentAvailFunds){
            //wfs test....remove!!
            double last;
            int sharesToTrade = 0;
            double neededMoneyForFullPosition = 0.0;
            if (actPositions.version > 1){
                qInfo = actChain.getQuote(thisTicket.ticker, false);
                if (qInfo.last == 0.0){
                    last = thisTicket.lastPrice;
                }else
                {
                    last = qInfo.last;
                }
                if(last != 0.0){
                    neededMoneyForFullPosition = (actPositions.availFunds * (actPositions.maxPercentPerPos / 100.0));
                    if (currentAvailFunds > neededMoneyForFullPosition){
                        sharesToTrade = (int) myUtils.truncate(((double)(actPositions.availFunds * (actPositions.maxPercentPerPos / 100.0)) / last), 2);    
                    }else{
                        //short of money...so calculate what we can afford..and return that..
                        sharesToTrade = (int) myUtils.truncate(((double)(currentAvailFunds) / last), 2);
                    }                   
                }else{
                    System.out.println("\ncalculateSharesToTrade: last == 0!!!!! not good...");
                    sharesToTrade = 0;
                }
                if ((false) && (sharesToTrade > 200)){
                    sharesToTrade = 200;
                }
            }else{
                //vers 1 defaults to 100 shares to trade..
                sharesToTrade = 100;
            }   
            //thisTicket.numOfSharesToTrade = sharesToTrade;
            
            return (sharesToTrade);
        }
        int calculateSharesToTradeBackTest(TradeTicket thisTicket, double currentAvailFunds, double lastClosedDay){
            int sharesToTrade;
            sharesToTrade = (int) myUtils.truncate(((double)(actPositions.availFunds * (actPositions.maxPercentPerPos / 100.0)) / lastClosedDay), 2);
            return sharesToTrade;
        }
        int calculateSharesToTrade(TradeTicket thisTicket, double currentAvailFunds, double lastClosedDay){
            //wfs test....remove!!
            double last;
            int sharesToTrade = 0;
            double neededMoneyForFullPosition = 0.0;
            if (actPositions.version > 1){
                qInfo = actChain.getQuote(thisTicket.ticker, false);
                if (qInfo.last == 0.0){                   
                    last = (thisTicket.lastPrice == 0.0) ? lastClosedDay: thisTicket.lastPrice;
                }else
                {
                    last = qInfo.last;
                }
                if(last != 0.0){
                    neededMoneyForFullPosition = (actPositions.availFunds * (actPositions.maxPercentPerPos / 100.0));
                    if (currentAvailFunds > neededMoneyForFullPosition){
                        sharesToTrade = (int) myUtils.truncate(((double)(actPositions.availFunds * (actPositions.maxPercentPerPos / 100.0)) / last), 2);    
                    }else{
                        //short of money...so calculate what we can afford..and return that..
                        sharesToTrade = (int) myUtils.truncate(((double)(currentAvailFunds) / last), 2);
                    }                   
                }else{
                    System.out.println("\ncalculateSharesToTrade: last == 0!!!!! not good...use lastDayClose");
                    
                    sharesToTrade = 0;
                }
                if ((false) && (sharesToTrade > 200)){
                    sharesToTrade = 200;
                }
            }else{
                //vers 1 defaults to 100 shares to trade..
                sharesToTrade = 100;
            }   
            //thisTicket.numOfSharesToTrade = sharesToTrade;
            
            return (sharesToTrade);
        }
        public void placeOrderNew(String ticker, boolean buyit, int sharesToTrade, String tradeSpecifics){
            //do either buy or sell based on buyit                        
            sharesToTrade = Math.abs(sharesToTrade);
            
            TradeRequestData trd = new TradeRequestData();
            trd.setTicker(ticker);
            if(buyit == true){
                System.out.println("\nbuying " + sharesToTrade + " shares..of +" + ticker);
                trd.setOperation(TradeRequestData.TradeOptions.oOpBuy);
            }
            else{
                System.out.println("\nselling " + sharesToTrade + " shares..of " + ticker);
                trd.setOperation(TradeRequestData.TradeOptions.oOpSell);
            }            
            trd.setOriginalSharesToTrade(sharesToTrade);
            trd.setTradeSpecifics(tradeSpecifics);
            actTradeRequestList.addOne(trd);
            actTradeRequestList.bumpReqCnt();
        }
        public OrderResult placeOrder(String ticker, boolean buyit, int sharesToTrade){
            //do either buy or sell based on buyit
            int tradeId = 0;
            OrderResult  result = new OrderResult();
            sharesToTrade = Math.abs(sharesToTrade);
            if(buyit == true){
                System.out.println("\nbuying " + sharesToTrade + " shares..of +" + ticker);
                    tradeId = actIbApi.testOrder.setOrderInfo(accountNumber, ticker, false /* no option */,
                            "BUY", "MKT", "DAY", 0 /*price*/, sharesToTrade);
                    actIbApi.testOrder.placeOrder(tradeId);
                    /*give IB some time to deliver */
                    myUtils.delay(3000);
                    orderStatus = actIbApi.testOrder.getOrderStatus(tradeId);
                    result.fillme(orderStatus, buyit);                   
            }else{
                System.out.println("\nselling " + sharesToTrade + " shares..of " + ticker);
                    tradeId = actIbApi.testOrder.setOrderInfo(accountNumber, ticker, false /* no option */,
                            "SELL", "MKT", "DAY", 0 /*price*/, sharesToTrade);
                    actIbApi.testOrder.placeOrder(tradeId);
                     /*give IB some time to deliver */
                    myUtils.delay(3000);
                    orderStatus = actIbApi.testOrder.getOrderStatus(tradeId);
                    result.fillme(orderStatus, buyit/*should be false for a sell*/);              
            }
            //put this in to cancel if need be...
            result.tradeId = tradeId;
            if ((result.isErrorFree() == false) || (result.error == true)) {
                System.out.println("\nError in placing order with: " + ticker);
            } else {
                System.out.println("\nSuccess in placing order with: " + ticker);
                actPositions.todaysTrades++;
            }
            return(result);
        }
        public double getOpenPosPurshasePrice(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            double val = 0.0;
            if(thisTicket.lastOperation.equals(slopeDefs.BOT)){
                val = thisTicket.currentCost / thisTicket.numOfSharesToTrade;
            }else{
                val = 0.0;
            }
            return(val);
        }
        public double getOpenPosValue(TradeTicket thisTicket, double lastQuote){
            //no need to store this in file..just calculate
            double val = 0.0;            //shit
            
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true){
                //long operation..
                //see if we have an open long position..and get it's value.
                //2.25.16 changed to sharesAtHand != 0 for open short shares we may have when overshooting..
                if (thisTicket.sharesAtHand != 0) {//fuckduck
                    if (thisTicket.sharesAtHand < 0){
                         //must be shorts so reverse order..and use abs..                        
                        val = myUtils.truncate((Math.abs(thisTicket.currentCost) - ((thisTicket.sharesAtHand * -1) * lastQuote) ), 2);
                    }else{
                        val = myUtils.truncate(((thisTicket.sharesAtHand * lastQuote) - thisTicket.currentCost), 2);
                    }
                } else {
                    //no open shares at hand.
                    val = 0.0;
                }

            }else{
                //short operation..
                //see if we have an open short position..and get it's value...
                //need to reverse for short.
                if (thisTicket.lastOperation.equals(slopeDefs.SOLD)) {
                    val = myUtils.truncate(((thisTicket.currentCost) - (Math.abs(thisTicket.sharesAtHand) * lastQuote)), 2);

                } else {
                    val = 0.0;
                }
            }
            
            return(val);
        }
        public double getOpenPosValuePercent(TradeTicket thisTicket, double lastQuote){
            //no need to store this in file..just calculate
            double val = 0.0;
            
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true){
                //long operation..
                //see if we have an open position..and get it's value.
                //3.16.2016 add SLD incase of short overshoot position open..
                if ((thisTicket.lastOperation.equals(slopeDefs.BOT) || (thisTicket.lastOperation.equals(slopeDefs.SOLD))) && (thisTicket.currentCost > 0)) {
                    val = myUtils.truncate((getOpenPosValue(thisTicket, lastQuote) / thisTicket.currentCost) * 100.0, 2);

                } else {
                    val = 0.0;
                }

            }else{
                //short operation...
                //see if we have an open position..and get it's value.
                if (thisTicket.lastOperation.equals(slopeDefs.SOLD) && (thisTicket.currentCost > 0)) {
                    val = myUtils.truncate((getOpenPosValue(thisTicket, lastQuote) / thisTicket.currentCost) * 100.0, 2);

                } else {
                    val = 0.0;
                }
            }          
            return(val);
        }
        public double getRunningPlPercent(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            double val = 0.0; 
            //use abs for short positions..
            if (Math.abs(thisTicket.originalCost) != 0){
                val = myUtils.truncate(((thisTicket.runningPl / Math.abs(thisTicket.originalCost)) * 100.0), 2);
            }
            return(val);
        }
        public double getOverAllPl(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            //same as getRunningPlPercent..
            double retVal = 0.0;
            double runningPl = 0.0;
            double overAllPl = 0.0;
            if (Math.abs(thisTicket.originalCost) != 0){
                runningPl = myUtils.truncate((thisTicket.runningPl / Math.abs(thisTicket.originalCost)), 2);
                overAllPl = myUtils.truncate(((runningPl + thisTicket.openPosValue) / Math.abs(thisTicket.originalCost)), 2);            
            }
            return(overAllPl);
        }
        public double getOverAllPlPercent(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            //same as getRunningPlPercent..
            double overAllPlPercent = 0.0;
            double retVal = 0.0;
            double overAllPl = 0.0;
            if (Math.abs(thisTicket.originalCost) != 0){
                overAllPl = myUtils.truncate((thisTicket.runningPl / Math.abs(thisTicket.originalCost)), 2);
                overAllPlPercent = myUtils.truncate((((overAllPl) / Math.abs(thisTicket.originalCost)) * 100.0), 2);            
            }
            return(overAllPlPercent);
        }
        public double getOverAllPlPlusOpenPercent(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            //same as getRunningPlPercent..
            double overAllPlPercent = 0.0;
            double overAllPl = 0.0;
            if (Math.abs(thisTicket.originalCost) != 0){
                overAllPl = myUtils.truncate(((thisTicket.runningPl + thisTicket.openPosValue) / Math.abs(thisTicket.originalCost)), 2);
                overAllPlPercent = myUtils.truncate(((overAllPl) * 100.0), 2);            
            }
            return(overAllPlPercent);
        }
        public double getMaxRunningProfitPercent(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            //same as getRunningPlPercent..
            double val = 0.0; 
            if (Math.abs(thisTicket.originalCost) != 0){
                val = myUtils.truncate(((thisTicket.maxRunningProfit / Math.abs(thisTicket.originalCost)) * 100.0), 2);
                
            }
            return(val);
        }
        public double getMaxRunningLossPercent(TradeTicket thisTicket){
            //no need to store this in file..just calculate
            //same as getRunningPlPercent..
            double val = 0.0; 
            if (Math.abs(thisTicket.originalCost) != 0){
                val = myUtils.truncate(((thisTicket.maxRunningLoss / Math.abs(thisTicket.originalCost)) * 100.0), 2);
                
            }
            return(val);
        }
        void updateTicketData(TradeTicket thisTicket, double lastQuote){
            //see if we have an open position..and get it's value.
            if (thisTicket.lastOperation.equals(slopeDefs.BOT)) {
                thisTicket.openPosValue = myUtils.truncate(((thisTicket.sharesAtHand * lastQuote) - thisTicket.currentCost), 2);

            }    
            
        }
        void updateTicketData(TradeTicket thisTicket, OrderResult orderResult){
            /*
            this methode updates the ticket data. Needed now because an order went through.
            input bot is true if it was a purchase, false if it was a sell. One or the other did happen.
            */
            boolean buyOp = orderResult.bot;
            double currentPl = 0.0;
            double costBasis = 0.0;          
            String todaysDate = null;
            double plPercent = 0.0;
            //check to see if this is a short order...
            boolean shortIt = longOrShort.equals(slopeDefs.oBiasShortStr);
            todaysDate = myUtils.GetTodaysDate("YYY/MM/DD");
            todaysDate = myUtils.reverseDate(todaysDate);
            
            thisTicket.numberOfTrades++;
            if (shortIt == false){
               //long position operation
                if (buyOp == true) {
                    //buy it is..
                    thisTicket.currentCost = myUtils.truncate(((orderResult.price) * orderResult.quantity), 2);
                    thisTicket.sharesAtHand += orderResult.quantity;
                    //if this is the first purchase ever, current cost is also original
                    //cost. Ultimately Profit/loss is based on this value.
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        thisTicket.originalCost = thisTicket.currentCost;

                    }
                    thisTicket.lastOperation = slopeDefs.BOT;
                    thisTicket.OpenPosDateIn = todaysDate;
                    //thisTicket.daysIn++;
                } else {
                    //must be a sell
                    thisTicket.lastOperation = slopeDefs.SOLD;
                    thisTicket.sharesAtHand -= orderResult.quantity;
                    //profit for this pivot (buy to sell)
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    currentPl = ((orderResult.quantity * orderResult.price) - thisTicket.currentCost);
                    currentPl = myUtils.truncate(currentPl, 2);
                    //set hi water mark for any one pivot (buy to sell) for both profit and loss
                    if ((currentPl > 0) && (currentPl > thisTicket.maxProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxProfit = currentPl;
                        thisTicket.maxPDate = todaysDate;
                        thisTicket.maxProfitPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);

                    } else if ((currentPl < 0) && (currentPl < thisTicket.maxLoss)) {
                        thisTicket.maxLoss = currentPl;
                        thisTicket.maxLDate = todaysDate;
                        thisTicket.maxLossPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);
                    }
                    //accumulate every pl in runningPl
                    thisTicket.runningPl += currentPl;

                    //keep max/min of running pl too..
                    if ((thisTicket.runningPl > 0) && (thisTicket.runningPl > thisTicket.maxRunningProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxRunningProfit = thisTicket.runningPl;
                        thisTicket.maxRunningProfitDate = todaysDate;

                    } else if ((thisTicket.runningPl < 0) && (thisTicket.runningPl < thisTicket.maxRunningLoss)) {
                        thisTicket.maxRunningLoss = thisTicket.runningPl;
                        thisTicket.maxRunningLossDate = todaysDate;
                    }
                    thisTicket.currentPlPercent = myUtils.truncate(((currentPl / thisTicket.currentCost) * 100.0), 2);
                    //thisTicket.daysOut++;
                } //end sell for long position

            } else {
                //short position operation
                if (buyOp == false) {
                    //Sell to open a short it is..
                    thisTicket.currentCost = myUtils.truncate(((orderResult.price) * orderResult.quantity), 2);
                    //make negative number to show short position!..
                    thisTicket.sharesAtHand -= (orderResult.quantity);
                    //if this is the first purchase ever, current cost is also original
                    //cost. Ultimately Profit/loss is based on this value.
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        thisTicket.originalCost = thisTicket.currentCost;

                    }
                    thisTicket.lastOperation = slopeDefs.SOLD;
                    thisTicket.OpenPosDateIn = todaysDate;
                    //thisTicket.daysIn++;
                } else {
                    //must be a buy to close a short
                    thisTicket.lastOperation = slopeDefs.BOT;
                    //adding makes the negative number zero....
                    thisTicket.sharesAtHand += orderResult.quantity;
                    //profit for this pivot (buy to sell)
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    /*
                        for short buy to close, currentCost should be bigger than what it's
                        worth now for a profit. If not, it's a loss..
                    */
                    currentPl = (thisTicket.currentCost - (orderResult.quantity * orderResult.price));
                    currentPl = myUtils.truncate(currentPl, 2);
                    //set hi water mark for any one pivot (buy to sell) for both profit and loss
                    if ((currentPl > 0) && (currentPl > thisTicket.maxProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxProfit = currentPl;
                        thisTicket.maxPDate = todaysDate;
                        thisTicket.maxProfitPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);

                    } else if ((currentPl < 0) && (currentPl < thisTicket.maxLoss)) {
                        thisTicket.maxLoss = currentPl;
                        thisTicket.maxLDate = todaysDate;
                        thisTicket.maxLossPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);
                    }
                    //accumulate every pl in runningPl
                    thisTicket.runningPl += currentPl;

                    //keep max/min of running pl too..
                    if ((thisTicket.runningPl > 0) && (thisTicket.runningPl > thisTicket.maxRunningProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxRunningProfit = thisTicket.runningPl;
                        thisTicket.maxRunningProfitDate = todaysDate;

                    } else if ((thisTicket.runningPl < 0) && (thisTicket.runningPl < thisTicket.maxRunningLoss)) {
                        thisTicket.maxRunningLoss = thisTicket.runningPl;
                        thisTicket.maxRunningLossDate = todaysDate;
                    }
                    thisTicket.currentPlPercent = myUtils.truncate(((currentPl / thisTicket.currentCost) * 100.0), 2);
                    //thisTicket.daysOut++;
                } //end sell short operation

            }//end short operation
            
            costBasis = thisTicket.originalCost;
            if (costBasis > 0) {
                thisTicket.overAllPlPercent = myUtils.truncate(((thisTicket.runningPl / costBasis) * 100), 2);
                thisTicket.overAllPl = getOverAllPl(thisTicket);
                thisTicket.overAllPlIncludeOpenPercent = getOverAllPlPlusOpenPercent(thisTicket);
                thisTicket.maxRunningProfitPercent = myUtils.truncate(((thisTicket.maxRunningProfit / costBasis) * 100), 4);
                thisTicket.maxRunningLossPercent = myUtils.truncate(((thisTicket.maxRunningLoss / costBasis) * 100), 4);
                plPercent = thisTicket.currentPlPercent;
            }else{
                System.out.println("\ncost basis = zero@!!!!");
            }

        }
        public String getOperation(boolean shortIt, boolean buyIt){
            String retOp = "";
            if ((shortIt == false) && (buyIt == false)){
                //sell long, so close
                retOp = slopeDefs.oCLOSE;
            }else if ((shortIt == false) && (buyIt == true)){
                //buy long, so open
                retOp = slopeDefs.oOPEN;
            }else if ((shortIt == true) && (buyIt == false)){
                //sell short, so open
                retOp = slopeDefs.oOPEN;
            }else if ((shortIt == true) && (buyIt == true)){
                //buy short, so close
                retOp = slopeDefs.oCLOSE;
            }
            return retOp;
        }
        void updateTicketDataSegmented(TradeTicket thisTicket, OrderResult orderResult, String tradeOperation){
            /*
            this methode updates the ticket data. Needed now because an order went through.
            input bot is true if it was a purchase, false if it was a sell. One or the other did happen.
            */
            boolean buyOp = orderResult.bot;
            double currentPl = 0.0;
            double costBasis = 0.0;          
            String todaysDate = null;
            double plPercent = 0.0;
            double segmentCost = 0.0;
            double segPrice = 0.0;
            int segShares = 0;
            int segSharesOvershoot = 0;
            //check to see if this is a short order...
            boolean shortIt = longOrShort.equals(slopeDefs.oBiasShortStr);            
            todaysDate = myUtils.GetTodaysDate("YYY/MM/DD");
            todaysDate = myUtils.reverseDate(todaysDate);
            String operation = getOperation(shortIt, buyOp);
            
            thisTicket.numberOfTrades++;
            if (shortIt == false){
               //long position operation
                if ((tradeOperation.equals(slopeDefs.oBuyToOpenLong) == true) || (tradeOperation.equals(slopeDefs.oSellToOpenShort))) {
                    //opening a position, buyToOpenLong OR sellToOpenShort..just record the price/quantity..                               
                    segPrice = thisTicket.segTrade(tradeOperation, orderResult.price, orderResult.quantity); 
                    //get cost of both seg types..the one that is not being used will return 0.0...
                    //get cost of current seg..and accumulate..
                    
                    //thisTicket.currentCost += myUtils.truncate(thisTicket.segTradeGetCost(thisTicket.segTradeDoThisOne[thisTicket.actSegTradeDoThisOne]), 2); 
                    
                    if(tradeOperation.equals(slopeDefs.oBuyToOpenLong) == true){
                        //currentCost of longs
                        thisTicket.currentCost += myUtils.truncate(thisTicket.segTradeGetCost(thisTicket.segTrades[thisTicket.actSegTrade]), 2); 
                        //add to at hand..
                        thisTicket.sharesAtHand += orderResult.quantity;
                        
                        //if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        //3.17.16 have orig cost be the max of current cost..it will peg with position max $ kicks in..
                        //use abs they may be negative is shorts..
                        //3.20.16, better to stop updating origCost once it's segments are full. Use ORIG_COST_FULL to mark the event,
                        //so check for this condition before updating..
                        if (Math.abs(thisTicket.currentCost) > Math.abs(thisTicket.originalCost)) {
                        //if (Math.abs(thisTicket.originalCost) < slopeDefs.oORIG_COST_FULL) {
                            //means we are not full yet, so update origCost with current cost..
                            //thisTicket.originalCost = (thisTicket.numOfSharesToTrade * hd.getLastClosedDayPrice());
                              thisTicket.originalCost += thisTicket.currentCost;
                        }
                        thisTicket.lastOperation = slopeDefs.BOT;
                        /*
                        if (thisTicket.segTradeAllIn(thisTicket.segTrades) == true){
                            //segments are full for first time, so flag to not update origCost anymore
                            //origCost now represents position cost from inception..
                            thisTicket.originalCost += slopeDefs.oORIG_COST_FULL;
                        }else{
                            //not full yet so no flag..
                        }
                        */
                    } else {
                        //must be sellToOpenShort
                        //current cost of short..3.18.16 cost for short is negative..
                        thisTicket.currentCost += myUtils.truncate(thisTicket.segTradeGetCost(thisTicket.segTradesOverShoot[thisTicket.actSegTradeOverShoot]), 2);
                        //takeeth away
                        thisTicket.sharesAtHand -= orderResult.quantity;
                        //if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        if (thisTicket.originalCostOverShoot < slopeDefs.oORIG_COST_FULL) {
                             // note: change to have overshoot's own current cost!!
                             //3.17.16 have orig cost be the max of current cost..it will peg with position max $ kicks in..
                            thisTicket.originalCostOverShoot += thisTicket.currentCost;                            
                        }
                        thisTicket.lastOperation = slopeDefs.SOLD;
                        if (thisTicket.segTradeAllIn(thisTicket.segTradesOverShoot) == true){
                            //segments are full for first time, so flag to not update origCost anymore
                            //origCost now represents position cost from inception..
                            thisTicket.originalCostOverShoot += slopeDefs.oORIG_COST_FULL;
                        }else{
                            //not full yet so no flag..
                        }
                    }                            
                    
                    //if this is the first purchase ever, current cost is also original
                    //cost. Ultimately Profit/loss is based on this value.
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                   
                    thisTicket.OpenPosDateIn = todaysDate;
                                      
                    //thisTicket.daysIn++;
                } else if ((tradeOperation.equals(slopeDefs.oSellToCloseLong) == true) || (tradeOperation.equals(slopeDefs.oBuyToCloseShort))){
                    //must be closing a position, so either sell to close long or buy to close short, either way calculate p/l stuff
                    if(tradeOperation.equals(slopeDefs.oSellToCloseLong) == true){
                        thisTicket.lastOperation = slopeDefs.SOLD;
                        thisTicket.sharesAtHand -= orderResult.quantity;
                        //sold so subtract from current cost
                        if (thisTicket.sharesAtHand != 0){
                            thisTicket.currentCost -= myUtils.truncate(thisTicket.segTradeGetCost(thisTicket.segTrades[thisTicket.actSegTrade]), 2);
                        }else{
                            //if no more segments left clear currentCost.
                            thisTicket.currentCost= 0.0;
                        }
                        
                    }else{
                        //must be buyToCloseShort..
                        thisTicket.lastOperation = slopeDefs.BOT;
                        thisTicket.sharesAtHand += orderResult.quantity;
                        //buy to close short so subtract from current costs..
                        if (thisTicket.sharesAtHand != 0){
                            thisTicket.currentCost -= myUtils.truncate(thisTicket.segTradeGetCost(thisTicket.segTradesOverShoot[thisTicket.actSegTradeOverShoot]), 2);
                        }else{
                            //if no more segments left clear currentCost.
                            thisTicket.currentCost= 0.0;
                        }
                        
                    }
                                  //fuckduck currentCost needs to be taken away from...
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    //segPrice will have price of segment we paid when it opened..
                    segPrice = thisTicket.segTrade(tradeOperation, 0.0/*pass nothing*/, 0 /*pass nothing */);
                    if(tradeOperation.equals(slopeDefs.oSellToCloseLong) == true){
                        segShares = thisTicket.segGetNumShares(thisTicket.segTrades, thisTicket.actSegTrade);
                        segmentCost = (segPrice * segShares);
                        currentPl = ((orderResult.quantity * orderResult.price) - segmentCost);
                    }else{
                        segShares = thisTicket.segGetNumShares(thisTicket.segTradesOverShoot, thisTicket.actSegTradeOverShoot);
                        segmentCost = (segPrice * segShares);
                        currentPl = (((orderResult.quantity * orderResult.price) - segmentCost) * -1); 
                    }                                                                                                   
                    currentPl = myUtils.truncate(currentPl, 2);
                    //set hi water mark for any one pivot (buy to sell) for both profit and loss
                    if ((currentPl > 0) && (currentPl > thisTicket.maxProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxProfit = currentPl;
                        thisTicket.maxPDate = todaysDate;
                        if(thisTicket.currentCost != 0){
                            //use abs for current cost because short is neg number..
                            thisTicket.maxProfitPercent = myUtils.truncate((currentPl / Math.abs(thisTicket.currentCost)), 2);
                        }else{
                            
                        }
                    } else if ((currentPl < 0) && (currentPl < thisTicket.maxLoss)) {
                        thisTicket.maxLoss = currentPl;
                        thisTicket.maxLDate = todaysDate;
                        if(thisTicket.currentCost != 0){
                            thisTicket.maxLossPercent = myUtils.truncate((currentPl / Math.abs(thisTicket.currentCost)), 2);
                        }
                    }
                    //accumulate every pl in runningPl
                    thisTicket.runningPl += currentPl;

                    //keep max/min of running pl too..
                    if ((thisTicket.runningPl > 0) && (thisTicket.runningPl > thisTicket.maxRunningProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxRunningProfit = thisTicket.runningPl;
                        thisTicket.maxRunningProfitDate = todaysDate;

                    } else if ((thisTicket.runningPl < 0) && (thisTicket.runningPl < thisTicket.maxRunningLoss)) {
                        thisTicket.maxRunningLoss = thisTicket.runningPl;
                        thisTicket.maxRunningLossDate = todaysDate;
                    }
                    if (segmentCost != 0.0){
                        thisTicket.currentPlPercent = myUtils.truncate(((currentPl / segmentCost) * 100.0), 2);
                    }                    
                    //thisTicket.daysOut++;
                } //end sell for long position

            } else {
                //short position operation
                if (buyOp == false) {
                    //Sell to open a short it is..
                    thisTicket.currentCost = myUtils.truncate(((orderResult.price) * orderResult.quantity), 2);
                    //make negative number to show short position!..
                    thisTicket.sharesAtHand -= (orderResult.quantity);
                    //if this is the first purchase ever, current cost is also original
                    //cost. Ultimately Profit/loss is based on this value.
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        thisTicket.originalCost = thisTicket.currentCost;

                    }
                    thisTicket.lastOperation = slopeDefs.SOLD;
                    thisTicket.OpenPosDateIn = todaysDate;
                    //thisTicket.daysIn++;
                } else {
                    //must be a buy to close a short
                    thisTicket.lastOperation = slopeDefs.BOT;
                    //adding makes the negative number zero....
                    thisTicket.sharesAtHand += orderResult.quantity;
                    //profit for this pivot (buy to sell)
                    System.out.println("updateTicketData: orderResult.price: " + orderResult.price);
                    System.out.println("updateTicketData: orderResult.quantity: " + orderResult.quantity);
                    /*
                        for short buy to close, currentCost should be bigger than what it's
                        worth now for a profit. If not, it's a loss..
                    */
                    currentPl = (thisTicket.currentCost - (orderResult.quantity * orderResult.price));
                    currentPl = myUtils.truncate(currentPl, 2);
                    //set hi water mark for any one pivot (buy to sell) for both profit and loss
                    if ((currentPl > 0) && (currentPl > thisTicket.maxProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxProfit = currentPl;
                        thisTicket.maxPDate = todaysDate;
                        thisTicket.maxProfitPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);

                    } else if ((currentPl < 0) && (currentPl < thisTicket.maxLoss)) {
                        thisTicket.maxLoss = currentPl;
                        thisTicket.maxLDate = todaysDate;
                        thisTicket.maxLossPercent = myUtils.truncate((currentPl / thisTicket.currentCost), 2);
                    }
                    //accumulate every pl in runningPl
                    thisTicket.runningPl += currentPl;

                    //keep max/min of running pl too..
                    if ((thisTicket.runningPl > 0) && (thisTicket.runningPl > thisTicket.maxRunningProfit)) {
                        //gain..remember max profit..
                        thisTicket.maxRunningProfit = thisTicket.runningPl;
                        thisTicket.maxRunningProfitDate = todaysDate;

                    } else if ((thisTicket.runningPl < 0) && (thisTicket.runningPl < thisTicket.maxRunningLoss)) {
                        thisTicket.maxRunningLoss = thisTicket.runningPl;
                        thisTicket.maxRunningLossDate = todaysDate;
                    }
                    thisTicket.currentPlPercent = myUtils.truncate(((currentPl / thisTicket.currentCost) * 100.0), 2);
                    //thisTicket.daysOut++;
                } //end sell short operation

            }//end short operation
            
            costBasis = thisTicket.segTradeGetCost();
            if (costBasis > 0) {
                thisTicket.overAllPlPercent = myUtils.truncate((((thisTicket.runningPl) / costBasis) * 100), 2);
                thisTicket.overAllPl = getOverAllPl(thisTicket);
                thisTicket.overAllPlIncludeOpenPercent = getOverAllPlPlusOpenPercent(thisTicket);
                thisTicket.maxRunningProfitPercent = myUtils.truncate(((thisTicket.maxRunningProfit / costBasis) * 100), 4);
                thisTicket.maxRunningLossPercent = myUtils.truncate(((thisTicket.maxRunningLoss / costBasis) * 100), 4);
                plPercent = thisTicket.currentPlPercent;
            }else{
                System.out.println("\ncost basis = zero@!!!!");
            }

        }
        private void updateNewDateData() {
            int idx;
            //change all didWeTradeToday Strings to NO 
            //since it's a new day. And updtae daysIn/daysOut based on last operation..
            for (idx = 0; idx < tradeTickets.numOfTickets; idx++) {
                tradeTickets.tickets[idx].didWeTradeToday = slopeDefs.NO;
                
            }
            //write tickets to file..
            System.out.println("\nbackUpTicketInfoToFile..Its a new day...");
            backUpTicketInfoToFile(tradeTickets);
            //update datefile with todaysDate
            writeDateToFile(todaysDate);
        }
        // colums for totals
        final int oEmptyColmn       = 0;
        final int oOrigCostColmn    = 1;
        final int oRunningPLColmn   = 2;
        final int oPercentColmn     = 3;
        final int oTotals           = 4;
        // lines for totals
        final int oEmptyRow         = 0;
        final int oOpenPosRow       = 1;
        final int oClosedPosRow     = 2;
        final int oTotalsRow        = 3;
        
        final int oTradesTdyRow     = 4;
        final int oTrendUpRow       = 5;
        final int oTrendDnRow       = 6;
        final int oHistDataErrRow   = 7;
        final int oTradeErrsRow     = 8;
        
        
        final String OpenPos        = "OpenPos :";
        final String ClosedPos      = "ClosdPos:";
        final String Totals         = "Totals  :";
        final String NewPos         = "NewPos  :";
        final String Trades         = "Trades:";
        final String HistDataErrs   = "HistDatErrs:";
        final String TradeErrors    = "TradeErrs:";
        final String TrendUp        = "TrandUp:";
        final String TrendDn        = "TrendDn:";
        
        public class Totals {

            double openOrigCost = 0.0;
            double netLiqValue = 0.0;
            double openOrigCostShort = 0.0;
            double openRunning = 0.0;
            double closeOrigCost = 0.0;
            double closeRunning = 0.0;
            double totalOrigCost = 0.0;
            double totalRunning = 0.0;
            double openPercent = 0.0;
            double closePercent = 0.0;
            double totalPercent = 0.0;
            int totalOpen = 0;
            int totalClosed = 0;
            int totalNew = 0;
            int totalOpenClosed = 0;
            int tradesToday = 0;
            int histDataErrs = 0;
            int tradeErrors = 0;
            int trendUpCnt = 0;
            int trendDnCnt = 0;
            int botCnt = 0;
            int soldCnt = 0;

            public Totals(){
                //set up table r/c one time.
                totalsTable.getModel().setValueAt(NewPos, oEmptyRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(OpenPos, oOpenPosRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(ClosedPos, oClosedPosRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(Totals, oTotalsRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(Trades, oTradesTdyRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(HistDataErrs, oHistDataErrRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(TradeErrors, oTradeErrsRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(TrendUp, oTrendUpRow, oEmptyColmn);
                totalsTable.getModel().setValueAt(TrendDn, oTrendDnRow, oEmptyColmn);
            }
            public void updateTotalsTextArea(TradeTickets ticketsIn, int posIn, boolean init) {
                
                TradeTicket thisTicket;

                    //no display just gather numbers..
                //easier access..
                thisTicket = ticketsIn.tickets[posIn];

                if (thisTicket.problemProcessing == false) {
                    if (init == false) {
                        qInfo = actChain.getQuote(thisTicket.ticker, false);
                        if (qInfo.last == 0.0) {
                            actPositions.getQuoteProblemCnt++;
                            qInfo.last = thisTicket.lastPrice;
                        }else{
                            
                        }
                    }else{
                        qInfo.last = thisTicket.lastPrice;   
                    }
                    thisTicket.chLastToOpenPrice = myUtils.truncate((qInfo.last - qInfo.open), 2);
                    thisTicket.lastPrice = myUtils.truncate((qInfo.last), 2);

                } else {
                    thisTicket.chLastToOpenPrice = 0;
                    thisTicket.lastPrice = 0;
                    return;
                }
                thisTicket.openPosValue = getOpenPosValue(thisTicket, thisTicket.lastPrice);
                netLiqValue += (Math.abs(thisTicket.lastPrice * thisTicket.sharesAtHand));
                if (longOrShort.equals(slopeDefs.oBiasLongStr) == true) {
                    //process long condition...
                    if ((thisTicket.lastOperation.equals(slopeDefs.BOT) && thisTicket.sharesAtHand != 0) || (thisTicket.lastOperation.equals(slopeDefs.SOLD) && thisTicket.sharesAtHand != 0)) {
                    //if (thisTicket.sharesAtHand != 0) {    
                    //    openOrigCost += thisTicket.originalCost;
                        
                        if(thisTicket.sharesAtHand > 0){
                            openOrigCost += thisTicket.currentCost;
                        }else if (thisTicket.sharesAtHand < 0){
                            openOrigCostShort += thisTicket.currentCost;
                        }
                        //System.out.println("\n---totalOrigCost---> " + (openOrigCost + openOrigCostShort) + " <------- right Now.");
                   //     openRunning += (thisTicket.runningPl + thisTicket.openPosValue);
                        openRunning += (thisTicket.openPosValue);
                        //some of these open positons have closed values..count them..
                        closeRunning += thisTicket.runningPl;
                        if(thisTicket.sharesAtHand > 0){
                            closeOrigCost += (thisTicket.originalCost > thisTicket.originalCostOverShoot ? thisTicket.originalCost : thisTicket.originalCostOverShoot);
                        }
                        totalOpen++;
                    } else if (((thisTicket.lastOperation.equals(slopeDefs.SOLD)) && (thisTicket.sharesAtHand == 0)) || ((thisTicket.lastOperation.equals(slopeDefs.BOT)) && (thisTicket.sharesAtHand == 0))) {
                    //} else if ((!thisTicket.lastOperation.equals(slopeDefs.NEW)) && (thisTicket.sharesAtHand == 0)) {    
                        if(thisTicket.sharesAtHand > 0){
                            closeOrigCost += (thisTicket.originalCost > thisTicket.originalCostOverShoot ? thisTicket.originalCost : thisTicket.originalCostOverShoot);
                        }
                        closeRunning += thisTicket.runningPl;
                        totalClosed++;
                    } else {
                        totalNew++;
                    }
                }else{
                    //must be short condtion...
                    if (thisTicket.lastOperation.equals(slopeDefs.SOLD)) {
                        openOrigCost += thisTicket.originalCost;
                        openRunning += (thisTicket.runningPl + thisTicket.openPosValue);
                        totalOpen++;
                    } else if (thisTicket.lastOperation.equals(slopeDefs.BOT)) {
                        closeOrigCost += thisTicket.originalCost;
                        closeRunning += thisTicket.runningPl;
                        totalClosed++;
                    } else {
                        totalNew++;
                    }
                }
                
                totalOpenClosed = totalOpen + totalClosed;
                if (openOrigCost > 0) {
                    openPercent = myUtils.truncate(((openRunning / openOrigCost) * 100.0), 2);
                } else {
                    openPercent = 0.0;
                }
                if (closeOrigCost > 0) {
                    closePercent = myUtils.truncate(((closeRunning / closeOrigCost) * 100.0), 2);
                } else {
                    closePercent = 0.0;
                }
                totalRunning = (openRunning + closeRunning);
                //3.17.16 peg highest..instead of adding the two..
                totalOrigCost = (closeOrigCost > openOrigCost) ? closeOrigCost : openOrigCost;
                if (totalOrigCost > 0) {
                    totalPercent = myUtils.truncate((((totalRunning) / (totalOrigCost)) * 100.0), 2);
                } else {
                    totalPercent = 0.0;
                }
                if (thisTicket.didWeTradeToday.equals(slopeDefs.YES)){
                    tradesToday++;
                    if (thisTicket.lastOperation.equals(slopeDefs.BOT)){
                        botCnt++;
                    }else if (thisTicket.lastOperation.equals(slopeDefs.SOLD)){
                        soldCnt++;
                    }
                }
                if ((thisTicket.curTrend != null) && (thisTicket.curTrend.equals(slopeDefs.TREND_UP))){
                    trendUpCnt++;
                }else if ((thisTicket.curTrend != null) && (thisTicket.curTrend.equals(slopeDefs.TREND_DN))){
                    trendDnCnt++;
                }
                histDataErrs = actPositions.histDataProblemCnt;
                tradeErrors = actPositions.tradeErrorsCnt;
                actPositions.newPositions = totalNew;
                actPositions.closedPositions = totalClosed;
                actPositions.openPositions = totalOpen;
            }
            public void updateTotalsTextAreaNew(TradeTickets ticketsIn, int posIn, boolean init) {
                /*
                3/16/2016
                Re-doing the totals display since the segmented trading is in place. Keep the old one for now as fall back
                because it kind of works. 
                */
                TradeTicket thisTicket;
                
                thisTicket = ticketsIn.tickets[posIn];

                if (thisTicket.problemProcessing == false) {
                    if (init == false) {
                        qInfo = actChain.getQuote(thisTicket.ticker, false);
                        if (qInfo.last == 0.0) {
                            actPositions.getQuoteProblemCnt++;
                            qInfo.last = thisTicket.lastPrice;
                            //if one fails, both fail so show no change..
                            qInfo.open = thisTicket.lastPrice;
                        }else{
                            
                        }
                    }else{
                        qInfo.last = thisTicket.lastPrice; 
                        qInfo.open = thisTicket.lastPrice;
                    }
                    thisTicket.chLastToOpenPrice = myUtils.truncate((qInfo.last - qInfo.open), 2);
                    thisTicket.lastPrice = myUtils.truncate((qInfo.last), 2);

                } else {
                    thisTicket.chLastToOpenPrice = 0;
                    thisTicket.lastPrice = 0;
                    return;
                }
                thisTicket.openPosValue = getOpenPosValue(thisTicket, thisTicket.lastPrice);
                if (longOrShort.equals(slopeDefs.oBiasLongStr) == true) {
                    //process long condition...                    
                    if (thisTicket.sharesAtHand != 0) {    
                        //openOrigCost += thisTicket.originalCost;
                        //if short position, currentCost will be negative..
                        if(thisTicket.sharesAtHand < 0){
                            //must be short if neg number..
                            //openOrigCost += thisTicket.originalCostOverShoot;
                        }else{
                            //must be long if pos number..
                            openOrigCost += thisTicket.originalCost;
                        }
                        
                        openRunning += (thisTicket.runningPl + thisTicket.openPosValue);
                        //openRunning += (thisTicket.openPosValue);
                        // runningpl is realized gain..need to get it from both open and closed positions...this one is open..
                        //closeRunning += thisTicket.runningPl;                        
                        totalOpen++;                   
                    }
                    //gather open and closed position original cost. new positions will have zero so no harm.. 
                    /*
                    if (thisTicket.originalCost > 0) {
                        closeOrigCost += thisTicket.originalCost;
                    } else {
                        closeOrigCost += thisTicket.originalCostOverShoot;
                    }
                    */
                    if (!thisTicket.lastOperation.equals(slopeDefs.NEW) && thisTicket.sharesAtHand == 0) {
                        /*
                        so this position is not new, and is closed                        
                         */                       
                        // runningpl is realized gain..need to get it from both open and closed positions...this oe is closed..
                        if (thisTicket.originalCost > 0) {
                            closeOrigCost += thisTicket.originalCost;
                        } else {
                            closeOrigCost += thisTicket.originalCostOverShoot;
                        }
                        closeRunning += thisTicket.runningPl;
                        totalClosed++;
                    } else if (thisTicket.lastOperation.equals(slopeDefs.NEW)) {
                        totalNew++;
                    }
                } else {
                    //must be short condtion...
                    if (thisTicket.lastOperation.equals(slopeDefs.SOLD)) {
                        openOrigCost += thisTicket.originalCost;
                        openRunning += (thisTicket.runningPl + thisTicket.openPosValue);
                        totalOpen++;
                    } else if (thisTicket.lastOperation.equals(slopeDefs.BOT)) {
                        closeOrigCost += thisTicket.originalCost;
                        closeRunning += thisTicket.runningPl;
                        totalClosed++;
                    } else {
                        totalNew++;
                    }
                }
                totalOpenClosed = totalOpen + totalClosed;
                if (openOrigCost > 0) {
                    openPercent = myUtils.truncate(((openRunning / openOrigCost) * 100.0), 2);
                } else {
                    openPercent = 0.0;
                }
                if (closeOrigCost > 0) {
                    closePercent = myUtils.truncate(((closeRunning / closeOrigCost) * 100.0), 2);
                } else {
                    closePercent = 0.0;
                }
                totalRunning = (openRunning + closeRunning);
                //3.17.16 peg highest..instead of adding the two..
                totalOrigCost = (closeOrigCost > openOrigCost) ? closeOrigCost : openOrigCost;
                //totalOrigCost = (closeOrigCost + openOrigCost);
                if (totalOrigCost > 0) {
                    totalPercent = myUtils.truncate((((totalRunning) / (totalOrigCost)) * 100.0), 2);
                } else {
                    totalPercent = 0.0;
                }
                if (thisTicket.didWeTradeToday.equals(slopeDefs.YES)){
                    tradesToday++;
                    if (thisTicket.lastOperation.equals(slopeDefs.BOT)){
                        botCnt++;
                    }else if (thisTicket.lastOperation.equals(slopeDefs.SOLD)){
                        soldCnt++;
                    }
                }
                if (thisTicket.curTrend.equals(slopeDefs.TREND_UP)){
                    trendUpCnt++;
                }else if (thisTicket.curTrend.equals(slopeDefs.TREND_DN)){
                    trendDnCnt++;
                }
                histDataErrs = actPositions.histDataProblemCnt;
                tradeErrors = actPositions.tradeErrorsCnt;
                actPositions.newPositions = totalNew;
                actPositions.closedPositions = totalClosed;
                actPositions.openPositions = totalOpen;
            }            
            void displayIt() {
                DecimalFormat df = new DecimalFormat("#.##");

                //do open line
                totalsTable.getModel().setValueAt(df.format(openOrigCost), oOpenPosRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt(df.format(openRunning), oOpenPosRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt(df.format(openPercent), oOpenPosRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(totalOpen), oOpenPosRow, oTotals);
                //do closeed line
                totalsTable.getModel().setValueAt(df.format(closeOrigCost), oClosedPosRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt(df.format(closeRunning), oClosedPosRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt(df.format(closePercent), oClosedPosRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(totalClosed), oClosedPosRow, oTotals);
                //and totals line
                totalsTable.getModel().setValueAt(df.format(totalOrigCost), oTotalsRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt(df.format(totalRunning), oTotalsRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt(df.format(totalPercent), oTotalsRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(totalOpenClosed), oTotalsRow, oTotals);

                totalsTable.getModel().setValueAt("-", oEmptyRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oEmptyRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oEmptyRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(totalNew), oEmptyRow, oTotals);
                //trades today..
                totalsTable.getModel().setValueAt("-", oTradesTdyRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oTradesTdyRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oTradesTdyRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(tradesToday) + 
                                                  "(B" + Integer.toString(botCnt) +
                                                  "S" + Integer.toString(soldCnt) +
                                                  ")" , oTradesTdyRow, oTotals);
                 //trendUp today..
                totalsTable.getModel().setValueAt("-", oTrendUpRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oTrendUpRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oTrendUpRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(trendUpCnt), oTrendUpRow, oTotals);
                //trendDn today..
                totalsTable.getModel().setValueAt("-", oTrendDnRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oTrendDnRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oTrendDnRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(trendDnCnt), oTrendDnRow, oTotals);
                //historical data errors today..
                totalsTable.getModel().setValueAt("-", oHistDataErrRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oHistDataErrRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oHistDataErrRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(histDataErrs), oHistDataErrRow, oTotals);
                //trade errors today..
                totalsTable.getModel().setValueAt("-", oTradeErrsRow, oOrigCostColmn);
                totalsTable.getModel().setValueAt("-", oTradeErrsRow, oRunningPLColmn);
                totalsTable.getModel().setValueAt("-", oTradeErrsRow, oPercentColmn);
                totalsTable.getModel().setValueAt(Integer.toString(tradeErrors), oTradeErrsRow, oTotals);
                liqValueLabel.setText(Double.toString(myUtils.roundMe(netLiqValue, 2)) + " ( " + Double.toString(myUtils.roundMe((openOrigCost + openOrigCostShort), 2)) + " )");
            }

            void resetTotals() {
                openOrigCost = 0.0;
                openOrigCostShort = 0.0;
                netLiqValue = 0.0;
                openRunning = 0.0;
                closeOrigCost = 0.0;
                closeRunning = 0.0;
                totalOrigCost = 0.0;
                totalRunning = 0.0;
                openPercent = 0.0;
                closePercent = 0.0;
                totalPercent = 0.0;
        
                totalOpen = 0;
                totalClosed = 0;
                totalNew = 0;
                totalOpenClosed = 0;
                tradesToday = 0;
                trendUpCnt = 0;
                trendDnCnt = 0;
                botCnt = 0;
                soldCnt = 0;

            }
            public void initTotalsTable(TradeTickets ticketsIn){
                int idx;
                /*
                 initialize the table with data orignally read from file that is in traderTickets[]...
                 */
                for (idx = 0; idx < ticketsIn.numOfTickets; idx++) {
                    this.updateTotalsTextArea(ticketsIn, idx, false/*initialize*/);
                    actPositions.newPositions = this.totalNew;
                    actPositions.closedPositions = this.totalClosed;
                    actPositions.openPositions = this.totalOpen;
                }
                this.displayIt();

            }
            public void updateTotalsTable(TradeTickets ticketsIn){
                int idx;
                /*
                 update totals table
                 */
                this.resetTotals();
                for (idx = 0; idx < ticketsIn.numOfTickets; idx++) {
                    this.updateTotalsTextArea(ticketsIn, idx, false/*initialize*/);
                    actPositions.newPositions = this.totalNew;
                    actPositions.closedPositions = this.totalClosed;
                    actPositions.openPositions = this.totalOpen;
                }
                this.displayIt();

            }
            
        }
        public void openStreams(TradeTickets allTickets){
            int idx;
            String actTicker;
            int numOfTickers;
            String progressStr;
            numOfTickers = allTickets.numOfTickets;
            System.out.println("\nopenStreams: " + numOfTickers);
            for (idx = 0; idx < numOfTickers; idx++){
                actTicker = allTickets.tickets[idx].ticker;
                System.out.println("working on opening: " + actTicker + " (" + idx + ") Stream.");
                        progressStr = "working on opening: " + actTicker + " (" + idx + ") Stream." ;
                        progressLable.setText(progressStr);
                qInfo = actChain.getQuote(actTicker, false);
            }
            
            System.out.println("\nopenStreams: Done.");
            myUtils.delay(1000);
        }
        
        int calculateAvailFunds(TradeTickets allTickets){
            int availFunds = 0;
            int currentTotalCost = 0;
            int idx;
            for(idx = 0; idx < allTickets.numOfTickets; idx++ ){
                if((longOrShort.equals(slopeDefs.oBiasLongStr)) && (allTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))) {
                //use shares at had to cover shorts too..
                //if((longOrShort.equals(slopeDefs.oBiasLongStr)) && (allTickets.tickets[idx].sharesAtHand > 0)) {
                    //current cost for shorts will be negative so will subtract nicely..
                    currentTotalCost += allTickets.tickets[idx].currentCost;    
                }
                else if ((longOrShort.equals(slopeDefs.oBiasShortStr)) && (allTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))){
                    currentTotalCost += allTickets.tickets[idx].currentCost;
                }
            }
            if (currentTotalCost > actPositions.availFunds){
                System.out.println("\n currentCost: " + currentTotalCost + " too high. availFunds: " + actPositions.availFunds);
                availFunds = 0;
            }else{
                availFunds = actPositions.availFunds - currentTotalCost;                       
            }
            return(availFunds);
        }
        void updateQuotes(TradeTickets ticketsIn) {
            int idx;
            System.out.println("\nupdateQuotes..");
            for (idx = 0; idx < ticketsIn.numOfTickets; idx++) {
                qInfo = actChain.getQuote(ticketsIn.tickets[idx].ticker, false);
                if (qInfo.last != 0.0) {
                    ticketsIn.tickets[idx].lastPrice = qInfo.last;
                } else {

                }
            }
            System.out.println("updateQuotes..done.");
        }
        void updateTradeActivity(TradeTickets ticketsIn, TradeRequestList tradeList){
            int idx;
            TradeRequestData actTradeReq = new TradeRequestData();
            OrderResult result = new OrderResult();
            System.out.println("\nupdateTradeActivity..");
            String tradeSpecifics = "";
            for (idx = 0; idx < ticketsIn.numOfTickets; idx++) {
                if((actTradeReq = tradeList.findOne(ticketsIn.tickets[idx].ticker)) != null){
                    //found this ticker in tradeList so process..
                    if((actTradeReq.isFilledNow() == true) && (actTradeReq.IsUpdated() == false)){
                        tradeSpecifics = actTradeReq.getTradeSpecifics();
                        result.bot = (actTradeReq.getOperation().equals(TradeRequestData.TradeOptions.oOpBuy));
                        result.filled = true;
                        result.price = actTradeReq.getFilledPrice();
                        result.quantity = actTradeReq.getSharesTraded();  
                        if(actTradeReq.IsUpdated() == false){
                            updateTicketDataSegmented(ticketsIn.tickets[idx], result, tradeSpecifics);
                            updateTraderStatusTbl(idx, ticketsIn.tickets[idx], false/*init*/);
                            actTradeReq.setIsUpdated(true);
                        }                        
                        System.out.println("\nSuccess in placing order with: " + ticketsIn.tickets[idx].ticker);
                        actPositions.todaysTrades++; 
                        actTradeRequestList.bumpFillCnt();
                    }
                };
            }
            System.out.println("updateTradeActivity..done.");
        }
        void checkSellCriteriaTrade(TradeTickets ticketsIn){
            /*
            this routine will look through all tickets for gainLock/liquidateAll/stopLoss and trade 
            them immediately.
            */
            int idx;
            String actTicker;
            double openPosValuePercent;
            boolean buyIt = false;
            boolean sellIt = false;
            TradeTicket hotTicket = new TradeTicket(false);
            OrderResult result = new OrderResult();
       
            for (idx = 0; idx < ticketsIn.numOfTickets; idx++) {
                actTicker = tradeTickets.tickets[idx].ticker;
                if (tradeTickets.tickets[idx].lockGain == true) {
                    System.out.println("\nLockGain set TRUE!! LockGain = "
                            + tradeTickets.tickets[idx].lockGainPercent
                            + " for ticker: " + actTicker
                    );
                    openPosValuePercent = getOpenPosValuePercent(tradeTickets.tickets[idx], tradeTickets.tickets[idx].lastPrice);
                    System.out.println("\nLockGain == true with " + actTicker + " lockGainPercent: " + tradeTickets.tickets[idx].lockGainPercent);
                    if (openPosValuePercent >= tradeTickets.tickets[idx].lockGainPercent) {
                        
                        System.out.println("\nSet Close position true here.");
                        commentsTextArea.append("\nLockGain Triggered Sale: "
                                + actTicker + " LockG: "
                                + tradeTickets.tickets[idx].lockGainPercent
                                + " openPosG: " + openPosValuePercent
                        );
                        if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                            //long, sellit to close
                            sellIt = true;
                            buyIt = !sellIt;
                        }else{
                            //short, buyit to close
                            buyIt = true;
                            sellIt = !buyIt;
                        }
                        
                    }else{
                        
                    }
                }else if (tradeTickets.tickets[idx].stopLoss == true){
                    System.out.println("\nStopLoss set TRUE!! StopLoss = "
                            + tradeTickets.tickets[idx].stopLossPercent
                            + " for ticker: " + actTicker
                    );
                    openPosValuePercent = getOpenPosValuePercent(tradeTickets.tickets[idx], tradeTickets.tickets[idx].lastPrice);
                    System.out.println("\nStopLoss == true with " + actTicker + " stopLossPercent: " + tradeTickets.tickets[idx].stopLossPercent);
                    if (openPosValuePercent <= (tradeTickets.tickets[idx].stopLossPercent) * -1.0) {
                        System.out.println("\nSet Close position true here.");
                        commentsTextArea.append("\nStopLoss Triggered Sale: "
                                + actTicker + " StopL: "
                                + tradeTickets.tickets[idx].stopLossPercent
                                + " openPosG: " + openPosValuePercent
                        );
                        if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                            //long, sellit to close
                            sellIt = true;
                            buyIt = !sellIt;
                        }else{
                            //short, buyit to close
                            buyIt = true;
                            sellIt = !buyIt;
                        }
                    }else{
                        
                    }                  
                }
                if (tradeTickets.tickets[idx].liquidateAll == true) {
                    System.out.println("\nLiquidating set TRUE!! ");
                    commentsTextArea.append("\nLiquidating! " + actTicker);
                    if (longOrShort.equals(slopeDefs.oBiasLongStr)) {
                        //long, sellit to close
                        sellIt = true;
                        buyIt = !sellIt;
                    } else {
                        //short, buyit to close
                        buyIt = true;
                        sellIt = !buyIt;
                    }

                    
                } else if ((false) && (userSelectedLiquidateAll == true)) {
                    
                    //if we are liquidating block opening positions..
                    buyIt = false;
                    if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                        buyIt = false;
                        sellIt = !buyIt;
                    }else{
                        sellIt = false;
                        buyIt = !sellIt;
                    }
                    
                }
                if ((sellIt == true) || (buyIt == true)) {
//                    sellIt = false;
//                    buyIt = false;
                    /* 
                     tag that we traded today, so we don't trigger another trade for this
                     position again today.
                     */
                    tradeTickets.tickets[idx].didWeTradeToday = slopeDefs.YES;
                    //for easier processing put it in hotTicket..
                    hotTicket = tradeTickets.tickets[idx];
                    System.out.println("\nClose!!");
                    //keep currentTrend it has not changed...just add trade asterisk
                    hotTicket.curTrend += "*";
                    if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                        hotTicket.sellOrderPlaced = true;
                        hotTicket.buyOrderPlaced = false; 
                        buyIt = false;                       
                    }else{                       
                        hotTicket.sellOrderPlaced = false;
                        hotTicket.buyOrderPlaced = true;
                        buyIt = true;                        
                    }
                    result = placeOrder(actTicker, buyIt, hotTicket.sharesAtHand);
                    buyIt = false;
                    sellIt = false;
                    if (result.filled == true) {
                        updateTicketData(hotTicket, result);                        
                    } else {
                        System.out.println("ORDER NOT FILLED????? Canceling order: " + result.tradeId);
                        actPositions.tradeErrorsCnt++;
                        actIbApi.testOrder.cancelOrder(result.tradeId);
                        commentsTextArea.append("\nError placing order on " + actTicker);
                        if (result.error == true){
                            commentsTextArea.append("\n  Error messagae: " + result.err);    
                        }
                    }
                    //reset the triggers..
                    if (hotTicket.lockGain == true) {
                        hotTicket.lockGain = false;
                        hotTicket.lockGainPercent = 0.0;
                    }
                    if (hotTicket.stopLoss == true) {
                        hotTicket.stopLoss = false;
                        hotTicket.stopLossPercent = 0.0;
                    }
                    if (hotTicket.liquidateAll == true) {
                        hotTicket.liquidateAll = false;
                    }
                    myUtils.delay(1000);
                    initTraderStatusTable(tradeTickets);
                } 
            }
            
        }
        void checkBuyCriteriaTrade(TradeTickets ticketsIn){
            
        }
        public class TradeState{
            boolean currentChangeInDirection;
            String currentTrend;
            boolean didWeTradeToday;
            String actTicker;
            //New trade conditions...
            boolean openBasedOnPivot;
            boolean closeBasedOnPivot;
            boolean openBasedOnTrend;
            boolean closeBasedOnTrend;
            boolean closeReopen;
            boolean openBasedOnClosing;
            boolean closeBasedOnClosing;
            boolean openBasedOnMaBounce;
            boolean openBasedOnWeakness;
            boolean openBasedOnStrength;
            boolean closeBasedOnWeakness;
            boolean closeBasedOnStrength;
            boolean segmentedTrading = false;
            int sharesToTrade;
            
            // any value > 0 means open/close based on closing days...
            int openBasedOnClosingDays;
            int closeBasedOnClosingDays;
            int reOpenBasedOnHysteresisDays;
            int openBasedOnMaBounceWithinPercent;
            int overShootPercent;
            int currentBias;
            boolean openBasedOnClosingDaysTrigger;
            boolean closeBasedOnClosingDaysTrigger;
            boolean hysteresisExpired;
            boolean hysteresisReopenTrigger;
            boolean openBasedOnMaBounceTrigger;
            boolean openBasedOnWeaknessTrigger;
            boolean openBasedOnStrengthTrigger;
            boolean closeBasedOnWeaknessTrigger;
            boolean closeBasedOnStrengthTrigger;
            
            String tradeDescriptionSuccess = "";
            String tradeDescription = "";
            static final int  PIVOT_BIT = 0;
            static final int  CLOSING_BIT = 1;
            static final int OR_BIT = 2;
            static final int TREND_BIT = 3;
            static final int REOPEN_BIT = 4;
            static final int MABOUNCE_BIT = 5;
            static final int WEAKNESS_BIT = 6;
            static final int STRENGTH_BIT = 7;
            static final int DISABLE = 0;
            int openState = DISABLE;
            int closeState = DISABLE;
            boolean openOrSet;
            boolean closeOrSet;
            
            static final int STATE_DISABLED = 0;                            //valid
            static final int STATE_PIVOT_ONLY = 1;                          //valid
            static final int STATE_CLOSE_ONLY = 2;                          //valid
            static final int STATE_PIVOT_AND_CLOSE = 3;                     //valid
            static final int STATE_OR_SET_ONLY_NOTVALID = 4;                //not valid
            static final int STATE_OR_SET_PIVOT_SET_NOTVALID = 5;           //not valid
            static final int STATE_OR_SET_CLOSE_SET_NOTVALID = 6;           //not valid
            static final int STATE_OR_SET_PIVOT_OR_CLOSE = 7;               //valid
            static final int STATE_TREND_ONLY_NOTVALID = 8;                 //not valid
            static final int STATE_TREND_AND_PIVOT_NOTVALID = 9;            //not valid
            static final int STATE_TREND_AND_CLOSE = 10;                    //valid
            static final int STATE_TREND_AND_CLOSE_AND_PIVOT = 11;          //valid
            static final int STATE_TREND_OR_SET_NOTVALID = 12;          //not valid
            static final int STATE_TREND_OR_SET_PIVOT_SET_NOTVALID = 13;//not valid
            static final int STATE_TREND_OR_SET_CLOSE_SET_NOTVALID = 14; //not valid
            static final int STATE_TREND_CLOSE_OR_PIVOT = 15;    
            static final int STATE_REOPEN_POSITION = 16;
            static final int STATE_MABOUNCE_ONLY = 32;
            static final int STATE_ON_WEAKNESS = 64;
            static final int STATE_ON_STRENGTH = 128;

            public TradeState(){
                
                currentChangeInDirection = false;
                currentTrend = "";
                didWeTradeToday = false;
                actTicker = "";
                openBasedOnPivot = false;
                closeBasedOnPivot = false;
                openBasedOnClosingDaysTrigger = false;
                closeBasedOnClosingDaysTrigger = false;
                openBasedOnMaBounce = false;
                openBasedOnMaBounceTrigger = false;
                hysteresisExpired = false;
                hysteresisReopenTrigger = false;
                openBasedOnClosing = false;
                closeBasedOnClosing = false;
                closeReopen = false;        
                openBasedOnClosingDays = 0;
                closeBasedOnClosingDays = 0;
                reOpenBasedOnHysteresisDays = 0;
                openBasedOnMaBounceWithinPercent = 0;
                openOrSet = false;
                closeOrSet = false;
                openBasedOnWeakness = false;
                openBasedOnStrength = false;
                closeBasedOnWeakness = false;
                closeBasedOnStrength = false;
                overShootPercent = 0;
            }
            public void setUpTradeConditions(traderConfigParams tc, ibApi.historicalData hd) {
                int daysCnt;
                int maMustTouchCount;
                int maMustBeAboveCount;
                int maPiercesAllowedCount;
                boolean met50MaBounce;
                boolean met100MaBounce;
                boolean met200MaBounce;
                int positionNumber = 0;
                openBasedOnPivot = traderConfigParams.isPivotSignalSet(tc.getOpenWhenCode());
                closeBasedOnPivot = traderConfigParams.isPivotSignalSet(tc.getCloseWhenCode());
                currentBias = traderConfigParams.getBias();
                segmentedTrading = false;
                /*
                    check if open pos based on Prev closing price set.
                    if so, and if long, count number of days above 10Ma. If short,
                    then count number of days below 10Ma.
                */
                hd.countClosingDaysTransitions10dMa(0);
                if (traderConfigParams.isClosePriceSet(tc.getOpenWhenCode()) == true) {
                    openBasedOnClosingDays = tc.getOpenCloseDays();
                    openBasedOnClosing = true;
                    if (currentBias == slopeDefs.oBiasLong) {
                        daysCnt = hd.countClosingDaysTo10dMa(/*aboveMa*/true, openBasedOnClosingDays);                       
                    }else{
                        daysCnt = hd.countClosingDaysTo10dMa(/*belowMa*/false, openBasedOnClosingDays);
                    }
                    // if count == wanted days, set trigger
                    if (daysCnt == openBasedOnClosingDays){
                        openBasedOnClosingDaysTrigger = true;
                    }else{
                        openBasedOnClosingDaysTrigger = false;
                    }
                } else {
                    openBasedOnClosingDays = 0;
                    openBasedOnClosing = false;
                }
                if (traderConfigParams.isClosePriceSet(tc.getCloseWhenCode()) == true) {
                    closeBasedOnClosingDays = tc.getCloseCloseDays();
                    closeBasedOnClosing = true;
                    if (currentBias == slopeDefs.oBiasLong) {
                        daysCnt = hd.countClosingDaysTo10dMa(/*aboveMa*/false, closeBasedOnClosingDays);
                    } else {
                        daysCnt = hd.countClosingDaysTo10dMa(/*belowMa*/true, closeBasedOnClosingDays);
                    }
                    // if count == wanted days, set trigger
                    if (daysCnt == closeBasedOnClosingDays) {
                        closeBasedOnClosingDaysTrigger = true;
                    } else {
                        closeBasedOnClosingDaysTrigger = false;
                    }
                } else {
                    closeBasedOnClosingDays = 0;
                    closeBasedOnClosing = false;
                }
                if (traderConfigParams.isOrSet(tc.getOpenWhenCode()) == true){
                    openOrSet = true;
                }else{
                    openOrSet = false;
                }
                if (traderConfigParams.isOrSet(tc.getCloseWhenCode()) == true){
                    closeOrSet = true;
                }else{
                    closeOrSet = false;
                }
                
                if (traderConfigParams.isTrendGoodSet(tc.getOpenWhenCode()) == true){
                   reOpenBasedOnHysteresisDays = tc.getOpenTrendDays();
                   openBasedOnTrend = true;
                }else{
                    reOpenBasedOnHysteresisDays = 0;
                    openBasedOnTrend = false;
                }
                if (traderConfigParams.isTrendGoodSet(tc.getCloseWhenCode()) == true){
                   closeBasedOnTrend = true;
                }else{                   
                    closeBasedOnTrend = false;
                }
                //
                if (traderConfigParams.isReopenSet(tc.getCloseWhenCode()) == true){
                   reOpenBasedOnHysteresisDays = tc.getCloseReopenDays();
                   closeReopen = true;
                }else{
                    reOpenBasedOnHysteresisDays = 0;
                    closeReopen = false;
                }
                                
                //open based on moving average bounce..
                if (traderConfigParams.isMaBounceSet(tc.getOpenWhenCode()) == true) {
                    openBasedOnMaBounce = true;
                    if (maWindowSizes == null){
                        //first time so set up windowsz..
                        maWindowSizes = saOuter.new MaWindowSz(/*askUserInput*/ false);
                        maWindowSizes.set50DaySz(actPositions.ma50DaySz);
                        maWindowSizes.set100DaySz(actPositions.ma100DaySz);
                        maWindowSizes.set200DaySz(actPositions.ma200DaySz);
                    }
                    hd.calcPercentages(maWindowSizes);
                    openBasedOnMaBounceWithinPercent = tc.getOpenMaBouncePercent();
                    //do 50ma
                    maMustTouchCount = (int)Math.ceil((actPositions.maMustTouchPercent / 100.0 ) * (double)(maWindowSizes.get50DaySz()));
                    maMustBeAboveCount = (maWindowSizes.get50DaySz() - maMustTouchCount);
                    /*calculate how many times we can pierce ma ..
                     This is a percentage of the window size selected..
                     */
                    maPiercesAllowedCount = (int) Math.ceil((actPositions.maMaxPiercePercent / 100.0) * (double) (maWindowSizes.get50DaySz()));
                
                    met50MaBounce = ((hd.getTimesAbove(MA_50DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_50DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_50DAY) <= maPiercesAllowedCount));
                   
                    //do 100ma
                    maMustTouchCount = (int)Math.ceil((actPositions.maMustTouchPercent / 100.0 ) * (double)(maWindowSizes.get100DaySz()));
                    maMustBeAboveCount = (maWindowSizes.get100DaySz() - maMustTouchCount);
                    /*calculate how many times we can pierce ma ..
                     This is a percentage of the window size selected..
                     */
                    maPiercesAllowedCount = (int) Math.ceil((actPositions.maMaxPiercePercent / 100.0) * (double) (maWindowSizes.get100DaySz()));
                    met100MaBounce = ((hd.getTimesAbove(MA_100DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_100DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_100DAY) <= maPiercesAllowedCount));
                    //do 200ma
                    maMustTouchCount = (int)Math.ceil((actPositions.maMustTouchPercent / 100.0 ) * (double)(maWindowSizes.get200DaySz()));
                    maMustBeAboveCount = (maWindowSizes.get200DaySz() - maMustTouchCount);
                    /*calculate how many times we can pierce ma ..
                     This is a percentage of the window size selected..
                     */
                    maPiercesAllowedCount = (int) Math.ceil((actPositions.maMaxPiercePercent / 100.0) * (double) (maWindowSizes.get200DaySz()));
                    met200MaBounce = ((hd.getTimesAbove(MA_200DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_200DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_200DAY) <= maPiercesAllowedCount));
                    //now see if we trigger for one of them..
                    if (met50MaBounce == true){
                        System.out.println("\n  Ticker: " + actTicker + " bouncing off 50dma..");
                        openBasedOnMaBounceTrigger = hd.isCurrentCloseWithinMa(MA_50DAY, openBasedOnMaBounceWithinPercent);    
                    }else if (met100MaBounce == true){
                        System.out.println("\n  Ticker: " + actTicker + " bouncing off 100dma..");
                        openBasedOnMaBounceTrigger = hd.isCurrentCloseWithinMa(MA_100DAY, openBasedOnMaBounceWithinPercent); 
                    }else if (met200MaBounce == true){
                        System.out.println("\n  Ticker: " + actTicker + " bouncing off 200dma..");
                        openBasedOnMaBounceTrigger = hd.isCurrentCloseWithinMa(MA_200DAY, openBasedOnMaBounceWithinPercent);
                    }                   
                } else {
                    openBasedOnMaBounceWithinPercent = 0;
                    openBasedOnMaBounce = false;
                }

                if (hysteresisExpired == true){
                    /* days idle expired, time to check if yesterdays
                       close was above (long), or below (short), to trigger
                       re-entering the position. Count back one day.
                    */
                    if (currentBias == slopeDefs.oBiasLong) {
                        //long, if above 10dma yesterday, get back in...
                        daysCnt = hd.countClosingDaysTo10dMa(/*aboveMa*/true, 1);
                    } else {
                        //short, if below 10dma yester, get back in....
                        daysCnt = hd.countClosingDaysTo10dMa(/*belowMa*/false, 1);
                    }
                    // if count == 1 for yesterday, set trigger
                    if (daysCnt == 1) {
                        hysteresisReopenTrigger = true;
                    } else {
                        hysteresisReopenTrigger = false;
                    }
                    
                }
                //check the open for open on weakness..
                if (traderConfigParams.isWeaknessSet(tc.getOpenWhenCode()) == true){
                    openBasedOnWeakness = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oOPEN)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            openBasedOnWeaknessTrigger = true;
                            segmentedTrading = true;
                        } else {
                            openBasedOnWeaknessTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    openBasedOnWeakness = false;
                    //segmentedTrading = false;
                }
                //check the open for open on strength..
                if (traderConfigParams.isStrengthSet(tc.getOpenWhenCode()) == true){
                    openBasedOnStrength = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oOPEN)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            openBasedOnStrengthTrigger = true;
                            segmentedTrading = true;
                        } else {
                            openBasedOnStrengthTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    openBasedOnStrength = false;
                    //segmentedTrading = false;
                }
                 //check the close for close on strength..
                if (traderConfigParams.isStrengthSet(tc.getCloseWhenCode()) == true){
                    closeBasedOnStrength = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oCLOSE)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            closeBasedOnStrengthTrigger = true;
                            segmentedTrading = true;
                        } else {
                            closeBasedOnStrengthTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    closeBasedOnStrength = false;
                    //segmentedTrading = false;
                }
                //check the close for close on weakness..
                if (traderConfigParams.isWeaknessSet(tc.getCloseWhenCode()) == true){
                    closeBasedOnWeakness = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oCLOSE)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            closeBasedOnWeaknessTrigger = true;
                            segmentedTrading = true;
                        } else {
                            closeBasedOnWeaknessTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    closeBasedOnWeakness = false;
                    //segmentedTrading = false;
                }
            }
            
            public void setUpTradeConditionsLinear(traderConfigParams tc, ibApi.historicalData hd, TradeTicket tradeTicket) {
                /*
                segemented linear trade is handled here. 
                If we are buying on weakness, we check to see if there are short
                segments open, if so, we buy those back first before buying long segments. 
                If we are selling on strenghth, we check to see
                if there are any long positions open and sell those first, otherwise we sell short positions.
                Doing it this way means as a position gets weak, we close shorts if any, and if weakness persists,
                we buy long positions at an even lower price. Conversely, when we are closing on strength, we sell our longs
                if any and if strength persists, we sell short at an even higher price.
                Note: there are two seperate trade segments kept, one for longs and one for shorts. each have same number of
                segments: 6 each. 6 was chosen impericaly. All these segmetns are stored away in tradeTickets 
                that are stored in a file. tradeTicket was added as input paramater.
                */
                int daysCnt;               
                int positionNumber = 0;               
                segmentedTrading = false;         
                
                //if we traded today split..
                if(tradeTicket.didWeTradeToday.equals(slopeDefs.YES)){
                    return;
                }
                //check the open for open on weakness..
                if (traderConfigParams.isWeaknessSet(tc.getOpenWhenCode()) == true){
                    openBasedOnWeakness = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oOPEN)) {
                            //open on weakness is true, so first check if we have an open short 
                            //segment to buy back (to close).
                            if (tradeTicket.segTradesOverShoot[tradeTicket.actSegTradeOverShoot].segOpen == false){
                                //this seg closed already bump to next ..
                                if (tradeTicket.actSegTradeOverShoot > 0){
                                    tradeTicket.actSegTradeOverShoot--;
                                } else {
                                    //no shorts to close look at longs to open..
                                    if (tradeTicket.segTrades[tradeTicket.actSegTrade].segOpen == true) {
                                        //this is already open bump to next if not all open..
                                        if (tradeTicket.actSegTrade < (segTradingScale - 1)) {
                                            tradeTicket.actSegTrade++;
                                        } else {
                                            //all full no more to open..
                                            return;
                                        }
                                        //open a long... 
                                        tradeTicket.segTradeDoThisOne = tradeTicket.segTrades;
                                        tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTrade;                                        
                                        sharesToTrade = hd.getSharesToTrade(positionNumber);
                                        openBasedOnWeaknessTrigger = true;
                                        segmentedTrading = true;
                                        return;
                                    } else {
                                        //open long ..
                                        tradeTicket.segTradeDoThisOne = tradeTicket.segTrades;
                                        tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTrade;
                                        sharesToTrade = hd.getSharesToTrade(positionNumber);
                                        openBasedOnWeaknessTrigger = true;
                                        segmentedTrading = true;
                                        return;
                                    }
                                }
                                //overshoot short is open so close and leave..
                                tradeTicket.segTradeDoThisOne = tradeTicket.segTradesOverShoot;
                                tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTradeOverShoot;
                                closeBasedOnWeaknessTrigger = true;
                                segmentedTrading = true;
                                //changed to get shares to close from segments, since this is the number we sold..
                                //sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);
                                sharesToTrade = tradeTicket.segTradesOverShoot[tradeTicket.actSegTradeOverShoot].segShares;
                                return;
                            }else{
                                //overshoot short is open so close and leave..
                                tradeTicket.segTradeDoThisOne = tradeTicket.segTradesOverShoot;
                                tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTradeOverShoot;                                
                                closeBasedOnWeaknessTrigger = true;
                                segmentedTrading = true;
                                //changed to get shares to close from segments, since this is the number we sold..
                                //sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);                                
                                sharesToTrade = tradeTicket.segTradesOverShoot[tradeTicket.actSegTradeOverShoot].segShares;
                                return;
                            }                            
                        } else {
                            //operation not to open so set these triggers false;
                            openBasedOnWeaknessTrigger = false;
                            closeBasedOnWeaknessTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    openBasedOnWeakness = false;
                    //segmentedTrading = false;
                }
                //check the open for open on strength..
                if (traderConfigParams.isStrengthSet(tc.getOpenWhenCode()) == true){
                    openBasedOnStrength = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oOPEN)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            openBasedOnStrengthTrigger = true;
                            segmentedTrading = true;
                        } else {
                            openBasedOnStrengthTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    openBasedOnStrength = false;
                    //segmentedTrading = false;
                }
                 //check the close for close on strength..
                if (traderConfigParams.isStrengthSet(tc.getCloseWhenCode()) == true){
                    closeBasedOnStrength = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oCLOSE)) {
                            //close on strength is true, so first sell any open long segments..
                            //if none left to close(sell), open (sell) overshoot segments..
                            if (tradeTicket.segTrades[tradeTicket.actSegTrade].segOpen == false){
                                //this one is already closed look at next one, if not all out..
                                if(tradeTicket.actSegTrade > 0){
                                    tradeTicket.actSegTrade--;
                                    //close long ...
                                    tradeTicket.segTradeDoThisOne = tradeTicket.segTrades;
                                    tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTrade;                                    
                                    //sharesToTrade = hd.getSharesToTrade(positionNumber);
                                    //changed 3/11/2016 use what we sold from segment..
                                    sharesToTrade = tradeTicket.segTrades[tradeTicket.actSegTrade].segShares;
                                    closeBasedOnStrengthTrigger = true;
                                    segmentedTrading = true;
                                    return;
                                } else if (tradeTicket.segTradesOverShoot[tradeTicket.actSegTradeOverShoot].segOpen == true){
                                    //this one already open so bump to next one if there are any left..
                                    if(tradeTicket.actSegTradeOverShoot < (segTradingScale - 1)){
                                        tradeTicket.actSegTradeOverShoot++;
                                    }else{
                                        //no more shorts to open..leave
                                        return;
                                    } 
                                    // sell short..if shares to short..check first..
                                     sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);
                                     if(sharesToTrade > 0){
                                        tradeTicket.segTradeDoThisOne = tradeTicket.segTradesOverShoot;
                                        tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTradeOverShoot;                                     
                                        sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);
                                        openBasedOnStrengthTrigger = true;
                                        segmentedTrading = true;                                         
                                     }else{
                                         
                                     }                                     
                                     return;
                                }else{
                                    // sell short..if shares to short..check first..
                                     sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);
                                     if(sharesToTrade > 0){
                                        tradeTicket.segTradeDoThisOne = tradeTicket.segTradesOverShoot;
                                        tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTradeOverShoot;                                     
                                        sharesToTrade = hd.getSharesToTradeOverShoot(positionNumber);
                                        openBasedOnStrengthTrigger = true;
                                        segmentedTrading = true;                                         
                                     }else{
                                         
                                     }                                                                          
                                     return;
                                }                                
                            }else {
                                //this one is open so close the long 
                                tradeTicket.segTradeDoThisOne = tradeTicket.segTrades;
                                tradeTicket.actSegTradeDoThisOne = tradeTicket.actSegTrade;                                
                                //sharesToTrade = hd.getSharesToTrade(positionNumber);
                                //changed 3/11/2016 use what we sold from segment..
                                sharesToTrade = tradeTicket.segTrades[tradeTicket.actSegTrade].segShares;
                                closeBasedOnStrengthTrigger = true;
                                segmentedTrading = true;
                                return;
                            }
                        } else {
                            closeBasedOnStrengthTrigger = false;
                            openBasedOnStrengthTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    closeBasedOnStrength = false;
                    //segmentedTrading = false;
                }
                //check the close for close on weakness..
                if (traderConfigParams.isWeaknessSet(tc.getCloseWhenCode()) == true){
                    closeBasedOnWeakness = true;
                    overShootPercent = tc.getOverShootPercent();
                    if ((positionNumber = hd.searchForTicker(actTicker)) != -1){
                        if (hd.getCurrentRealOperation(positionNumber).equals(slopeDefs.oCLOSE)) {
                            sharesToTrade = hd.getSharesToTrade(positionNumber);
                            closeBasedOnWeaknessTrigger = true;
                            segmentedTrading = true;
                        } else {
                            closeBasedOnWeaknessTrigger = false;
                        }
                    }else{
                        System.out.println("\nCould not find ticker: " + actTicker);
                    }
                }else{
                    closeBasedOnWeakness = false;
                    //segmentedTrading = false;
                }
            }            
            void setStates(){
                
                openState |= (((openBasedOnPivot == true) ? 1 : 0) << PIVOT_BIT);
                openState |= (((openBasedOnClosing == true) ? 1 : 0) << CLOSING_BIT);
                openState |= (((openOrSet == true) ? 1 : 0) << OR_BIT);
                openState |= (((openBasedOnTrend == true) ? 1 : 0) << TREND_BIT); 
                if (hysteresisReopenTrigger == true){
                    openState = (1 << REOPEN_BIT);
                }
                openState |= (((openBasedOnMaBounce == true) ? 1 : 0) << MABOUNCE_BIT);
                
                openState |= (((openBasedOnWeakness == true) ? 1 : 0) << WEAKNESS_BIT);
                openState |= (((openBasedOnStrength == true) ? 1 : 0) << STRENGTH_BIT);
                
                closeState |= (((closeBasedOnPivot == true) ? 1 : 0) << PIVOT_BIT);
                closeState |= (((closeBasedOnClosing == true) ? 1 : 0) << CLOSING_BIT);
                closeState |= (((closeOrSet == true) ? 1 : 0) << OR_BIT);
                closeState |= (((closeBasedOnTrend == true) ? 1 : 0) << TREND_BIT);
                
                closeState |= (((closeBasedOnWeakness == true) ? 1 : 0) << WEAKNESS_BIT);
                closeState |= (((closeBasedOnStrength == true) ? 1 : 0) << STRENGTH_BIT);
                //closeState |= (((closeReopen == true) ? 1 : 0) << REOPEN_BIT);
            }
            int getOpenState(){
                return openState;
            }
            int getCloseState(){
                return closeState;
            }
            int getReopenDays(){
                return reOpenBasedOnHysteresisDays;
            }
            void clrReopenBit(){
                //return (in &= ~(1 << PIVOT_SIG_INT));
                openState &= ~(1 << REOPEN_BIT);
            }
        }
        String seeIfLongBuyTrigger(TradeState tradeState, int idx) {
            boolean buyIt1 = false;
            boolean buyIt2 = false;
            String buyOperation = slopeDefs.oNoTradeYet;
            int openState;
            
            tradeState.setStates();
            openState = tradeState.getOpenState();
            
            switch (openState) {
                case TradeState.STATE_DISABLED:
                    tradeState.tradeDescription = "DISABLED";
                    break;
                case TradeState.STATE_PIVOT_ONLY:
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (buyIt1 == false) {
                        //another chance to buy...
                        buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                        if (buyIt1 == true) {
                            commentsTextArea.append("\nDelayed BUY Based On Pivot occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Pivot ";
                    }                        
                    break;
                case TradeState.STATE_CLOSE_ONLY:
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             Check that openBasedOnClosing actuall triggered.
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnClosingDaysTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Closing ";
                    }
                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. Added openBasedOnClosingTrigger
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnClosingDaysTrigger == true)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (buyIt1 == false) {
                        //another chance to buy...
                        buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && (tradeState.openBasedOnClosingDaysTrigger == true)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                        if (buyIt1 == true) {
                            commentsTextArea.append("\nDelayed BUY Based On Pivot AND CLOSE occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }                   
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Pivot AND Closing ";
                    }
                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_ONLY_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_PIVOT_SET_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_CLOSE_SET_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                    //pivot OR Close..
                    //pivot first..
                    buyIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (buyIt1 == false) {
                        //another chance to buy...
                        buyIt1 = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                        if (buyIt1 == true) {
                            commentsTextArea.append("\nDelayed BUY Based On Pivot occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //check closingTrigger
                    buyIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnClosingDaysTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                    buyIt1 = tradeTickets.tickets[idx].buyNow = (buyIt1 || buyIt2);
                    
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Pivot OR closing ";
                    }                    
                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID: 
                    tradeState.tradeDescription = "STATE_TREND_ONLY_NOTVALID";
                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_AND_PIVOT_NOTVALID";
                    break;
                case TradeState.STATE_TREND_AND_CLOSE:
                    //do trend and close in one shot
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && tradeState.openBasedOnClosingDaysTrigger
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Trend AND Closing ";
                    }
                    break; 
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                    //trend AND (CLOSE AND PIVOT)                   
                    //do pivot
                    buyIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (buyIt1 == false) {
                        //another chance to buy...
                        buyIt1 = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                        if (buyIt1 == true) {
                            commentsTextArea.append("\nDelayed BUY Based On Pivot occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //do close AND TREND
                    buyIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false) 
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeState.openBasedOnClosingDaysTrigger))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    //now close AND trend AND pivot..                   
                    buyIt1 = tradeTickets.tickets[idx].buyNow = (buyIt1 && buyIt2);
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open because Trend AND (Closing AND Pivot) ";
                    }
                    
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_NOTVALID";
                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_PIVOT_SET_NOTVALID";
                    break;
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_CLOSE_SET_NOTVALID";
                    break;
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    //trend AND (close OR Pivot)
                    //do pivot..
                    buyIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (buyIt1 == false) {
                        //another chance to buy...
                        buyIt1 = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                        if (buyIt1 == true) {
                            commentsTextArea.append("\nDelayed BUY Based On Pivot occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //do close 
                    buyIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && ((tradeState.openBasedOnClosingDaysTrigger))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    //now pivot OR close ..  buyit1 has pviot..buyIt2 has closeTrigger..                 
                    buyIt1 = (buyIt1 || buyIt2);
                    //now do trend..
                    buyIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    buyIt1 = tradeTickets.tickets[idx].buyNow = (buyIt1 || buyIt2);
                    
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open becaue Trend AND (Pivot OR Closing) ";
                    }
                    break; 
                case TradeState.STATE_REOPEN_POSITION:
                    buyIt1 = ( 
                            /* reopen position..just chck if trend is still up, we already verified
                               yesterday's close was higher than 10dma..
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))); 
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open becaue Re-open position happened ";
                    }
                    break;
                case TradeState.STATE_MABOUNCE_ONLY:  
                    System.out.println("\nMA BOUNCE STATE..");
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.                             
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnMaBounceTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (buyIt1 == true){
                        tradeState.tradeDescriptionSuccess = "Open becaue MA bounce.";
                    }
                    break;
                case TradeState.STATE_ON_WEAKNESS:
                    System.out.println("\nCheck Open on Weakness");
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                            /*  don't place order if we already did in this session 
                                don't place order if we are allIn..meaning we have
                                6 open positions already.
                             */
                            //check if all in with longs..
                            (tradeTickets.tickets[idx].segTradeAllIn(tradeTickets.tickets[idx].segTrades) == false) 
                            &&
                            (tradeState.didWeTradeToday == false)
                                      /* this could be buy to open long */               /* this could be buy to close short */
                            && ((tradeState.openBasedOnWeaknessTrigger == true) || tradeState.closeBasedOnWeaknessTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))                            
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))
                            || ((tradeState.segmentedTrading == true) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))))
                    );
                    
                    if (buyIt1 == true){
                        System.out.println("\nOpen on Weakness!!!! Shares To Trade: " + tradeState.sharesToTrade);
                        if (tradeState.openBasedOnWeaknessTrigger == true){
                            buyOperation = slopeDefs.oBuyToOpenLong;
                            tradeState.tradeDescriptionSuccess = "Open Based on Weakness (buyToOpenLong)";
                        }else if (tradeState.closeBasedOnWeaknessTrigger == true){
                            buyOperation = slopeDefs.oBuyToCloseShort;
                            tradeState.tradeDescriptionSuccess = "Close Based on Weakness (buyToCloseShort)";
                        }                       
                        tradeState.segmentedTrading = true;
                    }
                    break;
                case TradeState.STATE_ON_STRENGTH: 
                    System.out.println("\nCheck Open on Strength");
                    buyIt1 = tradeTickets.tickets[idx].buyNow = ( 
                             /*  don't place order if we already did in this session 
                                don't place order if we are allIn..meaning we have
                                6 open positions already.
                             */ 
                            (tradeTickets.tickets[idx].segTradeAllIn(tradeTickets.tickets[idx].segTrades) == false) 
                            &&
                            (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnStrengthTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))
                            || ((tradeState.segmentedTrading == true) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)))));
                    if (buyIt1 == true){
                        System.out.println("\nOpen on Weakness!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    
                    break;
            } /* switch */
            /*
            take this out, done elsewhere when we are opening a position..
            
            if ((buyIt1 == true) && (tradeTickets.tickets[idx].numOfSharesToTrade == 0)) {
                commentsTextArea.append("\nPurchase of " + tradeState.actTicker + " blocked for lack of funds.");
                buyIt1 = false;
            }
            */
            if ((buyIt1 == true) && buyOperation.equals(slopeDefs.oNoTradeYet)){
                // must be buyToOpenLong..
                buyOperation = slopeDefs.oBuyToOpenLong;
            }
                
            return buyOperation;
        }

        String seeIfLongSellTrigger(TradeState tradeState, int idx) {
            boolean sellIt1 = false;
            boolean sellIt2 = false;
            String sellOperation = slopeDefs.oNoTradeYet;
            int closeState;

            tradeState.setStates();
            closeState = tradeState.getCloseState();           
            
            switch (closeState) {
                case TradeState.STATE_DISABLED:
                    tradeState.tradeDescription = "Disabled";
                    break;
                case TradeState.STATE_PIVOT_ONLY:
                    sellIt1 = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == false) {
                        //another chance to sell...
                        sellIt1 = tradeTickets.tickets[idx].sellNow = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && /*should never be in BOT state with downtrend..
                                 means we missed the transition..*/ 
                                (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                        if (sellIt1 == true) {
                            commentsTextArea.append("\nDelayed SELL occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (sellIt1 == true){
                       tradeState.tradeDescriptionSuccess = "close because Pivot only "; 
                    }
                        
                    break;
                case TradeState.STATE_CLOSE_ONLY:
                    sellIt1 = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.                            
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == true){
                        tradeState.tradeDescriptionSuccess = "close becuase Closing only ";
                    }
                    
                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    //do pivot
                    sellIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)                           
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == false) {
                        //another chance to sell...
                        sellIt1 = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */
                                (tradeState.didWeTradeToday == false)
                                && (tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && /*should never be in BOT state with downtrend..
                                 means we missed the transition..*/ 
                                (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                        if (sellIt1 == true) {
                            commentsTextArea.append("\nDelayed SELL occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }                    
                    sellIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.                            
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    sellIt1 = tradeTickets.tickets[idx].sellNow = (sellIt1 && sellIt2);
                    if (sellIt1 == true){
                       tradeState.tradeDescriptionSuccess = "close becuase Pivot AND Closing "; 
                    }
                    
                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_ONLY_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_PIVOT_SET_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_OR_SET_CLOSE_SET_NOTVALID";
                    break;
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                    //pivot OR close..pivot first..
                    sellIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == false) {
                        //another chance to sell...
                        sellIt1 = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && /*should never be in BOT state with downtrend..
                                 means we missed the transition..*/ (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                        if (sellIt1 == true) {
                            commentsTextArea.append("\nDelayed SELL occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //do close
                    sellIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.                            
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    sellIt1 = tradeTickets.tickets[idx].sellNow = (sellIt1 || sellIt2); 
                    if (sellIt1 == true){
                        tradeState.tradeDescriptionSuccess = "close becuase Pivot OR Closing ";
                    }
                    
                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID: 
                    tradeState.tradeDescription = "STATE_TREND_ONLY_NOTVALID";
                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_AND_PIVOT_NOTVALID";
                    break;
                case TradeState.STATE_TREND_AND_CLOSE:
                    //close AND trend..
                    sellIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.                            
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)                            
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));  
                    
                    if (sellIt1 == true){
                        tradeState.tradeDescriptionSuccess = "close becuase Closing ";
                    }
                    break;
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                    //first do PIVOT
                    sellIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == false) {
                        //another chance to sell...
                        sellIt1 = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && /*should never be in BOT state with downtrend..
                                 means we missed the transition..*/ (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                        if (sellIt1 == true) {
                            commentsTextArea.append("\nDelayed SELL occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //now do trend AND close..
                    sellIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeState.closeBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))); 
                    sellIt1 = tradeTickets.tickets[idx].sellNow = (sellIt1 && sellIt2); 
                    
                    if (sellIt1 == true){
                        tradeState.tradeDescriptionSuccess = "close becuase Trend AND (Closing AND Pivot) ";
                    }
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID: 
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_NOTVALID";
                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID: 
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_PIVOT_SET_NOTVALID";
                    break;  
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_CLOSE_SET_NOTVALID";
                    break;  
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    //trend AND (close OR Pivot)
                    //first do PIVOT
                    sellIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    if (sellIt1 == false) {
                        //another chance to sell...
                        sellIt1 = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && /*should never be in BOT state with downtrend..
                                 means we missed the transition..*/ 
                                (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                        if (sellIt1 == true) {
                            commentsTextArea.append("\nDelayed SELL occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    //do close..
                    sellIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.                            
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)));
                    //pivot || close
                    sellIt1 = (sellIt1 || sellIt2);
                    //do trend..
                    sellIt2 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))); 
                    // trend AND (pivot || close)
                    sellIt1 = tradeTickets.tickets[idx].sellNow = (sellIt1 && sellIt2);
                    
                    if (sellIt1 == true){
                       tradeState.tradeDescriptionSuccess = "close becuase Trend AND (Closing OR Pivot) "; 
                    }
                    break;
                case TradeState.STATE_ON_WEAKNESS:
                    System.out.println("\nCheck Close on Weakness");
                    sellIt1 = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session                               
                             */
                           
                            (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnWeaknessTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                            || ((tradeState.segmentedTrading == true) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))))
                            );
                    if (sellIt1 == true){
                        System.out.println("\nOpen on Weakness!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    
                    break;
                case TradeState.STATE_ON_STRENGTH:
                    System.out.println("\nCheck Close on Strength");
                    sellIt1 = tradeTickets.tickets[idx].sellNow = (                           
                            /* don't place order if we already did in this session                                
                             */                           
                            (tradeState.didWeTradeToday == false)
                                     /* this could be close long on strength*/    /* this could be open short on strength */
                            && ((tradeState.closeBasedOnStrengthTrigger == true) || (tradeState.openBasedOnStrengthTrigger == true))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                            || ((tradeState.segmentedTrading == true) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD)))
                            || ((tradeState.segmentedTrading == true) && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW)) && (tradeState.openBasedOnStrengthTrigger == true))));
                    if (sellIt1 == true) {
                        System.out.println("\nClose on Strength!!!! Shares To Trade: " + tradeState.sharesToTrade);
                        tradeState.segmentedTrading = true;
                        if (tradeState.closeBasedOnStrengthTrigger == true) {
                            tradeState.tradeDescriptionSuccess = "Close Based on Strength (sellToCloseLong)";
                            sellOperation = slopeDefs.oSellToCloseLong;
                        } else if (tradeState.openBasedOnStrengthTrigger == true) {
                            tradeState.tradeDescriptionSuccess = "Open Based on Strength (sellToOpenShort)";
                            sellOperation = slopeDefs.oSellToOpenShort;
                        }
                    }

                    break;    
                    
            } /* switch */    
            if((sellIt1 == true) && (sellOperation.equals(slopeDefs.oNoTradeYet))){
                //must be sell to close long..
                sellOperation = slopeDefs.oSellToCloseLong;
            }
            return sellOperation;
        }
        
        boolean seeIfShortSellTrigger(TradeState tradeState, int idx){
            boolean openIt = false;
            boolean openIt1 = false;
            int openState;

            tradeState.setStates();
            openState = tradeState.getOpenState();           
            
            switch (openState) {
                case TradeState.STATE_DISABLED:
                    tradeState.tradeDescription = "Disabled";
                    break;
                case TradeState.STATE_PIVOT_ONLY:
                    
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == false) {
                        //another chance to sell...
                        openIt = tradeTickets.tickets[idx].sellNow = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                        if (openIt == true) {
                            commentsTextArea.append("\nDelayed SELL short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because of Pivot only";
                    }
                    break;
                case TradeState.STATE_CLOSE_ONLY:                   
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeState.openBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because Closing only ";
                    }
                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeState.openBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == false) {
                        //another chance to sell...
                        openIt = tradeTickets.tickets[idx].sellNow =  ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && (tradeState.openBasedOnClosingDaysTrigger)
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                        if (openIt == true) {
                            commentsTextArea.append("\nDelayed SELL short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }                  
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because of Pivot AND Close ";
                    }
                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID: 
                    tradeState.tradeDescription = "STATE_OR_SET_ONLY_NOTVALID ";
                    break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID: 
                    tradeState.tradeDescription = "STATE_OR_SET_PIVOT_SET_NOTVALID ";
                    break;
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID: 
                    tradeState.tradeDescription = "STATE_OR_SET_CLOSE_SET_NOTVALID ";
                    break;   
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:     
                    //pivot first..
                    openIt = ( /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && ((tradeState.currentChangeInDirection == true))
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))                       
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == false) {
                        //another chance to sell...
                        openIt = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && ((tradeState.currentChangeInDirection == true))
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                        if (openIt == true) {
                            commentsTextArea.append("\nDelayed SELL short occurred with: " + tradeState.actTicker);
                        } else {

                        }                       
                    }
                    //do close..
                    openIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.openBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    openIt = tradeTickets.tickets[idx].sellNow = (openIt || openIt1);
                    if (openIt == true) {
                        tradeState.tradeDescription = "Open because of Pivot OR Close ";
                    }
                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_ONLY_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_AND_PIVOT_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_AND_CLOSE:                    
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false) 
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeState.openBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because of Trend AND Close ";
                    }
                    break; 
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:                   
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeState.openBasedOnClosingDaysTrigger)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == false) {
                        //another chance to sell...
                        openIt = tradeTickets.tickets[idx].sellNow =  ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && (tradeState.openBasedOnClosingDaysTrigger)
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                        if (openIt == true) {
                            commentsTextArea.append("\nDelayed SELL short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    } 
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because of Trend AND Pivot AND Close ";
                    }
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_PIVOT_SET_NOTVALID ";
                    break; 
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_CLOSE_SET_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    //trend AND (close OR Pivot)
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)  
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && ((tradeState.currentChangeInDirection == true) || (tradeState.openBasedOnClosingDaysTrigger))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == false) {
                        //another chance to sell...
                        openIt = tradeTickets.tickets[idx].sellNow =  ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is DN
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was BOT, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..
                                 Only do this one time a day. becuase we are looking for no 
                                 direction change, this can bounce a few times in a day.                                                    
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                                && ((tradeState.currentChangeInDirection == true) || (tradeState.openBasedOnClosingDaysTrigger))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))
                                || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));

                        if (openIt == true) {
                            commentsTextArea.append("\nDelayed SELL short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (openIt == true){
                        tradeState.tradeDescription = "Open because of Trend AND (Close OR Pivot) ";                       
                    }
                    break; 
                case TradeState.STATE_REOPEN_POSITION:
                    openIt = ( 
                            /* reopen position..just chck if trend is still down, we already verified
                               yesterday's close was lower than 10dma..
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_DN))
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT))); 
                    if (openIt == true){
                        tradeState.tradeDescription = "Open becaue Re-open position happened ";
                    }
                    break;
                case TradeState.STATE_ON_WEAKNESS: 
                    System.out.println("\nCheck Open on Weakness");
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)  
                            && (tradeState.didWeTradeToday == false)                             
                            && (tradeState.openBasedOnWeaknessTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == true){
                        System.out.println("\nOpen on Weakness!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    break;
                case TradeState.STATE_ON_STRENGTH:
                    System.out.println("\nCheck Open on Strength");
                    openIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)  
                            && (tradeState.didWeTradeToday == false)                             
                            && (tradeState.openBasedOnStrengthTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.BOT)
                            || (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.NEW))));
                    if (openIt == true){
                        System.out.println("\nOpen on Strength!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    break;    
            } /* switch */

            if ((openIt == true) && (tradeTickets.tickets[idx].numOfSharesToTrade == 0)) {
                commentsTextArea.append("\nSell Short of  " + tradeState.actTicker + " blocked for lack of funds.");
                openIt = false;
            }
            return openIt;           
        }
        boolean seeIfShortBuyTrigger(TradeState tradeState, int idx) {
            
            /* 
             buy to close a short position. This routine is only called when we are servicing a short portfolio.
             this methode will see if we are buying back a short position previously placed.
             This would happen when a transition from down trend to up trend happenned, the last operation 
             was a sell only not open.
             */
            boolean closeIt = false;
            boolean closeIt1 = false;
            int closeState;

            tradeState.setStates();
            closeState = tradeState.getCloseState();           
            
            switch (closeState) {
                case TradeState.STATE_DISABLED:
                    tradeState.tradeDescription = "STATE DISABLED? ";
                    break;
                case TradeState.STATE_PIVOT_ONLY:
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (closeIt == false) {
                        //another chance to buy...
                        closeIt = tradeTickets.tickets[idx].buyNow = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                        if (closeIt == true) {
                            commentsTextArea.append("\nDelayed BUY-to-Close Short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Pivot only ";
                    }
                    break;
                case TradeState.STATE_CLOSE_ONLY:
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Close only ";
                    }
                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)                           
                            && (tradeState.currentChangeInDirection == true)
                            && (tradeState.closeBasedOnClosingDaysTrigger)
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (closeIt == false) {
                        //another chance to buy...
                        closeIt = tradeTickets.tickets[idx].buyNow = ( 
                                /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */
                                (tradeState.didWeTradeToday == false)
                                && (tradeState.didWeTradeToday == false)
                                && (tradeState.currentChangeInDirection == false)
                                && (tradeState.closeBasedOnClosingDaysTrigger)
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                        if (closeIt == true) {
                            commentsTextArea.append("\nDelayed BUY-to-Close Short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Pivot AND Closing ";
                    }
                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID:
                        tradeState.tradeDescription = "STATE_OR_SET_ONLY_NOTVALID ";
                        break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:
                        tradeState.tradeDescription = "STATE_OR_SET_PIVOT_SET_NOTVALID ";
                        break; 
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:
                        tradeState.tradeDescription = "STATE_OR_SET_CLOSE_SET_NOTVALID ";
                        break; 
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                    //do pivot..
                    closeIt = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && ((tradeState.currentChangeInDirection == true))
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (closeIt == false) {
                        //another chance to buy...
                        closeIt = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && ((tradeState.currentChangeInDirection == true))
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                        if (closeIt == true) {
                            commentsTextArea.append("\nDelayed BUY-to-Close Short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    closeIt1 = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
//                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    closeIt = tradeTickets.tickets[idx].buyNow = (closeIt || closeIt1);
                    if (closeIt == true) {
                        tradeState.tradeDescription = "Closed becaue Pivot OR Closing ";
                    }
                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_ONLY_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_AND_PIVOT_NOTVALID ";
                    break;  
                case TradeState.STATE_TREND_AND_CLOSE:
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && (tradeState.closeBasedOnClosingDaysTrigger == true)
//leave out trend
//                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Trend AND Close ";
                    }
                    break; 
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && ((tradeState.currentChangeInDirection == true) 
                            && (tradeState.closeBasedOnClosingDaysTrigger))
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (closeIt == false) {
                        //another chance to buy...
                        closeIt = tradeTickets.tickets[idx].buyNow = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */(tradeState.didWeTradeToday == false)
                                && ((tradeState.currentChangeInDirection == true) 
                                && (tradeState.closeBasedOnClosingDaysTrigger))
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                        if (closeIt == true) {
                            commentsTextArea.append("\nDelayed BUY-to-Close Short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Trend AND Close AND Pivot ";
                    }
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_NOTVALID ";
                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_PIVOT_SET_NOTVALID ";
                    break;   
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                    tradeState.tradeDescription = "STATE_TREND_OR_SET_CLOSE_SET_NOTVALID ";
                    break;   
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    //Trend & (close or pivot)
                    closeIt = tradeTickets.tickets[idx].buyNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is BOT already. This means we restarted a session and we already
                             did the purchase. buyOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today. 
                             */
                            (tradeTickets.tickets[idx].buyOrderPlaced == false)
                            && (tradeState.didWeTradeToday == false)
                            && ((tradeState.currentChangeInDirection == true) || (tradeState.closeBasedOnClosingDaysTrigger))                           
                            && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                            && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                    /*
                     ok. so it is possible that we missed a buy sig or sell sig during a 
                     trading day: If the program runs in the morning, and does't detect signal to buy
                     and later in the day toward the close, the stock price moves up and does signal a buy
                     (trend changed from down to up). The next day when we run the program the trend is down,
                     the transition already happen so we miss it. Same could happen on a sell signal.
                     */
                    if (closeIt == false) {
                        //another chance to buy...
                        closeIt = tradeTickets.tickets[idx].buyNow = ( /*  this is a case of missing a signal that happened yesterday.
                                 don't place order if we already did in this session. The trend is UP
                                 but change in direction did not happen (it happened yesterday). The previous 
                                 state was SOLD or NEW, it's ok to buy. Most important the signal 
                                 to buy happened one day ago; 1==yesterday, 0==today..                                      
                                 */
                                (tradeState.didWeTradeToday == false)
                                && (tradeState.didWeTradeToday == false)
                                && ((tradeState.currentChangeInDirection == true) || (tradeState.closeBasedOnClosingDaysTrigger))                              
                                && (tradeState.currentTrend.equals(slopeDefs.TREND_UP))
                                && (slopeStruct.dayInTradingYear == 1)
                                && ((tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))));
                        if (closeIt == true) {
                            commentsTextArea.append("\nDelayed BUY-to-Close Short occurred with: " + tradeState.actTicker);
                        } else {

                        }
                    }
                    if (closeIt == true){
                        tradeState.tradeDescription = "Closed becaue Trend AND (Close OR Pivot) ";
                    }
                    break; 
                case TradeState.STATE_ON_WEAKNESS: 
                    System.out.println("\nCheck Close on Weakness");
                    closeIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)  
                            && (tradeState.didWeTradeToday == false)                             
                            && (tradeState.closeBasedOnWeaknessTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            );
                    if (closeIt == true){
                        System.out.println("\nClose on Weakness!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    break;
                case TradeState.STATE_ON_STRENGTH:
                    System.out.println("\nCheck Close on Strength");
                    closeIt = tradeTickets.tickets[idx].sellNow = ( 
                            /* don't place order if we already did in this session and don't do it
                             if the State is SLD already. This means we restarted a session and we already
                             did the sell. sellOrderPlaced is not stored in the file.
                             The first term is redundant since currentChangeInDirection would be false
                             if a trade happend today.
                             */
                            (tradeTickets.tickets[idx].sellOrderPlaced == false)  
                            && (tradeState.didWeTradeToday == false)                             
                            && (tradeState.closeBasedOnStrengthTrigger == true)
                            && (tradeTickets.tickets[idx].lastOperation.equals(slopeDefs.SOLD))
                            );
                    if (closeIt == true){
                        System.out.println("\nClose on Strength!!!! Shares To Trade: " + tradeState.sharesToTrade); 
                        tradeState.segmentedTrading = true;
                    }
                    break;    
            } /* switch */
            
            return closeIt;            
        }
        public class Btparms{
            public boolean boughtLast = false;
            public boolean soldLast = false;
            public boolean firstTrade = true;
            public double costBasis = 0.0;
            public double currentCostBasis = 0.0;
            public final int NUM_SHARES = 100;
            public double currentRunningPl = 0.0;
            public int whatDay = 0;
            public int tradesCnt = 0;
            public int hysteresisDays = 0;
        }
        public class BtTotals{
            public double currentRunningPl = 0.0;
            public int numOfWinners = 0;
            public int numOfLosers = 0;
            public double runningLoss = 0.0;
            public double runningGain = 0.0;
            public double costBasis = 0.0;
            public int numTickers = 0;           
            public double openPosPl = 0.0;
            public double runningOpenPosPl = 0.0;
        }
        Btparms btp = new Btparms();
        BtTotals btTotals = new BtTotals();
        BackTestDataOneYear bt1Year = null;
        
        void backTestOpenLong(BackTestDataOneDay oneDay){
            oneDay.boughtToday = true;
            oneDay.positionOpen = true;
            btp.tradesCnt++;
            btp.boughtLast = true;
            btp.soldLast = !btp.boughtLast;
            btp.whatDay = oneDay.dayCnt;
            if (btp.firstTrade == true) {
                btp.firstTrade = false;
                btp.currentCostBasis = btp.costBasis = (oneDay.closePrice * btp.NUM_SHARES);
                btp.currentRunningPl = 0;
            } else {
                btp.currentCostBasis = (oneDay.closePrice * btp.NUM_SHARES);
            }
        }
        void backTestRunLongOpen(BackTestDataOneDay oneDay, TradeState ts){
            int openState;
            
            ts.setStates();
            openState = ts.getOpenState();
            
            switch (openState) {
                    case TradeState.STATE_DISABLED:
                        break;
                    case TradeState.STATE_PIVOT_ONLY:
                        //buy case..
                        if((oneDay.changeInDirection == true) 
                                && (oneDay.newTrend.equals(slopeDefs.TREND_UP))
                                && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot occured buy to open it..
                            backTestOpenLong(oneDay);
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_CLOSE_ONLY:
                        //buy case..
                        if((oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays)                                 
                                && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //consecutive closes passed to buy it..
                            backTestOpenLong(oneDay);
                        }
                        break;
                    case TradeState.STATE_PIVOT_AND_CLOSE:
                        //buy case..Pivot AND Close
                        if((oneDay.changeInDirection == true) 
                            && (oneDay.newTrend.equals(slopeDefs.TREND_UP))
                            && (oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays)
                            && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot AND Closing days passed buy it..
                            backTestOpenLong(oneDay);
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_OR_SET_ONLY_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                        //buy case..Pivot OR Close
                        if((((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_UP)))
                            || (oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays))
                                && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot OR Closing days passed buy it..
                            backTestOpenLong(oneDay);
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_TREND_ONLY_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_AND_CLOSE:
                        //buy case..Trend AND Close
                        if((oneDay.trendToday.equals(slopeDefs.TREND_UP)) 
                            && (oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays)
                            && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //Trend AND Closing days passed buy it..
                            backTestOpenLong(oneDay);  
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }                        
                        break;
                    case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                        //same as Pivot AND close (pivot has trend in it)..??
                        break;
                    case TradeState.STATE_TREND_OR_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                        //Trend && (close OR Pivot)                        
                        if((
                            ((oneDay.trendToday.equals(slopeDefs.TREND_UP)) 
                              && 
                             (oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays))
                              || ((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_UP)))
                           )
                            && ((btp.boughtLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {
                            backTestOpenLong(oneDay);  
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;      
                        }
                        break;
                    case TradeState.STATE_REOPEN_POSITION:
                         //reopen trigger occurred...
                            backTestOpenLong(oneDay);
                            //restart hysteresis..
                            ts.hysteresisReopenTrigger = false;
                            ts.hysteresisExpired = false;
                            btp.hysteresisDays = 0;
                            ts.clrReopenBit();
                        break;
                } /* switch */
            
        }
        void backTestCloseLong(BackTestDataOneDay oneDay){
            btp.whatDay = oneDay.dayCnt;
            btp.tradesCnt++;
            btp.boughtLast = false;
            oneDay.positionOpen = false;
            btp.soldLast = !btp.boughtLast;
            oneDay.lastPl = ((oneDay.closePrice * btp.NUM_SHARES) - btp.currentCostBasis);
            btp.currentRunningPl += oneDay.lastPl;
            oneDay.soldToday = true;
            oneDay.runningPl = btp.currentRunningPl;            
            oneDay.currentCostBasis = btp.currentCostBasis;
            if ((oneDay.closePrice == 0) || oneDay.date.equals("empty")){
                System.out.println("\n??");
            }
        }
        boolean btCheckGainLock(BackTestDataOneDay oneDay){
            double plPercent;
            
            if (bt1Year.positionOpen == true){
                plPercent = (((oneDay.closePrice * btp.NUM_SHARES) - btp.currentCostBasis) / btp.currentCostBasis) * 100;
                if ((plPercent > 5 ) || (plPercent < -2)){
                   return true;
                }
            }
            return false;
            
        }
        void backTestRunLongClose(BackTestDataOneDay oneDay, TradeState ts){
            int closeState;
            
            ts.setStates();
            closeState = ts.getCloseState();
            
            if ((false) && (oneDay.empty == false) && btCheckGainLock(oneDay) == true){
                //means close position, either lock gain or stop loss..
                backTestCloseLong(oneDay);
            };
            
            switch (closeState) {
                case TradeState.STATE_DISABLED:
                    break;
                case TradeState.STATE_PIVOT_ONLY:
                    //sell case..
                    if ((oneDay.changeInDirection == true)
                            && (oneDay.newTrend.equals(slopeDefs.TREND_DN))
                            && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot occured sell it...    
                        backTestCloseLong(oneDay);
                    }
                    break;
                case TradeState.STATE_CLOSE_ONLY:
                    //sell case..
                    if ((oneDay.btConsecutiveDaysBelow10dma >= ts.closeBasedOnClosingDays)                                                              
                                && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt))) 
                        {
                            //consecutive closes passed so sell it..
                            backTestCloseLong(oneDay);                           
                            btp.hysteresisDays = ts.reOpenBasedOnHysteresisDays;
                        } else if (btp.hysteresisDays > 0) {
                            if (--btp.hysteresisDays == 0) {
                                //days of waiting has expired, say so..
                                ts.hysteresisExpired = true;
                                //check if above 10dma for 1 day..
                                if ((oneDay.btConsecutiveDaysAbove10dma >= 1)
                                        && (btp.boughtLast == false)
                                        && (oneDay.trendToday.equals(slopeDefs.TREND_UP))
                                        ) {
                                    //it's high so trugger reopen
                                    ts.hysteresisReopenTrigger = true;
                                }else{
                                    ts.hysteresisExpired = false; 
                                    ts.hysteresisReopenTrigger = false;
                                }
                            }
                        }

                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    //sell case..
                    if ((oneDay.changeInDirection == true)
                         && (oneDay.newTrend.equals(slopeDefs.TREND_DN))
                         && (oneDay.btConsecutiveDaysBelow10dma >= ts.closeBasedOnClosingDays)
                         && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot AND CLOSE occured sell it...
                        backTestCloseLong(oneDay);                       
                    }

                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                    if((((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_DN)))
                            || (oneDay.btConsecutiveDaysBelow10dma >= ts.closeBasedOnClosingDays))
                                && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt)))
                    {
                        //pivot OR CLOSE occured sell it...    
                        backTestCloseLong(oneDay);
                    }

                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID:

                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:

                    break;
                case TradeState.STATE_TREND_AND_CLOSE:
                    //sell case..Trend AND Close
                    if ((oneDay.trendToday.equals(slopeDefs.TREND_DN))
                            && (oneDay.btConsecutiveDaysBelow10dma >= ts.closeBasedOnClosingDays)
                            && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot OR CLOSE occured sell it...    
                        backTestCloseLong(oneDay);
                    }

                    break;
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                    //same as Pivot AND close (pivot has trend in it)..??
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    // (Trend AND Close) OR Pivot
                    if ((((oneDay.trendToday.equals(slopeDefs.TREND_DN))
                            && (oneDay.btConsecutiveDaysAbove10dma >= ts.openBasedOnClosingDays))
                            || ((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_DN))))
                            && ((btp.boughtLast == true) && (btp.whatDay != oneDay.dayCnt))) 
                    {
                         //(Trend AND Close) OR Pivot occured, sell it..    
                        backTestCloseLong(oneDay);
                    }
                    break;
                case TradeState.STATE_REOPEN_POSITION:
                    //no such thing in sell of long position...
                    break;
            } /* switch */

        }
        void backTestOpenShort(BackTestDataOneDay oneDay){
            oneDay.soldToday = true;
            oneDay.positionOpen = true;
            btp.tradesCnt++;
            btp.soldLast = true;
            btp.boughtLast = !btp.soldLast;
            btp.whatDay = oneDay.dayCnt;
            if (btp.firstTrade == true) {
                btp.firstTrade = false;
                btp.currentCostBasis = btp.costBasis = (oneDay.closePrice * btp.NUM_SHARES);
                btp.currentRunningPl = 0;
            } else {
                btp.currentCostBasis = (oneDay.closePrice * btp.NUM_SHARES);
            }           
        }
        void backTestRunShortOpen(BackTestDataOneDay oneDay, TradeState ts){
            int openState;
            
            ts.setStates();
            openState = ts.getOpenState();
            
            switch (openState) {
                    case TradeState.STATE_DISABLED:
                        break;
                    case TradeState.STATE_PIVOT_ONLY:
                        // pivot open short position case..
                        if((oneDay.changeInDirection == true) 
                                && (oneDay.newTrend.equals(slopeDefs.TREND_DN))
                                && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot occured sell to open it..
                            backTestOpenShort(oneDay);                          
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_CLOSE_ONLY:
                        // open short position case..
                        if((oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays)                                 
                                && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //consecutive closes passed so sell to open ..
                            backTestOpenShort(oneDay);
                        }
                        break;
                    case TradeState.STATE_PIVOT_AND_CLOSE:
                        //open position case..Pivot AND Close
                        if((oneDay.changeInDirection == true) 
                            && (oneDay.newTrend.equals(slopeDefs.TREND_DN))
                            && (oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays)
                            && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot AND Closing days passed open it..
                            backTestOpenShort(oneDay);                           
                            //clear hysterisis time in case it was counting...we opened because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_OR_SET_ONLY_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:
                        break;
                    case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                        //open case..Pivot OR Close
                        if((((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_DN)))
                            || (oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays))
                                && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //pivot OR Closing days passed open it..
                            backTestOpenShort(oneDay);                           
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }
                        break;
                    case TradeState.STATE_TREND_ONLY_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_AND_CLOSE:
                        //open case..Pivot AND Close
                        if((oneDay.trendToday.equals(slopeDefs.TREND_DN)) 
                            && (oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays)
                            && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {                            
                            //Trend AND Closing days passed open it..
                            backTestOpenShort(oneDay); 
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;                                                         
                        }                        
                        break;
                    case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                        //same as Pivot AND close (pivot has trend in it)..??
                        break;
                    case TradeState.STATE_TREND_OR_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:
                        break;
                    case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                        //Trend && (close OR Pivot)                        
                        if((
                            ((oneDay.trendToday.equals(slopeDefs.TREND_DN)) 
                              && 
                             (oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays))
                              || ((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_DN)))
                           )
                            && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt)))
                        {
                            backTestOpenShort(oneDay);                          
                            //clear hysterisis time in case it was counting...we bought because pivot happened..                            
                            btp.hysteresisDays = 0;      
                        }
                        break;
                    case TradeState.STATE_REOPEN_POSITION:
                         //reopen trigger occurred...
                            backTestOpenShort(oneDay);
                            //restart hysteresis..
                            ts.hysteresisReopenTrigger = false;
                            ts.hysteresisExpired = false;
                            btp.hysteresisDays = 0;
                            ts.clrReopenBit();
                        break;
                } /* switch */
            
        }        
        void backTestCloseShort(BackTestDataOneDay oneDay){            
            btp.whatDay = oneDay.dayCnt;
            btp.tradesCnt++;
            btp.soldLast = false;
            oneDay.positionOpen = false;
            btp.boughtLast = !btp.soldLast;
            oneDay.lastPl = (btp.currentCostBasis - (oneDay.closePrice * btp.NUM_SHARES));
            btp.currentRunningPl += oneDay.lastPl;
            oneDay.boughtToday = true;
            oneDay.runningPl = btp.currentRunningPl;            
            oneDay.currentCostBasis = btp.currentCostBasis;           
        }
        void backTestRunShortClose(BackTestDataOneDay oneDay, TradeState ts){
            int closeState;
            
            ts.setStates();
            closeState = ts.getCloseState();
            
            switch (closeState) {
                    case TradeState.STATE_DISABLED:
                        break;
                case TradeState.STATE_PIVOT_ONLY:
                    //close case..
                    if ((oneDay.changeInDirection == true)
                            && (oneDay.newTrend.equals(slopeDefs.TREND_UP))
                            && ((btp.soldLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot occured close it...
                        backTestCloseShort(oneDay);                       
                    }
                    break;
                    case TradeState.STATE_CLOSE_ONLY:
                        //buy to close case..
                        if ((oneDay.btConsecutiveDaysAbove10dma >= ts.closeBasedOnClosingDays)                                                              
                                && ((btp.soldLast == true) && (btp.whatDay != oneDay.dayCnt))) 
                        {
                            //consecutive closes passed so close it..
                            backTestCloseShort(oneDay); 
                            btp.hysteresisDays = ts.reOpenBasedOnHysteresisDays;
                        } else if (btp.hysteresisDays > 0) {
                            if (--btp.hysteresisDays == 0) {
                                //days of waiting has expired, say so..
                                ts.hysteresisExpired = true;
                                //check if above 10dma for 1 day..
                                if ((oneDay.btConsecutiveDaysBelow10dma >= 1)
                                        && (btp.soldLast == false)
                                        && (oneDay.trendToday.equals(slopeDefs.TREND_DN))
                                        ) {
                                    //it's low so trugger reopen
                                    ts.hysteresisReopenTrigger = true;
                                }else{
                                    ts.hysteresisExpired = false; 
                                    ts.hysteresisReopenTrigger = false;
                                }
                            }
                        }

                    break;
                case TradeState.STATE_PIVOT_AND_CLOSE:
                    //sell to close case..
                    if ((oneDay.changeInDirection == true)
                         && (oneDay.newTrend.equals(slopeDefs.TREND_UP))
                         && (oneDay.btConsecutiveDaysAbove10dma >= ts.closeBasedOnClosingDays)
                         && ((btp.soldLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot AND CLOSE occured close it...  
                        backTestCloseShort(oneDay);                                               
                    }

                    break;
                case TradeState.STATE_OR_SET_ONLY_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_PIVOT_SET_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_CLOSE_SET_NOTVALID:

                    break;
                case TradeState.STATE_OR_SET_PIVOT_OR_CLOSE:
                    if((((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_UP)))
                            || (oneDay.btConsecutiveDaysAbove10dma >= ts.closeBasedOnClosingDays))
                                && ((btp.soldLast == true) && (btp.whatDay != oneDay.dayCnt)))
                    {
                        //pivot OR CLOSE occured close it...  
                        backTestCloseShort(oneDay);                        
                    }

                    break;
                case TradeState.STATE_TREND_ONLY_NOTVALID:

                    break;
                case TradeState.STATE_TREND_AND_PIVOT_NOTVALID:

                    break;
                case TradeState.STATE_TREND_AND_CLOSE:
                    //sell case..Trend AND Close
                    if ((oneDay.trendToday.equals(slopeDefs.TREND_UP))
                            && (oneDay.btConsecutiveDaysAbove10dma >= ts.closeBasedOnClosingDays)
                            && ((btp.soldLast == true) && (btp.whatDay != oneDay.dayCnt))) {
                        //pivot OR CLOSE occured close it...    
                        backTestCloseShort(oneDay);
                    }

                    break;
                case TradeState.STATE_TREND_AND_CLOSE_AND_PIVOT:
                    //same as Pivot AND close (pivot has trend in it)..??
                    break;
                case TradeState.STATE_TREND_OR_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_OR_SET_PIVOT_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_OR_SET_CLOSE_SET_NOTVALID:

                    break;
                case TradeState.STATE_TREND_CLOSE_OR_PIVOT:
                    // (Trend AND Close) OR Pivot
                    if ((((oneDay.trendToday.equals(slopeDefs.TREND_DN))
                            && (oneDay.btConsecutiveDaysBelow10dma >= ts.openBasedOnClosingDays))
                            || ((oneDay.changeInDirection == true) && (oneDay.newTrend.equals(slopeDefs.TREND_DN))))
                            && ((btp.soldLast == false) && (btp.whatDay != oneDay.dayCnt))) 
                    {
                         //(Trend AND Close) OR Pivot occured, close it..    
                        backTestCloseShort(oneDay);
                    }
                    break;
                case TradeState.STATE_REOPEN_POSITION:
                    //no such thing in sell of long position...
                    break;
            } /* switch */

        }
        
        boolean wrInitialized = false;
        PrintStream console;
        File file ;
        FileOutputStream fos ;
        PrintStream ps ;
        
        public void prepWrToFile() throws FileNotFoundException {
            //temp file stuff..  
            if (wrInitialized == false) {
                console = System.out;
                file = new File("btResults.txt");
                fos = new FileOutputStream(file);
                ps = new PrintStream(fos);
                wrInitialized = true;
            }
        }
        
        void testTradeActivity(TradeTicket tt){
            //placeOrder(actTicker, buyIt, hotTicket.numOfSharesToTrade);
            TradeRequestData trd = new TradeRequestData();
            trd.setTicker(tt.ticker);
            trd.setOperation(TradeRequestData.TradeOptions.oOpBuy);
            //trd.setOperation(TradeRequestData.TradeOptions.oOpSell);
            trd.setOriginalSharesToTrade(tt.numOfSharesToTrade /*/ 6*/);
            actTradeRequestList.addOne(trd);
        }
                
        void backTestPosition(TradeState ts, ibApi.historicalData hd) throws FileNotFoundException{
            /*
             BackTestPosition. Goes back one year and records trigger points:
             trendCHanges, a boolean. NewTrend, a string. consecutive counts above/below
             10dma. This information is then used to generate buy sell trigger points 
             in history to perform back testing on a ticker. hd already has historical
             data of an active ticker.
            
            */
            int actDay;
            int numOfDays;
            int pivotCnt = 0;
            
            slopeAnalysis outerSa = new slopeAnalysis();
            BackTestDataOneDay oneDay = outerSa.new BackTestDataOneDay();
            int openState;
            int closeState;
            boolean boutLast = false;
            boolean soldLast = false;
            boolean firstTrade = true;
            final int NUM_SHARES = 100;
            double costBasis = 0.0;
            double currentRunningPl = 0.0;
            boolean closePosition = false;
            ts.setStates();
            openState = ts.getOpenState();
            closeState = ts.getCloseState();
            hd.recordBackTestData(slopeDefs.MA_10DAY);
            bt1Year = hd.getBackTestData();
            numOfDays = bt1Year.getSz() - 1;
            System.out.println("\nWorking on: " + bt1Year.ticker);
            
            btp = new Btparms();
            //cycle through a year for each ticker..
            for (actDay = numOfDays; actDay > 0; actDay--) {
                oneDay = bt1Year.day[actDay];
                //a number used to differentiate days.
                oneDay.dayCnt = actDay;  
                if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                    backTestRunLongOpen(oneDay, ts);
                    backTestRunLongClose(oneDay, ts);
                }else if (longOrShort.equals(slopeDefs.oBiasShortStr)){
                    backTestRunShortOpen(oneDay, ts);
                    backTestRunShortClose(oneDay, ts);
                }
                
                //record everytime a purchase is made
                //so we can figure out the openPos value later..
                if (((longOrShort.equals(slopeDefs.oBiasLongStr)) && (oneDay.boughtToday == true)) ||
                   ((longOrShort.equals(slopeDefs.oBiasShortStr)) && (oneDay.soldToday == true))){
                    bt1Year.positionOpen = oneDay.positionOpen;  
                    bt1Year.openPosCostBasis = (oneDay.closePrice * NUM_SHARES);
                }
                
                closePosition = ((longOrShort.equals(slopeDefs.oBiasLongStr) && oneDay.soldToday == true)
                        || (longOrShort.equals(slopeDefs.oBiasShortStr) && oneDay.boughtToday == true));
                if (closePosition == true) {
                    bt1Year.positionOpen = false;
                    bt1Year.openPosCostBasis = 0.0;
                } else {
                    int a = 0;
                    a++;
                }
                //need close price of last not empty day...
                if(oneDay.empty == false){
                    bt1Year.openPosClosePrice = oneDay.closePrice;
                }
            }/*for*/
            bt1Year.numberOfTrades =  btp.tradesCnt;
            bt1Year.runningPl = btp.currentRunningPl;
            
            if (longOrShort.equals(slopeDefs.oBiasLongStr)){
                bt1Year.openPosValue = ((bt1Year.positionOpen == true) ? ((bt1Year.openPosClosePrice * NUM_SHARES) - bt1Year.openPosCostBasis) : 0);
            }else if (longOrShort.equals(slopeDefs.oBiasShortStr)){
                bt1Year.openPosValue = ((bt1Year.positionOpen == true) ? (bt1Year.openPosCostBasis - (bt1Year.openPosClosePrice * NUM_SHARES)) : 0);
            }           
            
            btTotals.costBasis += btp.costBasis;
            btTotals.currentRunningPl += bt1Year.runningPl;
            btTotals.numTickers++;
            btTotals.runningOpenPosPl += bt1Year.openPosValue;           
         
            if ((bt1Year.runningPl + bt1Year.openPosValue) > 0){
                btTotals.numOfWinners++;
                btTotals.runningGain += (bt1Year.runningPl + bt1Year.openPosValue);
            }else{
                btTotals.numOfLosers++;
                btTotals.runningLoss += (bt1Year.runningPl + bt1Year.openPosValue);
            }
            
            //print to consile... 
            System.setOut(console);
            System.out.println("\nTicker: " + bt1Year.ticker);
            System.out.println("\n 10DayMa transitions: " + hd.transitionsRelativeTo10DayMaCount + 
                                " %: " + myUtils.truncate(hd.transitionsRelativeTo10DayMaPercent, 2));
            for (actDay = numOfDays; actDay > 0; actDay--) {
                oneDay = bt1Year.day[actDay];
                
                if (longOrShort.equals(slopeDefs.oBiasLongStr)) {
                    if (oneDay.boughtToday == true) {
                        System.out.println("\n   BOT on " + oneDay.date + " price: " + myUtils.truncate(oneDay.closePrice, 2));
                    }
                    if (oneDay.soldToday == true) {
                        System.out.println("\n   SLD on " + oneDay.date + " price: "
                                + myUtils.truncate(oneDay.closePrice, 2) + " pl: "
                                + myUtils.truncate(oneDay.lastPl, 2) + " runpl: " + myUtils.truncate(oneDay.runningPl, 2)
                        );
                    }
                }else if (longOrShort.equals(slopeDefs.oBiasShortStr)){
                    if (oneDay.soldToday == true) {
                        System.out.println("\n   SLD on " + oneDay.date + " price: " + myUtils.truncate(oneDay.closePrice, 2));
                    }
                    if (oneDay.boughtToday == true) {
                        System.out.println("\n   BOT on " + oneDay.date + " price: "
                                + myUtils.truncate(oneDay.closePrice, 2) + " pl: "
                                + myUtils.truncate(oneDay.lastPl, 2) + " runpl: " + myUtils.truncate(oneDay.runningPl, 2)
                        );
                    }
                }
                

            }
            if (bt1Year.positionOpen == true){
                double totPercent = myUtils.truncate((((bt1Year.runningPl + bt1Year.openPosValue) / btp.costBasis) * 100.0), 2);
                System.out.println("\n Trades: " + bt1Year.numberOfTrades + 
                    " runningPl: " + myUtils.truncate(bt1Year.runningPl, 2) + 
                    " percent: " + myUtils.truncate(((bt1Year.runningPl / btp.costBasis) * 100.0), 2) + "%" 
                        + " OpenPosValue: " + myUtils.truncate(bt1Year.openPosValue, 2) + " TotalPercent: " + totPercent);
            }else if (bt1Year.numberOfTrades > 0){            
                    System.out.println("\n Trades: " + bt1Year.numberOfTrades + 
                    " runningPl: " + myUtils.truncate(bt1Year.runningPl, 2) + 
                    " percent: " + myUtils.truncate(((bt1Year.runningPl / btp.costBasis) * 100.0), 2) + "%");
            }
            //print to file..            
            System.setOut(ps);
            System.out.println("\nTicker: " + bt1Year.ticker);
            for (actDay = numOfDays; actDay > 0; actDay--) {
                oneDay = bt1Year.day[actDay];                 
                if (oneDay.boughtToday == true){
                    System.out.println(  "\n   BOT on " + oneDay.date + " price: " + myUtils.truncate(oneDay.closePrice, 2));
                }
                if (oneDay.soldToday == true){
                    System.out.println(  "\n   SLD on " + oneDay.date + " price: " + 
                            myUtils.truncate(oneDay.closePrice, 2) + " pl: " + 
                            myUtils.truncate(oneDay.lastPl, 2) + " runpl: " + myUtils.truncate(oneDay.runningPl, 2)
                    );
                }
            }        
            if (bt1Year.positionOpen == true){
                double totPercent = myUtils.truncate((((bt1Year.runningPl + bt1Year.openPosValue) / btp.costBasis) * 100.0), 2);
                System.out.println("\n Trades: " + bt1Year.numberOfTrades + 
                    " runningPl: " + myUtils.truncate(bt1Year.runningPl, 2) + 
                    " percent: " + myUtils.truncate(((bt1Year.runningPl / btp.costBasis) * 100.0), 2) + "%" 
                        + " OpenPosValue: " + myUtils.truncate(bt1Year.openPosValue, 2) + " TotalPercent: " + totPercent);
            }else if (bt1Year.numberOfTrades > 0){
                    System.out.println("\n Trades: " + bt1Year.numberOfTrades + 
                    " runningPl: " + myUtils.truncate(bt1Year.runningPl, 2) + 
                    " percent: " + myUtils.truncate(((bt1Year.runningPl / btp.costBasis) * 100.0), 2) + "%");
            }
            System.setOut(console);
        }
        void backTestDisplayTotals(){
            System.setOut(console);           
            System.out.println("\n\n\n Total Cost Basis: " + btTotals.costBasis);
            System.out.println("\n Total Gain: " + btTotals.runningGain);
            System.out.println("\n Total Loss: " + myUtils.truncate(btTotals.runningLoss, 2));            
            System.out.println("\n Total Winners: " + btTotals.numOfWinners); 
            System.out.println("\n Total Losers: " + btTotals.numOfLosers); 
            System.out.println("\n Total Pl: " + btTotals.currentRunningPl);
            System.out.println("\n Total OpenPosValue: " + btTotals.runningOpenPosPl);
            System.setOut(ps);
            System.out.println("\n\n\n Total Cost Basis: " + btTotals.costBasis);
            System.out.println("\n Total Gain: " + btTotals.runningGain);
            System.out.println("\n Total Loss: " + myUtils.truncate(btTotals.runningLoss, 2));           
            System.out.println("\n Total Winners: " + btTotals.numOfWinners); 
            System.out.println("\n Total Losers: " + btTotals.numOfLosers); 
            System.out.println("\n Total Pl: " + btTotals.currentRunningPl); 
            System.out.println("\n Total OpenPosValue: " + btTotals.runningOpenPosPl);
            System.setOut(console);
        }
        @Override
        public void run() {
            String endDate10;
            String endDate50;
            String endDate100;
            String endDate150;
            String endDate200;
            
            final int ELEVEN_SECS = 11000;
            final int TWO_SECS = 2000;
            String actTicker = null;            
            int idx = 0;
            boolean traderRunning = false;
            String progressStr = null;
            boolean buyIt = false;
            boolean sellIt = false;
            boolean buyToOpenLong = false;
            boolean sellToCloseLong = false;
            boolean buyToCloseShort = false;
            boolean sellToOpenShort = false;
            String tradeOperation = slopeDefs.oNoTradeYet;
            int iteration = 0;            
            TradeTicket hotTicket = new TradeTicket(false);
            boolean currentChangeInDirection = false;
            boolean streamsOpened = false;
            int currentAvailFunds = 0;
            Totals keepTotals = new Totals();
            int sharesToTradeNow;
            int sharesToTrade1YearAgo;
            double openPosValuePercent;
            boolean didWeTradeToday = false;
            TradeState actTradeState;
            TradeState tradeCriteria;
            MaWindowSz maWindowSizes = null;
            boolean hdError = false;
            int fcnt = 0;
            int rcnt = 0;
            String posStatisticsStr = "";
            if (thisIsANewDay == true){
                updateNewDateData();
            }
            updateQuotes(tradeTickets);
            initTraderStatusTable(tradeTickets);
            keepTotals.initTotalsTable(tradeTickets);
                                    
            while (startTrader == true) {
               
                traderRunning = startTrader;
                iteration++;               
                
                try {                   
                    System.out.println("\ntrader running..");
                    backUpTicketInfoToFile(tradeTickets);                  
                    //need to create allSlopes again for next round..
                    allSlopes = saOuter.new SlopesFound();
                    allSlopes.setSize(tradeTickets.numOfTickets);
                    //open all streams up front. gives them more time to arrive..do once only.
                    streamsOpened = actPositions.quoteStreamsOpened;
                    if (streamsOpened == false){
                        openStreams(tradeTickets);
                        streamsOpened = true;
                    }
                    //keep per run..so clear first.
                    actPositions.histDataProblemCnt = 0;
                    actPositions.todaysTrades = 0;
                    actPositions.tradeErrorsCnt = 0;
                    if(accInfoFrameForm != null){
                        accInfoFrameForm.setActTradeTickets(tradeTickets);
                    }                    
                    for(idx = 0; (traderRunning == true) && ((idx < tradeTickets.numOfTickets) || (userSelectedBackTestOneTicker == true)); idx++){
                        if ((userSelectedBackTestOneTicker == true) && (userSelectedBackTesting == true)){
                            actTicker = userSelectedTicker;
                            if (idx > 0){
                                //run once.
                                idx = tradeTickets.numOfTickets;
                                userSelectedBackTestOneTicker = false;
                                continue;
                            }else{
                                
                            }                           
                        }else{
                            actTicker = tradeTickets.tickets[idx].ticker;
                        }
                        System.out.println("working on : " + actTicker + " (" + (idx+1) + ")");
                        progressStr = "working on : " + actTicker + " (" + idx + ")" + " iteration: " + iteration;
                        traderStatusTable.setRowSelectionInterval(idx, idx);
                        if (idx > 0) {
                            traderStatusTable.removeRowSelectionInterval(idx - 1, idx - 1);
                        }
                        progressLable.setText(progressStr);
                        hd = null;
                        hd = actIbApi.setActHistoricalData(hd);
                        hd.nextTid(idx);
                        actHistory = hd.actWally;
                        actTradeState = new TradeState();
                        //if (hd.getHistoricalData(actTicker, "", "") == true) {
                        hd.setSelectedYear(userSelectedBackTestYear);
                        hd.setSelectedDuration("3 Y");
                        hd.setSelectedBarSize("1 day");
                        if (hd.getHistoricalData(actTicker) == true) {
                            hdError = false;
                            System.out.println("..OK.");
                            progressStr = progressStr + "..OK.";
                            progressLable.setText(progressStr);                                                     
                            hd.calcSimpleMovingAve(MA_50DAY);
                            endDate50 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_100DAY);
                            endDate100 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_200DAY);
                            endDate200 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_10DAY);
                            endDate10 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_150DAY);
                            endDate150 = hd.endingDate;
                            hd.calcPercentages();
                            hd.calcAveVolume(AVE_90DAY);
                            hd.calcSimpleMovingAve(MA_20DAY);
                            hd.calcSimpleMovingAve(MA_30DAY);
                            //took this out 3/11/16 since changing size of historical data could be less than year..
                            //this caused the following to fail..fix later..
                            //hd.calcBollingerBands();
                            System.out.println("\ncalling findSlopes with: " + userSelectedMa + " userMa.");
                            actHistory = hd.findSlopes1(DIRECTION_CHANGE, userSelectedMa);
                            currentAvailFunds = calculateAvailFunds(tradeTickets);
                            sharesToTradeNow = calculateSharesToTrade(tradeTickets.tickets[idx], currentAvailFunds);
                            //if can't get quote from ib to calclulate sharesToTrade, 
                            //then use historical data..last close day price
                            if(sharesToTradeNow == 0){
                                sharesToTradeNow = calculateSharesToTrade(tradeTickets.tickets[idx], currentAvailFunds, hd.getLastClosedDayPrice());
                            }
                            hd.analizeDataSetSize(tradeTickets.numOfTickets);                            
                            /*get shares to trade for back testing..
                              based on share price in history (when 10ma begins, 
                              not a year ago!!)..
                            */
                            sharesToTrade1YearAgo = calculateSharesToTradeBackTest(tradeTickets.tickets[idx], currentAvailFunds, hd.getInitialStockPrice1YearAgo());                          
                            hd.setSharesPerTrade(sharesToTrade1YearAgo, segTradingScale);
                            hd.setOverShootPercent(actTraderConfigParams.getOverShootPercent()); 
                            hd.setOverShootLinearMode(linearMode);
                            hd.setTraderConfigParams(actTraderConfigParams, currentBias);
                            hd.setRecommendOverShootPercent(userSelectedRecommendedOverShootPercent);
                            hd.analizeDataThrowaway(longOrShort, 
                                                    segTradingScale, 
                                                    sharesToTrade1YearAgo,
                                                    sharesToTradeNow);
                            actHistory.setLongShort(longOrShort);
                            tradeTickets.tickets[idx].curTrend = actHistory.currentTrend;
                            /*moved this up here it should work...*/
                            slopeStruct = saOuter.new SlopeStructure(); 
                            slopeStruct.addAllFound(actHistory, actTicker);
                            allSlopes.addOne(slopeStruct); 
                            tradeTickets.tickets[idx].avePriceMove = slopeStruct.avePriceMove;
                            /*
                            needed to add didWeTradeToday because todays close price is changing 
                            through out today since close did not happen yet. this could easliy trigger multiple
                            trade triggers which we do not want. So if we trade today, don't trade again on this 
                            position until tomorrow.
                            */
                            currentChangeInDirection = ((actHistory.currentChangeInDirection) && 
                                                (tradeTickets.tickets[idx].didWeTradeToday.equals(slopeDefs.NO)));
                           
                            if (actPositions.version > 1){
                                //newer version
                                if (currentAvailFunds > 0){
                                    System.out.println("\n shares allowed to trade: " + sharesToTradeNow);                           
                                    tradeTickets.tickets[idx].numOfSharesToTrade = sharesToTradeNow;                                   
                                }else{
                                    sharesToTradeNow = 0;
                                    tradeTickets.tickets[idx].numOfSharesToTrade = 0;
                                    System.out.println("\n Avail FUNDS == 0!!");
                                    commentsTextArea.append("\nAvail FUNDS == 0 with ticker: " + actTicker);
                                }
                            }else{
                                //older version just do 100 no matter 
                                tradeTickets.tickets[idx].numOfSharesToTrade = 100;
                            }
                            
                            System.out.println("\n avail funds: " + currentAvailFunds);
                            availFundsLabel.setText(Integer.toString(currentAvailFunds));
                            //see if we traded today (buyOrSell) this is stored on file too..
                            didWeTradeToday = (tradeTickets.tickets[idx].didWeTradeToday.equals(slopeDefs.YES));
                            //ugly...
                            actTradeState.actTicker = actTicker;
                            actTradeState.currentChangeInDirection = currentChangeInDirection;
                            actTradeState.currentTrend = actHistory.currentTrend;
                            actTradeState.didWeTradeToday = didWeTradeToday;
                            /*
                             check if position is in hysteresis mode, where it's idle
                             for hysteresisDays. When this counter decrements from 1->0,
                             time has expired, and we need to check previous days close.
                             check for this transition here.
                            */
                            if (tradeTickets.tickets[idx].hysteresisDays > 0) {
                                if (--tradeTickets.tickets[idx].hysteresisDays == 0) {
                                    /* we went from 1->0, time to 
                                     check if closing price is confirming
                                     trend is still in tact. If so, 
                                     re-open position.
                                     */
                                    actTradeState.hysteresisExpired = true;
                                } else {
                                    //still time, just decrement..
                                }
                            }
                            //set up trade condition based on traderConfig...
                            /*
                             check if we are in linearMode and call appropriate methode.
                             if linearMode is true, we are processing segmented long and short trades.
                             if not, we only do segmented long trades. Got segmented long trading to work
                             first so don't want to mess with it. keep segmented long and short separate..
                            */
                            if(linearMode == true){
                                actTradeState.setUpTradeConditionsLinear(actTraderConfigParams, hd, tradeTickets.tickets[idx]);
                            }else{
                                actTradeState.setUpTradeConditions(actTraderConfigParams, hd);
                            }
                            if (userSelectedBackTesting == true){
                                prepWrToFile();
                                //wfs remove after testing is done!!!!
                                //testTradeActivity(tradeTickets.tickets[idx]);
                                backTestPosition(actTradeState, hd);                               
                                SlopeTrader.sleep(ELEVEN_SECS);
                                traderRunning = startTrader;
                                if (userSelectedStreamToFile == true) {
                                    posStatisticsStr = dispStatisticsToStr(actTicker);
                                    perfFileWr.write(posStatisticsStr);
                                }
                                continue;
                            }
                            if (longOrShort.equals(slopeDefs.oBiasLongStr)){                              
                                tradeOperation = seeIfLongBuyTrigger(actTradeState, idx);
                                if (tradeOperation.equals(slopeDefs.oNoTradeYet)){
                                    tradeOperation = seeIfLongSellTrigger(actTradeState, idx);   
                                }else{
                                    
                                }                                        
                            }else if (longOrShort.equals(slopeDefs.oBiasShortStr)){
                                buyToCloseShort = seeIfShortBuyTrigger(actTradeState, idx);
                                sellToOpenShort = seeIfShortSellTrigger(actTradeState, idx);
                            }
                            
                            buyIt = ((tradeOperation.equals(slopeDefs.oBuyToOpenLong)) || tradeOperation.equals(slopeDefs.oBuyToCloseShort));
                            sellIt = ((tradeOperation.equals(slopeDefs.oSellToCloseLong)) || tradeOperation.equals(slopeDefs.oSellToOpenShort));
                            if ((userSelectedLiquidateAll == false) && ((buyIt == true) || (sellIt == true))){
                                /*
                                6.20.2016
                                should we block trade based on lack of funds? Only block if opening position cuz no cash to use..
                                so check both buyToOpenLong OR sellToOpenShort..otherwise its ok to trade to close a 
                                position, giving us more cash....
                                */
                                if((sharesToTradeNow == 0) && (tradeOperation.equals(slopeDefs.oBuyToOpenLong) || tradeOperation.equals(slopeDefs.oSellToOpenShort))){
                                    commentsTextArea.append("\nOpening of " + actTradeState.actTicker + " position blocked for lack of funds.");
                                    continue;
                                }
                                //temporary
                                if(buyIt == true){
                                    System.out.println("\nBUY!! "+ actTicker + " " + actTradeState.tradeDescription);                                   
                                }else if ((sellToCloseLong == true) || (sellToOpenShort)){
                                    System.out.println("\nSELL!! "+ actTicker + " " + actTradeState.tradeDescription);
                                }
                                if(((buyIt == true) || (sellIt == true)) && (userSelectedShowTradeDescription == true)){
                                    commentsTextArea.append("\n" + actTradeState.tradeDescriptionSuccess + " for Ticker: " + actTicker);
                                }
                                /* 
                                tag that we traded today, so we don't trigger another trade for this
                                position again today.
                                */
                                
                                tradeTickets.tickets[idx].didWeTradeToday = slopeDefs.YES;
                                //for easier processing put it in hotTicket..
                                hotTicket = tradeTickets.tickets[idx];
                                if(buyIt == true){
                                    System.out.println("\nBUY!!");
                                    //hotTicket.curTrend = actHistory.currentTrend + "*";
                                    hotTicket.curTrend = actHistory.currentTrend;
                                    hotTicket.buyOrderPlaced = true;
                                    hotTicket.sellOrderPlaced = false;
                                    //last pivot has how many days we were out of the market..
                                    hotTicket.daysOut =+ actHistory.getCurrentDnDays();
                                    if (actTradeState.segmentedTrading == true){
                                        hotTicket.numOfSharesToTrade =  actTradeState.sharesToTrade;
                                        placeOrderNew(actTicker, buyIt, hotTicket.numOfSharesToTrade, tradeOperation);
                                    }else{
                                        placeOrderNew(actTicker, buyIt, 
                                                      longOrShort.equals(slopeDefs.oBiasLongStr)?hotTicket.numOfSharesToTrade:hotTicket.sharesAtHand,
                                                      tradeOperation
                                        );
                                    }
                                    
                                }
                                if (sellIt == true){
                                    System.out.println("\nSELL!!"); 
                                    //hotTicket.curTrend = actHistory.currentTrend + "*";
                                    hotTicket.curTrend = actHistory.currentTrend;
                                    hotTicket.sellOrderPlaced = true;
                                    hotTicket.buyOrderPlaced = false;
                                    //last pivot has how many days we were in the market..
                                    hotTicket.daysIn =+ actHistory.getCurrentUpDays();
                                    if (actTradeState.segmentedTrading == true){
                                        hotTicket.numOfSharesToTrade =  actTradeState.sharesToTrade;
                                        placeOrderNew(actTicker, buyIt, hotTicket.numOfSharesToTrade, tradeOperation);
                                    }else{
                                        placeOrderNew(actTicker, buyIt, 
                                                      longOrShort.equals(slopeDefs.oBiasLongStr)?hotTicket.sharesAtHand:hotTicket.numOfSharesToTrade,
                                                      tradeOperation
                                        );
                                    }                                   
                                }                          
                            }else{
                                
                            }
                        }else{
                            System.out.println("..Bad."+ hd.errorMsg);
                            progressStr = progressStr + "..Bad." + hd.errorMsg;
                            progressLable.setText(progressStr);
                            commentsTextArea.append("\n" + progressStr);
                            tradeTickets.tickets[idx].problemProcessing = true;
                            actPositions.histDataProblemCnt++;
                            slopeStruct = saOuter.new SlopeStructure();
                            //null because it failed...
                            slopeStruct = null;
                            allSlopes.addOne(slopeStruct);
                            userSelectedTicker = null;
                            hdError = true;
                        }                        
                        //updateTraderStatusTbl(idx, tradeTickets.tickets[idx], false);
                        moneySavedLabel.setText(Double.toString(myUtils.roundMe(actTradeRequestList.getTotalMoneySaved(), 2)));
                        tradeTypeLabel.setText(actTradeActivity.getTradeMode().equals(TradeRequestData.TradeModes.oMarket) ?
                                                "MRKT" : "AlgoLimit"
                        );
                        updateTradeActivity(tradeTickets, actTradeRequestList);
                        if (iteration == 1) {
                            keepTotals.updateTotalsTable(tradeTickets);
                            //keepTotals.updateTotalsTextArea(tradeTickets, idx, false /*init*/);
                            //keepTotals.displayIt();
                        } else {
                            keepTotals.updateTotalsTextArea(tradeTickets, idx, true /*init*/);
                        }
                        SlopeTrader.sleep(ELEVEN_SECS);
                        
                        traderRunning = startTrader;
                        if (userSelectedPause == true){
                            progressStr = "Paused. Hit Pause again to continue...";
                            progressLable.setText(progressStr);
                            while(userSelectedPause == true){
                                SlopeTrader.sleep(100);
                            }
                            progressStr = "Continuing..";
                            progressLable.setText(progressStr);
                        }
                        updateQuotes(tradeTickets);
                        System.out.println("\nbackUpTicketInfoFile..");
                        backUpTicketInfoToFile(tradeTickets);
                        checkSellCriteriaTrade(tradeTickets);
                        checkBuyCriteriaTrade(tradeTickets);
                        if (userSelectedStreamToFile == true) {
                            posStatisticsStr = dispStatisticsToStr(actTicker);
                            perfFileWr.write(posStatisticsStr);
                        }                        
                    }/*for*/
                    int waited = 0;
                    System.out.println("\nwait for all to fill...");
                    if(actTradeRequestList.getReqCnt() == actTradeRequestList.getFillCnt()){
                        System.out.println("\n ReqCnt == FillCnt! ..Done. " + actTradeRequestList.getFillCnt());
                        actTradeRequestList.setAreWeAllDone(true);
                    } else {                        
                        while ((rcnt = actTradeRequestList.getReqCnt()) != (fcnt = actTradeRequestList.getFillCnt())) {                            
                            System.out.println("\n All orders not complete, waiting on: "
                                    + (rcnt - fcnt) + " orders to fill.");
                            commentsTextArea.append("\n All orders not complete, waiting on: "
                                    + (rcnt - fcnt) + " orders to fill.");
                            while (!actTradeRequestList.areAllFilled()) {
                                SlopeTrader.sleep(TWO_SECS);
                                waited++;
                            }
                            System.out.println("\n waited : " + (waited * 2) + " seconds for all to fill.");
                            commentsTextArea.append("\n All orders filled, waited : " + (waited * 2) + " seconds to fill.");
                            updateTradeActivity(tradeTickets, actTradeRequestList);
                        } 
                        if (iteration == 1) {
                            keepTotals.updateTotalsTable(tradeTickets);
                            //keepTotals.updateTotalsTextArea(tradeTickets, idx, false /*init*/);
                            //keepTotals.displayIt();
                        } else {
                            keepTotals.updateTotalsTextArea(tradeTickets, idx, true /*init*/);
                        }
                        System.out.println("\n All orders now filled.");
                        actTradeRequestList.setAreWeAllDone(true);
                    }
                    
                    System.out.println("\n..done.");
                    moneySavedLabel.setText(Double.toString(myUtils.roundMe(actTradeRequestList.getTotalMoneySaved(), 2)));
                    String perfDataPortfolio = "";
                    perfDataPortfolio = hd.analizeDataPositions(actPositions.getPortfolioName());
                    
                    if (userSelectedStreamToFile == true) {
                        perfFileWr.write(perfDataPortfolio);
                    }
                    if (userSelectedBackTesting == true){
                        if ((btTotals != null) && (userSelectedBackTestOneTicker == false) && (hdError == false)){
                            backTestDisplayTotals();                           
                        }
                    }
                    if(traderRunning == true){
                        keepTotals.displayIt();
                        keepTotals.resetTotals();
                    }
                    //just run once..
                    startTrader = false;
                    traderRunning = startTrader;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(slopeTraderFrameForm.class.getName()).log(Level.SEVERE, null, ex);
                }
            } /* while */
            System.out.println("\nbackUpTicketInfoFile..");
            backUpTicketInfoToFile(tradeTickets);
            System.out.println("\ntrader Stopped..");
            progressLable.setText("Trader Stopped..");
        } /* run */

    } /* dispSlopeJob */


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
            java.util.logging.Logger.getLogger(slopeTraderFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(slopeTraderFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(slopeTraderFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(slopeTraderFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        final Positions dummy = new Positions();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new slopeTraderFrameForm(dummy).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem TradeRulesMenuItem;
    private javax.swing.JMenuItem TradingActivityMenuItem;
    private javax.swing.JMenuItem accountInfoMenuItem;
    private javax.swing.JLabel availFundsLabel;
    private javax.swing.JButton backTestOneTickerButton;
    private javax.swing.JMenuItem backTestYearMenuItem;
    private javax.swing.JCheckBoxMenuItem backtestingMenuItemCheckBox;
    private javax.swing.JButton buyCriteriaButton;
    private javax.swing.JMenu chartMenu;
    private javax.swing.JMenuItem chartPortfolioMenuItem;
    private javax.swing.JMenuItem chartPortfolioScaleUsageMenuItem;
    private javax.swing.JMenuItem chartPositionMenuItem;
    private javax.swing.JMenuItem chartPositionScaleUsageMenuItem;
    private javax.swing.JMenuItem chartVolocityMenuItem;
    private javax.swing.JMenu clickChartOnRadioButtonItemMenu;
    private javax.swing.JRadioButtonMenuItem clickChartOnRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem clickPivotStatisticsOnRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem clickStatisticsOnRadioButtonMenuItem;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextArea commentsTextArea;
    private javax.swing.JMenuItem displayCriteriaMenuItem;
    private javax.swing.JMenu displayMenu;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JCheckBoxMenuItem linearModeCheckBoxMenuItem;
    private javax.swing.JLabel liqValueLabel;
    private javax.swing.JCheckBoxMenuItem marketOrdersCheckBoxMenuItem;
    private javax.swing.JLabel moneySavedLabel;
    private javax.swing.JMenuItem openCloseCriteriaMenuItem;
    private javax.swing.JMenuItem openPortfolioPositionValues;
    private javax.swing.JMenuItem openPositionValuesMenuItem;
    private javax.swing.JButton pauseButton;
    private javax.swing.JLabel portfolioNameLabel;
    private javax.swing.JMenuItem portfolioOsUsageMenuItem;
    private javax.swing.JMenuItem portfolioPerformanceMenuItem;
    private javax.swing.JLabel positionBiasLable;
    private javax.swing.JMenuItem positionPerformanceMenuItem;
    private javax.swing.JLabel progressLable;
    private javax.swing.JCheckBoxMenuItem recommendOverShootCheckBoxMenuItem;
    private javax.swing.JButton sellCriteriaButton;
    private javax.swing.JMenuItem setMovingAverageMenuItem;
    private javax.swing.JRadioButton showTradeDescriptioinsRadioButton;
    private javax.swing.JButton startStopButton;
    private javax.swing.JCheckBoxMenuItem streamToFileCheckBoxMenuItem;
    private javax.swing.JTextField tickerTextField;
    private javax.swing.JLabel tickersLabel;
    private javax.swing.JTable totalsTable;
    private javax.swing.JComboBox tradeListComboBox;
    private javax.swing.JLabel tradeTypeLabel;
    private javax.swing.JMenuBar traderMenuBar;
    private javax.swing.JTable traderStatusTable;
    private javax.swing.JLabel tradingModeLable;
    // End of variables declaration//GEN-END:variables
}
