package mainpanel;

import renderers.CtClassRenderer;
import renderers.CtMemberRenderer;
import javassist.*;
import utils.LoggerFormatter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class ClassPanel extends JPanel {
    // Lists and models
    private JList<CtField> fieldList;
    private JList<CtBehavior> methodList;
    private JList<CtClass> parameterList;
    private DefaultListModel<CtBehavior> methodModel;
    private DefaultListModel<CtField> fieldModel;
    // Selected class
    private CtClass selectedClass = null;
    private CtBehavior selectedBehavior = null;
    private CtField selectedField = null;
    // Map of buttons
    private HashMap<JarEditButtons, JButton> clsBtns;
    // Panel with Labels
    private LabelPanel labelPanel;

    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());

    public ClassPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        initClassEditButtons();
        initFieldAccessButtons();
        initMethodAccessButtons();

        initLists();
        labelPanel = new LabelPanel();
        add(labelPanel);
    }

    public void setCtClass(CtClass selectedClass) {
        logger.info(String.format("Selected class %s.", selectedClass.getName()));
        this.selectedClass = selectedClass;
        methodList.clearSelection();
        DefaultListModel<CtClass> parameterModel = new DefaultListModel<>();
        fieldModel = new DefaultListModel<>();
        methodModel = new DefaultListModel<>();
        for (CtConstructor constructor : selectedClass.getDeclaredConstructors()) {
            methodModel.addElement(constructor);
            logger.info("Added constructor.");
        }
        for (CtMethod method : selectedClass.getDeclaredMethods()) {
            methodModel.addElement(method);
            logger.info(String.format("Added method %s.", method.getName()));
        }
        for (CtField field : selectedClass.getDeclaredFields()) {
            fieldModel.addElement(field);
            logger.info(String.format("Added field %s.", field.getName()));
        }
        fieldList.setModel(fieldModel);
        parameterList.setModel(parameterModel);
        methodList.setModel(methodModel);
        setButtonEnabled(JarEditButtons.ADD_METHOD, true);
        setButtonEnabled(JarEditButtons.ADD_FIELD, true);
    }

    private void initLists() {
        logger.info("Init lists.");
        JScrollPane fieldListScrollPane = generateList("Fields", new CtMemberRenderer());
        JScrollPane methodListScrollPane = generateList("Methods", new CtMemberRenderer());
        JScrollPane parameterListScrollPane = generateList("Parameters", new CtClassRenderer());
        fieldList = (JList<CtField>) fieldListScrollPane.getViewport().getView();
        methodList = (JList<CtBehavior>) methodListScrollPane.getViewport().getView();
        parameterList = (JList<CtClass>) parameterListScrollPane.getViewport().getView();
        fieldSelectListener(fieldList);
        methodSelectListener(methodList);
        JPanel listPanel = new JPanel(new FlowLayout());
        listPanel.add(fieldListScrollPane);
        listPanel.add(methodListScrollPane);
        listPanel.add(parameterListScrollPane);
        add(listPanel);
    }

    private JScrollPane generateList(String title, ListCellRenderer renderer) {
        logger.info(String.format("Generating new list: %s.", title));
        JList<CtBehavior> newList = new JList<>();
        newList.setCellRenderer(renderer);
        newList.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 2), title));
        newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(newList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        return scrollPane;
    }

    private void methodSelectListener(JList list) {
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            logger.info("Method selection changed.");
            selectedBehavior = (CtBehavior) list.getSelectedValue();
            setButtonEnabled(JarEditButtons.EDIT_METHOD, (selectedBehavior != null));
            setButtonEnabled(JarEditButtons.DELETE_METHOD, (selectedBehavior != null));
            setButtonEnabled(JarEditButtons.SET_METHOD_PUBLIC, (selectedBehavior != null));
            setButtonEnabled(JarEditButtons.SET_METHOD_PROTECTED, (selectedBehavior != null));
            setButtonEnabled(JarEditButtons.SET_METHOD_PRIVATE, (selectedBehavior != null));
            setButtonEnabled(JarEditButtons.SET_METHOD_STATIC, (selectedBehavior != null));
            if (selectedBehavior == null) return;
            try {
                labelPanel.setMethodLabels((CtMethod) selectedBehavior);
            } catch (ClassCastException e1) {
                labelPanel.resetMethodLabels();
            }
            try {
                DefaultListModel<CtClass> paramModel = new DefaultListModel<>();
                for (CtClass param : selectedBehavior.getParameterTypes()) {
                    logger.info(String.format("Added parameter %s.", param.getName()));
                    paramModel.addElement(param);
                }
                parameterList.setModel(paramModel);
            } catch (NotFoundException e1) {
                logger.warning("Parameter type not found.");
                e1.printStackTrace();
            }
        });
    }

    private void fieldSelectListener(JList<CtField> list) {
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            logger.info("Field selection changed.");
            selectedField = list.getSelectedValue();
            setButtonEnabled(JarEditButtons.DELETE_FIELD, (selectedField != null));
            setButtonEnabled(JarEditButtons.SET_FIELD_PUBLIC, (selectedField != null));
            setButtonEnabled(JarEditButtons.SET_FIELD_PROTECTED, (selectedField != null));
            setButtonEnabled(JarEditButtons.SET_FIELD_PRIVATE, (selectedField != null));
            setButtonEnabled(JarEditButtons.SET_FIELD_STATIC, (selectedField != null));
            if (selectedField == null) {
                logger.info("Selected field is now null.");
                labelPanel.resetFieldLabels();
            } else {
                logger.info(String.format("Selected field is now %s.", selectedField.getName()));
                labelPanel.setFieldLabels(selectedField);
            }
        });
    }

    private void setButtonEnabled(JarEditButtons key, boolean b) {
        logger.info(String.format("Button %s - setEnabled %b.", key, b));
        clsBtns.get(key).setEnabled(b);
    }

    private void initClassEditButtons() {
        logger.info("Init buttons.");
        clsBtns = new LinkedHashMap<>();
        clsBtns.put(JarEditButtons.ADD_FIELD, new JButton("Add Field"));
        clsBtns.put(JarEditButtons.DELETE_FIELD, new JButton("Delete Field"));
        clsBtns.put(JarEditButtons.ADD_METHOD, new JButton("Add Method"));
        clsBtns.put(JarEditButtons.DELETE_METHOD, new JButton("Delete Method"));
        clsBtns.put(JarEditButtons.EDIT_METHOD, new JButton("Edit Method"));
        JPanel btnPanel = new JPanel(new FlowLayout());
        for (JButton button : clsBtns.values()) {
            button.setEnabled(false);
            btnPanel.add(button);
        }

        addButtonListeners();
        add(btnPanel);
    }

    private void initFieldAccessButtons() {
        JPanel fieldAccessPanel = new JPanel(new FlowLayout());
        fieldAccessPanel.add(new JLabel("Field "));
        JButton accessPublicBtn = createButton(JarEditButtons.SET_FIELD_PUBLIC, "Set Public");
        fieldAccessPanel.add(accessPublicBtn);
        JButton accessProtectedBtn = createButton(JarEditButtons.SET_FIELD_PROTECTED, "Set Protected");
        fieldAccessPanel.add(accessProtectedBtn);
        JButton accessPrivateBtn = createButton(JarEditButtons.SET_FIELD_PRIVATE, "Set Private");
        fieldAccessPanel.add(accessPrivateBtn);
        JButton setStaticBtn = createButton(JarEditButtons.SET_FIELD_STATIC, "Toggle static");
        fieldAccessPanel.add(setStaticBtn);
        accessPublicBtn.addActionListener(e -> setModifier(true, Modifier.PUBLIC));
        accessProtectedBtn.addActionListener(e -> setModifier(true, Modifier.PROTECTED));
        accessPrivateBtn.addActionListener(e -> setModifier(true, Modifier.PRIVATE));
        setStaticBtn.addActionListener(e -> setModifier(true, Modifier.STATIC));
        add(fieldAccessPanel);
    }

    private void initMethodAccessButtons() {
        JPanel methodAccesPanel = new JPanel(new FlowLayout());
        methodAccesPanel.add(new JLabel("Method "));
        JButton accessPublicBtn = createButton(JarEditButtons.SET_METHOD_PUBLIC, "Set Public");
        methodAccesPanel.add(accessPublicBtn);
        JButton accessProtectedBtn = createButton(JarEditButtons.SET_METHOD_PROTECTED, "Set Protected");
        methodAccesPanel.add(accessProtectedBtn);
        JButton accessPrivateBtn = createButton(JarEditButtons.SET_METHOD_PRIVATE, "Set Private");
        methodAccesPanel.add(accessPrivateBtn);
        JButton setStaticBtn = createButton(JarEditButtons.SET_METHOD_STATIC, "Toggle static");
        methodAccesPanel.add(setStaticBtn);
        accessPublicBtn.addActionListener(e -> setModifier(false, Modifier.PUBLIC));
        accessProtectedBtn.addActionListener(e -> setModifier(false, Modifier.PROTECTED));
        accessPrivateBtn.addActionListener(e -> setModifier(false, Modifier.PRIVATE));
        setStaticBtn.addActionListener(e -> setModifier(false, Modifier.STATIC));
        add(methodAccesPanel);
    }

    private JButton createButton(JarEditButtons button, String text) {
        clsBtns.put(button, new JButton(text));
        clsBtns.get(button).setEnabled(false);
        return clsBtns.get(button);
    }

    private void deleteSelectedMethod() {
        try {
            try {
                selectedClass.removeMethod((CtMethod) selectedBehavior);
            } catch (ClassCastException e) {
                selectedClass.removeConstructor((CtConstructor) selectedBehavior);
            }
            logger.info(String.format("Successfully removed method %s", selectedBehavior.getName()));
            methodModel.removeElement(selectedBehavior);
            selectedBehavior = null;
        } catch (NotFoundException e1) {
            logger.warning(String.format("Couldn't delete method %s.", selectedBehavior.getName()));
            JOptionPane.showMessageDialog(null, "Can't delete selected method");
        }
    }

    private void addFieldToMethod() {
        String field = JOptionPane.showInputDialog(null,
                "Enter field declaration (for example: \"private int count;\").",
                "Field declaration",
                JOptionPane.PLAIN_MESSAGE);
        if (field == null) return;
        try {
            CtField ctField = CtField.make(field, selectedClass);
            selectedClass.addField(ctField);
            fieldModel.addElement(ctField);
            logger.info(String.format("Successfully added field %s.", field));
        } catch (CannotCompileException e1) {
            logger.warning(String.format("Couldn't create field %s.", field));
            JOptionPane.showMessageDialog(null, e1.getReason());
        }
    }

    private void deleteFieldFromMethod() {
        try {
            selectedClass.removeField(selectedField);
            logger.info(String.format("Successfully removed field %s.", selectedField));
            fieldModel.removeElement(selectedField);
            selectedField = null;
        } catch (NotFoundException e1) {
            logger.warning(String.format("Couldn't delete field %s.", selectedField.getName()));
            JOptionPane.showMessageDialog(null, "Can't delete selected field");
        }
    }

    private void addButtonListeners() {
        logger.info("Adding listener for New method button.");
        clsBtns.get(JarEditButtons.ADD_METHOD).addActionListener(e ->
                new ClassEditor("Creating new method", selectedClass, methodModel));
        logger.info("Adding listener for Edit method button.");
        clsBtns.get(JarEditButtons.EDIT_METHOD).addActionListener(e ->
                new ClassEditor(selectedBehavior.getName(), selectedClass, selectedBehavior, methodModel));
        logger.info("Adding listener for Delete method button.");
        clsBtns.get(JarEditButtons.DELETE_METHOD).addActionListener(e -> {
                deleteSelectedMethod();
        });
        logger.info("Adding listener for Add field button.");
        clsBtns.get(JarEditButtons.ADD_FIELD).addActionListener(e -> {
                addFieldToMethod();
        });
        logger.info("Adding listener for Delete field button.");
        clsBtns.get(JarEditButtons.DELETE_FIELD).addActionListener(e -> {
                deleteFieldFromMethod();
        });
    }

    private void setModifier(boolean isField, int modifier) {
        logger.info(String.format("Setting modifier for %s.", isField ? selectedField.getName() : selectedBehavior.getName()));
        int currentMod = (isField) ? selectedField.getModifiers() : selectedBehavior.getModifiers();
        if ((modifier & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) > 0) {
            modifier = currentMod & ~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE) | modifier;
        } else if (modifier == Modifier.STATIC) {
            if (Modifier.isStatic(currentMod)) modifier = currentMod & (~modifier);
            else modifier = currentMod | modifier;
        }
        if (isField) {
            selectedField.setModifiers(modifier);
            labelPanel.setFieldLabels(selectedField);
        } else {
            selectedBehavior.setModifiers(modifier);
            try {
                labelPanel.setMethodLabels((CtMethod) selectedBehavior);
            } catch (ClassCastException e) {
                labelPanel.resetMethodLabels();
            }
        }
    }
}
