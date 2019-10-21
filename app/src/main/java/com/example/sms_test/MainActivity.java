package com.example.sms_test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
// import com.google.android.gms.auth.api.phone.SmsRetriever;



import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
   // ArrayList<String> appSignature; //firme per la ricezione di sms
    //SmsWarden warden;
    SmsGuardian guardian;
    final int REQUEST_CODE = 1;


    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button send_button = (Button)findViewById(R.id.send_button);
        //TextView number = (TextView)findViewById(R.id.number_txtview);
        //TextView text = (TextView)findViewById(R.id.text_txtview);

        //warden = new SmsWarden(context);
        guardian = new SmsGuardian();
        //Controllo per l'accesso in ricezione e invio di sms, se lo trova non concesso, lo richede
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.SEND_SMS", "android.permission.RECEIVE_SMS"}, REQUEST_CODE);
        }

        send_button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        TextView number = (TextView)findViewById(R.id.number_txtview);
                        TextView text = (TextView)findViewById(R.id.text_txtview);

                        TextView message = (TextView)findViewById(R.id.message_txtview);

                        message.setText(number.getText() + " " + text.getText());
                        String res = guardian.Send(number.getText().toString(), text.getText().toString());
                        //String res = warden.Send(number.getText().toString(), text.getText().toString());
                        Toast.makeText(context, res, Toast.LENGTH_LONG).show();

                    }
                }
        );


    }




}


class SmsWarden {

    SmsManager sender;
    String signature;

    //SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);

    public SmsWarden(String signature)
    {
        sender = SmsManager.getDefault();
        this.signature = signature;
    }
    public SmsWarden(Context context)
    {
        sender = SmsManager.getDefault();
    }

    public String Send(String number, String text)
    {   //ritorna "1" se c'è stato un errore legato agli argomenti, "0" se l'invio è andato a buon fine
        //per gli altri tipi di eccezione ritorna direttamente la stringa di errore
        String exitCode = "0";

        try {
            sender.sendTextMessage(number, null, text, null, null);
        }
        catch (IllegalArgumentException ex) {

            exitCode = "1";
        }
        catch (Exception e) {
            exitCode = e.getMessage() + " SMS failed, please try again.";

        }

        return exitCode;
    }


}



/*
  This is a helper class to generate your message hash to be included in your SMS message.

  Without the correct hash, your app won't recieve the message callback. This only needs to be
  generated once per app and stored. Then you can remove this helper class from your code.
*/
class AppSignatureHelper extends ContextWrapper {
    public static final String TAG = AppSignatureHelper.class.getSimpleName();
    private static final String HASH_TYPE = "SHA-256";
    public static final int NUM_HASHED_BYTES = 9;
    public static final int NUM_BASE64_CHAR = 11;

    public AppSignatureHelper(Context context) {
        super(context);
        getAppSignatures();
    }

    /**
     * Get all the app signatures for the current package
     * @return
     */
    public ArrayList<String> getAppSignatures() {
        ArrayList<String> appCodes = new ArrayList<>();

        try {
            // Get all package signatures for the current package
            String packageName = getPackageName();
            PackageManager packageManager = getPackageManager();
            Signature[] signatures = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES).signatures;

            // For each signature create a compatible hash
            for (Signature signature : signatures) {
                String hash = hash(packageName, signature.toCharsString());
                if (hash != null) {
                    appCodes.add(String.format("%s", hash));
                }

                Log.v(TAG, "Hash " + hash);

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return appCodes;
    }

    private static String hash(String packageName, String signature) {
        String appInfo = packageName + " " + signature;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
            }
            byte[] hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
            // encode into Base64
            String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

            Log.d(TAG, String.format("pkg: %s -- hash: %s", packageName, base64Hash));
            return base64Hash;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e);
        }
        return null;
    }
}
