package com.whiteboard.model;

public class DrawingMessage {
     private String type;
     private String boardId;
     private String userId;
     private Stroke stroke;
     private String action;
     private Object data;

     public DrawingMessage() {
     }

     public String getType() {
          return type;
     }

     public void setType(String type) {
          this.type = type;
     }

     public String getBoardId() {
          return boardId;
     }

     public void setBoardId(String boardId) {
          this.boardId = boardId;
     }

     public String getUserId() {
          return userId;
     }

     public void setUserId(String userId) {
          this.userId = userId;
     }

     public Stroke getStroke() {
          return stroke;
     }

     public void setStroke(Stroke stroke) {
          this.stroke = stroke;
     }

     public String getAction() {
          return action;
     }

     public void setAction(String action) {
          this.action = action;
     }

     public Object getData() {
          return data;
     }

     public void setData(Object data) {
          this.data = data;
     }
}
