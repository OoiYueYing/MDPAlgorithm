package simulator;

import utils.CommMgr;
import java.util.regex.Pattern;

public class RecvTest {


    public static void main(String[] args) {


        String msg = "SDATA|3, 5, 2, 2, 162, 3";


        String[] msgArr = msg.split(Pattern.quote("|"));
        int[] result = new int[6];

        System.out.println(msgArr[0]);
        System.out.println(msgArr[1]);

        //msgArr[0] = SDATA
        //msgArr[1] = msg
        // SENSOR_DATA = "SDATA";       // Arduino --> PC
        if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
            //split into different types of sensors
            String[] sensorArr = msgArr[1].split(Pattern.quote(", "));
            //in each sensor type split by space and obtain the second part of the string
            result[0] = Integer.parseInt(sensorArr[0]);
            result[1] = Integer.parseInt(sensorArr[1]);
            result[2] = Integer.parseInt(sensorArr[2]);
            result[3] = Integer.parseInt(sensorArr[3]);
            result[4] = Integer.parseInt(sensorArr[4]);
            result[5] = Integer.parseInt(sensorArr[5]);
        }

        System.out.println(result[0]);
        System.out.println(result[1]);
        System.out.println(result[2]);
        System.out.println(result[3]);
        System.out.println(result[4]);
        System.out.println(result[5] + "\n");



        String msg1 = "EX_START";
        String msg2 = "WAY_POINT|Waypoint: 10,8";


        Integer waypointRow;
        Integer waypointCol;

        if (msg1.equals(CommMgr.EX_START)) {
            String[] msgArr2 = msg2.split(Pattern.quote("|"));
            System.out.println(msgArr2[0]);
            System.out.println(msgArr2[1]);
            if (msgArr2[0].equals(CommMgr.WAY_POINT)) {
                String[] coords= msgArr2[1].split(Pattern.quote(": "));


                String[] xycoords = coords[1].split(Pattern.quote(","));

                waypointRow = Integer.parseInt(xycoords[0]);
                waypointCol = Integer.parseInt(xycoords[1]);
                System.out.println(waypointRow);
                System.out.println(waypointCol);
            }
        }


    }
}

