package com.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponse {
    private Long id;
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private String estado;
    private String direccionEnvio;
    private String nombreUsuario;
    private List<DetallePedidoResponse> detalles;

    public PedidoResponse() {}

    public PedidoResponse(Long id, LocalDateTime fechaPedido, BigDecimal total, String estado,
                          String direccionEnvio, String nombreUsuario, List<DetallePedidoResponse> detalles) {
        this.id = id;
        this.fechaPedido = fechaPedido;
        this.total = total;
        this.estado = estado;
        this.direccionEnvio = direccionEnvio;
        this.nombreUsuario = nombreUsuario;
        this.detalles = detalles;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public List<DetallePedidoResponse> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoResponse> detalles) { this.detalles = detalles; }
}