package fr.unistra.ibmc.assemble2;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.sun.javafx.application.PlatformImpl;
import fr.unistra.ibmc.assemble2.event.AssembleKeyListener;
import fr.unistra.ibmc.assemble2.event.SelectionTransmitter;
import fr.unistra.ibmc.assemble2.event.WebSocketClient;
import fr.unistra.ibmc.assemble2.gui.*;
import fr.unistra.ibmc.assemble2.gui.components.*;
import fr.unistra.ibmc.assemble2.gui.icons.*;
import fr.unistra.ibmc.assemble2.io.AssembleProject;
import fr.unistra.ibmc.assemble2.io.FileParser;
import fr.unistra.ibmc.assemble2.io.computations.Rnaview;
import fr.unistra.ibmc.assemble2.io.drivers.ChimeraDriver;
import fr.unistra.ibmc.assemble2.model.*;
import fr.unistra.ibmc.assemble2.model.Shape;
import fr.unistra.ibmc.assemble2.utils.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.*;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bson.types.ObjectId;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.jdesktop.application.Application;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.noos.xing.mydoggy.*;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLLIElement;
import org.w3c.dom.html.HTMLTextAreaElement;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileView;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.regex.Pattern;


public class Assemble extends Application implements SelectionTransmitter {

    private static GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

    public static boolean HELP_MODE;

    private static File lastFilePath = Assemble.getUserDir();
    public static Color A_Color = new Color(128,128,0);
    public static Color U_Color = new Color(128,128,128);
    public static Color G_Color = new Color(192,0,0);
    public static Color C_Color = new Color(255,128,0);
    public static Color SecondaryInteraction_Color = new Color(192,128,0);
    public static Color TertiaryInteraction_Color = new Color(192,128,0);
    public static final int MORE = 500;

    public static boolean USE_ABSOLUTE_NUMBERING_SYSTEM = true, DRAW_HELICES_NAMES;
    private static Map<String,List<RNAMotifIcon>> motifsIcons = new Hashtable<String,List<RNAMotifIcon>>();
    private ToolWindow mongoDBPanelWindow, alignmentPanelWindow, genomicAnnotationsWindow;

    private MyRNAMotifsPanel myRNAMotifsPanel;
    private SecondaryStructureNavigator secondaryStructureNavigator;
    public MessageBar messageBar;
    protected Window window;
    private fr.unistra.ibmc.assemble2.gui.SplashScreen splashScreen;
    private AssembleProject currentAssembleProject;
    private String lastDatabaseName = "", lastOrganismName = "Saccharomyces cerevisiae";

    private static int motifsIconSize = 200;

    private JMenu loadRecentFiles, alignmentMenu, genomicAnnotations;

    private FileMenu fileMenu;
    private ExternalResourcesMenu externalResourcesMenu;
    private TutorialsMenu tutorialsMenu;
    private JMenu configure;

    private Mediator mediator;
    public static int NUMBERING_FREQUENCY = 5;
    private JMenu qualitativeColorMenu, tertiaryMenu;
    private JMenuItem configureAssemble;

    private NDBWebsite ndbWebsite;
    private RNACentralWebsite rnaCentralWebsite;
    private MfoldWebServer mfoldWebServer;
    private Tutorial2Website tutorial2Website;

