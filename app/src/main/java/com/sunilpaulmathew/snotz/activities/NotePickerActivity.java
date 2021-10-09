package com.sunilpaulmathew.snotz.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.snotz.R;
import com.sunilpaulmathew.snotz.utils.AsyncTasks;
import com.sunilpaulmathew.snotz.utils.Utils;
import com.sunilpaulmathew.snotz.utils.sNotzColor;
import com.sunilpaulmathew.snotz.utils.sNotzData;
import com.sunilpaulmathew.snotz.utils.sNotzItems;
import com.sunilpaulmathew.snotz.utils.sNotzUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 13, 2020
 */
public class NotePickerActivity extends AppCompatActivity {

    private AppCompatEditText mContents;
    private AppCompatImageView mImage;
    private Bitmap mBitmap = null;
    private int mSelectedColorBg, mSelectedColorTxt;
    private LinearLayout mProgressLayout;
    private String mNote = null;
    private SwitchCompat mHidden;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnote);

        AppCompatImageButton mAdd = findViewById(R.id.add);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        AppCompatImageButton mSave = findViewById(R.id.save_button);
        mImage = findViewById(R.id.image);
        mContents = findViewById(R.id.contents);
        mProgressLayout = findViewById(R.id.progress_layout);
        MaterialCardView mColorBackground = findViewById(R.id.color_background);
        MaterialCardView mColorText = findViewById(R.id.color_text);
        NestedScrollView mScrollView = findViewById(R.id.scroll_view);
        mHidden = findViewById(R.id.hidden);

        if (Build.VERSION.SDK_INT < 30 && Utils.isPermissionDenied(this)) {
            LinearLayout mMainLayout = findViewById(R.id.main_layout);
            LinearLayout mColorLayout = findViewById(R.id.color_layout);
            LinearLayout mPermissionLayout = findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = findViewById(R.id.permission_text);
            mPermissionText.setText(getString(R.string.permission_denied_message));
            mMainLayout.setVisibility(View.GONE);
            mColorLayout.setVisibility(View.GONE);
            mPermissionLayout.setVisibility(View.VISIBLE);
            mPermissionGrant.setOnClickListener(v -> {
                ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                finish();
            });
            return;
        }

        mColorBackground.setCardBackgroundColor(sNotzColor.getAccentColor(this));
        mColorText.setCardBackgroundColor(sNotzColor.getTextColor(this));
        mScrollView.setBackgroundColor(sNotzColor.getAccentColor(this));
        mContents.setTextColor(sNotzColor.getTextColor(this));
        mContents.setHintTextColor(sNotzColor.getTextColor(this));
        mSelectedColorBg = sNotzColor.getAccentColor(this);
        mSelectedColorTxt = sNotzColor.getTextColor(this);

        if (getIntent().getData() != null) {
            // Handle notes picked from File Manager
            try {
                InputStream inputStream = getContentResolver().openInputStream(getIntent().getData());
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int result = bis.read(); result != -1; result = bis.read()) {
                    buf.write((byte) result);
                }
                mNote = buf.toString("UTF-8");
            } catch (IOException ignored) {}

            if (mNote != null) {
                if (sNotzUtils.validBackup(mNote)) {
                    new MaterialAlertDialogBuilder(this)
                            .setCancelable(false)
                            .setMessage(getString(R.string.restore_notes_question))
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> finish())
                            .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> restore(this)).show();
                } else {
                    Utils.toggleKeyboard(mContents, this);
                    mContents.setText(mNote);
                }
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle(R.string.note_editor)
                        .setMessage(getString(R.string.file_path_error))
                        .setCancelable(false)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> finish()).show();
            }
        }

        mColorBackground.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(this)
                .setTitle(R.string.choose_color)
                .initialColor(sNotzColor.getAccentColor(this))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {
                })
                .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                    mScrollView.setBackgroundColor(selectedColor);
                    mColorBackground.setCardBackgroundColor(selectedColor);
                    mSelectedColorBg = selectedColor;
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                }).build().show());

        mColorText.setOnClickListener(v -> ColorPickerDialogBuilder
                .with(this)
                .setTitle(R.string.choose_color)
                .initialColor(sNotzColor.getAccentColor(this))
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(selectedColor -> {
                })
                .setPositiveButton(R.string.ok, (dialog, selectedColor, allColors) -> {
                    mContents.setTextColor(selectedColor);
                    mContents.setHintTextColor(selectedColor);
                    mColorText.setCardBackgroundColor(selectedColor);
                    mSelectedColorTxt = selectedColor;
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                }).build().show());

        mBack.setOnClickListener(v -> onBackPressed());

        mAdd.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mAdd);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 0, Menu.NONE, getString(mBitmap == null ? R.string.image_add : R.string.image_replace));
            if (mBitmap != null) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.image_remove));
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        if (Build.VERSION.SDK_INT < 29 && Utils.isPermissionDenied(this)) {
                            ActivityCompat.requestPermissions(this, new String[] {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        } else if (Utils.getBoolean("first_image", true, this)) {
                            new MaterialAlertDialogBuilder(this)
                                    .setIcon(R.mipmap.ic_launcher)
                                    .setTitle(R.string.warning)
                                    .setMessage(getString(R.string.image_add_warning))
                                    .setCancelable(false)
                                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                                    })
                                    .setPositiveButton(R.string.go_ahead, (dialogInterface, i) -> {
                                        Utils.saveBoolean("first_image", false, this);
                                        Intent addImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        try {
                                            startActivityForResult(addImage, 0);
                                        } catch (ActivityNotFoundException ignored) {}
                                    }).show();
                        } else {
                            Intent addImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            try {
                                startActivityForResult(addImage, 0);
                            } catch (ActivityNotFoundException ignored) {}
                        }
                        break;
                    case 1:
                        mImage.setImageBitmap(null);
                        mImage.setVisibility(View.GONE);
                        mBitmap = null;
                }
                return false;
            });
            popupMenu.show();
        });

        mSave.setOnClickListener(v -> {
            if (mContents.getText() == null || mContents.getText().toString().trim().isEmpty()) {
                Utils.showSnackbar(findViewById(R.id.contents), getString(R.string.text_empty));
                return;
            }
            save(this).execute();
        });
    }

    private AsyncTasks save(Activity activity) {
        return new AsyncTasks() {
            private JSONArray mJSONArray = null;
            private JSONObject mJSONObject = null;
            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mJSONObject = new JSONObject();
                mJSONArray = new JSONArray();
            }

            @Override
            public void doInBackground() {
                if (Utils.exist(getFilesDir().getPath() + "/snotz")) {
                    try {
                        for (sNotzItems items : sNotzData.getRawData(activity)) {
                            JSONObject note = new JSONObject();
                            note.put("note", items.getNote());
                            note.put("date", items.getTimeStamp());
                            note.put("image", items.getImageString());
                            note.put("hidden", items.isHidden());
                            note.put("colorBackground", items.getColorBackground());
                            note.put("colorText", items.getColorText());
                            note.put("noteID", items.getNoteID());
                            mJSONArray.put(note);
                        }
                        JSONObject note = new JSONObject();
                        note.put("note", mContents.getText());
                        note.put("date", DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                        note.put("image", (mBitmap != null ? sNotzUtils.bitmapToBase64(mBitmap, activity) : null));
                        note.put("hidden", mHidden.isChecked());
                        note.put("colorBackground", mSelectedColorBg);
                        note.put("colorText", mSelectedColorTxt);
                        note.put("noteID", 0);
                        mJSONArray.put(note);
                        mJSONObject.put("sNotz", mJSONArray);
                        Utils.create(mJSONObject.toString(), getFilesDir().getPath() + "/snotz");
                    } catch (JSONException ignored) {
                    }
                } else {
                    try {
                        JSONObject note = new JSONObject();
                        note.put("note", mContents.getText());
                        note.put("date", DateFormat.getDateTimeInstance().format(System.currentTimeMillis()));
                        note.put("image", (mBitmap != null ? sNotzUtils.bitmapToBase64(mBitmap, activity) : null));
                        note.put("hidden", mHidden.isChecked());
                        note.put("colorBackground", mSelectedColorBg);
                        note.put("colorText", mSelectedColorTxt);
                        note.put("noteID", 0);
                        mJSONArray.put(note);
                        mJSONObject.put("sNotz", mJSONArray);
                        Utils.create(mJSONObject.toString(), getFilesDir().getPath() + "/snotz");
                    } catch (JSONException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                Utils.restartApp(activity);
            }
        };
    }

    public void restore(Activity activity) {
        new AsyncTasks() {
            private JSONArray mJSONArray;
            private JSONObject mJSONObject;
            private int i;
            @Override
            public void onPreExecute() {
                mProgressLayout.setVisibility(View.VISIBLE);
                mJSONObject = new JSONObject();
                mJSONArray = new JSONArray();
                i = 0;
            }

            @Override
            public void doInBackground() {
                try {
                    if (Utils.exist(activity.getFilesDir().getPath() + "/snotz")) {
                        for (sNotzItems items : sNotzData.getRawData(activity)) {
                            JSONObject note = new JSONObject();
                            note.put("note", items.getNote());
                            note.put("date", items.getTimeStamp());
                            note.put("image", items.getImageString());
                            note.put("hidden", items.isHidden());
                            note.put("colorBackground", items.getColorBackground());
                            note.put("colorText", items.getColorText());
                            note.put("noteID", i);
                            i++;
                            mJSONArray.put(note);
                        }
                    }
                    if (sNotzUtils.validBackup(mNote)) {
                        for (sNotzItems items : sNotzUtils.getNotesFromBackup(mNote, activity)) {
                            JSONObject note = new JSONObject();
                            note.put("note", items.getNote());
                            note.put("date", items.getTimeStamp());
                            note.put("image", items.getImageString());
                            note.put("hidden", items.isHidden());
                            note.put("colorBackground", items.getColorBackground());
                            note.put("colorText", items.getColorText());
                            note.put("noteID", i);
                            i++;
                            mJSONArray.put(note);
                        }
                    }
                    mJSONObject.put("sNotz", mJSONArray);
                    Utils.create(mJSONObject.toString(), activity.getFilesDir().getPath() + "/snotz");
                } catch (JSONException ignored) {
                }
            }

            @Override
            public void onPostExecute() {
                mProgressLayout.setVisibility(View.GONE);
                Utils.restartApp(activity);
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && data != null && resultCode == RESULT_OK){
            if (data.getData() != null) {
                try {
                    float mRatio;
                    mImage.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()));
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    int mSize = sNotzUtils.getMaxSize(this);
                    int mHeight, mWidth;
                    if (mBitmap.getHeight() > mBitmap.getWidth()) {
                        mRatio = (float) (mBitmap.getHeight() / mBitmap.getWidth());
                        mHeight = mSize;
                        mWidth = (int) (mSize / mRatio);
                    } else if (mBitmap.getHeight() == mBitmap.getWidth()) {
                        mHeight = mSize;
                        mWidth = mSize;
                    } else {
                        mRatio = (float) (mBitmap.getWidth() / mBitmap.getHeight());
                        mHeight = (int) (mSize / mRatio);
                        mWidth = mSize;
                    }
                    LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(mWidth, mHeight);
                    mLayoutParams.gravity = Gravity.CENTER;
                    mImage.setLayoutParams(mLayoutParams);
                    mImage.setVisibility(View.VISIBLE);
                } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mContents.getText() != null && !mContents.getText().toString().isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.discard_note))
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    })
                    .setPositiveButton(R.string.discard, (dialogInterface, i) -> finish()).show();
            return;
        }
        finish();
    }

}