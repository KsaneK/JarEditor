import javassist.NotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.logging.Logger;

class MenuBar extends JMenuBar {
    private JarEditor parentFrame;
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    MenuBar(JarEditor frame) {
        super();
        logger.info("Creating MenuBar.");
        parentFrame = frame;
        JMenu file = new JMenu("File");
        JMenuItem loadJarItem = new JMenuItem("Load JAR");
        JMenuItem exportJarItem = new JMenuItem("Export JAR");
        JMenuItem exitItem = new JMenuItem("Exit");
        file.add(loadJarItem);
        file.add(exportJarItem);
        file.add(exitItem);

        addExitActionListener(exitItem);
        addLoadJarActionListener(loadJarItem);
        addExportJarActionListener(exportJarItem);

        add(file);
    }

    private void addExitActionListener(JMenuItem item) {
        logger.info("Adding action listener for Exit item.");
        item.addActionListener(e -> {
            logger.info("Clicked MenuBar Exit item.");
            parentFrame.dispose();
        });
    }

    private void addExportJarActionListener(JMenuItem item) {
        logger.info("Adding action listener for Export JAR item.");
        item.addActionListener(e -> {
            logger.info("Showing file save dialog.");
            JFileChooser fc = new JFileChooser();
            int selection = fc.showSaveDialog(parentFrame);
            if (selection == JFileChooser.APPROVE_OPTION) {
                logger.info("Save file approved.");
                try {
                    parentFrame.getFileTree().exportToJar(fc.getSelectedFile());
                } catch (IOException e1) {
                    logger.warning(e1.getMessage());
                    JOptionPane.showMessageDialog(parentFrame,
                            "Error. Make sure the selected file is correct.");
                }
            }
        });
    }

    private void addLoadJarActionListener(JMenuItem item) {
        logger.info("Adding action listener for Load JAR item.");
        item.addActionListener(e -> {
            logger.info("Showing file open dialog.");
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("JAR files", "jar"));
            int selection = fc.showOpenDialog(parentFrame);
            if (selection == JFileChooser.APPROVE_OPTION) {
                logger.info("Opening " + fc.getSelectedFile().getAbsolutePath());
                try {
                    parentFrame.getFileTree().loadJar(fc.getSelectedFile());
                    parentFrame.getClassPool().insertClassPath(parentFrame.getFileTree().getJarFilePath());
                } catch (IOException ioException) {
                    logger.warning("IO Error.");
                    JOptionPane.showMessageDialog(parentFrame,
                            "Error. Make sure the selected file is correct.");
                } catch (NotFoundException e1) {
                    logger.warning("ClassPath not found.");
                    e1.printStackTrace();
                }
            } else if (selection == JFileChooser.CANCEL_OPTION) {
                logger.info("Cancelled FileChooser");
            }
        });
    }
}
