const API_PROJECTS = '/v1/api/projects';
const API_INVITES = '/v1/api/invites';

const form = document.getElementById('createProjectForm');
const msg = document.getElementById('formMessage');
const listEl = document.getElementById('projectList');
const inviteListEl = document.getElementById('inviteList');

const sessionUserEl = document.getElementById('sessionUser');
const logoutBtn = document.getElementById('logoutBtn');
const authPageBtn = document.getElementById('authPageBtn');

const session = SessionManager.requireSession();
if (session) {
    sessionUserEl.textContent = 'Logged in as ' + (session.displayName || session.email || 'user');
    logoutBtn.style.display = 'inline-block';
    authPageBtn.style.display = 'none';
} else {
    logoutBtn.style.display = 'none';
    authPageBtn.style.display = 'inline-block';
}

logoutBtn.addEventListener('click', () => {
    SessionManager.clearSession();
    window.location.href = '/auth.html';
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    msg.textContent = '';

    const body = {
        key: document.getElementById('projectKey').value.trim(),
        name: document.getElementById('projectName').value.trim(),
        description: document.getElementById('projectDesc').value.trim()
    };

    try {
        const res = await SessionManager.authFetch(API_PROJECTS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!res.ok) throw new Error(await safeErrorText(res));
        const project = await res.json();
        msg.textContent = 'Created project #' + project.id + ' (' + project.key + ')';
        msg.classList.add('success');
        form.reset();
        loadProjects();
    } catch (err) {
        msg.textContent = 'Error: ' + err.message;
        msg.classList.remove('success');
    }
});

async function loadProjects() {
    listEl.innerHTML = '';
    try {
        const res = await SessionManager.authFetch(API_PROJECTS);
        if (!res.ok) throw new Error(await safeErrorText(res));

        const projects = await res.json();
        if (!projects.length) {
            listEl.innerHTML = '<p class="empty">No projects yet.</p>';
            return;
        }

        listEl.innerHTML = projects.map(p =>
            '<div class="project-item" data-id="' + p.id + '">' +
                '<strong>' + esc(p.key) + '</strong> — ' + esc(p.name) +
                '<span class="task-count">Open</span>' +
            '</div>'
        ).join('');
    } catch (err) {
        listEl.innerHTML = '<p class="empty">Failed to load projects.</p>';
    }
}

listEl.addEventListener('click', (e) => {
    const row = e.target.closest('.project-item');
    if (!row) return;
    window.location.href = '/project.html?id=' + row.dataset.id;
});

async function loadMyInvites() {
    inviteListEl.innerHTML = '';
    try {
        const res = await SessionManager.authFetch(API_INVITES + '/me');
        if (!res.ok) throw new Error(await safeErrorText(res));
        const invites = await res.json();

        if (!invites.length) {
            inviteListEl.innerHTML = '<p class="empty">No pending invites.</p>';
            return;
        }

        inviteListEl.innerHTML = invites.map(i =>
            '<div class="invite-item" data-token="' + escAttr(i.token) + '">' +
                '<div><strong>' + esc(i.projectKey || ('Project #' + i.projectId)) + '</strong></div>' +
                '<div class="invite-meta">Invited as ' + esc(i.role) + '</div>' +
                '<div class="invite-actions">' +
                    '<button class="btn btn-secondary accept-invite" type="button">Accept</button>' +
                    '<button class="btn btn-secondary decline-invite" type="button">Decline</button>' +
                '</div>' +
            '</div>'
        ).join('');
    } catch (err) {
        inviteListEl.innerHTML = '<p class="empty">Failed to load invites.</p>';
    }
}

inviteListEl.addEventListener('click', async (e) => {
    const card = e.target.closest('.invite-item');
    if (!card) return;

    const token = card.dataset.token;
    if (e.target.classList.contains('accept-invite')) {
        await respondToInvite(token, 'accept');
    }
    if (e.target.classList.contains('decline-invite')) {
        await respondToInvite(token, 'decline');
    }
});

async function respondToInvite(token, action) {
    const res = await SessionManager.authFetch(API_INVITES + '/' + encodeURIComponent(token) + '/' + action, {
        method: 'POST'
    });
    if (res.ok) {
        await loadMyInvites();
        await loadProjects();
    }
}

function esc(s) {
    if (!s) return '';
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function escAttr(s) {
    return esc(s).replace(/'/g, '&#39;');
}

async function safeErrorText(res) {
    try {
        const data = await res.json();
        return data.error || data.message || JSON.stringify(data);
    } catch (_) {
        return await res.text();
    }
}

document.getElementById('refreshBtn').addEventListener('click', loadProjects);
document.getElementById('refreshInvitesBtn').addEventListener('click', loadMyInvites);

loadProjects();
loadMyInvites();
