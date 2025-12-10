# 🏗️ Architecture & Design Document

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Realtime Whiteboard System                   │
├─────────────────────┬───────────────────────┬───────────────────┤
│   Frontend (Client) │   Network (WebSocket) │  Backend (Server) │
├─────────────────────┼───────────────────────┼───────────────────┤
│                     │                       │                   │
│ • HTML5 Canvas      │ • STOMP Protocol      │ • Spring Boot     │
│ • Pointer Events    │ • SockJS Fallback     │ • Message Broker  │
│ • STOMP Client      │ • JSON Messages       │ • In-Memory State │
│ • Toolbar UI        │ • Real-time Sync      │ • Service Layer   │
│                     │                       │                   │
└─────────────────────┴───────────────────────┴───────────────────┘
```

---

## Backend Architecture

### 1. WebSocket Configuration (`WebSocketConfig.java`)

```
@Configuration
@EnableWebSocketMessageBroker
├── configureMessageBroker()
│   ├── enableSimpleBroker("/topic", "/queue")
│   └── setApplicationDestinationPrefixes("/app")
│
└── registerStompEndpoints()
    └── addEndpoint("/ws-whiteboard")
        └── withSockJS()
```

**STOMP Message Flow:**
- Client connects: `ws://localhost:8080/ws-whiteboard`
- Client subscribes: `/topic/board/{boardId}`
- Client sends: `/app/stroke/{boardId}` → Server processes → Broadcasts `/topic/board/{boardId}`

### 2. Data Models

```
Point
├── x: double
└── y: double

Stroke
├── id: String (UUID)
├── userId: String
├── points: List<Point>
├── color: String (#RRGGBB)
├── width: double
└── timestamp: long

BoardState
├── boardId: String
├── strokes: List<Stroke>
├── hiddenStrokeIds: Set<String>  [Undo/Redo tracking]
└── lastModified: long

DrawingMessage
├── type: "stroke" | "action" | "full-state" | "sync-request"
├── boardId: String
├── userId: String
├── stroke: Stroke
├── action: "undo" | "redo" | "clear"
└── data: Object (BoardState or any)
```

### 3. Service Layer (`BoardService`)

```
BoardService (Singleton)
│
├── Map<String, BoardState> boards
│
├── getOrCreateBoard(boardId)
│   └── Create BoardState if not exists
│
├── addStroke(boardId, stroke)
│   └── Append stroke to board.strokes
│
├── undoLastStroke(boardId, userId)
│   └── Find last visible stroke by user → add to hiddenStrokeIds
│
├── redoLastStroke(boardId, userId)
│   └── Find last hidden stroke by user → remove from hiddenStrokeIds
│
├── clearBoard(boardId, userId)
│   └── Hide all strokes by user
│
└── getVisibleStrokes(boardId)
    └── Filter: strokes not in hiddenStrokeIds
```

### 4. Message Handler (`DrawingController`)

```
DrawingController
│
├── @MessageMapping("/stroke/{boardId}")
│   @SendTo("/topic/board/{boardId}")
│   └── handleStroke(boardId, stroke)
│       ├── boardService.addStroke()
│       ├── Broadcast to /topic/board/{boardId}
│       └── Response: {type:"stroke", stroke:...}
│
├── @MessageMapping("/sync/{boardId}")
│   @SendTo("/topic/board/{boardId}")
│   └── handleSync(boardId)
│       ├── boardService.getBoardState()
│       ├── Broadcast full state
│       └── Response: {type:"full-state", data:BoardState}
│
└── @MessageMapping("/action/{boardId}")
    @SendTo("/topic/board/{boardId}")
    └── handleAction(boardId, message)
        ├── Switch action → call service
        ├── Broadcast to all subscribers
        └── Response: {type:"action", action:...}
```

---

## Frontend Architecture

### 1. Canvas Drawing Engine (`canvas.js`)

```
Canvas State
├── allStrokes: Stroke[]         [All strokes ever drawn]
├── hiddenStrokeIds: Set         [For undo/redo]
├── currentStroke: Stroke        [Being drawn]
│   ├── points: []
│   ├── color
│   ├── width
│   └── userId
│
└── Rendering Loop:
    ├── Pointer Down
    │   ├── Initialize currentStroke
    │   └── Clear points
    │
    ├── Pointer Move (throttled ~60fps)
    │   ├── Accumulate points
    │   └── drawLine() locally
    │
    └── Pointer Up
        ├── Finalize currentStroke
        └── sendStroke(currentStroke)
```

