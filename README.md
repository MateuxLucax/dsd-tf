# Trabalho final - Desenvolvimento de Sistemas Paralelos e Distribuídos

## Integrantes

- Guilherme Luiz Lange
- Lucas Moraes Schwambach
- Mateus Lucas Cruz Brandt

## Sistema a ser desenvolvido

## Descrição geral

Sistema de comunicação de mensagens entre dispositivos. A sincronização entre os usuários será feita utilizando Sockets ociosos, que serão responsáveis por trafegar as mensagens. Além disso, as mensagens enviadas serão persistidas. Usuários necessitaram apenas de nome de usuário e senha, podendo adicionar outros usuários para conversa através do nome  com todo o controle de comunicação sendo realizado por um servidor.

## Requisitos funcionais

- RF1: O sistema deverá manter usuários.
- RF2: O sistema deverá manter mensagens.
- RF3: O sistema deverá manter pedidos de amizade.
- RF4: O sistema deverá manter amizades.
- RF5: O sistema deverá permitir comunicação entre usuários através de mensagens.

## Específicação preliminar das mensagens

Todas as requisições e respostas são do tipo [JSON](https://www.json.org/json-en.html).

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

Descrição: Envia um pedido de amizade ao usuário desejado.

Retorno: Sucesso ou falha

#### Pedido de amizade recebido
Campos:
- Usuário que enviou o pedido

Descrição: Servidor notifica usuário quando recebe um pedido de amizade

#### Aceitar/rejeitar pedido de amizade
Campos:
- Usuário atual
- Usuário que fez o pedido
- Aceite/rejeição

Descrição: Servidor notifica usuário quando recebe um pedido de amizade.

Retorno: Sucesso ou falha

#### Pedido de amizade aceitado/rejeitado
Campos:
- Usuário que aceitou/rejeitou o pedido
- Status (pedido aceitado ou rejeitado)

Descrição: Servidor notifica usuário quando um pedido de amizade feito foi aceitado ou rejeitado.

#### Listar pedidos de amizade
Campos:
- Usuário atual

Descrição: Lista os pedidos de amizade que o usuário recebeu mas ainda não aceitou ou rejeitou

### Listar usuários disponíveis para amizade
---
Lista contendo todos os usuários que não são amigos do usuário atual.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"user": 1, "name": "robinson"}]` |
| Nenhum usuário disponível | `[]` |
| Erro | `{"error":"couldn\" retrieve users due to ..."}` |

### Lista de amigos
---
Lista todos os amigos do usuário atual.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"user": 1, "name": "robinson"}]` |
| Nenhum amigo | `[]` |
| Erro | `{"error":"couldn\" retrieve friends due to ..."}` |


### Listar as últimas N mensagens entre dois amigos
---
Lista das N mensagens anteriores à mensagem de ID dado. Se nenhuma mensagem for dada, pega as últimas N mensagens trocadas.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |
| `friend` | Usuário amigo |
| `offset` | ID da mensagem (opcional) |
| `limit` | Número de mensagens a carregar (opcional) |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"message": "test", "sent_at": "2022-01-01T00:12:00.000Z"}]` |
| Nenhuma mensagem | `[]` |
| Erro | `{"error":"couldn\" retrieve messages due to ..."}` |