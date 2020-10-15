/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;
import ibTradeApi.ibApi;
import tradeMenus.TradeActivityDialogForm;

/**
 *
 * @author earlie87
 */
public class TradeActivity extends Thread{
    private final int ONE_MSEC = 1;
    private final int ONE_100MSEC = ONE_MSEC * 100;
    private final long ONE_SEC = ONE_100MSEC * 10;
    private final long FIVE_SEC = ONE_SEC * 5;
    private final long TEN_SEC = ONE_SEC * 10;
    private final long TWELVE_SEC = ONE_SEC * 12;
    private final long TWENTY_SEC = ONE_SEC * 20;
    private final long FIFTEEN_SEC = ONE_SEC * 15;
    private final long ONE_MIN = ONE_SEC * 60;
    private final long FIVE_MIN = ONE_MIN * 5;
    private final long TEN_MIN = FIVE_MIN * 2;
    private int runTimes = 0;
    private long currentTimeMs = 0;
    private long lastTimeMs = 0;
    private TradeRequestList actTradeList = null;
    long runFrequency = ONE_SEC;
    private boolean weRun = false;
    
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    private ibApi actIbApi = ibApi.getActApi();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain(); 
    private ibApi.OrderStatus orderStatus = new ibApi.OrderStatus();
    
    private TradeActivityDialogForm actTradeActivityFrameform;
    
    private class OrderData {

        private String accountNumber;
        private TradeRequestData tradeReqData;
        //buy or sell
        private String operation = "";
        //market or LimitAlgo
        private String orderType = "";
        private int orderId = 0;
        private int sharesToTrade = 0;
        private double currentOffer = 0.0;
        private String ticker = "";
        //orderStatus
        private TradeRequestData.TradeStates tradeState = TradeRequestData.TradeStates.oIdle;
        private boolean statusRdy = false;
        private boolean statusRdyOnly = false;
        private boolean filled = false;
        private boolean partialFill = false;
        private boolean canceled = false;
        private boolean orderError = false;
        private String orderErrorStr = "";
        private int orderErrorCode = 0;
        private int sharesFilled = 0;
        private double filledPrice = 0.0;
        private int sharesRemaining = 0;
        private boolean submitMode = false;
        public void changeOffer(Double oin){
            currentOffer = oin;
        }
        private OrderData(TradeRequestData trdin, String accNumin) {
            accountNumber = accNumin;
            operation = (trdin.getOperation().equals(TradeRequestData.TradeOptions.oOpBuy)) ? "BUY" : "SELL";
            if(actTradeMode.equals(TradeRequestData.TradeModes.oMarket)){
                //for market orders, set offer to zero..
                orderType = "MKT";
                currentOffer = 0.0;
            }else{
                orderType = "LMT";
                currentOffer = trdin.getOffer();
            }            
            sharesToTrade = trdin.getSharesToTrade();
            ticker = trdin.getTicker();                       
        }
        private void placeOrder(){
            if(sharesToTrade == 0){
                System.out.print("\nWTF!!!");
            }
            orderId = actIbApi.testOrder.setOrderInfo(
                    actAccountNumber, ticker, false /* no option */,
                    operation,
                    orderType,
                    "DAY", currentOffer /*price*/, sharesToTrade
            );
            actIbApi.testOrder.placeOrder(orderId);            
        }
        private ibApi.OrderStatus getOrderStatus(){
            orderStatus = actIbApi.testOrder.getOrderStatusFast(orderId);            
            if(orderStatus != null){
                statusRdy = true;
                filled = (orderStatus.status.contains("Filled"));
                orderError = orderStatus.error; 
                orderErrorStr = orderStatus.errorMsg;
                orderErrorCode = orderStatus.errorCode;
                if(filled == true){
                    tradeState = TradeRequestData.TradeStates.oFilled;
                }else if((orderStatus.status.contains("Submitted") && (orderStatus.remaining != 0))){
                    submitMode = true;
                    tradeState = TradeRequestData.TradeStates.oSubmitted;
                } else if(orderStatus.status.contains("Cancelled")){
                    canceled = true;
                    tradeState = TradeRequestData.TradeStates.oCancelled;
                }else if ((orderStatus.filled != 0) && (orderStatus.remaining != 0)){
                    partialFill = true;
                    tradeState = TradeRequestData.TradeStates.oPFill;
                }else if(orderError == true){
                    tradeState = TradeRequestData.TradeStates.oErrored;
                }              
                
                if((filled == true) && (!partialFill)){
                    sharesFilled = orderStatus.filled;
                }else if (partialFill == true){
                    sharesFilled = orderStatus.filled;
                    sharesRemaining = orderStatus.remaining;
                }else if (submitMode == true){
                    sharesFilled = orderStatus.filled;
                    sharesRemaining = orderStatus.remaining;
                }                
                filledPrice = orderStatus.aveFillPrice;                             
            }else{
                //check if we got an error from IB so read status only..
                orderStatus = actIbApi.testOrder.getOrderStatusOnly(orderId);
                if(orderStatus != null){
                    statusRdyOnly = true;
                    if(orderStatus.status.contains("Cancelled")){
                        //if ib is confirming cancel operation, not really an error then..
                        canceled = true;
                        orderError = false; 
                        orderErrorStr = "";
                        orderErrorCode = 0;
                        tradeState = TradeRequestData.TradeStates.oCancelled;
                    }else{
                        //must be a real error from IB..
                        tradeState = TradeRequestData.TradeStates.oErrored;
                        orderError = orderStatus.error; 
                        orderErrorStr = orderStatus.errorMsg;
                        orderErrorCode = orderStatus.errorCode;
                    }                    
                }
            }
            return orderStatus;
        }
        public ibApi.OrderStatus getOrderStatusTest() {
            orderStatus = actIbApi.testOrder.getOrderStatusOnly(orderId);
            if (orderStatus != null) {
                statusRdy = true;
                filled = (orderStatus.status.contains("Filled"));
                orderError = orderStatus.error;
                orderErrorStr = orderStatus.errorMsg;
                if (filled == true) {
                    tradeState = TradeRequestData.TradeStates.oFilled;
                } else if ((orderStatus.status.contains("Submitted") && (orderStatus.remaining != 0))) {
                    submitMode = true;
                    tradeState = TradeRequestData.TradeStates.oSubmitted;
                } else if (orderStatus.status.contains("Cancelled")) {
                    canceled = true;
                    tradeState = TradeRequestData.TradeStates.oCancelled;
                } else if ((orderStatus.filled != 0) && (orderStatus.remaining != 0)) {
                    partialFill = true;
                    tradeState = TradeRequestData.TradeStates.oPFill;
                } else if (orderError == true) {
                    tradeState = TradeRequestData.TradeStates.oErrored;
                }

                if ((filled == true) && (!partialFill)) {
                    sharesFilled = orderStatus.filled;
                } else if (partialFill == true) {
                    sharesFilled = orderStatus.filled;
                    sharesRemaining = orderStatus.remaining;
                } else if (submitMode == true) {
                    sharesFilled = orderStatus.filled;
                    sharesRemaining = orderStatus.remaining;
                }
                filledPrice = orderStatus.aveFillPrice;
            }
            return orderStatus;
        }

