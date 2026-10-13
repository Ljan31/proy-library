package com.proyecto.fhce.library.services;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class DocxToPdfConverter {

  // private static final Logger log =
  // LoggerFactory.getLogger(DocxToPdfConverter.class);

  /**
   * Convierte un .docx (como bytes) a PDF (como bytes).
   * Usa docx4j-export-fo con Apache FOP internamente.
   * No escribe nada al disco — todo en memoria.
   *
   * @param docxBytes bytes del documento .docx ya procesado (con variables
   *                  reemplazadas)
   * @return bytes del PDF generado
   */
  public byte[] convertir(byte[] docxBytes) throws Exception, IOException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(docxBytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(bis);

      // Docx4J.toPDF usa Apache FOP bajo el capó
      // El flag EXPORT_PREFER_XSL es el modo estándar para conversión via FO
      Docx4J.toPDF(wordPackage, bos);

      return bos.toByteArray();
    }
  }
}