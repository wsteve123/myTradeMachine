/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import positions.myUtils;
import tradeMenus.slopeTraderFrameForm.TradeTicket;

/**
 *
 * @author earlie87
 */
public class FileItAway {
    /*
     Use this to read/write structures to/from files.
     */

    //private String homeDirectory = "/Users/earlie87/NetBeansProjects/myTradeMachine/src/supportFiles/";
    private String homeDirectory = myUtils.getMyWorkingDirectory() + "/src/supportFiles/";
    IoWrTextFiles wrFile = null;
    IoRdTextFiles rdFile = null;
    final String BEGIN = "BEG";
    final String END = "END";
    int orderWr = 0;
    int orderRd = 0;
    int order = 0;
    List<Doubles> dlist = new ArrayList<Doubles>();
    List<Integers> ilist = new ArrayList<Integers>();
    List<Strings> slist = new ArrayList<Strings>();
    List<Floats> flist = new ArrayList<Floats>();

    class Doubles {

        String label;
        double dbl = 0.0;
        int order = 0;
    }

    class Integers {

        String label;
        int intgr = 0;
        int order = 0;
    }

    class Strings {

        String label;
        String str = "";
        int order = 0;
    }

    class Floats {

        String label;
        float flt = 0;
        int order = 0;
    }
    public void setHomeDirectory(String homeDir){
        homeDirectory = homeDir;
    }
    public void setBeg(int element) {
        setString(Integer.toString(element) /*value*/, BEGIN /*label*/);
    }

    public void setEnd(int element) {
        setString(Integer.toString(element) /*value*/, END/*label*/);
    }

    public void setDouble(double din, String label) {
        Doubles d = new Doubles();
        d.dbl = din;
        d.label = label;
        dlist.add(d);
        d.order = orderWr;
        orderWr++;
    }

    public void setInteger(int iin, String label) {
        Integers i = new Integers();
        i.intgr = iin;
        i.label = label;
        ilist.add(i);
        i.order = orderWr;
        orderWr++;

    }

    public void setString(String sin, String label) {
        Strings s = new Strings();
        s.str = sin;
        s.label = label;
        slist.add(s);
        s.order = orderWr;
        orderWr++;

    }

    public void setFloat(float fin, String label) {
        Floats f = new Floats();
        f.flt = fin;
        f.label = label;
        flist.add(f);
        f.order = orderWr;
        orderWr++;

    }

    public void wr() {
        int keepOrder = 0;
        int idx = 0;
        Integers i = new Integers();
        Doubles d = new Doubles();
        Strings s = new Strings();
        Floats f = new Floats();
        int szToWr = orderWr;
        int ydx = 0;
        boolean wrDone = false;
        /*  cycle all which is the value in orderWr (counter of writes that happened)
         write each in the order they were called prior...all ordered so they match the 
         order of the structure.
         */
        for (keepOrder = 0; keepOrder <= szToWr; keepOrder++) {
            //first see if doubles need to be written, pay attention to order..
            wrDone = false;
            if (dlist.size() > 0) {
                for (idx = 0; ((!wrDone) && (idx < dlist.size())); idx++) {
                    d = dlist.get(idx);
                    if (keepOrder == d.order) {
                        if (!d.label.equals("")) {
                            wrFile.write(d.label);
                        }
                        wrFile.write(Double.toString(d.dbl));
                        wrDone = true;
                    }
                }
            }
            if (ilist.size() > 0) {
                for (idx = 0; ((!wrDone) && (idx < ilist.size())); idx++) {
                    i = ilist.get(idx);
                    if (keepOrder == i.order) {
                        if (!i.label.equals("")) {
                            wrFile.write(i.label);
                        }
                        wrFile.write(Integer.toString(i.intgr));
                        wrDone = true;
                    }
                }
            }
            if (flist.size() > 0) {
                for (idx = 0; ((!wrDone) && (idx < flist.size())); idx++) {
                    f = flist.get(idx);
                    if (keepOrder == f.order) {
                        if (!f.label.equals("")) {
                            wrFile.write(f.label);
                        }
                        wrFile.write(Float.toString(f.flt));
                        wrDone = true;
                    }
                }
            }
            if (slist.size() > 0) {
                for (idx = 0; ((!wrDone) && (idx < slist.size())); idx++) {
                    s = slist.get(idx);
                    if (keepOrder == s.order) {
                        if (!s.label.equals("")) {
                            wrFile.write(s.label);
                        }
                        wrFile.write(s.str);
                        wrDone = true;
                    }
                }
            }
        }
        //we are done so reset orderWr clear lists...
        orderWr = 0;
        slist.clear();
        dlist.clear();
        ilist.clear();
        flist.clear();
    }

