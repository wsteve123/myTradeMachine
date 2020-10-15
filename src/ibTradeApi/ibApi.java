/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ibTradeApi;

/**
 *
 * @author earlie87
 */

import ibApiSrc.Contract;
import ibApiSrc.ContractDetails;
import ibApiSrc.EClientSocket;
import ibApiSrc.EWrapper;
import ibApiSrc.EWrapperMsgGenerator;
import ibApiSrc.Execution;
import ibApiSrc.Order;
import ibApiSrc.OrderState;
import ibApiSrc.TickType;
import ibApiSrc.UnderComp;
import ibTradeApi.ibApi.historicalData.data;
import java.math.*;
import java.security.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.JFrame;
import tradeMenus.displayStocksWthFilter;
import tradeMenus.slopeDefs;
import positions.*;
import tradeMenus.slopeAnalysis.*;
//import tradeMenus.displayStocksWthFilter.BullBearCross;
import tradeMenus.slopeAnalysis;
import tradeMenus.traderConfigParams;

public class ibApi extends JFrame implements EWrapper {
    public final static int BID           = 0x0001;
    public final static int LAST          = 0x0002;
    public final static int ASK           = 0x0004;
    public final static int OPEN          = 0x0008;
    public final static int PREV_CLOSE    = 0x0010;
    public final static int VOLUME        = 0x0020;
    public final static int DELTA         = 0x0040;
    public final static int OPEN_INTEREST = 0x0080;
    public final static int VALUE         = 0x0100;
    public final static int ALLDONE       = 0x00C5;                    //  = 0x01ff;
    final static String ALL_GENERIC_TICK_TAGS = "100,101,104,105,106,107,165,221,225,233,236,258,293,294,295,318";
    final static boolean GET_SNAP_SHOT = true;
    final static int NOT_FOUND = 9999;
    final static boolean API_MSGS_ON = false;
    final static boolean IBAPI_TEST_ON = false;
    final static boolean CHAIN_ON = false;
    final static boolean CHAIN_REPEAT = false;
    final static boolean CHAIN_ORDER_DISPLAY = false;
    final static boolean ORDER_BASIC_ON = false;
    final static boolean QUOTE_ORDER_TEST_1 = false;
    private static final int NOT_AN_FA_ACCOUNT_ERROR = 321;
    private int faErrorCodes[] = {503, 504, 505, 522, 1100, NOT_AN_FA_ACCOUNT_ERROR};
    final static int MKT_DEPTH_DATA_RESET = 317;
    final static int NO_HISTORICAL_DATA_FOUND = 366;
    private boolean faError;
    private boolean m_disconnectInProgress = false;
    private EClientSocket m_client = new EClientSocket(this);

    private static Contract m_contract = new Contract();
    final static int MAX_STREAMS = 2000; //750;
    public quoteInfo xQuote = new quoteInfo();
    private static quoteInfo[] quoteStreams;
    
    private static int qsIdx = 0;
    private static int tickId = 0;
    private boolean isTickSnapshotEnd = false;

    private static Semaphore apiSem = new Semaphore(1);
    private static Semaphore qSem = new Semaphore(1);
    private Semaphore wrapperSem = new Semaphore(1);
    public OptionChain schain = new OptionChain();
    public OptionChain actChain = new OptionChain();
    public orderInfo testOrder = new orderInfo();
    //public historicalData.HistorySlope actHistorySlope = new historicalData.HistorySlope();
    private static ibApi actApi;
    private accountInfo actAccountInfo = new accountInfo("DU123604");
    public ibApiConnectivitySimple apiAlarms = new ibApiConnectivitySimple();
    public historicalData actHistoricalData = null;
    public int nextValidId = -1;
    public int nextValudIdTO = 100;
    //wfs added 7/13/12
    private String[] managedAccounts;
    public ManagedAccounts activeAccounts = null;
    //these are used for analizeData..
    int numOfPositions;
    int actPosition;
    PosManager positions[];
    int overShootPercent = 0;
    int overShootShares = 0;
    int backTestSharesPerTrade = 0;    
    int realSharesPerTrade = 0;
    int realSharesPerTradeOverShoot = 0;
    boolean initialized = false;
    boolean linearMode = false;
    boolean limitRiskOn = false;
    boolean recommendOverShootPercent = false;
    traderConfigParams actTraderConfig = null;
    boolean ibThrottleMsgFlow = false;
    private void setManagedAccounts(String accountList){
        /*
        ManagedAccounts = accountList.split(",");
        System.out.println("Number Of Managed Accounts: "+ManagedAccounts.length);
        for(int i = 0 ; i < ManagedAccounts.length; i++) {
            System.out.println(" "+i+" "+ ManagedAccounts[i]);
        }
        System.out.println();
        * 
        */
        if(activeAccounts == null){
            System.out.println();
            activeAccounts = new ManagedAccounts(accountList, false);
            System.out.println("number of managed accounts is -----> " + activeAccounts.numOf());
            
        }
        
    }
    public class PortfolioInfo{
        //portfolio info
        Contract portContract;
        int portPosition;
        double portMarketPrice;
        double portMarketValue;
        double portAveCost;
        double portUnrealizedPNL;
        double portRealizedPNL;
        String portAccountName;
        
    }
    public class accountInfo {
        String accountNumber = null;
        String time = null;
        boolean reqActive = false;
        //balances
        double netLiquidationValue;
        double cash;
        //avail for trading
        double currentAvailFunds;
        double currentExcessLiquidity;
        double specialMemorandomAccount;
        double buyingPower;
        //market value
        double totalCash;
        double stock;
        double option;
        double netLiqValue; // addition of last 3
        double currentUnrealizedPandL;
        double currentRealizedPandL;
        
        //portfolio info
        Contract portContract;
        int portPosition;
        double portMarketPrice;
        double portMarketValue;
        double portAveCost;
        double portUnrealizedPNL;
        double portRealizedPNL;
        String portAccountName;
        
        List<PortfolioInfo> portfolioInfoList = new ArrayList<PortfolioInfo>();
        private int updates = 0;
        private void updatePortfolioInfo(Contract contract, int position, double marketPrice, 
                                         double marketValue, double averageCost, double unrelaizedPNL, 
                                         double realizedPNL, String accountName){
            PortfolioInfo actPortfolio = new PortfolioInfo();
            System.out.println("updatePortfolioInformation: ticker + " + contract.m_symbol);
            actPortfolio.portContract = contract;
            actPortfolio.portPosition = position;
            actPortfolio.portMarketPrice = myUtils.roundMe(marketPrice, 2);
            actPortfolio.portMarketValue = myUtils.roundMe(marketValue, 2);
            actPortfolio.portAveCost = myUtils.roundMe(averageCost, 2);
            actPortfolio.portUnrealizedPNL = myUtils.roundMe(unrelaizedPNL, 2);
            actPortfolio.portRealizedPNL = myUtils.roundMe(realizedPNL, 2);
            actPortfolio.portAccountName = accountName;
            addReplace(portfolioInfoList, actPortfolio);                            
        }
        public int getUpdateCnt(){
            return updates;
        }
        private void addReplace(List<PortfolioInfo> listin, PortfolioInfo newpin){
            int x = 0;
            boolean found = false;
            int replaceCnt = 0;
            PortfolioInfo y = new PortfolioInfo();
            for(x = 0; x < listin.size(); x++){
                y = listin.get(x);
                if(y.portContract.equals(newpin.portContract)){
                    Collections.replaceAll(listin, y, newpin);                    
                    found = true;
                    replaceCnt++;
                }
            }
            //if not found add to last...
            if(found == false){
                listin.add(newpin);
            }else{
                //System.out.println("\nreplaceCnt = " + replaceCnt);
            }
        }
        private void updateAccountTime(String time){ 
            /*
            if(!actAccountInfo.time.equals(time)){
               updates++; 
            } 
            */
            if((actAccountInfo.time != null) && (!actAccountInfo.time.equals(time))){
                updates++;
            }
            actAccountInfo.time = time;
        }
        public String getAccountTime(){
            return actAccountInfo.time;
        }
        public int getPortfolioListSize(){
            return portfolioInfoList.size();
        }
        public PortfolioInfo getPortfolio(int byidx){
            return portfolioInfoList.get(byidx);
        }
        public Contract getPortfolioContract(int byidx){
            return portfolioInfoList.get(byidx).portContract;
        }
        public int getPortfolioPosition(int byidx){
            return portfolioInfoList.get(byidx).portPosition;
        }
        public double getPortfolioMarketPrice(int byidx){
            return portfolioInfoList.get(byidx).portMarketPrice;
        }
        public double getPortfolioMarketValue(int byidx){
            return portfolioInfoList.get(byidx).portMarketValue;
        }
        public double getPortfolioAveCost(int byidx){
            double retval = 0.0;
            if(portfolioInfoList.get(byidx).portContract.m_secType.equals("OPT")){
                retval = myUtils.roundMe((portfolioInfoList.get(byidx).portAveCost / Integer.valueOf(portfolioInfoList.get(byidx).portContract.m_multiplier)), 2);
            }else{
                retval = portfolioInfoList.get(byidx).portAveCost;
            }
            return retval;
        }
        public double getPortfolioUnrealizedPNL(int byidx){
            return portfolioInfoList.get(byidx).portUnrealizedPNL;
        }
        public double getPortfolioRealizedPNL(int byidx){
            return portfolioInfoList.get(byidx).portRealizedPNL;
        }
        public String getPortfolioAccountName(int byidx){
            return portfolioInfoList.get(byidx).portAccountName;
        }
        public String getPortfolioContractSymbol(int byidx){
            return portfolioInfoList.get(byidx).portContract.m_symbol;
        }
        public String getPortfolioOptionName(int byidx){
            String str1 = "";
            str1 = portfolioInfoList.get(byidx).portContract.m_symbol;
            str1 += " ";
            str1 += portfolioInfoList.get(byidx).portContract.m_expiry;
            str1 += " ";
            str1 += portfolioInfoList.get(byidx).portContract.m_strike;
            str1 += " ";
            str1 += portfolioInfoList.get(byidx).portContract.m_right;
            return str1;
        }
        public String getPortfolioOptionSymbol(int byidx){
            return portfolioInfoList.get(byidx).portContract.m_localSymbol;
        }
        public Contract getPortContract(){
            return portContract;                   
        }
        public int getPortPosition(){
            return portPosition;                   
        }
        public double getPortMarketPrice(){
            return portMarketPrice;                   
        }
        public double getPortMarketValue(){
            return portMarketValue;                   
        }
        public double getPortAveCost(){
            return portAveCost;                   
        }
        public double getPortUnrealizedPNL(){
            return portUnrealizedPNL;                   
        }
        public double getPortRealizedPNL(){
            return portRealizedPNL;                   
        }
        public String getPortName(){
            return portAccountName;                   
        }
        
        public accountInfo(String accNum) {
            accountNumber = accNum;
            
        }
        
        /*
         * This is called to start/stop the account updates messages
         */
        public void reqUpdateAccountInformation(Boolean turnOn) {
            
            if((turnOn == true) & (reqActive == false)) {
                m_client.reqAccountUpdates(true, accountNumber);     
                reqActive = true;
            }else if ((turnOn == false) && (reqActive == true)) {
                m_client.reqAccountUpdates(false, accountNumber);     
                reqActive = false;
            }
            
        }
        /*
         * This routine is called when updates come in from IB to update the fields.
         */
        private void updateAccountInformation(String key, String value, String currency, String accountName) {
        
			System.out.println("updateAccountInformation: key is " + key);
        
            if (key.equals("AvailableFunds")) {
                actAccountInfo.currentAvailFunds = myUtils.roundMe(Double.parseDouble(value),2);
            } else if (key.equals("CashBalance")) {
                actAccountInfo.cash = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("FullAvailableFunds")) {
                actAccountInfo.currentAvailFunds = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("NetLiquidation")) {
                actAccountInfo.netLiquidationValue = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("OptionMarketValue")) {
                actAccountInfo.option = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("RealizedPnL")) {
                actAccountInfo.currentRealizedPandL = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("StockMarketValue")) {
                actAccountInfo.stock = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("TotalCashBalance")) {
                actAccountInfo.totalCash = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("UnrealizedPnL")) {
                actAccountInfo.currentUnrealizedPandL = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("ExcessLiquidity")) {
                actAccountInfo.currentExcessLiquidity = myUtils.roundMe(Double.parseDouble(value),2);
            }else if (key.equals("BuyingPower")) {
                actAccountInfo.buyingPower = myUtils.roundMe(Double.parseDouble(value),2);
            }
            actAccountInfo.netLiqValue = myUtils.roundMe(actAccountInfo.totalCash + actAccountInfo.stock + actAccountInfo.option, 2);           

        }
        public String getAccountNumber(){
            return accountNumber;
        }
        public double getNetLiqVal() {
            return(netLiquidationValue);
        }
        public double getCashVal() {
            return (cash);
        }
        public double getCurrentAvailFundsVal() {
            return(currentAvailFunds);
        }
        public double getCurrentExcessLiquidityVal() {
            return(currentExcessLiquidity);
        }
        public double getSpecialMemorandomAccountVal() {
            return(specialMemorandomAccount);
        }
        public double getBuyingPowerVal() {
            return(buyingPower);
        }
        public double getTotalCashVal() {
            return(totalCash);
        }
        public double getStockVal() {
            return(stock);
        }
        public double getOptionVal() {
            return(option);
        }
        public double getNetLiqValueTotal() {
            return(netLiqValue);
        }
        public double getCurrentUnrealizedPandLVal() {
            return(currentUnrealizedPandL);
        }
        public double getCurrentRealizedPandLVal() {
            return(currentRealizedPandL);
        }
        
        public void setAccount(String acc){
            accountNumber = acc;
        }
        public void clrList(){
            portfolioInfoList.clear();
        }
       public boolean isRequestActive(){
		   return reqActive;
	   } 
	   public int getNumberOfShares(String tickerIn){
			tickerIn = tickerIn.toUpperCase();
			int numOfShares = 0;
			boolean found = false;
			int idx = 0;
			for (idx = 0; ((idx < this.getPortfolioListSize()) && !found); idx++) {
				if ((tickerIn.equals(this.getPortfolioContractSymbol(idx))) && (this.getPortfolioPosition(idx) > 0)) {
					System.out.println("\nticker: " + this.getPortfolioContractSymbol(idx) + " #shares: " + this.getPortfolioPosition(idx));
					numOfShares = this.getPortfolioPosition(idx);
					found = true;
				}
			}
			return numOfShares;
		}
        
    }
    
    public void setActAccountInfo(accountInfo aai) {
        actAccountInfo = aai;
    }
    public accountInfo getActAccountInfo() {
        return actAccountInfo;
    }
    public Wally w = new Wally(3);
    public boolean ibConnectError = false;
    public class Wally {

        int intWally;

        public Wally(int in) {
            intWally = in;
        }
    }

    public void setActApi(ibApi thisOne) {
        actApi = thisOne;
    }
    public static ibApi getActApi() {
        return actApi;
    }
    public orderInfo getActTrade() {
        return testOrder;
    }
    public OptionChain getActOptionChain() {
        return actChain;
    }
    
    public historicalData.HistorySlope getActHistorySlope() {
        return actHistoricalData.actWally;
    }
    public void setActOptionChain(OptionChain ch) {
        actChain = ch;
    }
    public void setActOrder(orderInfo ord) {
        testOrder = ord;
    }
    public static void semTake() {
        apiSem.acquire();
    }

    public static void semGive() {
        apiSem.release();
    }
    
    public static class quoteInfo {
        public int respBits;
        public int tickId;
        public boolean optionQuote;
        public double bid;
        public double last;
        public double ask;
        public double open;
        public double prevClose;
        public int volume;
        public String underlying;
        public String symbol;
        public double strikePrice;
        public String description;
        public String cpType;
        public double delta;
        public int cOpenInterest;
        public int pOpenInterest;
        public String contractMonth;
        public String optionDate;
        public String optionSymbol;
        public double value;
        public double impliedVolatility;
        public boolean errorGettingQuote;
        public int orderNum;
        public int tickerUsed;
        public boolean enStreaming;
        public int updateCnt;
        public int errorCodeFromApi;
        public String errorMsgFromApi;
    }
    public static class balanceInfo {
            /* start balances */
            public int bAccountId;
            public float stockBuyingPower;
            public float optionBuyingPower;
            /* cash balance */
            public float cashBalanceInitial;
            public float cashBalanceCurrent;
            public float cashBalanceChange;

             /* margin balance */
            public float marginBalanceInitial;
            public float marginBalanceCurrent;
            public float marginBalanceChange;

            /* long stock value */
            public float longStockValueInitial;
            public float longStockValueCurrent;
            public float longStockValueChange;

            /* long stock value */
            public float shortStockValueInitial;
            public float shortStockValueCurrent;
            public float shortStockValueChange;

            /* long option value */
            public float longOptionValueInitial;
            public float longOptionValueCurrent;
            public float longOptionValueChange;
            /* short option value */
            public float shortOptionValueInitial;
            public float shortOptionValueCurrent;
            public float shortOptionValueChange;
            /* account value */
            public float accountValueInitial;
            public float accountValueCurrent;
            public float accountValueChange;

            public float availFundsForTrading;
        /* end balance */
    }

    public static class posInfo {

        public int quantity;
        public String symbol;
        public String description;
        public float closePrice;
        public String posType;
        public float averagePrice;
        public float currentValue;
    }

    public static class balancePositionInfo {
        public long acountId;
        public balanceInfo balanceInformation = new balanceInfo();
        public Vector stocks = new Vector(1, 1);
        public Vector options = new Vector(1, 1);
        private int stockIdx = 0;
        private int optionIdx = 0;
        private boolean errorFetchingData = false;

        void addStock(posInfo info) {
            stocks.add(stockIdx, (posInfo) info);
        }

        void addOption(posInfo info) {
            options.add(optionIdx, (posInfo) info);
        }

        posInfo getStock(int ix) {

            if (ix == 0) {
                return ((posInfo) stocks.get(0));
            } else if ((ix > 0) && (ix < stocks.size())) {
                return ((posInfo) stocks.get(ix));
            } else {
                return (null);
            }

        }
        posInfo getOption(int ix) {

            if (ix == 0) {
                return ((posInfo) options.get(0));
            } else if ((ix > 0) && (ix < options.size())) {
                return ((posInfo) options.get(ix));
            } else {
                return (null);
            }

        }
        int numOfOptionPositions() {
            return(optionIdx);
        }
        int numOfStockPositions() {
            return(stockIdx);
        }

    }

    public static balancePositionInfo getBalanceAndPositions(String account) {
        balancePositionInfo b = new balancePositionInfo();
        return (b);
    }

    public double roundMe(double roundme, int places) {
        //double r = 3.1537;
        double retme;

        BigDecimal bd = new BigDecimal(roundme);
        bd = bd.setScale(places, BigDecimal.ROUND_UP);
        retme = bd.doubleValue();
        return (retme);
    }

    private static int getStream(String sym, boolean option) {

        for (int ix = 0; (ix < qsIdx) && (ix < MAX_STREAMS); ix++) {

            if ((quoteStreams[ix] != null) && (quoteStreams[ix].symbol.equals(sym)) && (quoteStreams[ix].optionQuote == option)) {

                return (ix);
            }

        }

        return (NOT_FOUND);
    }

    public static class optionParams{
        public static String symP;
        public static double strikeP;
        public static String cOrPV;
        public static String ymdV;
    }
    public static optionParams convertOptionParams(String optionSymbol) {
        optionParams retParms = new optionParams();
        if (getOptionParams(optionSymbol) == true) {
            optionParams.symP = symbol;
            optionParams.strikeP = strike;
            optionParams.ymdV = ymd;
            optionParams.cOrPV = cOrP;
        }else {
            retParms = null;
        }
        return (retParms);
    }
    private String expiring = "20120316"; //full year/month/day
    private static double strike = 400;
    private static String cOrP = "C";      /* needs to be upper cap! */

    private static String symbol = "aapl";
    private static String ymd = "";
    private static String tmp = "";
    private static String testStr = "aapl  120317C00375000";
                          //         012345678901234567890
    private static Boolean getOptionParams(String sym) {
        Boolean retVal = false;
        int tmpInt;
        double tmpDouble;
        
        //sym = testStr;
//sym = "PFE   120121C00010000";
        symbol = sym.substring(0, 5);
        symbol = symbol.trim();

        tmp = sym.substring(6,12);
        
        ymd = "20" + sym.substring(6, 12);        
        
        tmpInt = (Integer.parseInt(ymd) - 1);
      
        ymd = Integer.toString(tmpInt);
        
        //ymd = "20120120";
        cOrP = sym.substring(12, 13);
        testStr = sym.substring(13, 18);
        strike = Double.parseDouble(testStr);
        testStr = sym.substring(18, 20);
        tmpDouble = Double.parseDouble(testStr) / 100.0;
        strike = strike + tmpDouble;


        return (retVal);
    }
    public static boolean keepAlive(){
        return(true);
    }
    public static void loginIn(){
        
    }
    public static boolean loginIn(int tryme){

        return(true);
    }
    public static void loginIn(boolean yes){

    }
    public static void logOut(boolean yes){

    }
    public static void logOut(){

    }
    public static boolean getOperationError() {
        return(false);
    }
    private boolean openQuote(String sym, boolean option) {
        boolean retValue = false;
        //qSem.acquire();
        if (getStream(sym, option) == NOT_FOUND) {

            m_contract = new Contract();
            if (option) {
                getOptionParams(sym);
                m_contract.m_symbol = symbol;

                m_contract.m_expiry = ymd;
                m_contract.m_strike = strike;
                m_contract.m_secType = "OPT";
                m_contract.m_exchange = "SMART";
                m_contract.m_multiplier = "100";

                m_contract.m_currency = "USD";
                m_contract.m_right = cOrP;

                quoteStreams[qsIdx] = new quoteInfo();
                quoteStreams[qsIdx].tickId = tickId;
                quoteStreams[qsIdx].symbol = sym;
                quoteStreams[qsIdx].optionQuote = true;
                quoteStreams[qsIdx].cpType = cOrP;
                quoteStreams[qsIdx].strikePrice = strike;
                quoteStreams[qsIdx].optionDate = ymd;
            } else {

                m_contract.m_symbol = sym;
                m_contract.m_secType = "STK";
                m_contract.m_strike = 0;
                m_contract.m_exchange = "SMART";
                m_contract.m_primaryExch = "ISLAND";
                m_contract.m_currency = "USD";

                quoteStreams[qsIdx] = new quoteInfo();
                quoteStreams[qsIdx].tickId = tickId;
                quoteStreams[qsIdx].symbol = sym;
                quoteStreams[qsIdx].optionQuote = false;
            }

            qsIdx++;
            if (qsIdx < MAX_STREAMS) {

                m_client.reqMktData(tickId++, m_contract, ALL_GENERIC_TICK_TAGS, !GET_SNAP_SHOT);
                retValue = true;
            } else {
                System.out.println("Create Stream: too many streams!!");
            }


        }
        //qSem.release();
        return retValue;

    }

    public quoteInfo getQuote(String sym, boolean option) {
        int ix = 0;
        int timeOut = 10;
        quoteInfo retQuote;
        qSem.acquire();
        if ((ix = getStream(sym, option)) == NOT_FOUND) {
            openQuote(sym, option);
            System.out.println("getQuote: " + sym + " not found in Streams. Added it..");
            qSem.release();
            delay(2000);
            qSem.acquire();
        }

        if ((ix = getStream(sym, option)) != NOT_FOUND) {
            while ((quoteStreams[ix].updateCnt == 0) && (timeOut > 0)){
                qSem.release();
                delay(100);
                qSem.acquire();
                timeOut--;
            }
            if (timeOut == 0){
                System.out.println("getQuote: timed out waiting for stream update.");
            }
            retQuote = new quoteInfo();
            retQuote.tickId = quoteStreams[ix].tickId;
            retQuote.optionQuote = quoteStreams[ix].optionQuote;
            retQuote.bid = quoteStreams[ix].bid;
            retQuote.last = quoteStreams[ix].last;
            retQuote.ask = quoteStreams[ix].ask;
            retQuote.open = quoteStreams[ix].open;
            retQuote.prevClose = quoteStreams[ix].prevClose;
            retQuote.volume = quoteStreams[ix].volume;
            retQuote.pOpenInterest = quoteStreams[ix].pOpenInterest;
            retQuote.cOpenInterest = quoteStreams[ix].cOpenInterest;


            retQuote.delta = quoteStreams[ix].delta;
            retQuote.underlying = quoteStreams[ix].underlying;
            retQuote.strikePrice = quoteStreams[ix].strikePrice;
            retQuote.description = quoteStreams[ix].description;
            retQuote.value = quoteStreams[ix].last;
            retQuote.impliedVolatility = quoteStreams[ix].impliedVolatility;
            retQuote.errorGettingQuote = quoteStreams[ix].errorGettingQuote;
            retQuote.cpType = quoteStreams[ix].cpType;
            retQuote.optionDate = quoteStreams[ix].optionDate;
 /*
  need to massage the LAST option quote value if the volume is zero.
             * Give the mid point between ask and bid, as a fair value.
 */
            if (option == true) {
                if ((retQuote.last >= retQuote.ask) || (retQuote.last <= retQuote.bid) || (retQuote.volume == 0) || (true == true)) {
                    /* figure out mid point betwen bid and ask. */
                    float tmp = (float) Math.abs(retQuote.ask - retQuote.bid) / 2;
                    /* assume fair value between bid and ask. */
                    retQuote.value = myUtils.Round((float) (tmp + retQuote.bid), 2);
                } else {
                    // no fudging necc. so set value to last.
                    retQuote.value = retQuote.last;
                }
                if (retQuote.volume == 0) {
                    retQuote.last = retQuote.value;
                }

            }else {
                
            }

        } else {
            retQuote = null;
        }
        qSem.release();
        return (retQuote);

    }
    private int apiClientId = 0;
    public int getCliendId() {
        return apiClientId;
    }
    public void setClientId(int clientIn){
        apiClientId = clientIn;
    }
    public boolean connectToIbHost() {
        // connect to TWS
        m_disconnectInProgress = false;
        String ipAddress = "";
        int retPort = 7496;
        System.out.println("connectToIbHost: trying to connect with this clientId : " + apiClientId);
        m_client.eConnect(ipAddress, retPort, apiClientId);
        if (m_client.isConnected()) {
            System.out.println("Connected to Tws server version "
                    + m_client.serverVersion() + " at "
                    + m_client.TwsConnectionTime());


        } else {
            System.out.println("Connecting to Tws server failed ");

        }
        ibConnectError = !m_client.isConnected();
        return (m_client.isConnected());
    }
    public void disConnectFromIbHost(){
        m_disconnectInProgress = true;
        m_client.eDisconnect();
    }
    static void  delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
public class ibApiConnectivity {
    final boolean MSG_ON = false;
    /* number of alarms per data feeds */
    final int MRKT_DATA_FARM = 4;
    final int HMDS_DATA_FARM = 2;
    
    /*exceeded ib msg rate*/
    final int MSG_RATE_EXEEDED = 100;
    
    final int MRKT_DATA_OK_CODE = 2104;
    final int MRKT_DATA_BROKEN_CODE = 2103;
    
    final int HMDS_DATA_OK_CODE = 2106;
    final int HMDS_DATA_BROKEN_CODE = 2105;
    
    /* connection codes */
    final int CONNECTION_OK_DATA_OK_CODE = 1102;
    final int CONNECTION_OK_DATA_LOST_CODE = 1101;
    final int CONNECTION_LOST_CODE = 1100;
    
    /* start with all good */
    private Integer curMrktDataState = 0;
    private Integer curHmdsDataState = 0;
    private Boolean curConnectionLost = false;
    
    private Boolean okToDisplayGoodness = false;
    private Boolean okToDisplayBadness = false;
    
    public Boolean allOk(boolean hang){
       Boolean allGood = true;
       
        allGood = ((curMrktDataState >= 0) && (curHmdsDataState >= 0) && (curConnectionLost == false));
        if((hang == true) && (allGood == false)){
            while (allGood == false) {
                myUtils.delay(1000);
            }
            
        }
        return allGood;
    }
    public void postWarning(Integer errorCode, String errMsg){
        switch (errorCode){
            case MRKT_DATA_BROKEN_CODE: 
                curMrktDataState--;
                if(MSG_ON == true){
                    System.out.println("curMrktDataState Bad: " + curMrktDataState);
                }
                break;
            case MRKT_DATA_OK_CODE:
                curMrktDataState = (curMrktDataState >= 0)? 0 : curMrktDataState+1;
                if(MSG_ON == true){
                    System.out.println("curMrktDataState Gd: " + curMrktDataState);
                }
                break;
            case HMDS_DATA_BROKEN_CODE: 
                curHmdsDataState--; 
                if(MSG_ON == true){
                    System.out.println("curHmdsDataState Bad: " + curHmdsDataState);
                }
                break;
            case HMDS_DATA_OK_CODE:     
                curHmdsDataState = (curHmdsDataState >= 0)? 0 : curHmdsDataState+1;
                if(MSG_ON == true){
                    System.out.println("curHmdsDataState Gd: " + curHmdsDataState);
                }
                break;
            case CONNECTION_LOST_CODE:  
                curConnectionLost = true; 
                if(MSG_ON == true){
                    System.out.println("curConnectionState Bad: " + curConnectionLost);
                }
                break;
            case CONNECTION_OK_DATA_OK_CODE:
                curConnectionLost = false;
                if(MSG_ON == true){
                    System.out.println("curConnectionState Gd: " + curConnectionLost);
                }
                break;
            case CONNECTION_OK_DATA_LOST_CODE:
                curConnectionLost = false;
                if(MSG_ON == true){
                    System.out.println("curConnectionState Gd: " + curConnectionLost);
                }
                break;
            case MSG_RATE_EXEEDED:
                System.out.println("\nMSG_RATE_EXCEEDED!!!!" + errorCode + errMsg);
                break;
            default:
                if (MSG_ON) {
                    System.out.println("ibApiConnectivty: unkown Code in post warnings:" + errorCode + errMsg);
                }
                break;
                
        }
        if ((curConnectionLost == false) && (curMrktDataState >= 0) && (curHmdsDataState >= 0)){
            okToDisplayBadness = true;
            if(okToDisplayGoodness == true){
                System.out.println("Connection to TWS Cleared.");
                okToDisplayGoodness = false;              
            }else {
                
            }
        }else{           
            okToDisplayGoodness = true;
            if (okToDisplayBadness == true) {
                System.out.println("Connection to TWS problem.");  
                okToDisplayBadness = false;
            }else {
                
            }
        }
    }    
        
    }
    public class ibApiConnectivitySimple {
    final boolean MSG_ON = true;
    /* posible alarm bits  */
    final int MRKT_DATA_FARM = 1;
    final int HMDS_DATA_FARM = 2;
    final int CONNECTION     = 4;
    /*exceeded ib msg rate*/
    final int MSG_RATE_APPROACH = 0;
    final int MSG_RATE_EXEEDED = 100;
    
    final int MRKT_DATA_OK_CODE = 2104;
    final int MRKT_DATA_BROKEN_CODE = 2103;
    
    final int HMDS_DATA_OK_CODE = 2106;
    final int HMDS_DATA_BROKEN_CODE = 2105;
    
    /* connection codes */
    final int CONNECTION_OK_DATA_OK_CODE = 1102;
    final int CONNECTION_OK_DATA_LOST_CODE = 1101;
    final int CONNECTION_LOST_CODE = 1100;
    
    /* start with all good */
    private Integer curAlarmState = 0;
    
    
    private Boolean okToDisplayGoodness = false;
    private Boolean okToDisplayBadness = false;
    
    public Boolean allOk(boolean hang){
       Boolean allGood = true;
       
        allGood = (curAlarmState == 0);
        if((hang == true) && (allGood == false)){
            while (allGood == false) {
                myUtils.delay(1000);
            }
            
        }
        return allGood;
    }
    public void postWarning(Integer errorCode, String errMsg){
        switch (errorCode){
            case MRKT_DATA_BROKEN_CODE: 
                /* set the bit for alarm */
                curAlarmState |= MRKT_DATA_FARM;
                
                break;
            case MRKT_DATA_OK_CODE:
                /* clear the bit */
                curAlarmState &= ~MRKT_DATA_FARM;
                
                break;
            case HMDS_DATA_BROKEN_CODE: 
                curAlarmState |= HMDS_DATA_FARM;
                
                break;
            case HMDS_DATA_OK_CODE:     
                curAlarmState &= ~HMDS_DATA_FARM;
                
                break;
            case CONNECTION_LOST_CODE:  
                curAlarmState |= CONNECTION;
                
                break;
            case CONNECTION_OK_DATA_OK_CODE:
                curAlarmState &= ~CONNECTION;

                break;
            case CONNECTION_OK_DATA_LOST_CODE:
                curAlarmState &= ~CONNECTION;

                break;
            case MSG_RATE_EXEEDED:
                System.out.println("\nMSG_RATE_EXCEEDED!!!! ->" + errorCode + errMsg);
                ibThrottleMsgFlow = true;
                myUtils.delay(100);
                break;
            case MSG_RATE_APPROACH:
                System.out.println("\nAPPROACHING MSG_RATE_!!! ->" + errorCode + errMsg);
                ibThrottleMsgFlow = true;
                myUtils.delay(100);
                break;
            default:
                if (MSG_ON) {
                    System.out.println("ibApiConnectivty: unkown Code in post warnings. ->" + errorCode + errMsg);
                }
                break;
                
        }
        if (curAlarmState == 0){
            okToDisplayBadness = true;
            if(okToDisplayGoodness == true){
                System.out.println("Connection to TWS Cleared.");
                okToDisplayGoodness = false;              
            }else {
                
            }
        }else{           
            okToDisplayGoodness = true;
            if (okToDisplayBadness == true) {
                System.out.println("Connection to TWS problem.");  
                okToDisplayBadness = false;
            }else {
                
            }
        }
            
        if (MSG_ON == true){
            System.out.println("curAlarmState: " + curAlarmState);    
        }
    }
    public void ibApiConnectivity(){
        curAlarmState = 0;
    }
}

    public class OptionChain {

        private double highestYMD = 0;
        /* this multiplyer is need to not have the strike affect the option date when
         * ordering the option chain values. option date is added with strike to order them.
         * Strike can be big enough to cause miss ordering so this multiplyer eliminates this.
         */
        private final double OPTION_DATE_MULTIPLIER = 100.0;
        public Vector qOptionChainVec;
        private int storeIdx, getIdx;
        private Boolean doneFilling = false;
        private Boolean errorFillingFromApi = false;
        
        private int chId = 2;
        private quoteInfo[] quoteStreams;
        
        private int qsIdx = 0;
        private int tickId = 0;
        private int chainOrderedIdx = 0;
        private int chainOffset = 100;
        private int filterRange = 7;
        //private final int optionChainStart = 100;
        
