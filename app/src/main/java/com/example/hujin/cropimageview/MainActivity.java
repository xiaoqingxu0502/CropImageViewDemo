package com.example.hujin.cropimageview;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import static com.example.hujin.cropimageview.CropActivity.EXTRA_PATH;

public class MainActivity extends AppCompatActivity {

    ImageView mImageAdd;
    ImageView mImageShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageAdd = findViewById(R.id.iv_add);
        mImageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this,CropActivity.class),1);
            }
        });
        mImageShow = findViewById(R.id.iv_show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null && requestCode == 1){
            String path = data.getStringExtra(EXTRA_PATH);
            if(!TextUtils.isEmpty(path)){
                mImageShow.setImageBitmap(BitmapFactory.decodeFile(path));
            }
        }
    }
}
