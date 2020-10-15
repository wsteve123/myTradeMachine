/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;

import tradeMenus.genComboBoxDialogForm;

/**
 *
 * @author walterstevenson
 */
public class closePosition {
    private String posToCloseStr;
    private positionData posToClose;
    private positionEditor posToEdit;
    public closePosition(positions allPositions) {
        String[] posList;
        posList = myUtils.fillTickerList(allPositions);
        genComboBoxDialogForm posCb; 
        if (allPositions.posDataEmpty() == true) {
            commonGui.prMsg("No positions to close.");
        }else {
            //posToCloseStr = commonGui.getUserInput("Ticker Of Position To Close", "csco");
            posCb = new genComboBoxDialogForm(new javax.swing.JFrame(), true, "Select Ticker", posList);
            posCb.setVisible(true);
            posToCloseStr = posCb.getSelection();
            if ((posToClose = allPositions.posDataSearch(posToCloseStr)) != null) {
                if (posToClose.closed == true) {
                    commonGui.postInformationMsg("Position is already Closed.");
                } else {
                    posToEdit = new positionEditor(posToClose);
                    /*
                     * first do short close out..
                     */
                    posToEdit.closeOutPosition();
                    /*
                     * then long position close out..
                     */
                    posToEdit = new positionEditor(posToClose);
                    posToEdit.closeOutPosition();
                    commonGui.postInformationMsg("Position Is Now Closed.");

                    System.out.println("Close Position!!");
                }
            } else {
                commonGui.postInformationMsg("position not found.");
            }
        }
        
    }

} /* closePosition */
