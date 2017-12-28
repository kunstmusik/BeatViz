/*
 * Copyright (C) 2017 Steven Yi<stevenyi@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package beatviz;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author stevenyi
 */
public class BeatViz extends Application {

    GridPane grid;

    @Override
    public void start(Stage primaryStage) {
        grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rect = new Rectangle();
                rect.setArcHeight(5);
                rect.setArcWidth(5);
                rect.setWidth(20);
                rect.setHeight(20);

                rect.setStroke(Color.LIME);
                rect.setStrokeWidth(2.0);
                rect.setEffect(new Glow(0.8));

                grid.add(rect, j, i);
            }
        }

        updateGrid(0);

        // SETUP SCENE
        Scene scene = new Scene(grid, 125, 125, Color.BLACK);

        scene.setOnMouseClicked(evt -> {
            if(evt.getClickCount() > 2) {
                Platform.exit();
            }
        });


        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("BeatViz");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMaxX() - scene.getWidth() - 10);
        primaryStage.setY(screenBounds.getMaxY() - scene.getHeight() - 10);

        scene.setOnKeyPressed(evt -> {
            if(evt.getCode().equals(KeyCode.LEFT)) {
                primaryStage.setX(primaryStage.getX() - 10);
            } else if(evt.getCode().equals(KeyCode.RIGHT)) {
                primaryStage.setX(primaryStage.getX() + 10);
            } else if(evt.getCode().equals(KeyCode.UP)) {
                primaryStage.setY(primaryStage.getY() - 10);
            } else if(evt.getCode().equals(KeyCode.DOWN)) {
                primaryStage.setY(primaryStage.getY() + 10);
            }
        });

        primaryStage.show();

        // SETUP UDP THREAD
        Thread t = new Thread(() -> {
            try {
                DatagramSocket serverSocket = new DatagramSocket(9228);
                byte[] receiveData = new byte[1024];

                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String strVal = new String(receivePacket.getData());

                    try {
                        long beat = Long.parseLong(strVal.trim());
                        Platform.runLater(() -> updateGrid(beat));
                    } catch (Exception ex) {
                        System.out.println("Invalid Beat: " + strVal);
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error: Could not listen on UDP port 9228");
                System.exit(1);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void updateGrid(long beat) {
        ObservableList<Node> children = grid.getChildren();

        for (int i = 0; i < 16; i++) {
            int row = i / 4;
            int col = i % 4;
            Rectangle n = (Rectangle) children.get(i);
            n.setFill(
                    ((beat / (long)Math.pow(4, row)) % 4 == col)
                    ? Color.GREEN : Color.BLACK
            );

//            System.out.printf("%d %d %d %d\n", i, row, col, 
//                    ((beat / (long)Math.pow(4, row)) % 4));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }

}
