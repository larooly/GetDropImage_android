package com.example.testclipboardmaybe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.DragAndDropPermissions;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity {

    UpEditText editText;
    ImageView imageView;


    Button copyBut;
    Button pasteBut;
    TextView resultTest;

    LinearLayout totalView;

    String DropPath = "drop_cache";
    String SharePath = "share_cache";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextTextPersonName);
        imageView = findViewById(R.id.imageView);

        copyBut = findViewById(R.id.buttonCopy);
        pasteBut =findViewById(R.id.buttonPaste);

        resultTest = findViewById(R.id.textViewShow);

        totalView = findViewById(R.id.Total);
        Log.d("@@@",getBaseContext().getFilesDir().getPath());


        Intent intent = getIntent();
//        String action = intent.getAction();
//        String type = intent.getType();
//        if(Intent.ACTION_SEND.equals(action) && type != null) {//공유 시작한거 맞아요?
//            ActionShareIntent(intent,action);// 공유를 위해 임시 추가
//        }
        if(intent != null){
            //ActionShareIntent(intent);// 공유를 위해 임시 추가
            Log.d("@@@",intent.getAction());
            if(intent.getAction().equals(Intent.ACTION_SEND)){
                if(intent.getType().startsWith("text")){
                    Log.d("@@@", intent.getStringExtra(Intent.EXTRA_TEXT));

                }else {
                    Uri uri =intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    Log.d("@@@", uri.toString());
                    Log.d("@@@", getFileName(uri));
                }
            }
        }

        copyBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)getSystemService(getBaseContext().CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label",editText.getText());
                clipboard.setPrimaryClip(clip);
            }
        });

        pasteBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(getBaseContext().CLIPBOARD_SERVICE);
                String pasteData = "";
                if (!(clipboard.hasPrimaryClip())) {
                    ;
                }
                else if (!(clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    Toast.makeText(getBaseContext(),"Other Type",Toast.LENGTH_LONG).show();
                }
                else {
                    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                    pasteData = item.getText().toString();
                    resultTest.setText(pasteData);
                }

            }
        });
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                String a = clipboardManager.getPrimaryClip().toString();
                Toast.makeText(getBaseContext(),a,Toast.LENGTH_LONG).show();

            }
        });

        clipboardManager.removePrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Toast.makeText(getBaseContext(),"나도 이젠 몰라 ",Toast.LENGTH_LONG).show();

            }
        });



        imageView.setOnDragListener(new View.OnDragListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                switch (action){
                    case DragEvent.ACTION_DRAG_STARTED:
                        //Toast.makeText(getBaseContext(),"ACTION_DRAG_STARTED",Toast.LENGTH_LONG).show();
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                     //   Toast.makeText(getBaseContext(),"ACTION_DRAG_EXITED",Toast.LENGTH_LONG).show();
                        return true;

                    case DragEvent.ACTION_DRAG_ENTERED:
                      //  Toast.makeText(getBaseContext(),"ACTION_DRAG_ENTERED",Toast.LENGTH_LONG).show();
                        return true;

                    case DragEvent.ACTION_DROP:

                        Toast.makeText(getBaseContext(),"ACTION_DROP",Toast.LENGTH_LONG).show();


                        requestDragAndDropPermissions(event);//이거 없으면 헬파티 여
                        showAlert(event.getClipData());


//                        }
                        return true;
                }
                return false;
            }
        });
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public void ActionShareIntent(Intent intent ){
        String action = intent.getAction();
        String type = intent.getType();
//
        Log.d("share!",action);
        Log.d("share!",intent.getType());
        String ShareFilename = "Sharing";
//        if (intent.getStringExtra(Intent.EXTRA_TEXT) != null) {
//            Log.d("share!", intent.getStringExtra(Intent.EXTRA_TEXT));
//        }
        if(intent.getParcelableExtra(Intent.EXTRA_STREAM) != null){
            Uri shadata = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
//            Log.d("share!",shadata.toString());
//            String exten = shadata.toString().substring(shadata.toString().lastIndexOf("."));
//            Log.d("share!",exten);
            String File =  FileSaveAndPath(shadata,getBaseContext(),"123.jpg");
            File img = new File(File);
            if(img.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
            }
        }
        //여기까지는 장난이고 이밑부터 진짜

        ArrayList<Uri> uriArrayList = new ArrayList<>();


        if(Intent.ACTION_SEND.equals(action) && (type != null)){//단일 공유
            //타입때문에 둘이 따로 짜야 함 (여긴 파일도 있음)


        }else if(Intent.ACTION_SEND_MULTIPLE.equals(action) && (type != null)){
            uriArrayList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if(uriArrayList != null){
                //하던거 하면 될 듯?
            }
        }
    }
    public void SimpleShare(ArrayList<Uri> uriArrayList){

    }
    public void MultiShare(ArrayList<Uri> uriArrayList){
        if(AllimageTypeByClipData(uriArrayList)){
            Toast.makeText(getBaseContext(),"가능 이긴한데 이제 만드러야지 ",Toast.LENGTH_LONG).show();
            //아 파일이름!
            for (int i = 0 ; i <uriArrayList.size() ; i++){
                Uri uri = uriArrayList.get(i);
                String exten = uri.toString().substring(uri.toString().lastIndexOf("."));
                FileSaveAndPath(uri,getBaseContext(),"ShareImage"+String.valueOf(i)+exten);
            }

        }else {
            Toast.makeText(getBaseContext(),"지원하지 않는 파일형식입니다.",Toast.LENGTH_LONG).show();
        }

    }


    public void showAlert(ClipData getData){
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(MainActivity.this);
        alertdialog.setTitle("이미지 전송")
                .setMessage("이미지를 전송하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //DeletePathFiles(getBaseContext(),DropPath);// 이거 주석풀고 돌리는 걸 권장
                       // getData.getItemAt(0);
                        ArrayList<Uri> uriArray = new ArrayList<>();
                        for(int i = 0 ; i < getData.getItemCount() ; i++){
                            uriArray.add(getData.getItemAt(i).getUri());
                        }
                        if(AllimageTypeByClipData(uriArray)){
                            //전송 - 사진 Only
                            ArrayList<String> pathFilesArray = new ArrayList<>();
                            for(int i = 0 ; i < getData.getItemCount() ; i++){
                                Uri uri = uriArray.get(i);
                                FileSaveAndPath(uri,getBaseContext(),"ShareImage"+String.valueOf(i)+setFileType(uri));
                                imageView.setImageURI(uri);
                            }
                            //사진들 저장
                            //이걸로 이제 보내면 됨

                        }else {
                            //취소
                            Toast.makeText(getBaseContext(),"ACTION_DROP",Toast.LENGTH_LONG).show();

                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeletePathFiles(getBaseContext(),DropPath);
                    }
                })
                .show();
    }
    public boolean AllimageTypeByClipData(ArrayList<Uri> arrayList){
        for(int i = 0 ; i < arrayList.size() ; i++){
            Uri uri = arrayList.get(i);
            String mimeT = getContentResolver().getType(uri);
            if(!(mimeT.endsWith("jpeg")||mimeT.endsWith("png")||mimeT.endsWith("bmp")||mimeT.endsWith("jpg")||mimeT.endsWith("gif"))){
                Log.d("report", "failed type");
                return false;
            }//지원하디 않는 파일 타입
        }
        return true;
    }

    public String setFileType(Uri uri){
        String mimetype = getContentResolver().getType(uri);
        if(mimetype.endsWith("jpeg") || mimetype.endsWith("jpg")){
            return ".jpg";
        }else if (mimetype.endsWith("png")){
            return ".png";
        }else if (mimetype.endsWith("bmp")){
            return ".bmp";
        }else if (mimetype.endsWith("gif")){
            return ".gif";
        }else {
            return ".jpg";
        }
    }

    public void SimpleShare( Uri uri){
        //여기는 딱히 검사할 필요 없음 ->어차피 저장하고 보내버릴꺼양
        String mimeType = getContentResolver().getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        String fileName = "share_file"+"."+extension;
        // Define.FileSaveAndPath(uri,getBaseContext(),"",Define.IN_SHARE_FILE_PATH);
    }
    public void DeletePathFiles(Context context , String paths){
        //path 에는 폳더 이름 넣어주길 바람
        String appPath = context.getFilesDir().getPath();
        String FilesPath = appPath +"/"+paths;
        File directory = new File(FilesPath);
        File[] files = directory.listFiles();
        for (int i = 0 ;i<files.length ;i++){
            Log.d("report",files[i].getPath());
            if(files[i].exists()) {//이거 없으면 오류 날수도 있다 주의
                files[i].delete();
            }
        }
    }

    public String FileSaveAndPath(Uri uri, Context context , String Filename){
        String appPath = context.getFilesDir().getPath()+"/"+DropPath;
//        fileName = "ShareImage1.jpg";
        InputStream is = null;
        BufferedOutputStream bos = null;

        File directory = new File(appPath);
        if(!directory.exists()){//없으면 만드러
            directory.mkdir();
        }
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(appPath+"/"+Filename, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return appPath+"/"+Filename;
    }



}




