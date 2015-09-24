/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bilal.chatserver.server;
import com.bilal.chatserver.database.DbAdapter;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import com.bilal.chatserver.model.Message;
import com.bilal.chatserver.model.User;
import java.util.HashMap;
import javax.inject.Inject;
/**
 *
 * @author baloch
 */
@ApplicationScoped
public class UserManager {
   
    @Inject
    private DbAdapter mDbAdapter;
    
    private final Set<User> users = new HashSet<>();
    private final Set<Message> chatLogs = new HashSet<>();
    private final HashMap<Integer, Set<User>> friends = new HashMap<>();
    private final HashMap<Integer, Session> userSession = new HashMap<>();
    
    public void joinChat(User user, Session session) {
        System.out.println("New user added with name: "+user.getName());
        if(!users.contains(user)) {
            users.add(user);
        }
        userSession.put(user.getId(), session);
        prepareAndBroadcastFriendList(user);
    }
    
    public void leaveChat(int id , Session session) {
        User user = getUserById(id);
        if (user != null) {
            
            users.remove(user);
            userSession.remove(user.getId());
            
            JsonProvider provider = JsonProvider.provider();
            JsonObject removeMessage = provider.createObjectBuilder()
                    .add("action", Config._LEAVE_CHAT)
                    .add("id", id)
                    .add("status",Config._STATUS_OFFLINE)
                    .add("name",user.getName())
                    .add("message","is offline")
                    .build();
            
            //todo-task
            //notify friends if someone offline
            
            for(User friend : friends.get(id)) {
                if(userSession.containsKey(friend.getId())) {
                    sendToSession(userSession.get(friend.getId()), removeMessage);
                }
            }
        }
    }

    public void toggleStatus(int id, Session session) {
        JsonProvider provider = JsonProvider.provider();
        User user = getUserById(id);
        if (user != null) {
            if (Config._STATUS_ONLINE.equals(user.getStatus())) {
                user.setStatus(Config._STATUS_OFFLINE);
            } else {
                user.setStatus(Config._STATUS_ONLINE);
            }
            JsonObject updateStatusMessage = provider.createObjectBuilder()
                    .add("action", Config._TOGGLE_STATUS)
                    .add("id", user.getId())
                    .add("name",user.getName())
                    .add("status", user.getStatus())
                    .build();
            
            for(User friend : friends.get(id)) {
                if(userSession.containsKey(friend.getId())) {
                    sendToSession(userSession.get(friend.getId()), updateStatusMessage);
                }
            }
            
            System.out.println(user.getName()+"'s status set to: "+user.getStatus());
        }
    }
    
    private User getUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
    /*
    *Async method...
    */
    private void prepareAndBroadcastFriendList(User user) {
        Set<User> _friends = mDbAdapter.getFriends(user.getId());
        friends.put(user.getId(), _friends);
        JsonObject addMessage;
        for (User friend : _friends) {
            if(userSession.containsKey(friend.getId())) {
                addMessage = createToggleMessage(user);
                sendToSession(userSession.get(friend.getId()), addMessage);
                friend.setStatus(Config._STATUS_ONLINE);
            }else {
                friend.setStatus(Config._STATUS_OFFLINE);
            }
            addMessage = createAddMessage(friend);
            sendToSession(userSession.get(user.getId()), addMessage);
        }
    }
    
    private JsonObject createToggleMessage(User user) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject updateStatusMessage = provider.createObjectBuilder()
                    .add("action", Config._TOGGLE_STATUS)
                    .add("id", user.getId())
                    .add("name",user.getName())
                    .add("message","is online")
                    .add("status", user.getStatus())
                    .build();
        return updateStatusMessage;
    }

    private JsonObject createPublicMessage(int sender , String message) {
        User user = getUserById(sender);
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("action", Config._PUBLIC_IM)
                .add("id" , sender)
                .add("name" , (user != null) ? user.getName() : "")
                .add("message" , message)
                .build();
        return addMessage;
    }
    
    private JsonObject createPrivateMessage(int senderId, int receiverId, String message, String date) {
        //User user = getUserById(senderId);
        JsonProvider provider = JsonProvider.provider();
        JsonObject response = provider.createObjectBuilder()
                .add("action", Config._PRIVATE_IM)
                .add("id" , senderId)
                .add("receiverId" , receiverId)
                .add("message" , message)
                .add("date" , date)
                .build();
        return response;
    }
    /*
    * send public message
    */
    public void sendMessage(int sender , String message) {
        JsonObject chatMessage = createPublicMessage(sender,message);
        
        sendToSession(userSession.get(sender), chatMessage);
        
        //broadcast public message to all friends...
        for(User friend : friends.get(sender)) {
            if(userSession.containsKey(friend.getId())) {
                sendToSession(userSession.get(friend.getId()), chatMessage);
            }
        }
    }
    /*
    * send private message
    */
    public void sendMessage(int senderId, int receiverId, String message, String date) {
        Message mMessage = new Message();
        mMessage.setSender(getUserById(senderId));
        mMessage.setReceiver(getUserById(receiverId));
        mMessage.setMessage(message);
        mMessage.setDate(date);
        chatLogs.add(mMessage);//save private messages...
        
        JsonObject response = createPrivateMessage(senderId, receiverId, message, date);
        
        Session receiverSession = (userSession.containsKey(receiverId)) ? userSession.get(receiverId) : null;
        Session senderSession = (userSession.containsKey(senderId)) ? userSession.get(senderId) : null;
        
        if(receiverSession!= null && receiverSession.isOpen()) {
            if(senderSession!= null && senderSession.isOpen()) {
                try {
                    sendToSession(receiverSession, response);
                    sendToSession(senderSession, response);
                }catch(Exception e) {
                    System.out.println("Error: "+e);
                }
            }else {
                System.out.println(getUserById(senderId)+"'s session already closed");
            }
        }else {
            System.out.println(getUserById(receiverId)+"'s session already closed");
        }
    }
    
    private JsonObject createAddMessage(User user) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject addMessage = provider.createObjectBuilder()
                .add("action", Config._JOIN_CHAT)
                .add("id", user.getId())
                .add("name", user.getName())
                .add("message","has joined")
                .add("status", user.getStatus())
                .build();
        return addMessage;
    }

    private void sendToSession(Session session, JsonObject message) {
        try {
            if(session.isOpen()) {
                session.getBasicRemote().sendText(message.toString());
            }
        } catch (IOException ex) {
            //Logger.getLogger(UserSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Session already destroyed..");
        }
    }
}
