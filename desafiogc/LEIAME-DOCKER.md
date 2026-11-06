# Sistema de Códigos de Serviço — Gerdau (Docker)

Sobe o sistema inteiro (aplicação + banco PostgreSQL já com o catálogo saneado) com um comando.

## Requisitos
Docker Desktop (Windows/Mac) ou Docker Engine + Compose (Linux). Nada mais: Java e Maven são usados dentro do build.

## Subir
```bash
cp .env.example .env        # no Windows: copy .env.example .env
# edite o .env e troque a senha do banco (letras e números)
docker compose up -d --build
```
- 1ª vez: o build leva alguns minutos (baixa dependências) e o banco restaura o catálogo (≈ 7,3 mil serviços, 111 compradores, 9,3 mil vínculos).
- Acompanhe: `docker compose logs -f` (procure `>>> Serviços restaurados: 7324` e `Started DesafiogcApplication`).
- Abra **http://localhost:8080**

## Limpeza dos almoços duplicados
O arquivo `db/02-limpar-almoco-duplicado.sql` roda sozinho na primeira subida e remove 8 serviços `ALMOCO PREMIUM` duplicados (séries 03 e 04), mantendo a série 02. Se o banco já estava criado antes, aplique uma vez:
```bash
docker compose exec -T db psql -U gerdau -d desafiogc < db/02-limpar-almoco-duplicado.sql
```

## Primeiro acesso
| Perfil | Login | Senha inicial |
|---|---|---|
| Administrador | `admin@gerdau.local` | `trocar-admin` |
| Comprador | `comprador@gerdau.local` | `trocar-comprador` |

**Troque essas senhas antes de usar de verdade** — o passo a passo está na Documentação Completa (seção "Trocar a senha do administrador").

## Comandos do dia a dia
```bash
docker compose ps                 # estado dos serviços
docker compose logs -f app        # log da aplicação
docker compose stop               # para (dados ficam guardados)
docker compose up -d              # volta
docker compose down               # remove containers (dados ficam no volume pgdata)
docker compose down -v            # APAGA tudo, inclusive o banco; na próxima subida restaura do zero
```

## Backup
```bash
docker compose exec db pg_dump -Fc -U gerdau desafiogc > backup_$(date +%F).dump
```

Documentação completa: veja o arquivo "Documentacao_Completa_Sistema" da entrega.
