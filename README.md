# Cifrabook

Aplicativo Android para organizar e consultar cifras musicais em PDF, com leitor integrado, rolagem automática, metrônomo e afinador de instrumentos.

O projeto é desenvolvido em **Kotlin** e utiliza componentes da plataforma Android. Esta documentação descreve a implementação presente no repositório; não pressupõe funcionalidades que ainda não estejam implementadas.

## Funcionalidades

- **Importação de cifras:** seleção de arquivos PDF pelo seletor de documentos do Android.
- **Armazenamento local:** cópia do PDF selecionado para o armazenamento interno privado do aplicativo.
- **Lista de recentes:** registro das cifras abertas, persistido com `SharedPreferences` e serializado com Gson.
- **Leitor de PDF:** renderização das páginas por meio de `PdfRenderer`, em uma visualização personalizada (`PdfDocumentView`).
- **Zoom e navegação:** gestos de ampliação e deslocamento implementados na visualização do documento.
- **Rolagem automática:** controles de reprodução e ajuste de velocidade para acompanhar a cifra.
- **Metrônomo:** controle de BPM e geração de sinais sonoros durante a leitura.
- **Afinador:** captura de áudio pelo microfone e análise da frequência para auxiliar na afinação.
- **Layouts adaptáveis:** recursos específicos para a orientação paisagem nas telas principal e do leitor.

## Tecnologias

| Área | Implementação |
|---|---|
| Linguagem | Kotlin |
| Plataforma | Android |
| Interface | XML e Views personalizadas |
| Renderização de PDF | `PdfRenderer` |
| Persistência de recentes | `SharedPreferences` e Gson |
| Captura de áudio | `AudioRecord` |
| Navegação e seleção de PDF | AndroidX Activity Result API (`OpenDocument`) |
| Interface de lista | `RecyclerView` e `GridLayoutManager` |
| Componentes visuais | AndroidX, Material Components e Views personalizadas |

O catálogo de versões e as dependências efetivamente declaradas estão em `gradle/libs.versions.toml` e `app/build.gradle.kts`. A dependência PhotoView também está declarada no projeto e referenciada em `item_pdf_page.xml`; sua utilização no fluxo atual do leitor deve ser considerada em uma revisão específica antes de qualquer remoção.

## Estrutura do projeto

```text
Cifrabook/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cifrabook/  # Código Kotlin
│   │   │   ├── res/                         # Layouts e recursos Android
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                            # Testes locais
│   │   └── androidTest/                     # Testes instrumentados
│   └── build.gradle.kts                     # Configuração do módulo
├── docs/                                    # Documentação técnica
├── gradle/                                  # Wrapper e catálogo de versões
├── build.gradle.kts                         # Configuração da raiz
├── settings.gradle.kts
├── gradle.properties
├── gradlew
└── README.md
```

## Requisitos de compilação

Consulte as versões configuradas no próprio projeto antes de instalar ou atualizar ferramentas:

- Android Studio com suporte ao Android Gradle Plugin utilizado;
- Android SDK Platform 34;
- Gradle Wrapper do repositório;
- JDK compatível com a versão do Android Gradle Plugin e com a configuração do Gradle Daemon JVM.

Configurações atualmente declaradas:

- `minSdk`: 24;
- `targetSdk`: 34;
- `compileSdk`: 34;
- `applicationId`: `com.example.cifrabook`;
- `versionCode`: 1;
- `versionName`: `1.0`;
- compatibilidade de compilação Java: 11;
- Android Gradle Plugin: versão definida no catálogo `libs.versions.toml`;
- Gradle: distribuição definida em `gradle/wrapper/gradle-wrapper.properties`.

## Compilar e instalar

Na raiz do projeto, execute:

```bash
# Compilar a variante de depuração
./gradlew assembleDebug

# Executar os testes locais
./gradlew testDebugUnitTest

# Executar os testes instrumentados (requer dispositivo ou emulador)
./gradlew connectedDebugAndroidTest

# Instalar a versão de depuração em um dispositivo conectado
./gradlew installDebug
```

No Linux ou macOS, caso necessário, conceda permissão de execução ao wrapper:

```bash
chmod +x gradlew
```

O APK de depuração é normalmente gerado em `app/build/outputs/apk/debug/`.

## Documentação técnica

- [Arquitetura e fluxo do aplicativo](docs/ARCHITECTURE.md)
- [Componentes Kotlin](docs/ANDROID_COMPONENTS.md)
- [Layouts e interface](docs/LAYOUTS.md)
- [Configuração Gradle e dependências](docs/GRADLE_CONFIGURATION.md)
- [Manifest e permissões](docs/ANDROID_MANIFEST.md)
- [Recursos Android](docs/RESOURCES.md)
- [Estrutura e inventário de arquivos](docs/PROJECT_STRUCTURE.md)
- [Compilação e testes](docs/BUILD_AND_INSTALL.md)
- [Registro da auditoria e itens pendentes](docs/REVIEW_NOTES.md)

## Integridade do projeto

A documentação foi organizada para refletir a implementação existente. Alterações de documentação e organização não devem modificar o comportamento do aplicativo. Arquivos de código, layouts, recursos e configurações devem ser preservados até que sua função tenha sido confirmada.

## Estado do projeto

Este repositório representa a versão de desenvolvimento documentada. Consulte o histórico de commits e as notas de revisão antes de considerar qualquer alteração estrutural ou atualização de dependências.
