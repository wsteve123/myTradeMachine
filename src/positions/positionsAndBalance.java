/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import ibTradeApi.ibApi;
import ibTradeApi.ibApi.balancePositionInfo;

/**
 *
 * @author walterstevenson
 */
public class positionsAndBalance {
    balancePositionInfo balPosInfo = null;
    public positionsAndBalance(String accIn) {
        balPosInfo = ibApi.getBalanceAndPositions(accIn /* account # - default main account if "" */);
        displayBalanceAndPositions();

    }
    
    
    
    private void displayBalanceAndPositions() {
        new dispBalAndPos("Td Balance And Positions");
    }

    private class dispBalAndPos extends JFrame {

        String[] columns = {
            "","Initial", "Current", "Todays change "
        };
        String[][] dataArr = new String[20][columns.length];
        JTable jt = null;
        JScrollPane pane = null;
        /* columns */
        final int LABLE = 0;
        final int INITIAL = 1;
        final int CURRENT = 2;
        final int CHANGE = 3;
        /* rows */
        final int CASHBAL =0;
        final int MARGINBAL = 1;
        final int LONGSTOCKBAL = 2;
        final int SHORTSTOCKBAL = 3;
        final int LONGOPTIONBAL = 4;
        final int SHORTOPTIONBAL = 5;
        final int ACCOUNTVALUE = 6;
        final int EMPTY1 = 7;
        final int ACCOUNT = 8;
        final int AVAILFUNDS = 9;
        final int OPTION_BP = 10;
        final int STOCK_BP = 11;
        

        private dispBalAndPos(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    // dispose();
                    // System.exit( 0 );
                }
            });

            initTable();

            pack();
            setVisible(true);

        }

        private void initTable() {

            dataArr[CASHBAL][LABLE] = "Cash Balance";
            dataArr[CASHBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.cashBalanceInitial);
            dataArr[CASHBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.cashBalanceCurrent);
            dataArr[CASHBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.cashBalanceChange);
            
            dataArr[MARGINBAL][LABLE] = "Margin Balance";
            dataArr[MARGINBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.marginBalanceInitial);
            dataArr[MARGINBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.marginBalanceCurrent);
            dataArr[MARGINBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.marginBalanceChange);
            
            dataArr[LONGSTOCKBAL][LABLE] = "Long Stock Value";
            dataArr[LONGSTOCKBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.longStockValueInitial);
            dataArr[LONGSTOCKBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.longStockValueCurrent);
            dataArr[LONGSTOCKBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.longStockValueChange);
            
            dataArr[SHORTSTOCKBAL][LABLE] = "Short Stock Value";
            dataArr[SHORTSTOCKBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.shortStockValueInitial);
            dataArr[SHORTSTOCKBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.shortStockValueCurrent);
            dataArr[SHORTSTOCKBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.shortStockValueChange);
            
            dataArr[LONGOPTIONBAL][LABLE] = "Long Option Value";
            dataArr[LONGOPTIONBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.longOptionValueInitial);
            dataArr[LONGOPTIONBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.longOptionValueCurrent);
            dataArr[LONGOPTIONBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.longOptionValueChange);
            
            dataArr[SHORTOPTIONBAL][LABLE] = "Short Option Value";
            dataArr[SHORTOPTIONBAL][INITIAL] = Float.toString(balPosInfo.balanceInformation.shortOptionValueInitial);
            dataArr[SHORTOPTIONBAL][CURRENT] = Float.toString(balPosInfo.balanceInformation.shortOptionValueCurrent);
            dataArr[SHORTOPTIONBAL][CHANGE] = Float.toString(balPosInfo.balanceInformation.shortOptionValueChange);
            
            dataArr[ACCOUNTVALUE][LABLE] = "Account Value";
            dataArr[ACCOUNTVALUE][INITIAL] = Float.toString(balPosInfo.balanceInformation.accountValueInitial);
            dataArr[ACCOUNTVALUE][CURRENT] = Float.toString(balPosInfo.balanceInformation.accountValueCurrent);
            dataArr[ACCOUNTVALUE][CHANGE] = Float.toString(balPosInfo.balanceInformation.accountValueChange);

            dataArr[ACCOUNT][LABLE] = "Account #";
            dataArr[ACCOUNT][LABLE+1] = Long.toString(balPosInfo.acountId);
            
            dataArr[AVAILFUNDS][LABLE] = "Available Funds ";
            dataArr[AVAILFUNDS][LABLE+1] = Float.toString(balPosInfo.balanceInformation.availFundsForTrading);
            
            dataArr[STOCK_BP][LABLE] = "Stock Buying Power ";
            dataArr[STOCK_BP][LABLE+1] = Float.toString(balPosInfo.balanceInformation.stockBuyingPower);
            dataArr[OPTION_BP][LABLE] = "Option Buying Power ";
            dataArr[OPTION_BP][LABLE+1] = Float.toString(balPosInfo.balanceInformation.optionBuyingPower);


            if (jt == null) {
                jt = new JTable(dataArr, columns);
            }
            if (pane == null) {
                pane = new JScrollPane(jt);
            }

            getContentPane().add(pane);
        }
    }    
    
    
    
}
