package org.example;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Line Rasterization");

        CanvasRasterBufferedImage canvas = new CanvasRasterBufferedImage(800, 600);

        JTextArea infoPanel = new JTextArea();
        infoPanel.setEditable(false);
        infoPanel.setText(
                "Ovládání:\n" +
                        "1. Levé tlačítko myši: Kreslení čar (klikni a táhni).\n" +
                        "2. Klávesa C: Vymazání plátna.\n" +
                        "3. Klávesa B: Přepnutí mezi tenkou a tlustou čárou.\n" +
                        "4. Klávesa V: Volná čára při tažení myši.\n" +
                        "5. Dynamické čáry: Červená (tenká), modrá (tlustá) při tažení myši.\n"+
                        "6. Polygon X: Kliknutím vytvořte 2 body. klávesou enter potvrdíte polygon.\n"
        );

        infoPanel.setBackground(Color.LIGHT_GRAY);
        infoPanel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.setLayout(new BorderLayout());
        frame.add(infoPanel, BorderLayout.SOUTH);
        frame.add(canvas, BorderLayout.CENTER);

        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        canvas.requestFocusInWindow();
    }
}
