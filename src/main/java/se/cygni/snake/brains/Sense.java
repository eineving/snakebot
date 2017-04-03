package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danie on 2017-04-03.
 */
public abstract class Sense {
    public abstract List<SnakeDirection> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes);


    protected List<MapCoordinate> filledSurroundings(MapUtil mapUtil) {
        List<MapCoordinate> filled = new LinkedList<>();
        filled.add(mapUtil.getMyPosition().translateBy(0, -1));
        filled.add(mapUtil.getMyPosition().translateBy(0, 1));
        filled.add(mapUtil.getMyPosition().translateBy(1, 0));
        filled.add(mapUtil.getMyPosition().translateBy(-1, 0));
        return filled;
    }
}
