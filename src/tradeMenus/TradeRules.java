/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import ibTradeApi.ibApi.historicalData;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import positions.commonGui;
import positions.myUtils;
import static positions.myUtils.convertDate;
import tradeMenus.NewTraderTest.*;
//import tradeMenus.NewTraderTest.TradeLotData;

/**
 *
 * @author earlie87
 */
public class TradeRules {

    //private final String[] movingAveragesList = {"10 Day", "20 Day", "50 Day", "100 Day", "150 Day", "200 Day"};
    IOTextFiles.ioRdTextFiles tradeRulesRdTextFile;
    IOTextFiles.ioWrTextFiles tradeRulesWrTextFile;
    IOTextFiles.ioDeleteTextFiles tradeRulesDeleteTextFile;
    IOTextFiles ioTextFiles = new IOTextFiles();
    IOTextFiles.ioListDirectory tradeRulesDirList;
    List<File> rulesDirList;
    String prefixDirectory = "";
    final String rulesExtention = ".rules";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    List<TradeRules.Term> activeTerms = null;
    List<TradeRules.Term> openTerms = null;
    List<TradeRules.Term> closeTerms = null;
    ibApi actIbApi = ibApi.getActApi();
    ibApi.historicalData hd = null;
    historicalData hdOuter = actIbApi.new historicalData();
    ibApi.historicalData.data histData = hdOuter.new data();
    ibApi.historicalData.data histDataArray[] = null;
    List<ibApi.historicalData.data> histDataList ;//= new ArrayList<ibApi.historicalData.data>();
    final int RUN_IN_DAYS = 1;
    SupportedMovingAverages movingAves = new SupportedMovingAverages();
    OccurenceLogPeriod tradeLogPeriod;
    JCheckBox GTLTCheckBox;
    EquationTrigger openEquationTrigger = new EquationTrigger();
    EquationTrigger closeEquationTrigger = new EquationTrigger();
    int tid = 0;
	public boolean testIt = false;
	String userSelectedMaType = "None";
    public class EquationTrigger{
        String trigger = "";
        int bars = 0;
        String description = "";
        String getTrigger(){
            return trigger;
        }
        void setTrigger(String trigin){
            trigger = trigin;
        }
        int getBars(){
            return bars;
        }
        void setBars(int barsin){
            bars = barsin;
        }
        String getDescription(){
            switch(trigger){
                case "None":
                    description = "None";
                    break;
                case "Delayed":
                    description = "Equation true for " + bars + " bars then start.";
                    break;                
                case "Begin":
                    description = "Equation becomes true within last " + bars + " bars.";
                    break;
                case "":
                    break;
            }
            return description;
        }
    }
	public static enum MovingAverages {
		o1Day("1 BarMa", 1),
		o2Day("2 BarMa", 2),
		o3Day("3 BarMa", 3),
		o4Day("4 BarMa", 4),
		o5Day("5 BarMa", 5),
		o10Day("10 BarMa", 10),
		o15Day("15 BarMa", 15),
		o20Day("20 BarMa", 20),
		o25Day("25 BarMa", 25),
		o30Day("30 BarMa", 30),
		o35Day("35 BarMa", 35),
		o40Day("40 BarMa", 40),
		o50Day("50 BarMa", 50),
		o60Day("60 BarMa", 60),
		o70Day("70 BarMa", 70),
		o100Day("100 BarMa", 100),
		o150Day("150 BarMa", 150),
		o200Day("200 BarMa", 200),
		//500day == 100week
		o500Day("500 BarMa", 500);
		private final String strVal;
		private final int intVal;

		MovingAverages(String strin, int intin) {
			strVal = strin;
			intVal = intin;
		}

		public String getStrVal() {
			return strVal;
		}

		public int getIntVal() {
			return intVal;
		}
	}

	public class SupportedMovingAverages {

		MovingAverages ma;
		int sz = MovingAverages.values().length;
		String[] listStr;
		int[] listInt;

		public String[] getMaList() {
			String[] retListStr = new String[sz];
			int[] retlistInt = new int[sz];
			for (MovingAverages ma : MovingAverages.values()) {
				retListStr[ma.ordinal()] = ma.strVal;
				retlistInt[ma.ordinal()] = ma.intVal;
			}
			listStr = retListStr;
			listInt = retlistInt;
			return retListStr;
		}

		public int getMaIntegerValue(int indexin) {
			return listInt[indexin];
		}
	}

	public static enum TradeTerms {
		oPivotalHL("PivotalHL"),
		oPivotalLH("PivotalLH"),
		oStrength("Strength"),
		oWeakness("Weakness"),
		oMovingAverage("MovingAverage"),
		oMovingAveragePlus("MovingAveragePlus"),
		oHistorical("Historical"),
		oRSIGT("RSIGT"),
		oRSILT("RSILT"),
		oSlopeNeg("NegSlope"),
		oSlopePos("PosSlope"),
		oVolume("Volume"),
		oSlopeFilter("SlopeFilter"),
		oCompareMAs("Compare2MAs"),
		oCorrectionAve("CorrectionAve");
		private String strVal = "";

		TradeTerms(String sin) {
			strVal = sin;
		}

		public TradeTerms findTerm(String sin) {
			TradeTerms retValue = null;
			for (TradeTerms actTerm : TradeTerms.values()) {
				if (actTerm.strVal.equals(sin)) {
					retValue = actTerm;
				}
			}
			return retValue;
		}
	}

    public static enum Logic4Terms {

        oAnd("AND"),
        oOr("OR"),
        oNone("NONE");
        private String strVal = "";

        Logic4Terms(String sin) {
            strVal = sin;
        }

