/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;


import org.jfree.data.xy.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.*;

import java.io.*;
import tradeMenus.genComboBoxDialogForm;
import positions.positionData.positionStates;

/**
 *
 * @author walterstevenson
 */
//public class positionChart {

//}
public class positionChart {

    reader[] posReaders;
    writer[] posWriters;
   
    private positions actPositions = null;
    private Semaphore sem = null;
    private boolean pauseLogToFile = false;
    private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    
    private int numOfCharts = 0;
    private positionChart actCharts;
    /**
     * creates charting. stock and option quotes are
     * charted while at the same time the data is written to disk. 
     * Each position data is logged to file by ticker name (ticker.dat). 
     * The file logging will run for 3 days before overwritting the data again. 
     * The chart can then be read from the file for upto 3 days worth.
     *
     * @param title  the frame title.
     */
    public positionChart(positions allPosIn) {
        homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
        int posNum = 0;
        numOfCharts = 0;
        posReaders = new reader[20];
        reader tmpReader;
        posWriters = new writer[20];
        writer tmpWriter;
        positionData actPos;
        int numOfPositions;
        actCharts = this;
        

        actPositions = allPosIn;
        allPosIn.semTake();
        numOfCharts = numOfPositions = allPosIn.posDataVecSize();
        for (posNum = 0          , actPos = allPosIn.posDataFetchNext(true); posNum < numOfPositions; actPos = allPosIn.posDataFetchNext(false)) {
            //first do reader
            tmpReader = new reader(actPos.longTicker, actPos.getPosId());
            getLastStockOptionValues(tmpReader);
            posReaders[posNum] = tmpReader;
            //now writer
            tmpWriter = new writer(actPos.longTicker, actPos.getPosId());
            /* here we set the writers prev values with the last values stored in file */
            tmpWriter.sPrevValue = tmpReader.sval;
            tmpWriter.oPrevValue = tmpReader.oval;
            posWriters[posNum] = tmpWriter;
            
            posNum++;
        }
        
        allPosIn.semGive();
        
        sem = new Semaphore(1);

    }

    int findIndex(String tickerin) {

        for (int ix = 0; ix < numOfCharts; ix++) {
            if (tickerin.equals(posWriters[ix].associatedTicker)) {
                return (ix);
            }
        }
        return (-1);

    } /* findIndex */
    class reader {

        private FileInputStream fis;
        private BufferedReader bir;
        private DataInputStream dis;
        private String rdFilename;
        private String associatedTicker;
        private int posId;
        private float sval;
        private float oval;
        private chart rdToChart = null;
        // this is the x on the chart for time in ticks.
        private float tick;