        /* used to filter streams, this is rounded */
        private double stockQuote;
        private double bid;
        private double ask;
        private int lpCnt = 0;
        private boolean turnStreamsOnToo = true;
        public OptionChain() {

            qOptionChainVec = new Vector(200, 10);
            storeIdx = 0;
            getIdx = 0;
            chainClear();
            quoteStreams = new quoteInfo[MAX_STREAMS];
            //private qOptionChain qOptions = new qOptionChain();
            
        }
        public void turnWithStreamsOn(boolean withStreams){
            turnStreamsOnToo = withStreams;
        }
        private int getStream(String sym, boolean option) {

            for (int ix = 0; (ix < qsIdx) && (ix < MAX_STREAMS); ix++) {

                if ((quoteStreams[ix] != null) && (quoteStreams[ix].symbol.equals(sym)) && (quoteStreams[ix].optionQuote == option)) {

                    return (ix);
                }

            }

            return (NOT_FOUND);
        }

        private boolean openQuote(String sym, boolean option) {
            boolean retValue = false;
            //qSem.acquire();
            if (getStream(sym, option) == NOT_FOUND) {

                m_contract = new Contract();
                if (option) {

                    getOptionParams(sym);
                    if (true) {
                        /* try m_symbol = null and set local symbol to symbol */
///               m_contract.m_symbol = symbol;
//                m_contract.m_symbol = sym;
//try!!!!
                        m_contract.m_localSymbol = sym;

//try!!!!                m_contract.m_expiry = ymd;
//try!!!!                m_contract.m_strike = strike;
                        m_contract.m_secType = "OPT";
                        m_contract.m_exchange = "SMART";
                        //try!!!               m_contract.m_multiplier = "100";

                        m_contract.m_currency = "USD";
 //try!!               m_contract.m_right = cOrP;
                    } else {
                        m_contract.m_symbol = symbol;

                        m_contract.m_expiry = ymd;
                        m_contract.m_strike = strike;
                        m_contract.m_secType = "OPT";
                        m_contract.m_exchange = "SMART";
                        m_contract.m_multiplier = "100";

                        m_contract.m_currency = "USD";
                        m_contract.m_right = cOrP;

                    }

                    quoteStreams[qsIdx] = new quoteInfo();
                    quoteStreams[qsIdx].tickId = tickId;
                    quoteStreams[qsIdx].symbol = sym;
                    quoteStreams[qsIdx].optionQuote = true;
                    quoteStreams[qsIdx].cpType = cOrP;
                    quoteStreams[qsIdx].strikePrice = strike;
                    quoteStreams[qsIdx].optionDate = ymd;
                } else {

                    m_contract.m_symbol = sym;
                    m_contract.m_secType = "STK";
                    m_contract.m_strike = 0;
                    m_contract.m_exchange = "SMART";
                    m_contract.m_primaryExch = "ISLAND";
                    m_contract.m_currency = "USD";

                    quoteStreams[qsIdx] = new quoteInfo();
                    quoteStreams[qsIdx].tickId = tickId;
                    quoteStreams[qsIdx].symbol = sym;
                    quoteStreams[qsIdx].optionQuote = false;
                }

                qsIdx++;
                if (qsIdx < MAX_STREAMS) {

                    m_client.reqMktData((chainOffset + tickId), m_contract, ALL_GENERIC_TICK_TAGS, !GET_SNAP_SHOT);

                    tickId++;
                    retValue = true;
                } else {
                    System.out.println("Create Stream: too many streams!!");
                }

            }
            //qSem.release();
            return retValue;

        }

        public boolean cancelStream(String sym, boolean option) {
            boolean result = true;
            int ix = 0;
            String timeStamp;
            qSem.acquire();
            if ((ix = getStream(sym, option)) == NOT_FOUND) {
                System.out.println("cancelStream error: stream does not exist for symbol! " + sym);
                result = false;
            } else {
                m_client.cancelMktData(quoteStreams[ix].tickId + chainOffset);
                quoteStreams[ix].tickId = 0;
                quoteStreams[ix].tickerUsed = 0;
                quoteStreams[ix].symbol = "";
                quoteStreams[ix].optionQuote = false;
                qSem.release();
                delay(20);
                qSem.acquire();
                if (ibThrottleMsgFlow == true) {
                    System.out.println("\nIbThrottle received!!Delay..");
                    timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());                    
                    System.out.println(timeStamp);
                    qSem.release();
                    delay(1000);
                    qSem.acquire();
                    ibThrottleMsgFlow = false;
                    System.out.println("\nDelay..done");
                    timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());                    
                    System.out.println(timeStamp);
                }
            }
            qSem.release();          
            
