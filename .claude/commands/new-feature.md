# Nova Feature: $ARGUMENTS

Implemente a feature seguindo **rigorosamente** os padrões do projeto.
Execute cada etapa em ordem e não pule nenhuma.

---

## Etapa 1 — Entender o requisito

Antes de escrever qualquer código, responda:
- O que essa feature faz?
- Quais camadas serão afetadas?
- Precisa de nova entidade ou altera uma existente?
- Há novos endpoints? São públicos ou protegidos por JWT?
- Quais regras de negócio e validações se aplicam?
- Quais exceções precisam ser criadas?

---

## Etapa 2 — Entidade (se necessário)

Se precisar de nova entidade ou alterar `domain/`:
- Seguir padrão: `@Entity`, `@Table`, Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- Timestamps via `@PrePersist` / `@PreUpdate`
- Nunca usar anotações Hibernate para timestamps

---

## Etapa 3 — Repository (se necessário)

Se precisar de nova query:
- Adicionar método na interface do repository
- Usar métodos derivados Spring Data quando possível
- `@Query` apenas se necessário

---

## Etapa 4 — DTOs

Criar os records necessários em `dto/request/` e `dto/response/`:
- **Request:** validações com Bean Validation (`@NotBlank`, `@Email`, `@Size`, etc.)
- **Response:** nunca incluir `password`
- Sempre **Java Records** — nunca classes com getters/setters

---

## Etapa 5 — Mapper (se necessário)

Se houver nova entidade ou novo response:
- Adicionar método estático em `mapper/NomeMapper.java`
- Classe utilitária com construtor privado

---

## Etapa 6 — Exceções (se necessário)

Para cada novo caso de erro de negócio:
1. Criar classe em `exception/` estendendo `RuntimeException`
2. Registrar handler no `GlobalExceptionHandler` com status HTTP correto

---

## Etapa 7 — Service

1. Adicionar método na **interface** `NomeService`
2. Implementar em `NomeServiceImpl`:
   - `@RequiredArgsConstructor` para injeção
   - `@Transactional` em métodos que escrevem no banco
   - Lançar exceções customizadas para erros de negócio
   - Usar mapper para converter Entity → DTO

---

## Etapa 8 — Controller

Adicionar endpoint em `NomeController`:
- `@Valid @RequestBody` para requests com body
- Status HTTP correto (201 para criação, 204 para delete, 200 para o resto)
- Chamar apenas o service — nunca o repository diretamente

---

## Etapa 9 — Segurança (se necessário)

Se o novo endpoint precisar de regra diferente:
- Atualizar `SecurityConfig.securityFilterChain()` com o matcher correto
- Endpoints de auth → `.permitAll()`
- Demais → `.authenticated()`

---

## Etapa 10 — Checklist final

Antes de encerrar, confirme cada item:

- [ ] DTO de response **não** contém `password`
- [ ] Novo endpoint protegido com JWT (se não for público)
- [ ] `@Valid` no request body do controller
- [ ] Exceção customizada criada e registrada no `GlobalExceptionHandler`
- [ ] `@Transactional` nos métodos que escrevem no banco
- [ ] Lógica de negócio **apenas** no service
- [ ] Sem `@Autowired` — apenas `@RequiredArgsConstructor`
- [ ] Status HTTP correto em cada endpoint
- [ ] Nenhum dado sensível exposto na response
