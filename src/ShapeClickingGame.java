import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShapeClickingGame extends JFrame {
    private JPanel gamePanel;
    private JLabel instructionLabel;
    private JLabel scoreLabel;
    private List<Shape> shapes;
    private String targetShape;
    private int score;
    private long startTime;
    private final Random random;
    private final int SHAPES_COUNT = 8;
    private final int TARGET_SCORE = 5;
    
    // Shape types
    private enum ShapeType {
        CIRCLE, SQUARE, TRIANGLE, DIAMOND, STAR
    }
    
    public ShapeClickingGame() {
        random = new Random();
        score = 0;
        startTime = System.currentTimeMillis();
        
        setTitle("Shape Clicking Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initializeUI();
        startNewRound();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Top panel with instructions and score
        JPanel topPanel = new JPanel(new BorderLayout());
        instructionLabel = new JLabel("Click the CIRCLE!", JLabel.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 24));
        instructionLabel.setForeground(Color.BLUE);
        
        scoreLabel = new JLabel("Score: 0/" + TARGET_SCORE, JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        
        topPanel.add(instructionLabel, BorderLayout.CENTER);
        topPanel.add(scoreLabel, BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Game panel
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                for (Shape shape : shapes) {
                    shape.draw(g2d);
                }
            }
        };
        gamePanel.setBackground(Color.WHITE);
        gamePanel.setPreferredSize(new Dimension(900, 500));
        
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        
        add(topPanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
    }
    
    private void startNewRound() {
        shapes = new ArrayList<>();
        
        // Choose random target shape
        ShapeType[] shapeTypes = ShapeType.values();
        ShapeType targetShapeType = shapeTypes[random.nextInt(shapeTypes.length)];
        targetShape = targetShapeType.toString();
        
        // Update instruction
        instructionLabel.setText("Click the " + targetShape + "!");
        
        // Create shapes
        List<ShapeType> shapeList = new ArrayList<>();
        
        // Add one target shape
        shapeList.add(targetShapeType);
        
        // Add other random shapes (not the target shape)
        for (int i = 1; i < SHAPES_COUNT; i++) {
            ShapeType randomType;
            do {
            randomType = shapeTypes[random.nextInt(shapeTypes.length)];
            } while (randomType == targetShapeType);
            shapeList.add(randomType);
        }
        
        // Shuffle the list
        Collections.shuffle(shapeList);
        
        // Create and position shapes
        for (int i = 0; i < SHAPES_COUNT; i++) {
            int x, y;
            boolean validPosition;
            
            // Find a valid position that doesn't overlap with existing shapes
            do {
                int panelWidth = gamePanel.getWidth() > 0 ? gamePanel.getWidth() : gamePanel.getPreferredSize().width;
                int panelHeight = gamePanel.getHeight() > 0 ? gamePanel.getHeight() : gamePanel.getPreferredSize().height;
                x = random.nextInt(panelWidth - 120) + 60;
                y = random.nextInt(panelHeight - 120) + 60;
                validPosition = true;
                
                for (Shape existingShape : shapes) {
                    if (Math.abs(x - existingShape.x) < 100 && Math.abs(y - existingShape.y) < 100) {
                        validPosition = false;
                        break;
                    }
                }
            } while (!validPosition);
            
            Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            shapes.add(new Shape(x, y, shapeList.get(i), color));
        }
        
        gamePanel.repaint();
    }
    
    private void handleClick(int clickX, int clickY) {
        for (Shape shape : shapes) {
            if (shape.contains(clickX, clickY)) {
                if (shape.type.toString().equals(targetShape)) {
                    score++;
                    scoreLabel.setText("Score: " + score + "/" + TARGET_SCORE);
                    
                    if (score >= TARGET_SCORE) {
                        endGame();
                        return;
                    }
                    
                    startNewRound();
                } else {
                    // Wrong shape - visual feedback
                    shape.color = Color.RED;
                    gamePanel.repaint();
                    
                    startNewRound();
                }
                break;
            }
        }
    }
    
    private void endGame() {
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        double seconds = totalTime / 1000.0;
        
        String message = String.format("Congratulations!\nYou completed the game in %.2f seconds!", seconds);
        
        int choice = JOptionPane.showConfirmDialog(
            this, 
            message + "\n\nWould you like to play again?", 
            "Game Complete!", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Reset game
            score = 0;
            startTime = System.currentTimeMillis();
            scoreLabel.setText("Score: 0/" + TARGET_SCORE);
            startNewRound();
        } else {
            System.exit(0);
        }
    }
    
    private class Shape {
        int x, y;
        ShapeType type;
        Color color;
        final int size = 50;
        
        public Shape(int x, int y, ShapeType type, Color color) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.color = color;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            
            switch (type) {
                case CIRCLE -> {
                    g2d.fillOval(x - size/2, y - size/2, size, size);
                    g2d.setColor(Color.BLACK);
                    g2d.drawOval(x - size/2, y - size/2, size, size);
                }
                    
                case SQUARE -> {
                    g2d.fillRect(x - size/2, y - size/2, size, size);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x - size/2, y - size/2, size, size);
                }
                    
                case TRIANGLE -> {
                    int[] xPoints = {x, x - size/2, x + size/2};
                    int[] yPoints = {y - size/2, y + size/2, y + size/2};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(xPoints, yPoints, 3);
                }
                    
                case DIAMOND -> {
                    int[] xDiamond = {x, x - size/2, x, x + size/2};
                    int[] yDiamond = {y - size/2, y, y + size/2, y};
                    g2d.fillPolygon(xDiamond, yDiamond, 4);
                    g2d.setColor(Color.BLACK);
                    g2d.drawPolygon(xDiamond, yDiamond, 4);
                }
                    
                case STAR -> drawStar(g2d, x, y, size/2, size/4, 5);
            }
        }
        
        private void drawStar(Graphics2D g2d, int centerX, int centerY, int outerRadius, int innerRadius, int numPoints) {
            int[] xPoints = new int[numPoints * 2];
            int[] yPoints = new int[numPoints * 2];
            
            for (int i = 0; i < numPoints * 2; i++) {
                double angle = Math.PI * i / numPoints;
                int radius = (i % 2 == 0) ? outerRadius : innerRadius;
                
                xPoints[i] = centerX + (int) (radius * Math.cos(angle - Math.PI / 2));
                yPoints[i] = centerY + (int) (radius * Math.sin(angle - Math.PI / 2));
            }
            
            g2d.fillPolygon(xPoints, yPoints, numPoints * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, numPoints * 2);
        }
        
        public boolean contains(int clickX, int clickY) {
            return Math.abs(clickX - x) <= size/2 && Math.abs(clickY - y) <= size/2;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ShapeClickingGame().setVisible(true);
        });
    }
}