        reader(String ticker, int id) {
            /* the filename in positions contains the extention (.dat) 
               in this case we work with out and add to end...so we need first start with no exention.
               so strip it out.
             */
            String noExtentionFilename = actPositions.getPositionFileName();
            noExtentionFilename = noExtentionFilename.substring(0, noExtentionFilename.indexOf("."));
            
            rdFilename = homeDirectory + noExtentionFilename + "."+ticker+".dat";
            associatedTicker = ticker;
            posId = id;

            // read the data to this chart.
            rdToChart = null;
            tick = 0;
            sval = 0;
            oval = 0;
            
        }
        void open(){
            try {
                fis = new FileInputStream(rdFilename);
                dis = new DataInputStream(fis);
                bir = new BufferedReader(new InputStreamReader(fis));
            } catch (Exception e) {
                System.out.println("error reading text from: " + rdFilename + "(" + e + ").");
            }
            
        }
        void close() {
            try {
                bir.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        void bumpTick() {
            tick += 20;
        }
        void createChart() {
            rdToChart = new chart(associatedTicker);
        }
    }

    class writer {

        private FileOutputStream fos;
        private BufferedWriter bow;
        private DataOutputStream dos;
        private String wrFilename;
        private String associatedTicker;
        private int posId;
        float sPrevQuote;
        float sPrevValue;
        float oPrevQuote;
        float oPrevValue;
        int wrCount;
        private boolean chartingEnabled;
        private chart wrToChart;
        // this is the x on the chart for time in ticks.
        private float tick;
        
        writer(String ticker, int id) {
            /* the filename in positions contains the extention (.dat) 
               in this case we work with out and add to end...so we need first start with no exention.
               so strip it out.
             */
            String noExtentionFilename = actPositions.getPositionFileName();
            noExtentionFilename = noExtentionFilename.substring(0, noExtentionFilename.indexOf("."));
            
            wrFilename = homeDirectory + noExtentionFilename + "."+ ticker+".dat";
            associatedTicker = ticker;
            posId = id;

            sPrevQuote = 0;
            sPrevValue = 0;
            oPrevQuote = 0;
            oPrevValue = 0;
            wrCount = 0;
            // write to file and update this chart
            wrToChart = null;
            chartingEnabled = false;
            tick = 0;
            
        }

        
        void open() {
            
            try {
                
                bow = new BufferedWriter(new FileWriter(wrFilename, true /*append*/));
/*
                fos = new FileOutputStream(wrFilename);
                dos = new DataOutputStream(fos);
                bow = new BufferedWriter(new OutputStreamWriter(fos));
*/

            } catch (Exception e) {
                System.out.println("error writing text to: " + wrFilename + "(" + e + ").");
            }
            
        }
        void close() {
            try {
                bow.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        void createChart() {
            wrToChart = new chart(associatedTicker);
        }
        boolean chartingEnabled() {
            return chartingEnabled;
        }

        void setChartingEnabled(boolean en) {
            chartingEnabled = en;
        }

        void bumpTick() {
            tick += 20;
        }
    }

    void getLastStockOptionValues(reader readin) {
        /* read in the last sValue and oValue from the log file.
         * we need this for interrupted charting. These are values
         * we should use to start the new chart segment.
         */
        File rf;
        long fSizeInBytes = 0;
        long numOfRealValues = 0;
        boolean goRead = false;
        String tmpStr = "";
        int sz = 0;
        boolean split = false;
        
        rf = new File(readin.rdFilename);
        
        readin.open();
        if (rf.exists() == true) {
            fSizeInBytes = rf.length();
            numOfRealValues = (fSizeInBytes / (Float.SIZE / 8));
            System.out.println("file size is :" + numOfRealValues + " as in number of floats.");
            goRead = true;
        }
        if (goRead == true) {
            try {
                while ((!split) && (sz <= numOfRealValues)) {
                    if ((!split) && (tmpStr = readin.bir.readLine()) != null) {
                        //read the stock value
                        readin.sval = Float.parseFloat(tmpStr);

                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = readin.bir.readLine()) != null) {
                        //now option value
                        readin.oval = Float.parseFloat(tmpStr);

                    } else {
                        split = true;
                    }
                    if (!split) {
                        sz++;
                    }

                }
                readin.bir.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("the last two values are: " + readin.sval + " stock, and "+ readin.oval+" option.");
        }
    }
    
    void logFromFile(positionData pos) {
        String tmpStr;
        boolean split = false;
        BufferedReader bir;
        int index;
        reader actReader;

        if ((index = findIndex(pos.longTicker)) == -1) {
            System.out.println("positionChart: logFromFile, index not found!");
            return;
        }
        actReader = posReaders[index];
        actReader.tick = 0;
        
        actReader.createChart();
        this.sem.acquire();
        actReader.open();
        bir = actReader.bir;
        while (!split) {
            try {
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //read the stock value
                    actReader.sval = Float.parseFloat(tmpStr);

                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now option value
                    actReader.oval = Float.parseFloat(tmpStr);

                } else {
                    split = true;
                }
                if (split == true) {
                    actReader.close();
                }
            } catch (Exception e) {
                System.out.println("error reading text for: " + actReader.rdFilename + "(" + e + ").");
            }
            if (!split) {
                actReader.rdToChart.stockSeries.addOrUpdate(actReader.tick, actReader.sval);
                actReader.rdToChart.optionSeries.addOrUpdate(actReader.tick, actReader.oval);
                actReader.rdToChart.gainSeries.addOrUpdate(actReader.tick, (actReader.oval + actReader.sval));
                
                actReader.bumpTick();
            }
        }
        this.sem.release();

    }
    public void setPauseToRun(boolean pauseIt) {
        pauseLogToFile = pauseIt;
    }

    public boolean getPauseToRun() {
        return (pauseLogToFile);
    }

    void logToFile(positionData pos, float squote, float oquote) {
        float sValue;
        float oValue;
        int index;
        positionAdjustment actAdj;
        BufferedWriter bow;
        writer actWriter;
        reader actReader;

        /* check for reasons to exit prematurely */
        if ((pauseLogToFile == true) || (chartExists(pos) == false) || (pos.currentState != positionStates.ACTIVE)) {
            return;
        }
        if ((index = findIndex(pos.longTicker)) == -1) {
            System.out.println("positionChart: logToFile, index not found!");
        }
        actWriter = posWriters[index];
       

        if (actWriter.sPrevQuote == 0) {
            actWriter.sPrevQuote = squote;
        }
        if (actWriter.oPrevQuote == 0) {
            actWriter.oPrevQuote = oquote;
        }
        actAdj = (positionAdjustment) pos.posAdjGet(pos.posAdjustments() - 1);
        sValue = (actAdj.sharesLong * (squote - actWriter.sPrevQuote)) + actWriter.sPrevValue;
        oValue = (actAdj.sharesShort * (oquote - actWriter.oPrevQuote) * 100) + actWriter.oPrevValue;
        actWriter.sPrevValue = sValue;
        actWriter.oPrevValue = oValue;
        actWriter.sPrevQuote = squote;
        actWriter.oPrevQuote = oquote;
        this.sem.acquire();
        actWriter.open();
        bow = actWriter.bow;
        
        try {
            bow.write(Float.toString(sValue));
            bow.newLine();

            bow.write(Float.toString(oValue));
            bow.newLine();
            actWriter.wrCount++;
        } catch (Exception e) {
            System.out.println("error writing text to: " + actWriter.wrFilename + "(" + e + ").");
        }
        try {
            bow.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.sem.release();
        if (actWriter.chartingEnabled() == true) {
            actWriter.wrToChart.stockSeries.addOrUpdate(actWriter.tick, sValue);
            actWriter.wrToChart.optionSeries.addOrUpdate(actWriter.tick, oValue);
            actWriter.wrToChart.gainSeries.addOrUpdate(actWriter.tick, (oValue + sValue));
            actWriter.bumpTick();
        }
    }

    private class chart {

        XYSeries stockSeries;
        XYSeries optionSeries;
        XYSeries gainSeries;
        XYDataset xyDatasetStock;
        XYDataset xyDatasetOption;
        XYSeriesCollection dataset;
        JFreeChart chart;
        ChartFrame frame1;

        private chart(String ticker) {
            stockSeries = new XYSeries("Stock Value");
            optionSeries = new XYSeries("Option Value");
            gainSeries = new XYSeries("Gain Value");
            
            dataset = new XYSeriesCollection();
            dataset.addSeries(stockSeries);
            dataset.addSeries(optionSeries);
            dataset.addSeries(gainSeries);
            
//            xyDatasetStock = new XYSeriesCollection(stockSeries);
            
//            xyDatasetOption = new XYSeriesCollection(optionSeries);
            chart = ChartFactory.createXYLineChart("Stock vs Option Value", "Time(secs)", "Position Value",
                    dataset, PlotOrientation.VERTICAL, true, true, false);
            
            frame1 = new ChartFrame(ticker+" Chart", chart);
            frame1.setVisible(true);
            frame1.setSize(300, 300);

        }

        
    }
    
    public void dispChart() {
        String actPosStr;
        positionData actPosition;
        String[] posList = null;
        posList = myUtils.fillTickerList(actPositions);
        genComboBoxDialogForm posCb; 
        
        if (actPositions.posDataEmpty() == false) {
            posCb = new genComboBoxDialogForm(new javax.swing.JFrame(), true, "Select Ticker", posList);
            posCb.setVisible(true);
            actPosStr = posCb.getSelection();
            if ((actPosition = actPositions.posDataSearch(actPosStr.toUpperCase())) != null) {
                logFromFile(actPosition);
                //commonGui.prMsg("Charting Completed.");
            } else {
                commonGui.prMsg("position not found.");
            }
            /*
            actPosStr = commonGui.getUserInput("Enter Long Ticker Of Position to Chart", "rimm");
            if ((actPosition = actPositions.posDataSearch(actPosStr.toUpperCase())) != null) {
                logFromFile(actPosition);
                commonGui.prMsg("Charting Completed.");
            } else {
                commonGui.prMsg("position not found.");
            }
            */
        } else {
            commonGui.prMsg("There are no positions to chart.");
        }
    }
    private boolean chartExists(positionData pos) {
        int ix;
        /* check for posId to see if we have this chart. 
         * use posReaders, should be identicle to posWriters.
         */
        for (ix = 0; ix <  this.numOfCharts; ix++) {
            if (pos.getPosId() == posReaders[ix].posId) {
                /* found the id, chart exists! */
                return(true);
            }
            
        }
        /* went through all and found no match! */
        return(false);
    }

}
