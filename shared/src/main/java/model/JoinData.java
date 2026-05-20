package model;

import chess.ChessGame;

public record JoinData(int gameID, ChessGame.TeamColor teamColor) {}
