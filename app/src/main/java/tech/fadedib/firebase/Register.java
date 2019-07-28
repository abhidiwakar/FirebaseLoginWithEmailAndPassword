package tech.fadedib.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    EditText userEmail, userPassword, userContact;
    Button btnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userEmail = findViewById(R.id.userEmailREG);
        userPassword = findViewById(R.id.userPasswordREG);
        userContact = findViewById(R.id.userContact);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUserRegistration();
            }
        });
    }

    private void startUserRegistration() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString().trim();
        final String contact = userContact.getText().toString().trim();
        if (email.isEmpty()){
            createAlert("Error", "Please enter your email!", "OK");
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            createAlert("Error", "Please enter a valid email!", "OK");
        }else if(password.isEmpty()){
            createAlert("Error", "Please enter password!", "OK");
        }else if(password.length() < 6){
            createAlert("Error", "Minimum password length must be 6!", "OK");
        }else if (contact.isEmpty()){
            createAlert("Error", "Please enter a mobile number!", "OK");
        }else if(!Patterns.PHONE.matcher(contact).matches()){
            createAlert("Error", "Please enter a valid mobile number!", "OK");
        }else{
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    progressDialog.setMessage("Saving user data...");
                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put("contact", contact);
                    firebaseFirestore.collection(authResult.getUser().getUid()).add(dataMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            progressDialog.dismiss();
                            Toast.makeText(Register.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            createAlert("Error", "User registered successfully but couldn't save details!", "OK");
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    if (e instanceof FirebaseAuthUserCollisionException){
                        createAlert("Error", "This email address is already registered with us!", "OK");
                    }else{
                        Toast.makeText(Register.this, "Failed to register! Please try again later.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void createAlert(String alertTitle, String alertMessage, String positiveText){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(alertTitle)
                .setMessage(alertMessage)
                .setPositiveButton(positiveText, null)
                .create().show();
    }
}
