/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;

/**
 *
 * @author walterstevenson
 */
public class perPositionConfig {

    private dispGainLoss actDisp = null;
    private String[] tickerList;
    private float pGainLock;
    private float pStopLoss;
    private boolean pControlEnable;
    private boolean pLoopbackTrading;
    private positions actPositions;
    private positionData selPosition;
    private Choice posList;
    private JFormattedTextField ftf[];
    /* this is the positions global control over gain loss feature. 
     * if global is enabled then individual gain/loss control is allowed.
     * if global is disabled then individual gain/loss controlis not allowed.
     */
    private boolean pGlobalControlEnable;
    private boolean pGlobalLoopbackTrading;
    private boolean editMade = false;
    public perPositionConfig(positions posin) {
        actPositions = posin;
        editMade = false;
        if (posin.posDataEmpty()) {
            commonGui.postInformationMsg("Position folder empty.");

        }else {
             /* global gain control is stored in positionConfig not the position folder */
            pGlobalControlEnable = positionConfig.getActConfig().getStopLossLockGain();
            pGlobalLoopbackTrading = positionConfig.getActConfig().getTradeLoopback();
            tickerList = new String[posin.posDataVecSize()];
            fillTickerList(posin);
            actDisp = new dispGainLoss("Position Gain/Loss Control");
            
           
        }
        
    }

    private void fillTickerList(positions posin) {
        posin.semTake();
        positionData actPos = posin.posDataFetchNext(true);
        for(int idx = 0; actPos != null ; actPos = posin.posDataFetchNext(false), idx++) {
            tickerList[idx] = actPos.longTicker;
        }
        posin.semGive();
    }
    private void getPositionConfigValues(positionData posin) {
        pGainLock = posin.getGainLock();
        pStopLoss = posin.getStopLoss();
        pControlEnable = posin.getGainLockControl();
        pLoopbackTrading = posin.getTradeLoopback();
    }
    private void setPositionConfigValues(positionData posin) {
        /* the only way to save anything is here, after a change is made and the
         * done button is pressed. It then goes here. So mark as change. 
         * Note: If the done button is
         * pressed it will count as an edit, even if the user didn't change anything.
         */
        editMade = true;
        posin.setGainLock(pGainLock);
        posin.setStopLoss(pStopLoss);
        posin.setGainLockControl(pControlEnable);
        posin.setTradeLoopback(pLoopbackTrading);
        
    }
    
    class dispGainLoss extends JFrame {

        JTable jt = null;
        JScrollPane pane = null;
        getParamsInfo getParms;

