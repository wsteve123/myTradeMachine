/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import positions.positionData.positionStates;
import ibTradeApi.*;
import ibTradeApi.ibApi.quoteInfo;
import ibTradeApi.ibApi.OrderStatus;
//import tdTradeApi.actTraderApi.quoteInfo;


import ibTradeApi.ibApi.quoteInfo;
/**
 *
 * @author walterstevenson
 */
public class tradeHandler extends Thread {

    positions positionsToHandle = null;
    positionTrader actTrader = null;
    private int runFrequency = 1000; /* 1 second */

    private boolean allowToRun = false;
    
    ibApi actIbApi = ibApi.getActApi();
    public tradeHandler(positions positionsIn) {
        /* these are the positions we work on */
        positionsToHandle = positionsIn;
        this.start();
        //wfs 4/10/11 don't know why but caused it to not run every so often?? remove..
        //maybe attach it to a function to control later.....??
        //allowToRun = false;
        System.out.println("tradeHandler: Not blocking trades for now!!!");
        //ibApi.setBlockTrade(false);

    }
    public void setActTrader(positionTrader posTrader) {
        actTrader = posTrader;
    }
    public void setAllowToRun(Boolean run) {
        allowToRun = run;
        System.out.println("tradeHandler: setAllowToRun: " + allowToRun + " for tradeHandler servicing : "+positionsToHandle.getPositionFileName());
    }
    public void run() {
        int runTimes = 0;
        allowToRun = true;
        while (allowToRun) {

            try {
                runTimes++;

                if ((runTimes % 10) == 0) {
                    System.out.println("tradeHandler running ("+positionsToHandle.getPositionFileName()+") : " + runTimes + " times.");
                }
                runHandler();

                tradeHandler.sleep(runFrequency);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } /* allowToRun */
        System.out.println("tradeHandler: exited while loop, allowToRun == " + allowToRun);

    } /* run */

    public class limitOrders {
        int orderMultiplyer;
        double lastBidAsk;
        int orderTimer;
        int orderTimeout;
        int buyOrSell;
        String tickerSymbol;
        int quantity;
        static final int BUYING = 0;
        static final int SELLING = 1;
        quoteInfo oQuote = new quoteInfo();
        String accountNumber = "no account Number";
        long orderId;
        String placeTradeError = "";
        double prevQuote;
        double orderIncrement;
        double saveOrderIncrement;
        boolean orderPlaced = false;
        boolean forceMarketOrder = false;
        boolean priceIncrementError = false;
        int cancelCount = 0;
        limitOrders(int buyOrSellIn, String tickerin, int quantityin) {
            orderMultiplyer = 0;
            lastBidAsk = 0;
            orderTimeout = 60;
            orderTimer = 0;
            buyOrSell = buyOrSellIn;
            tickerSymbol = tickerin;
            quantity = quantityin;
            orderId = -1;
            accountNumber ="";
            placeTradeError = "";
            prevQuote = 0;
            orderIncrement = 0;
            saveOrderIncrement = 0;
            orderPlaced = false;
            forceMarketOrder = false;
            priceIncrementError = false;
        }
        
        void setAccountNumber(String accIn) {
            accountNumber = accIn;
            
        }
        long getOrderIdNumber() {
            return(orderId);
        }
        String getPlaceTradeError() {
            return(placeTradeError);
        }

       
// wfs test....
     private double determinePriceIncrement(double bid, double ask) {
            /* This routine looks at bid/ask to try and determine if the option
             * related to these quote values trade in penny or 5 cent increments. If
             * any of these values are NOT divisable by 5 then we assume penny increments. Otherwise it
             * assumes 5 cent.
             * For example 1.10 1.15 would be 5 cent (divisable by 5)
             * 1.12 1.15  would be penny because 1.12 is not divisable by 5, so therefore must be penny
             * option. Note: cannot look at last trade because this could be a penny increment that occurred
             * via a multi leg order but still does not trade in penny increments.
             *
             * changed 1/20/2012 IB tells me all options under 3 trade in .01 increments....and no options
             * trade in .10 increments. All above 3, trade in .05. So either .01 or .05.
             * 
             * changed 1/22/20 made in to double instead of float.
             */
            double retValue = 0;
            double bidFloat, askFloat;

            /* do the bid....take the bid multyply by 100 and round it to 2 places. */
            bidFloat = myUtils.Round((bid * 100.0),2);
            /* do mod 5 operation */
            bidFloat %= 5;

            /* do the ask....take the ask multyply by 100 and round it to 2 places. */
            askFloat = myUtils.Round((ask * 100.0),2);
            /* do mod 5 operation */
            askFloat %= 5;


if (false) {
            /* if any of these are NOT Divisable by 5, then return penny increment, else 5 cents */
            if((bidFloat != 0) || (askFloat != 0)) {
                System.out.println("determinePriceIncrement:" + "penny");
                retValue = (float).01;
            }else {
                // wfs 9/10/10 check stock price and adjust correctly < 3 in .05, or >= 3 .10 increments.
                if (bid >= 3.0){
                    System.out.println("determinePriceIncrement:" + "dime");
                    // .05 X 2
                    retValue = (float).10;
                }else {
                    System.out.println("determinePriceIncrement:" + "nickle");
                    retValue = (float).05;
                }
            }
}                 
           
            if (saveOrderIncrement != 0) {
                /*
                 * means we already went through a price error cycle and now
                 * know the correct increment to use
                 */
                retValue = (float) saveOrderIncrement;
            } else {
                /*
                 * see note above
                 */
                if ((priceIncrementError == false) && ((bid < 3.0) && (ask < 3.0))) {
                    System.out.println("determinePriceIncrement:" + "penny");
                    retValue = (float) .01;
                } else if ((priceIncrementError == true) && (myUtils.roundMe(orderIncrement, 2) == .01)) {
                    System.out.println("determinePriceIncrement:" + "nickle");
                    retValue = (float) .05;
                } else if ((priceIncrementError == true) && (myUtils.roundMe(orderIncrement, 2) == .05)) {
                    System.out.println("determinePriceIncrement:" + "dime");
                    retValue = (float) .10;
                } else {
                    retValue = (float) .05;
                }
            }

            return (retValue);
        } /*
         * determinePriceIncrement
         */

    private double calcMidPointBidAsk(int buyOrSell, double bid, double ask) {
            /*
             * changed 1/22/20 made in to double instead of float.
             */
            double midpoint = (float) 0.0;
            double tmpVal1 = (float) 0.0;
            double tmpVal3;

            /* //tmp for testing only...
            ask = oQuote.ask = (float) .31;
            bid = oQuote.bid = (float) .20;
            buyOrSell = SELLING;
             */
            //check if penny increment first....
            if (determinePriceIncrement(bid, ask) == (float) .01) {
                tmpVal1 = myUtils.Round((ask - bid) / 2, 2);
            } else {
                // divisable by 5 so do some match...
                tmpVal1 = (ask - bid) / 2;
                tmpVal1 = myUtils.Round(tmpVal1, 2);
                tmpVal3 = myUtils.Round((tmpVal1 * 100) % 5, 2);
                if (tmpVal3 != 0) {
                    tmpVal3 = (tmpVal1 * 100) % 5;
                    tmpVal3 = (5 - tmpVal3) / 100;
                    tmpVal1 += tmpVal3;
                }

            }
                // depending on buy or sell add or subtract to to bid or ask....
            System.out.println("calcMidPointBidAsk: tmpVal = "+tmpVal1);
            switch (buyOrSell) {
                case BUYING:
                    midpoint = myUtils.Round((bid + tmpVal1), 2);
                    System.out.println("calcMidPointBidAsk: buyOrSel = "+ buyOrSell);
                    break;
                case SELLING:
                    midpoint = myUtils.Round((ask - tmpVal1), 2);
                    System.out.println("calcMidPointBidAsk: buyOrSel = "+ buyOrSell);
                    break;
                default:
                    System.out.println("calcMidPointBidAsk: buyOrSell? = " + buyOrSell);
                    break;
            } /* switch */
            /* priceIncrement means we are dealing with an option that is not
             * penny even though it's under 3 bucks..so account for that here...
             * by using nickle increment.
             */
            
        if (saveOrderIncrement != 0) {
            /*
             * means we already went through a price error cycle and now know
             * the correct increment to use
             */
            midpoint = roundToNearest(midpoint, saveOrderIncrement);
        } else {

            if (midpoint > 3.0 || (priceIncrementError == true)) {

                midpoint = roundToNearest(midpoint, .05);
            }
            if ((priceIncrementError == true) && (myUtils.roundMe(orderIncrement, 2) == .05)) {
                midpoint = roundToNearest(midpoint, .10);
                System.out.println("priceIncrementError && last inc was .05..set set to .10 now.. " + midpoint);
            }
        }

        return midpoint;

        } /* calcMidPointBidAsk */

    private double roundToNearest(double rountThis, double toNearest) {
        double rounded = 0;
        double tmp1;

        tmp1 = rountThis % toNearest;

        if (tmp1 > (toNearest / 2.0)) {
            tmp1 = myUtils.roundMe(tmp1, 2);
            rounded = rountThis - tmp1 + toNearest;
            rounded = myUtils.roundMe(rounded, 2);

        } else {
            tmp1 = myUtils.roundMe(tmp1, 2);
            rounded = rountThis - tmp1;
            rounded = myUtils.roundMe(rounded, 2);
        }
        if (false) {
            System.out.println("roundedResult = " + rounded
                    + " for roundThis = " + rountThis
                    + " to nearest : " + toNearest);
        }

        return (rounded);
    }
     
        
// wfs end test....

        private double calcNextBidAsk(int buyOrSell) {
            double bidAsk = 0;
            /* either way we need a quote. Then either bump up bid or bump down ask depending on 
             * whether we are buying(bid) or selling(ask).
             */
            System.out.println("getting quote of " + tickerSymbol);
//            oQuote = actTraderApi.getQuote("+" + tickerSymbol, true /* option quote */);
            oQuote = actIbApi.getQuote(tickerSymbol, true /* option quote */);
            /* only do this if this is first time through when 
             * prevQuote is null. This insures prevQuote is never null.
             */
            if ((oQuote == null) || ((oQuote != null) && oQuote.value == -1)) {
                System.err.println("calcNextBidAsk: oQuote == null???");
                bidAsk = -1;
                return(bidAsk);
            }else {
            bidAsk = calcMidPointBidAsk(buyOrSell,  oQuote.bid, oQuote.ask);
            }
            if (buyOrSell == BUYING) {
                
                /* if bid price changed on us then we need to start
                 * over. Added cancelCount (2.14.12) to only reset the first
                 * two times.
                 */
                if ((prevQuote != oQuote.bid) && cancelCount < 3 ){
                    orderMultiplyer = 0;
                }
                /* we are trying to buy so pay as little as we can for it!!! 
                 * This means try to pay the bid price first, then if that does not work
                 * add either penny or five cent increment and try again .... up to the ask price(yuk). 
                 */
                /* this value must be rounded to 2 decimal digits, and
                 * add it to the quote bid, because we are buying.
                 */
                orderIncrement = determinePriceIncrement(oQuote.bid, oQuote.ask);
//                bidAsk = myUtils.Round((oQuote.bid + (orderMultiplyer * (float) orderIncrement)), 2);
                bidAsk = (bidAsk + (orderMultiplyer * orderIncrement));
                /* check to make sure we don't go beyond the ask price! */
                if (bidAsk <= oQuote.ask) {
                    /* setup next bid offer. */
                    orderMultiplyer++;
                    System.out.println("calcNextBidAsk: orderMultiplyer is " + orderMultiplyer);
                } else {
                    
                    if (cancelCount < 3) {
                        /*
                         * start over if we reached the ask price
                         */
                        orderMultiplyer = 0;
                        //bidAsk = myUtils.Round((oQuote.bid + (orderMultiplyer * (float) orderIncrement)), 2);
                        System.out.println("start over: bidAsk = " + bidAsk);
                        bidAsk = calcMidPointBidAsk(BUYING, oQuote.bid, oQuote.ask);
                        System.out.println("starting over we reached the ask price. setting multiplyer to zero.");
                        orderMultiplyer++;
                    }else{
                        /* ok, so we've canceled too many times, so need to go market....*/
                        forceMarketOrder = true;
                        System.out.println("Canceled too many times, forcing market order. " + cancelCount);
                    }
                        
                }
                /* update prevQuote with bid price for next time through */
                prevQuote = oQuote.bid;
            }else if (buyOrSell == SELLING) {
                /* if ask price changed on us then we need to start
                 * over.Added cancelCount (2.14.12) to only reset the first
                 * two times.
                 */
                if ((prevQuote != oQuote.ask) && cancelCount < 3 ){
                    orderMultiplyer = 0;
                }
                /* we are trying to sell so get as much as we can for it!!! 
                 * This means try to get the asking price first, then if that does not work
                 * subtract 5 cents from that and try that .... down to the bid price, the 
                 * worst price but it should execute there cuz someone is biding that price.
                 */
                orderIncrement = determinePriceIncrement(oQuote.bid, oQuote.ask);
//                bidAsk = myUtils.Round((oQuote.ask - (orderMultiplyer * (float) orderIncrement)), 2);
                bidAsk = (bidAsk - (orderMultiplyer * orderIncrement));
                /* check to make sure we don't go beyond the ask price! */
                if (bidAsk >= oQuote.bid) {
                    /* setup next ask offer. */
                    orderMultiplyer++;
                    System.out.println("calcNextBidAsk: orderMultiplyer is " + orderMultiplyer);
                } else {
                    if (cancelCount < 3) {
                        /*
                         * start over if we reached the bid price
                         */
                        orderMultiplyer = 0;
                        //bidAsk = myUtils.Round((oQuote.ask - (orderMultiplyer * (float) orderIncrement)), 2);
                        System.out.println("start over: bidAsk = " + bidAsk);
                        bidAsk = calcMidPointBidAsk(SELLING, oQuote.bid, oQuote.ask);
                        System.out.println("starting over we reached the bid price. setting multiplyer to zero.");
                        orderMultiplyer++;
                    } else {
                        /* ok, so we've canceled too many times, so need to go market....*/
                        forceMarketOrder = true;
                        System.out.println("Canceled too many times, forcing market order. " + cancelCount);
                    }
                }
                /* update prevQuote with ask price for next time through */
                prevQuote = oQuote.ask;
            }else {
                System.out.println("calcNextBidAsk: ERROR!!!!!! buyOrSell not correct!!!");
                bidAsk = 0;
            }
            System.out.println("bidValue is :" + oQuote.bid);
            System.out.println("askValue is :" + oQuote.ask);
            System.out.println("adjBidAskValue is :" + bidAsk);
            return(bidAsk);
            
        } /* calcNextBidAsk */
        boolean cancelExistingOrder() {
            /* assume all good */
            boolean retVal = true;
            OrderStatus stat = new OrderStatus();

            /* if orderId == -1, there was no previous order to cancel. So don't do it.
             changed to -1 on 1/24/2012....orderId of 0 is now ok....
             */
            if ((orderId != -1) && (priceIncrementError == false)){
                /* time to cancel the order that we previously placed. */
                System.out.println("canceling order: " + orderId);

                //ibApi.orderStatusInfo.cancelOrder(accountNumber, Long.toString(orderId));
                //System.out.println("result = " + actTraderApi.orderStatusInfo.getOrderStatus(accountNumber, Long.toString(orderId)));
                //System.out.println("status = " + actTraderApi.orderStatusInfo.getStatusId());
                actIbApi.testOrder.cancelOrder((int)orderId);
                stat = actIbApi.testOrder.getOrderStatus((int)orderId);
                System.out.println("statusRdy = " + stat.statusRdy);
                System.out.println("status = " + stat.status );
                if (stat.status.equals("Cancelled")) {
                    System.out.println("Canceled successfull!!!");
                    orderId = -1;
                    cancelCount++;
                } else {
                    retVal = false;
                }
            }
            
            return(retVal);
        } /* cancelExistingOrder */
        
        boolean checkLimitOrder() {

            /* assume goodness */
            boolean retValue = true;
            String tradeResult = "";
            OrderStatus stat = new OrderStatus();
            
            /* this just tells us if we placed an order this time around. */
            orderPlaced = false;
            
            /* orderTimer expiring to zero only means one thing:
             * we waited our maximum time for an order to fill and it did not fill.
             * so we need to bump the order limit and try again, but first cancel the pending 
             * order.
             */
            if (orderTimer == 0) {
                if (cancelExistingOrder() == true) {
                    /* we either canceled ok or there
                     * was no existing order to cancel.
                     * either way we are ok.
                     * so reset our maximum timeout
                     * and try next trade.
                     */
                    //orderTimer = (orderTimeout + (orderMultiplyer * orderTimeout));
                    
                    lastBidAsk = calcNextBidAsk(buyOrSell);
                    if (lastBidAsk == -1) {
                        System.err.println("checkLimitOrder: -1 value return by calcNextBitAsk??????");
                        retValue = false;
                        return(retValue);
                    
                    }
                    if (buyOrSell == BUYING) {
                        //actTraderApi.equityTradeInfo.setTradeInfo(accountNumber, "+" + tickerSymbol, actTraderApi.equityTradeInfo.orderAction.BUY_TO_OPEN, actTraderApi.equityTradeInfo.orderType.LIMIT,
                               // actTraderApi.equityTradeInfo.timeInForce.DAY, lastBidAsk /* limit value */, quantity, true /* is an option*/);
                        if(forceMarketOrder == false) {
                            orderId = actIbApi.testOrder.setOrderInfo(accountNumber, tickerSymbol, true, "BUY", "LMT", "DAY", lastBidAsk, quantity);
                        }else {
                            orderId = actIbApi.testOrder.setOrderInfo(accountNumber, tickerSymbol, true, "BUY", "MKT", "DAY",0 /* price - mkt*/, quantity);    
                        }
                    }else if (buyOrSell == SELLING) {
                        /* added 12/12/10 check if merket sell order needed ... */
                        if (forceMarketOrder == false) {
                            //actTraderApi.equityTradeInfo.setTradeInfo(accountNumber, "+" + tickerSymbol, actTraderApi.equityTradeInfo.orderAction.SELL_TO_CLOSE/*SELL_TO_CLOSE*/, actTraderApi.equityTradeInfo.orderType.LIMIT,
                              //      actTraderApi.equityTradeInfo.timeInForce.DAY, lastBidAsk /* limit value */, quantity , true /* is an option*/);
                            orderId = actIbApi.testOrder.setOrderInfo(accountNumber, tickerSymbol, true, "SELL", "LMT", "DAY", lastBidAsk, quantity);
                        }else {
                            //force market order on this sale!!
                            System.out.println("checkLimitOrder: forceMarketOrder TRUE, do market order on option sale!!!!!!");
                            //actTraderApi.equityTradeInfo.setTradeInfo(accountNumber, "+" + tickerSymbol, actTraderApi.equityTradeInfo.orderAction.SELL_TO_CLOSE/*SELL_TO_CLOSE*/, actTraderApi.equityTradeInfo.orderType.MARKET,
                              //      actTraderApi.equityTradeInfo.timeInForce.DAY, 0 /* no limit value */, quantity , true /* is an option*/);
                            orderId = actIbApi.testOrder.setOrderInfo(accountNumber, tickerSymbol, true, "SELL", "MKT", "DAY",0 /* price - mkt*/, quantity);
                        }
                        
                    }
                    //actTraderApi.equityTradeInfo.placeTrade();
                    actIbApi.testOrder.placeOrder((int)orderId);

                    orderPlaced = true;
                    /* added more delay here....on 5/14/12.... the order error sometimes takes longer to get back to us... */
                    myUtils.delay(3000);
                    stat = actIbApi.testOrder.getOrderStatus((int)orderId);
                    //tradeResult = placeTradeError = actTraderApi.equityTradeInfo.getResult();
                    tradeResult = placeTradeError = stat.errorMsg;
                    if (tradeResult.equals("") == true) {
                        /* moved this here to set timer if order was successfull */
                        orderTimer = (orderTimeout);
                        //orderId = actTraderApi.equityTradeInfo.getOrderId();
                        retValue = true;
                        System.out.println("checkLimitOrder: limit order placed. orderId: "+ orderId);
                        if (priceIncrementError == true) {
                            // remember correct orderIncrement for next time!
                            saveOrderIncrement = orderIncrement;
                        }
                        priceIncrementError = false;
                    } else {
                        /*
                         * added this next check to handle the case where the increment we used was wrong
                         * IB will tell us via message. Don't error out here instead record this and 
                         * the fillOrders section will try the correct increment to try and recover. 
                         */
                        if ((priceIncrementError = stat.errorMsg.contains("invalid price") == true) ||
                           (priceIncrementError = stat.errorMsg.contains("minimum price") == true))     {
                            System.out.println("checkLimitOrder: price Increment wrong" + lastBidAsk);
                            retValue = true;
                        } else {
                            System.out.println("tradeError:" + tradeResult);
                            retValue = false;
                        }

                    }
                    
                }else {
                    /* did not cancel yet. We do not reset the
                     * orderTimer so we should just try again next time.
                     */
                    System.out.println("Cancel in progress!!!");
                }                

            }else {
                orderTimer--;
                /* ugly, but...
                 * just before we are about to cancel the order (ie. orderTimer == 1), get the remaining contracts from queue remaining so
                 * we update our quantity (number of contracts remaining in this order) in case some contracts did execute.
                 *  8/13/10
                 * ok, so the situation where we partially filled an order happened. We Need to update
                 * the quantity of the limit order incase some filled. RemainingQuantity has this value
                 * so just update the limit order quantity with it and we should be good.
                 i.e. we started with 3 now only have to 1 - place new order for 2.
                        
                 */
                if (orderTimer == 1) {
                    stat = actIbApi.testOrder.getOrderStatus((int)orderId);
                    //quantity = actTraderApi.orderStatusInfo.getRemainingQuantity();
                    quantity = stat.remaining;
                    System.out.println("about to cancel order, store current quantity: "+quantity);
                }
            }
            return(retValue);

        } /* checkLimitOrder */


        void setLimitQuantity(int qin){
            quantity = qin;
        }
      
    } /* limitOrders */

    boolean positionNeedsTrade(positionData onThisPos) {

        if (((onThisPos.prevState == positionStates.ACTIVE) &&
                ((onThisPos.currentState == positionStates.ADJUST) || ((onThisPos.currentState == positionStates.CLOSED) && (onThisPos.closed == false)))) || (onThisPos.currentState == positionStates.FILL)) {
            return (true);
        }
        return (false);

    }
    
    
    static class positionTradePriority {
        /* 
         * This class will determine the following from the position:
         * 1) is there an optionTrade in pending state
         * 2) is there an optionTrade waiting to be placed
         * 3) is there an stock trade waiting to be placed
         * This is used to conditinally handle placing trades on a given position.
         * a. We do nothing if a positin has a pending (already placed) option trade that is
         * waiting to be filled. Priority # 1 wait til option pending is filled.
         * b. if a is not true, then we place the trade for the option that is waiting next. Priority #2.
         * c. if a and b is false, then we place the stock trade. Lastly place the stock trade if a or b not true. Priority #3.
         */
        private boolean optionPending;
        private boolean optionTradeNeeded;
        private boolean stockTradeNeeded;
        private positionData actPos = null;
        positionTradePriority() {
            optionPending = false;
            optionTradeNeeded = false;
            stockTradeNeeded = false;
        }
        void getTradePriority(positionData posIn) {
            actPos = posIn;
            optionPending = false;
            optionTradeNeeded = false;
            stockTradeNeeded = false;
            scanTradeAction();
        }
        private void scanTradeAction() {
            
            positionAdjustment actAdj;
            int numOfAdjustments = actPos.posAdjustments();
            int ix = 0;
            int optionId = 0;
            int stockId = 0;
            
            while (numOfAdjustments > 0) {
                actAdj = actPos.posAdjGet(ix++);
                numOfAdjustments--;

                /* see if adjustment has an option buy or sell pending # 1 priority */
                if (((actAdj.outcome.isOutcomeBuyShort() == true) || (actAdj.outcome.isOutcomeSellShort())) && (actAdj.getTradeNeeded() == true) && (actAdj.getTradePending() == true)) {
                    /* found an adjustment related to an option and it is in pending state!!! */
                    optionPending = true;
                    optionId = ix;
                } /* if */

                /* see if adjustment has an option buy or sell needed # 2 priority */
                if (((actAdj.outcome.isOutcomeBuyShort() == true) || (actAdj.outcome.isOutcomeSellShort())) && (actAdj.getTradeNeeded() == true) && (actAdj.getTradePending() == false)) {
                    /* found an adjustment related to an option and it needs to be traded!!! */
                    optionTradeNeeded = true;
                    optionId = ix;
                } /* if */

                /* see if adjustment has an option buy or sell needed # 3 priority */
                if (((actAdj.outcome.isOutcomeBuyLong() == true) || (actAdj.outcome.isOutcomeSellLong())) && (actAdj.getTradeNeeded() == true) && (actAdj.getTradePending() == false)) {
                    /* found an adjustment related to an stock and it is waiting to be traded!! */
                    stockTradeNeeded = true;
                    stockId = ix;
                } /* if */

            } /* while */
            
            /* if we have an option either pending OR in 
             * need of trade AND a stock trade is needed, then
             * we look at position of the adjustment to determine if 
             * the option should have priority over the stock.
             * if option is positioned before the stock, 
             * then priority is ON. If stock is before option
             * priority is OFF. So when you want to give priority to the 
             * option, it must be put in first before the stock trade.
             */
            if (((optionPending == true) || (optionTradeNeeded == true)) && (stockTradeNeeded == true)) {
                if (optionId > stockId) {
                    optionPending = false;
                    optionTradeNeeded = false;
                }
            }
        } /* scanTradeAction */

        boolean isOptionPending() {
            return(optionPending);
        }
        boolean isOptionTradeNeeded() {
            return(optionTradeNeeded);
        }
        boolean isStockTradeNeeded() {
            return(stockTradeNeeded);
        }
        boolean blockLongTrade() {
            return(optionTradeNeeded || optionPending);
        }
    } /* positionTradePriority */


    void postAllTrades(positionData posin, positionTradePriority posPri) {
        /* 
         * this position is in a need trade state, so
         * search through all adjustments
         * for this position and place trades.
         * If position is in loopback, it does not do the
         * trade, just displays a message.
         */
        positionAdjustment actAdj;
        int numOfAdjustments = posin.posAdjustments();
        int ix = 0;
        String tradeResult = "";
        boolean tradeError;
        float limitAmount = (float)0.0;
        boolean blockLongTrade;
        boolean orderPlaced;
        int tmpTradeId;
        OrderStatus stat = new OrderStatus();
        /* ugly but hey */
        String accountNumber = positionsToHandle.getAccountNumber();
        
        /* check if this position has an option either pending or needed for trade. 
         * If so, block the long stock from being placed for trade.
         */
        blockLongTrade = (posPri.isOptionPending() || (posPri.isOptionTradeNeeded()));
        
        while (numOfAdjustments > 0) {
            actAdj = posin.posAdjGet(ix++);
            numOfAdjustments--;
            tradeError = false;
            tradeResult = "";
            orderPlaced = false;
            
            /* check that trade is needed and is NOT pending (already done) */
            if ((actAdj.getTradeNeeded() == true) && actAdj.getTradePending() == false) {

                /* make sure there is something to trade, check for non zero shares or contracts. */
                if (actAdj.outcome.getAdjustment() > 0) {
                    /* something there, do the trade. */
                    /* if long operation make sure an option is not pending or needed. We need to do the option first! */
                    if (actAdj.outcome.isOutcomeSellLong() && !blockLongTrade) {
                        /* sell long shares */
                        if (posin.getTradeLoopback() == true) {
                            /* position is in loopback, so don't do the actual trade. */
                            System.out.println("postAllTrades: LPBK sellLongShares:" + actAdj.outcome.getAdjustment());
                        } else {
                            System.out.println("postAllTrades: sellLong "+posin.longTicker+" shares: "+actAdj.outcome.getAdjustment());
                            
                            //actTraderApi.equityTradeInfo.setTradeInfo(positionsToHandle.getAccountNumber(), posin.longTicker, actTraderApi.equityTradeInfo.orderAction.SELL, actTraderApi.equityTradeInfo.orderType.MARKET,
                                   // actTraderApi.equityTradeInfo.timeInForce.DAY, 0 /* market - no price */, actAdj.outcome.getAdjustment() /* quantity */, false /*not an option*/);
                            //actTraderApi.equityTradeInfo.placeTrade();
                            tmpTradeId = actIbApi.testOrder.setOrderInfo(accountNumber, posin.longTicker, false /* no option */ , "SELL", "MKT", "DAY",0/* price */, actAdj.outcome.getAdjustment());
                            actIbApi.testOrder.placeOrder(tmpTradeId);
                            /* flag order Placed for a bit later. */
                            orderPlaced = true;
                            stat = actIbApi.testOrder.getOrderStatus(tmpTradeId);
                            //tradeResult = actTraderApi.equityTradeInfo.getResult();
                            tradeResult = stat.errorMsg;
                            System.out.println("result:" + tradeResult);
                            actAdj.setTradeIdNumber(tmpTradeId);
                            
                        }
                        
                    } else if (actAdj.outcome.isOutcomeSellShort()) {
                        /* sell option */
                        if (posin.getTradeLoopback() == true) {
                            /* position is in loopback, so don't do the actual trade. */
                            System.out.println("postAllTrades: LPBK sellShortShares:" + actAdj.outcome.getAdjustment());
                        } else {
                            System.out.println("postAllTrades: sellShort "+posin.shortTicker+" contracts: "+actAdj.outcome.getAdjustment());
                            /* check if we already created a limit order */
                            if (actAdj.actLimitOrder == null) {
                                /* if not then create one and store it in adjustment */
                                actAdj.actLimitOrder = new limitOrders(
                                        /* we are selling */
                                        limitOrders.SELLING,
                                        /* this ticker */
                                        posin.shortTicker,
                                        /* this quantity */
                                        actAdj.outcome.getAdjustment());
                                actAdj.actLimitOrder.setAccountNumber(positionsToHandle.getAccountNumber());
                                /* added 12/12/10
                                 * may need to use market order for option in case we just need to force a sale of 
                                 * option position, no matter what. See if this is such a situation.
                                 */
                                actAdj.actLimitOrder.forceMarketOrder = actAdj.isItOptionMarketOrder();
                                
                                if (actAdj.actLimitOrder.checkLimitOrder() == false) {
                                    tradeResult = actAdj.actLimitOrder.placeTradeError;
                                    System.out.println("place Order Error:" + tradeResult);
                                }else {
                                    System.out.println("postAllTrades: checkLimitOrder good");
                                }
                                orderPlaced = actAdj.actLimitOrder.orderPlaced;
                                if (orderPlaced == true) {
                                    actAdj.setTradeIdNumber(actAdj.actLimitOrder.getOrderIdNumber());
                                }else{
                                    //do nothing.
                                }
                                
                            }else {
                                System.out.println("postAllTrades: actLimitOrder not null when trying to create one?");
                            }
                                                                                                          
                        }
                        
                    } else if (actAdj.outcome.isOutcomeBuyLong() && !blockLongTrade) {
                        /* buy shares */
                        if (posin.getTradeLoopback() == true) {
                            /* position is in loopback, so don't do the actual trade. */
                            System.out.println("postAllTrades: LPBK buyLongShares:" + actAdj.outcome.getAdjustment());
                        } else {
                            System.out.println("postAllTrades: buyLong "+posin.longTicker+" shares: "+actAdj.outcome.getAdjustment());
                            //actTraderApi.equityTradeInfo.setTradeInfo(positionsToHandle.getAccountNumber(), posin.longTicker, actTraderApi.equityTradeInfo.orderAction.BUY, actTraderApi.equityTradeInfo.orderType.MARKET,
                                    //actTraderApi.equityTradeInfo.timeInForce.DAY, 0 /* market - no price */, actAdj.outcome.getAdjustment() /* quantity */, false /*not an option*/);
                            //actTraderApi.equityTradeInfo.placeTrade();
                            tmpTradeId = actIbApi.testOrder.setOrderInfo(accountNumber, posin.longTicker, false /* option */,
                                                                         "BUY", "MKT", "DAY",
                                                                         0 /* price */,
                                                                         actAdj.outcome.getAdjustment());
                            actIbApi.testOrder.placeOrder(tmpTradeId);

                            orderPlaced = true;
                            stat = actIbApi.testOrder.getOrderStatus(tmpTradeId);
                            tradeResult = stat.errorMsg;
                            System.out.println("result:" + tradeResult);

                            actAdj.setTradeIdNumber(tmpTradeId);
                            /* arm the long long to trigger the fill for testing... */
                        }
                        
                    } else if (actAdj.outcome.isOutcomeBuyShort()) {
                        /* buy options */
                        if (posin.getTradeLoopback() == true) {
                            /* position is in loopback, so don't do the actual trade. */
                            System.out.println("postAllTrades: LPBK buyShortShares:" + actAdj.outcome.getAdjustment());
                        } else {
                            System.out.println("postAllTrades: buyShort "+posin.shortTicker+" shares: "+actAdj.outcome.getAdjustment());
                            /* check if we already created a limit order */
                            if (actAdj.actLimitOrder == null) {
                                /* if not then create one and store it in adjustment */
                                actAdj.actLimitOrder = new limitOrders(
                                        /* we are buying */
                                        limitOrders.BUYING,
                                        /* this ticker */
                                        posin.shortTicker,
                                        /* this quantity */
                                        actAdj.outcome.getAdjustment());
                                actAdj.actLimitOrder.setAccountNumber(positionsToHandle.getAccountNumber());
                                
                                if (actAdj.actLimitOrder.checkLimitOrder() == false) {
                                    tradeResult = actAdj.actLimitOrder.placeTradeError;
                                    System.out.println("place Order Error:" + tradeResult);
                                }else {
                                    System.out.println("postAllTrades: checkLimitOrder good");    
                                }
                                
                                orderPlaced = actAdj.actLimitOrder.orderPlaced;
                                if (orderPlaced == true) {
                                    actAdj.setTradeIdNumber(actAdj.actLimitOrder.getOrderIdNumber());
                                }else{
                                    //do nothing.
                                }
                                
                            }else {
                                System.out.println("postAllTrades: actLimitOrder not null when trying to create one?");
                            }
                        }
                        
                    }
                    
                    
                    /* orderPlaced is only used when we are really trading, not while in loopback.
                     * So if true then treat as real trade, if false, check if loopback is set and just 
                     * set trade pending true so that we don't loop here for ever.
                     */
                    if (orderPlaced == true) {
                        /*
                         * if trade result is not empty AND not a price increment error, then error out.
                         * priceIncrementError just means we tried a wrong price and we should try again by
                         * correcting the bidask...this will happen in fillOrders, so don't error out here.
                         */
                        if (!tradeResult.equals("") && !actAdj.actLimitOrder.priceIncrementError) {
                            System.out.println("result: error-> " + tradeResult);
                            tradeError = true;
                            /*error occured so clear tradeNeeded */
                            actAdj.setTradeNeeded(false);
                            System.out.println("postAllTrades: setting trade error TRUE!");
                            posin.setTradeError(true);
                        } else {
                            /* set this adjustment as tradePending so we don't put in order again. */
                            actAdj.setTradePending(true);
                        }
                    }else if (posin.getTradeLoopback() == true) {
                        /* set this adjustment as tradePending so we don't put in order again. */
                        actAdj.setTradePending(true);  
                        
                    }
                   

                } else {
                    /* this can happen when we met the stock price change critera (> 3%) 
                     * but could not buy/sell even one contract to make us delta neutral. The
                     * amount we are off is not enough, and it does not make sense to put a trade on. If we did 
                     * do 1 contract it would swing us to the opposite bias. So just forget this trade but log it as 
                     * nothing traded.
                     */
                    actAdj.setTradeNeeded(false);
                    System.out.println("postAllTrades: no adjustment detected so no trade done?");
                    /* put to previous state */
                    posin.currentState = posin.prevState;  
                } /* else */
            } /* if */
        } /* while */


    } /* postAllTrades */


    
    private void fillAllTrades(positionData posin) {
        positionAdjustment actAdj;
        int numOfAdjustments = posin.posAdjustments();
        int ix = 0;
        boolean fillOcurred = false;
        int fillsNeeded = 0;
        float prevBalance = 0;
        OrderStatus stat = new OrderStatus();

        while (numOfAdjustments > 0) {


            actAdj = posin.posAdjGet(ix);
            numOfAdjustments--;
            /* check that trade is needed and is pending (already done) */
            if ((actAdj.getTradeNeeded() == true) && actAdj.getTradePending() == true) {
                /* this guy tells us how many fills we need to fill before we 
                 * go to our previous state! Only when it is zero meaning no more fills left 
                 * do we change states.
                */
                fillsNeeded++;
                /* must get the previous adjustment balance to work with */
                if (ix > 0) {
                    /* get prev balance from prev adjustment */
                    prevBalance = posin.posAdjGet(ix - 1).balance;
                } else {
                    /* no previous adjustment so must use pos balance. */
                    prevBalance = posin.posBalance;
                }

                if (posin.getTradeLoopback() == true) {


                    quoteInfo sQuote = new quoteInfo();
                    quoteInfo oQuote = new quoteInfo();
                    /* we are looped back so just get quote no trading has been done, so no point checking
                     * for OrderStatus.
                     */
                    sQuote = actIbApi.getQuote(posin.longTicker, false);
//                    oQuote = actTraderApi.getQuote("+" + posin.shortTicker, true /* option quote */);
                    oQuote = actIbApi.getQuote(posin.shortTicker, true /* option quote */);

                    if ((sQuote != null) && (oQuote != null)) {


                        if (actAdj.outcome.isOutcomeBuyLong() == true) {

//                            actAdj.balance = prevBalance;
                            actAdj.longPrice = (float)sQuote.value;
//                            actAdj.balance -= (actAdj.outcome.adjustment * actAdj.longPrice);
                            
                            /* we are buying so subtract cost from our running balance */
                            actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * actAdj.longPrice));

                        } else if (actAdj.outcome.isOutcomeBuyShort() == true) {

//                            actAdj.balance = prevBalance;
                            actAdj.shortPrice = (float)oQuote.value;
//                            actAdj.balance -= (actAdj.outcome.adjustment * actAdj.shortPrice * 100);
                            
                            /* we are buying so subtract cost from our running balance */
                            actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * actAdj.shortPrice * 100));

                        } else if (actAdj.outcome.isOutcomeSellLong() == true) {

//                            actAdj.balance = prevBalance;
                            actAdj.longPrice = (float)sQuote.value;
//                            actAdj.balance += (actAdj.outcome.adjustment * actAdj.longPrice);
                            
                            /* we are selling so add sale to our running balance */
                            actAdj.balance = posin.addRunningBalance((actAdj.outcome.adjustment * actAdj.longPrice));
                            

                        } else if (actAdj.outcome.isOutcomeSellShort() == true) {

//                            actAdj.balance = prevBalance;
                            actAdj.shortPrice = (float)oQuote.value;
//                            actAdj.balance += (actAdj.outcome.adjustment * actAdj.shortPrice * 100);
                            
                            /* we are selling so add sale to our running balance */
                            actAdj.balance = posin.addRunningBalance((actAdj.outcome.adjustment * actAdj.shortPrice * 100));

                        }
                        actAdj.profitLoss = (((actAdj.sharesLong * actAdj.longPrice) +
                                (actAdj.sharesShort * actAdj.shortPrice * 100) +
                                (actAdj.balance)) - (posin.posBalance));

                        fillOcurred = true;
                        actAdj.setTradePending(false);
                        actAdj.setTradeNeeded(false);
                        /* done with this adjustment so put it back. */
                        posin.posAdjReplace(actAdj, ix);

                    } else {
                        System.out.println("fillAllTrades: in LPBK sQuote or oQuote null!");
                    }

                } else {

                    /* NOT LOOPED BACK, so go find out status of trade */

                    stat = actIbApi.testOrder.getOrderStatus((int)actAdj.getTradeIdNumber());
                    
                    System.out.println("result = " + stat.status);
                    System.out.println("error = " + stat.errorMsg);
                    System.out.println("symbol = " + stat.symbol);                    
                    System.out.println("remainQ = " + stat.remaining);
                    System.out.println("quantity = " + stat.quantity);
                   
                    if (stat.status.equals("Filled")) {
                        System.out.println("order FILLED!");
                        System.out.println("filledQuantity = " + stat.filled);
                        if (actAdj.outcome.isOutcomeBuyLong() == true) {

                            // actAdj.sharesLong += actTraderApi.orderStatusInfo.getFilledTotalQuantity();
                            /* added 3/29/12, don't update this if we just did an implied vol average in operation*/
                            if(actAdj.ivAveInFlag == false) {
                                actAdj.longPrice = (float) stat.aveFillPrice;
                            }
//                            actAdj.balance -= (actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.longPrice);
                            
                            /* we are buying so subtract cost from our running balance */
                            /* wfs 10/26/10 totalQ filled was incorrect ? it returned 4 when it should have returned 3? not sure why but
                             * changed to using adjustment.
                             */
//                            actAdj.balance = posin.subtractRunningBalance(actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.longPrice);
                            if(actAdj.ivAveInFlag == false) {
                                actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * actAdj.longPrice));   
                            }else {
                                actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * (float) stat.aveFillPrice));
                            }

                        } else if (actAdj.outcome.isOutcomeBuyShort() == true) {

                            // actAdj.sharesShort += actTraderApi.orderStatusInfo.getFilledTotalQuantity();
                            /* added 3/29/12, don't update this if we just did an implied vol average in operation*/
                            if(actAdj.ivAveInFlag == false) {
                                actAdj.shortPrice = (float) stat.aveFillPrice;
                            }
//                            actAdj.balance -= (actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.shortPrice * 100);
                            /* we are buying so subtract cost from our running balance */
                            /* wfs 10/26/10 totalQ filled was incorrect ? it returned 4 when it should have returned 3? not sure why but
                             * changed to using adjustment.
                             */
//                            actAdj.balance = posin.subtractRunningBalance(actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.shortPrice * 100);
                            if(actAdj.ivAveInFlag == false) {
                                actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * actAdj.shortPrice * 100));
                            }else {
                                actAdj.balance = posin.subtractRunningBalance((actAdj.outcome.adjustment * (float) stat.aveFillPrice * 100));
                            }

                        } else if (actAdj.outcome.isOutcomeSellLong() == true) {

                            // actAdj.sharesLong -= actTraderApi.orderStatusInfo.getFilledTotalQuantity();
                            actAdj.longPrice = (float) stat.aveFillPrice;
//                            actAdj.balance += (actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.longPrice);
                            /* we are selling so add sale to our running balance */
                            /* wfs 10/26/10 totalQ filled was incorrect ? it returned 4 when it should have returned 3? not sure why but
                             * changed to using adjustment.
                             */
//                            actAdj.balance = posin.addRunningBalance(actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.longPrice);
                            actAdj.balance = posin.addRunningBalance((actAdj.outcome.adjustment * actAdj.longPrice));

                        } else if (actAdj.outcome.isOutcomeSellShort() == true) {

                            // actAdj.sharesShort -= actTraderApi.orderStatusInfo.getFilledTotalQuantity();
                            actAdj.shortPrice = (float) stat.aveFillPrice;
//                            actAdj.balance += (actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.shortPrice * 100);
                            /* we are selling so add sale to our running balance */
                            /* wfs 10/26/10 totalQ filled was incorrect ? it returned 4 when it should have returned 3? not sure why but
                             * changed to using adjustment.
                             */
//                            actAdj.balance = posin.addRunningBalance(actTraderApi.orderStatusInfo.getFilledTotalQuantity() * actAdj.shortPrice * 100);
                            actAdj.balance = posin.addRunningBalance((actAdj.outcome.adjustment * actAdj.shortPrice * 100));

                        }

                        actAdj.profitLoss = (((actAdj.sharesLong * actAdj.longPrice) +
                                (actAdj.sharesShort * actAdj.shortPrice * 100) +
                                (actAdj.balance)) - (posin.posBalance));
                        fillOcurred = true;
                        actAdj.setTradePending(false);
                        actAdj.setTradeNeeded(false);
                        /* done with this adjustment so put it back. */
                        posin.posAdjReplace(actAdj, ix);
                        /* order filled so if this was a limit order nullify the 
                         * now filled actLimitOrder for re-use later.
                         */
                        if (actAdj.actLimitOrder != null) {
                            actAdj.actLimitOrder = null;
                        }



                    } else if (actAdj.actLimitOrder != null) {
                        /*
                         * so order did not fill and there is a limit order pending.
                         * we are waiting for the order to fill at our limit price. So
                         * go check if a new limit order needs to be placed.
                         */
                        
                        actAdj.actLimitOrder.checkLimitOrder();
                        /* check if new order was placed. If so update orderId. */
                        if (actAdj.actLimitOrder.orderPlaced == true) {
                            actAdj.setTradeIdNumber(actAdj.actLimitOrder.getOrderIdNumber());    
                        }
                    } /* limit order */
                }
                if((fillOcurred == true) && (fillsNeeded > 0)){
                    /* cound down a fill that was needed because
                     * one was filled.
                     */
                    fillsNeeded--;
                    /* reset the fillOccured flag */
                    fillOcurred = false;
                }
                if (posin.currentState == positionStates.FILL) {
                    /* if we are filling this position for the first time,
                     * we must adjust the following inital position data now that we have
                     * real information.
                     */
                    if (actAdj.outcome.isOutcomeBuyLong() == true) {
                        posin.longEntryPrice = actAdj.longPrice;
                    } else if (actAdj.outcome.isOutcomeBuyShort() == true) {
                        posin.shortEntryPrice = actAdj.shortPrice;
                    }

                }

            }/* trade needed and pending */ 
            else if ((actAdj.getTradeNeeded() == true) && (actAdj.getTradePending() == false)) {
                /* this guy tells us how many fills we need to fill before we 
                 * go to our previous state! Only when it is zero meaning no more fills left 
                 * do we change states.
                 * Have to count trade needed but not yet pending case because in a fill case the
                 * option will pend first by itself, then when that fills, the stock will go pending. We therefore 
                 * cannot change back to previous state until both are filled. This else if counts the one needed for trade but is 
                 * not yet pending.
                */
                fillsNeeded++;
                
            }
            /* bump adj index now */
            ix++;
        } /* while */

        /* if all fills were filled on this
         * position, then change it's state back to previous
         * because it is finished and it needs to go back to it's 
         * prior state of being. If a fill was not completed then we stay in fill 
         * state and loop through here again until it is filled. When all trades pending
         * in one position are filled then we think about changing states on the position.
         */
        if (fillsNeeded == 0) {
            System.out.println("fillAllTrades: change in position occurred on:");
            System.out.println("ticker: " + posin.longTicker);
            System.out.println("pState: " + posin.prevState.toString());
            System.out.println("cState: " + posin.currentState.toString());
            if ((posin.currentState == positionStates.CLOSED) && (posin.isClosed() == false)) {
                /* when we close a position, we need to store the positions "risk" at the time
                 * we are closing the position in order to calculate % profit/loss later. The amount of
                 * risk or how much money is working to make the profit/loss is the posBal - adjBal which is the 
                 * amount of money in the market at that time. This value is in the adjustment just before the 
                 * closing adjustments.
                 */
                if (posin.posAdjustments() >= 4) {
                    posin.posClosedRisk = (posin.posBalance - posin.posAdjGet(posin.posAdjustments() - 3).balance);
                } else {
                    System.out.println("fillAllTrades: error here in CLOSING a position, not enough adjustments?");
                }

                posin.setClosed(true);
            } else if (posin.currentState == positionStates.FILL) {
                posin.currentState = positionStates.ACTIVE;
                posin.prevState = positionStates.FILL;
            } else {
                posin.currentState = posin.prevState;
            }
            if (actTrader != null) {
                System.out.println("fillAllTrades: setting updateFile for active trader.");
                actTrader.setUpdateFile(true);
            } else {
                System.out.println("fillAllTrades: tried to set file update for actTrader but it was NULL! Update did not occur!");
            }

        } /* fillsNeeded == 0 */

    } /* fillAllTrades */


     void runHandler() {
        positionData actPos = null;
        positionAdjustment actAdj = null;

        positionTradePriority posPriority = new positionTradePriority();
        
        if (positionsToHandle != null) {
            positionsToHandle.semTake();
            /* cycle through all positions and look for states to act on for trades */
            for (actPos = positionsToHandle.posDataFetchNext(true /* first fetch */); actPos != null; actPos = positionsToHandle.posDataFetchNext(false)) {

                /* check and post trades if needed on this position */
                if (positionNeedsTrade(actPos) == true) {
                    /* look through the entire position (all adjustments) for types of trades. */
                    posPriority.getTradePriority(actPos);
                     /* we will not do any new trades if 
                      * there is an option pending. Most important
                      * when we are doing pairs (FILL/CLOSE) we do not
                      * want to place a stock trade until the corresponding option has
                      * filled first. We do limit orders on options only for now and these could
                      * take a while to fill. We do not want to execute the stock order until the option
                      * actualy filled. So we do nothing with new trades if there is an option pending fill.
                      * Note that when FILL or CLOSE states are entered the first adjustment made is the option then follows
                      * the stock. This sets the order of the trade - option first, stock next. And since we will not place another trade as 
                      * long as the option is not filled, we are giving the option priority over the stock. We do
                      * not fill stock order until option order is filled first - this is true for FILL or CLOSE trades
                      * (pair trades). The side effect of this now is that the option trade will occur first on normal single adjustment
                      * orders too. This should not matter though.
                      */
                    if(posPriority.optionPending == false) {
                        /* option is not pending so place trades */
                        postAllTrades(actPos, posPriority);
                    }
                    /* check for filled orders and process.. */
                    fillAllTrades(actPos);
                 
                } /* positionNeedsTrade */

            } /* for */
            positionsToHandle.semGive();

        }

    } /* runHandler */

} /* tradeHandler */
