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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class PerfilActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityPerfilBinding.inflate( layoutInflater )
    }
    private var temPermissaoCamera = false
    private var temPermissaoGaleria = false

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val gerenciadorGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ){ uri ->
        if( uri != null ){
            binding.imagePerfil.setImageURI( uri )
            uploadImagemStorage( uri )
        }else{
            exibirMensagem("Nenhuma imagem selecionada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( binding.root )
        inicializarToolbar()
        solicitarPermissoes()
        inicializarEventosClique()
    }

    override fun onStart() {
        super.onStart()
        recuperarDadosIniciaisUsuarios()
    }

    private fun recuperarDadosIniciaisUsuarios() {
        val idUsuario = firebaseAuth.currentUser?.uid
        if( idUsuario != null ){

            firestore
                .collection("usuario")
                .document( idUsuario )
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val dadosUsuarios = documentSnapshot.data
                    if( dadosUsuarios != null ){

                        val nome = dadosUsuarios["nome"] as String
                        val foto = dadosUsuarios["foto"] as String

                        binding.editTextNome.setText( nome )
                        if( foto.isNotEmpty() ){
                            Picasso.get()
                                .load( foto )
                                .into( binding.imagePerfil )
                        }
                    }
                }
        }
    }

    private fun uploadImagemStorage(uri: Uri) {

        // fotos -> usuarios -> idUsuario -> perfil.jpg
        val idUsuario = firebaseAuth.currentUser?.uid
        if( idUsuario != null ){

            storage
                .getReference("fotos")
                .child("usuarios")
                .child(idUsuario)
                .child("perfil.jpg")
                .putFile( uri )
                .addOnSuccessListener { task ->
                    exibirMensagem("Sucesso ao fazer upload da imagem")
                    task.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val dados = mapOf("foto" to uri.toString())
                        atualizarDadosPerfil(idUsuario, dados)
                    } ?: exibirMensagem("URI da imagem é nulo.")
                }
        }
    }

    private fun atualizarDadosPerfil(idUsuario: String, dados: Map<String, String>) {
        Log.i("TOKEN", "O id é: $idUsuario")
        if (idUsuario != null && dados.isNotEmpty()) {

            firestore
                .collection("usuario")
                .document(idUsuario)
                .update(dados)
                .addOnSuccessListener {
                    exibirMensagem("Sucesso ao atualizar perfil!")
                }
                .addOnFailureListener { e ->
                    Log.i("TOKEN", "O id é: $e")
                    exibirMensagem("Erro ao atualizar perfil: ${e.message}")
                }
        } else {
            exibirMensagem("ID do usuário ou dados estão vazios.")
        }

    }

    private fun inicializarEventosClique() {

        binding.fabSelecionar.setOnClickListener {
            if( temPermissaoGaleria ){
                gerenciadorGaleria.launch("image/*")
            }else{
                exibirMensagem("Não tem permissão para acessar galeria")
                solicitarPermissoes()
            }
        }

        binding.btnAtualizarNome.setOnClickListener {

            val nomeUsuario = binding.editTextNome.text.toString()
            if( nomeUsuario.isNotEmpty() ){

                val idUsuario = firebaseAuth.currentUser?.uid
                if( idUsuario != null ){
                    val dados = mapOf("nome" to nomeUsuario)
                    atualizarDadosPerfil(idUsuario, dados )
                }
            }else{
                exibirMensagem("Preencha o nome para atualizar")
            }
        }
    }

    private fun solicitarPermissoes() {

        //Verifico se usuário já tem permissão
        temPermissaoCamera = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        temPermissaoGaleria = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

        //LISTA DE PERMISSÕES NEGADAS
        val listaPermissoesNegadas = mutableListOf<String>()
        if( !temPermissaoCamera )
            listaPermissoesNegadas.add( Manifest.permission.CAMERA )
        if( !temPermissaoGaleria )
            listaPermissoesNegadas.add( Manifest.permission.READ_MEDIA_IMAGES )

        if( listaPermissoesNegadas.isNotEmpty() ){

            //Solicitar multiplas permissões
            val gerenciadorPermissoes = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()){ permissoes ->

                temPermissaoCamera = permissoes[Manifest.permission.CAMERA]
                    ?: temPermissaoCamera

                temPermissaoGaleria = permissoes[Manifest.permission.READ_MEDIA_IMAGES]
                    ?: temPermissaoGaleria

            }
            gerenciadorPermissoes.launch( listaPermissoesNegadas.toTypedArray() )

        }

    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbarPerfil.tbToolbar
        setSupportActionBar( toolbar )
        supportActionBar?.apply {
            title = "Editar perfil"
            setDisplayHomeAsUpEnabled(true)
        }
    }

}