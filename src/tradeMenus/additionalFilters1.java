/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import positions.commonGui;
import tradeMenus.slopeAnalysis.*;

/**
 *
 * @author earlie87
 */
public class additionalFilters1 extends javax.swing.JDialog {

    int userSelected90DayVolume = 0;
    double userSelectedChangeInGainLo = 0;
    double userSelectedChangeInGainHi = 0;
    int userSelectedMaMustTouchMinPercent = 0;
    int userSelectedgMaPiercedMaxPercent = 0;
    int userSelectedInMonths = 0;
    int userSelectedPricePerShare  = 0;
    String userSelectedMaSelectionStr;
    int userSelectedMaSelectionInt = 0;  
    final int SLOPE_SET_MA_WINDOW = 0;
    final int SLOPE_BULLBEAR_CROSS = 1;
     //outer class in slopeAnalysis..
    slopeAnalysis saOuter = new slopeAnalysis();
    BullBearCross bullBearCross = saOuter.new BullBearCross();
    
    MaWindowSz maWindowSizes = null;
    /**
     * Creates new form additionalFilters1
     */
    public additionalFilters1(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    int get90DayVolume(){
        return userSelected90DayVolume;
    }
    int getPricePerShare(){
        return userSelectedPricePerShare;
    }
    double getChangeInGainHi(){
        return userSelectedChangeInGainHi;
    }
    double getChangeInGainLo(){
        return userSelectedChangeInGainLo;
    }
    int getChangeInMonths(){
        return userSelectedInMonths;
    }
    int getMaMustTouchMinPercent(){
        return userSelectedMaMustTouchMinPercent;
    }
    int getMaPiercedMaxPercent(){
        return userSelectedgMaPiercedMaxPercent;
    }
    MaWindowSz getMaWindowSz(){
        return maWindowSizes;
    }
    BullBearCross getBullBearCross(){
        return bullBearCross;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        doneButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        volumeAve90DaysTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pricePerShareTextField = new javax.swing.JTextField();
        movingAverageComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        changeInGainLoTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        changeInGainHiTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        maBounceMustTouchMinPercentTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        maBouncePiercedMaxPercentTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Additional Filtering");

        volumeAve90DaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeAve90DaysTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Min 90DayAveVolume");

        jLabel4.setText("Min PricePer Share");

        pricePerShareTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pricePerShareTextFieldActionPerformed(evt);
            }
        });

        movingAverageComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "maWindowSz", "maBullBear" }));
        movingAverageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                movingAverageComboBoxActionPerformed(evt);
            }
        });

        jLabel5.setText("MovingAve");

        changeInGainLoTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeInGainLoTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("% Change In 1yr Gain Range:");

        changeInGainHiTextField.setText("       ");
        changeInGainHiTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeInGainHiTextFieldActionPerformed(evt);
            }
        });

        jLabel8.setText("Low:");

        jLabel9.setText("Hi:");

        jLabel10.setText("MovingAve Bounce Filter");

        maBounceMustTouchMinPercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maBounceMustTouchMinPercentTextFieldActionPerformed(evt);
            }
        });

        jLabel11.setText("MustTouchMin%:");

        maBouncePiercedMaxPercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maBouncePiercedMaxPercentTextFieldActionPerformed(evt);
            }
        });

        jLabel12.setText("PiercedMax%:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(doneButton)
                                .addComponent(volumeAve90DaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jLabel6)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(pricePerShareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(62, 62, 62)
                                        .addComponent(movingAverageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(68, 68, 68)
                                        .addComponent(cancelButton))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel12)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(maBouncePiercedMaxPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel11)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(maBounceMustTouchMinPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeInGainLoTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addGap(2, 2, 2)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addComponent(jLabel4)
                                .addGap(44, 44, 44)
                                .addComponent(jLabel5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(changeInGainHiTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(115, 115, 115)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(jLabel3)))
                .addContainerGap(284, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volumeAve90DaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pricePerShareTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, Short.MAX_VALUE)
                    .addComponent(movingAverageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changeInGainLoTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeInGainHiTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(maBounceMustTouchMinPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maBouncePiercedMaxPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(doneButton)
                    .addComponent(cancelButton))
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pricePerShareTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pricePerShareTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedPricePerShare = Integer.valueOf(pricePerShareTextField.getText());
    }//GEN-LAST:event_pricePerShareTextFieldActionPerformed

    private void volumeAve90DaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeAve90DaysTextFieldActionPerformed
        // TODO add your handling code here:
        userSelected90DayVolume = Integer.valueOf(volumeAve90DaysTextField.getText());
    }//GEN-LAST:event_volumeAve90DaysTextFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:
        boolean ok;
        ok = (commonGui.postConfirmationMsg(
                                            "You Entered: \n" +
                                            "Min90DayVolume: " + userSelected90DayVolume + "\n" +
                                            "MinPricePerShare: " + userSelectedPricePerShare + "\n" +
                                            "ChangeInGainL: " + userSelectedChangeInGainLo + "\n" +
                                            "ChangeInGainH: " + userSelectedChangeInGainHi + "\n" +                                         
                                            "MaMustTouch: " + userSelectedMaMustTouchMinPercent + "\n" +
                                            "MaPiercedMax " + userSelectedgMaPiercedMaxPercent
             ) == 0);
        if (ok == true){
            setVisible(false);
            dispose();   
        }else{
            commonGui.postInformationMsg("Done aborted.");
        }
        
    }//GEN-LAST:event_doneButtonActionPerformed

    private void movingAverageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_movingAverageComboBoxActionPerformed
        // TODO add your handling code here:
        if (movingAverageComboBox.getItemCount() > 0) {
            userSelectedMaSelectionStr = movingAverageComboBox.getSelectedItem().toString();
            userSelectedMaSelectionInt = movingAverageComboBox.getSelectedIndex();
        };

        System.out.println("selected Slope is: " + movingAverageComboBox);
        System.out.println("selected Slope idx is : " + movingAverageComboBox);
        if (userSelectedMaSelectionInt == SLOPE_BULLBEAR_CROSS){
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
        if (userSelectedMaSelectionInt == SLOPE_SET_MA_WINDOW){ 
            if (maWindowSizes == null){           
                maWindowSizes = saOuter.new MaWindowSz(/*askUserInput*/ true); 
            }else{
                maWindowSizes.getUserInput();
            }
        }
    }//GEN-LAST:event_movingAverageComboBoxActionPerformed

    private void changeInGainLoTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeInGainLoTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedChangeInGainLo = Double.valueOf(changeInGainLoTextField.getText());
    }//GEN-LAST:event_changeInGainLoTextFieldActionPerformed

    private void changeInGainHiTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeInGainHiTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedChangeInGainHi = Double.valueOf(changeInGainHiTextField.getText());
    }//GEN-LAST:event_changeInGainHiTextFieldActionPerformed

    private void maBounceMustTouchMinPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maBounceMustTouchMinPercentTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedMaMustTouchMinPercent = Integer.valueOf(maBounceMustTouchMinPercentTextField.getText());
    }//GEN-LAST:event_maBounceMustTouchMinPercentTextFieldActionPerformed

    private void maBouncePiercedMaxPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maBouncePiercedMaxPercentTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedgMaPiercedMaxPercent = Integer.valueOf(maBouncePiercedMaxPercentTextField.getText());
    }//GEN-LAST:event_maBouncePiercedMaxPercentTextFieldActionPerformed

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
            java.util.logging.Logger.getLogger(additionalFilters1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(additionalFilters1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(additionalFilters1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(additionalFilters1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                additionalFilters1 dialog = new additionalFilters1(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField changeInGainHiTextField;
    private javax.swing.JTextField changeInGainLoTextField;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField maBounceMustTouchMinPercentTextField;
    private javax.swing.JTextField maBouncePiercedMaxPercentTextField;
    private javax.swing.JComboBox movingAverageComboBox;
    private javax.swing.JTextField pricePerShareTextField;
    private javax.swing.JTextField volumeAve90DaysTextField;
    // End of variables declaration//GEN-END:variables
}