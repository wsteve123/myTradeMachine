/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;
import javax.swing.*;
/**
 *
 * @author walterstevenson
 */

public class positionGuis {
    positionGuis() {
        commonGuis.postInformationMsg("hey");
    }
    public void prInfo(String info) {
        commonGuis.postInformationMsg(info);
    }
    
}

class commonGuis {
    public static void postInformationMsg(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }
}