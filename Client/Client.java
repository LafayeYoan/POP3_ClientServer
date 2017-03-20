/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Client;

import POP3_ClientServer.Server.ServerThread;
import POP3_ClientServer.common.MessageReseau;
import sun.plugin2.message.Message;

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
            
            //En attente du serveur
            System.out.println("En attente du serveur...");
            MessageReseau message  = MessageReseau.readMessage(input);
            System.out.println("[Réponse du serveur reçue]:" + message);

            /* -------------- CAS VALIDES -------------- */
            if(message.command.equals(OK)) {

                switch (etat) {
                    case CLOSED:
                        switch (lastCommand) {
                            case QUIT:
                                handleQuit();
                                break;
                            default:
                                etat = ClientEtat.ATTENTE;
                        }

                        break;

                    case ATTENTE:
                        switch (lastCommand) {
                            case QUIT:
                                handleQuit();
                                break;
                            default:
                                etat = ClientEtat.ACTIF;
                                System.out.println("Saisir votre nom utilisateur");
                                this.user = sc.nextLine();
                                sendAPOP();
                        }
                        break;

                    case ACTIF:

                        etat = ClientEtat.CONNECTED;

                        System.out.println("--------------------------------");
                        System.out.println("Les commandes possibles sont : ");
                        System.out.println("[STAT]");
                        System.out.println("[RETR x] avec x le numero du message");
                        System.out.println("[QUIT]");
                        System.out.println("--------------------------------");

                        sendUserMessage();
                        break;


                    case CONNECTED:

                        switch (lastCommand) {
                            case QUIT:
                                this.handleQuit();
                                continue;
                            case STAT:
                                this.handleStat(message);
                                break;
                            case RETR:
                                this.handleRetr();
                                break;
                        }
                        sendUserMessage();
                        break;
                }
            }


            /* -------------- CAS D'ERREURS -------------- */
            if(message.command.equals(ERR)) {

                MessageReseau messageToSend;

                switch (etat) {

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
                        sendAPOP();
                        break;

                    case CONNECTED:
                        System.out.println(message);
                        sendUserMessage();
                        break;

                    default:
                        System.out.println("Erreur non prise en compte : " + message);
                }
                break;
            }
        }
    }

    /***
     * Envoi un MessageReseau au server en fonction de la commande donnée par l'utilisateur
     */
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

    /***
     * Gestion du QUIT
     */
    private void handleQuit(){
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

        exit = true;
    }

    /***
     * Gestion du RETR
     * + Enregistrement des messages sur le poste client
     */
    private void handleRetr(){

        String filename = "";
        String currentMsg = "";
        StringBuilder text = new StringBuilder();

        //Si le dossier pour enregistrer les mails n'existe pas, on le crée
        if (!Files.exists(Paths.get(sourceMailFolder))) {
            File theDir = new File(sourceMailFolder);
            theDir.mkdir();
        }

        System.out.println("OK RETR reçu : ");
        System.out.println("--------------------------------");

        //Enregistrement en local + affichage
        while(!currentMsg.equals(". ")){

            MessageReseau mg = MessageReseau.readMessage(input);

            currentMsg = mg.toString();
            text.append(currentMsg + "\r\n");
            System.out.println(currentMsg);

            //Extraction de l'id du message
            if(currentMsg.contains("Message-ID")){
                filename = ((currentMsg.split(":")[1]).split("@")[0]).split("<")[1];

            }
        }

        //Ecriture dans le fichier
        try {

            File file = new File(sourceMailFolder + filename + ".txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            writer.append(text);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("--------------------------------");
    }
    
    private void handleStat(MessageReseau message){
        //do nothing
    }

    /***
     * Gestion du APOP
     */
    private void sendAPOP() {
        new MessageReseau("APOP", this.user).sendMessage(output);
        lastCommand = ClientCommandes.APOP;
    }
    
    public static void main(String [] args){
        Client c = new Client("localhost", 5555);
        c.run();
    }   
}
