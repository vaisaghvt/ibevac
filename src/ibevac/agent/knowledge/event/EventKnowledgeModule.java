package ibevac.agent.knowledge.event;

import ibevac.agent.AgentDescriptionModule.AgentType;
import ibevac.agent.knowledge.event.PhaseBucket.Phase;
import ibevac.agent.planner.Planner;
import ibevac.cue.Cue;
import ibevac.cue.Cue.Ambiguity;

import java.util.EnumMap;
import java.util.HashMap;

/**
 * This class is responsible for handling cues and their perception.
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class EventKnowledgeModule {

    private final EnumMap<Phase, PhaseBucket> phaseValues;
    private Planner planner;

    /**
     * Initializes the phase buckets.
     *
     * @param agentType
     * @see #createPhaseBuckets(ibevac.agent.AgentDescriptionModule.AgentType)
     */
    public EventKnowledgeModule(AgentType agentType) {
        phaseValues = createPhaseBuckets(agentType);
    }

    /**
     * Initializes the planner. Seperate from teh constructor becase of the
     * complicated set of interdependencies.
     *
     * @param planner
     */
    public void setPlanner(Planner planner) {
        this.planner = planner;
    }

    /**
     * Find the effects of the passed cue. A seperate function is created so that
     * this can be extended later for showing memory effects
     *
     * @param cue
     * @see #processEffects(ibevac.cue.Cue)
     */
    public void perceiveCue(Cue cue) {
        processEffects(cue);
    }

    /**
     * Determine the effects of this particular cue on all the phase buckets.
     *
     * @param cue
     * @see Phase
     */
    private void processEffects(Cue cue) {
        if (cue.indicatesFire()) {
            phaseValues.get(Phase.UNUSUAL).incrementByValue(cue.ambiguityLevel().getUnusual());
            phaseValues.get(Phase.FIRE).incrementByValue(cue.ambiguityLevel().getFire());
        } else {
            phaseValues.get(Phase.UNUSUAL).decrementByValue(cue.ambiguityLevel().getUnusual());
            phaseValues.get(Phase.FIRE).decrementByValue(cue.ambiguityLevel().getFire());

        }
    }

    /**
     * Resets all the phase buckets to have 0 value.
     */
    public void resetAllPhases() {
        for (PhaseBucket phase : phaseValues.values()) {
            phase.reset();
        }
        planner.informAbout(Phase.NONE);
    }

    /**
     * A special function is created to process the lack of cues which is itself
     * a cue.
     */
    public void perceiveLackOfCues() {

        for (Phase phase : phaseValues.keySet()) {
            switch (phase) {
                case UNUSUAL:
                    phaseValues.get(phase).decrementByValue(100);
                    break;
                case FIRE:
                    phaseValues.get(phase).decrementByValue(100);
                    break;
            }
        }
    }

    /**
     * This function acts as an abstract factory that creates the phase buckets
     * as per the agent type specified. This enables us to model the fact that
     * the phase transitions for diffrent types of people might be different.
     *
     * @param agentType
     * @return
     * @see AgentType
     */
    private EnumMap<Phase, PhaseBucket> createPhaseBuckets(AgentType agentType) {
        EnumMap<Phase, PhaseBucket> phaseBuckets = new EnumMap<Phase, PhaseBucket>(Phase.class);
        switch (agentType) {
            case DEFAULT:
                phaseBuckets.put(Phase.NONE, new PhaseBucket(Integer.MAX_VALUE));
                phaseBuckets.put(Phase.UNUSUAL, new PhaseBucket(10000));
                phaseBuckets.put(Phase.FIRE, new PhaseBucket(10000));
                return phaseBuckets;
            case MANAGEMENT:
                phaseBuckets.put(Phase.NONE, new PhaseBucket(Integer.MAX_VALUE));
                phaseBuckets.put(Phase.UNUSUAL, new PhaseBucket(5));
                phaseBuckets.put(Phase.FIRE, new PhaseBucket(10000));
                return phaseBuckets;
        }
        return null;
    }

    /**
     * This function instructs the planner to take care of strategies appropriately
     * based on the current state of cues observed and the state of the phase
     * buckets.
     */
    public void updateState() {
        if (phaseValues.get(Phase.FIRE).upperThresholdBreached()) {
            planner.informAbout(Phase.FIRE);
        } else if (phaseValues.get(Phase.UNUSUAL).upperThresholdBreached()) {
            planner.informAbout(Phase.UNUSUAL);
        } else if (phaseValues.get(Phase.UNUSUAL).lowerThresholdBreached()) {
            planner.informAbout(Phase.NONE);
        }
    }
}
