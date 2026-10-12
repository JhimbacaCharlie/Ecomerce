package com.ecommerce.config;

import com.ecommerce.entity.Categoria;
import com.ecommerce.entity.Cliente;
import com.ecommerce.entity.Producto;
import com.ecommerce.entity.Usuario;
import com.ecommerce.enums.Rol;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.repository.ClienteRepository;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearUsuariosIniciales();
        crearCategoriasSiNoExisten();
        crearProductosSiNoExisten();
    }

    private void crearUsuariosIniciales() {
        asegurarUsuario("Administrador TechStore", "admin@ecommerce.com", "admin123", Rol.ADMIN,
                "Av. Principal Central 100 - TechStore HQ", "999888777");
        asegurarUsuario("Melandy Garcia", "melandy@gmail.com", "123456", Rol.CLIENTE,
                "Jr. Ayacucho 450, Huamanga", "987654321");
    }

    /**
     * Crea el usuario si no existe. Si ya existe (por ejemplo importado desde el .sql con un hash
     * invalido), repara la contrasena y el rol para que el login funcione siempre.
     */
    private void asegurarUsuario(String nombre, String email, String passwordPlano, Rol rol,
                                 String direccion, String telefono) {
        Usuario u = usuarioRepository.findByEmail(email).orElse(null);
        if (u == null) {
            u = new Usuario();
            u.setNombre(nombre);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(passwordPlano));
            u.setRol(rol);
            u = usuarioRepository.save(u);
        } else {
            boolean cambio = false;
            if (u.getPassword() == null || !passwordEncoder.matches(passwordPlano, u.getPassword())) {
                u.setPassword(passwordEncoder.encode(passwordPlano));
                cambio = true;
            }
            if (u.getRol() != rol) {
                u.setRol(rol);
                cambio = true;
            }
            if (cambio) u = usuarioRepository.save(u);
        }
        if (clienteRepository.findByUsuarioId(u.getId()).isEmpty()) {
            Cliente c = new Cliente();
            c.setUsuario(u);
            c.setDireccion(direccion);
            c.setTelefono(telefono);
            clienteRepository.save(c);
        }
    }

    private void crearCategoriasSiNoExisten() {
        crearCategoriaSiNoExiste("Electronica");
        crearCategoriaSiNoExiste("Ropa");
        crearCategoriaSiNoExiste("Hogar");
        crearCategoriaSiNoExiste("Deportes");
        crearCategoriaSiNoExiste("Libros");
    }

    private void crearCategoriaSiNoExiste(String nombre) {
        if (categoriaRepository.findByNombre(nombre).isEmpty()) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoriaRepository.save(categoria);
        }
    }

    private void crearProductosSiNoExisten() {
        if (productoRepository.count() > 0) return;

        Categoria electronica = categoriaRepository.findByNombre("Electronica").orElse(null);
        Categoria ropa = categoriaRepository.findByNombre("Ropa").orElse(null);
        Categoria hogar = categoriaRepository.findByNombre("Hogar").orElse(null);
        Categoria deportes = categoriaRepository.findByNombre("Deportes").orElse(null);
        Categoria libros = categoriaRepository.findByNombre("Libros").orElse(null);

        crearProducto("Smartphone Galaxy S24 Ultra", "Telefono inteligente de ultima generacion con IA integrada, pantalla AMOLED 6.8\" y camara 200MP.", new BigDecimal("3799.00"), new BigDecimal("2650.00"), 25, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600&h=600&fit=crop", electronica);
        crearProducto("Laptop Pro 15 Gamer RTX 4060", "Laptop profesional Intel Core i7 13va gen, 32GB RAM, 1TB SSD NVMe, pantalla 165Hz.", new BigDecimal("4899.00"), new BigDecimal("3420.00"), 15, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600&h=600&fit=crop", electronica);
        crearProducto("Audifonos Bluetooth Sony ANC", "Audifonos inalambricos con cancelacion activa de ruido y autonomia de 30 horas.", new BigDecimal("649.00"), new BigDecimal("420.00"), 40, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&h=600&fit=crop", electronica);
        crearProducto("Camiseta Algodon Organico Tech", "Camiseta 100% algodon organico suave, transpirable y costuras reforzadas.", new BigDecimal("49.90"), new BigDecimal("22.00"), 80, "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600&h=600&fit=crop", ropa);
        crearProducto("Jeans Slim Fit Denim Premium", "Jeans clasicos resistentes y comodos con acabado lavado moderno.", new BigDecimal("129.00"), new BigDecimal("65.00"), 50, "https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&h=600&fit=crop", ropa);
        crearProducto("Lampara LED Inteligente RGB", "Lampara de escritorio regulable con control por app y carga inalambrica integrada.", new BigDecimal("119.00"), new BigDecimal("58.00"), 35, "https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=600&h=600&fit=crop", hogar);
        crearProducto("Zapatillas Running Pro Sport", "Zapatillas ultraligeras con amortiguacion avanzada y suela antideslizante.", new BigDecimal("289.00"), new BigDecimal("155.00"), 30, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&h=600&fit=crop", deportes);
        crearProducto("Libro: Clean Code en Español", "Manual de desarrollo de software agil de Robert C. Martin.", new BigDecimal("99.00"), new BigDecimal("45.00"), 45, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=600&h=600&fit=crop", libros);
    }

    private void crearProducto(String nombre, String descripcion, BigDecimal precio, BigDecimal costo, int stock, String imagenUrl, Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setCostoUnitario(costo != null ? costo : precio.multiply(new BigDecimal("0.70")));
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);
        producto.setCategoria(categoria);
        productoRepository.save(producto);
    }
}