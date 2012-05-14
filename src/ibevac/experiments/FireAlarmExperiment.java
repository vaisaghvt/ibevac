/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ibevac.experiments;

import ibevac.cue.Cue.Ambiguity;
import ibevac.cue.FireAlarmCue;
import ibevac.cue.MessageCue;
import ibevac.datatracker.DatabaseHandler;
import ibevac.engine.IbevacModel;

/**
 * The value of the fire alarm is set to the single value set in args
 *
 * @author vaisagh
 */
public class FireAlarmExperiment {
    public static final int NUMBER_OF_REPLICATIONS = 90;
    public static final int NUMBER_OF_AGENTS = 200;
     public static final int NUMBER_OF_MANAGEMENT = 0;
    public static final int REPORT_TIME= 500;
    public static final int STARTING_SEED = 20;
    public static final int EXPERIMENT_ID =1;
    
    public static void main(String[] args){
        if(args.length ==1){
            int ambiguityOfFireAlarm = Integer.parseInt(args[0]);
            //Bug possible
            FireAlarmCue.ambiguityLevel = Ambiguity.values()[ambiguityOfFireAlarm];
            MessageCue.defaultAmbiguity = Ambiguity.TEN;
            DatabaseHandler.instance().checkAndAddExperiment(EXPERIMENT_ID, "Fire Alarm Ambiguity Experiment");
            
            IbevacModel.runLoop(STARTING_SEED, REPORT_TIME, NUMBER_OF_REPLICATIONS, 
                    NUMBER_OF_AGENTS, NUMBER_OF_MANAGEMENT,  EXPERIMENT_ID, args[0]);
        
        }else {
            System.err.println("Invalid input");
        }
    }
    
}
