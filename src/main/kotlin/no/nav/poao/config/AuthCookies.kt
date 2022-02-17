package no.nav.poao.config

enum class AuthCookies(val cookieName: String) {
    OPEN_AM("ID_token"),
    AZURE_AD("isso-idtoken")
}