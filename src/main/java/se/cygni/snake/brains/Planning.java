package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;

/**
 * Created by danie on 2017-04-03.
 */
public class Planning extends Sense {
    private Set<MapCoordinate> tempBounding;

    @Override
    public List<SnakeDirection> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        //bounding box sizes
        int up = 0, down = 0, left = 0, right = 0;

        if (mapUtil.canIMoveInDirection(SnakeDirection.UP)) {
            up = wrapped(mapUtil, mapUtil.getMyPosition().translateBy(0, -1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.DOWN)) {
            down = wrapped(mapUtil, mapUtil.getMyPosition().translateBy(0, 1));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.RIGHT)) {
            right = wrapped(mapUtil, mapUtil.getMyPosition().translateBy(1, 0));
        }
        if (mapUtil.canIMoveInDirection(SnakeDirection.LEFT)) {
            left = wrapped(mapUtil, mapUtil.getMyPosition().translateBy(-1, 0));
        }

        System.out.println("Up:" + up + " Down:" + down + " Right: " + right + " Left:" + left);


        List<SnakeDirection> orderdDirs = new LinkedList<>();
        for (int i = 0; i < 4; i++) {
            if (up == 0 && down == 0 && right == 0 && left == 0) {
                break;
            } else if (up >= down && up >= right && up >= left) {
                orderdDirs.add(SnakeDirection.UP);
                up = 0;
            } else if (down >= up && down >= right && down >= left) {
                orderdDirs.add(SnakeDirection.DOWN);
                down = 0;

            } else if (right >= up && right >= down && down >= left) {
                orderdDirs.add(SnakeDirection.RIGHT);
                right = 0;

            } else if (left >= up && left >= right && left >= right) {
                orderdDirs.add(SnakeDirection.LEFT);
                left = 0;
            }
        }
        return orderdDirs;
    }

    private int wrapped(MapUtil mapUtil, MapCoordinate start) {
        tempBounding = new HashSet<>();
        tempBounding.add(start);
        //System.out.println("Up");
        recursive(mapUtil, start.translateBy(0, -1));
        //System.out.println("Down");
        recursive(mapUtil, start.translateBy(0, 1));
        //System.out.println("Right");
        recursive(mapUtil, start.translateBy(1, 0));
        //System.out.println("Left");
        recursive(mapUtil, start.translateBy(-1, 0));

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
