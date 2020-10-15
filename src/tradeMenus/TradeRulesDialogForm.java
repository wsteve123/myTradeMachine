/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableColumnModel;
import positions.commonGui;
import positions.myUtils;
import tradeMenus.TradeRules.Term;
 
/**
 *
 * @author earlie87
 */
public class TradeRulesDialogForm extends javax.swing.JDialog {

    TradeRules tradeRules = new TradeRules();
    private boolean ignoreAction = false;
    TradeRules.Term activeTerm = null;
    TradeRules.TradeTerms activeSelTerm = null;
    javax.swing.JTable activeTermTable = null;
    List<TradeRules.Term> activeTerms = null;
    List<TradeRules.Term> openTerms = new ArrayList <>();
    List<TradeRules.Term> closeTerms = new ArrayList <>();
    List<File> rulesDirList;
    static final int oOPEN_PANEL = 0;
    static final int oCLOSED_PANEL = 1;    
    IOTextFiles ioTextFiles = new IOTextFiles();
    IOTextFiles.ioListDirectory tradeRulesDirList;
    String prefixDirectory = "";
    String activeTradeRulesName = "";
    final String rulesExtention = ".rules";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/"; 
    String userSelectedTermOperation = "Open";
    int myCellEditorRowSelected = -1;
    String userSelectedTrigger = "None";
    int userSelectedBars = 0;
	//simple, weighted, hull
	String userSelectedMaType = "None";
    void clearTermTable(){
        for (int row = 0; row < activeTermTable.getRowCount(); row++){
            for (int col = 0; col < activeTermTable.getColumnCount(); col++){
                activeTermTable.getModel().setValueAt(null, row, col);
            }
        }
    }
    void clearRulesFileListTable(){
        for (int row = 0; row < rulesFilesTable.getRowCount(); row++){
            for (int col = 0; col < rulesFilesTable.getColumnCount(); col++){
                rulesFilesTable.getModel().setValueAt(null, row, col);
            }
        }
    }
    void updateRulesFileListTable(List<File> rulesList){
        clearRulesFileListTable();
		if(rulesList.size() > rulesFilesTable.getRowCount()){
			System.err.println("updateRulesFileListTable: error! Too rulesFileTable too small! Need: " + rulesList.size());
		}
        for(int row = 0; row < rulesList.size(); row++){
            rulesFilesTable.getModel().setValueAt(rulesList.get(row).getName(), row, 0);            
        }    
    }
    void updateTermTable(){
        //update activerTermTable..row #'s same as terms table index..
        //Term actTerm = new Term();
        TradeRules.Term actTerm;
        int sz = 0;
        clearTermTable();  
        
        for(int row = 0; row < activeTerms.size(); row++){ 
            actTerm = (TradeRules.Term)activeTerms.get(row);
            //actTerm = actTerm.getDaTerm();
            activeTermTable.getModel().setValueAt(actTerm.getTermSelItem(), row, 0);
            activeTermTable.getModel().setValueAt(actTerm.getDescription(), row, 1);
            activeTermTable.getModel().setValueAt(actTerm.getAndOrSelItem(), row, 2); 
            //andOrComboBox.setSelectedItem(actTerm.andOrSelItem);
        }        
    }
    int termTableUsedRows(){
        int retv = 0;
        System.out.println("\nactiveTermTable.getRowCnt: " + activeTermTable.getRowCount());
        for(int row = 0; row < activeTermTable.getRowCount(); row++){
            //if first two columns in row are accupied count it as one. Last AND OR maybe not be set so ignore.
            if ((activeTermTable.getModel().getValueAt(row, 0) != null) && (activeTermTable.getModel().getValueAt(row, 1) != null)){
                retv++;
            }
        }
        return retv;
    }
      
