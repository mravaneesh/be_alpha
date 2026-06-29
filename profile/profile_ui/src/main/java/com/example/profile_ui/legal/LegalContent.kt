package com.example.profile_ui.legal

/**
 * Starting-template legal copy for Apogee. Review/customize before a public launch (ideally with
 * legal counsel). Kept in-app so it works offline and needs no server.
 */
object LegalContent {

    const val EFFECTIVE_DATE = "June 2026"
    const val CONTACT_EMAIL = "avaneeshpandey0830@gmail.com"

    val TERMS: List<Pair<String, String>> = listOf(
        "1. Acceptance" to
            "By creating an account or using Apogee (the \"app\"), you agree to these Terms. If you do not agree, please don't use the app.",
        "2. The service" to
            "Apogee helps you build and track habits, view your progress, and connect with friends for accountability. We may add, change, or remove features over time.",
        "3. Your account" to
            "You sign in with Google. You're responsible for activity on your account and for keeping your sign-in secure. You must be at least 13 years old (or the minimum age in your country) to use Apogee.",
        "4. Acceptable use" to
            "Don't misuse the app: no harassment of other users, no attempts to break, overload, or reverse-engineer the service, and no uploading of unlawful or harmful content (including in your name, username, or habit titles).",
        "5. Your content" to
            "Your habits, profile, and the activity you choose to share remain yours. You control which habits are shared with friends. By sharing, you allow friends you've accepted to see that activity within the app.",
        "6. Not medical advice" to
            "Apogee is a self-improvement tool, not a medical, mental-health, or professional service. Nothing in the app is medical advice. Consult a qualified professional for health decisions.",
        "7. Termination" to
            "You can stop using Apogee any time and delete your account from Settings. We may suspend or terminate accounts that violate these Terms.",
        "8. Disclaimers & liability" to
            "The app is provided \"as is\" without warranties. To the extent permitted by law, we aren't liable for indirect or incidental damages arising from your use of the app.",
        "9. Changes" to
            "We may update these Terms. Continued use after changes means you accept the updated Terms.",
        "10. Contact" to
            "Questions about these Terms? Email $CONTACT_EMAIL.",
    )

    val PRIVACY: List<Pair<String, String>> = listOf(
        "Overview" to
            "This policy explains what Apogee collects, how it's used, and your choices. We aim to collect only what's needed to run the app.",
        "Information we collect" to
            "Account: your name and email from Google Sign-In, and a username you choose. App data: your habits, completions, streaks, and the friends/requests you create. We do not collect payment information.",
        "How we use it" to
            "To provide the app: save and sync your habits, compute streaks and stats, power reminders, and enable the friends and accountability features you opt into.",
        "Sharing with friends" to
            "Only habits you mark as shared count toward what friends see (your daily progress, streak, and weekly totals). You can make any habit private at any time. We do not sell your data or show third-party ads.",
        "Storage & security" to
            "Your data is stored using Google Firebase (Authentication and Firestore). Access is restricted to your account; friend data is visible only to friends you've accepted.",
        "Your choices" to
            "You can edit your profile, change per-habit sharing, sign out, and delete your account (which removes your profile and habit data) from Settings.",
        "Children" to
            "Apogee isn't directed to children under 13, and we don't knowingly collect their data.",
        "Changes" to
            "We may update this policy and will revise the effective date above.",
        "Contact" to
            "Questions about your privacy? Email $CONTACT_EMAIL.",
    )
}
