package com.example.chatfag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatfag.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarEventosClick()
        //firebaseAuth.signOut()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null){
            startActivity(Intent(this, CadastroActivity::class.java))
            finish()
        }
    }

    private fun inicializarEventosClick() {
        binding.textCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            if (validarCampos()) {
                logarUsuario()

            }
        }
    }

    private fun logarUsuario() {
        firebaseAuth.signInWithEmailAndPassword(
            email, senha
        ).addOnSuccessListener {
            Toast.makeText(this, "Logado com sucesso", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }.addOnFailureListener { erro ->
            try {
                throw erro
            } catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException) {
                erroUsuarioInvalido.printStackTrace()
                Toast.makeText(this, "Email Não cadastrado", Toast.LENGTH_SHORT).show()
            } catch (erroCredencialInvalida: FirebaseAuthInvalidCredentialsException) {
                erroCredencialInvalida.printStackTrace()
                Toast.makeText(this, "Email ou senha estão incorretas", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun validarCampos(): Boolean {
        email = binding.textEditEmail.text.toString()
        senha = binding.textEditSenha.text.toString()

        if (email.isNotEmpty()) {
            binding.textInputLayoutLoginEmail.error = null

            if (senha.isNotEmpty()) {
                binding.textInputLayoutSenhaLogin.error = null
                return true
            }else {
                binding.textInputLayoutLoginEmail.error = "Preencha a senha"
            }
        }else {
            binding.textInputLayoutSenhaLogin.error = "Preencha seu email"
        }
        return false
    }
}

