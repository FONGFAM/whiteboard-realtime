# 🎨 Realtime Whiteboard

A multi-user realtime drawing application built with **Spring Boot WebSocket (STOMP)** backend and **HTML5 Canvas + JavaScript** frontend.

## 📋 Features

### MVP (v1)
- ✅ Realtime stroke sync between multiple clients
- ✅ Room-based boards (join by board ID)
- ✅ Per-user Undo/Redo
- ✅ Toolbar: color picker, line width slider, clear button
- ✅ Full state sync on client join
- ✅ In-memory board state management

## 🏗️ Architecture

```
┌─ Backend (Java/Spring Boot)
│  ├─ WebSocket Server (STOMP + SockJS)
│  ├─ BoardService (in-memory state mgmt)
│  └─ DrawingController (message handler)
│
└─ Frontend (HTML5 Canvas + JS)
   ├─ Canvas drawing engine
   ├─ STOMP client
   └─ UI (color picker, toolbar)
```

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2 + Spring WebSocket (STOMP) + Maven
- **Frontend**: HTML5 Canvas, Vanilla JavaScript, SockJS, StompJS
- **Java Version**: JDK 17+

## 📦 Project Structure

```
whiteboard-realtime/
├── src/main/java/com/whiteboard/
│   ├── config/
│   │   └── WebSocketConfig.java      # STOMP configuration
│   ├── controller/
│   │   └── DrawingController.java    # Message handlers
│   ├── model/
│   │   ├── Stroke.java
│   │   ├── Point.java
│   │   ├── BoardState.java
│   │   └── DrawingMessage.java
│   ├── service/
│   │   └── BoardService.java         # Business logic
│   └── WhiteboardApplication.java    # Main class
│
├── src/main/resources/
│   ├── static/
│   │   ├── index.html
│   │   ├── css/style.css
│   │   └── js/
│   │       ├── app.js
│   │       ├── canvas.js
│   │       └── socket.js
│   └── application.properties
│
└── pom.xml
```

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.8+
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Installation & Running

#### 1. Clone & Navigate
```bash
cd whiteboard-realtime
```

#### 2. Build Backend
```bash
mvn clean package
```

#### 3. Run Backend Server
```bash
mvn spring-boot:run
# OR
java -jar target/realtime-whiteboard-1.0.0-SNAPSHOT.jar
```

Server will start at `http://localhost:8080`

#### 4. Open Frontend
- Open browser to `http://localhost:8080`
- Or open `index.html` directly if serving statically

### 5. Test Realtime Sync

**Step 1**: Open 2 browser tabs/windows to `http://localhost:8080`

**Step 2**: Make sure both tabs have same Board ID (default: `board1`)

**Step 3**: Draw on one tab → see drawing appear on the other tab instantly!

**Step 4**: Test Undo/Redo/Clear buttons

**Step 5**: Try changing Board ID to join different rooms

## 📡 Message Protocol

### WebSocket Endpoints

**Connection**: `ws://localhost:8080/ws-whiteboard`

### Message Types

#### 1. Stroke (Draw)
```javascript
Client sends to: /app/stroke/{boardId}
Server broadcasts to: /topic/board/{boardId}

{
  "type": "stroke",
  "userId": "user_abc123",
  "points": [
    {"x": 10, "y": 20},
    {"x": 15, "y": 25},
    ...
  ],
  "color": "#000000",
  "width": 3
}
```

#### 2. Sync Request
```javascript
Client sends to: /app/sync/{boardId}
Server responds on: /topic/board/{boardId}

Server returns: {
  "type": "full-state",
  "data": {
    "boardId": "board1",
    "strokes": [...],
    "hiddenStrokeIds": [...]
  }
}
```

#### 3. Action (Undo/Redo/Clear)
```javascript
Client sends to: /app/action/{boardId}

{
  "type": "action",
  "userId": "user_abc123",
  "action": "undo" | "redo" | "clear"
}
```

## 🎮 UI Controls

- **Board ID Input**: Join a specific board (default: `board1`)
- **Color Picker**: Select drawing color
- **Width Slider**: Adjust line thickness (1-20px)
- **Undo Button**: Undo last stroke
- **Redo Button**: Redo undone stroke
- **Clear Button**: Clear all your strokes from board
- **Canvas**: Draw by clicking and dragging

## 🧪 Testing

### Manual Testing Checklist

- [ ] Start backend server successfully
- [ ] Open frontend in browser
- [ ] Draw on canvas (strokes appear locally)
- [ ] Open second browser tab/window
- [ ] Strokes sync between tabs in real-time
- [ ] Click Undo → previous stroke disappears on all clients
- [ ] Click Redo → stroke reappears on all clients
- [ ] Change board ID → join different room
- [ ] New client joins → receives full board state
- [ ] Colors and line widths apply correctly
- [ ] Clear button works

### Load Testing

Use browser DevTools Network tab to monitor WebSocket messages or create a script to simulate multiple clients.

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8080                              # Server port
logging.level.com.whiteboard=DEBUG            # Debug logging
```

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| "Failed to connect to WebSocket" | Ensure backend is running on port 8080 |
| Strokes not syncing | Check browser console for errors; verify board IDs match |
| Undo/Redo not working | Ensure both clients are connected to same board |
| Server crashes with StackOverflow | Check for infinite loops in message handling |

## 📈 Performance Notes

- **Stroke Throttling**: Client samples pointer events at ~60fps to reduce network traffic
- **In-Memory Storage**: Board state stored in HashMap (no persistence yet)
- **Broadcast**: STOMP broker ensures all subscribers receive updates

## 🎯 Future Enhancements (v2+)

- [ ] Persist board state to database (MongoDB/Postgres)
- [ ] Redis pub/sub for multi-instance deployment
- [ ] JWT authentication
- [ ] Rate limiting per client
- [ ] Eraser tool
- [ ] Shapes (rectangle, circle)
- [ ] Text tool
- [ ] Recording & playback
- [ ] User permissions (owner/editor/viewer)
- [ ] Export as PNG/PDF
- [ ] Real-time chat integration

## 📝 License

MIT License

## 👥 Authors

- Whiteboard Project Team

---

## 📞 Support

For issues or questions, refer to the troubleshooting section or create an issue.

**Happy Drawing! 🎨**
