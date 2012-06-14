package fptool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;


public class Main extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;

    private static final String APPLICATION_TITLE = "EVAC Scenario Editor";

    private JMenuBar menubar = new JMenuBar();
    private JMenu mFile = new JMenu("File");
    private JMenuItem miNew = new JMenuItem("New...");
    private JMenuItem miOpen = new JMenuItem("Open...");
    private JMenuItem miSave = new JMenuItem("Save");
    private JMenuItem miExport = new JMenuItem("Export");
    private JMenuItem miClose = new JMenuItem("Close");
    private JMenuItem miExit = new JMenuItem("Exit");

    private JMenu mView = new JMenu("View");
    private JMenuItem miViewModeling = new JMenuItem("Modeling");
    private JMenuItem miViewObstacles = new JMenuItem("Obstacles");

    private MapImagePanel imagePanel = null;
    private JScrollPane scrollPane = null;

    private JPopupMenu pmenu = new JPopupMenu();
    private JMenu mFloors = new JMenu("Floors...");
    private JMenuItem miAddFloor = new JMenuItem("Add Floor...");
    private JMenu mSwitchToFloor = new JMenu("Display Floor...");
    private JMenuItem miRemoveFloor = new JMenuItem("Remove Current Floor");

    private JMenuItem miAddRoom = new JMenuItem("Add Room");
    private JMenuItem miAddLink = new JMenuItem("Add Link");
    private JMenuItem miAddExit = new JMenuItem("Add Exit");
    private JMenuItem miAddStaircase = new JMenuItem("Add Staircase");
    private JMenuItem miAddFire = new JMenuItem("Add Fire");
    private JMenuItem miEditCrowd = new JMenuItem("Edit Crowd...");
    private JMenuItem miEditStaircaseGroup = new JMenuItem("Edit Staircase Group...");
    private JMenuItem miRemoveSelection = new JMenuItem("Remove Selected Objects");
    private Set<JMenuItem> floorItems = null;

    //	private Integer buttonPressed = null;
    private Integer buttonReleased = null;
    private Integer x1 = null;
    private Integer y1 = null;
    private Integer x2 = null;
    private Integer y2 = null;
