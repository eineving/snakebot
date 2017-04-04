package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Checks for no head crash.
 */
public class Caution extends Sense {
    Double reductionFactor;

    public Caution(Double reductionFactor) {
        this.reductionFactor = reductionFactor;
    }

    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        MapCoordinate[] heads = new MapCoordinate[liveSnakes.size() - 1];
        List<MapCoordinate> nextCoordinates = filledSurroundings(mapUtil);
        Map<SnakeDirection, Double> weighted = getPrioTemplate();


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

        if(enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(-1,0))){
           weighted.put(SnakeDirection.LEFT, reductionFactor);
        }
        if(enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(1,0))){
            weighted.put(SnakeDirection.RIGHT, reductionFactor);
        }
        if(enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(0,1))){
            weighted.put(SnakeDirection.DOWN, reductionFactor);
        }
        if(enemyPossibleNext.contains(mapUtil.getMyPosition().translateBy(0,-1))){
            weighted.put(SnakeDirection.UP, reductionFactor);
        }
        return weighted;
    }


}
