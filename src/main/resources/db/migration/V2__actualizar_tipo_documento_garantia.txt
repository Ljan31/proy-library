ALTER TABLE loans
DROP CONSTRAINT IF EXISTS loans_tipo_documento_garantia_check;

ALTER TABLE loans
ADD CONSTRAINT loans_tipo_documento_garantia_check
CHECK (
    tipo_documento_garantia IN (
        'CI',
        'MATRICULA',
        'CI_Y_MATRICULA'
    )
);