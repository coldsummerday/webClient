package com.example.zhouhaibin.webclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.zhouhaibin.webclient.HttpClient;
import com.example.zhouhaibin.webclient.R;
import com.example.zhouhaibin.webclient.Setting;

public class PostFileActivity extends AppCompatActivity {

    private Button postButton;
    private Context context;
    private HttpClient httpClient;
    private Handler postHandler;
    private Button postPuButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_file);
        postButton = (Button)findViewById(R.id.post_file_private_button);
        postPuButton =(Button)findViewById(R.id.post_file_public_button);
        context = this;
        httpClient = new HttpClient();
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Postintent = new Intent(Intent.ACTION_GET_CONTENT);
                Postintent.setType("*/*"); //选择任意类型
                Postintent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Postintent, 1);
            }
        });
        postPuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Postintent = new Intent(Intent.ACTION_GET_CONTENT);
                Postintent.setType("*/*"); //选择任意类型
                Postintent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Postintent, 2);
            }
        });
        postHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what== Setting.RESPONSEOK){
                    Toast.makeText(context,"上传成功;",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"上传失败;",Toast.LENGTH_SHORT).show();
                }
            }

        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String SelectPath ="";
            Uri uri = data.getData();
            switch (requestCode){
                case 1:
                    httpClient.postFile(uri,context,postHandler,true);
                    break;
                case 2:
                    httpClient.postFile(uri,context,postHandler,false);
                    break;

            }



        }
    }
}
