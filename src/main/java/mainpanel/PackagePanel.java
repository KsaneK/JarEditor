package mainpanel;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import tree.JarTree;
import tree.TreeNode;
import utils.LoggerFormatter;
import utils.TreePathParser;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PackagePanel extends JPanel {
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private JarTree jarTree;
    private Map<JarEditButtons, JButton> pkgBtns;
    private Map<JarEditButtons, JButton> modBtns;
    private DefaultMutableTreeNode dirNode = null;
    private DefaultMutableTreeNode activeNode = null;
    private Map<String, JLabel> clsLabels;
    private CtClass ctClass;

    public PackagePanel(JarTree jarTree) {
        this.jarTree = jarTree;
        initButtons();
        addModButtons();
        pkgBtns.get(JarEditButtons.ADD_PACKAGE).setEnabled(true);
        pkgBtns.get(JarEditButtons.ADD_CLASS).setEnabled(true);
        pkgBtns.get(JarEditButtons.ADD_INTERFACE).setEnabled(true);

        addInfoPanel();
    }

    public void setActiveNodes(DefaultMutableTreeNode dir, DefaultMutableTreeNode node) {
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
        if (!treeNode.isBaseNode() && !treeNode.isDirectory()) {
            pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(true);
        } else {
            pkgBtns.get(JarEditButtons.DELETE_CLASS).setEnabled(false);
        }
        ctClass = ((TreeNode) activeNode.getUserObject()).getCtClass();
        if (ctClass != null) {
            setLabelsText(ctClass);
            for (JButton btn : modBtns.values()) btn.setEnabled(true);
        } else {
            for (JButton btn : modBtns.values()) btn.setEnabled(false);
        }
    }

    private void initButtons() {
        pkgBtns = new HashMap<>();
        addButton(pkgBtns, JarEditButtons.ADD_PACKAGE, "Add package");
        addButton(pkgBtns, JarEditButtons.DELETE_PACKAGE, "Delete package");
        addButton(pkgBtns, JarEditButtons.ADD_CLASS, "Add class");
        addButton(pkgBtns, JarEditButtons.ADD_INTERFACE, "Add interface");
        addButton(pkgBtns, JarEditButtons.DELETE_CLASS, "Delete class/interface");

        pkgBtns.get(JarEditButtons.DELETE_PACKAGE).addActionListener(this::deletePackage);
        pkgBtns.get(JarEditButtons.DELETE_CLASS).addActionListener(this::deleteClass);
        pkgBtns.get(JarEditButtons.ADD_CLASS).addActionListener(e -> addClass(false));
        pkgBtns.get(JarEditButtons.ADD_PACKAGE).addActionListener(this::addPackage);
        pkgBtns.get(JarEditButtons.ADD_INTERFACE).addActionListener(e -> addClass(true));
    }

    private void addModButtons() {
        modBtns = new HashMap<>();
        addButton(modBtns, JarEditButtons.SET_CLASS_INTERFACE, "Toggle interface");
        addButton(modBtns, JarEditButtons.SET_CLASS_ABSTRACT, "Toggle abstract");
        addButton(modBtns, JarEditButtons.SET_CLASS_ENUM, "Toggle enum");
        addButton(modBtns, JarEditButtons.SET_CLASS_FINAL, "Toggle final");
        modBtns.get(JarEditButtons.SET_CLASS_INTERFACE).addActionListener(e -> toggleModifier(Modifier.INTERFACE));
        modBtns.get(JarEditButtons.SET_CLASS_ABSTRACT).addActionListener(e -> toggleModifier(Modifier.ABSTRACT));
        modBtns.get(JarEditButtons.SET_CLASS_ENUM).addActionListener(e -> toggleModifier(Modifier.ENUM));
        modBtns.get(JarEditButtons.SET_CLASS_FINAL).addActionListener(e -> toggleModifier(Modifier.FINAL));
    }

    private void toggleModifier(int modifier) {
        int classMod = ctClass.getModifiers();
        if ((modifier & classMod) == modifier) modifier = classMod & ~modifier;
        else modifier = classMod | modifier;
        ctClass.setModifiers(modifier);
        setLabelsText(ctClass);
    }

    private void addButton(Map<JarEditButtons, JButton> map, JarEditButtons enumVal, String text) {
        map.put(enumVal, new JButton(text));
        map.get(enumVal).setEnabled(false);
        add(map.get(enumVal));
    }

    private void addInfoPanel() {
        clsLabels = new LinkedHashMap<>();
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        clsLabels.put("classname", new JLabel());
        clsLabels.put("interface", new JLabel());
        clsLabels.put("abstract", new JLabel());
        clsLabels.put("enum", new JLabel());
        clsLabels.put("final", new JLabel());
        for (JLabel label : clsLabels.values()) infoPanel.add(label);
        add(infoPanel);
    }

    private void setLabelsText(CtClass ctClass) {
        if (ctClass == null) {
            for (JLabel label : clsLabels.values()) label.setText("");
        } else {
            int mod = ctClass.getModifiers();
            clsLabels.get("classname").setText(ctClass.getName());
            clsLabels.get("interface").setText(String.format("isInterface: %b", Modifier.isInterface(mod)));
            clsLabels.get("abstract").setText(String.format("isAbstract: %b", Modifier.isAbstract(mod)));
            clsLabels.get("enum").setText(String.format("isEnum: %b", Modifier.isEnum(mod)));
            clsLabels.get("final").setText(String.format("isFinal: %b", Modifier.isFinal(mod)));
        }
    }

    private void addPackage(ActionEvent e) {
        if (dirNode == null) {
            JOptionPane.showMessageDialog(null, "Select package before adding new package.");
            return;
        }
        String realPath = JOptionPane.showInputDialog(null,
                "Enter package name.",
                "Package creation",
                JOptionPane.PLAIN_MESSAGE);
        realPath = ((TreeNode) dirNode.getUserObject()).getRealName() + realPath + "/";
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new TreeNode(realPath));
        dirNode.add(newNode);
        jarTree.addNode(Paths.get(realPath), newNode);
        jarTree.reload();
    }

    private void addClass(boolean isInterface) {
        if (dirNode == null) {
            JOptionPane.showMessageDialog(null, "Select package before adding class.");
            return;
        }
        String realPath = JOptionPane.showInputDialog(null,
                "Enter class name.",
                "Class creation",
                JOptionPane.PLAIN_MESSAGE);
        realPath = ((TreeNode) dirNode.getUserObject()).getRealName() + realPath + ".class";

        String className = TreePathParser.getClassNameFromRealName(realPath);
        ClassPool classPool = ClassPool.getDefault();
        CtClass newClass;
        if (isInterface)newClass = classPool.makeInterface(className);
        else newClass = classPool.makeClass(className);

        TreeNode treeNode = new TreeNode(newClass, realPath);

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(treeNode);
        dirNode.add(newNode);
        jarTree.addNode(Paths.get(realPath), newNode);
        jarTree.reload();
    }

    private void deleteClass(ActionEvent e) {
        jarTree.deleteNode(Paths.get(((TreeNode)activeNode.getUserObject()).getRealName()));
        dirNode.remove(activeNode);
        activeNode = dirNode;
        jarTree.reload();
    }

    private void deletePackage(ActionEvent e) {
        if (activeNode.getChildCount() > 0) {
            JOptionPane.showMessageDialog(null, "There are still entries in this package.");
            return;
        }
        if (activeNode.isRoot()) {
            JOptionPane.showMessageDialog(null, "You can't delete root node.");
            return;
        }
        TreeNode treeNode = (TreeNode) activeNode.getUserObject();
        if (treeNode.isBaseNode()) {
            JOptionPane.showMessageDialog(null, "You can't delete base file node.");
        }
        logger.info(String.format("Deleting package %s", treeNode.getRealName()));
        jarTree.deleteNode(Paths.get(treeNode.getRealName()));
        if (dirNode == activeNode) dirNode = jarTree.getNode(Paths.get(treeNode.getRealName()).getParent());
        dirNode.remove(activeNode);
        activeNode = dirNode;
        jarTree.reload();
    }
}
