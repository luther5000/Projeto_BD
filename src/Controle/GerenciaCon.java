package Controle;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe responsável por fazer as chamadas de ControlaBD.
 * <P>Nela são tratados os erros possíveis que possam ter acontecidos
 * e é gerenciada a conexão com o banco de dados.
 */
public abstract class GerenciaCon extends ControlaBD{

    private HashMap<String, String> qualNomeTabelaBanco = null;
    private Connection connection;
    private int usuarioBanco;

    /**
     * função responsável por criar as conexões com o banco de
     * dados sempre que necessário.
     * retorna o erro de não ter conexão caso ele não consiga criar
     * a conexão
     */
    private boolean criaCon(int quem) throws ConexaoException{

        String dbURL = "";
        String login = "";
        String password = "";

        switch (quem){
            case 0 -> {
                dbURL = "jdbc:postgresql://localhost:5432/livraria";
                login = "cliente_role";
                password = "12345678";
            }

            case 1 -> {
                dbURL = "jdbc:postgresql://localhost:5432/livraria";
                login = "vendedor_role";
                password = "123456";
            }
        }

        if (qualNomeTabelaBanco == null){
            criaQualNomeTabelaBanco();
        }
        try {
            connection = DriverManager.getConnection(dbURL, login, password);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            connection = null;
            throw new ConexaoException();
        }
    }

