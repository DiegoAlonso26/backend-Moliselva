param([switch]$OnlyAuth)
# Prueba HTTP de la demo PO-01 sobre datos locales de prueba.
# Usa DEMO_PASSWORD del .env local o de la carpeta hermana de la BD; nunca lo imprime.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $MyInvocation.MyCommand.Path)
$envPath = if (Test-Path '.env') { '.env' } else { '..\SW E3 Base de datos Moliselva\.env' }
$line = Get-Content -LiteralPath $envPath -Encoding UTF8 | Where-Object { $_.StartsWith('DEMO_PASSWORD=') } | Select-Object -First 1
if (-not $line) { throw 'Falta DEMO_PASSWORD en el archivo .env local o de la BD' }
$demoPassword = $line.Substring('DEMO_PASSWORD='.Length)
$baseUrl = 'http://127.0.0.1:8080/api'
Add-Type -AssemblyName System.Net.Http
$script:httpClient = New-Object System.Net.Http.HttpClient

function Call-Api([string]$method,[string]$path,[string]$token,[object]$body,[string]$origin='') {
    $request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::new($method), [Uri]($baseUrl+$path))
    if ($token) { [void]$request.Headers.TryAddWithoutValidation('Authorization',('Bearer '+$token)) }
    if ($origin) { [void]$request.Headers.TryAddWithoutValidation('Origin',$origin) }
    if ($null -ne $body) {
        $jsonText = $body | ConvertTo-Json -Depth 8 -Compress
        $request.Content = [System.Net.Http.ByteArrayContent]::new([Text.Encoding]::UTF8.GetBytes($jsonText))
        $request.Content.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/json; charset=utf-8')
    }
    $response = $null
    try {
        $response = $script:httpClient.SendAsync($request).GetAwaiter().GetResult()
        $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $json = $null
        if ($response.Content.Headers.ContentType.MediaType -eq 'application/json' -and $raw) {
            $json = $raw | ConvertFrom-Json
        }
        $exposed = if ($response.Headers.Contains('Access-Control-Expose-Headers')) {
            ($response.Headers.GetValues('Access-Control-Expose-Headers') -join ',')
        } else { '' }
        $allowedOrigin = if ($response.Headers.Contains('Access-Control-Allow-Origin')) {
            ($response.Headers.GetValues('Access-Control-Allow-Origin') -join ',')
        } else { '' }
        return [pscustomobject]@{ Status=[int]$response.StatusCode; Json=$json; Raw=$raw;
            ExposedHeaders=$exposed; AllowedOrigin=$allowedOrigin }
    } finally {
        if ($null -ne $response) { $response.Dispose() }
        $request.Dispose()
    }
}

function Expect([bool]$condition,[string]$message) {
    if (-not $condition) { throw "FALLÓ: $message" }
    Write-Host "OK: $message"
}

function Login([string]$username) {
    $result = Call-Api 'POST' '/auth/login' '' @{ usuario=$username; password=$demoPassword }
    Expect ($result.Status -eq 200 -and $result.Json.token) "login $username"
    return $result.Json
}

$operario = Login 'operario_demo'
$jefe = Login 'jefe_demo'
$contadora = Login 'contadora_demo'
$admin = Login 'admin_demo'
Expect ($operario.usuario.rol -eq 'OPERARIO' -and $admin.usuario.rol -eq 'ADMINISTRADOR') 'roles de la BD'
if ($OnlyAuth) {
    $audit = Call-Api 'GET' '/bitacora' $admin.token $null
    Expect ($audit.Status -eq 200 -and @($audit.Json).Count -ge 1) 'bitácora después del despliegue'
    Write-Host 'Autenticación y bitácora verificadas.'
    return
}
$anonymous = Call-Api 'GET' '/proveedores' '' $null
Expect ($anonymous.Status -eq 401 -and $anonymous.Json.codigo -eq 'NO_AUTENTICADO') 'ruta protegida sin token'

