package org.fossify.gallery.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.gallery.BuildConfig
import org.fossify.gallery.R
import org.fossify.gallery.databinding.ActivityAboutBinding

class AboutActivity : SimpleActivity() {
    private val binding by viewBinding(ActivityAboutBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupEdgeToEdge(
            padTopSystem = listOf(binding.aboutAppbar),
            padBottomSystem = listOf(binding.aboutNestedScrollview)
        )
        setupMaterialScrollListener(binding.aboutNestedScrollview, binding.aboutAppbar)

        binding.aboutVersion.text = getString(R.string.about_version, BuildConfig.VERSION_NAME)
        binding.aboutEmailValue.text = getString(R.string.about_contact_email)
        binding.aboutSourceHolder.setOnClickListener {
            openUrl(getString(R.string.about_source_url))
        }
        binding.aboutEmailHolder.setOnClickListener {
            sendEmail()
        }
        binding.aboutLicenseHolder.setOnClickListener {
            openUrl(getString(R.string.about_license_url))
        }
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.aboutAppbar, NavigationIcon.Arrow)
        updateTextColors(binding.aboutHolder)
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun sendEmail() {
        val email = getString(R.string.about_contact_email)
        val subject = getString(R.string.about_email_subject, getString(R.string.app_name))
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        startActivity(Intent.createChooser(intent, getString(org.fossify.commons.R.string.send_email)))
    }
}