        public TradeRequestData.TradeStates getTradeState() {
            return tradeState;
        }
        public boolean isOrderStatusRdy(){
            return statusRdy;
        }
        public boolean isOrderFilled(){
            return filled;
        }
        public boolean isOrderPatialyFilled(){
            return partialFill;
        }
        public void cancelOrder(){
            actIbApi.testOrder.cancelOrder(orderId);
        }
        public double getFilledPrice(){
            return filledPrice;
        }
        public int getSharesFilled(){
            return sharesFilled;
        }
        public int getSharesRemaining(){
            return sharesRemaining;
        }
        public boolean isSubmitMode(){            
            return submitMode;
        }
        public String getErrorMsg(){
            return orderErrorStr;
        }
        public int getErrorCode(){
            return orderErrorCode;
        }
        public boolean didWeError(){
            return orderError;
        }
    }
    public void placeOrderNew(String ticker, boolean buyit, int sharesToTrade, String tradeSpecifics) {
        //do either buy or sell based on buyit                        
        sharesToTrade = Math.abs(sharesToTrade);

        TradeRequestData trd = new TradeRequestData();
        trd.setTicker(ticker);
        if (buyit == true) {
            System.out.println("\nbuying " + sharesToTrade + " shares..of +" + ticker);
            trd.setOperation(TradeRequestData.TradeOptions.oOpBuy);
        } else {
            System.out.println("\nselling " + sharesToTrade + " shares..of " + ticker);
            trd.setOperation(TradeRequestData.TradeOptions.oOpSell);
        }
        trd.setOriginalSharesToTrade(sharesToTrade);
        trd.setTradeSpecifics(tradeSpecifics);
        actTradeList.addOne(trd);
        actTradeList.bumpReqCnt();
    }
    
