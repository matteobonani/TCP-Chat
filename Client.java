import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String ip;
    private Scanner scan = new Scanner(System.in);

    @Override
    public void run() {
        try{
            System.out.println("PLease provide the IP of the computer hosting the server. If you are the host type localhost: ");
            ip = scan.next();
            if (ip.equals("localhost")){
                ip = "127.0.0.1";
            }
            client = new Socket(ip, 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        } catch (IOException e){
            shutDown();
        }
    }

    public void shutDown(){
        done = true;
        try{
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e){
            //ignore
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();
                    if (message.equals("/quit")){
                        out.println(message);
                        inReader.close();
                        shutDown();
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e){
                shutDown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}