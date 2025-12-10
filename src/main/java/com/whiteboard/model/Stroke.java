package com.whiteboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stroke {
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String userId;        // Who drew this stroke
    private List<Point> points;   // Array of points
    private String color;         // e.g. "#000000"
    private double width;         // Line width
    private long timestamp;       // When stroke was created
}
