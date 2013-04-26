/*Possible Changes:
 * Win/Lose dialog followed by retry (might be easiest to implement in main and instantiate new snake)
 * speed up each time food is eaten
 * add highscore
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Snake extends JFrame {
	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}

	private int height = 32, width = 32;
	private int delay = 100;
	private int initLength = 5, addLength = 1;
	private int foodCount = 1;

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
		snake.pack();
		snake.setVisible(true);
	}

	public Snake(String s) {
		super(s);
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
			System.out.println("Problem with the file settings.txt");
			System.out.println("Make sure it contains:\nGrid Height\nGrid Width\nGame Delay\nInital Snake Length\nAdded Snake Length\nFood Count");
		}

		setLayout(new BorderLayout());
		addKeyListener(new DirectionListener());
		setFocusable(true);

		fieldIcon = new ImageIcon("field.gif");
		snakeIcon = new ImageIcon("snake.gif");
		foodIcon = new ImageIcon("food.gif");

		base = new JPanel(new GridLayout(height, width));
		field = new JLabel[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				base.add(field[i][j] = new JLabel(fieldIcon));

		snake = new LinkedList<int[]>();
		for (int i = 0; i < initLength; i++) {
			add(new int[] { height / 2, width / 2 - initLength / 2 + i });
		}

		scoreBoard = new JLabel();
		scoreBoard.setText("Press any key to start!");

		add(scoreBoard, BorderLayout.NORTH);
		add(base, BorderLayout.CENTER);

		rng = new Random();
		for (int i = 0; i < foodCount; i++)
			addFood(i);
		lastDir = dir = Direction.RIGHT;
		removeWait = score = 0;
		finished = false;

		timer = new Timer(delay, taskPerformer);
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

	public boolean isSnake(int[] pos) { //Straightforward unless it's the tail and it will be removed
		return (!(pos[0] == snake.getFirst()[0] && pos[1] == snake.getFirst()[1]) || removeWait > 0) && field[pos[0]][pos[1]].getIcon() == snakeIcon;
	}

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
			head[0] = (head[0] + height) % height;
			head[1] = (head[1] + width) % width;
			
			if (isSnake(head)) {
				timer.stop();
				finished = true;
				// if(snake.size() == HEIGHT*WIDTH) You Win!
			}else {
				if (isFood(head)) { // add(snake.getLast()[0], snake.getLast()[1]);
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
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
			case KeyEvent.VK_NUMPAD8:
			case KeyEvent.VK_W:
				dir = lastDir != Direction.DOWN ? Direction.UP : dir;
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_NUMPAD2:
			case KeyEvent.VK_S:
				dir = lastDir != Direction.UP ? Direction.DOWN : dir;
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_NUMPAD4:
			case KeyEvent.VK_A:
				dir = lastDir != Direction.RIGHT ? Direction.LEFT : dir;
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_NUMPAD6:
			case KeyEvent.VK_D:
				dir = lastDir != Direction.LEFT ? Direction.RIGHT : dir;
				break;
			}
			if (!(timer.isRunning() || finished)) {
				timer.start();
				scoreBoard.setText("Score: " + score);
			}
		}

		public void keyTyped(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}
	}
}