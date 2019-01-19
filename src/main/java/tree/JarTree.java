package tree;

import javassist.CannotCompileException;
import utils.LoggerFormatter;
import utils.TreePathParser;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.*;
import java.util.logging.Logger;

public class JarTree extends JTree {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private Map<Path, DefaultMutableTreeNode> pathNodes = new LinkedHashMap<>();
    private DefaultMutableTreeNode selected = null;
    private DefaultTreeModel model = null;

    public JarTree() {
        super(new DefaultMutableTreeNode(new TreeNode("")));
    }

    public boolean isJarLoaded() {
        return model != null;
    }

    public DefaultMutableTreeNode getActiveDirectory() {
        TreeNode treeNode = (TreeNode) selected.getUserObject();
        logger.info(String.format("Getting active directory for: %s", treeNode.toString()));
        if (treeNode.isDirectory()) return selected;
        else return pathNodes.get(Paths.get(treeNode.getRealName()).getParent());
    }

    public DefaultMutableTreeNode getSelectedNode(TreePath path) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node == null) {
            logger.warning("Node is null.");
            return null;
        }
        selected = node;
        logger.info(String.format("Selected Path: %s", TreePathParser.getRealPath(path)));
        return node;
    }

    public void exportToJar(File selectedFile) throws IOException {
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

    public void loadJar(File file) throws IOException {
        logger.info("Loading " + file.getName());
        JarFile jarFile = new JarFile(file);
        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(file));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeNode("")); // Use name of the file as root
        pathNodes.put(null, root);
        JarEntry entry;
        while ((entry = jarInputStream.getNextJarEntry()) != null) { // While entry is not null
            logger.info(String.format("Adding %s.", entry.getRealName()));
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeNode(jarFile, entry, true)); // Create tree node
            Path path = Paths.get(entry.getRealName()); // Create entry path
            DefaultMutableTreeNode dirNode = pathNodes.get(path.getParent()); // Get node related to directory
            if (dirNode == null && path.getParent() != null) {
                dirNode = new DefaultMutableTreeNode(new TreeNode(path.getParent().toString() + "/"));
                pathNodes.put(Paths.get(path.getParent().toString() + "/"), dirNode);
            }
            dirNode.add(node); // Add entry node to directory node
            pathNodes.put(path, node); // Add node to map
        }
        JarEntry manifestEntry = jarFile.getJarEntry(jarFile.MANIFEST_NAME);
        if (manifestEntry != null) {
            DefaultMutableTreeNode manifest = new DefaultMutableTreeNode(
                    new TreeNode(jarFile, manifestEntry, true));
            pathNodes.put(Paths.get(jarFile.MANIFEST_NAME), manifest);
            pathNodes.get(Paths.get("META-INF")).add(manifest);
        }

        model = new DefaultTreeModel(root);
        setModel(model);
    }

    public void addNode(Path path, DefaultMutableTreeNode node) {
        pathNodes.put(path, node);
    }

    public DefaultMutableTreeNode getNode(Path path) {
        return pathNodes.get(path);
    }

    public void deleteNode(Path path) {
        pathNodes.remove(path);
    }

    public void reload() {
        model.reload();
    }
}
