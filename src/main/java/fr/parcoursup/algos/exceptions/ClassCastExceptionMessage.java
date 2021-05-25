package fr.parcoursup.algos.exceptions;

public enum ClassCastExceptionMessage {

    MESSAGE("%s"),

    GLOBAL_TEST_EGALITE_IMPREVU("Test d'égalité imprévu"),

    VOEU_CLASSE_TEST_EGALITE_IMPREVU("equals test entre un VoeuClasse et un autre objet de type différent")

    ;

    private final String message;

    ClassCastExceptionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
    
}
