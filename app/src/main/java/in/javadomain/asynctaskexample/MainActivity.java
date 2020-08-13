package in.javadomain.asynctaskexample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MainActivity extends Activity {
    private TextView incomeNum, incomeMsg,numOpr, numCircle;
    IntentFilter iFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(myReceiver, iFilter);
        incomeNum = (TextView) findViewById(R.id.Income_Msg_Number);
        numOpr = (TextView) findViewById(R.id.Num_Operator);
        numCircle = (TextView) findViewById(R.id.Num_Circle);
        incomeMsg = (TextView) findViewById(R.id.Income_Msg);
    }


    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public static final String SMS_BUNDLE = "pdus";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle intentExtras = intent.getExtras();
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                String smsMessageStr = "";


                for (int i = 0; i < sms.length; ++i) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                    String smsBody = smsMessage.getMessageBody().toString();
                    // SMS body content
                    if (null!=smsBody) {
                        incomeMsg.setText("SMS Content :"+smsBody);
                    }

                    String address = smsMessage.getOriginatingAddress();
                    smsMessageStr += "SMS From: " + address + "\n";
                    Toast.makeText(context, smsMessageStr, Toast.LENGTH_LONG).show();
                    incomeNum.setText("SMS Received From :"+address.substring(3,address.length()));
                    try {
                        String jdCircleOprInfoURL = "http://demo.javadomain.in/AsyncTaskAndroid/GetCircleOpr.php?input="+address.substring(3,address.length())+"";
                        FetchOprCircleInfo fetchOprCircleInfo = new FetchOprCircleInfo();
                        fetchOprCircleInfo.execute(new String[]{jdCircleOprInfoURL});
                    } catch (Exception e) {
                        Toast.makeText(context, "Javadomain.in-->" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

    };

    @Override
    public void onDestroy()    {
        super.onDestroy();
        unregisterReceiver(myReceiver);

    }


    public class FetchOprCircleInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... reqArray) {
            String responseStatus = "";
            try {
                if(reqArray!=null) {
                    if (null != reqArray[0]) {
                        // Jsoup used here
                        Document doc = Jsoup.connect(reqArray[0]).timeout(0).get();
                        if (doc != null) {
                            String result = doc.select("body").text();
                            responseStatus = result;
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return responseStatus;
        }

        @Override
        protected void onPostExecute(String result) {
            String opr = "";
            String circle = "";
            if(null!=result && !result.isEmpty()){
                if(result.contains("|")){
                    String[] splitPipe = result.split("\\|");
                        opr = splitPipe[0];
                        circle = splitPipe[1];
                     }
            }

            numOpr.setText("Operator : "+opr.toString());
            numCircle.setText("Circle : "+circle.toString());
        }
    }
}