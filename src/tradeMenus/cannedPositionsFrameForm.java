/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.*;
import positions.commonGui;
import positions.positionData;
import positions.*;

/**
 *
 * @author earlie87
 */
public class cannedPositionsFrameForm extends javax.swing.JFrame {
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    private String cannedFileName = "canned_dn_positions.prn";
    // an array of month and positions per month..
    private final int MONTHS = 12;
    private final int POS_MAX = 15;
    //private String[][] cannedPositionsArray = new String[MONTHS][POS_MAX];
    private cannedData[][] cannedPositionsArray;
    private String selectedMonth = null;
    private int selectedMonthIdx = 0;
    private int selectedTableRow = -1;
    private final String[] MONTH_LIST = {   "January","February","March","April",
                                            "May","June","July","August","September",
                                            "October","November","December"
    };  
    displayOptionChainDialogForm optionChainDialog;
    positions actPositions = null;
    createPositionDialogForm createPosition;
    
    /**
     * Creates new form cannedPositionsFrameForm
     */
    public cannedPositionsFrameForm() {
        initComponents();
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });
        cannedPositionsArray = new cannedData[MONTHS][POS_MAX];
        readCannedPositionsFromFile();
        updateComboBoxList();
        /* disable these for now, enable when table row is selected. */
        deletPositionButton.setEnabled(false);
        createPositionButton.setEnabled(false);
        
    }
    public void addToThisPortfolio(positions posin){
        actPositions = posin;
    }
    private class cannedData {
        String displayName;
        String ticker;  
        boolean deleted;
    }

    private void initCannedArray() {
        int ir = 0;
        int ic = 0;
        for (ir = 0; ir < MONTHS; ir++) {
            for (ic = 0; ic < POS_MAX; ic++){
            cannedPositionsArray[ir][ic] = new cannedData();
            cannedPositionsArray[ir][ic].displayName = null;
            cannedPositionsArray[ir][ic].ticker = null;
            cannedPositionsArray[ir][ic].deleted = false;
            }
        }
    }
    private void readCannedPositionsFromFile() {
        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        String tmpStr = null;
        String rdFilename = homeDirectory;
        String[] month;
        int midx = 0;
        int pidx = 0;
        rdFilename += cannedFileName;
        boolean error = false;
        int tint = 0;
        boolean done = false;
        
        initCannedArray();
        try {
            fis = new FileInputStream(rdFilename);
            dis = new DataInputStream(fis);
            bir = new BufferedReader(new InputStreamReader(fis));
            
            for (midx = 0; (midx < MONTHS) && !error && !done; midx++) {
                if (((tmpStr = bir.readLine()) != null) && !error) {
                    System.out.println(tmpStr);
                    month = tmpStr.split("[*]");
                    
                    System.out.println("month sz = " + month.length);
                    if (month.length >= POS_MAX) {
                        commonGui.postInformationMsg("canned entry has too many positions!!!! Over allowed:  " + POS_MAX);
                        error = true;
                    } else {
                        /*
                             * start with actual position, skip month name in
                             * first element.
                             */
                        for (pidx = 0; pidx < month.length-1; pidx++) {
                            /*
                             * start with actual position, skip month name in
                             * first element.
                             */
                            cannedPositionsArray[midx][pidx].displayName = month[pidx+1].trim();
                            tint = month[pidx+1].indexOf(" ");
                            if (tint < 0){
                                commonGui.postInformationMsg("Canned file reader error! "+month[pidx]+" wrong.");
                                cannedPositionsArray[midx][pidx].ticker = month[pidx+1].trim();
                            }else {
                                tmpStr = month[pidx+1].substring(0, tint);
                                tmpStr = tmpStr.trim();
                                cannedPositionsArray[midx][pidx].ticker = tmpStr.trim();
                            }
                        }
                    }

                }else{
                    done = true;
                }
            }


            bir.close();
        } catch (Exception e) {
            System.out.println("error reading text for: " + cannedFileName + "(" + e + ").");
            System.out.println("homeDir:" + homeDirectory + cannedFileName);
        }
        
        System.out.println("done.");
    }
    private void writeCannedPositionsToFile() {
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        boolean split = false;
        String wrFileName = homeDirectory;
        String tmpStr;
        int midx = 0;
        int pidx = 0;
        wrFileName += cannedFileName;
        boolean error = false;
        int tint = 0;
        boolean done = false;
        cannedData pos = new cannedData();
        
        
        try {
            fos = new FileOutputStream(wrFileName);
            dos = new DataOutputStream(fos);
            bow = new BufferedWriter(new OutputStreamWriter(fos));
            
            for (midx = 0; (midx < MONTHS) && !error; midx++) {
                bow.write(MONTH_LIST[midx]+" *");
                for (pidx = 0, done = false; !done; pidx++) {
                    pos = cannedPositionsArray[midx][pidx];
                    if (pos.displayName == null) {
                        done = true;
                    }else if (pos.deleted == false){
                        bow.write(pos.displayName+"*");    
                    }
                }
                bow.newLine(); 
            }
            
            
            bow.close();
        } catch (Exception e) {
            System.out.println("error writing text to: " + wrFileName + "(" + e + ").");
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
        monthSelectCombobox = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        cannedPositionTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        createPositionButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        addPositionButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        deletPositionButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Canned Positions Dialog ");

        jLabel1.setText("Seclect Month");

        monthSelectCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        monthSelectCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthSelectComboboxActionPerformed(evt);
            }
        });

        cannedPositionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Symbols"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        cannedPositionTable.setToolTipText("Select Position From List");
        cannedPositionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cannedPositionTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(cannedPositionTable);
        cannedPositionTable.getColumnModel().getColumn(0).setResizable(false);

        jLabel2.setText("Create New Position");

        createPositionButton.setText("Create");
        createPositionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPositionButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Add Position");

        addPositionButton.setText("Add");
        addPositionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPositionButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Delete Position");

        deletPositionButton.setText("Delete");
        deletPositionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletPositionButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Canned List");

        jLabel6.setText("Select From Symbol List To Perform Operation");

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
                .add(54, 54, 54)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addPositionButton)
                    .add(monthSelectCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(deletPositionButton))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(45, 45, 45)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(39, 39, 39)
                                .add(createPositionButton))
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(jLabel2))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(60, 60, 60)
                        .add(jLabel5)))
                .addContainerGap(36, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 298, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(79, 79, 79))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(doneButton)
                        .add(203, 203, 203))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 269, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(33, 33, 33)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(monthSelectCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(38, 38, 38)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addPositionButton)
                        .add(26, 26, 26)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deletPositionButton))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(122, 122, 122)
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(createPositionButton)))
                .add(18, 18, 18)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 26, Short.MAX_VALUE)
                .add(doneButton)
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
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void updateComboBoxList(){
        cannedData posData;
        boolean done = false;
        
        clearCannedPositionTable();
        selectedMonthIdx = monthSelectCombobox.getSelectedIndex();
        for (int ir = 0; !done; ir++) {
            posData = cannedPositionsArray[selectedMonthIdx][ir];
            if (posData.displayName == null){
                done = true;
            }else {
                cannedPositionTable.getModel().setValueAt(posData.displayName, ir, 0);
            }
        }    
    }
    private void clearCannedPositionTable() {
        int rowCount = cannedPositionTable.getRowCount();
        int colCount = cannedPositionTable.getColumnCount();
        for (int il = 0; il < rowCount; il++){
            for (int ic = 0; ic < colCount; ic++) {
                cannedPositionTable.getModel().setValueAt("", il, ic);
            }
        }
        
    }
    private void monthSelectComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthSelectComboboxActionPerformed
        String posStr = "ok";
        
        selectedMonth = String.valueOf(monthSelectCombobox.getSelectedItem());
        
        selectedMonthIdx = monthSelectCombobox.getSelectedIndex();
        System.out.println("selectMonth : "+selectedMonth);
        updateComboBoxList();
        
        
    }//GEN-LAST:event_monthSelectComboboxActionPerformed

    private void createPositionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPositionButtonActionPerformed
        String posTickerToCreate = null;
        
        if (actPositions == null){
            commonGui.postInformationMsg("actPositions == null?");
            return;
        }
        posTickerToCreate = cannedPositionsArray[selectedMonthIdx][selectedTableRow].ticker;
        System.out.println("createPosition for : " + posTickerToCreate);
        
        createPosition = new createPositionDialogForm(new javax.swing.JFrame(), true);
        createPosition.addToThisPortfolio(actPositions);        
        createPosition.setTickerToStart(posTickerToCreate);
        createPosition.setVisible(true);
        
        
        /*
        optionChainDialog = new displayOptionChainDialogForm(new javax.swing.JFrame(), true);
        optionChainDialog.setActivePortfolio(PlayWIthMenus.actMainMenu.actPositions);
        optionChainDialog.setTickerToStart(posTickerToCreate);
        optionChainDialog.setVisible(true);
        * 
        */
        
    }//GEN-LAST:event_createPositionButtonActionPerformed

    private void addPositionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPositionButtonActionPerformed
        
        boolean found = false;
        String newPos = null;
        int nextAvailPos = 0;
        int tint = 0;
        String tmpStr = null;
        
        newPos = commonGui.getUserInput("New Position Name", "ADBE (e/m/l)");
        System.out.println("entered: "+newPos);
        if (newPos != null) {
            newPos = newPos.toUpperCase();

            /*
             * search for next avail location to add this new guy..
             */
            for (int i = 0; (i < POS_MAX) && !found; i++) {
                if (cannedPositionsArray[selectedMonthIdx][i].displayName == null) {
                    found = true;
                    nextAvailPos = i;
                }

            }
            if (found == true) {
                cannedPositionsArray[selectedMonthIdx][nextAvailPos].displayName = newPos;
                tint = newPos.indexOf(" ");
                tmpStr = newPos.substring(0, tint);
                cannedPositionsArray[selectedMonthIdx][nextAvailPos].ticker = tmpStr.trim();
                System.out.println("new display Name = " + newPos + " ticker is : " + tmpStr);
                System.out.println("writing to canned position file...");
                writeCannedPositionsToFile();
                System.out.println("reading canned position from file...");
                readCannedPositionsFromFile();
                updateComboBoxList();
            }
        }
    }//GEN-LAST:event_addPositionButtonActionPerformed

    private void deletPositionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletPositionButtonActionPerformed
        selectedTableRow = cannedPositionTable.getSelectedRow();
        System.out.println("selected Table row: "+selectedTableRow);
        System.out.println("Deleting selected position...("+cannedPositionsArray[selectedMonthIdx][selectedTableRow].displayName+")");
        cannedPositionsArray[selectedMonthIdx][selectedTableRow].deleted = true;
        System.out.println("writing to canned position file...");
        writeCannedPositionsToFile();
        System.out.println("reading canned position from file...");
        readCannedPositionsFromFile();
        updateComboBoxList();
    }//GEN-LAST:event_deletPositionButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        writeCannedPositionsToFile();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void cannedPositionTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cannedPositionTableMouseClicked
        selectedTableRow = cannedPositionTable.getSelectedRow();
        System.out.println("selected Table row: "+selectedTableRow);
        deletPositionButton.setEnabled(true);
        createPositionButton.setEnabled(true);
    }//GEN-LAST:event_cannedPositionTableMouseClicked

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPositionButton;
    private javax.swing.JTable cannedPositionTable;
    private javax.swing.JButton createPositionButton;
    private javax.swing.JButton deletPositionButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox monthSelectCombobox;
    // End of variables declaration//GEN-END:variables
}
