package com.whiteboard.controller;

import com.whiteboard.model.BoardState;
import com.whiteboard.model.DrawingMessage;
import com.whiteboard.model.Stroke;
import com.whiteboard.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Slf4j
@RequiredArgsConstructor
public class DrawingController {
    
    private final BoardService boardService;

    /**
     * Handle incoming stroke from client
     * Client sends to: /app/stroke/{boardId}
     * Server broadcasts to: /topic/board/{boardId}
     */
    @MessageMapping("/stroke/{boardId}")
    @SendTo("/topic/board/{boardId}")
    public DrawingMessage handleStroke(
            @DestinationVariable String boardId,
            Stroke stroke
    ) {
        log.debug("Received stroke on board {}: userId={}", boardId, stroke.getUserId());
        stroke.setTimestamp(System.currentTimeMillis());
        boardService.addStroke(boardId, stroke);

        DrawingMessage response = new DrawingMessage();
        response.setType("stroke");
        response.setBoardId(boardId);
        response.setStroke(stroke);
        
        return response;
    }

    /**
     * Handle sync-request: client asks for full board state
     * Client sends to: /app/sync/{boardId}
     * Server broadcasts to: /topic/board/{boardId}
     */
    @MessageMapping("/sync/{boardId}")
    @SendTo("/topic/board/{boardId}")
    public DrawingMessage handleSync(
            @DestinationVariable String boardId
    ) {
        log.debug("Sync requested for board: {}", boardId);
        BoardState boardState = boardService.getBoardState(boardId);

        DrawingMessage response = new DrawingMessage();
        response.setType("full-state");
        response.setBoardId(boardId);
        response.setData(boardState);
        
        return response;
    }

    /**
     * Handle action (undo/redo/clear)
     * Client sends to: /app/action/{boardId}
     * Server broadcasts to: /topic/board/{boardId}
     */
    @MessageMapping("/action/{boardId}")
    @SendTo("/topic/board/{boardId}")
    public DrawingMessage handleAction(
            @DestinationVariable String boardId,
            DrawingMessage message
    ) {
        String action = message.getAction();
        String userId = message.getUserId();
        
        log.debug("Action on board {}: action={}, userId={}", boardId, action, userId);

        switch (action) {
            case "undo":
                boardService.undoLastStroke(boardId, userId);
                break;
            case "redo":
                boardService.redoLastStroke(boardId, userId);
                break;
            case "clear":
                boardService.clearBoard(boardId, userId);
                break;
            default:
                log.warn("Unknown action: {}", action);
        }

        message.setType("action");
        message.setBoardId(boardId);
        
        return message;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
