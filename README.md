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

O sistema permite headers que não sejam esses, mas não faz nada com eles.

Esse é o formato das requests. As respostas segue o mesmo formato, mas os headers são diferentes:
- `status`: Os valores possíveis são `ok` ou `err:<tipo do erro>`, onde `<tipo do erro>` pode ser `internal` (erro do servidor) ou `badRequest` (erro do usuário, na request).
- `body-size`: Tamanho do corpo em bytes (sempre presente, mesmo que seja 0).

Apresentamos a seguir a especificação de cada mensagem. A não ser onde outro formato é especificado, presuma que o formato de todos os corpos de request e response são JSON.

Outro detalhe é que qualquer request com corpo JSON pode ter possível retorno o código de erro `FAILED_TO_PARSE_JSON`, então omitimos na coluna de "possíves erros".

### Tipos de dados

<table>
  <tr>
    <th>Nome</th>
    <td><code>FriendshipStatus</code></td>
  </tr>
  <tr>
    <th>Possíveis valores</th>
    <td>
      <ul>
        <li><code>"NO_FRIEND_REQUEST"</code>: Usuário não é amigo e não existe nenhum pedido de amizade dele ou enviado para ele</li>
        <li><code>"SENT_FRIEND_REQUEST"</code>: Usuário não é amigo mas lhe enviou um pedido de amizade</li>
        <li><code>"RECEIVED_FRIEND_REQUEST"</code>: Usuário não é amigo mas você enviou um pedido de amizade a ele</li>
        <li><code>"IS_FRIEND"</code>: Usuário é amigo</li>
      </ul>
    </td>
  </tr>
</table>

### "Meta"-requests

Essas requisições não tem erros possíveis nem requerem corpos.

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
    <th>Descrição</th>
    <td>Cria uma conta para um usuário novo</td>
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

<table>
  <tr>
    <th>Operação</th>
    <td><code>create-session</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Operação para fazer login do usuário. Cria uma sessão com um token associado, que é retornado para autenticar o usuário em operações posteriores.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Não</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>username</code></li>
        <li><code>password</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      <ul>
        <li><code>token</code> Token para ser utilizado no header <code>token</code> para acessos posteriores</li>
        <li><code>id</code> ID do usuário</li>
        <li><code>fullname</code> Nome completo do usuário</li>
      </ul>
      Os dois últimos campos retornam os dados do usuário que o cliente não tem (tem somente username e password usados no login) para que tenha os dados completos do usuário.
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>INCORRECT_CREDENTIALS</code></li>
      </ul>
    </td>
  </tr>
</table>


<table>
  <tr>
    <th>Operação</th>
    <td><code>edit-user</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Altera dadaos do usuário</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>newFullname</code> Novo nome completo</li>
        <li><code>newPassword</code> Nova senha</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>Sem resposta</td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>INTERNAL</code> Quando não existe usuário com o ID de usuário associado ao token enviado na request. Indica um bug, não deveria ser possível. </li>
      </ul>
    </td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>whoami</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Retorna os dados completos do usuário logado (exceto senha)</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>Sem corpo</td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      <ul>
        <li><code>username</code></li>
        <li><code>fullname</code></li>
        <li><code>createdAt</code> Timestamp da data de criação do usuário</li>
        <li><code>updatedAt</code> Timestamp da data da última alteração dos dados do usuário (`null` caso nunca)</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>Nenhum</td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>search-users</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Busca de usuários. Permite filtrar por nome. Tem paginação.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>query</code> Filtro por nome de usuário</li>
        <li><code>page</code> Os resultados vem paginados (20 por página), então é necessário informar qual página de resultados está sendo visualizada</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      A resposta é um array com os dados dos usuários cujos nomes contém a string dada em <code>query</code>. Os dados de cada usuários retornados são os seguintes:
      <ul>
        <li><code>id</code></li>
        <li><code>username</code></li>
        <li><code>fullname</code>
        <li><code>friendshipStatus</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>INVALID_PAGE_NUMBER</code> Caso <code>page</code> seja menor que 0.</li>
      </ul>
    </td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>end-session</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Finaliza sessão atual do usuário.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>Sem corpo</td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>Sem resposta</td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>INTERNAL</code> Caso o token enviado não tenha uma sessão correspondente.</li>
      </ul>
    </td>
  </tr>
