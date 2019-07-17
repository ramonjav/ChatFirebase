package firebase.app.pruebas2.Actividades;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import firebase.app.pruebas2.Entidades.firebase.logica.Mensaje;
import firebase.app.pruebas2.Entidades.firebase.logica.User;
import firebase.app.pruebas2.Entidades.logica.LMensaje;
import firebase.app.pruebas2.Entidades.logica.LUser;
import firebase.app.pruebas2.Utilidades.Constantes;
import firebase.app.pruebas2.adapters.AdapterMensaje;
import firebase.app.pruebas2.R;
import firebase.app.pruebas2.persistencia.MensajeDAO;
import firebase.app.pruebas2.persistencia.UserDAO;

import static firebase.app.pruebas2.Notificacions.notification.sendNotification;
import static firebase.app.pruebas2.Utilidades.Constantes.UBIC;
import static firebase.app.pruebas2.Utilidades.Constantes.UBICA;

public class MensajeActivity extends AppCompatActivity implements ChildEventListener, ValueEventListener {

   FirebaseAuth mAuth = FirebaseAuth.getInstance();
   FirebaseDatabase database = FirebaseDatabase.getInstance();

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Button btnEnviar, btnaudios;
    private ImageButton btnFoto;
    private EditText txtMensaje;
    private RecyclerView rvMensaje;
    private AdapterMensaje adapter;

    private String Key_receptor;
    String token;
    User user;
   static String fileName;

    final static int RESULTADO_GALERIA = 2;
    //TODO: tomar de camara: final static int RESULTADO_CAMARA = 4;
    private MediaRecorder mediaRecorder;
    private static String fichero = Environment.getExternalStorageDirectory().getAbsolutePath()+UBICA;
   // private static String fileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje);

       Bundle bundle = getIntent().getExtras();

        permisos();
        storage = FirebaseStorage.getInstance();

        final File file = new File(fichero);

        if(!file.exists()){
            file.mkdirs();
        }

      if(bundle!=null){
            Key_receptor = bundle.getString(Constantes.KEY);
            token = bundle.getString(Constantes.TOKEN);
        }else{
            finish();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference reference = database.getReference("Usuarios/" + currentUser.getUid());
            reference.addListenerForSingleValueEvent(this);

        }

        btnEnviar = findViewById(R.id.btnEnviar);
        txtMensaje = findViewById(R.id.txtMensaje);
        rvMensaje = findViewById(R.id.rvMensajes);
        btnFoto = findViewById(R.id.btn_foto);
        btnaudios = findViewById(R.id.btn_audios);

        adapter = new AdapterMensaje(this);
        LinearLayoutManager l = new LinearLayoutManager(this);
        rvMensaje.setLayoutManager(l);
        rvMensaje.setAdapter(adapter);

        comprobarEditView(txtMensaje, btnEnviar);
        Buttonenviar();

      txtMensaje.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

