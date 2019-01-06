import javassist.ClassPool;
import javassist.CtClass;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PackagePanel extends JPanel {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private JarTree jarTree;
    private Map<JarEditButtons, JButton> pkgBtns;
    private DefaultMutableTreeNode dirNode = null;
    private DefaultMutableTreeNode activeNode = null;

    public PackagePanel(JarTree jarTree) {
        this.jarTree = jarTree;
        initButtons();
        pkgBtns.get(JarEditButtons.ADD_PACKAGE).setEnabled(true);
        pkgBtns.get(JarEditButtons.ADD_CLASS).setEnabled(true);
    }

    void setActiveNodes(DefaultMutableTreeNode dir, DefaultMutableTreeNode node) {
        dirNode = dir;
        activeNode = node;
        if (activeNode.isRoot()) {
            pkgBtns.get(JarEditButtons.DELETE_PACKAGE).setEnabled(false);
            pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(false);
            return;
        }
        TreeNode treeNode = (TreeNode) node.getUserObject();

        if (!treeNode.isBaseNode() && treeNode.isDirectory())
            pkgBtns.get(JarEditButtons.DELETE_PACKAGE).setEnabled(true);
        else
            pkgBtns.get(JarEditButtons.DELETE_PACKAGE).setEnabled(false);
        if (!treeNode.isBaseNode() && !treeNode.isDirectory())
            pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(true);
        else
            pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(false);
    }

    private void initButtons() {
        pkgBtns = new HashMap<>();
        pkgBtns.put(JarEditButtons.ADD_PACKAGE, new JButton("Add package"));
        pkgBtns.get(JarEditButtons.ADD_PACKAGE).setEnabled(false);
        pkgBtns.put(JarEditButtons.DELETE_PACKAGE, new JButton("Delete package"));
        pkgBtns.get(JarEditButtons.DELETE_PACKAGE).setEnabled(false);
        pkgBtns.put(JarEditButtons.ADD_CLASS, new JButton("Add class to package"));
        pkgBtns.get(JarEditButtons.ADD_CLASS).setEnabled(false);
        pkgBtns.put(JarEditButtons.DELETE_CLASS, new JButton("Delete class from package"));
        pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(false);

        pkgBtns.get(JarEditButtons.DELETE_PACKAGE).addActionListener(this::deletePackage);
        pkgBtns.get(JarEditButtons.DELETE_CLASS).addActionListener(this::deleteClass);
        pkgBtns.get(JarEditButtons.ADD_CLASS).addActionListener(this::addClass);
        pkgBtns.get(JarEditButtons.ADD_PACKAGE).addActionListener(this::addPackage);

        for (JButton btn : pkgBtns.values()) add(btn);
    }

    private void addPackage(ActionEvent e) {
    }

    private void addClass(ActionEvent e) {
        String realPath = JOptionPane.showInputDialog(null,
                "Enter class name.",
                "Class creation",
                JOptionPane.PLAIN_MESSAGE);

        if (!dirNode.isRoot())
            realPath = ((TreeNode) dirNode.getUserObject()).getRealName() + realPath;
        realPath += ".class";

        String className = TreePathParser.getClassNameFromRealName(realPath);
        ClassPool classPool = ClassPool.getDefault();
        CtClass newClass = classPool.makeClass(className);
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e1) {
            logger.warning(String.format("Class %s not found.", className));
        }

        TreeNode treeNode = new TreeNode(newClass, realPath);

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(treeNode);
        dirNode.add(newNode);
        jarTree.addNode(Paths.get(realPath), newNode);
        jarTree.reload();
    }

    private void deleteClass(ActionEvent e) {
        dirNode.remove(activeNode);
        activeNode = dirNode;
        jarTree.reload();
    }

    private void deletePackage(ActionEvent e) {
        TreeNode treeNode = (TreeNode) activeNode.getUserObject();
        logger.info(String.format("Deleting package %s", treeNode.getRealName()));
    }
}
