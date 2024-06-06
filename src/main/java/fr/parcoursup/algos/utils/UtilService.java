package fr.parcoursup.algos.utils;

import java.util.logging.Logger;

/**
 * Classe pour stocker des methodes pour diverses tâches utilitaires.
 */
public class UtilService {
	
	public static final String sautLigne = "\n";
	
	/**
	 * Retourne le message dans un cadre étoilés qui s'adapte a la longueur de ce dernier;
	 * @param message : le message
	 * @return
	 */
	public static String encadrementLog(String message) {
		int nbrEtoile = message.length()+20;
		
		/** Premier saut de ligne **/
		StringBuffer log = new StringBuffer(sautLigne);
		
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
		 log.append("*");
		}
		log.append(sautLigne);
		
		/*                            */
		log.append("*");
		for (int i=0;i<=(nbrEtoile-2);i++) {
			log.append(" ");
		}
		log.append("*");
		log.append(sautLigne);
		
		
		/**          message         **/
		log.append("*          ");
		log.append(message);
		log.append("         *");
		log.append(sautLigne);	
		
		
		/*                            */
		log.append("*");
		for (int i=0;i<=(nbrEtoile-2);i++) {
			log.append(" ");
		}
		log.append("*");
		log.append(sautLigne);
		
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
			log.append("*");
		}	
		
		/**  Saut de ligne final ***/
		log.append(sautLigne);	
		
		return log.toString();
	}

	
	
	/**
	 * Retourne le message dans un cadre étoilés de longueur fixe;
	 * @param message : le message
	 * @return
	 */
	public static String encadrementLog2(String message) {
		int nbrEtoile = 130;
		
		/** Premier saut de ligne **/
		String log = sautLigne;
		
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
			log = log.concat("*");
		}
		log = log.concat(sautLigne);
		
		/**/                    
		log = log.concat("*");
		log = log.concat(sautLigne);	
		
		/**          message         **/
		log = log.concat("*          ");
		log = log.concat(message);
		log = log.concat(sautLigne);	
		log = log.concat("*");
		log = log.concat(sautLigne);
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
			log = log.concat("*");
		}
		
		/**  Saut de ligne final ***/
		log = log.concat(sautLigne);	
		return log;
	}
	
	
	
	public static String petitEncadrementLog(String message) {
		int nbrEtoile = message.length()+20;
		
		/** Premier saut de ligne **/
		StringBuffer log = new StringBuffer(sautLigne);
		
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
		 log.append("*");
		}
		log.append(sautLigne);
		
		
		/**          message         **/
		log.append("*          ");
		log.append(message);
		log.append("         *");
		log.append(sautLigne);	
				
		
		/*****************************/
		for (int i=0;i<=nbrEtoile;i++) {
			log.append("*");
		}	
		
		/**  Saut de ligne final ***/
		log.append(sautLigne);	
		
		return log.toString();
	}
}
