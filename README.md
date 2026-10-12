# 🎸 Cifrabook

O **Cifrabook** é um aplicativo Android leve, prático e moderno, desenvolvido em **Kotlin**, projetado para funcionar como uma biblioteca pessoal de cifras musicais em PDF. O principal objetivo é proporcionar uma experiência fluida para músicos e estudantes de violão, permitindo gerir cifras baixadas da internet e tocá-las sem interrupções.

---

## ✨ Funcionalidades Principais

- **📂 Importação Local de PDFs:** Integração nativa com o seletor de ficheiros do Android para importar facilmente qualquer cifra em PDF armazenada no dispositivo.
- **📚 Histórico de Recentes Inteligente:** Mantém o registo automático das últimas cifras abertas (geridas via `RecentManager` e `SharedPreferences`) e exibe o nome real do documento na tela principal.
- **🔍 Leitor Contínuo com Zoom Global:** Converte todas as páginas do PDF e une-as num único documento vertical contínuo. Suporta pinch-to-zoom global (via `PhotoView`) para ampliar a cifra inteira de uma só vez, garantindo máxima nitidez.
- **📜 Rolagem Automática (Auto-Scroll):** Sistema de rolagem vertical aveludada integrado por um `ScrollView`, com controlos de Play/Pause e uma barra de ajuste dinâmico de velocidade (`SeekBar`) para acompanhar o ritmo da música enquanto toca.
- **🌙 Tema Escuro Nativo:** Interface limpa e minimalista otimizada em tons escuros (`#121212`), ideal para ambientes com pouca luz.

---

## 🛠️ Tecnologias e Arquitetura

- **Linguagem:** Kotlin
- **Plataforma:** Android
- **Componentes Nativos:** 
  - `PdfRenderer` (para renderização eficiente de PDFs)
  - `RecyclerView` e `LinearLayoutManager` (para a listagem dinâmica)
  - `ScrollView` e `Handler` (para o motor de rolagem suave)
- **Bibliotecas Externas:**
  - `PhotoView` (`com.github.chrisbanes.photoview:PhotoView`) para gestão avançada de gestos e zoom.
  - `Gson` para serialização e persistência da lista de ficheiros recentes.

---

## 📱 Ambiente de Desenvolvimento e Testes

- **Sistema Operativo:** Linux Debian (`x86_64`)
- **Ferramentas:** Android Studio / Gradle / ADB (Android Debug Bridge)
- **Aparelho Físico de Testes:** Motorola G52 (conectado via USB com depuração ADB ativa)

---

## 🚀 Como Executar o Projeto

1. Certifique-se de ter o **Android Studio** ou o **SDK do Android** configurado no seu ambiente Linux.
2. Conecte o seu smartphone Android via cabo USB com a **Depuração USB** ativada.
3. No terminal do Debian, na pasta raiz do projeto, execute o comando de compilação:
   ```bash
   ./gradlew assembleDebug