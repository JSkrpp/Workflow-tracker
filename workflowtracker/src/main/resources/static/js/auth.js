const API_AUTH = '/auth';

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const loginCard = document.getElementById('loginCard');
const registerCard = document.getElementById('registerCard');
const showRegisterBtn = document.getElementById('showRegisterBtn');
const showLoginBtn = document.getElementById('showLoginBtn');

if (SessionManager.getSession()) {
    window.location.href = '/';
}

showRegisterBtn.addEventListener('click', () => {
    loginCard.style.display = 'none';
    registerCard.style.display = 'block';
    registerMessage.textContent = '';
});

showLoginBtn.addEventListener('click', () => {
    registerCard.style.display = 'none';
    loginCard.style.display = 'block';
    loginMessage.textContent = '';
});

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    loginMessage.textContent = '';
    loginMessage.classList.remove('success');

    const body = {
        email: document.getElementById('loginEmail').value.trim(),
        password: document.getElementById('loginPassword').value
    };

    try {
        const res = await fetch(API_AUTH + '/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            throw new Error(await safeErrorText(res));
        }

        const auth = await res.json();
        SessionManager.saveSession(auth);
        window.location.href = '/';
    } catch (err) {
        loginMessage.textContent = 'Error: ' + err.message;
    }
});

registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    registerMessage.textContent = '';
    registerMessage.classList.remove('success');

    const body = {
        displayName: document.getElementById('registerDisplayName').value.trim(),
        email: document.getElementById('registerEmail').value.trim(),
        password: document.getElementById('registerPassword').value
    };

    try {
        const res = await fetch(API_AUTH + '/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            throw new Error(await safeErrorText(res));
        }

        const auth = await res.json();
        SessionManager.saveSession(auth);
        registerMessage.textContent = 'Account created. Redirecting...';
        registerMessage.classList.add('success');
        setTimeout(() => {
            window.location.href = '/';
        }, 500);
    } catch (err) {
        registerMessage.textContent = 'Error: ' + err.message;
    }
});

async function safeErrorText(res) {
    try {
        const data = await res.json();
        return data.error || data.message || JSON.stringify(data);
    } catch (_) {
        return await res.text();
    }
}
