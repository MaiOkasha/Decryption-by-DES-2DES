package DoubleDes.client;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class DoubleInterface {

    static Socket socket;
    static DataInputStream dis;
    static DataOutputStream dos;

    static GUI gui;
    //static Des.Client.GUI gui;
    static PrintWriter writerCM;


    static String serverMsg = "", clientMsg = "";

    public static void main(String[] args) {
        gui = new GUI();
        setupClientConnection();

        try{
            PrintWriter writer = new PrintWriter("serverMsgFile.txt", "UTF-8");

            writerCM = new PrintWriter("clientMsgFile.txt", "UTF-8");

            do{
                serverMsg = dis.readUTF();
                writer.println(serverMsg);
                writer.close();

                byte[] text = getText("serverMsgFile.txt");
                long key = getKey("mohammed");
                long IV = getKey("emademad");
                long[] blocks = splitInputIntoBlocks(text);
                runCBCDecrypt(blocks,key,IV);
                gui.getjTextArea().append("\nServer : "+serverMsg);
            }while (true);

        }catch (IOException e){
            e.getMessage();
        }

        gui.getjButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                writerCM.println(gui.getjTextField().getText());
                writerCM.close();
                byte[] text = getText("clientMsgFile.txt");
                long key = getKey("mohammed");
                long IV = getKey("emademad");
                long[] blocks = splitInputIntoBlocks(text);
                runCBCEncrypt(blocks, key, IV);
                gui.getjTextArea().append("\nClient : "+clientMsg);
                gui.jTextField.setText(null);
                JOptionPane.showMessageDialog(null, "Test");
            }
        });
    }

    private static void runCBCEncrypt(long[] blocks, long key, long IV) {
        DoubleDes des = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts;

        cipherTexts = des.CBCEncrypt(blocks, key, IV);

        String clientMSG = "";

        for (long block : cipherTexts) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            clientMSG += new String(bytes);

            try {
                dos.writeUTF(clientMSG);
            }catch ( IOException e){
                e.getMessage();
            }
        }

        clientMsg = clientMSG;

        try {
            dos.flush();
        }catch ( IOException e){
            e.getMessage();
        }
    }

    private static void runCBCDecrypt(long[] blocks, long key, long IV){
        DoubleDes des = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts, plainTexts;

        cipherTexts = des.CBCEncrypt(blocks, key, IV);
        plainTexts = des.CBCDecrypt(cipherTexts, key, IV);

        serverMsg = "";

        for (long block : plainTexts) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();

            serverMsg += new String(bytes);
        }
    }

    private static void runCipher(long[] blocks, long key) {
        DoubleDes des = new DoubleDes();
        byte[] bytes;
        long[] cipherTexts = new long[blocks.length], plainTexts = new long[blocks.length];

        System.out.println("Input plaintext: ");
        for (long block : blocks) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            System.out.print(new String(bytes));
        }

        System.out.println("\nEncrypted cipher text: ");
        for (int i = 0; i < blocks.length; i++)
            cipherTexts[i] = des.encrypt(blocks[i], key);

        for (long block : cipherTexts) {
            bytes = ByteBuffer.allocate(8).putLong(block).array();
            System.out.print(new String(bytes));
        }

        System.out.println("\nDecrypted plaintext: ");
        for (int i = 0; i < cipherTexts.length; i++)
            plainTexts[i] = des.decrypt(cipherTexts[i], key);

        for (long block : plainTexts) {
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


        return getByteArrayFromFile(text);
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


    private static long getKey(/*BufferedReader reader*/String key) {
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

    static void setupClientConnection(){
        try {
            socket = new Socket("localhost",1000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            JOptionPane.showMessageDialog(null, "Connection Successful");
        }catch (IOException e){
            JOptionPane.showMessageDialog(null, "Connection Failed");
        }
    }
}