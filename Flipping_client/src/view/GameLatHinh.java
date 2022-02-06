package view;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.event.AncestorListener;

public class GameLatHinh extends JFrame implements ActionListener {

    int count = 0, id, preX, preY, X, Y;
    int level = 0, hit = 0, h;
    int sizeX = 2;
    int sizeY = 2;
    int TIME[] = {100, 20, 30, 50, 65, 80, 100, 120, 140, 150};
    int maxTime = 20;
    int time = 0, secondPassed = 0;
    boolean loss = false;
    int BOM, dem = 0;
    int maxXY = 100;
    final int m = 2, n = 3;
    private JProgressBar progressTime;
    private JButton bt[][] = new JButton[maxXY][maxXY];
    private boolean tick[][] = new boolean[maxXY][maxXY];
    private int a[][] = new int[maxXY][maxXY];
    private int xFood, yFood;
    private JButton score_bt;
    private JPanel pn, pn2;
    Container cn;
    Timer timer, timer2;
    Thread timerThread;

    public GameLatHinh(int max) {
        maxTime = max;
        this.setTitle("Game Lật Hình");

        cn = init();
        timer = new Timer(240, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open();
                timer.stop();
            }
        });

        timer2 = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                time++;
                secondPassed++;
                progressTime.setValue(maxTime - time);
                System.out.println(time + " " + maxTime);
                if (maxTime == time) {
//                    JOptionPane.showMessageDialog(null, "Thời gian:" + time);
                    timer2.stop();


                }
            }
        });
    }

    public void open() {
        //score_bt.setText(String.valueOf(secondPassed));
        if (id == a[X][Y]) {
            bt[preX][preY].setIcon(getIcon(-1));
            a[X][Y] = a[preX][preY] = 0;
            tick[X][Y] = tick[preX][preY] = false;
            bt[X][Y].setBorder(null);
            bt[preX][preY].setBorder(null);
            showMatrix();
            bt[X][Y].setIcon(getIcon(-1));

            hit++;
            if (hit == m * n / 2) {
                timer.stop();
                timer2.stop();
            }
        } else {
            bt[preX][preY].setIcon(getIcon(0));
            bt[X][Y].setIcon(getIcon(0));
            tick[preX][preY] = true;
            tick[X][Y] = true;

        }
    }

    public Container init() {

        time = 0;
        Container cn = this.getContentPane();
        pn = new JPanel();
        pn.setLayout(new GridLayout(m, n));
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                bt[i][j] = new JButton();
                pn.add(bt[i][j]);
                bt[i][j].setActionCommand(i + " " + j);
                bt[i][j].addActionListener(this);
                bt[i][j].setBackground(Color.black);
                a[i][j] = (int) (Math.random() * 2 + 1);
                bt[i][j].setIcon(getIcon(0));
                tick[i][j] = true;
            }
        }
        pn2 = new JPanel();
        pn2.setLayout(new FlowLayout());

        progressTime = new JProgressBar(0, maxTime);
        progressTime.setValue(maxTime);
        progressTime.setForeground(Color.orange);

        createMatrix();
        showMatrix();
        cn.add(pn);
        cn.add(progressTime, "North");
        cn.add(pn2, "South");
        this.setVisible(true);
        this.setSize(n * 120, m * 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        setResizable(false);

        return cn;
    }

    public void createMatrix() {
        int images = 50;
        int N = m * n;
        int b[] = new int[m * n + images];
        int c[] = new int[m * n + images];
        for (int i = 0; i < images; i++) {
            b[i] = i;
            c[i] = (int) (Math.random() * 1000000);
        }
        for (int i = 0; i < images - 1; i++) {
            for (int j = i + 1; j < images; j++) {
                if (c[i] > c[j]) {
                    int tmp = b[i];
                    b[i] = b[j];
                    b[j] = tmp;
                    tmp = c[i];
                    c[i] = c[j];
                    c[j] = tmp;
                }
            }
        }
        for (int i = N / 2; i < N; i++) {
            b[i] = b[i - N / 2];
        }
        for (int i = 0; i < m * n; i++) {
            c[i] = (int) (Math.random() * 1000000);
        }
        for (int i = 0; i < N - 1; i++) {
            for (int j = i + 1; j < N; j++) {
                if (c[i] > c[j]) {
                    int tmp = b[i];
                    b[i] = b[j];
                    b[j] = tmp;
                    tmp = c[i];
                    c[i] = c[j];
                    c[j] = tmp;
                }
            }
        }
        N = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] = b[N++];
            }
        }
    }

    public void showMatrix() {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%3d", a[i][j]);
            }
            System.out.println();
        }
        System.out.println("-----------------");
        System.out.println();
    }

    private Icon getIcon(int index) {
        int width = 120, height = 170;
        Image image = new ImageIcon("src/images/icon" + index + ".jpg").getImage();
        Icon icon = new ImageIcon(image.getScaledInstance(width, height, image.SCALE_SMOOTH));
        return icon;
    }

    public void newGame() {
        this.dispose();
        new GameLatHinh(20);
    }

    public void showDialogNewGame(String message, String title) {
        int select = JOptionPane.showOptionDialog(null, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                null, null);
        if (select == 0) {
            this.dispose();
        } else {
            this.dispose();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i, j;
        String s = e.getActionCommand();
        int k = s.indexOf(32);
        i = Integer.parseInt(s.substring(0, k));
        j = Integer.parseInt(s.substring(k + 1, s.length()));
        if (tick[i][j]) {
            tick[i][j] = false;
            if (count == 0) {
                bt[i][j].setIcon(getIcon(a[i][j]));
                id = a[i][j];
                preX = i;
                preY = j;
            } else {
                bt[i][j].setIcon(getIcon(a[i][j]));
                X = i;
                Y = j;
                timer.start();
            }
            count = 1 - count;
        }
    }

    public int getTime() {
        if(time == 0 || this == null)
            time = maxTime;
        return time;
    }

}
