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
import ibevac.environment.FireSpace;

/**
 * The value of the fire alarm is set to the single value set in args
 *
 * @author vaisagh
 */
public class CommunicationExperiment {
    public static final int NUMBER_OF_REPLICATIONS = 90;
    public static final int NUMBER_OF_AGENTS = 200;
    public static final int REPORT_TIME= 500;
    public static final int STARTING_SEED = 20;
    public static final int EXPERIMENT_ID = 2;
    
    public static void main(String[] args){
        if(args.length ==1){
            int messageAmbiguity = Integer.parseInt(args[0]);
            //Bug possible
            MessageCue.defaultAmbiguity = Ambiguity.values()[messageAmbiguity];
            FireAlarmCue.ambiguityLevel = Ambiguity.TEN;
            DatabaseHandler.instance().checkAndAddExperiment(EXPERIMENT_ID, "Message Ambiguity Experiment");
            
            IbevacModel.runLoop(STARTING_SEED, REPORT_TIME, NUMBER_OF_REPLICATIONS, 
                    NUMBER_OF_AGENTS, EXPERIMENT_ID, args[0]);
        
        }else {
            System.err.println("Invalid input");
        }
    }
    
}
