package fr.unistra.ibmc.assemble2.gui;


import fr.unistra.ibmc.assemble2.model.BaseBaseInteraction;
import fr.unistra.ibmc.assemble2.model.Helix;
import fr.unistra.ibmc.assemble2.model.Residue;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.utils.DrawingUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class GraphicContext {

    static final byte BASECOLOR = 0, STRUCTURECOLOR = 1, SEQUENCECOLOR = 2, INTERACTIONCOLOR = 3;
    private byte colorMode = STRUCTURECOLOR;
    private Font f;
    //the upperleft corner of the current view
    double viewX, viewY;
    double finalZoomLevel = 1;
    private static java.util.List<String> families2show ;
    private boolean showFamily, displayFiveToThreeOrientation = true, displayLabels = true, editStructure = false;
    private Point2D gravityPoint;
    private Rectangle drawingArea;
    private double width,
            height,
            half = Math.sqrt(width * width + height * height) / 2,
            symbolsize = 4* width - 2* half;
    private CanvasInterface canvas;
    static {
        families2show = new ArrayList<String>();
        families2show.add("CWW");
        families2show.add("TWW");
        families2show.add("CHH");
        families2show.add("THH");
        families2show.add("CSS");
        families2show.add("TSS");
        families2show.add("CHW");
        families2show.add("CWH");
        families2show.add("THW");
        families2show.add("TWH");
        families2show.add("CHS");
        families2show.add("CSH");
        families2show.add("THS");
        families2show.add("TSH");
        families2show.add("CSW");
        families2show.add("CWS");
        families2show.add("TSW");
        families2show.add("TWS");
        families2show.add("SingleHBond");
    }
    boolean displaySingleHBonds = true,
            displayQuantitativeValues = false,
            displayQualitativeValues = false,
            displayTertiaryInteractions = true,
            displayPositions = true,
            displayResidues = true;
    float maxQuantitativeValue = Float.MIN_VALUE, minQuantitativeValue = Float.MAX_VALUE;
    public Map<String, Integer> qualitativeNames;
    public Map<String, Color> qualitative2Colors;
    boolean drawInPNG = false,  displayConsensusMaskInSecondaryCanvas = false, displayBasePairingProbabilities = false;

    public Color startGradientColor = new Color(128, 192,0),
            endGradientColor = Color.RED,
            startBpsProbColor = new Color(255, 192, 0),
            endBpsProbColor = new Color(128, 0, 255),
            noQuantitativeValueColor = Color.BLACK,
            noQualitativeValueColor = Color.BLACK;

    private Rectangle2D motifArea;
    private boolean drawOnlySelectedMotif;
    private java.util.List<Residue> selectedResiduesFromMotif;
    private java.util.List<BaseBaseInteraction> selectedInteractionsFromMotif;

    GraphicContext() {}

    GraphicContext(CanvasInterface canvas) {
        this(6,9,canvas);
    }

    GraphicContext(final double width, final double height, final CanvasInterface canvas) {
        this.width = width;
        this.height = height;
        this.half = Math.sqrt(width * width + height * height) / 2;
        this.symbolsize = 4* width - 2* half;
        this.canvas = canvas;
        this.selectedResiduesFromMotif = new ArrayList<Residue>();
        this.selectedInteractionsFromMotif = new ArrayList<BaseBaseInteraction>();
        this.qualitativeNames = new Hashtable<String, Integer>();
        this.qualitative2Colors = new Hashtable<String, Color>();
    }

    public boolean isDrawOnlySelectedMotif() {
        return drawOnlySelectedMotif;
    }

    public void setDrawOnlySelectedMotif(boolean drawOnlySelectedMotif) {
        if (drawOnlySelectedMotif) {
            this.motifArea = null;
            this.selectedInteractionsFromMotif.clear();
            this.selectedResiduesFromMotif.clear();
        }
        this.drawOnlySelectedMotif = drawOnlySelectedMotif;
    }

    public Rectangle2D getMotifArea() {
        return this.motifArea;
    }

    public void addToSelectionArea(Rectangle2D area) {
        if (area != null) {
            if (this.motifArea == null)
                this.motifArea = area;
            else
                this.motifArea = this.motifArea.createUnion(area);
        }
    }

    public void addInteractionFromMotif(BaseBaseInteraction interaction ) {
        this.selectedInteractionsFromMotif.add(interaction);
    }

    public void addResidueFromMotif(Residue residue ) {
        this.selectedResiduesFromMotif.add(residue);
    }

    public java.util.List<Residue> getSelectedResiduesFromMotif() {
        return new ArrayList<Residue>(this.selectedResiduesFromMotif);
    }

    public java.util.List<BaseBaseInteraction> getSelectedInteractionsFromMotif() {
        return new ArrayList<BaseBaseInteraction>(this.selectedInteractionsFromMotif);
    }

    public boolean isDrawInPNG() {
        return drawInPNG;
    }

    public void setDrawInPNG(boolean drawInSVG) {
        this.drawInPNG = drawInSVG;
    }

    public boolean displaySingleHBonds() {
        return displaySingleHBonds;
    }

    public void addQualitative(String name) {
        if (this.qualitativeNames.containsKey(name))
            this.qualitativeNames.put(name, this.qualitativeNames.get(name)+1);
        else {
            this.qualitativeNames.put(name, 1);
            Random rand = new Random();
            this.qualitative2Colors.put(name, new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
        }
    }

    public Map<String, Integer> getQualitativeNames() {
        return qualitativeNames;
    }

    public void clearQualitativeNames() {
        this.qualitativeNames.clear();
    }

    public void setMaxQuantitativeValue(float maxQuantitativeValue) {
        this.maxQuantitativeValue = maxQuantitativeValue;
    }

    public float getMaxQuantitativeValue() {
        return maxQuantitativeValue;
    }

    public float getMinQuantitativeValue() {
        return minQuantitativeValue;
    }

    public void setMinQuantitativeValue(float minQuantitativeValue) {
        this.minQuantitativeValue = minQuantitativeValue;
    }

    public Color getQuantitativeValue2Color(float value) {
        if (value < 0)
            return noQuantitativeValueColor;

        float ratio = value/(this.maxQuantitativeValue-this.minQuantitativeValue);
        if (ratio > 1)
            ratio = 1;
        int red = (int) (startGradientColor.getRed() * (1 - ratio) + endGradientColor.getRed() * ratio);
        int green = (int) (startGradientColor.getGreen() * (1 - ratio) + endGradientColor.getGreen() * ratio);
        int blue = (int) (startGradientColor.getBlue() * (1 - ratio) + endGradientColor.getBlue() * ratio);
        int alpha  =(int) (startGradientColor.getAlpha() * (1 - ratio) + endGradientColor.getAlpha() * ratio);
        return new Color(red, green, blue, alpha);
    }

    public Color getBpProbabilityColor(float probability) {
        int red = (int) (startBpsProbColor.getRed() * (1 - probability) + endBpsProbColor.getRed() * probability);
        int green = (int) (startBpsProbColor.getGreen() * (1 - probability) + endBpsProbColor.getGreen() * probability);
        int blue = (int) (startBpsProbColor.getBlue() * (1 - probability) + endBpsProbColor.getBlue() * probability);
        int alpha  =(int) (startBpsProbColor.getAlpha() * (1 - probability) + endBpsProbColor.getAlpha() * probability);
        return new Color(red, green, blue, alpha);
    }


    public Color getStartGradientColor() {
        return startGradientColor;
    }

    public Color getEndGradientColor() {
        return endGradientColor;
    }

    public Map<String, Color> getQualitative2Colors() {
        return qualitative2Colors;
    }

    public void displayQuantitativeValues(boolean display) {
        this.displayQuantitativeValues = display;
        if (display) {
            this.displayQualitativeValues = false;
            this.displayConsensusMaskInSecondaryCanvas = false;
            this.displayBasePairingProbabilities = false;
        }
    }

    public void displayQualitativeValues(boolean display) {
        this.displayQualitativeValues = display;
        if (display) {
            this.displayQuantitativeValues = false;
            this.displayConsensusMaskInSecondaryCanvas = false;
            this.displayBasePairingProbabilities = false;
        }
    }

    public boolean displayConsensusMaskInSecondaryCanvas() {
        return this.displayConsensusMaskInSecondaryCanvas;
    }

    public void setDisplayTertiaryInteractions(boolean displayTertiaryInteractions) {
        this.displayTertiaryInteractions = displayTertiaryInteractions;
    }

    public void setDisplaySingleHBonds(boolean displaySingleHBonds) {
        this.displaySingleHBonds = displaySingleHBonds;
    }

    public boolean isDisplayPositions() {
        return displayPositions;
    }

    public void setDisplayPositions(boolean displayPositions) {
        this.displayPositions = displayPositions;
    }

    public boolean isDisplayResidues() {
        return displayResidues;
    }

    public void setDisplayResidues(boolean displayResidues) {
        this.displayResidues = displayResidues;
    }

    public CanvasInterface getCanvas() {
        return canvas;
    }

    public boolean isEditStructure() {
        return editStructure;
    }

    public void setEditStructure(boolean editStructure) {
        this.editStructure = editStructure;
    }

    public void initialize(SecondaryStructure ss) {
        for (Helix h2D:ss.getHelices())
            this.initializeHelixCurrentContext(h2D);
    }

    public void initializeHelixCurrentContext(Helix h2D) {
        Residue[] _5PrimEnds = h2D.get5PrimeEnds();
        Residue _5PrimeEnd1 = _5PrimEnds[0], _5PrimeEnd2 = _5PrimEnds[1];
        double  length = DrawingUtils.getDistance(_5PrimeEnd1.getRealCoordinates(), h2D.get3PrimeEnd(_5PrimeEnd1).getRealCoordinates());
        Point2D[]   _5 = DrawingUtils.fit(_5PrimeEnd1.getRealCoordinates(),h2D.get3PrimeEnd(_5PrimeEnd1).getRealCoordinates(), (length-this.height *(h2D.getLength()-1))/2),
                _3 = DrawingUtils.fit(_5PrimeEnd2.getRealCoordinates(),h2D.get3PrimeEnd(_5PrimeEnd2).getRealCoordinates(), (length-this.height *(h2D.getLength()-1))/2);
        h2D.setEnds(_5PrimeEnd1,_5[0], h2D.get3PrimeEnd(_5PrimeEnd1), _5[1], _5PrimeEnd2, _3[0], h2D.get3PrimeEnd(_5PrimeEnd2), _3[1],this);
        double _width = DrawingUtils.getDistance(_5PrimeEnd1.getRealCoordinates(),h2D.getPairedResidue(_5PrimeEnd1).getRealCoordinates());
        _5 = DrawingUtils.fit(_5PrimeEnd1.getRealCoordinates(),h2D.getPairedResidue(_5PrimeEnd1).getRealCoordinates(), (_width-this.width *4)/2);
        _3 = DrawingUtils.fit(_5PrimeEnd2.getRealCoordinates(),h2D.getPairedResidue(_5PrimeEnd2).getRealCoordinates(), (_width-this.width *4)/2);
        h2D.setEnds(_5PrimeEnd1, _5[0], h2D.getPairedResidue(_5PrimeEnd1), _5[1], _5PrimeEnd2, _3[0], h2D.getPairedResidue(_5PrimeEnd2), _3[1],this);
    }

    void moveView(final double transX, final double transY) {
        this.viewX += transX;
        this.viewY += transY;
    }

    void zoomParameters(final double zoomFactor) {
        this.finalZoomLevel *= zoomFactor;
    }

    public double getCurrentWidth() {
        return width * this.finalZoomLevel;
    }

    public double getRealWidth() {
        return width;
    }

    public double getCurrentHeight() {
        return height * this.finalZoomLevel;
    }

    public double getRealHeight() {
        return height;
    }

    public double getCurrentSymbolSize() {
        return symbolsize * this.finalZoomLevel;
    }

    public double getRealSymbolSize() {
        return symbolsize;
    }

    public double getCurrentHalf() {
        return half * this.finalZoomLevel;
    }

    public double getRealHalf() {
        return half;
    }

    public Font getFont() {
        return new Font(Font.MONOSPACED, Font.PLAIN,  (int) this.getCurrentHeight());
    }

    public double getViewX() {
        return viewX;
    }

    public double getViewY() {
        return viewY;
    }

    Point2D getGravityPoint() {
        return gravityPoint;
    }

    public double getFinalZoomLevel() {
        return finalZoomLevel;
    }

    public static boolean containFamily(final String family) {
        for (String f:families2show)
            if (f.equals(family))
                return true;
        return false;
    }

    public static void addFamily(final String family) {
        if (containFamily(family))
            families2show.remove(family);
        else
            families2show.add(family);
    }

    boolean isShowFamily() {
        return this.showFamily;
    }

    void setShowFamily(final boolean showFamily) {
        this.showFamily = showFamily;
    }

    void setGravityPoint(final Point2D gravityPoint) {
        this.gravityPoint = gravityPoint;
    }

    void translateGravityPoint(final double transX, final double transY) {
        if (this.gravityPoint != null)
            this.gravityPoint.setLocation(this.gravityPoint.getX() + transX, this.gravityPoint.getY() + transY);
    }

    void zoomGravityPoint(final double zoomLevel) {
        if (this.gravityPoint != null)
            this.gravityPoint.setLocation(this.gravityPoint.getX() * zoomLevel, this.gravityPoint.getY() * zoomLevel);
    }

    void setColorMode(final byte colorMode) {
        this.colorMode = colorMode;
    }

    void setDrawingArea(final Rectangle drawingArea) {
        this.drawingArea = drawingArea;
    }

    public Rectangle getDrawingArea() {
        return drawingArea;
    }

    public boolean isLabelsDisplayed() {
        return displayLabels;
    }

    public void setDisplayLabels(boolean displayLabels) {
        this.displayLabels = displayLabels;
    }

    public boolean isTertiaryInteractionsDisplayed() {
        return displayTertiaryInteractions;
    }

    //for the alignment display

    int horizontalAdvance, verticalAdvance, letterHeight, letterWidth;
    public int currentX, currentY, currentWidth;
    boolean quickDraw;
    int ratio = 10;
    private Font alignmentFont = new java.awt.Font("Courier", java.awt.Font.PLAIN, 20);


    public int getRatio() {
        return ratio;
    }

    void setRatio(final int ratio) {
        this.ratio = ratio;
        if (this.ratio < 10)
            this.ratio = 10;
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    void setCurrentX(final int currentX) {
        this.currentX = currentX;
    }

    void setCurrentY(final int currentY) {
        this.currentY = currentY;
    }

    public int getHorizontalAdvance() {
        return horizontalAdvance;
    }

    public int getVerticalAdvance() {
        return verticalAdvance;
    }

    public void setHorizontalAdvance(final int horizontalAdvance) {
        this.horizontalAdvance = horizontalAdvance;
    }

    public void setVerticalAdvance(final int verticalAdvance) {
        this.verticalAdvance = verticalAdvance;
    }

    public int getLetterHeight() {
        return letterHeight;
    }

    public void setLetterHeight(final int letterHeight) {
        this.letterHeight = letterHeight;
    }

    public int getLetterWidth() {
        return letterWidth;
    }

    public void setLetterWidth(final int width) {
        this.letterWidth = width;
    }

    boolean isQuickDraw() {
        return quickDraw;
    }

    void setQuickDraw(final boolean quickDraw) {
        this.quickDraw = quickDraw;
    }

    public void setAlignmentFont(final Font f) {
        this.alignmentFont = f;
    }

    public Font getAlignmentFont() {
        return this.alignmentFont;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}