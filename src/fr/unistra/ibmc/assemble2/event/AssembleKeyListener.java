package fr.unistra.ibmc.assemble2.event;

import fr.unistra.ibmc.assemble2.gui.Mediator;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class AssembleKeyListener implements KeyListener {

    private Mediator mediator;

    public AssembleKeyListener(Mediator mediator) {
        this.mediator = mediator;
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