    public static File getTertiaryDataDirectory() throws IOException {
        File tertiaryDataDir = new File(getUserDir(),"tertiary_data");
        if (!tertiaryDataDir.exists() || tertiaryDataDir.listFiles().length == 0) {
            tertiaryDataDir.mkdir();
            File destFile = new File(tertiaryDataDir, "tertiary_data.zip");
            URL inputUrl = Assemble.class.getResource("/fr/unistra/ibmc/assemble2/utils/data/tertiary_data.zip");
            FileUtils.copyURLToFile(inputUrl, destFile);
            File dataZipped = new File(getInstallPath(),"tertiary_data.zip");
            try {
                IoUtils.extractArchivedFile(tertiaryDataDir.getAbsolutePath(),dataZipped, null);
                dataZipped.delete();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
        return tertiaryDataDir;
    }

    public static File getUserDir() {
        File f = new File(new StringBuffer(System.getProperty("user.home")).append(System.getProperty("file.separator")).append(".assemble2").toString());
        if (!f.exists()) {
            f.mkdir();
            new File(f, "tmp").mkdir();
        }
        return f;
    }

    public static String getInstallPath() {
        //the decode function to remove the %20 characters for spaces
        return java.net.URLDecoder.decode(Assemble.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("assemble.jar")[0]);
    }

    public fr.unistra.ibmc.assemble2.gui.ProgressMonitor getMessageBar() {
        return this.messageBar;
    }

    public AssembleProject getCurrentAssembleProject() {
        return currentAssembleProject;
    }

    public TutorialsMenu getTutorialsMenu() {
        return tutorialsMenu;
    }

    public JMenuItem getConfigureAssemble() {
        return configureAssemble;
    }

    public static File getMotifsDirectory() {
        File motifsDir = new File(getUserDir(),"motifs");
        if (!motifsDir.exists())
            motifsDir.mkdir();
        return motifsDir;
    }

    public Assemble(final fr.unistra.ibmc.assemble2.gui.SplashScreen splashScreen) {
        this.splashScreen = splashScreen;
        if (new File(getUserDir(), "tmp").exists())
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    splashScreen.setMessage("Cleaning tmp directory");
                    splashScreen.getProgressBar().setIndeterminate(true);
                    IoUtils.clearDirectory(new File(getUserDir(), "tmp"));
                    splashScreen.getProgressBar().setIndeterminate(false);
                    try {
                        AssembleConfig.loadConfig();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Assemble.this.startup();
                    return null;
                }
            }.execute();
    }

    public JMenu getQualitativeColorMenu() {
        return qualitativeColorMenu;
    }

    public void updateRecentFilesMenu() {
        loadRecentFiles.removeAll();
        for (final MutablePair<String,String> pair:AssembleConfig.getRecentEntries()) {
            final String id = pair.left;
            if (pair.right.endsWith("-file")) {
                final File f = new File(id);
                if (f.exists()) {
                    JMenuItem item = new JMenuItem(f.getName());
                    item.setToolTipText(pair.left);
                    item.setIcon(new FileIcon());
                    loadRecentFiles.add(item);
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            loadData(id, pair.right);
                        }
                    });
                    loadRecentFiles.add(item);
                }
            } else if (pair.right.endsWith("-db")) {
                JMenuItem item = new JMenuItem(pair.left);
                item.setToolTipText(pair.left);
                item.setIcon(new WebIcon());
                loadRecentFiles.add(item);
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        loadData(pair.left, pair.right);
                    }
                });
                loadRecentFiles.add(item);
            }
        }
        loadRecentFiles.addSeparator();
        JMenuItem item = new JMenuItem("Clear Recent Entries");
        item.setIcon(new ClearIcon());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AssembleConfig.clearRecentFiles();
                try {
                    AssembleConfig.saveConfig();
                } catch (BackingStoreException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        item.setEnabled(!AssembleConfig.getRecentEntries().isEmpty());
        loadRecentFiles.add(item);
    }

    public void loadData(final String id, String format) {

        if (format.equals("assemble-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        AssembleProject project = new AssembleProject(mediator, f);
                        project.load(mediator);
                        List<StructuralAlignment> alignments = project.getStructuralAlignments();
                        StructuralAlignment selectedAlignment = null;
                        if (alignments.isEmpty()) { //for old Assemble projects. Then we use the secondary structures to initiate the working session
                            List<SecondaryStructure> secondaryStructures = project.getSecondaryStructures();
                            SecondaryStructure selectedSecondaryStructure = null;
                            if (secondaryStructures.size() == 1) {
                                selectedSecondaryStructure = secondaryStructures.get(0);
                            } else {
                                Map<String, SecondaryStructure> names = new HashMap<String, SecondaryStructure>();
                                for (SecondaryStructure ss:secondaryStructures)
                                    names.put(ss.getName(),ss);
                                String name = (String)JOptionPane.showInputDialog(null,"Choose a SecondaryStructure", "Choose a SecondaryStructure", JOptionPane.PLAIN_MESSAGE,null, new ArrayList<String>(names.keySet()).toArray(), names.keySet().iterator().next());
                                if (name != null) {
                                    try {
                                        selectedSecondaryStructure = names.get(name);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                            if (selectedSecondaryStructure != null) {
                                currentAssembleProject = project;

                                AssembleConfig.addRecentEntry(currentAssembleProject.getLocation().getAbsolutePath(), "assemble-file");
                                mediator.getAssemble().updateRecentFilesMenu();
                                setLastWorkingDirectory(currentAssembleProject.getLocation().getParentFile());
                                AssembleConfig.saveConfig();
                                ((JXFrame)window).setTitle(f.getName());

                                mediator.loadRNASecondaryStructure(selectedSecondaryStructure, false, true);
                                if (selectedSecondaryStructure.getLinkedTs() != null) {
                                    mediator.setTertiaryStructure(selectedSecondaryStructure.getLinkedTs());
                                    if (project.getChimeraSession() != null && mediator.getChimeraDriver() != null) {
                                        mediator.getChimeraDriver().restoreSession(project.getChimeraSession());
                                        mediator.getChimeraDriver().synchronizeFrom();
                                    }
                                    else if (mediator.getChimeraDriver() != null) {
                                        try {
                                            File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                                            FileParser.writePDBFile(mediator.getTertiaryStructure().getResidues3D(), true, new FileWriter(tmpF));
                                            mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        else if (alignments.size() == 1) {
                            selectedAlignment = alignments.get(0);
                        } else {
                            Map<String, StructuralAlignment> names = new HashMap<String, StructuralAlignment>();
                            for (StructuralAlignment alignment:alignments)
                                names.put(alignment.getName(),alignment);
                            String name = (String)JOptionPane.showInputDialog(null,"Choose a Structural Alignment", "Choose a Structural Alignment", JOptionPane.PLAIN_MESSAGE,null, new ArrayList<String>(names.keySet()).toArray(), names.keySet().iterator().next());
                            if (name != null) {
                                try {
                                    selectedAlignment = names.get(name);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        if (selectedAlignment != null) {
                            currentAssembleProject = project;

                            AssembleConfig.addRecentEntry(currentAssembleProject.getLocation().getAbsolutePath(), "assemble-file");
                            mediator.getAssemble().updateRecentFilesMenu();
                            setLastWorkingDirectory(currentAssembleProject.getLocation().getParentFile());
                            AssembleConfig.saveConfig();
                            ((JXFrame)window).setTitle(f.getName());

                            mediator.getAlignmentCanvas().setMainAlignment(selectedAlignment); //the alignment first to avoid to create a new one with the 2D
                            mediator.loadRNASecondaryStructure(selectedAlignment.getReferenceStructure().getSecondaryStructure(), false, true);

                            if (selectedAlignment.getReferenceStructure().getSecondaryStructure().getLinkedTs() != null)
                                mediator.setTertiaryStructure(selectedAlignment.getReferenceStructure().getSecondaryStructure().getLinkedTs());

                            if (project.getChimeraSession() != null && mediator.getChimeraDriver() != null) {
                                mediator.getChimeraDriver().restoreSession(project.getChimeraSession());
                                mediator.getChimeraDriver().synchronizeFrom();
                            }
                            else if (mediator.getTertiaryStructure() != null && mediator.getChimeraDriver() != null) {
                                try {
                                    File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                                    FileParser.writePDBFile(mediator.getTertiaryStructure().getResidues3D(), true, new FileWriter(tmpF));
                                    mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        ((JXFrame)window).setTitle(f.getName());
                    }
                    catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();

        }
        else if (format.equals("fasta-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground()  {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        mediator.getFoldingLandscape().clear();
                        if (AssembleConfig.popupLateralPanels())
                            mediator.getFoldingLandscape().getToolWindow().setVisible(true);
                        List<SecondaryStructure> secondaryStructures = FileParser.parseFasta(new FileReader(f), mediator);
                        if (! secondaryStructures.isEmpty()) {
                            for (SecondaryStructure ss : secondaryStructures)
                                mediator.loadRNASecondaryStructure(ss, true, false);
                            AssembleConfig.addRecentEntry(f.getAbsolutePath(), "fasta-file");
                            mediator.getAssemble().updateRecentFilesMenu();
                            Assemble.setLastWorkingDirectory(f.getParentFile());
                            AssembleConfig.saveConfig();
                            ((JXFrame) window).setTitle(f.getName());
                            if (Assemble.HELP_MODE)
                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("2Ds have been computed using several algorithms. Choose one prediction from the lateral panel \"2D Folds\".", null, null);
                            mediator.getSecondaryCanvas().repaint();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("vienna-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        SecondaryStructure ss = FileParser.parseVienna(f, mediator);
                        if (ss != null) {
                            mediator.loadRNASecondaryStructure(ss, false, true);
                            AssembleConfig.addRecentEntry(f.getAbsolutePath(), "vienna-file");
                            mediator.getAssemble().updateRecentFilesMenu();
                            Assemble.setLastWorkingDirectory(f.getParentFile());
                            AssembleConfig.saveConfig();
                            ((JXFrame)window).setTitle(f.getName());
                        }
                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("bpseq-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        SecondaryStructure ss = FileParser.parseBPSeq(f, mediator);
                        if (ss != null) {
                            mediator.loadRNASecondaryStructure(ss, false, true);
                            AssembleConfig.addRecentEntry(f.getAbsolutePath(), "bpseq-file");
                            mediator.getAssemble().updateRecentFilesMenu();
                            Assemble.setLastWorkingDirectory(f.getParentFile());
                            AssembleConfig.saveConfig();
                            ((JXFrame)window).setTitle(f.getName());

                        }
                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("ct-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        SecondaryStructure ss = FileParser.parseCT(new FileReader(f), mediator);
                        if (ss != null) {
                            mediator.loadRNASecondaryStructure(ss, false, true);
                            AssembleConfig.addRecentEntry(f.getAbsolutePath(), "ct-file");
                            mediator.getAssemble().updateRecentFilesMenu();
                            Assemble.setLastWorkingDirectory(f.getParentFile());
                            AssembleConfig.saveConfig();
                            ((JXFrame)window).setTitle(f.getName());

                        }
                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("pdb-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground()  {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        FileParser.parsePDB(mediator, f);
                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "pdb-file");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame)window).setTitle(f.getName());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("pdb-db")) {
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground()  {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        loadPDBID(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("rfam-db")) {
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground()  {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        loadRfamID(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("clustal-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground()  {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        Pair<Pair<String, List<SecondaryStructure>>, List<AlignedMolecule>> result = FileParser.parseClustal(new FileReader(f), mediator, null);

                        List<Molecule> molecules = new ArrayList<Molecule>();

                        for (AlignedMolecule am:result.getSecond())
                            molecules.add(am.getMolecule());

                        Molecule m = (Molecule) JOptionPane.showInputDialog(null, "Choose the molecule to display", "Choose the molecule to display", JOptionPane.PLAIN_MESSAGE, null, molecules.toArray(), molecules.get(0));

                        int index = molecules.indexOf(m);
                        //since no referenceId precised
                        SecondaryStructure reference2D = result.getFirst().getSecond().get(index);
                        AlignedMolecule referenceMolecule = result.getSecond().get(index);
                        ReferenceStructure referenceStructure = new ReferenceStructure(mediator, referenceMolecule, reference2D);
                        result.getSecond().remove(referenceMolecule);
                        StructuralAlignment alignment = new StructuralAlignment(mediator,  result.getFirst().getFirst(), referenceMolecule, referenceStructure,  result.getSecond());
                        mediator.getAlignmentCanvas().setMainAlignment(alignment);
                        mediator.loadRNASecondaryStructure(referenceStructure.getSecondaryStructure(), false, true);

                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "clustal-file");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame)window).setTitle(f.getName());

                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("stockholm-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                return;
            mediator.clearSession();
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        FileParser.parseStockholm(new FileReader(f), mediator, null);
                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "stockholm-file");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame)window).setTitle(f.getName());

                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("numeric values")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        FileParser.parseQuantitativeValues(f, mediator);

                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "numeric values");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame) window).setTitle(f.getName());

                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("gff3-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        String organismName = JOptionPane.showInputDialog(null, "Enter an organism name", lastOrganismName);
                        if (organismName == null)
                            return null;
                        organismName = organismName.trim();
                        lastOrganismName = organismName;
                        JFileChooser fileChooser = new JFileChooser(f.getParentFile());
                        fileChooser.setFileHidingEnabled(true);
                        fileChooser.setAcceptAllFileFilterUsed(false);

                        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                            @Override
                            public boolean accept(File file) {
                                return file.isDirectory() || file.getName().endsWith(".fasta") || file.getName().endsWith(".fna") || file.getName().endsWith(".fas") || file.getName().endsWith(".fa");
                            }

                            @Override
                            public String getDescription() {
                                return "FASTA Files (.fasta, .fna, .fas, .fa)";
                            }
                        });

                        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                            try {
                                File fastaFile = fileChooser.getSelectedFile();
                                //we will reformat everything to a local MongoDB-like structure
                                File tmpDirectory = IoUtils.createTemporaryDirectory(f.getName());

                                File genomesDir = new File(tmpDirectory, "genomes");
                                genomesDir.mkdir();
                                File ncRNAsDir = new File(tmpDirectory, "ncRNAs");
                                ncRNAsDir.mkdir();
                                File secondaryStructuresDir = new File(tmpDirectory, "secondaryStructures");
                                secondaryStructuresDir.mkdir();
                                File tertiaryStructuresDir = new File(tmpDirectory, "tertiaryStructures");
                                tertiaryStructuresDir.mkdir();
                                File alignmentsDir = new File(tmpDirectory, "alignments");
                                alignmentsDir.mkdir();
                                File annotationsDir = new File(tmpDirectory, "annotations");
                                annotationsDir.mkdir();

                                mediator.getGenomicAnnotationsPanel().setGenomicDataHandler(new LocalGenomicDataHandler(mediator, tmpDirectory));
                                //we extract and store the genomic sequences
                                List<Molecule> molecules = new ArrayList<Molecule>();
                                BufferedReader in = new BufferedReader(new FileReader(fastaFile));
                                StringBuffer seq = new StringBuffer();
                                String name = null;
                                String line = null;
                                while ((line = in.readLine()) != null) {
                                    if (line.startsWith(">")) {
                                        if (seq.length() != 0 && name != null) {
                                            String sequence = seq.toString().toUpperCase();
                                            molecules.add(new Molecule(name, sequence));
                                        }
                                        name = line.substring(1);
                                        seq = new StringBuffer();
                                    } else
                                        seq.append(line.replace('.', '-').replace('_', '-').replace(" ", ""));
                                }
                                //the last
                                if (seq.length() != 0 && name != null) {
                                    String sequence = seq.toString().toUpperCase();
                                    molecules.add(new Molecule(name, sequence));
                                }
                                in.close();
                                for (Molecule m : molecules) {
                                    m.setOrganism(organismName);
                                    BasicDBObject moleculeJSON = new BasicDBObject();
                                    moleculeJSON.put("_id", m.getId());
                                    moleculeJSON.put("sequence", m.printSequence());
                                    moleculeJSON.put("organism", organismName);
                                    PrintWriter pw = new PrintWriter(new File(genomesDir, moleculeJSON.get("_id") + ".json"));
                                    pw.print(moleculeJSON.toString());
                                    pw.close();
                                }
                                //now we extract all the annotations from the gff3 file
                                in = new BufferedReader(new FileReader(f));
                                line = null;
                                List<BasicDBObject> ncRNAs = new ArrayList<BasicDBObject>();

                                Map<Molecule, BasicDBList> annotations = new HashMap<Molecule, BasicDBList>();
                                for (Molecule m : molecules)
                                    annotations.put(m, new BasicDBList());
                                //int i= 0;
                                while ((line = in.readLine()) != null) {
                                    if (!line.startsWith("#")) {
                                        String[] tokens = line.split("\t+");
                                        int start = Integer.parseInt(tokens[3]), end = Integer.parseInt(tokens[4]);
                                        double score = 0f;
                                        try {
                                            score = Double.parseDouble(tokens[5]);
                                        } catch (NumberFormatException e) {

                                        }
                                        String moleculeName = tokens[0];
                                        BasicDBObject annotation = new BasicDBObject();
                                        Molecule annotatedMolecule = null;
                                        for (Molecule m : molecules)
                                            if (m.getName().equals(moleculeName)) {
                                                annotatedMolecule = m;
                                                break;
                                            }

                                        annotation.put("_id", new ObjectId().toString());
                                        annotation.put("genomicStrand", tokens[6]);
                                        BasicDBList genomicPositions = new BasicDBList();
                                        if (start < end) {
                                            genomicPositions.add(start);
                                            genomicPositions.add(end);
                                        } else {
                                            genomicPositions.add(end);
                                            genomicPositions.add(start);
                                        }
                                        annotation.put("genomicPositions", genomicPositions);
                                        annotation.put("genomeName", annotatedMolecule.getName());
                                        annotation.put("genome", annotatedMolecule.getId() + "@genomes");
                                        annotation.put("source", f.getName());
                                        annotation.put("organism", organismName);
                                        annotation.put("class", tokens[2]);
                                        annotation.put("score", score);
                                        if ("ncRNA".equals(tokens[2]))
                                            ncRNAs.add(annotation);
                                        else
                                            annotations.get(annotatedMolecule).add(annotation);
                                        mediator.getGenomicAnnotationsPanel().addRow(annotation);
                                    }
                                }
                                for (Map.Entry<Molecule, BasicDBList> e : annotations.entrySet()) {
                                    PrintWriter pw = new PrintWriter(new File(annotationsDir, e.getKey().getId() + ".json"));
                                    pw.print(e.getValue().toString());
                                    pw.close();
                                }

                                for (BasicDBObject a : ncRNAs) {
                                    PrintWriter pw = new PrintWriter(new File(ncRNAsDir, a.get("_id") + ".json"));
                                    pw.print(a.toString());
                                    pw.close();
                                }

                            } catch (Exception ex) {
                                Assemble.this.getMessageBar().printException(ex);
                            }

                        }
                        genomicAnnotationsWindow.setVisible(true);
                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "gff3-file");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame) window).setTitle(f.getName());

                    } catch (Exception e) {
                        messageBar.printException(e);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
        else if (format.equals("genbank-file")) {
            final File f = new File(id);
            setLastWorkingDirectory(f.getParentFile());
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() {
                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                    try {
                        //we will reformat everything to a local MongoDB-like structure
                        File tmpDirectory = IoUtils.createTemporaryDirectory(f.getName());

                        File genomesDir = new File(tmpDirectory, "genomes");
                        genomesDir.mkdir();
                        File ncRNAsDir = new File(tmpDirectory, "ncRNAs");
                        ncRNAsDir.mkdir();
                        File secondaryStructuresDir = new File(tmpDirectory, "secondaryStructures");
                        secondaryStructuresDir.mkdir();
                        File tertiaryStructuresDir = new File(tmpDirectory, "tertiaryStructures");
                        tertiaryStructuresDir.mkdir();
                        File alignmentsDir = new File(tmpDirectory, "alignments");
                        alignmentsDir.mkdir();
                        File annotationsDir = new File(tmpDirectory, "annotations");
                        annotationsDir.mkdir();

                        mediator.getGenomicAnnotationsPanel().setGenomicDataHandler(new LocalGenomicDataHandler(mediator, tmpDirectory));

                        BufferedReader in = new BufferedReader(new FileReader(f));
                        StringBuffer seq = new StringBuffer();
                        String line = null,
                                organismName = null,
                                featureType = null,
                                location = null,
                                accession = null;
                        boolean inOrganism = false, startOfSequence = false;
                        List<BasicDBObject> ncRNAs =  new ArrayList<BasicDBObject>();
                        BasicDBList annotations = new BasicDBList();
                        Pattern locationPattern = Pattern.compile("\\.\\.\\>?[0-9]+");
                        while ((line = in.readLine()) != null) {
                            String[] tokens = line.split("\\s+");
                            if (line.startsWith("ACCESSION")) {
                                accession = tokens[1];
                            } else if (line.trim().startsWith("ORGANISM")) {
                                organismName = line.split("ORGANISM")[1].trim();
                                inOrganism = true;
                            } else if (line.startsWith("REFERENCE")) {
                                inOrganism = false;
                            } else if (line.startsWith("ORIGIN")) {
                                startOfSequence = true;
                                //last feature
                                if (featureType != null && !"source".equals(featureType)) {

                                    Pair<String, BasicDBList> genomicStrand_and_genomicPositions = parseLocation(location);
                                    BasicDBObject annotation = new BasicDBObject();

                                    annotation.put("_id", new ObjectId().toString());
                                    annotation.put("genomicStrand", genomicStrand_and_genomicPositions.getFirst());
                                    annotation.put("genomicPositions", genomicStrand_and_genomicPositions.getSecond());
                                    annotation.put("source", f.getName());
                                    annotation.put("organism", organismName);
                                    annotation.put("class", featureType);
                                    if (featureType.equals("ncRNA")) {
                                        ncRNAs.add(annotation);
                                    } else {
                                        annotations.add(annotation);
                                    }
                                }
                            }

                            else if (tokens.length == 3 && locationPattern.matcher(tokens[2].trim()).find()) {

                                //new feature
                                if (featureType != null && !"source".equals(featureType)) {

                                    Pair<String, BasicDBList> genomicStrand_and_genomicPositions = parseLocation(location);
                                    BasicDBObject annotation = new BasicDBObject();

                                    annotation.put("_id", new ObjectId().toString());
                                    annotation.put("genomicStrand", genomicStrand_and_genomicPositions.getFirst());
                                    annotation.put("genomicPositions", genomicStrand_and_genomicPositions.getSecond());
                                    annotation.put("source", f.getName());
                                    annotation.put("organism", organismName);
                                    annotation.put("class", featureType);
                                    if (featureType.equals("ncRNA")) {
                                        ncRNAs.add(annotation);
                                    } else {
                                        annotations.add(annotation);
                                    }
                                }
                                featureType = tokens[1].trim();
                                location = tokens[2].trim();
                            }
                            else if (tokens.length == 2 && !tokens[1].trim().startsWith("/") && locationPattern.matcher(tokens[1].trim()).find()) { //still the content of the current location
                                location += tokens[1].trim();
                            }
                            else if (startOfSequence) {
                                seq.append(StringUtils.join(Arrays.copyOfRange(line.trim().split("\\s+"), 1, line.trim().split("\\s+").length)).toUpperCase());
                            }

                        }
                        if (seq.toString().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "No genomic sequence found!!");
                            mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                            return null;
                        }
                        Molecule m = new Molecule(accession, seq.toString());
                        m.setOrganism(organismName);
                        BasicDBObject moleculeJSON = new BasicDBObject();
                        moleculeJSON.put("_id", m.getId());
                        moleculeJSON.put("sequence", m.printSequence());
                        moleculeJSON.put("organism",organismName);
                        PrintWriter pw = new PrintWriter(new File(genomesDir, moleculeJSON.get("_id")+".json"));
                        pw.print(moleculeJSON.toString());
                        pw.close();

                        for (BasicDBObject o:ncRNAs) {
                            o.put("genomeName", m.getName());
                            o.put("genome", m.getId()+"@genomes");
                            mediator.getGenomicAnnotationsPanel().addRow(o);
                            pw = new PrintWriter(new File(ncRNAsDir, o.get("_id")+".json"));
                            pw.print(o.toString());
                            pw.close();
                        }

                        for (Object o:annotations) {
                            ((BasicDBObject)o).put("genomeName", m.getName());
                            ((BasicDBObject)o).put("genome", m.getId() + "@genomes");
                            mediator.getGenomicAnnotationsPanel().addRow((BasicDBObject) o);
                        }
                        pw = new PrintWriter(new File(annotationsDir, m.getId()+".json"));
                        pw.write(annotations.toString());
                        pw.close();

                        genomicAnnotationsWindow.setVisible(true);

                        AssembleConfig.addRecentEntry(f.getAbsolutePath(), "genbank-file");
                        mediator.getAssemble().updateRecentFilesMenu();
                        Assemble.setLastWorkingDirectory(f.getParentFile());
                        AssembleConfig.saveConfig();
                        ((JXFrame)window).setTitle(f.getName());
                    }
                    catch (Exception ex) {
                        Assemble.this.getMessageBar().printException(ex);
                    }
                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                    return null;
                }
            }.execute();
        }
    }

    private Pair<String,BasicDBList> parseLocation(String location) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE ;
        BasicDBList genomicPositions = new BasicDBList();
        String genomicStrand = "+";
        if (location.startsWith("complement(join(")) {
            genomicStrand = "-";
            String[] ends = location.split("complement\\(join\\(");
            ends = ends[1].substring(0, ends[1].length()-2).replace("join\\(", "").replace("\\)","").replace(",","..").split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else if (location.startsWith("join(complement(")) {
            genomicStrand = "-";
            String[] ends = location.split("join\\(complement\\(");
            ends = ends[1].substring(0, ends[1].length()-2).replace("complement\\(", "").replace("\\)","").replace(",","..").split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else if (location.startsWith("complement(order(")) {
            genomicStrand = "-";
            String[] ends = location.split("complement\\(order\\(");
            ends = ends[1].substring(0, ends[1].length()-2).replace(",","..").split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else if (location.startsWith("order(")) {
            String[] ends = location.split("order\\(");
            ends = ends[1].substring(0, ends[1].length()-1).replace(",","..").split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else if (location.startsWith("complement(")) {
            genomicStrand = "-";
            String[] ends = location.split("complement\\(");
            ends = ends[1].substring(0, ends[1].length()-1).split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else if (location.startsWith("join(")) {
            String[] ends = location.split("join\\(");
            ends = ends[1].substring(0, ends[1].length()-1).replace(",","..").split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        else { //regular location
            String[] ends = location.split("\\.\\.");
            for (String end: ends) {
                int _end = Integer.parseInt(end.replace(">", "").replace("<", ""));
                if (_end < min )
                    min = _end;
                if (_end > max)
                    max = _end;
            }
            genomicPositions.add(min);
            genomicPositions.add(max);
        }
        return new Pair<String, BasicDBList>(genomicStrand, genomicPositions);
    }

    public void loadTertiaryStructures(List<TertiaryStructure> tertiaryStructures) throws Exception {
        for (TertiaryStructure t:tertiaryStructures)
            System.out.println("tertiary structure residues: "+t.getMolecule().size());
        TertiaryStructure ts = null;
        Object[] choices = new Object[tertiaryStructures.size()+1];
        choices[0] = "Merge all molecules";
        for (int i = 0 ; i < tertiaryStructures.size() ; i++)
            choices[i+1] = tertiaryStructures.get(i).getMolecule();
        if (tertiaryStructures.size() > 1) {
            Object choice = JOptionPane.showInputDialog(null, "Choose a Molecular Chain", "Choose a Molecular Chain", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (Molecule.class.isInstance(choice)) {
                for (int i = 0 ; i < tertiaryStructures.size() ; i++)
                    if (tertiaryStructures.get(i).getMolecule() == (Molecule)choice) {
                        ts = tertiaryStructures.get(i);
                        break;
                    }
            }
            else if (choice.toString().equals(choices[0])) {
                StringBuffer buff = new StringBuffer();
                List<Residue3D> allResidues3D = new ArrayList<Residue3D>();
                int absPos = 1;
                Molecule newMolecule = new Molecule("A");
                for (TertiaryStructure _ts: tertiaryStructures) {
                    buff.append(_ts.getMolecule().printSequence());
                    for (Residue3D residue3D:_ts.getResidues3D()) {
                        residue3D.setAbsolutePosition(absPos++);
                        residue3D.setMolecule(newMolecule);
                        allResidues3D.add(residue3D);
                    }
                }
                newMolecule.setSequence(buff.toString());
                ts = new TertiaryStructure(newMolecule);
                for (Residue3D residue3D:allResidues3D)
                    ts.addResidue3D(residue3D);
            }
        }
        else if (!tertiaryStructures.isEmpty())
            ts = tertiaryStructures.get(0);
        else {
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("No 3D found.", null, null);
            mediator.getSecondaryCanvas().repaint();
        }
        if (ts != null) {
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("3D annotation.", null, null);
            mediator.getSecondaryCanvas().repaint();
            SecondaryStructure ss = new Rnaview(mediator).annotate(ts);
            if (ss != null) {
                mediator.setTertiaryStructure(ss.getLinkedTs());
                if (mediator.getChimeraDriver() != null) {
                    File tmpF = IoUtils.createTemporaryFile("ts.pdb");
                    FileParser.writePDBFile(mediator.getTertiaryStructure().getResidues3D(), true, new FileWriter(tmpF));
                    mediator.getChimeraDriver().loadTertiaryStructure(tmpF);
                }
                mediator.loadRNASecondaryStructure(ss, false, true);
                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Done.", null, null);
                mediator.getSecondaryCanvas().repaint();
            } else {
                final StringWriter pdbContent = new StringWriter();
                FileParser.writePDBFile(ts.getResidues3D(), true, pdbContent);
                java.util.List<String> texts = new ArrayList<String>();
                java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                        nextActions = new ArrayList<MessagingSystemAction>();
                texts.add("RNAVIEW is not able to annotate your 3D.");
                closeActions.add(null);
                nextActions.add(null);
                texts.add("Click next to report a bug.");
                closeActions.add(null);
                nextActions.add(new MessagingSystemAction() {
                    @Override
                    public void run() {
                        ReportDialog d = new ReportDialog(mediator);
                        d.setTitle("Bug Report");
                        d.getReportContent().setContentType("text/html");
                        d.getReportContent().setText("Dear Assemble2 team,<br/><br/>" +
                                "My Assemble2 had problems to annotate the following 3D:<br/><br/>" +
                                "<pre>" + pdbContent.toString() + "</pre>" +
                                "<br/><br/><b>My details:</b><br/>" +
                                "Assemble2 version: " + IoUtils.getAssemble2Release() + "<br/>" +
                                "Operating system: " + System.getProperty("os.name") + "<br/>" +
                                "Java version: " + System.getProperty("java.version") + "<br/><br/>" +
                                "Cheers."
                        );
                        final java.awt.Dimension win =  Toolkit.getDefaultToolkit().getScreenSize().getSize();
                        d.setSize(win.width/2, win.height/2);
                        d.setResizable(false);
                        IoUtils.centerOnScreen(d);
                        d.setVisible(true);
                    }
                });
                mediator.getSecondaryCanvas().getMessagingSystem().addThread(texts, closeActions, nextActions);
                mediator.getSecondaryCanvas().repaint();
            }
        }
    }

    protected void startup() {
        this.window = new JXFrame("Assemble2");
        ((JXFrame) this.window).setDefaultCloseOperation(((JXFrame) this.window).DO_NOTHING_ON_CLOSE);
        this.window.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(final java.awt.event.WindowEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to exit Assemble2?", "Confirm exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        AssembleConfig.saveConfig();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (mediator.getChimeraDriver() != null)
                        mediator.getChimeraDriver().close();
                    System.exit(0);
                }
            }

        });

        this.mediator = new Mediator(this);

        this.mediator.setToolWindowManager(new MyDoggyToolWindowManager());

        this.mediator.getToolWindowManager().getToolWindowManagerDescriptor().setPushAwayMode(PushAwayMode.HORIZONTAL);

        Rna2DViewer rna2dViewer = new Rna2DViewer(this.mediator);
        rna2dViewer.addKeyListener(new AssembleKeyListener(mediator));

        String release = IoUtils.getAssemble2Release();

        ((JFrame) this.window).setTitle(release);

        //###### the panels displayed by the lateral toolbar ####

        AssembleDisplayPanel assembleDisplayPanel = new AssembleDisplayPanel();
        final JScrollPane assembleDisplayPanelScrollPane = new JScrollPane(assembleDisplayPanel);
        assembleDisplayPanelScrollPane.getViewport().setBackground(Color.WHITE);

        AssembleSelectionPanel assembleSelectionPanel = new AssembleSelectionPanel();
        final JScrollPane assembleSelectionPanelScrollPane = new JScrollPane(assembleSelectionPanel);
        assembleSelectionPanelScrollPane.getViewport().setBackground(Color.WHITE);

        AssembleEditionPanel assembleEditionPanel = new AssembleEditionPanel();
        final JScrollPane assembleEditionPanelScrollPane = new JScrollPane(assembleEditionPanel);
        assembleEditionPanelScrollPane.getViewport().setBackground(Color.WHITE);

        this.secondaryStructureNavigator = new SecondaryStructureNavigator(mediator);
        final JScrollPane navigatorScrollPane = new JScrollPane(this.secondaryStructureNavigator);
        navigatorScrollPane.getViewport().setBackground(Color.WHITE);

        FoldingLandscape ws = new FoldingLandscape(mediator);
        JScrollPane p = new JScrollPane(ws);
        p.getViewport().setBackground(Color.WHITE);
        ToolWindow toolWindow = mediator.getToolWindowManager().registerToolWindow("2D Folds", "2D Folds", null, p, ToolWindowAnchor.BOTTOM);
        DockedTypeDescriptor descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(300);
        descriptor.setPreviewEnabled(false);
        ws.setToolWindow(toolWindow);

        mediator.getToolWindowManager().getContentManager().addContent("2DView", "2D Viewer", null, new JScrollPane(rna2dViewer));
        toolWindow = mediator.getToolWindowManager().registerToolWindow("Secondary Structure", "Secondary Structure", null, navigatorScrollPane, ToolWindowAnchor.LEFT);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(200);
        descriptor.setPreviewEnabled(false);

        CreateRNAMotifPanel rnamotifPanel = new CreateRNAMotifPanel();
        final JScrollPane rnaMotifPanelScrollPane = new JScrollPane(rnamotifPanel);
        rnaMotifPanelScrollPane.getViewport().setBackground(Color.WHITE);
        toolWindow = mediator.getToolWindowManager().registerToolWindow("Create RNA Motif", "Create RNA motif", null, rnaMotifPanelScrollPane, ToolWindowAnchor.RIGHT);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(300);
        descriptor.setPreviewEnabled(false);

        this.myRNAMotifsPanel = new MyRNAMotifsPanel();
        toolWindow = mediator.getToolWindowManager().registerToolWindow("My RNA Motifs", "My RNA Motifs", null, this.myRNAMotifsPanel, ToolWindowAnchor.BOTTOM);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(200);
        descriptor.setPreviewEnabled(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        TertiaryFragmentsPanel tertiaryFragmentsPanel = new TertiaryFragmentsPanel(mediator);
        mediator.setTertiaryFragmentsPanel(tertiaryFragmentsPanel);
        toolWindow = mediator.getToolWindowManager().registerToolWindow("3D Folds", "3D Folds", null, new JScrollPane(tertiaryFragmentsPanel), ToolWindowAnchor.RIGHT);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(200);
        descriptor.setPreviewEnabled(false);
        tertiaryFragmentsPanel.setToolWindow(toolWindow);

        AlignmentCanvas alignmentCanvas = new AlignmentCanvas(this.mediator, (int) screenSize.getWidth(), (int) screenSize.getHeight());
        JPanel alignmentPanel = new JPanel();
        alignmentPanel.setLayout(new BorderLayout());
        alignmentPanel.add(new JScrollPane(alignmentCanvas), BorderLayout.CENTER);
        this.alignmentPanelWindow = mediator.getToolWindowManager().registerToolWindow("Structural Alignment", "Structural Alignment", null, alignmentPanel, ToolWindowAnchor.BOTTOM);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(400);
        descriptor.setPreviewEnabled(false);

        MoleculesList moleculesList = new MoleculesList(this.mediator);
        final JScrollPane moleculesListScrollPane = new JScrollPane(moleculesList);
        toolWindow = mediator.getToolWindowManager().registerToolWindow("Aligned RNAs", "Aligned RNAs", null, moleculesListScrollPane, ToolWindowAnchor.LEFT);
        descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(200);
        descriptor.setPreviewEnabled(false);

        GenomicAnnotationsPanel genomicAnnotationsPanel = new GenomicAnnotationsPanel(this.mediator);
        final JScrollPane genomicAnnotationsPanelScrollPane = new JScrollPane(genomicAnnotationsPanel);
        this.genomicAnnotationsWindow = this.mongoDBPanelWindow = mediator.getToolWindowManager().registerToolWindow("Genomic Annotations", "Genomic Annotations", null, genomicAnnotationsPanelScrollPane, ToolWindowAnchor.LEFT);
        descriptor = (DockedTypeDescriptor) this.genomicAnnotationsWindow.getTypeDescriptor(ToolWindowType.DOCKED);
        descriptor.setDockLength(500);
        descriptor.setPreviewEnabled(false);

        for (ToolWindow window : mediator.getToolWindowManager().getToolWindows())
            window.setAvailable(true);

        this.window.add(mediator.getToolWindowManager(), BorderLayout.CENTER);
        ((JXFrame) this.window).setJMenuBar(new AssembleMenu());

        this.splashScreen.dispose();

        this.window.pack();

        this.window.setSize((int) (screenSize.width * 0.5), (int) (screenSize.height));
        this.window.setLocation((int) (screenSize.width * 0.5), 0);
        this.window.setVisible(true);

        if (AssembleConfig.launchChimeraAtStart())
            new ChimeraDriver(mediator);
        //the first time a file is opened, the user is in the samples directory
        setLastWorkingDirectory(new File(Assemble.getInstallPath(), "samples"));

        javax.swing.Timer timer = new javax.swing.Timer(60000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (AssembleConfig.showWelcomeDialog()) {
                    WelcomeDialog dialog = new WelcomeDialog(mediator);
                    dialog.pack();
                    dialog.setResizable(false);
                    IoUtils.centerOnScreen(dialog);
                    dialog.setVisible(true);
                }
            }
        });
        timer.setRepeats(false);
        timer.start();

        if (!isServerReachable())
            JOptionPane.showMessageDialog(Assemble.this.window,
                    "Cannot reach "+AssembleConfig.getWebservicesAddress().get(0),
                    "Server unreachable",
                    JOptionPane.WARNING_MESSAGE);
        else
            openWebSocket();
    }

    public void openWebSocket() {
        new javax.swing.SwingWorker() {
            @Override
            protected Object doInBackground() {
                try {
                    if (mediator.getWebSocketClient() != null)
                        mediator.getWebSocketClient().close();
                    String baseURL = AssembleConfig.getWebservicesAddress().get(0).split("http://")[1];
                    WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://" + baseURL + "/websocket"));
                    mediator.setWebSocketClient(webSocketClient);
                    webSocketClient.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public boolean isServerReachable() {
        // Otherwise an exception may be thrown on invalid SSL certificates:
        String url =  AssembleConfig.getWebservicesAddress().get(0).replaceFirst("^https", "http");
        url = url.split("/api/")[0];
        int timeout = 100;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }

    /**
     * So far, insertion can only be done inside single-strands.
     * @param position
     * @param subsequence
     * @throws Exception
     */
    public void insertSubSequence(int position, String subsequence) throws Exception {

        SecondaryStructure ss = mediator.getSecondaryStructure();

        if (!SingleStrand.class.isInstance(ss.getResidue(position+1).getStructuralDomain())) {
            JOptionPane.showMessageDialog(Assemble.this.window,"You can only insert new residues in a single-strand");
            return;
        }

        Molecule m = new Molecule(ss.getName(), new StringBuffer(ss.getMolecule().printSequence()).insert(position, subsequence).toString());

        List<Location> helicalLocations = new ArrayList<Location>();
        List<MutablePair<Location,String>> tertiary_interactions = new ArrayList<MutablePair<Location, String>>();

        for (Helix h:ss.getHelices()) {
            int[] ends = h.getLocation().getEnds();
            if (position <= ends[0])
                helicalLocations.add((new Location(new Location(ends[0]+subsequence.length(), ends[1]+subsequence.length()), new Location(ends[2]+subsequence.length(), ends[3]+subsequence.length()))));
                //since an insertion cannot be done inside an helix, the condition position <= ends[1] is not tested
            else if (position < ends[2]+1)
                helicalLocations.add((new Location(new Location(ends[0], ends[1]), new Location(ends[2]+subsequence.length(), ends[3]+subsequence.length()))));
            else
                helicalLocations.add(h.getLocation());
        }

        for (BaseBaseInteraction bbi: ss.getTertiaryInteractions()) {
            int[] ends = bbi.getLocation().getEnds();
            String type = bbi.getOrientation()+""+bbi.getEdge(bbi.getResidue())+" "+bbi.getEdge(bbi.getPartnerResidue());
            if (position < ends[0])
                tertiary_interactions.add(new MutablePair<Location, String>(new Location(new Location(ends[0] + subsequence.length()), new Location(ends[2] + subsequence.length())), type));
            else if (position < ends[2])
                tertiary_interactions.add(new MutablePair<Location, String>(new Location(new Location(ends[0]), new Location(ends[2] + subsequence.length())), type));
        }

        boolean gotInsertionPoint = false;
        TertiaryStructure ts = mediator.getTertiaryStructure();
        if (ts != null) {
            ts.setMolecule(m); //the molecule has changed, we need to update it for the 3D
            for (Residue3D r: ts.getResidues3D()) {
                if (r.getAbsolutePosition() >= position+1)
                    gotInsertionPoint = true;
                if (gotInsertionPoint) {
                    r.setAbsolutePosition(r.getAbsolutePosition()+subsequence.length());
                }
            }
            if (mediator.getChimeraDriver() != null)
                mediator.getChimeraDriver().synchronizeTo(); //we send the new 3D to Chimera
        }

        SecondaryStructure _ss = new SecondaryStructure(mediator, m, helicalLocations, new ArrayList<MutablePair<Location,String>>(), tertiary_interactions);
        _ss.setName(ss.getName());
        mediator.loadRNASecondaryStructure(_ss, false, true);
        mediator.getSecondaryStructure().setId(ss.getId());
        mediator.getSecondaryCanvas().select(mediator.getSecondaryStructure().getResidue(position+1));
    }

    public void removeSubSequence(int position, int length) throws Exception {

        SecondaryStructure ss = mediator.getSecondaryStructure();

        if (!SingleStrand.class.isInstance(ss.getResidue(position).getStructuralDomain())) {
            JOptionPane.showMessageDialog(Assemble.this.window,"You can only remove residues from a single-strand");
            return;
        }

        Molecule m = new Molecule(ss.getName(), new StringBuffer(ss.getMolecule().printSequence()).replace(position-1, position, "").toString());

        List<Location> helicalLocations = new ArrayList<Location>();
        List<MutablePair<Location,String>> tertiary_interactions = new ArrayList<MutablePair<Location, String>>();

        for (Helix h:ss.getHelices()) {
            int[] ends = h.getLocation().getEnds();
            if (position <= ends[0])
                helicalLocations.add((new Location(new Location(ends[0]-length, ends[1]-length), new Location(ends[2]-length, ends[3]-length))));
                //since an insertion cannot be done inside an helix, the condition position <= ends[1] is not tested
            else if (position < ends[2]+1)
                helicalLocations.add((new Location(new Location(ends[0], ends[1]), new Location(ends[2]-length, ends[3]-length))));
            else
                helicalLocations.add(h.getLocation());
        }

        for (BaseBaseInteraction bbi: ss.getTertiaryInteractions()) {
            int[] ends = bbi.getLocation().getEnds();
            String type = bbi.getOrientation()+""+bbi.getEdge(bbi.getResidue())+" "+bbi.getEdge(bbi.getPartnerResidue());
            if (position < ends[0])
                tertiary_interactions.add(new MutablePair<Location, String>(new Location(new Location(ends[0] - length), new Location(ends[2] - length)), type));
            else if (position < ends[2])
                tertiary_interactions.add(new MutablePair<Location, String>(new Location(new Location(ends[0]), new Location(ends[2] - length)), type));
        }

        boolean gotInsertionPoint = false;
        TertiaryStructure ts = mediator.getTertiaryStructure();
        if (ts != null) {
            ts.setMolecule(m); //the molecule has changed, we need to update it for the 3D
            ts.removeResidue3D(position);//we remove the deleted residue3D
            for (Residue3D r: ts.getResidues3D()) {
                if (r.getAbsolutePosition() >= position+1)
                    gotInsertionPoint = true;
                if (gotInsertionPoint) {
                    r.setAbsolutePosition(r.getAbsolutePosition()-length);
                }
            }
            if (mediator.getChimeraDriver() != null)
                mediator.getChimeraDriver().synchronizeTo(); //we send the new 3D to Chimera
        }
        SecondaryStructure _ss = new SecondaryStructure(mediator, m, helicalLocations, new ArrayList<MutablePair<Location,String>>(), tertiary_interactions);
        _ss.setName(ss.getName());
        mediator.loadRNASecondaryStructure(_ss, false, true);
        mediator.getSecondaryStructure().setId(ss.getId());
        mediator.getSecondaryCanvas().select(mediator.getSecondaryStructure().getResidue(position-1));
    }

    private class AssembleMenu extends javax.swing.JMenuBar {
        private AssembleMenu() {
            fileMenu = new FileMenu();
            this.add(fileMenu);
            this.add(new ColorsMenu());
            this.add(new ToolBarsMenu());
            tutorialsMenu = new TutorialsMenu();
            this.add(tutorialsMenu);
            externalResourcesMenu = new ExternalResourcesMenu();
            this.add(externalResourcesMenu);
            this.add(new AboutMenu());
        }
    }
    public JFrame getFrame() {
        return (JFrame) this.window;
    }

    public static boolean isAssembleProject(File file) {
        File moleculeDir = new File(file,Molecule.class.getSimpleName()+"s"),
                secondaryDir = new File(file,SecondaryStructure.class.getSimpleName()+"s"),
                tertiaryDir = new File(file,TertiaryStructure.class.getSimpleName()+"s");
        return file.isDirectory() && moleculeDir.exists() && moleculeDir.isDirectory() && (
                secondaryDir.exists() && secondaryDir.isDirectory() ||
                        tertiaryDir.exists() && tertiaryDir.isDirectory()
        );
    }

    private class FileMenu extends JMenu {

        public FileMenu() {
            super("File");
            JMenu load = new JMenu("Load...");
            load.setIcon(new LoadIcon());
            this.add(load);
            JMenuItem item = new JMenuItem("Assemble2 Project");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setFileFilter(AssembleProject.getFileFilter());
                    fileChooser.setFileView(new FileView() {
                        public Icon getIcon(File f) {
                            if (IoUtils.isAssembleProject(f))
                                return new ImageIcon(RessourcesUtils.getImage("assemble2-project.png"));
                            else
                                return null;
                        }

                        public Boolean isTraversable(File f) {
                            return f.isDirectory() && !IoUtils.isAssembleProject(f);
                        }
                    });
                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        loadData(fileChooser.getSelectedFile().getAbsolutePath(), "assemble-file");
                    }
                }
            });
            load.add(item);

            genomicAnnotations = new JMenu("Genomic Annotations...");
            genomicAnnotations.setIcon(new FileIcon());
            load.add(genomicAnnotations);

            item = new JMenuItem("From a GFF3 File");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.getGenomicAnnotationsPanel().clearList();
                    mediator.clearSession();
                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".gff3");
                        }

                        @Override
                        public String getDescription() {
                            return "GFF3 Files (.gff3)";
                        }
                    });

                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        loadData(fileChooser.getSelectedFile().getName(), "gff3-file");
                    }
                }});
            genomicAnnotations.add(item);

            item = new JMenuItem("From a GenBank File");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.getGenomicAnnotationsPanel().clearList();
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".gb");
                        }

                        @Override
                        public String getDescription() {
                            return "GenBank Files (.gb)";
                        }
                    });

                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        loadData(fileChooser.getSelectedFile().getAbsolutePath(), "genbank-file");
                    }
                }});
            genomicAnnotations.add(item);

            item = new JMenuItem("From the GenBank Database");
            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.getGenomicAnnotationsPanel().clearList();
                    mediator.clearSession();
                    final String genbankID = JOptionPane.showInputDialog(null,"Enter your Genbank ID", "NC_000913.2");

                    if (genbankID != null && genbankID.trim().length() != 0) {
                        new javax.swing.SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                File lastWorkingDir = Assemble.getLastWorkingDirectory();
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                BufferedReader in = null;
                                StringBuffer bpseqData = new StringBuffer();
                                String moleculeName = null;
                                try {
                                    URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&rettype=gbwithparts&retmode=text&id="+genbankID.trim());
                                    in = new BufferedReader(new InputStreamReader(url.openStream()));
                                    File tmpFile = IoUtils.createTemporaryFile(genbankID.trim());
                                    String str = null;
                                    StringBuffer buffer = new StringBuffer();
                                    PrintWriter pw = new PrintWriter(tmpFile);
                                    while ((str = in.readLine()) != null)
                                        pw.write(str + "\n");
                                    in.close();
                                    pw.close();
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Genbank entry downloaded.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                    loadData(tmpFile.getAbsolutePath(), "genbank-file");
                                    //We don't want the dir of the tmp file as the new last working dir
                                    Assemble.setLastWorkingDirectory(lastWorkingDir);
                                    ((JXFrame)window).setTitle("NCBI:"+genbankID);
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                } catch (Exception ex) {
                                    messageBar.printException(ex);
                                }
                                return null;
                            }
                        }.execute();

                    }
                }});
            genomicAnnotations.add(item);

            /*if (AssembleConfig.getWebservicesAddress().trim().length() == 0) {
                item = new JMenuItem("From MongoDB");

                item.setIcon(new DatabaseIcon());
                item.addActionListener(loadScaffoldsFromMongoDB);
                genomicAnnotations.add(item);
            }*/

            item = new JMenuItem("RNA Molecule(s)");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".fasta") || file.getName().endsWith(".fna") || file.getName().endsWith(".fas");
                        }

                        @Override
                        public String getDescription() {
                            return "FASTA Files (.fasta, .fna, .fas)";
                        }
                    });

                    /*fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".gff3");
                        }

                        @Override
                        public String getDescription() {
                            return "GFF3 Files (.gff3)";
                        }
                    });*/

                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        loadData(fileChooser.getSelectedFile().getAbsolutePath(), "fasta-file");
                    }
                }});

            load.add(item);

            JMenu secondaryMenu = new JMenu("RNA Secondary Structure...");
            secondaryMenu.setIcon(new LoadIcon());
            load.add(secondaryMenu);
            item = new JMenuItem("from the RNA STRAND Database");
            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final String strandID = JOptionPane.showInputDialog(null,"Enter your STRAND ID", "ASE_00001");

                    if (strandID != null && strandID.trim().length() != 0) {
                        new javax.swing.SwingWorker() {
                            @Override
                            protected Object doInBackground() {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                BufferedReader in = null;
                                StringBuffer bpseqData = new StringBuffer();
                                String moleculeName = null;
                                try {
                                    URL url = new URL("http://www.rnasoft.ca/strand/show_file.php?format=Bpseq&molecule_ID="+strandID.trim());
                                    in = new BufferedReader(new InputStreamReader(url.openStream()));
                                    String str = null;
                                    boolean inBpseqData = false;
                                    while ((str = in.readLine()) != null)
                                        if (str.trim().startsWith("# File"))
                                            inBpseqData = true;
                                        else if (str.trim().startsWith("# RNA SSTRAND database,"))
                                            moleculeName  = str.split("# RNA SSTRAND database,")[1];
                                        else if (inBpseqData)
                                            bpseqData.append(str+"\n");
                                        else if (str.trim().startsWith("</textarea>"))
                                            inBpseqData = false;
                                    in.close();

                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("SSTRAND entry downloaded.", null, null);
                                    mediator.getSecondaryCanvas().repaint();

                                    SecondaryStructure ss = FileParser.parseBPSeq(new StringReader(bpseqData.toString()), mediator);
                                    ss.setName(strandID.trim()+" (RNA STRAND DB)");
                                    mediator.loadRNASecondaryStructure(ss, false, true);
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                    ((JXFrame)window).setTitle(moleculeName);
                                } catch (Exception ex) {
                                    messageBar.printException(ex);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                ((JXFrame)window).setTitle("RNA STRAND:"+strandID);
                                return null;
                            }
                        }.execute();

                    }
                }
            });
            secondaryMenu.add(item);

            item = new JMenuItem("from a CT/BPSEQ/Vienna File");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".ct");
                        }

                        @Override
                        public String getDescription() {
                            return "CT Files (.ct)";
                        }
                    });
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".bpseq");
                        }

                        @Override
                        public String getDescription() {
                            return "BPSEQ Files (.bpseq)";
                        }
                    });
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".fasta") || file.getName().endsWith(".fna") || file.getName().endsWith(".fas");
                        }

                        @Override
                        public String getDescription() {
                            return "Vienna Files (.fasta, .fna, .fas)";
                        }
                    });

                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        final File f = fileChooser.getSelectedFile();
                        if (f.getName().endsWith(".bpseq")) {
                            loadData(f.getAbsolutePath(), "bpseq-file");
                        }
                        else if (f.getName().endsWith(".ct")) {
                            loadData(f.getAbsolutePath(), "ct-file");
                        }
                        else if (f.getName().endsWith(".fasta") || f.getName().endsWith(".fna") || f.getName().endsWith(".fas")) {
                            loadData(f.getAbsolutePath(), "vienna-file");
                        }
                    }
                }
            });
            secondaryMenu.add(item);

            tertiaryMenu = new JMenu("RNA Tertiary Structure...");
            tertiaryMenu.setIcon(new LoadIcon());
            load.add(tertiaryMenu);
            item = new JMenuItem("from the RCSB Protein Data Bank");
            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    final String pdbId = JOptionPane.showInputDialog(null,"Enter your PDB ID", "1EHZ");
                    if (pdbId != null && pdbId.trim().length() != 0) {
                        new javax.swing.SwingWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                loadPDBID(pdbId.trim());
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            tertiaryMenu.add(item);

            item = new JMenuItem("from a PDB File");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".pdb");
                        }

                        @Override
                        public String getDescription() {
                            return "PDB Files (.pdb)";
                        }
                    });


                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        loadData(fileChooser.getSelectedFile().getAbsolutePath(), "pdb-file");
                    }
                }
            });
            tertiaryMenu.add(item);

            item = new JMenuItem("from Chimera");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        List<JComponent> inputs = new ArrayList<JComponent>();

                        inputs.add(new JLabel("Model ID"));
                        final JTextField modelID = new JTextField("0");
                        inputs.add(modelID);
                        inputs.add(new JLabel("Relative to ID"));
                        final JTextField relativeID = new JTextField();
                        inputs.add(relativeID);


                        if (JOptionPane.OK_OPTION ==  JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "Choose Chimera IDs", JOptionPane.OK_CANCEL_OPTION)) {

                            new javax.swing.SwingWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    int modelId = -1, relativeId = -1;
                                    try {
                                        modelId = Integer.parseInt(modelID.getText().trim());
                                        relativeId = Integer.parseInt(relativeID.getText().trim());
                                    }
                                    finally {
                                        if (modelId != -1) {
                                            mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                            mediator.getChimeraDriver().importStructure(modelId, relativeId);
                                            mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                        }
                                    }
                                    return null;
                                }
                            }.execute();
                        }

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            tertiaryMenu.add(item);

            alignmentMenu = new JMenu("RNA Alignment...");
            alignmentMenu.setIcon(new LoadIcon());
            load.add(alignmentMenu);
            item = new JMenuItem("from the Rfam Database");
            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    List<JComponent> inputs = new ArrayList<JComponent>();
                    inputs.add(new JLabel("Rfam ID"));
                    final JTextField rfamId = new JTextField("RF00025");
                    inputs.add(rfamId);
                    //inputs.add(new JLabel("Type"));
                    final JComboBox type = new JComboBox(new String[]{"seed", "full"});
                    //inputs.add(type);
                    final JComboBox nse = new JComboBox(new String[]{"Yes", "No"});
                    //inputs.add(new JLabel("Organism names"));
                    //inputs.add(nse);
                    if (JOptionPane.OK_OPTION ==  JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "Enter your Rfam Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {

                        if (rfamId.getText().trim().length() != 0) {
                            new javax.swing.SwingWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    loadRfamID(rfamId.getText().trim());
                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                    return null;
                                }
                            }.execute();

                        }
                    }
                }
            });
            alignmentMenu.add(item);

            item = new JMenuItem("from a Clustal/Stockholm File");
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window,"Are you sure to quit the current Project?"))
                        return;
                    mediator.clearSession();
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".sto");
                        }

                        @Override
                        public String getDescription() {
                            return "Stockholm (.sto)";
                        }
                    });

                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".aln");
                        }

                        @Override
                        public String getDescription() {
                            return "Clustal (.aln)";
                        }
                    });

                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        final File f = fileChooser.getSelectedFile();
                        if (f.getName().endsWith(".aln")) {
                            loadData(f.getAbsolutePath(), "clustal-file");
                        }
                        else if (f.getName().endsWith(".sto")) {
                            loadData(f.getAbsolutePath(), "stockholm-file");
                        }
                    }
                }
            });
            alignmentMenu.add(item);

            /*if (AssembleConfig.getWebservicesAddress().trim().length() == 0) {
                item = new JMenuItem("From MongoDB");
                item.setIcon(new DatabaseIcon());
                item.addActionListener(loadAlignmentsFromMongoDB);
                alignmentMenu.add(item);
            }*/

            loadRecentFiles = new JMenu("Load Recent...") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(!AssembleConfig.getRecentEntries().isEmpty());
                    super.paintComponent(graphics);
                }
            };
            loadRecentFiles.setIcon(new FileIcon());
            this.add(loadRecentFiles);
            Assemble.this.updateRecentFilesMenu();

            item = new JMenuItem("Save Project") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(currentAssembleProject != null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new SaveIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    new javax.swing.SwingWorker() {
                        protected Object doInBackground()  {
                            try {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                if (mediator.getChimeraDriver() != null)
                                    mediator.getChimeraDriver().synchronizeFrom();
                                new AssembleProject(mediator, currentAssembleProject.getLocation()).save();
                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Saving done.", null, null);
                                mediator.getSecondaryCanvas().repaint();
                            }
                            catch (Exception e) {
                                messageBar.printException(e);
                            }
                            mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                            return null;
                        }
                    }.execute();
                }
            });
            this.add(item);
            JMenu saveProject = new JMenu("Save Project...") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            saveProject.setIcon(new SaveIcon());
            this.add(saveProject);

            item = new JMenuItem("in a file") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new SaveIcon());
            saveProject.add(item);

            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setFileFilter(AssembleProject.getFileFilter());
                    fileChooser.setFileView(new FileView() {
                        public Icon getIcon(File f) {
                            if (IoUtils.isAssembleProject(f))
                                return new ImageIcon(RessourcesUtils.getImage("assemble2-project.png"));
                            else
                                return null;
                        }

                        public Boolean isTraversable(File f) {
                            return f.isDirectory() && !IoUtils.isAssembleProject(f);
                        }
                    });
                    if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(Assemble.this.window)) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    if (mediator.getChimeraDriver() != null)
                                        mediator.getChimeraDriver().synchronizeFrom();
                                    currentAssembleProject = new AssembleProject(mediator, fileChooser.getSelectedFile());
                                    currentAssembleProject.save();
                                    AssembleConfig.addRecentEntry(currentAssembleProject.getLocation().getAbsolutePath(), "assemble-file");
                                    updateRecentFilesMenu();
                                    setLastWorkingDirectory(currentAssembleProject.getLocation().getParentFile());
                                    AssembleConfig.saveConfig();
                                    mediator.getAssemble().getFrame().setTitle(fileChooser.getSelectedFile().getName());
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Saving done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });

            item = new JMenuItem("in your account") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new SaveIcon());
            saveProject.add(item);

            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {

                    String project_name = JOptionPane.showInputDialog("Choose a project name...");
                    Map<String,String> request = new HashMap<String, String>();
                    request.put("tool_id", AssembleConfig.getID());
                    request.put("tool_name", "Assemble2");
                    BasicDBObject project = new BasicDBObject();
                    project.put("name", project_name);
                    request.put("project", project.toString());

                    try {
                        StringBuffer allData = new StringBuffer();
                        String answer = null;
                        for (String key:request.keySet()) {
                            if (allData.length() != 0)
                                allData.append("&");
                            allData.append(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(request.get(key), "UTF-8"));
                        }
                        URL url = new URL(AssembleConfig.getWebservicesAddress().get(0)+"/save_project");
                        URLConnection conn = url.openConnection();
                        conn.setDoOutput(true);
                        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                        wr.write(allData.toString());
                        wr.flush();

                        StringBuffer result = new StringBuffer();
                        // Get the response
                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line;

                        while ((line = rd.readLine()) != null)
                            result.append(line+"\n");
                        wr.close();
                        rd.close();
                        answer = result.toString();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

            JMenu exportMenu = new JMenu("Export...") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };

            exportMenu.setIcon(new SaveIcon());

            this.add(exportMenu);

            item = new JMenuItem("RNA Molecule as FASTA file") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure() != null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".fasta") || file.getName().endsWith(".fna") || file.getName().endsWith(".fas");
                        }

                        @Override
                        public String getDescription() {
                            return "FASTA Files (.fasta, .fna, .fas)";
                        }
                    });
                    if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(Assemble.this.window)) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    List<Residue> selectedResidues = mediator.getSecondaryCanvas().getSelectedResidues();
                                    if (selectedResidues.size() != 0) {
                                        StringBuffer subsequence = new StringBuffer();
                                        for (Residue r:selectedResidues)
                                            subsequence.append(r.getSymbol());
                                        FileParser.writeFastaFile(mediator.getSecondaryStructure().getMolecule().getName(), subsequence.toString(), new PrintWriter(fileChooser.getSelectedFile()));
                                    }
                                    else
                                        FileParser.writeFastaFile(mediator.getSecondaryStructure().getMolecule().getName(), mediator.getSecondaryStructure().getMolecule().printSequence(), new PrintWriter(fileChooser.getSelectedFile()));
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            item = new JMenuItem("3D Model as PDB file") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getTertiaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".pdb");
                        }

                        @Override
                        public String getDescription() {
                            return "PDB Files (.pdb)";
                        }
                    });
                    if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(Assemble.this.window)) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    List<Residue> selectedResidues = mediator.getSecondaryCanvas().getSelectedResidues();
                                    if (selectedResidues.size() != 0) {
                                        List<Residue3D> selectedResidues3D = new ArrayList<Residue3D>();
                                        for (Residue r: selectedResidues)
                                            selectedResidues3D.add(mediator.getTertiaryStructure().getResidue3DAt(r.getAbsolutePosition()));
                                        FileParser.writePDBFile(selectedResidues3D, true, new PrintWriter(fileChooser.getSelectedFile()));

                                    }
                                    else
                                        FileParser.writePDBFile(mediator.getTertiaryStructure().getResidues3D(), true, new PrintWriter(fileChooser.getSelectedFile()));
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            item = new JMenuItem("2D Model as BPSEQ file") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".bpseq");
                        }

                        @Override
                        public String getDescription() {
                            return "BPSEQ Files (.bpseq)";
                        }
                    });
                    if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(Assemble.this.window)) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    List<Residue> selectedResidues = mediator.getSecondaryCanvas().getSelectedResidues();
                                    if (selectedResidues.size() != 0) {
                                        PrintWriter pw = new PrintWriter(fileChooser.getSelectedFile());
                                        int shift = selectedResidues.get(0).getAbsolutePosition()-1;
                                        for (Residue r :selectedResidues) {
                                            pw.write(r.getAbsolutePosition()-shift+" "+r.getSymbol()+" "+(r.getSecondaryInteraction() == null ? 0 :  selectedResidues.contains(r.getSecondaryInteraction().getPairedResidue(r)) ? r.getSecondaryInteraction().getPairedResidue(r).getAbsolutePosition()-shift : 0)+"\n");
                                        }
                                        pw.close();
                                    } else
                                        FileParser.writeBPSEQFile(mediator, mediator.getSecondaryStructure(), new PrintWriter(fileChooser.getSelectedFile()));
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            item = new JMenuItem("2D Model as SVG file") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryStructure()!= null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser(getLastWorkingDirectory());
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".svg");
                        }

                        @Override
                        public String getDescription() {
                            return "SVG Files (.svg)";
                        }
                    });
                    if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(Assemble.this.window)) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    PrintWriter pw = new PrintWriter(fileChooser.getSelectedFile());
                                    SecondaryStructure ss = mediator.getSecondaryStructure();
                                    pw.write("<?xml version=\"1.0\"?>\n");
                                    pw.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n");
                                    pw.write("<svg width=\"${m.secondaryCanvas.width}\" height=\"${m.secondaryCanvas.height}\">\n");
                                    pw.write("<title>${ss.name}</title>\n");
                                    for (Helix h: ss.getHelices()) {
                                        for (Residue r: h.getResidues())
                                            if (r.isInsideDrawingArea(mediator.getSecondaryCanvas().getGraphicContext()))
                                                pw.write("<circle cx=\""+String.format("%.02f", r.getCircle().getCenterX())+"\" cy=\""+String.format("%.02f", r.getCircle().getCenterY())+"\" r=\""+r.getCircle().getBounds2D().getWidth() / 2+"\" fill=\"#"+ Integer.toHexString(r.getFinalColor(mediator.getSecondaryCanvas().getGraphicContext()).getRGB()).substring(2)+"\" stroke-width=\"0\"/>\n");
                                        for (BaseBaseInteraction bbi: h.getSecondaryInteractions()) {
                                            if (!bbi.isInsideDrawingArea(mediator.getSecondaryCanvas().getGraphicContext()))
                                                continue;
                                            for (Shape shape: bbi.getShapes())
                                                pw.write(shape.getSVG(bbi) + "\n");
                                        }
                                    }
                                    for (SingleStrand single_strand: ss.getSingleStrands())
                                        for (Residue r: single_strand.getResidues())
                                            if (r.isInsideDrawingArea(mediator.getSecondaryCanvas().getGraphicContext()))
                                                pw.write("<circle cx=\""+String.format("%.02f", r.getCircle().getCenterX())+"\" cy=\""+String.format("%.02f", r.getCircle().getCenterY())+"\" r=\""+r.getCircle().getBounds2D().getWidth() / 2+"\" fill=\"#"+ Integer.toHexString(r.getFinalColor(mediator.getSecondaryCanvas().getGraphicContext()).getRGB()).substring(2)+"\" stroke-width=\"0\"/>\n");
                                    if (mediator.getSecondaryCanvas().getGraphicContext().isTertiaryInteractionsDisplayed()) {
                                        for (BaseBaseInteraction bbi: ss.getTertiaryInteractions()) {
                                            if (SingleHBond.class.isInstance(bbi)  && !mediator.getSecondaryCanvas().getGraphicContext().displaySingleHBonds() || !bbi.isInsideDrawingArea(mediator.getSecondaryCanvas().getGraphicContext()))
                                            continue;
                                            for (Shape shape: bbi.getShapes())
                                                pw.write(shape.getSVG(bbi) + "\n");
                                        }
                                    }
                                    for (BaseBaseInteraction phospho : ss.getPhosphodiesterBonds())
                                        for (Shape shape: phospho.getShapes())
                                            if (phospho.isInsideDrawingArea(mediator.getSecondaryCanvas().getGraphicContext()))
                                                pw.write(shape.getSVG(phospho)+"\n");
                                    pw.write("</svg>");
                                    pw.close();
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            item = new JMenuItem("Alignment as Clustal File") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getAlignmentCanvas().getMainAlignment() != null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final JFileChooser fileChooser = new JFileChooser(getLastWorkingDirectory());
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".aln");
                        }

                        @Override
                        public String getDescription() {
                            return "Clustal (.aln)";
                        }
                    });

                    if (fileChooser.showSaveDialog(Assemble.this.window) == JFileChooser.APPROVE_OPTION) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    List<Symbol> selection = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getSelection();
                                    if (selection.size() != 0) {
                                        int firstIndex = mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(0)),
                                                lastIndex =  mediator.getAlignmentCanvas().getMainAlignment().getBiologicalReferenceSequence().getIndex(selection.get(selection.size()-1));
                                        FileParser.writeClustalFile(mediator.getAlignmentCanvas().getMainAlignment(), new PrintWriter(fileChooser.getSelectedFile()), firstIndex, lastIndex);
                                    }
                                    else
                                        FileParser.writeClustalFile(mediator.getAlignmentCanvas().getMainAlignment(), new PrintWriter(fileChooser.getSelectedFile()), 0, mediator.getAlignmentCanvas().getMainAlignment().getLength() - 1);
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            item = new JMenuItem("Alignment as FASTA File") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getAlignmentCanvas().getMainAlignment() != null);
                    super.paintComponent(graphics);
                }
            };
            item.setIcon(new FileIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    final JFileChooser fileChooser = new JFileChooser(getLastWorkingDirectory());
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setFileHidingEnabled(true);
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().endsWith(".fasta");
                        }

                        @Override
                        public String getDescription() {
                            return "FASTA (.fasta)";
                        }
                    });

                    if (fileChooser.showSaveDialog(Assemble.this.window) == JFileChooser.APPROVE_OPTION) {
                        new javax.swing.SwingWorker() {
                            protected Object doInBackground()  {
                                mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                try {
                                    FileParser.writeFastaFile(mediator.getAlignmentCanvas().getMainAlignment(), new PrintWriter(fileChooser.getSelectedFile()));
                                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Export done.", null, null);
                                    mediator.getSecondaryCanvas().repaint();
                                }
                                catch (Exception e) {
                                    messageBar.printException(e);
                                }
                                mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                return null;
                            }
                        }.execute();
                    }
                }
            });
            exportMenu.add(item);

            configure = new JMenu("Configure...");
            configure.setIcon(new ConfigureIcon());
            this.add(configure);

            configureAssemble = new JMenuItem("Assemble2");
            configureAssemble.setIcon(new WrenchIcon());
            configure.add(configureAssemble);

            configureAssemble.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    List<JComponent> inputs = new ArrayList<JComponent>();

                    inputs.add(new JLabel("Assemble ID: "+AssembleConfig.getID()));

                    JButton b = new JButton("Link to your account");
                    inputs.add(b);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            IoUtils.openBrowser(AssembleConfig.getWebservicesAddress().get(0)+"/account?tool_name=Assemble2&tool_id="+AssembleConfig.getID());
                        }
                    });

                    inputs.add(new JLabel("Chimera Executable"));
                    final JTextField chimeraExecutable = new JTextField(AssembleConfig.getChimeraPath());
                    inputs.add(chimeraExecutable);

                    inputs.add(new JLabel("Web Services Address"));
                    final JComboBox webServiceAddresses = new JComboBox(AssembleConfig.getWebservicesAddress().toArray(new String[]{}));
                    inputs.add(webServiceAddresses);

                    b = new JButton("Add new Address");
                    inputs.add(b);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String address = new JOptionPane().showInputDialog("Add new Address", "http://hostname_or_IP:port");
                            if (address != null && address.trim().length() != 0 && address.trim().startsWith("http://")) {
                                AssembleConfig.addWebservicesAddress(address.trim());
                                webServiceAddresses.addItem(address.trim());
                                webServiceAddresses.setSelectedItem(address.trim());
                            }
                        }
                    });

                    inputs.add(new JLabel("3D Fragments Library"));
                    final JComboBox fragmentsLibrary = new JComboBox(new String[]{"Non redundant",
                            "Redundant"});
                    fragmentsLibrary.setSelectedItem(AssembleConfig.getFragmentsLibrary());
                    inputs.add(fragmentsLibrary);

                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    ((FlowLayout)panel.getLayout()).setHgap(0);
                    final JCheckBox useLocalAlgorithms = new JCheckBox();
                    useLocalAlgorithms.setSelected(AssembleConfig.useLocalAlgorithms());
                    panel.add(useLocalAlgorithms);
                    panel.add(new JLabel("Use algorithms locally (need Docker installed)"));
                    inputs.add(panel);

                    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    ((FlowLayout)panel.getLayout()).setHgap(0);
                    final JCheckBox launchChimera = new JCheckBox();
                    launchChimera.setSelected(AssembleConfig.launchChimeraAtStart());
                    panel.add(launchChimera);
                    panel.add(new JLabel("Launch Chimera at startup"));
                    inputs.add(panel);

                    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    ((FlowLayout)panel.getLayout()).setHgap(0);
                    final JCheckBox popupLateralPanels = new JCheckBox();
                    popupLateralPanels.setSelected(AssembleConfig.popupLateralPanels());
                    panel.add(popupLateralPanels);
                    panel.add(new JLabel("Pop up lateral panels automatically"));
                    inputs.add(panel);

                    if (JOptionPane.OK_OPTION ==  JOptionPane.showConfirmDialog(null, inputs.toArray(new JComponent[]{}), "Configuration", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
                        String param = ((String)webServiceAddresses.getSelectedItem()).trim();
                        if (param != null) {
                            AssembleConfig.setCurrentWebservicesAddress(param.trim());
                            if (!isServerReachable())
                                JOptionPane.showMessageDialog(Assemble.this.window,
                                        "Cannot reach " + AssembleConfig.getWebservicesAddress().get(0),
                                        "Server unreachable",
                                        JOptionPane.WARNING_MESSAGE);
                            else
                                openWebSocket();

                        }

                        String previousChimeraPath = AssembleConfig.getChimeraPath();
                        param = chimeraExecutable.getText().trim();
                        if (param != null && param.trim().length() != 0)
                            AssembleConfig.setChimeraPath(param.trim());

                        AssembleConfig.useLocalAlgorithms(useLocalAlgorithms.isSelected());

                        AssembleConfig.launchChimeraAtStart(launchChimera.isSelected());

                        AssembleConfig.setFragmentsLibrary((String)fragmentsLibrary.getSelectedItem());

                        AssembleConfig.popupLateralPanels(popupLateralPanels.isSelected());

                        try {
                            AssembleConfig.saveConfig();
                            if (!previousChimeraPath.equals(AssembleConfig.getChimeraPath()))
                                new ChimeraDriver(mediator);
                        } catch (BackingStoreException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            JMenuItem configureAlgorithms = new JMenuItem("RNA algorithms");
            configureAlgorithms.setIcon(new Wrench2Icon());
            configure.add(configureAlgorithms);

            configureAlgorithms.addActionListener(new ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent actionEvent) {
                    AlgorithmsConfigurationDialog dialog = new AlgorithmsConfigurationDialog();
                    dialog.pack();
                    IoUtils.centerOnScreen(dialog);
                    dialog.setVisible(true);
                }
            });

            item = new JMenuItem("Quit");
            item.setIcon(new QuitIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (JOptionPane.showConfirmDialog(null, "Are you sure you want to exit Assemble2?", "Confirm exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            AssembleConfig.saveConfig();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        if (mediator.getChimeraDriver() != null)
                            mediator.getChimeraDriver().close();
                        System.exit(0);
                    }
                }
            });
            this.add(item);
        }
    }

    private class ExternalResourcesMenu extends JMenu {

        public ExternalResourcesMenu() {
            super("External Resources");

            JMenuItem item = new JMenuItem("Nucleic Acid Database");
            this.add(item);

            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (ndbWebsite != null) {
                                ndbWebsite.setVisible(true);
                                ndbWebsite.toFront();
                            } else {
                                ndbWebsite = new NDBWebsite();
                            }
                            return null;
                        }
                    }.execute();
                }});

            item = new JMenuItem("RNAcentral");
            this.add(item);

            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (rnaCentralWebsite != null) {
                                rnaCentralWebsite.setVisible(true);
                                rnaCentralWebsite.toFront();
                            } else {
                                rnaCentralWebsite = new RNACentralWebsite();
                            }
                            return null;
                        }
                    }.execute();
                }});

            item = new JMenuItem("mfold Web Server");
            this.add(item);

            item.setIcon(new WebIcon());
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (mfoldWebServer != null) {
                                mfoldWebServer.setVisible(true);
                                mfoldWebServer.toFront();
                            } else {
                                mfoldWebServer = new MfoldWebServer();
                            }
                            return null;
                        }
                    }.execute();
                }});

        }
    }

    private class TutorialsMenu extends JMenu {

        public TutorialsMenu() {
            super("Tutorials");

            final JMenuItem tutorial1 = new JMenuItem("How load data");
            //this.add(tutorial1);

            tutorial1.setIcon(new TutorialIcon());
            /*tutorial1.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (tutorial1.isSelected()) {
                                for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                    if (TutorialsMenu.this.getItem(i) != tutorial1)
                                        TutorialsMenu.this.getItem(i).setSelected(false);
                                mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false); //we need to feed the thread of the tutorial
                                Assemble.HELP_MODE = false; //incompatible
                                java.util.List<String> texts = new ArrayList<String>();
                                java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>();
                                java.util.List<MessagingSystemAction> nextActions = new ArrayList<MessagingSystemAction>();
                                texts.add("Welcome to this tutorial.");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(null);
                                texts.add("To end it, unselect it from the Tutorials menu or click on the red icon.");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(null);
                                texts.add("To move on, we need now to load the 3D stored in the sample file \"1u6b.pdb\".");
                                nextActions.add(new MessagingSystemAction() {
                                    @Override
                                    public void run() {
                                        mediator.getAssemble().loadData(new File(new File(new File(Assemble.getInstallPath(), "samples"), "tertiary structures"), "1u6b.pdb").getAbsolutePath(), "pdb-file");
                                    }
                                });
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                texts.add("Choose the molecular chain B.");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(new MessagingSystemAction() {
                                    @Override
                                    public void run() {
                                        ToolTip tip =  new ToolTip(mediator);
                                        tip.setOrientation(ToolTip.RIGHT);
                                        tip.setResidue(mediator.getSecondaryCanvas().getSecondaryStructure().getResidue(4));
                                        tip.setHeight(40);
                                        tip.setText("Click me!!");
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(tip);
                                    }
                                });
                                texts.add("Click three times on this residue to select its helix");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(new MessagingSystemAction() {
                                    @Override
                                    public void run() {
                                        ToolTip tip =  new ToolTip(mediator);
                                        tip.setOrientation(ToolTip.LEFT);
                                        tip.setResidue(mediator.getSecondaryCanvas().getSecondaryStructure().getResidue(35));
                                        tip.setHeight(40);
                                        tip.setText("Click me!!");
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(tip);
                                    }
                                });
                                texts.add("Now click on the...");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(new MessagingSystemAction() {
                                    @Override
                                    public void run() {
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                texts.add("To finish...");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(null);
                                texts.add("End of the tutorial. You can click on the red icon.");
                                closeActions.add(new MessagingSystemAction() {  //if the user close the message, this is the end of this tutorial
                                    @Override
                                    public void run() {
                                        for (int i= 0 ; i < TutorialsMenu.this.getItemCount() ; i++)
                                            TutorialsMenu.this.getItem(i).setSelected(false);
                                        mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false);
                                        mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                                    }
                                });
                                nextActions.add(null);
                                mediator.getSecondaryCanvas().getMessagingSystem().addThread(texts, closeActions, nextActions);
                                mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(true); //now the messaging system is unresponsive until the user click on a close icon or unselect the tutorial in the menu
                            } else {
                                mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false); //the messaging system display new messages
                                mediator.getSecondaryCanvas().getMessagingSystem().clear(); //we clear the thread of messages from the tutorial
                                mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                            }
                            mediator.getSecondaryCanvas().repaint();
                            return null;
                        }
                    }.execute();
                }});*/


            final JMenuItem tutorial2 = new JMenuItem("How to edit an RNA secondary structure");
            this.add(tutorial2);

            tutorial2.setIcon(new TutorialIcon());
            tutorial2.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (tutorial2Website != null) {
                                tutorial2Website.setVisible(true);
                                tutorial2Website.toFront();
                            } else {
                                tutorial2Website = new Tutorial2Website();
                            }
                            return null;
                        }
                    }.execute();
                }
            });

        }

    }

    private class ToolBarsMenu extends JMenu {

        private Icon displayed =  new DisplayedIcon(),
                hidden = new HiddenIcon();

        private ToolBarsMenu() {
            super("Toolbars");
            final JMenuItem secondaryToolBar = new JMenuItem("2D Toolbar") {
                public void paintComponent(Graphics graphics) {
                    this.setIcon(mediator.getSecondaryCanvas().isDisplaySecondaryStructureToolBar() ? hidden : displayed);
                    super.paintComponent(graphics);
                }
            };
            secondaryToolBar.setIcon(mediator.getSecondaryCanvas().isDisplaySecondaryStructureToolBar() ? hidden : displayed);
            secondaryToolBar.setSelected(mediator.getSecondaryCanvas().isDisplaySecondaryStructureToolBar());
            secondaryToolBar.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    mediator.getSecondaryCanvas().setDisplaySecondaryStructureToolBar(!mediator.getSecondaryCanvas().isDisplaySecondaryStructureToolBar());
                    secondaryToolBar.setSelected(mediator.getSecondaryCanvas().isDisplaySecondaryStructureToolBar());
                    mediator.getSecondaryCanvas().repaint();
                }
            });
            this.add(secondaryToolBar);

            final JMenuItem tertiaryToolBar = new JMenuItem("3D Toolbar") {
                public void paintComponent(Graphics graphics) {
                    this.setIcon(mediator.getSecondaryCanvas().isDisplayTertiaryStructureToolBar() ? hidden : displayed);
                    super.paintComponent(graphics);
                }
            };
            tertiaryToolBar.setIcon(mediator.getSecondaryCanvas().isDisplayTertiaryStructureToolBar() ? hidden : displayed);
            tertiaryToolBar.setSelected(mediator.getSecondaryCanvas().isDisplayTertiaryStructureToolBar());
            tertiaryToolBar.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    mediator.getSecondaryCanvas().setDisplayTertiaryStructureToolBar(!mediator.getSecondaryCanvas().isDisplayTertiaryStructureToolBar());
                    tertiaryToolBar.setSelected(mediator.getSecondaryCanvas().isDisplayTertiaryStructureToolBar());
                    mediator.getSecondaryCanvas().repaint();
                }
            });
            this.add(tertiaryToolBar);

            final JMenuItem activityToolBar = new JMenuItem("Activity Toolbar") {
                public void paintComponent(Graphics graphics) {
                    this.setIcon(mediator.getSecondaryCanvas().isDisplayActivityToolBar() ? hidden : displayed);
                    super.paintComponent(graphics);
                }
            };
            activityToolBar.setIcon(mediator.getSecondaryCanvas().isDisplayActivityToolBar() ? hidden : displayed);
            activityToolBar.setSelected(mediator.getSecondaryCanvas().isDisplayActivityToolBar());
            activityToolBar.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    mediator.getSecondaryCanvas().setDisplayActivityToolBar(!mediator.getSecondaryCanvas().isDisplayActivityToolBar());
                    activityToolBar.setSelected(mediator.getSecondaryCanvas().isDisplayActivityToolBar());
                    mediator.getSecondaryCanvas().repaint();
                }
            });
            this.add(activityToolBar);

            final JMenuItem alignmentToolBar = new JMenuItem("Alignment Toolbar"){
                public void paintComponent(Graphics graphics) {
                    this.setIcon(mediator.getAlignmentCanvas().isDisplayAlignmentToolbar() ? hidden : displayed);
                    super.paintComponent(graphics);
                }
            };
            alignmentToolBar.setIcon(mediator.getAlignmentCanvas().isDisplayAlignmentToolbar() ? hidden : displayed);
            alignmentToolBar.setSelected(mediator.getAlignmentCanvas().isDisplayAlignmentToolbar());
            alignmentToolBar.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    mediator.getAlignmentCanvas().setDisplayAlignmentToolbar(!mediator.getAlignmentCanvas().isDisplayAlignmentToolbar());
                    alignmentToolBar.setSelected(mediator.getAlignmentCanvas().isDisplayAlignmentToolbar());
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            this.add(alignmentToolBar);
        }


    }

    private class ColorsMenu extends JMenu {

        public ColorsMenu() {
            super("Colors");

            JMenu referenceStructureColors = new JMenu("Reference Structure");
            this.add(referenceStructureColors);
            referenceStructureColors.setIcon(new PalleteIcon());

            JMenu themes = new JMenu("Themes");
            themes.setIcon(new PalleteIcon());
            referenceStructureColors.add(themes);

            JMenuItem theme = new AssembleMenuItem("Candies");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(0,192,255);
                    U_Color = new Color(192,128,128);
                    G_Color = new Color(128,192,0);
                    C_Color = new Color(255,0,255);
                    SecondaryInteraction_Color = new Color(88, 191, 191);
                    TertiaryInteraction_Color = new Color(88, 191, 191);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            theme = new AssembleMenuItem("Grapes");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(128,128,0);
                    U_Color = new Color(128,128,128);
                    G_Color = new Color(192,0,0);
                    C_Color = new Color(255,128,0);
                    SecondaryInteraction_Color = new Color(192,128,0);
                    TertiaryInteraction_Color = new Color(192,128,0);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            theme = new AssembleMenuItem("ColorBrewer1");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(127,201,127);
                    U_Color = new Color(190,174,212);
                    G_Color = new Color(253,192,134);
                    C_Color = new Color(255,255,153);
                    SecondaryInteraction_Color = new Color(56,108,176);
                    TertiaryInteraction_Color = new Color(240,2,127);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            theme = new AssembleMenuItem("ColorBrewer2");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(27,158,119);
                    U_Color = new Color(217,95,2);
                    G_Color = new Color(117,112,179);
                    C_Color = new Color(231,41,138);
                    SecondaryInteraction_Color = new Color(102,166,30);
                    TertiaryInteraction_Color = new Color(230,171,2);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            theme = new AssembleMenuItem("ColorBrewer3");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(166,206,227);
                    U_Color = new Color(31,120,180);
                    G_Color = new Color(178,223,138);
                    C_Color = new Color(51,160,44);
                    SecondaryInteraction_Color = new Color(251,154,153);
                    TertiaryInteraction_Color = new Color(227,26,28);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            theme = new AssembleMenuItem("ColorBrewer4");
            theme.setIcon(new BucketIcon());
            theme.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    A_Color = new Color(102,194,165);
                    U_Color = new Color(252,141,98);
                    G_Color = new Color(141,160,203);
                    C_Color = new Color(231,138,195);
                    SecondaryInteraction_Color = new Color(166,216,84);
                    TertiaryInteraction_Color = new Color(255,217,47);
                    if (mediator.getSecondaryCanvas().getGraphicContext() != null)
                        mediator.getSecondaryCanvas().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getSecondaryCanvas().repaint();
                    if (mediator.getFoldingLandscape().getGraphicContext() != null)
                        mediator.getFoldingLandscape().getGraphicContext().displayQuantitativeValues(false);
                    mediator.getFoldingLandscape().repaint();
                    mediator.getAlignmentCanvas().repaint();
                }
            });
            themes.add(theme);

            JMenu residues = new JMenu("Residues");
            residues.setIcon(new PalleteIcon());
            referenceStructureColors.add(residues);
            residues.add(new ColorMenu(mediator, "Adenine", ColorMenu.ADENINE));
            residues.add(new ColorMenu(mediator, "Uracil", ColorMenu.URACIL));
            residues.add(new ColorMenu(mediator, "Guanine", ColorMenu.GUANINE));
            residues.add(new ColorMenu(mediator, "Cytosine", ColorMenu.CYTOSINE));

            JMenu interactions = new JMenu("Interactions");
            interactions.setIcon(new PalleteIcon());
            referenceStructureColors.add(interactions);
            interactions.add(new ColorMenu(mediator, "Secondary", ColorMenu.SECONDARY_INTERACTIONS));
            interactions.add(new ColorMenu(mediator, "Tertiary", ColorMenu.TERTIARY_INTERACTIONS));

            JMenuItem setAsDefault = new AssembleMenuItem("Set as Default");
            setAsDefault.setIcon(new UserIcon());
            setAsDefault.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    AssembleConfig.saveUserColors();
                    try {
                        AssembleConfig.saveConfig();
                    } catch (BackingStoreException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            referenceStructureColors.add(setAsDefault);

            JMenu consensusStructureValues = new JMenu("Consensus Structure");
            consensusStructureValues.setIcon(new PalleteIcon());
            this.add(consensusStructureValues);

            consensusStructureValues.add(new ColorMenu(mediator, "Single Strands", ColorMenu.CONSENSUS_STRUCTURE_SINGLE_STRANDS));

            JMenuItem shuffleColors = new JMenuItem("Shuffle Colors");
            shuffleColors.setIcon(new BucketIcon());
            shuffleColors.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    mediator.getAlignmentCanvas().getMainAlignment().getConsensusStructure().changeColors();
                    mediator.getAlignmentCanvas().repaint();
                    if (mediator.getSecondaryCanvas().getSecondaryStructureToolBar().getRenderingMode() == mediator.getSecondaryCanvas().getSecondaryStructureToolBar().CONSENSUS_STRUCTURE) {
                        mediator.getSecondaryCanvas().repaint();
                        mediator.getFoldingLandscape().repaint();
                    }
                }
            });

            consensusStructureValues.add(shuffleColors);

            JMenu quantitativeValues = new JMenu("Quantitative Data");
            quantitativeValues.setIcon(new QuantitativeDataIcon());
            this.add(quantitativeValues);
            quantitativeValues.add(new ColorMenu(mediator, "No Value", ColorMenu.QUANTITATIVE_VALUES_NO_VALUE));
            quantitativeValues.add(new ColorMenu(mediator, "Min", ColorMenu.QUANTITATIVE_VALUES_START_GRADIENT));
            quantitativeValues.add(new ColorMenu(mediator, "Max", ColorMenu.QUANTITATIVE_VALUES_END_GRADIENT));

            qualitativeColorMenu = new JMenu("Qualitative Data") {
                public void paintComponent(Graphics graphics) {
                    this.setEnabled(mediator.getSecondaryCanvas().getGraphicContext()!= null && !mediator.getSecondaryCanvas().getGraphicContext().getQualitativeNames().isEmpty());
                    super.paintComponent(graphics);
                }
            };
            qualitativeColorMenu.setIcon(new QualitativeDataIcon());
            this.add(qualitativeColorMenu);

            JMenu bpsProbs = new JMenu("Base Pairing Probabilities");
            bpsProbs.setIcon(new LineChartIcon());
            this.add(bpsProbs);
            bpsProbs.add(new ColorMenu(mediator, "Min", ColorMenu.BP_PROBS_START_GRADIENT));
            bpsProbs.add(new ColorMenu(mediator, "Max", ColorMenu.BP_PROBS_END_GRADIENT));

        }

    }

    private class AboutMenu extends JMenu {

        private AboutMenu() {
            super("About");
            JMenuItem about = new JMenuItem("About Assemble2");
            about.setIcon(new AboutIcon());
            this.add(about);
            about.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (AssembleConfig.showWelcomeDialog()) {
                        WelcomeDialog dialog = new WelcomeDialog(mediator);
                        dialog.pack();
                        dialog.setResizable(false);
                        IoUtils.centerOnScreen(dialog);
                        dialog.setVisible(true);
                    } else
                        new fr.unistra.ibmc.assemble2.gui.SplashScreen(false);
                }
            });
            this.add(new OpenWebPageItem("The Assemble2 Website", "http://bioinformatics.org/assemble/"));
            this.add(new OpenWebPageItem("The Assemble2 Blog", "http://assemble2.wordpress.com/"));
            //this.add(new OpenWebPageItem(new GooglePlusIcon(), "Google Plus", "https://www.google.fr/url?sa=t&rct=j&q=&esrc=s&source=web&cd=2&ved=0CEEQFjAB&url=https%3A%2F%2Fplus.google.com%2F116605164961471297311&ei=ujFgUvCrKYO57Abv34GIDw&usg=AFQjCNFQRhGcOGKrbdpPDmdSTo58_GGObg&sig2=3Ig_VT-egILNYbS4XP9jHg&bvm=bv.54176721,d.ZGU"));
            this.add(new OpenWebPageItem(new TwitterIcon(), "Twitter", "https://twitter.com/fjossinet"));

        }

        private class OpenWebPageItem extends JMenuItem implements ActionListener {

            private String url;

            public OpenWebPageItem(Icon icon, String webPageTitle, String url) {
                super(webPageTitle);
                this.setIcon(icon);
                this.url = url;
                this.addActionListener(this);
            }

            public OpenWebPageItem(String webPageTitle, String url) {
                this(new WebIcon(), webPageTitle, url);
            }

            public void actionPerformed(ActionEvent actionEvent) {
                IoUtils.openBrowser(this.url);
            }
        }
    }

    private class PreferencesMenu extends JMenu implements ActionListener {
        private JMenuItem keyManager = new JMenuItem("Keyboard Shortcuts");
        private JMenuItem renderingOptions = new JMenuItem("3D Rendering Parameters");

        private PreferencesMenu() {
            super("Preferences");
            this.add(keyManager);
            this.add(renderingOptions);

            keyManager.addActionListener(this);
            renderingOptions.addActionListener(this);

            keyManager.setIcon(RessourcesUtils.getIcon("22/keyboard.png"));
            renderingOptions.setIcon(RessourcesUtils.getIcon("22/parameters.png"));
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
        }
    }

    private class FlatButton extends JButton {
        private FlatButton(String url, String name) {
            this(RessourcesUtils.getIcon(url), name);
        }

        private FlatButton(Icon image, String name) {
            super(image);
            this.setMargin(new Insets(0, 0, 0, 0));
            this.setBorderPainted(false);
            this.setBackground(Color.WHITE);
            this.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {

                    setBackground(Color.LIGHT_GRAY);
                    setBorderPainted(true);
                }

                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                    setBorderPainted(false);
                }
            });

            this.setToolTipText(name);

        }

        public Point getToolTipLocation(MouseEvent e) {
            return new Point(0, 0);
        }
    }


    private class CreateRNAMotifPanel extends JXPanel {

        private JLabel snapshot;
        private BufferedImage image;

        private CreateRNAMotifPanel() {
            this.setLayout(new MigLayout("", "[left]"));
            this.setBackground(Color.WHITE);
            this.snapshot = new JLabel();
            this.snapshot.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            snapshot.setIcon(new PhotoIcon());
            this.add(this.snapshot, "span");
            JButton capture = new JButton("capture");
            capture.setBackground(Color.WHITE);
            capture.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (mediator.getRna2DViewer().getSecondaryCanvas().getSecondaryStructure()!= null){
                        image = mediator.getRna2DViewer().getSecondaryCanvas().getImage(true);
                        if (image !=null) {
                            int width = image.getWidth(), height = image.getHeight();
                            float ratio = width >= height ? (float)width/(float)300 : (float)height/(float)300;
                            if (ratio > 1)//if the image is smaller than the size of 150px, we do nothing
                                snapshot.setIcon(new ImageIcon(image.getScaledInstance((int)((float)width/ratio),(int)((float)height/ratio),Image.SCALE_SMOOTH)));
                            else
                                snapshot.setIcon(new ImageIcon(image));
                        }
                    }
                }
            });
            this.add(capture, "span");
            this.add(new JLabel("RNA Motif Details"),"split, span, gaptop 20");
            this.add(new JSeparator(), "growx, wrap, gaptop 20");
            this.add(new JLabel("Name"), "gap 10");
            final JTextField motifName = new JTextField(10);
            this.add(motifName,"span");
            this.add(new JLabel("PubMed ID (Optional)"), "gap 10");
            final JTextField pmid = new JTextField(10);
            this.add(pmid,"span");
            this.add(new JLabel("Category"), "gap 10");
            final List<String> motifCategories = new ArrayList<String>();
            for (File motifDir:Assemble.getMotifsDirectory().listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory();
                }
            })) {
                for (File motif:motifDir.listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        return f.getName().endsWith(".rnaml");
                    }
                })) {
                    if (!motifCategories.contains(motifDir.getName()))
                        motifCategories.add(motifDir.getName());
                }
            }

            final JComboBox motifCategory = new JComboBox(motifCategories.toArray());
            this.add(motifCategory);
            JButton b = new JButton("Create new category");
            b.setBackground(Color.WHITE);
            this.add(b, "wrap");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    String newCategoryName = JOptionPane.showInputDialog(Assemble.this.window,"Choose a new category name");
                    if (newCategoryName != null && newCategoryName.length() != 0){
                        if (motifCategories.contains(newCategoryName))
                            JOptionPane.showMessageDialog(Assemble.this.window,"This category already exists!!");
                        else {
                            motifCategory.addItem(newCategoryName);
                            motifCategory.setSelectedItem(newCategoryName);
                        }
                    }
                }
            });

            this.add(new JLabel("Comment"), "span, gap 10");
            final JTextArea comment = new JTextArea(10,30);
            this.add(new JScrollPane(comment), "span, gap 10");

            final JButton createMotif = new JButton("Create motif");
            createMotif.setBackground(Color.WHITE);
            createMotif.addActionListener(new ActionListener() {


                /**
                 * This class is used to gather all the Residue3D objects for a given motif.
                 * These objects are ordered and the final location for all these residues is stored to be able to generate the corresponding subsequences.
                 */

                class MyTreeSet extends TreeSet<Residue3D> {
                    private Molecule m;
                    private Location location;

                    private MyTreeSet(final Molecule m) {
                        super(new Comparator() {

                            public int compare(Object o1, Object o2) {
                                return ((Residue3D)o1).getAbsolutePosition() - ((Residue3D)o2).getAbsolutePosition();
                            }

                            public boolean equals(Object obj) { // not used by the sort
                                return false;
                            }

                        });
                        this.m = m;
                        this.location = new Location();
                    }


                    /**
                     * If the residue object is stored successfully, its position in the original molecule is added to the glocal location.
                     * @param position
                     * @param residue3D
                     */

                    public void add(int position,Residue3D residue3D) {
                        if (super.add(residue3D))
                            this.location.add(position);
                    }


                    /**
                     * Return all the subsequences associated with the Location for the original molecule
                     * @return
                     */

                    public List<LocatedString> getSubSequences() {
                        List<LocatedString> subSequences = new ArrayList<LocatedString>();
                        String sequence = this.m.printSequence();
                        int[] boundaries = this.location.getEnds();
                        for (int i=0; i<boundaries.length-1;i+=2)
                            subSequences.add(new LocatedString(sequence.substring(boundaries[i]-1,boundaries[i+1]),new Location(boundaries[i], boundaries[i+1])));
                        return subSequences;
                    }
                }

                class LocatedString {
                    private String subSequence;
                    private Location location;

                    LocatedString(String subSequence, Location l) {
                        this.subSequence = subSequence;
                        this.location = l;
                    }
                }

                public void actionPerformed(final ActionEvent e) {
                    new SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            try {
                                if (mediator.getSecondaryStructure()== null) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"No 2D loaded");
                                    return null;
                                }
                                if (mediator.getTertiaryStructure() == null) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"No 3D loaded");
                                    return null;
                                }
                                if (CreateRNAMotifPanel.this.image == null) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"You have to do a capture of your motif");
                                    return null;
                                }
                                if (motifCategory.getSelectedItem() == null || motifCategory.getSelectedItem().toString().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"You have to choose a valid category for your motif");
                                    return null;
                                }
                                if (motifName == null || motifName.getText().trim().length() == 0) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"You have to choose a valid name for your motif");
                                    return null;
                                }
                                //We check is a motif with the same name doesn't already exist
                                //creation of the rnaml file
                                File motifCategoryDir = new File(getMotifsDirectory(),(String)motifCategory.getSelectedItem());
                                if (!motifCategoryDir.exists())
                                    motifCategoryDir.mkdir();
                                File rnamlFile2D = new File(motifCategoryDir,motifName.getText().trim()+".rnaml");
                                if (rnamlFile2D.exists()) {
                                    JOptionPane.showMessageDialog(Assemble.this.window,"A motif with the name "+motifName.getText().trim()+" already exists!!");
                                    return null;
                                }
                                rnamlFile2D.createNewFile();
                                Map<Molecule,MyTreeSet> sortedResidues3D = new HashMap<Molecule,MyTreeSet>();
                                for (Residue r: mediator.getRna2DViewer().getSecondaryCanvas().getSelectedResidues()) {
                                    MyTreeSet _residues3D = sortedResidues3D.get(mediator.getSecondaryStructure().getMolecule());
                                    int position = r.getAbsolutePosition();
                                    if (_residues3D != null)
                                        _residues3D.add(position,mediator.getTertiaryStructure().getResidue3DAt(position));
                                    else {
                                        _residues3D = new MyTreeSet(mediator.getSecondaryStructure().getMolecule());
                                        _residues3D.add(position,mediator.getTertiaryStructure().getResidue3DAt(position));
                                        sortedResidues3D.put(mediator.getSecondaryStructure().getMolecule(),_residues3D);
                                    }
                                }

                                Element rnaml =  new Element("rnaml");
                                Document doc = new Document(rnaml);
                                if (comment.getText() != null && comment.getText().trim().length() != 0)
                                    rnaml.setAttribute("comment", comment.getText().trim());
                                if (pmid.getText() != null) {
                                    String _pmid = pmid.getText().trim();
                                    if (_pmid.length() != 0) {
                                        Element reference = new Element("reference");
                                        Element pubmed_id = new Element("pubmed-id");
                                        reference.addContent(pubmed_id);
                                        pubmed_id.addContent( _pmid);
                                        rnaml.addContent(reference);
                                    }
                                }
                                Element tertiaryStructure = new Element("tertiary-structure");
                                tertiaryStructure.setAttribute("name",motifName.getText());
                                Element structureAnnotation = new Element("structure-annotation");
                                structureAnnotation.setAttribute("name",motifName.getText());
                                StringBuffer moleculeIds = new StringBuffer();
                                for (Map.Entry<Molecule,MyTreeSet> _e:sortedResidues3D.entrySet()) {
                                    List<LocatedString> subSequences = _e.getValue().getSubSequences();
                                    for (LocatedString s:subSequences) {
                                        Element molecule = new Element("molecule");
                                        molecule.setAttribute("id",_e.getKey().getName()+subSequences.indexOf(s));
                                        moleculeIds.append(_e.getKey().getName()+subSequences.indexOf(s)+" ");
                                        molecule.setAttribute("type","rna");
                                        molecule.setAttribute("start",""+s.location.getStart());
                                        molecule.setAttribute("end",""+s.location.getEnd());
                                        rnaml.addContent(molecule);
                                        Element identity = new Element("identity");
                                        molecule.addContent(identity);
                                        Element name = new Element("name");
                                        identity.addContent(name);
                                        name.addContent(_e.getKey().getName()+subSequences.indexOf(s));
                                        Element sequence = new Element("sequence");
                                        molecule.addContent(sequence);
                                        sequence.setAttribute("length",""+s.subSequence.length());
                                        Element seqData = new Element("seq-data");
                                        seqData.addContent(s.subSequence);
                                        sequence.addContent(seqData);
                                        for (Residue3D r: _e.getValue()) {
                                            int position = r.getAbsolutePosition();
                                            if (s.location.hasPosition(position)) {
                                                Element base = new Element("base");
                                                base.setAttribute("molecule-id",_e.getKey().getName()+subSequences.indexOf(s));
                                                tertiaryStructure.addContent(base);
                                                int[] ends = s.location.getEnds();
                                                for (int i=0 ; i< ends.length-1;i+=2)
                                                    if (position >=ends[i] && position <= ends[i+1]) {
                                                        base.setAttribute("id",""+(position-ends[i]+1));
                                                        break;
                                                    }
                                                for (Residue3D.Atom a:r.getAtoms()) {
                                                    if (a.hasCoordinatesFilled()) {
                                                        Element atom = new Element("atom");
                                                        atom.setAttribute("type",a.getName());
                                                        atom.setAttribute("x",""+a.getX());
                                                        atom.setAttribute("y",""+a.getY());
                                                        atom.setAttribute("z",""+a.getZ());
                                                        base.addContent(atom);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                tertiaryStructure.setAttribute("molecule-ids",moleculeIds.toString().trim());
                                rnaml.addContent(tertiaryStructure);

                                structureAnnotation.setAttribute("molecule-ids",moleculeIds.toString().trim());

                                List<BaseBaseInteraction> interactionsToExport = mediator.getRna2DViewer().getSecondaryCanvas().getGraphicContext().getSelectedInteractionsFromMotif();

                                for (BaseBaseInteraction interaction:interactionsToExport) {
                                    if (BaseBaseInteraction.class.isInstance(interaction)) {
                                        Molecule molecule = interaction.getResidue().getMolecule();
                                        MyTreeSet treeSet = sortedResidues3D.get(molecule),
                                                partnerTreeSet = sortedResidues3D.get(molecule);
                                        Element basePair = new Element("base-pair");
                                        structureAnnotation.addContent(basePair);
                                        List<LocatedString> subSequences = treeSet.getSubSequences();
                                        int position = interaction.getResidue().getAbsolutePosition();
                                        basePair.setAttribute("orientation",""+interaction.getOrientation());
                                        for (LocatedString s:subSequences)
                                            if (s.location.hasPosition(position)) {
                                                basePair.setAttribute("molecule1-id",molecule.getName()+subSequences.indexOf(s));
                                                int[] ends = s.location.getEnds();
                                                for (int i=0 ; i< ends.length-1;i+=2)
                                                    if (position >=ends[i] && position <= ends[i+1]) {
                                                        basePair.setAttribute("base1-id",""+(position-ends[i]+1));
                                                        break;
                                                    }
                                                break;
                                            }

                                        basePair.setAttribute("edge1",""+interaction.getEdge(interaction.getResidue()));
                                        subSequences = partnerTreeSet.getSubSequences();
                                        position = interaction.getPartnerResidue().getAbsolutePosition();
                                        for (LocatedString s:subSequences)
                                            if (s.location.hasPosition(position)) {
                                                basePair.setAttribute("molecule2-id",molecule.getName()+subSequences.indexOf(s));
                                                int[] ends = s.location.getEnds();
                                                for (int i=0 ; i< ends.length-1;i+=2)
                                                    if (position >=ends[i] && position <= ends[i+1]) {
                                                        basePair.setAttribute("base2-id",""+(position-ends[i]+1));
                                                        break;
                                                    }
                                                break;
                                            }
                                        basePair.setAttribute("edge2",""+interaction.getEdge(interaction.getPartnerResidue()));

                                    }
                                }
                                rnaml.addContent(structureAnnotation);

                                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

                                BufferedWriter out =new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rnamlFile2D), Charset.forName("UTF-8")));
                                out.write(outputter.outputString(doc));
                                out.close();

                                //creation of the png file
                                File pngFile = new File(motifCategoryDir,motifName.getText().trim()+".png");
                                ImageIO.write(image, "png", pngFile);

                                //creation of the PDB file to store the full 3D
                                FileParser.writePDBFile(mediator.getTertiaryStructure().getResidues3D(), false, new FileWriter(new File(motifCategoryDir,motifName.getText().trim()+".pdb")));

                                myRNAMotifsPanel.addNewIconMotif((String)motifCategory.getSelectedItem(),motifName.getText().trim());
                                mediator.getToolWindowManager().getToolWindow("My RNA Motifs").setActive(true);
                                mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("3D fragment created.", null, null);
                                mediator.getSecondaryCanvas().repaint();
                            } catch (IOException e1) {
                                ErrorInfo errorInfo = new ErrorInfo(
                                        "Error",
                                        "Cannot create the RNA motif!!",
                                        null,
                                        null,
                                        e1,
                                        Level.SEVERE,
                                        null);
                                JXErrorPane.showDialog(null,errorInfo);
                            }
                            return null;
                        }
                    }.execute();
                }
            });
            this.add(createMotif, "span, gap 10");
        }

    }


    private class AssembleDisplayPanel extends JXPanel {
        private AssembleDisplayPanel() {
            this.setLayout(new MigLayout("", "[left]"));
            this.setBackground(Color.WHITE);
        }
    }

    private class AssembleSelectionPanel extends JXPanel {
        private AssembleSelectionPanel() {
            this.setLayout(new MigLayout("", "[left]"));
            this.setBackground(Color.WHITE);
        }
    }

    private class AssembleEditionPanel extends JXPanel {
        private AssembleEditionPanel() {
            this.setLayout(new MigLayout("", "[left]"));
            this.setBackground(Color.WHITE);
        }
    }

    class MessageBar extends JToolBar implements fr.unistra.ibmc.assemble2.gui.ProgressMonitor {

        private javax.swing.JTextField message;
        private JProgressBar progressBar;
        private MemoryMonitor memoryMonitor;
        private JLabel warning, activity;
        private Exception currentException;
        private String messageAssociatedToTheException;
        private javax.swing.Timer errorTimer, activityTimer;

        MessageBar() {
            this.setFloatable(false);
            this.setBackground(Color.white);
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.errorTimer = new javax.swing.Timer(500,new ActionListener() {
                private boolean b = true;
                public void actionPerformed(ActionEvent actionEvent) {

                    this.b = !this.b;
                }
            });
            this.activityTimer = new javax.swing.Timer(500,new ActionListener() {
                private boolean b = true;
                public void actionPerformed(ActionEvent actionEvent) {

                    this.b = !this.b;
                }
            });
            this.message = new javax.swing.JTextField("Assemble2 ready...");
            this.message.setBackground(Color.white);
            this.message.setEditable(false);
            this.add(this.message);
            this.add(Box.createRigidArea(new Dimension(20,0)));

            this.memoryMonitor = new MemoryMonitor();
            this.add(this.memoryMonitor);
            this.memoryMonitor.start();
        }

        public void printMessage(String message) {
            this.message.setText(message);
        }

        public void printException(Exception e) {
            e.printStackTrace();
            this.errorTimer.start();
            this.currentException = e;
            this.messageAssociatedToTheException = e.getMessage();
            this.warning.setToolTipText("Click me to get error details");
        }
    }

    private void load3DStructure(final String pdbID, final boolean displayInAssemble) {

    }

    public static void main(String[] args) throws BackingStoreException, IOException, JDOMException {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (args.length == 0 || !"start".equals(args[0])) {
            JOptionPane.showMessageDialog(null,"To start Assemble, double-click on one of the launch files in the Assemble directory.");
            System.exit(0);
        }
        else {
            AssembleConfig.loadConfig();
            new fr.unistra.ibmc.assemble2.gui.SplashScreen(true);
        }
    }

    public class PreferencesPanel extends JXTaskPaneContainer {

        public PreferencesPanel() {

            JXTaskPane taskPane = new JXTaskPane();
            taskPane.setTitle("General Options");
            taskPane.add(new GeneralOptions());
            this.add(taskPane);

            taskPane = new JXTaskPane();
            taskPane.setTitle("2D Panel Options");
            //taskPane.add(new Model2DRendering());
            this.add(taskPane);

        }

        private class GeneralOptions extends JXPanel {

            private GeneralOptions() {
                this.setLayout(new VerticalLayout(5));
                JPanel panel = new JPanel();
                JComboBox choices = new JComboBox(new String[]{"tutu","titi"});
                choices.setSelectedIndex(0);
                choices.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                    }
                });
                panel.add(choices);
                panel.add(new JLabel("for shortcuts"));
                this.add(panel);
            }
        }

