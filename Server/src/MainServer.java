import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class MainServer extends RemoteObject implements ServerInterface{

    private List<User> users;       //Lista di utenti e il loro status
    private ArrayList<Project> projects;        //Lista dei progetti
    private HashMap<SocketChannel, String> clients; //registra le client socket e i rispettivi dati da inviare
    private ArrayList<NotifyEventInterface> calls;

    private Address addr;       //Ultimo indirizzo utilizzato
    private ArrayList<Address> free_addr;       //Indirizzi utilizzati

    private File backup;    //directory per il backup
    private File usersfile; //File backup per gli utenti
    private File addressinfo;   //File backup per l'ultimo indirizzo utilizzato
    private File freeaddress;   //File contentente gli indirizzi dei progetti cancellati

    public MainServer() throws IOException {
        super();
        users = new ArrayList<>();
        projects = new ArrayList<>();
        clients = new HashMap<>();
        calls = new ArrayList<>();
        free_addr =new ArrayList<>();

        //Indirizzo di partenza;
        //Se ci sono file di backup verrà sovrascritto
        addr = new Address();
        int[] ip = new int[4];
        ip[0] = 224; ip[1] = 0; ip[2] = 0; ip[3] = 0;
        addr.setIp(ip);
        addr.setPort(6000);

        backup = new File("./backup");
        restoredata();
    }



    private void restoredata() throws IOException {
        ObjectMapper map = new ObjectMapper();
        map.enable(SerializationFeature.INDENT_OUTPUT);
        if(!backup.exists()){
            backup.mkdir();
        }
        usersfile = new File(backup + "/Users.json");
        addressinfo = new File(backup + "/Address_info.json");
        freeaddress = new File(backup + "/Free_Address.json");
        if(!usersfile.exists()){
            usersfile.createNewFile();
            map.writeValue(usersfile, users);
        }
        if(!addressinfo.exists()){
            addressinfo.createNewFile();
            map.writeValue(addressinfo, addr);
        }
        if(!freeaddress.exists()){
            freeaddress.createNewFile();
            map.writeValue(freeaddress, free_addr);
        }
        String[] file = backup.list();
        for(int i=1; i<file.length; i++){
            if(file[i].contains("Users.json")){
                users = new ArrayList<>(Arrays.asList(map.readValue(usersfile, User[].class)));     //Recupero la lista degli utenti
            }
            else if(file[i].contains("Address_info.json")){
                addr = map.readValue(addressinfo, Address.class);       //recupero gli ultimi indirizzi utilizzati
            }
            else if(file[i].contains("Free_Address.json")){
                free_addr = new ArrayList<>(Arrays.asList(map.readValue(freeaddress, Address[].class)));
            }
            else{   //file[i] è la cartella di un progetto
                //Recupero i dati del progetto(Card, indirizzi e lista membri)
                String[] project_data = (new File(backup + "/"+file[i])).list();
                Project p = new Project(file[i]);
                for(int j = 0; j<project_data.length; j++){
                    if(project_data[j].contains("members.json")){
                        ArrayList<String> members = new ArrayList<>(Arrays.asList(map.readValue(new File(backup+"/"+file[i]+"/"+project_data[j]), String[].class)));
                        p.restoremembers(members);
                    }
                    else if(project_data[j].contains("infoaddress.json")){
                        Address addr = map.readValue(new File(backup+"/"+file[i]+"/"+project_data[j]), Address.class);
                        p.setAddr(addr);
                    }
                    else{//project_data[j] è una card
                        Card c = map.readValue(new File(backup+"/"+file[i]+"/"+project_data[j]), Card.class);
                        p.addCard(c);
                    }
                }
                projects.add(p);
            }

        }
    }


    private void savedata() throws InvalidOperation, IOException {

        //rimuovo dai file backup precedenti i progetti per evitare inconsistenza (qualche progetto potrebbe esere stato cancellato)
        String[] file = backup.list();
        for(int i=1; i<file.length; i++){
            if(new File(backup + "/"+ file[i]).isDirectory())
                deleteDirectory(new File(backup + "/"+ file[i]));
        }

        //backup
        ObjectMapper map = new ObjectMapper();
        for(Project p : projects){
            File f_proj = new File(backup +"/"+p.getName());
            if(!f_proj.exists())
                f_proj.mkdir();         //creo una cartella per progetto
            for(String c : p.getCards()){
                File f_card = new File(f_proj + "/" + c + ".json"); //Salvo le card
                f_card.createNewFile();
                map.writeValue(f_card, p.getCard(c));
            }
            File f_members = new File(f_proj + "/members.json");    //Salvo la lista dei membri del progetto p
            f_members.createNewFile();
            map.writeValue(f_members, p.getmembers());
            File f_info_address = new File(f_proj + "/infoaddress.json");   //Salvo gli indirizzi relativi alla chat del progetto p
            f_info_address.createNewFile();
            map.writeValue(f_info_address, p.getAddr());
        }
        map.writeValue(usersfile, users);
        map.writeValue(addressinfo, addr);
        map.writeValue(freeaddress, free_addr);
    }

    //rimuovo tutti i file della directory "path" e infine rimuovo la directory
    private static void deleteDirectory(File path) {
        if(path.exists()) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                    files[i].delete();
            }
        }
        path.delete();
    }

    public void start() throws IOException {

        ServerSocketChannel ssocket = ServerSocketChannel.open();  //Apertura della listening socket
        ssocket.socket().bind(new InetSocketAddress(19999)); //Configuro la socket
        ssocket.configureBlocking(false);
        Selector sel = Selector.open();
        ssocket.register(sel, SelectionKey.OP_ACCEPT);

        while(true){
            sel.select();
            Set<SelectionKey> keys = sel.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){     //Ok
                    ServerSocketChannel server_socket = (ServerSocketChannel) key.channel();
                    SocketChannel client_socket = server_socket.accept();
                    clients.put(client_socket, "default");      //inserisco una stringa di default per utilizzare la replace
                    client_socket.configureBlocking(false);
                    client_socket.register(sel, SelectionKey.OP_READ);
                    key.attach(null);
                }
                else if(key.isReadable()){
                    SocketChannel client_socket = (SocketChannel) key.channel();
                    ByteBuffer client_data = ByteBuffer.allocate(1024);
                    client_socket.read(client_data);
                    String request = new String(client_data.array()).trim();
                    if(request.length() > 0) {
                        StringTokenizer request_token = new StringTokenizer(request);
                        switch (request_token.nextToken()) {
                            case "login":
                                clients.replace(client_socket, login(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "logout":
                                clients.replace(client_socket, logout(request_token.nextToken()));
                                break;
                            case "listprojects":
                                clients.replace(client_socket, listprojects(request_token.nextToken()));
                                break;
                            case "createproject":                            //username                   //project_name
                                clients.replace(client_socket, createproject(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "addmember":                               //username member           //project_name
                                clients.replace(client_socket, addmember(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "showmembers":                             //project_name
                                clients.replace(client_socket, showmembers(request_token.nextToken()));
                                break;
                            case "showcards":                               //projectname
                                clients.replace(client_socket, showcards(request_token.nextToken()));
                                break;
                            case "showcard":                            //cardname                  //projectname
                                clients.replace(client_socket, showcard(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "addcard":                             //cardname                 //description              //projectname
                                clients.replace(client_socket, addcard(request_token.nextToken(), request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "movecard":                            //projectname               //cardname                  //oldstatus                 //newstatus
                                clients.replace(client_socket, movecard(request_token.nextToken(), request_token.nextToken(), request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "getcardhistory":                              //projectname               //cardname
                                clients.replace(client_socket, getcardhistory(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "cancelproject":                           //projectname
                                clients.replace(client_socket, cancelproject(request_token.nextToken()));
                                break;
                            case "joinproject":                             //projectname               //username
                                clients.replace(client_socket, joinproject(request_token.nextToken(), request_token.nextToken()));
                                break;
                            case "getudpinfo":                              //projectname
                                clients.replace(client_socket, getUDPInfo(request_token.nextToken()));
                                break;
                        }
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                }
                else if(key.isWritable()){
                    SocketChannel client_socket = (SocketChannel) key.channel();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(clients.get(client_socket));
                    byte[] response = baos.toByteArray();
                    client_socket.write(ByteBuffer.wrap(response));
                    if(clients.get(client_socket).equals("Logout successfully")){
                        clients.remove(client_socket);
                        client_socket.close();
                    }
                    else key.interestOps(SelectionKey.OP_READ);
                }
            }
        }
    }


    private String login(String username, String password) throws RemoteException {
        if(username.length() == 0) return "Username null";
        for(User u : users){
            if(u.getUsername().equalsIgnoreCase(username)){
                if(password.equals(u.getPassword())){
                    if(!u.getStatus().equals("online")){
                        u.setOnline();
                        callbacks(username, "online");
                        return "Logged successfully";
                    }
                    else return "This user is already logged";
                }
                else return "Wrong password";
            }
        }
        return "User does not exists";
    }

    private String logout(String username) throws RemoteException {
        for(User u : users){
            if(u.getUsername().equalsIgnoreCase(username)){
                u.setOffline();
                callbacks(username, "offline");
                return "Logout successfully";
            }
        }
        return "error";
    }

    private String listprojects(String username){
        String response = "";
        for(Project p : projects){
            if(p.checkmember(username)) response = response + " " + p.getName();
        }
        if(response.equals("")) return "Project_List_empty";
        return response;
    }

    private String createproject(String username, String projectname) throws IOException {
        //Controllo che il nome sia univoco
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)) return "Project_already_exists";
        }

        Project project = new Project(projectname);
        project.addmember(username);

        //Configuro i dati per la chat
        //Controllo se ci sono indirizzi riutilizzabili
        if(!free_addr.isEmpty()){
            Address tmp = free_addr.remove(0);
            project.setAddr(tmp);
        }
        //altrimenti cerco ip e porta liberi
        else{
            int port = generatePort();
            int[] ip = generateIp();

            project.setAddr(new Address(ip, port));
        }

        projects.add(project);

        return "Project created";
    }

    private String addmember(String username, String projectname){
        for(User s : users){
            if(s.getUsername().equalsIgnoreCase(username)){
                for(Project p : projects){
                    if(p.getName().equalsIgnoreCase(projectname)){
                        if(p.checkmember(username)) return "Member already is in the project";
                        else{
                            p.addmember(username);
                            return "Member added";
                        }
                    }
                }
                return "Project doesn't exist";

            }
        }
        return "User doesn't exist";
    }

    private String showmembers(String projectname){
        String response = "Project_doesn't_exist";
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                response = "";
                for(String s : p.getmembers()) response = response.concat(" "+s);
                return response;
            }
        }
        return response;
    }

    private String addcard(String cardname, String description, String projectname) throws IOException {
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                try {
                    p.createCard(cardname, description);
                } catch (InvalidOperation invalidOperation) {
                    return "Card already exists in this project";
                }
                return "Card added";
            }
        }
        return "Project_doesn't_exist";
    }

    private String showcard(String cardname, String projectname){
        //ritorna nome+descrizione+stato
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                try {
                    Card tmp = p.getCard(cardname);
                    String response = "Name: " + tmp.getName() + "\n";
                    String status = "status: " + tmp.getStatus() + "\n";
                    String description = tmp.getDescription();
                    return response + status + description;
                } catch (InvalidOperation invalidOperation) {
                    return "Card does not exist in this project";
                }
            }
        }
        return "Project does not exist";
    }

    private String showcards(String projectname){
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                ArrayList<String> cards = p.getCards();
                if(cards.isEmpty()) return "empty";
                String response = "";
                for(String s : cards) response = response.concat(s+"\n");
                return response;
            }
        }
        return "Project does not exist";
    }

    private String movecard(String projectname, String cardname, String oldstatus, String newstatus) throws IOException {
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)) {
                try {
                    p.moveCard(cardname, oldstatus, newstatus);
                } catch (InvalidOperation invalidOperation) {
                    return invalidOperation.getMessage();
                }

                String msg = cardname + " moved from " + oldstatus + " to " + newstatus;
                byte[] buff = msg.getBytes();
                DatagramPacket dp = new DatagramPacket(buff, buff.length, InetAddress.getByName(p.getIp()), addr.getPort());
                try(DatagramSocket ds = new DatagramSocket()){  //Uso una porta effimera
                    ds.send(dp);
                }
                return "Card moved successfully";
            }
        }
        return "Project does not exist";
    }

    private String getcardhistory(String projectname, String cardname){
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)) {
                try {
                    Card c = p.getCard(cardname);
                    String response = cardname + " history:\n";
                    for (String s : c.getHistory()) response = response.concat(s + "\n");
                    return response;
                } catch (InvalidOperation invalidOperation) {
                    return "Card does not exist in this project";
                }
            }
        }
        return "Project does not exist";
    }

    private String cancelproject(String projectname) throws IOException {
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                free_addr.add(p.getAddr());
                projects.remove(p);
                return "Project removed";
            }
        }
        return "error";
    }

    private String joinproject(String projectname, String username){
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                if(p.checkmember(username)) return "Allowed";
                else return "Denied";
            }
        }
        return "Project_doesn't_exists";
    }

    private String getUDPInfo(String projectname){
        for(Project p : projects){
            if(p.getName().equalsIgnoreCase(projectname)){
                System.out.println("generato indirizzo: " + p.getIp() + " " + p.getPort());
                return "" + p.getPort() + " " + p.getIp();
            }
        }
        return "error";
    }

    private int generatePort(){
        addr.setPort();
        return addr.getPort();
    }

    private int[] generateIp(){
        int[] last_ip;
        last_ip = addr.getIp();
        if(last_ip[3] < 255){
            last_ip[3]++;
        }
        else if(last_ip[2] < 255){
            last_ip[2]++;
        }
        else if(last_ip[1] < 255){
            last_ip[1]++;
        }
        else if(last_ip[0] < 239){
            last_ip[0]++;
        }
        addr.setIp(last_ip);
        return last_ip;
    }

    public void setAllOffline(){
        for(User u : users){
            u.setOffline();
        }
    }

    //CallBack method
    @Override
    public String register(String username, String password) throws IOException {
        if(username.length() == 0) return "Username null";
        for(User u : users){
            if(u.getUsername().equalsIgnoreCase(username)) return "Username already exists";
        }
        if(password.length() < 4) return "Password must be longer 4";
        else{
            users.add(new User(username, password));
            callbacks(username, "online");
            return "Registered successfully";
        }
    }

    @Override
    public Map<String, String> getUsers(){
        Map<String, String> tmp = new HashMap<>();
        for(User u : users){
            tmp.put(u.getUsername(), u.getStatus());
        }
        return tmp;
    }

    @Override
    public synchronized void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException{
        calls.add(ClientInterface);
    }

    @Override
    public synchronized void unregisterForCallback(NotifyEventInterface client) throws RemoteException {
        calls.remove(client);
    }

    private synchronized void callbacks(String username, String status) throws RemoteException {
        for(NotifyEventInterface n : calls){
            n.NotifyEvent(username, status);
        }
    }

    public static void main(String[] args) throws IOException {
        MainServer server = new MainServer();

        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
        LocateRegistry.createRegistry(16666);
        Registry registry = LocateRegistry.getRegistry(16666);
        registry.rebind("Server", stub);

        //Handler per gestire l'interruzione del server ed eseguire il backup dei dati
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                try {
                    System.out.println("Backup eseguito");
                    System.out.println("Server Chiuso");
                    server.setAllOffline();
                    server.savedata();
                } catch (IOException | InvalidOperation e) {
                    e.printStackTrace();
                }
            }
        });
        server.start();

    }

}