$newProvider = Call-Api 'POST' '/proveedores' $admin.token @{
    razonSocial='PROVEEDOR_CRUD_DE_PRUEBA'; documento='20123456789'; telefono='987654321'
}
Expect ($newProvider.Status -eq 201 -and $newProvider.Json.idProveedor -gt 0 -and
    $newProvider.Json.documento -eq '20123456789') 'administrador crea proveedor'
$crudId = $newProvider.Json.idProveedor
$providerDetail = Call-Api 'GET' ("/proveedores/{0}" -f $crudId) $operario.token $null
Expect ($providerDetail.Status -eq 200 -and $providerDetail.Json.razonSocial -eq 'PROVEEDOR_CRUD_DE_PRUEBA') 'operario consulta proveedor'
$providerDenied = Call-Api 'PUT' ("/proveedores/{0}" -f $crudId) $contadora.token @{
    razonSocial='SIN_PERMISO'
}
Expect ($providerDenied.Status -eq 403) 'contadora no modifica proveedor'
$providerUpdated = Call-Api 'PUT' ("/proveedores/{0}" -f $crudId) $jefe.token @{
    razonSocial='PROVEEDOR_CRUD_ACTUALIZADO'; documento='20123456789'; telefono=''
}
Expect ($providerUpdated.Status -eq 200 -and $providerUpdated.Json.razonSocial -eq 'PROVEEDOR_CRUD_ACTUALIZADO' -and
    $null -eq $providerUpdated.Json.telefono) 'jefe actualiza proveedor'
$providerList = Call-Api 'GET' '/proveedores' $operario.token $null
Expect (@($providerList.Json | Where-Object { $_.idProveedor -eq $crudId }).Count -eq 1) 'proveedor actualizado aparece en lista'
$deleteDenied = Call-Api 'DELETE' ("/proveedores/{0}" -f $crudId) $operario.token $null
Expect ($deleteDenied.Status -eq 403) 'operario no elimina proveedor'
$providerDeleted = Call-Api 'DELETE' ("/proveedores/{0}" -f $crudId) $admin.token $null
Expect ($providerDeleted.Status -eq 204) 'administrador desactiva proveedor'
$providerMissing = Call-Api 'GET' ("/proveedores/{0}" -f $crudId) $admin.token $null
Expect ($providerMissing.Status -eq 404 -and $providerMissing.Json.codigo -eq 'PROVEEDOR_NO_ENCONTRADO') 'proveedor desactivado no se consulta'
$providerList = Call-Api 'GET' '/proveedores' $admin.token $null
Expect (@($providerList.Json | Where-Object { $_.idProveedor -eq $crudId }).Count -eq 0) 'proveedor desactivado no aparece en lista'
$inactiveReception = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$crudId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.2; mermaLimpiezaKg=10
}
Expect ($inactiveReception.Status -eq 400 -and $inactiveReception.Json.codigo -eq 'PROVEEDOR_INVALIDO') 'proveedor desactivado no admite nuevas recepciones'
$providerAudit = Call-Api 'GET' '/bitacora' $admin.token $null
foreach ($action in @('CREAR_PROVEEDOR','ACTUALIZAR_PROVEEDOR','DESACTIVAR_PROVEEDOR')) {
    Expect (@($providerAudit.Json | Where-Object { $_.accion -eq ("{0} id={1}" -f $action,$crudId) }).Count -eq 1) "bitácora $action"
}
$badProvider = Call-Api 'POST' '/proveedores' $admin.token @{ razonSocial='   ' }
Expect ($badProvider.Status -eq 400 -and $badProvider.Json.codigo -eq 'DATOS_INVALIDOS') 'razón social obligatoria'

