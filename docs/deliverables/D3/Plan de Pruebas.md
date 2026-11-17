# Plan de Pruebas

**Asignatura:** Diseño y Pruebas (Grado en Ingeniería del Software, Universidad de Sevilla)<br>
**Curso académico:** 2026/2027<br>
**Grupo/Equipo:** L5-5<br>
**Nombre del proyecto:** Cluedo<br>
**Repositorio:** https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5<br>
**Integrantes (máx. 6):** Javier Bonilla Fernández, Amalio Romero López, David Calzado Villalba, Pedro Larios Soler, Alfonso Perea Morillo, Nicolas Martinez Adega


## 1. Introducción

Este documento describe el plan de pruebas para el proyecto **Cluedo** desarrollado en el marco de la asignatura **Diseño y Pruebas 1** por el grupo **L5-5**. El objetivo del plan de pruebas es garantizar que el software desarrollado cumple con los requisitos especificados en las historias de usuario y que se han realizado las pruebas necesarias para validar su funcionamiento.

## 2. Alcance

El alcance de este plan de pruebas incluye:

- Pruebas unitarias.
  - Pruebas unitarias de backend incluyendo pruebas servicios o repositorios
  - Pruebas unitarias de frontend: pruebas de las funciones javascript creadas en frontend.
  - Pruebas unitarias de interfaz de usuario. Usan la interfaz de  usuario de nuestros componentes frontend.
- Pruebas de integración.  En nuestro caso principalmente son pruebas de controladores que también se ejecutarán mediante JUnit.

## 3. Estrategia de Pruebas

### 3.1 Tipos de Pruebas

#### 3.1.1 Pruebas Unitarias
Las pruebas unitarias se realizarán para verificar el correcto funcionamiento de los componentes individuales del software. Se utilizarán herramientas de automatización de pruebas como **JUnit** en backend y jest en frontend.

#### 3.1.2 Pruebas de Integración
Las pruebas de integración se enfocarán en evaluar la interacción entre los distintos módulos o componentes del sistema, nosotros las realizaremos a nivel de API, probando nuestros controladores Spring.

## 4. Herramientas y Entorno de Pruebas

### 4.1 Herramientas
- **Maven**: Gestión de dependencias y ejecución de las pruebas.
- **JUnit**: Framework de pruebas unitarias.
- **Jacoco**: Generación de informes de cobertura de código. Si se ejecuta el comando de maven install, se copiará el informe de cobertura a la subcarpeta del repositorio /docs/deliverables/D3/coverage (puede visualizarse pulsando en el fichero index.html de dicho directorio).
- **Allure**: Generación de informes de estado de las últimas ejecuciones de las pruebas. Permite agrupar las pruebas por módulo/épica y feature. Si se ejecuta el comando de maven install, se copiará el informe de estado a la subcarpeta del repositorio /docs/deliverables/D3/status (puede visualizarse pulsando en el fichero index.html de dicho directorio).
- **Jest**: Framework para pruebas unitarias en javascript.
- **React-test**: Librería para la creación de pruebas unitarias de componentes React.

### 4.2 Entorno de Pruebas
Las pruebas se ejecutarán en el entorno de desarrollo y, eventualmente, en el entorno de pruebas del servidor de integración continua.

## 5. Planificación de Pruebas
### 5.1 Estado y trazadibilidad de Pruebas por Módulo y Épica

