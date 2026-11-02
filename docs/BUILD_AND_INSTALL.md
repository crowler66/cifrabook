# Compilação, testes e instalação

Execute os comandos a partir da raiz do repositório.

```bash
# Compilação de depuração
./gradlew assembleDebug

# Testes unitários locais
./gradlew testDebugUnitTest

# Testes instrumentados em dispositivo/emulador
./gradlew connectedDebugAndroidTest

# Instalação em dispositivo conectado por ADB
./gradlew installDebug
```

Para listar dispositivos conectados:

```bash
adb devices
```

O APK de depuração é normalmente encontrado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Antes de publicar alterações

1. Confira o diff para garantir que mudanças de documentação não alteraram arquivos funcionais.
2. Execute `./gradlew assembleDebug`.
3. Execute os testes disponíveis, quando o ambiente permitir.
4. Teste manualmente importação de PDF, abertura de recentes, zoom/deslocamento, rolagem automática, metrônomo e afinador.
5. Registre no commit o escopo real da alteração.

A execução dos comandos deve ser feita no ambiente Android configurado. A documentação, por si só, não atesta que uma compilação foi executada.
