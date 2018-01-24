package fr.unistra.ibmc.assemble2.utils;

import fr.unistra.ibmc.assemble2.model.StructuralAlignment;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bson.types.ObjectId;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.jdom.input.SAXBuilder;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.List;
import java.util.ArrayList;

import fr.unistra.ibmc.assemble2.Assemble;

public class AssembleConfig {

    private static Document document;
    private static List<String> availableServers = new ArrayList<>();

    public static void loadConfig() throws BackingStoreException, IOException {
        if (document != null)
            return;
        File assembleUserDir = Assemble.getUserDir();
        File configFile = new File(assembleUserDir, "config.xml");
        if (!configFile.exists()) {
            Element root = new Element("assemble-config");
            root.setAttribute("version", Assemble.CURRENT_RELEASE);
            document = new Document(root);
            saveConfig();
        } else {
            SAXBuilder builder = new SAXBuilder();
            try {
                document = builder.build(configFile);
                if (document.getRootElement().getAttribute("version") == null) { //if no version attribute, the config file comes from a release before the 1.0 Release Candidate 1, and so => deletion
                    configFile.delete();
                    Element root = new Element("assemble-config");
                    root.setAttribute("version", Assemble.CURRENT_RELEASE);
                    document = new Document(root);
                    showWelcomeDialog(true);
                    saveConfig();
                } else {
                    if (!document.getRootElement().getAttribute("version").getValue().trim().equals(Assemble.CURRENT_RELEASE))
                        showWelcomeDialog(true);
                    document.getRootElement().setAttribute("version", Assemble.CURRENT_RELEASE);
                    //recover the user colors (if any)
                    Element colors = document.getRootElement().getChild("colors");
                    if (colors != null) {
                        for (Object o:colors.getChildren("color")) {
                            Element color = (Element)o;
                            String[] value = color.getAttributeValue("value").split(" ");
                            String target = color.getAttributeValue("target");
                            if ("secondary-interactions".equals(target))
                                Assemble.SecondaryInteraction_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                            else if ("tertiary-interactions".equals(target))
                                Assemble.TertiaryInteraction_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                            else if ("adenine".equals(target))
                                Assemble.A_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                            else if ("uracil".equals(target))
                                Assemble.U_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                            else if ("guanine".equals(target))
                                Assemble.G_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                            if ("cytosine".equals(target))
                                Assemble.C_Color = new Color(Integer.parseInt(value[0]),Integer.parseInt(value[1]),Integer.parseInt(value[2]));
                        }
                    }
                    Element assemble_id = document.getRootElement().getChild("id");
                    if (assemble_id == null) {
                        assemble_id = new Element("id");
                        assemble_id.setText(new ObjectId().toString());
                        document.getRootElement().addContent(assemble_id);
                    }
                    saveConfig();
                }

            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadServersFromGithub();
        }
    }

    public static void saveUserColors() {
        Element colors = document.getRootElement().getChild("colors");
        if (colors == null) {
            colors = new Element("colors");
            document.getRootElement().addContent(colors);
        }
        else
            colors.removeChildren("color");
        Element color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","secondary-interactions");
        color.setAttribute("value", Assemble.SecondaryInteraction_Color.getRed()+" "+Assemble.SecondaryInteraction_Color.getGreen()+" "+Assemble.SecondaryInteraction_Color.getBlue());

        color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","tertiary-interactions");
        color.setAttribute("value",Assemble.TertiaryInteraction_Color.getRed()+" "+Assemble.TertiaryInteraction_Color.getGreen()+" "+Assemble.TertiaryInteraction_Color.getBlue());

        color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","adenine");
        color.setAttribute("value",Assemble.A_Color.getRed()+" "+Assemble.A_Color.getGreen()+" "+Assemble.A_Color.getBlue());

        color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","uracil");
        color.setAttribute("value",Assemble.U_Color.getRed()+" "+Assemble.U_Color.getGreen()+" "+Assemble.U_Color.getBlue());

        color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","guanine");
        color.setAttribute("value",Assemble.G_Color.getRed()+" "+Assemble.G_Color.getGreen()+" "+Assemble.G_Color.getBlue());

        color = new Element("color");
        colors.addContent(color);
        color.setAttribute("target","cytosine");
        color.setAttribute("value",Assemble.C_Color.getRed()+" "+Assemble.C_Color.getGreen()+" "+Assemble.C_Color.getBlue());
    }

    public static void clearRecentFiles() {
        Element e = document.getRootElement().getChild("recent-files");
        if (e != null)
            e.removeChildren("file");
    }

    public static void setNumberOfSequencesToDisplay(int nb) {
        document.getRootElement().getChild("sequences-2-display-in-alignment").setText(""+nb);
    }

    public static int getNumberOfSequencesToDisplay(StructuralAlignment alignment) {
        Element e = document.getRootElement().getChild("sequences-2-display-in-alignment");
        if (e == null) {
            e = new Element("sequences-2-display-in-alignment");
            e.setText("1");
            document.getRootElement().addContent(e);
        }
        //the number of sequences to display could be greater than the total number of sequences in the alignment, so...
        int nb = Integer.parseInt(e.getTextTrim());
        nb = nb > alignment.getBiologicalSequenceCount() ? alignment.getBiologicalSequenceCount() : nb;
        return nb;
    }

    private static void loadServersFromGithub() {

        try {
            URL url = null;
            url = new URL("https://raw.githubusercontent.com/fjossinet/RNA-Science-Toolbox/master/servers.txt");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                availableServers.add("http://"+inputLine);
            }
            availableServers.add("http://localhost:8080");
            in.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void addWebservicesAddress(String address) {
        Element url = new Element("url");
        url.setText(address.trim());
        document.getRootElement().getChild("webservices-address").addContent(0, url);
    }

    public static List<String> getAvailableServers() {
        return availableServers;
    }

    public static void setCurrentServer(String address) {
        document.getRootElement().getChild("current-server").setText(address);
    }

    public static String getCurrentServer() {
        Element e = document.getRootElement().getChild("current-server");
        if (e == null) {
            e = new Element("current-server");
            e.setText(availableServers.get(0));
            document.getRootElement().addContent(e);
            setCurrentServer(availableServers.get(0));
        }
        return e.getTextTrim();
    }

    public static String getMongoDBAddress() {
        Element e = document.getRootElement().getChild("mongodb-address");
        if (e == null) {
            e = new Element("mongodb-address");
            e.setText("localhost:27017");
            document.getRootElement().addContent(e);
        }
        return e.getTextTrim();
    }

    public static void setMongoDBAddress(String address) {
        document.getRootElement().getChild("mongodb-address").setText(address);
    }

    public static String getID() {
       return document.getRootElement().getChild("id").getText();
    }

    public static String getFragmentsLibrary() {
        Element e = document.getRootElement().getChild("fragments-library");
        if (e == null) {
            e = new Element("fragments-library");
            e.setText("Non redundant");
            document.getRootElement().addContent(e);
        }
        return e.getTextTrim();
    }

    public static void setFragmentsLibrary(String library) {
        document.getRootElement().getChild("fragments-library").setText(library);
    }

    public static List<MutablePair<String,String>> getRecentEntries() {
        Element e = document.getRootElement().getChild("recent-entries");
        if (e == null) {
            e = new Element("recent-entries");
            document.getRootElement().addContent(e);
        }
        List<MutablePair<String,String>> files = new ArrayList<MutablePair<String,String>>();
        UPPERFOR: for (Object o: e.getChildren("entry")) {
            Element entry = (Element)o;
            for (MutablePair<String,String> f:files)
                if (f.getLeft().equals(entry.getAttributeValue("id")) && f.getRight().equals(entry.getAttributeValue("type"))) {
                    continue UPPERFOR;
                }
            files.add(new MutablePair<String, String>(entry.getAttributeValue("id"), entry.getAttributeValue("type")));
        }
        document.getRootElement().removeContent(e);
        e = new Element("recent-entries");
        document.getRootElement().addContent(e);
        for (MutablePair<String,String> f:files) {
            Element file = new Element("entry");
            file.setAttribute("id",f.getLeft());
            file.setAttribute("type",f.getRight());
            e.addContent(file);
        }
        return files;
    }

    public static void addRecentEntry(String id, String type) {
        Element e = document.getRootElement().getChild("recent-entries");
        if (e == null) {
            e = new Element("recent-entries");
            document.getRootElement().addContent(e);
        }
        Element file = new Element("entry");
        file.setAttribute("id", id);
        file.setAttribute("type", type);
        List files = new ArrayList(e.getChildren("entry"));
        if (files.size() == 10) {
            e.removeContent((Element)files.get(files.size()-1));
            e.addContent(0,file);
        }
        else  {
            for (Object o:files) {
                Element _f = (Element)o;
                if (_f.getAttributeValue("id").equals(id) && _f.getAttributeValue("type").equals(type)) {
                    e.removeContent(_f);
                }
            }
            e.addContent(0,file);
        }
    }

    public static void setChimeraPath(String path) {
        document.getRootElement().getChild("external-viewers").getChild("chimera-path").setText(path);
    }

    public static String getChimeraPath() {
        Element e = document.getRootElement().getChild("external-viewers");
        if (e == null) {
            String osName = System.getProperty("os.name");
            e = new Element("external-viewers");
            e.addContent(new Element("chimera-path"));
            document.getRootElement().addContent(e);
            if (osName.startsWith("Mac OS")) {
                e.getChild("chimera-path").setText("/Applications/Chimera.app/Contents/MacOS/chimera");
            } else if (osName.startsWith("Windows")) {
                e.getChild("chimera-path").setText("c:\\Program Files\\Chimera\\bin\\chimera");
            } else {
                e.getChild("chimera-path").setText("/usr/local/chimera/bin/chimera");
            }
        }
        else {
            Element _e = e.getChild("chimera-path");
            if (_e == null)
                e.addContent(new Element("chimera-path"));
        }
        return document.getRootElement().getChild("external-viewers").getChild("chimera-path").getValue();
    }

    public static void saveConfig() throws BackingStoreException, IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(new File(Assemble.getUserDir(),"config.xml"));
        outputter.output(document, writer);
        writer.close();
    }

    public static boolean showHelpToolTip() {
        Element e = document.getRootElement().getChild("show-help-tooltip");
        if (e == null) {
            e = new Element("show-help-tooltip");
            e.setText("true");
            document.getRootElement().addContent(e);
        }
        return new Boolean(e.getTextTrim());
    }

    public static void showHelpToolTip(boolean show) {
        document.getRootElement().getChild("show-help-tooltip").setText(""+show);
    }

    public static boolean showWelcomeDialog() {
        Element e = document.getRootElement().getChild("show-welcome-dialog");
        if (e == null) {
            e = new Element("show-welcome-dialog");
            e.setText("true");
            document.getRootElement().addContent(e);
        }
        //return new Boolean(e.getTextTrim());
        return false;
    }

    public static void showWelcomeDialog(boolean show) {
        Element e = document.getRootElement().getChild("show-welcome-dialog");
        if (e == null) {
            e = new Element("show-welcome-dialog");
            e.setText("true");
            document.getRootElement().addContent(e);
        }
        document.getRootElement().getChild("show-welcome-dialog").setText(""+show);
    }

    public static boolean launchChimeraAtStart() {
        Element e = document.getRootElement().getChild("launch-chimera");
        if (e == null) {
            e = new Element("launch-chimera");
            e.setText("false");
            document.getRootElement().addContent(e);
        }
        return new Boolean(e.getTextTrim());
    }

    public static void launchChimeraAtStart(boolean launch) {
        document.getRootElement().getChild("launch-chimera").setText(""+launch);
    }

    public static boolean useLocalAlgorithms() {
        Element e = document.getRootElement().getChild("local-algorithms");
        if (e == null) {
            e = new Element("local-algorithms");
            e.setText("false");
            document.getRootElement().addContent(e);
        }
        return new Boolean(e.getTextTrim());
    }

    public static void useLocalAlgorithms(boolean use) {
        document.getRootElement().getChild("local-algorithms").setText(""+use);
    }


    public static boolean popupLateralPanels() {
        Element e = document.getRootElement().getChild("popup-lateral-panels");
        if (e == null) {
            e = new Element("popup-lateral-panels");
            e.setText("true");
            document.getRootElement().addContent(e);
        }
        return new Boolean(e.getTextTrim());
    }

    public static void popupLateralPanels(boolean popup) {
        document.getRootElement().getChild("popup-lateral-panels").setText(""+popup);
    }

}
