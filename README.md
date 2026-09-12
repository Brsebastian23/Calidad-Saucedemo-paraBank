# SauceDemo Backend

API REST en Spring Boot para el proyecto SauceDemo. Expone endpoints de productos y carrito de compras, con persistencia en PostgreSQL (Neon).

## Requisitos

- Java 21
- Maven
- Una base de datos PostgreSQL (el proyecto está configurado para [Neon](https://neon.tech))

## Configuración

La password de la base de datos **no** está en el repositorio. Se carga desde un archivo `.env` (ignorado por git) gracias a la dependencia [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv).

1. Copia la plantilla:

   ```bash
   cp .env.example .env
   ```

2. Edita `.env` y coloca la password real de tu base de datos (en Neon: Dashboard → Connection Details → "Show password"):

   ```
   DB_PASSWORD=tu_password_real
   ```

3. Si usas una base de datos distinta a la configurada por defecto, ajusta también `spring.datasource.url` y `spring.datasource.username` en [`src/main/resources/application.properties`](src/main/resources/application.properties).

> El archivo `.env` nunca debe subirse al repositorio (ya está en `.gitignore`). Usa `.env.example` como referencia para saber qué variables se necesitan.

## Ejecutar el proyecto

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints principales

### Productos

| Método | Ruta                | Descripción                |
|--------|---------------------|-----------------------------|
| GET    | `/api/products`     | Lista todos los productos  |
| GET    | `/api/products/{id}`| Obtiene un producto por id |

### Carrito

| Método | Ruta                    | Descripción                              |
|--------|-------------------------|-------------------------------------------|
| GET    | `/api/cart?sessionId=`  | Obtiene el carrito de una sesión          |
| POST   | `/api/cart`             | Agrega un producto al carrito             |
| PUT    | `/api/cart/{itemId}`    | Actualiza la cantidad de un item          |
| DELETE | `/api/cart/{itemId}`    | Elimina un item del carrito               |

## Estructura del proyecto

```
src/main/java/com/saucedemo/
├── config/       # Configuración y carga inicial de datos (DataSeeder)
├── controller/    # Controladores REST
├── dto/          # Objetos de transferencia de datos
├── model/         # Entidades JPA
├── repository/   # Repositorios Spring Data
└── service/      # Lógica de negocio
```
