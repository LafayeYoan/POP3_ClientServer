/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Client;

import POP3_ClientServer.common.MessageReseau;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lafay
 */
public class Client {
    
    Socket socket;
    InputStream input;
    
    OutputStream output;
    
    ClientEtat etat;
    
    String lastCommand;
    
    public static final String OK = "+OK";
    public static final String ERR = "-ERR";
    
    public Client(String adress, int port){
        
        try {
            socket = new Socket(adress, port);

            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        etat = ClientEtat.CLOSED;
        this.run();
    }
    
    private void run(){
        boolean exit = false;
        
        while(!exit){
            MessageReseau message  = MessageReseau.readMessage(input);
            
            
            switch (message.command){
                case OK:
                    
                    switch (etat){
                        case CLOSED:
                            etat = ClientEtat.ATTENTE;
                            break;
                            
                        case ATTENTE:
                            etat = ClientEtat.ACTIF;
                            
                            //envoi APOP
                            break;
                            
                        case ACTIF:
                            
                            etat = ClientEtat.CONNECTED;
                            break;
                            
                        case CONNECTED:
                            
                            
                            //action en fonction de la derniere comande envoiyée
                            
                            
                            
                            break;
                    }
                    
                    
                    break;
                    
                case ERR:
                    break;
                    
                default:
                    System.out.println("message inconu");
            }
            
        }
        
        
    }
    
}
