/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.util.ArrayList;
import java.util.List;
import tradeMenus.portfolio.PosCreationParameters;

/**
 *
 * @author earlie87
 */
public class Positions {
    private String portfolio;
    private String portfolioAlias = "";
    private List<String> myTradeList = new ArrayList<>();
    public String tradeListFileName = "slopeTraderList.txt";
    public String pathNamePrefix = "";
    public String accountNumber = "";
    public boolean appendToExistingFile = false;
    public String userCriteria;
    public boolean userCanceledPosCreation = false;
    IOTextFiles ioTextFiles = new IOTextFiles();
    //values to pass back
    public int todaysTrades;
    public int openPositions;
    public int closedPositions;
    public int newPositions;
    int histDataProblemCnt = 0;
    int getQuoteProblemCnt = 0;
    int tradeErrorsCnt = 0;
    int numOfSharesToTrade = 0;
    boolean tradeLongAndShort = false;
    String longOrShort = slopeDefs.oBiasLongStr;
    int positionBias = slopeDefs.oBiasLong;
    double maxPercentPerPos = 0;
    int availFunds = 0;
    int version;
    boolean quoteStreamsOpened = false;
    String state;
    int min90DayVolume = 0;
    int minPricePerShare = 0;
    int maMustTouchPercent = 0;
    int maMaxPiercePercent = 0;
    int ma50DaySz = 0;
    int ma100DaySz = 0;
    int ma200DaySz = 0;
    //portfolio pouter = new portfolio();
    PosCreationParameters posCreationParams;
    public void setPortfolioAlias(String aliasIn){
        portfolioAlias = aliasIn;
    }
    public void setAccountNumber(String accIn){
        accountNumber = accIn;
    }
    public String getAccountNumber(){
        return accountNumber;
    }
    public void setPortolioName(String portIn){
        portfolio = portIn;
    }
    public String getPortfolioName(){
        return portfolio;
    }
    public String getPortfolioAlias(){
        return portfolioAlias;
    }
    public void setToThisTradeList(List<String> tradeIn){
        myTradeList = tradeIn;
    }
    public List<String> getTradeList(){
        return myTradeList;
    }
    public void setPathnamePrefix(String pathIn){
        pathNamePrefix = pathIn;
    }
    public boolean isThereATradeList(){
        return(myTradeList.size() > 0);       
    }
    public void setState(String stateIn){
        state = stateIn;
    }
    public void setUserCriteria(String critIn){
        userCriteria = critIn;
    }
    public void setNumberOfSharesToTrade(int numIn){
        numOfSharesToTrade = numIn;
    }
    public void setTradeLongAndShort(boolean ls){
        tradeLongAndShort = ls;
    }
    public void setPositionBias(int bias){
        positionBias = bias;
    }
    public void setStreamsOpened(boolean openIn){
        quoteStreamsOpened = openIn;
    }
    public void setAvailFunds(int availIn){
        availFunds = availIn;
    }
    public void setMaxPerPos(double maxIn){
        maxPercentPerPos = maxIn;
    }
    public void setVersion(int versIn){
        version = versIn;
    }
    public boolean getDidUserCancelPosCreatioin(){
        return userCanceledPosCreation;
    }
    public int getMin90DayVolume(){
        return min90DayVolume;
    }
    public int getMinPricePerShare(){
        return minPricePerShare;
    }
    public int getMaMustTouchPercent(){
        return maMustTouchPercent;
    }
    public int getMaMaxPiercePercent(){
        return maMaxPiercePercent;
    }
    public void setMaMustTouchPercent(int m){
        maMustTouchPercent = m;
    }
    public void setMaMaxPiercePercent(int m){
        maMaxPiercePercent = m;
    }
    public void setMa50daySz(int s){
        ma50DaySz = s;
    }
    public void setMa100daySz(int s){
        ma100DaySz = s;
    }
    public void setMa200daySz(int s){
        ma200DaySz = s;
    }
    public int getMa50daySz(){
        return ma50DaySz;
    }
    public int getMa100daySz(){
        return ma100DaySz;
    }
    public int getMa200daySz(){
        return ma200DaySz;
    }
    public void setLastPosCreationParams(PosCreationParameters pin){
        posCreationParams = pin;
    }
    
    public void wrTradeListToFile(boolean appendIt){
        
        int idx;
        if (myTradeList.size() > 0) {
            System.out.println("writing to tradeList file.");
            IOTextFiles.ioWrTextFiles wrFile = ioTextFiles.new ioWrTextFiles(pathNamePrefix + "slopeTraderList.txt", appendIt);
            for (idx = 0; idx < myTradeList.size(); idx++) {
                //System.out.println("\n" + myTradeList.get(idx));
                wrFile.write(myTradeList.get(idx));
            }
            wrFile.closeWr();
            System.out.println("wrote to tradeList file.");
        } else {
            System.out.println("tradeList was empty? Did not write to file..");
        }
    }
    public void rdTradeListFromFile(){
        
        System.out.println("reading tradeList from file.");
        IOTextFiles.ioRdTextFiles rdFile = ioTextFiles.new ioRdTextFiles(pathNamePrefix + "slopeTraderList.txt", false);
        
        String tmpStr = null;
        int idx = 0;
        myTradeList.removeAll(myTradeList);
        while ((tmpStr = rdFile.read(false)) != null) {
            myTradeList.add(tmpStr);
            idx++;
        }
        System.out.println("\nread tradeList.." + idx + " tickers.");
        rdFile.closeRd();
        
    }
    public void deleteTradeListFile(){
        System.out.println("deleting tradeList file: slopeTraderList.txt");
        IOTextFiles.ioDeleteTextFiles delFile = ioTextFiles.new ioDeleteTextFiles(pathNamePrefix + "slopeTraderList.txt");
        if (delFile.delete() == true){
            System.out.println("File deleted.");           
        }else{
            System.out.println("File does not exist?.");
        }
        
    }
    public void createPositionTradeList(){
        positionCreationDialog positionCreator;       
        positionCreator = new positionCreationDialog(new javax.swing.JFrame(), true, myTradeList); 
        positionCreator.setLastPosCreationParams(posCreationParams);
        positionCreator.setVisible(true);
        posCreationParams = positionCreator.getUpdatedLastPosCreationParams();
        appendToExistingFile = positionCreator.appendToExistingFile();
        userCriteria = positionCreator.getUserCriteria();
        userCanceledPosCreation = positionCreator.getDidUserCancel();
        min90DayVolume = positionCreator.getMin90DayAveVol();
        minPricePerShare = positionCreator.getMinPricePerShare();

        maMustTouchPercent = positionCreator.getMaMustTouchPercent();
        maMaxPiercePercent = positionCreator.getMaMaxPiercePercent();
        ma50DaySz = positionCreator.getMa50DaySz();
        ma100DaySz = positionCreator.getMa100DaySz();
        ma200DaySz = positionCreator.getMa200DaySz();
                
    }
    
}
