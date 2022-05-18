-------------------------------------------------------
--  Création de la table A_ADM
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_ADM"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "A_TA_COD" NUMBER(3,0) NOT NULL,
    "A_SV_COD" NUMBER(3,0) NOT NULL,
    "C_GP_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(3,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TA_COD", "I_RH_COD")
);

--------------------------------------------------------
--  Création de la table A_ADM
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_ADM_DEM"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "I_RH_COD" NUMBER(3,0) NOT NULL,
    "C_GP_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(8,0),
   	"A_AD_TYP_DEM" NUMBER(3,0) NOT NULL,
    "EST_DEM_PROP" NUMBER(1,0) NOT NULL,
    "NB_JRS" NUMBER(3,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS")
);

 --------------------------------------------------------
 --  Création de la table A_ADM_PRED_DER_APP
 --------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_ADM_PRED_DER_APP"
(   "G_TA_COD" NUMBER(8,0) NOT NULL,
    "C_GP_COD" NUMBER(8,0) NOT NULL,
    "A_RG_RAN_DER" NUMBER(8,0) NOT NULL,
    "NB_JRS" NUMBER(3,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_TA_COD", "C_GP_COD", "NB_JRS")
);

 --------------------------------------------------------
 --  Création de la table A_ADM_PROP
 --------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_ADM_PROP"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "I_RH_COD" NUMBER(1,0) NOT NULL,
    "C_GP_COD" NUMBER(8,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0) NOT NULL,
    "C_GI_COD" NUMBER(8,0),
    "A_AM_FLG_MBC" NUMBER(1,0),
    "NB_JRS" NUMBER(3,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS")
);

--------------------------------------------------------
--  DDL de la Table A_REC
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_REC"
(   "G_TI_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "A_RC_CAP" NUMBER(5,0),
    "A_RC_CAP_INT_FIL" NUMBER(5,0),
    "A_RC_CAP_INT_GAR" NUMBER(5,0),
    "A_RC_CAP_INF" NUMBER(4,0),
    "A_RC_FLG_TAU_BRS" NUMBER(1,0),
    "A_RC_TAU_BRS_REC" NUMBER(3,0),
    "A_RC_FLG_TAU_NON_RES" NUMBER(1,0),
    "A_RC_TAU_NON_RES_REC" NUMBER(3,0),
    "A_RC_FLG_FIN_RES_PLA" NUMBER(1,0),
    "A_RC_CAP_INT_MIX" NUMBER(5,0),
    "C_JA_COD" NUMBER(6,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_TI_COD", "G_TA_COD")
);

--------------------------------------------------------
--  Création de la table A_REC_GRP
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_REC_GRP"
(   "C_GP_COD" NUMBER(6,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "C_JA_COD" NUMBER(6,0) NOT NULL,
    "A_RG_PLA" NUMBER(5,0),
    "A_RG_NBR_SOU" NUMBER(5,0),
    "A_RG_RAN_LIM" NUMBER(5,0),
    "A_RG_FLG_ADM_STOP" NUMBER(1,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("C_GP_COD", "G_TI_COD", "G_TA_COD")
);

--------------------------------------------------------
--  Création de la table A_REC_GRP_INT
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_REC_GRP_INT"
(   "C_GI_COD" NUMBER(6,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "G_EA_COD_INS" VARCHAR2(8 CHAR),
    "A_RI_NBR_SOU" NUMBER(5,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("C_GI_COD")
);

--------------------------------------------------------
--  Création de la table A_REC_GRP_INT_PROP
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_REC_GRP_INT_PROP"
(   "C_GI_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0) NOT NULL,
    "C_GP_COD" NUMBER(8,0) NOT NULL,
    "A_RG_RAN_DER" NUMBER(8,0) NOT NULL,
    "A_RG_RAN_DER_INT" NUMBER(8,0),
    "NB_JRS" NUMBER(3,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("C_GI_COD", "G_TA_COD", "G_TI_COD", "C_GP_COD", "NB_JRS")
);

--------------------------------------------------------
--  Création de la table A_SIT_VOE
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_SIT_VOE"
(   "A_SV_COD" NUMBER(3,0) NOT NULL,
    "A_SV_FLG_AFF" NUMBER(1,0) NOT NULL,
    "A_SV_FLG_ATT" NUMBER(1,0) NOT NULL,
	"A_SV_FLG_CLO" NUMBER(1,0) NOT NULL,
    "A_SV_FLG_OUI" NUMBER(1,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("A_SV_COD")
);

--------------------------------------------------------
--  Création de la table A_VOE
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_VOE"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "I_RH_COD" NUMBER(3,0) NOT NULL,
    "A_SV_COD" NUMBER(3,0) NOT NULL,
    "A_VE_ORD" NUMBER(3,0),
    "A_VE_TYP_MAJ" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TA_COD", "I_RH_COD")
);

--------------------------------------------------------
--  Création de la table A_VOE_PROP
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "A_VOE_PROP"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TA_COD" NUMBER(8,0) NOT NULL,
    "I_RH_COD" NUMBER(3,0) NOT NULL,
    "NB_JRS" NUMBER(3,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0),
    "C_GP_COD" NUMBER(8,0),
    "A_VE_RAN_LST_ATT" NUMBER(8,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS")
);

--------------------------------------------------------
--  Création de la table C_CAN_GRP
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "C_CAN_GRP"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "C_GP_COD" NUMBER(6,0) NOT NULL,
    "I_IP_COD" NUMBER(3,0),
    "C_CG_RAN" NUMBER(5,0),
    "C_CG_ORD_APP" NUMBER(6,0),
    "C_CG_ORD_APP_AFF" NUMBER(6,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "C_GP_COD")
);

--------------------------------------------------------
--  Création de la table C_CAN_GRP_INT
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "C_CAN_GRP_INT"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "C_GI_COD" NUMBER(6,0) NOT NULL,
    "I_IP_COD" NUMBER(3,0),
    "C_CI_RAN" NUMBER(5,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "C_GI_COD")
);

--------------------------------------------------------
--  Création de la table C_GRP
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "C_GRP"
(   "C_GP_COD" NUMBER(6,0) NOT NULL,
    "C_GP_FLG_PAS_CLA" NUMBER(1,0),
    "C_JA_COD" NUMBER(6,0),
    "C_GP_ETA_CLA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("C_GP_COD")
);

--------------------------------------------------------
--  Création de la table C_JUR_ADM
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "C_JUR_ADM"
(   "C_JA_COD" NUMBER(6,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("C_JA_COD")
);

--------------------------------------------------------
--  Création de la table G_CAN
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_CAN"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_IC_COD" NUMBER(3,0) NOT NULL,
    "G_CN_FLG_BRS_CER" NUMBER(1,0),
    "G_CN_BRS" NUMBER(1,0),
    "I_CL_COD_BAC" VARCHAR2(8 CHAR),
    "G_CN_FLG_RA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD")
);

--------------------------------------------------------
--  Création de la table G_FIL
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_FIL"
(   "G_FL_COD" NUMBER(6,0) NOT NULL,
    "G_FL_COD_FI" NUMBER(6,0),
    "G_FL_FLG_APP" NUMBER(1,0),
    "G_FL_LIB" VARCHAR2(150 CHAR),
    "G_FL_SIG" VARCHAR2(20 CHAR),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_FL_COD")
);


--------------------------------------------------------
--  Création de la table G_FOR
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_FOR"
(   "G_FR_COD" NUMBER(6,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_FR_COD")
);


-------------------------------------------------------
--  Création de la table G_PAR
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_PAR"
(   "G_PR_COD" NUMBER(3,0) NOT NULL,
    "G_PR_VAL" VARCHAR2(800 BYTE),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_PR_COD")
);


--------------------------------------------------------
--  Création de la table G_TRI_AFF
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_TRI_AFF"
(   "G_TA_COD" NUMBER(8,0) NOT NULL,
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_TA_COD")
);

--------------------------------------------------------
--  Création de la table G_TRI_INS
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "G_TRI_INS"
(   "G_TI_COD" NUMBER(8,0) NOT NULL,
    "G_EA_COD_INS" VARCHAR2(8 CHAR) NOT NULL,
    "G_FR_COD_INS" NUMBER(6,0) NOT NULL,
    "G_FL_COD_INS" NUMBER(6,0) NOT NULL,
    "G_TI_FLG_PAR_EFF" NUMBER(1,0),
    "G_TI_CLA_INT_UNI" NUMBER(3,0),
    "G_TI_ETA_CLA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_TI_COD")
 );


--------------------------------------------------------
--  Création de la table I_INS
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS "I_INS"
(   "G_CN_COD" NUMBER(8,0) NOT NULL,
    "G_TI_COD" NUMBER(8,0) NOT NULL ,
    "I_IS_FLC_SEC" NUMBER(1,0),
    "I_IS_VAL" NUMBER(1,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR),
    PRIMARY KEY("G_CN_COD", "G_TI_COD")
);

--------------------------------------------------------
--  Création de la table J_ORD_APPEL_TMP
--------------------------------------------------------

CREATE LOCAL TEMPORARY TABLE "J_ORD_APPEL_TMP"
(   "C_GP_COD" NUMBER(6,0) NOT NULL,
    "G_CN_COD" NUMBER(8,0) NOT NULL,
    "C_CG_ORD_APP" NUMBER(6,0),
    PRIMARY KEY("C_GP_COD", "G_CN_COD")
);
