# Prueba de los CRUD ampliados. Usa datos descartables y conserva los historiales inmutables.
$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path -Parent $MyInvocation.MyCommand.Path)
$envPath=if (Test-Path '.env') { '.env' } else { '..\SW E3 Base de datos Moliselva\.env' }
$line=Get-Content -LiteralPath $envPath | Where-Object { $_.StartsWith('DEMO_PASSWORD=') } | Select-Object -First 1
if (-not $line) { throw 'DEMO_PASSWORD no configurada' }
$password=$line.Substring('DEMO_PASSWORD='.Length)
$base='http://127.0.0.1:8080/api'

function Api([string]$method,[string]$path,[string]$token,[object]$body=$null) {
    $args=@{ Uri=$base+$path; Method=$method; SkipHttpErrorCheck=$true; TimeoutSec=20 }
    if ($token) { $args.Headers=@{ Authorization='Bearer '+$token } }
    if ($null -ne $body) {
        $args.ContentType='application/json'
        $args.Body=ConvertTo-Json -InputObject $body -Depth 8 -Compress
    }
    $response=Invoke-WebRequest @args
    $json=$null
    if ($response.Content -and $response.Headers['Content-Type'] -match 'application/json') {
        $json=$response.Content | ConvertFrom-Json
    }
    return [pscustomobject]@{ Status=[int]$response.StatusCode; Json=$json; Raw=$response.Content }
}
function Check([bool]$condition,[string]$name) {
    if (-not $condition) { throw "FALLO: $name" }
    Write-Host "OK: $name"
}
function Status([object]$response,[int]$expected,[string]$name) {
    if ($response.Status -ne $expected) {
        throw "FALLO: $name - HTTP $($response.Status) $($response.Raw)"
    }
    Write-Host "OK: $name"
}
function Login([string]$name) {
    $r=Api 'POST' '/auth/login' '' @{ usuario=$name; password=$password }
    if ($r.Status -ne 200 -or -not $r.Json.token) { throw "Login fallido: $name" }
    return $r.Json.token
}

