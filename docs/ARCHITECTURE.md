# Arquitetura do Cifrabook

## Visão geral

O Cifrabook utiliza uma arquitetura Android baseada em `AppCompatActivity`, Views XML e componentes visuais personalizados. A lógica está distribuída entre as Activities, o gerenciador de recentes e as Views responsáveis por desenho, interação e renderização.

Este documento descreve a organização observada no código atual. Não representa uma proposta de migração arquitetural.

## Fluxos principais

### Importação e abertura de uma cifra

1. `MainActivity` disponibiliza a ação de selecionar um documento.
2. O contrato `ActivityResultContracts.OpenDocument` solicita um arquivo do tipo `application/pdf`.
3. O aplicativo tenta manter a permissão de leitura retornada pelo seletor.
4. `copiarUriParaArmazenamentoInterno` copia o conteúdo para `filesDir`.
5. `RecentManager` registra a URI interna.
6. `MainActivity` inicia `PdfViewerActivity`, enviando a URI pela chave `PDF_URI`.

### Histórico de recentes

`RecentManager` utiliza `SharedPreferences` com o arquivo lógico `cifrabook_prefs` e a chave `recent_cifras`. A lista é serializada em JSON pelo Gson. A implementação limita o histórico armazenado a 20 itens e filtra entradas vazias ou que contenham `msf:`.

A tela principal também filtra entradas inválidas e apresenta até 18 itens na lista exibida.

### Leitura do PDF

`PdfViewerActivity` recebe a URI do documento e utiliza `PdfDocumentView` para apresentar o conteúdo. A View personalizada utiliza `PdfRenderer`, renderização em executor dedicado, cache de bitmaps e tratamento de gestos de escala e deslocamento. A Activity coordena os controles de rolagem automática, o menu flutuante, o progresso da leitura e o metrônomo.

### Afinador

`TunerActivity` solicita a permissão `RECORD_AUDIO`, inicializa `AudioRecord` e processa amostras de áudio para estimar a frequência. Os resultados são apresentados por `TunerView`.

## Relações entre componentes

```text
MainActivity
 ├── RecentManager
 ├── seletor OpenDocument
 └── PdfViewerActivity
      ├── PdfDocumentView
      ├── SpeedRingView
      └── CurvedTitleView

MainActivity
 └── TunerActivity
      └── TunerView
```

## Limites desta documentação

Não foram inferidos contratos internos além do que está declarado no código. Mudanças de arquitetura, extração de camadas ou substituição de componentes ficam fora do escopo desta revisão documental.
