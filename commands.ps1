docker compose -down -v
docker compose -up -d
docker exec -it reception_postgres psql -U ${DB_USER} -d reception_db