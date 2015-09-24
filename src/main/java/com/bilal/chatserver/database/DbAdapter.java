/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bilal.chatserver.database;

import com.bilal.chatserver.model.Message;
import com.bilal.chatserver.model.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author baloch
 */
@ApplicationScoped
public class DbAdapter {
    
    public DbAdapter() {
        //extend this class to main database class which provides database methods.
    }
    
    public Set<User> getFriends(int id) {
        Set<User> friends = new HashSet<>();
        // fetch friend list from database...
        
        for(int i=1; i<10; i++) {
            if(i == id) continue;
            User user = new User();
            user.setId(i);
            user.setName("User"+i);
            user.setStatus("off");
            friends.add(user);
        }
        
        return friends;
    }
    
    public boolean updateStatus(int id, String status) {
        //query to update status e.g on/off
        //return true on stauts update otherwise false
        return true;
    }
    
    public Set<Message> getMessages(int senderId, int receiverId) {
        Set<Message> messages = new HashSet<>();
        //query to fetch all conversation between sender and receiver
        return messages;
    }
    
    public boolean saveMessage(Set<Message> messages) {
        //query to save list of messages
        //return true on save, otherwise false
        return true;
    }
    
    public boolean saveMessage(int senderId, int receiverId, String message, String date) {
        //query to save single messages
        //return true on save, otherwise false
        return true;
    }
}
