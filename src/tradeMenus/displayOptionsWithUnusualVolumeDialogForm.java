/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tradeMenus;

import ibTradeApi.*;
import ibTradeApi.ibApi;
import ibTradeApi.ibApi.quoteInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class displayOptionsWithUnusualVolumeDialogForm extends javax.swing.JDialog {
    final String NASQ1 = "NasdaqQ1";
    final String NASQ2 = "NasdaqQ2";
    final String NASQ3 = "NasdaqQ3";
    final String NASQ4 = "NasdaqQ4";
    final String NYSEQ1 = "NyseQ1";
    final String NYSEQ2 = "NyseQ2";
    final String NYSEQ3 = "NyseQ3";
    final String NYSEQ4 = "NyseQ4";
    final String NYSEQ5 = "NyseQ5";
    final String NYSEQ6 = "NyseQ6";
    final String NYSEQ7 = "NyseQ7";
    final String NYSEQ8 = "NyseQ8";
    final String WATCH1 = "WATCH1";
    final String SNP1 = "SnP1";
    final String SNP2 = "SnP2";
    final String DOW = "Dow";
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    String[] exchanges = {  NASQ1, NASQ2, NASQ3, NASQ4, 
                            NYSEQ1, NYSEQ2, NYSEQ3, NYSEQ4 ,NYSEQ5, NYSEQ6, NYSEQ7, NYSEQ8, 
                            WATCH1, SNP1, SNP2, DOW};
    // User Selected Exchange index in both string and int
    String selectedExchangeStr = exchanges[0];
    //use to aviod reading twice....
    boolean alreadyRead = false;
    int selectedExchangeInt = 0;
    // User slected volume number to look for i.e. : 1000 means if 1000 or > store option info
    int selectedVolumeInt = 0;
    boolean selectedAndFuntion = false;
    // User slected volume number to look for as a percent of open interest. This value is in % (20 = 20%)
    int selectedPercentOfOpenInterest = 0;
    // goSearch says user entered correct criteria and we are ready to do search..
    boolean goSearch = false;
    private String actContractFile = null;
    //this structure will hold all data for chosen exchange..
    readTickerFile exchangeData = null;
    private ibApi actIbApi = ibApi.getActApi();
    private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
    private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
    performSearchTask searchTask = null;
    getOptionData optionDat;
    String lastTickerProcessed = null;
    private final int oNumber   = 0;
    private final int oCONTRACT = oNumber + 1;
    private final int oTYPE     = oCONTRACT + 1;
    private final int oSTRIKE   = oTYPE + 1;
    private final int oBID      = oSTRIKE + 1;
    private final int oASK      = oBID + 1;
    private final int oLAST     = oASK + 1;
    private final int oDELTA    = oLAST + 1;
    private final int oVOLUME   = oDELTA + 1;
    private final int oOI       = oVOLUME + 1;
    private final int oAmount   = oOI + 1;
    private boolean createNew = true;
    /**
     * Creates new form displayOptionsWithUnusualVolumeDialogForm
     */
    public displayOptionsWithUnusualVolumeDialogForm(java.awt.Frame parent, boolean modal) {
        
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
           
        if (indexComboBox.getItemCount() > 0) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString();
            selectedExchangeInt = indexComboBox.getSelectedIndex();
            
            if (selectedExchangeStr.equals(NASQ1)) {
                actContractFile = homeDirectory + "nasContracts1.txt";
            }else if (selectedExchangeStr.equals(NASQ2)) {
                actContractFile = homeDirectory + "nasContracts2.txt";
            }else if (selectedExchangeStr.equals(NASQ3)) {
                actContractFile = homeDirectory + "nasContracts3.txt";
            }else if (selectedExchangeStr.equals(NASQ4)) {
                actContractFile = homeDirectory + "nasContracts4.txt";
            }else if (selectedExchangeStr.equals(NYSEQ1)) {
                actContractFile = homeDirectory + "nyseContracts1.txt";
            }else if (selectedExchangeStr.equals(NYSEQ2)) {
                actContractFile = homeDirectory + "nyseContracts2.txt";
            }else if (selectedExchangeStr.equals(NYSEQ3)) {
                actContractFile = homeDirectory + "nyseContracts3.txt";
            }else if (selectedExchangeStr.equals(NYSEQ4)) {
                actContractFile = homeDirectory + "nyseContracts4.txt";
            }else if (selectedExchangeStr.equals(NYSEQ5)) {
                actContractFile = homeDirectory + "nyseContracts5.txt";
            }else if (selectedExchangeStr.equals(NYSEQ6)) {
                actContractFile = homeDirectory + "nyseContracts6.txt";
            }else if (selectedExchangeStr.equals(NYSEQ7)) {
                actContractFile = homeDirectory + "nyseContracts7.txt";
            }else if (selectedExchangeStr.equals(NYSEQ8)) {
                actContractFile = homeDirectory + "nyseContracts8.txt";
            }else if (selectedExchangeStr.equals(WATCH1)) {
                actContractFile = homeDirectory + "watch1Contracts.txt";
            }else if (selectedExchangeStr.equals(SNP1)) {
                actContractFile = homeDirectory + "snp1Contracts.txt";
            }else if (selectedExchangeStr.equals(SNP2)) {
                actContractFile = homeDirectory + "spn2Contracts.txt";
            }else if (selectedExchangeStr.equals(DOW)) {
                actContractFile = homeDirectory + "dowContracts.txt";
            }else {
                actContractFile = null;
            }

        };
            /*    for (int idx = 0; idx < exchanges.length; idx++) {
             indexComboBox.addItem(exchanges[idx]);           
             }  
             */
        
        
        
    }
    class optionData {
        boolean quoteAble;
        String stockTicker;
        String optionTicker;
        ibApi.quoteInfo chainInfo = new ibApi.quoteInfo();
        optionData() {
            quoteAble = false;
            stockTicker = null;
            optionTicker = null;
        }
   }
    public class readTickerFile {
        
        final int MAX_TICKERS = 4000;
        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        String fileName = homeDirectory;
        String tmpStr = null;      
        String actExchange = null;
        int numberOfTickers = 0;
        optionData tickerDataHere[];
        
        public readTickerFile(String tickerFileName, int numOfTickers) throws IOException {
            boolean split = false;
            int tickerNumber = numOfTickers - 1;
            int actTicker = 0;
            
            if (numOfTickers > MAX_TICKERS) {
                System.out.println("numOfTickers too big!!!!!");
                return;
            }
            numberOfTickers = numOfTickers;
            optionData tickerData[] = new optionData[numOfTickers];
            tickerDataHere = tickerData;
            if (fileName.equals(null)) {
                return;
            }
            
            for(int idx = 0; idx < numOfTickers; idx++){
                tickerData[idx] = new optionData();
            }
            try {
                fis = new FileInputStream(tickerFileName);
                dis = new DataInputStream(fis);
                bir = new BufferedReader(new InputStreamReader(fis));
                
                //read first line just is "Symbol" string....
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    // get rid of last char \
                    tmpStr = tmpStr.substring(0,tmpStr.length()-1);
                } else {
                    split = true;
                }
                tickerNumber--;
                System.out.println("tmpStr = "+tmpStr);
                while ((tickerNumber != 0) && !split) {
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        // get rid of last char \
                        tmpStr = tmpStr.substring(0,tmpStr.length()-1);
                        System.out.println("tmpStr = "+tmpStr);
                        //look for ^ in string and mark as don't quote..
                        if(tmpStr.indexOf('^') > 0){
                            // found ^ so mark as no quote..
                            tickerData[actTicker].quoteAble = false;
                            System.out.println("^ found...don't quote it...");
                        }else{
                            tickerData[actTicker].quoteAble = true;
                        }
                        tickerData[actTicker].stockTicker = tmpStr;
                        tickerNumber--;
                        actTicker++;
                    } else {
                        split = true;
                    }
                
                }
                bir.close();
            } catch (Exception e) {
                System.out.println("error reading text from: " + tickerFileName + "(" + e + ").");
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

        jPasswordField1 = new javax.swing.JPasswordField();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        indexComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        volumeTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        searchButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        tickersLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        optionDataTable = new javax.swing.JTable();
        pauseResumeButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        AndCheckBox = new javax.swing.JCheckBox();

        jPasswordField1.setText("jPasswordField1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("optionVolumeLocatorDialog"); // NOI18N

        jLabel1.setText("Option High Volume Locater");

        indexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NasdaqQ1", "NasdaqQ2", "NasdaqQ3", "NasdaqQ4", "NyseQ1", "NyseQ2", "NyseQ3", "NyseQ4", "NyseQ5", "NyseQ6", "NyseQ7", "NyseQ8", "WATCH1", "SnP1", "SnP2", "Dow" }));
        indexComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Select Search Index");

        volumeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                volumeTextFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Search For Volume > (i.e 1000)");

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        optionDataTable.setModel(new javax.swing.table.DefaultTableModel(
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
                "#", "Contract", "Call/PUT", "Strike", "Bid", "Ask", "Last", "Delta", "Volume", "Open Int", "Amount"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(optionDataTable);
        if (optionDataTable.getColumnModel().getColumnCount() > 0) {
            optionDataTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        }

        pauseResumeButton.setText("PauseResume");
        pauseResumeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseResumeActionPerformed(evt);
            }
        });

        jLabel5.setText("volume is > Open Interest");

        AndCheckBox.setText("AND");
        AndCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AndActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(408, Short.MAX_VALUE)
                        .addComponent(tickersLabel)
                        .addGap(143, 143, 143))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(135, 135, 135)
                                .addComponent(closeButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(138, 138, 138)
                                .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(58, 58, 58)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(220, 220, 220)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jLabel2))
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(volumeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addComponent(AndCheckBox)
                                .addGap(28, 28, 28)
                                .addComponent(jLabel5)))))
                .addGap(73, 73, 73))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(149, 149, 149)
                .addComponent(searchButton)
                .addGap(33, 33, 33)
                .addComponent(pauseResumeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(10, 10, 10)
                .addComponent(indexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel3)
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(volumeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(AndCheckBox))
                .addGap(76, 76, 76)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchButton)
                    .addComponent(pauseResumeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tickersLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeButton))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void indexComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexComboBoxActionPerformed
        // TODO add your handling code here:
        /* get the user selected exhange to later search in */
        if (indexComboBox.getItemCount() > 0 ) {
            selectedExchangeStr = indexComboBox.getSelectedItem().toString(); 
            selectedExchangeInt = indexComboBox.getSelectedIndex();
        }; 
        System.out.println("selected index is: "+selectedExchangeStr);
        System.out.println("selected index idx is : "+selectedExchangeInt);
        
            if (selectedExchangeStr.equals(NASQ1)) {
                actContractFile = homeDirectory + "nasContracts1.txt";
            }else if (selectedExchangeStr.equals(NASQ2)) {
                actContractFile = homeDirectory + "nasContracts2.txt";
            }else if (selectedExchangeStr.equals(NASQ3)) {
                actContractFile = homeDirectory + "nasContracts3.txt";
            }else if (selectedExchangeStr.equals(NASQ4)) {
                actContractFile = homeDirectory + "nasContracts4.txt";
            }else if (selectedExchangeStr.equals(NYSEQ1)) {
                actContractFile = homeDirectory + "nyseContracts1.txt";
            }else if (selectedExchangeStr.equals(NYSEQ2)) {
                actContractFile = homeDirectory + "nyseContracts2.txt";
            }else if (selectedExchangeStr.equals(NYSEQ3)) {
                actContractFile = homeDirectory + "nyseContracts3.txt";
            }else if (selectedExchangeStr.equals(NYSEQ4)) {
                actContractFile = homeDirectory + "nyseContracts4.txt";
            }else if (selectedExchangeStr.equals(NYSEQ5)) {
                actContractFile = homeDirectory + "nyseContracts5.txt";
            }else if (selectedExchangeStr.equals(NYSEQ6)) {
                actContractFile = homeDirectory + "nyseContracts6.txt";
            }else if (selectedExchangeStr.equals(NYSEQ7)) {
                actContractFile = homeDirectory + "nyseContracts7.txt";
            }else if (selectedExchangeStr.equals(NYSEQ8)) {
                actContractFile = homeDirectory + "nyseContracts8.txt";
            }else if (selectedExchangeStr.equals(WATCH1)) {
                actContractFile = homeDirectory + "watch1Contracts.txt";
            }else if (selectedExchangeStr.equals(SNP1)) {
                actContractFile = homeDirectory + "snp1Contracts.txt";
            }else if (selectedExchangeStr.equals(SNP2)) {
                actContractFile = homeDirectory + "snp2Contracts.txt";
            }else if (selectedExchangeStr.equals(DOW)) {
                actContractFile = homeDirectory + "dowContracts.txt";
            }else {
                actContractFile = null;
            }

        
    }//GEN-LAST:event_indexComboBoxActionPerformed

    private void volumeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_volumeTextFieldActionPerformed
        // TODO add your handling code here:
        // get user input for volume to search for ( equeal to or greater than )..
        selectedVolumeInt = Integer.valueOf(volumeTextField.getText());
        
    }//GEN-LAST:event_volumeTextFieldActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:  
        
        if(searchTask != null) {
            System.out.println("Search running...stopping it..");
            
            searchTask.running = false;
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

        
    public class getOptionData {
        private final int CHAIN_ARRAY_PCSZ = 2;
        private final int CHAIN_ARRAY_ENTRIES = 800;
        private ibApi.quoteInfo[][] optionChainArray = new ibApi.quoteInfo[CHAIN_ARRAY_PCSZ][CHAIN_ARRAY_ENTRIES];
        private ibApi actIbApi = ibApi.getActApi();
        private ibApi.OptionChain actChain = actIbApi.getActOptionChain();
        private ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
        private ibApi.quoteInfo cInfo = new ibApi.quoteInfo();
        String tmpStrike = null;
        private final int CALL = 0;
        private final int PUT = 1;
        private int optionChainCallArraySz = 0;
        private int optionChainPutArraySz = 0;
        boolean noErrors = true;
        
        public void justUpdate(String ticker) {
            noErrors = fillOptionData(ticker, true);    
        }
        
        private boolean fillOptionData(String ticker, boolean justUpdate) {
            //String ticker = "aapl";
            int chainSize = 0;
            int chainNum = 0;
            int pcIdx = 0;
            boolean allGood = true;
            
            //initialize the option Chain array first..
            for (pcIdx = 0; pcIdx < CHAIN_ARRAY_PCSZ; pcIdx++) {
                for (int idx = 0; idx < CHAIN_ARRAY_ENTRIES; idx++) {
                    optionChainArray[pcIdx][idx] = null;
                }
            }

            if (justUpdate == false) {
                actChain.startNewChain();
                // we want the streams turned on too..
                actChain.turnWithStreamsOn(true);
                allGood = actChain.getOptionChain(ticker);
                if (allGood == false){
                    //System.out.println("error geting chain.");
                    return allGood;
                }
                //userMonth = null;
            
            }
                       

            actChain.chainIdxReset();
            chainSize = actChain.chainLeft();
            /* we should get call then put with the same strike as pairs. 
             * if the strike price changes we increment the store idx (pcIdx).
             * That's why we store the tmpStrike.
             */
            for (chainNum = 0, pcIdx = 0; chainNum <= chainSize;) {
                cInfo = actChain.chainGetNextOrdered(chainNum++);
                if ((cInfo != null) && (cInfo.enStreaming == true)) {

                    /*
                     * check if we should move to next pair via bump pcidx
                     * take care of initial entry where tmpStrike == null.
                     */
                    if (tmpStrike == null) {
                        tmpStrike = String.valueOf(cInfo.strikePrice);
                    } else {
                        if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                            /*
                             * strike changed so bump to next pair
                             */
                            pcIdx++;
                            tmpStrike = String.valueOf(cInfo.strikePrice);
                        }
                    }
                    qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                     * option
                     */);

                    if (cInfo.cpType.equals("C")) {
                        /*
                         * was the call, so save then read for PUT
                         */
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        optionChainArray[CALL][pcIdx] = qInfo;

                        cInfo = actChain.chainGetNextOrdered(chainNum);
                        if ((cInfo != null) && (cInfo.enStreaming == true)) {
                            /* check if we should move to next pair via bump pcidx */
                            if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                                /* strike changed so bump to next pair */
                                pcIdx++;
                                tmpStrike = String.valueOf(cInfo.strikePrice);
                            }
                            qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                             * option
                             */);
                            qInfo.optionSymbol = cInfo.optionSymbol;
                            qInfo.underlying = cInfo.underlying;
                            optionChainArray[PUT][pcIdx] = qInfo;
                        } else {
                        }

                    } else {
                        /*
                         * must be PUT, so save then read for CALL
                         */
                        qInfo.optionSymbol = cInfo.optionSymbol;
                        qInfo.underlying = cInfo.underlying;
                        optionChainArray[PUT][pcIdx] = qInfo;

                        cInfo = actChain.chainGetNextOrdered(chainNum);
                        if ((cInfo != null) && (cInfo.enStreaming == true)) {
                            /* check if we should move to next pair via bump pcidx */
                            if (!String.valueOf(cInfo.strikePrice).equals(tmpStrike)) {
                                /* strike changed so bump to next pair */
                                pcIdx++;
                                tmpStrike = String.valueOf(cInfo.strikePrice);
                            }
                            qInfo = actChain.getQuote(cInfo.optionSymbol, true /*
                             * option
                             */);
                            qInfo.optionSymbol = cInfo.optionSymbol;
                            qInfo.underlying = cInfo.underlying;
                            optionChainArray[CALL][pcIdx] = qInfo;
                        } else {
                        }

                    }
                    chainNum++;
                }

            }
            optionChainCallArraySz = pcIdx;
            optionChainPutArraySz = pcIdx;
            return allGood;

        }
        
        // this will use exchangeData to get option quote data from IB IPA...
        public getOptionData(String ticker){
            noErrors = fillOptionData(ticker, false);
            
        }
    }
    
    private void readTickersFromFile(String exchange) {
        String tickerFileName = homeDirectory;
        int numOfTickers = 0;
        // first see if we already read this file...don't do it twice..
        if (exchange.equals(selectedExchangeStr) && (alreadyRead == true)){
            System.out.println("already read this file...don't do it again.");
            return; 
        }
        if (exchange.equals(NASQ1)) {
            tickerFileName += "nasdaqTickers1.txt";
            actContractFile = homeDirectory + "nasContracts1.txt";
        } else if (exchange.equals(NASQ2)) {
            tickerFileName += "nasdaqTickers2.txt";
            actContractFile = homeDirectory + "nasContracts2.txt";
        } else if (exchange.equals(NASQ3)) {
            tickerFileName += "nasdaqTickers3.txt";
            actContractFile = homeDirectory + "nasContracts3.txt";
        } else if (exchange.equals(NASQ4)) {
            tickerFileName += "nasdaqTickers4.txt";
            actContractFile = homeDirectory + "nasContracts4.txt";
        }else if (exchange.equals(NYSEQ1)) {
            tickerFileName += "nyseTickers1.txt";
            actContractFile = homeDirectory + "nyseContracts1.txt";
        }else if (exchange.equals(NYSEQ2)) {
            tickerFileName += "nyseTickers2.txt";
            actContractFile = homeDirectory + "nyseContracts2.txt";
        }else if (exchange.equals(NYSEQ3)) {
            tickerFileName += "nyseTickers3.txt";
            actContractFile = homeDirectory + "nyseContracts3.txt";
        }else if (exchange.equals(NYSEQ4)) {
            tickerFileName += "nyseTickers4.txt";
            actContractFile = homeDirectory + "nyseContracts4.txt";
        }else if (exchange.equals(NYSEQ5)) {
            tickerFileName += "nyseTickers5.txt";
            actContractFile = homeDirectory + "nyseContracts5.txt";
        }else if (exchange.equals(NYSEQ6)) {
            tickerFileName += "nyseTickers6.txt";
            actContractFile = homeDirectory + "nyseContracts6.txt";
        }else if (exchange.equals(NYSEQ7)) {
            tickerFileName += "nyseTickers7.txt";
            actContractFile = homeDirectory + "nyseContracts7.txt";
        }else if (exchange.equals(NYSEQ8)) {
            tickerFileName += "nyseTickers8.txt";
            actContractFile = homeDirectory + "nyseContracts8.txt";
        }else if (exchange.equals(WATCH1)) {
            tickerFileName += "watch1Tickers.txt";
            actContractFile = homeDirectory + "watch1Contracts.txt";
        }else if (exchange.equals(SNP1)) {
            tickerFileName += "snp1Tickers.txt";
            actContractFile = homeDirectory + "snp1Contracts.txt";
        }else if (exchange.equals(SNP2)) {
            tickerFileName += "snp2Tickers.txt";
            actContractFile = homeDirectory + "snp2Contracts.txt";
        }else if (exchange.equals(DOW)) {
            tickerFileName += "dowTickers.txt";
            actContractFile = homeDirectory + "dowContracts.txt";
        }else {
            tickerFileName = null;
            return;
        }
        try {
            numOfTickers = myUtils.countLinesInFile(tickerFileName);
        } catch (IOException ex) {
            Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("num of ticker are: " + numOfTickers);
        
        try {
            exchangeData = new readTickerFile(tickerFileName, numOfTickers);
            alreadyRead = true;
        } catch (IOException ex) {
            Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            alreadyRead = false;
        }

    }
    
    private class ioRdTextFiles{
        String fname = homeDirectory;
        
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;
        
        
        void openRd(String fileName, boolean append) {
            fname = fileName;
            
            
            try {
                fis = new FileInputStream(fname);
                bir = new BufferedReader(new InputStreamReader(fis));
            } catch (Exception e) {
                System.out.println("error reading text from: " + fname + "(" + e + ").");
            }
        }
        void closeRd() {
            try {
                bir.close();
                
            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }
        ioRdTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openRd(fileName, append);
            
        }
        
        String read()  {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }
    
    public class ioWrTextFiles {

        String fname = homeDirectory;

        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;

        void openWr(String fileName, boolean append) {
            fname = fileName;

            try {
                //fos = new FileOutputStream(fileName);
                //dos = new DataOutputStream(fos);
                bow = new BufferedWriter(new FileWriter(fname, append));

                try {
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                } catch (Exception e) {
                    System.out.println("file does not exist " + fname);
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                    bir = new BufferedReader(new InputStreamReader(fis));

                }

            } catch (Exception e) {
                System.out.println("error writing text to: " + fileName + "(" + e + ").");
            }
        }

        void closeWr() {
            try {
                bow.close();

            } catch (Exception e) {
                System.out.println("error closing file: " + fname + "(" + e + ").");
            }
            fname = homeDirectory;
        }

        ioWrTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openWr(fileName, append);

        }

        boolean write(String str) {

            try {
                if (str != null) {
                    bow.write(str);
                    bow.newLine();
                    bow.flush();
                }
            } catch (Exception e) {
                System.out.println("error write to file: " + fname + "(" + e + ").");
            }
            return true;
        }

        String read(Boolean str) {
            String rdStr = null;
            try {
                rdStr = bir.readLine();
            } catch (IOException ex) {
                Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rdStr;
        }
    }
    
    private void clearOptionOptionDataTable() {
        int rowCount = optionDataTable.getRowCount();
        
        for (int il = 0; il < rowCount; il++){
            optionDataTable.getModel().setValueAt(0, il, oNumber);
            optionDataTable.getModel().setValueAt("", il, oCONTRACT);
            optionDataTable.getModel().setValueAt("", il, oTYPE);
            optionDataTable.getModel().setValueAt(0, il, oSTRIKE);
            optionDataTable.getModel().setValueAt(0, il, oBID);
            optionDataTable.getModel().setValueAt(0, il, oASK);
            optionDataTable.getModel().setValueAt(0, il, oLAST);
            optionDataTable.getModel().setValueAt(0, il, oDELTA);
            optionDataTable.getModel().setValueAt(0, il, oVOLUME);
            optionDataTable.getModel().setValueAt(0, il, oOI);
            optionDataTable.getModel().setValueAt("", il, oAmount);
            
        }
        
    }
    
    
    public class performSearchTask extends Thread {
        String tmpStr = "";
        final int STREAMSZ = 100;//150;
        String[] blkOfStr = new String[STREAMSZ];
        boolean running = false;
        boolean shutDown = false;
        int sz = 0;
        int optionTickCnt = 0;
        int tickCnt = 0;
        ioRdTextFiles fileToProcess;
        ioWrTextFiles saveToFile;
        String actTicker = null;
        
        private String findTickerToProcess(String searchTicker){
            String tStr = null;
            boolean foundIt = false;
            while(((tStr = fileToProcess.read()) != null) && (!foundIt)) {
                if (searchTicker.equals(tStr)) {
                    foundIt = true;
                }
                
            }
            if (foundIt == false){
                tStr = null;
            }
            return(tStr);
        }
        private void windDown() {
            shutDown = true;
        }
        public void startme(){
            running = true;
            this.start();
            
        }
        public performSearchTask(ioRdTextFiles rdFile) {
            running = false;
            shutDown = false;
            fileToProcess = rdFile;
            // call starme to start things up...
            //this.start();  
        }
        private class qverify {
            int updated;
            int updateCnt;
        }
        private boolean waitForOIBit(String sym) {
            boolean error = false;
            int timeOut = 10;
            while (((qInfo.respBits & ibApi.OPEN_INTEREST) == 0) && (timeOut > 0)) {
                myUtils.delay(100);
                qInfo = actChain.getQuote(sym, true /* option q*/);
                timeOut--;
            }
            if (timeOut == 0) {
                error = true;
            }else {
                error = false;
            }
            return(error);
        }
        public void run() {
           
            String optionTypeStr = null;
            int optionOI = 0;
            int tblLine = 0;
            qverify[] qus = new qverify[STREAMSZ];
            String todayDate = null;
            boolean displayIt = false;
            int timeOut = 10;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            todayDate = dateFormat.format(cal.getTime());
            String optionAmtStr = null;
            NumberFormat numformat = NumberFormat.getInstance();
            //myUtils.delay(100);
            //do blocks at at time because IB only gives you X number of streams. 
            // set up all streams, then get quotes for them, then release streams and repeat..
            this.saveToFile.write("\n** Criteria ** Volume: " + selectedVolumeInt + 
                                  " ** Percent Of OI: " + selectedPercentOfOpenInterest + 
                                  " ** Exchange: " + selectedExchangeStr + 
                                  " ** Date: " + todayDate + " ** \n"
            );
            /* check if we paused and want to resume..*/
            if (lastTickerProcessed != null) {
                /* this shoudl return with the file pointing to the next ticker to process 
                   in the case where we paused. So next line read should be the ticker to resume with.
                */
                actTicker = lastTickerProcessed;
                tmpStr = findTickerToProcess(lastTickerProcessed);
                if (tmpStr == null){
                    System.out.println("we shouldn't have gotten here....humm");
                    fileToProcess = new ioRdTextFiles(actContractFile, false);
                }else {
                    System.out.println("the last ticker we did: " + tmpStr);
                }
            }
            while ((running == true) && ((tmpStr = fileToProcess.read()) != null)) {
                //if > 10 then must be an option contract, not just ticker line..so get option quote..
                
                if (tmpStr.length() > 10) {
                    // store only contracts and call getQuote to set up stream..
                    blkOfStr[sz++] = tmpStr;
                    
                    qInfo = actChain.getQuote(tmpStr, true /* option q*/);
                    optionTickCnt++;
                } else {
                    tickCnt++;
                    actTicker = tmpStr;
                }
                // activeTickerTextField.setText( "Ticks: " + Integer.toString(tickCnt));
                tickersLabel.setText("working on Ticker: " + actTicker + "(" + Integer.toString(tickCnt) + ")" +
                        " and option number: " + Integer.toString(optionTickCnt) + " found: " + Integer.toString(tblLine)
                        );
                
                if (sz >= STREAMSZ) {
                    if (actIbApi.apiAlarms.allOk(false /*hang*/) != true) {
                        /* if any connection problems with API, sleep */
                        System.out.println(" API connection Problem...");
                        while (actIbApi.apiAlarms.allOk(false /*hang*/) != true) {
                            myUtils.delay(1000);
                        }
                        System.out.println(" API connection Problem Cleared...");
                    }
                    myUtils.delay(3000);
                    // all STREAMSZ streams are all setup so just get quotes now..
                    int tickUsed = 0;
                    for (int lp = 0; sz != 0; lp++, sz--) {
                        
                        tmpStr = blkOfStr[lp];
                        qInfo = actChain.getQuote(tmpStr, true /* option q*/);
                        
                        // see if we were properly updated...
                        qus[lp] = new qverify();
                        qus[lp].updated = qInfo.tickerUsed;
                        qus[lp].updateCnt = qInfo.updateCnt;
                        
                        if (qus[lp].updateCnt > 0 ) {
                            tickUsed++;
                            
                        }
                        optionOI = qInfo.cpType.equals("C") ? qInfo.cOpenInterest : qInfo.pOpenInterest;
                        displayIt = false;
                        if ((selectedAndFuntion == true) && (qInfo.volume >= selectedVolumeInt) && (qInfo.volume >= optionOI)){
                            if ((qInfo.respBits & ibApi.OPEN_INTEREST) == 0) {
                                System.out.println("Wait for openInterest bit to be set..");
                                if (waitForOIBit(tmpStr) == true){
                                    System.out.println("ERROR waiting for openInterest bit to be set! Release stream..");
                                    if (actChain.cancelStream(tmpStr, true) == true ) {
                                        // re-establish stream....
                                        qInfo = actChain.getQuote(tmpStr, true /* option q*/);  
                                        myUtils.delay(1000);
                                        if (waitForOIBit(tmpStr) == true){
                                            System.out.println("ERROR AGAIN waiting for openInterest bit to be set!");
                                            //dipslay it anyway 
                                            displayIt = true;
                                        }else{
                                            System.out.println("IO Bit set!! Cool. ");
                                            // check again...
                                            optionOI = qInfo.cpType.equals("C") ? qInfo.cOpenInterest : qInfo.pOpenInterest;
                                            if (qInfo.volume >= optionOI) {
                                                displayIt = true;
                                            }else{
                                                displayIt = false;   
                                            }
                                                
                                        }                                         
                                    }
                                }else{
                                    System.out.println("openInterest bit SET!");
                                }
                                
                            }else{
                                displayIt = true;
                            }    
                        }else if ((selectedAndFuntion == false) && (qInfo.volume >= selectedVolumeInt)){
                            displayIt = true;
                        }
                        if (displayIt == true) {
                            System.out.println(
                                    tmpStr + " "
                                    + qInfo.cpType
                                    + " strk = " + qInfo.strikePrice
                                    + " dlta = " + myUtils.roundMe(qInfo.delta, 4)
                                    + " cOPI = " + qInfo.cOpenInterest
                                    + " pOPI = " + qInfo.pOpenInterest
                                    + " volu = " + qInfo.volume
                            );
                            optionDataTable.getModel().setValueAt(Integer.toString(tblLine), tblLine, oNumber);
                            optionDataTable.getModel().setValueAt(tmpStr, tblLine, oCONTRACT);
                            optionTypeStr = qInfo.cpType.equals("C") ? "C" : "P";
                            optionOI = qInfo.cpType.equals("C") ? qInfo.cOpenInterest : qInfo.pOpenInterest;

                            optionAmtStr = numformat.format((int)(myUtils.roundMe((qInfo.last * qInfo.volume * 100.0), 4))); 
                            
                            optionDataTable.getModel().setValueAt(optionTypeStr, tblLine, oTYPE);
                            optionDataTable.getModel().setValueAt(Double.toString(qInfo.strikePrice), tblLine, oSTRIKE);
                            optionDataTable.getModel().setValueAt((qInfo.bid), tblLine, oBID);
                            optionDataTable.getModel().setValueAt((qInfo.ask), tblLine, oASK);
                            optionDataTable.getModel().setValueAt((qInfo.last), tblLine, oLAST);                          
                            optionDataTable.getModel().setValueAt((qInfo.delta), tblLine, oDELTA);
                            optionDataTable.getModel().setValueAt(qInfo.volume, tblLine, oVOLUME);
                            optionDataTable.getModel().setValueAt(optionOI, tblLine, oOI);
                            optionDataTable.getModel().setValueAt(optionAmtStr, tblLine, oAmount);
                            
                            
                            tblLine++;
                            this.saveToFile.write("\n" + 
                                                Integer.toString(tblLine)                               +   "\t"    +                                             tmpStr + "\t"                                           +
                                                optionTypeStr                                           +   "\t"    +
                                                Double.toString(qInfo.strikePrice)                      +   "\t"    +
                                                Double.toString(qInfo.bid)                              +   "\t"    +
                                                Double.toString(qInfo.ask)                              +   "\t"    +       
                                                Double.toString(qInfo.last)                             +   "\t"    +
                                                Double.toString(myUtils.roundMe(qInfo.delta, 4))        +   "\t"    +
                                                Integer.toString(qInfo.volume)                          +   "\t"    +
                                                Integer.toString(optionOI)                              +   "\t"    +
                                                optionAmtStr
                            );
                            
                            if (tblLine >= optionDataTable.getRowCount()) {
                                this.saveToFile.write("\nTable full, starting over..");
                                clearOptionOptionDataTable();
                                System.out.println("performSearchTask: Table over flow!!!!! resetting tblLine...");
                                tblLine = 0;
                            }
                        }
                            
                    }
                    System.out.println("tickUsed: " + tickUsed);
                    // we can only have a certain number of streams at one time
                    // we reached that number, now release the streams and continue..
                    sz = 0;
                    System.out.println("Releasing streams..");
                    actChain.startNewChain();
                    //myUtils.delay(500);
                    //actChain.cancelChainStreams();
                    if (shutDown == true) {
                        System.out.println("shut down detected...wait..");
                        lastTickerProcessed = actTicker;
                        System.out.println("last ticker processed: " + lastTickerProcessed);
                        running = false;
                    }
                }
            }
            System.out.println("PerformSearchTask: Done search.\n");
            tickersLabel.setText("done with Ticker: " + actTicker + "(" + Integer.toString(tickCnt) + ")" +
                        " and option number: " + Integer.toString(optionTickCnt) + " found: " + Integer.toString(tblLine) +
                        "....done with search."
                        );
            this.saveToFile.write("\nSearch Completed.");
            searchTask.saveToFile.closeWr();
            searchTask.fileToProcess.closeRd();
            
        }

    }
    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // TODO add your handling code here:       
       
        ioRdTextFiles rdFile = null;
        ioWrTextFiles wrFile = null;
        
        

        if ((selectedExchangeStr == null) || ((selectedVolumeInt == 0) && (selectedPercentOfOpenInterest == 0))) {
            System.out.println("Cannot search, enter correct criteria.");
        }else{
            System.out.println("Selected index is: "+ selectedExchangeStr 
                             + " selected Volume is: "+ selectedVolumeInt 
                             + " selcted volume by percent of OI is: "+ selectedPercentOfOpenInterest);
            if((selectedVolumeInt != 0) && (selectedPercentOfOpenInterest != 0)) {
                System.out.println("Enter one or the other volume criteria, not both.");
                goSearch = false;
            }else{
                 int sz = 0;
                 rdFile = new ioRdTextFiles(actContractFile, false);
                 wrFile = new ioWrTextFiles(actContractFile + ".saveme", true /* append */);
                 lastTickerProcessed = null;
                 createNew = true;
                 searchTask = new performSearchTask(rdFile);
                 searchTask.saveToFile = wrFile;
                 searchTask.startme();
            }
            
           
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void pauseResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseResumeActionPerformed
        // TODO add your handling code here:
        ioRdTextFiles rdFile = null;
        ioRdTextFiles rdHisFile = null;
        ioWrTextFiles wrFile = null;
        File tmpFile = null;
         // if searchTask exists, means we have a session going that we will interrupt.
        if ((searchTask != null) && searchTask.running == true){
            searchTask.windDown();
            System.out.println("winding down search thread..");
            System.out.println("waiting for search to stop...");
            while (searchTask.running == true){
                myUtils.delay(100);
            }
            /* save the last ticker processed.. */
            wrFile = new ioWrTextFiles(actContractFile + ".history", false /* append */);
            wrFile.write(lastTickerProcessed);
            wrFile.closeWr();
            
            
        } else {
            if ((selectedExchangeStr == null)) {
                System.out.println("Cannot start, select exchange.");
                return;
            } else {
                System.out.println("Selected index is: " + selectedExchangeStr);

                rdFile = new ioRdTextFiles(actContractFile, false);
                wrFile = new ioWrTextFiles(actContractFile + ".saveme", true /* append */);
                rdHisFile = new ioRdTextFiles(actContractFile + ".history", false);
                lastTickerProcessed = rdHisFile.read();
                rdHisFile.closeRd();
                System.out.println("\nResume Processing with ticker " + lastTickerProcessed);
                createNew = false;
                clearOptionOptionDataTable();
                searchTask = new performSearchTask(rdFile);
                searchTask.saveToFile = wrFile;
                searchTask.startme();
            }
        }
    }//GEN-LAST:event_pauseResumeActionPerformed

    private void AndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AndActionPerformed
        // TODO add your handling code here:
        
        if (AndCheckBox.isSelected() == true) {
            selectedAndFuntion = true;    
        }else{
            selectedAndFuntion = false;
        }
        System.out.println("SelectedAndFunction: " + selectedAndFuntion);
    }//GEN-LAST:event_AndActionPerformed

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
            java.util.logging.Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(displayOptionsWithUnusualVolumeDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                displayOptionsWithUnusualVolumeDialogForm dialog = new displayOptionsWithUnusualVolumeDialogForm(new javax.swing.JFrame(), true);
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
    private javax.swing.JCheckBox AndCheckBox;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox indexComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable optionDataTable;
    private javax.swing.JButton pauseResumeButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel tickersLabel;
    private javax.swing.JTextField volumeTextField;
    // End of variables declaration//GEN-END:variables
}
