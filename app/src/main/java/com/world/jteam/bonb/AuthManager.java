package com.world.jteam.bonb;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
            login(  new ModelUser(account.getDisplayName(), account.getId(), account.getEmail(), 1),
                    true);
         else //По умолчанию
            AppInstance.setUser(getDefaultUser());

    }

    private static ModelUser getDefaultUser(){
        return new ModelUser(AppInstance.getAppContext().getString(R.string.default_user_name), null, -1);
    }

    private static void login(final ModelUser user, final boolean regIfNecessary){
        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<ModelUser> serviceCall = mDataApi.loginUser(user.google_id);
        serviceCall.enqueue(new Callback<ModelUser>() {
            @Override
            public void onResponse(Call<ModelUser> call, Response<ModelUser> response) {
                ModelUser rUser = response.body();
                if(rUser!=null) {
                    AppInstance.setUser(rUser);
                } else if (regIfNecessary) {
                    register(user);
                } else {
                    AppInstance.setUser(getDefaultUser());
                }

            }

            @Override
            public void onFailure(Call<ModelUser> call, Throwable t) {
                if (regIfNecessary)
                    register(user);
                else
                    AppInstance.setUser(getDefaultUser());
            }
        });
    }

    private static void register(final ModelUser user){

        DataApi mDataApi = SingletonRetrofit.getInstance().getDataApi();
        Call<Void> serviceCall = mDataApi.registerUser(user);
        serviceCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                login(user,false);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                AppInstance.setUser(getDefaultUser());
            }
        });

    }
}
