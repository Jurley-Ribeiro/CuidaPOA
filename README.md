# CuidaPOA - Aplicativo de Saúde para UBS

Aplicativo Android desenvolvido como trabalho final da disciplina de Desenvolvimento Android do IFRS. O CuidaPOA facilita o acesso às informações das Unidades Básicas de Saúde de Porto Alegre.

## 📱 Funcionalidades

### Para Cidadãos (sem login)
- ✅ Consultar vacinas disponíveis nas UBS
- ✅ Buscar Unidades Básicas de Saúde
- ✅ Visualizar horários e serviços das UBS
- ✅ Ligar diretamente para as unidades
- ✅ Ver localização no mapa

### Para Gestores (com login)
- ✅ Login com credenciais (admin@cuidapoa.com / 123456)
- 🔄 Cadastrar vacinas (em desenvolvimento)
- 🔄 Gerenciar informações das UBS (em desenvolvimento)

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 17
- **IDE:** Android Studio
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35
- **Arquitetura:** Single Activity + Fragments
- **UI:** Material Design 3
- **Navegação:** Navigation Component
- **Build:** Gradle com Kotlin DSL

## 📦 Bibliotecas

```gradle
- androidx.appcompat
- com.google.android.material
- androidx.navigation
- androidx.recyclerview
- androidx.cardview
- androidx.core:core-splashscreen
```

## 🏗️ Estrutura do Projeto

```
app/src/main/java/com/example/cuidapoa/
├── adapter/
│   ├── VacinasAdapter.java
│   └── UBSAdapter.java
├── model/
│   ├── Vacina.java
│   └── UBS.java
├── view/
│   ├── LoginActivity.java
│   └── fragments/
│       ├── HomeFragment.java
│       ├── VacinasFragment.java
│       └── UBSFragment.java
└── MainActivity.java
```

## 🚀 Como Executar

1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/CuidaPOA.git
```

2. Abra o projeto no Android Studio

3. Sincronize o projeto com Gradle

4. Execute em um emulador ou dispositivo físico

## 📱 Screenshots

### Tela Inicial
- Cards de navegação para as principais funcionalidades
- Menu lateral com opções adicionais

### Tela de Vacinas
- Lista de vacinas com informações detalhadas
- Indicador de disponibilidade
- Busca por nome ou descrição

### Tela de UBS
- Lista de unidades com horários
- Botões para ligar e ver no mapa
- Status de funcionamento (aberta/fechada)

## ✅ Checklist de Requisitos

- [x] Desenvolvido em Java 17 com Gradle
- [x] Logo e cores definidas com acessibilidade
- [x] Pacotes organizados (view, adapter, model)
- [x] Intenções implícitas (telefone, mapa, email)
- [x] Arquivo de dimensões configurado
- [x] Arquivo de strings configurado
- [x] Navigation Drawer implementado
- [x] Toolbar com opção "Sobre"
- [x] RecyclerView com CardView
- [x] SearchView implementado
- [x] Material Design aplicado
- [x] TextInputLayout para validações
- [ ] Firebase Authentication (simplificado para login local)
- [ ] Firebase Realtime Database (pendente)

## 👨‍💻 Desenvolvedor

- **Nome:** Jurley Colares Ribeiro
- **Instituição:** IFRS - Instituto Federal do Rio Grande do Sul | Campus Porto Alegre
- **Disciplina:** Programação para Web III - POA-SSI502

## 📄 Licença

Este projeto é desenvolvido para fins acadêmicos como trabalho final da disciplina.

## 🤝 Contribuições

Projeto acadêmico individual. Sugestões e feedback são bem-vindos!
