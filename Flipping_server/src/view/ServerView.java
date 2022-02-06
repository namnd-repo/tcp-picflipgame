
package view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.logging.*;
import javax.swing.*;
import controller.*;

public class ServerView extends JFrame implements Runnable {
    
    private static int PORT = 1109;
    JButton btnStart, btnStop;
    JTextArea tabInfo;
    ServerSocket serverSocket;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    ServerThread serverThread;
    
    public ServerView() {
        JPanel panel = new JPanel(new BorderLayout());
     
        tabInfo = new JTextArea();
        tabInfo.setEditable(false);
        tabInfo.setFont(new java.awt.Font("Tahoma", 0, 16));
        tabInfo.setBackground(new java.awt.Color(255, 255, 255));
        tabInfo.setForeground(new java.awt.Color(0, 0, 0));
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(tabInfo);
        scroll.setPreferredSize(new Dimension(400, 400));
        
        btnStart = new JButton("START");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btnStartEvent(ae);
            }
        });
        
        btnStop = new JButton("STOP");
        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                btnStopEvent(ae);
            }
        });
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        
        JPanel panelBtn = new JPanel();
        panelBtn.setBackground(new java.awt.Color(255, 255, 255));
        panelBtn.add(btnStart);
        panelBtn.add(btnStop);
        
        JPanel p1 = new JPanel();
        p1.setPreferredSize(new Dimension(30, 30));
        JPanel p2 = new JPanel();
        p2.setPreferredSize(new Dimension(30, 30));

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelBtn, BorderLayout.SOUTH);
        
        this.add(panel);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void appendMessage(String message) {
        tabInfo.append(message);
        tabInfo.setCaretPosition(tabInfo.getText().length() - 1);
    }
    
    private void btnStartEvent(ActionEvent ae) {
        Connection conn = new UserDatabase().connect();
        if(conn == null) {
            JOptionPane.showMessageDialog(this, "Not connect to DB!", "ERROR!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new Thread(this).start();
        this.btnStart.setEnabled(false);
        this.btnStop.setEnabled(true);
    }
    
    private void btnStopEvent(ActionEvent ae) {
        int kq = JOptionPane.showConfirmDialog(this, "You're about to close server?", "WARNING", JOptionPane.YES_NO_OPTION);
        if(kq == JOptionPane.YES_OPTION) {
            try {
                if(serverThread != null) {
                    serverThread.notifyToAllUsers("SERVER_CLOSE|");
                }
                if(serverSocket != null) {
                    serverSocket.close();
                }
                this.btnStart.setEnabled(true);
                this.btnStop.setEnabled(false);                
            } catch (IOException ex) {
                Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);

            }
        }
    }
    
    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            appendMessage("[" + sdf.format(new Date()) + "] Server is running!");
            appendMessage("\n[" + sdf.format(new Date()) + "] Server is empty!\n");
            
            while(true) {
                Socket socketOfServer = serverSocket.accept();
                serverThread = new ServerThread(socketOfServer);
                serverThread.tabServer = this.tabInfo;
                serverThread.start();
            }
            
        } catch (java.net.SocketException e) {
            System.out.println("Server is closed!");
        } catch (IOException ex) {
            Logger.getLogger(ServerView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Port in use / Server closed!");
            JOptionPane.showMessageDialog(this, "Port in use!", "ERROR", JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
            System.exit(0);
        }
    }    

    @Override
    public void run() {
        this.startServer();
    }
    
}
