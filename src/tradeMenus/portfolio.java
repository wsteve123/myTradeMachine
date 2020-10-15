/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import positions.commonGui;
import ibTradeApi.ibApi;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class portfolio {

    String accountNumber = "";
    int availFunds = 0;
    double percentPerPosition = 0;
    int percentLong = 0;
    int percentShort = 0;
    //String longOrShort = slopeDefs.oLONG;
    int positionBias = slopeDefs.oBiasDisabled;
    int numOfSharesToTrade = 100;
    String portfolioFileName = "portfolio.";
    boolean tradeLongAndShort = false;
    String portfolioFileNamePrefix = "";
    boolean filenameValid = false;
    int tableLineNumber;
    String state;
    int version = 3;
    public String userCriteria = "Empty.";
    int min90DayVolume = 0;
    int minPricePerShare = 0;
    int maMustTouchPercent = 0;
    int maMaxPiercePercent = 0;
    int ma50DaySz = 0;
    int ma100DaySz = 0;
    int ma200DaySz = 0;
    String aliasName = "";
    PosCreationParameters posCreationParams = new PosCreationParameters();
    Positions positions = new Positions();
    PortfolioTrader portfolioTrader;
    //not sure why we need this list...the real one is within positions..
    List<String> tradeList = new ArrayList<>();
    //files for write/read
    IOTextFiles.ioRdTextFiles portfolioRdTextFile;
    IOTextFiles.ioWrTextFiles portfolioWrTextFile;
    IOTextFiles.ioDeleteTextFiles portfolioDeleteTextFile;
/*
    public portfolio() {
        
    }
*/
    public class PosCreationParameters{
        String selectedIndex = "NasdaqQ1";
        int ma50Day = 3;
        int ma100Day = 80;
        int ma200Day = 90;
        boolean longPos = true;
        boolean shortPos = false;
        int selectedPositionBias = slopeDefs.oBiasLong;
        boolean appendToList = true;
        boolean andOp = true;
        boolean orOp = false;
        double maxRunProfit = 15;
        double maxRunLoss = 2;
        String maBullBearSelection = slopeDefs.oBULL_50_200DMA;
        int ma50WinSz = slopeDefs.MA_50DAY_MAXSZ;
        int ma100WinSz = slopeDefs.MA_100DAY_MAXSZ;
        int ma200WinSz = slopeDefs.MA_200DAY_MAXSZ;
        int minVol = 100000;
        int minPrice = 9;
    }
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accIn) {
        accountNumber = accIn;
    }
    public void buildPortfolioName(){
        portfolioFileName += (aliasName + "_" + accountNumber + ".txt");
        filenameValid = true;       
    }
    public int getAvailFunds() {
        return (availFunds);
    }

    public void setAvailFunds(int fundsIn) {
        availFunds = fundsIn;
    }

    public double getPercentPerPos() {
        return (percentPerPosition);
    }

    public void setPercentPerPos(double percIn) {
        percentPerPosition = percIn;
    }
    
    public int getPositionBias() {
        return (positionBias);
    }
    public int getPercentLong() {
        return (percentLong);
    }

    public void setPercentLong(int percIn) {
        percentLong = percIn;
    }

    public int getPercentShort() {
        return (percentShort);
    }

    public void setPercentShort(int percIn) {
        percentShort = percIn;
    }

    public boolean isFilenameValid() {
        return filenameValid;
    }

    public void setAlias(String alIn) {
        aliasName = alIn;
    }

    public String getAlias() {
        return (aliasName);
    }
    public void setUserCriteria(String critIn){
        userCriteria = critIn;
    }
    public void setTradeList(List<String> listIn) {
        tradeList = listIn;
    }
    public void setState(String stateIn){
        state = positions.state = stateIn;
    }
    public List<String> getTradeList() {
        return tradeList;
    }

    public void addToTradeList(String sIn) {
        tradeList.add(sIn);
    }

    public int getTradeListSz() {
        return (tradeList.size());
    }

    public String getPortfolioFilename() {
        return portfolioFileName;
    }
    public void setPortfolioFilename(String fin){
        portfolioFileName = fin;
        portfolioFileNamePrefix = fin.replaceFirst(".txt", ".");
        
    }
    public Positions getPositions() {
        return positions;
    }
    public int getTodaysTrades(){
        return positions.todaysTrades;
    }
    public int getNewCnt(){
        return positions.newPositions;
    }
    public int getOpenCnt(){
        return positions.openPositions;
    }
    public int getClosedCnt(){
        return positions.closedPositions;
    }
    public double getNLV(){
        return 0.0;
    }
    public double getCash(){
        return 0.0;
    }
    public String getState(){
        return state = positions.state;
    }
    public void setNumOfSharesToTrade(int tradeIn){
        this.positions.setNumberOfSharesToTrade(numOfSharesToTrade = tradeIn);
    }
    public void setPositionBias(int biasIn){
        this.positions.setPositionBias(biasIn);
        positionBias = biasIn;
    }
    public void setTradeLongAndShort(boolean ls){
        this.positions.setTradeLongAndShort(ls);
        tradeLongAndShort = ls;
    }
    public void setVersion(int versin){
        this.positions.setVersion(version = versin);
    }
    public boolean isThisNew(){
        boolean newOne;
        
        newOne = ((accountNumber.equals("") && 
                  (availFunds == 0) && (percentPerPosition == 0) && 
                  (percentLong == 0) && (percentShort == 0) &&
                  (positionBias == slopeDefs.oBiasDisabled) &&
                   aliasName.equals("")));
        return newOne;
    }
    public void wrToFile() {
        System.out.println("\nwriting portfolio file: " + portfolioFileName);
        IOTextFiles ioTextFiles = new IOTextFiles();
        portfolioWrTextFile = ioTextFiles.new ioWrTextFiles(portfolioFileName, false);
        portfolioWrTextFile.write(aliasName);
        portfolioWrTextFile.write(accountNumber);
        portfolioWrTextFile.write(Integer.toString(availFunds));
        portfolioWrTextFile.write(Double.toString(percentPerPosition));
        portfolioWrTextFile.write(Integer.toString(percentLong));
        portfolioWrTextFile.write(Integer.toString(percentShort));
        portfolioWrTextFile.write(Integer.toString(positionBias));
        
        portfolioWrTextFile.write(getState());
        portfolioWrTextFile.write(userCriteria);
        portfolioWrTextFile.write(Integer.toString(numOfSharesToTrade));
        portfolioWrTextFile.write(Integer.toString(version));
        //if vers 3 or >, include ma bounce paramters..
        if (version > 2){
            portfolioWrTextFile.write(Integer.toString(maMustTouchPercent));
            portfolioWrTextFile.write(Integer.toString(maMaxPiercePercent));
            portfolioWrTextFile.write(Integer.toString(ma50DaySz));
            portfolioWrTextFile.write(Integer.toString(ma100DaySz));
            portfolioWrTextFile.write(Integer.toString(ma200DaySz));
        }
        portfolioWrTextFile.closeWr();

    }

    public void rdFromFile() {
        String tmpStr = "";
        System.out.println("\nreading portfolio file: " + portfolioFileName);
        IOTextFiles ioTextFiles = new IOTextFiles();
        portfolioRdTextFile = ioTextFiles.new ioRdTextFiles(portfolioFileName, false);
        aliasName = portfolioRdTextFile.read(false);
        accountNumber = portfolioRdTextFile.read(false);
        availFunds = Integer.valueOf(portfolioRdTextFile.read(false));
        //wfs 4.21.16 changed percentPerPosition to double instead of integer...
        tmpStr = portfolioRdTextFile.read(false);
        if(tmpStr.indexOf(".") == -1){
            //-1 no decemal, so must be integer..so convert it..
            percentPerPosition = (double) Integer.valueOf(tmpStr);
        }else{
            //has dec point so must be double already.
            percentPerPosition = Double.valueOf(tmpStr);
        }               
        //percentPerPosition = Integer.valueOf(portfolioRdTextFile.read(false));
        percentLong = Integer.valueOf(portfolioRdTextFile.read(false));
        percentShort = Integer.valueOf(portfolioRdTextFile.read(false));
        positionBias = Integer.valueOf(portfolioRdTextFile.read(false));
        setState(portfolioRdTextFile.read(false));
        userCriteria = portfolioRdTextFile.read(false);
        numOfSharesToTrade = Integer.valueOf(portfolioRdTextFile.read(false));
        version = Integer.valueOf(portfolioRdTextFile.read(false));
        //if vers 3 or >, include ma bounce paramters..
        if (version > 2){
            maMustTouchPercent = Integer.valueOf(portfolioRdTextFile.read(false));
            maMaxPiercePercent = Integer.valueOf(portfolioRdTextFile.read(false));
            ma50DaySz = Integer.valueOf(portfolioRdTextFile.read(false));
            ma100DaySz = Integer.valueOf(portfolioRdTextFile.read(false));
            ma200DaySz = Integer.valueOf(portfolioRdTextFile.read(false));
        }
        portfolioRdTextFile.closeRd();

    }
    public void deleteFile(){
        
        IOTextFiles ioTextFiles = new IOTextFiles();
        portfolioDeleteTextFile = ioTextFiles.new ioDeleteTextFiles(portfolioFileName);
       
        System.out.println("\ndeleting portfolio file: " + portfolioFileName);
        if (portfolioDeleteTextFile.delete() == true){
            System.out.println("File deleted.");
            
        }else{
            System.out.println("File NOT deleted.");
            
        }
    }
    
    public class PortfolioTrader{
        slopeTraderFrameForm slopeTrader;
        /*
        need this to calclulate numOfSharesToTrade for each ticker.
        This value is calculated using maxDollarAmount per pos and 
        price per share. maxDollarPerPos / pricePerShare == shares to 
        buy (max). We need to put this in positions.numOfSharesToTrade before
        we run the trader.
        */
        ibApi actIbApi = ibApi.getActApi();
        ibApi.OptionChain actChain = actIbApi.getActOptionChain();
        ibApi.quoteInfo qInfo = new ibApi.quoteInfo();
        List<String> tradeList = positions.getTradeList();
        public void initTrader(){
            int idx;
            int numOfTickers = tradeList.size();
            String actTicker;
            for (idx = 0; idx < numOfTickers; idx++){
                actTicker = tradeList.get(idx);
                System.out.println("working on opening: " + actTicker + " (" + idx + ") Stream.");
                qInfo = actChain.getQuote(actTicker, false);
            }
            System.out.println("\nopenStreams: Done.");
            myUtils.delay(1000); 
            positions.setStreamsOpened(true);
        }
        
        public void startTrader(){
            state = positions.state = slopeDefs.oRUNNING;
            positions.setMaxPerPos(percentPerPosition);
            positions.setAvailFunds(availFunds);
            positions.setMaMustTouchPercent(maMustTouchPercent);
            positions.setMaMaxPiercePercent((maMaxPiercePercent));
            positions.setMa50daySz(ma50DaySz);
            positions.setMa100daySz(ma100DaySz);
            positions.setMa200daySz(ma200DaySz);
            slopeTrader = new slopeTraderFrameForm(positions);           
            slopeTrader.setVisible(true);
        }
        
    }
    public void createPositions(){
        this.rdFromFile();
        positions = new Positions();
        posCreationParams.selectedPositionBias = positionBias;
        positions.positionBias = positionBias;
        positions.setLastPosCreationParams(posCreationParams);
        positions.createPositionTradeList();
        if (positions.getDidUserCancelPosCreatioin() == false){
            this.setPortfolioFilename(this.portfolioFileName);
            positions.setPathnamePrefix(portfolioFileNamePrefix);
            positions.setAccountNumber(accountNumber);
            positions.setPortfolioAlias(aliasName);
            positions.wrTradeListToFile(positions.appendToExistingFile);
            this.setState(slopeDefs.oREADY);
            this.min90DayVolume = positions.getMin90DayVolume();
            this.minPricePerShare = positions.getMinPricePerShare();
            this.userCriteria = positions.userCriteria;
            this.setNumOfSharesToTrade(this.numOfSharesToTrade);
            this.maMustTouchPercent = positions.getMaMustTouchPercent();
            this.maMaxPiercePercent = positions.getMaMaxPiercePercent();
            this.ma50DaySz = positions.getMa50daySz();
            this.ma100DaySz = positions.getMa100daySz();
            this.ma200DaySz = positions.getMa200daySz();
            this.setVersion(this.version);
            this.wrToFile();
        }
    }
    /*
    public void startTrading(){
        positions = new Positions();
        positions.setPathnamePrefix(portfolioFileNamePrefix);
        positions.rdTradeListFromFile();
        positions.setAccountNumber(accountNumber);
        positions.setPortolioName(portfolioFileName);
        positions.setPortfolioAlias(aliasName);
        portfolioTrader = new PortfolioTrader();
        portfolioTrader.startTrader();
    }
    */
}
