import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;

public class ProjectFrame extends JFrame implements ActionListener, WindowListener {

    private Container c = getContentPane();

    private JButton show_members = new JButton("show members");
    private JButton add_member = new JButton("add member");


    private JButton cancel_project = new JButton("Cancel project");

    private JButton join_chat = new JButton("join chat");

    private JButton show_cards = new JButton("show cards");
    private JButton show_card = new JButton("show a card");
    private JButton move_card = new JButton("move card");
    private JButton add_card = new JButton("add card");
    private JButton card_history = new JButton("get history");

    private JButton back = new JButton("BACK");

    private JTextField field1 = new JTextField();
    private JTextField field2 = new JTextField();
    private JTextField field3 = new JTextField();
    private JButton confirm = new JButton();

    private String proj_name;
    private ServerInterface stub;
    private DataClient data;
    private MulticastSocket ms;
    private ChatFrame chat;
    private Thread chat_tread;
    private NotifyEventInterface clientInterface;

    public ProjectFrame(String proj_name, ServerInterface stub, DataClient data, MulticastSocket ms, ChatFrame chat, Thread t, NotifyEventInterface clientInterface){
        chat.setFrame(this);
        chat.setVisible(false);
        this.proj_name = proj_name;
        this.ms = ms;
        this.stub = stub;
        this.data = data;
        this.chat = chat;
        this.clientInterface = clientInterface;
        chat_tread = t;
        setTitle("WORTH - " + proj_name);
        c.setLayout(null);

        back.setBounds(20, 5, 150, 25);
        join_chat.setBounds(570, 100, 150, 25);

        show_members.setBounds(20, 50, 150, 25);
        add_member.setBounds(180, 50, 150, 25);
        show_cards.setBounds(340, 50, 150, 25);
        move_card.setBounds(500, 50, 150, 25);
        add_card.setBounds(660, 50, 150, 25);
        cancel_project.setBounds(820, 50, 150, 25);
        show_card.setBounds(980, 50, 150, 25);
        card_history.setBounds(1140, 50, 150, 25);
        field1.setBounds(545, 300, 200, 25);
        field1.setVisible(false);
        field2.setBounds(545, 350, 200, 25);
        field2.setVisible(false);
        field3.setBounds(545, 250, 200, 25);
        field3.setVisible(false);
        confirm.setBounds(545, 400, 200, 25);
        confirm.setVisible(false);

        c.add(back);
        c.add(join_chat);
        c.add(show_members);
        c.add(add_member);
        c.add(show_cards);
        c.add(move_card);
        c.add(add_card);
        c.add(card_history);
        c.add(cancel_project);
        c.add(show_card);
        c.add(field1);
        c.add(field2);
        c.add(field3);
        c.add(confirm);

        back.addActionListener(this);
        join_chat.addActionListener(this);
        show_members.addActionListener(this);
        add_member.addActionListener(this);
        show_cards.addActionListener(this);
        move_card.addActionListener(this);
        add_card.addActionListener(this);
        cancel_project.addActionListener(this);
        show_card.addActionListener(this);
        card_history.addActionListener(this);
        confirm.addActionListener(this);


        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1310, 800);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == back){
            chat_tread.interrupt();
            chat.dispose();
            new MiddleFrame(stub, clientInterface, data);
            dispose();
        }
        if(e.getSource() == join_chat){
            chat.setVisible(true);
            this.setVisible(false);
        }
        if(e.getSource() == add_member){
            field1.setVisible(true);
            confirm.setText("Add member");
            confirm.setVisible(true);
        }
        if(e.getSource() == confirm && confirm.getText().equals("Add member") && !field1.getText().equals("")){
            String response = null;
            try {
                response = addmember(field1.getText());
            } catch (IOException | ClassNotFoundException ignored) {
            }
            JOptionPane.showMessageDialog(this, response);
            field1.setVisible(false);
            confirm.setVisible(false);
        }
        if(e.getSource() == show_members){
            String response = null;
            try {
                response = showmembers();
            } catch (IOException | ClassNotFoundException ignored) {
            }
            JOptionPane.showMessageDialog(this, response);
        }
        if(e.getSource() == show_cards){
            String response = null;
            try {
                response = showcards();
            } catch (IOException | ClassNotFoundException ignored) {
            }
            JOptionPane.showMessageDialog(this, response);
        }
        if(e.getSource() == show_card){
            field1.setVisible(true);
            confirm.setText("Show Card");
            confirm.setVisible(true);
        }
        if(e.getSource() == confirm && confirm.getText().equals("Show Card") && !field1.getText().equals("")){
            String response = null;
            try{
                response  = showcard(field1.getText());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, response);
            confirm.setVisible(false);
            field1.setVisible(false);
        }
        if(e.getSource() == move_card){
            field1.setVisible(true);
            field1.setText("Old status");
            field2.setVisible(true);
            field2.setText("New Status");
            field3.setVisible(true);
            field3.setText("Card name");
            confirm.setVisible(true);
            confirm.setText("Move card");
        }
        if(e.getSource() == confirm && confirm.getText().equals("Move card") && !field3.getText().equals("")){
            if(!legitmovement(field1.getText()) || !legitmovement(field2.getText())){
                JOptionPane.showMessageDialog(this, "states card must be:TODO\nINPROGRESS\nTOBEREVISED\nDONE");
            }
            else {
                String response = null;
                try {
                    response = movecard(field3.getText(), field1.getText(), field2.getText());
                } catch (IOException | ClassNotFoundException ignored) {
                }
                JOptionPane.showMessageDialog(this, response);
                field1.setVisible(false);
                field1.setText("");
                field2.setVisible(false);
                field2.setText("");
                field3.setVisible(false);
                field3.setText("");
                confirm.setVisible(false);
            }
        }
        if(e.getSource() == add_card){
            field3.setVisible(false);
            field1.setVisible(true);
            field1.setText("name");
            field2.setVisible(true);
            field2.setText("description");
            confirm.setVisible(true);
            confirm.setText("Add card");
        }
        if(e.getSource() == confirm && confirm.getText().equals("Add card") && !field1.getText().equals("") && !field2.getText().equals("")){
            String response = null;
            try {
                response = addcard(field1.getText(), field2.getText());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, response);
            field1.setVisible(false);
            field1.setText("");
            field2.setVisible(false);
            field2.setText("");
            confirm.setVisible(false);
        }
        if(e.getSource() == cancel_project){
            String response = null;
            try {
                response = cancelproject();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, response);
            chat_tread.interrupt();
            chat.dispose();
            new MiddleFrame(stub, clientInterface, data);
            dispose();
        }
        if(e.getSource() == card_history){
            field1.setVisible(true);
            confirm.setVisible(true);
            confirm.setText("get history");
        }
        if(e.getSource() == confirm && confirm.getText().equals("get history") && !field1.getText().equals("")){
            String response = null;
            try{
                response = getHistory(field1.getText());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, response);
            field1.setVisible(false);
            field1.setText("");
            confirm.setVisible(false);
        }
    }

    private String addmember(String username) throws IOException, ClassNotFoundException {
        String request = "addmember " + username + " " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String showmembers() throws IOException, ClassNotFoundException {
        String request = "showmembers " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String showcards() throws IOException, ClassNotFoundException {
        String request = "showcards " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String showcard(String cardname) throws IOException, ClassNotFoundException {
        String request = "showcard " + cardname + " " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String movecard(String cardname, String oldstatus, String newstatus) throws IOException, ClassNotFoundException {
        String request = "movecard " + proj_name + " " + cardname + " " + oldstatus + " " + newstatus;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String addcard(String cardname, String description) throws IOException, ClassNotFoundException {
        String request = "addcard " + cardname + " " + description + " " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String cancelproject() throws IOException, ClassNotFoundException {
        String request = "cancelproject " + proj_name;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private String getHistory(String cardname) throws IOException, ClassNotFoundException {
        String request = "getcardhistory " + proj_name + " " + cardname;
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    private boolean legitmovement(String status){
        if(status.equals("TODO") || status.equals("INPROGRESS") || status.equals("TOBEREVISED") || status.equals("DONE")) return true;
        return false;
    }

    private String logoutuser() throws IOException, ClassNotFoundException {
        String request = "logout " + data.getMy_username();
        (data.getSocket()).write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)));
        ObjectInputStream in = new ObjectInputStream((data.getSocket()).socket().getInputStream());
        return (String) in.readObject();
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

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
        chat_tread.interrupt();
        chat.dispose();
        ms.close();
        dispose();
        System.exit(0);
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
