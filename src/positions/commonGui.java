/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
/**
 *
 * @author walterstevenson
 */
public class commonGui extends JFrame{
    public static void prMsg(String msg) {
        postInformationMsg(msg);
    }
    public static void postInformationMsg(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }
    private static javax.swing.JTextArea genTextArea;
    private static javax.swing.JScrollPane jScrollPaneGenArea;
    private static JPanel controlPanel;
    private static JFrame frame;
    
    public static void fuckingTest(String header, String msg){
        JLabel originalString =  new JLabel(msg);       
        JPanel panel = new JPanel();
        panel.add(originalString);
        
        JDialog diag = new JDialog();
        diag.setPreferredSize(new Dimension(300, 100));
        diag.setLocationRelativeTo(null);
        diag.setTitle(header);
        diag.setModal(true);
        diag.setAlwaysOnTop(true);
        diag.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        diag.getContentPane().add(panel);
        diag.pack();
        diag.setVisible(true);
    }

    public static void postToTextAreaMsg(String header, String msg) {
        JPanel middlePanel = new JPanel();
        middlePanel.setBorder(new TitledBorder(new EtchedBorder(), header));

        // create the middle panel components
        JTextArea display = new JTextArea(16, 58);
        display.setEditable(false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        display.append(msg);
        //Add Textarea in to middle panel
        middlePanel.add(scroll);
        // My code
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.add(middlePanel);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setAlwaysOnTop(true);
        dialog.setEnabled(true);
        dialog.setVisible(true);
        
    }


    public static String getUserInput(String prompt, String example) {

        String inString = (String) JOptionPane.showInputDialog(null,
                "Please Enter " + prompt,
                prompt, JOptionPane.PLAIN_MESSAGE, null,
                null, example
                 );
   
        System.out.println("User's input: " + inString);
 
 
        return (inString);
    } 
    public static String getUserInput(String prompt, String[] choices) {
        
        String inString = (String) JOptionPane.showInputDialog(null,
                        "Please Enter " + prompt,
                        prompt, JOptionPane.PLAIN_MESSAGE,null,
                        choices, choices[0]
                 );
   
        System.out.println("User's input: " + inString);
 
 
        return (inString);
    } 
    public static boolean getUserInput(String prompt, Object panel) {        
        int res = 0;
        
        int result = JOptionPane.showConfirmDialog(null, panel,
                prompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if(result == JOptionPane.OK_OPTION)
            return true;
        else
            return false;
    }
    
    public static int postConfirmationMsg(String msg) {
        int retVal = JOptionPane.showConfirmDialog(null, msg, "Confirmation On Operation", JOptionPane.YES_NO_OPTION);
        return(retVal);
    }  
    static Frame f;
    static boolean complete = false;
    
    private static class doneListener implements ActionListener {
        private String sel;
        private Choice selChoice;
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                if (command.equals("Done")) {
                    complete = true;
                    sel = selChoice.getSelectedItem();
                    System.out.println("you chose: " + sel);
                    f.setVisible(false);
                    f.dispose();

                }
            }
            String getSelection(){
                return(sel);
            }
            void setChoice(Choice c) {
                selChoice = c;
            }
        }
    
    
    
    public static String dropList(String title, String [] s) {

        final Frame frame = f =  new Frame("Select "+title);
        Label label = new Label("From List:");
        final Choice choice = new Choice();
        frame.add(label);
        frame.add(choice);
        for (int ix = 0; ix < s.length; ix++) {
            choice.add(s[ix]);
        }
        
        JButton doneButton = new JButton("Done");
        doneListener doneHandle = new doneListener();
        doneButton.addActionListener(doneHandle);
        doneHandle.setChoice(choice);
        frame.add(doneButton);
        
            
        frame.setLayout(new FlowLayout());
        frame.setSize(250, 150);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                //System.exit(0);
                    frame.setVisible(false);
                    frame.dispose();
            }
            
            
        });
        
        choice.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent ie) {
//                String str = (String) choice.getSelectedItem();
//                System.out.println("you chose: " + str);
            }
        });
       
        if(complete) {
            return(doneHandle.getSelection());
        }
        

        return ("hey");
    }
}
