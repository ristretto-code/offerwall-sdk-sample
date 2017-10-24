package kr.ive.ive_offerwall_sdk_sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import kr.ive.offerwall_sdk.IveOfferwall;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IveOfferwall.GetPointListener, IveOfferwall.UsePointListener {
    public static final String TAG = "MainActivity";

    private TextView mPointTextView;

    private interface OnNumberInputDialogListener {
        void onNumberInput(int number);
    }

    private EditText mIdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIdEditText = (EditText) findViewById(R.id.id_edittext);
        mIdEditText.setText(loadId());

        mPointTextView = ((TextView) findViewById(R.id.point_textview));

        Button openOfferwallActivityButton = (Button) findViewById(R.id.open_offerwall_activity_button);
        openOfferwallActivityButton.setOnClickListener(this);

        Button openOfferwallFragmentButton = (Button) findViewById(R.id.open_offerwall_fragment_button);
        openOfferwallFragmentButton.setOnClickListener(this);

        Button getUserPointButton = (Button) findViewById(R.id.get_user_point_button);
        getUserPointButton.setOnClickListener(this);

        Button useUserPointButton = (Button) findViewById(R.id.use_user_point_button);
        useUserPointButton.setOnClickListener(this);
    }

    private void saveId() {
        SharedPreferences pref = getPref();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id", getId());
        editor.apply();
    }

    private String loadId() {
        SharedPreferences pref = getPref();
        return pref.getString("id", "");
    }

    private SharedPreferences getPref() {
        return getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveId();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_offerwall_activity_button:
                openOfferwallActivity();
                break;
            case R.id.open_offerwall_fragment_button:
                openOfferwallFragment();
                break;
            case R.id.get_user_point_button:
                getUserPoint();
                break;
            case R.id.use_user_point_button:
                useUsePoint();
                break;
        }
    }

    private void openOfferwallActivity() {
        String id = getId();
        if(checkValidId(id)) {
            IveOfferwall.openActivity(this, id);
        }
    }

    private void openOfferwallFragment() {
        String id = getId();
        if(checkValidId(id)) {
            setFragment(IveOfferwall.createFragment(this, id));
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content, fragment);
        fragmentTransaction.commit();
    }

    private void getUserPoint() {
        String id = getId();
        if(checkValidId(id)) {
            IveOfferwall.getPoint(this, id, this);
        }
    }

    private void useUsePoint() {
        final String id = getId();
        if(checkValidId(id)) {
            showNumberInputDialog("사용할 포인트를 입력해주세요.", new OnNumberInputDialogListener() {
                @Override
                public void onNumberInput(int number) {
                    IveOfferwall.usePoint(MainActivity.this, id, number, MainActivity.this);
                }
            });
        }
    }

    private boolean checkValidId(String id) {
        boolean isValid = !TextUtils.isEmpty(id);

        if(!isValid) {
            showAlertDialog("ID를 입력해 주세요.", null);
        }

        return isValid;
    }

    private String getId() {
        return mIdEditText.getText().toString();
    }

    private void showAlertDialog(String message, DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setPositiveButton("확인", onOkClickListener);

        builder.create().show();
    }

    private void showNumberInputDialog(String message, final OnNumberInputDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setMessage(message);
        builder.setView(editText);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int number = Integer.parseInt(editText.getText().toString());
                    if(listener != null)
                        listener.onNumberInput(number);
                } catch (NumberFormatException e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onGetPointComplete(boolean isSuccess, long point, String errorMessage) {
        setPoint(point);
    }

    @Override
    public void onUsePointComplete(boolean isSuccess, long remainPoint, String errorMessage) {
        if(isSuccess) {
            setPoint(remainPoint);
        } else {
            showAlertDialog(errorMessage, null);
        }
    }

    private void setPoint(long point) {
        mPointTextView.setText(String.valueOf(point));
    }
}