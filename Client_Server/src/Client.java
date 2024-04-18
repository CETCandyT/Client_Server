import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Client extends Application {
    // IO streams
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private Socket socket;

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        // Panel p to hold the label and text field
        BorderPane paneForTextField = new BorderPane();
        paneForTextField.setPadding(new Insets(5, 5, 5, 5));
        paneForTextField.setStyle("-fx-border-color: green");
        paneForTextField.setLeft(new Label("Enter a number: "));

        // Initialized the TextField for  user input
        TextField tf = new TextField();
        tf.setAlignment(Pos.BOTTOM_RIGHT);
        paneForTextField.setCenter(tf);

        BorderPane mainPane = new BorderPane();
        // Text area to display contents
        TextArea ta = new TextArea();
        mainPane.setCenter(new ScrollPane(ta));
        mainPane.setTop(paneForTextField);

        // Create a scene
        Scene scene = new Scene(mainPane, 450, 200);
        primaryStage.setTitle("Client"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        // Set focus on the text field after the stage is shown
        tf.requestFocus();

        connectToServer();

        // Set up event handling for text field
        tf.setOnAction(e -> {
            try {
                // Get the number from the text field
                int number = Integer.parseInt(tf.getText().trim());

                // Send the number to the server
                if (socket != null && socket.isConnected()) {
                    toServer.writeInt(number);
                    toServer.flush();

                    // Get result from the server
                    String result = fromServer.readUTF();

                    // Display to the text area
                    ta.appendText("The number is " + number + "\n");
                    ta.appendText("The response from the server is: " + result + "\n");

                    // Clear the text field for the next input
                    tf.clear();
                } else {
                    ta.appendText("Error: Not connected to the server.\n");
                    connectToServer(); // Attempt to reconnect
                }
            } catch (IOException ex) {
                ta.appendText("Error communicating with server: " + ex.getMessage() + "\n");
                ex.printStackTrace(); // Print stack trace for debugging
                connectToServer(); // Attempt to reconnect
            } catch (NumberFormatException ex) {
                // Handle non-integer input
                ta.appendText("Input is not a valid number\n");
            }
        });
    }

    private void connectToServer() {
        try {
            // Create a socket to connect to the server
            socket = new Socket("localhost", 8000);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            System.err.println("Failed to connect to server: " + ex.getMessage());
        }
    }

    @Override
    public void stop() {
        // Close the socket and streams when the application stops
        try {
            if (socket != null) {
                socket.close();
            }
            if (toServer != null) {
                toServer.close();
            }
            if (fromServer != null) {
                fromServer.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
