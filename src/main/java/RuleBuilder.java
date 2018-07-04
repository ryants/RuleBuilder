import javax.swing.*; // For graphical interface tools


import java.awt.*; // For borderlayout
import java.net.*; //For URL image loading from Jar files
import java.awt.event.*; // For mouse interactions
import java.io.*;

import javax.swing.border.EtchedBorder;


public class RuleBuilder extends JFrame {
    String version = "RuleBuilder v2.0.2";

    BNGWidgetPanel editor_panel = new BNGWidgetPanel(this);
    BNGModelStub model_stub = new BNGModelStub(this);

    public JLabel status_bar = new JLabel();
    public JTextField textbox = new JTextField(15);
    public String mode = "manipulate";
    public JLabel error_bar = new JLabel(" ");

    String current_path = "."; // Remember the last directory the user used to save a jpg


    class FrameListener implements WindowListener {
        FrameListener() {
        }

        public void windowActivated(WindowEvent e) {
            // TODO Auto-generated method stub

        }

        public void windowClosed(WindowEvent e) {

        }

        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }

        public void windowDeactivated(WindowEvent e) {
            // TODO Auto-generated method stub

        }

        public void windowDeiconified(WindowEvent e) {
            // TODO Auto-generated method stub

        }

        public void windowIconified(WindowEvent e) {
            // TODO Auto-generated method stub

        }

