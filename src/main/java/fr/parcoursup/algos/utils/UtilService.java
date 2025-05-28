package fr.parcoursup.algos.utils;

/**
 * Classe pour stocker des methodes pour diverses tâches utilitaires.
 */
public class UtilService {
	
	public static final String sautLigne = "\n";
	
	/**
	 * Retourne le message dans un cadre étoilés qui s'adapte a la longueur de ce dernier;
	 * @param message : le message
	 * @return le message enjolivé
	 */
	public static String encadrementLog(String message) {
		int nbrEtoile = message.length()+20;
		return sautLigne + "*".repeat(Math.max(0, nbrEtoile + 1)) +
                sautLigne +
                "*" +
                " ".repeat(Math.max(0, nbrEtoile - 1)) +
                "*" +
                sautLigne +
                "*          " +
                message +
                "         *" +
                sautLigne +
                "*" +
                " ".repeat(Math.max(0, nbrEtoile - 1)) +
                "*" +
                sautLigne +
                "*".repeat(Math.max(0, nbrEtoile + 1)) +
                sautLigne;
	}


    public static String petitEncadrementLog(String message) {
		int nbrEtoile = message.length()+20;
        return sautLigne + "*".repeat(Math.max(0, nbrEtoile + 1)) +
                sautLigne +
                "*          " +
                message +
                "         *" +
                sautLigne +
                "*".repeat(Math.max(0, nbrEtoile + 1)) +
                sautLigne;
	}
}
