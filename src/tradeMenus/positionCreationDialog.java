/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;
import ibTradeApi.ibApi;
import positions.commonGui;
import ibTradeApi.ibApi;
import java.util.Arrays;
import tradeMenus.portfolio.*;

/**
 *
 * @author earlie87
 */
public class positionCreationDialog extends javax.swing.JDialog {

    String selectedExchangeStr = "NasdaqQ1";
    //trend filter
    int selectedPercentOf50Ma = 0;
    int selectedPercentOf100Ma = 0;
    int selectedPercentOf200Ma = 0;
    boolean selectedAppend = false;
    boolean selectedTrendAndPerf = false;
    String userSelectedLongShort = slopeDefs.oBiasLongStr;
    //int userSelectedPositionBias = slopeDefs.oBiasLong;
    //performance filter
    double selectedMaxRunningProfit = 0.0;
    double selectedMaxRunningLoss = 0.0;
    boolean selectedAboveMa = true;
    boolean selectedBelowMa = false;
    //these next two are implied by user input of params they enter for trend/perf
    boolean selectedTrendFilter = false;
    boolean selectedPerfFilter = false;
    boolean selectedAllFiltersMustPass = false;
    boolean selectedGainFilter = false;
    boolean selectedMovingAveBounceFilter = false;
    boolean selectedChannelFiltering = false;
    //additinal filtering...
    boolean selectedVolumeFilter = false;
    boolean selectedPriceFilter = false;
    int min90DayVolume = 0;
    int minPricePerShare = 0;
    double selectedChangeInGainLow = 0;
    double selectedChangeInGainHi = 0;
    int selectedMaMustTouchMinPercent = 0;
    int selectedMaPiercedMaxPercent = 0;
    int selectedChangeInMonths = 0;
    //position bias
    String userSelectedMaStr = "10DayMa";
    int userSelectedMaInt = 0;
    int maTable[] = {10,20,50,100,200};
    double userSelectedChannelRangePercent = 0.0;
    
    int selectedPositionBias = 0;
    //final private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    String actTickersFile = null;
    int numberOfTickers = 0;
    int startIterations = 0;
    readTickerFile exchangeData = null;
    List<String> actTradeList = new ArrayList<>();
    runFilterThread rfThread = null;
    private final ibApi actIbApi = ibApi.getActApi();
    private String todaysDate = null;
    TradeList trendTradeList = new TradeList();
    TradeList performanceTradeList = new TradeList();
    TradeList volumeTradeList = new TradeList();
    TradeList priceTradeList = new TradeList();
    TradeList gainRangeTradeList = new TradeList();
    TradeList maBounceTradeList = new TradeList();
    TradeList channelTradeList = new TradeList();
    //holds an appended list..
    TradeList tradeList;
    IOTextFiles.ioWrTextFiles portfolioWrTextFile;
     //outer class in slopeAnalysis..
    slopeAnalysis saOuter = new slopeAnalysis();
    //inner classes within slopeAnalysis...
    slopeAnalysis.SlopesFound allSlopes;
    
    slopeAnalysis.SlopeStructure slopeStruct = saOuter.new SlopeStructure();
    String criteriaSelected;
    public slopeAnalysis.MaWindowSz maWindowSizes;// = saOuter.new MaWindowSz();
    //public slopeAnalysis.BullBearCross bullBearCross;
    public slopeAnalysis.BullBearCross bullBearCross = saOuter.new BullBearCross();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    
    boolean userCanceled = false;
    final String NASQ1 = "NasdaqQ1";
    final String NASQ2 = "NasdaqQ2";
    final String NASQ3 = "NasdaqQ3";
    final String NASQ4 = "NasdaqQ4";
    final String NYSEQ1 = "NyseQ1";
    final String NYSEQ2 = "NyseQ2";
    final String NYSEQ3 = "NyseQ3";
    final String NYSEQ4 = "NyseQ4";
    final String NYSEQ5 = "NyseQ5";
    final String NYSEQ6 = "NyseQ6";
    final String NYSEQ7 = "NyseQ7";
    final String NYSEQ8 = "NyseQ8";
    final String WATCH1 = "WATCH1";

    final int MA_50DAY = slopeDefs.MA_50DAY;
    final int MA_100DAY = slopeDefs.MA_100DAY;
    final int MA_200DAY = slopeDefs.MA_200DAY;
    final int MA_10DAY  = slopeDefs.MA_10DAY;
    final int AVE_90DAY = slopeDefs.AVE_90DAY;
     //outer class in slopeAnalysis..
    portfolio portfolioOuter = new portfolio();
    PosCreationParameters posCreationParms = portfolioOuter.new PosCreationParameters();
    /**
     * Creates new form positionCreationDialog
     *
     * @param parent
     * @param modal
     */
    public positionCreationDialog(java.awt.Frame parent, boolean modal, List<String> posList) {
        super(parent, modal);
        actTradeList = posList;
        initComponents();
        if (exchangeComboBox.getItemCount() > 0) {
            selectedExchangeStr = exchangeComboBox.getSelectedItem().toString();
        }
        if(maComboBox.getItemCount() > 0){
            userSelectedMaStr = maComboBox.getSelectedItem().toString();
            userSelectedMaInt = maTable[maComboBox.getSelectedIndex()];
        }
       
    }
    public void updateLastPosCreationParams(){
        posCreationParms.selectedIndex = exchangeComboBox.getSelectedItem().toString();
        posCreationParms.andOp = andCheckBox.isSelected();
        posCreationParms.appendToList = appendCheckBox.isSelected();
        posCreationParms.longPos = (selectedPositionBias == slopeDefs.oBiasLong);
        posCreationParms.shortPos = !posCreationParms.longPos;
        posCreationParms.ma50Day = selectedPercentOf50Ma;
        posCreationParms.ma100Day = selectedPercentOf100Ma;
        posCreationParms.ma200Day = selectedPercentOf200Ma;
        posCreationParms.maxRunLoss = selectedMaxRunningLoss;
        posCreationParms.maxRunProfit = selectedMaxRunningProfit;
    }
    public PosCreationParameters getUpdatedLastPosCreationParams(){
        return(posCreationParms);
    }
    public String getPosBiasStr(int pin){
        String retStr = "";
        switch (pin) {
            case slopeDefs.oBiasLong:
                retStr = slopeDefs.oBiasLongStr;
                break;
            case slopeDefs.oBiasShort:
                retStr = slopeDefs.oBiasShortStr;
                break;
        }
        return retStr;
    }
    public void setLastPosCreationParams(PosCreationParameters pin){
        posCreationParms = pin;
        exchangeComboBox.setSelectedItem(pin.selectedIndex);
        selectedPercentOf50Ma = pin.ma50Day;
        fiftyDayTextField.setText(Integer.toString(selectedPercentOf50Ma));
        selectedPercentOf100Ma = pin.ma100Day;
        hundredDayTextField.setText(Integer.toString(selectedPercentOf100Ma));
        selectedPercentOf200Ma = pin.ma200Day;
        twoHundredDayTextField.setText(Integer.toString(selectedPercentOf200Ma));
        selectedAppend = pin.appendToList;
        appendCheckBox.setSelected(selectedAppend);
        selectedTrendAndPerf = pin.andOp;
        andCheckBox.setSelected(selectedTrendAndPerf);
        selectedMaxRunningProfit = pin.maxRunProfit;
        maxRunningProfitTextField.setText(Double.toString(selectedMaxRunningProfit));
        selectedMaxRunningLoss = pin.maxRunLoss;
        maxRunningLossTextField.setText(Double.toString(selectedMaxRunningLoss));
        selectedPositionBias = pin.selectedPositionBias;
        posBiasLable.setText(getPosBiasStr(pin.selectedPositionBias));
       
        switch (selectedPositionBias) {
            case slopeDefs.oBiasLong:
                selectedAboveMa = true;
                selectedBelowMa = false;
                aboveBelowLabel.setText("Above MA by at least:");
                break;
            case slopeDefs.oBiasShort:
                selectedAboveMa = false;
                selectedBelowMa = true;
                aboveBelowLabel.setText("Below MA by at least:");
                break;
        }

    }
    public void createThisPositionList(List<String> pIn) {
        actTradeList = pIn;
    }

    public boolean appendToExistingFile() {
        return selectedAppend;
    }

    public String getUserCriteria() {
        return criteriaSelected;
    }

    public boolean getDidUserCancel() {
        return userCanceled;
    }

    public int getMinPricePerShare() {
        return minPricePerShare;
    }

