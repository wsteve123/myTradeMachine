/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.awt.Choice;
import java.awt.event.*;
import java.text.ParseException;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author walterstevenson
 */
public class volatilityAveragingIn {
    private dispIvAveIn actDisp = null;
    private ivAveIn newIvAveIn;
    private boolean ivAveIn = false;
    private double ivAveInPercentDrop = 0;
    private int ivAveInMaxAdjustments = 0;
    private positionData actPos = null;
    private positions actAllPos = null;
    private String[] tickerList;
    private Choice posList;
    private boolean editMade = false;
    
    public boolean wasEditMade() {
        return editMade;
    }
    /* do one position */
    public volatilityAveragingIn(positionData posToChange) {
        editMade = false;
        if (posToChange != null) {
            actPos = posToChange;

            ivAveIn = actPos.ivAveIn;
            ivAveInPercentDrop = actPos.ivAveInPrecentDrop;
            ivAveInMaxAdjustments = actPos.ivAveInMaxAdjustments;
                    
            //newIvAveIn = new ivAveIn();
            actDisp = new dispIvAveIn("Position Implied Volatility Average IN Control");
    
        }else {
            commonGui.postInformationMsg("posToChange is null!!");
            System.out.println("posToChange is NULL!!");
        }
    }
    /* do all positions */
    public volatilityAveragingIn(positions allPositions) {
        editMade = false;
        if (allPositions != null) {
            actAllPos = allPositions;
            
            tickerList = new String[actAllPos.posDataVecSize()];
            fillTickerList(actAllPos); 
            actDisp = new dispIvAveIn("Position Implied Volatility Average IN Control");

        }else {
            commonGui.postInformationMsg("posToChange is null!!");
            System.out.println("posToChange is NULL!!");
        }
    }
    
    
    class dispIvAveIn extends JFrame {

        JTable jt = null;
        JScrollPane pane = null;
        

