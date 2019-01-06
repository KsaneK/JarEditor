import javax.swing.*;
import java.awt.event.KeyEvent;

class MainPane extends JTabbedPane {
    private PackagePanel packagePanel;
    private ClassPanel classPanel;

    private JarTree jarTree;

    MainPane(JarTree jarTree) {
        super();
        this.jarTree = jarTree;
        packagePanel = new PackagePanel(this.jarTree);
        classPanel = new ClassPanel(this.jarTree);
        addTab("Package control", packagePanel);
        setMnemonicAt(0, KeyEvent.VK_1);
        addTab("Class control", classPanel);
        setMnemonicAt(1, KeyEvent.VK_2);
    }

    PackagePanel getPackagePanel() {
        return packagePanel;
    }

    ClassPanel getClassPanel() {
        return classPanel;
    }
}