//	private boolean isMoving = false;

    private Document current = null;

    private Integer hoveringOverRoomId = null;
    private Integer hoveringOverStaircaseId = null;

    public Main() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        imagePanel = new MapImagePanel();
        imagePanel.addMouseListener(this);
        imagePanel.addMouseMotionListener(this);

        this.createMainMenu();
        this.createContextMenu();
        this.update();

        this.getContentPane().setLayout(new BorderLayout());

        scrollPane = new JScrollPane(imagePanel);

        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        this.setSize(800, 600);
    }

    private void update() {
        if (current != null) {
            this.setTitle(current.name() + " - " + APPLICATION_TITLE);

            miSave.setEnabled(true);
            miExport.setEnabled(true);
            miClose.setEnabled(true);
            mView.setEnabled(true);

            BufferedImage image = current.image();

            imagePanel.setDocument(current);
            if (image != null) {
                imagePanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            }
            imagePanel.revalidate();
        } else {
            this.setTitle(APPLICATION_TITLE);
            miSave.setEnabled(false);
            miExport.setEnabled(false);
            miClose.setEnabled(false);
            mView.setEnabled(false);

            imagePanel.setDocument(null);
            imagePanel.setPreferredSize(new Dimension(0, 0));
            imagePanel.revalidate();
            imagePanel.repaint();
        }
    }

    private void createMainMenu() {
        //main menu
        menubar.removeAll();
        menubar.add(mFile);
        menubar.add(mView);

        mFile.add(miNew);
        mFile.add(miOpen);
        mFile.add(miSave);
        mFile.add(miExport);
        mFile.add(miClose);
        mFile.add(miExit);

        mView.add(miViewModeling);
        mView.add(miViewObstacles);

        miNew.addActionListener(this);
        miOpen.addActionListener(this);
        miSave.addActionListener(this);
        miExport.addActionListener(this);
        miClose.addActionListener(this);
        miExit.addActionListener(this);
        miViewModeling.addActionListener(this);
        miViewObstacles.addActionListener(this);

        this.setJMenuBar(menubar);
    }

    private void createContextMenu() {
        //context menu
        pmenu.removeAll();

        mFloors.add(miAddFloor);
        mFloors.add(mSwitchToFloor);
        mFloors.add(miRemoveFloor);

        pmenu.add(mFloors);
        pmenu.add(miAddRoom);
        pmenu.add(miAddLink);
        pmenu.add(miAddExit);
        pmenu.add(miAddStaircase);
        pmenu.add(miAddFire);
        pmenu.add(miEditCrowd);
        pmenu.add(miEditStaircaseGroup);
        pmenu.add(miRemoveSelection);

        miAddRoom.addActionListener(this);
        miAddLink.addActionListener(this);
        miAddExit.addActionListener(this);
        miAddStaircase.addActionListener(this);
        miAddFire.addActionListener(this);
        miEditCrowd.addActionListener(this);
        miEditStaircaseGroup.addActionListener(this);
        miRemoveSelection.addActionListener(this);

        miAddFloor.addActionListener(this);
        miRemoveFloor.addActionListener(this);

//		pmenu.addMouseListener(this);
    }

    private void showPopupMenu(MouseEvent event) {
        //is it a point click?
        boolean isPointClick = x1.equals(x2) && y1.equals(y2);

        miEditCrowd.setEnabled(false);
        miEditStaircaseGroup.setEnabled(false);

        if (isPointClick) {
            miAddRoom.setEnabled(false);
            miAddLink.setEnabled(false);
            miAddExit.setEnabled(false);
            miAddStaircase.setEnabled(false);
            miAddFire.setEnabled(false);

            if (this.hoveringOverRoomId != null) miEditCrowd.setEnabled(true);
            if (this.hoveringOverStaircaseId != null) miEditStaircaseGroup.setEnabled(true);
        } else {
            miAddRoom.setEnabled(true);
            miAddLink.setEnabled(true);
            miAddExit.setEnabled(true);
            miAddStaircase.setEnabled(true);
            miAddFire.setEnabled(true);
        }

        miRemoveSelection.setEnabled(false);

        //is there any selection?
        if (current.selection() != null) {
            miRemoveSelection.setEnabled(true);
            miAddRoom.setEnabled(false);
            miAddLink.setEnabled(false);
            miAddExit.setEnabled(false);
            miAddStaircase.setEnabled(false);
            miAddFire.setEnabled(false);
        }

        //update the floors
        mSwitchToFloor.removeAll();
        floorItems = null;
        if (current != null) {
            if (current.numberOfFloors() > 0) {
                floorItems = new HashSet<JMenuItem>();
                for (int i = 0; i < current.numberOfFloors(); ++i) {
                    JMenuItem item = new JMenuItem("Floor #" + i);
                    item.addActionListener(this);
                    mSwitchToFloor.add(item);


                    floorItems.add(item);
                }

                miRemoveFloor.setEnabled(true);
                mSwitchToFloor.setEnabled(true);
            } else {
                miRemoveFloor.setEnabled(false);
                mSwitchToFloor.setEnabled(false);
            }
        }

        pmenu.show(event.getComponent(), x2, y2);
    }

    private boolean handleUnsavedChanges() {
        Object[] options = {"Yes", "No", "Cancel"};

        int result = JOptionPane.showOptionDialog(this,
                "'" + current.name() + "' has been modified. Save changes?",
                "Save Document",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        if (result == 0) {
            //yes
            this.tryToSave();
            return true;
        } else if (result == 1) {
            //no
            return true;
        } else {
            //cancel
            return false;
        }
    }

    private void tryToSave() {
        try {
            assert (current != null);
            current.save();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "ERROR: the document could not be saved!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean tryToClose() {
        assert (current != null);

        if (current.hasUnsavedChanges()) {
            if (!this.handleUnsavedChanges()) {
                return false;
            }
        }

        current = null;
        this.update();

        return true;
    }

    private void tryToCreateNew() {
        //check whether a document is already open and, if so, try to close it
        if (current != null) {
            if (!this.tryToClose()) return;
        }

        //enter a name for this scenario
        String name = (String) JOptionPane.showInputDialog(
                this,
                "Name of the scenario",
                "Next",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (name == null) return;

        //enter the scale of this scenario
        String scale = (String) JOptionPane.showInputDialog(
                this,
                "Scale of the scenario",
                "Create New Document",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (scale == null) return;

        //select a directory name
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select scenario file destination");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showDialog(this, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File dir = chooser.getSelectedFile();

                String path = dir.getAbsolutePath();
                String filename = path + File.separator + (name.replaceAll(" ", "_")) + ".xml";

                current = new Document(filename, name, path, Double.parseDouble(scale));
                current.save();

                this.update();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ERROR: the document could not be saved!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void tryToOpen() {
        //check whether a document is already open and, if so, try to close it
        if (current != null) {
            if (!this.tryToClose()) return;
        }

        //open an existing file...
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select scenario file");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                current = new Document(file);
                this.update();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "ERROR: the file '" + file.getName() + "' could not be opened!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public void invalidateImagePanel() {
        imagePanel.repaint();
    }

    public void unsetMouseSelection() {
        imagePanel.unsetSelection();
        imagePanel.repaint();
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == miNew) {
            this.tryToCreateNew();
        } else if (event.getSource() == miOpen) {
            this.tryToOpen();
        } else if (event.getSource() == miSave) {
            this.tryToSave();
        } else if (event.getSource() == miExport) {
            assert (current != null);
        } else if (event.getSource() == miClose) {
            assert (current != null);

            this.tryToClose();
        } else if (event.getSource() == miExit) {
            if (current != null && current.hasUnsavedChanges()) {
                if (this.handleUnsavedChanges()) {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        } else if (event.getSource() == miViewModeling) {
        } else if (event.getSource() == miViewObstacles) {
        } else if (event.getSource() == miAddRoom) {
            current.addRoom(x1, y1, x2, y2);
            this.invalidateImagePanel();
        } else if (event.getSource() == miAddLink) {
            current.addLink(x1, y1, x2, y2);
            this.invalidateImagePanel();
        } else if (event.getSource() == miAddExit) {
            current.addExit(x1, y1, x2, y2);
            this.invalidateImagePanel();
        } else if (event.getSource() == miAddStaircase) {
            current.addStaircase(x1, y1, x2, y2);
            this.invalidateImagePanel();
        } else if (event.getSource() == miAddFire) {
            current.addFire(x1, y1, x2, y2);
            this.invalidateImagePanel();
        } else if (event.getSource() == miEditStaircaseGroup) {
            ObjectPanelStaircaseGroup dialog = new ObjectPanelStaircaseGroup(current);
            dialog.setVisible(true);
        } else if (event.getSource() == miEditCrowd) {
            if (hoveringOverRoomId != null) {
                ObjectPanelCrowd dialog = new ObjectPanelCrowd(current, hoveringOverRoomId);
                dialog.setVisible(true);
            }
        } else if (event.getSource() == miRemoveSelection) {
            current.removeSelection();
            this.invalidateImagePanel();
        } else if (event.getSource() == miAddFloor) {
            //open an existing file...
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select floor plan file");

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    current.addFloor(file.getAbsolutePath());

                    this.update();

                    this.invalidateImagePanel();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "ERROR: the file '" + file.getName() + "' could not be opened!", "An Error Occured", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }

        } else if (event.getSource() == miRemoveFloor) {
            if (current != null) {
                current.removeCurrentFloor();

                this.update();
                this.invalidateImagePanel();
            }
        } else if (floorItems != null && floorItems.contains(event.getSource())) {
            if (current != null) {
                JMenuItem item = (JMenuItem) event.getSource();
                String t1 = item.getText();
                String t2 = t1.substring(t1.indexOf("#") + 1);
                int idx = Integer.parseInt(t2);

                current.selectFloor(idx);
                this.invalidateImagePanel();
            }
        }
    }


    public void mouseClicked(MouseEvent event) {
    }


    public void mouseEntered(MouseEvent event) {
    }


    public void mouseExited(MouseEvent event) {
    }


    public void mousePressed(MouseEvent event) {
        if (current != null) {
            x1 = event.getX();
            y1 = event.getY();
            x2 = x1;
            y2 = y1;
//			buttonPressed = event.getButton();
            buttonReleased = null;
//
//			//left button?
//			if(buttonPressed == 1) {
//				//is there already a selection of objects?
////				if(current.selection() != null) {
////					//mark this point as the reference point for moving...
////					current.setMovingReferencePoint(x1, y1);
////				}
//			}
//			//right button?
//			else if(buttonPressed == 3) {
//				
//			}
        }
    }


    public void mouseReleased(MouseEvent event) {
        if (current != null) {
            x2 = event.getX();
            y2 = event.getY();
//			buttonPressed = null;
            buttonReleased = event.getButton();

            //left button?
            if (buttonReleased == 1) {
                //anything being moved around?
//				if(isMoving) {
//					isMoving = false;
//				}
//				else {
                //make a new selection
                current.selectObjects(x1, y1, x2, y2);
//				}
            }
            //right button?
            if (buttonReleased == 3) {
                this.showPopupMenu(event);
            }

        }

        this.unsetMouseSelection();
    }


    public void mouseDragged(MouseEvent event) {
        if (current != null) {
            x2 = event.getX();
            y2 = event.getY();

            //no, draw the selection frame...
            imagePanel.setSelection(x1, y1, x2, y2);
            this.invalidateImagePanel();
        }
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        if (current != null) {
            x1 = event.getX();
            y1 = event.getY();

            hoveringOverRoomId = current.isHoveringOverRoom(x1, y1);
            hoveringOverStaircaseId = current.isHoveringOverStaircase(x1, y1);

            this.invalidateImagePanel();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.setVisible(true);
    }
}
