/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import positions.ManagedAccounts;
import positions.TradeActivity;
import positions.TradeRequestData;
import positions.TradeRequestList;
import positions.commonGui;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class NewTraderTest extends javax.swing.JDialog {
    List<String> rulesFilesList = null;
    String userSelectedTradeRule;
    private final String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    List<TradeRules.Term> activeTerms = null;
    List<TradeRules.Term> openTerms = new ArrayList <>();
    List<TradeRules.Term> closeTerms = new ArrayList <>();
    TradeRules tradeRules = new TradeRules();
    File currentFolder = new File(System.getProperty("user.dir") + "/src/supportFiles/");
    File[] listOfFiles = currentFolder.listFiles();
    String userSelectedSymbolFile = "";
    IOTextFiles.ioRdTextFiles userRdSymbolTextFile;
    boolean tradeRulesInputIgnore = false;
    List<String> symbolList = new ArrayList<>();
    String todaysDate;
    TradeRules.OccurenceLogPeriod openTradeLogPeriod;
    TradeRules.OccurenceLogPeriod closeTradeLogPeriod;
    TradeRules.OccurenceLogPeriod mergedOpenCloseTradeLogPeriod;
    HistoryConfigDialog historyConfigDialog = null;
    HistoryConfig historyConfig = null;
	PurchaseLimitDialog purchaseLimitDialog = null;
	PurchaseLimits purchaseLimits;
	
    String userSelHistSize = "2 Y";
    String userSelBackTestSize = "1 Y";
    String userSelBarSize = "1 day";
    String userSelDate = "";
	String userSelTime = "13:15:00"; 
	//purchase limit stuff..first total account Limit
	double userSelAccountLimit = 0.0;
	//user wants fixed number of dollars per position i.e 10,000 dollars
	double userSelFixedDollarsPerStockLimit = 0.0;
	//user wants fixed number of shares per position i.e 200 shares
	int userSelFixedNumberOfSharesLimit = 0;
	boolean userSelEquallyDivideAmongStocks = false;
	//if bar sz less than 1day then must be intraday..
	boolean userSelIntraDayTrading = false;
    TickerGroup actTickerGroup;
    boolean userSelLiveTrades = false;
    //userSelEventWindow i.e. occurence happened: 0 == today, 1 == yesterday .. today means partial bar because bar is not cloed yet. 
	// yesterday means completed bar. Default to yesterday, or completed bar.
    String userSelActiveBar = "previous"; 
    TradeActivity actTradeActivity = null;
    TradeRequestList actTradeRequestList = null;
    TradeActivityDialogForm actTradeActivityDialogForm = null;
    String userSelectedAccountNumber = "DU218372";
    double availCashInAccount = 0.0;
    boolean userMarketTrade = false; /* do bidAsk algo trades for now..*/
    List<String> symbolFailedList = new ArrayList<>();
    List<StockCorrections> allStockCorrectionList = new ArrayList<>();
    int userBuyLotsSz = 1;
    int userSellLotsSz = 1;
	boolean userSelectedSingleStock = false;
	boolean forceNewHistData = false;
	String userSelectedStockSymbol = "";
	String userSelectedMaType = "Simple";
	ManagedAccounts actManagedAcccounts;
	//ugly but global debug variable..trying to find a bug..
	boolean Debug = false;
	//temporary..
	boolean trimBackTest = false;
	//total cash allowed must divide by ticker list to assign amout per stock
	double userCashAllowedTotal = 0;
	//double userCashSingleStock = 3000;
    public class StockCorrection {
        boolean correctionComplete;
        double startCorrectionValue;
        String startCorrectionDate;
        double troughValue;
        String troughDate;
        double endCorrectionValue;
        String endCorrectionDate;       
        double correction;
        double correctionPercent;
        double troughToCurrentValue;
        double troughToCurrentPercent;
        double startOfCorrectionToCurrentValue;
        double startOfCorrectionToCurrentPercent;
        void fill(ibApi.historicalData.data din){
            startCorrectionValue = din.getClosePrice();
            startCorrectionDate = din.getDate();
        }
    }
    public class StockCorrections{
        String stockTicker;
        int numOfCorrections;
        double newHigh;
        double newHighPercent;
        boolean isNewHigh;
		//take sum of all corrections and div by numOfCorrections
		double aveCorrectionPercent;
		//percent above/below aveCorrection i.e. 1% is 1 percent above ave -1% is -1 percent below ave
		double currentlyWithinAveCorrectionPercent;
        List<StockCorrection> correctionList = new ArrayList<>();
    }
    public class TickerGroup {
        String groupName;
        int groupCurrentPositionOpenCnt = 0;
        int groupCurrentOpenEquationTrueCnt = 0;
        int groupCurrentCloseEquationTrueCnt = 0;
        List<String> groupCurrentPositionOpenTickerList = new ArrayList<>();
        List<String> groupCurrentOpenEquationTrueTickerList = new ArrayList<>();
        List<String> groupCurrentClosedEquationTrueTickerList = new ArrayList<>();
        List<TradeRules.OccurenceLogPeriod> openTradeLogList = new ArrayList<>();
        List<TradeRules.OccurenceLogPeriod> closeTradeLogList = new ArrayList<>();
        List<ibApi.historicalData.data> getOpenHistData(String tickerIn){
            int ix;
            for (ix = 0; ix < openTradeLogList.size(); ix++){
                if(openTradeLogList.get(ix).ticker.equals(tickerIn)){
                    return openTradeLogList.get(ix).histDataList;
                }
            }
            return null;
        }
        void putOpenHistData(String tickerIn, List<ibApi.historicalData.data> hdIn){
            int ix;
            for (ix = 0; ix < openTradeLogList.size(); ix++){
                if(openTradeLogList.get(ix).ticker.equals(tickerIn)){
                    openTradeLogList.get(ix).histDataList = hdIn;
                }
            }
        }
        void addOpenTradeLog(String tickerIn, TradeRules.OccurenceLogPeriod tradeLogIn){
            int ix;
            for (ix = 0; ix < openTradeLogList.size(); ix++){
                if(openTradeLogList.get(ix).ticker.equals(tickerIn)){
                    //it exists no need to add, so exit.
                    return;
                }
            }
            //made it here so not already in list, so add it now..
            openTradeLogList.add(tradeLogIn);
        }
        void addCloseTradeLog(String tickerIn, TradeRules.OccurenceLogPeriod tradeLogIn){
            int ix;
            for (ix = 0; ix < closeTradeLogList.size(); ix++){
                if(closeTradeLogList.get(ix).ticker.equals(tickerIn)){
                    //it exists no need to add, so exit.
                    return;
                }
            }
            //made it here so not already in list, so add it now..
            closeTradeLogList.add(tradeLogIn);
        }
        List<ibApi.historicalData.data> getCloseHistData(String tickerIn){
            int ix;
            for (ix = 0; ix < closeTradeLogList.size(); ix++){
                if(closeTradeLogList.get(ix).ticker.equals(tickerIn)){
                    return closeTradeLogList.get(ix).histDataList;
                }
            }
            return null;
        }
        void putCloseHistData(String tickerIn, List<ibApi.historicalData.data> hdIn){
            int ix;
            for (ix = 0; ix < closeTradeLogList.size(); ix++){
                if(closeTradeLogList.get(ix).ticker.equals(tickerIn)){
                    closeTradeLogList.get(ix).histDataList = hdIn;
                }
            }
        }
    }
    
    private static enum ShowOptions{
        oSHOW_ALL("AllOccurences"),
        oSHOW_TRANSITIONS("TransitionsOnly"),
        oSHOW_CURRENT("CurrentActive"),
        oSHOW_TRADES("BackTestTrades"),
        oSHOW_CORRECTIONS("Corrections"),
        oSHOW_UNUSUALOPTION_VOL("UnusualOptionVolume");        
        private final String strVal;
        ShowOptions(String strin){
            strVal = strin;
        }
        public String getStrVal(){
            return strVal;
        }        
        public static ShowOptions valueOfOrd(String strin){
            for (ShowOptions i : ShowOptions.values()){
                if(i.getStrVal().equalsIgnoreCase(strin)){
                    return i;
                }
            }
            return null;
        }
        public static ShowOptions ordFromString(String text) {
            for (ShowOptions b : ShowOptions.values()) {
                if (b.strVal.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }
    ShowOptions userSelShowOption;

    /**
     * Creates new form NewTraderTest
     */
    
    public NewTraderTest(java.awt.Frame parent, boolean modal) {        
        super(parent, modal);
        initComponents();
		String accNumber = "";
        String accAlias = ""; 
        addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });        
        historyConfigDialog = new HistoryConfigDialog(new javax.swing.JFrame(), true, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelDate);
        //historyConfig = historyConfigDialog.deSerialize();
        historyConfig = historyConfigDialog.getHistoryConfig();        
        userSelHistSize = historyConfig.getUserSelHistSize();
        userSelBackTestSize = historyConfig.getUserSelBackTestSize();
        userSelBarSize = historyConfig.getUserSelBarSize();
        userSelDate = historyConfig.getUserSelDate();
		userSelIntraDayTrading = historyConfig.getUserSelIntraDayTrade();
        updateHistoricalLables();                
        todaysDate =  myUtils.GetTodaysDate("yyyyMMdd HH:mm:ss");
        //purchase limits dialog..
		purchaseLimitDialog = new PurchaseLimitDialog(parent, modal, 100000, 20000, 100, false);
		purchaseLimits = purchaseLimitDialog.getPurchaseLimits();
		userSelAccountLimit = purchaseLimits.getAccountLimit();
		
		userSelFixedDollarsPerStockLimit = purchaseLimits.getPositionDollarAmountLimit();
		userSelFixedNumberOfSharesLimit = purchaseLimits.getPositionFixedNumOfSharesLimit();
		userSelEquallyDivideAmongStocks = purchaseLimits.getDivideEquallAmongAll();
		updatePurchaseLimitLables();
		tradeTextArea.append("\nAccountLimit: " + purchaseLimits.displayIt(userSelAccountLimit));
		tradeTextArea.append("\nPositionLimit: " + purchaseLimits.displayIt(userSelFixedDollarsPerStockLimit));
		tradeTextArea.append("\nPositionLimitByStock: " + Double.toString(userSelFixedNumberOfSharesLimit));
		tradeTextArea.append("\nDividAmongAllStocks: " + Boolean.toString(userSelEquallyDivideAmongStocks));
        activeTerms = openTerms; 
        tradeRules.setActiveTerms(activeTerms);
        tradeRules.setOpenTerms(openTerms);
        tradeRules.setClosedTerms(closeTerms);      
        rulesFilesList = tradeRules.getTradeRulesFiles();
        System.out.println("\nrulesDirList size " + rulesFilesList.size());
        tradeRuleComboBox.removeAllItems();
        tradeRulesInputIgnore = true;        
        for (int i = 0; i < rulesFilesList.size(); i++){
            tradeRuleComboBox.addItem(rulesFilesList.get(i));
        }
        tradeRulesInputIgnore = false;
        indexComboBox.removeAllItems();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith("sym")) {
                System.out.println("File " + listOfFiles[i].getName());
                indexComboBox.addItem(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        if (indexComboBox.getItemCount() > 0) {
            userSelectedSymbolFile = (String)indexComboBox.getSelectedItem();
            System.out.println("\nselectedIndex = " + userSelectedSymbolFile);
        } 
        userSelShowOption = ShowOptions.valueOfOrd(showComboBox.getSelectedItem().toString());
        System.out.println("\nuserSelShowOption : " + userSelShowOption);
		actTradeRequestList = new TradeRequestList();
		actTradeActivity = new TradeActivity(actTradeRequestList);
		actTradeActivity.setTradeMode((
                                        (userMarketTrade == true) ? 
                                        TradeRequestData.TradeModes.oMarket : 
                                        TradeRequestData.TradeModes.oLimitAlgo)
		);		
        actTradeActivity.setAccountNumber(userSelectedAccountNumber);
		//set eventWindow to either today or yesterday...
		activeBarComboBox.setEnabled(true);
		activeBarComboBox.setSelectedItem(userSelActiveBar);
		activeBarComboBox.setEnabled(false);
        if(userSelLiveTrades == true){
            activeBarComboBox.setEnabled(true);
            activeBarLabel.setEnabled(true);
        }else{
            activeBarComboBox.setEnabled(false);
            activeBarLabel.setEnabled(false);
        }
		singleStockTextField.setEnabled(false);
		if(userSelIntraDayTrading == true){
			userSelectedSingleStock = true;
			//if checking the box programmically..we need to run the action too..
			singleStockCheckBox.doClick();
			singleStockCheckBox.setSelected(userSelIntraDayTrading);
		}		
		System.out.println("\ninitialize accounts combo box..");
		actManagedAcccounts = ManagedAccounts.getAllAccounts();
		selectAccountComboBox.removeAllItems();
        for(int idx = 0; idx < actManagedAcccounts.getNumOfAccounts(); idx++){ 
            accNumber = actManagedAcccounts.getAnAccount(idx).getName();
            accAlias = actManagedAcccounts.getAliasFromAccount(accNumber);            
            selectAccountComboBox.addItem(accNumber + "(" + accAlias + ")");
        }
		
    }
    boolean tradeTaskRunning = false;
    int iteration = 0;
    String lastTicker = "";
    
    private List<TradeRules.TermOccurences> processTerms(String forTicker, List<TradeRules.Term> termsIn, String operationIn) {
        TradeRules.Term actTerm = tradeRules.new Term();
        int termIdx;
        int numOfTerms = termsIn.size();       
        List<TradeRules.TermOccurences> retTermTradesList = new ArrayList<>();
		TradeRules.TermOccurences actTermOccurence = tradeRules.new TermOccurences();
		boolean latchedError = false;
        if (numOfTerms == 0) {
            return retTermTradesList;
        }
        actTerm = termsIn.get(0);
        if ((lastTicker != forTicker) || (forceNewHistData == true)) {
            tradeRules.setMaType(userSelectedMaType);
            //if tickers changed or working with single stock force new data from ib..
            //tradeTextArea.append("\n  getting new historical data from API.");
            if (actTerm.prepareHistoricalData(forTicker, true/* this will force new data from ib */) == true) {
                //tradeTextArea.append(" OK.");
                lastTicker = forTicker;
            } else {
                tradeTextArea.append(" Failure getting historical data for ticker: " + forTicker);
                symbolFailedList.add(forTicker);
                lastTicker = "";
                //return empty list on error.
                return retTermTradesList;
            }
            forceNewHistData = false;
        }
        for (termIdx = 0; termIdx < numOfTerms; termIdx++) {
            actTerm = tradeRules.new Term();
            actTerm = termsIn.get(termIdx);
            System.out.println("\ntermsIn.getAndOr() " + actTerm.getAndOr());
            System.out.println("\ntermsIn.getAndOr() " + actTerm.getAndOrSelItem());
            retTermTradesList.add(actTermOccurence = actTerm.processTerm(forTicker, operationIn, actTerm.getMa()));
            if (actTermOccurence.termHead.termRanFine == false) {
                latchedError |= true;
            }
        }
        if (latchedError == true) {
            tradeTextArea.append(" Term had error.");
        } else {
            tradeTextArea.append(" OK.");
        }
        return retTermTradesList;
    }

    private int isDateInList(List<TradeRules.OccurenceData> termIn, String dateIn) {
        /*
         return -1 if not in list, else return the index of termIn the date is in..
         */
        int tradeIdx = 0;
        int numOfTrades = termIn.size();
        for (tradeIdx = 0; tradeIdx < numOfTrades; tradeIdx++) {
            if (termIn.get(tradeIdx).occurenceDate.equals(dateIn)) {
                return tradeIdx;
            }
        }
        return -1;
    }
    boolean isEquationTrue(List<TradeRules.TermOccurences> termsIn) {
        /*
         check each term's logic (And/OR) and perform over all result i.e. term1 AND term2 OR term3..
         */
        TradeRules tradeRulesOuter = new TradeRules();

        TradeRules.TermHead actTerm = tradeRulesOuter.new TermHead();
        int termIdx;
        boolean result = false;
        actTerm = termsIn.get(0).termHead;
        result = actTerm.termIsTrue;
        String actOp = actTerm.andOrLogic;
        for (termIdx = 1; termIdx < termsIn.size(); termIdx++) {
            actTerm = termsIn.get(termIdx).termHead;
            if (actOp.equals("AND")) {
                //And operation
                result &= actTerm.termIsTrue;
            }else if(actOp.equals("OR")){
                //OR operation
                result |= actTerm.termIsTrue;
            }
            actOp = actTerm.andOrLogic;
        }
        return result;
    }
    
    int howManyMatches(String dateIn, List<TradeRules.OccurenceData> occurencesIn){
        int count = 0;
        int idx = 0;
        int occurenceCnt = occurencesIn.size();
        if(occurenceCnt == 0){
            return -1;
        }
        for(idx = 0; idx < occurenceCnt; idx++){
            if(dateIn.equals(occurencesIn.get(idx).occurenceDate)){
                count++;
            }
        }
        return count;
    }
    int removeDuplicates(List<TradeRules.OccurenceData> occurencesIn){
        /*
         remove any duplicate dates in occurence sequence.
        */
        int count = 0;
        int idx = 0;
        int occurenceCnt = occurencesIn.size();
        TradeRules.OccurenceData actOccurence;
        if(occurenceCnt == 0){
            return -1;
        }
        actOccurence = occurencesIn.get(idx);
        for(idx = 0; idx < occurenceCnt; idx++){
            
        }
        actOccurence = occurencesIn.get(idx);
        return count;
    }
    int findEarliestDatePosition(List<TradeRules.OccurenceData> occurencesIn){
        long pos = 0;
        int idx = 0;
        int retPos = 0;
        TradeRules.OccurenceData actOccurence;
        int occurenceCnt = occurencesIn.size();
        if(occurenceCnt == 0){
            return -1;
        }
        actOccurence = occurencesIn.get(idx);
        for(idx = 0; idx < occurenceCnt; idx++){
            if((pos = myUtils.getDiffInDays(actOccurence.occurenceDate, occurencesIn.get(idx).occurenceDate)) < 0){
                actOccurence = occurencesIn.get(idx);
                retPos = idx;
            }
        }
        
        return retPos;
    }
    String findOldestDate(List<TradeRules.OccurenceData> occurencesIn){
        long newDiff;
        int idx;
        long oldestDiff;
        String oldestDate = todaysDate;
        TradeRules.OccurenceData actOccurence;
        int occurenceCnt = occurencesIn.size();
        if (occurenceCnt == 0) {
            return "";
        }
        oldestDiff = myUtils.getDiffInDays(todaysDate, occurencesIn.get(0).occurenceDate);
        oldestDate = occurencesIn.get(0).occurenceDate;
        for (idx = 0; idx < occurenceCnt; idx++) {
            if ((newDiff = myUtils.getDiffInDays(todaysDate, occurencesIn.get(idx).occurenceDate)) < oldestDiff) {                
                oldestDiff = newDiff;
                oldestDate = occurencesIn.get(idx).occurenceDate;
            } else {

            }
        } 
        return oldestDate;
    }
    int findOldestDatePos(List<TradeRules.OccurenceData> occurencesIn){
        long newDiff;
        int idx;		
        long oldestDiff;
        int oldestDatePos = 0;
        String oldestDate = "";//todaysDate;
		String tmpStr = "";
        int occurenceCnt = occurencesIn.size();
		if (occurenceCnt == 0) {
            return -1;
        }
		boolean timePresent = occurencesIn.get(0).occurenceDate.contains(":") ? true : false;
		if (timePresent == true){
			tmpStr = myUtils.GetTodaysDate("yyyyMMdd  hh:mm:ss");
			//set seconds to 00
			todaysDate = (tmpStr.substring(0, 16) +"00");
			//remove!!!!!!!!!
			//todaysDate = "20200409  12:59:00";
			oldestDate = todaysDate;
		}else{
			oldestDate = todaysDate = myUtils.GetTodaysDate("yyyyMMdd");
		}
        //oldestDiff = myUtils.getDiffInDays(todaysDate, occurencesIn.get(0).occurenceDate);
		oldestDiff = tradeRules.getTradingBarsBetweenDates(todaysDate, occurencesIn.get(0).occurenceDate);
        //0 index is always oldest in time
		oldestDate = occurencesIn.get(0).occurenceDate;
        for (idx = 0; idx < occurenceCnt; idx++) {
            if ((newDiff = Math.abs(tradeRules.getTradingBarsBetweenDates(todaysDate, occurencesIn.get(idx).occurenceDate))) > oldestDiff) {                
                oldestDiff = newDiff;
                oldestDate = occurencesIn.get(idx).occurenceDate;
                oldestDatePos = idx;
            } else {

            }
        } 
        return oldestDatePos;
    }
    private List<TradeRules.OccurenceData> sortOccurenceDates(String begDate, String tickerIn, List<TradeRules.OccurenceData> tradesIn){
        /*
         take input allOccurences and sort in assending order by date. Also remove any duplicates.
        */        
        int oldestDatePos = 0;
        int idx = 0;
        int days = 0;
        int trimSz = 0;
        List<TradeRules.OccurenceData> newList = new ArrayList<>();
        if(tradesIn.isEmpty() == true){
            return newList;
        }
        while (tradesIn.size() > 0) {  
            oldestDatePos = findOldestDatePos(tradesIn);
            //check for duplicates, delete em, add only new ones..
            if ((oldestDatePos != -1) && (!newList.contains(tradesIn.get(oldestDatePos)))) {
                //no duplicate, so add new oldest date..
                newList.add(tradesIn.get(oldestDatePos));
                tradesIn.remove(oldestDatePos);
            } else {
                //must be duplicate so just remove it..
                tradesIn.remove(oldestDatePos);
            }
        }
		//wfs remove false...fall through loop if trimBackTest is true @!!!!!!!
		//if trimBackTest == true, wait for lagit start signal before opening a position,
		//else if false, open a position if open condition is true at begining of backtest period (buy it right away if 
		//condition is true)..
        if ((begDate != null) && (begDate.equals(newList.get(0).occurenceDate)) && (trimBackTest == true)){
            //now trim begining of stream of occurences that are consecutive dates
            trimSz = newList.size();
            for (idx = 0; idx < trimSz; idx++) {
                if ((newList.size() > 1) && ((days = tradeRules.getTradingDaysBetweenDates(newList.get(0).occurenceDate, newList.get(1).occurenceDate)) > 1)) {
                    if(!newList.isEmpty()){
                        newList.remove(0);
                    }                    
                    break;
                } else {
                    if(!newList.isEmpty()){
                        newList.remove(0);
                    }
                }
            }
        }
        if (!newList.isEmpty()) {
            tradesIn.removeAll(tradesIn);
            tradesIn.addAll(newList);
        }
        return newList;        
    }

	private List<TradeRules.OccurenceData> sortOccurenceDates1(String begDate, String tickerIn, List<TradeRules.OccurenceData> tradesIn) {
		/*
         take input allOccurences and sort in assending order by date. Also remove any duplicates.
		 */
		int oldestDatePos = 0;
		int idx = 0;
		int days = 0;
		int trimSz = 0;
		List<TradeRules.OccurenceData> newList = new ArrayList<>();
		if (tradesIn.isEmpty() == true) {
			return newList;
		}
		while (tradesIn.size() > 0) {
			oldestDatePos = findOldestDatePos(tradesIn);
			//check for duplicates, delete em, add only new ones..
			if ((oldestDatePos != -1) && (!newList.contains(tradesIn.get(oldestDatePos)))) {
				//no duplicate, so add new oldest date..
				newList.add(tradesIn.get(oldestDatePos));
				tradesIn.remove(oldestDatePos);
			} else {
				//must be duplicate so just remove it..
				tradesIn.remove(oldestDatePos);
			}
		}
		if ((begDate != null) && (begDate.equals(newList.get(0).occurenceDate))) {
			//now trim begining of stream of occurences that are consecutive dates
			trimSz = newList.size();
			for (idx = 0; idx < trimSz; idx++) {
				if ((newList.size() > 1) && ((days = tradeRules.getTradingDaysBetweenDates(newList.get(0).occurenceDate, newList.get(1).occurenceDate)) > 1)) {
					if (!newList.isEmpty()) {
						newList.remove(0);
					}
					break;
				} else {
					if (!newList.isEmpty()) {
						newList.remove(0);
					}
				}
			}
		}
		if (!newList.isEmpty()) {
			tradesIn.removeAll(tradesIn);
			tradesIn.addAll(newList);
		}
		return newList;
	}
	//List<TradeRules.MergeList> mergeList
    private List<TradeRules.OccurenceData> logicallyMerge(String logicalOp, TradeRules.MergeList list1, TradeRules.MergeList list2){
        /*
        take two input lists and generate one output list.        
        AND 
            - only common entries are passed on to output list i.e for a true output, date needs to be in both terms.
        OR 
            - one instance of each entry in both term lists will be included in output list. i.e summed together no duplicates.
        */
        List<TradeRules.OccurenceData> result = new ArrayList<>();  
        TradeRules tradeRulesOuter = new TradeRules();
        int idx;
        int ldx;
        TradeRules.OccurenceData tdata = tradeRulesOuter.new OccurenceData();
        switch (logicalOp){
            case "AND":
                //do AND merge
                for(idx = 0; idx < list1.occurences.size(); idx++){
                    if(((ldx = isDateInList(list2.occurences, list1.occurences.get(idx).occurenceDate))) != -1){
                        //merge termOutputList of each occurence..w/o duplicates though..
                        list1.occurences.get(idx).termOutputList.removeAll(list2.occurences.get(ldx).termOutputList);
                        list1.occurences.get(idx).termOutputList.addAll(list2.occurences.get(ldx).termOutputList);
                        list1.occurences.get(idx).occurenceDescription += ((logicalOp).concat(list2.occurences.get(ldx).occurenceDescription));
                        result.add(list1.occurences.get(idx));                       
                    }else{
                        
                    }
                }
                break;
            case "OR": 
                //do OR merge, all occurences1 go in                 
                for(idx = 0; idx < list1.occurences.size(); idx++){ 
                    list1.occurences.get(idx).occurenceDescription += (logicalOp);
                    result.add(list1.occurences.get(idx));                    
                }
                // now all of occurencs2 go in as long as it's not already in
                for(idx = 0; idx < list2.occurences.size(); idx++){
                    if(((ldx = isDateInList(list1.occurences, list2.occurences.get(idx).occurenceDate))) == -1){
                        //list2.allOccurences.get(idx).occurenceDescription += (list1.allOccurences.get(idx).occurenceDescription);
                        result.add(list2.occurences.get(idx));                        
                    }else{
                        //common to both lists so merge termOutputLists.. in original list1, which is already inside result list.
                        tdata = result.get(ldx);
                        //no duplicates so removeAll common in both list then addAll
                        tdata.termOutputList.removeAll(list2.occurences.get(idx).termOutputList);
                        tdata.termOutputList.addAll(list2.occurences.get(idx).termOutputList);
                        result.add(tdata);
                    }
                }
                break;
            default:
                break;
        }        
        return result;
    }
    private List<TradeRules.MergeList> merge(String operation, List<TradeRules.MergeList> listsToMerge){
        /*
        ListToMerge is list of OccurenceData that will be paired and merged. 
        This repeats until all pairs have merged RECURSIVELY!!! to just one.
        That one is then returned.
        */
        int numOfLists = listsToMerge.size();
        int numOfPairs = (numOfLists / 2);
        boolean perfectlyPaired = ((numOfLists % 2) == 0);
        int actPair;
        List<TradeRules.MergeList> mergeList = new ArrayList<>();
        List<TradeRules.OccurenceData> result = new ArrayList<>();
        List<TradeRules.MergeList> resultList = new ArrayList<>();
        //TradeRules tradeRulesOuter = new TradeRules();
        TradeRules.MergeList mlst;// = tradeRulesOuter.new MergeList();
        if(tradeRulesOuter == null){
            tradeRulesOuter = new TradeRules();
        }
        //copy list to merge..
        for(int idx = 0; idx < listsToMerge.size(); idx++){
            mergeList.add(listsToMerge.get(idx));
        }
        while (mergeList.size() > 1){
            for (actPair = 0; actPair < (numOfPairs * 2); actPair +=2){
                result = logicallyMerge(mergeList.get(actPair).logicalOperator, mergeList.get(actPair), mergeList.get(actPair+1)); 
                mlst = tradeRulesOuter.new MergeList();
                mlst.occurences = result;
                //merged, now move logical operator to new merge..
                mlst.logicalOperator = mergeList.get(actPair+1).logicalOperator;
                resultList.add(mlst);                               
            }
            /*check if perfectly paired up in above for loop..
            if not we have one last odd one dangling, so merge that one now..
            merge last one in mergeList with the last pair we merged above that is now 
            the last element in the resultList..
            */
            if(!perfectlyPaired){
                result = logicallyMerge(mergeList.get(numOfLists - 2).logicalOperator, mergeList.get(numOfLists - 1), resultList.get(resultList.size()-1));
                mlst = tradeRulesOuter.new MergeList();
                mlst.occurences = result;
                resultList.add(mlst);
            }            
            mergeList.removeAll(mergeList);
            mergeList = merge(operation, resultList);
        }
        return mergeList;
    }

    private List<TradeRules.OccurenceData> applyTrigger(List<TradeRules.OccurenceData> occurenceIn, String operation) {
        /*
         Psuedo code:
            go through final occurence list and apply trigger criteria to the list and generate 
            an output reflecting the trigger criteria.
            For now, the two trigger options are:
            - Begin; go through all dates of allOccurences starting with the first and only generate trigger if in the last number of Bars, the transition to true occurred.
              For example, if the bars was set at 5, the occurence transitioned to true during the last 5 bars. If so, the trigger occurred. This
              is used to capture when the condition began.
            - Delayed;go through all dates of allOccurences starting with the first and only generate a trigger if in the last number of Bars, 
              the occurence is true. So if bars was set to 5, the trigger will happen if for last 5 bars, the condition was true.
         
         */
        //TradeRules tradeRulesOuter = new TradeRules();
        int tradingDays;
        String trigger;
        int bars;
        int idx;
        int idy;
        boolean failed = false;
        List<TradeRules.OccurenceData> retList = new ArrayList<>();
        if(operation.equals(slopeDefs.oOPEN)){
            trigger = tradeRules.getEquationTrigger("Open");
            bars = tradeRules.getEquationBars("Open");
        }else{
            trigger = tradeRules.getEquationTrigger("Close");
            bars = tradeRules.getEquationBars("Close");
        }

        switch (trigger){
            case "Begin":
                for(idx =  bars; idx < occurenceIn.size(); idx++, failed = false){
                    for(idy = idx; ((idy > ((idx - bars))) && !failed); idy--){
                        if(tradeRules.getTradingDaysBetweenDates(occurenceIn.get(idy).occurenceDate, occurenceIn.get(idy-1).occurenceDate) != 1){
                            failed = true;
                        }
                    }
                    if(failed){
                        retList.add(occurenceIn.get(idx));
                    }else{
                        //it failed so don't add
                    }
                }
                break;
            case "Delayed":
                for(idx =  bars; idx < occurenceIn.size(); idx++, failed = false){
                    for(idy = idx; ((idy > ((idx - bars))) && !failed); idy--){
                        if(tradeRules.getTradingDaysBetweenDates(occurenceIn.get(idy).occurenceDate, occurenceIn.get(idy-1).occurenceDate) != 1){
                            failed = true;
                        }
                    }
                    if(!failed){
                        retList.add(occurenceIn.get(idx));
                    }else{
                        //it failed so don't add
                    }
                }
                break;
            case "None":
                break;
            default:
                break;
        }               
        return retList;
    }    
    private List<TradeRules.OccurenceData> filterTransitions(List<TradeRules.OccurenceData> occurenceIn) {
        /*
         Psuedo code:
        only display allOccurences 
         */
        //TradeRules tradeRulesOuter = new TradeRules();
        int idx;
        List<TradeRules.OccurenceData> retList = new ArrayList<>();
        //first one goes in regardless..
        if (!occurenceIn.isEmpty()) {
            retList.add(occurenceIn.get(0));
        }
        for (idx = 1; idx < occurenceIn.size(); idx++) {
            if (!(occurenceIn.get(idx).occurenceDescription.equals(occurenceIn.get(idx - 1).occurenceDescription))) {
                //two consecutive dates are different terms, so display
                retList.add(occurenceIn.get(idx));
            } else if (tradeRules.getTradingDaysBetweenDates(occurenceIn.get(idx).occurenceDate, occurenceIn.get(idx - 1).occurenceDate) != 1) 
              {
                //the two consecutive dates are equal but distance between two dates are more than one, so display..
                retList.add(occurenceIn.get(idx));
            }
        }
        return retList;
    }    
    TradeRules tradeRulesOuter = null;
    private void mergeTerms(TradeRules.OccurenceLogPeriod logIn, String operation) {
        /*
         Psuedo code:
         logIn has all terms to merge. They will be grouped in two's i.e fist two is one group, next two second group etc.
         if there on odd number of terms, say 3, the first two will be group 1, merged logically, then result will be merged with 
         third term. If there are 4 terms, result of merge of group1/2 will be merged with result of merge of group3/4.
         The merge is always with two lists (could be group1 list and group 2 list) and is as follows:
         MERGE two term lists:
         AND 
         - only common entries are passed on to output list i.e for a true output, date needs to be in both terms.
         OR 
         - one instance of each entry in both term lists will be included in output list. i.e summed together no duplicates.
         */
        //TradeRules tradeRulesOuter = new TradeRules();
        int numOfTerms = logIn.allTerms.size();
        int termIdx;      
        List<TradeRules.MergeList> mergeList = new ArrayList<>();
        if(tradeRulesOuter == null){
            tradeRulesOuter = new TradeRules();
        }
        TradeRules.MergeList mlst = tradeRulesOuter.new MergeList();
        List<TradeRules.MergeList> result = new ArrayList<>();
        if (numOfTerms == 0) {
            return;
        }
        //search all terms of equation..        
        for (termIdx = 0; (termIdx < numOfTerms) && (!logIn.allTerms.isEmpty()); termIdx++) {
            mlst = tradeRulesOuter.new MergeList();
            //mlst.allOccurences = new ArrayList<TradeRules.OccurenceData>();
            mlst.occurences = logIn.allTerms.get(termIdx).occurences;
            mlst.logicalOperator = logIn.allTerms.get(termIdx).termHead.andOrLogic;
            mlst.termsDescription = logIn.allTerms.get(termIdx).termHead.termDescription;
            mergeList.add(mlst);
        }
        result = merge(operation, mergeList);
        result.get(0).occurences = sortOccurenceDates(logIn.begBtDate, logIn.ticker, result.get(0).occurences);
        logIn.allOccurences = result.get(0).occurences;
        logIn.transitionOccurences = filter(ShowOptions.oSHOW_TRANSITIONS, logIn.allOccurences, logIn.endDate);
        logIn.currentOccurences = filter(ShowOptions.oSHOW_CURRENT, logIn.allOccurences, logIn.endDate);
        //return result.get(0).occurences;
    }
    List<TradeRules.OccurenceData> filter(ShowOptions optionIn, List<TradeRules.OccurenceData> listIn, String dateIn){
        List<TradeRules.OccurenceData> resList = new ArrayList<>();
        int idx;
		int tmp1;
		String occurenceDate = "";
        switch(optionIn){
            case oSHOW_TRANSITIONS:
                //first one goes in regardless..
                if (!listIn.isEmpty()) {
                    resList.add(listIn.get(0));
                }
                for (idx = 1; idx < listIn.size(); idx++) {
                    if (!(listIn.get(idx).occurenceDescription.equals(listIn.get(idx - 1).occurenceDescription))) {
                        //two consecutive dates are different terms, so display
                        resList.add(listIn.get(idx));
                    } else if ((tmp1 = tradeRules.getTradingDaysBetweenDates( listIn.get(idx - 1).occurenceDate, listIn.get(idx).occurenceDate)) != 1) {
                        //the two consecutive dates are equal but distance between two dates are more than one, so display..
                        resList.add(listIn.get(idx));
                    }
                }
                break;
            case oSHOW_CURRENT:
                for (int x = 0; x < listIn.size(); x++) {
					/*
					if (todaysDate.equals(listIn.get(x).occurenceDate)) {
                        resList.add(listIn.get(x));                       
                    }
					*/
					occurenceDate = listIn.get(x).occurenceDate;
                    if ((occurenceDate != null) && (dateIn.equals(occurenceDate))) {
                        resList.add(listIn.get(x));                       
                    }
                };
                break;
            default:;
        }
        
        return resList;
    }
    private void placeLiveTrade(String tickerIn, boolean buyIt, int numOfShares){	
	    actTradeActivity.placeOrderNew(tickerIn, buyIt, numOfShares, "");
	    System.out.println("\nwait for all filled.");
	    while (!actTradeRequestList.areAllFilled()) {
		    myUtils.delay(100);
	    }
	    System.out.println("\nwe are all filled.");

	}
	boolean debugTradeIsOpen = false;
	double debugBuyPrice = 0.0;
	double debugSellPrice = 0.0;
	double debugPL = 0.0;
	double debugPLPercent = 0.0;
	double debugTotalPL = 0.0;
	double debugTotalPLPercent = 0.0;
	double DEBUG_PRICE_LOCK = 0.80;
	private double debugGainLock(double bprice, double sprice){
		/*
		calculate current profit/loss and return value.
		*/
		double pl;
		double plPercent;
		
		plPercent = (myUtils.roundMe((((sprice - bprice) / bprice) * 100.0), 2));
		pl = myUtils.roundMe((sprice - bprice), 2);
		
		return pl;
	}
	private void processLiveFake(TradeRules.OccurenceLogPeriod openLogPeriod, TradeRules.OccurenceLogPeriod closeLogPeriod) {
		
		tradeTextArea.append("\n       Bars open is : " + openLogPeriod.positionDaysOpen);
		tradeTextArea.append("\n       Bars closed is : " + closeLogPeriod.positionLastCloseInDays);
		tradeTextArea.append("\n       CurrentOccurencesOpen : " + openLogPeriod.currentOccurences.size());
		tradeTextArea.append("\n       CurrentOccurencesClose : " + closeLogPeriod.currentOccurences.size());

		if ((openLogPeriod.positionOpen == true) && (openLogPeriod.currentOccurences.size() > 0)
			&& (openLogPeriod.positionDaysOpen == 1) && (debugTradeIsOpen == false)) {			
			//if we opened new position, must mean we have no closes..
			openLogPeriod.positionLastCloseInDays = closeLogPeriod.positionLastCloseInDays = 0;
			debugBuyPrice = openLogPeriod.currentOccurences.get(0).closePrice;
			tradeTextArea.append("\n       Fake Open trade on Ticker: " + openLogPeriod.ticker + " buyPrice: " + debugBuyPrice);
			System.out.println("\ndaysOpen: " + openLogPeriod.positionDaysOpen);
			debugTradeIsOpen = true;
			debugPL = 0.0;
			debugPLPercent = 0.0;
		} else if ((openLogPeriod.positionOpen == false) && (closeLogPeriod.currentOccurences.size() > 0)
				&& (closeLogPeriod.positionLastCloseInDays >= 1) && (debugTradeIsOpen == true)) {
			//if we closed new position, must mean we have no opens..
			openLogPeriod.positionDaysOpen = closeLogPeriod.positionDaysOpen = 0;
			debugSellPrice = closeLogPeriod.currentOccurences.get(0).closePrice;
			tradeTextArea.append("\n       Fake Close Trade on Ticker: " + openLogPeriod.ticker + " sellPrice: " + debugSellPrice);
			debugPLPercent = (myUtils.roundMe((((debugSellPrice - debugBuyPrice) / debugBuyPrice) * 100.0), 2));
			debugPL = myUtils.roundMe((debugSellPrice - debugBuyPrice), 2);
			debugTotalPL += debugPL;
			debugTotalPLPercent += debugPLPercent;
			tradeTextArea.append("\n       Profit/Loss: " + debugPL + " %: " + debugPLPercent);
			tradeTextArea.append("\n       ProfitTotal: " + debugTotalPL + " %: " + debugTotalPLPercent);
			debugTradeIsOpen = false;
		} else if ((false) && (debugTradeIsOpen == true) && (openLogPeriod.histDataList.size() > 0) && ((debugPL = debugGainLock(debugBuyPrice, openLogPeriod.histDataList.get(0).getClosePrice())) >= DEBUG_PRICE_LOCK)) {
			tradeTextArea.append("\n       ProfitLocked: " + debugPL);
			debugPLPercent = myUtils.roundMe((debugPL / debugBuyPrice) * 100.0, 2);
			tradeTextArea.append("\n       Profit/Loss: " + debugPL + " %: " + debugPLPercent);
			debugTotalPL += debugPL;
			debugTotalPLPercent += debugPLPercent;
			tradeTextArea.append("\n       ProfitTotal: " + debugTotalPL + " %: " + debugTotalPLPercent);
			debugTradeIsOpen = false;
		}
	}	
	private int calcSharesToBuy(TradeRules.OccurenceLogPeriod openLogPeriod, boolean displayOk){
		/*
		look at purchaseLimit data to determine how many shares we will buy
		*/
		double accountStockValue = 0.0;
		double unrealizedPl = 0.0;
		double accountMoneyInvested = 0.0;
		double currentStockPrice = 0.0;
		int retNumOfShares = 0;
		double perStockDollarLimit = 0.0;
		double availCash = 0.0;
		//get account total stock value
		accountStockValue = actAccInfo.getStockVal();
		//get account total unrealized gain/loss
		unrealizedPl = actAccInfo.getCurrentUnrealizedPandLVal();
		//get today's closing price (changes during day until close)
		currentStockPrice = openLogPeriod.histDataList.get(0).getClosePrice();
		//calculate amount invested so far by taking total stock value and subtracting unrealizedPl
		accountMoneyInvested = (accountStockValue - unrealizedPl);
		availCash = actAccInfo.getTotalCashVal();
		if(displayOk == true){
			tradeTextArea.append("\n            stockValue: " + accountStockValue);
			tradeTextArea.append("\n            unrealizedGL: " + unrealizedPl);
			tradeTextArea.append("\n            moneyInvested: " + accountMoneyInvested);
			tradeTextArea.append("\n            currentStockPrice: " + currentStockPrice);
			tradeTextArea.append("\n            availCash: " + availCash);
		}
		if(accountMoneyInvested > userSelAccountLimit){
			//we are over our limit already, so don't go on..
			return retNumOfShares;
		}		
		if((userSelFixedNumberOfSharesLimit != 0) && ((accountMoneyInvested + (userSelFixedNumberOfSharesLimit * currentStockPrice)) < userSelAccountLimit)){
			//we have money to buy more so return fixed number to buy..
			retNumOfShares = userSelFixedNumberOfSharesLimit;
		}else if ((userSelFixedDollarsPerStockLimit != 0) && ((accountMoneyInvested + userSelFixedDollarsPerStockLimit) < userSelAccountLimit)){
			//divide dollar amount limit wanted by stock price to return number of shares to buy..
			retNumOfShares = Math.round((float)(userSelFixedDollarsPerStockLimit / currentStockPrice));
		}else if(userSelEquallyDivideAmongStocks == true){
			//wants total account limit divided equally among all stocks in index.
			perStockDollarLimit = (userSelAccountLimit / openLogPeriod.partOfIndexOfThisSize);
			//check if we have enough money for this..
			if((accountMoneyInvested + perStockDollarLimit) < userSelAccountLimit){
				//yes we do calc numOfShares to buy..
				retNumOfShares = Math.round((float)(perStockDollarLimit / currentStockPrice));
			}else{
				//don't need this but ...
				retNumOfShares = 0;
			}
		}
		return retNumOfShares;		
	}
	private void processLiveTrades(TradeRules.OccurenceLogPeriod openLogPeriod, TradeRules.OccurenceLogPeriod closeLogPeriod) {
		int sharesInAccount;
		double availCash = 0.0;
		if ((openLogPeriod.positionOpen == true) && (openLogPeriod.currentOccurences.size() > 0)) {
			tradeTextArea.append("\nDAYS OPEN IS : " + openLogPeriod.positionDaysOpen);
		}

		if ((openLogPeriod.positionOpen == false) && (closeLogPeriod.currentOccurences.size() > 0)) {
			tradeTextArea.append("\nDAYS CLOSED IS : " + closeLogPeriod.positionLastCloseInDays);
		}		
		if ((openLogPeriod.positionOpen == true) && (openLogPeriod.currentOccurences.size() > 0)
			&& (openLogPeriod.positionDaysOpen == 1)) {
			//get shares in account..
			sharesInAccount = actAccInfo.getNumberOfShares(openLogPeriod.ticker);
			//if we opened new position, must mean we have no closes..
			openLogPeriod.positionLastCloseInDays = closeLogPeriod.positionLastCloseInDays = 0;			
			if ((sharesInAccount == 0) /*&& ((availCash = actAccInfo.getCashVal()) > 0.0)*/ && (openLogPeriod.liveSharesToBuy != 0)) {
				//open a trade!!!
				tradeTextArea.append("\n   Place Open Trade on Ticker: " + openLogPeriod.ticker + " shares we can buy: " + openLogPeriod.liveSharesToBuy);
				if(openLogPeriod.liveSharesToBuy > 500){
					//ib 500 max limit
					tradeTextArea.append("\nmax shares allowed by IB is 500! So set purchase to 500.");
					openLogPeriod.liveSharesToBuy = 500;
				}
				placeLiveTrade(openLogPeriod.ticker, true, openLogPeriod.liveSharesToBuy);				
			} else {
				tradeTextArea.append("\n     Shares in Account: " + sharesInAccount + " AvailCash: " + availCash + " SharesWeCanBuy: " + openLogPeriod.liveSharesToBuy);
			}
			System.out.println("\ndaysOpen: " + openLogPeriod.positionDaysOpen);
		} else if ((openLogPeriod.positionOpen == false) && (closeLogPeriod.currentOccurences.size() > 0)
			&& (closeLogPeriod.positionLastCloseInDays >= 1)) {
			sharesInAccount = actAccInfo.getNumberOfShares(openLogPeriod.ticker);
			//if we closed new position, must mean we have no opens..
			openLogPeriod.positionDaysOpen = closeLogPeriod.positionDaysOpen = 0;
			if (sharesInAccount > 0) {
				//close a trade!!!
				tradeTextArea.append("\n   Place Close Trade on Ticker: " + openLogPeriod.ticker);
				placeLiveTrade(openLogPeriod.ticker, false, sharesInAccount);
			} else {
				System.out.println("\nshares in account: " + sharesInAccount);
			}
		}
	}

	public class CompletedTrade {

		double aveBuyPrice = 0.0;
		double aveSellPrice = 0.0;
		double aveAccumBuyPrice = 0.0;
		double aveAccumSellPrice = 0.0;
		int daysOpen;
		int lotsUsed;
		int lotsAvail;

		public CompletedTrade(int lotSz) {
			int idx;
			lotsAvail = lotSz;
			lotsUsed = 0;
			aveBuyPrice = 0.0;
			aveSellPrice = 0.0;
		}

		List<TradeLotData> tradeLotData = new ArrayList<>();
	}
	double GlobalPlSumOfClosed = 0.0;
	double GlobalPlSumOfClosedPercent = 0.0;

	public class CompletedTradeTry1 {

		private double profitLoss = 0.0;
		private double profitLossPercent = 0.0;
		private double profitLossPercentNew = 0.0;
		private double costSumOfOpen = 0.0;
		private double costSumOfClosed = 0.0;
		private double proceedsSumOfOpen = 0.0;
		private double proceedsSumOfClosed = 0.0;
		//average of above 4..
		private double costSumOfOpenAve = 0.0;
		private double costSumOfClosedAve = 0.0;
		private double proceedsSumOfOpenAve = 0.0;
		private double proceedsSumOfClosedAve = 0.0;
		//now for stock
		private double costSumOfOpenAveStock = 0.0;
		private double costSumOfClosedAveStock = 0.0;
		private double proceedsSumOfOpenAveStock = 0.0;
		private double proceedsSumOfClosedAveStock = 0.0;
		
		private double profitLossOpen = 0.0;
		private double profitLossPercentOpen = 0.0;
		private double profitLossPercentOpenNew = 0.0;
		private int maxOutstandingBuys;
		private int maxOutstandingSells;
		private int buyIdx = 0;
		private int sellIdx = 0;
		private int currentSz = 0;
		private int numOfOpenTrades = 0;
		private int numOfClosedTrades = 0;
		private int numOfTradesLeftOpen = 0;
		private String todaysDate = "";
		private String savedLastBuyDate = "";
		private double savedLastBuyPrice = 0.0;
		private double cashToSpend = 0.0;
		private double profitLossStock = 0.0;
		private double profitLossPercentStock = 0.0;
		private double proceedsSumOfOpenStock = 0.0;
		private double proceedsSumOfClosedStock = 0.0;
		private double profitLossOpenStock = 0.0;
		private double profitLossPercentOpenStock = 0.0;
		private double costSumOfOpenStock = 0.0;
		private double costSumOfClosedStock = 0.0;

		//private int daysOpen = 0;
		//private int daysClosed = 0;
		public CompletedTradeTry1(int maxBuyLots, int maxSellLots, String todaysDateIn) {
			maxOutstandingBuys = maxBuyLots;
			maxOutstandingSells = maxSellLots;
			todaysDate = todaysDateIn;
		}

		public void addBuy(double buyPrice, String buyDate) {
			TradeLotData tld;
			if (numOfOpenTrades < maxOutstandingBuys) {
				tld = new TradeLotData();
				tld.buyDate = buyDate;
				tld.buyPrice = buyPrice;
				tld.daysOpen = tradeRules.getTradingDaysBetweenDates(buyDate, todaysDate);
				tld.sharesBot = (int)(cashToSpend / maxOutstandingBuys / buyPrice);
				//tld.daysClosed = tradeRules.getTradingDaysBetweenDates(tld.buyDate, tld.sellDate);
				//tradeLotData.add(tld); 				
				buyIdx++;
				numOfOpenTrades = (buyIdx - sellIdx);
				tradeLotData.add(tld);
				currentSz = tradeLotData.size();
				savedLastBuyDate = buyDate;
				savedLastBuyPrice = buyPrice;
			}
		}

		public void addSell(double sellPrice, String sellDate) {
			TradeLotData tld;
			//get the trade from list modify to add sell data then place it back to same index..
			if (numOfOpenTrades > 0) {
				tld = new TradeLotData();
				tld = tradeLotData.get(sellIdx);
				//for day trading don't allow a buy one day then sell the next
				//instead close the last buy out at no gain/loss..
				if ((userSelIntraDayTrading == true) && (!savedLastBuyDate.substring(0, 8).equals(sellDate.substring(0, 8)))) {
					//buy/sell dates are not equal sho close the last buy at no change...
					tld.sellDate = savedLastBuyDate;
					tld.sellPrice = savedLastBuyPrice;
				} else {
					tld.sellDate = sellDate;
					tld.sellPrice = sellPrice;
				}
				//tld.daysClosed = tradeRules.getTradingDaysBetweenDates(tld.sellDate, todaysDate);
				tld.profitLoss = myUtils.roundMe(tld.sellPrice - tld.buyPrice, 2);
				tld.profitLossPercent = myUtils.roundMe((tld.profitLoss / tld.buyPrice) * 100.0, 2);
				tld.profitLossStock = myUtils.roundMe((tld.sellPrice * tld.sharesBot) - (tld.buyPrice * tld.sharesBot), 2);
				tld.profitLossPercentStock = myUtils.roundMe((tld.profitLossStock / (tld.buyPrice * tld.sharesBot)) * 100.0, 2);
				tld.daysOpen = tradeRules.getTradingDaysBetweenDates(tld.buyDate, tld.sellDate);
				profitLossStock += tld.profitLossStock;
				profitLossPercentStock += tld.profitLossPercentStock;
				tradeLotData.set(sellIdx, tld);
                sellIdx++;
                numOfOpenTrades = (buyIdx - sellIdx);
				
            }
        }
		public void setCashToSpend(double cashIn){
			cashToSpend = cashIn;
		}
        public void calcGainLoss(double todaysPrice, String todaysDate) {
            int idx = 0;
            double plsum = 0.0;
			double plsumPercent = 0.0;
            //int openTradeCount = 0;
            int completedDaysOpen = 0;
            int openDaysOpen = 0;
            String dateOnly = this.todaysDate.substring(0, 8);
            TradeLotData actLot = new TradeLotData();
            double todaysGain = 0.0;
            double overAllGain = 0.0;
			double twRClosed = 1.0;
			double twROpen = 1.0;
			double twRa = 0.0;
			double twRb = 0.0;
            String detectDateChange = (tradeLotData.size()) > 0 ? tradeLotData.get(0).buyDate.substring(0, 8) : "";
            for (idx = 0; idx < tradeLotData.size(); idx++) {
                actLot = tradeLotData.get(idx);
                // might be multiple days if single stock (intraday), so detect date change..
                if ((userSelIntraDayTrading == true) && (!actLot.buyDate.substring(0, 8).equals(detectDateChange)) && (userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true)) {
                    tradeTextArea.append("\n   today's gain: " + myUtils.roundMe(todaysGain, 2));
                    detectDateChange = actLot.buyDate.substring(0, 8);
                    overAllGain += todaysGain;
                    todaysGain = 0.0;
                } else {

                }
                //check if trade complete buy&Sell
                if ((actLot.sellDate != null) && (!actLot.sellDate.isEmpty())) {
                    //completed trade so accum pl
                    plsum += actLot.profitLoss;
					plsumPercent += actLot.profitLossPercent;
                    costSumOfClosed += actLot.buyPrice;
                    proceedsSumOfClosed += actLot.sellPrice;
					proceedsSumOfClosedStock += (actLot.sellPrice * actLot.sharesBot);
					//
					costSumOfClosedStock += (actLot.buyPrice * actLot.sharesBot);
                    numOfClosedTrades++;
                    completedDaysOpen += actLot.daysOpen;
                    if (userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) {
                        tradeTextArea.append("\n   open on: " + actLot.buyDate + " (" + actLot.buyPrice + ")" + " close on: " + actLot.sellDate + " (" + actLot.sellPrice + ")" + " (daysOpen: " + actLot.daysOpen + ", PL: " + actLot.profitLoss + ", PL%: " + actLot.profitLossPercent + ")");
                        System.out.println("\n   open on: " + actLot.buyDate + " (" + actLot.buyPrice + ")" + " close on: " + actLot.sellDate + " (" + actLot.sellPrice + ")" + " (daysOpen: " + actLot.daysOpen + ", PL: " + actLot.profitLoss + ", PL%: " + actLot.profitLossPercent + ")");
                    }
                    if (userSelIntraDayTrading == true) {
                        //accumulate today's gain..
                        todaysGain += actLot.profitLoss;
                    } else {

                    }
					//remove!! time weighted return formula
					// twr =  [(1 + pr1) X (1 + pr2) X (1 + pr3) ... (1 + prn)] - 1
					// prx = bV - eV / bV ; bV == begining value, eV == endValue
					//profitLossPercent has PL in percent so divide by 100..
					twRa = (actLot.profitLossPercent / 100.0);
					twRb = (1 + twRa);
					twRClosed *= twRb;
                } else {
                    //still open
                    costSumOfOpen += actLot.buyPrice;
                    proceedsSumOfOpen += todaysPrice;
					proceedsSumOfOpenStock += (todaysPrice * actLot.sharesBot);
					costSumOfOpenStock += (actLot.sharesBot * actLot.buyPrice);
                    numOfTradesLeftOpen++;
                    openDaysOpen += actLot.daysOpen;
                    if (userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) {
                        tradeTextArea.append("\n   open on: " + actLot.buyDate + " (" + actLot.buyPrice + ")" + " still open: " + actLot.daysOpen + " Days.");
                        System.out.println("\n   open on: " + actLot.buyDate + " (" + actLot.buyPrice + ")" + " still open: " + actLot.daysOpen + " Days.");
                    }
                }
            }
            //calculate completed trades
            profitLoss = myUtils.roundMe(plsum, 2);
            profitLossPercent = myUtils.roundMe((profitLoss / (costSumOfClosed / numOfClosedTrades)) * 100.0, 2);
			profitLossPercentNew = myUtils.roundMe(plsumPercent, 2);
            //calculate open trades (buys not closed yet)
            profitLossOpen = myUtils.roundMe((proceedsSumOfOpen - costSumOfOpen), 2);
            profitLossPercentOpen = myUtils.roundMe((profitLossOpen / costSumOfOpen) * 100.0, 2);
			
			profitLossOpenStock = myUtils.roundMe((proceedsSumOfOpenStock - costSumOfOpenStock), 2);
            profitLossPercentOpenStock = myUtils.roundMe((profitLossOpenStock / costSumOfOpenStock) * 100.0, 2);
			//calculate average cost/proceeds
			costSumOfOpenAve = (costSumOfOpen / ((numOfTradesLeftOpen > 0) ? numOfTradesLeftOpen : 1));
			costSumOfClosedAve = (costSumOfClosed / ((numOfClosedTrades > 0) ? numOfClosedTrades : 1));
			proceedsSumOfOpenAve = (proceedsSumOfOpen / ((numOfTradesLeftOpen > 0) ? numOfTradesLeftOpen : 1));
			proceedsSumOfClosedAve = (proceedsSumOfClosed / ((numOfClosedTrades > 0) ? numOfClosedTrades : 1));
			//now for stock
			costSumOfOpenAveStock = (costSumOfOpenStock / ((numOfTradesLeftOpen > 0) ? numOfTradesLeftOpen : 1));
			costSumOfClosedAveStock = (costSumOfClosedStock / ((numOfClosedTrades > 0) ? numOfClosedTrades : 1));
			proceedsSumOfOpenAveStock = (proceedsSumOfOpenStock / ((numOfTradesLeftOpen > 0) ? numOfTradesLeftOpen : 1));
			proceedsSumOfClosedAveStock = (proceedsSumOfClosedStock / ((numOfClosedTrades > 0) ? numOfClosedTrades : 1));
			//added twr (time weighted Return)
			twRClosed -= 1;
            if ((userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) && (userSelIntraDayTrading == true)) {
                tradeTextArea.append("\n   todays's gain: " + myUtils.roundMe(todaysGain, 2));
            }
            if ((userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) && (numOfClosedTrades > 0)) {
                tradeTextArea.append("\n   completed Trades: " + numOfClosedTrades + " (daysOpen: " + completedDaysOpen + ", PL: " + profitLoss + ", PL%: " + profitLossPercent + ", PLN%: " + profitLossPercentNew +  ", PLTWR%: " + myUtils.roundMe((twRClosed * 100), 2) + ")");
                System.out.println("\n   completed Trades: " + numOfClosedTrades + " (daysOpen: " + completedDaysOpen + ", PL: " + profitLoss + ", PL%: " + profitLossPercent + ")");
				tradeTextArea.append("\n   stockGainsLoss: " + profitLossStock + " Percent: " + profitLossPercentStock + " Spent: " + cashToSpend);
            }
            if ((userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) && (numOfTradesLeftOpen > 0)) {
				//time weighted return added..
				twRa = (profitLossPercentOpen / 100.0);
				twRb = (1 + twRa);
				twROpen *= twRb;
				twROpen -= 1;
                tradeTextArea.append("\n   open Trades: " + numOfTradesLeftOpen + " (daysOpen: " + openDaysOpen + ", PL: " + profitLossOpen + ", PL%: " + profitLossPercentOpen + ", PLTWR%: " + myUtils.roundMe((twROpen * 100), 2) + ")");
                System.out.println("\n   open Trades: " + numOfTradesLeftOpen + " (daysOpen: " + openDaysOpen + ", PL: " + profitLossOpen + ", PL%: " + profitLossPercentOpen + ")");
				tradeTextArea.append("\n   openStockGainsLoss: " + profitLossOpenStock + " Percent: " + profitLossPercentOpenStock);
            }
            if ((userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true) && (userSelIntraDayTrading == true)) {
                tradeTextArea.append("\n   AllDays's gain: " + myUtils.roundMe(overAllGain, 2));
            }
			//this.daysOpen = openDaysOpen;
            //this.daysClosed = daysClosed;
            //remove!!
            GlobalPlSumOfClosed += profitLoss;
            GlobalPlSumOfClosedPercent += profitLossPercent;
            System.err.println("\nGlobalPlSumOfClosed " + GlobalPlSumOfClosed + " GlobalPlSumOfClosed% " + GlobalPlSumOfClosedPercent);
        }
        private List<TradeLotData> tradeLotData = new ArrayList<>();
    }
    public class TradeLotData{
        double buyPrice;
        double sellPrice;
        String buyDate;
        String sellDate;
        double profitLoss;
        double profitLossPercent;
		double profitLossStock;
		double profitLossPercentStock;
		//double profitLossShort;
		//double profitLossPercentShort;
        int daysOpen;
		//int daysClosed;
        double tmpAveBuyPrice;
		int sharesBot;
    }
    private int findNextOccurence(String actionIn, int startHere, List<TradeRules.OccurenceData> inlist){
        int idx = startHere;
        int retIdx = -1;
        boolean found = false;
        for(idx = startHere; (idx < inlist.size()) && !found; idx++){
            if(actionIn.equals(inlist.get(idx).action)){
                found = true;
                retIdx = idx;
            }
        }
        return retIdx;
    }    
    private void processTrades(TradeRules.OccurenceLogPeriod tradeLogPeriod) {
        List<TradeRules.OccurenceData> tradeList = tradeLogPeriod.mergedOpenCloseOccurences;        
        TradeRules.OccurenceData actAction;        
        TradeRules.OccurenceData lastClose = null;
        int numOfTrades = 0;        
        int tradeIdx;
        boolean done = false;
        int actBuyOccurence;
        int actSellOccurence;
        CompletedTradeTry1 completedTrades;
        double gainLoss = 0.0;
        /*
        we have a merged list of open and close occurences, all in sequence according to date.
        cycle through occurences and open and close and keep track of gains/losses.. 
        */
        numOfTrades = tradeList.size();
        tradeLogPeriod.numOfBuyLots = userBuyLotsSz;
        tradeLogPeriod.numOfSellLots = userSellLotsSz;
        if((numOfTrades == 0) || (findNextOccurence("OPEN", 0, tradeList) == -1) /*|| (findNextOccurence("CLOSE", 0, tradeList) == -1)*/){
            return;
        }
		completedTrades = new CompletedTradeTry1(tradeLogPeriod.numOfBuyLots, tradeLogPeriod.numOfBuyLots, tradeLogPeriod.endDate);
		completedTrades.setCashToSpend(tradeLogPeriod.cashToSpend);
        for(tradeIdx = 0; (tradeIdx < numOfTrades) && (!done); tradeIdx++){
            actAction = tradeList.get(tradeIdx);
			//if action is open && the date of the open is not shared with a close date i.e open and close on same day
			//howManyMatches returns how many times the date occured in the list. it should just be one time, the OPEN.
            if((actAction.action.equals("OPEN")) && (howManyMatches(actAction.occurenceDate, tradeList) == 1)){
			//if(actAction.action.equals("OPEN")){
                //the trade is to OPEN so add to trade list
				//action is to OPEN AND with no CLOSE on that same day/bar
                completedTrades.addBuy(actAction.closePrice, actAction.occurenceDate); 
            }else if(actAction.action.equals("CLOSE")){	
				completedTrades.addSell(actAction.closePrice, actAction.occurenceDate); 										                                                                             
            }
        }                
        completedTrades.calcGainLoss(tradeLogPeriod.endPrice, tradeLogPeriod.endDate);
        System.out.println("\ndone.");
        tradeLogPeriod.accumClosedCostAve = completedTrades.costSumOfClosedAve;
        tradeLogPeriod.accumOpenCostAve = completedTrades.costSumOfOpenAve;
        
        tradeLogPeriod.accumClosedProceedesAve = completedTrades.proceedsSumOfClosedAve;
        tradeLogPeriod.accumOpenProceedesAve = completedTrades.proceedsSumOfOpenAve;
        //calculate closed gainLoss first
        tradeLogPeriod.gainLoss = myUtils.roundMe((completedTrades.profitLoss), 2);
        tradeLogPeriod.gainLossPercent = myUtils.roundMe((completedTrades.profitLossPercent), 2);
        //calculate open gainLoss next
        tradeLogPeriod.openGainLoss = myUtils.roundMe((completedTrades.profitLossOpen), 2);  
        tradeLogPeriod.openGainLossPercent = myUtils.roundMe((completedTrades.profitLossPercentOpen), 2);
        
        tradeLogPeriod.completedTradesTry1 = completedTrades;//wfs here
        //tradeLogPeriod.positionOpen = (completedTrades.numOfTradesLeftOpen > 0 ? true : false);
        tradeLogPeriod.openGainLoss = completedTrades.profitLossOpen;
        tradeLogPeriod.openGainLossPercent = completedTrades.profitLossPercentOpen;
		//added actuall money and stock so keep track..
		tradeLogPeriod.gainLossStock = myUtils.roundMe((completedTrades.profitLossStock), 2);
        tradeLogPeriod.gainLossPercentStock = myUtils.roundMe((completedTrades.profitLossPercentStock), 2);
		tradeLogPeriod.openGainLossStock = myUtils.roundMe((completedTrades.profitLossOpenStock), 2);  
        tradeLogPeriod.openGainLossPercentStock = myUtils.roundMe((completedTrades.profitLossPercentOpenStock), 2);
		tradeLogPeriod.accumClosedCostStock = myUtils.roundMe((completedTrades.costSumOfClosedStock), 2);
		tradeLogPeriod.accumOpenCostStock = myUtils.roundMe((completedTrades.costSumOfOpenStock), 2);
		tradeLogPeriod.accumClosedProceedsStock = myUtils.roundMe((completedTrades.proceedsSumOfClosedStock), 2);
		tradeLogPeriod.accumOpenProceedsStock = myUtils.roundMe((completedTrades.proceedsSumOfOpenStock), 2);
		//stock average cost /  proceeds..
		tradeLogPeriod.accumClosedCostAveStock = myUtils.roundMe((completedTrades.costSumOfClosedAveStock), 2);
		tradeLogPeriod.accumOpenCostAveStock = myUtils.roundMe((completedTrades.costSumOfOpenAveStock), 2);
		tradeLogPeriod.accumClosedProceedsAveStock = myUtils.roundMe((completedTrades.proceedsSumOfClosedAveStock), 2);
		tradeLogPeriod.accumOpenProceedsAveStock = myUtils.roundMe((completedTrades.proceedsSumOfOpenAveStock), 2);
        //tradeLogPeriod.positionDaysOpen = completedTrades.daysOpen;
		//tradeLogPeriod.positionLastCloseInDays = completedTrades.daysClosed;
    }    
    private List<TradeRules.OccurenceData> mergeOpenCloseTrades(TradeRules.OccurenceLogPeriod openLogPeriod, TradeRules.OccurenceLogPeriod closeLogPeriod) {
        /*
        mergeOpenCloseTrades: create a merged log (output) that has both open and close events in sequence by date.
        */
        List<TradeRules.OccurenceData> openList = openLogPeriod.allOccurences;
        List<TradeRules.OccurenceData> closeList = closeLogPeriod.allOccurences;
        List<TradeRules.OccurenceData> mergedOutputList = new ArrayList<>();
        TradeRules tradeRulesOuter = new TradeRules();
        TradeRules.OccurenceData actOpen;
        TradeRules.OccurenceData actClose;        
        int numOfOpenTrades = 0;
        int numOfCloseTrades = 0;
        int totalTrades = 0;
		//these are counts at the end of the merge output that were leftover
		int opensLeftOver = 0;
		int closesLeftOver = 0;
        int openIdx = 0;
		//remove!!!!!! should be 0!!!!!!
        int closeIdx = 0;
		int daysBetween = 0;
		int zeroCnt = 0;
        /*
        we have a list of open triggers, and list of close triggers.
        start with first open date, and compare with first close occurence date. 
        if negative return value, means close is before open so place the close occurence in 
        output merge list. If however it's positive then place open occurence in output merge list.
        Then move to next open occurence and compare with next close occurence. Again, if negative then close 
        occurence happened before open occurence so place close occurence in out put list, if positive place
        open occurence in output list. Repeat till all open occurences are processed. If there are still close occurences left
        then place all left over close occurences in output list.
        At the begining if either input list is empty return the non empty list and return. If both are
        empty, return list of zero size.
        */
        numOfOpenTrades = openList.size();
        numOfCloseTrades = closeList.size();
        totalTrades = numOfCloseTrades + numOfOpenTrades;
        if((numOfOpenTrades == 0) && (numOfCloseTrades > 0)){
            return closeList;            
        }else if((numOfCloseTrades == 0) && (numOfOpenTrades > 0)){
            return openList;
        }else if ((numOfCloseTrades == 0) && (numOfOpenTrades == 0)){            
            return openList; //return empty list.
        }
        actOpen = tradeRulesOuter.new OccurenceData();
        actOpen = openList.get(openIdx);
        actClose = tradeRulesOuter.new OccurenceData();
        actClose = closeList.get(closeIdx);
		
		daysBetween = tradeRules.getTradingBarsBetweenDates(actOpen.occurenceDate, actClose.occurenceDate);
		
        while ((openIdx < numOfOpenTrades) && (closeIdx < numOfCloseTrades)){                    
			if((daysBetween = tradeRules.getTradingBarsBetweenDates(actOpen.occurenceDate, actClose.occurenceDate)) < 0) {
                //negative so close date occurred before open date so put close date in output list..
                mergedOutputList.add(actClose);
                closeIdx++;
                if ((closeIdx) < numOfCloseTrades) {
                    actClose = tradeRulesOuter.new OccurenceData();
                    actClose = closeList.get(closeIdx);
                }else{
                    
                }
            }else if(daysBetween >= 0){
                //positive so open date occurred before close date so put open date in output list..
                mergedOutputList.add(actOpen);
                openIdx++;
                if ((openIdx) < numOfOpenTrades) {
                    actOpen = tradeRulesOuter.new OccurenceData();
                    actOpen = openList.get(openIdx);
                }else{
                    
                }
				if((daysBetween == 0) && (false)){
					/*
					ok added this so when open and close have same day, we put in open date (which
					we did above), but need to bump the actClose to next one so it does not get added 
					next time around; we can't have an open and close on the same day..
					*/
					closeIdx++;
					zeroCnt++;
					
					if ((closeIdx) < numOfCloseTrades) {
						actClose = tradeRulesOuter.new OccurenceData();
						actClose = closeList.get(closeIdx);
					}else{
                    
					}
					
				}else{
					
				}
            }else{
				//can only be zero..means open and close happened on same day...
				System.out.println("\nopen/close occurred same day!");
			}
        }
		System.out.println("\nopen/close occurred same day: " + zeroCnt + " times.");
        // left over closes are counted
		closesLeftOver = (numOfCloseTrades - closeIdx);	
		if(closesLeftOver < 0){
			System.err.print("\nwtf!!!");
		}
		openLogPeriod.positionLastCloseInDays = closesLeftOver;
		closeLogPeriod.positionLastCloseInDays = closesLeftOver;
		//see if we have close occurences left at end..if so append them to output list..		
        while (closeIdx < numOfCloseTrades) {
            actClose = tradeRulesOuter.new OccurenceData();
            actClose = closeList.get(closeIdx++);
            mergedOutputList.add(actClose);
        }
		if(closesLeftOver > 0){
			//means been closed for some number of bars, so means openDays must be zero
			openLogPeriod.positionDaysOpen = 0;
			closeLogPeriod.positionDaysOpen = 0; 
			opensLeftOver = 0;
		}else{
			// left over opens are counted wfs here
			opensLeftOver = (numOfOpenTrades - openIdx);
			openLogPeriod.positionDaysOpen = opensLeftOver;
			closeLogPeriod.positionDaysOpen = opensLeftOver; 
			if(opensLeftOver < 0){
				System.err.print("\nwtf!!!");
			}else {
			}			
		}       
		//see if we have open occurences left at end..if so append them to output list..
        while (openIdx < numOfOpenTrades) {
            actOpen = tradeRulesOuter.new OccurenceData();
            actOpen = openList.get(openIdx++);
            mergedOutputList.add(actOpen);
        }
		if(opensLeftOver > 0){
			//means been open for some number of bars, so means closeDays must be zero
			closesLeftOver = 0;		
			openLogPeriod.positionLastCloseInDays = 0;
			closeLogPeriod.positionLastCloseInDays = 0;
			//means position is left open
			openLogPeriod.positionOpen = true;
			closeLogPeriod.positionOpen = true; 
		}
        System.out.println("\ndoneOpenCloseMerge. Merged output list sz: " + mergedOutputList.size());
        if(totalTrades == mergedOutputList.size()){
            System.out.println("\ndoneOpenCloseMerge. Good Match!");
        }else{
            System.err.println("\ndoneOpenCloseMerge. Bad Match!");
        }
        return mergedOutputList;
    }    
    private void dateTest2(String dateSta, String dateEnd){
        long daysDiff = 0;
        daysDiff = myUtils.getDiffInDays(dateSta, dateEnd);
        System.out.println("\n diff between " + dateSta + " and " + dateEnd + " is " + daysDiff);
    }
    private void updateHistoricalLables(){
        histSzLable.setText(userSelHistSize);
        backTestSzLable.setText(userSelBackTestSize);
        barSzLable.setText(userSelBarSize);
        histDateLable.setText(userSelDate);	
		intradayTradingLabel.setVisible(userSelIntraDayTrading);
		if(userSelIntraDayTrading == true){
			//this is temporary..we will be able to trade a group of stocks intraday in the future..for now just one stock 
			userSelectedSingleStock = true;
			//if checking the box programmically..we need to run the action too..
			singleStockCheckBox.doClick();
			singleStockCheckBox.setSelected(userSelIntraDayTrading);
			intradayTradingLabel.setVisible(true);
		}
    }  
	private void updatePurchaseLimitLables(){
		
		accountLimitLable.setText(purchaseLimits.displayIt(userSelAccountLimit));
		positionLimitLable.setText(purchaseLimits.displayIt(userSelFixedDollarsPerStockLimit));
		positionLimitByStock.setText(Double.toString(userSelFixedNumberOfSharesLimit));
		equallyDivideAmongAllCheckBox.setSelected(userSelEquallyDivideAmongStocks);
	}
    private String displayResults(TradeRules.OccurenceLogPeriod logIn) {
        List<TradeRules.OccurenceData> displayList = new ArrayList<>(); 
        String showString = "";
        StringBuilder dispString = new StringBuilder();
        dispString.append("");
        String outputString = "";
        int daysActive = 0;
        switch (userSelShowOption){
            case oSHOW_ALL:
                //show all allOccurences..
                displayList = logIn.allOccurences;
                showString = "Occurence";
                break;
            case oSHOW_CURRENT:
                //show current only..wfs
                displayList = logIn.currentOccurences;
                showString = "CurrentActive";                
                break;
            case oSHOW_TRADES:
                showString = "BackTestTrades";
                ;
                break;
            case oSHOW_TRANSITIONS:
                //show transitions only...
                displayList = logIn.transitionOccurences; 
                showString = "Transition";
                break;                
        }
        if (displayList.isEmpty()) {
            return outputString;
        }
        dispString.append("\n       " + showString + " Dates:");
        for (int x = 0; x < displayList.size(); x++) {
            dispString.append("\n         " + displayList.get(x).occurenceDate);
            for (int i = 0; i < displayList.get(x).termOutputList.size(); i++) {
                dispString.append("\n           " + displayList.get(x).termOutputList.get(i));
            }
        }
        outputString = dispString.toString();
        return outputString;
    }
    private class TimeKeep {
		private String actTime;
		private String actDate;
		private String setTimeOfs;
		private String setBarSz;
		private int delayInMs;
		public TimeKeep(String timeOfsIn, String barSzIn, String dateIn, String timeIn){
			actTime = timeIn;
			actDate = dateIn;
			myUtils.SetSetDateTime("yyyyMMdd HH:mm:ss", dateIn + " " + timeIn);
			setTimeOfs = timeOfsIn;
			setBarSz = barSzIn;
			delayInMs = 0;					
			update();
		}
		public String getActiveTime(){
			return actTime;
		}
		public String getActiveDate(){
			return actDate;
		}
		public int getDelayInMs(){
			return delayInMs;
		}
		public void update(){
			if(setTimeOfs.equals("current")){
				actTime = myUtils.GetSetTime();
				actDate = myUtils.GetSetDate();
				if(setBarSz.equals("1 day")){					
					delayInMs = 0;
				}else if (setBarSz.equals("1 min")){
					//set time to add one minute
					myUtils.AddSetTime(Calendar.MINUTE, 1);
					actTime = myUtils.GetSetTime();
					delayInMs = 60000;
				}				
			}else if(setTimeOfs.equals("previous")){
				//now we need to go back a bar in time; day or minute
				if(setBarSz.equals("1 day")){
					//set current date to minus one day..
					myUtils.AddSetTime(Calendar.DATE, -1);					
					delayInMs = 0;
				}else if (setBarSz.equals("1 min")){
					//set time to minus one minute
					myUtils.AddSetTime(Calendar.MINUTE, -1);	
					delayInMs = 60000;
				}	
				actTime = myUtils.GetSetTime();
				actDate = myUtils.GetSetDate();	
			}			
		}
	}
    private class TradeTask extends Thread {

        TradeTask() {
            tradeTaskRunning = true;
        }

        @Override
        public void run(){
            int symIdx = 0;
            String actTicker; 
			int intradayTradeLoopCnt = 0;            
            double selectedIndexBuyHoldCostTotal = 0.0;
            double selectedIndexBuyHoldProceedsTotal = 0.0;
            double selectedIndexBuyHoldTotalPl = 0.0;
            double selectedIndexBuyHoldTotalPlPercent = 0.0;
            double selectedIndexAccumClosedPl = 0.0;
            double selectedIndexAccumClosedPlPercent = 0.0;
			double selectedIndexAccumClosedPlPercentPerCost = 0.0;
            double selectedIndexAccumOpenPl = 0.0;
            double selectedIndexAccumOpenPlPercent = 0.0;
			double selectedIndexAccumOpenPlPercentPerCost = 0.0;
            //new ones:
            double selectedIndexAccumAveClosedCost = 0.0;
            double selectedIndexAccumAveOpenCost = 0.0;
            double selectedIndexAccumAveClosedProceeds = 0.0;
            double selectedIndexAccumAveOpenProceeds = 0.0;            
			//stock added..
			double selectedIndexAccumClosedPlStock = 0.0;
            double selectedIndexAccumClosedPlPercentStock = 0.0;
            double selectedIndexAccumOpenPlStock = 0.0;
            double selectedIndexAccumOpenPlPercentStock = 0.0;
			double selectedIndexAccumClosedCostStock = 0.0;
			double selectedIndexAccumOpenCostStock = 0.0;
			double selectedIndexAccumClosedProceedsStock = 0.0;
			double selectedIndexAccumOpenProceedsStock = 0.0;
			//averages..
			double selectedIndexAccumClosedCostAveStock = 0.0;
			double selectedIndexAccumOpenCostAveStock = 0.0;
			double selectedIndexAccumClosedProceedsAveStock = 0.0;
			double selectedIndexAccumOpenProceedsAveStock = 0.0;
			//add buy/hold with actual stock gains
			double selectedIndexAccumBuyHoldStockPl = 0.0;
			//add total closed count and total open count so we can estimate trading cost....
            int selectedIndexAccumTotalClosedTradeCnt = 0;
			int selectedIndexAccumTotalOpenTradeCnt = 0;			
			//total winners/losers counters	
			int selectedIndexAccumTotalWinnersCnt = 0;
			int selectedIndexAccumTotalLosersCnt = 0;
			//now do for buy and hold
			int selectedIndexAccumTotalBuyHoldWinnersCnt = 0;
			int selectedIndexAccumTotalBuyHoldLosersCnt = 0;
			
            int indexOpenPositions = 0;
            int indexClosedPositions = 0;
            StockCorrections actCorrections = new StockCorrections();
            StockCorrection actCorrection = new StockCorrection();
            int idx;
            int ydx;
            //all counters..
            int correctionTerritory = 0;
            int bearTerritory = 0; 
            int newHighs0To5 = 0;
            int newHighs5To10 = 0;
            int newHighs10To15 = 0;
            int newHighsGt15 = 0;
            int allCorrectionsSz = 0;
            double currentCorrectionValue = 0.0;
            double currentValue = 0.0;
            double currentCorrectionValuePercent = 0.0;
			TimeKeep timeKeep = null;
			long timeDiffInMins = 0;	
			String currentDateTime = "";
			//double cashToSpendPerStock = 0.0;
			double acp = 0.0;
			double aop = 0.0;
			double currentStockPrice = 0.0;
			//ugly!!!
			lastTicker = "";
            if((actTickerGroup == null) || (!actTickerGroup.groupName.equals(userSelectedSymbolFile))){
                actTickerGroup = new TickerGroup();
                actTickerGroup.groupName = userSelectedSymbolFile;
            }
			if(userSelLiveTrades == true){
				//if live trading set up account info data
				tradeTextArea.append("\nsetting up account info data..start reqInfo..");
				if(actAccInfo == null){
					actAccInfo = actIbApi.new accountInfo(userSelectedAccountNumber);					
				}else{
				}
				actAccInfo.reqUpdateAccountInformation(true);
				if(userSelIntraDayTrading == true){
					//for intraday trading set to market orders..
					userMarketTrade = true;
					actTradeActivity.setTradeMode((
												(userMarketTrade == true) ? 
												TradeRequestData.TradeModes.oMarket : 
												TradeRequestData.TradeModes.oLimitAlgo)
					);
				}				
			}
			if(Debug == true){
				//6:30 pacific, 9:30 eastern start of trading day....move later so we can calculate moving averages..
				if (userSelShowOption.equals(userSelShowOption.oSHOW_CURRENT)){
					userSelTime = "7:10:00"; 
				}else if (userSelShowOption.equals(userSelShowOption.oSHOW_TRADES)){
					userSelTime = "13:15:00";
				}
				//userSelTime = "13:15:00";
				/*
				current active bar means user current bar even if it's partially complete.
				previous means use last completed bar.
				 */
				timeKeep = new TimeKeep(userSelActiveBar, userSelBarSize, userSelDate, userSelTime);
				userSelTime = timeKeep.getActiveTime();
				todaysDate = timeKeep.getActiveDate();
			} else {
				//userSelTime = "9:30:00";
				/*
				userSelTime = myUtils.GetCurrentTime("HH:mm:ss"); b=1971.76; s=1967.35 diff=-4.41 %: -.22%
				*/
				timeKeep = new TimeKeep(userSelActiveBar, userSelBarSize, userSelDate, userSelTime);
				userSelTime = timeKeep.getActiveTime();
				todaysDate = timeKeep.getActiveDate();				
			}
			
			//loop here if intra day trading is enabled...
			while((tradeTaskRunning == true ) && (userSelIntraDayTrading == true) && (userSelLiveTrades == true) ){
				forceNewHistData = true;
				actTicker = ((userSelectedSingleStock == true) ? userSelectedStockSymbol : symbolList.get(symIdx));
				openTradeLogPeriod = tradeRules.new OccurenceLogPeriod(actTicker, userSelDate, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelTime);
				openTradeLogPeriod.histDataList = actTickerGroup.getOpenHistData(actTicker);
				if ((userSelectedSingleStock == true) && (intradayTradeLoopCnt == 0)) {
					tradeTextArea.append("\nactive Ticker: " + actTicker);
				} else {
					tradeTextArea.append("\nactive Ticker(" + symIdx + "/" + symbolList.size() + "): " + actTicker);
				}
				openTradeLogPeriod.allTerms = new ArrayList<>();
				tradeRules.setTradeLogPeriod(openTradeLogPeriod);
				openTradeLogPeriod.allTerms = (processTerms(actTicker, openTerms, slopeDefs.oOPEN));
				mergeTerms(openTradeLogPeriod, slopeDefs.oOPEN);
				tradeTextArea.append(displayResults(openTradeLogPeriod));
				if (openTradeLogPeriod.allTerms.isEmpty() == true) {
					continue;
				}
				forceNewHistData = true;
				closeTradeLogPeriod = tradeRules.new OccurenceLogPeriod(actTicker, userSelDate, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelTime);
				closeTradeLogPeriod.allTerms = new ArrayList<>();
				tradeRules.setTradeLogPeriod(closeTradeLogPeriod);
				closeTradeLogPeriod.allTerms = (processTerms(actTicker, closeTerms, slopeDefs.oCLOSE));
				mergeTerms(closeTradeLogPeriod, slopeDefs.oOPEN);
				tradeTextArea.append(displayResults(closeTradeLogPeriod));
				if (closeTradeLogPeriod.allTerms.isEmpty() == true) {
					continue;
				}
				//merge open and close logs into one sequenced by date..
				openTradeLogPeriod.mergedOpenCloseOccurences = mergeOpenCloseTrades(openTradeLogPeriod, closeTradeLogPeriod);
				currentStockPrice = openTradeLogPeriod.histDataList.get(0).getClosePrice();					
				openTradeLogPeriod.liveSharesToBuy = calcSharesToBuy(openTradeLogPeriod, userSelLiveTrades);
				openTradeLogPeriod.cashToSpend = (openTradeLogPeriod.liveSharesToBuy * currentStockPrice);
				userCashAllowedTotal = (openTradeLogPeriod.cashToSpend * symbolList.size());
				processTrades(openTradeLogPeriod);
				if(userSelLiveTrades == true){
					if(Debug == true){
						processLiveFake(openTradeLogPeriod, closeTradeLogPeriod);
					}else{
						processLiveTrades(openTradeLogPeriod, closeTradeLogPeriod);
					}					
                }
				intradayTradeLoopCnt++;
				if (pausing == true) {
					tradeTextArea.append("\nPausing. Hit run/pause again to resume.");
					while (pausing == true) {
						myUtils.delay(500);
					}
					tradeTextArea.append("\nResuming. Hit run/pause again to pause.");
				}
				timeKeep.update();
				currentDateTime = myUtils.GetCurrentTime("yyyyMMdd HH:mm:ss");
				timeDiffInMins = myUtils.getDiffInMinutes(currentDateTime, userSelDate + " "+ userSelTime);
				userSelTime = timeKeep.getActiveTime();
				
				if((timeKeep.getDelayInMs() > 0) && (timeDiffInMins <= 0)){
					//we are cought up so need to delay real time
					tradeTextArea.append("\n***DelayFor: " + (timeKeep.getDelayInMs()/1000.0) + " secsonds ***");
					myUtils.delay(timeKeep.getDelayInMs());					
				}
				if(((userSelShowOption.equals(userSelShowOption.oSHOW_CURRENT)) && (userSelTime.contains("12:59"))) || 
				  ((userSelShowOption.equals(userSelShowOption.oSHOW_TRADES) == true))){
					//done!
					tradeTaskRunning = false;
					tradeTextArea.append("\n***TotalPl: " + myUtils.roundMe(debugTotalPL, 2) + " TotalPl%: " + myUtils.roundMe(debugTotalPLPercent, 2) + "***");
				}else{
					
				}
			}
			//only enter while loop if task running and NOT intraday trading..
            while ((tradeTaskRunning == true)) {                
                iteration++;
                tradeTextArea.append("\ntradeTask running.. " + iteration + " times.");
                for(symIdx = 0; (symIdx < symbolList.size() && tradeTaskRunning == true) || ((userSelectedSingleStock == true) && (tradeTaskRunning == true)); symIdx++){
                    if(pausing == true){
                        tradeTextArea.append("\nPausing. Hit run/pause again to resume.");
                        while (pausing == true){
                            myUtils.delay(500);
                        }
                        tradeTextArea.append("\nResuming. Hit run/pause again to pause.");
                    }
                    actTicker = ((userSelectedSingleStock == true) ? userSelectedStockSymbol : symbolList.get(symIdx));
                    openTradeLogPeriod = tradeRules.new OccurenceLogPeriod(actTicker, userSelDate, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelTime);  
					openTradeLogPeriod.partOfIndexOfThisSize = symbolList.size();
                    forceNewHistData = true;
					openTradeLogPeriod.histDataList = actTickerGroup.getOpenHistData(actTicker);    
					if(userSelectedSingleStock == true){
						tradeTextArea.append("\nactive Ticker: " + actTicker);
					}else{
						tradeTextArea.append("\nactive Ticker(" + symIdx + "/" + symbolList.size() + "): " + actTicker);
					}                   
                    tradeTextArea.append("\n   process OpenTerms..");
                    openTradeLogPeriod.allTerms = new ArrayList<>();
                    tradeRules.setTradeLogPeriod(openTradeLogPeriod);
                    openTradeLogPeriod.allTerms = (processTerms(actTicker, openTerms, slopeDefs.oOPEN));
                    mergeTerms(openTradeLogPeriod, slopeDefs.oOPEN);                    
                    tradeTextArea.append(displayResults(openTradeLogPeriod));                   
                    if(openTradeLogPeriod.allTerms.isEmpty() == true){
                        continue;
                    }
					forceNewHistData = true;
                    actTickerGroup.addOpenTradeLog(actTicker, openTradeLogPeriod);                                      
                    closeTradeLogPeriod = tradeRules.new OccurenceLogPeriod(actTicker, userSelDate, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelTime);
                    tradeTextArea.append("\n   process CloseTerms..");
                    closeTradeLogPeriod.allTerms = new ArrayList<>();                    
                    tradeRules.setTradeLogPeriod(closeTradeLogPeriod);
                    closeTradeLogPeriod.allTerms = (processTerms(actTicker, closeTerms, slopeDefs.oCLOSE));
                    mergeTerms(closeTradeLogPeriod, slopeDefs.oOPEN);
                    tradeTextArea.append(displayResults(closeTradeLogPeriod));
                    if (closeTradeLogPeriod.allTerms.isEmpty() == true) {
                        continue;
                    }
                    actTickerGroup.addCloseTradeLog(actTicker, closeTradeLogPeriod);
                    //merge open and close logs into one sequenced by date..
                    openTradeLogPeriod.mergedOpenCloseOccurences = mergeOpenCloseTrades(openTradeLogPeriod, closeTradeLogPeriod);                   
					currentStockPrice = openTradeLogPeriod.histDataList.get(0).getClosePrice();					
					openTradeLogPeriod.liveSharesToBuy = calcSharesToBuy(openTradeLogPeriod, userSelLiveTrades);
					openTradeLogPeriod.cashToSpend = (openTradeLogPeriod.liveSharesToBuy * currentStockPrice);
					userCashAllowedTotal = (openTradeLogPeriod.cashToSpend * symbolList.size());
                    processTrades(openTradeLogPeriod);
                    if(openTradeLogPeriod.currentOccurences.size() > 0){
                       actTickerGroup.groupCurrentOpenEquationTrueCnt++;
                       actTickerGroup.groupCurrentOpenEquationTrueTickerList.add(openTradeLogPeriod.ticker);
                    }
                    if(closeTradeLogPeriod.currentOccurences.size() > 0){
                       actTickerGroup.groupCurrentCloseEquationTrueCnt++;
                       actTickerGroup.groupCurrentClosedEquationTrueTickerList.add(closeTradeLogPeriod.ticker);
                    } 
                    if(userSelLiveTrades == true){
						if(Debug == true){
							processLiveFake(openTradeLogPeriod, closeTradeLogPeriod);
						}else{
							processLiveTrades(openTradeLogPeriod, closeTradeLogPeriod);
						}
                        //processLiveTrades(openTradeLogPeriod, closeTradeLogPeriod);
                    }                    
                    selectedIndexBuyHoldCostTotal += openTradeLogPeriod.begBtPrice;
                    selectedIndexBuyHoldProceedsTotal += openTradeLogPeriod.endBtPrice; 
                    //indexAccumClosedPl += closeTradeLogPeriod.gainLoss;
                    selectedIndexAccumClosedPl += openTradeLogPeriod.gainLoss;
                    selectedIndexAccumClosedPlPercent += openTradeLogPeriod.gainLossPercent;
					//added stock stuff..
					selectedIndexAccumClosedPlStock += openTradeLogPeriod.gainLossStock;
                    selectedIndexAccumClosedPlPercentStock += openTradeLogPeriod.gainLossPercentStock;
					selectedIndexAccumOpenPlStock += openTradeLogPeriod.openGainLossStock;
                    selectedIndexAccumOpenPlPercentStock += openTradeLogPeriod.openGainLossPercentStock;
					selectedIndexAccumClosedCostStock += openTradeLogPeriod.accumClosedCostStock;
					selectedIndexAccumOpenCostStock += openTradeLogPeriod.accumOpenCostStock;
					selectedIndexAccumClosedProceedsStock += openTradeLogPeriod.accumClosedProceedsStock;
					selectedIndexAccumOpenProceedsStock += openTradeLogPeriod.accumOpenProceedsStock;
                    /*
                    remove just for test:
                    */
                    selectedIndexAccumAveClosedCost += openTradeLogPeriod.accumClosedCostAve;
                    selectedIndexAccumAveOpenCost += openTradeLogPeriod.accumOpenCostAve;
                    selectedIndexAccumAveClosedProceeds += openTradeLogPeriod.accumClosedProceedesAve;
                    selectedIndexAccumAveOpenProceeds += openTradeLogPeriod.accumOpenProceedesAve ;
					//ave stock
					selectedIndexAccumClosedCostAveStock += openTradeLogPeriod.accumClosedCostAveStock;
                    selectedIndexAccumOpenCostAveStock += openTradeLogPeriod.accumOpenCostAveStock;
                    selectedIndexAccumClosedProceedsAveStock += openTradeLogPeriod.accumClosedProceedsAveStock;
                    selectedIndexAccumOpenProceedsAveStock += openTradeLogPeriod.accumOpenProceedsAveStock ; 
					//stock owned with buy and hold
					selectedIndexAccumBuyHoldStockPl += ((openTradeLogPeriod.buyHoldGainLossPercent / 100.0) * openTradeLogPeriod.cashToSpend);
					//count winners/losers of closed + open positions..
					if((openTradeLogPeriod.gainLoss + openTradeLogPeriod.openGainLoss) >=0.0){
						selectedIndexAccumTotalWinnersCnt ++;
					}else{
						selectedIndexAccumTotalLosersCnt ++;
					}										
					//track winners/losers for buy and hold..
					if(openTradeLogPeriod.buyHoldGainLoss >= 0){
						selectedIndexAccumTotalBuyHoldWinnersCnt++;
					}else{
						selectedIndexAccumTotalBuyHoldLosersCnt++;
					}					
					//accum open/closed total trade counts..
					if(openTradeLogPeriod.completedTradesTry1 != null){
						selectedIndexAccumTotalClosedTradeCnt += openTradeLogPeriod.completedTradesTry1.numOfClosedTrades;
						selectedIndexAccumTotalOpenTradeCnt += openTradeLogPeriod.completedTradesTry1.numOfOpenTrades;
					}
                    if(openTradeLogPeriod.positionOpen == true){
                        selectedIndexAccumOpenPl += openTradeLogPeriod.openGainLoss;
                        selectedIndexAccumOpenPlPercent += openTradeLogPeriod.openGainLossPercent;
                        actTickerGroup.groupCurrentPositionOpenCnt++;
                        actTickerGroup.groupCurrentPositionOpenTickerList.add(actTicker);                        
                    }                                                          
                    if (userSelShowOption.equals(ShowOptions.oSHOW_TRADES)) {
						tradeTextArea.append("\n   BuyHoldPeriod: " + openTradeLogPeriod.begBtDate + ", " + openTradeLogPeriod.endBtDate);
						tradeTextArea.append("\n   BuyHoldBegEndPrice: " + openTradeLogPeriod.begBtPrice + ", " + openTradeLogPeriod.endBtPrice);
                        tradeTextArea.append("\n   BuyHoldPL: " + openTradeLogPeriod.buyHoldGainLoss);
                        tradeTextArea.append("\n   BuyHoldPL%: " + openTradeLogPeriod.buyHoldGainLossPercent);
						tradeTextArea.append("\n   BuyHoldStockPL: " + ((openTradeLogPeriod.buyHoldGainLossPercent / 100.0) * openTradeLogPeriod.cashToSpend));                                               
                        tradeTextArea.append("\n   PL: " + openTradeLogPeriod.gainLoss);
                        tradeTextArea.append("\n   PL%: " + openTradeLogPeriod.gainLossPercent);  
						tradeTextArea.append("\n   TotalPlStock: " + (openTradeLogPeriod.gainLossStock + openTradeLogPeriod.openGainLossStock));
                        tradeTextArea.append("\n   TotalPlStock%: " + (openTradeLogPeriod.gainLossPercentStock + openTradeLogPeriod.openGainLossPercentStock));
                    }                    
                    if ((userSelShowOption.equals(ShowOptions.oSHOW_TRADES)) && (openTradeLogPeriod.positionOpen == true)){
                        tradeTextArea.append("\n   OpenPl: " + openTradeLogPeriod.openGainLoss);
                        tradeTextArea.append("\n   OpenPl%: " + openTradeLogPeriod.openGainLossPercent);
                        tradeTextArea.append("\n   TotalPl: " + (openTradeLogPeriod.gainLoss + openTradeLogPeriod.openGainLoss));
                        tradeTextArea.append("\n   TotalPl%: " + (openTradeLogPeriod.gainLossPercent + openTradeLogPeriod.openGainLossPercent));
						//added stock stuff..
						tradeTextArea.append("\n   OpenPlStock: " + openTradeLogPeriod.openGainLossStock);
                        tradeTextArea.append("\n   OpenPlStock%: " + openTradeLogPeriod.openGainLossPercentStock);
                        tradeTextArea.append("\n   TotalPlStock: " + (openTradeLogPeriod.gainLossStock + openTradeLogPeriod.openGainLossStock));
                        tradeTextArea.append("\n   TotalPlStock%: " + (openTradeLogPeriod.gainLossPercentStock + openTradeLogPeriod.openGainLossPercentStock));
                    } 
                    if (userSelShowOption.equals(ShowOptions.oSHOW_TRADES) && (openTradeLogPeriod.completedTradesTry1 != null)) {
                        tradeTextArea.append("\n   TradeCount(open+closed): " + (openTradeLogPeriod.completedTradesTry1.numOfClosedTrades + openTradeLogPeriod.completedTradesTry1.numOfOpenTrades));
                    }
					if(openTradeLogPeriod.completedTradesTry1 != null){
						indexOpenPositions += openTradeLogPeriod.completedTradesTry1.numOfOpenTrades;
						indexClosedPositions += openTradeLogPeriod.completedTradesTry1.numOfClosedTrades;
					}
                    if(userSelShowOption.equals(ShowOptions.oSHOW_CORRECTIONS)){
                        tradeTextArea.append(displayCorrections(actTicker));
                    }
					if(userSelectedSingleStock == true){
						tradeTaskRunning = false;
					}
                } /*for*/
				if((actAccInfo != null) && (actAccInfo.isRequestActive() == true)){
					tradeTextArea.append("\nturn accInfo turn requests off..");
					actAccInfo.reqUpdateAccountInformation(false);
				}
                tradeTaskRunning = false;
                tradeTask = null;                  
                selectedIndexBuyHoldTotalPl = myUtils.roundMe(((selectedIndexBuyHoldProceedsTotal - selectedIndexBuyHoldCostTotal)), 4);
                selectedIndexBuyHoldTotalPlPercent = myUtils.roundMe((selectedIndexBuyHoldTotalPl / selectedIndexBuyHoldCostTotal) * 100.0, 4);               
                tradeTextArea.append("\nDone. Iterations " + iteration);
                if (userSelShowOption.equals(ShowOptions.oSHOW_TRADES)) {
					selectedIndexAccumClosedPlPercentPerCost = myUtils.roundMe((selectedIndexAccumClosedPl / selectedIndexAccumAveClosedCost) * 100.0, 2);
					selectedIndexAccumOpenPlPercentPerCost = myUtils.roundMe((selectedIndexAccumOpenPl / selectedIndexAccumAveOpenCost) * 100.0, 2);
                    tradeTextArea.append("\n     closedPl: " + selectedIndexAccumClosedPl);
                    tradeTextArea.append("\n     closedPl%: " + myUtils.roundMe(selectedIndexAccumClosedPlPercent, 2) + 										 
										 " (pl/cost: " + selectedIndexAccumClosedPlPercentPerCost + " %)"
					);
                    tradeTextArea.append("\n     openPl: " + selectedIndexAccumOpenPl);
                    tradeTextArea.append("\n     openPl%: " + myUtils.roundMe(selectedIndexAccumOpenPlPercent, 2) + 										 
										 " (pl/cost: " + selectedIndexAccumOpenPlPercentPerCost + " %)"
					);
                    tradeTextArea.append("\n     totalPl: " + (selectedIndexAccumClosedPl + selectedIndexAccumOpenPl));
					tradeTextArea.append("\n     totalPl%: " + myUtils.roundMe((selectedIndexAccumClosedPlPercent + selectedIndexAccumOpenPlPercent), 2) +
										 " (pl/cost: " + myUtils.roundMe((selectedIndexAccumClosedPlPercentPerCost + selectedIndexAccumOpenPlPercentPerCost), 2) + " %)"
					);
                    tradeTextArea.append("\n     buyAllHoldPl: " + selectedIndexBuyHoldTotalPl);
                    tradeTextArea.append("\n     buyAllHoldPl%: " + myUtils.roundMe(selectedIndexBuyHoldTotalPlPercent, 2));							  										
					tradeTextArea.append("\n     buyAllHoldStockPl: " + myUtils.roundMe(selectedIndexAccumBuyHoldStockPl, 2));
					tradeTextArea.append("\n     buyAllHoldStockPl%: " + myUtils.roundMe((selectedIndexAccumBuyHoldStockPl / (userCashAllowedTotal) * 100.0), 2));
					//added stock stuff
					tradeTextArea.append("\n     closedPlStock: " + myUtils.roundMe(selectedIndexAccumClosedPlStock, 2));
                    tradeTextArea.append("\n     closedPlStock%: " + myUtils.roundMe((selectedIndexAccumClosedPlStock/((userSelectedSingleStock == true) ? openTradeLogPeriod.cashToSpend : userCashAllowedTotal)) * 100.0, 2));
					tradeTextArea.append("\n     closedPlStockAV%: " + (acp = myUtils.roundMe((selectedIndexAccumClosedPlStock/selectedIndexAccumClosedCostAveStock) * 100.0, 2)));
					tradeTextArea.append("\n     openPlStock: " + myUtils.roundMe(selectedIndexAccumOpenPlStock, 2));
                    tradeTextArea.append("\n     openPlStock%: " + myUtils.roundMe((selectedIndexAccumOpenPlStock / ((userSelectedSingleStock == true) ? openTradeLogPeriod.cashToSpend : userCashAllowedTotal)) * 100.0, 2));
					tradeTextArea.append("\n     openPlStockAV%: " + (aop = myUtils.roundMe((selectedIndexAccumOpenPlStock / selectedIndexAccumOpenCostAveStock) * 100.0, 2)));
                    tradeTextArea.append("\n     totalPlStock: " + myUtils.roundMe((selectedIndexAccumClosedPlStock + selectedIndexAccumOpenPlStock), 2));
					tradeTextArea.append("\n     totalPlStock%: " + myUtils.roundMe(((selectedIndexAccumClosedPlStock + selectedIndexAccumOpenPlStock) / ((userSelectedSingleStock == true) ? openTradeLogPeriod.cashToSpend : userCashAllowedTotal)) * 100.0, 2));
					tradeTextArea.append("\n     totalPlStockAV%: " + myUtils.roundMe((acp + aop), 2));
					tradeTextArea.append("\n     totalCompletedTrades%: " + myUtils.roundMe(selectedIndexAccumTotalClosedTradeCnt, 2));
					tradeTextArea.append("\n     totalTradesLeftOpen%: " + myUtils.roundMe(selectedIndexAccumTotalOpenTradeCnt, 2));
					tradeTextArea.append("\n     totalTradeCnt%: " + myUtils.roundMe((selectedIndexAccumTotalOpenTradeCnt + selectedIndexAccumTotalClosedTradeCnt), 2));					
					tradeTextArea.append("\n     totalWinners: " + selectedIndexAccumTotalWinnersCnt);
					tradeTextArea.append("\n     totalLosers: " + selectedIndexAccumTotalLosersCnt);					
					tradeTextArea.append("\n     totalBuyHoldWinners: " + selectedIndexAccumTotalBuyHoldWinnersCnt);
					tradeTextArea.append("\n     totalBuyHoldLoserss: " + selectedIndexAccumTotalBuyHoldLosersCnt);
                }else if (userSelShowOption.equals(ShowOptions.oSHOW_CORRECTIONS)) {                    
                    tradeTextArea.append("\n Correction Summary on Index: " + actTickerGroup.groupName + " Size: " + allStockCorrectionList.size());                                              
                    for (idx = 0; idx < (allCorrectionsSz = allStockCorrectionList.size()); idx++) {
                        actCorrections = allStockCorrectionList.get(idx);
                        //last correct is the currently open correction..get that one..
                        actCorrection = actCorrections.correctionList.get(actCorrections.correctionList.size() - 1);
                        //dont have current value so calculate it..
                        currentValue = (actCorrection.troughToCurrentValue + actCorrection.troughValue);
                        currentCorrectionValue = (currentValue - actCorrection.startCorrectionValue);
                        currentCorrectionValuePercent = (currentCorrectionValue < 0.0) ? Math.abs((currentCorrectionValue / actCorrection.startCorrectionValue) * 100.0) : 0.0;
                        if ((currentCorrectionValuePercent > 10.0) && (currentCorrectionValuePercent < 20.0)) {
                            correctionTerritory++;
                        } else if (currentCorrectionValuePercent > 20.0) {
                            bearTerritory++;
                        } else if ((actCorrections.isNewHigh) && ((actCorrections.newHighPercent >= 0) && (actCorrections.newHighPercent <= 5))) {
                            newHighs0To5++;
                        } else if ((actCorrections.isNewHigh) && ((actCorrections.newHighPercent > 5) && (actCorrections.newHighPercent <= 10))) {
                            newHighs5To10++;
                        } else if ((actCorrections.isNewHigh) && ((actCorrections.newHighPercent > 10) && (actCorrections.newHighPercent <= 15))) {
                            newHighs10To15++;
                        } else if ((actCorrections.isNewHigh) && (actCorrections.newHighPercent > 15)) {
                            newHighsGt15++;
                        }

                    }
                    tradeTextArea.append("\n     CorrectionTerritory: " + correctionTerritory + " (" + myUtils.roundMe((((double)correctionTerritory / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                    tradeTextArea.append("\n     BearTerritory:       " + bearTerritory + " (" + myUtils.roundMe((((double)bearTerritory / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                    tradeTextArea.append("\n     NewHighs0To5:        " + newHighs0To5 + " (" + myUtils.roundMe((((double)newHighs0To5 / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                    tradeTextArea.append("\n     newHighs5To10:       " + newHighs5To10 + " (" + myUtils.roundMe((((double)newHighs5To10 / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                    tradeTextArea.append("\n     newHighs10To15:      " + newHighs10To15 + " (" + myUtils.roundMe((((double)newHighs10To15 / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                    tradeTextArea.append("\n     newHigh > 15:        " + newHighsGt15 + " (" + myUtils.roundMe((((double)newHighsGt15 / (double)allCorrectionsSz) * 100.0), 2) + "%)");
                }
            }/* while */
			if (!(userSelIntraDayTrading == true)) {
				if (symbolFailedList.size() > 0) {
					tradeTextArea.append("\nSymbols that Failed:");
					for (symIdx = 0; symIdx < symbolFailedList.size(); symIdx++) {
						tradeTextArea.append("\n" + symbolFailedList.get(symIdx));
					}
				}
				tradeTextArea.append("\nCurrentPositionOpenCount:" + actTickerGroup.groupCurrentPositionOpenCnt);
				tradeTextArea.append("\nCurrentPositionClosedCount:" + (symbolList.size() - actTickerGroup.groupCurrentPositionOpenCnt));
				tradeTextArea.append("\nCurrentOpenEquationTrueCnt:" + actTickerGroup.groupCurrentOpenEquationTrueCnt);
				tradeTextArea.append("\nCurrentOpenEquationFalseCount:" + (symbolList.size() - actTickerGroup.groupCurrentOpenEquationTrueCnt));
				tradeTextArea.append("\nCurrentClosedEquationTrueCnt:" + actTickerGroup.groupCurrentCloseEquationTrueCnt);
				tradeTextArea.append("\nCurrentClosedEquationFalseCount:" + (symbolList.size() - actTickerGroup.groupCurrentCloseEquationTrueCnt));
				tradeTextArea.append("\nOpenEquationTrue:" + myUtils.roundMe(((double) actTickerGroup.groupCurrentOpenEquationTrueCnt / (double) symbolList.size() * 100.0), 2) + " %");
				tradeTextArea.append("\nCloseEquationTrue:" + myUtils.roundMe(((double) actTickerGroup.groupCurrentCloseEquationTrueCnt / (double) symbolList.size() * 100.0), 2) + " %");
				System.out.println("\nTradeTask not running.");
				String tstr;
				tstr = tradeTextArea.getText();
				System.err.println("*****************************************");
				System.out.println(tstr);
				System.err.println("*****************************************");
				allStockCorrectionList = new ArrayList<>();
			} else {

			}
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        indexComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        tradeRuleComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tradeTextArea = new javax.swing.JTextArea();
        exitButton = new javax.swing.JButton();
        runPauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        showComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        barSzLable = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        histDateLable = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        histSzLable = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        backTestSzLable = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        openWhenTextArea = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        closeWhenTextArea = new javax.swing.JTextArea();
        displayButton = new javax.swing.JButton();
        liveTradesRadioButton = new javax.swing.JRadioButton();
        activeBarComboBox = new javax.swing.JComboBox();
        activeBarLabel = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        buyLotsComboBox = new javax.swing.JComboBox();
        sellLotsComboBox = new javax.swing.JComboBox();
        singleStockCheckBox = new javax.swing.JCheckBox();
        singleStockTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        maTypeComboBox = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        selectAccountComboBox = new javax.swing.JComboBox<>();
        intradayTradingLabel = new javax.swing.JLabel();
        turnDbgOnButton = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        lable = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        accountLimitLable = new javax.swing.JLabel();
        positionLimitLable = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        positionLimitByStock = new javax.swing.JLabel();
        equallyDivideAmongAllCheckBox = new javax.swing.JCheckBox();
        equallyDivideAmongAllLable = new javax.swing.JLabel();
        trimBackTestCheckBox = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        histConfigMenuItem = new javax.swing.JMenuItem();
        tradeRulesMenuItem = new javax.swing.JMenuItem();
        optionActivityRulesMenuItem = new javax.swing.JMenuItem();
        purchaseLimitsMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        TestTradesMenuItem = new javax.swing.JMenuItem();
        accountInfoMenuItem = new javax.swing.JMenuItem();
        showMemoryMenuItem = new javax.swing.JMenuItem();
        showCorrectionsMenuItem = new javax.swing.JMenuItem();
        chartItMenuItem = new javax.swing.JMenuItem();
        showAccountsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        indexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        indexComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Select Index");

        tradeRuleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        tradeRuleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tradeRuleComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Select TradeRules:");

        tradeTextArea.setColumns(20);
        tradeTextArea.setRows(5);
        jScrollPane1.setViewportView(tradeTextArea);

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        runPauseButton.setText("Run/Pause");
        runPauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runPauseButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Show:");

        showComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AllOccurences", "TransitionsOnly", "CurrentActive", "BackTestTrades", "Corrections", "UnusualOptionVol" }));
        showComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showComboBoxActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Historical Configuration"));

        jLabel7.setText("barSz:");

        jLabel9.setText("histDate:");

        jLabel4.setText("histSz:");

        jLabel5.setText("backTestSz:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel9)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(backTestSzLable, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(histSzLable, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(histDateLable, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(barSzLable, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(barSzLable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(histDateLable, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(histSzLable, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(backTestSzLable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jLabel6.setText("OpenWhen:");

        jLabel8.setText("CloseWhen:");

        openWhenTextArea.setColumns(20);
        openWhenTextArea.setRows(5);
        jScrollPane2.setViewportView(openWhenTextArea);

        closeWhenTextArea.setColumns(20);
        closeWhenTextArea.setRows(5);
        jScrollPane3.setViewportView(closeWhenTextArea);

        displayButton.setText("Display");
        displayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayButtonActionPerformed(evt);
            }
        });

        liveTradesRadioButton.setText("LiveTrades");
        liveTradesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                liveTradesRadioButtonActionPerformed(evt);
            }
        });

        activeBarComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "current", "previous" }));
        activeBarComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeBarComboBoxActionPerformed(evt);
            }
        });

        activeBarLabel.setText("ActiveBar:");

        jLabel10.setText("buyLots:");

        jLabel11.setText("sellLots:");

        buyLotsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", " " }));
        buyLotsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buyLotsComboBoxActionPerformed(evt);
            }
        });

        sellLotsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        sellLotsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sellLotsComboBoxActionPerformed(evt);
            }
        });

        singleStockCheckBox.setText("Single Stock");
        singleStockCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleStockCheckBoxActionPerformed(evt);
            }
        });

        singleStockTextField.setText("     ");
        singleStockTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleStockTextFieldActionPerformed(evt);
            }
        });

        jLabel13.setText("SelectMaType:");

        maTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Simple", "Weighted", "Hull" }));
        maTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maTypeComboBoxActionPerformed(evt);
            }
        });

        jLabel14.setText("Select Account:");

        selectAccountComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        selectAccountComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAccountComboBoxActionPerformed(evt);
            }
        });

        intradayTradingLabel.setText("IntraDay is ON");

        turnDbgOnButton.setText("TurnDbgOn");
        turnDbgOnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                turnDbgOnButtonActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("PurchaseLimits"));

        lable.setText("Account:");

        jLabel15.setText("FixedDollarAmnt:");

        accountLimitLable.setText("                    ");

        positionLimitLable.setText("                       ");

        jLabel12.setText("FixedNumOfShares:");

        positionLimitByStock.setText("                   ");

        equallyDivideAmongAllLable.setText("EquallyDivideAmongStocks:");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lable)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(accountLimitLable)
                            .addComponent(positionLimitLable)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(positionLimitByStock)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(equallyDivideAmongAllLable)
                .addGap(18, 18, 18)
                .addComponent(equallyDivideAmongAllCheckBox)
                .addGap(63, 63, 63))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lable)
                    .addComponent(accountLimitLable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(positionLimitLable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(positionLimitByStock))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(equallyDivideAmongAllCheckBox)
                    .addComponent(equallyDivideAmongAllLable))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        trimBackTestCheckBox.setText("TrimBackTest");
        trimBackTestCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trimBackTestCheckBoxActionPerformed(evt);
            }
        });

        jCheckBox2.setText("TrimBackTest");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(singleStockCheckBox))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(singleStockTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(36, 36, 36)
                                .addComponent(jLabel3)
                                .addGap(101, 101, 101)
                                .addComponent(jLabel14))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(tradeRuleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(showComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(selectAccountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(exitButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel13)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(runPauseButton)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(stopButton)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(displayButton))
                                                .addComponent(jCheckBox2))))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(liveTradesRadioButton))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addGap(17, 17, 17)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(trimBackTestCheckBox)
                                        .addComponent(turnDbgOnButton)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(intradayTradingLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(maTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(99, 99, 99)))))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(activeBarLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(activeBarComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel10)
                                    .addGap(5, 5, 5)
                                    .addComponent(buyLotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sellLotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(38, 38, 38)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 744, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 408, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6)
                                .addComponent(jLabel8)))))
                .addGap(0, 51, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(singleStockCheckBox)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel3)
                                .addComponent(jLabel2)
                                .addComponent(jLabel14)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tradeRuleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(singleStockTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectAccountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel8)
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(exitButton)
                            .addComponent(runPauseButton)
                            .addComponent(stopButton)
                            .addComponent(displayButton)
                            .addComponent(activeBarLabel)
                            .addComponent(activeBarComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(liveTradesRadioButton))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(buyLotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel11)
                                    .addComponent(sellLotsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(intradayTradingLabel, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addGap(24, 24, 24)
                        .addComponent(trimBackTestCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(turnDbgOnButton)
                        .addGap(53, 53, 53)
                        .addComponent(jCheckBox2)
                        .addGap(41, 41, 41))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Configure");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        histConfigMenuItem.setText("Historical Paramters");
        histConfigMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histConfigMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(histConfigMenuItem);

        tradeRulesMenuItem.setText("TradeRules");
        tradeRulesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tradeRulesMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(tradeRulesMenuItem);

        optionActivityRulesMenuItem.setText("OptionActivityRules");
        optionActivityRulesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionActivityRulesMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(optionActivityRulesMenuItem);

        purchaseLimitsMenuItem.setText("Purchase Limits");
        purchaseLimitsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                purchaseLimitsMenuItemActionPerformed(evt);
            }
        });
        jMenu2.add(purchaseLimitsMenuItem);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("TestStuff");

        TestTradesMenuItem.setText("Trades");
        TestTradesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TestTradesMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(TestTradesMenuItem);

        accountInfoMenuItem.setText("AcccountInfo");
        accountInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountInfoMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(accountInfoMenuItem);

        showMemoryMenuItem.setText("ShowMemory");
        showMemoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showMemoryMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(showMemoryMenuItem);

        showCorrectionsMenuItem.setText("ShowCorrections");
        showCorrectionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCorrectionsMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(showCorrectionsMenuItem);

        chartItMenuItem.setText("chartIt");
        chartItMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartItMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(chartItMenuItem);

        showAccountsMenuItem.setText("ShowAccounts");
        showAccountsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAccountsMenuItemActionPerformed(evt);
            }
        });
        jMenu3.add(showAccountsMenuItem);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    TradeTask tradeTask = null;
    boolean pausing = false;
    private void runPauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runPauseButtonActionPerformed
        // TODO add your handling code here:
        if (tradeTask == null) {
            tradeTask = new TradeTask();
            tradeTextArea.append("\nCreated new task.");
            tradeTask.start();
            tradeTaskRunning = true;
            pausing = false;
        }else if ((tradeTaskRunning == true) && (pausing == false)){            
            pausing = !pausing;            
        }else if ((tradeTaskRunning == true) && (pausing == true)){            
            pausing = !pausing;
        }
        
    }//GEN-LAST:event_runPauseButtonActionPerformed
    TradeRulesDialogForm actTraderRulesDialog = null;
    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // TODO add your handling code here:
        if (actTradeActivity != null) {
            actTradeActivity.stopIt();
            myUtils.delay(200);
            if (actTradeActivity.haveWeStoppedRunning() == true) {
                System.out.println("TradeActivity has stopped.");
            } else {
                System.out.println("TradeActivity has NOT stopped.");
            }
            actTradeActivity = null;
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed
    private void updateTradeRuleComboBox(){
        rulesFilesList = tradeRules.getTradeRulesFiles();
        System.out.println("\nrulesDirList size " + rulesFilesList.size());
        tradeRuleComboBox.removeAllItems();
        tradeRulesInputIgnore = true;        
        for (int i = 0; i < rulesFilesList.size(); i++){
            tradeRuleComboBox.addItem(rulesFilesList.get(i));
        }
        tradeRulesInputIgnore = false;
    }
    private void tradeRuleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tradeRuleComboBoxActionPerformed
        // TODO add your handling code here:
        userSelectedTradeRule = (String) tradeRuleComboBox.getSelectedItem();
        if((tradeRulesInputIgnore == true) || (userSelectedTradeRule == null)){
            return;
        }
        openTerms.clear();
        closeTerms.clear();
        tradeRules.loadTradeRulesFile(userSelectedTradeRule);
        openTerms = tradeRules.getOpenTerms();
        closeTerms = tradeRules.getClosedTerms();
        System.out.println("\nactiveTerms size = " + activeTerms.size());
        openWhenTextArea.setText("");
        openWhenTextArea.append("\nOpen When: ");
        for(int i = 0; i < openTerms.size(); i++){
            openWhenTextArea.append("\n  " + openTerms.get(i).getDescription() + " " + openTerms.get(i).getAndOrSelItem());
        }
        //tradeTextArea.append("\n  Trigger: " + tradeRules.getEquationDesciption("Open"));
        closeWhenTextArea.append("\n\nClose When: ");
        for(int i = 0; i < closeTerms.size(); i++){
            closeWhenTextArea.append("\n  " + closeTerms.get(i).getDescription() + " " + closeTerms.get(i).getAndOrSelItem());
        }
        //tradeTextArea.append("\n  MaType: " + tradeRules.getMaType());
    }//GEN-LAST:event_tradeRuleComboBoxActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        // TODO add your handling code here:
        tradeTaskRunning = false;
        tradeTextArea.append("\nstopped tradeThread.");
        tradeTask = null;
    }//GEN-LAST:event_stopButtonActionPerformed

    private void indexComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexComboBoxActionPerformed
        // TODO add your handling code here:
        userSelectedSymbolFile = (String)indexComboBox.getSelectedItem();
        System.out.println("\nselectedIndex = " + userSelectedSymbolFile);
        IOTextFiles ioTextFile = new IOTextFiles();
        String strA;
        tradeTextArea.append("\nreading " + userSelectedSymbolFile + " file.");
        symbolList.removeAll(symbolList);
        userRdSymbolTextFile = ioTextFile.new ioRdTextFiles(userSelectedSymbolFile, false);
        for (int xIdx = 0; ((strA = userRdSymbolTextFile.read(false)) != null); xIdx++) {
                symbolList.add(strA);
        }
        tradeTextArea.append("\ndone. " + symbolList.size() + " Symbols read.");
        forceNewHistData = true;
    }//GEN-LAST:event_indexComboBoxActionPerformed

    private void histConfigMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histConfigMenuItemActionPerformed
        // TODO add your handling code here:
        //historyConfigDialog = new HistoryConfigDialog(new javax.swing.JFrame(), true, userSelHistSize, userSelBackTestSize, userSelBarSize, userSelDate);
        historyConfigDialog.setVisible(true);
        userSelHistSize = historyConfigDialog.getHistSize();
        userSelBackTestSize = historyConfigDialog.getBackTestSize();
        userSelBarSize = historyConfigDialog.getHistBarSize();
        userSelDate = historyConfigDialog.getBackTestDate();
		userSelIntraDayTrading = historyConfig.getUserSelIntraDayTrade();
        updateHistoricalLables();
        System.out.println("\nhistWindowMenuItemAction");
    }//GEN-LAST:event_histConfigMenuItemActionPerformed

    private void tradeRulesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tradeRulesMenuItemActionPerformed
        // TODO add your handling code here:
        actTraderRulesDialog = new TradeRulesDialogForm(new javax.swing.JFrame(), true);
        actTraderRulesDialog.setVisible(true);
        System.out.println("\ntradeRulesMenuItemActionPerformed exiting...");
        //update tradeRules in case we added file..
        updateTradeRuleComboBox();
    }//GEN-LAST:event_tradeRulesMenuItemActionPerformed
    
    private void showComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showComboBoxActionPerformed
        // TODO add your handling code here:
        userSelShowOption = ShowOptions.valueOfOrd(showComboBox.getSelectedItem().toString());
        System.out.println("\nshowComboBoxActionPerformed: " + userSelShowOption);
    }//GEN-LAST:event_showComboBoxActionPerformed

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void displayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayButtonActionPerformed
        // TODO add your handling code here:
        StringBuilder outStr = new StringBuilder();
        List<TradeRules.OccurenceData> displayList = new ArrayList<>();
        int listSz = 0;
        int count = 0;
        int idx;
        int idy;
        int idz;
        String showString = "";
        String ttarea = "";
        for (idx = 0; idx < actTickerGroup.openTradeLogList.size(); idx++) {
            switch (userSelShowOption) {
                case oSHOW_ALL:
                    //show all allOccurences..
                    displayList = actTickerGroup.openTradeLogList.get(idx).allOccurences;
                    showString = "Occurence";
                    break;
                case oSHOW_CURRENT:
                    //show current only..
                    displayList = actTickerGroup.openTradeLogList.get(idx).currentOccurences;
                    showString = "Current";
                    break;
                case oSHOW_TRADES:
                    showString = "Trades";
                    ;
                    break;
                case oSHOW_TRANSITIONS:
                    //show transitions only...
                    displayList = actTickerGroup.openTradeLogList.get(idx).transitionOccurences;
                    showString = "Transition";
                    break;
            }
            listSz = displayList.size();
            for (idy = 0; idy < listSz; idy++){
                if (displayList.isEmpty()) {
                    continue;
                }
                count++;
                outStr.append("\n    Ticker(" + idx + "/" + actTickerGroup.openTradeLogList.size() + "):" + actTickerGroup.openTradeLogList.get(idx).ticker);
                outStr.append("\n         " + showString + " Dates:");
                outStr.append("\n           " + displayList.get(idy).occurenceDate);
                for ( idz = 0; idz < displayList.get(idy).termOutputList.size(); idz++) {
                    outStr.append("\n       " + displayList.get(idy).termOutputList.get(idz));
                }
            }
        }
        outStr.append("\nTotal Occurences: ").append(Integer.toString(count));
        commonGui.postToTextAreaMsg("Display Area", outStr.toString());
        //tradeTextArea.setText("");
        //tradeTextArea.append(outStr.toString());        
    }//GEN-LAST:event_displayButtonActionPerformed
    TestTradesDialogForm testTradeDialogForm;
    private void TestTradesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TestTradesMenuItemActionPerformed
        // TODO add your handling code here:
        testTradeDialogForm = new TestTradesDialogForm(new javax.swing.JFrame(), true);
        testTradeDialogForm.setVisible(true);
    }//GEN-LAST:event_TestTradesMenuItemActionPerformed
   
    //String userSelectedAccount = "DU218372";
    ibApi actIbApi = ibApi.getActApi();
    ibApi.accountInfo actAccInfo = null;        
    private void accountInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountInfoMenuItemActionPerformed
        int idx = 0;
        
        // TODO add your handling code here:
        actAccInfo = actIbApi.new accountInfo(userSelectedAccountNumber);
        actIbApi.setActAccountInfo(actAccInfo);
        actAccInfo.reqUpdateAccountInformation(true);
        myUtils.delay(2000);
        actAccInfo.reqUpdateAccountInformation(false);
        System.out.println("\nportfolioSize = " + actAccInfo.getPortfolioListSize());
        for(idx = 0; idx < actAccInfo.getPortfolioListSize(); idx++){
            if (actAccInfo.getPortfolioPosition(idx)> 0){
                System.out.println("\nticker: " + actAccInfo.getPortfolioContractSymbol(idx) + " #shares: " + actAccInfo.getPortfolioPosition(idx));
            }
        }
    }//GEN-LAST:event_accountInfoMenuItemActionPerformed
	private ibApi.accountInfo getAccountInfo(String tickerIn, String accountIn){
		int idx;
		int numOfShares = 0;
		boolean found = false;
		tickerIn = tickerIn.toUpperCase();
		if (actAccInfo == null) {
			actAccInfo = actIbApi.new accountInfo(accountIn/*userSelectedAccount.substring(0, userSelectedAccount.indexOf("("))*/);
		}
		actIbApi.setActAccountInfo(actAccInfo);
		actAccInfo.reqUpdateAccountInformation(true);
		myUtils.delay(2000);
		actAccInfo.reqUpdateAccountInformation(false);
		System.out.println("\nportfolioSize = " + actAccInfo.getPortfolioListSize());
		for (idx = 0; ((idx < actAccInfo.getPortfolioListSize()) && !found); idx++) {
			if ((tickerIn.equals(actAccInfo.getPortfolioContractSymbol(idx))) && (actAccInfo.getPortfolioPosition(idx) > 0)) {
				System.out.println("\nticker: " + actAccInfo.getPortfolioContractSymbol(idx) + " #shares: " + actAccInfo.getPortfolioPosition(idx));
				numOfShares = actAccInfo.getPortfolioPosition(idx);
				found = true;
			}
		}
		return actAccInfo;
	}
	public class accountInfoData{
		public accountInfoData(String accountNumIn) {			
			if (actAccInfo == null) {
				actAccInfo = actIbApi.new accountInfo(accountNumIn);
			}
			actIbApi.setActAccountInfo(actAccInfo);
			actAccInfo.reqUpdateAccountInformation(true);
			myUtils.delay(2000);
			actAccInfo.reqUpdateAccountInformation(false);
			System.out.println("\nportfolioSize = " + actAccInfo.getPortfolioListSize());
		}
		public int getNumOfShares(String tickerIn){
			int idx;
			int numOfShares = 0;
			tickerIn = tickerIn.toUpperCase();
			boolean found = false;
			if (actAccInfo == null) {
				tradeTextArea.append("\nactAccInfo == null!");
				return -1;
			}
			for (idx = 0; ((idx < actAccInfo.getPortfolioListSize()) && !found); idx++) {
				if ((tickerIn.equals(actAccInfo.getPortfolioContractSymbol(idx))) && (actAccInfo.getPortfolioPosition(idx) > 0)) {
					System.out.println("\nticker: " + actAccInfo.getPortfolioContractSymbol(idx) + " #shares: " + actAccInfo.getPortfolioPosition(idx));
					numOfShares = actAccInfo.getPortfolioPosition(idx);
					found = true;
				}
			}
			return numOfShares;
		}
		public double getAvailCash(String tickerIn){
			availCashInAccount = actAccInfo.getCashVal();
			return (availCashInAccount);
		}
	}
	private int accountInfoGetNumOfShares(String tickerIn, String accountIn){
        int idx;
        int numOfShares = 0;
        boolean found = false;
		tickerIn = tickerIn.toUpperCase();
        if(actAccInfo == null){
            actAccInfo = actIbApi.new accountInfo(accountIn);
        }
        actIbApi.setActAccountInfo(actAccInfo);
        actAccInfo.reqUpdateAccountInformation(true);
        myUtils.delay(2000);
        actAccInfo.reqUpdateAccountInformation(false);
        System.out.println("\nportfolioSize = " + actAccInfo.getPortfolioListSize());
        for(idx = 0; ((idx < actAccInfo.getPortfolioListSize()) && !found); idx++){
            if ((tickerIn.equals(actAccInfo.getPortfolioContractSymbol(idx))) && (actAccInfo.getPortfolioPosition(idx) > 0)){
                System.out.println("\nticker: " + actAccInfo.getPortfolioContractSymbol(idx) + " #shares: " + actAccInfo.getPortfolioPosition(idx));
                numOfShares = actAccInfo.getPortfolioPosition(idx);
                found = true;
            }
        }
        return numOfShares;
    }
    private double accountInfoGetAvailCash(){
        actAccInfo.reqUpdateAccountInformation(true);
        myUtils.delay(2000);
        actAccInfo.reqUpdateAccountInformation(false);
        System.out.println("\nuserSelectedAccountNumber: " + userSelectedAccountNumber);
        //tradeTextArea.append("\nUserSelectedAccount: " + userSelectedAccountNumber);
        System.out.println("\nAvail Cash: " + actAccInfo.getCashVal());
        //tradeTextArea.append("\nAvail Cash: " + actAccInfo.getCashVal());
        availCashInAccount = actAccInfo.getCashVal();
        return (availCashInAccount);
    }
    private void liveTradesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_liveTradesRadioButtonActionPerformed
        // TODO add your handling code here:
        userSelLiveTrades = liveTradesRadioButton.isSelected();
        System.out.println("\nuserSelLiveTrades: " + userSelLiveTrades);
        if(userSelLiveTrades == true){
            activeBarComboBox.setEnabled(true);
            activeBarLabel.setEnabled(true);           
			userSelActiveBar = (String) activeBarComboBox.getSelectedItem();			
			System.out.println("\nuserSelActiveBar: " + userSelActiveBar);
			tradeTextArea.append("\nActive Bar: " + userSelActiveBar);
        }else{
            activeBarComboBox.setEnabled(false);
            activeBarLabel.setEnabled(false);
        }
    }//GEN-LAST:event_liveTradesRadioButtonActionPerformed

    private void showMemoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showMemoryMenuItemActionPerformed
        // TODO add your handling code here:
        commonGui.postInformationMsg("MemoryAvail (bytes):" + Runtime.getRuntime().freeMemory());
    }//GEN-LAST:event_showMemoryMenuItemActionPerformed
    
    String displayCorrections(String tickerIn){
        String retStr = "";
        StringBuilder dispString = new StringBuilder();
        List<ibApi.historicalData.data> hdList;
        int idx;
        hdList = actTickerGroup.getOpenHistData(tickerIn);
        StockCorrections allCorrections = new StockCorrections();
        StockCorrection actCorrection = new StockCorrection();
        System.out.println("\nhdListSz: " + hdList.size());
        if(hdList.size() == 0){
            return "";
        }
        allCorrections = findCorrections(tickerIn, hdList, 5);
        //dispString.append("\n\nStock: " + tickerIn);
        dispString.append("\n   Corrections:");
        dispString.append("\n      StartDate     TroughDate     EndDate     Correction");
        for (idx = 0; idx < allCorrections.correctionList.size(); idx++){
            actCorrection = allCorrections.correctionList.get(idx);
            if(actCorrection.correctionComplete == true){
                dispString.append("\n    " + actCorrection.startCorrectionDate + "    " + actCorrection.troughDate + "    " + actCorrection.endCorrectionDate + "    " + actCorrection.correctionPercent);
            }else{
                dispString.append("\n   Correction In Progress:");
                dispString.append("\n      StartDate   TroughDate Correction   Recovered   ToGo   NewHigh");                
                dispString.append("\n    " + actCorrection.startCorrectionDate + "    " + actCorrection.troughDate + "    " +  actCorrection.correctionPercent + "      " + actCorrection.troughToCurrentPercent + "            " + actCorrection.startOfCorrectionToCurrentPercent);
                if(allCorrections.isNewHigh == true){
                    dispString.append("        " + allCorrections.newHighPercent);
                }else{
                    dispString.append("     --");
                }
                //dispString.append("\n");
            }            
        }
		dispString.append("\n   AveCorrection%: " + allCorrections.aveCorrectionPercent);
		dispString.append("\n   CurrentlyWithin: " + allCorrections.currentlyWithinAveCorrectionPercent + "% of Ave Correction.");
		if(allCorrections.correctionList.size() > 0){
			double correctionTroughToCurrentPercent = allCorrections.correctionList.get(allCorrections.correctionList.size() - 1).troughToCurrentPercent;
			if((allCorrections.aveCorrectionPercent > 0.0) && (Math.abs(allCorrections.currentlyWithinAveCorrectionPercent) <= 2.0) && (Math.abs(correctionTroughToCurrentPercent) <= 2.0)){
				dispString.append("\n   !!WithIn2%!!");
			}
		}				
		dispString.append("\n");
        allStockCorrectionList.add(allCorrections);
        return dispString.toString();
    }
    private void showCorrectionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCorrectionsMenuItemActionPerformed
        // TODO add your handling code here:
        String userTicker;
        List<ibApi.historicalData.data> hdList;
        StockCorrections allCorrections = new StockCorrections();
        userTicker = commonGui.getUserInput("Enter Ticker:", "").toUpperCase();
        hdList = actTickerGroup.getOpenHistData(userTicker);
        System.out.println("\nhdListSz: " + hdList.size());
        allCorrections = findCorrections(userTicker, hdList, 5);
    }//GEN-LAST:event_showCorrectionsMenuItemActionPerformed

    private void optionActivityRulesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionActivityRulesMenuItemActionPerformed
        // TODO add your handling code here:
        int optionVolumeToExceed;
        optionVolumeToExceed = Integer.valueOf(commonGui.getUserInput("Volume To Exceed:", "")); 
        System.out.println("\noptionVolumeToExceed: " + optionVolumeToExceed);
    }//GEN-LAST:event_optionActivityRulesMenuItemActionPerformed

    private void activeBarComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeBarComboBoxActionPerformed
        // TODO add your handling code here:
		userSelActiveBar = (String) activeBarComboBox.getSelectedItem();
		System.out.println("\nuserSelActiveBar: " + userSelActiveBar);
		tradeTextArea.append("\nActive Bar: " + userSelActiveBar);
    }//GEN-LAST:event_activeBarComboBoxActionPerformed
    
    private void buyLotsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buyLotsComboBoxActionPerformed
        // TODO add your handling code here:
        if(buyLotsComboBox.getSelectedItem() != null){
            userBuyLotsSz = Integer.valueOf((String)buyLotsComboBox.getSelectedItem());
            System.out.println("\nuserBuyLotsSz: " + userBuyLotsSz);
        }
    }//GEN-LAST:event_buyLotsComboBoxActionPerformed

    private void sellLotsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sellLotsComboBoxActionPerformed
        // TODO add your handling code here:
        if(sellLotsComboBox.getSelectedItem() != null){
            userSellLotsSz = Integer.valueOf((String)sellLotsComboBox.getSelectedItem());
            System.out.println("\nuserSellLotsSz: " + userSellLotsSz);
        }
    }//GEN-LAST:event_sellLotsComboBoxActionPerformed
    
    public void chartIt(String tickerIn, List<ibApi.historicalData.data> hdListIn){
        ChartItJDialog chartIt;
        XYSeries stockSeries;
        XYSeries rsiMaSeries;
        XYSeries rsiSeries;
        XYSeries rsi70Series;
        XYSeries rsi30Series;
        XYDataset xyDatasetStock;
        XYDataset xyDatasetRsiMa;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;
        String actTicker = "";
        float actTick = 0;
        double stockValue = 0.0;
        double rsiValue = 0.0;
        double rsiMa200Value = 0.0;
        int idx = 0;
        int staChartLoc = 0;
        double curClose = 0.0;
        double curRsiMa = 0.0;
        double curRsi = 0.0;
        double curRsi30 = 30.0;
        double curRsi70 = 70.0;
        int hdsz = hdListIn.size();
        if(hdListIn.isEmpty()){
            return;
        }
        stockSeries = new XYSeries("Stock Chart");
        rsiSeries = new XYSeries("Rsi");
        rsiMaSeries = new XYSeries("Rsi Ma");
        rsi70Series = new XYSeries("Rsi 70");
        rsi30Series = new XYSeries("Rsi 30");
        dataset = new XYSeriesCollection();
        dataset.addSeries(stockSeries);
        dataset.addSeries(rsiSeries);
        dataset.addSeries(rsiMaSeries);
        dataset.addSeries(rsi30Series);
        dataset.addSeries(rsi70Series);
//      xyDatasetStock = new XYSeriesCollection(stockSeries);
//      xyDatasetOption = new XYSeriesCollection(optionSeries);

        chart = ChartFactory.createXYLineChart("Stock vs Rsi vs RsiMa", "Time(Days)", "Position Value", dataset, PlotOrientation.VERTICAL, true, true, false);
        frame1 = new ChartFrame(tickerIn + " Chart", chart, true);
        //ChartPanel chartPanel = new ChartPanel(chart);
        //this.getContentPane().add(chartPanel);
        frame1.setVisible(true);
        frame1.setSize(900, 600);
        //find first valid rsiMa..
        for (idx = (hdListIn.size() - 1); idx > 0; idx--) {
            if (hdListIn.get(idx).getRsiMa() > 0) {
                break;
            }
        }
        staChartLoc = idx;
        //cycle from oldest date to present..
        for (idx = staChartLoc; idx >= 0; idx--) {
            curClose = hdListIn.get(idx).getClosePrice();
            curRsi = hdListIn.get(idx).getRsi();
            curRsiMa = hdListIn.get(idx).getRsiMa(); 
            curRsi30 = curRsiMa - 20;
            curRsi70 = curRsiMa + 20;
            stockSeries.addOrUpdate(actTick, curClose);
            rsiMaSeries.addOrUpdate(actTick, curRsiMa);
            rsiSeries.addOrUpdate(actTick, curRsi);    
            rsi30Series.addOrUpdate(actTick, curRsi30);
            rsi70Series.addOrUpdate(actTick, curRsi70);
            actTick += 1;
        }
        
    }
    private void chartItMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chartItMenuItemActionPerformed
        // TODO add your handling code here:
        
        String userTicker;
        List<ibApi.historicalData.data> hdList;
        userTicker = commonGui.getUserInput("Enter Ticker:", "").toUpperCase();
        hdList = actTickerGroup.getOpenHistData(userTicker);
        System.out.println("\nhdListSz: " + hdList.size());
        chartIt(userTicker, hdList);

        //ChartItJDialog chartIt = new ChartItJDialog(new javax.swing.JFrame(), true);
        //chartIt.setActHistData(hdList);
        //chartIt.setVisible(true);

    }//GEN-LAST:event_chartItMenuItemActionPerformed

    private void singleStockCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleStockCheckBoxActionPerformed
        // TODO add your handling code here:
		if(userSelectedSingleStock = singleStockCheckBox.isSelected() == true){
			indexComboBox.setEnabled(false);
			singleStockTextField.setEnabled(true);
			userSelectedStockSymbol = commonGui.getUserInput("Enter Symbol", "aapl");
			System.out.println("\nSymbol enterred: " + userSelectedStockSymbol);			
			singleStockTextField.setText(userSelectedStockSymbol);
			forceNewHistData = true;
		}else{
			indexComboBox.setEnabled(true);
			userSelectedStockSymbol = "";
			singleStockTextField.setEnabled(false);
		}
		
    }//GEN-LAST:event_singleStockCheckBoxActionPerformed

    private void singleStockTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleStockTextFieldActionPerformed
        // TODO add your handling code here:
		if(userSelectedSingleStock == true){
			userSelectedStockSymbol = singleStockTextField.getText();
			System.out.println("\nuserSelectedStockSymbol: " + userSelectedStockSymbol);
		}else{
			commonGui.postInformationMsg("Select single stock first!");
		}
    }//GEN-LAST:event_singleStockTextFieldActionPerformed

    private void maTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maTypeComboBoxActionPerformed
        // TODO add your handling code here:
		userSelectedMaType = (String)maTypeComboBox.getSelectedItem();
		System.out.println("\nuserSelectedMaType is: " + userSelectedMaType);
		tradeTextArea.append("\nMovingAveType is: " + userSelectedMaType);
		forceNewHistData = true;
    }//GEN-LAST:event_maTypeComboBoxActionPerformed

    private void showAccountsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAccountsMenuItemActionPerformed
        // TODO add your handling code here:
		String accNumber = "";
        String accAlias = "";        
        actManagedAcccounts = ManagedAccounts.getAllAccounts();        
        //accountsComboBox.removeAll();
		tradeTextArea.append("\nDisplaying All Accounts");
        for(int idx = 0; idx < actManagedAcccounts.getNumOfAccounts(); idx++){ 
            accNumber = actManagedAcccounts.getAnAccount(idx).getName();
            accAlias = actManagedAcccounts.getAliasFromAccount(accNumber);  
			tradeTextArea.append("\n  Account Number: " + accNumber);
			tradeTextArea.append("\n  Account Alias: " + accAlias);
			
            //accountsComboBox.addItem(accNumber + "(" + accAlias + ")");
        }    
	tradeTextArea.append("\nDone.");
    }//GEN-LAST:event_showAccountsMenuItemActionPerformed

    private void selectAccountComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAccountComboBoxActionPerformed
        // TODO add your handling code here:		
		userSelectedAccountNumber = (String)selectAccountComboBox.getSelectedItem();
		if(userSelectedAccountNumber != null){
			//remove all characters after ( open bracket..which is any alias..
			userSelectedAccountNumber = userSelectedAccountNumber.substring(0, userSelectedAccountNumber.indexOf("("));
			actAccInfo = actIbApi.new accountInfo(userSelectedAccountNumber);
			actIbApi.setActAccountInfo(actAccInfo);
			actAccInfo.reqUpdateAccountInformation(true);
			myUtils.delay(2000);
			actAccInfo.reqUpdateAccountInformation(false);
			System.out.println("\nuserSelectedAccountNumber: " + userSelectedAccountNumber); 
			tradeTextArea.append("\nUserSelectedAccount: " + userSelectedAccountNumber);
			System.out.println("\nAvail Cash: " + actAccInfo.getCashVal());
			tradeTextArea.append("\nAvail Cash: " + actAccInfo.getCashVal());
			System.out.println("\nStock Value: " + actAccInfo.getStockVal());
			tradeTextArea.append("\nStock Value: " + actAccInfo.getStockVal());
			System.out.println("\nUnrealizedPL: " + actAccInfo.getCurrentUnrealizedPandLVal());
			tradeTextArea.append("\nUnrealizedPL: " + actAccInfo.getCurrentUnrealizedPandLVal());
			tradeTextArea.append("\nMoneyInvested: " + (actAccInfo.getStockVal() - actAccInfo.getCurrentUnrealizedPandLVal()));			
			actTradeActivity.setAccountNumber(userSelectedAccountNumber);
                        availCashInAccount = actAccInfo.getCashVal();
		}		
    }//GEN-LAST:event_selectAccountComboBoxActionPerformed

    private void turnDbgOnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_turnDbgOnButtonActionPerformed
        // TODO add your handling code here:
		Debug = turnDbgOnButton.isSelected();
		System.out.println("\nDebug: " + Debug);
			
    }//GEN-LAST:event_turnDbgOnButtonActionPerformed

    private void purchaseLimitsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_purchaseLimitsMenuItemActionPerformed
        // TODO add your handling code here:
		purchaseLimitDialog.setVisible(true);		
		userSelAccountLimit = purchaseLimits.getAccountLimit();
		userSelFixedDollarsPerStockLimit = purchaseLimits.getPositionDollarAmountLimit();
		userSelFixedNumberOfSharesLimit = purchaseLimits.getPositionFixedNumOfSharesLimit();
		userSelEquallyDivideAmongStocks = purchaseLimits.getDivideEquallAmongAll();
		updatePurchaseLimitLables();
    }//GEN-LAST:event_purchaseLimitsMenuItemActionPerformed

    private void trimBackTestCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trimBackTestCheckBoxActionPerformed
        // TODO add your handling code here:
		trimBackTest = trimBackTestCheckBox.isSelected();
		System.out.println("\nTrimBackTest: " + trimBackTest);
    }//GEN-LAST:event_trimBackTestCheckBoxActionPerformed

    private StockCorrections findCorrections(String tickerIn, List<ibApi.historicalData.data> hdataIn, double correctionPercentIn){
    /*
     starting with the oldest data, keep hi and low water mark on closing prices. 
     A correction is defined as followes:
        whenever a previous high water mark is reached a second time, after the lower water mark went down 5% from first high water mark, 
        a correction has occured. At this time record neccessary data of correcton and generate a list entry.
     every time closing price makes new high bump up the high water mark, at the same time clear the low water mark
     if the current price is lower than high water mark and higher than the low water mark, do nothing. 
     if current price is lower than high water mark and lower than low water mark, update low water mark.
      
    */
        StockCorrections allCorrections = new StockCorrections();
        StockCorrection actCorrection = new StockCorrection();
        int x;
        double hiWater = 0.0;
        double loWater = 0.0;
        double curClose = 0.0;
        double startClose = 0.0;
		double correctionSumPercent = 0.0;
        boolean enCorrection = false;
        int hdsz = hdataIn.size();
        if(hdataIn.isEmpty()){
            return null;
        }
		if(tickerIn.equals("UTX")){
			System.out.println("\nUTX");
		}
        //setup initial condition..
        hiWater = startClose = loWater = curClose = hdataIn.get(hdsz - 1).getClosePrice();
        allCorrections.stockTicker = tickerIn;
        //cycle from oldest date to present..
        for (x = (hdataIn.size() - 1); x >= 0; x--){
            curClose = hdataIn.get(x).getClosePrice();            
            if(curClose > hiWater){
                if(enCorrection == true){
                    //correction happenned so trigger an entery...
                    allCorrections.numOfCorrections++;
                    enCorrection = false;                   
                    actCorrection.endCorrectionValue = curClose;
                    actCorrection.endCorrectionDate = hdataIn.get(x).getDate();
                    actCorrection.correction = myUtils.roundMe((actCorrection.startCorrectionValue - actCorrection.troughValue), 2);
                    actCorrection.correctionPercent = myUtils.roundMe((actCorrection.correction / actCorrection.startCorrectionValue) * 100.0, 2);
                    actCorrection.correctionComplete = true;
					correctionSumPercent += actCorrection.correctionPercent;
                    allCorrections.correctionList.add(actCorrection);
                    actCorrection = new StockCorrection();                    
                }
                hiWater = curClose;
                loWater = hiWater;
                actCorrection.startCorrectionValue = curClose;
                actCorrection.startCorrectionDate = hdataIn.get(x).getDate();
            }else if(curClose < loWater){
                loWater = curClose;
                //check if glorified correction as needs to be larger than correctionPercentIn
                enCorrection = ((hiWater - loWater) / hiWater) > (correctionPercentIn / 100.0);
                actCorrection.troughValue = curClose;
                actCorrection.troughDate = hdataIn.get(x).getDate();
            }
        }
        //put in info on correction in progress..
        actCorrection.correction = myUtils.roundMe((actCorrection.startCorrectionValue - actCorrection.troughValue), 2);
        actCorrection.correctionPercent = myUtils.roundMe(((actCorrection.correction / actCorrection.startCorrectionValue) * 100.0) , 2);
        actCorrection.correctionComplete = false;
        //the following only make sense to compute when a correction is in progress..
        actCorrection.troughToCurrentValue = (curClose - actCorrection.troughValue);
        actCorrection.troughToCurrentPercent = myUtils.roundMe(((actCorrection.troughToCurrentValue / actCorrection.troughValue) * 100.0), 2);
        actCorrection.startOfCorrectionToCurrentValue = (actCorrection.startCorrectionValue - curClose);
        actCorrection.startOfCorrectionToCurrentPercent = myUtils.roundMe(((actCorrection.startOfCorrectionToCurrentValue / actCorrection.startCorrectionValue) * 100.0), 2);
        allCorrections.correctionList.add(actCorrection);
        
        int sz = allCorrections.numOfCorrections - 1;
        double lastHigh;
        if(sz >= 0){
            //get last complete correction's (size minus one) endCorrectionValue (end of complete correction)..
            lastHigh = allCorrections.correctionList.get(sz).endCorrectionValue;
        }else{
            //no complete corrections, so use starting close..
            lastHigh = startClose;
        }
        //compare current incomplete correction's start value to last completed correction to see if new high happening..
        if((actCorrection.startCorrectionValue > lastHigh) && (enCorrection == false)){
            //new high happening..
            allCorrections.isNewHigh = true;
            allCorrections.newHigh = actCorrection.startCorrectionValue;
            allCorrections.newHighPercent = myUtils.roundMe(((allCorrections.newHigh - lastHigh) / lastHigh) * 100.0, 2);
        }
        if(allCorrections.numOfCorrections >= 3){
			allCorrections.aveCorrectionPercent =  myUtils.roundMe((correctionSumPercent / (double)allCorrections.numOfCorrections), 2);
			allCorrections.currentlyWithinAveCorrectionPercent = myUtils.roundMe((allCorrections.aveCorrectionPercent - actCorrection.startOfCorrectionToCurrentPercent), 2);
		}
        return allCorrections;
    }
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
            java.util.logging.Logger.getLogger(NewTraderTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewTraderTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewTraderTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewTraderTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                NewTraderTest dialog = new NewTraderTest(new javax.swing.JFrame(), true);
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
    private javax.swing.JMenuItem TestTradesMenuItem;
    private javax.swing.JMenuItem accountInfoMenuItem;
    private javax.swing.JLabel accountLimitLable;
    private javax.swing.JComboBox activeBarComboBox;
    private javax.swing.JLabel activeBarLabel;
    private javax.swing.JLabel backTestSzLable;
    private javax.swing.JLabel barSzLable;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox buyLotsComboBox;
    private javax.swing.JMenuItem chartItMenuItem;
    private javax.swing.JTextArea closeWhenTextArea;
    private javax.swing.JButton displayButton;
    private javax.swing.JCheckBox equallyDivideAmongAllCheckBox;
    private javax.swing.JLabel equallyDivideAmongAllLable;
    private javax.swing.JButton exitButton;
    private javax.swing.JMenuItem histConfigMenuItem;
    private javax.swing.JLabel histDateLable;
    private javax.swing.JLabel histSzLable;
    private javax.swing.JComboBox indexComboBox;
    private javax.swing.JLabel intradayTradingLabel;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lable;
    private javax.swing.JRadioButton liveTradesRadioButton;
    private javax.swing.JComboBox<String> maTypeComboBox;
    private javax.swing.JTextArea openWhenTextArea;
    private javax.swing.JMenuItem optionActivityRulesMenuItem;
    private javax.swing.JLabel positionLimitByStock;
    private javax.swing.JLabel positionLimitLable;
    private javax.swing.JMenuItem purchaseLimitsMenuItem;
    private javax.swing.JButton runPauseButton;
    private javax.swing.JComboBox<String> selectAccountComboBox;
    private javax.swing.JComboBox sellLotsComboBox;
    private javax.swing.JMenuItem showAccountsMenuItem;
    private javax.swing.JComboBox showComboBox;
    private javax.swing.JMenuItem showCorrectionsMenuItem;
    private javax.swing.JMenuItem showMemoryMenuItem;
    private javax.swing.JCheckBox singleStockCheckBox;
    private javax.swing.JTextField singleStockTextField;
    private javax.swing.JButton stopButton;
    private javax.swing.JComboBox tradeRuleComboBox;
    private javax.swing.JMenuItem tradeRulesMenuItem;
    private javax.swing.JTextArea tradeTextArea;
    private javax.swing.JCheckBox trimBackTestCheckBox;
    private javax.swing.JCheckBox turnDbgOnButton;
    // End of variables declaration//GEN-END:variables
}
