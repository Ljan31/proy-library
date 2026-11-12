-- Renombrar tabla loans a prestamos
ALTER TABLE loans
RENAME TO prestamos;

-- Renombrar restricción CHECK
ALTER TABLE prestamos
RENAME CONSTRAINT loans_tipo_documento_garantia_check
TO prestamos_tipo_documento_garantia_check;


-- Bibliotecas
ALTER TABLE libraries RENAME TO bibliotecas;

-- Categorías de libros
ALTER TABLE book_categories RENAME TO categorias_libro;

-- Certificados de no deuda
ALTER TABLE no_debt_certificates RENAME TO certificados_no_deuda;

-- Historial de estados de ejemplares
ALTER TABLE ejemplar_status_history RENAME TO historial_estados_ejemplar;

-- Libros
ALTER TABLE books RENAME TO libros;

-- Permisos
ALTER TABLE permissions RENAME TO permisos;

-- Personas
ALTER TABLE persons RENAME TO personas;

-- Razones de certificados
ALTER TABLE certificate_reasons RENAME TO razones_certificado;

