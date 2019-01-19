package com.world.jteam.bonb.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.world.jteam.bonb.Constants;
import com.world.jteam.bonb.DataApi;
import com.world.jteam.bonb.R;
import com.world.jteam.bonb.SingletonRetrofit;
import com.world.jteam.bonb.model.ModelSearchResult;
import com.world.jteam.bonb.model.ModelUser;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationActivity extends AppCompatActivity {

    //Тупая константа результата интента
    private static final int RC_SIGN_IN = 334;
    //Авторизация
    public GoogleSignInClient mGoogleSignInClient;
    //Настройки приложения
    public SharedPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //Обработчик кнопки авторизации
        SignInButton button = findViewById(R.id.sign_in_button);
        button.setOnClickListener( new View.OnClickListener()
        {
            public void onClick (View v){
                signIn();
            }
        });

        //Подтягивание настроек
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user_displayName = myPreferences.getString("user_displayName","Не авторизованный пользователь");
        //user_id
        checkCacheDir();

        //getStart("1");

        signIn();

    }

    private void checkCacheDir() {

        File tmpParentFolder = new File(Constants.APP_CACHE_PATH);
        tmpParentFolder.mkdirs();

    }

    private void signIn() {

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        Log.v("Errrr", "Начало вызова");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.v("Errrr", "Получили интент");
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }else{
            Toast myToast = Toast.makeText(getApplicationContext(), "Ошибка входа",Toast.LENGTH_SHORT);
            myToast.show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            //Сохранение авторизационных данных
            final ModelUser user = new ModelUser(account.getDisplayName(), account.getId(),account.getEmail(),1);
            //Запись данных на сервере
            DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
            Call<Void> serviceCall = mDataApi.registerUser(user);
            serviceCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    getStart(user.id);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });



        } catch (ApiException e) {
            Log.e("Errrr", "Ошибка входа: "+e.toString());
            Toast myToast = Toast.makeText(getApplicationContext(), "Ошибка входа",Toast.LENGTH_SHORT);
            myToast.show();
        }
    }

    private void getStart(String userID){

        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
        intent.putExtra("userID",userID);
        startActivity(intent);

    }
}