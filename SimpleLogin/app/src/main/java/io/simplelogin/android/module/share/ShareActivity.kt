package io.simplelogin.android.module.share

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.databinding.FragmentAliasCreateBinding
import io.simplelogin.android.module.alias.create.AliasCreateSpinnerAdapter
import io.simplelogin.android.module.alias.create.AliasCreateViewModel
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.toSpannableString
import java.net.URI
import java.net.URISyntaxException

class ShareActivity : BaseAppCompatActivity() {
    private var apiKey: String? = null
    private lateinit var binding: FragmentAliasCreateBinding // Use the same layout with AliasCreateFragment
    private var selectedSuffix: String? = null
    private lateinit var viewModel: AliasCreateViewModel

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentAliasCreateBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { finish() }
        setContentView(binding.root)

        // Temporary hide everything except toolbar
        binding.rootLinearLayout.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        // Enable/disable createButton
        binding.prefixEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.createButton.isEnabled = s?.toString()?.isValidEmailPrefix() ?: false
            }
        })

        apiKey = SLSharedPreferences.getApiKey(this)

        if (apiKey == null) {
            showNotSignedInAlert()
            return
        }

        binding.mailboxesTitleLinearLayout.setOnClickListener { showSelectMailboxesAlert() }
        binding.mailboxesTextView.setOnClickListener { showSelectMailboxesAlert() }

        binding.root.setOnClickListener { dismissKeyboard() }

        binding.createButton.text = "Create & Copy"
        binding.createButton.setOnClickListener {
            if (selectedSuffix == null) {
                toastShortly("No suffix is selected")
                return@setOnClickListener
            }
            create()
        }

        fillPrefix()
        setUpViewModel()
    }

    private fun setUpViewModel() {
        viewModel = AliasCreateViewModel(this)
        setLoading(true)
        viewModel.fetchUserOptionsAndMailboxes()

        viewModel.error.observe(this) { error ->
            if (error != null) {
                toastError(error)
                finish()
            }
        }

        viewModel.userOptions.observe(this) { userOptions ->
            if (userOptions != null) {
                setLoading(false)

                if (userOptions.canCreate) {
                    setUpSuffixesSpinner(userOptions.suffixes.map { it.suffix })
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Can not create more alias")
                        .setMessage("Go premium for unlimited aliases and more.")
                        .setPositiveButton("Close", null)
                        .setOnDismissListener {
                            finish()
                        }
                        .show()
                }
            }
        }

        viewModel.selectedMailboxes.observe(this) { selectedMailboxes ->
            if (selectedMailboxes != null) {
                setLoading(false)
                binding.mailboxesTextView.setText(
                    selectedMailboxes.toSpannableString(this),
                    TextView.BufferType.SPANNABLE
                )
            }
        }
    }

    private fun setUpSuffixesSpinner(suffixes: List<String>) {
        binding.suffixesSpinner.adapter = AliasCreateSpinnerAdapter(this, suffixes)
        binding.suffixesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSuffix = suffixes[position]
                }
            }
    }

    private fun fillPrefix() {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val uri = try { URI(text) } catch (e: URISyntaxException) { null }
        if (uri?.host != null) {
            binding.prefixEditText.setText(uri.host.extractWebsiteName())
        } else {
            // Can not detect domain from text, take the first word from text
            binding.prefixEditText.setText(text?.extractFirstWord())
        }

        // Move cursor to the last character
        binding.prefixEditText.setSelection(binding.prefixEditText.text.count())
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            binding.rootLinearLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.rootLinearLayout.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showNotSignedInAlert() {
        MaterialAlertDialogBuilder(this)
            .setTitle("SimpleLogin sign-in required")
            .setMessage("To create alias through share, you must be signed in.")
            .setNeutralButton("Close") { _, _ ->
                finish()
            }
            .setPositiveButton("Sign me in") { _, _ ->
                val simpleLoginIntent =
                    packageManager.getLaunchIntentForPackage("io.simplelogin.android")
                startActivity(simpleLoginIntent)
                finish()
            }
            .setOnDismissListener { finish() }
            .show()
    }

    private fun showSelectMailboxesAlert() {
        viewModel.selectedMailboxes.value?.let { selectedMailboxes ->
            showSelectMailboxesAlert(
                viewModel.mailboxes,
                selectedMailboxes
            ) { checkedMailboxes ->
                viewModel.setSelectedMailboxes(checkedMailboxes)
            }
        }
    }

    private fun create() {
        val signedSuffix =
            viewModel.userOptions.value!!.suffixes.first { it.suffix == selectedSuffix }.signedSuffix
        setLoading(true)

        SLApiService.createAlias(
            apiKey!!,
            binding.prefixEditText.text.toString(),
            signedSuffix,
            viewModel.selectedMailboxes.value!!.map { it.id },
            binding.nameEditText.text.toString(),
            binding.noteEditText.text.toString()
        ) { result ->
            runOnUiThread {
                setLoading(false)

                result.onSuccess { alias ->
                    val email = alias.email
                    toastLongly("Created & copied \"$email\"")
                    copyToClipboard(email, email)
                    finish()
                }

                result.onFailure(::toastThrowable)

            }
        }
    }
}
