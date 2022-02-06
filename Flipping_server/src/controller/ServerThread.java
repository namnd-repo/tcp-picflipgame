
package controller;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import model.User;
import view.ServerView;

public class ServerThread extends Thread {
    Socket socketOfServer;
    BufferedWriter bw;
    BufferedReader br;
    
    public JTextArea tabServer;
    
    String clientName, clientPass, clientMatchId, clientTimeStr;
    
    Boolean checkInGame;
    public static Hashtable<String, ServerThread> listUser = new Hashtable<>();
    
    public static final String SIGNIN_SUCCESS = "Login successfully!";
    public static final String SIGNUP_SUCCESS = "Signup successfully!";
    public static final String ACCOUNT_USED = "Account is in use!";
    public static final String ACCOUNT_INVALID = "Incorrect username/password!";
    public static final String ACCOUNT_EXIST = "Account has been registered already!";
    public static final String ACCOUNT_NONEXIST = "Account does not exist!";
    public static final String START_GAME_ERROR = "Can't start game!";
    
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    
    boolean isRunning;
    
    UserDatabase userDB;
    
    public ServerThread(Socket socketOfServer) {
        this.socketOfServer = socketOfServer;
        this.bw = null;
        this.br = null;
        
        isRunning = true;
        
        clientName = "";
        clientPass = "";
        clientMatchId = "";
        clientTimeStr = "0";

        checkInGame = false;
        
        userDB = new UserDatabase();
        userDB.connect();
    }

    public void appendMessage(String message) {
        tabServer.append(message);
        tabServer.setCaretPosition(tabServer.getText().length() - 1);
    }
    
    public String receiveFromClient() {
        try {
            return br.readLine();
        } catch (IOException ex) {
            System.out.println(clientName+" is disconnected!");
        }
        return null;
    }

