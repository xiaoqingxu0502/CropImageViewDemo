package com.example.hujin.cropimageview;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;

/**
 * @author hujin
 * @package com.example.hujin.cropimageview
 * @description: ${TODO}(用一句话描述该文件做什么)
 * @email xiaoqingxu0502@gamil.com
 * @since 2018/7/22 下午6:27
 */
public class CropActivity extends AppCompatActivity {
    public static final String EXTRA_PATH = "crop_bitmap_path";
    CropImageView mCropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        final File outFile = FileUtil.createImageFile(this,"crop", ".jpg");
        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.tv_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FileUtil.saveOutput(mCropImageView.getCropBitmap(), outFile)){
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_PATH,outFile.getAbsolutePath());
                    setResult(2, intent);
                    finish();
                }
            }
        });
        mCropImageView = findViewById(R.id.crop_bitmap);
        mCropImageView.setBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.timg));
    }


}
