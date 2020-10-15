/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.*;
import positions.commonGui;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class deletePortfolioDialogForm extends javax.swing.JDialog {

    String[] portfolioNameList;
    final String PREFACE = "portfolio";
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    private String selectedPortfolio = null;
    /**
     * Creates new form deletePortfolioDialogForm
     */
    private void updatePortfolioList() {
        String tmpStr = null;
        portfolioNameList = null;
        deletePortfolioComboBox.removeAllItems();
        portfolioNameList = getListOfPortfolioFilenames(homeDirectory);
        for (int i = 0; i < portfolioNameList.length; i++) {
            if (tmpStr == null) {
                tmpStr = portfolioNameList[i];
            }
            deletePortfolioComboBox.addItem(portfolioNameList[i]);
        }
        deletePortfolioComboBox.setSelectedItem(tmpStr);
    }
    public deletePortfolioDialogForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        updatePortfolioList();
    }

    
    private String[] getListOfPortfolioFilenames(String fromThisDirectory) {
 
        // our filename filter (filename pattern matcher)
        class onlyPortfolioNames implements FilenameFilter {

            public boolean accept(File dir, String s) {
                if (s.startsWith(PREFACE)) {
                    return true;
                }
                return false;
            }
        }
        return new java.io.File(fromThisDirectory).list( new onlyPortfolioNames() );
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
        deletePortfolioComboBox = new javax.swing.JComboBox();
        selectPortfolioLabel = new javax.swing.JLabel();
        deletePortfolioButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        deletePortfolioMessageLabel = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Delete Portfolio");

        deletePortfolioComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePortfolioComboBoxActionPerformed(evt);
            }
        });

        selectPortfolioLabel.setText("Select Portfolio");

        deletePortfolioButton.setText("Delete");
        deletePortfolioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePortfolioButtonActionPerformed(evt);
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
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(141, 141, 141)
                        .add(selectPortfolioLabel))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(145, 145, 145)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deletePortfolioButton)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(doneButton))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(90, 90, 90)
                        .add(deletePortfolioComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 201, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(76, 76, 76)
                        .add(deletePortfolioMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 231, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(58, 58, 58)
                .add(selectPortfolioLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deletePortfolioComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deletePortfolioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 98, Short.MAX_VALUE)
                .add(deletePortfolioMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(doneButton))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void deletePortfolioFilename() {   
        
        BufferedWriter bow = null;
        String tmpStr = homeDirectory;
        File f;
        
        tmpStr +=  selectedPortfolio;
       
        f = new File(tmpStr);
        if (!f.exists() == true) {
            System.out.println("File Does not Exists!!");
            commonGui.postInformationMsg("File Does not Exist. Please enter new one.");
            return;
        }else {
            System.out.println("File Exists!.. Deleting....");
        }
       
       
        //actPositions.setPositionFileName(tmpStr);

        System.out.println("deleting file " + tmpStr + " as portfolio filename.");
         // Attempt to delete it
        boolean success = f.delete();

        if (!success) {
            throw new IllegalArgumentException("Delete: deletion failed");
        }
        deletePortfolioMessageLabel.setText(selectedPortfolio + " file deleted.");
        //commonGui.postInformationMsg(tmpStr + " file deleted.");
    }
    
    private void deletePortfolioComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePortfolioComboBoxActionPerformed
        selectedPortfolio = String.valueOf(deletePortfolioComboBox.getSelectedItem());
//        deletePortfolioMessageLabel.setText("");
        //updatePortfolioList();
        
        
    }//GEN-LAST:event_deletePortfolioComboBoxActionPerformed

    private void deletePortfolioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePortfolioButtonActionPerformed
        if (selectedPortfolio.equals(PlayWIthMenus.actMainMenu.activePortfolio)) {
            commonGui.postInformationMsg("Cannot Delete the Active Portfolio!");
        }else {
            deletePortfolioFilename();
            updatePortfolioList();
            PlayWIthMenus.actMainMenu.portfolioListChanged = true;
            PlayWIthMenus.actMainMenu.refreshPortfolioComboBox();
        }
    }//GEN-LAST:event_deletePortfolioButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deletePortfolioButton;
    private javax.swing.JComboBox deletePortfolioComboBox;
    private javax.swing.JLabel deletePortfolioMessageLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel selectPortfolioLabel;
    // End of variables declaration//GEN-END:variables
}