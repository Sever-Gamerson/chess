package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {
    private final GameData game;

    //all other messages will come through here
    public LoadGameMessage(GameData game) {

        super(ServerMessageType.LOAD_GAME);

        this.game = game;
    }

    public GameData getGame() {
        return game;
    }
}