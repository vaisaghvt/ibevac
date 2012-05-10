package ibevac.agent.knowledge;

import ibevac.agent.knowledge.event.EventKnowledgeModule;
import ibevac.agent.knowledge.environment.EnvironmentKnowledgeModule;
import ibevac.agent.IbevacAgent;
import ibevac.agent.knowledge.environment.CompleteKnowledge;
import ibevac.agent.knowledge.environment.CompleteKnowledgeInverted;
import ibevac.agent.planner.DefaultPlanner;
import ibevac.agent.planner.Planner;
import ibevac.cue.MessageCue;
import ibevac.cue.ShoutedCue;
import ibevac.datatypes.CEvacuationScenario;
import java.util.Set;

/**
 *  <h4>This class is the combined knowledge base of the agent. The main purpose
 *  is to improve the structure of the architecture.</h4>
 *  
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class KnowledgeBase {

    /**
     * Reference to the agent's environment knowledge module
     */
    private EnvironmentKnowledgeModule environmentKnowledge;
    /**
     * Reference to the agent's event knowledge module
     */
    private EventKnowledgeModule eventKnowledge;
    /**
     * A reference to the agent
     */
    IbevacAgent me;

    /**
     * Simple constructor just initializing the internal references to the 
     * initialized ones
     * @param agent
     * @param envKM
     * @param eventKM 
     */
    public KnowledgeBase(IbevacAgent agent,
            EnvironmentKnowledgeModule envKM, EventKnowledgeModule eventKM) {
//        environmentKnowledge = new CompleteKnowledge(scenario);
//        eventKnowledge = new EventKnowledgeModule(agent.getPlanner());
        environmentKnowledge = envKM;
        eventKnowledge = eventKM;
        me = agent;

    }

    /**
     * 
     * @return reference to environment Knowledge 
     */
    public EnvironmentKnowledgeModule getEnvironmentKnowledge() {
        return environmentKnowledge;
    }

    /**
     * 
     * @return a reference to event knowledge
     */
    public EventKnowledgeModule getEventKnowledge() {
        return eventKnowledge;
    }

    /**
     * Set's the planner for the event knowledge module to call when phase 
     * thresholds are met
     * @param planner : reference to planner
     */
    public void setPlanner(Planner planner) {
        this.eventKnowledge.setPlanner(planner);
    }

    /**
     * Returns a set of messages that contain information that is shouted by the 
     * agent
     * @return a Set of MessageCue(s) 
     */
    public Set<ShoutedCue> getShoutedInformation() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}