package com.taximechanic;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.taximechanic.databinding.ActivityWorkBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WorkActivity extends BaseActivity {

    private ActivityWorkBinding bind;

    private File mDirectory;
    private String mPhotoName = "";
    private String mPhotoFront = "";
    private String mPhotoBack = "";
    private String mPhotoRight = "";
    private String mPhotoLeft = "";
    private EditText etTicket;
    private EditText etComments;

    public enum F_PHOTO {
        F_FRONT(1),
        F_BACK(2),
        F_RIGHT(3),
        F_LEFT(4);

        public final int mSide;
        F_PHOTO(int i) {
            this.mSide = i;
        }

    };

    public static ArrayList<Question> mQuestions = new ArrayList();
    QuestionsAdapter mQuestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityWorkBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        createDirectory();
        mQuestionsAdapter = new QuestionsAdapter();
        RecyclerView rv = findViewById(R.id.rvQuestions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mQuestionsAdapter);
        findViewById(R.id.ivFront).setOnClickListener(this);
        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.ivRight).setOnClickListener(this);
        findViewById(R.id.ivLeft).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.imgProfile).setOnClickListener(this);
        etTicket = findViewById(R.id.etTicket);
        etComments = findViewById(R.id.etComments);

        WebQuery wq = new WebQuery(WebQuery.mHostUrlMechanicQuestions, WebQuery.HttpMethod.GET, WebResponse.getQuestions, this);
        wq.setHeader("Authorization", "Bearer " + Config.mBearerKey);
        wq.request();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.ivBack:
                takePhoto(F_PHOTO.F_BACK);
                break;
            case R.id.ivFront:
                takePhoto(F_PHOTO.F_FRONT);
                break;
            case R.id.ivRight:
                takePhoto(F_PHOTO.F_RIGHT);
                break;
            case R.id.ivLeft:
                takePhoto(F_PHOTO.F_LEFT);
                break;
            case R.id.btnSave:
                saveResult();
                break;
            case R.id.imgProfile:
                showProfile();
                break;
        }
    }

    public void takePhoto(F_PHOTO f) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoURI = null;
            File photoFile = new File(mDirectory.getPath() + "/p" + System.currentTimeMillis() + ".jpg");
            mPhotoName = photoFile.getAbsolutePath();
            photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".MechanicFileProvider",  photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, f.mSide);
        }
    }

    public void showProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void saveResult() {
        if (etTicket.getText().toString().isEmpty()) {
            alertDialog(R.string.Error, R.string.TicketIsEmpty);
            return;
        }
        for (int i = 0; i < mQuestions.size(); i++) {
            Question q = mQuestions.get(i);
            if (q.mYes == false &&  q.mNo == false) {
                alertDialog(R.string.Error, R.string.NotAllChecked);
                return;
            }
        }
        if (mPhotoFront.isEmpty() || mPhotoBack.isEmpty() || mPhotoRight.isEmpty() || mPhotoLeft.isEmpty()) {
            alertDialog(R.string.Error, R.string.NotAllPhotos);
            return;
        }
        if (!createProgressDialog(R.string.Empty, R.string.Saving)) {
            return;
        }
        WebQuery wq = new WebQuery(WebQuery.mHostUrlMechanicReport, WebQuery.HttpMethod.POST, WebResponse.getmResponseMechanicSaveReport, this);
        wq.setHeader("Authorization", "Bearer " + Config.mBearerKey);
        wq.setParameter("waybill_number", etTicket.getText().toString());
        wq.setParameter("data[speedometer]", bind.etSpeedometer.getText().toString());
        wq.setParameter("data[comment]", etComments.getText().toString());
        for (int i = 0; i < mQuestions.size(); i++) {
//            Question q = mQuestions.get(i);
//            wq.setParameter(q.mFieldName, q.mYes ? "1" : "0");
//            if (!q.mComment.isEmpty()) {
//                wq.setParameter(q.mFieldName + "_comment", q.mComment);
//            }
            Question q = mQuestions.get(i);
            wq.setParameter(String.format("question[%s][verify]",q.mId), q.mYes ? "1" : "0");
            wq.setParameter(String.format("question[%s][comment]", q.mId), q.mComment);
        }
        wq.setFile("images[]", mPhotoBack);
        wq.setFile("images[]", mPhotoFront);
        wq.setFile("images[]", mPhotoLeft);
        wq.setFile("images[]", mPhotoRight);
        wq.request();
    }

    public void clearReport() {
        etTicket.setText("");
        for (int i = 0; i < mQuestions.size(); i++) {
            Question q = mQuestions.get(i);
            q.mComment = "";
            q.mNo = false;
            q.mYes = false;
        }
        mQuestionsAdapter.notifyDataSetChanged();
        mPhotoLeft = "";
        mPhotoRight = "";
        mPhotoBack = "";
        mPhotoFront = "";
        mPhotoName = "";
        etComments.setText("");
        ((ImageView) findViewById(R.id.ivLeft)).setImageDrawable(getDrawable(R.drawable.camera));
        ((ImageView) findViewById(R.id.ivRight)).setImageDrawable(getDrawable(R.drawable.camera));
        ((ImageView) findViewById(R.id.ivFront)).setImageDrawable(getDrawable(R.drawable.camera));
        ((ImageView) findViewById(R.id.ivBack)).setImageDrawable(getDrawable(R.drawable.camera));
    }

    @Override
    public void webResponse(int code, int webResponse, String s) {
        super.webResponse(code, webResponse, s);
        if (webResponse != 200) {
            if (s == null) {
                s = "";
            }
            alertDialog(getString(R.string.Error), s);
            return;
        }
        switch (code) {
            case WebResponse.getmResponseMechanicSaveReport:
                clearReport();
                alertDialog(R.string.Empty, R.string.Saved);
                break;
            case WebResponse.getQuestions:
                try {
                    JSONArray jquestions = new JSONArray(s);
                    WorkActivity.mQuestions.clear();
                    for (int i = 0; i < jquestions.length(); i++) {
                        JSONObject jq = jquestions.getJSONObject(i);
                        WorkActivity.mQuestions.add(new Question(jq));
                    }
                    mQuestionsAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            mPhotoName = "";
            return;
        }
        switch (requestCode) {
            case 1:
                mPhotoFront = mPhotoName;
                previewImage(R.id.ivFront, mPhotoName);
                break;
            case 2:
                mPhotoBack = mPhotoName;
                previewImage(R.id.ivBack, mPhotoName);
                break;
            case 3:
                mPhotoRight = mPhotoName;
                previewImage(R.id.ivRight, mPhotoName);
                break;
            case 4:
                mPhotoLeft = mPhotoName;
                previewImage(R.id.ivLeft, mPhotoName);
                break;
        }
    }

    public void previewImage(int iv, String fileName) {
        ImageView img = findViewById(iv);
        File photos = new File(fileName);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photos.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int angle = 0;

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            angle = 90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            angle = 180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            angle = 270;
        }
        Bitmap b;
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        mat.setScale(0.2f, 0.2f);

        Bitmap bm = BitmapFactory.decodeFile(fileName);
        b = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mat, true);
        img.setImageBitmap(b);
        File f = new File(fileName);
        try {
            FileOutputStream fos = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDirectory() {
        mDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TaxiMechanic");
        if (!mDirectory.exists()) {
            mDirectory.mkdirs();
        }
    }

    public class QuestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvQuestion;
            CheckBox cbYes;
            CheckBox cbNo;
            EditText etComment;

            public VH(View v) {
                super(v);
                tvQuestion = v.findViewById(R.id.tvQuestion);
                etComment = v.findViewById(R.id.etComment);
                cbYes = v.findViewById(R.id.cbYes);
                cbNo = v.findViewById(R.id.cbNo);
                cbYes.setOnClickListener(this);
                cbNo.setOnClickListener(this);
            }

            public void onBind(int index) {
                Question q = mQuestions.get(index);
                tvQuestion.setText(q.mQuestion);
                cbYes.setChecked(false);
                cbNo.setChecked(false);
            }

            @Override
            public void onClick(View v) {
                int index = getAdapterPosition();
                Question q = null;
                if (index > -1) {
                    q = mQuestions.get(index);
                }

                switch (v.getId()) {
                    case R.id.cbNo:
                        etComment.setVisibility(cbNo.isChecked() ? View.VISIBLE : View.GONE);
                        if (cbNo.isChecked()) {
                            cbYes.setChecked(false);
                            q.mYes = false;
                        }
                        q.mNo = cbNo.isChecked();
                        break;
                    case R.id.cbYes:
                        q.mYes = cbYes.isChecked();
                        if (cbYes.isChecked()) {
                            q.mNo = false;
                            cbNo.setChecked(false);
                            etComment.setVisibility(View.GONE);
                            etComment.setText("");
                        }
                        break;
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new VH(WorkActivity.this.getLayoutInflater().inflate(R.layout.item_question, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ((VH) viewHolder).onBind(i);
        }

        @Override
        public int getItemCount() {
            return mQuestions.size();
        }
    }
}
