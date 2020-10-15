/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
//import ibApi.actTraderApi.*;
import ibTradeApi.ibApi.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author walterstevenson
 */
public class syncPosition {

    private newPos pos;
    private positionData createdPosition;
    private positionData actEditPos;
    private positions positionStorage;
    private String todaysDate;
    private boolean pGlobalLoopbackTrading = true;
    private boolean pLoopbackTrading = true;

    public syncPosition(positions posStoreHere) {
        setTodaysDate();
        positionStorage = posStoreHere;
        pGlobalLoopbackTrading = positionConfig.getActConfig().getTradeLoopback();
        JFrame f = new JFrame("Sync Delta Neutral Position ");
        f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pos = new newPos();
        f.getContentPane().add(new JButton("yes?"));

        f.setContentPane(pos);
        f.setSize(200, 200);
        f.pack();
        f.setVisible(true);

    }

    private void setTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        //todaysDate = DateFormat.getDateInstance().format(new Date());

        todaysDate = sdf.format(new Date());

    }

    private String getTodaysDate() {
        return (todaysDate);
    }

    public positionData getCreatedPosition() {
        return (createdPosition);
    }

    public void storeNewPositionHere(positions posStore) {
        positionStorage = posStore;
    }

    private class newPos extends JPanel implements ActionListener, ItemListener {

        private int posId;
        public String longTicker;
        public String shortTicker;
        public float longEntryPrice;
        public float shortEntryPrice;
        public float shortDelta;
        public float posBalance;
        public boolean completed = false;
        public int longShares;
        public int shortShares;
        private String posDate = todaysDate;
        final int POS_ID = 0,  LONG_TICKER = 1,  LONG_PRICE = 2,  LONG_SHARES = 3,  SHORT_TICKER = 4,  SHORT_PRICE = 5,  SHORT_DELTA = 6,  SHORT_CONTRACTS = 7,  POS_BALANCE = 8,  POS_DATE = 9,  LAST_ELEMENT = 10;
        private JFormattedTextField ftf[] = new JFormattedTextField[LAST_ELEMENT];

        private newPos() {

            // ftf[] = new JFormattedTextField[6];
            String des[] = new String[ftf.length]; // description of each field

            des[POS_ID] = "Pos ID";
            this.ftf[POS_ID] = new JFormattedTextField();
            this.ftf[POS_ID].setEnabled(false);
            this.ftf[POS_ID].setColumns(des[POS_ID].length() - 2);
            this.ftf[POS_ID].setText(String.valueOf(posId));

            des[LONG_TICKER] = "Long Ticker Symbol";
            this.ftf[LONG_TICKER] = new JFormattedTextField();
            this.ftf[LONG_TICKER].setEnabled(true);
            this.ftf[LONG_TICKER].setColumns(des[LONG_TICKER].length() - 8);
            this.ftf[LONG_TICKER].addActionListener(this);

            des[LONG_PRICE] = "Long Entry Price";
            this.ftf[LONG_PRICE] = new JFormattedTextField();
            this.ftf[LONG_PRICE].setEnabled(true);
            this.ftf[LONG_PRICE].setColumns(des[LONG_PRICE].length() - 8);
            this.ftf[LONG_PRICE].addActionListener(this);

            des[LONG_SHARES] = "Long Shares";
            this.ftf[LONG_SHARES] = new JFormattedTextField();
            this.ftf[LONG_SHARES].setEnabled(false);
            this.ftf[LONG_SHARES].setColumns(des[LONG_SHARES].length() - 4);
            this.ftf[LONG_SHARES].setText(String.valueOf(0));

            des[SHORT_TICKER] = "Short Ticker Symbol";
            this.ftf[SHORT_TICKER] = new JFormattedTextField();
            this.ftf[SHORT_TICKER].setColumns(des[SHORT_TICKER].length() - 8);
            this.ftf[SHORT_TICKER].setEnabled(true);
            this.ftf[SHORT_TICKER].addActionListener(this);

            des[SHORT_PRICE] = "Short Entry Price";
            this.ftf[SHORT_PRICE] = new JFormattedTextField();
            this.ftf[SHORT_PRICE].setColumns(des[SHORT_PRICE].length() - 6);
            this.ftf[SHORT_PRICE].setEnabled(true);
            this.ftf[SHORT_PRICE].addActionListener(this);

            des[SHORT_DELTA] = "Short Delta Value";
            this.ftf[SHORT_DELTA] = new JFormattedTextField();
            this.ftf[SHORT_DELTA].setColumns(des[SHORT_DELTA].length() - 8);
            this.ftf[SHORT_DELTA].setEnabled(true);
            this.ftf[SHORT_DELTA].addActionListener(this);

            des[SHORT_CONTRACTS] = "Short Contracts";
            this.ftf[SHORT_CONTRACTS] = new JFormattedTextField();
            this.ftf[SHORT_CONTRACTS].setColumns(des[SHORT_CONTRACTS].length() - 4);
            this.ftf[SHORT_CONTRACTS].setEnabled(false);
            this.ftf[SHORT_CONTRACTS].setText(String.valueOf(0));


            des[POS_BALANCE] = "Position Start Balance";
            this.ftf[POS_BALANCE] = new JFormattedTextField();
            this.ftf[POS_BALANCE].setColumns(des[POS_BALANCE].length() - 8);
            this.ftf[POS_BALANCE].setEnabled(true);
            this.ftf[POS_BALANCE].addActionListener(this);

            des[POS_DATE] = "Position Date";
            this.ftf[POS_DATE] = new JFormattedTextField();
            this.ftf[POS_DATE].setColumns(des[POS_DATE].length() - 8);
            this.ftf[POS_DATE].setEnabled(false);
            this.ftf[POS_DATE].addActionListener(this);
            this.ftf[POS_DATE].setText(posDate);

            // add each ftf[] to a BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            for (int j = 0; j < this.ftf.length; j += 1) {
                JPanel borderPanel = new JPanel(new java.awt.BorderLayout());
                borderPanel.setBorder(new javax.swing.border.TitledBorder(des[j]));
                borderPanel.add(this.ftf[j], java.awt.BorderLayout.CENTER);
                add(borderPanel);
            }
            JButton doneButton = new JButton("Done");
            doneListener doneHandle = new doneListener();
            doneButton.addActionListener(doneHandle);

            add(doneButton);
        }

        private positionData posDataCopy() {
            positionData pdRet = new positionData(true);
            pdRet.setPosId(posId);
            pdRet.longTicker = longTicker;
            pdRet.shortTicker = shortTicker;
            pdRet.longEntryPrice = longEntryPrice;
            pdRet.shortEntryPrice = shortEntryPrice;
            pdRet.shortDelta = shortDelta;
            pdRet.posBalance = posBalance;
            pdRet.completed = completed;
            pdRet.longShares = longShares;
            pdRet.shortShares = shortShares;
            pdRet.posDateStr = posDate;

            return (pdRet);
        }

        private class doneListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Done") && (completed == false)) {
                    for (int lp = 0; lp < ftf.length; lp++) {
                        try {
                            ftf[lp].commitEdit();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }

                    longTicker = ftf[LONG_TICKER].getText();
                    try {
                        longEntryPrice = Float.parseFloat(ftf[LONG_PRICE].getText());
                        longShares = Integer.parseInt(ftf[LONG_SHARES].getText());
                        shortTicker = ftf[SHORT_TICKER].getText();
                        shortEntryPrice = Float.parseFloat(ftf[SHORT_PRICE].getText());

                        shortDelta = Math.abs(Float.parseFloat(ftf[SHORT_DELTA].getText()));
                        shortShares = Integer.parseInt(ftf[SHORT_CONTRACTS].getText());
                        posBalance = Float.parseFloat(ftf[POS_BALANCE].getText());
                    // posDate = ftf[POS_DATE].getText();
                    } catch (Exception ef) {
                        //ef.printStackTrace();
                        commonGui.prMsg("Incorrect Numeric Input. Try Again.");
                    }
                    completed = true;
                    positionData pd = posDataCopy();
                    pd.staLongShares = longShares;
                    pd.staShortShares = shortShares;
                    createdPosition = pd;
                    
                    positionStorage.posDataStore(pd);
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            quoteInfo quote = new quoteInfo();

            try {
                completed = false;
                if (event.equals(ftf[LONG_TICKER])) {
                    longTicker = event.getText();
                } else if (event.equals(ftf[LONG_PRICE])) {
                    longEntryPrice = Float.parseFloat(event.getText());
                } else if (event.equals(ftf[SHORT_TICKER])) {
                    shortTicker = event.getText();
                } else if (event.equals(ftf[SHORT_PRICE])) {
                    shortEntryPrice = Float.parseFloat(event.getText());
                } else if (event.equals(ftf[SHORT_DELTA])) {
                    shortDelta = Float.parseFloat(event.getText());
                } else if (event.equals(ftf[POS_BALANCE])) {
                    posBalance = Float.parseFloat(event.getText());
                }

            } catch (Exception ef) {
                //ef.printStackTrace();
                commonGui.prMsg("Incorrect Numeric Input. Try Again.");
            }

        }

        public void itemStateChanged(ItemEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());

        }

        public void processKeyEvent(KeyEvent ke) {
            int key = ke.getID();
            System.out.println("key:" + key);
        }
    }
    
    private class getNewAdj extends JPanel implements  ActionListener {
        private float longPrice;
        private float shortPrice;
        private float delta;
        private int sharesLong;
        private int sharesShort;
        private float balance;
        private boolean pBuySell = true;
        JCheckBox jbBuySell;
        private positionAdjustment setThisAdjustment;
        public boolean completed = false;
                
        private JFormattedTextField ftf[] = new JFormattedTextField[3];
        getNewAdj(positionAdjustment thisAdj) { 
            setThisAdjustment = thisAdj;
           // ftf[] = new JFormattedTextField[6];
            String des[] = new String[ftf.length]; // description of each field
                        
            
            des[0] = "Long Price";
            this.ftf[0] = new JFormattedTextField();
            this.ftf[0].setEnabled(true);
            this.ftf[0].setColumns(des[0].length()-4);
            this.ftf[0].addActionListener(this); 
            
            des[1] = "Short Price";
            this.ftf[1] = new JFormattedTextField();
            this.ftf[1].setEnabled(true);
            this.ftf[1].setColumns(des[1].length()-4);
            this.ftf[1].addActionListener(this);            
            
            des[2] = "Delta";
            this.ftf[2] = new JFormattedTextField();
            this.ftf[2].setEnabled(true);
            this.ftf[2].setColumns(des[2].length());
            this.ftf[2].addActionListener(this);
            
            jbBuySell = new JCheckBox("Check to Buy, Uncheck to Sell");
            jbBuySell.setMnemonic(KeyEvent.VK_C);
            
            jbBuySell.setEnabled(true);
            /* default to buy (checked) */
            jbBuySell.setSelected(pBuySell);
            checkListener checkHandle = new checkListener();
            jbBuySell.addActionListener(checkHandle);
            this.add(jbBuySell);
            
            // add each ftf[] to a BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            for (int j = 0; j < this.ftf.length; j += 1) {
                JPanel borderPanel = new JPanel(new java.awt.BorderLayout());
                borderPanel.setBorder(new javax.swing.border.TitledBorder(des[j]));
                borderPanel.add(this.ftf[j], java.awt.BorderLayout.CENTER);
                add(borderPanel);
            }            
           JButton doneButton = new JButton("Done"); 
           doneListener doneHandle = new doneListener();
           doneButton.addActionListener(doneHandle);
           
           add(doneButton);
        }
        
        private class doneListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Done") && (completed == false)) {
                    for (int lp = 0; lp < ftf.length; lp++) {
                        try {
                            ftf[lp].commitEdit();
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    try {
                        longPrice = Float.parseFloat(ftf[0].getText());
                        shortPrice = Float.parseFloat(ftf[1].getText());
                        delta = Float.parseFloat(ftf[2].getText());
                        pBuySell = jbBuySell.isSelected();
                        System.out.println("SyncAdj: longPrice = "+longPrice + "shortPrice = "+shortPrice + "delta = "+delta + "pBuySell = " + pBuySell);
                    }catch (Exception pe) {
                        commonGui.postInformationMsg("Incorrect Numeric Input. Try again.");
                    }
                    completed = true;
                    setThisAdjustment.longPrice = longPrice;
                    setThisAdjustment.shortPrice = shortPrice;
                    setThisAdjustment.delta = delta;
                    
                    /* pBuySell true means BUY, pBuySell false means SELL */
                    if ((pBuySell == true) && (sharesLong > 0) ) {
                        
                        setThisAdjustment.outcome.setOutcomeBuyLong(sharesLong);
                        setThisAdjustment.sharesLong += setThisAdjustment.outcome.adjustment;
                        setThisAdjustment.balance -= (setThisAdjustment.outcome.adjustment * setThisAdjustment.longPrice);
                        
                    }else if ((pBuySell == true) && (sharesShort > 0)) {
                        
                        setThisAdjustment.outcome.setOutcomeBuyShort(sharesShort);
                        setThisAdjustment.sharesShort += setThisAdjustment.outcome.adjustment;
                        setThisAdjustment.balance -= (setThisAdjustment.outcome.adjustment * setThisAdjustment.sharesShort);
                        
                    }if ((pBuySell == false) && (sharesLong > 0)) {
                        
                        setThisAdjustment.outcome.setOutcomeSellLong(sharesLong);
                        setThisAdjustment.sharesLong -= setThisAdjustment.outcome.adjustment;
                        setThisAdjustment.balance += (setThisAdjustment.outcome.adjustment * setThisAdjustment.longPrice);
                        
                    }else if ((pBuySell == false) && (sharesShort > 0)) {
                        
                        setThisAdjustment.outcome.setOutcomeSellShort(sharesShort);
                        setThisAdjustment.sharesShort -= setThisAdjustment.outcome.adjustment;
                        setThisAdjustment.balance += (setThisAdjustment.outcome.adjustment * setThisAdjustment.shortPrice * 100);
                        
                    }
                    
                    setThisAdjustment.profitLoss = (((setThisAdjustment.sharesLong * setThisAdjustment.longPrice) + 
                                       (setThisAdjustment.sharesShort * setThisAdjustment.shortPrice * 100) +
                                       (setThisAdjustment.balance)) - (actEditPos.posBalance));
                    
                    /* we don't want to trades to trigger so clear tradeNeeded and trade pending  */
                    setThisAdjustment.setTradeNeeded(false);
                    setThisAdjustment.setTradePending(false);
                    actEditPos.posAdjAdd(setThisAdjustment);

                }
            }
        }
        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField)e.getSource();
            Integer pid;
            try {
                completed = false;
                if (event.equals(ftf[0])) {
                longPrice = Float.parseFloat(event.getText());
                }
                else if (event.equals(ftf[1])) {
                    shortPrice = Float.parseFloat(event.getText());
                }
                else if (event.equals(ftf[2])) {
                    delta = Float.parseFloat(event.getText());
                }
            }catch (Exception pe) {
                commonGui.postInformationMsg("Incorrect Numeric Input. Try again.");                
            }
        }
        
        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                
                completed = false;
                if (command.equals("Check to Buy, Uncheck to Sell")) {
                    pBuySell = jbBuySell.isSelected();
                    System.out.println("pBuySell = " + pBuySell);
                }
                
            }
        }
        
     } /* getNewAdj */     
    
    public void syncAdjustment(positionData posDataIn) {
        
        positionAdjustment newAdjustment = new positionAdjustment(true);
        getNewAdj adjData;
        actEditPos = posDataIn;
        
        JFrame f = new JFrame("Enter Position Adjustment ");
        f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        f.setContentPane(adjData = new getNewAdj(newAdjustment));
        f.setSize(200,200);
        f.pack();
        f.setVisible(true);      
        
        
    }
}
