package com.ecommerce.config;

import com.ecommerce.entity.Categoria;
import com.ecommerce.entity.Producto;
import com.ecommerce.entity.Usuario;
import com.ecommerce.enums.Rol;
import com.ecommerce.repository.CategoriaRepository;
import com.ecommerce.repository.ProductoRepository;
import com.ecommerce.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearAdminSiNoExiste();
        crearCategoriasSiNoExisten();
        crearProductosSiNoExisten();
    }

    private void crearAdminSiNoExiste() {
        if (!usuarioRepository.existsByEmail("admin@ecommerce.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(Rol.ADMIN);
            usuarioRepository.save(admin);
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

        crearProducto("Smartphone Galaxy S24", "Telefono inteligente de ultima generacion.", new BigDecimal("799.99"), 50, "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop", electronica);
        crearProducto("Laptop Pro 15", "Laptop profesional con procesador i7.", new BigDecimal("1299.99"), 30, "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop", electronica);
        crearProducto("Audifonos Bluetooth", "Audifonos inalambricos con cancelacion de ruido.", new BigDecimal("149.99"), 100, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop", electronica);
        crearProducto("Camiseta Algodon", "Camiseta de algodon organico.", new BigDecimal("29.99"), 200, "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop", ropa);
        crearProducto("Jeans Slim Fit", "Jeans de mezclilla premium.", new BigDecimal("59.99"), 150, "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=400&fit=crop", ropa);
        crearProducto("Lampara LED", "Lampara moderna con luz regulable.", new BigDecimal("45.99"), 80, "https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=400&h=400&fit=crop", hogar);
        crearProducto("Zapatillas Running", "Zapatillas con amortiguacion avanzada.", new BigDecimal("119.99"), 90, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop", deportes);
        crearProducto("Libro: Clean Code", "Guia para escribir codigo limpio.", new BigDecimal("34.99"), 120, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=400&fit=crop", libros);
    }

    private void crearProducto(String nombre, String descripcion, BigDecimal precio, int stock, String imagenUrl, Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);
        producto.setCategoria(categoria);
        productoRepository.save(producto);
    }
}