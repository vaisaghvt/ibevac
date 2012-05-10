package ibevac.agent;

import ibevac.agent.planner.ManagementPlanner;
import ibevac.agent.knowledge.KnowledgeBase;
import ibevac.agent.knowledge.environment.CompleteKnowledge;
import ibevac.agent.knowledge.event.EventKnowledgeModule;
import ibevac.agent.planner.DefaultPlanner;
import ibevac.datatypes.CEvacuationScenario;

/**
 *  <h4>This class helps create a specific type of agent</h4>
 *  
 *  <h4>Agent type determines the thresholds and list of strategies of the agent 
 * and also his/her knowledge of the environment. Delegation Pattern</h4>
 * 
 * 
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class AgentDescriptionModule {


    /**
     * The type of agent.
     * 
     */
    public enum AgentType {

        /**
         * Most normal agents. They are supposed to have incomplete knowledge and
         * standard reaction to fire.
         */
        DEFAULT, // TODO Implement partial knowledge
        /**
         * These are agents that are trained to be in charge during the fire.
         * They generally have complete knowledege and strategies that enable 
         * them to help others as much as possible.
         */
        MANAGEMENT;  // TODO Implement management behavior
    }

    private AgentDescriptionModule() {
    }

    /**
     *
     *  Somewhat like static factory method to create a default agent. Rather than
     * creating the agent. It initializes his knowledge and strategy list.
     * 
     * @param	scenario The CEvacuationScenario extracted from xml which describes
     *  the environment to be used for initializing environment knowledge
     *
     * @param	agent    The reference to the agent whose knowledge has to be 
     *  initialized
     * 
     *		
     */
    public static void createDefaultAgent(CEvacuationScenario scenario, IbevacAgent agent) {

        agent.knowledge = new KnowledgeBase(agent,
                new CompleteKnowledge(scenario),
                new EventKnowledgeModule(AgentType.DEFAULT));

        agent.planner = new DefaultPlanner(agent,
                agent.knowledge.getEventKnowledge(),
                agent.knowledge.getEnvironmentKnowledge());
        agent.knowledge.setPlanner(agent.planner);
    }
    
    /**
     *
     *  Somewhat like static factory method to create a default agent. Rather than
     * creating the agent. It initializes his knowledge and strategy list.
     * 
     * @param	scenario The CEvacuationScenario extracted from xml which describes
     *  the environment to be used for initializing environment knowledge
     *
     * @param	agent    The reference to the agent whose knowledge has to be 
     *  initialized
     * 
     *		
     */
    public static void createManagementAgent(CEvacuationScenario scenario, IbevacAgent agent) {

        agent.knowledge = new KnowledgeBase(agent,
                new CompleteKnowledge(scenario),
                new EventKnowledgeModule(AgentType.MANAGEMENT));

        agent.planner = new ManagementPlanner(agent,
                agent.knowledge.getEventKnowledge(),
                agent.knowledge.getEnvironmentKnowledge());
        
        agent.knowledge.setPlanner(agent.planner);
    }
}
