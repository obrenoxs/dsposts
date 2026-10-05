# DSPosts

API REST de posts e usuários construída com **Java**, **Spring Boot** e **MongoDB**, desenvolvida durante o curso **Java Spring Reference** da [DevSuperior](https://devsuperior.com.br).

O projeto explora os principais conceitos de um banco NoSQL orientado a documentos: **documentos aninhados (embedded)**, **referências entre coleções (`@DBRef`)**, **query methods** e **consultas personalizadas com `@Query`**.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Modelo de dados](#modelo-de-dados)
- [Arquitetura](#arquitetura)
- [Endpoints](#endpoints)
- [Tratamento de erros](#tratamento-de-erros)
- [Como executar](#como-executar)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Conceitos praticados](#conceitos-praticados)
- [Autor](#autor)

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Web MVC | via `spring-boot-starter-webmvc` |
| Spring Data MongoDB | via `spring-boot-starter-data-mongodb` |
| MongoDB | local (`localhost:27017`) |
| Maven | wrapper incluso no projeto |

---

## Modelo de dados

O projeto usa duas coleções (`users` e `posts`) e combina duas estratégias de relacionamento do MongoDB.

```
User (coleção users)
 ├── id, name, email
 └── posts ──(@DBRef, lazy)──► Post

Post (coleção posts)
 ├── id, moment, title, body
 ├── author   ──(embedded)──► Author { id, name }
 └── comments ──(embedded)──► Comment { text, moment, author: Author }
```

- **Embedded:** o autor e os comentários ficam dentro do próprio documento do post, o que permite ler tudo em uma única consulta, sem joins.
- **Referência (`@DBRef`):** o usuário guarda apenas referências aos seus posts, carregados sob demanda (`lazy = true`).

---

## Arquitetura

O projeto segue a arquitetura em camadas:

```
Controller  →  Service  →  Repository  →  MongoDB
    ↑             ↑
   DTO        Exceptions
```

- **controllers:** recebem as requisições HTTP e devolvem `ResponseEntity`.
- **services:** regras de aplicação e conversão entre entidade e DTO.
- **repositories:** acesso aos dados com `MongoRepository`.
- **dto:** objetos de transferência, que evitam expor referências internas (como a lista `posts` do usuário).
- **exceptions:** exceções de serviço e handler global (`@RestControllerAdvice`).

---

## Endpoints

### Usuários

| Método | Rota | Descrição | Resposta |
|---|---|---|---|
| `GET` | `/users` | Lista todos os usuários | `200 OK` |
| `GET` | `/users/{id}` | Busca um usuário por id | `200 OK` / `404` |
| `POST` | `/users` | Cadastra um novo usuário | `201 Created` |
| `PUT` | `/users/{id}` | Atualiza um usuário | `200 OK` / `404` |
| `DELETE` | `/users/{id}` | Remove um usuário | `204 No Content` / `404` |
| `GET` | `/users/{id}/posts` | Lista os posts de um usuário | `200 OK` / `404` |

### Posts

| Método | Rota | Descrição | Resposta |
|---|---|---|---|
| `GET` | `/posts/{id}` | Busca um post por id | `200 OK` / `404` |
| `GET` | `/posts/titlesearch?text={texto}` | Busca posts pelo título (sem diferenciar maiúsculas/minúsculas) | `200 OK` |

> Sem o parâmetro `text`, a busca por título retorna todos os posts.

### Exemplos

**Cadastrar usuário**

```http
POST /users
Content-Type: application/json

{
  "name": "Ana Silva",
  "email": "ana@gmail.com"
}
```

**Buscar posts por título**

```http
GET /posts/titlesearch?text=bom
```

```json
[
  {
    "id": "...",
    "moment": "2021-02-14T10:05:49Z",
    "title": "Bom dia",
    "body": "Acordei feliz hoje!",
    "author": { "id": "...", "name": "Maria Brown" },
    "comments": [
      {
        "text": "Tenha um ótimo dia!",
        "moment": "2021-02-14T12:34:26Z",
        "author": { "id": "...", "name": "Alex Green" }
      }
    ]
  }
]
```

---

## Tratamento de erros

Recursos inexistentes lançam `ResourceNotFoundException`, tratada globalmente por um `@RestControllerAdvice`, que devolve **404 Not Found** com um corpo padronizado:

```json
{
  "timestamp": "2026-10-05T14:30:00Z",
  "status": 404,
  "error": "Not found",
  "message": "Objeto não encontrado",
  "path": "/users/id-inexistente"
}
```

---

## Como executar

### Pré-requisitos

- [JDK 17](https://adoptium.net/) ou superior
- [MongoDB](https://www.mongodb.com/try/download/community) em execução na porta padrão `27017`
- (Opcional) [MongoDB Compass](https://www.mongodb.com/products/tools/compass) e [Postman](https://www.postman.com/) para explorar os dados e testar a API

### Passo a passo

```bash
# 1. Clone o repositório
git clone https://github.com/obrenoxs/dsposts.git
cd dsposts

# 2. Execute a aplicação
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080`.

### Configuração

O projeto usa o perfil `test`, ativado em `application.properties`:

```properties
spring.application.name=workshopmongo
spring.profiles.active=test
```

E a conexão com o banco é definida em `application-test.properties`:

```properties
spring.mongodb.uri=mongodb://localhost:27017/workshop_mongodb
```

### Dados iniciais (seed)

No perfil `test`, a classe `TestConfig` limpa as coleções e popula o banco a cada inicialização:

| Usuário | E-mail | Posts |
|---|---|---|
| Maria Brown | maria@gmail.com | "Partiu viagem", "Bom dia" |
| Alex Green | alex@gmail.com | — |
| Bob Grey | bob@gmail.com | — |

Alex e Bob aparecem como autores de comentários nos posts da Maria.

---

## Estrutura do projeto

```
src/main/java/com/devsuperior/workshopmongo
├── WorkshopmongoApplication.java
├── config
│   └── TestConfig.java
├── controllers
│   ├── UserController.java
│   ├── PostController.java
│   └── exceptions
│       ├── ResourceExceptionHandler.java
│       └── StandardError.java
├── models
│   ├── dto
│   │   ├── UserDTO.java
│   │   └── PostDTO.java
│   ├── embedded
│   │   ├── Author.java
│   │   └── Comment.java
│   └── entities
│       ├── User.java
│       └── Post.java
├── repositories
│   ├── UserRepository.java
│   └── PostRepository.java
└── services
    ├── UserService.java
    ├── PostService.java
    └── exceptions
        └── ResourceNotFoundException.java
```

---

## Conceitos praticados

- Mapeamento de documentos com `@Document` e `@Id`
- Objetos embutidos (embedded) versus referências com `@DBRef`
- CRUD completo com `MongoRepository`
- Padrão DTO e conversão entre entidade e DTO
- Respostas HTTP semânticas (`201 Created` com header `Location`, `204 No Content`, `404 Not Found`)
- Tratamento global de exceções com `@RestControllerAdvice`
- Query methods do Spring Data (`findByTitleContainingIgnoreCase`)
- Consultas personalizadas com `@Query` e o operador `$regex`
- Carga inicial de dados por perfil (`@Profile("test")`)

---

## Autor

**Breno Oliveira de Souza**

[![GitHub](https://img.shields.io/badge/GitHub-obrenoxs-181717?logo=github)](https://github.com/obrenoxs)

Projeto desenvolvido com fins educacionais durante o curso **Java Spring Reference** da [DevSuperior](https://devsuperior.com.br).
