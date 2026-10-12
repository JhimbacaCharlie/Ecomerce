package com.ecommerce.repository;
import com.ecommerce.entity.Pedido; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface PedidoRepository extends JpaRepository<Pedido,Long>{ List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId); List<Pedido> findAllByOrderByFechaPedidoDesc(); long countByEstado(com.ecommerce.enums.EstadoPedido estado); long countByEstadoPago(com.ecommerce.enums.EstadoPago estado); }
