package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by danie on 2017-04-03.
 */
public class Obvious extends Sense {
    @Override
    public List<SnakeDirection> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        List<SnakeDirection> movableDirections = new LinkedList<>();

        if(mapUtil.canIMoveInDirection(SnakeDirection.UP)){
            movableDirections.add(SnakeDirection.UP);
        }
        if(mapUtil.canIMoveInDirection(SnakeDirection.DOWN)){
            movableDirections.add(SnakeDirection.DOWN);
        }
        if(mapUtil.canIMoveInDirection(SnakeDirection.RIGHT)){
            movableDirections.add(SnakeDirection.RIGHT);
        }
        if(mapUtil.canIMoveInDirection(SnakeDirection.LEFT)){
            movableDirections.add(SnakeDirection.LEFT);
        }
        return movableDirections;
    }
}
