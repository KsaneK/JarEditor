import javassist.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.logging.Logger;

class JarEditor extends JFrame {
    private MainPane mainPane;
    private JarTree fileTree;
    private ClassPool classPool = ClassPool.getDefault();

    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    JarEditor(String title) {
        super(title);

        logger.info("Creating Jar Editor Frame.");
        // Set default close operation and size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1024, 700));
        // Set layout
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(5);
        setLayout(borderLayout);
        // Create menu bar
        setJMenuBar(new MenuBar(this));
        // Init File Tree
        initScrollPaneWithJTree();
        // Init Side Panel
        initSidePanel();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    JarTree getFileTree() {
        return fileTree;
    }

    ClassPool getClassPool() {
        return classPool;
    }

    MainPane getMainPane() {
        return mainPane;
    }

    private void initSidePanel() {
        mainPane = new MainPane(fileTree);
        getContentPane().add(mainPane);
    }

    private void initScrollPaneWithJTree() {
        logger.info("Init ScrollPane and JTree.");
        fileTree = new JarTree();
        fileTree.addTreeSelectionListener(e -> {
            if (fileTree.getJarFilePath() == null) return;
            DefaultMutableTreeNode selected = fileTree.select(e.getPath());
            CtClass ctClass = null;
            if (!selected.isRoot())
                ctClass = ((TreeNode) selected.getUserObject()).getCtClass();
            if (ctClass != null) mainPane.getClassPanel().setCtClass(ctClass);
            DefaultMutableTreeNode dirNode = fileTree.getActiveDirectory();
            mainPane.getPackagePanel().setActiveNodes(dirNode, selected);
        });
        JScrollPane treeScrollPane = new JScrollPane(fileTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setPreferredSize(new Dimension(300, 700));
        getContentPane().add(treeScrollPane, BorderLayout.WEST);
    }
}
