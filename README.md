# Trabalho final - Desenvolvimento de Sistemas Paralelos e Distribuídos

## Integrantes

- Guilherme Luiz Lange
- Lucas Moraes Schwambach
- Mateus Lucas Cruz Brandt

## Descrição geral do sistema

O sistema é um serviço para troca de mensagens (chat).

Para utilizar, os usuários precisam criar uma conta. A conta é identificada por usuário e senha, e também é possível informar o nome completo.

Para conversar com outro usuário, é necessário ser amigo dele. Para tanto, um pedido de amizade deve ser enviado e aceito. O outro usuário pode rejeitar o pedido. Até aceitar ou rejeitar, o pedido fica pendente.

O conteúdo das mensagens pode ser texto ou um áudio. O servidor na verdade está preparado para mensagens com qualquer conteúdo binário (áudio, imagem etc.) desde que não muito grande (há mensagens `put-file` e `get-file` para, respectivamente, armazenar ou recuperar qualquer arquivo binário). A limitação é no cliente Android que suporta somente áudios no momento.

## Requisitos funcionais

- RF1: O sistema deverá manter usuários.
- RF2: O sistema deverá manter mensagens.
- RF3: O sistema deverá manter pedidos de amizade.
- RF4: O sistema deverá manter amizades.
- RF5: O sistema deverá permitir comunicação entre usuários através de mensagens.
- RF6: O sistemá deverá permitir o envio de mensagens de áudio.

## Regras de negócio

- RN1: Um usuário só pode se comunicar com outro quando for amigo deste.
- RN2: Um usuário só se torna amigo de outro quando o primeiro enviar um pedido de amizade e o segundo aceitar esse pedido.

## Especificação das mensagens

Toda mensagem é composta por uma sequência de headers, uma linha em branco, e o corpo da mensagem (JSON ou dados binários). Cada header é um par chave-valor separado por um espaço.  Os headers que consideramos são os seguintes:
- `operation`: Identificador da operação sendo realizada; obrigatório.
- `token`: Token de acesso. Obrigatório dependendo da request, nem todas precisam.
- `body-size`: Tamanho do corpo em bytes; obrigatório (mesmo que seja 0).

Mesmo que o sistema não utilize eles, headers arbitrários podem ser informados.

Esse é o formato das requests. O formato das respostas segue o mesmo formato. O que muda são os headers:
- `status`: Os valores possíveis são `ok` ou `err:<tipo do erro>`, onde `<tipo do erro>` pode ser `internal` (erro do servidor) ou `badRequest` (erro do usuário, na request).

Apresentamos a seguir a especificação de cada mensagem. A não ser onde outro formato é especificado, presuma que o formato de todos os corpos de request e response são JSON.

### "Meta"-requests

|Operação|Requer token?|Resposta|
|---|---|---|
|`get-index`|Não|Array com os nomes de todas as operações disponíveis. Por exemplo: `["get-index", "get-all-error-codes", "create-user", "edit-user", ...]`|

Quando uma operação falha e é necessário informar na resposta o que aconteceu, retornamos um código de erro para que o cliente se responsabilize por mostrar como for melhor. De fato, o cliente Android pode mostrar a mensagem em inglês ou português. A seguinte request é um índice desses códigos de erro.

Qualquer operação, quando falhar, retorna um corpo semelhante a `{"messageCode": "INCORRECT_CREDENTIALS"}`

|Operação|Requer token?|Resposta|
|---|---|---|
|`get-all-error-codes`|Não|Array com os nomes de todos os error codes retornados. Por exemplo: `["INTERNAL", "FAILED_TO_PARSE_JSON", "INCORRECT_CREDENTIALS", "FAILED_TO_SEND_FRIEND_REQUEST", ...]`|

### Requests para manter usuários

<table>
  <tr>
    <th>Operação</th>
    <td><code>create-user</code></td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Não</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>username</code> Nome do usuário (único)</li>
        <li><code>password</code> Senha</li>
        <li><code>fullname</code> Nome completo</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      <ul>
        <li><code>id</code> ID do usuário criado</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>USERNAME_IN_USE</code></li>
        <li><code>FAILED_TO_CREATE_USER</code></li>
      </ul>
    </td>
  </tr>
</table>

edit-user
create-session
whoami
search-users
end-session

get-friend-requests
send-friend-request
finish-friend-request
get-friends
remove-friend

put-file
get-file

send-message
get-messages