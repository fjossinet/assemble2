package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.utils.IoUtils;
import fr.unistra.ibmc.assemble2.utils.RessourcesUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.jar.Manifest;

public class SplashScreen extends JFrame implements MouseListener {

    JLabel label;
    JProgressBar progressBar;
    int zipCount;

    public SplashScreen(boolean startAssemble) {
        this.repaint();
        this.setUndecorated(true);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon(RessourcesUtils.getImage("logo4.png"));
        this.add(new JLabel(icon),BorderLayout.NORTH);
        this.progressBar = new JProgressBar();
        String release = IoUtils.getAssemble2Release();
        this.label = new JLabel(release);
        this.label.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.add(this.label);
        labelPanel.add(this.progressBar);
        this.add(labelPanel, BorderLayout.SOUTH);

        this.pack();
        IoUtils.centerOnScreen(this);
        this.setVisible(true);
        this.toFront();
        if (startAssemble)
            new org.jdesktop.swingworker.SwingWorker() {
                protected Object doInBackground() {
                    SplashScreen.this.progressBar.setValue(0);
                    SplashScreen.this.progressBar.setMaximum(1);
                    new Assemble(SplashScreen.this);
                    return null;

                }
            }.execute();
        this.addMouseListener(this);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLabel() {
        return label;
    }

    public void setMessage(String message) {
        this.label.setText(message);
        if (message.startsWith("Extracting"))
            this.progressBar.setValue(++zipCount);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        this.dispose();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }
}