        public void windowOpened(WindowEvent e) {
            // TODO Auto-generated method stub

        }

    }

    class TextboxListener implements KeyListener {
        TextboxListener() {
        }

        public void keyPressed(KeyEvent e) {
            //System.out.println("Key Pressed");
            model_stub.parseBNGL(textbox.getText());
        }

        public void keyTyped(KeyEvent e) {
            //System.out.println("Key Typed");
            model_stub.parseBNGL(textbox.getText());
        }

        public void keyReleased(KeyEvent e) {
            model_stub.parseBNGL(textbox.getText());
        }
    }

    class ToolbarAction extends AbstractAction {

        private BNGWidgetPanel panel;

        public ToolbarAction(String text, Icon icon, String description, char accelerator, BNGWidgetPanel panel) {

            super(text, icon);
            this.panel = panel;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            putValue(SHORT_DESCRIPTION, description);

        }

        public void actionPerformed(ActionEvent e) {
            if (getValue(NAME).equals("save")) {
                try {
                    JFileChooser fc = new JFileChooser();

                    // Start in current directory
                    fc.setCurrentDirectory(new File(current_path));

                    // Set filter for Java source files.
                    fc.setFileFilter(new ImageFilter());

                    // Set to a default name for save.

                    boolean bad_extension = false;

                    do {
                        fc.setSelectedFile(new File(current_path));

                        int returnVal = fc.showSaveDialog(editor_panel);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();

                            current_path = file.getPath();
                            if (!editor_panel.toImageFile(file)) {
                                JOptionPane.showMessageDialog(null,
                                        file.getName() + " does not have a supported file extension.");
                                bad_extension = true;
                            } else {
                                if (file.exists()) {
                                    int response = JOptionPane.showConfirmDialog(null,
                                            "Overwrite existing file?", "Confirm Overwrite",
                                            JOptionPane.OK_CANCEL_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);
                                    if (response == JOptionPane.CANCEL_OPTION) return;
                                }
                                bad_extension = false;
                            }
                        } else {
                            // FileChooser cancelled by the user
                            return;
                        }
                    } while (bad_extension);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (getValue(NAME).equals("erase")) {
                boolean widget_removed = panel.removeSelectedWidget();

                // if the last element was removed from the canvas by the user
                // then clear the bngl_string, bngl_string can't do it because there
                // are other ways all the widgets could be removed

                if (editor_panel.operators.isEmpty() && editor_panel.containers.isEmpty() && widget_removed)
                    textbox.setText("");
            } else {
                editor_panel.clearPreviousSelection();

                if (getValue(NAME).equals("manipulate")) {
                    setMode("manipulate");
                    status_bar.setText("Object Manipulation Mode");
                } else if (getValue(NAME).equals("add bond")) {
                    setMode("add bond");
                    status_bar.setText("Add Bond (constructs a bond between two sites)");
                } else if (getValue(NAME).equals("add map")) {
                    setMode("add maps");
                    status_bar.setText("Add Mapping (defines a mapping between objects on either side of a rule)");
                } else if (getValue(NAME).equals("add sites (arbitrary bond state)")) {
                    setMode("add sites (arbitrary bond state)");
                    status_bar.setText("Add Site (arbitrary bond state)");
                } else if (getValue(NAME).equals("add sites (unbound)")) {
                    setMode("add sites (unbound)");
                    status_bar.setText("Add Site (unbound)");
                } else if (getValue(NAME).equals("add sites (unknown binding partner)")) {
                    setMode("add sites (unknown binding partner)");
                    status_bar.setText("Add Site (bound, unspecified binding partner)");
                } else if (getValue(NAME).equals("add molecule")) {
                    setMode("add molecule");
                    status_bar.setText("Add Molecule");
                } else if (getValue(NAME).equals("add operator (plus)")) {
                    setMode("add operator (plus)");
                    status_bar.setText("Add Plus Sign (separates molecules that are part of distinct species)");
                } else if (getValue(NAME).equals("add operator (arrow)")) {
                    setMode("add operator (arrow)");
                    status_bar.setText("Add Single Arrow Separator (defines a unidirectional rule)");
                } else if (getValue(NAME).equals("add operator (double arrow)")) {
                    setMode("add operator (double arrow)");
                    status_bar.setText("Add Double Arrow Separator (defines a bidirectional rule)");
                } else if (getValue(NAME).equals("about")) {
                    
                    String about_title = "About";
                    String about_text = "<html><body><H1><font color=#193d77>RuleBuilder 2</font></H1><p>Version:" + version + "</p><p>Authors: G. Matthew Fricke, Ryan Suderman, and William S. Hlavacek</p><p>Copyright (c) 2017, Los Alamos National Security, LLC  All rights reserved.</p><p>Github Repository: <a href=\"https://github.com/RuleWorld/RuleBuilder\">https://github.com/RuleWorld/RuleBuilder</a></p> <H3>License</H3><p style='width: 600px;'>This software was produced under U.S. Government contract DE-AC52-06NA25396 for Los Alamos National Laboratory (LANL), which is operated by Los Alamos National Security, LLC for the U.S. Department of Energy. The U.S. Government has rights to use, reproduce, and distribute this software.  NEITHER THE GOVERNMENT NOR LOS ALAMOS NATIONAL SECURITY, LLC MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE.  If software is modified to produce derivative works, such modified software should be clearly marked, so as not to confuse it with the version available from LANL. Additionally, redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 1.      Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 2.      Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 3.      Neither the name of Los Alamos National Security, LLC, Los Alamos National Laboratory, LANL, the U.S. Government, nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY LOS ALAMOS NATIONAL SECURITY, LLC AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LOS ALAMOS NATIONAL SECURITY, LLC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p></body></HTML>";
                    JOptionPane.showMessageDialog(editor_panel, about_text, about_title, JOptionPane.INFORMATION_MESSAGE);
                }
            }


            model_stub.writeBNGL();

            panel.setCursorAccordingToMode();
        }

    }

    public RuleBuilder() {
        init();
    }

    public void init() {
        setSize(600, 400);
        setBackground(Color.white);
        addWindowListener(new FrameListener());

        setTitle(version);

        // Create the editor panel
        JToolBar toolbar = createToolbar();

        status_bar.setText("Object Manipulation Mode");
        status_bar.setBackground(Color.white);

        textbox.addKeyListener(new TextboxListener());

        error_bar.setForeground(Color.red);

        JPanel drawing_panel = new JPanel();
        drawing_panel.setBackground(Color.white);
        drawing_panel.setLayout(new BorderLayout());
        drawing_panel.add(toolbar, BorderLayout.NORTH);
        drawing_panel.add(editor_panel, BorderLayout.CENTER);
        drawing_panel.add(status_bar, BorderLayout.SOUTH);

        error_bar.setBackground(Color.white);

        JPanel text_panel = new JPanel();
        text_panel.setBackground(Color.white);
        text_panel.setLayout(new BorderLayout());
        text_panel.add(new JLabel("BNGL String:"), BorderLayout.WEST);
        text_panel.add(textbox, BorderLayout.CENTER);
        text_panel.add(error_bar, BorderLayout.SOUTH);

        text_panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        //getContentPane().add(drawing_panel, BorderLayout.CENTER);
        //getContentPane().add(text_panel, BorderLayout.SOUTH);

        add(drawing_panel, BorderLayout.CENTER);
        add(text_panel, BorderLayout.SOUTH);

        setVisible(true);

        //model_stub.parseBNGL("Lig(test!0!1,l).Lyn(test!0,SH2,U,SH2!1).Lig(l,l).Lyn(U,SH2)");
        model_stub.writeBNGL();
    }

    public JToolBar createToolbar() {
        ClassLoader cldr = this.getClass().getClassLoader();

        URL erase_gif_url = this.getClass().getResource("erase.gif");
        URL manip_gif_url = cldr.getResource("manip.gif");
        URL add_edge_gif_url = cldr.getResource("add_edge.gif");
        URL add_map_gif_url = cldr.getResource("add_map.gif");
        URL unspecified_component_gif_url = cldr.getResource("unspecified_binding_component.gif");
        URL bound_component_gif_url = cldr.getResource("bound_component.gif");
        URL unbound_component_gif_url = cldr.getResource("unbound_component.gif");
        URL container_gif_url = cldr.getResource("container.gif");

        URL double_arrow_gif_url = cldr.getResource("double_arrow.gif");
        URL arrow_gif_url = cldr.getResource("arrow.gif");
        URL plus_gif_url = cldr.getResource("plus.gif");
        URL save_gif_url = cldr.getResource("save.gif");
        URL about_gif_url = cldr.getResource("about.gif");

//	  Create a set of actions to use in both the menu and toolbar.
        ImageIcon manip_icon = new ImageIcon(manip_gif_url);
        ToolbarAction rule_manip_action = new ToolbarAction("manipulate", manip_icon, "manipulate objects", 'M', editor_panel);

        ToolbarAction erase_action = new ToolbarAction("erase", new ImageIcon(erase_gif_url), "erase selection", 'E', editor_panel);

        ToolbarAction save_action = new ToolbarAction("save", new ImageIcon(save_gif_url), "export to image", 'S', editor_panel);

        ImageIcon add_edge_icon = new ImageIcon(add_edge_gif_url);
        ToolbarAction rule_add_edge_action = new ToolbarAction("add bond", add_edge_icon, "Bond", 'A', editor_panel);

        ImageIcon add_map_icon = new ImageIcon(add_map_gif_url);
        ToolbarAction rule_add_map_action = new ToolbarAction("add map", add_map_icon, "Map", 'M', editor_panel);

        ImageIcon add_container_icon = new ImageIcon(container_gif_url);
        ToolbarAction rule_add_container_action = new ToolbarAction("add molecule", add_container_icon, "molecule", 'N', editor_panel);

        ImageIcon add_unspecified_component_icon = new ImageIcon(unspecified_component_gif_url);
        ToolbarAction rule_add_unspecified_component_action = new ToolbarAction("add sites (arbitrary bond state)", add_unspecified_component_icon, "site with arbitrary bond state", 'C', editor_panel);

        ImageIcon add_bound_component_icon = new ImageIcon(bound_component_gif_url);
        ToolbarAction rule_add_bound_component_action = new ToolbarAction("add sites (unknown binding partner)", new ImageIcon(bound_component_gif_url), "site bound to unknown partner", 'C', editor_panel);

        ImageIcon add_unbound_component_icon = new ImageIcon(unbound_component_gif_url);
        ToolbarAction rule_add_unbound_component_action = new ToolbarAction("add sites (unbound)", new ImageIcon(unbound_component_gif_url), "unbound site", 'C', editor_panel);

        ImageIcon add_arrow_icon = new ImageIcon(arrow_gif_url);
        ToolbarAction add_arrow_action = new ToolbarAction("add operator (arrow)", add_arrow_icon, "arrow", 'C', editor_panel);

        ImageIcon add_double_arrow_icon = new ImageIcon(double_arrow_gif_url);
        ToolbarAction add_double_arrow_action = new ToolbarAction("add operator (double arrow)", add_double_arrow_icon, "double arrow", 'C', editor_panel);

        ImageIcon add_plus_icon = new ImageIcon(plus_gif_url);
        ToolbarAction add_plus_action = new ToolbarAction("add operator (plus)", add_plus_icon, "plus", 'C', editor_panel);

        ImageIcon about_icon = new ImageIcon(about_gif_url);
        ToolbarAction about_action = new ToolbarAction("about", about_icon, "about", 'C', editor_panel);
        
        JToggleButton manip_button = new JToggleButton(manip_icon, true);
        manip_button.setAction(rule_manip_action);
        manip_button.setText(null);

        JToggleButton add_unbound_component_button = new JToggleButton(add_unbound_component_icon, false);
        add_unbound_component_button.setAction(rule_add_unbound_component_action);
        add_unbound_component_button.addActionListener(rule_add_unbound_component_action);
        add_unbound_component_button.setText(null);

        JToggleButton add_bound_component_button = new JToggleButton(add_unbound_component_icon, false);
        add_bound_component_button.setAction(rule_add_bound_component_action);
        add_bound_component_button.addActionListener(rule_add_bound_component_action);
        add_bound_component_button.setText(null);

        JToggleButton add_unspecified_component_button = new JToggleButton(add_unspecified_component_icon, false);
        add_unspecified_component_button.setAction(rule_add_unspecified_component_action);
        add_unspecified_component_button.addActionListener(rule_add_unspecified_component_action);
        add_unspecified_component_button.setText(null);

        JToggleButton add_container_button = new JToggleButton(add_container_icon, false);
        add_container_button.setAction(rule_add_container_action);
        add_container_button.addActionListener(rule_add_container_action);
        add_container_button.setText(null);

        JToggleButton add_edge_button = new JToggleButton(add_edge_icon, false);
        add_edge_button.setAction(rule_add_edge_action);
        add_edge_button.addActionListener(rule_add_edge_action);
        add_edge_button.setText(null);

        JToggleButton add_map_button = new JToggleButton(add_map_icon, false);
        add_map_button.setAction(rule_add_map_action);
        add_map_button.addActionListener(rule_add_map_action);
        add_map_button.setText(null);

        JToggleButton add_arrow_button = new JToggleButton(add_arrow_icon, false);
        add_arrow_button.setAction(add_arrow_action);
        add_arrow_button.addActionListener(add_arrow_action);
        add_arrow_button.setText(null);

        JToggleButton add_double_arrow_button = new JToggleButton(add_double_arrow_icon, false);
        add_double_arrow_button.setAction(add_double_arrow_action);
        add_double_arrow_button.addActionListener(add_double_arrow_action);
        add_double_arrow_button.setText(null);

        JToggleButton add_plus_button = new JToggleButton(add_plus_icon, false);
        add_plus_button.setAction(add_plus_action);
        add_plus_button.addActionListener(add_plus_action);
        add_plus_button.setText(null);

        ButtonGroup modes = new ButtonGroup();
        modes.add(manip_button);
        modes.add(add_unspecified_component_button);
        modes.add(add_unbound_component_button);
        modes.add(add_bound_component_button);
        modes.add(add_container_button);
        modes.add(add_edge_button);
        modes.add(add_map_button);
        modes.add(add_arrow_button);
        modes.add(add_double_arrow_button);
        modes.add(add_plus_button);

        JToolBar toolbar = new JToolBar("Tools");

        toolbar.add(manip_button);

        toolbar.addSeparator();
        toolbar.add(add_unspecified_component_button);
        toolbar.add(add_bound_component_button);
        toolbar.add(add_unbound_component_button);
        toolbar.add(add_container_button);
        toolbar.add(add_edge_button);
        toolbar.addSeparator();
        toolbar.add(add_map_button);
        toolbar.add(add_plus_button);
        toolbar.add(add_arrow_button);
        toolbar.add(add_double_arrow_button);
        toolbar.addSeparator();
        toolbar.add(erase_action);
        toolbar.addSeparator();
        toolbar.add(save_action);
        toolbar.add(about_action);
        return toolbar;
    }

    public void setMode(String mode) {
        editor_panel.repaint();
        this.mode = mode;
    }

    public static void main(String[] args){
        new RuleBuilder();
    }
}