        dispIvAveIn(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    System.out.println("dispGainLoss: window closing...");
                    if((editMade == true ) && (actAllPos != null)) {
                        actAllPos.posDataSaveToDisk(actAllPos.getPositionFileName(), false /* prompt? */);
                        System.out.println("updated "+actAllPos.getPositionFileName()+" file with new per position configuration change.");
                    }
                    setVisible(false);
                    dispose();
                }
            });
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            newIvAveIn = new ivAveIn();

            setContentPane(newIvAveIn);
            setSize(200, 200);
            pack();
            setVisible(true);
        }
    }
    
    
    
    private void fillTickerList(positions posin) {
        posin.semTake();
        positionData actPos = posin.posDataFetchNext(true);
        for (int idx = 0; actPos != null; actPos = posin.posDataFetchNext(false), idx++) {
            tickerList[idx] = actPos.longTicker;
        }
        posin.semGive();
    }
    
    private void getPositionConfigValues(positionData posin) {
        ivAveIn = posin.ivAveIn;
        ivAveInPercentDrop = posin.ivAveInPrecentDrop;
        ivAveInMaxAdjustments = posin.ivAveInMaxAdjustments;               
    }
    
    private class ivAveIn extends JPanel implements ActionListener, ItemListener {

        public boolean completed = false;
        final int AVE_IN_PERCENT = 0, AVE_IN_MAX_ADJ = 1, LAST_ELEMENT = 2;
        private JFormattedTextField ftf[] = new JFormattedTextField[LAST_ELEMENT];
        String des[] = new String[ftf.length]; // description of each field
        JCheckBox ivAveragingEnable;
        
        public void prAveInPercentDrop(double gl){
            if(ivAveIn == true){
                ftf[AVE_IN_PERCENT].setEnabled(true);
            }else {
                ftf[AVE_IN_PERCENT].setEnabled(false);   
            }
            ftf[AVE_IN_PERCENT].setText(Double.toString(gl * 100));    
        }
        
        public void prAveInMaxAdj(int sl){
            if(ivAveIn == true) {
                ftf[AVE_IN_MAX_ADJ].setEnabled(true);
            }else {
                ftf[AVE_IN_MAX_ADJ].setEnabled(false);   
            }
            ftf[AVE_IN_MAX_ADJ].setText(Integer.toString(sl));    
        }
        private ivAveIn() {
            /* only do the position selector if we have all positions */
            if (actAllPos != null) {
                Label posLable = new Label("Positions:");
                posList = new Choice();

                add(posLable);
                add(posList);
                for (int ix = 0; ix < tickerList.length; ix++) {
                    posList.add(tickerList[ix]);
                }
                /*
                 * connect a listener for position selection from the user..
                 */
                posList.addItemListener(new positionSelListener());
                if (tickerList.length > 0) {
                    actPos = actAllPos.posDataSearch(posList.getSelectedItem());
                    System.out.println("selPosition Ticker is: " + actPos.longTicker);
                    getPositionConfigValues(actPos);
                }
            }
            
            ivAveragingEnable = new JCheckBox("Average In");
            ivAveragingEnable.setMnemonic(KeyEvent.VK_C);
            /* enable individual pos control only if global is turned off! 
             * if you want to change this value, you need to turn off the global first.
             */
            ivAveragingEnable.setEnabled(true /*actPos.ivAveIn*/);
            ivAveragingEnable.setSelected(ivAveIn);
            checkListener checkHandle = new checkListener();
            ivAveragingEnable.addActionListener(checkHandle);
            this.add(ivAveragingEnable);


            des[AVE_IN_PERCENT] = "When IV drops (in %)";
            this.ftf[AVE_IN_PERCENT] = new JFormattedTextField();
            if (ivAveIn == true){
                this.ftf[AVE_IN_PERCENT].setEnabled(true);
            }else {
                this.ftf[AVE_IN_PERCENT].setEnabled(false);
            }
            this.ftf[AVE_IN_PERCENT].setColumns(des[AVE_IN_PERCENT].length() - 4);
            this.ftf[AVE_IN_PERCENT].addActionListener(this);
            this.ftf[AVE_IN_PERCENT].setText(Double.toString(ivAveInPercentDrop * 100));

            des[AVE_IN_MAX_ADJ] = "For maximum Adjustments";
            this.ftf[AVE_IN_MAX_ADJ] = new JFormattedTextField();
            if (ivAveIn == true){
                this.ftf[AVE_IN_MAX_ADJ].setEnabled(true);
            }else {
                this.ftf[AVE_IN_MAX_ADJ].setEnabled(false);
            }
            this.ftf[AVE_IN_MAX_ADJ].setColumns(des[AVE_IN_MAX_ADJ].length() - 4);
            this.ftf[AVE_IN_MAX_ADJ].addActionListener(this);
            this.ftf[AVE_IN_MAX_ADJ].setText(Integer.toString(ivAveInMaxAdjustments));
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
        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();

                completed = false;
                if (command.equals("Average In")) {
                    ivAveIn = ivAveragingEnable.isSelected();
                    
                    System.out.println("ivAveragingEnable = " + ivAveIn);
                    if (ivAveIn == true) {
                        newIvAveIn.ftf[AVE_IN_PERCENT].setEnabled(true);
                    } else {
                        newIvAveIn.ftf[AVE_IN_PERCENT].setEnabled(false);
                    }
        
                    if (ivAveIn == true) {
                        newIvAveIn.ftf[AVE_IN_MAX_ADJ].setEnabled(true);
                    } else {
                        newIvAveIn.ftf[AVE_IN_MAX_ADJ].setEnabled(false);
                    }
                    
                }
            }
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
                        
                        ivAveInPercentDrop = Double.parseDouble(ftf[AVE_IN_PERCENT].getText());
                        ivAveInPercentDrop = ivAveInPercentDrop / 100;
                        ivAveInMaxAdjustments = Integer.parseInt(ftf[AVE_IN_MAX_ADJ].getText());

                    } catch (Exception ef) {
                        //ef.printStackTrace();
                        commonGui.prMsg("Incorrect Numeric Input. Try Again.");
                    }
                    completed = true;
                    actPos.ivAveIn = ivAveIn;
                    actPos.ivAveInPrecentDrop = ivAveInPercentDrop;
                    actPos.ivAveInMaxAdjustments = ivAveInMaxAdjustments;
                    editMade = true;

                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            

            try {
                completed = false;
                if (event.equals(ftf[AVE_IN_PERCENT])) {
                    ftf[AVE_IN_PERCENT].commitEdit();
                    ivAveInPercentDrop = Double.parseDouble(event.getText());
                } else if (event.equals(ftf[AVE_IN_MAX_ADJ])) {

                    ftf[AVE_IN_MAX_ADJ].commitEdit();
                    
                    
                    ivAveInMaxAdjustments = Integer.parseInt(event.getText());
                    event.commitEdit();                    

                }
            } catch (Exception ef) {
                //ef.printStackTrace();
                ef.printStackTrace();
                commonGui.prMsg("Incorrect Numeric Input. Try Again." + ef);
            }

        }
        public void itemStateChanged(ItemEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());

        }

        public void processKeyEvent(KeyEvent ke) {
            int key = ke.getID();
            System.out.println("key:" + key);
        }
        
        private class positionSelListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
                String selPos = posList.getSelectedItem();
                actPos = actAllPos.posDataSearch(selPos);
                System.out.println("you selected: " + selPos);
                getPositionConfigValues(actPos);
                
                prAveInPercentDrop(ivAveInPercentDrop);
                prAveInMaxAdj(ivAveInMaxAdjustments);                              
                ivAveragingEnable.setSelected(ivAveIn);
                
            }
        }
    }
}
