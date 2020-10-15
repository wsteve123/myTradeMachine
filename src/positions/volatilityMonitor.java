/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Walter Stevenson
 * Used to keep track of implied volatility.
 */
public class volatilityMonitor {

    private volatilityMonitor defVm;
   
    private static final int NUMBER_OF_DAYS_TO_MONITOR = 30;
    public runningAverage todaysBin;
    public int bottomAveIndex;
    public int todaysIndex;
    public int newHighToBeatIndex;
    
    public Vector monitorDays;
    public boolean startDay = false;
    public boolean stopDay = false;
    public boolean launchIt = false;
    public boolean oneShot = false;
    private positions actPositions;
    public int triggerDayIndex;
    public int count =0;
    /*
     * this keeps track of how many days we have before launch.
     */
    public int daysLaunchCount;

    /* this is how many days of consecutive rise days before
     * we launch. Configurable but default to 2.
     *
     */
    public int daysTilLaunch = 2;
    private final int NO_TRIGGER = 1000;
    static boolean stopAllVm = false;
    private volatilityMonitorManager myManager;

    public void setMyManager(volatilityMonitorManager vmm) {
        myManager = vmm;
    }
    public volatilityMonitorManager getMyManager() {
        return myManager;
    }
    public static void disAllVm() {
        stopAllVm = true;
    }
    public static void enAllVm() {
        stopAllVm = false;
    }
    public static boolean getControlVm() {
        return stopAllVm;
    }

    public runningAverage creRunningAve() {
        return (new runningAverage());
    }
    public void setOneShot(boolean os) {
        oneShot = os;
    }
    public volatilityMonitor(positions allPosIn) {

        
        bottomAveIndex = 0;
        todaysIndex = 0;

        /*
         * create number of days total to monitor, the first is overwritten
         * when we run out of room.
         */
        monitorDays = new Vector(NUMBER_OF_DAYS_TO_MONITOR, 2);
        todaysBin = new runningAverage();
        daysLaunchCount = 0;
        newHighToBeatIndex = 0;
        
        actPositions = allPosIn;

        triggerDayIndex = NO_TRIGGER;
        defVm = this;
        count = 0;
        stopAllVm = false;
    }

    public void setDefaultVm(volatilityMonitor def) {
        defVm = def;
    }
    /*
     * this guy must be called every sample time (i.e 20 seconds etc)
     */

    public void volatilityMonitorTick(float newSample) {

        if (stopAllVm == true){
            return;
        }
        /*
         * check if manager enabled us firsty...
         */
        if (oneShot == true) {
        /* one shot operation */
                if (count == 1) {
                    count--;
                    stoptDayMonitor();
                } else if ((startDay == true) && (count > 0)) {

                    todaysBin.runningAveTick(newSample);
                    count--;
                }

        }else {
            /* start/Stop operation */
            if (startDay == true) {
                todaysBin.runningAveTick(newSample);
            }

        }

    }

    public runningAverage volatilityGetRunningAve(int bin) {
      
        if (bin <= this.monitorDays.size()) {
            return ((runningAverage) monitorDays.get(bin));
        } else {
            return null;
        }
    }

    public void volatilityRunningAveSet(int bin, runningAverage ra) {
        monitorDays.add(bin, (runningAverage) ra);
    }

    public void startDayMonitor() {

        if (startDay == false) {
            todaysBin.runningAveCtrl(true);
            startDay = true;
        } else {
            System.out.println("startDayMonitor aleardy TRUE??");
        }

    }
    public boolean isStartDayOn() {
        return startDay;
    }
    public void setCounter(int cnt) {
        count = cnt+1;
    }
    public void setStartDayMonitor(boolean start) {
        startDay = start;
    }
    public void setStopDayMonitor(boolean stop) {
        stopDay = stop;
    }
    
