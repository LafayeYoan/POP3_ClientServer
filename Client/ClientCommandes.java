package POP3_ClientServer.Client;

/**
 * Created by Sachouw on 20/03/2017.
 */
public enum ClientCommandes {

    EMPTY,
    STAT,
    RETR,
    APOP,
    QUIT;
    
    public static ClientCommandes getValue(String in){
        
        if(in.equals(STAT.toString())) {
            return STAT;
        }
        
        if(in.equals(RETR.toString())) {
            return RETR;
        }
        
        if(in.equals(APOP.toString())) {
            return APOP;
        }
        
        if(in.equals(QUIT.toString())) {
            return QUIT;
        }
        
        return EMPTY;
    }

}