$admin=Login 'admin_demo'
$jefe=Login 'jefe_demo'
$operario=Login 'operario_demo'
$contadora=Login 'contadora_demo'
$suffix=[DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$roles=Api 'GET' '/roles' $admin
Check ($roles.Status -eq 200 -and @($roles.Json).Count -eq 4) 'listar cuatro roles fijos'
Status (Api 'GET' "/roles/$($roles.Json[0].idRol)" $admin) 200 'consultar rol'
Status (Api 'GET' '/roles' $operario) 403 'operario no administra roles'

# Usuarios: CRUD y revocación inmediata de token/rol.
$user=Api 'POST' '/usuarios' $admin @{ nombres='Usuario CRUD'; usuario="crud_$suffix"; password=$password; rol='OPERARIO' }
Status $user 201 'crear usuario'
$userId=$user.Json.id
$userToken=Login "crud_$suffix"
Status (Api 'GET' "/usuarios/$userId" $admin) 200 'consultar usuario'
Status (Api 'PUT' "/usuarios/$userId" $operario @{ nombres='Sin permiso'; usuario="crud_$suffix"; rol='OPERARIO' }) 403 'operario no actualiza usuario'
$userUpdated=Api 'PUT' "/usuarios/$userId" $admin @{
    nombres='Usuario CRUD actualizado'; usuario="crud_$suffix"; rol='CONTADORA'
}
Check ($userUpdated.Status -eq 200 -and $userUpdated.Json.rol -eq 'CONTADORA') 'actualizar usuario y rol'
Status (Api 'GET' '/inventario/maiz/kardex/exportar' $userToken) 200 'token usa rol actual de BD'
$adminId=(Api 'GET' '/auth/me' $admin).Json.id
Status (Api 'DELETE' "/usuarios/$adminId" $admin) 409 'administrador no se desactiva a sí mismo'
Status (Api 'DELETE' "/usuarios/$userId" $admin) 204 'desactivar usuario'
Status (Api 'GET' '/auth/me' $userToken) 401 'token de usuario inactivo queda revocado'

# Maestros: crear, consultar, actualizar y desactivar.
$cliente=Api 'POST' '/clientes' $jefe @{
    tipoCliente='REVENDEDOR'; nombreRazonSocial="CLIENTE CRUD $suffix"; documento='12345678'; telefono='999888777'; zona='Lima'
}
Status $cliente 201 'crear cliente'
$clienteId=$cliente.Json.idCliente
Status (Api 'GET' "/clientes/$clienteId" $contadora) 200 'consultar cliente'
Status (Api 'GET' "/clientes/$clienteId" $operario) 403 'operario no ve datos de clientes'
$clienteUpdated=Api 'PUT' "/clientes/$clienteId" $admin @{
    tipoCliente='OTRO'; nombreRazonSocial="CLIENTE CRUD ACTUALIZADO $suffix"; documento='12345678'; zona='Lima'
}
Check ($clienteUpdated.Status -eq 200 -and $clienteUpdated.Json.tipoCliente -eq 'OTRO') 'actualizar cliente'

$insumo=Api 'POST' '/insumos' $admin @{ nombre="INSUMO CRUD $suffix"; unidadMedida='kg' }
Status $insumo 201 'crear insumo'
$insumoId=$insumo.Json.idInsumo
Status (Api 'GET' "/insumos/$insumoId" $contadora) 200 'consultar insumo'
Status (Api 'PUT' "/insumos/$insumoId" $jefe @{ nombre="INSUMO CRUD ACTUALIZADO $suffix"; unidadMedida='kg' }) 200 'actualizar insumo'

$producto=Api 'POST' '/productos' $jefe @{
    nombre="PRODUCTO CRUD $suffix"; especie='Bovino'; fase='Crecimiento'; lineaComercial='GJA'; presentacion='Saco de 40 kg'
}
Status $producto 201 'crear producto'
$productoId=$producto.Json.idProducto
Status (Api 'GET' "/productos/$productoId" $operario) 200 'consultar producto'
Status (Api 'PUT' "/productos/$productoId" $admin @{
    nombre="PRODUCTO CRUD ACTUALIZADO $suffix"; especie='Bovino'; fase='Engorde'; lineaComercial='GJA'; presentacion='Saco de 40 kg'
}) 200 'actualizar producto'

# Fórmulas y lotes. Las proporciones solo son visibles para el jefe.
Status (Api 'GET' '/formulas' $operario) 403 'operario no lee fórmulas'
$formula=Api 'POST' '/formulas' $jefe @{ idProducto=$productoId; version="crud-$suffix"; vigente=$true }
Status $formula 201 'crear fórmula'
$formulaId=$formula.Json.idFormula
Status (Api 'GET' "/formulas/$formulaId" $jefe) 200 'consultar fórmula'
Status (Api 'PUT' "/formulas/$formulaId" $jefe @{ idProducto=$productoId; version="crud-$suffix-v2"; vigente=$true }) 200 'actualizar fórmula'
$formulaDetail=Api 'POST' "/formulas/$formulaId/detalles" $jefe @{ idInsumo=$insumoId; proporcionConfidencial=25 }
Status $formulaDetail 201 'agregar ingrediente de fórmula'
Status (Api 'GET' "/formulas/$formulaId/detalles/$insumoId" $jefe) 200 'consultar ingrediente de fórmula'
Status (Api 'PUT' "/formulas/$formulaId/detalles/$insumoId" $jefe @{ idInsumo=$insumoId; proporcionConfidencial=30 }) 200 'actualizar ingrediente de fórmula'
$lote=Api 'POST' '/lotes' $jefe @{ idProducto=$productoId; idFormula=$formulaId; tamanoKg=500 }
Status $lote 201 'crear lote'
$loteId=$lote.Json.idLote
Status (Api 'GET' "/lotes/$loteId" $operario) 200 'consultar lote'
Status (Api 'PUT' "/lotes/$loteId" $jefe @{ idProducto=$productoId; idFormula=$formulaId; tamanoKg=1000 }) 200 'actualizar lote'
Status (Api 'PUT' "/formulas/$formulaId" $jefe @{ idProducto=$productoId; version='no'; vigente=$true }) 409 'fórmula utilizada no se modifica'
$loteDetail=Api 'POST' "/lotes/$loteId/insumos" $jefe @{
    idInsumo=$insumoId; cantidadCalculadaKg=250; cantidadRealKg=249
}
Status $loteDetail 201 'agregar insumo al lote'
Status (Api 'GET' "/lotes/$loteId/insumos/$insumoId" $operario) 200 'consultar insumo del lote'
Status (Api 'PUT' "/lotes/$loteId/insumos/$insumoId" $jefe @{
    idInsumo=$insumoId; cantidadCalculadaKg=300; cantidadRealKg=299
}) 200 'actualizar insumo del lote'
Status (Api 'DELETE' "/lotes/$loteId/insumos/$insumoId" $jefe) 204 'eliminar insumo del lote'
Status (Api 'DELETE' "/lotes/$loteId" $jefe) 204 'eliminar lote'
Status (Api 'DELETE' "/formulas/$formulaId/detalles/$insumoId" $jefe) 204 'eliminar ingrediente de fórmula'
Status (Api 'DELETE' "/formulas/$formulaId" $jefe) 204 'eliminar fórmula'

# Pedido y despacho de borrador.
$pedido=Api 'POST' '/pedidos' $contadora @{ idCliente=$clienteId; observacion='Pedido CRUD' }
Status $pedido 201 'crear pedido'
$pedidoId=$pedido.Json.idPedido
Status (Api 'GET' "/pedidos/$pedidoId" $contadora) 200 'consultar pedido'
Status (Api 'GET' "/pedidos/$pedidoId" $operario) 403 'operario no ve pedidos'
Status (Api 'PUT' "/pedidos/$pedidoId" $jefe @{ idCliente=$clienteId; observacion='Pedido actualizado' }) 200 'actualizar pedido'
Status (Api 'POST' "/pedidos/$pedidoId/detalles" $contadora @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 201 'agregar detalle de pedido'
Status (Api 'GET' "/pedidos/$pedidoId/detalles/$productoId" $admin) 200 'consultar detalle de pedido'
Status (Api 'PUT' "/pedidos/$pedidoId/detalles/$productoId" $admin @{
    idProducto=$productoId; cantidadSacos=2; cantidadKg=80
}) 200 'actualizar detalle de pedido'
$despacho=Api 'POST' '/despachos' $jefe @{ idPedido=$pedidoId; tipoEntrega='REPARTO'; destino='Lima' }
Status $despacho 201 'crear despacho'
$despachoId=$despacho.Json.idDespacho
Status (Api 'GET' "/despachos/$despachoId" $contadora) 200 'consultar despacho'
Status (Api 'GET' "/despachos/$despachoId" $operario) 403 'operario no ve despachos'
Status (Api 'PUT' "/despachos/$despachoId" $contadora @{
    idPedido=$pedidoId; tipoEntrega='RECOJO_PLANTA'
}) 200 'actualizar despacho'
Status (Api 'POST' "/despachos/$despachoId/detalles" $contadora @{
    idProducto=$productoId; cantidadSacos=2; cantidadKg=80
}) 201 'agregar detalle de despacho'
Status (Api 'GET' "/despachos/$despachoId/detalles/$productoId" $admin) 200 'consultar detalle de despacho'
Status (Api 'PUT' "/despachos/$despachoId/detalles/$productoId" $jefe @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 200 'actualizar detalle de despacho'
Status (Api 'DELETE' "/despachos/$despachoId/detalles/$productoId" $admin) 204 'eliminar detalle de despacho'
Status (Api 'DELETE' "/despachos/$despachoId" $admin) 204 'eliminar despacho borrador'
Status (Api 'DELETE' "/pedidos/$pedidoId/detalles/$productoId" $contadora) 204 'eliminar detalle de pedido'
Status (Api 'DELETE' "/pedidos/$pedidoId" $contadora) 204 'eliminar pedido pendiente'

# Kardex: consulta y ajuste compensatorio. El trigger impide UPDATE y DELETE.
$adjustment=Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_ENTRADA'; idProducto=$productoId; cantidad=40; motivo='Prueba CRUD'
}
Status $adjustment 201 'registrar ajuste de inventario'
Status (Api 'GET' "/inventario/movimientos/$($adjustment.Json.idMovimiento)" $operario) 200 'consultar movimiento'
Status (Api 'GET' '/inventario/movimientos' $admin) 200 'listar movimientos'
Status (Api 'GET' '/inventario/stock' $admin) 200 'listar stock general'
$productStock=Api 'GET' "/inventario/stock/PRODUCTO/$productoId" $contadora
Check ($productStock.Status -eq 200 -and $productStock.Json.stockActual -eq 40) 'stock de producto tras ajuste'
Status (Api 'DELETE' "/productos/$productoId" $admin) 409 'producto con stock no se desactiva'
$tooMuch=Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_SALIDA'; idProducto=$productoId; cantidad=41; motivo='Prueba de stock'
}
Check ($tooMuch.Status -eq 409 -and $tooMuch.Json.codigo -eq 'STOCK_INSUFICIENTE') 'stock negativo rechazado'

# Producción: iniciar y finalizar lote en una transacción, con kardex de entrada y salida.
Status (Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_ENTRADA'; idInsumo=$insumoId; cantidad=100; motivo='Insumo para lote CRUD'
}) 201 'cargar stock de insumo para lote'
Status (Api 'DELETE' "/insumos/$insumoId" $jefe) 409 'insumo con stock no se desactiva'
$formula2=Api 'POST' '/formulas' $jefe @{ idProducto=$productoId; version="produccion-$suffix"; vigente=$true }
Status $formula2 201 'crear fórmula de producción'
$formula2Id=$formula2.Json.idFormula
Status (Api 'POST' "/formulas/$formula2Id/detalles" $jefe @{
    idInsumo=$insumoId; proporcionConfidencial=100
}) 201 'crear ingrediente de producción'
$lote2=Api 'POST' '/lotes' $jefe @{ idProducto=$productoId; idFormula=$formula2Id; tamanoKg=500 }
Status $lote2 201 'planificar lote de producción'
$lote2Id=$lote2.Json.idLote
Status (Api 'POST' "/lotes/$lote2Id/insumos" $jefe @{
    idInsumo=$insumoId; cantidadCalculadaKg=100
}) 201 'asignar insumo al lote'
$started=Api 'PATCH' "/lotes/$lote2Id/estado" $jefe @{ estado='EN_PROCESO' }
Check ($started.Status -eq 200 -and $started.Json.estado -eq 'EN_PROCESO') 'iniciar lote'
Status (Api 'PUT' "/lotes/$lote2Id/insumos/$insumoId" $jefe @{
    idInsumo=$insumoId; cantidadCalculadaKg=100; cantidadRealKg=101
}) 200 'registrar consumo superior al stock'
$sinStock=Api 'POST' "/lotes/$lote2Id/finalizar" $jefe
Check ($sinStock.Status -eq 409 -and $sinStock.Json.codigo -eq 'STOCK_INSUFICIENTE') 'finalización rechaza stock insuficiente'
$movimientosFallidos=(Api 'GET' '/inventario/movimientos' $admin).Json | Where-Object { $_.idLote -eq $lote2Id }
Check (@($movimientosFallidos).Count -eq 0 -and
    (Api 'GET' "/lotes/$lote2Id" $jefe).Json.estado -eq 'EN_PROCESO') 'finalización fallida no deja movimientos ni cambia estado'
