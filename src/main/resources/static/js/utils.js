const API='/api';
function auth(){try{return JSON.parse(localStorage.getItem('authData')||'null')}catch{return null}}
function token(){return auth()?.token||null}
function isAdmin(){return auth()?.rol==='ADMIN'}
function money(v){return new Intl.NumberFormat('es-PE',{style:'currency',currency:'PEN'}).format(Number(v||0))}
function esc(v){return String(v??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]))}
function saveAuth(d){localStorage.setItem('authData',JSON.stringify(d))}
function logout(){localStorage.removeItem('authData');location.href='/login.html'}
async function api(url,opts={}){opts.headers={...(opts.headers||{}),...(token()?{Authorization:'Bearer '+token()}:{}),...(opts.body&&typeof opts.body!=='string'?{'Content-Type':'application/json'}:{})};if(opts.body&&typeof opts.body!=='string')opts.body=JSON.stringify(opts.body);const r=await fetch(url,opts);if(r.status===401&&token()){localStorage.removeItem('authData');location.href='/login.html';throw new Error('Sesion expirada')}return r}
async function json(url,opts){const r=await api(url,opts);let d=null;try{d=await r.json()}catch{}if(!r.ok)throw new Error(d?.mensaje||d?.message||d?.error||Object.values(d?.errores||{})[0]||'No se pudo completar la operación');return d}
function alertBox(msg,type='ok'){const el=document.getElementById('alerts');if(!el)return;const x=document.createElement('div');x.className='alert '+(type==='error'?'error':'ok');x.textContent=msg;el.appendChild(x);setTimeout(()=>x.remove(),4500)}
function nav(active=''){const a=auth();const links=[['/index.html','Productos','index'],['/carrito.html','Carrito','carrito'],['/pedidos.html','Pedidos','pedidos'],['/perfil.html','Mi perfil','perfil']];if(a?.rol==='ADMIN')links.push(['/admin.html','Administración','admin']);document.getElementById('navlinks').innerHTML=links.map(x=>`<a class="${active===x[2]?'active':''}" href="${x[0]}">${x[1]}</a>`).join('')+(a?`<span class="muted">${esc(a.nombre)}</span><button class="btn btn-sm" onclick="logout()">Salir</button>`:`<a class="btn btn-primary btn-sm" href="/login.html">Ingresar</a>`)}
function requireAuth(){if(!auth()){location.href='/login.html';return false}return true}
function requireAdmin(){if(!isAdmin()){location.href='/index.html';return false}return true}
function statusClass(s){return s==='CONFIRMADO'||s==='ENTREGADO'?'ok':(s==='CANCELADO'||s==='RECHAZADO'?'bad':'warn')}
