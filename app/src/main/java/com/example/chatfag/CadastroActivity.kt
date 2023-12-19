package com.example.chatfag

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatfag.databinding.ActivityCadastroBinding
import com.example.chatfag.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClick()
    }

    private fun inicializarEventosClick() {
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario(nome, email, senha)
            }
        }
    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, senha
        ).addOnCompleteListener { resultado ->
            if (resultado.isSuccessful) {
                Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
                val idUsuario = resultado.result.user?.uid
                if (idUsuario != null) {
                    val usuario = Usuario(
                        idUsuario, nome, email)
                    salvarUsuarioFireStore(usuario)
                }
            }
        }.addOnFailureListener { erro ->
            try {
                throw erro
            } catch (erro: FirebaseAuthInvalidCredentialsException) {
                erro.printStackTrace()
                Toast.makeText(this, "Email inválido, digite outro", Toast.LENGTH_SHORT).show()
            } catch (erroExistente: FirebaseAuthUserCollisionException) {
                erroExistente.printStackTrace()
                Toast.makeText(this, "Este email já existe, digite outro", Toast.LENGTH_SHORT).show()
            } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                erroSenhaFraca.printStackTrace()
                Toast.makeText(this, "Digite uma senha mais forte", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun salvarUsuarioFireStore(usuario: Usuario) {
        firestore
            .collection("usuario")
            .document(usuario.id)
            .set(usuario)
            .addOnSuccessListener {
                Toast.makeText(this, "Cadastro feito com sucesso", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao salvar no Firestore: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun validarCampos(): Boolean {
        nome = binding.textInputLayoutNome.text.toString()
        email = binding.textInputLayoutEmail.text.toString()
        senha = binding.textInputLayoutSenha.text.toString()

        if (nome.isNotEmpty()) {
            binding.textInputLayoutNome.error = null

            if (email.isNotEmpty()) {
                binding.textInputLayoutEmail.error = null

                if (senha.isNotEmpty()) {
                    binding.textInputLayoutSenha.error = null
                    return true
                } else {
                    binding.textInputLayoutSenha.error = "Preencha a senha"
                }
            } else {
                binding.textInputLayoutEmail.error = "Preencha seu email"
            }
        } else {
            binding.textInputLayoutNome.error = "Preencha seu nome"
        }
        return false
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Faça o seu cadastro"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}
