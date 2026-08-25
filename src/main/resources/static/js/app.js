const API = 'http://localhost:8080/api';
let userSession = { loggedIn: false, role: '', name: '' };

// Validar Sesión al cargar
async function checkSession() {
    const auth = getAuthData();
    if (auth && auth.token) {
        userSession = { loggedIn: true, role: auth.rol, name: auth.nombre };
        updateUI();
        cargarProductos();
    } else {
        userSession = { loggedIn: false, role: '', name: '' };
        updateUI();
        mostrarLogin(); // Forzar login a pantalla completa
    }
}

function updateUI() {
    if (userSession.loggedIn) {
        document.getElementById('loginSection').style.display = 'none';
        document.getElementById('registroSection').style.display = 'none';
        document.getElementById('mainApp').style.display = 'block';
        document.getElementById('userName').textContent = `Hola, ${userSession.name}`;
        
        if (userSession.role === 'ADMIN') {
            document.getElementById('navAdmin').style.display = 'inline-block';
            verAdminPanel(); // Redirigir directamente al panel admin
        } else {
            document.getElementById('navAdmin').style.display = 'none';
            verCatalogo(); // Usuarios normales ven el catálogo
        }
    } else {
        document.getElementById('mainApp').style.display = 'none';
        document.getElementById('registroSection').style.display = 'none';
        document.getElementById('loginSection').style.display = 'flex';
    }
}

function verCatalogo() {
    document.getElementById('vistaAdmin').style.display = 'none';
    document.getElementById('vistaCatalogo').style.display = 'block';
    document.getElementById('navCatalogo').classList.add('active');
    document.getElementById('navAdmin').querySelector('a').classList.remove('active');
}

function verAdminPanel() {
    document.getElementById('vistaCatalogo').style.display = 'none';
    document.getElementById('vistaAdmin').style.display = 'block';
    document.getElementById('navAdmin').querySelector('a').classList.add('active');
    document.getElementById('navCatalogo').classList.remove('active');
    cargarProductosAdmin();
}

// --- PRODUCTOS ---
async function cargarProductos() {
    const res = await fetch(`${API}/productos`);
    const productos = await res.json();
    renderProductos(productos);
    cargarCategorias(); // Llamar a cargarCategorias independientemente
}

function renderProductos(productos) {
    const grid = document.getElementById('productosGrid');
    grid.innerHTML = productos.map(p => `
        <div class="card">
            <img class="card-img" src="${p.imagenUrl || 'https://images.unsplash.com/photo-1555664424-778a1e5e1b48?w=400&h=300&fit=crop'}" alt="${p.nombre}">
            <div class="card-body">
                <div class="card-category">${p.categoriaNombre || 'General'}</div>
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

async function cargarCategorias() {
    const res = await fetch(`${API}/productos/categorias`);
    const categorias = await res.json();
    const select = document.getElementById('categoriaFilter');
    select.innerHTML = '<option value="">Todas las categorías</option>' + 
        categorias.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
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
    await fetchWithAuth(`${API}/carrito/items`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productoId, cantidad: 1 })
    });
    alert('Producto añadido al carrito');
    actualizarContadorCarrito();
}

async function actualizarContadorCarrito() {
    if (!userSession.loggedIn) return;
    try {
        const res = await fetchWithAuth(`${API}/carrito`);
        const carrito = await res.json();
        document.getElementById('carritoCount').textContent = carrito.items ? carrito.items.length : 0;
    } catch (e) { console.log(e); }
}

async function verCarrito() {
    if (!userSession.loggedIn) return mostrarLogin();
    document.getElementById('carritoModal').classList.remove('hidden');
    const res = await fetchWithAuth(`${API}/carrito`);
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
    await fetchWithAuth(`${API}/carrito/items/${detalleId}`, {
        method: 'PUT', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cantidad: nuevaCantidad })
    });
    verCarrito();
}

async function eliminarDelCarrito(detalleId) {
    await fetchWithAuth(`${API}/carrito/items/${detalleId}`, { method: 'DELETE' });
    verCarrito();
    actualizarContadorCarrito();
}

async function vaciarCarrito() {
    if(confirm('¿Vaciar carrito?')) {
        await fetchWithAuth(`${API}/carrito`, { method: 'DELETE' });
        verCarrito();
        actualizarContadorCarrito();
    }
}

async function realizarPedido() {
    try {
        const res = await fetchWithAuth(`${API}/pedidos/confirmar`, { method: 'POST' });
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
function mostrarLogin() { 
    document.getElementById('registroSection').style.display = 'none';
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('loginSection').style.display = 'flex'; 
}
function cerrarLogin() { document.getElementById('loginSection').style.display = 'none'; }

function mostrarRegistro() { 
    document.getElementById('loginSection').style.display = 'none';
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('registroSection').style.display = 'flex'; 
}
function cerrarRegistro() { document.getElementById('registroSection').style.display = 'none'; }

async function login(e) {
    e.preventDefault();
    const dto = { email: document.getElementById('loginEmail').value, password: document.getElementById('loginPassword').value };
    try {
        const res = await fetch(`${API}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto) });
        if (res.ok) {
            const data = await res.json();
            setAuthData(data); // Guardar sesión
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
    if (res.ok) {
        const data = await res.json();
        setAuthData(data); // Auto-login después de registrarse
        cerrarRegistro(); 
        await checkSession();
    } else { const err = await res.json(); alert(err.error || 'Error al registrar'); }
}