    public void getBeg(int element) {
        getString(Integer.toString(element) /*value*/, BEGIN /*label*/);
    }

    public void getEnd(int element) {
        getString(Integer.toString(element) /*value*/, END/*label*/);
    }

    public void getDouble(double din, String label) {
        Doubles d = new Doubles();
        d.dbl = din;
        d.label = label;
        dlist.add(d);
        d.order = orderRd;
        orderRd++;
    }

    public void getInteger(int iin, String label) {
        Integers i = new Integers();
        i.intgr = iin;
        i.label = label;
        ilist.add(i);
        i.order = orderRd;
        orderRd++;

    }

    public void getString(String sin, String label) {
        Strings s = new Strings();
        s.str = sin;
        s.label = label;
        slist.add(s);
        s.order = orderRd;
        orderRd++;

    }

    public void getFloat(float fin, String label) {
        Floats f = new Floats();
        f.flt = fin;
        f.label = label;
        flist.add(f);
        f.order = orderRd;
        orderRd++;

    }

    public void rd() {

        int keepOrder = 0;
        int idx = 0;
        Integers i = new Integers();
        Doubles d = new Doubles();
        Strings s = new Strings();
        Floats f = new Floats();
        int szToRd = orderRd;
        boolean rdDone = false;
        /*  
         */
        for (keepOrder = 0; keepOrder <= szToRd; keepOrder++) {
            //first see if doubles need to be read, pay attention to order..
            rdDone = false;
            
            for (idx = 0; ((rdDone == false) && idx < dlist.size()); idx++) {
                d = dlist.get(idx);
                if(keepOrder == d.order) {
                    d.label = rdFile.read(false/*notused*/);
                    d.dbl = Double.valueOf(rdFile.read(false));
                    dlist.set(idx,d); /* add back to list the value read */
                    rdDone = true;
                }
            }
            
            for (idx = 0; ((rdDone == false) && idx < ilist.size()); idx++) {
                i = ilist.get(idx);
                if(keepOrder == i.order) {
                    i.label = rdFile.read(false/*notused*/);
                    i.intgr = Integer.valueOf(rdFile.read(false));
                    ilist.set(idx,i); /* add back to list the value read */
                    rdDone = true;
                }
            }    
            
            for (idx = 0; ((rdDone == false) && idx < flist.size()); idx++) {
                f = flist.get(idx);
                if(keepOrder == f.order) {
                    f.label = rdFile.read(false/*notused*/);
                    f.flt = Integer.valueOf(rdFile.read(false));
                    flist.set(idx,f); /* add back to list the value read */
                    rdDone = true;
                }
            }
                
            for (idx = 0; ((rdDone == false) && (idx < slist.size())); idx++) {
                s = slist.get(idx);
                if(keepOrder == s.order) {
                    s.label = rdFile.read(false/*notused*/);
                    s.str = String.valueOf(rdFile.read(false));
                    slist.set(idx,s); /* add back to list the value read */
                    rdDone = true;
                    
                }
            }           
        }
        //we are done so reset orderRd, lists are ready with data
        orderRd = 0;
    }
    public void putToThisTicket(TradeTicket thisTicket){

        String beg = null;
        String end = null;
        
        beg = slist.get(0).str;   
        
        thisTicket.ticker = slist.get(1).str;
        
        thisTicket.originalCost = dlist.get(0).dbl;
        thisTicket.originalCostOverShoot = dlist.get(1).dbl;
        thisTicket.currentCost = dlist.get(2).dbl;
        thisTicket.runningPl = dlist.get(3).dbl;
        thisTicket.runningPlPercent = dlist.get(4).dbl;
        thisTicket.maxProfit = dlist.get(5).dbl;
        thisTicket.maxProfitPercent = dlist.get(6).dbl;
        thisTicket.maxLoss = dlist.get(7).dbl;
        thisTicket.maxLossPercent = dlist.get(8).dbl;
        
        
        thisTicket.sharesAtHand = ilist.get(0).intgr;
        thisTicket.numberOfTrades = ilist.get(1).intgr;
        thisTicket.numOfSharesToTrade = ilist.get(2).intgr;
        thisTicket.daysIn = ilist.get(3).intgr;
        thisTicket.daysOut = ilist.get(4).intgr;
        //added 11/23/15
        thisTicket.hysteresisDays = ilist.get(5).intgr;
        if (true) {
            //added 2/10/16 for long trades
            thisTicket.actSegTrade = ilist.get(6).intgr;
            int tmp = 0;
            boolean open = false;

            //all this trouble becaue of boolean...
            tmp = ilist.get(7).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[0].segOpen = open;

            tmp = ilist.get(8).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[1].segOpen = open;

            tmp = ilist.get(9).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[2].segOpen = open;

            tmp = ilist.get(10).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[3].segOpen = open;

            tmp = ilist.get(11).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[4].segOpen = open;

            tmp = ilist.get(12).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTrades[5].segOpen = open;

            thisTicket.segTrades[0].segShares = ilist.get(13).intgr;
            thisTicket.segTrades[1].segShares = ilist.get(14).intgr;
            thisTicket.segTrades[2].segShares = ilist.get(15).intgr;
            thisTicket.segTrades[3].segShares = ilist.get(16).intgr;
            thisTicket.segTrades[4].segShares = ilist.get(17).intgr;
            thisTicket.segTrades[5].segShares = ilist.get(18).intgr;
        }
        if (true) {
            //added 2/22/16 for overshoot short trades
            thisTicket.actSegTradeOverShoot = ilist.get(19).intgr;
            int tmp = 0;
            boolean open = false;

            //all this trouble becaue of boolean...
            tmp = ilist.get(20).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[0].segOpen = open;

            tmp = ilist.get(21).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[1].segOpen = open;

            tmp = ilist.get(22).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[2].segOpen = open;

            tmp = ilist.get(23).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[3].segOpen = open;

            tmp = ilist.get(24).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[4].segOpen = open;

            tmp = ilist.get(25).intgr;
            open = ((tmp == 1) ? true : false);
            thisTicket.segTradesOverShoot[5].segOpen = open;

            thisTicket.segTradesOverShoot[0].segShares = ilist.get(26).intgr;
            thisTicket.segTradesOverShoot[1].segShares = ilist.get(27).intgr;
            thisTicket.segTradesOverShoot[2].segShares = ilist.get(28).intgr;
            thisTicket.segTradesOverShoot[3].segShares = ilist.get(29).intgr;
            thisTicket.segTradesOverShoot[4].segShares = ilist.get(30).intgr;
            thisTicket.segTradesOverShoot[5].segShares = ilist.get(31).intgr;
        }
        thisTicket.lastOperation = slist.get(2).str; 
        //new added 2/20/15 to remember if we traded today
        thisTicket.didWeTradeToday = slist.get(3).str;
        thisTicket.curTrend = slist.get(4).str;
        
        thisTicket.maxPDate = slist.get(5).str;
        thisTicket.maxLDate = slist.get(6).str;
        thisTicket.maxRunningProfitDate = slist.get(7).str;
        thisTicket.maxRunningLossDate = slist.get(8).str;
        thisTicket.OpenPosDateIn = slist.get(9).str;
        
        thisTicket.maxRunningProfit = dlist.get(9).dbl;
        thisTicket.maxRunningLoss = dlist.get(10).dbl;
        thisTicket.lastPrice = dlist.get(11).dbl;
        if (true) {
            //added 2/10/16 this is for longs
            thisTicket.segTrades[0].segPrice = dlist.get(12).dbl;
            thisTicket.segTrades[1].segPrice = dlist.get(13).dbl;
            thisTicket.segTrades[2].segPrice = dlist.get(14).dbl;
            thisTicket.segTrades[3].segPrice = dlist.get(15).dbl;
            thisTicket.segTrades[4].segPrice = dlist.get(16).dbl;
            thisTicket.segTrades[5].segPrice = dlist.get(17).dbl;
        }
        if (true) {
            //added 2/22/16 this is for overShoot shorts
            thisTicket.segTradesOverShoot[0].segPrice = dlist.get(18).dbl;
            thisTicket.segTradesOverShoot[1].segPrice = dlist.get(19).dbl;
            thisTicket.segTradesOverShoot[2].segPrice = dlist.get(20).dbl;
            thisTicket.segTradesOverShoot[3].segPrice = dlist.get(21).dbl;
            thisTicket.segTradesOverShoot[4].segPrice = dlist.get(22).dbl;
            thisTicket.segTradesOverShoot[5].segPrice = dlist.get(23).dbl;
        }
        end = slist.get(10).str;
        //clear the lists..
        slist.clear();
        dlist.clear();
        ilist.clear();
        flist.clear();
    }
    public double getDouble(String label) {
        return (2.3);
    }

