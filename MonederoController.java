package co.edu.uniquindio.poo.monedero.controller;

import co.edu.uniquindio.poo.monedero.model.Cliente;
import co.edu.uniquindio.poo.monedero.model.Monedero;
import co.edu.uniquindio.poo.monedero.model.MonederoVirtual;
import co.edu.uniquindio.poo.monedero.model.Transaccion;
import co.edu.uniquindio.poo.monedero.model.TransaccionProgramada;

import java.time.LocalDate;
import java.util.List;

public class MonederoController {

    private final MonederoVirtual monederoVirtual;

    public MonederoController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public String getNombre() {
        return monederoVirtual.getNombre();
    }

    public Monedero registrarMonedero(Cliente cliente, String nombre) {
        return monederoVirtual.registrarMonedero(cliente, nombre);
    }

    public Transaccion depositar(Cliente cliente,
                                 Monedero monedero,
                                 double monto) {
        return monederoVirtual.depositar(cliente, monedero, monto);
    }

    public Transaccion retirar(Cliente cliente,
                               Monedero monedero,
                               double monto) {
        return monederoVirtual.retirar(cliente, monedero, monto);
    }

    public Transaccion transferir(Cliente cliente,
                                  Monedero desde,
                                  Cliente destinatario,
                                  Monedero hacia,
                                  double monto) {
        return monederoVirtual.transferir(
                cliente, desde, destinatario, hacia, monto
        );
    }

    public void revertir(Cliente cliente, int codigo) {
        monederoVirtual.revertir(cliente, codigo);
    }

    public void canjear(Cliente cliente,
                        Monedero monedero,
                        int puntos) {
        monederoVirtual.canjear(cliente, monedero, puntos);
    }

    public TransaccionProgramada programar(
            String tipo,
            Cliente cliente,
            Monedero desde,
            Cliente destino,
            Monedero hacia,
            double monto,
            LocalDate fecha,
            String frecuencia) {

        return monederoVirtual.programar(
                tipo, cliente, desde, destino,
                hacia, monto, fecha, frecuencia
        );
    }

    public void cancelarProgramada(int codigo) {
        monederoVirtual.cancelarProgramada(codigo);
    }

    public String ejecutarPendientes(LocalDate hoy) {
        return monederoVirtual.ejecutarPendientes(hoy);
    }

    public String reporte(Cliente cliente) {
        return monederoVirtual.reporte(cliente);
    }

    public List<Cliente> obtenerClientes() {
        return monederoVirtual.getListaClientes();
    }

    public List<TransaccionProgramada> obtenerProgramadas() {
        return monederoVirtual.getListaProgramadas();
    }

    public List<String> obtenerNotificaciones() {
        return monederoVirtual.getListaNotificaciones();
    }
}