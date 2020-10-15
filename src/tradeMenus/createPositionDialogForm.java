/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import positions.*;
import ibTradeApi.ibApi.*;
import ibTradeApi.ibApi;
import ibTradeApi.ibApi.quoteInfo;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author earlie87
 */
public class createPositionDialogForm extends javax.swing.JDialog {

    /**
     * Creates new form createPositionDialogForm
     */
    private boolean loopBackPos = false;
    private positions actPortfolio = null;
    private int posId = 0;
    private String longTicker = null;
    private int longShares = 0;
    private String shortTicker = null;
    private int posStartBal = 0;
    private String todaysDate = null;
    private int shortShares = 0;
    private float shortDelta = 0;
    private float biasLS;
    private double longEntryPrice = 0;
    private double shortEntryPrice = 0;
    private boolean completed = false;
    private positionData newPositionData;
    displayOptionChainDialogForm optionChainDialog;
    private positionData createdPosition;
    private String posToCreateTicker = null;
    
    ibApi.quoteInfo quote = new ibApi.quoteInfo();
    ibApi actIbApi = ibApi.getActApi();
    
    
    public createPositionDialogForm(java.awt.Frame parent, boolean modal) {
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
        
        posIdTextField.setText(Integer.toString(posId));
        longTickerTextField.requestFocusInWindow();
        activePortfolioMessageLabel.setText("Create New Position in: " + PlayWIthMenus.actMainMenu.activePortfolio);
        fillFieldsCheckBox.setEnabled(false);
        openPositionButton.setEnabled(true);
        openPositionButton.setSelected(true);
        
    }
    private String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        //todaysDate = DateFormat.getDateInstance().format(new Date());
        
