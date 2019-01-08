import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

class LabelPanel extends JPanel {
    private JPanel fieldPanel;
    private JPanel methodPanel;
    private Map<String, JLabel> fieldLabels;
    private Map<String, JLabel> methodLabels;
    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    LabelPanel() {
        fieldLabels = new LinkedHashMap<>();
        methodLabels = new LinkedHashMap<>();
        setLayout(new GridLayout(1, 0));
        fieldPanel = new JPanel();
        methodPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.Y_AXIS));

        initLabels();

        add(fieldPanel, 0);
        add(methodPanel, 1);
    }

    void setFieldLabels(CtField field) {
        try {
            int modifiers = field.getModifiers();
            fieldLabels.get("type").setText(String.format("Type: %s", field.getType().getName()));
            fieldLabels.get("access").setText(String.format("Access: %s", getAccess(modifiers)));
            fieldLabels.get("static").setText(String.format("IsStatic: %b", Modifier.isStatic(modifiers)));
            fieldLabels.get("final").setText(String.format("IsFinal: %b", Modifier.isFinal(modifiers)));
        } catch (NotFoundException e) {
            logger.warning(String.format("Couldn't set field labels: %s", e.getMessage()));
        }
    }

    void setMethodLabels(CtMethod method) {
        try {
            int modifiers = method.getModifiers();
            methodLabels.get("return").setText(String.format("Return: %s", method.getReturnType().getName()));
            methodLabels.get("access").setText(String.format("Access: %s", getAccess(modifiers)));
            methodLabels.get("abstract").setText(String.format("isAbstract: %b", Modifier.isAbstract(modifiers)));
            methodLabels.get("static").setText(String.format("isStatic: %b", Modifier.isStatic(modifiers)));
        } catch (NotFoundException e) {

        }
    }

    void resetFieldLabels() {
        fieldLabels.get("type").setText("Return: ");
        fieldLabels.get("access").setText("Access: ");
        fieldLabels.get("static").setText("IsStatic: ");
        fieldLabels.get("final").setText("IsFinal: ");
    }

    void resetMethodLabels() {
        methodLabels.get("return").setText("Return: ");
        methodLabels.get("access").setText("Access: ");
        methodLabels.get("static").setText("IsStatic: ");
        methodLabels.get("abstract").setText("IsAbstract: ");
    }

    private String getAccess(int modifiers) {
        String access = "unknown";
        if (Modifier.isPublic(modifiers)) access = "Public";
        else if (Modifier.isPrivate(modifiers)) access = "Private";
        else if (Modifier.isProtected(modifiers)) access = "Protected";
        else if (Modifier.isPackage(modifiers)) access = "Package-private";
        return access;
    }

    private void initLabels() {
        fieldLabels.put("FIELD", new JLabel("Field modifiers"));
        fieldLabels.put("type", new JLabel("Type: "));
        fieldLabels.put("access", new JLabel("Access: "));
        fieldLabels.put("static", new JLabel("IsStatic: "));
        fieldLabels.put("final", new JLabel("IsFinal: "));

        methodLabels.put("METHOD", new JLabel("Method modifiers"));
        methodLabels.put("return", new JLabel("Return: "));
        methodLabels.put("access", new JLabel("Access: "));
        methodLabels.put("static", new JLabel("isStatic: "));
        methodLabels.put("abstract", new JLabel("isAbstract: "));

        for (JLabel label : fieldLabels.values()) fieldPanel.add(label);
        for (JLabel label : methodLabels.values()) methodPanel.add(label);
    }
}
