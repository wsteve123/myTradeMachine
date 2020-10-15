/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ibTradeApi.ibApi.quoteInfo;

import java.io.*;

/**
 *
 * @author walterstevenson
 */
public class positionStatistics {

    private class stats {

        int storeId;
        String ticker;
        int getQuoteError;
        float stockQMaxValue;
        float stockQMinValue;
        float optionQMaxValue;
        float optionQMinValue;
        float posProfitMax;
        float posProfitMin;
        /* added 1/27/2012 because IB gives -1 values
        when market is not open and this messes status window up.
        so we will remember and use these numbers when the market is
        closed.
         */
        public double slastBid = 0;
        public double slastAsk = 0;
        public double slastLast = 0;
        public double olastBid = 0;
        public double olastAsk = 0;
        public double olastLast = 0;
        public double olastValue = 0;
    }
    private Vector statStore;
    private int storeIdx;
    private int fetchIdx;
    private positionStatistics curPositionStats;
    
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    //homeDirectory = homeDirectory.replaceFirst("myTradeMachine", "activeTrader_Ib_1");
    //        "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";

    public positionStatistics() {
        statStore = new Vector(20, 5);
        storeIdx = 0;
        fetchIdx = 0;
        curPositionStats = this;
    }
    public int size() {
        return(storeIdx);
    }
    
    public void statVecInit() {
        statStore.clear();
        storeIdx = 0;
        fetchIdx = 0;
        
    }
    
    public positionStatistics(boolean empty) {
    }

    private void store(stats s, int location) {
        if (location < statStore.size()) {
            statStore.setElementAt(s, location);
        } else {
            statStore.insertElementAt(s, location);
        }

    }

    private stats fetchNext(boolean first) {
        if (first == true) {
            fetchIdx = 0;
        }
        if (fetchIdx < statStore.size()) {
            return ((stats) statStore.elementAt(fetchIdx++));
        } else {
            return (null);
        }
    }

    private stats search(String byTicker) {
        stats actStat;
        for (int lp = 0; lp < statStore.size(); lp++) {
            actStat = (stats) statStore.elementAt(lp);
            if (byTicker.equals(actStat.ticker)) {
                return actStat;
            }
        }
        return (null);
    }

    private stats getStatsCounts(String byTicker) {
        stats locStat;

        if ((locStat = search(byTicker)) != null) {
            return (locStat);
        } else {
            locStat = new stats();
            locStat.ticker = byTicker;
            locStat.storeId = storeIdx++;
            return (locStat);
        }
    }

    public double getStockAsk(String byTicker) {
        return (getStatsCounts(byTicker).slastAsk);
    }
    public double getStockBid(String byTicker) {
        return (getStatsCounts(byTicker).slastBid);
    }
    public double getStockLast(String byTicker) {
        return (getStatsCounts(byTicker).slastLast);
    }
    public double getOptionAsk(String byTicker) {
        return (getStatsCounts(byTicker).olastAsk);
    }
    public double getOptionBid(String byTicker) {
        return (getStatsCounts(byTicker).olastBid);
    }
    public double getOptionLast(String byTicker) {
        return (getStatsCounts(byTicker).olastLast);
    }
    public double getOptionValue(String byTicker) {
        return (getStatsCounts(byTicker).olastValue);
    }



    public void quoteErrors(String byTicker) {
        new quoteErrors(byTicker);
    }
    
    public void quoteErrors(String byTicker, quoteInfo quote) {
        /* check if quote error (null) exists and count it if so */
        if (quote == null) {
            new quoteErrors(byTicker);
        }
    }

    public void sQuoteMinMax(String byTicker, quoteInfo quote) {
        /* first check if quote error exists */
        if (quote != null) {
            /* no quote error so do stock range stat */
            new stockQRange(byTicker, quote);
        }else {
            /* cannot do stock range stat so count as quote error only*/
            quoteErrors(byTicker);
        }
            
    }

    public void oQuoteMinMax(String byTicker, quoteInfo quote) {
        /* first check if quote error exists */
        if (quote != null) {
            /* no quote error so do range stat */
            new optionQRange(byTicker, quote);
        }else {
            /* cannot do range stat so count as quote error only*/
            quoteErrors(byTicker);
        }
    }

    public void posProfitMinMax(String byTicker, float profit) {
            
        new posProfitRange(byTicker, profit);
   
    }
    
    private class quoteErrors extends positionStatistics {

        stats locStat;

        private quoteErrors(String ticker) {
            super(true);
            locStat = getStatsCounts(ticker);
            locStat.getQuoteError++;
            store(locStat, locStat.storeId);
        }
    }

    private class stockQRange extends positionStatistics {

        stats locStat;

