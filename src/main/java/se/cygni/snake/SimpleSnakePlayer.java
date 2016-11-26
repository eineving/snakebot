package se.cygni.snake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketSession;
import se.cygni.snake.api.event.*;
import se.cygni.snake.api.exception.InvalidPlayerName;
import se.cygni.snake.api.model.*;
import se.cygni.snake.api.response.PlayerRegistered;
import se.cygni.snake.api.util.GameSettingsUtils;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimpleSnakePlayer extends BaseSnakeClient {

    private MapUtil mapUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSnakePlayer.class);

    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = false;

    // Personalise your game ...
    private static final String SERVER_NAME = "snake.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TRAINING;
    private static final String SNAKE_NAME = "Kaa";

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
        /*
        Random r = new Random();
        SnakeDirection chosenDirection = SnakeDirection.DOWN;

        // Choose a random direction
        if (!directions.isEmpty())
            chosenDirection = directions.get(r.nextInt(directions.size()));

        */

        // Register action here!
        registerMove(mapUpdateEvent.getGameTick(), chosenDirection);
    }

    private SnakeDirection antiGravityDirection(List<String> snakeIDs, List<SnakeDirection> possibleDirections) {
        double weightX = 0;
        double weightY = 0;

        MapCoordinate myPos = mapUtil.getMyPosition();
        for (String id : snakeIDs) {
            MapCoordinate snakeHead = mapUtil.getSnakeSpread(id)[0];

            try {
                weightX += 1 / (myPos.x - snakeHead.x);
            } catch (ArithmeticException ign) {
            }
            try {
                weightY += 1 / (myPos.y - snakeHead.y);
            } catch (ArithmeticException ign) {
            }
            //snakeHead.getManhattanDistanceTo(myPos);
        }

        //To make it easier to understand nested hell bellow
        boolean xLargestAbs = Math.abs(weightX) > Math.abs(weightY);
        boolean xWeightUp = weightX > 0;
        boolean yWeightLeft = weightY > 0;

        boolean leftPossible = possibleDirections.contains(SnakeDirection.LEFT);
        boolean rightPossible = possibleDirections.contains(SnakeDirection.RIGHT);
        boolean upPossible = possibleDirections.contains(SnakeDirection.UP);
        boolean downPossible = possibleDirections.contains(SnakeDirection.DOWN);

        SnakeDirection antiGravityDirection;


        //Biggest abs first, then smallest, then inverse of smallerst, last inverse of biggest
        if (xLargestAbs) {
            if (xWeightUp && yWeightLeft) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.UP, SnakeDirection.LEFT, SnakeDirection.RIGHT, SnakeDirection.DOWN);
            } else if (xWeightUp && !yWeightLeft) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.UP, SnakeDirection.RIGHT, SnakeDirection.LEFT, SnakeDirection.DOWN);
            } else if (!xWeightUp && !yWeightLeft) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.DOWN, SnakeDirection.RIGHT, SnakeDirection.LEFT, SnakeDirection.UP);
            } else {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.DOWN, SnakeDirection.LEFT, SnakeDirection.RIGHT, SnakeDirection.UP);
            }
        } else {
            if (xWeightUp && yWeightLeft) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.LEFT, SnakeDirection.UP, SnakeDirection.DOWN, SnakeDirection.RIGHT);
            } else if (xWeightUp && !yWeightLeft) {
                return getDirectionFromPrio(possibleDirections, SnakeDirection.RIGHT, SnakeDirection.UP, SnakeDirection.DOWN, SnakeDirection.LEFT);
            } else if (!xWeightUp && !yWeightLeft) {
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

    private SnakeDirection leftOrRight(List<SnakeDirection> possibleDirections, double weightX) {
        if (possibleDirections.contains(SnakeDirection.LEFT) && weightX < 0) {
            return SnakeDirection.LEFT;
        } else if (possibleDirections.contains(SnakeDirection.RIGHT) && weightX > 0) {
            return SnakeDirection.RIGHT;
        }
        return null;
    }

    private SnakeDirection upOrDown(List<SnakeDirection> possibleDirections, double weightX) {
        if (possibleDirections.contains(SnakeDirection.DOWN) && weightX < 0) {
            return SnakeDirection.DOWN;
        } else if (possibleDirections.contains(SnakeDirection.UP) && weightX > 0) {
            return SnakeDirection.UP;
        }
        return null;
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
