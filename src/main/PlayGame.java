package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class PlayGame extends JFrame implements Runnable {
	private boolean isRunning, mainMenuHovering, resumeHovering, countdown, deathAnimation;
	private Image dbImage;
	private Graphics dbg;
	public static int[][] levelMap;
	public static int[][] levelObjects;
	private boolean drawMap = true;
	private boolean mouseClicked = false;
	private Image topRightCorner, topLeftCorner, topPiece, bottomPiece, rightPiece, leftPiece, allPiece, 
	bottomRightCorner, bottomLeftCorner, verticalSides, horizontalSides, lightBlue, whitePiece, noSides, greenPiece,
	rightEnd, bottomEnd, leftEnd, topEnd, playerPic, food, enemy, mainMenuPicture, resumePicture, mainMenuHover, resumeHover, pauseBackground, background,
	playerColorOne, playerColorTwo, playerColorThree, playerColorFour;
	private MyRectangle player, winSquare, resumeR, mainMenuR, checkpoint;
	private ArrayList<MyRectangle> ground = new ArrayList<MyRectangle>();
	private ArrayList<MyRectangle> enemies = new ArrayList<MyRectangle>();
	private ArrayList<MyRectangle> foodObjects = new ArrayList<MyRectangle>();
	
	public static int enemySpeed = 7;
	public static int transitionTime;
	public static String transitionMessage;
	private int initialX, initialY, foodCount, mx, my;
	private int countdownNumber = 4;
	private float alpha;
	private boolean schylersException = false;
	
	private PrintWriter writer;
	
	public static int currentLevel;
	
	private List<String> dataFile;
	private String path;
	private Path readPath;
	
	private boolean died, playTransition;
	private static int deaths;
	private boolean pause = false;
	private boolean refresh = false;
	private File file = null;
	private File wkdir = null;

	public PlayGame(int width, int height, String title) {
		setSize(width, height);
		setTitle(title);
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		isRunning = true;
		addKeyListener(new AL());
		addMouseListener(new ML());
		addMouseMotionListener(new ML());
		System.out.println(currentLevel + " CURRENT LEVEL");
		foodCount = 0;
		
		if(System.getProperty("os.name").toLowerCase().contains("linux")) {
			file = new File(System.getProperty("user.home") + "/impossibleGame/userData.txt");
			wkdir = new File(System.getProperty("user.home") + "/impossibleGame");
		}
		else if(System.getProperty("os.name").toLowerCase().contains("windows")) {
			file = new File(System.getProperty("user.home") + File.separator + "impossibleGame" + File.separator + "userData.txt");
			wkdir = new File(System.getProperty("user.home") + File.separator + "impossibleGame");
		}
		else if(System.getProperty("os.name").toLowerCase().contains("mac os")) {
			file = new File(System.getProperty("user.home") + File.separator + "impossibleGame" + File.separator + "userData.txt");
			wkdir = new File(System.getProperty("user.home") + File.separator + "impossibleGame");
		}
		
		readPath = file.toPath();
		
		try {
			dataFile = Files.readAllLines(readPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0; i < dataFile.size(); i++) {
			String[] truth = dataFile.get(i).split("=");
			if(truth[0].equals("deaths"))
				deaths = Integer.parseInt(truth[1]);
		}

		for(int i=0; i < levelObjects.length; i++) {
			for(int j=0; j < levelObjects[i].length; j++) {	
				if(levelObjects[i][j] == 12) {
					player = new MyRectangle(30, 30, j * 40, i *40);
					initialX = player.x;
					initialY = player.y;
					player.i = i;
					player.j = j;
				}
				
				if(levelObjects[i][j] == 20) {
					foodCount++;
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					MyRectangle currentFood = new MyRectangle(20, 20, x, y);
					currentFood.i = i;
					currentFood.j = j;
					foodObjects.add(currentFood);
				}
				
				
				//Special Enemy
				if(levelObjects[i][j] == 102) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.dy = 0;
					enemyP.dx = enemySpeed + 1;
					enemyP.minX = enemyP.x;
					enemyP.maxX = enemyP.x + (40 * 11);
					enemyP.id = 20;
					enemies.add(enemyP);
				}
				
				//Enemy moving up				
				if(levelObjects[i][j] >= 25 && (levelObjects[i][j] - 25) % 8 == 0) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					int multiplyFactor = 0;
					
					if(levelObjects[i][j] == 25)
						multiplyFactor = 10;
					else
						multiplyFactor = 11 - ((levelObjects[i][j] - 25) / 8);
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.dy = -enemySpeed;
					enemyP.minY = enemyP.y - (40 * multiplyFactor);
					enemyP.maxY = enemyP.y;
					enemyP.id = 1;
					enemies.add(enemyP);
				}				
				
				
				//Enemy moving down
				if(levelObjects[i][j] >= 26 && (levelObjects[i][j] - 26) % 8 == 0) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					int multiplyFactor = 0;
					
					if(levelObjects[i][j] == 26)
						multiplyFactor = 10;
					else
						multiplyFactor = 11 - ((levelObjects[i][j] - 26) / 8);
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.id = 1;
					enemyP.dy = -enemySpeed;
					enemyP.maxY = enemyP.y + (40 * multiplyFactor);
					enemyP.minY = enemyP.y;
					enemies.add(enemyP);
				}
				
				//Enemy moving left
				if(levelObjects[i][j] >= 27 && (levelObjects[i][j] - 27) % 8 == 0) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					int multiplyFactor = 0;
					
					if(levelObjects[i][j] == 27)
						multiplyFactor = 10;
					else
						multiplyFactor = 11 - ((levelObjects[i][j] - 27) / 8);
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.id = 2;
					enemyP.dx = -enemySpeed;
					enemyP.minX = enemyP.x - (40 * multiplyFactor);
					enemyP.maxX = enemyP.x;
					enemies.add(enemyP);
				}
					
				//Enemy moving right
				if(levelObjects[i][j] >= 28 && (levelObjects[i][j] - 28) % 8 == 0) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					int multiplyFactor = 0;
					
					if(levelObjects[i][j] == 28)
						multiplyFactor = 10;
					else
						multiplyFactor = 11 - ((levelObjects[i][j] - 28) / 8);
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.id = 2;
					enemyP.dx = -enemySpeed;
					enemyP.maxX = enemyP.x + (40 * multiplyFactor);
					enemyP.minX = enemyP.x;
					enemies.add(enemyP);
				}
				
				//Enemy moving in a square of certain radius
				if(levelObjects[i][j] >= 29 && (levelObjects[i][j] - 29) % 8 == 0) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					int multiplyFactor = 0;
					
					if(levelObjects[i][j] == 29)
						multiplyFactor = 10;
					else
						multiplyFactor = 11 - ((levelObjects[i][j] - 29) / 8);
					
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.id = 3;
					enemyP.dx = 0;
					enemyP.dy = -enemySpeed;
					enemyP.maxY = enemyP.y + (40 * multiplyFactor);
					enemyP.minY = enemyP.y;
					enemyP.maxX = enemyP.x + (40 * multiplyFactor);
					enemyP.minX = enemyP.x;
					enemies.add(enemyP);
				}
				
				//Stationary enemy
				if(levelObjects[i][j] == 30) {
					int y = (i * 40) + 10;
					int x = (j * 40) + 10;
					MyRectangle enemyP = new MyRectangle(10, 10, x, y);
					enemyP.dy = 0;
					enemyP.dx = 0;
					enemies.add(enemyP);
				}
			}
		}
		
		for(int i=0; i < levelMap.length; i++) {
			for(int j=0; j < levelMap[i].length; j++) {
				int currentTile = levelMap[i][j];
				switch(currentTile) {
				case 1:
					MyRectangle OtherNewPiece = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(OtherNewPiece);
					break;
				case 2:
					MyRectangle OtherNewPiece1 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(OtherNewPiece1);
					break;
				case 3:
					MyRectangle OtherNewPiece2 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(OtherNewPiece2);
					break;
				case 6:
					MyRectangle piece = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(piece);
					break;
				case 7:
					MyRectangle piece1 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(piece1);
					break;
				case 8:
					winSquare = new MyRectangle(40, 40, j * 40, i * 40);
					break;
				case 9:
					MyRectangle somePiece = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece);
					break;
				case 10:
					MyRectangle somePiece1 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece1);
					break;
				case 11:
					MyRectangle somePiece144 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece144);
					break;
				case 14:
					MyRectangle somePiece2 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece2);
					break;
				case 15:
					MyRectangle somePiece3 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece3);
					break;
				case 17:
					MyRectangle somePiece4 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece4);
					break;
				case 18:
					MyRectangle somePiece5 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece5);
					break;
				case 19:
					MyRectangle somePiece6 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece6);
					break;
				case 22:
					MyRectangle somePiece7 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece7);
					break;
				case 23:
					MyRectangle somePiece8 = new MyRectangle(40, 40, j * 40, i * 40);
					ground.add(somePiece8);
					break;
				case 101:
					checkpoint = new MyRectangle(40, 40, j * 40, i * 40);
					checkpoint.i = i;
					checkpoint.j = j;
					break;
				}
			}
		}
		
		init();
	}
	
	public class ML extends MouseAdapter implements MouseMotionListener {
		public void mouseMoved(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
		}
		
		public void mousePressed(MouseEvent e) {
			mouseClicked = true;
		}
	}
	
	public class AL extends KeyAdapter {
		@SuppressWarnings("static-access")
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if(keyCode == e.VK_UP || keyCode == e.VK_W)
				player.dy = -5;
			if(keyCode == e.VK_DOWN || keyCode == e.VK_S)
				player.dy = 5;
			if(keyCode == e.VK_LEFT || keyCode == e.VK_A)
				player.dx = -5;
			if(keyCode == e.VK_RIGHT || keyCode == e.VK_D)
				player.dx = 5;
			if(keyCode == e.VK_ESCAPE) 
				pause = true;
			if(keyCode == e.VK_F5) 
				refresh = true;
		}
		
		@SuppressWarnings("static-access")
		public void keyReleased(KeyEvent e) {
			int keyCode = e.getKeyCode();
			if(keyCode == e.VK_UP || keyCode == e.VK_W)
				player.dy = 0;
			if(keyCode == e.VK_DOWN || keyCode == e.VK_S)
				player.dy = 0;
			if(keyCode == e.VK_RIGHT || keyCode == e.VK_D)
				player.dx = 0;
			if(keyCode == e.VK_LEFT || keyCode == e.VK_A) 
				player.dx = 0;
		}
	}
	
	public void init() {
		BufferedImageLoader loader = new BufferedImageLoader();
		BufferedImage spriteSheet = null;
		
		try {
			spriteSheet = loader.loadImage(wkdir.toString() + File.separator + "resources" + File.separator + "spritesheet.png");
			background = loader.loadImage(wkdir.toString() + File.separator + "resources" + File.separator + "background.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SpriteSheet ss = new SpriteSheet(spriteSheet);
		
		bottomRightCorner = ss.grabSprite(700, 220, 40, 40);
		bottomLeftCorner = ss.grabSprite(750, 220, 40, 40);
		topLeftCorner = ss.grabSprite(800, 220, 40, 40);
		noSides = ss.grabSprite(850, 220, 40, 40);
		greenPiece = ss.grabSprite(900, 220, 40, 40);
		rightEnd = ss.grabSprite(950, 220, 40, 40);
		bottomEnd = ss.grabSprite(1000, 220, 40, 40);
		bottomPiece = ss.grabSprite(700, 270, 40, 40);
		leftPiece = ss.grabSprite(750, 270, 40, 40);
		topRightCorner = ss.grabSprite(800, 270, 40, 40);
		playerPic = ss.grabSprite(855, 275, 30, 30);
		lightBlue = ss.grabSprite(900, 270, 40, 40);
		horizontalSides = ss.grabSprite(950, 270, 40, 40);
		leftEnd = ss.grabSprite(1000, 270, 40, 40);
		allPiece = ss.grabSprite(700, 320, 40, 40);
		topPiece = ss.grabSprite(750, 320, 40, 40);
		rightPiece = ss.grabSprite(800, 320, 40, 40);
		food = ss.grabSprite(850, 320, 40, 40);
		whitePiece = ss.grabSprite(900, 320, 40, 40);
		verticalSides = ss.grabSprite(950, 320, 40, 40);
		topEnd = ss.grabSprite(1000, 320, 40, 40);
		enemy = ss.grabSprite(1060, 330, 20, 20);
		mainMenuPicture = ss.grabSprite(0, 361, 163, 140);
		resumePicture = ss.grabSprite(0, 503, 245, 60);
		mainMenuHover = ss.grabSprite(174,361, 163, 140);
		resumeHover = ss.grabSprite(254, 503, 245, 60);
		pauseBackground = ss.grabSprite(5, 580, 530, 300);
		
		resumeR = new MyRectangle(245, 60, 210, 310);
		mainMenuR = new MyRectangle(163, 140, 210, 160);
		playerColorOne = ss.grabSprite(400, 360, 64, 64);
		playerColorTwo = ss.grabSprite(470, 360, 64, 64);
		playerColorThree = ss.grabSprite(550, 360, 64, 64);
		playerColorFour = ss.grabSprite(623, 360, 64, 64);
	}	
	
	public void move() {
		player.x += player.dx;
		player.y += player.dy;
		
		for(int i=0; i < ground.size(); i++) {
			player.blockRectangle(player, ground.get(i));
		}
		
		for(int i=0; i < enemies.size(); i++) {
			
			//For enemies moving up or down
			if(enemies.get(i).id == 1) {
				enemies.get(i).y += enemies.get(i).dy;
				
				if(enemies.get(i).y > enemies.get(i).maxY) {
					enemies.get(i).y = enemies.get(i).maxY;
					enemies.get(i).dy *= -1;
				} else if(enemies.get(i).y < enemies.get(i).minY) {
					enemies.get(i).y = enemies.get(i).minY;
					enemies.get(i).dy *= -1;
				}
			} //For enemies moving left or right 
			else if(enemies.get(i).id == 2 || enemies.get(i).id == 20) {
				enemies.get(i).x += enemies.get(i).dx;
				
				if(enemies.get(i).x > enemies.get(i).maxX) {
					enemies.get(i).x = enemies.get(i).maxX;
					enemies.get(i).dx *= -1;
				} else if(enemies.get(i).x < enemies.get(i).minX) {
					enemies.get(i).x = enemies.get(i).minX;
					enemies.get(i).dx *= -1;
				}
			} //For enemies moving in a square radius
			else if(enemies.get(i).id == 3) {
				MyRectangle currentEnemy = enemies.get(i);
				
				currentEnemy.x += currentEnemy.dx;
				currentEnemy.y += currentEnemy.dy;
				
				if(currentEnemy.x > currentEnemy.maxX) {
					currentEnemy.x = currentEnemy.maxX;
					currentEnemy.dx = 0;
					currentEnemy.dy = enemySpeed;
				} else if(currentEnemy.x < currentEnemy.minX) {
					currentEnemy.x = currentEnemy.minX;
					currentEnemy.dx = 0;
					currentEnemy.dy = -enemySpeed;
				}
				
				if(currentEnemy.y > currentEnemy.maxY) {
					currentEnemy.y = currentEnemy.maxY;
					currentEnemy.dy = 0;
					currentEnemy.dx = -enemySpeed;
				} else if(currentEnemy.y < currentEnemy.minY){
					currentEnemy.y = currentEnemy.minY;
					currentEnemy.dy = 0;
					currentEnemy.dx = enemySpeed;
				}
			}
			
			if(enemies.get(i).blockRectangle(player, enemies.get(i)) != 0) {
				died = true;
				deaths++;
			}
			
			if(died) {
				playDeathAnimation();
				player.x = initialX;
				player.y = initialY;
				died = false;
				foodCount = 0;
				
				for(int j=0; j < foodObjects.size(); j++) {
					MyRectangle currentFood = foodObjects.get(j);
					currentFood.width = 20;
					currentFood.height = 20;
					if(currentFood.x < 0)
						currentFood.x += 10000;
					
					if(currentFood.y < 0)
						currentFood.y += 10000;
					
					levelObjects[currentFood.i][currentFood.j] = 20;
					foodCount++;
				}
				
				path = file.getAbsolutePath();
				
				
				try {
					writer = new PrintWriter(path, "UTF-8");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				for(int j=0; j < 35; j++) {
					int x = j % 7;
					int y = j % 5;
					boolean lockValue = false;
					if(j < currentLevel)
						lockValue = true;
					
					String truth;
					if(lockValue || MainMenu.levels[x][y].unlocked)
						truth = "true\n";
					else 
						truth = "false\n";
					writer.write(j + 1 + "=" + truth);
					
				}
				writer.write("deaths=" + deaths);
				
				writer.close();
			}
				
		}
		
		for(int i=0; i < foodObjects.size(); i++) {
			MyRectangle currentFood = foodObjects.get(i);
			
			if(currentFood.blockRectangle(currentFood, player) != 0) {
				levelObjects[currentFood.i][currentFood.j] = 0;
				
				currentFood.x -= 10000;
				currentFood.y -= 10000;
				currentFood.width = 0;
				currentFood.height = 0;
				foodCount--;
				System.out.println(foodCount);	
			}
		}
		
		if(checkpoint != null) {
			if(player.x + 10 > checkpoint.x && player.x + player.width - 10 < checkpoint.x + checkpoint.width && player.y + 10
					> checkpoint.y && player.y + player.height - 10 < checkpoint.y + checkpoint.height) {
				for(int i=0; i < foodObjects.size(); i++) {
					MyRectangle currentFood = foodObjects.get(i);
					if(currentFood.x < 0 || currentFood.y < 0) {
						foodObjects.remove(i);
					}
				}
				
				if(initialX != checkpoint.x && initialY != checkpoint.y) {
					initialX = checkpoint.x;
					initialY = checkpoint.y;
					levelObjects[checkpoint.i][checkpoint.j] = 12;
					levelObjects[player.i][player.j] = 0;
				}
			}
		}
		
		if(player.x + 10 > winSquare.x && player.x + player.width - 10 < winSquare.x + winSquare.width && player.y + 10
				> winSquare.y && player.y + player.height - 10 < winSquare.y + winSquare.height && foodCount == 0) {
			path = file.getAbsolutePath();
			
			
			try {
				writer = new PrintWriter(path, "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			for(int i=0; i < 35; i++) {
				int x = i % 7;
				int y = i % 5;
				boolean lockValue = false;
				if(i < currentLevel + 1)
					lockValue = true;
				
				String truth;
				if(lockValue || MainMenu.levels[x][y].unlocked)
					truth = "true\n";
				else 
					truth = "false\n";
				writer.write(i + 1 + "=" + truth);
				
			}
			writer.write("deaths=" + deaths);
			
			writer.close();
			
			transitionAnimation(PlayGame.transitionMessage, PlayGame.transitionTime);
		}	
		
		if(refresh) {
			for(int j=0; j < foodObjects.size(); j++) {
				MyRectangle currentFood = foodObjects.get(j);
				currentFood.width = 20;
				currentFood.height = 20;
				if(currentFood.x < 0)
					currentFood.x += 10000;
				
				if(currentFood.y < 0)
					currentFood.y += 10000;
				
				levelObjects[currentFood.i][currentFood.j] = 20;
				foodCount++;
			}
			
			setVisible(false);
			isRunning = false;
			dispose();
			refresh = false;
			Main.init();
		}
	}
	
	public void transitionAnimation(String message, int time) {
		playTransition = true;
		drawMap = false;
		
		if(currentLevel < 36)
			currentLevel++;
		else {
			Main.playGame = false;
			Main.drawMenu = true;
			Main.init();
			setVisible(false);
			isRunning = false;
			playTransition = false;
			dispose();
		}
			
		
		double currentTime = System.currentTimeMillis();
		double previousTime = System.currentTimeMillis();
		
		while(previousTime - currentTime < time * 1000) {
			previousTime = System.currentTimeMillis();
		}
		
		if(currentLevel < 20) {
			PlayGame.levelMap = GetLevelMap.getLevelMapOne(currentLevel);
			PlayGame.levelObjects = GetLevelMap.getLevelObjectsOne(currentLevel);
		}
		else {
			PlayGame.levelMap = GetLevelMapTwo.getLevelTwo(currentLevel);
			PlayGame.levelObjects = GetLevelMapTwo.getLevelObjectsTwo(currentLevel);
		}
		
		if(currentLevel < 36) {
			Main.playGame = true;
			Main.drawMenu = false;
			Main.init();
			PlayGame.deaths = deaths;
			setVisible(false);
			isRunning = false;
			playTransition = false;
			dispose();
		}
	}
	
	public void drawPause() {
		if(mx > mainMenuR.x && mx < mainMenuR.x + mainMenuR.width && my > mainMenuR.y && my < mainMenuR.y + mainMenuR.height) {
			mainMenuHovering = true;
			if(mouseClicked) {
				mouseClicked = false;
				Main.playGame = false;
				Main.drawMenu = true;
				Main.init();
				setVisible(false);
				isRunning = false;
				dispose();
			}
		} else {
			mainMenuHovering = false;
		}
		
		if(mx > resumeR.x && mx < resumeR.x + resumeR.width && my > resumeR.y && my < resumeR.y + resumeR.height){
			resumeHovering = true;
			if(mouseClicked) {
				pause = false;
				countdown = true;
				mouseClicked = false;
				isRunning = false;
				doCountDown();
			}
		} else {
			resumeHovering = false;
		}
	}
	
	public void doCountDown() {
		double previousTime = 0;
		while(true) {
			double time = System.currentTimeMillis();
			if(time - previousTime > 500) {
				countdownNumber--;
				previousTime = System.currentTimeMillis();
			}
			if(countdownNumber == 0) {
				isRunning = true;
				countdown = false;
				pause = false;
				countdownNumber = 4;
				run();
				break;
			}
		}
	}
	
	public void playDeathAnimation() {
		deathAnimation = true;
		double currentTime = System.currentTimeMillis();
		double previousTime = System.currentTimeMillis();
		alpha = 1;
		while(currentTime - previousTime < 1000) {
			currentTime = System.currentTimeMillis();
		} 
		deathAnimation = false;
	}
	
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paintComponent(dbg);
		g.drawImage(dbImage, 0, 0, this);
	}
	
	public void paintComponent(Graphics g) {
		if(drawMap) {
			for(int i=0; i < levelMap.length; i++) {
				for(int j=0; j < levelMap[i].length; j++) {
					int currentTile = levelMap[i][j];
					switch(currentTile) {
					case 1:
						g.drawImage(bottomRightCorner, j * 40, i * 40, 40, 40, null);
						break;
					case 2:
						g.drawImage(bottomLeftCorner, j * 40, i * 40, 40, 40, null);
						break;
					case 3:
						g.drawImage(topLeftCorner, j * 40, i * 40, 40, 40, null);
						break;
					case 4:
						g.drawImage(noSides, j * 40, i * 40, 40, 40, null);
						break;
					case 5:
						g.drawImage(greenPiece, j * 40, i * 40, 40, 40, null);
						break;
					case 6:
						g.drawImage(rightEnd, j * 40, i * 40, 40, 40, null);
						MyRectangle piece = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(piece);
						break;
					case 7:
						g.drawImage(bottomEnd, j * 40, i * 40, 40, 40, null);
						MyRectangle piece1 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(piece1);
						break;
					case 8:
						g.drawImage(greenPiece, j * 40, i * 40, 40, 40, null);
						break;
					case 101:
						g.drawImage(greenPiece, j * 40, i * 40, 40, 40, null);
						break;
					case 9:
						g.drawImage(bottomPiece, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece);
						break;
					case 10:
						g.drawImage(leftPiece, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece1 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece1);
						break;
					case 11:
						g.drawImage(topRightCorner, j * 40, i * 40, 40, 40, null);
						break;
					case 13:
						g.drawImage(lightBlue, j * 40, i * 40, 40, 40, null);
						break;
					case 14:
						g.drawImage(horizontalSides, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece2 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece2);
						break;
					case 15:
						g.drawImage(leftEnd, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece3 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece3);
						break;
					case 17:
						g.drawImage(allPiece, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece4 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece4);
						break;
					case 18:
						g.drawImage(topPiece, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece5 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece5);
						break;
					case 19:
						g.drawImage(rightPiece, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece6 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece6);
						break;
					case 21:
						g.drawImage(whitePiece, j * 40, i * 40, 40, 40, null);
						break;
					case 22:
						g.drawImage(verticalSides, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece7 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece7);
						break;
					case 23:
						g.drawImage(topEnd, j * 40, i * 40, 40, 40, null);
						MyRectangle somePiece8 = new MyRectangle(40, 40, j * 40, i * 40);
						ground.add(somePiece8);
						break;
					}
				}
			}
			
			for(int i=0; i < levelObjects.length; i++) {
				for(int j=0; j < levelObjects[i].length; j++) {
					if(levelObjects[i][j] == 20)
						g.drawImage(food, j * 40, i * 40, 40, 40, null);
				}
			}
			
			for(int i=0; i < enemies.size(); i++) {
				g.drawImage(enemy, enemies.get(i).x, enemies.get(i).y, 20, 20, null);
			}
			
			if(!schylersException) {
				try {
					if(deathAnimation) {
						if(alpha > 0.01)
							alpha -= 0.01; //draw half transparent
						AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
						((Graphics2D) g).setComposite(ac);
						if(MainMenu.playerColor == 0)
							g.drawImage(playerPic, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 1)
							g.drawImage(playerColorOne, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 2)
							g.drawImage(playerColorTwo, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 3)
							g.drawImage(playerColorThree, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 4)
							g.drawImage(playerColorFour, player.x, player.y, player.width, player.height, null);
					} else {
						if(MainMenu.playerColor == 0)
							g.drawImage(playerPic, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 1)
							g.drawImage(playerColorOne, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 2)
							g.drawImage(playerColorTwo, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 3)
							g.drawImage(playerColorThree, player.x, player.y, player.width, player.height, null);
						else if(MainMenu.playerColor == 4)
							g.drawImage(playerColorFour, player.x, player.y, player.width, player.height, null);
					}
				} catch(Exception e) {
					schylersException = true;
					System.out.println("SCHYLERS EXCEPTION");
				}
			} else {
				if(deathAnimation) {
					if(alpha > 0.01)
						alpha -= 0.01; //draw half transparent
					AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha);
					((Graphics2D) g).setComposite(ac);
					g.setColor(Color.black);
					g.fillRect(player.x, player.y, player.width, player.height);
					if(MainMenu.playerColor == 0) 
						g.setColor(Color.red);
					else if(MainMenu.playerColor == 1)
						g.setColor(Color.green);
					else if(MainMenu.playerColor == 2)
						g.setColor(Color.cyan);
					else if(MainMenu.playerColor == 3)
						g.setColor(Color.MAGENTA);
					else if(MainMenu.playerColor == 4)
						g.setColor(Color.orange);
					g.fillRect(player.x + 2, player.y + 2, player.width - 4, player.height - 4);
				} else {
					g.setColor(Color.black);
					g.fillRect(player.x, player.y, player.width, player.height);
					if(MainMenu.playerColor == 0) 
						g.setColor(Color.red);
					else if(MainMenu.playerColor == 1)
						g.setColor(Color.green);
					else if(MainMenu.playerColor == 2)
						g.setColor(Color.cyan);
					else if(MainMenu.playerColor == 3)
						g.setColor(Color.MAGENTA);
					else if(MainMenu.playerColor == 4)
						g.setColor(Color.orange);
					g.fillRect(player.x + 2, player.y + 2, player.width - 4, player.height - 4);
				}
			}
		}
		
		if(pause) {
			g.drawImage(pauseBackground, 200, 150, 530, 300, null);
			
			if(mainMenuHovering)
				g.drawImage(mainMenuHover, 210, 160, 156, 135, null);
			else
				g.drawImage(mainMenuPicture, 210, 160, 156, 135, null);
			
			if(resumeHovering)
				g.drawImage(resumeHover, 210, 310, 235, 50, null);
			else
				g.drawImage(resumePicture, 210, 310, 235, 50, null);
		}
		
		if(countdown) {
			Font otherFont = new Font("Times New Roman", 0, 80);
			g.setColor(Color.black);
			g.setFont(otherFont);
			g.drawString(Integer.toString(countdownNumber), 350, 250);
		}
		
		if(playTransition) {
			g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
			Font otherFont = new Font("Times New Roman", 0, 40);
			g.setColor(Color.decode("0x24543d"));
			g.setFont(otherFont);
			drawString(g, PlayGame.transitionMessage, 30, 200, 900);
		}
		
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1);
		((Graphics2D) g).setComposite(ac);
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), 40);
		
		g.setColor(Color.white);
		Font font = new Font("Times New Roman", 0, 30);
		g.setFont(font);
		g.drawString("Deaths: " + deaths, (getWidth() / 2) - 50, 30);
		
		repaint();
	}
	
	public void drawString(Graphics g, String s, int x, int y, int width)
	{
	    // FontMetrics gives us information about the width,
	    // height, etc. of the current Graphics object's Font.
	    FontMetrics fm = g.getFontMetrics();

	    int lineHeight = fm.getHeight();

	    int curX = x;
	    int curY = y;

	    String[] words = s.split(" ");

	    for (String word : words)
	    {
	        // Find out the width of the word.
	        int wordWidth = fm.stringWidth(word + " ");

	        // If text exceeds the width, then move to next line.
	        if (curX + wordWidth >= x + width)
	        {
	            curY += lineHeight;
	            curX = x;
	        }

	        g.drawString(word, curX, curY);

	        // Move over to the right for next word.
	        curX += wordWidth;
	    }
	}
	
	public void run() {
		try {
			while(isRunning && !pause) {
				move();
				
				Thread.sleep(30);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			while(pause && !countdown) {
				drawPause();
				
				Thread.sleep(30);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			while(countdown && !isRunning) {
				doCountDown();
				
				Thread.sleep(30);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}