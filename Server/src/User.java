import java.io.Serializable;

public class User implements Serializable {

    private String username;
    private String password;
    private String status;

    public User(String username, String password){
        this.username = username;
        this.password = password;
        status = "online";
    }

    public User(){}

    public boolean checkPassword(String username, String password){
        return username.equals(this.username) && password.equals(this.password);
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public void setOnline(){
        status = "online";
    }

    public void setOffline(){
        status = "offline";
    }

    public synchronized String getStatus(){
        return status;
    }
}
