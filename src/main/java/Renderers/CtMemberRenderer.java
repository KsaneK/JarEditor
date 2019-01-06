package Renderers;

import javassist.CtMember;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CtMemberRenderer extends JLabel implements ListCellRenderer<CtMember> {
    @Override
    public Component getListCellRendererComponent(JList<? extends CtMember> list, CtMember value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getName());
        setOpaque(true);
        if (isSelected) setBackground(Color.GRAY);
        else setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setBorder(new EmptyBorder(5, 10, 5, 10));
        return this;
    }
}
