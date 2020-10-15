/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytrademachine;
import ibTradeApi.ibApi;
import java.nio.file.Path;
import java.nio.file.Paths;
import positions.myUtils;
import tradeMenus.playMenuForm;


/**
 *
 * @author earlie87
 */
public class MyTradeMachine {

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
        /*6/5/2016
        have to use path and set working directory to allow execution from both netBeans and term when 
          working directories are different. Need to force the working directory so that it works
          ie when term execution working directory becomes .../dist and not when using netbeans.
        */
        Path path = Paths.get(myUtils.getMyWorkingDirectory(), "", "");
        path = path.subpath(0, 4);
        System.out.println("\nmain: #elements in path:" + path.getNameCount());
        final ibApi myApiTest = new ibApi((args.length > 0) ? Integer.parseInt(args[0]) : 0);
        myApiTest.setActApi(myApiTest);
        myUtils.setMyWorkingDirectory("/" + path.toString());
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
