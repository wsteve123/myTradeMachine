/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;
import java.io.*;
import java.awt.event.ItemEvent;
import java.text.ParseException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import tradeMenus.globalConfigDialogForm;

/**
 *
 * @author walterstevenson
 */
public class  positionConfig {
    
    private final int defTradeDays = 20;
    private final int defStockMove = 5;
    private final int defStockMoveOneTime = 3;
    private final boolean defFiller = false;
    private final int defPollFrequency = 20; /* seconds */
    private final boolean defOneDayTrade = false;
    private final String defTradeTime = "12:55:00:PM";
    private final boolean defAutoTrade = true;
    private final boolean defStopLossLockGain = false;
    private final float defStopLoss = (float)1.0;
    private final float defLockGain = (float) 1.5;
    private final boolean defTradeLoopback = false;
    private int pTradeDays = defTradeDays;
    private int pStockMove = defStockMove;
    private boolean pFiller = defFiller;
    private boolean pFiller1 = defFiller;
    private int pPollFrequency = defPollFrequency;
    private boolean pOneDayTrade = defOneDayTrade;
    private String pTradeTime = defTradeTime;
    private int pStockMoveOneTime = defStockMoveOneTime;
    private boolean pAutoTrade = defAutoTrade;
    private boolean pStopLossLockGain = defStopLossLockGain;
    private float pStopLoss = defStopLoss;
    private float pLockGain = defLockGain;
    private boolean pTradeLoopback = defTradeLoopback;
    private dispParams dptp;
    private String positionsFilename = "";
    private static configDataToSave pConfigData = null;
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    private final String posConfigFilename = homeDirectory + "posConfigParams.dat";
    private static positionConfig actConfig = null;
    private JFormattedTextField ftf[];
    
