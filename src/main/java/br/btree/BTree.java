package br.btree;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BTreeNode {
    List<Integer> keys;
    List<BTreeNode> children;
    boolean isLeaf;

    public BTreeNode() {
        // Construtor para a classe BTreeNode
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = true;
    }
}

public class BTree {
    private BTreeNode root;
    private int t; // Ordem da Árvore B

    public BTree(int t) {
        // Construtor para a classe BTree
        this.root = new BTreeNode();
        this.t = t;
    }

    public void insert(int key) {
        // Inserir chave na Árvore B
        BTreeNode r = root;
        if (r.keys.size() == (2 * t - 1)) {
            // Se a raiz estiver cheia, criar uma nova raiz
            BTreeNode s = new BTreeNode();
            root = s;
            s.children.add(r);
            splitChild(s, 0);
            insertNonFull(s, key);
        } else {
            // Se a raiz não estiver cheia, inserir na raiz não cheia
            insertNonFull(r, key);
        }
    }

    private void insertNonFull(BTreeNode x, int key) {
        // Inserir chave em um nó não cheio
        int i = x.keys.size() - 1;

        if (x.isLeaf) {
            // Se o nó for uma folha, inserir a chave na folha
            x.keys.add(0); // Valor fictício
            while (i >= 0 && key < x.keys.get(i)) {
                x.keys.set(i + 1, x.keys.get(i));
                i--;
            }
            x.keys.set(i + 1, key);
        } else {
            // Se o nó não for uma folha, encontrar o filho para inserir
            while (i >= 0 && key < x.keys.get(i)) {
                i--;
            }
            i++;
            if (x.children.get(i).keys.size() == (2 * t - 1)) {
                // Se o filho estiver cheio, dividir o filho
                splitChild(x, i);
                if (key > x.keys.get(i)) {
                    i++;
                }
            }
            // Inserir recursivamente no filho não cheio
            insertNonFull(x.children.get(i), key);
        }
    }

    private void splitChild(BTreeNode x, int i) {
        // Dividir o i-ésimo filho do nó x
        BTreeNode y = x.children.get(i);
        BTreeNode z = new BTreeNode();
        x.children.add(i + 1, z);
        x.keys.add(i, y.keys.get(t - 1));
        z.keys.addAll(y.keys.subList(t, 2 * t - 1));
        y.keys.subList(t - 1, 2 * t - 1).clear();

        if (!y.isLeaf) {
            // Se y não for uma folha, ajustar os filhos
            z.isLeaf = false;
            z.children.addAll(y.children.subList(t, 2 * t));
            y.children.subList(t, 2 * t).clear();
        }
    }

    public void delete(int key) {
        // Excluir chave da Árvore B
        deleteKey(root, key);
        if (root.keys.isEmpty() && !root.isLeaf) {
            // Se a raiz estiver vazia e não for uma folha, atualizar a raiz
            root = root.children.get(0);
        }
    }

    private void deleteKey(BTreeNode x, int key) {
        // Excluir chave do nó x
        int index = findKeyIndex(x, key);

        if (index < x.keys.size() && x.keys.get(index) == key) {
            // Se a chave estiver neste nó, excluí-la
            deleteKeyFromNode(x, key, index);
        } else {
            if (x.isLeaf) {
                // Se o nó for uma folha, a chave não está presente
                System.out.println("A chave " + key + " não está presente na árvore.");
                return;
            }

            BTreeNode child = x.children.get(index);
            if (child.keys.size() == t - 1) {
                // Se o filho tiver o número mínimo de chaves, pedir emprestado ou mesclar
                borrowFromAdjacent(x, index);
            }

            if (x.children.size() > index) {
                // Excluir recursivamente do filho apropriado
                deleteKey(child, key);
            } else {
                deleteKey(x.children.get(x.children.size() - 1), key);
            }
        }
    }

    private void deleteKeyFromNode(BTreeNode x, int key, int index) {
        // Excluir chave do nó x no índice fornecido
        if (x.isLeaf) {
            // Se o nó for uma folha, simplesmente remover a chave
            x.keys.remove(index);
        } else {
            // Se o nó não for uma folha, substituir a chave pelo predecessor e excluir recursivamente
            BTreeNode pred = getPredecessor(x, index);
            int predKey = pred.keys.get(pred.keys.size() - 1);
            x.keys.set(index, predKey);
            deleteKey(pred, predKey);
        }
    }

