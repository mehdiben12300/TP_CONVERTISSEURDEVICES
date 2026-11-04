package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Locale

/**
 * ViewModel gérant la logique métier du convertisseur de devises
 * et la persistance des données lors de la rotation d'écran.
 */
class MainViewModel : ViewModel() {

    /**
     * Taux de change fixes par rapport à l'euro (EUR = 1.0)
     */
    val taux: Map<String, Double> = mapOf(
        "EUR" to 1.0,
        "USD" to 1.17,
        "TND" to 3.40,
        "GBP" to 0.87,
        "JPY" to 172.50,
        "CAD" to 1.62
    )

    private val _montant = MutableLiveData("0")
    val montant: LiveData<String> get() = _montant

    private val _resultat = MutableLiveData("")
    val resultat: LiveData<String> get() = _resultat

    private val _sourceIndex = MutableLiveData(0) // USD par défaut (index 0)
    val sourceIndex: LiveData<Int> get() = _sourceIndex

    private val _cibleIndex = MutableLiveData(1) // EUR par défaut (index 1)
    val cibleIndex: LiveData<Int> get() = _cibleIndex

    fun setSourceIndex(index: Int) {
        if (_sourceIndex.value != index) {
            _sourceIndex.value = index
        }
    }

    fun setCibleIndex(index: Int) {
        if (_cibleIndex.value != index) {
            _cibleIndex.value = index
        }
    }

    /**
     * Ajoute un caractère au montant (chiffre ou point décimal).
     * Respecte les contraintes :
     * - Un seul point décimal.
     * - Max 10 caractères.
     * - Remplacement du 0 initial.
     */
    fun ajouterCaractere(c: String) {
        val actuel = _montant.value ?: "0"

        // Limite de 10 caractères
        if (actuel.length >= 10) return

        // Un seul point décimal autorisé
        if (c == "." && actuel.contains(".")) return

        val nouveauMontant = if (actuel == "0" && c != ".") {
            c
        } else {
            actuel + c
        }

        _montant.value = nouveauMontant
    }

    /**
     * Reinitialise le montant à "0" et vide le résultat (Touche C).
     */
    fun effacer() {
        _montant.value = "0"
        _resultat.value = ""
    }

    /**
     * Effectue la conversion en passant par l'euro comme devise pivot.
     * @return true si la conversion s'est faite, false si le montant est égal à 0
     */
    fun effectuerConversion(sourceDevise: String, cibleDevise: String): Boolean {
        val montantStr = _montant.value ?: "0"
        val valMontant = montantStr.toDoubleOrNull() ?: 0.0

        if (valMontant == 0.0) {
            _resultat.value = ""
            return false
        }

        val res = convertir(valMontant, sourceDevise, cibleDevise)
        _resultat.value = String.format(Locale.US, "%.2f", res)
        return true
    }

    /**
     * Méthode de calcul de conversion via l'EUR pivot
     */
    fun convertir(montantVal: Double, source: String, cible: String): Double {
        val rateSource = taux[source] ?: 1.0
        val rateCible = taux[cible] ?: 1.0
        val montantEnEUR = montantVal / rateSource
        return montantEnEUR * rateCible
    }

    /**
     * Échange les sélections des deux Spinner et relance la conversion si un résultat est affiché.
     */
    fun inverserDevises(devisesArray: Array<String>) {
        val srcIdx = _sourceIndex.value ?: 0
        val tgtIdx = _cibleIndex.value ?: 1

        _sourceIndex.value = tgtIdx
        _cibleIndex.value = srcIdx

        // Relancer la conversion si un résultat était déjà calculé/affiché
        if ((_resultat.value ?: "").isNotEmpty()) {
            val sDev = devisesArray.getOrNull(tgtIdx) ?: "USD"
            val cDev = devisesArray.getOrNull(srcIdx) ?: "EUR"
            effectuerConversion(sDev, cDev)
        }
    }
}