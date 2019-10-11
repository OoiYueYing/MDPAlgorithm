package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Communication manager to communicate with the different parts of the system via the RasPi.
 *
 * @author SuyashLakhotia
 */

public class CommMgr {

    public static final String EX_START = "EX_START";       // Android --> PC
    public static final String FP_START = "FP_START";       // Android --> PC
    public static final String WAY_POINT = "WAY_POINT";     // Android --> PC
    public static final String MAP_STRINGS = "MAP";         // PC --> Android
    public static final String BOT_POS = "BOT_POS";         // PC --> Android
    public static final String GET_SENSOR = "r";             // PC --> Arduino
    public static final String INSTRUCTIONS = "INSTR";      // PC --> Arduino
    public static final String SENSOR_DATA = "SDATA";       // Arduino --> PC

    private static CommMgr commMgr = null;
    private static Socket conn = null;

    private BufferedWriter writer;
    private BufferedReader reader;

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final Charset ISO = Charset.forName("ISO-8859-1");

    private CommMgr() {
    }

    public static CommMgr getCommMgr() {
        if (commMgr == null) {
            commMgr = new CommMgr();
        }
        return commMgr;
    }

    public void openConnection() {
        System.out.println("Opening connection...");

        try {
            String HOST = "192.168.26.1";
            int PORT = 2626;
            conn = new Socket(HOST, PORT);

            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("openConnection() --> " + "Connection established successfully!");

            return;
        } catch (UnknownHostException e) {
            System.out.println("openConnection() --> UnknownHostException");
        } catch (IOException e) {
            System.out.println("openConnection() --> IOException");
        } catch (Exception e) {
            System.out.println("openConnection() --> Exception");
            System.out.println(e.toString());
        }

        System.out.println("Failed to establish connection!");
    }

    public void closeConnection() {
        System.out.println("Closing connection...");

        try {
            reader.close();

            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("closeConnection() --> IOException");
        } catch (NullPointerException e) {
            System.out.println("closeConnection() --> NullPointerException");
        } catch (Exception e) {
            System.out.println("closeConnection() --> Exception");
            System.out.println(e.toString());
        }
    }

    public void sendMsg(String msg, String msgType) {
        System.out.println("Sending a message...");

        try {
//            String outputMsg;
//            if (msg == null) {
//                outputMsg = msgType + "\n";
//            } else if (msgType.equals(MAP_STRINGS) || msgType.equals(BOT_POS)) {
//                outputMsg = msgType + " " + msg + "\n";
//            } else {
//                outputMsg = msgType + "\n" + msg + "\n";
//            }
            String outputMsg = "";
            if(msgType.equals(BOT_POS)){
                outputMsg = "Alg|And|"+msg + "|"; //changed
            } else if(msgType.equals(GET_SENSOR) || msgType.equals(INSTRUCTIONS)){
                outputMsg = "Alg|Ard|"+msg + "|"; //changed
            } else if(msgType.equals(MAP_STRINGS)){
                String str = msg;
                String[] arrOfStr = str.split(" ", 2);
                outputMsg = "Alg|And|md1|"+arrOfStr[0]+"|and|And|md2|"+arrOfStr[1] + "|"; //changed
            }

            System.out.println("Sending out message:\n" + outputMsg + "\n");
            String value = new String(outputMsg.getBytes(ISO), UTF_8);
            writer.write(value);
            writer.flush();

            try{
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }

        } catch (IOException e) {
            System.out.println("sendMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("sendMsg() --> Exception");
            System.out.println(e.toString());
        }
    }

    public String recvMsg() {
        System.out.println("Receiving a message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                System.out.println(sb.toString());
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("recvMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("recvMsg() --> Exception");
            System.out.println(e.toString());
        }

        return null;
    }

    public boolean isConnected() {
        return conn.isConnected();
    }
}
