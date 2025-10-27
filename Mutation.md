# Relatório de Mutantes Equivalentes

A seguir estão listados os mutantes que sobreviveram à análise e são considerados equivalentes ao código original, com suas respectivas justificativas.

## br.ifsp.demo.domain.service.CategoryService

| Linha | Mutante (Operador) | Justificativa da Equivalência |
| :---: | :--- | :--- |
| 127 | `changed conditional boundary` | O mutante troca `>= 0` por `> 0`. A variável `slash` vem de `lastIndexOf('/')`. Como os caminhos (`path`) nunca começam com `/`, é impossível que `lastIndexOf('/')` retorne `0`. Portanto, a condição se comporta de forma idêntica para todos os casos de entrada válidos (`-1` ou `> 0`). |
| 153 | `changed conditional boundary` | Mesma justificativa da linha 127. O valor de `slash` nunca pode ser `0`, tornando a troca de `>= 0` para `> 0` semanticamente irrelevante. |