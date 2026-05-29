# ⚙️ Smart Book Finder - Backend

Este proyecto es el backend de la aplicación **Smart Book Finder**, desarrollado en Java con Spring Boot.

Se encarga de:

* Gestionar la lógica de negocio
* Consumir la API externa de Open Library
* Aplicar validaciones estrictas
* Persistir los datos de historial y favoritos

---

# 📐 Arquitectura del Proyecto

El código está organizado bajo una arquitectura limpia en capas para cumplir con los estándares del Taller Final.

## Controller

Exposición de endpoints REST y captura de peticiones HTTP.

---

## Service

Implementación de las reglas de negocio obligatorias:

* Validaciones
* Filtros de año
* Filtros de idioma
* Validación de mínimo de coincidencias

---

## Repository

Acceso a datos y persistencia real mediante Spring Data JPA.

---

## DTO

Objetos de transferencia de datos para el desacoplamiento de entidades.

---

## Exception

Manejo global y centralizado de excepciones del sistema (`GlobalExceptionHandler`).

---

# 🛠️ Requisitos Previos

* **Java 17** o superior
* **Maven 3.8+**

---

# 🗄️ Base de Datos (H2 Console)

El proyecto utiliza **H2 Database** configurada en modo archivo para asegurar la persistencia real de la información (Historial y Favoritos) de forma portable y sin requerir instalaciones externas.

## Acceder a la consola H2

### 1. Asegúrate de que el servidor Spring Boot esté en ejecución

---

### 2. Ingresa a la consola web de H2 en tu navegador

```bash id="h1"
http://localhost:8080/h2-console
```

---

### 3. Configuración de acceso

* **JDBC URL:** `jdbc:h2:file:./data/bookfinderdb`
* **User Name:** `sa`
* **Password:** *(dejar en blanco o usar la configurada en `application.properties`)*

---

### 4. Haz clic en `Connect`

---

# 🚀 Instrucciones de Ejecución

Sigue estos pasos para descargar y levantar la API en tu máquina local.

## 1. Clonar el repositorio

```bash id="h2"
git clone https://github.com/AlejoCastroT/smart-book-finder-back.git
cd smart-book-finder-back
```

---

## 2. Compilar e instalar dependencias

En la raíz del proyecto, ejecuta:

```bash id="h3"
./mvnw clean install
```

---

## 3. Iniciar la aplicación

Levanta el servidor de Spring Boot con el siguiente comando:

```bash id="h4"
./mvnw spring-boot:run
```

La API estará disponible en:

```bash id="h5"
http://localhost:8080
```

---

# 🧪 Ejecución de Pruebas Automatizadas

El proyecto cuenta con:

* Pruebas unitarias (`JUnit + Mockito`)
* Pruebas de integración REST (`Spring MockMvc`)

Estas pruebas aseguran la calidad y estabilidad del software.

## Ejecutar toda la suite de pruebas

```bash id="h6"
./mvnw test
```

---

# 📜 Lista de Comandos CURL

A continuación se detallan las llamadas CURL necesarias para probar los endpoints implementados en el sistema.

---

## 1. Búsqueda Exitosa de Libros

Envía parámetros válidos para consumir la API de Open Library y aplicar las reglas de negocio.

```bash id="h7"
curl -X POST http://localhost:8080/api/books/search \
     -H "Content-Type: application/json" \
     -d '{
       "title": "El Senor de los Anillos",
       "author": "Tolkien",
       "language": "espanol",
       "publishedAfter": 1950
     }'
```

---

## 2. Búsqueda Fallida (Parámetros requeridos faltantes)

Simula un error validado por la capa de servicios al no proveer título ni autor.

```bash id="h8"
curl -X POST http://localhost:8080/api/books/search \
     -H "Content-Type: application/json" \
     -d '{
       "title": "",
       "author": "",
       "language": "ingles",
       "publishedAfter": null
     }'
```

---

## 3. Guardar Libro en Favoritos

Almacena de forma persistente un libro en la base de datos mediante su identificador único.

```bash id="h9"
curl -X POST http://localhost:8080/api/favorites/ElSenordeLosAnillosTolkien \
     -H "Content-Type: application/json"
```
