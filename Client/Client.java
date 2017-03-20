/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Client;

import POP3_ClientServer.Server.ServerThread;
import POP3_ClientServer.common.MessageReseau;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lafay
 */
public class Client implements Runnable{
    
    Socket socket;
    InputStream input;
    
    OutputStream output;
    
    ClientEtat etat;
    
    ClientCommandes lastCommand;
    
    Scanner sc;
    
    String user;
    String pass;

    boolean exit = false;
    

    
    public static final String OK = "+OK";
    public static final String ERR = "-ERR";
    public static String sourceMailFolder =
            FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/MailsPOP3/";
    
    public Client(String adress, int port){

        lastCommand = ClientCommandes.EMPTY;
        
        sc = new Scanner (System.in);
        try {
            socket = new Socket(adress, port);

            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        etat = ClientEtat.ATTENTE;
    }
    
    public synchronized void run(){
        
        
        while(!exit){
            
            //attete de la reponse associée            
            System.out.println("en attente du serveur...");
            MessageReseau message  = MessageReseau.readMessage(input);
            System.out.println("[réponse du serveur reçue]:" + message);
            
            switch (message.command){

                case OK:

                    switch (etat){
                        
                        case CLOSED:
                            switch(lastCommand){
                                case QUIT:
                                    handleQuit(message);
                                    break;
                                default:
                                     etat = ClientEtat.ATTENTE;
                            }                      

                            break;

                        case ATTENTE:
                            switch(lastCommand){
                                case QUIT:
                                    handleQuit(message);
                                    break;
                                default:
                                    etat = ClientEtat.ACTIF;
                                    
                                    System.out.println("Saisir votre nom utilisateur");

                                    //envoi APOP
                                    this.user = sc.nextLine();
                                    MessageReseau toSend= new MessageReseau("APOP",user);
                                    toSend.sendMessage(output);
                                    lastCommand = ClientCommandes.APOP;
                            }          

                            break;

                        case ACTIF:

                            etat = ClientEtat.CONNECTED;
                            sendUserMessage();
                            break;


                        case CONNECTED:

                            switch(lastCommand){
                                case STAT:
                                    this.handleStat(message);
                                    break;
                                case RETR:
                                    this.handleRetr(message);
                                    break;
                                case QUIT:                                    
                                    this.handleQuit(message);
                                    break;
                            }
                            
                            sendUserMessage();

                            break;
                    }
                    break;

                case ERR:
                    MessageReseau messageToSend;

                    switch (etat){

                        case CLOSED:
                            System.out.println(message);
                            break;

                        case ATTENTE:
                            System.out.println(message);
                            System.out.println("[Fermeture de la connection...]");
                            etat = ClientEtat.CLOSED;
                            messageToSend = new MessageReseau("QUIT");
                            messageToSend.sendMessage(output);
                            lastCommand = ClientCommandes.QUIT;
                            break;

                        case ACTIF:
                            System.out.println(message);
                            System.out.println("[Nouvelle tentative...]");
                            messageToSend = new MessageReseau("APOP", this.user);
                            messageToSend.sendMessage(output);
                            lastCommand = ClientCommandes.APOP;
                            break;

                        case CONNECTED:
                            System.out.println(message);
                            
                            sendUserMessage();
                            break;

                        default:
                            System.out.println("Erreur non prise en compte : " + message);
                    }
                    break;

                default:
                    System.out.println("message inconu");
            }
        }
    }
    
   private void sendUserMessage(){
       System.out.println("Commande:");
       
       String[] splitedmess = sc.nextLine().split(" ");
       
       String comm = splitedmess[0];
       
       String[]param = new String [splitedmess.length-1];
       for (int i = 1; i< splitedmess.length; i++){
           param[i-1] = splitedmess[i];
       }
       
       MessageReseau mess = new MessageReseau(comm,param);
       mess.sendMessage(output);
       
       lastCommand = ClientCommandes.getValue(comm);       
   }
    
    private void handleQuit(MessageReseau message){
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        exit = true;
    }

    /***
     * Gestion du RETR + Enregistrement des messages sur le poste client
     * @param message
     */
    private void handleRetr(MessageReseau message){


        //Si le dossier n'existe pas, on le crée
        if (!Files.exists(Paths.get(sourceMailFolder))) {

            File theDir = new File(sourceMailFolder);
            theDir.mkdir();
        }

        //On enregistre le message en local
        String mail = "temp"; //message.args.toString(); 
        System.out.println("OK RETR reçu:" + mail);
        
        String finalFileName = "unknownID" ;

        BufferedWriter writer = null;
        File mailFile = new File(sourceMailFolder + mail + ".txt");
        try {
            // This will output the full path where the file will be written to...
            System.out.println(mailFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(mailFile));
            String endMessage = "";
            while(!endMessage.equals(". ")){
                MessageReseau mg = MessageReseau.readMessage(input);
                endMessage = mg.toString();
                writer.write(mg.toString()+"\r\n");
                System.out.println(mg.toString());
                if(mg.toString().contains("Message-ID")){
                    finalFileName = mg.toString().split(":")[1];
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        

        File newFile = new File(sourceMailFolder + finalFileName + ".txt");
        
        System.out.println("fichier renomé:"+mailFile.renameTo(newFile));
            
    }
    
    private void handleStat(MessageReseau message){
        System.out.println(message);
    }
    
    public static void main(String [] args){
        Client c = new Client("localhost", 5555);
        c.run();
    }   
}
