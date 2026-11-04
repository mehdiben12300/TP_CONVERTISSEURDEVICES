package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

/**
 * Activité principale de l'application Currency Converter.
 * Utilise View Binding et un ViewModel pour assurer la pérennité des données
 * lors des changements d'orientation de l'écran.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var devisesArray: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devisesArray = resources.getStringArray(R.array.devises)

        setupSpinners()
        setupKeypad()
        setupActions()
        observeViewModel()
    }

    /**
     * Initialise et alimente les deux Spinner à partir de R.array.devises
     */
    private fun setupSpinners() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.devises,
            R.layout.spinner_item
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }

        binding.spDeviseSource.adapter = adapter
        binding.spDeviseCible.adapter = adapter

        // Sélection par défaut : Source USD (0), Cible EUR (1)
        if (viewModel.sourceIndex.value == null) viewModel.setSourceIndex(0)
        if (viewModel.cibleIndex.value == null) viewModel.setCibleIndex(1)

        binding.spDeviseSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSourceIndex(position)
                autoConvertIfPossible()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spDeviseCible.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setCibleIndex(position)
                autoConvertIfPossible()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Associe les écouteurs de clics pour le clavier numérique (boutons 0 à 9 et ".")
     */
    private fun setupKeypad() {
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        for (btn in numberButtons) {
            btn.setOnClickListener {
                viewModel.ajouterCaractere(btn.text.toString())
                autoConvertIfPossible()
            }
        }

        binding.btnDot.setOnClickListener {
            viewModel.ajouterCaractere(".")
            autoConvertIfPossible()
        }

        binding.btnC.setOnClickListener {
            viewModel.effacer()
        }
    }

    /**
     * Configure les actions sur les boutons Convertir et Inverser
     */
    private fun setupActions() {
        // Bouton Convertir
        binding.btnConvertir.setOnClickListener {
            val source = devisesArray.getOrNull(binding.spDeviseSource.selectedItemPosition) ?: "USD"
            val cible = devisesArray.getOrNull(binding.spDeviseCible.selectedItemPosition) ?: "EUR"

            val converti = viewModel.effectuerConversion(source, cible)
            if (!converti) {
                Toast.makeText(this, getString(R.string.toast_enter_amount), Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton Inverser
        binding.btnInverser.setOnClickListener {
            viewModel.inverserDevises(devisesArray)
        }
    }

    /**
     * Observe les modifications des données dans le ViewModel
     */
    private fun observeViewModel() {
        viewModel.montant.observe(this) { montant ->
            binding.tvMontant.text = montant
        }

        viewModel.resultat.observe(this) { resultat ->
            binding.tvResultat.text = resultat
        }

        viewModel.sourceIndex.observe(this) { index ->
            if (binding.spDeviseSource.selectedItemPosition != index) {
                binding.spDeviseSource.setSelection(index)
            }
        }

        viewModel.cibleIndex.observe(this) { index ->
            if (binding.spDeviseCible.selectedItemPosition != index) {
                binding.spDeviseCible.setSelection(index)
            }
        }
    }

    /**
     * Conversion automatique lorsqu'un chiffre est tapé ou qu'une devise change (Bonus 1)
     */
    private fun autoConvertIfPossible() {
        val currentMontant = viewModel.montant.value ?: "0"
        val valDouble = currentMontant.toDoubleOrNull() ?: 0.0

        if (valDouble > 0) {
            val source = devisesArray.getOrNull(binding.spDeviseSource.selectedItemPosition) ?: "USD"
            val cible = devisesArray.getOrNull(binding.spDeviseCible.selectedItemPosition) ?: "EUR"
            viewModel.effectuerConversion(source, cible)
        }
    }
}