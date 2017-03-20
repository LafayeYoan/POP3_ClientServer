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
    public static String sourceMailFolder =
            FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/MailsPOP3/";
    
    public Client(String adress, int port, String user, String pass){
        this.user = user;
        this.pass = pass;
        lastCommand = ClientCommandes.EMPTY;
        
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
                            
                                    //envoi APOP
                                    MessageReseau toSend= new MessageReseau("APOP",user);
                                    toSend.sendMessage(output);
                                    awnserWaited = true;
                                    lastCommand = ClientCommandes.APOP;
                            }          
                            
                            break;
                            
                        case ACTIF:
                            
                            etat = ClientEtat.CONNECTED;
                            
                            
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
            switch(waitingCommands.getFirst().command){
                case "APOP":
                    lastCommand = ClientCommandes.APOP;
                    break;
                case "RETR":
                    lastCommand = ClientCommandes.RETR;
                    break;
                case "STAT":
                    lastCommand = ClientCommandes.STAT;
                    break;
                case "QUIT":
                    lastCommand = ClientCommandes.QUIT;
                    break;
                    
            }
            waitingCommands.removeFirst();
            awnserWaited = true;
            break;
                    
                    
                    
                    
                
            }
            
        }
        
        
    }
    public void addRetr(int numMessage){
        waitingCommands.add(new MessageReseau("RETR", numMessage+""));
    }
    
    public void addStat(){
         waitingCommands.add(new MessageReseau("STAT"));
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
        String mail = "VALENDURE"; //message.args.toString(); //todo : découper les args attention le serveur renvoi plusieurs messages réseaux...
        System.out.println("OK RETR reçu:" + mail);

        BufferedWriter writer = null;

        try {
            File mailFile = new File(sourceMailFolder + mail + ".txt"); //todo mettre id du mail en nom de fichier

            // This will output the full path where the file will be written to...
            System.out.println(mailFile.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(mailFile));
            writer.write("Hello world!");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void handleStat(MessageReseau message){
        System.out.println("OK STAT reçu:"+message.args);
    }
    
    
    
    
}
