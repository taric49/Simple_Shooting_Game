
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;

public class Game extends JFrame implements MouseListener, KeyListener {

    boolean ripPlayer = false;

    ArrayList<Thread> cubes = new ArrayList<>();

    AirCraft player = null;

    int side = 10;

    ReentrantLock lock1 = new ReentrantLock();
    ReentrantLock lock2 = new ReentrantLock();
    ReentrantLock lock3 = new ReentrantLock();

    JPanel panel = new JPanel() {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            for (int i = 0; i < cubes.size(); i++) {
                g.setColor(Color.RED);
                g.fillRect(player.x, player.y, side, side);

                if (cubes.get(i) instanceof Enemy) {
                    g.setColor(Color.BLACK);
                    Enemy e = (Enemy) cubes.get(i);
                    g.fillRect(e.x, e.y, side, side);
                } else if (cubes.get(i) instanceof Friend) {
                    g.setColor(Color.GREEN);
                    Friend f = (Friend) cubes.get(i);
                    g.fillRect(f.x, f.y, side, side);
                } else if (cubes.get(i) instanceof Lazer) {
                    Lazer l = (Lazer) cubes.get(i);
                    g.setColor(l.color);
                    g.fillRect(l.x, l.y, 5, 5);
                }
            }

        }
    };

    public Game() {
        setSize(500, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        panel.setSize(500, 500);
        add(panel, BorderLayout.CENTER);
        addKeyListener(this);
        addMouseListener(this);
        setVisible(true);
    }

    public void paint(Graphics g) {
        panel.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (!ripPlayer) {
            char key = e.getKeyChar();

            if (key == 'w' && player.y - 10 >= panel.getY() && !oppsFriendInTheWay(player.x, player.y - 10)) {
                player.y -= 10;
            }

            else if (key == 's' && player.y + 10 <= 490 && !oppsFriendInTheWay(player.x, player.y + 10)) {
                player.y += 10;
            }

            else if (key == 'd' && player.x + 10 < 500 && !oppsFriendInTheWay(player.x + 10, player.y)) {
                player.x += 10;
            }

            else if (key == 'a' && player.x - 10 >= 0 && !oppsFriendInTheWay(player.x - 10, player.y)) {
                player.x -= 10;
            }
            repaint();
        }
    }

    class Enemy extends Thread {
        public int x;
        public int y;
        Random random;
        public boolean dead = false;
        public int dir;

        public Enemy() {
            random = new Random();
            boolean retry = true;
            while (retry) {
                retry = false;
                x = random.nextInt(50) * 10;
                y = random.nextInt(49) * 10;
                for (Thread t : cubes) {
                    if (t instanceof Enemy) {
                        Enemy e = (Enemy) t;
                        if (e.x == x && e.y == y) {
                            retry = true;
                        }
                    } else if (t instanceof Friend) {
                        Friend f = (Friend) t;
                        if (f.x == x && f.y == y) {
                            retry = true;
                        }
                    } else if (t instanceof AirCraft) {
                        if (250 == x && 250 == y) {
                            retry = true;
                        }
                    }
                }
            }

            cubes.add(this);
        }

        public void run() {
            while (!ripPlayer && !iDestroyThemAll()) {

                dir = random.nextInt(4) + 1;
                if (dir == 1 && y - 10 >= panel.getY() && !oppsEnemyInTheWay(x, y - 10)) {
                    y -= 10;
                } else if (dir == 3 && y + 10 < 490 && !oppsEnemyInTheWay(x, y + 10)) {
                    y += 10;
                } else if (dir == 2 && x + 10 < 500 && !oppsEnemyInTheWay(x + 10, y)) {
                    x += 10;
                } else if (dir == 4 && x - 10 >= 0 && !oppsEnemyInTheWay(x - 10, y)) {
                    x -= 10;
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("FRIEND SLEEP ERROR");
                }
                // checkCollapse(this);
                repaint();

                if (!dead) {
                    Lazer r = new Lazer(x + 5, y + 2, Color.BLUE, 1);
                    Lazer l = new Lazer(x + 5, y + 2, Color.BLUE, -1);
                    lock1.lock();
                    cubes.add(r);
                    cubes.add(l);
                    lock1.unlock();
                    r.start();
                    l.start();

                }

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("FRIEND SLEEP ERROR");
                }

                repaint();
            }
        }

    }

    class Friend extends Thread {
        public int x;
        public int y;
        Random random;
        public boolean dead = false;
        public int dir; // this is class variable because when squares collapse we want to know witch
                        // direction that our square comes

        public Friend() {
            random = new Random();
            boolean retry = true;
            while (retry) {
                retry = false;
                x = random.nextInt(50) * 10;
                y = random.nextInt(49) * 10;
                for (Thread t : cubes) {
                    if (t instanceof Enemy) {
                        Enemy e = (Enemy) t;
                        if (e.x == x && e.y == y) {
                            retry = true;
                        }
                    } else if (t instanceof Friend) {
                        Friend f = (Friend) t;
                        if (f.x == x && f.y == y) {
                            retry = true;
                        }
                    } else if (t instanceof AirCraft) {
                        if (250 == x && 250 == y) {
                            retry = true;
                        }
                    }
                }
            }
            cubes.add(this);
        }

        public void run() {
            while (!ripPlayer && !iDestroyThemAll()) {
                dir = random.nextInt(4) + 1;
                if (dir == 1 && y - 10 >= panel.getY() && !oppsFriendInTheWay(x, y - 10) && player.x != x
                        && player.y != y - 10) {
                    y -= 10;
                } else if (dir == 3 && y + 10 < 490 && !oppsFriendInTheWay(x, y + 10) && player.x != x
                        && player.y != y + 10) {
                    y += 10;
                } else if (dir == 2 && x + 10 < 500 && !oppsFriendInTheWay(x + 10, y) && player.x != x + 10
                        && player.y != y) {
                    x += 10;
                } else if (dir == 4 && x - 10 >= 0 && !oppsFriendInTheWay(x - 10, y) && player.x != x - 10
                        && player.y != y) {
                    x -= 10;
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("FRIEND SLEEP ERROR");
                }
                // checkCollapse(this);
                repaint();

                if (!dead) {
                    Lazer r = new Lazer(x + 5, y + 2, new Color(128, 0, 128), 1);
                    Lazer l = new Lazer(x + 5, y + 2, new Color(128, 0, 128), -1);
                    lock2.lock();
                    cubes.add(r);
                    cubes.add(l);
                    lock2.unlock();
                    r.start();
                    l.start();

                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("FRIEND SLEEP ERROR");
                }

                repaint();
            }

        }

    }

    class AirCraft extends Thread {
        int x = 250;
        int y = 250;

        public AirCraft() {
            player = this;
            cubes.add(this);
        }

        public void run() {
            while (!ripPlayer && !iDestroyThemAll()) {
                for (int i = 0; i < cubes.size(); i++) {
                    if (i < cubes.size() && cubes.get(i) instanceof Enemy) {
                        Enemy e = (Enemy) cubes.get(i);
                        if (player.x == e.x && player.y == e.y) {
                            ripPlayer = true;
                        }
                    }
                }
            }

            JFrame endFrame = new JFrame();
            endFrame.setSize(500, 500);
            endFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            endFrame.setLayout(new BorderLayout());
            Game.this.setVisible(false);

            if (ripPlayer) {
                JButton loose = new JButton("OYUNU KAYBETTINIZ");
                loose.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }

                });

                endFrame.add(loose, BorderLayout.CENTER);
                loose.setBackground(Color.RED);

            } else {
                JButton win = new JButton("OYUNU KAZANDINIZ");
                win.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }

                });
                endFrame.add(win, BorderLayout.CENTER);
                win.setBackground(Color.GREEN);
            }
            endFrame.setVisible(true);

        }
    }

    class Lazer extends Thread {
        int x;
        int y;
        int direction;
        Color color;

        public Lazer(int x, int y, Color c, int dir) {
            this.x = x;
            this.y = y;
            this.direction = dir;
            this.color = c;
            cubes.add(this);
        }

        @Override
        public void run() {
            while (x < 500 && x > 0) {
                x += 10 * direction;
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("LAZER SLEEP ERROR");
                }
                lock3.lock();
                if (x + 15 >= 500 || x - 15 <= 0) {
                    cubes.remove(this);

                }
                lock3.unlock();

                lock3.lock();
                for (int i = 0; i < cubes.size(); i++) {

                    if (i < cubes.size() && cubes.get(i) != null && !(cubes.get(i) instanceof Lazer)) {
                        if (cubes.get(i) instanceof Enemy) {
                            Enemy e = (Enemy) cubes.get(i);
                            if (((x >= e.x && x <= e.x + 10) || (x + 5 >= e.x && x + 5 <= e.x + 10))
                                    && ((y >= e.y && y <= e.y + 10) || (y + 5 >= e.y && y + 5 <= e.y + 10))) {
                                if (color.equals(Color.ORANGE) || color.equals(new Color(128, 0, 128))) {
                                    cubes.remove(e);
                                    e.dead = true;

                                }
                                cubes.remove(this);
                            }
                        } else if (cubes.get(i) instanceof Friend) {
                            Friend f = (Friend) cubes.get(i);

                            if (((x >= f.x && x <= f.x + 10) || (x + 5 >= f.x && x + 5 <= f.x + 10))
                                    && ((y >= f.y && y <= f.y + 10) || (y + 5 >= f.y && y + 5 <= f.y + 10))) {
                                if (color.equals(Color.BLUE)) {
                                    cubes.remove(f);
                                    f.dead = true;
                                }
                                cubes.remove(this);
                            }
                        } else if (cubes.get(i) instanceof AirCraft) {
                            AirCraft p = (AirCraft) cubes.get(i);
                            if (((x >= p.x && x <= p.x + 10) || (x + 5 >= p.x && x + 5 <= p.x + 10))
                                    && ((y >= p.y && y <= p.y + 10) || (y + 5 >= p.y && y + 5 <= p.y + 10))) {
                                if (color.equals(Color.BLUE)) {
                                    ripPlayer = true;
                                }
                                cubes.remove(this);
                            }
                        }
                    }

                }
                lock3.unlock();
                repaint();
            }
        }
    }

    /*
     * public synchronized void checkCollapse(Thread t) {
     * Random ranDircet = new Random();
     * if (t instanceof Friend) {
     * 
     * Friend currBlock = (Friend) t;
     * 
     * for (int i = 0; i < cubes.size(); i++) {
     * 
     * if (currBlock != cubes.get(i)) {
     * 
     * if (cubes.get(i) instanceof Enemy) {
     * Enemy e = (Enemy) cubes.get(i);
     * if (e.x == currBlock.x && e.y == currBlock.y) {
     * cubes.remove(currBlock);
     * currBlock.dead = true;
     * cubes.remove(e);
     * e.dead = true;
     * }
     * }
     * 
     * else if (cubes.get(i) instanceof Friend) {
     * Friend f = (Friend) cubes.get(i);
     * if (f.x == currBlock.x && f.y == currBlock.y) {
     * int tempDir = 1;
     * if (currBlock.dir == 1) {
     * 
     * tempDir = ranDircet.nextInt(3) + 2;
     * } else if (currBlock.dir == 3) {
     * 
     * tempDir = ranDircet.nextInt(2) + 1;
     * } else if (currBlock.dir == 2) {
     * 
     * tempDir = ranDircet.nextInt(2) + 3;
     * } else if (currBlock.dir == 4) {
     * 
     * tempDir = ranDircet.nextInt(3) + 1;
     * }
     * 
     * if (tempDir == 1) {
     * currBlock.y -= 10;
     * } else if (tempDir == 3) {
     * currBlock.y += 10;
     * } else if (tempDir == 2) {
     * currBlock.x += 10;
     * } else if (tempDir == 4) {
     * currBlock.x -= 10;
     * }
     * }
     * 
     * }
     * 
     * else if (cubes.get(i) instanceof AirCraft) {
     * if (player.x == currBlock.x && player.y == currBlock.y) {
     * int tempDir = 1;
     * if (currBlock.dir == 1) {
     * 
     * tempDir = ranDircet.nextInt(3) + 2;
     * } else if (currBlock.dir == 3) {
     * 
     * tempDir = ranDircet.nextInt(2) + 1;
     * } else if (currBlock.dir == 2) {
     * 
     * tempDir = ranDircet.nextInt(2) + 3;
     * } else if (currBlock.dir == 4) {
     * 
     * tempDir = ranDircet.nextInt(3) + 1;
     * }
     * 
     * if (tempDir == 1) {
     * currBlock.y -= 10;
     * } else if (tempDir == 3) {
     * currBlock.y += 10;
     * } else if (tempDir == 2) {
     * currBlock.x += 10;
     * } else if (tempDir == 4) {
     * currBlock.x -= 10;
     * }
     * }
     * }
     * 
     * }
     * }
     * }
     * 
     * else if (t instanceof Enemy) {
     * Enemy currBlock = (Enemy) t;
     * 
     * for (int i = 0; i < cubes.size(); i++) {
     * if (cubes.get(i) instanceof Enemy) {
     * Enemy e = (Enemy) cubes.get(i);
     * if (e.x == currBlock.x && e.y == currBlock.y) {
     * int tempDir = 1;
     * if (currBlock.dir == 1) {
     * 
     * tempDir = ranDircet.nextInt(3) + 2;
     * } else if (currBlock.dir == 3) {
     * 
     * tempDir = ranDircet.nextInt(2) + 1;
     * } else if (currBlock.dir == 2) {
     * 
     * tempDir = ranDircet.nextInt(2) + 3;
     * } else if (currBlock.dir == 4) {
     * 
     * tempDir = ranDircet.nextInt(3) + 1;
     * }
     * 
     * if (tempDir == 1) {
     * currBlock.y -= 10;
     * } else if (tempDir == 3) {
     * currBlock.y += 10;
     * } else if (tempDir == 2) {
     * currBlock.x += 10;
     * } else if (tempDir == 4) {
     * currBlock.x -= 10;
     * }
     * }
     * } else if (cubes.get(i) instanceof Friend) {
     * Friend f = (Friend) cubes.get(i);
     * if (f.x == currBlock.x && f.y == currBlock.y) {
     * cubes.remove(currBlock);
     * currBlock.dead = true;
     * cubes.remove(f);
     * f.dead = true;
     * }
     * 
     * } else if (cubes.get(i) instanceof AirCraft) {
     * 
     * if (player.x == currBlock.x && player.y == currBlock.y) {
     * ripPlayer = true;
     * currBlock.dead = true;
     * }
     * }
     * 
     * }
     * }
     * 
     * }
     */

    public synchronized boolean iDestroyThemAll() // HHAH HHA HAHA!! (in villanish voice)
    {

        for (int i = 0; i < cubes.size(); i++) {
            if (cubes.get(i) instanceof Enemy) {
                return false;
            }
        }

        return true;
    }

    public synchronized boolean oppsFriendInTheWay(int desx, int desy) // try to dont bump a bro
    {
        for (int i = 0; i < cubes.size(); i++) {
            if (cubes.get(i) instanceof Friend) {
                Friend f = (Friend) cubes.get(i);

                if (f.x == desx && f.y == desy) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized boolean oppsEnemyInTheWay(int desx, int desy) // try to dont bump a Enemy bro
    {
        for (int i = 0; i < cubes.size(); i++) {
            if (cubes.get(i) instanceof Enemy) {
                Enemy f = (Enemy) cubes.get(i);

                if (f.x == desx && f.y == desy) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!ripPlayer) {
            Lazer r = new Lazer(player.x + 5, player.y + 2, Color.ORANGE, 1);
            Lazer l = new Lazer(player.x + 5, player.y + 2, Color.ORANGE, -1);
            // adding extra numbers because of we want to shoot laser throught the middle of
            // the cube
            cubes.add(r);
            cubes.add(l);
            r.start();
            l.start();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

}
