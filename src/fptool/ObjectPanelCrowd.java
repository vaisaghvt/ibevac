package fptool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ibevac.datatypes.CCrowd;

public class ObjectPanelCrowd extends JDialog implements ActionListener, ChangeListener {
    private static final long serialVersionUID = 1L;

    private Document document = null;
    private CCrowd crowd = null;

    private JPanel centerPanel = new JPanel();
    private JPanel sizePanel = new JPanel();
    private JPanel knowledgePanel = new JPanel();

    private JLabel labelSize = new JLabel("Size:");
    private JLabel labelKnowledge = new JLabel("Knowledge:");

    private JTextField textSize = new JTextField();
    private JTextField textKnowledge = new JTextField();

    private JSlider sliderSize = new JSlider();
    private JSlider sliderKnowledge = new JSlider();

    private JPanel buttonPanel = new JPanel();
    private JButton bOk = new JButton("Ok");
    private JButton bCancel = new JButton("Cancel");


    public ObjectPanelCrowd(Document document, int roomId) {
        this.document = document;
        this.crowd = document.getCrowdForRoomId(roomId);

        this.setTitle("Crowd Properties");
        this.setResizable(false);
        this.setSize(400, 120);

        this.initialiseButtonPanel();
        this.initialiseCenterPanel();

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(centerPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == bOk) {
            this.document.updateCrowd(crowd);

            this.setVisible(false);
            this.dispose();
        } else if (event.getSource() == bCancel) {
            this.setVisible(false);
            this.dispose();
        }
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == sliderSize) {
            int value = sliderSize.getValue();
            this.updateSize(value);
        } else if (event.getSource() == sliderKnowledge) {
            int value = sliderKnowledge.getValue();
            this.updateKnowledge(value);
        }
    }

    private void updateSize(int n) {
        textSize.setText(n + " agents");
        crowd.setSize(n);
    }

    private void updateKnowledge(int p) {
        textKnowledge.setText(p + " %");
        crowd.setKnowledge(p / 100.0);
    }

    private void initialiseCenterPanel() {
        labelSize.setPreferredSize(new Dimension(95, 20));
        labelKnowledge.setPreferredSize(new Dimension(95, 20));

        textSize.setEditable(false);
        textSize.setPreferredSize(new Dimension(75, 20));

        textKnowledge.setEditable(false);
        textKnowledge.setPreferredSize(new Dimension(75, 20));

        sliderSize.setMinimum(0);
        sliderSize.setMaximum(100);
        sliderSize.setValue(crowd.getSize());
        sliderSize.addChangeListener(this);
        this.updateSize(crowd.getSize());

        sliderKnowledge.setMinimum(0);
        sliderKnowledge.setMaximum(100);
        sliderKnowledge.setValue((int) (crowd.getKnowledge() * 100));
        sliderKnowledge.addChangeListener(this);
        this.updateKnowledge((int) (crowd.getKnowledge() * 100));

        sizePanel.setLayout(new FlowLayout());
        sizePanel.add(labelSize);
        sizePanel.add(textSize);
        sizePanel.add(sliderSize);

        knowledgePanel.setLayout(new FlowLayout());
        knowledgePanel.add(labelKnowledge);
        knowledgePanel.add(textKnowledge);
        knowledgePanel.add(sliderKnowledge);


        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(sizePanel, BorderLayout.NORTH);
        centerPanel.add(knowledgePanel, BorderLayout.SOUTH);
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
