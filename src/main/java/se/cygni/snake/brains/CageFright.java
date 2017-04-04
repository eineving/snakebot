package se.cygni.snake.brains;

import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danie on 2017-04-05.
 */
public class CageFright extends Sense {
    private Double reward;
    private Double penalty;

    public CageFright(Double reward, Double penalty) {
        this.reward = reward;
        this.penalty = penalty;
    }

    @Override
    public Map<SnakeDirection, Double> getMovesRanked(MapUtil mapUtil, List<String> liveSnakes) {
        Map<SnakeDirection, Double> prio = new HashMap<>();
        if (isBorder(mapUtil.getMyPosition().translateBy(0, -1))) {
            prio.put(SnakeDirection.UP, penalty );
        } else {
            prio.put(SnakeDirection.UP, reward);
        }


        if (isBorder(mapUtil.getMyPosition().translateBy(0, 1))) {
            prio.put(SnakeDirection.DOWN, penalty );
        } else {
            prio.put(SnakeDirection.DOWN, reward);
        }


        if (isBorder(mapUtil.getMyPosition().translateBy(1, 0))) {
            prio.put(SnakeDirection.RIGHT, penalty );
        } else {
            prio.put(SnakeDirection.RIGHT, reward);
        }


        if (isBorder(mapUtil.getMyPosition().translateBy(-1, 0))) {
            prio.put(SnakeDirection.LEFT, penalty );
        } else {
            prio.put(SnakeDirection.LEFT, reward);
        }
        //45 33


        return prio;
    }

    private boolean isBorder(MapCoordinate coordinate) {
        return (coordinate.x < 1 && coordinate.x >= 45 && coordinate.y < 1 && coordinate.y >= 33);
    }
}
