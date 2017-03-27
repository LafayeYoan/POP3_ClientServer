/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Client;

import POP3_ClientServer.common.MessageReseau;
import POP3_ClientServer.common.UserManagement;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;

/**
 *
 * @author LAFAYE DE MICHEAUX Yoan - LHOPITAL Sacha
 */
public class Client implements Runnable{
    
    Socket socket;
    Scanner sc;
    InputStreamReader input;
    OutputStreamWriter output;

    ClientEtat etat;
    ClientCommandes lastCommand;

    String user;
    String pass;
    long timestamp;
    boolean exit = false;
    int apopTentatives = 0;

    public static final String OK = "+OK";
    public static final String ERR = "-ERR";
    public static String sourceMailFolder =
            FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + "/MailsPOP3/";
    
    public Client(String adress, int port){

        lastCommand = ClientCommandes.EMPTY;
        
        sc = new Scanner (System.in);
        try {
            socket = SSLSocketFactory.getDefault().createSocket(adress, port);
            ((SSLSocket)socket).setEnabledCipherSuites(new String[]{"SSL_DH_anon_WITH_DES_CBC_SHA"});

            input = new InputStreamReader(socket.getInputStream(),"UTF-8");
            output = new OutputStreamWriter(socket.getOutputStream(),"UTF-8");
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

                /* Gestion du QUIT */
                if(lastCommand.equals(ClientCommandes.QUIT)) {

                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    exit = true;
                    continue;

                } else {

                    switch (etat) {

                        case CLOSED:
                            etat = ClientEtat.ATTENTE;
                            break;

                        case ATTENTE:
                            etat = ClientEtat.ACTIF;
                            timestamp = Long.parseLong(message.args[0].trim().replace("<", "").replace(">", ""));
                            sendAPOP(timestamp);
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
                                case RETR:
                                    this.handleRetr();
                                    break;
                            }
                            sendUserMessage();
                            break;
                    }
                }
            }

            /* -------------- CAS D'ERREURS -------------- */
            if(message.command.equals(ERR)) {

                switch (etat) {

                    case CLOSED:
                        //do nothing
                        break;

                    case ATTENTE:
                        System.out.println("[Fermeture de la connection...]");
                        etat = ClientEtat.CLOSED;
                        sendQuit();
                        break;

                    case ACTIF:
                        if(apopTentatives < 5) {
                            System.out.println("[Nouvelle tentative APOP n°" + apopTentatives + "]");
                            sendAPOP(timestamp);
                        } else {
                            System.out.println("[Echec APOP - Exit programme]");
                            sendQuit();
                        }
                        break;

                    case CONNECTED:
                        sendUserMessage();
                        break;

                    default:
                        System.out.println("Erreur non prise en compte !");
                }
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

    /***
     * Tentative d'envoi d'un APOP
     * @param timestamp
     */
    private void sendAPOP(long timestamp) {
        System.out.println("Saisir votre nom utilisateur (ou QUIT pour fermer le programme)");
        user = sc.nextLine();

        if(user.equals("QUIT")) {
            sendQuit();
            return;
        }

        System.out.println("Saisir votre mot de passe");
        pass = sc.nextLine();

        new MessageReseau("APOP", this.user, UserManagement.hashMD5("<"+timestamp+">"+pass)).sendMessage(output);
        lastCommand = ClientCommandes.APOP;
        apopTentatives++;
    }

    /***
     * Envoi un QUIT
     */
    private void sendQuit() {
        new MessageReseau("QUIT").sendMessage(output);
        lastCommand = ClientCommandes.QUIT;
    }
    
    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        
        System.out.println("Quelle est l'adresse du serveur?");
        String adress = sc.nextLine();
        System.out.println("Quel est le port du serveur?");
        int port = Integer.parseInt(sc.nextLine());
        Client c = new Client(adress, port);
        c.run();
    }   
}
