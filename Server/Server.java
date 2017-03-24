/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Server;

import POP3_ClientServer.common.UserManagement;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;

/**
 *
 * @author lafay
 */
public class Server {
    
    private static ServerSocket serverSocket;
    private static int PORT = 5555;
    
    public static void main (String [] args){
        
        //gestion des utilisateur
        Scanner sc = new Scanner(System.in);
        Map<String, String> users = UserManagement.getUsers();
        System.out.println("Les utilisateurs suivants sont inscrits:[user:pass]:");
        for(Entry<String,String> e : users.entrySet()){
            System.out.println("["+e.getKey()+":"+e.getValue()+"]");
        }
        System.out.println("Voulez vous ajouter un utilisateur(yes/no)?");
        String reponse = sc.nextLine();
        if(reponse.equals("yes")){
            System.out.println("Entrez un nom d'utilisateur:");
            String user = sc.nextLine();
            System.out.println("Entrez un mot de passe:");
            String pass = sc.nextLine();
            
            UserManagement.createUser(user, pass);
        }
        
        System.out.println("Serveur démarré");
        
        try {
            //Socket Sécurisée
            serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(PORT);

            //Setup Cyphersuite
            ((SSLServerSocket)serverSocket).setEnabledCipherSuites(new String[]{"SSL_DH_anon_WITH_DES_CBC_SHA"});

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
