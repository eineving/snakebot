package se.cygni.snake;

import se.cygni.snake.api.event.MapUpdateEvent;
import se.cygni.snake.api.model.SnakeDirection;
import se.cygni.snake.api.model.SnakeInfo;
import se.cygni.snake.brains.MyUtils;
import se.cygni.snake.brains.Obvious;
import se.cygni.snake.brains.Sense;
import se.cygni.snake.brains.Caution;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by danie on 2017-03-27.
 */
public class BrainySnake extends SimpleSnakePlayer {
    private MapUtil mapUtil;
    private List<Sense> senses = new LinkedList<>();

    public BrainySnake() {
        senses.add(new Obvious());
        senses.add(new Caution());
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
        List<SnakeDirection> combined = MyUtils.filledMoves(mapUtil);
        List<SnakeDirection> lastSense;
        for (Sense sense : senses) {
            lastSense = sense.getMovesRanked(mapUtil, liveSnakeIDs);
            combined = getIntersectionOfMoves(combined, lastSense);
            System.out.println(combined);
        }

        registerMove(mapUpdateEvent.getGameTick(), combined.get(0));
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
