package com.world.jteam.bonb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.world.jteam.bonb.model.ModelUser;
import com.world.jteam.bonb.server.DataApi;
import com.world.jteam.bonb.server.SingletonRetrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthManager {
    public static void setStartUser(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(AppInstance.getAppContext());
        if (account!=null) //Аккаунт уже был задействован
            loginServer(new ModelUser(account.getDisplayName(), account.getId(), account.getEmail(), 1),
                    true,
                    new OnLoginListener() {
                        @Override
                        public void onLogin() {

                        }

                        @Override
                        public void onFailureLogin() {

                        }
                    });
         else //По умолчанию
            AppInstance.setUser(getDefaultUser());

    }

    private static ModelUser getDefaultUser(){
        return new ModelUser(AppInstance.getAppContext().getString(R.string.default_user_name), null, -1);
    }

    public static void informLoginError(){
        Context context = AppInstance.getAppContext();
        Toast myToast = Toast.makeText(context, context.getString(R.string.auth_error),Toast.LENGTH_SHORT);
        myToast.show();
    }

    public interface OnLoginListener{
        void onLogin();
        void onFailureLogin();
    }

    public static void signIn(Activity activity, int requestCode) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        activity.startActivityForResult(signInIntent, requestCode);
    }

    public static void signInOnResult(int resultCode,Intent data,OnLoginListener loginListener){
        boolean error = false;

        if (resultCode == -1) {
            //RESULT_OK
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                loginServer(new ModelUser(account.getDisplayName(), account.getId(),account.getEmail(),1),
                        true,
                        loginListener);

            } catch (ApiException e) {
                error = true;
            }
        }else{
            error = true;
        }

        if (error){
            loginListener.onFailureLogin();
        }
    }

    private static void loginServer(final ModelUser user, final boolean regIfNecessary,final OnLoginListener loginListener){
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<ModelUser> serviceCall = mDataApi.loginUser(user.google_id);
        serviceCall.enqueue(new Callback<ModelUser>() {
            @Override
            public void onResponse(Call<ModelUser> call, Response<ModelUser> response) {
                ModelUser rUser = response.body();
                if(rUser!=null) {
                    AppInstance.setUser(rUser);
                    loginListener.onLogin();
                } else if (regIfNecessary) {
                    registerServer(user,loginListener);
                } else {
                    AppInstance.setUser(getDefaultUser());
                    loginListener.onFailureLogin();
                }
            }

            @Override
            public void onFailure(Call<ModelUser> call, Throwable t) {
                if (regIfNecessary) {
                    registerServer(user, loginListener);
                }
                else {
                    AppInstance.setUser(getDefaultUser());
                    loginListener.onFailureLogin();
                }
            }
        });
    }

    private static void registerServer(final ModelUser user,final OnLoginListener loginListener){

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Void> serviceCall = mDataApi.registerUser(user);
        serviceCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loginServer(user,false,loginListener);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppInstance.setUser(getDefaultUser());
                loginListener.onFailureLogin();
            }
        });

    }
}
