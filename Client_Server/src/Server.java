import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Server extends Application {
    // Create a logger for the Server class
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    @Override
    public void start(Stage primaryStage) {
        // Text area for displaying contents
        TextArea ta = new TextArea();

        // Create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(ta), 450, 200);
        primaryStage.setTitle("Server"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        // Create a thread for handling client requests
        new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                Platform.runLater(() ->
                        ta.appendText("Server started at " + new Date() + '\n'));

                while (true) {
                    // Listen for a connection request
                    Socket socket = serverSocket.accept();
                    Platform.runLater(() ->
                            ta.appendText("Connected to client: " + socket.getInetAddress() + " at " + new Date() + '\n'));

                    // Start a new thread to handle the client request
                    new Thread(new ClientHandler(socket, ta)).start();
                }
            } catch (IOException ex) {
                logger.severe("Error in server: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private final TextArea ta;

    // Create a logger for the ClientHandler class
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket, TextArea ta) {
        this.socket = socket;
        this.ta = ta;
    }

    @Override
    public void run() {
        try {
            // Create data input and output streams
            DataInputStream inputFromClient = new DataInputStream(
                    socket.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(
                    socket.getOutputStream());

            // Receive number from the user/client class
            int number = inputFromClient.readInt();
            Platform.runLater(() ->
                    ta.appendText("Received number from client: " + number + '\n'));

            // Check if the number is prime
            boolean isPrime = isPrime(number);
            Platform.runLater(() ->
                    ta.appendText("Checking if number is prime...\n"));

            // Send the result back to the client
            if (isPrime) {
                outputToClient.writeUTF("The number is a prime number.");
            } else {
                outputToClient.writeUTF("The number is NOT a prime number.");
            }

            Platform.runLater(() -> {
                ta.appendText("Response sent to client.\n");
                ta.appendText("Client connection handled.\n");
            });

        } catch (IOException ex) {
            logger.severe("Error in client handler: " + ex.getMessage());
        } finally {
            try {
                // Close the socket after handling the request
                socket.close();
            } catch (IOException ex) {
                logger.severe("Error while closing socket: " + ex.getMessage());
            }
        }
    }

    /**
     * Check if a number is prime number.
     *
     * @param num The number to check
     * @return true if the number is prime, false otherwise
     */
    private boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
