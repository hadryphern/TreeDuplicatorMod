# Gameplay Spec

## Geracao natural

- A arvore duplicadora deve nascer apenas no bioma `Lush Caves`.
- A geracao precisa acontecer em locais subterraneos, sem visao direta do ceu.
- O spawn deve ser raro o suficiente para ser um evento especial de exploracao.

## Estrutura da arvore

- Tronco vertical de `4` blocos.
- Copa inspirada na oak tree vanilla.
- Texturas atuais reaproveitam a identidade visual vanilla para agilizar a primeira release.

## Regeneracao

- Um `Duplicator Log` colocado pelo jogador no chao inicia um cronometro.
- Apos cerca de `10 minutos`, o tronco regenera a arvore completa.
- A duplicacao so fica habilitada quando a estrutura completa existe.

## Duplicacao

- O gatilho e colocar um bloco adjacente a qualquer tronco da arvore.
- Apenas blocos colocaveis sem block entity sao aceitos na implementacao atual.
- O mod replica o `BlockState` do bloco fonte.
- A area de destino e um quadrado `5x5` centrado na arvore, na mesma altura do bloco gatilho.
- O processo e lento e termina em cerca de `25 minutos`.
- Ao concluir, a arvore trava o ciclo ate o bloco gatilho ser removido e colocado outra vez.

## Limitacoes atuais conhecidas

- Blocos com inventario ou NBT complexo foram bloqueados de proposito nesta primeira versao para evitar corrupcao.
- O port de `Fabric` ainda nao recebeu a logica final.
- O port de `Forge 1.7.10` ainda nao foi iniciado no repositorio.
