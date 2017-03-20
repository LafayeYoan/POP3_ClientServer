/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Client;

import POP3_ClientServer.Server.ServerThread;
import POP3_ClientServer.common.MessageReseau;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
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
    
    ClientCommandes lastCommand;

    String user;
    String pass;
    boolean exit = false;
    boolean awnserWaited = true;
    
    LinkedList <MessageReseau> waitingCommands = new LinkedList<>();

    
    public static final String OK = "+OK";
    public static final String ERR = "-ERR";
    
    public Client(String adress, int port, String user, String pass){
        this.user = user;
        this.pass = pass;
        
        try {
            socket = new Socket(adress, port);

            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        etat = ClientEtat.ATTENTE;
        this.run();
    }
    
    private void run(){
        
        
        while(!exit){
            if(awnserWaited){

                MessageReseau message  = MessageReseau.readMessage(input);
            
                awnserWaited = false;
            switch (message.command){
                case OK:
                    
                    switch (etat){
                        case CLOSED:
                            switch(lastCommand){
                                case QUIT:
                                    handleQuit();
                                    break;
                                default:
                                     etat = ClientEtat.ATTENTE;
                            }                      
                           
                            break;
                            
                        case ATTENTE:
                            switch(lastCommand){
                                case QUIT:
                                    handleQuit();
                                    break;
                                default:
                                    etat = ClientEtat.ACTIF;
                            
                                    //envoi APOP
                                    MessageReseau toSend= new MessageReseau("APOP",user);
                                    toSend.sendMessage(output);
                                    awnserWaited = true;
                                    lastCommand = ClientCommandes.QUIT;
                            }          
                            
                            break;
                            
                        case ACTIF:
                            
                            etat = ClientEtat.CONNECTED;
                            
                            
                        case CONNECTED:
                            
                            switch(lastCommand){
                                case STAT:
                                    this.handleStat();
                                    break;
                                case RETR:
                                    this.handleRetr();
                                    break;
                                case QUIT:                                    
                                    this.handleQuit();
                                    break;
                            }
                                 
                            break;
                    }
                    break;
                    
                case ERR:
                    MessageReseau messageToSend;

                    switch (etat){

                        case CLOSED:
                            System.out.println(message.args);
                            break;

                        case ATTENTE:
                            System.out.println(message.args);
                            System.out.println("[Fermeture de la connection...]");
                            etat = ClientEtat.CLOSED;
                            messageToSend = new MessageReseau("QUIT");
                            messageToSend.sendMessage(output);
                            lastCommand = ClientCommandes.QUIT;
                            break;

                        case ACTIF:
                            System.out.println(message.args);
                            System.out.println("[Nouvelle tentative...]");
                            messageToSend = new MessageReseau("APOP", this.user);
                            messageToSend.sendMessage(output);
                            lastCommand = ClientCommandes.APOP;
                            break;

                        case CONNECTED:
                            System.out.println(message.args);
                            break;

                        default:
                            System.out.println("Erreur non prise en compte : " + message.args);
                    }
                    break;
                    
                default:
                    System.out.println("message inconu");
            }
            

            //message OK envoi suite
            if(waitingCommands.isEmpty()){
                break;
            }
            waitingCommands.getFirst().sendMessage(output);
            waitingCommands.removeFirst();
            awnserWaited = true;
            break;
                    
                    
                    
                    
                
            }
            
        }
        
        
    }
    
    private void handleQuit(){
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        exit = true;
    }
    
    private void handleRetr(){
        
    }
    
    private void handleStat(){
        
    }
    
    
    
    
}
