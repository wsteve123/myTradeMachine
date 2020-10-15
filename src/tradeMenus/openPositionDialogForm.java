/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import positions.myUtils;
import positions.*;

/**
 *
 * @author earlie87
 */
public class openPositionDialogForm extends javax.swing.JDialog {

    private String tickList[];
    private String selectedTicker;
    private positions allPositions;
    /**
     * Creates new form openPositionDialogForm
     */
    public openPositionDialogForm(java.awt.Frame parent, boolean modal, positions allin) {
        super(parent, modal);
        initComponents();

        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });

        allPositions = allin;
        int ix = 0;
        tickList = myUtils.fillTickerList(allPositions);
        for ( ix = 0; ix < tickList.length; ix++) {
            tickerComboBox.addItem(tickList[ix]);
        }
        if (ix > 0) {
            tickerComboBox.setSelectedItem(tickList[0]);
        }
        
    }

    private void openPosition() {
        positionData posToOpen;
        float biasLS;
        positionEditor posToEdit;
        if ((posToOpen = allPositions.posDataSearch(selectedTicker)) != null) {
            
            posToEdit = new positionEditor(posToOpen);
            posToEdit.editAdjData.setAdjDate(posToOpen.getPosDate());
            biasLS = (float) (((float) posToOpen.shortShares * 100.0 * -1.0) + (float) posToOpen.longShares);
            posToEdit.startOutPosition(0, posToOpen.shortShares, biasLS);

            posToEdit = new positionEditor(posToOpen);
            posToEdit.editAdjData.setAdjDate(posToOpen.getPosDate());
            posToEdit.startOutPosition(posToOpen.longShares, 0, biasLS);
            /*
             * create a volatility monitor for this new position
             */
            posToOpen.setVm(new volatilityMonitor(allPositions));
            allPositions.setDefVm(posToOpen.getVm());
           
        } else {
            commonGui.postInformationMsg("position not found.");
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
        jLabel1 = new javax.swing.JLabel();
        tickerComboBox = new javax.swing.JComboBox();
        openButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Open Position");

        jLabel1.setText("Select Position");

        tickerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tickerComboBoxActionPerformed(evt);
            }
        });

        openButton.setText("Open");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(doneButton)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(105, 105, 105)
                            .add(jLabel1))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(95, 95, 95)
                            .add(tickerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(116, 116, 116)
                            .add(openButton))))
                .addContainerGap(137, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(17, 17, 17)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tickerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(31, 31, 31)
                .add(openButton)
                .add(68, 68, 68)
                .add(doneButton)
                .addContainerGap(78, Short.MAX_VALUE))
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tickerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tickerComboBoxActionPerformed
        
        if (tickerComboBox.getItemCount() > 0 ) {
            selectedTicker = String.valueOf(tickerComboBox.getSelectedItem());
        }
        
    }//GEN-LAST:event_tickerComboBoxActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        openPosition();
    }//GEN-LAST:event_openButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton openButton;
    private javax.swing.JComboBox tickerComboBox;
    // End of variables declaration//GEN-END:variables
}