Status (Api 'PUT' "/lotes/$lote2Id/insumos/$insumoId" $jefe @{
    idInsumo=$insumoId; cantidadCalculadaKg=100; cantidadRealKg=100
}) 200 'registrar cantidad real en proceso'
$finished=Api 'POST' "/lotes/$lote2Id/finalizar" $jefe
Check ($finished.Status -eq 200 -and $finished.Json.estado -eq 'TERMINADO') 'finalizar lote y registrar movimientos'
$loteMoves=(Api 'GET' '/inventario/movimientos' $admin).Json | Where-Object { $_.idLote -eq $lote2Id }
Check (@($loteMoves).Count -eq 2 -and
    @($loteMoves | Where-Object { $_.tipoMovimiento -eq 'SALIDA' }).Count -eq 1 -and
    @($loteMoves | Where-Object { $_.tipoMovimiento -eq 'ENTRADA' }).Count -eq 1) 'lote registra una salida y una entrada'
$ingredientStock=Api 'GET' "/inventario/stock/INSUMO/$insumoId" $contadora
Check ($ingredientStock.Status -eq 200 -and $ingredientStock.Json.stockActual -eq 0) 'stock de insumo consumido'
Status (Api 'POST' "/lotes/$lote2Id/finalizar" $jefe) 409 'lote no se finaliza dos veces'
Status (Api 'DELETE' "/lotes/$lote2Id" $jefe) 409 'lote terminado no se elimina'

