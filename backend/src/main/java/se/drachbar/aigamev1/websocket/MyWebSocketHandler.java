package se.drachbar.aigamev1.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.drachbar.aigamev1.model.GameState;
import se.drachbar.aigamev1.service.GameService;

import java.util.List;

@Slf4j
public class MyWebSocketHandler implements WebSocketHandler {

    private final GameService gameService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        final String sessionId = session.getId(); // Använd WebSocket-sessionens ID som spel-session-ID
        try {
            // Antag att meddelandet är i formatet "command:payload", t.ex. "start:theme" eller "choice:valet"
            final String[] parts = message.split(":", 2);
            final String command = parts[0].trim();
            final String payload = parts.length > 1 ? parts[1].trim() : "";

            return switch (command.toLowerCase()) {
                case "start" -> {
                    // Starta ett nytt spel med ett tema och en spelare för test
                    final List<String> playerIds = List.of("Mattias"); // Testspelare
                    yield gameService.startGame(sessionId, playerIds, payload, session)
                            .flatMap(gameState -> sendGameState(session, gameState)); // Testspelare
                }
                case "choice" ->
                    // Hantera ett val från spelaren
                        gameService.nextTurn(sessionId, "Mattias", payload, session)
                                .flatMap(gameState -> sendGameState(session, gameState));
                default -> session.send(Mono.just(session.textMessage("Okänt kommando: " + command)));
            };
        } catch (Exception e) {
            return session.send(Mono.just(session.textMessage("Fel: " + e.getMessage())));
        }
    }

    private Mono<Void> sendGameState(WebSocketSession session, GameState gameState) {
        final StringBuilder response = new StringBuilder();

        try {
            String choicesJson = objectMapper.writeValueAsString(gameState.getCurrentChoices());
            response.append("<choices>").append(choicesJson).append("</choices>\n");
        } catch (Exception e) {
            log.error("Kunde inte generera JSON för choices", e);
            response.append("<choices>[]</choices>\n");
        }

        response.append("<spel-slut>").append(gameState.isGameOver()).append("</spel-slut>").append("\n");

        return session.send(Mono.just(session.textMessage(response.toString())));
    }
}