            return(result);
        }
        public quoteInfo getQuote(String sym, boolean option) {
            int ix = 0;
            int cnt = 0;
            final int ERRONEOUS = 10000000;
            boolean timeOut = false;
            quoteInfo retQuote;
            qSem.acquire();
            if ((ix = getStream(sym, option)) == NOT_FOUND) {

                openQuote(sym, option);
//           System.out.println("getQuote: " + sym + " not found in Streams. Added it..");

                qSem.release();
                // for options (unusual option activity..use 80, else for quotes use 2000)...
                delay((option == true) ? 80 : 50/* use 80 for unusual option volume!!! 500 works *//*3000 works*/);
                qSem.acquire();

            }

            if ((ix = getStream(sym, option)) != NOT_FOUND) {
               
                if (quoteStreams[ix].volume > ERRONEOUS) {
                    System.out.println("ERRONIOUS volume..delaying..");
                    delay(500);
                }
                /*
                while ((quoteStreams[ix].last == 0.0) && (timeOut == false)){               
                    System.out.println("last == 0..delaying..cnt = " + cnt);
                    delay(500);
                    cnt++;
                    timeOut = (cnt > 25);                    
                }
                if (timeOut == true){
                    System.out.println("getQuote: TIME OUT waiting for last > 0!!!!");
                }
                */
                retQuote = new quoteInfo();
                retQuote.tickId = quoteStreams[ix].tickId;
                retQuote.optionQuote = quoteStreams[ix].optionQuote;
                retQuote.bid = quoteStreams[ix].bid;
                retQuote.last = quoteStreams[ix].last;
                retQuote.ask = quoteStreams[ix].ask;
                retQuote.open = quoteStreams[ix].open;
                retQuote.prevClose = quoteStreams[ix].prevClose;
                retQuote.volume = quoteStreams[ix].volume;
                retQuote.pOpenInterest = quoteStreams[ix].pOpenInterest;
                retQuote.cOpenInterest = quoteStreams[ix].cOpenInterest;


                retQuote.delta = quoteStreams[ix].delta;
                retQuote.underlying = quoteStreams[ix].underlying;
                retQuote.strikePrice = quoteStreams[ix].strikePrice;
                retQuote.description = quoteStreams[ix].description;
                retQuote.value = quoteStreams[ix].last;
                retQuote.impliedVolatility = quoteStreams[ix].impliedVolatility;
                retQuote.errorGettingQuote = quoteStreams[ix].errorGettingQuote;
                retQuote.cpType = quoteStreams[ix].cpType;
                retQuote.optionDate = quoteStreams[ix].optionDate;
                retQuote.tickerUsed = quoteStreams[ix].tickerUsed;
                retQuote.updateCnt = quoteStreams[ix].updateCnt;
                retQuote.respBits = quoteStreams[ix].respBits;
                /*
                need to massage the LAST option quote value if the volume is zero.
                 * Give the mid point between ask and bid, as a fair value.
                 */
                if (option == true) {
                    if ((retQuote.last >= retQuote.ask) || (retQuote.last <= retQuote.bid) || (retQuote.volume == 0) || (true == true)) {
                        /* figure out mid point betwen bid and ask. */
                        float tmp = (float) Math.abs(retQuote.ask - retQuote.bid) / 2;
                        /* assume fair value between bid and ask. */
                        retQuote.value = myUtils.Round((float) (tmp + retQuote.bid), 2);
                    } else {
                        // no fudging necc. so set value to last.
                        retQuote.value = retQuote.last;
                    }
                    if (retQuote.volume == 0){
                        retQuote.last = retQuote.value;
                    }

                } else {
                }

            } else {
                retQuote = null;
            }
            qSem.release();
            return (retQuote);

        }
        public void setFilterRange(int rangein){
            filterRange = rangein;
        }
        public void cancelChainStreams() {
            int actIdx = 0;
            //System.out.println("cancelChainStreams: " + qsIdx);
            String timeStamp;
            qSem.acquire();
            while (actIdx < qsIdx) {
                /* added actIdx == 0 to catch the first stream */
                if ((quoteStreams[actIdx].tickerUsed > 0) || (actIdx == 0)) {
                    if (actIdx == 0){
                        System.out.println("cancelChainStreams: qsIdx = " + qsIdx);
                    }
                    m_client.cancelMktData(quoteStreams[actIdx].tickId + chainOffset);
                    quoteStreams[actIdx].tickId = 0;
                    quoteStreams[actIdx].tickerUsed = 0;
                    quoteStreams[actIdx].symbol = "";
                    tickId = 0;
                }
                actIdx++;
                qSem.release();
                delay(20);
                qSem.acquire();
                if (ibThrottleMsgFlow == true) {
                    System.out.println("\nIbThrottle received!!Delay..");
                    timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());                    
                    System.out.println(timeStamp);
                    qSem.release();
                    delay(1000);
                    qSem.acquire();
                    ibThrottleMsgFlow = false;
                    System.out.println("\nDelay..done");
                    timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());                    
                    System.out.println(timeStamp);
                }
            }
            qsIdx = 0;
            qSem.release();
        }

        public void startNewChain() {
            chainClear();
            doneFilling = false;
            errorFillingFromApi = false;
            
            chId++;
            cancelChainStreams();
            qsIdx = 0;
        }

        private void chainClear() {
            qOptionChainVec.clear();
            storeIdx = 0;
            getIdx = 0;
        }

        private void chainAdd(quoteInfo q) {
            qOptionChainVec.add((quoteInfo) q);
            storeIdx++;
        }

        private void chainReplace(quoteInfo q, int location) {

            qOptionChainVec.setElementAt(q, location);
        }

        private quoteInfo chainGet(int location) {

            return (quoteInfo) qOptionChainVec.get(location);
        }

        public int chainSz() {
            return (storeIdx);
        }

        public int chainLeft() {
            return (storeIdx - getIdx);
        }

        private int chainGetIdx() {
            return (getIdx - 1);
        }

        public quoteInfo chainGetNext() {
            
            if (getIdx < storeIdx) {
                return (quoteInfo) qOptionChainVec.elementAt(getIdx++);
            } else {
                getIdx = 0;
                return (quoteInfo) qOptionChainVec.elementAt(getIdx++);
            }



        }

        public void chainIdxReset() {
            getIdx = 0;
        }

        public void chainOrderedIdxReset() {
            chainOrderedIdx = 0;
        }
        public int chainOrderedGetIdx() {
            return(chainOrderedIdx);
        }
        
        public void chainOrderedSetIdx(int set) {
            chainOrderedIdx = set;
        }


        public quoteInfo chainGetNextOrdered(int lastOne) {
            quoteInfo actSearch = new quoteInfo();
            boolean found = false;
            int saveIdx = getIdx;
            /*
             * zero means start from start. Any other number just increment to next one to
             */           
            lastOne++;
            if (lastOne < storeIdx) {
                getIdx = 0;
                while (!found && getIdx < storeIdx) {
                    actSearch = this.chainGetNext();
                    if (actSearch.orderNum == lastOne) {
                        found = true;
                    }
                }
                if (found) {
                    getIdx = saveIdx;
                    return (actSearch);
                } else {
                    getIdx = saveIdx;
                    return (null);
                }

            } else {
                return (null);
            }
        }

        public quoteInfo chainGetNextOrdered() {
            quoteInfo actSearch = new quoteInfo();
            boolean found = false;
            int saveIdx = getIdx;
            quoteInfo retValue;

            chainOrderedIdx++;
            if (chainOrderedIdx < storeIdx) {
                getIdx = 0;
                while (!found && (getIdx < storeIdx)) {
                    actSearch = this.chainGetNext();
                    if (actSearch == null) {
                        found = false;
                                
                    }
                    if (actSearch.orderNum == chainOrderedIdx) {
                        found = true;
                    }
                    
                }
                if (found) {
                    getIdx = saveIdx;
                    retValue = actSearch;
                } else {
                    getIdx = saveIdx;
                    retValue = null;
                }

            } else {
                retValue = null;
            }
            
            return(retValue);
        }

        void enATM() {
            /*
             * this routine will go through all ordered option chains for this underlying
             * search for the at the money option and enable it. This is part of the filter process i.e
             * turn on streaming around ATM option.
             */
            
            quoteInfo oc = new quoteInfo();
            boolean keepGoing = true;

            System.out.println("Enabling ATM options..");
            actChain.chainIdxReset();
            actChain.chainOrderedIdxReset();
            oc = chainGetNextOrdered();
            while(chainOrderedGetIdx() < (storeIdx - 2)) {

                /* look for strike on ascending options....*/
                while ((oc != null) && (stockQuote > oc.strikePrice) && (keepGoing = (chainOrderedGetIdx() < (storeIdx - 2)))) {
                    oc = chainGetNextOrdered();
                    while ((oc == null) && (chainOrderedGetIdx() < (storeIdx - 2))) {
                        System.out.println("enATM: could not find " + chainOrderedGetIdx() +" ? in chainGetNextOrdered.");
                        oc = chainGetNextOrdered();
                    }                      
                }
                /* if we didn't hit end, we found our ATM option*/
                if ((keepGoing) && (oc !=null)) {
                    /* we are not at the end, and we found our ATM option, enable it...
                     first call ....*/
                    oc.enStreaming = true;
//                    actChain.chainReplace(oc, chainOrderedGetIdx());
                    /*
                     * now put...
                     */
                    oc = chainGetNextOrdered();
                    oc.enStreaming = true;
                    /* prime for next read */
                    oc = chainGetNextOrdered();
                }
                /* scan through remainder of options above ATM until we hit next months...
                   where the option strike will again be smaller than our quote. search for this
                 * condition.
                 */
                while ((keepGoing = (chainOrderedGetIdx() < (storeIdx - 2))) && (oc != null) && (stockQuote <= oc.strikePrice)) {
                    oc = chainGetNextOrdered();
                    while ((oc == null) && (chainOrderedGetIdx() < (storeIdx - 2))) {
                        System.out.println("enATM: could not find " + chainOrderedGetIdx() +" ? in chainGetNextOrdered.");
                        oc = chainGetNextOrdered();
                    }                   

                }
                

            }
            /* we are done....all ATM options should be enabled. */
            System.out.println("Completed Enabling ATM options.");
            actChain.chainIdxReset();
            actChain.chainOrderedIdxReset();
            
            while (chainOrderedGetIdx() < (storeIdx - 2)) {
                oc = chainGetNextOrdered();
                if ((oc != null) && oc.enStreaming == true) {
                    System.out.println(oc.strikePrice + " is enabled.");
                }

            }
            actChain.chainIdxReset();
            actChain.chainOrderedIdxReset();
        }

        void filter(int range) {
            quoteInfo oc = new quoteInfo();
            int begEnableHere = 0;
            int endEnableHere = 0;
            
            System.out.println("running filter with: " + range + " range.");

             /* search for enabled option and enable -range and +range form ATM */
            
            actChain.chainIdxReset();
            actChain.chainOrderedIdxReset();
            range *=2;
            while (chainOrderedGetIdx() < (storeIdx - 2)) {
                oc = chainGetNextOrdered();
                if ((oc != null) && oc.enStreaming == true) {
                    begEnableHere = (actChain.chainOrderedGetIdx() - range);
                    if (begEnableHere <= 0) {
                        begEnableHere = 0;
                    }
                    endEnableHere = begEnableHere + (range * 2);
                    if (endEnableHere >= (storeIdx - 2)) {
                        endEnableHere = (storeIdx - 2);
                    }
                    System.out.println("filter starting at "+ begEnableHere + " ending at "+ endEnableHere);
                    actChain.chainOrderedSetIdx(begEnableHere);
                    while (actChain.chainOrderedGetIdx() <= endEnableHere) {
                        oc = chainGetNextOrdered();
                        if (oc != null) {
                            /* first call ....*/
                            oc.enStreaming = true;
                        }else{
                            System.out.println("filter: null?");
                        }

                        oc = chainGetNextOrdered();
                        
                        if (oc != null) {
                            oc.enStreaming = true;
                        }else{
                            System.out.println("filter: null?");
                        }


                    }
                }


            }
            actChain.chainIdxReset();
            actChain.chainOrderedIdxReset();

            System.out.println("completed filtering. ");

        }

        public Boolean getOptionChain(String symbol) {
            int sz = 0;
            quoteInfo qc;
            quoteInfo sq;
            Contract specificDetails = new Contract();
            double chainQ = 0;
            final int doneTimeout = 1500;


            m_client.reqIds(chId);
            specificDetails.m_symbol = symbol;
            specificDetails.m_secType = "OPT";
            specificDetails.m_strike = 0;
            specificDetails.m_exchange = "SMART";
            specificDetails.m_primaryExch = "ISLAND";
            specificDetails.m_currency = "USD";
            specificDetails.m_multiplier = "100";

            m_client.reqContractDetails(chId, specificDetails);
            int sz2 = 0;
            while (this.doneFilling.equals(false) && this.errorFillingFromApi.equals(false) && sz2 < doneTimeout) {
                delay(100);
                sz2++;
                if((sz2 % 50) == 0) {
                    System.out.println("getOptionChain: waiting for doneFilling..sz2 : " + sz2);
                }
                
            }
            if(this.errorFillingFromApi.equals(true)) {
                //System.out.println("getOptionChain: error filling chain.");
                return(false);
            }
            if (sz2 >= doneTimeout) {
                System.out.println("getOptionChain: ERROR waiting for doneFilling to complete!!!!!!!!!!!!!!!!!!");
                //commonGui.postInformationMsg("getOptionChain: ERROR waiting for doneFilling to complete!!!!!!!!!!!!!!!!!!");
                return(false);
            }

            /* chain comes down in somewhat random order from IB, this will
             * enumerate so that displaying is orderly...
             */
            this.enumerateChain();
            /* we may not want to turn streams on right now.*/
            if (turnStreamsOnToo) {
                /*
                 * need to get stock quote of underlying in order to filter around
                 * the at the money strikes by 3,5,10 etc..
                 */
                lpCnt = 0;
                do {
                    sq = getQuote(symbol, false);
                    this.stockQuote = Math.round(sq.last);
                    this.bid = Math.round(sq.bid);
                    this.ask = Math.round(sq.ask);
                    lpCnt++;
                //remove
                    //this.stockQuote = 447.0;
                } while ((this.stockQuote == 0.0) && (this.bid == 0.0) && (this.ask == 0.0) && (lpCnt < 100));
                if (lpCnt >= 100) {
                    return (false);
                }
                this.enATM();
                //this.filter(7);
                this.filter(filterRange);

                /* now go through all option chain entries and begin streaming for each. */
                actChain.chainIdxReset();
                qSem.acquire();
                System.out.println("Openning streams for chain...sz is:" + actChain.chainSz());
                while (sz++ < actChain.chainSz()) {
                    qc = new quoteInfo();
                    qc = actChain.chainGetNext();
                    if (qc.enStreaming == true) {
                        openQuote(qc.optionSymbol, true);
                    }
                    //add delay..throttle msgs to ib..
                    myUtils.delay(15);
                    if(ibThrottleMsgFlow == true){
                        myUtils.delay(100);
                        ibThrottleMsgFlow = false;
                    }
                }
                qSem.release();
                actChain.chainIdxReset();
                System.out.println("Completed opening streams for chain.");
            }
            return (true);
        }

        private double highestYMD() {
            /* search through active chain and find lowest year to return */
            double cyr = 0, hyr = 0;
            quoteInfo ac = new quoteInfo();
            int testint;
            double testdbl;
            this.chainIdxReset();

            ac = this.chainGetNext();
            hyr = cyr = ((double) Integer.parseInt(ac.optionDate.substring(0, 8)) * OPTION_DATE_MULTIPLIER) + ac.strikePrice;

            while (this.getIdx < this.storeIdx) {

                /* if orderNum is not zero, it's already been ordered, so skip */
                if (cyr > hyr) {
                    hyr = cyr;

                }
                ac = this.chainGetNext();
                cyr = ((double) Integer.parseInt(ac.optionDate.substring(0, 8)) * OPTION_DATE_MULTIPLIER) + ac.strikePrice;
            }


            return (hyr);

        }

        private int lowestYMD() {
            /* search through active chain and find lowest year to return */
            double cyr = 0, lyr = 0;
            int svIdx = -1;
            quoteInfo ac = new quoteInfo();

            this.chainIdxReset();

            lyr = highestYMD;
            while (this.getIdx < this.storeIdx) {
                ac = this.chainGetNext();
                cyr = ((double) Integer.parseInt(ac.optionDate.substring(0, 8)) * OPTION_DATE_MULTIPLIER) + ac.strikePrice;
                /* if orderNum is not zero, it's already been ordered, so skip */
                if ((ac.orderNum == 0) && cyr < lyr) {
                    lyr = cyr;
                    svIdx = this.getIdx - 1;
                }

            }


            return (svIdx);
        }

        private void enumerateChain() {
            int actIdx = 0;
            int actOrder = 1;
            int tempOrder = 1;
            quoteInfo ac = new quoteInfo();
            boolean done = false;

            System.out.println("ordering the option chain.");
            highestYMD = highestYMD();
            //while (actOrder <= this.actOptionChain.storeIdx) {
            while (done == false) {
                if ((actIdx = lowestYMD()) != -1) {
                    /* we have found the lowest year/month/day, so mark it found with an order number */
                    ac = this.chainGet(actIdx);
                    ac.orderNum = actOrder++;
                    this.chainReplace(ac, actIdx);
                } else {
                    System.out.println("ordering completed.");
                    done = true;
                    if (CHAIN_ORDER_DISPLAY) {
                        this.chainIdxReset();
                        tempOrder = 1;
                        while (tempOrder < actOrder) {
                            ac = this.chainGetNext();
                            if (tempOrder == ac.orderNum) {
                                System.out.println("date " + ac.optionDate + " " + ac.strikePrice + " " + ac.cpType + " " + tempOrder);
                                tempOrder++;
                                this.chainIdxReset();
                            }

                        }
                    }
                }
            }
        }
    }

    public static class OrderStatus {

        private boolean updatedOrderData = false;
        private boolean updatedOrderStatus = false;
        public boolean statusRdy = false;
        public boolean statusRdyOnly = false;
        public boolean error = false;
        public String errorMsg = "";
        public int errorCode = 0;
        private String action;
        public int quantity;
        public String symbol;
        private String secType;
        private String orderType;
        private double price;
        public String status = "";
        public int filled;
        public int remaining;
        public double aveFillPrice;
        private int lastFillPrice;
        public int ordId;
    }

    public class orderData {

            public orderData() {
                orderContract = new Contract();
                order = new Order();
                orderStatus = new OrderStatus();

            }
            private Contract orderContract;
            private Order order;
            private OrderStatus orderStatus;
            private int ordNum;

            public String symbol;
            public boolean option;
            public String action;
            public String type;
            public String timeInForce;
            public int quantity;
            public double price;
        }

    public class orderInfo {

        private Vector orders = new Vector(10, 5);
        private orderData newOrder;
        private int storeIdx;
        private int lastOrderNum;
        private int getIdx;

        private void orderAdd(orderData order) {
            wrapperSem.acquire();
            orders.add((orderData) order);
            storeIdx++;
            wrapperSem.release();
        }

        public orderData orderGetNext() {

            if (getIdx < storeIdx) {
                return (orderData) orders.elementAt(getIdx++);
            } else {
                getIdx = 0;
                return (orderData) orders.elementAt(getIdx++);
            }
        }
        public void orderIdxReset() {
            getIdx = 0;
        }
        private orderData orderGet(int loc) {
            orderData actOrd = new orderData();
            if (loc < storeIdx) {
                wrapperSem.acquire();
                actOrd = (orderData) orders.get(loc);
                wrapperSem.release();
            }else {
                System.out.println("orderGet: error loc out of bounds! " + loc);
                actOrd = null;
            }
            return (actOrd);

        }
        public int getNumberOfOrders() {
            return storeIdx;
        }
        public OrderStatus getOrderStatusFast(int orderIn){
            orderData actOrder = new orderData();
            actOrder = this.orderGet(orderIn);
            actOrder.orderStatus.statusRdy = (actOrder.orderStatus.updatedOrderData && actOrder.orderStatus.updatedOrderStatus);
            if(actOrder.orderStatus.statusRdy == true){
                return actOrder.orderStatus;
            }
            return null;
        }
        public OrderStatus getOrderStatusOnly(int orderIn){
            orderData actOrder = new orderData();
            actOrder = this.orderGet(orderIn);
            actOrder.orderStatus.statusRdyOnly = (actOrder.orderStatus.updatedOrderStatus);
            if(actOrder.orderStatus.statusRdyOnly == true){
                return actOrder.orderStatus;
            }
            return null;
        }
        public OrderStatus getOrderStatus(int orderIn) {
            orderData actOrder = new orderData();
            final int timeOut = 80;
            boolean timeIsOut = false;
            int to = 0;
            boolean orderFilled = false;
            boolean error = false;
            
            actOrder = this.orderGet(orderIn);
            if (actOrder != null) {
                actOrder.orderStatus.statusRdy = (actOrder.orderStatus.updatedOrderData && actOrder.orderStatus.updatedOrderStatus);
                orderFilled = (actOrder.orderStatus.statusRdy && (actOrder.orderStatus.status.contains("Filled")));
                System.out.println("getOrderStatus: statusRdy = " + actOrder.orderStatus.statusRdy);
                System.out.println("getOrderStatus: orderFilled = " + orderFilled);
                error = actOrder.orderStatus.error;
                while ((orderFilled == false) && (timeIsOut == false) && (error == false)) {
                    System.out.println("getOrderStatus: waiting for statusRdy and Filled to be true.. " + to);
                    myUtils.delay(500);
 //                   actOrder = new orderData();
                    actOrder = this.orderGet(orderIn);
                    if (actOrder != null) {
                        actOrder.orderStatus.statusRdy = (actOrder.orderStatus.updatedOrderData && actOrder.orderStatus.updatedOrderStatus);
                        orderFilled = (actOrder.orderStatus.statusRdy && (actOrder.orderStatus.status.contains("Filled")));
                        error = actOrder.orderStatus.error;
                        System.out.println("getOrderStatus: statusRdy = " + actOrder.orderStatus.statusRdy);
                        System.out.println("getOrderStatus: orderFilled = " + orderFilled);
                    } else {
                        System.out.println("getOrderStatus: actOrder == NULL !!!!!");
                    }
                    to++;
                    if (to >= timeOut){
                        timeIsOut = true;
                        actOrder.orderStatus.errorMsg = "Timed out waiting for fill to complete!";
                    }
                }
                if (timeIsOut == true){
                    System.out.println("getOrderStatus: timedOut!!! waiting for filled to be true.. ");
                    System.out.println("getOrderStatus: status = " + actOrder.orderStatus.status);
                    System.out.println("getOrderStatus: filled = " + actOrder.orderStatus.filled);
                    System.out.println("getOrderStatus: remaining = " + actOrder.orderStatus.remaining);
                    System.out.println("getOrderStatus: avPrice = " + actOrder.orderStatus.aveFillPrice);
                }else if (error == true ){
                    System.out.println("orderStatus.error: " + actOrder.orderStatus.errorMsg + "occurred!!");
                }else{
                    System.out.println("getOrderStatus: filled true after waiting.. " + to);
                    System.out.println("getOrderStatus: status = " + actOrder.orderStatus.status);
                    System.out.println("getOrderStatus: filled = " + actOrder.orderStatus.filled);
                    System.out.println("getOrderStatus: remaining = " + actOrder.orderStatus.remaining);
                    System.out.println("getOrderStatus: avPrice = " + actOrder.orderStatus.aveFillPrice);
                }
            } else {
                System.out.println("getOrderStatus: actOrder == NULL !!!!!");
            }
            
            return (actOrder.orderStatus);
        }
        /*
         * this next method is needed to quarantee a different order number for each order.
         * if new one read is same as last, then keep reading until it changes.
         * had affect of throddling orders to a second each max.
         */

        private int generateOrderNumber() {
            int curVal;
            curVal = (int) (System.currentTimeMillis() / 1000);
            if (curVal == this.lastOrderNum) {
                while (curVal == (int) (System.currentTimeMillis() / 1000)) {
                    delay(100);
                }
                curVal = (int) (System.currentTimeMillis() / 1000);
            }
            this.lastOrderNum = curVal;
            return (curVal);
        }

        private orderData searchForOrder(int ordId) {
            int ix = 0;

            orderData retData = new orderData();
            while (ix < storeIdx) {
                retData = orderGet(ix++);
                if (retData.ordNum == ordId) {
                    return (retData);
                }
            }

            return (null);
        }

        public void updateOrderData(int id, Contract con, Order ord, OrderState stat) {
            orderData actDat = new orderData();

            if ((actDat = searchForOrder(id)) != null) {

                actDat.orderStatus.symbol = con.m_symbol;
                actDat.orderStatus.secType = con.m_secType;

                actDat.orderStatus.price = ord.m_lmtPrice;
                actDat.orderStatus.orderType = ord.m_orderType;
                actDat.orderStatus.action = ord.m_action;
                actDat.orderStatus.quantity = ord.m_totalQuantity;
                actDat.orderStatus.status = stat.m_status;
                /* mark as ready to read */
                actDat.orderStatus.updatedOrderData = true;
                actDat.orderStatus.ordId = id;

            }

        }

        public void updateOrderStatus(int id, String status, int filled, int remaining, double avePrice) {
            orderData actDat = new orderData();

            if ((actDat = searchForOrder(id)) != null) {
                actDat.orderStatus.status = status;
                actDat.orderStatus.filled = filled;
                actDat.orderStatus.remaining = remaining;
                actDat.orderStatus.aveFillPrice = avePrice;
                /* mark as ready to read */
                actDat.orderStatus.updatedOrderStatus = true;
                System.out.println("updateOrderStatus: id = " + id);
                System.out.println("updateOrderStatus: status = " + status);
                System.out.println("updateOrderStatus: filled = " + filled);
                System.out.println("updateOrderStatus: remaining = " + remaining);
                System.out.println("updateOrderStatus: avPrice = " + avePrice);
            }

        }

        public void updateOrderError(int id, int errorCode, String errorMsg) {
            orderData actDat = new orderData();

            if ((actDat = searchForOrder(id)) != null) {
                actDat.orderStatus.error = true;
                actDat.orderStatus.errorCode = errorCode;
                actDat.orderStatus.errorMsg = errorMsg;
                //wfs remove!!!
                System.err.println("updateOrderError: errorCode is "+errorCode+" error msg is " + errorMsg);

                /* mark as ready to read */
                actDat.orderStatus.updatedOrderStatus = true;
            }

        }

        public int setOrderInfo(String sym, boolean option, String inAction, String inOrderType,
                String timeInForce, double price, int howMany) {
            newOrder = new orderData();

            newOrder.symbol = sym;
            newOrder.option = option;
            newOrder.action = inAction;            
            newOrder.type = inOrderType;
            newOrder.timeInForce = timeInForce;

            newOrder.price = myUtils.roundMe((float)price, 2);
            newOrder.quantity = howMany;
            
            if (option == true) {
                /* if option order break down the sym containing all option info. */
                qSem.acquire();
                getOptionParams(sym);
                
                newOrder.orderContract.m_symbol = symbol;
                newOrder.orderContract.m_expiry = ymd;
                newOrder.orderContract.m_right = cOrP;
                newOrder.orderContract.m_strike = strike;
                newOrder.orderContract.m_multiplier = "100";
                qSem.release();

            } else {
                newOrder.orderContract.m_symbol = sym;
            }
            newOrder.orderContract.m_secType = option ? "OPT" : "STK";
            newOrder.orderContract.m_exchange = "SMART";
            newOrder.orderContract.m_currency = "USD";

            newOrder.order.m_clientId = 1;
            newOrder.order.m_orderId = 1;
            newOrder.order.m_permId = 1;
            newOrder.order.m_action = inAction; /* buy/sell */
            newOrder.order.m_totalQuantity = howMany;
            newOrder.order.m_orderType = inOrderType; /* market, limit etc */
            if (inOrderType.equals("LMT")) {
                newOrder.order.m_lmtPrice = myUtils.roundMe((float)price, 2);
            }
            newOrder.order.m_account = "DU123603";
            newOrder.ordNum = generateOrderNumber();
            System.out.println("setOrderInfo: price(round) = "+newOrder.order.m_lmtPrice);
            System.out.println("setOrderInfo: price = "+price);
            this.orderAdd(newOrder);

            System.out.println("setOrder (ordNum) " + newOrder.ordNum);

            return (storeIdx - 1);
        }
/* added 8/6/12 included account string. */
        public int setOrderInfo(String account, String sym, boolean option, String inAction, String inOrderType,
                String timeInForce, double price, int howMany) {
            newOrder = new orderData();

            newOrder.symbol = sym;
            newOrder.option = option;
            newOrder.action = inAction;
            newOrder.type = inOrderType;
            newOrder.timeInForce = timeInForce;

            newOrder.price = myUtils.roundMe((float) price, 2);
            newOrder.quantity = howMany;

            if (option == true) {
                /*
                 * if option order break down the sym containing all option
                 * info.
                 */
                qSem.acquire();
                getOptionParams(sym);

                newOrder.orderContract.m_symbol = symbol;
                newOrder.orderContract.m_expiry = ymd;
                newOrder.orderContract.m_right = cOrP;
                newOrder.orderContract.m_strike = strike;
                newOrder.orderContract.m_multiplier = "100";
                qSem.release();

            } else {
                newOrder.orderContract.m_symbol = sym;
            }
            newOrder.orderContract.m_secType = option ? "OPT" : "STK";
            newOrder.orderContract.m_exchange = "SMART";
            //2.26.16 wfws added prim exch to see if it fixes the ambiquis respose I sometimes get with some symbols..
            newOrder.orderContract.m_primaryExch = "ISLAND";
            newOrder.orderContract.m_currency = "USD";

            newOrder.order.m_clientId = 1;
            newOrder.order.m_orderId = 1;
            newOrder.order.m_permId = 1;
            newOrder.order.m_action = inAction; /*
             * buy/sell
             */
            newOrder.order.m_totalQuantity = howMany;
            newOrder.order.m_orderType = inOrderType; /*
             * market, limit etc
             */
            if (inOrderType.equals("LMT")) {
                newOrder.order.m_lmtPrice = myUtils.roundMe((float) price, 2);
            }
            newOrder.order.m_account = account;
            newOrder.ordNum = generateOrderNumber();
            System.out.println("setOrderInfo: price(round) = " + newOrder.order.m_lmtPrice);
            System.out.println("setOrderInfo: price = " + price);
            this.orderAdd(newOrder);

            System.out.println("setOrder (ordNum) " + newOrder.ordNum);
            System.out.println("setOrderInfo: howMany = " + howMany);
            return (storeIdx - 1);
        }

        public boolean placeOrder(int orderIndex) {
            orderData actOrder = new orderData();

            //actOrder = (orderData) orders.get(ordNum-1);
            actOrder = this.orderGet(orderIndex);
            m_client.placeOrder(actOrder.ordNum, actOrder.orderContract, actOrder.order);

            return (true);
        }

        public boolean cancelOrder(int ordNum) {
            orderData actOrder = new orderData();

            //actOrder = (orderData) orders.get(ordNum-1);
            actOrder = this.orderGet(ordNum);
            m_client.cancelOrder(actOrder.ordNum);
            System.out.println("cancelOrder (ordNum) " + actOrder.ordNum);
            return (true);
        }

        public void reqOpenOrders() {
            m_client.reqOpenOrders();
        }
    }
    public final class validBars {
        final String []barTable = {
            "1 secs",
            "5 secs",
            "10 secs",
            "15 secs",
            "30 secs",
            "1 min",
            "2 mins",
            "3 mins",
            "5 mins",
            "10 mins",
            "15 mins",
            "20 mins",
            "30 mins",
            "1 hour",
            "2 hours",
            "3 hours",
            "4 hours",
            "8 hour",
            "1 day",
            "1W",
            "1M"
        };
         
        public boolean validBars(String barIn){
            for(int i = 0; i <= barTable.length; i++){
                if (barIn.equals(barTable[i])){
                    return(true);
                }
            }
            return(false);
        }
    }
    public historicalData setActHistoricalData(historicalData withThis) {
        withThis = new historicalData();
        actHistoricalData = withThis;
        return(actHistoricalData);
    }
    public historicalData getActHistoricalData() {
        return actHistoricalData;
    }
    public class SlopeAnalysisDataPerDay{
        String thisDate;
        double slopeValue;
        double secondDerivativeSv;
        double secondDerivativeSvPercent;
        double slopeAvePercent;
        double slopeValuePercent;
        double todaysClose;
        String operation = "";
        String operationReal = "";
        double runningPl = 0.0;
        double openPosValue = 0.0;
        double openPosValueOverShoot = 0.0;
        int openPosCnt = 0;
        int openPosCntOverShoot = 0;
        double ma10Day = 0.0;
        double ma20Day = 0.0;
        double ma30Day = 0.0;
        double ma50Day = 0.0; 
        double ma100Day = 0.0;
        double ma150Day = 0.0;
        double ma200Day = 0.0;
        double useThisMa = 0.0;
        double percentWithInMa10Day = 0.0;
        double percentWithInMa20Day = 0.0;
        double percentWithInMa30Day = 0.0;
        double percentWithInMa50Day = 0.0; 
        double percentWithInMa100Day = 0.0;
        double percentWithInMa150Day = 0.0;
        double percentWithInMa200Day = 0.0;
        double percentAboveMa10Day = 0.0;
        double percentAboveMa20Day = 0.0;
        double percentAboveMa30Day = 0.0;
        double percentAboveMa50Day = 0.0; 
        double percentAboveMa100Day = 0.0;
        double percentAboveMa150Day = 0.0;
        double percentAboveMa200Day = 0.0;
        public SlopeAnalysisDataPerDay(){
            
        }
    }
    
    SlopeAnalysisData[] slopeAnalysisDataAllMas[];
    
    public class SlopeAnalysisData{
        String thisTicker;  
        int thisMA;
        double maxPosSlope;
        String maxPosSlopeDate;
        double maxNegSlope;
        String maxNegSlopeDate;
        double avePosSlope;
        double aveNegSlope;
        int posSlopeCnt;
        int negSlopeCnt;
        public int slopeCnt;
        double initialStockPrice1YearAgo;
        double initialStockPriceNow;
        
        
        //public int useThisMa = 0;
        SlopeAnalysisDataPerDay slopeAnalysisPerDay[];
        public SlopeAnalysisData(int sz){
            slopeAnalysisPerDay = new SlopeAnalysisDataPerDay[sz];
        }
    }
    public class Trade{
        double openPrice;
        double closePrice;
        boolean isItOpen;
        double pl;
    }
    public class PosManager{
        String ticker;
        int tradeScale;
        int totalSharesForBackTest;
        int totalSharesForNow;
        double initialStockPrice1YearAgo;
        double stockPriceNow;
        double initialPositionValue;
        double positionValueNow;
        double runningPl;
        double runningPlByLongs;
        double runningPlByShorts;
        int tradeCount = 0;
        int tradeGCount = 0;
        int tradeLCount = 0;
        double runningPlPercent;
        double endingStockPrice;
        double lastClosedPrice;
        String operationReal;
        String lastClosedOperation;
        double buyHoldPl;
        double buyHoldPlPercent;
        double openPosValue;
        double openPosValueOverShoot;
        int numOfOpenPos;
        int numOfClosedPos;
        int actTrade = 0;
        int actTradeOvershoot = 0;
        int sharesPerTradeForBackTest;
        int sharesPerTradeForNow;
        int sharesPerTradeForNowOverShoot;
        int sharesPerTradeForBackTestOverShoot;
        int overShootSharesPercent;
        boolean limitRiskNow = false;
        double currentStockPrice;
        //boolean allIn = false;
        //boolean allOut = true;
        double maxRunningGainForYear = 0.0;
        double maxRunningLossForYear = 0.0;
        String maxRunningGainForYearDate = "";
        String maxRunningLossForYearDate = "";
        double maxRunningPlusOpenGainForYear = 0.0;
        double maxRunningPlusOpenLossForYear = 0.0;
        String maxRunningPlusOpenGainForYearDate = "";
        String maxRunningPlusOpenLossForYearDate = "";
        
        Trade trades[];
        Trade tradesOverShoot[];
        SlopeAnalysisData slopeData;
        //remember slope value when position was opened..
        double lastSlopeValue;
        public boolean doWeLimitRiskNow(){
            return limitRiskNow;
        }
        public PosManager(String posTicker, int posTradeScale, 
                          int posSharesToStart1YearAgo, double posInitialStockPrice1YearAgo,
                          int posSharesToStartNow, double posInitialStockPriceNow)
        {
            /*
            Each position's trade is managed by this class. PosTradeScale defines how many trades it
            does to enter/exit a position. Say it's set to 4. This means the that if we are trading say 
            100 shares total per position, then we will trade 25 shares 4 times to totally enter a posiiton.
            These trades are FILO, first in last out. This manager keeps track of gains/losses for one position.
            PosSharesToStart defines how many total shares we will trade per position. posInitialClosingValue is 
            the very first closing value of the position.
            */   
            int x = 0;
            //final double dollarAmountPerPos = 8000.00;
            
            ticker = posTicker;
            tradeScale = posTradeScale;
            totalSharesForBackTest = posSharesToStart1YearAgo;
            totalSharesForNow = posSharesToStartNow;
            initialStockPrice1YearAgo = posInitialStockPrice1YearAgo; 
            stockPriceNow = posInitialStockPriceNow;
            //totalShares = (int) myUtils.roundMe((double)(dollarAmountPerPos / initialStockPrice), 2);                              
            initialPositionValue = (totalSharesForBackTest * initialStockPrice1YearAgo);
            //now set up for real trading..
            positionValueNow = (totalSharesForNow * stockPriceNow);
            backTestSharesPerTrade = (int) myUtils.roundMe((double)(totalSharesForBackTest / tradeScale), 2);
            sharesPerTradeForBackTestOverShoot = (int) myUtils.roundMe((double)(((double)overShootPercent / 100.0) * backTestSharesPerTrade), 2);
            sharesPerTradeForBackTest = (int) myUtils.roundMe((double)(totalSharesForBackTest / tradeScale), 2);
            sharesPerTradeForNow = (int) myUtils.roundMe((double)(totalSharesForNow / tradeScale), 2);           
            sharesPerTradeForNowOverShoot = (int) myUtils.roundMe((double)(((double)overShootPercent / 100.0) * sharesPerTradeForNow), 2);
            overShootSharesPercent = overShootPercent;
            
            /*
                trades contains the segmented purchases we will do entering a position.
                Sort of averaging in and averaging out. The PosTradScale defines how many small purchases 
                we make before we are totally in. Totally in is when we are at the last trade in the array. 
                I.e, if scale == 4, we are totally in if 0,1,2,3 are open. Also, this is a FILO, so First open is 
                the last to close. ActTrade says which one we are working on...
            */
            trades = new Trade[posTradeScale];
            tradesOverShoot = new Trade[posTradeScale];
            
            for(x = 0; x < posTradeScale; x++){
                trades[x] = new Trade();
                tradesOverShoot[x] = new Trade();
            }
            
        }
        public void trade(String operation, double price, String bias){
            
            double cost;
            double currentValue;
            double thisPl = 0.0;
            Trade trade = trades[actTrade];
            
            //see if open or close..
            if (operation.equals("open") == true) {
                if (trade.isItOpen == true) {
                    //want to open a position and this one is currently open, so go to next one if not all in..
                    if (actTrade < (tradeScale - 1)) {
                        actTrade++;
                        trade = trades[actTrade];
                    } else {
                        //allIn = true;
                        return;
                    }
                  
                }                 
                trade.openPrice = price;
                trade.isItOpen = true;
                trade.pl = 0.0;
                trade.closePrice = 0.0;
                tradeCount++;
            }else if (operation.equals("close") == true){
                if (trade.isItOpen == false){
                    //want to close a position and this one is already closed, so go to previos one if not all out..
                    if (actTrade > 0) {
                        actTrade--;
                        trade = trades[actTrade];
                    } else {
                        //nothing to close..
                        //allOut = true;
                        return;
                    }
                }                
                trade.closePrice = price; 
                cost = (backTestSharesPerTrade * trade.openPrice);
                currentValue = (backTestSharesPerTrade * trade.closePrice);
                if (bias.equals(slopeDefs.oBiasLongStr)){
                    thisPl = currentValue - cost;
                }else if (bias.equals(slopeDefs.oBiasShortStr)){
                    thisPl = cost - currentValue;
                }                
                //trade.pl += thisPl;
                trade.isItOpen = false;
                //allIn = false;
                runningPl += thisPl; 
                tradeCount++;
            }   
        }
        
        public void tradeOverShooting(String operation, double price, String bias){
            
            double cost;
            double currentValue;
            double thisPl = 0.0;
            Trade trade = trades[actTrade];
            
            //see if open or close..
            if (operation.equals("open") == true) {
                if (trade.isItOpen == true) {
                    //want to open a position and this one is currently open, so go to next one if not all in..
                    if (actTrade < (tradeScale - 1)) {
                        actTrade++;
                        trade = trades[actTrade];
                    } else {
                        //allIn = true;
                        return;
                    }
                  
                }                 
                trade.openPrice = price;
                trade.isItOpen = true;

                //see if overShoot is enabled, and first trade..
                if((overShootShares > 0) && (tradeCount > 0) && (trade.closePrice > 0.0)){
                    //not first trade, so process overShoot short side.
                    cost = (overShootShares * trade.closePrice);
                    currentValue = (overShootShares * trade.openPrice);
                    if(bias.equals(slopeDefs.oBiasLongStr)){
                        thisPl = (cost - currentValue);
                    }else if (bias.equals(slopeDefs.oBiasShortStr)){
                        thisPl = (currentValue - cost);
                    }                   
                    runningPl += thisPl; 
                    runningPlByShorts += thisPl;
                    trade.closePrice = 0.0;                    
                }else{
                    //first trade
                    trade.pl = 0.0;
                    trade.closePrice = 0.0;
                }
                tradeCount++;
            }else if (operation.equals("close") == true){
                if (trade.isItOpen == false){
                    //want to close a position and this one is already closed, so go to previos one if not all out..
                    if (actTrade > 0) {
                        actTrade--;
                        trade = trades[actTrade];
                    } else {
                        //nothing to close..
                        //allOut = true;
                        return;
                    }
                }                
                trade.closePrice = price; 
                cost = (backTestSharesPerTrade * trade.openPrice);
                currentValue = (backTestSharesPerTrade * trade.closePrice);
                if (bias.equals(slopeDefs.oBiasLongStr)){
                    thisPl = currentValue - cost;
                }else if (bias.equals(slopeDefs.oBiasShortStr)){
                    thisPl = cost - currentValue;
                }                
                //trade.pl += thisPl;
                trade.isItOpen = false;
                //allIn = false;
                runningPl += thisPl;
                runningPlByLongs += thisPl;
                tradeCount++;
            }   
        }
        public void tradeOverShootingLinear(String operation, double price, String bias){
            /*
            This trading routine is used to process longs and overshooting shorts, by when
            buying longs, will buy back any shorts first, then start buying longs. Likewise,
            when selling longs, it will sell any longs first, then if condition persists, will 
            also sell shorts.
            */
            
            double cost;
            double currentValue;
            double thisPl = 0.0;
            Trade trade = trades[actTrade];
            Trade tradeOverShoot = tradesOverShoot[actTradeOvershoot];
       
            //see if open or close..
            if (operation.equals("open") == true) {
                //check if any overshot shorts are open, if so buy shorts back first..areWeAllOut will always be true if
                //overshoot is not wanted/enabled...
                if (this.areWeAllOut(tradesOverShoot) == false) {
                    if (tradeOverShoot.isItOpen == false) {
                        //want to close an overShoot short position and this one is already closed, 
                        //so go to previos one, we know there's one open..                      
                        actTradeOvershoot--;
                        tradeOverShoot = tradesOverShoot[actTradeOvershoot];                                      
                    }                                                                     
                    tradeOverShoot.closePrice = price;
                    //open price is what se sold the short for (opened) in close phase..
                    //this is our cost.
                    cost = (overShootShares * tradeOverShoot.openPrice);
                        //current value is price now which will be closing price..
                    //and hopefully is a lower price for profit..
                    currentValue = (overShootShares * tradeOverShoot.closePrice);
                    if (bias.equals(slopeDefs.oBiasLongStr)) {
                        thisPl = cost - currentValue;
                    } else if (bias.equals(slopeDefs.oBiasShortStr)) {
                        thisPl = currentValue - cost;
                    }
                    //done closing short..flag it and return
                    tradeOverShoot.isItOpen = false;
                    runningPl += thisPl;
                    runningPlByShorts += thisPl;
                    if (thisPl > 0){
                        //count gain (greater than 0)
                        tradeGCount++;
                    }else{
                        //count loss (less than or equal to 0)
                        tradeLCount++;
                    }
                    tradeCount++;
                    tradeOverShoot.openPrice = 0;
                    //closed a short so just return
                    return;
                }
                
                //no shorts open so open long..if not all in already
                if (trade.isItOpen == true) {
                    //want to open a position and this one is currently open, so go to next one if not all in..
                    if (actTrade < (tradeScale - 1)) {
                        actTrade++;
                        trade = trades[actTrade];
                    } else {
                        //allIn = true;
                        //all in so leave..                        
                        return;
                    }
                }
                //open long..
                trade.openPrice = price;
                trade.closePrice = 0;
                trade.isItOpen = true;
                tradeCount++;               
            } else if (operation.equals("close") == true) {
                if (trade.isItOpen == false) {
                    //want to close a position and this one is already closed, so go to previos one if not all out..
                    if (actTrade > 0) {
                        actTrade--;
                        trade = trades[actTrade]; 
                        //fall thru and close long...
                    } else {
                        //nothing to close in longs..try opening shorts, if not all in already AND overShootShares are wanted/enabled..
                        if ((overShootShares > 0) && (this.areWeAllIn(tradesOverShoot) == false)) {                            
                            if(tradeOverShoot.isItOpen == true){
                                //want to open a short position and this one is currently open, 
                                //so go to next one 
                                actTradeOvershoot++;
                                tradeOverShoot = tradesOverShoot[actTradeOvershoot];                                
                            }
                            //open short and split..
                            tradeOverShoot.openPrice = price;
                            tradeOverShoot.isItOpen = true; 
                            tradeOverShoot.closePrice = 0;
                            return;
                        }else{
                            //nothing to close in longs and shorts are all in so do nothing...
                            //here                           
                            return;
                        }
                        
                    }
                }
                //close open position..
                trade.closePrice = price; 
                cost = (backTestSharesPerTrade * trade.openPrice);
                currentValue = (backTestSharesPerTrade * trade.closePrice);
                if (bias.equals(slopeDefs.oBiasLongStr)){
                    thisPl = currentValue - cost;
                }else if (bias.equals(slopeDefs.oBiasShortStr)){
                    thisPl = cost - currentValue;
                }                               
                trade.isItOpen = false;                
                runningPl += thisPl;
                runningPlByLongs += thisPl;
                if (thisPl > 0) {
                    //count gain (greater than 0)
                    tradeGCount++;
                } else {
                    //count loss (less than or equal to 0)
                    tradeLCount++;
                }
                tradeCount++;
                trade.openPrice = 0;
            }
        }
        
        public void tradeOverShootingLinearLimitRisk(String operation, double price, String bias){
            /*
            This trading routine is used to process longs and overshooting shorts, by when
            buying longs, will buy back any shorts first, then start buying longs. Likewise,
            when selling longs, it will sell any longs first, then if condition persists, will 
            also sell shorts.
            */
            
            double cost;
            double currentValue;
            double thisPl = 0.0;
            Trade trade = trades[actTrade];
            Trade tradeOverShoot = tradesOverShoot[actTradeOvershoot];
       
            //see if open or close..
            if (operation.equals("open") == true) {
                //to limit Risk we don't open anything...should not get here..
            } else if (operation.equals("close") == true) {
                if (trade.isItOpen == false) {
                    //want to close a position and this one is already closed, so go to previos one if not all out..
                    if (actTrade > 0) {
                        actTrade--;
                        trade = trades[actTrade]; 
                        //fall thru and close long...
                    } else {
                        //no more open longs to close, check for closing shorts..
                        if (this.areWeAllOut(tradesOverShoot) == false) {
                            if (tradeOverShoot.isItOpen == false) {
                                //want to close an overShoot short position and this one is already closed, 
                                //so go to previos one, we know there's one open..                      
                                actTradeOvershoot--;
                                tradeOverShoot = tradesOverShoot[actTradeOvershoot];
                            }
                            tradeOverShoot.closePrice = price;
                            //open price is what se sold the short for (opened) in close phase..
                            //this is our cost.
                            cost = (overShootShares * tradeOverShoot.openPrice);
                            //current value is price now which will be closing price..
                            //and hopefully is a lower price for profit..
                            currentValue = (overShootShares * tradeOverShoot.closePrice);
                            if (bias.equals(slopeDefs.oBiasLongStr)) {
                                thisPl = cost - currentValue;
                            } else if (bias.equals(slopeDefs.oBiasShortStr)) {
                                thisPl = currentValue - cost;
                            }
                            //done closing short..flag it and return
                            tradeOverShoot.isItOpen = false;
                            runningPl += thisPl;
                            runningPlByShorts += thisPl;
                            tradeCount++;
                            tradeOverShoot.openPrice = 0;
                            //closed a short so just return
                            return;
                        }else{
                            //we are all out of shorts leave..
                            return;
                        }
                        
                    }
                }
                //close open position..
                trade.closePrice = price;
                cost = (backTestSharesPerTrade * trade.openPrice);
                currentValue = (backTestSharesPerTrade * trade.closePrice);
                if (bias.equals(slopeDefs.oBiasLongStr)){
                    thisPl = currentValue - cost;
                }else if (bias.equals(slopeDefs.oBiasShortStr)){
                    thisPl = cost - currentValue;
                }                               
                trade.isItOpen = false;                
                runningPl += thisPl;
                runningPlByLongs += thisPl;
                tradeCount++;
                trade.openPrice = 0;               
            }
        }        
        
        
        
        
        
        
        public double getOpenPosValue(double currentStockPrice, String bias)
        {
            int x;
            double openV = 0.0;
            for (x = 0; x < tradeScale; x++){
                if(trades[x].isItOpen){
                    if(bias.equals(slopeDefs.oBiasLongStr)){
                        openV +=  ((currentStockPrice * this.sharesPerTradeForBackTest) - (trades[x].openPrice * this.sharesPerTradeForBackTest)); 
                    }else{
                        openV +=  ((trades[x].openPrice * this.sharesPerTradeForBackTest) - (currentStockPrice * this.sharesPerTradeForBackTest)); 
                    }         
                }
            }
            return openV;
        }
        public double getOpenPosValue(Trade trades[], double currentStockPrice, String bias)
        {
            int x;
            double openV = 0.0;
            for (x = 0; x < tradeScale; x++){
                if(trades[x].isItOpen){
                    if(bias.equals(slopeDefs.oBiasLongStr)){
                        //openPrice is what we sold it for..currentStockPrice is what it's worth now..
                        openV +=  ((trades[x].openPrice * overShootShares) - (currentStockPrice * overShootShares)); 
                    }else{
                        openV +=  ((currentStockPrice * overShootShares) - (trades[x].openPrice * overShootShares)); 
                    }         
                }
            }
            return openV;
        }
        public int getNumOfPosOpened(Trade trades[]){
            int x;
            int cnt = 0;
            for (x = 0; x < tradeScale; x++) {
                if (trades[x].isItOpen) {
                    cnt++;
                }
            }
            return cnt;
        }
        public int getNumOfPosOpened(){
            int x;
            int cnt = 0;
            for (x = 0; x < tradeScale; x++){
                if(trades[x].isItOpen){
                    cnt++;
                }
            }
            return cnt;
        }
        
        public boolean areWeAllIn(boolean linear){
            boolean longAllin = false;
            boolean shortAllout = false;
            boolean result = false;
            if (linear == true){
                longAllin = (howManyOpen(trades) == tradeScale);
                shortAllout = (howManyOpen(tradesOverShoot) == 0);
                result = (longAllin && shortAllout);
            }else{
                result = (howManyOpen() == tradeScale);
            }            
            return (/*result*/false); //fuck
        }        
        public boolean areWeAllIn(){
            return (/*allIn = */(howManyOpen() == tradeScale));
        }
        
        public boolean areWeAllIn(Trade trades[]){
            return (howManyOpen(trades) == tradeScale);
        }
        public boolean areWeAllOut(Trade trades[]){
            return (howManyOpen(trades) == 0);
        }
        public boolean areWeAllOut(){
            return (/*allOut = */(howManyOpen() == 0));
        }
        public boolean areWeAllOut(boolean linear){
            boolean longAllout = false;
            boolean shortAllin = false;
            boolean result = false;
            if(linear == true){
               longAllout= (howManyOpen(trades) == 0);
                shortAllin = (howManyOpen(tradesOverShoot) == tradeScale); 
               result = (longAllout && shortAllin);
            }else{
               result = (howManyOpen(trades) == 0);
            }                       
            return (/*result*/false); //fuck           
        }
        /*
        public boolean areWeAllOut(Trade trades[]){
            return (howManyOpen(trades) == 0);
        }   
        */
        private int howManyOpen(){
            int i =0;
            int cnt = 0;
            for (i = 0; i < tradeScale; i++){
                if (trades[i].isItOpen == true){
                    cnt++;
                }
            }
            return cnt;
        }
        private int howManyOpen(Trade trades[]){
            int i =0;
            int cnt = 0;
            for (i = 0; i < tradeScale; i++){
                if (trades[i].isItOpen == true){
                    cnt++;
                }
            }
            return cnt;
        }
    }
    
    public class historicalData {
        final int MAX_WAIT = 300;
        public int size = 0;
        public int fetchLoc = 0;
        public int storeLoc = 0;
        public boolean filledDone = false;
        public boolean fuckError = false;
        public Vector vecData = new Vector(10,10);
        public data actData = new data();
        public HistorySlope actWally = new HistorySlope();
        public String forThisTicker = "";
        float percentAbove50ma = 0;
        float percentAbove100ma = 0;
        float percentAbove150ma = 0;
        float percentAbove200ma = 0;
        public double max10MaPercent = 0.0;
        public double max20MaPercent = 0.0;
        public double max50MaPercent = 0.0;
        public double max100MaPercent = 0.0;
        public double max200MaPercent = 0.0;
        public String max10MaPercentDate = "";
        public String max20MaPercentDate = "";
        public String max50MaPercentDate = "";
        public String max100MaPercentDate = "";
        public String max200MaPercentDate = "";
        int timesAbove50ma = 0;
        int timesAbove100ma = 0;
        int timesAbove200ma = 0;
        int timesTouched50ma = 0;
        int timesTouched100ma = 0;
        int timesTouched200ma = 0;
        int timesPierced50ma = 0 ;
        int timesPierced100ma = 0;
        int timesPierced200ma = 0;
        String endDate50Day = "";
        String endDate100Day = "";
        String endDate200Day = "";
        double percentWithin50ma = 0;
        double percentWithin100ma = 0;
        double percentWithin150ma = 0;
        double percentWithin200ma = 0;
        int windowSize50ma = 0;
        int windowSize100ma = 0;
        int windowSize200ma = 0;
        public double todaysRsi = 0.0;
        public double rsiMa = 0.0;
        public double bollingerBandsHi = 0.0;
        public double bollingerBandsLow = 0.0;
        public double aveVolume30Day = 0.0;
        public double aveVolume60Day = 0.0;
        public double aveVolume90Day = 0.0;
        public double todaysPrevCloseToTodayOpen = 0.0;
        public double todaysOpenToLast = 0.0;
        public double stdDev50Day = 0.0;
        public double stdDev100Day = 0.0;
        public double stdDev200Day = 0.0;
        public double vol50Day = 0.0;
        public double vol100Day = 0.0;
        public double vol200Day = 0.0;
        public int transitionsRelativeTo10DayMaCount = 0;
        public double transitionsRelativeTo10DayMaPercent = 0;
        boolean allGood = true;
        public String errorMsg = null;
        public String endingDate = null;
        public double yearsGainLoss = 0.0;
		public double currentPriceRelativeTo1ma = 0.0;
		public double currentPriceRelativeTo2ma = 0.0;
		public double currentPriceRelativeTo3ma = 0.0;
		public double currentPriceRelativeTo4ma = 0.0;
		public double currentPriceRelativeTo5ma = 0.0;
        public double currentPriceRelativeTo10ma = 0.0;
		public double currentPriceRelativeTo15ma = 0.0;
        public double currentPriceRelativeTo50ma = 0.0;
        public double currentPriceRelativeTo100ma = 0.0;
        public double currentPriceRelativeTo150ma = 0.0;
        public double currentPriceRelativeTo200ma = 0.0;
        public double currentPriceRelativeTo20ma = 0.0;
		public double currentPriceRelativeTo25ma = 0.0;
        public double currentPriceRelativeTo30ma = 0.0;
		public double currentPriceRelativeTo35ma = 0.0;
		public double currentPriceRelativeTo40ma = 0.0;
		public double currentPriceRelativeTo60ma = 0.0;
		public double currentPriceRelativeTo70ma = 0.0;
	public double currentPriceRelativeTo500ma = 0.0;
        public double lastClosedPrice = 0.0;
        public double initialStockPrice = 0.0;
        public String todaysDate = null;
        public String backTestDate = null;
        public int tidSeed = 1000;
        public int tid = tidSeed;
        public String selectedYear = "";
		public String selectedTime = "13:15:00";
        public String selectedDuration = "";
        public String SelectedBarSize = "";
        public int backTestSizeInBars = 0;
        public SlopeLog actSlopeLog= new SlopeLog(this.size);
        //store all slopeValues for ticker here..slopeCnt counts them..
        //public SlopeAnalysisData slopeAnalysisData;
        public int slopeCnt = 0;

        public void setSelectedYear(String yearIn){
            selectedYear = yearIn;
        }
		public void setSelectedTime(String timeIn){
            selectedTime = timeIn;
        }
        public void setSelectedDuration(String durationIn){
            selectedDuration = durationIn;
        }
        public void setSelectedBarSize(String barIn){
            SelectedBarSize = barIn;
        }
        public void setSelectedBackTestSizeInBars(int sz){
            backTestSizeInBars = sz;
        }
        public double getPercentWithin(int percent) {
            double value = 0.0;
            String dispErrNaN = "getPercentWithin error: NaN!!";
            switch (percent) {
                case 50:
                    if(Double.isNaN(percentWithin50ma) || Double.isInfinite(percentWithin50ma)){
                        System.out.println("\n" + dispErrNaN);
                    }else{
                        value = myUtils.roundMe(percentWithin50ma, 2);
                    } 
                    break;
                case 100:                    
                    if(Double.isNaN(percentWithin100ma) || Double.isInfinite(percentWithin100ma)){
                        System.out.println("\n" + dispErrNaN);                        
                    }else{
                        value = myUtils.roundMe(percentWithin100ma, 2);
                    } 
                    break;
                case 150:                    
                    if(Double.isNaN(percentWithin150ma) || Double.isInfinite(percentWithin150ma)){
                        System.out.println("\n" + dispErrNaN);
                    }else{
                        value = myUtils.roundMe(percentWithin150ma, 2);
                    }    
                    break;
                case 200:
                    if(Double.isNaN(percentWithin200ma) || Double.isInfinite(percentWithin200ma)){
                        System.out.println("\n" + dispErrNaN);
                    }else{
                        value = myUtils.roundMe(percentWithin200ma, 2);
                    }                    
                    break;
                default:
                    value = 0;
            }
            return value;
        }
        public double getPercentAbove(int percent){
            double value = 0.0;
            switch (percent) {
                case 50:    value = ((Double.isNaN(percentAbove50ma) == true) ? (value = 0) : myUtils.roundMe(percentAbove50ma, 2));
                    break;
                case 100:   value = ((Double.isNaN(percentAbove100ma) == true) ? (value = 0) : myUtils.roundMe(percentAbove100ma, 2));
                    break;
                case 150:   value = ((Double.isNaN(percentAbove150ma) == true) ? (value = 0) : myUtils.roundMe(percentAbove150ma, 2));
                    break;
                case 200:   value = ((Double.isNaN(percentAbove200ma) == true) ? (value = 0) : myUtils.roundMe(percentAbove200ma, 2));
                    break;
                default:    value = 0;
            }
            return value;
        }
        public int getTimesAbove(int ma){
            int value = 0;
            switch (ma) {
                case 50:    value = timesAbove50ma;
                    break;
                case 100:   value = timesAbove100ma;
                    break;
                case 200:   value = timesAbove200ma;
                    break;
                default:    value = 0;
            }
            return value;
        }
        public int getTimesTouched(int ma){
            int value = 0;
            switch (ma) {
                case 50:    value = timesTouched50ma;
                    break;
                case 100:   value = timesTouched100ma;
                    break;
                case 200:   value = timesTouched200ma;
                    break;
                default:    value = 0;
            }
            return value;
        }
        public int getTimesPierced(int ma){
            int value = 0;
            switch (ma) {
                case 50:    value = timesPierced50ma;
                    break;
                case 100:   value = timesPierced100ma;
                    break;
                case 200:   value = timesPierced200ma;
                    break;
                default:    value = 0;
            }
            return value;
        }
        public historicalData() {
            todaysDate = myUtils.GetTodaysDate("yyyyMMdd HH:mm:ss");
            //todaysDate = myUtils.reverseDate(todaysDate);
            System.out.println("today's date: " + todaysDate);
            
        }
        public String getErrorMsg(){
            return(errorMsg);
        }
        public String getEndDate(){
            return(endingDate);
        }
        public String get50DayEndDate(){
            return(endDate50Day);
        }
        public String get100DayEndDate(){
            return(endDate100Day);
        }
        public String get200DayEndDate(){
            return(endDate200Day);
        }
        public void nextTid(int id) {
            tid = tidSeed + id;
        }
        public String getTodaysDate(){
            return(todaysDate);
        }
        public void setBackTestDate(String datein){
            backTestDate = datein;
        }
        public boolean getHistoricalData(String ticker) {
            String testDate = "20150309";
            int giveUp = MAX_WAIT;
            final int TRADING_DAYS_1YR = 252;
            int idx = 100;
            forThisTicker = ticker;
            m_contract = new Contract();
            m_contract.m_symbol = ticker;
            m_contract.m_secType = "STK";
            m_contract.m_strike = 0;
            m_contract.m_exchange = "SMART";
            m_contract.m_primaryExch = "ISLAND";
            m_contract.m_currency = "USD";

            //actHistoricalData = new historicalData();
            /* get first year..*/
            m_client.reqHistoricalData(tid, m_contract,
                    //"20140802 13:15:00", /* endDate */
                    //todaysDate+ " 13:15:00", /* endDate */
                    selectedYear+ " " + selectedTime /*13:15:00"*/, 
                    // "60 D", or "23400 S"              /* duration */
                    selectedDuration, /* duration */
                    //"1 day", /* bar size */
                    SelectedBarSize, /* bar size */
                    "TRADES", /* what to show */
                    1, /* RTH (reg trade hours)1 == RTH */
                    1 /* format date yyyymmdd hh:mm:ss*/
            );
            while ((actHistoricalData.filledDone == false) && (true/*actHistoricalData.fuckError == false*/) && (giveUp > 0)) {
                myUtils.delay(50);
                giveUp--;
            }
            
            if ((giveUp > 0) && (/*actHistoricalData.fuckError == false*/true)) {
                /* get second year.. */
                System.out.println("filled up!! Cool!! Ticker: " + ticker);
                allGood = true;
            } else {
                m_client.cancelHistoricalData(tid);
                System.out.println("gave up!! Not Cool!! Ticker: " + ticker);
                errorMsg = "error waiting for fill to complete.";
                allGood = false;
            }
            if (allGood == true){
                /* must have full year of data or else forget it.. 3.11.2016 change to half trading year..*/
                if (actHistoricalData.getSize() >= (backTestSizeInBars - 1)) {
                    allGood = true;
                } else {
                    //sometimes we get filledDone, but all messages didn't come in so wait..
                    System.out.println("\nfilledDone == true but size is short...wait for more messages.. Ticker: " + ticker);
                    while(idx > 0){
                        myUtils.delay(10);
                        idx--;
                    }
                    System.out.println("\nWaited done..");
                    if (actHistoricalData.getSize() >= (backTestSizeInBars - 1)) {
                        allGood = true;
                        System.out.println("\nAll good now.");
                    }else{
                        System.out.println("\nStill not long enough!! Ticker: " + ticker);
                        allGood = false;//fuckit ..fix this should work so set back to TRUE and trouble shoot..
                        errorMsg = "trading history not long enough:" + actHistoricalData.getSize();
                        System.out.println("\nactHistoricalData.getSize() is " + actHistoricalData.getSize() + " not long enough.");                        
                    } 
                }
            }
            //m_client.cancelHistoricalData(tid);
            tidSeed++;
            return(allGood);
        }
        int calcTradingDays(String barIn, String durIn) {
            final int TRADING_DAYS_PER_YEAR = 252;
            final int TRADING_WEEKS_PER_YEAR = 52;
            final int TRADING_MONTHS_PER_YEAR = 12;
            /*
             calculate valid number of trading days based on barSize and Duration. 
             i.e barSz 1 day, duration 1 year:
             validTrading Days = 252 (trading days in year) * 1 == 252
             Or barSz 1 day, duration 2 year:
             validTrading Days = 252 (trading days in year) * 2 == 504
             Or barSz 1 week, duration 4 year:
             validTrading Days = 52 (trading weeks in year) * 4 == 208
             */
            //remove chars only want int, then convert string to int..
            int barSzNumber = Integer.parseInt(barIn.replaceAll("\\D+", ""));
            String barSzString = barIn.replaceAll("[^A-Za-z]+", "");
            int durSzNumber = Integer.parseInt(durIn.replaceAll("\\D+", ""));
            String durSzString = durIn.replaceAll("[^A-Za-z]+", "");
            int retValue = 0;
            switch (barSzString){
                case "day":
                    retValue =  (TRADING_DAYS_PER_YEAR * durSzNumber);
                    break;
                case "week":
                    retValue =  (TRADING_WEEKS_PER_YEAR * durSzNumber);
                    break;
                case "month":
                    retValue =  (TRADING_MONTHS_PER_YEAR * durSzNumber);
                    break;
                default:
                    break;
                
            }
            return retValue;
        }
        public boolean getHistoricalData(String ticker, String duration, String barSz) {
            
            int giveUp = MAX_WAIT;
            int validTradingDays = calcTradingDays(barSz, duration);
            forThisTicker = ticker;
            m_contract = new Contract();
            m_contract.m_symbol = ticker;
            m_contract.m_secType = "STK";
            m_contract.m_strike = 0;
            m_contract.m_exchange = "SMART";
            m_contract.m_primaryExch = "ISLAND";
            m_contract.m_currency = "USD";

            //actHistoricalData = new historicalData();
            /* get first year..*/
            m_client.reqHistoricalData(tid, m_contract,
                    //"20140802 13:15:00", /* endDate */
                    todaysDate,/*+ " 13:15:00",  endDate */                    
                    // "60 D",                  /* duration */
                    duration, //"4 Y", /* duration */
                    //"1 day", /* bar size */
                    barSz, /* bar size */
                    "TRADES", /* what to show */
                    1, /* RTH (reg trade hours) */
                    1 /* format date yyyymmdd hh:mm:ss*/
            );
            while ((actHistoricalData.filledDone == false) && (giveUp > 0)) {
                myUtils.delay(50);
                giveUp--;
            }
            if (giveUp > 0) {
                /* get second year.. */
                System.out.println("filled up!! Cool!!");
                allGood = true;

            } else {
                m_client.cancelHistoricalData(tid);
                System.out.println("gave up!! Not Cool!!");
                errorMsg = "error waiting for fill to complete.";
                allGood = false;
            }
            if (allGood == true){
                /* must have full year of data or else forget it.. */
                if (actHistoricalData.getSize() >= (validTradingDays - 1)) {
                    allGood = true;
                } else {
                    allGood = false;
                    errorMsg = "trading history not long enough:" + actHistoricalData.getSize();
                }
            }
            //m_client.cancelHistoricalData(tid);
            tidSeed++;
            return(allGood);
        }
        public void restartAccess(){
            fetchLoc = size - 1;
            storeLoc = 0;
        }
        public void resetData() {
            vecData.clear();
            fetchLoc = 0;
            storeLoc = 0;
            size = 0;
        }

        public data getNext() {
            data d = null;
            if (fetchLoc > size) {
                fetchLoc = 0;
                return (null);
            }
            if(fetchLoc == size){
                System.out.println("\n..");
            }
            if(fetchLoc < 0){
                System.out.println("\nShit!!");
            }
            d = (data) vecData.elementAt(fetchLoc);
            fetchLoc--;
            return (d);
        }

        public void storeToNext(data din) {
            vecData.add((data) din);
            actData = new data();
            fetchLoc = storeLoc;
            storeLoc++;
            size++;
        }

        public int getSize() {
            return (size);
        }
        public List<data> getHistData(){
            int x;
            List<data> retList = new ArrayList<data>();
            int saveSz = this.size;
            this.restartAccess();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < saveSz; x++) {
               retList.add(this.getNext());
            }
            this.resetData();
            this.filledDone = false;
            //this.fuckError = false;
            return retList;
        }
		
		private class Range{
			int min;
			int max;
		}
		private List<Range> skipRanges = new ArrayList<Range>();
		
		private List<Range> skipTheseRanges(data din[], int avein){
			/*
			This is for moving average calculations when intraday trading is active.
			go through entire history data and create range of bars that should not be used during intra day trading.
			every time 06:30:00 is encountered (begining of day) don't use the first x bars from first of day because moving average
			cannot be calculated yet.
			*/
			int x = 0;
			List<Range> retSkipRange = new ArrayList<Range>();
			Range range = null;
			/*
			this routine will look at begining of the trading day where the moving ave cannot be calculated.
			for instance, for 10bar moving average, from 630 - 640 am is skiped. you need atleast 10 to calculate ave
			*/
			//cycle from oldest date to today..
			for(x = (din.length - 2); x >=0; x--){
				if(din[x].date.contains("06:30:00")){
					//found beg of range to skip
					range = new Range();
					range.max = x;
					range.min = (x - avein);
					retSkipRange.add(range);
				}					
			}
			return retSkipRange;
		}
		private boolean doWeSkip(int indxin, List<Range> rangein){
			Range actRange = null;
			int indx = 0;
			for(indx = 0; indx < rangein.size(); indx++){
				actRange = rangein.get(indx);
				if(myUtils.withInRangeInclusive(indxin, actRange.min, actRange.max) == true){
					return true;
				}
			}
			return false;
		} 
		private double [] fillme(data[] dArrin, int indexin, int lenin){
			/*
			fill up ret array with closing values of dArrin of length len;
			index 0 is most recent index len is most past date.
			*/
			double retSet[] = new double[lenin];
			int x;
			for (x = indexin; x < (indexin + lenin); x++){
				retSet[x - indexin] = dArrin[x].close;
			}
			return retSet;
		} 
		public void calcSimpleMovingAve(int whatAve) {
			/* calculate the whatAve bar MA. The vector structure is FILO
               so we fetch from the most recently stored location.
			 */
			int x = 0;
			int y = 0;
			//data dArr[] = new data[this.size + 1];
			data dArr[] = new data[this.size];
			int saveSz = this.size;
			double tmpPrice = 0.0;
			boolean weAreDone = false;
			double tmpYearGain = 0.0;
			boolean intraDay = false;
			
			int skipCount = 0;
			if (saveSz == 0) {
				System.out.println("calcMovingAve:" + "ERROR! size = " + this.size);
				return;
			} else if (whatAve > saveSz) {
				System.out.println("calcMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
				return;
			}
			data tmpData = new data();
			/* put data from vec into array for easier manipulating */
			for (x = 0; x < this.size; x++) {
				dArr[x] = new data();
				dArr[x] = this.getNext();
			}
			//0 == today, saveSz is year ago..
			tmpYearGain = dArr[0].close - dArr[saveSz - 1].close;
			lastClosedPrice = dArr[0].close;
			this.yearsGainLoss = (tmpYearGain / dArr[0].close) * 100.0;
			//see if this is intraday trading..if date includes time and date then intra day is true..
			if ((intraDay = (dArr[0].date.length() > 8))) {
				//intra day trading so create skip list for MA calcuations..
				skipRanges = skipTheseRanges(dArr, whatAve);
			}

			for (y = 0; (weAreDone == false); y++) {
				if ((intraDay == true) && (doWeSkip(y, skipRanges) == true)) {
					continue;
				}
				for (x = 0; x < whatAve; x++) {
					if ((x + y) < this.size) {
						tmpPrice += dArr[x + y].close;
					} else {
						System.out.println("calcMovingAve:" + "ERROR! size = " + this.size);
					}
				}
				
				switch (whatAve) {
					case 1:
						dArr[y].mA1Day = tmpPrice / whatAve;
						break;
					case 2:
						dArr[y].mA2Day = tmpPrice / whatAve;
						break;
					case 3:
						dArr[y].mA3Day = tmpPrice / whatAve;
						break;
					case 4:
						dArr[y].mA4Day = tmpPrice / whatAve;
						break;
					case 5:
						dArr[y].mA5Day = tmpPrice / whatAve;
						break;
					case 50:
						dArr[y].mA50Day = tmpPrice / whatAve;
						break;
					case 100:
						dArr[y].mA100Day = tmpPrice / whatAve;
						break;
					case 150:
						dArr[y].mA150Day = tmpPrice / whatAve;
						break;
					case 200:
						dArr[y].mA200Day = tmpPrice / whatAve;
						break;
					case 10:
						dArr[y].mA10Day = tmpPrice / whatAve;
						break;
					case 15:
						dArr[y].mA15Day = tmpPrice / whatAve;
						break;
					case 20:
						dArr[y].mA20Day = tmpPrice / whatAve;
						break;
					case 25:
						dArr[y].mA25Day = tmpPrice / whatAve;
						break;
					case 30:
						dArr[y].mA30Day = tmpPrice / whatAve;
						break;
					case 35:
						dArr[y].mA35Day = tmpPrice / whatAve;
						break;
					case 40:
						dArr[y].mA40Day = tmpPrice / whatAve;
						break;
					case 60:
						dArr[y].mA60Day = tmpPrice / whatAve;
						break;
					case 70:
						dArr[y].mA70Day = tmpPrice / whatAve;
						break;
					case 500:
						dArr[y].mA500Day = tmpPrice / whatAve;
						break;
					default:
						System.out.println("error: calcMovingAve");
				}

				tmpPrice = 0.0;
				/* stop when we are at the end of year's worth */
				if ((x + y) >= saveSz) {
					weAreDone = true;
					this.endingDate = dArr[y].date;
				}
			}
			//store current price relative to MA. So priceToday minus 10/50/100/200MA..
			switch (whatAve) {
				case 1:
					this.currentPriceRelativeTo1ma = (dArr[0].close - dArr[0].mA1Day);
					break;
				case 2:
					this.currentPriceRelativeTo2ma = (dArr[0].close - dArr[0].mA2Day);
					break;
				case 3:
					this.currentPriceRelativeTo3ma = (dArr[0].close - dArr[0].mA3Day);
					break;
				case 4:
					this.currentPriceRelativeTo4ma = (dArr[0].close - dArr[0].mA4Day);
					break;
				case 5:
					this.currentPriceRelativeTo5ma = (dArr[0].close - dArr[0].mA5Day);
					break;
				case 50:
					this.currentPriceRelativeTo50ma = (dArr[0].close - dArr[0].mA50Day);
					break;
				case 100:
					this.currentPriceRelativeTo100ma = (dArr[0].close - dArr[0].mA100Day);
					break;
				case 150:
					this.currentPriceRelativeTo150ma = (dArr[0].close - dArr[0].mA150Day);
					break;
				case 200:
					this.currentPriceRelativeTo200ma = (dArr[0].close - dArr[0].mA200Day);
					break;
				case 10:
					this.currentPriceRelativeTo10ma = (dArr[0].close - dArr[0].mA10Day);
					break;
				case 15:
					this.currentPriceRelativeTo15ma = (dArr[0].close - dArr[0].mA15Day);
					break;
				case 20:
					this.currentPriceRelativeTo20ma = (dArr[0].close - dArr[0].mA20Day);
					break;
				case 25:
					this.currentPriceRelativeTo25ma = (dArr[0].close - dArr[0].mA25Day);
					break;
				case 30:
					this.currentPriceRelativeTo30ma = (dArr[0].close - dArr[0].mA30Day);
					break;
				case 35:
					this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA35Day);
					break;
				case 40:
					this.currentPriceRelativeTo40ma = (dArr[0].close - dArr[0].mA40Day);
					break;
				case 60:
					this.currentPriceRelativeTo60ma = (dArr[0].close - dArr[0].mA60Day);
					break;
				case 70:
					this.currentPriceRelativeTo70ma = (dArr[0].close - dArr[0].mA70Day);
					break;
				case 500:
					this.currentPriceRelativeTo500ma = (dArr[0].close - dArr[0].mA500Day);
					break;
				default:
					System.out.println("error: current Price relative to MA..");
			}
			this.resetData();
			/* store all back in vector now.. */
			for (x = (saveSz - 1); x >= 0; x--) {
				this.storeToNext(dArr[x]);
			}
			System.out.println("done.");

		}
		private double wma(double[] setin, int lenin){
			/*
				calculate weighted moving average on setin[] of length lenin;
				weighted moving average:
					wma = [(cp(p1) * w1) + (cp(p2) * w2) + (cp(p3) * w3) .. (cp(pn) * wn)] DIV (w1 + w2 + w3 + wn)
					cp == closing price, 1 == recent date, 2 == recent date - 1, 3 == recent date - 2..etc
					p == position, 1 == recent date, 2 == recent date - 1 ..
					wx == ABS(position - n) + n
				example: 
					cp today: 10.5, cp yesterday: 12.5, cp three days ago: 14.1
					n == 3 for three day moving average
					(10.5 * 3) + (12.5 * 2) + (14.1 * 1) / (3 + 2 + 1) == 11.76
					SMA	would equal : 12.36
			*/
			int x;
			double weightFactor;
			double sumWeightFactor = 0.0;
			double wmaVal = 0.0;
			double tmpPrice = 0.0;
			for (x = 0; x < lenin; x++) {				
				weightFactor = (lenin - (x));
				sumWeightFactor += weightFactor;
				tmpPrice += (setin[x] * weightFactor);
			}
			wmaVal = (tmpPrice / sumWeightFactor);
			return wmaVal;
		}
		public void calcHullMovingAve1(int whatAve){
			/* calculate the whatAve bar WMA. The vector structure is FILO
               so we fetch from the most recently stored location.
			   hull moving average:
			   hma = (2 * wma(close, whatave/2) - wma(close, whatave)
			   hma = wma(hma, sqrt whatave)
			   example: 
				
				whatAve is the number of bars we want to average out. whatAve of 10 would be today thru back 10 days.
				below darr[0] is today. darr[1] is yesterday etc.
            */
		
            int x = 0;
            int y = 0;
			data dArr[] = new data[this.size];
            int saveSz = this.size;
            double tmpPrice = 0.0;
            boolean weAreDone = false;
            double tmpYearGain = 0.0;
            boolean intraDay = false;
			double dset[] = new double[whatAve];
			double wmaSlow = 0.0;
			double wmaFast = 0.0;
			double hullSet[] = new double[Math.toIntExact(Math.round(Math.sqrt(whatAve)))];
			int fastLength = 0;
			long tsqrt = 0;
			int tsqrtint = Math.toIntExact(tsqrt = Math.round(Math.sqrt(whatAve)));;
            if (saveSz == 0){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }            
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            //0 == today, saveSz is year ago..
            tmpYearGain = dArr[0].close - dArr[saveSz - 1].close;
            lastClosedPrice = dArr[0].close;           
            this.yearsGainLoss = (tmpYearGain / dArr[0].close) * 100.0;  
			//see if this is intraday trading..if date includes time and date then intra day is true..
			if((intraDay = (dArr[0].date.length() > 8))){
				//intra day trading so create skip list for MA calcuations..
				skipRanges = skipTheseRanges(dArr, whatAve);
			}
			fastLength = Math.round((whatAve / 2));
			tsqrtint = Math.toIntExact(tsqrt = Math.round(Math.sqrt(whatAve)));
			for (y = 0; (weAreDone == false); y++) {
				for (x = 0; x < tsqrtint && (!weAreDone); x++) {
					/* stop when we are at the end of year's worth */
					if ((x + y + whatAve) >= saveSz) {
						weAreDone = true;
						this.endingDate = dArr[y].date;
					}
					//fill half set first
					dset = fillme(dArr, (x + y + fastLength), fastLength);
					wmaFast = wma(dset, fastLength);
					//now fill full length
					dset = fillme(dArr, (x + y), whatAve);
					wmaSlow = wma(dset, whatAve);
					hullSet[x] = ((wmaFast * 2) - wmaSlow);
				}
				if (weAreDone == true){
					continue;
				}
				tmpPrice = wma(hullSet, tsqrtint);
				switch (whatAve) {
					case 50:
						dArr[y].mA50Day = tmpPrice;
						break;
					case 100:
						dArr[y].mA100Day = tmpPrice;
						break;
					case 150:
						dArr[y].mA150Day = tmpPrice;
						break;
					case 200:
						dArr[y].mA200Day = tmpPrice;
						break;
					case 10:
						dArr[y].mA10Day = tmpPrice;
						break;
					case 15:
						dArr[y].mA15Day = tmpPrice;
						break;
					case 20:
						dArr[y].mA20Day = tmpPrice;
						break;
					case 25:
						dArr[y].mA25Day = tmpPrice;
						break;
					case 30:
						dArr[y].mA30Day = tmpPrice;
						break;
					case 35:
						dArr[y].mA35Day = tmpPrice;
						break;
					case 40:
						dArr[y].mA40Day = tmpPrice;
						break;
					case 500:
						dArr[y].mA500Day = tmpPrice;
						break;
					default:
						System.out.println("error: calcMovingAve");
				}
				tmpPrice = 0.0;				
			}
            switch (whatAve) {
                case 50:
                    this.currentPriceRelativeTo50ma = (dArr[0].close - dArr[0].mA50Day);
                    break;
                case 100:
                    this.currentPriceRelativeTo100ma = (dArr[0].close - dArr[0].mA100Day);
                    break;
                case 150:
                    this.currentPriceRelativeTo150ma = (dArr[0].close - dArr[0].mA150Day);
                    break;
                case 200:
                    this.currentPriceRelativeTo200ma = (dArr[0].close - dArr[0].mA200Day);
                    break;
                case 10:
                    this.currentPriceRelativeTo10ma = (dArr[0].close - dArr[0].mA10Day);
                    break;
				case 15:
                    this.currentPriceRelativeTo15ma = (dArr[0].close - dArr[0].mA15Day);
                    break;
                case 20:
                    this.currentPriceRelativeTo20ma = (dArr[0].close - dArr[0].mA20Day);
                    break;
				case 25:
                    this.currentPriceRelativeTo25ma = (dArr[0].close - dArr[0].mA25Day);
                    break;
                case 30:
                    this.currentPriceRelativeTo30ma = (dArr[0].close - dArr[0].mA30Day);
                    break; 
				case 35:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA35Day);
                    break;
				case 40:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA40Day);
                    break;
				case 500:
                    this.currentPriceRelativeTo500ma = (dArr[0].close - dArr[0].mA500Day);
                    break;     
                default:
                    System.out.println("error: current Price relative to calcWeightedMovingAve..");
            }
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }
            System.out.println("done.");
		}		
		public void calcHullMovingAve(int whatAve){
			/* calculate the whatAve bar WMA. The vector structure is FILO
               so we fetch from the most recently stored location.
			   hull moving average:
			   hma = (2 * wma(close, whatave/2) - wma(close, whatave)
			   hma = wma(hma, sqrt whatave)
			   example: 
				
				whatAve is the number of bars we want to average out. whatAve of 10 would be today thru back 10 days.
				below darr[0] is today. darr[1] is yesterday etc.
            */
		
            int x = 0;
            int y = 0;
			data dArr[] = new data[this.size];
            int saveSz = this.size;
            double tmpPrice = 0.0;
            boolean weAreDone = false;
            double tmpYearGain = 0.0;
            boolean intraDay = false;
			double dset[] = new double[whatAve];
			double wmaSlow = 0.0;
			double wmaFast = 0.0;
			double hullSet[] = new double[Math.toIntExact(Math.round(Math.sqrt(whatAve)))];
			int fastLength = 0;
			long tsqrt = 0;
			int tsqrtint = Math.toIntExact(tsqrt = Math.round(Math.sqrt(whatAve)));;
            if (saveSz == 0){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }            
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            //0 == today, saveSz is year ago..
            tmpYearGain = dArr[0].close - dArr[saveSz - 1].close;
            lastClosedPrice = dArr[0].close;           
            this.yearsGainLoss = (tmpYearGain / dArr[0].close) * 100.0;  
			//see if this is intraday trading..if date includes time and date then intra day is true..
			if((intraDay = (dArr[0].date.length() > 8))){
				//intra day trading so create skip list for MA calcuations..
				skipRanges = skipTheseRanges(dArr, whatAve);
			}
			fastLength = Math.round((whatAve / 2));
			tsqrtint = Math.toIntExact(tsqrt = Math.round(Math.sqrt(whatAve)));
			for (y = 0; (weAreDone == false); y++) {
				for (x = 0; x < tsqrtint && (!weAreDone); x++) {
					/* stop when we are at the end of year's worth */
					if ((x + y + whatAve) >= saveSz) {
						weAreDone = true;
						this.endingDate = dArr[y].date;
					}
					//fill half set first; latest back fastLength in past
					dset = fillme(dArr, (x + y), fastLength);
					wmaFast = wma(dset, fastLength);
					//now fill full length
					dset = fillme(dArr, (x + y), whatAve);
					wmaSlow = wma(dset, whatAve);
					hullSet[x] = ((wmaFast * 2) - wmaSlow);
				}
				if (weAreDone == true){
					continue;
				}
				tmpPrice = wma(hullSet, tsqrtint);
				switch (whatAve) {
					case 1:
						dArr[y].mA1Day = tmpPrice;
						break;
					case 2:
						dArr[y].mA2Day = tmpPrice;
						break;
					case 3:
						dArr[y].mA3Day = tmpPrice;
						break;
					case 4:
						dArr[y].mA4Day = tmpPrice;
						break;
					case 5:
						dArr[y].mA5Day = tmpPrice;
						break;
					case 50:
						dArr[y].mA50Day = tmpPrice;
						break;
					case 100:
						dArr[y].mA100Day = tmpPrice;
						break;
					case 150:
						dArr[y].mA150Day = tmpPrice;
						break;
					case 200:
						dArr[y].mA200Day = tmpPrice;
						break;
					case 10:
						dArr[y].mA10Day = tmpPrice;
						break;
					case 15:
						dArr[y].mA15Day = tmpPrice;
						break;
					case 20:
						dArr[y].mA20Day = tmpPrice;
						break;
					case 25:
						dArr[y].mA25Day = tmpPrice;
						break;
					case 30:
						dArr[y].mA30Day = tmpPrice;
						break;
					case 35:
						dArr[y].mA35Day = tmpPrice;
						break;
					case 40:
						dArr[y].mA40Day = tmpPrice;
						break;
					case 60:
						dArr[y].mA60Day = tmpPrice;
						break;
					case 70:
						dArr[y].mA70Day = tmpPrice;
						break;
					case 500:
						dArr[y].mA500Day = tmpPrice;
						break;
					default:
						System.out.println("error: calcMovingAve");
				}
				tmpPrice = 0.0;				
			}
            switch (whatAve) {
				case 1:
                    this.currentPriceRelativeTo1ma = (dArr[0].close - dArr[0].mA1Day);
                    break;
				case 2:
                    this.currentPriceRelativeTo2ma = (dArr[0].close - dArr[0].mA2Day);
                    break;
				case 3:
                    this.currentPriceRelativeTo3ma = (dArr[0].close - dArr[0].mA3Day);
                    break;
				case 4:
                    this.currentPriceRelativeTo4ma = (dArr[0].close - dArr[0].mA4Day);
                    break;
				case 5:
                    this.currentPriceRelativeTo5ma = (dArr[0].close - dArr[0].mA5Day);
                    break;
                case 50:
                    this.currentPriceRelativeTo50ma = (dArr[0].close - dArr[0].mA50Day);
                    break;
                case 100:
                    this.currentPriceRelativeTo100ma = (dArr[0].close - dArr[0].mA100Day);
                    break;
                case 150:
                    this.currentPriceRelativeTo150ma = (dArr[0].close - dArr[0].mA150Day);
                    break;
                case 200:
                    this.currentPriceRelativeTo200ma = (dArr[0].close - dArr[0].mA200Day);
                    break;
                case 10:
                    this.currentPriceRelativeTo10ma = (dArr[0].close - dArr[0].mA10Day);
                    break;
				case 15:
                    this.currentPriceRelativeTo15ma = (dArr[0].close - dArr[0].mA15Day);
                    break;
                case 20:
                    this.currentPriceRelativeTo20ma = (dArr[0].close - dArr[0].mA20Day);
                    break;
				case 25:
                    this.currentPriceRelativeTo25ma = (dArr[0].close - dArr[0].mA25Day);
                    break;
                case 30:
                    this.currentPriceRelativeTo30ma = (dArr[0].close - dArr[0].mA30Day);
                    break; 
				case 35:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA35Day);
                    break;
				case 40:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA40Day);
                    break;
				case 60:
                    this.currentPriceRelativeTo60ma = (dArr[0].close - dArr[0].mA60Day);
                    break;
				case 70:
                    this.currentPriceRelativeTo70ma = (dArr[0].close - dArr[0].mA70Day);
                    break;
				case 500:
                    this.currentPriceRelativeTo500ma = (dArr[0].close - dArr[0].mA500Day);
                    break;     
                default:
                    System.out.println("error: current Price relative to calcWeightedMovingAve..");
            }
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }
            System.out.println("done.");
		}
        public void calcWeightedMovingAve(int whatAve) {
            /* calculate the whatAve bar WMA. The vector structure is FILO
               so we fetch from the most recently stored location.
			   weighted moving average:
			   wma = [(cp(p1) * w1) + (cp(p2) * w2) + (cp(p3) * w3) .. (cp(pn) * wn)] DIV (w1 + w2 + w3 + wn)
			   cp == closing price, 1 == today, 2 == yesterday, 3 == three days ago..
			   p == position, 1 == today, 2 == yesterday ..
			   wx == ABS(position - n) + n
		example: 
				cp today: 10.5, cp yesterday: 12.5, cp three days ago: 14.1
				n == 3 for three day moving average
				(10.5 * 3) + (12.5 * 2) + (14.1 * 1) / (3 + 2 + 1) == 11.76
				SMA	would equal : 12.36
		
				whatAve is the number of bars we want to average out. whatAve of 10 would be today thru back 10 days.
				below darr[0] is today. darr[1] is yesterday etc.
            */
		
            int x = 0;
            int y = 0;
			data dArr[] = new data[this.size];
            int saveSz = this.size;
            double tmpPrice = 0.0;
            boolean weAreDone = false;
            double tmpYearGain = 0.0;
            boolean intraDay = false;
			double wf = 0.0;
			double sumWf = 0.0;
			double wma = 0.0;
            if (saveSz == 0){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcWeightedMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }            
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            //0 == today, saveSz is year ago..
            tmpYearGain = dArr[0].close - dArr[saveSz - 1].close;
            lastClosedPrice = dArr[0].close;           
            this.yearsGainLoss = (tmpYearGain / dArr[0].close) * 100.0;  
			//see if this is intraday trading..if date includes time and date then intra day is true..
			if((intraDay = (dArr[0].date.length() > 8))){
				//intra day trading so create skip list for MA calcuations..
				skipRanges = skipTheseRanges(dArr, whatAve);
			}
            for (y = 0; (weAreDone == false); y++) {
				if ((intraDay == true) && (doWeSkip(y, skipRanges) == true)){
					continue;
				}
                for (x = 0; x < whatAve; x++) {
                    if((x + y) < this.size){
						wf = (whatAve - (x));
						sumWf += wf;
                        tmpPrice += (dArr[x + y].close * wf);
                    }else{
                        System.out.println("calcWeightedMovingAve:" + "ERROR! size = " + this.size);
                    }                    
                }
				wma = (tmpPrice / sumWf);
				sumWf = 0.0;
                switch (whatAve) {
					case 1: dArr[y].mA1Day = wma; 
                        break;
					case 2: dArr[y].mA2Day = wma; 
                        break;
					case 3: dArr[y].mA3Day = wma; 
                        break;
					case 4: dArr[y].mA4Day = wma; 
                        break;
					case 5: dArr[y].mA5Day = wma; 
                        break;
                    case 50: dArr[y].mA50Day = wma; 
                        break;
                    case 100: dArr[y].mA100Day = wma; 
                        break;
                    case 150: dArr[y].mA150Day = wma; 
                        break;
                    case 200:
                        dArr[y].mA200Day = wma;
                        break;
                    case 10:
						if(tmpPrice > 0.0){
							dArr[y].mA10Day = wma;
						}else{
							System.out.println("\nwft!!");
						}                        
                        break;
					case 15:
                        dArr[y].mA15Day = wma;
                        break;
                    case 20:
                        dArr[y].mA20Day = wma;
                        break;
					case 25:
                        dArr[y].mA25Day = wma;
                        break;
                    case 30:
                        dArr[y].mA30Day = wma;
                        break;
					case 35:
                        dArr[y].mA35Day = wma;
                        break;
					case 40:
                        dArr[y].mA40Day = wma;
                        break;
					case 60:
                        dArr[y].mA60Day = wma;
                        break;
					case 70:
                        dArr[y].mA70Day = wma;
                        break;
					case 500:
                        dArr[y].mA500Day = wma;
                        break;		    
                    default:
                        System.out.println("error: calcWeightedMovingAve");
                }                 
                tmpPrice = 0.0;
                /* stop when we are at the end of year's worth */
                if ((x+y) >= saveSz){
                    weAreDone = true; 
                    this.endingDate = dArr[y].date;
                }
            }
            //store current price relative to MA. So priceToday minus 10/50/100/200MA..
            switch (whatAve) {
				case 1:
                    this.currentPriceRelativeTo1ma = (dArr[0].close - dArr[0].mA1Day);
                    break;
				case 2:
                    this.currentPriceRelativeTo2ma = (dArr[0].close - dArr[0].mA2Day);
                    break;
				case 3:
                    this.currentPriceRelativeTo3ma = (dArr[0].close - dArr[0].mA3Day);
                    break;
				case 4:
                    this.currentPriceRelativeTo4ma = (dArr[0].close - dArr[0].mA4Day);
                    break;
				case 5:
                    this.currentPriceRelativeTo5ma = (dArr[0].close - dArr[0].mA5Day);
                    break;
                case 50:
                    this.currentPriceRelativeTo50ma = (dArr[0].close - dArr[0].mA50Day);
                    break;
                case 100:
                    this.currentPriceRelativeTo100ma = (dArr[0].close - dArr[0].mA100Day);
                    break;
                case 150:
                    this.currentPriceRelativeTo150ma = (dArr[0].close - dArr[0].mA150Day);
                    break;
                case 200:
                    this.currentPriceRelativeTo200ma = (dArr[0].close - dArr[0].mA200Day);
                    break;
                case 10:
                    this.currentPriceRelativeTo10ma = (dArr[0].close - dArr[0].mA10Day);
                    break;
				case 15:
                    this.currentPriceRelativeTo15ma = (dArr[0].close - dArr[0].mA15Day);
                    break;
                case 20:
                    this.currentPriceRelativeTo20ma = (dArr[0].close - dArr[0].mA20Day);
                    break;
				case 25:
                    this.currentPriceRelativeTo25ma = (dArr[0].close - dArr[0].mA25Day);
                    break;
                case 30:
                    this.currentPriceRelativeTo30ma = (dArr[0].close - dArr[0].mA30Day);
                    break; 
				case 35:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA35Day);
                    break;
				case 40:
                    this.currentPriceRelativeTo35ma = (dArr[0].close - dArr[0].mA40Day);
                    break;
				case 60:
                    this.currentPriceRelativeTo60ma = (dArr[0].close - dArr[0].mA60Day);
                    break;
				case 70:
                    this.currentPriceRelativeTo70ma = (dArr[0].close - dArr[0].mA70Day);
                    break;
				case 500:
                    this.currentPriceRelativeTo500ma = (dArr[0].close - dArr[0].mA500Day);
                    break;     
                default:
                    System.out.println("error: current Price relative to calcWeightedMovingAve..");
            }
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }
            System.out.println("done.");
            
        }	
        private class GetAverage{            
            private double hiAve;
            private double loAve;
            public void calcAverage(data dArr[], int staIdx, int days) {
                int idx;
                double tmp;
                int hiCnt = 0;
                int loCnt = 0;
                double hiSum = 0;
                double loSum = 0;
                //sum them up..
                for (idx = staIdx; idx < (staIdx + days); idx++) {
                    if ((tmp = dArr[idx].daysHigh) > 0.0) {
                        hiSum += tmp;
                        hiCnt++;
                    }
                    if ((tmp = dArr[idx].daysLow) > 0.0) {
                        loSum += tmp;
                        loCnt++;
                    }
                }
                //divide total sum to get average..
                hiAve = (hiSum / hiCnt);
                loAve = (loSum / loCnt);
            }           
            public double getHiAve(){
                return hiAve;
            }
            public double getLoAve(){
                return loAve;
            }
        }

        public void calcHighsLows(int days) {
            /* 
            Calculate Peaks and troughs during a bar period. 
            Say a bar is 10 days,
                cycle through a 10 day period, keeping track of highest and lowest days in that 10 day period. Write these 
                high/low values on the 10th day. Go on to the next 10 day period and repeat. 
            */
            int x = 0;
            int y = 0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            boolean weAreDone = false;
            double hiWaterMark = 0.0;
            double loWaterMark = 0.0;            
            GetAverage getAve;
            int location = 0;
            if (saveSz == 0){
                System.out.println("calcMovingAve:" + "ERROR! size = " + this.size);
                return;
            }else if(days > saveSz){
                System.out.println("calcMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + days);
                return;
            }
            //data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            /*
            find high/low for every days period. example days == 10,
            every 10 days water mark highest/lowest and log it. repeat this
            for all history. every 10 days there will be high/low value.
            */
            for (y = 0; (weAreDone == false); y++) {
                location = (y * days);
                for (x = 0; ((x < days ) && !weAreDone); x++) {
                    /* stop when we are at the end of period's worth */
                    if ((location + days) >= saveSz){
                        weAreDone = true;
                    } else {
                        if (dArr[x + location].high > hiWaterMark) {
                            hiWaterMark = dArr[x + location].high;
                        }
                        if ((dArr[x + location].low < loWaterMark) || (loWaterMark == 0.0)) {
                            loWaterMark = dArr[x + location].low;
                        }
                    }
                }
                //write hi/low every days distance..ie if bars == 10, write every 10 days
                dArr[location].daysHigh = hiWaterMark; 
                dArr[location].daysLow = loWaterMark;
                hiWaterMark = 0.0;
                loWaterMark = 0.0;                
            } 
            /*
            now, keep running average of 1 years worth of average high/low.
            sum up first years highs/lows and divide by 252 (trades per year). This 
            is average high/low for one year. Then every day going forward (1year + 1), 
            repeat. Sum up last 252 highs/lows and divide by 252, write these 
            averages for each day.            
            */
            getAve = new GetAverage();
            for(x = 0; x < (saveSz - slopeDefs.TRADING_DAYS_PER_YEAR); x++) {
                getAve.calcAverage(dArr, x, slopeDefs.TRADING_DAYS_PER_YEAR);
                dArr[x].yearsAveHigh = getAve.getHiAve();
                dArr[x].yearsAveLow = getAve.getLoAve();
            }            
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }
            System.out.println("done.");
            
        }        
        public void calcPercentages(){
            /* first calculate how many times in one year the 
               stock traded above it's 50/100/200 dayMA, as a precentage. 
            */
            int above50Day = 0;
            int above100Day = 0;
            int above200Day = 0;
            int above150Day = 0;
            data tmpdata = new data();
            int idx = 0;
            this.restartAccess();
            for(idx = 0; idx < this.size ; idx ++) {
                tmpdata = this.getNext();
                if ((tmpdata.mA50Day > 0) && (tmpdata.close >= tmpdata.mA50Day)){
                    above50Day++;
                }
                if ((tmpdata.mA100Day > 0) && (tmpdata.close >= tmpdata.mA100Day)){
                    above100Day++;
                }
                if ((tmpdata.mA150Day > 0) && (tmpdata.close >= tmpdata.mA150Day)){
                    above150Day++;
                }
                if ((tmpdata.mA200Day > 0) && (tmpdata.close >= tmpdata.mA200Day)){
                    above200Day++;
                }                
            }
            this.restartAccess();
            this.percentAbove50ma = (float)((float)above50Day / (float)((this.size - 49)));
            this.percentAbove100ma = (float)((float)above100Day / (float)((this.size - 99)));
            this.percentAbove150ma = (float)((float)above150Day / (float)((this.size - 149)));
            this.percentAbove200ma = (float)((float)above200Day / (float)((this.size) - 199));      
            
        }
        public void calcPercentages(MaWindowSz window){
            /* first calculate how many times in "window" the 
               stock traded above it's 50/100/200 dayMA, as a precentage. 
               window max sizes for each: 1year trading days = 252. 252 - ma = max size.
               10ma -> 242 (252 - 10)
               50ma -> 202 (252 - 50)
              100ma -> 152 (252 - 100)
              200ma -> 52  (252 - 200)
            */
            int above50Day = 0;
            int above100Day = 0;
            int above200Day = 0;
            int touched50Day = 0;
            int touched100Day = 0;
            int touched200Day = 0;
            int pierced50Day = 0;
            int pierced100Day = 0;
            int pierced200Day = 0;
            data tmpdata = new data();
            int idx = 0;
            this.restartAccess();
            
            
            for(idx = 0; idx < window.get50DaySz() ; idx ++) {   
                tmpdata = this.getNext();
                //count times we closed above the 50day..
                if ((tmpdata.mA50Day > 0) && (tmpdata.close >= tmpdata.mA50Day)){
                    above50Day++;
                }
                //count times we touched the 50day..
                if ((tmpdata.mA50Day > 0) && ((tmpdata.open >= tmpdata.mA50Day) && (tmpdata.low <= tmpdata.mA50Day))){
                    touched50Day++;
                }
                //count times we closed below the 50day..
                if ((tmpdata.mA50Day > 0) && (tmpdata.close < tmpdata.mA50Day)){
                    pierced50Day++;
                }
                
            }
            this.endDate50Day = tmpdata.date;
            this.restartAccess();
            for(idx = 0; idx < window.get100DaySz() ; idx ++) { 
                tmpdata = this.getNext();
                //count times we closed above the 100day..
                if ((tmpdata.mA100Day > 0) && (tmpdata.close >= tmpdata.mA100Day)){
                    above100Day++;
                } 
                //count times we touched the 100day..
                if ((tmpdata.mA100Day > 0) && ((tmpdata.open >= tmpdata.mA100Day) && (tmpdata.low <= tmpdata.mA100Day))){
                    touched100Day++;
                }
                //count times we closed below the 100day..
                if ((tmpdata.mA100Day > 0) && (tmpdata.close < tmpdata.mA100Day)){
                    pierced100Day++;
                }
            }
            this.endDate100Day = tmpdata.date;
            this.restartAccess();
            for(idx = 0; idx < window.get200DaySz() ; idx ++) { 
                tmpdata = this.getNext();
                if ((tmpdata.mA200Day > 0) && (tmpdata.close >= tmpdata.mA200Day)){
                    above200Day++;
                }
                //count times we touched the 100day..
                if ((tmpdata.mA200Day > 0) && ((tmpdata.open >= tmpdata.mA200Day) && (tmpdata.low <= tmpdata.mA200Day))){
                    touched200Day++;
                }
                //count times we closed below the 100day..
                if ((tmpdata.mA200Day > 0) && (tmpdata.close < tmpdata.mA200Day)){
                    pierced200Day++;
                }
            }
            this.endDate200Day = tmpdata.date;
            this.restartAccess();
            this.percentAbove50ma = (float)((float)above50Day / (float)(window.get50DaySz()));
            this.percentAbove100ma = (float)((float)above100Day / (float)(window.get100DaySz()));
            this.percentAbove200ma = (float)((float)above200Day / (float)(window.get200DaySz())); 
            
            this.timesAbove50ma = above50Day;
            this.timesAbove100ma = above100Day;
            this.timesAbove200ma = above200Day;
            
            this.timesTouched50ma = touched50Day;
            this.timesTouched100ma = touched100Day;
            this.timesTouched200ma = touched200Day;
            
            this.timesPierced50ma = pierced50Day;
            this.timesPierced100ma = pierced100Day;
            this.timesPierced200ma = pierced200Day;
        }        
        
        public void calcChannelRangePercentages(){
            /* calculate max percent a stocks closing price was relative to the moving average. For
               these posible averages: 10/20/50/100/200. So for 50ma, maximum away from this average the stock
               traded in the last year. It could be +24% or -20% etc..
            */            
            double max10DayPercent = 0.0;
            double max20DayPercent = 0.0;
            double max50DayPercent = 0.0;
            double max100DayPercent = 0.0;
            double max200DayPercent = 0.0;
            String max10DayPercentDate = "";
            String max20DayPercentDate = "";
            String max50DayPercentDate = "";
            String max100DayPercentDate = "";
            String max200DayPercentDate = "";
            data tmpdata = new data();
            int idx = 0;
            this.restartAccess();
            double tmp1 = 0.0;
            for(idx = 0; idx < this.size ; idx ++) {
                tmpdata = this.getNext();
                if (tmpdata.mA10Day > 0){
                    if ((tmp1 = (Math.abs(tmpdata.close - tmpdata.mA10Day) / tmpdata.mA10Day)) > max10DayPercent ){
                        max10DayPercent = tmp1;
                        max10DayPercentDate = tmpdata.date;
                    }else{
                        
                    }
                }
                if (tmpdata.mA20Day > 0){
                    if ((tmp1 = (Math.abs(tmpdata.close - tmpdata.mA20Day) / tmpdata.mA20Day)) > max20DayPercent ){
                        max20DayPercent = tmp1;
                        max20DayPercentDate = tmpdata.date;
                    }else{
                        
                    }
                }
                if (tmpdata.mA50Day > 0){
                    if ((tmp1 = (Math.abs(tmpdata.close - tmpdata.mA50Day) / tmpdata.mA50Day)) > max50DayPercent ){
                        max50DayPercent = tmp1;
                        max50DayPercentDate = tmpdata.date;
                    }else{
                        
                    }
                }
                if (tmpdata.mA100Day > 0){
                    if ((tmp1 = (Math.abs(tmpdata.close - tmpdata.mA100Day) / tmpdata.mA100Day)) > max100DayPercent ){
                        max100DayPercent = tmp1;
                        max100DayPercentDate = tmpdata.date;
                    }else{
                        
                    }
                }
                if (tmpdata.mA200Day > 0){
                    if ((tmp1 = (Math.abs(tmpdata.close - tmpdata.mA200Day) / tmpdata.mA200Day)) > max200DayPercent ){
                        max200DayPercent = tmp1;
                        max200DayPercentDate = tmpdata.date;
                    }else{
                        
                    }
                }                
            }/*for*/
            this.restartAccess();
            this.max10MaPercent = myUtils.roundMe((max10DayPercent * 100.0), 2);
            this.max20MaPercent = myUtils.roundMe((max20DayPercent * 100.0), 2);      
            this.max50MaPercent = myUtils.roundMe((max50DayPercent * 100.0), 2);
            this.max100MaPercent = myUtils.roundMe((max100DayPercent * 100.0), 2);
            this.max200MaPercent = myUtils.roundMe((max200DayPercent * 100.0), 2);
            this.max10MaPercentDate = max10DayPercentDate;
            this.max20MaPercentDate = max20DayPercentDate;
            this.max50MaPercentDate = max50DayPercentDate;
            this.max100MaPercentDate = max100DayPercentDate;
            this.max200MaPercentDate = max200DayPercentDate;
        }        
        public double getPercentMaxAbove(int whatAve){
            double retDouble = 0.0;
            switch (whatAve) {                
                    case 10: retDouble = this.max10MaPercent;
                        break;
                    case 20: retDouble = this.max20MaPercent;
                        break;
                    case 50:retDouble = this.max50MaPercent;
                        break;
                    case 100:retDouble = this.max100MaPercent;
                        break;
                    case 200:retDouble = this.max200MaPercent;
                        break;    
                    default:
                        System.out.println("error: getPercentMaxAbove");
            }
            return retDouble;
        }
        public String getPercentMaxAboveDate(int whatAve) {
            String retStr = "";
            switch (whatAve) {
                case 10:
                    retStr = this.max10MaPercentDate;
                    break;
                case 20:
                    retStr = this.max20MaPercentDate;
                    break;
                case 50:
                    retStr = this.max50MaPercentDate;
                    break;
                case 100:
                    retStr = this.max100MaPercentDate;
                    break;
                case 200:
                    retStr = this.max200MaPercentDate;
                    break;
                default:
                    System.out.println("error: getPercentMaxAboveDate");
            }
            return retStr;
        }

        public boolean isCurrentCloseWithinMa(int whatAve, double percent) {
            /*
             This routine will see if the current close is within a percentage of latest ma.
             percent in is 5, 10 etc for .005, .10 etc so we divide by 100 here..
             */
            percent /= 100.0;
            data tmpdata = new data();
            this.restartAccess();
            double d1 = 0.0;
            tmpdata = this.getNext();
            
            switch (whatAve) {
                //first get the difference then calculate the percent from ma..
                    case 50: d1 =  Math.abs(tmpdata.close - tmpdata.mA50Day);
                             d1 = d1 / tmpdata.mA50Day;
                             this.percentWithin50ma = d1;
                        break;
                    case 100:d1 =  Math.abs(tmpdata.close - tmpdata.mA100Day);
                             d1 = d1 / tmpdata.mA100Day;
                             this.percentWithin100ma = d1;
                        break;
                    case 150:d1 =  Math.abs(tmpdata.close - tmpdata.mA150Day);
                             d1 = d1 / tmpdata.mA150Day;
                             this.percentWithin150ma = d1;
                        break;
                    case 200:d1 =  Math.abs(tmpdata.close - tmpdata.mA200Day);
                             d1 = d1 / tmpdata.mA200Day;
                             this.percentWithin200ma = d1;
                        break;
                    default:
                        System.out.println("error: isCurrentCloseWithinMa");
            }
            //System.out.println("closing price to ma == d1, d1 =  "+ d1);
            //System.out.println("percent in = "+ percent);
            if (percent > d1) {
                return(true);
            }else
                return(false);
            
        }
        
        public void calcRSI1(){
            /*
            This routine will calculate current RSI. Todays info is stored at the end
            of the data vecotor working backwards 252 days. The oldest data is stored at begining
            of vector. This routine sums up (adds) the number of times closing prices were "gains" in the last 14 days, 
            then does the same for "losses". Each of these numbers are then divided by 14 to get an average of each.
            These averages are then divided by each other to get RS: RS = aveGains/aveLosses. The RSI is then calculated by:
            RSI = 100 - (100 / 1 + RS ). From there, each new smoothRSI is calculated by taking the 
            (((lastAveGain X 13) + new gain) / 14 ) divided by ((lastAveLoss X 13) + new loss))
            The new RSI is then = 100 - (100 / 1 + smoothRSI).
            */
            
            final int RSI_14_DAYS = 14;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            int idx = 0;
            double gainLoss = 0.0;
            double gainsTotal = 0.0;
            double lossTotal = 0.0;
            double gainAve = 0.0;
            double lossAve = 0.0;
            double rs = 0.0;
            double rsi = 0.0;
            double smoothRs = 0.0;
            double sAveGain = 0.0;
            double sAveLoss = 0.0;
            double addGain = 0.0;
            double addLoss = 0.0;
            
            this.restartAccess();
            
            /* put data from vec into array for easier manipulating */
            for(idx = 0; idx < this.size; idx++) {
               dArr[idx] = new data();
               dArr[idx] = this.getNext();
            }
            
            /* dArr[0] now has todays data..dArr[1] has yesterdays etc..
               calculate the 14 day RSI at the oldest data possible, so size - 14 days.
            */
            for(idx = (saveSz - RSI_14_DAYS); idx < (saveSz -1); idx ++) {
				if((dArr[idx] == null) || (dArr[idx+1] == null)){
					//oops! should not happen, investigate!
					System.out.println("\ncalcRSI1: dArr[] == null!! idx = " + idx);
				}
                gainLoss = dArr[idx].close - dArr[idx + 1].close;
                //check if gain or loss and add them up..
                if (gainLoss > 0) {
                    //gain so add gains total.
                    gainsTotal += Math.abs(gainLoss);
                }else{
                    //loss so add loss total.
                    lossTotal += Math.abs(gainLoss);
                }
            }
            gainAve = (gainsTotal / RSI_14_DAYS);
            lossAve = (lossTotal / RSI_14_DAYS);
            rs = gainAve / lossAve;
            rsi = 100 - (100 / (1 + rs));
            sAveGain = gainAve;
            sAveLoss = lossAve;
            /*
            now that the initial 14 day RSI is calculated, calculate smooth RS and 
            new RSIs from oldest data to today..
            */
            for(idx = (saveSz - (RSI_14_DAYS + 1)); idx >= 0 ; idx --) {
                gainLoss = dArr[idx].close - dArr[idx + 1].close;
                if (gainLoss > 0){
                    addGain = Math.abs(gainLoss);
                    addLoss = 0;
                }else if (gainLoss < 0){
                    addLoss = Math.abs(gainLoss);
                    addGain = 0;
                }else{
                    addGain = 0;
                    addLoss = 0;
                }                
                gainLoss = Math.abs(gainLoss);
                sAveGain = (((sAveGain * 13) + addGain) / 14);
                sAveLoss = (((sAveLoss * 13) + addLoss) / 14);
                smoothRs = (sAveGain / sAveLoss);
                rsi = 100 - (100 / (1 + smoothRs)); 
                dArr[idx].rsi = myUtils.roundMe(rsi, 4);
            }
            this.todaysRsi = Math.round(rsi);
            this.restartAccess();     
        }
        public void calcRsiMovingAve(){
            int x = 0;
            int y = 0;
            data dArr[] = new data[this.size + 1];
            int szTotal = this.size;
            int szRsiRange = szTotal / 2;
            int idx = 0;
            double rsiSum = 0.0;
            boolean weAreDone = false;
            double tmpRsi = 0.0;
            this.restartAccess();
            
            /* put data from vec into array for easier manipulating */
            for(idx = 0; idx < szTotal; idx++) {
               dArr[idx] = new data();
               dArr[idx] = this.getNext();
            }
            
            /* dArr[0] now has todays data..dArr[1] has yesterdays etc..
               calculate moving ave of rsi backin days back.
               start with today and work backwards in time..
            */
            
            //0 == today, saveSz is year ago..       
            //find first valid rsiMa..
            for(x = (szTotal - 1); x > 0; x--){
                if(dArr[y].rsiMa > 0){
                    break;
                }
            }
            for (y = 0; (weAreDone == false); y++) {
                for (x = 0; x < szRsiRange; x++) {
                    if((x + y) < szTotal){
                        tmpRsi += dArr[x + y].rsi;
                    }else{
                        System.out.println("calcRsiMovingAve:" + "ERROR! size = " + this.size);
                    }
                    
                }
                dArr[y].rsiMa = (tmpRsi / szRsiRange);                                
                tmpRsi = 0.0;
                /* stop when we are at the end of year's worth */
                if ((x+y) >= szTotal){
                    weAreDone = true; 
                    this.endingDate = dArr[y].date;
                }
            }
             
            System.out.println("\nmaRsi: " + dArr[0].rsiMa);
            this.resetData();
            /* store all back in vector now.. */
            for(idx = (szTotal -1); idx >= 0; idx--) {
               this.storeToNext(dArr[idx]);
            }
        }
        public void calcRsiMovingAve1(int whatAve) {
            /* calculate the 50 day MA. The vector structure is FILO
               so we fetch from the most recently stored location.
            */
            int x = 0;
            int y = 0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            double tmpRsi = 0.0;
            boolean weAreDone = false;
            
            if (saveSz == 0){
                System.out.println("calcRsiMovingAve:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcRsiMovingAve:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }            
            data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            //0 == today, saveSz is year ago..       
            for (y = 0; (weAreDone == false); y++) {
                for (x = 0; x < whatAve; x++) {
                    if((x + y) < this.size){
                        tmpRsi += dArr[x + y].rsi;
                    }else{
                        System.out.println("calcRsiMovingAve:" + "ERROR! size = " + this.size);
                    }
                    
                }
                switch (whatAve) {
                    case 50: dArr[y].mA50DayRsi = tmpRsi / whatAve; 
                        break;
                    case 100: dArr[y].mA100DayRsi = tmpRsi / whatAve; 
                        break;
                    case 150: dArr[y].mA150DayRsi = tmpRsi / whatAve; 
                        break;
                    case 200:
                        dArr[y].mA200DayRsi = tmpRsi / whatAve;
                        break;
                    case 10:
                        dArr[y].mA10DayRsi = tmpRsi / whatAve;
                        break;
                    case 20:
                        dArr[y].mA20DayRsi = tmpRsi / whatAve;
                        break;
                    case 30:
                        dArr[y].mA30DayRsi = tmpRsi / whatAve;
                        break;    
                    default:
                        System.out.println("error: calcRsiMovingAve");
                }
                 
                tmpRsi = 0.0;
                /* stop when we are at the end of year's worth */
                if ((x+y) >= saveSz){
                    weAreDone = true; 
                    this.endingDate = dArr[y].date;
                }
            }
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }
            System.out.println("done.");
            
        }
        public void calcRSI(){
            /*
            This routine will calculate current RSI. Todays info is stored at the end
            of the data vecotor working backwards 252 days. The oldest data is stored at begining
            of vector. This routine sums up (adds) the number of times closing prices were "gains" in the last 14 days, 
            then does the same for "losses". Each of these numbers are then divided by 14 to get an average of each.
            These averages are then divided by each other to get RS: RS = aveGains/aveLosses. The RSI is then calculated by:
            RSI = 100 - (100 / 1 + RS ).
            */
            
            final int RSI_14_DAYS = 14;
            data tmpdata = new data();
            data dArr[] = new data[RSI_14_DAYS + 1];
            int idx = 0;
            double gainLoss = 0.0;
            double gainsTotal = 0.0;
            double lossTotal = 0.0;
            double gainAve = 0.0;
            double lossAve = 0.0;
            double rs = 0.0;
            double rsi = 0.0;
            this.restartAccess();
            //first get last 14 days (from todays date back 14 days) in dArr..
            for (idx = 0; idx < (RSI_14_DAYS + 1); idx++) {
                dArr[idx] = new data();
                dArr[idx] = this.getNext();
            }
            // dArr[0] now has todays data..dArr[1] has yesterdays etc..
            for(idx = 0; idx < (RSI_14_DAYS) ; idx ++) {
                gainLoss = dArr[idx].close - dArr[idx + 1].close;
                //check if gain or loss and add them up..
                if (gainLoss > 0) {
                    //gain so add gains total.
                    gainsTotal += Math.abs(gainLoss);
                }else{
                    //loss so add loss total.
                    lossTotal += Math.abs(gainLoss);
                }
            }
            gainAve = (gainsTotal / RSI_14_DAYS);
            lossAve = (lossTotal / RSI_14_DAYS);
            rs = gainAve / lossAve;
            rsi = 100 - (100 / (1 + rs));
            this.todaysRsi = rsi;
            this.restartAccess();     
        }
        public void findTodaysMovers(){
            /* 
            gets todays and yesterdays data and figures out close-to-open and open-to-current. 
            This info is stored in last two spots of the vector (it works backwards).
            */
            double prevCloseToOpen = 0.0;
            double todaysOpenToCurrent = 0.0;
            quoteInfo todaysQuote;
            todaysQuote = new quoteInfo();
            data yesterdaysData = new data();
            data todaysData = new data();
            
            this.restartAccess();
            
            todaysData = this.getNext();
            yesterdaysData = this.getNext();
            
            prevCloseToOpen = (todaysData.open - yesterdaysData.close);
            todaysPrevCloseToTodayOpen = myUtils.roundMe(((prevCloseToOpen / yesterdaysData.close) * 100.0), 2);
            //getQuote(todaysData.ticker, false);
            todaysQuote = actChain.getQuote(todaysData.ticker, false);
            todaysOpenToCurrent = (todaysQuote.last - todaysData.open);
            //todaysOpenToCurrent = 0;
            todaysOpenToLast = myUtils.roundMe(((todaysOpenToCurrent / todaysData.open) * 100.0), 2);
            //remove!!!!
            if(todaysOpenToLast < -50.0){
                System.out.println("\ntodaysOpenToCurrent: " + todaysOpenToCurrent + " todaysData.open: " + todaysData.open);
            }
            System.out.println("  closeToOpen:" + todaysPrevCloseToTodayOpen + "%");
            System.out.println("  openToLast:" + todaysOpenToLast + "%");
            //actChain.cancelStream(todaysData.ticker, false);
            this.restartAccess();
            
        }
        
        public void calcAveVolume(int whatAve) {
            /* calculate the ave volume for whatAve period. The vector structure is FILO
               so we fetch from the most recently stored location.
            */
            int x = 0;
            int y = 0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            double tmpVolume = 0.0;
            
            if (saveSz == 0){
                System.out.println("calcAveVolume:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcAveVolume:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }
            data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
  
            for (x = 0; x < whatAve; x++) {
                if(x < this.size){
                    tmpVolume += dArr[x].volume;
                }else{
                    System.out.println("calcAveVolume:" + "ERROR! size = " + this.size);
                }
                
            }
            switch (whatAve) {
                case 30:
                    this.aveVolume30Day = tmpVolume / whatAve;
                    break;
                case 60:
                    this.aveVolume60Day = tmpVolume / whatAve;
                    break;
                case 90:
                    this.aveVolume90Day = myUtils.roundMe((tmpVolume / whatAve) * 100.0, 2);
                    break;
                default:
                    System.out.println("error: calcAveVolume");
            }

            this.resetData();
            /* store all back in vector now.. */
            for (x = (saveSz - 1); x >= 0; x--) {
                this.storeToNext(dArr[x]);
            }
            System.out.println("done.");

        }
        public void calcBollingerBands(){
            //use 20day ma and 2nd standard deviation.
            final int RSI_20_DAYS = 20;
            data tmpdata = new data();
            data dArr[] = new data[RSI_20_DAYS + 1];
            int idx = 0;
            
            double d1[] = new double[RSI_20_DAYS + 1];
            double dCalc = 0.0;
            this.restartAccess();
            //first get last 14 days (from todays date back 14 days) in dArr..
            for (idx = 0; idx < (RSI_20_DAYS + 1); idx++) {
                dArr[idx] = new data();
                dArr[idx] = this.getNext();
                d1[idx] = 0;
            }
            // dArr[0] now has todays data..dArr[1] has yesterdays etc..
            for(idx = 0; idx < (RSI_20_DAYS) ; idx ++) {
                //diff close and today's 20day, and square it..
                d1[idx] = Math.pow(dArr[idx].close - dArr[0].mA20Day, 2);                
            }
            for(idx = 0; idx < (RSI_20_DAYS) ; idx ++) {
                //add up all sqr'd differences to get average..
                dCalc += d1[idx];              
            }
            //get average..
            dCalc = dCalc / RSI_20_DAYS;
            //now square root it
            dCalc = Math.sqrt(dCalc);
            
            this.bollingerBandsHi = dArr[0].mA20Day + (2 * dCalc);
            this.bollingerBandsLow = dArr[0].mA20Day - (2 * dCalc);
            System.out.println("\n bollingerBandsHi: " + this.bollingerBandsHi + " bollingerBandsLow: " + this.bollingerBandsLow);
            this.restartAccess();
            
        }
        public void calcStdDev(int whatAve) {
            /* calculate the volatility or standard deviation for whatAve period. The vector structure is FILO
               so we fetch from the most recently stored location.
               sd = sqrt(sq(d1) + sq(d2) /whatAve)
               example for 50day moving average:
               d1 = sqrt[(close price - 50dma)Sqrd + (close price - 50dma)Sqrd .. 50days / 50days]
            */
            int x = 0;
            double d = 0.0;
            double dsum = 0.0;
            double dstddev = 0.0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            double tmpVolume = 0.0;
            
            if (saveSz == 0){
                System.out.println("calcStdDev:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcStdDev:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }
            data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
  
            for (x = 0; x < whatAve; x++) {
                /* sum up all diff squared for period of time */
                //get diff 
                switch (whatAve) {
                    case 50:
                            d = dArr[x].close - dArr[x].mA50Day;
                            //percent it..
                            d = (d / dArr[x].mA50Day);
                        break;
                    case 100:
                            d = dArr[x].close - dArr[x].mA100Day;
                             //percent it..
                            d = (d / dArr[x].mA100Day);
                        break;
                    case 200:
                            d = dArr[x].close - dArr[x].mA200Day;
                             //percent it..
                            d = (d / dArr[x].mA200Day);
                        break;
                    default:
                        System.out.println("error: calcStdDev");
                }
                
                //square it
                d = Math.pow(d, 2);
                //add up all squares
                dsum += d;
            }
            //get average of dsum over period
            dstddev = (dsum / whatAve);
            //squar root the sum
            dstddev = Math.sqrt(dstddev);
            switch (whatAve) {
                case 50:
                    this.stdDev50Day = dstddev;
                    break;
                case 100:
                    this.stdDev100Day = dstddev;
                    break;
                case 200:
                    this.stdDev200Day = dstddev;
                    break;
                default:
                    System.out.println("error: calcStdDev");
            }

            this.resetData();
            /* store all back in vector now.. */
            for (x = (saveSz - 1); x >= 0; x--) {
                this.storeToNext(dArr[x]);
            }
            System.out.println("done.");

        }       
        
        public void calcVolatility(int whatAve) {
            /* calculate price change day to day and get average for period.
            */
            int x = 0;
            double d = 0.0;
            double dsum = 0.0;
            double dave = 0.0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            double tmpVolume = 0.0;
            
            if (saveSz == 0){
                System.out.println("calcVolatility:" + "ERROR! size = " + this.size);
                return;
            }else if(whatAve > saveSz){
                System.out.println("calcVolatility:" + "ERROR! size < whatAve! " + "size: " + this.size + " whatAve: " + whatAve);
                return;
            }
            data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
  
            for (x = 0; x < whatAve; x++) {
                /* sum up all diff squared for period of time */
                //get diff 
                if((dArr[x] != null) && (dArr[x + 1] != null)) {              
                    switch (whatAve) {
                        case 50:
                            d = dArr[x].close - dArr[x + 1].close;
                            break;
                        case 100:
                            d = dArr[x].close - dArr[x + 1].close;
                            break;
                        case 200:
                            d = dArr[x].close - dArr[x + 1].close;
                            break;
                        default:
                            System.out.println("error: calcVolatility");
                    }
                }else{
                    System.out.println("\n error calcVolatility: dArr[x] or dArr[x+1] is null!!!! x = " + x + " dArrSz = " + this.size);
                }
                //percent it..and absolute it
                d = Math.abs(d / dArr[x].close);
                //keep sum of abs percent changes..
                dsum += d;
            }
            //get average of dsum over period
            dave = (dsum / whatAve);
          
            switch (whatAve) {
                case 50:
                    this.vol50Day = dave;
                    break;
                case 100:
                    this.vol100Day = dave;
                    break;
                case 200:
                    this.vol200Day = dave;
                    break;
                default:
                    System.out.println("error: calcVolatility");
            }

            this.resetData();
            /* store all back in vector now.. */
            for (x = (saveSz - 1); x >= 0; x--) {
                this.storeToNext(dArr[x]);
            }
            System.out.println("done.");

        }
        
        public void findTails(GapUpDnTail tailToFind) {
            
            /*            
             Gap is previous day's close to today's open.
             gap up:
             If up today, then we gapped UP.
             If down, we gapped Down.
             Tail gapUP:
               the tail is today's open price to Hi of day 
             Tail gapDn:
               the tail is today's open price to Low of day          
            search through histor looking for Tails that meet criteria in tailToFind.
            If found, count the number of tails found, and record the date it occurred.
             */
            int x = 0;
            double d = 0.0;            
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;            
            double gapUp = 0.0;
            double gapDn = 0.0;
            double tail = 0.0;
            int numFoundGapUp = 0;
            int numFoundGapDn = 0;
            
            if (saveSz == 0){
                System.out.println("calcVolatility:" + "ERROR! size = " + this.size);
                return;
            }           
            /* put data from vec into array for easier manipulating */
            for(x = 0; x < this.size; x++) {
               dArr[x] = new data();
               dArr[x] = this.getNext();
            }
            // dArr[0] now has todays data..dArr[1] has yesterdays etc..
            //start back in histor (size) minus one day, cycle through to yesterday....
            for(x = (saveSz -2); x > 0; x--){
                if((dArr[x + 1].close) < (dArr[x].open)){
                    //gaped UP
                    gapUp = (dArr[x].open) - (dArr[x + 1].close);
                    gapUp = (gapUp / dArr[x+1].close) * 100;
                    //tail is today's hi minus today's open.
                    tail = dArr[x].high - dArr[x].open;
                    if (tail > 0){
                        tail = ((tail / dArr[x].open) * 100);
                        if ((gapUp >= tailToFind.getGapUpPct()) && 
                            (tail >= tailToFind.getGapUpTailPct()) && 
                            (dArr[x].close <= dArr[x].open)
                           ){
                            //found one!!
                            tailToFind.addDate(dArr[x].ticker + " " + dArr[x].date + " gapUp");
                            numFoundGapUp++;                            
                        }else{
                            
                        }
                    }else{
                        //no tail
                        tail = 0.0;
                    }                  
                }else if ((dArr[x + 1].close) > (dArr[x].open)){
                    //gaped Dn
                    gapDn = (dArr[x + 1].close) - (dArr[x].open);
                    gapDn = (gapDn / dArr[x+1].close) * 100;
                    //tail is today's open minus today's low.
                    tail = dArr[x].open - dArr[x].low;
                    if (tail > 0){
                        tail = ((tail / dArr[x].open) * 100);
                        if ((gapDn >= tailToFind.getGapDnPct()) && 
                            (tail >= tailToFind.getGapDnTailPct()) && 
                            (dArr[x].close >= dArr[x].open)
                           ){
                            //found one!!
                            tailToFind.addDate(dArr[x].ticker + " " + dArr[x].date + " gapDown");
                            numFoundGapDn++;
                        }else{
                            
                        }
                    }else{
                        //no tail
                        tail = 0.0;
                    }     
                }
                gapUp = 0.0;
                tail = 0.0;
            } /*for*/
        }
        public void checkForBullBearCrossing(BullBearCross bbcross) {
            /* calculate price change day to day and get average for period.
             */
            int x = 0;
            int daysConsecutiveTrue = 0;
            int daysTrue = 0;
            int daysFalse = 0;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            boolean passed = false;
            if (saveSz == 0) {
                System.out.println("checkForBullBearCrossing:" + "ERROR! size = " + this.size);
                return;
            }
            data tmpData = new data();
            /* put data from vec into array for easier manipulating */
            for (x = 0; x < this.size; x++) {
                dArr[x] = new data();
                dArr[x] = this.getNext();
            }
            //assume goodness.
            passed = true;
            for (x = 0; ((x <= bbcross.conditionPeriodInDays) && (passed == true)); x++) {
                switch (bbcross.crossConditionInt) {
                    case slopeDefs.oBULL_50_200DMA_INT:
                        passed = (dArr[x].mA50Day > dArr[x].mA200Day);                       
                        break;
                    case slopeDefs.oBULL_50_100_200DMA_INT:
                        passed = (dArr[x].mA50Day > dArr[x].mA100Day) && (dArr[x].mA100Day > dArr[x].mA200Day);
                        break;
                    case slopeDefs.oBEAR_200_50DMA_INT:
                        passed = (dArr[x].mA50Day < dArr[x].mA200Day);
                        break;
                    case slopeDefs.oBEAR_200_100_50DMA_INT:
                        passed = (dArr[x].mA50Day < dArr[x].mA100Day) && (dArr[x].mA100Day < dArr[x].mA200Day);                     
                        break;
                    default:
                        System.out.println("error: checkForBullBearCrossing");
                }
                if (passed == true){
                    daysTrue++;
                }else{
                    daysFalse++;
                }
                if ((passed == true) && (daysFalse == 0)){
                    daysConsecutiveTrue++;
                }
            }
            /*during the window period, the On condition can be true for multiple (continueous) days, 
              but false only one.
              This means we transitioned from not true to true during the window period. 
              This is what we are looking for. If it happens, we store the ticker, the date it happened and how many
              days back it occured. The window period max is 53 because of the 200 dayma lenth. There are 253 trading
              days in one year (the historical data length), so that means only 53 days of the 200 day can be calculated.
              Right now, all avail conditions involve the 200dma.
            */
            if ((daysConsecutiveTrue > 0) && (daysFalse > 0)){
                bbcross.dateItHappend = dArr[x].date;
                bbcross.tickerFound = dArr[x].ticker;
                bbcross.daysBackWhenOccurred = daysConsecutiveTrue;
                System.out.println("\nCONDITION HAPPENED!!! " + dArr[x].ticker + " " + bbcross.crossConditionStr + 
                                   " true for: " + daysConsecutiveTrue + " conecutive days.("+dArr[x].date+")");
                passed = true;
            }else{
                passed = false;
            }
            
            bbcross.wePassed = passed;
            
            this.resetData();
            /* store all back in vector now.. */
            for (x = (saveSz - 1); x >= 0; x--) {
                this.storeToNext(dArr[x]);
            }
            System.out.println("done.");

        }
        public int countClosingDaysTo10dMa(boolean aboveMa, int daysBackToCount){
            
            data tmpdata = new data();
            this.restartAccess();
            double d1 = 0.0;
            tmpdata = this.getNext();
            int days;
            int cnt;
            //tmdata now has todays info..need to start with yesterdays..
            for (days = 0, cnt = 0; days < daysBackToCount; days++){
                tmpdata = this.getNext();  
                if ((aboveMa == true) && (tmpdata.close > tmpdata.mA10Day)){
                    cnt++;
                }else if ((aboveMa == false) && (tmpdata.close < tmpdata.mA10Day)){
                    cnt++;
                }
            }  
            this.restartAccess();
            return cnt;
        }
        public int countClosingDaysTransitions10dMa(int daysBackToCount){
            
            data tmpdata = new data();
            data dArr[] = new data[this.size + 1];
            this.restartAccess();
            int saveSz = this.size;
            double d1 = 0.0;           
            int days;
            int cnt = 0;
            int x;
            boolean weAreAbove = false;
            
            /* put data from vec into array for easier manipulating */
            for (x = 0; x < this.size; x++) {
                dArr[x] = new data();
                dArr[x] = this.getNext();
            }
            x--;
            //tmdata now has todays info..need to start with yesterdays..
            weAreAbove = (dArr[x].close > dArr[x].mA10Day);
            for (days = (x-1); days > 0; days--){  
                //count transitions only
                if ((weAreAbove == true) && ((dArr[days].close) < dArr[days].mA10Day)){
                    cnt++;
                    weAreAbove = false;
                }else if ((weAreAbove == false) && ((dArr[days].close) > dArr[days].mA10Day)){
                    cnt++;
                    weAreAbove = true;
                }               
            }  
            this.transitionsRelativeTo10DayMaCount = cnt;
            this.transitionsRelativeTo10DayMaPercent = (double)((double)cnt / (double)this.size);
            this.resetData();
            /* store all back in vector now.. */
            for (x = (saveSz - 1); x >= 0; x--) {
                this.storeToNext(dArr[x]);
            }
            return cnt;
        }
        public boolean areWeCurrently(boolean aboveMa) {

            data tmpdata = new data();
            this.restartAccess();
            tmpdata = this.getNext();
            boolean passed = false;
            //todaysClose == what it's trading at today..
            double todaysClose = 0.0;
            double yesterdaysClose = 0.0;
            //tmdata now has todays info..get current trade value..                        
            todaysClose = tmpdata.close;
            //tmdata now has todays info..need to start with yesterdays..
            tmpdata = this.getNext();
            yesterdaysClose = tmpdata.close;
            
            passed = (aboveMa ? (todaysClose > yesterdaysClose) : (todaysClose < yesterdaysClose));
            
            this.restartAccess();
            return passed;
        }
        
        BackTestDataOneYear btData1Year;
        public void recordBackTestData(int useThisMa){       
            /* 
            Scans the historical data back one year and using the 10Day moving average 
            stores every instance of a Trend Change(boolean), new trend (string),
            number of times the closing prices were consecutivly above and below the 10dayMa. 
            */
            int x = 0;
            slopeData slopeResult;
            double day1 = 0.0;
            double day5 = 0.0;
            double closePrice = 0.0;           
            boolean found = false;           
            final int RUN_SLOPE_PERIOD = SLOPE_DAYS;
            data dArr[] = new data[this.size + 1];
            int saveSz = this.size;
            int idx = 0;
            int idy = 0;
            int btIdx = 0;
            boolean stop = false;
            int aboveCnt = 0;
            int belowCnt = 0;
            int endIdx = 0;
           // BackTestDataOneYear btData1Year;
            processSlopes process = new processSlopes();
            this.restartAccess();           
            slopeAnalysis saOuter = new slopeAnalysis();
             
            btData1Year = saOuter.new BackTestDataOneYear(this.size);
            //oldest data at end..
            btIdx = this.size - 1;
            btData1Year.ticker = this.forThisTicker;
            /* put data from vec into array for easier manipulating */
            for(idx = 0; idx < this.size; idx++) {
               dArr[idx] = new data();
               dArr[idx] = this.getNext();
            }
            
            /* dArr[0] now has todays data..dArr[1] has yesterdays etc..
               We start from oldest data to newest data. 
               first look for begining of real data ..
            */
            for (idx = (saveSz -1), found = false ; (found == false); idx--){
                if (dArr[idx].getMa(useThisMa) > 0) {
                    found = true;
                }            
            }
           
            // this loop for history..
            idx++;
            //mark the begining of data for later 
            //we don't search past this point.
            if (found == true){
                endIdx = idx;
            }
            slopeResult = new slopeData();
            //set very first slope true...
            process.setFirstSlope(true);
            for (idx = (idx - RUN_SLOPE_PERIOD); idx >= 0; idx--, btIdx--) {
                slopeResult = new slopeData();
                slopeCnt = 0;
                day1 = dArr[idx].getMa(useThisMa);
                slopeResult.dateP1 = dArr[idx].date;
                btData1Year.day[btIdx].date = dArr[idx].date;
                day5 = dArr[idx + RUN_SLOPE_PERIOD].getMa(useThisMa);
                slopeResult.dateP2 = dArr[idx + RUN_SLOPE_PERIOD].date;
                //get close price for P/L later
                closePrice = dArr[idx].close;
                
                btData1Year.day[btIdx].closePrice = dArr[idx].close;
                
                process.workOnThisSlope(slopeResult, RUN_SLOPE_PERIOD);
                process.doIt(day5, day1);
                //no longer veryfirst slope...
                process.setFirstSlope(false);
                if (process.didDirectionChange() == true){
                    //idx == 0, means today, 1 == yesterday..store this..
                    slopeResult.dayInTradingYear = idx;
                    slopeResult.closePrice = closePrice;
                    dArr[idx].btTrendChanged = true;
                    dArr[idx].btNewTrendis = slopeResult.isTrendUp() ? slopeDefs.TREND_UP : slopeDefs.TREND_DN;                  
                    btData1Year.day[btIdx].changeInDirection = true;
                    btData1Year.day[btIdx].newTrend = slopeResult.isTrendUp() ? slopeDefs.TREND_UP : slopeDefs.TREND_DN;
                }
                dArr[idx].btTrendToday = slopeResult.isTrendUp() ? slopeDefs.TREND_UP : slopeDefs.TREND_DN;
                btData1Year.day[btIdx].trendToday = slopeResult.isTrendUp() ? slopeDefs.TREND_UP : slopeDefs.TREND_DN;
                //go back and count consequtive days above/below 10ma counts.
                //first do above..endIdx is the end of data...don't go beyond that..
                for (idy = 1, stop = false; (((idx + idy) <= endIdx) && (stop == false)); idy++){
                    //count above..
                    if (dArr[idx + idy].close > dArr[idx + idy].mA10Day){
                        aboveCnt++;
                    }else{
                        //not consequtive..stop..
                        stop = true; 
                    }
                }
                dArr[idx].btConsecutiveDaysAbove10dma = aboveCnt;
                btData1Year.day[btIdx].btConsecutiveDaysAbove10dma = aboveCnt;
                //now below..
                for (idy = 1, stop = false; (((idx + idy) <= endIdx) && (stop == false)); idy++){
                    //count above..
                    if (dArr[idx + idy].close < dArr[idx + idy].mA10Day){
                        belowCnt++;
                    }else{
                        //not consequtive..stop..
                        stop = true; 
                    }
                }
                dArr[idx].btConsecutiveDaysBelow10dma = belowCnt;
                btData1Year.day[btIdx].btConsecutiveDaysBelow10dma = belowCnt;
                btData1Year.day[btIdx].empty = false;
                aboveCnt = belowCnt = 0;
            } /*for*/
            
            this.resetData();
            /* store all back in vector now.. */
            for(x = (saveSz -1); x >= 0; x--) {
               this.storeToNext(dArr[x]);
            }

            this.restartAccess();
 
        }
        public BackTestDataOneYear getBackTestData(){
            
            return btData1Year;
        }
        public class SlopeLog {
            int logSize = 0;
            String trendWas = null;
            final String BULL_TO_BEAR = "bull2Bear";
            final String BEAR_TO_BULL = "Bear2Bull";
            final String NO_HISTORY = "NO_HISTORY";
            slopeData[] slopeLog;
            
            public SlopeLog(int sz){
                slopeLog = new slopeData[sz];
                for (int i = 0; i < sz; i++){
                    slopeLog[i] = new slopeData();
                }
                
            }
            public void addSlope(slopeData newSlope){
                slopeLog[logSize] = newSlope;
                logSize++;
            }
            public int getLogSz(){
                return(logSize);
            }
            public slopeData getSlope(int slopeNumber){
                return slopeLog[slopeNumber];
            }
            
        }

        public class slopeData {
            double slopeValue = 0.0;
            boolean positive = true;
            public String dateP1 = null;
            public String dateP2 = null;
            public String trend = null;
            final String TREND_UP   = slopeDefs.TREND_UP;
            final String TREND_DN   = slopeDefs.TREND_DN;
            final String NO_HISTORY = slopeDefs.NO_HISTORY;
            double slopeUpMax = 0.0;
            double slopeDnMax = 0.0;
            public int trendUpTicks = 0;
            public int trendDnTicks = 0;
            /*
            keep maximum day price while we are in 
            the position. This will be used to figure out a recommended GainLock value.
            */
            public double maxClosingPrice = 0.0;
            /*this keeps the trading day that the change occurred in
              0 == today, 1 == yesterday etc..
            */
            public double stdDev50Day = 0.0;
            public double vol50Day = 0.0;
            public int dayInTradingYear = 0;
            public double closePrice = 0.0;
            public double currentClosePrice = 0.0;
            public boolean changeInDirection = false;
            public boolean isTrendUp(){
                boolean res = false;
                res = ((positive == true) && (trend == TREND_UP));
                return res;
            }
            public boolean isTrendDn(){
                boolean res = true;
                res = ((positive == false) && (trend == TREND_DN));
                return res;
            }
            void setDirection(){
                //negative number is positive slope, positive number is negative slope
                if (slopeValue < 0)
                    positive = true;
                else
                    positive = false;
            }
        }
        public class HistorySlope{
            final int HIST_SZ = 3; 
            final int LOG_SZ = 800;
            slopeData history[] = new slopeData[HIST_SZ];
            slopeData[] slopeLog = new slopeData[LOG_SZ];
            int idx = 0;
            int actHistory = 0;
            boolean empty = true;
            int logSize = 0;
            public int logIdx = 0;
            public boolean currentChangeInDirection = false;
            public String currentTrend = null;
            final int SLOPE_OFF         = slopeDefs.SLOPE_OFF;
            final int DIRECTION_CHANGE  = slopeDefs.DIRECTION_CHANGE;
            final int UPTREND           = slopeDefs.UPTREND;
            final int DNTREND           = slopeDefs.DNTREND;
            double maxClosingPrice = 0.0;
            public double stdDev50Day = 0.0;
            public double volDev50Day = 0.0;
            public double stdDev100Day = 0.0;
            public double volDev100Day = 0.0;
            public String longOrShort = slopeDefs.oBiasLongStr;
            public void createLog(int size){
                for (idx = 0; idx < size; idx++) {
                    slopeLog[idx] = new slopeData();
                }
                logSize = size;
            }
            //init the history array..
            public HistorySlope(){
                for (idx = 0; idx < HIST_SZ; idx++) {
                    history[idx] = new slopeData();   
                }
                empty = true;
            }
            public void addHistory(slopeData newSlope){
                history[actHistory++] = newSlope;
                if (actHistory >= HIST_SZ){
                    actHistory = 0;
                }
                empty = false;
            }
            public void addToLog(slopeData newSlope){
                currentChangeInDirection = newSlope.changeInDirection;
                //wfs
                currentTrend = newSlope.trend;
                maxClosingPrice = newSlope.maxClosingPrice;
                slopeLog[logIdx++] = newSlope;
                if(logIdx >= LOG_SZ){
                    logIdx = 0;
                }
                
            }
            public slopeData getFromLog(int idx){
                return slopeLog[idx];
            }
            public slopeData getHistory(){
                if (empty == true) {
                    return null;
                } else if (actHistory == 0) {
                    return history[HIST_SZ - 1];
                } else {
                    return history[actHistory - 1];
                }
            }
            public String getCurrentTrend(){
                if (logIdx < 1) {
                    return null;
                }else{
                    return slopeLog[logIdx - 1].trend;
                }
            }
            public int getCurrentUpDays(){
                if (logIdx < 1) {
                    return -1;
                }else{
                    return slopeLog[logIdx - 1].trendUpTicks;
                }
            }
            public int getCurrentDnDays(){
                if (logIdx < 1) {
                    return -1;
                }else{
                    return slopeLog[logIdx - 1].trendDnTicks;
                }
            }
            public void setLongShort(String longShort){
                longOrShort = longShort;
            }
        }
        public class processSlopes{
            double slopeUpMax = 0.0;
            double slopeDnMax = 0.0;
            boolean currentTrendPositive = false;
            boolean lastTrendPositive = false;
            String slopeUpMaxDate = null;
            String slopeDnMaxDate = null;
            int trendUpTicks = 0;
            int trendDnTicks = 0;
            int tmpTicks = 0;
            slopeData actSlope = new slopeData();
            int slopePeriod = 0;
            boolean changedDirection = false;
            boolean slopeMatched = false;
            double currentClosePrice = 0.0;
            /*
            wfs 5/29/15 this was added to abort very first slope (oldest)
            because we don't know more than a year. if first slope is positive we 
            would buy shares because lastTrendPositive is initialized to false;
            */
            boolean veryFirstSlope = true;
            public boolean didDirectionChange(){
                return changedDirection;
            }
            
            public boolean getSlopeMatched(){
                return true;
            }
            public double getSLopeUpMax(){
                return slopeUpMax;
            }
            public String getSLopeUpMaxDate(){
                return slopeUpMaxDate;
            }
            public double getSLopeDnMax(){
                return slopeDnMax;
            }
            public String getSLopeDnMaxDate(){
                return slopeDnMaxDate;
            }
            public void workOnThisSlope(slopeData slopeIn, int period){
                actSlope = slopeIn;
                slopePeriod = period;
            }
            public void setFirstSlope(boolean fs){
                veryFirstSlope = fs;
            }
            public void doIt(double d1, double d2){
                actSlope.slopeValue = ((d1 - d2));                
                //now do rise over run
                actSlope.slopeValue = myUtils.roundMe((actSlope.slopeValue / slopePeriod), 2);
if((false) && (veryFirstSlope == false) && (lastTrendPositive == false)){                
                if (Math.abs(actSlope.slopeValue) <= 0.02){
                    actSlope.positive = lastTrendPositive;
                }else{
                    actSlope.setDirection();
                }
}else{
                    actSlope.setDirection();
}               
 
                if (actSlope.positive == true) {
                    trendUpTicks++;
                    actSlope.trend = actSlope.TREND_UP;
                    if (Math.abs(actSlope.slopeValue) > slopeUpMax) {
                        slopeUpMax = Math.abs(actSlope.slopeValue);
                        slopeUpMaxDate = actSlope.dateP1;
                    }else{
                        
                    }
                    
                }else{
                    trendDnTicks++;
                    actSlope.trend = actSlope.TREND_DN;
                    if (Math.abs(actSlope.slopeValue) > slopeDnMax) {
                        slopeDnMax = Math.abs(actSlope.slopeValue);
                        slopeDnMaxDate = actSlope.dateP1;
                    }else{
                        
                    }                   
                }
                //see if we changed direction.. log if so..don't trigger if very first slope..
                if ((veryFirstSlope == false) && (lastTrendPositive != actSlope.positive)){
                    actSlope.trend = (actSlope.positive == true) ? actSlope.TREND_UP: actSlope.TREND_DN;
                    actSlope.trendUpTicks = trendUpTicks;
                    actSlope.trendDnTicks = trendDnTicks;
                    trendDnTicks = 0;
                    trendUpTicks = 0;
                    changedDirection = true;
                    
                }else{
                    changedDirection = false;
                    
                }
                //update last trend
                lastTrendPositive = actSlope.positive;
                currentClosePrice = actSlope.currentClosePrice;
                
            }
            public void setDates(String d1, String d2){
                actSlope.dateP1 = d1;
                actSlope.dateP2 = d2;
            }
            
        }
        
        final int DIRECTION_CHANGE  = slopeDefs.DIRECTION_CHANGE;
        final int TREND_UP          = slopeDefs.UPTREND;
        final int TREND_DN          = slopeDefs.DNTREND;
        public final int SLOPE_DAYS = slopeDefs.SLOPE_DAYS;
        public int getSlopeDays(){
            return SLOPE_DAYS;
        }
        
        SlopeAnalysisData slopeAnalysisData;
        private void calcSecondDerivative(int sz)
        {
            SlopeAnalysisDataPerDay slopeDay;
            
            //first calculate the second derivative of the slopeValue which is the first derivitive..
            for (slopeCnt = SLOPE_DAYS; slopeCnt < sz; slopeCnt++){
                slopeDay = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt];                
                slopeDay.secondDerivativeSv = (slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue - slopeAnalysisData.slopeAnalysisPerDay[slopeCnt - SLOPE_DAYS].slopeValue) / SLOPE_DAYS;
                slopeDay.secondDerivativeSvPercent = (slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValuePercent - slopeAnalysisData.slopeAnalysisPerDay[slopeCnt - SLOPE_DAYS].slopeValuePercent) / SLOPE_DAYS;               
            }
            
        }
        private void calcSlopeAve(int sz){
            SlopeAnalysisDataPerDay slopeDay;
            SlopeAnalysisDataPerDay aveDay;
            double slopeAve = 0.0;
            int slopeCnt = 0;
            int aveCnt;
            //cycle all days..
            for (slopeCnt = SLOPE_DAYS; slopeCnt < sz; slopeCnt++){                
                aveDay = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt-1]; 
                //get ave of last SLOPE_DAYS days..
                for (aveCnt = (slopeCnt - SLOPE_DAYS) ;  aveCnt < slopeCnt; aveCnt++ ){
                    slopeAve += slopeAnalysisData.slopeAnalysisPerDay[aveCnt].slopeValuePercent;
                }
                //store the ave..start over..
                aveDay.slopeAvePercent = (slopeAve / SLOPE_DAYS);
                slopeAve = 0;               
            }
            
        }
        private class appendToStr {

            String wrToStr = "";

            public void setStrToWr(String s) {
                wrToStr = s;
            }

            public void appendMe(String s) {
                wrToStr += s;
            }

            public String getStr() {
                return wrToStr;
            }
        }

        public String analizeDataPositions(String portfolioName){
            int pos = 0;
            String outPutStr = "";
            appendToStr strToWr = new appendToStr();
            strToWr.setStrToWr(outPutStr);
            int totWinners = 0;
            int totLosers  = 0;
            double totPl = 0.0;
            double totP = 0.0;
            double totL = 0.0;
            double totPlPercent = 0.0;
            double totPPercent = 0.0;
            double totLPercent = 0.0;
            double initialAccountValue = 0.0;
            double finalAccountValue = 0.0;
            double buyHoldPl = 0.0;
            double buyHoldPlPercent = 0.0;
            double openPosValue = 0.0;
            double openPosValueOverShoot = 0.0;
            int openPosCnt = 0;
            int openPosCntOverShoot = 0;
            double maxPositionGain = 0.0;
            double maxPositionLoss = 0.0;
            String maxPositionGainDate = "";
            String maxPositionLossDate = "";
            String maxPositionGainTicker = "";
            String maxPositionLossTicker = "";
            double maxPortfolioGain = 0.0;
            double maxPortfolioGainLast = 0.0;
            double maxPortfolioLossLast = 0.0;
            //double maxPortfolioLoss = 0.0;
            String maxPortfolioGainDate = "";
            String maxPortfolioLossDate = "";
            String maxPortfolioGainTicker = "";
            //String maxPortfolioLossTicker = "";
            double maxPortfolioGainWOpen = 0.0;
            double maxPortfolioGainLastWOpen = 0.0;
            double maxPortfolioLossLastWOpen = 0.0;
            //double maxPortfolioLossWOpen = 0.0;
            String maxPortfolioGainDateWOpen = "";
            String maxPortfolioLossDateWOpen = "";
            double portfolioRunningPlByLongs = 0.0;
            double portfolioRunningPlByShorts = 0.0;
                       
            int numOfDays = 0;
            int day = 0;
            for (pos = 0; pos < numOfPositions; pos++) {
                if (positions[pos] != null) {
                    if (positions[pos].runningPl >= 0) {
                        totWinners++;
                        totP += positions[pos].runningPl;
                        totPPercent += positions[pos].runningPlPercent;
                    } else {
                        totLosers++;
                        totL += positions[pos].runningPl;
                        totLPercent += positions[pos].runningPlPercent;
                    }
                    totPl += positions[pos].runningPl;
                    totPlPercent += positions[pos].runningPlPercent;
                    initialAccountValue += positions[pos].initialPositionValue;
                    finalAccountValue += (positions[pos].initialPositionValue + positions[pos].runningPl);
                    buyHoldPl += positions[pos].buyHoldPl;
                    
                    openPosValue += positions[pos].openPosValue;
                    openPosValueOverShoot =+ positions[pos].openPosValueOverShoot;
                    
                    if (positions[pos].getNumOfPosOpened() > 0){
                        openPosCnt++;
                    }
                    if (positions[pos].getNumOfPosOpened(positions[pos].tradesOverShoot) > 0){
                        openPosCntOverShoot++;
                    }
                    portfolioRunningPlByLongs += positions[pos].runningPlByLongs;
                    portfolioRunningPlByShorts += positions[pos].runningPlByShorts;
                    //find maximum days for next operation..below
                    if(positions[pos].slopeData.slopeCnt > numOfDays){
                        numOfDays = positions[pos].slopeData.slopeCnt;
                    }
                }
            }
            
            //numOfDays now has the max num of days of all ports, used next..
            //for each day, cycle thru all positions and get max gain/loss data..
            for (day = 0; day < numOfDays; day++) {
                for (pos = 0; pos < numOfPositions; pos++) {
                     // make sure position is there..  each position's # of days could be differenct don't go behond positions #
                    if ((positions[pos] != null) && (day <= positions[pos].slopeData.slopeCnt) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)) {
                        //look for max position..
                        if (positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl > maxPositionGain) {
                            maxPositionGain = positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl;
                            maxPositionGainDate = positions[pos].slopeData.slopeAnalysisPerDay[day].thisDate;
                            maxPositionGainTicker = positions[pos].ticker;
                        }
                        if (positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl < maxPositionLoss) {
                            maxPositionLoss = positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl;
                            maxPositionLossDate = positions[pos].slopeData.slopeAnalysisPerDay[day].thisDate;
                            maxPositionLossTicker = positions[pos].ticker;
                        }
                        maxPortfolioGain += maxPositionGain = positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl;
                        maxPortfolioGainWOpen += (positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl + 
                                                  positions[pos].slopeData.slopeAnalysisPerDay[day].openPosValue);
                    }
                }
                //look for max portfolio gain/loss day..
                if ((maxPortfolioGain > 0) && (maxPortfolioGain > maxPortfolioGainLast)) {
                    maxPortfolioGainLast = maxPortfolioGain;
                    maxPortfolioGainDate = positions[0].slopeData.slopeAnalysisPerDay[0].thisDate;
                }
                if ((maxPortfolioGain < 0) && (maxPortfolioGain < maxPortfolioLossLast)){
                    maxPortfolioLossLast = maxPortfolioGain; 
                    maxPortfolioLossDate = positions[0].slopeData.slopeAnalysisPerDay[0].thisDate;
                }
                maxPortfolioGain = 0.0;
                //look for max portfolio gain/loss day..with open included..
                if ((maxPortfolioGainWOpen > 0) && (maxPortfolioGainWOpen > maxPortfolioGainLastWOpen)) {
                    maxPortfolioGainLastWOpen = maxPortfolioGainWOpen;
                    maxPortfolioGainDateWOpen = positions[0].slopeData.slopeAnalysisPerDay[0].thisDate;
                }
                if ((maxPortfolioGainWOpen < 0) && (maxPortfolioGainWOpen < maxPortfolioLossLastWOpen)){
                    maxPortfolioLossLastWOpen = maxPortfolioGainWOpen; 
                    maxPortfolioLossDateWOpen = positions[0].slopeData.slopeAnalysisPerDay[0].thisDate;
                }
                maxPortfolioGainWOpen = 0.0;
            }
            buyHoldPlPercent = ((buyHoldPl / initialAccountValue) * 100);
            System.out.println("\n  Total winners: " + totWinners + " Total Losers: " + totLosers);
            System.out.println("\n  Profits: " + totP + " Losses: " + totL);
            System.out.println("\n  Overall PL: " + totPl);
            System.out.println("\n  OpenPosValue : " + openPosValue + "  (" + openPosCnt + ")");
            System.out.println("\n  OpenPosValueOverShoot : " + openPosValueOverShoot + "  (" + openPosCntOverShoot + ")");
            System.out.println("\n  OverallPl + openValue : " + (totPl + openPosValue));
            System.out.println("\n  OverALL : " + ((totPl + openPosValue)/initialAccountValue) + " %");
            System.out.println("\n  InitalAccountValue: " + initialAccountValue);
            System.out.println("\n  FinalAccountValue: " + finalAccountValue);
            System.out.println("\n  BuyHoldPl: " + buyHoldPl);
            System.out.println("\n  BuyHoldPlPercent: " + buyHoldPlPercent);
            System.out.println("\n  Overall PLPercent: " + totPlPercent);
            System.out.println("\n  MaxPositionGain: " + maxPositionGain + " OnDate: " + maxPositionGainDate + " Ticker: " + maxPositionGainTicker);
            System.out.println("\n  MaxPositionLoss: " + maxPositionLoss + " OnDate: " + maxPositionLossDate + " Ticker: " + maxPositionLossTicker);
            System.out.println("\n  MaxPortfolioGain: " + maxPortfolioGainLast + " OnDate: " + maxPortfolioGainDate);
            System.out.println("\n  MaxPortfolioLoss: " + maxPortfolioLossLast + " OnDate: " + maxPortfolioLossDate);
            System.out.println("\n  MaxPortfolioGainWOpen: " + maxPortfolioGainLastWOpen + " OnDate: " + maxPortfolioGainDateWOpen);
            System.out.println("\n  MaxPortfolioLossWOpen: " + maxPortfolioLossLastWOpen + " OnDate: " + maxPortfolioLossDateWOpen);
            System.out.println("\n  RunningPlByLongs: " + portfolioRunningPlByLongs);
            System.out.println("\n  RunningPlByShorts: " + portfolioRunningPlByShorts);
            
            strToWr.appendMe("\n\n " + portfolioName);
            strToWr.appendMe("\n  Total winners: " + totWinners + " Total Losers: " + totLosers);
            strToWr.appendMe("\n  Profits: " + myUtils.truncate(totP, 2) + " Losses: " + myUtils.truncate(totL, 2));
            strToWr.appendMe("\n  Overall PL: " + myUtils.truncate(totPl, 2));
            strToWr.appendMe("\n  OpenPosValue : " + myUtils.truncate(openPosValue, 2) + "  (" + openPosCnt + ")");
            strToWr.appendMe("\n  OpenPosValueOverShoot : " + myUtils.truncate(openPosValueOverShoot, 2) + "  (" + myUtils.truncate(openPosCntOverShoot, 2) + ")");
            strToWr.appendMe("\n  OverallPl + openValue : " + myUtils.truncate((totPl + openPosValue), 2));
            strToWr.appendMe("\n  OverALL : " + (myUtils.truncate(((totPl + openPosValue)/initialAccountValue) * 100, 2) + " %"));
            strToWr.appendMe("\n  InitalAccountValue: " + myUtils.truncate(initialAccountValue, 2));
            strToWr.appendMe("\n  FinalAccountValue: " + myUtils.truncate(finalAccountValue, 2));
            strToWr.appendMe("\n  BuyHoldPl: " + myUtils.truncate(buyHoldPl, 2));
            strToWr.appendMe("\n  BuyHoldPlPercent: " + myUtils.truncate(buyHoldPlPercent, 2));
            strToWr.appendMe("\n  Overall PLPercent: " + myUtils.truncate(totPlPercent, 2));
            strToWr.appendMe("\n  MaxPositionGain: " + myUtils.truncate(maxPositionGain, 2) + " OnDate: " + maxPositionGainDate + " Ticker: " + maxPositionGainTicker);
            strToWr.appendMe("\n  MaxPositionLoss: " + myUtils.truncate(maxPositionLoss, 2) + " OnDate: " + maxPositionLossDate + " Ticker: " + maxPositionLossTicker);
            strToWr.appendMe("\n  MaxPortfolioGain: " + myUtils.truncate(maxPortfolioGainLast, 2) + " OnDate: " + maxPortfolioGainDate);
            strToWr.appendMe("\n  MaxPortfolioLoss: " + myUtils.truncate(maxPortfolioLossLast, 2) + " OnDate: " + maxPortfolioLossDate);
            strToWr.appendMe("\n  MaxPortfolioGainWOpen: " + myUtils.truncate(maxPortfolioGainLastWOpen, 2) + " OnDate: " + maxPortfolioGainDateWOpen);
            strToWr.appendMe("\n  MaxPortfolioLossWOpen: " + myUtils.truncate(maxPortfolioLossLastWOpen, 2) + " OnDate: " + maxPortfolioLossDateWOpen);
            strToWr.appendMe("\n  RunningPlByLongs: " + myUtils.truncate(portfolioRunningPlByLongs, 2));
            strToWr.appendMe("\n  RunningPlByShorts: " + myUtils.truncate(portfolioRunningPlByShorts, 2));
            outPutStr = strToWr.getStr();
            //set up for next run of new portfolio..
            initialized = false;
            return outPutStr;
        }
        public void analizeDataSetSize(int sz){
                       
            if (initialized == false){
                numOfPositions = sz-1;
                actPosition = 0;
                positions = new PosManager[sz];
                initialized = true;
                actPosition = 0;
            }
        }
        public class TradeStats{
            double sumSv;
            double sumSvPercent;
            boolean complete = false;
            boolean abort = false;
        }
        
        public int searchForTicker(String tin){
            int portNum = -1;
            int idx = 0;
            //no positions, no deal..
            if(positions == null){
                return portNum;
            }
            for(idx = 0, portNum = -1; (idx < positions.length) && (portNum == -1); idx++){
                if(positions[idx] != null){
                    if (positions[idx].ticker.equals(tin.toUpperCase())){
                        portNum = idx;
                    }
                }
            }
            return portNum;
        }
        public int getNumOfPositions(){
            
            return positions.length;
        }
        public int getNumOfPositionsInPortfolio(){
            int ap = 0;
            int pos = 0;
            for (pos = 0; pos < numOfPositions; pos++){
                //look for real number of positions.as some may have failed to get historical data..
                if (positions[pos] != null) {
                    ap++;
                }                    
            }
            return ap;
        }
        public int getMaxDaysOfAllPositions(){
            int md = 0;
            int pos = 0;
            for (pos = 0; pos < numOfPositions; pos++){
                //look for max # of days of all positions..
                if ((positions[pos] != null) && (positions[pos].slopeData.slopeCnt > md)){
                    md = positions[pos].slopeData.slopeCnt;
                }                    
            }
            return md;
        }
        public double getMaxGainForYear(int pos){
            double g = 0;
            if(positions[pos]!= null){
                g = positions[pos].maxRunningGainForYear;
            }
            return g;
        }
        public double getMinGainForYear(int pos){
            double l = 0;
            if(positions[pos]!= null){
                l = positions[pos].maxRunningLossForYear;
            }
            return l;
        }
        public String getMaxGainForYearDate(int pos){
            String ld = "";
            if(positions[pos]!= null){
                ld = positions[pos].maxRunningGainForYearDate;
            }
            return ld;
        }
        public String getMinGainForYearDate(int pos){
            String ld = "";
            if(positions[pos]!= null){
                ld = positions[pos].maxRunningLossForYearDate;
            }
            return ld;
        }
        public int getNumOfSlopes(int pos){
            int s = 0;
            if(positions[pos]!= null){
                s = positions[pos].slopeData.slopeCnt;
            }
            return s;
        }
        public double getPlLongs(int pos){
            double pl = 0;
            if(positions[pos]!= null){
                pl = positions[pos].runningPlByLongs;
            }
            return pl;
        }
        public double getPlShorts(int pos){
            double pl = 0;
            if(positions[pos]!= null){
                pl = positions[pos].runningPlByShorts;
            }
            return pl;
        }
        public int getSharesTradedForBackTesting(int pos){
            int s = 0;
            if(positions == null){
                return s;
            }
            if(positions[pos]!= null){
                s = positions[pos].totalSharesForBackTest;
            }
            return s;
        }
        public int getNumOfTrades(int pos){
            int tc = 0;
            if(positions[pos]!= null){
                tc = positions[pos].tradeCount;
            }
            return tc;
        }
        
        public double getClosePrice(int pos, int day){
            double cp = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                cp = positions[pos].slopeData.slopeAnalysisPerDay[day].todaysClose;
            }           
            return cp;
        }
        public double getBuyHoldValue(int pos, int day){
            double bh = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                bh = (positions[pos].slopeData.slopeAnalysisPerDay[day].todaysClose * positions[pos].totalSharesForBackTest) - positions[pos].initialPositionValue;  
                //remove 
                System.out.println("\n    todaysClose: " + positions[pos].slopeData.slopeAnalysisPerDay[day].todaysClose + " totSharesForBt: " + positions[pos].totalSharesForBackTest + " initPosValue: " + positions[pos].initialPositionValue);
            }           
            return bh;
        }
        public double getBuyHoldValue(int pos){
            double bh = 0.0;
            if(positions[pos] != null){
                bh = (positions[pos].buyHoldPl);            
            }           
            return bh;
        }
        public double getBuyHoldValuePercent(int pos){
            double bh = 0.0;
            if(positions[pos] != null){
                bh = (positions[pos].buyHoldPlPercent);            
            }           
            return bh;
        }
        public int getOpenPositionCnt(int pos, int day){
            int op = 0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                op = positions[pos].slopeData.slopeAnalysisPerDay[day].openPosCnt;            
            }           
            return op;
        }
        public int getOpenPositionCntOvershoot(int pos, int day){
            int op = 0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                op = positions[pos].slopeData.slopeAnalysisPerDay[day].openPosCntOverShoot;            
            }           
            return op;
        }
        public double getRunningPl(int pos, int day){
            double cp = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                cp = positions[pos].slopeData.slopeAnalysisPerDay[day].runningPl;
            }           
            return cp;
        }
        public double getOpenPosValue(int pos, int day){
            double cp = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                cp = positions[pos].slopeData.slopeAnalysisPerDay[day].openPosValue;
            }           
            return cp;
        }
        public double getOpenPosValueOverShoot(int pos, int day){
            double cp = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                cp = positions[pos].slopeData.slopeAnalysisPerDay[day].openPosValueOverShoot;
            }           
            return cp;
        }
        public double getSlopeValue(int pos, int day){
            double sv = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                sv = positions[pos].slopeData.slopeAnalysisPerDay[day].slopeValuePercent;
            }           
            return sv;
        }
        public double get10DayMa(int pos, int day){
            double sv = 0.0;
            if((positions[pos] != null) && (positions[pos].slopeData.slopeAnalysisPerDay[day] != null)){
                sv = positions[pos].slopeData.slopeAnalysisPerDay[day].ma10Day;
            }           
            return sv;
        }
        public String getLastClosedDayOperation(int pos){
            String op = "";
            if((positions[pos] != null)){
                op = positions[pos].lastClosedOperation;
            }     
            return op;
        }
        public String getCurrentRealOperation(int pos){
            String op = "";
            if((positions[pos] != null)){
                op = positions[pos].operationReal;// = slopeDefs.oCLOSE;//FUCK//slopeDefs.oOPEN
                //op = slopeDefs.oOPEN;
            }     
            return op;
        }
        public double getLastClosedDayPrice(){
            return lastClosedPrice;
        }
        public double getInitialStockPrice1YearAgo(){
            /* important , this is the price of stock back when 10dma begins NOT one year ago. */
            return slopeAnalysisData.initialStockPrice1YearAgo;
        }
        public double getInitialStockPrice1YearAgo(int position){
            /* important , this is the price of stock back when 10dma begins NOT one year ago. */
            double p = 0.0;
            if((positions != null) && positions[position] != null){
                p = positions[position].initialStockPrice1YearAgo;
            }
            return p;
        }
        public double getRunningPl(int position){
            /* important , this is the price of stock back when 10dma begins NOT one year ago. */
            double p = 0.0;
            if((positions != null) && positions[position] != null){
                p = positions[position].runningPl;
            }
            return p;
        }
        public double getOpenPosValue(int position){
            /* important , this is the price of stock back when 10dma begins NOT one year ago. */
            double p = 0.0;
            if((positions != null) && positions[position] != null){
                p = positions[position].openPosValue;
            }
            return p;
        }
        public double getOpenPosValueOverShoot(int position){
            /* important , this is the price of stock back when 10dma begins NOT one year ago. */
            double p = 0.0;
            if((positions != null) && positions[position] != null){
                p = positions[position].openPosValueOverShoot;
            }
            return p;
        }
        
        public double getLastClosedDayPrice(int pos){
            double cp = 0.0;
            if((positions[pos] != null)){
                cp = positions[pos].lastClosedPrice;
            }     
            return cp;
        }
        public int getSharesToTrade(int pos){
            int st = 0;
            if((positions[pos] != null)){
                st = positions[pos].sharesPerTradeForNow;
            } 
            return st;
        }
        public int getSharesToTradeForBackTestOverShoot(int pos){
            int st = 0;
            if((positions[pos] != null)){
                st = positions[pos].sharesPerTradeForBackTestOverShoot;
            } 
            return st;
        }
        public int getOverShootSharesPercent(int pos){
            int st = 0;
            if((positions[pos] != null)){
                st = positions[pos].overShootSharesPercent;
            } 
            return st;
        }
        public int getSharesToTradeOverShoot(int pos){
            int st = 0;
            if((positions[pos] != null)){
                st = positions[pos].sharesPerTradeForNowOverShoot;
            } 
            return st;
        }
        public String getTicker(int pos){
            String t = "";
            if(positions[pos] != null){
                t = positions[pos].ticker;
            }
            return t;
        }
        public void setSharesPerTrade(int sharesToTrade, int tradeScale){
            backTestSharesPerTrade = (int) myUtils.roundMe((double)(sharesToTrade / tradeScale), 2);
        }
        public void setOverShootPercent(int overshootpercent){ 
            //set overShootPercent and overShootShares to use and 
            overShootPercent = overshootpercent;
            overShootShares = (int) (((overShootPercent) / 100.0) * backTestSharesPerTrade);            
        }
        public int getOverShootPercent(){
            return overShootPercent;
        }
        public void setOverShootLinearMode(boolean linearModeIn){ 
 
            linearMode = linearModeIn;
        }
        public void setRecommendOverShootPercent(boolean recOsPercent){ 
 
            recommendOverShootPercent = recOsPercent;
        }
        public void setTraderConfigParams(traderConfigParams tc, int bias){
            /* we use this to see if buy on weekness sell on strength or visaversa */
            actTraderConfig = new traderConfigParams(bias);
            actTraderConfig = tc;
        }
        public int findRecommendedOverShootPercent(PosManager posIn, String bias){
            /*
            this routine will cycle through overShoot percents: 0, 25, 50, 75, 100, 125, and 150 on a particular position.
            The best performing value is then returned as the recommented one. Each value is run through a year's worth
            of backtest data. The final runningPl + openPosValues is then divided by number of trading days, to get gain per
            day value. The highest of this value is the winner and is returned as the recommended value.
            */
            SlopeAnalysisDataPerDay slopeToday;
            SlopeAnalysisDataPerDay slopeYesterday;
            boolean openLong = false;
            boolean closeLong = false;  
            boolean openShort = false;
            boolean closeShort = false;
            int recOvershootPercent = 0;
            int actOverShoot = 0; 
            int bestOverShoot = 0;
            int overShootTable[] = {0, 25, 50, 75, 100, 125, 150};
            int sz = slopeAnalysisData.slopeCnt;
            int dayCnt = 0;
            int saveOld = getOverShootPercent();
            double runningPlTotal = 0.0; 
            double runningPlTotalMax = 0.0;
            double bestPlPerTradingDay = 0.0;
            boolean nonFound = true;
            
            
            for (actOverShoot = 0; actOverShoot < overShootTable.length; actOverShoot++) {
                PosManager position = new PosManager(slopeAnalysisData.thisTicker, 
                                                 posIn.tradeScale /*tradeScale*/, 
                                                 posIn.totalSharesForBackTest /*total shares to trade for backtest*/, 
                                                 slopeAnalysisData.initialStockPrice1YearAgo, /*for backTest*/
                                                 posIn.totalSharesForNow,
                                                 slopeAnalysisData.initialStockPriceNow
                );
                //set the overshoot percent and shares to use..
                setOverShootPercent(overShootTable[actOverShoot]);
                for (dayCnt = 1; dayCnt < sz; dayCnt++) {
                    slopeToday = slopeAnalysisData.slopeAnalysisPerDay[dayCnt];
                    slopeYesterday = slopeAnalysisData.slopeAnalysisPerDay[dayCnt - 1];
                    //check the open for open on weakness..
                    if ((actTraderConfig != null) && (traderConfigParams.isWeaknessSet(actTraderConfig.getOpenWhenCode()) == true)) {
                        if (bias.equals(slopeDefs.oBiasLongStr)) {
                            openLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent));
                            closeLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent));
                        } else {
                            //put oBiasShortStr case here if ever implemented..
                        }
                        //check the open for open on strength..
                    } else if ((actTraderConfig != null) && (traderConfigParams.isStrengthSet(actTraderConfig.getOpenWhenCode()) == true)) {
                        if (bias.equals(slopeDefs.oBiasLongStr)) {
                            openLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent));
                            closeLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent));
                        } else {
                            //put oBiasShortStr case here if ever implemented..
                        }
                    } else {//default to open on weakness close on strength..
                        if (bias.equals(slopeDefs.oBiasLongStr)) {
                            openLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent));
                            closeLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                    && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent));
                        } else {
                            //put oBiasShortStr case here if ever implemented..
                        }
                    }

                    if ((openLong == true) || (openShort == true)) {
                        if (linearMode == true) {
                            position.tradeOverShootingLinear("open", slopeToday.todaysClose, bias);
                        } else {
                            position.tradeOverShooting("open", slopeToday.todaysClose, bias);
                        }
                        slopeToday.operation = slopeDefs.oOPEN;
                    } else if ((closeLong == true) || (closeShort == true)) {
                        if (linearMode == true) {
                            position.tradeOverShootingLinear("close", slopeToday.todaysClose, bias);
                        } else {
                            position.tradeOverShooting("close", slopeToday.todaysClose, bias);
                        }
                        slopeToday.operation = slopeDefs.oCLOSE;
                    }
                    
                    
                }/*for*/
                //get today's close price..
                position.endingStockPrice = slopeAnalysisData.slopeAnalysisPerDay[sz-1].todaysClose;
                runningPlTotal = (position.runningPl + 
                                  position.getOpenPosValue(position.trades, position.endingStockPrice, bias) +
                                  position.getOpenPosValue(position.tradesOverShoot, position.endingStockPrice, bias)
                                  );
                                        
                //peg the max..
                if (runningPlTotal > runningPlTotalMax) {
                    runningPlTotalMax = runningPlTotal;
                    bestOverShoot = overShootTable[actOverShoot];
                    //see if we found one..
                    nonFound = false;
                }
                        
            }
            if (nonFound == true){
                System.out.println("\n findRecommendedOverShootPercent:" + " nonFound == true..for this ticker: " + slopeAnalysisData.thisTicker + " defaults to OS = 0.");
            }
            bestPlPerTradingDay = (runningPlTotalMax / (sz - 1));
            recOvershootPercent = bestOverShoot;
            //restore original value..
            setOverShootPercent(saveOld);
            System.out.println("*********recommended OverShoot: " + recOvershootPercent + " **********");
            return recOvershootPercent;
        }        
        public void calculatePercentAboveMa(SlopeAnalysisData withInTheseDays, int staDay, int stopDay){            
            int actDay;
            int lastDay = 0;
            int ma10Cnt = 0;
            int ma20Cnt = 0;
            int ma30Cnt = 0;
            int ma50Cnt = 0;
            int ma100Cnt = 0;
            int ma150Cnt = 0;
            int ma200Cnt = 0;
            int totalDays = slopeDefs.oOneYearTrades - 1;            
            int lastHistDay = 0;
      
            //go back 1 year from actDay
            for (actDay = staDay, lastHistDay = (staDay - slopeDefs.oOneYearTrades); actDay > lastHistDay; actDay--) {
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma10Day) {
                    ma10Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma20Day) {
                    ma20Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma30Day) {
                    ma30Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma50Day) {
                    ma50Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma100Day) {
                    ma100Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma150Day) {
                    ma150Cnt++;
                }
                if (withInTheseDays.slopeAnalysisPerDay[actDay].todaysClose >= withInTheseDays.slopeAnalysisPerDay[actDay].ma200Day) {
                    ma200Cnt++;
                }

            }
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa10Day = (((double) ma10Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa20Day = (((double) ma20Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa30Day = (((double) ma30Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa50Day = (((double) ma50Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa100Day = (((double) ma100Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa150Day = (((double) ma150Cnt / (double) totalDays) * 100.0);
            withInTheseDays.slopeAnalysisPerDay[staDay].percentAboveMa200Day = (((double) ma200Cnt / (double) totalDays) * 100.0);

            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa10Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma10Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma10Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa20Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma20Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma20Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa30Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma30Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma30Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa50Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma50Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma50Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa100Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma100Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma100Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa150Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma150Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma150Day) * 100.0;
            withInTheseDays.slopeAnalysisPerDay[staDay].percentWithInMa200Day = ((withInTheseDays.slopeAnalysisPerDay[staDay].todaysClose - withInTheseDays.slopeAnalysisPerDay[staDay].ma200Day)
                    / withInTheseDays.slopeAnalysisPerDay[staDay].ma200Day) * 100.0;
        }        
        public void analizeDataThrowaway(String bias, int tradeScale, int totalSharesToTrade1YearAgo, int totalSharesToTradeNow){
            int dayCnt = 0;
            //just go back one year
            int sz = slopeAnalysisData.slopeCnt;
            SlopeAnalysisDataPerDay slopeToday;
            SlopeAnalysisDataPerDay slopeYesterday;
            double maxRunningGainForYear = 0.0;
            double maxRunningLossForYear = 0.0;
            double maxRunningPlusOpenGainForYear = 0.0;
            double maxRunningPlusOpenLossForYear = 0.0;
            String maxGainForYearDate = "";
            String maxLossForYearDate = "";
            String maxGainPlusOpenForYearDate = "";
            String maxLossPlusOpenForYearDate = "";
            boolean openLong = false;
            boolean closeLong = false;
            boolean openShort = false;
            boolean closeShort = false;
            boolean openLongReal = false;
            boolean closeLongReal = false;
            boolean openShortReal = false;
            boolean closeShortReal = false;        
            int recommendedOverShootValuePercent = 0;
            double percentAboveMa = 0.0;
            double percentWithinMa = 0.0;
       
            calcSecondDerivative(sz);
            calcSlopeAve(sz);
            System.out.println("\n slopeAnalysisData size is: " + sz + " for ticker: " + slopeAnalysisData.thisTicker); 
            PosManager position = new PosManager(slopeAnalysisData.thisTicker, 
                                                 tradeScale /*tradeScale*/, 
                                                 totalSharesToTrade1YearAgo /*total shares to trade for backtest*/, 
                                                 slopeAnalysisData.initialStockPrice1YearAgo, /*for backTest*/
                                                 totalSharesToTradeNow,
                                                 slopeAnalysisData.initialStockPriceNow
            );
            position.slopeData = new SlopeAnalysisData(sz);
            position.slopeData = slopeAnalysisData;
            
            if (recommendOverShootPercent == true){
                recommendedOverShootValuePercent = findRecommendedOverShootPercent(position, bias);
                setOverShootPercent(recommendedOverShootValuePercent);
                //now set position's..
                position.overShootSharesPercent = recommendedOverShootValuePercent;
                position.sharesPerTradeForNowOverShoot = (int) myUtils.roundMe((double)(((double)position.overShootSharesPercent / 100.0) * position.sharesPerTradeForNow), 2);
            }else{
                //now set position's..
                position.overShootSharesPercent = getOverShootPercent();
            }
                       
            for (dayCnt = 300; dayCnt < sz; dayCnt++){
                slopeToday = slopeAnalysisData.slopeAnalysisPerDay[dayCnt];                
                slopeYesterday = slopeAnalysisData.slopeAnalysisPerDay[dayCnt-1];
                
                //check the open for open on weakness..
                if ((actTraderConfig != null) && (traderConfigParams.isWeaknessSet(actTraderConfig.getOpenWhenCode()) == true)) {
                    if (bias.equals(slopeDefs.oBiasLongStr)) {
                        openLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent));
                        closeLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent));
                    } else {
                        //put oBiasShortStr case here if ever implemented..
                    }
                //check the open for open on strength..
                } else if ((actTraderConfig != null) && (traderConfigParams.isStrengthSet(actTraderConfig.getOpenWhenCode()) == true)) {
                    
                    if (bias.equals(slopeDefs.oBiasLongStr)) {
                        openLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent >= slopeYesterday.slopeValuePercent)
                                && (slopeToday.slopeValuePercent > 0)
                                );
                        closeLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent <= slopeYesterday.slopeValuePercent)
                                && (slopeToday.slopeValuePercent < 0));
                    } else {
                        //put oBiasShortStr case here if ever implemented..
                    }
                    
                    calculatePercentAboveMa(slopeAnalysisData, dayCnt, slopeAnalysisData.slopeCnt - 1); 
                    percentWithinMa = slopeToday.percentWithInMa100Day;
                    if ((slopeToday.percentAboveMa100Day > 50) && (Math.abs(slopeToday.percentWithInMa100Day) <= 2)){
                        System.out.println("\n**");
                        System.out.println("\nTicker: " + slopeAnalysisData.thisTicker);
                        System.out.println("\nDate: " + slopeToday.thisDate);
                        System.out.println("\n  above 100dma is: " + slopeToday.percentAboveMa100Day + " WithinMa by: " + slopeToday.percentWithInMa100Day );
                        System.out.println("\n**");
                    }
                    
                    
                } else {//default to open on weakness close on strength..
                    if (bias.equals(slopeDefs.oBiasLongStr)) {
                        openLong = ((slopeToday.todaysClose <= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent));
                        closeLong = ((slopeToday.todaysClose >= slopeToday.useThisMa)
                                && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent));
                    } else {
                        //put oBiasShortStr case here if ever implemented..
                    }
                }

                if ((openLong == true) || (openShort == true)) {
                    if (linearMode == true) {
                            position.tradeOverShootingLinear("open", slopeToday.todaysClose, bias);
                    } else {
                        position.tradeOverShooting("open", slopeToday.todaysClose, bias);
                    }
                    slopeToday.operation = slopeDefs.oOPEN;
                } else if ((closeLong == true) || (closeShort == true)) {
                    if (linearMode == true) {
                        position.tradeOverShootingLinear("close", slopeToday.todaysClose, bias);
                    } else {
                        position.tradeOverShooting("close", slopeToday.todaysClose, bias);
                    }
                    slopeToday.operation = slopeDefs.oCLOSE;
                }
                
                slopeToday.runningPl = position.runningPl;
                slopeToday.openPosValue = position.getOpenPosValue(slopeToday.todaysClose, bias);
                slopeToday.openPosValueOverShoot = position.getOpenPosValue(position.tradesOverShoot, slopeToday.todaysClose, bias);
                slopeToday.openPosCnt = position.getNumOfPosOpened();
                //get openPos count of overShoot shorts..
                slopeToday.openPosCntOverShoot = position.getNumOfPosOpened(position.tradesOverShoot);
                //log max p/l
                if(slopeToday.runningPl > maxRunningGainForYear){
                    maxRunningGainForYear = slopeToday.runningPl;
                    maxGainForYearDate = slopeToday.thisDate;
                }
                if(slopeToday.runningPl < maxRunningLossForYear){
                    maxRunningLossForYear = slopeToday.runningPl;
                    maxLossForYearDate = slopeToday.thisDate;
                }  
                 if((slopeToday.runningPl + slopeToday.openPosValue + slopeToday.openPosValueOverShoot) > maxRunningPlusOpenGainForYear){
                    maxRunningPlusOpenGainForYear = (slopeToday.runningPl + slopeToday.openPosValue + slopeToday.openPosValueOverShoot);
                    maxGainPlusOpenForYearDate = slopeToday.thisDate;
                }
                if(slopeToday.runningPl < maxRunningLossForYear){
                    maxRunningPlusOpenLossForYear = (slopeToday.runningPl  + slopeToday.openPosValue + slopeToday.openPosValueOverShoot);
                    maxLossPlusOpenForYearDate = slopeToday.thisDate;
                }    
            }/*for*/
            
            //get today's close price..
            position.endingStockPrice = slopeAnalysisData.slopeAnalysisPerDay[sz-1].todaysClose;
            //for real trading..
            
            slopeToday = slopeAnalysisData.slopeAnalysisPerDay[sz-1];                
            slopeYesterday = slopeAnalysisData.slopeAnalysisPerDay[sz-2];
            openLongReal = ((bias.equals(slopeDefs.oBiasLongStr))
                    && ((slopeToday.todaysClose <= slopeToday.useThisMa)
                    && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent)));
            closeLongReal = ((bias.equals(slopeDefs.oBiasLongStr))
                    && ((slopeToday.todaysClose >= slopeToday.useThisMa)
                    && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent)));
            openShortReal = ((bias.equals(slopeDefs.oBiasShortStr))
                    && ((slopeToday.todaysClose >= slopeToday.useThisMa)
                    && (slopeToday.slopeValuePercent > slopeYesterday.slopeValuePercent)));
            closeShortReal = ((bias.equals(slopeDefs.oBiasShortStr))
                    && ((slopeToday.todaysClose <= slopeToday.useThisMa)
                    && (slopeToday.slopeValuePercent < slopeYesterday.slopeValuePercent)));
            
            if ((openLongReal == true) || (openShortReal == true)) {               
                slopeToday.operationReal = slopeDefs.oOPEN;
            } else if ((closeLongReal == true) || (closeShortReal == true)) {               
                slopeToday.operationReal = slopeDefs.oCLOSE;
            }
            //get last closed day closing price and today's operation, use this to trade real..
            position.lastClosedPrice = slopeAnalysisData.slopeAnalysisPerDay[sz-2].todaysClose;
            position.lastClosedOperation = slopeAnalysisData.slopeAnalysisPerDay[sz-1].operation;
            position.operationReal = slopeToday.operationReal;
            
            position.openPosValue = position.getOpenPosValue(position.endingStockPrice, bias);
            position.openPosValueOverShoot = position.getOpenPosValue(position.tradesOverShoot, position.endingStockPrice, bias);
            
            position.buyHoldPl = (position.endingStockPrice * position.totalSharesForBackTest) - position.initialPositionValue;
            if (position.initialPositionValue != 0){
                position.buyHoldPlPercent = myUtils.truncate(position.buyHoldPl / position.initialPositionValue, 2);
                position.runningPlPercent = myUtils.truncate((position.runningPl / position.initialPositionValue), 2);
            }
            position.maxRunningGainForYear = maxRunningGainForYear;
            position.maxRunningGainForYearDate = maxGainForYearDate;
            position.maxRunningLossForYear = maxRunningLossForYear;
            position.maxRunningLossForYearDate = maxLossForYearDate;
            position.maxRunningPlusOpenGainForYear = maxRunningPlusOpenGainForYear;
            position.maxRunningPlusOpenGainForYearDate = maxGainPlusOpenForYearDate;
            position.maxRunningPlusOpenLossForYear = maxRunningPlusOpenLossForYear;
            position.maxRunningPlusOpenLossForYearDate = maxLossPlusOpenForYearDate;
            
            positions[actPosition++] = position;
            
        }        
        public HistorySlope findSlopes1(int wantedSlope, int useThisMa){
            /* 
            Scans the historical data of the 10Day moving average and looks for every instance
            of slope. Right now slope is fixed at zero. The dates of when this slope occurred in history is saved. 
            Slope is calculated by: slope = rise / run. Rise is the change in price and run is the time period.
            Currently the run is fixed at 5 trading day values of the 10Day MA points.
            
            */
            
            slopeData slopeResult;
            //slopeData lastSlopeStored = new slopeData();
            double day1 = 0.0;
            double day5 = 0.0;
            double closePrice = 0.0;
            double maxClosingPrice = 0.0;
            double currentClosePrice = 0.0;
            boolean found = false;
            actWally = new HistorySlope();
            actWally.createLog(this.size);
            final int RUN_SLOPE_PERIOD = SLOPE_DAYS;
            data dArr[] = new data[this.size + 1];
            int tmpTicks = 0;
            int saveSz = this.size;
            int idx = 0;
            processSlopes process = new processSlopes();
            this.restartAccess();
            //slopeAnalysisData = new SlopeAnalysisData[this.size + 1];
            slopeAnalysisData = new SlopeAnalysisData(this.size + 1);
             
            /* put data from vec into array for easier manipulating */
            for(idx = 0; idx < this.size; idx++) {
               dArr[idx] = new data();
               dArr[idx] = this.getNext();
            }
            
            /* dArr[0] now has todays data..dArr[1] has yesterdays etc..
               We start from oldest data to newest data. 
               first look for begining of real data ..
            */
            for (idx = (saveSz -1), found = false ; (found == false); idx--){
                
                if (dArr[idx].getMa(200) > 0) {
                    found = true;
                }            
            }
            //go back just one year..if there is one year worth of ma data
            //else do less than a year to where the data begins..some stocks
            //may not have the history...
            
            idx++;
            
            // this loop for history..
            
            slopeResult = new slopeData();
            //set very first slope true...
            process.setFirstSlope(true);
            
            
            for (idx = (idx - RUN_SLOPE_PERIOD), slopeCnt = 0; idx >= 0; idx--, slopeCnt++) {
                slopeResult = new slopeData();
                //for easy access..  
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt] = new SlopeAnalysisDataPerDay();
                slopeAnalysisData.thisTicker = this.forThisTicker;
                slopeAnalysisData.thisMA = useThisMa;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].thisDate = dArr[idx].date;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].todaysClose = dArr[idx].close;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma10Day = dArr[idx].mA10Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma20Day = dArr[idx].mA20Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma30Day = dArr[idx].mA30Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma50Day = dArr[idx].mA50Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma100Day = dArr[idx].mA100Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma150Day = dArr[idx].mA150Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].ma200Day = dArr[idx].mA200Day;
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].useThisMa = dArr[idx].getMa(useThisMa);
                
                day1 = dArr[idx].getMa(useThisMa);
                slopeResult.dateP1 = dArr[idx].date;
                day5 = dArr[idx + RUN_SLOPE_PERIOD].getMa(useThisMa);
                slopeResult.dateP2 = dArr[idx + RUN_SLOPE_PERIOD].date;
                //get close price for P/L later
                closePrice = dArr[idx].close;
                if (closePrice > maxClosingPrice){
                    maxClosingPrice = closePrice;
                }
                process.workOnThisSlope(slopeResult, RUN_SLOPE_PERIOD);
                process.doIt(day5, day1);
                //store slope values of this ticker..invert polarity + value == upslope..
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue = (process.actSlope.slopeValue * -1.0);
                /*
                get percent change of change in slope values 
                (from the two points), multyply by RUN_SLOPE_PERIOD, 
                and divid by 10Day for +- % move. This gives a percent move 
                relative instead of using actual dollar move...
                */
                slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValuePercent = myUtils.truncate(((slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue * RUN_SLOPE_PERIOD) / dArr[idx].getMa(useThisMa) * 100.0), 2);
                if ((slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue >= 0) && (slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue > slopeAnalysisData.maxPosSlope)){
                    slopeAnalysisData.maxPosSlope = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue;
                    slopeAnalysisData.maxPosSlopeDate = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].thisDate;
                    slopeAnalysisData.avePosSlope += slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue;
                    slopeAnalysisData.posSlopeCnt++;
                }else if ((slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue < 0) && (slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue < slopeAnalysisData.maxNegSlope)){
                    //must be negative..
                    slopeAnalysisData.maxNegSlope = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue;
                    slopeAnalysisData.maxNegSlopeDate = slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].thisDate;
                    slopeAnalysisData.aveNegSlope += slopeAnalysisData.slopeAnalysisPerDay[slopeCnt].slopeValue;
                    slopeAnalysisData.negSlopeCnt++;
                }
                //no longer veryfirst slope...
                process.setFirstSlope(false);
                if (process.didDirectionChange() == true){
                    //idx == 0, means today, 1 == yesterday..store this..
                    slopeResult.dayInTradingYear = idx;
                    slopeResult.closePrice = closePrice;
                    slopeResult.maxClosingPrice = maxClosingPrice;
                    maxClosingPrice = 0.0;
                    actWally.addToLog(slopeResult);   
                }
                //remember the last three no matter what they were...
                actWally.addHistory(slopeResult);
            }
            //log initial stock price..back 1 year ago...    
            slopeAnalysisData.initialStockPrice1YearAgo = slopeAnalysisData.slopeAnalysisPerDay[0].todaysClose;
                
            slopeAnalysisData.slopeCnt = slopeCnt;
            slopeAnalysisData.avePosSlope = slopeAnalysisData.avePosSlope / slopeAnalysisData.posSlopeCnt;
            slopeAnalysisData.aveNegSlope = slopeAnalysisData.aveNegSlope / slopeAnalysisData.negSlopeCnt;
            currentClosePrice = dArr[0].close;
            
            slopeResult.currentClosePrice = currentClosePrice;
            slopeAnalysisData.initialStockPriceNow = currentClosePrice;
            
            if ((wantedSlope == DIRECTION_CHANGE) && (process.didDirectionChange() == true)) {
                slopeResult.changeInDirection = true;               
                actWally.addToLog(slopeResult);
                System.out.println("\nChange In Direction NOW!");    
            }else if ((wantedSlope == TREND_UP) && (process.currentTrendPositive == true)) {               
                actWally.addToLog(slopeResult);
                System.out.println("\nCurrent Slope Positive.");    
            }else if ((wantedSlope == TREND_DN) && (process.currentTrendPositive == false)) {               
                actWally.addToLog(slopeResult);
                System.out.println("\nCurrent Slope Negative.");    
            }
 
            //remember the last three no matter what they were...
            actWally.addHistory(slopeResult);

            this.restartAccess();            
            actWally.stdDev50Day = this.stdDev50Day;
            actWally.volDev50Day = this.vol50Day;
            actWally.stdDev100Day = this.stdDev100Day;
            actWally.volDev100Day = this.vol100Day;
            return actWally;

        }
                
        public HistorySlope findSlopes(int wantedSlope){
            /* 
            Scans the historical data of the 10Day moving average and looks for every instance
            of slope. Right now slope is fixed at zero. The dates of when this slope occurred in history is saved. 
            Slope is calculated by: slope = rise / run. Rise is the change in price and run is the time period.
            Currently the run is fixed at 5 trading day values of the 10Day MA points.
            
            */
            
            slopeData slopeResult;
            slopeData lastSlopeStored = new slopeData();
            double day1 = 0.0;
            double day5 = 0.0;
            double closePrice = 0.0;
            double currentClosePrice = 0.0;
            boolean found = false;
            actWally = new HistorySlope();
            actWally.createLog(this.size);
            final int RUN_SLOPE_PERIOD = SLOPE_DAYS;
            data dArr[] = new data[this.size + 1];
            int tmpTicks = 0;
            int saveSz = this.size;
            int idx = 0;
            processSlopes process = new processSlopes();
            this.restartAccess();
            
            
            /* put data from vec into array for easier manipulating */
            for(idx = 0; idx < this.size; idx++) {
               dArr[idx] = new data();
               dArr[idx] = this.getNext();
            }
            
            /* dArr[0] now has todays data..dArr[1] has yesterdays etc..
               We start from oldest data to newest data. 
               first look for begining of real data ..
            */
            for (idx = (saveSz -1), found = false ; (found == false); idx--){
                if (dArr[idx].mA10Day > 0) {
                    found = true;
                }            
            }
            // this loop for history..
            for (idx = (idx + 1); idx >= RUN_SLOPE_PERIOD; idx -= RUN_SLOPE_PERIOD) {
                slopeResult = new slopeData();
                day1 = dArr[idx].mA10Day;
                slopeResult.dateP1 = dArr[idx].date;
                day5 = dArr[idx - RUN_SLOPE_PERIOD].mA10Day;
                slopeResult.dateP2 = dArr[idx - RUN_SLOPE_PERIOD].date;
                //get close price for P/L later
                closePrice = dArr[idx - RUN_SLOPE_PERIOD].close;
                
                process.workOnThisSlope(slopeResult, RUN_SLOPE_PERIOD);
                process.doIt(day1, day5);
                if (process.didDirectionChange() == true){
                    slopeResult.closePrice = closePrice;
                    actWally.addToLog(slopeResult);   
                }
                //remember the last three no matter what they were...
                actWally.addHistory(slopeResult);
            }
            // now do today calculation...
            slopeResult = new slopeData();
            day1 = dArr[0].mA10Day;
            slopeResult.dateP1 = dArr[0].date;
            day5 = dArr[RUN_SLOPE_PERIOD].mA10Day;
            slopeResult.dateP2 = dArr[RUN_SLOPE_PERIOD].date;
            currentClosePrice = dArr[0].close;
            
            slopeResult.currentClosePrice = currentClosePrice;
            
            process.workOnThisSlope(slopeResult, RUN_SLOPE_PERIOD);
            process.doIt(day5, day1);
            
            if ((wantedSlope == DIRECTION_CHANGE) && (process.didDirectionChange() == true)) {
                slopeResult.changeInDirection = true;               
                actWally.addToLog(slopeResult);
                System.out.println("\nChange In Direction NOW!");    
            }else if ((wantedSlope == TREND_UP) && (process.currentTrendPositive == true)) {               
                actWally.addToLog(slopeResult);
                System.out.println("\nCurrent Slope Positive.");    
            }else if ((wantedSlope == TREND_DN) && (process.currentTrendPositive == false)) {               
                actWally.addToLog(slopeResult);
                System.out.println("\nCurrent Slope Negative.");    
            }
                
            
            
            //remember the last three no matter what they were...
            actWally.addHistory(slopeResult);

            this.restartAccess();
            
            System.out.println("\n total pivit points:" + actWally.logIdx);
            System.out.println(
                    "\n Largest uptrend slope: " + process.getSLopeUpMax() + 
                    " on Date: " + process.getSLopeUpMaxDate()+ 
                    ". Largest downtrend slope: " + process.getSLopeDnMax() +
                    " on Date: " + process.getSLopeDnMaxDate() + 
                    "."
            );
            for (idx = 0; idx <= actWally.logIdx; idx++){
                tmpTicks = (actWally.slopeLog[idx].positive == true) ? actWally.slopeLog[idx].trendDnTicks : actWally.slopeLog[idx].trendUpTicks;
                
                System.out.println("\nP"+idx+": "+
                        actWally.slopeLog[idx].dateP1+" to "+actWally.slopeLog[idx].dateP2 +
                        " Trend is now: " + actWally.slopeLog[idx].trend + 
                        " Last Trend was for " +
                        tmpTicks + " ticks."
                );
                
            }
            return actWally;

        }

        public class data {

			String ticker = "";
			String date = "";
			double open = 0;
			double high = 0;
			double low = 0;
			double close = 0;
			int volume = 0;
			int count = 0;
			double WAP = 0;
			boolean hasGaps = false;
			/* simple moving averages */
			double mA1Day = 0;
			double mA2Day = 0;
			double mA3Day = 0;
			double mA4Day = 0;
			double mA5Day = 0;
			double mA50Day = 0;
			double mA100Day = 0;
			double mA150Day = 0;
			double mA200Day = 0;
			double mA10Day = 0;
			double mA15Day = 0;
			double mA20Day = 0;
			double mA25Day = 0;
			double mA30Day = 0;
			double mA35Day = 0;
			double mA40Day = 0;
			double mA60Day = 0;
			double mA70Day = 0;
			double mA500Day = 0;
			
			/* percent above moving Averages */
			double percentAbove50 = 0;
			double percentAbove100 = 0;
			double percentAbove150 = 0;
			double percentAbove200 = 0;
			/* volatility (standare deviation )*/
			double mA50DaySd = 0;
			double mA100DaySd = 0;
			double mA200DaySd = 0;
			/*back test data*/
			boolean btTrendChanged = false;
			String btNewTrendis = "";
			String btTrendToday = "";
			int btConsecutiveDaysAbove10dma = 0;
			int btConsecutiveDaysBelow10dma = 0;
			double rsi;
			double rsiMa;
			/* RSI moving averages */
			double mA10DayRsi = 0;
			double mA20DayRsi = 0;
			double mA30DayRsi = 0;
			double mA50DayRsi = 0;
			double mA100DayRsi = 0;
			double mA150DayRsi = 0;
			double mA200DayRsi = 0;

			double slope1stDerivative;
			double slope2ndDerivative;
			double daysHigh;
			double daysLow;
			double yearsAveHigh;
			double yearsAveLow;

			public void data() {

			}

			public double getMa(int main) {
				double ave = 0.0;
				switch (main) {
					case 1:
						ave = this.mA1Day;
						break;
					case 2:
						ave = this.mA2Day;
						break;
					case 3:
						ave = this.mA3Day;
						break;
					case 4:
						ave = this.mA4Day;
						break;
					case 5:
						ave = this.mA5Day;
						break;
					case 10:
						ave = this.mA10Day;
						break;
					case 15:
						ave = this.mA15Day;
						break;
					case 20:
						ave = this.mA20Day;
						break;
					case 25:
						ave = this.mA25Day;
						break;
					case 30:
						ave = this.mA30Day;
						break;
					case 35:
						ave = this.mA35Day;
						break;
					case 40:
						ave = this.mA40Day;
						break;
					case 50:
						ave = this.mA50Day;
						break;
					case 60:
						ave = this.mA60Day;
						break;
					case 70:
						ave = this.mA70Day;
						break;
					case 100:
						ave = this.mA100Day;
						break;
					case 150:
						ave = this.mA150Day;
						break;
					case 200:
						ave = this.mA200Day;
						break;
					case 500:
						ave = this.mA500Day;
						break;
					default:
						System.out.println("\ngetMa error??");
						break;
				}
				return ave;
			}

			public void setMaPercentAbove(int maIn, double maValIn) {
				switch (maIn) {
					case 50:
						this.percentAbove50 = maValIn;
						break;
					case 100:
						this.percentAbove100 = maValIn;
						break;
					case 150:
						this.percentAbove150 = maValIn;
						break;
					case 200:
						this.percentAbove200 = maValIn;
						break;
					default:
						System.out.println("\nsetMa error??");
						break;
				}
			}

			public double getPercentAboveMa(int main) {
				double ave = 0.0;
				switch (main) {
					case 50:
						ave = this.percentAbove50;
						break;
					case 100:
						ave = this.percentAbove100;
						break;
					case 150:
						ave = this.percentAbove150;
						break;
					case 200:
						ave = this.percentAbove200;
						break;
					default:
						System.out.println("\ngetPercentAboveMa error??");
						break;
				}
				return ave;
			}

			public double getPercentWithInAbsMa(int main) {
				double ret = 0.0;
				switch (main) {
					case 50:
						ret = (double) (Math.abs(this.close - this.mA50Day) / this.mA50Day);
						break;
					case 100:
						ret = (double) (Math.abs(this.close - this.mA100Day) / this.mA100Day);
						break;
					case 150:
						ret = (double) (Math.abs(this.close - this.mA150Day) / this.mA150Day);
						break;
					case 200:
						ret = (double) (Math.abs(this.close - this.mA200Day) / this.mA200Day);
						break;
					default:
						System.out.println("\ngetPercentWithInAbsMa error??");
						break;
				}
				return ret;
			}

			public double getPercentWithInActMa(int main) {
				double ret = 0.0;
				switch (main) {
					case 50:
						ret = (double) ((this.close - this.mA50Day) / this.mA50Day);
						break;
					case 100:
						ret = (double) ((this.close - this.mA100Day) / this.mA100Day);
						break;
					case 150:
						ret = (double) ((this.close - this.mA150Day) / this.mA150Day);
						break;
					case 200:
						ret = (double) ((this.close - this.mA200Day) / this.mA200Day);
						break;
					default:
						System.out.println("\ngetPercentWithInActMa error??");
						break;
				}
				return ret;
			}

			public String getDate() {
				return date;
			}

			public double getClosePrice() {
				return close;
			}

			public double getOpenPrice() {
				return open;
			}

			public String getTicker() {
				return ticker;
			}

			public double getRsi() {
				return rsi;
			}

			public double getRsiMa() {
				return rsiMa;
			}

			public double getRsiMa20() {
				return mA20DayRsi;
			}

			public double getRsiMa50() {
				return mA50DayRsi;
			}

			public double getRsiMa100() {
				return mA100DayRsi;
			}

			public double getRsiMa200() {
				return mA200DayRsi;
			}

			public void set1stDerivative(double din) {
				slope1stDerivative = din;
			}

			public double get1stDerivative() {
				return slope1stDerivative;
			}

			public void set2ndDerivative(double din) {
				slope2ndDerivative = din;
			}

			public double get2ndDerivative() {
				return slope2ndDerivative;
			}

			public double getYearsAveHigh() {
				return yearsAveHigh;
			}

			public double getYearsAveLo() {
				return yearsAveLow;
			}
		}
	}


    public void testQuoteAndOrders(String inSymb, boolean inOption) {
        /*
         * monitor a quote, wait for correct bid/ask, place order, read and
         * display status throughout then sell when it hits an ask.
         *
         */
        //orderInfo orderTest = new orderInfo();
        int times = 0;
        int ordnum = 0;
        int wait = 0;
        boolean filled = false;
        double rval;
        boolean error = false;

        OrderStatus stat = new OrderStatus();

        quoteInfo actQuote = new quoteInfo();

        actQuote = getQuote(inSymb, inOption);
        ordnum = testOrder.setOrderInfo(inSymb, inOption, "BUY", "LMT", "DAY", actQuote.last, inOption ? 1 : 10);
        testOrder.placeOrder(ordnum);
        delay(100);
        while ((wait++ < 100) && !filled && !error) {
            stat = testOrder.getOrderStatus(ordnum);
            if ((error = stat.error) == true) {
                continue;
            }
            if (stat.statusRdy != true) {
                System.out.println("trying to buy " + inSymb + " , stat not ready. (" + ordnum + ")");
                delay(100);
                continue;
            }
            if (stat.remaining != 0) {
                System.out.println("status: " + stat.status + " remaining: " + stat.remaining);
            } else {
                filled = true;
                System.out.println("status: " + stat.status + " remaining: " + stat.remaining);
                System.out.println("BUY filled. Bought " + inSymb);
            }

            delay(200);
        }
        if (!filled && !error) {
            System.out.println("BUY not filled...canceling...(" + ordnum + ")");
            testOrder.cancelOrder(ordnum);
        } else {
            if (!error) {
                filled = false;
                wait = 0;
                actQuote = getQuote(inSymb, inOption);
                ordnum = testOrder.setOrderInfo(inSymb, inOption, "SELL", "LMT", "DAY", actQuote.bid, inOption ? 1 : 10);
                testOrder.placeOrder(ordnum);
                delay(100);
                while (!filled) {
                    while ((wait++ < 100) && !filled) {
                        stat = testOrder.getOrderStatus(ordnum);
                        if (stat.statusRdy != true) {
                            System.out.println("trying to sell " + inSymb + ", stat not ready.");
                            delay(100);
                            continue;
                        }
                        if (stat.remaining != 0) {
                            System.out.println("status: " + stat.status + " remaining: " + stat.remaining);
                        } else {
                            filled = true;
                            System.out.println("status: " + stat.status + " remaining: " + stat.remaining);
                            System.out.println("SELL filled. Sold " + inSymb);
                        }

                        delay(200);
                    }
                    if (!filled) {
                        System.out.println("SELL not filled...canceling...(" + ordnum + ")");
                        testOrder.cancelOrder(ordnum);
                        delay(1000);
                        actQuote = getQuote(inSymb, inOption);
                        ordnum = testOrder.setOrderInfo(inSymb, inOption, "SELL", "LMT", "DAY", actQuote.bid - .01, inOption ? 1 : 10);
                        testOrder.placeOrder(ordnum);
                        delay(100);
                        wait = 0;
                    }
                }
            }
        }
        if (error == true) {
            System.out.println("error testQuoteAndOrders: " + stat.errorCode + " " + stat.errorMsg);
        }

        while (times++ < 20) {
            actQuote = getQuote("BAC", false);
            System.out.println("last: " + actQuote.last + " bid:" + actQuote.bid + " ask:" + actQuote.ask + "    times:" + times);

        }
        actQuote = getQuote("BAC", false);
        System.out.println("last: " + actQuote.last + " bid:" + actQuote.bid + " ask:" + actQuote.ask);

        actQuote = getQuote("BAC", false);
        System.out.println("last: " + actQuote.last + " bid:" + actQuote.bid + " ask:" + actQuote.ask);

        System.out.println("done");
    }
    static void commitPosNeutral(int shortsTobuy, float delta) {
        /* we know how many puts to buy lets calculate the number of longs to buy so that
         * we have neutral outcome.
           */
        int longsToBuy;
        longsToBuy = Math.round(((float)shortsTobuy * delta * (float) 100.0));
        System.err.println("comitPosNeutral: longs to buy is " + longsToBuy + " with these shorts: " + shortsTobuy + " and this delta:  " + delta);
        
    }
    public ibApi(int clientIdIn) {
        quoteStreams = new quoteInfo[MAX_STREAMS];
        
        quoteInfo sq;
        String actSym[] = {"aapl", "rimm", "mo", "amzn", "c", "bac"};
        int times = 0;
        int symIdx = 0;
        int chainIdx = 0;
        quoteInfo qc = new quoteInfo();
        quoteInfo oq = new quoteInfo();
        boolean giveUpConnect = false;
        boolean connected = false;
        int usrIn;
        DecimalFormat df = new DecimalFormat("##.##");
        int ordNum1, ordNum2, ordNum3, ordNum4;
        apiClientId = clientIdIn;
        
        while (((connected = connectToIbHost()) == false) && (giveUpConnect == false)) {
            usrIn = commonGui.postConfirmationMsg("Connect To TWS Failed:" + "\n" + "Bring up TWS and enable API - Try again?");
            if (usrIn == 1) {
                giveUpConnect = true;               
            }else {
                giveUpConnect = false;
                apiClientId++;
                /* only allowd 8 client id accourding to IB so roll */
                if (apiClientId > 7){
                    apiClientId = 0;
                }
                commonGui.postInformationMsg("Tring with Client id: " + String.valueOf(apiClientId));
                
            }
            
        }
        
//"BAC   120121P00005000"
        if ((/*(connectToIbHost() ==*/ true) && (IBAPI_TEST_ON)) {
            if (QUOTE_ORDER_TEST_1){
                testQuoteAndOrders("PFE   120121P00021000", true);

            }
            if (ORDER_BASIC_ON) {
                testOrder = new orderInfo();

                //ordNum = testOrder.setOrderInfo("BAC", false /*option?*/, "BUY", "MKT", "DAY", 0 /*price*/, 30 /* numShares */);
                //ordNum = testOrder.setOrderInfo("BAC", false /*option?*/, "BUY", "LMT", "DAY", 7 /*price*/, 30 /* numShares */);
                ordNum1 = testOrder.setOrderInfo("PFE   120121P00021000",
                        true /*option?*/,
                        "BUY",
                        "LMT",
                        "DAY",
                        .09 /*price*/,
                        1 /* numShares */);
                System.out.println("placing order 1...");
                testOrder.placeOrder(ordNum1);
                delay(1000);

                System.out.println("placing order 2...");
                ordNum2 = testOrder.setOrderInfo("BAC",
                        false /*option?*/,
                        "BUY",
                        "LMT",
                        "DAY",
                        3 /*price*/,
                        30 /* numShares */);
                testOrder.placeOrder(ordNum2);
                delay(1000);


                System.out.println("requesting open orders ...");
                testOrder.reqOpenOrders();
                delay(1000);
                ordNum3 = testOrder.setOrderInfo("AAPL", false, "BUY", "LMT", "DAY", 400, 10);
                testOrder.placeOrder(ordNum3);

                delay(2000);

//                System.out.println("done...now cancel it...");
                testOrder.cancelOrder(ordNum1);
                testOrder.cancelOrder(ordNum2);
                testOrder.cancelOrder(ordNum3);
                System.out.println("Canceled..");
            }
            /* go get all option chain infor for this symbol */
            if (CHAIN_ON) {
                actChain.getOptionChain("AAPL");
            }

            while ((chainIdx++ < actChain.chainSz()) && CHAIN_ON) {

                qc = actChain.chainGetNext();

                if ((oq = getQuote(qc.optionSymbol, true)) != null) {
                    //if (((oq.delta > .2) && ( oq.delta < .8)) || (oq.delta > -.2) && ( oq.delta < -.8)) {
                    //System.out.println("\nix  bid  ask   last    volume   openInt   Symbol   Strike   Type   Delta   date");
                    System.out.format("\n%2s %6s %6s   %6s %4s %8s %8s %8s %8s %8s          %8s",
                            "ix",
                            "bid",
                            "ask",
                            "last",
                            "volume",
                            "openInt",
                            "underlying",
                            "strike",
                            "type",
                            "delta",
                            "optionSym");
                    System.out.format("\n%2d   %3.3f %3.3f   %3.3f %4d %8d  %6s       %3.3f       %2s       %3.3f   %26s\n",
                            actChain.chainGetIdx(),
                            oq.bid,
                            oq.ask,
                            oq.last,
                            oq.volume,
                            (qc.cpType.equals("C") ? oq.cOpenInterest : oq.pOpenInterest),
                            qc.symbol,
                            qc.strikePrice,
                            qc.cpType,
                            oq.delta,
                            qc.optionSymbol);

                    //}
                    delay(500);
                }

                if ((CHAIN_REPEAT == true) && chainIdx >= actChain.storeIdx) {
                    chainIdx = 0;
                    actChain.chainIdxReset();
                }

            }
            //PFE   120121C00010000
            times = 0;
            while (times++ < 2) {
                delay(1000);
            }
            times = 0;
            while (times++ < 100) {
                if ((oq = getQuote("PFE   120121C00010000", true)) != null) {
                    System.out.format("\n%6s %6s %6s %4s %8s   %8s %8s ",
                            "bid",
                            "ask",
                            "last",
                            "volume",
                            "cOI",
                            "pOI",
                            "delta");
                    System.out.format("\n%3.3f  %3.3f  %3.3f %4d %8d %8d     %3.3f\n",
                            oq.bid,
                            oq.ask,
                            oq.last,
                            oq.volume,
                            oq.cOpenInterest,
                            oq.pOpenInterest,
                            oq.delta);
                }
            }
            while (symIdx < 1/*actSym.length*/) {
                if ((sq = getQuote(actSym[symIdx], false)) != null) {
                    System.out.println(actSym[symIdx] + " last " + sq.last);
                    System.out.println(actSym[symIdx] + " bid " + sq.bid);
                    System.out.println(actSym[symIdx] + " ask " + sq.ask);
                    System.out.println(actSym[symIdx] + " volume " + sq.volume);
                    System.out.println(actSym[symIdx] + " optionCallVolume " + sq.cOpenInterest);
                    System.out.println(actSym[symIdx] + " optionPutVolume " + sq.pOpenInterest);

                    System.out.println("\n");

                    symIdx++;
                    if (symIdx == 1/*actSym.length*/) {
                        symIdx = 0;
                        delay(500);
                    }
                }


            }


            System.out.println("done.");
        }
        System.out.println("Calling reqMarketDataType(2) to turn on Frozen data when market closes...");
        m_client.reqMarketDataType(2);
        double is = 0;
        double im = 0;
        double d1 = 500.4899494949494;
        double d2 = .448888484848484848;
        double d3 = 1234567.5352252532234245;
        double d4 = 0.0;
        String tmpStr = String.format("%s %d","well?", 3);
        is = roundMe(-1, 2);
        is = .04999999998;
        im = .232222000099999;
        if ((im = myUtils.roundMe(is,2)) == .05) {
            System.err.println("y");
        }else {
            System.err.println("n");
        }
        im = .2322225678999;
        is = .2350222222222;
        im = myUtils.roundMe(im,5);
        is = myUtils.roundMe(is,5);
        System.err.println("im: " + im +" is: " + is );
        System.err.println("is - im / is :"+ myUtils.roundMe((is - im)/(is),5) * 100.0 + "%");
        commitPosNeutral(2, (float)0.3111);
        System.out.println(tmpStr);
        tmpStr += String.format("%s %f", " added: ", .02);
        System.out.println(tmpStr);
        
        m_client.reqManagedAccts();
//        actAccountInfo.reqUpdateAccountInformation(true);
        
        d1 = -500.4899494949494;
        d2 = -.448888484848484848;
        d3 = -1234567.5352252532234245;
        
        System.err.println("\nd1 =  " + d1 + " turncated is : ");
        d4 = myUtils.truncate(d1, 3);
        System.err.println("d4 =  " + d4);
        
        System.err.println("\nd2 =  " + d2 + " turncated is : ");
        d4 = myUtils.truncate(d2, 3);
        System.err.println("d4 =  " + d4);
        
        System.err.println("\nd3 =  " + d3 + " turncated is : ");
        d4 = myUtils.truncate(d3, 3);
        System.err.println("d4 =  " + d4);
        
        d1 = 500.4899494949494;
        d2 = .448888484848484848;
        d3 = 1234567.5352252532234245;
        
        System.err.println("\nd1 =  " + d1 + " turncated is : ");
        d4 = myUtils.truncate(d1, 3);
        System.err.println("d4 =  " + d4);
        
        System.err.println("\nd2 =  " + d2 + " turncated is : ");
        d4 = myUtils.truncate(d2, 3);
        System.err.println("d4 =  " + d4);
        
        System.err.println("\nd3 =  " + d3 + " turncated is : ");
        d4 = myUtils.truncate(d3, 3);
        System.err.println("d4 =  " + d4);
        d3 = 1.3;
        System.err.println("\nd3 =  " + d3 + " turncated is : ");
        d4 = myUtils.truncate(d3, 3);
        System.err.println("d4 =  " + d4);
        String x = "2.3";
        String y = "23";
        if(x.indexOf(".") == -1){
            System.out.println("\nx == integer " + x);
        }else{
            System.out.println("\nx == decimal " + x);
        }
        if(y.indexOf(".") == -1){
            System.out.println("\ny == integer " + y);
        }else{
            System.out.println("\ny == decimal " + y);
        }
        System.out.println("\n\nWorking directory is: " + myUtils.getMyWorkingDirectory());
        String d = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1");
                
        System.out.println("\n..:" + d);
        d = myUtils.getMyWorkingDirectory("activeTrader_Ib_1", "myTradeMachine");
        System.out.println("\n..:" + d);
        nextValidId = -1;
        nextValudIdTO = 100;
        m_client.reqIds(-1);
        while((nextValudIdTO > 0) && (nextValidId == -1)){
            myUtils.delay(100);
            nextValudIdTO--;
        }
        if(nextValidId != -1){
            System.out.println("\nnextValudId: " + nextValidId);
        }
        /*m_contract = new Contract();
            m_contract.m_symbol = "aapl";
            m_contract.m_secType = "STK";
            m_contract.m_exchange = "SMART";
            m_contract.m_primaryExch = "ISLAND";
            m_contract.m_currency = "USD";
        m_client.reqFundamentalData(nextValidId, m_contract, "CalendarReport");
        */
    }

    /*
     * implementation of EWrapper
     */
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        // received price tick
        quoteInfo qi = new quoteInfo();
        
        String msg = EWrapperMsgGenerator.tickPrice(tickerId, field, price, canAutoExecute);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        qSem.acquire();

        xQuote = quoteStreams[tickerId];


        if (tickerId >= actChain.chainOffset) {
            tickerId -= actChain.chainOffset;
            /* check for bid */
            if (msg.indexOf("bidPrice") > 0) {
                actChain.quoteStreams[tickerId].bid = price;             //roundMe(price, 2);
                actChain.quoteStreams[tickerId].respBits |= BID; 
            } else if (msg.indexOf("lastPrice") > 0) {
                actChain.quoteStreams[tickerId].last = price;                  //roundMe(price, 2);
                actChain.quoteStreams[tickerId].respBits |= LAST;
            } else if (msg.indexOf("askPrice") > 0) {
                actChain.quoteStreams[tickerId].ask = price;                       //roundMe(price, 2);
                actChain.quoteStreams[tickerId].respBits |= ASK;
            } else if (msg.indexOf("open") > 0) {
                actChain.quoteStreams[tickerId].open = price;                              //roundMe(price, 2);
                actChain.quoteStreams[tickerId].respBits |= OPEN;
            } else if (msg.indexOf("close") > 0) {
                actChain.quoteStreams[tickerId].prevClose = price;                     //roundMe(price, 2);
                actChain.quoteStreams[tickerId].respBits |= PREV_CLOSE;
            } else if (msg.indexOf("volume") > 0) {
                actChain.quoteStreams[tickerId].volume = (int) price;
                actChain.quoteStreams[tickerId].respBits |= VOLUME;
            } else if (msg.indexOf("delta") > 0) {
                actChain.quoteStreams[tickerId].delta = roundMe(price, 5);
                actChain.quoteStreams[tickerId].respBits |= DELTA;
            } else if (msg.indexOf("underlying") > 0) {
                actChain.quoteStreams[tickerId].underlying = "none";
            } else if (msg.indexOf("strikePrice") > 0) {
                actChain.quoteStreams[tickerId].strikePrice = (float) price;
            } else if (msg.indexOf("description") > 0) {
                actChain.quoteStreams[tickerId].description = "none";
            } else if (msg.indexOf("value") > 0) {
                actChain.quoteStreams[tickerId].value = (float) price;
                actChain.quoteStreams[tickerId].respBits |= VALUE;
            } else if (msg.indexOf(": vol") > 0) {
                actChain.quoteStreams[tickerId].impliedVolatility = roundMe(price, 5);
            } else if (msg.indexOf("errorGettingQuote") > 0) {
                actChain.quoteStreams[tickerId].errorGettingQuote = false;
            }
            actChain.quoteStreams[tickerId].tickerUsed = tickerId;
            actChain.quoteStreams[tickerId].updateCnt++;

        } else {
            /* check for bid */
            if (msg.indexOf("bidPrice") > 0) {
                quoteStreams[tickerId].bid = price;             //roundMe(price, 2);
            } else if (msg.indexOf("lastPrice") > 0) {
                quoteStreams[tickerId].last = price;                  //roundMe(price, 2);
            } else if (msg.indexOf("askPrice") > 0) {
                quoteStreams[tickerId].ask = price;                       //roundMe(price, 2);
            } else if (msg.indexOf("open") > 0) {
                quoteStreams[tickerId].open = price;                              //roundMe(price, 2);
            } else if (msg.indexOf("close") > 0) {
                quoteStreams[tickerId].prevClose = price;                     //roundMe(price, 2);
            } else if (msg.indexOf("volume") > 0) {
                quoteStreams[tickerId].volume = (int) price;
            } else if (msg.indexOf("delta") > 0) {
                quoteStreams[tickerId].delta = roundMe(price, 5);
            } else if (msg.indexOf("underlying") > 0) {
                quoteStreams[tickerId].underlying = "none";
            } else if (msg.indexOf("strikePrice") > 0) {
                quoteStreams[tickerId].strikePrice = (float) price;
            } else if (msg.indexOf("description") > 0) {
                quoteStreams[tickerId].description = "none";
            } else if (msg.indexOf("value") > 0) {
                quoteStreams[tickerId].value = (float) price;
            } else if (msg.indexOf(": vol") > 0) {
                quoteStreams[tickerId].impliedVolatility = roundMe(price, 5);
            } else if (msg.indexOf("errorGettingQuote") > 0) {
                quoteStreams[tickerId].errorGettingQuote = false;
            }
            quoteStreams[tickerId].tickerUsed = tickerId;
            quoteStreams[tickerId].updateCnt++;


        }
        qSem.release();
    }

    public void tickSnapshotEnd(int tickerId) {
        isTickSnapshotEnd = true;
        String msg = EWrapperMsgGenerator.tickSnapshotEnd(tickerId);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
        String msg = EWrapperMsgGenerator.deltaNeutralValidation(reqId, underComp);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void fundamentalData(int reqId, String data) {
        String msg = EWrapperMsgGenerator.fundamentalData(reqId, data);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void currentTime(long time) {
        String msg = EWrapperMsgGenerator.currentTime(time);
        System.out.println(msg);
    }

    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
        String msg = EWrapperMsgGenerator.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void scannerDataEnd(int reqId) {
        String msg = EWrapperMsgGenerator.scannerDataEnd(reqId);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void scannerData(int reqId, int rank, ContractDetails contractDetails,
            String distance, String benchmark, String projection, String legsStr) {
        String msg = EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance,
                benchmark, projection, legsStr);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void scannerParameters(String xml) {

        System.out.println(EWrapperMsgGenerator.SCANNER_PARAMETERS);
        System.out.println(xml);
    }

    public void historicalData(int reqId, String date, double open, double high, double low,
            double close, int volume, int count, double WAP, boolean hasGaps) {
        String msg = EWrapperMsgGenerator.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        if (date.contains("finished") == true){
            actHistoricalData.filledDone = true;
        }else {
            actHistoricalData.actData.date = date;
            actHistoricalData.actData.open = open;
            actHistoricalData.actData.high = high;
            actHistoricalData.actData.low = low;
            actHistoricalData.actData.close = close;
            actHistoricalData.actData.volume = volume;
            actHistoricalData.actData.count = count;
            actHistoricalData.actData.WAP = WAP;
            actHistoricalData.actData.hasGaps = hasGaps;
            actHistoricalData.actData.ticker = actHistoricalData.forThisTicker;
            actHistoricalData.storeToNext(actHistoricalData.actData);         
        }
        
    }

    public void receiveFA(int faDataType, String xml) {
    }

    public void managedAccounts(String accountsList) {
        System.out.println(accountsList);
        setManagedAccounts(accountsList);
    }

    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
    }

    public void updateMktDepth(int tickerId, int position, int operation,
            int side, double price, int size) {
    }

    public void updateMktDepthL2(int tickerId, int position, String marketMaker,
            int operation, int side, double price, int size) {
    }

    public void execDetailsEnd(int reqId) {
    }

    public void execDetails(int reqId, Contract contract, Execution execution) {
    }

    public void contractDetails(int reqId, ContractDetails contractDetails) {
        String msg = EWrapperMsgGenerator.contractDetails(reqId, contractDetails);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        //System.out.println("contractDetails: reqId: "+reqId);
        quoteInfo newChainInfo = new quoteInfo();

        newChainInfo.underlying = contractDetails.m_summary.m_symbol;
        newChainInfo.optionDate = contractDetails.m_summary.m_expiry;
        newChainInfo.strikePrice = contractDetails.m_summary.m_strike;
        
        newChainInfo.cpType = contractDetails.m_summary.m_right;
        newChainInfo.optionSymbol = contractDetails.m_summary.m_localSymbol;
        newChainInfo.symbol = contractDetails.m_summary.m_symbol;

        newChainInfo.contractMonth = contractDetails.m_contractMonth;               
        actChain.chainAdd(newChainInfo);

    }

    public void contractDetailsEnd(int reqId) {
        quoteInfo tmp = new quoteInfo();
        String msg = EWrapperMsgGenerator.contractDetailsEnd(reqId);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        actChain.doneFilling = true;        
    }

    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    public void nextValidId(int orderId) {
        // received next valid order id
        String msg = EWrapperMsgGenerator.nextValidId(orderId);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        nextValidId = orderId;
    }

    public void accountDownloadEnd(String accountName) {
    }

    public void updateAccountTime(String timeStamp) {
        System.out.println(timeStamp);
        actAccountInfo.updateAccountTime(timeStamp);
    }

    public void updatePortfolio(Contract contract, int position, double marketPrice,
            double marketValue, double averageCost, double unrealizedPNL, double realizedPNL,
            String accountName) {
        actAccountInfo.updatePortfolioInfo(contract, position, marketPrice, marketValue, 
                                           averageCost, unrealizedPNL, realizedPNL, accountName);
    }

    public void updateAccountValue(String key, String value,
            String currency, String accountName) {
        String msg = EWrapperMsgGenerator.updateAccountValue(key, value, currency, accountName);
        System.out.println(msg);
        actAccountInfo.updateAccountInformation(key, value, currency, accountName);
    }

    public void openOrderEnd() {
        // received open order end
        String msg = EWrapperMsgGenerator.openOrderEnd();
        if (API_MSGS_ON) {
            System.out.println(msg);
        }

    }

    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        // received open order
        String msg = EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState);

        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        if(testOrder != null) {
            testOrder.updateOrderData(orderId, contract, order, orderState);
        }else{
            System.out.println("openOrder Error: testOrder is null...");
        }


    }

    public void orderStatus(int orderId, String status, int filled, int remaining,
            double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld) {
        // received order status
        String msg = EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining,
                avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
        // make sure id for next order is at least orderId+1
        System.out.println("orderId" + orderId);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        if(testOrder != null) {
            testOrder.updateOrderStatus(orderId, status, filled, remaining, avgFillPrice);
        }else{
            System.out.println("orderStatus Error: testOrder is null...");
        }

    }

    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints,
            double impliedFuture, int holdDays, String futureExpiry, double dividendImpact,
            double dividendsToExpiry) {
        // received EFP tick
        String msg = EWrapperMsgGenerator.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints,
                impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void tickString(int tickerId, int tickType, String value) {
        // received String tick
        String msg = EWrapperMsgGenerator.tickString(tickerId, tickType, value);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void tickGeneric(int tickerId, int tickType, double value) {
        // received generic tick
        String msg = EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }

    }

    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend,
            double gamma, double vega, double theta, double undPrice) {
        // received computation tick
        String msg = EWrapperMsgGenerator.tickOptionComputation(tickerId, field, impliedVol, delta, optPrice, pvDividend,
                gamma, vega, theta, undPrice);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }


        if ((msg.indexOf("delta") > 0) && (delta < 1.0)/* && (field == TickType.BID_OPTION) */) {
            qSem.acquire();
            if (tickerId >= actChain.chainOffset) {
                actChain.quoteStreams[tickerId - actChain.chainOffset].delta = roundMe(delta, 5);
                actChain.quoteStreams[tickerId - actChain.chainOffset].tickerUsed = tickerId;
                actChain.quoteStreams[tickerId - actChain.chainOffset].updateCnt++;
                actChain.quoteStreams[tickerId - actChain.chainOffset].respBits |= DELTA;
            }else {
                quoteStreams[tickerId].delta = roundMe(delta, 5);
            }

            qSem.release();
        }

        if ((msg.indexOf(": vol") > 0) && (impliedVol < 10.0) && (field == TickType.BID_OPTION)){
            qSem.acquire();
            if (tickerId >= actChain.chainOffset) {
                actChain.quoteStreams[tickerId - actChain.chainOffset].impliedVolatility = roundMe(impliedVol, 5);
                actChain.quoteStreams[tickerId - actChain.chainOffset].tickerUsed = tickerId;
                actChain.quoteStreams[tickerId - actChain.chainOffset].updateCnt++;
            }else {
                quoteStreams[tickerId].impliedVolatility = roundMe(impliedVol, 5);
            }

            qSem.release();
        }

        

    }

    public void tickSize(int tickerId, int field, int size) {
        // received size tick
        String msg = EWrapperMsgGenerator.tickSize(tickerId, field, size);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
        qSem.acquire();
        /* check if this is for a chain... */
        if (field == 8){
//            System.out.println("size = " + size);
        }
        if (tickerId >= actChain.chainOffset) {
            
            if (msg.indexOf("volume") > 0) {
                actChain.quoteStreams[tickerId - actChain.chainOffset].volume = (int) size;
                actChain.quoteStreams[tickerId - actChain.chainOffset].respBits |= VOLUME;
            } else if (msg.indexOf("OptionCallOpenInterest") > 0) {
                actChain.quoteStreams[tickerId - actChain.chainOffset].cOpenInterest = (int) size;
                actChain.quoteStreams[tickerId - actChain.chainOffset].respBits |= OPEN_INTEREST;
            } else if (msg.indexOf("OptionPutOpenInterest") > 0) {
                actChain.quoteStreams[tickerId - actChain.chainOffset].pOpenInterest = (int) size;
                actChain.quoteStreams[tickerId - actChain.chainOffset].respBits |= OPEN_INTEREST;
            }
            actChain.quoteStreams[tickerId - actChain.chainOffset].tickerUsed = tickerId;
            actChain.quoteStreams[tickerId - actChain.chainOffset].updateCnt++;

        }else {
            
            if (msg.indexOf("volume") > 0) {
                quoteStreams[tickerId].volume = (int) size;
            } else if (msg.indexOf("OptionCallOpenInterest") > 0) {
                quoteStreams[tickerId].cOpenInterest = (int) size;
            } else if (msg.indexOf("OptionPutOpenInterest") > 0) {
                quoteStreams[tickerId].pOpenInterest = (int) size;
            }
            
        }
        qSem.release();
    }

    public void connectionClosed() {
        String msg = EWrapperMsgGenerator.connectionClosed();
        JOptionPane.showConfirmDialog(null, "connectionClosed  calling INFORM...."+msg);
        System.out.println("connectionClosed: "+ msg);
        //activetrader.menu.inform(this, msg);
        if (API_MSGS_ON) {
            System.out.println(msg);
        }
    }

    public void error(String str) {
        String msg = EWrapperMsgGenerator.error(str);
        System.out.println(msg);

    }

    public void error(int id, int errorCode, String errorMsg) {
        // received error fuckerror
        String msg = EWrapperMsgGenerator.error(id, errorCode, errorMsg);
        System.out.println(msg);
        for (int ctr = 0; ctr < faErrorCodes.length; ctr++) {
            faError |= (errorCode == faErrorCodes[ctr]);
        }
        if ((id < 1000) && (errorCode == 200)) {
         
            if ((actChain != null) && (actChain.turnStreamsOnToo == true)) { 
                if (id >= actChain.chainOffset) {   
                    actChain.quoteStreams[id - actChain.chainOffset].errorCodeFromApi = errorCode;
                    actChain.quoteStreams[id - actChain.chainOffset].errorMsgFromApi = errorMsg;
                }else {
                    actChain.quoteStreams[id].errorCodeFromApi = errorCode;
                    actChain.quoteStreams[id].errorMsgFromApi = errorMsg;
                }
            }
            if(actChain != null) 
                actChain.errorFillingFromApi = true;
            //System.out.println("error 200 from API...No contract for this ticker..");
            
        }
        if (errorCode == MKT_DEPTH_DATA_RESET) {
            System.out.println("market depth data reset.");
        }
        if (errorCode == NO_HISTORICAL_DATA_FOUND) {
            System.out.println("No historical data query found for ticker id:");
            actHistoricalData.errorMsg = "No historical data query found for ticker id";
        }
        
        if(testOrder != null) {
            testOrder.updateOrderError(id, errorCode, errorMsg);
        }else{
            System.out.println("orderError Error: testOrder is null...");
        }
        /* post/handle any connection alarms */
        if (apiAlarms != null) {
            apiAlarms.postWarning(errorCode, errorMsg);
        }
        /*
        if(actHistoricalData != null){
            actHistoricalData.fuckError = true;
        }
        */
    }

    public void error(Exception ex) {
        // do not report exceptions if we initiated disconnect
        if (!m_disconnectInProgress) {
            String msg = EWrapperMsgGenerator.error(ex);
            JOptionPane.showConfirmDialog(null, "ERROR calling INFORM...." + msg);
            System.out.println("error: "+ex+"  "+msg);
            ex.printStackTrace();
            //activetrader.menu.inform(this, msg);
        }
    }
    public void marketDataType(int reqId, int marketDataType) {
        System.out.println("marketDataType: reqId " + reqId + "marketDataType "+ marketDataType);
        
    }
}
