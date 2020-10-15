/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import ibTradeApi.ibApi.quoteInfo;
import ibTradeApi.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellRenderer;
import positions.*;


/**
 *
 * @author earlie87
 */
public class displayOptionChainDialogForm extends javax.swing.JDialog {
    
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    private ibApi.quoteInfo cInfo = new ibApi.quoteInfo();
    
    private ibApi actIbApi = ibApi.getActApi();
    private int chainSize;
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    //optionChainTable
    private final int CALL = 0;
    private final int PUT = 1;
    private final int sTICKER = 0;
    private final int sLAST = 1;
    private final int sBID = 2;
    private final int sASK = 3;
    private final int sVOLUME = 4;
    private final int oLAST = 0;
    private final int oBID = 1;
    private final int oASK = 2;
    private final int oOI = 3;   
    private final int oVOLUME = 4;
    private final int oDELTA = 5;
    private final int oSTRIKE = 6;
    //stockQuoteTable
    //columns
    private final int oSQT_UNDERLYING = 0;
    private final int oSQT_LAST = 1;
    private final int oSQT_BID = 2;
    private final int oSQT_ASK = 3;
    private final int oSQT_VOLUME = 4;
    
    private final int CHAIN_ARRAY_PCSZ = 2;
    private final int CHAIN_ARRAY_ENTRIES = 800;
    private quoteInfo[][] optionChainArray = new quoteInfo[CHAIN_ARRAY_PCSZ][CHAIN_ARRAY_ENTRIES];
    private int chainNum;
    private int optionChainCallArraySz = 0;
    private int optionChainPutArraySz = 0;
    private int cIdx = 0;
    private int pIdx = 0;
    private int pcIdx = 0;
    private int userMonthIdx = 0;
    String userTicker = null;
    String lastTicker = null;
    String userMonth = null;
    String lastUserMonth = null;
    String userFormedOptionDate = null;
    private final int CHAIN_MONTH_SZ = 25;
    String[] optionChainMonthList = new String[CHAIN_MONTH_SZ];
    String[] optionChainMonthOptionSymbol = new String[CHAIN_MONTH_SZ];
    public posCreateData retData;
    StrategyBuilder strategyBuilder = null;
    OptionChainTask optionChainTask;
    int selectedRange = 7;
    private boolean iamStillRunning = false;
    class OCTCustomColors{
        boolean enabled = false;  
        Color originalColor;
        int staARow;
        int staACol;        
        Color colorA;
        public OCTCustomColors(){
            enabled = false;
            originalColor = null;
        }
        public void setEnabled(boolean enin){
            enabled = enin;
        }
        public boolean isEnabled(){
            return enabled;
        }
    }
    OCTCustomColors octCustomColors = new OCTCustomColors();
    /**
     * Creates new form displayOptionChainDialogForm
     */
    public displayOptionChainDialogForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        iamStillRunning = true;
        retData = new posCreateData();
        optionChainTask = new OptionChainTask();
        optionChainTask.start();
        selectedRange = Integer.parseInt(selectRangeComboBox.getSelectedItem().toString());
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        iamStillRunning = false;
                        setVisible(false);
                        dispose();
                    }
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        iamStillRunning = false;
                        setVisible(false);
                        dispose();
                    }
                });
        
    }
    public class posCreateData {
        boolean dataReady;
        String stockTicker;
        String optionTicker;
        public posCreateData() {
            dataReady = false;
            stockTicker = null;
            optionTicker = null;
        }
    }
    public void setStillRunning(boolean runin){
        iamStillRunning = runin;
    }
    public boolean getStillRunning(){
        return iamStillRunning;
    }
    public void setTickerToStart(String tickerin){
        
        userTicker = tickerin;
        tickerTextField.setText(userTicker);
        
    }
    public void setActivePortfolio(positions posin) {
        
    }
    private boolean fillMyOptionChainOld(String ticker, Boolean justUpdate) {
        String tmpStrike = null;
        
        //initialize the option Chain array first..
        for(int pcIdx = 0; pcIdx < CHAIN_ARRAY_PCSZ; pcIdx++) {
            for (int idx = 0; idx < CHAIN_ARRAY_ENTRIES; idx++) {
                optionChainArray[pcIdx][idx] = null;
            }
        }
        if (justUpdate == false) {
            actChain.startNewChain();
            if (actChain.getOptionChain(ticker) == false){
                //failed!!
                return false;
            }
            userMonth = null;
            
        }
        
        actChain.chainIdxReset();
        chainSize = actChain.chainLeft();   
        /* we should get call then put with the same strike as pairs. 
         * if the strike price changes we increment the store idx (pcIdx).
         * That's why we store the tmpStrike.
         */
        for (chainNum = 0, pcIdx = 0; chainNum <= chainSize;) {
            cInfo = actChain.chainGetNextOrdered(chainNum++);            
            if ((cInfo != null) && (cInfo.enStreaming == true)) {
                
                /*
                 * check if we should move to next pair via bump pcidx
                 * take care of initial entry where tmpStrike == null.
                 */
                if (tmpStrike == null) {
                    tmpStrike = String.valueOf(cInfo.strikePrice);
                } else {
                    if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                        /*
                         * strike changed so bump to next pair
                         */
                        pcIdx++;
                        tmpStrike = String.valueOf(cInfo.strikePrice);
                    }else{
                        
                    }
                }
                qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                         * option
                         */);
                
                if (cInfo.cpType.equals("C")) {
                    /*
                     * was the call, so save then read for PUT
                     */
                    qInfo.optionSymbol = cInfo.optionSymbol;
                    qInfo.underlying = cInfo.underlying;
                    optionChainArray[CALL][pcIdx] = qInfo;

                    cInfo = actChain.chainGetNextOrdered(chainNum);
                    if ((cInfo != null) && (cInfo.enStreaming == true)) {
                        /* check if we should move to next pair via bump pcidx */
                        if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                            /* strike changed so bump to next pair */
                            pcIdx++;
                            tmpStrike = String.valueOf(cInfo.strikePrice);
                        }
                        qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                                 * option
                                 */);
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        optionChainArray[PUT][pcIdx] = qInfo;
                    } else {
                    }

                } else {
                    /*
                     * must be PUT, so save then read for CALL
                     */
                    qInfo.optionSymbol = cInfo.optionSymbol;
                    qInfo.underlying = cInfo.underlying;
                    optionChainArray[PUT][pcIdx] = qInfo;

                    cInfo = actChain.chainGetNextOrdered(chainNum);
                    if ((cInfo != null) && (cInfo.enStreaming == true)) {
                        /* check if we should move to next pair via bump pcidx */
                        if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                            /* strike changed so bump to next pair */
                            pcIdx++;
                            tmpStrike = String.valueOf(cInfo.strikePrice);
                        }
                        qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                                 * option
                                 */);
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        optionChainArray[CALL][pcIdx] = qInfo;
                    } else {
                    }

                }
                chainNum++;
            }

        }
        optionChainCallArraySz = pcIdx;
        optionChainPutArraySz = pcIdx;
        return true;
    }
    
    private boolean fillMyOptionChain(String ticker, Boolean justUpdate) {
        String tmpStrike = null;
        
        //initialize the option Chain array first..
        for(int pcIdx = 0; pcIdx < CHAIN_ARRAY_PCSZ; pcIdx++) {
            for (int idx = 0; idx < CHAIN_ARRAY_ENTRIES; idx++) {
                optionChainArray[pcIdx][idx] = null;
            }
        }
        actChain.setFilterRange(selectedRange);
        if (justUpdate == false) {
            actChain.startNewChain();
            if (actChain.getOptionChain(ticker) == false){
                //failed!!
                return false;
            }
            userMonth = null;            
        }        
        actChain.chainIdxReset();
        chainSize = actChain.chainLeft();   
        /* we should get call then put with the same strike as pairs. 
         * if the strike price changes we increment the store idx (pcIdx).
         * That's why we store the tmpStrike.
         */
        for (chainNum = 0, pcIdx = 0; chainNum <= chainSize;) {
            cInfo = actChain.chainGetNextOrdered(chainNum++);            
            if ((cInfo != null) && (cInfo.enStreaming == true)) {               
                /*
                 * check if we should move to next pair via bump pcidx
                 * take care of initial entry where tmpStrike == null.
                 */
                if (tmpStrike == null) {
                    tmpStrike = String.valueOf(cInfo.strikePrice);
                } else {
                    if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                        /*
                         * strike changed so bump to next pair
                         */
                        pcIdx++;
                        tmpStrike = String.valueOf(cInfo.strikePrice);
                    }else{
                        
                    }
                }
                qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                         * option
                         */);
                
                if (cInfo.cpType.equals("C")) {
                    /*
                     * was the call, so save then read for PUT
                     */
                    qInfo.optionSymbol = cInfo.optionSymbol;
                    qInfo.underlying = cInfo.underlying;
                    optionChainArray[CALL][pcIdx] = qInfo;
if(true){
                    cInfo = actChain.chainGetNextOrdered(chainNum);
                    if ((cInfo != null) && (cInfo.enStreaming == true)) {
                        /* check if we should move to next pair via bump pcidx */
                        if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                            /* strike changed so bump to next pair */
                            pcIdx++;
                            tmpStrike = String.valueOf(cInfo.strikePrice);
                        }
                        qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                                 * option
                                 */);
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        if(cInfo.cpType.equals("P")){
                            optionChainArray[PUT][pcIdx] = qInfo;
                        }else{
                            //must be CALL
                            optionChainArray[CALL][pcIdx] = qInfo;
                        }                                               
                    } else {
                    }
}
                } else if (cInfo.cpType.equals("P")) {
                    /*
                     * must be PUT, so save then read for CALL
                     */
                    qInfo.optionSymbol = cInfo.optionSymbol;
                    qInfo.underlying = cInfo.underlying;
                    optionChainArray[PUT][pcIdx] = qInfo;
if(true){
                    cInfo = actChain.chainGetNextOrdered(chainNum);
                    if ((cInfo != null) && (cInfo.enStreaming == true)) {
                        /* check if we should move to next pair via bump pcidx */
                        if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                            /* strike changed so bump to next pair */
                            pcIdx++;
                            tmpStrike = String.valueOf(cInfo.strikePrice);
                        }
                        qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                                 * option
                                 */);
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        if(cInfo.cpType.equals("P")){
                            optionChainArray[PUT][pcIdx] = qInfo;
                        }else{
                            //must be CALL
                            optionChainArray[CALL][pcIdx] = qInfo;
                        }
                    } else {
                    }
}
                }
                chainNum++;
            }
        }
        optionChainCallArraySz = pcIdx;
        optionChainPutArraySz = pcIdx;
        return true;
    }
    
    
    
    
    private void updateTable(String monthin) {
        int line = 0;
        int rowCount = 0;
        int lpCnt = 0;
        Double strike =0.0;
        int strikeRow = -1;
        boolean foundStrike = false;
        octCustomColors = new OCTCustomColors();
        octCustomColors.setEnabled(true);
        Double stockPriceLast = (Double)stockQuoteTable.getModel().getValueAt(0, oSQT_LAST);
  
        for (line = 0, lpCnt = 0; lpCnt < optionChainCallArraySz; lpCnt++ ) {
            if (((optionChainArray[CALL][lpCnt] != null) && 
                 (optionChainArray[PUT][lpCnt] != null)) && 
                 (monthin.equals(optionChainArray[CALL][lpCnt].optionDate))) { 
                
                optionChainTable.getModel().setValueAt(Double.toString(myUtils.roundMe(optionChainArray[CALL][lpCnt].last,2)), line, oLAST);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[CALL][lpCnt].bid), line, oBID);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[CALL][lpCnt].ask), line, oASK);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[CALL][lpCnt].cOpenInterest), line, oOI);
                optionChainTable.getModel().setValueAt(Integer.toString(optionChainArray[CALL][lpCnt].volume), line, oVOLUME);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[CALL][lpCnt].delta), line, oDELTA);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[CALL][lpCnt].strikePrice), line, oSTRIKE);
                
                optionChainTable.getModel().setValueAt(Double.toString(myUtils.roundMe(optionChainArray[PUT][lpCnt].last, 2)), line, oLAST + 7);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[PUT][lpCnt].bid), line, oBID + 7);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[PUT][lpCnt].ask), line, oASK + 7);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[PUT][lpCnt].pOpenInterest), line, oOI + 7);
                optionChainTable.getModel().setValueAt(Integer.toString(optionChainArray[PUT][lpCnt].volume), line, oVOLUME + 7);
                optionChainTable.getModel().setValueAt(Double.toString(optionChainArray[PUT][lpCnt].delta), line, oDELTA + 7);
                strike = Double.valueOf((String)optionChainTable.getModel().getValueAt(line, oSTRIKE));
                line++;
            }
        }
        //figure out at the money strike..
        for(rowCount = line--, line = 0; ((line < rowCount) && !foundStrike); line++){
            strike = Double.valueOf((String)optionChainTable.getModel().getValueAt(line, oSTRIKE));
            if(strike > stockPriceLast){
                foundStrike = true;
            }
        }
        octCustomColors.staARow = line-1;
        octCustomColors.staACol = 0;
        octCustomColors.colorA = Color.LIGHT_GRAY;
        
    }
    private void fillMyOptionChainMonthList() {
        String tmpMonth = null;
        String tmpOptionDate = null;
        int qIdx = 0;
        int lIdx = 0;
        
        //clear out these arrays...so we can fill them fresh...
        for(int i = 0; i < CHAIN_MONTH_SZ; i++) {
            optionChainMonthList[i] = null;
            optionChainMonthOptionSymbol[i] = null;
        }
        // could be null in this array so check...
        while((optionChainArray[CALL][qIdx] == null) && (qIdx < optionChainArray.length)) {
            qIdx++;            
        }
        if(qIdx >= optionChainArray.length){
            System.out.println("\nfillMyOptionChainMonthList: qIdx out of range:" + qIdx);
        }
        tmpOptionDate = optionChainArray[CALL][qIdx].optionSymbol;
        tmpMonth = optionChainArray[CALL][qIdx++].optionDate;
        /* store the first month */
        optionChainMonthOptionSymbol[lIdx] = tmpOptionDate;
        optionChainMonthList[lIdx++] = tmpMonth;
        while ((qIdx < optionChainCallArraySz) && (lIdx < CHAIN_MONTH_SZ)) {
            if (optionChainArray[CALL][qIdx] == null) {
                
            } else {
                if (!tmpMonth.equals(optionChainArray[CALL][qIdx].optionDate)) {
                    /*
                     * found new option Date month so store in list
                     */
                    optionChainMonthOptionSymbol[lIdx] = optionChainArray[CALL][qIdx].optionSymbol;
                    optionChainMonthList[lIdx++] = optionChainArray[CALL][qIdx].optionDate;
                    if(lIdx >= CHAIN_MONTH_SZ){
                        System.out.println("lIdx too big!!!--->"+lIdx);
                    }
                    tmpMonth = optionChainArray[CALL][qIdx].optionDate;
                }
            }
            qIdx++;
        }
        System.out.println("MonthList is " + lIdx + "in size.");
        monthSelectComboBox.removeAllItems();
        for (qIdx = 0; qIdx < lIdx; qIdx++) {
            monthSelectComboBox.addItem(optionChainMonthList[qIdx]);           
        }
    }
    private void clearOptionChainTable() {
        int rowCount = optionChainTable.getRowCount();
        int colCount = optionChainTable.getColumnCount();
        for (int il = 0; il < rowCount; il++){
            for (int ic = 0; ic < colCount; ic++) {
                optionChainTable.getModel().setValueAt("", il, ic);
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        stockQuoteTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        optionChainTable = new javax.swing.JTable(){

            public Component prepareRenderer(TableCellRenderer r, int row, int col){
                Component comp = super.prepareRenderer (r, row, col);

                if (octCustomColors.originalColor == null){
                    //save original color.
                    octCustomColors.originalColor = comp.getBackground();
                }
                if(!octCustomColors.isEnabled()){
                    return comp;
                }

                if((octCustomColors.staARow == row) && (col == 0)){
                    comp.setBackground(octCustomColors.colorA);
                    //comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                }
                if((octCustomColors.staARow+1 == row) && (col == 0)){
                    comp.setBackground(octCustomColors.originalColor);
                    //comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                }
                return comp;
            }

        };
        tickerTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        updateButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        monthSelectComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        statusLable = new javax.swing.JLabel();
        closeButton = new javax.swing.JButton();
        optionSymbolLabel = new javax.swing.JLabel();
        strategyBuilderButton = new javax.swing.JButton();
        selectRangeComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        stockQuoteTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "Underlying", "Last", "Bid", "Ask", "Volume"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        stockQuoteTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stockQuoteTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(stockQuoteTable);

        optionChainTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Last", "Bid", "Ask", "OI", "Vol", "delta", "Strike", "Last", "Bid", "Ask", "OI", "Vol", "delta"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        optionChainTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        optionChainTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                optionChainTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(optionChainTable);

        tickerTextField.setToolTipText("Enter Ticker Here");
        tickerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tickerTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Ticker");

        updateButton.setText("Update");
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("/--------------------------Call--------------------------------\\");

            jLabel3.setText("/---------------------------Put-------------------------------------\\");

                monthSelectComboBox.setToolTipText("Select Option Month");
                monthSelectComboBox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        monthSelectComboBoxActionPerformed(evt);
                    }
                });

                jLabel4.setText("Select Month");

                jLabel6.setText("OptionSymbol:");

                closeButton.setText("Close");
                closeButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        closeButtonActionPerformed(evt);
                    }
                });

                strategyBuilderButton.setText("StrategyBuilder");
                strategyBuilderButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        strategyBuilderButtonActionPerformed(evt);
                    }
                });

                selectRangeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2", "4", "6", "8", "10", "12", "14", "16" }));
                selectRangeComboBox.setSelectedIndex(3);
                selectRangeComboBox.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectRangeComboBoxActionPerformed(evt);
                    }
                });

                jLabel5.setText("Select Range");

                org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
                jPanel1.setLayout(jPanel1Layout);
                jPanel1Layout.setHorizontalGroup(
                    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jScrollPane1)
                        .add(57, 57, 57))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(statusLable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 311, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(84, 84, 84)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(tickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 116, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(103, 103, 103)
                                .add(jLabel5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(selectRangeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(98, 98, 98)
                                .add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(monthSelectComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(128, 128, 128)
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(optionSymbolLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 210, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel2)
                                .add(18, 18, 18)
                                .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .add(39, 39, 39))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 942, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(143, 143, 143)
                        .add(closeButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(updateButton)
                        .add(113, 113, 113)
                        .add(strategyBuilderButton)
                        .add(405, 405, 405))
                );
                jPanel1Layout.setVerticalGroup(
                    jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(tickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(jLabel1))
                            .add(statusLable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(monthSelectComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel4)
                                    .add(jLabel6)
                                    .add(selectRangeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel5)))
                            .add(optionSymbolLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(closeButton)
                            .add(updateButton)
                            .add(strategyBuilderButton))
                        .addContainerGap(40, Short.MAX_VALUE))
                );

                org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                    layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                );

                pack();
            }// </editor-fold>//GEN-END:initComponents

    private void tickerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tickerTextFieldActionPerformed
        
        userTicker = tickerTextField.getText();
        lastTicker = "";
        needUpdate = true;
    }//GEN-LAST:event_tickerTextFieldActionPerformed
    boolean optionChainTaskRunning = false;
    private class OptionChainTask extends Thread{
        OptionChainTask() {
            optionChainTaskRunning = true;
        }

        @Override
        public void run() {
            while (optionChainTaskRunning == true) {
                if (userTicker != null) {
                    qInfo = actIbApi.getQuote(userTicker, false /*option?*/);
                    stockQuoteTable.getModel().setValueAt(userTicker, 0, sTICKER);
                    stockQuoteTable.getModel().setValueAt(qInfo.last, 0, sLAST);
                    stockQuoteTable.getModel().setValueAt(qInfo.bid, 0, sBID);
                    stockQuoteTable.getModel().setValueAt(qInfo.ask, 0, sASK);
                    stockQuoteTable.getModel().setValueAt(qInfo.volume, 0, sVOLUME);
                    if (needUpdate == true) {
                        clearOptionChainTable();
                        if (userTicker.equals(lastTicker)) {
                            if (fillMyOptionChain(userTicker, true /* just update */) == false){
                                statusLable.setText("Getting option chain...failed.");
                                System.out.println("getting option chain..failed.");
                            }else{
                                
                            }
                        } else {
                            statusLable.setText("Getting option chain...wait");
                            System.out.println("getting option chain..wait...");
                            if (fillMyOptionChain(userTicker, false /* get new chain */) == false) {
                                statusLable.setText("Getting option chain...failed.");
                                System.out.println("getting option chain..failed.");
                            } else {
                                statusLable.setText("getting option chain..done.");
                                System.out.println("done.\n");
                                lastTicker = userTicker;
                                fillMyOptionChainMonthList();
                                lastUserMonth = userMonth;                                
                            }
                        }
                        needUpdate = false;
                        if (userMonth != null) {
                            updateTable(userMonth);
                        } else {

                        }
                    }
                }
                myUtils.delay(100);
        }
    }
}
    boolean needUpdate = false;
    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed

        needUpdate = true;
        
    }//GEN-LAST:event_updateButtonActionPerformed

    private void monthSelectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthSelectComboBoxActionPerformed
        if (monthSelectComboBox.getItemCount() > 0 ) {
            userMonth = monthSelectComboBox.getSelectedItem().toString();
            userMonthIdx = monthSelectComboBox.getSelectedIndex();    
        };
        
    }//GEN-LAST:event_monthSelectComboBoxActionPerformed

    private void optionChainTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_optionChainTableMouseClicked
        int row = 0;
        int col = 0;
        String selMonth = null;
        int lpCnt = 0;
        String optionSym = null;
        String underlying = null;
        boolean foundit = false;
        String strike = null;
        try {
            row = optionChainTable.getSelectedRow();
            col = optionChainTable.getSelectedColumn();                    
            System.out.println("optionChainTalbeMouseClicked: " + "row, col is : " + row + ", " + col);
        }catch(Exception e) {
            System.out.println("optionChainTalbeMouseClicked: Exception!!" + evt);   
        }
        /* need to search for selected month and option with in.. */
        selMonth = userMonth;        
        strike = String.valueOf(optionChainTable.getModel().getValueAt(row, oSTRIKE));        
        if ((selMonth != null) && (optionChainMonthList[userMonthIdx] != null)) {
            selMonth = optionChainMonthList[userMonthIdx];
            for (lpCnt = 0; !foundit && (lpCnt < optionChainCallArraySz); lpCnt++) {
                if (       (optionChainArray[CALL][lpCnt] != null)
                        && (optionChainArray[PUT][lpCnt] != null)
                        && (selMonth.equals(optionChainArray[CALL][lpCnt].optionDate))
                        && (strike.equals(Double.toString(optionChainArray[CALL][lpCnt].strikePrice)))) {
                    /*
                     * found it, now use row to index into this area of array
                     */
                   // optionSym = optionChainArray[PUT][lpCnt + row].optionSymbol;
                   // underlying = optionChainArray[PUT][lpCnt + row].underlying;
                     optionSym = optionChainArray[PUT][lpCnt].optionSymbol;
                     underlying = optionChainArray[PUT][lpCnt].underlying;
                    foundit = true;
                }
            }

        }

        System.out.println("optionChainTableMouseClicked: optionSym found is" + optionSym);

        optionSymbolLabel.setText(optionSym);
        retData.dataReady = true;
        retData.optionTicker = optionSym;
        retData.stockTicker = underlying;
        
        switch (col) {
                case oASK:
                    if(foundit == true){
                        strategyBuilder.setOperation("BUY");
                        strategyBuilder.setSymbol(optionChainArray[CALL][lpCnt-1].optionSymbol);
                        strategyBuilder.setStrike(optionChainArray[CALL][lpCnt-1].strikePrice);
                        strategyBuilder.setOptionType(optionChainArray[CALL][lpCnt-1].cpType);
                        strategyBuilder.setOptionDate(optionChainArray[CALL][lpCnt-1].optionDate);
                    }                    
                    break;
                case oBID:
                    if(foundit == true){
                        strategyBuilder.setOperation("SELL");
                        strategyBuilder.setSymbol(optionChainArray[CALL][lpCnt-1].optionSymbol);
                        strategyBuilder.setStrike(optionChainArray[CALL][lpCnt-1].strikePrice);
                        strategyBuilder.setOptionType(optionChainArray[CALL][lpCnt-1].cpType);
                        strategyBuilder.setOptionDate(optionChainArray[CALL][lpCnt-1].optionDate);
                    } 
                    break;
                case oASK + 7:
                    if(foundit == true){
                        strategyBuilder.setOperation("BUY");
                        strategyBuilder.setSymbol(optionChainArray[PUT][lpCnt-1].optionSymbol);
                        strategyBuilder.setStrike(optionChainArray[PUT][lpCnt-1].strikePrice);
                        strategyBuilder.setOptionType(optionChainArray[PUT][lpCnt-1].cpType);
                        strategyBuilder.setOptionDate(optionChainArray[PUT][lpCnt-1].optionDate);
                    }
                    break;
                case oBID + 7:
                    if(foundit == true){
                        strategyBuilder.setOperation("SELL");
                        strategyBuilder.setSymbol(optionChainArray[PUT][lpCnt-1].optionSymbol);
                        strategyBuilder.setStrike(optionChainArray[PUT][lpCnt-1].strikePrice);
                        strategyBuilder.setOptionType(optionChainArray[PUT][lpCnt-1].cpType);
                        strategyBuilder.setOptionDate(optionChainArray[PUT][lpCnt-1].optionDate);
                    }
                    break;
            }    
    }//GEN-LAST:event_optionChainTableMouseClicked

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        System.out.println("\nCancelling chain Streams...");
        actChain.cancelChainStreams();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void stockQuoteTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stockQuoteTableMouseClicked
        // TODO add your handling code here:
        int row, col;
        String ticker = "";
        try {
            row = stockQuoteTable.getSelectedRow();
            col = stockQuoteTable.getSelectedColumn();
            ticker = String.valueOf(stockQuoteTable.getModel().getValueAt(row, sTICKER));
            switch (col) {
                case oASK+1:
                    strategyBuilder.setOperation("BUY");
                    strategyBuilder.setSymbol(ticker);
                    break;
                case oBID+1:
                    strategyBuilder.setOperation("SELL");
                    strategyBuilder.setSymbol(ticker);
                    break;
            }
            System.out.println("stockQuoteTableMouseClicked: " + "row, col is : " + row + ", " + col);
        }catch(Exception e) {
            System.out.println("stockQuoteTableMouseClicked: Exception!!" + evt);   
        }
    }//GEN-LAST:event_stockQuoteTableMouseClicked

    private void strategyBuilderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strategyBuilderButtonActionPerformed
        // TODO add your handling code here:
        strategyBuilder = new StrategyBuilder(new javax.swing.JFrame(), false);
        strategyBuilder.setVisible(true);
    }//GEN-LAST:event_strategyBuilderButtonActionPerformed

    private void selectRangeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectRangeComboBoxActionPerformed
        // TODO add your handling code here:
        selectedRange = Integer.parseInt(selectRangeComboBox.getSelectedItem().toString());
    }//GEN-LAST:event_selectRangeComboBoxActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JComboBox monthSelectComboBox;
    private javax.swing.JTable optionChainTable;
    private javax.swing.JLabel optionSymbolLabel;
    private javax.swing.JComboBox selectRangeComboBox;
    private javax.swing.JLabel statusLable;
    private javax.swing.JTable stockQuoteTable;
    private javax.swing.JButton strategyBuilderButton;
    private javax.swing.JTextField tickerTextField;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
}
