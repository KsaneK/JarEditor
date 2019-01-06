import javassist.*;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

class ClassEditor extends JFrame {
    private DefaultListModel<CtBehavior> model;
    private JButton before;
    private JButton overwrite;
    private JButton after;
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
        before = new JButton("Prepend Method");
        overwrite = new JButton((ctBehavior == null) ? "Create Method" : "Overwrite Method");
        after = new JButton("Append Method");
        if (ctBehavior == null) {
            before.setVisible(false);
            after.setVisible(false);
        } else {
            isConstructor.setVisible(false);
        }
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(before);
        bottomPanel.add(overwrite);
        bottomPanel.add(after);
        addButtonListeners();
        add(bottomPanel);
    }

    private void addButtonListeners() {
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
        before.addActionListener(e -> {
            logger.info(String.format("Adding code at the beggining of method %s.", ctBehavior));
            addToMethod(true);
        });
        after.addActionListener(e -> {
            logger.info(String.format("Adding code at the end of method %s.", ctBehavior));
            addToMethod(false);
        });
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

    private void addToMethod(boolean beginning) {
        String text = textPane.getText();
        try {
            if (beginning) {
                ctBehavior.insertBefore(text);
                JOptionPane.showMessageDialog(null, "Added code at the beginning of the method.");
            }
            else {
                ctBehavior.insertAfter(text);
                JOptionPane.showMessageDialog(null, "Added code at the end of the method.");
            }
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
