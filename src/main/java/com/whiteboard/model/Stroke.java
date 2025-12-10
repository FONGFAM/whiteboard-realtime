package com.whiteboard.model;

import java.util.List;
import java.util.UUID;

public class Stroke {
     private String id;
     private String userId;
     private List<Point> points;
     private String color;
     private double width;
     private long timestamp;

     public Stroke() {
          this.id = UUID.randomUUID().toString();
     }

     public String getId() {
          return id;
     }

     public void setId(String id) {
          this.id = id;
     }

     public String getUserId() {
          return userId;
     }

     public void setUserId(String userId) {
          this.userId = userId;
     }

     public List<Point> getPoints() {
          return points;
     }

     public void setPoints(List<Point> points) {
          this.points = points;
     }

     public String getColor() {
          return color;
     }

     public void setColor(String color) {
          this.color = color;
     }

     public double getWidth() {
          return width;
     }

     public void setWidth(double width) {
          this.width = width;
     }

     public long getTimestamp() {
          return timestamp;
     }

     public void setTimestamp(long timestamp) {
          this.timestamp = timestamp;
     }
}
