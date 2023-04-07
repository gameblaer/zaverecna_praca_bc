package sk.ukf.wiw_google_earth;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class History extends AppCompatActivity {

    private final DB_Functions dbh = new DB_Functions(this);
    private ListView gLV;
    private ArrayAdapter gAdapter;
    private SimpleCursorAdapter cadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        gLV = findViewById(R.id.listViewHistory);

        showWay();
        addCursorAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.BackFromHistory) {
            //startActivityForResult(new Intent(MapsActivity.this, Settings.class), 1);
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return true;
        }

        //Delete
        if (item.getItemId() == R.id.deleteAllData) {

            final boolean[] isReturn = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete)+" ?");
            builder.setMessage(getString(R.string.deleteQuestion));
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                //show what delete
                Toast.makeText(History.this,  getString(R.string.deleted_all), Toast.LENGTH_SHORT).show();

                //detele
                dbh.allDelete();

                setResult(RESULT_OK, new Intent());
                finish();
                isReturn[0] = true;
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                // akcia pre stlačenie tlačidla Zrušiť
                isReturn[0] = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return isReturn[0];
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuhistory, menu);

        return true;
    }

    private void showWay(){
        gAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dbh.getAllWayData());
        gLV.setAdapter(gAdapter);
    }

    private void addCursorAdapter(){

        Cursor cursor = dbh.getCursorWayData();
        if (cursor != null) {
            cadapter = new SimpleCursorAdapter(this, R.layout.history_lv, cursor, new String[] {
                    MyDB.WAY.WAY_ID,
                    MyDB.WAY.NAME,
            }, new int[]{
                    R.id.textView13,
                    R.id.textView15,
            }, 0);
            gLV.setAdapter(cadapter);
        } else {
            showWay();
        }

        gLV.setOnItemClickListener((adapterView, view, i, l) -> {
            Cursor c = ((SimpleCursorAdapter) gLV.getAdapter()).getCursor();
            c.moveToPosition(i);
            //Log.d("CADAPTER",  c.getLong(0)+"" );
            long idWay = c.getLong(0);

            Toast.makeText(History.this, getString(R.string.opening), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(History.this, sk.ukf.wiw_google_earth.ShowData.class);
            intent.putExtra("idWay", Long.toString(idWay) );
            startActivityForResult(intent, 1);
        });

        /*
        gLV.setOnItemLongClickListener((adapterView, view, i, l) -> {
            //Delete
            final boolean[] isReturn = {false};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete)+" ?");
            builder.setMessage(getString(R.string.deleteQuestion));
            builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {

                Cursor c = ((SimpleCursorAdapter) gLV.getAdapter()).getCursor();
                c.moveToPosition(i);
                long id = c.getLong(0);

                //show what delete
                Toast.makeText(History.this, getString(R.string.deleted)+" id("+id+") "+getString(R.string.name)+"("+dbh.getWayName(id)+").", Toast.LENGTH_SHORT).show();

                //detele
                dbh.deleteWay(id);

                showWay();
                addCursorAdapter();

                isReturn[0] = true;
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                // akcia pre stlačenie tlačidla Zrušiť
                isReturn[0] = false;
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            return isReturn[0];

            //return true blokovat onItemClickListener
            //return false neblokovat onItemClickListener
        });
        */

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK){

                }
                break;
        }
        showWay();
        addCursorAdapter();
    }

}