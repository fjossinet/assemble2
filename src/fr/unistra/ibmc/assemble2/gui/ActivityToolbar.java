package fr.unistra.ibmc.assemble2.gui;

import com.sun.javafx.application.PlatformImpl;
import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.components.MessagingSystemAction;
import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import fr.unistra.ibmc.assemble2.utils.SvgPath;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;

public class ActivityToolbar implements ToolBar {

    private Shape activity,
            help;
    private boolean overHelp;
    private Mediator mediator;
    private Timer activityTimer;
    private double step = 0;
    private SwingWorker lastWorker, currentWorker;
    private ComputingServer computingServer;
    private static GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");


    public ActivityToolbar(final Mediator mediator) {
        this.mediator = mediator;
        try {
            this.activity = new SvgPath("M31.229,17.736c0.064-0.571,0.104-1.148,0.104-1.736s-0.04-1.166-0.104-1.737l-4.377-1.557c-0.218-0.716-0.504-1.401-0.851-2.05l1.993-4.192c-0.725-0.91-1.549-1.734-2.458-2.459l-4.193,1.994c-0.647-0.347-1.334-0.632-2.049-0.849l-1.558-4.378C17.165,0.708,16.588,0.667,16,0.667s-1.166,0.041-1.737,0.105L12.707,5.15c-0.716,0.217-1.401,0.502-2.05,0.849L6.464,4.005C5.554,4.73,4.73,5.554,4.005,6.464l1.994,4.192c-0.347,0.648-0.632,1.334-0.849,2.05l-4.378,1.557C0.708,14.834,0.667,15.412,0.667,16s0.041,1.165,0.105,1.736l4.378,1.558c0.217,0.715,0.502,1.401,0.849,2.049l-1.994,4.193c0.725,0.909,1.549,1.733,2.459,2.458l4.192-1.993c0.648,0.347,1.334,0.633,2.05,0.851l1.557,4.377c0.571,0.064,1.148,0.104,1.737,0.104c0.588,0,1.165-0.04,1.736-0.104l1.558-4.377c0.715-0.218,1.399-0.504,2.049-0.851l4.193,1.993c0.909-0.725,1.733-1.549,2.458-2.458l-1.993-4.193c0.347-0.647,0.633-1.334,0.851-2.049L31.229,17.736zM16,20.871c-2.69,0-4.872-2.182-4.872-4.871c0-2.69,2.182-4.872,4.872-4.872c2.689,0,4.871,2.182,4.871,4.872C20.871,18.689,18.689,20.871,16,20.871z").getShape();
            this.help = new SvgPath("M16,1.466C7.973,1.466,1.466,7.973,1.466,16c0,8.027,6.507,14.534,14.534,14.534c8.027,0,14.534-6.507,14.534-14.534C30.534,7.973,24.027,1.466,16,1.466z M17.328,24.371h-2.707v-2.596h2.707V24.371zM17.328,19.003v0.858h-2.707v-1.057c0-3.19,3.63-3.696,3.63-5.963c0-1.034-0.924-1.826-2.134-1.826c-1.254,0-2.354,0.924-2.354,0.924l-1.541-1.915c0,0,1.519-1.584,4.137-1.584c2.487,0,4.796,1.54,4.796,4.136C21.156,16.208,17.328,16.627,17.328,19.003z").getShape();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.activityTimer = new Timer(500,new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Rectangle2D buttonShape = activity.getBounds2D();
                Rectangle rec = new Rectangle(mediator.getSecondaryCanvas().getWidth()-80+(int)help.getBounds2D().getWidth()+5, mediator.getSecondaryCanvas().getHeight()-40, (int)buttonShape.getWidth()+10, (int)buttonShape.getHeight()+10);
                step ++;
                mediator.getSecondaryCanvas().repaint(rec);
            }
        });

    }

    public void startActivity(SwingWorker worker) {
        this.currentWorker = worker;
        this.activityTimer.start();
    }

    public void stopActivity() {
        this.activityTimer.stop();
        this.step = 0;
        this.currentWorker = null;
    }

    public void draw(Graphics2D g2, int startX, int startY) {
        if (this.help != null) {
            Rectangle2D buttonShape = this.help.getBounds2D();
            g2.translate(startX, startY);
            if (Assemble.HELP_MODE) {
                if (overHelp) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(10));
                    g2.setColor(iconHighlight);
                    g2.draw(this.help);
                    g2.setStroke(s);
                }
                g2.setColor(new Color(163, 9, 18));
            }
            else  {
                if (overHelp) {
                    Stroke s = g2.getStroke();
                    g2.setStroke(new BasicStroke(5));
                    g2.setColor(iconHighlight);
                    g2.draw(this.help);
                    g2.setStroke(s);
                }
                g2.setColor(Color.BLACK);
            }
            g2.fill(this.help);
            g2.translate(-startX, -startY);
            startX += buttonShape.getWidth()+5;
        }

        if (this.activity != null && this.step%2 == 0) {
            Rectangle2D buttonShape = this.activity.getBounds2D();
            g2.translate(startX, startY);
            g2.setColor(Color.BLACK);
            g2.fill(this.activity);
            g2.translate(-startX, -startY);
            startX += buttonShape.getWidth()+5;
        }

    }

    public void mouseClicked(MouseEvent e, int startX, int startY) {
        if (this.help != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.help.getBounds2D().getMinX()+startX, this.help.getBounds2D().getBounds2D().getMinY()+startY, this.help.getBounds2D().getWidth(), this.help.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                mediator.getSecondaryCanvas().eraseHelpDialog();
                AssembleConfig.showHelpToolTip(false);
                Assemble.HELP_MODE = !Assemble.HELP_MODE;
                if (Assemble.HELP_MODE) {
                    //we need to stop the tutorials, incompatible
                    for (int i= 0 ; i < ((JMenu)mediator.getAssemble().getTutorialsMenu()).getItemCount() ; i++) {
                        ((JMenu)mediator.getAssemble().getTutorialsMenu()).getItem(i).setSelected(false);
                    }
                    mediator.getSecondaryCanvas().setPermanentTutorialModeTooTip(null);
                    mediator.getSecondaryCanvas().getMessagingSystem().setUnResponsive(false); //we need to feed the new thread
                    java.util.List<String> texts = new ArrayList<String>();
                    java.util.List<MessagingSystemAction> closeActions = new ArrayList<MessagingSystemAction>(),
                            nextActions = new ArrayList<MessagingSystemAction>();
                    texts.add("You have activated the help mode. Click on the arrow in the lower-right corner.");
                    closeActions.add(new MessagingSystemAction() { //if the user close at this step, this means that he doesn't want the basic introduction anymore
                        @Override
                        public void run() {
                            mediator.getSecondaryCanvas().getMessagingSystem().clear();
                            mediator.getSecondaryCanvas().setPermanentHelpModeToolTip(null);
                        }
                    });
                    nextActions.add(null);
                    texts.add("When the help mode is on, some actions trigger messages to give you some hints.");
                    closeActions.add(new MessagingSystemAction() { //if the user close at this step, this means that he doesn't want the basic introduction anymore
                        @Override
                        public void run() {
                            mediator.getSecondaryCanvas().getMessagingSystem().clear();
                            mediator.getSecondaryCanvas().setPermanentHelpModeToolTip(null);
                        }
                    });
                    nextActions.add(null);
                    texts.add("Move your mouse over a residue to have a demo.");
                    closeActions.add(new MessagingSystemAction() { //if the user close at this step, this means that he doesn't want the basic introduction anymore
                        @Override
                        public void run() {
                            mediator.getSecondaryCanvas().getMessagingSystem().clear();
                            mediator.getSecondaryCanvas().setPermanentHelpModeToolTip(null);
                        }
                    });
                    nextActions.add(null);
                    mediator.getSecondaryCanvas().getMessagingSystem().addThread(texts, closeActions, nextActions);

                } else {
                    mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("You have stopped the help mode.", null, null);
                    mediator.getSecondaryCanvas().setPermanentHelpModeToolTip(null);
                }
                mediator.getSecondaryCanvas().repaint();
                return;
            }
            startX += buttonShape.getWidth()+5;
        }

        if (this.activity != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.activity.getBounds2D().getMinX()+startX, this.activity.getBounds2D().getBounds2D().getMinY()+startY, this.activity.getBounds2D().getWidth(), this.activity.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (currentWorker != null)  {
                    stopActivity();
                } else {
                    new javax.swing.SwingWorker() {
                        @Override
                        protected Object doInBackground() throws Exception {
                            if (computingServer != null) {
                                computingServer.setVisible(true);
                                computingServer.toFront();
                            } else {
                                computingServer = new ComputingServer();
                            }
                            return null;
                        }
                    }.execute();
                }
                return;
            }
            startX += buttonShape.getWidth()+5;
        }
    }

    public void mouseMoved(MouseEvent e, int startX, int startY) {
        boolean redraw = false;
        if (this.help != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.help.getBounds2D().getMinX()+startX, this.help.getBounds2D().getBounds2D().getMinY()+startY, this.help.getBounds2D().getWidth(), this.help.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                if (! overHelp)
                    redraw = true;
                overHelp = true;
            } else {
                if (overHelp)
                    redraw = true;
                overHelp = false;
            }
            startX += buttonShape.getWidth()+5;
        }
        if (this.activity != null) {
            Rectangle2D buttonShape = new Rectangle2D.Double(this.activity.getBounds2D().getMinX()+startX, this.activity.getBounds2D().getBounds2D().getMinY()+startY, this.activity.getBounds2D().getWidth(), this.activity.getBounds2D().getHeight());
            if (buttonShape.contains(e.getX(), e.getY())) {
                return;
            }
            startX += buttonShape.getWidth()+5;
        }

        if (redraw)
            this.mediator.getSecondaryCanvas().repaint();
    }

    private class ComputingServer extends JFrame {
        private JFXPanel jfxPanel;

        private ComputingServer() {
            this.setTitle("Computing Server");
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

                    stage.setTitle("Computing Server");

                    Scene scene = new Scene(new Group());
                    stage.setScene(scene);

                    // Set up the embedded browser:
                    String baseURL = AssembleConfig.getWebservicesAddress().get(0)+"/server";
                    WebView browser = new WebView();
                    final WebEngine webEngine = browser.getEngine();
                    webEngine.load(baseURL);

                    root.setCenter(browser);

                    javafx.scene.control.Button home = new javafx.scene.control.Button(null, fontAwesome.create(FontAwesome.Glyph.HOME));
                    home.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
                        @Override
                        public void handle(javafx.event.ActionEvent actionEvent) {
                            webEngine.load(baseURL);
                        }
                    });

                    final HBox hbox = new HBox();
                    hbox.setPadding(new javafx.geometry.Insets(15, 12, 15, 12));
                    hbox.setSpacing(10);
                    hbox.setStyle("-fx-background-color: #3F7D97;");

                    hbox.getChildren().add(home);

                    root.setTop(hbox);

                    scene.setRoot(root);

                    jfxPanel.setScene(scene);
                }
            });
        }



    }
}
