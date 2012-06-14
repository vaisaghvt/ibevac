/**
 *
 */
package ibevac.gui;

import ibevac.agent.IbevacAgent;
import ibevac.datatracker.PEDDataTracker;
import ibevac.engine.IbevacModel;
import ibevac.environment.FireSpace;
import ibevac.environment.FireSpace.CellState;
import ibevac.environment.SmokeSpace;
import ibevac.gui.ObstaclePortrayal;
import ibevac.utilities.IbevacRNG;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastObjectGridPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.util.Double2D;
import sim.util.gui.SimpleColorMap;

/**
 * This class is responsible for the overall portrayal of the environment.
 * It contains the main that is to be called if the animation is to be seen.
 * As well as all the controls for calling and controlling the different
 * portrayals.
 *
 * @author <A HREF="mailto:vaisagh1@e.ntu.edu.sg">Vaisagh</A>
 * @version $Revision: 1.0.0.0 $ $Date: 9/Nov/2011 $
 */
public class IbevacGui extends GUIState {

    /**
     * A reference to the model.
     */
    private IbevacModel model;

    /**
     * The Display2Ds on which everything is drawn. These are like the pallete
     * on which the painting is done. A list is to make one for each floor.
     */
    private ArrayList<Display2D> displays = new ArrayList<Display2D>();
    /**
     * Creates a frame for each floor. the display mentioned above are kept in
     * each frame.
     */
    private ArrayList<JFrame> displayFrames = new ArrayList<JFrame>();


    private final ArrayList<FastObjectGridPortrayal2D> firePortrayals = new ArrayList<FastObjectGridPortrayal2D>();
    private final ArrayList<FastValueGridPortrayal2D> smokePortrayals = new ArrayList<FastValueGridPortrayal2D>();
    private final ArrayList<ContinuousPortrayal2D> agentPortrayals = new ArrayList<ContinuousPortrayal2D>();
    private final ArrayList<ContinuousPortrayal2D> obstaclePortrayals = new ArrayList<ContinuousPortrayal2D>();
    private final ArrayList<ContinuousPortrayal2D> backgroundPortrayals = new ArrayList<ContinuousPortrayal2D>();

    /**
     * The constructor which adds all the required portrayals to each array and
     * sets up things to start.
     *
     * @param state
     */
    IbevacGui(SimState state) {
        super(state);
        model = (IbevacModel) state;

        for (int floorNumber = 0; floorNumber < model.getSpace().getNumberOfFloors(); floorNumber++) {

            firePortrayals.add(new FastObjectGridPortrayal2D(false) {

                private static final long serialVersionUID = -347075072221219800L;

                @Override
                public double doubleValue(Object obj) {
                    if (obj instanceof FireSpace.CellState) {
                        CellState currentCell = (CellState) obj;
                        int chosenColor = 0;
                        if (currentCell.equals(CellState.BURNING)) {
                            chosenColor = IbevacRNG.instance().nextInt(
                                    currentCell.getColorList().size()) + 1;
                        }
                        return chosenColor;
                    } else {
                        return super.doubleValue(obj);
                    }

                }
            });
            firePortrayals.get(floorNumber).setMap(
                    new SimpleColorMap(new Color[]{new Color(0, 0, 0, 0), Color.red,
                            Color.orange, Color.yellow}));
            smokePortrayals.add(new FastValueGridPortrayal2D(false));

            smokePortrayals.get(floorNumber).setMap(
                    new SimpleColorMap(new Color[]{new Color(0, 0, 0, 0)},
                            0, SmokeSpace.SMOKE_LIMIT,
                            new Color(0, 0, 0, 25), new Color(0, 0, 0, 150)));

            agentPortrayals.add(new ContinuousPortrayal2D());

            obstaclePortrayals.add(new ContinuousPortrayal2D());

            backgroundPortrayals.add(new ContinuousPortrayal2D());

        }
    }

    /**
     * Run at the start of the simulation. Calls the start on the model, sets up
     * the portrayals and then renders all the required frames.
     *
     * @see IbevacGui#setupPortrayals()
     */
    @Override
    public void start() {
        super.start();
        setupPortrayals(); // set up our portrayals
        for (Display2D display : displays) {
            display.reset(); // reschedule the displayer
            display.repaint(); // redraw the display
        }
    }

