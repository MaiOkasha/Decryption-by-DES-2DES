package DoubleDes.client;

import javax.swing.*;

public class GUI extends JFrame {

    JTextArea jTextArea;
    JTextField jTextField;
    JButton jButton;

    GUI() {
        setTitle("Client");
        setLayout(null);
        jTextArea = new JTextArea();
        jTextArea.setBounds(20, 20, 350, 250);
        jTextArea.setEditable(false);
        jTextField = new JTextField();
        jTextField.setBounds(20, 290, 250, 40);
        jButton = new JButton();
        jButton.setBounds(290, 290, 80, 40);
        jButton.setText("Send");
        add(jTextArea);
        add(jTextField);
        add(jButton);
        setSize(400, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public JTextArea getjTextArea() {
        return jTextArea;
    }

    public JTextField getjTextField() {
        return jTextField;
    }

    public JButton getjButton() {
        return jButton;
    }
}