/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Server;

import POP3_ClientServer.common.Message;
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
public class ServerThread implements Runnable{
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private String message;
    private ServerEtat etat;
    
    private static final String APOP = "APOP";
    private static final String STAT = "STAT";
    private static final String RETR = "RETR";
    private static final String QUIT = "QUIT";
    
    private static final String OK = "+OK ";
    private static final String ERR = "-ERR ";
    private static final String ENDLINE = "\r\n";
    public ServerThread (Socket socket){
        try {
            this.socket = socket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
            etat = ServerEtat.CLOSED;
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    public void run() {
        System.out.println("Un client s'est connecté.");
        etat = ServerEtat.AUTHORIZATION;
        //message de bienvenue
        message = OK + "Bienvenue sur POP3 Sacha, nous vous souhaitons un agréable séjour."+ENDLINE;
        try {
            output.write(message.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean exit = false;
        while(!exit){
            Message message = Message.readMessage(input);
            
            switch (message.command){
                case APOP:
                    
                    break;
                case STAT:
                    break;
                case RETR:
                    break;
                case QUIT:
                    break;
                default:
                    System.out.println("message non géré");
            }
        }
        
        
        
        
    }
    
}
