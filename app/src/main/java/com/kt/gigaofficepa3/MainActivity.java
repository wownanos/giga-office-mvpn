package com.kt.gigaofficepa3;

// Be Certain you import the ActionBar class
// If supporting API levels lower than 11 (Android 3.0) : import android.support.v7.app.ActionBar & extends AppCompatActivity
// If supporting only API level 11 and higher : import android.app.ActionBar
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

// android.support.v7.app.ActionBarActivity is deprecated.
public class MainActivity extends AppCompatActivity {

    // 소스코드 위치 : 상단은 로그인 관련, 중단은 스레드 관련, 하단은 액션바 관련 내용

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "01011112222:123qwe", "01022866328:fv.2015"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPhoneView;
    private EditText mPasswordView;
    private View mViewProgress;
    private View mViewConnect;

    // TODO: 체크, 전역 변수로 빼지 않아도 될지 모른다.
    private Button mButtonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPhoneView = (EditText) findViewById(R.id.edittext_phone);

        mPasswordView = (EditText) findViewById(R.id.edittext_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                // 뷰의 ID가 login 이거나 키보드에서 엔터키 입력 검출되는 경우
                if (id == R.id.ime_connect || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mButtonConnect = (Button) findViewById(R.id.button_connect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mViewConnect = findViewById(R.id.view_connect);
        mViewProgress = findViewById(R.id.view_progress);

        // TODO: 수정
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*
        Log.d("1.Text.isEmpty(phone)"+(phone),""+(TextUtils.isEmpty(phone)));
        Log.d("2.isEmailValid(phone))"+(phone),""+(isEmailValid(phone)));
        Log.d("3.TexT.isEmpty(passwd)"+password,""+(TextUtils.isEmpty(password)));
        Log.d("4.isPasswordValid(pass)"+password,""+(isPasswordValid(password)));
        */
        Log.d("1.phone is -->", ""+phone);
        Log.d("2.password is -->", ""+password);

        // Check for a valid phone number.
        // 전화번호가 비어 있을 때 --> 필수사항 입니다. (error_field_required)
        // 전화번호가 유효하지 않을 때 --> 전화번호가 유효하지 않습니다. (error_invalid_email)
        if (TextUtils.isEmpty(phone)) {
            Log.d("3.isEmpty("+phone+") -->", ""+(TextUtils.isEmpty(phone)));
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            Log.d("4.!isPhoneValid("+phone+") -->", ""+(!isPhoneValid(phone)));
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        // 비밀번호를 옵션으로 하지 않고 필수사항으로 테스트
        // 비밀번호가 비어 있을 때 --> 필수사항 입니다.
        // 비밀번호가 유효하지 않을 때 --> 비밀번호가 유효하지 않습니다.
        if (TextUtils.isEmpty(password)) {
            Log.d("5.isEmpty("+password+") -->", ""+(TextUtils.isEmpty(password)));
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            Log.d("6.!isPasswordValid("+password+") -->", ""+(!isPasswordValid(password)));
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // TODO: 삭제
        /*
        // Check for a valid password, if the user entered one. 비밀번호가 옵션이다!!
        // 비밀번호가 채워있고 비밀번호가 유효하지 않을때 --> 비밀번호가 짧습니다. (error_invalid_password)
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            Log.d("5.!Empty && Valid(pass)"+password,""+(!TextUtils.isEmpty(password) && !isPasswordValid(password)));
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        */

        // 에러가 발생해서 캔슬 변수가 참이 되면, focusView 라는 인스턴스에 포커스
        // 에러가 없어서 캔슬 변수가 거짓이라면 로그인 과정을 진행한다!
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPhoneValid(String phone) {
        //TODO: Replace this with your own logic
        return (phone.length() > 9 && phone.charAt(0) == '0');
        // TODO: 삭제
        //return phone.contains("0");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // The ViewPropertyAnimator APIs are not available, so simply show
        // and hide the relevant UI components.
        mViewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        mViewConnect.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPassword;

        UserLoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mPhone)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            // doInBackground 결과가 파라미터(success)로 전달된다.
            if (success) {
                // 로그인에 성공하면 프로그레스 화면에서 다시 로그인화면으로 돌아온다.
                mViewProgress.setVisibility(success ? View.GONE : View.VISIBLE);
                mViewConnect.setVisibility(success ? View.VISIBLE : View.GONE);
                // TODO: 삭제, 로그인에 성공하면 앱을 종료한다.
                //finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    // 구글 트레이닝 : https://developer.android.com/training/basics/actionbar/adding-buttons.html#AddActions

    /**
     * 액션바에 아이템 등록 Add the Action to the Action Bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // 구글 트레이닝 : https://developer.android.com/training/basics/actionbar/adding-buttons.html#Respond

    /**
     * 액션바의 아이템 눌렀을 때 반응 등록 Respond to Action Buttons
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // TODO: 반응 등록
        switch (id) {
            case R.id.item_about:
                break;
            case R.id.item_import:
                break;
            case R.id.item_preferences:
                break;
            case R.id.item_help:
                break;
            default:
                return false;
        }
        return true;

        // TODO: 삭제
        /*
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
        */
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
