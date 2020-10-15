/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tradeMenus;

import ibTradeApi.ibApi;
import ibTradeApi.ibApi.*;
import ibTradeApi.*;


/**
 *
 * @author earlie87
 */
public class PlayWIthMenus {

    /**
     * @param args the command line arguments ≈ public static void main(String[]
     * 
     */
    public static playMenuForm actMainMenu;
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(playMenuForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(playMenuForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(playMenuForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(playMenuForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        final ibApi myApiTest = new ibApi((args.length > 0) ? Integer.parseInt(args[0]) : 0);
        myApiTest.setActApi(myApiTest);
        if (myApiTest.ibConnectError == true) {
            System.out.println("main: iBConnectError is TRUE.. no point in starting up..");
        }else {
            //swMainMenu mainMenu = new swMainMenu("client: " + Integer.toString(myApiTest.getCliendId()));
        }
        
        /*
         * Create and display the form
         */
 //       new playMenuForm("client: " + Integer.toString(myApiTest.getCliendId())).setVisible(true);
     
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
               actMainMenu =  new playMenuForm("client: " + Integer.toString(myApiTest.getCliendId()));
               actMainMenu.setVisible(true);
            }
        });
       
    }
    
}
