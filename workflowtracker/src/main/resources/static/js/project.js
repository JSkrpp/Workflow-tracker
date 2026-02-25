const API = '/v1/api/projects';

const sessionUserEl = document.getElementById('sessionUser');
const logoutBtn = document.getElementById('logoutBtn');
const taskListEl = document.getElementById('taskList');
const sentInviteListEl = document.getElementById('sentInviteList');
const ownerInviteCard = document.getElementById('ownerInviteCard');

const session = SessionManager.requireSession();
if (session) {
    sessionUserEl.textContent = 'Logged in as ' + (session.displayName || session.email || 'user');
    logoutBtn.style.display = 'inline-block';
}

logoutBtn.addEventListener('click', () => {
    SessionManager.clearSession();
    window.location.href = '/auth.html';
});

const projectId = Number(new URLSearchParams(window.location.search).get('id'));
if (!projectId) {
    window.location.href = '/';
}

async function init() {
    await loadProject();
    await loadTasks();
    await detectOwnerAndLoadInvites();
}

async function loadProject() {
    const res = await SessionManager.authFetch(API + '/' + projectId);
    if (!res.ok) {
        document.getElementById('projectTitle').textContent = 'Project not found';
        return;
    }
    const project = await res.json();
    document.getElementById('projectTitle').textContent = (project.key || '') + ' — ' + (project.name || 'Project');
    document.getElementById('projectDescription').textContent = project.description || '';
}

async function loadTasks() {
    const res = await SessionManager.authFetch(API + '/' + projectId + '/tasks');
    if (!res.ok) {
        taskListEl.innerHTML = '<p class="empty">Failed to load tasks.</p>';
        return;
    }

    const tasks = await res.json();
    if (!tasks.length) {
        taskListEl.innerHTML = '<p class="empty">No tasks yet.</p>';
        return;
    }

    taskListEl.innerHTML = tasks.map(t => {
        const statusClass = (t.status || '').toLowerCase().replace(/\s+/g, '_');
        return '<div class="task-item">' +
            '<strong>' + esc(t.title) + '</strong>' +
            '<span class="task-status ' + statusClass + '">' + esc(t.status) + '</span>' +
            (t.description ? '<p>' + esc(t.description) + '</p>' : '') +
            '</div>';
    }).join('');
}

async function detectOwnerAndLoadInvites() {
    const res = await SessionManager.authFetch(API + '/' + projectId + '/invites');
    if (!res.ok) {
        ownerInviteCard.style.display = 'none';
        return;
    }

    ownerInviteCard.style.display = 'block';
    const invites = await res.json();
    renderSentInvites(invites);
}

function renderSentInvites(invites) {
    if (!invites.length) {
        sentInviteListEl.innerHTML = '<p class="empty">No invites sent yet.</p>';
        return;
    }

    sentInviteListEl.innerHTML = invites.map(i =>
        '<div class="task-item">' +
            '<strong>' + esc(i.email) + '</strong>' +
            '<span class="task-status">' + esc(i.status) + '</span>' +
        '</div>'
    ).join('');
}

document.getElementById('addTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const message = document.getElementById('taskFormMessage');
    message.textContent = '';

    const body = {
        title: document.getElementById('taskTitle').value.trim(),
        description: document.getElementById('taskDesc').value.trim(),
        status: 'TODO'
    };

    const res = await SessionManager.authFetch(API + '/' + projectId + '/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        message.textContent = 'Error adding task';
        return;
    }

    message.textContent = 'Task added';
    message.classList.add('success');
    document.getElementById('addTaskForm').reset();
    await loadTasks();
});

document.getElementById('inviteForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const message = document.getElementById('inviteMessage');
    message.textContent = '';

    const body = {
        email: document.getElementById('inviteEmail').value.trim(),
        role: 'MEMBER'
    };

    const res = await SessionManager.authFetch(API + '/' + projectId + '/invites', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        message.textContent = 'Error sending invite';
        return;
    }

    message.textContent = 'Invite sent';
    message.classList.add('success');
    document.getElementById('inviteForm').reset();
    await detectOwnerAndLoadInvites();
});

document.getElementById('refreshTasksBtn').addEventListener('click', loadTasks);

function esc(s) {
    if (!s) return '';
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

init();
