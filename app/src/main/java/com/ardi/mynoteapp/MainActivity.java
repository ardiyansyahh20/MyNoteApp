package com.ardi.mynoteapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.ardi.mynoteapp.adapter.NoteAdapter;
import com.ardi.mynoteapp.db.NoteHelper;

import static com.ardi.mynoteapp.FormAddUpdateActivity.REQUEST_UPDATE;
import static com.ardi.mynoteapp.db.DatabaseContract.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerView rvNotes;
    ProgressBar progressBar;
    FloatingActionButton fabAdd;

    private Cursor list;
    private NoteAdapter adapter;
    private NoteHelper noteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Notes");

        rvNotes = (RecyclerView) findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setHasFixedSize(true);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        adapter = new NoteAdapter(this);
        adapter.setListNotes(list);
        rvNotes.setAdapter(adapter);

        new LoadNoteAsync().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_add) {
            Intent intent = new Intent(MainActivity.this, FormAddUpdateActivity.class);
            startActivityForResult(intent, FormAddUpdateActivity.REQUEST_ADD);
        }
    }

    private class LoadNoteAsync extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return getContentResolver().query(CONTENT_URI, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor notes) {
            super.onPostExecute(notes);
            progressBar.setVisibility(View.GONE);

            list = notes;
            adapter.setListNotes(list);
            adapter.notifyDataSetChanged();

            if (list.getCount() == 0) {
                showSnackBarMessage("Tidak ada data saat ini");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FormAddUpdateActivity.REQUEST_ADD) {
            if (resultCode == FormAddUpdateActivity.RESULT_ADD) {
                new LoadNoteAsync().execute();
                showSnackBarMessage("Satu item berhasil ditambahkan");
                rvNotes.getLayoutManager().smoothScrollToPosition(rvNotes, new RecyclerView.State(), 0);
            }
        } else if (requestCode == REQUEST_UPDATE) {
            if (resultCode == FormAddUpdateActivity.RESULT_UPDATE) {
                new LoadNoteAsync().execute();
                showSnackBarMessage("Satu item berhasil diubah");
                int position = data.getIntExtra(FormAddUpdateActivity.EXTRA_POSITION, 0);
                rvNotes.getLayoutManager().smoothScrollToPosition(rvNotes, new RecyclerView.State(), position);
            } else if (resultCode == FormAddUpdateActivity.RESULT_DELETE) {
                new LoadNoteAsync().execute();
                showSnackBarMessage("Satu item berhasil dihapus");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noteHelper != null) {
            noteHelper.close();
        }
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }
}
