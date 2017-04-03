package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Checks for no head crash.
 */
public class Caution extends Sense {


    @Override
    public List<SnakeDirection> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        MapCoordinate[] heads = new MapCoordinate[liveSnakes.size() - 1];
        List<MapCoordinate> nextCoordinates = filledSurroundings(mapUtil);
        List<SnakeDirection> possibleDirection = new LinkedList<>();


        int i = 0;
        for (String snake : liveSnakes) {
            MapCoordinate temp = mapUtil.getSnakeSpread(snake)[0];
            if (!temp.equals(mapUtil.getMyPosition())) {
                heads[i] = temp;
                i++;
            }
        }

        List<MapCoordinate> enemyPossibleNext = new LinkedList<>();

        for (MapCoordinate enemyHead : heads) {
            enemyPossibleNext.add(enemyHead.translateBy(-1, 0));
            enemyPossibleNext.add(enemyHead.translateBy(1, 0));
            enemyPossibleNext.add(enemyHead.translateBy(0, 1));
            enemyPossibleNext.add(enemyHead.translateBy(0, -1));
        }

        if(!enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(-1,0))){
            possibleDirection.add(SnakeDirection.LEFT);
        }
        if(!enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(1,0))){
            possibleDirection.add(SnakeDirection.RIGHT);
        }
        if(!enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(0,1))){
            possibleDirection.add(SnakeDirection.DOWN);
        }
        if(!enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(0,-1))){
            possibleDirection.add(SnakeDirection.UP);
        }
        return possibleDirection;
    }


}
