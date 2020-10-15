/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package positions;

/**
 *
 * @author walterstevenson
 */
public class Semaphore {

    private int count;

     public Semaphore(int n) {
        this.count = n;
    }

    public  synchronized void acquire() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                //keep trying
                System.out.println("Semaphore wait: "+e);
                }
        }
        count--;
    }

    public synchronized void release() {
        count++;
        notify(); //alert a thread that's blocking on this semaphore
    }
}    
