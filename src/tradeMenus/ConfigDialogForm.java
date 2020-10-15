/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import positions.commonGui;

/**
 *
 * @author earlie87
 */
public class ConfigDialogForm extends javax.swing.JDialog {

    private void useExistingConfig(){
        if (traderConfigParams.isPivotSignalSet(actConfig.getOpenWhenCode())){
            openWhenPivotSignalRadioButton.setSelected(true);
        }else{
            openWhenPivotSignalRadioButton.setSelected(false);
        }
        if (traderConfigParams.isPivotSignalSet(actConfig.getCloseWhenCode())){
            closeWhenPivotSignalRadioButton.setSelected(true);
        }else{
            closeWhenPivotSignalRadioButton.setSelected(false);
        }
        if (traderConfigParams.isClosePriceSet(actConfig.getOpenWhenCode())){
            openWhenClosingPriceRadioButton.setSelected(true);
            openClosePriceDaysTextField.setEnabled(true);
            openClosePriceDaysTextField.setText(Integer.toString(actConfig.getOpenCloseDays()));
        }else{
            openWhenClosingPriceRadioButton.setSelected(false);
            openClosePriceDaysTextField.setEnabled(false);
        }
        if (traderConfigParams.isClosePriceSet(actConfig.getCloseWhenCode())){
            closeWhenClosingPriceRadioButton.setSelected(true);
            closeClosePriceDaysTextField.setEnabled(true);
            closeClosePriceDaysTextField.setText(Integer.toString(actConfig.getCloseCloseDays()));
        }else{
            closeWhenClosingPriceRadioButton.setSelected(false);
            closeClosePriceDaysTextField.setEnabled(false);
        }
        if (traderConfigParams.isTrendGoodSet(actConfig.getOpenWhenCode())){
            openWhenTrendConfirmsRadioButton.setSelected(true);
            openTrendConfirmsDaysTextField.setEnabled(true);
            openTrendConfirmsDaysTextField.setText(Integer.toString(actConfig.getOpenTrendDays()));   
        }else{
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setText(Integer.toString(actConfig.getOpenTrendDays()));
        }
        if (traderConfigParams.isTrendGoodSet(actConfig.getCloseWhenCode())){
            closeWhenTrendConfirmsRadioButton.setSelected(true);           
        }else{
            closeWhenTrendConfirmsRadioButton.setSelected(false);
        }
        if (traderConfigParams.isReopenSet(actConfig.getCloseWhenCode())){
            closeWhenReopenRadioButton.setSelected(true);
            closeReopenDaysTextField.setEnabled(true);
            closeReopenDaysTextField.setText(Integer.toString(actConfig.getCloseReopenDays()));
        }else{
            closeWhenReopenRadioButton.setSelected(false);
            closeReopenDaysTextField.setEnabled(false);
            closeReopenDaysTextField.setText(Integer.toString(actConfig.getCloseReopenDays()));
        }
        if ((traderConfigParams.isPivotSignalSet(openWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(openWhenCode) == true)) {
            openWhenOrRadioButton.setEnabled(true);
            if (traderConfigParams.isOrSet(openWhenCode)) {
                openWhenOrRadioButton.setSelected(true);
            } else {
                openWhenOrRadioButton.setSelected(false);
                traderConfigParams.clrWhenOrSelected(openWhenCode);
            }
        } else {
            openWhenOrRadioButton.setEnabled(false);
        }
        if ((traderConfigParams.isPivotSignalSet(closeWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(closeWhenCode) == true)) {
            closeWhenOrRadioButton.setEnabled(true);
            if (traderConfigParams.isOrSet(closeWhenCode)) {
                closeWhenOrRadioButton.setSelected(true);
            } else {
                closeWhenOrRadioButton.setSelected(false);
            }
        } else {
            closeWhenOrRadioButton.setSelected(false);
            closeWhenOrRadioButton.setEnabled(false);
            traderConfigParams.clrWhenOrSelected(closeWhenCode);
        }
        
        if (traderConfigParams.isMaBounceSet(openWhenCode) == true){                
            openWhenBouncesOffMaRadioButton.setEnabled(true);
            openWhenBouncesOffMaRadioButton.setSelected(true);
            bouncesOffMaWithinPercentTextField.setEnabled(true);
            bouncesOffMaWithinPercentTextField.setText(Integer.toString(actConfig.getOpenMaBouncePercent()));
            
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            
        } else {
            openWhenBouncesOffMaRadioButton.setSelected(false);
            //openWhenBouncesOffMaRadioButton.setEnabled(false);
            bouncesOffMaWithinPercentTextField.setEnabled(false);
            traderConfigParams.clrWhenMaBounce(openWhenCode);
        }       
        
        if (traderConfigParams.isWeaknessSet(openWhenCode) == true){                
            openOnWeaknessRadioButton.setEnabled(true);
            openOnWeaknessRadioButton.setSelected(true);
            overShootTextField.setEnabled(true);
            overShootTextField.setText(Integer.toString(actConfig.getOverShootPercent()));
            
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            
        } else {
            openOnWeaknessRadioButton.setSelected(false);
            overShootTextField.setEnabled(false);
            traderConfigParams.clrWhenWeakness(openWhenCode);
        }       
        
        if (traderConfigParams.isStrengthSet(openWhenCode) == true){                
            openOnStrengthRadioButton.setEnabled(true);
            openOnStrengthRadioButton.setSelected(true);
            overShootTextField.setEnabled(true);
            overShootTextField.setText(Integer.toString(actConfig.getOverShootPercent()));
            
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            
        } else {
            openOnStrengthRadioButton.setSelected(false);
            overShootTextField.setEnabled(false);
            traderConfigParams.clrWhenStrength(openWhenCode);
        }
        if (traderConfigParams.isWeaknessSet(closeWhenCode) == true){                
            closeOnWeaknessRadioButton.setEnabled(true);
            closeOnWeaknessRadioButton.setSelected(true);
            overShootTextField.setEnabled(true);
            overShootTextField.setText(Integer.toString(actConfig.getOverShootPercent()));
            
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            
        } else {
            closeOnWeaknessRadioButton.setSelected(false);
            overShootTextField.setEnabled(false);
            traderConfigParams.clrWhenWeakness(closeWhenCode);
        } 
        if (traderConfigParams.isStrengthSet(closeWhenCode) == true){                
            closeOnStrengthRadioButton.setEnabled(true);
            closeOnStrengthRadioButton.setSelected(true);
            overShootTextField.setEnabled(true);
            overShootTextField.setText(Integer.toString(actConfig.getOverShootPercent()));
            
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
            
        } else {
            closeOnStrengthRadioButton.setSelected(false);  
            overShootTextField.setEnabled(false);
            traderConfigParams.clrWhenStrength(closeWhenCode);
        }
        
        currentBias = traderConfigParams.getBias();
    }
    private void useNewConfig() {
        if (openWhenPivotSignalRadioButton.isSelected() == true) {
            openWhenCode = traderConfigParams.setWhenPivotSignal(openWhenCode);

        } else {
            openWhenCode = traderConfigParams.clrWhenPivotSignal(openWhenCode);
        }
        if (closeWhenPivotSignalRadioButton.isSelected() == true) {
            closeWhenCode = traderConfigParams.setWhenPivotSignal(closeWhenCode);

        } else {
            closeWhenCode = traderConfigParams.clrWhenPivotSignal(closeWhenCode);
        }

        if (openWhenClosingPriceRadioButton.isSelected() == true) {
            openWhenCode = traderConfigParams.setWhenClosePrice(openWhenCode);
            openClosePriceDaysTextField.setEnabled(true);

        } else {
            openWhenCode = traderConfigParams.clrWhenClosePrice(openWhenCode);
            openClosePriceDaysTextField.setEnabled(false);
        }

        if (closeWhenClosingPriceRadioButton.isSelected() == true) {
            closeWhenCode = traderConfigParams.setWhenClosePrice(closeWhenCode);
            closeClosePriceDaysTextField.setEnabled(true);

        } else {
            closeWhenCode = traderConfigParams.clrWhenClosePrice(closeWhenCode);
            closeClosePriceDaysTextField.setEnabled(false);
        }
        
        if (openWhenTrendConfirmsRadioButton.isSelected() == true){
            openWhenCode = traderConfigParams.setWhenTrendGood(openWhenCode);
            openTrendConfirmsDaysTextField.setEnabled(true);
         }else{
            openWhenCode = traderConfigParams.clrWhenTrendGood(openWhenCode);
            openTrendConfirmsDaysTextField.setEnabled(false);
         }
        if (openWhenBouncesOffMaRadioButton.isSelected() == true){
            openWhenBouncesOffMaRadioButton.setEnabled(true);
            openWhenBouncesOffMaRadioButton.setSelected(true);
            bouncesOffMaWithinPercentTextField.setEnabled(true);
            openWhenCode = traderConfigParams.setWhenMaBounce(openWhenCode);
                       
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
         }else{
            openWhenCode = traderConfigParams.clrWhenMaBounce(openWhenCode);
            openWhenBouncesOffMaRadioButton.setSelected(false);
            bouncesOffMaWithinPercentTextField.setEnabled(false);
            openWhenBouncesOffMaRadioButton.setEnabled(true);
         }
        if (openOnWeaknessRadioButton.isSelected() == true){
            openOnWeaknessRadioButton.setEnabled(true);
            openOnWeaknessRadioButton.setSelected(true);
            openWhenCode = traderConfigParams.setWhenWeakness(openWhenCode);
            overShootTextField.setEnabled(true);
                       
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
         }else{
            openWhenCode = traderConfigParams.clrWhenWeakness(openWhenCode);
            openOnWeaknessRadioButton.setSelected(false);
            openOnWeaknessRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
         }
        if (closeOnWeaknessRadioButton.isSelected() == true){
            closeOnWeaknessRadioButton.setEnabled(true);
            closeOnWeaknessRadioButton.setSelected(true);
            closeWhenCode = traderConfigParams.setWhenWeakness(closeWhenCode);
            overShootTextField.setEnabled(true);
            
            closeWhenClosingPriceRadioButton.setSelected(false);
            closeWhenClosingPriceRadioButton.setEnabled(false);
            closeWhenPivotSignalRadioButton.setSelected(false);
            closeWhenPivotSignalRadioButton.setEnabled(false);
            closeWhenTrendConfirmsRadioButton.setSelected(false);
            closeWhenTrendConfirmsRadioButton.setEnabled(false);
            closeWhenOrRadioButton.setSelected(false);
            closeWhenOrRadioButton.setEnabled(false);
            closeClosePriceDaysTextField.setEnabled(false);
            
         }else{
            closeWhenCode = traderConfigParams.clrWhenWeakness(closeWhenCode);
            closeOnWeaknessRadioButton.setSelected(false);
            closeOnWeaknessRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
         }
        if (openOnStrengthRadioButton.isSelected() == true){
            openOnStrengthRadioButton.setEnabled(true);
            openOnStrengthRadioButton.setSelected(true);
            openWhenCode = traderConfigParams.setWhenStrength(openWhenCode);
            overShootTextField.setEnabled(true);
                       
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
         }else{
            openWhenCode = traderConfigParams.clrWhenStrength(openWhenCode);
            openOnStrengthRadioButton.setSelected(false);
            openOnStrengthRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
         }
        if (closeOnStrengthRadioButton.isSelected() == true){
            closeOnStrengthRadioButton.setEnabled(true);
            closeOnStrengthRadioButton.setSelected(true);
            closeWhenCode = traderConfigParams.setWhenStrength(closeWhenCode);
            overShootTextField.setEnabled(true);
            
            closeWhenClosingPriceRadioButton.setSelected(false);
            closeWhenClosingPriceRadioButton.setEnabled(false);
            closeWhenPivotSignalRadioButton.setSelected(false);
            closeWhenPivotSignalRadioButton.setEnabled(false);
            closeWhenTrendConfirmsRadioButton.setSelected(false);
            closeWhenTrendConfirmsRadioButton.setEnabled(false);
            closeWhenOrRadioButton.setSelected(false);
            closeWhenOrRadioButton.setEnabled(false);
            closeClosePriceDaysTextField.setEnabled(false);
            
         }else{
            closeWhenCode = traderConfigParams.clrWhenStrength(closeWhenCode);
            closeOnStrengthRadioButton.setSelected(false);
            closeOnStrengthRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
         }       
        
       /*
        openWhenTrendConfirmsRadioButton.setEnabled(false);
        openTrendConfirmsDaysTextField.setEnabled(false);
         */
        if (closeWhenReopenRadioButton.isSelected() == true) {
            closeWhenCode = traderConfigParams.setWhenReopen(closeWhenCode);
            closeReopenDaysTextField.setEnabled(true);
        } else {
            closeWhenCode = traderConfigParams.clrWhenReopen(closeWhenCode);
            closeReopenDaysTextField.setEnabled(false);
        }
        if (closeWhenTrendConfirmsRadioButton.isSelected() == true) {
            closeWhenCode = traderConfigParams.setWhenTrendGood(closeWhenCode);
        } else {
            closeWhenCode = traderConfigParams.clrWhenTrendGood(closeWhenCode);           
        }
        if ((traderConfigParams.isPivotSignalSet(openWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(openWhenCode) == true)) {
            openWhenOrRadioButton.setEnabled(true);
            if (openWhenOrRadioButton.isSelected() == true) {
                openWhenCode = traderConfigParams.setWhenOrSelected(openWhenCode);
            } else {
                openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode);
            }
        } else {
            //if pivot AND closePrice not true, then de-select OR and clr bit 
            openWhenOrRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode);
            openWhenOrRadioButton.setEnabled(false);
        }
        if ((traderConfigParams.isPivotSignalSet(closeWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(closeWhenCode) == true)) {
            closeWhenOrRadioButton.setEnabled(true);
            if (closeWhenOrRadioButton.isSelected() == true) {
                closeWhenCode = traderConfigParams.setWhenOrSelected(closeWhenCode);
            } else {
                closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
            }
        } else {
            //if pivot AND closePrice not true, then de-select OR and clr bit 
            closeWhenOrRadioButton.setSelected(false);
            closeWhenOrRadioButton.setEnabled(false);
            closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
        }
        currentBias = traderConfigParams.getBias();

    }
    /**
     * Creates new form ConfigDialogForm
     */
    public ConfigDialogForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        if ((actConfig != null) && (actConfig.getGoodInput() == true)){
            useExistingConfig();
        }else{
            useNewConfig();
        }
        
    }
    public boolean getGoodInput(){
        return(userGoodness);
    }
    
    traderConfigParams actConfig = null;
    public void setTraderConfigParams(traderConfigParams tc){
        actConfig = tc;
        if (actConfig.getGoodInput() == true){
            fetchConfigParams(actConfig);
        }
    }
    public void setConfigNow(){
        if ((actConfig != null) && (actConfig.getGoodInput() == true)){
            useExistingConfig();
        }
    }
    public void create(int bias){
        actConfig = new traderConfigParams(bias);
        currentBias = bias;
        biasLable.setText(slopeDefs.getPositionBiasStr(bias));
    }
    public String getCriteriaStrForLable(){
        criteriaStr = fillCriteriaStrForLable();
        return criteriaStr;
    }
    public String getCriteriaStr(){
        criteriaStr = fillCriteriaStr();
        return criteriaStr;
    }
    public void wrConfigToFile(){
        
    }
    public void rdConfigFromFile(){
        
    }
    
    boolean userGoodness = true;
    
    int openWhenCode = traderConfigParams.DISABLED_INT;
    int closeWhenCode = traderConfigParams.DISABLED_INT;
    int openCloseDays = 0;
    int closeCloseDays = 0;
    int openTrendDays = 0;
    int closeTrendDays = 0;
    int closeReopenDays = 0;
    int currentBias = 0;
    int bouncesOffMaWithinPercent = 0;
    int overShootPercent = 0;
    String criteriaStr = "empty";
    
    private void storeConfigParams(traderConfigParams tc){
        tc.setOpenWhenCode(openWhenCode);
        tc.setCloseWhenCode(closeWhenCode);
        tc.setOpenCloseDays(openCloseDays);
        tc.setCloseCloseDays(closeCloseDays);
        tc.setOpenTrendDays(openTrendDays);
        tc.setCloseTrendDays(closeTrendDays);
        tc.setCurrentBias(currentBias);
        tc.setGoodInput(userGoodness);
        tc.setCloseReopenDays(closeReopenDays);
        tc.setOpenMaBouncePercent(bouncesOffMaWithinPercent);
        tc.setOverShootPercent(overShootPercent);
    }
    private void fetchConfigParams(traderConfigParams tc){
        openWhenCode = tc.getOpenWhenCode();
        closeWhenCode = tc.getCloseWhenCode();
        openCloseDays = tc.getOpenCloseDays();
        closeCloseDays = tc.getCloseCloseDays();
        openTrendDays = tc.getOpenTrendDays();
        closeTrendDays = tc.getCloseTrendDays();
        currentBias = tc.getCurrentBias();
        userGoodness = tc.getGoodInput();
        closeReopenDays = tc.getCloseReopenDays();
        bouncesOffMaWithinPercent = tc.getOpenMaBouncePercent();
        overShootPercent = tc.getOverShootPercent();
    }
            
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();
        biasLable = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        displayButton = new javax.swing.JButton();
        openWhenPivotSignalRadioButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        openWhenClosingPriceRadioButton = new javax.swing.JRadioButton();
        openWhenTrendConfirmsRadioButton = new javax.swing.JRadioButton();
        openWhenOrRadioButton = new javax.swing.JRadioButton();
        closeWhenPivotSignalRadioButton = new javax.swing.JRadioButton();
        closeWhenClosingPriceRadioButton = new javax.swing.JRadioButton();
        closeWhenReopenRadioButton = new javax.swing.JRadioButton();
        closeWhenOrRadioButton = new javax.swing.JRadioButton();
        openClosePriceDaysTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        openTrendConfirmsDaysTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        closeClosePriceDaysTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        closeReopenDaysTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        closeWhenTrendConfirmsRadioButton = new javax.swing.JRadioButton();
        openWhenBouncesOffMaRadioButton = new javax.swing.JRadioButton();
        jLabel8 = new javax.swing.JLabel();
        bouncesOffMaWithinPercentTextField = new javax.swing.JTextField();
        openOnWeaknessRadioButton = new javax.swing.JRadioButton();
        openOnStrengthRadioButton = new javax.swing.JRadioButton();
        closeOnWeaknessRadioButton = new javax.swing.JRadioButton();
        closeOnStrengthRadioButton = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        overShootTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Open and Close Criteria ");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        biasLable.setText("        ");

        jLabel5.setText("CurrentBias:");

        displayButton.setText("Display");
        displayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                displayButtonActionPerformed(evt);
            }
        });

        openWhenPivotSignalRadioButton.setText("PivotSignal");
        openWhenPivotSignalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openWhenPivotSignalRadioButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("OpenOn:");

        jLabel3.setText("CloseOn");

        openWhenClosingPriceRadioButton.setText("ClosingPrice");
        openWhenClosingPriceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openWhenClosingPriceRadioButtonActionPerformed(evt);
            }
        });

        openWhenTrendConfirmsRadioButton.setText("AndTrendConfirms");
        openWhenTrendConfirmsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openWhenTrendConfirmsRadioButtonActionPerformed(evt);
            }
        });

        openWhenOrRadioButton.setSelected(true);
        openWhenOrRadioButton.setText("OR above");
        openWhenOrRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openWhenOrRadioButtonActionPerformed(evt);
            }
        });

        closeWhenPivotSignalRadioButton.setText("PivotSignal");
        closeWhenPivotSignalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWhenPivotSignalRadioButtonActionPerformed(evt);
            }
        });

        closeWhenClosingPriceRadioButton.setText("ClosingPrice");
        closeWhenClosingPriceRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWhenClosingPriceRadioButtonActionPerformed(evt);
            }
        });

        closeWhenReopenRadioButton.setText("Re-open after");
        closeWhenReopenRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWhenReopenRadioButtonActionPerformed(evt);
            }
        });

        closeWhenOrRadioButton.setSelected(true);
        closeWhenOrRadioButton.setText("OR above");
        closeWhenOrRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWhenOrRadioButtonActionPerformed(evt);
            }
        });

        openClosePriceDaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openClosePriceDaysTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Days:");

        openTrendConfirmsDaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrendConfirmsDaysTextFieldActionPerformed(evt);
            }
        });

        jLabel4.setText("Days:");

        closeClosePriceDaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeClosePriceDaysTextFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Days:");

        closeReopenDaysTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeReopenDaysTextFieldActionPerformed(evt);
            }
        });

        jLabel7.setText("Days:");

        closeWhenTrendConfirmsRadioButton.setText("TrendConfirms");
        closeWhenTrendConfirmsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWhenTrendConfirmsRadioButtonActionPerformed(evt);
            }
        });

        openWhenBouncesOffMaRadioButton.setText("BouncesOffMa");
        openWhenBouncesOffMaRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openWhenBouncesOffMaRadioButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("WithIn%");

        bouncesOffMaWithinPercentTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bouncesOffMaWithinPercentTextFieldActionPerformed(evt);
            }
        });

        openOnWeaknessRadioButton.setText("Weakness");
        openOnWeaknessRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openOnWeaknessRadioButtonActionPerformed(evt);
            }
        });

        openOnStrengthRadioButton.setText("Strength");
        openOnStrengthRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openOnStrengthRadioButtonActionPerformed(evt);
            }
        });

        closeOnWeaknessRadioButton.setText("Weakness");
        closeOnWeaknessRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeOnWeaknessRadioButtonActionPerformed(evt);
            }
        });

        closeOnStrengthRadioButton.setText("Strength");
        closeOnStrengthRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeOnStrengthRadioButtonActionPerformed(evt);
            }
        });

        jLabel9.setText("OverShoot%");

        overShootTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overShootTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(biasLable, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(123, 123, 123))))
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(doneButton)
                        .addGap(183, 183, 183))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(openWhenClosingPriceRadioButton)
                                    .addGap(22, 22, 22)
                                    .addComponent(jLabel1))
                                .addComponent(openWhenOrRadioButton)
                                .addComponent(openWhenPivotSignalRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(openWhenTrendConfirmsRadioButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel4)))
                            .addGap(6, 6, 6)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(openClosePriceDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(openTrendConfirmsDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(129, 129, 129)
                            .addComponent(displayButton))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(openWhenBouncesOffMaRadioButton)
                                .addComponent(openOnWeaknessRadioButton)
                                .addComponent(openOnStrengthRadioButton))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel9)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel8)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(bouncesOffMaWithinPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(21, 21, 21)
                                    .addComponent(overShootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(closeWhenTrendConfirmsRadioButton)
                            .addComponent(closeWhenPivotSignalRadioButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(closeWhenClosingPriceRadioButton)
                                .addGap(22, 22, 22)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(closeClosePriceDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(closeWhenOrRadioButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(closeWhenReopenRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(closeReopenDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(closeOnWeaknessRadioButton)
                            .addComponent(closeOnStrengthRadioButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addGap(62, 62, 62)))
                .addGap(18, 18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(biasLable)
                    .addComponent(jLabel5))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeWhenPivotSignalRadioButton)
                    .addComponent(openWhenPivotSignalRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openWhenClosingPriceRadioButton)
                    .addComponent(closeWhenClosingPriceRadioButton)
                    .addComponent(openClosePriceDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(closeClosePriceDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeWhenOrRadioButton)
                    .addComponent(openWhenOrRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openWhenTrendConfirmsRadioButton)
                    .addComponent(jLabel4)
                    .addComponent(openTrendConfirmsDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeWhenTrendConfirmsRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeWhenReopenRadioButton)
                    .addComponent(jLabel7)
                    .addComponent(closeReopenDaysTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openWhenBouncesOffMaRadioButton)
                    .addComponent(jLabel8)
                    .addComponent(bouncesOffMaWithinPercentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openOnWeaknessRadioButton)
                    .addComponent(closeOnWeaknessRadioButton)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openOnStrengthRadioButton)
                    .addComponent(closeOnStrengthRadioButton)
                    .addComponent(overShootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(doneButton)
                    .addComponent(displayButton))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        userGoodness = false;
        setVisible(false);
        dispose();
     
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:
        userGoodness = true;
        storeConfigParams(actConfig);
        actConfig.setGoodInput(userGoodness);
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed
    private String fillCriteriaStrForLable(){
        String  dispStr = "";
        int numSet = traderConfigParams.howManySet(openWhenCode);
        dispStr = "<html>";
        dispStr += "<br>Open Position when: ";
        if (traderConfigParams.isPivotSignalSet(openWhenCode) == true){
            dispStr += "Pivot Occures";
        }
        if ((traderConfigParams.isPivotSignalSet(openWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(openWhenCode) == true)) {
            if (traderConfigParams.isOrSet(openWhenCode) == true) {
                dispStr += " OR ";
            } else {
                dispStr += " AND ";
            }
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong) {
                dispStr += "is Above MA for " + openCloseDays + " Days ";
            } else if (currentBias == slopeDefs.oBiasShort) {
                dispStr += "is Below MA for " + openCloseDays + " Days ";
            }
        } else if (traderConfigParams.isClosePriceSet(openWhenCode) == true) {
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong) {
                dispStr += "is Above MA for " + openCloseDays + " Days ";
            } else if (currentBias == slopeDefs.oBiasShort) {
                dispStr += "is Below MA for " + openCloseDays + " Days ";
            }
        }

        if (traderConfigParams.isTrendGoodSet(openWhenCode) == true){
            if ((numSet > 1) && traderConfigParams.isOrSet(openWhenCode) == true){
                dispStr += " OR ";
            }else if ((numSet > 1) && (traderConfigParams.isOrSet(openWhenCode) == false)){
                dispStr += " AND ";
            }
            dispStr += "Trend Confirms ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "Long After " + openTrendDays + " Days";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "Short After " + openTrendDays + " Days";                
            }
        }
        
        numSet = traderConfigParams.howManySet(closeWhenCode);
        dispStr += "<br><br>Close Position when: ";
        if ((traderConfigParams.isPivotSignalSet(closeWhenCode) == true) && 
            (traderConfigParams.isClosePriceSet(closeWhenCode) == true)){
            dispStr += "Pivot Occures";
            if (traderConfigParams.isOrSet(closeWhenCode) == true){
                dispStr += " OR ";
            }else {
                dispStr += " AND ";
            }
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "is Below MA for " + closeCloseDays + " Days ";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "is Above MA for " + closeCloseDays + " Days ";                
            }
        }else if (traderConfigParams.isPivotSignalSet(closeWhenCode) == true){
            dispStr += "Pivot Occures";   
        }else if (traderConfigParams.isClosePriceSet(closeWhenCode) == true){
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "is Below MA for " + closeCloseDays + " Days ";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "is Above MA for " + closeCloseDays + " Days ";                
            }
        }        
        if ((traderConfigParams.isTrendGoodSet(closeWhenCode) == true) && 
            (traderConfigParams.isClosePriceSet(closeWhenCode) == true)){           
            dispStr += "And Re-open if Trend Confirms ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "Up After " + closeReopenDays + " Days";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "Down After " + closeReopenDays + " Days";                
            }
        }  
        dispStr += "</html>";
        return dispStr;
    }
    private String fillCriteriaStr(){
        String  dispStr = "";
        int numSet = traderConfigParams.howManySet(openWhenCode);
        
        dispStr = "\nOpen Position when: ";
        if (traderConfigParams.isPivotSignalSet(openWhenCode) == true){
            dispStr += "Pivot Occures";
        }
        if ((traderConfigParams.isPivotSignalSet(openWhenCode) == true)
                && (traderConfigParams.isClosePriceSet(openWhenCode) == true)) {
            if (traderConfigParams.isOrSet(openWhenCode) == true) {
                dispStr += " OR ";
            } else {
                dispStr += " AND ";
            }
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong) {
                dispStr += "is Above MA for " + openCloseDays + " Days ";
            } else if (currentBias == slopeDefs.oBiasShort) {
                dispStr += "is Below MA for " + openCloseDays + " Days ";
            }
        } else if (traderConfigParams.isClosePriceSet(openWhenCode) == true) {
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong) {
                dispStr += "is Above MA for " + openCloseDays + " Days ";
            } else if (currentBias == slopeDefs.oBiasShort) {
                dispStr += "is Below MA for " + openCloseDays + " Days ";
            }
        }

        if (traderConfigParams.isTrendGoodSet(openWhenCode) == true){
            if ((numSet > 1) && traderConfigParams.isOrSet(openWhenCode) == true){
                dispStr += " OR ";
            }else if ((numSet > 1) && (traderConfigParams.isOrSet(openWhenCode) == false)){
                dispStr += " AND ";
            }
            dispStr += "Trend Confirms ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "Long After " + openTrendDays + " Days";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "Short After " + openTrendDays + " Days";                
            }
        }
        if(traderConfigParams.isWeaknessSet(openWhenCode) == true){
           dispStr += "Weakness Begins "; 
        }else if(traderConfigParams.isStrengthSet(openWhenCode) == true){
           dispStr += "Strength Begins "; 
        }
        numSet = traderConfigParams.howManySet(closeWhenCode);
        dispStr += "\n\nClose Position when: ";
        if ((traderConfigParams.isPivotSignalSet(closeWhenCode) == true) && 
            (traderConfigParams.isClosePriceSet(closeWhenCode) == true)){
            dispStr += "Pivot Occures";
            if (traderConfigParams.isOrSet(closeWhenCode) == true){
                dispStr += " OR ";
            }else {
                dispStr += " AND ";
            }
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "is Below MA for " + closeCloseDays + " Days ";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "is Above MA for " + closeCloseDays + " Days ";                
            }
        }else if (traderConfigParams.isPivotSignalSet(closeWhenCode) == true){
            dispStr += "Pivot Occures";   
        }else if (traderConfigParams.isClosePriceSet(closeWhenCode) == true){
            dispStr += "Closing Price ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "is Below MA for " + closeCloseDays + " Days ";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "is Above MA for " + closeCloseDays + " Days ";                
            }
        }
        if (traderConfigParams.isTrendGoodSet(closeWhenCode) == true){
            dispStr += "AND Trend Confirms "; 
        }
        if ((traderConfigParams.isReopenSet(closeWhenCode) == true) && 
            (traderConfigParams.isClosePriceSet(closeWhenCode) == true)){           
            dispStr += "And Re-open if Trend Confirms ";
            if (currentBias == slopeDefs.oBiasLong){
                dispStr += "Up After " + closeReopenDays + " Days";    
            }else if (currentBias == slopeDefs.oBiasShort){
                dispStr += "Down After " + closeReopenDays + " Days";                
            }
        }
        if(traderConfigParams.isWeaknessSet(closeWhenCode) == true){
           dispStr += "Weakness Begins "; 
        }else if(traderConfigParams.isStrengthSet(closeWhenCode) == true){
           dispStr += "Strength Begins "; 
        }
        return dispStr;
    }
    public void displayCriteria(){
        int numSet = traderConfigParams.howManySet(openWhenCode);
        
        String dispStr = "For Open:" + "\n";
     
        dispStr += (traderConfigParams.isPivotSignalSet(openWhenCode) ? "pivotSig set" : "pviotSig Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isClosePriceSet(openWhenCode) ? "closePrice set" : "closePrice Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isTrendGoodSet(openWhenCode) ? "trendGood set" : "trendGood Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isOrSet(openWhenCode) ? "OR set" : "OR Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isWeaknessSet(openWhenCode) ? "Weakness set" : "Weakness Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isStrengthSet(openWhenCode) ? "Strength set" : "Strength Clrd");
        dispStr += "For Close:" + "\n";
        dispStr += (traderConfigParams.isPivotSignalSet(closeWhenCode) ? "pivotSig set" : "pviotSig Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isClosePriceSet(closeWhenCode) ? "closePrice set" : "closePrice Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isTrendGoodSet(closeWhenCode) ? "trendGood set" : "trendGood Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isOrSet(closeWhenCode) ? "OR set" : "OR Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isWeaknessSet(closeWhenCode) ? "Weakness set" : "Weakness Clrd");
        dispStr += "\n";
        dispStr += (traderConfigParams.isStrengthSet(closeWhenCode) ? "Strength set" : "Strength Clrd");
        dispStr += "total closeWhenCode set: " + traderConfigParams.howManySet(closeWhenCode) +            
                   " openWhenCode set: " + traderConfigParams.howManySet(openWhenCode);
                
        dispStr += "\ndone.";
        commonGui.postInformationMsg(dispStr);
        
        dispStr = fillCriteriaStr();
 
        criteriaStr = dispStr;
        commonGui.postInformationMsg(dispStr);
        
        System.out.println();    
    }
    private void displayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_displayButtonActionPerformed
        // TODO add your handling code here:
        displayCriteria();       
    }//GEN-LAST:event_displayButtonActionPerformed

    private void openWhenPivotSignalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openWhenPivotSignalRadioButtonActionPerformed
        // TODO add your handling code here:
        if (openWhenPivotSignalRadioButton.isSelected() == true){
            openWhenCode = traderConfigParams.setWhenPivotSignal(openWhenCode); 
            if (traderConfigParams.isClosePriceSet(openWhenCode) == true){
                openWhenOrRadioButton.setEnabled(true);
                openWhenCode = ((openWhenOrRadioButton.isSelected() == true) ? 
                        traderConfigParams.setWhenOrSelected(openWhenCode) : 
                        traderConfigParams.clrWhenOrSelected(openWhenCode));
            }else{
                openWhenOrRadioButton.setSelected(false);
                openWhenOrRadioButton.setEnabled(false);
                traderConfigParams.clrWhenOrSelected(openWhenCode);
            }
        }else{
            openWhenCode = traderConfigParams.clrWhenPivotSignal(openWhenCode);
            openWhenOrRadioButton.setEnabled(false);
        }
        
        
    }//GEN-LAST:event_openWhenPivotSignalRadioButtonActionPerformed

    private void openWhenClosingPriceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openWhenClosingPriceRadioButtonActionPerformed
        // TODO add your handling code here:
        if (openWhenClosingPriceRadioButton.isSelected() == true){
            openWhenCode = traderConfigParams.setWhenClosePrice(openWhenCode); 
            openClosePriceDaysTextField.setEnabled(true);
            if (traderConfigParams.isPivotSignalSet(openWhenCode) == true){
                openWhenOrRadioButton.setEnabled(true);
                openWhenCode = ((openWhenOrRadioButton.isSelected() == true) ? 
                        traderConfigParams.setWhenOrSelected(openWhenCode) : 
                        traderConfigParams.clrWhenOrSelected(openWhenCode));               
            }else{
                openWhenOrRadioButton.setSelected(false);
                openWhenOrRadioButton.setEnabled(false);
                traderConfigParams.clrWhenOrSelected(openWhenCode);
            }
        }else{
            openWhenCode = traderConfigParams.clrWhenClosePrice(openWhenCode);
            openClosePriceDaysTextField.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenOrRadioButton.setEnabled(false);
            traderConfigParams.clrWhenOrSelected(openWhenCode);
        }
    }//GEN-LAST:event_openWhenClosingPriceRadioButtonActionPerformed

    private void openWhenTrendConfirmsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openWhenTrendConfirmsRadioButtonActionPerformed
        // TODO add your handling code here:
  
        if (openWhenTrendConfirmsRadioButton.isSelected() == true) {
            openWhenCode = traderConfigParams.setWhenTrendGood(openWhenCode);
            openTrendConfirmsDaysTextField.setEnabled(true);
        } else {
            openWhenCode = traderConfigParams.clrWhenTrendGood(openWhenCode);
            openTrendConfirmsDaysTextField.setEnabled(false);
        }

    }//GEN-LAST:event_openWhenTrendConfirmsRadioButtonActionPerformed

    private void openWhenOrRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openWhenOrRadioButtonActionPerformed
        // TODO add your handling code here:
        if ((openWhenPivotSignalRadioButton.isSelected() == true) &&
            (openWhenClosingPriceRadioButton.isSelected() == true)){
            openWhenOrRadioButton.setEnabled(true);
           if (openWhenOrRadioButton.isSelected() == true){
                openWhenCode = traderConfigParams.setWhenOrSelected(openWhenCode);    
            }else{
                openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode); 
            }               
        }else{
            //if not both Pivot AND Cose then clr or..
            openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode); 
        }
    }//GEN-LAST:event_openWhenOrRadioButtonActionPerformed

    private void closeWhenPivotSignalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWhenPivotSignalRadioButtonActionPerformed
        // TODO add your handling code here:
        if (closeWhenPivotSignalRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenPivotSignal(closeWhenCode); 
            if (traderConfigParams.isClosePriceSet(closeWhenCode) == true){
                closeWhenOrRadioButton.setEnabled(true);
                closeWhenCode = ((closeWhenOrRadioButton.isSelected() == true) ? 
                        traderConfigParams.setWhenOrSelected(closeWhenCode) : 
                        traderConfigParams.clrWhenOrSelected(closeWhenCode));
            }else{
                closeWhenOrRadioButton.setSelected(false);
                closeWhenOrRadioButton.setEnabled(false);
                closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
            }
        }else{
            closeWhenOrRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenPivotSignal(closeWhenCode);
            traderConfigParams.clrWhenOrSelected(closeWhenCode);
            closeWhenOrRadioButton.setEnabled(false);
        }
    }//GEN-LAST:event_closeWhenPivotSignalRadioButtonActionPerformed

    private void closeWhenClosingPriceRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWhenClosingPriceRadioButtonActionPerformed
        // TODO add your handling code here:
        if (closeWhenClosingPriceRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenClosePrice(closeWhenCode);  
            closeClosePriceDaysTextField.setEnabled(true);
            if (traderConfigParams.isPivotSignalSet(closeWhenCode) == true){
                closeWhenOrRadioButton.setEnabled(true);                
            }else{
                closeWhenOrRadioButton.setEnabled(false);
            }
        }else{
            closeWhenCode = traderConfigParams.clrWhenClosePrice(closeWhenCode);             
            closeClosePriceDaysTextField.setEnabled(false);
            closeWhenOrRadioButton.setSelected(false);
            closeWhenOrRadioButton.setEnabled(false);
            traderConfigParams.clrWhenOrSelected(closeWhenCode);
        }
    }//GEN-LAST:event_closeWhenClosingPriceRadioButtonActionPerformed

    private void closeWhenReopenRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWhenReopenRadioButtonActionPerformed
        // TODO add your handling code here:
         if (closeWhenReopenRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenReopen(closeWhenCode);
            closeReopenDaysTextField.setEnabled(true);
        }else{
            closeWhenCode = traderConfigParams.clrWhenReopen(closeWhenCode);
            closeReopenDaysTextField.setEnabled(false);
        }
    }//GEN-LAST:event_closeWhenReopenRadioButtonActionPerformed

    private void closeWhenOrRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWhenOrRadioButtonActionPerformed
        // TODO add your handling code here:
        if (closeWhenOrRadioButton.isSelected() == true) {
            closeWhenCode = traderConfigParams.setWhenOrSelected(closeWhenCode);
        } else {
            closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
        }
    }//GEN-LAST:event_closeWhenOrRadioButtonActionPerformed

    private void openClosePriceDaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openClosePriceDaysTextFieldActionPerformed
        // TODO add your handling code here:
        openCloseDays = Integer.parseInt(openClosePriceDaysTextField.getText());
    }//GEN-LAST:event_openClosePriceDaysTextFieldActionPerformed

    private void openTrendConfirmsDaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrendConfirmsDaysTextFieldActionPerformed
        // TODO add your handling code here:
        openTrendDays = Integer.parseInt(openTrendConfirmsDaysTextField.getText());
    }//GEN-LAST:event_openTrendConfirmsDaysTextFieldActionPerformed

    private void closeClosePriceDaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeClosePriceDaysTextFieldActionPerformed
        // TODO add your handling code here:
        closeCloseDays = Integer.parseInt(closeClosePriceDaysTextField.getText());
    }//GEN-LAST:event_closeClosePriceDaysTextFieldActionPerformed

    private void closeReopenDaysTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeReopenDaysTextFieldActionPerformed
        // TODO add your handling code here:
        closeReopenDays = Integer.parseInt(closeReopenDaysTextField.getText());
    }//GEN-LAST:event_closeReopenDaysTextFieldActionPerformed

    private void closeWhenTrendConfirmsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWhenTrendConfirmsRadioButtonActionPerformed
        // TODO add your handling code here:
         if (closeWhenTrendConfirmsRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenTrendGood(closeWhenCode);           
        }else{
            closeWhenCode = traderConfigParams.clrWhenTrendGood(closeWhenCode);
        }
    }//GEN-LAST:event_closeWhenTrendConfirmsRadioButtonActionPerformed

    private void openWhenBouncesOffMaRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openWhenBouncesOffMaRadioButtonActionPerformed
        // TODO add your handling code here:
         if (openWhenBouncesOffMaRadioButton.isSelected() == true){
            openWhenCode = traderConfigParams.setWhenMaBounce(openWhenCode);
            bouncesOffMaWithinPercentTextField.setEnabled(true);
            
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenClosePrice(openWhenCode);
            
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenPivotSignal(openWhenCode);
            
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenTrendGood(openWhenCode);
            
            openWhenOrRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode);
            
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
        }else{
            openWhenCode = traderConfigParams.clrWhenMaBounce(openWhenCode);
            bouncesOffMaWithinPercentTextField.setEnabled(false);
            openWhenClosingPriceRadioButton.setEnabled(true);;
            openWhenPivotSignalRadioButton.setEnabled(true);
            openWhenTrendConfirmsRadioButton.setEnabled(true);
            openWhenOrRadioButton.setEnabled(true);
            openClosePriceDaysTextField.setEnabled(true);
            openTrendConfirmsDaysTextField.setEnabled(true);
        }
    }//GEN-LAST:event_openWhenBouncesOffMaRadioButtonActionPerformed

    private void bouncesOffMaWithinPercentTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bouncesOffMaWithinPercentTextFieldActionPerformed
        // TODO add your handling code here:
        bouncesOffMaWithinPercent = Integer.parseInt(bouncesOffMaWithinPercentTextField.getText());       
    }//GEN-LAST:event_bouncesOffMaWithinPercentTextFieldActionPerformed

    private void openOnWeaknessRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openOnWeaknessRadioButtonActionPerformed
        // TODO add your handling code here:
        if (openOnWeaknessRadioButton.isSelected() == true){
            overShootTextField.setEnabled(true);
            openWhenCode = traderConfigParams.setWhenWeakness(openWhenCode);
            //weakness and strength can cannot be selected, one or the other..
            closeWhenCode = traderConfigParams.clrWhenWeakness(closeWhenCode);
            closeOnWeaknessRadioButton.setEnabled(false);
            closeOnWeaknessRadioButton.setSelected(false);
            //mutualy exclusive..
            openWhenCode = traderConfigParams.clrWhenStrength(openWhenCode);
            openOnStrengthRadioButton.setEnabled(false);
            openOnStrengthRadioButton.setSelected(false);
            //automatically select strength...
            closeWhenCode = traderConfigParams.setWhenStrength(closeWhenCode);
            closeOnStrengthRadioButton.setEnabled(true);
            closeOnStrengthRadioButton.setSelected(true);            
            
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenClosePrice(openWhenCode);
            
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenPivotSignal(openWhenCode);
            
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenTrendGood(openWhenCode);
            
            openWhenOrRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode);
            
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
        }else{
            openWhenCode = traderConfigParams.clrWhenWeakness(openWhenCode);
            //enable other end if this one is disabled...
            closeOnWeaknessRadioButton.setEnabled(true);
            openOnStrengthRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
            
            openWhenClosingPriceRadioButton.setEnabled(true);;
            openWhenPivotSignalRadioButton.setEnabled(true);
            openWhenTrendConfirmsRadioButton.setEnabled(true);
            openWhenOrRadioButton.setEnabled(true);
            openClosePriceDaysTextField.setEnabled(true);
            openTrendConfirmsDaysTextField.setEnabled(true);
        }
    }//GEN-LAST:event_openOnWeaknessRadioButtonActionPerformed

    private void openOnStrengthRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openOnStrengthRadioButtonActionPerformed
        // TODO add your handling code here:
        if (openOnStrengthRadioButton.isSelected() == true){
            openWhenCode = traderConfigParams.setWhenStrength(openWhenCode);
            overShootTextField.setEnabled(true);
            openWhenCode = traderConfigParams.clrWhenWeakness(openWhenCode);
            openOnWeaknessRadioButton.setSelected(false);
             //strength and weakness can cannot be selected, one or the other..
            closeWhenCode = traderConfigParams.clrWhenStrength(closeWhenCode);
            closeOnStrengthRadioButton.setEnabled(false);
            closeOnStrengthRadioButton.setSelected(false);
            
            //mutualy exclusive..
            openWhenCode = traderConfigParams.clrWhenWeakness(openWhenCode);
            openOnWeaknessRadioButton.setSelected(false);
            openOnWeaknessRadioButton.setEnabled(false);
            //automatically select weakness...
            closeWhenCode = traderConfigParams.setWhenWeakness(closeWhenCode);
            closeOnWeaknessRadioButton.setEnabled(true);
            closeOnWeaknessRadioButton.setSelected(true);            
            
            openWhenClosingPriceRadioButton.setEnabled(false);
            openWhenClosingPriceRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenClosePrice(openWhenCode);
            
            openWhenPivotSignalRadioButton.setEnabled(false);
            openWhenPivotSignalRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenPivotSignal(openWhenCode);
            
            openWhenTrendConfirmsRadioButton.setEnabled(false);
            openWhenTrendConfirmsRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenTrendGood(openWhenCode);
            
            openWhenOrRadioButton.setEnabled(false);
            openWhenOrRadioButton.setSelected(false);
            openWhenCode = traderConfigParams.clrWhenOrSelected(openWhenCode);
            
            openClosePriceDaysTextField.setEnabled(false);
            openTrendConfirmsDaysTextField.setEnabled(false);
        }else{
            openWhenCode = traderConfigParams.clrWhenStrength(openWhenCode);
            openOnWeaknessRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
            //enable other end if this one is disabled...
            closeOnStrengthRadioButton.setEnabled(true);
            openWhenClosingPriceRadioButton.setEnabled(true);;
            openWhenPivotSignalRadioButton.setEnabled(true);
            openWhenTrendConfirmsRadioButton.setEnabled(true);
            openWhenOrRadioButton.setEnabled(true);
            openClosePriceDaysTextField.setEnabled(true);
            openTrendConfirmsDaysTextField.setEnabled(true);
        }
    }//GEN-LAST:event_openOnStrengthRadioButtonActionPerformed

    private void closeOnStrengthRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeOnStrengthRadioButtonActionPerformed
        // TODO add your handling code here:
        if (closeOnStrengthRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenStrength(closeWhenCode);                       
            closeOnWeaknessRadioButton.setSelected(false);
            overShootTextField.setEnabled(true);
             //strength and weakness can cannot be selected, one or the other..
            openWhenCode = traderConfigParams.clrWhenStrength(openWhenCode);
            openOnStrengthRadioButton.setEnabled(false);
            openOnStrengthRadioButton.setSelected(false);
            //mutualy exclusive..
            closeWhenCode = traderConfigParams.clrWhenWeakness(closeWhenCode);
            closeOnWeaknessRadioButton.setSelected(false);
            closeOnWeaknessRadioButton.setEnabled(false);
            //automatically select weakness...
            openWhenCode = traderConfigParams.setWhenWeakness(openWhenCode);
            openOnWeaknessRadioButton.setEnabled(true);
            openOnWeaknessRadioButton.setSelected(true);            
                        
            
            closeWhenClosingPriceRadioButton.setEnabled(false);
            closeWhenClosingPriceRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenClosePrice(closeWhenCode);
            
            closeWhenPivotSignalRadioButton.setEnabled(false);
            closeWhenPivotSignalRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenPivotSignal(closeWhenCode);
            
            closeWhenTrendConfirmsRadioButton.setEnabled(false);
            closeWhenTrendConfirmsRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenTrendGood(closeWhenCode);
            
            closeWhenOrRadioButton.setEnabled(false);
            closeWhenOrRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
            
            closeClosePriceDaysTextField.setEnabled(false);            
        }else{
            closeWhenCode = traderConfigParams.clrWhenWeakness(closeWhenCode);
            closeOnWeaknessRadioButton.setEnabled(true);
            overShootTextField.setEnabled(false);
            //this side disabled..enable other side...
            openOnWeaknessRadioButton.setEnabled(true);
            closeWhenClosingPriceRadioButton.setEnabled(true);;
            closeWhenPivotSignalRadioButton.setEnabled(true);
            closeWhenTrendConfirmsRadioButton.setEnabled(true);
            closeWhenOrRadioButton.setEnabled(true);
            closeClosePriceDaysTextField.setEnabled(true);
        }
    }//GEN-LAST:event_closeOnStrengthRadioButtonActionPerformed

    private void closeOnWeaknessRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeOnWeaknessRadioButtonActionPerformed
        // TODO add your handling code here:
        if (closeOnWeaknessRadioButton.isSelected() == true){
            closeWhenCode = traderConfigParams.setWhenWeakness(closeWhenCode);                       
            closeOnStrengthRadioButton.setSelected(false);
            overShootTextField.setEnabled(true);
             //strength and weakness can cannot be selected, one or the other..
            openWhenCode = traderConfigParams.clrWhenWeakness(openWhenCode);
            openOnWeaknessRadioButton.setEnabled(false);
            openOnWeaknessRadioButton.setSelected(false);
            //mutualy exclusive..
            closeWhenCode = traderConfigParams.clrWhenStrength(closeWhenCode);
            closeOnStrengthRadioButton.setSelected(false);
            closeOnStrengthRadioButton.setEnabled(false);
            //automatically select strength...
            openWhenCode = traderConfigParams.setWhenStrength(openWhenCode);
            openOnStrengthRadioButton.setEnabled(true);
            openOnStrengthRadioButton.setSelected(true);            
              
            
            closeWhenClosingPriceRadioButton.setEnabled(false);
            closeWhenClosingPriceRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenClosePrice(closeWhenCode);
            
            closeWhenPivotSignalRadioButton.setEnabled(false);
            closeWhenPivotSignalRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenPivotSignal(closeWhenCode);
            
            closeWhenTrendConfirmsRadioButton.setEnabled(false);
            closeWhenTrendConfirmsRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenTrendGood(closeWhenCode);
            
            closeWhenOrRadioButton.setEnabled(false);
            closeWhenOrRadioButton.setSelected(false);
            closeWhenCode = traderConfigParams.clrWhenOrSelected(closeWhenCode);
            
            closeClosePriceDaysTextField.setEnabled(false);            
        }else{
            closeWhenCode = traderConfigParams.clrWhenWeakness(closeWhenCode);
            overShootTextField.setEnabled(false);
            closeOnStrengthRadioButton.setEnabled(true);
            openOnWeaknessRadioButton.setEnabled(true);
            closeWhenClosingPriceRadioButton.setEnabled(true);;
            closeWhenPivotSignalRadioButton.setEnabled(true);
            closeWhenTrendConfirmsRadioButton.setEnabled(true);
            closeWhenOrRadioButton.setEnabled(true);
            closeClosePriceDaysTextField.setEnabled(true);
        }
    }//GEN-LAST:event_closeOnWeaknessRadioButtonActionPerformed

    private void overShootTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overShootTextFieldActionPerformed
        // TODO add your handling code here:
        overShootPercent = Integer.parseInt(overShootTextField.getText());
    }//GEN-LAST:event_overShootTextFieldActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ConfigDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConfigDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConfigDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConfigDialogForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConfigDialogForm dialog = new ConfigDialogForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel biasLable;
    private javax.swing.JTextField bouncesOffMaWithinPercentTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField closeClosePriceDaysTextField;
    private javax.swing.JRadioButton closeOnStrengthRadioButton;
    private javax.swing.JRadioButton closeOnWeaknessRadioButton;
    private javax.swing.JTextField closeReopenDaysTextField;
    private javax.swing.JRadioButton closeWhenClosingPriceRadioButton;
    private javax.swing.JRadioButton closeWhenOrRadioButton;
    private javax.swing.JRadioButton closeWhenPivotSignalRadioButton;
    private javax.swing.JRadioButton closeWhenReopenRadioButton;
    private javax.swing.JRadioButton closeWhenTrendConfirmsRadioButton;
    private javax.swing.JButton displayButton;
    private javax.swing.JButton doneButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField openClosePriceDaysTextField;
    private javax.swing.JRadioButton openOnStrengthRadioButton;
    private javax.swing.JRadioButton openOnWeaknessRadioButton;
    private javax.swing.JTextField openTrendConfirmsDaysTextField;
    private javax.swing.JRadioButton openWhenBouncesOffMaRadioButton;
    private javax.swing.JRadioButton openWhenClosingPriceRadioButton;
    private javax.swing.JRadioButton openWhenOrRadioButton;
    private javax.swing.JRadioButton openWhenPivotSignalRadioButton;
    private javax.swing.JRadioButton openWhenTrendConfirmsRadioButton;
    private javax.swing.JTextField overShootTextField;
    // End of variables declaration//GEN-END:variables
}
