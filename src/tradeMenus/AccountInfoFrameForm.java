/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class AccountInfoFrameForm extends javax.swing.JFrame {

    /**
     * Creates new form accountInfoFrameForm
     */
    public enum TblColumnsAccInfo{
        oLiqValue,
        oTotalCash,
        oAvailFunds,
        oUnrlzdPNL,
        oRlzdPNL,                
    };
    public enum TblColumnsPortfolioInfo{
        oNumber,
        oSymbol,
        oPositions,
        oMrktPrice,
        oMrktValue,
        oAveCost, 
        oUnrlzdPNL,
        oRlzdPNL,
        oPlToday,
        oTktPositions
    };
    public enum TblColumnsTotalPlInfo{        
        oTotUnrealGain,
        oTotUnrealLoss,
        oTotRealGain,
        oTotRealLoss,
        oTotPLToday
    };
    public enum TblColumnsTotalLongShort{        
        oTotNumShorts,
        oTotNumLongs,
        oTotShortVal,
        oTotLongVal,
        oTotMoneyAtWork,
        oTotPercentShorts
    };
    ibApi.accountInfo actAccount;        
    boolean letsRun = false;
    boolean userWantsOut = false;
    AccountInfoThread actThread;
    String userMsg = "";
    ibApi actIbApi = ibApi.getActApi();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();  
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    public AccountInfoFrameForm() {
        
        initComponents();
        actThread = new AccountInfoThread();
        
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.out.println("closing AccountInfoFrameForm window.");
                        setVisible(false);
                        dispose();
                    }
                });

    }
    public void setProgressText(String txt){
        progressLabel.setText(txt);
    }
    public void setActAccountInfo(ibApi.accountInfo actAcc){
        actAccount = actAcc;
    }
    slopeTraderFrameForm.TradeTickets actTickets = null;
    public void setActTradeTickets(slopeTraderFrameForm.TradeTickets tickets){
        actTickets = tickets;
    }
    public void setRun(boolean run){
        letsRun = run;
        if(letsRun == true){            
            actThread.start();
        }else{
            
        }
    }
    private int findInTicketsSharesAtHand(String byticker){
        int x = 0;
        boolean found = false;
        int retVal = -1;
        for(x = 0; ((x < actTickets.numOfTickets) && !found); x++){
            if(actTickets.tickets[x].ticker.equals(byticker)){
                found = true;
            }else{
                
            }
        }
        if(found == true){
            retVal = actTickets.tickets[x-1].sharesAtHand;
        }
        return retVal;
    }
    public void updateTables(){
        int sz = actAccount.getPortfolioListSize();
        int x = 0;        
        int sharesAtHand = 0;
        int misMatches = 0;
        double totUnrealizedGain = 0.0;
        double totUnrealizedLoss = 0.0;
        double totRealizedGain = 0.0;
        double totRealizedLoss = 0.0;
        int totNumLongs = 0;
        int totNumShorts = 0;
        double totLongVal = 0.0;
        double totShortVal = 0.0;
        double totPercentShorts = 0.0;
        double totMoneyAtWork = 0.0;
        double plToday = 0.0;
        double plTodayTotal = 0.0;       
        
        accountNumberLabel.setText(actAccount.getAccountNumber());
        accountInfoTable.getModel().setValueAt(Double.toString(actAccount.getNetLiqVal()), 0, TblColumnsAccInfo.oLiqValue.ordinal());
        accountInfoTable.getModel().setValueAt(Double.toString(actAccount.getTotalCashVal()), 0, TblColumnsAccInfo.oTotalCash.ordinal());
        accountInfoTable.getModel().setValueAt(Double.toString(actAccount.getCurrentAvailFundsVal()), 0, TblColumnsAccInfo.oAvailFunds.ordinal());
        accountInfoTable.getModel().setValueAt(Double.toString(actAccount.getCurrentUnrealizedPandLVal()), 0, TblColumnsAccInfo.oUnrlzdPNL.ordinal());
        accountInfoTable.getModel().setValueAt(Double.toString(actAccount.getCurrentRealizedPandLVal()), 0, TblColumnsAccInfo.oRlzdPNL.ordinal());
        for (x = 0; x < sz; x++) {
            qInfo = actChain.getQuote(actAccount.getPortfolioContractSymbol(x), false);
            if(actAccount.getPortfolioPosition(x) > 0){
                //positive numbers mean long position..
                plToday = myUtils.roundMe((qInfo.last * actAccount.getPortfolioPosition(x)) - (qInfo.prevClose * actAccount.getPortfolioPosition(x)), 2);
            }else if(actAccount.getPortfolioPosition(x) < 0){
                //negative numbers mean short position..so reverse terms and use absolute
                plToday = myUtils.roundMe((qInfo.prevClose * Math.abs(actAccount.getPortfolioPosition(x))) - (qInfo.last * Math.abs(actAccount.getPortfolioPosition(x))), 2);
            }else{
                plToday = 0;
            }           
            portfolioInfoTable.getModel().setValueAt(Integer.toString(x + 1), x, TblColumnsPortfolioInfo.oNumber.ordinal());
            portfolioInfoTable.getModel().setValueAt(actAccount.getPortfolioContractSymbol(x), x, TblColumnsPortfolioInfo.oSymbol.ordinal());
            portfolioInfoTable.getModel().setValueAt(Integer.toString(actAccount.getPortfolioPosition(x)), x, TblColumnsPortfolioInfo.oPositions.ordinal());
            portfolioInfoTable.getModel().setValueAt(Double.toString(actAccount.getPortfolioMarketPrice(x)), x, TblColumnsPortfolioInfo.oMrktPrice.ordinal());
            portfolioInfoTable.getModel().setValueAt(Double.toString(actAccount.getPortfolioMarketValue(x)), x, TblColumnsPortfolioInfo.oMrktValue.ordinal());
            portfolioInfoTable.getModel().setValueAt(Double.toString(actAccount.getPortfolioAveCost(x)), x, TblColumnsPortfolioInfo.oAveCost.ordinal());
            portfolioInfoTable.getModel().setValueAt(Double.toString(actAccount.getPortfolioUnrealizedPNL(x)), x, TblColumnsPortfolioInfo.oUnrlzdPNL.ordinal());
            portfolioInfoTable.getModel().setValueAt(Double.toString(actAccount.getPortfolioRealizedPNL(x)), x, TblColumnsPortfolioInfo.oRlzdPNL.ordinal());            
            portfolioInfoTable.getModel().setValueAt(Double.toString(plToday), x, TblColumnsPortfolioInfo.oPlToday.ordinal());
            if (actTickets != null) {
                sharesAtHand = findInTicketsSharesAtHand(actAccount.getPortfolioContractSymbol(x));
                if (sharesAtHand != actAccount.getPortfolioPosition(x)) {
                    portfolioInfoTable.getModel().setValueAt(Integer.toString(sharesAtHand) + "*", x, TblColumnsPortfolioInfo.oTktPositions.ordinal());
                    misMatches++;
                    userMsg = "Pos MissMatch! " + misMatches + " (see *)";
                } else {
                    portfolioInfoTable.getModel().setValueAt(Integer.toString(sharesAtHand), x, TblColumnsPortfolioInfo.oTktPositions.ordinal());
                }
            }
            //add up totals
            if (actAccount.getPortfolioUnrealizedPNL(x) > 0) {
                totUnrealizedGain += actAccount.getPortfolioUnrealizedPNL(x);
            } else if (actAccount.getPortfolioUnrealizedPNL(x) < 0) {
                totUnrealizedLoss += actAccount.getPortfolioUnrealizedPNL(x);
            }
            if (actAccount.getPortfolioRealizedPNL(x) > 0) {
                totRealizedGain += actAccount.getPortfolioRealizedPNL(x);
            } else if (actAccount.getPortfolioRealizedPNL(x) < 0) {
                totRealizedLoss += actAccount.getPortfolioRealizedPNL(x);
            }

            if (actAccount.getPortfolioPosition(x) > 0) {
                totNumLongs++;
                totLongVal += actAccount.getPortfolioMarketValue(x);
            } else if (actAccount.getPortfolioPosition(x) < 0) {
                totNumShorts++;
                totShortVal += actAccount.getPortfolioMarketValue(x);
            }
            plTodayTotal += plToday;
        }/*for*/
        plTodayTotal = myUtils.roundMe(plTodayTotal, 2);
        if ((totNumLongs > 0) || (totNumShorts > 0)) {
            totPercentShorts = (double)((double)totNumShorts / ((double)totNumLongs + (double)totNumShorts));
        }
        totMoneyAtWork = myUtils.roundMe((totLongVal + Math.abs(totShortVal)), 2);
        //display totals..rounded..
        totLongVal = myUtils.roundMe(totLongVal, 2);
        totShortVal = myUtils.roundMe(totShortVal, 2);
        totPercentShorts = myUtils.roundMe(totPercentShorts, 2);
        
        totUnrealizedGain = myUtils.roundMe(totUnrealizedGain, 2);
        totUnrealizedLoss = myUtils.roundMe(totUnrealizedLoss, 2);
        totRealizedGain = myUtils.roundMe(totRealizedGain, 2);
        totRealizedLoss = myUtils.roundMe(totRealizedLoss, 2); 
        
        totalPLTable.getModel().setValueAt(Double.toString(totUnrealizedGain), 0, TblColumnsTotalPlInfo.oTotUnrealGain.ordinal());
        totalPLTable.getModel().setValueAt(Double.toString(totUnrealizedLoss), 0, TblColumnsTotalPlInfo.oTotUnrealLoss.ordinal());
        totalPLTable.getModel().setValueAt(Double.toString(totRealizedGain), 0, TblColumnsTotalPlInfo.oTotRealGain.ordinal());
        totalPLTable.getModel().setValueAt(Double.toString(totRealizedLoss), 0, TblColumnsTotalPlInfo.oTotRealLoss.ordinal());
        totalPLTable.getModel().setValueAt(Double.toString(plTodayTotal), 0, TblColumnsTotalPlInfo.oTotPLToday.ordinal());
        totalShortLongTable.getModel().setValueAt(Integer.toString(totNumShorts), 0, TblColumnsTotalLongShort.oTotNumShorts.ordinal());
        totalShortLongTable.getModel().setValueAt(Integer.toString(totNumLongs), 0, TblColumnsTotalLongShort.oTotNumLongs.ordinal());
        totalShortLongTable.getModel().setValueAt(Double.toString(totShortVal), 0, TblColumnsTotalLongShort.oTotShortVal.ordinal());
        totalShortLongTable.getModel().setValueAt(Double.toString(totLongVal), 0, TblColumnsTotalLongShort.oTotLongVal.ordinal());
        totalShortLongTable.getModel().setValueAt(Double.toString(totMoneyAtWork), 0, TblColumnsTotalLongShort.oTotMoneyAtWork.ordinal());
        totalShortLongTable.getModel().setValueAt(Double.toString(totPercentShorts), 0, TblColumnsTotalLongShort.oTotPercentShorts.ordinal());
        if(misMatches == 0 ){
            userMsg = "All Pos Matched.";
        }
    }
    
    class AccountInfoThread extends Thread{   
        AccountInfoThread(){            
        }
        @Override
        public void run() {
            int sz = 0;
            progressLabel.setText("Getting data..");
            actAccount.reqUpdateAccountInformation(true);           
            while((letsRun == true) && (userWantsOut == false)){
                try{                    
                    AccountInfoThread.sleep(1000);
                    progressLabel.setText("Last Update("+ actAccount.getUpdateCnt() + "): " + actAccount.getAccountTime() + " " + userMsg);
                    sz = actAccount.getPortfolioListSize();
                    updateTables();                    
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }                                 
            }
            actAccount.reqUpdateAccountInformation(false);
            
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

        jLabel1 = new javax.swing.JLabel();
        accountNumberLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountInfoTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        portfolioInfoTable = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        totalPLTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        totalShortLongTable = new javax.swing.JTable();

        setTitle("Account Information");

        jLabel1.setText("Account Number:");

        accountInfoTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "LiqVal", "TotalCash", "AvailFunds", "UnlzdPNL", "RlzdPNL"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(accountInfoTable);

        portfolioInfoTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Number", "Symbol", "Positions", "MrktPrice", "MrktValue", "AveCost", "UnrlzdPNL", "RlzdPNL", "PLToday", "TcktPositions"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(portfolioInfoTable);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Progress:");

        totalPLTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null}
            },
            new String [] {
                "TotUnRlzdProfit", "TotUnRlztLoss", "TotRlzdProfit", "TotRlztLoss", "TotPLToday"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(totalPLTable);

        totalShortLongTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null}
            },
            new String [] {
                "TotShorts", "TotLongs", "TotShortVal", "TotLongVal", "Tot$AtWork", "Tot%Shorts"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(totalShortLongTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(177, 177, 177)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(accountNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jScrollPane4)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 505, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1023, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(accountNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 344, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(progressLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(closeButton)
                        .addGap(41, 41, 41))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        System.out.println("closing AccountInfoFrameForm..");        
        userWantsOut = true;
        myUtils.delay(100);
        actThread = null;
        setVisible(false);       
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

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
            java.util.logging.Logger.getLogger(AccountInfoFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AccountInfoFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AccountInfoFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AccountInfoFrameForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //final AccountInfoFrameForm dummy = new AccountInfoFrameForm();
        /* Create and display the form */
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AccountInfoFrameForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable accountInfoTable;
    private javax.swing.JLabel accountNumberLabel;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable portfolioInfoTable;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JTable totalPLTable;
    private javax.swing.JTable totalShortLongTable;
    // End of variables declaration//GEN-END:variables
}
