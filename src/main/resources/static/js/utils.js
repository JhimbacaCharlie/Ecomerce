/* ========================================
   UTILIDADES GLOBALES
   ======================================== */

function getAuthData() {
    const data = localStorage.getItem('authData');
    return data ? JSON.parse(data) : null;
}

function setAuthData(data) {
    localStorage.setItem('authData', JSON.stringify(data));
}

function getToken() {
    const data = getAuthData();
    return data ? data.token : null;
}

function isLoggedIn() {
    return getAuthData() !== null;
}

function isAdmin() {
    const data = getAuthData();
    return data !== null && data.rol === 'ADMIN';
}

function logout() {
    localStorage.removeItem('authData');
    window.location.href = 'login.html';
}

function formatCurrency(amount) {
    return 'S/ ' + Number(amount).toFixed(2);
}

async function fetchWithAuth(url, options = {}) {
    const token = getToken();
    if (token) {
        if (!options.headers) {
            options.headers = {};
        }
        options.headers['Authorization'] = 'Bearer ' + token;
    }
    const response = await fetch(url, options);
    if (response.status === 401) {
        localStorage.removeItem('authData');
        window.location.href = 'login.html';
        throw new Error('No autorizado');
    }
    return response;
}

function mostrarAlerta(mensaje, tipo) {
    let container = document.getElementById('alert-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'alert-container';
        const main = document.querySelector('.container') || document.querySelector('.auth-container');
        if (main) {
            main.insertBefore(container, main.firstChild);
        }
    }
    const alert = document.createElement('div');
    alert.className = 'alert alert-' + tipo;
    alert.textContent = mensaje;
    container.appendChild(alert);
    setTimeout(() => {
        alert.remove();
    }, 4000);
}

function getUrlParam(param) {
    const params = new URLSearchParams(window.location.search);
    return params.get(param);
}

function updateNavbar() {
    const authData = getAuthData();
    const navLinks = document.getElementById('nav-links');
    if (!navLinks) return;

    if (authData) {
        let html = '<li><a href="index.html" class="' + (getCurrentPage() === 'index' ? 'active' : '') + '">Productos</a></li>';

        if (authData.rol === 'ADMIN') {
            html += '<li><a href="admin.html" class="' + (getCurrentPage() === 'admin' ? 'active' : '') + '">Admin</a></li>';
        }

        html += '<li><a href="carrito.html" class="cart-badge ' + (getCurrentPage() === 'carrito' ? 'active' : '') + '" data-count="0" id="cart-count-link">Carrito</a></li>';
        html += '<li><a href="pedidos.html" class="' + (getCurrentPage() === 'pedidos' ? 'active' : '') + '">Pedidos</a></li>';
        html += '<li><span style="font-size:0.85rem;color:var(--gray-500);margin-right:0.25rem">' + authData.nombre + '</span></li>';
        html += '<li><button class="btn-logout" onclick="logout()">Salir</button></li>';
        navLinks.innerHTML = html;
        loadCartCount();
    } else {
        navLinks.innerHTML = '<li><a href="index.html" class="' + (getCurrentPage() === 'index' ? 'active' : '') + '">Productos</a></li>' +
            '<li><a href="login.html" class="' + (getCurrentPage() === 'login' ? 'active' : '') + '">Ingresar</a></li>' +
            '<li><a href="registro.html" class="' + (getCurrentPage() === 'registro' ? 'active' : '') + '">Registrarse</a></li>';
    }
}

async function loadCartCount() {
    if (!isLoggedIn()) return;
    try {
        const res = await fetchWithAuth('/api/carrito');
        if (res.ok) {
            const data = await res.json();
            const link = document.getElementById('cart-count-link');
            if (link) {
                link.setAttribute('data-count', data.totalItems || 0);
            }
        }
    } catch (e) {
        // silencioso
    }
}

function getCurrentPage() {
    const path = window.location.pathname;
    if (path.includes('index')) return 'index';
    if (path.includes('login')) return 'login';
    if (path.includes('registro')) return 'registro';
    if (path.includes('detalle-producto')) return 'detalle-producto';
    if (path.includes('carrito')) return 'carrito';
    if (path.includes('pedidos')) return 'pedidos';
    if (path.includes('admin')) return 'admin';
    return '';
}

document.addEventListener('DOMContentLoaded', function() {
    updateNavbar();
});
