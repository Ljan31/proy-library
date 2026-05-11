package com.proyecto.fhce.library.services.loads;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;

/**
 * Motor de cálculo de multas por retraso en devoluciones.
 *
 * ── Reglas de negocio que implementa ────────────────────────────────────────
 *
 * 1. La multa comienza a correr el día siguiente a la fechaDevolucionEstimada.
 *
 * 2. Se cobra multaPorDia por cada día de retraso.
 *
 * 3. El tope de días cobrables es multaMaxDias (campo en
 * ConfiguracionPrestamo).
 * Una vez alcanzado, la multa queda CONGELADA en ese valor máximo.
 *
 * 4. El cálculo siempre usa la configuración que estaba vigente al momento
 * del préstamo (idConfigUsado guardado en el préstamo), no la actual.
 *
 * ── Ejemplo concreto (del enunciado) ────────────────────────────────────────
 *
 * fechaDevolucionEstimada = 5 mayo
 * multaMaxDias = 7
 * multaPorDia = Bs 3
 *
 * Hoy 6 mayo → diasRetraso = 1 → min(1, 7) × 3 = Bs 3
 * Hoy 7 mayo → diasRetraso = 2 → min(2, 7) × 3 = Bs 6
 * Hoy 8 mayo → diasRetraso = 3 → min(3, 7) × 3 = Bs 9
 * Hoy 12 mayo → diasRetraso = 7 → min(7, 7) × 3 = Bs 21 ← tope
 * Hoy 20 mayo → diasRetraso =15 → min(15,7) × 3 = Bs 21 ← congelado
 *
 * ── Cuándo se activa la suspensión ──────────────────────────────────────────
 *
 * Cuando diasRetraso > multaMaxDias el usuario entra en suspensión.
 * La multa ya no aumenta pero la suspensión se activa por diasSuspension días.
 *
 * Esta clase es PURA (sin anotaciones Spring, sin efectos secundarios).
 * Se puede testear con un JUnit sin levantar contexto.
 */
public final class CalculadoraMultas {

  private CalculadoraMultas() {
    // Clase utilitaria — no instanciar
  }

  // ─────────────────────────────────────────────────────────────────────────
  // API pública
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Calcula los días de retraso entre la fecha estimada de devolución y hoy
   * (o la fecha real si el libro ya fue devuelto).
   *
   * @param fechaDevolucionEstimada Fecha límite original del préstamo.
   * @param fechaDevolucionReal     null si el libro aún no fue devuelto
   *                                (se usa LocalDate.now() como referencia).
   * @return Días de retraso. 0 o negativo significa que no hay retraso.
   */
  public static int calcularDiasRetraso(
      LocalDate fechaDevolucionEstimada,
      LocalDate fechaDevolucionReal) {
    LocalDate referencia = (fechaDevolucionReal != null)
        ? fechaDevolucionReal
        : LocalDate.now();

    return (int) ChronoUnit.DAYS.between(fechaDevolucionEstimada, referencia);
  }

  /**
   * Calcula el monto de multa acumulado dado un número de días de retraso
   * y la configuración histórica del préstamo.
   *
   * Fórmula: min(diasRetraso, multaMaxDias) × multaPorDia
   *
   * @param diasRetraso Días transcurridos desde la fecha estimada.
   * @param config      Configuración de préstamo (histórica, guardada en el
   *                    préstamo).
   * @return Monto en Bs. Nunca negativo. BigDecimal.ZERO si no aplica multa.
   */
  public static BigDecimal calcularMonto(int diasRetraso, ConfiguracionPrestamo config) {
    if (diasRetraso <= 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal multaPorDia = config.getMultaPorDia();
    Integer multaMaxDias = config.getMultaMaxDias();

    // Sin multa configurada
    if (multaPorDia == null || multaPorDia.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    // Sin tope configurado → cobrar todos los días
    int diasCobrables = (multaMaxDias == null || multaMaxDias <= 0)
        ? diasRetraso
        : Math.min(diasRetraso, multaMaxDias);

    return multaPorDia.multiply(BigDecimal.valueOf(diasCobrables));
  }

  /**
   * Determina si el usuario debe ser suspendido según los días de retraso.
   * La suspensión se activa cuando diasRetraso SUPERA multaMaxDias.
   *
   * Con el ejemplo: multaMaxDias = 7
   * - 7 días → NO suspensión (está en el límite, solo multa máxima)
   * - 8 días → SÍ suspensión
   *
   * @param diasRetraso Días de retraso calculados.
   * @param config      Configuración histórica del préstamo.
   * @return true si corresponde aplicar suspensión.
   */
  public static boolean debeSuspender(int diasRetraso, ConfiguracionPrestamo config) {
    Integer multaMaxDias = config.getMultaMaxDias();

    // Sin tope configurado → nunca se suspende automáticamente
    if (multaMaxDias == null || multaMaxDias <= 0) {
      return false;
    }

    return diasRetraso > multaMaxDias;
  }

  /**
   * Calcula el monto actual de una sanción ya existente sin modificarla.
   * Útil para mostrar al usuario "cuánto deberías pagar si pagas hoy".
   *
   * Respeta el tope: si ya pasó el límite, devuelve el monto máximo.
   *
   * @param fechaDevolucionEstimada Fecha límite original del préstamo.
   * @param config                  Configuración histórica del préstamo.
   * @return Monto acumulado a la fecha actual.
   */
  public static BigDecimal calcularMontoActual(
      LocalDate fechaDevolucionEstimada,
      ConfiguracionPrestamo config) {
    int diasRetraso = calcularDiasRetraso(fechaDevolucionEstimada, null);
    return calcularMonto(diasRetraso, config);
  }

  /**
   * Monto máximo posible que puede alcanzar una multa con esta configuración.
   * Sirve para mostrar al usuario el "peor caso".
   *
   * @param config Configuración del préstamo.
   * @return Monto tope, o null si no hay límite configurado.
   */
  public static BigDecimal calcularMontoMaximo(ConfiguracionPrestamo config) {
    if (config.getMultaPorDia() == null || config.getMultaMaxDias() == null) {
      return null;
    }
    return config.getMultaPorDia().multiply(BigDecimal.valueOf(config.getMultaMaxDias()));
  }

  /**
   * Indica si la multa ya alcanzó su tope máximo (está congelada).
   *
   * @param diasRetraso Días de retraso actuales.
   * @param config      Configuración histórica del préstamo.
   * @return true si la multa está congelada en su valor máximo.
   */
  public static boolean multaCongelada(int diasRetraso, ConfiguracionPrestamo config) {
    Integer multaMaxDias = config.getMultaMaxDias();
    if (multaMaxDias == null || multaMaxDias <= 0)
      return false;
    return diasRetraso >= multaMaxDias;
  }
}