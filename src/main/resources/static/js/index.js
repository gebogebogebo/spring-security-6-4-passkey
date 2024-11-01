$(window).on("load", function () {
    $("#createPasskey").on("click", () => createPasskey());
    $("#authenticatePasskey").on("click", () => signInWithPasskey());
});

/*
 * Register
 */
async function createPasskey() {
    const csrfToken = getCsrfToken();

    // get option
    let attResp;
    try {
        const resp = await fetch("/webauthn/register/options", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken
            }
        });

        let optionsJSON = await resp.json();
        let options = {
            optionsJSON: optionsJSON,
            useAutoRegister: false
        }
        attResp = await SimpleWebAuthnBrowser.startRegistration(options);
    } catch (e) {
        if (e.name === "InvalidStateError") {
            $("#statusCreatePasskey").text("Error: Authenticator was probably already registered by user");
        } else {
            $("#statusCreatePasskey").text("Error: " + e);
        }
        return;
    }

    // verify
    try {
        let publicKeyCredential = {
            publicKey: {
                credential: attResp,
                label: "hoge",      // TODO Set a fixed value for label.
            }
        }

        const verificationResp = await fetch("/webauthn/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken
            },
            body: JSON.stringify(publicKeyCredential),
        });

        const verificationJSON = await verificationResp.json();
        if (verificationJSON && verificationJSON.success) {
            $("#statusCreatePasskey").text("Successfully created passkey!");
        } else {
            $("#statusCreatePasskey").text("Failed to create passkey.");
        }
    } catch (e) {
        $("#statusCreatePasskey").text("Error: " + e);
    }
}

/*
 * Authenticate
 */
async function signInWithPasskey() {
    const csrfToken = getCsrfToken();

    // get option
    let asseResp;
    try {
        const resp = await fetch("/webauthn/authenticate/options", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken
            }
        });

        const optionsJSON = await resp.json();
        asseResp = await SimpleWebAuthnBrowser.startAuthentication({ optionsJSON });
    } catch (e) {
        $("#statusSigninWithPasskey").text("Error: " + e);
        return;
    }

    // verify
    try {
        const verificationResp = await fetch("/login/webauthn", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-CSRF-TOKEN": csrfToken,
            },
            body: JSON.stringify(asseResp),
        });

        const statusCode = verificationResp.status;
        if (statusCode !== 200) {
            $("#statusSigninWithPasskey").text("Status Code:" + statusCode);
            return;
        }

        const verificationJSON = await verificationResp.json();
        if (verificationJSON.authenticated) {
            if (verificationJSON.redirectUrl) {
                window.location.href = verificationJSON.redirectUrl;
            }
        } else {
            $("#statusSigninWithPasskey").text("Error: " + verificationJSON.message);
        }
    } catch (e) {
        $("#statusSigninWithPasskey").text("Error: " + e);
    }
}

/*
 * Functions
 */
function getCsrfToken() {
    const csrfInput = document.querySelector('input[name="_csrf"]');
    return csrfInput ? csrfInput.value : null;
}

