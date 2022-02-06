
package view;

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class ClientView extends JFrame implements Runnable {
    public static final String HOSTNAME = "localhost";
    private static int PORT = 1109;

//    public static final String HOSTNAME = "http://0196-171-224-181-248.ngrok.io";
//    private static int PORT = 3306;
    
    public static final String SIGNIN_SUCCESS = "Login successfully!";
    public static final String SIGNUP_SUCCESS = "Signup successfully!";
    public static final String ACCOUNT_USED = "Account is in use!";
    public static final String ACCOUNT_INVALID = "Incorrect username/password!";
    public static final String ACCOUNT_EXIST = "Account has been registered already!";
    public static final String ACCOUNT_NONEXIST = "Account does not exist!";
    public static final String START_GAME_ERROR = "Can't start game!";
    
    String username;
    String onlineUser;
    Socket socketOfClient;
    BufferedWriter bw;
    BufferedReader br;
    
    JPanel mainPanel;
    LoginPanel loginPanel;
    WelcomePanel welcomePanel;
    SignUpPanel signUpPanel;
    HomePanel homePanel;
    GameLatHinh gameLatHinh;
    
    Thread clientThread;
    boolean isRunning;
        
    StringTokenizer tokenizer, tokenizer2;
    
    Socket socketOfSender, socketOfReceiver;
    
    DefaultListModel<String> listModel;
    DefaultListModel<String> listAvailableModel;
    Hashtable<String, ChatFrame> listReceiver;
        
    boolean isConnectToServer;
    
    int timeClicked = 0;
    int countDown = 20;
    
    String opponentPlayer = "";
    
    int yourTime = 0;
    int opponentTime = 0;
    
    boolean ingame = false;

    public ClientView(String username) {
        this.username = username;
        socketOfClient = null;
        bw = null;
        br = null;
        isRunning = true;
        listModel = new DefaultListModel<>();
        listAvailableModel = new DefaultListModel<>();
        isConnectToServer = false;
        listReceiver = new Hashtable<>();
        
        mainPanel = new JPanel();
        loginPanel = new LoginPanel();
        welcomePanel = new WelcomePanel();
        signUpPanel = new SignUpPanel();
        homePanel = new HomePanel();
        
        welcomePanel.setVisible(true);
        signUpPanel.setVisible(false);
        loginPanel.setVisible(false);
        homePanel.setVisible(false);
        
        mainPanel.add(welcomePanel);
        mainPanel.add(signUpPanel);
        mainPanel.add(loginPanel);
        mainPanel.add(homePanel);
        
        addEventsForWelcomePanel();
        addEventsForSignUpPanel();
        addEventsForLoginPanel();
        addEventsForHomePanel();
        
        pack();
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        setSize(570, 520);
        setLocation(400, 100);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle(username);        
    }
    
    private void addEventsForWelcomePanel() {
        
        welcomePanel.getBtnLogin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(true);
            }
        });
        welcomePanel.getBtnSignUp().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                welcomePanel.setVisible(false);
                signUpPanel.setVisible(true);
                loginPanel.setVisible(false);
            }
        });
        
    }    

    private void addEventsForSignUpPanel() {
        signUpPanel.getBtnBack().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                signUpPanel.clearTf();
                welcomePanel.setVisible(true);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
            }
        });
        signUpPanel.getBtnSignUp().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btnSignUpEvent();
            }
        });
    }

    private void addEventsForLoginPanel() {
        loginPanel.getBtnLogin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btnLoginEvent();
            }
        });
        loginPanel.getBtnBack().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                loginPanel.clearTf();
                welcomePanel.setVisible(true);
                signUpPanel.setVisible(false);
                loginPanel.setVisible(false);
            }
        });
    }
    
    private void addEventsForHomePanel() {
        homePanel.getBtnChat().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                openChat();
            }
        });                    
        
        homePanel.getOnlineList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                inviteUser();
            }
        });
    }   

    private void openChat() {
//        String chatReceiver = homePanel.getOnlineList().getSelectedValue();
        String temp = homePanel.getOnlineList().getSelectedValue();
        tokenizer2 = new StringTokenizer(temp, ":");
        String chatReceiver = tokenizer2.nextToken();

        ChatFrame cf = listReceiver.get(chatReceiver);
        if(cf == null) {
            cf = new ChatFrame(username, chatReceiver, bw, br);

            cf.getLbReceiver().setText("Send to " + cf.receiver);
            cf.setTitle(cf.sender); 
            cf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            cf.setVisible(true);

            listReceiver.put(chatReceiver, cf);
        } else {
            cf.setVisible(true);
        }  
    }
    
    private void inviteUser() {
        timeClicked++;
        if(timeClicked == 1) {
            Thread countingTo500ms = new Thread(counting);
            countingTo500ms.start();
        }

        if(timeClicked == 2) {
            
//            String receiverUser = homePanel.getOnlineList().getSelectedValue();
//            sendToServer("INVITE_USER|" + this.username + "|" + receiverUser);
            
            String temp = homePanel.getOnlineList().getSelectedValue();
            tokenizer2 = new StringTokenizer(temp, ":");
            String receiverUser = tokenizer2.nextToken();
            if (receiverUser.equals(username)) {
                JOptionPane.showMessageDialog(this, "Can't invite yourself!", "WARNING", JOptionPane.WARNING_MESSAGE);
            }
            else {
                sendToServer("INVITE_USER|" + this.username + "|" + receiverUser);
            }

        }
    }    
    
    private void finishGame() {
        Thread counting1000ms = new Thread(countDown20s);
        counting1000ms.start();
    }

    Runnable counting = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
            timeClicked = 0;
        }
    }; 

    Runnable countDown20s = new Runnable() {
        @Override
        public void run() {
            while(countDown > 0) {
                try {
                    Thread.sleep(1000);
                    countDown--;
                    System.out.println(countDown);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
            
            ClientView.this.yourTime = gameLatHinh.getTime();
            sendToServer("FINISH_GAME|" + ClientView.this.opponentPlayer + "|" + ClientView.this.yourTime);

            yourTime = 0;
            opponentPlayer = "";
            
            countDown = 20;
        }
    };    
        

    private void btnSignUpEvent() {
        String password = this.signUpPanel.getTfPassword().getText();
        String confirmPassword = this.signUpPanel.getTfConfirmPassword().getText();
        if(!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password doesn't match!", "ERROR", JOptionPane.ERROR_MESSAGE);
        } else {
            String username = signUpPanel.getTfUsername().getText().trim();
            if(username.equals("") || password.equals("") || confirmPassword.equals("")) {
                JOptionPane.showMessageDialog(this, "Fill up all fields!", "WARNING", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(!isConnectToServer) {
                isConnectToServer = true;
                this.connectToServer(HOSTNAME);
            }    
            this.sendToServer("SIGN_UP|" + username + "|" +password);
        
            String response = this.receiveFromServer();
            if(response != null) {
                if(response.equals(ACCOUNT_EXIST)) {                   
                    JOptionPane.showMessageDialog(this, response, "ERROR", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, response + "\nLogin again!", "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
                    signUpPanel.clearTf();
                    welcomePanel.setVisible(true);
                    signUpPanel.setVisible(false);
                    loginPanel.setVisible(false);
                }
            }
        }
        
    }
    
    private void btnLoginEvent() {
        String username = loginPanel.getTfUsername().getText().trim();
        String password = loginPanel.getTfPassword().getText().trim();
        
        this.username = username;
        
        if(username.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(this, "Fill up all fields!", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if(!isConnectToServer) {
            isConnectToServer = true;
            this.connectToServer(HOSTNAME);
        }    
        this.sendToServer("CHECK_USERNAME|" + this.username + "|" + password);
        
        String response = this.receiveFromServer();
        if(response != null) {
            if (response.equals(ACCOUNT_USED) || response.equals(ACCOUNT_INVALID)) {
                JOptionPane.showMessageDialog(this, response, "ERROR", JOptionPane.ERROR_MESSAGE);
            } 
            else {
                loginPanel.setVisible(false);
                homePanel.setVisible(true);
                this.setTitle("User - " + username);

                clientThread = new Thread(this);
                clientThread.start();
                this.sendToServer("LIST_ONLINE_USERS|" + this.onlineUser);

                System.out.println("User " + username + " logins!");
            }
        } 
        else {
            System.out.println("[btOkEvent()] Server is not ready!");
        }
    }    

    public void connectToServer(String hostAddress) {
        try {
            socketOfClient = new Socket(hostAddress, PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socketOfClient.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socketOfClient.getInputStream()));
            
        } catch (java.net.UnknownHostException e) {
            isConnectToServer = false;
            JOptionPane.showMessageDialog(this, "Host IP is not correct!", "Failed to connect to server!", JOptionPane.ERROR_MESSAGE);
        } catch (java.net.ConnectException e) {
            isConnectToServer = false;
            JOptionPane.showMessageDialog(this, "Server is unreachable!", "Failed to connect to server!", JOptionPane.ERROR_MESSAGE);
        } catch(java.net.NoRouteToHostException e) {
            isConnectToServer = false;
            JOptionPane.showMessageDialog(this, "Can't find this host!", "Failed to connect to server!", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }

    public void sendToServer(String line) {
        try {
            this.bw.write(line);
            this.bw.newLine();
            this.bw.flush();
        } catch (java.net.SocketException e) {
            isConnectToServer = false;
            JOptionPane.showMessageDialog(this, "Server is closed, can't send message!", "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (java.lang.NullPointerException e) {
            isConnectToServer = false;
            System.out.println("[sendToServer()] Server is not ready!");
        } catch (IOException ex) {
            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String receiveFromServer() {
        try {
            return this.br.readLine();
        } catch (java.lang.NullPointerException e) {
            isConnectToServer = false;
            System.out.println("[recieveFromServer()] Server is not ready!");
        } catch (IOException ex) {
            System.out.println("[recieveFromServer()] Socket client is closed!");
//            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void disconnect() {
        try {
            isConnectToServer = false;
            isRunning = false;             
            this.bw.close();   
            this.br.close();
            this.socketOfClient.close();        
            ClientView.this.setVisible(false);
            new ClientView(null).setVisible(true);            
        } catch (IOException ex) {
            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    @Override
    public void run() {
        String response;
        String sender;
        String msg;
        String cmd, cmd2;
        ChatFrame cf;
        while(isRunning) {
            response = this.receiveFromServer();
            cmd = "";
            if(response!=null) {
                tokenizer = new StringTokenizer(response, "|");
                cmd = tokenizer.nextToken();   
                System.out.println(cmd);
            }
            switch (cmd) {
                case "LIST_ONLINE_USERS":
                    listModel.clear();
                   
                    while (tokenizer.hasMoreTokens()) {
                        cmd = tokenizer.nextToken();
                        
                        // status goes here                        
//                        tokenizer2 = new StringTokenizer(cmd, ":");
//                        cmd2 = tokenizer2.nextToken();
//                        listModel.addElement(cmd2 + ": " + tokenizer2.nextToken());
                        listModel.addElement(cmd);
                    }

//                    listModel.removeElement(this.username + ":Online");
//                    listModel.removeElement(this.username + ":Ingame");                    
                    homePanel.getOnlineList().setModel(listModel);
                    break;

                case "PRIVATE_CHAT":
                    sender = tokenizer.nextToken();
                    msg = response.substring(cmd.length() + sender.length() + 2, response.length());
                    
                    cf = listReceiver.get(sender);
                    
                    if(cf == null) {
                        cf = new ChatFrame(username, sender, bw, br);
                        cf.sender = username;
                        cf.receiver = sender;
                        cf.bw = ClientView.this.bw;
                        cf.br = ClientView.this.br;

                        cf.getLbReceiver().setText("Send to " + cf.receiver);
                        cf.setTitle(cf.sender);
                        cf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        cf.setVisible(true);

                        listReceiver.put(sender, cf);
                    } else {
                        cf.setVisible(true);
                    }
                    cf.appendMessage_Left(sender + ": ", msg);
                    break;  
                
                case ACCOUNT_NONEXIST:
                    JOptionPane.showMessageDialog(this, "Unknown Error", "ERROR", JOptionPane.ERROR_MESSAGE);
                    break;
                    
                case "AVAILABLE_USERS":
                    listAvailableModel.clear();
                    while(tokenizer.hasMoreTokens()) {
                        cmd = tokenizer.nextToken();
                        listAvailableModel.addElement(cmd);
                    }                  
                    break; 
                    
                case "RECEIVE_USER":
                    sender = tokenizer.nextToken();
                    int kq = JOptionPane.showConfirmDialog(ClientView.this, sender + " want to play with you?", "CONFIRM", JOptionPane.YES_NO_OPTION);
                    if(kq == JOptionPane.YES_OPTION) {
                        this.sendToServer("ACCEPT_SENDER|" + sender + "|" + this.username);
                    }
                    else {
                        this.sendToServer("REJECT_SENDER|" + sender);
                    }
                    break;
                
                case "REJECT_SENDER":
                    String inviteReceiver = tokenizer.nextToken();
                    JOptionPane.showMessageDialog(this, inviteReceiver + " rejected your invitation!", "INFORMATION", JOptionPane.INFORMATION_MESSAGE);
                    break;
                    
                case "INGAME_USER":
                    String receiverUser = tokenizer.nextToken();
                    JOptionPane.showMessageDialog(this, "User " + receiverUser +  " is ingame, please wait or find someone else!", "WARNING", JOptionPane.ERROR_MESSAGE);
                    break;
                                      
                case "START_GAME":
                    String opponent = tokenizer.nextToken();

                    this.gameLatHinh = new GameLatHinh(countDown);                    
                    this.gameLatHinh.timer2.start();

                    opponentPlayer = opponent;
                    
                    finishGame();
                    break;
                    
                case "START_GAME_ERROR":
                    JOptionPane.showMessageDialog(this, response, "ERROR", JOptionPane.ERROR_MESSAGE);
                    break;    
                                        
                case "RESULT_GAME":                   
                    String resultNotif = tokenizer.nextToken();
                    String message = tokenizer.nextToken();
                    JOptionPane.showMessageDialog(this, message, resultNotif, JOptionPane.INFORMATION_MESSAGE);
                    this.gameLatHinh.setVisible(false);
                    this.homePanel.setVisible(true);
                    break; 
                                       
                case "FINISH_GAME_ERROR":
                    JOptionPane.showMessageDialog(this, "FINISH GAME ERROR!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    this.gameLatHinh.setVisible(false);
                    this.homePanel.setVisible(true);                    
                    break;
                    
                case "SERVER_CLOSE":
                    JOptionPane.showMessageDialog(this, "Server is closed!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    Enumeration<String> e = listReceiver.keys();
                    while (e.hasMoreElements()) {
                        listReceiver.get(e.nextElement()).dispose();
                    }                    
                    this.disconnect();
                    break;                    
                
                default:
                    break;

            }
        }
        System.out.println("Disconnected to server!");            
    }
    
}


