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

//import tdTradeApi.actTraderApi.*;

import ibTradeApi.*;
import ibTradeApi.ibApi.quoteInfo;
import java.awt.Choice;
import java.awt.Label;
//import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 *
 * @author walterstevenson
 */
public class createPosition {

    private newPos pos;
    private positionData createdPosition;
    private positionEditor actEditPos;
    private positions positionStorage;
    private String todaysDate;
    private boolean pGlobalLoopbackTrading = true;
    private boolean pLoopbackTrading = true;
    ibApi actIbApi = ibApi.getActApi();

    public createPosition(positions posStoreHere) {
        setTodaysDate();
        positionStorage = posStoreHere;
        pGlobalLoopbackTrading = positionConfig.getActConfig().getTradeLoopback();
        JFrame f = new JFrame("Create New Delta Neutral Position ");
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
        return(todaysDate);
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
        Choice accList;
        String selItem;
        ManagedAccounts allAccounts;
        int ix;
        String anAccount;
        JLabel text1Label;
        private newPos() {

            if (positionStorage.posDataEmpty() == true){
                System.out.println("new positions file!!");
            }
            allAccounts = ManagedAccounts.getAllAccounts();
            
            if(!positionStorage.getAccountNumber().equals("New")) {
                /* if we have an account defined already, don't have user select one. */
                //accList.add(positionStorage.getAccountNumber());
                text1Label = new JLabel("<html>Account:"+
                        "<P>"+ positionStorage.getAccountNumber());
                add(text1Label);
            } else {
                
                Label accountLable = new Label("Account:");
                accList = new Choice();
                add(accountLable);
                add(accList);

                /* no account defined yet so ask user .. */
                for (ix = 0; ix < allAccounts.getNumOfAccounts(); ix++) {

                    if ((anAccount = allAccounts.getAnEnabledAccount(ix)) != null) {
                        accList.add(anAccount);
                    }

                }
                accList.addItemListener(new accountSelListener());
                selItem = accList.getSelectedItem();
                
            }
            
            
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
            this.ftf[LONG_PRICE].setEnabled(false);
            this.ftf[LONG_PRICE].setColumns(des[LONG_PRICE].length() - 8);
            this.ftf[LONG_PRICE].addActionListener(this);

            des[LONG_SHARES] = "Long Shares";
            this.ftf[LONG_SHARES] = new JFormattedTextField();
            this.ftf[LONG_SHARES].setEnabled(false);
            this.ftf[LONG_SHARES].setColumns(des[LONG_SHARES].length() - 4);
            this.ftf[LONG_SHARES].setText(String.valueOf(0));

            des[SHORT_TICKER] = "Short Ticker Symbol";
            this.ftf[SHORT_TICKER] = new JFormattedTextField();
//            this.ftf[SHORT_TICKER].setColumns(des[SHORT_TICKER].length() - 8);
            this.ftf[SHORT_TICKER].setColumns(des[SHORT_TICKER].length() +5);
            this.ftf[SHORT_TICKER].setEnabled(true);
            this.ftf[SHORT_TICKER].addActionListener(this);

            des[SHORT_PRICE] = "Short Entry Price";
            this.ftf[SHORT_PRICE] = new JFormattedTextField();
            this.ftf[SHORT_PRICE].setColumns(des[SHORT_PRICE].length() - 6);
            this.ftf[SHORT_PRICE].setEnabled(false);
            this.ftf[SHORT_PRICE].addActionListener(this);

            des[SHORT_DELTA] = "Short Delta Value";
            this.ftf[SHORT_DELTA] = new JFormattedTextField();
            this.ftf[SHORT_DELTA].setColumns(des[SHORT_DELTA].length() - 8);
            this.ftf[SHORT_DELTA].setEnabled(false);
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
            pdRet.setRunningBalance(posBalance);

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


                    /*  we must first calculate longShares and Short shares.
                     */

                    /* this is the first adjustment */
                    startUpPosition sp = new startUpPosition(pd);
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
                        ftf[LONG_TICKER].commitEdit();
                        ftf[POS_DATE].commitEdit();
                        longTicker = event.getText();
                        //posDate = ftf[POS_DATE].getText();
                        //quote = actTraderApi.getQuote(longTicker);

                        quote = actIbApi.getQuote(longTicker, false);
                        if(quote != null) {
                            ftf[LONG_PRICE].setText(String.valueOf(quote.last));
                        }else {
                            commonGui.prMsg("Problem getting stock quote.");  
                        }

                    }
                    if (event.equals(ftf[LONG_SHARES])) {
                        longShares = Integer.parseInt(event.getText());
                    } else if (event.equals(ftf[SHORT_TICKER])) {

                        ftf[SHORT_TICKER].commitEdit();
                        ftf[POS_DATE].commitEdit();
                        ftf[LONG_TICKER].commitEdit();
                        longTicker = ftf[LONG_TICKER].getText();
                        event.commitEdit();
                        
                        shortTicker = event.getText();
                        //posDate = ftf[POS_DATE].getText();
                         
//                        quote = actTraderApi.getQuote("+"+shortTicker, /* option */ true);

                        quote = actIbApi.getQuote(shortTicker, /* option */ true);
                        if(quote != null) {
                            
                            ftf[SHORT_PRICE].setText(String.valueOf(quote.value));
                            
                            ftf[SHORT_DELTA].setText(String.valueOf(quote.delta));
                        }else {
                            commonGui.prMsg("Problem getting option quote.");  
                        }        
                                
                    } else if (event.equals(ftf[POS_BALANCE])) {
                        posBalance = Float.parseFloat(event.getText());
                    } else if (event.equals(ftf[POS_DATE])) {
                        ftf[LONG_TICKER].commitEdit();
                        ftf[POS_DATE].commitEdit();
                        event.commitEdit();
                        longTicker = ftf[LONG_TICKER].getText();
                        //posDate = event.getText();
                        
                        quote = actIbApi.getQuote(longTicker, false);
                        if(quote != null) {
                            ftf[LONG_PRICE].setText(String.valueOf(quote.last));  
                        }else {
                            commonGui.prMsg("Problem getting stock quote.");  
                        }
                        
                        ftf[SHORT_TICKER].commitEdit();
                        ftf[POS_DATE].commitEdit();
                        ftf[LONG_TICKER].commitEdit();
//                        shortTicker = ftf[SHORT_TICKER].getText();
                        shortTicker = ftf[SHORT_TICKER].getText();
                        
                        quote = actIbApi.getQuote(longTicker, /* option */ true);
                        if(quote != null) {
                            
                            ftf[SHORT_PRICE].setText(String.valueOf(quote.value));
                            
                            ftf[SHORT_DELTA].setText(String.valueOf(quote.delta));
                        }else {
                            commonGui.prMsg("Problem getting option quote.");  
                        }        

                    }
                } catch (Exception ef) {
                    //ef.printStackTrace();
                    ef.printStackTrace();
                    commonGui.prMsg("Incorrect Numeric Input. Try Again."+ef);
                }
            
        }

        public void itemStateChanged(ItemEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());

        }

        public void processKeyEvent(KeyEvent ke) {
            int key = ke.getID();
            System.out.println("key:" + key);
        }
        
        private class accountSelListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
