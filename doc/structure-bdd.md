# Description de la base de données

Liste des tables

[[_TOC_]]

Si un identifiant apparaît plusieurs fois dans différentes tables,
il n'est commenté qu'une seule fois,
lors de sa première occurence dans la liste des tables.

## A_ADM

Table contenant la liste des propositions d'admission.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|valeur numérique|Identifiant du candidat|Référence au champ G_CN_COD de la table G_CAN|
|G_TI_COD|valeur numérique|Identifiant de la formation d'inscription|Référence au champ G_TI_COD de la table G_TRI_INS|
|G_TA_COD|valeur numérique|Identifiant de la formation d'affectation|Référence au champ G_TA_COD de la table G_TRI_AFF|
|C_GP_COD|valeur numérique|Identifiant du groupe de classement|Référence au champ C_GP_COD de la table C_GRP|
|A_TA_COD|valeur numérique|Code relatif au type d’admission|Valeurs possibles : 1 = admission en procédure principale, 2 = apprentissage, 3 = admission en procédure complémentaire, 5 = admission en CAES, 10 = inscription par l'établissement|
|A_SV_COD|valeur numérique|Code relatif à la situation du vœu|Référence au champ A_SV_COD de la table A_SIT_VOE|
|C_GI_COD|valeur numérique|Identifiant du groupe de classement internat|Référence au champ C_GI_COD de la table A_REC_GRP_INT|
|I_RH_COD|valeur numérique|Code relatif au régime d'hébergement demandé|Valeurs possibles : 0 = pas d’hébergement en internat demandé, 1 = hébergement en internat demandé|

## A_ADM_DEM

Rapport relatif aux démissions automatiques liées à la mise en oeuvre du répondeur automatique (mis à jour quotidiennement durant la campagne).

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_TI_COD|
|G_TA_COD|
|C_GP_COD|
|I_RH_COD|
|C_GI_COD|
|EST_DEM_PROP|valeur numérique|Code indiquant si la proposition a été rejetée automatiquement|Valeurs possibles : 0 = non, 1 = oui|
|NB_JRS|valeur numérique|Nombre de jours écoulés depuis le début de la campagne||

## A_ADM_PRED_DER_APP

Rapport relatif à la prédiction du dernier rang appelé dans les différents groupes de classement (mis à jour quotidiennement durant la campagne).

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_TA_COD|
|C_GP_COD|
|A_RG_RAN_DER|valeur numérique|Estimation du rang du dernier appelé à la date pivot||
|NB_JRS|

## A_ADM_PROP

Table contenant la liste des nouvelles propositions d’admission (mis à jour quotidiennement durant la campagne).

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_TI_COD|
|G_TA_COD|
|C_GP_COD|
|I_RH_COD|
|C_GI_COD|
|NB_JRS|

## A_REC

Table établissant la liste des formations accessibles dans le cadre de la procédure d'admission.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_TI_COD|
|G_TA_COD|
|A_RC_CAP|valeur numérique|Capacité totale de la formation (indicatif)||
|A_RC_CAP_INT_FIL|valeur numérique|Capacité de l'internat filles éventuellement associé à la formation (indicatif)||
|A_RC_CAP_INT_GAR|valeur numérique|Capacité de l'internat garçons éventuellement associé à la formation (indicatif)||
|A_RC_CAP_INT_MIX|valeur numérique|Capacité de l'internat mixte éventuellement associé à la formation (indicatif)||
|A_RC_CAP_INF|valeur numérique|Nombre de places disponibles au moment du paramétrage (indicatif)||
|A_RC_FLG_TAU_BRS|valeur numérique|Code indiquant si un taux minimum de boursiers doit être appliqué|Valeurs possibles : 0 = non, 1 = oui|
|A_RC_TAU_BRS_REC|valeur numérique|Taux minimum de boursiers|Valeur entre 0 et 100|
|A_RC_FLG_TAU_NON_RES|valeur numérique|Code indiquant si un taux maximum de non résidents doit être appliqué|Valeurs possibles : 0 = non, 1 = oui|
|A_RC_TAU_NON_RES_REC|valeur numérique|Taux maximum de non résidents|Valeur entre 0 et 100|
|A_RC_FLG_FIN_RES_PLA|valeur numérique|Code indiquant la fin de la réservation de places internats dans cette formation|
|C_JA_COD|valeur numérique|Identifiant du jury d’admission pédagogique|Référence au champ C_JA_COD de la table C_JUR_ADM|

## A_REC_GRP 