# Venta: preparar pedido y confirmar despacho con salida de stock.
$pedido2=Api 'POST' '/pedidos' $contadora @{ idCliente=$clienteId; observacion='Pedido para despacho confirmado' }
Status $pedido2 201 'crear pedido para confirmación'
$pedido2Id=$pedido2.Json.idPedido
Status (Api 'POST' "/pedidos/$pedido2Id/detalles" $contadora @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 201 'agregar producto al pedido'
$prepared=Api 'PATCH' "/pedidos/$pedido2Id/estado" $contadora @{ estado='EN_PREPARACION' }
Check ($prepared.Status -eq 200 -and $prepared.Json.estado -eq 'EN_PREPARACION') 'preparar pedido'
$despacho2=Api 'POST' '/despachos' $jefe @{
    idPedido=$pedido2Id; tipoEntrega='REPARTO'; destino='Lima'
}
Status $despacho2 201 'crear despacho para confirmar'
$despacho2Id=$despacho2.Json.idDespacho
Status (Api 'POST' "/despachos/$despacho2Id/detalles" $contadora @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 201 'agregar producto al despacho'
Status (Api 'PUT' "/despachos/$despacho2Id/detalles/$productoId" $contadora @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=41
}) 200 'preparar cantidad de despacho excesiva'
Status (Api 'POST' "/despachos/$despacho2Id/confirmar" $contadora) 409 'despacho no supera cantidad del pedido'
Status (Api 'PUT' "/despachos/$despacho2Id/detalles/$productoId" $contadora @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 200 'corregir cantidad de despacho'
$stockPrevio=[decimal](Api 'GET' "/inventario/stock/PRODUCTO/$productoId" $admin).Json.stockActual
$retiroTemporal=$stockPrevio-39
Status (Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_SALIDA'; idProducto=$productoId; cantidad=$retiroTemporal; motivo='Prueba de confirmación sin stock'
}) 201 'reducir stock para probar confirmación'
$sinStockDespacho=Api 'POST' "/despachos/$despacho2Id/confirmar" $contadora
Check ($sinStockDespacho.Status -eq 409 -and $sinStockDespacho.Json.codigo -eq 'DESPACHO_NO_CONFIRMADO') 'confirmación rechaza stock insuficiente'
$movimientosDespachoFallido=(Api 'GET' '/inventario/movimientos' $admin).Json | Where-Object { $_.idDespacho -eq $despacho2Id }
Check (@($movimientosDespachoFallido).Count -eq 0 -and
    (Api 'GET' "/despachos/$despacho2Id" $admin).Json.estado -eq 'BORRADOR' -and
    (Api 'GET' "/pedidos/$pedido2Id" $admin).Json.estado -eq 'EN_PREPARACION') 'confirmación fallida no deja movimientos ni cambia estados'
Status (Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_ENTRADA'; idProducto=$productoId; cantidad=$retiroTemporal; motivo='Restituir stock de prueba'
}) 201 'restituir stock para confirmar'
$despacho3=Api 'POST' '/despachos' $jefe @{
    idPedido=$pedido2Id; tipoEntrega='RECOJO_PLANTA'
}
Status $despacho3 201 'crear segundo borrador del pedido'
$despacho3Id=$despacho3.Json.idDespacho
Status (Api 'POST' "/despachos/$despacho3Id/detalles" $jefe @{
    idProducto=$productoId; cantidadSacos=1; cantidadKg=40
}) 201 'agregar producto al segundo borrador'
$confirmed=Api 'POST' "/despachos/$despacho2Id/confirmar" $contadora
Check ($confirmed.Status -eq 200 -and $confirmed.Json.estado -eq 'CONFIRMADO') 'confirmar despacho y registrar salida'
$dispatchMoves=(Api 'GET' '/inventario/movimientos' $admin).Json | Where-Object { $_.idDespacho -eq $despacho2Id }
Check (@($dispatchMoves).Count -eq 1 -and @($dispatchMoves)[0].tipoMovimiento -eq 'SALIDA') 'despacho registra una salida'
$attended=Api 'GET' "/pedidos/$pedido2Id" $admin
Check ($attended.Status -eq 200 -and $attended.Json.estado -eq 'ATENDIDO') 'pedido queda atendido'
Status (Api 'POST' "/despachos/$despacho2Id/confirmar" $admin) 409 'despacho no se confirma dos veces'
Status (Api 'POST' "/despachos/$despacho3Id/confirmar" $admin) 409 'pedido atendido no se despacha dos veces'
Status (Api 'DELETE' "/despachos/$despacho3Id" $admin) 204 'eliminar segundo borrador sin confirmar'
Status (Api 'DELETE' "/despachos/$despacho2Id" $admin) 409 'despacho confirmado no se elimina'
Status (Api 'POST' '/inventario/movimientos/ajustes' $contadora @{
    tipoMovimiento='AJUSTE_SALIDA'; idProducto=$productoId; cantidad=500; motivo='Cerrar stock de prueba'
}) 201 'compensar stock de producto de prueba'
$productStock=Api 'GET' "/inventario/stock/PRODUCTO/$productoId" $admin
Check ($productStock.Status -eq 200 -and $productStock.Json.stockActual -eq 0) 'stock de producto compensado'

