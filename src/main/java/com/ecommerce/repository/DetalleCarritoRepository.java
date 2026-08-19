package com.ecommerce.repository;

import com.ecommerce.entity.DetalleCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {
}