package fr.parcoursup.algos.propositions.algo;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CandidatAffecteInternat {

    /*l'identifiant unique du candidat */
    public final int gCnCod;

    /*l'identifiant unique de l'internat */
    public final @NotNull GroupeInternatUID internatId;


    public CandidatAffecteInternat(
            int gCnCod,
            @NotNull GroupeInternatUID internatId) {
        this.gCnCod = gCnCod;
        this.internatId = internatId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CandidatAffecteInternat that = (CandidatAffecteInternat) obj;
        return this.gCnCod == that.gCnCod && this.internatId.equals(that.internatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gCnCod, internatId.gTiCod, internatId.cGiCod, internatId.gTaCod);
    }

}
