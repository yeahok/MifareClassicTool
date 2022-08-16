/*
 * Copyright 2013 Gerhard Klostermeier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.syss.MifareClassicTool.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.TextViewCompat;

import de.syss.MifareClassicTool.Common;
import de.syss.MifareClassicTool.R;

/**
 * Display tag info like technology, size, sector count, etc.
 * This is the only thing a user can do with a device that does not support
 * MIFARE Classic.
 * @author Gerhard Klostermeier
 */
public class EndianReverseDecimal extends BasicActivity {

    private LinearLayout mLayout;
    private TextView UIDtext;
    private TextView mErrorMessage;
    private int mMFCSupport;

    /**
     * Calls {@link #updateTagInfo(Tag)} (and initialize some member
     * variables).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_endian_reverse_decimal);

        mLayout = findViewById(R.id.linearLayoutEndianReverseDecimal);
        UIDtext = findViewById(R.id.textEndianReverseDecimalUID);
        mErrorMessage = findViewById(
            R.id.textEndianReverseDecimalErrorMessage);
        //updateTagInfo(Common.getTag());
    }

    /**
     * Calls {@link Common#treatAsNewTag(Intent, android.content.Context)} and
     * then calls {@link #updateTagInfo(Tag)}
     */
    @Override
    public void onNewIntent(Intent intent) {
        Common.treatAsNewTag(intent, this);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            updateTagInfo(Common.getTag());
        }
    }

    /**
     * Update and display the tag information.
     * If there is no MIFARE Classic support, a warning will be shown.
     * @param tag A Tag from an NFC Intent.
     */
    @SuppressLint("SetTextI18n")
    private void updateTagInfo(Tag tag) {

        if (tag != null) {
            // Check for MIFARE Classic support.
            mMFCSupport = Common.checkMifareClassicSupport(tag, this);

            mLayout.removeAllViews();
            // Display generic info.
            // Create views and add them to the layout.

            int pad = Common.dpToPx(5); // 5dp to px.

            TextView genericInfo = new TextView(this);
            genericInfo.setPadding(pad, pad, pad, pad);
            TextViewCompat.setTextAppearance(genericInfo,
                android.R.style.TextAppearance_Medium);
            mLayout.addView(genericInfo);
            // Get generic info and set these as text.
            byte [] uid = tag.getId();

            reverse(uid);
            Long cardNumber = Long.parseLong(Common.bytes2Hex(uid), 16);
            String cardNumberPadded = ("0000000000" + cardNumber.toString()).substring(cardNumber.toString().length());
            UIDtext.setText(cardNumberPadded);

            LinearLayout layout = findViewById(
                R.id.linearLayoutEndianReverseDecimalSupport);
            // Check for MIFARE Classic support.
            if (mMFCSupport == 0) {

                layout.setVisibility(View.GONE);
            } else if (mMFCSupport == -1) {
                // No MIFARE Classic Support (due to the device hardware).
                // Set error message.
                mErrorMessage.setText(R.string.text_no_mfc_support_device);
                layout.setVisibility(View.VISIBLE);
            } else if (mMFCSupport == -2) {
                // The tag does not support MIFARE Classic.
                // Set error message.
                mErrorMessage.setText(R.string.text_no_mfc_support_tag);
                layout.setVisibility(View.VISIBLE);
            }
        } else {
            // There is no Tag.
            TextView text = new TextView(this);
            int pad = Common.dpToPx(5);
            text.setPadding(pad, pad, 0, 0);
            TextViewCompat.setTextAppearance(text, android.R.style.TextAppearance_Medium);
            text.setText(getString(R.string.text_no_tag));
            mLayout.removeAllViews();
            mLayout.addView(text);
            Toast.makeText(this, R.string.info_no_tag_found,
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get (determine) the tag type resource ID from ATQA + SAK + ATS.
     * If no resource is found check for the tag type only on ATQA + SAK
     * (and then on ATQA only).
     * @param atqa The ATQA from the tag.
     * @param sak The SAK from the tag.
     * @param ats The ATS from the tag.
     * @return The resource ID.
     */
    private int getTagIdentifier(String atqa, String sak, String ats) {
        String prefix = "tag_";
        ats = ats.replace("-", "");

        // First check on ATQA + SAK + ATS.
        int ret = getResources().getIdentifier(
            prefix + atqa + sak + ats, "string", getPackageName());

        if (ret == 0) {
            // Check on ATQA + SAK.
            ret = getResources().getIdentifier(
                prefix + atqa + sak, "string", getPackageName());
        }

        if (ret == 0) {
            // Check on ATQA.
            ret = getResources().getIdentifier(
                prefix + atqa, "string", getPackageName());
        }

        if (ret == 0) {
            // No match found return "Unknown".
            return R.string.tag_unknown;
        }
        return ret;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
}
