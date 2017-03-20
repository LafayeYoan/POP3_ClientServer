/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.Server;

import POP3_ClientServer.common.EMail;
import POP3_ClientServer.common.MailFileManager;
import POP3_ClientServer.common.MessageReseau;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lafay
 */
public class ServerThread implements Runnable{
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private String message;
    private ServerEtat etat;
    
    private static final String APOP = "APOP";
    private static final String STAT = "STAT";
    private static final String RETR = "RETR";
    private static final String QUIT = "QUIT";
    
    private static final String OK = "+OK ";
    private static final String ERR = "-ERR ";
    private static final String ENDLINE = "\r\n";
    
    private String user;
    MailFileManager userManager;
    
    public ServerThread (Socket socket){
        try {
            this.socket = socket;
            input = socket.getInputStream();
            output = socket.getOutputStream();
            etat = ServerEtat.CLOSED;
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    public void run() {
        System.out.println("Un client s'est connecte.");
        etat = ServerEtat.AUTHORIZATION;
        //message de bienvenue
        message = OK + "Bienvenue sur le serveur POP3, nous vous souhaitons un agreable sejour."+ENDLINE;
        try {
            output.write(message.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        boolean exit = false;
        while(!exit){
            MessageReseau messageReceived = MessageReseau.readMessage(input);
            
            MessageReseau messageToSend;
            switch (messageReceived.command){
                
                case APOP:
                    if( etat == ServerEtat.AUTHORIZATION){
                        if(messageReceived.args==null){
                            messageToSend = new MessageReseau(ERR,"Aucun utilisateur reçu, le format de la commande est 'APOP utilisateur'");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        user = messageReceived.args[0];
                        
                        userManager = new MailFileManager(user);
                        if(!userManager.userExist()){
                            messageToSend = new MessageReseau(ERR,"Utilisateur invalide");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        
                        messageToSend = new MessageReseau(OK,"L'utilisateur '"+user+"' s'est connecte.");
                        messageToSend.sendMessage(output);
                        
                        //change server state
                        etat = ServerEtat.TRANSACTION;
                        break;
                    }
                    
                    messageToSend = new MessageReseau(ERR,"Le serveur est deja connecte");
                    messageToSend.sendMessage(output);
                    //verification de la chaine de securité
                    //pas de verification, return ok                    
                    break;
                    
                case STAT:
                    if(etat == ServerEtat.TRANSACTION){
                        messageToSend = new MessageReseau(OK,userManager.getNbMailsNotDeleted()+"", userManager.getSizeDepot()+"");
                        messageToSend.sendMessage(output);
                        break;
                    }
                    messageToSend = new MessageReseau(ERR,"Action impossible dans cet etat");
                    messageToSend.sendMessage(output);
                    break;
                    
                case RETR:
                    if(etat == ServerEtat.TRANSACTION){
                        if(messageReceived.args == null){
                            messageToSend=new MessageReseau(ERR,"Aucun parametres detectes");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        int messageNumber;
                        try{
                            messageNumber = Integer.parseInt(messageReceived.args[0]);
                        }catch(Exception e){
                            messageToSend=new MessageReseau(ERR,"Le numero du message doit etre un nombre");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        
                        if(messageNumber <1 ){
                            messageToSend=new MessageReseau(ERR,"Le numero du message doit etre >= 1");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        if(messageNumber >userManager.getNbMailsNotDeleted() ){
                            messageToSend=new MessageReseau(ERR,"Le message avec ce numero n'existe pas.");
                            messageToSend.sendMessage(output);
                            break;
                        }
                        EMail email = userManager.getMessage(messageNumber);
                        
                        messageToSend=new MessageReseau(OK,email.getSize()+"");
                        messageToSend.sendMessage(output);
                        messageToSend=new MessageReseau("",email.getEmailToSend()+"");
                        messageToSend.sendMessage(output);
                        messageToSend=new MessageReseau(".",null);
                        messageToSend.sendMessage(output);
                        break;
                    }
                    messageToSend = new MessageReseau(ERR,"Action impossible dans cet etat");
                    messageToSend.sendMessage(output);
                    break;

                case QUIT:

                    //mise a jour du cache et arret du socket
                    try {
                        messageToSend=new MessageReseau(OK,"Fermeture OK");
                        messageToSend.sendMessage(output);
                        socket.close();
                        exit = true;
                    } catch (IOException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    break;
                default:
                    switch(etat){
                        case AUTHORIZATION:
                            messageToSend = new MessageReseau(ERR, "Le serveur attend une commande APOP.");
                            break;
                            
                        case TRANSACTION:
                            messageToSend = new MessageReseau(ERR, "Le serveur attend une commande STAT, RETR ou QUIT.");
                            break;
                        
                        default:
                            messageToSend = new MessageReseau(ERR, "Message non gere");
                    }
                    messageToSend.sendMessage(output);
                    System.out.println("message non gere");
            }
        }
        
        
        
        
    }
    
}
