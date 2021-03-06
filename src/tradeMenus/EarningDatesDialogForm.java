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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import positions.ManagedAccounts;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class EarningDatesDialogForm extends javax.swing.JDialog {

    /**
     * Creates new form EarningDatesDialogForm
     */
    public EarningDatesDialogForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        String accNumber = "";
        String accAlias = "";
        JCheckBoxMenuItem menuItem;
        actManagedAcccounts = ManagedAccounts.getAllAccounts();        
        accountsMenu.removeAll();
        for(int idx = 0; idx < actManagedAcccounts.getNumOfAccounts(); idx++){ 
            accNumber = actManagedAcccounts.getAnAccount(idx).getName();
            accAlias = actManagedAcccounts.getAliasFromAccount(accNumber);
            menuItem = new JCheckBoxMenuItem(accNumber + "-" + accAlias);
            accountsMenu.add(menuItem);
        }
        try {
            massageDates(listOfDates);
        } catch (ParseException ex) {
            Logger.getLogger(EarningDatesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        filterDaysTextField.setText(String.valueOf(userSelectedFilterDays));
    }
    String actAccount = "";
    boolean selectAll = false;
    ManagedAccounts actManagedAcccounts;
    ibApi actIbApi = ibApi.getActApi();
    ibApi.accountInfo actAccInfo;
    List<String> listOfPositions = new ArrayList <String>(); 
    List<String> listOfAccounts = new ArrayList <String>();
    SearchCompanyInfo searcher;
    Scanner scanner = null;
    boolean scanRunning = false;
    private class EarningDates{
        String account;
        String date;
        Date formalDate;
        long diffInDays;
    }
    List<EarningDates> listOfDates = new ArrayList <EarningDates>();
    int userSelectedFilterDays = 30;
    boolean userSelectedAllAccounts = false;
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        earningDatesPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTextArea = new javax.swing.JTextArea();
        progressLable = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        filterDaysTextField = new javax.swing.JTextField();
        selectAllAccountsCheckBox = new javax.swing.JCheckBox();
        earningDatesMenuBar = new javax.swing.JMenuBar();
        accountsMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Scan For Earning Dates");

        earningDatesPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        resultTextArea.setColumns(20);
        resultTextArea.setRows(5);
        resultTextArea.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane1.setViewportView(resultTextArea);

        jLabel2.setText("Working on:");

        jLabel1.setText("Show Only X DaysToEarnings:");

        filterDaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterDaysTextFieldActionPerformed(evt);
            }
        });

        selectAllAccountsCheckBox.setText("SelectAllAcounts");
        selectAllAccountsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllAccountsCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout earningDatesPanelLayout = new javax.swing.GroupLayout(earningDatesPanel);
        earningDatesPanel.setLayout(earningDatesPanelLayout);
        earningDatesPanelLayout.setHorizontalGroup(
            earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(earningDatesPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(earningDatesPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(startButton)
                        .addGap(114, 114, 114)
                        .addComponent(exitButton))
                    .addGroup(earningDatesPanelLayout.createSequentialGroup()
                        .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(earningDatesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(progressLable, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(earningDatesPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1))
                            .addGroup(earningDatesPanelLayout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(selectAllAccountsCheckBox)
                                    .addComponent(filterDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(0, 25, Short.MAX_VALUE))
        );
        earningDatesPanelLayout.setVerticalGroup(
            earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(earningDatesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(progressLable, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(earningDatesPanelLayout.createSequentialGroup()
                        .addComponent(filterDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27)
                        .addComponent(selectAllAccountsCheckBox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                .addGroup(earningDatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(exitButton))
                .addGap(20, 20, 20))
        );

        accountsMenu.setText("Accounts");
        accountsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountsMenuActionPerformed(evt);
            }
        });
        earningDatesMenuBar.add(accountsMenu);

        setJMenuBar(earningDatesMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(earningDatesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(earningDatesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);       
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        // TODO add your handling code here:
        String position = "";
        String result = "";
        //build list of accounts based on which are selected..
        listOfAccounts.clear();
        for(int idx = 0; idx < accountsMenu.getItemCount(); idx++){
            if(accountsMenu.getItem(idx).isSelected() == true){
                listOfAccounts.add(convertItemToAccountNumber(accountsMenu.getItem(idx).toString()));
            }
        }
        System.out.println("\nadded " + listOfAccounts.size() + " Accounts!");
        //start scanner
        scanner = new Scanner();
    }//GEN-LAST:event_startButtonActionPerformed

    private void accountsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountsMenuActionPerformed
        // TODO add your handling code here:       
    }//GEN-LAST:event_accountsMenuActionPerformed

    private void filterDaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterDaysTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedFilterDays = Integer.valueOf(filterDaysTextField.getText());
        System.out.println("\nuserSelectedFilterDays: " + userSelectedFilterDays);
    }//GEN-LAST:event_filterDaysTextFieldActionPerformed

    private void selectAllAccountsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllAccountsCheckBoxActionPerformed
        // TODO add your handling code here:
        if (selectAllAccountsCheckBox.isSelected() == true) {
            userSelectedAllAccounts = true;

        } else {
            userSelectedAllAccounts = false;
        }
        for (int idx = 0; idx < accountsMenu.getItemCount(); idx++) {
            accountsMenu.getItem(idx).setSelected(userSelectedAllAccounts);
        }
    }//GEN-LAST:event_selectAllAccountsCheckBoxActionPerformed

    private String convertItemToAccountNumber(String item){
        //items have account number and alias. this return just account number...
        String res = "";
        String accName = "";
        boolean found = false;
        for (int idx = 0; (idx < actManagedAcccounts.getNumOfAccounts()) && !found; idx++){
            if(item.contains(accName = actManagedAcccounts.getAnAccount(idx).getName())){
                res = accName;
                found = true;
            }
        }
        return res;
    }
    private class Scanner extends Thread {
        String position = "";
        String result = "";
        Scanner() {            
            scanRunning = true;
            this.start();
        }
        @Override
        public void run() {
            EarningDates date = null;
            long diffInDays = 0;
            System.out.println("\nstarting scanner..");
            resultTextArea.setText(""); 
            for (int ida = 0; ida < listOfAccounts.size(); ida++) {
                actAccount = listOfAccounts.get(ida);
                actAccInfo = actIbApi.new accountInfo(actAccount);
                actIbApi.setActAccountInfo(actAccInfo);
                actAccInfo.reqUpdateAccountInformation(true);
                myUtils.delay(2000);
                actAccInfo.reqUpdateAccountInformation(false);
                System.out.println("\nportfolioSize = " + actAccInfo.getPortfolioListSize());
                listOfPositions.clear();
                //build positions list
                for (int idp = 0; idp < actAccInfo.getPortfolioListSize(); idp++) {
                    position = actAccInfo.getPortfolioContractSymbol(idp);
                    //only add stock symbols for now.
                    if (actAccInfo.getPortfolioContract(idp).m_secType.equals("STK")) {
                        listOfPositions.add(position);
                        System.out.println("\naddingSymbol: " + position);
                    }
                }
                System.out.println("\nadded " + listOfPositions.size() + " Positions!");
                if (listOfPositions.size() > 0) {
                    resultTextArea.append("\nAccount: " + actAccount);
                    searcher = new SearchCompanyInfo();
                    resultTextArea.setTabSize(6);
                    resultTextArea.append("\n  Position\t" + "EarningsDate\t" + "DaysLeft");
                    resultTextArea.update(resultTextArea.getGraphics());
                    for (int idx = 0; idx < listOfPositions.size(); idx++) {
                        progressLable.setText(actAccount + " -> " + position);
                        position = listOfPositions.get(idx);
                        result = searcher.getInfo(position, SearchCompanyInfo.CompanyInfoType.oEarningsDate);
                        date = new EarningDates();
                        date.account = actAccount;
                        date.date = result;
                        if (!result.equals("-")) {
                            try {
                                date.formalDate = convertDate(result);
                            } catch (ParseException ex) {
                                Logger.getLogger(EarningDatesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            date.diffInDays = myUtils.getDiffInDaysFromToday(date.formalDate);
                        }else{
                            date.formalDate = null;
                            date.diffInDays = 0;
                        }
                        listOfDates.add(date);
                        if((date.diffInDays <= userSelectedFilterDays) && (date.diffInDays >= 0)){
                            resultTextArea.append("\n  " + position + "\t" + result + "\t(" + date.diffInDays + ")");
                        }
                    }                    
                    progressLable.setText("Done");                    
                }
            }
            System.out.println("\nended scanner..");
            resultTextArea.append("\nFinished Scan");
            //massageDates(listOfDates);
            scanRunning = false;
        }
    }
    private Date convertDate(String din) throws ParseException{
        DateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        String res = "";
        String tmpStr = "";
        Date date;
        Calendar calendarToday = Calendar.getInstance();
        //omit BMO/AMO from end
        if(din.length() > 7){
            tmpStr = din.substring(0, 7);
        }else{
            tmpStr += (din + " ");
        }        
        tmpStr += calendarToday.get(Calendar.YEAR);
        date = format.parse(tmpStr);
        return date;
    }
    /*
    private long getDiffInDaysFromToday(Date datein){
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
    */
    private void massageDates(List<EarningDates> datesin) throws ParseException{
         /*
        date from finviz is format Feb 9 BMO for 2/9 before market open.
        need to convert this to 2/9/20xx. This will allow us to calculate number of
        days between two dates.
        */
        String string2 = "Sep 20 BMO";
        String string3 = "";
        long difference;
        long differenceDates; 
        Date date;
        Date dateToday = new Date();
        Calendar calendarToday = Calendar.getInstance();        
        DateFormat format = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        //omit BMO/AMO from end      
        string3 = string2.substring(0, 7);       
        string3 += calendarToday.get(Calendar.YEAR);
        date = format.parse(string3);
        //calendarEarningDate.setTime(date);
        System.out.println("\nD1 = " + date + "D2 = " + dateToday);
        difference = Math.abs(date.getTime() - dateToday.getTime());
        differenceDates = (difference / (24 * 60 * 60 * 1000));
        System.out.println("\ndiffInDays = " + differenceDates);
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
            java.util.logging.Logger.getLogger(EarningDatesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EarningDatesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EarningDatesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EarningDatesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                EarningDatesDialogForm dialog = new EarningDatesDialogForm(new javax.swing.JFrame(), true);
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
    private javax.swing.JMenu accountsMenu;
    private javax.swing.JMenuBar earningDatesMenuBar;
    private javax.swing.JPanel earningDatesPanel;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField filterDaysTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel progressLable;
    private javax.swing.JTextArea resultTextArea;
    private javax.swing.JCheckBox selectAllAccountsCheckBox;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
