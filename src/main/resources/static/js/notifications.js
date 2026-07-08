/**
 * Notifications Client Script
 * Handles unread counts, WebSocket connection (if available), and floating notification bell.
 */
document.addEventListener('DOMContentLoaded', () => {
    initNotificationBell();
    fetchUnreadCount();
    setInterval(fetchUnreadCount, 30000); // Poll every 30s as fallback
});

function initNotificationBell() {
    if (document.getElementById('notif-bell')) return;

    const bellContainer = document.createElement('div');
    bellContainer.id = 'notif-bell';
    bellContainer.style.cssText = `
        position: fixed;
        bottom: 25px;
        right: 25px;
        width: 55px;
        height: 55px;
        background: #667eea;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        cursor: pointer;
        box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        z-index: 9999;
        transition: transform 0.2s;
    `;
    bellContainer.innerHTML = `
        🔔
        <span id="notif-badge" style="
            position: absolute;
            top: -5px;
            right: -5px;
            background: #f5576c;
            color: white;
            font-size: 12px;
            font-weight: bold;
            padding: 2px 6px;
            border-radius: 10px;
            display: none;
        ">0</span>
    `;
    bellContainer.onclick = showNotificationsModal;
    document.body.appendChild(bellContainer);
}

async function fetchUnreadCount() {
    try {
        const res = await fetch('/api/notifications/unread-count');
        if (res.ok) {
            const data = await res.json();
            const count = data.data ? data.data.count : 0;
            const badge = document.getElementById('notif-badge');
            if (badge) {
                if (count > 0) {
                    badge.textContent = count > 99 ? '99+' : count;
                    badge.style.display = 'block';
                } else {
                    badge.style.display = 'none';
                }
            }
        }
    } catch (e) {
        // Silently ignore auth or network errors
    }
}

async function showNotificationsModal() {
    let modal = document.getElementById('notif-modal');
    if (modal) {
        modal.remove();
        return;
    }

    modal = document.createElement('div');
    modal.id = 'notif-modal';
    modal.style.cssText = `
        position: fixed;
        bottom: 90px;
        right: 25px;
        width: 350px;
        max-height: 450px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        z-index: 10000;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        border: 1px solid #eee;
    `;

    modal.innerHTML = `
        <div style="background: #667eea; color: white; padding: 15px; display: flex; justify-content: space-between; align-items: center;">
            <h4 style="margin:0;">Notifications</h4>
            <div>
                <button onclick="markAllNotificationsRead()" style="background:none; border:none; color:white; cursor:pointer; font-size:12px; text-decoration:underline; margin-right: 10px;">Mark all read</button>
                <button onclick="clearAllNotifications()" style="background:none; border:none; color:white; cursor:pointer; font-size:12px; text-decoration:underline;">Clear all</button>
            </div>
        </div>
        <div id="notif-list" style="padding: 15px; overflow-y: auto; flex: 1;">
            <p style="text-align:center; color:#888;">Loading notifications...</p>
        </div>
    `;
    document.body.appendChild(modal);
    loadNotificationsList();
}

async function loadNotificationsList() {
    const listEl = document.getElementById('notif-list');
    if (!listEl) return;

    try {
        const res = await fetch('/api/notifications');
        if (res.ok) {
            const data = await res.json();
            const notifs = data.data || [];
            if (notifs.length === 0) {
                listEl.innerHTML = '<p style="text-align:center; color:#888; margin: 20px 0;">No notifications</p>';
                return;
            }
            listEl.innerHTML = notifs.map(n => `
                <div style="padding: 10px; border-bottom: 1px solid #f0f0f0; background: ${n.read ? 'white' : '#f8f9fe'}; position: relative;">
                    <strong style="font-size: 14px; color: #333; display: block; margin-bottom: 4px;">${n.title || 'Notification'}</strong>
                    <p style="font-size: 13px; color: #666; margin: 0 0 6px 0;">${n.message || ''}</p>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 11px; color: #aaa;">${new Date(n.createdAt || Date.now()).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                        <div>
                            ${!n.read ? `<button onclick="markNotificationRead('${n.id}')" style="background:none; border:none; color:#667eea; cursor:pointer; font-size:11px;">Mark read</button>` : ''}
                            <button onclick="deleteNotificationItem('${n.id}')" style="background:none; border:none; color:#f5576c; cursor:pointer; font-size:11px; margin-left:8px;">Delete</button>
                        </div>
                    </div>
                </div>
            `).join('');
        } else {
            listEl.innerHTML = '<p style="text-align:center; color:#f5576c;">Failed to load notifications.</p>';
        }
    } catch (e) {
        listEl.innerHTML = '<p style="text-align:center; color:#f5576c;">Error loading notifications.</p>';
    }
}

async function markNotificationRead(id) {
    await fetch(`/api/notifications/${id}/read`, { method: 'PUT' });
    fetchUnreadCount();
    loadNotificationsList();
}

async function markAllNotificationsRead() {
    await fetch('/api/notifications/read-all', { method: 'PUT' });
    fetchUnreadCount();
    loadNotificationsList();
}

async function deleteNotificationItem(id) {
    await fetch(`/api/notifications/${id}`, { method: 'DELETE' });
    fetchUnreadCount();
    loadNotificationsList();
}

async function clearAllNotifications() {
    if (confirm('Clear all notifications?')) {
        await fetch('/api/notifications/clear-all', { method: 'DELETE' });
        fetchUnreadCount();
        loadNotificationsList();
    }
}
