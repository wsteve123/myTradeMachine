/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;
import java.io.*;
/**
 *
 * @author earlie87
 */
public class volatilityMonitorManager {
    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/activeTrader_Ib_1/";
    final private String homeDirectory = myUtils.getMyWorkingDirectory("myTradeMachine", "activeTrader_Ib_1" + "/");
    positions actPositions;
    boolean enable;
    public void vmEnableAll() {
        enable = true;
        
        volatilityMonitor.enAllVm();


    }
    public void vmDisableAll() {
        
        enable = false;
        volatilityMonitor.disAllVm();

    }
    public void vmStopAll() {
        positionData tmpPos;

        System.out.println("stopping all VM");
        actPositions.semTake();

        tmpPos = actPositions.posDataFetchNext(true);
        while (tmpPos != null) {
            if ((tmpPos != null) && (tmpPos.getVm() != null)) {

                if ((this.vmControlGet() == false) && (tmpPos.getVm().isStartDayOn() == true)) {

                    tmpPos.getVm().stoptDayMonitor();
                    System.out.println("stopping VM for pos:" + tmpPos.longTicker);
                }

            }
            tmpPos = actPositions.posDataFetchNext(false);
        } /* while */

        actPositions.semGive();
    }
    
    public boolean vmControlGet() {
        /* this returns bool stopAllVm so inverse it */
        enable = !volatilityMonitor.getControlVm();
        return enable;
    }
    public volatilityMonitorManager(positions posin) {
        actPositions = posin;

        enable = true;
    }

