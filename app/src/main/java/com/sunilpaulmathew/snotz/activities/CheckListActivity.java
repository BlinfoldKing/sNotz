package com.sunilpaulmathew.snotz.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.snotz.R;
import com.sunilpaulmathew.snotz.adapters.CheckListAdapter;
import com.sunilpaulmathew.snotz.utils.CheckListItems;
import com.sunilpaulmathew.snotz.utils.CheckLists;
import com.sunilpaulmathew.snotz.utils.Common;
import com.sunilpaulmathew.snotz.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 10, 2021
 */
public class CheckListActivity extends AppCompatActivity {

    private final List<CheckListItems> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        AppCompatImageButton mSave = findViewById(R.id.save_button);
        AppCompatImageButton mBack = findViewById(R.id.back_button);
        MaterialTextView mTitle = findViewById(R.id.title);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.getSpanCount(this)));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        if (Utils.exist(getExternalFilesDir("checklists") + "/" + CheckLists.getCheckListName()) && CheckLists.getData(this).size() > 0) {
            mData.addAll(CheckLists.getData(this));
        } else {
            mData.add(new CheckListItems("", false));
        }

        if (CheckLists.getCheckListName() != null) {
            mTitle.setText(CheckLists.getCheckListName());
        }

        mRecyclerView.setAdapter(new CheckListAdapter(mData));

        /*
         * Based on the following Stack Overflow discussion
         * https://stackoverflow.com/questions/55949538/update-onmove-changes-in-recycler-view-data-to-room-database
         */
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (viewHolder.getAdapterPosition() < target.getAdapterPosition()) {
                    for (int i = viewHolder.getAdapterPosition(); i < target.getAdapterPosition(); i++) {
                        Collections.swap(mData, i, i + 1);
                    }
                } else {
                    for (int i = viewHolder.getAdapterPosition(); i > target.getAdapterPosition(); i--) {
                        Collections.swap(mData, i, i - 1);
                    }
                }
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

        });

        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mSave.setOnClickListener(v -> saveCheckList());

        mBack.setOnClickListener(v -> finish());
    }

    private void saveCheckList() {
        String mCheckListName = CheckLists.getCheckListName();
        if (mCheckListName.isEmpty()) {
            Utils.showSnackbar(findViewById(android.R.id.content), getString(R.string.check_list_name_empty_message));
            return;
        }
        JSONArray mJSONArray = new JSONArray();
        JSONObject mJSONObject = new JSONObject();
        try {
            for (CheckListItems items : mData) {
                if (!items.getTitle().equals("")) {
                    try {
                        JSONObject checklist = new JSONObject();
                        checklist.put("title", items.getTitle());
                        checklist.put("done", items.isChecked());

                        mJSONArray.put(checklist);
                    } catch (JSONException ignored) {
                    }
                    mJSONObject.put("checklist", mJSONArray);
                    Utils.create(mJSONObject.toString(), getExternalFilesDir("checklists") + "/" + mCheckListName);
                }
            }
        } catch (JSONException ignored) {
        }
        Common.isReloading(true);
        finish();
    }

}