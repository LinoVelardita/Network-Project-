import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class LoginFrame extends JFrame implements ActionListener, WindowListener {

    private Container c = getContentPane();
    private JLabel username = new JLabel("USERNAME");
    private JTextField insert_user = new JTextField();
    private JLabel password = new JLabel("PASSWORD");
    private JPasswordField insert_passw = new JPasswordField();
    private JCheckBox show_passw = new JCheckBox("Show Password");
    private JButton login = new JButton("login");
    private JButton register = new JButton("register");

    private ServerInterface stub;
    private DataClient data;
    private NotifyEventInterface clientInterface;


    public LoginFrame(ServerInterface stub, NotifyEventInterface clientInterface, DataClient data) throws IOException {

        SocketChannel socket = SocketChannel.open();
        socket.connect(new InetSocketAddress("127.0.0.1", 19999));
        data.setSocket(socket);

        this.stub = stub;
        this.clientInterface = clientInterface;
        this.data = data;

        c.setLayout(null);
        addWindowListener(this);

        username.setBounds(130, 150, 100, 25);
        insert_user.setBounds(230, 150, 200, 25);

        password.setBounds(130, 200, 100, 25);
        insert_passw.setBounds(230, 200, 200, 25);
        insert_passw.setEchoChar('*');
        show_passw.setBounds(230, 225, 150, 30);

        login.setBounds(230, 300, 120, 30);
        register.setBounds(230, 340, 120, 30);

        c.add(username);
        c.add(insert_user);
        c.add(password);
        c.add(insert_passw);
        c.add(show_passw);
        c.add(login);
        c.add(register);

        login.addActionListener(this);
        register.addActionListener(this);
        show_passw.addActionListener(this);

        setTitle("WORTH - Login");
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(600, 660);
        //this.setResizable(false);
        //il metodo setResizable(false) potrebbe dare problemi
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == show_passw){
            if(show_passw.isSelected()) insert_passw.setEchoChar((char) 0);
            else insert_passw.setEchoChar('*');
        }
        //LOGIN BUTTON
        if(e.getSource() == login && !insert_user.getText().equals("") && !new String(insert_passw.getPassword()).equals("")){
            String response = null;
            try {
                response = login_user(insert_user.getText(), new String(insert_passw.getPassword()));
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            if(response.equals("Logged successfully")) {
                data.setMy_username(insert_user.getText());
                data.setStatus(insert_user.getText(), "online");
                try {
                    stub.registerForCallback(data.getMy_username(), clientInterface);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                new MiddleFrame(stub, clientInterface, data);
                JOptionPane.showMessageDialog(this, response);
                dispose();
            }
            else{
                JOptionPane.showMessageDialog(this, response);
            }
        }
        //REGISTER BUTTON
        if(e.getSource() == register && !insert_user.getText().equals("") && !new String(insert_passw.getPassword()).equals("")){
            String check = null;
            try {
                check = stub.register(insert_user.getText(), new String(insert_passw.getPassword()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if(check.equals("Registered successfully")){
                data.setMy_username(insert_user.getText());
                data.setStatus(insert_user.getText(), "online");
                try {
                    stub.registerForCallback(data.getMy_username(), clientInterface);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                new MiddleFrame(stub , clientInterface, data);
                JOptionPane.showMessageDialog(this, check);
                dispose();
            }
            else JOptionPane.showMessageDialog(this, check);
        }
    }

    private String login_user(String username, String password) throws IOException, ClassNotFoundException {
        String request = "login " + username + " " + password;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    //handler per gestire la chiusura della finestra
    @Override
    public void windowClosing(WindowEvent windowEvent) {
        try {
            data.getSocket().close();
            stub.unregisterForCallback(clientInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose();
        System.exit(0);
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