    public void saveToDisk(String fileName) {
        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        boolean split = false;
        String wrFileName = homeDirectory;
        volatilityMonitor tmpVm;
        positionData tmpPos;
        runningAverage tmpAve;
        int posId;
        

        if (fileName == null) {
            wrFileName += "positions.vm";
        } else {
            wrFileName += (fileName+".vm");
        }
        actPositions.semTake();
        try {
            fos = new FileOutputStream(wrFileName);
            dos = new DataOutputStream(fos);
            bow = new BufferedWriter(new OutputStreamWriter(fos));
            //init to fetch from begining..and get the first.
            tmpPos = actPositions.posDataFetchNext(true);
            

            while (tmpPos != null) {
                posId = tmpPos.getPosId();
                tmpVm = tmpPos.getVm();
                if (tmpVm != null) {
                    /* write posIdx so that we know where it belongs when we read */
                    bow.write(Integer.toString(posId));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.bottomAveIndex));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.count));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.daysLaunchCount));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.daysTilLaunch));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.newHighToBeatIndex));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.todaysIndex));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.triggerDayIndex));
                    bow.newLine();

                    bow.write(Boolean.toString(tmpVm.launchIt));
                    bow.newLine();

                    bow.write(Boolean.toString(tmpVm.startDay));
                    bow.newLine();

                    bow.write(Boolean.toString(volatilityMonitor.stopAllVm));
                    bow.newLine();

                    bow.write(Boolean.toString(tmpVm.stopDay));
                    bow.newLine();

                    // write todays bin...
                    bow.write(Boolean.toString(tmpVm.todaysBin.isRunningAveOn()));
                    bow.newLine();

                    bow.write(Float.toString(tmpVm.todaysBin.runningAveGet()));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.todaysBin.runningSamplesGet()));
                    bow.newLine();

                    bow.write(Float.toString(tmpVm.todaysBin.runningTotalSamplesGet()));
                    bow.newLine();

                    bow.write(Boolean.toString(tmpVm.todaysBin.isPartialBin()));
                    bow.newLine();

                    bow.write(Integer.toString(tmpVm.monitorDays.size()));
                    bow.newLine();
                    // now write out monitored days...
                    for (int ix = 0; ix < tmpVm.monitorDays.size(); ix++) {
                        tmpAve = tmpVm.volatilityGetRunningAve(ix);
                         // write a days bin...
                        bow.write(Boolean.toString(tmpAve.isRunningAveOn()));
                        bow.newLine();

                        bow.write(Float.toString(tmpAve.runningAveGet()));
                        bow.newLine();

                        bow.write(Integer.toString(tmpAve.runningSamplesGet()));
                        bow.newLine();

                        bow.write(Float.toString(tmpAve.runningTotalSamplesGet()));
                        bow.newLine();

                        bow.write(Boolean.toString(tmpAve.isPartialBin()));
                        bow.newLine();
                        
                        bow.write(tmpAve.dateGet());
                        bow.newLine();

                    } /* for */

                } /* if */
                
                tmpPos = actPositions.posDataFetchNext(false);
            } /* while */
            bow.close();
            
            System.out.println(" volatilityMonitor Saved to file:"+wrFileName);

        } catch (Exception e) {
            System.out.println("error writing text to: " + wrFileName + "(" + e + ").");
        }

        actPositions.semGive();
    } /* saveToDisk */

       public  void readFromDisk(String fileName) {

        FileInputStream fis;
        BufferedReader bir;
        DataInputStream dis;
        boolean split = false;
        String rdFileName = homeDirectory;
        String tmpStr;
        int numOfDays = 0;
        positionData tmpPos;
        int posId;
        
        volatilityMonitor tmpVm;
       
        runningAverage tmpAve;
        
        if (fileName == null) {
            rdFileName += "positions.vm";
        } else {
            rdFileName += fileName+".vm";
        }
        actPositions.semTake();

        try {
            fis = new FileInputStream(rdFileName);
            dis = new DataInputStream(fis);
            bir = new BufferedReader(new InputStreamReader(fis));
            /* fetch first position (from start) */
            

            while (((tmpStr = bir.readLine()) != null) && (!split)) {
                tmpVm = new volatilityMonitor(actPositions);
                /* read the positions ID which tells us where it goes in actPositions */
                posId = Integer.parseInt(tmpStr);
                //read the bottomAveIndex first.
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the count
                    tmpVm.bottomAveIndex = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }

                if ((tmpStr = bir.readLine()) != null) {
                    //now read the count
                    tmpVm.count = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the daysLaunchCount
                    tmpVm.daysLaunchCount = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the daysTilLaunch
                    tmpVm.daysTilLaunch = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the newHighToBeatIndex
                    tmpVm.newHighToBeatIndex = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((tmpStr = bir.readLine()) != null) {
                    //now read the todaysIndex
                    tmpVm.todaysIndex = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the triggerDayIndex
                    tmpVm.triggerDayIndex = Integer.parseInt(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the launchIt
                    tmpVm.launchIt = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the startDay
                    tmpVm.startDay = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the stopAllVm
                    volatilityMonitor.stopAllVm = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the stopDay
                    tmpVm.stopDay = Boolean.parseBoolean(tmpStr);
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the runningAve
                    tmpVm.todaysBin.runningAveCtrl(Boolean.parseBoolean(tmpStr));
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the runningAve
                    tmpVm.todaysBin.runningAveSet(Float.parseFloat(tmpStr));
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the runningSamples
                    tmpVm.todaysBin.runningSamplesSet(Integer.parseInt(tmpStr));
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the runningTotalSamples
                    tmpVm.todaysBin.runningTotalSamplesSet(Float.parseFloat(tmpStr));
                } else {
                    split = true;
                }
                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the partialBin
                    tmpVm.todaysBin.partialBinCtrl(Boolean.parseBoolean(tmpStr));
                } else {
                    split = true;
                }

                if ((!split) && (tmpStr = bir.readLine()) != null) {
                    //now read the sz of monitor days..to be used below...
                    numOfDays = (Integer.parseInt(tmpStr));
                } else {
                    split = true;
                }
                for (int ix = 0; (ix < numOfDays) && (split == false); ix++) {
                    tmpAve = new runningAverage();
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the runningAve boolean
                        tmpAve.runningAveCtrl(Boolean.parseBoolean(tmpStr));
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the runningAve float
                        tmpAve.runningAveSet(Float.parseFloat(tmpStr));
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the runningSamples
                        tmpAve.runningSamplesSet(Integer.parseInt(tmpStr));
                    } else {
                        split = true;
                    }
                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the runningTotalSamples
                        tmpAve.runningTotalSamplesSet(Float.parseFloat(tmpStr));
                    } else {
                        split = true;
                    }

                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the partialBin boolean
                        tmpAve.partialBinCtrl(Boolean.parseBoolean(tmpStr));
                    } else {
                        split = true;
                    }

                    if ((!split) && (tmpStr = bir.readLine()) != null) {
                        //now read the date
                        tmpAve.dateSet(tmpStr);
                    } else {
                        split = true;
                    }
                    /* now put the tmpAve we re-created from the file into the tmpVm structure...*/
                    tmpVm.volatilityRunningAveSet(ix, tmpAve);
                } /* for */
                /* should be done with first position's vm so store it away ...*/
                if (split == false) {
                    /* fetch and restore  */
                    actPositions.semGive();
                    tmpPos = actPositions.posDataRetrieve(posId);
                    actPositions.semTake();
                    tmpPos.setVm(tmpVm);
                    actPositions.posDataReplace(tmpPos, posId);
                }
                
            } /* while */

            bir.close();

        } catch (Exception e) {
            System.out.println("error reading text for: " + rdFileName + "(" + e + ").");
        }
        actPositions.semGive();

    } /* readFromDisk */



}
