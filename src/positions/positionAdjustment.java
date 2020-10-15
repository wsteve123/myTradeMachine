/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.Serializable;
import java.text.ParseException;
import positions.tradeHandler.limitOrders;

/**
 *
 * @author walterstevenson
 */
public class positionAdjustment implements Serializable {

    public float longPrice;
    public float shortPrice;
    public float delta;
    public adjOutcome outcome;
    public adjOutcome auxOutcome;
    public int sharesLong;
    public int sharesShort;
    public float balance;
    private positionData owner;
    private Date adjDate;
    private String adjDateStr;
    public float profitLoss;
    private float actualPercentMove = 0;
    private boolean tradeMe = false;
    private boolean tradePending = false;
    private long tradeIdNumber = 0;
    public limitOrders actLimitOrder = null;
    private boolean filled;
    /* added 12/12/10 to fix a problem when we need to get rid
     * of an option with market order. This happens when
     * the price of the stock moves so much that
     * the bid == 0, so we just want to get rid of the option thus use
     * market order for the option - very unusual case but it can happen.
     */
    private boolean optionMarketOrder = false;
    /* 
     * This flag is used to flag tradeHandler to not update prices when
     * doing an averaging in adjustment. This is because the normal price adjustments 
     * use the last adj price to determine if a movement in underlying should justify an
     * dn adjustment. When we do a ave in, it should not affect the price (price should remain the same  
     * from previous real adjustment).
     */
    public boolean ivAveInFlag = false;
 
    positionAdjustment(boolean newDate) {
        longPrice = 0;
        shortPrice = 0;
        delta = 0;
        outcome = new adjOutcome();
        outcome.setOutcomeNotUsed();
        auxOutcome = new adjOutcome();
        auxOutcome.setOutcomeNotUsed();
        sharesLong = 0;
        sharesShort = 0;
        balance = 0;
        owner = null;
        profitLoss = 0;
        actualPercentMove = 0;
        if (newDate) {
            adjDate = new Date();
        }
        adjDateStr = getAdjDateStr();
        tradeMe = false;
        tradePending = false;
        tradeIdNumber = 0;
        actLimitOrder = null;
        filled = false;
        optionMarketOrder = false;
        
    }
    public boolean getTradeNeeded() {
        return tradeMe;
    }
    public void setTradeNeeded(boolean tin) {
        tradeMe = tin;
    }
    public boolean getTradePending() {
        return tradePending;
    }
    public void setTradePending(boolean tpin) {
        tradePending = tpin;
    }
    public long getTradeIdNumber() {
        return tradeIdNumber;
    }
    public void setTradeIdNumber(long id) {
        tradeIdNumber = id;
    }
    
    Date getAdjDate() {
        return (adjDate);
    }
    public String getAdjDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        if (adjDate != null) {
            adjDateStr = sdf.format(adjDate);

        }
        return (adjDateStr);
    }
/*    
    String getAdjDateStr() {
        //SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yy");
        //Date date = sdf.format("12/4/09");
        //adjDateStr = DateFormat.getDateInstance().format(adjDate);
        if (adjDate != null) {
            return (DateFormat.getDateInstance().format(adjDate));
        }
        return (adjDateStr);
    }
*/
     public void setAdjDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            adjDate = sdf.parse(dateStr);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
    public void setAdjDate(Date date) {
        adjDate = date;
    }

    public void setActualMove(float am) {
        actualPercentMove = am;
    }

    public float getActualMove() {
        return (actualPercentMove);
    }
    public boolean isItOptionMarketOrder(){
        return(optionMarketOrder);
    }
    public void setOptionMarketOrder(boolean inVal){
        optionMarketOrder = inVal;
    }
    public class adjOutcome implements Serializable {

        private final int NU = 0;
        public final int SS = 01;
        public final int BS = 02;
        public final int SL = 03;
        public final int BL = 04;
        public final int RIGHT_QUARTILE = 01;
        public final int LEFT_QUARTILE = 02;
        public final int CENTER_QUARTILE = 0;
        public int outcome;
        public int adjustment;
        public int BSCount;
        public int SSCount;
        public int BLCount;
        public int SLCount;

        private adjOutcome() {
            BSCount = 0;
            SSCount = 0;
            BLCount = 0;
            SLCount = 0;
            adjustment = 0;
            outcome = 0;
        }

        boolean isOutcomeShort() {
            return (isOutcomeSellShort() || isOutcomeBuyShort());
        }

        boolean isOutcomeLong() {
            return (isOutcomeSellLong() || isOutcomeBuyLong());
        }

        boolean isOutcomeSellShort() {
            return (outcome == SS);
        }

        boolean isOutcomeBuyShort() {
            return (outcome == BS);
        }

        boolean isOutcomeSellLong() {
            return (outcome == SL);
        }

        boolean isOutcomeBuyLong() {
            return (outcome == BL);
        }

        boolean isOutcomeUsed() {
            return (outcome != NU);
        }

        int getOutcome() {
            return (outcome);
        }

        void setOutcome(int os) {
            outcome = os;
        }

        void setOutcomeNotUsed() {
            outcome = NU;
        }

        void setOutcomeSellShort(int num) {
            outcome = SS;
            adjustment = num;
            SSCount++;
            tradeMe = true;
            tradePending = false;
        }

        void setOutcomeBuyShort(int num) {
            outcome = BS;
            adjustment = num;
            BSCount++;
            tradeMe = true;
            tradePending = false;
        }

        void setOutcomeSellLong(int num) {
            outcome = SL;
            adjustment = num;
            SLCount++;
            tradeMe = true;
            tradePending = false;
        }

        void setOutcomeBuyLong(int num) {
            outcome = BL;
            adjustment = num;
            BLCount++;
            tradeMe = true;
            tradePending = false;
        }
        int getAdjustment(){
            return adjustment;
        }

        public String getOutcomeShortStr() {
            if (outcome == SS) {
                return ("(S" + Integer.toString(adjustment) + ")");
            } else if (outcome == BS) {
                return ("(B" + Integer.toString(adjustment) + ")");
            } else {
                return ("(--)");
            }

        }

        public String getOutcomeLongStr() {
            if (outcome == SL) {
                return ("(S" + Integer.toString(adjustment) + ")");
            } else if (outcome == BL) {
                return ("(B" + Integer.toString(adjustment) + ")");
            } else {
                return ("(--)");
            }
        }
    }
}
