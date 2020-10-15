/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import positions.*;

/**
 *
 * @author earlie87
 */
public class displayPostionDetailFrameForm extends javax.swing.JFrame {
    private positions actPortfolio;
    //column constants
    private final int POSID = 0;
    private final int LSYM = 1;
    private final int SSYM = 2;
    private final int LPRICE = 3;
    private final int SPRICE = 4;
    private final int LSHARES = 5;
    private final int SCONTRACTS = 6;
    private final int SDELTA = 7;
    private final int BALANCE = 8;
    private final int PL = 9;
    private final int CREATED = 10;

    /**
     * Creates new form displayPostionDetailFrameForm
     */
    public displayPostionDetailFrameForm(positions portfoin) {
        
        actPortfolio = portfoin;
        initComponents();
        displayDetails();
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        setVisible(false);
                        dispose();
                    }
                });

    }

    private void displayDetails() {
        positionAdjustment actAdj;
        int posNum;
        positionData actPos;
        int actLine = 0;
        //posData actPos = new posData();
  
        int numOfPositions = actPortfolio.posDataVecSize();
        int numOfAdjustments;
        int rowNum = 0;
                /*
         * protection needed here, take the key.
         */
        actPortfolio.semTake();
        actLine = 0;
        for (posNum = 0, actPos = actPortfolio.posDataFetchNext(true); posNum < numOfPositions;) {
            numOfAdjustments = actPos.posAdjustments();
            displayPositionDetailTable.getModel().setValueAt(Integer.toString(actPos.getPosId() + 1), rowNum ,POSID );
            displayPositionDetailTable.getModel().setValueAt(actPos.longTicker, rowNum, LSYM);
            displayPositionDetailTable.getModel().setValueAt(actPos.shortTicker, rowNum, SSYM);
            displayPositionDetailTable.getModel().setValueAt(Float.toString(actPos.longEntryPrice), rowNum, LPRICE);
            displayPositionDetailTable.getModel().setValueAt(Float.toString(actPos.shortEntryPrice), rowNum, SPRICE);
            displayPositionDetailTable.getModel().setValueAt(Integer.toString(actPos.longShares), rowNum, LSHARES);
                
            displayPositionDetailTable.getModel().setValueAt(Integer.toString(actPos.shortShares), rowNum, SCONTRACTS);
            
            displayPositionDetailTable.getModel().setValueAt(Float.toString(actPos.shortDelta), rowNum, SDELTA);
            displayPositionDetailTable.getModel().setValueAt(Float.toString(actPos.posBalance), rowNum, BALANCE);
            displayPositionDetailTable.getModel().setValueAt(Float.toString((float)0.0), rowNum, PL);
            displayPositionDetailTable.getModel().setValueAt(actPos.getPosDateStr(), rowNum, CREATED);
            
            for (int adjLine = 1; adjLine < (numOfAdjustments + 1); adjLine++) {
                // get active Trade Obj to work on.
                actAdj = (positionAdjustment) actPos.posAdjGet(adjLine - 1);
                displayPositionDetailTable.getModel().setValueAt(
                        Integer.toString(actPos.getPosId() + 1) + "." + Integer.toString(adjLine), (rowNum + adjLine),
                        0);
                displayPositionDetailTable.getModel().setValueAt("-->",(rowNum + adjLine), 1);
                displayPositionDetailTable.getModel().setValueAt("-->",(rowNum + adjLine), 2);
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.longPrice),(rowNum + adjLine), 3);
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.shortPrice),(rowNum + adjLine), 4);    
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.sharesLong)+ actAdj.outcome.getOutcomeLongStr(),
                                                                (rowNum + adjLine), 
                                                                5);
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.sharesShort) + actAdj.outcome.getOutcomeShortStr(),
                                                                (rowNum + adjLine), 
                                                                6);  
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.delta),(rowNum + adjLine), 7); 
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.balance),(rowNum + adjLine), 8);
                displayPositionDetailTable.getModel().setValueAt(Float.toString(actAdj.profitLoss),(rowNum + adjLine), 9);
                displayPositionDetailTable.getModel().setValueAt(actAdj.getAdjDateStr(),(rowNum + adjLine), 10);    
            }
            rowNum = ((rowNum) + (numOfAdjustments) + 1);
            if (++posNum < numOfPositions) {
                actPos = actPortfolio.posDataFetchNext(false);
                numOfAdjustments = actPos.posAdjustments();

            }

        }
        /* give back key, we're done. */
        actPortfolio.semGive();
    }
    public void setActivePortfolio(positions posin) {
        actPortfolio = posin;
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
        displayPositionDetailTable = new javax.swing.JTable();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        displayPositionDetailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "PosID", "LSymb", "SSymb", "LPrice", "SPrice", "LShares", "SContracts", "SDelta", "Balance", "P/L", "Created"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        displayPositionDetailTable.setName("Position Detail Display");
        jScrollPane1.setViewportView(displayPositionDetailTable);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1219, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(556, 556, 556)
                .add(closeButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 429, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 81, Short.MAX_VALUE)
                .add(closeButton)
                .add(41, 41, 41))
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

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTable displayPositionDetailTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
