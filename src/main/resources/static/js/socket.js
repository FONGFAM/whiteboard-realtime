/**
 * socket.js - STOMP WebSocket connection and messaging
 */

let stompClient = null;
let isConnected = false;
let currentBoardId = 'board1';

/**
 * Connect to WebSocket server
 */
function connectWebSocket() {
    const socket = new SockJS('/ws-whiteboard');
    stompClient = Stomp.over(socket);

    // Disable debug logs in production
    stompClient.debug = function() {};

    stompClient.connect({}, onConnected, onError);
}

/**
 * Handle successful connection
 */
function onConnected() {
    console.log('Connected to WebSocket');
    isConnected = true;
    updateStatus('Connected', true);

    // Subscribe to board updates
    stompClient.subscribe(`/topic/board/${currentBoardId}`, onMessageReceived);

    // Request full state sync
    requestSync();
}

/**
 * Handle connection error
 */
function onError(error) {
    console.error('WebSocket error:', error);
    isConnected = false;
    updateStatus('Disconnected', false);
    
    // Retry connection after 3 seconds
    setTimeout(connectWebSocket, 3000);
}

/**
 * Update connection status display
 */
function updateStatus(message, connected) {
    const statusEl = document.getElementById('status');
    statusEl.textContent = message;
    statusEl.classList.toggle('connected', connected);
}

/**
 * Handle incoming message from server
 */
function onMessageReceived(message) {
    try {
        const data = JSON.parse(message.body);
        console.log('Received message type:', data.type);

        switch (data.type) {
            case 'stroke':
                // Received a stroke from another client
                handleRemoteStroke(data.stroke);
                break;

            case 'full-state':
                // Received full board state
                handleFullState(data.data);
                break;

            case 'action':
                // Received action (undo/redo/clear)
                handleRemoteAction(data);
                break;

            default:
                console.warn('Unknown message type:', data.type);
        }
    } catch (error) {
        console.error('Error processing message:', error);
    }
}

/**
 * Send stroke to server
 */
function sendStroke(stroke) {
    if (!isConnected || !stompClient) return;

    stompClient.send(
        `/app/stroke/${currentBoardId}`,
        {},
        JSON.stringify(stroke)
    );
}

/**
 * Request full board state
 */
function requestSync() {
    if (!isConnected || !stompClient) return;

    stompClient.send(
        `/app/sync/${currentBoardId}`,
        {},
        ''
    );
}

/**
 * Send action to server
 */
function sendAction(action) {
    if (!isConnected || !stompClient) return;

    const message = {
        type: 'action',
        boardId: currentBoardId,
        userId: currentStroke.userId,
        action: action
    };

    stompClient.send(
        `/app/action/${currentBoardId}`,
        {},
        JSON.stringify(message)
    );
}

/**
 * Handle remote stroke
 */
function handleRemoteStroke(stroke) {
    addStrokeLocally(stroke);
}

/**
 * Handle full state sync
 */
function handleFullState(boardState) {
    if (!boardState) {
        console.warn('Empty board state received');
        return;
    }

    console.log('Syncing full state, strokes:', boardState.strokes.length);
    
    // Clear and rebuild local state
    allStrokes = boardState.strokes || [];
    hiddenStrokeIds = new Set(boardState.hiddenStrokeIds || []);
    
    redrawCanvas();
}

/**
 * Handle remote action
 */
function handleRemoteAction(message) {
    const action = message.action;
    const userId = message.userId;
    
    console.log('Remote action:', action, 'by', userId);

    switch (action) {
        case 'undo':
        case 'redo':
        case 'clear':
            // Update hidden set
            // Server will send updated hiddenStrokeIds in next sync
            // For now, we need to refetch state
            requestSync();
            break;
    }
}

/**
 * Initialize WebSocket on page load
 */
window.addEventListener('load', () => {
    connectWebSocket();
});
