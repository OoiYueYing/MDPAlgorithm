package simulator;

public class test {




    public static void main(String[] args) {

        long startTime;
        long endTime;
        final int timeLimit = 60; //60 sec

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        System.out.println(startTime);
        System.out.println(endTime);

        do{}
        while(System.currentTimeMillis() <= endTime) ;

        System.out.println("END");

    }








}
