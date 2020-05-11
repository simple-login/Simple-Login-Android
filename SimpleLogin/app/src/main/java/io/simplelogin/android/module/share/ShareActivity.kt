package io.simplelogin.android.module.share

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.databinding.ActivityShareBinding
import io.simplelogin.android.module.alias.create.AliasCreateSpinnerAdapter
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.*
import java.net.URI
import java.net.URISyntaxException

class ShareActivity : BaseAppCompatActivity() {
    lateinit var binding: ActivityShareBinding
    private var selectedSuffix: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShareBinding.inflate(layoutInflater)
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

        val apiKey = SLSharedPreferences.getApiKey(this)

        if (apiKey == null) {
            showNotSignedInAlert()
            return
        }

        binding.createButton.setOnClickListener {
            if (selectedSuffix == null) {
                toastShortly("No suffix is selected")
                return@setOnClickListener
            }

            val prefix = binding.prefixEditText.text.toString()
            val note = binding.noteTextField.editText?.text.toString()
            setLoading(true)
            SLApiService.createAlias(apiKey, prefix, selectedSuffix!!, note) { result ->
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

        setLoading(true)
        SLApiService.fetchUserOptions(apiKey) { result ->
            runOnUiThread {
                setLoading(false)

                result.onSuccess { userOptions ->
                    if (userOptions.canCreate) {
                        setUpSuffixesSpinner(userOptions.suffixes)
                        binding.prefixEditText.requestFocus()
                        showKeyboard()
                        prefillPrefix()
                    } else {
                        toastLongly("You can not create more alias. Please upgrade to premium.")
                        finish()
                    }
                }

                result.onFailure {
                    toastThrowable(it)
                    finish()
                }
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

    private fun prefillPrefix() {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        try {
            val uri = URI(text)
            binding.prefixEditText.setText(uri.host.extractWebsiteName())
        } catch (e: URISyntaxException) {
            // Can not detect domain from text, take the first word from text
            binding.prefixEditText.setText(text.extractFirstWord())
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
}