    public int getMin90DayAveVol() {
        return min90DayVolume;
    }
    public int getMaMustTouchPercent(){
        return selectedMaMustTouchMinPercent;
    }
    public int getMaMaxPiercePercent(){
        return selectedMaPiercedMaxPercent;
    }
    public int getMa50DaySz(){
        if (maWindowSizes != null){
            return maWindowSizes.get50DaySz();    
        }else{
            return 0;
        }        
    }
    public int getMa100DaySz(){
        if (maWindowSizes != null){
            return maWindowSizes.get100DaySz();    
        }else{
            return 0;
        }        
    }
    public int getMa200DaySz(){
        if (maWindowSizes != null){
            return maWindowSizes.get200DaySz();    
        }else{
            return 0;
        }        
    }
    public double getChangeInGainLow() {
        return selectedChangeInGainLow;
    }
    public double getChangeInGainHi() {
        return selectedChangeInGainHi;
    }
    public int getChangeInMonths() {
        return selectedChangeInMonths;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        exchangeComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        fiftyDayTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        hundredDayTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        twoHundredDayTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        maxRunningProfitTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        maxRunningLossTextField = new javax.swing.JTextField();
        appendCheckBox = new javax.swing.JCheckBox();
        startButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultsTextArea = new javax.swing.JTextArea();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        andCheckBox = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        trendFoundLabel = new javax.swing.JLabel();
        perfFoundLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        moreFiltersButton = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        volumeFoundLabel = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        priceFoundLabel = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        criteriaLabel = new javax.swing.JLabel();
        aboveBelowLabel = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        posBiasLable = new javax.swing.JLabel();
        belowMaCheckBox = new javax.swing.JCheckBox();
        jLabel19 = new javax.swing.JLabel();
        gainRangeFoundLable = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        maBouncedFoundLabel = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        maComboBox = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        channelRangePercentTextField = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        channelRangeLabel = new javax.swing.JLabel();
        clearAllFieldsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Position Creator");

        jLabel2.setText("Select Exchange:");

        exchangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NasdaqQ1", "NasdaqQ2", "NasdaqQ3", "NasdaqQ4", "NyseQ1", "NyseQ2", "NyseQ3", "NyseQ4", "NyseQ5", "NyseQ6", "NyseQ7", "NyseQ8", "WATCH1" }));
        exchangeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exchangeComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("50Day: ");

        fiftyDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fiftyDayTextFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("100Day:");

        hundredDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hundredDayTextFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("200Day:");

        twoHundredDayTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoHundredDayTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Trend Filtering");

        jLabel7.setText("Performance Filtering");

        jLabel8.setText("MaxRunningProfit >=:");

        maxRunningProfitTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxRunningProfitTextFieldActionPerformed(evt);
            }
        });

        jLabel9.setText("MaxRunningLoss  <=:");

        maxRunningLossTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxRunningLossTextFieldActionPerformed(evt);
            }
        });

        appendCheckBox.setText("appendCheckBox");
        appendCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                appendCheckBoxActionPerformed(evt);
            }
        });

        startButton.setText("Start/Stop");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        resultsTextArea.setColumns(20);
        resultsTextArea.setRows(5);
        jScrollPane1.setViewportView(resultsTextArea);

        jLabel10.setText("Results:");

        jLabel11.setText("<--------OR-------->");

        andCheckBox.setText("And");
        andCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andCheckBoxActionPerformed(evt);
            }
        });

        jLabel12.setText("TrendFound:");

        jLabel13.setText("PerfFound   :");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        moreFiltersButton.setText("MoreFilters");
        moreFiltersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreFiltersButtonActionPerformed(evt);
            }
        });

        jLabel14.setText("Additioinal Filtering");

        jLabel15.setText("VolumeFound:");

        volumeFoundLabel.setText("             ");

        jLabel17.setText("PriceFound:");

        priceFoundLabel.setText("            ");

        jLabel16.setText("Criteria:");

        jLabel18.setText("PositionBias:");

        belowMaCheckBox.setText("Click For BelowMa");
        belowMaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                belowMaCheckBoxActionPerformed(evt);
            }
        });

        jLabel19.setText("GainRangeFound:");

        gainRangeFoundLable.setText("        ");
        gainRangeFoundLable.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                gainRangeFoundLableAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        jLabel20.setText("MABounceFound:");

        maBouncedFoundLabel.setText("      ");

        jLabel21.setText("Channel Filtering");

        jLabel22.setText("SelectMA:");

        maComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10DayMa", "20DayMa", "50DayMa", "100DayMa", "200DayMa" }));
        maComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maComboBoxActionPerformed(evt);
            }
        });

        jLabel23.setText("Enter % Range");

        channelRangePercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                channelRangePercentTextFieldActionPerformed(evt);
            }
        });

        jLabel24.setText("ChannelRangeFound:");

        channelRangeLabel.setText("           ");

        clearAllFieldsButton.setText("ClearFields");
        clearAllFieldsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllFieldsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(criteriaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(exchangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(11, 11, 11)
                                                .addComponent(jLabel10))
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(twoHundredDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(hundredDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(fiftyDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(108, 108, 108)
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel14)
                                    .addComponent(moreFiltersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(253, 253, 253))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(42, 42, 42)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel22))
                                    .addComponent(jLabel21)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(81, 81, 81)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel9)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(andCheckBox)
                                                .addGap(62, 62, 62)
                                                .addComponent(jLabel8)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxRunningProfitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(maComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(appendCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(belowMaCheckBox)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(291, 291, 291)
                                                .addComponent(maxRunningLossTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(clearAllFieldsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(168, 168, 168)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(channelRangePercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(20, 20, 20)))
                                        .addGap(21, 21, 21))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(aboveBelowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(133, 133, 133))
            .addGroup(layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(trendFoundLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 54, Short.MAX_VALUE)
                            .addComponent(perfFoundLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(volumeFoundLabel)
                            .addComponent(priceFoundLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startButton)
                        .addGap(77, 77, 77)
                        .addComponent(doneButton)))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gainRangeFoundLable, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cancelButton)
                            .addComponent(jLabel20))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maBouncedFoundLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(channelRangeLabel)
                .addGap(190, 190, 190))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(posBiasLable, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(405, 405, 405))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(posBiasLable, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exchangeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moreFiltersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel21))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(aboveBelowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(fiftyDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8)
                            .addComponent(maxRunningProfitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(andCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(hundredDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(maxRunningLossTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(twoHundredDayTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5)
                                    .addComponent(appendCheckBox))
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(belowMaCheckBox)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(clearAllFieldsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(trendFoundLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel15)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(volumeFoundLabel)
                                .addComponent(jLabel19)
                                .addComponent(gainRangeFoundLable)
                                .addComponent(jLabel24)
                                .addComponent(channelRangeLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(perfFoundLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(doneButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(cancelButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(startButton)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel17)
                                .addComponent(priceFoundLabel)
                                .addComponent(jLabel20)
                                .addComponent(maBouncedFoundLabel))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(maComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(channelRangePercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel16)
                    .addComponent(criteriaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exchangeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exchangeComboBoxActionPerformed
        // TODO add your handling code here:
        if (exchangeComboBox.getItemCount() > 0) {
            selectedExchangeStr = exchangeComboBox.getSelectedItem().toString();
            
        }
    }//GEN-LAST:event_exchangeComboBoxActionPerformed

    private void fiftyDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fiftyDayTextFieldActionPerformed
        // TODO add your handling code here:
        selectedPercentOf50Ma = Integer.valueOf(fiftyDayTextField.getText());
    }//GEN-LAST:event_fiftyDayTextFieldActionPerformed

    private void hundredDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hundredDayTextFieldActionPerformed
        // TODO add your handling code here:
        selectedPercentOf100Ma = Integer.valueOf(hundredDayTextField.getText());
    }//GEN-LAST:event_hundredDayTextFieldActionPerformed

    private void twoHundredDayTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoHundredDayTextFieldActionPerformed
        // TODO add your handling code here:
        selectedPercentOf200Ma = Integer.valueOf(twoHundredDayTextField.getText());
    }//GEN-LAST:event_twoHundredDayTextFieldActionPerformed

    private void appendCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_appendCheckBoxActionPerformed
        // TODO add your handling code here:

        if (appendCheckBox.isSelected() == true) {
            appendCheckBox.setSelected(true);
        } else {
            appendCheckBox.setSelected(false);
        }
        selectedAppend = appendCheckBox.isSelected();
    }//GEN-LAST:event_appendCheckBoxActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        /*
         either trend filter parameters OR performance paramters (or both) need to 
         be enterred before we start anything. 
         */ 
        boolean startIt = false;  
        int allOk = 0;
        String tmpStr;
        
        if ((rfThread != null) && (rfThread.areWeRunning() == true)){
            if (commonGui.postConfirmationMsg("Search running..Stop Current Search?") == 0){
                rfThread.mergeTradeLists();
                rfThread.windDown();
                rfThread = null;
               commonGui.postInformationMsg("Stopped Search.");
               return;
            }else{
               return;
            }
        }
        startIterations++;
        if (!selectedExchangeStr.equals("")){
            exchangeData = readTickersFromFile(selectedExchangeStr);
            System.out.println("\nread Tickers File.");    
        }
        
        selectedTrendFilter = (selectedPercentOf50Ma  != 0) && 
                              (selectedPercentOf100Ma != 0) && 
                              (selectedPercentOf200Ma != 0);
        selectedPerfFilter = ((selectedMaxRunningLoss != 0) && (selectedMaxRunningProfit != 0));
        selectedGainFilter = ((selectedChangeInGainLow != 0) && (selectedChangeInGainHi != 0));
        selectedMovingAveBounceFilter = ((selectedMaMustTouchMinPercent > 0) && (selectedMaPiercedMaxPercent > 0));
        selectedChannelFiltering = (userSelectedMaStr != "") && (userSelectedChannelRangePercent > 0.0);
         /*make sure enough input has been provided before
         starting thread. We need trend Filter input OR performance
         input. OR Both.
         */
        tmpStr = (selectedTrendAndPerf == true ? "AND" : "OR");
        criteriaSelected = "** " + 
                                      "50Ma:" + selectedPercentOf50Ma + " * " + 
                                      "100Ma:" + selectedPercentOf100Ma + " * " +
                                      "200Ma:" + selectedPercentOf200Ma + " * " +
                                      tmpStr + " * " +
                                      "MaxProfit:" + selectedMaxRunningProfit + " * " +
                                      "MaxLoss:" + selectedMaxRunningLoss + " * " + 
                                      "MinVol:" + min90DayVolume + " * " + 
                                      "MinPrice:" + minPricePerShare + " * " +
                                      "Exchng: " + selectedExchangeStr
                ;
        allOk = commonGui.postConfirmationMsg("\nYou Entered:\n" + 
                                      "50Ma: " + selectedPercentOf50Ma + "\n" +
                                      "100Ma: " + selectedPercentOf100Ma + "\n" +
                                      "200Ma: " + selectedPercentOf200Ma + "\n" +
                                      tmpStr + "\n" +
                                      "MaxProfit: " + selectedMaxRunningProfit + "\n" +
                                      "MaxLoss: " + selectedMaxRunningLoss + "\n" +
                                      "Also" + "\n" +
                                      "ChanMa: " + userSelectedMaStr + "\n" +
                                      "Chan%: " + userSelectedChannelRangePercent
        );
        criteriaLabel.setText(criteriaSelected);
        if (allOk == 0) {
            startIt = (selectedTrendFilter || 
                       selectedPerfFilter || 
                       selectedGainFilter || 
                       selectedMovingAveBounceFilter || 
                       selectedChannelFiltering
                      );
            if (startIt == true) {
                if (rfThread != null){
                    rfThread.mergeTradeLists();
                    rfThread.windDown();
                    rfThread = null;
                } 
                trendTradeList.removeAll();
                performanceTradeList.removeAll();
                volumeTradeList.removeAll();
                priceTradeList.removeAll();
                gainRangeTradeList.removeAll();
                maBounceTradeList.removeAll();
                channelTradeList.removeAll();
                actTradeList.removeAll(actTradeList);
                rfThread = new runFilterThread();
                rfThread.startit();
            }
        }
        
        System.out.println("\nstartIt is " + startIt);
    }//GEN-LAST:event_startButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:       
        mergeTradeListsToActTradeList();
        if (rfThread != null){
            rfThread.windDown();
        }
        updateLastPosCreationParams();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed
    private void mergeTradeListsToActTradeList() {
        int idx;
        /*
         put ticker on output list actTradeList for user..
         any ticker in any of four lists go in to actTradeList. Check
         that it's not already in there first..no duplicates.
         */
        //check Trend
        if (trendTradeList.getSz() > 0) {
            for (idx = 0; idx < trendTradeList.getSz(); idx++) {
                if (actTradeList.contains(trendTradeList.getOne(idx)) != true) {
                    actTradeList.add(trendTradeList.getOne(idx));
                }
            }
        }
        //check Perf
        if (performanceTradeList.getSz() > 0) {
            for (idx = 0; idx < performanceTradeList.getSz(); idx++) {
                if (actTradeList.contains(performanceTradeList.getOne(idx)) != true) {
                    actTradeList.add(performanceTradeList.getOne(idx));
                }
            }
        }
        //check Volume
        if (volumeTradeList.getSz() > 0) {
            for (idx = 0; idx < volumeTradeList.getSz(); idx++) {
                if (actTradeList.contains(volumeTradeList.getOne(idx)) != true) {
                    actTradeList.add(volumeTradeList.getOne(idx));
                }
            }
        }
        //check Price
        if (priceTradeList.getSz() > 0) {
            for (idx = 0; idx < priceTradeList.getSz(); idx++) {
                if (actTradeList.contains(priceTradeList.getOne(idx)) != true) {
                    actTradeList.add(priceTradeList.getOne(idx));
                }
            }
        }
        //check Gain Range
        if (gainRangeTradeList.getSz() > 0) {
            for (idx = 0; idx < gainRangeTradeList.getSz(); idx++) {
                if (actTradeList.contains(gainRangeTradeList.getOne(idx)) != true) {
                    actTradeList.add(gainRangeTradeList.getOne(idx));
                }
            }
        }
        //check maBounce 
        if (maBounceTradeList.getSz() > 0) {
            for (idx = 0; idx < maBounceTradeList.getSz(); idx++) {
                if (actTradeList.contains(maBounceTradeList.getOne(idx)) != true) {
                    actTradeList.add(maBounceTradeList.getOne(idx));
                }
            }
        }
        //check channel 
        if (channelTradeList.getSz() > 0) {
            for (idx = 0; idx < channelTradeList.getSz(); idx++) {
                if (actTradeList.contains(channelTradeList.getOne(idx)) != true) {
                    actTradeList.add(channelTradeList.getOne(idx));
                }
            }
        }
    }
    private void maxRunningProfitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxRunningProfitTextFieldActionPerformed
        // TODO add your handling code here:
        selectedMaxRunningProfit = Integer.valueOf(maxRunningProfitTextField.getText());
    }//GEN-LAST:event_maxRunningProfitTextFieldActionPerformed

    private void maxRunningLossTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxRunningLossTextFieldActionPerformed
        // TODO add your handling code here:
        selectedMaxRunningLoss = Integer.valueOf(maxRunningLossTextField.getText());
    }//GEN-LAST:event_maxRunningLossTextFieldActionPerformed

    private void andCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_andCheckBoxActionPerformed
        // TODO add your handling code here:
        if (andCheckBox.isSelected() == true) {
            andCheckBox.setSelected(true);
        } else {
            andCheckBox.setSelected(false);
        }
        selectedTrendAndPerf = andCheckBox.isSelected();
    }//GEN-LAST:event_andCheckBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        boolean cancelit;
        cancelit = ((commonGui.postConfirmationMsg("Cancel Position Creation?")) == 0);           
        if(cancelit == true){
            commonGui.postInformationMsg("Canceled.");
            userCanceled = true;
            if (rfThread != null) {
                rfThread.windDown();
            }
            setVisible(false);
            dispose();
        }
        
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void moreFiltersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreFiltersButtonActionPerformed
        // TODO add your handling code here:
        additionalFilters1 filter1;
        filter1 = new additionalFilters1( new javax.swing.JFrame(), true);
        filter1.setVisible(true);
        min90DayVolume = filter1.get90DayVolume();
        minPricePerShare = filter1.getPricePerShare();
        maWindowSizes = filter1.getMaWindowSz();
        bullBearCross = filter1.getBullBearCross();
        selectedChangeInGainLow = filter1.getChangeInGainLo();
        selectedChangeInGainHi = filter1.getChangeInGainHi();
        selectedMaMustTouchMinPercent = filter1.getMaMustTouchMinPercent();
        selectedMaPiercedMaxPercent = filter1.getMaPiercedMaxPercent();
        
    }//GEN-LAST:event_moreFiltersButtonActionPerformed

    private void belowMaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_belowMaCheckBoxActionPerformed
        // TODO add your handling code here:
        selectedAboveMa = !belowMaCheckBox.isSelected(); 
        if (selectedAboveMa == true)
            aboveBelowLabel.setText("Above MA by at least:");
        else
            aboveBelowLabel.setText("Below MA by at least:");
    }//GEN-LAST:event_belowMaCheckBoxActionPerformed

    private void gainRangeFoundLableAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_gainRangeFoundLableAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_gainRangeFoundLableAncestorAdded

    private void maComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maComboBoxActionPerformed
        if (maComboBox.getItemCount() > 0) {
            userSelectedMaStr = maComboBox.getSelectedItem().toString();
            userSelectedMaInt = maTable[maComboBox.getSelectedIndex()];
            System.out.println("\nUser Selected Ma: " + userSelectedMaStr + "( " + userSelectedMaInt + " )");
        }
    }//GEN-LAST:event_maComboBoxActionPerformed

    private void channelRangePercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelRangePercentTextFieldActionPerformed
        userSelectedChannelRangePercent = (Double.valueOf(channelRangePercentTextField.getText()));
        System.out.println("\nuser selected channelRange: " + userSelectedChannelRangePercent);
    }//GEN-LAST:event_channelRangePercentTextFieldActionPerformed

    private void clearAllFieldsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllFieldsButtonActionPerformed
        
        fiftyDayTextField.setText(Integer.toString(0));
        hundredDayTextField.setText(Integer.toString(0));
        twoHundredDayTextField.setText(Integer.toString(0));
        maxRunningLossTextField.setText(Double.toString(0));
        maxRunningProfitTextField.setText(Double.toString(0));
        channelRangePercentTextField.setText(Integer.toString(0));
        
        selectedPercentOf50Ma = 0;
        selectedPercentOf100Ma = 0;
        selectedPercentOf200Ma = 0;
        selectedMaxRunningLoss = 0;
        selectedMaxRunningProfit = 0;
        userSelectedChannelRangePercent = 0.0;
        
    }//GEN-LAST:event_clearAllFieldsButtonActionPerformed
    public class TradeList {

        List<String> list = new ArrayList<>();

        public void addOne(String ticker) {
            list.add(ticker);
        }

        public void tradeList() {
            
        }

        public String getOne(int idx) {
            return (list.get(idx));
        }

        public int findOne(String ticker) {
            int idx;
            int retIdx = 0;
            boolean found = false;
            for (idx = 0; (found == false) && (idx < list.size()); idx++) {
                if (ticker.equals(list.get(idx))) {
                    found = true;
                    retIdx = idx;
                }
            }
            if (found == true) {
                return (retIdx);
            } else {
                return -1;
            }
        }

        public void removeAll() {
            list.removeAll(list);
        }

        public void removeOne(String remTicker) {
            list.remove(remTicker);
        }

        public int getSz() {
            return (list.size());
        }

        public void displayAll() {
            int idx;
            for (idx = 0; idx < list.size(); idx++) {
                System.out.println("\n" + list.get(idx));
            }
        }

        public void wrToFile(boolean append) {
            IOTextFiles ioTextFiles = new IOTextFiles();
            int idx;
            if (list.size() > 0) {
                IOTextFiles.ioWrTextFiles wrFile = ioTextFiles.new ioWrTextFiles("slopeTraderList1.txt", append);
                for (idx = 0; idx < list.size(); idx++) {
                    System.out.println("\n" + list.get(idx));
                    wrFile.write(list.get(idx));
                }
                wrFile.closeWr();
                System.out.println("wrote to file.");
            } else {
                System.out.println("list was empty? Did not write to file..");
            }

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
        selectionLogic userSelectedLogic = new selectionLogic();
        
        public runFilterThread() {
            running = false;
        }

        public void stopit() {
            running = false;
        }

        public void startit() {
            running = true;
            this.start();
        }

        public void windDown() {
            shutDown = true;
            while (running == true){
                myUtils.delay(100);
            }
        }
        public boolean areWeRunning(){
            return ((shutDown == false) && (running == true));
        }
        public void dispBullBearCross(slopeAnalysis.BullBearCross bbx){
            System.out.println("Bull/Bear Condition met: " + bbx.crossConditionStr);
            resultsTextArea.append("\n   BullBear Condition met: "
                    + bbx.crossConditionStr
                    + " On " + bbx.dateItHappend
                    + " " + bbx.daysBackWhenOccurred + " Days ago.");

        }
        public void dispHistoricalData(ibApi.historicalData hd) {
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
            if ((bullBearCross.enabled == true) && (bullBearCross.wePassed == true)){
                dispBullBearCross(bullBearCross);
            }
        }
        void dispVolumeData(ibApi.historicalData hd){
            System.out.println("\n  volume: " + hd.aveVolume90Day);
            resultsTextArea.append("\n  volume: " + hd.aveVolume90Day);
            
        }
        void dispPriceData(ibApi.quoteInfo q){
            System.out.println("\n  price: " + q.last);
            resultsTextArea.append("\n  price: " + q.last);
            
        }
        void dispGain(double gain){
            System.out.println("\n  YearsGain: " + myUtils.truncate(gain, 2));
            resultsTextArea.append("\n  YearsGain: " + myUtils.truncate(gain, 2));
            
        }
        void dispChannelRange(double chRange, String onDate){
            System.out.println("\n  MaxChannelRange1Yr: " + myUtils.truncate(chRange, 2) + " on Day: " + onDate);
            resultsTextArea.append("\n  MaxChannelRange1Yr: " + myUtils.truncate(chRange, 2) + " on Day: " + onDate);
            
        }
        void dispMaBounce(String maStr){
            System.out.println("\n  " + maStr);
            resultsTextArea.append("\n  " + maStr);
            
        }
        public void mergeTradeLists() {
           
            mergeTradeListsToActTradeList();           
            trendTradeList.removeAll();
            performanceTradeList.removeAll();
            volumeTradeList.removeAll();
            priceTradeList.removeAll();
            gainRangeTradeList.removeAll();
            maBounceTradeList.removeAll();
            channelTradeList.removeAll();
        }

        private void dispPerformanceData(slopeAnalysis.SlopeStructure slopeIn) {
            System.out.println(" MaxRunningProfit: " + slopeIn.maxRunningProfitPercent);
            resultsTextArea.append("\n  MaxRunningProfit: " + slopeIn.maxRunningProfitPercent);
            System.out.println(" MaxRunningLoss: " + slopeIn.maxRunningLossPercent);
            resultsTextArea.append("\n  MaxRunningLoss: " + slopeIn.maxRunningLossPercent);          
        }

        public void openStreams(readTickerFile allTickers) {
            int idx;
            String actTicker;
            int numOfTickers;
            String progressStr;
            
            numOfTickers = allTickers.numberOfTickers;
            System.out.println("\nopenStreams: " + numOfTickers);
            resultsTextArea.append("\nopening streams..wait..");
            for (idx = 0; idx < numOfTickers; idx++) {
                actTicker = allTickers.tickerDataHere[idx];
                System.out.println("working on opening: " + actTicker + " (" + idx + ") Stream.");
                progressStr = "working on opening: " + actTicker + " (" + idx + ") Stream.";
                System.out.println(progressStr);
                qInfo = actChain.getQuote(actTicker, false);
            }
            resultsTextArea.append("done.\n");
            System.out.println("\nopenStreams: Done.");
            myUtils.delay(1000);
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
                openBlockEnd = STREAM_BLKS; 
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
                        openBlockEnd = openBlockStart + (numTickersTotal % STREAM_BLKS);
                        weAreDone = true;
                    }
                    actChain.cancelChainStreams();
                    open();
                }
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

        public class selectionLogic {
            //State definition
            static final int S0_DIS_ALL = 0;
            static final int S1_EN_TREND_FILTER = 1;
            static final int S2_EN_PERF_FILTER = 2;
            static final int S3_EN_TREND_AND_PERF_FILTERS = 3;
            static final int S4_EN_VOL_FILTER = 4;
            static final int S5_EN_VOL_AND_TREND_FILTERS = 5;
            static final int S6_EN_VOL_AND_PERF_FILTERS = 6;
            static final int S7_EN_VOL_AND_PERF_AND_TREND_FILTERS = 7;
            static final int S8_EN_PRICE_FILTER = 8;
            static final int S9_EN_PRICE_AND_TREND_FILTERS = 9;
            static final int S10_EN_PRICE_AND_PERF_FILTERS = 10;
            static final int S11_EN_PRICE_AND_PERF_AND_TREND = 11;
            static final int S12_EN_PRICE_AND_VOLUME_FILTERS = 12;
            static final int S13_EN_PRICE_AND_VOLUME_AND_TREND = 13;
            static final int S14_EN_PRICE_AND_VOLUME_AND_PERF_FILTERS = 14;
            static final int S15_EN_PRICE_AND_VOLUME_AND_PERF_AND_TREND_FILTERS = 15;
            //Gain Price Volume Perf Trend
            static final int S16_EN_GAIN_FILTER = 16;   //1 0000
            static final int S17_EN_GAIN_AND_TREND_FILTERS_NOTALLOWED = 17; //1 0001
            static final int S18_EN_GAIN_AND_PERF_FILTERS_NOTALLOWED = 18;  //1 0010
            static final int S19_EN_GAIN_AND_PERF_AND_TREND_FILTERS_NOTALLOWED = 19; //1 0011
            static final int S20_EN_GAIN_AND_VOLUME_FILTER = 20;  //1 0100
            static final int S21_EN_GAIN_AND_VOLUME_AND_TREND_FILTERS_NOTALLOWED = 21; //1 0101
            static final int S22_EN_GAIN_AND_VOLUME_AND_PERF_FILTERS_NOTALLOWED = 22;  //1 0110
            static final int S23_EN_GAIN_AND_VOLUME_AND_PERF_AND_TREND_NOTALLOWED = 23;//1 0111
            static final int S24_EN_GAIN_AND_PRICE = 24;  //1 1000
            static final int S25_EN_GAIN_AND_PRICE_AND_TREND_NOTALLOWED = 25; //1 1001
            static final int S26_EN_GAIN_AND_PRICE_AND_PERF_FILTERS_NOTALLOWED = 26; //1 1010
            static final int S27_EN_GAIN_AND_PRICE_AND_PERF_AND_TREND_FILTERS_NOTALLOWED = 27; //1 1011
            static final int S28_EN_GAIN_AND_PRICE_AND_VOLUME_FILTERS = 28; //1 1100
            static final int S29_EN_GAIN_AND_PRICE_AND_VOLUME_AND_TREND_FILTERS_NOTALLOWED = 29; //1 1101
            static final int S30_EN_GAIN_AND_PRICE_AND_VOLUME_AND_PERF_FILTERS_NOTALLOWED = 30; //1 1110
            static final int S31_EN_GAIN_AND_PRICE_AND_VOLUME_AND_PERF_AND_TREND_FILTERS_NOTALLOWED = 31; //1 1111
            //MA Bounce   
            static final int S32_EN_MABOUNCE_FILTER = 32;   //10 0000             
            //ma bounce and Volume
            static final int S36_EN_MABOUNCE_AND_VOLUME_FILTER = 36;   //10 0100
            //ma bounce and Price
            static final int S40_EN_MABOUNCE_AND_PRICE_FILTER = 40;   //10 1000 
            //ma bounce && Price && Volume
            static final int S44_EN_MABOUNCE_AND_PRICE_AND_VOLUME_FILTER = 44;   //10 1100
            //channel
            static final int S44_EN_CHANNEL_FILTER = 64;   //100 0000
            //channel and Volume
            static final int S44_EN_CHANNEL_AND_VOLUME_FILTER = 68;   //100 0100
            //channel and Price
            static final int S44_EN_CHANNEL_AND_PRICE_FILTER = 72;   //100 1000
            //channel and Price and Volume
            static final int S44_EN_CHANNEL_AND_PRICE_AND_VOLUME_FILTER = 76;   //100 1100
            
            
            //boolean positions in array.
            private final int selChannelFilter = 6;
            private final int selBounceFilter = 5;
            private final int selGainFilter = 4;
            private final int selPriceFilter = 3;
            private final int selVolumeFilter = 2;
            private final int selPerfFilter = 1;
            private final int selTrendFilter = 0;

            /*
             selPriceFilter     selVolumeFilter  selPerfFilter    selTrendFilter result
             0                   0               0               0           Disable all.
             0                   0               0               1           enable Trend filter.
             0                   0               1               0           enable Perf filter.
             0                   0               1               1           enable Trend AND Perf filters
             0                   1               0               0           enable Volume filter only
             0                   1               0               1           enable Volume AND Trend fitlers
             0                   1               1               0           enable Volume AND Perf filters.
             0                   1               1               1           enalbe Volume AND Perf AND Trend filters only.
             1                   0               0               0           enable Price filter only.
             1                   0               0               1           enalbe Price AND Trend filters.
             1                   0               1               0           enable Price AND Perf filters.
             1                   0               1               1           enable Price AND Perf AND Trend filters.
             1                   1               0               0           enalbe Price AND Volume filters only.
             1                   1               0               1           enable Price AND Volume AND Trend filters. 
             1                   1               1               0           enable Price AND Volume AND Perf fitlers.
             1                   1               1               1           enalbe ALL: Price AND Volume AND Perf AND Trend Filters.
             */
            

            boolean[] selBitMap = {false, false, false, false, false, false, false};
            private final int TBL_SZ = selBitMap.length;
            public int selBitMapIntValue = 0;

            public selectionLogic() {
                clearSelBitMap();
                selBitMapIntValue = 0;

            }

            public void setTrendFilter(boolean setwith) {
                selBitMap[selTrendFilter] = setwith;
            }

            public void setPerfFilter(boolean setwith) {
                selBitMap[selPerfFilter] = setwith;
            }

            public void setVolumeFilter(boolean setwith) {
                selBitMap[selVolumeFilter] = setwith;
            }

            public void setPriceFilter(boolean setwith) {
                selBitMap[selPriceFilter] = setwith;
            }
            public void setGainFilter(boolean setwith) {
                selBitMap[selGainFilter] = setwith;
            }
            public void setBounceFilter(boolean setwith) {
                selBitMap[selBounceFilter] = setwith;
            }
            public void setChannelFilter(boolean setwith) {
                selBitMap[selChannelFilter] = setwith;
            }
            public boolean getChannelFilter() {
                return selBitMap[selChannelFilter];
            }
            public boolean getTrendFilter() {
                return (selBitMap[selTrendFilter]);
            }

            public boolean getPerfFilter() {
                return (selBitMap[selPerfFilter]);
            }

            public boolean getVolumeFitler() {
                return (selBitMap[selVolumeFilter]);
            }

            public boolean getPriceFilter() {
                return (selBitMap[selPriceFilter]);
            }
            public boolean getGainFilter() {
                return (selBitMap[selGainFilter]);
            }
            public boolean getBounceFilter() {
                return (selBitMap[selBounceFilter]);
            }
            public void clearSelBitMap() {
                Arrays.fill(selBitMap, Boolean.FALSE);
            }

            public int getSelectedStateValue() {
                int actBit = 0;
                for (actBit = 0, selBitMapIntValue = 0; actBit < TBL_SZ; actBit++) {
                    selBitMapIntValue |= (selBitMap[actBit] ? 1 : 0) << actBit;
                }
                return (selBitMapIntValue);
            }
        }
        
        
        @Override
        public void run() {

            boolean met50Day;
            boolean met100Day;
            boolean met200Day;
            boolean metHistorical;
            boolean metPerformance = false;
            boolean metBullBear = false;
            boolean metPricePerShare = false;
            boolean metVolume = false;
            boolean metGainRange = false;
            boolean met50MaBounce = false;
            boolean met100MaBounce = false;
            boolean met200MaBounce = false;
            boolean metChannelRange = false;
            int maMustBeAboveCount = 0;
            int maMustTouchCount = 0;
            int maPiercesAllowedCount = 0;
            boolean gainFilterOn = false;
            boolean bouncingOffMa = false;
            int trendFoundCnt = 0;
            int perfFoundCnt = 0;
            int volumeFoundCnt = 0;
            int priceFoundCnt = 0;
            int gainRangeCnt = 0;
            int maBounceCnt = 0;
            int channelRangeCnt = 0;
            allSlopes = saOuter.new SlopesFound();
            allSlopes.setSize(exchangeData.numberOfTickers);
            Streams actStreams;
            
            while (running) {
                try {

                    System.out.println("thread running.. " + runXs++);
                    
                    actStreams = new Streams(exchangeData);
                    //openStreams(exchangeData);
                    

                    for (int idx = 0; ((shutDown == false) && (idx < (exchangeData.numberOfTickers - 1))); idx++) {
                        actStreams.openStreams(idx);
                        actTicker = exchangeData.tickerDataHere[idx];
                        System.out.println("working on : " + actTicker + " (" + idx + ")");
                        resultsTextArea.append("\nSearching...working on: " + actTicker + " (" + idx + " / " + exchangeData.numberOfTickers + " )");
                        ibApi.historicalData hd = null;// = new ibApi.historicalData(); 
                        hd = actIbApi.setActHistoricalData(hd);
                        hd.nextTid(idx);
                        ibApi.historicalData.HistorySlope actHistory = hd.actWally;
                        if (hd.getHistoricalData(actTicker, "", "") == true) {
                            resultsTextArea.append("..OK.");                           
                            hd.calcSimpleMovingAve(MA_50DAY);
                            endDate50 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_100DAY);
                            endDate100 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_200DAY);
                            endDate200 = hd.endingDate;
                            hd.calcSimpleMovingAve(MA_10DAY);
                            endDate10 = hd.endingDate;
                            hd.calcPercentages();
                            hd.calcAveVolume(AVE_90DAY);
                            if ((maWindowSizes != null) && (maWindowSizes.allSet == true)){
                                hd.calcPercentages(maWindowSizes);
                                
                                bouncingOffMa = (hd.getTimesAbove(MA_50DAY) > (maWindowSizes.get50DaySz() - 5)
                                                 && hd.getTimesTouched(MA_50DAY) > 5);
                                if (bouncingOffMa == true){
                                    System.out.println("\n  Ticker: " + actTicker + " bouncing off 50dma..");
                                }else{
                                    
                                }
                                bouncingOffMa = (hd.getTimesAbove(MA_100DAY) > (maWindowSizes.get100DaySz() - 5)
                                                 && hd.getTimesTouched(MA_100DAY) > 5);
                                if (bouncingOffMa == true){
                                    System.out.println("\n  Ticker: " + actTicker + " bouncing off 100dma..");
                                }else{
                                    
                                }
                                bouncingOffMa = (hd.getTimesAbove(MA_200DAY) > (maWindowSizes.get200DaySz() - 5)
                                                 && hd.getTimesTouched(MA_200DAY) > 5);
                                if (bouncingOffMa == true){
                                    System.out.println("\n  Ticker: " + actTicker + " bouncing off 200dma..");
                                }else{
                                    
                                }
                            }else{
                                hd.calcPercentages();
                            }
                            if (bullBearCross.searchNow == true){
                                hd.checkForBullBearCrossing(bullBearCross);
                                if (bullBearCross.wePassed == true){
                                    System.out.println("\nPASSED!!!!!");
                                }
                            }
                            
                            todaysDate = hd.getTodaysDate();
                            actHistory = hd.findSlopes1(slopeDefs.DIRECTION_CHANGE, slopeDefs.MA_10DAY);
                            actHistory.setLongShort(userSelectedLongShort);
                            slopeStruct = saOuter.new SlopeStructure();
                            
                            //this is for volume and price filtering..
                            userSelectedLogic.setVolumeFilter(min90DayVolume > 0);
                            userSelectedLogic.setPriceFilter(minPricePerShare > 0);
                            //do perf and trend..
                            userSelectedLogic.setPerfFilter(selectedPerfFilter);
                            userSelectedLogic.setTrendFilter(selectedTrendFilter);
                            //do gain filter..
                            userSelectedLogic.setGainFilter(selectedGainFilter);
                            //do bounce filter..
                            userSelectedLogic.setBounceFilter(selectedMovingAveBounceFilter);
                            //do channel filter..
                            userSelectedLogic.setChannelFilter(selectedChannelFiltering);
                            
                            metVolume = ((min90DayVolume > 0) && (hd.aveVolume90Day > (double) min90DayVolume));
                            
                            if (selectedGainFilter == true){
                                metGainRange = (hd.yearsGainLoss > selectedChangeInGainLow) && (hd.yearsGainLoss < selectedChangeInGainHi);
                            }
                            
                            if (selectedMovingAveBounceFilter == true){
                                
                                /*calculate how many times we must be above ma ..
                                  This is a percentage of the window size selected..
                                  it must touch TouchMinPercent the rest of the time must be above ma.                                  
                                */
                                //do 50ma first...
      
                                maMustTouchCount = (int)Math.ceil((selectedMaMustTouchMinPercent / 100.0 ) * (double)(maWindowSizes.get50DaySz()));
                                maMustBeAboveCount = (maWindowSizes.get50DaySz() - maMustTouchCount);                                
                                /*calculate how many times we can pierce ma ..
                                  This is a percentage of the window size selected..
                                */
                                
                                maPiercesAllowedCount = (int)Math.ceil((selectedMaPiercedMaxPercent / 100.0) * (double)(maWindowSizes.get50DaySz()));                                
                                met50MaBounce = ((hd.getTimesAbove(MA_50DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_50DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_50DAY) <= maPiercesAllowedCount));
                                //do 100ma...
                                maMustTouchCount = (int)Math.ceil((selectedMaMustTouchMinPercent / 100.0) * (double) (maWindowSizes.get100DaySz()));
                                maMustBeAboveCount = (maWindowSizes.get100DaySz() - maMustTouchCount);                                
                                /*calculate how many times we can pierce ma ..
                                  This is a percentage of the window size selected..
                                */                                
                                maPiercesAllowedCount = (int)Math.ceil((selectedMaPiercedMaxPercent / 100.0) * (double) (maWindowSizes.get100DaySz()));                                
                               
                                met100MaBounce = ((hd.getTimesAbove(MA_100DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_100DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_100DAY) <= maPiercesAllowedCount));
                                //do 200ma...
                                maMustTouchCount = (int)Math.ceil((selectedMaMustTouchMinPercent / 100.0) * (double) (maWindowSizes.get200DaySz()));
                                maMustBeAboveCount = (maWindowSizes.get200DaySz() - maMustTouchCount);                                
                                /*calculate how many times we can pierce ma ..
                                  This is a percentage of the window size selected..
                                */                                
                                maPiercesAllowedCount = (int)Math.ceil((selectedMaPiercedMaxPercent / 100.0) * (double) (maWindowSizes.get200DaySz()));                                
                                
                                met200MaBounce = ((hd.getTimesAbove(MA_200DAY) > maMustBeAboveCount)
                                                 && (hd.getTimesTouched(MA_200DAY) >= maMustTouchCount)
                                                 && (hd.getTimesPierced(MA_200DAY) <= maPiercesAllowedCount));
                            }
                            if (minPricePerShare > 0){
                                qInfo = actChain.getQuote(actTicker, false);
                                metPricePerShare = ((qInfo.last > (double) minPricePerShare ));
                            }
                            //end volume and price filtering..
                            
                            if (selectedAboveMa == true) {
                                met50Day = ((selectedPercentOf50Ma != 0) && ((hd.getPercentAbove(MA_50DAY) * 100) > selectedPercentOf50Ma));
                                met100Day = ((selectedPercentOf100Ma != 0) && ((hd.getPercentAbove(MA_100DAY) * 100) > selectedPercentOf100Ma));
                                met200Day = ((selectedPercentOf200Ma != 0) && ((hd.getPercentAbove(MA_200DAY) * 100) > selectedPercentOf200Ma));
                            } else {
                                met50Day = ((selectedPercentOf50Ma != 0) && (((1.0 - hd.getPercentAbove(MA_50DAY)) * 100) > selectedPercentOf50Ma));
                                met100Day = ((selectedPercentOf100Ma != 0) && (((1.0 - hd.getPercentAbove(MA_100DAY)) * 100) > selectedPercentOf100Ma));
                                met200Day = ((selectedPercentOf200Ma != 0) && (((1.0 - hd.getPercentAbove(MA_200DAY)) * 100) > selectedPercentOf200Ma));
                            }
                            metPerformance = false;
                            if (selectedPerfFilter == true){
                                slopeStruct.addAllFound(actHistory, actTicker);
                                allSlopes.addOne(slopeStruct);
                                if ((slopeStruct.maxRunningProfitPercent >= selectedMaxRunningProfit) && 
                                    (Math.abs(slopeStruct.maxRunningLossPercent) <= selectedMaxRunningLoss)) {
                                    metPerformance = true;
                                }
                            }
                           
                            metBullBear = ((bullBearCross.enabled == true) && (bullBearCross.wePassed == true));
                            if (bullBearCross.enabled == true){
                                metHistorical = (met50Day && met100Day && met200Day && metBullBear);
                            }else{
                                metHistorical = (met50Day && met100Day && met200Day);
                            }
                            
                            hd.calcChannelRangePercentages();
                            metChannelRange = ((selectedChannelFiltering == true) && 
                                              (hd.getPercentMaxAbove(userSelectedMaInt) < userSelectedChannelRangePercent)
                            );
                            switch (userSelectedLogic.getSelectedStateValue()) {
                                case selectionLogic.S0_DIS_ALL: {
                                    //do nothing all disabled
                                }
                                break;
                                case selectionLogic.S1_EN_TREND_FILTER: {
                                    if (metHistorical == true) {
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                    }
                                }
                                break;
                                case selectionLogic.S2_EN_PERF_FILTER: {
                                    if (metPerformance == true) {
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                    }

                                }
                                break;
                                case selectionLogic.S3_EN_TREND_AND_PERF_FILTERS: {
                                    //check if AND operation..
                                    if (selectedTrendAndPerf == true){
                                        if ((metHistorical == true) && (metPerformance == true)) {
                                            trendTradeList.addOne(actTicker);
                                            trendFoundCnt++;
                                            trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                            dispHistoricalData(hd);

                                            performanceTradeList.addOne(actTicker);
                                            dispPerformanceData(slopeStruct);
                                            perfFoundCnt++;
                                            perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                        }
                                    }else{
                                        //do OR
                                         if(metHistorical == true){
                                            trendTradeList.addOne(actTicker);
                                            trendFoundCnt++;
                                            trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                            dispHistoricalData(hd);
                                         }
                                         if(metPerformance == true){
                                             performanceTradeList.addOne(actTicker);
                                            dispPerformanceData(slopeStruct);
                                            perfFoundCnt++;
                                            perfFoundLabel.setText(Integer.toString(perfFoundCnt));                                            
                                         }                                        
                                    }
                                }
                                break;
                                case selectionLogic.S4_EN_VOL_FILTER: {
                                    if (metVolume == true){
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }

                                }
                                break;
                                case selectionLogic.S5_EN_VOL_AND_TREND_FILTERS: {
                                    if ((metHistorical == true) && (metVolume == true)){
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }                                                                      
                                }
                                break;
                                case selectionLogic.S6_EN_VOL_AND_PERF_FILTERS: {
                                    if ((metVolume == true) && (metPerformance == true)){
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                    }                               
                                }
                                break;
                                case selectionLogic.S7_EN_VOL_AND_PERF_AND_TREND_FILTERS: {
                                    
                                    if ((metVolume == true) && (metPerformance == true) && (metHistorical == true)){
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                        
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                        
                                    }                                                                                              
                                }
                                break;
                                case selectionLogic.S8_EN_PRICE_FILTER: {
                                    if (metPricePerShare == true){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                    }                               
                                }
                                break;
                                case selectionLogic.S9_EN_PRICE_AND_TREND_FILTERS: {
                                    if ((metPricePerShare == true) && (metHistorical == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                    }                                        
                                }
                                break;
                                case selectionLogic.S10_EN_PRICE_AND_PERF_FILTERS: {
                                    if ((metPricePerShare == true) && (metPerformance == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                    }                                                                    
                                }
                                break;
                                case selectionLogic.S11_EN_PRICE_AND_PERF_AND_TREND: {
                                    
                                    if ((metPricePerShare == true) && (metPerformance == true) && (metHistorical == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                        
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                        
                                    }                                  
                                }
                                break;
                                case selectionLogic.S12_EN_PRICE_AND_VOLUME_FILTERS: {
                                    
                                    if ((metPricePerShare == true) && (metVolume == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }                                                                      
                                }
                                break;
                                case selectionLogic.S13_EN_PRICE_AND_VOLUME_AND_TREND: {
                                    
                                    if ((metPricePerShare == true) && (metVolume == true) && (metHistorical == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                                    }                                                                      
                                }
                                break;
                                case selectionLogic.S14_EN_PRICE_AND_VOLUME_AND_PERF_FILTERS: {
                                    if ((metPricePerShare == true) && (metVolume == true) && (metPerformance == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                    }                                                                      
                                }
                                break;
                                case selectionLogic.S15_EN_PRICE_AND_VOLUME_AND_PERF_AND_TREND_FILTERS: {
                                    
                                    if ((metPricePerShare == true) && (metVolume == true) && (metPerformance == true) && (metHistorical == true)){
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        performanceTradeList.addOne(actTicker);
                                        dispPerformanceData(slopeStruct);
                                        perfFoundCnt++;
                                        perfFoundLabel.setText(Integer.toString(perfFoundCnt));
                                        
                                        trendTradeList.addOne(actTicker);
                                        trendFoundCnt++;
                                        trendFoundLabel.setText(Integer.toString(trendFoundCnt));
                                        dispHistoricalData(hd);
                      
                                    }
                                }
                                break;
                                case selectionLogic.S16_EN_GAIN_FILTER: {
                                    if (metGainRange == true) {
                                        gainRangeTradeList.addOne(actTicker);
                                        gainRangeCnt++;
                                        gainRangeFoundLable.setText(Integer.toString(gainRangeCnt));
                                        dispGain(hd.yearsGainLoss);                                        
                                    }
                                }
                                break;                                
                                case selectionLogic.S17_EN_GAIN_AND_TREND_FILTERS_NOTALLOWED: 
                                case selectionLogic.S18_EN_GAIN_AND_PERF_FILTERS_NOTALLOWED: 
                                case selectionLogic.S19_EN_GAIN_AND_PERF_AND_TREND_FILTERS_NOTALLOWED: { 
                                    System.out.println("State NOT ALLOWED in Switch!!!");
                                }
                                break; 
                                case selectionLogic.S20_EN_GAIN_AND_VOLUME_FILTER: {
                                    if ((metGainRange == true) && (metVolume == true)) {
                                        gainRangeTradeList.addOne(actTicker);
                                        gainRangeCnt++;
                                        gainRangeFoundLable.setText(Integer.toString(gainRangeCnt));
                                        dispGain(hd.yearsGainLoss);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }
 
                                }
                                break; 
                                case selectionLogic.S21_EN_GAIN_AND_VOLUME_AND_TREND_FILTERS_NOTALLOWED: 
                                case selectionLogic.S22_EN_GAIN_AND_VOLUME_AND_PERF_FILTERS_NOTALLOWED: 
                                case selectionLogic.S23_EN_GAIN_AND_VOLUME_AND_PERF_AND_TREND_NOTALLOWED: {  
                                    System.out.println("State NOT ALLOWED in Switch!!!");
                                }
                                break; 
                                case selectionLogic.S24_EN_GAIN_AND_PRICE: { 
                                    if ((metGainRange == true) && (metPricePerShare == true)) {
                                        gainRangeTradeList.addOne(actTicker);
                                        gainRangeCnt++;
                                        gainRangeFoundLable.setText(Integer.toString(gainRangeCnt));
                                        dispGain(hd.yearsGainLoss);
                                        
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo); 
                                    }
                                }
                                break; 
                                case selectionLogic.S25_EN_GAIN_AND_PRICE_AND_TREND_NOTALLOWED:
                                case selectionLogic.S26_EN_GAIN_AND_PRICE_AND_PERF_FILTERS_NOTALLOWED: 
                                case selectionLogic.S27_EN_GAIN_AND_PRICE_AND_PERF_AND_TREND_FILTERS_NOTALLOWED: { 
                                    System.out.println("State NOT ALLOWED in Switch!!!");
                                }
                                break;    
                                case selectionLogic.S28_EN_GAIN_AND_PRICE_AND_VOLUME_FILTERS: {
                                    if ((metGainRange == true) && (metVolume == true) && (metPricePerShare == true)) {
                                        gainRangeTradeList.addOne(actTicker);
                                        gainRangeCnt++;
                                        gainRangeFoundLable.setText(Integer.toString(gainRangeCnt));
                                        dispGain(hd.yearsGainLoss);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                        
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo); 
                                    }
                                }
                                break;
                                case selectionLogic.S29_EN_GAIN_AND_PRICE_AND_VOLUME_AND_TREND_FILTERS_NOTALLOWED: 
                                case selectionLogic.S30_EN_GAIN_AND_PRICE_AND_VOLUME_AND_PERF_FILTERS_NOTALLOWED: 
                                case selectionLogic.S31_EN_GAIN_AND_PRICE_AND_VOLUME_AND_PERF_AND_TREND_FILTERS_NOTALLOWED: {
                                    System.out.println("State NOT ALLOWED in Switch!!!");
                                }
                                break; 
                                case selectionLogic.S32_EN_MABOUNCE_FILTER:
                                    if ((met50MaBounce == true) || (met100MaBounce == true) || (met200MaBounce == true)){
                                        maBounceTradeList.addOne(actTicker);
                                        maBounceCnt++;
                                        maBouncedFoundLabel.setText(Integer.toString(maBounceCnt));
                                        if (met50MaBounce == true){
                                            dispMaBounce(actTicker + " Bounced off 50DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_50DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_50DAY) +
                                                         " StartDate: " + hd.get50DayEndDate()
                                            );    
                                        }                                         
                                        if (met100MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 100DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_100DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_100DAY) +
                                                         " StartDate: " + hd.get100DayEndDate()
                                            ); 
                                        }
                                        if (met200MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 200DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_200DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_200DAY) +
                                                         " StartDate: " + hd.get200DayEndDate()
                                            ); 
                                        }                                       
                                    }
                                       
                                break;
                                case selectionLogic.S36_EN_MABOUNCE_AND_VOLUME_FILTER:
                                    if ((metVolume == true) && ((met50MaBounce == true) || (met100MaBounce == true) || (met200MaBounce == true))){
                                        maBounceTradeList.addOne(actTicker);
                                        maBounceCnt++;
                                        maBouncedFoundLabel.setText(Integer.toString(maBounceCnt));
                                        
                                        
                                        if (met50MaBounce == true){
                                            dispMaBounce(actTicker + " Bounced off 50DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_50DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_50DAY) +
                                                         " StartDate: " + hd.get50DayEndDate()
                                            );    
                                        }                                         
                                        if (met100MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 100DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_100DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_100DAY) +
                                                         " StartDate: " + hd.get100DayEndDate()
                                            ); 
                                        }
                                        if (met200MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 200DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_200DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_200DAY) +
                                                         " StartDate: " + hd.get200DayEndDate()
                                            ); 
                                        }  
                                        
                                        
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }
                                break; 
                                case selectionLogic.S40_EN_MABOUNCE_AND_PRICE_FILTER:
                                    if ((metPricePerShare == true) && ((met50MaBounce == true) || (met100MaBounce == true) || (met200MaBounce == true))){
                                        maBounceTradeList.addOne(actTicker);
                                        maBounceCnt++;
                                        maBouncedFoundLabel.setText(Integer.toString(maBounceCnt));
                                        if (met50MaBounce == true){
                                            dispMaBounce(actTicker + " Bounced off 50DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_50DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_50DAY) +
                                                         " StartDate: " + hd.get50DayEndDate()
                                            );    
                                        }                                         
                                        if (met100MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 100DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_100DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_100DAY) +
                                                         " StartDate: " + hd.get100DayEndDate()
                                            ); 
                                        }
                                        if (met200MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 200DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_200DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_200DAY) +
                                                         " StartDate: " + hd.get200DayEndDate()
                                            ); 
                                        }  
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo); 
                                    }
                                break;
                                case selectionLogic.S44_EN_MABOUNCE_AND_PRICE_AND_VOLUME_FILTER:
                                    if ((metVolume == true) && (metPricePerShare == true) && ((met50MaBounce == true) || (met100MaBounce == true) || (met200MaBounce == true))){
                                        maBounceTradeList.addOne(actTicker);
                                        maBounceCnt++;
                                        maBouncedFoundLabel.setText(Integer.toString(maBounceCnt));
                                        if (met50MaBounce == true){
                                            dispMaBounce(actTicker + " Bounced off 50DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_50DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_50DAY) +
                                                         " StartDate: " + hd.get50DayEndDate()
                                            );    
                                        }                                         
                                        if (met100MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 100DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_100DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_100DAY) +
                                                         " StartDate: " + hd.get100DayEndDate()
                                            ); 
                                        }
                                        if (met200MaBounce){
                                            dispMaBounce(actTicker + " Bounced off 200DayMa." + 
                                                         " Touched: "  + hd.getTimesTouched(MA_200DAY) + 
                                                         " Pierced: " + hd.getTimesPierced(MA_200DAY) +
                                                         " StartDate: " + hd.get200DayEndDate()
                                            ); 
                                        }  
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo); 
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }
                                    break;
                                case selectionLogic.S44_EN_CHANNEL_FILTER:
                                    if (metChannelRange == true) {
                                        channelTradeList.addOne(actTicker);
                                        channelRangeCnt++;
                                        channelRangeLabel.setText(Integer.toString(channelRangeCnt));
                                        dispChannelRange(hd.getPercentMaxAbove(userSelectedMaInt), hd.getPercentMaxAboveDate(userSelectedMaInt));                                        
                                    }
                                    break;
                                case selectionLogic.S44_EN_CHANNEL_AND_VOLUME_FILTER:
                                    if ((metChannelRange == true) && (metVolume == true)) {
                                        channelTradeList.addOne(actTicker);
                                        channelRangeCnt++;
                                        channelRangeLabel.setText(Integer.toString(channelRangeCnt));
                                        dispChannelRange(hd.getPercentMaxAbove(userSelectedMaInt), hd.getPercentMaxAboveDate(userSelectedMaInt));  
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }
                                       
                                    break;
                                case selectionLogic.S44_EN_CHANNEL_AND_PRICE_FILTER:
                                    if ((metChannelRange == true) && (metPricePerShare == true)) {
                                        channelTradeList.addOne(actTicker);
                                        channelRangeCnt++;
                                        channelRangeLabel.setText(Integer.toString(channelRangeCnt));
                                        dispChannelRange(hd.getPercentMaxAbove(userSelectedMaInt), hd.getPercentMaxAboveDate(userSelectedMaInt));  
                                        
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                    }
                                    break;
                                case selectionLogic.S44_EN_CHANNEL_AND_PRICE_AND_VOLUME_FILTER:
                                    if ((metChannelRange == true) && (metPricePerShare == true) && (metVolume == true)) {
                                        channelTradeList.addOne(actTicker);
                                        channelRangeCnt++;
                                        channelRangeLabel.setText(Integer.toString(channelRangeCnt));
                                        dispChannelRange(hd.getPercentMaxAbove(userSelectedMaInt), hd.getPercentMaxAboveDate(userSelectedMaInt));  
                                        
                                        priceTradeList.addOne(actTicker);
                                        priceFoundCnt++;
                                        priceFoundLabel.setText(Integer.toString(priceFoundCnt));
                                        dispPriceData(qInfo);
                                        
                                        volumeTradeList.addOne(actTicker);
                                        volumeFoundCnt++;
                                        volumeFoundLabel.setText(Integer.toString(volumeFoundCnt));
                                        dispVolumeData(hd);
                                    }
                                    break;
                                default: {
                                    System.out.println("State unknown in Switch!!!");

                                }
                            } /* switch */

                        } else {
                            System.out.println("Error Getting Historical Data: " + hd.getErrorMsg());
                            resultsTextArea.append("..Bad.");
                        }
                        runFilterThread.sleep(ELEVEN_SECS);
                        if (shutDown == true) {
                            System.out.println("\n shutting down...");
                            resultsTextArea.append("\n shutting down..");
                        }
                    } /*for*/
                    
                    actChain.cancelChainStreams();
                    running = false;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } /* while */
            System.out.println("done...");
            resultsTextArea.append("\ndone..merging tradeLists..");
            mergeTradeListsToActTradeList();
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

        if (exchange.equals(NASQ1)) {
            actTickersFile = homeDirectory + "nasdaqTickers1.txt.wo";
        } else if (exchange.equals(NASQ2)) {
            actTickersFile = homeDirectory + "nasdaqTickers2.txt.wo";
        } else if (exchange.equals(NASQ3)) {
            actTickersFile = homeDirectory + "nasdaqTickers3.txt.wo";
        } else if (exchange.equals(NASQ4)) {
            actTickersFile = homeDirectory + "nasdaqTickers4.txt.wo";
        } else if (exchange.equals(NYSEQ1)) {
            actTickersFile = homeDirectory + "nyseTickers1.txt.wo";
        } else if (exchange.equals(NYSEQ2)) {
            actTickersFile = homeDirectory + "nyseTickers2.txt.wo";
        } else if (exchange.equals(NYSEQ3)) {
            actTickersFile = homeDirectory + "nyseTickers3.txt.wo";
        } else if (exchange.equals(NYSEQ4)) {
            actTickersFile = homeDirectory + "nyseTickers4.txt.wo";
        } else if (exchange.equals(NYSEQ5)) {
            actTickersFile = homeDirectory + "nyseTickers5.txt.wo";
        } else if (exchange.equals(NYSEQ6)) {
            actTickersFile = homeDirectory + "nyseTickers6.txt.wo";
        } else if (exchange.equals(NYSEQ7)) {
            actTickersFile = homeDirectory + "nyseTickers7.txt.wo";
        } else if (exchange.equals(NYSEQ8)) {
            actTickersFile = homeDirectory + "nyseTickers8.txt.wo";
        } else if (exchange.equals(WATCH1)) {
            actTickersFile = homeDirectory + "watch1Tickers.txt.wo";
        } else {
            actTickersFile = null;
            return null;
        }
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
            java.util.logging.Logger.getLogger(positionCreationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(positionCreationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(positionCreationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(positionCreationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        final List<String> posList = new ArrayList<>();
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                positionCreationDialog dialog = new positionCreationDialog(new javax.swing.JFrame(), true,  posList);
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
    private javax.swing.JLabel aboveBelowLabel;
    private javax.swing.JCheckBox andCheckBox;
    private javax.swing.JCheckBox appendCheckBox;
    private javax.swing.JCheckBox belowMaCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel channelRangeLabel;
    private javax.swing.JTextField channelRangePercentTextField;
    private javax.swing.JButton clearAllFieldsButton;
    private javax.swing.JLabel criteriaLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JComboBox exchangeComboBox;
    private javax.swing.JTextField fiftyDayTextField;
    private javax.swing.JLabel gainRangeFoundLable;
    private javax.swing.JTextField hundredDayTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel maBouncedFoundLabel;
    private javax.swing.JComboBox maComboBox;
    private javax.swing.JTextField maxRunningLossTextField;
    private javax.swing.JTextField maxRunningProfitTextField;
    private javax.swing.JButton moreFiltersButton;
    private javax.swing.JLabel perfFoundLabel;
    private javax.swing.JLabel posBiasLable;
    private javax.swing.JLabel priceFoundLabel;
    private javax.swing.JTextArea resultsTextArea;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel trendFoundLabel;
    private javax.swing.JTextField twoHundredDayTextField;
    private javax.swing.JLabel volumeFoundLabel;
    // End of variables declaration//GEN-END:variables
}
