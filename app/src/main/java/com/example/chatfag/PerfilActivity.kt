package com.example.chatfag

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.chatfag.databinding.ActivityPerfilBinding
import com.example.chatfag.util.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage


class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate(layoutInflater)
    }
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            binding.imagePerfil.setImageURI(uri)
            uploadImagemStorage(uri)
        } else {
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
    }

    private fun uploadImagemStorage(uri: Uri) {
        val idUsuario = firebaseAuth.currentUser?.uid
        Log.d("PerfilActivity", "ID do Usuário: $idUsuario")
        if (idUsuario != null) {
            Log.d("PerfilActivity", "Iniciando upload da imagem")
            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile(uri)
                .addOnSuccessListener { task ->
                    Log.d("PerfilActivity", "Sucesso ao fazer upload da imagem")
                    exibirMensagem("Sucesso ao fazer upload da imagem")
                }
                .addOnFailureListener {
                    Log.e("PerfilActivity", "Erro ao fazer upload da imagem", it)
                    exibirMensagem("Erro ao fazer upload da imagem")
                }
        } else {
            Log.d("PerfilActivity", "ID do Usuário é nulo")
        }
    }


    private fun inicializarEventosClique() {
        binding.fabSelecionar.setOnClickListener {
            if (temPermissaoGaleria) {
                gerenciadorGaleria.launch("image/*")
            } else {
                exibirMensagem("Não tem permissão para acessar galeria")
                solicitarPermissoes()
            }
        }

        // Adicione este bloco para o clique no botão de atualização do nome
        binding.btnAtualizarNome.setOnClickListener {
            atualizarNomeUsuario()
        }
    }

    private fun atualizarNomeUsuario() {
        val novoNome = binding.editTextNome.text.toString().trim()

        if (novoNome.isNotEmpty()) {
            // Atualize o nome do usuário no Firebase
            val idUsuario = firebaseAuth.currentUser?.uid
            if (idUsuario != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = novoNome
                }

                firebaseAuth.currentUser?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            exibirMensagem("Nome atualizado com sucesso")
                        } else {
                            exibirMensagem("Erro ao atualizar o nome")
                        }
                    }
            }
        } else {
            exibirMensagem("Por favor, digite um nome válido")
        }
    }


    private fun solicitarPermissoes() {

        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        //LISTA DE PERMISSÕES NEGADAS
        val listaPermissoesNegadas = mutableListOf<String>()
        if (!temPermissaoCamera)
            listaPermissoesNegadas.add(Manifest.permission.CAMERA)
        if (!temPermissaoGaleria)
            listaPermissoesNegadas.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (listaPermissoesNegadas.isNotEmpty()) {

            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: temPermissaoGaleria

                if (temPermissaoGaleria) {
                    //Se a permissão foi concedida, exibe a galeria
                    gerenciadorGaleria.launch("image/*")
                } else {
                    //Se a permissão foi negada, exibe uma mensagem de erro
                    exibirMensagem("Não tem permissão para acessar galeria")
                }

            }
            gerenciadorPermissoes.launch(listaPermissoesNegadas.toTypedArray())

        }
    }




    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Editar perfil"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}