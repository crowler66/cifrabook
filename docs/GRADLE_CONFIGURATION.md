# Gradle e dependências

## Arquivos de configuração

| Arquivo | Função |
|---|---|
| `settings.gradle.kts` | Define repositórios de plugins e dependências, nome do projeto e módulo incluído. |
| `build.gradle.kts` | Configuração Gradle da raiz; declara o plugin Android para uso nos módulos. |
| `app/build.gradle.kts` | Configuração do módulo Android, SDKs, identificadores, versões, opções de compilação e dependências. |
| `gradle/libs.versions.toml` | Catálogo central de versões e aliases de bibliotecas/plugins. |
| `gradle.properties` | Opções globais do Gradle e estilo Kotlin. |
| `gradle/wrapper/gradle-wrapper.properties` | Distribuição Gradle usada pelo Wrapper. |
| `gradle/gradle-daemon-jvm.properties` | Configuração gerada/gerenciada do toolchain da JVM do Gradle Daemon. |
| `gradlew` / `gradlew.bat` | Scripts do Gradle Wrapper para Unix e Windows. |

## Configurações declaradas no módulo `app`

- `namespace` e `applicationId`: `com.example.cifrabook`.
- `compileSdk`: 34.
- `minSdk`: 24.
- `targetSdk`: 34.
- `versionCode`: 1.
- `versionName`: `1.0`.
- Compatibilidade de origem e destino Java: 11.
- Minificação da variante `release`: desativada.

## Dependências declaradas

As versões do AndroidX, Material Components, JUnit e Espresso são centralizadas em `gradle/libs.versions.toml`. O módulo também declara diretamente:

- `com.github.chrisbanes:PhotoView:2.3.0`;
- `com.google.code.gson:gson:2.10.1`.

A configuração de repositórios inclui Google, Maven Central, Gradle Plugin Portal (para plugins) e JitPack (para dependências).

## Cuidados

Esta revisão não atualiza AGP, Gradle, SDK, JDK ou bibliotecas. Atualizações devem ser feitas isoladamente, verificando compatibilidade e executando uma compilação completa. O Wrapper e seu JAR são arquivos de projeto e devem ser preservados.
