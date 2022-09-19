import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChatFrame extends JFrame implements ActionListener, Runnable, WindowListener {

    private Container c = getContentPane();

    private JTextField msg_box= new JTextField();
    private JButton msg_send = new JButton(" SEND ");

    private JButton back = new JButton("BACK");
    private JLabel info_chat = new JLabel();
    static private JTextArea chat = new JTextArea();

    private String user;
    private MulticastSocket ms;
    private int port;
    private InetAddress ip;
    private ProjectFrame prev;


    public ChatFrame(String user, MulticastSocket ms, int port, InetAddress ip, ProjectFrame prev) throws IOException {
        this.ms = ms;
        ms.setTimeToLive(1);
        this.port = port;
        this.ip = ip;
        this.user = user;
        this.prev = prev;

        info_chat.setText("Chat - " + user);

        c.setLayout(null);

        msg_box.setBounds(10, 520, 800, 30);
        msg_send.setBounds(820, 520, 165, 30);

        back.setBounds(10, 5, 200, 25);
        info_chat.setBounds(430, 5, 300, 25);
        chat.setBounds(10, 40, 975, 465);
        chat.setBackground(new java.awt.Color(196, 203, 255, 162));
        chat.setEditable(false);
        chat.setLineWrap(true);
        chat.setWrapStyleWord(true);


        c.add(msg_box);
        c.add(msg_send);
        c.add(info_chat);
        c.add(back);
        c.add(chat);

        msg_send.addActionListener(this);
        back.addActionListener(this);


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("WORTH - Chat");
        setSize(1000, 600);
        //this.setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == msg_send){
            try {
                sendmsg(msg_box.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            msg_box.setText("");
        }
        if(e.getSource() == back){
            prev.setVisible(true);
            this.setVisible(false);
        }
    }

    //Manda il messaggio "msg" in multicast
    private void sendmsg(String msg) throws IOException {
        msg = user + ": " + msg;
        byte[] buff = msg.getBytes();
        DatagramPacket dp = new DatagramPacket(buff, buff.length, ip, port);
        try(DatagramSocket ds = new DatagramSocket()){  //Uso una porta effimera
            ds.send(dp);
        }
    }

    //Usato per mantenere il riferimento al Frame "precedente"
    public void setFrame(ProjectFrame p){
        this.prev = p;
    }

    //Ricevo i messaggi su un DatagramSocket
    @Override
    public void run() {
        byte[] buff = new byte[1024];

        DatagramPacket dp;
        while(true){
            dp = new DatagramPacket(buff, buff.length);
            try {
                ms.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String s = new String(dp.getData(), 0, dp.getLength());
            chat.append(s + "\n");
        }
    }

    //handler per gestire la chiusura della finestra
    @Override
    public void windowClosing(WindowEvent windowEvent) {
        prev.setVisible(true);
        this.setVisible(false);
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {

    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
