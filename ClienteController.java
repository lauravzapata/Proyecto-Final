package co.edu.uniquindio.poo.monedero.controller;

import co.edu.uniquindio.poo.monedero.model.Cliente;
import co.edu.uniquindio.poo.monedero.model.MonederoVirtual;

import java.util.List;

public class ClienteController {

    private final MonederoVirtual monederoVirtual;

    public ClienteController(MonederoVirtual monederoVirtual) {
        this.monederoVirtual = monederoVirtual;
    }

    public Cliente registrarCliente(String documento, String nombre) {
        return monederoVirtual.registrarCliente(documento, nombre);
    }

    public void editarNombre(Cliente cliente, String nombre) {
        monederoVirtual.editarNombre(cliente, nombre);
    }

    public void eliminarCliente(String documento) {
        monederoVirtual.eliminarCliente(documento);
    }

    public List<Cliente> obtenerClientes() {
        return monederoVirtual.getListaClientes();
    }
}