# Arranque limpio de PO-01

Fecha: 26 de octubre de 2026, zona America/Lima.

Desde SW E3 Base de datos Moliselva se ejecutó:

```powershell
docker compose down -v
docker compose up -d --build
```

Compose eliminó los contenedores api y db, la red y el volumen moliselva-demo_postgres_data. Después creó un volumen nuevo, dejó db Healthy y arrancó api.

La prueba backend/smoke-po01.ps1 -OnlyAuth pasó con operario_demo, jefe_demo, contadora_demo y admin_demo; salida en login-arranque-limpio-2026-10-26.txt.

El smoke completo antes de limpiar la base terminó con PO-01: flujo completo verificado; salida en smoke-po01-2026-10-26.txt.
