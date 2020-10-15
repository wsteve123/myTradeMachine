/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import positions.commonGui;
import positions.myUtils;

/**
 *
 * @author earlie87
 */
public class HistoryConfigDialog extends javax.swing.JDialog {
    private HistoricalDate historicalDate;
    private int MaxYearsBack = 25;
    HistoryConfig historyConfig = new HistoryConfig("2 Y", "1 Y", "1 day", "", false);
    boolean configSaved = false;
    final String HC_FILE_NAME = "historyConfig.ser";
    String historyConfigFileName = "";
	boolean backTestDateToday = false;
    public void serialize(HistoryConfig config, String fname){
        // write object to file
        try {
            FileOutputStream fos = new FileOutputStream(fname);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(config);
            oos.close();
            configSaved = true;
        } catch (FileNotFoundException e) {
		e.printStackTrace();
        } catch (IOException e) {
		e.printStackTrace();
	} 
    }
    public HistoryConfig deSerialize(String fname){
        // read object from file
        HistoryConfig config = new HistoryConfig(null, null, null, null, false);
        try {
            FileInputStream fis = new FileInputStream(fname);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            config = (HistoryConfig) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
		e.printStackTrace();
        } catch (IOException e) {
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	}
        return config;
    }
    public HistoryConfig getHistoryConfig(){
        return historyConfig;
    }
    /**
     * Creates new form HistoryConfig
     */
    public HistoryConfigDialog(java.awt.Frame parent, boolean modal, String hisSzIn, String hisBackTestIn, String hisBarIn, String dateIn) {
        
        super(parent, modal);
        initComponents();
        String tmpYear = "";
        int x = 0;
        Calendar cal = Calendar.getInstance();
        String dir = "";
        File f = null; 
		backTestDateToday = backTestTodayCheckBox.isSelected();
        historyConfigFileName = myUtils.getMyWorkingDirectory() + "/src/supportFiles/" + HC_FILE_NAME;
        f = new File(historyConfigFileName);
        if(f.exists()){
            historyConfig = historyConfig.deSerialize(historyConfigFileName);
        }else{
            historyConfig.setUserSelHistSize(hisSzIn);
            historyConfig.setUserSelBackTestSize(hisBackTestIn);
            historyConfig.setUserSelBarSize(hisBarIn);
            historyConfig.setUserSelDate(dateIn);
			historyConfig.setUserSelTodayDate(true);
        }        
        
        histSizeComboBox.setSelectedItem(historyConfig.getUserSelHistSize());        
        backTestSizeComboBox.setSelectedItem(historyConfig.getUserSelBackTestSize());        
        barSizeComboBox.setSelectedItem(historyConfig.getUserSelBarSize());  
		backTestTodayCheckBox.setSelected(historyConfig.getUserSelTodayDate());
        historicalDate = new HistoricalDate(historyConfig.getUserSelDate());
        backTestYearComboBox.removeAllItems();
        backTestYearComboBox.addItem(cal.get(Calendar.YEAR));        
        for(int y = 0; y <= MaxYearsBack; y++){
            cal.add(Calendar.YEAR, -1);
            System.out.println("\ndate: " + cal.get(Calendar.YEAR));  
            backTestYearComboBox.addItem(cal.get(Calendar.YEAR));
        }
        backTestYearComboBox.setSelectedItem(Integer.valueOf(historicalDate.getYear()));
		backTestMonthComboBox.setSelectedIndex(Integer.valueOf(historicalDate.getMonth()) - 1);
		backTestDayComboBox.setSelectedIndex(Integer.valueOf(historicalDate.getDay()) - 1);
        tmpYear = backTestYearComboBox.getSelectedItem().toString();        
    }
    private class HistoricalDate{
        private String year;
        private String month;
        private String day;
        private String ymd;
        private Calendar cal = Calendar.getInstance();
        private HistoricalDate(String datein){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            if (datein.length() > 0) {
                try {
                    cal.setTime(sdf.parse(datein));
                } catch (ParseException ex) {
                    Logger.getLogger(HistoryConfigDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
                year = String.valueOf(cal.get(Calendar.YEAR));
                month = String.valueOf(cal.get(Calendar.MONTH) + 1);
                day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                ymd = datein;
            }            
        }
        public void setYear(String yin){
            year = yin;
        }
        public void setMonth(String min){
            month = min;
        }
        public void setDay(String din){
            day = din;
        }
        public void setYmd(String ymdin){
            ymd = ymdin;
        }
        public String getYear(){
            return year;
        }
        public String getMonth(){
            return month;
        }
        public String getDay(){
            return day;
        }
        public String getYmd(){
            return ymd;
        }
    }
    public String getHistSize(){
        return historyConfig.getUserSelHistSize();
    }
    public String getBackTestSize(){
        return historyConfig.getUserSelBackTestSize();
    }
    public String getHistBarSize(){
        return historyConfig.getUserSelBarSize();
    }
    public String getBackTestDate(){
        return historyConfig.getUserSelDate();
    }
    public HistoryConfig deSerialize(){
        HistoryConfig hc = new HistoryConfig(null, null, null, null, false);
        hc = deSerialize(myUtils.getMyWorkingDirectory() + "/src/supportFiles/" + HC_FILE_NAME);
        return hc;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        barSizeLabel = new javax.swing.JLabel();
        histSizeComboBox = new javax.swing.JComboBox();
        barSizeComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        backTestYearComboBox = new javax.swing.JComboBox();
        backTestSizeComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        backTestMonthComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        backTestDayComboBox = new javax.swing.JComboBox<>();
        doneButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        backTestTodayCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Historical Data Config\n"));

        jLabel1.setText("HistSize:");

        jLabel2.setText("BackTestSize:");

        barSizeLabel.setText("BarSize:");

        histSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 D", "2 D", "3 D", "5 D", "1 W", "2 W", "3 W", "5 W", "8 W", "10 W", "12 W", "1 M", "2 M", "3 M", "4 M", "5 M", "6 M", "7 M", "8 M", "9 M", "10 M", "1 Y", "2 Y", "3 Y", "4 Y", "5 Y", "6 Y", "7 Y", "8 Y", "9 Y", "10 Y" }));
        histSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histSizeComboBoxActionPerformed(evt);
            }
        });

        barSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 secs", "5 secs", "10 secs", "15 secs", "30 secs", "1 min", "2 mins", "3 mins", "5 mins", "10 mins", "15 mins", "20 mins", "30 mins", "1 hour", "2 hours", "3 hours", "4 hours", "8 hours", "1 day", "1 week", "1 month" }));
        barSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barSizeComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("BackTestDate:");

        backTestYearComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        backTestYearComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestYearComboBoxActionPerformed(evt);
            }
        });

        backTestSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 D", "2 D", "3 D", "5 D", "1 W", "2 W", "3 W", "5 W", "8 W", "10 W", "12 W", "1 M", "2 M", "3 M", "4 M", "5 M", "6 M", "7 M", "8 M", "9 M", "10 M", "1 Y", "2 Y", "3 Y", "4 Y", "5 Y", "6 Y", "7 Y", "8 Y", "9 Y", "10 Y" }));
        backTestSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestSizeComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("+");

        jLabel5.setText("Month:");

        backTestMonthComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Jan", "feb", "march", "april", "may", "june", "july", "aug", "sept", "oct", "nov", "dec" }));
        backTestMonthComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestMonthComboBoxActionPerformed(evt);
            }
        });

        jLabel6.setText("Year:");

        jLabel7.setText("Day:");

        backTestDayComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", " " }));
        backTestDayComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestDayComboBoxActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        jLabel8.setText("Today:");

        backTestTodayCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backTestTodayCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(backTestYearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(barSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(40, 40, 40)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(backTestDayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(backTestMonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(doneButton)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(backTestSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(barSizeLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(backTestTodayCheckBox))
                            .addComponent(histSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(histSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backTestSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(barSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(backTestTodayCheckBox))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(backTestYearComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(backTestMonthComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(backTestDayComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(doneButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void histSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histSizeComboBoxActionPerformed
        // TODO add your handling code here:
        historyConfig.setUserSelHistSize(histSizeComboBox.getSelectedItem().toString());
        System.out.println("\nuserSelHistSize:" + historyConfig.getUserSelHistSize());      
    }//GEN-LAST:event_histSizeComboBoxActionPerformed

    private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        // TODO add your handling code here:
        historyConfig.serialize(historyConfig, historyConfigFileName);
        setVisible(false);
        dispose();
    }//GEN-LAST:event_doneButtonActionPerformed

    private void barSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barSizeComboBoxActionPerformed
        // TODO add your handling code here:
		int selectedBarSzIndx;
		int barSz1MinIndx;
		String selectedBarSzStr = "";
		//get index and str of selected bar size
        historyConfig.setUserSelBarSize((selectedBarSzStr = barSizeComboBox.getSelectedItem().toString()));
		selectedBarSzIndx = barSizeComboBox.getSelectedIndex();
		//set to 1day ro find out the index of 1day
		barSizeComboBox.setSelectedItem("1 day");
		barSz1MinIndx = barSizeComboBox.getSelectedIndex();
		//set it back to what it was..
		barSizeComboBox.setSelectedItem(selectedBarSzStr);
        System.out.println("\nuserSelBarSize:" + historyConfig.getUserSelBarSize());
		if(selectedBarSzIndx < barSz1MinIndx){
			//all this to determine if we are trading intraday or not..
			//yes we are the sel barsz is less than 1day
			historyConfig.setUserSelIntrDayTrade(true);
		}else{
			//no intraday...user selected 1 day or more
			historyConfig.setUserSelIntrDayTrade(false);
		}
    }//GEN-LAST:event_barSizeComboBoxActionPerformed

    private void backTestYearComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestYearComboBoxActionPerformed
        // TODO add your handling code here:
        DateFormat df = new SimpleDateFormat("yyyMMdd");
        String year; 
		String md = "";
		String y = "";
		String ymd = "";
        if(backTestYearComboBox.getSelectedItem() != null){
			year = backTestYearComboBox.getSelectedItem().toString();
			if((backTestDateToday = backTestTodayCheckBox.isSelected()) == true){
				//backTestDateToday means set date (ymd) to today
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, Integer.valueOf(year));
				historyConfig.setUserSelDate(df.format(cal.getTime()));
			}else{
				//otherwise just modify year here..leave month/day same..read/modify/write
				historyConfig.setUserSelYear(year);
			}			
        }
        
    }//GEN-LAST:event_backTestYearComboBoxActionPerformed

    private void backTestSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestSizeComboBoxActionPerformed
        // TODO add your handling code here:        
       
       String strin;
        if(backTestSizeComboBox.getSelectedItem().toString().equals("EnterSz")){
            strin = commonGui.getUserInput("EnterSz:",  "example: 4 Y or 3 W or 7 M)");
        }else{
            strin = backTestSizeComboBox.getSelectedItem().toString();
        }
        historyConfig.setUserSelBackTestSize(strin);
    }//GEN-LAST:event_backTestSizeComboBoxActionPerformed

    private void backTestMonthComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestMonthComboBoxActionPerformed
        // TODO add your handling code here:
		 //DateFormat df = new SimpleDateFormat("yyyMMdd");
		DateFormat df = new SimpleDateFormat("MM");
		int daysInMonth = 0;
		int month;
		if(backTestMonthComboBox.getSelectedItem() != null){
            month = (backTestMonthComboBox.getSelectedIndex());
            Calendar cal = Calendar.getInstance();
			System.out.println("\ngetTimeBefore: " + cal.getTime());
			//need to check if days in month are beyond max (feb has 28), if so we mess up date. so check and set to max..
			//the following sets to new wanted month..
            cal.set(Integer.parseInt(historyConfig.getUserSelYear()), month, 1);
			System.out.println("\ngetTimeAfter: " + cal.getTime());
			if((daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)) < Integer.parseInt(historyConfig.getUserSelDay())){
				//went over max days so set to max allowed for this new month
				cal.set(Calendar.DAY_OF_MONTH, daysInMonth);
				//also correct the user selection..
				historyConfig.setUserSelDay(Integer.toString(daysInMonth - 1));
				backTestDayComboBox.setSelectedIndex(daysInMonth - 1);
			}else{
				//we are ok with days, so set to user selected day of month
				cal.set(Calendar.DAY_OF_MONTH, (Integer.parseInt(historyConfig.getUserSelDay())));
			}
            //historyConfig.setUserSelDate(df.format(cal.getTime()));
			System.out.println("\nmonth is: " + df.format(cal.getTime()));
			System.out.println("\ngetTimeAfter: " + cal.getTime());
			historyConfig.setUserSelMonth(df.format(cal.getTime()));
        }
    }//GEN-LAST:event_backTestMonthComboBoxActionPerformed

    private void backTestTodayCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestTodayCheckBoxActionPerformed
        // TODO add your handling code here:
		DateFormat df = new SimpleDateFormat("yyyMMdd");
		backTestDateToday = backTestTodayCheckBox.isSelected();
		System.out.println("\nbackTestDateToday is: " + backTestDateToday);
		historyConfig.setUserSelTodayDate(backTestDateToday);
		if(backTestDateToday == true){
			Calendar cal = Calendar.getInstance();
			System.out.println("\n" + df.format(cal.getTime()));
			historyConfig.setUserSelDate(df.format(cal.getTime()));
			System.out.println("\nuserSelDate: " + historyConfig.getUserSelDate());
			backTestYearComboBox.setSelectedItem(Integer.valueOf(historyConfig.getUserSelYear()));
			backTestMonthComboBox.setSelectedIndex(Integer.valueOf(historyConfig.getUserSelMonth()) - 1);
			backTestDayComboBox.setSelectedIndex(Integer.valueOf(historyConfig.getUserSelDay()) - 1);
			backTestYearComboBox.setEnabled(false);
			backTestMonthComboBox.setEnabled(false);
			backTestDayComboBox.setEnabled(false);
		}else{
			backTestYearComboBox.setEnabled(true);
			backTestMonthComboBox.setEnabled(true);
			backTestDayComboBox.setEnabled(true);
		}
		
    }//GEN-LAST:event_backTestTodayCheckBoxActionPerformed

    private void backTestDayComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backTestDayComboBoxActionPerformed
        // TODO add your handling code here:
				 //DateFormat df = new SimpleDateFormat("yyyMMdd");
		DateFormat df = new SimpleDateFormat("dd");
		int day;
		int daysInMonth = 0;
		if(backTestMonthComboBox.getSelectedItem() != null){
            day = (backTestDayComboBox.getSelectedIndex() + 1);
            Calendar cal = Calendar.getInstance();
			//need to check if days in month are beyond max (feb has 28), if so we mess up date. so check and set to max..
            cal.set(Integer.parseInt(historyConfig.getUserSelYear()), (Integer.parseInt(historyConfig.getUserSelMonth()) - 1), 1);
			if((daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)) < day){
				//went over max so set to max allowed
				cal.set(Calendar.DAY_OF_MONTH, daysInMonth);
				//also correct the user selection..
				backTestDayComboBox.setSelectedIndex(daysInMonth - 1);
				historyConfig.setUserSelDay(Integer.toString(daysInMonth - 1));
			}else{
				//we are ok so set to user selected day
				cal.set(Calendar.DAY_OF_MONTH, day);
			}
            //cal.set(Calendar.DAY_OF_MONTH, day);
            //historyConfig.setUserSelDate(df.format(cal.getTime()));
			System.out.println("\nday is: " + df.format(cal.getTime()));
			historyConfig.setUserSelDay(df.format(cal.getTime()));
			System.out.println("\ntime: " + cal.getTime());
        }
    }//GEN-LAST:event_backTestDayComboBoxActionPerformed

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
            java.util.logging.Logger.getLogger(HistoryConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HistoryConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HistoryConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HistoryConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                HistoryConfigDialog dialog = new HistoryConfigDialog(new javax.swing.JFrame(), true, "", "", "", "");
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
    private javax.swing.JComboBox<String> backTestDayComboBox;
    private javax.swing.JComboBox<String> backTestMonthComboBox;
    private javax.swing.JComboBox backTestSizeComboBox;
    private javax.swing.JCheckBox backTestTodayCheckBox;
    private javax.swing.JComboBox backTestYearComboBox;
    private javax.swing.JComboBox barSizeComboBox;
    private javax.swing.JLabel barSizeLabel;
    private javax.swing.JButton doneButton;
    private javax.swing.JComboBox histSizeComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
