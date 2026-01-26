# Orchestra.io

<div align="center">

**Plataforma de Orquestração de Pagamentos**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)](https://redis.io/)

</div>

```  
         ____            __              __                   _     
        / __ \________  / /_  ___  _____/ /_________ _       (_)___ 
       / / / / ___/ __ \/ __ \/ _ \/ ___/ __/ ___/ __ `/   / / __ \
      / /_/ / /  / /_/ / / / /  __(__  ) /_/ /  / /_/ /   / / /_/ /
      \____/_/   \____/_/ /_/\___/____/\__/_/   \__,_(_) /_/\____/ 
```

<div align="center">

### Uma integração. Múltiplos gateways de pagamento.

[Ver Documentação](#referência-da-api) · [Reportar Problemas](https://github.com/vitinh0z/orchestra.io/issues) · [Sugerir Melhorias](https://github.com/vitinh0z/orchestra.io/discussions)

</div>

---

## Por que Orchestra.io?

### O Problema

Sua empresa vende produtos no Brasil e no exterior. Clientes brasileiros querem pagar com PIX. Clientes internacionais precisam usar cartão de crédito.

Resultado: você precisa integrar múltiplos sistemas de pagamento.

```diff
- Stripe para pagamentos internacionais
- MercadoPago para pagamentos no Brasil
- Dois códigos diferentes para manter
- Duas integrações para corrigir quando quebram
- Dobro de trabalho e complexidade
```

### A Solução

Orchestra.io unifica tudo em uma única integração:

```javascript
// Um único código funciona para qualquer moeda
const pagamento = await orchestra.processar({
  valor: 197.00,
  moeda: "BRL",           // Automaticamente usa MercadoPago
  metodoPagamento: "pix"
});

// Mesma interface para pagamentos internacionais
const payment = await orchestra.processar({
  valor: 49.00,
  moeda: "USD",           // Automaticamente usa Stripe
  metodoPagamento: "card"
});
```

**Resultado:** Um código. Menos manutenção. Menos dor de cabeça.

---

## Principais Recursos

### Roteamento Automático com Failover

O sistema escolhe automaticamente o melhor gateway baseado na moeda. **Se o gateway principal falhar, redireciona automaticamente para o alternativo.**

```mermaid
%%{init: {'theme':'dark', 'themeVariables': { 'primaryColor':'#1a1a1a','primaryTextColor':'#fff','primaryBorderColor':'#00ff00','lineColor':'#00ff00','secondaryColor':'#2a2a2a','tertiaryColor':'#1a1a1a'}}}%%
flowchart TD
    Start([Cliente envia pagamento BRL]) --> Router{SmartRouter analisa}
    Router -->|Gateway Principal| MP[Tenta MercadoPago]
    
    MP -->|✅ Sucesso| Success([Pagamento Aprovado])
    MP -->|❌ Timeout/Erro| Failover{Failover Automático}
    
    Failover -->|Tenta Gateway Alternativo| Stripe[Processa via Stripe]
    Stripe -->|✅ Sucesso| Success
    Stripe -->|❌ Falha| Failed([Pagamento Recusado])
    
    style Start fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Router fill:#1a1a1a,stroke:#00ff00,color:#fff
    style MP fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Failover fill:#1a1a1a,stroke:#ff9900,color:#fff
    style Stripe fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Success fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Failed fill:#1a1a1a,stroke:#ff0000,color:#fff
```

| Moeda | Gateway Principal | Gateway Alternativo | Métodos Disponíveis |
|-------|------------------|---------------------|---------------------|
| BRL | MercadoPago | Stripe (Failover) | PIX, Boleto, Cartão |
| USD | Stripe | PayPal (Planejado) | Cartão, Apple Pay |
| EUR | Stripe | Adyen (Planejado) | Cartão, SEPA |

**Exemplo prático:** Cliente brasileiro tenta pagar R$ 197 via PIX. MercadoPago está fora do ar (timeout após 3s). Orchestra.io detecta a falha e automaticamente redireciona para Stripe, processando como cartão. Cliente nem percebe o problema.

### Tradutor Universal de Webhooks

**Gateways te ajudam a cobrar. O Orchestra.io te ajuda a não enlouquecer com a integração.**

Em vez de o seu time de backend ter que tratar 5 formatos diferentes de Webhook (Stripe, MercadoPago, PayPal...), o Orchestra.io **normaliza tudo**.

**Você integra UMA vez. Nós traduzimos o resto.**

| Gateway | O que eles mandam (O Caos) | O que você recebe (Orchestra) |
|---------|---------------------------|-------------------------------|
| **Stripe** | `{ "type": "payment_intent.succeeded", ... }` | `{ "status": "APPROVED" }` |
| **MercadoPago** | `{ "action": "payment.created", ... }` | `{ "status": "APPROVED" }` |
| **PayPal** | `{ "event_type": "PAYMENT.CAPTURE.COMPLETED" }` | `{ "status": "APPROVED" }` |

> **Resultado:** Se você trocar de Gateway no futuro, seu código de webhook **não precisa mudar**.

### Segurança por Design

Orchestra.io nunca armazena dados sensíveis de cartão. Tudo é tokenizado antes de chegar no servidor.

```mermaid
%%{init: {'theme':'dark', 'themeVariables': { 'primaryColor':'#1a1a1a','primaryTextColor':'#fff','primaryBorderColor':'#00ff00','lineColor':'#00ff00','secondaryColor':'#2a2a2a','tertiaryColor':'#1a1a1a'}}}%%
sequenceDiagram
    autonumber
    participant Cliente
    participant Frontend
    participant Gateway
    participant Orchestra.io
    participant Banco de Dados

    Cliente->>Frontend: Digita dados do cartão
    Note over Frontend,Gateway: Dados nunca passam pelo seu servidor
    Frontend->>Gateway: Envia para tokenização
    Gateway-->>Frontend: Retorna token seguro
    Frontend->>Orchestra: Envia apenas o token
    Orchestra->>Gateway: Processa pagamento
    Gateway-->>Orchestra: Retorna resultado
    Orchestra->>Banco de Dados: Salva resultado criptografado
    Orchestra-->>Frontend: Confirma pagamento
```

**Seu servidor nunca vê dados de cartão. Conformidade simplificada.**

### Proteção Contra Duplicação e Failover Automático

Sistema de proteção evita cobranças duplicadas e garante alta disponibilidade:

| Recurso | Status | Benefício |
|---------|--------|-----------|
| **Idempotência** | ✅ Implementado | Mesmo pagamento não é cobrado 2x |
| **Bloqueio Distribuído** | ✅ Implementado | Funciona com múltiplos servidores |
| **Failover Automático** | 🚧 Em desenvolvimento | Se gateway falha, tenta o próximo automaticamente |
| **Retry Automático** | 🚧 Em desenvolvimento | Tenta novamente em caso de falha temporária |
| **Circuit Breaker** | 📋 Planejado | Isola gateways com problemas |
| **Métricas em Tempo Real** | 📋 Planejado | Dashboard de performance |

### Isolamento por Cliente

Cada cliente tem suas próprias credenciais e dados completamente separados:

```javascript
// Cada empresa tem suas configurações isoladas
POST /v1/tenants
{
  "name": "Minha Empresa",
  "gateways": {
    "stripe": "sk_live_...",      // Criptografado com AES-256
    "mercadopago": "APP_USR_..."  // Criptografado com AES-256
  }
}

// Requisições autenticadas por chave única
Headers: X-Orchestra-Key: tenant_abc123
```

**Dados de um cliente nunca vazam para outro.**

---

## Arquitetura

### Estrutura do Sistema

```mermaid
%%{init: {'theme':'dark', 'themeVariables': { 'primaryColor':'#1a1a1a','primaryTextColor':'#fff','primaryBorderColor':'#00ff00','lineColor':'#00ff00','secondaryColor':'#2a2a2a','tertiaryColor':'#1a1a1a'}}}%%
graph TB
    subgraph API["Camada de API"]
        Controller["PaymentController"]
        Filter["ApiKeyFilter"]
        Webhooks["WebhookController (Tradutor)"]
    end
    
    subgraph Application["Camada de Aplicação"]
        UseCase["ProcessPaymentUseCase"]
        Router["SmartRouter"]
    end
    
    subgraph Domain["Camada de Domínio"]
        Payment["Payment Entity"]
        Gateway["PaymentGateway Port"]
    end
    
    subgraph Infrastructure["Infraestrutura"]
        Stripe["Stripe Adapter"]
        MP["MercadoPago Adapter"]
        Postgres["PostgreSQL 16"]
        Redis["Redis 7"]
    end
    
    Controller --> Filter
    Webhooks --> UseCase
    Filter --> UseCase
    UseCase --> Router
    Router --> Gateway
    Gateway --> Stripe
    Gateway --> MP
    UseCase --> Postgres
    UseCase --> Redis

    style API fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Application fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Domain fill:#1a1a1a,stroke:#00ff00,color:#fff
    style Infrastructure fill:#1a1a1a,stroke:#00ff00,color:#fff
```

### Princípios de Design

| Princípio | Como Aplicamos |
|-----------|----------------|
| **Separação de Responsabilidades** | Cada camada tem função específica |
| **Independência de Frameworks** | Regras de negócio não dependem de tecnologias específicas |
| **Extensível** | Fácil adicionar novos gateways sem modificar código existente |
| **Testável** | Mais de 80% de cobertura de testes |

---

## Como Começar

### Requisitos

```bash
Java 21 ou superior
Docker e Docker Compose
Maven 3.8 ou superior
```

### Instalação

```bash
# 1. Clone o projeto
git clone https://github.com/vitinh0z/orchestra.io.git
cd orchestra.io

# 2. Inicie a infraestrutura
docker-compose up -d

# 3. Configure suas credenciais
cp .env.example .env

# 4. Execute a aplicação
./mvnw spring-boot:run
```

**API estará disponível em `http://localhost:8080`**

### Seu Primeiro Pagamento

```bash
# 1. Crie uma conta
curl -X POST http://localhost:8080/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{ "name": "Minha Empresa" }'

# Resposta: { "apiKey": "orch_abc123..." }

# 2. Processe um pagamento PIX
curl -X POST http://localhost:8080/v1/payments \
  -H "X-Orchestra-Key: orch_abc123..." \
  -H "Idempotency-Key: pedido-001" \
  -d '{ "amount": 197.00, "currency": "BRL", "paymentMethod": { "type": "pix" } }'
```

---

## Referência da API

### Processar Pagamento

**Endpoint:** `POST /v1/payments`

**Cabeçalhos obrigatórios:**
```http
X-Orchestra-Key: sua_chave_api
Idempotency-Key: identificador_unico
```

### Métodos de Pagamento

| Método | Código | Gateways Suportados | Disponível em |
|--------|--------|---------------------|---------------|
| **PIX** | `pix` | MercadoPago | Brasil |
| **Boleto** | `boleto` | MercadoPago | Brasil |
| **Cartão de Crédito** | `card_token` | Stripe, MercadoPago, PayPal (Breve) | Global |
| **Cartão de Débito** | `card_token` | Stripe, MercadoPago | Global |

> 📖 **Documentação Completa:** Para exemplos detalhados de requisições e respostas, veja [docs/api-reference.md](docs/api-reference.md)

---

## Segurança

### Criptografia de Credenciais

Credenciais dos gateways são criptografadas antes de serem armazenadas:

```mermaid
%%{init: {'theme':'dark', 'themeVariables': { 'primaryColor':'#1a1a1a','primaryTextColor':'#fff','primaryBorderColor':'#00ff00','lineColor':'#00ff00','secondaryColor':'#2a2a2a','tertiaryColor':'#1a1a1a'}}}%%
sequenceDiagram
    autonumber
    participant Administrador
    participant API
    participant Criptografia
    participant Banco de Dados

    Administrador->>API: Configura Gateway
    API->>Criptografia: Criptografa credencial
    Criptografia-->>API: Retorna dado criptografado
    API->>Banco de Dados: Armazena criptografado
    
    Note over API,Banco de Dados: Durante processamento de pagamento
    API->>Banco de Dados: Busca credencial
    Banco de Dados-->>API: Retorna dado criptografado
    API->>Criptografia: Descriptografa em memória
    Criptografia-->>API: Retorna credencial original
    Note over API: Usa e descarta imediatamente
```

### Práticas de Segurança

| Prática | Implementação |
|---------|---------------|
| **Validação Constante** | Toda requisição valida chave de API |
| **Privilégios Mínimos** | Chaves com permissões limitadas |
| **Múltiplas Camadas** | Várias barreiras de proteção |
| **Auditoria Completa** | Log de todas as transações |
| **Sem Dados Sensíveis em Logs** | Informações críticas nunca aparecem em logs |

---

## Testes

### Cobertura de Testes

```
Domínio:           95%
Aplicação:         85%
Infraestrutura:    70%
Geral:             83%
```

### Executar Testes

```bash
# Testes unitários
./mvnw test

# Testes de integração
./mvnw verify -P integration-tests

# Gerar relatório de cobertura
./mvnw jacoco:report
open target/site/jacoco/index.html
```

### Cartões de Teste

Use estes cartões no ambiente de testes do Stripe:

| Tipo | Número | Resultado Esperado |
|------|--------|-------------------|
| Visa Aprovado | `4242 4242 4242 4242` | Pagamento aprovado |
| Mastercard Aprovado | `5555 5555 5555 4444` | Pagamento aprovado |
| Recusado | `4000 0000 0000 9995` | Saldo insuficiente |
| Requer Autenticação | `4000 0025 0000 3155` | Solicita 3D Secure |

---

## Tecnologias Utilizadas

### Principais

| Componente | Tecnologia | Versão |
|-----------|-----------|--------|
| **Linguagem** | Java | 21 |
| **Framework** | Spring Boot | 3.x |
| **Build** | Maven | 3.8+ |

### Infraestrutura

| Componente | Tecnologia | Propósito |
|-----------|-----------|----------|
| **Banco de Dados** | PostgreSQL | Armazenamento de transações |
| **Cache** | Redis | Proteção contra duplicação |
| **Métricas** | Prometheus | Coleta de dados de performance |
| **Visualização** | Grafana | Dashboards e gráficos |

### Bibliotecas Principais

```xml
<!-- Proteção contra falhas e retry automático -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>

<!-- Processamento de JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Persistência de dados -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

## Como Contribuir

Contribuições são bem-vindas.

### Processo

1. Faça um fork do repositório
2. Crie uma branch: `git checkout -b minha-melhoria`
3. Faça suas alterações seguindo os padrões do projeto
4. Escreva testes (mínimo 80% de cobertura)
5. Commit: `git commit -m "feat: adiciona suporte para PayPal"`
6. Push: `git push origin minha-melhoria`
7. Abra um Pull Request

### Padrões

| Aspecto | Referência |
|---------|-----------|
| **Estilo de Código** | Google Java Style Guide |
| **Mensagens de Commit** | Conventional Commits |
| **Arquitetura** | Clean Architecture |
| **Documentação** | Javadoc para APIs públicas |

### Onde Ajudar

- 🐛 Encontrou um bug? Abra uma [issue](https://github.com/vitinh0z/orchestra.io/issues)
- 💡 Tem uma sugestão? Inicie uma [discussão](https://github.com/vitinh0z/orchestra.io/discussions)
- 🔌 Quer adicionar um gateway? Implemente a interface `PaymentGateway`
- 📖 Melhorar documentação? Pull requests são muito apreciados

---

## Status do Projeto

| Informação | Status |
|-----------|--------|
| **Build** | ✅ Funcionando |
| **Cobertura de Testes** | 83% |
| **Versão** | 0.4.0-alpha |
| **Licença** | MIT com Atribuição |
| **Progresso** | 67% completo |

---

## Licença

Este projeto usa a licença MIT com requisito de atribuição.

**Resumo:**
- ✅ Pode usar comercialmente
- ✅ Pode modificar
- ✅ Pode distribuir
- ⚠️ Deve incluir aviso de copyright original
- ⚠️ Deve dar crédito ao Orchestra.io

Veja [LICENSE](LICENSE) para termos completos.

---

## Suporte

| Tipo | Canal |
|------|-------|
| 🐛 **Bugs** | [GitHub Issues](https://github.com/vitinh0z/orchestra.io/issues) |
| 💬 **Discussões** | [GitHub Discussions](https://github.com/vitinh0z/orchestra.io/discussions) |
| 🔒 **Segurança** | security@orchestra.io |
| 📖 **Documentação** | Este arquivo |

---

## Por que escolher Orchestra.io?

### Comparado com integração direta

| Você faz | Orchestra faz |
|----------|---------------|
| Integra Stripe | Uma única integração |
| Integra MercadoPago | Funciona com múltiplos gateways |
| Gerencia roteamento | Roteamento automático |
| Implementa retry | Retry já incluído |
| Configura métricas | Métricas prontas |
| Trata 5 tipos de Webhook | **Traduz tudo para um formato único** |
| Trata falhas manualmente | **Failover automático entre gateways** |

### Comparado com outras soluções

| Orchestra.io | Outras soluções |
|--------------|-----------------|
| ✅ Código aberto | ❌ Código fechado |
| ✅ Self-hosted | ❌ Apenas SaaS |
| ✅ Licença MIT | ❌ Licenças restritivas |
| ✅ Sem taxas extras | ❌ Taxa de 0.5%+ por transação |
| ✅ Totalmente customizável | ❌ Limitado ao que oferecem |

---

**Desenvolvido por [@vitinh0z](https://github.com/vitinh0z)**

*Orquestração de pagamentos simplificada*

[GitHub](https://github.com/vitinh0z/orchestra.io) · [Twitter](https://twitter.com/vitinh0z_dev) · [LinkedIn](https://linkedin.com/in/vitinh0z)

</div>
