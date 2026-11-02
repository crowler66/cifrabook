# Layouts e interface

Os layouts XML estão em `app/src/main/res/layout/` e `app/src/main/res/layout-land/`.

| Arquivo | Uso |
|---|---|
| `layout/activity_main.xml` | Interface principal em orientação padrão. |
| `layout-land/activity_main.xml` | Variante da tela principal para paisagem. |
| `layout/activity_pdf_viewer.xml` | Interface do leitor de PDF em orientação padrão. |
| `layout-land/activity_pdf_viewer.xml` | Variante do leitor para paisagem. |
| `layout/activity_tuner.xml` | Interface do afinador. |
| `layout/item_pdf_page.xml` | Layout de página PDF que contém `PhotoView`; sua utilização no fluxo atual precisa ser confirmada. |

## Componentes personalizados nos layouts

- `com.example.cifrabook.PdfDocumentView`: área de renderização e interação com o documento.
- `com.example.cifrabook.TunerView`: mostrador do afinador.
- `com.example.cifrabook.SpeedRingView`: indicador circular associado ao controle de velocidade.
- `com.example.cifrabook.CurvedTitleView`: título com desenho curvado.

## Diretriz de manutenção

As variantes `layout` e `layout-land` devem ser analisadas em conjunto. Um recurso que não aparece em uma orientação pode ser necessário na outra. Não renomeie IDs, remova componentes ou altere constraints sem conferir todas as referências em Kotlin e nos demais layouts.
