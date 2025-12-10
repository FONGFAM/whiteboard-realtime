package com.whiteboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawingMessage {
    private String type;           // "stroke", "action", "sync-request", etc.
    private String boardId;
    private String userId;
    private Stroke stroke;         // For type="stroke"
    private String action;         // For type="action": "undo", "redo", "clear"
    private Object data;           // Additional data
}
