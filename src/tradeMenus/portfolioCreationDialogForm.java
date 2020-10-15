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
import positions.ManagedAccounts;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class portfolioCreationDialogForm extends javax.swing.JDialog {

    String userSelectedAlias = null;
    int userSelectedFundsAvail = 0;
    double userSelectedMaxPercentPerPos = 0;
    int userSelectedPercentLong = 0;
    int userSelectedPercentShort = 0;
    String userSelectedAccount = null;
    boolean newOne = false;
    List<String> accountList = new ArrayList<>();
    
    
    //user must enter these values before leaving..
    boolean userEnteredFundsAvail = false;
    boolean userEnteredMaxPercentPerPos = false;
    boolean userEnteredAccount = false;
    boolean userEnteredAlias = false;
    boolean userCancelled = false;
    String selectedExchangeStr = null;
    String selectedSlopeCriteriaStr = null;
    int selectedExchangeInt = 0;
    
    ManagedAccounts actManagedAcccounts = ManagedAccounts.getAllAccounts();
    ManagedAccounts.anAccount actAccount;
    portfolio actPortfolio = new portfolio();
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    /**
     * Creates new form portfolioCreationDialogForm
     */
    public portfolioCreationDialogForm(java.awt.Frame parent, boolean modal, portfolio portfolioIn) {
        super(parent, modal);
        initComponents();
        actPortfolio = portfolioIn;
        
        //get accounts available..from IB
        for (int i = 0; i < actManagedAcccounts.numOf(); i++) {
            actAccount = actManagedAcccounts.getAnAccount(i);
            accountList.add(actAccount.getName());
            accountComboBox.addItem(actAccount.getName());
        }
        if (actPortfolio.isThisNew() == true){
            titleLable.setText("Create Portfolio");
            newOne = true;
        }else{
            titleLable.setText("Edit Portfolio");
            newOne = false;
            updateUserSelectedFields();
        }
        if (indexComboBox.getItemCount() > 0) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString();
            selectedExchangeInt = indexComboBox.getSelectedIndex();
        };
        
    }

    public void createThisPortfolio(portfolio pIn){
        actPortfolio = pIn;
    }
    public boolean getUserCancelled(){
        return userCancelled;
    }
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        accountComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        portfolioDollarAmountTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        maxPercentPerPositionTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        aliasNameTextField = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();
        titleLable = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        positionBiasComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        tradeShortLongCheckBox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        indexComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create Portfolio");

        accountComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", " ", " " }));
        accountComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Select Account:");

        portfolioDollarAmountTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portfolioDollarAmountTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Enter Dollar Amount:");

        maxPercentPerPositionTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxPercentPerPositionTextFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Enter Max Percent Per Position:");

        doneButton.setText("Save");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Enter AliasName:");

        aliasNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aliasNameTextFieldActionPerformed(evt);
            }
        });

        jLabel9.setText("jLabel9");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        positionBiasComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Long", "Short" }));
        positionBiasComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionBiasComboBoxActionPerformed(evt);
            }
        });

        jLabel5.setText("PositionBias");

        tradeShortLongCheckBox.setText("TradeShort&Long");
        tradeShortLongCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tradeShortLongCheckBoxActionPerformed(evt);
            }
        });

        jLabel6.setText("Select Exchange To Trade:");

        indexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "WATCH1", "SnP1", "SnP2", "Dow" }));
        indexComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addGap(174, 174, 174))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(aliasNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel8))
                            .addComponent(jLabel2)
                            .addComponent(titleLable, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(doneButton)
                                .addGap(80, 80, 80)
                                .addComponent(cancelButton))
                            .addComponent(maxPercentPerPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(positionBiasComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(portfolioDollarAmountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tradeShortLongCheckBox)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel6)
                            .addComponent(accountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 42, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(titleLable, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aliasNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(accountComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portfolioDollarAmountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(positionBiasComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tradeShortLongCheckBox))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maxPercentPerPositionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doneButton)
                    .addComponent(cancelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void accountComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountComboBoxActionPerformed
        // TODO add your handling code here:
        if (accountComboBox.getItemCount() > 0) {
            userSelectedAccount = accountComboBox.getSelectedItem().toString();
            userEnteredAccount = true;
        }
    }//GEN-LAST:event_accountComboBoxActionPerformed

    private void portfolioDollarAmountTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portfolioDollarAmountTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedFundsAvail = Integer.valueOf(portfolioDollarAmountTextField.getText());
        userEnteredFundsAvail = true;
    }//GEN-LAST:event_portfolioDollarAmountTextFieldActionPerformed

    private void maxPercentPerPositionTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxPercentPerPositionTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedMaxPercentPerPos = Double.valueOf(maxPercentPerPositionTextField.getText());
        userEnteredMaxPercentPerPos = true;
    }//GEN-LAST:event_maxPercentPerPositionTextFieldActionPerformed
    private void updateActivePortfolio(){
        actPortfolio.setAlias(userSelectedAlias);
        actPortfolio.setAccountNumber(userSelectedAccount);
        if (newOne == true){
            actPortfolio.buildPortfolioName();
        }
        actPortfolio.setAvailFunds(userSelectedFundsAvail);
        actPortfolio.setPercentLong(userSelectedPercentLong);
        actPortfolio.setPercentShort(userSelectedPercentShort);
        actPortfolio.setPercentPerPos(userSelectedMaxPercentPerPos);
        actPortfolio.setState(slopeDefs.oINIT);
        actPortfolio.setUserCriteria("empty criteria");
        actPortfolio.setPositionBias(selectedPositionBiasInt);
        
    }
    private void updateUserSelectedFields(){
        
        userSelectedAlias = actPortfolio.aliasName;
        userSelectedAccount = actPortfolio.accountNumber;
        userSelectedFundsAvail = actPortfolio.availFunds;
        userSelectedPercentLong = actPortfolio.percentLong;
        userSelectedPercentShort = actPortfolio.percentShort;
        
        userSelectedMaxPercentPerPos = actPortfolio.percentPerPosition;
        userEnteredAccount = userEnteredFundsAvail = userEnteredMaxPercentPerPos = userEnteredAlias = true;
        
        //used when editing not creating porfolio so fill up fields..
        portfolioDollarAmountTextField.setText(Integer.toString(actPortfolio.availFunds));
        maxPercentPerPositionTextField.setText(Double.toString(actPortfolio.percentPerPosition));       
        
        accountComboBox.setSelectedItem(actPortfolio.accountNumber);
        aliasNameTextField.setText(actPortfolio.aliasName);
        positionBiasComboBox.setSelectedIndex(actPortfolio.positionBias);        
        
    }
    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:
        if ((userEnteredAlias == true) && (userEnteredAccount == true) && (userEnteredFundsAvail) && (userEnteredMaxPercentPerPos)){
            messageLabel.setText("Portfolio Saved.");
            updateActivePortfolio();
            actPortfolio.wrToFile();
            setVisible(false);
            dispose();
            
        }else{
            messageLabel.setText("Input All Data.");
        }
        
    }//GEN-LAST:event_doneButtonActionPerformed

    private void aliasNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aliasNameTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedAlias = aliasNameTextField.getText();
        userEnteredAlias = true;
    }//GEN-LAST:event_aliasNameTextFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        userCancelled = true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    boolean selectedTradeLongAndShort = false;
    int selectedPositionBiasInt = slopeDefs.oBiasLong;
    String selectedPositionBiasStr = "LONG";
    
    private void positionBiasComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionBiasComboBoxActionPerformed
        // TODO add your handling code here:
        if (positionBiasComboBox.getItemCount() > 0) {
            selectedPositionBiasStr = positionBiasComboBox.getSelectedItem().toString();
            selectedPositionBiasInt = positionBiasComboBox.getSelectedIndex();
            System.out.println("\nselectedLongShortBiasInt = " + selectedPositionBiasInt);
        }
    }//GEN-LAST:event_positionBiasComboBoxActionPerformed

    private void tradeShortLongCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tradeShortLongCheckBoxActionPerformed
        // TODO add your handling code here:
        //check box to allow trading in both long and short on each position.
        if (tradeShortLongCheckBox.isSelected()){
            selectedTradeLongAndShort = true;
        }else{
            selectedTradeLongAndShort = false;
        }
    }//GEN-LAST:event_tradeShortLongCheckBoxActionPerformed

    private void indexComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexComboBoxActionPerformed
        // TODO add your handling code here:
        if (indexComboBox.getItemCount() > 0) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString();
            selectedExchangeInt = indexComboBox.getSelectedIndex();
        };
        System.out.println("selected index is: " + selectedExchangeStr);
        System.out.println("selected index idx is : " + selectedExchangeInt);
    }//GEN-LAST:event_indexComboBoxActionPerformed
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
            java.util.logging.Logger.getLogger(portfolioCreationDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(portfolioCreationDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(portfolioCreationDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(portfolioCreationDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        final portfolio pdummy = new portfolio();
        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                portfolioCreationDialogForm dialog = new portfolioCreationDialogForm(new javax.swing.JFrame(), true, pdummy);
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
    private javax.swing.JComboBox accountComboBox;
    private javax.swing.JTextField aliasNameTextField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JComboBox indexComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField maxPercentPerPositionTextField;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JTextField portfolioDollarAmountTextField;
    private javax.swing.JComboBox positionBiasComboBox;
    private javax.swing.JLabel titleLable;
    private javax.swing.JCheckBox tradeShortLongCheckBox;
    // End of variables declaration//GEN-END:variables
}
