package com.whiteboard.controller;

import com.whiteboard.model.BoardState;
import com.whiteboard.model.DrawingMessage;
import com.whiteboard.model.Stroke;
import com.whiteboard.service.BoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class DrawingController {

     private static final Logger log = LoggerFactory.getLogger(DrawingController.class);
     private final BoardService boardService;

     public DrawingController(BoardService boardService) {
          this.boardService = boardService;
     }

     @MessageMapping("/stroke/{boardId}")
     @SendTo("/topic/board/{boardId}")
     public DrawingMessage handleStroke(
               @DestinationVariable String boardId,
               Stroke stroke) {
          log.debug("Received stroke on board {}: userId={}", boardId, stroke.getUserId());
          stroke.setTimestamp(System.currentTimeMillis());
          boardService.addStroke(boardId, stroke);

          DrawingMessage response = new DrawingMessage();
          response.setType("stroke");
          response.setBoardId(boardId);
          response.setStroke(stroke);

          return response;
     }

     @MessageMapping("/sync/{boardId}")
     @SendTo("/topic/board/{boardId}")
     public DrawingMessage handleSync(
               @DestinationVariable String boardId) {
          log.debug("Sync requested for board: {}", boardId);
          BoardState boardState = boardService.getBoardState(boardId);

          DrawingMessage response = new DrawingMessage();
          response.setType("full-state");
          response.setBoardId(boardId);
          response.setData(boardState);

          return response;
     }

     @MessageMapping("/action/{boardId}")
     @SendTo("/topic/board/{boardId}")
     public DrawingMessage handleAction(
               @DestinationVariable String boardId,
               DrawingMessage message) {
          String action = message.getAction();
          String userId = message.getUserId();

          log.debug("Action on board {}: action={}, userId={}", boardId, action, userId);

          if (action != null) {
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
          }

          message.setType("action");
          message.setBoardId(boardId);

          return message;
     }

     @GetMapping("/health")
     public String health() {
          return "OK";
     }
}
