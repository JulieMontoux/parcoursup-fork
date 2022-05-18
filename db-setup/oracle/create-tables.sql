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
ALTER TABLE "A_SIT_VOE" MODIFY ("A_SV_FLG_Oui" NOT NULL ENABLE);

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
