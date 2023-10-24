import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDown();
        }
    }


    public void brodcast(String message){
        for(ConnectionHandler ch : connections){
            if (ch != null){
                ch.SendMassage(message);
            }
        }
    }

    public void shutDown(){
        try{
            done = true;
            pool.shutdown();
            if (!server.isClosed()){
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.shutDown();
            }
        } catch (IOException e){
            // ignore
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickame;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {

            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter Nickname: ");
                nickame = in.readLine();
                System.out.println(nickame + " is connected with the address: " + client.getRemoteSocketAddress().toString());
                brodcast(nickame + " joined the chat!");
                String message;
                while((message = in.readLine()) != null){
                    if (message.startsWith("/nick")){
                        // Nickname change
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2){
                            brodcast(nickame + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickame + " renamed themselves to " + messageSplit[1]);
                            nickame = messageSplit[1];
                            out.println("Successfully changed nickname to " + nickame);
                        } else {
                            out.println("No nickname provided");
                        }
                    } else if(message.startsWith("/quit")){
                        System.out.println(nickame + "has disconnected");
                        brodcast(nickame + " left the chat!");
                        shutDown();
                    } else {
                        brodcast(nickame + ": " + message);
                    }
                }

            } catch (IOException e) {
                shutDown();
            }
        }

        public void SendMassage(String message){
            out.println(message);
        }

        public void shutDown(){
            try{
                in.close();
                out.close();
                if (!client.isClosed()){
                    client.close();
                }
            } catch(IOException e){
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}