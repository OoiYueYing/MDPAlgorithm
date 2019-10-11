package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import map.Cell;
import map.Map;
import map.MapConstants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;

/**
 * Exploration algorithm for the robot.
 *
 * @author Priyanshu Singh
 * @author Suyash Lakhotia
 */

public class ExplorationAlgo {
    private final Map exploredMap;
    private final Map realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int areaExplored;
    private long startTime;
    private long endTime;
    private int lastCalibrate;
    private boolean calibrationMode;
    private boolean timesUp;

    public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
//        //doing calibration
//        if (bot.getRealBot()) {
//
//            System.out.println("Starting calibration...");
//
//            //What is this??
//
//            bot.move(MOVEMENT.LEFT, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.CALIBRATE, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.LEFT, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.CALIBRATE, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.RIGHT, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.CALIBRATE, false);
//            CommMgr.getCommMgr().recvMsg();
//            bot.move(MOVEMENT.RIGHT, false);
//        }

        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);


        System.out.println("Setting end time as : " + endTime);

        //Initialize
        //First senseAndRepaint()
        senseAndRepaint();
        //First explored area
        areaExplored = calculateAreaExplored();
        System.out.println("\nExplored Area: " + areaExplored + "\n");

        //Start moving
        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {
        do {
            nextMove();

            //every move check area explored
            areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);

            // if robot back to starting position of exploration loop
            //start to check for unexplored
            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) {
                //unexplored logic
                timesUp = false;

                //handle unexplored, those cell in the middle
                List<Cell>  unexploredCells =  getUnexploredCell(exploredMap);
                //loop through all unexplored cell
                //Checking unexplored cell
                System.out.println("The unexplored cells are : ");
                for(int i=0; i<unexploredCells.size();i++) {
                    System.out.println("(" + unexploredCells.get(i).getRow() + "," + unexploredCells.get(i).getCol() + ")") ;
                }

                for(int i=0;i<unexploredCells.size();i++) {
                    if(unexploredCells.get(i).getIsExplored()) {
                        continue;
                    }
                    //get adj cell of the unexplored cell
                    List<Cell> adjacentCells = getAdjacentCell(unexploredCells.get(i), exploredMap);

                    System.out.println("The adjacent cells are : ");
                    for(int j=0 ; j<adjacentCells.size(); j++){
                        System.out.println("(" + adjacentCells.get(j).getRow() + "," + adjacentCells.get(j).getCol() + ")") ;
                    }

                    if(!adjacentCells.isEmpty()) {
                        //loop through all adjacent cells of the unexplored cells and try and find path
                        for(int j=0 ;j<adjacentCells.size();j++) {
                            //try and find from bot current position to unexplored cell
                            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap);
                            if(System.currentTimeMillis() <= endTime) {
                                if(goToGoal.runFastestPath(adjacentCells.get(j).getRow(), adjacentCells.get(j).getCol()) != null) {
                                    areaExplored = calculateAreaExplored();
                                    System.out.println("Area explored: " + areaExplored);
                                    break;
                                }
                            } else{
                                timesUp = true;
                                break;
                            }
                        }
                    }
                    if( timesUp == true){
                        break;
                    }

                }


                if (areaExplored >=100){
                    break;
                }
            }


            System.out.println("Current time now : " + System.currentTimeMillis());


        } while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);

        System.out.println("CAMEEOUTT");

        goHome();

        System.out.println("Exploration complete!");
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");


    }

    private boolean inStartZone(int row, int col) {
        return row >= 0 && row <= 2 && col >= 0 && col <= 2;
    }


    //adjacent cells list that are explored
    private List<Cell> getAdjacentCell(Cell unexploredCell, Map exploredMap){
        List<Cell> unexploredAdjacentCells = new ArrayList<Cell>();
        //set the boundary of the adjacent cells to 2

        for(int i=unexploredCell.getRow()-1; i <= unexploredCell.getRow()+1; i++) {
            for(int j=unexploredCell.getCol()-1; j <= unexploredCell.getCol()+1; j++) {
                if(i<MapConstants.MAP_ROWS && i>=0 && j<MapConstants.MAP_COLS && j >=0) {
                    Cell adjCell = exploredMap.getCell(i, j);
                    if(adjCell.getIsExplored() && !inStartZone(adjCell.getRow(),adjCell.getCol()) && !adjCell.getIsObstacle() && !adjCell.getIsVirtualWall()) {
                        unexploredAdjacentCells.add(adjCell);
                    } else {
                        System.out.println("denied: "+adjCell.getRow() + " " + adjCell.getCol());
                    }
                }
            }
        }
        return unexploredAdjacentCells;
    }

    //return an array of array of cell object
    private List<Cell>  getUnexploredCell(Map exploredMap) {
        // TODO Auto-generated method stub
        List<Cell>  unexploredCells = new ArrayList<Cell>();
        for (int row = 0; row < MapConstants.MAP_ROWS; row++) {
            for (int col = 0; col < MapConstants.MAP_COLS; col++) {
                if (!exploredMap.getCell(row,col).getIsExplored()) {
                    unexploredCells.add(exploredMap.getCell(row,col));
                }
            }
        }
        return unexploredCells;
    }

    //right wall hugging
    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() {
        if (lookRight()) {
            moveBot(MOVEMENT.RIGHT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
        } else if (lookForward()) {
            moveBot(MOVEMENT.FORWARD);
        } else if (lookLeft()) {
            moveBot(MOVEMENT.LEFT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
        } else {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }



    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean lookForward() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    private boolean lookLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {

        //make sure robot touched goal zone
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 360) {
            FastestPathAlgo goToGoal = new FastestPathAlgo(exploredMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }


        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, realMap);
        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        //calibrating at start position?
//        if (bot.getRealBot()) {
//            turnBotDirection(DIRECTION.WEST);
//            //moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.SOUTH);
//            //moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.WEST);
//            //moveBot(MOVEMENT.CALIBRATE);
//        }

        //finally turn to north
        turnBotDirection(DIRECTION.NORTH);
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    private int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapConstants.MAP_ROWS; r++) {
            for (int c = 0; c < MapConstants.MAP_COLS; c++) {
                if (exploredMap.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Moves the bot, repaints the map and calls senseAndRepaint().
     */
    private void moveBot(MOVEMENT m) {
        bot.move(m);
        exploredMap.repaint();
        if (m != MOVEMENT.CALIBRATE) {
            senseAndRepaint();
        }

//        else {
//            CommMgr commMgr = CommMgr.getCommMgr();
//            commMgr.recvMsg();
//        }

//        if (bot.getRealBot() && !calibrationMode) {
//            calibrationMode = true;
//
//            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
//                lastCalibrate = 0;
//                moveBot(MOVEMENT.CALIBRATE);
//            } else {
//                lastCalibrate++;
//                if (lastCalibrate >= 5) {
//                    DIRECTION targetDir = getCalibrationDirection();
//                    if (targetDir != null) {
//                        lastCalibrate = 0;
//                        calibrateBot(targetDir);
//                    }
//                }
//            }
//
//            calibrationMode = false;
//        }
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap);
        exploredMap.repaint();
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
    private DIRECTION getCalibrationDirection() {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        return null;
    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotDirection(targetDir);
        moveBot(MOVEMENT.CALIBRATE);
        turnBotDirection(origDir);
    }

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }
}
