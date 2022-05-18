package fr.parcoursup.algos.ordreappel.exemples;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.bacasable.ordreappel.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDemoOrdreAppel {

    @BeforeClass
	public static void setUpBeforeClass() {
		LogManager.getLogManager().reset();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void reproduction_de_la_demo_dExemple() throws Exception{
        // Code recopié de DemoOrdreAppel. A terme il faudra enlever la classe DemoOrdreAppel.
        final ExempleA1 exempleA1 = new ExempleA1();
        exempleA1.execute(true);

        final ExempleA2 exempleA2 = new ExempleA2();
        exempleA2.execute(true);

        final ExempleA3 exempleA3 = new ExempleA3();
        exempleA3.execute(true);

        final ExempleA4 exempleA4 = new ExempleA4();
        exempleA4.execute(true);

        final ExempleA5 exempleA5 = new ExempleA5();
        exempleA5.execute(true);

        final ExempleA6 exempleA6 = new ExempleA6();
        exempleA6.execute(true);
        
        // Obligation de recopier tous les examples pour enlever la boucle infinie
        for(int i=0 ; i<100 ; i++) {
            final Random r = new Random();
            final ExempleAleatoire e = new ExempleAleatoire(1 + r.nextInt(1_000));
            e.execute(false);
        }
    }

    @Test
    public void appel_du_main_et_interruption() throws Exception {
        // Pour avoir le coverage de DemoOrdreAppel
        final Duration timeout = Duration.ofSeconds(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<String> handler = executor.submit(() -> {
            DemoOrdreAppel.main(null);
            return "";
        });
        
        try {
            handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            handler.cancel(true);
        }
        
        executor.shutdownNow();
    }

}