    /**
     * Creates new form tradeRulesDialogForm
     * @param parent
     * @param modal
     */    
    public TradeRulesDialogForm(java.awt.Frame parent, boolean modal){
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
        ignoreAction = true;        
        TableColumnModel tcm;
        termComboBox.removeAllItems();
        for(TradeRules.TradeTerms term : TradeRules.TradeTerms.values()){                       
            termComboBox.addItem(term.toString());
        }
        
        andOrComboBox.removeAllItems();
        for(TradeRules.Logic4Terms logic : TradeRules.Logic4Terms.values()){                       
            andOrComboBox.addItem(logic.toString());
        } 
        ignoreAction = false; 
        openTermTable.setColumnSelectionAllowed(true);
        openTermTable.setRowSelectionAllowed(true);
        openTermTable.setRowSelectionInterval(0, 0);
        openTermTable.setColumnSelectionInterval(0, 0); 
        activeTermTable = openTermTable;
        activeTermTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                activeTermTableMouseClicked(evt);
            }
        });
        
        activeTerms = openTerms; 
        tradeRules.setActiveTerms(activeTerms);
        tradeRules.setOpenTerms(openTerms);
        tradeRules.setClosedTerms(closeTerms);
        tcm = openTermTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(150);
        tcm.getColumn(1).setPreferredWidth(500);
        tcm.getColumn(2).setPreferredWidth(150);        
        tradeRulesDirList = ioTextFiles.new ioListDirectory(homeDirectory, "rules");
        rulesDirList = tradeRulesDirList.getList();
        System.out.println("\ntradeRulesDirList size = " + tradeRulesDirList.getSize());
        updateRulesFileListTable(rulesDirList);
        if(openCloseComboBox.getSelectedItem().equals("Open")){
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Open:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));
        }else if (openCloseComboBox.getSelectedItem().equals("Close")){
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Close:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));            
        }else{
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "None", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));
        }
        repaint();
        //openTermTable.setEnabled(false);
    }
    private boolean myCellEditor(int row, int col){        
        boolean retv = false;        
        System.out.println("\nmyCellEditor, row == " + row + " TT row count: " + activeTermTable.getRowCount());
        if(openCloseComboBox.getSelectedItem().equals("Disabled")){
            //commonGui.postToTextAreaMsg("Message", "Select Open OR Close!");
            commonGui.fuckingTest("JAVA!!!!", "FuckYou!!!");
            return false;
        }
        myCellEditorRowSelected = row;
        if (row <= activeTerms.size()){
            retv = true;
        }else if (row > activeTerms.size()){
            retv = false;
        }
        return retv;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        termComboBox = new javax.swing.JComboBox();
        jFrame1 = new javax.swing.JFrame();
        andOrComboBox = new javax.swing.JComboBox();
        aboveBelowButtonGroup = new javax.swing.ButtonGroup();
        topPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tradeRulesNameTextField = new javax.swing.JTextField();
        clearAllTermsButton = new javax.swing.JButton();
        saveTermsButton = new javax.swing.JButton();
        deleteTermsButton = new javax.swing.JButton();
        openCloseComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        openPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        openTermTable = new javax.swing.JTable();
        equationTriggerPanel = new javax.swing.JPanel();
        equationTriggerComboBox = new javax.swing.JComboBox();
        selectBarLabel = new javax.swing.JLabel();
        selectBarsTextField = new javax.swing.JTextField();
        equationLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        rulesFilesTable = new javax.swing.JTable();
        rulesFileLoadButton = new javax.swing.JButton();
        rulesFileDeleteButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();

        termComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        termComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                termComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        andOrComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "AND", "OR" }));
        andOrComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                andOrComboBoxActionPerformed(evt);
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Trade Rules\n");

        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Trade Rules Profile"));

        jLabel3.setText("TradeRulesName:");

        tradeRulesNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tradeRulesNameTextFieldActionPerformed(evt);
            }
        });

        clearAllTermsButton.setText("DeleteAll");
        clearAllTermsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllTermsButtonActionPerformed(evt);
            }
        });

        saveTermsButton.setText("Save Term(s)");
        saveTermsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTermsButtonActionPerformed(evt);
            }
        });

        deleteTermsButton.setText("DeleteTerm");
        deleteTermsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTermsButtonActionPerformed(evt);
            }
        });

        openCloseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Open", "Close" }));
        openCloseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCloseComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Select Open/Close:");

        openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Open When:"));

        openTermTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Term", "Description", "AndOr"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return myCellEditor(rowIndex, columnIndex);
                //return canEdit [columnIndex];
            }
        });
        openTermTable.setColumnSelectionAllowed(true);
        openTermTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openTermTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(openTermTable);
        if (openTermTable.getColumnModel().getColumnCount() > 0) {
            openTermTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(termComboBox)
            );
            openTermTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(andOrComboBox)
            );
        }

        javax.swing.GroupLayout openPanelLayout = new javax.swing.GroupLayout(openPanel);
        openPanel.setLayout(openPanelLayout);
        openPanelLayout.setHorizontalGroup(
            openPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(openPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        openPanelLayout.setVerticalGroup(
            openPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
        );

        equationTriggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Equation Trigger:"));
        equationTriggerPanel.setEnabled(false);

        equationTriggerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Delayed", "Begin" }));
        equationTriggerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equationTriggerComboBoxActionPerformed(evt);
            }
        });

        selectBarLabel.setText("Bars:");
        selectBarLabel.setEnabled(false);

        selectBarsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectBarsTextFieldActionPerformed(evt);
            }
        });

        equationLabel.setText("       ");

        javax.swing.GroupLayout equationTriggerPanelLayout = new javax.swing.GroupLayout(equationTriggerPanel);
        equationTriggerPanel.setLayout(equationTriggerPanelLayout);
        equationTriggerPanelLayout.setHorizontalGroup(
            equationTriggerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, equationTriggerPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(equationTriggerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(selectBarLabel)
                    .addGroup(equationTriggerPanelLayout.createSequentialGroup()
                        .addComponent(equationTriggerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(equationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectBarsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        equationTriggerPanelLayout.setVerticalGroup(
            equationTriggerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(equationTriggerPanelLayout.createSequentialGroup()
                .addComponent(selectBarLabel)
                .addGap(1, 1, 1)
                .addGroup(equationTriggerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(equationTriggerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectBarsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(equationLabel))
                .addContainerGap(48, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addComponent(saveTermsButton)
                        .addGap(18, 18, 18)
                        .addComponent(deleteTermsButton)
                        .addGap(29, 29, 29)
                        .addComponent(clearAllTermsButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(openPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(topPanelLayout.createSequentialGroup()
                                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(topPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addGap(18, 18, 18)
                                        .addComponent(tradeRulesNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(openCloseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addGap(18, 18, 18)
                                .addComponent(equationTriggerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(29, Short.MAX_VALUE))))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(tradeRulesNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openCloseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(equationTriggerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(openPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveTermsButton)
                    .addComponent(deleteTermsButton)
                    .addComponent(clearAllTermsButton))
                .addGap(18, 18, 18))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        rulesFilesTable.setModel(new javax.swing.table.DefaultTableModel(
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
                {null},
                {null}
            },
            new String [] {
                "Rules Files"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(rulesFilesTable);

        rulesFileLoadButton.setText("Load");
        rulesFileLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rulesFileLoadButtonActionPerformed(evt);
            }
        });

        rulesFileDeleteButton.setText("Delete");
        rulesFileDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rulesFileDeleteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(295, 295, 295)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rulesFileLoadButton)
                    .addComponent(rulesFileDeleteButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(436, 436, 436)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(rulesFileLoadButton)
                .addGap(18, 18, 18)
                .addComponent(rulesFileDeleteButton)
                .addContainerGap(50, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(12, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(12, Short.MAX_VALUE)))
        );

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exitButton)
                    .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                .addComponent(exitButton)
                .addGap(21, 21, 21))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveTermsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTermsButtonActionPerformed
        // TODO add your handling code here:
        int termTableUsedRows = termTableUsedRows();
        activeTradeRulesName = tradeRulesNameTextField.getText();
        
        System.out.println("\ntermTableUsedRows: " + termTableUsedRows());
        //check that last one is added, the one without AND/OR. off by one..      
        if((activeTerms.size()) > 0 && ((termTableUsedRows) == activeTerms.size() + 1)){
            activeTerm.setAndOr(0);
            activeTerm.setAndOrSelItem("None");
            activeTerms.add(activeTerm);
        }
        if(!activeTradeRulesName.isEmpty()){                        
            tradeRules.setOpenTerms(openTerms);
            tradeRules.setClosedTerms(closeTerms);
            tradeRules.setActiveTerms(activeTerms);
            tradeRules.setEquationTrigger(userSelectedTermOperation, userSelectedTrigger);
            tradeRules.setEquationBars(userSelectedTermOperation, userSelectedBars);
			userSelectedMaType = tradeRules.getMaType();
			tradeRules.setMaType(userSelectedMaType);
            tradeRules.wrTermsToTextFile(activeTradeRulesName);
            tradeRulesDirList = ioTextFiles.new ioListDirectory(homeDirectory, "rules");
            rulesDirList = tradeRulesDirList.getList();
            System.out.println("\ntradeRulesDirList size = " + tradeRulesDirList.getSize());
            updateRulesFileListTable(rulesDirList);
        }else{
            commonGui.postInformationMsg("Please Assign a Name before saving.");
        }
    }//GEN-LAST:event_saveTermsButtonActionPerformed

    private void tradeRulesNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tradeRulesNameTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_tradeRulesNameTextFieldActionPerformed

    //inner classes within slopeAnalysis...
    private void termComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_termComboBoxActionPerformed
		// TODO add your handling code here:  
		int termToAffect = 0;
		boolean replace = false;
		boolean existingTerm = false;
		Term term;
		
		if (ignoreAction == false) {
			//term = tradeRules.new Term();
			if ((termComboBox.getSelectedItem() != null)) {				
				termToAffect = myCellEditorRowSelected; //activeTermTable.getSelectedRow();
				System.out.println("\ntermComboBoxActionPerformed myCellEditorRowSelected: " + myCellEditorRowSelected);
				String selTermStr = termComboBox.getSelectedItem().toString();
				System.out.println("\n termComboBoxActionPerformed: row = " + termToAffect);
				System.out.println("\nselTerm: " + selTermStr);
				activeSelTerm = TradeRules.TradeTerms.valueOf(selTermStr);
				existingTerm = ((activeTerms.size() > 0) && (termToAffect < activeTerms.size()));
				//need the following cuz ActionPerformed gets called twice..which I don't understand..this is a way to work around..
				if (existingTerm == true) {
					term = activeTerms.get(termToAffect);
					if (termComboBox.getSelectedItem().equals(term.getTermSelItem())) {
						//termComboBox.setSelectedItem(null);
						//assume wants to edit user input only..			
						term.getUserInput();
						activeTerms.remove(termToAffect);
						activeTerms.add(termToAffect, term);
						activeTermTable.getModel().setValueAt(term.getDescription(), termToAffect, 1);
						termComboBox.setSelectedItem(selTermStr);
						return;
					}
				}				
				activeTerm = tradeRules.getSelectedTerm(activeSelTerm);
				activeTerm.setTermSelItem(termComboBox.getSelectedItem());				
				activeTerm.clearMe();
				activeTerm.getUserInput();
				activeTerm.setAndOr(0);
				//remove..
				System.out.println("\nselMa = " + activeTerm.getMa());
				activeTerm.setAndOrSelItem(TradeRules.Logic4Terms.oNone.toString());
				activeTermTable.getModel().setValueAt(activeTerm.getDescription(), termToAffect, 1);
				if (existingTerm == true) {
					activeTerms.remove(termToAffect);
				}
				activeTerms.add(termToAffect, activeTerm);
				//andOrComboBox.setSelectedItem(activeTerm.getAndOrSelItem());               
				activeTermTable.setRowSelectionInterval(termToAffect, termToAffect);
				activeTermTable.setColumnSelectionInterval(2, 2);
				System.out.println("\ntermComboBox: selItem = " + termComboBox.getSelectedItem());
				activeTerm = null;
			} else {
				System.out.println("\n + termComboBoxActionPerformed selected item == null??");
			}
		}
    }//GEN-LAST:event_termComboBoxActionPerformed

    private void andOrComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_andOrComboBoxActionPerformed
        // TODO add your handling code here:
        String selectedOp = "";
        Term term;
        Object tmp;
        int row = 0;
        boolean replace = false;
        boolean changed = false;
        if(ignoreAction == true){
            return;
        }
        //term = tradeRules.new Term();
        //int termToAffect = openTerms.size();   
        int termToAffect = activeTerms.size();
        row = termToAffect = activeTermTable.getSelectedRow();
        System.out.println("\nandOrComboBoxActionPerformed: row = " + row);
        activeTermTable.setRowSelectionInterval(termToAffect, termToAffect);
        activeTermTable.setColumnSelectionInterval(0, 0);
        
        if(activeTerm != null){
            term = activeTerm;
        } else {
            //get term to affect, affect AndOr, then put back ter openTerms...
            term = (termToAffect >= 0) ? activeTerms.get(termToAffect) : null;            
             if(term == null) {
                return;
             }else{        
                 replace = true;
             }             
        }
        if(andOrComboBox.getSelectedItem() != null) {  
            term.setAndOrSelItem(andOrComboBox.getSelectedItem());
            if (term.getAndOrSelItem().equals(TradeRules.Logic4Terms.oAnd.toString())) {
                term.setAndOr(1);
            } else if (term.getAndOrSelItem().equals(TradeRules.Logic4Terms.oOr.toString())) {
                term.setAndOr(2);
            }else{
                term.setAndOr(0);
            }
            changed = true;            
            if(!term.getDescription().isEmpty()){
                //now that we are done with this term add/replace to list..  
                if((replace == true) && (changed == true)){
                    activeTerms.remove(termToAffect);
                    activeTerms.add(termToAffect, term);                 
                }else if (changed == true){
                    activeTerms.add(term);
                }else{
                    
                }                
                activeTerm = null;                
            }                        
        }
    }//GEN-LAST:event_andOrComboBoxActionPerformed

    private void deleteTermsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTermsButtonActionPerformed
        // TODO add your handling code here:
        int selRow = activeTermTable.getSelectedRow();
        //row number == index in terms table..
        if(commonGui.postConfirmationMsg("Delete selected term?") == 0){
            activeTerms.remove(selRow);
            updateTermTable();
        };
    }//GEN-LAST:event_deleteTermsButtonActionPerformed
    private void updateTriggerPanel(String trigin, int barsin){
        if(trigin.equals("None")){
            selectBarLabel.setEnabled(false);
            selectBarsTextField.setEnabled(false);
            equationTriggerComboBox.setEnabled(true);
            equationLabel.setText("       ");
        }else if (trigin.equals("Delayed")){
            selectBarLabel.setEnabled(true);
            selectBarsTextField.setEnabled(true);
            equationTriggerComboBox.setEnabled(true);
            equationLabel.setText("By:    ");
            equationTriggerComboBox.setSelectedItem(trigin);
            selectBarsTextField.setText(Integer.toString(barsin));
        }else if (trigin.equals("Begin")){
            selectBarLabel.setEnabled(true);
            selectBarsTextField.setEnabled(true);
            equationTriggerComboBox.setEnabled(true);
            equationLabel.setText("Within:");
            equationTriggerComboBox.setSelectedItem(trigin);
            selectBarsTextField.setText(Integer.toString(barsin));
        }else{
            
        }        
    }
    private void rulesFileLoadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rulesFileLoadButtonActionPerformed
        // TODO add your handling code here:
        int selRow = 0;
        File file = null;        
        selRow = rulesFilesTable.getSelectedRow();
        System.out.println("\rulesFilesTableSelRow:" + selRow);
        file = rulesDirList.get(selRow);
        activeTerms.clear();
        tradeRules.rdTermsFromTextFile(rulesDirList.get(selRow).getName(), ""/*no extension*/);
        userSelectedTrigger = tradeRules.getEquationTrigger(userSelectedTermOperation);
        userSelectedBars = tradeRules.getEquationBars(userSelectedTermOperation);
        updateTriggerPanel(userSelectedTrigger, userSelectedBars);
        
        clearTermTable();
        updateTermTable();
        if(activeTerms.equals(openTerms)){
            openCloseComboBox.setSelectedItem("Open");
        }else if (activeTerms.equals(closeTerms)){
            openCloseComboBox.setSelectedItem("Close");
        }else{
            openCloseComboBox.setSelectedItem("None");
        }
    }//GEN-LAST:event_rulesFileLoadButtonActionPerformed

    private void clearAllTermsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllTermsButtonActionPerformed
        // TODO add your handling code here:
        activeTerms.clear();
        openTerms.clear();
        closeTerms.clear();
        updateTermTable();
    }//GEN-LAST:event_clearAllTermsButtonActionPerformed

    private void openCloseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCloseComboBoxActionPerformed
        // TODO add your handling code here:
        userSelectedTermOperation = openCloseComboBox.getSelectedItem().toString();
        if(openCloseComboBox.getSelectedItem().equals("Open")){
            if(!activeTerms.equals(openTerms) && (activeTerm != null)){
                //save last term before switching..
                activeTerms.add(activeTerm);
                activeTerm = null;
            }else{
                
            }
            activeTerms = openTerms;
            equationTriggerPanel.setEnabled(true);
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Open:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));
            equationTriggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Open Equation Trigger:"));
            equationTriggerComboBox.setSelectedItem(tradeRules.getEquationTrigger(userSelectedTermOperation));
            selectBarsTextField.setText(Integer.toString(tradeRules.getEquationBars(userSelectedTermOperation)));
        }else if (openCloseComboBox.getSelectedItem().equals("Close")){
            if(!activeTerms.equals(closeTerms) && (activeTerm != null)){
                //save last term before switching..
                activeTerms.add(activeTerm);
                activeTerm = null;
            }else{
                
            }
            activeTerms = closeTerms;
            equationTriggerPanel.setEnabled(true);
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Close:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));
            equationTriggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Close Equation Trigger:"));
            equationTriggerComboBox.setSelectedItem(tradeRules.getEquationTrigger(userSelectedTermOperation));
            selectBarsTextField.setText(Integer.toString(tradeRules.getEquationBars(userSelectedTermOperation)));
        }else{
            openPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "None", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Font", 0, 14)));
            equationTriggerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Equation Trigger:"));
            equationTriggerPanel.setEnabled(false);
        }
        repaint(); 
        openTermTable.setEnabled(true);
        updateTermTable();
    }//GEN-LAST:event_openCloseComboBoxActionPerformed

    private void rulesFileDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rulesFileDeleteButtonActionPerformed
        // TODO add your handling code here:
        int selRow = 0;
        File file = null;        
        selRow = rulesFilesTable.getSelectedRow();
        System.out.println("\nrulesFilesTableSelRow:" + selRow);
        file = rulesDirList.get(selRow);
        System.out.println("\nfileToDeleteName:" + file.getName());
        System.out.println("\nfileToDeletePath:" + file.getPath());
        IOTextFiles.ioDeleteTextFiles delFile = ioTextFiles.new ioDeleteTextFiles(file.getName());
        if (delFile.delete() == true){
            System.out.println("File deleted.");  
            rulesDirList = tradeRulesDirList.getList();
            updateRulesFileListTable(rulesDirList);
        }else{
            System.out.println("File does not exist?.");
        }                
                
    }//GEN-LAST:event_rulesFileDeleteButtonActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void openTermTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_openTermTableMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_openTermTableMouseClicked

    private void equationTriggerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_equationTriggerComboBoxActionPerformed
        // TODO add your handling code here:
        
        if((userSelectedTrigger = equationTriggerComboBox.getSelectedItem().toString()).equals("None")){
            selectBarLabel.setEnabled(false);
            selectBarsTextField.setEnabled(false);
            equationLabel.setText("       ");
        }else if (userSelectedTrigger.equals("Delayed")){
            selectBarLabel.setEnabled(true);
            selectBarsTextField.setEnabled(true);
            equationLabel.setText("By:    ");
        }else if (userSelectedTrigger.equals("Begin")){
            selectBarLabel.setEnabled(true);
            selectBarsTextField.setEnabled(true);
            equationLabel.setText("Within:");
        }else{
            
        }
        tradeRules.setEquationTrigger(userSelectedTermOperation, userSelectedTrigger);
        
        System.out.println("\nuserSelectedTrigger: " + userSelectedTrigger);
    }//GEN-LAST:event_equationTriggerComboBoxActionPerformed

    private void selectBarsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectBarsTextFieldActionPerformed
        // TODO add your handling code here:
        userSelectedBars = Integer.valueOf(selectBarsTextField.getText());
        tradeRules.setEquationBars(userSelectedTermOperation, userSelectedBars);
        System.out.println("\nuserSelectedBars: " + userSelectedBars);
    }//GEN-LAST:event_selectBarsTextFieldActionPerformed
    
    private void activeTermTableMouseClicked(java.awt.event.MouseEvent evt) {                                             
        // TODO add your handling code here:
        int row, col;
        try {
            row = activeTermTable.getSelectedRow();
            col = activeTermTable.getSelectedColumn();
            
            System.out.println("activeTermTableMouseClicked: " + "row, col is : " + row + ", " + col);
        }catch(Exception e) {
            System.out.println("activeTermTableMouseClicked: Exception!!" + evt);   
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
            java.util.logging.Logger.getLogger(TradeRulesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TradeRulesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TradeRulesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TradeRulesDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                TradeRulesDialogForm dialog = null;
                dialog = new TradeRulesDialogForm(new javax.swing.JFrame(), true);
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
    private javax.swing.ButtonGroup aboveBelowButtonGroup;
    private javax.swing.JComboBox andOrComboBox;
    private javax.swing.JButton clearAllTermsButton;
    private javax.swing.JButton deleteTermsButton;
    private javax.swing.JLabel equationLabel;
    private javax.swing.JComboBox equationTriggerComboBox;
    private javax.swing.JPanel equationTriggerPanel;
    private javax.swing.JButton exitButton;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JComboBox openCloseComboBox;
    private javax.swing.JPanel openPanel;
    private javax.swing.JTable openTermTable;
    private javax.swing.JButton rulesFileDeleteButton;
    private javax.swing.JButton rulesFileLoadButton;
    private javax.swing.JTable rulesFilesTable;
    private javax.swing.JButton saveTermsButton;
    private javax.swing.JLabel selectBarLabel;
    private javax.swing.JTextField selectBarsTextField;
    private javax.swing.JComboBox termComboBox;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextField tradeRulesNameTextField;
    // End of variables declaration//GEN-END:variables
}
