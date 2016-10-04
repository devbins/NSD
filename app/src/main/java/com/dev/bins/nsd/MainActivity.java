package com.dev.bins.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Runnable {

    public static final String SERVICE_TYPE = "_http._tcp.";
    private Button mBtnSend;
    private EditText mEtContent;
    private Toolbar mToolbar;
    private Adapter adapter;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private Socket mSocket;
    private NsdManager nsdManager;
    private NsdManager.ResolveListener mResolverListener;
    private NsdServiceInfo mNsdServiceInfo;
    private BufferedWriter bufferedWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mBtnSend.setOnClickListener(this);
        createResolverListener();
    }

    private void createResolverListener() {
        mResolverListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Toast.makeText(MainActivity.this, "onResolveFailed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                mNsdServiceInfo = nsdServiceInfo;
                new Thread(MainActivity.this).start();
            }
        };
    }


    @Override
    public void onClick(View view) {
        if (bufferedWriter!= null){
            try {
                bufferedWriter.write(mEtContent.getText().toString());
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDiscoverListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "onStartDiscoveryFailed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "onStopDiscoveryFailed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Toast.makeText(MainActivity.this, "onDiscoveryStarted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String s) {
                Toast.makeText(MainActivity.this, "onDiscoveryStopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                adapter.add(nsdServiceInfo);
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, "onServiceLost", Toast.LENGTH_SHORT).show();
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View dialog = View.inflate(MainActivity.this, R.layout.dialog, null);
        RecyclerView reycleView = (RecyclerView) dialog.findViewById(R.id.recycle);
        reycleView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        adapter = new Adapter();
        reycleView.setAdapter(adapter);
        builder.setView(dialog);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onClick(NsdServiceInfo nsdServiceInfo) {
                nsdManager.resolveService(nsdServiceInfo,mResolverListener);
                alertDialog.dismiss();
            }
        });
        nsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        createDiscoverListener();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        try {
            mSocket = new Socket(mNsdServiceInfo.getHost().getHostAddress(),mNsdServiceInfo.getPort());
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            bufferedWriter.write("我连上你了!");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
