package fr.unistra.ibmc.assemble2.gui;

import com.mongodb.DB;
import com.mongodb.Mongo;
import fr.unistra.ibmc.assemble2.event.WebSocketClient;
import fr.unistra.ibmc.assemble2.io.drivers.ChimeraDriver;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.Assemble;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

public class Mediator {

    private Assemble assemble;
    private fr.unistra.ibmc.assemble2.model.TertiaryStructure ts; //to have a reference on the tertiary structure before to wait the construction of the Model3D object (for exemple to display the numbering system)
    private Rna2DViewer rna2DViewer;
    private SecondaryCanvas secondaryCanvas;
    private AlignmentCanvas alignmentCanvas;
    private SecondaryStructureNavigator secondaryStructureNavigator;
    private MyDoggyToolWindowManager myDoggyToolWindowManager;
    private TertiaryFragmentsPanel tertiaryFragmentsPanel;
    private ChimeraDriver chimeraDriver;
    private int horizontalStep = 25;
    private MoleculesList moleculesList;
    private GenomicAnnotationsPanel genomicAnnotationsPanel;
    private DB genomicMongo, pdbMongo;
    private Mongo mongo;
    private FoldingLandscape foldingLandscape;
    private MongoDBAlignments mongoDBAlignments;
    private WebSocketClient webSocketClient;

    public Mediator(Assemble assemble) {
        this.assemble = assemble;
    }

    public void loadRNASecondaryStructure(SecondaryStructure ss, boolean toWorkingSession, boolean toRna2DViewer) {
        if (ss != null) {
            if (toWorkingSession)
                this.foldingLandscape.addSecondaryStructure(new SecondaryStructure(ss));
            if (toRna2DViewer)
                this.secondaryCanvas.setSecondaryStructure(ss);
        }
    }

    public SecondaryStructure inferNew2DAnd3D(AlignedMolecule targetAlignedMolecule, int start, int end) {
        Molecule targetMolecule = targetAlignedMolecule.getMolecule();
        StructuralAlignment alignment = this.getAlignmentCanvas().getMainAlignment();
        AlignedMolecule referenceAlignedMolecule = alignment.getBiologicalReferenceSequence();
        SecondaryStructure newSecondaryStructure = alignment.deriveReferenceStructure(targetAlignedMolecule);
        /*int pos=0;
        if (newSecondaryStructure != null && this.getTertiaryStructure() != null) {
            Fragment fragment = new Fragment(this);
            TertiaryStructure referenceTertiaryStructure =  this.getTertiaryStructure();
            TertiaryStructure newTertiaryStructure = new TertiaryStructure(targetMolecule);
            Location targetSequence = new Location();
            List<Residue3D> referenceResidues3D = new ArrayList<Residue3D>();
            for (int i=start; i<= end; i++) {
                Symbol targetSymbol = targetAlignedMolecule.getSymbol(i),
                        referenceSymbol = referenceAlignedMolecule.getSymbol(i);
                if (!targetSymbol.isGap())
                    pos++;
                if (!targetSymbol.isGap() && !referenceSymbol.isGap()) {
                    Residue3D referenceResidue3D = referenceTertiaryStructure.getResidue3DAt(referenceSymbol.getPositionInSequence());
                    if (referenceResidue3D != null) {
                        referenceResidues3D.add(referenceResidue3D);
                        targetSequence.add(pos);
                    }
                }
                else if (!referenceResidues3D.isEmpty()) {
                    fragment.infer(targetMolecule, newTertiaryStructure, referenceResidues3D, targetSequence);
                    targetSequence = new Location();
                    referenceResidues3D = new ArrayList<Residue3D>();
                }
            }
            newSecondaryStructure.setLinkedTs(newTertiaryStructure);
        }*/
        return newSecondaryStructure;
    }

    public void setHorizontalStep(int step) {
        this.horizontalStep = step;
    }

    public int getHorizontalStep() {
        return horizontalStep;
    }

    public void setAlignmentCanvas(AlignmentCanvas alignmentCanvas) {
        this.alignmentCanvas = alignmentCanvas;
    }

    public AlignmentCanvas getAlignmentCanvas() {
        return alignmentCanvas;
    }

