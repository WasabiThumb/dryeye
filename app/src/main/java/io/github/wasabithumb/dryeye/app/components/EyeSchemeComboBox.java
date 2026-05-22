package io.github.wasabithumb.dryeye.app.components;

import io.github.wasabithumb.dryeye.app.context.ApplicationContext;
import io.github.wasabithumb.dryeye.face.eye.EyeScheme;
import io.github.wasabithumb.dryeye.face.eye.EyeSchemes;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class EyeSchemeComboBox extends JComboBox<EyeScheme> {

    public EyeSchemeComboBox(ApplicationContext ctx) {
        super(new Model());
        this.setRenderer(Renderer.INSTANCE);
        this.setSelectedItem(ctx.getEyeScheme());
        this.addActionListener(_ -> {
            Object selection = this.getSelectedItem();
            if (selection instanceof EyeScheme qual) ctx.setEyeScheme(qual);
        });
    }

    //

    private static final class Renderer
            extends JLabel
            implements ListCellRenderer<EyeScheme>
    {

        static final Renderer INSTANCE = new Renderer();

        //

        private Renderer() {
            this.setOpaque(true);
        }

        //

        @Override
        public Component getListCellRendererComponent(JList<? extends EyeScheme> list, EyeScheme value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.name().toString());

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            return this;
        }

    }

    private static final class Model
            extends AbstractListModel<EyeScheme>
            implements ComboBoxModel<EyeScheme>
    {

        private static final List<EyeScheme> SCHEMES = EyeSchemes.all();

        //

        private int selection = 0;

        //

        @Override
        public EyeScheme getSelectedItem() {
            return SCHEMES.get(this.selection);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            //noinspection SuspiciousMethodCalls
            int idx = SCHEMES.indexOf(anItem);
            if (idx == -1) throw new IllegalArgumentException();
            this.selection = idx;
        }

        @Override
        public int getSize() {
            return SCHEMES.size();
        }

        @Override
        public EyeScheme getElementAt(int index) {
            return SCHEMES.get(index);
        }

    }

}