    public int getInteger(String label) {
        return (3);
    }

    public String getStirng(String label) {
        return ("");
    }

    public float getFloat(String label) {
        return (2);
    }

    public void openWrFile(String fileName, boolean append) {
        wrFile = new IoWrTextFiles((fileName), append);
    }

    public void closeWrFile() {
        if (wrFile != null)
            wrFile.closeWr();
    }

    public void openRdFile(String fileName, boolean append) {
        rdFile = new IoRdTextFiles((fileName), append);
    }
    public void closeRdFile() {
        if (rdFile != null)
            rdFile.closeRd();
    }

    public class IoRdTextFiles {

        String fname = homeDirectory;

        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;
        BufferedWriter bow;

        void openRd(String fileName, boolean append) {
            fname = fileName;
            
            try {
                fis = new FileInputStream(fname);
                bir = new BufferedReader(new InputStreamReader(fis));
                
            } catch (Exception e) {
                System.out.println("error reading text from: " + fname + "(" + e + ").");
                System.out.println("file does not exist " + fname);
                try {
                    bow = new BufferedWriter(new FileWriter(fname, false));
                    fis = new FileInputStream(fname);
                } catch (IOException ex) {
                    Logger.getLogger(createOptionFilesForExchangesDialogForm.class.getName()).log(Level.SEVERE, null, ex);
                }

                bir = new BufferedReader(new InputStreamReader(fis));
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

        IoRdTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openRd(fname + fileName, append);

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

    public class IoWrTextFiles {

        String fname = homeDirectory;

        FileOutputStream fos;
        BufferedWriter bow;
        DataOutputStream dos;
        FileInputStream fis;
        BufferedReader bir;

        void openWr(String fileName, boolean append) {
            fname = fileName;

            try {
                fos = new FileOutputStream(fileName);
                dos = new DataOutputStream(fos);
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

        IoWrTextFiles(String fileName, boolean append) {
            fname = homeDirectory;
            this.openWr(fname + fileName, append);

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

}
