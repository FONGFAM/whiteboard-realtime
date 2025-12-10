/**
 * canvas.js - Canvas drawing logic
 */

let canvas = document.getElementById('drawingCanvas');
let ctx = canvas.getContext('2d');

// Drawing state
let isDrawing = false;
let currentStroke = {
    points: [],
    color: '#000000',
    width: 3,
    userId: generateUserId()
};

let allStrokes = [];
let hiddenStrokeIds = new Set();

/**
 * Generate or retrieve user ID
 */
function generateUserId() {
    let userId = localStorage.getItem('userId');
    if (!userId) {
        userId = 'user_' + Math.random().toString(36).substr(2, 9);
        localStorage.setItem('userId', userId);
    }
    return userId;
}

/**
 * Resize canvas to fit window
 */
function resizeCanvas() {
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width;
    canvas.height = rect.height;
    redrawCanvas();
}

/**
 * Handle pointer down event
 */
canvas.addEventListener('pointerdown', (e) => {
    if (!isConnected) return;
    
    isDrawing = true;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    currentStroke = {
        points: [{ x, y }],
        color: document.getElementById('colorPicker').value,
        width: parseInt(document.getElementById('widthSlider').value),
        userId: currentStroke.userId
    };
});

/**
 * Handle pointer move event (with throttling)
 */
let lastMoveTime = 0;
canvas.addEventListener('pointermove', (e) => {
    if (!isDrawing || !isConnected) return;

    const now = Date.now();
    // Throttle to ~60fps (sample every ~16ms)
    if (now - lastMoveTime < 16) return;
    lastMoveTime = now;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    currentStroke.points.push({ x, y });

    // Draw locally in real-time
    drawLine(
        currentStroke.points[currentStroke.points.length - 2],
        { x, y },
        currentStroke.color,
        currentStroke.width
    );
});

/**
 * Handle pointer up event
 */
canvas.addEventListener('pointerup', () => {
    if (!isDrawing) return;
    isDrawing = false;

    if (currentStroke.points.length > 1) {
        // Send stroke to server
        sendStroke(currentStroke);
    }
});

canvas.addEventListener('pointerleave', () => {
    isDrawing = false;
});

/**
 * Draw a line between two points
 */
function drawLine(fromPoint, toPoint, color, width) {
    ctx.strokeStyle = color;
    ctx.lineWidth = width;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    
    ctx.beginPath();
    ctx.moveTo(fromPoint.x, fromPoint.y);
    ctx.lineTo(toPoint.x, toPoint.y);
    ctx.stroke();
}

/**
 * Add stroke to local collection and redraw
 */
function addStrokeLocally(stroke) {
    allStrokes.push(stroke);
    drawStroke(stroke);
}

/**
 * Draw a complete stroke on canvas
 */
function drawStroke(stroke) {
    // Don't draw if stroke is hidden
    if (hiddenStrokeIds.has(stroke.id)) return;

    ctx.strokeStyle = stroke.color;
    ctx.lineWidth = stroke.width;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    if (stroke.points.length < 2) return;

    ctx.beginPath();
    ctx.moveTo(stroke.points[0].x, stroke.points[0].y);
    
    for (let i = 1; i < stroke.points.length; i++) {
        ctx.lineTo(stroke.points[i].x, stroke.points[i].y);
    }
    ctx.stroke();
}

/**
 * Redraw entire canvas
 */
function redrawCanvas() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    for (let stroke of allStrokes) {
        drawStroke(stroke);
    }
}

/**
 * Update width slider display
 */
document.getElementById('widthSlider').addEventListener('change', (e) => {
    document.getElementById('widthValue').textContent = e.target.value;
});

/**
 * Initialize canvas on load
 */
window.addEventListener('load', () => {
    resizeCanvas();
});

window.addEventListener('resize', () => {
    resizeCanvas();
});
