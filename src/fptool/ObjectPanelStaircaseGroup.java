package fptool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import ibevac.datatypes.CFloor;
import ibevac.datatypes.CStaircase;
import ibevac.datatypes.CStaircaseGroup;

public class ObjectPanelStaircaseGroup extends JDialog implements ActionListener {
    private static final long serialVersionUID = 1L;

    private class StaircaseModel implements TableModel {
        private HashSet<TableModelListener> listener = new HashSet<TableModelListener>();

        private ArrayList<CStaircase> staircases = new ArrayList<CStaircase>();
        private HashMap<CStaircase, Integer> floorMapping = new HashMap<CStaircase, Integer>();
        private HashMap<CStaircase, String> mapping1 = new HashMap<CStaircase, String>();
        private Map<String, Set<CStaircase>> mapping2 = new HashMap<String, Set<CStaircase>>();

        public StaircaseModel(Document document) {
            HashMap<Integer, CStaircase> idmapping = new HashMap<Integer, CStaircase>();
            for (int i = 0; i < document.numberOfFloors(); ++i) {
                CFloor floor = document.getFloors(i);

                for (CStaircase staircase : floor.getStaircases()) {
                    staircases.add(staircase);
                    floorMapping.put(staircase, i);
                    idmapping.put(staircase.getId(), staircase);
                }
            }

            Map<String, CStaircaseGroup> groups = document.getStaircaseGroups();
            for (String groupName : groups.keySet()) {
                CStaircaseGroup group = groups.get(groupName);

                HashSet<CStaircase> set = new HashSet<CStaircase>();
                mapping2.put(groupName, set);

                for (int id : group.getStaircaseIds()) {
                    CStaircase staircase = idmapping.get(id);
                    mapping1.put(staircase, groupName);
                    set.add(staircase);
                }
            }
        }

        public HashSet<String> groupNames() {
            return new HashSet<String>(mapping2.keySet());
        }

        public void addTableModelListener(TableModelListener listener) {
            this.listener.add(listener);
        }

        public void removeTableModelListener(TableModelListener listener) {
            this.listener.remove(listener);
        }

        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 0:
                    return Integer.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
            }
            return null;
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "Id";
                case 1:
                    return "Floor";
                case 2:
                    return "Area";
                case 3:
                    return "Group";
            }
            return null;
        }

        public int getRowCount() {
            return staircases.size();
        }

        public Object getValueAt(int row, int col) {
            CStaircase staircase = staircases.get(row);

            int mnx = Math.min(staircase.getCorner0().getX(), staircase.getCorner1().getX());
            int mny = Math.min(staircase.getCorner0().getY(), staircase.getCorner1().getY());
            int mxx = Math.max(staircase.getCorner0().getX(), staircase.getCorner1().getX());
            int mxy = Math.max(staircase.getCorner0().getY(), staircase.getCorner1().getY());


            switch (col) {
                case 0:
                    return staircase.getId();
                case 1:
                    return floorMapping.get(staircase);
                case 2:
                    return "(" + mnx + "," + mny + ")-(" + mxx + "," + mxy + ")";
                case 3:
                    return mapping1.containsKey(staircase) ? mapping1.get(staircase) : "(none)";
            }
            return null;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 3) {
                CStaircase staircase = staircases.get(row);
                String groupName = (String) value;

                String oldGroupName = mapping1.get(staircase);
                if (oldGroupName != null) {
                    mapping1.remove(staircase);

                    Set<CStaircase> group = mapping2.get(oldGroupName);
                    group.remove(staircase);

                    if (group.isEmpty()) {
                        mapping2.remove(oldGroupName);
                    }
                }

                if (!groupName.equals("(none)")) {
                    mapping1.put(staircase, groupName);

                    Set<CStaircase> group = mapping2.get(groupName);
                    if (group == null) {
                        group = new HashSet<CStaircase>();
                        mapping2.put(groupName, group);
                    }

                    group.add(staircase);
                }

                updateGroupNames();
            }
        }

        public boolean isCellEditable(int row, int col) {
            if (col == 3) return true;
            return false;
        }
    }

    private Document document = null;

    private StaircaseModel model = null;
    private JTable table = null;
    private JScrollPane scrollpane = null;
    private JComboBox cboxGroupNames = new JComboBox();

    private JPanel buttonPanel = new JPanel();
    private JButton bOk = new JButton("Ok");
    private JButton bCancel = new JButton("Cancel");


    public ObjectPanelStaircaseGroup(Document document) {
        this.document = document;

        this.setTitle("Staircase Group Settings");
        this.setResizable(false);
        this.setSize(400, 200);

        this.model = new StaircaseModel(document);
        this.table = new JTable(model);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(50);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(150);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(150);

        this.cboxGroupNames.setEditable(true);
        this.table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(cboxGroupNames));
        this.updateGroupNames();

        this.scrollpane = new JScrollPane(table);
        this.table.setFillsViewportHeight(true);

        this.initialiseButtonPanel();

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(scrollpane, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == bOk) {
            this.document.updateStaircaseGroups(model.mapping2);

            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }
    }

    private void updateGroupNames() {
        cboxGroupNames.removeAllItems();
        cboxGroupNames.addItem("(none)");

        for (String groupName : model.groupNames()) {
            cboxGroupNames.addItem(groupName);
        }
    }

    private void initialiseButtonPanel() {
        bOk.setPreferredSize(new Dimension(100, 20));
        bCancel.setPreferredSize(new Dimension(100, 20));

        bOk.addActionListener(this);
        bCancel.addActionListener(this);

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(bOk);
        buttonPanel.add(bCancel);
    }
}
