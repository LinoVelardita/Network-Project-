import java.io.Serializable;

public class Address implements Serializable {

    //la Classe Address nel Server è utilizzata per memorizzare
    //gli ultimi indirizzi usati, mentre è usato all'interno
    //di un progetto per memorizzare l'indirizzo di multicast per la chat

    int port;
    int[] ip = new int[4];

    public Address(){

    }

    public Address(int[] ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public void setPort(int port){
        this.port = port;
    }

    //prepara la porta per un nuovo multicast
    public void setPort(){
        port++;
    }

    //prepara l'ip per un nuovo multicast
    //l'ip è generato all'interno della classe MainServer nel  metodo generateIp()
    public void setIp(int[] ip){
        this.ip = ip;
    }

    public int[] getIp(){
        return ip;
    }

    public int getPort(){
        return port;
    }


}
