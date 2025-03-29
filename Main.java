import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

import java.awt.*;
import java.util.List;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {

    public static void main(String[] args) {
        int startHexagon = 0;
        int endHexagon = 0;

        int start = 1;
        int maxHexagons = 331;

        // Identify ringEndings
        Set<Integer> ringEndings = getRingEndings(start, maxHexagons);
        int currentRing = 0;
        int currentRotationIndex = 1;
        int rotateIndexCount = 0;
        System.out.println(ringEndings);

        // Populate neighbors
        Map<Integer, Hexagon> indexHexagonMap = new HashMap<>();
        Coordinates nextCenter = new Coordinates(BigDecimal.ZERO, BigDecimal.ZERO);
        Map<Coordinates, Hexagon> coordinatesHexagonMap = new HashMap<>();
        for (int hexagonCount = start; hexagonCount <= maxHexagons; hexagonCount++) {
            Hexagon hexagon = new Hexagon();
            hexagon.setIndex(hexagonCount);
            hexagon.setCenter(nextCenter);

            // add 6 neighbors
            hexagon.setNeighbors(new Coordinates[6]);
            // neighbor 0 coordinates = current center x, current center y + 1
            hexagon.addNeighbor(new Coordinates(nextCenter.getX(), nextCenter.getY().add(BigDecimal.ONE)));
            // neighbor 1 coordinates = current center x + (square root of 3)/2, current center y + 0.5
            hexagon.addNeighbor(new Coordinates(nextCenter.getX().add(new BigDecimal(Math.sqrt(3) / 2)), nextCenter.getY().add(new BigDecimal(0.5))));
            // neighbor 2 coordinates = current center x + (square root of 3)/2, current center y - 0.5
            hexagon.addNeighbor(new Coordinates(nextCenter.getX().add(new BigDecimal(Math.sqrt(3) / 2)), nextCenter.getY().subtract(new BigDecimal(0.5))));
            // neighbor 3 coordinates = current center x, current center y - 1
            hexagon.addNeighbor(new Coordinates(nextCenter.getX(), nextCenter.getY().subtract(BigDecimal.ONE)));
            // neighbor 4 coordinates = current center x - (square root of 3)/2, current center y - 0.5
            hexagon.addNeighbor(new Coordinates(nextCenter.getX().subtract(new BigDecimal(Math.sqrt(3) / 2)), nextCenter.getY().subtract(new BigDecimal(0.5))));
            // neighbor 5 coordinates = current center x - (square root of 3)/2, current center y + 0.5
            hexagon.addNeighbor(new Coordinates(nextCenter.getX().subtract(new BigDecimal(Math.sqrt(3) / 2)), nextCenter.getY().add(new BigDecimal(0.5))));
            indexHexagonMap.put(hexagon.getIndex(), hexagon);
            coordinatesHexagonMap.put(hexagon.getCenter(), hexagon);
            if (ringEndings.contains(hexagonCount)) {
                nextCenter = hexagon.getNeighbors()[0];
                hexagon.setRotationIndex(0);
                hexagon.setRing(currentRing);
                currentRing++;
            } else {
                nextCenter = hexagon.getNeighbors()[currentRotationIndex];
                hexagon.setRotationIndex(currentRotationIndex);
                hexagon.setRing(currentRing);
            }
            rotateIndexCount++;
            if (rotateIndexCount == currentRing) {
                currentRotationIndex++;
                if (currentRotationIndex > 5) {
                    currentRotationIndex = 0;
                }
                rotateIndexCount = 0; // Reset rotate index count
            }
        }

        indexHexagonMap.forEach((id, hexagon) -> {
            List<Integer> neighbors = new ArrayList<>();
            for (Coordinates neighbor : hexagon.getNeighbors()) {
                if (coordinatesHexagonMap.containsKey(neighbor)) {
                    neighbors.add(coordinatesHexagonMap.get(neighbor).getIndex());
                }
            }
            System.out.println(id + ": " + neighbors);
        });

        List<Hexagon> path = getShortestPath(indexHexagonMap, coordinatesHexagonMap, startHexagon, endHexagon);
        for (Hexagon hexagon : path) {
            System.out.println(hexagon.getIndex());
        }

        // Create and display the Cartesian plot
        SwingUtilities.invokeLater(() -> createAndShowCartesianPlot(indexHexagonMap, maxHexagons, startHexagon, endHexagon, path, coordinatesHexagonMap));
    }

    private static Set<Integer> getRingEndings(int start, int maxHexagons) {
        Set<Integer> ringEndings = new HashSet<>();
        int ringSize = 0;
        int rings = 0;
        int hexCount = 0;
        while (hexCount < maxHexagons) {
            ringSize = (6 * rings);
            hexCount += ringSize;
            if (hexCount > maxHexagons) {
                break;
            }
            ringEndings.add(hexCount+start);
            rings++;
        }
        return ringEndings;
    }

    private static List<Hexagon> getShortestPath(Map<Integer, Hexagon> hexagonMap, Map<Coordinates, Hexagon> coordinatesHexagonMap, int startHexagon, int endHexagon) {
        if (startHexagon == 0 && endHexagon == 0) {
            return new ArrayList<>();
        }
        List<Hexagon> path = new ArrayList<>();
        Hexagon startingHexagon = hexagonMap.get(startHexagon);
        Hexagon endingHexagon = hexagonMap.get(endHexagon);
        BigDecimal totalDistance = BigDecimal.ZERO;
        BigDecimal linearDistance = BigDecimal.ZERO;
        path.add(startingHexagon);
        
        BigDecimal upperBound = getDistance(startingHexagon.getCenter(), endingHexagon.getCenter());
        linearDistance = upperBound;
        Hexagon nearestHexagon = startingHexagon;
        int pathLength = 1;
        while (endingHexagon != nearestHexagon) {
            for (Coordinates neighbor : nearestHexagon.getNeighbors()) {
                if (coordinatesHexagonMap.containsKey(neighbor)) {
                    Hexagon neighborHexagon = coordinatesHexagonMap.get(neighbor);
                    if (getDistance(neighborHexagon.getCenter(), endingHexagon.getCenter()).compareTo(upperBound) < 0) {
                        upperBound = getDistance(neighborHexagon.getCenter(), endingHexagon.getCenter());
                        nearestHexagon = neighborHexagon;
                    }
                }
            }
            path.add(nearestHexagon);
            totalDistance = totalDistance.add(getDistance(nearestHexagon.getCenter(), path.get(pathLength - 1).getCenter()));
            pathLength++;
        }
        System.out.println("Total distance: " + totalDistance);
        System.out.println("Linear distance: " + linearDistance);
        return path;
    }

    private static BigDecimal getDistance(Coordinates c1, Coordinates c2) {
        BigDecimal x1 = c1.getX();
        BigDecimal y1 = c1.getY();
        BigDecimal x2 = c2.getX();
        BigDecimal y2 = c2.getY();
        BigDecimal deltaX = x2.subtract(x1);
        BigDecimal deltaY = y2.subtract(y1);
        BigDecimal distance = BigDecimal.valueOf(Math.sqrt(deltaX.pow(2).add(deltaY.pow(2)).doubleValue()));
        return distance.setScale(4, RoundingMode.HALF_UP);
    }
        
    

    /**
     * Creates and displays a Cartesian plot of hexagon centers
     */
    private static void createAndShowCartesianPlot(Map<Integer, Hexagon> hexagonMap, int maxHexagons, int startHexagon, int endHexagon, List<Hexagon> path, Map<Coordinates, Hexagon> coordinatesHexagonMap) {
        JFrame frame = new JFrame("Hexagon Centers - Cartesian Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        
        // Animation parameters
        final int[] currentPathIndex = {0};
        final Timer[] animationTimer = {null};
        
        // Create input panel for start and end hexagons
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel startLabel = new JLabel("Start Hexagon:");
        JTextField startField = new JTextField(String.valueOf(startHexagon), 5);
        JLabel endLabel = new JLabel("End Hexagon:");
        JTextField endField = new JTextField(String.valueOf(endHexagon), 5);
        JButton updateButton = new JButton("Update Path");
        JLabel clickInstructionLabel = new JLabel("Or click hexagons: Left-click for start, Right-click for end");
        
        inputPanel.add(startLabel);
        inputPanel.add(startField);
        inputPanel.add(endLabel);
        inputPanel.add(endField);
        inputPanel.add(updateButton);
        inputPanel.add(clickInstructionLabel);
        
        // Create a reference to the current path that can be updated
        final List<Hexagon>[] currentPath = new List[]{path};
        final int[] currentStartHex = {startHexagon};
        final int[] currentEndHex = {endHexagon};

        // Store hexagon shapes for click detection
        final Map<Polygon, Integer> hexagonShapes = new HashMap<>();
        
        // Create the visualization panel
        JPanel visualPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int scale = 50; // Scale factor to make coordinates visible

                // Clear the hexagon shapes map before repopulating
                hexagonShapes.clear();
                
                // Draw coordinate axes
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.draw(new Line2D.Double(0, centerY, getWidth(), centerY)); // X-axis
                g2d.draw(new Line2D.Double(centerX, 0, centerX, getHeight())); // Y-axis
                
                // Draw axis labels
                g2d.drawString("X", getWidth() - 20, centerY - 5);
                g2d.drawString("Y", centerX + 5, 20);
                
                // Draw grid lines
                g2d.setColor(new Color(220, 220, 220));
                for (int i = -10; i <= 10; i++) {
                    if (i == 0) continue; // Skip the axes
                    // Vertical grid lines
                    g2d.draw(new Line2D.Double(centerX + i * scale, 0, centerX + i * scale, getHeight()));
                    // Horizontal grid lines
                    g2d.draw(new Line2D.Double(0, centerY + i * scale, getWidth(), centerY + i * scale));

                    // Grid labels
                    g2d.setColor(Color.GRAY);
                    g2d.drawString(String.valueOf(i), centerX + i * scale, centerY + 15);
                    g2d.drawString(String.valueOf(-i), centerX - 10, centerY + i * scale);
                    g2d.setColor(new Color(220, 220, 220));
                }
                
                // Draw hexagons
                for (int i = 0; i <= maxHexagons; i++) {
                    Hexagon hexagon = hexagonMap.get(i);
                    if (hexagon != null) {
                        Double x = hexagon.getCenter().getX().doubleValue();
                        Double y = hexagon.getCenter().getY().doubleValue();

                        // Convert to screen coordinates (flip Y-axis)
                        int screenX = centerX + (int)(x * scale);
                        int screenY = centerY - (int)(y * scale); // Y is flipped in screen coordinates
                        
                        // Draw hexagon
                        Polygon hexShape = createHexagonShape(screenX, screenY, scale / 2);

                        // Store hexagon shape for click detection
                        hexagonShapes.put(hexShape, i);
                        
                        // Set color based on whether it's start, end, or path hexagon
                        if (i == currentStartHex[0]) {
                            g2d.setColor(Color.GREEN);
                        } else if (i == currentEndHex[0]) {
                            g2d.setColor(Color.RED);
                        } else if (currentPath[0].contains(hexagon)) {
                            // Check if this hexagon is part of the animated path
                            int pathIndex = currentPath[0].indexOf(hexagon);
                            if (pathIndex <= currentPathIndex[0] && pathIndex > 0) {
                                // This hexagon is part of the currently animated path
                                g2d.setColor(Color.GRAY);
                            } else {
                                // This hexagon is part of the path but not yet animated
                                g2d.setColor(Color.BLACK);
                            }
                        } else {
                            g2d.setColor(Color.BLACK);
                        }
                        
                        g2d.fill(hexShape);
                        g2d.setColor(Color.WHITE);
                        g2d.draw(hexShape);
                        
                        // Draw center point
                        g2d.setColor(Color.WHITE);
                        g2d.fill(new Ellipse2D.Double(screenX - 2, screenY - 2, 4, 4));
                        
                        // Draw index number in white
                        g2d.setColor(Color.WHITE);
                        g2d.drawString(String.valueOf(i), screenX - 5, screenY + 4);
                    }
                }
            }

            /**
             * Creates a hexagon shape centered at the given coordinates
             */
            private Polygon createHexagonShape(int centerX, int centerY, int size) {
                Polygon hexagon = new Polygon();

                // Create hexagon with flat sides at top and bottom (rotated 0 degrees)
                for (int i = 0; i < 6; i++) {
                    double angle = 2 * Math.PI / 6 * i;
                    int x = centerX + (int)(size * Math.cos(angle));
                    int y = centerY + (int)(size * Math.sin(angle));
                    hexagon.addPoint(x, y);
                }

                return hexagon;
            }
        };

        // Add mouse listener for hexagon clicks
        visualPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickPoint = e.getPoint();
                
                // Check if click is inside any hexagon
                for (Map.Entry<Polygon, Integer> entry : hexagonShapes.entrySet()) {
                    if (entry.getKey().contains(clickPoint)) {
                        int hexIndex = entry.getValue();
                        
                        // Left click sets start hexagon
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            startField.setText(String.valueOf(hexIndex));
                            currentStartHex[0] = hexIndex;
                        } 
                        // Right click sets end hexagon
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            endField.setText(String.valueOf(hexIndex));
                            currentEndHex[0] = hexIndex;
                        }
                        
                        // Calculate new path if both start and end are set
                        if (currentStartHex[0] > 0 && currentEndHex[0] > 0) {
                            // Calculate new path
                            currentPath[0] = getShortestPath(hexagonMap, coordinatesHexagonMap, 
                                                           currentStartHex[0], currentEndHex[0]);
                            
                            // Reset animation
                            currentPathIndex[0] = 0;
                            
                            // Stop existing timer if running
                            if (animationTimer[0] != null && animationTimer[0].isRunning()) {
                                animationTimer[0].stop();
                            }
                            
                            // Start new animation
                            animationTimer[0] = new Timer(300, event -> {
                                if (currentPathIndex[0] < currentPath[0].size() - 1) {
                                    currentPathIndex[0]++;
                                    visualPanel.repaint();
                                } else {
                                    // Animation complete, stop the timer
                                    ((Timer)event.getSource()).stop();
                                }
                            });
                            animationTimer[0].start();
                            
                            // Repaint panel
                            visualPanel.repaint();
                        }
                        
                        break;
                    }
                }
            }
        });
        
        // Add action listener to update button
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int newStart = Integer.parseInt(startField.getText().trim());
                    int newEnd = Integer.parseInt(endField.getText().trim());
                    
                    // Validate input
                    if (newStart <= 1 || newStart > maxHexagons || newEnd <= 1 || newEnd > maxHexagons) {
                        JOptionPane.showMessageDialog(frame, 
                            "Please enter valid hexagon indices between 1 and " + maxHexagons, 
                            "Invalid Input", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Update current values
                    currentStartHex[0] = newStart;
                    currentEndHex[0] = newEnd;
                    
                    // Calculate new path
                    Map<Coordinates, Hexagon> coordinatesHexagonMap = new HashMap<>();
                    for (int i = 1; i <= maxHexagons; i++) {
                        Hexagon hex = hexagonMap.get(i);
                        if (hex != null) {
                            coordinatesHexagonMap.put(hex.getCenter(), hex);
                        }
                    }
                    
                    // Get new path
                    currentPath[0] = getShortestPath(hexagonMap, coordinatesHexagonMap, newStart, newEnd);
                    
                    // Reset animation
                    currentPathIndex[0] = 0;
                    
                    // Stop existing timer if running
                    if (animationTimer[0] != null && animationTimer[0].isRunning()) {
                        animationTimer[0].stop();
                    }
                    
                    // Start new animation
                    animationTimer[0] = new Timer(300, event -> {
                        if (currentPathIndex[0] < currentPath[0].size() - 1) {
                            currentPathIndex[0]++;
                            visualPanel.repaint();
                        } else {
                            // Animation complete, stop the timer
                            ((Timer)event.getSource()).stop();
                        }
                    });
                    animationTimer[0].start();
                    
                    // Repaint panel
                    visualPanel.repaint();
                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Please enter valid integer values", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Set up the frame layout
        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(visualPanel, BorderLayout.CENTER);
        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Start the animation timer
        animationTimer[0] = new Timer(300, e -> {
            if (currentPathIndex[0] < currentPath[0].size() - 1) {
                currentPathIndex[0]++;
                visualPanel.repaint();
            } else {
                // Animation complete, stop the timer
                ((Timer)e.getSource()).stop();
            }
        });
        animationTimer[0].start();
    }
}
