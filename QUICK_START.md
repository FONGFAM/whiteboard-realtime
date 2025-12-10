# 🚀 Quick Start Guide

## 30-Second Setup

### 1️⃣ Build Backend
```bash
cd whiteboard-realtime
mvn clean package
```

### 2️⃣ Run Server
```bash
java -jar target/realtime-whiteboard-1.0.0-SNAPSHOT.jar
```

### 3️⃣ Open Browser
```
http://localhost:8080
```

## 🎯 5-Minute Demo

1. **Open 2 Tabs**: Same URL `http://localhost:8080`
2. **Tab 1**: Draw a line with your mouse
3. **Tab 2**: Watch it appear in real-time! 🎨
4. **Tab 1**: Click "Undo" → stroke disappears on both tabs
5. **Tab 2**: Click "Redo" → stroke reappears

**Done!** ✅ You have realtime collaboration working!

## 📱 Change Board

Tab 2 → Enter Board ID: `board2` → Click "Join Board"
- Now isolated from Tab 1
- Can draw independently
- Try opening Tab 3 with `board2` to see sync!

## 🎨 Customize Drawing

- **Color Picker**: Click colored circle in toolbar
- **Width Slider**: Adjust line thickness (1-20px)
- **Clear**: Remove your strokes only

## 🔧 Troubleshooting

| Problem | Fix |
|---------|-----|
| Port 8080 busy | Change in `application.properties` |
| Can't connect | Check server is running (`curl http://localhost:8080`) |
| No sync happening | Check browser console (F12) for errors |
| Undo not working | Draw at least 1 stroke first |

## 📚 Learn More

- **Full Docs**: See `README.md`
- **Testing Guide**: See `DEMO.md`
- **Architecture**: See `ARCHITECTURE.md`
- **Changes**: See `CHANGELOG.md`

## 🎬 Live Demo Tips

✅ Use 2 monitors if possible (better for demos)
✅ Draw slowly for audience to see strokes
✅ Use bright colors to make strokes visible
✅ Narrate each action: "Now I'm drawing on client A..."
✅ Test Undo/Redo separately to show isolation

---

**Ready? Start building! 🚀**
