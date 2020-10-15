/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.JOptionPane;
import java.util.*;
import javax.swing.Timer;

import positions.*;
import tradeMenus.portfolio.*;
import tradeMenus.NewTraderTest.*;

/**
 *
 * @author earlie87
 */
public class playMenuForm extends javax.swing.JFrame {
    public boolean portfolioListChanged = false;
    public positions actPositions = null;
    public String activeAccount = null;
    public String activePortfolio = "portfolio_DU123603_1.dat";
    private positionConfig actPosConfig = null;
    private boolean readyToRun = true;
    private tradeHandler actTradeHandler = null;
    private boolean traderRunning = false;
    private portfolioData newlyRunning = null;
    
    private final int MAX_PORTFOLIOS = 20;
    private int portfolioCnt = 0;
    private int tableIndex = 0;
    private portfolio activePortfolioTbl[] = new portfolio[MAX_PORTFOLIOS];
    private Timer aTimer;
    private int tCount = 0;
    //pivotPoint portfolio added 3/4/15
    public portfolio pivotPointPortfolio = new portfolio(); ;
    List<String> pivotPointTradeList = new ArrayList<>();
    private String pivotPointActivePortfolio = null;
    
    /* portfolio table columns */
    final int oPORTFOLIO_NAME   = 0;
    final int oSTATE            = 1; 
    final int oTRADES           = 2;
    final int oNEW              = 3;
    final int oOPEN             = 4;
    final int oCLOSED           = 5;
    final int oNLQVAL           = 6;
    final int oCASH             = 7;

    public class portfolioData {
        String state = null;
        int tableLineNumber = 0;
        String name = null;
        boolean traderRunning = false;
        int todaysTrades = 0;
        double netLiqVal = 0.0;
        double cashLeft = 0.0;
        double profitLoss = 0.0;
        public portfolioData() {
        }
    }
    private portfolio fetchPortfolioData(String portfolioName) {
        int idx = 0;
        boolean found = false;
        for (idx = 0; (idx < MAX_PORTFOLIOS) && !found; idx++) {
            
            if ((activePortfolioTbl[idx] != null) && (activePortfolioTbl[idx].portfolioFileName.equals(portfolioName))) {
                found = true;
            }
        }
        if (found == true) {
            return (activePortfolioTbl[idx-1]);
        }else {
            return (null);
        }
        
    }
    
    private boolean isThisPorfolioRunning(positions posin) {
        int idx = 0;
        boolean found = false;
        for (idx = 0; (idx < MAX_PORTFOLIOS) && !found; idx++) {
            
            if ((activePortfolioTbl[idx] != null) && (activePortfolioTbl[idx].portfolioFileName.equals(posin.getPositionFileName()))) {
                found = true;
            }
        }
        return(found);
    }
    
    private int findNextAvailSpot() {
        int idx = 0;
        boolean foundit = false;
        for (idx = 0; (idx < MAX_PORTFOLIOS) && !foundit; idx++) {
            if (activePortfolioTbl[idx] == null) {
                foundit = true;
            }
        }
        if (!foundit){
            idx = -1;
        }else{
            idx--;
        }
        return (idx);
    }
    private void addToActivePortfolioTbl(portfolio pin) {
        int tblSpot;
        
        //pin.setState(slopeDefs.oREADY);
        tblSpot = findNextAvailSpot();
        if (tblSpot != -1) {
            activePortfoliosTable.getModel().setValueAt(pin.aliasName, tableIndex, 0);
            activePortfoliosTable.getModel().setValueAt(pin.state, tableIndex, oSTATE);
            activePortfoliosTable.getModel().setValueAt(pin.getTodaysTrades(), tableIndex, oTRADES);
            activePortfoliosTable.getModel().setValueAt(pin.getNewCnt(), tableIndex, oNEW);
            activePortfoliosTable.getModel().setValueAt(pin.getOpenCnt(), tableIndex, oOPEN);
            activePortfoliosTable.getModel().setValueAt(pin.getClosedCnt(), tableIndex, oCLOSED);
            activePortfoliosTable.getModel().setValueAt(pin.getNLV(), tableIndex, oNLQVAL);
            activePortfoliosTable.getModel().setValueAt(pin.getCash(), tableIndex, oCASH);
            pin.tableLineNumber = tableIndex;
            tableIndex++;
            activePortfolioTbl[tblSpot] = pin;            
        }else {
            System.err.println("addToActivePortfolioTable: OUT of TABLE SPACE!!!!!");
        }
        
        
    }
    private void updatePortfolioData() {
        
    }
    private void removeActivePortfolioTbl(String portfolioNameIn) {
        int idx = 0;
        boolean done = false;
        for (idx = 0; (idx < MAX_PORTFOLIOS) && !done; idx++) {
            if ((activePortfolioTbl[idx] != null) && activePortfolioTbl[idx].portfolioFileName.equals(portfolioNameIn)) {
                activePortfoliosTable.getModel().setValueAt("", activePortfolioTbl[idx].tableLineNumber, 0);
                activePortfoliosTable.getModel().setValueAt("", activePortfolioTbl[idx].tableLineNumber, 1);
                activePortfolioTbl[idx] = null;
                done = true;
            }
        }
        if (!done){
            System.err.println("removeToActivePortfolioTable: did not find the portfolio!!!!!");    
        }else {
            updateActivePortfolioTable();
        }
        
    }
    
