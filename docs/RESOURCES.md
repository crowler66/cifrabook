# Recursos Android

Os recursos visuais e de interface ficam em `app/src/main/res/`.

| Diretório | Conteúdo observado |
|---|---|
| `drawable/` | Fundos, botões, ícones vetoriais e imagens usados pela interface. |
| `layout/` | Layouts XML para a orientação padrão e item de página PDF. |
| `layout-land/` | Layouts alternativos para orientação paisagem. |
| `mipmap-*` | Ícones do aplicativo em diferentes densidades. |
| `mipmap-anydpi-v26/` | Definições adaptativas de ícone para Android 8.0 ou superior. |
| `values/` | Cores, strings e tema principal. |
| `values-night/` | Tema para modo noturno. |
| `xml/` | Regras de backup e extração de dados. |

Também existe `app/src/main/ic_launcher-playstore.png`, imagem de ícone destinada a materiais de distribuição.

## Cuidados de manutenção

- Preserve recursos referenciados por XML, Kotlin, Manifest ou outros recursos.
- Verifique variantes por densidade e orientação antes de classificar imagens como duplicadas.
- Não remova imagens apenas por não terem referências textuais simples: referências podem ocorrer por nome construído em código ou por mecanismos de recursos.
- Confirme o uso de cada recurso com busca global e compilação antes de qualquer remoção.
