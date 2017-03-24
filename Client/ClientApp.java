package POP3_ClientServer.Client;

/***
 * LAFAYE DE MICHEAUX Yoan - LHOPITAL Sacha
 */
public class ClientApp {

    public static void main(String[] args) {


        Client c = new Client("localhost", 5555);
        Thread clientThread =  new Thread(c);
        clientThread.start();
        
        

    }
}
