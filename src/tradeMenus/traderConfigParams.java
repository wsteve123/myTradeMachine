/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.File;
import positions.commonGui;

/**
 *
 * @author earlie87
 */
public class traderConfigParams {

    //bit positions
    public static final int PIVOT_SIG_INT = 0;
    public static final int CLOSE_PRICE_INT = 1;
    public static final int TREND_GOOD_INT = 2;
    public static final int OR_INT = 3;
    public static final int REOPEN_INT = 4;
    public static final int MABOUNCE_INT = 5;
    public static final int WEAKNESS_INT = 6;
    public static final int STRENGTH_INT = 7;
    public static final int DISABLED_INT = 0;
    
    public static final String PIVOT_SIG_STR = "Pivotal Signal";
    public static final String CLOSE_PRICE_STR = "Closing Price";
    public static final String TREND_GOOD_STR = "Trend Confirmed";
    public static final String OR_STR = "OR";
    public static final String MABOUNCE_STR = "Bouncing Off MA";
    
    public static final String PIVOT_SIG_AND_CLOSE_PRICE_STR = "Pivot Signal AND Closing Price";
    public static final String PIVOT_SIG_OR_CLOSE_PRICE_STR = "Pivot Signal OR Closing Price";
    public static final String DISABLED_STR = "Disabled";
    
    static int currentBias = slopeDefs.oBiasLong;
    
    //these are the parameters we need.
    private int openWhenCode = traderConfigParams.DISABLED_INT;
    private int closeWhenCode = traderConfigParams.DISABLED_INT;
    private int openCloseDays = 0;
    private int closeCloseDays = 0;
    private int openTrendDays = 0;
    private int closeTrendDays = 0;
    private int closeReopenDays = 0;
    private int maBounceWithinPercent = 0;
    private int overShootPercent = 0;
    private int windowSz50Day = 0;
    private int windowSz100Day = 0;
    private int windowSz200Day = 0;
    private int maMustTouchMinPercent = 0;
    private int maPierceMaxPercent = 0;
    private boolean goodInput = false;
    String traderConfigFileName = "traderConfigFile.txt";
    //files for write/read
    IOTextFiles.ioRdTextFiles traderConfigRdTextFile;
    IOTextFiles.ioWrTextFiles traderConfigWrTextFile;
    IOTextFiles.ioDeleteTextFiles traderConfigDeleteTextFile;
    String prefixDirectory = "";
    public void setPrefixDirectory(String p){
        prefixDirectory = p;
    }
    public void setGoodInput(boolean gi){
        goodInput = gi;
    }
    public void setOpenWhenCode(int c){
        openWhenCode = c;
    }
    public void setCloseWhenCode(int c){
        closeWhenCode = c;
    }
    public void setOpenCloseDays(int c){
        openCloseDays = c;
    }
    public void setCloseCloseDays(int c){
        closeCloseDays = c;
    }
    public void setOpenTrendDays(int c){
        openTrendDays = c;
    }
    public void setCloseTrendDays(int c){
        closeTrendDays = c;
    }
    public void setCloseReopenDays(int c){
        closeReopenDays = c;
    }
    public void setOpenMaBouncePercent(int c){
        maBounceWithinPercent = c;
    }
    public void setOverShootPercent(int c){
        overShootPercent = c;
    }
    public void set50DayWindowSz(int s){
        windowSz50Day = s;
    }
    public void set100DayWindowSz(int s){
        windowSz100Day = s;
    }
    public void set200DayWindowSz(int s){
        windowSz200Day = s;
    }
    public void setMaMustTouchMinPercent(int p){
        maMustTouchMinPercent = p;
    }
    public void setMaPierceMaxPercent(int p){
        maPierceMaxPercent = p;
    }
    public boolean getGoodInput(){
        return goodInput;
    }
    public int getOpenWhenCode(){
        return openWhenCode;
    }
    public int getCloseWhenCode(){
        return closeWhenCode;
    }
    public int getOpenCloseDays(){
        return openCloseDays;
    }
    public int getCloseCloseDays(){
        return closeCloseDays;
    }
    public int getOpenTrendDays(){
        return openTrendDays;
    }
    public int getCloseTrendDays(){
        return closeTrendDays;
    }
    public int getCloseReopenDays(){
        return closeReopenDays;
    }
    public int getCurrentBias(){
        return currentBias;
    }
    public int getOpenMaBouncePercent(){
        return maBounceWithinPercent;
    }
    public int getOverShootPercent(){
        return overShootPercent;
    }
    public int get50DayWindowSz(){
        return windowSz50Day;
    }
    public int get100DayWindowSz(){
        return windowSz100Day;
    }
    public int get200DayWindowSz(){
        return windowSz200Day;
    }
    public int getMaMustTouchMinPercent(){
        return maMustTouchMinPercent;
    }
    public int getMaPierceMaxPercent(){
        return maPierceMaxPercent;
    }
    public void setCurrentBias(int c){
        currentBias = c;
    }
    
