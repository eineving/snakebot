package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.*;
import se.cygni.snake.api.model.Map;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;

public class SimpleSnakePlayer extends BaseSnakeClient {

    private MapUtil mapUtil;
    ArrayList<MapCoordinate> filled = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSnakePlayer.class);

    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = false;

    // Personalise your game ...
    private static final String SERVER_NAME = "snake.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TOURNAMENT;
    private static final String SNAKE_NAME = "Eine sneaky snake";

    // Set to false if you don't want the game world printed every game tick.
    private static final boolean ANSI_PRINTER_ACTIVE = false;
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, true);

    public static void main(String[] args) {
        SimpleSnakePlayer simpleSnakePlayer = new SimpleSnakePlayer();

        try {
            ListenableFuture<WebSocketSession> connect = simpleSnakePlayer.connect();
            connect.get();
        } catch (Exception e) {
            LOGGER.error("Failed to connect to server", e);
            System.exit(1);
        }

        startTheSnake(simpleSnakePlayer);
    }

    /**
     * The Snake client will continue to run ...
     * : in TRAINING mode, until the single game ends.
     * : in TOURNAMENT mode, until the server tells us its all over.
     */
    private static void startTheSnake(final SimpleSnakePlayer simpleSnakePlayer) {
        Runnable task = () -> {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (simpleSnakePlayer.isPlaying());

            LOGGER.info("Shutting down");
        };

        Thread thread = new Thread(task);
        thread.start();
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        ansiPrinter.printMap(mapUpdateEvent);
        filled = null;

        // MapUtil contains lot's of useful methods for querying the map!
        mapUtil = new MapUtil(mapUpdateEvent.getMap(), getPlayerId());

        List<SnakeDirection> possibleDirections = possibleDirections();

        SnakeInfo[] snakeInfo = mapUpdateEvent.getMap().getSnakeInfos();
        List<String> liveSnakeIDs = new ArrayList<>();

        for (SnakeInfo snake : snakeInfo) {
            if (snake.isAlive()) {
                liveSnakeIDs.add(snake.getId());
            }
        }

        SnakeDirection chosenDirection = antiGravityDirection(liveSnakeIDs, possibleDirections);
        System.out.println(chosenDirection.toString());

        // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
    }

    private List<SnakeDirection> directionsWithPossibleEscapeRoutes(List<SnakeDirection> possibleDirections) {
        List<SnakeDirection> directions = new ArrayList<>();
        //mapUtil.getSnakeSpread()


        return directions;
    }

    private int possibleStepsFromDirectionWrapper(List<String> snakeIDs, SnakeDirection direction) {
        if (filled == null) {
            filled = new ArrayList<>();
            for (String id : snakeIDs) {
                for (MapCoordinate coordinate : mapUtil.getSnakeSpread(id)) {
                    filled.add(coordinate);
                }
            }
        }
        switch (direction) {
            case UP:
                return psfd(filled, mapUtil.getMyPosition().translateBy(0, -1));
            case DOWN:
                return psfd(filled, mapUtil.getMyPosition().translateBy(0, 1));
            case RIGHT:
                return psfd(filled, mapUtil.getMyPosition().translateBy(1, 0));
            case LEFT:
                return psfd(filled, mapUtil.getMyPosition().translateBy(-1, 0));

        }
        return 0;
    }

    private int psfd(ArrayList<MapCoordinate> filled, MapCoordinate coordinate) {
        return 0;
    }

    private SnakeDirection antiGravityDirection(List<String> snakeIDs, List<SnakeDirection> possibleDirections) {
        double weightX = 0.0;
        double weightY = 0.0;

        MapCoordinate myPos = mapUtil.getMyPosition();

        System.out.println("xPos:" + myPos.x);
        System.out.println("yPos:" + myPos.y);

        for (String id : snakeIDs) {
            MapCoordinate snakeHead = mapUtil.getSnakeSpread(id)[0];

            //TODO change ugly stuff

            if (myPos.x != snakeHead.x) {
                weightX += (1.0 / (myPos.x - snakeHead.x));
            }
            if (myPos.y != snakeHead.y) {
                weightY += (1.0 / (myPos.y - snakeHead.y));
            }
            System.out.println("Manhattan distance: " + snakeHead.getManhattanDistanceTo(myPos));
            System.out.println("yDiff: " + (myPos.y - snakeHead.y));
            System.out.println("xDiff: " + (myPos.x - snakeHead.x));

            //snakeHead.getManhattanDistanceTo(myPos);
        }

        //To make it easier to understand nested hell bellow
        boolean xLargestAbs = Math.abs(weightX) > Math.abs(weightY);
        boolean xWeightLeft = weightX < 0;
        boolean yWeightUp = weightY < 0;

        System.out.println("WeightY: " + weightY);
        System.out.println("WeightX: " + weightX);
        System.out.println("");

        SnakeDirection antiGravityDirection;


        //Biggest abs first, then smallest, then inverse of smallerst, last inverse of biggest
        if (!xLargestAbs) {
            if (xWeightLeft && yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.UP, SnakeDirection.LEFT, SnakeDirection.RIGHT, SnakeDirection.DOWN);
            } else if (xWeightLeft && !yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.UP, SnakeDirection.RIGHT, SnakeDirection.LEFT, SnakeDirection.DOWN);
            } else if (!xWeightLeft && !yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.DOWN, SnakeDirection.RIGHT, SnakeDirection.LEFT, SnakeDirection.UP);
            } else {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.DOWN, SnakeDirection.LEFT, SnakeDirection.RIGHT, SnakeDirection.UP);
            }
        } else {
            if (xWeightLeft && yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.LEFT, SnakeDirection.UP, SnakeDirection.DOWN, SnakeDirection.RIGHT);
            } else if (xWeightLeft && !yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.RIGHT, SnakeDirection.UP, SnakeDirection.DOWN, SnakeDirection.LEFT);
            } else if (!xWeightLeft && !yWeightUp) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.RIGHT, SnakeDirection.DOWN, SnakeDirection.UP, SnakeDirection.LEFT);
            } else {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.LEFT, SnakeDirection.DOWN, SnakeDirection.UP, SnakeDirection.RIGHT);
            }
        }
    }

    private SnakeDirection getDirectionFromPrio(List<SnakeDirection> possibleDirections, SnakeDirection first, SnakeDirection second, SnakeDirection third, SnakeDirection forth) {
        if (possibleDirections.contains(first)) {
            return first;
        } else if (possibleDirections.contains(second)) {
            return second;
        } else if (possibleDirections.contains(third)) {
            return third;
        } else {
            return forth;
        }
    }

    private List<SnakeDirection> possibleDirections() {
        List<SnakeDirection> directions = new ArrayList<>();
        for (SnakeDirection direction : SnakeDirection.values()) {
            if (mapUtil.canIMoveInDirection(direction)) {
                directions.add(direction);
            }
        }
        return directions;
    }


    @Override
    public void onInvalidPlayerName(InvalidPlayerName invalidPlayerName) {
        LOGGER.debug("InvalidPlayerNameEvent: " + invalidPlayerName);
    }

    @Override
    public void onSnakeDead(SnakeDeadEvent snakeDeadEvent) {
        LOGGER.info("A snake {} died by {}",
                snakeDeadEvent.getPlayerId(),
                snakeDeadEvent.getDeathReason());
    }

    @Override
    public void onGameResult(GameResultEvent gameResultEvent) {
        LOGGER.info("Game result:");
        gameResultEvent.getPlayerRanks().forEach(playerRank -> LOGGER.info(playerRank.toString()));
    }

    @Override
    public void onGameEnded(GameEndedEvent gameEndedEvent) {
        LOGGER.debug("GameEndedEvent: " + gameEndedEvent);
    }

    @Override
    public void onGameStarting(GameStartingEvent gameStartingEvent) {
        LOGGER.debug("GameStartingEvent: " + gameStartingEvent);
    }

    @Override
    public void onPlayerRegistered(PlayerRegistered playerRegistered) {
        LOGGER.info("PlayerRegistered: " + playerRegistered);

        if (AUTO_START_GAME) {
            startGame();
        }
    }

    @Override
    public void onTournamentEnded(TournamentEndedEvent tournamentEndedEvent) {
        LOGGER.info("Tournament has ended, winner playerId: {}", tournamentEndedEvent.getPlayerWinnerId());
        int c = 1;
        for (PlayerPoints pp : tournamentEndedEvent.getGameResult()) {
            LOGGER.info("{}. {} - {} points", c++, pp.getName(), pp.getPoints());
        }
    }

    @Override
    public void onGameLink(GameLinkEvent gameLinkEvent) {
        LOGGER.info("The game can be viewed at: {}", gameLinkEvent.getUrl());
    }

    @Override
    public void onSessionClosed() {
        LOGGER.info("Session closed");
    }

    @Override
    public void onConnected() {
        LOGGER.info("Connected, registering for training...");
        GameSettings gameSettings = GameSettingsUtils.trainingWorld();
        registerForGame(gameSettings);
    }

    @Override
    public String getName() {
        return SNAKE_NAME;
    }

    @Override
    public String getServerHost() {
        return SERVER_NAME;
    }

    @Override
    public int getServerPort() {
        return SERVER_PORT;
    }

    @Override
    public GameMode getGameMode() {
        return GAME_MODE;
    }
}
