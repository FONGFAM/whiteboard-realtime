package com.whiteboard.service;

import com.whiteboard.model.BoardState;
import com.whiteboard.model.Stroke;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BoardService {
    private final Map<String, BoardState> boards = new HashMap<>();

    public BoardState getOrCreateBoard(String boardId) {
        return boards.computeIfAbsent(boardId, id -> {
            log.info("Creating new board: {}", id);
            return BoardState.builder()
                    .boardId(id)
                    .lastModified(System.currentTimeMillis())
                    .build();
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
        
        // Find the last stroke by this user that is not hidden
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
        
        // Find the last hidden stroke by this user, in reverse order
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
        // Hide all strokes by this user
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