    /**
     * Função responsável pelos inserts onde o id do que foi
     * inserido deseja ser retornado.
     * @param tabela a tabela onde será inserido.
     * @param infos as informações que serão inseridas na
     *              tabela.
     * @param atributos as colunas que irão receber as
     *                  informações.
     * @return -1 caso algum erro tenha acontecido
     * e a função tenha conseguido lidar com ele.
     * Qualquer outro inteiro positivo ou zero
     * representado o valor retornado.
     * @throws NaoTemConexaoException
     */
    protected int InsertRetornando(String tabela, String infos, String atributos) throws NaoTemConexaoException{
        if (connection != null) {
            try {
                String retornando = "RETURNING id_" + tabela;
                return InsertRetornando(qualNomeTabelaBanco.get(tabela),
                        infos, atributos, retornando, connection);

            } catch (SQLException e) {
                /*
                 * Se der erro, vai tentar fechar a conexão atual.
                 */
                try {
                    connection.close();

                } catch (SQLException f) {
                    /*
                     * Se isso também der erro, ele vai setar como
                     * null a conexão para que o garbage collector
                     * possa excluir a conexão antiga.
                     */
                    connection = null;

                } finally {
                    /*
                     * Tenha tido sucesso em fechar a conexão antiga
                     * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                         * Caso ele não consiga, é necessário avisar
                         * que existe um grave problema: não existe conexão
                         * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                }
            }
            /*
             * Caso tenha sido possível resolver os erros, a função avisa que ela
             * teve um comportamento inesperado e foi possível resolver ele,
             * por isso ela pode ser chamada novamente.
             */
            return -1;
        }
        throw new NaoTemConexaoException();
    }

    /**
     * Função responsável pelos inserts onde não se quer saber
     * o id criado pelo banco para a linha inserida. Pode executar
     * múltipos inserts de uma vez.
     * @Parâmetros: 'tabela' recebe a tabela que vai ser inserida;
     * 'infos' recebe um ArrayList contendo todas as coisas a serem
     * inseridas em cada INSERT; 'atributos' recebe quais as colunas
     * da tabela irão receber os valores em 'infos'.
     * @Retorna:
     * <ul>
     * <li>-2 caso um erro tenha acontecido e não tenha sido possível tratar ele
     * ou seja, não há um conexão estabelecida com o banco de dados;
     * <li>-1 caso um erro tenha acontecido e ele tenha sido tratado;
     * <li>0 caso não tenha conseguido inserir todos os valores;
     * <li>1 caso as inserções tenham acontecido.</li>
     * </ul>
     */
    protected int Insert(String tabela, ArrayList<String> infos,
                       String atributos) throws NaoTemConexaoException{
        if (connection != null) {
            try {
                int funcionou = Insert(tabela, infos, atributos, connection);
                return funcionou;
            } catch (ConexaoException e) {
                /*
                 * Se der erro, vai tentar fechar a conexão atual.
                 */
                try {
                    connection.close();

                } catch (SQLException f) {
                    /*
                     * Se isso também der erro, ele vai setar como
                     * null a conexão para que o garbage collector
                     * possa excluir a conexão antiga.
                     */
                    connection = null;

                } finally {
                    /*
                     * Tenha tido sucesso em fechar a conexão antiga
                     * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                         * Caso ele não consiga, é necessário avisar
                         * que existe um grave problema: não existe conexão
                         * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                }
            }
            /*
             * Caso tenha sido possível resolver os erros, a função avisa que ela
             * teve um comportamento inesperado e foi possível resolver ele,
             * por isso ela pode ser chamada novamente.
             */
            return -1;
        }
        throw new NaoTemConexaoException();
    }

    /**
     * Função responsável para saber se algo existe ou não no banco
     * de dados. Irá executar o SQL "SELECT * FROM 'tabela' WHERE
     * 'coluna' = 'condicao';".
     * @Parâmetros: Recebe o nome da tabela, a coluna a ser usada como
     * comparação e a condição que essa coluna precisa atender.
     * @Retorna:
     * <ul>
     * <li>1 caso exista;
     * <li>0 caso não exista;
     * <li>-1 caso um erro tenha acontecido e tenha sido possível tratar ele,
     * ou seja, deve-se chamar a função de novo;</li>
     * <li>-2 caso um erro tenha acontecido e não tenha sido possível tratar ele
     * ou seja, não há um conexão estabelecida com o banco de dados.
     * </ul>
     */
    protected int Existe(String tabela, String coluna, String condicao) throws NaoTemConexaoException{
        ResultSet rt = null;
        if (connection != null){
            try {
                String pesquisa ="WHERE " + coluna + " = " + condicao + ";";
                rt = Select(tabela, coluna, pesquisa, connection);

                return rt.next() ? rt.getInt(1) : 0;

            } catch (ConexaoException e) {
                /*
                 * Se der erro, vai tentar fechar a conexão atual.
                 */
                try {
                    connection.close();

                } catch (SQLException f) {
                    /*
                     * Se isso também der erro, ele vai setar como
                     * null a conexão para que o garbage collector
                     * possa excluir a conexão antiga.
                     */
                    connection = null;

                } finally {
                    /*
                     * Tenha tido sucesso em fechar a conexão antiga
                     * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                         * Caso ele não consiga, é necessário avisar
                         * que existe um grave problema: não existe conexão
                         * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                }
            } catch (SQLException e){
                /*
                * Apenas necessita informa quem o chamou ser necessário tentar
                * chamá-lo novamente
                 */
            } finally {
                try {
                    if (rt != null)
                        rt.close();
                }catch (SQLException e){

                }
            }
            /*
             * Caso tenha sido possível resolver os erros, a função avisa que ela
             * teve um comportamento inesperado e foi possível resolver ele,
             * por isso ela pode ser chamada novamente.
             */
            return -1;
        }
        throw new NaoTemConexaoException();
    }

    protected ResultSet Select(String tabela, String coluna, String pesquisa) throws NaoTemConexaoException {
        if (connection != null){
            try{
                return Select(tabela, coluna, pesquisa, connection);

            } catch (ConexaoException e) {
                /*
                 * Se der erro, vai tentar fechar a conexão atual.
                 */
                try {
                    connection.close();

                } catch (SQLException f) {
                    /*
                     * Se isso também der erro, ele vai setar como
                     * null a conexão para que o garbage collector
                     * possa excluir a conexão antiga.
                     */
                    connection = null;

                } finally {
                    /*
                     * Tenha tido sucesso em fechar a conexão antiga
                     * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                         * Caso ele não consiga, é necessário avisar
                         * que existe um grave problema: não existe conexão
                         * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                }
            }/*
             * Caso tenha sido possível resolver os erros, a função avisa que ela
             * teve um comportamento inesperado e foi possível resolver ele,
             * por isso ela pode ser chamada novamente.
             */
            return null;
        }
        throw new NaoTemConexaoException();
    }

    /**
     * Função responsável por verificar se o login fornecido está
     * ou não presente no banco de dados e se a senha informada equivale
     * à senha desse login.
     * @Parâmetros: recebe o nome da tabela, o nome do usário (nome de login)
     * e a senha daquele login.
     * @Retorna:
     * <ul>
     * <li>-1 caso algum erro tenha acontecido;</li>
     * <li>0 caso não exista o login;
     * <li>1 caso exista o login e senha esteja correta;
     * <li>2 caso exista o login mas a senha fornecida estava incorreta.</li>
     * </ul>
     * @Excessão: caso tenha havido algum erro com o SQL, e após encerrar
     * a conexão não tenha sido possível criar outra, ele irá retornar
     * ConexaoException
     */
    protected int login(String user, String password, String tabela) throws NaoTemConexaoException{
        ResultSet rt = null;
        if (connection != null){
            try{
                String pesquisa = "WHERE usuario" + " = " + user + ";";
                rt = Select(tabela, "*", pesquisa, connection);
                
                if (rt.next()){
                    if (password.equalsIgnoreCase(rt.getString("senha")))
                        return 1;
                    else
                        return 2;
                }
                return 0;

            } catch (ConexaoException e) {
                /*
                * Se der erro, vai tentar fechar a conexão atual.
                */
                try {
                    connection.close();
                    
                } catch (SQLException f) {
                    /*
                    * Se isso também der erro, ele vai setar como 
                    * null a conexão para que o garbage collector
                    * possa excluir a conexão antiga.
                     */
                        connection = null;
                        
                    } finally {
                    /*
                    * Tenha tido sucesso em fechar a conexão antiga 
                    * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                        * Caso ele não consiga, é necessário avisar
                        * que existe um grave problema: não existe conexão
                        * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                    }
                } catch (SQLException e){
                
            } finally {
                /*
                * Irá fechar tudo o que foi aberto na função
                */
                try {
                    if (rt != null)     
                        rt.close();

                } catch (SQLException a){
                    rt = null;
                }
            }
            /*
            * Caso tenha sido possível resolver os erros, a função avisa que ela
            * teve um comportamento inesperado e foi possível resolver ele,
            * por isso ela pode ser chamada novamente.
             */
            return -1;
        }
        throw new NaoTemConexaoException();
    }
    
    protected int variosUpdatesDiferentesColunas(String tabela, ArrayList<String> coluna, ArrayList<String> novo,
                                          ArrayList<String> condicao) throws NaoTemConexaoException{
        if (connection != null){
            try {
                connection.setAutoCommit(false);
                int totalQuantosUpdates = 0;
                for (int i = 0; i < condicao.size(); ++i) {
                    String mudancas = montaConsultaUpdate(coluna.get(i), novo.get(i));
                    
                    /*
                    * Realização do UPDATE
                     */
                    int quantosUpdates = update(tabela, mudancas, condicao.get(i), connection);
                    if (quantosUpdates == -1){
                        /*
                        * Caso algum erro tenha acontecido, o banco irá retornar
                        * ao seu estado original e então será informado a quem 
                        * chamou essa função que houve um erro.
                         */
                        connection.rollback();
                        connection.setAutoCommit(true);
                        
                        return -1;
                    }
                    
                    totalQuantosUpdates += quantosUpdates;
                }
                
                connection.commit();
                connection.setAutoCommit(true);
                
                return totalQuantosUpdates;
                
            }catch (ConexaoException e) {
                /*
                 * Se der erro, vai retornar o banco ao estado anterior
                 * e tentar fechar a conexão atual.
                 */
                try {
                    connection.rollback();
                    connection.close();

                } catch (SQLException f) {
                    /*
                     * Se isso também der erro, ele vai setar como
                     * null a conexão para que o garbage collector
                     * possa excluir a conexão antiga.
                     */
                    connection = null;

                } finally {
                    /*
                     * Tenha tido sucesso em fechar a conexão antiga
                     * ou não, o sistema tentar criar outra conexão.
                     */
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException f){
                        /*
                         * Caso ele não consiga, é necessário avisar
                         * que existe um grave problema: não existe conexão
                         * com o banco de dados.
                         */
                        throw new NaoTemConexaoException();
                    }
                }
            } catch (SQLException e){
                /*
                * Qualquer erro que possa acontecer com a conexão 
                * deve resultar no encerramento da mesma e no retorno
                * do banco a um estado seguro.
                 */
                try{
                    connection.close();
                } catch (SQLException f) {
                    connection = null;
                    
                } finally {
                    throw new NaoTemConexaoException();
                }
            }
            /*
             * Caso tenha sido possível resolver os erros, a função avisa que ela
             * teve um comportamento inesperado e foi possível resolver ele,
             * por isso ela pode ser chamada novamente.
             */
            return -1;
        }
        throw new NaoTemConexaoException();
    }
    
    protected int variosUpdatesMesmaColuna(String tabela, String coluna, ArrayList<String> novo,
                                           ArrayList<String> condicao) throws NaoTemConexaoException{}

    /**
     * Função responsável por executar deletes que respeitem uma condição.
     * Irá executar um SQL do tipo "DELETE FROM 'tabela' WHERE 'condição';".
     * @Parâmetros: Recebe o nome da tabela e a condição para o delete.
     * @Retorna:
     * <ul>
     * <li>-2 caso um erro tenha acontecido e não tenha sido possível tratar ele
     * ou seja, não há um conexão estabelecida com o banco de dados;
     * <li>-1 caso um erro tenha acontecido e tenha sido possível tratar ele,
     * ou seja, deve-se chamar a função de novo;</li>
     * <li>0 caso nenhum delete tenha sido executado;
     * <li>Qualquer outro valor inteiro positivo correspondente à quantidade
     * de linhas deletadas da tabela.
     * </ul>
     */
    protected int delete(String tabela, String condicao) throws NaoTemConexaoException{
        if (connection != null) {
            try {
                int verifica = delete(tabela, condicao, connection);
                if (verifica == -1){
                    return -3;
                }
                return verifica;
            } catch (ConexaoException e) {
                try {
                    connection.close();
                } catch (SQLException f) {

                } finally {
                    try {
                        criaCon(usuarioBanco);
                    } catch (ConexaoException a) {
                        throw new NaoTemConexaoException();
                    }
                }
            }
            return -1;
        }
        throw new NaoTemConexaoException();
    }

    protected void setUsuarioBanco(int usuarioBanco) {
        this.usuarioBanco = usuarioBanco;
    }

    /**
     * Função responsável por montar SETs para o update.
     * <P>Recebe uma string de colunas que serão alteradas
     * e uma string com os novos valores que serão atribuídos
     * a essas colunas.
     * @param coluna as colunas que terão os valores alterados.
     *               Cada nome de coluna deve estar separado por
     *               um espaço.
     * @param novo oas novos valores que serão atribuídos a cada
     *             colua. Os valores devem estar separados por
     *             um espaço.
     * @return
     */
    private String montaConsultaUpdate(String coluna, String novo){
        /*
         * Primeiro separamos as colunas que serão alteradas
         * e os valores novos para cada uma dessas colunas.
         * Cada alteração possui um índice no arrayList e
         * cada coluna alterada está separada por um espaço.
         */
        String mudancas = "";
        String[]colunasAlteradas = coluna.split(" ");
        String[]valorColunasAlteradas = novo.split(" ");

        /*
         * Aqui fazemos a criação do SET do UPDATE.
         */
        for (int j = 0; j < colunasAlteradas.length; ++j) {
            if (j != coluna.length() - 1)
                mudancas += colunasAlteradas[j] + " = " + valorColunasAlteradas[j] + ", ";
            else
                mudancas += colunasAlteradas[j] + " = " + valorColunasAlteradas[j];
        }
        
        return mudancas;
    }

    /**
     * Cria uma condição de select, update ou delete conforme
     * as necessidades do banco.
     * @param condicao a condição inicial para a operação
     *                 no banco.
     * @param tabela a tabela onde será realizada a operação.
     * @return uma string contendo a nova operação.
     */
    private String montaCondicao(String condicao, String tabela){
        if (condicao.isEmpty())
            condicao = "id_" + tabela + " >= 0";
        else
            condicao = "id_" + tabela + " >= 0 AND " + condicao;

        return condicao;
    }


    /**
     * Função responsável por criar na memória o HashMap
     * para pegar os nomes das tabelas no banco
     */
    private void criaQualNomeTabelaBanco(){
        qualNomeTabelaBanco.put("livro", "Estoque.livro");
        qualNomeTabelaBanco.put("vendedor", "Vendedores_Info.vendedor");
        qualNomeTabelaBanco.put("relatorio", "Vendedores_Info.relatorio");
        qualNomeTabelaBanco.put("relatorio_venda", "Vendedores_Info.relatorio_venda");
        qualNomeTabelaBanco.put("cliente", "Clientes_Info.cliente");
        qualNomeTabelaBanco.put("carrinho", "Clientes_Info.carrinho");
        qualNomeTabelaBanco.put("carrinho_livro", "Clientes_Info.carrinho_livro");
        qualNomeTabelaBanco.put("compra", "Compras_Info.compra");
    }
}
