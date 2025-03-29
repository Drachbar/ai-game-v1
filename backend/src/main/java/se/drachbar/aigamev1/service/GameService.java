package se.drachbar.aigamev1.service;

import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                    final String[] choices = choiceAgent.generateChoices(state.getStoryHistory(), playerIds.getFirst());
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
                .map(newMessages -> {
                    newMessages.forEach(state::addToHistory);

                    String updatedStory = newMessages.stream()
                            .filter(AiMessage.class::isInstance)
                            .map(AiMessage.class::cast)
                            .reduce((_, second) -> second)
                            .map(AiMessage::text)
                            .orElseThrow(() -> new IllegalStateException("Inget AiMessage hittades"));

                    String[] newChoices = choiceAgent.generateChoices(state.getStoryHistory(), playerId);
                    System.out.println("Loopa igenom start");
                    for (String choice : newChoices) {
                        System.out.println(choice);
                    }
                    System.out.println("Loopa igenom slut");
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
