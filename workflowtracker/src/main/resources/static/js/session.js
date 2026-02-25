(function () {
    const STORAGE_KEY = 'wf_session';

    function read() {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (_) {
            localStorage.removeItem(STORAGE_KEY);
            return null;
        }
    }

    function write(session) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    }

    function clear() {
        localStorage.removeItem(STORAGE_KEY);
    }

    function requireSession() {
        const session = read();
        if (!session || !session.token) {
            window.location.href = '/auth.html';
            return null;
        }
        return session;
    }

    async function authFetch(url, options = {}) {
        const session = read();
        const headers = new Headers(options.headers || {});
        if (!headers.has('Content-Type') && options.body) {
            headers.set('Content-Type', 'application/json');
        }
        if (session && session.token) {
            headers.set('Authorization', 'Bearer ' + session.token);
        }

        const response = await fetch(url, { ...options, headers });
        if (response.status === 401) {
            clear();
            window.location.href = '/auth.html';
        }
        return response;
    }

    window.SessionManager = {
        getSession: read,
        saveSession: write,
        clearSession: clear,
        requireSession,
        authFetch
    };
})();
