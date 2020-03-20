import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main extends Application
{


    private static List<ClientHandler> clientsList = new ArrayList<>();        // List to store active clients

    private int clientNumber = 0;



    // Text area for displaying contents
    private TextArea textArea = new TextArea();









    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // create a scene and place it in the stage
        Scene scene = new Scene(new ScrollPane(textArea), 450, 200);

        primaryStage.setTitle("Chat Application Server");
        primaryStage.setScene(scene);
        primaryStage.show();



        new Thread( () ->
        {

            try
            {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(8000);
                textArea.appendText("Chat Application Server start at: " + new Date() + '\n');


                while (true)
                {
                    // listen for a new connection request
                    Socket socket = serverSocket.accept();

                    // increment client number
                    clientNumber++;


                    Platform.runLater( () ->
                    {
                        // display the client number
                        textArea.appendText("Starting thread for client " + clientNumber + " at " + new Date() + '\n');


                        // find the client's host name and IP address
                        InetAddress inetAddress = socket.getInetAddress();

                        textArea.appendText("Client " + clientNumber + "'s host name: " + inetAddress.getHostName() + '\n');
                        textArea.appendText("Client " + clientNumber + "'s IP address: " + inetAddress.getHostAddress() + '\n');
                    });





                    DataInputStream inputToClient = new DataInputStream(socket.getInputStream());
                    DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

                    ClientHandler clientHandler = new ClientHandler(socket, "Client " + clientNumber, inputToClient, outputToClient);

                    clientsList.add(clientHandler);


                    new Thread(clientHandler).start();



                }

            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }

        }).start();
    }














    class ClientHandler implements Runnable
    {

        private DataInputStream inputFromClient;
        private DataOutputStream outputToClient;
        private Socket socket;
        private String name;
        boolean isLoggedIn;







        public ClientHandler(Socket socket, String name, DataInputStream inputFromClient, DataOutputStream outputToClient)
        {
            this.socket = socket;
            this.name = name;
            this.inputFromClient = inputFromClient;
            this.outputToClient = outputToClient;
            this.isLoggedIn = true;
        }





        @Override
        public void run()
        {
            String messageReceived;


            try
            {


                // continuously serve the client
                while (true)
                {



                    // receive message from the client
                    messageReceived = inputFromClient.readUTF();

                    System.out.println(this.name + ": " + messageReceived);


                    String sender = this.name;

                    if (messageReceived.equals("logout"))
                    {
                        this.isLoggedIn = false;
                        this.socket.close();
                        break;
                    }




                    // send message to chatRoom
                    for (ClientHandler clientHandler: Main.clientsList)
                    {
                        // if the recipient is found, write on its output stream


                        if (clientHandler.isLoggedIn)
                        {
                            clientHandler.outputToClient.writeUTF(sender + ": " + messageReceived);

                        }
                    }


                    String finalMessageReceived = messageReceived;
                    Platform.runLater( () ->
                    {
                        textArea.appendText(this.name + ": " + finalMessageReceived + '\n');
                    });
                }





            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }









//            try
//            {
//                this.outputToClient.close();
//                this.inputFromClient.close();
//            }
//            catch (IOException ex)
//            {
//                ex.printStackTrace();
//            }



        }


    }


    public static void main(String[] args) {
        launch(args);
    }
}







