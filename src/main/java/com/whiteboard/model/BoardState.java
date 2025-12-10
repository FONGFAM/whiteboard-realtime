package com.whiteboard.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoardState {
     private String boardId;
     private List<Stroke> strokes;
     private Set<String> hiddenStrokeIds;
     private long lastModified;

     public BoardState() {
          this.strokes = new ArrayList<>();
          this.hiddenStrokeIds = new HashSet<>();
     }

     public String getBoardId() {
          return boardId;
     }

     public void setBoardId(String boardId) {
          this.boardId = boardId;
     }

     public List<Stroke> getStrokes() {
          return strokes;
     }

     public void setStrokes(List<Stroke> strokes) {
          this.strokes = strokes;
     }

     public Set<String> getHiddenStrokeIds() {
          return hiddenStrokeIds;
     }

     public void setHiddenStrokeIds(Set<String> hiddenStrokeIds) {
          this.hiddenStrokeIds = hiddenStrokeIds;
     }

     public long getLastModified() {
          return lastModified;
     }

     public void setLastModified(long lastModified) {
          this.lastModified = lastModified;
     }
}
