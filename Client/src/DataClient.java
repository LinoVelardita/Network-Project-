import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class DataClient {

    //status[i] is the status(online/offline) of users[i]
    private HashMap<String, String> users_info;


    private String my_username;
    private SocketChannel socket;

    public DataClient(SocketChannel socket){
        this.socket = socket;
        users_info = new HashMap<>();
    }

    public void setMy_username(String username){
        my_username = username;
    }

    public String getMy_username(){
        return my_username;
    }

    public SocketChannel getSocket(){
        return socket;
    }

    public void setSocket(SocketChannel s){
        socket = s;
    }

    public void setStatus(String username, String status){
        if(users_info.containsKey(username)) {
            users_info.replace(username, status);
        }
        else users_info.put(username, status);
    }

    public void setUsers(HashMap<String, String> users){
        users_info = users;
    }

    public String showusers(){
        String result = "";
        String tmp;
        for(String s : users_info.keySet()){
            tmp = s + ": " + users_info.get(s);
            result = result.concat(tmp + "\n");
        }
        return result;
    }
}