    public void stopIt() {
        weRun = false;
    }
    public boolean haveWeStoppedRunning(){
        return (weRun == false);
    }
    public TradeActivity(TradeRequestList tlist) {
        actTradeList = tlist;
        weRun = true;
        this.start();
    }
    public void workOnThisTradeActivityTbl(TradeActivityDialogForm tat){
        actTradeActivityFrameform = tat;
    }
    public void setTradeMode(TradeRequestData.TradeModes tmin){
        actTradeMode = tmin;
        
    }
    public TradeRequestData.TradeModes getTradeMode(){
        return actTradeMode;
    }
    public void setAccountNumber(String accin){
        actAccountNumber = accin;
    }
    public String getAccountNumber(){
        return actAccountNumber;
    }
    public boolean areWeDone(){
        return weAreDone;
    }
    private class Timer{
        private long lastTimeMs;
        private long currentTimeMs;
        private long actWaitTime;
        private boolean autoTrigger = false;
        private boolean timerStarted = false;
        private Timer(long waitTime, boolean autoReArm) {
            lastTimeMs = System.currentTimeMillis();
            actWaitTime = waitTime;
            autoTrigger = autoReArm;
            timerStarted = false;
        }        
        private void newTime(long waitTime){
            actWaitTime = waitTime;
        }
        boolean checkExpired(){
            boolean expired = false;
            if(timerStarted == false){
                return false;
            }
            currentTimeMs = System.currentTimeMillis();
            if (expired = ((currentTimeMs - lastTimeMs) >= actWaitTime)) {
                if(autoTrigger == true){
                   lastTimeMs = currentTimeMs;
                }else{
                    
                }
            }
            return expired;
        }
        private void start(){
            timerStarted = true;
            lastTimeMs = currentTimeMs = System.currentTimeMillis();            
        }
        private void stop(){
            timerStarted = false;
            lastTimeMs = currentTimeMs = 0;
        }
        private void reArm(boolean reArm){
            autoTrigger = reArm;
            if(autoTrigger == true){
                lastTimeMs = currentTimeMs;
            }
        }
        public boolean isTimerRunning(){
            return timerStarted;
        }
    }
    double currentOffer = 0.0;
    int fillAttempts = 0;
    int priceErrors = 0;
    Timer t1 = new Timer(TWELVE_SEC, true/*autoReArm*/);
    Timer ttest = new Timer(FIVE_SEC, true /*autoReArm*/);
    TradeRequestData workingOnThisOne = null;
    int workingOnThisReqNumber = 0;
    int actReqNumber = 0;
    TradeRequestData.TradeStates actTradeState = TradeRequestData.TradeStates.oIdle;
    //int actTradeNumber = 0;
    boolean weAreDone = false;
    TradeRequestData.TradeModes actTradeMode = TradeRequestData.TradeModes.oMarket;
    String actAccountNumber = ""; 
    //there can be only one order active at a time!
    OrderData actOrder = null;
    int totalFilled = 0;
    
