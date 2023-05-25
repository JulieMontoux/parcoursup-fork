--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
-- CREATION DES TABLES
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------



-------------------------------------------------------
--  Création de la table A_ADM
--------------------------------------------------------

CREATE TABLE "A_ADM"
(   "G_CN_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "A_TA_COD" NUMBER(3,0),
    "A_SV_COD" NUMBER(3,0),
    "C_GP_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_ADM
--------------------------------------------------------

ALTER TABLE "A_ADM" ADD CONSTRAINT "PK_A_ADM" PRIMARY KEY ("G_CN_COD", "G_TA_COD", "I_RH_COD");

ALTER TABLE "A_ADM" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM" MODIFY ("I_RH_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM" MODIFY ("A_SV_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM" MODIFY ("A_TA_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_ADM_DEM
--------------------------------------------------------

CREATE TABLE "A_ADM_DEM"
(   "G_CN_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(3,0),
    "C_GP_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(8,0),
    "EST_DEM_PROP" NUMBER(1,0),
    "A_AD_TYP_DEM" NUMBER(3,0),
    "NB_JRS" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_ADM_DEM
--------------------------------------------------------

ALTER TABLE "A_ADM_DEM" ADD PRIMARY KEY ("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS");

ALTER TABLE "A_ADM_DEM" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_DEM" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_DEM" MODIFY ("I_RH_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_DEM" MODIFY ("NB_JRS" NOT NULL ENABLE);
ALTER TABLE "A_ADM_DEM" MODIFY ("EST_DEM_PROP" NOT NULL ENABLE);
ALTER TABLE "A_ADM_DEM" MODIFY ("A_AD_TYP_DEM" NOT NULL ENABLE);

 --------------------------------------------------------
 --  Création de la table A_ADM_PRED_DER_APP
 --------------------------------------------------------

CREATE TABLE "A_ADM_PRED_DER_APP"
(   "G_TA_COD" NUMBER(8,0),
    "C_GP_COD" NUMBER(8,0),
    "A_RG_RAN_DER" NUMBER(8,0),
    "NB_JRS" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_ADM_PRED_DER_APP
--------------------------------------------------------

ALTER TABLE "A_ADM_PRED_DER_APP" ADD PRIMARY KEY ("G_TA_COD", "C_GP_COD", "NB_JRS");

ALTER TABLE "A_ADM_PRED_DER_APP" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PRED_DER_APP" MODIFY ("C_GP_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PRED_DER_APP" MODIFY ("NB_JRS" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PRED_DER_APP" MODIFY ("A_RG_RAN_DER" NOT NULL ENABLE);

 --------------------------------------------------------
 --  Création de la table A_ADM_PROP
 --------------------------------------------------------

CREATE TABLE "A_ADM_PROP"
(   "G_CN_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(1,0),
    "C_GP_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(8,0),
    "A_AM_FLG_MBC" NUMBER(1,0),
    "NB_JRS" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_ADM_PROP
--------------------------------------------------------

ALTER TABLE "A_ADM_PROP" ADD PRIMARY KEY ("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS");

ALTER TABLE "A_ADM_PROP" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PROP" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PROP" MODIFY ("I_RH_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PROP" MODIFY ("NB_JRS" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PROP" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "A_ADM_PROP" MODIFY ("C_GP_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  DDL de la Table A_REC
--------------------------------------------------------

CREATE TABLE "A_REC"
(   "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "A_RC_CAP" NUMBER(5,0),
    "A_RC_CAP_INT_FIL" NUMBER(5,0),
    "A_RC_CAP_INT_GAR" NUMBER(5,0),
    "A_RC_CAP_INF" NUMBER(4,0),
    "A_RC_FLG_TAU_BRS" NUMBER(1,0),
    "A_RC_TAU_BRS_REC" NUMBER(3,0),
    "A_RC_FLG_TAU_NON_RES" NUMBER(1,0),
    "A_RC_FLG_FIN_RES_PLA" NUMBER(1,0),
    "A_RC_TAU_NON_RES_REC" NUMBER(3,0),
    "A_RC_CAP_INT_MIX" NUMBER(5,0),
    "C_JA_COD" NUMBER(6,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_REC
--------------------------------------------------------

ALTER TABLE "A_REC" ADD CONSTRAINT "PK_A_REC" PRIMARY KEY ("G_TI_COD", "G_TA_COD");

ALTER TABLE "A_REC" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC" MODIFY ("G_TA_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_REC_GRP
--------------------------------------------------------

CREATE TABLE "A_REC_GRP"
(   "C_GP_COD" NUMBER(6,0),
    "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "C_JA_COD" NUMBER(6,0),
    "A_RG_PLA" NUMBER(5,0),
    "A_RG_NBR_SOU" NUMBER(5,0),
    "A_RG_RAN_LIM" NUMBER(5,0),
    "A_RG_FLG_ADM_STOP" NUMBER(1,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_REC_GRP
--------------------------------------------------------

ALTER TABLE "A_REC_GRP" ADD CONSTRAINT "PK_A_REC_GRP" PRIMARY KEY ("C_GP_COD", "G_TI_COD", "G_TA_COD");

ALTER TABLE "A_REC_GRP" MODIFY ("C_GP_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP" MODIFY ("C_JA_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_REC_GRP_INT
--------------------------------------------------------

CREATE TABLE "A_REC_GRP_INT"
(   "C_GI_COD" NUMBER(6,0),
    "G_TI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "G_EA_COD_INS" VARCHAR2(8 CHAR),
    "A_RI_NBR_SOU" NUMBER(5,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

-------------------------------------------------------
--  Contraintes sur la table A_REC_GRP_INT
--------------------------------------------------------

ALTER TABLE "A_REC_GRP_INT" ADD CONSTRAINT "PK_A_REC_GRP_INT" PRIMARY KEY ("C_GI_COD");

ALTER TABLE "A_REC_GRP_INT" MODIFY ("C_GI_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_REC_GRP_INT_PROP
--------------------------------------------------------

CREATE TABLE "A_REC_GRP_INT_PROP"
(   "C_GI_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "C_GP_COD" NUMBER(8,0),
    "A_RG_RAN_DER" NUMBER(8,0),
    "A_RG_RAN_DER_INT" NUMBER(8,0),
    "NB_JRS" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_REC_GRP_INT_PROP
--------------------------------------------------------

ALTER TABLE "A_REC_GRP_INT_PROP" ADD CONSTRAINT "PK_A_REC_GRP_INT_PROP" PRIMARY KEY ("C_GI_COD", "G_TA_COD", "G_TI_COD", "C_GP_COD", "NB_JRS");

ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("C_GI_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("C_GP_COD" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("NB_JRS" NOT NULL ENABLE);
ALTER TABLE "A_REC_GRP_INT_PROP" MODIFY ("A_RG_RAN_DER" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_SIT_VOE
--------------------------------------------------------

CREATE TABLE "A_SIT_VOE"
(   "A_SV_COD" NUMBER(3,0),
    "A_SV_FLG_AFF" NUMBER(1,0),
    "A_SV_FLG_ATT" NUMBER(1,0),
	"A_SV_FLG_CLO" NUMBER(1,0),
    "A_SV_FLG_OUI" NUMBER(1,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_SIT_VOE
--------------------------------------------------------

ALTER TABLE "A_SIT_VOE" ADD CONSTRAINT "PK_A_SIT_VOE" PRIMARY KEY ("A_SV_COD");

ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_COD" NOT NULL ENABLE);
ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_FLG_ATT" NOT NULL ENABLE);
ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_FLG_AFF" NOT NULL ENABLE);
ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_FLG_CLO" NOT NULL ENABLE);
ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_FLG_OUI" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_VOE
--------------------------------------------------------

CREATE TABLE "A_VOE"
(   "G_CN_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(3,0),
    "A_SV_COD" NUMBER(3,0),
    "A_VE_ORD" NUMBER(3,0),
    "A_VE_TYP_MAJ" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_VOE
--------------------------------------------------------

ALTER TABLE "A_VOE" ADD CONSTRAINT "PK_A_VOE" PRIMARY KEY ("G_CN_COD", "G_TA_COD", "I_RH_COD");

ALTER TABLE "A_VOE" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE" MODIFY ("I_RH_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE" MODIFY ("A_SV_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table A_VOE_PROP
--------------------------------------------------------

CREATE TABLE "A_VOE_PROP"
(   "G_CN_COD" NUMBER(8,0),
    "G_TA_COD" NUMBER(8,0),
    "I_RH_COD" NUMBER(3,0),
    "NB_JRS" NUMBER(3,0),
    "G_TI_COD" NUMBER(8,0),
    "C_GP_COD" NUMBER(8,0),
    "A_VE_RAN_LST_ATT" NUMBER(8,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table A_VOE_PROP
--------------------------------------------------------

ALTER TABLE "A_VOE_PROP" ADD PRIMARY KEY ("G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS");

ALTER TABLE "A_VOE_PROP" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE_PROP" MODIFY ("G_TA_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE_PROP" MODIFY ("I_RH_COD" NOT NULL ENABLE);
ALTER TABLE "A_VOE_PROP" MODIFY ("NB_JRS" NOT NULL ENABLE);
ALTER TABLE "A_VOE_PROP" MODIFY ("A_VE_RAN_LST_ATT" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table C_CAN_GRP
--------------------------------------------------------

CREATE TABLE "C_CAN_GRP"
(   "G_CN_COD" NUMBER(8,0),
    "C_GP_COD" NUMBER(6,0),
    "I_IP_COD" NUMBER(3,0),
    "C_CG_RAN" NUMBER(5,0),
    "C_CG_ORD_APP" NUMBER(6,0),
    "C_CG_ORD_APP_AFF" NUMBER(6,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table C_CAN_GRP
--------------------------------------------------------

ALTER TABLE "C_CAN_GRP" ADD CONSTRAINT "PK_C_CAN_GRP" PRIMARY KEY ("G_CN_COD", "C_GP_COD");

ALTER TABLE "C_CAN_GRP" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "C_CAN_GRP" MODIFY ("C_GP_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table C_CAN_GRP_INT
--------------------------------------------------------

CREATE TABLE "C_CAN_GRP_INT"
(   "G_CN_COD" NUMBER(8,0),
    "C_GI_COD" NUMBER(6,0),
    "I_IP_COD" NUMBER(3,0),
    "C_CI_RAN" NUMBER(5,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table C_CAN_GRP_INT
--------------------------------------------------------

ALTER TABLE "C_CAN_GRP_INT" ADD CONSTRAINT "PK_C_CAN_GRP_INT" PRIMARY KEY ("G_CN_COD", "C_GI_COD");

ALTER TABLE "C_CAN_GRP_INT" MODIFY ("G_CN_COD" NOT NULL ENABLE);
ALTER TABLE "C_CAN_GRP_INT" MODIFY ("C_GI_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table C_GRP
--------------------------------------------------------

CREATE TABLE "C_GRP"
(   "C_GP_COD" NUMBER(6,0),
    "C_GP_FLG_PAS_CLA" NUMBER(1,0),
    "C_JA_COD" NUMBER(6,0),
    "C_GP_ETA_CLA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table C_GRP
--------------------------------------------------------

ALTER TABLE "C_GRP" ADD CONSTRAINT "PK_C_GRP" PRIMARY KEY ("C_GP_COD");

ALTER TABLE "C_GRP" MODIFY ("C_GP_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table C_JUR_ADM
--------------------------------------------------------

CREATE TABLE "C_JUR_ADM"
(   "C_JA_COD" NUMBER(6,0),
    "G_TI_COD" NUMBER(8,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table C_JUR_ADM
--------------------------------------------------------

 ALTER TABLE "C_JUR_ADM" ADD CONSTRAINT "PK_C_JUR_ADM" PRIMARY KEY ("C_JA_COD");

 ALTER TABLE "C_JUR_ADM" MODIFY ("C_JA_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table G_CAN
--------------------------------------------------------

CREATE TABLE "G_CAN"
(   "G_CN_COD" NUMBER(8,0),
    "G_IC_COD" NUMBER(3,0),
    "G_CN_FLG_BRS_CER" NUMBER(1,0),
    "G_CN_BRS" NUMBER(1,0),
    "I_CL_COD_BAC" VARCHAR2(8 CHAR),
    "G_CN_FLG_RA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table G_CAN
--------------------------------------------------------

ALTER TABLE "G_CAN" ADD CONSTRAINT "PK_G_CAN" PRIMARY KEY ("G_CN_COD");

ALTER TABLE "G_CAN" MODIFY ("G_CN_COD" NOT NULL ENABLE NOVALIDATE);
ALTER TABLE "G_CAN" MODIFY ("G_IC_COD" NOT NULL ENABLE NOVALIDATE);

--------------------------------------------------------
--  Création de la table G_FIL
--------------------------------------------------------

CREATE TABLE "G_FIL"
(   "G_FL_COD" NUMBER(6,0),
    "G_FL_COD_FI" NUMBER(6,0),
    "G_FL_FLG_APP" NUMBER(1,0),
    "G_FL_LIB" VARCHAR2(150 CHAR),
    "G_FL_SIG" VARCHAR2(20 CHAR),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table G_FIL
--------------------------------------------------------

 ALTER TABLE "G_FIL" ADD CONSTRAINT "PK_G_FIL" PRIMARY KEY ("G_FL_COD");

 ALTER TABLE "G_FIL" MODIFY ("G_FL_COD" NOT NULL ENABLE);


--------------------------------------------------------
--  Création de la table G_FOR
--------------------------------------------------------

CREATE TABLE "G_FOR"
(   "G_FR_COD" NUMBER(6,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table G_FOR
--------------------------------------------------------

 ALTER TABLE "G_FOR" ADD CONSTRAINT "PK_G_FOR" PRIMARY KEY ("G_FR_COD");

 ALTER TABLE "G_FOR" MODIFY ("G_FR_COD" NOT NULL ENABLE);


-------------------------------------------------------
--  Création de la table G_PAR
--------------------------------------------------------

CREATE TABLE "G_PAR"
(   "G_PR_COD" NUMBER(3,0),
    "G_PR_VAL" VARCHAR2(800 BYTE),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table G_PAR
--------------------------------------------------------

ALTER TABLE "G_PAR" ADD CONSTRAINT "PK_G_PAR" PRIMARY KEY ("G_PR_COD");

ALTER TABLE "G_PAR" MODIFY ("G_PR_COD" NOT NULL ENABLE);


--------------------------------------------------------
--  Création de la table G_TRI_AFF
--------------------------------------------------------

CREATE TABLE "G_TRI_AFF"
(   "G_TA_COD" NUMBER(8,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table G_TRI_AFF
--------------------------------------------------------

ALTER TABLE "G_TRI_AFF" ADD CONSTRAINT "PK_G_TRI_AFF" PRIMARY KEY ("G_TA_COD");

ALTER TABLE "G_TRI_AFF" MODIFY ("G_TA_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table G_TRI_INS
--------------------------------------------------------

CREATE TABLE "G_TRI_INS"
(   "G_TI_COD" NUMBER(8,0),
    "G_EA_COD_INS" VARCHAR2(8 CHAR),
    "G_FR_COD_INS" NUMBER(6,0),
    "G_FL_COD_INS" NUMBER(6,0),
    "G_TI_FLG_PAR_EFF" NUMBER(1,0),
    "G_TI_CLA_INT_UNI" NUMBER(3,0),
    "G_TI_ETA_CLA" NUMBER(3,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
 );

--------------------------------------------------------
--  Contraintes sur la table G_TRI_INS
--------------------------------------------------------

ALTER TABLE "G_TRI_INS" ADD CONSTRAINT "PK_G_TRI_INS" PRIMARY KEY ("G_TI_COD");

ALTER TABLE "G_TRI_INS" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "G_TRI_INS" MODIFY ("G_FL_COD_INS" NOT NULL ENABLE);
ALTER TABLE "G_TRI_INS" MODIFY ("G_FR_COD_INS" NOT NULL ENABLE);
ALTER TABLE "G_TRI_INS" MODIFY ("G_EA_COD_INS" NOT NULL ENABLE);


--------------------------------------------------------
--  Création de la table I_INS
--------------------------------------------------------

CREATE TABLE "I_INS"
(   "G_CN_COD" NUMBER(8,0),
    "G_TI_COD" NUMBER(8,0),
    "I_IS_FLC_SEC" NUMBER(1,0),
    "I_IS_VAL" NUMBER(1,0),
    "ETIQUETTE1" VARCHAR2(150 CHAR)
);

--------------------------------------------------------
--  Contraintes sur la table I_INS
--------------------------------------------------------

ALTER TABLE "I_INS" ADD CONSTRAINT "PK_I_INS" PRIMARY KEY ("G_CN_COD", "G_TI_COD");

ALTER TABLE "I_INS" MODIFY ("G_TI_COD" NOT NULL ENABLE);
ALTER TABLE "I_INS" MODIFY ("G_CN_COD" NOT NULL ENABLE);

--------------------------------------------------------
--  Création de la table J_ORD_APPEL_TMP
--------------------------------------------------------

CREATE GLOBAL TEMPORARY TABLE "J_ORD_APPEL_TMP"
(   "C_GP_COD" NUMBER(6,0),
    "G_CN_COD" NUMBER(8,0),
    "C_CG_ORD_APP" NUMBER(6,0)
) ON COMMIT DELETE ROWS ;

--------------------------------------------------------
--  Contraintes sur la table J_ORD_APPEL_TMP
--------------------------------------------------------

 ALTER TABLE "J_ORD_APPEL_TMP" ADD PRIMARY KEY ("C_GP_COD", "G_CN_COD") ENABLE;

 ALTER TABLE "J_ORD_APPEL_TMP" MODIFY ("C_GP_COD" NOT NULL ENABLE);
 ALTER TABLE "J_ORD_APPEL_TMP" MODIFY ("G_CN_COD" NOT NULL ENABLE);


--------------------------------------------------------
--  Contraintes d'intégrité référentielle
-- (désactivées ici pour accélérer / faciliter le
-- nettoyage des tables lors des tests unitaires)
--------------------------------------------------------

-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES  C_GRP(C_GP_COD);
-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_A_SV_COD FOREIGN KEY (A_SV_COD) REFERENCES A_SIT_VOE(A_SV_COD);
-- ALTER TABLE "A_ADM" ADD CONSTRAINT FK_A_ADM_C_GI_COD FOREIGN KEY (C_GI_COD) REFERENCES A_REC_GRP_INT(C_GI_COD);

-- ALTER TABLE "A_ADM_DEM" ADD CONSTRAINT FK_A_ADM_DEM_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "A_ADM_DEM" ADD CONSTRAINT FK_A_ADM_DEM_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_ADM_DEM" ADD CONSTRAINT FK_A_ADM_DEM_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_ADM_DEM" ADD CONSTRAINT FK_A_ADM_DEM_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);
-- ALTER TABLE "A_ADM_DEM" ADD CONSTRAINT FK_A_ADM_DEM_C_GI_COD FOREIGN KEY (C_GI_COD) REFERENCES A_REC_GRP_INT(C_GI_COD);

-- ALTER TABLE "A_ADM_PRED_DER_APP" ADD CONSTRAINT FK_A_ADM_PRED_DER_APP_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_ADM_PRED_DER_APP" ADD CONSTRAINT FK_A_ADM_PRED_DER_APP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);

-- ALTER TABLE "A_ADM_PROP" ADD CONSTRAINT FK_A_ADM_PROP_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "A_ADM_PROP" ADD CONSTRAINT FK_A_ADM_PROP_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_ADM_PROP" ADD CONSTRAINT FK_A_ADM_PROP_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_ADM_PROP" ADD CONSTRAINT FK_A_ADM_PROP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);
-- ALTER TABLE "A_ADM_PROP" ADD CONSTRAINT FK_A_ADM_PROP_C_GI_COD FOREIGN KEY (C_GI_COD) REFERENCES A_REC_GRP_INT(C_GI_COD);

-- ALTER TABLE "A_REC" ADD CONSTRAINT FK_A_REC_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_REC" ADD CONSTRAINT FK_A_REC_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_REC" ADD CONSTRAINT FK_A_REC_C_JA_COD FOREIGN KEY (C_JA_COD) REFERENCES C_JUR_ADM(C_JA_COD);

-- ALTER TABLE "A_REC_GRP" ADD CONSTRAINT FK_A_REC_GRP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);
-- ALTER TABLE "A_REC_GRP" ADD CONSTRAINT FK_A_REC_GRP_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_REC_GRP" ADD CONSTRAINT FK_A_REC_GRP_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_REC_GRP" ADD CONSTRAINT FK_A_REC_GRP_C_JA_COD FOREIGN KEY (C_JA_COD) REFERENCES C_JUR_ADM(C_JA_COD);

-- ALTER TABLE "A_REC_GRP_INT" ADD CONSTRAINT FK_A_REC_GRP_INT_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_REC_GRP_INT" ADD CONSTRAINT FK_A_REC_GRP_INT_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);

-- ALTER TABLE "A_REC_GRP_INT_PROP" ADD CONSTRAINT FK_A_REC_GRP_INT_PROP_C_GI_COD FOREIGN KEY (C_GI_COD) REFERENCES A_REC_GRP_INT(C_GI_COD);
-- ALTER TABLE "A_REC_GRP_INT_PROP" ADD CONSTRAINT FK_A_REC_GRP_INT_PROP_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_REC_GRP_INT_PROP" ADD CONSTRAINT FK_A_REC_GRP_INT_PROP_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_REC_GRP_INT_PROP" ADD CONSTRAINT FK_A_REC_GRP_INT_PROP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);

-- ALTER TABLE "A_VOE" ADD CONSTRAINT FK_A_VOE_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "A_VOE" ADD CONSTRAINT FK_A_VOE_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_VOE" ADD CONSTRAINT FK_A_VOE_A_SV_COD FOREIGN KEY (A_SV_COD) REFERENCES A_SIT_VOE(A_SV_COD);

-- ALTER TABLE "A_VOE_PROP" ADD CONSTRAINT FK_A_VOE_PROP_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "A_VOE_PROP" ADD CONSTRAINT FK_A_VOE_PROP_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);
-- ALTER TABLE "A_VOE_PROP" ADD CONSTRAINT FK_A_VOE_PROP_G_TA_COD FOREIGN KEY (G_TA_COD) REFERENCES G_TRI_AFF(G_TA_COD);
-- ALTER TABLE "A_VOE_PROP" ADD CONSTRAINT FK_A_VOE_PROP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES  C_GRP(C_GP_COD);

-- ALTER TABLE "C_CAN_GRP" ADD CONSTRAINT FK_C_CAN_GRP_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "C_CAN_GRP" ADD CONSTRAINT FK_C_CAN_GRP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);

-- ALTER TABLE "C_CAN_GRP_INT" ADD CONSTRAINT FK_C_CAN_GRP_INT_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "C_CAN_GRP_INT" ADD CONSTRAINT FK_C_CAN_GRP_INT_C_GI_COD FOREIGN KEY (C_GI_COD) REFERENCES A_REC_GRP_INT(C_GI_COD);

-- ALTER TABLE "C_GRP" ADD CONSTRAINT FK_C_GRP_C_GP_COD FOREIGN KEY (C_GP_COD) REFERENCES C_GRP(C_GP_COD);
-- ALTER TABLE "C_GRP" ADD CONSTRAINT FK_C_GRP_C_JA_COD FOREIGN KEY (C_JA_COD) REFERENCES C_JUR_ADM(C_JA_COD);

-- ALTER TABLE "C_JUR_ADM" ADD CONSTRAINT FK_C_JUR_ADM_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);

-- ALTER TABLE "G_TRI_INS" ADD CONSTRAINT FK_G_TRI_INS_G_FR_COD FOREIGN KEY (G_FR_COD_INS) REFERENCES G_FOR(G_FR_COD);
-- ALTER TABLE "G_TRI_INS" ADD CONSTRAINT FK_G_TRI_INS_G_FL_COD FOREIGN KEY (G_FL_COD_INS) REFERENCES G_FIL(G_FL_COD);

-- ALTER TABLE "I_INS" ADD CONSTRAINT FK_I_INS_G_CN_COD FOREIGN KEY (G_CN_COD) REFERENCES G_CAN(G_CN_COD);
-- ALTER TABLE "I_INS" ADD CONSTRAINT FK_I_INS_G_TI_COD FOREIGN KEY (G_TI_COD) REFERENCES G_TRI_INS(G_TI_COD);




--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
-- CREATION DES FONCTIONS
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------
--------------------------------------------------------


--------------------------------------------------------
--  Création des fonctions de calcul de dates
--------------------------------------------------------


create or replace FUNCTION f_propGetDateFromParam (o_g_pr_cod IN NUMBER)
RETURN DATE
IS
fmt 					g_par.g_pr_fmt%TYPE;
retour 					g_par.g_pr_val%TYPE;
BEGIN
    --convertit un index de paramètre (g_pr_cod) en date
    --renvoie null si la date est inconnue
	BEGIN
		SELECT g_pr_val , g_pr_fmt
		INTO   retour , fmt
		FROM   g_par
		WHERE  g_pr_cod= o_g_pr_cod;
	EXCEPTION
	WHEN NO_DATA_FOUND THEN RETURN NULL;
	END;
	RETURN  TO_DATE(retour,fmt);
END;
/

create or replace FUNCTION f_propDebAdm
RETURN DATE AS
    dat DATE;
BEGIN
    -- calcule la date de début de la phase d'admission
	dat := f_propGetDateFromParam(35);
    if dat is null then return null; end if;
    return dat;
END f_propDebAdm;
/

create or replace FUNCTION f_propGetNbjrsFromDate
(
  dat IN DATE
) RETURN NUMBER IS
  debAdm DATE;
BEGIN
  -- convertit la date 'dat' en un nombre de jours (nb_jrs) utilisé par l'algorithme des propositions
  -- le résultat est 1 si la date donnée en paramètre correspond à la date de début des admissions (g_pr_cod 35)
  -- renvoie null si la date n'est pas calculable
  debAdm := f_propDebAdm();
  IF dat is null or debAdm is null then return null; end if;
  RETURN 1 + TRUNC(DAT - debAdm);
END;
/

create or replace FUNCTION f_propGetNbJrsFromParam
(
  o_g_pr_cod IN NUMBER
)
RETURN NUMBER IS
    dat DATE;
BEGIN
  --convert a date stored in g_par into a nb_jrs used by the admission algorithm
  --on first day of admissions (g_pr_cod 35), the result shall be 1
  dat := f_propGetDateFromParam(o_g_pr_cod);
  IF dat is null THEN return null; end if;
  RETURN f_propGetNbjrsFromDate(dat);
END f_propGetNbJrsFromParam;
/

create or replace FUNCTION f_propGetNbJrsNow RETURN NUMBER AS
BEGIN
  -- renvoie la date actuelle convertie en nb_jrs utilisé par l'algorithme des admissions
  RETURN f_propGetNbjrsFromDate(SYSDATE);
END f_propGetNbJrsNow;
/


--------------------------------------------------------
--  Création des vues utilisées par l'algo d'admission
--------------------------------------------------------

CREATE OR REPLACE VIEW V_PROP_CAN_RA
AS SELECT  G_CN_COD FROM  G_CAN can WHERE  NVL(can.g_cn_flg_ra,0) = 1;
COMMENT ON TABLE V_PROP_CAN_RA IS 'candidats ayant activé le répondeur automatique';
/

CREATE OR REPLACE VIEW V_PROP_RAN_DER_APP
AS SELECT
    rec.g_ta_cod g_ta_cod,
    rec.g_ti_cod g_ti_cod,
    rec.c_gp_cod c_gp_cod,
    MAX(NVL(C_CG_ORD_APP ,-1)) ran_der_app
 FROM  A_ADM_PROP  adm,  C_CAN_GRP  cla,  A_REC_GRP  rec
 WHERE adm.g_cn_cod=cla.g_cn_cod
 AND adm.c_gp_cod=cla.c_gp_cod
 AND adm.c_gp_cod=rec.c_gp_cod
 AND adm.g_ta_cod=rec.g_ta_cod
 AND adm.g_ti_cod=rec.g_ti_cod
 AND  rec.a_rg_nbr_sou > 0
 AND  cla.i_ip_cod=5
 AND  cla.c_cg_ord_app is not null
 GROUP BY  rec.g_ta_cod,rec.g_ti_cod,rec.c_gp_cod;
/

COMMENT ON TABLE V_PROP_RAN_DER_APP IS 'rang du dernier appelé dans chaque groupe d''affectation';
/


CREATE OR REPLACE VIEW V_PROP_REC_GRP
AS SELECT
c_gp_cod,
rec.g_ti_cod g_ti_cod,
rec.g_ta_cod g_ta_cod,
A_RG_NBR_SOU capacite,
NVL(a_rg_ran_lim,0) a_rg_ran_lim,
NVL(r.a_rc_flg_fin_res_pla,0) a_rc_flg_fin_res_pla
FROM  A_REC_GRP  rec,  A_REC  r
WHERE rec.g_ta_cod=r.g_ta_cod
and NVL(rec.a_rg_flg_adm_stop,0) = 0;
/

COMMENT ON TABLE V_PROP_REC_GRP IS 'groupes, capacités, flag d''arrêt d''admission et de fin de réservation de places';
/

CREATE OR REPLACE VIEW V_PROP_REC_GRP_INT
AS SELECT C_GI_COD, NVL(g_ta_cod,0) g_ta_cod, NVL(g_ti_cod,0) g_ti_cod,A_RI_NBR_SOU  FROM  A_REC_GRP_INT;
/

COMMENT ON TABLE V_PROP_REC_GRP_INT IS 'groupes internats';
/




 CREATE OR REPLACE VIEW V_PROP_VOE
 AS SELECT
 v.g_cn_cod,
 v.g_ta_cod,
 v.i_rh_cod,
 NVL(v.a_ve_ord,0) a_ve_ord,
 ti.g_ti_cod,
 ti.g_ea_cod_ins,
 case when ti.g_ti_cla_int_uni in (0,1) then 1 else 0 end flg_int_cla_prop,
 cg.c_gp_cod,
 cg.c_cg_ord_app,
 cg.c_cg_ord_app_aff,
 NVL(cg.c_cg_ord_app,cg.c_cg_ran) rang,
 sv.a_sv_flg_aff,
 sv.a_sv_flg_oui,
 case when (cg.i_ip_cod=5 AND c.g_ic_cod >= 0 AND ti.g_ti_eta_cla=2) then 1 else 0 end flg_cla,
 case when (sv.a_sv_flg_att=1 OR sv.a_sv_flg_clo=1) then 1 else 0 end a_sv_flg_att_clo,
 case when (sv.a_sv_cod > -40) then 1 else 0 end flg_valid,
 case when (cg.C_CG_ORD_APP is not null) then 1 else 0 end flg_ord_app,
 case when (NVL(v.a_ve_typ_maj,0) in (1,10,11,30,31)) then 1 else 0 end flg_ign_rang_att,
 case when (NVL(v.a_ve_typ_maj,0) in (40,41)) then 1 else 0 end flg_ign_bar_int FROM
 G_CAN  c,
 A_VOE  v,
 A_SIT_VOE  sv,
 A_REC_GRP  rg,
 C_CAN_GRP  cg,
 G_TRI_INS  ti
 WHERE
  cg.C_CG_ORD_APP is not null
 AND  c.g_cn_cod=v.g_cn_cod
 AND  v.a_sv_cod=sv.a_sv_cod
 AND  v.g_cn_cod=cg.g_cn_cod
 AND  cg.c_gp_cod=rg.c_gp_cod
 AND  rg.g_ta_cod=v.g_ta_cod
 AND  rg.g_ti_cod=ti.g_ti_cod;
/

 COMMENT ON TABLE V_PROP_VOE IS
 'Voeux avec classements pédagogiques et info inscription. En prod admission, seuls les  voeux satisfaisants flg_cla=1 AND a_sv_flg_att_clo=1 AND flg_ord_app=1 sont récupérés.';
/

 CREATE OR REPLACE VIEW V_PROP_VOE_INT
 AS SELECT
 v.g_cn_cod,
 v.g_ta_cod,
 v.g_ti_cod,
 v.a_ve_ord,
 v.c_gp_cod,
 v.rang,
 v.c_cg_ord_app,
 v.c_cg_ord_app_aff,
 v.a_sv_flg_aff,
 v.a_sv_flg_oui,
 v.g_ea_cod_ins,
 v.a_sv_flg_att_clo,
 v.flg_cla,
 v.flg_valid,
 v.flg_ord_app,
 v.flg_ign_rang_att,
 v.flg_ign_bar_int,
 cgi.c_gi_cod,
 cgi.c_ci_ran
 FROM
 V_PROP_VOE v,
 A_REC_GRP_INT rgi,
 C_CAN_GRP_INT cgi
 WHERE
 v.i_rh_cod=1
 AND v.flg_int_cla_prop = 1
 AND v.g_cn_cod = cgi.g_cn_cod
 AND cgi.c_gi_cod = rgi.c_gi_cod
 AND  (
           (
           -- internat par établissement identifié par code établissement g_ea_cod_ins
           -- -> un seul groupe de classement
            rgi.g_ta_cod is null
            AND rgi.g_ti_cod is null
            AND rgi.g_ea_cod_ins is not null
            AND rgi.g_ea_cod_ins=v.g_ea_cod_ins
            )
        OR
           (
           -- internat par établissement identifié par code inscription g_ti_cod
           -- -> un seul groupe de classement
           rgi.g_ta_cod is null
           AND rgi.g_ti_cod is not null
           AND rgi.g_ea_cod_ins is null
           AND rgi.g_ti_cod=v.g_ti_cod)
        OR
           (
           -- internat par formation
           rgi.g_ta_cod is not null
           AND rgi.g_ta_cod=v.g_ta_cod
           AND rgi.g_ti_cod=v.g_ti_cod
           )
      );
/

 COMMENT ON TABLE V_PROP_VOE_INT IS
 'Voeux avec internats, avec classement pédagogique et classement internat. En prod admission, seuls les  voeux satisfaisants flg_cla=1 AND a_sv_flg_att_clo=1 AND flg_ord_app=1 sont récupérés.';
/

CREATE OR REPLACE VIEW V_PROP_ADM
AS SELECT rec.g_ta_cod g_ta_cod,
rec.g_ti_cod g_ti_cod,
rec.c_gp_cod c_gp_cod,
adm.nb_jrs nb_jrs,
NVL(C_CG_ORD_APP ,-1) c_cg_ord_app
    FROM  A_ADM_PROP  adm,  C_CAN_GRP  cla,  A_REC_GRP  rec
    WHERE adm.g_cn_cod=cla.g_cn_cod
    AND adm.c_gp_cod=cla.c_gp_cod
    AND adm.c_gp_cod=rec.c_gp_cod
    AND adm.g_ta_cod=rec.g_ta_cod
    AND adm.g_ti_cod=rec.g_ti_cod
    AND  rec.a_rg_nbr_sou > 0
    AND  cla.i_ip_cod=5
    AND  cla.c_cg_ord_app is not null;
/

COMMENT ON TABLE V_PROP_ADM IS 'propositions d''admission générées par l''algorithme d''affectation, incluant  le rang dans l''ordre d''appel et le jour de la proposition';
/

CREATE OR REPLACE VIEW V_PROP_PROP
AS SELECT
            adm.G_CN_COD g_cn_cod,
            adm.G_TI_COD g_ti_cod,
            adm.g_ta_cod g_ta_cod,
            adm.I_RH_COD i_rh_cod,
            adm.C_GP_COD c_gp_cod,
            adm.C_GI_COD c_gi_cod,
            CASE WHEN adm.a_ta_cod in (1,8) THEN 1 ELSE 0 END flg_adm_pp,
            NVL(voe.a_ve_ord,0) a_ve_ord,
            sv.a_sv_flg_oui a_sv_flg_oui,
            sv.a_sv_flg_aff a_sv_flg_aff
        FROM   A_ADM  adm
        LEFT JOIN  A_SIT_VOE  sv
            ON  adm.a_sv_cod=sv.a_sv_cod
        LEFT JOIN  A_VOE  voe
            ON adm.g_cn_cod=voe.g_cn_cod  AND  adm.g_ta_cod=voe.g_ta_cod  AND  adm.i_rh_cod=voe.i_rh_cod,  G_TRI_INS  ti
        WHERE  sv.a_sv_flg_aff=1
        AND  adm.g_ti_cod=ti.g_ti_cod
        AND  adm.a_ta_cod != 2;
/

COMMENT ON TABLE V_PROP_PROP IS 'récupère les propositions hors apprentissage, y compris les propositions refusées';
/

CREATE OR REPLACE VIEW V_PROP_ATT_PROP_ANT AS
SELECT  v.G_CN_COD,v.g_ta_cod,v.I_RH_COD
FROM
A_VOE v,
A_SIT_VOE asv,
A_ADM adm
WHERE
v.a_sv_cod = asv.a_sv_cod
AND asv.a_sv_flg_att=1
AND v.G_CN_COD=adm.G_CN_COD
AND v.g_ta_cod=adm.g_ta_cod
AND adm.A_TA_COD=1;
/

COMMENT ON TABLE V_PROP_ATT_PROP_ANT IS 'voeux en attente dont les candidats ont déjà eu une proposition dans la même formation';
/