    public traderConfigParams(int bias) {
        currentBias = bias;
    }    
    static public int setWhenPivotSignal(int in){
        return (in |= (1 << PIVOT_SIG_INT));
    }
    static public int clrWhenPivotSignal(int in){
        return (in &= ~(1 << PIVOT_SIG_INT));
    }
    static public int setWhenClosePrice(int in){
        return (in |= (1 << CLOSE_PRICE_INT));
    }
    static public int clrWhenClosePrice(int in){
        return (in &= ~(1 << CLOSE_PRICE_INT));
    }
    static public int setWhenTrendGood(int in){
        return (in |= (1 << TREND_GOOD_INT));
    }
    static public int clrWhenTrendGood(int in){
        return (in &= ~(1 << TREND_GOOD_INT));
    }
    static public int setWhenOrSelected(int in){
        return (in |= (1 << OR_INT));
    }
    static public int clrWhenOrSelected(int in){
        return (in &= ~(1 << OR_INT));
    }
    static public int setWhenReopen(int in){
        return (in |= (1 << REOPEN_INT));
    }
    static public int clrWhenReopen(int in){
        return (in &= ~(1 << REOPEN_INT));
    }
    static public int setWhenMaBounce(int in){
        return (in |= (1 << MABOUNCE_INT));
    }
    static public int clrWhenMaBounce(int in){
        return (in &= ~(1 << MABOUNCE_INT));
    }
    static public int setWhenWeakness(int in){
        return (in |= (1 << WEAKNESS_INT));
    }
    static public int clrWhenWeakness(int in){
        return (in &= ~(1 << WEAKNESS_INT));
    }
    static public int setWhenStrength(int in){
        return (in |= (1 << STRENGTH_INT));
    }
    static public int clrWhenStrength(int in){
        return (in &= ~(1 << STRENGTH_INT));
    }
    static boolean isPivotSignalSet(int code){
        
        return ((code & (1 << PIVOT_SIG_INT)) > 0);
        
    }
    static boolean isClosePriceSet(int code){
        
        return ((code & (1 << CLOSE_PRICE_INT)) > 0);
        
    }
    static boolean isTrendGoodSet(int code){
        
        return ((code & (1 << TREND_GOOD_INT)) > 0);
        
    }
    static boolean isOrSet(int code){
        
        return ((code & (1 << OR_INT)) > 0);
        
    }
    static boolean isReopenSet(int code){
        
        return ((code & (1 << REOPEN_INT)) > 0);
        
    }
    static boolean isMaBounceSet(int code){
        
        return ((code & (1 << MABOUNCE_INT)) > 0);
        
    }
    static public boolean isWeaknessSet(int code){
        
        return ((code & (1 << WEAKNESS_INT)) > 0);
        
    }
    static public boolean isStrengthSet(int code){
        
        return ((code & (1 << STRENGTH_INT)) > 0);
        
    }
    static int howManySet(int code){
        int tmp = code;
        int bit = 1;
        int cnt = 0;
        while (tmp > 0){
            if ((tmp & bit) > 0){
                cnt++;
                tmp &= ~(tmp & bit);
            }
            bit <<= 1;
        }
        
        return cnt;
    }

    static public int getBias(){
        return currentBias;
    }
    public void wrToFile(){
        
        System.out.println("\nwriting traderConfig file: " + prefixDirectory + traderConfigFileName);
        IOTextFiles ioTextFiles = new IOTextFiles();
        traderConfigWrTextFile = ioTextFiles.new ioWrTextFiles(prefixDirectory + traderConfigFileName, false);
        
        traderConfigWrTextFile.write(Integer.toString(openWhenCode));
        traderConfigWrTextFile.write(Integer.toString(closeWhenCode));
        traderConfigWrTextFile.write(Integer.toString(openCloseDays));
        traderConfigWrTextFile.write(Integer.toString(closeCloseDays));
        traderConfigWrTextFile.write(Integer.toString(openTrendDays));
        traderConfigWrTextFile.write(Integer.toString(closeTrendDays));
        traderConfigWrTextFile.write(Integer.toString(closeReopenDays));
        
        traderConfigWrTextFile.write(Integer.toString(maBounceWithinPercent));
        //these are stored in portfolio file now..with version 3..
        if (false){
            traderConfigWrTextFile.write(Integer.toString(maMustTouchMinPercent));
            traderConfigWrTextFile.write(Integer.toString(maPierceMaxPercent));
        }       
        traderConfigWrTextFile.write(Integer.toString(overShootPercent));
        traderConfigWrTextFile.write(Boolean.toString(goodInput));
        traderConfigWrTextFile.closeWr();
        System.out.println("\ndone.");       
    }
    public void rdFromFile(){
        String dir;
        File f;
        System.out.println("\nreading traderConfig file: " + prefixDirectory + traderConfigFileName);
        IOTextFiles ioTextFiles = new IOTextFiles();
        traderConfigRdTextFile = ioTextFiles.new ioRdTextFiles(prefixDirectory + traderConfigFileName, false);
        
        dir = traderConfigRdTextFile.getDirectory();
        //f = new File(dir+traderConfigFileName);
        f = new File(dir + prefixDirectory + traderConfigFileName);
        if ((f.exists() == true) && (f.length() > 4)) {
            System.out.println("\n file does exist.");
            openWhenCode = Integer.valueOf(traderConfigRdTextFile.read(false));
            closeWhenCode = Integer.valueOf(traderConfigRdTextFile.read(false));
            openCloseDays = Integer.valueOf(traderConfigRdTextFile.read(false));
            closeCloseDays = Integer.valueOf(traderConfigRdTextFile.read(false));
            openTrendDays = Integer.valueOf(traderConfigRdTextFile.read(false));
            closeTrendDays = Integer.valueOf(traderConfigRdTextFile.read(false));
            closeReopenDays = Integer.valueOf(traderConfigRdTextFile.read(false));
            
            maBounceWithinPercent = Integer.valueOf(traderConfigRdTextFile.read(false)); 
            //these are stored in portfolio file now..with version 3..
            if(false){
                maMustTouchMinPercent = Integer.valueOf(traderConfigRdTextFile.read(false));
                maPierceMaxPercent = Integer.valueOf(traderConfigRdTextFile.read(false));    
            }
            overShootPercent = Integer.valueOf(traderConfigRdTextFile.read(false)); 
            goodInput = Boolean.valueOf(traderConfigRdTextFile.read(false));
            traderConfigRdTextFile.closeRd();
            System.out.println("\ndone.");
        }
    }
}