</table>

### Requests para manter amizades

<table>
  <tr>
    <th>Operação</th>
    <td><code>get-friend-requests</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Lista pedidos de amizade envolvendo o usuário (enviados por ele ou para ele).</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>Sem corpo</td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      Array de pedidos de amizade, onde cada pedido de amizade é um objeto com os seguintes atributos:
      <ul>
        <li>
          <code>from</code> Objeto representando usuário que enviou o pedido:
          <ul>
            <li><code>id</code></li>
            <li><code>username</code></li>
            <li><code>fullname</code></li>
          </ul>
        </li>
        <li>
          <code>to</code> Objeto representando o usuário que recebeu o pedido:
          <ul>
            <li><code>id</code></li>
            <li><code>username</code></li>
            <li><code>fullname</code></li>
          </ul>
        </li>
        <li><code>createdAt</code> Data em que o pedido de amizade foi enviado</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>Nenhum</td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>send-friend-request</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Envia pedido de amizade a um outro usuário</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>userId</code> ID do usuário a quem o pedido vai ser enviado</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      <ul>
        <li><code>messageCode</code> igual a <code>SENT_FRIEND_REQUEST_SUCCESSFULLY</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>FRIEND_REQUEST_TO_YOURSELF</code></li>
        <li><code>NO_USER_WITH_GIVEN_ID</code></li>
        <li><code>YOU_ALREADY_SENT_FRIEND_REQUEST</code></li>
        <li><code>THEY_ALREADY_SENT_FRIEND_REQUEST</code></li>
        <li><code>ALREADY_FRIENDS</code></li>
        <li><code>FAILED_TO_SEND_FRIEND_REQUEST</code></li>
      </ul>
    </td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>finish-friend-request</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Finaliza (aceita ou rejeita) um pedido de amizade. Ao aceitar, o usuário se torna amigo. Em ambos os casos, o pedido é simplesmente exluído no final.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>senderId</code> ID do usuário que enviou o pedido. Isso serve para identificar o pedido (há no máximo 1 pedido de amizade por par de usuários)</li>
        <li><code>accepted</code> Se o pedido foi aceito ou rejeitado.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      <ul>
        <li><code>messageCode</code> com mensagem de código <code>FINISHED_FRIEND_REQUEST_SUCCESSFULLY</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>
      <ul>
        <li><code>FRIEND_REQUEST_TO_YOURSELF</code></li>
        <li><code>FRIEND_REQUEST_NOT_FOUND</code> Caso o usuário com id <code>senderId</code> não enviou um pedido de amizade a você.</li>
        <li><code>FAILED_TO_FINISH_FRIEND_REQUEST</code></li>
      </ul>
    </td>
  </tr>
</table>

get-friends

<table>
  <tr>
    <th>Operação</th>
    <td><code>get-friends</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Retorna lista de amigos do usuário.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>Sem corpo</td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>
      Array com os dados dos usuários amigos, onde cada usuário é representado por um objeto com os seguintes atributos:
      <ul>
        <li><code>id</code></li>
        <li><code>username</code></li>
        <li><code>fullname</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>Nenhum</td>
  </tr>
</table>

<table>
  <tr>
    <th>Operação</th>
    <td><code>remove-friends</code></td>
  </tr>
  <tr>
    <th>Descrição</th>
    <td>Desfaz uma amizade. Os usuários não serão mais amigos e, portanto, não poderão conversar mais. Poderão ainda enviar pedidos de amizade entre si.</td>
  </tr>
  <tr>
    <th>Requer token?</th>
    <td>Sim</td>
  </tr>
  <tr>
    <th>Request</th>
    <td>
      <ul>
        <li><code>friendId</code></li>
      </ul>
    </td>
  </tr>
  <tr>
    <th>Resposta ok</th>
    <td>Sem corpo</td>
  </tr>
  <tr>
    <th>Possíveis erros</th>
    <td>Nenhum. Não importa se o usuário nem era amigo em primeiro lugar, o resultado final é o mesmo (não são amigos), então nesse caso não consideramos erro.</td>
  </tr>
</table>

### Requests para manter arquivos

put-file
get-file

### Requests para manter mensagens

send-message
get-messages