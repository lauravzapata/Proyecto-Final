package co.edu.uniquindio.poo.monedero.model;

public class Monedero {

    private String nombre;
    private double saldo;

    public Monedero(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public void depositar(double monto) {
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser positivo."
            );
        }

        monto = Math.round(monto * 100.0) / 100.0;

        if (monto == 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser de al menos un centavo."
            );
        }

        saldo = Math.round((saldo + monto) * 100.0) / 100.0;
    }

    public void retirar(double monto) {
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser positivo."
            );
        }

        monto = Math.round(monto * 100.0) / 100.0;

        if (monto == 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser de al menos un centavo."
            );
        }

        if (monto > saldo) {
            throw new IllegalStateException(
                    "No hay saldo suficiente en " + nombre + "."
            );
        }

        saldo = Math.round((saldo - monto) * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return nombre + " ($" + String.format("%.2f", saldo) + ")";
    }
}