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
import se.cygni.snake.brain.*;
import se.cygni.snake.client.AnsiPrinter;
import se.cygni.snake.client.BaseSnakeClient;
import se.cygni.snake.client.MapCoordinate;
import se.cygni.snake.client.MapUtil;

import java.util.*;

public class BrainySnakePlayer extends BaseSnakeClient {

    private MapUtil mapUtil;
    ArrayList<MapCoordinate> filled = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(BrainySnakePlayer.class);
    int move = 0;
    // Set to false if you want to start the game from a GUI
    private static final boolean AUTO_START_GAME = false;

    // Personalise your game ...
    private static final String SERVER_NAME = "snake.cygni.se";
    private static final int SERVER_PORT = 80;

    private static final GameMode GAME_MODE = GameMode.TRAINING;
    private static final String SNAKE_NAME = "Eine Sneaky Snake";

    // Set to false if you don't want the game world printed every game tick.
    private static final boolean ANSI_PRINTER_ACTIVE = false;
    private AnsiPrinter ansiPrinter = new AnsiPrinter(ANSI_PRINTER_ACTIVE, true);
    private List<Sense> senses = new LinkedList<>();


    public static void main(String[] args) {
        BrainySnakePlayer brainySnake = new BrainySnakePlayer();

        try {
            ListenableFuture<WebSocketSession> connect = brainySnake.connect();
            connect.get();
        } catch (Exception e) {
            LOGGER.error("Failed to connect to server", e);
            System.exit(1);
        }

        startTheSnake(brainySnake);
    }

    public BrainySnakePlayer() {
        senses.add(new Obvious());
        senses.add(new Caution(0.1));
        senses.add(new Planning(0.25));
        senses.add(new CageFright(1.0, 0.95));
        senses.add(new Fear(3, 0.1));
    }

    @Override
    public void onMapUpdate(MapUpdateEvent mapUpdateEvent) {
        long start = System.nanoTime();
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
        List<java.util.Map<SnakeDirection, Double>> sensePrios = new LinkedList<>();

        for (Sense sense : senses) {
            sensePrios.add(sense.getMovesRanked(mapUtil, liveSnakeIDs));
        }


        Double up = 1.0, down = 1.0, left = 1.0, right = 1.0;
        for (java.util.Map<SnakeDirection, Double> instance : sensePrios) {
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

        System.out.println("X=" + mapUtil.getMyPosition().x + " Y=" + mapUtil.getMyPosition().y);
        System.out.println("MOVE: " + moveToMake);
        System.out.println("Took " + (System.nanoTime() - start) / 1000000 + "ms");
        // Register action here!
    }

    /**
     * The Snake client will continue to run ...
     * : in TRAINING mode, until the single game ends.
     * : in TOURNAMENT mode, until the server tells us its all over.
     */
    private static void startTheSnake(final BrainySnakePlayer simpleSnakePlayer) {
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
