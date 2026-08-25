const API = 'http://localhost:8080/api';
let userSession = { loggedIn: false, role: '', name: '' };

// Validar Sesión al cargar
async function checkSession() {
    const res = await fetch(`${API}/auth/me`);
    userSession = await res.json();
    updateUI();
    cargarProductos();
}

function updateUI() {
    document.getElementById('userName').textContent = userSession.loggedIn ? `Hola, ${userSession.name}` : '';
    document.getElementById('btnLogin').style.display = userSession.loggedIn ? 'none' : 'inline-block';
    document.getElementById('btnRegister').style.display = userSession.loggedIn ? 'none' : 'inline-block';
    document.getElementById('btnLogout').style.display = userSession.loggedIn ? 'inline-block' : 'none';
    
    // Mostrar panel admin si es ADMIN
    if (userSession.role === 'ADMIN') {
        document.getElementById('adminSection').style.display = 'block';
        cargarProductosAdmin();
    } else {
        document.getElementById('adminSection').style.display = 'none';
    }
}

// --- PRODUCTOS ---
async function cargarProductos() {
    const res = await fetch(`${API}/productos`);
    const productos = await res.json();
    renderProductos(productos);
    cargarCategorias(productos);
}

function renderProductos(productos) {
    const grid = document.getElementById('productosGrid');
    grid.innerHTML = productos.map(p => `
        <div class="card">
            <img class="card-img" src="${p.imagenUrl || 'https://images.unsplash.com/photo-1555664424-778a1e5e1b48?w=400&h=300&fit=crop'}" alt="${p.nombre}">
            <div class="card-body">
                <div class="card-category">${p.categoria || 'General'}</div>
                <h3 class="card-title" title="${p.nombre}">${p.nombre}</h3>
                <p style="font-size: 0.85rem; color: var(--text-dim); margin-bottom: 0.5rem; height: 40px; overflow: hidden; text-overflow: ellipsis;">${p.descripcion || ''}</p>
                <div class="card-price">S/ ${p.precio.toFixed(2)}</div>
                <div class="card-stock ${p.stock > 0 ? '' : 'out-of-stock'}">${p.stock > 0 ? 'Stock: ' + p.stock : 'Agotado'}</div>
                <button class="btn btn-primary btn-block" onclick="agregarAlCarrito(${p.id})" ${p.stock <= 0 ? 'disabled' : ''}>
                    Agregar al Carrito
                </button>
            </div>
        </div>
    `).join('');
}

function cargarCategorias(productos) {
    const select = document.getElementById('categoriaFilter');
    const cats = [...new Set(productos.map(p => p.categoria).filter(Boolean))];
    select.innerHTML = '<option value="">Todas las categorías</option>' + cats.map(c => `<option value="${c}">${c}</option>`).join('');
}

async function buscarProductos() {
    const query = document.getElementById('searchInput').value;
    if (!query) return cargarProductos();
    const res = await fetch(`${API}/productos/buscar?nombre=${query}`);
    renderProductos(await res.json());
}

async function filtrarPorCategoria() {
    const cat = document.getElementById('categoriaFilter').value;
    if (!cat) return cargarProductos();
    const res = await fetch(`${API}/productos/categoria/${cat}`);
    renderProductos(await res.json());
}

// --- CARRITO ---
async function agregarAlCarrito(productoId) {
    if (!userSession.loggedIn) return mostrarLogin();
    await fetch(`${API}/carrito/agregar`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productoId, cantidad: 1 })
    });
    alert('Producto añadido al carrito');
    actualizarContadorCarrito();
}

async function actualizarContadorCarrito() {
    if (!userSession.loggedIn) return;
    try {
        const res = await fetch(`${API}/carrito`);
        const carrito = await res.json();
        document.getElementById('carritoCount').textContent = carrito.items ? carrito.items.length : 0;
    } catch (e) { console.log(e); }
}

