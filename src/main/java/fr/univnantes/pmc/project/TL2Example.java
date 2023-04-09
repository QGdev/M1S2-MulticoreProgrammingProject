package fr.univnantes.pmc.project;

import fr.univnantes.pmc.project.api.AbortException;
import fr.univnantes.pmc.project.api.Register;
import fr.univnantes.pmc.project.api.Transaction;
import fr.univnantes.pmc.project.impl.RegisterImpl;
import fr.univnantes.pmc.project.impl.TL2Dictionary;
import fr.univnantes.pmc.project.impl.TL2Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TL2Example {

    public static void main(String[] args) throws AbortException, InterruptedException {
        Register<Integer> X = new RegisterImpl<>(4, 0);
        Register<Integer> Y = new RegisterImpl<>(1, 0);

        Transaction transaction = new TL2Transaction();

        while (!transaction.isCommitted()) {
            try {
                transaction.begin();
                Integer x = X.read(transaction);
                System.out.println(x);
                X.write(transaction, Y.read(transaction));
                Y.write(transaction, x);
                System.out.println(X.read(transaction));
                System.out.println(Y.read(transaction));
                transaction.tryToCommit();
            } catch (AbortException e) {
                e.printStackTrace();
            }
        }

        TL2Dictionary dico = new TL2Dictionary();
        String letters = "abcdefghijklmnopqrstuvwxyz";
        List<String> words = new ArrayList<>();
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        //  Will generate words of length 1 to 5 with letters from the string letters
        for (int i = 1; i <= 2; i++) {
            for (int j = 0; j < Math.pow(letters.length(), i); j++) {
                StringBuilder word = new StringBuilder();
                int k = j;
                for (int l = 0; l < i; l++) {
                    word.append(letters.charAt(k % letters.length()));
                    k /= letters.length();
                }
                words.add(word.toString());
                queue.add(word.toString());
            }
        }

        Thread[] t = new Thread[10];
        for (int i = 0; i < t.length; i++) {
            t[i] = new Thread(() -> {
                while (!queue.isEmpty()) {
                    String word = queue.poll();
                    if (word != null) {
                        try {
                            System.out.println("Add : " + word + " : " + dico.add(word));
                        } catch (AbortException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        for (int i = 0; i < t.length; i++) {
            t[i].start();
        }
        for (int i = 0; i < t.length; i++) {
            t[i].join();
        }

        boolean testPassed = true;
        for (String word : words) {
            boolean test = dico.contains(word);
            testPassed = testPassed && test;
            System.out.println("Contains : " + word + " : " + test);
        }
        System.out.println("Test passed : " + testPassed);
        System.out.println("END");
    }
}
