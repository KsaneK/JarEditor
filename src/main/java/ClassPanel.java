import Renderers.CtClassRenderer;
import Renderers.CtMemberRenderer;
import javassist.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.logging.Logger;

class ClassPanel extends JPanel {
    private JarTree jarTree;
    private CtClass selectedClass = null;
    private CtBehavior selectedMethod = null;
    private CtField selectedField = null;
    private JList<CtField> fieldList;
    private JList<CtBehavior> methodList;
    private JList<CtClass> parameterList;
    private HashMap<JarEditButtons, JButton> clsBtns;
    private LabelPanel labelPanel;

    private final Logger logger = Logger.getLogger(LoggerFormatter.class.getName());
    private DefaultListModel<CtBehavior> methodModel;
    private DefaultListModel<CtField> fieldModel;

    ClassPanel(JarTree jarTree) {
        super();
        this.jarTree = jarTree;
        BorderLayout layout = new BorderLayout();
        layout.setHgap(10);
        layout.setVgap(10);
        setLayout(layout);

        initButtons();
        initLists();
        labelPanel = new LabelPanel();
        add(labelPanel, BorderLayout.SOUTH);
    }

    void setCtClass(CtClass selectedClass) {
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

    private void methodSelectListener(JList list) {
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            logger.info("Method selection changed.");
            selectedMethod = (CtBehavior) list.getSelectedValue();
            if(selectedMethod != null)
                logger.info(String.format("Selected method is now %s.", selectedMethod.getName()));
            else {
                logger.info("Selection method is now null.");
                setButtonEnabled(JarEditButtons.EDIT_METHOD, false);
                setButtonEnabled(JarEditButtons.DELETE_METHOD, false);
                return;
            }
            try {
                labelPanel.setMethodLabels((CtMethod) selectedMethod);
            } catch (ClassCastException ce) {
                logger.info("Selected constructor. Resetting labels.");
                labelPanel.resetMethodLabels();
            }
            setButtonEnabled(JarEditButtons.EDIT_METHOD, true);
            setButtonEnabled(JarEditButtons.DELETE_METHOD, true);
            try {
                DefaultListModel<CtClass> paramModel = new DefaultListModel<>();
                for (CtClass param : selectedMethod.getParameterTypes()) {
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
            if (selectedField == null) {
                logger.info("Selected field is now null.");
                setButtonEnabled(JarEditButtons.DELETE_FIELD, false);
                labelPanel.resetFieldLabels();
            } else {
                logger.info(String.format("Selected field is now %s.", selectedField.getName()));
                setButtonEnabled(JarEditButtons.DELETE_FIELD, true);
                labelPanel.setFieldLabels(selectedField);
            }
        });
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
        scrollPane.setPreferredSize(new Dimension(200, 200));
        return scrollPane;
    }

    private void setButtonEnabled(JarEditButtons key, boolean b) {
        logger.info(String.format("Button %s - setEnabled %b.", key, b));
        clsBtns.get(key).setEnabled(b);
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
        add(listPanel, BorderLayout.CENTER);
    }

    private void initButtons() {
        logger.info("Init buttons.");
        JarEditButtons[] buttons = {JarEditButtons.ADD_FIELD, JarEditButtons.DELETE_FIELD, JarEditButtons.ADD_METHOD,
                                    JarEditButtons.DELETE_METHOD, JarEditButtons.EDIT_METHOD};
        clsBtns = new HashMap<>();
        clsBtns.put(JarEditButtons.ADD_FIELD, new JButton("Add Field"));
        clsBtns.put(JarEditButtons.DELETE_FIELD, new JButton("Delete Field"));
        clsBtns.put(JarEditButtons.ADD_METHOD, new JButton("Add Method"));
        clsBtns.put(JarEditButtons.DELETE_METHOD, new JButton("Delete Method"));
        clsBtns.put(JarEditButtons.EDIT_METHOD, new JButton("Edit Method"));
        JPanel btnPanel = new JPanel();
        for (JarEditButtons button : buttons) {
            clsBtns.get(button).setEnabled(false);
            btnPanel.add(clsBtns.get(button));
        }
        addButtonListeners();
        add(btnPanel, BorderLayout.NORTH);
    }

    private void deleteSelectedMethod() {
        try {
            String clsName = selectedClass.getName().substring(selectedClass.getName().lastIndexOf(".") + 1);
            if (selectedMethod.getName().equals(clsName))
                selectedClass.removeConstructor((CtConstructor) selectedMethod);
            else selectedClass.removeMethod((CtMethod) selectedMethod);
            logger.info(String.format("Successfully removed method %s", selectedMethod.getName()));
            methodModel.removeElement(selectedMethod);
            selectedMethod = null;
        } catch (NotFoundException e1) {
            logger.warning(String.format("Couldn't delete method %s.", selectedMethod.getName()));
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
                new ClassEditor(selectedMethod.getName(), selectedClass, selectedMethod, methodModel));
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
}
