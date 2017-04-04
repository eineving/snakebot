package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;

/**
 * Created by danie on 2017-04-03.
 */
public class Planning extends Sense {
    private Set<MapCoordinate> tempBounding = new HashSet<>();
    private Double first = 1.0, second = 0.75, third = 0.5, fourth = 0.25;

    private Double reduction = 0.25;
    private Double increment = 0.25;

    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        //bounding box sizes
        int up = 0, down = 0, left = 0, right = 0;

        if (mapUtil.canIMoveInDirection(SnakeDirection.UP)) {
            up = maxIf(mapUtil, mapUtil.getMyPosition().translateBy(0, -1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.DOWN)) {
            down = maxIf(mapUtil, mapUtil.getMyPosition().translateBy(0, 1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.RIGHT)) {
            right = maxIf(mapUtil, mapUtil.getMyPosition().translateBy(1, 0));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.LEFT)) {
            left = maxIf(mapUtil, mapUtil.getMyPosition().translateBy(-1, 0));
        }

        System.out.println("Up:" + up + " Down:" + down + " Right: " + right + " Left:" + left);

        return getSnakeDirections(up, down, left, right);
    }

    private int maxIf(MapUtil mapUtil, MapCoordinate proposedMove) {

        int max = 1;
        int temp = 0;
        for (MapCoordinate next : MyUtils.getTheFourNeighbours(proposedMove)) {
            tempBounding = new HashSet<>();
            tempBounding.add(proposedMove);
            temp = wrapped(mapUtil, next);
            if (max < temp) {
                max = temp;
            }
        }
        return max;
    }

    private Map<SnakeDirection, Double> getSnakeDirections(int up, int down, int left, int right) {
        Map<SnakeDirection, Double> prios = new HashMap<>();
        TreeMap<Integer, List<SnakeDirection>> tree = new TreeMap<>();


        //UP
        List<SnakeDirection> first = new LinkedList<>();
        first.add(SnakeDirection.UP);
        tree.put(up, first);

        //DOWN
        List<SnakeDirection> temp = tree.get(down);
        if (temp != null) {
            temp.add(SnakeDirection.DOWN);
        } else {
            List<SnakeDirection> tempList = new LinkedList<>();
            tempList.add(SnakeDirection.DOWN);
            tree.put(down, tempList);
        }

        //LEFT
        temp = tree.get(left);
        if (temp != null) {
            temp.add(SnakeDirection.LEFT);
        } else {
            List<SnakeDirection> tempList = new LinkedList<>();
            tempList.add(SnakeDirection.LEFT);
            tree.put(left, tempList);
        }


        //RIGHT
        temp = tree.get(right);
        if (temp != null) {
            temp.add(SnakeDirection.RIGHT);
        } else {
            List<SnakeDirection> tempList = new LinkedList<>();
            tempList.add(SnakeDirection.RIGHT);
            tree.put(right, tempList);
        }

        Double currentPrio = 0.0;
        for (Map.Entry<Integer, List<SnakeDirection>> entry : tree.entrySet()) {
            for (SnakeDirection direction : entry.getValue()) {
                System.out.println("DIRECTION " + direction + " has key " + entry.getKey());
                prios.put(direction, currentPrio);
            }
            currentPrio += increment;
        }
        return prios;



        /*
        List<SnakeDirection> orderdDirs = new LinkedList<>();

        for (int i = 0; i < 4; i++) {
            if (up >= down && up >= right && up >= left && !orderdDirs.contains(SnakeDirection.UP)) {
                orderdDirs.add(SnakeDirection.UP);
                up = 0;
            } else if (down >= up && down >= right && down >= left && !orderdDirs.contains(SnakeDirection.DOWN)) {
                orderdDirs.add(SnakeDirection.DOWN);
                down = 0;

            } else if (right >= up && right >= down && right >= left && !orderdDirs.contains(SnakeDirection.RIGHT)) {
                orderdDirs.add(SnakeDirection.RIGHT);
                right = 0;

            } else if (left >= up && left >= down && left >= right && !orderdDirs.contains(SnakeDirection.LEFT)) {
                orderdDirs.add(SnakeDirection.LEFT);
                left = 0;
            }
        }
        System.out.println("Planning debug: " + orderdDirs.toString());

        prios.put(orderdDirs.get(0), this.first);
        prios.put(orderdDirs.get(1), second);
        prios.put(orderdDirs.get(2), third);
        prios.put(orderdDirs.get(3), fourth);

        return prios;
        */
    }

    private int wrapped(MapUtil mapUtil, MapCoordinate start) {
        if (tempBounding.add(start) && mapUtil.isTileAvailableForMovementTo(start)) {
            //System.out.println("Up");
            recursive(mapUtil, start.translateBy(0, -1));
            //System.out.println("Down");
            recursive(mapUtil, start.translateBy(0, 1));
            //System.out.println("Right");
            recursive(mapUtil, start.translateBy(1, 0));
            //System.out.println("Left");
            recursive(mapUtil, start.translateBy(-1, 0));
        }
        return tempBounding.size();

    }

    private void recursive(MapUtil mapUtil, MapCoordinate coordinate) {
        //System.out.println("Recursive coordinate: "+ coordinate.toString());
        if (mapUtil.isTileAvailableForMovementTo(coordinate)) {
            if (tempBounding.add(coordinate)) {
                //      System.out.println("One more deepa!");
                recursive(mapUtil, coordinate.translateBy(0, -1));
                recursive(mapUtil, coordinate.translateBy(0, 1));
                recursive(mapUtil, coordinate.translateBy(1, 0));
                recursive(mapUtil, coordinate.translateBy(-1, 0));
            }
        }
    }
}
