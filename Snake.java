/*Possible Changes:
 * add controls menu
 * speed up each time food is eaten
 * add highscore
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Snake extends JFrame {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private int height = 32, width = 32,
                delay = 100, delayDecay = 1,
                initLength = 5, addLength = 3,
                foodCount = 1, iconSize = 8;

    private final static String START_PROMPT = "Press any key to start!";
    private final static String SETTINGS_ERROR = "Problem with the file settings.txt\nMake sure it contains:\nGrid Height\nGrid Width\nGame Delay\nInital Snake Length\nAdded Snake Length\nFood Count";
    private final static String SCORE_PREFIX = "Score: ";
    private final static String PLAY_AGAIN = "Would you like to play again?";
    private final static String WIN_MESSAGE = "You Won Snake!\nNow go outside and live a little.";

    private ImageIcon fieldIcon, snakeIcon, foodIcon;
    private JPanel base;
    private JLabel[][] field;
    private JLabel scoreBoard;

    private LinkedList<int[]> snake;
    private Direction dir, lastDir;
    private Timer timer;
    private Random rng;
    private int score, removeWait;
    private boolean finished;

    public static void main(String args[]) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        JFrame snake = new Snake("Snake - Eat to Win");
        snake.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        snake.setVisible(true);
    }

    public Snake(String s) {
        super(s);
        setLayout(new BorderLayout());
        addKeyListener(new DirectionListener());
        setFocusable(true);

        fieldIcon = createIcon(Color.WHITE);
        snakeIcon = createIcon(Color.BLACK);
        foodIcon = createIcon(Color.RED);

        rng = new Random();
        scoreBoard = new JLabel();
        add(scoreBoard, BorderLayout.NORTH);
        timer = new Timer(delay, gameLoop);

        setup();
    }

    private ImageIcon createIcon(Color color) {
        BufferedImage img = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(color);
        g.fillRect(0,0,iconSize,iconSize);
        return new ImageIcon(img);
    }

    private void setup() {
        readSettings();
        initGUI();
        initSnake();
    }

    private void readSettings() {
        try {
            Scanner scan = new Scanner(new File("settings.txt"));
            height = Integer.parseInt(scan.nextLine().trim());
            width = Integer.parseInt(scan.nextLine().trim());
            delay = Integer.parseInt(scan.nextLine().trim());
            initLength = Integer.parseInt(scan.nextLine().trim());
            addLength = Integer.parseInt(scan.nextLine().trim());
            foodCount = Integer.parseInt(scan.nextLine().trim());
            scan.close();
        } catch (Exception e) {
            System.out.println(SETTINGS_ERROR);
        }
    }

    private void initGUI() {
        base = new JPanel(new GridLayout(height, width));
        field = new JLabel[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                base.add(field[i][j] = new JLabel(fieldIcon));
        scoreBoard.setText(START_PROMPT);
        add(base, BorderLayout.CENTER);
        pack();
    }

    private void initSnake() {
        snake = new LinkedList<int[]>();
        for (int i = 0; i < initLength; i++) {
            add(new int[] { height / 2, width / 2 - initLength / 2 + i });
        }
        for (int i = 0; i < foodCount; i++)
            addFood(i);
        lastDir = dir = Direction.RIGHT;
        removeWait = score = 0;
        finished = false;
        timer.setDelay(delay);
    }

    private void add(int[] pos) {
        snake.add(pos);
        field[pos[0]][pos[1]].setIcon(snakeIcon);
    }

    private void remove() {
        if (removeWait > 0)
            removeWait--;
        else {
            int tail[] = snake.remove();
            field[tail[0]][tail[1]].setIcon(fieldIcon);
        }
    }

    private void addFood() {
        addFood(foodCount);
    }

    private void addFood(int food) {
        if (food == foodCount)
            scoreBoard.setText("Score: " + ++score);
        int whiteCount = height * width - snake.size() - food;
        if (whiteCount <= 0)
            return;
        int foodIndex = rng.nextInt(whiteCount) + 1;
        whiteCount = 0;
        for (JLabel[] i : field)
            for (JLabel j : i) {
                whiteCount += j.getIcon() == fieldIcon ? 1 : 0;
                if (whiteCount == foodIndex) {
                    j.setIcon(foodIcon);
                    return;
                }
            }
    }

    public boolean isFood(int[] pos) {
        return field[pos[0]][pos[1]].getIcon() == foodIcon;
    }

    public boolean isSnake(int[] pos) { // Straightforward unless it's the tail
                                        // and it will be removed
        return (!(pos[0] == snake.getFirst()[0] && pos[1] == snake.getFirst()[1]) || removeWait > 0)
                && field[pos[0]][pos[1]].getIcon() == snakeIcon;
    }

    ActionListener gameLoop = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int[] head = Arrays.copyOf(snake.getLast(), 2);
            switch (dir) {
            case UP:
                head[0] -= 1;
                break;
            case DOWN:
                head[0] += 1;
                break;
            case LEFT:
                head[1] -= 1;
                break;
            case RIGHT:
                head[1] += 1;
                break;
            }
            lastDir = dir;
            head[0] = (head[0] + height) % height;
            head[1] = (head[1] + width) % width;

            if (isSnake(head)) {
                timer.stop();
                finished = true;
                if (snake.size() == height * width)
                    if(JOptionPane.showConfirmDialog(Snake.this, WIN_MESSAGE, "Game Over", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                        System.exit(0);
                switch (JOptionPane.showConfirmDialog(Snake.this, PLAY_AGAIN)) {
                case JOptionPane.YES_OPTION:
                    setup();
                    break;
                case JOptionPane.NO_OPTION:
                    System.exit(0);
                    break;
                }
            } else {
                if (isFood(head)) {
                    removeWait += addLength;
                    addFood();
                }
                remove();
                add(head);
            }
        }
    };

    private class DirectionListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            boolean skip = false;
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_NUMPAD8:
            case KeyEvent.VK_W:
                if (lastDir != Direction.DOWN)
                    dir = Direction.UP;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_NUMPAD2:
            case KeyEvent.VK_S:
                if (lastDir != Direction.UP)
                    dir = Direction.DOWN;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_NUMPAD4:
            case KeyEvent.VK_A:
                if (lastDir != Direction.RIGHT)
                    dir = Direction.LEFT;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_NUMPAD6:
            case KeyEvent.VK_D:
                if (lastDir != Direction.LEFT)
                    dir = Direction.RIGHT;
                break;
            case KeyEvent.VK_R:
                setup();
            case KeyEvent.VK_P:
                timer.stop();
                skip = true;
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
            }
            if (!(timer.isRunning() || finished || skip)) {
                timer.start();
                scoreBoard.setText(SCORE_PREFIX + score);
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }
    }
}
