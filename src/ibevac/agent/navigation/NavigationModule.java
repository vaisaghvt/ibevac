package ibevac.agent.navigation;


import ibevac.agent.IbevacAgent;
import abmcs.motionplanning.level1.Level1MotionPlanning;
import abmcs.motionplanning.level2.Level2MotionPlanning;
import abmcs.motionplanning.level3.Level3MotionPlanning;

/**
 * <h4>A module to which all navigation related functionality of the agent is 
 * delegated.</h4>
 * 
 *
 *  @author     <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 *  @version    $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class NavigationModule {

	Level1MotionPlanning level1;
	Level2MotionPlanning level2;
	Level3MotionPlanning level3;
	IbevacAgent me;

	public NavigationModule(IbevacAgent ibevacAgent,
			Level1MotionPlanning level1,
			Level2MotionPlanning level2, Level3MotionPlanning level3) {
		me = ibevacAgent;

		this.level1 = level1;
		this.level2 = level2;
		this.level3 = level3;

	}

	public Level1MotionPlanning getLevel1MotionPlanning() {
		return level1;
		
	}

	public Level2MotionPlanning getLevel2MotionPlanning() {
		
		return level2;
	}

	public Level3MotionPlanning getLevel3MotionPlanning() {
		return level3;
	}

}

	

