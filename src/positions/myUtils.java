/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;
//import tdTradeApi.*;
//import tdTradeApi.actTraderApi.quoteInfo;
import ibTradeApi.*;
import ibTradeApi.ibApi.quoteInfo;
import java.io.IOException;
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import tradeMenus.NewTraderTest;


/**
 *
 * @author walterstevenson
 */

public class myUtils {

	static String ZEROES = "000000000000";
	static String BLANKS = "            ";
	static boolean weDisplayedThisAlready = false;
	public static String format( int val, int w) 
	{	return format( Integer.toString(val), w); }
		
	public static String format( String s, int w) 
	{
		int w1 = Math.abs(w);
		int n = w1-s.length();

		if ( n <= 0 ) return s;
		while (BLANKS.length()<n) BLANKS += "      ";
		if ( w<0 ) 
			return s + BLANKS.substring(0,n);
		else
			return BLANKS.substring(0,n) + s;
	}	

	public static String format( double val, int n, int w) 
	{
			//	rounding			
		double incr = 0.5;
		for( int j=n; j>0; j--) incr /= 10; 
		val += incr;
		
		String s = Double.toString(val);
		int n1 = s.indexOf('.');
		int n2 = s.length() - n1 - 1;
		
		if (n>n2)  {   
			int len = n-n2;
			while (ZEROES.length()<len) ZEROES += "000000";
			s = s+ZEROES.substring(0, len);
		}
		else if (n2>n) s = s.substring(0,n1+n+1);

		return format( s, w );
	}
        
    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float) tmp / p;
    }

    public static double Round(double Rval, int Rpl) {
        double p = (double) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (double) tmp / p;
    }

    public static double roundMe(double roundme, int places) {
        //double r = 3.1537;
        double retme;
        if(Double.isInfinite(roundme) || Double.isNaN(roundme)){
            System.out.println("\nroundMe Error!! isInfinite or NaN!!");
            return(0.0);
        }
        BigDecimal bd = new BigDecimal(roundme);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_EVEN);
        retme = bd.doubleValue();
        return (retme);
    }

    public static double roundMeUp(double roundme, int places) {
        //double r = 3.1537;
        double retme;

        BigDecimal bd = new BigDecimal(roundme);
        bd = bd.setScale(places, BigDecimal.ROUND_UP);
        retme = bd.doubleValue();
        return (retme);
    }

    public static double roundMeDown(double roundme, int places) {
        //double r = 3.1537;
        double retme;

        BigDecimal bd = new BigDecimal(roundme);
        bd = bd.setScale(places, BigDecimal.ROUND_DOWN);
        retme = bd.doubleValue();
        return (retme);
    }

    static public class myFloat {
        myFloat mf;
        
        myFloat getMyFloat(){
            return mf;
        }
        myFloat(float fin) {
            fval = fin;
            mf = this;
        }
        float fval;

        void setVal(float fin) {
            fval = fin;
        }

        float getVal() {
            return (fval);
        }
    }
    public static String GetTodaysDate() {
        String todaysDate;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        todaysDate = sdf.format(new Date());
        return(todaysDate);
    }
    public static String reverseDate(String inStr) {
        String reverse = null;
        String ymdhms[] = null;
        String y = null;
        String m = null;
        String d = null;
		String h = null;
		String min = null;
		String s = null;
        //parse ymdhms  "/ parses the '/', |\\ parses the space, |\\: parses the :
        ymdhms = inStr.split("/|\\ |\\:");
        m = ymdhms[0];
        d = ymdhms[1];
        y = ymdhms[2];
		h = ymdhms[3];
		min = ymdhms[4];
		s = ymdhms[5];
        reverse = y + m + d + " "+h + ":"+min + ":"+s;
        
        return reverse;
    }
    public static String reverseMe(String inStr) {
        String reverse = null;
        reverse = new StringBuilder(inStr).reverse().toString();
        return reverse;
    }
    public static String GetTodaysDate(String wantedFormat) {
        String todaysDate;
        SimpleDateFormat sdf = new SimpleDateFormat(wantedFormat);

        todaysDate = sdf.format(new Date());
        return(todaysDate);
    }
	public static String GetCurrentTime(String wantedFormat) {
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat(wantedFormat);
		
		String todaysDate = dateFormat.format(date);
       
        return(todaysDate);
    }
	static SimpleDateFormat df = null;
	static Calendar tCal = null;
	static Date tDate = null;
	public static void SetSetTime(String wantedFormat, String timeIn) {
		df = new SimpleDateFormat(wantedFormat);
		tCal = Calendar.getInstance();
		tDate = null;
		try {
			tDate = df.parse(timeIn);
		} catch (ParseException ex) {
			Logger.getLogger(NewTraderTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		tCal.setTime(tDate);
		return;
	}
	public static String GetSetTime(){
		/*
		return "" if error.
		*/
		if((df == null) || (tCal == null)){
			return "";
		}
		String dateTime = df.format(tCal.getTime());
		//return just time portion..from space to end is time
		return dateTime.substring(dateTime.indexOf(" "));
	}
	public static String AddSetTime(int field, int amount){
		/*
		field is Calendar.SECOND/MINUTE/HOUR etc, amount is number of added or subtracted. example: 
		Calendar.MINUTE, 3 is add 3 minutes or Calendar.SECONDS, -5 is subtract five seconds.		
		return "" if error.		
		*/
		if((df == null) || (tCal == null)){
			return "";
		}
		tCal.add(field, amount);
		return df.format(tCal.getTime());
	}
	public static void SetSetDateTime(String wantedFormat, String dateIn) {
		df = new SimpleDateFormat(wantedFormat);
		tCal = Calendar.getInstance();
		tDate = null;
		try {
			tDate = df.parse(dateIn);
		} catch (ParseException ex) {
			Logger.getLogger(NewTraderTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		tCal.setTime(tDate);
		return;
	}
	public static String GetSetDate(){
		/*
		return "" if error.
		*/
		if((df == null) || (tCal == null)){
			return "";
		}
		String dateTime = df.format(tCal.getTime());
		//just return date not time...index 0 to space is date, space to end is time..
		return dateTime.substring(0, dateTime.indexOf(" "));
	}
    public static boolean isItTradingDay() {
        boolean trade = false;

        String Day;
        SimpleDateFormat sdf = new SimpleDateFormat("E");
        Day = sdf.format(new Date());

        if ((Day.equals("Sat")) || (Day.equals("Sun"))) {
           trade = false;
        }else {
           trade = true;
           weDisplayedThisAlready = false;
        }
        if ((trade == false) && (weDisplayedThisAlready == false)) {
            commonGui.postInformationMsg("Market is closed on the Weekend!!");
            weDisplayedThisAlready = true;
        }
        //wfs fix this back!!!!!!
        return(trade);
        //return(true);
    }
    
    
    public static class marketVolume {
        static final int VOL_STATE_START_OVER = 0;
        static final int VOL_STATE_READ1 = 1;
        static final int VOL_STATE_READ2 = 2;
        static final int VOL_STATE_COMPARE = 3;
        static final int VOL_STATE_WAIT = 4;
        int marketState =0;
        int volVal = 0;
        boolean ready;
        boolean read = false;
        quoteInfo sQuote1 = new quoteInfo();
        int volRead1 = 0;
        int volRead2 = 0;
        ibApi actIbApi = ibApi.getActApi();
        public void marketVolume() {
            marketState = VOL_STATE_START_OVER;
            ready = false;
            volRead1 = 0;
            volRead2 = 0;
            volVal = 0;
            read = false;

        }
        public boolean checkVolume() {
            if (marketState != VOL_STATE_WAIT) {
                marketState++;
            }

            switch (marketState) {
                case VOL_STATE_READ1:
                    volVal = 0;
                    sQuote1 = actIbApi.getQuote("spy", false);
                    volRead1 = sQuote1.volume;
                    break;
                case VOL_STATE_READ2:
                    sQuote1 = actIbApi.getQuote("spy", false);
                    volRead2 = sQuote1.volume;
                    break;
                case VOL_STATE_COMPARE:
                    volVal = (volRead2 - volRead1);
                    ready = true;
                    read = false;
                    marketState = VOL_STATE_WAIT;
                    break;
                case VOL_STATE_WAIT:
                    if (read == true) {
                        ready = false;
                        marketState = VOL_STATE_START_OVER;
                    }
                    break;
            }
            return(ready);
        }
        
        public int readVol() {
            if ((ready == true) && (read == false)) {
                read = true;

                return(volVal);
            }
            return(0);
        }

    }

    public static void  delay(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public static int countOccurrences(String hayStack, char needle) {
        int count = 0;
        for (int i = 0; i < hayStack.length(); i++){
            if (hayStack.charAt(i) == needle ) {
                count++;
            }
        }
        return count;
    }
    
    public static String[] fillTickerList(positions posin) {
        String[] list = new String[20];
        int idx;
        String[] listOut;
        posin.semTake();
        positionData actPos = posin.posDataFetchNext(true);
        for(idx = 0; actPos != null ; actPos = posin.posDataFetchNext(false), idx++) {
            list[idx] = actPos.longTicker;
        }
        posin.semGive();
        listOut = new String[idx];
        listOut = list;
        
        return listOut;
    }
    
    public static int countLinesInFile(String filename) throws IOException {
        /*
            Count the number of lines in a text file and return that value.
         */
        LineNumberReader reader = new LineNumberReader(new FileReader(filename));
        int cnt = 0;
        String lineRead = "";
        while ((lineRead = reader.readLine()) != null) {
        }

        cnt = reader.getLineNumber();
        reader.close();
        return cnt + 1;
    }
    
    public static double truncate(double din){
        /*
        truncate with out rouding to two places.
        */
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        
        return(Double.valueOf(df.format(din)));
    }

    public static double truncate(double din, int places) {
        /*
        truncate with out rouding to number of places 
        */
        BigDecimal bd;
        if (din > 0) {
            bd = new BigDecimal(String.valueOf(din)).setScale(places, BigDecimal.ROUND_FLOOR);
        } else {
            bd = new BigDecimal(String.valueOf(din)).setScale(places, BigDecimal.ROUND_CEILING);
        }
        return Double.valueOf(bd.doubleValue());
    }
    
    public static String getMyWorkingDirectory(String replace, String replaceWith){
        String s = System.getProperty("user.dir");
        s = s.replaceFirst(replace, replaceWith);
        return s;
    }
    public static String getMyWorkingDirectory(){
        String s = System.getProperty("user.dir");        
        return s;
    }
    public static void setMyWorkingDirectory(String wdIn){
        System.setProperty("user.dir", wdIn);
    }
    
    public static long getDiffInDaysFromToday(Date datein){
        long difference;
        long differenceDates; 
        long d;
        Date dateToday = new Date();
        difference = Math.abs(datein.getTime() - dateToday.getTime());
        differenceDates = (difference / (24 * 60 * 60 * 1000));
        if (datein.getTime() < dateToday.getTime()){
            //means it's in the past
            differenceDates *= -1;
        }
        return differenceDates;
    }
    public static Date convertDate(String din){
        //DateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
		boolean timePresent = din.contains(":") ? true : false;
		DateFormat format;
		if(timePresent == true){
			format = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.ENGLISH);
		}else{
			format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
		}       
        Date date = null;
        try {
            date = format.parse(din);
        } catch (ParseException ex) {
            Logger.getLogger(myUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }
    public static long getDiffInDays(Date dateinBeg, Date dateinEnd){
        long difference;
        long differenceDates; 
        //long d;
        difference = Math.abs(dateinBeg.getTime() - dateinEnd.getTime());
        differenceDates = (difference / (24 * 60 * 60 * 1000));
        if (dateinEnd.getTime() < dateinBeg.getTime()){
            //means end is before begin
            differenceDates *= -1;
        }
        return differenceDates;
    }
    public static long getDiffInDays(String dateinBeg, String dateinEnd) {
		long difference;
		long differenceDates;
		long dDays;
		long dMins;
		Date beg = convertDate(dateinBeg);
		Date end = convertDate(dateinEnd);
		//long d;
		dDays = TimeUnit.MILLISECONDS.toDays(beg.getTime()) - TimeUnit.MILLISECONDS.toDays(end.getTime());
		difference = Math.abs(beg.getTime() - end.getTime());
		differenceDates = (difference / (24 * 60 * 60 * 1000));
		if (end.getTime() < beg.getTime()) {
			//means end is before begin so negate
			differenceDates *= -1;
		}
		return differenceDates;
	}
	public static long getDiffInMinutes(String dateinBeg, String dateinEnd) {
		
		long dMins;
		
		//dateinBeg = "20200625 09:30:00";
		//dateinEnd = "20200625 10:00:00";
		Date beg = convertDate(dateinBeg);
		Date end = convertDate(dateinEnd);
		//long d;
		dMins = TimeUnit.MILLISECONDS.toMinutes(beg.getTime()) - TimeUnit.MILLISECONDS.toMinutes(end.getTime());
		return dMins;
	}

	public static long[] getDiffInDaysHoursMinSecs(String dateinBeg, String dateinEnd) {
		long difference;
		long differenceDates;
		long dDays;
		long dHours;
		long dMins;
		long dSecs;
		long[] diffDaysHoursMinSecs = new long[4];
		long negate = 1;
		Date beg = convertDate(dateinBeg);
		Date end = convertDate(dateinEnd);
		//check if end is before begining, if so negate result...
		negate = (end.getTime() < beg.getTime()) ? -1 : 1;
		//long d;
		dDays = TimeUnit.MILLISECONDS.toDays(beg.getTime()) - TimeUnit.MILLISECONDS.toDays(end.getTime());
		dHours = TimeUnit.MILLISECONDS.toHours(beg.getTime()) - TimeUnit.MILLISECONDS.toHours(end.getTime());
		dMins = TimeUnit.MILLISECONDS.toMinutes(beg.getTime()) - TimeUnit.MILLISECONDS.toMinutes(end.getTime());
		dSecs = TimeUnit.MILLISECONDS.toSeconds(beg.getTime()) - TimeUnit.MILLISECONDS.toSeconds(end.getTime());
		diffDaysHoursMinSecs[0] = dDays * negate;
		diffDaysHoursMinSecs[1] = dHours * negate;
		diffDaysHoursMinSecs[2] = dMins * negate;
		diffDaysHoursMinSecs[3] = dSecs * negate;
		//difference = Math.abs(beg.getTime() - end.getTime());
		//differenceDates = (difference / (24 * 60 * 60 * 1000));
		if (end.getTime() < beg.getTime()) {
			//means end is before begin so negate
			dDays *= -1;
		}
		return diffDaysHoursMinSecs;
	}
	public static boolean withInRangeInclusive(int xin, int minin, int maxin){
		return ((xin >=minin) && (xin <=maxin));
	}
	public static boolean withInRangeExclusive(int xin, int minin, int maxin){
		return ((xin >minin) && (xin <maxin));
	}
}
