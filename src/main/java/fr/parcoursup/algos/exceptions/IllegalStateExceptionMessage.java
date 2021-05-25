package fr.parcoursup.algos.exceptions;

public enum IllegalStateExceptionMessage {

    MESSAGE("%s"),

    VOEU_DONNEE_INDISPONIBLE("Donnée indisponible"),

    REPONDEUR_AUTOMATIQUE_MULTIPLES_PROPOSITIONS("Un candidat ayant activé le répondeur automatique ne peut avoir qu'une proposition active, problème avec les voeux %s et %s"),
    REPONDEUR_AUTOMATIQUE_COMPARAISON_IMPOSSIBLE("Impossible de comparer les voeux %s et %s dans l'ordre du rep auto")

    ;

    private final String message;

    IllegalStateExceptionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }

    public String getMessage(Object... arguments){
        return String.format(this.getMessage(), arguments);
    }
    
}
