package se.drachbar.aigamev1.model;

import dev.langchain4j.data.message.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GameState {
    private List<ChatMessage> storyHistory;
    private String[] currentChoices;
    private int currentRound;
    private Map<String, PlayerStatus> playerStatuses;
    private boolean gameOver;
    public GameState() {
        this.storyHistory = new ArrayList<>();
        this.currentChoices = new String[0];
        this.currentRound = 1;
        this.playerStatuses = new HashMap<>();
        this.gameOver = false;
    }
    public void addToHistory(ChatMessage message) {
        this.storyHistory.add(message);
    }

    public void addPlayer(String playerId) {
        playerStatuses.putIfAbsent(playerId, new PlayerStatus());
    }

    public void killPlayer(String playerId) {
        PlayerStatus status = playerStatuses.get(playerId);
        if (status != null) {
            status.setAlive(false);
        }
    }

    public boolean isPlayerAlive(String playerId) {
        PlayerStatus status = playerStatuses.get(playerId);
        return status != null && status.isAlive();
    }

    public void checkGameOver() {
        if (playerStatuses.values().stream().noneMatch(PlayerStatus::isAlive)) {
            this.gameOver = true;
        }
    }

    @Data
    public static class PlayerStatus {
        private boolean alive;
        private List<String> choicesMade;      // Lista över val spelaren har gjort
        private List<String[]> offeredChoices; // Lista över valalternativ spelaren har fått

        public PlayerStatus() {
            this.alive = true;
            this.choicesMade = new ArrayList<>();
            this.offeredChoices = new ArrayList<>();
        }

        // Lägg till ett val som spelaren har gjort
        public void addChoiceMade(String choice) {
            this.choicesMade.add(choice);
        }

        // Lägg till en uppsättning valalternativ som erbjudits
        public void addOfferedChoices(String[] choices) {
            this.offeredChoices.add(choices.clone()); // Clone för att undvika referensproblem
        }
    }
}
