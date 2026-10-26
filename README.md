🎬 Catálogo de Filmes

API para gerenciamento de um catálogo de filmes, desenvolvida em Java 21 utilizando Maven e Mini Spring.

👥 Integrantes

* Joao Henrique
* Maria Karolayne
* Mylena Meirellys
* Victor Gabriel
* ⁠Glaydson
* ⁠Lucas Emanoel

🎯 Domínio

O sistema representa um catálogo de filmes de uma plataforma de streaming.

Cada filme possui:

* ID: gerado automaticamente
* Título: nome do filme
* Duração: duração do filme em minutos

Os dados são armazenados em memória.

▶️ Execução

Para executar o projeto:

mvn clean package

Depois:

java -jar target/catalogo-filmes-1.0.0.jar

A API ficará disponível em:



🔗 Rotas

Método	Rota	Função
GET	/filmes	Lista os filmes
GET	/filmes/{id}	Busca um filme pelo ID
POST	/filmes	Cadastra um filme
PUT	/filmes/{id}	Atualiza um filme
DELETE	/filmes/{id}	Remove um filme

Também é possível filtrar filmes pelo título:

GET /filmes?titulo=Matrix

📌 Regra de Negócio

Um filme pode possuir no máximo 600 minutos de duração.

Caso seja informado um valor maior que 600 minutos, a API retorna:

HTTP 422 - Unprocessable Entity

🤖 Uso de IA

A Inteligência Artificial foi utilizada como ferramenta de apoio durante o desenvolvimento do projeto, auxiliando na criação e adaptação de classes, testes e documentação.

O código foi revisado pelos integrantes do grupo, que são responsáveis por compreender e explicar o funcionamento do projeto.
