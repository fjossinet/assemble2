package fr.unistra.ibmc.assemble2.utils;

import fr.unistra.ibmc.assemble2.Assemble;
import fr.unistra.ibmc.assemble2.gui.Mediator;
import fr.unistra.ibmc.assemble2.gui.SplashScreen;
import fr.unistra.ibmc.assemble2.model.Molecule;
import fr.unistra.ibmc.assemble2.model.SecondaryStructure;
import fr.unistra.ibmc.assemble2.model.TertiaryStructure;
import org.apache.commons.io.IOUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.mail.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class IoUtils {

    private static File lastFilePath = new File(System.getProperty("user.home"));

    public static String getAssemble2Release() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            return "Assemble2.2 Development Release: "+format.format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static File getTmpDirectory() {
        File tmpDir = new File(new StringBuffer(Assemble.getUserDir().getAbsolutePath()).append(System.getProperty("file.separator")).append("tmp").toString());
        if (!tmpDir.exists())
            tmpDir.mkdir();
        return tmpDir;
    }

    public static void setLastWorkingDirectory(File f) {
        lastFilePath = f.isDirectory() ? f:f.getParentFile();
    }

    public static File getLastWorkingDirectory() {
        return lastFilePath;
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

    /**
     * Extract a Jar or Zip file
     * @param destDir where to extract the file
     * @param archivedFile the file to extract (zip or jar files are supported)
     * @throws IOException
     */
    public static void extractArchivedFile(String destDir,File archivedFile, SplashScreen printingDevice) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(archivedFile);
        java.util.Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) entries.nextElement();
            if (printingDevice != null)
                printingDevice.setMessage("Extracting "+file.getName());
            java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            else {
                f.createNewFile();
                java.io.InputStream is = jar.getInputStream(file); // get the input stream
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0) {  // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }
    }

    /**
     * Center a window on the current screen
     *
     * @param window the window to center
     */
    public static void centerOnScreen(final java.awt.Window window) {
        final java.awt.Dimension win = window.getSize();
        final java.awt.Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        final int newX = (screen.width - win.width) / 2;
        final int newY = (screen.height - win.height) / 2;

        window.setLocation(newX, newY);
    }

    public static File createTemporaryDirectory(String fileName) throws IOException {
        File f = new File(IoUtils.getTmpDirectory(),fileName+System.nanoTime());
        f.mkdir();
        f.deleteOnExit();
        return f;
    }

    public static File createTemporaryFile(String fileName) throws IOException {
        File f = new File(IoUtils.getTmpDirectory(),fileName+System.nanoTime());
        f.createNewFile();
        f.deleteOnExit();
        return f;
    }

    public static File createTemporaryFile(File dir,String fileName) throws IOException {
        File f = new File(dir,fileName+System.nanoTime());
        f.createNewFile();
        f.deleteOnExit();
        return f;
    }

    public static void deleteDirectory(File directory) {
        clearDirectory(directory);
        directory.delete();
    }

    public static void clearDirectory(File directory) {
        for(File f:directory.listFiles())
            if(f.isDirectory())
                deleteDirectory(f);
            else
                f.delete();
    }

    public static void copyFile(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdir();
            }
            String[] children = source.list();
            for (int i=0; i<children.length; i++) {
                if (!children[i].equals(".svn"))
                    copyFile(new File(source, children[i]), new File(target, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public static void moveFile(File source, File dest) throws IOException {

        if (!dest.delete())
            System.out.println("Could not delete file : "+dest.getName());

        copyFile(source, dest);

        if (!source.delete()) {
            System.out.println("Could not delete file : " + source.getName());
        }
    }

    public static void openBrowser(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error attempting to launch web browser:\n" + e.getLocalizedMessage());
        }
    }

    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;

    public static void sendReport(final Mediator mediator, final String subject, final String content) {
        try {
            mailServerProperties = System.getProperties();
            mailServerProperties.put("mail.smtp.port", "587");
            mailServerProperties.put("mail.smtp.auth", "true");
            mailServerProperties.put("mail.smtp.starttls.enable", "true");

            getMailSession = Session.getDefaultInstance(mailServerProperties, null);
            generateMailMessage = new MimeMessage(getMailSession);
            generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("fjossinet@gmail.com"));
            //generateMailMessage.addRecipient(Message.RecipientType.CC, new InternetAddress("test2@crunchify.com"));
            generateMailMessage.setSubject(subject);
            generateMailMessage.setContent(content, "text/html");

            Transport transport = getMailSession.getTransport("smtp");
            transport.connect("smtp.gmail.com", "assemble2.reports", "Hbh-y8D-XM2-sdN");
            transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
            transport.close();

            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Email sent. Thank you!", null, null);
            mediator.getSecondaryCanvas().repaint();
        } catch (MessagingException e) {
            mediator.getSecondaryCanvas().getMessagingSystem().addSingleStep("Problem to send the email. Please try later.", null, null);
            mediator.getSecondaryCanvas().repaint();
        }
    }
}

