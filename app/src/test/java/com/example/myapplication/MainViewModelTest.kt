package com.example.myapplication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainViewModel
    private val devisesArray = arrayOf("USD", "EUR", "TND", "GBP", "JPY", "CAD")

    @Before
    fun setUp() {
        viewModel = MainViewModel()
    }

    @Test
    fun testInitialState() {
        assertEquals("0", viewModel.montant.value)
        assertEquals("", viewModel.resultat.value)
        assertEquals(0, viewModel.sourceIndex.value)
        assertEquals(1, viewModel.cibleIndex.value)
    }

    @Test
    fun testAjouterCaractere_remplaceZeroInitial() {
        viewModel.ajouterCaractere("1")
        assertEquals("1", viewModel.montant.value)
    }

    @Test
    fun testAjouterCaractere_plusieursChiffres() {
        viewModel.ajouterCaractere("1")
        viewModel.ajouterCaractere("2")
        viewModel.ajouterCaractere("0")
        assertEquals("120", viewModel.montant.value)
    }

    @Test
    fun testAjouterCaractere_unSeulPointDecimal() {
        viewModel.ajouterCaractere("1")
        viewModel.ajouterCaractere(".")
        viewModel.ajouterCaractere("5")
        viewModel.ajouterCaractere(".") // Doit être ignoré
        assertEquals("1.5", viewModel.montant.value)
    }

    @Test
    fun testAjouterCaractere_limiteDixCaracteres() {
        repeat(15) {
            viewModel.ajouterCaractere("9")
        }
        assertEquals("9999999999", viewModel.montant.value)
        assertEquals(10, viewModel.montant.value?.length)
    }

    @Test
    fun testEffacer_reinitialiseMontantEtResultat() {
        viewModel.ajouterCaractere("5")
        viewModel.effectuerConversion("EUR", "USD")
        viewModel.effacer()

        assertEquals("0", viewModel.montant.value)
        assertEquals("", viewModel.resultat.value)
    }

    @Test
    fun testConversion_120_USD_vers_EUR() {
        viewModel.ajouterCaractere("1")
        viewModel.ajouterCaractere("2")
        viewModel.ajouterCaractere("0")

        val ok = viewModel.effectuerConversion("USD", "EUR")
        assertTrue(ok)
        assertEquals("102.56", viewModel.resultat.value)
    }

    @Test
    fun testConversion_100_EUR_vers_TND() {
        viewModel.ajouterCaractere("1")
        viewModel.ajouterCaractere("0")
        viewModel.ajouterCaractere("0")

        val ok = viewModel.effectuerConversion("EUR", "TND")
        assertTrue(ok)
        assertEquals("340.00", viewModel.resultat.value)
    }

    @Test
    fun testConversion_50_GBP_vers_USD() {
        viewModel.ajouterCaractere("5")
        viewModel.ajouterCaractere("0")

        val ok = viewModel.effectuerConversion("GBP", "USD")
        assertTrue(ok)
        assertEquals("67.24", viewModel.resultat.value)
    }

    @Test
    fun testConversion_10_EUR_vers_EUR() {
        viewModel.ajouterCaractere("1")
        viewModel.ajouterCaractere("0")

        val ok = viewModel.effectuerConversion("EUR", "EUR")
        assertTrue(ok)
        assertEquals("10.00", viewModel.resultat.value)
    }

    @Test
    fun testConversion_MontantZero_retourneFalse() {
        val ok = viewModel.effectuerConversion("USD", "EUR")
        assertFalse(ok)
        assertEquals("", viewModel.resultat.value)
    }

    @Test
    fun testInverserDevises() {
        // Initial : USD (0) et EUR (1)
        viewModel.inverserDevises(devisesArray)

        assertEquals(1, viewModel.sourceIndex.value)
        assertEquals(0, viewModel.cibleIndex.value)
    }
}