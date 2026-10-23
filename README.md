# bdget - Microservicio Base DevOps

## Propósito

**bdget** es un microservicio desarrollado en **Spring Boot 3 (Java 17)** que expone una API REST para la gestión de estudiantes (`Student`), conectado a una base de datos Oracle. Este repositorio se preparó como base de trabajo para el pipeline DevOps que se construirá durante el semestre de la asignatura Ingeniería DevOps (DOY0101), aplicando control de versiones con Git/GitHub y automatización con GitHub Actions.

## Instrucciones de configuración e instalación

### Requisitos previos
- Java 17 (JDK)
- Maven (o usar el wrapper incluido `mvnw` / `mvnw.cmd`)
- Una instancia de base de datos Oracle (o Docker, ver más abajo)

### Clonar el repositorio
```bash
git clone https://github.com/cristian2434/ing-devops-ev.-parcial-2.git
cd ing-devops-ev.-parcial-2
```

### Configurar la base de datos
Edita `src/main/resources/application.properties` y reemplaza los valores de conexión:
```properties
spring.datasource.url=<db_url>
spring.datasource.username=<db_username>
spring.datasource.password=<db_password>
```

### Ejecutar el proyecto localmente
```bash
./mvnw spring-boot:run
```
La aplicación queda disponible en `http://localhost:8081`.

### Ejecutar con Docker Compose
Requiere un archivo `.env` (no versionado) con las credenciales y la carpeta `wallet/` de Oracle en la raíz:
```bash
# .env
DB_TNS_ALIAS=<alias_tns>
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>
```
```bash
docker compose up --build
```
El servicio queda en `http://localhost:8081` y se puede verificar con `curl http://localhost:8081/api/health`.

### Ejecutar los tests
```bash
./mvnw test
```

## Ejemplos de uso

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/students` | Lista todos los estudiantes |
| `GET` | `/students/{id}` | Obtiene un estudiante por ID |
| `GET` | `/students/search?name=juan` | Busca estudiantes por nombre (parcial, sin distinguir mayúsculas) |
| `POST` | `/students` | Crea un nuevo estudiante |
| `PUT` | `/students/{id}` | Actualiza un estudiante existente |
| `DELETE` | `/students/{id}` | Elimina un estudiante |
| `GET` | `/api/health` | Verifica el estado del servicio |

Ejemplo con `curl`:
```bash
curl http://localhost:8081/students/search?name=maria
```

---

## Estrategia de Ramificación: GitFlow

### ¿Qué son GitFlow y Trunk-Based Development?

**GitFlow** es una estrategia de ramificación estructurada que organiza el desarrollo en ramas específicas:
- **main** (producción)
- **develop** (desarrollo)
- **feature** (nuevas funcionalidades)
- **release** (preparación de versiones)
- **hotfix** (correcciones urgentes)

**Trunk-Based Development (TBD)** es un enfoque más ágil donde todos los desarrolladores integran sus cambios frecuentemente en una única rama principal (`main` o `trunk`), usando ramas de corta duración que se fusionan rápidamente.

### Justificación de la elección

Elegí **GitFlow** para este proyecto porque:
- Separa claramente el código en producción (`main`) del código en desarrollo activo (`develop`), lo cual da estabilidad mientras se integran nuevas funcionalidades.
- Permite trabajar features de forma aislada (`feature/<nombre>`) sin afectar la rama estable, manteniendo trazabilidad clara de qué cambio corresponde a qué propósito.
- Facilita correcciones urgentes mediante ramas `hotfix/<nombre>`, que se crean desde `main` y se integran tanto a `main` como a `develop`, sin interrumpir el desarrollo en curso.
- En un entorno colaborativo en la nube (GitHub), GitFlow da control explícito sobre qué llega a producción mediante Pull Requests revisados, lo cual es apropiado para un proyecto académico donde se evalúa el proceso, no solo el resultado final.
- A diferencia de Trunk-Based Development —que exige integración continua muy frecuente y una suite de pruebas automatizadas robusta para minimizar riesgos de romper `main`—, GitFlow se adapta mejor a un ritmo de trabajo con avances puntuales, no despliegues varias veces al día.

### Estructura de Ramas del repositorio
- `main`: código estable, listo para producción.
- `develop`: rama de integración, donde se juntan los features antes de pasar a `main`.
- `feature/<nombre>`: nuevas funcionalidades, creadas desde `develop` y mergeadas de vuelta a `develop`.
- `hotfix/<nombre>`: correcciones urgentes, creadas desde `main` y mergeadas tanto a `main` como a `develop`.

---

## Convenciones de Commits

Formato: `tipo: descripción breve en presente`

| Tipo | Uso |
|---|---|
| `feat:` | nueva funcionalidad |
| `fix:` | corrección de un bug |
| `docs:` | cambios de documentación |
| `chore:` | tareas de mantenimiento (configuración, dependencias, etc.) |
| `test:` | agregar o modificar tests |
| `ci:` | cambios en la configuración de integración continua |

Ejemplos reales usados en este repositorio:
- `feat: agregar busqueda de estudiantes por nombre`
- `feat: agregar endpoint de health check`
- `fix: capturar ruta real de la peticion y eliminar imports duplicados`
- `ci: activar trigger push a develop y pull_request a main`

## Naming de Ramas

- `feature/nombre-corto-descriptivo` (ej. `feature/busqueda-por-nombre`, `feature/health-check`)
- `hotfix/nombre-corto-descriptivo` (ej. `hotfix/ruta-real-en-errores`)

## Estructura de Carpetas

```
src/main/java/com/example/bdget/
├── controller/    # Endpoints REST (StudentController, HealthController)
├── service/       # Lógica de negocio (interfaz + implementación)
├── repository/    # Acceso a datos (Spring Data JPA)
├── model/         # Entidades (Student)
└── exception/     # Manejo global de excepciones

