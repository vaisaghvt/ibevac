package fptool;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class MapImagePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private Document document = null;
    private boolean hasSelection = false;
    private Integer x1 = null;
    private Integer y1 = null;
    private Integer x2 = null;
    private Integer y2 = null;

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setSelection(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        hasSelection = true;
    }

    public void unsetSelection() {
        hasSelection = false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (document != null) {
            g.drawImage(document.image(), 0, 0, null);
        }

        if (hasSelection) {
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);

            g.setColor(Color.RED);
            g.drawRect(minX, minY, maxX - minX, maxY - minY);
            g.dispose();
        }
    }
}