    public void stoptDayMonitor() {
        // first check that we started the day ok...
        runningAverage currentBottomAve, newHighToBeatAve;

        
        if (startDay == true) {
            //stop the accumulation...
            todaysBin.runningAveCtrl(false);
            stopDay = true;
            //restart for tomorrow.
            startDay = false;
            /*
             * ok so we must have started the day good and ended the day good,
             * meaning we have been accumulating running average complete day.
             * now we need to move today's data bin to yesterday and bump a new day for tomorrow.
             */
            System.out.println("number of samples today is: " + todaysBin.runningSamplesGet());
            //not partial, it's filled nicely...
            todaysBin.runningPartialSet(false);

            //set todays date
            todaysBin.dateSet(myUtils.GetTodaysDate());
            //put todays newly aquired full bin to totaysIndex.
            monitorDays.add(todaysIndex, (runningAverage) todaysBin);
            //get the bottomAve bin out to see if we have a new bottom....
            currentBottomAve = (runningAverage) monitorDays.get(bottomAveIndex);
            newHighToBeatAve = (runningAverage) monitorDays.get(newHighToBeatIndex);
            /* check if we found a new lower average or
             * new hightobeat or ready to launch condition.
             * but only if it has NOT been launched already.
             */
            if (launchIt == false) {
                if (todaysBin.runningAveGet() < currentBottomAve.runningAveGet()) {

                    //new lower average so change index to point to it...
                    bottomAveIndex = todaysIndex;
                    //reset days to launch cuz we found a new low.
                    daysLaunchCount = 0;
                    newHighToBeatIndex = todaysIndex;
                    System.out.println("new bottom: " + bottomAveIndex);
                } else if (todaysBin.runningAveGet() > newHighToBeatAve.runningAveGet()) {
                    newHighToBeatIndex = todaysIndex;
                    System.out.println("new high to beat: " + newHighToBeatIndex);
                    if (++daysLaunchCount == daysTilLaunch) {
                        launchIt = true;
                        System.out.println("VolatilityMonitor: LAUNCH!");
                        displayDaysMonitored();
                        triggerDayIndex = todaysIndex;
                    }


                }
            }
            bumpTodaysIndex();

            //create a new todaysBin pointer for tommorrow.
            todaysBin = new runningAverage();


        } else {
            System.out.println("start/stop error: startDay = " + startDay + " StopDay = " + stopDay);
        }

    }

    public void displayDaysMonitored() {

        for (int ix = 0; ix < todaysIndex; ix++) {
            System.out.println("/nDay: " + ix);
            this.volatilityGetRunningAve(ix).prRunningAveParms();
        }
        System.out.println("bottomAveIndex: " + bottomAveIndex);
        System.out.println("newHighToBeatIndex: " + newHighToBeatIndex);
        System.out.println("daysToLauch: " + daysLaunchCount);
        System.out.println("\n");
    }

    private void bumpTodaysIndex() {
        /*circular buffer, so wrap around if needed
         *
         */
        todaysIndex++;
        todaysIndex %= NUMBER_OF_DAYS_TO_MONITOR;

    }

    private int getYesterdaysIndex() {
        /* circular buffer, so check for zero case
         * first check if we have filled up all days,
         * if so and index is 0, then take care of wrap.
         * else just return zero
         */
        if ((monitorDays.size() == NUMBER_OF_DAYS_TO_MONITOR)) {
            if (todaysIndex == 0) {
                return (NUMBER_OF_DAYS_TO_MONITOR - 1);
            } else {
                //no wrap just return today - 1 for yesterday
                return (todaysIndex - 1);
            }
        } else {
            if (todaysIndex == 0) {
                //we have no yesterday yet so return 0...
                return (0);
            } else {
                //there is a yesterday so return it...
                return (todaysIndex - 1);
            }
        }

    }

    void setDaysToLaunch(int days) {
        daysTilLaunch = days;
    }
    private volatilityMonitor actVm;
    private dispCoolIV actDisp;
    private  final int DISP_ROWS = 100;
    private final int DISP_COLUMNS = 15;
    String[][] dataArr = new String[DISP_ROWS][DISP_COLUMNS];
    JTable actJt = null;
    String[] columns = {
        "Day", "IVSampleCount", "IVAve", "Bottom", "TriggerDay", "Date"
    };
    class dispCoolIV extends JFrame {

        
        JScrollPane pane = null;
        iv impVol;

