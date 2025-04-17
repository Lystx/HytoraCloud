/*
 * Created by JFormDesigner on Tue Apr 15 18:23:55 CEST 2025
 */

package cloud.hytora.application.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Lystx
 */
public class DevPanel extends JPanel {
    public DevPanel() {
        initComponents();
    }


    private void backButtonClicked(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        this.setBounds(65, 200, 850, 350);
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - julheeg
        HytoraCloud = new JTabbedPane();
        scrollPane1 = new JScrollPane();
        panel1 = new JPanel();
        scrollPane2 = new JScrollPane();
        list1 = new JList();

        //======== HytoraCloud ========
        {

            //======== scrollPane1 ========
            {

                //======== panel1 ========
                {
                    panel1.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax. swing
                    . border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frmDes\u0069gner \u0045valua\u0074ion", javax. swing. border. TitledBorder
                    . CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .Font ("D\u0069alog" ,java .
                    awt .Font .BOLD ,12 ), java. awt. Color. red) ,panel1. getBorder( )) )
                    ; panel1. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override public void propertyChange (java .beans .PropertyChangeEvent e
                    ) {if ("\u0062order" .equals (e .getPropertyName () )) throw new RuntimeException( ); }} )
                    ;
                    panel1.setLayout(new GridLayout(15, 15));

                    //======== scrollPane2 ========
                    {
                        scrollPane2.setViewportView(list1);
                    }
                    panel1.add(scrollPane2);
                }
                scrollPane1.setViewportView(panel1);
            }
            HytoraCloud.addTab("text", scrollPane1);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - julheeg
    private JTabbedPane HytoraCloud;
    private JScrollPane scrollPane1;
    private JPanel panel1;
    private JScrollPane scrollPane2;
    private JList list1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