src/test/java/com/example/bdget/
└── ...            # Tests unitarios por capa (controller, service)
```

## Control de Versiones y Flujo de Merge

1. Todo cambio se integra vía **Pull Request (PR)**, nunca con push directo a `main` o `develop`.
2. Se verifica que el pipeline de GitHub Actions pase correctamente (tests + build) antes de mergear cada PR.
3. Los `hotfix`, una vez mergeados a `main`, también se mergean a `develop` para mantener sincronizadas ambas ramas.
4. Las ramas ya mergeadas se eliminan para mantener el repositorio limpio.

## Estrategia de Revisión

- Ningún cambio llega a `main` o `develop` sin pasar por un Pull Request, aunque se trate de un único desarrollador, para dejar registro formal y trazable de cada cambio.
- El PR describe brevemente qué cambia y por qué.
- Se revisa que el pipeline de CI (tests y build) pase en verde antes de aprobar el merge.
- Se prioriza revisar: consistencia de nombres, que el cambio no rompa funcionalidad existente, y que incluya sus pruebas correspondientes si aplica.

## Pipeline CI/CD — GitHub Actions

El pipeline está definido en `.github/workflows/main.yml` y se ejecuta en cada **push a `main` y `develop`** y en cada **Pull Request hacia `main`**. Si llega un nuevo push a la misma rama, se cancela la ejecución anterior (`concurrency`).

```
test ──┐
       ├──> docker ──> deploy (solo push a main)
