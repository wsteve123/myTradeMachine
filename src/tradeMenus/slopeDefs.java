/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
/**
 *
 * @author earlie87
 */
public class slopeDefs {
    public static final int TRADING_DAYS_PER_YEAR = 252;
    public static final int TRADING_WEEKS_PER_YEAR = 52;
    public static final int TRADING_MONTHS_PER_YEAR = 12;
	public static final int TRADING_1SEC_PER_DAY = 23400;
	public static final int TRADING_5SEC_PER_DAY = 4680;
	public static final int TRADING_10SEC_PER_DAY = 2340;
	public static final int TRADING_15SEC_PER_DAY = 1560;
	public static final int TRADING_30SEC_PER_DAY = 780;
	public static final int TRADING_1MIN_PER_DAY = 390;
	public static final int TRADING_2MIN_PER_DAY = 195;
	public static final int TRADING_3MIN_PER_DAY = 130;
	public static final int TRADING_5MIN_PER_DAY = 78;
	public static final int TRADING_10MIN_PER_DAY = 39;
	public static final int TRADING_15MIN_PER_DAY = 26;
	public static final int TRADING_20MIN_PER_DAY = 19;
	public static final int TRADING_30MIN_PER_DAY = 13;
	public static final int TRADING_1HOUR_PER_DAY = 6;
	public static final int TRADING_2HOUR_PER_DAY = 3;
	public static final int TRADING_3HOUR_PER_DAY = 2;
    public static final int MA_50DAY = 50;
    public static final int MA_100DAY = 100;
    public static final int MA_200DAY = 200;
    public static final int MA_10DAY = 10;
    public static final int MA_20DAY = 20;
    public static final int MA_30DAY = 30;
    public static final int MA_150DAY = 150;
    public static final int MA_TRADING_DAYS_PER_YEAR = TRADING_DAYS_PER_YEAR;
    public static final int MA_50DAY_MAXSZ = MA_TRADING_DAYS_PER_YEAR - MA_50DAY;
    public static final int MA_100DAY_MAXSZ = MA_TRADING_DAYS_PER_YEAR - MA_100DAY;
    public static final int MA_200DAY_MAXSZ = MA_TRADING_DAYS_PER_YEAR - MA_200DAY;
    public static final int AVE_90DAY = 90;
    
    public static final int SLOPE_OFF = 0;
    public static final int SLOPE_CHANGE = 1;
    public static final int SLOPE_UPTREND = 2;
    public static final int SLOPE_DNTREND = 3;
    public static final int SLOPE_LONGEST = 4;
    public static final int SLOPE_SAVE_ALL = 5;
    public static final int SLOPE_FILTERED = 6;
    public static final int SLOPE_BULLBEAR_CROSS = 7;
    public static final int SLOPE_SET_MA_WINDOW = 8;
    
    public static final String TREND_UP = "trendUp";
    public static final String TREND_DN = "trendDN";
    public static final String NO_HISTORY = "NO_HISTORY";
    
    public static final int DIRECTION_CHANGE = 1;
    public static final int UPTREND = 2;
    public static final int DNTREND = 3;
    
    public static final int SLOPE_DAYS = 5;
    
    //pos state the ticket can be..
    public static final String NEW = "NEW";
    public static final String BOT = "BOT";
    public static final String SOLD = "SLD";
    public static final String FAILED = "FAILED";
    //added 2.25.16 all posabilities..
    public static final String oBuyToOpenLong = "oBuyToOpenLong";
    public static final String oSellToCloseLong = "oSellToCloseLong";
    public static final String oBuyToCloseShort = "oBuyToCloseShort";
    public static final String oSellToOpenShort = "oSellToOpenShort";
    public static final String oNoTradeYet = "oNoTradeYet";
    
