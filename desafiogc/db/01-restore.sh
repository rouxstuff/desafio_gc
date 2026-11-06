#!/bin/bash
# Roda UMA vez, automaticamente, na primeira subida do banco (volume vazio).
# O Postgres executa tudo que está em /docker-entrypoint-initdb.d/.
DUMP=/docker-entrypoint-initdb.d/backup.dump

echo ">>> Restaurando $DUMP no banco $POSTGRES_DB ..."

# O banco novo já tem o schema "public"; tiramos essa linha da lista de restore
# para não gerar o aviso "schema public already exists".
pg_restore -l "$DUMP" | grep -v ' SCHEMA - public ' > /tmp/restore.list

pg_restore --no-owner --no-privileges \
  -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -L /tmp/restore.list "$DUMP" \
  || echo ">>> pg_restore terminou com avisos (verificando os dados abaixo)"

# Confere se os dados chegaram; se não, falha de propósito para você ver o erro.
QTD=$(psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -tAc "select count(*) from public.servico")
echo ">>> Serviços restaurados: $QTD"
if [ -z "$QTD" ] || [ "$QTD" -eq 0 ]; then
  echo ">>> ERRO: tabela servico vazia depois do restore" >&2
  exit 1
fi
