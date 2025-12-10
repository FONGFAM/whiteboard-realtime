# 🎬 Demo Guide - Realtime Whiteboard

## Chuẩn bị Demo (Testing Steps)

### 1. Khởi động Backend Server

```bash
cd whiteboard-realtime
java -jar target/realtime-whiteboard-1.0.0-SNAPSHOT.jar
```

**Output mong đợi:**
```
Started WhiteboardApplication in X.XXX seconds
Tomcat started on port 8080 (http) with context path ''
SimpleBrokerMessageHandler started
```

### 2. Mở 2 Browser Windows/Tabs

#### **Window/Tab 1:**
- Mở: `http://localhost:8080`
- Sẽ thấy giao diện Whiteboard
- Status sẽ hiển thị "Connected"

#### **Window/Tab 2:**
- Mở cùng: `http://localhost:8080`
- Cùng Board ID (mặc định: `board1`)
- Status sẽ hiển thị "Connected"

---

## 🎯 Demo Scenario 1: Realtime Stroke Sync

**Mục tiêu:** Chứng minh vẽ trên client này xuất hiện ngay tức thì trên client khác

### Steps:

1. **Tab 1**: Vẽ một đường thẳng (kéo chuột từ trái sang phải)
   - Nét xuất hiện trên Tab 1 ngay lập tức
   - Nhìn sang Tab 2 → **Nét cũng xuất hiện ở đó!** ✅

2. **Tab 2**: Chọn màu xanh lam từ Color Picker
   - Vẽ một hình tròn
   - **Nét xanh xuất hiện trên Tab 1 ngay tức thì!** ✅

3. **Tab 1**: Thay đổi Width slider thành 15px
   - Vẽ một nét dày
   - **Nét dày xuất hiện trên Tab 2 với đúng kích thước!** ✅

**Kết luận:** ✅ Realtime sync hoạt động hoàn hảo

---

## 🎯 Demo Scenario 2: Undo/Redo Per-User

**Mục tiêu:** Chứng minh Undo/Redo chỉ ảnh hưởng nét của user hiện tại

### Steps:

1. **Setup:** Vẽ 2 nét khác nhau:
   - Tab 1: Vẽ nét đỏ (user 1)
   - Tab 2: Vẽ nét xanh (user 2)
   - Cả 2 nét đều xuất hiện trên cả 2 tab

2. **Tab 1**: Click nút **Undo**
   - Nét đỏ (của user 1) biến mất trên cả 2 tab
   - **Nét xanh (của user 2) vẫn còn đó!** ✅

3. **Tab 2**: Click nút **Undo**
   - Nét xanh (của user 2) biến mất
   - Tab 1 cũng không còn nét xanh nữa ✅

4. **Tab 1**: Click nút **Redo**
   - Nét đỏ tái xuất hiện trên cả 2 tab ✅

5. **Tab 2**: Click nút **Redo**
   - Nét xanh tái xuất hiện ✅

**Kết luận:** ✅ Undo/Redo per-user hoạt động đúng

---

## 🎯 Demo Scenario 3: Full State Sync on Join

**Mục tiêu:** Client mới join sẽ nhận toàn bộ state trước đó

### Steps:

1. **Tab 1**: Vẽ 3-4 nét (các màu/kích thước khác nhau)
   - Tất cả nét hiển thị đúng

2. **Mở Tab 3 mới** (hoặc cửa sổ mới):
   - Nhập Board ID: `board1` (mặc định)
   - Click **Join Board**
   - **Tab 3 sẽ ngay tức thì hiển thị toàn bộ 3-4 nét từ Tab 1!** ✅

3. **Tab 1**: Vẽ thêm nét mới
   - **Nét mới xuất hiện trên Tab 3 ngay lập tức!** ✅

**Kết luận:** ✅ Full state sync on join hoạt động

---

## 🎯 Demo Scenario 4: Multi-Board Support

**Mục tiêu:** Các board khác nhau độc lập với nhau

### Steps:

1. **Tab 1**: Giữ nguyên Board ID = `board1`
   - Vẽ 2 nét

