/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import positions.commonGui;
import tradeMenus.slopeDefs;


/**
 *
 * @author earlie87
 */
public class displayStocksWthFilter extends javax.swing.JDialog {
    
    final String FIFTY_DAY = "50 Day";
    final String HUNDRED_DAY = "100 Day";
    final String TWO_HUNDRED_DAY = "200 Day";
    //result combo box commands user can selected
    final String CLR_ALL = "ClearAll";
    final String DISP_ALL = "DisplayAll";
    final String ADD_TO_TRADE_LIST = "AddToTradeList";
    final int NUM_OF_COMMANDS = (3 - 1);
    final int FIRST_TICKER = NUM_OF_COMMANDS + 1;
    int dispCnt = 0;
    private final ibApi actIbApi = ibApi.getActApi();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    private boolean userPaused = false;
    File currentFolder = new File(System.getProperty("user.dir") + "/src/supportFiles/");
    File[] listOfFiles = currentFolder.listFiles();
    displayOptionChainDialogForm optionChainDialog = null;
    /**
     * Creates new form displayStocksWthFilter
     */
    public displayStocksWthFilter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        indexComboBox.removeAllItems();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith("sym")) {
                System.out.println("File " + listOfFiles[i].getName());
                indexComboBox.addItem(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        if (indexComboBox.getItemCount() > 0) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString();
            selectedExchangeInt = indexComboBox.getSelectedIndex();
        };
        selectedMovingAverage = movingAveComboBox.getSelectedItem().toString();
        
        selectRsiGreaterTextField.setText(Integer.toString(selectedShowRsiGreaterThan));
        selectRsiLessTextField.setText(Integer.toString(selectedShowRsiLessThan));
       // slopeFinderTextField.setText(Integer.toString(selectedSlopePercent));
        resultsComboBox.removeAllItems();
        resultsComboBox.addItem(DISP_ALL);
        resultsComboBox.addItem(CLR_ALL);       
        resultsComboBox.addItem(ADD_TO_TRADE_LIST);
        dayWeekLabel.setText("Day");
    }
    
    final int MA_50DAY  = slopeDefs.MA_50DAY;
    final int MA_100DAY = slopeDefs.MA_100DAY;
    final int MA_200DAY = slopeDefs.MA_200DAY;
    final int MA_10DAY  = slopeDefs.MA_10DAY;
    
    //private ibApi actIbApi = ibApi.getActApi();
    //private ibApi actHistory = ibApi.getActHistorySlope();
    String selectedExchangeStr = null;
    String selectedSlopeCriteriaStr = null;
    int selectedSlopeCriteriaInt = 0;
    int selectedExchangeInt = 0;
    String selectedResultToViewStr = null;
    int selectedResultToViewInt = 0;
    
    final int SLOPE_OFF = slopeDefs.SLOPE_OFF;
    final int SLOPE_CHANGE = slopeDefs.SLOPE_CHANGE;
    final int SLOPE_UPTREND = slopeDefs.SLOPE_UPTREND;
    final int SLOPE_DNTREND = slopeDefs.SLOPE_DNTREND;
    final int SLOPE_LONGEST = slopeDefs.SLOPE_LONGEST;
    final int SLOPE_SAVE_ALL = slopeDefs.SLOPE_SAVE_ALL;
    final int SLOPE_FILTERED = slopeDefs.SLOPE_FILTERED;
    final int SLOPE_BULLBEAR_CROSS = slopeDefs.SLOPE_BULLBEAR_CROSS;
    final int SLOPE_SET_MA_WINDOW = slopeDefs.SLOPE_SET_MA_WINDOW;
    
    public  SlopesFound allSlopesFound;
    String selectedMovingAverage = null;
    boolean selectedAboveMa = true;
    boolean selectedBelowMa = false;
    boolean selectedCurrentlyWithinAndFunction = false;
    boolean selectedRsiAndFunction = false;
    // this is a group that work together:
    boolean SelCurrentlyWithinAND = false;
    boolean selCurrentlyWithinOR = false;
    boolean selCurrentRsiAND = false;
    boolean selCurrentRsiOR = false;
    int selectedNumericValue = 0;
    // end group; the four booleans are converted 
    // to a numberic value for easy case statement       
    int selectedPercentOf50Ma = 0;
    int selectedPercentOf100Ma = 0;
    int selectedPercentOf200Ma = 0;
    int selectedCurrentlyAbove50Ma = 0;
    int selectedCurrentlyAbove100Ma = 0;
    int selectedCurrentlyAbove200Ma = 0;
    int selectedCurrentlyBelowMa = 0;
    int selectedShowRsiGreaterThan = 0;
    int selectedShowRsiLessThan = 0;
    int selectedMoversPercent = 0;
    // -1 is default since 0 is valid slope search..
    int selectedSlopePercent = -1;
    
    //State definition
    private final int S0_DIS_ALL = 0;
    private final int S1_DIS_CURRENTWITHIN_EN_RSI = 1;
    private final int S2_EN_RSI_AND_HIST_DIS_CURRENTWITHIN = 2;
    private final int S3_DIS_CURRENTWITHIN_NOT_ALLOWED = 3;
    private final int S4_EN_CURRENTWITHIN_DIS_RSI = 4;
    private final int S5_EN_CURRENTWITHIN_EN_RSI = 5;
    private final int S6_EN_CURRENTWITHIN_EN_RSI_AND_HIST = 6;
    private final int S7_EN_CURRENTWITHIN_NOT_ALLOWED = 7;
    private final int S8_EN_CURRENTWITHIN_AND_HIST_DIS_RSI = 8;
    private final int S9_EN_CURRENTWITHIN_AND_HIST_EN_RSI = 9;
    private final int S10_EN_CURRENTWITHIN_AND_HIST_AND_RSI = 10;
    private final int S11_EN_CURRENTWITHIN_AND_HIST_NOT_ALLOWED = 11;
    private final int S12_NOT_ALLOWED_DIS_RSI = 12;
    private final int S13_NOT_ALOWED_EN_RSI = 13;
    private final int S14_NOT_ALLOWED_EN_RSI_AND_HIST = 14;
    private final int S15_NOT_ALLOWED_NOT_ALLOWED = 15;
    
    String selectedTicker = null;
    ioRdTextFiles rdTickerFile = null;
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    String actTickersFile = null;
    readTickerFile exchangeData = null;
    boolean userTicker = false;
    boolean userSlectedAppendToTradeListFile = false;
    String userSelectedLongShort = slopeDefs.oBiasLongStr;
    //one shot. user selected a particular ticker...
    boolean userOneShot = false;
    runFilterThread rfThread = null;
    resultSummary[] results;
    int resCnt = 0;
    String searchCriteriaString;
    selectionLogic userSelectedLogic = new selectionLogic();
    int numberOfTickers = 0;
    TradeList tradeList = new TradeList();
    String todaysDate = null;
    boolean userSelectedDayWeek = false;
    //Tail criteria:
    int userSelectedGapUpPercent = 0;    
    int userSelectedGapUpTail = 0;
    int userSelectedGapDnPercent = 0;
    int userSelectedGapDnTail = 0;
     //outer class in slopeAnalysis..
    slopeAnalysis saOuter = new slopeAnalysis();
    
    public slopeAnalysis.MaWindowSz maWindowSizes;
    public slopeAnalysis.BullBearCross bullBearCross = saOuter.new BullBearCross();
    public slopeAnalysis.GapUpDnTail gapUpDnTail = saOuter.new GapUpDnTail();
    
    public class resultSummary {

        String ticker;
        double fiftyDayMa;
        double hundredDayMa;
        double twoHundredDayMa;
        double currentlyWithin50DayMa;
        double currentlyWithin100DayMa;
        double currentlyWithin200DayMa;
    }
    public class TradeList {
        
        List<String> list = new ArrayList<String>();
        public void addOne(String ticker){
            list.add(ticker);
        }
        public void tradeList(){
               
        }
        public String returnStr(int idx){
            return(list.get(idx));
        }
        public int findOne(String ticker){
            int idx = 0;
            int retIdx = 0;
            boolean found = false;
            for (idx = 0; (found == false) && (idx < list.size()); idx++){
                if (ticker == list.get(idx)) {
                    found = true;
                    retIdx =idx;
                }
            }
            if (found == true){
                return(retIdx);
            }else{
                return -1;
            }
        }
        public void removeAll(){
            list.removeAll(list);
        }
        public void removeOne(String remTicker){
            list.remove(remTicker);
        }
        public int getSz(){
            return(list.size());
        }
        public void displayAll(){
            for(int idx = 0; idx < list.size(); idx++){
                System.out.println("\n" + list.get(idx));
            }
        }
        public void wrToFile(boolean append){
             ioWrTextFiles wrFile;
    
            if (list.size() > 0) {
                wrFile = new ioWrTextFiles("slopeTraderList1.txt", append);
                for (int idx = 0; idx < list.size(); idx++) {
                    System.out.println("\n" + list.get(idx));
                    wrFile.write(list.get(idx));
                }
                wrFile.closeWr();
                System.out.println("wrote to file.");
            }else{
                System.out.println("list was empty? Did not write to file..");
            }

        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel19 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        slopeFinderComboBox = new javax.swing.JComboBox();
        DeleteButton = new javax.swing.JButton();
        resultsComboBox = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        displaySlopeButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        selectedTodaysMoversPercentTextField = new javax.swing.JTextField();
        todaysMoversOrCheckBox = new javax.swing.JCheckBox();
        todaysMoversAndCheckBox = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        enterTickerCheckBox = new javax.swing.JCheckBox();
        indexComboBox = new javax.swing.JComboBox();
        tickerTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        historicalBelowCheckBox = new javax.swing.JCheckBox();
        MAPercentTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        movingAveComboBox = new javax.swing.JComboBox();
        dayWeekLabel = new javax.swing.JLabel();
        dayWeekCheckBox = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        currentlyTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        currentlyWithinAndCheckBox = new javax.swing.JCheckBox();
        currentlyWithinOrCheckBox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        filterByTailPanel = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        gapUpTextField = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        gapUpTailTextField = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        gapDnTextField = new javax.swing.JTextField();
        gapDnTailTextField = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        displayTailButton = new javax.swing.JButton();
        tailAndCheckBox = new javax.swing.JCheckBox();
        tailOrCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        selectRsiLessTextField = new javax.swing.JTextField();
        selectRsiGreaterTextField = new javax.swing.JTextField();
        currentRsiAndCheckBox = new javax.swing.JCheckBox();
        currentRsiOrCheckBox = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsTextArea = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        searchCriteriaLable = new javax.swing.JLabel();
        runStopButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        shortCheckBox = new javax.swing.JCheckBox();
        appendTradeListFileCheckBox = new javax.swing.JCheckBox();
        setDefaultButton = new javax.swing.JButton();
        invokeOptionChainButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search For Stocks With Specific Criteria");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Slope Finder"));

        slopeFinderComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "OFF", "Trend Change", "UpTrend", "DnTrend", "Longest Trend", "ShowAll", "Filtered", "BullBearCross", "SetMaWindowSz", " " }));
        slopeFinderComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slopeFinderComboBoxActionPerformed(evt);
            }
        });

        DeleteButton.setText("Delete");
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });

        resultsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resultsComboBoxActionPerformed(evt);
            }
        });

        jLabel20.setText("Results");

        displaySlopeButton.setText("Display");
        displaySlopeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displaySlopeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resultsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(DeleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(displaySlopeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(jLabel20)
                    .addComponent(slopeFinderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(slopeFinderComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel20))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(resultsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(displaySlopeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DeleteButton)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Include Today's Movers:\n"));

        jLabel3.setText("Enter %");

        selectedTodaysMoversPercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedTodaysMoversPercentTextFieldActionPerformed(evt);
            }
        });

        todaysMoversOrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                todaysMoversOrCheckBoxActionPerformed(evt);
            }
        });

        todaysMoversAndCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                todaysMoversAndCheckBoxActionPerformed(evt);
            }
        });

        jLabel12.setText("And OR");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectedTodaysMoversPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(todaysMoversAndCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(todaysMoversOrCheckBox)
                .addGap(43, 43, 43))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedTodaysMoversPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(todaysMoversOrCheckBox)
                    .addComponent(todaysMoversAndCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Ticker Source:"));

        jLabel1.setText("Select Exchange");

        enterTickerCheckBox.setText("Enter Ticker");
        enterTickerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterTickerCheckBoxActionPerformed(evt);
            }
        });

        indexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NasdaqQ1", "NasdaqQ2", "NasdaqQ3", "NasdaqQ4", "NyseQ1", "NyseQ2", "NyseQ3", "NyseQ4", "NyseQ5", "NyseQ6", "NyseQ7", "NyseQ8", "WATCH1", "SnP1", "SnP2", "Dow" }));
        indexComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexComboBoxSelectIndexComboBoxActionPerformed(evt);
            }
        });

        tickerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tickerTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("OR");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(tickerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(enterTickerCheckBox)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enterTickerCheckBox))
                .addGap(4, 4, 4)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tickerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Enter MA Percentages"));

        jLabel11.setText("MA Historical %");

        historicalBelowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historicalBelowCheckBoxActionPerformed(evt);
            }
        });

        MAPercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MAPercentTextFieldActionPerformed(evt);
            }
        });

        jLabel10.setText("Click for Below");

        jLabel4.setText("Select MA");

        movingAveComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "50 Day", "100 Day", "200 Day", " " }));
        movingAveComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movingAveComboBoxActionPerformed(evt);
            }
        });

        dayWeekLabel.setText("day/wk");

        dayWeekCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dayWeekCheckBoxActionPerformed(evt);
            }
        });

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Include Currently Within:"));

        currentlyTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentlyPercentTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Enter %");

        currentlyWithinAndCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentlyWithinAndCheckBoxActionPerformed(evt);
            }
        });

        currentlyWithinOrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentlyWithinOrCheckBoxActionPerformed(evt);
            }
        });

        jLabel7.setText("And OR");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(currentlyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(currentlyWithinAndCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(currentlyWithinOrCheckBox))
                    .addComponent(jLabel7))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentlyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(currentlyWithinAndCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(currentlyWithinOrCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(movingAveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel8Layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jLabel4)))
                                .addGap(6, 6, 6)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dayWeekCheckBox)
                                    .addComponent(dayWeekLabel)))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel10)))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MAPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(historicalBelowCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4))
                            .addComponent(dayWeekLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MAPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(movingAveComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dayWeekCheckBox))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(historicalBelowCheckBox))))))
        );

        filterByTailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Include Tail Parameters "));
        filterByTailPanel.setToolTipText("Define Search Critera By Tail");

        jLabel18.setText("Up2Dn:");

        jLabel23.setText("gapUp%:");

        gapUpTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gapUpTextFieldActionPerformed(evt);
            }
        });

        jLabel24.setText("Tail%:");

        gapUpTailTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gapUpTailTextFieldActionPerformed(evt);
            }
        });

        jLabel22.setText("Dn2Up:");

        jLabel25.setText("gapDn%:");

        gapDnTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gapDnTextFieldActionPerformed(evt);
            }
        });

        gapDnTailTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gapDnTailTextFieldActionPerformed(evt);
            }
        });

        jLabel26.setText("Tail%:");

        jCheckBox1.setText("IncludeHistory");

        displayTailButton.setText("DispHistory");
        displayTailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayTailButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("And OR");

        javax.swing.GroupLayout filterByTailPanelLayout = new javax.swing.GroupLayout(filterByTailPanel);
        filterByTailPanel.setLayout(filterByTailPanelLayout);
        filterByTailPanelLayout.setHorizontalGroup(
            filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterByTailPanelLayout.createSequentialGroup()
                        .addComponent(displayTailButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(filterByTailPanelLayout.createSequentialGroup()
                        .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel18))
                            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel24))
                                .addGap(12, 12, 12)
                                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(gapUpTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                    .addComponent(gapUpTailTextField)))
                            .addComponent(jCheckBox1)
                            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel25))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gapDnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(gapDnTailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(17, Short.MAX_VALUE))))
            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(filterByTailPanelLayout.createSequentialGroup()
                        .addComponent(tailAndCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tailOrCheckBox)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        filterByTailPanelLayout.setVerticalGroup(
            filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterByTailPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(gapUpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(gapUpTailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(gapDnTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(gapDnTailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayTailButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(filterByTailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tailAndCheckBox)
                    .addComponent(tailOrCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Include RSI:"));

        jLabel8.setText("show RSI > :");

        jLabel9.setText("show RSI < :");

        selectRsiLessTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRsiLessTextFieldActionPerformed(evt);
            }
        });

        selectRsiGreaterTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRsiGreaterTextFieldActionPerformed(evt);
            }
        });

        currentRsiAndCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentRsiAndCheckBoxActionPerformed(evt);
            }
        });

        currentRsiOrCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentRsiOrCheckBoxActionPerformed(evt);
            }
        });

        jLabel13.setText("And OR");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addComponent(selectRsiGreaterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectRsiLessTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(currentRsiAndCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentRsiOrCheckBox)))))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectRsiGreaterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectRsiLessTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(currentRsiAndCheckBox)
                    .addComponent(currentRsiOrCheckBox)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtered Results:"));

        resultsTextArea.setColumns(20);
        resultsTextArea.setRows(5);
        jScrollPane1.setViewportView(resultsTextArea);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("User Search Criteria:"));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(searchCriteriaLable, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchCriteriaLable, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 589, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addContainerGap())
        );

        runStopButton.setText("Run/Stop");
        runStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runStopButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        pauseButton.setText("Pause/Resume");
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(runStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(77, 77, 77)
                        .addComponent(closeButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runStopButton)
                    .addComponent(closeButton)
                    .addComponent(pauseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        shortCheckBox.setText("CheckForShort");
        shortCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shortCheckBoxActionPerformed(evt);
            }
        });

        appendTradeListFileCheckBox.setText("appendToTradeListFile");
        appendTradeListFileCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendTradeListFileCheckBoxActionPerformed(evt);
            }
        });

        setDefaultButton.setText("SetDefault");
        setDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDefaultButtonActionPerformed(evt);
            }
        });

        invokeOptionChainButton.setText("OptonChain");
        invokeOptionChainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invokeOptionChainButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filterByTailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(invokeOptionChainButton)
                                    .addComponent(setDefaultButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(shortCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(appendTradeListFileCheckBox))
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(180, 180, 180)
                        .addComponent(jLabel19))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(9, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addGap(17, 17, 17)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(shortCheckBox)
                                    .addComponent(appendTradeListFileCheckBox))
                                .addGap(14, 14, 14)
                                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filterByTailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(setDefaultButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(invokeOptionChainButton)))))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void indexComboBoxSelectIndexComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexComboBoxSelectIndexComboBoxActionPerformed
        // TODO add your handling code here:
        /* get the user selected exhange to later search in */
        if (indexComboBox.getItemCount() > 0) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString();
            selectedExchangeInt = indexComboBox.getSelectedIndex();
        };

        System.out.println("selected index is: " + selectedExchangeStr);
        System.out.println("selected index idx is : " + selectedExchangeInt);
    }//GEN-LAST:event_indexComboBoxSelectIndexComboBoxActionPerformed

    private void movingAveComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movingAveComboBoxActionPerformed
        // TODO add your handling code here:
        if (movingAveComboBox.getItemCount() > 0) {
            selectedMovingAverage = movingAveComboBox.getSelectedItem().toString();
            if (selectedMovingAverage.equals(FIFTY_DAY)) {
                MAPercentTextField.setText(Integer.toString(selectedPercentOf50Ma));
                currentlyTextField.setText(Integer.toString(selectedCurrentlyAbove50Ma));
            } else if (selectedMovingAverage.equals(HUNDRED_DAY)) {
                MAPercentTextField.setText(Integer.toString(selectedPercentOf100Ma));
                currentlyTextField.setText(Integer.toString(selectedCurrentlyAbove100Ma));
            } else if (selectedMovingAverage.equals(TWO_HUNDRED_DAY)) {
                MAPercentTextField.setText(Integer.toString(selectedPercentOf200Ma));
                currentlyTextField.setText(Integer.toString(selectedCurrentlyAbove200Ma));
            }

        };
        System.out.println("selected MA is: " + selectedMovingAverage);

    }//GEN-LAST:event_movingAveComboBoxActionPerformed

    private void MAPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MAPercentTextFieldActionPerformed
        // TODO add your handling code here:
        if (selectedMovingAverage.equals(FIFTY_DAY)) {
            selectedPercentOf50Ma = Integer.valueOf(MAPercentTextField.getText());
        } else if (selectedMovingAverage.equals(HUNDRED_DAY)) {
            selectedPercentOf100Ma = Integer.valueOf(MAPercentTextField.getText());
        } else if (selectedMovingAverage.equals(TWO_HUNDRED_DAY)) {
            selectedPercentOf200Ma = Integer.valueOf(MAPercentTextField.getText());
        }
    }//GEN-LAST:event_MAPercentTextFieldActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        System.out.println("\n cancelChainStreams in close.");
        actChain.cancelChainStreams();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    public SlopesFound getActAllSlopes(){
        return allSlopesFound;
    }
    public class selectionLogic {
        //boolean positions in array.
        private final int selCurWithinAND = 3;
        private final int selCurWithinOR = 2;
        private final int selCurRsiAND = 1;
        private final int selCurRsiOR = 0;
        
        /*
            selCurWithinAnd     selCurWithinOR  selCurRsiAND    selCurRsiOR     result
                0                   0               0               0           Disable both.
                0                   0               0               1           Current within values are disabled.
                                                                                Show RSI values if criteria is met.
                0                   0               1               0           Show RSI values if criteria is met AND the
                                                                                historical 50/100/200 data if
                                                                                criteria is met. Current-within 
                                                                                values are disabled.
                0                   0               1               1           Current within values are disabled.
                                                                                NOT allowed,
                0                   1               0               0           Show current-within values if criteria met. Disable RSI.
                0                   1               0               1           Show current-within values if criteria met.
                                                                                Show RSI values if criteria is met.
                0                   1               1               0           show current-within values 
                                                                                if criteria is met. Show RSI if criteria is 
                                                                                met AND historical if criteria is met.
                0                   1               1               1           Show current-within values 
                                                                                if criteria is met. NOT allowed.
                1                   0               0               0           Show Current-within values if
                                                                                criteria is met AND the historical 50/100/200 if
                                                                                criteria is met. RSI is disabled.
                1                   0               0               1           Show Current within values IF
                                                                                criteria is met AND the historical 50/100/200
                                                                                if criteria is met. Show RSI values 
                                                                                if criteria is met.
                1                   0               1               0           Show Current within values if
                                                                                criteria is met AND the historical 50/100/200
                                                                                if criteria is met AND RSI if criteria is met.
                1                   0               1               1           Show Current within values if
                                                                                criteria is met AND the historical 50/100/200
                                                                                if criteria is met. NOT allowed. 
                1                   1               0               0           NOT allowed. RSI is disabled.
                1                   1               0               1           NOT allowed. Show RSI values 
                1                   1               1               0           NOT allowed. Show RSI values if criteria is met AND
                                                                                historical 50/100/200 data if
                                                                                criteria is met.
                1                   1               1               1           NOT allowed.
        */
        
        private final int TBL_SZ = 4;
        
        boolean[] selBitMap = {false,false,false,false};        
        public int selBitMapIntValue = 0;
        
        public selectionLogic(){
            clearSelBitMap();
            selBitMapIntValue = 0;
            
        }
        public void setCurrentWithinOR(boolean setwith){
            selBitMap[selCurWithinOR] = setwith;
        }
        public void setCurrentWithinAND(boolean setwith){
            selBitMap[selCurWithinAND] = setwith;
        }
        public void setCurrentRsiOR(boolean setwith){
            selBitMap[selCurRsiOR] = setwith;
        }
        public void setCurrentRsiAND(boolean setwith){
            selBitMap[selCurRsiAND] = setwith;
        }
        public boolean getCurrentWithinOR(){
            return(selBitMap[selCurWithinOR]);
        }
        public boolean getCurrentWithinAND(){
            return(selBitMap[selCurWithinAND]);
        }
        public boolean getCurrentRsiOR(){
            return (selBitMap[selCurRsiOR]);
        }
        public boolean getCurrentRsiAND(){
            return(selBitMap[selCurWithinAND]);
        }
        public void clearSelBitMap() {
            Arrays.fill(selBitMap, Boolean.FALSE);
        }
        public int getSelectedStateValue(){
            int actBit = 0;
            for(actBit = 0, selBitMapIntValue = 0; actBit < TBL_SZ; actBit++){
                selBitMapIntValue |= (selBitMap[actBit] ?1:0)   << actBit;
            }
            return(selBitMapIntValue);
        }      
    }
    public class AllPivotsPl{
        double pl;
        double plPercent;
        String dateOut;
    }

    public class ProfitLoss{
        final int SHARES = 100;
        final String LONG_SHORT = "LONG";
        public int transactions = 0;
        double currentPl = 0;
        double currentPlPercent = 0.0;
        public double runningPl = 0;
        public boolean lastBought = false;
        boolean lastSold = false;
        public double maxProfit = 0.0;
        public String maxProfitDate = null;
        public String maxLossDate = null;
        public double maxLoss = 0.0;
        public double maxRunningProfit = 0.0;
        public String maxRunningProfitDate = null;
        public String maxRunningLossDate = null;
        public double maxRunningLoss = 0.0;
        double closePrice = 0.0;
        double currentClosePrice = 0.0;
        double currentCost = 0.0;
        int ticksIn = 0;
        int ticksOut = 0;
        int buySellCnt = 0;
        double accumulatedPriceMoves = 0.0;
        double currentPrice;
        double tmp;
        double openPosValue = 0.0;
        double costBasis = 0.0;
        String longOrShort = slopeDefs.oBiasLongStr;
        public String openPosDateIn = null;
        public AllPivotsPl actPivotPl;
        public List<AllPivotsPl> allPl = new ArrayList<AllPivotsPl>();
        
        void doSimTrade(ibApi.historicalData.slopeData slopeIn) {
            if(isLong(longOrShort) == true){
                doLongSimTrade(slopeIn);
            }else if (isShort(longOrShort) == true){
                doShortSimTrade(slopeIn);
            }
        }
               
        void doLongSimTrade(ibApi.historicalData.slopeData slopeIn){
            this.closePrice = slopeIn.closePrice;
            this.currentClosePrice = slopeIn.currentClosePrice; 
            ticksIn += slopeIn.trendUpTicks;
            ticksOut += slopeIn.trendDnTicks;
            
            if (slopeIn.isTrendUp() == true){
                //buy SHARES..
                currentCost = (SHARES * closePrice);
                if (transactions == 0){
                    costBasis = currentCost;
                }
                openPosDateIn = slopeIn.dateP1;
                lastBought = true;
                lastSold = false;
                transactions++;
                 //reset to start fresh..
                slopeIn.maxClosingPrice = 0.0;
            }else if (lastBought == true) {
                //sell shares...for either profit or loss..
                currentPl = ((SHARES * closePrice) - currentCost);
                if ((currentPl > 0) && (currentPl > maxProfit)){
                    //gain..remember max profit..
                    maxProfit = currentPl;
                    maxProfitDate = slopeIn.dateP1;
                    
                }else if ((currentPl < 0)&&(currentPl < maxLoss)){
                    maxLoss = currentPl;
                    maxLossDate = slopeIn.dateP1;
                }
                runningPl += currentPl;
                //keep max/min of running pl too..
                if ((runningPl > 0) && (runningPl > maxRunningProfit)){
                    //gain..remember max profit..
                    maxRunningProfit = runningPl;
                    maxRunningProfitDate = slopeIn.dateP1;
                    
                }else if ((runningPl < 0)&&(runningPl < maxRunningLoss)){
                    maxRunningLoss = runningPl;
                    maxRunningLossDate = slopeIn.dateP1;
                }
                
                lastBought = false;
                lastSold = true;
                transactions++;
                currentPlPercent += (currentPl / currentCost);
                //used for gainLock..keep track of highest close during uptrend...
                //get price we started with with bot
                currentPrice = (currentCost / SHARES);
                //get change from buy price to maxClosing during uptrend..
                tmp = (slopeIn.maxClosingPrice - currentPrice);
                //accumulate the change in price.
                accumulatedPriceMoves += tmp;
                buySellCnt++;
                actPivotPl = new AllPivotsPl();
                actPivotPl.pl = currentPl;
                actPivotPl.plPercent = (currentPl / currentCost);
                actPivotPl.dateOut = slopeIn.dateP1;
                allPl.add(actPivotPl);
            }
        }
        
        void doShortSimTrade(ibApi.historicalData.slopeData slopeIn){
            /*
                do short simulated trading. When slope trend turns up, buy back 
                shares that were sold when slope trend changed to down; Trend change from Up to Down, 
                sell shares short; Trend change from Down to Up, buy back shares (cover short). Hopefully
                at a profit.
            */
            this.closePrice = slopeIn.closePrice;
            this.currentClosePrice = slopeIn.currentClosePrice; 
            ticksIn += slopeIn.trendUpTicks;
            ticksOut += slopeIn.trendDnTicks;
            
            if (slopeIn.isTrendDn() == true){
                //sell SHARES SHORT...
                currentCost = (SHARES * closePrice);
                if (transactions == 0){
                    costBasis = currentCost;
                }
                openPosDateIn = slopeIn.dateP1;
                lastBought = false;
                lastSold = true;
                transactions++;
                 //reset to start fresh..
                slopeIn.maxClosingPrice = 0.0;
            }else if (lastSold == true) {
                //buy shares...to cover short for either profit or loss..
                currentPl = (currentCost - (SHARES * closePrice));
                if ((currentPl > 0) && (currentPl > maxProfit)){
                    //gain..remember max profit..
                    maxProfit = currentPl;
                    maxProfitDate = slopeIn.dateP1;
                    
                }else if ((currentPl < 0)&&(currentPl < maxLoss)){
                    maxLoss = currentPl;
                    maxLossDate = slopeIn.dateP1;
                }
                runningPl += currentPl;
                //keep max/min of running pl too..
                if ((runningPl > 0) && (runningPl > maxRunningProfit)){
                    //gain..remember max profit..
                    maxRunningProfit = runningPl;
                    maxRunningProfitDate = slopeIn.dateP1;
                    
                }else if ((runningPl < 0)&&(runningPl < maxRunningLoss)){
                    maxRunningLoss = runningPl;
                    maxRunningLossDate = slopeIn.dateP1;
                }
                
                lastBought = true;
                lastSold = false;
                transactions++;
                currentPlPercent += (currentPl / currentCost);
                //used for gainLock..keep track of highest close during uptrend...
                //get price we started with with bot
                currentPrice = (currentCost / SHARES);
                //get change from buy price to maxClosing during uptrend..
                tmp = (slopeIn.maxClosingPrice - currentPrice);
                //accumulate the change in price.
                accumulatedPriceMoves += tmp;
                buySellCnt++;
                actPivotPl = new AllPivotsPl();
                actPivotPl.pl = currentPl;
                actPivotPl.plPercent = (currentPl / currentCost);
                actPivotPl.dateOut = slopeIn.dateP1;
                allPl.add(actPivotPl);
            }
        }        
        public double getPlPercent(){
            return myUtils.roundMe(currentPlPercent, 4);
        }
        public double getRunningPl(){
            return runningPl;
        }
        public double getMaxRunningP(){
            return maxRunningProfit;
        }
        public double getMaxRunningL(){
            return maxRunningLoss;
        }
        public int getDaysIn(){
            return ticksIn;
        }
        public boolean isPosOpen(){
            boolean retVal = false;
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true)
                retVal = (lastBought == true);
            else if (longOrShort.equals(slopeDefs.oBiasShortStr) == true){
                retVal = (lastSold == true);
            }
            return(retVal);
        }
        public double getCurrentPosValue(){
            double cv = 0.0;
            if (longOrShort.equals(slopeDefs.oBiasLongStr) == true){
                if (lastBought == true) {
                    cv =  ((SHARES * currentClosePrice) - currentCost);
                }else{
                    cv = 0.0;
                }
            }else if (longOrShort.equals(slopeDefs.oBiasShortStr) == true){
                if (lastSold == true) {
                    cv =  (currentCost - (SHARES * currentClosePrice));
                }else{
                    cv = 0.0; 
                }               
            }
            return myUtils.roundMe(cv, 2);
        }
        public String getCurrentPosDateIn(){
            String cd = "";
            if (isLong(longOrShort) == true){
                if (lastBought == true) {
                    cd =  openPosDateIn;
                }else{
                    
                }
            }else if (isShort(longOrShort) == true){
                if (lastSold == true) {
                    cd =  openPosDateIn;
                }else{
                    
                }                
            }
            return cd;
        }
        public boolean isLong(String ls){
            return (ls.equals(slopeDefs.oBiasLongStr));
        }
        public boolean isShort(String ls){
            return (ls.equals(slopeDefs.oBiasShortStr));
        }
    }
    public class SlopeStructure{
            String ticker = null;
            final int SLOPE_MAX = 30;
            public int size = 0;
            boolean currentChangeInDirection = false;
            public String currentTrendIs = null;
            public double openPosValue = 0.0;
            public String longOrShort = slopeDefs.oBiasLongStr;
            public double plPercent = 0.0;
            public double overAllPl = 0.0;
            public double maxRunningProfitPercent = 0.0;
            public double maxRunningLossPercent = 0.0;
            public double overAllPlIncludeOpen = 0.0;
            public double accumulatedPriceMoves = 0.0;
            public double avePriceMove = 0;
            public double stdDev50Day = 0.0;
            public double volDev50Day = 0.0;
            public double stdDev100Day = 0.0;
            public double volDev100Day = 0.0;
            
            
            //this is the first purchase in sequence made. this is the cost we compare our over all profit
            //to determine profit/loss
            public double costBasis = 0.0;
            //profit loss sim...
            public ProfitLoss pl = new ProfitLoss();
            ibApi.historicalData.slopeData[] data = new ibApi.historicalData.slopeData[SLOPE_MAX];
            public void createPl(){
                pl = new ProfitLoss();
            }
            public void addOne(ibApi.historicalData.slopeData newOne){
                data[size++] = newOne;
                
            }
            public void addAllFound(ibApi.historicalData.HistorySlope allFound, String forTicker){
                int idx = 0;
                ticker = forTicker;
                size = 0;
                ibApi.historicalData.slopeData lastSlope;
                longOrShort = pl.longOrShort =  allFound.longOrShort;
                for(idx = 0; idx < allFound.logIdx; idx++){           
                    pl.doSimTrade(allFound.getFromLog(idx));
                    data[size++] = allFound.getFromLog(idx);                   
                }
                //last one has today's or current change in direction...get that one..
                currentChangeInDirection = data[allFound.logIdx - 1].changeInDirection;
                //wfshere
                currentTrendIs = allFound.currentTrend;
                //get last slope in history to get current closing value
                lastSlope = allFound.getHistory();
                pl.currentClosePrice = lastSlope.currentClosePrice;
                //see if there is currently open pos and get value of it..
                if (pl.isPosOpen() == true){                  
                    openPosValue = pl.getCurrentPosValue();                   
                }
                //percent profit/loss is figured by what dollar amount we are up or down (runningPl) 
                //divided by the original amount invested at the very begining (first purchase)
                //get closing price which is the first purchase price. CostBasis has very first purchase.
                costBasis = pl.costBasis;
                overAllPl = myUtils.roundMe(((pl.getRunningPl() / costBasis) * 100), 2);
                overAllPlIncludeOpen = myUtils.roundMe((((pl.getRunningPl() + pl.getCurrentPosValue() )/ costBasis) * 100), 2);
                maxRunningProfitPercent = myUtils.roundMe(((pl.getMaxRunningP() / costBasis) * 100), 2);
                maxRunningLossPercent = myUtils.roundMe(((pl.getMaxRunningL() / costBasis) * 100), 2);
                plPercent = pl.getPlPercent();
                accumulatedPriceMoves = pl.accumulatedPriceMoves;
                avePriceMove = accumulatedPriceMoves / pl.buySellCnt;
                this.stdDev50Day = allFound.stdDev50Day;
                this.volDev50Day = allFound.volDev50Day;
                this.stdDev100Day = allFound.stdDev100Day;
                this.volDev100Day = allFound.volDev100Day;
            }
            
        }
        public class SlopesFound {
            SlopeStructure Slopes[] = new SlopeStructure[numberOfTickers] ;
            public int tickerNumber = 0;
            
            public void addOne(SlopeStructure newOne){
                Slopes[tickerNumber++] = newOne;
                
            }
            public void calcPL(SlopeStructure newOne){
                int idx = 0;
                for (idx = 0; idx < newOne.size; idx++){
                    if ((newOne.data[idx].changeInDirection == true) && (newOne.data[idx].trend == "trendUp")){
                        //do it here...
                    }
                }
            }
            public void setSize(int sz){
                Slopes = new SlopeStructure[sz];
                
            }
            
            
        }
    
    public class runFilterThread extends Thread {

        boolean shutDown = false;
        int runXs = 0;
        boolean running = false;
        final int FIVE_SECS = 5000;
        final int TEN_SECS = FIVE_SECS * 2;
        final int ELEVEN_SECS = 11000;

        String endDate50 = null;
        String endDate100 = null;
        String endDate200 = null;
        String actTicker = null;
        String endDate10 = null;
        SlopeStructure slopeStruct;
        //allSlopesFound = new SlopesFound();
        
        public runFilterThread() {

            running = true;
            this.start();

        }

        public void stopit() {
            running = false;
        }

        public void startit() {
            running = true;
        }

        public void windDown() {
            shutDown = true;
        }
        public void dispHistoricalData(ibApi.historicalData hd){
            //resCnt++;
            int tmpInt = 0;
            String tmpStr = null;
            if (selectedAboveMa == true) {
                tmpStr = " Above";
                tmpInt = 0;
            } else if (selectedBelowMa == true) {
                tmpStr = " Below";
                tmpInt = 1;
            }
            
            System.out.println(tmpStr + " 50day: " + hd.getPercentAbove(50));
            resultsTextArea.append("\n  " + tmpStr + " 50day: " + (Math.abs(tmpInt - hd.getPercentAbove(50)) * 100) + " %" + " (endDate: " + endDate50 + ")");
            System.out.println(tmpStr + " 100day: " + hd.getPercentAbove(100));
            resultsTextArea.append("\n  " + tmpStr + " 100day: " + (Math.abs(tmpInt - hd.getPercentAbove(100)) * 100) + " %" + " (endDate: " + endDate100 + ")");
            System.out.println(tmpStr + " 200day: " + hd.getPercentAbove(200));
            resultsTextArea.append("\n  " + tmpStr + " 200day: " + (Math.abs(tmpInt - hd.getPercentAbove(200)) * 100) + " %" + " (endDate: " + endDate200 + ")");

           // results[resCnt] = new resultSummary();
            results[resCnt].ticker = actTicker;
            results[resCnt].fiftyDayMa = (hd.getPercentAbove(MA_50DAY) * 100);
            results[resCnt].hundredDayMa = (hd.getPercentAbove(MA_100DAY) * 100);
            results[resCnt].twoHundredDayMa = (hd.getPercentAbove(MA_200DAY) * 100);

        }
        public void dispCurrentlyWithinData(ibApi.historicalData hd){
            
            System.out.println("Currently within 50day by: " + hd.getPercentWithin(MA_50DAY));
            resultsTextArea.append("\n   Currently within 50day by: " + ((hd.getPercentWithin(MA_50DAY)) * 100) + " %");
            results[resCnt].currentlyWithin50DayMa = (hd.getPercentWithin(MA_50DAY) * 100);

            System.out.println("Currently within 100day by: " + hd.getPercentWithin(MA_100DAY));
            resultsTextArea.append("\n   Currently within 100day by: " + ((hd.getPercentWithin(MA_100DAY)) * 100) + " %");
            results[resCnt].currentlyWithin100DayMa = (hd.getPercentWithin(MA_100DAY) * 100);

            System.out.println("Currently within 200day by: " + hd.getPercentWithin(MA_200DAY));
            resultsTextArea.append("\n   Currently within 200day by: " + ((hd.getPercentWithin(MA_200DAY)) * 100) + " %");
            results[resCnt].currentlyWithin200DayMa = (hd.getPercentWithin(MA_200DAY) * 100);
            
        }
        public void dispRsiData(ibApi.historicalData hd){
            System.out.println("Todays RSI: " + hd.todaysRsi);
            resultsTextArea.append("\n   Todays RSI: " + hd.todaysRsi);
        }
        public void dispBullBearCross(slopeAnalysis.BullBearCross bbx){
            System.out.println("Bull/Bear Condition met: " + bbx.crossConditionStr);
            resultsTextArea.append("\n   BullBear Condition met: "
                    + bbx.crossConditionStr
                    + " On " + bbx.dateItHappend
                    + " " + bbx.daysBackWhenOccurred + " Days ago.");

        }
        public void dispSlopeData(ibApi.historicalData.HistorySlope slopIn, int selector){
            
                switch (selector){
                    case SLOPE_CHANGE:
                        System.out.println("Change In DIRECTION!: Current trend is " + slopIn.getCurrentTrend());
                        resultsTextArea.append("\n   Change In DIRECTION!: Current trend is " + slopIn.getCurrentTrend());
                        break;
                    case SLOPE_UPTREND:
                        System.out.println("Current trend is " + slopIn.getCurrentTrend());
                        resultsTextArea.append("\n   Current trend is " + slopIn.getCurrentTrend());
                        break;
                    case SLOPE_DNTREND:
                        System.out.println("Current trend is " + slopIn.getCurrentTrend());
                        resultsTextArea.append("\n   Current trend is " + slopIn.getCurrentTrend());
                        break;
                    case SLOPE_BULLBEAR_CROSS:                       
                        System.out.println("Bull/Bear Condition met: " + bullBearCross.crossConditionStr);
                        resultsTextArea.append("\n   BullBear Condition met: " + 
                                               bullBearCross.crossConditionStr + 
                                               " On " + bullBearCross.dateItHappend + 
                                               " " + bullBearCross.daysBackWhenOccurred + " Days ago.");
                        break;    
                    default:
                        break;
                }
                
        }
        
        
        public void dispTodaysMovers(ibApi.historicalData hd){
            System.out.println("Met Percent Move: " + "Close_To_Open: " + hd.todaysPrevCloseToTodayOpen + "%  Open_To_Current: " + hd.todaysOpenToLast + "%");
            resultsTextArea.append("\n   Percent Move: " + "Close_To_Open: " + hd.todaysPrevCloseToTodayOpen + "%  Open_To_Current: " + hd.todaysOpenToLast + "%");            
        }
        public class Streams{
            int numTickersTotal = 0;
            int numOfBlocksTotal = 0;
            int openBlockStart = 0;
            int openBlockEnd = 0;
            int actBlock = 0;
            int actIdx = 0;
            boolean weAreDone = false;
            readTickerFile allTicks;
            final int STREAM_BLKS = 100;
            Streams(readTickerFile allTickers){
                numTickersTotal = allTickers.numberOfTickers;
                numOfBlocksTotal = (int) (numTickersTotal / STREAM_BLKS);
                actBlock = 0;
                openBlockStart = 0;
                //wfs 11.12.16 dow only has 32 tickers..
                if(numTickersTotal >= STREAM_BLKS){
                    openBlockEnd = STREAM_BLKS; 
                }else{
                    openBlockEnd = numTickersTotal;
                }
                
                allTicks = allTickers;
                open();
            }
            public void openStreams(int index) {
                if ((weAreDone == false) && (index >= openBlockEnd)) {
                    if (actBlock < (numOfBlocksTotal - 1)) {
                        openBlockStart = ((actBlock * STREAM_BLKS) + STREAM_BLKS);
                        openBlockEnd = (openBlockStart + STREAM_BLKS);
                        actBlock ++;
                    } else if (actBlock == (numOfBlocksTotal - 1)) {
                        openBlockStart = (numOfBlocksTotal * STREAM_BLKS);
                        openBlockEnd = (openBlockStart + (numTickersTotal % STREAM_BLKS));
                        weAreDone = true;
                    }
                    actChain.cancelChainStreams();
                    open();
                }
            }
            public void reOpenStreams(){
                open();
            }
            void open(){
                int idx;
                String progressStr;
                
                System.out.println("\nopenStreams: " + openBlockStart + " to " + openBlockEnd);
                resultsTextArea.append("\nopening streams..wait.." + openBlockStart + " to " + openBlockEnd);
                for (idx = openBlockStart; idx < openBlockEnd; idx++) {
                    actTicker = allTicks.tickerDataHere[idx];
                    System.out.println("working on opening: " + actTicker + " (" + idx + ") Stream.");
                    qInfo = actChain.getQuote(actTicker, false);
                }
                resultsTextArea.append("..done.\n");
                System.out.println("\nopenStreams: Done.");
                myUtils.delay(1000);
            }
        }
        public void run() {
            /*
            1) 50, 100, 200 historical moving averages can be turned on/off by 
            entering values > 0. 0 == turn off, don't display.
            2) 50, 100, 200 current values within % can be turned on/off by entering
            values > 0. 0 == turn off, don't display.
            3) Current RSI values can be displayed with criteria of > or < a number.
            
            Each of the above three can be turned on individually by selecting the OR button, 
            which is also the default setting.
            You can also "AND" each with one another to narrow a search/criteria further. A zero field disables 
            the display of that function.
            
            logic Table:
            
            state    selectedCurrentlyWithinAndOrFunction   selectedRsiAndOrFunction     result
             A(0)          AND = 0 OR = 0                      AND = 0 OR = 0        Disable these functions
             B(1)          AND = 0 OR = 0                      AND = 0 OR = 1        Currently within values are disabled.
                                                                                     Display RSI values if criteria is met.
             C(2)          AND = 0 OR = 0                      AND = 1 OR = 0        Display RSI values IF criteria is met AND the
                                                                                     historical 50/100/200 data if
                                                                                     criteria is met. Currently-within 
                                                                                     values are disabled.
             D(3)          AND = 0 OR = 0                      AND = 1 OR = 1        NOT allowed, Currently within values 
                                                                                     are disabled.
             E(4)          AND = 0 OR = 1                      AND = 0 OR = 0        Display currently-within values if criteria is met 
                                                                                     regardless of 50/100/200 historical criteria.
             F(5)          AND = 0 OR = 1                      AND = 0 OR = 1        Display currently-within values if criteria is met 
                                                                                     Display RSI values if criteria is met.
             G(6)          AND = 0 OR = 1                      AND = 1 OR = 0        Display currently-within values if criteria is met.
                                                                                     Display RSI values if criteria is met AND
                                                                                     historical 50/100/200 data if
                                                                                     criteria is met.
             H(7)          AND = 0 OR = 1                      AND = 1 OR = 1        NOT allowed, Display currently-within values 
                                                                                     if criteria is met.
             I(8)          AND = 1 OR = 0                      AND = 0 OR = 0        Display Currently within values IF
                                                                                     criteria is met AND the historical 50/100/200
                                                                                     criteria is met. RSI is disabled.
             J(9)          AND = 1 OR = 0                      AND = 0 OR = 1        Display Currently within values IF
                                                                                     criteria is met AND the historical 50/100/200
                                                                                     criteria is met. Display RSI values 
                                                                                     if criteria is met.
             K(A)          AND = 1 OR = 0                      AND = 1 OR = 0        Display Currently within values if
                                                                                     criteria is met AND the historical 50/100/200
                                                                                     if criteria is met AND RSI if criteria is met.
             L(B)          AND = 1 OR = 0                      AND = 1 OR = 1        NOT allowed. Display Currently within values if
                                                                                     criteria is met AND the historical 50/100/200
                                                                                     if criteria is met.
             M(C)          AND = 1 OR = 1                      AND = 0 OR = 0        NOT allowed. RSI is disabled.
             N(D)          AND = 1 OR = 1                      AND = 0 OR = 1        NOT allowed. Display RSI values 
                                                                                     if criteria is met.
             O(E)          AND = 1 OR = 1                      AND = 1 OR = 0        NOT allowed. Display RSI values if criteria is met AND
                                                                                     historical 50/100/200 data if
                                                                                     criteria is met.
             P(F)          AND = 1 OR = 1                      AND = 1 OR = 1        NOT allowed. 
            */
            
            results = new resultSummary[exchangeData.numberOfTickers + 1];
            resCnt = 0;
            boolean met50Day = false;
            boolean met100Day = false;
            boolean met200Day = false;
            boolean metHistorical = false;
            boolean metCurrentlyWithin50Day = false;
            boolean metCurrentlyWithin100Day = false;
            boolean metCurrentlyWithin200Day = false;
            boolean metCurrentlyWithin = false;
            boolean metRsiUpperband = false;
            boolean metRsiLowerband = false;
            boolean metRsi = false;
            boolean metBullBear = false;
            allSlopesFound = new SlopesFound();
            allSlopesFound.setSize(numberOfTickers);
            Streams actStreams;
            String histDataDuration = "";
            String histDataBarSize = "";
            boolean reversalUp2DnBasedOnTail = ((userSelectedGapUpPercent != 0) && (userSelectedGapUpTail != 0));
            boolean reversalDn2UpBasedOnTail = ((userSelectedGapDnPercent != 0) && (userSelectedGapDnTail != 0));

            while (running) {
                try {
                    System.out.println("thread running.. " + runXs++);
                    actStreams = new Streams(exchangeData);
                    // this maybe a continuation so start where we left off..
                    String tmpStr = null;
                    if (selectedAboveMa == true) {
                        tmpStr = "AboveMA";
                    } else if (selectedBelowMa == true) {
                        tmpStr = "BelowMA";
                    }
                    searchCriteriaLable.setText(
                            "*** " + tmpStr
                            + " 50day: " + selectedPercentOf50Ma + "% ***"
                            + " 100day: " + selectedPercentOf100Ma + "% ***"
                            + " 200day: " + selectedPercentOf200Ma + "% ***");
                    resultsTextArea.append(
                            "\n *** search criteria *** " + tmpStr + " by "
                            + " 50day: " + selectedPercentOf50Ma + "% ***"
                            + " 100day: " + selectedPercentOf100Ma + "% ***"
                            + " 200day: " + selectedPercentOf200Ma + "% ***"
                    );
                    searchCriteriaString = "\n *** search criteria *** " + tmpStr + " by "
                            + " 50day: " + selectedPercentOf50Ma + "% ***"
                            + " 100day: " + selectedPercentOf100Ma + "% ***"
                            + " 200day: " + selectedPercentOf200Ma + "% ***";
                    for (int idx = 0; (userOneShot == true) || ((shutDown == false) && (idx < (exchangeData.numberOfTickers - 1))); idx++) {
                        actStreams.openStreams(idx);
                        if (userOneShot == true) {
                            actTicker = selectedTicker;
                            userOneShot = false;
                            shutDown = true;
                        }else{
                            actTicker = exchangeData.tickerDataHere[idx];    
                        }
                        
                        //actTicker = exchangeData.tickerDataHere[idx];
                        resultsTextArea.append("\nSearching...working on: " + actTicker + " (" + idx + " / " + exchangeData.numberOfTickers + " )");
                        System.out.println("working on : " + actTicker + " (" + idx + ")");
                        ibApi.historicalData hd = null;// = new ibApi.historicalData(); 
                        hd = actIbApi.setActHistoricalData(hd);
                        hd.nextTid(idx);
                        ibApi.historicalData.HistorySlope actHistory = hd.actWally;
                        if(userSelectedDayWeek == false){
                            //true == day
                            histDataDuration = "1 Y";
                            //histDataDuration = "2 Y";
                            histDataBarSize = "1 day";
                        }else{
                            //false == week
                            histDataDuration = "4 Y";
                            histDataBarSize = "1 week";
                        }
                        if (hd.getHistoricalData(actTicker, histDataDuration, histDataBarSize) == true) {
                            resultsTextArea.append("..OK.");
                            hd.calcSimpleMovingAve(MA_50DAY);
                            endDate50 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_100DAY);
                            endDate100 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_200DAY);
                            endDate200 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_10DAY);
                            endDate10 = hd.endingDate;
                            if ((maWindowSizes != null) && (maWindowSizes.allSet == true)){
                                hd.calcPercentages(maWindowSizes);
                            }else{
                                hd.calcPercentages();
                            }
                            
                            hd.calcRSI1();
                            hd.calcAveVolume(90);
                            hd.calcStdDev(MA_50DAY);
                            hd.calcVolatility(MA_50DAY);
                            hd.calcStdDev(MA_100DAY);
                            hd.calcVolatility(MA_100DAY); 
                            if(gapUpDnTail.isEnabled() == true){
                                hd.findTails(gapUpDnTail);
                            }                            
                            System.out.println("ave90DayVolume = " + hd.aveVolume90Day);
                            System.out.println("50DayStdDev = " + (hd.stdDev50Day * 100) + "%");
                            System.out.println("50Volatility = " + (hd.vol50Day * 100) + "%");
                            System.out.println("100DayStdDev = " + (hd.stdDev100Day * 100) + "%");
                            System.out.println("100Volatility = " + (hd.vol100Day * 100) + "%");
                            if(selectedMoversPercent > 0){ 
                                hd.findTodaysMovers();
                            }
                            if (bullBearCross.searchNow == true){
                                hd.checkForBullBearCrossing(bullBearCross);
                                if (bullBearCross.wePassed == true){
                                    System.out.println("\nPASSED!!!!!");
                                }
                            }
                            todaysDate = hd.getTodaysDate();
                            if(selectedSlopeCriteriaInt != SLOPE_OFF){
                                actHistory = hd.findSlopes1(selectedSlopeCriteriaInt, slopeDefs.MA_10DAY);                               
                                actHistory.setLongShort(userSelectedLongShort);
                                slopeStruct = new SlopeStructure();                               
                                if ( ((selectedSlopeCriteriaInt == SLOPE_SAVE_ALL)) ||
                                     (((selectedSlopeCriteriaInt == SLOPE_CHANGE) && (actHistory.currentChangeInDirection == true)) ||
                                     ((selectedSlopeCriteriaInt == SLOPE_UPTREND) && (actHistory.currentTrend == "trendUp")) ||
                                     ((selectedSlopeCriteriaInt == SLOPE_BULLBEAR_CROSS) && (bullBearCross.wePassed == true)) ||   
                                     ((selectedSlopeCriteriaInt == SLOPE_DNTREND) && (actHistory.currentTrend == "trendDN")))
                                    ){
                                    slopeStruct.addAllFound(actHistory, actTicker);
                                    allSlopesFound.addOne(slopeStruct);
                                    resultsComboBox.addItem(actTicker);
                                    tradeList.addOne(actTicker);
                                    tradeList.displayAll();
                                    dispSlopeData(actHistory, selectedSlopeCriteriaInt);
                                    }
                            }
                            System.out.println("\nRSI = " + hd.todaysRsi);
                            if (selectedAboveMa == true){
                                met50Day = ((selectedPercentOf50Ma != 0) && ((hd.getPercentAbove(MA_50DAY) * 100) >= selectedPercentOf50Ma));                                
                                met100Day = ((selectedPercentOf100Ma != 0) && ((hd.getPercentAbove(MA_100DAY) * 100) >= selectedPercentOf100Ma));
                                met200Day = ((selectedPercentOf200Ma != 0) && ((hd.getPercentAbove(MA_200DAY) * 100) >= selectedPercentOf200Ma));
                            }else{
                                met50Day = ((selectedPercentOf50Ma != 0) &&   (((1.0 - hd.getPercentAbove(MA_50DAY)) * 100) >= selectedPercentOf50Ma));
                                met100Day = ((selectedPercentOf100Ma != 0) && (((1.0 - hd.getPercentAbove(MA_100DAY)) * 100) >= selectedPercentOf100Ma));
                                met200Day = ((selectedPercentOf200Ma != 0) && (((1.0 - hd.getPercentAbove(MA_200DAY)) * 100) >= selectedPercentOf200Ma));           
                            }
                            metCurrentlyWithin50Day = ((selectedCurrentlyAbove50Ma != 0) && (hd.isCurrentCloseWithinMa(MA_50DAY, selectedCurrentlyAbove50Ma) == true));
                            metCurrentlyWithin100Day = ((selectedCurrentlyAbove100Ma != 0) && (hd.isCurrentCloseWithinMa(MA_100DAY, selectedCurrentlyAbove100Ma) == true));
                            metCurrentlyWithin200Day = ((selectedCurrentlyAbove200Ma != 0) && (hd.isCurrentCloseWithinMa(MA_200DAY, selectedCurrentlyAbove200Ma) == true));
                            
                            metRsiUpperband = ((selectedShowRsiGreaterThan != 0) && (hd.todaysRsi >= selectedShowRsiGreaterThan));
                            metRsiLowerband = ((selectedShowRsiLessThan != 0) && (hd.todaysRsi <= selectedShowRsiLessThan));
                            
                            metHistorical = (met50Day && met100Day && met200Day);
                            metCurrentlyWithin = (metCurrentlyWithin50Day || metCurrentlyWithin100Day || metCurrentlyWithin200Day);
                            metRsi = (metRsiUpperband || metRsiLowerband);
                            metBullBear = ((bullBearCross.enabled == true) && (bullBearCross.wePassed == true));
                            if ((selectedMoversPercent > 0) && ((Math.abs(hd.todaysPrevCloseToTodayOpen) >= selectedMoversPercent) || ((Math.abs(hd.todaysOpenToLast)) >= selectedMoversPercent))){
                                dispTodaysMovers(hd);
                            }
                            if ((selectedSlopeCriteriaInt == SLOPE_FILTERED) && (metHistorical == true) && (bullBearCross.enabled == false)){
                                slopeStruct.addAllFound(actHistory, actTicker);
                                allSlopesFound.addOne(slopeStruct);
                                resultsComboBox.addItem(actTicker);
                                tradeList.addOne(actTicker);
                                tradeList.displayAll();
                                dispSlopeData(actHistory, selectedSlopeCriteriaInt);                               
                            }else  if ((selectedSlopeCriteriaInt == SLOPE_FILTERED) && (metHistorical == true) && (metBullBear == true)){
                                slopeStruct.addAllFound(actHistory, actTicker);
                                allSlopesFound.addOne(slopeStruct);
                                resultsComboBox.addItem(actTicker);
                                tradeList.addOne(actTicker);
                                tradeList.displayAll();
                                dispSlopeData(actHistory, selectedSlopeCriteriaInt);                               
                            }
                            results[resCnt] = new resultSummary();
                            if(metBullBear == true){
                                dispBullBearCross(bullBearCross);
                            }
                            switch (userSelectedLogic.getSelectedStateValue()) {
                                case S0_DIS_ALL: {
                                    //do nothing, all disabled, check historical only..
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                }
                                break;
                                case S1_DIS_CURRENTWITHIN_EN_RSI: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                    if (metRsi == true) {
                                        dispRsiData(hd);
                                    }
                                }
                                break;
                                case S2_EN_RSI_AND_HIST_DIS_CURRENTWITHIN: {
                                    if ((metRsi == true) && (metHistorical == true)) {
                                        dispHistoricalData(hd);
                                        dispRsiData(hd);
                                    }
                                }
                                break;
                                case S3_DIS_CURRENTWITHIN_NOT_ALLOWED: {
                                    //do nothing, all disabled, check historical only..
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                }
                                break;
                                case S4_EN_CURRENTWITHIN_DIS_RSI: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                    if (metCurrentlyWithin == true) {
                                        dispCurrentlyWithinData(hd);
                                    }

                                }
                                break;
                                case S5_EN_CURRENTWITHIN_EN_RSI: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                    if (metCurrentlyWithin == true) {
                                        dispCurrentlyWithinData(hd);
                                    }
                                    if (metRsi == true) {
                                        dispRsiData(hd);
                                    }

                                }
                                break;
                                case S6_EN_CURRENTWITHIN_EN_RSI_AND_HIST: {
                                    if (metCurrentlyWithin == true) {
                                        dispCurrentlyWithinData(hd);
                                    }
                                    if ((metHistorical == true) && (metRsi == true)) {
                                        dispHistoricalData(hd);
                                        dispRsiData(hd);
                                    }
                                }
                                break;
                                case S7_EN_CURRENTWITHIN_NOT_ALLOWED: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                    if (metCurrentlyWithin == true) {
                                        dispCurrentlyWithinData(hd);
                                    }
                                }
                                break;
                                case S8_EN_CURRENTWITHIN_AND_HIST_DIS_RSI: {
                                    if ((metCurrentlyWithin == true) && (metHistorical == true)) {
                                        dispHistoricalData(hd);
                                        dispCurrentlyWithinData(hd);
                                        // not enabled but go ahead and display these two..
                                        dispRsiData(hd);
                                        dispTodaysMovers(hd);
                                    }
                                }
                                break;
                                case S9_EN_CURRENTWITHIN_AND_HIST_EN_RSI: {
                                    if ((metHistorical == true) && (metCurrentlyWithin == true)) {
                                        dispHistoricalData(hd);   
                                        dispCurrentlyWithinData(hd);
                                        // not enabled but go ahead and display these two..
                                        if(metRsi == false){
                                            dispRsiData(hd);
                                        }
                                        dispTodaysMovers(hd);
                                    }
                                    if (metRsi == true){
                                       dispRsiData(hd); 
                                    }
                                }
                                break;
                                case S10_EN_CURRENTWITHIN_AND_HIST_AND_RSI: {
                                    if ((metHistorical == true) && (metCurrentlyWithin == true) && (metRsi == true)) {
                                        dispHistoricalData(hd);
                                        dispCurrentlyWithinData(hd);
                                        dispRsiData(hd);
                                        //not enabled but go ahead show..
                                        dispTodaysMovers(hd);
                                    }
                                }
                                break;
                                case S11_EN_CURRENTWITHIN_AND_HIST_NOT_ALLOWED: {
                                    if ((metHistorical == true) && (metCurrentlyWithin == true)) {
                                        dispHistoricalData(hd);
                                        dispCurrentlyWithinData(hd);
                                        // not enabled but go ahead and display these two..
                                        dispRsiData(hd);
                                        dispTodaysMovers(hd);
                                    }
                                }
                                break;
                                case S12_NOT_ALLOWED_DIS_RSI: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                }
                                break;
                                case S13_NOT_ALOWED_EN_RSI: {
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                    if (metRsi == true) {
                                        dispRsiData(hd);
                                    }
                                }
                                break;
                                case S14_NOT_ALLOWED_EN_RSI_AND_HIST: {
                                    if ((metHistorical == true) && (metRsi == true)) {
                                        dispHistoricalData(hd);
                                        dispRsiData(hd);
                                    }
                                }
                                break;
                                case S15_NOT_ALLOWED_NOT_ALLOWED: {
                                    //do nothing but historical...
                                    if (metHistorical == true) {
                                        dispHistoricalData(hd);
                                    }
                                }
                                break;

                                default: {
                                    System.out.println("State unknown in Switch!!!");

                                }
                            } /* switch */
                            resCnt++;

                        } else {
                            System.out.println("Error Getting Historical Data: " + hd.getErrorMsg());
                            resultsTextArea.append("..Bad.");
                            //resultsTextArea.append("\nError Getting Historical Data: " + hd.getErrorMsg());
                        }
                        runFilterThread.sleep(ELEVEN_SECS);
                        if (shutDown == true) {
                            System.out.println("\n shutting down...");
                            resultsTextArea.append("\n shutting down..");
                        }
                        if(userPaused == true){
                            actChain.cancelChainStreams();
                            while (userPaused == true){
                                myUtils.delay(200);
                            }
                            actStreams.reOpenStreams();
                        }else{
                            
                        }
                        if((optionChainDialog != null) && (optionChainDialog.getStillRunning() == true)){
                            actChain.cancelChainStreams();
                            resultsTextArea.append("\nOption Chain running pausing...");
                            while (optionChainDialog.getStillRunning() == true){
                                myUtils.delay(200);
                            }
                            resultsTextArea.append("\nOption Chain stopped resuming...");
                            actStreams.reOpenStreams();
                            optionChainDialog = null;
                        }else{
                            
                        }
                    }
                    if (selectedSlopeCriteriaInt == SLOPE_FILTERED){
                        /* only write to tradelist file if we were in SLOPE_FILTER mode */
                        if (userSlectedAppendToTradeListFile == true){
                            tradeList.wrToFile(true);
                        }else{
                            tradeList.wrToFile(false);
                        }
                    }
                    running = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("done...");
            resultsTextArea.append("\ndone..");
            writeResultsFile(selectedExchangeStr);

        }

    }

    void writeResultsFile(String exchStr) {
        int sz = 0;
        int lp = 0;
        Boolean done = false;
        ioWrTextFiles wrFile = new ioWrTextFiles(exchStr + "filterResults.txt", false);
        sz = results.length;
        wrFile.write("\n For Exchange: " + exchStr);
        wrFile.write("\n" + searchCriteriaString + "\n\n");
        wrFile.write("\n=== " + myUtils.GetTodaysDate() + " ===");
        System.out.println("writing to file; entries: " + sz);

        for (lp = 0; done.equals(false); lp++) {
            if (results[lp] != null) {
                wrFile.write(results[lp].ticker);
                wrFile.write("\n   50d: " + results[lp].fiftyDayMa);
                wrFile.write("\n  100d: " + results[lp].hundredDayMa);
                wrFile.write("\n  200d: " + results[lp].twoHundredDayMa + "\n");
            } else {
                done = true;
            }
        }
        wrFile.write("\n=== " + (lp - 1) + " found. ===");
        wrFile.closeWr();
        System.out.println("wrote to file.");

    }

    private void runStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runStopButtonActionPerformed
        // TODO add your handling code here:
     

        if ((selectedExchangeStr == null) || (((selectedTicker == null) && userTicker == true))) {
            System.out.println("Enter Critera..cannot start search.");
            resultsTextArea.append("\nEnter Critera..connot start search.");
        } else if (userTicker == true) {
            //run one time with user selected ticker..
            userOneShot = true;
            exchangeData = readTickersFromFile(selectedExchangeStr);
            rfThread = new runFilterThread();

        } else if (selectedExchangeStr != null) {
            if ((rfThread == null) || ((rfThread != null) && (rfThread.shutDown == true))) {
                exchangeData = readTickersFromFile(selectedExchangeStr);
                rfThread = new runFilterThread();

            } else {
                rfThread.shutDown = true;
            }
        }

    }//GEN-LAST:event_runStopButtonActionPerformed

    private void tickerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tickerTextFieldActionPerformed
        // TODO add your handling code here:
        selectedTicker = tickerTextField.getText();
    }//GEN-LAST:event_tickerTextFieldActionPerformed

    private void enterTickerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterTickerCheckBoxActionPerformed
        // TODO add your handling code here:
        if (enterTickerCheckBox.isSelected() == true) {
            userTicker = true;
        } else {
            userTicker = false;
        }
        System.out.println("userTicker: " + userTicker);
    }//GEN-LAST:event_enterTickerCheckBoxActionPerformed

    private void currentlyPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentlyPercentTextFieldActionPerformed
        // TODO add your handling code here:

        if (selectedMovingAverage.equals(FIFTY_DAY)) {
            selectedCurrentlyAbove50Ma = Integer.valueOf(currentlyTextField.getText());
        } else if (selectedMovingAverage.equals(HUNDRED_DAY)) {
            selectedCurrentlyAbove100Ma = Integer.valueOf(currentlyTextField.getText());
        } else if (selectedMovingAverage.equals(TWO_HUNDRED_DAY)) {
            selectedCurrentlyAbove200Ma = Integer.valueOf(currentlyTextField.getText());
        } else {

        }

    }//GEN-LAST:event_currentlyPercentTextFieldActionPerformed

    private void selectRsiGreaterTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRsiGreaterTextFieldActionPerformed
        // TODO add your handling code here:
        selectedShowRsiGreaterThan = Integer.valueOf(selectRsiGreaterTextField.getText());
        System.out.println("selectedShowRsiGreaterThan = " + selectedShowRsiGreaterThan);
    }//GEN-LAST:event_selectRsiGreaterTextFieldActionPerformed

    private void selectRsiLessTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRsiLessTextFieldActionPerformed
        // TODO add your handling code here:
        selectedShowRsiLessThan = Integer.valueOf(selectRsiLessTextField.getText());
        System.out.println("selectedShowRsiLessThan = " + selectedShowRsiLessThan);
    }//GEN-LAST:event_selectRsiLessTextFieldActionPerformed
    // AND OR check boxes - four of them ...
    private void currentlyWithinAndCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentlyWithinAndCheckBoxActionPerformed
        // TODO add your handling code here:
        userSelectedLogic.setCurrentWithinAND(currentlyWithinAndCheckBox.isSelected());
        if (currentlyWithinAndCheckBox.isSelected() == true){
           currentlyWithinOrCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_currentlyWithinAndCheckBoxActionPerformed

    private void currentRsiAndCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentRsiAndCheckBoxActionPerformed
        // TODO add your handling code here:
        userSelectedLogic.setCurrentRsiAND(currentRsiAndCheckBox.isSelected());
        if (currentRsiAndCheckBox.isSelected() == true){
            currentRsiOrCheckBox.setSelected(false);
            userSelectedLogic.setCurrentRsiOR(false);
        }
    }//GEN-LAST:event_currentRsiAndCheckBoxActionPerformed

    private void currentRsiOrCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentRsiOrCheckBoxActionPerformed
        // TODO add your handling code here:
        userSelectedLogic.setCurrentRsiOR(currentRsiOrCheckBox.isSelected());
        if (currentRsiOrCheckBox.isSelected() == true){
            currentRsiAndCheckBox.setSelected(false);
            userSelectedLogic.setCurrentRsiAND(false);
        }
    }//GEN-LAST:event_currentRsiOrCheckBoxActionPerformed

    private void currentlyWithinOrCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentlyWithinOrCheckBoxActionPerformed
        // TODO add your handling code here:
        userSelectedLogic.setCurrentWithinOR(currentlyWithinOrCheckBox.isSelected());
        if (currentlyWithinOrCheckBox.isSelected() == true){
           currentlyWithinAndCheckBox.setSelected(false);
           userSelectedLogic.setCurrentWithinAND(false);
        }
    }//GEN-LAST:event_currentlyWithinOrCheckBoxActionPerformed

    private void historicalBelowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historicalBelowCheckBoxActionPerformed
        // TODO add your handling code here:
        if (historicalBelowCheckBox.isSelected() == true){
            selectedAboveMa = false;
            selectedBelowMa = true;
        }else{
            selectedAboveMa = true;
            selectedBelowMa = false;
        }
    }//GEN-LAST:event_historicalBelowCheckBoxActionPerformed

    private void selectedTodaysMoversPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedTodaysMoversPercentTextFieldActionPerformed
        // TODO add your handling code here:
        selectedMoversPercent = Integer.valueOf(selectedTodaysMoversPercentTextField.getText());
        System.out.println("selectedMoversPercent = " + selectedMoversPercent);
    }//GEN-LAST:event_selectedTodaysMoversPercentTextFieldActionPerformed

    private void slopeFinderComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_slopeFinderComboBoxActionPerformed
        // TODO add your handling code here:
        /* get the user selected exhange to later search in */
        if (slopeFinderComboBox.getItemCount() > 0) {
            selectedSlopeCriteriaStr = slopeFinderComboBox.getSelectedItem().toString();
            selectedSlopeCriteriaInt = slopeFinderComboBox.getSelectedIndex();
        };

        System.out.println("selected Slope is: " + selectedSlopeCriteriaStr);
        System.out.println("selected Slope idx is : " + selectedSlopeCriteriaInt);
        if (selectedSlopeCriteriaInt == SLOPE_BULLBEAR_CROSS){
            bullBearCross = saOuter.new BullBearCross();           
            displayBullBearDialogForm bullBearDialogForm;
            bullBearDialogForm = new displayBullBearDialogForm(new javax.swing.JFrame(), true);
            bullBearDialogForm.setVisible(true);
            if ((bullBearCross.enabled = (bullBearDialogForm.getEnabled()) == true) && (bullBearDialogForm.getUserSelectedGoodness() == true)){               
                bullBearCross.crossConditionStr = bullBearDialogForm.getBullBearConditionStr();
                bullBearCross.crossConditionInt = bullBearDialogForm.getBullBearConditionInt();
                bullBearCross.conditionPeriodInDays = bullBearDialogForm.getDaysOn();
                //see if user turned it off 
                bullBearCross.searchNow = !(bullBearCross.crossConditionInt == slopeDefs.oTURNED_OFF_INT);               
            }
            
        }
        if (selectedSlopeCriteriaInt == SLOPE_SET_MA_WINDOW){ 
            if (maWindowSizes == null){           
                maWindowSizes = saOuter.new MaWindowSz(/*askUserInput*/ true); 
            }else{
                maWindowSizes.getUserInput();
            }
        }
    }//GEN-LAST:event_slopeFinderComboBoxActionPerformed

    private void resultsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resultsComboBoxActionPerformed
        // TODO add your handling code here:
        String userIn = null;
        int userItem = 0;
        userIn = resultsComboBox.getSelectedItem().toString();
        userItem = resultsComboBox.getSelectedIndex();
        
        if(userIn == CLR_ALL){
            resultsComboBox.removeAllItems();
            tradeList.removeAll();
            resultsComboBox.addItem(DISP_ALL);
            resultsComboBox.addItem(CLR_ALL);       
            resultsComboBox.addItem(ADD_TO_TRADE_LIST);
        }else if (userIn == DISP_ALL) {
            selectedResultToViewStr = null;
            selectedResultToViewInt = 0;
        } else if (userIn == ADD_TO_TRADE_LIST) {
        } else {
            selectedResultToViewStr = userIn;
            //subtract 3 for CLR_ALL, DISP_ALL, ADD_TO in location 0/1/2..
            selectedResultToViewInt = userItem - 3;
        }
        
    }//GEN-LAST:event_resultsComboBoxActionPerformed

    private void displaySlopeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displaySlopeButtonActionPerformed
        // TODO add your handling code here:
        this.dispCnt++;
        System.out.println("\n displaySlopeButtonActionPerformed");
        displaySlopeDialogForm displaySlopes;
        displaySlopes = new displaySlopeDialogForm(new javax.swing.JFrame(), true);
        displaySlopes.setSingleOneToView(selectedResultToViewStr, selectedResultToViewInt);    
        displaySlopes.setActiveSlopes(allSlopesFound);
        displaySlopes.setExchangeStr(selectedExchangeStr);
        displaySlopes.setSearchCriteriaStr(searchCriteriaString);
        displaySlopes.setWrFileCnt(this.dispCnt);
        displaySlopes.setVisible(true);
        //displaySlopes.startJob();
        //displaySlopes.displayThem();
        
    }//GEN-LAST:event_displaySlopeButtonActionPerformed

    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
        // TODO add your handling code here:
        String str1 = "";
        str1 = resultsComboBox.getSelectedItem().toString();
        int itemIdx = 0;
        itemIdx = tradeList.findOne(str1);
        int idx = 0;
        //restore the cmmands..
        if (itemIdx != -1) {
            System.out.println("\n" + str1 + " removed from tradeList. ");          
            //remove only tickers,  leave commands in..tricky..remove same location x times the list shrinks every
            //time one is removed.
            resultsComboBox.setSelectedItem(DISP_ALL);
            for (idx = 0; idx < tradeList.getSz(); idx ++){
                resultsComboBox.removeItemAt(FIRST_TICKER);    
            }
            tradeList.removeOne(str1);
            // restore tickers append to commands...
            for (idx = 0; idx < tradeList.getSz(); idx++){
                resultsComboBox.addItem(tradeList.returnStr(idx));    
            }
            resultsComboBox.setSelectedItem(DISP_ALL);
        }else{
            System.out.println("\n" + str1 + " not found in tradeList..? ");
        }
    }//GEN-LAST:event_DeleteButtonActionPerformed

    private void appendTradeListFileCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendTradeListFileCheckBoxActionPerformed
        // TODO add your handling code here:
        if (appendTradeListFileCheckBox.isSelected() == true) {
            userSlectedAppendToTradeListFile = true;
        } else {
            userSlectedAppendToTradeListFile = false;
        }
        System.out.println("appendTradeListFile: " + userSlectedAppendToTradeListFile);
    }//GEN-LAST:event_appendTradeListFileCheckBoxActionPerformed

    private void shortCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortCheckBoxActionPerformed
        // TODO add your handling code here:
        if (shortCheckBox.isSelected() == true){
            userSelectedLongShort = slopeDefs.oBiasShortStr;
        }else{
            userSelectedLongShort = slopeDefs.oBiasLongStr;
        }
        
    }//GEN-LAST:event_shortCheckBoxActionPerformed

    private void dayWeekCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dayWeekCheckBoxActionPerformed
        // TODO add your handling code here:
        
        if (dayWeekCheckBox.isSelected() == true) {
            //true == week
            userSelectedDayWeek = true;
            dayWeekLabel.setText("Week");
        } else {
            //false == day
            userSelectedDayWeek = false;
            dayWeekLabel.setText("Day");
        }
        System.out.println("dayWeek: " + userSelectedDayWeek); 
        
        
    }//GEN-LAST:event_dayWeekCheckBoxActionPerformed

    private void gapUpTailTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gapUpTailTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedGapUpTail = Integer.valueOf(gapUpTailTextField.getText());
        gapUpDnTail.gapUpTailPct = userSelectedGapUpTail;
        System.out.println("\nuserSelectedGapUpTail = " + userSelectedGapUpTail);
    }//GEN-LAST:event_gapUpTailTextFieldActionPerformed

    private void gapUpTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gapUpTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedGapUpPercent = Integer.valueOf(gapUpTextField.getText()); 
        gapUpDnTail.gapUpPct = userSelectedGapUpPercent;
        System.out.println("\nuserSelectedGapUpPercent = " + userSelectedGapUpPercent);
    }//GEN-LAST:event_gapUpTextFieldActionPerformed

    private void gapDnTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gapDnTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedGapDnPercent = Integer.valueOf(gapDnTextField.getText());
        gapUpDnTail.gapDnPct = userSelectedGapDnPercent;
        System.out.println("\nuserSelectedGapDnPercent = " + userSelectedGapDnPercent);
    }//GEN-LAST:event_gapDnTextFieldActionPerformed

    private void gapDnTailTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gapDnTailTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedGapDnTail = Integer.valueOf(gapDnTailTextField.getText());
        gapUpDnTail.gapDnTailPct = userSelectedGapDnTail;
        System.out.println("\nuserSelectedGapDnTail = " + userSelectedGapDnTail);
    }//GEN-LAST:event_gapDnTailTextFieldActionPerformed

    private void todaysMoversAndCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_todaysMoversAndCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_todaysMoversAndCheckBoxActionPerformed

    private void todaysMoversOrCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_todaysMoversOrCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_todaysMoversOrCheckBoxActionPerformed

    private void displayTailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayTailButtonActionPerformed
        // TODO add your handling code here:
        int totFound = gapUpDnTail.getTotalFound();
        int idx;
        System.out.println("total found: " + totFound);
        System.out.println(gapUpDnTail.getNextDate(true));
        for(idx = 1; idx < totFound; idx++){
            System.out.println(gapUpDnTail.getNextDate(false));
        }
        
    }//GEN-LAST:event_displayTailButtonActionPerformed

    private void setDefaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDefaultButtonActionPerformed

       // TODO add your handling code here:
        indexComboBox.setSelectedIndex(0);
        selectedExchangeStr = indexComboBox.getSelectedItem().toString();
        selectedExchangeInt = indexComboBox.getSelectedIndex(); 
        movingAveComboBox.setSelectedItem("200");
        selectedPercentOf50Ma = 3;
        selectedPercentOf100Ma = 60;
        selectedPercentOf200Ma = 60;
        selectedCurrentlyAbove50Ma = 2;
        selectedCurrentlyAbove100Ma = 2;
        selectedCurrentlyAbove200Ma = 2;   
        MAPercentTextField.setText("60");
        currentlyTextField.setText("2");
        currentlyWithinAndCheckBox.setSelected(true);
        userSelectedLogic.setCurrentWithinAND(currentlyWithinAndCheckBox.isSelected());
        currentlyWithinOrCheckBox.setSelected(false);
        userSelectedLogic.setCurrentWithinOR(currentlyWithinOrCheckBox.isSelected());
        selectedMoversPercent = 4;
        selectedTodaysMoversPercentTextField.setText("4");
        todaysMoversAndCheckBox.setSelected(false);        
        todaysMoversOrCheckBox.setSelected(true);
        selectRsiGreaterTextField.setText("70");
        selectedShowRsiGreaterThan = 70;
        selectRsiLessTextField.setText("30");
        selectedShowRsiLessThan = 30;
        currentRsiAndCheckBox.setSelected(false);
        userSelectedLogic.setCurrentRsiAND(currentRsiAndCheckBox.isSelected());
        currentRsiOrCheckBox.setSelected(true);
        userSelectedLogic.setCurrentRsiOR(currentRsiOrCheckBox.isSelected());
    }//GEN-LAST:event_setDefaultButtonActionPerformed

    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        // TODO add your handling code here:
        userPaused = !userPaused;
        System.out.println("\npause button hit...");
        if(userPaused == true){
            resultsTextArea.append("\nPaused..Hit Pause again to continue.");
        }else{
            resultsTextArea.append("\nresumed..Hit Pause again to pause.");
        }                
    }//GEN-LAST:event_pauseButtonActionPerformed

    private void invokeOptionChainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invokeOptionChainButtonActionPerformed
        // TODO add your handling code here:           
        optionChainDialog = new displayOptionChainDialogForm(new javax.swing.JFrame(), false);
        optionChainDialog.setVisible(true);        

    }//GEN-LAST:event_invokeOptionChainButtonActionPerformed
    public class ioRdTextFiles {

        String fname = homeDirectory;

        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;
        BufferedWriter bow;

        void openRd(String fileName, boolean append) {
            fname = fileName;

            try {
                fis = new FileInputStream(fname);
                bir = new BufferedReader(new InputStreamReader(fis));
            } catch (Exception e) {
                System.out.println("error reading text from: " + fname + "(" + e + ").");
                System.out.println("file does not exist " + fname);
                try {
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                } catch (IOException ex) {
                    Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
                }

                bir = new BufferedReader(new InputStreamReader(fis));
            }
        }

        void closeRd() {
            try {
                bir.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioRdTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openRd(fileName, append);

        }

        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }

    public class ioWrTextFiles {

        String fname = homeDirectory;

        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;

        void openWr(String fileName, boolean append) {
            fname = fileName;

            try {
                //fos = new FileOutputStream(fileName);
                //dos = new DataOutputStream(fos);
                bow = new BufferedWriter(new FileWriter(fname, append));

                try {
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                } catch (Exception e) {
                    System.out.println("file does not exist " + fname);
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                }

            } catch (Exception e) {
                System.out.println("error writing text to: " + fileName + "(" + e + ").");
            }
        }

        void closeWr() {
            try {
                bow.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioWrTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openWr(fname+fileName, append);

        }

        boolean write(String str) {

            try {
                if (str != null) {
                    bow.write(str);
                    bow.newLine();
                    bow.flush();
                }
            } catch (Exception e) {
                System.out.println("error write to file: " + fname + "(" + e + ").");
            }
            return true;
        }

        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }

    public class readTickerFile {

        final int MAX_TICKERS = 4000;
        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        String fileName = homeDirectory;
        String tmpStr = null;
        String actExchange = null;
        int numberOfTickers = 0;
        String tickerDataHere[];

        public readTickerFile(String tickerFileName, int numOfTickers) throws IOException {
            boolean split = false;
            int tickerNumber = numOfTickers - 1;
            int actTicker = 0;

            if (numOfTickers > MAX_TICKERS) {
                System.out.println("numOfTickers too big!!!!!");
                return;
            }
            numberOfTickers = numOfTickers;
            String tickerData[] = new String[numOfTickers];
            tickerDataHere = tickerData;
            if (fileName.equals(null)) {
                return;
            }

            for (int idx = 0; idx < numOfTickers; idx++) {
                tickerData[idx] = new String();
            }
            try {
                fis = new FileInputStream(tickerFileName);
                dis = new DataInputStream(fis);
                bir = new BufferedReader(new InputStreamReader(fis));

                while ((tickerNumber != 0) && !split) {
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        tickerData[actTicker] = tmpStr;
                        tickerNumber--;
                        actTicker++;
                    } else {
                        split = true;
                    }

                }
                bir.close();
            } catch (Exception e) {
                System.out.println("error reading text from: " + tickerFileName + "(" + e + ").");
            }
        }
    }

    private readTickerFile readTickersFromFile(String exchange) {

        int numOfTickers = 0;
        readTickerFile rtf;

        actTickersFile = homeDirectory + exchange;       
        try {
            numOfTickers = myUtils.countLinesInFile(actTickersFile);
        } catch (IOException ex) {
            Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("num of ticker are: " + numOfTickers);
        numberOfTickers = numOfTickers;
        try {
            rtf = new readTickerFile(actTickersFile, numOfTickers);

        } catch (IOException ex) {
            Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            return (null);
        }
        return (rtf);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(displayStocksWthFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(displayStocksWthFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(displayStocksWthFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(displayStocksWthFilter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                displayStocksWthFilter dialog = new displayStocksWthFilter(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton DeleteButton;
    private javax.swing.JTextField MAPercentTextField;
    private javax.swing.JCheckBox appendTradeListFileCheckBox;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox currentRsiAndCheckBox;
    private javax.swing.JCheckBox currentRsiOrCheckBox;
    private javax.swing.JTextField currentlyTextField;
    private javax.swing.JCheckBox currentlyWithinAndCheckBox;
    private javax.swing.JCheckBox currentlyWithinOrCheckBox;
    private javax.swing.JCheckBox dayWeekCheckBox;
    private javax.swing.JLabel dayWeekLabel;
    private javax.swing.JButton displaySlopeButton;
    private javax.swing.JButton displayTailButton;
    private javax.swing.JCheckBox enterTickerCheckBox;
    private javax.swing.JPanel filterByTailPanel;
    private javax.swing.JTextField gapDnTailTextField;
    private javax.swing.JTextField gapDnTextField;
    private javax.swing.JTextField gapUpTailTextField;
    private javax.swing.JTextField gapUpTextField;
    private javax.swing.JCheckBox historicalBelowCheckBox;
    private javax.swing.JComboBox indexComboBox;
    private javax.swing.JButton invokeOptionChainButton;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox movingAveComboBox;
    private javax.swing.JButton pauseButton;
    private javax.swing.JComboBox resultsComboBox;
    private javax.swing.JTextArea resultsTextArea;
    private javax.swing.JButton runStopButton;
    private javax.swing.JLabel searchCriteriaLable;
    private javax.swing.JTextField selectRsiGreaterTextField;
    private javax.swing.JTextField selectRsiLessTextField;
    private javax.swing.JTextField selectedTodaysMoversPercentTextField;
    private javax.swing.JButton setDefaultButton;
    private javax.swing.JCheckBox shortCheckBox;
    private javax.swing.JComboBox slopeFinderComboBox;
    private javax.swing.JCheckBox tailAndCheckBox;
    private javax.swing.JCheckBox tailOrCheckBox;
    private javax.swing.JTextField tickerTextField;
    private javax.swing.JCheckBox todaysMoversAndCheckBox;
    private javax.swing.JCheckBox todaysMoversOrCheckBox;
    // End of variables declaration//GEN-END:variables
}
