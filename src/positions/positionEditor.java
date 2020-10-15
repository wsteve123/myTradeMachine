/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;
import java.math.*;
import positions.positionData.positionStates;
/**
 *
 * @author walterstevenson
 */
public class positionEditor {

    private positionData editPosition;
    public positionAdjustment editAdjData;
    private boolean interimAdjustment = false;

    public positionEditor(positionData pos) {

        editPosition = pos;
        /* create adjustment */
        
        editAdjData = new positionAdjustment(true);

        editPosition.initCurrentAdjData(editAdjData);
    }
    public positionData getEditPosition() {
        return(editPosition);
    }
    boolean isInterimAdj() {
        return (interimAdjustment);
    }

    public void startOutPosition(int longs, int shorts, float bias) {
        /* we are starting this position,
         * so buy initial long and short shares.
         */
        interimAdjustment = true;
        /* do longs first */
        if (longs > 0 /*editAdjData.sharesLong == 0*/) {
            editAdjData.outcome.setOutcomeBuyLong(longs);
            editAdjData.sharesLong += editAdjData.outcome.adjustment;
//            editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.longPrice);
            
        } else if (shorts > 0 /*editAdjData.sharesShort == 0*/) {
            editAdjData.outcome.setOutcomeBuyShort(shorts);
            editAdjData.sharesShort += editAdjData.outcome.adjustment;
//            editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
            
        }
        editPosition.closed = false;

        /* setting the state to FILL triggers the tradeHandler to do the actual trade ticket to do the initial fill of this position.
           An adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade(s), adjustments will be allowed safely.
         */
        if(editPosition.currentState != positionStates.FILL) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.FILL);    
        }
        
        
        editPosition.posAdjAdd(editAdjData);
    /* these values are used to flag when 
     * we switch quartiles in commit edit 
     */

    }

    void closeOutPosition() {
        /* we are closing this position,
         * so sell everything and mark closed and 
         * interimAdjustment.
         */

        interimAdjustment = true;
        
        /* order important here. Do option first so that it trades first before
         * the stock upon closing */
        
        if (editAdjData.sharesShort > 0) {
            editAdjData.outcome.setOutcomeSellShort(editAdjData.sharesShort);
            editAdjData.sharesShort -= editAdjData.outcome.adjustment;
//            editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);

        } else if (editAdjData.sharesLong > 0) {
            editAdjData.outcome.setOutcomeSellLong(editAdjData.sharesLong);
            editAdjData.sharesLong -= editAdjData.outcome.adjustment;
//            editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);

        }

        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
        /* moved this to tradeHandler so that we flag when the position is really closed */
        //editPosition.closed = true;
         /* setting the state to CLOSED triggers the tradeHandler to do the actual trade ticket to close the position.
           Another adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade.
         */
        if(editPosition.currentState != positionStates.CLOSED) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.CLOSED);    
        }
        
        editPosition.posAdjAdd(editAdjData);
    }

    void forcePositionAdj() {
    }

    void buyLong(int shares) {
    
        editAdjData.outcome.setOutcomeBuyLong(shares);
        editAdjData.sharesLong += editAdjData.outcome.adjustment;
        editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.longPrice);

        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
    }

    void sellLong(int shares) {

        editAdjData.outcome.setOutcomeSellLong(shares);
        editAdjData.sharesLong -= editAdjData.outcome.adjustment;
        editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);

        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
    }

    void buyShort(int shares) {
 
        editAdjData.outcome.setOutcomeBuyShort(Math.round(shares / 100));
        editAdjData.sharesShort += editAdjData.outcome.adjustment;
        editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
  
        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
    }

    void sellShort(int shares) {
 
        editAdjData.outcome.setOutcomeSellShort(Math.round(shares / 100));
        editAdjData.sharesShort -= editAdjData.outcome.adjustment;
        editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
        

        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
    }
    int checkQuartileSwitchAmount() {
    /*
     * This routine checks to see if after a quartile switch and after eccess shares or options were
     * already sold off, we have enough of a bias to perform the purchase of either stock or option.
     * The routine returns the number of contracts or shares
     * are still off neutral. The caller can decide to act.
     * Note: the routine assumes interim is set true, meaning quartile switch occurred and eccess was already
     * sold off. A quartile switch occures when a stock price moves behond the original entry point. For example say
     * we got in to a position at 10 dallars per share. The stock moved downward to 9 and we aquired more shares going down.
     * All of a sudden, the stock jumps to over 10, say 10.5. This is a quartile move, the logic then says to get rid of any aquired stock first
     * to try and nuetralize the position. Then, after the stock is shed, buy more shorts. Well after the stock was sold off,
     * the # of contracts would many times be 0, so an error would occur.
     * This routine will make sure we have enough to justify the additional adjustment to prevent the error condition. 
     */
        int qamount=0;
        float posBiasFloat;
        int posBiasInt;
        float posBiasSign;
        boolean posBiasPositive = false;

        /* we must be in interimAdjustment mode, otherwise forget it. */
        if (interimAdjustment == false) {
            return(qamount);
        }
        posBiasFloat = ((float) editAdjData.sharesLong + (float) ((float) editAdjData.sharesShort * 100.0 * editAdjData.delta * -1.0));
        posBiasSign = Math.signum(posBiasFloat);
        if (posBiasSign > 0.0) {
            posBiasPositive = true;
        } else {
            posBiasPositive = false;
        }
        posBiasInt = Math.round(posBiasFloat);
        posBiasInt = Math.abs(posBiasInt);

        if (posBiasPositive) {
            /* check if in fact we did switch quartile. */
            if (editAdjData.longPrice > editPosition.longEntryPrice) {
                /* means we are now exposed in the long direction
                 * by posBiasInt amount. And in the right quartile.
                 * so we need to adjust by going short this same amount with
                 * buy short, but only if there's enough to do so. Return this amount
                 * and let the caller decide.
                 */
                 qamount = Math.round((float) ((float) posBiasInt / editAdjData.delta) / 100);

            } else {
                /* did not switch quartile, so just return zero */
                qamount = 0;

            }
        } else {
            /* must be negative so exposed in short direction
             * by posBiasInt amount.
             * so we need to adjust by going long this same amount.
             * Options are buy long or sell short, depends on
             * which quartile we are currently in.
             */
            if (editAdjData.longPrice < editPosition.longEntryPrice) {
                qamount = posBiasInt;

            } else {
                /* did not switch quartiles? error..just return zero. */
                qamount = 0;
            }
        }

     return(qamount);
    }
    void commitPosEdit(float longPrice, float shortPrice, float delta) {
        float posBiasFloat;
        int posBiasInt;
        float posBiasSign;
        boolean posBiasPositive = false;

        editAdjData.delta = delta;
        editAdjData.longPrice = longPrice;
        editAdjData.shortPrice = shortPrice;//wfs

        posBiasFloat = ((float) editAdjData.sharesLong + (float) ((float) editAdjData.sharesShort * 100.0 * editAdjData.delta * -1.0));
        posBiasSign = Math.signum(posBiasFloat);
        if (posBiasSign > 0.0) {
            posBiasPositive = true;
        } else {
            posBiasPositive = false;
        }
        posBiasInt = Math.round(posBiasFloat);

        System.out.println("posBias = " + posBiasPositive + "posBiasInt = " + posBiasInt + "posBiasFloat = " + posBiasFloat);
        posBiasInt = Math.abs(posBiasInt);
        /*
         * we check to see weather we are 
         * exposed long or short and 
         * which quartile we are in to determine the 
         * operation for this adjustment.
         */
        if (posBiasPositive) {
            if (editAdjData.longPrice > editPosition.longEntryPrice) {
                if (editAdjData.sharesLong > editPosition.staLongShares) {
                    /* we have switched to other quartile and we
                    have extra long shares that we need to sell off
                    before we start buying shorts. 
                     */
                    /* 10/21/2012 can't sell all extra here anymore because we are accumulating shares 
                     * when implied vol goes down (averaging in IV), so we can't sell all excess shares it would peg 
                     * us to be extra short then we would end up bying a bunch of puts...so no. Instead just sell the number
                     * of longs you need to sell no more.
                     */
                    editAdjData.outcome.setOutcomeSellLong(posBiasInt);
                    //editAdjData.outcome.setOutcomeSellLong(editAdjData.sharesLong - editPosition.staLongShares);
                    editAdjData.sharesLong -= editAdjData.outcome.adjustment;
//                    editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);
                    System.err.println("commitPosEdit:switched to other quartile and have extra shares to sell. would have sold: "+
                                        (editAdjData.sharesLong - editPosition.staLongShares)+ " but no longer, instead selling : " + 
                                        posBiasInt);
                    interimAdjustment = true;
                } else {
                    /* means we are now exposed in the long direction
                     * by posBiasInt amount. And in the right quartile.
                     * so we need to adjust by going short this same amount with
                     * buy short.
                     */
                    editAdjData.outcome.setOutcomeBuyShort(Math.round((float) ((float) posBiasInt / delta) / 100));
                    editAdjData.sharesShort += editAdjData.outcome.adjustment;
//                    editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
                }
            } else {
                /* means we are now exposed in the long direction
                 * by posBiasInt amount. And in the left quartile.
                 * so we need to adjust by going short this same amount with
                 * selling long.
                 */
                editAdjData.outcome.setOutcomeSellLong(posBiasInt);
                editAdjData.sharesLong -= editAdjData.outcome.adjustment;
//                editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);
            }
        } else {
            /* must be negative so exposed in short direction
             * by posBiasInt amount.
             * so we need to adjust by going long this same amount.
             * Options are buy long or sell short, depends on 
             * which quartile we are currently in.
             */
            if (editAdjData.longPrice < editPosition.longEntryPrice) {
                if (editAdjData.sharesShort > editPosition.staShortShares) {
                    /* we have switched to other quartile and we
                    have extra short contracts that we need to sell off
                    before we start buying longs 
                     */
                    
                    /* 10/21/2012 can't sell all extra here anymore because we are accumulating options 
                     * when implied vol goes down (averaging in IV), so we can't sell all excess options it would peg 
                     * us to be extra long then we would end up buying a bunch of longs...so no. Instead just sell the number
                     * of shorts you need sell no more.
                     */
                    
                    editAdjData.outcome.setOutcomeSellShort(Math.round((float) ((float) posBiasInt / delta) / 100));
                    // was : editAdjData.outcome.setOutcomeSellShort(editAdjData.sharesShort - editPosition.staShortShares);
                    editAdjData.sharesShort -= editAdjData.outcome.adjustment;
//                    editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
                    
                    System.err.println("commitPosEdit:switched to other quartile and have extra option puts to sell. would have sold: "+
                                        (editAdjData.sharesShort - editPosition.staShortShares)+ " but no longer, instead selling : " + 
                                        Math.round((float) ((float) posBiasInt / delta) / 100));
                    
                    interimAdjustment = true;
                } else {
                    editAdjData.outcome.setOutcomeBuyLong(posBiasInt);
                    editAdjData.sharesLong += editAdjData.outcome.adjustment;
//                    editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.longPrice);
                }
            } else {
                editAdjData.outcome.setOutcomeSellShort(Math.round((float) ((float) posBiasInt / delta) / 100));
                editAdjData.sharesShort -= editAdjData.outcome.adjustment;
//                editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
            }
        }


        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));

        /* setting the state to ADJUST triggers the tradeHandler to do the actual trade ticket.
           Another adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade, the state will be changed back to ACTIVE, then other adjustments can occure safely.
         */
        if(editPosition.currentState != positionStates.ADJUST) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.ADJUST);
        }
        editPosition.posAdjAdd(editAdjData);
    }

    void commitPosBuyShorts(float longPrice, float shortPrice, int shortsTobuy, float delta) {
        /* don't update this because this is an aveIn operation that shouldn't
         * affect any of these values.
         
        editAdjData.delta = delta;
        editAdjData.longPrice = longPrice;
        editAdjData.shortPrice = shortPrice;
        *
        */
        /* set this flag to tell tradeHandle to not update long/short price values. */
        editAdjData.ivAveInFlag = true;
        /* buy puts .. */
        editAdjData.outcome.setOutcomeBuyShort(shortsTobuy);
        editAdjData.sharesShort += editAdjData.outcome.adjustment;
        
        
        editAdjData.profitLoss = (((editAdjData.sharesLong * longPrice) + 
                                       (editAdjData.sharesShort * shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
        
        /* setting the state to ADJUST triggers the tradeHandler to do the actual trade ticket.
           Another adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade, the state will be changed back to ACTIVE, then other adjustments can occure safely.
         */
        if(editPosition.currentState != positionStates.ADJUST) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.ADJUST);
        }
                
        editPosition.posAdjAdd(editAdjData);
        
    }
    void commitPosBuyLongs(float longPrice, float shortPrice, int shortsTobuy, float delta) {
        /* we know how many puts to buy lets calculate the number of longs to buy so that
         * we have neutral outcome.
           */
        int longsToBuy;
        longsToBuy = Math.round(((float)shortsTobuy * delta * (float) 100.0));
        System.err.println("comitPosNeutral: longs to buy is " + longsToBuy + " with these shorts: " + shortsTobuy + " and this delta:  " + delta);
        
        /* don't update this because this is an aveIn operation that shouldn't
         * affect any of these values.
         
        editAdjData.delta = delta;
        editAdjData.longPrice = longPrice;
        editAdjData.shortPrice = shortPrice;
        *
        */
        /* set this flag to tell tradeHandle to not update long/short price values. */
        editAdjData.ivAveInFlag = true;
        /* buy longs now */
        editAdjData.outcome.setOutcomeBuyLong(longsToBuy);
        editAdjData.sharesLong += editAdjData.outcome.adjustment;
        
        editAdjData.profitLoss = (((editAdjData.sharesLong * longPrice) + 
                                       (editAdjData.sharesShort * shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));
        
        /* setting the state to ADJUST triggers the tradeHandler to do the actual trade ticket.
           Another adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade, the state will be changed back to ACTIVE, then other adjustments can occure safely.
         */
        if(editPosition.currentState != positionStates.ADJUST) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.ADJUST);
        }
                
        editPosition.posAdjAdd(editAdjData);
        
    }
    void commitPosEdit(float longPrice, float shortPrice, float delta, boolean filler) {
        float posBiasFloat;
        int posBiasInt;
        float posBiasSign;
        boolean posBiasPositive = false;

        editAdjData.delta = delta;
        editAdjData.longPrice = longPrice;
        editAdjData.shortPrice = shortPrice;//wfs

        int intTmp1;
//        BigDecimal bigDTmp1;
        float floatTmp1;
        int shortContracts;
        int longShares;
        /*            
        BigDecimal wfs;
        float wfsf= (float)12.5;
        wfs = new BigDecimal(wfsf);
        System.out.println("intValue : "+wfs.intValue());
        System.out.println("fracValue : "+(float)(wfsf - wfs.intValue()));
         */
        posBiasFloat = ((float) editAdjData.sharesLong + (float) ((float) editAdjData.sharesShort * 100.0 * editAdjData.delta * -1.0));
        posBiasSign = Math.signum(posBiasFloat);
        if (posBiasSign > 0.0) {
            posBiasPositive = true;
        } else {
            posBiasPositive = false;
        }
        posBiasInt = Math.round(posBiasFloat);

        System.out.println("posBias = " + posBiasPositive + "posBiasInt = " + posBiasInt + "posBiasFloat = " + posBiasFloat);
        posBiasInt = Math.abs(posBiasInt);
        /*
         * we check to see weather we are 
         * exposed long or short and 
         * which quartile we are in to determine the 
         * operation for this adjustment.
         */
        if (posBiasPositive) {
            if (editAdjData.longPrice > editPosition.longEntryPrice) {
                if (editAdjData.sharesLong > editPosition.staLongShares) {
                    /* we have switched to other quartile and we
                    have extra long shares that we need to sell off
                    before we start buying shorts. 
                     */
                    editAdjData.outcome.setOutcomeSellLong(editAdjData.sharesLong - editPosition.staLongShares);
                    editAdjData.sharesLong -= editAdjData.outcome.adjustment;
//                    editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);
                    interimAdjustment = true;
                } else {
                    /* means we are now exposed in the long direction
                     * by posBiasInt amount. And in the right quartile.
                     * so we need to adjust by going short this same amount with
                     * buy short.
                     */
                    // the following section was added to "fill" in more precisly with shares and contracts 
                    // instead of just contracts.
                    // first round the ammount we are off
                    intTmp1 = Math.round(posBiasInt);
                    //floatTmp1 will have the number of contracts on the whole number
                    floatTmp1 = (float) ((float) intTmp1 / delta / (float) 100);
                    shortContracts = (int) (floatTmp1);
                    // we subtract the whole # and work with fraction part
                    // multiply by 100 then by delta to give us the # of shares this represents.
                    longShares = Math.round(((float) (floatTmp1 - shortContracts)) * 100 * delta);
                    //so shortContracts + longShares should equal the amount to adjust exactly.
                    // if not zero, then make short adjustment...
                    if (shortContracts > 0) {
                        editAdjData.outcome.setOutcomeBuyShort(shortContracts);
                        editAdjData.sharesShort += editAdjData.outcome.adjustment;
//                        editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
                    }
                    // may need to qualify here and not make adjustment if value too small.....
                    editAdjData.auxOutcome.setOutcomeSellLong(longShares);
                    editAdjData.sharesLong -= editAdjData.auxOutcome.adjustment;
//                    editAdjData.balance += (editAdjData.auxOutcome.adjustment * editAdjData.longPrice);

                }
            } else {
                /* means we are now exposed in the long direction
                 * by posBiasInt amount. And in the left quartile.
                 * so we need to adjust by going short this same amount with
                 * selling long.
                 */
                editAdjData.outcome.setOutcomeSellLong(posBiasInt);
                editAdjData.sharesLong -= editAdjData.outcome.adjustment;
//                editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.longPrice);
            }
        } else {
            /* must be negative so exposed in short direction
             * by posBiasInt amount.
             * so we need to adjust by going long this same amount.
             * Options are buy long or sell short, depends on 
             * which quartile we are currently in.
             */
            if (editAdjData.longPrice < editPosition.longEntryPrice) {
                if (editAdjData.sharesShort > editPosition.staShortShares) {
                    /* we have switched to other quartile and we
                    have extra short contracts that we need to sell off
                    before we start buying longs 
                     */
                    editAdjData.outcome.setOutcomeSellShort(editAdjData.sharesShort - editPosition.staShortShares);
                    editAdjData.sharesShort -= editAdjData.outcome.adjustment;
//                    editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
                    interimAdjustment = true;
                } else {
                    editAdjData.outcome.setOutcomeBuyLong(posBiasInt);
                    editAdjData.sharesLong += editAdjData.outcome.adjustment;
//                    editAdjData.balance -= (editAdjData.outcome.adjustment * editAdjData.longPrice);
                }
            } else {
                // the following section was added to "fill" in more precisly with shares and contracts 
                // instead of just contracts.
                // first round the ammount we are off
                intTmp1 = Math.round(posBiasInt);
                //floatTmp1 will have the number of contracts on the whole number
                floatTmp1 = (float) ((float) intTmp1 / delta / (float) 100);
                shortContracts = (int) (floatTmp1);
                // we subtract the whole # and work with fraction part
                // multiply by 100 then by delta to give us the # of shares this represents.
                longShares = Math.round(((float) (floatTmp1 - shortContracts)) * 100 * delta);
                //so shortContracts + longShares should equal the amount to adjust exactly.
                // if not zero, then make short adjustment...
                if (shortContracts > 0) {
                    editAdjData.outcome.setOutcomeSellShort(shortContracts);
                    editAdjData.sharesShort -= editAdjData.outcome.adjustment;
//                    editAdjData.balance += (editAdjData.outcome.adjustment * editAdjData.shortPrice * 100);
                }
                // may need to qualify here and not make adjustment if value too small.....    
                editAdjData.auxOutcome.setOutcomeBuyLong(longShares);
                editAdjData.sharesLong += editAdjData.auxOutcome.adjustment;
//                editAdjData.balance -= (editAdjData.auxOutcome.adjustment * editAdjData.longPrice);
            }
        }

        editAdjData.profitLoss = (((editAdjData.sharesLong * editAdjData.longPrice) + 
                                       (editAdjData.sharesShort * editAdjData.shortPrice * 100) +
                                       (editAdjData.balance)) - (editPosition.posBalance));

        /* setting the state to ADJUST triggers the tradeHandler to do the actual trade ticket.
           Another adjustment will not happen while we are in this state. Once the tradeHandler completes the desired
           trade, the state will be changed back to ACTIVE, then other adjustments can occure safely.
         */
        if(editPosition.currentState != positionStates.ADJUST) {
            editPosition.saveCurrentState();
            editPosition.setState(positionStates.ADJUST);
        }
                
        editPosition.posAdjAdd(editAdjData);
    }
    
} /* positionEditor */
    
