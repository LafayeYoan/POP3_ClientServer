/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gère la communication Réseau Client-Serveur
 * @author lafay
 */
public class MessageReseau {
    public String command;
    public String args[];
    

    public MessageReseau(String command, String ... args){
        this.command = command;
        this.args = args;
    }

    /***
     * Décortique les message transmit par sockets
     * @param input
     * @return
     */
    public static MessageReseau readMessage(InputStreamReader input){
        
        int byteRead = 0;
        StringBuilder sb = new StringBuilder();
        boolean read = true;
        
        while(read){
            try {
                byteRead = input.read();
            } catch (IOException ex) {
                Logger.getLogger(MessageReseau.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if(byteRead == -1){
                read = false;
                continue;
            }
            
            sb.append((char)byteRead);
            
            // \r\n test
            if(sb.lastIndexOf("\r\n")!=-1){
                read = false;
            }
        }
        
        String [] splitedMessage = sb.toString().replaceAll("\r\n","").split(" ");
        String command = splitedMessage[0];
        String [] args = splitedMessage.length>1 ? Arrays.copyOfRange(splitedMessage, 1, splitedMessage.length) : null;

        return new MessageReseau(command,args);
    }
    
    public  void sendMessage(OutputStreamWriter output){
        String sMessage = this.command;
        if(this.args!=null){
            for (int i=0; i< this.args.length; i++ ){
                sMessage += " "+ this.args[i];
            }
        }
        
        sMessage += "\r\n";
        
        try {
            output.write(sMessage.toCharArray());
            output.flush();
        } catch (IOException ex) {
            Logger.getLogger(MessageReseau.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(this.command + " ");
        if(args!=null){
            for (String s : args) {
                sb.append(s + " ");
            }
        }
        return sb.toString();
    }
    
}
