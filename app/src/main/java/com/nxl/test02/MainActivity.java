package com.nxl.test02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nxl.test02.HTTPService.LoginService;
import com.nxl.test02.HTTPService.RegisterService;
import com.nxl.test02.meshTools.BleMeshManager;
import com.nxl.test02.meshTools.MeshTools;
import com.nxl.test02.bean.LEDUser;
import com.nxl.test02.tools.ConfigReadUtil;

import java.io.IOException;

import no.nordicsemi.android.mesh.MeshManagerApi;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText pwdEditText;
    private Drawable drawable;
    private Button loginButton;
    private Button registerButton;
    private LEDUser user;
    private Retrofit retrofit;
    private ConfigReadUtil configReadUtil;
    private LoginService loginService;
    private RegisterService registerService;
    private MeshTools meshTools;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化
        super.onCreate(savedInstanceState);
        setTitle("请登录/注册");
        setContentView(R.layout.activity_main);
        user=new LEDUser();

        //设置用户名处的logo
        usernameEditText = findViewById(R.id.userName);
        drawable = getResources().getDrawable(R.drawable.login_user);
        setDrawableStyle(drawable,usernameEditText);

        //设置密码处的logo
        pwdEditText = findViewById(R.id.passWord);
        drawable = getResources().getDrawable(R.drawable.login_password);
        setDrawableStyle(drawable,pwdEditText);
        meshTools  = MeshTools.getInstance();

        //登录监听
        login();

        //注册监听
        register();
    }


    //登录功能
    private void login(){
        loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable usernameText = usernameEditText.getText();
                Editable pwdText = pwdEditText.getText();
                configReadUtil = new ConfigReadUtil();
                String baseUrl = configReadUtil.getProperty(getApplicationContext(),"config.properties","base_url");
                //retrofit初始化
                retrofit = new Retrofit.Builder().baseUrl(baseUrl).build();
                loginService = retrofit.create(LoginService.class);

                if (usernameText.toString().isEmpty()||pwdText.toString().isEmpty()){
                        Toast.makeText(getApplicationContext(),"请输入完整信息",Toast.LENGTH_LONG).show();
                }
                else{
                    user.setUsername(usernameText.toString());
                    user.setPassword(pwdText.toString());
                    Gson gson=new Gson();
                    String route= gson.toJson(user);
                    RequestBody requestBody =
                            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), route);
                    retrofit2.Call<ResponseBody> call = loginService.post(requestBody);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String loginReponse = response.body().string();
                                JsonElement je = new JsonParser().parse(loginReponse);
                                String code =je.getAsJsonObject().get("code").toString();
                                if ("0".equals(code)) {
                                    Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.putExtra("username",user.getUsername());
                                    intent.setClass(MainActivity.this, InterfaceActivity.class);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(getApplicationContext(),"账号或密码错误",Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private void register(){
        registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable usernameText = usernameEditText.getText();
                Editable pwdText = pwdEditText.getText();
                configReadUtil = new ConfigReadUtil();
                String baseUrl = configReadUtil.getProperty(getApplicationContext(),"config.properties","base_url");
                retrofit = new Retrofit.Builder().baseUrl(baseUrl).build();
                registerService = retrofit.create(RegisterService.class);

                if (usernameText.toString().isEmpty()||pwdText.toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"请输入完整信息",Toast.LENGTH_LONG).show();
                }
                else{
                    user.setUsername(usernameText.toString());
                    user.setPassword(pwdText.toString());
                    Gson gson=new Gson();
                    String route= gson.toJson(user);
                    RequestBody requestBody =
                            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), route);
                    retrofit2.Call<ResponseBody> call = registerService.post(requestBody);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                String registerResponse = response.body().string();
                                JsonElement je = new JsonParser().parse(registerResponse);
                                String code =je.getAsJsonObject().get("code").toString();
                                if ("0".equals(code)) {
                                    Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplicationContext(),"注册失败，已有相同账户",Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),"网络错误",Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
        });
    }


    //设置drawable的格式（开源库的图片太大了）
    private void setDrawableStyle(Drawable drawable,EditText editText){
        drawable.setBounds(0, 0, 50, 50);
        editText.setCompoundDrawables(drawable,null,null,null);
    }
}