import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class MainClient extends RemoteObject implements NotifyEventInterface {

    DataClient data;

    public MainClient(){
        super();
    }

    public void start() throws IOException, NotBoundException {

        //Registrazione RMI
        Registry reg = LocateRegistry.getRegistry(16666);
        ServerInterface stub = (ServerInterface) reg.lookup("Server");


        NotifyEventInterface callback = this;
        NotifyEventInterface stubcallback = (NotifyEventInterface) UnicastRemoteObject.exportObject(callback, 0);

        data = new DataClient(null);    //dati del client
        HashMap<String, String> tmp = (HashMap<String, String>) stub.getUsers();    //ricevo dal server gli utenti registarti al servizio
        data.setUsers(tmp);

        new LoginFrame(stub, callback, data);

    }


    public static void main(String [] args) throws IOException, NotBoundException {
        MainClient client = new MainClient();
        client.start();
    }

    @Override
    public void NotifyEvent(String username, String status) throws RemoteException {
        data.setStatus(username, status);
    }

}
