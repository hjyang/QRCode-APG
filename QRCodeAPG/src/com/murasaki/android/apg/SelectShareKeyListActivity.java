package com.murasaki.android.apg;

import org.bouncycastle2.openpgp.PGPPublicKey;
import org.bouncycastle2.openpgp.PGPPublicKeyRing;
import org.bouncycastle2.util.encoders.Hex;
import org.bouncycastle2.util.encoders.HexEncoder;
import org.thialfihar.android.apg.Apg;
import org.thialfihar.android.apg.Constants;
import org.thialfihar.android.apg.Id;
import org.thialfihar.android.apg.BaseActivity;
import org.thialfihar.android.apg.KeyListActivity;
import org.thialfihar.android.apg.KeyServerQueryActivity;
import org.thialfihar.android.apg.PublicKeyListActivity;
import org.thialfihar.android.apg.SelectSecretKeyListAdapter;

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

import java.lang.Long;

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
            	byte[] finger_print    = masterKey.getFingerprint();
            	byte[] hex_fingerp     = Hex.encode(finger_print);
            	String fp_str          = new String(hex_fingerp);
            	
            	Toast.makeText(getApplicationContext(), fp_str, Toast.LENGTH_SHORT).show();
            	
            	//Toast.makeText(getApplicationContext(), Long.toHexString(id), Toast.LENGTH_SHORT).show();
            	
                
                /*Intent data = new Intent();
                data.putExtra(Apg.EXTRA_KEY_ID, id);
                data.putExtra(Apg.EXTRA_USER_ID, (String)mList.getItemAtPosition(position));
                setResult(RESULT_OK, data);
                finish();*/
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
