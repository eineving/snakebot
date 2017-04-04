package se.cygni.snake;

import se.cygni.snake.api.event.MapUpdateEvent;
import se.cygni.snake.api.model.GameMode;
import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.api.model.SnakeInfo;
import se.cygni.snake.brains.*;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by danie on 2017-03-27.
 */
public class BrainySnake extends SimpleSnakePlayer {
    private MapUtil mapUtil;
    private List<Sense> senses = new LinkedList<>();

    private static final GameMode GAME_MODE = GameMode.TOURNAMENT;

    public BrainySnake() {
        senses.add(new Obvious());
        senses.add(new Caution(0.1));
        senses.add(new Planning());
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        System.out.println("=================MOVE NUMBER " + move++ + "==============");
        // MapUtil contains lot's of useful methods for querying the map!
        mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        SnakeInfo[] snakeInfo = mapUpdateEvent.getMap().getSnakeInfos();

        List<String> liveSnakeIDs = new ArrayList<>();

        for (SnakeInfo snake : snakeInfo) {
            if (snake.isAlive()) {
                liveSnakeIDs.add(snake.getId());
            }
        }
        List<Map<SnakeDirection, Double>> sensePrios = new LinkedList<>();

        for (Sense sense : senses) {
            sensePrios.add(sense.getMovesRanked(mapUtil, liveSnakeIDs));
        }


        Double up = 1.0, down = 1.0, left = 1.0, right = 1.0;
        for (Map<SnakeDirection, Double> instance : sensePrios) {
            up *= instance.get(SnakeDirection.UP);
            down *= instance.get(SnakeDirection.DOWN);
            left *= instance.get(SnakeDirection.LEFT);
            right *= instance.get(SnakeDirection.RIGHT);
        }

        System.out.println("PRIOS! Up:" + up + " Down:" + down + " Right: " + right + " Left:" + left);

        SnakeDirection moveToMake = SnakeDirection.UP;
        if (up == 0 && down == 0 && right == 0 && left == 0) {
            System.out.println("FUUUUUUUUUUUUUUUUCK!");
        } else if (up >= down && up >= right && up >= left) {
            moveToMake = SnakeDirection.UP;
        } else if (down >= up && down >= right && down >= left) {
            moveToMake = SnakeDirection.DOWN;
        } else if (right >= up && right >= down && right >= left) {
            moveToMake = SnakeDirection.RIGHT;
        } else if (left >= up && left >= right && left >= down) {
            moveToMake = SnakeDirection.LEFT;
        }

        registerMove(mapUpdateEvent.getGameTick(), moveToMake);

        System.out.println("MOVE: " + moveToMake);
        // Register action here!
    }

    private List<SnakeDirection> getIntersectionOfMoves(List<SnakeDirection> combinedMoves, List<SnakeDirection> wantedOrder) {
        List<SnakeDirection> intersections = new LinkedList<>();
        for (SnakeDirection dir : wantedOrder) {
            if (combinedMoves.contains(dir)) {
                intersections.add(dir);
            }
        }
        return intersections;
    }
}
