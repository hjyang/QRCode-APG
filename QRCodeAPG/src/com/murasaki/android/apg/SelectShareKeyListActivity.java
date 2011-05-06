package com.murasaki.android.apg;

import com.murasaki.android.qrcode.Intents;

import org.bouncycastle2.jce.provider.BouncyCastleProvider;
import org.bouncycastle2.openpgp.PGPException;
import org.bouncycastle2.openpgp.PGPPublicKey;
import org.bouncycastle2.bcpg.PublicKeyPacket;
import org.bouncycastle2.bcpg.BCPGKey;
import org.bouncycastle2.openpgp.PGPPublicKeyRing;
import org.bouncycastle2.util.encoders.Hex;
import org.bouncycastle2.util.encoders.Base64;

import org.thialfihar.android.apg.Apg;
import org.thialfihar.android.apg.Constants;
import org.thialfihar.android.apg.Id;
import org.thialfihar.android.apg.BaseActivity;
//import org.thialfihar.android.apg.KeyListActivity;
//import org.thialfihar.android.apg.KeyServerQueryActivity;
//import org.thialfihar.android.apg.PublicKeyListActivity;
//import org.thialfihar.android.apg.SelectSecretKeyListAdapter;

import com.google.zxing.BarcodeFormat;
import com.murasaki.android.qrcode.Contents;
import com.murasaki.android.qrcode.Intents;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import java.lang.Long;
import java.security.MessageDigest;
import java.security.PublicKey;

public class SelectShareKeyListActivity extends BaseActivity {
    protected ListView mList;
    protected SelectShareKeyListAdapter mListAdapter;
    protected View mFilterLayout;
    protected Button mClearFilterButton;
    protected TextView mFilterInfo;

    protected long mSelectedKeyId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        setContentView(R.layout.select_secret_key);

        mList = (ListView) findViewById(R.id.list);

        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                
            	PGPPublicKey masterKey = Apg.getPublicKey(id);
            	
            	try {
             		  //byte[] public_key = masterKey.getKeyPacket().getKey().getEncoded();
              		  byte[] public_key_byte = masterKey.getKeyPacket().getEncodedContents();
            		  //PublicKey public_key = masterKey.getKey(new BouncyCastleProvider());
            	      //byte[] public_key_byte = public_key.getEncoded();
             	      MessageDigest md = MessageDigest.getInstance("SHA-256");
                      //MessageDigest md = MessageDigest.getInstance("SHA-1");
            	      //md.update(public_key_byte); 
                      md.update((byte)0x99);
                      md.update((byte)(public_key_byte.length >> 8));
                      md.update((byte)public_key_byte.length);
                      md.update(public_key_byte);
                      
                      byte[] hashed_key = md.digest();
            	      //byte[] hex_hashed_key = Hex.encode(hashed_key);
            	      //String hashed_key_str = new String(hex_hashed_key);
                      byte[] b64_hashed_key = Base64.encode(hashed_key);
                      String hashed_key_str = new String(b64_hashed_key);
                      
            	      Intent intent = new Intent(Intents.Encode.ACTION);
            	      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            	      intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
            	      intent.putExtra(Intents.Encode.DATA, hashed_key_str);
            	      intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
            	      startActivity(intent);
            	      
            	      //Toast.makeText(getApplicationContext(), hashed_key_str, Toast.LENGTH_LONG).show();
             	    }
           	
             	catch( Exception e )
             	{
             		String err_msg = new String("no such hash algorithm");
             		Toast.makeText(getApplicationContext(), err_msg, Toast.LENGTH_SHORT).show();
             	}
            	
            }
        });

        mFilterLayout = (View) findViewById(R.id.layout_filter);
        mFilterInfo = (TextView) mFilterLayout.findViewById(R.id.filterInfo);
        mClearFilterButton = (Button) mFilterLayout.findViewById(R.id.btn_clear);

        mClearFilterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleIntent(new Intent());
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String searchString = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchString = intent.getStringExtra(SearchManager.QUERY);
            if (searchString != null && searchString.trim().length() == 0) {
                searchString = null;
            }
        }

        if (searchString == null) {
            mFilterLayout.setVisibility(View.GONE);
        } else {
            mFilterLayout.setVisibility(View.VISIBLE);
            mFilterInfo.setText(getString(R.string.filterInfo, searchString));
        }

        if (mListAdapter != null) {
            mListAdapter.cleanup();
        }

        mListAdapter = new SelectShareKeyListAdapter(this, mList, searchString);
        mList.setAdapter(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, Id.menu.option.search, 0, R.string.menu_search)
                .setIcon(android.R.drawable.ic_menu_search);
        return true;
    }
}
