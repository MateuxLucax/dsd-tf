# Trabalho final - Desenvolvimento de Sistemas Paralelos e Distribuídos

## Integrantes

- Guilherme Luiz Lange
- Lucas Moraes Schwambach
- Mateus Lucas Cruz Brandt

## Sistema à ser desenvolvido

### Descrição geral

Sistema de comunicação de mensagens entre dispositivos. A sincronização entre os usuários será feita utilizando Sockets ociosos, que serão responsáveis por trafegar as mensagens. Além disso, as mensagens enviadas serão persistidas. Usuários necessitaram apenas de nome de usuário e senha, podendo adicionar outros usuários para conversa através do nome  usuário, com todo o controle de comunicação sendo realizado por um servidor.

### Requisitos funcionais

- RF1: O sistema deverá manter usuário.
- RF2: O sistema deverá manter mensagens.
- RF3: O sistema deverá manter pedidos de amizade.
- RF4: O sistema deverá manter amizades.
- RF5: O sistema deverá permitir comunicação entre usuários através de mensagens. 

### Específicação preliminar das mensagens

#### Mensagem enviada para outro usuário
Campos:
- Conteúdo da mensagem. 
- Usuário de destino.
Descrição: Mensagem enviada de um amigo, para outro
Retorno: Sucesso ou falha

#### Mensagem recebida de outro usuário
Campos:
- Nenhum
Descrição: Mensagem recebida de um amigo.
Retorno: Um JSON com os dados da mensagem e usuário que enviou. No formato: `{"message": "test", "user": 1}`

#### Cadastro de usuário
Campos: 
- Nome de usuário.
- Senha. 
Descrição: Novo usuário cadastrado no sistema.
Retorno: Sucesso ou falha

#### Pedir usuário em amizade
Campos:
- Usuário atual
- Usuário desejado
Descrição: Envia um pedido de amizade ao usuário desejado
Retorno: Sucesso ou falha

#### Aceitar/rejeitar pedido de amizade
Campos:
- Usuário atual
- Usuário que fez o pedido
- Aceite/rejeição
Descrição: Mensagem enviada quando o usuário que recebeu um pedido de amizade aceita ou rejeita ele
Retorno: Sucesso ou falha

#### Listar pedidos de amizade
Campos:
- Usuário atual
Descrição: Lista os pedidos de amizade que o usuário recebeu mas ainda não aceitou

#### Listar usuários disponíveis para amizade
Campos:
- Usuário atual
Descrição: Lista contendo todos os usuários que não são amigos do usuário atual.
Retorno: Lista JSON, com dados dos usuários para adição. No formato: `[{"user": 1, "name": "robinson"}]`

#### Lista de amigos
Campos:
- Usuário atual
Descrição: Lista todos os amigos do usuário atual.
Retorno: Lista JSON, com dados dos usuários amigos. No formato: `[{"user": 1, "name": "robinson"}]`

#### Listar todas as mensagens entre dois amigos
Campos: 
- Usuário atual
- Usuário amigo
Descrição: Lista todas as mensagens trocadas entre dois amigos.
Retorno: Lista JSON, com dados das mensages. No formato: `[{"message": "test", "sent_at": "2022-01-01T00:12:00.000Z"}]`
