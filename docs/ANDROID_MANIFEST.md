# AndroidManifest.xml

Arquivo: `app/src/main/AndroidManifest.xml`.

## Permissão declarada

| Permissão | Finalidade observada |
|---|---|
| `android.permission.RECORD_AUDIO` | Permite que o afinador solicite acesso ao microfone para capturar áudio. |

A permissão é declarada no Manifest e solicitada em tempo de execução por `TunerActivity`.

## Activities declaradas

| Activity | Exportada | Configuração relevante |
|---|---:|---|
| `.MainActivity` | `true` | Activity de entrada, com os filtros `MAIN` e `LAUNCHER`. |
| `.PdfViewerActivity` | `false` | Declara tratamento de `orientation`, `screenSize` e `keyboardHidden` por `configChanges`. |
| `.TunerActivity` | `false` | Orientação retrato e tema `Theme.AppCompat.NoActionBar`. |

## Configurações da aplicação

- Backup automático desativado (`allowBackup="false"`).
- Ícones definidos por recursos mipmap.
- Nome exibido obtido de `@string/app_name`.
- Tema principal definido como `@style/Theme.Cifrabook`.
- Suporte a RTL habilitado (`supportsRtl="true"`).

## Diretriz

Não altere `exported`, filtros de Intent, permissões, `configChanges`, orientação ou temas sem avaliar os efeitos no ciclo de vida e na navegação do aplicativo.