Table contenant les informations relatives aux groupes d'affectation constitués pour l'accès aux formations.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|C_GP_COD|
|G_TI_COD|
|G_TA_COD|
|C_JA_COD|
|A_RG_PLA|valeur numérique|Nombre de places dans le groupe (indicatif)||
|A_RG_NBR_SOU|valeur numérique|Nombre maximal de propositions simultanées dans ce groupe d'affectation, hors appel par bloc|Valeur susceptible d’évoluer durant la campagne (mise à jour possible par le responsable de l'établissement)|
|A_RG_RAN_LIM|valeur numérique|Rang limite d''appel pour l'appel par bloc dans ce groupe d'affectation|Valeur susceptible d’évoluer durant la campagne (mise à jour possible par le responsable de l'établissement)|
|A_RG_FLG_ADM_STOP ?|valeur numérique|Code indiquant si les admissions sont bloquées pour ce groupe ?|Parmi les valeurs possibles : 0 = non|

## A_REC_GRP_INT

Table contenant les informations relatives à un groupe d’affectation en internat.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|C_GI_COD|
|G_TI_COD|
|G_TA_COD| | |non-null pour les internats de formation|
|G_EA_COD_INS|chaîne de caractères|Identifiant de l’établissement d’inscription|non-null pour les internats d'établissement|
|A_RI_NBR_SOU|valeur numérique|Nombre de recrutements souhaités||

## A_REC_GRP_INT_PROP

Rapport relatif aux barres d'admission dans les groupes d'affectation / formations et internats (mis à jour quotidiennement durant la campagne).

| Colonne | Type | Description | Notes |
| --- | --- | --- | --- |
| C_GI_COD |
| G_TA_COD |
| G_TI_COD |
| C_GP_COD |
| A_RG_RAN_DER | valeur numérique | Valeur de la barre d’admission pour le groupe de candidats concernés | |	
| A_RG_RAN_DER_INT | valeur numérique | Valeur de la barre d’admission pour l’internat concerné	| |
| NB_JRS |

## A_SIT_VOE

Table décrivant les états possibles d'un voeu.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|A_SV_COD|valeur numérique|Code relatif à la situation du vœu||
|A_SV_FLG_AFF|valeur numérique|Code indiquant si le vœu est affecté (proposition envoyée au candidat et non-refusée)|Valeurs possibles : 0 = non, 1 = oui|
|A_SV_FLG_ATT|valeur numérique|Code indiquant si le vœu est en attente de proposition |Valeurs possibles : 0 = non, 1 = oui|
|A_SV_FLG_CLO|valeur numérique|Code indiquant si le vœu est clôturé|Valeurs possibles : 0 = non, 1 = oui|

## A_VOE

Liste des voeux des candidats.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_TA_COD|
|I_RH_COD|
|A_SV_COD|
|A_VE_ORD|valeur numérique|Rang du vœu (ordre de préférence spécifié par le candidat s’il a activé son répondeur auttomatique)||
|A_VE_TYP_MAJ|valeur numérique|Code identifiant les circonstances particulières ayant entraîné la modification de l’état du vœu|Valeurs possibles : 1 = annulation de démission d''un vœu, 10 ou 20 = modification du classement|

## A_VOE_PROP

Rapport relatif aux voeux en attente de proposition, 
avec les rangs des candidats dans les listes d'attente (mis à jour quotidiennement durant la campagne).

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_TI_COD|
|G_TA_COD|
|C_GP_COD|
|I_RH_COD|
|A_VE_RAN_LST_ATT|valeur numérique|Rang du vœu sur la liste d’attente||
|NB_JRS|

## C_CAN_GRP

Table enregistrant les classements des groupes constitués pour l'accès aux formations.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|C_GP_COD|
|I_IP_COD|valeur numérique|Code relatif à l’état d'avancement du dossier|Parmi les valeurs possibles : 5 = dossier reçu et complet, candidat classé|
|C_CG_RAN|valeur numérique|Rang initial du candidat dans le groupe de classement pédagogique||
|C_CG_ORD_APP|valeur numérique|Rang du candidat après calcul de l’ordre d’appel (prise en compte des taux minimum de boursiers et résidents)||
|C_CG_ORD_APP_AFF|valeur numérique|Rang du candidat affiché sur le site Parcoursup|Peut différer de la valeur C_CG_ORD_APP en cas de correction de l’ordre d’appel (par exemple, après détection d’une erreur dans le classement)|

## C_CAN_GRP_INT

Table enregistrant les classements des groupes constitués pour l'accès aux internats.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|C_GI_COD|
|I_IP_COD|
|C_CI_RAN|valeur numérique|Rang du candidat dans le groupe de classement||

## C_GRP

Table contenant les informations relatives aux groupes de classement établis pour l'accès aux formations.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|C_GP_COD|
|C_GP_FLG_PAS_CLA|valeur numérique||Parmi les valeurs possibles, 1 = pas de classement |
|C_JA_COD|valeur numérique|Identifiant du jury d’admission pédagogique|Référence au champ C_JA_COD de la table C_JUR_ADM|
|C_GP_ETA_CLA|valeur numérique|Etat du classement des dossiers|Parmi les valeurs possibles : 2 = terminé|

## C_JUR_ADM

Tables contenant les informations relatives aux jurys d’admission pédagogiques.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|C_JA_COD|valeur numérique|Identifiant du jury d’admission pédagogique||
|G_TI_COD|

## G_CAN

Table contenant les informations relatives aux candidats.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_IC_COD|valeur numérique|Code relatif à l’état d'avancement de la saisie du dossier d'inscription du candidat sur Parcoursup|Parmi les valeurs possibles : -10 = dossier annulé, 100 = candidat inscrit|
|G_CN_BRS|valeur numérique|Code indiquant si le candidat est boursier|Parmi les valeurs possibles : 0 = non boursier, 1 = boursier du secondaire, 2 = boursier de l''enseignement supérieur|
|G_CN_FLG_BRS_CER|valeur numérique|Code identifiant la source de saisie / validation du statut de boursier|Parmi les valeurs possibles : 0 = non certifié, 1 = certifié SIECLE, 2 = certifié par le Chef d'Etablissement|
|I_CL_COD_BAC|chaine de caractères|Identifiant de la série de l’enseignement secondaire ou supérieur à laquelle est rattaché le candidat|Référence au champ I_CL_COD de la table I_CLA|
|G_CN_FLG_RA|valeur numérique|Code indiquant si le candidat a activé son répondeur automatique|Parmi les valeurs possibles : 1 = oui|

## G_FIL

Table contenant les informations relatives aux filières / spécialités / voies / mentions de formation.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_FL_COD|valeur numérique|Code identifiant la filière de formation||

## G_FOR

Table contenant les informations relatives aux types de formations.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_FR_COD|valeur numérique|Code identifiant le type de formation||

## G_PAR

Table contenant les informations relatives au paramétrage de l'application.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_PR_COD|valeur numérique|Code identifiant un paramètre de l’application||
|G_PR_VAL|chaîne de caractères|Valeur du paramètre||

Exemple de valeurs possibles :		
		
|G_PR_COD|G_PR_VAL|Description|
| --- | --- | --- |
|35|19/05/2023:0000|Date du début de la campagne|

## G_TRI_INS

Table listant les formations d'inscription.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_TI_COD|
|G_EA_COD_INS|chaîne de caractères|Identifiant de l’établissement d’inscription||
|G_FR_COD_INS|valeur numérique|Code identifiant le type de formation|Référence au champ G_FR_COD de la table G_FOR|
|G_FL_COD_INS|valeur numérique|Code identifiant la filière de formation|Référence au champ G_FL_COD de la table G_FIL|
|G_TI_FLG_PAR_EFF|valeur numérique|Code indiquant si le paramétrage de la formation a été effectué|Parmi les valeurs possibles : 0 = non effectué|
|G_TI_CLA_INT_UNI|valeur numérique|Code relatif au type d’internat associé (si internat possible)|Parmi les valeurs possibles : -1 = pas d'internat, 0 = internat propre à la formation, 1 = internat commun à plusieurs formations proposées par l'établissement, 2 = internat sans sélection, 3 = internat obligatoire|
|G_TI_ETA_CLA|valeur numérique|Etat du classement des dossiers|Parmi les valeurs possibles : 1 = commencé, 2 = terminé|


## I_INS

Table contenant les informations relatives aux candidatures.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_CN_COD|
|G_TI_COD|
|I_IS_FLC_SEC|valeur numérique|Code indiquant si le candidat est considéré comme étant du secteur pour cette formation|Valeurs possibles : 0 = non, 1 = oui|
|I_IS_VAL|valeur numérique|Code indiquant si la candidature a été validée|Valeurs possibles : 0 = non, 1 = oui|

## J_ORD_APPEL_TMP

Table temporaire dont le contenu est destiné à être injecté dans la table C_CAN_GRP.

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|C_GP_COD|
|G_CN_COD|
|C_CG_ORD_APP|

## SP_G_TRI_AFF

Table des formations

|Colonne|Type|Description|Notes|
| --- | --- | --- | --- |
|G_TA_COD|valeur numérique|Identifiant de la formation d'affectation|Référence au champ G_TA_COD de la table G_TRI_AFF|
|G_FL_COD_AFF|valeur numérique|Code identifiant la filière de formation|Référence au champ G_FL_COD de la table G_FIL|
