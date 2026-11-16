package com.proyecto.fhce.library.iteracion3.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.services.loads.CalculadoraMultas;

/**
 * Iteración 3 · Motor de cálculo de multas (clase pura, sin Spring ni BD).
 * Caso base del enunciado: multa Bs 3 por día, tope de 7 días.
 */
@DisplayName("Iteración 3 · CalculadoraMultas (unitaria)")
class CalculadoraMultasTest {

  private ConfiguracionPrestamo config(String multaPorDia, Integer maxDias) {
    ConfiguracionPrestamo c = new ConfiguracionPrestamo();
    c.setMultaPorDia(multaPorDia == null ? null : new BigDecimal(multaPorDia));
    c.setMultaMaxDias(maxDias);
    return c;
  }

  @Test
  @DisplayName("IT3-U-MUL-01: días de retraso = fecha real - fecha estimada")
  void diasRetraso_conFechaReal() {
    LocalDate estimada = LocalDate.of(2026, 5, 5);
    assertThat(CalculadoraMultas.calcularDiasRetraso(estimada, LocalDate.of(2026, 5, 8))).isEqualTo(3);
  }

  @Test
  @DisplayName("IT3-U-MUL-02: sin fecha real se usa la fecha de hoy como referencia")
  void diasRetraso_sinFechaReal_usaHoy() {
    assertThat(CalculadoraMultas.calcularDiasRetraso(LocalDate.now().minusDays(4), null)).isEqualTo(4);
  }

  @Test
  @DisplayName("IT3-U-MUL-03: devolver a tiempo o antes da 0 o negativo (sin retraso)")
  void diasRetraso_aTiempo() {
    LocalDate estimada = LocalDate.of(2026, 5, 5);
    assertThat(CalculadoraMultas.calcularDiasRetraso(estimada, estimada)).isZero();
    assertThat(CalculadoraMultas.calcularDiasRetraso(estimada, estimada.minusDays(2))).isNegative();
  }

  @Test
  @DisplayName("IT3-U-MUL-04: monto = días x multa por día mientras no se llegue al tope")
  void monto_bajoElTope() {
    assertThat(CalculadoraMultas.calcularMonto(3, config("3.00", 7))).isEqualByComparingTo("9");
  }

  @Test
  @DisplayName("IT3-U-MUL-05: el monto queda congelado en el tope (15 días -> 7 x 3 = 21)")
  void monto_conTope() {
    assertThat(CalculadoraMultas.calcularMonto(7, config("3.00", 7))).isEqualByComparingTo("21");
    assertThat(CalculadoraMultas.calcularMonto(15, config("3.00", 7))).isEqualByComparingTo("21");
  }

  @Test
  @DisplayName("IT3-U-MUL-06: sin retraso o sin multa configurada el monto es cero")
  void monto_cero() {
    assertThat(CalculadoraMultas.calcularMonto(0, config("3.00", 7))).isEqualByComparingTo("0");
    assertThat(CalculadoraMultas.calcularMonto(-2, config("3.00", 7))).isEqualByComparingTo("0");
    assertThat(CalculadoraMultas.calcularMonto(5, config(null, 7))).isEqualByComparingTo("0");
    assertThat(CalculadoraMultas.calcularMonto(5, config("0.00", 7))).isEqualByComparingTo("0");
  }

  @Test
  @DisplayName("IT3-U-MUL-07: sin tope configurado se cobran todos los días")
  void monto_sinTope() {
    assertThat(CalculadoraMultas.calcularMonto(10, config("2.00", null))).isEqualByComparingTo("20");
    assertThat(CalculadoraMultas.calcularMonto(10, config("2.00", 0))).isEqualByComparingTo("20");
  }

  @Test
  @DisplayName("IT3-U-MUL-08: la suspensión se activa solo cuando el retraso SUPERA el tope")
  void debeSuspender_limite() {
    ConfiguracionPrestamo c = config("3.00", 7);
    assertThat(CalculadoraMultas.debeSuspender(7, c)).isFalse();
    assertThat(CalculadoraMultas.debeSuspender(8, c)).isTrue();
  }

  @Test
  @DisplayName("IT3-U-MUL-09: sin tope configurado nunca se suspende")
  void debeSuspender_sinTope() {
    assertThat(CalculadoraMultas.debeSuspender(100, config("3.00", null))).isFalse();
    assertThat(CalculadoraMultas.debeSuspender(100, config("3.00", 0))).isFalse();
  }

  @Test
  @DisplayName("IT3-U-MUL-10: la multa está congelada al alcanzar el tope")
  void multaCongelada() {
    ConfiguracionPrestamo c = config("3.00", 7);
    assertThat(CalculadoraMultas.multaCongelada(6, c)).isFalse();
    assertThat(CalculadoraMultas.multaCongelada(7, c)).isTrue();
    assertThat(CalculadoraMultas.multaCongelada(9, c)).isTrue();
    assertThat(CalculadoraMultas.multaCongelada(9, config("3.00", null))).isFalse();
  }

  @Test
  @DisplayName("IT3-U-MUL-11: monto máximo = multa por día x tope; null si falta alguno")
  void montoMaximo() {
    assertThat(CalculadoraMultas.calcularMontoMaximo(config("3.00", 7))).isEqualByComparingTo("21");
    assertThat(CalculadoraMultas.calcularMontoMaximo(config(null, 7))).isNull();
    assertThat(CalculadoraMultas.calcularMontoMaximo(config("3.00", null))).isNull();
  }

  @Test
  @DisplayName("IT3-U-MUL-12: monto actual calculado a partir de hoy")
  void montoActual() {
    assertThat(CalculadoraMultas.calcularMontoActual(LocalDate.now().minusDays(2), config("3.00", 7)))
        .isEqualByComparingTo("6");
    assertThat(CalculadoraMultas.calcularMontoActual(LocalDate.now().plusDays(2), config("3.00", 7)))
        .isEqualByComparingTo("0");
  }
}
