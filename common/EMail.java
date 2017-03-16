/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package POP3_ClientServer.common;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;
import java.util.Observable;

/**
 *
 * @author lafay
 */
public class EMail {

    /*****************************************
     * GETTERS & SETTERS
     **************************************************/
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage_id() {
        return message_id;
    }
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    public boolean isDelete() {
        return delete;
    }
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    /*****************************************
     * PROPERTY GETTERS
     **************************************************/
    public StringProperty getFromProperty() { return new SimpleStringProperty(getFrom()); }
    public StringProperty getObjetProperty() { return new SimpleStringProperty(getSubject()); }

    String from;
    String to;
    String subject;
    Date date;
    String message_id;
    
    String body;
    
    String toString;
    
    boolean delete;
    int size;
        
    public EMail(String from, String to, String subject, Date date, String message_id, String body, int size){
        
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.date = date;
        
        this.message_id = message_id;
        this.body = body;
        
        this.delete = false;
        this.size = size;
    }
    
    public String getEmailToSend(){
        StringBuilder str = new StringBuilder();
        str.append("From:"+this.from+"\r\n");
        str.append("To:"+this.to+"\r\n");
        str.append("Subject:"+this.subject+"\r\n");
        str.append("Message-ID:"+this.message_id+"\r\n");
        str.append(this.body+"\r\n");
        return str.toString();
    }
    
    
}
