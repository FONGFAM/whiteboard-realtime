/**
 * app.js - Main application logic
 */

/**
 * Join a board by ID
 */
function joinBoard() {
    const boardIdInput = document.getElementById('boardIdInput');
    const newBoardId = boardIdInput.value.trim() || 'board1';

    if (currentBoardId === newBoardId && isConnected) {
        alert('Already connected to this board');
        return;
    }

    console.log('Joining board:', newBoardId);
    currentBoardId = newBoardId;

    // Clear local state
    allStrokes = [];
    hiddenStrokeIds = new Set();
    redrawCanvas();

    // Disconnect and reconnect
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
            console.log('Disconnected');
            connectWebSocket();
        });
    } else {
        connectWebSocket();
    }
}

/**
 * Perform undo action
 */
function performUndo() {
    console.log('Undo requested');
    sendAction('undo');
}

/**
 * Perform redo action
 */
function performRedo() {
    console.log('Redo requested');
    sendAction('redo');
}

/**
 * Clear board (current user's strokes)
 */
function clearBoard() {
    if (!confirm('Clear all your strokes from the board?')) {
        return;
    }
    console.log('Clear requested');
    sendAction('clear');
}

/**
 * Handle Enter key in board ID input
 */
document.addEventListener('keypress', (e) => {
    if (e.target.id === 'boardIdInput' && e.key === 'Enter') {
        joinBoard();
    }
});

console.log('App initialized');
