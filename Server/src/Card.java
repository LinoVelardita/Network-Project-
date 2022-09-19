import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {

    private String name;
    private String description;

    private ArrayList<String> history = new ArrayList<>();
    private String status;//stato corrente della card


    public Card(String name, String description){
        this.name = name;
        this.description = description;
        history.add("TODO");
        status = "TODO";
    }

    public Card(){}

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public ArrayList<String> getHistory(){
        return history;
    }

    public void modifyStatus(String new_status){
        history.add(new_status);
        status = new_status;
        System.out.println(name + " spostata in " + new_status);
    }

    //Lo stato corrente della card corrisponde alla coda della history
    public String getStatus(){
        return status;
    }

}
