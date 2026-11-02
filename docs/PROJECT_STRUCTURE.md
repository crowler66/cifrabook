# Estrutura do repositório

```text
Cifrabook/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cifrabook/  # Implementação Kotlin
│   │   │   ├── res/                         # Recursos e layouts Android
│   │   │   ├── keepRules/                   # Regras de preservação
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                            # Testes locais na JVM
│   │   └── androidTest/                     # Testes executados no Android
│   ├── build.gradle.kts
│   └── .gitignore
├── docs/                                    # Documentação do projeto
├── gradle/
│   ├── wrapper/                             # Wrapper do Gradle
│   ├── libs.versions.toml                   # Catálogo de versões
│   └── gradle-daemon-jvm.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── .gitignore
└── README.md
```

## Arquivos que devem ser preservados

- Código Kotlin e layouts atualmente ligados ao aplicativo.
- `AndroidManifest.xml` e recursos referenciados.
- Arquivos Gradle, catálogo de versões, Wrapper e `gradle-wrapper.jar`.
- Regras de preservação em `app/src/main/keepRules/` até confirmar sua ligação com a configuração de build.
- Testes existentes até avaliar seu conteúdo e utilidade.

## Arquivos normalmente locais ou gerados

Os diretórios `.gradle/`, `.kotlin/`, `build/` e arquivos locais como `local.properties` não devem ser versionados. O `.gitignore` da raiz foi preparado para ignorar esses artefatos sem remover nada do disco.

## Inventário versus remoção

A presença de um arquivo no inventário não significa que ele possa ser excluído. A decisão exige verificar referências Kotlin, XML, Manifest, Gradle, nomes de recursos e variantes de configuração.
