import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface NotifyEventInterface extends Remote {

        void NotifyEvent(String username, String status) throws RemoteException;


}