    private BTreeNode getPredecessor(BTreeNode x, int index) {
        // Obter o predecessor da chave no índice fornecido no nó x
        BTreeNode curr = x.children.get(index);
        while (!curr.isLeaf) {
            curr = curr.children.get(curr.children.size() - 1);
        }
        return curr;
    }

    private void borrowFromAdjacent(BTreeNode x, int index) {
        // Pedir emprestado do irmão à esquerda ou à direita, ou mesclar com o irmão
        if (index > 0 && x.children.get(index - 1).keys.size() >= t) {
            borrowFromLeftSibling(x, index);
        } else if (index < x.children.size() && x.children.get(index + 1).keys.size() >= t) {
            borrowFromRightSibling(x, index);
        } else {
            mergeWithAdjacent(x, index);
        }
    }

    private void borrowFromLeftSibling(BTreeNode x, int index) {
        // Pedir emprestado do irmão à esquerda
        BTreeNode child = x.children.get(index);
        BTreeNode leftSibling = x.children.get(index - 1);

        child.keys.add(0, x.keys.get(index - 1));
        x.keys.set(index - 1, leftSibling.keys.get(leftSibling.keys.size() - 1));

        if (!leftSibling.isLeaf) {
            child.children.add(0, leftSibling.children.get(leftSibling.children.size() - 1));
            leftSibling.children.remove(leftSibling.children.size() - 1);
        }

        leftSibling.keys.remove(leftSibling.keys.size() - 1);
    }

    private void borrowFromRightSibling(BTreeNode x, int index) {
        // Pedir emprestado do irmão à direita
        BTreeNode child = x.children.get(index);
        BTreeNode rightSibling = x.children.get(index + 1);

        child.keys.add(x.keys.get(index));
        x.keys.set(index, rightSibling.keys.get(0));

        if (!rightSibling.isLeaf) {
            child.children.add(rightSibling.children.get(0));
            rightSibling.children.remove(0);
        }

        rightSibling.keys.remove(0);
    }

    private void mergeWithAdjacent(BTreeNode x, int index) {
        // Mesclar com o irmão à esquerda ou à direita
        BTreeNode child = x.children.get(index);
        BTreeNode rightSibling = x.children.get(index + 1);

        child.keys.add(x.keys.get(index));
        child.keys.addAll(rightSibling.keys);

        if (!rightSibling.isLeaf) {
            child.children.addAll(rightSibling.children);
        }

        x.keys.remove(index);
        x.children.remove(index + 1);
    }

    private int findKeyIndex(BTreeNode x, int key) {
        // Encontrar o índice da chave no nó x
        int index = 0;
        while (index < x.keys.size() && key > x.keys.get(index)) {
            index++;
        }
        return index;
    }

    public void print() {
        // Imprimir a Árvore B
        printNode(root, 0);
    }

    private void printNode(BTreeNode node, int level) {
        // Imprimir o nó e recursivamente imprimir os filhos
        System.out.print("Nível " + level + ": ");
        for (int key : node.keys) {
            System.out.print(key + " ");
        }
        System.out.println();

        if (!node.isLeaf) {
            for (BTreeNode child : node.children) {
                printNode(child, level + 1);
            }
        }
    }

    public static void main(String[] args) {
        // Método principal para interação com o usuário
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite a ordem da Árvore B:");
        int order = scanner.nextInt();
        BTree bTree = new BTree(order);

        while (true) {
            System.out.println("\n1. Inserir");
            System.out.println("2. Excluir");
            System.out.println("3. Imprimir");
            System.out.println("4. Sair");
            System.out.println("Escolha uma opção:");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Digite o número a ser inserido:");
                    int insertKey = scanner.nextInt();
                    bTree.insert(insertKey);
                    break;
                case 2:
                    System.out.println("Digite o número a ser excluído:");
                    int deleteKey = scanner.nextInt();
                    bTree.delete(deleteKey);
                    break;
                case 3:
                    System.out.println("Árvore B:");
                    bTree.print();
                    break;
                case 4:
                    System.out.println("Saindo do programa.");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }
}
