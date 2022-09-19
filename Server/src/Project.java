import java.io.Serializable;
import java.util.ArrayList;

public class Project implements Serializable {

    private String name;    //nome del progetto
    private ArrayList<String> members = new ArrayList<>();  //Utenti che partecipano al progetto

    //liste delle card
    private ArrayList<Card> TODO = new ArrayList<>();
    private ArrayList<Card> INPROGRESS= new ArrayList<>();
    private ArrayList<Card> TOBEREVISED= new ArrayList<>();
    private ArrayList<Card> DONE= new ArrayList<>();

    private Address addr; //ip e porta realtivi alla chat del progetto


    public Project(String name){
        this.name = name;
    }

    public Project(){}

    public void addCard(Card c){        //Usato per ricostruire dai file di backup
        if(c.getStatus().equals("TODO")) TODO.add(c);
        if(c.getStatus().equals("INPROGRESS")) INPROGRESS.add(c);
        if(c.getStatus().equals("TOBEREVISED")) TOBEREVISED.add(c);
        if(c.getStatus().equals("DONE")) DONE.add(c);
    }

    public void restoremembers(ArrayList<String> members){      //Usato per ricostruire la lista di membri
        this.members = members;
    }

    public void addmember(String username){
        members.add(username);
    }

    public ArrayList<String> getmembers(){
        return members;
    }

    //restituisce true se l'utente fa parte del progetto else false
    public boolean checkmember(String member){
        for(String s : members){
            if(s.equalsIgnoreCase(member)) return true;
        }
        return false;
    }

    //crea una card e la inserisce nella lista TOD0
    public void createCard(String name, String description) throws InvalidOperation {

        /* CONTROLLO L'UNICITÀ DEL NOME DELLA CARD */
        for(Card c : TODO){
            if(c.getName().equalsIgnoreCase(name)) throw new InvalidOperation("Il nome "+name+" è gìa associato ad una card");
        }
        for(Card c : INPROGRESS){
            if(c.getName().equalsIgnoreCase(name)) throw new InvalidOperation("Il nome "+name+" è gìa associato ad una card");
        }
        for(Card c : TOBEREVISED){
            if(c.getName().equalsIgnoreCase(name)) throw new InvalidOperation("Il nome "+name+" è gìa associato ad una card");
        }
        for(Card c : DONE){
            if(c.getName().equalsIgnoreCase(name)) throw new InvalidOperation("Il nome "+name+" è gìa associato ad una card");
        }

        TODO.add(new Card(name, description));
    }

    //sposta una card da una lista ad un'altra
    public void moveCard(String cardname, String old_status, String new_status)throws InvalidOperation{
        //controllo la validità dello spostamento richiesto
        if(!legit(old_status, new_status)) throw new InvalidOperation("Non è possibile spostare la card "+cardname+" da "+old_status+" a "+new_status);

        switch(old_status){

            case "TODO":
                for(Card c : TODO){
                    if(c.getName().equalsIgnoreCase(cardname)){
                        INPROGRESS.add(c);
                        TODO.remove(c);
                        c.modifyStatus(new_status);
                        break;
                    }
                }
                break;

            case "INPROGRESS":
                if(new_status.equalsIgnoreCase("TOBEREVISED"))
                    for(Card c : INPROGRESS){
                        if(c.getName().equalsIgnoreCase(cardname)){
                            TOBEREVISED.add(c);
                            INPROGRESS.remove(c);
                            c.modifyStatus(new_status);
                            break;
                        }
                    }
                else
                    for(Card c : INPROGRESS){
                        if(c.getName().equalsIgnoreCase(cardname)){
                            DONE.add(c);
                            INPROGRESS.remove(c);
                            c.modifyStatus(new_status);
                            break;
                        }
                    }
                break;

            case "TOBEREVISED":
                if(new_status.equals("INPROGRESS"))
                    for(Card c : TOBEREVISED){
                        if(c.getName().equalsIgnoreCase(cardname)){
                            INPROGRESS.add(c);
                            TOBEREVISED.remove(c);
                            c.modifyStatus(new_status);
                            break;
                        }
                    }
                else
                    for(Card c : TOBEREVISED){
                        if(c.getName().equalsIgnoreCase(cardname)){
                            DONE.add(c);
                            TOBEREVISED.remove(c);
                            c.modifyStatus(new_status);
                            break;
                        }
                    }
                break;
        }
    }

    //restituisce la card con il nome "cardname"
    public Card getCard(String cardname) throws InvalidOperation {
        for(Card c : TODO){
            if(c.getName().equalsIgnoreCase(cardname)) return c;
        }
        for(Card c : INPROGRESS){
            if(c.getName().equalsIgnoreCase(cardname)) return c;
        }
        for(Card c : TOBEREVISED){
            if(c.getName().equalsIgnoreCase(cardname)) return c;
        }
        for(Card c : DONE){
            if(c.getName().equalsIgnoreCase(cardname)) return c;
        }
        throw new InvalidOperation();
    }

    //get method
    public ArrayList<String> getCards(){
        ArrayList<String> cards = new ArrayList<>();
        for(Card c : TODO) cards.add(c.getName());
        for(Card c : INPROGRESS) cards.add(c.getName());
        for(Card c : TOBEREVISED) cards.add(c.getName());
        for(Card c : DONE) cards.add(c.getName());
        return cards;
    }

    public String getName(){
        return name;
    }

    public ArrayList<Card> getTODO(){
        return TODO;
    }

    public ArrayList<Card> getINPROGRESS(){
        return INPROGRESS;
    }

    public ArrayList<Card> getTOBEREVISED(){
        return TOBEREVISED;
    }

    public ArrayList<Card> getDONE(){
        return DONE;
    }

    public Address getAddr(){
        return addr;
    }


    public void setAddr(Address addr){
        this.addr = addr;
    }

    public String getIp(){
        int[] ip = addr.getIp();
        String result = "" + ip[0];
        for(int i=1; i<4; i++){
            result = result.concat("."+ip[i]);
        }
        return result;
    }

    public int getPort(){
        return addr.getPort();
    }


    //verifico di poter spostare la card da A a B
    private boolean legit(String old_status, String new_status){
        if(old_status.equals("TODO"))
            return new_status.equals("INPROGRESS");
        if(old_status.equals("INPROGRESS"))
            return new_status.equals("TOBEREVISED") || new_status.equals("DONE");
        if(old_status.equals("TOBEREVISED"))
            return new_status.equals("INPROGRESS") || new_status.equals("DONE");
        else return false;
    }


}
