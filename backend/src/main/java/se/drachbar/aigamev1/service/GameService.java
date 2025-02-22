package se.drachbar.aigamev1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import se.drachbar.aigamev1.aiAgents.ChoiceAgent;
import se.drachbar.aigamev1.aiAgents.GameStoryAgent;
import se.drachbar.aigamev1.aiAgents.StartGameStoryAgent;
import se.drachbar.aigamev1.model.GameState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {
    private final StartGameStoryAgent startGameStoryAgent;
    private final GameStoryAgent gameStoryAgent;
    private final ChoiceAgent choiceAgent;
    private final Map<String, GameState> gameSessions = new ConcurrentHashMap<>();

    public Mono<GameState> startGame(String sessionId, List<String> playerIds, String theme, WebSocketSession session) {
        GameState state = new GameState();
        playerIds.forEach(state::addPlayer); // Lägg till alla spelare
        gameSessions.put(sessionId, state);

        return startGameStoryAgent.startStory(state.getStoryHistory(), playerIds, theme, session)
                .map(initialStory -> {
                    String[] choices = choiceAgent.generateChoices(state.getStoryHistory());
                    state.setCurrentChoices(choices);
                    playerIds.forEach(playerId -> state.getPlayerStatuses().get(playerId).addOfferedChoices(choices));
                    return state;
                });
    }

    public Mono<GameState> nextTurn(String sessionId, String playerId, String playerChoice, WebSocketSession session) {
        GameState state = gameSessions.get(sessionId);
        if (state == null || !state.isPlayerAlive(playerId) || state.isGameOver()) {
            return Mono.just(state);
        }

        return gameStoryAgent.processQuery(state.getStoryHistory(), playerChoice, state.getCurrentRound(), session)
                .map(updatedStory -> {
                    String[] newChoices = choiceAgent.generateChoices(state.getStoryHistory());
                    GameState.PlayerStatus playerStatus = state.getPlayerStatuses().get(playerId);

                    playerStatus.addChoiceMade(playerChoice);
                    playerStatus.addOfferedChoices(newChoices);

                    state.setCurrentChoices(newChoices);
                    state.setCurrentRound(state.getCurrentRound() + 1);

                    if (updatedStory.contains("du dör") || updatedStory.contains("du förlorar")) {
                        state.killPlayer(playerId);
                    }
                    if (updatedStory.contains("[GAME OVER]") || updatedStory.contains("historien når sitt slut")) {
                        state.setGameOver(true);
                    }

                    state.checkGameOver();
                    return state;
                });
    }

    public GameState getGameState(String sessionId) {
        return gameSessions.get(sessionId);
    }
}