//                accountEnable.setEnabled(true);
                selItem = accList.getSelectedItem();
                positionStorage.setAccountNumber(selItem);
                System.out.println("accountSelListener: selacc = " + selItem);
                System.out.println("accountSelListener: wrote "+ selItem+ " to positionStorage.");
                
                //isAccountEnabled = accountEnable.isSelected();
                
                //System.out.println("isAccountEnabled = " + isAccountEnabled);
                
            }
        }
        
        
        
    }

    public positionData getPosData() {
        positionData pd = new positionData(false);
        pd.setPosId(pos.POS_ID);
        pd.longTicker = pos.longTicker;
        pd.shortTicker = pos.shortTicker;
        pd.longEntryPrice = pos.longEntryPrice;
        pd.shortEntryPrice = pos.shortEntryPrice;
        pd.shortDelta = pos.shortDelta;
        pd.completed = pos.completed;
        pd.longShares = pos.longShares;
        pd.shortShares = pos.shortShares;

        return (pd);
    }

    private class startPosition extends JPanel implements ActionListener, ItemListener {

        public float biasLS;
        public boolean completed = false;
        public int longShares = 0;
        public int shortShares = 0;
        private positionData posToStart;
        private JFormattedTextField ftf[] = new JFormattedTextField[3];
        JCheckBox jbLoopbackTrading;

        startPosition(positionData pos) {
            posToStart = pos;
            String des[] = new String[ftf.length]; // description of each field

            des[0] = "Long Shares";
            this.ftf[0] = new JFormattedTextField();
            this.ftf[0].setEnabled(true);
            this.ftf[0].setColumns(des[0].length() - 4);
            this.ftf[0].addActionListener(this);

            des[1] = "Short Contracts";
            this.ftf[1] = new JFormattedTextField();
            this.ftf[1].setEnabled(false);
            this.ftf[1].setColumns(des[1].length() - 2);
            this.ftf[1].setText(String.valueOf(shortShares));
            this.ftf[1].addActionListener(this);

            des[2] = "Long/Short Bias";
            this.ftf[2] = new JFormattedTextField();
            this.ftf[2].setEnabled(false);
            this.ftf[2].setColumns(des[2].length() - 2);
            this.ftf[2].setText(String.valueOf(biasLS));
            this.ftf[2].addActionListener(this);

            jbLoopbackTrading = new JCheckBox("Loopback Trading");
            jbLoopbackTrading.setMnemonic(KeyEvent.VK_C);
            /* enable individual pos control only if global is turned off! 
             * if you want to change this value, you need to turn off the global first.
             */
            pLoopbackTrading = posToStart.getTradeLoopback();
            if (pGlobalLoopbackTrading == true) {
                jbLoopbackTrading.setEnabled(false);
                jbLoopbackTrading.setSelected(true);
            }else {
                jbLoopbackTrading.setEnabled(true);
                jbLoopbackTrading.setSelected(pLoopbackTrading);
            }
            checkListener checkLoopbackTradingHandle = new checkListener();
            jbLoopbackTrading.addActionListener(checkLoopbackTradingHandle);
            this.add(jbLoopbackTrading);
            
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
                        //longShares = Integer.parseInt(ftf[0].getText());
                    } catch (Exception ef) {
                        //ef.printStackTrace();
                        commonGui.prMsg("Incorrect Numeric Input. Try Again.");
                    }
                    completed = true;
                    /* set the loopback trading (trade simulator) boolean.. */
                    posToStart.setTradeLoopback(pLoopbackTrading);
                    /* now add these adjustments to position *///wfs
                    posToStart.staLongShares = longShares;
                    posToStart.staShortShares = shortShares;
                    /* order important here. Do option first this will end up
                     * as an order first before the stock.
                     */
                    actEditPos = new positionEditor(posToStart);
                    actEditPos.editAdjData.setAdjDate(posToStart.getPosDate());
                    actEditPos.startOutPosition(0, shortShares, biasLS);
                    
                    actEditPos = new positionEditor(posToStart);
                    actEditPos.editAdjData.setAdjDate(posToStart.getPosDate());
                    actEditPos.startOutPosition(longShares, 0, biasLS);
                    /* create a volatility monitor for this new position */
                    posToStart.setVm(new volatilityMonitor(positionStorage));
                    positionStorage.setDefVm(posToStart.getVm());

                    
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            float tmp1f;
            int extraShares;
            try {
                completed = false;
                if (event.equals(ftf[0])) {
                    longShares = Integer.parseInt(event.getText());
                }
            } catch (Exception ef) {
                //ef.printStackTrace();
                commonGui.prMsg("Incorrect Numeric Input. Try Again.");
            }
            if (longShares > 0) {
                /* do the calculations now 
                 * fist the bias Long/short.
                 * then # of contracts to to become
                 * nuetral.
                 */
                biasLS = (float) (((float) posToStart.shortShares * 100.0 * -1.0) + (float) longShares);
                
                if(isFiller1Enabled() == true) {
                    /* filler 1 is meant to start things out closer to nuetral so ..
                       figure out whole number of contracts first, the fraction (extra short shares)
                     * are lost here.
                     */

    
                    shortShares = Math.round(((float)longShares / posToStart.shortDelta) / (float)100.0);
                    extraShares = (Math.round(shortShares * posToStart.shortDelta * 100) * -1) + longShares;
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
                    shortShares = Math.round(((float)longShares / posToStart.shortDelta) / (float)100.0);
                }                

            }
            this.ftf[1].setText(String.valueOf(shortShares));
            this.ftf[2].setText(String.valueOf(biasLS));
            this.ftf[0].setText(String.valueOf(longShares));
        }

        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                
                completed = false;

                if (command.equals("Loopback Trading")) {
                    pLoopbackTrading = jbLoopbackTrading.isSelected();
                    System.out.println("pLoopbackTrading = " + pLoopbackTrading);
                }

            }
        }        
        
        public void itemStateChanged(ItemEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());

        }

        public void processKeyEvent(KeyEvent ke) {
            int key = ke.getID();
            System.out.println("key:" + key);
        }

        void setPosData(positionData pd) {
            posToStart = pd;
        }

        boolean isFiller1Enabled() {
            if (positionConfig.getActConfig() != null) {
                return (positionConfig.getActConfig().getConfigFiller1());
            }
            return (false);
        }
        
        
    }

    private class startUpPosition {

        private startPosition staPos;

        private startUpPosition(positionData pos) {
            JFrame f = new JFrame("Startup Position ");
            f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            staPos = new startPosition(pos);
            f.getContentPane().add(new JButton("yes?"));

            f.setContentPane(staPos);
            f.setSize(200, 200);
            f.pack();
            f.setVisible(true);

        }

        int getNewShortShares() {
            return (staPos.shortShares);
        }

        int getNewLongShares() {
            return (staPos.longShares);
        }
    }


}
