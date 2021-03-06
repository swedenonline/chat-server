/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bilal.chatserver.server;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import com.bilal.chatserver.model.User;

/**
 *
 * @author baloch
 */
@ApplicationScoped
@ServerEndpoint("/start")
public class ChatServer {

    @Inject
    private UserManager mUserManager;
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Session started");
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session closed");
    }

    @OnError
    public void onError(Throwable error) {
        System.out.println("Error occured: "+error);
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();

            if (Config._JOIN_CHAT.equals(jsonMessage.getString("action"))) {
                joinChat(jsonMessage, session);
            }

            if (Config._LEAVE_CHAT.equals(jsonMessage.getString("action"))) {
                leaveChat(jsonMessage, session);
            }

            if (Config._TOGGLE_STATUS.equals(jsonMessage.getString("action"))) {
                toggleStatus(jsonMessage, session);
            }
            
            if (Config._PUBLIC_IM.equals(jsonMessage.getString("action"))) {
                publicMessage(jsonMessage, session);
            }
            
            if (Config._PRIVATE_IM.equals(jsonMessage.getString("action"))) {
                privateMessage(jsonMessage, session);
            }
        }
    }
    
    private void joinChat(JsonObject jsonMessage , Session session) {
        User mUser = new User();
        mUser.setId(jsonMessage.getInt("id"));
        mUser.setName(jsonMessage.getString("name"));
        mUser.setStatus(Config._STATUS_ONLINE);
        mUserManager.joinChat(mUser,session);
    }
    
    private void leaveChat(JsonObject jsonMessage , Session session) {
        int id = (int) jsonMessage.getInt("id");
        mUserManager.leaveChat(id , session);
        System.out.println(id+" removed");
    }
    
    private void toggleStatus(JsonObject jsonMessage , Session session) {
        int id = (int) jsonMessage.getInt("id");
        mUserManager.toggleStatus(id , session);    
    }
    
    private void publicMessage(JsonObject jsonMessage , Session session) {
        int id = (int) jsonMessage.getInt("id");
        String msg = jsonMessage.getString("message");
        mUserManager.sendMessage(id , msg);
        System.out.println(msg);
    }
    
    private void privateMessage(JsonObject jsonMessage , Session session) {
        int senderId = (int) jsonMessage.getInt("id");
        int receiverId = (int) jsonMessage.getInt("receiverId");
        String msg = jsonMessage.getString("message");
        String date = Long.toString(System.currentTimeMillis());
        mUserManager.sendMessage(senderId, receiverId, msg, date);
        System.out.println(senderId+" to "+receiverId+": "+msg);
    }
}  
