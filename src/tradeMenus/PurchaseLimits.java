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
import java.text.DecimalFormat;
/**
 *
 * @author earlie87
 */
public class PurchaseLimits implements Serializable {
	private double accountTotalLimit = 0.0;
	private double singlePositionDollarAmountLimit = 0.0;
	private int singlePositionFixedNumOfSharesLimit = 0;
	private boolean divideEquallyAmongAll = false;
	DecimalFormat meFormatter = new DecimalFormat("$###,###,###");
	public PurchaseLimits(double accountLimit, double stockLimit, double stockLimitByStock, boolean divideAmongAll) {
		accountTotalLimit = accountLimit;
		singlePositionDollarAmountLimit = stockLimit;
		divideEquallyAmongAll = divideAmongAll;
	}
	public void setAccountLimit(double limIn){
		accountTotalLimit = limIn;
	}
	public double getAccountLimit(){
		return accountTotalLimit;
	}
	public void setPositionDollarAmountLimit(double limIn){
		singlePositionDollarAmountLimit = limIn;
	}
	public void setPositionFixedNumOfSharesLimit(int limIn){
		singlePositionFixedNumOfSharesLimit = limIn;
	}
	public int getPositionFixedNumOfSharesLimit(){
		return singlePositionFixedNumOfSharesLimit;
	}
	public double getPositionDollarAmountLimit(){
		return singlePositionDollarAmountLimit;
	}
	public void setDivideEqallyAmongAll(boolean divIn){
		divideEquallyAmongAll = divIn;
	}
	public boolean getDivideEquallAmongAll(){
		return divideEquallyAmongAll;
	}
	@Override
	public String toString() {
		return ("accountTotalLimit: " + accountTotalLimit + " singlePosLimit: " + 
			    singlePositionDollarAmountLimit + " singlePosLimitByStock: " + singlePositionFixedNumOfSharesLimit + "\n" + 
				"divideEquallyAmongAll: " + divideEquallyAmongAll
			);
	}
	
	public void serialize(PurchaseLimits limits, String fname) {
		// write object to file
		try {
			FileOutputStream fos = new FileOutputStream(fname);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(limits);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public PurchaseLimits deSerialize(String fname) {
		// read object from file
		PurchaseLimits limits = new PurchaseLimits(0.0, 0.0, 0.0, false);
		try {
			FileInputStream fis = new FileInputStream(fname);
			ObjectInputStream ois = new ObjectInputStream(fis);

			limits = (PurchaseLimits) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return limits;
	}
	public String displayIt(double val){
		return(meFormatter.format(val));
	}
}
