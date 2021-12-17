package com.example.testclipboardmaybe;

import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

public class UpEditText extends EditText {
    private final Context context;


    public UpEditText(Context context) {
        super(context);
        this.context = context;
    }
    public UpEditText(Context context , AttributeSet attrs){
        super(context, attrs);
        this.context =context;
    }
    public UpEditText(Context context , AttributeSet attrs , int defStyle){
        super(context, attrs , defStyle);
        this.context = context;
    }


    @Override
    public boolean onTextContextMenuItem(int id) {//
        switch (id){
            case android.R.id.paste:
                onTextPaste();
                break;
            case android.R.id.copy:
                onTextCopy();
                break;
        }
        return super.onTextContextMenuItem(id);
    }


    public void onTextPaste(){
        Toast.makeText(context,"Hi",Toast.LENGTH_LONG).show();



    }
    public void onTextCopy(){
        Toast.makeText(context,"Hello",Toast.LENGTH_LONG).show();
    }
}