$proveedores = Call-Api 'GET' '/proveedores' $operario.token $null
Expect ($proveedores.Status -eq 200 -and @($proveedores.Json).Count -ge 1) 'listar proveedores'
$proveedorId = @($proveedores.Json)[0].idProveedor
$badDate = Call-Api 'GET' '/recepciones?fecha=ayer' $operario.token $null
Expect ($badDate.Status -eq 400 -and $badDate.Json.codigo -eq 'DATOS_INVALIDOS' -and
    $badDate.Json.mensaje.Contains('AAAA-MM-DD')) 'fecha inválida devuelve JSON estándar'
$stock0 = (Call-Api 'GET' '/inventario/maiz/stock' $operario.token $null).Json.stockKg
$missingZeroFields = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; humedadPct=14.2
}
Expect ($missingZeroFields.Status -eq 400 -and $missingZeroFields.Json.codigo -eq 'DATOS_INVALIDOS') 'sacos y merma deben enviarse, incluso si valen cero'
$sample = @{ idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200;
    pesoSacosKg=0; humedadPct=14.2; mermaLimpiezaKg=10 }
$adminDenied = Call-Api 'POST' '/recepciones' $admin.token $sample
Expect ($adminDenied.Status -eq 403) 'administrador no registra recepción'
$contadoraDenied = Call-Api 'POST' '/recepciones' $contadora.token $sample
Expect ($contadoraDenied.Status -eq 403) 'contadora no registra recepción'
$badHumidity = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=100.1; mermaLimpiezaKg=10
}
Expect ($badHumidity.Status -eq 400 -and $badHumidity.Json.codigo -eq 'HUMEDAD_FUERA_DE_RANGO') 'humedad inválida devuelve código claro'
$badWeight = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=200; taraKg=200; pesoSacosKg=0;
    humedadPct=14.2; mermaLimpiezaKg=0
}
Expect ($badWeight.Status -eq 400 -and $badWeight.Json.codigo -eq 'PESOS_INVALIDOS') 'peso neto no positivo'
$badMerma = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.2; mermaLimpiezaKg=800
}
Expect ($badMerma.Status -eq 400 -and $badMerma.Json.codigo -eq 'PESOS_INVALIDOS') 'merma no menor al neto'

$normal = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.2; mermaLimpiezaKg=10
}
Expect ($normal.Status -eq 201 -and $normal.Json.estado -eq 'ACEPTADA' -and
    [decimal]$normal.Json.pesoNetoKg -eq 800 -and [decimal]$normal.Json.entradaKardexKg -eq 790) 'recepción normal y merma'
$stock1 = (Call-Api 'GET' '/inventario/maiz/stock' $operario.token $null).Json.stockKg
Expect ([decimal]$stock1 -eq [decimal]$stock0 + 790) 'entrada automática al kardex'

$pending = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.8; mermaLimpiezaKg=10
}
Expect ($pending.Status -eq 201 -and $pending.Json.estado -eq 'PENDIENTE' -and
    $null -eq $pending.Json.entradaKardexKg) 'humedad excepcional pendiente'
$stock2 = (Call-Api 'GET' '/inventario/maiz/stock' $operario.token $null).Json.stockKg
Expect ([decimal]$stock2 -eq [decimal]$stock1) 'pendiente no altera stock'

$denied = Call-Api 'PATCH' ("/recepciones/{0}/decision" -f $pending.Json.idRecepcion) $operario.token @{
    decision='ACEPTAR'; motivo='Motivo de prueba'
}
Expect ($denied.Status -eq 403) 'operario no puede decidir'
$noReason = Call-Api 'PATCH' ("/recepciones/{0}/decision" -f $pending.Json.idRecepcion) $jefe.token @{
    decision='ACEPTAR'; motivo=''
}
Expect ($noReason.Status -eq 400 -and $noReason.Json.codigo -eq 'DATOS_INVALIDOS') 'motivo obligatorio'
$accepted = Call-Api 'PATCH' ("/recepciones/{0}/decision" -f $pending.Json.idRecepcion) $jefe.token @{
    decision='ACEPTAR'; motivo='Motivo de prueba'
}
Expect ($accepted.Status -eq 200 -and $accepted.Json.estado -eq 'ACEPTADA' -and
    [decimal]$accepted.Json.entradaKardexKg -eq 790) 'jefe acepta con motivo'
