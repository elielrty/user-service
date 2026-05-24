# Padrões do Projeto — user-service

Referência completa dos padrões adotados. Siga rigorosamente antes de implementar qualquer coisa.

---

## Stack
- Spring Boot 4.0.6 / Java 21
- Spring Data JPA + PostgreSQL
- Spring Security 7 + JJWT 0.12.6
- Lombok / Bean Validation
- Pacote base: `com.exemplo.userservice`

---

## 1. Entidade JPA (`domain/`)

```java
@Entity
@Table(name = "nome_tabela")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NomeEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String campo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Regras:**
- Timestamps sempre via `@PrePersist` / `@PreUpdate` — nunca anotações Hibernate
- Sempre `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`
- Nunca expor `password` em DTO de resposta

---

## 2. Repository (`repository/`)

```java
public interface NomeRepository extends JpaRepository<Entidade, Long> {
    Optional<Entidade> findByCampo(String campo);
    boolean existsByCampo(String campo);
}
```

**Regras:**
- Apenas interface — sem `@Repository` (Spring Data já registra)
- Métodos derivados do Spring Data quando possível
- `@Query` apenas quando necessário

---

## 3. DTOs — Request (`dto/request/`)

```java
public record NomeRequest(

    @NotBlank(message = "Campo é obrigatório")
    String campo,

    @Email(message = "Email inválido")
    String email,

    @Size(min = 6, message = "Mínimo 6 caracteres")
    String password
) {}
```

**Regras:**
- Sempre **Java Records** — nunca classes com getters/setters
- Anotações de validação em todos os campos obrigatórios
- Campos opcionais em `UpdateRequest` não levam `@NotBlank`

---

## 4. DTOs — Response (`dto/response/`)

```java
public record NomeResponse(
    Long id,
    String campo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**Regras:**
- Sempre **Java Records**
- **Nunca incluir `password`** em qualquer response
- Sem anotações de validação (apenas saída)

---

## 5. Mapper (`mapper/`)

```java
public class NomeMapper {

    private NomeMapper() {}

    public static NomeResponse toResponse(Entidade entidade) {
        return new NomeResponse(
                entidade.getId(),
                entidade.getCampo(),
                entidade.getCreatedAt(),
                entidade.getUpdatedAt()
        );
    }
}
```

**Regras:**
- Classe utilitária estática — construtor privado
- Sem `@Component` — não é um bean Spring
- Um mapper por entidade

---

## 6. Service (`service/`)

### Interface
```java
public interface NomeService {
    NomeResponse findById(Long id);
    Page<NomeResponse> findAll(Pageable pageable);
    NomeResponse update(Long id, UpdateNomeRequest request);
    void delete(Long id);
}
```

### Implementação
```java
@Service
@RequiredArgsConstructor
public class NomeServiceImpl implements NomeService {

    private final NomeRepository nomeRepository;
    private final PasswordEncoder passwordEncoder; // se necessário

    @Override
    public NomeResponse findById(Long id) {
        Entidade entidade = nomeRepository.findById(id)
                .orElseThrow(() -> new NomeNotFoundException(id));
        return NomeMapper.toResponse(entidade);
    }

    @Override
    @Transactional
    public NomeResponse update(Long id, UpdateNomeRequest request) {
        // lógica de atualização parcial
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!nomeRepository.existsById(id)) {
            throw new NomeNotFoundException(id);
        }
        nomeRepository.deleteById(id);
    }
}
```

**Regras:**
- Sempre **interface + implementação separada**
- Injeção via `@RequiredArgsConstructor` — nunca `@Autowired`
- `@Transactional` em métodos que escrevem no banco
- Exceções de negócio lançadas na camada de service, nunca no controller
- Nunca acessar o repository diretamente no controller

---

## 7. Controller (`controller/`)

```java
@RestController
@RequestMapping("/api/v1/nomes")
@RequiredArgsConstructor
public class NomeController {

    private final NomeService nomeService;

    @GetMapping("/{id}")
    public ResponseEntity<NomeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(nomeService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<NomeResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(nomeService.findAll(pageable));
    }

    @PostMapping
    public ResponseEntity<NomeResponse> create(@Valid @RequestBody CreateNomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(nomeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NomeResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateNomeRequest request) {
        return ResponseEntity.ok(nomeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nomeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Status HTTP obrigatórios:**
| Operação | Status |
|---|---|
| GET (sucesso) | 200 OK |
| POST (criação) | 201 Created |
| PUT (atualização) | 200 OK |
| DELETE | 204 No Content |
| Não encontrado | 404 Not Found |
| Email duplicado | 409 Conflict |
| Validação falhou | 400 Bad Request |
| Sem autenticação | 401 Unauthorized |

---

## 8. Exceções (`exception/`)

```java
// Exceção customizada
public class NomeNotFoundException extends RuntimeException {
    public NomeNotFoundException(Long id) {
        super("Recurso não encontrado com id: " + id);
    }
}

// GlobalExceptionHandler já trata automaticamente:
// - NomeNotFoundException → 404
// - EmailAlreadyExistsException → 409
// - MethodArgumentNotValidException → 400
// - Exception genérica → 500
```

**Regras:**
- Criar exceção específica para cada caso de negócio
- Sempre registrar o handler no `GlobalExceptionHandler`
- Nunca lançar `RuntimeException` diretamente — sempre exceção nomeada

---

## 9. Segurança

```java
// Endpoints públicos (SecurityConfig)
.requestMatchers("/api/v1/auth/**", "/actuator/**").permitAll()

// Todo o resto exige JWT
.anyRequest().authenticated()

// DaoAuthenticationProvider (Spring Security 7)
new DaoAuthenticationProvider(userDetailsService) // construtor obrigatório
provider.setPasswordEncoder(passwordEncoder())     // setter ainda existe

// JJWT 0.12.x
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
```

---

## 10. Checklist antes de commitar

- [ ] DTO de resposta não contém `password`?
- [ ] Novo endpoint protegido com JWT (se necessário)?
- [ ] `@Valid` no request body do controller?
- [ ] Exceção customizada criada e registrada no handler?
- [ ] `@Transactional` nos métodos que escrevem no banco?
- [ ] Lógica de negócio apenas no service, não no controller?
- [ ] Nenhum `@Autowired` — apenas `@RequiredArgsConstructor`?
