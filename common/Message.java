/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lafay
 */
public class Message {
    public String command;
    public String args[];
    

    public Message(String command, String ... args){
        this.command = command;
        this.args = args;
    }
    
    public static Message readMessage(InputStream input){
        
        int byteRead = 0;
        StringBuilder sb = new StringBuilder();
        boolean read = true;
        
        while(read){
            try {
                byteRead = input.read();
            } catch (IOException ex) {
                Logger.getLogger(Message.class.getName()).log(Level.SEVERE, null, ex);
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
        
        // convert datas to byteArray
//        byte [] byteDatas = new byte[datas.size()];
//        for(int i=0; i< byteDatas.length; i++){
//            byteDatas[i] = datas.get(i).byteValue();
//        }
        // split datas
        
        System.out.println(sb.toString());
        
        String [] splitedMessage = sb.toString().replaceAll("\r\n","").split(" ");
        String command = splitedMessage[0];
        String [] args = splitedMessage.length>1 ? Arrays.copyOfRange(splitedMessage, 1, splitedMessage.length) : null;
        
        return new Message(command,args);
    }
}
