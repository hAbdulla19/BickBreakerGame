package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdvancedBrickBreakerGame extends JPanel implements ActionListener, ComponentListener {
    private int ballX = 120;
    private int ballY = 350;
    private final int ballDiameter = 20;
    private int ballSpeedX = 2;
    private int ballSpeedY = -2;
    private int paddleX = 100;
    private int paddleWidth = 80;
    private final int paddleHeight = 10;
    private boolean[] bricks;
    private final int numBricks = 60;
    private int brickWidth = 50;
    private final int brickHeight = 20;
    private int score = 0;
    private int level = 1;
    private final Timer timer;
    private boolean gameInProgress = true;

    public AdvancedBrickBreakerGame() {
        bricks = new boolean[numBricks];
        for (int i = 0; i < numBricks; i++) {
            bricks[i] = true;
        }

        timer = new Timer(10, this);
        timer.start();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_LEFT && paddleX > 0) {
                    paddleX -= 10;
                } else if (key == KeyEvent.VK_RIGHT && paddleX < getWidth() - paddleWidth) {
                    paddleX += 10;
                }
                repaint();
            }
        });

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addComponentListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameInProgress) {
            return;
        }

        // Ball movement logic
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Check for collision with the paddle
        if (new Rectangle(ballX, ballY, ballDiameter, ballDiameter).intersects(new Rectangle(paddleX, getHeight() - paddleHeight - 30, paddleWidth, paddleHeight))) {
            ballSpeedY = -ballSpeedY;
        }

        // Check for collision with the window edges
        if (ballX <= 0 || ballX >= getWidth() - ballDiameter) {
            ballSpeedX = -ballSpeedX;
        }
        // If the ball hits the top of the screen, it should bounce back
        if (ballY <= 0) {
            ballSpeedY = -ballSpeedY;
        }

        // Collision detection with bricks
        for (int i = 0; i < numBricks; i++) {
            if (bricks[i]) {
                int brickX = (i % 10) * brickWidth;
                int brickY = (i / 10) * brickHeight + 50; // Offset for top margin
                if (new Rectangle(ballX, ballY, ballDiameter, ballDiameter).intersects(new Rectangle(brickX, brickY, brickWidth, brickHeight))) {
                    bricks[i] = false;
                    ballSpeedY = -ballSpeedY;
                    score += 10;
                    break; // Important to prevent multiple collisions in one move
                }
            }
        }

        // Check for level completion
        boolean levelComplete = true;
        for (boolean brick : bricks) {
            if (brick) {
                levelComplete = false;
                break;
            }
        }

        if (levelComplete) {
            level++;
            if (level > 3) {
                gameInProgress = false;
                timer.stop();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Congratulations! You've won the game!", "Game Over", JOptionPane.INFORMATION_MESSAGE));
            } else {
                resetLevel();
            }
        }

        // Game over if the ball goes out of bounds
        if (ballY > getHeight()) {
            gameInProgress = false;
            timer.stop();
            SwingUtilities.invokeLater(() -> {
                int choice = JOptionPane.showOptionDialog(this, "Game Over! Your score: " + score + "\nWould you like to restart?", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (choice == JOptionPane.YES_OPTION) {
                    restartGame();
                }
            });
        }

        repaint();
    }

    private void resetLevel() {
        initializeBricks();
        ballX = getWidth() / 2 - ballDiameter / 2;
        ballY = getHeight() / 2 - ballDiameter / 2;
        ballSpeedX = 2;
        ballSpeedY = -2;
        paddleX = getWidth() / 2 - paddleWidth / 2;
    }

    private void initializeBricks() {
        bricks = new boolean[numBricks];
        for (int i = 0; i < numBricks; i++) {
            bricks[i] = true;
        }
    }

    private void restartGame() {
        resetLevel();
        score = 0;
        level = 1;
        gameInProgress = true;
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBall(g);
        drawPaddle(g);
        drawBricks(g);
        drawScore(g);
        drawLevel(g);
    }

    private void drawBall(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    private void drawPaddle(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(paddleX, getHeight() - 30 - paddleHeight, paddleWidth, paddleHeight);
    }

    private void drawBricks(Graphics g) {
        for (int i = 0; i < numBricks; i++) {
            if (bricks[i]) {
                g.setColor(new Color(0, 85, 0, 255)); // Darker green for better visibility
                int x = (i % 10) * brickWidth;
                int y = (i / 10) * brickHeight + 50; // Offset for top margin
                g.fillRect(x, y, brickWidth, brickHeight);
            }
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);
    }

    private void drawLevel(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Level: " + level, getWidth() - 100, 30);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        paddleWidth = getWidth() / 10;
        brickWidth = getWidth() / 10;
        paddleX = getWidth() / 2 - paddleWidth / 2;
        ballX = getWidth() / 2 - ballDiameter / 2;
        ballY = getHeight() / 2 - ballDiameter / 2;

        // Recalculate brick layout
        for (int i = 0; i < numBricks; i++) {
            int row = i / 10;
            int col = i % 10;
            int brickX = col * brickWidth;
            int brickY = row * brickHeight + 50; // Offset for top margin

            if (brickX + brickWidth > getWidth()) {
                // If the brick would go out of the screen, initialize bricks again with new dimensions
                initializeBricks();
                break;
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Advanced Brick Breaker Game");
            AdvancedBrickBreakerGame game = new AdvancedBrickBreakerGame();
            frame.setContentPane(game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 500);
            frame.setVisible(true);
            frame.setResizable(true);
        });
    }
}