# ADR-0002: Separação entre `auth` e `usuario` e proteção contra força bruta

Status: Aceita  
Data: 2026-11-23

## Contexto
A arquitetura de referência (`carToon-implementacao`) separa claramente responsabilidades de **autenticação/sessão** (`auth`) das responsabilidades de **gestão cadastral de usuários** (`usuario`), aplicando bloqueio temporário contra força bruta (`LoginAttemptService`) e modal global de edição do próprio perfil ("Meus Dados").

## Decisão
1. Manter o pacote `auth/` responsável por `LoginDTO`, `AuthService`, `LoginAttemptService` (5 falhas consecutivas = bloqueio de 15 minutos usando `RelogioSistema` injetável), `SenhaHasher` (SHA-256 com salt aleatório de 16 bytes) e `LoginScreen`.
2. Manter o pacote `usuario/` responsável pela entidade Room `Usuario`, `UsuarioDao`, `UsuarioRepository`, `UsuarioService` (CRUD de contas, alteração de perfil, ativação/inativação e edição de "Meus Dados") e `UsuariosScreen`.
3. Lançar exceções específicas de domínio (`CredenciaisInvalidasException`, `BloqueioTentativasException`, `RecursoInativoException`, `ConflitoException`).

## Consequências
- Código modular, desacoplado e diretamente testável sem depender do emulador Android.
- Senhas nunca são armazenadas em texto puro no banco Room.
- O administrador não pode inativar ou rebaixar acidentalmente a própria conta em uso.
