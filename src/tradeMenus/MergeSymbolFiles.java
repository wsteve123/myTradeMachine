/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import positions.commonGui;

/**
 *
 * @author earlie87
 */
public class MergeSymbolFiles extends javax.swing.JDialog {
    
    File currentFolder = new File(System.getProperty("user.dir") + "/src/supportFiles/");
    File[] listOfFiles = currentFolder.listFiles();
    String userSelectedOutputFileName = "";
    String userSelectedBaseFile = "";
    String userSelectedAddFile = "";

    IOTextFiles.ioWrTextFiles userWrTextFile;
    IOTextFiles.ioRdTextFiles userRdBaseTextFile;
    IOTextFiles.ioRdTextFiles userRdAddTextFile;
    List<String> combinedUniqeSymbols = new ArrayList<String>();
    /**
     * Creates new form mergeSymbolFiles
     */
    public MergeSymbolFiles(java.awt.Frame parent, boolean modal) {
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

        inputFileComboBox.removeAllItems();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith("sym")) {
                System.out.println("File " + listOfFiles[i].getName());
                inputFileComboBox.addItem(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
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
        jLabel1 = new javax.swing.JLabel();
        inputFileComboBox = new javax.swing.JComboBox();
        selectedBaseFileTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        selectedAddFileTextField = new javax.swing.JTextField();
        mergeFilesButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resultTextArea = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        outputFileTextField = new javax.swing.JTextField();
        writeToFileButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("MergeCompare Symbol Files"));

        jLabel1.setText("SelectFile:");

        inputFileComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        inputFileComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputFileComboBoxActionPerformed(evt);
            }
        });

        selectedBaseFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedBaseFileTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("SelectedBaseFile:");

        jLabel3.setText("SelectedAddFile:");

        selectedAddFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectedAddFileTextFieldActionPerformed(evt);
            }
        });

        mergeFilesButton.setText("MergeFiles");
        mergeFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeFilesButtonActionPerformed(evt);
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
        jScrollPane1.setViewportView(resultTextArea);

        jLabel4.setText("Enter OutputFile:");

        outputFileTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFileTextFieldActionPerformed(evt);
            }
        });

        writeToFileButton.setText("WriteToFile");
        writeToFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeToFileButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(inputFileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(66, 66, 66)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(selectedBaseFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(selectedAddFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(58, 58, 58)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(outputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mergeFilesButton)
                        .addGap(35, 35, 35)
                        .addComponent(writeToFileButton)
                        .addGap(37, 37, 37)
                        .addComponent(exitButton)))
                .addGap(42, 42, 42))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputFileComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectedBaseFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectedAddFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                        .addGap(50, 50, 50))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(mergeFilesButton)
                            .addComponent(writeToFileButton)
                            .addComponent(exitButton))
                        .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectedAddFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedAddFileTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedAddFile = selectedAddFileTextField.getText();
    }//GEN-LAST:event_selectedAddFileTextFieldActionPerformed

    private void inputFileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputFileComboBoxActionPerformed
        // TODO add your handling code here:
        if(inputFileComboBox.getItemCount() > 0){
            if(userSelectedBaseFile.equals("")){
                userSelectedBaseFile = inputFileComboBox.getSelectedItem().toString();
                if((userSelectedBaseFile.length() > 0) && userSelectedBaseFile.equals(userSelectedAddFile)){
                    commonGui.postInformationMsg("Files cannot be the same.");
                    userSelectedBaseFile = "";
                }else{
                    selectedBaseFileTextField.setText(userSelectedBaseFile);
                }
                
            }else if (userSelectedAddFile.equals("")){
                userSelectedAddFile = inputFileComboBox.getSelectedItem().toString();
                if((userSelectedAddFile.length() > 0) && userSelectedAddFile.equals(userSelectedBaseFile)){
                    commonGui.postInformationMsg("Files cannot be the same.");
                    userSelectedAddFile = "";
                }else{
                    selectedAddFileTextField.setText(userSelectedAddFile);
                }
            }else{              
                commonGui.postInformationMsg("Delete inputFile1 or inputFile2.");
            }            
        }
    }//GEN-LAST:event_inputFileComboBoxActionPerformed

    private void selectedBaseFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectedBaseFileTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedBaseFile = selectedBaseFileTextField.getText();
    }//GEN-LAST:event_selectedBaseFileTextFieldActionPerformed
    private void compareFiles(){
        //compare file1 and file2 and show differences in text area..
        String strA = "";
        String strB = "";
        int xIdx = 0;
        int yIdx = 0;
        int duplicateCnt = 0;
        int originalSz = 0;
        
        resultTextArea.append("\nComparing files: " + userSelectedBaseFile + " and " + userSelectedAddFile);
        combinedUniqeSymbols.removeAll(combinedUniqeSymbols);
        //read both files first.
        for (xIdx = 0; ((strA = userRdBaseTextFile.read(false)) != null); xIdx++) {
            combinedUniqeSymbols.add(strA);
        }
        for (xIdx = 0; ((strA = userRdAddTextFile.read(false)) != null); xIdx++) {
            combinedUniqeSymbols.add(strA);
        }
        originalSz = combinedUniqeSymbols.size();
        //now look for duplicates and remove them..
        for (xIdx = 0; xIdx < combinedUniqeSymbols.size(); xIdx++) {
            strA = combinedUniqeSymbols.get(xIdx);
            for (yIdx = (xIdx + 1); yIdx < combinedUniqeSymbols.size(); yIdx++) { 
                strB = combinedUniqeSymbols.get(yIdx);
                if(strB.equals(strA)){
                    //found duplicate, remove..
                    combinedUniqeSymbols.remove(yIdx);
                    resultTextArea.append("\nremoving duplicate " + strB);
                    duplicateCnt++;
                }
            }
            resultTextArea.append("\nadded " + strA);
        }
        System.out.println("\ncombined List size = " + combinedUniqeSymbols.size());
        resultTextArea.append("\nFound " + duplicateCnt + " duplicates.");
        resultTextArea.append("\nOriginal combined size was " + originalSz);
        resultTextArea.append("\nMerged size is " + combinedUniqeSymbols.size());
        resultTextArea.append("\nDone.");
    }
    private void mergeFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeFilesButtonActionPerformed
        // TODO add your handling code here:
        boolean compare = true;
        if(userSelectedBaseFile.length() > 0){
            IOTextFiles ioTextFile = new IOTextFiles();
            userRdBaseTextFile = ioTextFile.new ioRdTextFiles(userSelectedBaseFile, false); 
        }else{
            compare = false;
        }
        if(userSelectedAddFile.length() > 0){
            IOTextFiles ioTextFile = new IOTextFiles();
            userRdAddTextFile = ioTextFile.new ioRdTextFiles(userSelectedAddFile, false); 
        }else{
            compare = false;
        }
        if (compare == true){
            compareFiles();
        }else{
            commonGui.postInformationMsg("Files to compare problem.");
        }
    }//GEN-LAST:event_mergeFilesButtonActionPerformed

    private void writeToFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeToFileButtonActionPerformed
        // TODO add your handling code here:
        int xIdx = 0;
        if(userSelectedOutputFileName == ""){
            commonGui.postInformationMsg("Enter output file name.");
        }else{
            userSelectedOutputFileName = outputFileTextField.getText();
            IOTextFiles ioTextFile = new IOTextFiles();
            userWrTextFile = ioTextFile.new ioWrTextFiles(userSelectedOutputFileName, false);
            //write to file..
            resultTextArea.append("\nwriting to file " + userSelectedOutputFileName);
            for (xIdx = 0; xIdx < combinedUniqeSymbols.size(); xIdx++){
                userWrTextFile.write(combinedUniqeSymbols.get(xIdx));
            }
            resultTextArea.append("\nDone. Wrote " + xIdx + " symbols");
        }
    }//GEN-LAST:event_writeToFileButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void outputFileTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFileTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedOutputFileName = outputFileTextField.getText();
    }//GEN-LAST:event_outputFileTextFieldActionPerformed

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
            java.util.logging.Logger.getLogger(MergeSymbolFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MergeSymbolFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MergeSymbolFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MergeSymbolFiles.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MergeSymbolFiles dialog = new MergeSymbolFiles(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton exitButton;
    private javax.swing.JComboBox inputFileComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton mergeFilesButton;
    private javax.swing.JTextField outputFileTextField;
    private javax.swing.JTextArea resultTextArea;
    private javax.swing.JTextField selectedAddFileTextField;
    private javax.swing.JTextField selectedBaseFileTextField;
    private javax.swing.JButton writeToFileButton;
    // End of variables declaration//GEN-END:variables
}