async function verCarrito() {
    if (!userSession.loggedIn) return mostrarLogin();
    document.getElementById('carritoModal').classList.remove('hidden');
    const res = await fetch(`${API}/carrito`);
    const carrito = await res.json();
    
    document.getElementById('carritoItems').innerHTML = carrito.items.length === 0 ? '<p>El carrito está vacío</p>' :
        carrito.items.map(i => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <strong>${i.nombreProducto}</strong><br>
                    S/ ${i.precioUnitario.toFixed(2)} x ${i.cantidad} = <b>S/ ${i.subtotal.toFixed(2)}</b>
                </div>
                <div class="cart-item-actions">
                    <button onclick="cambiarCantidad(${i.detalleId}, ${i.cantidad - 1})">-</button>
                    ${i.cantidad}
                    <button onclick="cambiarCantidad(${i.detalleId}, ${i.cantidad + 1})">+</button>
                    <button style="background:#e74c3c;color:white;" onclick="eliminarDelCarrito(${i.detalleId})">X</button>
                </div>
            </div>
        `).join('');
    
    document.getElementById('carritoTotal').textContent = carrito.total.toFixed(2);
}

async function cambiarCantidad(detalleId, nuevaCantidad) {
    if (nuevaCantidad <= 0) return eliminarDelCarrito(detalleId);
    await fetch(`${API}/carrito/actualizar`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ detalleId, cantidad: nuevaCantidad })
    });
    verCarrito();
}

async function eliminarDelCarrito(detalleId) {
    await fetch(`${API}/carrito/eliminar/${detalleId}`, { method: 'DELETE' });
    verCarrito();
    actualizarContadorCarrito();
}

async function vaciarCarrito() {
    if(confirm('¿Vaciar carrito?')) {
        await fetch(`${API}/carrito/vaciar`, { method: 'DELETE' });
        verCarrito();
        actualizarContadorCarrito();
    }
}

async function realizarPedido() {
    try {
        const res = await fetch(`${API}/pedidos`, { method: 'POST' });
        if (res.ok) {
            alert('¡Pedido realizado con éxito!');
            cerrarCarrito();
            actualizarContadorCarrito();
            verPedidos();
        } else {
            const err = await res.json();
            alert(err.error || 'Error al realizar pedido');
        }
    } catch (e) { alert('Error de conexión'); }
}

function cerrarCarrito() { document.getElementById('carritoModal').classList.add('hidden'); }

// --- AUTENTICACIÓN ---
function mostrarLogin() { document.getElementById('loginModal').classList.remove('hidden'); }
function cerrarLogin() { document.getElementById('loginModal').classList.add('hidden'); }
function mostrarRegistro() { document.getElementById('registroModal').classList.remove('hidden'); }
function cerrarRegistro() { document.getElementById('registroModal').classList.add('hidden'); }

async function login(e) {
    e.preventDefault();
    const dto = { email: document.getElementById('loginEmail').value, password: document.getElementById('loginPassword').value };
    try {
        const res = await fetch(`${API}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) });
        if (res.ok) {
            cerrarLogin();
            await checkSession();
            actualizarContadorCarrito();
        } else { alert('Credenciales incorrectas'); }
    } catch(e) { alert('Error de conexión'); }
}

async function registrar(e) {
    e.preventDefault();
    const dto = {
        nombre: document.getElementById('regNombre').value, email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value,
        direccion: document.getElementById('regDireccion').value, telefono: document.getElementById('regTelefono').value
    };
    const res = await fetch(`${API}/auth/registro`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) });
    if (res.ok) { cerrarRegistro(); alert('Registrado exitosamente. Inicie sesión.'); mostrarLogin(); }
    else { const err = await res.json(); alert(err.error || 'Error al registrar'); }
}

async function cerrarSesion() {
    await fetch(`${API}/auth/logout`, { method: 'POST' });
    userSession = { loggedIn: false, role: '', name: '' };
    updateUI();
    document.getElementById('carritoCount').textContent = '0';
}

// --- PEDIDOS ---
async function verPedidos() {
    if (!userSession.loggedIn) return mostrarLogin();
    document.getElementById('pedidosModal').classList.remove('hidden');
    const res = await fetch(`${API}/pedidos`);
    const pedidos = await res.json();
    
    document.getElementById('pedidosList').innerHTML = pedidos.length === 0 ? '<p>No tiene pedidos</p>' :
        pedidos.map(p => `
            <div class="order-card">
                <div class="order-header">
                    <span><strong>Pedido #${p.id}</strong> - ${new Date(p.fecha).toLocaleString()}</span>
                    <span class="badge bg-${getBadgeClass(p.estado)}">${p.estado}</span>
                </div>
                ${p.detalles.map(d => `<div style="font-size:0.9rem; margin-bottom:5px;">- ${d.nombreProducto} (x${d.cantidad}): S/ ${d.subtotal.toFixed(2)}</div>`).join('')}
                <div style="text-align:right; margin-top:10px; font-weight:bold;">Total: S/ ${p.total.toFixed(2)}</div>
            </div>
        `).join('');
}

function getBadgeClass(estado) {
    if (estado === 'PENDIENTE') return 'pending'; if (estado === 'PAGADO') return 'paid';
    if (estado === 'ENVIADO') return 'sent'; return 'delivered';
}
function cerrarPedidos() { document.getElementById('pedidosModal').classList.add('hidden'); }

// --- ADMIN PANEL ---
async function cargarProductosAdmin() {
    const res = await fetch(`${API}/productos/todos`);
    const productos = await res.json();
    const tbody = document.getElementById('adminTableBody');
    tbody.innerHTML = productos.map(p => `
        <tr>
            <td>${p.id}</td><td>${p.nombre}</td><td>${p.categoria}</td>
            <td>S/ ${p.precio.toFixed(2)}</td><td>${p.stock}</td>
            <td class="${p.activo ? 'status-active' : 'status-inactive'}">${p.activo ? 'Activo' : 'Inactivo'}</td>
            <td>
                ${p.activo ? 
                    `<button onclick="toggleProducto(${p.id}, 'desactivar')" style="background:#e74c3c;color:white;padding:5px;border:none;border-radius:3px;cursor:pointer;">Desactivar</button>` : 
                    `<button onclick="toggleProducto(${p.id}, 'activar')" style="background:#27ae60;color:white;padding:5px;border:none;border-radius:3px;cursor:pointer;">Activar</button>`
                }
            </td>
        </tr>
    `).join('');
}

async function toggleProducto(id, accion) {
    await fetch(`${API}/productos/${id}/${accion}`, { method: 'PUT' });
    cargarProductosAdmin();
    cargarProductos(); // Actualiza la vista pública también
}

// Init
window.onload = checkSession;
