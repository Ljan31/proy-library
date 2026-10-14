package com.proyecto.fhce.library.services.library;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Component
public class QrGeneratorService {

  @Value("${app.certificados.url-verificacion-base:http://localhost:8098/certificados}")
  private String urlBase;

  /**
   * Genera un QR como PNG en memoria para el código de verificación dado.
   * La URL codificada en el QR apunta a la pantalla de validación del frontend.
   *
   * @param codigoVerificacion código único del certificado
   * @return bytes PNG del QR
   */
  public byte[] generarQr(String codigoVerificacion) throws WriterException, IOException {
    String url = urlBase + "?tab=validar&codigo=" + codigoVerificacion;
    Map<EncodeHintType, Object> hints = Map.of(
        EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN, 1);

    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300, hints);

    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      MatrixToImageWriter.writeToStream(matrix, "PNG", bos);
      return bos.toByteArray();
    }
  }
}