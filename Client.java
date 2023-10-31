import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String ip;
    private Scanner scan = new Scanner(System.in);
    private String encryptionKey;
    private boolean firstMessage = true; // just to give the nickname to the server not crypted

    @Override
    public void run() {
        try{
            System.out.println("Please provide the IP of the computer hosting the server. If you are the host type localhost: ");
            ip = scan.next();
            if (ip.equals("localhost")){
                ip = "127.0.0.1";
            }

            System.out.println("Enter your encryption key: ");
            encryptionKey = scan.next();

            client = new Socket(ip, 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            // Schedule a task to send "PING" every 5 seconds
            ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);
            pingScheduler.scheduleAtFixedRate(this::sendPing, 0, 5, TimeUnit.SECONDS);


            String inMessage;
            while ((inMessage = in.readLine()) != null){
                if (!inMessage.startsWith("/")) inMessage = decryptMessage(inMessage);

                System.out.println(inMessage);
            }
        } catch (IOException e){
            shutDown();
        }
    }
    public void sendPing() {
        if (!done) {
            if(!firstMessage) out.println("PING");
        }
    }

    public String decryptMessage(String message) {

        int indexOfColon = message.indexOf(":");

        if (indexOfColon != -1){

            String str1 = message.substring(0,indexOfColon + 1);
            String str2 = message.substring(indexOfColon + 2);

            if(!str2.startsWith("/")) str2 = StringEncryptor.decrypt(str2, encryptionKey);
            else str2 = str2.substring(1);

            return str1 + " " + str2;
        }else return message;
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
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();

                        if (firstMessage) {
                            out.println(message);
                            firstMessage = false;
                        } else {

                            if (!message.startsWith("/")) message = StringEncryptor.encrypt(message, encryptionKey);

                            if (message.startsWith("/key")) {
                                String[] messageSplit = message.split(" ", 2);
                                encryptionKey = messageSplit[1];
                                System.out.println("key changed");

                            } else if (message.startsWith("/quit")) {
                                out.println(message);
                                inReader.close();
                                shutDown();

                            } else out.println(message);
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