El informe de estado de las pruebas (con trazabilidad de éstas hacia los módulos y las épicas/historias de usaurio) se encuentra [aquí](
https://gii-is-dp1.github.io/dp1-2627-groupproject-L5-5/deliverables/D3/status/#behaviors).

### 5.2 Cobertura de Pruebas

El informe de cobertura de pruebas se puede consultar [aquí](
https://gii-is-dp1.github.io/dp1-2627-groupproject-L5-5/deliverables/D3/coverage/).



| Historia de Usuario | Prueba | Descripción | Estado | Tipo |
|---------------------|--------|-------------|--------|------|
| [HU-001: Configuración inicial de una nueva partida](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/39) | [UTB-01:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Verifica la creación de la partida con nombre, aforo (2-6 jugadores) y selección del personaje anfitrión. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-002: Validar el acceso a una partida privada](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/41) | [UTB-02:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Valida el acceso por código alfanumérico, comprobando que la sala existe y tiene plazas libres. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-003: Seleccionar personaje](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/42) | [UTB-03:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Verifica la creación de la partida con un personaje y la elección de personaje al unirse. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-004: Acceso y visualización de la sala de espera (Lobby)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/43) | [UTB-04:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Comprueba la consulta del estado del lobby, participantes conectados y cumplimiento del mínimo para iniciar (RN-1). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-005: Expulsión de jugadores en el Lobby](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/44) | [UTB-05:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Verifica la potestad del anfitrión para expulsar a un jugador concreto de la sala de espera. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-006: Tirada de dados inicial para establecer el orden de los turnos](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/73) | [UTB-06:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Comprueba la tirada competitiva inicial de 2 dados y la determinación del orden de turnos. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-006: Tirada de dados inicial para establecer el orden de los turnos](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/73) | [ITC-06:TurnRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnRestControllerTests.java) | Comprueba que el endpoint de tirada inicial responde 200 y devuelve el jugador, el resultado y si el orden ya está determinado. | Implementada | Prueba de controlador |
| [HU-007: Tirar los dados al inicio del turno](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/74) | [UTB-07:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Comprueba la tirada del turno al sumar el bonificador de +6 y rechaza la tirada cuando el turno ha expirado. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-008: Movimiento de la ficha por el tablero hacia una habitación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/75) | [UTB-08:BoardServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/board/BoardServiceTests.java) | Verifica el algoritmo BFS de cálculo de rutas, acceso por puertas y bloqueo por otros jugadores en pasillos (RN-11). También comprueba que, al recibir el identificador de la partida, el servicio la resuelve por id o por código, usa al jugador autenticado y rechaza una partida inexistente o un usuario que no participa. | Implementada | Unitaria en backend de lógica de negocio (BFS) |
| [HU-008: Movimiento de la ficha por el tablero hacia una habitación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/75) | [ITC-08:BoardControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/board/BoardControllerTests.java) | Comprueba que el endpoint de casillas alcanzables responde 200 y que el de movimiento responde 200 si es válido y 400 si la casilla no se puede alcanzar. | Implementada | Prueba de controlador |
| [HU-009: Uso de pasadizos secretos](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/76) | [UTB-09:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Valida el uso del pasadizo secreto en TurnService: desplazamiento instantáneo entre habitaciones diagonales opuestas sin gastar la tirada de dados y control de restricciones de turno (RN-12). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-010: Robar carta de Pista (Casilla Bonus)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/77) | [UTB-10:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Verifica el robo de cartas del mazo bonus al finalizar el movimiento en casilla bonus y su asignación (RN-14). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-011: Jugar carta Especial](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/78) | [UTB-11:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Comprueba el consumo y la ejecución del +6 al movimiento y del turno extra, y su descarte (RN-15). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-012: Temporizador de turno](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/79) | [UTB-12:TurnServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/turn/TurnServiceTests.java) | Valida el temporizador regresivo de 60s y el timeout con paso forzoso al siguiente turno (RN-13). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-013: Ver mis cartas](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/80) | [UTB-13:GameServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/game/GameServiceTests.java) | Verifica la consulta privada y protegida de la mano de cartas y pistas asignadas al jugador autenticado. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-014: Consultar mi libreta de investigación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/81) | [UTB-14:NotebookServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/notebook/NotebookServiceTests.java) | Comprueba la carga e inicialización de la matriz de deducciones personales (sospechosos, armas y salas). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-014: Consultar mi libreta de investigación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/81) | [ITC-14:NotebookRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/notebook/NotebookRestControllerTests.java) | Comprueba que el endpoint de la libreta responde 200 con las marcas del jugador autenticado. | Implementada | Prueba de controlador |
| [HU-015: Marcar mis anotaciones en la libreta](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/82) | [UTB-15:NotebookServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/notebook/NotebookServiceTests.java) | Valida la persistencia y alternancia de marcas de investigación del jugador en su libreta privada. | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-015: Marcar mis anotaciones en la libreta](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/82) | [ITC-15:NotebookRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/notebook/NotebookRestControllerTests.java) | Comprueba que el endpoint de actualización de una marca responde 200 y delega el nuevo valor en el servicio. | Implementada | Prueba de controlador |
| [HU-016: Formular una sugerencia al entrar en una habitación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/83) | [UTB-16:SuggestionServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/suggestion/SuggestionServiceTests.java) | Verifica la hipótesis formulada en la sala actual y el teletransporte de ficha de sospechoso y arma sugerida (RN-5, RN-6, RN-10). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-016: Formular una sugerencia al entrar en una habitación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/83) | [ITC-16:SuggestionRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/suggestion/SuggestionRestControllerTests.java) | Comprueba que el endpoint de sugerencia responde 200 con la hipótesis en curso y que la consulta de la sugerencia actual responde 200, o 204 si no hay ninguna. | Implementada | Prueba de controlador |
| [HU-017: Refutar la sugerencia de otro jugador](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/84) | [UTB-17:SuggestionServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/suggestion/SuggestionServiceTests.java) | Comprueba el turno horario de refutación y la muestra confidencial de una carta coincidente al sugeridor (RN-7, RN-8). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-017: Refutar la sugerencia de otro jugador](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/84) | [ITC-17:SuggestionRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/suggestion/SuggestionRestControllerTests.java) | Comprueba que el endpoint de refutación responde 200 con el estado refutado y que el de pasar responde 200 avanzando al siguiente jugador. | Implementada | Prueba de controlador |
| [HU-018: Preparar el sobre y repartir las cartas](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/86) | [UTB-18:CardServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/card/CardServiceTests.java) | Valida la selección secreta de la solución en el sobre confidencial y el reparto equitativo del mazo restante (RN-3). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-019: Consultar una carta neutral en una partida de dos jugadores](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/87) | - | - | No Implementada | - |
| [HU-020: Formular la acusación final en la casilla central](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/88) | [UTB-20:AccusationServiceTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/accusation/AccusationServiceTests.java) | Comprueba que la acusación solo se acepta en la casilla central y en el turno del jugador. Si coincide con el sobre, termina la partida y declara ganador; si no, elimina al jugador, conserva su mano y revela las cartas neutrales que coincidan (RN-9). | Implementada | Unitaria en backend a nivel de Servicio |
| [HU-020: Formular la acusación final en la casilla central](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/88) | [ITC-20:AccusationRestControllerTests](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/blob/main/src/test/java/es/us/dp1/L5_5_26_27/cluedo/accusation/AccusationRestControllerTests.java) | Comprueba que el endpoint de acusación responde 200 con el resultado, el estado de la partida y el ganador si el trío es correcto, y 400 si la petición no trae sospechoso, arma o habitación. | Implementada | Prueba de controlador |
| [HU-021: Detección del fin de partida y anuncio del ganador](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/89) | - | - | No Implementada | - |
| [HU-022: Visualizar el listado de partidas en curso](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/90) | - | - | No Implementada | - |
| [HU-023: Visualizar el listado de partidas jugadas por el usuario actual](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/91) | - | - | No Implementada | - |
| [HU-024: Registro de un nuevo usuario en el sistema (Sign-up)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/92) | - | - | No Implementada | - |
| [HU-025: Inicio de sesión en la aplicación web (Login)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/93) | - | - | No Implementada | - |
| [HU-026: Cierre de sesión seguro (Logout)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/94) | - | - | No Implementada | - |
| [HU-027: Visualización y edición del perfil personal del jugador](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/95) | - | - | No Implementada | - |
| [HU-028: Visualización del listado global de usuarios registrados](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/96) | - | - | No Implementada | - |
| [HU-029: Crear perfiles de usuario (Create)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/97) | - | - | No Implementada | - |
| [HU-030: Consultar perfiles de usuario (Read)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/98) | - | - | No Implementada | - |
| [HU-031: Actualizar perfiles de usuario (Update)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/99) | - | - | No Implementada | - |
| [HU-032: Eliminar perfiles de usuario (Delete)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/100) | - | - | No Implementada | - |
| [HU-033: Enviar una invitación de amistad a otro usuario](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/101) | - | - | No Implementada | - |
| [HU-034: Gestión del listado de amistades](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/102) | - | - | No Implementada | - |
| [HU-035: Notificación de amigos en línea en el inicio](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/103) | - | - | No Implementada | - |
| [HU-036: Invitaciones directas a amigos para unirse a partidas](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/104) | - | - | No Implementada | - |
| [HU-037: Consultar el catálogo de logros disponibles](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/105) | - | - | No Implementada | - |
| [HU-038: Evaluación y desbloqueo de logros de participación](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/106) | - | - | No Implementada | - |
| [HU-039: Evaluación y desbloqueo de logros de victoria](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/107) | - | - | No Implementada | - |
| [HU-040: Crear nuevos logros (Create)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/108) | - | - | No Implementada | - |
| [HU-041: Consultar el listado de logros configurados (Read)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/109) | - | - | No Implementada | - |
| [HU-042: Editar un logro (Update)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/110) | - | - | No Implementada | - |
| [HU-043: Eliminar logros (Delete)](https://github.com/gii-is-DP1/dp1-2627-groupproject-L5-5/issues/111) | - | - | No Implementada | - |

## 6. Criterios de Aceptación

- Todas las pruebas unitarias deben pasar con éxito antes de la entrega final del proyecto.
- La cobertura de código debe ser al menos del 70%.
- No debe haber fallos críticos en las pruebas de integración y en la funcionalidad.

## 7. Conclusión

Este plan de pruebas establece la estructura y los criterios para asegurar la calidad del software desarrollado. Es responsabilidad del equipo de desarrollo y pruebas seguir este plan para garantizar la entrega de un producto funcional y libre de errores.
