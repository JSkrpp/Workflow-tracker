const API = '/v1/api/projects';

const sessionUserEl = document.getElementById('sessionUser');
const logoutBtn = document.getElementById('logoutBtn');
const taskListEl = document.getElementById('taskList');
const sentInviteListEl = document.getElementById('sentInviteList');
const ownerInviteCard = document.getElementById('ownerInviteCard');
const taskAssigneeEl = document.getElementById('taskAssignee');

let projectMembers = [];
let currentUserRole = null;

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
    await loadMembers();
    await loadTasks();
    await detectOwnerAndLoadInvites();
}

async function loadMembers() {
    const res = await SessionManager.authFetch(API + '/' + projectId + '/members');
    if (!res.ok) {
        projectMembers = [];
        currentUserRole = null;
        taskAssigneeEl.innerHTML = '<option value="">Unassigned</option>';
        return;
    }

    const members = await res.json();
    projectMembers = members;
    const currentUserId = session && session.userId ? Number(session.userId) : null;
    const myMember = members.find(m => Number(m.userId) === currentUserId);
    currentUserRole = myMember ? myMember.role : null;

    taskAssigneeEl.innerHTML = '<option value="">Unassigned</option>' + members.map(m => {
        return '<option value="' + m.userId + '">' + esc(m.displayName || m.email || ('User #' + m.userId)) + '</option>';
    }).join('');
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
        const normalizedStatus = normalizeStatusValue(t.status);
        const statusClass = normalizedStatus.toLowerCase();
        const isAssignee = session && Number(session.userId) === Number(t.assigneeUserId);
        const canReassign = currentUserRole === 'OWNER';
        return '<div class="task-item">' +
            '<strong>' + esc(t.title) + '</strong>' +
            '<span class="task-status ' + statusClass + '">' + esc(getStatusLabel(normalizedStatus)) + '</span>' +
            (t.assigneeDisplayName ? '<p><small>Assignee: ' + esc(t.assigneeDisplayName) + '</small></p>' : '') +
            (t.description ? '<p>' + esc(t.description) + '</p>' : '') +
            (isAssignee ? renderStatusEditor(t, normalizedStatus) : '') +
            (canReassign ? renderAssigneeEditor(t) : '') +
            '</div>';
    }).join('');
}

function renderStatusEditor(task, normalizedStatus) {
    const statuses = ['TODO', 'IN_PROGRESS', 'DONE'];
    return '<div class="form-group">' +
        '<label>Update Status</label>' +
        '<div style="display:flex;gap:8px;align-items:center;">' +
        '<select data-action="status-select" data-task-id="' + task.id + '">' +
        statuses.map(s => '<option value="' + s + '"' + (normalizedStatus === s ? ' selected' : '') + '>' + esc(getStatusLabel(s)) + '</option>').join('') +
        '</select>' +
        '<button class="btn btn-secondary" type="button" data-action="save-status" data-task-id="' + task.id + '">Save</button>' +
        '</div>' +
        '</div>';
}

function renderAssigneeEditor(task) {
    return '<div class="form-group">' +
        '<label>Change Assignee</label>' +
        '<div style="display:flex;gap:8px;align-items:center;">' +
        '<select data-action="assignee-select" data-task-id="' + task.id + '">' +
        '<option value="">Select assignee</option>' +
        projectMembers.map(m => {
            const selected = Number(m.userId) === Number(task.assigneeUserId) ? ' selected' : '';
            return '<option value="' + m.userId + '"' + selected + '>' + esc(m.displayName || m.email || ('User #' + m.userId)) + '</option>';
        }).join('') +
        '</select>' +
        '<button class="btn btn-secondary" type="button" data-action="save-assignee" data-task-id="' + task.id + '">Save</button>' +
        '</div>' +
        '</div>';
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
        status: 'TODO',
        assigneeUserId: taskAssigneeEl.value ? Number(taskAssigneeEl.value) : null
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

taskListEl.addEventListener('click', async (e) => {
    const button = e.target.closest('button[data-action]');
    if (!button) return;

    const action = button.getAttribute('data-action');
    const taskId = Number(button.getAttribute('data-task-id'));
    if (!taskId) return;

    if (action === 'save-status') {
        const select = taskListEl.querySelector('select[data-action="status-select"][data-task-id="' + taskId + '"]');
        if (!select) return;
        await updateTask(taskId, { status: normalizeStatusValue(select.value) });
        return;
    }

    if (action === 'save-assignee') {
        const select = taskListEl.querySelector('select[data-action="assignee-select"][data-task-id="' + taskId + '"]');
        if (!select || !select.value) return;
        await updateTask(taskId, { assigneeUserId: Number(select.value) });
    }
});

async function updateTask(taskId, body) {
    const message = document.getElementById('taskFormMessage');
    message.classList.remove('success');
    message.textContent = '';

    const res = await SessionManager.authFetch(API + '/' + projectId + '/tasks/' + taskId, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!res.ok) {
        message.textContent = 'Error updating task: ' + await safeErrorText(res);
        return;
    }

    message.textContent = 'Task updated';
    message.classList.add('success');
    await loadTasks();
}

async function safeErrorText(res) {
    try {
        const data = await res.json();
        return data.error || data.message || JSON.stringify(data);
    } catch (_) {
        return await res.text();
    }
}

function normalizeStatusValue(raw) {
    const normalized = String(raw || 'TODO').trim().toUpperCase().replace(/\s+/g, '_');
    if (normalized === 'TODO' || normalized === 'IN_PROGRESS' || normalized === 'DONE') {
        return normalized;
    }
    return 'TODO';
}

function getStatusLabel(statusValue) {
    if (statusValue === 'IN_PROGRESS') return 'IN PROGRESS';
    return statusValue;
}

function esc(s) {
    if (!s) return '';
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

init();