        private stockQRange(String ticker, quoteInfo squote) {
            super(true);
            locStat = getStatsCounts(ticker);
            if ((locStat.stockQMinValue == 0) && (locStat.stockQMaxValue == 0)) {
                // must be first time call, set both the same to start.
                locStat.stockQMinValue = locStat.stockQMaxValue = (float)squote.value;

            } else {
                if (squote.value > locStat.stockQMaxValue) {
                    locStat.stockQMaxValue = (float)squote.value;
                }
                if (squote.value < locStat.stockQMinValue) {
                    locStat.stockQMinValue = (float)squote.value;
                }
            }
            /* new values to store because Ib gives wrong #s when market is closed */
            locStat.slastBid = squote.bid;
            locStat.slastAsk = squote.ask;
            locStat.slastLast = squote.last;
            store(locStat, locStat.storeId);
        }
    }

    private class optionQRange extends positionStatistics {

        stats locStat;

        private optionQRange(String ticker, quoteInfo oquote) {
            super(true);
            locStat = getStatsCounts(ticker);
            if ((locStat.optionQMinValue == 0) && (locStat.optionQMaxValue == 0)) {
                // must be first time call, set both the same to start.
                locStat.optionQMinValue = locStat.optionQMaxValue = (float)oquote.value;

            } else {
                if (oquote.value > locStat.optionQMaxValue) {
                    locStat.optionQMaxValue = (float)oquote.value;
                }
                if (oquote.value < locStat.optionQMinValue) {
                    locStat.optionQMinValue = (float)oquote.value;
                }
            }
            /* new values to store because Ib gives wrong #s when market is closed */
            locStat.olastBid = oquote.bid;
            locStat.olastAsk = oquote.ask;
            locStat.olastLast = oquote.last;
            locStat.olastValue = oquote.value;
            store(locStat, locStat.storeId);
        }
    }

        private class posProfitRange extends positionStatistics {

        stats locStat;

        private posProfitRange(String ticker, float profit) {
            super(true);
            locStat = getStatsCounts(ticker);
            if ((locStat.posProfitMin == 0) && (locStat.posProfitMax == 0)) {
                // must be first time call, set both the same to start.
                locStat.posProfitMin = locStat.posProfitMax = profit;

            } else {
                if (profit > locStat.posProfitMax) {
                    locStat.posProfitMax = profit;
                }
                if (profit < locStat.posProfitMin) {
                    locStat.posProfitMin = profit;
                }
            }
            store(locStat, locStat.storeId);
        }
    }
    
    public void displayStatistics() {
        new dispStats("Position Statistics");
    }

    private class dispStats extends JFrame {

        String[] columns = {
            "Ticker", "SQuote Errors", "OQuote Errors ", " StockQMax", "StockQMin", "OptionQMax ", "OptionQMin", "ProfitMax", "ProfitMin"
        };
        String[][] dataArr = new String[20][columns.length];
        JTable jt = null;
        JScrollPane pane = null;
        final int TICKER = 0;
        final int SQERRORS = 1;
        final int OQERRORS = 2;
        final int SQMAX = 3;
        final int SQMIN = 4;
        final int OQMAX = 5;
        final int OQMIN = 6;
        final int PROFMAX = 7;
        final int PROFMIN = 8;

