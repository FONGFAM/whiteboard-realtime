package com.whiteboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardState {
    private String boardId;
    private List<Stroke> strokes = new ArrayList<>();  // All strokes ever drawn
    private Set<String> hiddenStrokeIds = new HashSet<>();  // Undo/cleared strokes
    private long lastModified;
}
