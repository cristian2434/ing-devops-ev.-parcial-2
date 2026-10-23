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
git clone https://github.com/cristian2434/ing.-devops.git
cd ing.-devops
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

### Ejecutar con Docker
```bash
docker-compose up --build
```

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

## CI/CD — GitHub Actions

El repositorio usa GitHub Actions (`.github/workflows/main.yml`) como entorno cloud simulado de integración continua:
- Se ejecuta automáticamente con cada **push a `develop`** (job `test` + job `build`).
- Se ejecuta automáticamente con cada **Pull Request hacia `main`** (job `test` + job `build`).
- El job `test` corre `mvn test`, validando que toda la suite de pruebas unitarias pase.
- El job `build` depende de que `test` sea exitoso (`needs: test`), compila el proyecto (`mvn package -DskipTests`) y sube el `.jar` generado como artefacto descargable.

Esto simula un flujo CI/CD básico: cada cambio que se integra a `develop` se valida automáticamente, y cada propuesta de llevar código a producción (`main`) pasa por el mismo control antes de aceptarse.

## Funcionalidades agregadas en este trabajo

- **Búsqueda de estudiantes por nombre**: endpoint `GET /students/search?name=` (búsqueda parcial, insensible a mayúsculas/minúsculas), implementado en las capas Repository, Service y Controller, con su test unitario correspondiente.
- **Health Check**: endpoint `GET /api/health` que retorna el estado del servicio, útil para monitoreo básico en el entorno cloud simulado.
- **Hotfix**: corrección en `GlobalExceptionHandler` para capturar la ruta real donde ocurre un error no controlado (antes no se informaba el endpoint afectado), además de limpieza de imports duplicados.

## Declaración de Uso de Inteligencia Artificial

Se utilizó IA (Claude, Anthropic) como apoyo técnico para: estructurar los comandos Git del flujo de trabajo, generar plantillas de configuración (workflow de GitHub Actions), y redactar este README. Todas las decisiones técnicas, la implementación del código y las justificaciones fueron revisadas, validadas y comprendidas antes de integrarlas al repositorio. Más información sobre uso ético de IA: https://bibliotecas.duoc.cl/ia

## Conclusiones y Reflexión Personal

*(Sin apoyo de IA)*

Cristian Valenzuela: aprendi que existe diferentes tipos de ramas que antes no conocia. Ahora se que la rama feature se usa solo para agregar una nueva funcionalidad al proyecto; nunca habia trabajado con las ramas feature y hotfix antes. Antes solo usaba el main y a lo más creaba una rama extra para trabajar en un proyecto. 

Yo antes habia trabajado el hacer pull request para hacer la conexion con la rama main y tuve que aprender hacer cambios significativos para ver como funciona. 

Entendi la diferencia entre feature y hotfix que uno es para nueva funcionalidad y el otro para corregir errores urgentes. Tambien fue dificil al inicio trabajar solo pero después me acostumbre.