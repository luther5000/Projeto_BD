package Entities;

import java.util.LinkedList;
import java.util.Stack;

public class Carrinho {

    private LinkedList<Livro> livros;
    private int id;

    /**
    * Como essa lista de pilha funciona?
    * Então, a lista vai guardar a quantidade de livros individualmente,
    * tipo a lista livros tem os livros [a, b, c, d, a, c, b, d, a]
    * o primeiro index da pilha vai representar o livro 'a' e na pilha
    * vai ter o index dos livros na lista e o tamanho da pilha é
    * a quantidade de cada livro.
    * <P>Exemplo:
    * <P>livros = [a, b, c, d, a, c, b, d, a]
    * <P>lista de pilha = [[0, 4, 8], [1, 6], [2, 5], [3, 7]]
    * */
    private static LinkedList<Stack<Integer>> quantidade;

    public Carrinho() {
        livros = new LinkedList<>();
        id = -1;
        quantidade = new LinkedList<>();
    }

    public LinkedList<Livro> getLivros() {
        return livros;
    }

    public void setLivro(Livro livro) {
        int livroindex = 0, a = livro.getId();
        boolean flag = false;

        if (!livros.isEmpty()){

            for (Livro x : livros) {

                if (x.getId() == a) {
                    quantidade.get(livroindex).add(livros.size()-1);
                    flag = true;
                    break;
                }
                livroindex++;

            }
            livros.add(livro);

            if(!flag){
                quantidade.add(new Stack<>());
                quantidade.get(quantidade.size()-1).add(livros.size()-1);
            }

        } else {

            livros.add(livro);
            quantidade.add(new Stack<>());
            quantidade.get(quantidade.size()-1).add(livros.size()-1);

        }
    }

    public void setCarrinho(LinkedList<Livro> carrinho){
        for(Livro l : carrinho){
            setLivro(l);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void removeLivro(int i){
        int x = quantidade.get(i).pop();
        livros.remove(x);

        if(x <= quantidade.size() - 1){
            quantidade.remove(x);

        }

        if(!(x >= livros.size())){
            for (int j = 0; j < quantidade.size(); j++) {

                for (int k = 0; k < quantidade.get(j).size(); k++) {

                    if (quantidade.get(j).get(k) > x) {
                        quantidade.get(j).set(k, quantidade.get(j).get(k) - 1);
                    }
                }
            }
        }

    }

    public boolean isEmpty(){
        return livros.isEmpty();
    }

    public String toString(){

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i <quantidade.size(); i++){
            sb.append((i + 1) + " - " + livros.get(i).toString()
                    + "\nquantidade = " + quantidade.get(i).size());
        }
        return sb.toString();
    }

}