/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager de lecture et écriture du fichier de sauvegarde des mails
 * @author lafay
 */
public class MailFileManager {
    
    public static String sourceMailFolder = "src\\POP3_ClientServer\\Data"; //à vérifier
    List<EMail> userMail;
    
    public MailFileManager(String user){
        userMail = this.getUserEMail(user);
    }
    
    public boolean userExist(){
        return userMail !=null;
    }
    /***
     * Lecture du fichier de l'utilisateur
     * @param user
     * @return 
     */
    private List<EMail> getUserEMail(String user){
        ArrayList<EMail> emails = new ArrayList<EMail>();
        File f = new File(sourceMailFolder + "\\" + user + ".txt");
        int byteRead = 0;
        StringBuilder sb = new StringBuilder();
        boolean read = true;
        
        try {
            
            if(f.exists()) {
                //Lecture
                FileReader fReader = new FileReader(sourceMailFolder + "\\" + user + ".txt");
                while(read){
                    
                    byteRead = fReader.read();

                    if(byteRead == -1){
                        read = false;
                        continue;
                    }

                    sb.append((char)byteRead);
                }
                
                String [] splitedMessages = sb.toString().split("\r\n.\r\n");
                
                for(int i = 0; i < splitedMessages.length; i++) {
                    // public EMail(String from, String to, String subject, Date date, String message_id, String body, int size)
                    
                    String [] splitedMessage = splitedMessages[i].split("\r\n\r\n");
                    
                    String [] splitedHeader = splitedMessage[0].split("\r\n");
                    String from = getValue("From",splitedHeader);
                    String to = getValue("To",splitedHeader);
                    String subject = getValue("Subject",splitedHeader);
                    String date = getValue("Date",splitedHeader);
                    String message_id = getValue("Message-ID",splitedHeader);
                    String body = splitedMessage[1];
                    
                    
                    EMail aMail = new EMail(from, to, subject, new java.util.Date(date), message_id, body, splitedMessages[i].length());
                    emails.add(aMail);
                    
                    
                }
                fReader.close();

            } else {
                //Fichier user non valide
                        return null;
            }
        } catch(Exception e) {
            System.err.println("Impossible d'ouvrir le fichier associé : " + e.getMessage());
        }
        
        return emails;
    }
    
    public List<EMail> getEmails(){
        return this.userMail;
    }
    
    private List<EMail> getNotDeletedMail(){
        List<EMail> list= new ArrayList<>();
        for(EMail e : this.userMail){
            if(!e.delete){
                list.add(e);
            }
        }
        return list;
        
    }
    
    public int getNbMailsNotDeleted(){
        return getNotDeletedMail().size();
    }
    
    public int getSizeDepot(){
        int size = 0;
        for(EMail e : this.getNotDeletedMail()){
                size+=e.size;
        }
        return size;
    }
    
    public EMail getMessage(int number){
        if(number<1 && number >= getNbMailsNotDeleted()){
            return null;
        }
        return this.getNotDeletedMail().get(number-1);
        
    }
    
    /**
     * REturn the value after the given key
     * ex: datas = [From: blalbla,To: toto]
     * getValue("From", datas) return blalbla
     * @param key
     * @param datas
     * @return 
     */
    private static String getValue(String key, String [] datas){
        String val = "";
        if(key == null){
            //return body
            
            return "";
        }
        for(int i = 0 ; i< datas.length; i++){
            if(datas[i].startsWith(key)){
                //return the second part of the string 
                return datas[i].substring(datas[i].indexOf(":")+1);
            }
        }
        
        return val;
    }
}