    //added 3.20.16 to flag segments are full for first time and stope updating origCost
    public static final double oORIG_COST_FULL = 100000;
    
    
    public static final String YES = "YES";
    public static final String NO = "NO";
    //states of a portfolio
    public static final String oREADY     = "RDY";
    public static final String oRUNNING   = "RUNNING";
    public static final String oINIT      = "INIT";
    //public static final String oLONG      = "LONG";
    //public static final String oSHORT     = "SHORT";
    //bull/bear crossing slope defs
    //TurnedOff
    public static final String oTURNED_OFF = "TURNED_OFF";
    //BULL when 50dma crosses 200dma
    public static final String oBULL_50_200DMA = "BULL_50_200DMA";
    //bull when 50dma is highest, 100dma is middle, and 200dma is lowest
    public static final String oBULL_50_100_200DMA = "BULL_50_100_200DMA";
    //bear when 50dma crosses 200dma, ordered 200dma higher than 50dma.
    public static final String oBEAR_200_50DMA = "BEAR_200_50DMA";
    //bear when 200dma is highest, 100dma is middle, and 50dma is lowest.
    public static final String oBEAR_200_100_50DMA = "BEAR_200_100_50DMA";
    
    public static final int oTURNED_OFF_INT = 0;
    public static final int oBULL_50_200DMA_INT = 1;
    public static final int oBULL_50_100_200DMA_INT = 2;
    public static final int oBEAR_200_50DMA_INT = 3;
    public static final int oBEAR_200_100_50DMA_INT = 4;
    
    //sellCriteria defs..
    public static final int oS_STOP_LOSS = 0;
    public static final int oS_LOCK_GAIN = 1;
    public static final int oS_HITS_10DMA = 2;
    public static final int oS_HITS_50DMA = 3;
    public static final int oS_HITS_100DMA = 4;
    public static final int oS_HITS_200DMA = 5;
    public static final int oS_NONE = 6;
    
    //buyCriteria defs..
    public static final int oB_BUY_NOW = 0;
    public static final int oB_HITS_10MA = 1;
    public static final int oB_HITS_50MA = 2;
    public static final int oB_HITS_100MA = 3;
    public static final int oB_HITS_200MA = 4;
    public static final int oB_NONE = 5;
    
    //buyCriteria defs str..
    public static final String oB_BUY_NOW_STR = "oB_BUY_NOW_STR";
    public static final String oB_HITS_10MA_STR = "oB_HITS_10MA_STR";
    public static final String oB_HITS_50MA_STR = "oB_HITS_50MA_STR";
    public static final String oB_HITS_100MA_STR = "oB_HITS_100MA_STR";
    public static final String oB_HITS_200MA_STR = "oB_HITS_200MA_STR";
    public static final String oB_NONE_STR = "oB_NONE_STR";
    
    public static final int oBiasLong = 0;
    public static final int oBiasShort = 1;
    public static final int oBiasLongAndShort = 2;
    public static final int oBiasDisabled = 3;
    
    public static final String oBiasLongStr = "BiasLong";
    public static final String oBiasShortStr = "BiasShort";
    public static final String oBiasLongAndShortStr = "BiasLongAndShort";
    public static final String oBiasDisabledStr = "BiasDisabled";
    
    //trade config
    public static final int oPivot = 0;
    public static final int oClosedAboveBelowMa = 1;
    public static final String oOPEN = "OPEN";
    public static final String oCLOSE = "CLOSE";
    //segTrades
    public static final double oCLOSED_POSITION = -1.0;
    public static final String DAY = "1 day";
    public static final String WEEK = "1 week";
    public static final int oOneYearTrades = 253;
    public static final String getPositionBiasStr(int bint){
        String s1 = "";
        switch (bint){
            case oBiasLong:
                s1 = oBiasLongStr;
                break;
            case oBiasShort:
                s1 = oBiasShortStr;
                break;
            case oBiasLongAndShort:
                s1 = oBiasLongAndShortStr;
                break;
            case oBiasDisabled:
                s1 = oBiasDisabledStr;
                break;
        }
        return s1;
    }
    public static enum ReqHistDataBarSizes{ 
        oDay(DAY),
        oWeek(WEEK);        
        private String strVal = "";
        private ReqHistDataBarSizes(String sin){
            strVal = sin;
        }
    }    
    
}
