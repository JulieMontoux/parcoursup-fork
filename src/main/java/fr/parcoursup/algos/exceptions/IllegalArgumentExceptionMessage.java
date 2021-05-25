package fr.parcoursup.algos.exceptions;

public enum IllegalArgumentExceptionMessage {
    
    MESSAGE("%s"),

    GROUPE_AFFECTATION_INTERNAT_MULTIPLE_SPECIFICATION("Impossible de spécifier à la fois la formation et l'établissement, veuillez ne spécifier dans ce cas que la formation")

    ;

    private final String message;

    IllegalArgumentExceptionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public String getMessage(Object... arguments){
        return String.format(this.getMessage(), arguments);
    }

}
