/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.commonGui;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class StrategyBuilder extends javax.swing.JDialog {

    /**
     * Creates new form StrategyBuilder
     */
    BuilderTask btask = null;
    boolean taskRunning = false;
    String operationIn = "";
    String symbolIn = "";
    String operationOut = "";
    double strikeIn = 0.0;
    String optionTypeIn = "";
    String optionDateIn = "";
    int strategyTableSelRow = -1;
    private ibApi actIbApi = ibApi.getActApi();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    private StrategyResults actStrategyResults;
    double userDividend = 0.0;
    enum StrategyTable{
        oOperation,
        oSymbol,
        oLast,
        oBid,
        oAsk,
        oOffer
    }
    private class StrategyResults{
        int multiplier;
        double maxGain;
        double maxGainPercent;
        double  lossAt10Percent;
        double lossAt20Percent;
        String optionDate;
    }
    private class Strategy {
        String operation;
        boolean isAnOption;
        String optionType;
        String optionDate;
        String symbol;
        double strike;
        double lastQ;
        double bid;
        double ask;
        double offer;
    }
    List<Strategy> strategyList = new ArrayList<Strategy>();
    public StrategyBuilder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        btask = new BuilderTask();
        btask.start();
    }
    public void setOperation(String opin){
        operationIn = opin;
    }
    public void setSymbol(String symin){
        symbolIn = symin;
    }
    public void setStrike(double strikein){
        strikeIn = strikein;
    }
    public void setOptionType(String typein){
        optionTypeIn = typein;
    }
    public void setOptionDate(String datein){
        optionDateIn = datein;
    }
    public String getOperation(){
        String retVal = operationOut;
        operationOut = "";
        return retVal;
    }
    private void clearStrategyTable(){
        int rowCount = strategyBuilderTable.getRowCount();
        int colCount = strategyBuilderTable.getColumnCount();
        for (int il = 0; il < rowCount; il++){
            for (int ic = 0; ic < colCount; ic++) {
                strategyBuilderTable.getModel().setValueAt("", il, ic);
            }
        }
    }
    
    private void updateStrategyTable(List<Strategy> listin){
        int idx = 0;
        clearStrategyTable();
        Strategy actStrategy = new Strategy();
        for (idx = 0; idx < listin.size(); idx++) {
            actStrategy = strategyList.get(idx);
            strategyBuilderTable.getModel().setValueAt(actStrategy.operation, idx, StrategyTable.oOperation.ordinal());
            strategyBuilderTable.getModel().setValueAt(actStrategy.symbol, idx, StrategyTable.oSymbol.ordinal());            
            strategyBuilderTable.getModel().setValueAt(myUtils.roundMe(actStrategy.lastQ, 2), idx, StrategyTable.oLast.ordinal());
            strategyBuilderTable.getModel().setValueAt(myUtils.roundMe(actStrategy.bid, 2), idx, StrategyTable.oBid.ordinal());
            strategyBuilderTable.getModel().setValueAt(myUtils.roundMe(actStrategy.ask, 2), idx, StrategyTable.oAsk.ordinal());
            strategyBuilderTable.getModel().setValueAt(myUtils.roundMe(actStrategy.offer, 2), idx, StrategyTable.oOffer.ordinal());
        }
    }
    private class BuilderTask extends Thread{
        Strategy actStrategy;
        BuilderTask(){
            taskRunning = true;
        }
        @Override
        public void run(){
            while (taskRunning == true){
                if(operationIn != ""){
                    actStrategy = new Strategy(); 
                    actStrategy.isAnOption = (symbolIn.length() > 6) ? true: false;
                    qInfo = actChain.getQuote(symbolIn, actStrategy.isAnOption /*option==true, false == stock*/);                    
                    actStrategy.operation = operationIn;
                    actStrategy.symbol = symbolIn;
                    if(actStrategy.isAnOption == true){
                        actStrategy.strike = strikeIn;
                        actStrategy.optionType = optionTypeIn;
                        actStrategy.optionDate = optionDateIn;
                    }else{
                        actStrategy.strike = 0.0;
                    }                    
                    actStrategy.bid = qInfo.bid;
                    actStrategy.ask = qInfo.ask;
                    actStrategy.lastQ = qInfo.last;
                    actStrategy.offer = myUtils.roundMe((((actStrategy.ask - actStrategy.bid) / 2) + actStrategy.bid), 2);
                    strategyList.add(actStrategy);
                    updateStrategyTable(strategyList);
                    operationIn = "";
                    symbolIn = "";
                }
                myUtils.delay(100);
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
        strategyBuilderTable = new javax.swing.JTable();
        maxGainTextField = new javax.swing.JTextField();
        maxGainPercentTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        calculateButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        deleteAllButton = new javax.swing.JButton();
        deleteSelectedRowButton = new javax.swing.JButton();
        multiplyerComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        maxGainPercentPerAnum = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        dividendTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Strategy Builder"));

        strategyBuilderTable.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        strategyBuilderTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Operation", "Symbol", "Last", "Bid", "Ask", "Offer"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        strategyBuilderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                strategyBuilderTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(strategyBuilderTable);

        maxGainTextField.setText("            ");

        maxGainPercentTextField.setText("       ");

        jLabel1.setText("MaxGain:");

        jLabel2.setText("% Period");

        jLabel3.setText("$");

        calculateButton.setText("Calculate");
        calculateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calculateButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        deleteAllButton.setText("DeleteAll");
        deleteAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllButtonActionPerformed(evt);
            }
        });

        deleteSelectedRowButton.setText("DelSelRow");
        deleteSelectedRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedRowButtonActionPerformed(evt);
            }
        });

        multiplyerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", " " }));

        jLabel4.setText("Multipler:");

        maxGainPercentPerAnum.setText("       ");

        jLabel5.setText("% Anum");

        jLabel6.setText("Enter Dividend:");

        dividendTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dividendTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(exitButton)
                        .addGap(18, 18, 18)
                        .addComponent(deleteAllButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteSelectedRowButton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(calculateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(maxGainTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel4))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                                        .addComponent(multiplyerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(maxGainPercentPerAnum, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel5))
                                    .addComponent(jLabel6)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(dividendTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(maxGainPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel2)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(14, 14, 14))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxGainTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(multiplyerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jLabel2))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(maxGainPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(maxGainPercentPerAnum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel6)
                        .addGap(3, 3, 3)
                        .addComponent(dividendTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exitButton)
                    .addComponent(deleteAllButton)
                    .addComponent(deleteSelectedRowButton)
                    .addComponent(calculateButton))
                .addGap(0, 15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void deleteSelectedRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedRowButtonActionPerformed
        // TODO add your handling code here:
        if((strategyTableSelRow != -1) && (strategyTableSelRow < strategyList.size())){
            strategyList.remove(strategyTableSelRow);
            updateStrategyTable(strategyList);
            strategyTableSelRow = -1;
        }else{
            commonGui.postInformationMsg("Select a row.");
        }
    }//GEN-LAST:event_deleteSelectedRowButtonActionPerformed

    private void deleteAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllButtonActionPerformed
        // TODO add your handling code here:
        
        maxGainTextField.setText("");
        maxGainPercentTextField.setText("");
        maxGainPercentPerAnum.setText("");
        dividendTextField.setText("");
        strategyList.clear();
        userDividend = 0.0;
        updateStrategyTable(strategyList);
        
    }//GEN-LAST:event_deleteAllButtonActionPerformed

    private void strategyBuilderTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_strategyBuilderTableMouseClicked
        // TODO add your handling code here:
        int row = -1;
        int col = -1;
        try {
            row = strategyBuilderTable.getSelectedRow();
            col = strategyBuilderTable.getSelectedColumn();                    
            System.out.println("strategyBuilderTableMouseClicked: " + "row, col is : " + row + ", " + col);
        }catch(Exception e) {
            System.out.println("strategyBuilderTableMouseClicked: Exception!!" + evt);   
        }
        strategyTableSelRow = row;
    }//GEN-LAST:event_strategyBuilderTableMouseClicked
    private boolean isPutOption(String symin){
        return (symin.contains("P")) ? true : false;
    }
    private boolean isCallOption(String symin){
        return (symin.contains("C")) ? true : false;
    }
    private Date convertDate(String din) throws ParseException{
        //DateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        Date date;
        date = format.parse(din);
        return date;
    }
    private void calculateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calculateButtonActionPerformed
        // TODO add your handling code here:
        double accumGain = 0.0;
        double stockCost = 0.0;
        String optionDateStr = "";
        long daysTilExpiry = 0;
        double monthsToExpirey = 0.0;
        double gainPerAnum = 0.0;
        Strategy actStrategy = new Strategy();
        for (int idx = 0; idx < strategyList.size(); idx++){
            actStrategy = strategyList.get(idx);
            if(actStrategy.operation.equals("BUY")){
                //buy is debit
                accumGain -= actStrategy.offer;
                if(!actStrategy.isAnOption){
                    //must be stock
                    stockCost = actStrategy.offer;
                }
            }else if (actStrategy.operation.equals("SELL")){
                //sell is credit
                accumGain += actStrategy.offer;
                if(!actStrategy.isAnOption){
                    //must be short stock
                    stockCost = -actStrategy.offer;
                }
                if((actStrategy.isAnOption == true) && actStrategy.optionType.equals("C")){
                    //call option
                    accumGain += actStrategy.strike;
                    optionDateStr = actStrategy.optionDate;                    
                }
                //humm
                if ((stockCost < 0) && (actStrategy.isAnOption == true) && actStrategy.optionType.equals("P")) {
                    //put option
                    accumGain -= actStrategy.strike;
                    optionDateStr = actStrategy.optionDate;
                }
            }
        }
        //add dividend (total for period, in cents i.e. .58 cents for period.
        accumGain += userDividend;
        actStrategyResults = new StrategyResults();
        actStrategyResults.optionDate = optionDateStr;
        actStrategyResults.multiplier = Integer.valueOf(multiplyerComboBox.getSelectedItem().toString());
        actStrategyResults.maxGain = myUtils.roundMe((accumGain * actStrategyResults.multiplier * 100.0), 2);
        actStrategyResults.maxGainPercent = myUtils.roundMe(((actStrategyResults.maxGain / 
                                                            (Math.abs(stockCost) * actStrategyResults.multiplier * 100.0)) * 100), 2);
        maxGainTextField.setText(Double.toString(actStrategyResults.maxGain));
        maxGainPercentTextField.setText(Double.toString(actStrategyResults.maxGainPercent));
        try {
            daysTilExpiry = myUtils.getDiffInDaysFromToday(convertDate(actStrategyResults.optionDate));
        } catch (ParseException ex) {
            Logger.getLogger(AccountValuesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        monthsToExpirey = myUtils.roundMe((daysTilExpiry / 30), 2); 
        gainPerAnum = myUtils.roundMe(((actStrategyResults.maxGainPercent/monthsToExpirey) * 12.0), 2);
        maxGainPercentPerAnum.setText(Double.toString(gainPerAnum));
    }//GEN-LAST:event_calculateButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void dividendTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dividendTextFieldActionPerformed
        // TODO add your handling code here:
        userDividend = (Double.valueOf(dividendTextField.getText()));
    }//GEN-LAST:event_dividendTextFieldActionPerformed

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
            java.util.logging.Logger.getLogger(StrategyBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StrategyBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StrategyBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StrategyBuilder.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                StrategyBuilder dialog = new StrategyBuilder(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton calculateButton;
    private javax.swing.JButton deleteAllButton;
    private javax.swing.JButton deleteSelectedRowButton;
    private javax.swing.JTextField dividendTextField;
    private javax.swing.JButton exitButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField maxGainPercentPerAnum;
    private javax.swing.JTextField maxGainPercentTextField;
    private javax.swing.JTextField maxGainTextField;
    private javax.swing.JComboBox multiplyerComboBox;
    private javax.swing.JTable strategyBuilderTable;
    // End of variables declaration//GEN-END:variables
}