        dispGainLoss(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    System.out.println("dispGainLoss: window closing...");
                    if(editMade == true ) {
                        actPositions.posDataSaveToDisk(actPositions.getPositionFileName(), false /* prompt? */);
                        System.out.println("updated "+actPositions.getPositionFileName()+" file with new per position configuration change.");
                    }
                    setVisible(false);
                    dispose();
                }
            });
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            getParms = new getParamsInfo();

            setContentPane(getParms);
            setSize(200, 200);
            pack();
            setVisible(true);
        }
    }



    class getParamsInfo extends JPanel implements ActionListener {

        public boolean completed = false;
        JCheckBox jbControlEnable;
        JCheckBox jbLoopbackTrading;
        
        /* Allow individual position control only if the global folder (all positions) 
         * enable is turned off. If global is on, then it over rides all positions control.
         */
        public void prGainLock(float gl){
            if((pControlEnable == true) && (pGlobalControlEnable == false)) {
                ftf[0].setEnabled(true);
            }else {
                ftf[0].setEnabled(false);   
            }
            ftf[0].setText(Float.toString(gl));    
        }
        public void prStopLoss(float sl){
            if((pControlEnable == true) && (pGlobalControlEnable == false)) {
                ftf[1].setEnabled(true);
            }else {
                ftf[1].setEnabled(false);   
            }
            ftf[1].setText(Float.toString(sl));    
        }

        
        getParamsInfo() {
            
            Label posLable = new Label("Positions:");
            posList = new Choice();
            
            add(posLable);
            add(posList);
            for (int ix = 0; ix < tickerList.length; ix++) {
                posList.add(tickerList[ix]);
            }
            /* connect a listener for position selection from the user.. */
            posList.addItemListener(new positionSelListener());
            if(tickerList.length > 0) {
                selPosition = actPositions.posDataSearch(posList.getSelectedItem());
                System.out.println("selPosition Ticker is: "+selPosition.longTicker);
                getPositionConfigValues(selPosition);
            }
            ftf = new JFormattedTextField[2];
            String des[] = new String[ftf.length]; // description of each field


            des[0] = "Gain Lock Value (%)";
            ftf[0] = new JFormattedTextField();
            if((pControlEnable == true) && (pGlobalControlEnable == false)) {
                ftf[0].setEnabled(true);
            }else {
                ftf[0].setEnabled(false);   
            }
            ftf[0].setColumns(des[0].length() - 4);
            ftf[0].setSize(5, 2);
            ftf[0].addActionListener(this);
            ftf[0].setText(Float.toString(pGainLock));

            des[1] = "Stop Loss Value (%)";
            ftf[1] = new JFormattedTextField();
            if((pControlEnable == true) && (pGlobalControlEnable == false)) {
                ftf[1].setEnabled(true);
            }else {
                ftf[1].setEnabled(false);   
            }
            ftf[1].setColumns(des[1].length() - 4);
            ftf[1].setSize(5, 2);
            ftf[1].addActionListener(this);
            ftf[1].setText(Float.toString(pStopLoss));
            
            jbControlEnable = new JCheckBox("Enable Control");
            jbControlEnable.setMnemonic(KeyEvent.VK_C);
            /* enable individual pos control only if global is turned off! 
             * if you want to change this value, you need to turn off the global first.
             */
            jbControlEnable.setEnabled(!pGlobalControlEnable);
            jbControlEnable.setSelected(pControlEnable);
            checkListener checkHandle = new checkListener();
            jbControlEnable.addActionListener(checkHandle);
            this.add(jbControlEnable);
            
            jbLoopbackTrading = new JCheckBox("Loopback Trading");
            jbLoopbackTrading.setMnemonic(KeyEvent.VK_C);
            /* enable individual pos control only if global is turned off! 
             * if you want to change this value, you need to turn off the global first.
             */
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

        } /* getParamsInfo */

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
                        pGainLock = Float.parseFloat(ftf[0].getText());
                        pStopLoss  = Float.parseFloat(ftf[1].getText());

                    } catch (Exception pe) {
                        commonGui.prMsg("Incorrect String Input. Try again."+pe);
                    }
                    completed = true;
                    setPositionConfigValues(selPosition);
                    
                    System.out.println("pGainLock = " + pGainLock + " pStopLoss = " + pStopLoss +"pLoopbackTrading = " + pLoopbackTrading
                                        );

                }
            }
        }

        private class defaultListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Set Defaults")) {

                }
            }
        }

        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                
                completed = false;
                if (command.equals("Enable Control")) {
                    pControlEnable = jbControlEnable.isSelected();
                    System.out.println("pControlEnable = " + pControlEnable);
                    if(pControlEnable == true) {
                        ftf[0].setEnabled(true);
                        ftf[1].setEnabled(true);
                    }else {
                        ftf[0].setEnabled(false);
                        ftf[1].setEnabled(false);
                        
                    }

                }else 
                    if (command.equals("Loopback Trading")) {
                        pLoopbackTrading = jbLoopbackTrading.isSelected();
                        System.out.println("pLoopbackTrading = " + pLoopbackTrading);
                    }
                
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            try {
                completed = false;
                if (event.equals(ftf[0])) {
                    pGainLock = Float.parseFloat(event.getText());
                } else if (event.equals(ftf[1])) {
                    pStopLoss = Float.parseFloat(event.getText());
                } 
                

            } catch (Exception pe) {
                commonGui.prMsg("Incorrect String Input. Try again.");
            }
        }
        
        private class positionSelListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
                String selPos = posList.getSelectedItem();
                selPosition = actPositions.posDataSearch(selPos);
                System.out.println("you selected: " + selPos);
                getPositionConfigValues(selPosition);
                prGainLock(pGainLock);
                prStopLoss(pStopLoss);
                jbControlEnable.setSelected(pControlEnable);
                jbLoopbackTrading.setSelected(pLoopbackTrading);
            }
        }
      
    }

}