        dispCoolIV() {

            super("Display Implied Volatility");
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    System.out.println("display Implied Volatility: window closing...");

                    setVisible(false);
                    dispose();
                }
            });
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

            impVol = new iv();
            actVm = selPosition.getVm();
            setContentPane(impVol);
            initTable(actPositions);
            setSize(200, 200);
            pack();
            setVisible(true);
        }
        
        private void initTable(positions allPositions) {

            

            actJt = new JTable(dataArr, columns);

            update();
            pane = new JScrollPane(actJt);
            getContentPane().add(pane);

        }

        private void clearDisp() {

            for (int r = 0; r < DISP_ROWS; r++) {
                for (int c = 0; c < DISP_COLUMNS; c++) {
                    dataArr[r][c] = "";
                    actJt.getModel().setValueAt(dataArr[r][c], r, c);
                }
            }

        }
        private void update() {
            int rowNum = 0;
            runningAverage actAve;
            int sz;

            clearDisp();
            dataArr[rowNum][0] = "current";
            dataArr[rowNum][1] = Integer.toString(actVm.todaysBin.runningSamplesGet());
            dataArr[rowNum][2] = Float.toString(actVm.todaysBin.runningAveGet());
            rowNum++;
            sz = actVm.monitorDays.size();
            for (int ix = 0; ix < actVm.monitorDays.size(); ix++) {
                actAve = actVm.volatilityGetRunningAve(ix);

                dataArr[rowNum][0] = Integer.toString(ix + 1);
                dataArr[rowNum][1] = Integer.toString(actAve.runningSamplesGet());
                dataArr[rowNum][2] = Float.toString(actAve.runningAveGet());
                if (actVm.bottomAveIndex == ix) {
                    dataArr[rowNum][3] = "<";
                } else {
                    dataArr[rowNum][3] = " ";
                }
                if ((actVm.triggerDayIndex != NO_TRIGGER) && actVm.triggerDayIndex == ix) {
                    dataArr[rowNum][4] = "<";
                } else {
                    dataArr[rowNum][4] = " ";
                }
                dataArr[rowNum][5] = actAve.dateGet();
                rowNum++;
            }
            /* the following will update the fields affected by this update */
            for (int row = 0; row < rowNum; row++) {
                actJt.getModel().setValueAt(dataArr[row][0], row, 0);
                actJt.getModel().setValueAt(dataArr[row][1], row, 1);
                actJt.getModel().setValueAt(dataArr[row][2], row, 2);
                actJt.getModel().setValueAt(dataArr[row][3], row, 3);
                actJt.getModel().setValueAt(dataArr[row][4], row, 4);
                actJt.getModel().setValueAt(dataArr[row][5], row, 5);
            }
        }
    }
    Choice posList;
    String tickerList[];
    private positionData selPosition;

    class iv extends JPanel implements ActionListener {

        public boolean completed = false;

        iv() {
            Label posLable = new Label("Positions:");
            posList = new Choice();

            add(posLable);
            add(posList);
            tickerList = new String[actPositions.posDataVecSize()];
            fillTickerList(actPositions);
            for (int ix = 0; ix < tickerList.length; ix++) {
                posList.add(tickerList[ix]);
            }
            /* connect a listener for position selection from the user.. */
            posList.addItemListener(new positionSelListener());
            if (tickerList.length > 0) {
                selPosition = actPositions.posDataSearch(posList.getSelectedItem());
                System.out.println("selPosition Ticker is: " + selPosition.longTicker);

            }

            JButton updateButton = new JButton("Update");
            updateListener updateHandle = new updateListener();
            updateButton.addActionListener(updateHandle);

            add(updateButton);

            JButton resetButton = new JButton("Reset");
            resetButton.addActionListener(updateHandle);

            add(resetButton);
        }

        private void fillTickerList(positions posin) {
            posin.semTake();
            positionData actPos = posin.posDataFetchNext(true);
            for (int idx = 0; actPos != null; actPos = posin.posDataFetchNext(false), idx++) {
                tickerList[idx] = actPos.longTicker;
            }
            posin.semGive();
        }

        private class positionSelListener implements ItemListener {

            public void itemStateChanged(ItemEvent e) {
                String selPos = posList.getSelectedItem();
                selPosition = actPositions.posDataSearch(selPos);
                System.out.println("you selected: " + selPos);
                

            }
        }

        private class updateListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Update") && (completed == false)) {
                    System.out.println("Update action.");
                    /*
                     * get selected vol monitor and set
                     * defVm then create new display and put it in
                     * actDisp.
                     */
                    actVm = selPosition.getVm();
                    if (actVm.actDisp == null) {
                        actVm.actDisp = actDisp;
                    }
                    
                    actDisp.update();

                }
                if (command.equals("Reset")) {
                    System.out.println("Reset action.");
                    /*
                     * get selected vol monitor and set
                     * defVm then create new display and put it in
                     * actDisp.
                     */
                    selPosition.setVm(new volatilityMonitor(actPositions));
                    actDisp.clearDisp();
                    

                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            try {
                completed = false;

            } catch (Exception pe) {
                commonGui.prMsg("Incorrect String Input. Try again.");
            }
        }

    }

    public void dispImpliedVolatility() {

        actDisp = new dispCoolIV();

    }
    
    
}
