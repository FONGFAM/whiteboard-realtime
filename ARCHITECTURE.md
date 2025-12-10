# Architecture & Design

## System Overview

```
Frontend (HTML5 Canvas + JS) ←→ STOMP WebSocket ←→ Backend (Spring Boot)
- Pointer events               (SockJS fallback)    - STOMP Broker
- Canvas rendering            (JSON messages)      - In-memory state
- STOMP client                                      - Service layer
```

---

## Backend

### WebSocketConfig

- Enables SimpleBroker for `/topic/board/{boardId}`
- Sets application destination prefix `/app`
- Registers SockJS endpoint at `/ws-whiteboard`

### Data Models

- **Point**: {x, y}
- **Stroke**: {id, userId, points[], color, width, timestamp}
- **BoardState**: {boardId, strokes[], hiddenStrokeIds}
- **DrawingMessage**: {type, boardId, userId, stroke/action, data}

### BoardService

- Singleton managing Map<String, BoardState>
- Methods: getOrCreateBoard(), addStroke(), undoLastStroke(), redoLastStroke(), clearBoard()
- Undo/redo tracked via hiddenStrokeIds set per board

### DrawingController (Message Handlers)

- `/app/stroke/{boardId}` → addStroke() → broadcasts to `/topic/board/{boardId}`
- `/app/sync/{boardId}` → returns full BoardState
- `/app/action/{boardId}` → handles "undo"/"redo"/"clear" actions

---

## Frontend

### Canvas Engine (canvas.js)

- Tracks allStrokes, currentStroke, hiddenStrokeIds
- Pointer events: down (init) → move (throttled) → up (send)
- Throttles mousemove at 16ms (~60fps) to reduce network traffic
- Local rendering for instant feedback

### STOMP Client (socket.js)

- Connects via SockJS to `/ws-whiteboard`
- Auto-reconnects every 3s on disconnect
- Subscriptions: `/topic/board/{boardId}`
- Message handlers for "stroke", "full-state", "action" types
- Methods: connectWebSocket(), sendStroke(), sendAction(), requestSync()

### UI Controller (app.js)

- joinBoard() - switch board ID and reconnect
- performUndo/Redo/Clear() - send action messages

### Layout (index.html + style.css)

- Header with board ID input and status
- Toolbar: color picker, width slider, Undo/Redo/Clear buttons
- Responsive canvas (full remaining space)

---

## Message Flows

**Draw Stroke:**
Client A → /app/stroke/board1 → Server processes → Broadcasts /topic/board/1 → Client A & B render

**New Client Joins:**
New Client → /app/sync/board1 → Server → full-state message → Render all strokes

**Undo:**
Client A → /app/action/board1 {action:"undo"} → Server hides stroke → Broadcasts → All clients sync

---

## State Management

In-memory ConcurrentHashMap:

```
boards {
  "board1": {
    strokes: [s1, s2, s3, s4],
    hiddenStrokeIds: {"s2"}  // User undo tracking
  }
}
```

Undo/Redo: hiddenStrokeIds tracks which strokes are invisible per board

---

## Performance Optimization

- **Client Throttling**: 16ms throttle on pointer events (60fps max)
- **Message Size**: Stroke ~200-500 bytes, Full-state ~10KB/100 strokes
- **STOMP**: Built-in heartbeat, auto-reconnect, subscription mgmt
- **Thread Safety**: ConcurrentHashMap for shared board state

---

## Current Limitations (MVP)

| Issue          | Current        | v2+ Solution         |
| -------------- | -------------- | -------------------- |
| Persistence    | In-memory only | Database (MongoDB)   |
| Multi-instance | Single JVM     | Redis pub/sub        |
| Authentication | None           | JWT tokens           |
| Rate Limiting  | None           | Token bucket         |
| Tools          | Draw only      | Eraser, shapes, text |

---

## Success Metrics

- Latency: <100ms (achieved)
- Throughput: 100+ concurrent clients
- Stroke delay: <50ms (user perception)
- Memory: ~1MB per 1000 strokes
- CPU: <50% at 100 users

---

## Design Principles

1. **Stateless Controllers** - delegate to service layer
2. **Centralized State** - BoardService manages all boards
3. **Broadcast Pattern** - STOMP SimpleBroker for sync
4. **Client Rendering** - low-latency UX
5. **Throttling** - optimize network usage
