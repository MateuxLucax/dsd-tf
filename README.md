# Trabalho final - Desenvolvimento de Sistemas Paralelos e Distribuídos

## Integrantes

- Guilherme Luiz Lange
- Lucas Moraes Schwambach
- Mateus Lucas Cruz Brandt

## Sistema a ser desenvolvido

## Descrição geral

Sistema de comunicação de mensagens entre dispositivos (Chat). A sincronização entre os usuários será feita utilizando Sockets não ociosos, que serão responsáveis por trafegar as mensagens. Além disso, as mensagens enviadas serão persistidas. Usuários necessitaram apenas de nome de usuário e senha, podendo adicionar outros usuários para conversa através do nome  com todo o controle de comunicação sendo realizado por um servidor.

## Requisitos funcionais

- RF1: O sistema deverá manter usuários.
- RF2: O sistema deverá manter mensagens.
- RF3: O sistema deverá manter pedidos de amizade.
- RF4: O sistema deverá manter amizades.
- RF5: O sistema deverá permitir comunicação entre usuários através de mensagens.
- RF6: O sistemá deverá permitir o envio de mensagens multimidia.

## Específicação preliminar das mensagens

Todas as requisições e respostas são do tipo [JSON](https://www.json.org/json-en.html).

### Enviar mensagem para outro usuário
---
Usuário envia mensagem para amigo.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |
| `friend` | Amigo |
| `type` | Tipo da mensagem
| `content` | Conteúdo das mensagens |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"message": "message sent"}]` |
| Erro | `{"error":"couldn\"t send message to friend due to ..."}` |

### Mensagem recebida de outro usuário
---
Mensagem recebida de um amigo.

#### Retorno
| Condição | Resposta |
| --- | --- |
| Mensagem | `[{"friend": {"user": 1, "name": "robinson"}, "message": "hello work!"}]` |

### Cadastro de usuário
---
Cadastrar usuário no sistema.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `username` | Nome de usuário |
| `password` | Senha |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"message": "user created"}]` |
| Erro | `{"error":"couldn\"t create user due to ..."}` |

### Pedir usuário em amizade
---
Envia um pedido de amizade ao usuário desejado.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |
| `friend` | Usuário desejado |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"message": "invite sent"}]` |
| Erro | `{"error":"couldn\"t invite due to ..."}` |

### Pedido de amizade recebido
---
Descrição: Servidor notifica usuário quando recebe um pedido de amizade

#### Retorno
| Condição | Resposta |
| --- | --- |
| Usuário que enviou o pedido | `[{"user": 1, "name": "robinson"}]` |

### Aceitar/rejeitar pedido de amizade
---
Aceita ou rejeita pedido de amizade.

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |
| `friend` | Usuário que fez o pedido |
| `status` | Aceite/rejeição |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"message": "invite accepted"}]` |
| Erro | `{"error":"couldn\"t accept invite due to ..."}` |

### Pedido de amizade aceitado/rejeitado
---
Servidor notifica usuário quando um pedido de amizade feito foi aceitado ou rejeitado.

#### Retorno
| Condição | Resposta |
| --- | --- |
| Usuário | `[{"user": 1, "name": "robinson"}]` |
| Status | `{"status":"accepted"}` |

### Listar pedidos de amizade
---
Lista os pedidos de amizade que o usuário recebeu mas ainda não aceitou ou rejeitou

#### Requisição
| Campos | Conteúdo |
| --- | --- |
| `user` | Usuário atual |

#### Retorno
| Condição | Resposta |
| --- | --- |
| Sucesso | `[{"user": 1, "name": "robinson"}]` |
| Nenhum usuário disponível | `[]` |
| Erro | `{"error":"couldn\" retrieve friendship invites due to ..."}` |

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

### Listar os amigos
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
