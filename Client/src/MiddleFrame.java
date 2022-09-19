import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.StringTokenizer;


public class MiddleFrame extends JFrame implements ActionListener, WindowListener {

    private Container c = getContentPane();

    //campo e pulsanti per creare un nuovo progetto
    private JButton new_proj = new JButton("Create new project");
    private JTextField insert_name = new JTextField();
    private JButton create = new JButton("create");

    private JButton show_proj = new JButton("Project list");

    private JButton logout = new JButton("LOGOUT");

    //Campo e pulsante per unirsi ad un progetto
    private JTextField join = new JTextField();
    private JButton join_project = new JButton("Join Project");

    private JButton show_users = new JButton("Show Users");

    private DataClient data;
    private ServerInterface stub;                   //per la callbackunregister
    private NotifyEventInterface clientInterface;   //per la callbackunregister

    public MiddleFrame(ServerInterface stub, NotifyEventInterface clientInterface, DataClient data){
        this.data = data;
        this.stub = stub;
        this.clientInterface = clientInterface;
        c.setLayout(null);


        new_proj.setBounds(325, 50, 230, 25);
        insert_name.setBounds(325, 100, 230, 25);
        insert_name.setVisible(false);
        create.setBounds(350, 130, 180, 25);
        create.setVisible(false);
        show_proj.setBounds(45, 50, 230, 25);
        logout.setBounds(10, 5, 150, 25);
        join.setBounds(45, 100, 230, 25);
        join_project.setBounds(70, 130, 180, 25);
        show_users.setBounds(225, 400, 150, 25);

        c.add(new_proj);
        c.add(insert_name);
        c.add(create);
        c.add(show_proj);
        c.add(logout);
        c.add(join);
        c.add(join_project);
        c.add(show_users);


        logout.addActionListener(this);
        new_proj.addActionListener(this);
        create.addActionListener(this);
        show_proj.addActionListener(this);
        join_project.addActionListener(this);
        show_users.addActionListener(this);

        setTitle("WORTH - Home");
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == logout){
            try {
                stub.unregisterForCallback(clientInterface);
                String response = logoutuser();
                JOptionPane.showMessageDialog(this, response);
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            try {
                new LoginFrame(stub , clientInterface, data);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dispose();
        }
        if(e.getSource() == new_proj){
            insert_name.setVisible(true);
            create.setVisible(true);
        }
        if(e.getSource() == create && insert_name.getText()!=null){
            String response = null;
            try {
                response = create_project(insert_name.getText());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            if(response.equals("Project created")){
                JOptionPane.showMessageDialog(this, response);
                //CHIEDE A SERVER IP E PORTA PER COLLEGARSI ALLA CHAT
                String info = null;
                try {
                    info = getudpinfo(insert_name.getText());
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                StringTokenizer token = new StringTokenizer(info);
                int port = Integer.parseInt(token.nextToken());
                String ip = token.nextToken();
                MulticastSocket ms = null;
                try {
                    ms = new MulticastSocket(port);
                    ms.joinGroup(InetAddress.getByName(ip));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ChatFrame chat = null;  //Viene aperto il frame ChatFrame(settato a invisible)
                try {
                    chat = new ChatFrame(data.getMy_username(), ms, port, InetAddress.getByName(ip), null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Thread t = new Thread(chat);
                chat.setVisible(false);
                t.start();
                new ProjectFrame(insert_name.getText(), stub, data, ms, chat, t, clientInterface);
                dispose();
            }
            else JOptionPane.showMessageDialog(this, response);

        }
        if(e.getSource() == show_proj){
            StringTokenizer response = null;
            try {
                response = list_projects();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            int i = 0;
            String tmp = " ";
            while(response.hasMoreTokens()){
                tmp = tmp.concat(response.nextToken() +"\n");
            }
            JOptionPane.showMessageDialog(this, "Project list:\n"+tmp);
        }

        if(e.getSource() == join_project){
            String response = null;
            try {
                response = joinproject(join.getText());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            if(!response.equals("Allowed")) JOptionPane.showMessageDialog(this, response);
            else{
                //CHIEDE A SERVER IP E PORTA PER COLLEGARSI ALLA CHAT
                String info = null;
                try {
                    info = getudpinfo(join.getText());
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                StringTokenizer token = new StringTokenizer(info);
                int port = Integer.parseInt(token.nextToken());
                String ip = token.nextToken();
                MulticastSocket ms = null;
                try {
                    ms = new MulticastSocket(port);
                    ms.joinGroup(InetAddress.getByName(ip));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                ChatFrame chat = null;
                try {
                    chat = new ChatFrame(data.getMy_username(), ms, port, InetAddress.getByName(ip), null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                chat.setVisible(false);
                Thread t = new Thread(chat);
                t.start();
                new ProjectFrame(join.getText(), stub, data, ms, chat, t, clientInterface);
                dispose();
            }
        }
        if(e.getSource() == show_users){
            JOptionPane.showMessageDialog(this, data.showusers());
        }
    }

    //Metodi per comunicare tramite TCP le richieste al Server
    private String create_project(String project_name) throws IOException, ClassNotFoundException {
        String request = "createproject " + data.getMy_username() + " " + project_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private StringTokenizer list_projects() throws IOException, ClassNotFoundException {
        String request = "listprojects " + data.getMy_username();
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        String tmp = (String) in.readObject();
        return (new StringTokenizer(tmp));
    }

    private String joinproject(String projectname) throws IOException, ClassNotFoundException {
        String request = "joinproject " + projectname + " " + data.getMy_username();
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String getudpinfo(String projectname) throws IOException, ClassNotFoundException {
        String request = "getudpinfo " + projectname;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String logoutuser() throws IOException, ClassNotFoundException {
        String request = "logout " + data.getMy_username();
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    //handler per gestire la chiusura della finestra
    @Override
    public void windowClosing(WindowEvent windowEvent) {
        //effettuo il logout e chiudo la socket
        try {
            String foo = logoutuser();
            data.getSocket().close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            stub.unregisterForCallback(clientInterface);
        } catch (RemoteException e) {
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
