package com.example.demo.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class LoginController {
    @GetMapping("/")
    fun root(): String {
        return "redirect:mypage"
    }

    @GetMapping("login")
    fun login(
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "logout", required = false) logout: String?,
        model: Model,
        session: HttpSession,
    ): String {
        model.addAttribute("showErrorMsg", false)
        model.addAttribute("showLogoutedMsg", false)

        if (error != null) {
            val ex = session.getAttribute(AUTHENTICATION_EXCEPTION) as AuthenticationException?
            if (ex != null) {
                model.addAttribute("showErrorMsg", true)
                model.addAttribute("errorMsg", ex.message)
            }
        } else if (logout != null) {
            model.addAttribute("showLogoutedMsg", true)
            model.addAttribute("logoutedMsg", "Logouted")
        }

        return "login"
    }

    @GetMapping("mypage")
    fun mypage(
        request: HttpServletRequest,
        model: Model,
    ): String {
        val userName = getUserName(request)
        model.addAttribute("userName", userName)
        return "mypage"
    }

    private fun getUserName(request: HttpServletRequest): String {
        val session = request.session
        val securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT") as SecurityContext
        val authentication = securityContext.authentication

        val userName = when (val principal = authentication.principal) {
            is User -> principal.username
            is ImmutablePublicKeyCredentialUserEntity -> principal.name
            else -> ""
        }

        return userName
    }
}
