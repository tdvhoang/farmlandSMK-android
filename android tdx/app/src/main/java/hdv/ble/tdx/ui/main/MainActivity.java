package hdv.ble.tdx.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.ui.InforFragment;
import hdv.ble.tdx.ui.SplashActivity;
import hdv.ble.tdx.ui.account.AccountFragment;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.setup.SetupFragment;

/**
 * Created by Ann on 2/26/16.
 */
public class MainActivity extends BaseActivity implements MainMvpView {

    @Bind(R.id.tvTitle)
    TextView tvTitle;
    @Bind(R.id.ibTitleBackMainActivity)
    ImageButton ibTitleBackMainActivity;

    @Bind(R.id.llTabMenu)
    LinearLayout llTabMenu;

    @Inject MainPresenter mainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_new_main);
        ButterKnife.bind(this);
        mainPresenter.attachView(this);
        mainPresenter.bindService();
        addFragment(new SetupFragment());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainPresenter.detachView();
    }

    @OnClick(R.id.ibTabHome)
    void OnClickedTabHome(){
        setTitle("Home");
        repalceFragment(new SetupFragment());
    }

    @OnClick(R.id.ibTabAccount)
    void OnClickedTabAccount(){

        setTitle("Tài khoản");
        repalceFragment(new AccountFragment());

    }

    @OnClick(R.id.ibTabExit)
    void OnClickedTabExit(){

        repalceFragment(new InforFragment());
    }

    @OnClick(R.id.ibTitleBackMainActivity)
    void OnClickBack(){
        onBackPressed();
    }

    private static final String TAG = "MainActivity";


    public Fragment getVisibleFragment(){
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }


    private void repalceFragment(Fragment fragment){

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit();
    }

    private void addFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fragment, fragment)
                .commit();
    }

    public void showTitleUser(boolean isvalue){

    }

    public void showTabMenu(boolean isvalue){
        if(isvalue){
            llTabMenu.setVisibility(View.VISIBLE);
        }else{
            llTabMenu.setVisibility(View.GONE);

        }
    }

    public void setTextTitle(String ss){
        tvTitle.setText(ss);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void showError(String ss) {
        toast(ss);

    }

    @Override
    public void toast(final String ss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, ss, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void setReceiveData(String data) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void setNameDevice(String ss) {

    }

    @Override
    public void setSendData(String data) {

    }

    @Override
    public void updateImageForStatus(byte lock, byte vibrae) {

    }

    @Override
    public void errorPin() {

        toast("Sai mã pin!");
        Intent ii = new Intent(this,SplashActivity.class);
        finish();
        startActivity(ii);

    }

    private void setTitle(String ss){
        tvTitle.setText(ss);

    }

    public MainPresenter getMainPresenter() {
        return mainPresenter;
    }
}
