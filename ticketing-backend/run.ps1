param(
    [switch]$ResetDatabase
)

# Script para iniciar la aplicación. La base de datos se conserva por defecto.
Write-Host "=== Iniciando Ticketing Backend ===" -ForegroundColor Cyan

# 1. Detener contenedores (sin borrar volumen por defecto)
Write-Host "`n[1/5] Deteniendo contenedores Docker..." -ForegroundColor Yellow
if ($ResetDatabase) {
    Write-Host "    ResetDatabase activado: se eliminará el volumen de PostgreSQL" -ForegroundColor DarkYellow
    docker compose down -v 2>$null
} else {
    docker compose down 2>$null
}

# 2. Iniciar PostgreSQL
Write-Host "[2/5] Iniciando PostgreSQL..." -ForegroundColor Yellow
docker compose up -d

# 3. Esperar a que PostgreSQL esté listo
Write-Host "[3/5] Esperando a que PostgreSQL esté listo..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Verificar que PostgreSQL está corriendo
$container = docker ps --filter "name=ticketing-postgres" --format "{{.Names}}" 2>$null
if ($container -eq "ticketing-postgres") {
    Write-Host "    ✓ PostgreSQL está corriendo" -ForegroundColor Green
} else {
    Write-Host "    ✗ ERROR: PostgreSQL no se inició correctamente" -ForegroundColor Red
    exit 1
}

# 4. Compilar el proyecto
Write-Host "[4/5] Compilando el proyecto..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "    ✓ Compilación exitosa" -ForegroundColor Green
} else {
    Write-Host "    ✗ ERROR: Falló la compilación" -ForegroundColor Red
    exit 1
}

# 5. Ejecutar la aplicación
Write-Host "[5/5] Iniciando la aplicación..." -ForegroundColor Yellow
Write-Host "`nLa aplicación se está iniciando..." -ForegroundColor Cyan
Write-Host "Presiona Ctrl+C para detener la aplicación`n" -ForegroundColor Gray

$env:SPRING_PROFILES_ACTIVE = "local"
.\mvnw.cmd spring-boot:run