**Throttling Strategy:**
```javascript
lastMoveTime = 0
const THROTTLE = 16ms (60fps)

pointermove event:
    if (now - lastMoveTime < 16ms) return
    lastMoveTime = now
    // Process move
```

### 2. STOMP Client (`socket.js`)

```
StompClient
│
├── connectWebSocket()
│   └── new SockJS('/ws-whiteboard')
│       └── stompClient = Stomp.over(socket)
│           └── stompClient.connect()
│               ├── onConnected()
│               │   ├── Subscribe: /topic/board/{boardId}
│               │   └── requestSync()
│               │
│               └── onError()
│                   └── Retry after 3s
│
├── onMessageReceived(message)
│   ├── Parse JSON
│   └── Switch message.type:
│       ├── "stroke" → handleRemoteStroke()
│       ├── "full-state" → handleFullState()
│       └── "action" → handleRemoteAction()
│
├── sendStroke(stroke)
│   └── stompClient.send("/app/stroke/{boardId}", stroke)
│
├── sendAction(action)
│   └── stompClient.send("/app/action/{boardId}", message)
│
└── requestSync()
    └── stompClient.send("/app/sync/{boardId}", "")
```

### 3. UI Controller (`app.js`)

```
App Functions
│
├── joinBoard()
│   ├── Get boardId from input
│   ├── Clear local state
│   └── Reconnect STOMP to new board
│
├── performUndo()
│   └── sendAction("undo")
│
├── performRedo()
│   └── sendAction("redo")
│
└── clearBoard()
    └── sendAction("clear")
```

### 4. UI Layout (`index.html` + `style.css`)

```
Header (Gradient)
├── Title: "🎨 Realtime Whiteboard"
└── Controls:
    ├── Board ID Input
    ├── Join Button
    └── Status Indicator

Toolbar
├── Color Picker
├── Width Slider (1-20px)
└── Buttons:
    ├── Undo
    ├── Redo
    └── Clear

Canvas
├── Responsive (flex: 1)
├── Cursor: crosshair
└── Background: white
```

---

## Message Flow Diagrams

### Flow 1: User Draws Stroke

```
[Client A]                [Server]                 [Client B]
    │                         │                         │
    │─ Pointer Down ──────────┤                         │
    │─ Accumulate Points      │                         │
    │─ Draw Locally           │                         │
    │                         │                         │
    │─ Pointer Up ────────────┤                         │
    │                         │                         │
    │ POST /app/stroke/board1 │                         │
    ├────────────────────────>│                         │
    │                         │ Handle Stroke          │
    │                         │ Save in-memory         │
    │                         │                         │
    │                         │ Broadcast /topic/board/1
    │                         ├────────────────────────>│
    │                         │                         │ Receive
    │                         │                         │ drawStroke()
    │                         │                         │ redraw()
    │                         │
    │<────────────────────────┤
    │  ACK (echo broadcast)   │
    │ drawStroke()            │
```

### Flow 2: Client Joins Mid-Session

```
[New Client]             [Server]                 [Active Client]
    │                        │                          │
    │─ WebSocket Connect ────┤                          │
    │ /ws-whiteboard         │                          │
    │<───────── OK ──────────┤                          │
    │                        │                          │
    │─ Subscribe /topic/board/1                         │
    ├──────────────────────> │                          │
    │                        │                          │
    │─ Send /app/sync/board/1                           │
    ├──────────────────────> │                          │
    │                        │ Gather BoardState       │
    │                        │ (all strokes, hidden)   │
    │                        │                          │
    │<────────────────────────┤                          │
    │  full-state message     │                          │
    │ (render all strokes)    │                          │
    │                         │ (Active client also     │
    │                         │  receives in broadcast) │
    │ Now synced!             │                          │
```

### Flow 3: User Performs Undo

```
[Client A]               [Server]                  [Client B]
    │                        │                          │
    │─ Click Undo ───────────┤                          │
    │                        │                          │
    │ /app/action/board1     │                          │
    │ {action:"undo"}        │                          │
    ├──────────────────────> │                          │
    │                        │ Find last stroke        │
    │                        │ by userId (not hidden)  │
    │                        │ Add to hiddenIds        │
    │                        │                          │
    │                        │ Broadcast /topic/board/1
    │                        │ {action:"undo"}         │
    │                        ├────────────────────────>│
    │                        │                          │
    │<────────────────────────┤                          │ Receive
    │  ACK                    │                          │ requestSync()
    │ requestSync() to refetch│ Later: full-state       │ (optional)
    │ hidden state properly   ├────────────────────────>│
    │                        │                          │ Update UI
```