$stock3 = (Call-Api 'GET' '/inventario/maiz/stock' $contadora.token $null).Json.stockKg
Expect ([decimal]$stock3 -eq [decimal]$stock2 + 790) 'decisión genera una sola entrada'
$duplicate = Call-Api 'PATCH' ("/recepciones/{0}/decision" -f $pending.Json.idRecepcion) $jefe.token @{
    decision='ACEPTAR'; motivo='Repetición de prueba'
}
Expect ($duplicate.Status -eq 409) "segunda decisión rechazada (HTTP $($duplicate.Status), código $($duplicate.Json.codigo))"

$pendingReject = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.8; mermaLimpiezaKg=10
}
$chiefReject = Call-Api 'PATCH' ("/recepciones/{0}/decision" -f $pendingReject.Json.idRecepcion) $jefe.token @{
    decision='RECHAZAR'; motivo='Motivo de prueba'
}
Expect ($chiefReject.Status -eq 200 -and $chiefReject.Json.estado -eq 'RECHAZADA' -and
    $null -eq $chiefReject.Json.entradaKardexKg) 'jefe rechaza pendiente con motivo'
$stockAfterChiefReject = (Call-Api 'GET' '/inventario/maiz/stock' $operario.token $null).Json.stockKg
Expect ([decimal]$stockAfterChiefReject -eq [decimal]$stock3) 'rechazo del jefe no altera stock'

$rejected = Call-Api 'POST' '/recepciones' $operario.token @{
    idProveedor=$proveedorId; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=16; mermaLimpiezaKg=10
}
Expect ($rejected.Status -eq 201 -and $rejected.Json.estado -eq 'RECHAZADA') 'humedad mayor a 15 rechazada'
$stock4 = (Call-Api 'GET' '/inventario/maiz/stock' $operario.token $null).Json.stockKg
Expect ([decimal]$stock4 -eq [decimal]$stock3) 'rechazo no altera stock'

$kardex = Call-Api 'GET' '/inventario/maiz/kardex' $admin.token $null
Expect ($kardex.Status -eq 200 -and @($kardex.Json).Count -ge 2) 'kardex consultable'
Expect (@($kardex.Json | Where-Object { $_.idRecepcion -eq $pending.Json.idRecepcion }).Count -eq 1) 'una entrada por recepción aceptada'
Expect (@($kardex.Json | Where-Object { $_.idRecepcion -eq $pendingReject.Json.idRecepcion }).Count -eq 0) 'rechazo del jefe sin entrada'
$exportDenied = Call-Api 'GET' '/inventario/maiz/kardex/exportar' $operario.token $null
Expect ($exportDenied.Status -eq 403) 'operario no puede exportar'
$export = Call-Api 'GET' '/inventario/maiz/kardex/exportar' $contadora.token $null 'http://localhost:5173'
Expect ($export.Status -eq 200 -and $export.Raw.Contains('idMovimiento;fechaHora')) 'contadora exporta CSV'
Expect ($export.AllowedOrigin -eq 'http://localhost:5173' -and
    $export.ExposedHeaders.Contains('Content-Disposition')) 'CORS expone Content-Disposition al frontend'
$today = (Get-Date).ToString('yyyy-MM-dd')
$list = Call-Api 'GET' ("/recepciones?fecha={0}" -f $today) $contadora.token $null
Expect ($list.Status -eq 200 -and @($list.Json).Count -ge 4) 'recepciones del día'
$audit = Call-Api 'GET' '/bitacora' $admin.token $null
Expect ($audit.Status -eq 200 -and @($audit.Json).Count -ge 1) 'bitácora disponible'
Write-Host 'PO-01: flujo completo verificado.'
