$(window).on('load', function () {
    $("#createPasskey").on('click', () => createPasskey());
    $("#authenticatePasskey").on('click', () => signInWithPasskey());
    $("#authenticatePasskey2").on('click', () => authenticate());
});

const abortController = new AbortController();
const abortSignal = abortController.signal;

/*
 * Register
 */
function createPasskey() {
    getRegChallenge()
        .then(createCredentialOptions => {
            return createCredential(createCredentialOptions);
        })
        .then(() => {
            $("#statusCreatePasskey").text("Successfully created credential");
        })
        .catch(e => {
            $("#statusCreatePasskey").text("Error: " + e);
        });
}

function getRegChallenge() {
    return rest_post("/webauthn/register/options")
        .then(response => {
            logObject("Get reg challenge response", response);
            let createCredentialOptions = performMakeCredReq(response);
            return Promise.resolve(createCredentialOptions);
        });
}

function performMakeCredReq(makeCredReq) {
    makeCredReq.challenge = base64UrlDecode(makeCredReq.challenge);
    makeCredReq.user.id = base64UrlDecode(makeCredReq.user.id);

    //Base64url decoding of id in excludeCredentials
    if (makeCredReq.excludeCredentials instanceof Array) {
        for (let i of makeCredReq.excludeCredentials) {
            if ('id' in i) {
                i.id = base64UrlDecode(i.id);
            }
        }
    }

    delete makeCredReq.status;
    delete makeCredReq.errorMessage;

    removeEmpty(makeCredReq);

    logObject("Updating credentials ", makeCredReq)
    return makeCredReq;
}

function createCredential(options) {
    if (!PublicKeyCredential || typeof PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable !== "function") {
        return Promise.reject("WebAuthn APIs are not available on this user agent.");
    }

    return navigator.credentials.create({publicKey: options, signal: abortSignal})
        .then(createResponse => {
            let credential = {
                id: base64UrlEncode(createResponse.rawId),
                rawId: base64UrlEncode(createResponse.rawId),
                response: {
                    clientDataJSON: base64UrlEncode(createResponse.response.clientDataJSON),
                    attestationObject: base64UrlEncode(createResponse.response.attestationObject)
                },
                type: createResponse.type,
            };

            if (createResponse.getClientExtensionResults) {
                credential.clientExtensionResults = createResponse.getClientExtensionResults();
            }

            // set transports if it is available
            if (typeof createResponse.response.getTransports === "function") {
                credential.response.transports = createResponse.response.getTransports();
            }

            let publicKeyCredential = {
                publicKey: {
                    credential: credential,
                    label: "hoge",      // TODO
                }
            }

            logObject("=== PublicKeyCredential ===", publicKeyCredential);

            return rest_post("/webauthn/register", publicKeyCredential);
        })
        .catch(function(error) {
            logVariable("create credential error", error);
            if (error === "AbortError") {
                console.info("Aborted by user");
            }
            return Promise.reject(error);
        })
        .then(response => {
            if (response.success) {
                return Promise.resolve(response);
            } else {
                return Promise.reject(response.message);
            }
        });
}

/*
 * Authenticate
 */
function signInWithPasskey() {
    getAuthChallenge()
        .then(getCredentialOptions => {
            return getAssertion(getCredentialOptions);
        })
        .then(redirectUrl => {
            if (redirectUrl) {
                window.location.href = redirectUrl;
            }
        })
        .catch(e => {
            $("#status").text("Error: " + e);
        });
}

function getAuthChallenge() {
    return rest_post("/webauthn/authenticate/options")
        .then(response => {
            logObject("Get auth challenge", response);
            let getCredentialOptions = performGetCredReq(response);
            return Promise.resolve(getCredentialOptions);
        });
}

