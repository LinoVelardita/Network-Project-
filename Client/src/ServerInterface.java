import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public interface ServerInterface extends Remote {

    String register(String username, String password) throws IOException;

    Map<String, String> getUsers() throws RemoteException;

    void registerForCallback(String username, NotifyEventInterface ClientInterface) throws RemoteException;

    void unregisterForCallback(NotifyEventInterface ClientInterface) throws RemoteException;
}
