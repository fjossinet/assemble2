package fr.unistra.ibmc.assemble2.gui;

import fr.unistra.ibmc.assemble2.utils.AssembleConfig;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ServersPanelController implements Initializable {

    @FXML
    private VBox vbox;
    private List<SelectServer> checkboxes;
    private WebView browser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vbox.setPadding(new javafx.geometry.Insets(10,10,10,10));
        vbox.setSpacing(10);
        List<String> servers = AssembleConfig.getAvailableServers();
        checkboxes = new ArrayList<SelectServer>(servers.size());

        HBox serversAvailable = new HBox();
        serversAvailable.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        serversAvailable.setSpacing(20);
        serversAvailable.setAlignment(Pos.CENTER_LEFT);

        String selectedServer = null;
        for (String server:servers) {
            HBox serverDetails = new HBox();
            serverDetails.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
            serverDetails.setAlignment(Pos.CENTER_LEFT);

            SelectServer s = new SelectServer(server);
            if (AssembleConfig.getCurrentServer().equals(server)) {
                s.setSelected(true);
                selectedServer = server;
            }
            checkboxes.add(s);
            serverDetails.getChildren().add(s);

            Hyperlink link = new Hyperlink();
            link.setText(server);
            link.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    WebEngine webEngine = browser.getEngine();
                    webEngine.load(server);
                }
            });
            serverDetails.getChildren().add(link);
            serversAvailable.getChildren().add(serverDetails);
        }

        vbox.getChildren().add(serversAvailable);

        browser = new WebView();
        if (selectedServer != null) {
            WebEngine webEngine = browser.getEngine();
            webEngine.load(selectedServer);
        }

        vbox.getChildren().add(browser);

    }

    private class SelectServer extends CheckBox {

        private String server;

        private SelectServer(String server) {
            this.server = server;
            this.setOnAction((event) -> {
                if (!SelectServer.this.isSelected()) { //we cannot unselect a selection. Just select a checkbox unselected
                    SelectServer.this.setSelected(true);
                    return;
                }
                for (SelectServer ss:checkboxes) {
                    if (ss != this) {
                        ss.setSelected(false);
                    }
                    AssembleConfig.setCurrentServer(server);
                }
            });
        }
    }


}