security ┘
```

| Job | Qué hace | Indicador |
|---|---|---|
| `test` | `mvn clean verify`: 19 pruebas unitarias (JUnit) y verificación de cobertura con JaCoCo. Sube el reporte como artefacto. | IE2 |
| `security` | Snyk analiza dependencias (`snyk test`) y código (`snyk code test`). Falla con vulnerabilidades de severidad **alta o crítica**. | IE3 |
| `docker` | Construye la imagen con el `Dockerfile` multi-etapa. Solo si `test` y `security` pasaron. En `main` la publica en GHCR. | IE1 |
| `deploy` | Levanta el servicio con Docker Compose en el runner, espera el estado `healthy`, ejecuta una prueba de humo a `/api/health` y publica un resumen. | IE4, IE5 |

### Contenedores (IE1)
- `Dockerfile` multi-etapa: dependencias (caché de Maven), compilación con JDK 17 y ejecución con imagen **JRE 17** más liviana.
- Se ejecuta con un usuario no root (`appuser`).
- `.dockerignore` excluye `target/`, credenciales, wallet y documentación, para que no entren a la imagen.
- Los secretos nunca están en la imagen: se inyectan por variables de entorno y el wallet se monta como volumen de solo lectura.

### Orquestación (IE5)
`docker-compose.yml` define el servicio con: puerto `8081:8081`, `healthcheck` sobre `/api/health`, límites y reservas de CPU y memoria (`1 CPU / 1 GB` límite), `restart: unless-stopped`, `no-new-privileges` y `cap_drop: ALL`.

### Seguridad y bloqueos (IE3)
- **Dependabot** (`.github/dependabot.yml`) revisa semanalmente Maven, GitHub Actions y Docker y abre Pull Requests con las actualizaciones.
- **Snyk** bloquea el pipeline si encuentra vulnerabilidades altas o críticas: como `docker` depende de `security`, la imagen no se construye ni se despliega.
- En `Settings → Branches`, `main` queda protegida exigiendo que pasen los checks `test` y `security` antes de hacer merge.

### Calidad (IE2)
- La política de calidad exige **al menos 70 % de cobertura de líneas** (JaCoCo, regla `check` en el `pom.xml`). Si baja de ese valor, `mvn verify` falla y el pipeline se detiene.
- Las pruebas cubren las capas controller, service, model y el manejador global de excepciones.

### Trazabilidad (IE4)
- Cada imagen se publica en GHCR etiquetada con el **SHA del commit** (`ghcr.io/<repo>:<sha>`) y con labels OCI (`revision`, `source`).
- El job `deploy` despliega exactamente la imagen construida en ese mismo run, y el resumen del despliegue registra commit e imagen.
- Todo cambio entra por Pull Request, por lo que cada despliegue se puede rastrear: commit → PR → ejecución del pipeline → imagen → despliegue.

### Secretos requeridos en GitHub
`SNYK_TOKEN`, `DB_TNS_ALIAS`, `DB_USERNAME`, `DB_PASSWORD` y `DB_WALLET_ZIP_B64` (wallet de Oracle en zip codificado en base64), en `Settings → Secrets and variables → Actions`.

## Funcionalidades agregadas en este trabajo

- **Búsqueda de estudiantes por nombre**: endpoint `GET /students/search?name=` (búsqueda parcial, insensible a mayúsculas/minúsculas), implementado en las capas Repository, Service y Controller, con su test unitario correspondiente.
- **Health Check**: endpoint `GET /api/health` que retorna el estado del servicio, útil para monitoreo básico en el entorno cloud simulado.
- **Hotfix**: corrección en `GlobalExceptionHandler` para capturar la ruta real donde ocurre un error no controlado (antes no se informaba el endpoint afectado), además de limpieza de imports duplicados.

## Declaración de Uso de Inteligencia Artificial

Se utilizó IA (Claude, Anthropic) como apoyo técnico para: generar la plantilla del workflow de GitHub Actions, `docker-compose.yml`, `Dockerfile`, `dependabot.yml` y las pruebas unitarias adicionales, orientar la configuración de Snyk y los secretos, y redactar este README. Todo el contenido fue revisado, probado y validado por el equipo antes de integrarlo al repositorio. Las conclusiones y reflexiones personales fueron redactadas sin apoyo de IA. Más información sobre uso ético de IA: https://bibliotecas.duoc.cl/ia

## Conclusiones y Reflexión Personal

*(Cada integrante redacta su reflexión sin apoyo de IA: qué aprendió y cuál fue su contribución al proyecto.)*

### Evaluación 2

**Cristian Valenzuela:** *(escribir aquí)*

### Evaluación 1 (referencia)

Cristian Valenzuela: aprendi que existe diferentes tipos de ramas que antes no conocia. Ahora se que la rama feature se usa solo para agregar una nueva funcionalidad al proyecto; nunca habia trabajado con las ramas feature y hotfix antes. Antes solo usaba el main y a lo más creaba una rama extra para trabajar en un proyecto. 

Yo antes habia trabajado el hacer pull request para hacer la conexion con la rama main y tuve que aprender hacer cambios significativos para ver como funciona. 

Entendi la diferencia entre feature y hotfix que uno es para nueva funcionalidad y el otro para corregir errores urgentes. Tambien fue dificil al inicio trabajar solo pero después me acostumbre.