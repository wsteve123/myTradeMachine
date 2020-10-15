/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import positions.*;

/**
 *
 * @author earlie87
 */
public class createPortfolioDialogForm extends javax.swing.JDialog {

    /**
     * Creates new form createPortfolioDialogForm
     */
    String selectedAccount = null;
    int selectedTrailingInt = 1;
    String newPortfolioName = null;
    String[] portfolioNameList;
    final String PREFACE = "portfolio";
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    
    public createPortfolioDialogForm(java.awt.Frame parent, boolean modal) {
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
        String tmpStr = null;
        ManagedAccounts actManagedAcccounts = ManagedAccounts.getAllAccounts();
        for (int i = 0; i < actManagedAcccounts.numOf(); i++) {
            ManagedAccounts.anAccount acc = actManagedAcccounts.getAnAccount(i);
            if (acc.getEnabled() == true) {
                if(tmpStr == null) {
                    tmpStr = acc.getName();
                }
                selectAccountComboBox.addItem(acc.getName());
            }
      }
        if (actManagedAcccounts.numOf() > 0){
            selectAccountComboBox.setSelectedItem(tmpStr);
        }
        trailingNumberTextField.setText(String.valueOf(selectedTrailingInt));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectAccountLabel = new javax.swing.JLabel();
        selectAccountComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        trailingNumberTextField = new javax.swing.JTextField();
        createNewPortfolioButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        newPortfolioNameTextField = new javax.swing.JTextField();
        doneBotton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create New Portfolio");

        selectAccountLabel.setText("Select Account:");

        selectAccountComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " " }));
        selectAccountComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAccountComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Trailing Number");

        trailingNumberTextField.setToolTipText("enter a trailing number for new portfolio name");
        trailingNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trailingNumberTextFieldActionPerformed(evt);
            }
        });

        createNewPortfolioButton.setText("CreateNew");
        createNewPortfolioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewPortfolioButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("New Portfolio:");

        newPortfolioNameTextField.setEditable(false);
        newPortfolioNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPortfolioNameTextFieldActionPerformed(evt);
            }
        });

        doneBotton.setText("Done");
        doneBotton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneBottonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(48, 48, 48)
                                .add(selectAccountLabel))
                            .add(layout.createSequentialGroup()
                                .add(36, 36, 36)
                                .add(selectAccountComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jLabel1))
                            .add(layout.createSequentialGroup()
                                .add(82, 82, 82)
                                .add(trailingNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 66, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(45, 45, 45)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(newPortfolioNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 188, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(124, 124, 124)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(6, 6, 6)
                                .add(doneBotton))
                            .add(createNewPortfolioButton))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(36, 36, 36)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAccountLabel)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(selectAccountComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(trailingNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(24, 24, 24)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(newPortfolioNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(56, 56, 56)
                .add(createNewPortfolioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(doneBotton)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void createPortfolioFilename() {   
        
        BufferedWriter bow = null;
        String tmpStr = homeDirectory;
        File f;
        
        tmpStr +=  newPortfolioName;
       
        f = new File(tmpStr);
        if (f.exists() == true) {
            System.out.println("File Exists!!");
            commonGui.postInformationMsg("File Exists. Please enter new parameters or delete this one.");
            return;
        }else {
            System.out.println("File does not Exist!!");
        }
       
       
        //actPositions.setPositionFileName(tmpStr);

        System.out.println("creating file " + tmpStr + " as portfolio filename.");
        try {

            bow = new BufferedWriter(new FileWriter(tmpStr, true /*
                     * append
                     */));
            bow.write(newPortfolioName);
            bow.write("\n");
            bow.write(selectedAccount);
            bow.write("\n");
        } catch (Exception e) {
            System.out.println("error writing text to: " + tmpStr + "(" + e + ").");
        }

        try {
            bow.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
    
    private void selectAccountComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAccountComboBoxActionPerformed
        selectedAccount = String.valueOf(selectAccountComboBox.getSelectedItem());
        System.out.println("selectAccountComboBox:" + selectedAccount);
    }//GEN-LAST:event_selectAccountComboBoxActionPerformed

    private void trailingNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trailingNumberTextFieldActionPerformed
        selectedTrailingInt = Integer.parseInt(trailingNumberTextField.getText());
    }//GEN-LAST:event_trailingNumberTextFieldActionPerformed

    private void createNewPortfolioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPortfolioButtonActionPerformed
        newPortfolioName = PREFACE + "_" + selectedAccount + "_" + String.valueOf(selectedTrailingInt) + ".dat";
        newPortfolioNameTextField.setText(newPortfolioName);
        createPortfolioFilename();
        PlayWIthMenus.actMainMenu.portfolioListChanged = true;
        PlayWIthMenus.actMainMenu.refreshPortfolioComboBox();
        
        
        
    }//GEN-LAST:event_createNewPortfolioButtonActionPerformed

    private void newPortfolioNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPortfolioNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newPortfolioNameTextFieldActionPerformed

    private void doneBottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneBottonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneBottonActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createNewPortfolioButton;
    private javax.swing.JButton doneBotton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField newPortfolioNameTextField;
    private javax.swing.JComboBox selectAccountComboBox;
    private javax.swing.JLabel selectAccountLabel;
    private javax.swing.JTextField trailingNumberTextField;
    // End of variables declaration//GEN-END:variables
}