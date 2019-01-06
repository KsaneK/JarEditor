import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

class JarTree extends JTree {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private Map<Path, DefaultMutableTreeNode> pathNodes = new LinkedHashMap<>();
    private JarFile jarFile = null;
    private String jarFilePath = null;
    private DefaultMutableTreeNode activeDirectory;
    private DefaultTreeModel model;

    JarTree() {
        super(new DefaultMutableTreeNode("No JAR selected"));
    }

    String getJarFilePath() {
        return jarFilePath;
    }

    DefaultMutableTreeNode getActiveDirectory() {
        return activeDirectory;
    }

    void exportToJar(File selectedFile) throws IOException {
        logger.info(String.format("Exporting to %s.", selectedFile.getAbsolutePath()));
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(selectedFile));
        ByteArrayInputStream inputStream;
        for (Map.Entry<Path, DefaultMutableTreeNode> entry : pathNodes.entrySet()) {
            if (entry.getKey() == null) { logger.info("null"); continue; }
            TreeNode treeNode = (TreeNode) entry.getValue().getUserObject();
            logger.info(String.format("Path: %s", entry.getKey().toString()));

            try {
                treeNode.update();
            } catch (CannotCompileException e) {
                logger.warning(String.format("Can't compile %s.", treeNode.getRealName()));
                logger.warning(e.getMessage());
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
            String realName = (treeNode.isDirectory()) ? treeNode.getRealName() + "/" : treeNode.getRealName();
            jarOutputStream.putNextEntry(new JarEntry(realName));
            if (!treeNode.isDirectory()) {
                inputStream = new ByteArrayInputStream(treeNode.getFileBytes());
                jarOutputStream.write(inputStream.readAllBytes());
                jarOutputStream.closeEntry();
            }
        }
        jarOutputStream.close();
    }

    /**
     * Creates JTree from JAR file
     * @param file JAR file
     * @throws IOException thrown when can't create JarInputStream
     */
    void loadJar(File file) throws IOException {
        logger.info("Loading " + file.getName());
        jarFile = new JarFile(file);
        jarFilePath = file.getAbsolutePath();
        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(file));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(file.getName()); // Use name of the file as root
        pathNodes.put(null, root);
        JarEntry entry;
        while ((entry = jarInputStream.getNextJarEntry()) != null) { // While entry is not null
            logger.info(String.format("Adding %s.", entry.getRealName()));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNode(jarFile, entry, true)); // Create tree node
            Path path = Paths.get(entry.getRealName()); // Create entry path
            DefaultMutableTreeNode dirNode = pathNodes.get(path.getParent()); // Get node related to directory
            dirNode.add(node); // Add entry node to directory node
            pathNodes.put(path, node); // Add node to map
        }
        DefaultMutableTreeNode manifest = new DefaultMutableTreeNode(
                new TreeNode(jarFile, jarFile.getJarEntry("META-INF/MANIFEST.MF"), true));
        pathNodes.put(Paths.get("META-INF/MANIFEST.MF"), manifest);
        pathNodes.get(Paths.get("META-INF")).add(manifest);

        model = new DefaultTreeModel(root);
        setModel(model);
    }

    DefaultMutableTreeNode select(TreePath path) {
        String realPath = TreePathParser.getRealPath(path);
        Path p;
        if (realPath == null) p = null;
        else p = Paths.get(realPath);
        DefaultMutableTreeNode node = pathNodes.get(p);
        if (node == null) {
            logger.warning("Node is null.");
            return null;
        }
        setActivePath(node);
        logger.info(String.format("Path: %s", TreePathParser.getRealPath(path)));
        return node;
    }

    void addNode(Path path, DefaultMutableTreeNode node) {
        pathNodes.put(path, node);
    }

    void reload() {
        model.reload();
    }

    private void setActivePath(DefaultMutableTreeNode node) {
        if (node.isRoot()) {
            activeDirectory = node;
        } else {
            TreeNode treeNode = (TreeNode) node.getUserObject();
            if (treeNode.isDirectory()) activeDirectory = node;
            else activeDirectory = pathNodes.get(Paths.get(treeNode.getRealName()).getParent());
        }
        logger.info(String.format("Active directory: %s", activeDirectory.toString()));
    }
}
