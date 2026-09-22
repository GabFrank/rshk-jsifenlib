package com.roshka.sifen.test.fields;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regla 144 del Manual Tecnico v150: el rechazo 1862 compara
 *
 * <pre>[EA004 * 100 / E721]   contra   F010 (dPorcDescTotal)</pre>
 *
 * con una tolerancia de 0.8. F010 lo deriva TgTotSub de dDescTotal, que a su vez sale de
 * dTotDesc + dTotDescGlotem.
 *
 * EA004 (dDescGloItem) y EA002 (dDescItem) son los dos descuentos sobre el PRECIO UNITARIO,
 * asi que los dos totales tienen que ir multiplicados por dCantProSer. TgTotSub aplicaba el
 * multiply solo al primero, con lo cual el numerador de F010 quedaba sin ponderar mientras el
 * denominador (dTotOpe, suma de dTotOpeItem) si lo estaba.
 *
 * Estas pruebas reproducen la aritmetica de TgTotSub sin construir un DE completo: el calculo
 * vive dentro de setupSOAPElements, que necesita un SOAPElement y la firma. Lo que se fija aca
 * es la relacion entre las formulas, que es donde estaba el defecto.
 */
public class TgTotSubDescuentoGlobalTest {

    /** dTotOpeItem segun EA008: (precio - descuentos - anticipos) * cantidad. */
    private static BigDecimal totOpeItem(BigDecimal precioUnit, BigDecimal descGloItem, BigDecimal cantidad) {
        return precioUnit.subtract(descGloItem).multiply(cantidad);
    }

    /** F010 tal como lo calcula TgTotSub. */
    private static BigDecimal porcDescTotal(BigDecimal dTotDescGlotem, BigDecimal dTotOpe) {
        return dTotDescGlotem.multiply(BigDecimal.valueOf(100))
                .divide(dTotOpe.add(dTotDescGlotem), 2, RoundingMode.HALF_UP);
    }

    /** Ratio por item del lado izquierdo de la regla 144. */
    private static BigDecimal ratioItem(BigDecimal descGloItem, BigDecimal precioUnit) {
        return descGloItem.multiply(BigDecimal.valueOf(100))
                .divide(precioUnit, 2, RoundingMode.HALF_UP);
    }

    private static final BigDecimal TOLERANCIA = BigDecimal.valueOf(0.8);

    /**
     * Caso real de bodega, factura 517743: HIELO MEDIANO, cantidad 10, precio 5000,
     * descuento global de 25000 sobre la factura -> EA004 = 2500 por unidad.
     */
    @Test
    public void reglaCierraConCantidadDistintaDeUno() {
        BigDecimal precio = BigDecimal.valueOf(5000);
        BigDecimal cantidad = BigDecimal.valueOf(10);
        BigDecimal descGloItem = BigDecimal.valueOf(2500);

        BigDecimal dTotOpe = totOpeItem(precio, descGloItem, cantidad);
        BigDecimal dTotDescGlotem = descGloItem.multiply(cantidad);   // con el fix

        BigDecimal desvio = ratioItem(descGloItem, precio)
                .subtract(porcDescTotal(dTotDescGlotem, dTotOpe)).abs();

        Assert.assertTrue("desvio " + desvio + " supera la tolerancia de la regla 144",
                desvio.compareTo(TOLERANCIA) <= 0);
    }

    /** Sin el multiply, el mismo caso se va 41 puntos: es el rechazo 1862. */
    @Test
    public void sinElMultiplyLaReglaNoCierra() {
        BigDecimal precio = BigDecimal.valueOf(5000);
        BigDecimal cantidad = BigDecimal.valueOf(10);
        BigDecimal descGloItem = BigDecimal.valueOf(2500);

        BigDecimal dTotOpe = totOpeItem(precio, descGloItem, cantidad);
        BigDecimal dTotDescGlotemViejo = descGloItem;                 // sin el fix

        BigDecimal desvio = ratioItem(descGloItem, precio)
                .subtract(porcDescTotal(dTotDescGlotemViejo, dTotOpe)).abs();

        Assert.assertTrue("sin el multiply el desvio deberia superar la tolerancia",
                desvio.compareTo(TOLERANCIA) > 0);
    }

    /** Con cantidad 1 las dos formulas coinciden: por eso el defecto vivio sin detectarse. */
    @Test
    public void conCantidadUnoNoHayDiferencia() {
        BigDecimal precio = BigDecimal.valueOf(5500);
        BigDecimal cantidad = BigDecimal.ONE;
        BigDecimal descGloItem = BigDecimal.valueOf(500);

        BigDecimal dTotOpe = totOpeItem(precio, descGloItem, cantidad);

        Assert.assertEquals(
                porcDescTotal(descGloItem.multiply(cantidad), dTotOpe),
                porcDescTotal(descGloItem, dTotOpe));
    }

    /** Varias lineas con cantidades distintas: la regla tiene que cerrar en todas. */
    @Test
    public void reglaCierraConVariasLineasYCantidadesDistintas() {
        BigDecimal[][] items = {
                {BigDecimal.valueOf(5000),  BigDecimal.valueOf(10), BigDecimal.valueOf(500)},
                {BigDecimal.valueOf(12000), BigDecimal.valueOf(3),  BigDecimal.valueOf(1200)},
                {BigDecimal.valueOf(800),   BigDecimal.valueOf(25), BigDecimal.valueOf(80)},
        };

        BigDecimal dTotOpe = BigDecimal.ZERO;
        BigDecimal dTotDescGlotem = BigDecimal.ZERO;
        for (BigDecimal[] it : items) {
            dTotOpe = dTotOpe.add(totOpeItem(it[0], it[2], it[1]));
            dTotDescGlotem = dTotDescGlotem.add(it[2].multiply(it[1]));
        }
        BigDecimal f010 = porcDescTotal(dTotDescGlotem, dTotOpe);

        for (BigDecimal[] it : items) {
            BigDecimal desvio = ratioItem(it[2], it[0]).subtract(f010).abs();
            Assert.assertTrue("item de precio " + it[0] + ": desvio " + desvio,
                    desvio.compareTo(TOLERANCIA) <= 0);
        }
    }
}