        private dispStats(String title) {
            super(title);
            setSize(150, 150);
            
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    // dispose();
                    // System.exit( 0 );
                }
            });

            initTable();

            pack();
            setVisible(true);

        }

        private void initTable() {

            stats dispStats = null;
            int statNum;

            for (statNum = 0    , dispStats = curPositionStats.fetchNext(true); dispStats != null; dispStats = curPositionStats.fetchNext(false), statNum++) {
                dataArr[statNum][TICKER] = dispStats.ticker;
                dataArr[statNum][SQERRORS] = Integer.toString(dispStats.getQuoteError);
                dataArr[statNum][OQERRORS] = Integer.toString(dispStats.getQuoteError);
                dataArr[statNum][SQMAX] = Float.toString(dispStats.stockQMaxValue);
                dataArr[statNum][SQMIN] = Float.toString(dispStats.stockQMinValue);
                dataArr[statNum][OQMAX] = Float.toString(dispStats.optionQMaxValue);
                dataArr[statNum][OQMIN] = Float.toString(dispStats.optionQMinValue);
                dataArr[statNum][PROFMAX] = Float.toString(dispStats.posProfitMax);
                dataArr[statNum][PROFMIN] = Float.toString(dispStats.posProfitMin);
            }


            if (jt == null) {
                jt = new JTable(dataArr, columns);
            }
            if (pane == null) {
                pane = new JScrollPane(jt);
            }

            getContentPane().add(pane);
        }
    }
    
    
    public void statSaveToDisk(String fileName, boolean prompt) {
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        boolean split = false;
        String wrFileName = homeDirectory;
        stats tmpStat;
        int numOfPositions = 0;

        if (fileName == null) {
            wrFileName += "positions.stat";
        } else {
            String noExtentionFilename = fileName;
            noExtentionFilename = noExtentionFilename.substring(0, noExtentionFilename.indexOf("."));
            wrFileName += (noExtentionFilename+".stat");
        }

        try {
            fos = new FileOutputStream(wrFileName);
            dos = new DataOutputStream(fos);
            bow = new BufferedWriter(new OutputStreamWriter(fos));
            //init to fetch from begining..and get the first.
            tmpStat = curPositionStats.fetchNext(true);
            numOfPositions = curPositionStats.size();

            while ((numOfPositions > 0) && (tmpStat != null)) {
                bow.write(tmpStat.ticker);
                bow.newLine();
                bow.write(Float.toString(tmpStat.stockQMaxValue));
                bow.newLine();
                bow.write(Float.toString(tmpStat.stockQMinValue));
                bow.newLine();
                bow.write(Float.toString(tmpStat.optionQMaxValue));
                bow.newLine();
                bow.write(Float.toString(tmpStat.optionQMinValue));
                bow.newLine();
                bow.write(Float.toString(tmpStat.posProfitMax));
                bow.newLine();
                bow.write(Float.toString(tmpStat.posProfitMin));
                bow.newLine();
                /* 1/27/2012 new params to save Ib does not give good #s after market close
                   first stock values...
                 */
                bow.write(Double.toString(tmpStat.slastBid));
                bow.newLine();
                bow.write(Double.toString(tmpStat.slastAsk));
                bow.newLine();
                bow.write(Double.toString(tmpStat.slastLast));
                bow.newLine();
                /* now option values */
                bow.write(Double.toString(tmpStat.olastBid));
                bow.newLine();
                bow.write(Double.toString(tmpStat.olastAsk));
                bow.newLine();
                bow.write(Double.toString(tmpStat.olastLast));
                bow.newLine();
                bow.write(Double.toString(tmpStat.olastValue));
                bow.newLine();

                numOfPositions--;
                if (numOfPositions > 0) {
                    tmpStat = curPositionStats.fetchNext(false);
                }
            }
            bow.close();
            if(prompt == true) {
                commonGui.prMsg(curPositionStats.size() + " Status Position(s) Saved to File: " + wrFileName);
                
            }
            System.out.println(" Status Position(s) Saved to file:"+wrFileName);

        } catch (Exception e) {
            System.out.println("error writing text to: " + wrFileName + "(" + e + ").");
        }

    } /* statDataSaveToDisk */
    
       public  void statReadFromDisk(String fileName, boolean prompt) {

        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        boolean split = false;
        String rdFileName = homeDirectory;
        String tmpStr;
        stats tmpStat;
        
        if (fileName == null) {
            rdFileName += "positions.txt";
        } else {
            /* the filename in positions contains the extention (.dat) 
               in this case we work with out and add to end...so we need first start with no exention.
               so strip it out.
             */
            String noExtentionFilename = fileName;
            noExtentionFilename = noExtentionFilename.substring(0, noExtentionFilename.indexOf("."));
            rdFileName += noExtentionFilename+".stat";
        }

        statVecInit();
        try {
            fis = new FileInputStream(rdFileName);
            dis = new DataInputStream(fis);
            bir = new BufferedReader(new InputStreamReader(fis));
            while (((tmpStr = bir.readLine()) != null) && (!split)) {
                
                //read the ticker first.
                tmpStat = getStatsCounts(tmpStr);
                tmpStat.ticker = tmpStr;
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the stockQMax
                    tmpStat.stockQMaxValue = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the stockQMin
                    tmpStat.stockQMinValue = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the optioinQMax
                    tmpStat.optionQMaxValue = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the optioinQMin
                    tmpStat.optionQMinValue = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the MaxProfit
                    tmpStat.posProfitMax = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the MinProfit
                    tmpStat.posProfitMin = Float.parseFloat(tmpStr);
                } else {
                    split = true;
                }
                /* 1/27/2012 new params to save Ib does not give good #s after market close
                   first stock values...
                 */
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read slastBid
                    tmpStat.slastBid = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read slastAsk
                    tmpStat.slastAsk = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read slastAsk
                    tmpStat.slastLast = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read olastBid
                    tmpStat.olastBid = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read olastAsk
                    tmpStat.olastAsk = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read olastAsk
                    tmpStat.olastLast = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read olastAsk
                    tmpStat.olastValue = Double.parseDouble(tmpStr);
                } else {
                    split = true;
                }

                
                store(tmpStat, tmpStat.storeId);
                
            }
            if(prompt == true) {
                commonGui.prMsg(curPositionStats.size() + " Stats Position(s) Read from File: " + rdFileName);
            }
            bir.close();

        } catch (Exception e) {
            System.out.println("error reading text for: " + rdFileName + "(" + e + ").");
        }

    }
    
}





