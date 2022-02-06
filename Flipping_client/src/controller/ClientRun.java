
package controller;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import view.ClientView;

public class ClientRun {
    public static void main(String[] args) {
        ClientView client = new ClientView(null);
        client.setVisible(true);        
    }
}
