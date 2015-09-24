/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bilal.chatserver.model;

/**
 *
 * @author baloch
 */
public class Message {
    private User sender;
    private User receiver;
    private String message;
    private String date;

    public Message() {
    }
    
    public User getSender() {
        return sender;
    }
    
    public User getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
    
    public String getDate() {
        return date;
    }

    public void setSender(User user) {
        this.sender = user;
    }
    
    public void setReceiver(User user) {
        this.receiver = user;
    }

    public void setMessage(String str) {
        this.message = str;
    }
    
    public void setDate(String str) {
        this.date = str;
    }
}
