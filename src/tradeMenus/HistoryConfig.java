/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author earlie87
 */
public class HistoryConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userSelHistSize;
	private String userSelBackTestSize;
	private String userSelBarSize;
	private String userSelDate;
	private boolean userSelTodaysDate;
	private String userSelYear;
	private String userSelMonth;
	private String userSelDay;
	//following set true when bar size is less than 1day (ie 15min, 1min etc)
	private boolean userSelIntradayTrading;

	public HistoryConfig(String histSize, String btSize, String barSz, String date, boolean todayDate) {
		userSelHistSize = histSize;
		userSelBackTestSize = btSize;
		userSelBarSize = barSz;
		userSelDate = date;
		userSelTodaysDate = todayDate;
	}

	public String getUserSelHistSize() {
		return userSelHistSize;
	}

	public void setUserSelHistSize(String userSelHistSize) {
		this.userSelHistSize = userSelHistSize;
	}

	public String getUserSelBackTestSize() {
		return userSelBackTestSize;
	}

	public void setUserSelBackTestSize(String userSelBackTestSize) {
		this.userSelBackTestSize = userSelBackTestSize;
	}

	public String getUserSelBarSize() {
		return userSelBarSize;
	}

	public void setUserSelBarSize(String userSelBarSize) {
		this.userSelBarSize = userSelBarSize;
	}
	public void setUserSelIntrDayTrade(boolean userSelIntraday) {
		this.userSelIntradayTrading = userSelIntraday;
	}
	public boolean getUserSelIntraDayTrade(){
		return this.userSelIntradayTrading;
	}
	public String getUserSelDate() {
		return userSelDate;
	}

	public void setUserSelDate(String userSelDate) {
		//yyyymmdd format coming in..
		this.userSelDate = userSelDate;
		this.userSelYear = userSelDate.substring(0, 4);
		this.userSelMonth = userSelDate.substring(4, 6);
		this.userSelDay = userSelDate.substring(6, 8);
	}
	public void setUserSelTodayDate(boolean userSelToday){
		userSelTodaysDate = userSelToday;
	}
	public boolean getUserSelTodayDate (){
		return userSelTodaysDate;
	}
	public void setUserSelMonth(String min){
		//yyyyMMdd is the format only change the MM here..
		String y = userSelDate.substring(0, 4);
		String d = userSelDate.substring(6, 8);
		userSelDate = (y + min + d);
		userSelMonth = min;
	}
	public void setUserSelDay(String din){
		//yyyyMMdd is the format only change the dd here..
		String y = userSelDate.substring(0, 4);
		String m = userSelDate.substring(4, 6);
		userSelDate = (y + m + din);
		userSelDay = din;
	}
	public void setUserSelYear(String yin){
		//yyyyMMdd is the format only change the dd here..
		String m = userSelDate.substring(4, 6);
		String d = userSelDate.substring(6, 8);
		userSelDate = (yin + m + d);
		userSelYear = yin;
	}
	public String getUserSelYear(){
		return userSelYear;
	}
	public String getUserSelMonth(){
		return userSelMonth;
	}
	public String getUserSelDay(){
		return userSelDay;
	}
	@Override
	public String toString() {
		return ("HistSz: " + userSelHistSize + " BackTestSz: " + userSelBackTestSize + "\n"
			+ "HistBarSz: " + userSelBarSize + " HistDate: " + userSelDate);
	}

	public void serialize(HistoryConfig config, String fname) {
		// write object to file
		try {
			FileOutputStream fos = new FileOutputStream(fname);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(config);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HistoryConfig deSerialize(String fname) {
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
}
