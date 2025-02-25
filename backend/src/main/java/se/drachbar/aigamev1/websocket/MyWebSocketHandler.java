package se.drachbar.aigamev1.websocket;

import dev.langchain4j.data.message.AiMessage;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.drachbar.aigamev1.model.GameState;
import se.drachbar.aigamev1.service.GameService;

import java.util.List;

public class MyWebSocketHandler implements WebSocketHandler {
    private final GameService gameService;

    public MyWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<String> inputMessages = session.receive()
                .map(WebSocketMessage::getPayloadAsText);

        return inputMessages
                .flatMap(message -> processGameMessage(session, message))
                .then();
    }

    private Mono<Void> processGameMessage(WebSocketSession session, String message) {
        String sessionId = session.getId(); // Använd WebSocket-sessionens ID som spel-session-ID
        try {
            // Antag att meddelandet är i formatet "command:payload", t.ex. "start:theme" eller "choice:valet"
            String[] parts = message.split(":", 2);
            String command = parts[0].trim();
            String payload = parts.length > 1 ? parts[1].trim() : "";

            switch (command.toLowerCase()) {
                case "start":
                    // Starta ett nytt spel med ett tema och en spelare för test
                    List<String> playerIds = List.of("player1"); // Testspelare
                    return gameService.startGame(sessionId, playerIds, payload, session)
                            .flatMap(gameState -> sendGameState(session, gameState));

                case "choice":
                    // Hantera ett val från spelaren
                    return gameService.nextTurn(sessionId, "player1", payload, session)
                            .flatMap(gameState -> sendGameState(session, gameState));

                default:
                    return session.send(Mono.just(session.textMessage("Okänt kommando: " + command)));
            }
        } catch (Exception e) {
            return session.send(Mono.just(session.textMessage("Fel: " + e.getMessage())));
        }
    }

    private Mono<Void> sendGameState(WebSocketSession session, GameState gameState) {
        StringBuilder response = new StringBuilder();
        response.append("Runda: ").append(gameState.getCurrentRound()).append("\n");

        // Lägg till senaste historien (AI-svaret)
        gameState.getStoryHistory().stream()
                .filter(AiMessage.class::isInstance)
                .map(AiMessage.class::cast)
                .reduce((first, second) -> second) // Ta det sista AI-meddelandet
                .ifPresent(msg -> response.append("Historia: ").append(msg.text()).append("\n"));

        response.append("Val: ").append(String.join(", ", gameState.getCurrentChoices())).append("\n");
        response.append("Spel slut: ").append(gameState.isGameOver()).append("\n");

        return session.send(Mono.just(session.textMessage(response.toString())));
    }
}
