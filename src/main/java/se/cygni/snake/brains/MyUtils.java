package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by danie on 2017-04-03.
 */
public class MyUtils {
    public static List<SnakeDirection> filledMoves(MapUtil mapUtil) {
        List<SnakeDirection> filled = new LinkedList<>();
        filled.add(SnakeDirection.DOWN);
        filled.add(SnakeDirection.UP);
        filled.add(SnakeDirection.RIGHT);
        filled.add(SnakeDirection.LEFT);
        return filled;
    }

    public static List<MapCoordinate> getTheFourNeighbours(MapCoordinate root) {
        List<MapCoordinate> neighbours = new ArrayList<>();
        neighbours.add(root.translateBy(-1, 0));
        neighbours.add(root.translateBy(1, 0));
        neighbours.add(root.translateBy(0, 1));
        neighbours.add(root.translateBy(0, -1));
        return neighbours;
    }
}
