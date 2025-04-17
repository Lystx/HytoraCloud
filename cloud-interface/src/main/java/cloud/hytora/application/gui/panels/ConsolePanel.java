package cloud.hytora.application.gui.panels;

import cloud.hytora.application.elements.JComponentOutputStream;

import javax.swing.*;
import java.io.PrintStream;
import java.util.UUID;

public class ConsolePanel extends JPanel {

    public ConsolePanel() {
        setLayout(null);

        JTextArea console = new JTextArea();
        console.setEditable(false);

        JComponentOutputStream consoleOutputStream = new JComponentOutputStream(console, new JComponentOutputStream.JComponentHandler() {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public void setText(JComponent swingComponent, String text) {
                sb.delete(0, sb.length());
                append(swingComponent, text);
            }

            @Override
            public void replaceRange(JComponent swingComponent, String text, int start, int end) {
                sb.replace(start, end, text);
                redrawTextOf(swingComponent);
            }

            @Override
            public void append(JComponent swingComponent, String text) {
                sb.append(text);
                redrawTextOf(swingComponent);
            }

            private void redrawTextOf(JComponent swingComponent) {
                if (swingComponent instanceof JTextArea) {
                    JTextArea area = (JTextArea)swingComponent;
                    area.setText(sb.toString());
                } else {
                    ((JLabel) swingComponent).setText("<html><pre>" + sb.toString() + "</pre></html>");
                }
            }
        });

        PrintStream con = new PrintStream(consoleOutputStream);
        System.setOut(con);
        System.setErr(con);

        JScrollPane sp = new JScrollPane(
                console,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );


        sp.setBounds(50, 20, 900, 500);
        add(sp);


        for (int i = 0; i < 500; i++) {
            System.out.println(UUID.randomUUID().toString() + UUID.randomUUID().toString());
        }

        JTextField commandInput = new JTextField();
        commandInput.setBounds(50, 520, 900, 30);

        add(commandInput);
    }
}
