package fr.parcoursup.algos.propositions.exemples;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import fr.parcoursup.algos.bacasable.propositions.DemoAlgoPropositions;
import org.junit.Test;

public class TestDemoAlgoPropositions {

    @Test
    public void main_doit_sarreterQuandLeThreadSinterrompt() {
        // https://stackoverflow.com/a/1164333/6463920
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Void> task = () -> {
            String[] args = {};
            DemoAlgoPropositions.main(args);
            return null;
        };
        Future<Void> future = executor.submit(task);
        try {
            future.get(10, TimeUnit.SECONDS); 
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            // handle the timeout
        }
        // handle the interrupts
        // handle other exceptions
         finally {
            future.cancel(true); // may or may not desire this
        }
    }

}