    /**
     * maps each portrayal to the respective field and also sets up the image for
     * the background portrayal.
     */
    private void setupPortrayals() {

        for (int floorNumber = 0; floorNumber < model.getSpace().getNumberOfFloors(); floorNumber++) {

            firePortrayals.get(floorNumber).setField(
                    model.getSpace().getFireModel().getFireSpace(floorNumber));

            smokePortrayals.get(floorNumber).setField(model.getSpace().
                    getSmokeModel().getSmokeSpace(floorNumber));

            agentPortrayals.get(floorNumber).setField(
                    model.getSpace().getAgentSpace(floorNumber));
            agentPortrayals.get(floorNumber).setPortrayalForClass(
                    IbevacAgent.class, new AgentPortrayal());


            obstaclePortrayals.get(floorNumber).setField(
                    model.getSpace().getObstacleSpace(floorNumber));

            obstaclePortrayals.get(floorNumber).setPortrayalForAll(
                    new ObstaclePortrayal(this.model.getSpace()));


//            ImageIcon imageIcon = new ImageIcon(this.model.getSpace().getImage(floorNumber));
//            Image image = imageIcon.getImage();


            BufferedImage originalImage = null;
            try {
                originalImage = ImageIO.read(new File(this.model.getSpace().getImage(floorNumber)));
                System.out.println("success");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
//            int dimension = Math.max((int) (model.getWidth(floorNumber) / IbevacModel.scale), (int) (model.getHeight(floorNumber) / IbevacModel.scale));

            Continuous2D randomField = new Continuous2D(
                    1,
                    1,
                    1.02);
            randomField.setObjectLocation("", new Double2D(0.495, 0.5));

            backgroundPortrayals.get(floorNumber).setField(randomField);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

//            BufferedImage resizeImageJpg = resizeImage(originalImage, type, floorNumber);
//            ImageIO.write(resizeImageJpg, "jpg", new File("c:\\image\\mkyong_jpg.jpg"));


            backgroundPortrayals.get(floorNumber).setPortrayalForAll(

                    new ImagePortrayal2D(originalImage.getScaledInstance(
                            (int) (model.getWidth(floorNumber) / IbevacModel.scale),
                            (int) (model.getHeight(floorNumber) / IbevacModel.scale),
                            type)));

        }

    }

    /**
     * initializes the controller and the displays and attaches all the portrayals
     * to the display.
     *
     * @param c
     */
    @Override
    public void init(Controller c) {
        super.init(c);

        model = (IbevacModel) state;
        for (int floorNumber = 0; floorNumber < model.getSpace().getNumberOfFloors(); floorNumber++) {

            displays.add(new Display2D(model.getWidth(floorNumber)
                    / IbevacModel.scale, model.getHeight(floorNumber)
                    / IbevacModel.scale, this, 1));

            displayFrames.add(displays.get(floorNumber).createFrame());
            c.registerFrame(displayFrames.get(floorNumber)); // register the
            // frame so it
            // appears in
            // the "Display" list
            displayFrames.get(floorNumber).setTitle("Floor #" + floorNumber);
            displayFrames.get(floorNumber).setVisible(true);
            displayFrames.get(floorNumber).setResizable(true);
            displays.get(floorNumber).attach(backgroundPortrayals.get(floorNumber), "Overlay");
            displays.get(floorNumber).attach(firePortrayals.get(floorNumber),
                    "Fire");
            displays.get(floorNumber).attach(smokePortrayals.get(floorNumber),
                    "Smoke");
            displays.get(floorNumber).attach(agentPortrayals.get(floorNumber),
                    "People");
            displays.get(floorNumber).attach(
                    obstaclePortrayals.get(floorNumber), "Obstacles");
//            displays.get(floorNumber).attach(
//                    backgroundPortrayals.get(floorNumber), "Background");


//            displays.get(floorNumber).setBackdrop(null);
//
//            BufferedImage background = null;
//            displays.get(floorNumber).setOpaque(false);
//            try {
//                background = ImageIO.read(new File(this.model.getSpace().getImage(floorNumber)));
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
////            TexturePaint tp = new TexturePaint(background, new Rectangle(0, 0,
//                    (int) (model.getWidth(floorNumber) / IbevacModel.scale),
//                    (int) (model.getHeight(floorNumber) / IbevacModel.scale)));
//
//
//            displays.get(floorNumber).setBackdrop(tp);
//            displays.get(floorNumber).setOpaque(true);

            displayFrames.get(floorNumber).setVisible(true);
            displayFrames.get(floorNumber).setLocation(floorNumber * 300 + 10,
                    floorNumber * 50 + 10);
            displayFrames.get(floorNumber).setResizable(false);
        }
    }

    /**
     * Called by the Console when the user is loading in a new state from a
     * checkpoint. The new state is passed in as an argument. The default
     * version simply calls finish(), then sets this.state to the new state. You
     * should override this, calling super.load(state) first, to reset your
     * portrayals etc. to reflect the new state. state.start() will NOT be
     * called. Thus anything you handled in start() that needs to be reset to
     * accommodate the new state should be handled here. We recommend that you
     * call repaint() on any Display2Ds.
     */
    @Override
    public void load(SimState state) {
        super.load(state);

        setupPortrayals(); // set up our portrayals
        for (Display2D display : displays) {
            display.reset(); // reschedule the displayer
            display.repaint(); // redraw the display
        }
    }

    /**
     * A finalizer...
     */
    @Override
    public void quit() {
        super.quit();

        for (JFrame displayFrame : displayFrames) {
            if (displayFrame != null) {
                displayFrame.dispose();
            }
            displayFrame = null; // let gc
        }
        displayFrames = null;

        for (Display2D display : displays) {
            if (display != null) {
                display = null;
            }
            display = null; // let gc
        }
        displays = null;

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new IbevacGui(new IbevacModel()).createController();
    }

    public static String getName() {
        return "The IBEVAC Model";
    }

    public static Object getInfo() {
        return "<H2>IBEVAC</H2>"
                + "<p>This is an Information Based Crowd Evacuation Model </p>";
    }


}
