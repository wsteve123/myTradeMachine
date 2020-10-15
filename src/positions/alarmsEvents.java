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
public class alarmsEvents extends Thread{
    public boolean wally = false;
    private final int ONE_SEC = 1000;
    private final int TEN_SEC = ONE_SEC * 10;
    private final int ONE_MIN = ONE_SEC * 60;
    private final int FIVE_MIN = ONE_MIN * 5;
    private final int TEN_MIN = FIVE_MIN * 2;
    
    int runFrequency = TEN_MIN;
    int runTimes =0;
    positions actPositions = null;
    
    
    public alarmsEvents(positions positionsIn) {
        actPositions = positionsIn;
        this.start();
    }
    private static boolean marketIsClosed = false;
    private static boolean marketIsOpen = !marketIsClosed;
    public static void setMarketIsClosed(boolean closed) {
        marketIsClosed = closed;
        marketIsOpen = !marketIsClosed;
        System.out.println("setMarketIsClosed: in alarmEvents " + marketIsClosed);
    }
    
    public void run() {
        
        while (true) {
            System.out.println("alarmEvents: running.. " + runTimes + " times.");
            
            
            if (marketIsOpen == true) {
                actPositions.vmManager.saveToDisk(actPositions.getPositionFileName());
            }



            runTimes++;
            try {
                alarmsEvents.sleep(runFrequency);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
