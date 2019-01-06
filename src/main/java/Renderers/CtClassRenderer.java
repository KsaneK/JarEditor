package Renderers;

import javassist.CtClass;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CtClassRenderer extends JLabel implements ListCellRenderer<CtClass> {
    @Override
    public Component getListCellRendererComponent(JList<? extends CtClass> list, CtClass value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getName());
        setOpaque(true);
        if (isSelected) setBackground(Color.GRAY);
        else setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setBorder(new EmptyBorder(5, 10, 5, 10));
        return this;
    }
}
