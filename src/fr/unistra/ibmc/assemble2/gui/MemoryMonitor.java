package fr.unistra.ibmc.assemble2.gui;

import org.jdesktop.swingworker.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MemoryMonitor extends JPanel {

    private JLabel label;
    private Timer timer;

    public MemoryMonitor() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        this.label = new JLabel();
        add(label);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();

        long memoryUsed = totalMemory - freeMemory;

        label.setText((memoryUsed / (1024 * 1024)) + " of " + (maxMemory / (1024 * 1024)) + " MB used");

        Insets insets = getInsets();

        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        int x0 = insets.left;
        int x1 = insets.left + (int) (width * memoryUsed / maxMemory);
        int x2 = insets.left + (int) (width * totalMemory / maxMemory);
        int x3 = insets.left + width;

        int y0 = insets.top;
        int y1 = insets.top + height;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, height, new Color(0x0072B6)));
        g2d.fillRect(x0, y0, x1, y1);

        g2d.setBackground(Color.WHITE);
    }

    public Dimension getMaximumSize() {
        return new Dimension(150, 20);
    }

    public Dimension getMinimumSize() {
        return this.getMaximumSize();
    }

    public Dimension getPreferredSize() {
        return this.getMaximumSize();
    }

    public void start() {
        new UpdateMemoryMonitor().execute();
    }

    private class UpdateMemoryMonitor extends SwingWorker implements ActionListener {
        protected Object doInBackground() throws Exception {
            timer = new Timer(1000,this);
            timer.start();
            return null;
        }

        public void actionPerformed(ActionEvent e) {
            MemoryMonitor.this.repaint();
        }
    }

}

