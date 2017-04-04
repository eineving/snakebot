package se.cygni.snake.brains.workingprogress;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.brains.MyUtils;
import se.cygni.snake.brains.Sense;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;

/**
 * Created by danie on 2017-04-04.
 */
public class Aggression extends Sense {

    private Set<MapCoordinate> tempBounding = new HashSet<>();
    private List<String> liveSnakes;


    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        //bounding box sizes
        this.liveSnakes = liveSnakes;
        List<Integer> up, down, left, right;
        int biggestUp = 0;
        int biggestDown = 0;
        int biggestLeft = 0;
        int biggestRight = 0;
        int lowestUp = 100000;
        int lowestDown = 100000;
        int lowestLeft = 100000;
        int lowestRight = 100000;

        if (mapUtil.canIMoveInDirection(SnakeDirection.UP)) {
            up = reductionIfMove(mapUtil, mapUtil.getMyPosition().translateBy(0, -1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.DOWN)) {
            down = reductionIfMove(mapUtil, mapUtil.getMyPosition().translateBy(0, 1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.RIGHT)) {
            right = reductionIfMove(mapUtil, mapUtil.getMyPosition().translateBy(1, 0));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.LEFT)) {
            left = reductionIfMove(mapUtil, mapUtil.getMyPosition().translateBy(-1, 0));
        }


        for (int i = 0; i < liveSnakes.size(); i++) {
            if (up != null) {
                int value = up.get(i);
                if (value < lowestUp) {
                    lowestUp = value;
                }
                if (value > highestUp) {
                    highestUp = value;
                }
            }
        }

        System.out.println("Up:" + up + " Down:" + down + " Right: " + right + " Left:" + left);

        return getSnakeDirections(up, down, left, right);
    }

    private List<Integer> boundingsIfMove(MapUtil mapUtil, MapCoordinate proposedMove) {
        List<Integer> boundings = new LinkedList<>();
        for (MapCoordinate enemyHead : MyUtils.getEnemyHeads(mapUtil, liveSnakes)) {
            tempBounding = new HashSet<>();
            tempBounding.add(proposedMove);
            boundings.add(wrapped(mapUtil, enemyHead));
        }
        return boundings;
    }

    private int wrapped(MapUtil mapUtil, MapCoordinate start) {
        if (tempBounding.add(start)) {
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
