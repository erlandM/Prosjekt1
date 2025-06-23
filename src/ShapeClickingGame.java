import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShapeClickingGame extends JFrame {
    private JPanel gamePanel;
    private JLabel instructionLabel;
    private JLabel scoreLabel;
    private List<Shape> shapes;
    private String targetShape;
    private int score;
    private int round;
    private long startTime;
    private final Random random;
    private final int SHAPES_COUNT = 8;
    private final int TOTAL_ROUNDS = 5;
    
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
        
        scoreLabel = new JLabel("Score: 0/" + TOTAL_ROUNDS, JLabel.CENTER);
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
                    round++;
                    scoreLabel.setText("Score: " + score + "/" + TOTAL_ROUNDS);
                    
                    if (round >= TOTAL_ROUNDS) {
                        endGame();
                        return;
                    }
                    
                    startNewRound();
                } else {
                    // Wrong shape clicked
                    round++;
                    if (round >= TOTAL_ROUNDS) {
                        endGame();
                        return;
                    }
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
            round = 0;
            startTime = System.currentTimeMillis();
            scoreLabel.setText("Score: 0/" + TOTAL_ROUNDS);
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
        java.awt.Shape awtShape;

        public Shape(int x, int y, ShapeType type, Color color) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.color = color;
            this.awtShape = createAwtShape();
        }

        private java.awt.Shape createAwtShape() {
            switch (type) {
                case CIRCLE:
                    return new Ellipse2D.Double(x - size / 2, y - size / 2, size, size);
                case SQUARE:
                    return new Rectangle2D.Double(x - size / 2, y - size / 2, size, size);
                case TRIANGLE: {
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x, y - size / 2);
                    triangle.addPoint(x - size / 2, y + size / 2);
                    triangle.addPoint(x + size / 2, y + size / 2);
                    return triangle;
                }
                case DIAMOND: {
                    Polygon diamond = new Polygon();
                    diamond.addPoint(x, y - size / 2);
                    diamond.addPoint(x - size / 2, y);
                    diamond.addPoint(x, y + size / 2);
                    diamond.addPoint(x + size / 2, y);
                    return diamond;
                }
                case STAR:
                    return createStarShape(x, y, size / 2, size / 4, 5);
                default:
                    return null;
            }
        }

        private java.awt.Shape createStarShape(int centerX, int centerY, int outerRadius, int innerRadius, int numPoints) {
            Path2D star = new Path2D.Double();
            double angleStep = Math.PI / numPoints;
            for (int i = 0; i < numPoints * 2; i++) {
                double angle = i * angleStep - Math.PI / 2;
                int radius = (i % 2 == 0) ? outerRadius : innerRadius;
                double xPt = centerX + Math.cos(angle) * radius;
                double yPt = centerY + Math.sin(angle) * radius;
                if (i == 0) {
                    star.moveTo(xPt, yPt);
                } else {
                    star.lineTo(xPt, yPt);
                }
            }
            star.closePath();
            return star;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fill(awtShape);
            g2d.setColor(Color.BLACK);
            g2d.draw(awtShape);
        }

        public boolean contains(int clickX, int clickY) {
            return awtShape != null && awtShape.contains(clickX, clickY);
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
