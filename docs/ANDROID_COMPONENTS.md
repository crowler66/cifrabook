# Componentes Kotlin

Pacote principal: `com.example.cifrabook`.

| Arquivo | Responsabilidade observada |
|---|---|
| `MainActivity.kt` | Tela inicial, seleção/importação de PDF, apresentação dos recentes e navegação para leitor e afinador. |
| `PdfViewerActivity.kt` | Coordenação da leitura, controles de rolagem, velocidade, menu flutuante, progresso e metrônomo. |
| `PdfDocumentView.kt` | View personalizada que abre e renderiza o PDF com `PdfRenderer`, gerencia páginas, cache, zoom e deslocamento. |
| `RecentManager.kt` | Persistência e recuperação da lista de PDFs recentes usando `SharedPreferences` e Gson. |
| `TunerActivity.kt` | Permissão de microfone, captura com `AudioRecord`, análise de frequência e ciclo de vida da captura. |
| `TunerView.kt` | Desenho da interface visual do afinador. |
| `SpeedRingView.kt` | Desenho do indicador circular de velocidade utilizado nos controles do leitor. |
| `CurvedTitleView.kt` | Desenho de texto curvado utilizado na interface do leitor. |

## Observações para manutenção

- As Views personalizadas são referenciadas diretamente pelos layouts XML; não devem ser consideradas código órfão apenas por não serem Activities.
- `PdfDocumentView` é a implementação usada pelo leitor nas duas versões de `activity_pdf_viewer.xml`.
- `PhotoView` aparece na dependência Gradle e em `res/layout/item_pdf_page.xml`. A busca estática não encontrou referência Kotlin a esse layout; isso é um ponto de investigação, não autorização para exclusão.
- Os arquivos de teste existentes são exemplos padrão do Android e devem ser avaliados separadamente antes de remoção.

## Regra de alteração

Comentários e documentação podem ser aprimorados sem alterar expressões, condições, valores, ordem de chamadas, assinaturas ou fluxo de execução. Qualquer mudança de código funcional deve ser tratada como tarefa separada e validada por compilação e testes.
