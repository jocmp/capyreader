package com.capyreader.app.ui.accounts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.jocmp.capy.accounts.Source

@Composable
fun ServiceSignup(source: Source) {
    val prompt = buildPrompt(source) ?: return
    val theme = MaterialTheme.colorScheme

    Text(
        buildAnnotatedString {
            append(prompt.text)
            append(" ")
            withLink(
                LinkAnnotation.Url(
                    prompt.link,
                    TextLinkStyles(style = SpanStyle(color = theme.primary))
                )
            ) {
                append(prompt.linkText)
            }
        }
    )
}

@Composable
private fun buildPrompt(source: Source): SignupPrompt? {
    return when (source) {
        Source.FEEDBIN -> SignupPrompt(
            text = stringResource(R.string.add_account_feedbin_signup_prompt),
            linkText = stringResource(R.string.add_account_feedbin_signup_link_text),
            link = "https://feedbin.com/signup"
        )

        Source.FRESHRSS -> SignupPrompt(
            text = stringResource(R.string.add_account_freshrss_signup_prompt),
            linkText = stringResource(R.string.add_account_freshrss_signup_link_text),
            link = "https://freshrss.org/"
        )

        Source.MINIFLUX, Source.MINIFLUX_TOKEN -> SignupPrompt(
            text = stringResource(R.string.add_account_miniflux_signup_prompt),
            linkText = stringResource(R.string.add_account_miniflux_signup_link_text),
            link = "https://miniflux.app/"
        )

        else -> null
    }
}

private data class SignupPrompt(
    val text: String,
    val linkText: String,
    val link: String,
)

@Preview
@Composable
private fun ServiceSignupPreview() {
    ServiceSignup(Source.FEEDBIN)
}
