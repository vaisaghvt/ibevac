package ibevac.gui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;

/**
 * This class enables the inspection of an agent by simple double clicking on it
 * in the space.  It is delegated to by the IbevacAgent
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 16/Apr/2012 $
 */
public class AgentInspector extends Inspector {

    /**
     *
     */
    private static final long serialVersionUID = 8318361124309436062L;


    private final Inspector originalInspector;

    public AgentInspector(Inspector inspector, LocationWrapper wrapper,
                          GUIState guiState) {
        originalInspector = inspector;

//		final IbevacAgent agent = (IbevacAgent) wrapper.getObject();
//		final SimState state = guiState.state;
//		final Controller console = guiState.controller;

        // now let's add a Button
        Box viewBox = new Box(BoxLayout.X_AXIS);

        // set up our inspector: keep the properties inspector around too
        setLayout(new BorderLayout());
        add(originalInspector, BorderLayout.CENTER);
        add(viewBox, BorderLayout.SOUTH);
    }

    @Override
    public void updateInspector() {
        originalInspector.updateInspector();

    }

}