/*
        private class Model2DRendering extends JXPanel {

            private Model2DRendering() {
                this.setLayout(new VerticalLayout(5));
                final JCheckBox checkbox0= new JCheckBox("Display Name of Helices",false);
                this.add(checkbox0);
                checkbox0.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.DRAW_HELICES_NAMES = checkbox0.isSelected();
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                final JCheckBox checkbox1= new JCheckBox("Display Single HBonds",true);
                this.add(checkbox1);
                checkbox1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mediator.getRna2DViewer().getSecondaryCanvas().addFamily("SingleHBond");
                    }
                });
                final JCheckBox checkbox2= new JCheckBox("Display Tertiary Interactions",true);
                this.add(checkbox2);
                checkbox2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        mediator.getRna2DViewer().getSecondaryCanvas().displayTertiaryInteractions(checkbox2.isSelected());
                    }
                });
                final JCheckBox checkbox3= new JCheckBox("Compute Pseudoknots/InterMolecular Helices as Tertiary Interactions",!Paradise.COPY_PSEUDOKNOTS_AND_INTER_MOLECULAR_HELICES);
                this.add(checkbox3);
                checkbox3.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Paradise.COPY_PSEUDOKNOTS_AND_INTER_MOLECULAR_HELICES = !checkbox3.isSelected();
                    }
                });
                final JCheckBox checkbox4= new JCheckBox("Use Absolute Numbering System",true);
                this.add(checkbox4);
                checkbox4.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.USE_ABSOLUTE_NUMBERING_SYSTEM = checkbox4.isSelected();
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                        mediator.getSecondaryStructureNavigator().update();
                    }
                });

                this.add(new JLabel("Numbering frequency"));
                JXPanel choicesPanel = new JXPanel();
                ButtonGroup group = new ButtonGroup();
                JRadioButton b = new JRadioButton("1");
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.NUMBERING_FREQUENCY = 1;
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                group.add(b);
                choicesPanel.add(b);
                b =  new JRadioButton("2");
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.NUMBERING_FREQUENCY = 2;
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                group.add(b);
                choicesPanel.add(b);
                b =  new JRadioButton("3");
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.NUMBERING_FREQUENCY = 3;
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                group.add(b);
                choicesPanel.add(b);
                b =  new JRadioButton("4");
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.NUMBERING_FREQUENCY = 4;
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                group.add(b);
                choicesPanel.add(b);
                b =  new JRadioButton("5");
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Assemble.NUMBERING_FREQUENCY = 5;
                        mediator.getRna2DViewer().getSecondaryCanvas().repaint();
                    }
                });
                b.setSelected(true);
                group.add(b);
                choicesPanel.add(b);
                this.add(choicesPanel);

            }
        }
*/

    }

    public static void setLastWorkingDirectory(File f) {
        lastFilePath = f.isDirectory() ? f:f.getParentFile();
    }

    public static File getLastWorkingDirectory() {
        return lastFilePath;
    }

    public void loadPDBID(String pdbID) {
        mediator.clearSession();
        BufferedReader in = null;
        StringBuffer content = new StringBuffer();
        String moleculeName = null;
        try {
            URL url = new URL("http://www.rcsb.org/pdb/download/downloadFile.do?fileFormat=pdb&compression=NO&structureId="+pdbID);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = null;
            while ((str = in.readLine()) != null) {
                if (moleculeName == null && str.trim().startsWith("TITLE"))
                    moleculeName = str.trim().substring(10).trim();
                content.append(str+"\n");
            }
            in.close();
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("PDB entry downloaded.", null, null);
            mediator.getSecondaryCanvas().repaint();
            loadTertiaryStructures(FileParser.parsePDB(mediator, new StringReader(content.toString())));
            ((JXFrame)window).setTitle(moleculeName);
            AssembleConfig.addRecentEntry(pdbID, "pdb-db");
            updateRecentFilesMenu();
            AssembleConfig.saveConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
    }

    public void loadRfamID(String rfamID) {
        mediator.clearSession();
        BufferedReader in = null;
        StringBuffer content = new StringBuffer();
        try {
            URL url = new URL("http://rfam.xfam.org/family/"+rfamID+"/alignment?format=stockholm&acc="+rfamID+"&download=0");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = null;
            while ((str = in.readLine()) != null)
                content.append(str+"\n");
            in.close();
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("RFAM entry downloaded.", null, null);
            mediator.getSecondaryCanvas().repaint();
            FileParser.parseStockholm(new StringReader(content.toString()), mediator, null);
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Done.", null, null);
            mediator.getSecondaryCanvas().repaint();
            AssembleConfig.addRecentEntry(rfamID, "rfam-db");
            updateRecentFilesMenu();
            AssembleConfig.saveConfig();
        } catch (Exception ex) {
            messageBar.printException(ex);
        }
        ((JXFrame)window).setTitle("RFAM:"+rfamID);
        mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
    }

    private class Tutorial2Website extends JFrame {
        private JFXPanel jfxPanel;

        private Tutorial2Website() {
            this.setTitle("How to edit an RNA secondary structure");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight());
            this.setLayout(new BorderLayout());
            jfxPanel = new JFXPanel();
            this.createScene();
            this.add(jfxPanel, BorderLayout.CENTER);
            this.setVisible(true);
            this.toFront();
        }

        private void createScene() {
            PlatformImpl.startup(new Runnable() {
                @Override
                public void run() {
                    Stage stage = new Stage();
                    final BorderPane root = new BorderPane();

                    stage.setTitle("How to edit an RNA secondary structure");

                    Scene scene = new Scene(new Group());
                    stage.setScene(scene);

                    // Set up the embedded browser:
                    WebView browser = new WebView();
                    final WebEngine webEngine = browser.getEngine();
                    webEngine.load("file:///Users/fjossinet/Development_projects/assemble2/website/howto_edit2d.html");

                    root.setCenter(browser);


                    scene.setRoot(root);

                    jfxPanel.setScene(scene);

                    webEngine.getLoadWorker().stateProperty().addListener(
                            new javafx.beans.value.ChangeListener<Worker.State>() {
                                public void stateChanged(ChangeEvent e) {

                                }

                                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {

                                        org.w3c.dom.events.EventListener listener = new org.w3c.dom.events.EventListener() {
                                            @Override
                                            public void handleEvent(org.w3c.dom.events.Event ev) {
                                                String domEventType = ev.getType();
                                                if (domEventType.equals("click")) {
                                                    new javax.swing.SwingWorker() {
                                                        @Override
                                                        protected Object doInBackground() {
                                                            try {
                                                                Assemble.this.loadData(new File(new File(new File(Assemble.getInstallPath(), "samples"), "secondary structures"), "D.radiodurans_RNaseP_from_CRW_database.ct").getAbsolutePath(), "ct-file");
                                                            }
                                                            catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                            return null;
                                                        }
                                                    }.execute();
                                                }
                                            }
                                        };

                                        org.w3c.dom.Document doc = webEngine.getDocument();
                                        NodeList links = doc.getElementsByTagName("a");
                                        for (int i=0 ; i < links.getLength() ; i++) {
                                            org.w3c.dom.Node link = (org.w3c.dom.Node)links.item(i);
                                            if (link.getTextContent().equals("load a secondary structure")) {
                                                ((org.w3c.dom.events.EventTarget)link).addEventListener("click", listener, false);
                                            }

                                        }
                                    }
                                }
                            });
                }
            });
        }

    }

    private class NDBWebsite extends JFrame {
        private JFXPanel jfxPanel;
        private String currentPDBID;

        private NDBWebsite() {
            this.setTitle("Nucleic Acid Database");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight());
            this.setLayout(new BorderLayout());
            jfxPanel = new JFXPanel();
            this.createScene();
            this.add(jfxPanel, BorderLayout.CENTER);
            this.setVisible(true);
            this.toFront();
        }

        private void createScene() {
            PlatformImpl.startup(new Runnable() {
                @Override
                public void run() {
                    Stage stage = new Stage();
                    final BorderPane root = new BorderPane();

                    stage.setTitle("Nucleic Acid Database");

                    Scene scene = new Scene(new Group());
                    stage.setScene(scene);

                    // Set up the embedded browser:
                    WebView browser = new WebView();
                    final WebEngine webEngine = browser.getEngine();
                    webEngine.load("http://ndbserver.rutgers.edu/service/ndb/atlas/gallery/rna?polType=all&rnaFunc=all&protFunc=all&strGalType=rna&expMeth=all&seqType=all&galType=table&start=0&limit=50");

                    root.setCenter(browser);

                    javafx.scene.control.Button home = new javafx.scene.control.Button("Home", fontAwesome.create(FontAwesome.Glyph.HOME));
                    home.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {
                            webEngine.load("http://ndbserver.rutgers.edu/service/ndb/atlas/gallery/rna?polType=all&rnaFunc=all&protFunc=all&strGalType=rna&expMeth=all&seqType=all&galType=table&start=0&limit=50");
                        }
                    });


                    final javafx.scene.control.Button loadInAssemble = new javafx.scene.control.Button("Load", fontAwesome.create(FontAwesome.Glyph.SIGN_IN));
                    loadInAssemble.setDisable(true);
                    loadInAssemble.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {

                            new javax.swing.SwingWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    loadPDBID(currentPDBID);
                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                    return null;
                                }
                            }.execute();
                        }
                    });

                    loadInAssemble.setMaxWidth(Double.MAX_VALUE);

                    final HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(15, 12, 15, 12));
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-background-color: #C12427;");

                    hbox.getChildren().add(home);
                    hbox.getChildren().add(loadInAssemble);

                    root.setTop(hbox);

                    scene.setRoot(root);

                    jfxPanel.setScene(scene);

                    webEngine.getLoadWorker().stateProperty().addListener(
                            new javafx.beans.value.ChangeListener<Worker.State>() {
                                public void stateChanged(ChangeEvent e) {

                                }

                                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        org.w3c.dom.Document doc = webEngine.getDocument();
                                        NodeList h2s = doc.getElementsByTagName("h2");
                                        for (int i=0 ; i < h2s.getLength() ; i++) {
                                            org.w3c.dom.Node h2 = (org.w3c.dom.Node)h2s.item(i);
                                            String content = h2.getTextContent();
                                            if (content.matches("NDB ID:.+PDB ID:.+")) {
                                                currentPDBID = content.split("PDB ID:")[1].trim().substring(0,4);
                                                loadInAssemble.setText("Load "+currentPDBID);
                                                loadInAssemble.setDisable(false);
                                            }
                                        }
                                    }
                                }
                            });
                }
            });
        }

    }

    private class RNACentralWebsite extends JFrame {
        private JFXPanel jfxPanel;
        private javafx.scene.control.Button loadInAssemble;
        private String currentSequence, currentName;

        private RNACentralWebsite() {
            this.setTitle("RNAcentral");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight());
            this.setLayout(new BorderLayout());
            jfxPanel = new JFXPanel();
            this.createScene();
            this.add(jfxPanel, BorderLayout.CENTER);
            this.setVisible(true);
            this.toFront();
        }

        private void createScene() {
            PlatformImpl.startup(new Runnable() {
                @Override
                public void run() {
                    Stage stage = new Stage();
                    final BorderPane root = new BorderPane();

                    stage.setTitle("RNAcentral");

                    Scene scene = new Scene(new Group());
                    stage.setScene(scene);

                    // Set up the embedded browser:
                    WebView browser = new WebView();
                    final WebEngine webEngine = browser.getEngine();
                    webEngine.load("http://rnacentral.org");

                    root.setCenter(browser);

                    javafx.scene.control.Button home = new javafx.scene.control.Button(null, fontAwesome.create(FontAwesome.Glyph.HOME));
                    home.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {
                            webEngine.load("http://rnacentral.org");
                        }
                    });

                    loadInAssemble = new javafx.scene.control.Button(null, fontAwesome.create(FontAwesome.Glyph.SIGN_IN));
                    loadInAssemble.setDisable(true);

                    loadInAssemble.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {

                            new javax.swing.SwingWorker() {
                                @Override
                                protected Object doInBackground() {
                                    try {
                                        mediator.clearSession();
                                        mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                        mediator.getFoldingLandscape().clear();
                                        if (AssembleConfig.popupLateralPanels())
                                            mediator.getFoldingLandscape().getToolWindow().setVisible(true);
                                        List<SecondaryStructure> secondaryStructures = FileParser.parseFasta(new StringReader(">"+currentName+"\n" + currentSequence), mediator);
                                        if (!secondaryStructures.isEmpty()) {
                                            for (SecondaryStructure ss : secondaryStructures)
                                                mediator.loadRNASecondaryStructure(ss, true, false);
                                            ((JXFrame) window).setTitle(currentName);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                    return null;
                                }
                            }.execute();
                        }
                    });

                    final HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(15, 12, 15, 12));
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-background-color: #3F7D97;");

                    hbox.getChildren().add(home);
                    hbox.getChildren().add(loadInAssemble);

                    root.setTop(hbox);

                    scene.setRoot(root);

                    jfxPanel.setScene(scene);

                    webEngine.getLoadWorker().stateProperty().addListener(
                            new javafx.beans.value.ChangeListener<Worker.State>() {
                                public void stateChanged(ChangeEvent e) {
                                }

                                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        org.w3c.dom.Document doc = webEngine.getDocument();
                                        NodeList ps = doc.getElementsByTagName("li");
                                        for (int i=0 ; i < ps.getLength() ; i++) {
                                            HTMLLIElement li = (HTMLLIElement)ps.item(i);
                                            String content = li.getAttribute("class");
                                            if (content!= null && content.matches("lead")) {
                                                currentName = li.getTextContent();
                                                loadInAssemble.setText("Load "+currentName);
                                                loadInAssemble.setDisable(false);
                                                NodeList pres = doc.getElementsByTagName("pre");
                                                for (int j = 0 ; j < pres.getLength() ; j++) {
                                                    org.w3c.dom.Node pre = (org.w3c.dom.Node)pres.item(j);
                                                    currentSequence =  pre.getTextContent().trim().replaceAll("\\s+", " ");
                                                }
                                            }
                                        }
                                    }

                                }
                            });
                }
            });
        }

    }

    private class MfoldWebServer extends JFrame {
        private JFXPanel jfxPanel;
        private List<String> structureLinks;
        private javafx.scene.control.Button loadInAssemble;
        private int selectedStructure;

        private MfoldWebServer() {
            this.structureLinks = new ArrayList<String>();
            this.setTitle("mfold Web Server");
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight());
            this.setLayout(new BorderLayout());
            jfxPanel = new JFXPanel();
            this.createScene();
            this.add(jfxPanel, BorderLayout.CENTER);
            this.setVisible(true);
            this.toFront();
        }

        private void createScene() {
            PlatformImpl.startup(new Runnable() {
                @Override
                public void run() {
                    Stage stage = new Stage();
                    final BorderPane root = new BorderPane();

                    stage.setTitle("mfold Web Server");

                    Scene scene = new Scene(new Group());
                    stage.setScene(scene);

                    // Set up the embedded browser:
                    WebView browser = new WebView();
                    final WebEngine webEngine = browser.getEngine();
                    webEngine.load("http://mfold.rna.albany.edu/?q=mfold/RNA-Folding-Form");

                    root.setCenter(browser);

                    javafx.scene.control.Button home = new javafx.scene.control.Button(null, fontAwesome.create(FontAwesome.Glyph.HOME));
                    home.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {
                            webEngine.load("http://mfold.rna.albany.edu/?q=mfold/RNA-Folding-Form");
                        }
                    });

                    final ChoiceBox structures = new ChoiceBox();
                    structures.getSelectionModel().selectedIndexProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                            selectedStructure = (Integer) number2;
                        }

                    });
                    structures.setDisable(true);

                    loadInAssemble = new javafx.scene.control.Button(null, fontAwesome.create(FontAwesome.Glyph.SIGN_IN));
                    loadInAssemble.setDisable(true);

                    loadInAssemble.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {

                            new javax.swing.SwingWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    URL url = new URL(structureLinks.get(selectedStructure));
                                    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                                    StringBuffer content = new StringBuffer();
                                    String str = null;
                                    while ((str = in.readLine()) != null) {
                                        content.append(str + "\n");
                                    }
                                    in.close();
                                    if (mediator.getSecondaryStructure() != null && JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(Assemble.this.window, "Are you sure to quit the current Project?"))
                                        return null;
                                    mediator.getSecondaryCanvas().getActivityToolBar().startActivity(this);
                                    SecondaryStructure ss = FileParser.parseCT(new StringReader(content.toString()), mediator);
                                    if (ss != null) {
                                        mediator.loadRNASecondaryStructure(ss, false, true);
                                        ((JXFrame) window).setTitle("Structure #"+(selectedStructure+1) + " predicted from mfold");
                                    }
                                    mediator.getSecondaryCanvas().getActivityToolBar().stopActivity();
                                    return null;

                                }
                            }.execute();
                        }
                    });

                    final HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(15, 12, 15, 12));
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-background-color: #8454ab;");

                    hbox.getChildren().add(home);
                    hbox.getChildren().add(structures);
                    hbox.getChildren().add(loadInAssemble);

                    root.setTop(hbox);

                    scene.setRoot(root);

                    jfxPanel.setScene(scene);

                    webEngine.getLoadWorker().stateProperty().addListener(
                            new javafx.beans.value.ChangeListener<Worker.State>() {
                                public void stateChanged(ChangeEvent e) {
                                }

                                public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        org.w3c.dom.Document doc = webEngine.getDocument();
                                        NodeList inputs = doc.getElementsByTagName("input");
                                        structureLinks.clear();
                                        structures.getItems().clear();
                                        structures.setDisable(true);
                                        loadInAssemble.setDisable(true);
                                        for (int i=0 ; i < inputs.getLength() ; i++) {
                                            HTMLInputElement input = (HTMLInputElement)inputs.item(i);
                                            if (input.getAttribute("name") != null && input.getAttribute("name").equals("SEQ_NAME")) {
                                                input.setValue(mediator.getSecondaryStructure().getMolecule().getName());
                                                break;
                                            }
                                        }
                                        NodeList textareas = doc.getElementsByTagName("textarea");
                                        for (int i=0 ; i < textareas.getLength() ; i++) {
                                            HTMLTextAreaElement textarea = (HTMLTextAreaElement)textareas.item(i);
                                            if (textarea.getAttribute("name") != null && textarea.getAttribute("name").equals("SEQUENCE")) {
                                                textarea.setValue(mediator.getSecondaryStructure().getMolecule().printSequence());
                                                break;
                                            }
                                        }

                                        NodeList links = doc.getElementsByTagName("a");
                                        for (int i=0 ; i < links.getLength() ; i++) {
                                            HTMLAnchorElement link = (HTMLAnchorElement)links.item(i);
                                            if (link.getHref().matches("http://mfold\\.rna\\.albany\\.edu/cgi-bin/ct\\.cgi.+COUNT.+")) {
                                                structureLinks.add(link.getHref());
                                                structures.getItems().add("Structure #"+link.getHref().split("COUNT=")[1]);
                                            }
                                        }
                                        if (!structures.getItems().isEmpty()) {
                                            loadInAssemble.setDisable(false);
                                            structures.setDisable(false);
                                            structures.setValue(structures.getItems().get(0));
                                            selectedStructure = 0;
                                        }
                                    }

                                }
                            });
                }
            });
        }

    }

    private Control addWithMargin(VBox parent, Control control, javafx.geometry.Insets insets) {
        parent.getChildren().add(control);
        VBox.setMargin(control, insets);
        return control;
    }

    private class MyRNAMotifsPanel extends JPanel {

        private IconsPanel iconsPanel;
        private JPanel buttonsBar;
        private JScrollPane iconsPanelScrollPane;
        private JComboBox motifsCategories;

        private MyRNAMotifsPanel() {
            this.setLayout(new BorderLayout());
            this.iconsPanel = new IconsPanel();
            this.iconsPanelScrollPane = new JScrollPane(this.iconsPanel);
            this.add(iconsPanelScrollPane, BorderLayout.CENTER);
            this.buttonsBar = new JPanel();
            this.buttonsBar.setLayout(new BoxLayout(buttonsBar,BoxLayout.X_AXIS));
            this.add(this.buttonsBar, BorderLayout.SOUTH);
            this.buttonsBar.add(Box.createHorizontalGlue());
            this.motifsCategories = new JComboBox(new String[]{"All"}){
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(100,20);
                }
            };
            this.motifsCategories.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    String selectedValue = (String)cb.getSelectedItem();
                    if (selectedValue == null) //this is necessary when the motifs library is reloaded and the motifCategories JComboBox reinitialized
                        return;
                    for (Component c:iconsPanel.getComponents())
                        if (RNAMotifIcon.class.isInstance(c))
                            iconsPanel.remove(c);
                    if ("All".equals(selectedValue))
                        for (List<RNAMotifIcon> icons:motifsIcons.values()) {
                            for (RNAMotifIcon icon:icons)
                                iconsPanel.add(icon);
                        }
                    else
                        for (RNAMotifIcon icon:motifsIcons.get(selectedValue))
                            iconsPanel.add(icon);
                    iconsPanel.doLayout();
                    iconsPanel.revalidate();
                    iconsPanel.repaint();
                    iconsPanelScrollPane.doLayout();
                    iconsPanelScrollPane.revalidate();
                }
            });

            JLabel eye = new JLabel(new DisplayedIcon());
            this.buttonsBar.add(eye);
            this.buttonsBar.add(Box.createRigidArea(new Dimension(5,0)));
            this.buttonsBar.add(this.motifsCategories);
            this.buttonsBar.add(Box.createRigidArea(new Dimension(20,0)));

            final JSlider iconsSize = new JSlider(JSlider.HORIZONTAL,100,300,motifsIconSize);
            iconsSize.setMaximumSize(new Dimension(200,20));
            iconsSize.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    for (String category:motifsIcons.keySet()) {
                        for (RNAMotifIcon icon:motifsIcons.get(category)) {
                            try {
                                motifsIconSize = iconsSize.getValue();
                                icon.updateImage();
                            }
                            catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            JLabel zoom = new JLabel(new ZoomMinus());
            this.buttonsBar.add(zoom);
            this.buttonsBar.add(iconsSize);
            zoom = new JLabel(new ZoomPlus());
            this.buttonsBar.add(zoom);
            this.buttonsBar.add(Box.createRigidArea(new Dimension(5,0)));
            JLabel reload = new JLabel(new FlipIcon());
            reload.setToolTipText("Reload RNA Motifs Library");
            this.buttonsBar.add(reload);
            reload.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            for (Component c:iconsPanel.getComponents())
                                if (RNAMotifIcon.class.isInstance(c))
                                    iconsPanel.remove(c);
                            motifsIcons.clear();
                            motifsCategories.removeAllItems();
                            motifsCategories.addItem("All");
                            for (File motifDir:Assemble.getMotifsDirectory().listFiles(new FileFilter() {
                                public boolean accept(File f) {
                                    return f.isDirectory();
                                }
                            })) {
                                for (File motif:motifDir.listFiles(new FileFilter() {
                                    public boolean accept(File f) {
                                        return f.getName().endsWith(".rnaml");
                                    }
                                })) {
                                    String motifName = motif.getName().split("\\.")[0],categoryName = motifDir.getName();
                                    final RNAMotifIcon icon = new RNAMotifIcon(motifName, categoryName);
                                    if (motifsIcons.containsKey(categoryName))
                                        motifsIcons.get(categoryName).add(icon);
                                    else {
                                        List<RNAMotifIcon> icons = new ArrayList<RNAMotifIcon>();
                                        icons.add(icon);
                                        motifsIcons.put(categoryName,icons);
                                        motifsCategories.addItem(categoryName);
                                    }
                                    iconsPanel.add(icon);
                                    iconsPanel.doLayout();
                                    iconsPanel.revalidate();
                                    iconsPanel.repaint();
                                    iconsPanelScrollPane.doLayout();
                                    iconsPanelScrollPane.revalidate();
                                }
                            }
                            return null;
                        }
                    }.execute();
                }
            });

            //we do it a first time
            new javax.swing.SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    for (Component c:iconsPanel.getComponents())
                        if (RNAMotifIcon.class.isInstance(c))
                            iconsPanel.remove(c);
                    motifsIcons.clear();
                    motifsCategories.removeAllItems();
                    motifsCategories.addItem("All");
                    for (File motifDir:Assemble.getMotifsDirectory().listFiles(new FileFilter() {
                        public boolean accept(File f) {
                            return f.isDirectory();
                        }
                    })) {
                        for (File motif:motifDir.listFiles(new FileFilter() {
                            public boolean accept(File f) {
                                return f.getName().endsWith(".rnaml");
                            }
                        })) {
                            String motifName = motif.getName().split("\\.")[0],categoryName = motifDir.getName();
                            final RNAMotifIcon icon = new RNAMotifIcon(motifName, categoryName);
                            if (motifsIcons.containsKey(categoryName))
                                motifsIcons.get(categoryName).add(icon);
                            else {
                                List<RNAMotifIcon> icons = new ArrayList<RNAMotifIcon>();
                                icons.add(icon);
                                motifsIcons.put(categoryName,icons);
                                motifsCategories.addItem(categoryName);
                            }
                            iconsPanel.add(icon);
                            iconsPanel.doLayout();
                            iconsPanel.revalidate();
                            iconsPanel.repaint();
                            iconsPanelScrollPane.doLayout();
                            iconsPanelScrollPane.revalidate();
                        }
                    }
                    return null;
                }
            }.execute();

            this.buttonsBar.add(Box.createHorizontalGlue());
        }

        private void removeIconMotif(String categoryName, String motifName) {
            List<RNAMotifIcon> icons = motifsIcons.get(categoryName);
            for (RNAMotifIcon icon:new ArrayList<RNAMotifIcon>(icons))
                if (icon.categoryName.equals(categoryName) && icon.motifName.equals(motifName)) {
                    icons.remove(icon);
                    iconsPanel.remove(icon);
                    iconsPanel.doLayout();
                    iconsPanel.revalidate();
                    iconsPanel.repaint();
                    iconsPanelScrollPane.doLayout();
                    iconsPanelScrollPane.revalidate();
                    break;
                }
            if (motifsIcons.get(categoryName).isEmpty()) {
                motifsIcons.remove(categoryName);
                this.motifsCategories.removeItem(categoryName);

            }
        }


        private void addNewIconMotif(final String categoryName, final String motifName) throws IOException {
            final RNAMotifIcon icon = new RNAMotifIcon(motifName, categoryName);
            if (motifsIcons.containsKey(categoryName))
                motifsIcons.get(categoryName).add(icon);
            else {
                List<RNAMotifIcon> icons = new ArrayList<RNAMotifIcon>();
                icons.add(icon);
                motifsIcons.put(categoryName,icons);
                this.motifsCategories.addItem(categoryName);
            }
            this.iconsPanel.add(icon);
            this.iconsPanel.doLayout();
            this.iconsPanel.revalidate();
            this.iconsPanel.repaint();
            this.iconsPanelScrollPane.doLayout();
            this.iconsPanelScrollPane.revalidate();
        }

        private class IconsPanel extends JPanel {

            private static final int GAP = 10;

            private IconsPanel() {
                this.setLayout(new FlowLayout(FlowLayout.LEADING,GAP,GAP));
                this.setBackground(Color.LIGHT_GRAY);
            }

            protected void paintComponent( Graphics g ) {
                if (!isOpaque()) {
                    super.paintComponent( g );
                    return;
                }

                Graphics2D g2d = (Graphics2D)g;

                int w = getWidth( );
                int h = getHeight( );

                // Paint a gradient from top to bottom
                GradientPaint gp = new GradientPaint( 0, 0, Color.WHITE, 0, h, getBackground() );

                g2d.setPaint( gp );
                g2d.fillRect( 0, 0, w, h );

                setOpaque(false);
                super.paintComponent( g );
                setOpaque(true);
            }
        }
    }

    private class RNAMotifIcon extends JPanel {
        private String motifName, categoryName;
        private JButton button;
        private JLabel motifNameLabel, categoryLabel;

        private RNAMotifIcon(final String motifName, final String categoryName) throws IOException {
            this.motifName = motifName;
            this.categoryName = categoryName;
            this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
            this.setOpaque(false);

            final File motifCategoryDir = new File(new File(getUserDir(),"motifs"),categoryName);

            File icon = new File(motifCategoryDir, motifName+".png");
            if (!icon.exists())
                return;
            BufferedImage image= ImageIO.read(icon);
            int width = image.getWidth(), height = image.getHeight();
            float ratio = width >= height ? (float)width/(float) motifsIconSize : (float)height/(float) motifsIconSize;
            if (ratio > 1) {//if the image is smaller than the size of 150px, we do nothing
                this.button = new JButton(new ImageIcon(image.getScaledInstance((int)((float)width/ratio),(int)((float)height/ratio),Image.SCALE_SMOOTH)));
                this.add(Box.createRigidArea(new Dimension(-1, motifsIconSize -(int)((float)height/ratio))));
            }
            else {
                this.button = new JButton(new ImageIcon(image));
                this.add(Box.createRigidArea(new Dimension(-1, motifsIconSize -height)));
            }
            button.setBackground(Color.WHITE);

            button.addMouseListener(new MouseListener() {

                private JPopupMenu popupMenu = new JPopupMenu();

                {
                    JMenuItem menuItem = new JMenuItem("Get Details");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e){
                            try {
                                if (mediator.getToolWindowManager().getToolWindow("MotifDetails") != null)
                                    mediator.getToolWindowManager().unregisterToolWindow("MotifDetails");
                                ToolWindow toolWindow = mediator.getToolWindowManager().registerToolWindow("MotifDetails","RNA Motif Details",RessourcesUtils.getIcon("16/images.png"), new RNAMotifDetail(new RNAMotif(new File(motifCategoryDir, motifName+".rnaml"),categoryName), button.getIcon()), ToolWindowAnchor.RIGHT);
                                DockedTypeDescriptor descriptor = (DockedTypeDescriptor) toolWindow.getTypeDescriptor(ToolWindowType.DOCKED);
                                descriptor.setDockLength(300);
                                descriptor.setPreviewEnabled(false);
                                toolWindow.setAvailable(true);
                                toolWindow.aggregate();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    this.popupMenu.add(menuItem);

                    menuItem = new JMenuItem("Load 3D");
                    final File rnamlFile = new File(motifCategoryDir, motifName+".rnaml"),
                            pdbFile = new File(motifCategoryDir, motifName+".pdb");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Document doc = null;
                            try {
                                doc = new SAXBuilder().build(rnamlFile);
                            } catch (JDOMException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            List<String> positions = new ArrayList<String>();
                            if (doc != null) {
                                java.util.List molecules = doc.getRootElement().getChildren("molecule");
                                Element tertiaryStructure = doc.getRootElement().getChild("tertiary-structure");
                                for (Object o: molecules) {
                                    String moleculeName = ((Element)o).getAttributeValue("id");
                                    int start =  Integer.parseInt(((Element)o).getAttributeValue("start"));
                                    for (Object _o:tertiaryStructure.getChildren("base")) {
                                        Element base = (Element)_o;
                                        if (base.getAttributeValue("molecule-id").equals(moleculeName)) {
                                            positions.add(""+(start+Integer.parseInt(base.getAttributeValue("id"))));
                                        }
                                    }
                                }
                            }
                            if (mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().loadTertiaryStructure(pdbFile, 10); //we use the layer #10 of Chimera
                            if (!positions.isEmpty() && mediator.getChimeraDriver() != null)
                                mediator.getChimeraDriver().selectResidues(positions, 10);
                        }
                    });

                    if (pdbFile.exists())
                        this.popupMenu.add(menuItem);

                    menuItem = new JMenuItem("Remove Motif");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,"Are you sure to delete this RNA motif?")) {
                                //we remove the files...
                                new File(motifCategoryDir, motifName+".rnaml").delete();
                                new File(motifCategoryDir, motifName+".png").delete();
                                myRNAMotifsPanel.removeIconMotif(categoryName,motifName);
                                if (motifCategoryDir.listFiles().length == 0)
                                    motifCategoryDir.delete();
                            }
                        }
                    });
                    this.popupMenu.add(menuItem);

                    menuItem = new JMenuItem("Remove Category");
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,"Are you sure to delete this category of motifs?")) {
                                for (RNAMotifIcon icon: new ArrayList<RNAMotifIcon>(motifsIcons.get(categoryName))) {
                                    //we remove the files...
                                    new File(motifCategoryDir, icon.motifName+".rnaml").delete();
                                    new File(motifCategoryDir, icon.motifName+".png").delete();
                                    myRNAMotifsPanel.removeIconMotif(icon.categoryName,icon.motifName);
                                }
                                motifCategoryDir.delete();
                            }
                        }
                    });
                    this.popupMenu.add(menuItem);
                }


                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2) {

                    }
                }

                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        this.popupMenu.show(e.getComponent(),
                                e.getX(), e.getY());
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        this.popupMenu.show(e.getComponent(),
                                e.getX(), e.getY());
                    }
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            });

            button.setMargin(new Insets(0,0,0,0));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.add(button);

            this.motifNameLabel = new JLabel(motifName) ;

            this.motifNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.add(this.motifNameLabel);

            this.categoryLabel = new JLabel(categoryName) ;

            this.categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.add(this.categoryLabel);

        }

        private void updateImage() throws IOException {
            final File motifCategoryDir = new File(new File(getUserDir(),"motifs"),categoryName);
            File icon = new File(motifCategoryDir, motifName+".png");
            if (!icon.exists())
                return;
            BufferedImage image= ImageIO.read(icon);
            int width = image.getWidth(), height = image.getHeight();
            float ratio = width >= height ? (float)width/(float)motifsIconSize : (float)height/(float)motifsIconSize;
            if (ratio > 1) {//if the image is smaller than the size of 150px, we do nothing
                this.button.setIcon(new ImageIcon(image.getScaledInstance((int)((float)width/ratio),(int)((float)height/ratio),Image.SCALE_SMOOTH)));
                this.remove(0);
                this.add(Box.createRigidArea(new Dimension(-1,motifsIconSize-(int)((float)height/ratio))),0);
                this.doLayout();
                this.revalidate();
            }
            else {
                this.button.setIcon(new ImageIcon(image));
                this.remove(0);
                this.add(Box.createRigidArea(new Dimension(-1,motifsIconSize-height)),0);
                this.doLayout();
                this.revalidate();
            }
        }
    }


    private class RNAMotifDetail extends JXPanel {

        private RNAMotif motif;
        private javax.swing.JButton cancel;
        private javax.swing.JButton apply;
        private JXTable sequencesTable;
        private Icon icon;

        public RNAMotifDetail(RNAMotif motif, Icon icon) {
            this.motif = motif;
            this.icon = icon;
            initComponents();
        }

        private void initComponents() {
            this.setLayout(new BorderLayout());
            this.setBackground(Color.WHITE);
            JPanel header = new JPanel(new BorderLayout());
            header.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
            header.setBackground(Color.WHITE);
            JPanel iconPanel = new JPanel();
            iconPanel.setBackground(Color.WHITE);
            JLabel iconLabel = new JLabel(icon);
            iconPanel.add(iconLabel);
            header.add(iconPanel,BorderLayout.WEST);
            iconPanel.setBorder(BorderFactory.createLineBorder(Color.black));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
            JPanel description = new JPanel();
            description.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
            description.setBackground(Color.WHITE);
            description.setLayout(new BoxLayout(description, BoxLayout.Y_AXIS));
            description.add(new JLabel("Name: "+motif.name));
            description.add(new JLabel("Category: "+motif.category));
            if (motif.pubmedID != null)
                description.add(new JLabel("PubMed ID: "+motif.pubmedID));
            if (motif.comment != null)
                description.add(new JLabel("Comment: "+motif.comment));
            header.add(description,BorderLayout.CENTER);
            this.add(header, BorderLayout.NORTH);

            sequencesTable = new JXTable();
            sequencesTable.setBackground(Color.WHITE);

            apply = new javax.swing.JButton();
            cancel = new javax.swing.JButton();

            Object [][] objects = new Object [motif.motifSequences.size()][6];
            for (int i=0; i< motif.motifSequences.size() ; i++) {
                objects[i][0] = motif.motifSequences.get(i).sequence;
                objects[i][1] = motif.motifSequences.get(i).startPosition == -1 ? "NA" : ""+motif.motifSequences.get(i).startPosition;
                objects[i][2] = motif.motifSequences.get(i).endPosition == -1 ? "NA" : ""+motif.motifSequences.get(i).endPosition;
                objects[i][3] = motif.motifSequences.get(i).sequence.length();
                objects[i][4] = "???";
                objects[i][5] = "Link";
            }

            sequencesTable.setModel(new javax.swing.table.DefaultTableModel(
                    objects,
                    new String [] {
                            "Motif Sequences", "Start", "End", "Length", "Your Selection", "Link"
                    }
            ) {
                Class[] types = new Class [] {
                        java.lang.String.class, java.lang.String.class,java.lang.String.class,java.lang.String.class, java.lang.String.class, String.class
                };
                boolean[] canEdit = new boolean [] {
                        false, false,false,false,true, true
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            sequencesTable.getTableHeader().setReorderingAllowed(false);
            sequencesTable.getColumn("Link").setCellRenderer(new ButtonRenderer());
            sequencesTable.getColumn("Link").setCellEditor(new ButtonEditor(new JCheckBox()));
            sequencesTable.getColumn("Link").setMaxWidth(100);
            sequencesTable.getColumn("Link").setMinWidth(100);
            sequencesTable.setFillsViewportHeight(true);
            this.add(new JScrollPane(sequencesTable), BorderLayout.CENTER);

            apply.setText("Apply");
            apply.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        applyActionPerformed(evt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            JPanel footer = new JPanel();
            footer.setBackground(Color.WHITE);
            footer.add(apply);
            this.add(footer, BorderLayout.SOUTH);

        }

        private void applyActionPerformed(java.awt.event.ActionEvent evt) throws Exception {
            //we test if we still have null objects for userChains, meaning that not all the links have been made
            for (List<Residue> residues: motif.userChains )
                if (residues == null) {
                    JOptionPane.showMessageDialog(RNAMotifDetail.this, "You have to link all the sequences of the motif!!");
                    return;
                }

            //######### the secondary structure elements ###########

            //############ we create the new tertiary interactions #########
            for (Element interactionEl:motif.interactions) {
                String molecule1ID = interactionEl.getAttributeValue("molecule1-id"),
                        base5ID = interactionEl.getAttributeValue("base1-id"),
                        molecule2ID = interactionEl.getAttributeValue("molecule2-id"),
                        base3ID = interactionEl.getAttributeValue("base2-id"),
                        edge1 = interactionEl.getAttributeValue("edge1"),
                        edge2 = interactionEl.getAttributeValue("edge2"), orientation = interactionEl.getAttributeValue("orientation");
                Residue base1 = null, base2 = null;
                for (int i =0; i< motif.motifSequences.size();i++) {
                    if (motif.motifSequences.get(i).sequenceID.equals(molecule1ID))
                        base1 = motif.userChains.get(i).get(Integer.parseInt(base5ID) - 1);
                    if (motif.motifSequences.get(i).sequenceID.equals(molecule2ID))
                        base2 = motif.userChains.get(i).get(Integer.parseInt(base3ID) - 1);
                }
                if (base1 != null && base2 != null) {
                    Residue base12D = mediator.getSecondaryStructure().getResidue(base1.getAbsolutePosition()),
                            base22D = mediator.getSecondaryStructure().getResidue(base2.getAbsolutePosition());
                    for (BaseBaseInteraction interaction: mediator.getSecondaryStructure().getTertiaryInteractions())
                        if (interaction.getResidue().equals(base12D) && interaction.getPartnerResidue().equals(base22D)) {
                            mediator.getSecondaryStructure().removeTertiaryInteraction(interaction);
                            break;
                        }
                        else if (interaction.getResidue().equals(base22D) && interaction.getPartnerResidue().equals(base12D)) {
                            mediator.getSecondaryStructure().removeTertiaryInteraction(interaction);
                            break;
                        }
                    mediator.getSecondaryStructure().addTertiaryInteraction(new Location(new Location(base12D.getAbsolutePosition()), new Location(base22D.getAbsolutePosition())), orientation.charAt(0), edge1.charAt(0), edge2.charAt(0));
                }
            }

            mediator.getRna2DViewer().getSecondaryCanvas().repaint();

            //######### the 3D coordinates #############

            List<Residue3D> computedResidues = new ArrayList<Residue3D>();
            TertiaryStructure ts = null;
            boolean firstFragment = false;
            if (mediator.getTertiaryStructure() == null) {
                ts = new TertiaryStructure("3D");
                firstFragment = true;
            }
            else
                ts = mediator.getTertiaryStructure();
            for (int i =0; i< motif.motifSequences.size();i++) {
                List<Residue> residues = motif.userChains.get(i);
                Location targetLocation = new Location();
                for (Residue r:residues)
                    targetLocation.add(r.getAbsolutePosition());
                computedResidues.addAll(Modeling3DUtils.thread(mediator, ts, targetLocation.getStart(), targetLocation.getLength(), Modeling3DUtils.RNA, motif.motifSequences.get(i).residues3D));
            }
            List<Residue3D> previousResidues = mediator.getTertiaryStructure().getResidues3D();
            File tmpPDB = IoUtils.createTemporaryFile("motif.pdb");
            FileParser.writePDBFile(computedResidues, false, new FileWriter(tmpPDB));
            int anchorResidue1 = -1,anchorResidue2 = -1;
            for (Residue3D r: computedResidues) {
                int previous = r.getAbsolutePosition();
                Residue pairedResidue = mediator.getSecondaryStructure().getPairedResidueInSecondaryInteraction(mediator.getSecondaryStructure().getResidue(previous));
                if (pairedResidue == null)
                    continue;
                int pairedPrevious = pairedResidue.getAbsolutePosition();
                for (Residue3D previousRes:previousResidues) { //we search which residues in the new ones are already in the 3D scene
                    if (previousRes.getAbsolutePosition() == previous) {
                        anchorResidue1 = previous;
                    }
                    else if  (previousRes.getAbsolutePosition() == pairedPrevious) {
                        anchorResidue2 = pairedPrevious;
                    }
                }
                if (anchorResidue1 != -1 && anchorResidue2 != -1) {
                    break;
                }
            }
            if (mediator.getChimeraDriver() != null)
                mediator.getChimeraDriver().addFragment(tmpPDB, computedResidues, anchorResidue1, anchorResidue2, firstFragment);
        }

        private void cancelActionPerformed(java.awt.event.ActionEvent evt) {
        }


        public class ButtonRenderer extends JButton implements TableCellRenderer {

            public ButtonRenderer() {
                setOpaque(true);
            }

            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                    setBackground(table.getSelectionBackground());
                } else{
                    setForeground(table.getForeground());
                    setBackground(UIManager.getColor("Button.background"));
                }
                setText( (value ==null) ? "" : value.toString() );
                return this;
            }
        }

        public class ButtonEditor extends DefaultCellEditor {
            protected JButton button;
            private String    label;
            private boolean   isPushed;
            private int selectedRow;

            public ButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                button = new JButton();
                button.setOpaque(true);
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        fireEditingStopped();
                    }
                });
            }

            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                if (isSelected) {
                    button.setForeground(table.getSelectionForeground());
                    button.setBackground(table.getSelectionBackground());
                } else{
                    button.setForeground(table.getForeground());
                    button.setBackground(table.getBackground());
                }
                label = (value == null) ? "" : value.toString();
                button.setText( label );
                isPushed = true;
                selectedRow = row;
                return button;
            }

            public Object getCellEditorValue() {
                if (isPushed)  {
                    if (((String) sequencesTable.getValueAt(selectedRow, 0)).length() != mediator.getRna2DViewer().getSecondaryCanvas().getSelectedResidues().size())
                        JOptionPane.showMessageDialog(RNAMotifDetail.this, "Your selection should have the same size than the sequence in the motif!!");
                    else {
                        StringBuffer buff = new StringBuffer();
                        List<Residue> selectedResidues = new ArrayList<Residue>();
                        for (Residue r:mediator.getRna2DViewer().getSecondaryCanvas().getSelectedResidues())
                            selectedResidues.add(r);
                        //since the user can select the residues in any order, they are sorted according to their absolutePosition
                        Collections.sort(selectedResidues, new Comparator<Residue>() {
                            public int compare(Residue residue, Residue residue1) {
                                return residue.getAbsolutePosition()-residue1.getAbsolutePosition();
                            }
                        });
                        for (Residue r:selectedResidues)
                            buff.append(r.getSymbol());
                        sequencesTable.setValueAt(buff.toString(),selectedRow, 4);
                        motif.userChains.set(selectedRow,selectedResidues);
                    }
                }
                isPushed = false;
                return new String( label ) ;
            }

            public boolean stopCellEditing() {
                isPushed = false;
                return super.stopCellEditing();
            }

            protected void fireEditingStopped() {
                super.fireEditingStopped();
            }
        }

    }

    private class RNAMotif {
        private String name, category, pubmedID, comment;
        private Date date;
        private List<RNAMotifSequence> motifSequences;
        private List<Element> interactions, helices;
        private List<List<Residue>> userChains;

        private RNAMotif(File rnamlFile, String category) throws IOException {
            Document doc = null;
            try {
                doc = new SAXBuilder().build(rnamlFile);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            java.util.List molecules = doc.getRootElement().getChildren("molecule");
            java.util.List bases = doc.getRootElement().getChild("tertiary-structure").getChildren("base");
            this.motifSequences = new ArrayList<RNAMotifSequence>(molecules.size());
            this.userChains = new ArrayList<List<Residue>>();
            this.interactions = doc.getRootElement().getChild("structure-annotation").getChildren("base-pair");
            this.helices = doc.getRootElement().getChild("structure-annotation").getChildren("helix");
            for (int i=0; i<molecules.size() ; i++)
                this.userChains.add(null);
            for (int i=0;i<molecules.size();i++) {
                Element molecule = (Element)molecules.get(i);
                Molecule m = new Molecule(molecule.getAttributeValue("id"), molecule.getChild("sequence").getChild("seq-data").getText());
                char[] moleculeSequence = molecule.getChild("sequence").getChild("seq-data").getText().toCharArray();
                String moleculeId = molecule.getAttributeValue("id");
                List<Residue3D> residues3D = new ArrayList<Residue3D>();
                for (Object o:bases) {
                    Element base = (Element)o;
                    if (moleculeId.equals(base.getAttributeValue("molecule-id"))) {
                        char symbol = moleculeSequence[Integer.parseInt(base.getAttributeValue("id"))-1];
                        Residue3D residue3D = null;
                        switch (symbol) {
                            case 'A' : residue3D = new Adenine3D(m, Integer.parseInt(molecule.getAttributeValue("start"))+Integer.parseInt(base.getAttributeValue("id"))-1); break;
                            case 'U' : residue3D = new Uracil3D(m, Integer.parseInt(molecule.getAttributeValue("start"))+Integer.parseInt(base.getAttributeValue("id"))-1); break;
                            case 'G' : residue3D = new Guanine3D(m, Integer.parseInt(molecule.getAttributeValue("start"))+Integer.parseInt(base.getAttributeValue("id"))-1); break;
                            case 'C' : residue3D = new Cytosine3D(m, Integer.parseInt(molecule.getAttributeValue("start"))+Integer.parseInt(base.getAttributeValue("id"))-1); break;
                            default: ;
                        }
                        if (residue3D != null) {
                            for (Object _o:base.getChildren("atom"))
                                residue3D.setAtomCoordinates(((Element)_o).getAttributeValue("type"),
                                        Float.parseFloat(((Element)_o).getAttributeValue("x")),
                                        Float.parseFloat(((Element)_o).getAttributeValue("y")),
                                        Float.parseFloat(((Element)_o).getAttributeValue("z")));
                            residues3D.add(residue3D);
                        }
                    }
                }
                Attribute start = molecule.getAttribute("start"), end = molecule.getAttribute("end");
                if (start == null && end == null)
                    this.motifSequences.add(new RNAMotifSequence(molecule.getAttributeValue("id"), molecule.getChild("sequence").getChild("seq-data").getText(), residues3D));
                else
                    this.motifSequences.add(new RNAMotifSequence(molecule.getAttributeValue("id"), molecule.getChild("sequence").getChild("seq-data").getText(), residues3D, Integer.parseInt(start.getValue()), Integer.parseInt(end.getValue())));
            }
            this.name = rnamlFile.getName().split("\\.rnaml")[0];
            this.category = category;
            this.comment = doc.getRootElement().getAttributeValue("comment");
            for (Object ref:doc.getRootElement().getChildren("reference")) {
                Element pmidEl = ((Element)ref).getChild("pubmed-id");
                if (pmidEl != null)
                    this.pubmedID = pmidEl.getTextTrim();
            }
            this.date = new Date(rnamlFile.lastModified());
        }

        private class RNAMotifSequence {
            private String sequenceID;
            private String sequence;
            private int startPosition, endPosition;
            private List<Residue3D> residues3D;

            private RNAMotifSequence(String sequenceID, String sequence, List<Residue3D> residues3D) {
                this(sequenceID, sequence, residues3D, -1, -1);
            }

            private RNAMotifSequence(String sequenceID, String sequence, List<Residue3D> residues3D, int startPosition, int endPosition) {
                this.sequenceID = sequenceID;
                this.sequence = sequence;
                this.residues3D = residues3D;
                this.startPosition = startPosition;
                this.endPosition = endPosition;
            }
        }
    }

    public static Map<String,String> modifiedNucleotides;

    static {
        modifiedNucleotides = new HashMap<String,String>();
        modifiedNucleotides.put("T","U");
        modifiedNucleotides.put("PSU","U");
        modifiedNucleotides.put("I","A");
        modifiedNucleotides.put("+A","A");
        modifiedNucleotides.put("+C","C");
        modifiedNucleotides.put("+G","G");
        modifiedNucleotides.put("+I","A");
        modifiedNucleotides.put("+T","U");
        modifiedNucleotides.put("+U","U");
        modifiedNucleotides.put("PU","A");
        modifiedNucleotides.put("YG","G");
        modifiedNucleotides.put("1AP","G");
        modifiedNucleotides.put("1MA","A");
        modifiedNucleotides.put("1MG","G");
        modifiedNucleotides.put("2DA","A");
        modifiedNucleotides.put("2DT","U");
        modifiedNucleotides.put("2MA","A");
        modifiedNucleotides.put("2MG","G");
        modifiedNucleotides.put("4SC","C");
        modifiedNucleotides.put("4SU","U");
        modifiedNucleotides.put("5IU","U");
        modifiedNucleotides.put("5MC","C");
        modifiedNucleotides.put("5MU","U");
        modifiedNucleotides.put("5NC","C");
        modifiedNucleotides.put("6MP","A");
        modifiedNucleotides.put("7MG","G");
        modifiedNucleotides.put("A23","A");
        modifiedNucleotides.put("AD2","A");
        modifiedNucleotides.put("AET","A");
        modifiedNucleotides.put("AMD","A");
        modifiedNucleotides.put("AMP","A");
        modifiedNucleotides.put("APN","A");
        modifiedNucleotides.put("ATP","A");
        modifiedNucleotides.put("AZT","U");
        modifiedNucleotides.put("CCC","C");
        modifiedNucleotides.put("CMP","A");
        modifiedNucleotides.put("CPN","C");
        modifiedNucleotides.put("DAD","A");
        modifiedNucleotides.put("DCT","C");
        modifiedNucleotides.put("DDG","G");
        modifiedNucleotides.put("DG3","G");
        modifiedNucleotides.put("DHU","U");
        modifiedNucleotides.put("DOC","C");
        modifiedNucleotides.put("EDA","A");
        modifiedNucleotides.put("G7M","G");
        modifiedNucleotides.put("GDP","G");
        modifiedNucleotides.put("GNP","G");
        modifiedNucleotides.put("GPN","G");
        modifiedNucleotides.put("GTP","G");
        modifiedNucleotides.put("GUN","G");
        modifiedNucleotides.put("H2U","U");
        modifiedNucleotides.put("HPA","A");
        modifiedNucleotides.put("IPN","U");
        modifiedNucleotides.put("M2G","G");
        modifiedNucleotides.put("MGT","G");
        modifiedNucleotides.put("MIA","A");
        modifiedNucleotides.put("OMC","C");
        modifiedNucleotides.put("OMG","G");
        modifiedNucleotides.put("OMU","U");
        modifiedNucleotides.put("ONE","U");
        modifiedNucleotides.put("P2U","U");
        modifiedNucleotides.put("PGP","G");
        modifiedNucleotides.put("PPU","A");
        modifiedNucleotides.put("PRN","A");
        modifiedNucleotides.put("PST","U");
        modifiedNucleotides.put("QSI","A");
        modifiedNucleotides.put("QUO","G");
        modifiedNucleotides.put("RIA","A");
        modifiedNucleotides.put("SAH","A");
        modifiedNucleotides.put("SAM","A");
        modifiedNucleotides.put("T23","U");
        modifiedNucleotides.put("T6A","A");
        modifiedNucleotides.put("TAF","U");
        modifiedNucleotides.put("TLC","U");
        modifiedNucleotides.put("TPN","U");
        modifiedNucleotides.put("TSP","U");
        modifiedNucleotides.put("TTP","U");
        modifiedNucleotides.put("UCP","U");
        modifiedNucleotides.put("VAA","A");
        modifiedNucleotides.put("YYG","G");
        modifiedNucleotides.put("70U","U");
        modifiedNucleotides.put("12A","A");
        modifiedNucleotides.put("2MU","U");
        modifiedNucleotides.put("127","U");
        modifiedNucleotides.put("125","U");
        modifiedNucleotides.put("126","U");
        modifiedNucleotides.put("MEP","U");
        modifiedNucleotides.put("TLN","U");
        modifiedNucleotides.put("ADP","A");
        modifiedNucleotides.put("TTE","U");
        modifiedNucleotides.put("PYO","U");
        modifiedNucleotides.put("SUR","U");
        modifiedNucleotides.put("PSD","A");
        modifiedNucleotides.put("S4U","U");
        modifiedNucleotides.put("CP1","C");
        modifiedNucleotides.put("TP1","U");
        modifiedNucleotides.put("NEA","A");
        modifiedNucleotides.put("GCK","C");
        modifiedNucleotides.put("CH","C");
        modifiedNucleotides.put("EDC","G");
        modifiedNucleotides.put("DFC","C");
        modifiedNucleotides.put("DFG","G");
        modifiedNucleotides.put("DRT","U");
        modifiedNucleotides.put("2AR","A");
        modifiedNucleotides.put("8OG","G");
        modifiedNucleotides.put("IG","G");
        modifiedNucleotides.put("IC","C");
        modifiedNucleotides.put("IGU","G");
        modifiedNucleotides.put("IMC","C");
        modifiedNucleotides.put("GAO","G");
        modifiedNucleotides.put("UAR","U");
        modifiedNucleotides.put("CAR","C");
        modifiedNucleotides.put("PPZ","A");
        modifiedNucleotides.put("M1G","G");
        modifiedNucleotides.put("ABR","A");
        modifiedNucleotides.put("ABS","A");
        modifiedNucleotides.put("S6G","G");
        modifiedNucleotides.put("HEU","U");
        modifiedNucleotides.put("P","G");
        modifiedNucleotides.put("DNR","C");
        modifiedNucleotides.put("MCY","C");
        modifiedNucleotides.put("TCP","U");
        modifiedNucleotides.put("LGP","G");
        modifiedNucleotides.put("GSR","G");
        modifiedNucleotides.put("E","A");
        modifiedNucleotides.put("GSS","G");
        modifiedNucleotides.put("THX","U");
        modifiedNucleotides.put("6CT","U");
        modifiedNucleotides.put("TEP","G");
        modifiedNucleotides.put("GN7","G");
        modifiedNucleotides.put("FAG","G");
        modifiedNucleotides.put("PDU","U");
        modifiedNucleotides.put("MA6","A");
        modifiedNucleotides.put("UMP","U");
        modifiedNucleotides.put("SC","C");
        modifiedNucleotides.put("GS","G");
        modifiedNucleotides.put("TS","U");
        modifiedNucleotides.put("AS","A");
        modifiedNucleotides.put("ATD","U");
        modifiedNucleotides.put("T3P","U");
        modifiedNucleotides.put("5AT","U");
        modifiedNucleotides.put("MMT","U");
        modifiedNucleotides.put("SRA","A");
        modifiedNucleotides.put("6HG","G");
        modifiedNucleotides.put("6HC","C");
        modifiedNucleotides.put("6HT","U");
        modifiedNucleotides.put("6HA","A");
        modifiedNucleotides.put("55C","C");
        modifiedNucleotides.put("U8U","U");
        modifiedNucleotides.put("BRO","U");
        modifiedNucleotides.put("BRU","U");
        modifiedNucleotides.put("5IT","U");
        modifiedNucleotides.put("ADI","A");
        modifiedNucleotides.put("5CM","C");
        modifiedNucleotides.put("IMP","G");
        modifiedNucleotides.put("THM","U");
        modifiedNucleotides.put("URI","U");
        modifiedNucleotides.put("AMO","A");
        modifiedNucleotides.put("FHU","P");
        modifiedNucleotides.put("TSB","A");
        modifiedNucleotides.put("CMR","C");
        modifiedNucleotides.put("RMP","A");
        modifiedNucleotides.put("SMP","A");
        modifiedNucleotides.put("5HT","U");
        modifiedNucleotides.put("RT","U");
        modifiedNucleotides.put("MAD","A");
        modifiedNucleotides.put("OXG","G");
        modifiedNucleotides.put("UDP","U");
        modifiedNucleotides.put("6MA","A");
        modifiedNucleotides.put("5IC","C");
        modifiedNucleotides.put("SPT","U");
        modifiedNucleotides.put("TGP","G");
        modifiedNucleotides.put("BLS","A");
        modifiedNucleotides.put("64T","U");
        modifiedNucleotides.put("CB2","C");
        modifiedNucleotides.put("DCP","C");
        modifiedNucleotides.put("ANG","G");
        modifiedNucleotides.put("BRG","G");
        modifiedNucleotides.put("Z","A");
        modifiedNucleotides.put("AVC","A");
        modifiedNucleotides.put("5CG","G");
        modifiedNucleotides.put("UDP","U");
        modifiedNucleotides.put("UMS","U");
        modifiedNucleotides.put("BGM","G");
        modifiedNucleotides.put("SMT","U");
        modifiedNucleotides.put("DU","U");
        modifiedNucleotides.put("CH1","C");
        modifiedNucleotides.put("GH3","G");
        modifiedNucleotides.put("GNG","G");
        modifiedNucleotides.put("TFT","U");
        modifiedNucleotides.put("U3H","U");
        modifiedNucleotides.put("MRG","G");
        modifiedNucleotides.put("ATM","U");
        modifiedNucleotides.put("GOM","A");
        modifiedNucleotides.put("UBB","U");
        modifiedNucleotides.put("A66","A");
        modifiedNucleotides.put("T66","U");
        modifiedNucleotides.put("C66","C");
        modifiedNucleotides.put("3ME","A");
        modifiedNucleotides.put("A3P","A");
        modifiedNucleotides.put("ANP","A");
        modifiedNucleotides.put("FA2","A");
        modifiedNucleotides.put("9DG","G");
        modifiedNucleotides.put("GMU","U");
        modifiedNucleotides.put("UTP","U");
        modifiedNucleotides.put("5BU","U");
        modifiedNucleotides.put("APC","A");
        modifiedNucleotides.put("DI","A");
        modifiedNucleotides.put("UR3","U");
        modifiedNucleotides.put("3DA","A");
        modifiedNucleotides.put("DDY","C");
        modifiedNucleotides.put("TTD","U");
        modifiedNucleotides.put("TFO","U");
        modifiedNucleotides.put("TNV","U");
        modifiedNucleotides.put("MTU","U");
        modifiedNucleotides.put("6OG","G");
        modifiedNucleotides.put("E1X","A");
        modifiedNucleotides.put("FOX","A");
        modifiedNucleotides.put("CTP","C");
        modifiedNucleotides.put("D3T","U");
        modifiedNucleotides.put("TPC","C");
        modifiedNucleotides.put("7DA","A");
        modifiedNucleotides.put("7GU","U");
        modifiedNucleotides.put("2PR","A");
        modifiedNucleotides.put("CBR","C");
        modifiedNucleotides.put("I5C","C");
        modifiedNucleotides.put("5FC","C");
        modifiedNucleotides.put("GMS","G");
        modifiedNucleotides.put("2BT","U");
        modifiedNucleotides.put("8FG","G");
        modifiedNucleotides.put("MNU","U");
        modifiedNucleotides.put("AGS","A");
        modifiedNucleotides.put("NMT","U");
        modifiedNucleotides.put("NMS","U");
        modifiedNucleotides.put("UPG","U");
        modifiedNucleotides.put("G2P","G");
        modifiedNucleotides.put("2NT","U");
        modifiedNucleotides.put("EIT","U");
        modifiedNucleotides.put("TFE","U");
        modifiedNucleotides.put("P2T","U");
        modifiedNucleotides.put("2AT","U");
        modifiedNucleotides.put("2GT","U");
        modifiedNucleotides.put("2OT","U");
        modifiedNucleotides.put("BOE","U");
        modifiedNucleotides.put("SFG","G");
        modifiedNucleotides.put("CSL","A");
        modifiedNucleotides.put("PPW","G");
        modifiedNucleotides.put("IU","U");
        modifiedNucleotides.put("D5M","A");
        modifiedNucleotides.put("ZDU","U");
        modifiedNucleotides.put("DGT","U");
        modifiedNucleotides.put("UD5","U");
        modifiedNucleotides.put("S4C","C");
        modifiedNucleotides.put("DTP","A");
        modifiedNucleotides.put("5AA","A");
        modifiedNucleotides.put("2OP","A");
        modifiedNucleotides.put("PO2","A");
        modifiedNucleotides.put("DC","C");
        modifiedNucleotides.put("DA","A");
        modifiedNucleotides.put("LOF","A");
        modifiedNucleotides.put("ACA","A");
        modifiedNucleotides.put("BTN","A");
        modifiedNucleotides.put("PAE","A");
        modifiedNucleotides.put("SPS","A");
        modifiedNucleotides.put("TSE","A");
        modifiedNucleotides.put("A2M","A");
        modifiedNucleotides.put("NCO","A");
        modifiedNucleotides.put("A5M","C");
        modifiedNucleotides.put("M5M","C");
        modifiedNucleotides.put("S2M","U");
        modifiedNucleotides.put("MSP","A");
        modifiedNucleotides.put("P1P","A");
        modifiedNucleotides.put("N6G","G");
        modifiedNucleotides.put("MA7","A");
        modifiedNucleotides.put("FE2","G");
        modifiedNucleotides.put("AKG","G");
        modifiedNucleotides.put("SIN","G");
        modifiedNucleotides.put("PR5","G");
        modifiedNucleotides.put("GOL","G");
        modifiedNucleotides.put("XCY","G");
        modifiedNucleotides.put("5HU","U");
        modifiedNucleotides.put("CME","C");
        modifiedNucleotides.put("EGL","G");
        modifiedNucleotides.put("LC","C");
        modifiedNucleotides.put("LHU","U");
        modifiedNucleotides.put("LG","G");
        modifiedNucleotides.put("PUY","U");
        modifiedNucleotides.put("PO4","U");
        modifiedNucleotides.put("PQ1","U");
        modifiedNucleotides.put("ROB","U");
        modifiedNucleotides.put("O2C","C");
        modifiedNucleotides.put("C30","C");
        modifiedNucleotides.put("C31","C");
        modifiedNucleotides.put("C32","C");
        modifiedNucleotides.put("C33","C");
        modifiedNucleotides.put("C34","C");
        modifiedNucleotides.put("C35","C");
        modifiedNucleotides.put("C36","C");
        modifiedNucleotides.put("C37","C");
        modifiedNucleotides.put("C38","C");
        modifiedNucleotides.put("C39","C");
        modifiedNucleotides.put("C40","C");
        modifiedNucleotides.put("C41","C");
        modifiedNucleotides.put("C42","C");
        modifiedNucleotides.put("C43","C");
        modifiedNucleotides.put("C44","C");
        modifiedNucleotides.put("C45","C");
        modifiedNucleotides.put("C46","C");
        modifiedNucleotides.put("C47","C");
        modifiedNucleotides.put("C48","C");
        modifiedNucleotides.put("C49","C");
        modifiedNucleotides.put("C50","C");
        modifiedNucleotides.put("A30","A");
        modifiedNucleotides.put("A31","A");
        modifiedNucleotides.put("A32","A");
        modifiedNucleotides.put("A33","A");
        modifiedNucleotides.put("A34","A");
        modifiedNucleotides.put("A35","A");
        modifiedNucleotides.put("A36","A");
        modifiedNucleotides.put("A37","A");
        modifiedNucleotides.put("A38","A");
        modifiedNucleotides.put("A39","A");
        modifiedNucleotides.put("A40","A");
        modifiedNucleotides.put("A41","A");
        modifiedNucleotides.put("A42","A");
        modifiedNucleotides.put("A43","A");
        modifiedNucleotides.put("A44","A");
        modifiedNucleotides.put("A45","A");
        modifiedNucleotides.put("A46","A");
        modifiedNucleotides.put("A47","A");
        modifiedNucleotides.put("A48","A");
        modifiedNucleotides.put("A49","A");
        modifiedNucleotides.put("A50","A");
        modifiedNucleotides.put("G30","G");
        modifiedNucleotides.put("G31","G");
        modifiedNucleotides.put("G32","G");
        modifiedNucleotides.put("G33","G");
        modifiedNucleotides.put("G34","G");
        modifiedNucleotides.put("G35","G");
        modifiedNucleotides.put("G36","G");
        modifiedNucleotides.put("G37","G");
        modifiedNucleotides.put("G38","G");
        modifiedNucleotides.put("G39","G");
        modifiedNucleotides.put("G40","G");
        modifiedNucleotides.put("G41","G");
        modifiedNucleotides.put("G42","G");
        modifiedNucleotides.put("G43","G");
        modifiedNucleotides.put("G44","G");
        modifiedNucleotides.put("G45","G");
        modifiedNucleotides.put("G46","G");
        modifiedNucleotides.put("G47","G");
        modifiedNucleotides.put("G48","G");
        modifiedNucleotides.put("G49","G");
        modifiedNucleotides.put("G50","G");
        modifiedNucleotides.put("T30","U");
        modifiedNucleotides.put("T31","U");
        modifiedNucleotides.put("T32","U");
        modifiedNucleotides.put("T33","U");
        modifiedNucleotides.put("T34","U");
        modifiedNucleotides.put("T35","U");
        modifiedNucleotides.put("T36","U");
        modifiedNucleotides.put("T37","U");
        modifiedNucleotides.put("T38","U");
        modifiedNucleotides.put("T39","U");
        modifiedNucleotides.put("T40","U");
        modifiedNucleotides.put("T41","U");
        modifiedNucleotides.put("T42","U");
        modifiedNucleotides.put("T43","U");
        modifiedNucleotides.put("T44","U");
        modifiedNucleotides.put("T45","U");
        modifiedNucleotides.put("T46","U");
        modifiedNucleotides.put("T47","U");
        modifiedNucleotides.put("T48","U");
        modifiedNucleotides.put("T49","U");
        modifiedNucleotides.put("T50","U");
        modifiedNucleotides.put("U30","U");
        modifiedNucleotides.put("U31","U");
        modifiedNucleotides.put("U32","U");
        modifiedNucleotides.put("U33","U");
        modifiedNucleotides.put("U34","U");
        modifiedNucleotides.put("U35","U");
        modifiedNucleotides.put("U36","U");
        modifiedNucleotides.put("U37","U");
        modifiedNucleotides.put("U38","U");
        modifiedNucleotides.put("U39","U");
        modifiedNucleotides.put("U40","U");
        modifiedNucleotides.put("U41","U");
        modifiedNucleotides.put("U42","U");
        modifiedNucleotides.put("U43","U");
        modifiedNucleotides.put("U44","U");
        modifiedNucleotides.put("U45","U");
        modifiedNucleotides.put("U46","U");
        modifiedNucleotides.put("U47","U");
        modifiedNucleotides.put("U48","U");
        modifiedNucleotides.put("U49","U");
        modifiedNucleotides.put("U50","U");
        modifiedNucleotides.put("UFP","U");
        modifiedNucleotides.put("UFR","U");
        modifiedNucleotides.put("UCL","U");
        modifiedNucleotides.put("3DR","U");
        modifiedNucleotides.put("CBV","C");
        modifiedNucleotides.put("HFA","A");
        modifiedNucleotides.put("MMA","A");
        modifiedNucleotides.put("DCZ","C");
        modifiedNucleotides.put("GNE","C");
        modifiedNucleotides.put("A1P","A");
        modifiedNucleotides.put("6IA","A");
        modifiedNucleotides.put("CTG","G");
        modifiedNucleotides.put("5FU","U");
        modifiedNucleotides.put("2AD","A");
        modifiedNucleotides.put("T2T","U");
        modifiedNucleotides.put("XUG","G");
        modifiedNucleotides.put("2ST","U");
        modifiedNucleotides.put("5PY","U");
        modifiedNucleotides.put("4PC","C");
        modifiedNucleotides.put("US1","U");
        modifiedNucleotides.put("M5C","C");
        modifiedNucleotides.put("DG","G");
        modifiedNucleotides.put("DA","A");
        modifiedNucleotides.put("DT","U");
        modifiedNucleotides.put("DC","C");
        modifiedNucleotides.put("P5P","A");
        modifiedNucleotides.put("FMU","U");
    }

    public static final String[] genomic_features_classes = new String[] { //see http://www.ddbj.nig.ac.jp/FT/full_index.html#7.2 for details
            "attenuator",
            "CAAT_signal",
            "CDS",
            "centromere",
            "enhancer",
            "exon",
            "GC_signal",
            "gene",
            "intron",
            "LTR",
            "mat_peptide",
            "misc_binding",
            "misc_difference",
            "misc_feature",
            "misc_recomb",
            "misc_RNA",
            "misc_signal",
            "misc_structure",
            "mobile_element",
            "modified_base",
            "mRNA",
            "ncRNA",
            "operon",
            "oriT",
            "polyA_signal",
            "polyA_site",
            "precursor_RNA",
            "prim_transcript",
            "primer_bind",
            "promoter",
            "protein_bind",
            "RBS",
            "repeat_region",
            "rep_origin",
            "sig_peptide",
            "stem_loop",
            "STS",
            "TATA_signal",
            "telomere",
            "terminator",
            "transit_peptide",
            "unsure",
            "variation",
            "3'UTR",
            "5'UTR",
            "-10_signal",
            "-35_signal"
    };

    public static final String[] ncRNA_classes = new String[] { //all theses classes are coming from RFAM
            "Cis-reg",
            "Cis-reg, IRES",
            "Cis-reg, frameshift_element",
            "Cis-reg, leader",
            "Cis-reg, riboswitch",
            "Cis-reg, thermoregulator",
            "Gene",
            "Gene, CRISPR",
            "Gene, antisense",
            "Gene, antitoxin",
            "Gene, lncRNA",
            "Gene, miRNA",
            "Gene, rRNA",
            "Gene, ribozyme",
            "Gene, sRNA",
            "Gene, snRNA",
            "Gene, snRNA, snoRNA, CD-box",
            "Gene, snRNA, snoRNA, HACA-box",
            "Gene, snRNA, snoRNA, scaRNA",
            "Gene, snRNA, splicing",
            "Gene, tRNA",
            "intron",
            "RNA 2D/3D motif" //this one is Assemble2 specific
    };

    public static final Map<String, List<String>> ncRNA_ids = new HashMap<String, List<String>>();

    static { //this code is generated with the python script get_rfam_ids.py in pyrna
        List<String> ids = null;
        ids = new ArrayList<String>();
        ncRNA_ids.put("intron", ids);
        ids.add("GIR1");
        ids.add("Intron_gpI");
        ids.add("Intron_gpII");
        ids.add("group-II-D1D4-1");
        ids.add("group-II-D1D4-2");
        ids.add("group-II-D1D4-3");
        ids.add("group-II-D1D4-4");
        ids.add("group-II-D1D4-5");
        ids.add("group-II-D1D4-6");
        ids.add("group-II-D1D4-7");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, snRNA, snoRNA, CD-box", ids);
        ids.add("ACEA_U3");
        ids.add("Afu_190");
        ids.add("Afu_191");
        ids.add("Afu_198");
        ids.add("Afu_199");
        ids.add("Afu_264");
        ids.add("Afu_294");
        ids.add("Afu_298");
        ids.add("Afu_300");
        ids.add("Afu_304");
        ids.add("Afu_335");
        ids.add("Afu_455");
        ids.add("Afu_513");
        ids.add("Afu_514");
        ids.add("DdR1");
        ids.add("DdR10");
        ids.add("DdR11");
        ids.add("DdR12");
        ids.add("DdR13");
        ids.add("DdR14");
        ids.add("DdR15");
        ids.add("DdR16");
        ids.add("DdR17");
        ids.add("DdR2");
        ids.add("DdR4");
        ids.add("DdR5");
        ids.add("DdR6");
        ids.add("DdR7");
        ids.add("DdR8");
        ids.add("Fungi_U3");
        ids.add("Plant_U3");
        ids.add("SNORD10");
        ids.add("SNORD100");
        ids.add("SNORD101");
        ids.add("SNORD102");
        ids.add("SNORD103");
        ids.add("SNORD105");
        ids.add("SNORD107");
        ids.add("SNORD108");
        ids.add("SNORD109A");
        ids.add("SNORD11");
        ids.add("SNORD110");
        ids.add("SNORD111");
        ids.add("SNORD112");
        ids.add("SNORD113");
        ids.add("SNORD115");
        ids.add("SNORD116");
        ids.add("SNORD11B");
        ids.add("SNORD12");
        ids.add("SNORD121A");
        ids.add("SNORD123");
        ids.add("SNORD124");
        ids.add("SNORD125");
        ids.add("SNORD126");
        ids.add("SNORD127");
        ids.add("SNORD14");
        ids.add("SNORD15");
        ids.add("SNORD16");
        ids.add("SNORD17");
        ids.add("SNORD18");
        ids.add("SNORD19");
        ids.add("SNORD19B");
        ids.add("SNORD2");
        ids.add("SNORD20");
        ids.add("SNORD21");
        ids.add("SNORD22");
        ids.add("SNORD23");
        ids.add("SNORD24");
        ids.add("SNORD25");
        ids.add("SNORD26");
        ids.add("SNORD27");
        ids.add("SNORD28");
        ids.add("SNORD29");
        ids.add("SNORD30");
        ids.add("SNORD31");
        ids.add("SNORD33");
        ids.add("SNORD34");
        ids.add("SNORD35");
        ids.add("SNORD36");
        ids.add("SNORD37");
        ids.add("SNORD38");
        ids.add("SNORD39");
        ids.add("SNORD41");
        ids.add("SNORD42");
        ids.add("SNORD43");
        ids.add("SNORD44");
        ids.add("SNORD45");
        ids.add("SNORD46");
        ids.add("SNORD47");
        ids.add("SNORD48");
        ids.add("SNORD49");
        ids.add("SNORD5");
        ids.add("SNORD50");
        ids.add("SNORD51");
        ids.add("SNORD52");
        ids.add("SNORD53_SNORD92");
        ids.add("SNORD56");
        ids.add("SNORD57");
        ids.add("SNORD58");
        ids.add("SNORD59");
        ids.add("SNORD60");
        ids.add("SNORD61");
        ids.add("SNORD62");
        ids.add("SNORD63");
        ids.add("SNORD64");
        ids.add("SNORD65");
        ids.add("SNORD66");
        ids.add("SNORD67");
        ids.add("SNORD69");
        ids.add("SNORD70");
        ids.add("SNORD72");
        ids.add("SNORD73");
        ids.add("SNORD74");
        ids.add("SNORD75");
        ids.add("SNORD77");
        ids.add("SNORD78");
        ids.add("SNORD79");
        ids.add("SNORD81");
        ids.add("SNORD82");
        ids.add("SNORD83");
        ids.add("SNORD86");
        ids.add("SNORD87");
        ids.add("SNORD88");
        ids.add("SNORD89");
        ids.add("SNORD90");
        ids.add("SNORD91");
        ids.add("SNORD93");
        ids.add("SNORD94");
        ids.add("SNORD95");
        ids.add("SNORD96");
        ids.add("SNORD97");
        ids.add("SNORD98");
        ids.add("SNORD99");
        ids.add("SNORND104");
        ids.add("U3");
        ids.add("U54");
        ids.add("U8");
        ids.add("ceN103");
        ids.add("ceN106");
        ids.add("ceN108");
        ids.add("ceN109");
        ids.add("ceN111");
        ids.add("ceN113");
        ids.add("ceN114");
        ids.add("ceN22");
        ids.add("ceN27");
        ids.add("ceN28");
        ids.add("ceN30");
        ids.add("ceN33");
        ids.add("ceN40");
        ids.add("ceN44");
        ids.add("ceN47");
        ids.add("ceN53");
        ids.add("ceN54");
        ids.add("ceN61");
        ids.add("ceN63");
        ids.add("ceN65");
        ids.add("ceN69");
        ids.add("ceN70");
        ids.add("ceN86");
        ids.add("ceN88");
        ids.add("ceN89");
        ids.add("plasmodium_snoR14");
        ids.add("plasmodium_snoR16");
        ids.add("plasmodium_snoR17");
        ids.add("plasmodium_snoR20");
        ids.add("plasmodium_snoR21");
        ids.add("plasmodium_snoR24");
        ids.add("plasmodium_snoR26");
        ids.add("plasmodium_snoR28");
        ids.add("plasmodium_snoR30");
        ids.add("sR-tMet");
        ids.add("sR1");
        ids.add("sR10");
        ids.add("sR11");
        ids.add("sR12");
        ids.add("sR13");
        ids.add("sR14");
        ids.add("sR15");
        ids.add("sR16");
        ids.add("sR17");
        ids.add("sR18");
        ids.add("sR19");
        ids.add("sR2");
        ids.add("sR20");
        ids.add("sR21");
        ids.add("sR22");
        ids.add("sR23");
        ids.add("sR24");
        ids.add("sR28");
        ids.add("sR3");
        ids.add("sR30");
        ids.add("sR32");
        ids.add("sR33");
        ids.add("sR34");
        ids.add("sR35");
        ids.add("sR36");
        ids.add("sR38");
        ids.add("sR39");
        ids.add("sR4");
        ids.add("sR40");
        ids.add("sR41");
        ids.add("sR42");
        ids.add("sR43");
        ids.add("sR44");
        ids.add("sR45");
        ids.add("sR46");
        ids.add("sR47");
        ids.add("sR48");
        ids.add("sR49");
        ids.add("sR5");
        ids.add("sR51");
        ids.add("sR52");
        ids.add("sR53");
        ids.add("sR55");
        ids.add("sR58");
        ids.add("sR6");
        ids.add("sR60");
        ids.add("sR7");
        ids.add("sR8");
        ids.add("sR9");
        ids.add("sn1185");
        ids.add("sn1502");
        ids.add("sn2317");
        ids.add("sn2343");
        ids.add("sn2417");
        ids.add("sn2429");
        ids.add("sn2524");
        ids.add("sn2841");
        ids.add("sn2903");
        ids.add("sn2991");
        ids.add("sn3060");
        ids.add("sn3071");
        ids.add("sn668");
        ids.add("snR13");
        ids.add("snR190");
        ids.add("snR39");
        ids.add("snR4");
        ids.add("snR40");
        ids.add("snR41");
        ids.add("snR45");
        ids.add("snR47");
        ids.add("snR50");
        ids.add("snR51");
        ids.add("snR52");
        ids.add("snR56");
        ids.add("snR58");
        ids.add("snR62");
        ids.add("snR63");
        ids.add("snR67");
        ids.add("snR68");
        ids.add("snR70");
        ids.add("snR73");
        ids.add("snR75");
        ids.add("snR76");
        ids.add("snR77");
        ids.add("snR78");
        ids.add("snR79");
        ids.add("snR87");
        ids.add("snoCD11");
        ids.add("snoJ26");
        ids.add("snoJ33");
        ids.add("snoMBII-202");
        ids.add("snoMe18S-Gm1358");
        ids.add("snoMe18S-Um1356");
        ids.add("snoMe28S-Am2589");
        ids.add("snoMe28S-Am2634");
        ids.add("snoMe28S-Am982");
        ids.add("snoMe28S-Cm2645");
        ids.add("snoMe28S-Cm3227");
        ids.add("snoMe28S-Cm788");
        ids.add("snoMe28S-G3255");
        ids.add("snoMe28S-Gm1083");
        ids.add("snoMe28S-Gm3113");
        ids.add("snoMe28S-U3344");
        ids.add("snoPyro_CD");
        ids.add("snoR01");
        ids.add("snoR07");
        ids.add("snoR09");
        ids.add("snoR1");
        ids.add("snoR10");
        ids.add("snoR101");
        ids.add("snoR11");
        ids.add("snoR113");
        ids.add("snoR114");
        ids.add("snoR116");
        ids.add("snoR117");
        ids.add("snoR118");
        ids.add("snoR12");
        ids.add("snoR121");
        ids.add("snoR126");
        ids.add("snoR127");
        ids.add("snoR128");
        ids.add("snoR13");
        ids.add("snoR130");
        ids.add("snoR14");
        ids.add("snoR15");
        ids.add("snoR16");
        ids.add("snoR160");
        ids.add("snoR17");
        ids.add("snoR18");
        ids.add("snoR19");
        ids.add("snoR20");
        ids.add("snoR20a");
        ids.add("snoR21");
        ids.add("snoR22");
        ids.add("snoR23");
        ids.add("snoR24");
        ids.add("snoR25");
        ids.add("snoR26");
        ids.add("snoR27");
        ids.add("snoR28");
        ids.add("snoR29");
        ids.add("snoR30");
        ids.add("snoR31");
        ids.add("snoR31_Z110_Z27");
        ids.add("snoR32_R81");
        ids.add("snoR35");
        ids.add("snoR38");
        ids.add("snoR4");
        ids.add("snoR41");
        ids.add("snoR43");
        ids.add("snoR442");
        ids.add("snoR44_J54");
        ids.add("snoR4a");
        ids.add("snoR53");
        ids.add("snoR53Y");
        ids.add("snoR60");
        ids.add("snoR64");
        ids.add("snoR64a");
        ids.add("snoR66");
        ids.add("snoR69Y");
        ids.add("snoR71");
        ids.add("snoR72");
        ids.add("snoR77Y");
        ids.add("snoR79");
        ids.add("snoR8a");
        ids.add("snoR9");
        ids.add("snoR9_plant");
        ids.add("snoTBR17");
        ids.add("snoTBR5");
        ids.add("snoTBR7");
        ids.add("snoU105B");
        ids.add("snoU13");
        ids.add("snoU18");
        ids.add("snoU2-30");
        ids.add("snoU25");
        ids.add("snoU2_19");
        ids.add("snoU30");
        ids.add("snoU31b");
        ids.add("snoU36a");
        ids.add("snoU43");
        ids.add("snoU49");
        ids.add("snoU54");
        ids.add("snoU6-47");
        ids.add("snoU6-53");
        ids.add("snoU61");
        ids.add("snoU82P");
        ids.add("snoU83");
        ids.add("snoU83B");
        ids.add("snoU83D");
        ids.add("snoZ101");
        ids.add("snoZ102_R77");
        ids.add("snoZ103");
        ids.add("snoZ105");
        ids.add("snoZ107_R87");
        ids.add("snoZ112");
        ids.add("snoZ118");
        ids.add("snoZ119");
        ids.add("snoZ122");
        ids.add("snoZ13_snr52");
        ids.add("snoZ152");
        ids.add("snoZ155");
        ids.add("snoZ157");
        ids.add("snoZ159");
        ids.add("snoZ161_228");
        ids.add("snoZ162");
        ids.add("snoZ163");
        ids.add("snoZ165");
        ids.add("snoZ168");
        ids.add("snoZ169");
        ids.add("snoZ17");
        ids.add("snoZ173");
        ids.add("snoZ175");
        ids.add("snoZ178");
        ids.add("snoZ182");
        ids.add("snoZ185");
        ids.add("snoZ188");
        ids.add("snoZ194");
        ids.add("snoZ196");
        ids.add("snoZ199");
        ids.add("snoZ206");
        ids.add("snoZ221_snoR21b");
        ids.add("snoZ223");
        ids.add("snoZ242");
        ids.add("snoZ247");
        ids.add("snoZ248");
        ids.add("snoZ256");
        ids.add("snoZ266");
        ids.add("snoZ267");
        ids.add("snoZ278");
        ids.add("snoZ279_R105_R108");
        ids.add("snoZ30");
        ids.add("snoZ30a");
        ids.add("snoZ39");
        ids.add("snoZ40");
        ids.add("snoZ43");
        ids.add("snoZ5");
        ids.add("snoZ6");
        ids.add("snoZ7");
        ids.add("snosnR48");
        ids.add("snosnR54");
        ids.add("snosnR55");
        ids.add("snosnR57");
        ids.add("snosnR60_Z15");
        ids.add("snosnR61");
        ids.add("snosnR64");
        ids.add("snosnR66");
        ids.add("snosnR69");
        ids.add("snosnR71");
        ids.add("v-snoRNA-1");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg", ids);
        ids.add("23S-methyl");
        ids.add("6C");
        ids.add("ACAT");
        ids.add("AHBV_epsilon");
        ids.add("ALIL");
        ids.add("AMV_RNA1_SL");
        ids.add("Actino-pnp");
        ids.add("Alfamo_CPB");
        ids.add("Alpha_RBS");
        ids.add("ApoB_5_CRE");
        ids.add("BLV_package");
        ids.add("BMV3_UPD-PK1");
        ids.add("BMV3_UPD-PK3");
        ids.add("BTE");
        ids.add("BYDV_5_UTR");
        ids.add("BaMV_CRE");
        ids.add("Bacteroid-trp");
        ids.add("CAESAR");
        ids.add("CTV_rep_sig");
        ids.add("Cardiovirus_CRE");
        ids.add("Chlorobi-RRM");
        ids.add("Corona_SL-III");
        ids.add("Corona_package");
        ids.add("Corona_pk3");
        ids.add("DPB");
        ids.add("Downstream-peptide");
        ids.add("EAV_LTH");
        ids.add("Entero_5_CRE");
        ids.add("Entero_CRE");
        ids.add("Entero_OriR");
        ids.add("FIE3");
        ids.add("Flavi_CRE");
        ids.add("Flavivirus_DB");
        ids.add("Flavivirus_SLIV");
        ids.add("G-CSF_SLDE");
        ids.add("GABA3");
        ids.add("GAIT");
        ids.add("GP_knot1");
        ids.add("GP_knot2");
        ids.add("Gammaretro_CES");
        ids.add("Gurken");
        ids.add("HBV_epsilon");
        ids.add("HCV_ARF_SL");
        ids.add("HCV_SLIV");
        ids.add("HCV_SLVII");
        ids.add("HCV_X3");
        ids.add("HIV-1_DIS");
        ids.add("HIV-1_SD");
        ids.add("HIV-1_SL3");
        ids.add("HIV-1_SL4");
        ids.add("HIV_GSL3");
        ids.add("HIV_PBS");
        ids.add("HIV_POL-1_SL");
        ids.add("HLE");
        ids.add("HepC_CRE");
        ids.add("HepE_CRE");
        ids.add("Histone3");
        ids.add("IBV_D-RNA");
        ids.add("IFN_gamma");
        ids.add("IRE_I");
        ids.add("IRE_II");
        ids.add("JEV_hairpin");
        ids.add("JUMPstart");
        ids.add("K10_TLS");
        ids.add("K_chan_RES");
        ids.add("L17DE");
        ids.add("Lacto-rpoB");
        ids.add("Lnt");
        ids.add("MAT2A_A");
        ids.add("MAT2A_B");
        ids.add("MAT2A_C");
        ids.add("MAT2A_D");
        ids.add("MAT2A_E");
        ids.add("MAT2A_F");
        ids.add("MPMV_package");
        ids.add("Moco-II");
        ids.add("PK-BYV");
        ids.add("PK-CuYV_BPYV");
        ids.add("PK-HAV");
        ids.add("PK-IAV");
        ids.add("PK-PYVV");
        ids.add("PK-SPCSV");
        ids.add("PK-repBA");
        ids.add("PK-repZ");
        ids.add("PK1-TEV_CVMV");
        ids.add("PVX_3");
        ids.add("PYLIS_1");
        ids.add("Parecho_CRE");
        ids.add("Pedo-repair");
        ids.add("PhotoRC-I");
        ids.add("PhotoRC-II");
        ids.add("Pospi_RY");
        ids.add("Pox_AX_element");
        ids.add("Prion_pknot");
        ids.add("Pseudomon-Rho");
        ids.add("Pseudomon-groES");
        ids.add("PyrR");
        ids.add("R2_retro_el");
        ids.add("RCNMV_5UTR");
        ids.add("RCNMV_TE_DR1");
        ids.add("REN-SRE");
        ids.add("RRE");
        ids.add("RSV_PBS");
        ids.add("RSV_RNA");
        ids.add("Retro_dr1");
        ids.add("Rhino_CRE");
        ids.add("Rota_CRE");
        ids.add("RtT");
        ids.add("Rubella_3");
        ids.add("S-element");
        ids.add("SAM-Chlorobi");
        ids.add("SAM-II_long_loops");
        ids.add("SBRMV1_UPD-PKd");
        ids.add("SBRMV1_UPD-PKf");
        ids.add("SBWMV1_UPD-PKb");
        ids.add("SBWMV1_UPD-PKe");
        ids.add("SBWMV1_UPD-PKh");
        ids.add("SBWMV2_UPD-PKb");
        ids.add("SBWMV2_UPD-PKk");
        ids.add("SBWMV2_UPD-PKl");
        ids.add("SECIS_1");
        ids.add("SECIS_2");
        ids.add("SECIS_3");
        ids.add("SECIS_4");
        ids.add("SECIS_5");
        ids.add("Spi-1");
        ids.add("T-box");
        ids.add("TCV_H5");
        ids.add("TCV_Pr");
        ids.add("TLS-PK1");
        ids.add("TLS-PK2");
        ids.add("TLS-PK3");
        ids.add("TLS-PK4");
        ids.add("TLS-PK5");
        ids.add("TLS-PK6");
        ids.add("TMV_UPD-PK1");
        ids.add("TMV_UPD-PK2");
        ids.add("TMV_UPD-PK3");
        ids.add("Termite-flg");
        ids.add("Termite-leu");
        ids.add("Toga_5_CRE");
        ids.add("Tombus_3_III");
        ids.add("Tombus_3_IV");
        ids.add("Tombus_5");
        ids.add("Tombus_IRE");
        ids.add("TwoAYGGAY");
        ids.add("Tymo_tRNA-like");
        ids.add("U1A_PIE");
        ids.add("UPD-PK2");
        ids.add("UPD-PKc");
        ids.add("UPD-PKg");
        ids.add("UPD-PKib");
        ids.add("UPSK");
        ids.add("UnaL2");
        ids.add("Vimentin3");
        ids.add("WLE3");
        ids.add("atoC");
        ids.add("bicoid_3");
        ids.add("c-di-GMP-I");
        ids.add("cHP");
        ids.add("crcB");
        ids.add("epsC");
        ids.add("flg-Rhizobiales");
        ids.add("flpD");
        ids.add("gabT");
        ids.add("glnA");
        ids.add("gyrA");
        ids.add("hopC");
        ids.add("iscRS");
        ids.add("leu-phe_leader");
        ids.add("livK");
        ids.add("manA");
        ids.add("mini-ykkC");
        ids.add("mraW");
        ids.add("msiK");
        ids.add("nos_TCE");
        ids.add("nuoG");
        ids.add("p27_CRE");
        ids.add("pan");
        ids.add("pfl");
        ids.add("potC");
        ids.add("psaA");
        ids.add("psbNH");
        ids.add("purD");
        ids.add("radC");
        ids.add("rli51");
        ids.add("rli52");
        ids.add("rli53");
        ids.add("rli54");
        ids.add("rli56");
        ids.add("rli59");
        ids.add("rli61");
        ids.add("rli62");
        ids.add("rmf");
        ids.add("rncO");
        ids.add("rne-II");
        ids.add("rne5");
        ids.add("rox1");
        ids.add("rox2");
        ids.add("s2m");
        ids.add("satBaMV_CRE");
        ids.add("serC");
        ids.add("speF");
        ids.add("sucA");
        ids.add("sucA-II");
        ids.add("sucC");
        ids.add("sxy");
        ids.add("traJ-II");
        ids.add("traJ_5");
        ids.add("wcaG");
        ids.add("ybhL");
        ids.add("ydaO-yuaA");
        ids.add("yjdF");
        ids.add("ykkC-III");
        ids.add("ykkC-yxkD");
        ids.add("ykoK");
        ids.add("ylbH");
        ids.add("yybP-ykoY");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, sRNA", ids);
        ids.add("6S-Flavo");
        ids.add("ASdes");
        ids.add("ASpks");
        ids.add("Acido-1");
        ids.add("Acido-Lenti-1");
        ids.add("Afu_182");
        ids.add("Afu_203");
        ids.add("Afu_254");
        ids.add("Afu_309");
        ids.add("ArcZ");
        ids.add("Bacillaceae-1");
        ids.add("Bacillus-plasmid");
        ids.add("Bacteroidales-1");
        ids.add("Bacteroides-1");
        ids.add("BsrC");
        ids.add("BsrF");
        ids.add("BsrG");
        ids.add("C0299");
        ids.add("C0465");
        ids.add("C0719");
        ids.add("CC0196");
        ids.add("CC0734");
        ids.add("CC1840");
        ids.add("CC2171");
        ids.add("CC3510");
        ids.add("CC3513");
        ids.add("CC3552");
        ids.add("CC3664");
        ids.add("Chlorobi-1");
        ids.add("Chloroflexi-1");
        ids.add("Clostridiales-1");
        ids.add("Collinsella-1");
        ids.add("CrcZ");
        ids.add("CsrB");
        ids.add("CsrC");
        ids.add("CyaR_RyeE");
        ids.add("Cyano-1");
        ids.add("Cyano-2");
        ids.add("DdR35");
        ids.add("Dictyoglomi-1");
        ids.add("DsrA");
        ids.add("EBER1");
        ids.add("F6");
        ids.add("Flavo-1");
        ids.add("GadY");
        ids.add("GlmY_tke1");
        ids.add("GlmZ_SraJ");
        ids.add("Gut-1");
        ids.add("HPnc0580");
        ids.add("IS061");
        ids.add("IS102");
        ids.add("IS128");
        ids.add("Lacto-usp");
        ids.add("LhrA");
        ids.add("LhrC");
        ids.add("MIMT1_1");
        ids.add("Methylobacterium-1");
        ids.add("MicA");
        ids.add("MicC");
        ids.add("MicX");
        ids.add("MtlS");
        ids.add("Ocean-V");
        ids.add("OmrA-B");
        ids.add("OxyS");
        ids.add("P10");
        ids.add("P13");
        ids.add("P14");
        ids.add("P17");
        ids.add("P18");
        ids.add("P2");
        ids.add("P20");
        ids.add("P27");
        ids.add("P31");
        ids.add("P35");
        ids.add("P36");
        ids.add("P37");
        ids.add("P4");
        ids.add("P5");
        ids.add("P6");
        ids.add("P8");
        ids.add("Polynucleobacter-1");
        ids.add("PrrB_RsmZ");
        ids.add("PrrF");
        ids.add("Pseudomon-1");
        ids.add("Pxr");
        ids.add("Pyrobac-1");
        ids.add("QUAD");
        ids.add("Qrr");
        ids.add("RUF1");
        ids.add("RUF2");
        ids.add("RUF3");
        ids.add("RUF4");
        ids.add("RUF6-5");
        ids.add("Rhizobiales-2");
        ids.add("RprA");
        ids.add("RsaA");
        ids.add("RsaB");
        ids.add("RsaC");
        ids.add("RsaD");
        ids.add("RsaE");
        ids.add("RsaF");
        ids.add("RsaH");
        ids.add("RsaJ");
        ids.add("RsaOG");
        ids.add("RsmY");
        ids.add("RybB");
        ids.add("RydC");
        ids.add("RyeB");
        ids.add("RyhB");
        ids.add("SAR11_0636");
        ids.add("STAXI");
        ids.add("STnc150");
        ids.add("STnc170");
        ids.add("STnc180");
        ids.add("STnc210");
        ids.add("STnc220");
        ids.add("STnc230");
        ids.add("STnc240");
        ids.add("STnc250");
        ids.add("STnc260");
        ids.add("STnc280");
        ids.add("STnc290");
        ids.add("STnc300");
        ids.add("STnc310");
        ids.add("STnc320");
        ids.add("STnc340");
        ids.add("STnc350");
        ids.add("STnc361");
        ids.add("STnc370");
        ids.add("STnc380");
        ids.add("STnc390");
        ids.add("STnc40");
        ids.add("STnc410");
        ids.add("STnc420");
        ids.add("STnc430");
        ids.add("STnc440");
        ids.add("STnc450");
        ids.add("STnc460");
        ids.add("STnc470");
        ids.add("STnc490k");
        ids.add("STnc50");
        ids.add("STnc500");
        ids.add("STnc540");
        ids.add("STnc550");
        ids.add("STnc560");
        ids.add("STnc590");
        ids.add("STnc630");
        ids.add("Spot_42");
        ids.add("SprD");
        ids.add("SraB");
        ids.add("SraC_RyeA");
        ids.add("SraG");
        ids.add("Xoo1");
        ids.add("Xoo2");
        ids.add("Xoo5");
        ids.add("Xoo8");
        ids.add("Yfr1");
        ids.add("asX1");
        ids.add("asX2");
        ids.add("asX3");
        ids.add("asX4");
        ids.add("asX6");
        ids.add("asd");
        ids.add("b55");
        ids.add("bablM");
        ids.add("ceN115");
        ids.add("ceN23-1");
        ids.add("ceN56");
        ids.add("ceN72-3_ceN74-2");
        ids.add("ceN93");
        ids.add("ffh");
        ids.add("frnS");
        ids.add("isrA");
        ids.add("isrB");
        ids.add("isrC");
        ids.add("isrD");
        ids.add("isrF");
        ids.add("isrG");
        ids.add("isrH");
        ids.add("isrI");
        ids.add("isrJ");
        ids.add("isrK");
        ids.add("isrL");
        ids.add("isrN");
        ids.add("isrO");
        ids.add("isrP");
        ids.add("isrQ");
        ids.add("istR");
        ids.add("lactis-plasmid");
        ids.add("mascRNA-menRNA");
        ids.add("pntA");
        ids.add("rhtB");
        ids.add("rivX");
        ids.add("rli22");
        ids.add("rli24");
        ids.add("rli26");
        ids.add("rli27");
        ids.add("rli28");
        ids.add("rli31");
        ids.add("rli32");
        ids.add("rli33");
        ids.add("rli34");
        ids.add("rli36");
        ids.add("rli37");
        ids.add("rli38");
        ids.add("rli40");
        ids.add("rli41");
        ids.add("rli42");
        ids.add("rli43");
        ids.add("rli47");
        ids.add("rli48");
        ids.add("rli49");
        ids.add("rliA");
        ids.add("rliB");
        ids.add("rliD");
        ids.add("rliE");
        ids.add("rliF");
        ids.add("rliH");
        ids.add("rliI");
        ids.add("rpsB");
        ids.add("rseX");
        ids.add("rspL");
        ids.add("rydB");
        ids.add("ryfA");
        ids.add("sRNA-Xcc1");
        ids.add("sX11");
        ids.add("sX12");
        ids.add("sX13");
        ids.add("sX14");
        ids.add("sX15");
        ids.add("sX2");
        ids.add("sX4");
        ids.add("sX5");
        ids.add("sX6");
        ids.add("sX7");
        ids.add("sX8");
        ids.add("sX9");
        ids.add("sbcD");
        ids.add("sbrA");
        ids.add("sraA");
        ids.add("sraL");
        ids.add("sroB");
        ids.add("sroC");
        ids.add("sroD");
        ids.add("sroE");
        ids.add("sroH");
        ids.add("suhB");
        ids.add("t44");
        ids.add("tfoR");
        ids.add("tp2");
        ids.add("tpke11");
        ids.add("whalefall-1");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, tRNA", ids);
        ids.add("tRNA");
        ids.add("tRNA-Sec");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg, riboswitch", ids);
        ids.add("AdoCbl-variant");
        ids.add("AdoCbl_riboswitch");
        ids.add("Cobalamin");
        ids.add("FMN");
        ids.add("Glycine");
        ids.add("Lysine");
        ids.add("MFR");
        ids.add("MOCO_RNA_motif");
        ids.add("Mg_sensor");
        ids.add("PreQ1");
        ids.add("Purine");
        ids.add("SAH_riboswitch");
        ids.add("SAM");
        ids.add("SAM-I-IV-variant");
        ids.add("SAM-IV");
        ids.add("SAM-SAH");
        ids.add("SAM_V");
        ids.add("SAM_alpha");
        ids.add("SMK_box_riboswitch");
        ids.add("THF");
        ids.add("TPP");
        ids.add("c-di-GMP-II");
        ids.add("drz-agam-1");
        ids.add("drz-agam-2-2");
        ids.add("glmS");
        ids.add("preQ1-II");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, snRNA, snoRNA, HACA-box", ids);
        ids.add("ACA59");
        ids.add("ACA64");
        ids.add("DdR18");
        ids.add("SNORA1");
        ids.add("SNORA11");
        ids.add("SNORA12");
        ids.add("SNORA13");
        ids.add("SNORA14");
        ids.add("SNORA15");
        ids.add("SNORA16");
        ids.add("SNORA17");
        ids.add("SNORA18");
        ids.add("SNORA19");
        ids.add("SNORA2");
        ids.add("SNORA20");
        ids.add("SNORA21");
        ids.add("SNORA22");
        ids.add("SNORA23");
        ids.add("SNORA24");
        ids.add("SNORA25");
        ids.add("SNORA26");
        ids.add("SNORA27");
        ids.add("SNORA28");
        ids.add("SNORA29");
        ids.add("SNORA3");
        ids.add("SNORA30");
        ids.add("SNORA31");
        ids.add("SNORA32");
        ids.add("SNORA33");
        ids.add("SNORA35");
        ids.add("SNORA36");
        ids.add("SNORA38");
        ids.add("SNORA4");
        ids.add("SNORA40");
        ids.add("SNORA41");
        ids.add("SNORA42");
        ids.add("SNORA43");
        ids.add("SNORA44");
        ids.add("SNORA46");
        ids.add("SNORA47");
        ids.add("SNORA48");
        ids.add("SNORA49");
        ids.add("SNORA5");
        ids.add("SNORA50");
        ids.add("SNORA51");
        ids.add("SNORA52");
        ids.add("SNORA53");
        ids.add("SNORA54");
        ids.add("SNORA55");
        ids.add("SNORA56");
        ids.add("SNORA57");
        ids.add("SNORA58");
        ids.add("SNORA61");
        ids.add("SNORA62");
        ids.add("SNORA63");
        ids.add("SNORA64");
        ids.add("SNORA65");
        ids.add("SNORA66");
        ids.add("SNORA67");
        ids.add("SNORA68");
        ids.add("SNORA69");
        ids.add("SNORA7");
        ids.add("SNORA70");
        ids.add("SNORA71");
        ids.add("SNORA72");
        ids.add("SNORA73");
        ids.add("SNORA74");
        ids.add("SNORA75");
        ids.add("SNORA76");
        ids.add("SNORA77");
        ids.add("SNORA79");
        ids.add("SNORA8");
        ids.add("SNORA81");
        ids.add("SNORA84");
        ids.add("SNORA9");
        ids.add("SNORD71");
        ids.add("S_pombe_snR10");
        ids.add("S_pombe_snR100");
        ids.add("S_pombe_snR3");
        ids.add("S_pombe_snR33");
        ids.add("S_pombe_snR35");
        ids.add("S_pombe_snR36");
        ids.add("S_pombe_snR42");
        ids.add("S_pombe_snR46");
        ids.add("S_pombe_snR5");
        ids.add("S_pombe_snR90");
        ids.add("S_pombe_snR91");
        ids.add("S_pombe_snR92");
        ids.add("S_pombe_snR93");
        ids.add("S_pombe_snR94");
        ids.add("S_pombe_snR95");
        ids.add("S_pombe_snR96");
        ids.add("S_pombe_snR97");
        ids.add("S_pombe_snR98");
        ids.add("S_pombe_snR99");
        ids.add("TB10Cs1H1");
        ids.add("TB10Cs1H2");
        ids.add("TB10Cs1H3");
        ids.add("TB10Cs2H1");
        ids.add("TB10Cs2H2");
        ids.add("TB10Cs3H1");
        ids.add("TB10Cs3H2");
        ids.add("TB10Cs4H2");
        ids.add("TB10Cs4H3");
        ids.add("TB10Cs4H4");
        ids.add("TB10Cs5H2");
        ids.add("TB10Cs5H3");
        ids.add("TB11Cs2H1");
        ids.add("TB11Cs3H1");
        ids.add("TB11Cs4H1");
        ids.add("TB11Cs4H2");
        ids.add("TB11Cs4H3");
        ids.add("TB11Cs5H1");
        ids.add("TB11Cs5H2");
        ids.add("TB11Cs5H3");
        ids.add("TB3Cs2H1");
        ids.add("TB6Cs1H1");
        ids.add("TB6Cs1H3");
        ids.add("TB6Cs1H4");
        ids.add("TB8Cs2H1");
        ids.add("TB8Cs3H1");
        ids.add("TB8Cs4H2");
        ids.add("TB9Cs1H1");
        ids.add("TB9Cs1H2");
        ids.add("TB9Cs1H3");
        ids.add("TB9Cs2H1");
        ids.add("TB9Cs3H1");
        ids.add("TB9Cs3H2");
        ids.add("TB9Cs4H1");
        ids.add("TB9Cs4H2");
        ids.add("ceN100");
        ids.add("ceN101");
        ids.add("ceN102");
        ids.add("ceN104");
        ids.add("ceN105");
        ids.add("ceN110");
        ids.add("ceN125");
        ids.add("ceN126");
        ids.add("ceN36-1");
        ids.add("ceN38");
        ids.add("ceN39");
        ids.add("ceN41");
        ids.add("ceN42");
        ids.add("ceN43");
        ids.add("ceN45");
        ids.add("ceN46");
        ids.add("ceN48");
        ids.add("ceN49");
        ids.add("ceN51");
        ids.add("ceN58");
        ids.add("ceN59");
        ids.add("ceN67");
        ids.add("ceN68");
        ids.add("ceN80");
        ids.add("ceN81");
        ids.add("ceN82");
        ids.add("ceN84");
        ids.add("ceN92");
        ids.add("plasmodium_snoR11");
        ids.add("plasmodium_snoR27");
        ids.add("plasmodium_snoR31");
        ids.add("snR10");
        ids.add("snR11");
        ids.add("snR161");
        ids.add("snR189");
        ids.add("snR191");
        ids.add("snR3");
        ids.add("snR30");
        ids.add("snR31");
        ids.add("snR32");
        ids.add("snR33");
        ids.add("snR34");
        ids.add("snR35");
        ids.add("snR36");
        ids.add("snR37");
        ids.add("snR42");
        ids.add("snR43");
        ids.add("snR44");
        ids.add("snR46");
        ids.add("snR49");
        ids.add("snR5");
        ids.add("snR65");
        ids.add("snR8");
        ids.add("snR80");
        ids.add("snR81");
        ids.add("snR82");
        ids.add("snR83");
        ids.add("snR84");
        ids.add("snR85");
        ids.add("snR86");
        ids.add("snR9");
        ids.add("snoF1_F2");
        ids.add("snoM1");
        ids.add("snoR03");
        ids.add("snoR100");
        ids.add("snoR103");
        ids.add("snoR104");
        ids.add("snoR109");
        ids.add("snoR110");
        ids.add("snoR111");
        ids.add("snoR134");
        ids.add("snoR135");
        ids.add("snoR137");
        ids.add("snoR143");
        ids.add("snoR2");
        ids.add("snoR639");
        ids.add("snoR74");
        ids.add("snoR77");
        ids.add("snoR80");
        ids.add("snoR83");
        ids.add("snoR86");
        ids.add("snoR97");
        ids.add("snoR98");
        ids.add("snoR99");
        ids.add("snoU109");
        ids.add("snoU19");
        ids.add("snoU85");
        ids.add("snoU89");
        ids.add("snopsi18S-1377");
        ids.add("snopsi18S-1854");
        ids.add("snopsi18S-841");
        ids.add("snopsi28S-1192");
        ids.add("snopsi28S-2876");
        ids.add("snopsi28S-3316");
        ids.add("snopsi28S-3327");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, antitoxin", ids);
        ids.add("OrzO-P");
        ids.add("fstAT");
        ids.add("rdlD");
        ids.add("sok");
        ids.add("symR");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg, IRES", ids);
        ids.add("IRES_APC");
        ids.add("IRES_Aptho");
        ids.add("IRES_Bag1");
        ids.add("IRES_Bip");
        ids.add("IRES_Cripavirus");
        ids.add("IRES_Cx32");
        ids.add("IRES_Cx43");
        ids.add("IRES_EBNA");
        ids.add("IRES_FGF1");
        ids.add("IRES_FGF2");
        ids.add("IRES_HCV");
        ids.add("IRES_HIF1");
        ids.add("IRES_HepA");
        ids.add("IRES_Hsp70");
        ids.add("IRES_IGF2");
        ids.add("IRES_KSHV");
        ids.add("IRES_Kv1_4");
        ids.add("IRES_L-myc");
        ids.add("IRES_Pesti");
        ids.add("IRES_Picorna");
        ids.add("IRES_Tobamo");
        ids.add("IRES_TrkB");
        ids.add("IRES_VEGF_A");
        ids.add("IRES_c-myc");
        ids.add("IRES_c-sis");
        ids.add("IRES_mnt");
        ids.add("IRES_n-myc");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, rRNA", ids);
        ids.add("5S_rRNA");
        ids.add("5_8S_rRNA");
        ids.add("PK-G12rRNA");
        ids.add("SSU_rRNA_archaea");
        ids.add("SSU_rRNA_bacteria");
        ids.add("SSU_rRNA_eukarya");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, miRNA", ids);
        ids.add("MIR1023");
        ids.add("MIR1027");
        ids.add("MIR1122");
        ids.add("MIR1151");
        ids.add("MIR1222");
        ids.add("MIR1223");
        ids.add("MIR1428");
        ids.add("MIR1444");
        ids.add("MIR1446");
        ids.add("MIR158");
        ids.add("MIR159");
        ids.add("MIR162_2");
        ids.add("MIR164");
        ids.add("MIR167_1");
        ids.add("MIR168");
        ids.add("MIR169_2");
        ids.add("MIR169_5");
        ids.add("MIR171_1");
        ids.add("MIR171_2");
        ids.add("MIR1846");
        ids.add("MIR2118");
        ids.add("MIR2587");
        ids.add("MIR2907");
        ids.add("MIR390");
        ids.add("MIR394");
        ids.add("MIR396");
        ids.add("MIR397");
        ids.add("MIR398");
        ids.add("MIR403");
        ids.add("MIR405");
        ids.add("MIR408");
        ids.add("MIR439");
        ids.add("MIR444");
        ids.add("MIR473");
        ids.add("MIR474");
        ids.add("MIR475");
        ids.add("MIR476");
        ids.add("MIR477");
        ids.add("MIR478");
        ids.add("MIR480");
        ids.add("MIR529");
        ids.add("MIR530");
        ids.add("MIR535");
        ids.add("MIR807");
        ids.add("MIR811");
        ids.add("MIR815");
        ids.add("MIR820");
        ids.add("MIR821");
        ids.add("MIR824");
        ids.add("MIR828");
        ids.add("MIR845_1");
        ids.add("MIR845_2");
        ids.add("MIR854");
        ids.add("bantam");
        ids.add("ciona-mir-92");
        ids.add("hvt-mir-H");
        ids.add("let-7");
        ids.add("lin-4");
        ids.add("lsy-6");
        ids.add("miR-430");
        ids.add("mir-1");
        ids.add("mir-10");
        ids.add("mir-101");
        ids.add("mir-103");
        ids.add("mir-105");
        ids.add("mir-11");
        ids.add("mir-1178");
        ids.add("mir-1180");
        ids.add("mir-1183");
        ids.add("mir-12");
        ids.add("mir-1207");
        ids.add("mir-1208");
        ids.add("mir-122");
        ids.add("mir-1224");
        ids.add("mir-1225");
        ids.add("mir-1226");
        ids.add("mir-1227");
        ids.add("mir-1237");
        ids.add("mir-124");
        ids.add("mir-1249");
        ids.add("mir-1251");
        ids.add("mir-1253");
        ids.add("mir-1255");
        ids.add("mir-126");
        ids.add("mir-1265");
        ids.add("mir-127");
        ids.add("mir-1275");
        ids.add("mir-128");
        ids.add("mir-1280");
        ids.add("mir-1287");
        ids.add("mir-129");
        ids.add("mir-1296");
        ids.add("mir-130");
        ids.add("mir-1302");
        ids.add("mir-1306");
        ids.add("mir-1307");
        ids.add("mir-132");
        ids.add("mir-133");
        ids.add("mir-134");
        ids.add("mir-135");
        ids.add("mir-136");
        ids.add("mir-137");
        ids.add("mir-138");
        ids.add("mir-1388");
        ids.add("mir-139");
        ids.add("mir-14");
        ids.add("mir-140");
        ids.add("mir-1419");
        ids.add("mir-142");
        ids.add("mir-143");
        ids.add("mir-144");
        ids.add("mir-145");
        ids.add("mir-146");
        ids.add("mir-147");
        ids.add("mir-1473");
        ids.add("mir-148");
        ids.add("mir-149");
        ids.add("mir-1497");
        ids.add("mir-15");
        ids.add("mir-150");
        ids.add("mir-153");
        ids.add("mir-154");
        ids.add("mir-155");
        ids.add("mir-156");
        ids.add("mir-16");
        ids.add("mir-160");
        ids.add("mir-166");
        ids.add("mir-1662");
        ids.add("mir-17");
        ids.add("mir-172");
        ids.add("mir-1803");
        ids.add("mir-181");
        ids.add("mir-182");
        ids.add("mir-1827");
        ids.add("mir-1829");
        ids.add("mir-183");
        ids.add("mir-184");
        ids.add("mir-185");
        ids.add("mir-186");
        ids.add("mir-187");
        ids.add("mir-188");
        ids.add("mir-19");
        ids.add("mir-190");
        ids.add("mir-191");
        ids.add("mir-1912");
        ids.add("mir-192");
        ids.add("mir-193");
        ids.add("mir-1937");
        ids.add("mir-194");
        ids.add("mir-196");
        ids.add("mir-197");
        ids.add("mir-198");
        ids.add("mir-199");
        ids.add("mir-2");
        ids.add("mir-202");
        ids.add("mir-2024");
        ids.add("mir-203");
        ids.add("mir-204");
        ids.add("mir-205");
        ids.add("mir-207");
        ids.add("mir-208");
        ids.add("mir-21");
        ids.add("mir-210");
        ids.add("mir-214");
        ids.add("mir-216");
        ids.add("mir-217");
        ids.add("mir-218");
        ids.add("mir-219");
        ids.add("mir-22");
        ids.add("mir-221");
        ids.add("mir-223");
        ids.add("mir-2238");
        ids.add("mir-224");
        ids.add("mir-2241");
        ids.add("mir-228");
        ids.add("mir-23");
        ids.add("mir-230");
        ids.add("mir-231");
        ids.add("mir-232");
        ids.add("mir-233");
        ids.add("mir-234");
        ids.add("mir-235");
        ids.add("mir-239");
        ids.add("mir-24");
        ids.add("mir-240");
        ids.add("mir-241");
        ids.add("mir-242");
        ids.add("mir-244");
        ids.add("mir-245");
        ids.add("mir-246");
        ids.add("mir-248");
        ids.add("mir-249");
        ids.add("mir-25");
        ids.add("mir-250");
        ids.add("mir-251");
        ids.add("mir-2518");
        ids.add("mir-252");
        ids.add("mir-253");
        ids.add("mir-254");
        ids.add("mir-255");
        ids.add("mir-259");
        ids.add("mir-26");
        ids.add("mir-263");
        ids.add("mir-268");
        ids.add("mir-27");
        ids.add("mir-274");
        ids.add("mir-275");
        ids.add("mir-276");
        ids.add("mir-277");
        ids.add("mir-2774");
        ids.add("mir-2778");
        ids.add("mir-278");
        ids.add("mir-2780");
        ids.add("mir-279");
        ids.add("mir-28");
        ids.add("mir-280");
        ids.add("mir-2807");
        ids.add("mir-281");
        ids.add("mir-282");
        ids.add("mir-283");
        ids.add("mir-2833");
        ids.add("mir-284");
        ids.add("mir-286");
        ids.add("mir-287");
        ids.add("mir-288");
        ids.add("mir-289");
        ids.add("mir-29");
        ids.add("mir-290");
        ids.add("mir-296");
        ids.add("mir-2968");
        ids.add("mir-2970");
        ids.add("mir-2973");
        ids.add("mir-298");
        ids.add("mir-2985-2");
        ids.add("mir-299");
        ids.add("mir-3");
        ids.add("mir-30");
        ids.add("mir-301");
        ids.add("mir-3017");
        ids.add("mir-302");
        ids.add("mir-304");
        ids.add("mir-305");
        ids.add("mir-306");
        ids.add("mir-308");
        ids.add("mir-31");
        ids.add("mir-314");
        ids.add("mir-315");
        ids.add("mir-316");
        ids.add("mir-317");
        ids.add("mir-3179");
        ids.add("mir-318");
        ids.add("mir-3180");
        ids.add("mir-32");
        ids.add("mir-320");
        ids.add("mir-322");
        ids.add("mir-324");
        ids.add("mir-326");
        ids.add("mir-327");
        ids.add("mir-328");
        ids.add("mir-33");
        ids.add("mir-330");
        ids.add("mir-331");
        ids.add("mir-335");
        ids.add("mir-337");
        ids.add("mir-338");
        ids.add("mir-339");
        ids.add("mir-34");
        ids.add("mir-340");
        ids.add("mir-342");
        ids.add("mir-344");
        ids.add("mir-345");
        ids.add("mir-346");
        ids.add("mir-35");
        ids.add("mir-350");
        ids.add("mir-351");
        ids.add("mir-353");
        ids.add("mir-354");
        ids.add("mir-355");
        ids.add("mir-357");
        ids.add("mir-358");
        ids.add("mir-359");
        ids.add("mir-36");
        ids.add("mir-360");
        ids.add("mir-361");
        ids.add("mir-363");
        ids.add("mir-365");
        ids.add("mir-367");
        ids.add("mir-370");
        ids.add("mir-374");
        ids.add("mir-375");
        ids.add("mir-378");
        ids.add("mir-383");
        ids.add("mir-384");
        ids.add("mir-392");
        ids.add("mir-395");
        ids.add("mir-399");
        ids.add("mir-412");
        ids.add("mir-42");
        ids.add("mir-422");
        ids.add("mir-423");
        ids.add("mir-425");
        ids.add("mir-43");
        ids.add("mir-431");
        ids.add("mir-432");
        ids.add("mir-433");
        ids.add("mir-434");
        ids.add("mir-44");
        ids.add("mir-448");
        ids.add("mir-449");
        ids.add("mir-450");
        ids.add("mir-451");
        ids.add("mir-452");
        ids.add("mir-454");
        ids.add("mir-455");
        ids.add("mir-456");
        ids.add("mir-458");
        ids.add("mir-46");
        ids.add("mir-460");
        ids.add("mir-463");
        ids.add("mir-471");
        ids.add("mir-48");
        ids.add("mir-484");
        ids.add("mir-486");
        ids.add("mir-488");
        ids.add("mir-489");
        ids.add("mir-49");
        ids.add("mir-490");
        ids.add("mir-491");
        ids.add("mir-492");
        ids.add("mir-497");
        ids.add("mir-498");
        ids.add("mir-499");
        ids.add("mir-5");
        ids.add("mir-50");
        ids.add("mir-500");
        ids.add("mir-503");
        ids.add("mir-504");
        ids.add("mir-505");
        ids.add("mir-506");
        ids.add("mir-515");
        ids.add("mir-52");
        ids.add("mir-540");
        ids.add("mir-541");
        ids.add("mir-542");
        ids.add("mir-544");
        ids.add("mir-548");
        ids.add("mir-549");
        ids.add("mir-55");
        ids.add("mir-550");
        ids.add("mir-551");
        ids.add("mir-552");
        ids.add("mir-553");
        ids.add("mir-554");
        ids.add("mir-556");
        ids.add("mir-557");
        ids.add("mir-558");
        ids.add("mir-56");
        ids.add("mir-562");
        ids.add("mir-563");
        ids.add("mir-567");
        ids.add("mir-569");
        ids.add("mir-572");
        ids.add("mir-573");
        ids.add("mir-574");
        ids.add("mir-575");
        ids.add("mir-576");
        ids.add("mir-577");
        ids.add("mir-578");
        ids.add("mir-58");
        ids.add("mir-580");
        ids.add("mir-581");
        ids.add("mir-582");
        ids.add("mir-583");
        ids.add("mir-584");
        ids.add("mir-586");
        ids.add("mir-589");
        ids.add("mir-590");
        ids.add("mir-592");
        ids.add("mir-593");
        ids.add("mir-597");
        ids.add("mir-598");
        ids.add("mir-599");
        ids.add("mir-6");
        ids.add("mir-60");
        ids.add("mir-600");
        ids.add("mir-601");
        ids.add("mir-604");
        ids.add("mir-605");
        ids.add("mir-607");
        ids.add("mir-609");
        ids.add("mir-61");
        ids.add("mir-611");
        ids.add("mir-612");
        ids.add("mir-615");
        ids.add("mir-616");
        ids.add("mir-618");
        ids.add("mir-62");
        ids.add("mir-621");
        ids.add("mir-624");
        ids.add("mir-625");
        ids.add("mir-626");
        ids.add("mir-628");
        ids.add("mir-63");
        ids.add("mir-631");
        ids.add("mir-632");
        ids.add("mir-633");
        ids.add("mir-636");
        ids.add("mir-638");
        ids.add("mir-639");
        ids.add("mir-64");
        ids.add("mir-640");
        ids.add("mir-642");
        ids.add("mir-643");
        ids.add("mir-644");
        ids.add("mir-648");
        ids.add("mir-649");
        ids.add("mir-650");
        ids.add("mir-651");
        ids.add("mir-652");
        ids.add("mir-653");
        ids.add("mir-654");
        ids.add("mir-657");
        ids.add("mir-661");
        ids.add("mir-662");
        ids.add("mir-663");
        ids.add("mir-665");
        ids.add("mir-668");
        ids.add("mir-67");
        ids.add("mir-671");
        ids.add("mir-672");
        ids.add("mir-673");
        ids.add("mir-674");
        ids.add("mir-675");
        ids.add("mir-676");
        ids.add("mir-684");
        ids.add("mir-689");
        ids.add("mir-692");
        ids.add("mir-7");
        ids.add("mir-70");
        ids.add("mir-708");
        ids.add("mir-71");
        ids.add("mir-711");
        ids.add("mir-720");
        ids.add("mir-73");
        ids.add("mir-74");
        ids.add("mir-744");
        ids.add("mir-75");
        ids.add("mir-760");
        ids.add("mir-761");
        ids.add("mir-764");
        ids.add("mir-765");
        ids.add("mir-767");
        ids.add("mir-77");
        ids.add("mir-770");
        ids.add("mir-785");
        ids.add("mir-786");
        ids.add("mir-787");
        ids.add("mir-788");
        ids.add("mir-789");
        ids.add("mir-790");
        ids.add("mir-791");
        ids.add("mir-8");
        ids.add("mir-80");
        ids.add("mir-802");
        ids.add("mir-81");
        ids.add("mir-83");
        ids.add("mir-84");
        ids.add("mir-85");
        ids.add("mir-86");
        ids.add("mir-87");
        ids.add("mir-872");
        ids.add("mir-873");
        ids.add("mir-874");
        ids.add("mir-875");
        ids.add("mir-876");
        ids.add("mir-877");
        ids.add("mir-879");
        ids.add("mir-883");
        ids.add("mir-885");
        ids.add("mir-887");
        ids.add("mir-891");
        ids.add("mir-9");
        ids.add("mir-90");
        ids.add("mir-92");
        ids.add("mir-920");
        ids.add("mir-922");
        ids.add("mir-924");
        ids.add("mir-927");
        ids.add("mir-929");
        ids.add("mir-932");
        ids.add("mir-934");
        ids.add("mir-936");
        ids.add("mir-937");
        ids.add("mir-938");
        ids.add("mir-939");
        ids.add("mir-940");
        ids.add("mir-941");
        ids.add("mir-942");
        ids.add("mir-944");
        ids.add("mir-96");
        ids.add("mir-969");
        ids.add("mir-981");
        ids.add("mir-983");
        ids.add("mir-987");
        ids.add("mir-988");
        ids.add("mir-995");
        ids.add("mir-996");
        ids.add("mir-999");
        ids.add("mir-BART1");
        ids.add("mir-BART12");
        ids.add("mir-BART15");
        ids.add("mir-BART17");
        ids.add("mir-BART2");
        ids.add("mir-BART20");
        ids.add("mir-BART3");
        ids.add("mir-BART5");
        ids.add("mir-BART7");
        ids.add("mir-BHRF1-1");
        ids.add("mir-BHRF1-2");
        ids.add("mir-BHRF1-3");
        ids.add("mir-M7");
        ids.add("mir-TAR");
        ids.add("mir-iab-4");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, CRISPR", ids);
        ids.add("CRISPR-DR10");
        ids.add("CRISPR-DR11");
        ids.add("CRISPR-DR12");
        ids.add("CRISPR-DR13");
        ids.add("CRISPR-DR14");
        ids.add("CRISPR-DR15");
        ids.add("CRISPR-DR16");
        ids.add("CRISPR-DR17");
        ids.add("CRISPR-DR18");
        ids.add("CRISPR-DR19");
        ids.add("CRISPR-DR2");
        ids.add("CRISPR-DR20");
        ids.add("CRISPR-DR21");
        ids.add("CRISPR-DR22");
        ids.add("CRISPR-DR23");
        ids.add("CRISPR-DR24");
        ids.add("CRISPR-DR25");
        ids.add("CRISPR-DR26");
        ids.add("CRISPR-DR27");
        ids.add("CRISPR-DR28");
        ids.add("CRISPR-DR29");
        ids.add("CRISPR-DR3");
        ids.add("CRISPR-DR30");
        ids.add("CRISPR-DR31");
        ids.add("CRISPR-DR32");
        ids.add("CRISPR-DR33");
        ids.add("CRISPR-DR34");
        ids.add("CRISPR-DR35");
        ids.add("CRISPR-DR36");
        ids.add("CRISPR-DR37");
        ids.add("CRISPR-DR38");
        ids.add("CRISPR-DR39");
        ids.add("CRISPR-DR4");
        ids.add("CRISPR-DR40");
        ids.add("CRISPR-DR41");
        ids.add("CRISPR-DR42");
        ids.add("CRISPR-DR43");
        ids.add("CRISPR-DR44");
        ids.add("CRISPR-DR45");
        ids.add("CRISPR-DR46");
        ids.add("CRISPR-DR47");
        ids.add("CRISPR-DR48");
        ids.add("CRISPR-DR49");
        ids.add("CRISPR-DR5");
        ids.add("CRISPR-DR50");
        ids.add("CRISPR-DR51");
        ids.add("CRISPR-DR52");
        ids.add("CRISPR-DR53");
        ids.add("CRISPR-DR54");
        ids.add("CRISPR-DR55");
        ids.add("CRISPR-DR56");
        ids.add("CRISPR-DR57");
        ids.add("CRISPR-DR58");
        ids.add("CRISPR-DR59");
        ids.add("CRISPR-DR6");
        ids.add("CRISPR-DR60");
        ids.add("CRISPR-DR61");
        ids.add("CRISPR-DR62");
        ids.add("CRISPR-DR63");
        ids.add("CRISPR-DR64");
        ids.add("CRISPR-DR65");
        ids.add("CRISPR-DR66");
        ids.add("CRISPR-DR7");
        ids.add("CRISPR-DR8");
        ids.add("CRISPR-DR9");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, snRNA, splicing", ids);
        ids.add("SmY");
        ids.add("U1");
        ids.add("U11");
        ids.add("U12");
        ids.add("U1_yeast");
        ids.add("U2");
        ids.add("U4");
        ids.add("U4atac");
        ids.add("U5");
        ids.add("U6");
        ids.add("U6atac");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg, frameshift_element", ids);
        ids.add("Antizyme_FSE");
        ids.add("Corona_FSE");
        ids.add("DnaX");
        ids.add("HIV_FE");
        ids.add("HIV_FS2");
        ids.add("IS1222_FSE");
        ids.add("RF_site1");
        ids.add("RF_site2");
        ids.add("RF_site3");
        ids.add("RF_site4");
        ids.add("RF_site5");
        ids.add("RF_site6");
        ids.add("RF_site8");
        ids.add("RF_site9");
        ids.add("astro_FSE");
        ids.add("blv_FSE");
        ids.add("eeev_FSE");
        ids.add("eiav_FSE");
        ids.add("fiv_FSE");
        ids.add("flavi_FSE");
        ids.add("htlv_FSE");
        ids.add("mycoplasma_FSE");
        ids.add("neisseria_FSE");
        ids.add("ovine_lenti_FSE");
        ids.add("sobemo_FSE");
        ids.add("toga_FSE");
        ids.add("veev_FSE");
        ids.add("weev_FSE");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg, leader", ids);
        ids.add("His_leader");
        ids.add("L10_leader");
        ids.add("L13_leader");
        ids.add("L19_leader");
        ids.add("L20_leader");
        ids.add("L21_leader");
        ids.add("Leu_leader");
        ids.add("Phe_leader");
        ids.add("S15");
        ids.add("Thr_leader");
        ids.add("Trp_leader");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, lncRNA", ids);
        ids.add("BC040587");
        ids.add("CDKN2B-AS");
        ids.add("CDKN2B-AS_2");
        ids.add("CDKN2B-AS_3");
        ids.add("CLRN1-AS1");
        ids.add("DAOA-AS1_1");
        ids.add("DAOA-AS1_2");
        ids.add("DGCR5");
        ids.add("DISC2");
        ids.add("DLEU1_1");
        ids.add("DLEU1_2");
        ids.add("DLEU2_1");
        ids.add("DLEU2_2");
        ids.add("DLEU2_3");
        ids.add("DLEU2_4");
        ids.add("DLEU2_5");
        ids.add("DLEU2_6");
        ids.add("DLG2-AS1_1");
        ids.add("DLG2-AS1_2");
        ids.add("EGOT");
        ids.add("Evf1_1");
        ids.add("Evf1_2");
        ids.add("FAM13A-AS1_1");
        ids.add("FAM13A-AS1_2");
        ids.add("FAS-AS1");
        ids.add("FMR1-AS1_1");
        ids.add("FMR1-AS1_2");
        ids.add("FTX_1");
        ids.add("FTX_2");
        ids.add("FTX_3");
        ids.add("FTX_4");
        ids.add("FTX_5");
        ids.add("GHRLOS");
        ids.add("GNAS-AS1_1");
        ids.add("GNAS-AS1_2");
        ids.add("GNAS-AS1_3");
        ids.add("GNAS-AS1_4");
        ids.add("GNAS-AS1_5");
        ids.add("H19_1");
        ids.add("H19_2");
        ids.add("H19_3");
        ids.add("HAR1A");
        ids.add("HOTAIRM1_1");
        ids.add("HOTAIRM1_2");
        ids.add("HOTAIRM1_3");
        ids.add("HOTAIRM1_4");
        ids.add("HOTAIRM1_5");
        ids.add("HOTAIR_1");
        ids.add("HOTAIR_2");
        ids.add("HOTAIR_3");
        ids.add("HOTAIR_4");
        ids.add("HOTAIR_5");
        ids.add("HOTTIP_1");
        ids.add("HOTTIP_2");
        ids.add("HOTTIP_3");
        ids.add("HOTTIP_4");
        ids.add("HOXA11-AS1_1");
        ids.add("HOXA11-AS1_2");
        ids.add("HOXA11-AS1_3");
        ids.add("HOXA11-AS1_4");
        ids.add("HOXA11-AS1_5");
        ids.add("HOXA11-AS1_6");
        ids.add("HOXB13-AS1_1");
        ids.add("HOXB13-AS1_2");
        ids.add("HSR-omega_1");
        ids.add("HSR-omega_2");
        ids.add("HTT-AS1_1");
        ids.add("HTT-AS1_2");
        ids.add("HTT-AS1_3");
        ids.add("HULC");
        ids.add("HYMAI");
        ids.add("JPX_1");
        ids.add("JPX_2");
        ids.add("KCNQ1DN");
        ids.add("KCNQ1OT1_1");
        ids.add("KCNQ1OT1_2");
        ids.add("KCNQ1OT1_3");
        ids.add("KCNQ1OT1_5");
        ids.add("LOC285194");
        ids.add("MALAT1");
        ids.add("MEG3_1");
        ids.add("MEG3_2");
        ids.add("MEG8_1");
        ids.add("MEG8_2");
        ids.add("MEG8_3");
        ids.add("MESTIT1_1");
        ids.add("MESTIT1_2");
        ids.add("MESTIT1_3");
        ids.add("MIAT_exon1");
        ids.add("MIAT_exon5_1");
        ids.add("MIAT_exon5_2");
        ids.add("MIAT_exon5_3");
        ids.add("MIMT1_2");
        ids.add("Mico1");
        ids.add("NAMA_1");
        ids.add("NAMA_2");
        ids.add("NBR2");
        ids.add("NCRUPAR_1");
        ids.add("NCRUPAR_2");
        ids.add("NEAT1_1");
        ids.add("NEAT1_2");
        ids.add("NEAT1_3");
        ids.add("NPPA-AS1_1");
        ids.add("NPPA-AS1_2");
        ids.add("NPPA-AS1_3");
        ids.add("Nkx2-2as");
        ids.add("PART1_1");
        ids.add("PART1_2");
        ids.add("PART1_3");
        ids.add("PCA3_1");
        ids.add("PCA3_2");
        ids.add("PCGEM1");
        ids.add("PISRT1");
        ids.add("PRINS");
        ids.add("PVT1_1");
        ids.add("PVT1_2");
        ids.add("PVT1_3");
        ids.add("PVT1_4");
        ids.add("PVT1_5");
        ids.add("PVT1_6");
        ids.add("PVT1_7");
        ids.add("Pinc");
        ids.add("RFPL3-AS1_1");
        ids.add("RFPL3-AS1_2");
        ids.add("RMST_1");
        ids.add("RMST_10");
        ids.add("RMST_2");
        ids.add("RMST_3");
        ids.add("RMST_4");
        ids.add("RMST_5");
        ids.add("RMST_6");
        ids.add("RMST_7");
        ids.add("RMST_8");
        ids.add("RMST_9");
        ids.add("SMAD5-AS1_1");
        ids.add("SMAD5-AS1_2");
        ids.add("SMAD5-AS1_3");
        ids.add("SMAD5-AS1_4");
        ids.add("SMCR2_1");
        ids.add("SMCR2_2");
        ids.add("SOX2OT_exon1");
        ids.add("SOX2OT_exon2");
        ids.add("SOX2OT_exon3");
        ids.add("SOX2OT_exon4");
        ids.add("SPRY4-IT1_1");
        ids.add("SPRY4-IT1_2");
        ids.add("ST7-AS1_1");
        ids.add("ST7-AS1_2");
        ids.add("ST7-AS2_1");
        ids.add("ST7-AS2_2");
        ids.add("ST7-OT3_1");
        ids.add("ST7-OT3_2");
        ids.add("ST7-OT3_3");
        ids.add("ST7-OT3_4");
        ids.add("ST7-OT4_1");
        ids.add("ST7-OT4_2");
        ids.add("ST7-OT4_3");
        ids.add("ST7-OT4_4");
        ids.add("Six3os1_1");
        ids.add("Six3os1_2");
        ids.add("Six3os1_3");
        ids.add("Six3os1_4");
        ids.add("Six3os1_5");
        ids.add("Six3os1_6");
        ids.add("Six3os1_7");
        ids.add("Sphinx_1");
        ids.add("Sphinx_2");
        ids.add("TCL6_1");
        ids.add("TCL6_2");
        ids.add("TCL6_3");
        ids.add("TP53TG1_1");
        ids.add("TP53TG1_2");
        ids.add("TP73-AS1");
        ids.add("TTC28-AS1_1");
        ids.add("TTC28-AS1_2");
        ids.add("TTC28-AS1_3");
        ids.add("TTC28-AS1_4");
        ids.add("TUG1_1");
        ids.add("TUG1_2");
        ids.add("TUG1_3");
        ids.add("TUG1_4");
        ids.add("UCA1");
        ids.add("VIS1");
        ids.add("Vax2os1_1");
        ids.add("Vax2os1_2");
        ids.add("Vax2os1_3");
        ids.add("WT1-AS_1");
        ids.add("WT1-AS_2");
        ids.add("WT1-AS_3");
        ids.add("WT1-AS_4");
        ids.add("WT1-AS_5");
        ids.add("WT1-AS_6");
        ids.add("WT1-AS_7");
        ids.add("WT1-AS_8");
        ids.add("Xist_exon1");
        ids.add("Xist_exon4");
        ids.add("Yar_1");
        ids.add("Yar_2");
        ids.add("Yar_3");
        ids.add("ZEB2_AS1_1");
        ids.add("ZEB2_AS1_2");
        ids.add("ZEB2_AS1_3");
        ids.add("ZEB2_AS1_4");
        ids.add("ZFAT-AS1_1");
        ids.add("ZFAT-AS1_2");
        ids.add("ZFAT-AS1_3");
        ids.add("ZNFX1-AS1_1");
        ids.add("ZNFX1-AS1_2");
        ids.add("ZNFX1-AS1_3");
        ids.add("ZNRD1-AS1_1");
        ids.add("ZNRD1-AS1_2");
        ids.add("ZNRD1-AS1_3");
        ids.add("adapt33_1");
        ids.add("adapt33_2");
        ids.add("adapt33_3");
        ids.add("adapt33_4");
        ids.add("bxd_1");
        ids.add("bxd_2");
        ids.add("bxd_3");
        ids.add("bxd_4");
        ids.add("bxd_5");
        ids.add("bxd_6");
        ids.add("bxd_7");
        ids.add("lincRNA-p21_1");
        ids.add("lincRNA-p21_2");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, snRNA, snoRNA, scaRNA", ids);
        ids.add("SCARNA1");
        ids.add("SCARNA11");
        ids.add("SCARNA13");
        ids.add("SCARNA14");
        ids.add("SCARNA15");
        ids.add("SCARNA16");
        ids.add("SCARNA17");
        ids.add("SCARNA18");
        ids.add("SCARNA2");
        ids.add("SCARNA20");
        ids.add("SCARNA21");
        ids.add("SCARNA23");
        ids.add("SCARNA24");
        ids.add("SCARNA3");
        ids.add("SCARNA4");
        ids.add("SCARNA6");
        ids.add("SCARNA7");
        ids.add("SCARNA8");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, ribozyme", ids);
        ids.add("CPEB3_ribozyme");
        ids.add("CoTC_ribozyme");
        ids.add("HDV_ribozyme");
        ids.add("Hairpin");
        ids.add("Hammerhead_1");
        ids.add("Hammerhead_3");
        ids.add("Hammerhead_HH10");
        ids.add("Hammerhead_II");
        ids.add("RNaseP_arch");
        ids.add("RNaseP_bact_a");
        ids.add("RNaseP_bact_b");
        ids.add("RNaseP_nuc");
        ids.add("RNase_MRP");
        ids.add("RNase_P");
        ids.add("Vg1_ribozyme");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene", ids);
        ids.add("6S");
        ids.add("7SK");
        ids.add("AS1726");
        ids.add("AS1890");
        ids.add("ATPC");
        ids.add("AniS");
        ids.add("Archaea_SRP");
        ids.add("Arthropod_7SK");
        ids.add("Bacteria_large_SRP");
        ids.add("Bacteria_small_SRP");
        ids.add("Betaproteobacteria_toxic_sRNA");
        ids.add("Deinococcus_Y_RNA");
        ids.add("Evf-2_5p");
        ids.add("FinP");
        ids.add("FsrA");
        ids.add("Fungi_SRP");
        ids.add("GOLLD");
        ids.add("GRIK4_3p_UTR");
        ids.add("GcvB");
        ids.add("HEARO");
        ids.add("Hammerhead_HH9");
        ids.add("HgcC");
        ids.add("HgcE");
        ids.add("HgcF");
        ids.add("HgcG");
        ids.add("IMES-1");
        ids.add("IMES-2");
        ids.add("IMES-3");
        ids.add("IMES-4");
        ids.add("IS009");
        ids.add("InvR");
        ids.add("Metazoa_SRP");
        ids.add("NRON");
        ids.add("OLE");
        ids.add("P1");
        ids.add("P11");
        ids.add("P15");
        ids.add("P16");
        ids.add("P24");
        ids.add("P26");
        ids.add("P9");
        ids.add("Phage_pRNA");
        ids.add("Plant_SRP");
        ids.add("Plasmid_RNAIII");
        ids.add("Protozoa_SRP");
        ids.add("RNAIII");
        ids.add("RUF20");
        ids.add("RUF21");
        ids.add("SL1");
        ids.add("SL2");
        ids.add("STnc100");
        ids.add("STnc130");
        ids.add("STnc30");
        ids.add("STnc400");
        ids.add("STnc480");
        ids.add("STnc510");
        ids.add("STnc70");
        ids.add("Sacc_telomerase");
        ids.add("SscA");
        ids.add("StyR-44");
        ids.add("Telomerase-cil");
        ids.add("Telomerase-vert");
        ids.add("VA");
        ids.add("Vault");
        ids.add("XIST");
        ids.add("XIST_intron");
        ids.add("Y_RNA");
        ids.add("alpha_tmRNA");
        ids.add("beta_tmRNA");
        ids.add("class_I_RNA");
        ids.add("cyano_tmRNA");
        ids.add("enod40");
        ids.add("g2");
        ids.add("gadd7");
        ids.add("greA");
        ids.add("msr");
        ids.add("nse_sRNA");
        ids.add("pRNA");
        ids.add("rimP");
        ids.add("rnk_leader");
        ids.add("rnk_pseudo");
        ids.add("rpsL_psuedo");
        ids.add("rpsL_ricks");
        ids.add("rsmX");
        ids.add("srg1");
        ids.add("tmRNA");
        ids.add("uc_338");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, snRNA", ids);
        ids.add("Dictyostelium_SRP");
        ids.add("HSUR");
        ids.add("U7");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Cis-reg, thermoregulator", ids);
        ids.add("FourU");
        ids.add("Hsp90_CRE");
        ids.add("Lambda_thermo");
        ids.add("PrfA");
        ids.add("ROSE");
        ids.add("ROSE_2");
        ids.add("cspA");
        ids = new ArrayList<String>();
        ncRNA_ids.put("Gene, antisense", ids);
        ids.add("Anti-Q_RNA");
        ids.add("C4");
        ids.add("CopA");
        ids.add("DicF");
        ids.add("HPnc0260");
        ids.add("IsrR");
        ids.add("MicF");
        ids.add("NrrF");
        ids.add("Plasmid_R1162");
        ids.add("PtaRNA1");
        ids.add("RNA-OUT");
        ids.add("RNAI");
        ids.add("RatA");
        ids.add("SgrS");
        ids.add("VrrA");
        ids.add("ctRNA_p42d");
        ids.add("ctRNA_pGA1");
        ids.add("ctRNA_pND324");
        ids.add("ctRNA_pT181");
        ids.add("mtDNA_ssA");
        ids.add("rli23");
        ids.add("rli45");
        ids.add("sar");
        ids = new ArrayList<String>();
        ncRNA_ids.put("RNA 2D/3D motif", ids);
        ids.add("3-way junction");
        ids.add("4-way junction");
        ids.add("Loop E");
        ids.add("Kink-turn");
        ids.add("Tetraloop");
    }


    public static Map<String, Color> genomic_features_classes_to_colors = new HashMap<String, Color>();

    static {
        //ncRNA related
        genomic_features_classes_to_colors.put("ncRNA", new Color(251, 78, 209));
        for (String ncRNA_class:ncRNA_classes)
            genomic_features_classes_to_colors.put(ncRNA_class, new Color(251, 78, 209));
        genomic_features_classes_to_colors.put("tRNA", new Color(251, 78, 209));


        //gene related
        genomic_features_classes_to_colors.put("gene", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("promoter", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("TATA_signal", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("CAAT_signal", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("GC_signal", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("-10_signal", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("-35_signal", new Color(39, 74, 116));
        genomic_features_classes_to_colors.put("primer_bind", new Color(39, 74, 116));

        //mRNA related
        genomic_features_classes_to_colors.put("prim_transcript", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("precursor_RNA", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("mRNA", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("intron", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("exon", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("5'UTR", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("3'UTR", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("polyA_signal", new Color(133, 163, 63));
        genomic_features_classes_to_colors.put("polyA_site", new Color(133, 163, 63));

        //protein related
        genomic_features_classes_to_colors.put("CDS", new Color(163, 9, 18));
        genomic_features_classes_to_colors.put("mat_peptide", new Color(163, 9, 18));
        genomic_features_classes_to_colors.put("protein_bind", new Color(163, 9, 18));
        genomic_features_classes_to_colors.put("RBS", new Color(163, 9, 18));
        genomic_features_classes_to_colors.put("transit_peptide", new Color(163, 9, 18));
        genomic_features_classes_to_colors.put("sig_peptide", new Color(163, 9, 18));

        //chromosome structure related
        genomic_features_classes_to_colors.put("centromere", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("telomere", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("LTR", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("operon", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("mobile_element", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("oriT", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("rep_origin", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("modified_base", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("repeat_region", new Color(139, 71, 231));
        genomic_features_classes_to_colors.put("STS", new Color(139, 71, 231));

        //expression regulation related
        genomic_features_classes_to_colors.put("attenuator", new Color(82, 175, 255));
        genomic_features_classes_to_colors.put("enhancer", new Color(82, 175, 255));
        genomic_features_classes_to_colors.put("terminator", new Color(82, 175, 255));

        //misc stuff
        genomic_features_classes_to_colors.put("misc_binding", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_difference", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_feature", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_recomb", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_RNA", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_signal", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("misc_structure", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("unsure", new Color(121, 137, 139));
        genomic_features_classes_to_colors.put("variation", new Color(121, 137, 139));

    }

    public static Color getColorForGenomicFeature(String genomicFeature) {
        Color c = genomic_features_classes_to_colors.get(genomicFeature);
        return c == null ? Color.BLACK : c;
    }



}
