import javassist.*;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

class ClassEditor extends JFrame {
    private DefaultListModel<CtBehavior> model;
    private JCheckBox isConstructor;
    private JTextPane textPane;
    private CtBehavior ctBehavior = null;
    private CtClass ctClass;

    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    ClassEditor(String title, CtClass ctClass, CtBehavior ctBehavior, DefaultListModel<CtBehavior> model) {
        super(title);
        this.ctClass = ctClass;
        this.ctBehavior = ctBehavior;
        this.model = model;
        init();
    }

    ClassEditor(String title, CtClass ctClass, DefaultListModel<CtBehavior> model) {
        super(title);
        this.ctClass = ctClass;
        this.model = model;
        init();
    }

    private void init() {
        setSize(new Dimension(800, 600));
        setLayout(new FlowLayout());

        initTextPane();
        isConstructor = new JCheckBox("Constructor", false);
        add(isConstructor);
        initButtons();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initButtons() {
        JButton before = new JButton("Prepend Method");
        JButton overwrite = new JButton((ctBehavior == null) ? "Create Method" : "Overwrite Method");
        JButton after = new JButton("Append Method");
        before.setVisible(ctBehavior != null);
        after.setVisible(ctBehavior != null);
        isConstructor.setVisible(ctBehavior == null);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(before);
        bottomPanel.add(overwrite);
        bottomPanel.add(after);
        before.addActionListener(e -> addCodeToMethod(true));
        after.addActionListener(e -> addCodeToMethod(false));
        overwrite.addActionListener(e -> {
            if (ctBehavior != null) logger.info(String.format("Overwritting method %s", ctBehavior.getName()));
            else logger.info(String.format("Creating new method for class %s", ctClass.getName()));
            boolean doUpdateBtns = (ctBehavior == null);
            if(overwriteMethod(ctBehavior == null) && doUpdateBtns) {
                overwrite.setText("Overwrite method");
                before.setVisible(true);
                after.setVisible(true);
                isConstructor.setVisible(false);
            }
        });
        add(bottomPanel);
    }

    private void initTextPane() {
        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - 80));
        add(scrollPane);
    }

    private boolean overwriteMethod(boolean isNull) {
        try {
            String methodBody = textPane.getText();
            String object = (isConstructor.isSelected()) ? "Constructor" : "Method";
            if (isNull) {
                if(isConstructor.isSelected()) {
                    ctBehavior = CtNewConstructor.make(methodBody, ctClass);
                    ctClass.addConstructor((CtConstructor) ctBehavior);
                    model.insertElementAt(ctBehavior, 0);
                } else {
                    ctBehavior = CtMethod.make(methodBody, ctClass);
                    ctClass.addMethod((CtMethod) ctBehavior);
                    model.addElement(ctBehavior);
                }
                logger.info(String.format("%s has been created.", object));
                JOptionPane.showMessageDialog(null, String.format("%s has been created", object));
            } else {
                ctBehavior.setBody(methodBody);
                logger.info(String.format("%s has been overwritten.", object));
                JOptionPane.showMessageDialog(null, String.format("%s has been overwritten", object));
            }
            return true;
        } catch (CannotCompileException e) {
            logger.warning("CannotCompileException " + e.getReason());
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
    }

    private void addCodeToMethod(boolean beginning) {
        String text = textPane.getText();
        try {
            JOptionPane.showMessageDialog(null,
                    String.format("Added code at the %s of the method.", beginning ? "beginning" : "end"));
            if (beginning) ctBehavior.insertBefore(text);
            else ctBehavior.insertAfter(text);
        } catch (CannotCompileException e) {
            logger.warning("CannotCompileException " + e.getReason());
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (RuntimeException r) {
            logger.warning(r.getMessage());
            JOptionPane.showMessageDialog(null, String.format("%s. Defrosting.", r.getMessage()));
            ctClass.defrost();
        }
    }
}