    public void sendToClient(String response) {
        try {
            bw.write(response);
            bw.newLine();
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToSpecificClient(ServerThread socketOfClient, String response) {
        try {
            BufferedWriter writer = socketOfClient.bw;
            writer.write(response);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public void closeServerThread() {
        try {
            isRunning = false;
            if(bw != null) {
                bw.close();
            }
            if(br != null) {
                br.close();
            }            
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public String getAllUsers() {
        StringBuffer kq = new StringBuffer();
        String temp = null;
        
        Enumeration<String> keys = listUser.keys();
//        if(keys.hasMoreElements()) {
//            String str = keys.nextElement();
//            kq.append(str);
//        }
        
        while (keys.hasMoreElements()) {
            temp = keys.nextElement();
            
            ServerThread st_user = listUser.get(temp);
            String status = (st_user.checkInGame == true) ? "Ingame" : "Online";
            
            // kq will looks like this "|user1:online|user2:ingame|user3:ingame"
            kq.append("|").append(temp).append(":").append(status);           
        }
        
        return kq.toString();
    }    

    public void clientQuit() {
        if(clientName != null) {
            this.appendMessage("[" + sdf.format(new Date()) + "] Client \"" + clientName+ "\" is disconnected!\n");
            if(checkInGame) {
                userDB.updatePoint(clientName, 0, 20);
            }

            checkInGame = false;
            listUser.remove(clientName);
            if(listUser.isEmpty()) this.appendMessage("[" + sdf.format(new Date()) + "] Server is empty!\n");
            notifyToAllUsers("LIST_ONLINE_USERS" + getAllUsers());
            closeServerThread();
        }
    }

    public void notifyToAllUsers(String message) {
        Enumeration<ServerThread> clients = listUser.elements();        
        ServerThread st;
        BufferedWriter writer;
        while (clients.hasMoreElements()) {
            st = clients.nextElement();
            writer = st.bw;
            try {  
                writer.write(message);                               
                writer.newLine();
                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public String getAvailableUsers() {
        StringBuffer kq = new StringBuffer();
        Enumeration<ServerThread> clients = listUser.elements();
        ServerThread st;

        while(clients.hasMoreElements()) {
            st = clients.nextElement();
            if(st.clientMatchId.equals("")) {
                kq.append("|").append(st.clientName);
            }
        }
        
        return kq.toString();
    }           
    
    @Override
    public void run() {
        try {
            bw = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            
            boolean isUserExist = true;
            String message, sender, receiver, fileName;
            StringBuffer str;
            String cmd, icon;
            StringTokenizer tokenizer;
            while(isRunning) {
                try {
                    message = receiveFromClient();
                    tokenizer = new StringTokenizer(message, "|");
                    cmd = tokenizer.nextToken();  

                    switch (cmd) {

                        case "LIST_ONLINE_USERS":
                            notifyToAllUsers("LIST_ONLINE_USERS" + getAllUsers());
                            break;
                            
                        case "CHECK_USERNAME":
                            clientName = tokenizer.nextToken();
                            clientPass = tokenizer.nextToken();
                            isUserExist = listUser.containsKey(clientName);
                            
                            if(isUserExist) {
                                sendToClient(ACCOUNT_USED);
                            }
                            else {
                                boolean kq = userDB.checkUsername(clientName, clientPass);
                                if(kq) {
                                    sendToClient(SIGNIN_SUCCESS);
                                    this.appendMessage("["+sdf.format(new Date())+"] Client \"" + clientName + "\" connects to server!\n");
                                    listUser.put(clientName, this);
                                } 
                                else {
                                    sendToClient(ACCOUNT_INVALID);
                                }
                            }
                            break;
                            
                        case "SIGN_UP":
                            String username = tokenizer.nextToken();
                            String password = tokenizer.nextToken();
//                            System.out.println("name: " + username + " password: "+ password);

                            int kq = userDB.insertUser(new User(username, password, 0, 0, 0, 0, 0, 0));
                            if(kq > 0) {
                                sendToClient(SIGNUP_SUCCESS);
                            }
                            else {
                                sendToClient(ACCOUNT_EXIST);
                            }
                            break;
                            
                        case "PRIVATE_CHAT":
                            String privateSender = tokenizer.nextToken();
                            String privateReceiver = tokenizer.nextToken();
                            String messageContent = message.substring(cmd.length() + privateSender.length() + privateReceiver.length() + 3, message.length());
                            
                            ServerThread st_receiver = listUser.get(privateReceiver);
                            if(st_receiver != null && st_receiver.checkInGame == false) {
                                sendToSpecificClient(st_receiver, "PRIVATE_CHAT|" + privateSender + "|" + messageContent);
//                                System.out.println("[ServerThread] message = "+ messageContent);                                
                            }
                            else {
                                sendToClient("INGAME_USER|" + privateReceiver);
                            }
                            break;                                                 
                            
                        case "AVAILABLE_USERS":
                            sendToClient("AVAILABLE_USERS" + "|" + getAvailableUsers());
                            break;                         
                            
                        case "INVITE_USER":
                            String senderUser = tokenizer.nextToken();
                            String receiverUser = tokenizer.nextToken();
                            
                            clientMatchId = senderUser;
                            
                            ServerThread st_receiverUser = listUser.get(receiverUser);
                            if(st_receiverUser != null && st_receiverUser.checkInGame == false) {
                                sendToSpecificClient(st_receiverUser, "RECEIVE_USER|" + senderUser);                               
                            }
                            else {
                                sendToClient("INGAME_USER|" + receiverUser);
                            }
                            break; 
                            

                        case "ACCEPT_SENDER":
                            String host = tokenizer.nextToken();
                            String you = tokenizer.nextToken();
                            ServerThread st_host = listUser.get(host);

                            clientMatchId = host;
                            
                            User hostUser = userDB.getInfoUser(host);
                            User youUser = userDB.getInfoUser(you);
                            if (hostUser == null || youUser == null) {
                                sendToClient(ACCOUNT_NONEXIST);
                            }
                            else {

                                Enumeration<ServerThread> clients = listUser.elements();
                                ServerThread st;
                                int count = 0;
                                String player2 = "";
                                while (clients.hasMoreElements() && count < 2) {
                                    st = clients.nextElement();
                                    if(st.clientMatchId.equals(clientMatchId)) {
                                        count++;
                                        if(!st.clientName.equals(clientMatchId)) {
                                            player2 = st.clientName; // this is you, the one accept invitation
                                        }
                                    }
                                }
                                if (count == 2) {
                                    checkInGame = true;
                                    listUser.get(host).checkInGame = true;
                                    sendToClient("START_GAME|" + host);
                                    sendToSpecificClient(st_host, "START_GAME|" + clientName);
                                    notifyToAllUsers("LIST_ONLINE_USERS" + getAllUsers());
                                }
                                else {
                                    sendToClient(START_GAME_ERROR);
                                }
                            }                                                          
                            break;                            
                        
                        case "REJECT_SENDER":
                            String inviteSender = tokenizer.nextToken();
                            ServerThread st_inviteSender = listUser.get(inviteSender);
                            sendToSpecificClient(st_inviteSender, "REJECT_SENDER|" + clientName);                            
                            break;                            
                            
                        case "FINISH_GAME":
                            String opponentPlayer = tokenizer.nextToken();
//                            System.out.println(opponentPlayer);
                            
                            clientTimeStr = tokenizer.nextToken();
//                            System.out.println(opponentPlayer + "oppo player");
                            
                            int clientTime = Integer.parseInt(clientTimeStr);
//                            System.out.println(clientTime+ "xyz\n");

                            
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            clientMatchId = "";
                            
                            int opponentTime = 0;                            
                            if(listUser.containsKey(opponentPlayer)) {
                                opponentTime = Integer.parseInt(listUser.get(opponentPlayer).clientTimeStr);
//                                System.out.println(opponentTime + "abc");
                            }
                            System.out.println(opponentTime);
                            float point = -100;
                            String result = "";
                            String msg = "";
                            if(clientTime == opponentTime) {
                                point = (float)0.5;
                                result = "DRAW";
                                msg = "+ " + point + " point!";
                            }
                            else if(clientTime < opponentTime) {
                                point = 1;
                                result = "WIN";
                                msg = "+ " + point + " point!";                                
                            }
                            else if(clientTime > opponentTime) {
                                point = 0;
                                result = "LOSE";
                                msg = "+ " + point + " point!";  
                            }
                            
                            checkInGame = false;

                            if(point != -100 && userDB.updatePoint(clientName, point, clientTime)) {
                                sendToClient("RESULT_GAME|" + result + "|" + msg);                                
                            }
                            else {
                                sendToClient("FINISH_GAME_ERROR|");
                            }
                            notifyToAllUsers("LIST_ONLINE_USERS" + getAllUsers());
                            break;                          
                            
                        default:
                            notifyToAllUsers(message);
                            break;
                    }
                    
                } catch (Exception e) {
                    clientQuit();
                    break;
                }
            }
        } catch (IOException ex) {
            clientQuit();
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
}