---

## Data Persistence & State Management

### In-Memory Strategy (MVP)

```
BoardService.boards
├── Map<String, BoardState>
│
├── board1:
│   ├── strokes: [stroke1, stroke2, stroke3, stroke4]
│   └── hiddenStrokeIds: {"stroke2"} (user1 undone)
│
├── board2:
│   ├── strokes: [strokeA, strokeB]
│   └── hiddenStrokeIds: {}
│
└── board3:
    ├── strokes: []
    └── hiddenStrokeIds: {}
```

**Undo/Redo Logic:**

```
Stroke: [1, 2, 3, 4]
Hidden: []

User undos → Hidden: [4]         (stroke 4 hidden)
User undos → Hidden: [4, 3]      (stroke 3 hidden)
User redos → Hidden: [4]         (stroke 3 visible again)
User redos → Hidden: []          (stroke 4 visible again)
```

---

## Concurrency & Thread Safety

- **Thread Model**: Spring STOMP uses thread pool for async message handling
- **Shared State**: `BoardService.boards` Map is accessed by multiple threads
- **Solution**: Use `ConcurrentHashMap` (can upgrade in v2)
  
```java
private final Map<String, BoardState> boards = 
    new ConcurrentHashMap<>();  // Better for multi-thread
```

---

## Network Optimization

### 1. Throttling (Client-side)

```javascript
// Reduce WebSocket traffic during fast mouse movement
pointermove: throttle 16ms (~60fps)
Result: ~60 messages/sec per user instead of 1000+
```

### 2. Message Size

```
Stroke message size: ~200-500 bytes (points + metadata)
Full-state on join: ~10KB (for 100 strokes) - acceptable
```

### 3. STOMP Advantages

- Built-in heartbeat (keep-alive)
- Automatic reconnection (SockJS fallback)
- Subscription management
- Message headers

---

## Error Handling

### Client-side:

```javascript
try {
    onMessageReceived(message)
} catch (error) {
    console.error("Error processing message:", error)
}

Connection lost:
    → updateStatus("Disconnected", false)
    → Retry every 3 seconds
```

### Server-side:

```java
@ExceptionHandler
handleException() {
    log.error(...)
    // Return error response or broadcast to clients
}
```

---

## Scaling Considerations (for v2+)

| Component | Current | Bottleneck | Solution |
|-----------|---------|-----------|----------|
| State Storage | In-Memory | Max ~1GB | Database (MongoDB/Postgres) |
| Single Instance | 1 JVM | Max 10K connections | Multi-instance + Redis pub/sub |
| Broadcasting | SimpleBroker | Single broker | RabbitMQ/Kafka for real pub/sub |
| User Identity | UUID | No auth | JWT tokens |
| Rate Limiting | None | Spam/DOS | Token bucket per client |

---

## Security Considerations

### Current (MVP):
- ❌ No authentication
- ❌ No authorization
- ⚠️ Any user can undo others' work if we didn't implement per-user tracking

### Planned (v2+):
- ✅ JWT authentication on WebSocket handshake
- ✅ User roles (owner/editor/viewer)
- ✅ Rate limiting
- ✅ Input validation
- ✅ CORS configuration

---

## Performance Metrics (Baseline)

| Metric | Target | Notes |
|--------|--------|-------|
| Latency | <100ms | Local network |
| Throughput | 100+ concurrent clients | With in-memory state |
| Stroke Draw Delay | <50ms | User perception |
| Memory per Board | ~1MB per 1000 strokes | Rough estimate |
| Server CPU | <50% | At 100 concurrent users |

---

## Summary

The Realtime Whiteboard follows a classic **client-server STOMP architecture** with:

1. **Stateless Controllers** responding to WebSocket messages
2. **Centralized Service** managing board state
3. **Broadcast Pattern** for real-time synchronization
4. **Client-side Rendering** for low-latency UX
5. **Throttling Strategy** to optimize network usage

This design prioritizes:
- ✅ Real-time responsiveness
- ✅ Simplicity (MVP)
- ✅ Scalability roadmap (v2+)
- ⚠️ No persistence (future enhancement)
