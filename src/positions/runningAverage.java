/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package positions;

/**
 *
 * @author earlie87
 */
public class runningAverage {

    private float runningAveVal;
    private float runningTotalSamples;
    private int runningSamples;
    private boolean runningAveOn;
    private boolean partialBin;
    private String todaysDate = null;

    runningAverage() {
        runningAveVal = 0;
        runningSamples = 0;
        runningTotalSamples = 0;
        runningAveOn = false;
        partialBin = true;

    }

    public void runningPartialSet(boolean partial) {
        partialBin = partial;
    }

    public boolean runningPartialGet() {
        return (partialBin);

    }

    public void runningAveClear() {
        runningAveVal = 0;
        runningSamples = 0;
        runningTotalSamples = 0;
    }

    public void runningAveTick(float sample) {

        if (runningAveOn == true) {
            runningTotalSamples += sample;
            runningSamples++;
            runningAveVal = runningTotalSamples / runningSamples;
            prRunningAveParms();
            System.out.println("thisSample : " + sample);


        }
    }

    public void runningAveSet(float ra) {
        runningAveVal = ra;
    }

    public void runningSamplesSet(int rs) {
        runningSamples = rs;
    }

    public void runningTotalSamplesSet(float rts) {
        runningTotalSamples = rts;
    }

    public void prRunningAveParms() {
        System.out.println("\nrunningSamples : " + this.runningSamples);
        System.out.println("runningTotalSamples : " + this.runningTotalSamples);
        System.out.println("runningAveVal : " + this.runningAveVal);


    }

    public float runningAveGet() {
        return runningAveVal;
    }

    public int runningSamplesGet() {
        return runningSamples;
    }

    public void runningAveCtrl(boolean on) {
        runningAveOn = on;

    }
    public void partialBinCtrl(boolean on) {
        partialBin = on;

    }

    public boolean isRunningAveOn() {
        return runningAveOn;
    }

    public float runningTotalSamplesGet() {
        return runningTotalSamples;
    }

    public boolean isPartialBin() {
        return partialBin;
    }
    public void dateSet(String d) {
        todaysDate = d;
    }
    public String dateGet() {
        return(todaysDate);
    }
}
