/**
 * Central API Client for Doctor Appointment Booking System
 */

const isLocalDevServer = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') && window.location.port !== '8080';
const API_BASE_URL = isLocalDevServer || window.location.protocol === 'file:'
    ? 'http://localhost:8080/api'
    : `${window.location.origin}/api`;

const TOKEN_KEY = 'doctorapp_token';
const USER_KEY = 'doctorapp_user';

function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function setSession(authData) {
    localStorage.setItem(TOKEN_KEY, authData.token);
    localStorage.setItem(USER_KEY, JSON.stringify(authData));
}

function getCurrentUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
}

function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = 'login.html';
}

async function request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
    const token = getToken();

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);
        const data = await response.json();

        if (response.status === 401) {
            // Token expired or invalid
            showToast('Session expired. Please login again.', 'error');
            setTimeout(() => logout(), 1000);
            return null;
        }

        if (!response.ok) {
            const errorMsg = data.message || `Request failed with status ${response.status}`;
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

// Toast notification helper
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Render dynamic navbar based on auth state
function setupNavbar() {
    const user = getCurrentUser();
    const navRight = document.getElementById('nav-user-area');
    if (!navRight) return;

    if (user) {
        const isDoctor = user.role === 'ROLE_DOCTOR';
        const dashboardLink = isDoctor ? 'doctor-dashboard.html' : 'patient-dashboard.html';
        const roleName = isDoctor ? 'Doctor' : 'Patient';
        const roleClass = isDoctor ? 'doctor' : 'patient';

        navRight.innerHTML = `
            <div class="user-badge">
                <span>👋 ${user.fullName || user.email}</span>
                <span class="role-pill ${roleClass}">${roleName}</span>
            </div>
            <a href="${dashboardLink}" class="btn btn-outline btn-sm">Dashboard</a>
            <button onclick="logout()" class="btn btn-secondary btn-sm">Logout</button>
        `;
    } else {
        navRight.innerHTML = `
            <a href="login.html" class="btn btn-secondary btn-sm">Login</a>
            <a href="register.html" class="btn btn-primary btn-sm">Sign Up</a>
        `;
    }
}

document.addEventListener('DOMContentLoaded', setupNavbar);
