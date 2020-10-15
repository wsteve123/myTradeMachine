/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author earlie87
 */
public class TradeRequestData {
    /*
    A list of TradeRequestData object. This list will be all active, waiting, or filled requests.
    A request will have the following data:
        - ticker string
        - An Id number
        - operation i.e buy or sell
        - sharesToTrade
        - sharesTraded
        - number of lots
        - State i.e. "dormant", "waiting", "active", "filled", "errored"
        - fill attempts
        - Bid price
        - Ask price
        - Last price
        - Market price
        - Lots[10] up to ten lots, meaning an order partially filled multiple times at different prices (upto 10)
        - Average price of above losts.
        - saved $$ amount. how much did we save by negociating a price (limit orders) vs. market price.
    */
    //states:
    private static final String IDLE = "idle";
    private static final String SUBMITED = "submited";    
    private static final String FILLED = "filled";
    private static final String PARTIAL_FILL = "pfill";
    private static final String ERRORED = "errored";
    private static final String CANCELLED = "cancelled";
    private static final String SPIN = "spin";
    
    //operation options:
    private static final String NO_OP = "none";
    private static final String OP_BUY = "buy";
    private static final String OP_SELL = "sell";
       
    public enum TradeStates{ 
        oIdle(IDLE),
        oSubmitted(SUBMITED),
        oFilled(FILLED),
        oPFill(PARTIAL_FILL),
        oCancelled(CANCELLED),
        oSpin(SPIN),
        oErrored(ERRORED);
        private String strVal = "";
        private TradeStates(String sin){
            strVal = sin;
        }
    }    
    public enum TradeOptions{
        oNoOp(NO_OP),
        oOpBuy(OP_BUY),
        oOpSell(OP_SELL);
        
        private String strVal = "";
        private TradeOptions(String sin) {
            strVal = sin;
        }
    }
    public enum TradeModes{
        oMarket,        
        oLimitAlgo;
    }
    private String ticker;
    private int idNumber;
    private int lotNum = 0;
    private TradeStates status = TradeStates.oIdle;
    private TradeOptions operation = TradeOptions.oNoOp;
    private int sharesToTrade = 0;
    private int originalSharesToTrade = 0;
    private int partialSharesToTrade = 0;
    private int sharesTraded = 0;
    private int sharesTradedThisCycle = 0;
    boolean cancelled = false;
    private int fillAttempts = 0;
    private int priceErrors = 0;
    private double bidPrice = 0.0;
    private double askPrice = 0.0;
    private double lastPrice = 0.0;
    private double marketPrice = 0.0;
    private double[] lots = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    private double aveFillPrice = 0.0;
    private double moneySaved = 0.0;
    private double offerMade = 0.0;
    private int sharesRemaining = 0;
    private boolean partiallyFilled = false;
    private boolean filledNow = false;
    private boolean isUpdated = false;
    private String tradeSpecifics = "";
    public void setBid(double bin){
        bidPrice = bin;
    }
    public double getBid(){
        return bidPrice;
    }
    public void setAsk(double ain){
        askPrice = ain;
    }
    public double getAsk(){
        return askPrice;
    }
    public void setLast(double lin){
        lastPrice = lin;
    }
    public double getLast(){
        return lastPrice;
    }
    public void setMarket(double min){
        marketPrice = min;
    }
    public double getMarket(){
        return marketPrice;
    }
    public void setId(int idin){
        idNumber = idin;
    }
    public void setStatus(TradeStates sin){
        status = sin;
    }
    public TradeStates getStatus(){
        return status;
    }
    public String getStatusStr(){
        return status.strVal;
    }
    public int getId(){
        return (idNumber);
    }
    public void setTicker(String tickerin){
        ticker = tickerin;
    }
    public void setOperation(TradeOptions opin){
        operation = opin;
        
    }
    public TradeOptions getOperation(){
        return operation;
    }
    public String getOperationStr(){
        return operation.strVal;
    }
    public String getTicker(){
        return (ticker);
    }
    public TradeRequestData(){
        
    }
    public void setSharesToTrade(int sharesin){
        sharesToTrade = sharesin;
    }
    public int getSharesToTrade(){
        return sharesToTrade;
    }
    public void setOriginalSharesToTrade(int sharesin){
        originalSharesToTrade = sharesin;
    }
    public int getOriginalSharesToTrade(){
        return originalSharesToTrade;
    }
    public void setPartialSharesToTrade(int sharesin){
        partialSharesToTrade = sharesin;
    }
    public int getPartialSharesToTrade(){
        return partialSharesToTrade;
    }
    public void setSharesTraded(int sharesin){
        sharesTraded = sharesin;
    }
    public void addToSharesTraded(int sharesin){
        sharesTraded += sharesin;
    }    
    public int getSharesTraded(){
        return sharesTraded;
    }
    public void setSharesTradedThisCycle(int sharesin){
        sharesTradedThisCycle = sharesin;
    }
    public void addToSharesTradedThisCycle(int sharesin){
        sharesTradedThisCycle += sharesin;
    }
    public int getSharesTradedThisCycle(){
        return sharesTradedThisCycle;
    }
    public void setOffer(double oin){
        offerMade = oin;
    }
    public double getOffer(){
        return offerMade;
    }
    public void setMoneySaved(double min){
        moneySaved = min;
    }
    public void addMoneySaved(double min){
        moneySaved += min;
    }
    public double getMoneySaved(){
        return moneySaved;
    }
    public void setFillAttempts(int fillin){
        fillAttempts = fillin;
    }
    
    public int getFillAttempts(){
        return fillAttempts;
    }
    public void setPriceErrors(int oin){
        priceErrors = oin;
    }
    
    public int getPriceErrors(){
        return priceErrors;
    }
    public double getFilledPrice(){
        return aveFillPrice;
    }
    public void setFilledPrice(double fin){
        aveFillPrice = fin;
    }
    public void setLotFilledPrice(double fin){
        lots[lotNum++] = fin;
    }
    public void setIsUpdated(boolean uin){
        isUpdated = uin;
    }
    public boolean IsUpdated(){
        return isUpdated;
    }
    public double getLotFilledPrice(int indx){
        double ret = -1;
        if(indx < lots.length){
            ret = lots[indx];
        }
        return ret;
    }
    public int getNumberOfLots(){
        return lotNum;
    }
    public double getAveLotFilledPrice(){
        int x = 0;
        double ret = -1;
        for (x = 0; x < lotNum; x++){
            ret += lots[x];
        }
        if(lotNum > 0){
            ret /=  lotNum;
        }        
        return ret;
    }
    public void setSharesRemaining(int sin){
        sharesRemaining = sin;
    }
    public int getSharesRemaining(){
        return sharesRemaining;
    }
    public void addSharesRemaining(int sin){
        sharesRemaining += sin;
    }
    public void setPartiallyFilled(boolean pin){
        partiallyFilled = pin;
    }
    public boolean isPartiallyFilled(){
        return partiallyFilled;
    }
    public void setIsFilledNow(boolean pin){
        filledNow = pin;
    }
    public boolean isFilledNow(){
        return filledNow;
    }
    public void setCancelled(boolean cin){
        cancelled = cin;
    }
    public boolean getCancelled(){
        return cancelled;
    }
    public void setTradeSpecifics(String trin){
        tradeSpecifics = trin;
    }
    public String getTradeSpecifics(){
        return tradeSpecifics;
    }
            
}
