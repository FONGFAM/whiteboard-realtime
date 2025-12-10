package com.whiteboard.service;

import com.whiteboard.model.BoardState;
import com.whiteboard.model.Stroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BoardService {

     private static final Logger log = LoggerFactory.getLogger(BoardService.class);
     private final Map<String, BoardState> boards = new HashMap<>();

     public BoardState getOrCreateBoard(String boardId) {
          return boards.computeIfAbsent(boardId, id -> {
               log.info("Creating new board: {}", id);
               BoardState board = new BoardState();
               board.setBoardId(id);
               board.setLastModified(System.currentTimeMillis());
               return board;
          });
     }

     public void addStroke(String boardId, Stroke stroke) {
          BoardState board = getOrCreateBoard(boardId);
          board.getStrokes().add(stroke);
          board.setLastModified(System.currentTimeMillis());
          log.debug("Added stroke {} to board {}", stroke.getId(), boardId);
     }

     public void undoLastStroke(String boardId, String userId) {
          BoardState board = getOrCreateBoard(boardId);

          for (int i = board.getStrokes().size() - 1; i >= 0; i--) {
               Stroke stroke = board.getStrokes().get(i);
               if (stroke.getUserId().equals(userId) && !board.getHiddenStrokeIds().contains(stroke.getId())) {
                    board.getHiddenStrokeIds().add(stroke.getId());
                    log.debug("Undid stroke {} by user {} on board {}", stroke.getId(), userId, boardId);
                    board.setLastModified(System.currentTimeMillis());
                    return;
               }
          }
     }

     public void redoLastStroke(String boardId, String userId) {
          BoardState board = getOrCreateBoard(boardId);

          for (int i = board.getStrokes().size() - 1; i >= 0; i--) {
               Stroke stroke = board.getStrokes().get(i);
               if (stroke.getUserId().equals(userId) && board.getHiddenStrokeIds().contains(stroke.getId())) {
                    board.getHiddenStrokeIds().remove(stroke.getId());
                    log.debug("Redid stroke {} by user {} on board {}", stroke.getId(), userId, boardId);
                    board.setLastModified(System.currentTimeMillis());
                    return;
               }
          }
     }

     public void clearBoard(String boardId, String userId) {
          BoardState board = getOrCreateBoard(boardId);
          board.getStrokes().stream()
                    .filter(s -> s.getUserId().equals(userId))
                    .forEach(s -> board.getHiddenStrokeIds().add(s.getId()));
          log.debug("Cleared all strokes by user {} on board {}", userId, boardId);
          board.setLastModified(System.currentTimeMillis());
     }

     public Collection<Stroke> getVisibleStrokes(String boardId) {
          BoardState board = getOrCreateBoard(boardId);
          return board.getStrokes().stream()
                    .filter(s -> !board.getHiddenStrokeIds().contains(s.getId()))
                    .collect(Collectors.toList());
     }

     public BoardState getBoardState(String boardId) {
          return getOrCreateBoard(boardId);
     }
}
