package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;

import org.mindrot.jbcrypt.BCrypt;



public class MySqlDataAccess implements DataAccess {
    // Gson converts Java objects to JSON strings and back
    private static final Gson GSON = new Gson();
    //set up the database immediately
    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }


    // Makes sure the database and the 3 tables exist before we use it
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            var statements = new String[]{
                    // users table stores username, password, and email
                    """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(256) PRIMARY KEY,
                    password VARCHAR(256) NOT NULL,
                    email VARCHAR(256) NOT NULL
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(256),
                    blackUsername VARCHAR(256),
                    gameName VARCHAR(256) NOT NULL,
                    game TEXT NOT NULL
                )
                """,
                    """
                CREATE TABLE IF NOT EXISTS auth (
                    authToken VARCHAR(256) PRIMARY KEY,
                    username VARCHAR(256) NOT NULL
                )
                """
            };
            // Run each table statement
            for (var statement : statements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure database: " + e.getMessage());
        }
    }

    //USER

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // validate before even touching the database
        if (user == null || user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Bad Request");
        }
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        var sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("User already exists");
            }
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }
    //returns null if not found
    @Override
    public UserData getUser(String username) throws DataAccessException {
        // cant look up a null username
        if (username == null) {
            throw new DataAccessException("Bad Request");
        }
        var sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        var sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, null); // no white player yet
            ps.setString(2, null);// no black player yet
            ps.setString(3, gameName);
            ps.setString(4, GSON.toJson(new ChessGame()));  // empty game state
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);  // returns the auto-generated gameID
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
        throw new DataAccessException("Error creating game: no ID obtained");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            GSON.fromJson(rs.getString("game"), ChessGame.class)  // String -> ChessGame
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        var sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        GSON.fromJson(rs.getString("game"), ChessGame.class)  // String -> ChessGame
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, GSON.toJson(game.game()));
            ps.setInt(5, game.gameID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }


    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage());
        }
    }

    //  CLEAR

    @Override
    public void clear() throws DataAccessException {
        var sql = "TRUNCATE TABLE ";
        try (var conn = DatabaseManager.getConnection()) {
            // Order matters
            for (var table : new String[]{"auth", "games", "users"}) {
                try (var ps = conn.prepareStatement(sql + table)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing database: " + e.getMessage());
        }
    }
}