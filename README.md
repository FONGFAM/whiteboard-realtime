# Realtime Whiteboard

Multi-user drawing app with Spring Boot WebSocket backend and HTML5 Canvas frontend.

## Quick Start

**Prerequisites**: JDK 17+, Maven 3.8+

**Run**:

```bash
mvn clean package
java -jar target/realtime-whiteboard-1.0.0-SNAPSHOT.jar
```

Open `http://localhost:8080` and start drawing. Open a second tab to see realtime sync.

## Features

- Realtime stroke sync between clients
- Room-based boards (join by ID)
- Per-user Undo/Redo
- Color picker & line width slider
- Full state sync on client join

## Architecture

- **Backend**: Spring Boot 3.2 + STOMP WebSocket + in-memory state
- **Frontend**: HTML5 Canvas + Vanilla JS + SockJS
- **Messages**: JSON over STOMP (`/app/stroke/{boardId}`, `/app/action/{boardId}`, `/app/sync/{boardId}`)

## UI Controls

```
| Control         | Function             |
| --------------- | -------------------- |
| Board ID        | Join different rooms |
| Color Picker    | Drawing color        |
| Width Slider    | Line thickness       |
| Undo/Redo/Clear | Canvas operations    |
```

## Testing

1. Open 2 browser tabs to `http://localhost:8080`
2. Use same Board ID (default: `board1`)
3. Draw on tab 1, see it appear on tab 2 instantly
4. Test Undo/Redo/Clear buttons
5. Change Board ID to join different rooms

## Troubleshooting

```
| Issue                 | Solution                                      |
| --------------------- | --------------------------------------------- |
| WebSocket fails       | Ensure backend running on port 8080           |
| Strokes not syncing   | Check browser console; verify Board IDs match |
| Undo/Redo not working | Both clients must be on same Board ID         |

## Future Enhancements

- Database persistence (MongoDB/PostgreSQL)
- Redis pub/sub for multi-instance
- JWT authentication
- Eraser & shapes tools
- Recording & playback
- Export as PNG/PDF
```

## License

MIT
