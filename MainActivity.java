package com.password.dylan.cet324password;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText currentPassword, newPassword;
    TextView tv;
    ProgressBar pb;
    Button button, showbutton;
    String newPassword1, currentPassword1, getPassword;

    String[] common = {
            "password",
            "12345",
            "football",
            "black",
            "12345678",
            "baseball",
            "P@55WORD"
    };

    // connect to mySQLiteHelper activity
    MySQLiteHelper db = new MySQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPassword = (EditText) findViewById(R.id.currentPassword);
        newPassword = (EditText) findViewById(R.id.password);
        pb = (ProgressBar) findViewById(R.id.pb);
        button = (Button) findViewById(R.id.btnSave);
        button.setOnClickListener(this);

        showbutton = (Button) findViewById(R.id.btnShow);
        showbutton.setOnClickListener(this);

        tv = (TextView) findViewById(R.id.tv);

        currentPassword1 = currentPassword.getText().toString();
        newPassword1 = newPassword.getText().toString();

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

                String pass = s.toString();

                if (pass.length() == 0)
                {
                    pb.setProgress(0);
                }
                else if (pass.matches("^[A-Za-z\\d]{0,6}"))
                {
                    pb.setProgress(0);
                    tv.setText("Too Short");
                }
                else if (pass.matches("^[A-Za-z]{7,19}"))
                {
                    pb.setProgress(1);
                    tv.setText("Weak");
                }
                else if (pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{7,19}"))
                {
                    pb.setProgress(3);
                    tv.setText("Good");
                }
                else if (pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$£~:?%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}"))
                {
                    pb.setProgress(10);
                    tv.setText("Strong");
                }
                else if (pass.matches("^[A-Za-z]{19,1000}"))
                {
                    pb.setProgress(1);
                    tv.setText("Weak");
                }
                else
                {   pb.setProgress(0);
                }
            }
        });
    }

    public static String getSecurePassword(String passwordToHash, byte[] salt){

        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x1000, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt;
    }

    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.btnSave) {

            String pass = newPassword.getText().toString();
            String current = currentPassword.getText().toString();

            Cursor cursor = db.fetchPassword();

            getPassword = cursor.getString(cursor.getColumnIndex("password"));
            byte [] newByte = cursor.getBlob(cursor.getColumnIndex("salt"));


            if(getPassword.equals("password")){

                    if (TextUtils.isEmpty(pass) || !pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}")) {
                        pb.setProgress(0);
                        newPassword.setError("Your password must contain one upper case letter, one digit, one special character and at least 8 characters.");
                    }
                    else if (Arrays.asList(common).contains(pass)) {
                        pb.setProgress(0);
                        newPassword.setError("Password too easy. Try again");
                    }

                    else if (pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}")) {
                        pb.setProgress(10);
                        try {
                            MySQLiteHelper db = new MySQLiteHelper(this);
                            SQLiteDatabase sql = db.getWritableDatabase();
                            newPassword1 = newPassword.getText().toString();
                            byte[] salt = getSalt();

                            String securePassword = getSecurePassword(newPassword1, salt);

                            SQLiteStatement stmt = sql.compileStatement("UPDATE passwordCS SET password = ?, salt = ? WHERE _id = 1");
                            stmt.bindString(1, securePassword);
                            stmt.bindBlob(2, salt);
                            stmt.execute();
                            Toast.makeText(this, "Password saved", Toast.LENGTH_LONG).show();

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
            }

            else if (getPassword != "password")
            {
                try {

                    String currentverify = getSecurePassword(current, newByte);

                    if (!getPassword.equals(currentverify)) {
                        currentPassword.setError("Current Password does not match");
                    }
                    else if (Arrays.asList(common).contains(pass)) {
                        pb.setProgress(0);
                        newPassword.setError("Password too easy. Try again");
                    }

                    else if (TextUtils.isEmpty(pass) || !pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}")) {
                        pb.setProgress(0);
                        newPassword.setError("Your password must contain one upper case letter, one digit, one special character and at least 7 characters.");

                    } else if (pass.matches("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d][A-Za-z\\d!@#$%^&*()_+]{7,19}")) {

                        pb.setProgress(10);
                        try {
                            MySQLiteHelper db = new MySQLiteHelper(this);
                            SQLiteDatabase sql = db.getWritableDatabase();
                            newPassword1 = newPassword.getText().toString();
                            byte[] salt = getSalt();

                            String securePassword = getSecurePassword(newPassword1, salt);

                            SQLiteStatement stmt = sql.compileStatement("UPDATE passwordCS SET password = ?, salt = ? WHERE _id = 1");
                            stmt.bindString(1, securePassword);
                            stmt.bindBlob(2, salt);
                            stmt.execute();
                            Toast.makeText(this, "Password saved", Toast.LENGTH_LONG).show();

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
            }
        }
        else if (id == R.id.btnShow)
        {
            Cursor cursor = db.fetchPassword();
            getPassword = cursor.getString(cursor.getColumnIndex("password"));

            AlertDialog.Builder ab = new AlertDialog.Builder(this);

            ab.setTitle("Password: ");
            ab.setMessage(getPassword);
            ab.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                }
            });
            ab.show();
        }
    }
}