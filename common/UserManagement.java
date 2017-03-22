/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 *
 * @author lafay
 */
public class UserManagement {
    private final static String USER_FILE = "userFile.sav";
    
    static Map<String, String> users;
    static{
        loadUser();
    }
    
    public static boolean isUserOk(String user, long timestamp, String receivedHash){
        return receivedHash.equals(hashMD5("<"+timestamp+">"+users.get(user)));
    }
    
    public static void createUser(String user, String password){
        users.put(user, password);
        saveUser();
    }
    
    private static void saveUser(){
        
        try {
            FileOutputStream  saveFile = new FileOutputStream(USER_FILE);
            ObjectOutputStream save = new ObjectOutputStream(saveFile);
            
            save.writeObject(users);
            
            save.close();
            saveFile.close();
            
        } catch (Exception ex) {
            System.out.println("User cannot be saved:"+ex.getMessage());
        }
        
        
    }
    
    private static void loadUser(){
        try{
            FileInputStream saveFile = new FileInputStream(USER_FILE);
            ObjectInputStream save = new ObjectInputStream(saveFile);
            
            users = (Map<String, String>)save.readObject();
            
            save.close();
            saveFile.close();
        }catch(Exception ex){
            users = new HashMap<String,String>();
        }
    }
    
    public static String hashMD5(String input){
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            return new String(digest.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static Map<String, String> getUsers(){
        return users;
    }
}