    private void updateActivePortfolioTable() {
        int idx = 0;
        int rowCount = activePortfoliosTable.getRowCount();
        int colCount = activePortfoliosTable.getColumnCount();
        for (int il = 0; il < rowCount; il++){
            for (int ic = 0; ic < colCount; ic++) {
                activePortfoliosTable.getModel().setValueAt("", il, ic);
            }
        }
        tableIndex = 0;
        for (idx = 0; idx < MAX_PORTFOLIOS; idx++) {
            if (activePortfolioTbl[idx] != null) {
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].aliasName, tableIndex, 0);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getState(), tableIndex, oSTATE);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getTodaysTrades(), tableIndex, oTRADES);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getNewCnt(), tableIndex, oNEW);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getOpenCnt(), tableIndex, oOPEN);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getClosedCnt(), tableIndex, oCLOSED);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getNLV(), tableIndex, oNLQVAL);
                activePortfoliosTable.getModel().setValueAt(activePortfolioTbl[idx].getCash(), tableIndex, oCASH);
                tableIndex++;
            }
        }
    }
    private void updateActivePortfolioTableTimer() {
        tableIndex = 0;
        int idx = 0;
        for (idx = 0; idx < MAX_PORTFOLIOS; idx++) {
            if (activePortfolioTbl[idx] != null) {
                activePortfoliosTable.getModel().setValueAt(Integer.toString(tCount), tableIndex, 1);
                tableIndex++;     
            }
        }
        
    }
    
    String[] portfolioNameList;
    final String PREFACE = "portfolio";
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    /**
     * Creates new form playMenuForm
     */
    public playMenuForm(String titleIn) {
        int idx = 0, lNum = 0, cNum = 0;
        initComponents();
        this.setTitle("Pivotal Point Trading System");
        addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        final ibApi actIbApi = ibApi.getActApi();
                        System.out.println("Stopping aTimer... disconnecting from IB..and closing window.");                       
                        actIbApi.disConnectFromIbHost();
                        myUtils.delay(2000);
                        aTimer.stop();
                        setVisible(false);
                        dispose();
                    }
                });

        aTimer = new Timer(3000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                pivotPointPortfolio.state = pivotPointPortfolio.positions.state;
                updateActivePortfolioTable();
                tCount++;
                
            }
        });
        aTimer.start();
        //create positions from disk here...        
        
        portfolioNameList = getListOfPortfolioFilenames(homeDirectory);
        for (int i = 0; i < portfolioNameList.length; i++) {
            activePortfolioComboBox.addItem(portfolioNameList[i]);
            
            pivotPointPortfolio = new portfolio();
            pivotPointPortfolio.setPortfolioFilename(portfolioNameList[i]);
            pivotPointPortfolio.rdFromFile();
            pivotPointPortfolio.positions.setPortolioName(pivotPointPortfolio.portfolioFileName);
            pivotPointPortfolio.positions.setPortfolioAlias(pivotPointPortfolio.aliasName);
            pivotPointPortfolio.positions.setAccountNumber(pivotPointPortfolio.accountNumber);
            pivotPointPortfolio.positions.setPathnamePrefix(pivotPointPortfolio.portfolioFileNamePrefix);
            pivotPointPortfolio.setNumOfSharesToTrade(pivotPointPortfolio.numOfSharesToTrade);
            pivotPointPortfolio.setPositionBias(pivotPointPortfolio.positionBias);
            pivotPointPortfolio.setVersion(pivotPointPortfolio.version);
            if (pivotPointPortfolio.getState().equals(slopeDefs.oREADY)){
                pivotPointPortfolio.positions.rdTradeListFromFile();
                //create an enclosing instance ..
                PortfolioTrader trader = pivotPointPortfolio.new PortfolioTrader();
                pivotPointPortfolio.portfolioTrader = trader; 
                pivotPointPortfolio.positions.setUserCriteria(pivotPointPortfolio.userCriteria);
            }
            addToActivePortfolioTbl(pivotPointPortfolio);          
        }
    }
    public void addActivePortfolio() {
        
    }
    public void refreshPortfolioComboBox() {
        portfolioNameList = getListOfPortfolioFilenames(homeDirectory);
        for (int i = 0; i < portfolioNameList.length; i++) {
            activePortfolioComboBox.addItem(portfolioNameList[i]);
        }
        
    }
    
    private String[] getListOfPortfolioFilenames(String fromThisDirectory) {
        int count = 0;
        // our filename filter (filename pattern matcher)
        class onlyPortfolioNames implements FilenameFilter {
            
            public boolean accept(File dir, String s) {
                if (s.startsWith(PREFACE) && (myUtils.countOccurrences(s, '.') == 2) && (s.endsWith(".txt")) ) {
                    
                    return true;
                }
                return false;
            }
        }
        return new java.io.File(fromThisDirectory).list(new onlyPortfolioNames());
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        deltaNeutralPane = new javax.swing.JPanel();
        activePortfolioLabel = new javax.swing.JLabel();
        activePortfolioComboBox = new javax.swing.JComboBox();
        accountLabel = new javax.swing.JLabel();
        activeAccountLabel = new javax.swing.JLabel();
        runTraderStartButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        activePortfoliosTable = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        showPortfolioButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        testButton = new javax.swing.JButton();
        deltaNeutralMenuBar = new javax.swing.JMenuBar();
        profileMenu = new javax.swing.JMenu();
        createNewPortfolioMenuItem = new javax.swing.JMenuItem();
        editExistingMenuItem = new javax.swing.JMenuItem();
        deletePortfolioMenuItem = new javax.swing.JMenuItem();
        displayMenu = new javax.swing.JMenu();
        positionCreation = new javax.swing.JMenuItem();
        runSlopeTraderMenuItem = new javax.swing.JMenuItem();
        CreateMenu = new javax.swing.JMenu();
        displayStockQuoteMenuItem = new javax.swing.JMenuItem();
        displayOptionQuoteMenuItem = new javax.swing.JMenuItem();
        displayOptionChainMenuItem = new javax.swing.JMenuItem();
        displayStatsMenuItem = new javax.swing.JMenuItem();
        displayChartMenuItem = new javax.swing.JMenuItem();
        displayBalanceAndPositionsMenuItem = new javax.swing.JMenuItem();
        displayImpliedVolatilityMenuItem = new javax.swing.JMenuItem();
        displayManagedAccountInfoMenuItem = new javax.swing.JMenuItem();
        createOptionFilesForExchangeMenuItem = new javax.swing.JMenuItem();
        displayOptionsWithUnusualVolumeMenuItem = new javax.swing.JMenuItem();
        filterForStocksMenuItem = new javax.swing.JMenuItem();
        displayPrintableSummaryMenuItem = new javax.swing.JMenuItem();
        displayCompanyInfoMenuItem = new javax.swing.JMenuItem();
        earningDatesMenuItem = new javax.swing.JMenuItem();
        accountValuesMenuItem = new javax.swing.JMenuItem();
        FilesMenu = new javax.swing.JMenu();
        configGlobalMenuItem = new javax.swing.JMenuItem();
        configPerPositionMenuItem = new javax.swing.JMenuItem();
        configImpliedVolatilityMenuItem = new javax.swing.JMenuItem();
        ProcessFiles = new javax.swing.JMenu();
        csvFileMenuItem = new javax.swing.JMenuItem();
        mergeFilesMenuItem = new javax.swing.JMenuItem();
        splitFilesMenuItem = new javax.swing.JMenuItem();
        playPolyMenuItem = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Delta Neutral Trading");
        setName("DeltaNeutralTraderFrame"); // NOI18N

        deltaNeutralPane.setName(" null"); // NOI18N

        activePortfolioLabel.setText("Focus Portfolio");

        activePortfolioComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activePortfolioComboBoxActionPerformed(evt);
            }
        });

        accountLabel.setText(" Account");

        runTraderStartButton.setText("Activate");
        runTraderStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runTraderStartButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Action");

        activePortfoliosTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Portfolios", "State", "Trades", "New", "Open", "Closed", "NetLiqVal", "Cash"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(activePortfoliosTable);

        jLabel3.setText("Active Portfolios:");

        jLabel4.setText("Pivotal Point Automatic Trading ");

        showPortfolioButton.setText("Show");
        showPortfolioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPortfolioButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Show Portfolio");

        testButton.setText("Test");
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout deltaNeutralPaneLayout = new org.jdesktop.layout.GroupLayout(deltaNeutralPane);
        deltaNeutralPane.setLayout(deltaNeutralPaneLayout);
        deltaNeutralPaneLayout.setHorizontalGroup(
            deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(deltaNeutralPaneLayout.createSequentialGroup()
                .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(deltaNeutralPaneLayout.createSequentialGroup()
                                .add(accountLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(activeAccountLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 193, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, deltaNeutralPaneLayout.createSequentialGroup()
                                    .add(activePortfolioLabel)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(activePortfolioComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 228, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(jLabel2)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(runTraderStartButton)
                                    .add(25, 25, 25))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 543, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(54, 54, 54)
                        .add(jLabel5))
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(196, 196, 196)
                        .add(jLabel4))
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(230, 230, 230)
                        .add(jLabel3)))
                .addContainerGap(138, Short.MAX_VALUE))
            .add(deltaNeutralPaneLayout.createSequentialGroup()
                .add(44, 44, 44)
                .add(showPortfolioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(testButton)
                .add(172, 172, 172))
        );
        deltaNeutralPaneLayout.setVerticalGroup(
            deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(deltaNeutralPaneLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel4)
                .add(43, 43, 43)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 194, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(jLabel5)
                .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(2, 2, 2)
                        .add(showPortfolioButton)
                        .add(84, 84, 84)
                        .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(activePortfolioLabel)
                            .add(activePortfolioComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2)
                            .add(runTraderStartButton))
                        .add(18, 18, 18)
                        .add(deltaNeutralPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(accountLabel)
                            .add(activeAccountLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(deltaNeutralPaneLayout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(testButton))))
        );

        deltaNeutralMenuBar.setName("Pivotal Point Menu Bar"); // NOI18N

        profileMenu.setText("Portfolio");

        createNewPortfolioMenuItem.setText("Create New");
        createNewPortfolioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createNewPortfolioMenuItemActionPerformed(evt);
            }
        });
        profileMenu.add(createNewPortfolioMenuItem);

        editExistingMenuItem.setText("Edit Existing");
        editExistingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editExistingMenuItemActionPerformed(evt);
            }
        });
        profileMenu.add(editExistingMenuItem);

        deletePortfolioMenuItem.setText("Delete Existing");
        deletePortfolioMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePortfolioMenuItemActionPerformed(evt);
            }
        });
        profileMenu.add(deletePortfolioMenuItem);

        deltaNeutralMenuBar.add(profileMenu);

        displayMenu.setText("Positions");

        positionCreation.setText("Create New");
        positionCreation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionCreationActionPerformed(evt);
            }
        });
        displayMenu.add(positionCreation);

        runSlopeTraderMenuItem.setText("Run SlopeTrader");
        runSlopeTraderMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runSlopeTraderMenuItemActionPerformed(evt);
            }
        });
        displayMenu.add(runSlopeTraderMenuItem);

        deltaNeutralMenuBar.add(displayMenu);

        CreateMenu.setText("Display");

        displayStockQuoteMenuItem.setText("Stock Quote");
        displayStockQuoteMenuItem.setFocusable(true);
        displayStockQuoteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayStockQuoteMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayStockQuoteMenuItem);

        displayOptionQuoteMenuItem.setText("Option Quote");
        displayOptionQuoteMenuItem.setFocusable(true);
        displayOptionQuoteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayOptionQuoteMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayOptionQuoteMenuItem);

        displayOptionChainMenuItem.setText("Option Chain");
        displayOptionChainMenuItem.setFocusable(true);
        displayOptionChainMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayOptionChainMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayOptionChainMenuItem);

        displayStatsMenuItem.setText("Statistics");
        displayStatsMenuItem.setFocusable(true);
        displayStatsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayStatsMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayStatsMenuItem);

        displayChartMenuItem.setText("Chart");
        displayChartMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayChartMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayChartMenuItem);

        displayBalanceAndPositionsMenuItem.setText("Balance And Positions");
        displayBalanceAndPositionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayBalanceAndPositionsMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayBalanceAndPositionsMenuItem);

        displayImpliedVolatilityMenuItem.setText("Implied Volatility");
        displayImpliedVolatilityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayImpliedVolatilityMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayImpliedVolatilityMenuItem);

        displayManagedAccountInfoMenuItem.setText("Managed Account Information");
        displayManagedAccountInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayManagedAccountInfoMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayManagedAccountInfoMenuItem);

        createOptionFilesForExchangeMenuItem.setText("Process Option Chain Files");
        createOptionFilesForExchangeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createOptionFilesForExchangesMenuItem(evt);
            }
        });
        CreateMenu.add(createOptionFilesForExchangeMenuItem);

        displayOptionsWithUnusualVolumeMenuItem.setText("Unusual Option Volume");
        displayOptionsWithUnusualVolumeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayOptionsWithUnusualVolumeMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayOptionsWithUnusualVolumeMenuItem);

        filterForStocksMenuItem.setText("Filter Stocks With Criteria");
        filterForStocksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterForStocksMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(filterForStocksMenuItem);

        displayPrintableSummaryMenuItem.setText("Printable Summary");
        displayPrintableSummaryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayPrintableSummaryMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayPrintableSummaryMenuItem);

        displayCompanyInfoMenuItem.setText("Company Information");
        displayCompanyInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayCompanyInfoMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(displayCompanyInfoMenuItem);

        earningDatesMenuItem.setText("Earning Dates");
        earningDatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                earningDatesMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(earningDatesMenuItem);

        accountValuesMenuItem.setText("Account Values");
        accountValuesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountValuesMenuItemActionPerformed(evt);
            }
        });
        CreateMenu.add(accountValuesMenuItem);

        deltaNeutralMenuBar.add(CreateMenu);

        FilesMenu.setText("Configure");

        configGlobalMenuItem.setText("Global Configuration");
        configGlobalMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configGlobalMenuItemActionPerformed(evt);
            }
        });
        FilesMenu.add(configGlobalMenuItem);

        configPerPositionMenuItem.setText("Per Position Configuration");
        configPerPositionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configPerPositionMenuItemActionPerformed(evt);
            }
        });
        FilesMenu.add(configPerPositionMenuItem);

        configImpliedVolatilityMenuItem.setText("Implied Volatility");
        configImpliedVolatilityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configImpliedVolatilityMenuItemActionPerformed(evt);
            }
        });
        FilesMenu.add(configImpliedVolatilityMenuItem);

        deltaNeutralMenuBar.add(FilesMenu);

        ProcessFiles.setText("ProcessFiles");

        csvFileMenuItem.setText("CSV File");
        csvFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvFileMenuItemActionPerformed(evt);
            }
        });
        ProcessFiles.add(csvFileMenuItem);

        mergeFilesMenuItem.setText("MergeFiles");
        mergeFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeFilesMenuItemActionPerformed(evt);
            }
        });
        ProcessFiles.add(mergeFilesMenuItem);

        splitFilesMenuItem.setText("SplitFiles");
        splitFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitFilesMenuItemActionPerformed(evt);
            }
        });
        ProcessFiles.add(splitFilesMenuItem);

        playPolyMenuItem.setText("PlayPoly");
        playPolyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playPolyMenuItemActionPerformed(evt);
            }
        });
        ProcessFiles.add(playPolyMenuItem);

        deltaNeutralMenuBar.add(ProcessFiles);

        setJMenuBar(deltaNeutralMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(deltaNeutralPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, deltaNeutralPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void positionCreationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionCreationActionPerformed
        portfolio affectedPortfolio;
        boolean doit = false;
        if ((pivotPointPortfolio.positions != null ) && pivotPointPortfolio.positions.isThereATradeList() == true){
            if (commonGui.postConfirmationMsg("Portfolio: " + pivotPointPortfolio.portfolioFileName + " has positions." + "\n" + 
                "Create New Ones?"    
               ) == 0){
                doit = true;
            }else{
                commonGui.postInformationMsg("Canceled.");
            }
        }else{
            doit = true;
        }
        if (doit == true){
            pivotPointPortfolio.createPositions();
            if (pivotPointPortfolio.positions.isThereATradeList() == true){
                pivotPointPortfolio.setState(slopeDefs.oREADY);
            }else{
                commonGui.postInformationMsg("Portfolio Trade List size is zero? Not READY.");
            }           
            //fetch portfolio from table and update it.
            affectedPortfolio = fetchPortfolioData(pivotPointPortfolio.portfolioFileName);           
            //create an enclosing instance ..assign a trader to it...
            PortfolioTrader trader = pivotPointPortfolio.new PortfolioTrader();
            pivotPointPortfolio.portfolioTrader = trader; 
            //update the table now..
            affectedPortfolio = pivotPointPortfolio;
            updateActivePortfolioTable();
            
        }  
    }//GEN-LAST:event_positionCreationActionPerformed

    private void displayOptionQuoteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayOptionQuoteMenuItemActionPerformed
        JOptionPane.showMessageDialog(null, "displayOption Quote.....");
        
        
    }//GEN-LAST:event_displayOptionQuoteMenuItemActionPerformed

    private void displayStockQuoteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayStockQuoteMenuItemActionPerformed
          /*
         * Create and display the dialog
         */
        displayQuoteDialogForm dispQdialog;
    
        dispQdialog = new displayQuoteDialogForm(new javax.swing.JFrame(), true);
        dispQdialog.setVisible(true);
        
    }//GEN-LAST:event_displayStockQuoteMenuItemActionPerformed

    private void displayOptionChainMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayOptionChainMenuItemActionPerformed
        /* Create and display the dialog
         */
        displayOptionChainDialogForm optionChainDialog;
    
        optionChainDialog = new displayOptionChainDialogForm(new javax.swing.JFrame(), false);
        optionChainDialog.setActivePortfolio(actPositions);
        optionChainDialog.setVisible(true);      
    }//GEN-LAST:event_displayOptionChainMenuItemActionPerformed

    private void displayStatsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayStatsMenuItemActionPerformed
        positionStatistics actStats;

        actStats = new positionStatistics();
        actStats.statReadFromDisk(actPositions.getPositionFileName()/*"tdsave1test"*/, false /* no prompt */);

        actStats.displayStatistics();

    }//GEN-LAST:event_displayStatsMenuItemActionPerformed

    private void displayChartMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayChartMenuItemActionPerformed
        positionChart actChart;
        actChart = new positionChart(actPositions);
        actChart.dispChart();
    }//GEN-LAST:event_displayChartMenuItemActionPerformed

    private void displayBalanceAndPositionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayBalanceAndPositionsMenuItemActionPerformed
        accountInfoForm accountInfo = new accountInfoForm(actPositions);
        accountInfo.setActivePortfolio(actPositions);
        accountInfo.setVisible(true);
    }//GEN-LAST:event_displayBalanceAndPositionsMenuItemActionPerformed

    private void displayImpliedVolatilityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayImpliedVolatilityMenuItemActionPerformed
    //    volatilityAveragingIn actIv;
    //    actIv = new volatilityAveragingIn(actPositions);
        volatilityMonitor defvm;
        defvm = actPositions.getDefVm();
         defvm.dispImpliedVolatility();
    }//GEN-LAST:event_displayImpliedVolatilityMenuItemActionPerformed

    private void displayManagedAccountInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayManagedAccountInfoMenuItemActionPerformed
        
        displayManagedAccountsDialogForm managedAccounts;
    
        managedAccounts = new displayManagedAccountsDialogForm(new javax.swing.JFrame(), true, actPositions);
        managedAccounts.setVisible(true);    
    }//GEN-LAST:event_displayManagedAccountInfoMenuItemActionPerformed

    private void displayPrintableSummaryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayPrintableSummaryMenuItemActionPerformed
        
    }//GEN-LAST:event_displayPrintableSummaryMenuItemActionPerformed

    private void configGlobalMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configGlobalMenuItemActionPerformed
        actPosConfig = new positionConfig(false /* init */);
        
        /*
        globalConfigDialogForm globalConfig = new globalConfigDialogForm(new javax.swing.JFrame(), true);
        globalConfig.setVisible(true);
        * 
        */
    }//GEN-LAST:event_configGlobalMenuItemActionPerformed

    private void configPerPositionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configPerPositionMenuItemActionPerformed
        perPositionConfig pgl = new perPositionConfig(actPositions);
    }//GEN-LAST:event_configPerPositionMenuItemActionPerformed

    private void configImpliedVolatilityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configImpliedVolatilityMenuItemActionPerformed
        volatilityAveragingIn actIv;
        actIv = new volatilityAveragingIn(actPositions);
    }//GEN-LAST:event_configImpliedVolatilityMenuItemActionPerformed

    private void createNewPortfolioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createNewPortfolioMenuItemActionPerformed

        pivotPointPortfolio = new portfolio();
        pivotPointPortfolio.setState(slopeDefs.oINIT);
        portfolioCreationDialogForm portfolioCreator;
        portfolioCreator = new portfolioCreationDialogForm(new javax.swing.JFrame(), true, pivotPointPortfolio);
        portfolioCreator.setVisible(true);
        if (portfolioCreator.getUserCancelled() == false){
            portfolioListChanged = true;
            addToActivePortfolioTbl(pivotPointPortfolio);
        }else{
            commonGui.postInformationMsg("Create Portfolio Cancelled.");
        }
        
        
    }//GEN-LAST:event_createNewPortfolioMenuItemActionPerformed

    private void deletePortfolioMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePortfolioMenuItemActionPerformed
        int del ;
        if ((del = commonGui.postConfirmationMsg("Delete Portfolio: " + pivotPointActivePortfolio + " ?")) == 0) {
            pivotPointPortfolio.deleteFile();
            pivotPointPortfolio.positions.deleteTradeListFile();
            commonGui.postInformationMsg(pivotPointActivePortfolio + " and trade list was deleted.");
            portfolioListChanged = true;
            removeActivePortfolioTbl(pivotPointActivePortfolio);
            updateActivePortfolioTable();
        }else{
            commonGui.postInformationMsg(pivotPointActivePortfolio + " not deleted.");
        }
        
    }//GEN-LAST:event_deletePortfolioMenuItemActionPerformed

    private void activePortfolioComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activePortfolioComboBoxActionPerformed
        
        
        if (portfolioListChanged == true) {
            portfolioListChanged = false;
            activePortfolioComboBox.removeAllItems();
            portfolioNameList = getListOfPortfolioFilenames(homeDirectory);
            for (int i = 0; i < portfolioNameList.length; i++) {
                activePortfolioComboBox.addItem(portfolioNameList[i]);
            }
        }
        /* must init these because we are about to read in from disk and there could be
           old values in these..
         */
        pivotPointPortfolio = new portfolio();       
        pivotPointActivePortfolio = String.valueOf(activePortfolioComboBox.getSelectedItem());
        pivotPointPortfolio.setPortfolioFilename(pivotPointActivePortfolio);
        pivotPointPortfolio.rdFromFile();
        pivotPointPortfolio.positions.setPortolioName(pivotPointPortfolio.portfolioFileName);
        pivotPointPortfolio.positions.setPathnamePrefix(pivotPointPortfolio.portfolioFileNamePrefix);
        pivotPointPortfolio.setNumOfSharesToTrade(pivotPointPortfolio.numOfSharesToTrade);
        pivotPointPortfolio.setPositionBias(pivotPointPortfolio.positionBias);
        pivotPointPortfolio.setVersion(pivotPointPortfolio.version);
        if (pivotPointPortfolio.getState().equals(slopeDefs.oREADY)){
            pivotPointPortfolio.positions.rdTradeListFromFile();
            pivotPointPortfolio.positions.setUserCriteria(pivotPointPortfolio.userCriteria);
        }
        activeAccountLabel.setText(pivotPointPortfolio.accountNumber);
        commonGui.postInformationMsg(   "\n Acc#: " + pivotPointPortfolio.accountNumber + 
                                        "\n AvailFunds: " + pivotPointPortfolio.availFunds +
                                        "\n PercPerPos: " + pivotPointPortfolio.percentPerPosition + 
                                        "\n PercLong: " + pivotPointPortfolio.percentLong +
                                        "\n PercShort: " + pivotPointPortfolio.percentShort +
                                        "\n PosBias: " + slopeDefs.getPositionBiasStr(pivotPointPortfolio.positionBias) +
                                        "\n AccAlias: " + pivotPointPortfolio.getAlias() + 
                                        "\n FileName: " + pivotPointPortfolio.getPortfolioFilename()
        ); 
        //addToActivePortfolioTbl(pivotPointPortfolio);
    }//GEN-LAST:event_activePortfolioComboBoxActionPerformed

    private void runTraderStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runTraderStartButtonActionPerformed
        
        portfolio runme;
        //pivotPointPortfolio.startTrading();
        runme = fetchPortfolioData(pivotPointActivePortfolio);
        if(runme.getState().equals(slopeDefs.oREADY)){
            runme.portfolioTrader.initTrader();
            runme.portfolioTrader.startTrader();
        }else{
            commonGui.postInformationMsg("Connot Trade this portfolio, not in READY state! (positions created?)");
        } 
       
    }//GEN-LAST:event_runTraderStartButtonActionPerformed

    private void displayOptionsWithUnusualVolumeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayOptionsWithUnusualVolumeMenuItemActionPerformed
        // TODO add your handling code here:
         displayOptionsWithUnusualVolumeDialogForm optionsWithHighVol;
    
        optionsWithHighVol = new displayOptionsWithUnusualVolumeDialogForm(new javax.swing.JFrame(), true);
        
        optionsWithHighVol.setVisible(true);      
    }//GEN-LAST:event_displayOptionsWithUnusualVolumeMenuItemActionPerformed

    private void createOptionFilesForExchangesMenuItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createOptionFilesForExchangesMenuItem
        // TODO add your handling code here:
        createOptionFilesForExchangesDialogForm optionFiles;
    
        optionFiles = new createOptionFilesForExchangesDialogForm(new javax.swing.JFrame(), true);
        
        optionFiles.setVisible(true);  
    }//GEN-LAST:event_createOptionFilesForExchangesMenuItem

    private void filterForStocksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterForStocksMenuItemActionPerformed
        // TODO add your handling code here:
        displayStocksWthFilter stocksFilter;
        stocksFilter = new displayStocksWthFilter(new javax.swing.JFrame(), false);
        stocksFilter.setVisible(true);
        for (int z = 0; z < 10; z++){
            System.out.println("z= " + z);
            myUtils.delay(1000);
        }
    }//GEN-LAST:event_filterForStocksMenuItemActionPerformed

    private void runSlopeTraderMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runSlopeTraderMenuItemActionPerformed
        // TODO add your handling code here:
        
         portfolio runme;
        //pivotPointPortfolio.startTrading();
        runme = fetchPortfolioData(pivotPointActivePortfolio);
        if(runme.getState().equals(slopeDefs.oREADY)){
            runme.portfolioTrader.initTrader();
            runme.portfolioTrader.startTrader();
        }else{
            commonGui.postInformationMsg("Connot Trade this portfolio, not in READY state! (positions created?)");
        } 
    }//GEN-LAST:event_runSlopeTraderMenuItemActionPerformed

    private void showPortfolioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPortfolioButtonActionPerformed
        // TODO add your handling code here:
        
         pivotPointPortfolio.rdFromFile();
         commonGui.postInformationMsg(   "\n Acc#: " + pivotPointPortfolio.accountNumber + 
                                        "\n AvailFunds: " + pivotPointPortfolio.availFunds +
                                        "\n PercPerPos: " + pivotPointPortfolio.percentPerPosition + 
                                        "\n PercLong: " + pivotPointPortfolio.percentLong +
                                        "\n PercShort: " + pivotPointPortfolio.percentShort +
                                        "\n AccAlias: " + pivotPointPortfolio.getAlias() + 
                                        "\n FileName: " + pivotPointPortfolio.getPortfolioFilename() +
                                        "\n State: " + pivotPointPortfolio.getState() + 
                                        "\n Criteria: " + pivotPointPortfolio.userCriteria + 
                                        "\n NumOfSharesToTrade: " + pivotPointPortfolio.numOfSharesToTrade +
                                        "\n PositionBias: " + slopeDefs.getPositionBiasStr(pivotPointPortfolio.positionBias) + 
                                        "\n version: " + pivotPointPortfolio.version
                                    
        );
    }//GEN-LAST:event_showPortfolioButtonActionPerformed

    private void editExistingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editExistingMenuItemActionPerformed
        // TODO add your handling code here:
        portfolioCreationDialogForm portfolioCreator;
        portfolioCreator = new portfolioCreationDialogForm(new javax.swing.JFrame(), true, fetchPortfolioData(pivotPointActivePortfolio));
        portfolioCreator.setVisible(true);
    }//GEN-LAST:event_editExistingMenuItemActionPerformed

    private void displayCompanyInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayCompanyInfoMenuItemActionPerformed
        // TODO add your handling code here:
        DisplayCompanyInfoDialogForm displayCompanyInfo = new DisplayCompanyInfoDialogForm(new javax.swing.JFrame(), false);
        displayCompanyInfo.setVisible(true);
    }//GEN-LAST:event_displayCompanyInfoMenuItemActionPerformed

    private void earningDatesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_earningDatesMenuItemActionPerformed
        // TODO add your handling code here:
        EarningDatesDialogForm earningDates = new EarningDatesDialogForm(new javax.swing.JFrame(), true);
        earningDates.setVisible(true);
    }//GEN-LAST:event_earningDatesMenuItemActionPerformed

    private void accountValuesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountValuesMenuItemActionPerformed
        // TODO add your handling code here:
        AccountValuesDialogForm accountValues = new AccountValuesDialogForm(new javax.swing.JFrame(), true);
        accountValues.setVisible(true);
    }//GEN-LAST:event_accountValuesMenuItemActionPerformed

    private void csvFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvFileMenuItemActionPerformed
        // TODO add your handling code here:
        ProcessCsvFiles processCsvFiles = new ProcessCsvFiles(new javax.swing.JFrame(), true);
        processCsvFiles.setVisible(true);
    }//GEN-LAST:event_csvFileMenuItemActionPerformed

    private void mergeFilesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeFilesMenuItemActionPerformed
        // TODO add your handling code here:
        MergeSymbolFiles mergeSymbolFiles = new MergeSymbolFiles(new javax.swing.JFrame(), true);
        mergeSymbolFiles.setVisible(true);
    }//GEN-LAST:event_mergeFilesMenuItemActionPerformed

    private void splitFilesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitFilesMenuItemActionPerformed
        // TODO add your handling code here:
        SplitSymbolFile splitSymbolFile = new SplitSymbolFile(new javax.swing.JFrame(), true);
        splitSymbolFile.setVisible(true);
    }//GEN-LAST:event_splitFilesMenuItemActionPerformed

    private void playPolyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playPolyMenuItemActionPerformed
        // TODO add your handling code here:
        PolyPlay playPoly = new PolyPlay(new javax.swing.JFrame(), true);
    }//GEN-LAST:event_playPolyMenuItemActionPerformed

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        // TODO add your handling code here:
		int a = 0;
		a++;
        NewTraderTest newTraderTest = new NewTraderTest(new javax.swing.JFrame(), true);
        newTraderTest.setVisible(true);
    }//GEN-LAST:event_testButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu CreateMenu;
    private javax.swing.JMenu FilesMenu;
    private javax.swing.JMenu ProcessFiles;
    private javax.swing.JLabel accountLabel;
    private javax.swing.JMenuItem accountValuesMenuItem;
    private javax.swing.JLabel activeAccountLabel;
    private javax.swing.JComboBox activePortfolioComboBox;
    private javax.swing.JLabel activePortfolioLabel;
    private javax.swing.JTable activePortfoliosTable;
    private javax.swing.JMenuItem configGlobalMenuItem;
    private javax.swing.JMenuItem configImpliedVolatilityMenuItem;
    private javax.swing.JMenuItem configPerPositionMenuItem;
    private javax.swing.JMenuItem createNewPortfolioMenuItem;
    private javax.swing.JMenuItem createOptionFilesForExchangeMenuItem;
    private javax.swing.JMenuItem csvFileMenuItem;
    private javax.swing.JMenuItem deletePortfolioMenuItem;
    private javax.swing.JMenuBar deltaNeutralMenuBar;
    private javax.swing.JPanel deltaNeutralPane;
    private javax.swing.JMenuItem displayBalanceAndPositionsMenuItem;
    private javax.swing.JMenuItem displayChartMenuItem;
    private javax.swing.JMenuItem displayCompanyInfoMenuItem;
    private javax.swing.JMenuItem displayImpliedVolatilityMenuItem;
    private javax.swing.JMenuItem displayManagedAccountInfoMenuItem;
    private javax.swing.JMenu displayMenu;
    private javax.swing.JMenuItem displayOptionChainMenuItem;
    private javax.swing.JMenuItem displayOptionQuoteMenuItem;
    private javax.swing.JMenuItem displayOptionsWithUnusualVolumeMenuItem;
    private javax.swing.JMenuItem displayPrintableSummaryMenuItem;
    private javax.swing.JMenuItem displayStatsMenuItem;
    private javax.swing.JMenuItem displayStockQuoteMenuItem;
    private javax.swing.JMenuItem earningDatesMenuItem;
    private javax.swing.JMenuItem editExistingMenuItem;
    private javax.swing.JMenuItem filterForStocksMenuItem;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem mergeFilesMenuItem;
    private javax.swing.JMenuItem playPolyMenuItem;
    private javax.swing.JMenuItem positionCreation;
    private javax.swing.JMenu profileMenu;
    private javax.swing.JMenuItem runSlopeTraderMenuItem;
    private javax.swing.JButton runTraderStartButton;
    private javax.swing.JButton showPortfolioButton;
    private javax.swing.JMenuItem splitFilesMenuItem;
    private javax.swing.JButton testButton;
    // End of variables declaration//GEN-END:variables
}