# Recepción editable solo antes de decisión/entrada.
$provider=(Api 'GET' '/proveedores' $operario).Json | Select-Object -First 1
$pending=Api 'POST' '/recepciones' $operario @{
    idProveedor=$provider.idProveedor; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.8; mermaLimpiezaKg=10
}
Status $pending 201 'crear recepción pendiente'
$receiptId=$pending.Json.idRecepcion
Status (Api 'GET' "/recepciones/$receiptId" $contadora) 200 'consultar recepción'
$receiptUpdated=Api 'PUT' "/recepciones/$receiptId" $operario @{
    idProveedor=$provider.idProveedor; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.2; mermaLimpiezaKg=10
}
Check ($receiptUpdated.Status -eq 200 -and $receiptUpdated.Json.estado -eq 'ACEPTADA' -and
    $receiptUpdated.Json.entradaKardexKg -eq 790) 'corrección crea entrada al kardex'
Status (Api 'DELETE' "/recepciones/$receiptId" $jefe) 409 'recepción con kardex no se elimina'
$pendingDelete=Api 'POST' '/recepciones' $operario @{
    idProveedor=$provider.idProveedor; pesoBrutoKg=1000; taraKg=200; pesoSacosKg=0;
    humedadPct=14.8; mermaLimpiezaKg=10
}
Status $pendingDelete 201 'crear otra recepción pendiente'
Status (Api 'DELETE' "/recepciones/$($pendingDelete.Json.idRecepcion)" $jefe) 204 'eliminar recepción pendiente'

$retired=Api 'PATCH' "/formulas/$formula2Id/vigencia" $jefe @{ vigente=$false }
Check ($retired.Status -eq 200 -and $retired.Json.vigente -eq $false) 'retirar vigencia de fórmula histórica'
Status (Api 'DELETE' "/clientes/$clienteId" $admin) 204 'desactivar cliente'
Status (Api 'DELETE' "/insumos/$insumoId" $jefe) 204 'desactivar insumo'
Status (Api 'DELETE' "/productos/$productoId" $admin) 204 'desactivar producto'
$audit=Api 'GET' '/bitacora' $admin
Status $audit 200 'listar bitácora'
$auditId=$audit.Json[0].id_bitacora
Status (Api 'GET' "/bitacora/$auditId" $jefe) 200 'consultar evento de bitácora'
Write-Host 'CRUD ampliado verificado.'
