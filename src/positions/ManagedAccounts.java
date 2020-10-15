/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

import java.awt.Choice;
import java.awt.Label;
import java.awt.event.*;
import java.text.ParseException;
import javax.swing.*;

/**
 *
 * @author earlie87
 */
public class ManagedAccounts {
    private String[] managedAccountsTbl;
    
    private String[] accountsOkForTradingTbl = {/*"DF123602",*/"DU123603", "DU123604"};
    private final int SIMULATED = 0;
    private final int REALMONEY = 1;
    private String[][] accountsOkForTradingMatrix = {
            /* first, simulated trading accounts */
            {/*"DF123602",*/"DU123603", "DU123604"},
            /* second, real money accounts */
            {"U1001569",
             "U1007896",
           /*"U1008848",*/
           /*"U1016356",*/
           /*"U1017777",*/
           /*"U1018828",*/
           /*"U1029422",*/
             "U1036841",
             "U1037734",
           /*"U657285" ,*/
           /*"U659634"*/}
    };
    /*
      private String[] accountsOkForRealMoneyTradingTbl = { 
            "U1001569" ,
            "U1007896"/* , 
            "U1008848", 
            "U1016356", 
            "U1017777", 
            "U1018828", 
            "U1029422", 
            "U1036841", 
            "U1037734", 
            "U657285", 
            "U659634"
        
    }; 
    */
    private String[][] accountAlias = {
        {"U1001569","walterIra"},
        {"U1007896","bobTrust"}, 
        {"U1029422","bobIra"},
        {"U1036841","adamTrading"},
        {"U1037734","adamIra"},
        {"U1153780","darren"},
        {"U1439501","adamForAlex"},
        {"U1559484","kevinTrading"},
        {"U1588981","adamForBoys"},
        {"U1762803","bobTrading"},
        {"U2032329","motherDevito"},
        {"U657285","walterTrade"},
        {"U659634","krisIra"}
    };
    /* select the simualted account by default */
    private int selectedAccountType = SIMULATED;
   
    private final int MAX_ACCOUNTS = 2;
    private String selItem = null;
    private boolean selMade = false;
    private static ManagedAccounts actMa = null;
    
    public class anAccount{
        private String accName;
        private boolean accEnabled;
        private String accAlias;
        public String getName(){
            return accName;
        }
        public boolean getEnabled() {
            return accEnabled;
        }
        public String getAlias() {
            return accAlias;
        }
        private anAccount(String an, boolean ae, String aa){
            
        }
    }
    public static ManagedAccounts getAllAccounts() {
        return actMa;
    }
    public anAccount allAccounts[] = { 
        new anAccount("1", true, " "), 
        new anAccount("2", false, " "),
        new anAccount("3", false, " "),
        new anAccount("4", false, " "),
        new anAccount("5", false, " "),
        new anAccount("6", false, " "),
        new anAccount("7", false, " "),
        new anAccount("8", false, " "),
        new anAccount("9", false, " "),
        new anAccount("10", false, " "),
        new anAccount("11", false, " "),
        new anAccount("12", false, " "),
        new anAccount("13", false, " "),
        new anAccount("14", false, " "),
        new anAccount("15", false, " "),
                                        
    };
    
    private boolean editMade = false;
    private Choice accList;
    
    public String getAnEnabledAccount(int ix) {
        
        if (ix >= managedAccountsTbl.length) {
            return null;
        }else if (allAccounts[ix].accEnabled == true) {
            return allAccounts[ix].accName;
        }else{
            return null;
        }
        
        
    }
    public anAccount getAnAccount(int ix) {
        if (ix >= managedAccountsTbl.length) {
            return null;
        }else {
            return allAccounts[ix];
        }
    }
    public int getNumOfAccounts() {
        return managedAccountsTbl.length;
    }
    
    private accountMenu newAccountMenu;
    private boolean isAccountEnabled = false;
    private dispAccounts actAccountMenu;
    public ManagedAccounts(String ma, boolean simTradingin) {
       
       selectedAccountType = (simTradingin == true) ? SIMULATED : REALMONEY;
       
       managedAccountsTbl = ma.split(","); 
       for(int i = 0 ; i < managedAccountsTbl.length; i++) {
            System.out.println(" "+i+" "+ managedAccountsTbl[i]);
            allAccounts[i].accName = managedAccountsTbl[i];
            allAccounts[i].accEnabled = isAllowedForTrading(managedAccountsTbl[i]);
            allAccounts[i].accAlias = getAliasFromAccount(managedAccountsTbl[i]);
        }
 
       this.actMa = this;
       //showManagedAccounts(null);
    }
    public void askForAccount() {
        selMade = false;
        actAccountMenu = new dispAccounts("Managed Accounts");
        waitForDone();
    }
    public String getSelAccount() {
        return selItem;
    }
    public void waitForDone() {
        while(selMade == false) {
            myUtils.delay(1000);
        }
    }
    
    public void setAllowedAccounts(String[] allowThese) {
        accountsOkForTradingTbl = allowThese;
    }
    