async function cerrarSesion() {
    try { await fetch(`${API}/auth/logout`, { method: 'POST' }); } catch(e) {}
    localStorage.removeItem('authData');
    userSession = { loggedIn: false, role: '', name: '' };
    updateUI();
    document.getElementById('carritoCount').textContent = '0';
}

// --- PEDIDOS ---
async function verPedidos() {
    if (!userSession.loggedIn) return mostrarLogin();
    document.getElementById('pedidosModal').classList.remove('hidden');
    const res = await fetchWithAuth(`${API}/pedidos/mis-pedidos`);
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
    const res = await fetchWithAuth(`${API}/productos`);
    const productos = await res.json();
    const tbody = document.getElementById('adminTableBody');
    tbody.innerHTML = productos.map(p => `
        <tr>
            <td>${p.id}</td>
            <td>${p.nombre}</td>
            <td>S/ ${p.precio.toFixed(2)}</td>
            <td>${p.stock}</td>
            <td>${p.categoriaNombre || 'N/A'}</td>
            <td style="text-align:right;">
                <button onclick="abrirModalProducto(${p.id})" class="btn btn-sm btn-primary">Editar</button>
                <button onclick="eliminarProducto(${p.id})" class="btn btn-sm btn-danger">Eliminar</button>
            </td>
        </tr>
    `).join('');
}

async function abrirModalProducto(id = null) {
    const resCats = await fetch(`${API}/productos/categorias`);
    const categorias = await resCats.json();
    document.getElementById('prodCategoria').innerHTML = categorias.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');

    if (id) {
        document.getElementById('productoModalTitle').textContent = 'Editar Producto';
        const res = await fetch(`${API}/productos/${id}`);
        const p = await res.json();
        document.getElementById('prodId').value = p.id;
        document.getElementById('prodNombre').value = p.nombre;
        document.getElementById('prodDescripcion').value = p.descripcion || '';
        document.getElementById('prodPrecio').value = p.precio;
        document.getElementById('prodStock').value = p.stock;
        document.getElementById('prodCategoria').value = p.categoriaId || '';
        document.getElementById('prodImagen').value = p.imagenUrl || '';
    } else {
        document.getElementById('productoModalTitle').textContent = 'Nuevo Producto';
        document.getElementById('formProducto').reset();
        document.getElementById('prodId').value = '';
    }
    document.getElementById('productoModal').classList.remove('hidden');
}

function cerrarModalProducto() {
    document.getElementById('productoModal').classList.add('hidden');
}

async function guardarProducto(e) {
    e.preventDefault();
    const id = document.getElementById('prodId').value;
    const dto = {
        nombre: document.getElementById('prodNombre').value,
        descripcion: document.getElementById('prodDescripcion').value,
        precio: parseFloat(document.getElementById('prodPrecio').value),
        stock: parseInt(document.getElementById('prodStock').value),
        categoriaId: parseInt(document.getElementById('prodCategoria').value),
        imagenUrl: document.getElementById('prodImagen').value
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API}/productos/${id}` : `${API}/productos`;

    const res = await fetchWithAuth(url, {
        method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dto)
    });

    if (res.ok) {
        cerrarModalProducto();
        cargarProductosAdmin();
        cargarProductos();
    } else {
        alert('Error al guardar el producto');
    }
}

async function eliminarProducto(id) {
    if (confirm('¿Estás seguro de eliminar este producto?')) {
        const res = await fetchWithAuth(`${API}/productos/${id}`, { method: 'DELETE' });
        if (res.ok) {
            cargarProductosAdmin();
            cargarProductos();
        } else {
            alert('Error al eliminar');
        }
    }
}

// Init
window.onload = checkSession;
