package mainpanel;

import tree.JarTree;
import javax.swing.*;
import java.awt.event.KeyEvent;

public class MainPane extends JTabbedPane {
    private PackagePanel packagePanel;
    private ClassPanel classPanel;

    public MainPane(JarTree jarTree) {
        super();
        packagePanel = new PackagePanel(jarTree);
        classPanel = new ClassPanel();
        addTab("Package control", packagePanel);
        setMnemonicAt(0, KeyEvent.VK_1);
        addTab("Class control", classPanel);
        setMnemonicAt(1, KeyEvent.VK_2);
    }

    public PackagePanel getPackagePanel() {
        return packagePanel;
    }

    public ClassPanel getClassPanel() {
        return classPanel;
    }
}
