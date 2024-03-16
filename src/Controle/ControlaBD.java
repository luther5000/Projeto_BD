package Controle;
import java.sql.*;
import java.util.StringJoiner;

public class ControlaBD {

    private Connection con;

    public ControlaBD() {
        String dbURL = "jdbc:postgresql://localhost:5432/livraria";
        String login = "alecrim";
        String password = "21092004nicolas";
        try {

            con = DriverManager.getConnection(dbURL, login, password);

        } catch (Exception e) {
            System.out.println("Falha na conexão com o banco de dados: " + e);
            System.exit(1);
        }
    }

    /* o insert agr retorna o ID do negocio inserido, caso tenha dado erro, retorna -2, e caso
    * não queira retronar nenhum valor, só colocar false como último argumento, caso queira que
    * retorne, so colocar true */
    public int Insert(String tabela, String infos, boolean querRetornar) {
        try {
            if (querRetornar) {
                Statement st = con.createStatement();
                String consulta = "INSERT INTO " + tabela + " VALUES (" + infos + ") RETURNING id_" + tabela + ";";

                ResultSet rt = st.executeQuery(consulta);
                if (rt.next())
                    return rt.getInt("id_" + tabela);
            } else {
                Statement st = con.createStatement();
                String consulta = "INSERT INTO " + tabela + " VALUES (" + infos + ")";

                return st.executeUpdate(consulta);
            }
        } catch (Exception e) {
            System.out.println("ERRO - INSERT: " + e);
        }
        return -2;
    }

    public int Quantos(String pesquisa, String tabela) {
        try {

            /*caso não seja especificado um campo para procurar, sera realizada a consulta
             * utilizando o *, o que significa que ele ira contar todas as linhas da tabela*/

            if (pesquisa.isEmpty())
                pesquisa = "*";

            Statement st = con.createStatement();
            String consulta = "SELECT COUNT(" + pesquisa + ") FROM " + tabela + ";";

            ResultSet rt = st.executeQuery(consulta);
            return rt.next() ? rt.getInt(1) : -1;
            /*Nico: tive que adicionar a linha acima pq tava dando erro aqui. basicamente onde eu
             * posso botar .next eu coloco pra funcionar*/

        } catch (Exception e) {
            System.out.println("ERRO - QUERRY: " + e);
        }
        return -1;
    }

    private ResultSet pesquisa(String tabela, String argumentos, String pesquisa) throws Exception{
        Statement st = con.createStatement();
        String consulta = "SELECT " + argumentos + " FROM " + tabela + " " + pesquisa + ";";

        ResultSet rt = st.executeQuery(consulta);
        return rt;
    }

    /*Nico:tive que criar pra fazer o login e ta funfando certinho, só add a coluna usuario na
     * tua tabela visse*/
    public ResultSet login(String user, String password, String quem) {
        try {
            ResultSet rt = pesquisa(quem, "*", " WHERE usuario = '" + user + "'");

            if (rt.next()){
                if (password.equalsIgnoreCase(rt.getString("senha")))
                    return rt;
            }
            /*Esse next ta ajeitando o "ponteiro" para pegar a string. Sabe o index de quando se
             * lê um arquivo .txt? Então aquele bagulho lá*/

        } catch (Exception e) {
            System.out.println("ERRO - QUERRY: " + e);
        }
        return null;
    }
    public boolean update(String tabela, String coluna, String novo, String condicao) {
        try {
            Statement st = con.createStatement();
            String consulta = "UPDATE " + tabela + " SET " + coluna + " = " + novo +
                    " " + condicao + ";";

            int a = st.executeUpdate(consulta);
            if (a == 1)
                return true;
        } catch (Exception e) {
            System.out.println("ERRO - UPDATE: " + e);
        }
        return false;
    }

    public ResultSet Select(String atributos, String tabela, String infopesquisa, String pesquisa){
        try {
            return pesquisa(tabela, atributos, " WHERE " + pesquisa +
                    " = " + infopesquisa);
        } catch (Exception e) {
            System.out.println("ERRO - SELECT: " + e);
        }
        return null;
    }

    public void printa(String tabela){
        try{
            ResultSet rt = pesquisa(tabela, "*", "");

            ResultSetMetaData rtMetaData = rt.getMetaData();
            int numeroDeColunas = rtMetaData.getColumnCount();

            while (rt.next()) {
                StringJoiner joiner = new StringJoiner(", ", "[", "]\n");
                for (int coluna = 1; coluna <= numeroDeColunas; coluna++) {
                    String nomeDaColuna = rtMetaData.getColumnName(coluna);
                    joiner.add(nomeDaColuna + " = " + rt.getString(coluna));
                }
                System.out.println(joiner.toString());
            }

        } catch (Exception e){
            System.out.println("ERRO: " + e);
        }
    }

    public void printa(String tabela, String id){
        try{
            ResultSet rt = pesquisa(tabela, "*", " WHERE id_" + tabela + " = " + id);

            ResultSetMetaData rtMetaData = rt.getMetaData();
            int numeroDeColunas = rtMetaData.getColumnCount();

            while (rt.next()) {
                StringJoiner joiner = new StringJoiner(", ", "[", "]\n");
                for (int coluna = 1; coluna <= numeroDeColunas; coluna++) {
                    String nomeDaColuna = rtMetaData.getColumnName(coluna);
                    joiner.add(nomeDaColuna + " = " + rt.getObject(coluna));
                }
                System.out.println(joiner.toString());
            }

        } catch (Exception e){

        }
    }

    public boolean delete(String tabela, String condicao1, String condicao2, boolean deletaTudo){
        try {
            Statement st = con.createStatement();
            if (deletaTudo){
                String consulta = "DELETE FROM " + tabela + ";";

                return st.execute(consulta);
            } else {

                String consulta = "DELETE FROM " + tabela + " WHERE " + condicao1 + " = " + condicao2 + ";";

                return st.execute(consulta);
            }
        } catch (Exception e){
            System.out.println("ERRO: " + e);
        }
        return false;
    }
}