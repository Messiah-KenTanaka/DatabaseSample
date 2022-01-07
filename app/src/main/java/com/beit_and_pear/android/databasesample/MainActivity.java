package com.beit_and_pear.android.databasesample;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // 選択されたカクテルの主キーIDを表すフィールド
    private int _cocktailld = -1;
    // 選択されたカクテル名を表すフィールド
    private String _cocktailName = "";
    // データベースヘルパークラス
    private DatabaseHelper _helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // カクテルリスト用ListView(lvCocktail)を取得
        ListView lvCocktail = findViewById(R.id.lvCocktail);
        // lvCocktailにリスナを登録
        lvCocktail.setOnItemClickListener(new ListItemClickListener());
        // DBヘルパーオブジェクトを生成
        _helper = new DatabaseHelper(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        // DBヘルパーオブジェクトの解放
        _helper.close();
        super.onDestroy();
    }

    // 保存ボタンがタップされた時の処理メソッド
    public void onSaveButtonClick(View view) {
        // 感想欄を取得
        EditText etNote = findViewById(R.id.etNote);
        String note = etNote.getText().toString();

        // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
        SQLiteDatabase db = _helper.getWritableDatabase();
        // リストで選択されたカクテルのメモデータを削除　その後、インサートを行う。
        // 削除用のSQL文字列を用意。
        String sqlDelete = "DELETE FROM cocktailmemos WHERE _id = ?";
        // SQL文字列を元にプリペアドステートメントを取得
        SQLiteStatement stmt = db.compileStatement(sqlDelete);
        // 変数のバインド
        stmt.bindLong(1, _cocktailld);
        // 削除SQLの実行
        stmt.executeUpdateDelete();
        // インサート用SQL文字列の用意
        String sqlInsert = "INSERT INTO cocktailmemos (_id, name, note) VALUES (?, ?, ?)";
        // SQL文字列を元にプリペアドステートメントを取得
        stmt = db.compileStatement(sqlInsert);
        // 変数のバインド
        stmt.bindLong(1, _cocktailld);
        stmt.bindString(2, _cocktailName);
        stmt.bindString(3, note);
        // インサートSQLの実行
        stmt.executeInsert();

        // 感想らんの入力値を消去
        etNote.setText("");
        // カクテル名を「未選択」に変更
        TextView tvCocktailName = findViewById(R.id.tvCocktailName);
        tvCocktailName.setText(getString(R.string.tv_name));
        // 「保存」ボタンをタップできないように変更
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
    }

    // リストがタップされた時のメンバクラス
    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // タップされた行番号をフィールドの主キーIdに代入
            _cocktailld = position;
            // タップされた行のデータを取得、これがカクテル名となる、フィールドに代入
            _cocktailName = (String) parent.getItemAtPosition(position);
            // カクテル名を表示するTextViewに表示カクテル名を設定
            TextView tvCocktailName = findViewById(R.id.tvCocktailName);
            tvCocktailName.setText(_cocktailName);
            // 「保存」ボタンをタップできるように設定
            Button btnSave = findViewById(R.id.btnSave);
            btnSave.setEnabled(true);

            // データベースヘルパークラスオブジェクトからデータベース接続オブジェクトを取得
            SQLiteDatabase db = _helper.getWritableDatabase();
            // 主キーによる検索SQL文字列の用意
            String sql = "SELECT * FROM cocktailmemos WHERE _id = " + _cocktailld;
            // SQLの実行
            Cursor cursor = db.rawQuery(sql, null);
            // データベースから取得した値を格納する変数の用意　データがなかった時のための初期値も用意
            String note = "";
            // SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得
            while (cursor.moveToNext()) {
                // カラムのインデックス値を取得
                int idxNote = cursor.getColumnIndex("note");
                // カラムのインデックス値を元に実際のデータを取得
                note = cursor.getString(idxNote);
            }

            // 感想のEditTextの各画面部品を取得しデータベースの値を反映
            EditText etNote = findViewById(R.id.etNote);
            etNote.setText(note);
        }
    }
}