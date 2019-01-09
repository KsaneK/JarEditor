import javassist.*;
import mainpanel.MainPane;
import tree.JarTree;
import tree.TreeNode;
import utils.LoggerFormatter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.logging.Logger;

public class JarEditor extends JFrame {
    private MainPane mainPane;
    private JarTree fileTree;
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    public JarEditor(String title) {
        super(title);

        logger.info("Creating Jar Editor Frame.");
        // Set default close operation and size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1100, 700));
        // Set border layout
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(5);
        setLayout(borderLayout);
        // Init File Tree
        initScrollPaneWithJTree();
        // Create menu bar
        setJMenuBar(new MenuBar(this));
        // Init Side Panel
        initMainPanel();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public JarTree getFileTree() {
        return fileTree;
    }

    private void initMainPanel() {
        mainPane = new MainPane(fileTree);
        getContentPane().add(mainPane);
    }

    private void initScrollPaneWithJTree() {
        logger.info("Init ScrollPane and JTree.");
        fileTree = new JarTree();
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = fileTree.getSelectedNode(e.getPath());
            DefaultMutableTreeNode dirNode = fileTree.getActiveDirectory();
            mainPane.getPackagePanel().setActiveNodes(dirNode, selectedNode);
            CtClass ctClass = ((TreeNode)selectedNode.getUserObject()).getCtClass();
            if (ctClass != null)
                mainPane.getClassPanel().setCtClass(ctClass);

        });
        JScrollPane treeScrollPane = new JScrollPane(fileTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setPreferredSize(new Dimension(300, 700));
        getContentPane().add(treeScrollPane, BorderLayout.WEST);
    }
}
