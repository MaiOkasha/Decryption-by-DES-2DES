package DoubleDes.server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class GUI extends JFrame implements ActionListener{

    JTextArea jTextArea;
    JTextField jTextField;
    JButton jButton;


    static ServerSocket serverSocket;
    static Socket socket;

    static DataInputStream dis;
    static DataOutputStream dos;
    static String serverMsg, clientMsg;

    GUI() {
        setTitle("Server");
        setLayout(null);
        jTextArea = new JTextArea();
        jTextArea.setBounds(20, 20, 350, 250);
        jTextArea.setEditable(false);
        jTextField = new JTextField();
        jTextField.setBounds(20, 290, 250, 40);
        jButton = new JButton();
        jButton.setBounds(290, 290, 80, 40);
        jButton.setText("Send");
        jButton.addActionListener(this);
        add(jTextArea);
        add(jTextField);
        add(jButton);
        setSize(400, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);


        setupServerConnection();

        try{

            do{
                clientMsg = dis.readUTF();


                byte[] text = getText(clientMsg);
                long key = getKey("mohammed");
                long IV = getKey("emademad");
                long[] blocks = splitInputIntoBlocks(text);
                runCBCDecrypt(blocks,key,IV);
                jTextArea.append("\nClient : "+clientMsg);
            }while (true);

        }catch (IOException e){
            e.getMessage();
        }

    }

    private static void runCBCEncrypt(long[] blocks, long key, long IV) {
        DoubleDes tripleDes = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts;

        cipherTexts = tripleDes.CBCEncrypt(blocks, key, IV);

        String serverMSG = "";

        for (long block : cipherTexts) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            serverMSG += new String(bytes);
        }

        serverMsg = serverMsg;

        try {
            dos.writeUTF(serverMSG);
            dos.flush();
        }catch ( IOException e){
            e.getMessage();
        }
    }

    private static void runCBCDecrypt(long[] blocks, long key, long IV){
        DoubleDes tripleDes = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts, plainTexts;

        cipherTexts = tripleDes.CBCEncrypt(blocks, key, IV);
        plainTexts = tripleDes.CBCDecrypt(cipherTexts, key, IV);

        for (long block : plainTexts) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            clientMsg += new String(bytes);
        }
    }


    private static void runCipher(long[] blocks, long key)
    {
        DoubleDes tripleDes = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts = new long[blocks.length], plainTexts = new long[blocks.length];

        System.out.println("Input plaintext: ");
        for (long block : blocks)
        {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            System.out.print(new String(bytes));
        }

        System.out.println("\nEncrypted ciphertext: ");
        for (int i = 0; i < blocks.length; i++)
        {
            cipherTexts[i] = tripleDes.encrypt(blocks[i], key);
        }

        for (long block : cipherTexts)
        {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            System.out.print(new String(bytes));
        }

        System.out.println("\nDecrypted plaintext: ");
        for (int i = 0; i < cipherTexts.length; i++)
        {
            plainTexts[i] = tripleDes.decrypt(cipherTexts[i], key);
        }
        for (long block : plainTexts)
        {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            System.out.print(new String(bytes));
        }
    }

    private static long[] splitInputIntoBlocks(byte[] input) {
        long blocks[] = new long[input.length / 8 + 1];

        for (int i = 0, j = -1; i < input.length; i++) {
            if (i % 8 == 0)
                j++;
            blocks[j] <<= 8;
            blocks[j] |= input[i];
        }

        return blocks;
    }


    private static byte[] getText(String text) {
        DataOutputStream dataOutputStream;
        try{
            dataOutputStream = new DataOutputStream(new FileOutputStream("test.txt"));
            dataOutputStream.writeUTF(text);
        }catch (IOException e){
            e.getMessage();
        }

        return getByteArrayFromFile("test.txt");
    }


    private static byte[] getByteArrayFromFile(String filePath) {
        File file = new File(filePath);
        byte[] fileBuff = new byte[(int) file.length()];

        try {
            DataInputStream fileStream = new DataInputStream(new FileInputStream(file));
            fileStream.readFully(fileBuff);
            fileStream.close();
        } catch (IOException e) {
            printErrorAndDie("Cannot read from file.");
        }

        return fileBuff;
    }


    private static long getKey(String key) {
        String keyStr = key;
        byte[] keyBytes;
        long key64 = 0;


        if (keyStr.length() > 8) {
            System.out.println("Input is greater than 64 bits.");
            System.exit(0);
        }

        keyBytes = keyStr.getBytes();

        for (byte keyByte : keyBytes) {
            key64 <<= 8;
            key64 |= keyByte;
            System.out.println(Long.toBinaryString(key64));
        }

        return key64;
    }


    private static boolean getCBCConfirmation(BufferedReader reader)
    {
        int c = 0;
        try {
            c = reader.read();
        } catch (IOException e) {
            printErrorAndDie("");
        }

        return (Character.toLowerCase(c) == 'y');
    }

    private static void printErrorAndDie(String message)
    {
        System.err.println("Fatal IO error encountered." + "\n" + message);
        System.exit(1);
    }

    static void setupServerConnection(){
        try {
            serverSocket = new ServerSocket(1000);
            socket = serverSocket.accept();
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e){
            e.getMessage();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        byte[] text = getText(jTextField.getText());
        long key = getKey("mohammed");
        long IV = getKey("emademad");
        long[] blocks = splitInputIntoBlocks(text);
        runCBCEncrypt(blocks, key, IV);
        jTextArea.append("\nServer : "+serverMsg);
        jTextField.setText(null);
        JOptionPane.showMessageDialog(null, "Test");
    }
}