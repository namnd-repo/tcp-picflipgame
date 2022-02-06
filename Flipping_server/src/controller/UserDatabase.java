
package controller;

import java.sql.*;
import java.util.logging.*;
import model.User;

public class UserDatabase {
    private Connection conn;
    public final String DATABASE_NAME = "ltm_final";
    public final String USERNAME = "root";
    public final String PASSWORD = "12345678";
    public final String URL_MYSQL = "jdbc:mysql://localhost:3306/" + DATABASE_NAME;
    
    public final String USER_TABLE = "user";

    private PreparedStatement pst, ps;
    private ResultSet rs;
    
    public Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");    
            conn = DriverManager.getConnection(URL_MYSQL, USERNAME, PASSWORD);
            System.out.println("Connect DB successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("CONNECTION ERROR!");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    public int insertUser(User u) {
        try {
            pst = conn.prepareStatement("INSERT INTO " + USER_TABLE + " (username, password, score, win, draw, lose, avgoppo, avgtime) VALUES ('" + u.getUsername() + "', '" + u.getPassword() + "', '" + u.getScore() + "', '" + u.getWin() + "', '" + u.getDraw() + "', '" + u.getLose()+ "', '" + u.getAvgoppo()+ "', '" + u.getAvgtime() + "')");
            int result = pst.executeUpdate();
            if(result > 0) {
                System.out.println("Insert successfully!");
            }
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    public boolean checkUsername(String username, String password) {
        try {
            pst = conn.prepareStatement("SELECT * FROM " + USER_TABLE + " WHERE username = '" + username + "' AND password = '" + password + "'");
            rs = pst.executeQuery();
            
            if(rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return false;
    }
    
    public User getInfoUser(String username) {
        try {
            pst = conn.prepareStatement("SELECT * FROM " + USER_TABLE + " WHERE username = '" + username + "'");
            rs = pst.executeQuery();
            if(rs.next()) {
                return new User(rs.getString(1), rs.getString(2), rs.getFloat(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getFloat(7), rs.getFloat(8));
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    public boolean updatePoint(String username, float point, int time) {
        try {
            int totalgame;
            float avgoppo, avgtime;
            int win = 0, draw = 0, lose = 0;
            if (point == (float)1) {
                win = 1;
            }
            else if (point == (float)0.5) {
                draw = 1;
            }
            else if (point == (float)0) {
                lose = 1;
            }
            
            pst = conn.prepareStatement("SELECT avgtime, win, draw, lose, score FROM " + USER_TABLE + " WHERE username = '" + username + "'");
            rs = pst.executeQuery();
//            if (rs.next()) {
//                avgtime = rs.getInt(1);
//                totalgame = rs.getInt(2) + rs.getInt(3) + rs.getInt(4);
//                
//                
//                avgoppo = (rs.getInt(5) + point)/(totalgame + 1);  
//                avgtime = (avgtime*totalgame + time)/(totalgame + 1);
//
//                pst = conn.prepareStatement("UPDATE " + USER_TABLE + " SET score = score + " + point + ", win = win + " + win + ", draw = draw + " + draw + ", lose = lose + " + lose + ", avgtime = " + avgtime + ", avgoppo = " + avgoppo + " WHERE username = '" + username + "'");
//            }             
//

            if (rs.next()) {
                avgtime = rs.getInt(1);
                totalgame = rs.getInt(2) + rs.getInt(3) + rs.getInt(4); // win + draw + lose from database
                
                
                avgoppo = (rs.getInt(5) + point)/(totalgame + 1);
                
                if (win == 1) {
                    avgtime = (avgtime*rs.getInt(2) + time)/(rs.getInt(2) + 1);
                    pst = conn.prepareStatement("UPDATE " + USER_TABLE + " SET score = score + " + point + ", win = win + " + win + ", draw = draw + " + draw + ", lose = lose + " + lose + ", avgtime = " + avgtime + ", avgoppo = " + avgoppo + " WHERE username = '" + username + "'");
                }
                else {
                    pst = conn.prepareStatement("UPDATE " + USER_TABLE + " SET score = score + " + point + ", win = win + " + win + ", draw = draw + " + draw + ", lose = lose + " + lose + ", avgoppo = " + avgoppo + " WHERE username = '" + username + "'");
                }                
            }             
            

            int result = pst.executeUpdate();
            if (result > 0) {
                return true;
            }
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        return false;        
    }
}
