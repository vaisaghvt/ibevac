/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.agent.knowledge.event;

import ibevac.agent.planner.Planner;

/**
 *  This class is responsible for handling cues and their perception. 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class PhaseBucket {

    /**
     * The different phase buckets that are there currently. Technically, this 
     * should be expanded to include a bucket for risk to self and risk to 
     * others
     */
    public enum Phase {

        UNUSUAL,
        FIRE,
        NONE;
    }
    /**
     * The maximum threshold for this Phase. On overflowing this threshold an 
     * appropriate signal is sent to the planner to make a change of plans
     */
    private final int threshold;
    /**
     * The current value of amount of information in the bucket.
     */
    private int value;

    PhaseBucket(int threshold) {
        this.threshold = threshold;
        value = 0;
    }

    /**
     * 
     * @return the integer value that information should be set off a trigger 
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * 
     * @return the current value of information in the bucket.
     */
    public int getValue() {
        return value;
    }

    /**
     * increment value by 1
     */
    public void incrementValue() {
        value++;
        assert value <= threshold;
    }

    /**
     * reduce value by 1
     */
    public void decrementValue() {
        value--;
        assert value >= 0;

    }

    /**
     * 
     * @param num the amount by which value should be increased 
     */
    public void incrementByValue(int num) {
        assert num >= -1;
        if (num != -1) {
            value += num;
        } else {
            incrementToThreshold();
        }
        if (value > threshold) {
            value = threshold;
        }
    }

    /**
     * 
     * @param num the amount by which value should be decreased 
     */
    public void decrementByValue(int num) {
        assert num >= -1;
        if (num != -1) {
            value -= num;
        } else {
            reset();
        }
        if (value < 0) {
            value = 0;
        }

    }

    /**
     * To set the value to the threshold value so that the trigger for this is 
     * sent soon
     */
    void incrementToThreshold() {
        this.value = threshold;

    }

    /**
     * This function is called by the planner when the agent decides to give up 
     * on a particular phase. This basically means that the agent is forced to 
     * find more clues before it decides to react again
     * @param phase 
     */
    public void reset() {
        this.value = 0;

    }

    /**
     * 
     * @return true if overflow
     *          false if no overflow
     */
    public boolean upperThresholdBreached() {
        return value >= threshold;
    }

    /**
     * @return true if it has reached a lowest possible value and trigger has to be
     * sent to reset back from this stage
     *          false otherwise
     */
    public boolean lowerThresholdBreached() {
        return value <= 0;
    }
}
