/*
* Copyright (C) 2014 AOSB Project
* Author Hany alsamman @codex-corp
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.systemui.bubbles.model;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.content.Context;
import android.content.ContentResolver;

import android.provider.ContactsContract;
import android.provider.CallLog.Calls;

import android.graphics.Bitmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.List;
import java.util.ArrayList;

import com.android.systemui.bubbles.ContactBubbleActivity;

public final class People extends ContactUtils {

	private static int CONTACT_LOGS_LIMIT = 5;

	public static List<Contact> PEOPLE_STARRED(Context ctx) {

		ArrayList<Contact> people = new ArrayList<Contact>();

		Cursor cursor = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, "starred=?", new String[] {"1"}, null);

		int i=0;
		int contactID;
		String contactName;
		Bitmap contactIcon;

		try {
			while (cursor.moveToNext()) {
				contactID = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				contactIcon = getContactIcon(contactID, ctx);
				people.add( new Contact(i, contactIcon, contactName, contactName,contactID) );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			cursor.close();
		}

		return people;
	}

	public static List<Contact> PEOPLE_LOGS(Context ctx) {

		ArrayList<Contact> people_logs = new ArrayList<Contact>();

		Cursor cursor = ctx.getContentResolver().query(Calls.CONTENT_URI, null, null, null, Calls.DATE + " DESC");

		int i=0;
		int contactID;
		int callDuration;
		int callContactID;
		String callNumber;
		String callName;
		Bitmap callContactIcon;
		try {
			while (cursor.moveToNext()) {

				if(i == CONTACT_LOGS_LIMIT) break;

				callName = cursor.getString( cursor.getColumnIndex(Calls.CACHED_NAME) );
				callNumber = cursor.getString( cursor.getColumnIndex(Calls.NUMBER) );
				callContactID = getContactIDFromNumber(callNumber, ctx);
				callDuration = cursor.getInt( cursor.getColumnIndex(Calls.DURATION) );
				callContactIcon = getContactIcon(Long.valueOf(callContactID), ctx);

				String dir = null;
				int dircode = cursor.getInt( cursor.getColumnIndex(Calls.TYPE) );
				switch (dircode) {
				case Calls.OUTGOING_TYPE:
					dir = "OUTGOING";
					break;

				case Calls.INCOMING_TYPE:
					dir = "INCOMING";
					break;

				case Calls.MISSED_TYPE:
					dir = "MISSED";
					break;
				}
				String callInfo = "Type: " + dir + "\nDuration: " + callDuration;
				
				people_logs.add( new Contact(i, callContactIcon, callName, callInfo, callContactID) );
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			cursor.close();
		}

		return people_logs;
	}

	public static View inflateContactView(Context context, ViewGroup parent, Contact contact) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ImageButton contactView = (ImageButton)inflater.inflate(com.android.systemui.R.layout.button_contact, parent, false);
		contactView.setImageBitmap(contact.getIcon());
		contactView.setContentDescription(contact.getName());
		contactView.setTag(contact);
		contactView.setOnClickListener(new iOSDoubleClick() {
			@Override
			public void onSingleClick(View v) {
				Context context = v.getContext();
				context.startActivity(ContactBubbleActivity.createIntent(context, v, (Contact)v.getTag()));
			}
			@Override
			public void onDoubleClick(View v) {
				Context context = v.getContext();
				ContactUtils.OpenContact(context,(Contact)v.getTag());
			}
		});
		return contactView;
	}
}
