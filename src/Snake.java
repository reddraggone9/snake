/*Possible Changes:
 * read constants from text file
 * add custom snake length increase
 * speed up each time food is eaten
 * sets up paused; press arrows to start
 * white --> field and black --> snake
 * scoreboard (highscore?)
 * Win/Lose dialog followed by retry (might be easiest to implement in main and instantiate new snake)
 * GIT SOME MOAR
 */

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Snake extends JPanel {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public static final int HEIGHT = 8, WIDTH = 16;
    public static final int DELAY = 500;
    public static final int INIT_LENGTH = 5;
    public static final int FOOD_COUNT = 50;
    public static final Direction START_DIRECTION = Direction.RIGHT;

    private ImageIcon white, black, food;
    private JPanel base;
    private JLabel[][] field;

    LinkedList<int[]> snake;
    private Direction dir, lastDir;
    Timer timer;
    Random rng;

    public static void main(String args[]) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        JFrame frame = new JFrame("Snake - Eat to Win");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Snake snake = new Snake();
        frame.getContentPane().add(snake);

        frame.pack();
        frame.setVisible(true);
    }

    public Snake() {
        setLayout(new BorderLayout());
        addKeyListener (new DirectionListener());
        setFocusable(true);

        white = new ImageIcon("white.gif");
        black = new ImageIcon("black.gif");
        food = new ImageIcon("red.gif");

        base = new JPanel(new GridLayout(HEIGHT, WIDTH));
        field = new JLabel[HEIGHT][WIDTH];
        for (int i = 0; i < HEIGHT; i++)
            for (int j = 0; j < WIDTH; j++) {
                base.add(field[i][j] = new JLabel(white));
            }
        add(base, BorderLayout.CENTER);
        snake = new LinkedList<int[]>();
        for (int i = 0; i < INIT_LENGTH; i++) {
            add(new int[] { HEIGHT/2-INIT_LENGTH/2, WIDTH/2 + i });
        }
        
        rng = new Random();
        for(int i = 0; i < FOOD_COUNT; i++)
        	addFood(0);
        lastDir = dir = START_DIRECTION;
        
        timer = new Timer(DELAY, taskPerformer);
        timer.start();
    }

    private void add(int[] pos) {
        snake.add(pos);
        field[pos[0]][pos[1]].setIcon(black);
    }
    
    private void remove() {
        int tail[] = snake.remove();
        field[tail[0]][tail[1]].setIcon(white);
    }
    
    private void addFood(int n)
    {
    	int whiteCount = HEIGHT*WIDTH-snake.size()-FOOD_COUNT+n;
    	if (whiteCount <= 0)
    		return;
    	int foodIndex = rng.nextInt(whiteCount) + 1;
    	whiteCount = 0; 
    	for(JLabel[] i : field)
    		 for(JLabel j : i){
    			 whiteCount += j.getIcon()==white?1:0;
    			 if(whiteCount == foodIndex) {
    				 j.setIcon(food);
    				 return;
    			 }
    		 }
    }
    
    public boolean isFood(int[] pos) { return field[pos[0]][pos[1]].getIcon()==food; }
    public boolean isSnake(int[] pos) {	return field[pos[0]][pos[1]].getIcon()==black; }

    ActionListener taskPerformer = new ActionListener() {
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
            head[0] = (head[0]+HEIGHT) % HEIGHT;
            head[1] = (head[1]+WIDTH) % WIDTH;
            if(isFood(head)) {
            	add(head);
            	addFood(1); // add(snake.getLast()[0], snake.getLast()[1]);
        	} else if(isSnake(head)) {
            	timer.stop();
            	// if(snake.size() == HEIGHT*WIDTH) You Win!
        	} else {
            	add(head);
            	remove();
        	}
        }
    };
    
    private class DirectionListener implements KeyListener {
        public void keyPressed (KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_NUMPAD8:
            case KeyEvent.VK_W:
            	dir = lastDir!=Direction.DOWN?Direction.UP:dir;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_NUMPAD2:
            case KeyEvent.VK_S:
                dir = lastDir!=Direction.UP?Direction.DOWN:dir;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_NUMPAD4:
            case KeyEvent.VK_A:
                dir = lastDir!=Direction.RIGHT?Direction.LEFT:dir;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_NUMPAD6:
            case KeyEvent.VK_D:
                dir = lastDir!=Direction.LEFT?Direction.RIGHT:dir;
                break;
            }
        }
        public void keyTyped (KeyEvent e) {}
        public void keyReleased (KeyEvent e) {}
    }
}