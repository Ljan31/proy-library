package com.proyecto.fhce.library.services;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class DocxToPdfConverter {

  private static final Logger log = LoggerFactory.getLogger(DocxToPdfConverter.class);

  public byte[] convertir(byte[] docxBytes) throws Exception {

    long inicioTotal = System.nanoTime();

    try (ByteArrayInputStream bis = new ByteArrayInputStream(docxBytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

      long inicio = System.nanoTime();

      WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(bis);

      log.info(
          "[PDF-TIMING] Carga DOCX: {} ms",
          (System.nanoTime() - inicio) / 1_000_000.0);

      inicio = System.nanoTime();

      Docx4J.toPDF(wordPackage, bos);

      log.info(
          "[PDF-TIMING] docx4j + Apache FOP: {} ms",
          (System.nanoTime() - inicio) / 1_000_000.0);

      inicio = System.nanoTime();

      byte[] resultado = bos.toByteArray();

      log.info(
          "[PDF-TIMING] Extracción bytes PDF: {} ms",
          (System.nanoTime() - inicio) / 1_000_000.0);

      log.info(
          "[PDF-TIMING] Total conversor: {} ms",
          (System.nanoTime() - inicioTotal) / 1_000_000.0);

      return resultado;
    }
  }
}