let performGetCredReq = (getCredReq) => {
    getCredReq.challenge = base64UrlDecode(getCredReq.challenge);

    //Base64url decoding of id in allowCredentials
    if (getCredReq.allowCredentials instanceof Array) {
        for (let i of getCredReq.allowCredentials) {
            if ('id' in i) {
                i.id = base64UrlDecode(i.id);
            }
        }
    }

    delete getCredReq.status;
    delete getCredReq.errorMessage;

    removeEmpty(getCredReq);

    logObject("Updating credentials ", getCredReq)
    return getCredReq;
}

function getAssertion(options) {
    if (!PublicKeyCredential) {
        return Promise.reject("WebAuthn APIs are not available on this user agent.");
    }

    let publicKeyCredentialRequestOptions = {
        publicKey: options,
        signal: abortSignal,
    };

    return navigator.credentials.get(publicKeyCredentialRequestOptions)
        .then(getResponse => {
            let publicKeyCredential = {
                id: base64UrlEncode(getResponse.rawId),
                rawId: base64UrlEncode(getResponse.rawId),
                response: {
                    clientDataJSON: base64UrlEncode(getResponse.response.clientDataJSON),
                    userHandle: base64UrlEncode(getResponse.response.userHandle),
                    signature: base64UrlEncode(getResponse.response.signature),
                    authenticatorData: base64UrlEncode(getResponse.response.authenticatorData)
                },
                type: getResponse.type,
            };

            if (getResponse.getClientExtensionResults) {
                publicKeyCredential.clientExtensionResults = getResponse.getClientExtensionResults();
            }

            logObject("=== PublicKeyCredential ===", publicKeyCredential);

            return rest_post("/login/webauthn", publicKeyCredential);
            // return Promise.resolve(publicKeyCredential);
        })
        .catch(function(error) {
            logVariable("get assertion error", error);
            if (error === "AbortError") {
                console.info("Aborted by user");
            }
            return Promise.reject(error);
        })
        .then(response => {
            if (response.authenticated) {
                return Promise.resolve(response.redirectUrl);
            } else {
                return Promise.reject(response.message);
            }
        });
}

/*
 * functions
 */
function getCsrfToken() {
    const csrfInput = document.querySelector('input[name="_csrf"]');
    return csrfInput ? csrfInput.value : null;
}

function rest_post(endpoint, object) {
    const csrfToken = getCsrfToken();

    return fetch(endpoint, {
            method: "POST",
            credentials: "same-origin",
            body: JSON.stringify(object),
            headers: {
                "content-type": "application/json",
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(response => {
            return response.json();
        });
}

function logObject(name, object) {
    console.log(name + ": " + JSON.stringify(object));
}

function logVariable(name, text) {
    console.log(name + ": " + text);
}

function removeEmpty(obj) {
    for (let key in obj) {
        if (obj[key] == null || obj[key] === "") {
            delete obj[key];
        } else if (typeof obj[key] === 'object') {
            removeEmpty(obj[key]);
        }
    }
}

function base64UrlDecode(base64url) {
    let input = base64url
        .replace(/-/g, "+")
        .replace(/_/g, "/");
    let diff = input.length % 4;
    if (!diff) {
        while(diff) {
            input += '=';
            diff--;
        }
    }

    return Uint8Array.from(atob(input), c => c.charCodeAt(0));
}

function base64UrlEncode(arrayBuffer) {
    if (!arrayBuffer || arrayBuffer.length === 0) {
        return undefined;
    }

    return btoa(String.fromCharCode.apply(null, new Uint8Array(arrayBuffer)))
        .replace(/=/g, "")
        .replace(/\+/g, "-")
        .replace(/\//g, "_");
}

/*
 * Register
 */
async function register() {
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
    let publicKeyCredential = {
        publicKey: {
            credential: attResp,
            label: "hoge",      // TODO Set a fixed value for label.
        }
    }

    const verificationResp = await fetch('/webauthn/register', {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(publicKeyCredential),
    });

    try {
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
async function authenticate() {
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