        return (sdf.format(new Date()));
   
    }
    public positionData getCreatedPosition() {
        return (createdPosition);
    }
    public void setTickerToStart(String tickerin){
        
        posToCreateTicker = tickerin;
        
        
    }
    
    boolean isFiller1Enabled() {
        /* CHANGE THIS WFS.....*/
        return(true);
       /*  
            if (positionConfig.getActConfig() != null) {
                return (positionConfig.getActConfig().getConfigFiller1());
            }
            return (false);
        }
        * 
        */
    }
    private positionData createAPositionData() {
        positionData pdRet = new positionData(true);
        pdRet.setPosId(posId);
        pdRet.longTicker = longTicker;
        pdRet.shortTicker = shortTicker;
        pdRet.longEntryPrice = (float)longEntryPrice;
        pdRet.shortEntryPrice = (float)shortEntryPrice;
        pdRet.shortDelta = shortDelta;
        pdRet.posBalance = posStartBal;
        pdRet.completed = completed;
        pdRet.longShares = longShares;
        pdRet.shortShares = shortShares;
        pdRet.posDateStr = todaysDate;
        pdRet.setRunningBalance(posStartBal);

        return (pdRet);
    }
    private void calculateDeltaNeutralPosition() {
        
        int extraShares;
        if (longShares > 0) {
                /* do the calculations now 
                 * fist the bias Long/short.
                 * then # of contracts to to become
                 * nuetral.
                 */
                biasLS = (float) (((float) shortShares * 100.0 * -1.0) + (float) longShares);
                
                if(isFiller1Enabled() == true) {
                    /* filler 1 is meant to start things out closer to nuetral so ..
                       figure out whole number of contracts first, the fraction (extra short shares)
                     * are lost here.
                     */

    
                    shortShares = Math.round(((float)longShares / shortDelta) / (float)100.0);
                    extraShares = (Math.round(shortShares * shortDelta * 100) * -1) + longShares;
                    if (extraShares < 0) {
                        /* means negative number or short these shares
                           so you add these number of shorts to the long shares 
                         */
                        longShares += Math.abs(extraShares);
                    } else {
                        /* means positive number or long extra shares, 
                         so subtract these from the long shares to buy.
                         */
                        longShares -= Math.abs(extraShares);
                    }
                    
                }else {
                    shortShares = Math.round(((float)longShares / shortDelta) / (float)100.0);
                }                
                biasLS = (float) (((float) shortShares * 100.0 * shortDelta * -1.0) + (float) longShares);
            }
    }
    public void startPosition() {
        
    }
    public void addToThisPortfolio(positions port) {
        actPortfolio = port;
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
        loopbackCheckBox = new javax.swing.JCheckBox();
        posIdTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        longTickerTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        longEntryPriceTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        longSharesTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        shortTickerTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        shortEntryPriceTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        shortDeltaTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        shortContractsTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        positionStartBalanceTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        positionDateTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        longShortBiasTextField = new javax.swing.JTextField();
        createOkButton = new javax.swing.JButton();
        activePortfolioMessageLabel = new javax.swing.JLabel();
        optionChainButton = new javax.swing.JButton();
        fillFieldsCheckBox = new javax.swing.JCheckBox();
        openPositionButton = new javax.swing.JCheckBox();
        doneButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create Position\n");

        jLabel1.setText("Loop Back Position");

        loopbackCheckBox.setRequestFocusEnabled(false);
        loopbackCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopbackCheckBoxActionPerformed(evt);
            }
        });

        posIdTextField.setEditable(false);
        posIdTextField.setRequestFocusEnabled(false);

        jLabel2.setText("Posiiton ID");

        jLabel3.setText("Long Ticker");

        longTickerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longTickerTextFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Long Entry Price");

        longEntryPriceTextField.setEditable(false);

        jLabel5.setText("Long Shares");

        longSharesTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longSharesTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Short Ticker");

        shortTickerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shortTickerTextFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Short Entry Price");

        shortEntryPriceTextField.setEditable(false);

        jLabel8.setText("Short Delta");

        shortDeltaTextField.setEditable(false);

        jLabel9.setText("Short Contracts");

        shortContractsTextField.setEditable(false);

        jLabel10.setText("Position Start Balance");

        positionStartBalanceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                positionStartBalanceTextFieldActionPerformed(evt);
            }
        });

        jLabel11.setText("Position Date");

        positionDateTextField.setEditable(false);

        jLabel12.setText("Long/Short Bias");

        longShortBiasTextField.setEditable(false);

        createOkButton.setText("Create");
        createOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createOkButtonActionPerformed(evt);
            }
        });

        optionChainButton.setText("Option Chain");
        optionChainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionChainButtonActionPerformed(evt);
            }
        });

        fillFieldsCheckBox.setText("Fill Fields");
        fillFieldsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillFieldsCheckBoxActionPerformed(evt);
            }
        });

        openPositionButton.setText("Open Created Position");
        openPositionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPositionButtonActionPerformed(evt);
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
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(85, 85, 85)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel6)
                            .add(jLabel7)
                            .add(jLabel8)
                            .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel5)
                            .add(jLabel9)
                            .add(jLabel10)
                            .add(jLabel11)
                            .add(jLabel12)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel2)
                                .add(jLabel3))
                            .add(jLabel1))
                        .add(18, 18, 18)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(longEntryPriceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(loopbackCheckBox)
                            .add(posIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(longTickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(shortEntryPriceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(longSharesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(shortDeltaTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(shortContractsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(positionStartBalanceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(positionDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(longShortBiasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(shortTickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 184, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(activePortfolioMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 435, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(177, 177, 177)
                        .add(optionChainButton)
                        .add(51, 51, 51)
                        .add(fillFieldsCheckBox)))
                .addContainerGap(97, Short.MAX_VALUE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(200, 200, 200)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(doneButton)
                        .addContainerGap())
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(createOkButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(openPositionButton)
                        .add(39, 39, 39))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(activePortfolioMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(33, 33, 33)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(loopbackCheckBox)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(posIdTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(longTickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(longEntryPriceTextField))
                .add(9, 9, 9)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(longSharesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shortTickerTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shortEntryPriceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(shortDeltaTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shortContractsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(positionStartBalanceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel10))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(positionDateTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel11))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(longShortBiasTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12))
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(optionChainButton)
                    .add(fillFieldsCheckBox))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(createOkButton)
                    .add(openPositionButton))
                .add(18, 18, 18)
                .add(doneButton)
                .add(14, 14, 14))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loopbackCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopbackCheckBoxActionPerformed
        loopBackPos = loopbackCheckBox.isSelected();
    }//GEN-LAST:event_loopbackCheckBoxActionPerformed

    private void longTickerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longTickerTextFieldActionPerformed
        longTicker = longTickerTextField.getText();
        quote = actIbApi.getQuote(longTicker, false);
        longEntryPriceTextField.setText(Double.toString(quote.last));
        longEntryPrice = quote.last;
    }//GEN-LAST:event_longTickerTextFieldActionPerformed

    private void longSharesTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longSharesTextFieldActionPerformed
        boolean ready = false;
        
        longShares = Integer.parseInt(longSharesTextField.getText());
        ready = !((longTicker == null) || (longShares == 0) || (shortTicker == null));
        if (ready == true){
            calculateDeltaNeutralPosition();
            positionStartBalanceTextField.setText(Integer.toString(posStartBal));
            todaysDate = getTodaysDate();
            positionDateTextField.setText(todaysDate);
            longSharesTextField.setText(Integer.toString(longShares));
            
            shortContractsTextField.setText(Integer.toString(shortShares));
            longShortBiasTextField.setText(Float.toString(biasLS));
        }
        System.out.println("longShares = "+longShares);
    }//GEN-LAST:event_longSharesTextFieldActionPerformed

    private void shortTickerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortTickerTextFieldActionPerformed
        boolean ready = false;
        shortTicker = shortTickerTextField.getText();
        quote = actIbApi.getQuote(shortTicker, true);

        shortEntryPriceTextField.setText(Double.toString(myUtils.roundMe(quote.value, 2)));
        shortDeltaTextField.setText(Double.toString(quote.delta));
        shortEntryPrice = quote.value;
        shortDelta = Math.abs((float) quote.delta);
        
        calculateDeltaNeutralPosition();
        todaysDate = getTodaysDate();
        positionDateTextField.setText(todaysDate);
        ready = !((longTicker == null) || (longShares == 0) || (shortTicker == null));
        if(ready == true){
            positionStartBalanceTextField.setText(Integer.toString(posStartBal));
            
            longSharesTextField.setText(Integer.toString(longShares));
            
            shortContractsTextField.setText(Integer.toString(shortShares));
            longShortBiasTextField.setText(Float.toString(biasLS));
        }
        
    }//GEN-LAST:event_shortTickerTextFieldActionPerformed

    private void positionStartBalanceTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_positionStartBalanceTextFieldActionPerformed
        posStartBal = Integer.parseInt(positionStartBalanceTextField.getText());
    }//GEN-LAST:event_positionStartBalanceTextFieldActionPerformed

    private void createOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createOkButtonActionPerformed
       
        positionEditor actEditPos;
        boolean inError = false;
        boolean notReady = ((longTicker == null) || (longShares == 0) || (shortTicker == null) || (posStartBal == 0));
        
        if ((longTicker == null) || (longShares == 0) || (shortTicker == null) || (posStartBal == 0)){
            inError = true;
        }
        if (inError == false) {
            
            calculateDeltaNeutralPosition();
            positionStartBalanceTextField.setText(Integer.toString(posStartBal));
            todaysDate = getTodaysDate();
            positionDateTextField.setText(todaysDate);
            longSharesTextField.setText(Integer.toString(longShares));
            
            shortContractsTextField.setText(Integer.toString(shortShares));
            longShortBiasTextField.setText(Float.toString(biasLS));
            activePortfolioMessageLabel.setText("Position Created in: " + PlayWIthMenus.actMainMenu.activePortfolio + " Portfolio.");
            completed = true;
            /* copy all these parameters to positionData class...*/
            newPositionData = createAPositionData();
            
            /* clear these out and let the startOutPosition routine fille these in..later */
            newPositionData.longShares = 0;
            newPositionData.shortShares = 0;
            /* set the loopback trading (trade simulator) boolean.. */
            newPositionData.setTradeLoopback(loopBackPos);
           
            /*
             * now add these adjustments to position
             *///wfs
            newPositionData.staLongShares = longShares;
            newPositionData.staShortShares = shortShares;
            
            if (openPositionButton.isSelected() == true) {
                System.out.println("Creating and opening position..");
                actEditPos = new positionEditor(newPositionData);
                actEditPos.editAdjData.setAdjDate(newPositionData.getPosDate());
                actEditPos.startOutPosition(0, shortShares, biasLS);

                actEditPos = new positionEditor(newPositionData);
                actEditPos.editAdjData.setAdjDate(newPositionData.getPosDate());
                actEditPos.startOutPosition(longShares, 0, biasLS);
                /*
                 * create a volatility monitor for this new position
                 */
                createdPosition = newPositionData;
                actPortfolio.posDataStore(newPositionData);            
                actPortfolio.posDataSaveToDisk(actPortfolio.getPositionFileName(), false);
                newPositionData.setVm(new volatilityMonitor(actPortfolio));
                actPortfolio.setDefVm(newPositionData.getVm());

            }else {
                System.out.println("Creating position and storing only, not Opening..");
                createdPosition = newPositionData;
                actPortfolio.posDataStore(newPositionData);            
                actPortfolio.posDataSaveToDisk(actPortfolio.getPositionFileName(), false);
                newPositionData.setVm(new volatilityMonitor(actPortfolio));
                actPortfolio.setDefVm(newPositionData.getVm());
                
            }
        }else{
            commonGui.postInformationMsg("Not ready to create: check Fields.");
        }
        
    }//GEN-LAST:event_createOkButtonActionPerformed

    private void optionChainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionChainButtonActionPerformed
        //displayOptionChainDialogForm optionChainDialog;
    
        optionChainDialog = new displayOptionChainDialogForm(new javax.swing.JFrame(), true);
        optionChainDialog.setActivePortfolio(PlayWIthMenus.actMainMenu.actPositions);
        if(posToCreateTicker != null){
            optionChainDialog.setTickerToStart(posToCreateTicker);
        }
        optionChainDialog.setVisible(true);   
        fillFieldsCheckBox.setEnabled(true);
        
    }//GEN-LAST:event_optionChainButtonActionPerformed

    private void fillFieldsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillFieldsCheckBoxActionPerformed
        boolean rdy = optionChainDialog.retData.dataReady;
        String ticker = optionChainDialog.retData.stockTicker;
        String option = optionChainDialog.retData.optionTicker;
        
        if ((fillFieldsCheckBox.isSelected() == true) && (rdy == true)) {
            longTickerTextField.setText(ticker);
            shortTickerTextField.setText(option);
            longTickerTextFieldActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            shortTickerTextFieldActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
              
        }
    }//GEN-LAST:event_fillFieldsCheckBoxActionPerformed

    private void openPositionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPositionButtonActionPerformed
        
    }//GEN-LAST:event_openPositionButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activePortfolioMessageLabel;
    private javax.swing.JButton createOkButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JCheckBox fillFieldsCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField longEntryPriceTextField;
    private javax.swing.JTextField longSharesTextField;
    private javax.swing.JTextField longShortBiasTextField;
    private javax.swing.JTextField longTickerTextField;
    private javax.swing.JCheckBox loopbackCheckBox;
    private javax.swing.JCheckBox openPositionButton;
    private javax.swing.JButton optionChainButton;
    private javax.swing.JTextField posIdTextField;
    private javax.swing.JTextField positionDateTextField;
    private javax.swing.JTextField positionStartBalanceTextField;
    private javax.swing.JTextField shortContractsTextField;
    private javax.swing.JTextField shortDeltaTextField;
    private javax.swing.JTextField shortEntryPriceTextField;
    private javax.swing.JTextField shortTickerTextField;
    // End of variables declaration//GEN-END:variables
}