2. **Tab 2**: 
   - Thay đổi Board ID thành `board2`
   - Click **Join Board**
   - **Canvas trắng sạch (không có nét từ board1)!** ✅

3. **Tab 2**: Vẽ 3 nét khác trên `board2`

4. **Tab 1 (board1)**: Vẫn chỉ thấy 2 nét cũ
   - Không ảnh hưởng từ board2 ✅

5. **Mở Tab 3**:
   - Board ID = `board2`
   - Join → **Nhìn thấy 3 nét của board2!** ✅

**Kết luận:** ✅ Multi-board isolation hoạt động

---

## 🎯 Demo Scenario 5: UI Controls

**Mục tiêu:** Kiểm tra các control hoạt động đúng

### Color Picker:
- [ ] Thay đổi màu → nét vẽ mới có màu đó

### Width Slider:
- [ ] Di chuyển slider → số px thay đổi
- [ ] Vẽ nét → kích thước theo slider

### Clear Button:
- [ ] Vẽ 2 nét
- [ ] Click **Clear**
- [ ] **Nét của user hiện tại biến mất** (nét của user khác vẫn còn nếu có)

### Status Indicator:
- [ ] "Connected" = server hoạt động
- [ ] "Disconnected" = server offline (tạm dừng backend để test)

---

## 🔍 Advanced Testing (Optional)

### Network Monitoring:
1. Mở DevTools (F12)
2. Tab **Network** → Filter: `WS`
3. Thực hiện vẽ → Xem WebSocket messages

### Console Logging:
1. DevTools → **Console**
2. Mỗi khi gửi/nhận message sẽ log ra
3. Kiểm tra message type: `stroke`, `full-state`, `action`

### Server Restart Resilience:
1. Vẽ nét trên board1
2. Kill server (`Ctrl+C`)
3. Restart server
4. **Mở browser new → Board state bị reset** (in-memory không persist)
   - ℹ️ Điều này bình thường cho MVP

---

## 📊 Test Results Checklist

| Test Case | Expected | Result |
|-----------|----------|--------|
| Draw on Tab 1, see on Tab 2 | ✅ Sync instantly | ☐ |
| Undo on Tab 1, others see change | ✅ Broadcast | ☐ |
| Redo works correctly | ✅ Restore stroke | ☐ |
| New tab joins, gets full state | ✅ Sync complete | ☐ |
| Board isolation (board1 vs board2) | ✅ Independent | ☐ |
| Color & width applied correctly | ✅ Visual correct | ☐ |
| Clear removes only user's strokes | ✅ Selective clear | ☐ |
| Status indicator works | ✅ Shows connected | ☐ |
| No lag/latency visible | ✅ Real-time feel | ☐ |
| Server stays up under light load | ✅ No crashes | ☐ |

---

## 💡 Tips for Demo

1. **Use Clear Space**: Clear canvas trước khi demo new feature
2. **Slow Movements**: Di chuyển chuột chậm để dễ thấy strokes
3. **Different Colors**: Dùng màu khác nhau để dễ phân biệt nét từ các user
4. **Two Monitors** (nếu có): Tối ưu hơn so với 2 tabs
5. **Narrate**: Giải thích từng bước cho audience

---

## ⚠️ Known Limitations (MVP)

- ❌ No persistence (board reset khi server restart)
- ❌ No user authentication
- ❌ No rate limiting
- ❌ No recording/playback
- ✅ Eraser không hỗ trợ (dùng Clear thay thế)

---

## 🚀 If Something Goes Wrong

### "Cannot connect to WebSocket"
- ✅ Chắc chắn backend đang chạy: `curl http://localhost:8080/health`
- ✅ Xem console browser: `F12 → Console`

### "Strokes not syncing"
- ✅ Xác nhận cả 2 tab cùng Board ID
- ✅ Kiểm tra Network tab xem có WebSocket message không
- ✅ Refresh 1 tab

### "Undo không hoạt động"
- ✅ Chắc chắn có stroke để undo (vẽ ít nhất 1 nét)
- ✅ Kiểm tra Server log: `tail -f /tmp/whiteboard.log`

---

**Ready to demo! 🎨🎬**