    public positionConfig(boolean init) {
        if (actConfig != null) {
            pTradeDays = actConfig.pTradeDays;
            pStockMove = actConfig.pStockMove;
            pStockMoveOneTime = actConfig.pStockMoveOneTime;
            pFiller = actConfig.pFiller;
            pFiller1 = actConfig.pFiller1;
            pPollFrequency = actConfig.pPollFrequency;
            pOneDayTrade = actConfig.pOneDayTrade;
            pTradeTime = actConfig.pTradeTime;
            pAutoTrade = actConfig.pAutoTrade;
            pStopLossLockGain = actConfig.pStopLossLockGain;
            pStopLoss = actConfig.pStopLoss;
            pLockGain = actConfig.pLockGain;
            pTradeLoopback = actConfig.pTradeLoopback;
            
        }else{
            actConfig = this;
        }
        if (pConfigData == null) {
            pConfigData = new configDataToSave();
            configDataRetrieveIt();
            configDataCopyToRetrieve();
        }
        if (!init) {
//            globalConfigDialogForm globalConfig = new globalConfigDialogForm(new javax.swing.JFrame(), true);
//            globalConfig.setVisible(true);
            dptp = new dispParams("Global Configuration Settings");
        }

    }
    public void setPositionsFilename(String fn) {
        positionsFilename = fn;
    }
    public static positionConfig getActConfig() {
        return(actConfig);
    }
    public int getConfigStockMove() {
        return (pStockMove);
    }
    public int getConfigStockMoveOneTime() {
        return (pStockMoveOneTime);
    }
    public int getConfigTradeDays() {
        return (pTradeDays);
    }
    public boolean getConfigFiller() {
        return (pFiller);
    }
    public boolean getConfigFiller1() {
        return (pFiller1);
    }
    public int getConfigPollFrequency() {
        return (pPollFrequency);
    }
    public boolean isOneDayTradeSet() {
        return(pOneDayTrade);
    }
    public String getTradeTime() {
        return(pTradeTime);
    }
    public boolean getAutoTrade(){
        return pAutoTrade;
    }
    public boolean getStopLossLockGain(){
        return pStopLossLockGain;
    }
    public float getStopLoss() {
        return pStopLoss;
    }
    public float getLockGain() {
        return pLockGain;
    }
    public boolean getTradeLoopback() {
        return pTradeLoopback;
    }
    private void configDataSaveIt() {

        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(posConfigFilename);
            out = new ObjectOutputStream(fos);
            out.writeObject(pConfigData);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    private void configDataRetrieveIt() {

        //pConfigData = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(posConfigFilename);
            in = new ObjectInputStream(fis);
            pConfigData = (configDataToSave) in.readObject();
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    private void configDataCopyToRetrieve() {
        pTradeDays = pConfigData.sTradeDays;
        pStockMove = pConfigData.sStockMove;
        pStockMoveOneTime = pConfigData.sStockMoveOneTime;
        pFiller = pConfigData.sFiller;
        pFiller1 = pConfigData.sFiller1;
        pPollFrequency = pConfigData.sPollFrequency;
        pOneDayTrade = pConfigData.sOneDayTrade;
        pTradeTime = pConfigData.sTradeTime;
        pAutoTrade = pConfigData.sAutoTrade;
        pStopLossLockGain = pConfigData.sStopLossLockGain;
        pStopLoss = pConfigData.sStopLoss;
        pLockGain = pConfigData.sLockGain;
        pTradeLoopback = pConfigData.sTradeLoopback;
    }
    private void configDataCopyToSave () {
        actConfig.pTradeDays = pConfigData.sTradeDays = pTradeDays;
        actConfig.pStockMove = pConfigData.sStockMove = pStockMove;
        actConfig.pStockMoveOneTime = pConfigData.sStockMoveOneTime = pStockMoveOneTime;
        actConfig.pFiller = pConfigData.sFiller = pFiller;
        actConfig.pFiller1 = pConfigData.sFiller1 = pFiller1;
        actConfig.pPollFrequency = pConfigData.sPollFrequency = pPollFrequency;
        actConfig.pOneDayTrade = pConfigData.sOneDayTrade = pOneDayTrade;
        actConfig.pTradeTime = pConfigData.sTradeTime = pTradeTime;
        actConfig.pAutoTrade = pConfigData.sAutoTrade = pAutoTrade;
        actConfig.pStopLossLockGain = pConfigData.sStopLossLockGain = pStopLossLockGain;
        actConfig.pLockGain = pConfigData.sLockGain = pLockGain;
        actConfig.pStopLoss = pConfigData.sStopLoss = pStopLoss;
        actConfig.pTradeLoopback = pConfigData.sTradeLoopback = pTradeLoopback;
    }
    private static class configDataToSave implements Serializable {
        configDataToSave() {
            
        }
        private int sTradeDays;
        private int sStockMove;
        private int sStockMoveOneTime;
        private boolean sFiller;
        private boolean sFiller1;
        private int sPollFrequency;
        private boolean sOneDayTrade;
        private String sTradeTime;
        private boolean sAutoTrade;
        private boolean sStopLossLockGain;
        private float sStopLoss;
        private float sLockGain;
        private boolean sTradeLoopback;
    }
    class dispParams extends JFrame {

        JTable jt = null;
        JScrollPane pane = null;
        getDnParamsInfo getDnParms;

        dispParams(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    setVisible(false);
                    dispose();
                }
            });
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            getDnParms = new getDnParamsInfo();

            setContentPane(getDnParms);
            setSize(200, 200);
            pack();
            setVisible(true);
        }
    }

    class getDnParamsInfo extends JPanel implements ActionListener {

        public boolean completed = false;
        JCheckBox jbFiller;
        JCheckBox jbFiller1;
        JCheckBox jbOneDayTrade;
        JCheckBox jbAutoTrade;
        JCheckBox jbStopLossLockGain;
        JCheckBox jbTradeLoopback;
       
        public void prTradeDays(int td){
            ftf[0].setText(Integer.toString(td));    
        }
        public void prStockMove(int sm){
            ftf[1].setText(Integer.toString(sm));    
        }

        public void prPollFrequency(int pf) {
            ftf[2].setText(Integer.toString(pf));  
        }
        public void prTradeTime(String tt) {
            ftf[3].setText(tt);  
        }
        public void prStockMoveOneTime(int sm) {
            ftf[4].setText(Integer.toString(sm));
        }
        public void prStopLoss(float sl) {
            ftf[5].setText(Float.toString(sl));
        }
        public void prLockGain(float lg) {
            ftf[6].setText(Float.toString(lg));
        }
        getDnParamsInfo() {


            ftf = new JFormattedTextField[7];
            String des[] = new String[ftf.length]; // description of each field


            des[0] = "Number Of Trade Days";
            ftf[0] = new JFormattedTextField();
            ftf[0].setEnabled(true);
            ftf[0].setColumns(des[0].length() - 4);
            ftf[0].setSize(5, 2);
            ftf[0].addActionListener(this);
            ftf[0].setText(Integer.toString(pTradeDays));

            des[1] = "Stock Move Trigger (%)";
            ftf[1] = new JFormattedTextField();
            ftf[1].setEnabled(true);
            ftf[1].setColumns(des[1].length() - 4);
            ftf[1].setSize(5, 2);
            ftf[1].addActionListener(this);
            ftf[1].setText(Integer.toString(pStockMove));

            des[2] = "Poll Frequency (seconds)";
            ftf[2] = new JFormattedTextField();
            ftf[2].setEnabled(true);
            ftf[2].setColumns(des[1].length() - 4);
            ftf[2].setSize(5, 2);
            ftf[2].addActionListener(this);
            ftf[2].setText(Integer.toString(pPollFrequency));            
            
            des[3] = "Trade Time (format: 12:45:PM)";
            ftf[3] = new JFormattedTextField();
            if(pOneDayTrade == true) {
                ftf[3].setEnabled(true);
            }else {
                ftf[3].setEnabled(false);   
            }
            ftf[3].setColumns(des[3].length() - 4);
            ftf[3].setSize(5, 2);
            ftf[3].addActionListener(this);
            ftf[3].setText(pTradeTime); 
            
            des[4] = "Stock Move One Time Trigger (%)";
            ftf[4] = new JFormattedTextField();
            if(pOneDayTrade == true) {
                ftf[4].setEnabled(true);
            }else {
                ftf[4].setEnabled(false);    
            }
            ftf[4].setColumns(des[1].length() - 4);
            ftf[4].setSize(5, 2);
            ftf[4].addActionListener(this);
            ftf[4].setText(Integer.toString(pStockMoveOneTime));
            
            des[5] = "Stop Loss Trigger (%)";
            ftf[5] = new JFormattedTextField();
            if(pStopLossLockGain == true) {
                ftf[5].setEnabled(true);
            }else {
                ftf[5].setEnabled(false);    
            }
            ftf[5].setColumns(des[5].length() - 4);
            ftf[5].setSize(5, 2);
            ftf[5].addActionListener(this);
            ftf[5].setText(Float.toString(pStopLoss));
            
            des[6] = "Lock Gain Trigger (%)";
            ftf[6] = new JFormattedTextField();
            if(pStopLossLockGain == true) {
                ftf[6].setEnabled(true);
            }else {
                ftf[6].setEnabled(false);    
            }
            ftf[6].setColumns(des[6].length() - 4);
            ftf[6].setSize(5, 2);
            ftf[6].addActionListener(this);
            ftf[6].setText(Float.toString(pLockGain));
            
            jbFiller = new JCheckBox("Filler Enable");
            jbFiller.setMnemonic(KeyEvent.VK_C);
            jbFiller.setSelected(pFiller);
            checkListener checkHandle = new checkListener();
            jbFiller.addActionListener(checkHandle);
            this.add(jbFiller);
            
            jbFiller1 = new JCheckBox("Filler1 Enable");
            jbFiller1.setMnemonic(KeyEvent.VK_C); 
            jbFiller1.setSelected(pFiller1);
            checkListener checkHandleF1 = new checkListener();
            jbFiller1.addActionListener(checkHandleF1);
            this.add(jbFiller1);
            
            jbOneDayTrade = new JCheckBox("One-Time Trade Enable");
            jbOneDayTrade.setMnemonic(KeyEvent.VK_C); 
            jbOneDayTrade.setSelected(pOneDayTrade);
            checkListener checkHandleOneDayTrade = new checkListener();
            jbOneDayTrade.addActionListener(checkHandleOneDayTrade);
            this.add(jbOneDayTrade);
            
            jbAutoTrade = new JCheckBox("Automatic Trade");
            jbAutoTrade.setMnemonic(KeyEvent.VK_C); 
            jbAutoTrade.setSelected(pAutoTrade);
            checkListener checkHandleAutoTrade = new checkListener();
            jbAutoTrade.addActionListener(checkHandleOneDayTrade);
            this.add(jbAutoTrade);
            
            jbStopLossLockGain = new JCheckBox("StopLoss/LockGain Enable");
            jbStopLossLockGain.setMnemonic(KeyEvent.VK_C); 
            jbStopLossLockGain.setSelected(pStopLossLockGain);
            checkListener checkHandleStopLossLockGain = new checkListener();
            jbStopLossLockGain.addActionListener(checkHandleStopLossLockGain);
            this.add(jbStopLossLockGain);
            
            jbTradeLoopback = new JCheckBox("Trade Looback Enable");
            jbTradeLoopback.setMnemonic(KeyEvent.VK_C); 
            jbTradeLoopback.setSelected(pTradeLoopback);
            checkListener checkHandleTradeLoopback = new checkListener();
            jbTradeLoopback.addActionListener(checkHandleTradeLoopback);
            this.add(jbTradeLoopback);

            JButton defaultButton = new JButton("Set Defaults");
            defaultListener defHandle = new defaultListener();
            defaultButton.addActionListener(defHandle);

            add(defaultButton);


            // jb.addActionListener(this);


            // add each ftf[] to a BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            for (int j = 0; j < ftf.length; j += 1) {
                JPanel borderPanel = new JPanel(new java.awt.BorderLayout());
                borderPanel.setBorder(new javax.swing.border.TitledBorder(des[j]));
                borderPanel.add(ftf[j], java.awt.BorderLayout.CENTER);
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
                        pTradeDays = Integer.parseInt(ftf[0].getText());
                        pStockMove = Integer.parseInt(ftf[1].getText());
                        pPollFrequency = Integer.parseInt(ftf[2].getText());
                        pTradeTime = ftf[3].getText();
                        pStockMoveOneTime = Integer.parseInt(ftf[4].getText());
                        pStopLoss = Float.parseFloat(ftf[5].getText());
                        pLockGain = Float.parseFloat(ftf[6].getText());

                    } catch (Exception pe) {
                        commonGui.prMsg("Incorrect String Input. Try again."+pe);
                    }
                    completed = true;
                    configDataCopyToSave();
                    configDataSaveIt();
                    System.out.println("pTradeDays = " + pTradeDays + " pStockMoves = " + pStockMove + "pollFrequency = "+ 
                                        pPollFrequency + "pTradeTime" + pTradeTime + "pStockMoveOneTime" + pStockMoveOneTime +
                                        "pStopLossLockGain = "+ pStopLossLockGain + "pStopLoss = " + pStopLoss + "pLockGain = " + pLockGain +
                                        "pTradeLoopback = "+ pTradeLoopback
                                        );

                }
            }
        }

        private class defaultListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Set Defaults")) {
                    pStockMove = defStockMove;
                    pStockMoveOneTime = defStockMoveOneTime;
                    pTradeDays = defTradeDays;
                    pFiller = defFiller;
                    pFiller1 = defFiller;
                    pOneDayTrade = defOneDayTrade;
                    pTradeTime = defTradeTime;
                    pAutoTrade = defAutoTrade;
                    pPollFrequency = defPollFrequency;
                    pStopLossLockGain = defStopLossLockGain;
                    pStopLoss = defStopLoss;
                    pLockGain = defLockGain;
                    pTradeLoopback = defTradeLoopback;
                    dptp.getDnParms.prStockMove(pStockMove);
                    dptp.getDnParms.prTradeDays(pTradeDays);
                    dptp.getDnParms.prPollFrequency(pPollFrequency);
                    dptp.getDnParms.prTradeTime(pTradeTime);
                    dptp.getDnParms.prStockMoveOneTime(pStockMoveOneTime);
                    dptp.getDnParms.prStopLoss(pStopLoss);
                    dptp.getDnParms.prLockGain(pLockGain);
                    
                    
                    System.out.println("pTradeDays = " + pTradeDays + " pStockMoves = " + pStockMove + "pollFrequency = "+ 
                                        pPollFrequency + "pTradeTime" + pTradeTime + "pStockMoveOneTime" + pStockMoveOneTime +
                                        "pStopLoss = " + pStopLoss + "pLockGain = " + pLockGain
                                        );

                }
            }
        }

        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Filler Enable")) {
                    pFiller = jbFiller.isSelected();
                    System.out.println("pFiller = " + pFiller);

                }else
                if (command.equals("Filler1 Enable")) {
                    pFiller1 = jbFiller1.isSelected();
                    System.out.println("pFiller1 = "+pFiller1);
                    
                }else
                if (command.equals("One-Time Trade Enable")) {
                    pOneDayTrade = jbOneDayTrade.isSelected();
                    System.out.println("pOneDayTrade = "+pOneDayTrade);
                    
                }else
                if (command.equals("Automatic Trade")) {
                    pAutoTrade = jbAutoTrade.isSelected();
                    System.out.println("pAutoTrade = "+pAutoTrade);
                    
                }else
                if (command.equals("StopLoss/LockGain Enable")) {
                    pStopLossLockGain = jbStopLossLockGain.isSelected();
                    System.out.println("pStopLossLockGain = "+pStopLossLockGain);
                    if(pStopLossLockGain == true) {
                        ftf[5].setEnabled(true);
                    }else {
                        ftf[5].setEnabled(false);    
                    }
                    if(pStopLossLockGain == true) {
                        ftf[6].setEnabled(true);
                    }else {
                        ftf[6].setEnabled(false);    
                    }
                    
                }else
                if (command.equals("Trade Looback Enable")) {
                    pTradeLoopback = jbTradeLoopback.isSelected();
                    System.out.println("pTradeLoopback = "+pTradeLoopback);
                }
                
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            try {
                completed = false;
                if (event.equals(ftf[0])) {
                    pTradeDays = Integer.parseInt(event.getText());
                } else if (event.equals(ftf[1])) {
                    pStockMove = Integer.parseInt(event.getText());
                } else if (event.equals(ftf[2])) {
                    pPollFrequency = Integer.parseInt(event.getText());
                }else if (event.equals(ftf[3])) {
                    pTradeTime = event.getText();
                }else if (event.equals(ftf[4])) {
                    pStockMoveOneTime = Integer.parseInt(event.getText());
                }else if (event.equals(ftf[5])) {
                    pStopLoss = Float.parseFloat(event.getText());
                }else if (event.equals(ftf[6])) {
                    pLockGain = Float.parseFloat(event.getText());
                }
                

            } catch (Exception pe) {
                commonGui.prMsg("Incorrect String Input. Try again.");
            }
        }

        public void itemStateChanged(ItemEvent e) {
        }
    }
}
