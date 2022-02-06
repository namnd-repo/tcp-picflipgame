
package model;

public class User {
    private String username;
    private String password;
    private float score;
    private int win;
    private int draw;
    private int lose;
    private float avgoppo;
    private float avgtime;
      
    private String roomId;

    public User(String username, String password, float score, int win, int draw, int lose, float avgoppo, float avgtime) {
        this.username = username;
        this.password = password;
        this.score = score;
        this.win = win;
        this.draw = draw;
        this.lose = lose;
        this.avgoppo = avgoppo;
        this.avgtime = avgtime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getWin() {
        return win;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getLose() {
        return lose;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }

    public float getAvgoppo() {
        return avgoppo;
    }

    public void setAvgoppo(float avgoppo) {
        this.avgoppo = avgoppo;
    }

    public float getAvgtime() {
        return avgtime;
    }

    public void setAvgtime(float avgtime) {
        this.avgtime = avgtime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }


}
