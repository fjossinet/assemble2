package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.io.computations.Rnaplot;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.model.Button;
import org.noos.xing.mydoggy.ToolWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class FoldingLandscape extends JPanel implements CanvasInterface, java.awt.event.MouseMotionListener, MouseListener {

    private List<SecondaryStructure> allSecondaryStructures;
    private List<View2DFold> buttons;
    private GraphicContext gc;
    private java.awt.Image offScreenBuffer;
    private Mediator mediator;
    private double minX = 0, widest;
    private Map<Integer, Double> row2minY;
    private int ssPerRow = 11;
    private int translateX, translateY;
    private ToolWindow toolWindow;
    private Map<Integer, Integer> basePairingProbabilities;

    public FoldingLandscape(Mediator mediator) {
        this.allSecondaryStructures = new ArrayList<SecondaryStructure>();
        this.buttons = new ArrayList<View2DFold>();
        this.mediator = mediator;
        this.mediator.setFoldingLandscape(this);
        this.gc = new GraphicContext(this);
        this.setBackground(java.awt.Color.white);
        this.row2minY = new HashMap<Integer, Double>();
        this.row2minY.put(0, 0.0);
        this.basePairingProbabilities = new Hashtable<Integer, Integer>();
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
    }

    public void selectBaseBaseInteraction(BaseBaseInteraction bbi) {

    }

    public Mediator getMediator() {
        return this.mediator;
    }

    public float getBpProbability(int residuePos) {
        return this.basePairingProbabilities.containsKey(residuePos) ? (float)this.basePairingProbabilities.get(residuePos)/(float)this.allSecondaryStructures.size() : (float)0;
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public void addSecondaryStructure(SecondaryStructure ss) {
        if (ss.isPlotted())
            this.gc.initialize(ss);
        else {
            try {
                new Rnaplot(mediator).plot(ss);
                this.gc.initialize(ss);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i=1 ; i<= ss.getMolecule().size() ; i++)
            if (ss.getResidue(i).getSecondaryInteraction() != null || !ss.getResidue(i).getTertiaryInteractions().isEmpty())
                basePairingProbabilities.put(i, basePairingProbabilities.containsKey(i) ? basePairingProbabilities.get(i)+1 : 1 );

        this.allSecondaryStructures.add(ss);
        this.buttons.add(new View2DFold(mediator, ss));

        int row = (int)this.allSecondaryStructures.indexOf(ss)/ssPerRow;

        if (this.allSecondaryStructures.size() == 1 || this.allSecondaryStructures.indexOf(ss)%ssPerRow == 0)
            this.minX = 0; //its is a new row

        for (Residue r:ss.getResidues())
            r.setRealCoordinates(r.getX() + minX+20, r.getY() + this.row2minY.get(row)+20); //now we place the 2D at its final location

        this.minX = ss.getMaxX()+50; //for the next one

        if (this.minX > widest)
            widest = (int)this.minX;

        double maxY = ss.getMaxY()+50;
        if (!this.row2minY.containsKey(row+1))
            this.row2minY.put(row+1, maxY);
        else if (this.row2minY.containsKey(row+1) && maxY > this.row2minY.get(row+1))
            this.row2minY.put(row+1, maxY);

        this.setPreferredSize(new Dimension((int) widest, this.row2minY.get(row + 1).intValue()));
        this.doLayout();
        this.revalidate();
        this.repaint();
    }

    public GraphicContext getGraphicContext() {
        return this.gc;
    }

    public List<SecondaryStructure> getAllSecondaryStructures() {
        return allSecondaryStructures;
    }

    public void clear() {
        this.widest = 0;
        this.allSecondaryStructures.clear();
        this.buttons.clear();
        this.gc = new GraphicContext(this);
        this.row2minY = new HashMap<Integer, Double>();
        this.row2minY.put(0, 0.0);
        this.basePairingProbabilities = new Hashtable<Integer, Integer>();
        this.repaint();
    }

    public void paintComponent(final java.awt.Graphics g) {
        super.paintComponent(g);
        final java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING, java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setBackground(java.awt.Color.white);
        g2.setColor(Color.BLACK);
        this.gc.setDrawingArea(new Rectangle(0, 0, this.getWidth(), this
                .getHeight()));
        for (SecondaryStructure ss: new ArrayList<SecondaryStructure>(this.allSecondaryStructures)) {
            int row = (int)this.allSecondaryStructures.indexOf(ss)/ssPerRow;
            if (ss.isPlotted()) {
                ss.draw(g2, this.gc);
                this.buttons.get(this.allSecondaryStructures.indexOf(ss)).draw(g2, (float)ss.getMinX(), (float)(this.row2minY.get(row)-5), ss.getSource());
            }
        }
    }

    public void update(final java.awt.Graphics g) {
        final java.awt.Graphics2D gr;
        if (this.offScreenBuffer == null ||
                (!(offScreenBuffer.getWidth(this) == this.getSize().width
                        && offScreenBuffer.getHeight(this) == this.getSize().height))) {
            this.offScreenBuffer = this.createImage(this.getSize().width, this.getSize().height);
        }
        // We need to use our buffer Image as a Graphics object:
        gr = (java.awt.Graphics2D) this.offScreenBuffer.getGraphics();
        paintComponent(gr);
        g.drawImage(this.offScreenBuffer, 0, 0, this);
    }

    public void translateView(final double transX, final double transY) {
        this.gc.moveView(transX, transY);
        this.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        /*if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            final int transX = e.getX() - translateX;
            final int transY = e.getY() - translateY;
            this.translateView(transX, transY);
            translateX = e.getX();
            translateY = e.getY();
        }*/
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for (View2DFold b:new ArrayList<View2DFold>(this.buttons)) {
            int row = (int)this.buttons.indexOf(b)/ssPerRow;
            b.mouseMoved(e, (float)(this.allSecondaryStructures.get(this.buttons.indexOf(b)).getMinX()+this.gc.getViewX()-5), (float)(this.row2minY.get(row)+this.gc.getViewY())-5, this);
        }
    }

    @Override
    public void addButton(Button button) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public void setSelectedHelix(Helix h) {
    }

    @Override
    public void select(Residue residue) {
    }

    @Override
    public Helix getSelectedHelix() {
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (View2DFold b:new ArrayList<View2DFold>(this.buttons)) {
            int row = (int)this.buttons.indexOf(b)/ssPerRow;
            b.mouseClicked(e, (float)(this.allSecondaryStructures.get(this.buttons.indexOf(b)).getMinX()+this.gc.getViewX()-5), (float)(this.row2minY.get(row)+this.gc.getViewY())-5);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //translation
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            translateX = e.getX();
            translateY = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //translation
        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
            translateX = 0;
            translateY = 0;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void setBasePairingProbabilities(Map<Integer, Integer> basePairingProbabilities) {
        this.basePairingProbabilities = basePairingProbabilities;
    }

    public Map<Integer, Integer> getBasePairingProbabilities() {
        return basePairingProbabilities;
    }
}
