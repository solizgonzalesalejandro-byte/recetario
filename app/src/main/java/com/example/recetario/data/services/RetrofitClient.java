package com.example.recetario;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.100.251:8083/";
    private static Retrofit retrofit = null;

    private static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static UsuarioApiService getUsuarioApiService() {
        return getClient().create(UsuarioApiService.class);
    }

    // 🌟 NUEVO MÉTODO AÑADIDO: Retorna el punto service
    public static PuntoApiService getPuntoApiService() {
        return getClient().create(PuntoApiService.class);
    }

    // Añade esto dentro de tu clase RetrofitClient existente
    public static ResenasApiService getResenasApiService() {
        return getClient().create(ResenasApiService.class);
    }

}