        public String toString() {
            return strVal;
        }
    }
    public class OccurenceData{
        String occurenceDate = "";  //date it occurured
        String occurenceDescription = "";
        String action = "";     //Open or Close
        double closePrice = 0.0;//close price of stock
        double actualPercent = 0.0;
        double actualRsi = 0.0;
        double actualSlope = 0.0;
        double actualAverageSlope = 0.0;
        double actualMaValue = 0.0;
        double actualMa2Value = 0.0;
        double actualYearAveHi = 0.0;
        double actualYearAveLo = 0.0;
        double actualCorrection = 0.0;
		double actualAveCorrection = 0.0;
        List<String> termOutputList = new ArrayList<>();
    }
    public class MergeList{
        //list of occurence lists to merge
        String logicalOperator;
        String termsDescription;
        List<OccurenceData> occurences = new ArrayList<>();
    }
    public class TermHead{
        boolean termIsTrue;
	boolean termRanFine;
        TradeTerms term;
        String andOrLogic;
        String termDescription;
        int numOfOccurences;
    }
    public class TermOccurences{
        /*
        TradeTerms term;
        String andOrLogic;
        String termDescription;
        int numOfOccurences;
        */
        TermHead termHead;
        List<OccurenceData> occurences = new ArrayList<>();
        TermOccurences(){
            termHead = new TermHead();
        }
    }
    public class OccurenceLogPeriod{
        String ticker;
        String termsDescription;
        String year;
		String time;
        String duration;
		int durationInBars;
        String barSize;
        String backTestSize;
        int backTestSizeInBars;
        String begDate;
        String endDate;
        String begBtDate;
        String endBtDate;
        double begPrice;
        double endPrice;
        double begBtPrice;
        double endBtPrice;
		double cashToSpend;
		int liveSharesToBuy;
		//this stock is part of an index of this many stocks..
		int partOfIndexOfThisSize;
        double gainLoss;
        double gainLossPercent;
        double openGainLoss;
        double openGainLossPercent;
		//added stock stuff..
		double gainLossStock;
        double gainLossPercentStock;
        double openGainLossStock;
        double openGainLossPercentStock;
		double accumClosedCostStock;
		double accumOpenCostStock;
		double accumClosedProceedsStock;
		double accumOpenProceedsStock;
		//averages
		double accumClosedCostAveStock;
		double accumOpenCostAveStock;
		double accumClosedProceedsAveStock;
		double accumOpenProceedsAveStock;
		//buy hold..
        double buyHoldGainLoss;
        double buyHoldGainLossPercent;
        double firstOpenClosePrice;
        double lastCloseClosePrice;
        double accumClosedCostAve;
        double accumOpenCostAve;
        double accumOpenProceedesAve;
        double accumClosedProceedesAve;
        String closeDate;
        int numOfTerms;
        boolean positionOpen;        
        int positionDaysOpen;
        int positionLastCloseInDays;
        double openPositionPrice;        
        boolean histDataError = false;
        //int tradeCount = 0;
        int numOfBuyLots = 1;
        int numOfSellLots = 1;
        List<ibApi.historicalData.data> histDataList = new ArrayList<ibApi.historicalData.data>();
        boolean equationIsTrue;//List<TradesData> allOccurences
        List<TermOccurences> allTerms = new ArrayList<>();
        List<OccurenceData> allOccurences = new ArrayList<>();
        List<OccurenceData> transitionOccurences = new ArrayList<>();
        List<OccurenceData> currentOccurences = new ArrayList<>();
        List<OccurenceData> mergedOpenCloseOccurences = new ArrayList<>();
        //List<CompletedTradeTry1> completedTradesTry1 = new ArrayList<>(); 
        CompletedTradeTry1 completedTradesTry1; 
        List<CompletedTrade> completedTrades = new ArrayList<>();
        private int getBarsPerYear(String barsin){
            int retBars = 0;
            switch(barsin){
                case "1 day":
                    retBars = slopeDefs.TRADING_DAYS_PER_YEAR;
                    break;
                case "1 week":
                    retBars = slopeDefs.TRADING_WEEKS_PER_YEAR;
                    break;
                case "1 month":
                    retBars = slopeDefs.TRADING_MONTHS_PER_YEAR;
                    break;
				case "1 secs":
                    retBars = slopeDefs.TRADING_1SEC_PER_DAY;
                    break;	
				case "5 secs":
                    retBars = slopeDefs.TRADING_5SEC_PER_DAY;
                    break;
				case "10 secs":
                    retBars = slopeDefs.TRADING_10SEC_PER_DAY;
                    break;
				case "15 secs":
                    retBars = slopeDefs.TRADING_15SEC_PER_DAY;
                    break;
				case "30 secs":
                    retBars = slopeDefs.TRADING_30SEC_PER_DAY;
                    break;	
				case "1 min":
                    retBars = slopeDefs.TRADING_1MIN_PER_DAY;
                    break;
				case "2 mins":
                    retBars = slopeDefs.TRADING_2MIN_PER_DAY;
                    break;
				case "3 mins":
                    retBars = slopeDefs.TRADING_3MIN_PER_DAY;
                    break;
				case "5 mins":
                    retBars = slopeDefs.TRADING_5MIN_PER_DAY;
                    break;
                case "10 mins":
                    retBars = slopeDefs.TRADING_10MIN_PER_DAY;
                    break;
                case "15 mins":
                    retBars = slopeDefs.TRADING_15MIN_PER_DAY;
				case "20 mins":
                    retBars = slopeDefs.TRADING_20MIN_PER_DAY;
				case "30 min":
                    retBars = slopeDefs.TRADING_30MIN_PER_DAY;
                    break;	
            }
            return retBars;
        }
		private int getNumberOfBarsPerDuration(String durationIn, String barsIn){
			/*
			calculates number of bars for the duration wanted. Example:
			duration is 10 d, 1 sec bars. need to calculate how many 1 second bars there are
			in 10 day duration.
			Duration Strings (historical time)
			S  for seconds
			D  for days
			W  for weeks
			M  for months
			Y  for years
			examples: 10 D, 5 W, 4 M, 2 Y

			Valid Bar sizes:
			1 secs 5 secs 10 secs 15 secs 30 secs
			1 mins 2 mins 3 mins 5 mins 10 mins 15 mins 20 mins 30 mins
			1 hours 2 hours 3 hours 4 hours 8 hours
			1 day
			1 week
			1 month
			
			example:                                                            Duration  barsz
			client.reqHistoricalData(4001, ContractSamples.EurGbpFx(), queryTime, "1 M", "1 day", "TRADES", 1, 1, false, null);
			*/
			//seconds per week
			final int SecondsPerWeek = (slopeDefs.TRADING_1SEC_PER_DAY * 5);
			//seconds per month first..
			final int SecondsPerMonth = SecondsPerWeek * 4;
			//minutes per day
			final int MinutesPerDay = 390 /* 60 X 6.5 */;
			//minutes per week
			final int MinutesPerWeek = MinutesPerDay * 5;
			//minutes per month
			final int MinutesPerMonth = MinutesPerWeek * 4;
			//minutes per year
			final int MinutesPerYear = MinutesPerMonth * 12;
			//hours per day
			final double HoursPerDay = 6.5;
			//hours per week
			final double HoursPerWeek = 6.5 * 5;
			//hours per month
			final double HoursPerMonth = HoursPerWeek * 4;
			//hours per year
			final double HoursPerYear = HoursPerMonth * 12;
			//hours per day
			final double DaysPerDay = 1;
			//hours per year
			final double DaysPerYear = slopeDefs.TRADING_DAYS_PER_YEAR;
			//hours per month
			final double DaysPerMonth = DaysPerYear / 12;
			//hours per week
			final double DaysPerWeek = DaysPerMonth / 4;
			//weeks per day
			final double WeeksPerDay = 0;
			//hours per week
			final double WeeksPerWeek = 1;
			//hours per month
			final double WeeksPerMonth = WeeksPerWeek * 4;
			//hours per year
			final double WeeksPerYear = WeeksPerMonth * 12;
			//months per year
			final double MonthsPerYear = 12;
			int retBars = 0;
			//remove all letters and return just number
			int barMult = Integer.valueOf(barsIn.replaceAll("[^0-9]", ""));
			int durMult = Integer.valueOf(durationIn.replaceAll("[^0-9]", ""));
			String errString = "";
			//remove all numbers and return just letters
			String durationLetter = durationIn.replaceAll("[^A-Za-z]","");
			if(barsIn.contains("sec") == true){
				//handle seconds bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is also seconds	
						100 secs duration, 1 sec bar, so 100 x 1 = 100
						500 secs duration, 15sec bar, so 500 / 15 = 33.33 15sec bars in 500 seconds
						*/
						retBars = (durMult / barMult);
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is seconds
						how many seconds in a day. 60 x 60 x 6.5 
						example 5 day duration, 10 sec bars is 5 x 23400 = 117000, div by 5 = 23400 5sec bars in 5 days.
						*/
						retBars = ((durMult * slopeDefs.TRADING_1SEC_PER_DAY) / barMult);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is seconds
						how many seconds in a day. 60 x 60 x 6.5 = 23400
						example 2 week duration, 5 sec bars is 5 x 23400 = 117000, div by 5 = 23400 5sec bars in 5 days.
						*/												
						retBars = ((durMult * SecondsPerWeek) / barMult);
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is seconds
						how many seconds in a day. 60 x 60 x 6.5 = 23400
						example 2 months duration, 15 sec bars is 5 x 23400 = 117000, div by 5 = 23400 5sec bars in 5 days.
						*/
						retBars = ((durMult * SecondsPerMonth) / barMult);
					break;
					case "Y":
						//years; don't bother..
					break;
					default : 
						//wrong do something!!;
				}
				
			}else if (barsIn.contains("min") == true){
				//handle minute(s) bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is minute(s)	
						don't bother should not happen..
						*/
						errString = "getNumberOfBarsPerDuration: " + " duration seconds and bar size minutes????";
						retBars = -1;
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is minute(s)						
						example 5 day duration, 10 min bars.
						example 10 day duration, 1 min bar.
						*/
						retBars = ((durMult * MinutesPerDay) / barMult);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is minute(s)
						example 2 week duration, 5 min bars.
						*/												
						retBars = ((durMult * MinutesPerWeek) / barMult);
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is minute(s)
						how many seconds in a day. 60 x 60 x 6.5 = 23400
						example 2 months duration, 10 min bars.
						*/
						retBars = ((durMult * MinutesPerMonth) / barMult);
					break;
					case "Y":
						/*
						ok, we know duration is year(s), and bars size is minute(s)
						example 2 years duration, 15 min bars 
						*/
						retBars = ((durMult * MinutesPerYear) / barMult);
					break;
					default : 
						//wrong do something!!;
				}
				
			}else if (barsIn.contains("hour") == true){
				//handle hour(s) bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is hour(s)	
						don't bother should not happen..
						*/
						errString = "getNumberOfBarsPerDuration: " + " duration seconds and bar size hour????";
						retBars = -1;
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is hour(s)						
						example 5 day duration, 2 hour bars.						
						*/
						retBars = (int)(((double)durMult * HoursPerDay) / (double)barMult);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is hours(s)
						example 2 week duration, 5 hour bars.
						*/												
						retBars = (int)(((double)durMult * HoursPerWeek) / (double)barMult);
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is hours(s)
						example 2 months duration, 2 hour bars.
						*/
						retBars = (int)(((double)durMult * HoursPerMonth) / (double)barMult);
					break;
					case "Y":
						/*
						ok, we know duration is year(s), and bars size is hour(s)
						example 2 years duration, 5 hour bars 
						*/
						retBars = (int)(((double)durMult * HoursPerYear) / (double)barMult);
					break;
					default : 
						//wrong do something!!;
				}
				
			}else if (barsIn.contains("day") == true){
				//handle day(s) bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is day(s)	
						don't bother should not happen..
						*/
						errString = "getNumberOfBarsPerDuration: " + " duration seconds and bar size day????";
						retBars = -1;
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is day(s)						
						example 5 day duration, 1 day bars.						
						*/
						retBars = (int)(((double)durMult * DaysPerDay) / (double)barMult);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is day(s)
						example 10 day duration, 1 day bars.
						*/												
						retBars = (int)(((double)durMult * DaysPerWeek) / (double)barMult);
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is day(s)
						example 2 months duration, 1 day bars.
						example 24 M duration, 2 day bars
						*/
						retBars = (int)(((double)durMult * DaysPerMonth) / (double)barMult);
					break;
					case "Y":
						/*
						ok, we know duration is year(s), and bars size is day(s)
						example 2 years duration, 2 day bars 
						*/
						retBars = (int)(((double)durMult * DaysPerYear) / (double)barMult);
					break;
					default : 
						//wrong do something!!;
				}				
			}else if (barsIn.contains("week") == true){
				//handle week(s) bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is week(s)	
						don't bother should not happen..
						*/
						errString = "getNumberOfBarsPerDuration: " + " duration seconds and bar size week????";
						retBars = -1;
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is week(s)						
						example 100 day duration, 1 week bars.
						how many weeks in a 100 days? 100 / 5 = 20 weeks
						20 weeks / 1week bar = 20;
						*/
						double weeksByDays = durMult / 5;
						retBars = (int)(((double)weeksByDays)/ (double)barMult);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is week(s)
						example 10 week duration, 1 week bars.
						*/												
						retBars = (int)(((double)durMult) / (double)barMult);
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is week(s)
						example 2 months duration, 1 week bars. 8 / 1 = 8; 8/1 = 8 bars
						example 12 months duration, 2 week bars. 4 x 12 = 48/2 = 24 bars
						12 x 4 is 48 weeks duration. how many 2 week bars ? 48 / 2 = 24
						*/
						retBars = (int)(((double)durMult * WeeksPerMonth) / (double)barMult);
					break;
					case "Y":
						/*
						ok, we know duration is year(s), and bars size is week(s)
						example 2 years duration, 2 week bars 
						2 years is 96(48 x 2) weeks 96 / 2 week bars is 48 2week bars in 2years
						*/
						retBars = (int)(((double)durMult * WeeksPerYear) / (double)barMult);
					break;
					default : 
						//wrong do something!!;
				}
				
			}else if (barsIn.contains("month") == true){
				//handle month(s) bar
				switch(durationLetter){
					case "S":
						/*
						ok, we know duration is seconds, and bars size is week(s)	
						don't bother should not happen..
						*/
						errString = "getNumberOfBarsPerDuration: " + " duration seconds and bar size month????";
						retBars = -1;
						;
					break;
					case "D":
						/*
						ok, we know duration is days, and bars size is month(s)						
						example 200 day duration, 1 month bars.
						how many months in a 200 days? 200 / daysPerMonth = 20 weeks
						200(days) / 20(daysPerMonth) = 10 month bars in 200 days; 
						20 weeks / 1week bar = 20;
						*/							
						retBars = (int)((double)durMult / DaysPerMonth);
					break;
					case "W":
						/*
						ok, we know duration is week(s), and bars size is month(s)
						example 52 week duration, 1 month bars.
						how many months in 52 weeks? (52 * DaysPerWeek) / DaysPerMonth
						*/												
						retBars = (int)(((double)durMult * DaysPerWeek) / (double)(barMult * DaysPerMonth));
					break;
					case "M":
						/*
						ok, we know duration is month(s), and bars size is month(s)
						example 24 months duration, 2 month bars. 24 / 2 = 12 2-monthBars
						example 12 months duration, 2 month bars. 12 / 2 = 6 2-monthBars
						*/
						retBars = (int)((double)durMult / (double)barMult);
					break;
					case "Y":
						/*
						ok, we know duration is year(s), and bars size is month(s)
						example 2 years duration, 2 month bars 
						how many months in 2 years? 2 x MonthsPerYear = 2 x 12 = 24; 24 / 2 = 12 2-month bars
						2 years is 96(48 x 2) weeks 96 / 2 week bars is 48 2week bars in 2years
						*/
						retBars = (int)(((double)durMult * MonthsPerYear) / (double)barMult);
					break;
					default : 
						//wrong do something!!;
				}
			}
			return retBars;
		}
        OccurenceLogPeriod(String tickerIn, String yrIn, String durIn, String btSzIn, String barSzIn, String timeIn){
            String tmpa;
			double years;
            hd = actIbApi.setActHistoricalData(hd);
            hd.setSelectedYear(year = yrIn);
			hd.setSelectedTime(time = timeIn);
            //X2 the duration i.e. 1 Y to 2 Y
            //durIn = Integer.toString(Integer.parseInt(durIn.substring(0,1)) * 2) + durIn.substring(1);			
			/*
			extract only numbers of first two terms then extract Letter at the end..
			*/
            tmpa = Integer.toString(Integer.parseInt(durIn.replaceAll("[^0-9]", "")) + Integer.parseInt(btSzIn.replaceAll("[^0-9]", ""))) + " " + durIn.replaceAll("[^a-zA-Z]", "");
			//ib does not like if you say 15 months; it wants years if over 12 months. so if over 12 months round up to 2 years
			if(tmpa.contains("M")){
				//contains months check for over 12 months so we can round up to 2 years..
				if((years = (Integer.parseInt(tmpa.replaceAll("[^0-9]", "")) / 12.0)) > 1.0){
					//put rounded up years in the place of months..
					years++;
					tmpa = Integer.toString((int)years) + " " + "Y";
				}
			}
            //original that worked ---> backTestSizeInBars = Integer.parseInt(btSzIn.substring(0,1)) *  getBarsPerYear(barSzIn);
			backTestSizeInBars = getNumberOfBarsPerDuration(btSzIn, barSzIn);
            duration = durIn;
			durationInBars = getNumberOfBarsPerDuration(durIn, barSzIn);
            backTestSize = btSzIn;
            hd.setSelectedDuration(tmpa);
            hd.setSelectedBarSize(barSize = barSzIn);
            hd.setSelectedBackTestSizeInBars(backTestSizeInBars);
            ticker = tickerIn;
        }          
    }
    public TradeRules(){
        
    }
    public OccurenceLogPeriod getTradeLogPeriod(){
        return tradeLogPeriod;
    }

    public void setTradeLogPeriod(OccurenceLogPeriod tlpIn) {
        tradeLogPeriod = tlpIn;
    }
    public class Term {

        private Term daTerm;
        private String description = "";
        private int andOr = 0; //and == 1, or == 2, 0 == off; 
        private int movingAverage = 0;
        private Object andOrSelItem;
        TradeTerms term;
        private Object termSelItem;
        TermOccurences termTrades;
        public Term() {            
            termTrades = new TermOccurences();
        }
           
        public void clearMe() {
            description = "";
            andOr = 0;
            daTerm = null;
        }

        public void getUserInput() {

        }

        public void setDescription(String sin) {
            description = sin;
        }

        public String getDescription() {
            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setTerm(TradeTerms termin) {
            term = termin;
        }

        public void setAndOr(int aoin) {
            andOr = aoin;
        }

        public boolean isAnd() {
            //if andOr == true, AND function is true, else false;
            return (andOr == 1);
        }

        public boolean isOr() {
            //if andOr == false, OR function is true, else false;
            return (andOr == 2);
        }

        public int getAndOr() {
            return andOr;
        }

        public void setAndOrSelItem(Object i) {
            andOrSelItem = i;
        }

        public Object getAndOrSelItem() {
            return andOrSelItem;
        }

        public void setTermSelItem(Object i) {
            termSelItem = i;
        }

        public Object getTermSelItem() {
            return termSelItem;
        }

        public Term getDaTerm() {
            return daTerm;
        }

        public void setDaTerm(Term tin) {
            daTerm = tin;
        }
        public int getMa() {
            return movingAverage;
        }

        public void setMa(int tin) {
            movingAverage = tin;
        }
        public TermOccurences processTerm(String tickin, String operation, int maIn){
            return termTrades;
        }
        public void setUpLogPeriod(String tickerin){
            int btDateLocation = (tradeLogPeriod.backTestSizeInBars);
            if(btDateLocation > (histDataList.size() - 1)){
                System.out.println("\nerror!!! with ticker.." + tickerin + "backTestSizeInBars: " + tradeLogPeriod.backTestSizeInBars + "histDataListSz: " + histDataList.size());
                btDateLocation = (histDataList.size() - 1);
            }
            tradeLogPeriod.ticker = histDataList.get(0).getTicker();
            tradeLogPeriod.begDate = histDataList.get(histDataList.size() -1).getDate();
            tradeLogPeriod.endDate = histDataList.get(0).getDate();
            tradeLogPeriod.begPrice = histDataList.get(histDataList.size() -1 ).getClosePrice();
            tradeLogPeriod.endPrice = histDataList.get(0).getClosePrice();
            tradeLogPeriod.begBtDate = histDataList.get(btDateLocation).getDate();
            tradeLogPeriod.endBtDate = histDataList.get(0).getDate();
            tradeLogPeriod.begBtPrice = histDataList.get(btDateLocation).getClosePrice();
            tradeLogPeriod.endBtPrice = histDataList.get(0).getClosePrice();
            tradeLogPeriod.buyHoldGainLossPercent = myUtils.roundMe((((tradeLogPeriod.endBtPrice - tradeLogPeriod.begBtPrice) / tradeLogPeriod.begBtPrice) * 100.0), 2);
            tradeLogPeriod.buyHoldGainLoss = myUtils.roundMe((tradeLogPeriod.endBtPrice - tradeLogPeriod.begBtPrice), 2);
            tradeLogPeriod.histDataList = histDataList;
        }
    
	public boolean prepareHistoricalData(String tickerin, boolean forceNew) {
		boolean retVal = false;
		//check if we already have the historical data.. if so skip asking IB..
		if (tickerin.equals(tradeLogPeriod.ticker) && (forceNew == false) && 
		   (tradeLogPeriod.histDataList != null) && (tradeLogPeriod.histDataList.size() > 0)) {
			histDataList = tradeLogPeriod.histDataList;
			setUpLogPeriod(tickerin);
			retVal = true;
			return (retVal);
		}
		hd.nextTid(tid);
		tid++;
		if (hd.filledDone == true) {
			System.out.println("\nfilled done??");
		}
		if ((retVal = hd.getHistoricalData(tickerin)) == true) {
			if(userSelectedMaType.equals("Simple")){
				hd.calcSimpleMovingAve(MovingAverages.o1Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o2Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o3Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o4Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o5Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o10Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o15Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o20Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o25Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o30Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o35Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o40Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o50Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o60Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o70Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o100Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o200Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o150Day.intVal);
				hd.calcSimpleMovingAve(MovingAverages.o500Day.intVal);
			}else if (userSelectedMaType.equals("Weighted")){
				hd.calcWeightedMovingAve(MovingAverages.o3Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o10Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o15Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o20Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o25Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o30Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o35Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o40Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o50Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o60Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o70Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o100Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o200Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o150Day.intVal);
				hd.calcWeightedMovingAve(MovingAverages.o500Day.intVal);
			}else if (userSelectedMaType.equals("Hull")){
				hd.calcHullMovingAve(MovingAverages.o3Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o10Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o15Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o20Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o25Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o30Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o35Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o40Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o50Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o60Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o70Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o100Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o200Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o150Day.intVal);
				hd.calcHullMovingAve(MovingAverages.o500Day.intVal);
			}			
			
			hd.calcPercentages();
			hd.calcRSI1();
			hd.calcRsiMovingAve();
			hd.calcRsiMovingAve1(MovingAverages.o200Day.intVal);
			hd.calcRsiMovingAve1(MovingAverages.o50Day.intVal);
			hd.calcRsiMovingAve1(MovingAverages.o100Day.intVal);
			hd.calcRsiMovingAve1(MovingAverages.o20Day.intVal);
			//histDataList.removeAll(histDataList);
			histDataList = new ArrayList<ibApi.historicalData.data>();
			histDataList = hd.getHistData();
			setUpLogPeriod(tickerin);
			System.out.println("\ngot HistData...size is: " + histDataList.size());
		} else {
			System.out.println("\nHistData...failed for " + tickerin + " errMsg: " + hd.errorMsg);
		}
		return retVal;
	}

	public int getNumOfTerms() {
		return tradeLogPeriod.numOfTerms;
	}
}

	public void setActiveTerms(List<TradeRules.Term> termsin) {
		activeTerms = termsin;
	}

	public List<TradeRules.Term> getActiveTerms() {
		return activeTerms;
	}

	public void setOpenTerms(List<TradeRules.Term> termsin) {
		openTerms = termsin;
	}

	public List<TradeRules.Term> getOpenTerms() {
		return openTerms;
	}

	public void setClosedTerms(List<TradeRules.Term> termsin) {
		closeTerms = termsin;
	}

	public List<TradeRules.Term> getClosedTerms() {
		return closeTerms;
	}

	public int findHistDataIdx(String datein, List<ibApi.historicalData.data> histDataList) {
		int idx = -1;
		int sz = histDataList.size();
		for (idx = sz - 1; (idx >= 0); idx--) {
			if (histDataList.get(idx).getDate().equals(datein)) {
				break;
			}
		}
		return idx;
	}
    public List<String> getTradeRulesFiles(){
        List<String> list = new ArrayList();
        tradeRulesDirList = ioTextFiles.new ioListDirectory(homeDirectory, "rules");
        rulesDirList = tradeRulesDirList.getList();
        for (int i = 0; i < rulesDirList.size(); i++){
            list.add(rulesDirList.get(i).getName());
        }
        return list;
    }
	public int getTradingDaysBetweenDates(String d1, String d2) {
		String sta = d1;
		String stp = d2;
		int staIdx;
		int stpIdx;
		int idx;
		int backup = 0;
		long[] dhs = new long[4];
		dhs = myUtils.getDiffInDaysHoursMinSecs(d1, d2);
		/*
        if((backup = (int)myUtils.getDiffInDays(d1, d2)) < 0){
            //negative number means d2 date is before d1..
            sta = d2;
            stp = d1;
        }
		 */
		//find start idx
		staIdx = findHistDataIdx(sta, histDataList);
		stpIdx = findHistDataIdx(stp, histDataList);

		return (staIdx - stpIdx);
	}
    public int getTradingBarsBetweenDates(String d1, String d2){
        String sta = d1;
        String stp = d2;
        int staIdx;
        int stpIdx;
		Date beg = convertDate(d1);
        Date end = convertDate(d2);
		/*
		if (end.getTime() < beg.getTime()){
			//negative number means end date is before beg..so switch em
            sta = d2;
            stp = d1;
		}
		*/
        //find start idx
        staIdx = findHistDataIdx(sta, histDataList);
        stpIdx = findHistDataIdx(stp, histDataList);        
        
        return (staIdx - stpIdx);
    }	
    public void setEquationTrigger(String opin, String trin){
        if(opin.equals("Open")){
            openEquationTrigger.setTrigger(trin);
        }else if(opin.equals("Close")){
            closeEquationTrigger.setTrigger(trin);
        }else{
            
        }            
    }
    public String getEquationTrigger(String opin){
        String retVal = "";
        if(opin.equals("Open")){
            retVal =  openEquationTrigger.getTrigger();
        }else if(opin.equals("Close")){
            retVal =  closeEquationTrigger.getTrigger();
        }else{
            
        }   
        return retVal;
    }
	public void setMaType(String main){
		userSelectedMaType = main;
	}
	public String getMaType(){
		return userSelectedMaType;
	}
    public void setEquationBars(String opin, int barsin){
        if(opin.equals("Open")){
            openEquationTrigger.setBars(barsin);
        }else if(opin.equals("Close")){
            closeEquationTrigger.setBars(barsin);
        }            
    }
    public int getEquationBars(String opin) {
        int retVal = 0;
        if (opin.equals("Open")) {
            retVal = openEquationTrigger.getBars();
        } else if (opin.equals("Close")) {
            retVal = closeEquationTrigger.getBars();
        }
        return retVal;
    }
    public String getEquationDesciption(String opin){
        String retVal = "";
        if (opin.equals("Open")) {
            retVal = openEquationTrigger.getDescription();
        } else if (opin.equals("Close")) {
            retVal = closeEquationTrigger.getDescription();
        }
        return retVal;
    }
    public void loadTradeRulesFile(String filein){
        //read terms into activeTerm..
        rdTermsFromTextFile(filein, ""/*no extension*/);
    }
    
    public TradeRules.Term getSelectedTerm(TradeRules.TradeTerms termin) {
        /*
         TradeTerms term = TradeTerms.oSlopePos;
         */
        TradeRules.Term retTerm = null;
        switch (termin) {
            case oHistorical:
                retTerm = new Historical();
                break;
            case oMovingAverage:
                retTerm = new MovingAverage();
                break;
            case oMovingAveragePlus:
                retTerm = new MovingAveragePlus();
                break;
            case oPivotalHL:
                retTerm = new PivotalHL();
                break;
            case oPivotalLH:
                retTerm = new PivotalLH();
                break;
            case oRSIGT:
                retTerm = new RsiGT();
                break;
            case oRSILT:
                retTerm = new RsiLT();
                break;
            case oSlopeNeg:
                retTerm = new SlopeNeg();
                break;
            case oSlopePos:
                retTerm = new SlopePos();
                break;
            case oStrength:
                retTerm = new Strength();
                break;
            case oVolume:
                retTerm = new Volume();
                break;
            case oWeakness:
                retTerm = new Weakness();
                break;
            case oSlopeFilter:
                retTerm = new SlopeFilter();
                break;
            case oCompareMAs:
                retTerm = new Compare2MAs();
                break;
			case oCorrectionAve:
                retTerm = new CorrectionAve();
                break;
            default:
                retTerm = null;
        }
        return retTerm;
    }
    boolean userSelLTE = false; // <=
    boolean userSelGTE = false; // >=
    boolean userSelLT = false;  // <
    boolean userSelGT = false;  // >
    private void GTELTECheckBoxActionPerformed(java.awt.event.ActionEvent evt) {                                             
        // TODO add your handling code here:
        String text = "";
        if (GTLTCheckBox.isSelected() == true) {
            //true == GTE greater than or equal to...
            userSelGTE = true;
            userSelLTE = !userSelGTE;
            text = ">=";
            
        } else {
            //false == LTE less than or equal to...
            userSelGTE = false;
            userSelLTE = !userSelGTE;
            text = "<=";
        }
        GTLTCheckBox.setText(text);
        GTLTCheckBox.setSelected(userSelGTE);
        System.out.println("GTE: " + userSelGTE + " LTE: " + userSelLTE); 
    }
    private void GTLTCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        String text = "";
        if (GTLTCheckBox.isSelected() == true) {
            //true == GT greater...
            userSelGT = true;
            userSelLT = !userSelGT;
            text = ">";

        } else {
            //false == LTE less than or equal to...
            userSelGT = false;
            userSelLT = !userSelGT;
            text = "<";
        }
        GTLTCheckBox.setText(text);
        GTLTCheckBox.setSelected(userSelGT);
        System.out.println("GT: " + userSelGT + " LT: " + userSelLT);
    }

    public void wrTermsToTextFile(String tradeRulesFileNameIn) {
        int sz = activeTerms.size();
        int idx = 0;
        boolean done = false;
        boolean openTermsDone = false;
        boolean closeTermsDone = false;
        String termOperation = "";
        if (ioTextFiles.doesFileExist(tradeRulesFileNameIn + rulesExtention)) {
            if (commonGui.postConfirmationMsg("File Exists. Overwrite?") != 0) {
                return;
            }
        }
        tradeRulesWrTextFile = ioTextFiles.new ioWrTextFiles(tradeRulesFileNameIn + rulesExtention, false);
        TradeRules.Term actTerm = new Term();
        if (activeTerms.equals(openTerms)) {
            System.out.println("\nterm op == open");
            termOperation = "Open";
        } else if (activeTerms.equals(closeTerms)) {
            System.out.println("\nterm op == close");
            termOperation = "Close";
        } else {
            done = true;
        }
		//added ma type (simple, weighted, hull etc) 5/21/2020
		tradeRulesWrTextFile.write("//UserSelectedMaType");
		tradeRulesWrTextFile.write(userSelectedMaType);	
        while (!done) {
            sz = activeTerms.size();
            tradeRulesWrTextFile.write("//Term Operation");
            tradeRulesWrTextFile.write(termOperation);
            //new equation trigger with bars..
            tradeRulesWrTextFile.write("//equation Trigger");
            tradeRulesWrTextFile.write(getEquationTrigger(termOperation));
            tradeRulesWrTextFile.write("//equation Bars");
            tradeRulesWrTextFile.write(Integer.toString(getEquationBars(termOperation)));
					
            for (idx = 0; idx < sz; idx++) {
                actTerm = (TradeRules.Term) activeTerms.get(idx);
                tradeRulesWrTextFile.write("//beg");
                tradeRulesWrTextFile.write("//idx");
                tradeRulesWrTextFile.write(Integer.toString(idx));
                tradeRulesWrTextFile.write("//Term strval");
                tradeRulesWrTextFile.write(actTerm.getTerm().strVal);
                tradeRulesWrTextFile.write("//Description");
                tradeRulesWrTextFile.write(actTerm.getDescription());
                tradeRulesWrTextFile.write("//AndOr");
                tradeRulesWrTextFile.write(Integer.toString(actTerm.getAndOr()));
                tradeRulesWrTextFile.write("//AndOrSelItem");
                tradeRulesWrTextFile.write((String) actTerm.getAndOrSelItem());
                tradeRulesWrTextFile.write("//TermSelItem");
                tradeRulesWrTextFile.write((String) actTerm.getTermSelItem());
                tradeRulesWrTextFile.write("//MovingAve");
                tradeRulesWrTextFile.write(Integer.toString(actTerm.getMa()));                
                switch (actTerm.getTerm()) {
                    case oMovingAverage:
                        tradeRulesWrTextFile.write("//PercentWithin");
                        TradeRules.MovingAverage maTerm = (MovingAverage) actTerm;
                        tradeRulesWrTextFile.write(Double.toString(maTerm.getPercentValue()));
						tradeRulesWrTextFile.write("//SelCount");
						tradeRulesWrTextFile.write(Integer.toString(maTerm.getCount()));
						tradeRulesWrTextFile.write("//CurrentPriceWithinSelected");
						tradeRulesWrTextFile.write(Boolean.toString(maTerm.isCurrentPriceWithinPercentSelected()));
						tradeRulesWrTextFile.write("//ConsecutiveTimesAboveSelected");
						tradeRulesWrTextFile.write(Boolean.toString(maTerm.isConsecutiveTimesAboveSelected()));
						tradeRulesWrTextFile.write("//ConsecutiveTimesBelowSelected");
						tradeRulesWrTextFile.write(Boolean.toString(maTerm.isConsecutiveTimesBelowSelected()));
                        break;
                    case oMovingAveragePlus:
                        tradeRulesWrTextFile.write("//PercentWithin");
                        TradeRules.MovingAveragePlus maTermPlus = (MovingAveragePlus) actTerm;
                        tradeRulesWrTextFile.write(Double.toString(maTermPlus.getPercentGtValue()));
                        tradeRulesWrTextFile.write(Double.toString(maTermPlus.getPercentLtValue()));
                        break;
                    case oHistorical:
                        tradeRulesWrTextFile.write("//PercentWithin");
                        Historical histTerm = (Historical) actTerm;
                        tradeRulesWrTextFile.write(Double.toString(histTerm.getPercentValue()));
                        tradeRulesWrTextFile.write("//DaysHistory");
                        tradeRulesWrTextFile.write(Integer.toString(histTerm.getDaysHistory()));
                        break;
                    case oRSIGT:
                        tradeRulesWrTextFile.write("//GT RSI");
                        RsiGT rsiGtTerm = (RsiGT) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(rsiGtTerm.getGtRsi()));
                        break;
                    case oRSILT:
                        tradeRulesWrTextFile.write("//LT RSI");
                        RsiLT rsiLtTerm = (RsiLT) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(rsiLtTerm.getLtRsi()));
                        break;
                    case oSlopeNeg:
                        tradeRulesWrTextFile.write("//Days history");
                        SlopeNeg snegTerm = (SlopeNeg) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(snegTerm.getDaysHistory()));
                        break;
                    case oSlopePos:
                        tradeRulesWrTextFile.write("//Days history");
                        SlopePos sposTerm = (SlopePos) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(sposTerm.getDaysHistory()));
                        break;
                    case oVolume:
                        tradeRulesWrTextFile.write("//VolumeValue");
                        Volume volTerm = (Volume) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(volTerm.getVolumeVal()));
                        break;
                    case oSlopeFilter:
                        tradeRulesWrTextFile.write("//SlopeFilter");
                        SlopeFilter sSlopeFilterTerm = (SlopeFilter) actTerm;
                        tradeRulesWrTextFile.write(Double.toString(sSlopeFilterTerm.getSlopeValue()));
                        tradeRulesWrTextFile.write("//DaysHistory");
                        tradeRulesWrTextFile.write(Integer.toString(sSlopeFilterTerm.getDaysHistory()));
                        break;
                    case oCompareMAs:
                        tradeRulesWrTextFile.write("//CompareMAs");
                        Compare2MAs compareTerm = (Compare2MAs) actTerm;
                        tradeRulesWrTextFile.write("//MovingAveB");
                        tradeRulesWrTextFile.write(Integer.toString(compareTerm.getMaB()));
                        tradeRulesWrTextFile.write("//Operator");
                        tradeRulesWrTextFile.write(compareTerm.getOperator());
                        break;
					case oCorrectionAve:
                        tradeRulesWrTextFile.write("//PercentWithInCorrection");
                        CorrectionAve aveCorrectionTerm = (CorrectionAve) actTerm;
                        tradeRulesWrTextFile.write(Integer.toString(aveCorrectionTerm.getCorrectionWithInPercent()));
                        break;	
                    default:;
                }/*switch*/
            }/*for*/            
            tradeRulesWrTextFile.write("//end");            
            if (activeTerms.equals(openTerms)) {
                openTermsDone = true;
                if ((closeTerms.size() > 0) && (closeTermsDone == false)) {
                    activeTerms = closeTerms;
                    System.out.println("\nterm op == close");
                    termOperation = "Close";
                } else {
                    done = true;
                }
            } else if (activeTerms.equals(closeTerms)) {
                closeTermsDone = true;
                if ((openTerms.size() > 0) && (openTermsDone == false)) {
                    activeTerms = openTerms;
                    System.out.println("\nterm op == open");
                    termOperation = "Open";
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }/*while*/		
        tradeRulesWrTextFile.closeWr();
    }
    List<String> fileBuff = new ArrayList<String>();

    public List<String> fillBuf(IOTextFiles.ioRdTextFiles fileIn) {
        /*
         read in only non commented liness in to fileBuff.
         */
        String strin;
        fileBuff.clear();
        List<String> buff = new ArrayList<String>();
        while ((strin = fileIn.read(false)) != null) {
            if (strin.contains("//")) {

            } else {
                buff.add(strin);
            }
        }
        return buff;
    }

    public void rdTermsFromTextFile(String fileNameIn, String ext) {
        int index = 0;
        int lcnt = 0;
        String tmpStr = "";
        String termOperation = "";
        TradeTerms tradeTerm = TradeTerms.oHistorical;
        tradeRulesRdTextFile = ioTextFiles.new ioRdTextFiles(fileNameIn + ext, false);
        Term actTerm = null;
        fileBuff = fillBuf(tradeRulesRdTextFile);
		//new ma type simple, weighted etc
		setMaType(fileBuff.get(lcnt++));
        //read term operaton (open/close) 
        termOperation = fileBuff.get(lcnt++);
        if (termOperation.equals("Open")) {
            activeTerms = openTerms;
        } else if (termOperation.equals("Close")) {
            activeTerms = closeTerms;
        }
		
        //new equation trigger with bars...
        setEquationTrigger(termOperation, fileBuff.get(lcnt++));
        setEquationBars(termOperation, Integer.valueOf(fileBuff.get(lcnt++)));		
        //openCloseComboBox.setSelectedItem(userSelectedTermOperation);
        while (lcnt < fileBuff.size()){
            //first index
            index = Integer.valueOf(fileBuff.get(lcnt++));
            //then term
            tradeTerm = tradeTerm.findTerm(fileBuff.get(lcnt++));
            //actTerm = ttable[tradeTerm.ordinal()];   
            actTerm = getSelectedTerm(tradeTerm);
            actTerm.setTerm(tradeTerm);
            //then description
            actTerm.setDescription(fileBuff.get(lcnt++));
            //then AndOr
            actTerm.setAndOr(Integer.valueOf(fileBuff.get(lcnt++)));
            //then AndOrSelItem
            actTerm.setAndOrSelItem((Object) fileBuff.get(lcnt++));
            //then selTerm
            actTerm.setTermSelItem((Object) fileBuff.get(lcnt++));
            //then movingAverage
            actTerm.setMa(Integer.valueOf(fileBuff.get(lcnt++)));
            switch (actTerm.getTerm()) {
                case oMovingAverage:
                    MovingAverage maTerm = (MovingAverage) actTerm;
                    maTerm.setPercentValue(Double.valueOf(fileBuff.get(lcnt++)));
					maTerm.setCount(Integer.valueOf(fileBuff.get(lcnt++)));
					maTerm.setCurrentPriceWithinPercentSelected(Boolean.valueOf(fileBuff.get(lcnt++)));
					maTerm.setConsecutiveTimesAboveSelected(Boolean.valueOf(fileBuff.get(lcnt++)));
					maTerm.setConsecutiveTimesBelowSelected(Boolean.valueOf(fileBuff.get(lcnt++)));					
                    break;
                case oMovingAveragePlus:
                    MovingAveragePlus maTermPlus = (MovingAveragePlus) actTerm;
                    maTermPlus.setPercentGtValue(Double.valueOf(fileBuff.get(lcnt++)));
                    maTermPlus.setPercentLtValue(Double.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oHistorical:
                    Historical histTerm = (Historical) actTerm;
                    histTerm.setPercentValue(Double.valueOf(fileBuff.get(lcnt++)));
                    histTerm.setDaysHistory(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oRSIGT:
                    RsiGT rsiGtTerm = (RsiGT) actTerm;
                    rsiGtTerm.setGtRsi(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oRSILT:
                    RsiLT rsiLtTerm = (RsiLT) actTerm;
                    rsiLtTerm.setLtRsi(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oSlopeNeg:
                    SlopeNeg snegTerm = (SlopeNeg) actTerm;
                    snegTerm.setDaysHistory(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oSlopePos:
                    SlopePos sposTerm = (SlopePos) actTerm;
                    sposTerm.setDaysHistory(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oVolume:
                    Volume volTerm = (Volume) actTerm;
                    volTerm.setVolumeVal(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oSlopeFilter:
                    SlopeFilter sSlopeFilterTerm = (SlopeFilter) actTerm;
                    sSlopeFilterTerm.setSlopeFilter(Double.valueOf(fileBuff.get(lcnt++)));
                    sSlopeFilterTerm.setDaysHistory(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;
                case oCompareMAs:
                    Compare2MAs compareTerm = (Compare2MAs) actTerm;
                    compareTerm.setMaB(Integer.valueOf(fileBuff.get(lcnt++)));
                    compareTerm.setOperator(fileBuff.get(lcnt++));
                    break;
				case oCorrectionAve:
                    CorrectionAve aveCorrectionTerm = (CorrectionAve) actTerm;
                    aveCorrectionTerm.setCorrectionWithInPercent(Integer.valueOf(fileBuff.get(lcnt++)));
                    break;	
                default:;
            }
            //now add item to activeTerms
            activeTerms.add(index, actTerm);
            actTerm = null;
            //read term operaton (open/close) if there is one..
            tmpStr = ((lcnt < fileBuff.size()) ? fileBuff.get(lcnt++) : "");
            if (tmpStr.equals("Open")) {
                activeTerms = openTerms;
                termOperation = tmpStr;
                //new equation trigger with bars...
                setEquationTrigger(termOperation, fileBuff.get(lcnt++));
                setEquationBars(termOperation, Integer.valueOf(fileBuff.get(lcnt++)));
            } else if (tmpStr.equals("Close")) {
                activeTerms = closeTerms;
                termOperation = tmpStr;
                setEquationTrigger(termOperation, fileBuff.get(lcnt++));
                setEquationBars(termOperation, Integer.valueOf(fileBuff.get(lcnt++)));
            } else if (!tmpStr.equals("")){
				lcnt--;
            } else {                				
			}  
        }
        tradeRulesRdTextFile.closeRd();
    }
    private boolean genThresholdTradeLog(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        /*    
        go back backtTest period(ie 1yr)
        day1:
	begin:
		HTH == highThreshHold 
		LTH == lowThreshHold
		TH% == thresholdPercent

	Day1:
		highClose = lowClose = previousHigh = currentClose;
		currentClose = histData[dayOne];
		HTH = currentClose * (1 + TH%);
		LTH = currentClose * (1 - TH%);
	loop through timePeriod:
	repeat	

		currentClose = histData[day];

		if currentClose  > HTH begin
                        correction = highClose - lowClose;                        
                        generateEvent = true;
			highClose = currentClose;
			LTH = highClose * (1 - TH%);
                        
		end;

		if (currentClose  <  LTH)  begin
                        correction = lowClose - highClose;
                        generateEvent(Low -> High);
			lowClose = currentClose;
			HTH = lowClose * (1 + TH%);                        
		end;
                if (generateEvent == true) begin
                    correction = highClose - lowClose;
                    generateevent = false;
                end;
	until done;

 
         */
        int idx;
        boolean retVal = true;
        OccurenceData actTradeData;
        termTradesIn.occurences = new ArrayList<>();        
        double currentClose = 0.0;
        double correction = 0.0;
        final double THPercent = .05;       
        boolean genEvent = false;
        boolean blockEvent = false;
        double highWater;
        double lowWater;
        double prevLow;
        double prevHigh;
        int highCount;
        int lowCount;
        
        int btSize = tradeLogPeriod.backTestSizeInBars;
        //back in time X 2 of backTestSize..
        int histSize = btSize * 2;
        if (histSize > histDataList.size()) {
            System.out.println("\ngenPivotalTradeLog.backTestSize!!");
            retVal = false;
        } else {
            currentClose = histDataList.get(btSize).getClosePrice();
            prevHigh = prevLow = currentClose;
            highWater = currentClose ;//* (1 + THPercent);
            lowWater = currentClose ;//* (1 - THPercent);
            highCount = lowCount = 0;
            for (idx = (btSize - 1); idx >= 0; idx--) {
                //ease of access..
                histData = histDataList.get(idx);
                currentClose = histData.getClosePrice();
                if (currentClose > highWater) {
                    highCount++;
                    if(highCount == 1){
                        correction = prevHigh - lowWater;
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histData.getDate();
                        actTradeData.termOutputList.add("Stock Low to High: ");
                        actTradeData.termOutputList.add("currentClose Price: " + currentClose);
                        actTradeData.termOutputList.add("HighPrice: " + prevHigh);
                        actTradeData.termOutputList.add("LowPrice: " + lowWater);
                        actTradeData.termOutputList.add("Gain: " + myUtils.roundMe(correction, 2));
                        actTradeData.termOutputList.add("Gain %: " + myUtils.roundMe((correction / prevHigh * 100.0), 2));
                        termTradesIn.occurences.add(actTradeData);
                        prevLow = lowWater;
                        prevHigh = highWater;
                        lowCount = 0;
                    }
                    highWater = currentClose;
                    lowWater = highWater * (1 - THPercent);
                }                
                else if (currentClose < lowWater) {
                    lowCount++;
                    if(lowCount == 1){
                        correction = highWater - prevLow;
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histData.getDate();
                        actTradeData.termOutputList.add("Stock High to Low: ");
                        actTradeData.termOutputList.add("currentClose Price: " + currentClose);
                        actTradeData.termOutputList.add("HighPrice: " + highWater);
                        actTradeData.termOutputList.add("LowPrice: " + prevLow);
                        actTradeData.termOutputList.add("Correction: " + myUtils.roundMe(correction, 2));
                        actTradeData.termOutputList.add("Corrected %: " + myUtils.roundMe((correction / prevLow * 100.0), 2));
                        termTradesIn.occurences.add(actTradeData);
                        prevHigh = highWater;
                        prevLow = lowWater;
                        highCount = 0;
                    }
                    lowWater = currentClose;
                    highWater = lowWater * (1 + THPercent);                   
                }                
            }
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
    private boolean genHiLowTradeLog(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        /*    
        go back backtTest period(ie 1yr)
        day1:
	highClose = lowClose = previousHigh = currentClose;
	loop through timePeriod:
		if current close > highClose then 
			previousHigh = highClose;
			highClose = currentClose;
			highCount++;
			lowClose = highClose;
			if lowCount > 0 then
				correction = previouseHigh - lowClose;
				generateOccurence;	
				lowCount = 0;
				highCount = 0;
		if current close < lowClose then
			lowClose = currentClose;
			lowCount++;
 	until done;   
         */
        int idx;
        boolean retVal = true;
        OccurenceData actTradeData;
        termTradesIn.occurences = new ArrayList<>();
        double highClose = 0.0;
        double lowClose = 0.0;
        int highCount = 0;
        int lowCount = 0;
        int daysInCorrection = 0;
        double currentClose = 0.0;
        double correction = 0.0;
        int btSize = tradeLogPeriod.backTestSizeInBars;
        //back in time X 2 of backTestSize..
        int histSize = btSize * 2;
        if (histSize > histDataList.size()) {
            System.out.println("\ngenPivotalTradeLog.backTestSize!!");
            retVal = false;
        } else {
            highClose = lowClose = currentClose = histDataList.get(btSize).getClosePrice();
            for (idx = btSize; idx >= 0; idx--) {
                //ease of access..
                histData = histDataList.get(idx);
                currentClose = histData.getClosePrice();
                if (polarity == true) {
                    //long
                    if (currentClose > highClose) {
                        // new high..                    
                        highCount++;
                        if (lowCount > 0) {
                            //means there was a correction from high..
                            correction = highClose - lowClose;
                            actTradeData = new OccurenceData();
                            actTradeData.action = operationIn;
                            actTradeData.occurenceDate = histData.getDate();
                            actTradeData.termOutputList.add("Stock moving higher: ");
                            actTradeData.termOutputList.add("currentClose Price: " + currentClose);
                            actTradeData.termOutputList.add("HighPrice: " + highClose);
                            actTradeData.termOutputList.add("LowPrice: " + lowClose);
                            actTradeData.termOutputList.add("LowerLowDays: " + lowCount);
                            actTradeData.termOutputList.add("DaysInCorrection: " + daysInCorrection);
                            actTradeData.termOutputList.add("Correction: " + myUtils.roundMe(correction, 2));
                            actTradeData.termOutputList.add("Corrected %: " + myUtils.roundMe((correction / highClose * 100.0), 2));
                            termTradesIn.occurences.add(actTradeData);
                            lowCount = highCount = 0;
                        }
                        highClose = lowClose = currentClose;
                        daysInCorrection = 0;
                    } else if (currentClose < lowClose) {
                        lowClose = currentClose;
                        lowCount++;
                        daysInCorrection++;
                    } else {
                        daysInCorrection++;
                    }

                } else {
                    //short
                    if (currentClose < lowClose) {
                        // new low..                    
                        lowCount++;
                        if (highCount > 0) {
                            //means there was a correction from low..
                            correction = highClose - lowClose;
                            actTradeData = new OccurenceData();
                            actTradeData.action = operationIn;
                            actTradeData.occurenceDate = histData.getDate();
                            actTradeData.termOutputList.add("Stock moving lower: ");
                            actTradeData.termOutputList.add("currentClose Price: " + currentClose);
                            actTradeData.termOutputList.add("HighPrice: " + highClose);
                            actTradeData.termOutputList.add("LowPrice: " + lowClose);
                            actTradeData.termOutputList.add("HigherHighDays: " + highCount);
                            actTradeData.termOutputList.add("DaysInCorrection: " + daysInCorrection);
                            actTradeData.termOutputList.add("Correction: " + myUtils.roundMe(correction, 2));
                            actTradeData.termOutputList.add("Corrected %: " + myUtils.roundMe((correction / lowClose * 100.0), 2));
                            termTradesIn.occurences.add(actTradeData);
                            lowCount = highCount = 0;
                        }
                        highClose = lowClose = currentClose;
                        daysInCorrection = 0;
                    } else if (currentClose > highClose) {
                        highClose = currentClose;
                        highCount++;
                        daysInCorrection++;
                    } else {
                        daysInCorrection++;
                    }
                }
                if((idx == 160) && (polarity == false)){
                    System.out.println("\n...");
                }
            }
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private boolean genHiLowAverageTradeLog(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        /*    
         go back backtTest period(ie 1yr)
        from there, go back 1yr and average all hi/low data by:
            - add up hiPlghs for year and divide result by 252
            - add up lows for year and divide result by 252.
        these averages are then used as triggers against current stock price.
        If current price is greater than aveHigh then trigger occurence.
        If current price is lower than aveLow then trigger occurence.
        Which of above is done depends on polarity input.
        
        oldestDate   --------> newerDate  ------> todaysDate
        [0] []  []  []  []  []  []  [] ....[1yr]  [1yr+1]  []  []  []  []  []  [] .... [2yr]  [2yr+1]  []  []  [] [] .... [today]
                                         1yrTotAve 1yrTotAve    
         */
        int idx;
        boolean retVal = true;
        OccurenceData actTradeData;
        termTradesIn.occurences = new ArrayList<>(); 
        int btSize = tradeLogPeriod.backTestSizeInBars;
        //back in time X 2 of backTestSize..
        int histSize = btSize * 2;
        if (histSize > histDataList.size()) {
            System.out.println("\ngenPivotalTradeLog.backTestSize!!");
            retVal = false;
        } else {                                
            for (idx = btSize - 1; idx >= 0; idx--) {
                //ease of access..
                histData = histDataList.get(idx);
                if ((polarity == true) && (histData.getClosePrice() > histData.getYearsAveHigh())){
                    // close price today is greater than average High, so generate log..
                    actTradeData = new OccurenceData();
                    actTradeData.action = operationIn;
                    actTradeData.occurenceDate = histData.getDate();
                    actTradeData.closePrice = histData.getClosePrice();
                    actTradeData.actualSlope = 0;
                    actTradeData.actualYearAveHi = histData.getYearsAveHigh();
                    termTradesIn.occurences.add(actTradeData);   
                } else if ((polarity == false) && (histData.getClosePrice() < histData.getYearsAveLo())){
                    // close price today is less than average Low,so generate log..
                    actTradeData = new OccurenceData();
                    actTradeData.action = operationIn;
                    actTradeData.occurenceDate = histData.getDate();
                    actTradeData.closePrice = histData.getClosePrice();
                    actTradeData.actualSlope = 0;
                    actTradeData.actualYearAveLo = histData.getYearsAveLo();;
                    termTradesIn.occurences.add(actTradeData);   
                }                
            }

        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
   
    private boolean genPivotalTradeLog(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         This term relys on a MA value..so we need to find where this moving average calculation begins back in time i.e. 10dayMA needs 
         10 day history to calculate, 20 day needs 20 days  etc.
         */
        int idx;
        boolean retVal = true;
        //need two points to calculate RISE/RUN...            
        double riseOverRun;
        boolean slopeCurr;
        boolean slopePrev = false;
        int staValidData = 0;
        OccurenceData actTradeData;
        boolean pivotTriggered = false;
        termTradesIn.occurences = new ArrayList<>();
        final int CONFIRM_CNT = 1;
        int persistCnt = 0;        
        int btSize = tradeLogPeriod.backTestSizeInBars;        
        if (btSize > histDataList.size()) {
            System.out.println("\ngenPivotalTradeLog.backTestSize!!");
            retVal = false;
        } else {
            if (((staValidData = findStartOfValidData(maIn)) != -1) && (staValidData > btSize)) {
                System.out.println("\nstart of valid data is at : " + staValidData);
                slopePrev = slopeCurr = (findSlope(btSize,maIn) > 0.0);
                //start with oldest in history of valid MA minus RUN_DAYS                                       
                for (idx = btSize-1; idx >= 0; idx--) {                    
                    riseOverRun = findSlope(idx, maIn);                    
                    if (riseOverRun == 0.0) {
                        //if slope == zero, just continue to next..
                        continue;
                    }
                    slopeCurr = (riseOverRun > 0.0);
                    if ((slopeCurr != slopePrev) && (pivotTriggered == false)) {
                        //change in slope occurred..flag pivot occurred..now need to confirm
                        pivotTriggered = true;
                        persistCnt = 0;
                    } else if ((pivotTriggered == true) && (slopeCurr != slopePrev)) {
                        if (++persistCnt >= CONFIRM_CNT) {
                            if (slopeCurr == polarity) {
                                //change in slope occurred and slope is confirmed so generate log..
                                actTradeData = new OccurenceData();
                                actTradeData.action = operationIn;
                                actTradeData.occurenceDate = histDataList.get(idx).getDate();
                                actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                                actTradeData.actualSlope = riseOverRun;
                                actTradeData.actualMaValue = histDataList.get(idx).getMa(maIn);
                                actTradeData.termOutputList.add("Actual Slope: " + riseOverRun);                               
                                actTradeData.termOutputList.add("Actual MaVal: " + actTradeData.actualMaValue);
                                actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;                                
                                termTradesIn.occurences.add(actTradeData);
                                slopePrev = slopeCurr;
                                pivotTriggered = false;
                                persistCnt = 0;

                            }else{
                                slopePrev = slopeCurr;
                                pivotTriggered = false;
                                persistCnt = 0;
                            }
                        } else {

                        }
                    } else {
                        //slopePrev = slopeCurr;                        
                    }
                }

            } else {
                retVal = false;
                System.out.println("\ngenPivotalTradeLog.validData problem!!");
            }
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    public class PivotalHL extends Term {
        /*
         term finds point where the selected MA transitions from Positive slope to Negative Slope.
         usedMA is the selected moving average to use.
         */
        String description = "";
        TradeTerms term = TradeTerms.oPivotalHL;
        String selMA = "";
        String prompt = "Enter MA To Determine Pivot:"; 
        private int movingAverage = 0;
        public void getUserInput() {
            boolean result = false;
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
            }
            description = selMA + " Pivots + to - slope (/\\)";
        }

        public PivotalHL() {
            //super.daTerm = this;            
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public TradeTerms getTerm() {
            return term;
        }
        @Override
        public void setDescription(String sin) {
            description = sin;
        }
        @Override
        public int getMa(){
            return movingAverage;
        }
        @Override
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth, for ticker: " + tickerin);
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;  
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genPivotalTradeLog(false, termTrades, operation, maIn);
            //genHiLowTradeLog(positive, termTrades, operation, maIn);
            //genThresholdTradeLog(positive, termTrades, operation, maIn);
            return termTrades;
        }
    }

    private class PivotalLH extends Term {
        /*
         term finds point where the selected MA transitions from Negative slope to Positive Slope.
         usedMA is the selected moving average to use.
         */
        TradeTerms term = TradeTerms.oPivotalLH;
        String selMA = "";
        private int movingAverage = 0;
        String description = "";
        String prompt = "Enter MA To Determine Pivot:";
        public void getUserInput() {
            boolean result = false;
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
            }
            description = selMA + " Pivots - to + slope (\\/)";
        }

        public PivotalLH() {
            //super.daTerm = this;              
        }

        @Override
        public String getDescription() {

            return description;
        }

        @Override
        public TradeTerms getTerm() {
            return term;
        }

        @Override
        public void setDescription(String sin) {
            description = sin;
        }
        @Override
        public int getMa(){
            return movingAverage;
        }
        @Override
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genPivotalTradeLog(true, termTrades, operation, maIn);
            //genHiLowTradeLog(positive, termTrades, operation, maIn);
            //genThresholdTradeLog(positive, termTrades, operation, maIn);
            return termTrades;
        }
    }
    private boolean genSWMomentumTradeLog1(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        //int staValidDataMa = 0;         
        OccurenceData actOccurenceData;
        boolean retVal = true;
        termTradesIn.occurences = new ArrayList<>();
        int btSize = (tradeLogPeriod.backTestSizeInBars);
        int idx;
		int staValidData = 0;
        double firstDerivative;
        double secondDerivative;		
        if (btSize > histDataList.size()) {
            System.out.println("\ngenSWMomentumTradeLog1.backTestSize!!");
            retVal = false;
        } else if (((staValidData = findStartOfValidData(maIn)) != -1) && (staValidData > btSize)) {
            System.out.println("\nstart of valid data maA is at : " + staValidData);
            //first set slope values for all history (1st derivative approximation)
            for (idx = btSize + maIn; idx >= 0; idx--) {
                firstDerivative = findSlope(idx, maIn);
                histDataList.get(idx).set1stDerivative(firstDerivative);
            }
            //now do slope of slopes for all history (2nd derivative approximation)
            for (idx = btSize; idx >= 0; idx--) {
                secondDerivative = findSlopesSlope(idx, maIn);
                histDataList.get(idx).set2ndDerivative(secondDerivative);
            }
            /*
			now generate occurences if :
			polarity is true, means looking for going low to going high transition. so the 2nd dirivative must be a negative number meaning it
			is definely going downward in slope and the first dirivative (the most recent action) is greater than 0, meaning it's positive sloping and
			it must also be greater in value than the 2nd derivative. If all this is true, generate a positive occurence showing Strength.
			polarity is false, means looking for going high to low transition. so the 2 dirivative must be positive sloping and 1st dirivative must be 
			negative sloping but also must be less than the second dirivative. If all this is true, generate an negative occurence showing weakness.
             */
            for (idx = btSize; idx >= 0; idx--) {
                if ((((polarity == true) && (histDataList.get(idx).get2ndDerivative() < 0) && (histDataList.get(idx).get1stDerivative() > 0) && (histDataList.get(idx).get1stDerivative() > histDataList.get(idx).get2ndDerivative()))) || 
                   (((polarity == false) && (histDataList.get(idx).get2ndDerivative() > 0) && (histDataList.get(idx).get1stDerivative() < 0) && (histDataList.get(idx).get1stDerivative() < histDataList.get(idx).get2ndDerivative())))){
                    //generate event cuz polarity is true and current slope is greater than the average slope.
                    actOccurenceData = new OccurenceData();
                    actOccurenceData.action = operationIn;
                    actOccurenceData.occurenceDate = histDataList.get(idx).getDate();
                    actOccurenceData.closePrice = histDataList.get(idx).getClosePrice();
                    actOccurenceData.actualSlope = histDataList.get(idx).get1stDerivative();
                    actOccurenceData.actualAverageSlope = histDataList.get(idx).get2ndDerivative();
					actOccurenceData.termOutputList.add("1stDirivative " + actOccurenceData.actualSlope);
					actOccurenceData.termOutputList.add("2ndDirivative " + actOccurenceData.actualAverageSlope);
					actOccurenceData.occurenceDescription = termTradesIn.termHead.termDescription;
                    termTradesIn.occurences.add(actOccurenceData);
                }
            }
        } else {
            retVal = false;
            System.out.println("\ngenSWMomentumTradeLog1.validData problem!!");
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
    private boolean genSWMomentumTradeLog(boolean polarity, TermOccurences termTradesIn, String operationIn, int maIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         This term relys on a MA value..so we need to find where this moving average calculation begins back in time i.e. 10dayMA needs 
         10 day history to calculate, 20 day needs 20 days  etc.
         */
        int idx;
        boolean retVal = true;
        boolean found = false;
        ibApi.historicalData.data d;
        int staValidDataIdx;
        //need two points to calculate RISE/RUN...            
        double pA;
        double pB;
        double riseOverRun;
        boolean slopeCurr;
        boolean slopePrev = false;
        int numOfSlopes = 0;
        OccurenceData actTradeData;
        termTradesIn.occurences = new ArrayList<>();        
        //look for beging of real Moving Average data..
        for (staValidDataIdx = (histDataList.size() - 1); (staValidDataIdx >= 0) && !found; staValidDataIdx--) {
            if (histDataList.get(staValidDataIdx).getMa(maIn) > 0.0) {
                found = true;
            }
        }
        System.out.println("\nStart of valid MA data is " + staValidDataIdx + " index.");
        if (found == false) {
            retVal = false;
        } else {
            //start with oldest in history of valid MA minus RUN_DAYS
            for (idx = (staValidDataIdx - RUN_IN_DAYS); idx >= 0; idx--) {
                //get pointA then pointB..
                pB = histDataList.get(idx + RUN_IN_DAYS).getMa(maIn);
                pA = histDataList.get(idx).getMa(maIn);
                //calculate run/rise ratio...
                riseOverRun = ((pA - pB) / RUN_IN_DAYS);
                if (riseOverRun == 0.0) {
                    //if slope == zero, just continue to next..
                    continue;
                }
                slopeCurr = (riseOverRun > 0.0);
                if (++numOfSlopes > 1) {
                    if ((slopeCurr != slopePrev) && (slopeCurr == polarity)) {
                        //change in slope occurred..now negative slope..so log it..
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        termTradesIn.occurences.add(actTradeData);
                        slopePrev = slopeCurr;
                    } else {
                        slopePrev = slopeCurr;
                    }
                } else {
                    slopePrev = slopeCurr;
                }
            }
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private class Strength extends Term {
        /*
         term used to show when the selected slope moving to higher values (getting more postive), whether the values
         are positive or negative, so -.45 to -.40 would be strength.
         i.e strength.
         usedMA is the selected moving average to use.
         */

        TradeTerms term = TradeTerms.oStrength;
        String selMA = "";
        private int movingAverage = 0;
        String description = "";
        String prompt = "Enter MA In Determining Strength:";
        boolean strenth = true;
        public void getUserInput() {
            boolean result = false;
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
            }
            description = selMA + " Gaining Strength";
        }

        public Strength() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genSWMomentumTradeLog1(strenth, termTrades, operation, maIn);
            return termTrades;
        }
    }

    private class Weakness extends Term {
        /*
         term used to show when the selected slope is moving down from pos to less postive values (getting more negative)
         i.e weakness, so .78 to .70 would be weakness.
         usedMA is the selected moving average to use.
         */

        TradeTerms term = TradeTerms.oWeakness;
        String selMA = "";
        private int movingAverage = 0;
        String description = "";
        String prompt = "Enter MA In Determining Weakness:";
        boolean weakness = !true;
        public void getUserInput() {
            boolean result = false;
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
            }
            description = selMA + " Strength is Weakening";
        }

        public Weakness() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genSWMomentumTradeLog1(weakness, termTrades, operation, maIn);
            return termTrades;
        }
    }
	
	private boolean genMovingAverageTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, int countIn, boolean aboveIn) {
		/*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         go back in time backTestSize.
		 look for consecutive times (input countIn) current price is either above OR below movAveIn. 
		 If aboveIn is true, then look for consecutive times above ma,
		 otherwise look for consecutive times below ma.
         trigger when condition is true.
         triggers will be logged every time the these consecutive events occur. 
		 */
		int idx;
		boolean retVal = true;
		ibApi.historicalData.data d;
		OccurenceData actTradeData;
		int staValidData = 0;
		int actCount = 0;
		termTradesIn.occurences = new ArrayList<>();
		int btSize = (tradeLogPeriod.backTestSizeInBars - 1);
		if (btSize > histDataList.size()) {
			System.out.println("\ngenMovingAverageTradeLog.backTestSize!!");
			retVal = false;
		} else {
			if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
				System.out.println("\nstart of valud data is at : " + staValidData);
				for (idx = btSize; idx >= 0; idx--) {
					if ((histDataList.get(idx).getMa(movAveIn)) == 0.0) {
						continue;
					}
					if (aboveIn == true) {
						//look for consecutive aboves
						if (((histDataList.get(idx).getClosePrice() > histDataList.get(idx).getMa(movAveIn)) && testIt == false)
							|| ((testIt == true) && (histDataList.get(idx).getClosePrice() > histDataList.get(idx).getMa(movAveIn))
							&& (histDataList.get(idx).getOpenPrice() > histDataList.get(idx).getMa(movAveIn)))) {
							actCount++;
							if (actCount >= countIn) {
								actTradeData = new OccurenceData();
								actTradeData.action = operationIn;
								actTradeData.occurenceDate = histDataList.get(idx).getDate();
								actTradeData.closePrice = histDataList.get(idx).getClosePrice();
								actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
								termTradesIn.occurences.add(actTradeData);
								actCount = 0;
							}
						} else {
							actCount = 0;
						}
					} else {
						//look for consecutive belows
						if (histDataList.get(idx).getClosePrice() < histDataList.get(idx).getMa(movAveIn)) {
							actCount++;
							if (actCount >= countIn) {
								actTradeData = new OccurenceData();
								actTradeData.action = operationIn;
								actTradeData.occurenceDate = histDataList.get(idx).getDate();
								actTradeData.closePrice = histDataList.get(idx).getClosePrice();
								actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
								termTradesIn.occurences.add(actTradeData);
								actCount = 0;
							}
						} else {
							actCount = 0;
						}
					}
				}
			} else {
				retVal = false;
				System.out.println("\ngenMovingAverageTradeLog.valudData problem!!");
			}

		}
		termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
		return retVal;
	}

	private boolean genMovingAverageTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, double percentIn) {
	/*
		explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
		go back in time backTestSize.
		compare closing price to moving averate in. If within percentIn, then trigger the action,
		otherwise go to next date and repeat.
		triggers will be triggered every time the closing price of the day is within the percentIn value.
	*/
		int idx;
		boolean retVal = true;
		ibApi.historicalData.data d;
		OccurenceData actTradeData;
		int staValidData = 0;
		boolean triggerIt = false;
		double maValue = 0.0;
		termTradesIn.occurences = new ArrayList<>();
		int btSize = tradeLogPeriod.backTestSizeInBars;
		if (btSize > histDataList.size()) {
			System.out.println("\ngenMovingAverageTradeLog.backTestSize!!");
			retVal = false;
		} else {
			if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
				System.out.println("\nstart of valud data is at : " + staValidData);
				for (idx = btSize; idx >= 0; idx--) {
					maValue = histDataList.get(idx).getPercentWithInActMa(movAveIn);
					triggerIt = ((percentIn > 0) ? (((maValue <= (percentIn / 100.0)) && (maValue >= 0))) : (((maValue <= (percentIn / 100.0)) && (maValue <= 0))));
					if (triggerIt == true) {
						actTradeData = new OccurenceData();
						actTradeData.action = operationIn;
						actTradeData.occurenceDate = histDataList.get(idx).getDate();
						actTradeData.closePrice = histDataList.get(idx).getClosePrice();
						maValue = myUtils.roundMe(maValue * 100.0, 2);
						actTradeData.actualPercent = maValue;
						actTradeData.termOutputList.add("Percent Within " + movAveIn + "Ma " + maValue + "%");
						actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
						termTradesIn.occurences.add(actTradeData);
					}
				}
			} else {
				retVal = false;
				System.out.println("\ngenMovingAverageTradeLog.valudData problem!!");
			}

		}
		termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
		return retVal;
	}

	boolean isValidDouble(double din) {
		boolean retVal;
        return((din != Double.NEGATIVE_INFINITY) && (din != Double.POSITIVE_INFINITY));
    }
    private boolean genMovingAveragePlusTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, double percentGtIn, double percentLtIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         go back in time backTestSize.
         compare closing price to moving averate in. If within percentIn, then trigger the action,
         otherwise go to next date and repeat.
         triggers will be triggered every time the closing price of the day is within the percentIn value.
         
         */
        int idx;
        boolean retVal = true;
        ibApi.historicalData.data d;
        OccurenceData actTradeData;
        int staValidData = 0;
        boolean triggerIt = false;
        double maValue = 0.0;
        percentGtIn /= 100.0;
        percentLtIn /= 100.0;
        termTradesIn.occurences = new ArrayList<>();
        int btSize = tradeLogPeriod.backTestSizeInBars;
        //leave if both not valid.
        if(!isValidDouble(percentLtIn) && !isValidDouble(percentGtIn)){
            return false;
        }
        if (btSize > histDataList.size()) {
            System.out.println("\ngenMovingAverageTradeLog.backTestSize!!");
            retVal = false;
        } else {
            if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
                System.out.println("\nstart of valud data is at : " + staValidData);
                for (idx = btSize; idx >= 0; idx--) {
                    maValue = histDataList.get(idx).getPercentWithInActMa(movAveIn);                      
                    if(((isValidDouble(percentLtIn)) && (maValue < (percentLtIn))) && (isValidDouble(percentGtIn) && (maValue > percentGtIn))){
                        //both have valid values and are true logically..
                        triggerIt = true;
                    }else if ((!isValidDouble(percentLtIn)) && (maValue > percentGtIn)){
                        //percentLtIn is not valid, so just check Gt case and its true..
                        triggerIt = true;
                    }else if ((!isValidDouble(percentGtIn)) && (maValue < percentLtIn)){
                        //percentGtIn is not valid , so just check Lt case and its true..
                        triggerIt = true;
                    }
                    if (triggerIt == true) {
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        maValue = myUtils.roundMe(maValue * 100.0, 2);
                        actTradeData.actualPercent= maValue;
                        actTradeData.termOutputList.add("Percent Within " + movAveIn + "Ma " + maValue + "%");
                        actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
                        termTradesIn.occurences.add(actTradeData);  
                        triggerIt = false;
                    }
                }
            } else {
                retVal = false;
                System.out.println("\ngenMovingAverageTradeLog.valudData problem!!");
            }

        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
    private class MovingAverage extends Term {
        /*
         term used to show when the price of a stock is currently a selPercent above or below the selected MA.        
         selMA is the selected moving average to use.
         selPercent is the percent above or below selected MA.
         selAboveMa is true for above ma, false for below ma.
         Term is true when current price is above selMa within 2%, assuming selAboveMa = true, else false.
         Term is true when current price is below selMa with 2%, assuming selAboveMa = false, else false.
         */

        TradeTerms term = TradeTerms.oMovingAverage;
        String selMA = "";
        private int movingAverage = 0;
        double selPercent = 0.0; 
		int selCount = 0;
        boolean selAboveMa = true;
        String description = "";
        String prompt = "Enter % Current Price is above MA:";
		GetUserInputMaDialog getUserInputMaDialog = null;
		boolean currentPriceWithinPercentSelected = false;
		boolean consecutiveTimesAbove = false;
		boolean consecutiveTimesBelow = false;
        public void getUserInputSave() {
            boolean result = false;
            JTextField percentField = new JTextField(5);

            percentField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });

            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter %:"));
            myPanel.add(percentField);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selPercent = Double.valueOf(percentField.getText());
                selMA = cb.getSelectedItem().toString();
                movingAverage = movingAves.getMaIntegerValue(cb.getSelectedIndex());
                description = "Current Price within " + selPercent + "% of " + selMA;
      }
	    }

	    @Override
	    public void getUserInput() {
		    //getUserInputSave();
		    getUserInputMaDialog = new GetUserInputMaDialog(new javax.swing.JFrame(), true);
		    getUserInputMaDialog.setSupportedMa(movingAves.getMaList());
		    getUserInputMaDialog.setVisible(true);
		    movingAverage = movingAves.getMaIntegerValue(getUserInputMaDialog.getSelectedMaIndex());
		    selMA = getUserInputMaDialog.getSelectedMa();
			userSelectedMaType = getUserInputMaDialog.getSelectedMaType();
		    if (getUserInputMaDialog.getCurrentPriceWithinPercentSelected() == true) {
			    selPercent = getUserInputMaDialog.getPercentValue();
			    description = "Current Price within " + selPercent + "% of " + selMA;
			    currentPriceWithinPercentSelected = true;
		    } else if (getUserInputMaDialog.getConsecutiveTimesAboveSelected() == true) {
			    selCount = getUserInputMaDialog.getCountValue();
			    description = selCount + " Consecutive Times ABOVE " + selMA;
			    consecutiveTimesAbove = true;
		    } else if (getUserInputMaDialog.getConsecutiveTimesBelowSelected() == true) {
			    selCount = getUserInputMaDialog.getCountValue();
			    description = selCount + " Consecutive Times BELOW " + selMA;
			    consecutiveTimesBelow = true;
		    }
	    }

	    public MovingAverage() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setPercentValue(double din) {
            selPercent = din;
        }

        public double getPercentValue() {
            return selPercent;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
			movingAverage = ma;
		    selMA = Integer.toString(movingAverage);
	    }

	    public boolean isConsecutiveTimesAboveSelected() {
		    return consecutiveTimesAbove;
	    }

	    public void setConsecutiveTimesAboveSelected(boolean sin) {
		    consecutiveTimesAbove = sin;
	    }

	    public boolean isConsecutiveTimesBelowSelected() {
		    return consecutiveTimesBelow;
	    }

	    public void setConsecutiveTimesBelowSelected(boolean sin) {
		    consecutiveTimesBelow = sin;
	    }

	    public boolean isCurrentPriceWithinPercentSelected() {
		    return currentPriceWithinPercentSelected;
	    }

	    public void setCurrentPriceWithinPercentSelected(boolean sin) {
		    currentPriceWithinPercentSelected = sin;
	    }

	    public int getCount() {
		    return selCount;
	    }

	    public void setCount(int cin) {
		    selCount = cin;
	    }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn) {
		    termTrades = new TermOccurences();
		    System.out.println("\nchecking term: " + this.description + " for truth.");
		    termTrades.termHead.termDescription = description;
		    termTrades.termHead.term = term;
		    termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
		    if (currentPriceWithinPercentSelected == true) {
			    termTrades.termHead.termRanFine = genMovingAverageTradeLog(termTrades, operation, movingAverage, selPercent);
		    } else if ((consecutiveTimesAbove == true) || (consecutiveTimesBelow == true)) {
			    termTrades.termHead.termRanFine = genMovingAverageTradeLog(termTrades, operation, movingAverage, selCount, consecutiveTimesAbove);
		    }
		    return termTrades;
	    }
    }
    private class MovingAveragePlus extends Term {
        /*
         term used to show when the price of a stock is currently a selPercent above or below the selected MA.        
         selMA is the selected moving average to use.
         selPercent is the percent above or below selected MA.
         selAboveMa is true for above ma, false for below ma.
         Term is true when current price is above selMa within 2%, assuming selAboveMa = true, else false.
         Term is true when current price is below selMa with 2%, assuming selAboveMa = false, else false.
         */

        TradeTerms term = TradeTerms.oMovingAveragePlus;
        String selMA = "";
        private int movingAverage = 0;
        double selPercentGt = 0.0; 
        double selPercentLt = 0.0; 
        boolean selAboveMa = true;
        String description = "";
        String prompt = "Enter Range Above/Below (%) MA:";

        public void getUserInput() {
            boolean result = false;
            JTextField percentGtField = new JTextField(5);
            JTextField percentLtField = new JTextField(5);
            percentGtField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            percentLtField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter > :"));
            myPanel.add(percentGtField);
            myPanel.add(new JLabel("Enter < :"));
            myPanel.add(percentLtField);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selPercentGt = percentGtField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.valueOf(percentGtField.getText());
                selPercentLt = percentLtField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.valueOf(percentLtField.getText());
                selMA = cb.getSelectedItem().toString();
                movingAverage = movingAves.getMaIntegerValue(cb.getSelectedIndex());
                description = "Current Price > " + selPercentGt + "% And < " + selPercentLt + "% of " + selMA;
            }
        }

        public MovingAveragePlus() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setPercentGtValue(double din) {
            selPercentGt = din;
        }
        public void setPercentLtValue(double din) {
            selPercentLt = din;
        }
        public double getPercentGtValue() {
            return selPercentGt;
        }
        public double getPercentLtValue() {
            return selPercentLt;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();            
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genMovingAveragePlusTradeLog(termTrades, operation, movingAverage, selPercentGt, selPercentLt);
            return termTrades;
        }
    }
    private int findStartOfValidData1(int maIn){
        return 0;
    }
    private int findStartOfValidData(int maIn){
        int staValidDataIdx;
        boolean found = false;
        for (staValidDataIdx = (histDataList.size() - 1); (staValidDataIdx >= 0) && !found; staValidDataIdx--) {
            if (histDataList.get(staValidDataIdx).getMa(maIn) > 0.0) {
                found = true;
            }
        }
        if(found == true){
            return(staValidDataIdx + 1);
        }else{
            return (-1);
        }
    }

    private double findSlope(int idx, int maIn) {
        double slope = -1;
        double pB;
        double pA;
        double pD;
        //get pointA then pointB..
        pB = histDataList.get(idx + RUN_IN_DAYS).getMa(maIn);
        pA = histDataList.get(idx).getMa(maIn);
        //do percentages..take diff of two points
        pD = (pA - pB);
        if (pD > 0.0) {
            //pos number, means we went positive from older date to newer date..
            //diff relative to the older value in pB to get percent move up
            pD = (pD / pB);
        } else if (pD <= 0.0) {
            //neg number (or 0), means we went negative from older date to newer date..
            //diff relative to the newer value in pA to get percent move down
            pD = (pD / pA);
        }
        pD *= 100.0; // use percent so multiply by 100..
        //calculate run/rise ratio...percent increase/decrease divided by time frame of RUN_IN_DAYS
        slope = myUtils.roundMe((pD / RUN_IN_DAYS), 6);

        return slope;
    }
   private double findSlopesSlope(int idx, int runIn) {
       /*
       find second dirivative given slope at index idx. Take two points (from idx back runIn in days) 
       slope difference and divide it by runIn. So slope1 - slope2 / runIn == approximate of 2ndDirivitive.
       */
        double slopesSlope = -1;
        double pA = 0.0;
        int idy;
        int cnt = 0;
        //add up all 1st derivatives in past runIn days..
        for(idy = idx; idy <= (idx + runIn); idy++){
            pA += histDataList.get(idy).get1stDerivative();
            cnt++;
        }                       
        //calculate averate of these slopes...
        slopesSlope = myUtils.roundMe((pA / runIn), 6);
        return slopesSlope;
    }
    
    
    private void calculateMaPercentages(int maIn, int staIn, int sizeIn) {
	/*
	this routine calculates MaPercentAbove value by counting every time the close price is above the maIn. Starting with
	today and going back backtest size in time. A percentage is calculated then stored. Then yesterday is done ..etc all the way back
	to back test size.
	*/
        int x = 0;
        int y = 0;
        int maCount = 0;
        double maPercent = 0.0;
        boolean sizeProblem = false;
        for (y = staIn; (y < sizeIn) && !sizeProblem; y++) {
            for (x = staIn; (x < sizeIn) && !sizeProblem; x++) {
                //test!!!
                if((x+y) > (histDataList.size() - 1)){
                    System.out.println("Shit!!! histDataList.size: " + histDataList.size() + " x+y: " + x+y);
                    sizeProblem = true;
                }else if (histDataList.get(x + y).getClosePrice() > histDataList.get(x + y).getMa(maIn)) {
                    maCount++;
                }
            }
            if(!sizeProblem){
                maPercent = (float) ((float) maCount / (float) ((sizeIn)));
                histDataList.get(y).setMaPercentAbove(maIn, maPercent);
                maCount = 0;
                maPercent = 0.0;
            }          
        }        
    }

    private boolean genHistoricalTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, double percentIn, int forLastXDays) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         This term relys on a MA value..so we need to find where this moving average calculation begins back in time i.e. 10dayMA needs 
         10 day history to calculate, 20 day needs 20 days  etc.
	 This routine will generate an occurrence if for the back test period, the percent above movAveIn is >= percentIn.
	 starting today, going back in time, the # of times closing prices were above the ma is counted and a percent is calculated.
	 This is done for every day going back in time to back test size.
         */
        int idx;
        boolean retVal = true;
        ibApi.historicalData.data d;
        OccurenceData actTradeData;
        int staValidData = 0;
        double percent = 0.0;
        termTradesIn.occurences = new ArrayList<>();
//        int btSize = tradeLogPeriod.backTestSizeInBars;
        int btSize = forLastXDays;
        if (btSize > histDataList.size()) {
            System.out.println("\ngenHistoricalTradeLog.backTestSize!!");
            retVal = false;
        } else {
            if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
                System.out.println("\nstart of valud data is at : " + staValidData + " btSize: " + btSize);
                calculateMaPercentages(movAveIn, 0, btSize);
                for (idx = btSize; idx >= 0; idx--) {
                    if ((percent = histDataList.get(idx).getPercentAboveMa(movAveIn)) >= (percentIn / 100.0)) {
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        percent = myUtils.roundMe(percent * 100.0, 2);
                        actTradeData.actualPercent = percent;
                        actTradeData.termOutputList.add("Percent above " + movAveIn + "Ma " + percent + "%");
                        actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
                        termTradesIn.occurences.add(actTradeData);
                    }
                }
            } else {
                retVal = false;
                System.out.println("\nngenHistoricalTradeLog.valudData problem!!");
            }

        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private class Historical extends Term {
        /*
         term used to show when the price of a stock has historically been above or below the 
         selectedMA selPercent of the time. 
         example: selAbove = true, selPercent = 60%, selMa = 100, selDaysHistory = 251 (1year): will be true 
         if price for the past year has been above the 100dma 60% of the time.
         if selAbove = false, then true if price has been below the 100dma 60% of the time.
         */

        TradeTerms term = TradeTerms.oHistorical;
        String selMA = "";
        private int movingAverage = 0;
        double selPercent = 0.0;
        boolean selAboveMa = true;
        int selDaysHistory = 0;
        String description = "";
        String prompt = "Enter % Above MA For Last X Days:";
        
        public void getUserInput() {
            boolean result = false;
            JTextField percentField = new JTextField(5);
	    //percentField.setText(Integer.toString(1234));
            JTextField numOfDays = new JTextField(5);

            percentField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            numOfDays.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter %:"));
            myPanel.add(percentField);
            myPanel.add(new JLabel("Enter Days:"));
            myPanel.add(numOfDays);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selPercent = Double.valueOf(percentField.getText());
                selMA = cb.getSelectedItem().toString();
                selDaysHistory = Integer.valueOf(numOfDays.getText());
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
                description = "Hist Price is above " + selMA + " " + selPercent + "% of time for last " + selDaysHistory + " Days";
            }
        }

        public Historical() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setPercentValue(double din) {
            selPercent = din;
        }

        public double getPercentValue() {
            return selPercent;
        }

        public void setDaysHistory(int iin) {
            selDaysHistory = iin;
        }

        public int getDaysHistory() {
            return selDaysHistory;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            System.out.println("\nnumOfDays:" + this.selDaysHistory);
            termTrades.termHead.termRanFine = genHistoricalTradeLog(termTrades, operation, movingAverage, selPercent, selDaysHistory);
            return termTrades;
        }
    }
    private boolean genRsiTradeLog(TradeTerms rsiTerm, TermOccurences termTradesIn, String operationIn, double rsiIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         This term relys on a MA value..so we need to find where this moving average calculation begins back in time i.e. 10dayMA needs 
         10 day history to calculate, 20 day needs 20 days  etc.
         */        
        int idx;
        boolean retVal = true;
        ibApi.historicalData.data d;
        OccurenceData actTradeData;
        boolean triggerTrade = false;
        double rsi = 0.0;
        termTradesIn.occurences = new ArrayList<>();         
        int btSize = (tradeLogPeriod.backTestSizeInBars < (histDataList.size() -1) ? tradeLogPeriod.backTestSizeInBars : (histDataList.size() - 1));
        if(btSize < 0)/*replace with some minimum*/ {
            System.out.println("\ntradeLogPeriod.backTestSize!!");
            retVal =  false;
        }else{
            if(btSize > (histDataList.size() -1)){
                System.out.println("\nhaha..");
            }
            for(idx = btSize; idx >= 0; idx--){
                triggerTrade = (((rsiTerm == TradeTerms.oRSIGT) && ((rsi = histDataList.get(idx).getRsi()) > rsiIn)) || 
                                ((rsiTerm == TradeTerms.oRSILT) && ((rsi = histDataList.get(idx).getRsi()) < rsiIn))) ? true : false;
                if(triggerTrade == true){
                    //RSI triggered a trade..
                    actTradeData = new OccurenceData();
                    actTradeData.action = operationIn;
                    actTradeData.occurenceDate = histDataList.get(idx).getDate();
                    actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                    actTradeData.actualRsi = rsi;
                    actTradeData.termOutputList.add("RSI: " + rsi);
                    actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
                    termTradesIn.occurences.add(actTradeData);
                    /*
                    actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        percent = myUtils.roundMe(percent * 100.0, 2);
                        actTradeData.actualPercent = percent;
                        actTradeData.termOutputList.add("Percent above " + movAveIn + "Ma " + percent + "%");
                        actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
                        termTradesIn.occurences.add(actTradeData);
                    */
                }                
            }
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private class RsiGT extends Term {
        /*
         term used to show when RSI is above selGreaterThanValue. 
         Example: 
         selGreaterThanValue = 70. Term will be true if RSI is 70 or greater, else false.
         selLessThanValue = 30. Term will be true if RSI is 30 or less, else false.
         */

        TradeTerms term = TradeTerms.oRSIGT;
        int selGreaterThanValue = 0;
        String description = "";
        String prompt = "Enter High RSI to Beat::";

        public void getUserInput() {
            boolean result = false;
            JTextField greaterThanValueField = new JTextField(5);

            greaterThanValueField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });

            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter > RSI:"));
            myPanel.add(greaterThanValueField);

            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selGreaterThanValue = Integer.valueOf(greaterThanValueField.getText());
                description = "RSI Currently > " + selGreaterThanValue;
            }
        }

        public RsiGT() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setGtRsi(int rsii) {
            selGreaterThanValue = rsii;
        }

        public int getGtRsi() {
            return selGreaterThanValue;
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genRsiTradeLog(term, termTrades, operation, selGreaterThanValue);
            return termTrades;
        }
    }

    private class RsiLT extends Term {
        /*
         term used to show when RSI is above selGreaterThanValue. 
         Example: 
         selGreaterThanValue = 70. Term will be true if RSI is 70 or greater, else false.
         selLessThanValue = 30. Term will be true if RSI is 30 or less, else false.
         */

        TradeTerms term = TradeTerms.oRSILT;
        int selLessThanValue = 0;
        String description = "";
        String prompt = "Enter Low RSI To Beat:";

        public void getUserInput() {
            boolean result = false;
            JTextField lessThanValueField = new JTextField(5);
            lessThanValueField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter < RSI:"));
            myPanel.add(lessThanValueField);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selLessThanValue = Integer.valueOf(lessThanValueField.getText());
                description = "RSI Currently < " + selLessThanValue;
            }
        }

        public RsiLT() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setLtRsi(int rsii) {
            selLessThanValue = rsii;
        }

        public int getLtRsi() {
            return selLessThanValue;
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn){
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term; 
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            termTrades.termHead.termRanFine = genRsiTradeLog(term, termTrades, operation, selLessThanValue);
            return termTrades;
        }
    }
    private boolean genSlopePolarityTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, int daysBackIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date                   
         */
        int idx;
        double pB;
        double pA;
        double pD;
        double riseOverRun;
        int slopeCounter = 0;
        boolean retVal = true;
        ibApi.historicalData.data d;
        OccurenceData actTradeData;
        int staValidData = 0;
		/*this bool needs to be true before we can count an open with + slope,
		//otherwise at begging of bt time we are just opening a position on slope only not when it goes from 
		neg to pos. 
		7/14/2020 changed to start with true, I want a trigger if it starts out true...
		*/
		boolean firstNegSlopeOccured = true;
        String polarityIn = termTradesIn.termHead.term.strVal; //oSlopePos
        termTradesIn.occurences = new ArrayList<>();
        int btSize = tradeLogPeriod.backTestSizeInBars;        
        if (btSize > histDataList.size()) {
            System.out.println("\ngenSlopePolarityTradeLog.backTestSize!!");
            retVal = false;
        } else {            
            if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
                System.out.println("\nstart of valid data is at : " + staValidData);
				//idx >= 1 was idx >= 0; later included newest raw bar so saw lot of on/off when bar has not closed yet.
				//epecially when doing intraday trading..trying to stop on last complete bar to see if more stable. wfs here
                for (idx = btSize; idx >= 0; idx--) {
                    riseOverRun = findSlope(idx, movAveIn);
					if ((firstNegSlopeOccured == false) && (riseOverRun < 0)){
						firstNegSlopeOccured = true;
					}
                    if((polarityIn.equals(TradeRules.TradeTerms.oSlopePos.strVal) && (riseOverRun > 0) && (firstNegSlopeOccured == true)) ||                                             
                       (polarityIn.equals(TradeRules.TradeTerms.oSlopeNeg.strVal) && (riseOverRun < 0))){
                        slopeCounter++;
                    }else{
                        slopeCounter = 0;
                    }
                    if (slopeCounter >= daysBackIn) {
                        //wanted slope persisted number of days in, so trigger event..
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        actTradeData.actualSlope = riseOverRun; 
                        actTradeData.termOutputList.add("Actual Slope: " + riseOverRun);                               
                        termTradesIn.occurences.add(actTradeData);
                    }
                }
            } else {
                retVal = false;
                System.out.println("\ngenSlopePolarityTradeLog.valudData problem!!");
            }

        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private class SlopeNeg extends Term {
        /*
         term used to show when slope of selMa is negative. 
         Example: 
         selMa = 100dma, selDaysBack = 5, true if the slope value is greater than it was 5 days ago, else false.
         */

        TradeTerms term = TradeTerms.oSlopeNeg;
        String selMA = "";
        private int movingAverage = 0;
        int selDaysBack = 0;
        String description = "";
        String prompt = "Enter Days Back in determining - slope of MA:";
        
        public void getUserInput() {
            boolean result = false;
            JTextField numOfDays = new JTextField(5);
            numOfDays.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter Days:"));
            myPanel.add(numOfDays);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
                selDaysBack = Integer.valueOf(numOfDays.getText());
                description = "Slope of " + selMA + " is - for last " + selDaysBack + " days";
            }
        }

        public SlopeNeg() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setDaysHistory(int dbin) {
            selDaysBack = dbin;
        }

        public int getDaysHistory() {
            return selDaysBack;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn) {
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            System.out.println("\nnumOfDays:" + selDaysBack);
            System.out.println("\nselMa: " + selMA);
            termTrades.termHead.termRanFine = genSlopePolarityTradeLog(termTrades, operation, maIn, selDaysBack);
            return termTrades;
        }
    }

    private class SlopePos extends Term {
        /*
         term used to show when slope of selMa is positive. 
         Example: 
         selMa = 100dma, selDaysBack = 5, true if the slope value is less than it was 5 days ago, else false.
         */

        TradeTerms term = TradeTerms.oSlopePos;
        String selMA = "";
        private int movingAverage = 0;
        int selDaysBack = 0;
        String description = "";
        String prompt = "Enter Days Back in determining + slope of MA:";
        
        public void getUserInput() {
            boolean result = false;
            JTextField numOfDays = new JTextField(5);

            numOfDays.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter Days:"));
            myPanel.add(numOfDays);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            myPanel.add(cb);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selMA = cb.getSelectedItem().toString();
                selDaysBack = Integer.valueOf(numOfDays.getText());
                description = "Slope of " + selMA + " is + for last " + selDaysBack + " days";
                movingAverage =  movingAves.getMaIntegerValue(cb.getSelectedIndex());
            }
        }

        public SlopePos() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setDaysHistory(int dbin) {
            selDaysBack = dbin;
        }

        public int getDaysHistory() {
            return selDaysBack;
        }
        public int getMa(){
            return movingAverage;
        }
        public void setMa(int ma){
            movingAverage = ma;
            selMA = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn) {
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            System.out.println("\nnumOfDays:" + selDaysBack);
            System.out.println("\nselMa: " + selMA);
            termTrades.termHead.termRanFine = genSlopePolarityTradeLog(termTrades, operation, maIn, selDaysBack);
            return termTrades;
        }
    }

    private class Volume extends Term {
        /*
         term used to show when volume is greater then selVolume.
         Example: 
         selVolume set o 150,000, true if current volume is equal to or greater than
         150k.
         */

        TradeTerms term = TradeTerms.oVolume;
        int selVolume = 0;
        String description = "";
        String prompt = "Enter Volume To Beat:";

        public void getUserInput() {
            boolean result = false;
            JTextField volumeValueField = new JTextField(5);
            volumeValueField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Enter Volume:"));
            myPanel.add(volumeValueField);
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selVolume = Integer.valueOf(volumeValueField.getText());
                description = "Volume is > " + selVolume;
            }
        }

        public Volume() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setVolumeVal(int vin) {
            selVolume = vin;
        }

        public int getVolumeVal() {
            return selVolume;
        }
    }

    private boolean genSlopeStrengthTradeLog(TermOccurences termTradesIn, String operationIn, int movAveIn, double slopeValueIn, int daysBackIn) {
        /*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date
         This term relys on a MA value..so we need to find where this moving average calculation begins back in time i.e. 10dayMA needs 
         10 day history to calculate, 20 day needs 20 days  etc.
         slopeIn is positive number:
         if operaton is OPEN:
           if slope >= slopeIn, open a trade.
         else if operation is CLOSE:
           if slope <= slopeIn, close a trade.
         either case slopeIn could be positive or negative, such that:
         slopeIn is negative number:
         if operaton is OPEN:
           if slope <= slopeIn, open a trade.
         else if operation is CLOSE:
           if slope >= slopeIn, close a trade.
         either case slopeIn could be a negative number, such that:           
         */
        int idx;
        double pB;
        double pA;
        double pD;
        double riseOverRun;
        int slopeCounter = 0;
        boolean retVal = true;
        ibApi.historicalData.data d;
        OccurenceData actTradeData;
        int staValidData = 0;
        termTradesIn.occurences = new ArrayList<>();
        int btSize = tradeLogPeriod.backTestSizeInBars;        
        if (btSize > histDataList.size()) {
            System.out.println("\ngenSlopeStrengthTradeLog.backTestSize!!");
            retVal = false;
        } else {            
            if (((staValidData = findStartOfValidData(movAveIn)) != -1) && (staValidData > btSize)) {
                System.out.println("\nstart of valid data is at : " + staValidData);
                for (idx = btSize; idx >= 0; idx--) {
                    riseOverRun = findSlope(idx, movAveIn);                              
                    if(((operationIn.equals(slopeDefs.oOPEN)) && (slopeValueIn > 0) && (riseOverRun >= slopeValueIn)) || 
                       ((operationIn.equals(slopeDefs.oOPEN)) && (slopeValueIn < 0) && (riseOverRun >= slopeValueIn)) ||
                       ((operationIn.equals(slopeDefs.oOPEN)) && (slopeValueIn == 0) && (myUtils.roundMe(riseOverRun, 1) == slopeValueIn)) ||
                       ((operationIn.equals(slopeDefs.oCLOSE)) && (slopeValueIn > 0) && (riseOverRun <= slopeValueIn))|| 
                       ((operationIn.equals(slopeDefs.oCLOSE)) && (slopeValueIn < 0) && (riseOverRun <= slopeValueIn))){
                        slopeCounter++;
                    }else{
                        slopeCounter = 0;
                    }
                    if (slopeCounter >= daysBackIn) {
                        //wanted slope persisted number of days in, so trigger event..
                        actTradeData = new OccurenceData();
                        actTradeData.action = operationIn;
                        actTradeData.occurenceDate = histDataList.get(idx).getDate();
                        actTradeData.closePrice = histDataList.get(idx).getClosePrice();
                        actTradeData.actualSlope = riseOverRun; 
                        actTradeData.termOutputList.add("Actual Slope: " + riseOverRun);                               
                        termTradesIn.occurences.add(actTradeData);
                    }
                }
            } else {
                retVal = false;
                System.out.println("\ngenSlopeStrengthTradeLog.valudData problem!!");
            }

        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }

    private class SlopeFilter extends Term {
        /*
         term used to show when slope value is >= selSlopeValue, if positive value.
         or, if value is negative, show when slope is the negative value or greater.
         Example: 
         selSlopeValue set to +.5, show when slope == .5 or greater.
         selSlopeValue set to -.5, show when slope == -.5 or less.
         */

        TradeTerms term = TradeTerms.oSlopeFilter;
        int selDaysHistory = 0;
        String description = "";
        double selSlopeValue = 0.0;
        String selMa;
        String prompt = "Enter MA Slope Value (+ or -, 1 == 1%) For last Days:";
        private int movingAverage = 0;
        public void getUserInput() {
            //
            JTextField slopeStrengthValueField = new JTextField(5);
            JTextField numOfDays = new JTextField(5);
            GTLTCheckBox = new javax.swing.JCheckBox();
            slopeStrengthValueField.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            numOfDays.addActionListener(e -> {
                KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                manager.focusNextComponent();
            });
            GTLTCheckBox.setSelected(true);
            GTLTCheckBox.setText(">=");            
            userSelGTE = true; // >=
            userSelLTE = !userSelGTE; // <=
            GTLTCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    GTELTECheckBoxActionPerformed(evt);
                }
            });
            JComboBox cb = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            myPanel.add(new JLabel("Entere Slope Value In % (Sel >= Or <=):"));
            myPanel.add(GTLTCheckBox);
            myPanel.add(slopeStrengthValueField);
            myPanel.add(new JLabel("Enter Days:"));
            myPanel.add(numOfDays);
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel("Select MA:"));
            
            myPanel.add(cb);

            boolean result = false;
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {
                selSlopeValue = Double.valueOf(slopeStrengthValueField.getText());
                selMa = cb.getSelectedItem().toString();
                selDaysHistory = Integer.valueOf(numOfDays.getText());
                movingAverage = movingAves.getMaIntegerValue(cb.getSelectedIndex());
                description = selMa;
                description += ((userSelGTE == true ) ? (" Slope >= " + selSlopeValue) : ("Slope <= " + selSlopeValue));
                description += " for last " + selDaysHistory + " Days";
            }
        }

        public SlopeFilter() {
            //super.daTerm = this;              
        }

        public String getDescription() {

            return description;
        }

        public TradeTerms getTerm() {
            return term;
        }

        public void setDescription(String sin) {
            description = sin;
        }

        public void setSlopeFilter(double vin) {
            selSlopeValue = vin;
        }

        public double getSlopeValue() {
            return selSlopeValue;
        }
        public void setDaysHistory(int iin) {
            selDaysHistory = iin;
        }

        public int getDaysHistory() {
            return selDaysHistory;
        }
        public int getMa() {
            return movingAverage;
        }

        public void setMa(int ma) {
            movingAverage = ma;
            selMa = Integer.toString(movingAverage);
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int maIn) {
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
            System.out.println("\nnumOfDays:" + selDaysHistory);
            System.out.println("\nselMa: " + selMa);
            termTrades.termHead.termRanFine = genSlopeStrengthTradeLog(termTrades, operation, maIn, selSlopeValue, selDaysHistory);
            return termTrades;
        }
    }
    private boolean genCompare2MAsTradeLog1(TermOccurences termTradesIn, String operationIn, String operatorIn, int maAIn, int maBIn) {
        /*
            compare using operationIn (< or >) the two moving averages provided, maA and maB. 
            If the comparison is true, trigger the event..
        */
        int staValidDataMaA = 0;
        int staValidDataMaB = 0;
        OccurenceData actOccurenceData;
        boolean retVal = true;
        termTradesIn.occurences = new ArrayList<>();
        int btSize = tradeLogPeriod.backTestSizeInBars;
        int idx;
        double pA;
        double pB;
        boolean triggerOccurence = false;
        boolean filterOn = false;
        int filterCnt = 0;
        String lastTriggerDate = "";
        staValidDataMaA = findStartOfValidData(maAIn);
        staValidDataMaB = findStartOfValidData(maBIn);
        if (btSize > histDataList.size()) {
            System.out.println("\ngenCompare2MAsTradeLog.backTestSize!!");
            retVal = false;
        } else if (((staValidDataMaA != -1) && (staValidDataMaA > btSize)) && 
                   ((staValidDataMaB != -1) && (staValidDataMaB > btSize))) {
            System.out.println("\nstart of valid data maA is at : " + staValidDataMaA);
            System.out.println("\nstart of valid data maB is at : " + staValidDataMaB);
            for (idx = btSize; idx >= 0; idx--) {
                pA = histDataList.get(idx).getMa(maAIn);
                pB = histDataList.get(idx).getMa(maBIn);
                triggerOccurence = (operatorIn.equals(">") ? (pA > pB) : (operatorIn.equals("<") ? (pA < pB) : false));
                if (triggerOccurence == true) {
                    //check if we started triggered, if so filter out so we only capture beginings..
                    if (idx == btSize){
                        filterOn = true;
                        lastTriggerDate = histDataList.get(idx).getDate();
                        continue;
                    }else if ((filterOn == true) && (getTradingDaysBetweenDates(histDataList.get(idx).getDate(), lastTriggerDate) == 1)){
                        lastTriggerDate = histDataList.get(idx).getDate();
                        continue;
                    }else{
                        filterOn = false;
                    }
                    //Comparison passed, so trigger event..
                    actOccurenceData = new OccurenceData();
                    actOccurenceData.action = operationIn;
                    actOccurenceData.occurenceDate = histDataList.get(idx).getDate();
                    actOccurenceData.actualMaValue = pA;
                    actOccurenceData.actualMa2Value = pB;
                    actOccurenceData.closePrice = histDataList.get(idx).getClosePrice();
                    termTradesIn.occurences.add(actOccurenceData);
                }else{
                    System.out.println("\nhumm");
                }
            }
        }else{
            retVal = false;
            System.out.println("\ngenCompare2MAsTradeLog.validData problem!!");
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
        private boolean genCompare2MAsTradeLog(TermOccurences termTradesIn, String operationIn, String operatorIn, int maAIn, int maBIn) {
        /*
            compare using operationIn (< or >) the two moving averages provided, maA and maB. 
            If the comparison is true, trigger the event..
        */
        int staValidDataMaA = 0;
        int staValidDataMaB = 0;
        OccurenceData actOccurenceData;
        boolean retVal = true;
        termTradesIn.occurences = new ArrayList<>();
        int btSize = tradeLogPeriod.backTestSizeInBars;
        int idx;
        double pA;
        double pB;
        boolean triggerOccurence = false;
        staValidDataMaA = findStartOfValidData(maAIn);
        staValidDataMaB = findStartOfValidData(maBIn);
        if (btSize > histDataList.size()) {
            System.out.println("\ngenCompare2MAsTradeLog.backTestSize!!");
            retVal = false;
        } else if (((staValidDataMaA != -1) && (staValidDataMaA > btSize)) && 
                   ((staValidDataMaB != -1) && (staValidDataMaB >= btSize))) {
            System.out.println("\nstart of valid data maA is at : " + staValidDataMaA);
            System.out.println("\nstart of valid data maB is at : " + staValidDataMaB);
            tradeLogPeriod.begBtDate = histDataList.get(btSize).getDate();
            tradeLogPeriod.endBtDate = histDataList.get(0).getDate();
            for (idx = btSize; idx >= 0; idx--) {
                pA = histDataList.get(idx).getMa(maAIn);
                pB = histDataList.get(idx).getMa(maBIn);
                triggerOccurence = (operatorIn.equals(">") ? (pA > pB) : (operatorIn.equals("<") ? (pA < pB) : false));
                if(triggerOccurence == true){
                    //Comparison passed, so trigger event..
                        actOccurenceData = new OccurenceData();
                        actOccurenceData.action = operationIn;
                        actOccurenceData.occurenceDate = histDataList.get(idx).getDate(); 
                        actOccurenceData.actualMaValue = pA;
                        actOccurenceData.actualMa2Value = pB;
                        actOccurenceData.closePrice = histDataList.get(idx).getClosePrice();
                        termTradesIn.occurences.add(actOccurenceData);
                }
            }
        }else{
            retVal = false;
            System.out.println("\ngenCompare2MAsTradeLog.validData problem!!");
        }
        termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
        return retVal;
    }
    private class Compare2MAs extends Term {
        /*
         term used to compare 2 moving average values i.e. 10ma > 100ma.
         */

        TradeTerms term = TradeTerms.oCompareMAs;
        String description = "";
        String selMa;
        String selMaB;
        String prompt = "Enter MA1, operand(<or>), then MA2 to Compare:";
        String selOperator = "";
        private int movingAverage = 0;
        private int movingAverageB = 0;
        
        public void getUserInput() {
            //            
            GTLTCheckBox = new javax.swing.JCheckBox();            
            GTLTCheckBox.setSelected(true);
            GTLTCheckBox.setText(">");
            userSelGT = true; // >
            userSelLT = !userSelGT; // <
            GTLTCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    GTLTCheckBoxActionPerformed(evt);
                }
            });
            JComboBox cbA = new JComboBox(movingAves.getMaList());
            JComboBox cbB = new JComboBox(movingAves.getMaList());
            JPanel myPanel = new JPanel();
            
            myPanel.add(Box.createHorizontalStrut(5)); // a spacer
            myPanel.add(new JLabel(" MA1:"));
            myPanel.add(cbA);
            myPanel.add(new JLabel(" Is: "));
            myPanel.add(GTLTCheckBox);
            myPanel.add(new JLabel(" MA2:"));
            myPanel.add(cbB);

            boolean result = false;
            result = commonGui.getUserInput(prompt, myPanel);
            if (result == true) {               
                selMa = cbA.getSelectedItem().toString();
                movingAverage = movingAves.getMaIntegerValue(cbA.getSelectedIndex());
                description = selMa;
                description += ((userSelGT == true) ? (" > ") : (" < "));
                selMaB = cbB.getSelectedItem().toString();
                movingAverageB = movingAves.getMaIntegerValue(cbB.getSelectedIndex());                    
                description += (" " + selMaB);
                selOperator = ((userSelGT == true) ? (">") : ("<"));
            }
        }

        public Compare2MAs() {
            //super.daTerm = this;              
        }

        @Override
        public String getDescription() {

            return description;
        }

        @Override
        public TradeTerms getTerm() {
            return term;
        }

        @Override
        public void setDescription(String sin) {
            description = sin;
        }


        @Override
        public int getMa() {
            return movingAverage;
        }
        public int getMaB() {
            return movingAverageB;
        }
        @Override
        public void setMa(int ma) {
            movingAverage = ma;
            selMa = Integer.toString(movingAverage);
        }
        public void setMaB(int ma) {
            movingAverageB = ma;
            selMaB = Integer.toString(movingAverageB);
        }
        public String getOperator() {
            return selOperator;
        }
        public void setOperator(String oin) {
            selOperator = oin;
        }
        @Override
        public TermOccurences processTerm(String tickerin, String operation, int ma1In) {
            termTrades = new TermOccurences();
            System.out.println("\nchecking term: " + this.description + " for truth.");
            termTrades.termHead.termDescription = description;
            termTrades.termHead.term = term;
            termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();            
            System.out.println("\nselMa: " + selMa);
            System.out.println("\nselMb: " + selMaB);
            //ma1In comes from parent term, second ma comes from child term...
            termTrades.termHead.termRanFine = genCompare2MAsTradeLog(termTrades, operation, selOperator, ma1In, movingAverageB);
            return termTrades;
        }
    }
	
	//create an enclosing instance ..
	NewTraderTest nttOuter = null; //new NewTraderTest(actIbApi, userSelLT);	
	
   private NewTraderTest.StockCorrections findCorrections(int startFrom, int endAt, List<ibApi.historicalData.data> hdataIn, double correctionPercentIn) {
		/*
		starting with the oldest data, keep hi and low water mark on closing prices. 
		A correction is defined as followes:
        whenever a previous high water mark is reached a second time, after the lower water mark went down 5% from first high water mark, 
        a correction has occured. At this time record neccessary data of correcton and generate a list entry.
		 every time closing price makes new high bump up the high water mark, at the same time clear the low water mark
		if the current price is lower than high water mark and higher than the low water mark, do nothing. 
		if current price is lower than high water mark and lower than low water mark, update low water mark.
      
		*/
		//create an enclosing instance ..	
		//NewTraderTest nttOuter = new NewTraderTest(actIbApi, userSelLT);
		StockCorrection actCorrection = nttOuter.new StockCorrection();
		StockCorrections allCorrections = nttOuter.new StockCorrections();	
	   
		int x;
		double hiWater = 0.0;
		double loWater = 0.0;
		double curClose = 0.0;
		double startClose = 0.0;
		double correctionSumPercent = 0.0;
		boolean enCorrection = false;
		int hdsz = hdataIn.size();
		//"from" is start day "to" is end day of range to check for corrections/rallies. 
		//these both should be less than size of historical data In.
		if (hdataIn.isEmpty() || (startFrom > hdataIn.size() || endAt > hdataIn.size())) {
			return null;
		}
		//setup initial condition..
		hiWater = startClose = loWater = curClose = hdataIn.get(startFrom).getClosePrice();
		//allCorrections.stockTicker = tickerIn;
		//cycle from oldest date to present..
		for (x = startFrom; x >= endAt; x--) {
			curClose = hdataIn.get(x).getClosePrice();
			if (curClose > hiWater) {
				if (enCorrection == true) {
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
					actCorrection = nttOuter.new StockCorrection();
				}
				hiWater = curClose;
				loWater = hiWater;
				actCorrection.startCorrectionValue = curClose;
				actCorrection.startCorrectionDate = hdataIn.get(x).getDate();			
			} else if (curClose < loWater) {
				loWater = curClose;
				//check if glorified correction as needs to be larger than correctionPercentIn
				enCorrection = ((hiWater - loWater) / hiWater) > (correctionPercentIn / 100.0);
				actCorrection.troughValue = curClose;
				actCorrection.troughDate = hdataIn.get(x).getDate();
			}
		}
		//put in info on correction in progress..
		actCorrection.correction = myUtils.roundMe((actCorrection.startCorrectionValue - actCorrection.troughValue), 2);
		actCorrection.correctionPercent = myUtils.roundMe(((actCorrection.correction / actCorrection.startCorrectionValue) * 100.0), 2);
		actCorrection.correctionComplete = false;
		//the following only make sense to compute when a correction is in progress..
		actCorrection.troughToCurrentValue = (curClose - actCorrection.troughValue);
		actCorrection.troughToCurrentPercent = myUtils.roundMe(((actCorrection.troughToCurrentValue / actCorrection.troughValue) * 100.0), 2);
		actCorrection.startOfCorrectionToCurrentValue = (actCorrection.startCorrectionValue - curClose);
		actCorrection.startOfCorrectionToCurrentPercent = myUtils.roundMe(((actCorrection.startOfCorrectionToCurrentValue / actCorrection.startCorrectionValue) * 100.0), 2);
		allCorrections.correctionList.add(actCorrection);

		int sz = allCorrections.numOfCorrections - 1;
		double lastHigh;
		if (sz >= 0) {
			//get last complete correction's (size minus one) endCorrectionValue (end of complete correction)..
			lastHigh = allCorrections.correctionList.get(sz).endCorrectionValue;
		} else {
			//no complete corrections, so use starting close..
			lastHigh = startClose;
		}
		//compare current incomplete correction's start value to last completed correction to see if new high happening..
		if ((actCorrection.startCorrectionValue > lastHigh) && (enCorrection == false)) {
			//new high happening..
			allCorrections.isNewHigh = true;
			allCorrections.newHigh = actCorrection.startCorrectionValue;
			allCorrections.newHighPercent = myUtils.roundMe(((allCorrections.newHigh - lastHigh) / lastHigh) * 100.0, 2);
		}
		if (allCorrections.numOfCorrections >= 3) {
			allCorrections.aveCorrectionPercent = myUtils.roundMe((correctionSumPercent / (double) allCorrections.numOfCorrections), 2);
			allCorrections.currentlyWithinAveCorrectionPercent = myUtils.roundMe((allCorrections.aveCorrectionPercent - actCorrection.startOfCorrectionToCurrentPercent), 2);
		}
		return allCorrections;
	}
	private boolean genCorrectionAveTradeLog(TradeTerms aveCorrectionTerm, TermOccurences termTradesIn, String operationIn, double withInPercent) {
		/*
         explain! histDataList input. histDataList[0] == today, histDataList[size] == oldest date        
		 */
		int idx;
		boolean retVal = true;
		ibApi.historicalData.data d;
		OccurenceData actTradeData;		
		int from = 0;
		int to = 0;
		/*
		NewTraderTest nttOuter = new NewTraderTest(actIbApi, userSelLT);
		*/
		if(nttOuter == null){
			nttOuter = new NewTraderTest(actIbApi, userSelLT);
		}
		StockCorrections allCorrections = nttOuter.new StockCorrections();
		
		termTradesIn.occurences = new ArrayList<>();
		int btSize = (tradeLogPeriod.backTestSizeInBars < (histDataList.size() - 1) ? tradeLogPeriod.backTestSizeInBars : (histDataList.size() - 1));
		if (btSize < 0)/*replace with some minimum*/ {
			System.out.println("\ntradeLogPeriod.backTestSize!!");
			retVal = false;
		} else {
			if (btSize > (histDataList.size() - 1)) {
				System.out.println("\nhaha..");
			}else{
				from = histDataList.size() - 1;
				to = btSize;
				//to = 0;				
			}
			//idx = 0;
			for (idx = 0; idx <= btSize; idx++) {
				allCorrections = findCorrections((from /*- idx*/), (to - idx), histDataList, 5);
				if (allCorrections.correctionList.size() > 0) {
					double correctionTroughToCurrentPercent = allCorrections.correctionList.get(allCorrections.correctionList.size() - 1).troughToCurrentPercent;
					if ((allCorrections.aveCorrectionPercent > 0.0) && (Math.abs(allCorrections.currentlyWithinAveCorrectionPercent) <= withInPercent) && (Math.abs(correctionTroughToCurrentPercent) <= withInPercent)) {
						//Correction triggered a trade..
						actTradeData = new OccurenceData();
						actTradeData.action = operationIn;
						actTradeData.occurenceDate = histDataList.get(to - idx).getDate();
						actTradeData.closePrice = histDataList.get(to - idx).getClosePrice();
						actTradeData.actualCorrection = allCorrections.currentlyWithinAveCorrectionPercent;
						actTradeData.termOutputList.add("CurrentWithIn%: " + allCorrections.currentlyWithinAveCorrectionPercent);
						actTradeData.termOutputList.add("AveCorrection%: " + allCorrections.aveCorrectionPercent);
						actTradeData.actualAveCorrection = allCorrections.aveCorrectionPercent;
						actTradeData.occurenceDescription = termTradesIn.termHead.termDescription;
						termTradesIn.occurences.add(actTradeData);
					}
				}								
			}
		}
		termTradesIn.termHead.numOfOccurences = termTradesIn.occurences.size();
		return retVal;
	}

	private class CorrectionAve extends Term {

		/*
         term used to show when RSI is above selGreaterThanValue. 
         Example: 
         selGreaterThanValue = 70. Term will be true if RSI is 70 or greater, else false.
         selLessThanValue = 30. Term will be true if RSI is 30 or less, else false.
		 */

		TradeTerms term = TradeTerms.oCorrectionAve;
		int selCorrectionWithinValue = 0;
		String description = "";
		String prompt = "Enter Percent within Ave Correction:";

		public void getUserInput() {
			boolean result = false;
			JTextField withInAveCorrectionValueField = new JTextField(5);

			withInAveCorrectionValueField.addActionListener(e -> {
				KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
				manager.focusNextComponent();
			});

			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel("Enter % within AveCorrection:"));
			myPanel.add(withInAveCorrectionValueField);

			result = commonGui.getUserInput(prompt, myPanel);
			if (result == true) {
				selCorrectionWithinValue = Integer.valueOf(withInAveCorrectionValueField.getText());
				description = "Currently Correction Within (%) " + selCorrectionWithinValue;
			}
		}

		public CorrectionAve() {
			//super.daTerm = this;              
		}

		public String getDescription() {

			return description;
		}

		public TradeTerms getTerm() {
			return term;
		}

		public void setDescription(String sin) {
			description = sin;
		}

		public void setCorrectionWithInPercent(int within) {
			selCorrectionWithinValue = within;
		}

		public int getCorrectionWithInPercent() {
			return selCorrectionWithinValue;
		}

		@Override
		public TermOccurences processTerm(String tickerin, String operation, int maIn) {
			termTrades = new TermOccurences();
			System.out.println("\nchecking term: " + this.description + " for truth.");
			termTrades.termHead.termDescription = description;
			termTrades.termHead.term = term;
			termTrades.termHead.andOrLogic = this.getAndOrSelItem().toString();
			termTrades.termHead.termRanFine = genCorrectionAveTradeLog(term, termTrades, operation, selCorrectionWithinValue);
			return termTrades;
		}
	}
}
