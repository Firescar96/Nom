package com.firescar96.nom;

import android.content.Context;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

public class MonoText extends EditText implements TextWatcher {

	Context context;
	
	public MonoText(Context context) {
		super(context);
		this.context = context;
		addTextChangedListener(this);
	}

	public MonoText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		addTextChangedListener(this);
	}

	public MonoText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		addTextChangedListener(this);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	@Override
	public void afterTextChanged(Editable ed) {
		System.out.println("text chaningdd");
		String result = ed.toString().replaceAll(" ", "");
	    if (!ed.toString().equals(result)) 
	    {
	         setText(result);
	         setSelection(result.length());
	         // alert the user
	    }
	}

}