    private boolean isAllowedForTrading(String thisAccount) {
        for (int i = 0; i < accountsOkForTradingMatrix[selectedAccountType].length; i++) {
            if (thisAccount.equals(accountsOkForTradingMatrix[selectedAccountType][i]))
                return true;
            else;
        }
        return false;
    }
    
    public int numOf() {
        return managedAccountsTbl.length;
    }
    
    public String listAndSelectOne() {

        return "temp";
    }

    public String getAliasFromAccount(String forthisAccount) {
        boolean foundit = false;
        String retAlias = " ";
        
        for (int i = 0; (i < accountAlias.length) && !foundit; i++) {
            if (forthisAccount.equals(accountAlias[i][0])) {
                foundit = true;
                retAlias = accountAlias[i][1];
            }
        }
        return (retAlias);
    }
    
    public String getAccountFromAlias(String forthisAlias) {
        boolean foundit = false;
        String retAlias = " ";
        
        for (int i = 0; (i < accountAlias.length) && !foundit; i++) {
            if (forthisAlias.equals(accountAlias[i][1])) {
                foundit = true;
                retAlias = accountAlias[i][0];
            }
        }
        return (retAlias);
    }
    
    class dispAccounts extends JFrame {

        JTable jt = null;
        JScrollPane pane = null;

        dispAccounts(String title) {
            super(title);
            setSize(150, 150);
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent we) {
                    System.out.println("accountMenu: window closing...");
                    if (editMade == true) {
                    }
                    setVisible(false);
                    dispose();
                }
            });
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            newAccountMenu = new accountMenu();

            setContentPane(newAccountMenu);
            setSize(200, 200);
            pack();
            setVisible(true);
        }
    }

    private class accountMenu extends JPanel implements ActionListener, ItemListener {

        public boolean completed = false;
        final int AVE_IN_PERCENT = 0, AVE_IN_MAX_ADJ = 1, LAST_ELEMENT = 2;
        private JFormattedTextField ftf[] = new JFormattedTextField[LAST_ELEMENT];
        String des[] = new String[ftf.length]; // description of each field
        

        private accountMenu() {
            /*
             * only do the position selector if we have all positions
             */

            Label accountLable = new Label("Accounts:");
            accList = new Choice();

            add(accountLable);
            add(accList);
            
            checkListener checkHandle = new checkListener();
            
            
            for (int ix = 0; ix < managedAccountsTbl.length; ix++) {
                //accList.add(managedAccountsTbl[ix]);
                if (allAccounts[ix].accEnabled == true) {
                    accList.add(allAccounts[ix].accName);
                }
                
            }
            
            /*
             * connect a listener for account selection from the user..
             */
            accList.addItemListener(new accountSelListener());

            selItem = accList.getSelectedItem();
            

            // add each ftf[] to a BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            JButton doneButton = new JButton("Done");
            doneListener doneHandle = new doneListener();
            doneButton.addActionListener(doneHandle);

            add(doneButton);
        }

        private class checkListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                
                

                completed = false;
                
                if (command.equals("enable account")) {
                       
                    }
                }
            }
        private class doneListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Done") && (completed == false)) {
                    
                        try {

                    } catch (Exception ef) {
                        //ef.printStackTrace();
                        commonGui.prMsg("Incorrect Numeric Input. Try Again.");
                    }
                    selMade = completed = true;
                    editMade = true;

                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            JFormattedTextField event = (JFormattedTextField) e.getSource();
            Integer pid;
            

            try {
                completed = false;
                if (event.equals(ftf[AVE_IN_PERCENT])) {
                    ftf[AVE_IN_PERCENT].commitEdit();
                } else if (event.equals(ftf[AVE_IN_MAX_ADJ])) {

                    ftf[AVE_IN_MAX_ADJ].commitEdit();
                    
                    event.commitEdit();                    

                }
            } catch (Exception ef) {
                //ef.printStackTrace();
                ef.printStackTrace();
                commonGui.prMsg("Incorrect Numeric Input. Try Again." + ef);
            }

        }
        public void itemStateChanged(ItemEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());

        }

        public void processKeyEvent(KeyEvent ke) {
            int key = ke.getID();
            System.out.println("key:" + key);
        }
        
        private class accountSelListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
                selItem = accList.getSelectedItem();
                System.out.println("accountSelListener: selacc = " + selItem);
            }
        }
    }        
        
    public void showManagedAccounts(positions actPos) {
        JPanel jp = new JPanel();
        JLabel jl;
        JFrame jf = new JFrame();
        String buildStr = "<html> Managed accouts: <P>";
        
        jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jf.setSize(100, 250);
        
        if ((actPos != null) && (actPos.getAccountNumber() != "New")){
            selItem = actPos.getAccountNumber();
        }
        
        
        for(int idx = 0; idx < managedAccountsTbl.length; idx++) {
            buildStr += (managedAccountsTbl[idx] + 
                        ((allAccounts[idx].accEnabled == true)?
                        "  (Enabled)":"  (Disabled)") + "<P>"
                        );
        }
        buildStr += "Selected Account: " + selItem;
        
        jl = new JLabel(buildStr);
        jp.add(jl);
        jf.add(jp);
        jf.pack();
        jf.setVisible(true);
                        
    }    
    
}
