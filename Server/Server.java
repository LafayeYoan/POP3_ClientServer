/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lafay
 */
public class Server {
    
    private static ServerSocket serverSocket;
    private static int PORT = 5555;
    
    public static void main (String [] args){
        
        try {
            serverSocket = new ServerSocket(PORT);
            while(true){
                Socket socket = serverSocket.accept();
                
                Thread t = new Thread(new ServerThread(socket));
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
}
