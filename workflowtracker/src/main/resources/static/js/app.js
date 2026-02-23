const API = 'v1/api/projects';
const form = document.getElementById('createProjectForm');
const msg = document.getElementById('formMessage');
const listEl = document.getElementById('projectList');
let currentProjectId = null;

// --- Create Project ---
form.addEventListener('submit', async (e) => {
    e.preventDefault();
    msg.textContent = '';
    const body = {
        key: document.getElementById('projectKey').value.trim(),
        name: document.getElementById('projectName').value.trim(),
        description: document.getElementById('projectDesc').value.trim()
    };
    try {
        const res = await fetch(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error(await res.text());
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

// --- Load Projects ---
async function loadProjects() {
    listEl.innerHTML = '';
    try {
        const res = await fetch(API);
        const projects = await res.json();
        if (projects.length === 0) {
            listEl.innerHTML = '<p class="empty">No projects yet.</p>';
            return;
        }
        projects.forEach(p => {
            const div = document.createElement('div');
            div.className = 'project-item';
            div.dataset.id = p.id;
            div.dataset.label = esc(p.key) + ' \u2014 ' + esc(p.name);
            div.innerHTML =
                '<strong>' + esc(p.key) + '</strong> \u2014 ' + esc(p.name) +
                ' <span class="task-count">' + (p.tasks ? p.tasks.length : 0) + ' tasks</span>' +
                '<button class="delete-btn" data-id="' + p.id + '">\uD83D\uDDD1\uFE0F</button>';
            listEl.appendChild(div);
        });
    } catch (err) {
        listEl.innerHTML = '<p class="empty">Failed to load.</p>';
    }
}

// --- Delete Project ---
async function deleteProject(id) {
    try {
        const res = await fetch(API + '/' + id, { method: 'DELETE' });
        if (!res.ok) throw new Error(await res.text());
        loadProjects();
    } catch (err) {
        msg.textContent = 'Error deleting: ' + err.message;
        msg.classList.remove('success');
    }
}

// --- Project list click: open modal or delete ---
listEl.addEventListener('click', (e) => {
    const delBtn = e.target.closest('.delete-btn');
    if (delBtn) {
        e.stopPropagation();
        if (confirm('Delete project #' + delBtn.dataset.id + '?')) {
            deleteProject(delBtn.dataset.id);
        }
        return;
    }
    const row = e.target.closest('.project-item');
    if (row) {
        openTaskModal(Number(row.dataset.id), row.dataset.label);
    }
});

// --- Task Modal ---
function openTaskModal(projectId, label) {
    currentProjectId = projectId;
    document.getElementById('modalTitle').textContent = label;
    document.getElementById('taskFormMessage').textContent = '';
    document.getElementById('addTaskForm').reset();
    document.getElementById('taskModal').style.display = 'flex';
    loadTasks();
}

function closeTaskModal() {
    currentProjectId = null;
    document.getElementById('taskModal').style.display = 'none';
    loadProjects();
}

document.getElementById('modalCloseBtn').addEventListener('click', closeTaskModal);
document.getElementById('taskModal').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) closeTaskModal();
});

// --- Add Task ---
document.getElementById('addTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const tmsg = document.getElementById('taskFormMessage');
    tmsg.textContent = '';
    const body = {
        title: document.getElementById('taskTitle').value.trim(),
        description: document.getElementById('taskDesc').value.trim(),
        status: document.getElementById('taskStatus').value
    };
    try {
        const res = await fetch(API + '/' + currentProjectId + '/tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error(await res.text());
        tmsg.textContent = 'Task added!';
        tmsg.classList.add('success');
        document.getElementById('addTaskForm').reset();
        loadTasks();
    } catch (err) {
        tmsg.textContent = 'Error: ' + err.message;
        tmsg.classList.remove('success');
    }
});

// --- Load Tasks ---
async function loadTasks() {
    const container = document.getElementById('taskList');
    try {
        const res = await fetch(API + '/' + currentProjectId + '/tasks');
        const tasks = await res.json();
        if (tasks.length === 0) {
            container.innerHTML = '<p class="empty">No tasks yet.</p>';
            return;
        }
        container.innerHTML = tasks.map(t =>
            '<div class="task-item">' +
                '<span class="task-index">#' + t.index + '</span>' +
                '<strong>' + esc(t.title) + '</strong>' +
                '<span class="task-status ' + t.status.toLowerCase() + '">' + esc(t.status) + '</span>' +
                (t.description ? '<p>' + esc(t.description) + '</p>' : '') +
            '</div>'
        ).join('');
    } catch (err) {
        container.innerHTML = '<p class="empty">Error loading tasks.</p>';
    }
}

// --- Util ---
function esc(s) {
    if (!s) return '';
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// --- Init ---
document.getElementById('refreshBtn').addEventListener('click', loadProjects);
loadProjects();