    public void setTertiaryStructure(TertiaryStructure ts) {
        this.ts = ts;
    }

    public SecondaryStructureNavigator getSecondaryStructureNavigator() {
        return secondaryStructureNavigator;
    }

    public void setRna2DViewer(Rna2DViewer rna2DViewer) {
        this.rna2DViewer = rna2DViewer;
    }

    public SecondaryStructure getSecondaryStructure() {
        return this.rna2DViewer.getSecondaryCanvas().getSecondaryStructure();
    }

    public TertiaryStructure getTertiaryStructure() {
        return this.ts;
    }

    public void setToolWindowManager(MyDoggyToolWindowManager myDoggyToolWindowManager) {
        this.myDoggyToolWindowManager = myDoggyToolWindowManager;
    }

    public MyDoggyToolWindowManager getToolWindowManager() {
        return myDoggyToolWindowManager;
    }

    public void setTertiaryFragmentsPanel(TertiaryFragmentsPanel tertiaryFragmentsPanel) {
        this.tertiaryFragmentsPanel = tertiaryFragmentsPanel;
    }

    public TertiaryFragmentsPanel getTertiaryFragmentsPanel() {
        return tertiaryFragmentsPanel;
    }

    public Rna2DViewer getRna2DViewer() {
        return this.rna2DViewer;
    }

    public SecondaryCanvas getSecondaryCanvas() {
        return this.rna2DViewer.getSecondaryCanvas();
    }

    public Assemble getAssemble() {
        return this.assemble;
    }

    SecondaryStructure getModel2D() {
        return this.secondaryCanvas.getSecondaryStructure();
    }

    public void setSecondaryStructureNavigator(SecondaryStructureNavigator secondaryStructureNavigator) {
        this.secondaryStructureNavigator = secondaryStructureNavigator;
    }

    public void setSecondaryCanvas(SecondaryCanvas secondaryCanvas) {
        this.secondaryCanvas = secondaryCanvas;
    }

    public void setChimeraDriver(ChimeraDriver chimeraDriver) {
        this.chimeraDriver = chimeraDriver;
    }

    public ChimeraDriver getChimeraDriver() {
        return this.chimeraDriver;
    }

    public void clearSession() {
        this.getSecondaryCanvas().getSecondaryStructureToolBar().setRenderingMode(this.getSecondaryCanvas().getSecondaryStructureToolBar().REFERENCE_STRUCTURE);
        this.secondaryStructureNavigator.clear();
        this.foldingLandscape.clear();
        this.moleculesList.clearList();
        this.alignmentCanvas.clear();
        this.secondaryCanvas.clear();
        this.ts = null;
        if (this.chimeraDriver != null)
            this.chimeraDriver.closeSession();
    }

    public void setMoleculesList(MoleculesList moleculesList) {
        this.moleculesList = moleculesList;
    }

    public MoleculesList getMoleculesList() {
        return moleculesList;
    }

    public void setGenomicAnnotationsPanel(GenomicAnnotationsPanel genomicAnnotationsPanel) {
        this.genomicAnnotationsPanel = genomicAnnotationsPanel;
    }

    public GenomicAnnotationsPanel getGenomicAnnotationsPanel() {
        return genomicAnnotationsPanel;
    }

    public void setMongo(Mongo mongo) {
        this.mongo =  mongo;
    }

    public Mongo getMongo() {
        return this.mongo;
    }

    public DB getPDBMongo() {
        return this.pdbMongo;
    }

    public void setPDBMongo(DB pdbMongo) {
        this.pdbMongo =  pdbMongo;
    }

    public void setGenomicMongo(DB genomicMongo) {
        this.genomicMongo =  genomicMongo;
    }

    public DB getGenomicMongo() {
        return this.genomicMongo;
    }

    public void setFoldingLandscape(FoldingLandscape foldingLandscape) {
        this.foldingLandscape = foldingLandscape;
    }

    public FoldingLandscape getFoldingLandscape() {
        return foldingLandscape;
    }

    public void setMongoDBAlignments(MongoDBAlignments mongoDBAlignments) {
        this.mongoDBAlignments = mongoDBAlignments;
    }

    public MongoDBAlignments getMongoDBAlignments() {
        return mongoDBAlignments;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}
