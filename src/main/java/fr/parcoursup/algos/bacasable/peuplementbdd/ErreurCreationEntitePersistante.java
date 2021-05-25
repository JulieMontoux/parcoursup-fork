package fr.parcoursup.algos.bacasable.peuplementbdd;

public class ErreurCreationEntitePersistante extends RuntimeException {
    
    public ErreurCreationEntitePersistante(String errorMessage, Throwable err) {
        
        super(errorMessage, err);
        
    }

}