    public void run() {
                
        TradeRequestData actTradeReq = new TradeRequestData();                       
        while ((weRun == true) && (weAreDone == false)) {           
            runTimes++;
            try {
                //find next in sequence request to process
                actTradeReq = actTradeList.findNext();
                if (actTradeReq != null) {
                    if (actTradeActivityFrameform != null) {
                        actTradeActivityFrameform.updateTradeActivtyTable(actTradeActivityFrameform, actTradeReq.getId(), actTradeReq); 
                        if(actTradeActivityFrameform.getOrderType().equals("")){
                            actTradeActivityFrameform.setOrderType(actTradeMode.equals(TradeRequestData.TradeModes.oMarket) ? "MARKET" : "AlgoLimit");  
                        }
                    }                    
                                        
                    if ((t1.isTimerRunning() == true) && (t1.checkExpired() == true)){
                        //timer expired.
                        fillAttempts++;                        
                        workingOnThisOne.setFillAttempts(fillAttempts);                        
                        actOrder.cancelOrder();
                        System.out.println("\nTimer expired Canceling order..");
                        workingOnThisOne.setStatus(TradeRequestData.TradeStates.oCancelled);
                        t1.stop();
                    }
                    //testing...
                    /*
                    if((workingOnThisOne !=  null) && 
                       (actOrder != null) && 
                       (actOrder.getOrderStatusTest() != null) && 
                       ((actOrder.getOrderStatusTest().error == true) && 
                       !(actOrder.getTradeState().equals(TradeRequestData.TradeStates.oCancelled)))
                     ){ 
                        System.out.println("\ngot here..error == " + actOrder.getOrderStatusTest().error + " errMsg: " + actOrder.getOrderStatusTest().errorMsg + " for ticker: " + workingOnThisOne.getTicker() + " orderTicker: " + actOrder.ticker);                       
                        workingOnThisOne.setStatus(TradeRequestData.TradeStates.oErrored);
                        processReq(workingOnThisOne);
                    }
                    */
                    if((workingOnThisOne !=  null) && 
                       (actOrder != null) && 
                       (actOrder.getOrderStatus() != null) && 
                       ((actOrder.getOrderStatus().statusRdy == true) || (actOrder.getOrderStatus().statusRdyOnly == true))
                     ){                                                                                                   
                        workingOnThisOne.setStatus(actOrder.getTradeState());
                        processReq(workingOnThisOne);                       
                    }else if(actTradeReq.getStatus().equals(TradeRequestData.TradeStates.oIdle)){
                        processReq(actTradeReq);
                    }else if (actTradeReq.getStatus().equals(TradeRequestData.TradeStates.oErrored)){
                        processReq(actTradeReq);
                    }
                    
                }                                  
                if((actTradeReq != null) && (actTradeList.areWeAllDone()== true)){                                     
                    weAreDone = true;
                }
                else{
                    weAreDone = false;                            
                }
                totalFilled = actTradeList.getFillCnt();
                if(actTradeActivityFrameform != null){
                    actTradeActivityFrameform.setTotalFilled(totalFilled);
                    actTradeActivityFrameform.setTotalOpen(actTradeList.sizeOf() - totalFilled);
                }
                
                TradeActivity.sleep(ONE_100MSEC);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("\nTradeActivity stopped running..");
        if (actTradeActivityFrameform != null) {
            actTradeActivityFrameform.setProgressText("All done.");

        }
    }
    void processReq(TradeRequestData req){
        ibApi.OrderStatus ordStat = new ibApi.OrderStatus();
        switch (req.getStatus()){
            case oIdle:
                //if we are idle and we are not working on one...start this one
                if (workingOnThisOne == null){ 
                    //offerOffset = 0;
                    req.setStatus(TradeRequestData.TradeStates.oSubmitted);
                    qInfo = actChain.getQuote(req.getTicker(), false);
                    req.setBid(qInfo.bid);
                    req.setAsk(qInfo.ask);
                    req.setLast(qInfo.last);
                    fillAttempts = req.getFillAttempts();
                    priceErrors = req.getPriceErrors();                    
                    currentOffer = (qInfo.bid + ((qInfo.ask - qInfo.bid) / 2));
                    if (req.getOperation().equals(TradeRequestData.TradeOptions.oOpBuy)) {
                        req.setMarket(qInfo.ask);
                        
                        currentOffer = currentOffer + (((double) fillAttempts) / 100.0);

                    } else if (req.getOperation().equals(TradeRequestData.TradeOptions.oOpSell)) {
                        req.setMarket(qInfo.bid);
                        
                        currentOffer = currentOffer - (((double) fillAttempts) / 100.0);
                    }                    
                    req.setOffer(myUtils.truncate(currentOffer, 2));
                    System.out.println("\noIdle -> oSubmitted State: reqSent for ticker: " + req.getTicker() + "  offer: " + req.getOffer() + " Shares: " + req.getOriginalSharesToTrade());
                    /*start request timeOut timer*/
                    t1.start();                                                            
                    workingOnThisOne = req;
                    if(workingOnThisOne.getSharesRemaining() != 0){
                        System.out.println("\noIdle - > oSubmitted State: OLD PARTIAL fill, shares remaining: " + workingOnThisOne.getSharesRemaining() + " set as SharesToTrade now.");
                        workingOnThisOne.setSharesToTrade(workingOnThisOne.getSharesRemaining());                        
                    }else{
                        workingOnThisOne.setSharesToTrade(workingOnThisOne.getOriginalSharesToTrade());                        
                    }
                    //clear cycle shares traded.
                    workingOnThisOne.setSharesTradedThisCycle(0);
                    //place order now..first create OrderData
                    actOrder = new OrderData(req, actAccountNumber);
                    //do it..
                    actOrder.placeOrder();
                    if ((actTradeActivityFrameform != null) && (workingOnThisOne != null)) {
                        actTradeActivityFrameform.setProgressText(
                                workingOnThisOne.getTicker()
                                + " (" + (workingOnThisOne.getId() + 1) + ") State: "
                                + workingOnThisOne.getStatusStr()
                        );
                    }
                }
                break;
            case oSubmitted:
                //we can only get here if we are working on one already..
                //only change state if different that actOrder state ie. don't repeat..
                if ((workingOnThisOne != null) && 
                    (actOrder.isSubmitMode()) && 
                    (workingOnThisOne.getSharesTradedThisCycle() != actOrder.getSharesFilled())
                   ){ 
                    System.out.println("\noSubmitted State confirmed..sharesFilled: " + actOrder.sharesFilled + " remaining: " + actOrder.sharesRemaining);
                    workingOnThisOne.setStatus(TradeRequestData.TradeStates.oSubmitted);
                    if(actOrder.getSharesRemaining() != workingOnThisOne.getSharesRemaining()){
                        System.out.println("\n  oSubmitted State..sharesFilled: " + actOrder.sharesFilled + " remaining: " + actOrder.sharesRemaining);
                        workingOnThisOne.addToSharesTraded(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());
                        workingOnThisOne.addToSharesTradedThisCycle(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());
                        workingOnThisOne.setSharesRemaining(actOrder.getSharesRemaining());
                    }else{
                        //don't keep displaying the same thing...only when it changes.
                    }
                }
                break;           
            case oFilled:
                if (workingOnThisOne != null) {
                    System.out.println("\norderFilled..sharesFilled: " + actOrder.getSharesFilled() + " remaining: " + actOrder.getSharesRemaining());
                    if (actOrder.getSharesFilled() == 0) {
                        //wtf!!!
                        System.out.println("\nsharesFilled == 0!!!WTF");
                        if(actOrder != null){
                           ordStat = actOrder.getOrderStatus();
                           System.out.println("\ntried filled again.." + ordStat.filled);
                        }
                        if ((actOrder != null)
                                && (actOrder.getOrderStatus() != null)
                                && (actOrder.getOrderStatus().statusRdy == true)) {
                            System.out.println("\ntried getSharesFilled again.." + actOrder.getSharesFilled());
                        }
                    }
                    workingOnThisOne.setStatus(TradeRequestData.TradeStates.oFilled);
                    if(workingOnThisOne.getCancelled() == true){
                        System.out.println("\norder previously cancelled, add to sharesFilled: ");
                        workingOnThisOne.addToSharesTraded(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());
                    }else{
                        workingOnThisOne.setSharesTraded(actOrder.getSharesFilled()); 
                    }                    
                    workingOnThisOne.setSharesRemaining(actOrder.getSharesRemaining());
                    workingOnThisOne.setFilledPrice(myUtils.truncate(actOrder.getFilledPrice(),2));
                    if (workingOnThisOne.getOperation().equals(TradeRequestData.TradeOptions.oOpBuy)) {
                        workingOnThisOne.setMoneySaved(myUtils.truncate(((workingOnThisOne.getMarket() - actOrder.getFilledPrice()) * actOrder.getSharesFilled()), 2));
                    } else {
                        workingOnThisOne.setMoneySaved(myUtils.truncate(((actOrder.getFilledPrice() - workingOnThisOne.getMarket()) * actOrder.getSharesFilled()), 2));
                    }                    
                    //clear shares traded this cycle because we filled..                    
                    workingOnThisOne.setSharesTradedThisCycle(0);
                    workingOnThisOne.setIsFilledNow(true);
                    t1.stop();
                    actTradeList.updateTotalSaved(workingOnThisOne.getMoneySaved());
                    if ((actTradeActivityFrameform != null) && (workingOnThisOne != null)) {
                        actTradeActivityFrameform.setProgressText(
                                workingOnThisOne.getTicker()
                                + " (" + (workingOnThisOne.getId() + 1) + ") State: "
                                + workingOnThisOne.getStatusStr()
                        );
                    }
                    actOrder = null;
                    workingOnThisOne = null;
                    
                }                
                break;
            case oPFill:
                if (workingOnThisOne != null) {
                    System.out.println("\norder partially filled.");
                    if ((actOrder.getSharesRemaining() != 0) && 
                        (workingOnThisOne.getSharesRemaining() != actOrder.getSharesRemaining())
                       ) {                        
                        System.out.println("\norder partially filled..sharesFilled: " + actOrder.getSharesFilled() + " sharesRemaining: " + actOrder.getSharesRemaining());
                        workingOnThisOne.setPartiallyFilled(actOrder.isOrderPatialyFilled());
                
                        //System.out.println("\norder previously cancelled..add to shares traded: " + actOrder.getSharesFilled());
                        workingOnThisOne.addToSharesTraded(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());
                        workingOnThisOne.addToSharesTradedThisCycle(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());                                                
                        workingOnThisOne.setFilledPrice(actOrder.getFilledPrice());
                        workingOnThisOne.setSharesRemaining(actOrder.getSharesRemaining());
                        
                        //add to money saved since it was a partial..
                        if (workingOnThisOne.getOperation().equals(TradeRequestData.TradeOptions.oOpBuy)) {
                            workingOnThisOne.addMoneySaved(myUtils.roundMe(((workingOnThisOne.getMarket() - actOrder.getFilledPrice()) * actOrder.getSharesFilled()), 2));
                        } else {
                            workingOnThisOne.addMoneySaved(myUtils.roundMe(((actOrder.getFilledPrice() - workingOnThisOne.getMarket()) * actOrder.getSharesFilled()), 2));
                        }
                    }
                    if ((actTradeActivityFrameform != null) && (workingOnThisOne != null)) {
                        actTradeActivityFrameform.setProgressText(
                                workingOnThisOne.getTicker()
                                + " (" + (workingOnThisOne.getId() + 1) + ") State: "
                                + workingOnThisOne.getStatusStr()
                        );
                    }
                }                
                break;
            case oErrored:
                if (workingOnThisOne != null) {
                    System.out.println("\nOrder Error..error msg: " + actOrder.getErrorMsg() + " for ticker: " + workingOnThisOne.getTicker() + " orderTicker: " + actOrder.ticker);
                    fillAttempts++;                  
                    workingOnThisOne.setFillAttempts(fillAttempts);                    
                    //see if min price variation error..
                    if(actOrder.getErrorCode() == 110){
                        priceErrors++;
                        workingOnThisOne.setPriceErrors(priceErrors);
                    }
                    //don't what else to do so leave it..
                    t1.stop();                    
                    actOrder = null;
                    workingOnThisOne = null;
                }
                System.out.println("\noErrored state! ");
                //back to idle to try again....
                req.setStatus(TradeRequestData.TradeStates.oIdle);
                break;
            case oCancelled:
                if(workingOnThisOne != null){
                    //wait for confirmation that order is cancelled from IB
                    if (actOrder.getTradeState().equals(TradeRequestData.TradeStates.oCancelled)) {
                        System.out.println("\nCancelled confirmed from IB. sharesTraded: " + actOrder.getSharesFilled() + " sharesRemaining: " + actOrder.sharesRemaining);
                        System.out.println("\n  actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle() = " + (actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle()));
                        workingOnThisOne.addToSharesTraded(actOrder.getSharesFilled() - workingOnThisOne.getSharesTradedThisCycle());
                        workingOnThisOne.setSharesRemaining(actOrder.getSharesRemaining());
                        workingOnThisOne.setStatus(TradeRequestData.TradeStates.oIdle);
                        workingOnThisOne.setCancelled(true);
                        workingOnThisOne.setSharesTradedThisCycle(0);
                        if ((actTradeActivityFrameform != null) && (workingOnThisOne != null)) {
                            actTradeActivityFrameform.setProgressText(
                                    workingOnThisOne.getTicker()
                                    + " (" + (workingOnThisOne.getId() + 1) + ") State: "
                                    + workingOnThisOne.getStatusStr()
                            );
                        }
                        workingOnThisOne = null;
                        actOrder = null;
                        t1.stop();
                        
                    }
                }                
                break;
            case oSpin:                    
                break;                 
            default:
                System.out.println("\nTradeActivity: processReq status error?");
                ;
        } /*switch*/
        if ((actTradeActivityFrameform != null) && (workingOnThisOne != null)) {
            actTradeActivityFrameform.setProgressText(
                    workingOnThisOne.getTicker() + 
                    " (" + (workingOnThisOne.getId() + 1) + ") State: " + 
                    workingOnThisOne.getStatusStr()
            );
        }

    }
}