              if(!txtMensaje.getText().toString().trim().isEmpty()){
                  btnEnviar.setVisibility(View.VISIBLE);
                  btnaudios.setVisibility(View.GONE);
              }else{
                  btnEnviar.setVisibility(View.GONE);
                  btnaudios.setVisibility(View.VISIBLE);
              }
          }

          @Override
          public void afterTextChanged(Editable s) {

          }
      });

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MensajeActivity.this,
                        "    ／|＿＿＿＿＿＿＿＿＿ _ _\n" +
                        "〈　 To BE CONTINUED…//// |\n" +
                        "　＼|￣", Toast.LENGTH_SHORT).show();
                /*Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"Selecciona una foto"), RESULTADO_GALERIA);*/

            }
        });

        adapterRegister(adapter);
        FirebaseDatabase.getInstance()
                .getReference(Constantes.NODO_MENSAJES)
                .child(UserDAO.getInstancia().getkeyUser())
                .child(Key_receptor).addChildEventListener(this);

        btnaudios.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    btnaudios.setText("Grabando");
                    startRecording();

                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    btnaudios.setText("Grabar");
                    stopRecording();
                    subirAudio();
                }
                return false;
            }
        });
    }

    private void Buttonenviar() {
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String men = txtMensaje.getText().toString();

                    Mensaje mensaje = new Mensaje();
                    LMensaje lMensaje = new LMensaje(mensaje.getKeyEmisor(), mensaje);
                    lMensaje.setMessaje(mensaje);
                    mensaje.setMensaje(men);
                    mensaje.setConFoto(false);
                    mensaje.setUrlFoto("");
                    mensaje.setNameFoto("");
                    mensaje.setAudio(false);
                    mensaje.setUrlAudio("");
                    mensaje.setNameAudio("");
                    mensaje.setKeyEmisor(UserDAO.getInstancia().getkeyUser());
                    MensajeDAO.getInstancia().NewMenssage(UserDAO.getInstancia().getkeyUser(), Key_receptor,mensaje);
                    Toast.makeText(MensajeActivity.this, user.getNombre(), Toast.LENGTH_SHORT).show();
                   sendNotification(MensajeActivity.this, txtMensaje.getText().toString(), user.getNombre(), UserDAO.getInstancia().getkeyUser(), token);
                    txtMensaje.setText("");
            }
        });
    }

    private void setScrollbar(){

        rvMensaje.scrollToPosition(adapter.getItemCount()-1);

    }

    public void comprobarEditView(final EditText editText, final Button button){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                signos();
                if(editText.getText().toString().trim().length() > 0){
                    button.setVisibility(View.VISIBLE);
                }else{
                    button.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void adapterRegister(AdapterMensaje adapterMensaje){

        adapterMensaje.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
      final Map<String, LUser> mapUT = new HashMap<>();


            final Mensaje m = dataSnapshot.getValue(Mensaje.class);
            final LMensaje lmensaje = new LMensaje(dataSnapshot.getKey(), m);
            final int position =  adapter.addmensaje(lmensaje);

            if(mapUT.get(m.getKeyEmisor()) != null){
                lmensaje.setLuser(mapUT.get(m.getKeyEmisor()));
                adapter.actualizarmensaje(position, lmensaje);

            }else{
                UserDAO.getInstancia().OptenerUser(m.getKeyEmisor(), new UserDAO.IdevolverLuser() {
                    @Override
                    public void devolver(LUser lUser) {
                        mapUT.put(m.getKeyEmisor(), lUser);
                        lmensaje.setLuser(lUser);
                        adapter.actualizarmensaje(position, lmensaje);
                    }

                    @Override
                    public void devolverErro(String error) {

                        Toast.makeText(MensajeActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        user = dataSnapshot.getValue(User.class);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try{
            if(requestCode == RESULTADO_GALERIA){
                if(resultCode == Activity.RESULT_OK){
                    ponerFoto(data.getDataString());
                    Log.d("Ruta imagen", data.getDataString());
                }else{
                    Toast.makeText(MensajeActivity.this,
                            "La foto no se ha cargado",
                            Toast.LENGTH_SHORT).show();
                }

            }/*else if(requestCode == RESULTADO_CAMARA){

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                saveImage(imageBitmap);
            }*/

        }catch (Exception e){

        }
    }

    public void ponerFoto(String uri){
        if(uri != null && !uri.isEmpty() && !uri.equals("null")){
            Intent i = new Intent(MensajeActivity.this, FotoActivity.class);
            i.putExtra(Constantes.URI, uri);
            i.putExtra(Constantes.KEY, Key_receptor);
            startActivity(i);
            /*try {
                saveImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    public  void signos(){

        if(txtMensaje.getText().toString().trim().contains("?")){
        }else if(txtMensaje.getText().toString().trim().contains("¿")) {
            poner_signos("?");
         }

        if(txtMensaje.getText().toString().trim().contains("!")){
        }else if(txtMensaje.getText().toString().trim().contains("¡")){
            poner_signos("!");
        }

        /*if(txtMensaje.getText().toString().trim().contains(")")){
        }else if(txtMensaje.getText().toString().trim().contains("(")){
            poner_signos(")");
        }

        if(txtMensaje.getText().toString().trim().contains("]")){
        }else if(txtMensaje.getText().toString().trim().contains("[")){
            poner_signos("]");
        }

        if(txtMensaje.getText().toString().trim().contains("}")){
        }else if(txtMensaje.getText().toString().trim().contains("{")){
            poner_signos("}");
        }*/
    }

    public void poner_signos(String signo){
            int posicion = txtMensaje.getSelectionEnd();
            txtMensaje.setText(txtMensaje.getText().toString() + signo);
            txtMensaje.setSelection(posicion);
    }

    private void startRecording() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fileName = "audio:" + timeStamp + ".3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOutputFile(fichero+fileName);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        }catch(IOException e){
            Log.e("Algo", "Fallo grabando");
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


    public void subirAudio(){

        storageReference = storage.getReference("audios_chat");//imagenes_cha
        final StorageReference filepath = storageReference.child(fileName);
        Uri uri = Uri.fromFile(new File(fichero+fileName));
        filepath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String url = task.getResult().toString();
                EnviarMensaje(url, fileName);
            }
        });

    }

    public void EnviarMensaje(String url, String name){
        if(!url.isEmpty()){
            Mensaje mensaje = new Mensaje();
            mensaje.setMensaje("Audio");
            mensaje.setUrlFoto("");
            mensaje.setConFoto(false);
            mensaje.setNameFoto("");
            mensaje.setAudio(true);
            mensaje.setUrlAudio(url);
            mensaje.setNameAudio(name);
            mensaje.setKeyEmisor(UserDAO.getInstancia().getkeyUser());
            MensajeDAO.getInstancia().NewMenssage(UserDAO.getInstancia().getkeyUser(),Key_receptor,mensaje);

            txtMensaje.setText("");
        }
    }

    //pedir permisos para escribir y grabar audio
    public void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MensajeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1000);
        }
    }

}