//
//    void getPastAction(){
//        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//        clipboard.getPrimaryClip();
//
//        Log.d("@@@",String.valueOf(clipboard.getPrimaryClip()));
//
//    }


//        editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
//            MenuItem SelectAll, Cut, Copy, Paste;
//
//            @Override
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//
//                Toast.makeText(getBaseContext(),"08646",Toast.LENGTH_LONG).show();
//                SelectAll = menu.findItem(android.R.id.selectAll);
//                Cut = menu.findItem(android.R.id.cut);
//                Copy = menu.findItem(android.R.id.copy);
//                Paste = menu.findItem(android.R.id.paste);
//
//
//
//
//                return false;
//            }
//
//            @Override
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            @Override
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                switch(item.getItemId()) {
//
//                    case android.R.id.paste:
//
//                        Toast.makeText(getBaseContext(),"Hi",Toast.LENGTH_LONG).show();
//
//                        break;
//
//                    case android.R.id.copy:
//
//                        Toast.makeText(getBaseContext(),"H0i",Toast.LENGTH_LONG).show();
//
//                        break;
//
//                    case android.R.id.selectAll:
//
//                        Toast.makeText(getBaseContext(),"H00i",Toast.LENGTH_LONG).show();
//
//                        break;
//                }
//                return false;
//            }
//
//            @Override
//            public void onDestroyActionMode(ActionMode mode) {
//                SelectAll = null;
//                Cut = null;
//                Copy = null;
//                Paste = null;
//            }
//        });
//
//        registerForContextMenu(editText);


// item.getIntent().addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_GRANT_READ_URI_PERMISSION);
//Uri ko = item.getIntent().getData();


//koIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION| Intent.FLAG_GRANT_READ_URI_PERMISSION);

//                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(koIntent, PackageManager.MATCH_DEFAULT_ONLY);
//                        for (ResolveInfo resolveInfo : resInfoList) {
//                            String packageName = resolveInfo.activityInfo.packageName;
//                            grantUriPermission(packageName, ko, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        }

//                        Bundle bundle = getContentResolver().call(ko,"getData()",null,null);
//                        String data = bundle.getString("result");
//                        Log.d("report", data);
//                        try {
////                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),ko);//이걸 null로 만드네
////                            Log.d("report", "gg");
//
//
//
//                                }catch (Exception e){
//                                Log.d("report", "sh...");
//
//                                }

//
//                        Log.d("report",String.valueOf(ko));
////                     resolver.getOutgoingPersistedUriPermissions();
//                        try {
//                            Cursor cursor = getContentResolver().query(ko, null, null, null, null);
//                            cursor.moveToNext();
//                            String path = cursor.getString(cursor.getColumnIndex("_data"));
//                            cursor.close();
//                            Log.d("report", path);
//                        }catch (Exception e){
//
//
//                        }



// savefile(ko,"ko.jpg");
//                        Bitmap bitmap = BitmapFactory.decodeFile(FileSaveAndPath(ko,getBaseContext()));
//                        imageView.setImageBitmap(bitmap);
//        AlertDialog.Builder   alertdialog = new AlertDialog.Builder(MainActivity.this);
//
//        LayoutInflater inflaterr = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View  viewtemplelayout= inflaterr.inflate(R.layout.image_alert, null);
//        ImageView i = (ImageView) viewtemplelayout.findViewById(R.id.alert_image);//and set image to image view
//        i.setImageBitmap(map);
//        alertdialog.setView(viewtemplelayout);//add your view to alert dilaog
//
//        alertdialog.setTitle("이미지 전송").setMessage("이미지를 전송하시겠습니까?");
//        alertdialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        alertdialog.setNeutralButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        alertdialog.show();


//파일 저장 후 보여주기 이거 잠만 빼봐요 괜히 이상한짓 말고
//                        File imgFile = new  File( FileSaveAndPath(ko,getBaseContext()));
//                        if(imgFile.exists()){
//                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                            imageView.setImageBitmap(myBitmap);



/*
*
*     // Log.d("report",String.valueOf(requestDragAndDropPermissions(event)));
                        Log.d("report",String.valueOf(event.getClipData()));
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        Log.d("report", String.valueOf(item));
                        String description = event.getClipDescription().getMimeType(0);
                        //필요한건 다 있는데?
                     //   Log.d("report",description);
                        Uri ko = item.getUri();



                        for(int i = 0 ; i < event.getClipData().getItemCount() ; i++){
                            Uri uri = event.getClipData().getItemAt(i).getUri();
                            String mimeT = getContentResolver().getType(uri);
                            if(!(mimeT.endsWith("jpeg")||mimeT.endsWith("png")||mimeT.endsWith("bmp")||mimeT.endsWith("jpg")||mimeT.endsWith("gif"))){
                                Log.d("report", "failed type");
                                return false;
                            }//지원하디 않는 파일 타입
                        }
                        //타입에서 먼저 거르고

                        ArrayList<String> filepaths= new ArrayList<>() ;
                        for(int i = 0 ; i < event.getClipData().getItemCount() ; i++){
                            //저장을 하고
                            //해당 경로를 불러와야 한다
                            Uri uri = event.getClipData().getItemAt(i).getUri();
                            String filename = "DropImage";
                            String type = setFileType(getContentResolver().getType(uri));//.jpg / .bmp / .png / .gif
                            String savePath = FileSaveAndPath(uri,getBaseContext(),filename+String.valueOf(i)+type);//이러면 저장하고 경로 나옴
                            filepaths.add(savePath);
                        }

                        File img = new File(filepaths.get(filepaths.size()-1));
                        if(img.exists()){
                            Bitmap myBitmap = BitmapFactory.decodeFile(img.getAbsolutePath());
                            imageView.setImageBitmap(myBitmap);

                        }
                        DeletePathFiles(getBaseContext(),DropPath);

//                        ArrayList<String> filepaths= new ArrayList<>() ;
//                        for(int i = 0 ; i < event.getClipData().getItemCount() ; i++){
//                            //저장을 하고
//                            //해당 경로를 불러와야 한다
//                            Uri uri = event.getClipData().getItemAt(0).getUri();
//                            String filename = "DropImage";
//                            String type = setFileType( event.getClipDescription().getMimeType(i));
//                            String savePath = FileSaveAndPath(uri,getBaseContext(),filename+String.valueOf(i)+type);//이러면 저장하고 경로 나옴
//                            filepaths.add(savePath);
//                        }
//                        Log.d("report",String.valueOf(filepaths));


* */